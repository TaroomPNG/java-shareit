package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        if (userRepository.existsByEmailIgnoreCase(userDto.getEmail())) {
            throw new ConflictException("User with email " + userDto.getEmail() + " already exists");
        }
        User user = userRepository.save(UserMapper.toUser(userDto));
        log.info("Created user id={}", user.getId());
        return UserMapper.toUserDto(user);
    }

    @Override
    @Transactional
    public UserDto update(Long userId, UserDto userDto) {
        User user = findUser(userId);
        if (userDto.getEmail() != null && !userDto.getEmail().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(userDto.getEmail())) {
                throw new ConflictException("Email " + userDto.getEmail() + " is already in use");
            }
            user.setEmail(userDto.getEmail());
        }
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        User updated = userRepository.save(user);
        log.info("Updated user id={}", userId);
        return UserMapper.toUserDto(updated);
    }

    @Override
    public UserDto getById(Long userId) {
        log.debug("Get user id={}", userId);
        return UserMapper.toUserDto(findUser(userId));
    }

    @Override
    public Collection<UserDto> getAll() {
        log.debug("Get all users");
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long userId) {
        userRepository.deleteById(userId);
        log.info("Deleted user id={}", userId);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }
}
