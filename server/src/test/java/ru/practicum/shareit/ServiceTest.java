package ru.practicum.shareit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.dto.UserDto;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public abstract class ServiceTest {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ItemRepository itemRepository;

    @Autowired
    protected UserService userService;

    @Autowired
    protected ItemService itemService;

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
