package com.mindhub.todolist.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindhub.todolist.config.JwtUtils;
import com.mindhub.todolist.dtos.AuthResponseDTO;
import com.mindhub.todolist.dtos.LoginUser;
import com.mindhub.todolist.dtos.RegisterUserDTO;
import com.mindhub.todolist.enums.RoleEnum;
import com.mindhub.todolist.models.UserEntity;
import com.mindhub.todolist.services.AuthService;
import com.mindhub.todolist.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@ActiveProfiles("test")
@Import(JwtUtils.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserEntity user;
    private RegisterUserDTO registerUser;

    @BeforeEach
    void setUp() {
        user = new UserEntity("pabloromero", "pabloromerook@gmail.com", "test123", RoleEnum.USER);
        user.setId(1L);

        registerUser = new RegisterUserDTO();
        registerUser.setUsername("newuser");
        registerUser.setEmail("newuser@example.com");
        registerUser.setPassword("newpassword");

        MockitoAnnotations.openMocks(this);
    }


    @Test
    void login_success() throws Exception {
        AuthResponseDTO mockResponse = new AuthResponseDTO("mockToken", "Logged in successfully");

        when(authService.authenticateUser(any(LoginUser.class))).thenReturn(ResponseEntity.ok(mockResponse));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginUser("user@example.com", "password123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockToken"))
                .andExpect(jsonPath("$.message").value("Logged in successfully"));

        verify(authService, times(1)).authenticateUser(any(LoginUser.class));
    }


    @Test
    void login_invalidCredentials() throws Exception {
        when(authService.authenticateUser(any(LoginUser.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponseDTO("-", "Invalid email or password")));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginUser("wronguser@example.com", "wrongpassword"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.token").value("-"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));

        verify(authService, times(1)).authenticateUser(any(LoginUser.class));
    }

    @Test
    void login_internalServerError() throws Exception {
        when(authService.authenticateUser(any(LoginUser.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(new AuthResponseDTO("-", "An error occurred during login")));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginUser("user@example.com", "password123"))))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.token").value("-"))
                .andExpect(jsonPath("$.message").value("An error occurred during login"));

        verify(authService, times(1)).authenticateUser(any(LoginUser.class));
    }


    @Test
    void register_success() throws Exception {
        AuthResponseDTO mockResponse = new AuthResponseDTO("mockToken", "Token received");
        mockResponse.setToken("mockToken");
        when(authService.registerUser(any(RegisterUserDTO.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUser)))
                .andExpect(status().isOk());

        verify(authService, times(1)).registerUser(any(RegisterUserDTO.class));
    }


    @Test
    void register_missingFields() throws Exception {
        RegisterUserDTO incompleteUser = new RegisterUserDTO();
        incompleteUser.setUsername("newuser");

        when(authService.registerUser(any(RegisterUserDTO.class)))
                .thenReturn(new AuthResponseDTO("-", "Every field is required."));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(incompleteUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").value("-"))
                .andExpect(jsonPath("$.message").value("Every field is required."));

        verify(authService, times(1)).registerUser(any(RegisterUserDTO.class));
    }

    @Test
    void register_emailOrUsernameAlreadyInUse() throws Exception {
        when(authService.registerUser(any(RegisterUserDTO.class)))
                .thenReturn(new AuthResponseDTO("-", "The email or username is already in use."));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.token").value("-"))
                .andExpect(jsonPath("$.message").value("The email or username is already in use."));

        verify(authService, times(1)).registerUser(any(RegisterUserDTO.class));
    }


}
