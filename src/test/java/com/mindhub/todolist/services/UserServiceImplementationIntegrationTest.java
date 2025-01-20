package com.mindhub.todolist.services;

import com.mindhub.todolist.dtos.AuthResponseDTO;
import com.mindhub.todolist.dtos.ChangePasswordDTO;
import com.mindhub.todolist.dtos.UserDTO;
import com.mindhub.todolist.enums.RoleEnum;
import com.mindhub.todolist.exceptions.IllegalAttributeException;
import com.mindhub.todolist.exceptions.UserNotFoundException;
import com.mindhub.todolist.models.UserEntity;
import com.mindhub.todolist.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceImplementationIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        user = new UserEntity("pablooromero", "pablo@test.com", passwordEncoder.encode("currentPassword"), RoleEnum.USER);
        userRepository.save(user);
    }


    @Test
    @Transactional
    public void testGetUserByIdIntegration() throws UserNotFoundException {
        UserDTO result = userService.getUserById(user.getId());

        assertNotNull(result);
        assertEquals(user.getUsername(), result.getUsername());
        assertEquals(user.getEmail(), result.getEmail());
    }

    @Test
    public void testGetUserByIdNotFoundIntegration() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    @Transactional
    public void testUpdateUserIntegration() throws UserNotFoundException, IllegalAttributeException {
        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setUsername("updatedUser");
        updatedUserDTO.setEmail("updateduser@test.com");

        UserDTO updatedUser = userService.updateUser(user.getId(), updatedUserDTO);

        UserEntity updatedUserEntity = userRepository.findById(user.getId()).orElseThrow();
        assertNotNull(updatedUser);
        assertEquals("updatedUser", updatedUserEntity.getUsername());
        assertEquals("updateduser@test.com", updatedUserEntity.getEmail());
    }

    @Test
    public void testDeleteUserIntegration() throws UserNotFoundException {
        userService.deleteUser(user.getId());

        assertFalse(userRepository.existsById(user.getId()));
    }

    @Test
    public void testDeleteUserNotFoundIntegration() {
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(999L));
    }

    @Test
    public void testChangePasswordIntegration() throws Exception {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("currentPassword");
        changePasswordDTO.setNewPassword("newPassword123");

        UserEntity userEntity = userRepository.findById(this.user.getId()).orElseThrow();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                new User(userEntity.getEmail(), "currentPassword", new ArrayList<>()),
                "currentPassword",
                new ArrayList<>()
        );

        SecurityContext securityContextMock = mock(SecurityContext.class);
        when(securityContextMock.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContextMock);

        AuthResponseDTO response = userService.changePassword(changePasswordDTO, authentication);

        assertEquals("Password updated successfully", response.getMessage());

        SecurityContextHolder.clearContext();
    }
}
