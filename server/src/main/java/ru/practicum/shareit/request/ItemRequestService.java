package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.Collection;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, ItemRequestDto requestDto);

    Collection<ItemRequestDto> getOwn(Long userId);

    Collection<ItemRequestDto> getOthers(Long userId, int from, int size);

    ItemRequestDto getById(Long userId, Long requestId);
}
