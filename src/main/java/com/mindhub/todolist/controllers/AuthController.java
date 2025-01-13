package com.mindhub.todolist.controllers;

import com.mindhub.todolist.config.JwtUtils;
import com.mindhub.todolist.dtos.AuthResponseDTO;
import com.mindhub.todolist.dtos.ChangePasswordDTO;
import com.mindhub.todolist.dtos.LoginUser;
import com.mindhub.todolist.dtos.RegisterUserDTO;
import com.mindhub.todolist.enums.RoleEnum;
import com.mindhub.todolist.exceptions.UserNotFoundException;
import com.mindhub.todolist.models.UserEntity;
import com.mindhub.todolist.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    public ResponseEntity<AuthResponseDTO> authenticateUser(@RequestBody LoginUser loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwt = jwtUtils.generateToken(authentication.getName());
            return ResponseEntity.ok(new AuthResponseDTO(jwt, "Logged in successfully"));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO("-", "Invalid email or password"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponseDTO("-", "An error occurred during login"));
        }
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
        if (registerUserDTO.getEmail() == null || registerUserDTO.getPassword() == null || registerUserDTO.getUsername() == null) {
            return ResponseEntity.badRequest().body(new AuthResponseDTO("-", "Every field is required."));
        }

        if (userService.existsByUsername(registerUserDTO.getUsername()) || userService.existsByEmail(registerUserDTO.getEmail())) {
            return ResponseEntity.badRequest().body(new AuthResponseDTO("-", "The email or username is already in use."));
        }

        UserEntity user = new UserEntity();
        user.setEmail(registerUserDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerUserDTO.getPassword()));
        user.setUsername(registerUserDTO.getUsername());
        user.setRole(RoleEnum.USER);

        userService.saveUser(user);

        String token = jwtUtils.generateToken(user.getUsername());

        return ResponseEntity.ok(new AuthResponseDTO(token, "User registered successfully"));
    }

    @Operation(summary = "Change password", description = "Allow the user to change their password")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid current password or new password too short",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "New password must be at least 8 characters long"))),
            @ApiResponse(responseCode = "401", description = "Authorization token is missing or invalid",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "Authorization token is missing or invalid"))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "User not found"))),
            @ApiResponse(responseCode = "400", description = "New password cannot be the same as the current password",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "New password cannot be the same as the current password")))
    })
    @PostMapping("/change-password")
    public ResponseEntity<AuthResponseDTO> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO,
            @RequestHeader("Authorization") String authorizationHeader) throws UserNotFoundException {

        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO("-", "Authorization token is missing or invalid"));
        }

        String token = authorizationHeader.substring(7);
        String username = jwtUtils.extractUsername(token);

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO("-", "Invalid or expired token"));
        }

        UserEntity user = userService.findByEmail(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + username));

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponseDTO("-", "User not found"));
        }

        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponseDTO("-", "Current password is incorrect"));
        }

        if (changePasswordDTO.getNewPassword().length() < 8) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponseDTO("-", "New password must be at least 8 characters long"));
        }

        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponseDTO("-", "New password cannot be the same as the current password"));
        }

        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userService.saveUser(user);

        return ResponseEntity.ok(new AuthResponseDTO("-", "Password updated successfully"));
    }

}
