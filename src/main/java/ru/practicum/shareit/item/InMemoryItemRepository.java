package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryItemRepository implements ItemRepository {

    private final Map<Long, Item> items = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public Item create(Item item) {
        item.setId(nextId++);
        items.put(item.getId(), item.toBuilder().build());
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item.toBuilder().build());
        return item;
    }

    @Override
    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id)).map(item -> item.toBuilder().build());
    }

    @Override
    public Collection<Item> findAllByOwnerId(Long ownerId) {
        return items.values().stream()
                .filter(item -> item.getOwner() != null && ownerId.equals(item.getOwner().getId()))
                .map(item -> item.toBuilder().build())
                .toList();
    }

    @Override
    public Collection<Item> searchAvailable(String text) {
        String query = text.toLowerCase();
        return items.values().stream()
                .filter(item -> Boolean.TRUE.equals(item.getAvailable()))
                .filter(item -> contains(item.getName(), query) || contains(item.getDescription(), query))
                .map(item -> item.toBuilder().build())
                .toList();
    }

    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query);
    }
}
