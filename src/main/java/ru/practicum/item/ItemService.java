package ru.practicum.item;

import java.util.List;
import java.util.Set;

public interface ItemService {
    List<ItemDto> getItems(long userId, Set<String> tags);

    ItemDto addNewItem(long userId, ItemDto item);

    void deleteItem(long userId, long item);

}
