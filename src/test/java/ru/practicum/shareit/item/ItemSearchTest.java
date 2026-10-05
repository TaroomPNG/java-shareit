package ru.practicum.shareit.item;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.ServiceTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserDto;

class ItemSearchTest extends ServiceTest {

    @Test
    void searchByNameIgnoringCase() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        ItemDto drill = addItem(owner.getId(), "Electric drill");
        addItem(owner.getId(), "Saw");

        assertThat(itemService.search("DRILL"))
                .extracting(ItemDto::getId)
                .containsExactly(drill.getId());
    }

    @Test
    void searchByDescription() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        ItemDto item = itemService.create(owner.getId(), ItemDto.builder()
                .name("Tool")
                .description("Powerful drill")
                .available(true)
                .build());

        assertThat(itemService.search("powerful"))
                .extracting(ItemDto::getId)
                .containsExactly(item.getId());
    }

    @Test
    void searchSkipsUnavailable() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        itemService.create(owner.getId(), ItemDto.builder()
                .name("Drill")
                .description("Drill")
                .available(false)
                .build());

        assertThat(itemService.search("drill")).isEmpty();
    }

    @Test
    void searchBlankText() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        addItem(owner.getId(), "Drill");

        assertThat(itemService.search("   ")).isEmpty();
        assertThat(itemService.search("")).isEmpty();
    }

    @Test
    void searchNullText() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        addItem(owner.getId(), "Drill");

        assertThat(itemService.search(null)).isEmpty();
    }

    @Test
    void searchNoMatches() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        addItem(owner.getId(), "Drill");

        assertThat(itemService.search("ladder")).isEmpty();
    }

    @Test
    void searchKeepsInsertionOrder() {
        UserDto owner = addUser("test@yandex.ru", "TEST");
        ItemDto first = addItem(owner.getId(), "Drill one");
        addItem(owner.getId(), "Saw");
        ItemDto third = addItem(owner.getId(), "Drill two");

        assertThat(itemService.search("drill"))
                .extracting(ItemDto::getId)
                .containsExactly(first.getId(), third.getId());
    }

    @Test
    void searchSkipsNullNameAndDescription() {
        itemRepository.create(Item.builder()
                .name(null)
                .description("only-description")
                .available(true)
                .build());
        itemRepository.create(Item.builder()
                .name("only-name")
                .description(null)
                .available(true)
                .build());
        itemRepository.create(Item.builder()
                .name("hidden")
                .description("hidden")
                .available(null)
                .build());

        assertThat(itemRepository.searchAvailable("only-description"))
                .extracting(Item::getDescription)
                .containsExactly("only-description");
        assertThat(itemRepository.searchAvailable("only-name"))
                .extracting(Item::getName)
                .containsExactly("only-name");
        assertThat(itemRepository.searchAvailable("hidden")).isEmpty();
    }
}
