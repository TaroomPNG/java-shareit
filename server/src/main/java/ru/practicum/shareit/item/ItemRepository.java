package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findAllByOwnerIdOrderByIdAsc(Long ownerId);

    List<Item> findAllByRequestIdInOrderByIdAsc(Collection<Long> requestIds);

    @Query("""
            select i from Item i
            where i.available = true
              and (upper(i.name) like upper(concat('%', ?1, '%'))
                or upper(i.description) like upper(concat('%', ?1, '%')))
            order by i.id
            """)
    List<Item> searchAvailable(String text);
}
