package ru.practicum.shareit.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.ServiceTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

class UserDeleteTest extends ServiceTest {

    @Test
    void deleteCorrectUser() {
        UserDto created = addUser("test@yandex.ru", "TEST");

        userService.delete(created.getId());

        assertThat(userRepository.existsById(created.getId())).isFalse();
        assertThat(userRepository.existsByEmail("test@yandex.ru")).isFalse();
        assertThatThrownBy(() -> userService.getById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteUnknownUser() {
        addUser("test@yandex.ru", "TEST");

        userService.delete(9999L);

        assertThat(userService.getAll()).hasSize(1);
    }

    @Test
    void createUserWithEmailAfterDelete() {
        UserDto created = addUser("test@yandex.ru", "TEST");
        userService.delete(created.getId());

        UserDto recreated = addUser("test@yandex.ru", "TEST");

        assertThat(recreated.getId()).isEqualTo(2L);
        assertThat(recreated).hasFieldOrPropertyWithValue("email", "test@yandex.ru");
    }
}
