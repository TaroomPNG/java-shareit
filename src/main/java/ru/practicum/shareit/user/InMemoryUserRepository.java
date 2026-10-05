package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();
    private long nextId = 1;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), copy(user));
        emails.add(normalize(user.getEmail()));
        return user;
    }

    @Override
    public User update(User user) {
        User old = users.get(user.getId());
        emails.remove(normalize(old.getEmail()));
        users.put(user.getId(), copy(user));
        emails.add(normalize(user.getEmail()));
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id)).map(this::copy);
    }

    @Override
    public Collection<User> findAll() {
        return users.values().stream()
                .map(this::copy)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        User removed = users.remove(id);
        if (removed != null) {
            emails.remove(normalize(removed.getEmail()));
        }
    }

    @Override
    public boolean existsById(Long id) {
        return users.containsKey(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return emails.contains(normalize(email));
    }

    private String normalize(String email) {
        return email == null ? null : email.toLowerCase();
    }

    private User copy(User user) {
        return new User(user.getId(), user.getName(), user.getEmail());
    }
}
