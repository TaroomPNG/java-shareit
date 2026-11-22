package ru.practicum.shareit.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.ServiceTest;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.user.dto.UserDto;

class UserPostTest extends ServiceTest {

    @Test
    void addCorrectUser() {
        UserDto user = addUser("test@yandex.ru", "TEST");

        assertThat(user.getId()).isNotNull();
        assertThat(user)
                .hasFieldOrPropertyWithValue("name", "TEST")
                .hasFieldOrPropertyWithValue("email", "test@yandex.ru");
        assertThat(userRepository.existsById(user.getId())).isTrue();
        assertThat(userRepository.existsByEmailIgnoreCase("TEST@yandex.ru")).isTrue();
    }

    @Test
    void addUsersWithSequentialIds() {
        UserDto first = addUser("test@yandex.ru", "TEST");
        UserDto second = addUser("test2@yandex.ru", "TEST2");

        assertThat(second.getId()).isGreaterThan(first.getId());
    }

    @Test
    void createDuplicateEmail() {
        addUser("test@yandex.ru", "TEST");

        assertThatThrownBy(() -> addUser("test@yandex.ru", "TEST2"))
                .isInstanceOf(ConflictException.class);
        assertThat(userService.getAll()).hasSize(1);
    }

    @Test
    void createDuplicateEmailIgnoringCase() {
        addUser("Test@Yandex.ru", "TEST");

        assertThatThrownBy(() -> addUser("test@yandex.ru", "TEST2"))
                .isInstanceOf(ConflictException.class);
    }
}
