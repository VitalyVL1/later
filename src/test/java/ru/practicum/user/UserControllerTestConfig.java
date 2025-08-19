package ru.practicum.user;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableWebMvc
@Import(ru.practicum.CommonExceptionHandler.class)
public class UserControllerTestConfig {
    @Bean
    public UserService userService() {
        return mock(UserService.class);
    }
}
