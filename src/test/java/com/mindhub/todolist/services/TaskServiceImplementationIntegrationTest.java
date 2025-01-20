package com.mindhub.todolist.services;

import com.mindhub.todolist.dtos.TaskDTO;
import com.mindhub.todolist.enums.RoleEnum;
import com.mindhub.todolist.enums.TaskStatusEnum;
import com.mindhub.todolist.exceptions.AccessDeniedException;
import com.mindhub.todolist.exceptions.IllegalAttributeException;
import com.mindhub.todolist.exceptions.TaskNotFoundException;
import com.mindhub.todolist.exceptions.UserNotFoundException;
import com.mindhub.todolist.models.Task;
import com.mindhub.todolist.models.UserEntity;
import com.mindhub.todolist.repositories.TaskRepository;
import com.mindhub.todolist.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class TaskServiceImplementationIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    private UserEntity user;
    private Task task;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        taskRepository.deleteAll();

        user = new UserEntity("pablooromero", "pablo@test.com", "password", RoleEnum.USER);
        userRepository.save(user);

        task = new Task("Test Task", "Test Description", TaskStatusEnum.PENDING);
        task.setUser(user);
        taskRepository.save(task);
    }

    @Test
    @Transactional
    public void testGetTaskByIdIntegration() throws TaskNotFoundException, UserNotFoundException {
        TaskDTO result = taskService.getTaskById(task.getId());

        assertNotNull(result);
        assertEquals(task.getTitle(), result.getTitle());
        assertEquals(task.getDescription(), result.getDescription());
    }

    @Test
    public void testGetTaskByIdNotFoundIntegration() {
        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(999L));
    }

    @Test
    @Transactional
    public void testGetTaskByUserIdIntegration() throws TaskNotFoundException, UserNotFoundException {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(), "password", new ArrayList<>()
        );

        SecurityContext securityContextMock = mock(SecurityContext.class);
        when(securityContextMock.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContextMock);

        TaskDTO result = taskService.getTaskByUserId(task.getId(), authentication);

        assertNotNull(result);
        assertEquals(task.getTitle(), result.getTitle());
        assertEquals(task.getDescription(), result.getDescription());

        SecurityContextHolder.clearContext();
    }

    @Test
    public void testCreateTaskIntegration() throws IllegalAttributeException, UserNotFoundException {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("New Task");
        taskDTO.setDescription("Description");
        taskDTO.setStatus(TaskStatusEnum.PENDING);
        TaskDTO createdTask = taskService.createTaskUser(taskDTO, new UsernamePasswordAuthenticationToken(user.getEmail(), "password", new ArrayList<>()));

        assertNotNull(createdTask);
        assertEquals("New Task", createdTask.getTitle());
        assertEquals("Description", createdTask.getDescription());
    }

    @Test
    public void testCreateTaskInvalidTitleIntegration() {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setTitle("");
        taskDTO.setDescription("Description");
        taskDTO.setStatus(TaskStatusEnum.PENDING);
        assertThrows(IllegalAttributeException.class, () -> taskService.createTaskUser(taskDTO, new UsernamePasswordAuthenticationToken(user.getEmail(), "password", new ArrayList<>())));
    }

    @Test
    public void testUpdateTaskIntegration() throws TaskNotFoundException, IllegalAttributeException, UserNotFoundException {
        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setTitle("Updated Task");
        updatedTaskDTO.setDescription("Updated Description");
        updatedTaskDTO.setStatus(TaskStatusEnum.COMPLETED);

        TaskDTO updatedTask = taskService.updateTask(task.getId(), updatedTaskDTO);

        assertNotNull(updatedTask);
        assertEquals("Updated Task", updatedTask.getTitle());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertEquals(TaskStatusEnum.COMPLETED, updatedTask.getStatus());
    }

    @Test
    public void testDeleteTaskIntegration() throws TaskNotFoundException {
        taskService.deleteTask(task.getId());

        assertFalse(taskRepository.existsById(task.getId()));
    }

    @Test
    public void testDeleteTaskNotFoundIntegration() {
        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(999L));
    }

    @Test
    public void testCompleteTaskIntegration() throws TaskNotFoundException, UserNotFoundException {
        taskService.completeTask(task.getId());

        Task completedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertEquals(TaskStatusEnum.COMPLETED, completedTask.getStatus());
    }

    @Test
    public void testCompleteTaskNotFoundIntegration() {
        assertThrows(TaskNotFoundException.class, () -> taskService.completeTask(999L));
    }

    @Test
    public void testAccessDeniedOnUpdateTaskIntegration() {
        TaskDTO updatedTaskDTO = new TaskDTO();
        updatedTaskDTO.setTitle("Updated Task");
        updatedTaskDTO.setDescription("Updated Description");
        updatedTaskDTO.setStatus(TaskStatusEnum.COMPLETED);

        UserEntity anotherUser = new UserEntity("anotherUser", "another@test.com", "password", RoleEnum.USER);
        userRepository.save(anotherUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                anotherUser.getEmail(), "password", new ArrayList<>()
        );

        SecurityContext securityContextMock = mock(SecurityContext.class);
        when(securityContextMock.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContextMock);

        assertThrows(AccessDeniedException.class, () -> taskService.updateTaskUser(task.getId(), updatedTaskDTO, authentication));

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        taskRepository.deleteAll();

        userRepository.deleteAll();
    }
}
