package com.mindhub.todolist.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindhub.todolist.config.JwtUtils;
import com.mindhub.todolist.dtos.TaskDTO;
import com.mindhub.todolist.enums.RoleEnum;
import com.mindhub.todolist.enums.TaskStatusEnum;
import com.mindhub.todolist.models.Task;
import com.mindhub.todolist.models.UserEntity;
import com.mindhub.todolist.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.ArrayList;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@ActiveProfiles("test")
@Import(JwtUtils.class)
@AutoConfigureMockMvc(addFilters = false)
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    TaskService taskService;

    @Mock
    Authentication authentication;

    private UserEntity user;
    private Task task;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        user = new UserEntity("pabloromero", "pabloromerook@gmail.com", "test123", RoleEnum.USER);
        user.setId(1L);

        task = new Task();
        task.setId(1L);
        task.setTitle("Task 1");
        task.setDescription("Task 1");
        task.setStatus(TaskStatusEnum.PENDING);
        task.setUser(user);

        MockitoAnnotations.openMocks(this);

    }

    @Test
    void getAllTasksByUserIdSuccess() throws Exception {
        List<Task> mockTasks = new ArrayList<>();

        mockTasks.add(task);

        when(taskService.getAllTasksByUserId(any(Authentication.class))).thenReturn(mockTasks);

        mockMvc.perform(get("/api/user/tasks")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Task 1"));

        verify(taskService, times(1)).getAllTasksByUserId(any(Authentication.class));
    }



    @Test
    void getTaskByUserIdSuccess() throws Exception {
        TaskDTO mockTaskDTO = new TaskDTO(task);

        when(taskService.getTaskByUserId(eq(task.getId()), any(Authentication.class))).thenReturn(mockTaskDTO);

        mockMvc.perform(get("/api/user/tasks/{id}", task.getId())
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task 1"));

        verify(taskService, times(1)).getTaskByUserId(eq(task.getId()), any(Authentication.class));
    }

    @Test
    void createTaskUserSuccess() throws Exception {
        TaskDTO mockTaskDTO = new TaskDTO(task);

        when(taskService.createTaskUser(any(TaskDTO.class), any(Authentication.class))).thenReturn(mockTaskDTO);

        mockMvc.perform(post("/api/user/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockTaskDTO))
                        .principal(authentication))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Task 1"));

        verify(taskService, times(1)).createTaskUser(any(TaskDTO.class), any(Authentication.class));
    }

    @Test
    void updateTaskUserSuccess() throws Exception {
        TaskDTO mockTaskDTO = new TaskDTO(task);

        when(taskService.updateTaskUser(eq(task.getId()), any(TaskDTO.class), any(Authentication.class))).thenReturn(mockTaskDTO);

        mockMvc.perform(put("/api/user/tasks/{id}", task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockTaskDTO))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Task 1"));

        verify(taskService, times(1)).updateTaskUser(eq(task.getId()), any(TaskDTO.class), any(Authentication.class));
    }

    @Test
    void deleteTaskUserSuccess() throws Exception {
        doNothing().when(taskService).deleteTaskByUser(eq(task.getId()), any(Authentication.class));

        mockMvc.perform(delete("/api/user/tasks/{id}", task.getId())
                        .principal(authentication))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).deleteTaskByUser(eq(task.getId()), any(Authentication.class));
    }

    @Test
    void completeTaskUserSuccess() throws Exception {
        doNothing().when(taskService).completeTaskUser(eq(task.getId()), any(Authentication.class));

        mockMvc.perform(put("/api/user/tasks/complete/{id}", task.getId())
                        .principal(authentication))
                .andExpect(status().isNoContent());

        verify(taskService, times(1)).completeTaskUser(eq(task.getId()), any(Authentication.class));
    }

}
