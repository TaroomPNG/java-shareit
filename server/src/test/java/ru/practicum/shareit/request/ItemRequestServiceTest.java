package ru.practicum.shareit.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.ServiceTest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestItemDto;
import ru.practicum.shareit.user.dto.UserDto;

class ItemRequestServiceTest extends ServiceTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Test
    void create() {
        UserDto requester = addUser("requester@yandex.ru", "requester");

        ItemRequestDto created = itemRequestService.create(requester.getId(),
                ItemRequestDto.builder().description("Need a drill").build());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Need a drill");
        assertThat(created.getCreated()).isNotNull();
        assertThat(created.getItems()).isEmpty();
    }

    @Test
    void getOwn() throws InterruptedException {
        UserDto requester = addUser("requester@yandex.ru", "requester");
        ItemRequestDto older = itemRequestService.create(requester.getId(),
                ItemRequestDto.builder().description("older").build());
        Thread.sleep(5);
        ItemRequestDto newer = itemRequestService.create(requester.getId(),
                ItemRequestDto.builder().description("newer").build());

        assertThat(itemRequestService.getOwn(requester.getId()))
                .extracting(ItemRequestDto::getId)
                .containsExactly(newer.getId(), older.getId());
    }

    @Test
    void getOthers() {
        UserDto requester = addUser("requester@yandex.ru", "requester");
        UserDto other = addUser("other@yandex.ru", "other");
        ItemRequestDto created = itemRequestService.create(requester.getId(),
                ItemRequestDto.builder().description("Need a drill").build());
        itemRequestService.create(other.getId(),
                ItemRequestDto.builder().description("Own request").build());

        assertThat(itemRequestService.getOthers(other.getId(), 0, 10))
                .extracting(ItemRequestDto::getId)
                .containsExactly(created.getId());
    }

    @Test
    void getById() {
        UserDto requester = addUser("requester@yandex.ru", "requester");
        UserDto owner = addUser("owner@yandex.ru", "owner");
        ItemRequestDto created = itemRequestService.create(requester.getId(),
                ItemRequestDto.builder().description("Need a drill").build());
        ItemDto item = itemService.create(owner.getId(), ItemDto.builder()
                .name("Drill")
                .description("Cordless")
                .available(true)
                .requestId(created.getId())
                .build());

        ItemRequestDto found = itemRequestService.getById(owner.getId(), created.getId());

        assertThat(found.getItems())
                .extracting(RequestItemDto::getId, RequestItemDto::getName, RequestItemDto::getOwnerId)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(item.getId(), "Drill", owner.getId()));
    }
}
