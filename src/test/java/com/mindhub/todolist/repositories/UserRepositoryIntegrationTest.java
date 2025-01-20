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

import static com.mindhub.todolist.ErrorMessages.*;
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
    void findByEmailUserExists() {
        Optional<UserEntity> result = userRepository.findByEmail(testUser.getEmail());

        assertTrue(result.isPresent(), "User should be found by email");
        assertEquals(testUser.getEmail(), result.get().getEmail(), EMAIL_SHOULD_MATCH);
    }

    @Test
    @DisplayName("Find user by email - User does not exist")
    void findByEmailUserDoesNotExist() {
        Optional<UserEntity> result = userRepository.findByEmail("nonexistent@example.com");

        assertFalse(result.isPresent(), NOT_EXISTENT_EMAIL);
    }

    @Test
    @DisplayName("Check if email exists - Email exists")
    void existsByEmailEmailExists() {
        boolean exists = userRepository.existsByEmail(testUser.getEmail());

        assertTrue(exists, EMAIL_SHOULD_EXIST);
    }

    @Test
    @DisplayName("Check if email exists - Email does not exist")
    void existsByEmailEmailDoesNotExist() {
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        assertFalse(exists, EMAIL_SHOULD_NOT_EXIST);
    }

    @Test
    @DisplayName("Check if username exists - Username exists")
    void existsByUsernameUsernameExists() {
        boolean exists = userRepository.existsByUsername(testUser.getUsername());

        assertTrue(exists, USERNAME_SHOULD_EXIST);
    }

    @Test
    @DisplayName("Check if username exists - Username does not exist")
    void existsByUsernameUsernameDoesNotExist() {
        boolean exists = userRepository.existsByUsername("nonexistentuser");

        assertFalse(exists, USERNAME_SHOULD_NOT_EXIST);
    }
}
