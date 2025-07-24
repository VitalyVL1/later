package ru.practicum.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;

    public ItemServiceImpl() {
        log.info("Creating ItemServiceImpl");
        this.itemRepository = new ItemRepositoryImpl();
    }

    @Override
    public List<Item> getItems(long id) {
        log.info("Getting items by id: " + id);
        return itemRepository.findByUserId(id);
    }

    @Override
    public Item addNewItem(Long userId, Item item) {
        log.info("Adding new item: " + item);
        return itemRepository.save(userId, item);
    }

    @Override
    public void deleteItem(long userId, long item) {
        log.info("Deleting item: " + item);
        itemRepository.deleteByUserIdAndItemId(userId, item);
    }
}
