package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.BadRequestException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
public class BookingController {

    public static final String USER_HEADER = "X-Sharer-User-Id";

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader(USER_HEADER) Long userId,
                                         @Valid @RequestBody BookingCreateDto createDto) {
        validateDates(createDto);
        return bookingClient.create(userId, createDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approve(@RequestHeader(USER_HEADER) Long userId,
                                          @PathVariable Long bookingId,
                                          @RequestParam boolean approved) {
        return bookingClient.approve(userId, bookingId, approved);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByBooker(@RequestHeader(USER_HEADER) Long userId,
                                                 @RequestParam(defaultValue = "ALL") String state) {
        requireState(state);
        return bookingClient.getAllByBooker(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByOwner(@RequestHeader(USER_HEADER) Long userId,
                                                @RequestParam(defaultValue = "ALL") String state) {
        requireState(state);
        return bookingClient.getAllByOwner(userId, state);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getById(@RequestHeader(USER_HEADER) Long userId,
                                          @PathVariable Long bookingId) {
        return bookingClient.getById(userId, bookingId);
    }

    private void validateDates(BookingCreateDto createDto) {
        LocalDateTime start = createDto.getStart();
        LocalDateTime end = createDto.getEnd();
        LocalDateTime now = LocalDateTime.now();
        if (!end.isAfter(start)) {
            throw new BadRequestException("End must be after start");
        }
        if (start.isBefore(now)) {
            throw new BadRequestException("Start must be in the future");
        }
        if (end.isBefore(now)) {
            throw new BadRequestException("End must be in the future");
        }
    }

    private void requireState(String state) {
        if (BookingState.from(state).isEmpty()) {
            throw new BadRequestException("Unknown state: " + state);
        }
    }
}
