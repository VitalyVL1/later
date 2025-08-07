package ru.practicum.user;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class UserMapper {
    // Экземпляры мапперов для использования
    private static final Function<UserDto, User> DTO_TO_USER = new DtoToUserMapper();
    private static final Function<User, UserDto> USER_TO_DTO = new UserToDtoMapper();

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd, HH:mm:ss")
                    .withZone(ZoneId.systemDefault());

    // Базовый класс для маппера DTO -> User
    public static class DtoToUserMapper implements Function<UserDto, User> {
        @Override
        public User apply(UserDto dto) {
            return User.builder()
                    .id(dto.getId())
                    .lastName(dto.getLastName())
                    .firstName(dto.getFirstName())
                    .email(dto.getEmail())
                    .state(dto.getState())
                    .registrationDate(parseRegistrationDate(dto.getRegistrationDate()))
                    .build();
        }
    }

    // Базовый класс для маппера User -> DTO
    public static class UserToDtoMapper implements Function<User, UserDto> {
        @Override
        public UserDto apply(User user) {
            return UserDto.builder()
                    .id(user.getId())
                    .lastName(user.getLastName())
                    .firstName(user.getFirstName())
                    .email(user.getEmail())
                    .state(user.getState())
                    .registrationDate(formatRegistrationDate(user.getRegistrationDate()))
                    .build();
        }
    }

    // Дополнительные методы для удобства
    public static User mapToUser(UserDto dto) {
        return DTO_TO_USER.apply(dto);
    }

    public static UserDto mapToUserDTO(User user) {
        return USER_TO_DTO.apply(user);
    }

    public static List<User> mapToUser(List<UserDto> dtos) {
        return dtos.stream()
                .map(DTO_TO_USER)
                .toList();
    }

    public static List<UserDto> mapToUserDto(List<User> users) {
        return users.stream()
                .map(USER_TO_DTO)
                .toList();
    }

    public static User mapToNewUser(UserDto userDto) {
        return User.builder()
                .email(userDto.getEmail())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .state(userDto.getState())
                .build();
    }

    // Вспомогательные методы
    private static Instant parseRegistrationDate(String dateString) {
        if (dateString == null) return null;
        return Instant.from(DATE_TIME_FORMATTER.parse(dateString));
    }

    private static String formatRegistrationDate(Instant instant) {
        if (instant == null) return null;
        return DATE_TIME_FORMATTER.format(instant);
    }
}