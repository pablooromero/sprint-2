package com.mindhub.todolist.services;

import com.mindhub.todolist.ErrorMessages;
import com.mindhub.todolist.config.JwtUtils;
import com.mindhub.todolist.dtos.AuthResponseDTO;
import com.mindhub.todolist.dtos.LoginUser;
import com.mindhub.todolist.dtos.RegisterUserDTO;
import com.mindhub.todolist.models.UserEntity;
import com.mindhub.todolist.repositories.UserRepository;
import com.mindhub.todolist.services.implement.AuthServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static com.mindhub.todolist.ErrorMessages.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplementationTest {

    @InjectMocks
    private AuthServiceImplementation authService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void authenticateUserSuccess() {
        LoginUser loginUser = new LoginUser("test@example.com", "password");
        String generatedToken = "mockToken";

        Authentication authenticationMock = mock(UsernamePasswordAuthenticationToken.class);
        when(authenticationMock.getName()).thenReturn("test@example.com");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authenticationMock);
        when(jwtUtils.generateToken("test@example.com")).thenReturn(generatedToken);

        ResponseEntity<AuthResponseDTO> response = authService.authenticateUser(loginUser);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(generatedToken, response.getBody().getToken());
        assertEquals(LOGIN_SUCCESSFUL, response.getBody().getMessage());
    }


    @Test
    void authenticateUserBadCredentials() {
        LoginUser loginUser = new LoginUser("test@example.com", "wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        ResponseEntity<AuthResponseDTO> response = authService.authenticateUser(loginUser);

        assertEquals(401, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("-", response.getBody().getToken());
        assertEquals(INVALID_MAIL_PASSWORD, response.getBody().getMessage());
    }

    @Test
    void authenticateUserInternalServerError() {
        LoginUser loginUser = new LoginUser("test@example.com", "password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        ResponseEntity<AuthResponseDTO> response = authService.authenticateUser(loginUser);

        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("-", response.getBody().getToken());
        assertEquals(ERROR_DURING_LOGIN, response.getBody().getMessage());
    }

    @Test
    void registerUserSuccess() {
        RegisterUserDTO registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setEmail("test@example.com");
        registerUserDTO.setUsername("username");
        registerUserDTO.setPassword("password");

        String encodedPassword = "encodedPassword";
        String generatedToken = "mockToken";

        when(userRepository.existsByUsername(registerUserDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(registerUserDTO.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerUserDTO.getPassword())).thenReturn(encodedPassword);
        when(jwtUtils.generateToken(registerUserDTO.getUsername())).thenReturn(generatedToken);

        AuthResponseDTO response = authService.registerUser(registerUserDTO);

        assertEquals(generatedToken, response.getToken());
        assertEquals(REGISTER_SUCCESSFUL, response.getMessage());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    void registerUserMissingFields() {
        RegisterUserDTO registerUserDTO = new RegisterUserDTO();

        AuthResponseDTO response = authService.registerUser(registerUserDTO);

        assertEquals("-", response.getToken());
        assertEquals(EVERY_FIELD_REQUIRED, response.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void registerUserEmailOrUsernameInUse() {
        RegisterUserDTO registerUserDTO = new RegisterUserDTO();
        registerUserDTO.setEmail("test@example.com");
        registerUserDTO.setUsername("username");
        registerUserDTO.setPassword("password");

        when(userRepository.existsByUsername(registerUserDTO.getUsername())).thenReturn(true);

        AuthResponseDTO response = authService.registerUser(registerUserDTO);

        assertEquals("-", response.getToken());
        assertEquals(EMAIL_USERNAME_ALREADY_USED, response.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }
}
