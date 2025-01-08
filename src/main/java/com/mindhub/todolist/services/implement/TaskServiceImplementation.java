package com.mindhub.todolist.services.implement;

import com.mindhub.todolist.dtos.TaskDTO;
import com.mindhub.todolist.enums.TaskStatusEnum;
import com.mindhub.todolist.exceptions.IllegalAttributeException;
import com.mindhub.todolist.exceptions.TaskNotFoundException;
import com.mindhub.todolist.exceptions.UserNotFoundException;
import com.mindhub.todolist.models.Task;
    import com.mindhub.todolist.models.UserEntity;
import com.mindhub.todolist.repositories.TaskRepository;
import com.mindhub.todolist.repositories.UserRepository;
import com.mindhub.todolist.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImplementation implements TaskService {
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }


    @Override
    public TaskDTO getTaskById(Long id) throws TaskNotFoundException {
        return new TaskDTO(taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id)));
    }


    @Override
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }


    @Override
    public TaskDTO createTask(TaskDTO taskDTO) throws UserNotFoundException, IllegalAttributeException {
        validateTask(taskDTO);

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : TaskStatusEnum.PENDING);

        Long userId = taskDTO.getUserId();
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        task.setUser(userEntity);

        Task savedTask = saveTask(task);
        return new TaskDTO(savedTask);
    }


    @Override
    public TaskDTO updateTask(Long id, TaskDTO taskDTO) throws UserNotFoundException, TaskNotFoundException, IllegalAttributeException {
        validateTask(taskDTO);

        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));

        existingTask.setTitle(taskDTO.getTitle());
        existingTask.setDescription(taskDTO.getDescription());
        existingTask.setStatus(taskDTO.getStatus());

        Long userId = taskDTO.getUserId();
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with id: " + userId));

        existingTask.setUser(userEntity);

        Task updatedTask = saveTask(existingTask);
        return new TaskDTO(updatedTask);
    }

    @Override
    public void deleteTask(Long id) throws TaskNotFoundException {
        if(!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Task not found with ID: " + id);
        }

        taskRepository.deleteById(id);
    }

    @Override
    public TaskDTO completeTask(Long id) throws TaskNotFoundException {
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Task not found with ID: " + id));

        existingTask.setStatus(TaskStatusEnum.COMPLETED);
        Task updatedTask = saveTask(existingTask);
        return new TaskDTO(updatedTask);
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

