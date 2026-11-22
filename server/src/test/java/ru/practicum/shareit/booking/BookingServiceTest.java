package ru.practicum.shareit.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.ServiceTest;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

class BookingServiceTest extends ServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

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

    @Test
    void filtersBookingsByState() {
        LocalDateTime now = LocalDateTime.now();
        Booking past = saveBooking(now.minusDays(2), now.minusDays(1), BookingStatus.APPROVED);
        Booking current = saveBooking(now.minusHours(1), now.plusHours(1), BookingStatus.APPROVED);
        BookingDto waiting = bookingService.create(booker.getId(), request());
        BookingDto rejected = bookingService.create(booker.getId(), request(now.plusDays(3), now.plusDays(4)));
        bookingService.approve(owner.getId(), rejected.getId(), false);

        assertThat(ids(bookingService.getAllByBooker(booker.getId(), BookingState.PAST)))
                .containsExactly(past.getId());
        assertThat(ids(bookingService.getAllByBooker(booker.getId(), BookingState.CURRENT)))
                .containsExactly(current.getId());
        assertThat(ids(bookingService.getAllByBooker(booker.getId(), BookingState.FUTURE)))
                .containsExactly(rejected.getId(), waiting.getId());
        assertThat(ids(bookingService.getAllByBooker(booker.getId(), BookingState.WAITING)))
                .containsExactly(waiting.getId());
        assertThat(ids(bookingService.getAllByBooker(booker.getId(), BookingState.REJECTED)))
                .containsExactly(rejected.getId());

        assertThat(ids(bookingService.getAllByOwner(owner.getId(), BookingState.PAST)))
                .containsExactly(past.getId());
        assertThat(ids(bookingService.getAllByOwner(owner.getId(), BookingState.CURRENT)))
                .containsExactly(current.getId());
        assertThat(ids(bookingService.getAllByOwner(owner.getId(), BookingState.FUTURE)))
                .containsExactly(rejected.getId(), waiting.getId());
        assertThat(ids(bookingService.getAllByOwner(owner.getId(), BookingState.WAITING)))
                .containsExactly(waiting.getId());
        assertThat(ids(bookingService.getAllByOwner(owner.getId(), BookingState.REJECTED)))
                .containsExactly(rejected.getId());
    }

    @Test
    void approveRejectsUnknownUserNotOwnerAndSecondDecision() {
        BookingDto booking = bookingService.create(booker.getId(), request());

        assertThatThrownBy(() -> bookingService.approve(9999L, booking.getId(), true))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> bookingService.approve(booker.getId(), booking.getId(), true))
                .isInstanceOf(ForbiddenException.class);

        bookingService.approve(owner.getId(), booking.getId(), true);

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), booking.getId(), true))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getByIdHidesBookingFromStranger() {
        BookingDto booking = bookingService.create(booker.getId(), request());
        UserDto stranger = addUser("stranger@yandex.ru", "stranger");

        assertThatThrownBy(() -> bookingService.getById(stranger.getId(), booking.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createRejectsInvalidIntervalUnavailableItemAndOwner() {
        assertThatThrownBy(() -> bookingService.create(booker.getId(), BookingCreateDto.builder()
                .itemId(item.getId())
                .build()))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> bookingService.create(booker.getId(),
                request(LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1))))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> bookingService.create(booker.getId(),
                request(LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1))))
                .isInstanceOf(BadRequestException.class);

        itemService.update(owner.getId(), item.getId(), ItemDto.builder().available(false).build());
        assertThatThrownBy(() -> bookingService.create(booker.getId(), request()))
                .isInstanceOf(BadRequestException.class);

        ItemDto ownItem = addItem(booker.getId(), "Saw");
        assertThatThrownBy(() -> bookingService.create(booker.getId(), BookingCreateDto.builder()
                .itemId(ownItem.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void unknownStateIsBadRequest() {
        assertThatThrownBy(() -> BookingState.from("NOPE"))
                .isInstanceOf(BadRequestException.class);
    }

    private List<Long> ids(List<BookingDto> bookings) {
        return bookings.stream().map(BookingDto::getId).toList();
    }

    private Booking saveBooking(LocalDateTime start, LocalDateTime end, BookingStatus status) {
        return bookingRepository.save(Booking.builder()
                .start(start)
                .end(end)
                .item(itemRepository.findById(item.getId()).orElseThrow())
                .booker(userRepository.findById(booker.getId()).orElseThrow())
                .status(status)
                .build());
    }

    private BookingCreateDto request(LocalDateTime start, LocalDateTime end) {
        return BookingCreateDto.builder()
                .itemId(item.getId())
                .start(start)
                .end(end)
                .build();
    }

    private BookingCreateDto request() {
        return BookingCreateDto.builder()
                .itemId(item.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .build();
    }
}
