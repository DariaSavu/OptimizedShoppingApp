package org.example.shoppingapp.repository.interfaces;

import org.example.shoppingapp.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User save(User user);

    List<User> saveAll(Iterable<User> users);

    Optional<User> findById(Integer userId);

    boolean existsById(Integer userId);

    List<User> findAll();

    long count();
    void deleteById(Integer userId);

    void delete(User user);
    void deleteAll();
    Optional<User> findByUsername(String username);
}