package ru.practicum.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.item.model.Item;
import ru.practicum.user.User;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, QuerydslPredicateExecutor<Item> {

    List<Item> findByUserId(long userId);

    void deleteByUserIdAndId(long userId, long itemId);

    List<Item> findAllByUserLastNameStartingWith(String lastNamePrefix);

    Optional<Item> findByUserAndResolvedUrl(User user, String resolvedUrl);

    Optional<Item> findByUserIdAndId(long userId, long itemId);
}