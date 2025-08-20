package ru.practicum.item;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.common.NotFoundException;
import ru.practicum.item.dto.AddItemRequest;
import ru.practicum.item.dto.GetItemRequest;
import ru.practicum.item.dto.ItemDto;
import ru.practicum.item.dto.ModifyItemRequest;
import ru.practicum.item.model.Item;
import ru.practicum.item.model.QItem;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final UrlMetaDataRetriever urlMetaDataRetriever;

    @Override
    public List<ItemDto> getItems(long userId) {
        return ItemMapper.mapToItemDto(itemRepository.findByUserId(userId));
    }

    @Override
    public List<ItemDto> getItems(GetItemRequest request) {
        // Для поиска ссылок используем QueryDSL чтобы было удобно настраивать разные варианты фильтров
        QItem item = QItem.item;
        // Мы будем анализировать какие фильтры указал пользователь
        // И все нужные условия фильтрации будем собирать в список
        List<BooleanExpression> conditions = new ArrayList<>();
        // Условие, которое будет проверяться всегда - пользователь сделавший запрос
        // должен быть тем же пользователем, что сохранил ссылку
        conditions.add(item.user.id.eq(request.userId()));

        // Проверяем один из фильтров указанных в запросе - state
        GetItemRequest.State state = request.state();
        // Если пользователь указал, что его интересуют все ссылки, вне зависимости
        // от состояния, тогда пропускаем этот фильтр. В обратном случае анализируем
        // указанное состояние и формируем подходящее условие для запроса
        if (!state.equals(GetItemRequest.State.ALL)) {
            conditions.add(makeStateCondition(state));
        }

        // Если пользователь указал, что его интересуют ссылки вне зависимости
        // от типа их содержимого, то пропускаем фильтра, иначе анализируем
        // указанный тип контента и формируем соответствующее условие
        GetItemRequest.ContentType contentType = request.contentType();
        if (!contentType.equals(GetItemRequest.ContentType.ALL)) {
            conditions.add(makeContentTypeCondition(contentType));
        }

        // если пользователя интересуют ссылки с конкретными тэгами,
        // то добавляем это условие в запрос
        if (request.tags() != null && !request.tags().isEmpty()) {
            conditions.add(item.tags.any().in(request.tags()));
        }

        // из всех подготовленных условий, составляем единое условие
        BooleanExpression finalCondition = conditions.stream()
                .reduce(BooleanExpression::and)
                .get();

        // анализируем, какой вариант сортировки выбрал пользователь
        // и какое количество элементов он выбрал для отображения
        Sort sort = makeOrderByClause(request.sort());
        PageRequest pageRequest = PageRequest.of(0, request.limit(), sort);

        // выполняем запрос к базе данных со всеми подготовленными настройками
        // конвертируем результат в DTO и возвращаем контроллеру
        Iterable<Item> items = itemRepository.findAll(finalCondition, pageRequest);
        return ItemMapper.mapToItemDto(items);
    }

    @Transactional
    @Override
    public ItemDto addNewItem(long userId, AddItemRequest request) {
        User user = userRepository.getReferenceById(userId);
        UrlMetaDataRetriever.UrlMetadata urlMetadata =
                urlMetaDataRetriever.retrieve(request.url());

        Item item;
        Optional<Item> foundItem = itemRepository.findByUserAndResolvedUrl(user, urlMetadata.getResolvedUrl());

        if (foundItem.isPresent()) {
            item = foundItem.get();
            if (request.tags() != null || !request.tags().isEmpty()) {
                item.getTags().addAll(request.tags());
                itemRepository.save(item);
            }
        } else {
            item = itemRepository.save(ItemMapper.mapToItem(urlMetadata, user, request.tags()));
        }

        return ItemMapper.mapToItemDto(item);
    }

    @Override
    public void deleteItem(long userId, long item) {
        itemRepository.deleteByUserIdAndId(userId, item);
    }

    @Override
    public ItemDto modifyItem(long userId, ModifyItemRequest request) {
        Optional<Item> itemOpt = itemRepository.findByUserIdAndId(userId, request.itemId());

        Item item = itemOpt.orElseThrow(
                () -> new NotFoundException(String.format("Item with id %d not found", request.itemId()))
        );

        item.setUnread(!request.read());

        if (request.replaceTags()) {
            item.getTags().clear();
        }

        if (request.hasTags()) {
            item.getTags().addAll(request.tags());
        }

        return ItemMapper.mapToItemDto(itemRepository.save(item));
    }

    private BooleanExpression makeStateCondition(GetItemRequest.State state) {
        switch (state) {
            case READ:
                return QItem.item.unread.isFalse();
            case UNREAD:
            default:
                return QItem.item.unread.isTrue();
        }
    }

    private BooleanExpression makeContentTypeCondition(GetItemRequest.ContentType contentType) {
        switch (contentType) {
            case IMAGE:
                return QItem.item.mimeType.eq("image");
            case VIDEO:
                return QItem.item.mimeType.eq("video");
            case ARTICLE:
            default:
                return QItem.item.mimeType.eq("text");
        }
    }

    private Sort makeOrderByClause(GetItemRequest.Sort sort) {
        switch (sort) {
            case TITLE:
                return Sort.by("title");
            case SITE:
                return Sort.by("resolvedUrl");
            case OLDEST:
                return Sort.by("dateResolved");
            case NEWEST:
            default:
                return Sort.by("dateResolved").descending();
        }
    }

}
