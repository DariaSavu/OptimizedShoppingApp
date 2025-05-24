package org.example.shoppingapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        user1 = new User(1, "john.doe", "John", "Doe");
        user2 = new User(1, "john.doe", "John", "Doe");
        user3 = new User(2, "jane.doe", "Jane", "Doe");
    }

    @Test
    @DisplayName("Constructor should set all fields correctly")
    void constructor_ShouldSetAllFields() {
        Integer expectedUserId = 100;
        String expectedUsername = "testuser";
        String expectedFirstName = "Test";
        String expectedLastName = "User";

        User testUser = new User(expectedUserId, expectedUsername, expectedFirstName, expectedLastName);

        assertEquals(expectedUserId, testUser.getUserId());
        assertEquals(expectedUsername, testUser.getUsername());
        assertEquals(expectedFirstName, testUser.getFirstName());
        assertEquals(expectedLastName, testUser.getLastName());
    }

    @Test
    @DisplayName("Getters should return correct values")
    void getters_ShouldReturnCorrectValues() {
        assertEquals(1, user1.getUserId());
        assertEquals("john.doe", user1.getUsername());
        assertEquals("John", user1.getFirstName());
        assertEquals("Doe", user1.getLastName());
    }

    @Test
    @DisplayName("Setters should update field values")
    void setters_ShouldUpdateFieldValues() {
        User testUser = new User(99, "initial", "Initial", "User");

        testUser.setUserId(101);
        assertEquals(101, testUser.getUserId());

        testUser.setUsername("updated.user");
        assertEquals("updated.user", testUser.getUsername());

        testUser.setFirstName("Updated");
        assertEquals("Updated", testUser.getFirstName());

        testUser.setLastName("Name");
        assertEquals("Name", testUser.getLastName());
    }

    @Test
    @DisplayName("Equals should return true for identical objects")
    void equals_ShouldReturnTrueForIdenticalObjects() {
        assertTrue(user1.equals(user2));
    }

    @Test
    @DisplayName("Equals should return true for same object instance")
    void equals_ShouldReturnTrueForSameInstance() {
        assertTrue(user1.equals(user1));
    }

    @Test
    @DisplayName("Equals should return false for different objects")
    void equals_ShouldReturnFalseForDifferentObjects() {
        assertFalse(user1.equals(user3));
    }

    @Test
    @DisplayName("Equals should return false for null object")
    void equals_ShouldReturnFalseForNull() {
        assertFalse(user1.equals(null));
    }

    @Test
    @DisplayName("Equals should return false for object of different type")
    void equals_ShouldReturnFalseForDifferentType() {
        Object otherObject = new Object();
        assertFalse(user1.equals(otherObject));
    }

    @Test
    @DisplayName("Equals should be symmetric")
    void equals_ShouldBeSymmetric() {
        assertEquals(user1.equals(user2), user2.equals(user1));
    }

    @Test
    @DisplayName("Equals should handle null fields consistently")
    void equals_ShouldHandleNullFields() {
        User userWithNulls1 = new User(null, null, null, null);
        User userWithNulls2 = new User(null, null, null, null);
        User userWithSomeNulls = new User(1, "test", null, "User");
        User userWithDifferentSomeNulls = new User(1, "test", "First", null);

        assertTrue(userWithNulls1.equals(userWithNulls2));
        assertFalse(userWithNulls1.equals(user1));
        assertFalse(user1.equals(userWithNulls1));
        assertFalse(userWithSomeNulls.equals(userWithDifferentSomeNulls));
    }


    @Test
    @DisplayName("HashCode should be consistent for equal objects")
    void hashCode_ShouldBeConsistentForEqualObjects() {
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    @DisplayName("HashCode should (ideally) be different for unequal objects")
    void hashCode_ShouldBeDifferentForUnequalObjects() {
        if (!user1.equals(user3)) {
             assertNotEquals(user1.hashCode(), user3.hashCode());
        }
    }

    @Test
    @DisplayName("HashCode consistency: multiple calls return same hash")
    void hashCode_ShouldBeConsistentAcrossMultipleCalls() {
        int initialHashCode = user1.hashCode();
        assertEquals(initialHashCode, user1.hashCode());
        assertEquals(initialHashCode, user1.hashCode());
    }
}