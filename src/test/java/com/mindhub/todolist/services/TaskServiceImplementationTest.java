package com.mindhub.todolist.services;

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
import com.mindhub.todolist.services.implement.TaskServiceImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TaskServiceImplementationTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private TaskServiceImplementation taskService;

    @Mock
    private Authentication authentication;

    @Mock
    private UserEntity userEntity;

    private Task task;
    private TaskDTO taskDTO;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        task = new Task();
        task.setId(1L);
        task.setTitle("Task 1");
        task.setDescription("Task description");
        task.setStatus(TaskStatusEnum.PENDING);

        taskDTO = new TaskDTO(task);
    }

    @Test
    public void testGetAllTasks() {
        when(taskRepository.findAll()).thenReturn(List.of((task)));

        assertEquals(1, taskService.getAllTasks().size());
    }

    @Test
    public void testGetAllTasksByUserId() throws UserNotFoundException {
        when(securityUtils.getAuthenticatedUser(authentication)).thenReturn(userEntity);

        when(userEntity.getTasks()).thenReturn(Set.of(task));

        assertEquals(1, taskService.getAllTasksByUserId(authentication).size());
    }


    @Test
    public void testGetTaskById() throws TaskNotFoundException {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        TaskDTO result = taskService.getTaskById(1L);
        assertNotNull(result);
        assertEquals("Task 1", result.getTitle());
    }

    @Test
    public void testGetTaskByIdNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(1L));
    }

    @Test
    public void testCreateTask() throws UserNotFoundException, IllegalAttributeException {
        when(userRepository.existsById(taskDTO.getUserId())).thenReturn(true);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task result = taskService.createTask(taskDTO);
        assertNotNull(result);
        assertEquals("Task 1", result.getTitle());
    }

    @Test
    public void testCreateTaskWithInvalidTitle() {
        taskDTO.setTitle(null);

        assertThrows(IllegalAttributeException.class, () -> taskService.createTask(taskDTO));
    }

    @Test
    public void testCreateTaskWithInvalidDescription() {
        taskDTO.setDescription("123");

        assertThrows(IllegalAttributeException.class, () -> taskService.createTask(taskDTO));
    }

    @Test
    public void testUpdateTask() throws TaskNotFoundException, IllegalAttributeException, UserNotFoundException {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        taskDTO.setTitle("Updated Task");
        TaskDTO result = taskService.updateTask(1L, taskDTO);

        assertNotNull(result);
        assertEquals("Updated Task", result.getTitle());
    }

    @Test
    public void testUpdateTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(1L, taskDTO));
    }

    @Test
    public void testUpdateTaskUserNotAllowed() throws UserNotFoundException {
        UserEntity otherUser = new UserEntity();
        otherUser.setId(3L);

        task.setUser(otherUser);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(securityUtils.getAuthenticatedUser(authentication)).thenReturn(userEntity);
        when(userEntity.getId()).thenReturn(2L);

        assertThrows(AccessDeniedException.class, () -> taskService.updateTaskUser(1L, taskDTO, authentication));
    }


    @Test
    public void testDeleteTask() throws TaskNotFoundException {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        doNothing().when(taskRepository).delete(task);

        taskService.deleteTask(1L);
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    public void testDeleteTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(1L));
    }

    @Test
    public void testCompleteTask() throws TaskNotFoundException {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        taskService.completeTask(1L);
        assertEquals(TaskStatusEnum.COMPLETED, task.getStatus());
    }

    @Test
    public void testCompleteTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.completeTask(1L));
    }

    @Test
    public void testValidateTaskValid() throws UserNotFoundException, IllegalAttributeException {
        taskDTO.setTitle("Valid Task");
        taskDTO.setDescription("Valid description");
        taskDTO.setStatus(TaskStatusEnum.PENDING);

        taskService.validateTask(taskDTO);
    }

    @Test
    public void testValidateTaskInvalidTitle() {
        taskDTO.setTitle("");

        assertThrows(IllegalAttributeException.class, () -> taskService.validateTask(taskDTO));
    }

    @Test
    public void testValidateTaskInvalidDescription() {
        taskDTO.setDescription("123");

        assertThrows(IllegalAttributeException.class, () -> taskService.validateTask(taskDTO));
    }

    @Test
    public void testValidateTaskUserNotFound() {
        taskDTO.setUserId(99L);
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> taskService.validateTask(taskDTO));
    }
}
