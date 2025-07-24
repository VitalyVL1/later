package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<User> getAllUsers() {
        log.info("getAllUsers");
        return repository.findAll();
    }

    @Override
    public User saveUser(User user) {
        log.info("saveUser" + user);
        return repository.save(user);
    }
}