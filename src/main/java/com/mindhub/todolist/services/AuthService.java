package com.mindhub.todolist.services;

import com.mindhub.todolist.dtos.AuthResponseDTO;
import com.mindhub.todolist.dtos.LoginUser;
import com.mindhub.todolist.dtos.RegisterUserDTO;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<AuthResponseDTO> authenticateUser(LoginUser loginRequest);

    AuthResponseDTO registerUser(RegisterUserDTO registerUserDTO);

}
