package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestDto create(Long userId, ItemRequestDto requestDto) {
        User requester = findUser(userId);
        if (requestDto.getDescription() == null || requestDto.getDescription().isBlank()) {
            throw new BadRequestException("Request description must not be blank");
        }
        ItemRequest request = itemRequestRepository.save(ItemRequest.builder()
                .description(requestDto.getDescription())
                .requester(requester)
                .created(LocalDateTime.now())
                .build());
        log.info("Created item request id={} by user id={}", request.getId(), userId);
        return ItemRequestMapper.toDto(request, List.of());
    }

    @Override
    public Collection<ItemRequestDto> getOwn(Long userId) {
        log.debug("Get own item requests of user id={}", userId);
        findUser(userId);
        return toDtos(itemRequestRepository.findAllByRequesterIdOrderByCreatedDesc(userId));
    }

    @Override
    public Collection<ItemRequestDto> getOthers(Long userId, int from, int size) {
        log.debug("Get other users' item requests for user id={}, from={}, size={}", userId, from, size);
        findUser(userId);
        if (from < 0 || size <= 0) {
            throw new BadRequestException("from must be >= 0 and size must be > 0");
        }
        PageRequest page = PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "created"));
        return toDtos(itemRequestRepository.findAllByRequesterIdNot(userId, page));
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        log.debug("Get item request id={} by user id={}", requestId, userId);
        findUser(userId);
        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Item request with id " + requestId + " not found"));
        List<Item> items = itemRepository.findAllByRequestIdInOrderByIdAsc(List.of(requestId));
        return ItemRequestMapper.toDto(request, items);
    }

    private Collection<ItemRequestDto> toDtos(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();
        Map<Long, List<Item>> itemsByRequestId = itemRepository.findAllByRequestIdInOrderByIdAsc(requestIds)
                .stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId()));
        return requests.stream()
                .map(request -> ItemRequestMapper.toDto(
                        request,
                        itemsByRequestId.getOrDefault(request.getId(), List.of())))
                .toList();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
    }
}
