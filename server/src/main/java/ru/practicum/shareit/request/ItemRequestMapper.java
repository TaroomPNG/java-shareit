package ru.practicum.shareit.request;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestItemDto;

import java.util.List;

public final class ItemRequestMapper {

    private ItemRequestMapper() {
    }

    public static ItemRequestDto toDto(ItemRequest request, List<Item> items) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(items.stream().map(ItemRequestMapper::toItemDto).toList())
                .build();
    }

    public static RequestItemDto toItemDto(Item item) {
        return RequestItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .ownerId(item.getOwner().getId())
                .build();
    }
}
