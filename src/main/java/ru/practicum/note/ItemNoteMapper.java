package ru.practicum.note;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.item.Item;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemNoteMapper {

    // Экземпляры мапперов для использования
    private static final Function<ItemNoteDto, ItemNote> DTO_TO_ITEM_NOTE = new DtoToItemNoteMapper();
    private static final Function<ItemNote, ItemNoteDto> ITEM_NOTE_TO_DTO = new ItemNoteToDtoMapper();

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd, HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    public static class DtoToItemNoteMapper implements Function<ItemNoteDto, ItemNote> {
        @Override
        public ItemNote apply(ItemNoteDto itemNoteDto) {
            return ItemNote.builder()
                    .id(itemNoteDto.getId())
                    .text(itemNoteDto.getText())
                    .itemNoteDate(parseItemNoteDate(itemNoteDto.getItemNoteDate()))
                    .build();
        }
    }

    public static class ItemNoteToDtoMapper implements Function<ItemNote, ItemNoteDto> {
        @Override
        public ItemNoteDto apply(ItemNote itemNote) {
            return ItemNoteDto.builder()
                    .id(itemNote.getId())
                    .itemId(itemNote.getItem().getId())
                    .text(itemNote.getText())
                    .itemNoteDate(formatItemNoteDate(itemNote.getItemNoteDate()))
                    .itemUrl(itemNote.getItem().getUrl())
                    .build();
        }
    }

    public static ItemNote mapToItemNote(ItemNoteDto dto, Item item) {
        ItemNote itemNote = DTO_TO_ITEM_NOTE.apply(dto);
        itemNote.setItem(item);
        return itemNote;
    }

    public static ItemNoteDto mapToItemNoteDto(ItemNote itemNote) {
        return ITEM_NOTE_TO_DTO.apply(itemNote);
    }

    public static List<ItemNoteDto> mapToItemNoteDto(List<ItemNote> itemNotes) {
        return itemNotes.stream()
                .map(ITEM_NOTE_TO_DTO)
                .toList();
    }


    // Вспомогательные методы
    private static Instant parseItemNoteDate(String dateString) {
        if (dateString == null) return null;
        return Instant.from(DATE_TIME_FORMATTER.parse(dateString));
    }

    private static String formatItemNoteDate(Instant instant) {
        if (instant == null) return null;
        return DATE_TIME_FORMATTER.format(instant);
    }
}
