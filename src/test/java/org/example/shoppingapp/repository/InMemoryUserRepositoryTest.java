package org.example.shoppingapp.repository;

import org.example.shoppingapp.model.User;
import org.example.shoppingapp.repository.interfaces.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserRepositoryTest {

    private UserRepository userRepository;
    private User u1, u2, u3;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        u1 = new User(1, "daria.s", "Daria", "Savu");
        u2 = new User(2, "john.d", "John", "Doe");
        u3 = new User(1, "daria.s.updated", "Daria Updated", "Savu Updated"); // Same ID as u1
    }

    @Test
    @DisplayName("Save new user should add it")
    void save_NewUser_AddsUser() {
        userRepository.save(u1);
        assertEquals(1, userRepository.count());
        assertTrue(userRepository.findById(1).isPresent());
    }

    @Test
    @DisplayName("Save existing user should update it")
    void save_ExistingUser_UpdatesUser() {
        userRepository.save(u1);
        userRepository.save(u3); // u3 has same ID as u1
        assertEquals(1, userRepository.count());
        Optional<User> found = userRepository.findById(1);
        assertTrue(found.isPresent());
        assertEquals("daria.s.updated", found.get().getUsername());
        assertEquals("Daria Updated", found.get().getFirstName());
    }

    @Test
    @DisplayName("SaveAll should add multiple users")
    void saveAll_AddsMultipleUsers() {
        userRepository.saveAll(Arrays.asList(u1, u2));
        assertEquals(2, userRepository.count());
        assertTrue(userRepository.existsById(1));
        assertTrue(userRepository.existsById(2));
    }

    @Test
    @DisplayName("FindById should retrieve correct user")
    void findById_RetrievesCorrectUser() {
        userRepository.save(u1);
        userRepository.save(u2);
        Optional<User> found = userRepository.findById(2);
        assertTrue(found.isPresent());
        assertEquals(u2, found.get());
        assertFalse(userRepository.findById(99).isPresent());
    }

    @Test
    @DisplayName("FindAll should return all users")
    void findAll_ReturnsAllUsers() {
        userRepository.save(u1);
        userRepository.save(u2);
        List<User> allUsers = userRepository.findAll();
        assertEquals(2, allUsers.size());
        assertTrue(allUsers.contains(u1));
        assertTrue(allUsers.contains(u2)); // Note: u2 might be replaced if u3 (same ID) was saved after.
                                          // Testul anterior 'save_ExistingUser_UpdatesUser' acoperă update-ul.
    }

    @Test
    @DisplayName("DeleteById should remove user")
    void deleteById_RemovesUser() {
        userRepository.save(u1);
        userRepository.deleteById(1);
        assertFalse(userRepository.existsById(1));
        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("DeleteAll should remove all users")
    void deleteAll_RemovesAllUsers() {
        userRepository.save(u1);
        userRepository.save(u2);
        userRepository.deleteAll();
        assertEquals(0, userRepository.count());
    }

    @Test
    @DisplayName("FindByUsername should retrieve correct user (case-sensitive)")
    void findByUsername_RetrievesCorrectUser() {
        userRepository.save(u1);
        userRepository.save(u2);

        Optional<User> foundDaria = userRepository.findByUsername("daria.s");
        assertTrue(foundDaria.isPresent());
        assertEquals(u1, foundDaria.get());

        Optional<User> foundJohn = userRepository.findByUsername("john.d");
        assertTrue(foundJohn.isPresent());
        assertEquals(u2, foundJohn.get());

        Optional<User> notFound = userRepository.findByUsername("non.existent");
        assertFalse(notFound.isPresent());
        
        Optional<User> caseSensitiveNotFound = userRepository.findByUsername("Daria.s"); // Assuming case-sensitive
        assertFalse(caseSensitiveNotFound.isPresent(), "FindByUsername should be case-sensitive by default unless specified otherwise");
    }
     @Test
    @DisplayName("FindByUsername should return empty for null username")
    void findByUsername_NullUsername_ReturnsEmpty() {
        userRepository.save(u1);
        Optional<User> found = userRepository.findByUsername(null);
        assertFalse(found.isPresent());
    }

}