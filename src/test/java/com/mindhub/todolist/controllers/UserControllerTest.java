package com.mindhub.todolist.controllers;

import com.mindhub.todolist.config.JwtUtils;
import com.mindhub.todolist.dtos.UserDTO;
import com.mindhub.todolist.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@ActiveProfiles("test")
@Import(JwtUtils.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Mock
    private Authentication authentication;

    private UserDTO mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new UserDTO();
        mockUser.setUsername("testuser");
        mockUser.setEmail("testuser@example.com");
        mockUser.setId(1L);
    }

    @Test
    void getUserProfile_success() throws Exception {

        when(userService.getUserProfile(eq(1L), any(Authentication.class))).thenReturn(mockUser);

        mockMvc.perform(get("/api/user/users/1")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"));

        verify(userService, times(1)).getUserProfile(eq(1L), any(Authentication.class));
    }

    @Test
    void updateUserByUser_success() throws Exception {
        mockUser.setUsername("updateduser");
        mockUser.setEmail("updateduser@example.com");

        when(userService.updateUserByUser(eq(1L), any(UserDTO.class), any(Authentication.class))).thenReturn(mockUser);

        mockMvc.perform(put("/api/user/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"updateduser\", \"email\": \"updateduser@example.com\"}")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("updateduser"))
                .andExpect(jsonPath("$.email").value("updateduser@example.com"));

        verify(userService, times(1)).updateUserByUser(eq(1L), any(UserDTO.class), any(Authentication.class));
    }

    @Test
    void deleteUserByUser_success() throws Exception {
        doNothing().when(userService).deleteUserByUser(eq(1L), any(Authentication.class));

        mockMvc.perform(delete("/api/user/users/1")
                        .principal(authentication))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUserByUser(eq(1L), any(Authentication.class));
    }
}
