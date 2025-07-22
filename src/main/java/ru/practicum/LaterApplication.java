package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LaterApplication {
    private static final int PORT = 8080;

    public static void main(String[] args) {
        SpringApplication.run(LaterApplication.class, args);
    }
}
