package com.mindhub.todolist.repositories;

import com.mindhub.todolist.config.JwtUtils;
import com.mindhub.todolist.models.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@Import(JwtUtils.class)
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
public class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private UserEntity testUser;

    @BeforeEach
    @Transactional
    void setUp() {
        userRepository.deleteAll();

        testUser = new UserEntity();
        testUser.setUsername("testuser" + System.currentTimeMillis());
        testUser.setEmail("testuser" + System.currentTimeMillis() + "@example.com");
        testUser.setPassword("password");
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("Find user by email - User exists")
    void findByEmail_UserExists() {
        Optional<UserEntity> result = userRepository.findByEmail(testUser.getEmail());

        assertTrue(result.isPresent(), "User should be found by email");
        assertEquals(testUser.getEmail(), result.get().getEmail(), "The email should match");
    }

    @Test
    @DisplayName("Find user by email - User does not exist")
    void findByEmail_UserDoesNotExist() {
        Optional<UserEntity> result = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(result.isPresent(), "User should not be found by non-existent email");
    }

    @Test
    @DisplayName("Check if email exists - Email exists")
    void existsByEmail_EmailExists() {
        boolean exists = userRepository.existsByEmail(testUser.getEmail());

        assertTrue(exists, "Email should exist in the database");
    }

    @Test
    @DisplayName("Check if email exists - Email does not exist")
    void existsByEmail_EmailDoesNotExist() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertFalse(exists, "Email should not exist in the database");
    }

    @Test
    @DisplayName("Check if username exists - Username exists")
    void existsByUsername_UsernameExists() {
        boolean exists = userRepository.existsByUsername(testUser.getUsername());

        assertTrue(exists, "Username should exist in the database");
    }

    @Test
    @DisplayName("Check if username exists - Username does not exist")
    void existsByUsername_UsernameDoesNotExist() {
        boolean exists = userRepository.existsByUsername("nonexistentuser");

        assertFalse(exists, "Username should not exist in the database");
    }
}
