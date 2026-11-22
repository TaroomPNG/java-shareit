package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private static final Sort SORT_BY_START_DESC = Sort.by(Sort.Direction.DESC, "start");

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto create(Long bookerId, BookingCreateDto createDto) {
        User booker = findUser(bookerId);
        Item item = findItem(createDto.getItemId());
        validateCreate(createDto, booker, item);

        Booking booking = Booking.builder()
                .start(createDto.getStart())
                .end(createDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        Booking saved = bookingRepository.save(booking);
        log.info("Created booking id={} by booker id={} for item id={}",
                saved.getId(), bookerId, item.getId());
        return BookingMapper.toBookingDto(saved);
    }

    @Override
    @Transactional
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        if (!userRepository.existsById(ownerId)) {
            throw new ForbiddenException("User " + ownerId + " not found");
        }
        Booking booking = findBooking(bookingId);
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("User " + ownerId + " is not the owner of booking " + bookingId);
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Booking " + bookingId + " is already " + booking.getStatus());
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updated = bookingRepository.save(booking);
        log.info("Booking id={} status set to {}", bookingId, updated.getStatus());
        return BookingMapper.toBookingDto(updated);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        findUser(userId);
        Booking booking = findBooking(bookingId);
        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();
        if (!userId.equals(bookerId) && !userId.equals(ownerId)) {
            throw new NotFoundException("Booking " + bookingId + " is not available for user " + userId);
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllByBooker(Long bookerId, BookingState state) {
        findUser(bookerId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByBookerId(bookerId, SORT_BY_START_DESC);
            case CURRENT -> bookingRepository.findByBookerIdAndStartIsBeforeAndEndIsAfter(
                    bookerId, now, now, SORT_BY_START_DESC);
            case PAST -> bookingRepository.findByBookerIdAndEndIsBefore(bookerId, now, SORT_BY_START_DESC);
            case FUTURE -> bookingRepository.findByBookerIdAndStartIsAfter(bookerId, now, SORT_BY_START_DESC);
            case WAITING -> bookingRepository.findByBookerIdAndStatus(
                    bookerId, BookingStatus.WAITING, SORT_BY_START_DESC);
            case REJECTED -> bookingRepository.findByBookerIdAndStatus(
                    bookerId, BookingStatus.REJECTED, SORT_BY_START_DESC);
        };
        return bookings.stream().map(BookingMapper::toBookingDto).toList();
    }

    @Override
    public List<BookingDto> getAllByOwner(Long ownerId, BookingState state) {
        findUser(ownerId);
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case ALL -> bookingRepository.findByItemOwnerId(ownerId, SORT_BY_START_DESC);
            case CURRENT -> bookingRepository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(
                    ownerId, now, now, SORT_BY_START_DESC);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndIsBefore(ownerId, now, SORT_BY_START_DESC);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartIsAfter(ownerId, now, SORT_BY_START_DESC);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatus(
                    ownerId, BookingStatus.WAITING, SORT_BY_START_DESC);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatus(
                    ownerId, BookingStatus.REJECTED, SORT_BY_START_DESC);
        };
        return bookings.stream().map(BookingMapper::toBookingDto).toList();
    }

    private void validateCreate(BookingCreateDto createDto, User booker, Item item) {
        LocalDateTime start = createDto.getStart();
        LocalDateTime end = createDto.getEnd();
        LocalDateTime now = LocalDateTime.now();

        if (start == null || end == null) {
            throw new BadRequestException("Start and end must not be null");
        }
        if (!end.isAfter(start)) {
            throw new BadRequestException("End must be after start");
        }
        if (start.isBefore(now)) {
            throw new BadRequestException("Start must be in the future");
        }
        if (end.isBefore(now)) {
            throw new BadRequestException("End must be in the future");
        }
        if (Boolean.FALSE.equals(item.getAvailable())) {
            throw new BadRequestException("Item " + item.getId() + " is not available");
        }
        if (booker.getId().equals(item.getOwner().getId())) {
            throw new NotFoundException("Owner cannot book their own item");
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }

    private Item findItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id " + itemId + " not found"));
    }

    private Booking findBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with id " + bookingId + " not found"));
    }
}
