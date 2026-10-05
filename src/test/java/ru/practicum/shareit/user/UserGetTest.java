package ru.practicum.shareit.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.ServiceTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

class UserGetTest extends ServiceTest {

    @Test
    void getCorrectUser() {
        UserDto created = addUser("test@yandex.ru", "TEST");

        assertThat(userService.getById(created.getId()))
                .hasFieldOrPropertyWithValue("id", created.getId())
                .hasFieldOrPropertyWithValue("name", "TEST")
                .hasFieldOrPropertyWithValue("email", "test@yandex.ru");
    }

    @Test
    void getIncorrectUserId() {
        assertThatThrownBy(() -> userService.getById(9999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllUsers() {
        UserDto first = addUser("test@yandex.ru", "TEST");
        UserDto second = addUser("test2@yandex.ru", "TEST2");

        assertThat(userService.getAll())
                .extracting(UserDto::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void getAllWhenEmpty() {
        assertThat(userService.getAll()).isEmpty();
        assertThat(userRepository.existsById(1L)).isFalse();
        assertThat(userRepository.existsByEmail(null)).isFalse();
    }

    @Test
    void findByIdReturnsCopy() {
        UserDto created = addUser("test@yandex.ru", "TEST");
        User stored = userRepository.findById(created.getId()).orElseThrow();
        stored.setName("CHANGED");

        assertThat(userService.getById(created.getId()))
                .hasFieldOrPropertyWithValue("name", "TEST");
    }
}
