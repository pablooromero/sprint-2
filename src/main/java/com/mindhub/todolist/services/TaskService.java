package com.mindhub.todolist.services;

import com.mindhub.todolist.dtos.TaskDTO;
import com.mindhub.todolist.exceptions.IllegalAttributeException;
import com.mindhub.todolist.exceptions.TaskNotFoundException;
import com.mindhub.todolist.exceptions.UserNotFoundException;
import com.mindhub.todolist.models.Task;

import java.util.List;

public interface TaskService {
    List<Task> getAllTasks();
    TaskDTO getTaskById(Long id) throws TaskNotFoundException;

    Task saveTask(Task task);

    TaskDTO createTask(TaskDTO task) throws UserNotFoundException, IllegalAttributeException;
    TaskDTO updateTask(Long id, TaskDTO taskDTO) throws UserNotFoundException, TaskNotFoundException, IllegalAttributeException;
    TaskDTO completeTask(Long id) throws TaskNotFoundException;

    void validateTask(TaskDTO taskDTO) throws UserNotFoundException, IllegalAttributeException;

    void deleteTask(Long id) throws TaskNotFoundException;
}

