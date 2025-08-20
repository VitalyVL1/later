package ru.practicum.item;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.item.dto.AddItemRequest;
import ru.practicum.item.dto.GetItemRequest;
import ru.practicum.item.dto.ItemDto;
import ru.practicum.item.dto.ModifyItemRequest;

import java.util.List;

@Transactional(readOnly = true)
public interface ItemService {
    List<ItemDto> getItems(long userId);

    List<ItemDto> getItems(GetItemRequest request);

    @Transactional
    ItemDto addNewItem(long userId, AddItemRequest request);

    @Transactional
    void deleteItem(long userId, long item);

    @Transactional
    ItemDto modifyItem(long userId, ModifyItemRequest request);

}
