package ru.practicum.user;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class UserRepositoryInmemory implements UserRepository {
    Map<Long, User> users = new HashMap<>();


    @Override
    public List<User> findAll() {
        if (users.isEmpty()) {
            loadUserData(5);
        }

        return users.values().stream().toList();
    }

    @Override
    public User save(User user) {
        return users.put(user.getId(), user);
    }

    private void loadUserData(int count) {
        for (int i = 0; i < count; i++) {
            User user = new User();
            user.setId((long) i + 1);
            user.setName("user" + i);
            user.setEmail(String.format("user%d@email.com", i));

            users.put(user.getId(), user);
        }
    }
}
