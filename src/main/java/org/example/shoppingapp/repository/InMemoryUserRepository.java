package org.example.shoppingapp.repository;

import org.example.shoppingapp.model.User;
import org.example.shoppingapp.repository.interfaces.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryUserRepository.class);
    private final Map<Integer, User> users = new ConcurrentHashMap<>();

    @Override
    public User save(User user) {
        if (user == null || user.getUserId() == null) {
            logger.warn("Attempted to save a null user or user with null ID.");
            throw new IllegalArgumentException("User or User ID cannot be null.");
        }
        users.put(user.getUserId(), user);
        logger.trace("User saved/updated: {}", user.getUserId());
        return user;
    }

    @Override
    public List<User> saveAll(Iterable<User> users) {
        List<User> savedUsers = new ArrayList<>();
        if (users != null) {
            for (User user : users) {
                if (user != null) {
                    savedUsers.add(save(user));
                }
            }
        }
        return savedUsers;
    }

    @Override
    public Optional<User> findById(Integer userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public boolean existsById(Integer userId) {
        return users.containsKey(userId);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public long count() {
        return users.size();
    }

    @Override
    public void deleteById(Integer userId) {
        users.remove(userId);
        logger.trace("User deleted by ID: {}", userId);
    }

    @Override
    public void delete(User entity) {
        if (entity != null && entity.getUserId() != null) {
            deleteById(entity.getUserId());
        }
    }

    @Override
    public void deleteAll() {
        users.clear();
        logger.info("All users cleared from repository.");
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if(username == null)
            return Optional.empty();
        return users.values().stream()
                .filter(user -> username.equals(user.getUsername()))
                .findFirst();
    }
}