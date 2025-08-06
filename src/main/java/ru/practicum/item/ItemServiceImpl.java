package ru.practicum.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    @Override
    public List<ItemDto> getItems(long id) {
        return ItemMapper.mapToDto(itemRepository.findByUserId(id));
    }

    @Transactional
    @Override
    public ItemDto addNewItem(long userId, ItemDto itemDto) {
        itemDto.setUserId(userId);
        Item savedItem = itemRepository.save(ItemMapper.mapToItem(itemDto));
        return ItemMapper.mapToDto(savedItem);
    }

    @Override
    public void deleteItem(long userId, long item) {
        itemRepository.deleteByUserIdAndId(userId, item);
    }
}
