package ru.practicum.item;

import java.util.List;

public interface ItemService {
    List<ItemDto> getItems(long id);

    ItemDto addNewItem(long userId, ItemDto item);

    void deleteItem(long userId, long item);

}
