package com.mindhub.todolist.models;

import com.mindhub.todolist.enums.RoleEnum;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private RoleEnum role;

    @OneToMany(mappedBy = "userEntity")
    private Set<Task> tasks = new HashSet<>();

    public UserEntity(String username, String email, String password, RoleEnum role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public UserEntity() {}

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void addTask(Task task) {
        task.setUser(this);
        tasks.add(task);
    }

    public RoleEnum getRole() {
        return role;
    }

    public void setRole(RoleEnum role) {
        this.role = role;
    }

    public void setId(Long userId) {
        this.id = userId;
    }
}
