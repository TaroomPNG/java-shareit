package ru.practicum.shareit.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.ServiceTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

class ItemPostTest extends ServiceTest {

    @Test
    void addCorrectItem() {
        UserDto owner = addUser("test@yandex.ru", "TEST");

        ItemDto item = itemService.create(owner.getId(), ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(false)
                .build());

        assertThat(item)
                .hasFieldOrPropertyWithValue("id", 1L)
                .hasFieldOrPropertyWithValue("name", "Drill")
                .hasFieldOrPropertyWithValue("description", "Powerful drill")
                .hasFieldOrPropertyWithValue("available", false);
    }

    @Test
    void addItemsWithSequentialIds() {
        UserDto owner = addUser("test@yandex.ru", "TEST");

        ItemDto first = addItem(owner.getId(), "Drill");
        ItemDto second = addItem(owner.getId(), "Saw");

        assertThat(first.getId()).isEqualTo(1L);
        assertThat(second.getId()).isEqualTo(2L);
    }

    @Test
    void createItemForUnknownUser() {
        assertThatThrownBy(() -> addItem(9999L, "Drill"))
                .isInstanceOf(NotFoundException.class);
        assertThat(itemRepository.findAllByOwnerId(9999L)).isEmpty();
    }
}
