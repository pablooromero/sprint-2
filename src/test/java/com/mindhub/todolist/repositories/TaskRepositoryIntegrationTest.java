package com.mindhub.todolist.repositories;

import com.mindhub.todolist.enums.TaskStatusEnum;
import com.mindhub.todolist.models.Task;
import com.mindhub.todolist.models.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TaskRepositoryIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("securePassword");
        user = userRepository.save(user);
    }

    @Test
    @DisplayName("Find task by ID and user ID - Success")
    void findByIdAndUserEntityIdSuccess() {
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("This is a test task");
        task.setUser(user);
        task.setStatus(TaskStatusEnum.IN_PROGRESS);
        task = taskRepository.save(task);

        Optional<Task> result = taskRepository.findByIdAndUserEntityId(task.getId(), user.getId());

        assertTrue(result.isPresent(), "Task should be found");
        assertEquals(task.getId(), result.get().getId(), "Task ID should match");
        assertEquals(user.getId(), result.get().getUser().getId(), "User ID should match");
    }

    @Test
    @DisplayName("Find task by ID and user ID - No match for ID")
    void findByIdAndUserEntityIdNoMatchForId() {
        Optional<Task> result = taskRepository.findByIdAndUserEntityId(999L, user.getId());

        assertFalse(result.isPresent(), "Task should not be found for non-existent task ID");
    }

    @Test
    @DisplayName("Find task by ID and user ID - No match for user ID")
    void findByIdAndUserEntityIdNoMatchForUserId() {
        UserEntity anotherUser = new UserEntity();
        anotherUser.setUsername("anotherUser");
        anotherUser.setEmail("another@example.com");
        anotherUser.setPassword("anotherPassword");
        anotherUser = userRepository.save(anotherUser);

        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("This is a test task");
        task.setUser(user);
        task.setStatus(TaskStatusEnum.IN_PROGRESS);
        task = taskRepository.save(task);

        Optional<Task> result = taskRepository.findByIdAndUserEntityId(task.getId(), anotherUser.getId());

        assertFalse(result.isPresent(), "Task should not be found for a different user ID");
    }

    @Test
    @DisplayName("Find task by ID and user ID - Task not found for non-existent user")
    void findByIdAndUserEntityIdTaskNotFoundForNonExistentUser() {
        UserEntity nonExistentUser = new UserEntity();
        nonExistentUser.setUsername("nonExistentUser");
        nonExistentUser.setEmail("nonexistent@example.com");
        nonExistentUser.setPassword("password");
        nonExistentUser = userRepository.save(nonExistentUser);

        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("This is a test task");
        task.setUser(nonExistentUser);
        task.setStatus(TaskStatusEnum.IN_PROGRESS);
        task = taskRepository.save(task);

        Long nonExistentUserId = nonExistentUser.getId() + 1;

        Optional<Task> result = taskRepository.findByIdAndUserEntityId(task.getId(), nonExistentUserId);

        assertFalse(result.isPresent(), "Task should not be found for non-existent user ID");
    }



    @Test
    @DisplayName("Find task by ID and user ID - Multiple tasks")
    void findByIdAndUserEntityIdMultipleTasks() {
        Task task1 = new Task();
        task1.setTitle("Task 1");
        task1.setDescription("Task 1 description");
        task1.setUser(user);
        task1.setStatus(TaskStatusEnum.IN_PROGRESS);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("Task 2");
        task2.setDescription("Task 2 description");
        task2.setUser(user);
        task2.setStatus(TaskStatusEnum.COMPLETED);
        taskRepository.save(task2);

        Optional<Task> result1 = taskRepository.findByIdAndUserEntityId(task1.getId(), user.getId());
        Optional<Task> result2 = taskRepository.findByIdAndUserEntityId(task2.getId(), user.getId());

        assertTrue(result1.isPresent(), "Task 1 should be found");
        assertTrue(result2.isPresent(), "Task 2 should be found");
    }
}
