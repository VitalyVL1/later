package ru.practicum.user;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ru.practicum.common.CommonExceptionHandler;

import static org.mockito.Mockito.mock;

@TestConfiguration
@EnableWebMvc
@Import(CommonExceptionHandler.class)
public class UserControllerTestConfig {
    @Bean
    public UserService userService() {
        return mock(UserService.class);
    }
}
