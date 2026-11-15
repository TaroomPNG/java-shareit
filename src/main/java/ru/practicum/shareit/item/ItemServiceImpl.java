package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = findUser(userId);
        Item item = itemRepository.save(ItemMapper.toItem(itemDto, owner));
        log.info("Created item id={} by owner id={}", item.getId(), userId);
        return ItemMapper.toItemDto(item);
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        findUser(userId);
        Item item = findItem(itemId);
        if (!userId.equals(item.getOwner().getId())) {
            throw new NotFoundException("User " + userId + " is not the owner of item " + itemId);
        }
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        Item updated = itemRepository.save(item);
        log.info("Updated item id={} by owner id={}", itemId, userId);
        return ItemMapper.toItemDto(updated);
    }

    @Override
    public ItemDto getById(Long userId, Long itemId) {
        log.debug("Get item id={} by user id={}", itemId, userId);
        findUser(userId);
        Item item = findItem(itemId);
        ItemDto dto = ItemMapper.toItemDto(item);
        if (userId.equals(item.getOwner().getId())) {
            enrichWithBookings(dto, item.getId());
        }
        enrichWithComments(dto, item.getId());
        return dto;
    }

    @Override
    public Collection<ItemDto> getAllByOwner(Long userId) {
        log.debug("Get all items of owner id={}", userId);
        findUser(userId);
        return itemRepository.findAllByOwnerIdOrderByIdAsc(userId).stream()
                .map(item -> {
                    ItemDto dto = ItemMapper.toItemDto(item);
                    enrichWithBookings(dto, item.getId());
                    enrichWithComments(dto, item.getId());
                    return dto;
                })
                .toList();
    }

    @Override
    public Collection<ItemDto> search(String text) {
        log.debug("Search items by text='{}'", text);
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchAvailable(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = findUser(userId);
        Item item = findItem(itemId);
        LocalDateTime now = LocalDateTime.now();
        boolean hasFinishedBooking = bookingRepository
                .existsByItemIdAndBookerIdAndStatusAndEndIsBefore(
                        itemId, userId, BookingStatus.APPROVED, now);
        if (!hasFinishedBooking) {
            throw new BadRequestException(
                    "User " + userId + " has no finished booking for item " + itemId);
        }
        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(now)
                .build();
        Comment saved = commentRepository.save(comment);
        log.info("Created comment id={} for item id={} by user id={}",
                saved.getId(), itemId, userId);
        return CommentMapper.toCommentDto(saved);
    }

    private void enrichWithBookings(ItemDto dto, Long itemId) {
        LocalDateTime now = LocalDateTime.now();
        BookingShortDto lastBooking = bookingRepository
                .findFirstByItemIdAndStatusAndStartLessThanEqualOrderByStartDesc(
                        itemId, BookingStatus.APPROVED, now)
                .map(BookingMapper::toBookingShortDto)
                .orElse(null);
        BookingShortDto nextBooking = bookingRepository
                .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(
                        itemId, BookingStatus.APPROVED, now)
                .map(BookingMapper::toBookingShortDto)
                .orElse(null);
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
    }

    private void enrichWithComments(ItemDto dto, Long itemId) {
        dto.setComments(commentRepository.findAllByItemIdOrderByCreatedDesc(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .toList());
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }

    private Item findItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id " + itemId + " not found"));
    }
}
