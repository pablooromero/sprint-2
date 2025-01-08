package com.mindhub.todolist.dtos;

import com.mindhub.todolist.models.UserEntity;

import java.util.List;

public class UserDTO {
    private Long id;

    private String username;
    private String email;

    private List<TaskDTO> tasks;

    public UserDTO(UserEntity userEntity) {
        id = userEntity.getId();
        username = userEntity.getUsername();
        email = userEntity.getEmail();
        tasks = userEntity.getTasks()
                .stream()
                .map( task -> new TaskDTO(task) ) //TaskDTO::new versión corta
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
}
