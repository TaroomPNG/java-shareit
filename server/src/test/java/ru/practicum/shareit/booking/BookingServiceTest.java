package ru.practicum.shareit.booking;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.ServiceTest;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

class BookingServiceTest extends ServiceTest {

    @Autowired
    private BookingService bookingService;

    private UserDto owner;
    private UserDto booker;
    private ItemDto item;

    @BeforeEach
    void setUp() {
        owner = addUser("owner@yandex.ru", "owner");
        booker = addUser("booker@yandex.ru", "booker");
        item = addItem(owner.getId(), "Drill");
    }

    @Test
    void create() {
        BookingDto booking = bookingService.create(booker.getId(), request());

        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(booking.getBooker().getId()).isEqualTo(booker.getId());
        assertThat(booking.getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void approve() {
        BookingDto booking = bookingService.create(booker.getId(), request());

        BookingDto approved = bookingService.approve(owner.getId(), booking.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void getById() {
        BookingDto booking = bookingService.create(booker.getId(), request());

        assertThat(bookingService.getById(booker.getId(), booking.getId()).getId())
                .isEqualTo(booking.getId());
    }

    @Test
    void getAllByBooker() {
        BookingDto booking = bookingService.create(booker.getId(), request());

        assertThat(bookingService.getAllByBooker(booker.getId(), BookingState.ALL))
                .extracting(BookingDto::getId)
                .containsExactly(booking.getId());
    }

    @Test
    void getAllByOwner() {
        BookingDto booking = bookingService.create(booker.getId(), request());

        assertThat(bookingService.getAllByOwner(owner.getId(), BookingState.ALL))
                .extracting(BookingDto::getId)
                .containsExactly(booking.getId());
    }

    private BookingCreateDto request() {
        return BookingCreateDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
    }
}
