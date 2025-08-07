package ru.practicum.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {
    public static final DtoToItemMapper DTO_TO_ITEM = new DtoToItemMapper();
    public static final ItemToDtoMapper ITEM_TO_DTO = new ItemToDtoMapper();

    public static class DtoToItemMapper implements Function<ItemDto, Item> {
        @Override
        public Item apply(ItemDto itemDTO) {
            return Item.builder()
                    .id(itemDTO.getId())
                    .userId(itemDTO.getUserId())
                    .tags(itemDTO.getTags())
                    .build();
        }
    }

    public static class ItemToDtoMapper implements Function<Item, ItemDto> {
        @Override
        public ItemDto apply(Item item) {
            return ItemDto.builder()
                    .id(item.getId())
                    .userId(item.getUserId())
                    .tags(item.getTags())
                    .build();
        }
    }

    // Дополнительные методы для удобства
    public static Item mapToItem(ItemDto dto) {
        return DTO_TO_ITEM.apply(dto);
    }

    public static ItemDto mapToItemDto(Item item) {
        return ITEM_TO_DTO.apply(item);
    }

    public static List<Item> mapToItem(List<ItemDto> dtos) {
        return dtos.stream()
                .map(DTO_TO_ITEM)
                .toList();
    }

    public static List<ItemDto> mapToItemDto(List<Item> items) {
        return items.stream()
                .map(ITEM_TO_DTO)
                .toList();
    }

    public static List<ItemDto> mapToItemDto(Iterable<Item> items) {
        List<Item> dtos = new ArrayList<>();
        items.forEach(dtos::add);
        return dtos.stream()
                .map(ITEM_TO_DTO)
                .toList();
    }
}
