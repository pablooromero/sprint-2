package com.mindhub.todolist.services;

import com.mindhub.todolist.config.SecurityUtils;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static com.mindhub.todolist.ErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceImplementationTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private PasswordEncoder passwordEncoder;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity("pablooromero", "pablo@test.com", passwordEncoder.encode("currentPassword"), RoleEnum.USER);
        user.setId(1L);
    }

    @Test
    public void testGetUserByIdSuccess() throws UserNotFoundException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(1L);

        assertEquals("pablooromero", result.getUsername());
        assertEquals("pablo@test.com", result.getEmail());
    }

    @Test
    public void testGetUserByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(1L));
    }


    @Test
    public void testUpdateUserSuccess() throws UserNotFoundException, IllegalAttributeException {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenReturn(user);

        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setUsername("pabloupdated");
        updatedUserDTO.setEmail("pabloupdated@test.com");

        UserDTO result = userService.updateUser(1L, updatedUserDTO);

        assertEquals("pabloupdated", result.getUsername());
        assertEquals("pabloupdated@test.com", result.getEmail());
    }

    @Test
    public void testUpdateUserNotFound() {
        UserDTO updatedUserDTO = new UserDTO();
        updatedUserDTO.setUsername("pabloupdated");
        updatedUserDTO.setEmail("pabloupdated@test.com");

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(1L, updatedUserDTO));
    }

    @Test
    public void testDeleteUserSuccess() throws UserNotFoundException {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(1L));
    }

    @Test
    public void testChangePasswordSuccess() throws Exception {

        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("currentPassword");
        changePasswordDTO.setNewPassword("newPassword123");

        user = new UserEntity("pablooromero", "pablo@test.com", passwordEncoder.encode("currentPassword"), RoleEnum.USER);
        user.setId(1L);

        Authentication authentication = mock(Authentication.class);

        when(securityUtils.getAuthenticatedUser(authentication)).thenReturn(user);
        when(passwordEncoder.matches("currentPassword", user.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("newPassword123", user.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("newPassword123")).thenReturn("hashedNewPassword");

        AuthResponseDTO response = userService.changePassword(changePasswordDTO, authentication);

        assertEquals(PASSWORD_UPDATED, response.getMessage());
    }


    @Test
    public void testChangePasswordIncorrectCurrentPassword() throws Exception {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("wrongPassword");
        changePasswordDTO.setNewPassword("newPassword123");

        Authentication authentication = mock(Authentication.class);

        when(securityUtils.getAuthenticatedUser(authentication)).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "hashedCurrentPassword")).thenReturn(false);

        AuthResponseDTO response = userService.changePassword(changePasswordDTO, authentication);

        assertEquals(PASSWORD_INCORRECT, response.getMessage());
    }

    @Test
    public void testChangePasswordTooShortNewPassword() throws Exception {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("currentPassword");
        changePasswordDTO.setNewPassword("short");

        user = new UserEntity("pablooromero", "pablo@test.com", passwordEncoder.encode("currentPassword"), RoleEnum.USER);
        user.setId(1L);

        Authentication authentication = mock(Authentication.class);

        when(securityUtils.getAuthenticatedUser(authentication)).thenReturn(user);
        when(passwordEncoder.matches("currentPassword", user.getPassword())).thenReturn(true);

        AuthResponseDTO response = userService.changePassword(changePasswordDTO, authentication);

        assertEquals(PASSWORD_TOO_SHORT, response.getMessage());
    }


    @Test
    public void testChangePasswordNewPasswordSameAsCurrent() throws Exception {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("currentPassword");
        changePasswordDTO.setNewPassword("samePassword");

        Authentication authentication = mock(Authentication.class);

        when(securityUtils.getAuthenticatedUser(authentication)).thenReturn(user);
        when(passwordEncoder.matches("currentPassword", user.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("samePassword", user.getPassword())).thenReturn(true);

        AuthResponseDTO response = userService.changePassword(changePasswordDTO, authentication);

        assertEquals(PASSWORD_CANNOT_BE_SAME, response.getMessage());
    }

}
