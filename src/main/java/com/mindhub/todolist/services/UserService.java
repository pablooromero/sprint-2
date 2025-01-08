package com.mindhub.todolist.services;

import com.mindhub.todolist.dtos.UserDTO;
import com.mindhub.todolist.exceptions.IllegalAttributeException;
import com.mindhub.todolist.exceptions.UserNotFoundException;
import com.mindhub.todolist.models.UserEntity;

import java.util.List;

public interface UserService {

    List<UserEntity> getAllUsers();
    UserDTO getUserById(Long id) throws UserNotFoundException;

    UserEntity saveUser(UserEntity userEntity);

    UserDTO createUser(UserDTO userDTO) throws UserNotFoundException, IllegalAttributeException;
    UserDTO updateUser(Long id, UserDTO userDTO) throws UserNotFoundException, IllegalAttributeException;

    void validateUser(UserDTO userDTO) throws UserNotFoundException, IllegalAttributeException;

    void deleteUser(Long id) throws UserNotFoundException;
}
