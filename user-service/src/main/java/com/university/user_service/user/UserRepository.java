package com.university.user_service.user;

import com.university.user_service.user.model.User;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {

    private final Map<UUID, User> database = new ConcurrentHashMap<>();

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }
        database.put(user.getId(), user);
        return user;
    }

    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(database.get(id));
    }

    public Optional<User> findByUsername(String username) {
        return database.values().stream()
                .filter(u -> u.getUsername() != null && u.getUsername().equals(username))
                .findFirst();
    }

    public Optional<User> findByMatrikelnummer(Long matrikelnummer) {
        return database.values().stream()
                .filter(u -> u.getMatrikelnummer() != null && u.getMatrikelnummer().equals(matrikelnummer))
                .findFirst();
    }

    public List<User> findAll() {
        return new ArrayList<>(database.values());
    }

    public void deleteById(UUID id) {
        database.remove(id);
    }

    public boolean existsById(UUID id) {
        return database.containsKey(id);
    }

    public boolean existsByUsername(String username) {
        return database.values().stream()
                .filter(u -> u.getStatus() != com.university.user_service.common.Status.DELETED)
                .anyMatch(u -> u.getUsername() != null && u.getUsername().equals(username));
    }

    public boolean existsByMatrikelnummer(Long matrikelnummer) {
        return database.values().stream()
                .filter(u -> u.getStatus() != com.university.user_service.common.Status.DELETED)
                .anyMatch(u -> u.getMatrikelnummer() != null && u.getMatrikelnummer().equals(matrikelnummer));
    }

    public Long getNextMatrikelnummer() {
        return database.values().stream()
                .filter(u -> u.getStatus() != com.university.user_service.common.Status.DELETED)
                .map(User::getMatrikelnummer)
                .filter(Objects::nonNull)
                .max(Long::compareTo)
                .map(max -> max + 1)
                .orElse(1L);
    }
}
