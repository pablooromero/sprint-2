package com.mindhub.todolist.dtos;

import com.mindhub.todolist.models.UserEntity;

import java.util.List;

public class UserDTO {
    private Long id;

    private String username;
    private String email;
    private String password;

    private List<TaskDTO> tasks;

    public UserDTO(UserEntity userEntity) {
        id = userEntity.getId();
        username = userEntity.getUsername();
        email = userEntity.getEmail();
        password = userEntity.getPassword();
        tasks = userEntity.getTasks()
                .stream()
                .map(TaskDTO::new)
                .toList();
    }

    public UserDTO() {}

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public List<TaskDTO> getTasks() {
        return tasks;
    }

    public void setUsername(String newUser) {
        username = newUser;
    }

    public void setEmail(String mail) {
        email = mail;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
