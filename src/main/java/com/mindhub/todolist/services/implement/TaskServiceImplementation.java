package com.mindhub.todolist.services.implement;

import com.mindhub.todolist.config.SecurityUtils;
import com.mindhub.todolist.dtos.TaskDTO;
import com.mindhub.todolist.enums.TaskStatusEnum;
import com.mindhub.todolist.exceptions.AccessDeniedException;
import com.mindhub.todolist.exceptions.IllegalAttributeException;
import com.mindhub.todolist.exceptions.TaskNotFoundException;
import com.mindhub.todolist.exceptions.UserNotFoundException;
import com.mindhub.todolist.models.Task;
    import com.mindhub.todolist.models.UserEntity;
import com.mindhub.todolist.repositories.TaskRepository;
import com.mindhub.todolist.repositories.UserRepository;
import com.mindhub.todolist.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskServiceImplementation implements TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SecurityUtils securityUtils;

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Override
    public List<Task> getAllTasksByUserId(Authentication authentication) throws UserNotFoundException {
        UserEntity userEntity = securityUtils.getAuthenticatedUser(authentication);

        return new ArrayList<>(userEntity.getTasks());
    }


    @Override
    public TaskDTO getTaskById(Long id) throws TaskNotFoundException {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));

        return new TaskDTO(task);
    }

    @Override
    public TaskDTO getTaskByUserId(Long id, Authentication authentication) throws UserNotFoundException, TaskNotFoundException {
        UserEntity userEntity = securityUtils.getAuthenticatedUser(authentication);
        Task task = taskRepository.findByIdAndUserEntityId(id, userEntity.getId())
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));

        return new TaskDTO(task);
    }


    @Override
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }


    @Override
    public Task createTask(TaskDTO taskDTO) throws UserNotFoundException, IllegalAttributeException {
        validateTask(taskDTO);

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatusEnum.PENDING);

        return saveTask(task);
    }

    @Override
    public TaskDTO createTaskAdmin(TaskDTO taskDTO) throws UserNotFoundException, IllegalAttributeException {
        Task task = createTask(taskDTO);

        UserEntity user = userRepository.findById(taskDTO.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + taskDTO.getUserId()));

        task.setUser(user);
        saveTask(task);

        return new TaskDTO(task);
    }

    @Override
    public TaskDTO createTaskUser(TaskDTO taskDTO, Authentication authentication) throws UserNotFoundException, IllegalAttributeException {
        Task task = createTask(taskDTO);

        UserEntity user = securityUtils.getAuthenticatedUser(authentication);

        task.setUser(user);
        saveTask(task);

        return new TaskDTO(task);
    }


    @Override
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) throws UserNotFoundException, TaskNotFoundException, IllegalAttributeException {
        validateTask(taskDTO);

        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));


        existingTask.setTitle(taskDTO.getTitle());
        existingTask.setDescription(taskDTO.getDescription());
        existingTask.setStatus(taskDTO.getStatus());

        Task updatedTask = saveTask(existingTask);
        return new TaskDTO(updatedTask);
    }

    @Override
    public TaskDTO updateTaskUser(Long id, TaskDTO taskDTO, Authentication authentication) throws UserNotFoundException, IllegalAttributeException, TaskNotFoundException {
        validateTask(taskDTO);

        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));

        UserEntity userEntity = securityUtils.getAuthenticatedUser(authentication);

        if (!existingTask.getUser().getId().equals(userEntity.getId())) {
            throw new AccessDeniedException("You do not have permission to update this task");
        }

        existingTask.setTitle(taskDTO.getTitle());
        existingTask.setDescription(taskDTO.getDescription());
        existingTask.setStatus(taskDTO.getStatus());

        Task updatedTask = saveTask(existingTask);
        return new TaskDTO(updatedTask);
    }

    @Override
    public void deleteTask(Long id) throws TaskNotFoundException {
        Task existingTask = taskRepository.findById(id)
                        .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));

        taskRepository.delete(existingTask);
    }

    @Override
    public void deleteTaskByUser(Long id, Authentication authentication) throws UserNotFoundException, TaskNotFoundException {
        UserEntity user = securityUtils.getAuthenticatedUser(authentication);

        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));

        if (!user.getId().equals(existingTask.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to delete this task");
        }

        taskRepository.delete(existingTask);
    }

    @Override
    public void completeTask(Long id) throws TaskNotFoundException {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));

        existingTask.setStatus(TaskStatusEnum.COMPLETED);
        Task updatedTask = saveTask(existingTask);
        new TaskDTO(updatedTask);
    }

    @Override
    public void completeTaskUser(Long id, Authentication authentication) throws TaskNotFoundException, UserNotFoundException {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));

        UserEntity user = securityUtils.getAuthenticatedUser(authentication);

        if (!user.getId().equals(existingTask.getUser().getId())) {
           throw  new AccessDeniedException("You do not have permission to complete this task");
        }

        existingTask.setStatus(TaskStatusEnum.COMPLETED);
        Task updatedTask = saveTask(existingTask);
        new TaskDTO(updatedTask);
    }

    @Override
    public void validateTask(TaskDTO taskDTO) throws UserNotFoundException, IllegalAttributeException {

        if (taskDTO.getTitle() == null || taskDTO.getTitle().trim().isEmpty()) {
            throw new IllegalAttributeException("Task title cannot be null or empty");
        }

        if (taskDTO.getDescription() == null || taskDTO.getDescription().length() < 5) {
            throw new IllegalAttributeException("Task description must be at least 5 characters long");
        }

        if (taskDTO.getStatus() == null) {
            throw new IllegalAttributeException("Task status cannot be null");
        }

        if (taskDTO.getUserId() != null && !userRepository.existsById(taskDTO.getUserId())) {
            throw new UserNotFoundException("User not found with ID: " + taskDTO.getUserId());
        }
    }

}

