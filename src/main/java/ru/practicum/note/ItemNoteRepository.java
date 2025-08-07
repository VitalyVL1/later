package ru.practicum.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.item.Item;

public interface ItemNoteRepository extends JpaRepository<Item, Long>, QuerydslPredicateExecutor<Item> {

}
