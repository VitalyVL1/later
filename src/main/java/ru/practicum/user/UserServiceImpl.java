package ru.practicum.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<UserDto> getAllUsers() {
        return UserMapper.mapToUserDto(repository.findAll());
    }

    @Transactional
    @Override
    public UserDto saveUser(UserDto dto) {
        User user = repository.save(UserMapper.mapToNewUser(dto));
        return UserMapper.mapToUserDTO(user);
    }
}