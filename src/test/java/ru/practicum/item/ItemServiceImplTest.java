package ru.practicum.item;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.NotFoundException;
import ru.practicum.item.dto.AddItemRequest;
import ru.practicum.item.dto.GetItemRequest;
import ru.practicum.item.dto.ItemDto;
import ru.practicum.item.dto.ModifyItemRequest;
import ru.practicum.item.model.Item;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;
import ru.practicum.user.UserState;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@Transactional
@SpringBootTest(
        properties = "spring.datasource.url=jdbc:postgresql://localhost:5432/later", //здесь можно прописать нужную тестовую базу, сейчас стоит основная.
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @MockitoBean
    private UrlMetaDataRetriever urlMetaDataRetriever;

    private User user;
    private UrlMetaDataRetriever.UrlMetadata urlMetadata;
    private Item item;

    private static final String NORMAL_URL = "https://txt.ru/";
    private static final String RESOLVED_URL = "https://text.ru/";
    private static final String MIME_TYPE = "text";
    private static final String TITLE = "Текст";
    private static final Boolean HAS_IMAGE = false;
    private static final Boolean HAS_VIDEO = false;
    private static final Instant DATE_RESOLVED = Instant.parse("2022-07-03T00:00:00Z");
    private static final Boolean UNREAD = true;
    private static final Set<String> TAGS = new HashSet<>(List.of("txt1", "txt2", "txt3"));

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("john.doe@mail.com")
                .firstName("John")
                .lastName("Doe")
                .registrationDate(Instant.parse("2020-01-01T00:00:00Z"))
                .state(UserState.ACTIVE)
                .build();
        user = userRepository.save(user);

        urlMetadata = mock(UrlMetaDataRetriever.UrlMetadata.class);
        when(urlMetadata.getNormalUrl()).thenReturn(NORMAL_URL);
        when(urlMetadata.getResolvedUrl()).thenReturn(RESOLVED_URL);
        when(urlMetadata.getMimeType()).thenReturn(MIME_TYPE);
        when(urlMetadata.getTitle()).thenReturn(TITLE);
        when(urlMetadata.isHasImage()).thenReturn(HAS_IMAGE);
        when(urlMetadata.isHasVideo()).thenReturn(HAS_VIDEO);
        when(urlMetadata.getDateResolved()).thenReturn(DATE_RESOLVED);

        item = Item.builder()
                .url(NORMAL_URL)
                .resolvedUrl(RESOLVED_URL)
                .mimeType(MIME_TYPE)
                .title(TITLE)
                .hasImage(HAS_IMAGE)
                .hasVideo(HAS_VIDEO)
                .dateResolved(DATE_RESOLVED)
                .unread(UNREAD)
                .tags(TAGS)
                .user(user)
                .build();
    }

    @Test
    void addNewItem_WhenItemNotExists_ShouldCreateNewItem() {
        // Подготовка данных
        AddItemRequest request = new AddItemRequest(NORMAL_URL, TAGS);
        when(urlMetaDataRetriever.retrieve(NORMAL_URL)).thenReturn(urlMetadata);

        // Выполнение
        itemService.addNewItem(user.getId(), request);
        TypedQuery<Item> query = em.createQuery("Select i from Item i where i.url = :url", Item.class);
        Item item = query.setParameter("url", NORMAL_URL).getSingleResult();
        // Проверки

        assertThat(item.getId(), notNullValue());
        assertThat(item.getUrl(), equalTo(NORMAL_URL));
        assertThat(item.getResolvedUrl(), equalTo(RESOLVED_URL));
        assertThat(item.getMimeType(), equalTo(MIME_TYPE));
        assertThat(item.getTitle(), equalTo(TITLE));
        assertThat(item.isHasImage(), is(HAS_IMAGE));
        assertThat(item.isHasVideo(), is(HAS_VIDEO));
        assertThat(item.getDateResolved(), equalTo(DATE_RESOLVED));
        assertThat(item.isUnread(), is(UNREAD));
        assertThat(item.getTags(), containsInAnyOrder(TAGS.toArray()));


        verify(urlMetaDataRetriever).retrieve(NORMAL_URL);
    }

    @Test
    void addNewItem_WhenItemExists_ShouldUpdateTags() {
        // Подготовка данных
        AddItemRequest request = new AddItemRequest(NORMAL_URL, Set.of("newTag"));
        Item existingItem = itemRepository.save(item);

        when(urlMetaDataRetriever.retrieve(NORMAL_URL)).thenReturn(urlMetadata);

        Set<String> tags = new HashSet<>();
        tags.addAll(TAGS);
        tags.add("newTag");

        // Выполнение
        ItemDto result = itemService.addNewItem(user.getId(), request);

        // Проверки
        assertNotNull(result);
        assertThat(result.tags(), containsInAnyOrder(tags.toArray()));
        verify(urlMetaDataRetriever).retrieve(NORMAL_URL);
    }

    @Test
    void getItems_ByUserId_ShouldReturnUserItems() {
        // Подготовка данных
        itemRepository.save(item);

        // Выполнение
        List<ItemDto> actualItems = itemService.getItems(user.getId());
        ItemDto actualItem = actualItems.get(0);


        // Проверки
        // т.к. ItemDto record, а не класс то использовать hasItem(allAff(hasProperty())) - не получится, т.к. нет геттеров и сеттеров
        assertNotNull(actualItem.id());
        assertEquals(NORMAL_URL, actualItem.normalUrl());
        assertEquals(RESOLVED_URL, actualItem.resolvedUrl());
        assertEquals(MIME_TYPE, actualItem.mimeType());
        assertEquals(TITLE, actualItem.title());
        assertEquals(HAS_IMAGE, actualItem.hasImage());
        assertEquals(HAS_VIDEO, actualItem.hasVideo());
        assertEquals(UNREAD, actualItem.unread());
        assertEquals(DATE_RESOLVED.toString(), actualItem.dateResolved());
        assertThat(actualItem.tags(), containsInAnyOrder(TAGS.toArray()));
    }

    @Test
    void getItems_ByRequestWithStateFilter_ShouldReturnFilteredItems() {
        // Подготовка данных
        Item item1 = ItemMapper.mapToItem(urlMetadata, user, Set.of("tag1"));
        item1.setUnread(true);
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setUser(user);
        item2.setUrl("https://google.com/");
        item2.setResolvedUrl("https://google.com/");
        item2.setMimeType(MIME_TYPE);
        item2.setTitle("Google");
        item2.setHasImage(HAS_IMAGE);
        item2.setHasVideo(HAS_VIDEO);
        item2.setDateResolved(DATE_RESOLVED);
        item2.setUnread(false); // READ
        item2.setTags(new HashSet<>(Set.of("tag2")));
        itemRepository.save(item2);

        GetItemRequest request = GetItemRequest.builder()
                .userId(user.getId())
                .state(GetItemRequest.State.UNREAD)
                .contentType(GetItemRequest.ContentType.ALL)
                .sort(GetItemRequest.Sort.NEWEST)
                .limit(10)
                .build();

        // Выполнение
        List<ItemDto> result = itemService.getItems(request);

        // Проверки
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).unread());
    }

    @Test
    void getItems_ByRequestWithContentTypeFilter_ShouldReturnFilteredItems() {
        // Подготовка данных
        UrlMetaDataRetriever.UrlMetadata imageMetadata = mock(UrlMetaDataRetriever.UrlMetadata.class);
        when(imageMetadata.getNormalUrl()).thenReturn("https://image.com/");
        when(imageMetadata.getResolvedUrl()).thenReturn("https://image.com/");
        when(imageMetadata.getMimeType()).thenReturn("image");
        when(imageMetadata.getTitle()).thenReturn("Image");
        when(imageMetadata.isHasImage()).thenReturn(true);
        when(imageMetadata.isHasVideo()).thenReturn(false);
        when(imageMetadata.getDateResolved()).thenReturn(DATE_RESOLVED);

        Item textItem = new Item();
        textItem.setUser(user);
        textItem.setUrl(NORMAL_URL);
        textItem.setResolvedUrl(RESOLVED_URL);
        textItem.setMimeType("text");
        textItem.setTitle(TITLE);
        textItem.setHasImage(HAS_IMAGE);
        textItem.setHasVideo(HAS_VIDEO);
        textItem.setDateResolved(DATE_RESOLVED);
        textItem.setUnread(UNREAD);
        textItem.setTags(new HashSet<>(Set.of("tag1")));
        itemRepository.save(textItem);

        Item imageItem = new Item();
        imageItem.setUser(user);
        imageItem.setUrl("https://image.com/");
        imageItem.setResolvedUrl("https://image.com/");
        imageItem.setMimeType("image");
        imageItem.setTitle("Image");
        imageItem.setHasImage(true);
        imageItem.setHasVideo(false);
        imageItem.setDateResolved(DATE_RESOLVED);
        imageItem.setUnread(UNREAD);
        imageItem.setTags(new HashSet<>(Set.of("tag2")));
        itemRepository.save(imageItem);

        GetItemRequest request = GetItemRequest.builder()
                .userId(user.getId())
                .state(GetItemRequest.State.ALL)
                .contentType(GetItemRequest.ContentType.IMAGE)
                .sort(GetItemRequest.Sort.NEWEST)
                .limit(10)
                .build();

        // Выполнение
        List<ItemDto> result = itemService.getItems(request);

        // Проверки
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("image", result.get(0).mimeType());
    }

    @Test
    void getItems_ByRequestWithTagsFilter_ShouldReturnFilteredItems() {
        // Подготовка данных
        Item item1 = new Item();
        item1.setUser(user);
        item1.setUrl("https://site1.com/");
        item1.setResolvedUrl("https://site1.com/");
        item1.setMimeType(MIME_TYPE);
        item1.setTitle("Site 1");
        item1.setHasImage(HAS_IMAGE);
        item1.setHasVideo(HAS_VIDEO);
        item1.setDateResolved(DATE_RESOLVED);
        item1.setUnread(UNREAD);
        item1.setTags(new HashSet<>(Set.of("tag1", "common")));
        itemRepository.save(item1);

        Item item2 = new Item();
        item2.setUser(user);
        item2.setUrl("https://site2.com/");
        item2.setResolvedUrl("https://site2.com/");
        item2.setMimeType(MIME_TYPE);
        item2.setTitle("Site 2");
        item2.setHasImage(HAS_IMAGE);
        item2.setHasVideo(HAS_VIDEO);
        item2.setDateResolved(DATE_RESOLVED);
        item2.setUnread(UNREAD);
        item2.setTags(new HashSet<>(Set.of("tag2", "common")));
        itemRepository.save(item2);

        GetItemRequest request = GetItemRequest.builder()
                .userId(user.getId())
                .state(GetItemRequest.State.ALL)
                .contentType(GetItemRequest.ContentType.ALL)
                .tags(Set.of("tag1"))
                .sort(GetItemRequest.Sort.NEWEST)
                .limit(10)
                .build();

        // Выполнение
        List<ItemDto> result = itemService.getItems(request);

        // Проверки
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).tags().contains("tag1"));
    }

    @Test
    void modifyItem_WhenItemExists_ShouldUpdateItem() {
        // Подготовка данных
        Item item = new Item();
        item.setUser(user);
        item.setUrl(NORMAL_URL);
        item.setResolvedUrl(RESOLVED_URL);
        item.setMimeType(MIME_TYPE);
        item.setTitle(TITLE);
        item.setHasImage(HAS_IMAGE);
        item.setHasVideo(HAS_VIDEO);
        item.setDateResolved(DATE_RESOLVED);
        item.setUnread(UNREAD);
        item.setTags(new HashSet<>(Set.of("oldTag")));
        item = itemRepository.save(item);

        ModifyItemRequest request = new ModifyItemRequest(
                item.getId(),
                true, // mark as read
                Set.of("newTag1", "newTag2"),
                true // replace tags
        );

        // Выполнение
        ItemDto result = itemService.modifyItem(user.getId(), request);

        // Проверки
        assertNotNull(result);
        assertFalse(result.unread());
        assertTrue(result.tags().contains("newTag1"));
        assertTrue(result.tags().contains("newTag2"));
        assertFalse(result.tags().contains("oldTag"));
    }

    @Test
    void modifyItem_WhenItemNotFound_ShouldThrowException() {
        // Подготовка данных
        ModifyItemRequest request = new ModifyItemRequest(999L, false, Set.of("tag"), true);

        // Выполнение и Проверки
        assertThrows(NotFoundException.class, () -> {
            itemService.modifyItem(user.getId(), request);
        });
    }

    @Test
    void deleteItem_ShouldRemoveItem() {
        // Подготовка данных
        Item item = ItemMapper.mapToItem(urlMetadata, user, Set.of("tag"));
        item = itemRepository.save(item);

        long initialCount = itemRepository.count();

        // Выполнение
        itemService.deleteItem(user.getId(), item.getId());

        // Проверки
        assertEquals(initialCount - 1, itemRepository.count());
        assertFalse(itemRepository.existsById(item.getId()));
    }

    @Test
    void deleteItem_WhenItemNotExists_ShouldNotThrowException() {
        // Подготовка данных
        long initialCount = itemRepository.count();

        // Выполнение и Проверки
        assertDoesNotThrow(() -> {
            itemService.deleteItem(user.getId(), 999L);
        });

        assertEquals(initialCount, itemRepository.count());
    }
}