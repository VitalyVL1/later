package ru.practicum.item;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.item.dto.ItemDto;
import ru.practicum.user.User;
import ru.practicum.user.UserRepository;
import ru.practicum.user.UserState;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;

@Transactional
@SpringBootTest(
        properties = "spring.datasource.url=jdbc:postgresql://localhost:5432/later", //здесь можно прописать нужную тестовую базу, сейчас стоит основная.
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceImplTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserRepository userRepository;
    private ItemDto itemDto;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User(
                1L,
                "john.doe@mail.com",
                "John",
                "Doe",
                Instant.parse("2020-01-01T00:00:00Z"),
                UserState.ACTIVE);
        itemDto = new ItemDto(
                1L,
                "https://ya.ru/",
                "https://yandex.ru/",
                "text",
                "Яндекс",
                true,
                false,
                true,
                "2022.07.03 00:00:00",
                new HashSet<>(List.of("tag1", "tag2", "tag3"))
        );
        userRepository.save(user);
    }

    @Test
    public void addNewItem_Test() {

    }

    @Test
    public void getItems_ByUserId_Test() {


    }

    @Test
    public void getItems_ByRequest() {

    }

    @Test
    public void modifyItem_Test() {

    }

    @Test
    public void deleteItem_Test() {

    }
}