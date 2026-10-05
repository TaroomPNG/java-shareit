package ru.practicum.shareit;

import org.junit.jupiter.api.BeforeEach;
import ru.practicum.shareit.item.InMemoryItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.InMemoryUserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.UserServiceImpl;
import ru.practicum.shareit.user.dto.UserDto;

public abstract class ServiceTest {

    protected InMemoryUserRepository userRepository;

    protected InMemoryItemRepository itemRepository;

    protected UserService userService;

    protected ItemService itemService;

    @BeforeEach
    void resetData() {
        userRepository = new InMemoryUserRepository();
        itemRepository = new InMemoryItemRepository();
        userService = new UserServiceImpl(userRepository);
        itemService = new ItemServiceImpl(itemRepository, userRepository);
    }

    protected UserDto addUser(String email, String name) {
        return userService.create(UserDto.builder()
                .email(email)
                .name(name)
                .build());
    }

    protected ItemDto addItem(Long ownerId, String name) {
        return itemService.create(ownerId, ItemDto.builder()
                .name(name)
                .description(name)
                .available(true)
                .build());
    }
}
