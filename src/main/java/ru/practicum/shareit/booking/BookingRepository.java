package ru.practicum.shareit.booking;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(
            Long itemId, BookingStatus status, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
            Long itemId, BookingStatus status, LocalDateTime now);

    boolean existsByItemIdAndBookerIdAndStatusAndEndIsBefore(
            Long itemId, Long bookerId, BookingStatus status, LocalDateTime end);

    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long bookerId,
                                                              LocalDateTime start,
                                                              LocalDateTime end,
                                                              Sort sort);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartIsAfter(Long bookerId, LocalDateTime start, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Sort sort);

    List<Booking> findByItemOwnerId(Long ownerId, Sort sort);

    List<Booking> findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(Long ownerId,
                                                                 LocalDateTime start,
                                                                 LocalDateTime end,
                                                                 Sort sort);

    List<Booking> findByItemOwnerIdAndEndIsBefore(Long ownerId, LocalDateTime end, Sort sort);

    List<Booking> findByItemOwnerIdAndStartIsAfter(Long ownerId, LocalDateTime start, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Sort sort);
}
