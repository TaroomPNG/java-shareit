package ru.practicum.shareit.item;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.ServiceTest;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

class ItemCommentTest extends ServiceTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void addComment() {
        UserDto owner = addUser("owner@yandex.ru", "owner");
        UserDto booker = addUser("booker@yandex.ru", "booker");
        ItemDto item = addItem(owner.getId(), "Drill");
        LocalDateTime now = LocalDateTime.now();
        bookingRepository.save(Booking.builder()
                .start(now.minusDays(2))
                .end(now.minusDays(1))
                .item(itemRepository.findById(item.getId()).orElseThrow())
                .booker(userRepository.findById(booker.getId()).orElseThrow())
                .status(BookingStatus.APPROVED)
                .build());

        CommentDto comment = itemService.addComment(booker.getId(), item.getId(),
                CommentDto.builder().text("Good drill").build());

        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getText()).isEqualTo("Good drill");
        assertThat(comment.getAuthorName()).isEqualTo("booker");
        assertThat(comment.getCreated()).isNotNull();
    }
}
