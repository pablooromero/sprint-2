package com.mindhub.todolist.services;

import com.mindhub.todolist.dtos.AuthResponseDTO;
import com.mindhub.todolist.dtos.ChangePasswordDTO;
import com.mindhub.todolist.dtos.UserDTO;
import com.mindhub.todolist.exceptions.IllegalAttributeException;
import com.mindhub.todolist.exceptions.UserNotFoundException;
import com.mindhub.todolist.models.UserEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<UserEntity> getAllUsers();
    UserDTO getUserById(Long id) throws UserNotFoundException;

    UserDTO getUserProfile(Long id, Authentication authentication) throws UserNotFoundException;

    UserEntity saveUser(UserEntity userEntity);

    UserDTO createUser(UserDTO userDTO) throws UserNotFoundException, IllegalAttributeException;

    UserDTO createAdmin(UserDTO userDTO) throws IllegalAttributeException;

    UserDTO updateUser(Long id, UserDTO userDTO) throws UserNotFoundException, IllegalAttributeException;

    void deleteUserByUser(Long id, Authentication authentication) throws UserNotFoundException;

    AuthResponseDTO changePassword(ChangePasswordDTO changePasswordDTO, Authentication authentication) throws UserNotFoundException;

    void validateUser(UserDTO userDTO) throws UserNotFoundException, IllegalAttributeException;

    UserDTO updateUserByUser(Long id, UserDTO userDTO, Authentication authentication) throws UserNotFoundException, IllegalAttributeException;

    void deleteUser(Long id) throws UserNotFoundException;
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<UserEntity> findByEmail(String email);
}
