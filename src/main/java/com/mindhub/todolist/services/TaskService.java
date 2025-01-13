package com.mindhub.todolist.services;

import com.mindhub.todolist.dtos.TaskDTO;
import com.mindhub.todolist.exceptions.IllegalAttributeException;
import com.mindhub.todolist.exceptions.TaskNotFoundException;
import com.mindhub.todolist.exceptions.UserNotFoundException;
import com.mindhub.todolist.models.Task;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface TaskService {
    List<Task> getAllTasks();
    List<Task> getAllTasksByUserId(Authentication authentication) throws UserNotFoundException;
    TaskDTO getTaskById(Long id) throws TaskNotFoundException, UserNotFoundException;

    TaskDTO getTaskByUserId(Long id, Authentication authentication) throws UserNotFoundException, TaskNotFoundException;

    Task saveTask(Task task);

    Task createTask(TaskDTO task) throws UserNotFoundException, IllegalAttributeException;

    TaskDTO createTaskAdmin(TaskDTO taskDTO) throws UserNotFoundException, IllegalAttributeException;

    TaskDTO createTaskUser(TaskDTO taskDTO, Authentication authentication) throws UserNotFoundException, IllegalAttributeException;

    TaskDTO updateTask(Long id, TaskDTO taskDTO) throws UserNotFoundException, TaskNotFoundException, IllegalAttributeException;

    void deleteTaskByUser(Long id, Authentication authentication) throws UserNotFoundException, TaskNotFoundException;

    void completeTask(Long id) throws TaskNotFoundException, UserNotFoundException;

    void completeTaskUser(Long id, Authentication authentication) throws TaskNotFoundException, UserNotFoundException;

    void validateTask(TaskDTO taskDTO) throws UserNotFoundException, IllegalAttributeException;

    TaskDTO updateTaskUser(Long id, TaskDTO taskDTO, Authentication authentication) throws UserNotFoundException, IllegalAttributeException, TaskNotFoundException;

    void deleteTask(Long id) throws TaskNotFoundException;

}

