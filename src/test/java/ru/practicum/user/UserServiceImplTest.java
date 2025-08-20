package ru.practicum.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "spring.datasource.url=jdbc:postgresql://localhost:5432/later", //здесь можно прописать нужную тестовую базу, сейчас стоит основная.
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTest {

    private final EntityManager em;
    private final UserService userService;

    @Test
    void checkDatabaseConnection() {
        // Выполнить запрос к БД, чтобы узнать её имя
        String databaseName = (String) em.createNativeQuery("SELECT current_database();").getSingleResult();
        String schemaName = (String) em.createNativeQuery("SELECT current_schema();").getSingleResult();

        System.out.println(">>>>>>> Current database: " + databaseName);
        System.out.println(">>>>>>> Current schema: " + schemaName);

        assertThat(databaseName, equalTo("later"));
    }

    @Test
    void testSaveUser() {
        UserDto userDto = makeUserDto("some@email.com", "Пётр", "Иванов");

        userService.saveUser(userDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail())
                .getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getFirstName(), equalTo(userDto.getFirstName()));
        assertThat(user.getLastName(), equalTo(userDto.getLastName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
        assertThat(user.getState(), equalTo(userDto.getState()));
        assertThat(user.getRegistrationDate(), notNullValue());
    }

    private UserDto makeUserDto(String email, String firstName, String lastName) {
        UserDto dto = new UserDto();
        dto.setEmail(email);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setState(UserState.ACTIVE);

        return dto;
    }

    @Test
    void testGetAllUsers() {
        // Подготовка данных
        List<UserDto> expectedUsers = IntStream.range(0, 10)
                .mapToObj(i -> makeUserDto(
                        "user" + i + "@email.com",
                        "FirstName" + i,
                        "LastName" + i
                ))
                .map(userService::saveUser)
                .collect(Collectors.toList());

        // Выполнение
        List<UserDto> actualUsers = userService.getAllUsers();

        // Проверки
        for (UserDto expected : expectedUsers) {
            assertThat(actualUsers, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("email", equalTo(expected.getEmail())),
                    hasProperty("firstName", equalTo(expected.getFirstName())),
                    hasProperty("lastName", equalTo(expected.getLastName()))
            )));
        }
    }
}