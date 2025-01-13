package com.mindhub.todolist.services.implement;

import com.mindhub.todolist.config.SecurityUtils;
import com.mindhub.todolist.dtos.UserDTO;
import com.mindhub.todolist.enums.RoleEnum;
import com.mindhub.todolist.exceptions.AccessDeniedException;
import com.mindhub.todolist.exceptions.IllegalAttributeException;
import com.mindhub.todolist.exceptions.UserNotFoundException;
import com.mindhub.todolist.models.UserEntity;
import com.mindhub.todolist.repositories.UserRepository;
import com.mindhub.todolist.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserServiceImplementation implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Override
    public List<UserEntity> getAllUsers(){
        return userRepository.findAll();
    }

    @Override
    public UserDTO getUserById(Long id) throws UserNotFoundException {
        return new UserDTO(userRepository.findById(id)
                .orElseThrow( () -> new UserNotFoundException("User not found with ID: " + id) ));
    }

    @Override
    public UserDTO getUserProfile(Long id, Authentication authentication) throws UserNotFoundException {
        UserEntity user = securityUtils.getAuthenticatedUser(authentication);

        if (!user.getId().equals(id)) {
            throw new AccessDeniedException("Access denied");
        }

        return new UserDTO(userRepository.findById(id)
                .orElseThrow( () -> new UserNotFoundException("User not found with ID: " + id) ));
    }

    @Override
    public UserEntity saveUser(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) throws IllegalAttributeException {
        validateUser(userDTO);

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userDTO.getUsername());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setRole(RoleEnum.USER);

        UserEntity savedUser = saveUser(userEntity);
        return new UserDTO(savedUser);
    }

    @Override
    public UserDTO createAdmin(UserDTO userDTO) throws IllegalAttributeException {
        validateUser(userDTO);

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userDTO.getUsername());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setRole(RoleEnum.ADMIN);

        UserEntity savedUser = saveUser(userEntity);
        return new UserDTO(savedUser);
    }

    @Override
    public UserDTO updateUser(Long id, UserDTO userDTO) throws UserNotFoundException, IllegalAttributeException {
        validateUser(userDTO);

        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow( () -> new UserNotFoundException("User not found with ID: " + id));

        existingUser.setUsername(userDTO.getUsername());
        existingUser.setEmail(userDTO.getEmail());

        UserEntity savedUser = saveUser(existingUser);
        return new UserDTO(savedUser);
    }

    @Override
    public UserDTO updateUserByUser(Long id, UserDTO userDTO, Authentication authentication) throws UserNotFoundException, IllegalAttributeException {
        validateUser(userDTO);

        UserEntity user = securityUtils.getAuthenticatedUser(authentication);

        if (!user.getId().equals(id)) {
            throw new AccessDeniedException("Access denied");
        }

        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());

        UserEntity savedUser = saveUser(user);
        return new UserDTO(savedUser);
    }

    @Override
    public void deleteUser(Long id) throws UserNotFoundException {
        userRepository.findById(id)
                        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        userRepository.deleteById(id);
    }

    @Override
    public void deleteUserByUser(Long id, Authentication authentication) throws UserNotFoundException {
        UserEntity user = securityUtils.getAuthenticatedUser(authentication);

        if (!user.getId().equals(id)) {
            throw new AccessDeniedException("Access denied");
        }

        userRepository.deleteById(id);
    }

    @Override
    public void validateUser(UserDTO userDTO) throws IllegalAttributeException {

        if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
            throw new IllegalAttributeException("Email cannot be null or empty");
        }

        String emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        if (!Pattern.matches(emailPattern, userDTO.getEmail())) {
            throw new IllegalAttributeException("Invalid email format");
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalAttributeException("Email is already in use");
        }

        if (userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
            throw new IllegalAttributeException("Username cannot be null or empty");
        }

        if (userDTO.getUsername().length() < 3) {
            throw new IllegalAttributeException("Username must be at least 3 characters long");
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
