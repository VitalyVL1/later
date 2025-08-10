package ru.practicum.note;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.item.Item;
import ru.practicum.item.ItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemNoteServiceImpl implements ItemNoteService {
    private final ItemNoteRepository itemNoteRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemNoteDto addNewItemNote(long userId, ItemNoteDto itemNoteDto) {
        Item item = itemRepository.findById(itemNoteDto.getItemId())
                .orElseThrow(() -> new RuntimeException("Item not found"));
        ItemNote savedItemNote = itemNoteRepository.save(ItemNoteMapper.mapToItemNote(itemNoteDto, item));
        return ItemNoteMapper.mapToItemNoteDto(savedItemNote);
    }

    @Override
    public List<ItemNoteDto> searchNotesByUrl(String url, Long userId) {
        List<ItemNote> itemNotes = itemNoteRepository.findAllByItemUrlContainingAndItemUserId(url, userId);
        return ItemNoteMapper.mapToItemNoteDto(itemNotes);
    }

    @Override
    public List<ItemNoteDto> searchNotesByTag(long userId, String tag) {
        List<ItemNote> itemNotes = itemNoteRepository.findByUserIdAndTags(userId, tag);
        return ItemNoteMapper.mapToItemNoteDto(itemNotes);
    }

    @Override
    public List<ItemNoteDto> listAllItemsWithNotes(long userId, int from, int size) {
        PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size);
        return itemNoteRepository.findAllByItemUserId(userId, page)
                .map(ItemNoteMapper::mapToItemNoteDto)
                .getContent();
    }
}
