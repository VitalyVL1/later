package ru.practicum.item;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private Map<Long, Map<Long, Item>> items = new HashMap<>();
    private Set<Long> itemIds = new HashSet<>();

    @Override
    public List<Item> findByUserId(long userId) {
        if (!itemIds.contains(userId)) {
            return Collections.emptyList();
        }
        return items.get(userId).values().stream().toList();
    }

    @Override
    public Item save(long userId, Item item) {
        if (!items.containsKey(userId)) {
            items.put(userId, new HashMap<>());
        }
        item.setId(generateItemId());
        items.get(userId).put(item.getId(), item);
        return item;
    }

    @Override
    public void deleteByUserIdAndItemId(long userId, long itemId) {
        if (!itemIds.contains(itemId)) {
            return;
        }
        items.get(userId).remove(itemId);
    }

    public long generateItemId() {
        long id = itemIds.stream()
                .max(Long::compare)
                .orElse(0L);
        return ++id;
    }
}
