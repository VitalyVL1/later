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
        properties = "jdbc.url=jdbc:postgresql://localhost:5432/test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserServiceImplTest {

    private final EntityManager em;
    private final UserService service;

    @Test
    void testSaveUser() {
        UserDto userDto = makeUserDto("some@email.com", "Пётр", "Иванов");

        service.saveUser(userDto);

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
                .peek(service::saveUser)
                .collect(Collectors.toList());

        // Выполнение
        List<UserDto> actualUsers = service.getAllUsers();

        // Проверки
        assertThat(actualUsers, hasSize(expectedUsers.size()));

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