package ru.practicum.shareit.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.ServiceTest;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

class ItemPutTest extends ServiceTest {

    @Test
    void putCorrectItem() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        ItemDto created = addItem(owner.getId(), "Drill");

        ItemDto updated = itemService.update(owner.getId(), created.getId(), ItemDto.builder()
                .name("Saw")
                .description("Hand saw")
                .available(false)
                .build());

        assertThat(updated)
                .hasFieldOrPropertyWithValue("id", created.getId())
                .hasFieldOrPropertyWithValue("name", "Saw")
                .hasFieldOrPropertyWithValue("description", "Hand saw")
                .hasFieldOrPropertyWithValue("available", false);
    }

    @Test
    void putOnlyNameKeepsOtherFields() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        ItemDto created = itemService.create(owner.getId(), ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build());

        ItemDto updated = itemService.update(owner.getId(), created.getId(), ItemDto.builder()
                .name("Saw")
                .build());

        assertThat(updated)
                .hasFieldOrPropertyWithValue("name", "Saw")
                .hasFieldOrPropertyWithValue("description", "Powerful drill")
                .hasFieldOrPropertyWithValue("available", true);
    }

    @Test
    void putBlankFieldsKeepValues() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        ItemDto created = addItem(owner.getId(), "Drill");

        ItemDto updated = itemService.update(owner.getId(), created.getId(), ItemDto.builder()
                .name("   ")
                .description("   ")
                .build());

        assertThat(updated)
                .hasFieldOrPropertyWithValue("name", "Drill")
                .hasFieldOrPropertyWithValue("description", "Drill");
    }

    @Test
    void putNullFieldsKeepValues() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        ItemDto created = addItem(owner.getId(), "Drill");

        ItemDto updated = itemService.update(owner.getId(), created.getId(), ItemDto.builder().build());

        assertThat(updated)
                .hasFieldOrPropertyWithValue("name", "Drill")
                .hasFieldOrPropertyWithValue("description", "Drill")
                .hasFieldOrPropertyWithValue("available", true);
    }

    @Test
    void putAvailableToFalse() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        ItemDto created = addItem(owner.getId(), "Drill");

        ItemDto updated = itemService.update(owner.getId(), created.getId(), ItemDto.builder()
                .available(false)
                .build());

        assertThat(updated).hasFieldOrPropertyWithValue("available", false);
    }

    @Test
    void putIncorrectItemId() {
        UserDto owner = addUser("test@yandex.ru", "TEST");

        assertThatThrownBy(() -> itemService.update(owner.getId(), 9999L, ItemDto.builder()
                .name("Saw")
                .build()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void putUnknownUser() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        ItemDto created = addItem(owner.getId(), "Drill");

        assertThatThrownBy(() -> itemService.update(9999L, created.getId(), ItemDto.builder()
                .name("Saw")
                .build()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void putByNotOwner() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        UserDto other = addUser("test2@yandex.ru", "TEST2");
        ItemDto created = addItem(owner.getId(), "Drill");

        assertThatThrownBy(() -> itemService.update(other.getId(), created.getId(), ItemDto.builder()
                .name("Saw")
                .build()))
                .isInstanceOf(ForbiddenException.class);
        assertThat(itemService.getById(created.getId()))
                .hasFieldOrPropertyWithValue("name", "Drill");
    }
}
