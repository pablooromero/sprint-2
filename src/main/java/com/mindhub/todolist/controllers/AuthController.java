package com.mindhub.todolist.controllers;

import com.mindhub.todolist.dtos.AuthResponseDTO;
import com.mindhub.todolist.dtos.ChangePasswordDTO;
import com.mindhub.todolist.dtos.LoginUser;
import com.mindhub.todolist.dtos.RegisterUserDTO;
import com.mindhub.todolist.exceptions.UserNotFoundException;
import com.mindhub.todolist.services.AuthService;
import com.mindhub.todolist.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Operation(summary = "Login user", description = "Authenticate a user and generate a JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "Invalid email or password"))),
            @ApiResponse(responseCode = "500", description = "Internal server error during login",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "An error occurred during login")))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginUser loginRequest) {
        return authService.authenticateUser(loginRequest);
    }


    @Operation(summary = "Register user", description = "Create a new user and generate a JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Missing or invalid fields",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "Every field is required."))),
            @ApiResponse(responseCode = "400", description = "Email or username already in use",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "The email or username is already in use.")))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> registerUser(@RequestBody RegisterUserDTO registerUserDTO) {
        AuthResponseDTO response = authService.registerUser(registerUserDTO);
        if (response.getMessage().equals("Every field is required.") || response.getMessage().equals("The email or username is already in use.")) {
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Change user password", description = "Change the password of the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation errors",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "New password must be at least 8 characters long")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "User not found with ID: 1")
                    )
            )
    })
    @PutMapping("/change-password")
    public ResponseEntity<AuthResponseDTO> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO, Authentication authentication) throws UserNotFoundException {
        AuthResponseDTO response = userService.changePassword(changePasswordDTO, authentication);
        if (response.getMessage().equals("Password updated successfully")) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

}

