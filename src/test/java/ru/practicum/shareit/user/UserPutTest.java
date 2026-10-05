package ru.practicum.shareit.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.ServiceTest;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

class UserPutTest extends ServiceTest {

    @Test
    void putCorrectUser() {
        UserDto created = addUser("test@yandex.ru", "TEST");

        UserDto updated = userService.update(created.getId(), UserDto.builder()
                .email("new@yandex.ru")
                .build());

        assertThat(updated)
                .hasFieldOrPropertyWithValue("id", created.getId())
                .hasFieldOrPropertyWithValue("email", "new@yandex.ru")
                .hasFieldOrPropertyWithValue("name", "TEST");
        assertThat(userRepository.existsByEmail("test@yandex.ru")).isFalse();
        assertThat(userRepository.existsByEmail("new@yandex.ru")).isTrue();
    }

    @Test
    void putOnlyNameKeepsEmail() {
        UserDto created = addUser("test@yandex.ru", "TEST");

        UserDto updated = userService.update(created.getId(), UserDto.builder()
                .name("NEW")
                .build());

        assertThat(updated)
                .hasFieldOrPropertyWithValue("id", created.getId())
                .hasFieldOrPropertyWithValue("name", "NEW")
                .hasFieldOrPropertyWithValue("email", "test@yandex.ru");
    }

    @Test
    void putBlankNameKeepsName() {
        UserDto created = addUser("test@yandex.ru", "TEST");

        UserDto updated = userService.update(created.getId(), UserDto.builder()
                .name("   ")
                .build());

        assertThat(updated).hasFieldOrPropertyWithValue("name", "TEST");
    }

    @Test
    void putNullFieldsKeepsUser() {
        UserDto created = addUser("test@yandex.ru", "TEST");

        UserDto updated = userService.update(created.getId(), UserDto.builder().build());

        assertThat(updated)
                .hasFieldOrPropertyWithValue("name", "TEST")
                .hasFieldOrPropertyWithValue("email", "test@yandex.ru");
    }

    @Test
    void putSameEmailIgnoringCase() {
        UserDto created = addUser("test@yandex.ru", "TEST");

        UserDto updated = userService.update(created.getId(), UserDto.builder()
                .email("TEST@yandex.ru")
                .build());

        assertThat(updated).hasFieldOrPropertyWithValue("email", "test@yandex.ru");
    }

    @Test
    void putIncorrectUserId() {
        assertThatThrownBy(() -> userService.update(9999L, UserDto.builder()
                .name("TEST")
                .build()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void putDuplicateEmail() {
        UserDto created = addUser("test@yandex.ru", "TEST");
        addUser("test2@yandex.ru", "TEST2");

        assertThatThrownBy(() -> userService.update(created.getId(), UserDto.builder()
                .email("TEST2@yandex.ru")
                .build()))
                .isInstanceOf(ConflictException.class);
        assertThat(userService.getById(created.getId()))
                .hasFieldOrPropertyWithValue("email", "test@yandex.ru");
    }
}
