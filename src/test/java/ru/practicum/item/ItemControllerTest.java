package ru.practicum.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.item.dto.AddItemRequest;
import ru.practicum.item.dto.GetItemRequest;
import ru.practicum.item.dto.ItemDto;
import ru.practicum.item.dto.ModifyItemRequest;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
class ItemControllerTest {
    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private ItemService itemService;

    @Autowired
    private MockMvc mvc;

    ItemDto itemDto = new ItemDto(
            1L,
            "https://ya.ru/",
            "https://yandex.ru/",
            "text",
            "Яндекс",
            true,
            false,
            true,
            "2022.07.03 00:00:00",
            new HashSet<>(List.of("tag1", "tag2", "tag3"))
    );

    @Test
    public void getTest() throws Exception {
        when(itemService.getItems(any(GetItemRequest.class))).thenReturn(List.of(itemDto));
        mvc.perform(get("/items")
                        .header("X-Later-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.id()), Long.class))
                .andExpect(jsonPath("$[0].normalUrl", is(itemDto.normalUrl()), String.class))
                .andExpect(jsonPath("$[0].resolvedUrl", is(itemDto.resolvedUrl()), String.class))
                .andExpect(jsonPath("$[0].mimeType", is(itemDto.mimeType()), String.class))
                .andExpect(jsonPath("$[0].title", is(itemDto.title()), String.class))
                .andExpect(jsonPath("$[0].hasImage", is(itemDto.hasImage()), Boolean.class))
                .andExpect(jsonPath("$[0].hasVideo", is(itemDto.hasVideo()), Boolean.class))
                .andExpect(jsonPath("$[0].unread", is(itemDto.unread()), Boolean.class))
                .andExpect(jsonPath("$[0].dateResolved", is(itemDto.dateResolved()), String.class))
                .andExpect(jsonPath("$[0].tags", hasItems(itemDto.tags().toArray())));
    }

    @Test
    public void getWithTagsTest() throws Exception {
        when(itemService.getItems(any(GetItemRequest.class))).thenReturn(List.of(itemDto));
        mvc.perform(get("/items")
                        .header("X-Later-User-Id", 1)
                        .param("state", "unread")
                        .param("contentType", "all")
                        .param("sort", "newest")
                        .param("limit", "10")
                        .param("tags", "tag1", "tag2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.id()), Long.class));
    }

    @Test
    public void addTest() throws Exception {
        AddItemRequest addItemRequest =
                new AddItemRequest("https://ya.ru/", new HashSet<>(List.of("tag1", "tag2", "tag3")));

        when(itemService.addNewItem(anyLong(), any(AddItemRequest.class))).thenReturn(itemDto);

        mvc.perform(post("/items")
                        .header("X-Later-User-Id", 1)
                        .param("state", "unread")
                        .param("contentType", "all")
                        .param("sort", "newest")
                        .param("limit", "10")
                        .content(mapper.writeValueAsString(addItemRequest))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.id()), Long.class))
                .andExpect(jsonPath("$.normalUrl", is(itemDto.normalUrl()), String.class))
                .andExpect(jsonPath("$.resolvedUrl", is(itemDto.resolvedUrl()), String.class))
                .andExpect(jsonPath("$.mimeType", is(itemDto.mimeType()), String.class))
                .andExpect(jsonPath("$.title", is(itemDto.title()), String.class))
                .andExpect(jsonPath("$.hasImage", is(itemDto.hasImage()), Boolean.class))
                .andExpect(jsonPath("$.hasVideo", is(itemDto.hasVideo()), Boolean.class))
                .andExpect(jsonPath("$.unread", is(itemDto.unread()), Boolean.class))
                .andExpect(jsonPath("$.dateResolved", is(itemDto.dateResolved()), String.class))
                .andExpect(jsonPath("$.tags", hasItems(itemDto.tags().toArray())));
    }

    @Test
    public void deleteItemTest() throws Exception {
        doNothing().when(itemService).deleteItem(anyLong(), anyLong());

        mvc.perform(delete("/items/1")
                        .header("X-Later-User-Id", 1))
                .andExpect(status().isOk());

        verify(itemService).deleteItem(1L, 1L);
    }

    @Test
    public void modifyItemTest() throws Exception {
        ModifyItemRequest modifyRequest = ModifyItemRequest.builder()
                .itemId(1L)
                .read(true)
                .tags(new HashSet<>(List.of("newTag1", "newTag2")))
                .replaceTags(true)
                .build();

        ItemDto updatedItemDto = new ItemDto(
                1L,
                "https://updated.url",
                "https://updated-resolved.url",
                "text",
                "Updated Title",
                true,
                false,
                false,
                "2022.07.03 00:00:00",
                new HashSet<>(List.of("newTag1", "newTag2"))
        );

        when(itemService.modifyItem(anyLong(), any(ModifyItemRequest.class))).thenReturn(updatedItemDto);

        mvc.perform(patch("/items")
                        .header("X-Later-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(modifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updatedItemDto.id()), Long.class))
                .andExpect(jsonPath("$.normalUrl", is(updatedItemDto.normalUrl()), String.class))
                .andExpect(jsonPath("$.resolvedUrl", is(updatedItemDto.resolvedUrl()), String.class))
                .andExpect(jsonPath("$.unread", is(updatedItemDto.unread()), Boolean.class))
                .andExpect(jsonPath("$.tags", hasItems(updatedItemDto.tags().toArray())));

        verify(itemService).modifyItem(eq(1L), any(ModifyItemRequest.class));
    }

    @Test
    public void getItemsWithoutUserIdHeaderShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/items"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getItemsWithInvalidStateShouldReturnBadRequest() throws Exception {
        mvc.perform(get("/items")
                        .header("X-Later-User-Id", 1)
                        .param("state", "invalid")
                        .param("contentType", "all")
                        .param("sort", "newest")
                        .param("limit", "10"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void getItemsWithDefaultParameters() throws Exception {
        when(itemService.getItems(any(GetItemRequest.class))).thenReturn(List.of(itemDto));

        mvc.perform(get("/items")
                        .header("X-Later-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemDto.id()), Long.class));
    }

}