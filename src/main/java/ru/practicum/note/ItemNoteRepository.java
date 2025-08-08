package ru.practicum.note;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.item.Item;

import java.util.List;

public interface ItemNoteRepository extends JpaRepository<ItemNote, Long>, QuerydslPredicateExecutor<Item> {

    List<ItemNote> findAllByUrlContainingAndItemUserId(String url, long userId);

    @Query("""
            select inote from ItemNote as inote
            join inote.item as i
            where i.user.id = ?1
            and ?2 member of i.tags
            """)
    List<ItemNote> findByUserIdAndTags(long userId, String tag);

    Page<ItemNote> findAllByItemUserId(long userId, Pageable pageable);
}
