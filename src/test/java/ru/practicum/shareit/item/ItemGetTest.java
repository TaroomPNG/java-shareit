package ru.practicum.shareit.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.ServiceTest;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;

class ItemGetTest extends ServiceTest {

    @Test
    void getCorrectItem() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        ItemDto created = addItem(owner.getId(), "Drill");

        assertThat(itemService.getById(created.getId()))
                .hasFieldOrPropertyWithValue("id", created.getId())
                .hasFieldOrPropertyWithValue("name", "Drill")
                .hasFieldOrPropertyWithValue("description", "Drill")
                .hasFieldOrPropertyWithValue("available", true);
    }

    @Test
    void getIncorrectItemId() {
        assertThatThrownBy(() -> itemService.getById(9999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByOwner() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        UserDto other = addUser("test2@yandex.ru", "TEST2");
        ItemDto first = addItem(owner.getId(), "Drill");
        addItem(other.getId(), "Saw");
        ItemDto third = addItem(owner.getId(), "Hammer");

        assertThat(itemService.getAllByOwner(owner.getId()))
                .extracting(ItemDto::getId)
                .containsExactly(first.getId(), third.getId());
    }

    @Test
    void getAllByOwnerWhenEmpty() {
        UserDto owner = addUser("test@yandex.ru", "TEST");

        assertThat(itemService.getAllByOwner(owner.getId())).isEmpty();
    }

    @Test
    void getAllByUnknownOwner() {
        assertThatThrownBy(() -> itemService.getAllByOwner(9999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByOwnerSkipsItemsWithoutOwner() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        ItemDto own = addItem(owner.getId(), "Drill");
        itemRepository.create(Item.builder()
                .name("Orphan")
                .description("Orphan")
                .available(true)
                .build());

        assertThat(itemService.getAllByOwner(owner.getId()))
                .extracting(ItemDto::getName)
                .containsExactly(own.getName());
    }

    @Test
    void findByIdReturnsCopy() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        ItemDto created = addItem(owner.getId(), "Drill");
        Item stored = itemRepository.findById(created.getId()).orElseThrow();
        stored.setName("CHANGED");

        assertThat(itemService.getById(created.getId()))
                .hasFieldOrPropertyWithValue("name", "Drill");
    }
}
