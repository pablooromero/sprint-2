package com.mindhub.todolist.controllers;

import com.mindhub.todolist.dtos.TaskDTO;
import com.mindhub.todolist.dtos.UserDTO;
import com.mindhub.todolist.exceptions.IllegalAttributeException;
import com.mindhub.todolist.exceptions.TaskNotFoundException;
import com.mindhub.todolist.exceptions.UserNotFoundException;
import com.mindhub.todolist.services.TaskService;
import com.mindhub.todolist.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @Operation(summary = "Get all tasks", description = "Retrieve a list of all tasks")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of tasks",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskDTO.class),
                            examples = @ExampleObject(value = "[{\"id\": 1, \"title\": \"Task 1\", \"description\": \"Description of task 1\", \"status\": \"PENDING\", \"userId\": 1}]")
                    )
            )
    })
    @GetMapping("/tasks")
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        List<TaskDTO> tasks = taskService.getAllTasks()
                .stream()
                .map(TaskDTO::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }


    @Operation(summary = "Get a task by ID", description = "Retrieve details of a task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Task not found with ID: 1")
                    )
            )
    })
    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) throws UserNotFoundException, TaskNotFoundException {
        TaskDTO task = taskService.getTaskById(id);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }


    @Operation(summary = "Create a new task", description = "Create a new task and associate it with a user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Task created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation errors",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Task title cannot be null or empty")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "User not found with ID: 1")
                    )
            )
    })
    @PostMapping("/tasks")
    public ResponseEntity<TaskDTO> createTask(@RequestBody TaskDTO taskDTO) throws UserNotFoundException, IllegalAttributeException {
        TaskDTO newTask = taskService.createTaskAdmin(taskDTO);
        return new ResponseEntity<>(newTask, HttpStatus.CREATED);
    }


    @Operation(summary = "Update a task", description = "Update an existing task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Task updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation errors",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Task description must be at least 5 characters long")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "Task or user not found",
                    content = @Content(mediaType = "application/json",
                            examples = {
                                    @ExampleObject(value = "User not found with ID: 1")
                            }
                    )
            )
    })
    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskDTO taskDTO) throws TaskNotFoundException, UserNotFoundException, IllegalAttributeException {
        TaskDTO updatedTask = taskService.updateTask(id, taskDTO);
        return new ResponseEntity<>(updatedTask, HttpStatus.OK);
    }


    @Operation(summary = "Delete a task", description = "Delete an existing task by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Task not found with ID: 1")
                    )
            )
    })
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) throws TaskNotFoundException {
        taskService.deleteTask(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @Operation(summary = "Complete a task", description = "Mark a task as completed")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Task completed successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Task not found with ID: 1")
                    )
            )
    })
    @PutMapping("/tasks/complete/{id}")
    public ResponseEntity<Void> completeTask(@PathVariable Long id) throws TaskNotFoundException, UserNotFoundException {
        taskService.completeTask(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }



    @Operation(summary = "Get all users", description = "Retrieve a list of all users")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successful retrieval of users",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDTO.class),
                            examples = @ExampleObject(value = "[{\"id\": 1, \"username\": \"user1\", \"email\": \"user1@example.com\"}]")
                    )
            )
    })
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers()
                .stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @Operation(summary = "Get a user by ID", description = "Retrieve details of a user by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "User not found with ID: 1")
                    )
            )
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) throws UserNotFoundException {
        UserDTO user = userService.getUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }


    @Operation(summary = "Create a new user", description = "Create a new user and associate the details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation errors",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Email format is invalid")
                    )
            ),
            @ApiResponse(responseCode = "409", description = "Email already in use",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Email is already in use")
                    )
            )
    })
    @PostMapping("/users")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) throws IllegalAttributeException, UserNotFoundException {
        UserDTO newUser = userService.createUser(userDTO);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }



    @Operation(summary = "Create a new admin", description = "Create a new admin and associate the details")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User admin created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation errors",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Email format is invalid")
                    )
            ),
            @ApiResponse(responseCode = "409", description = "Email already in use",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Email is already in use")
                    )
            )
    })
    @PostMapping("/users/create-admin")
    public ResponseEntity<UserDTO> createAdmin(@RequestBody UserDTO userDTO) throws IllegalAttributeException {
        UserDTO newUser = userService.createAdmin(userDTO);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }


    @Operation(summary = "Update an existing user", description = "Update an existing user's details by their ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Validation errors",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "Username must be at least 3 characters long")
                    )
            ),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "User not found with ID: 1")
                    )
            )
    })
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) throws UserNotFoundException, IllegalAttributeException {
        UserDTO updatedUser = userService.updateUser(id, userDTO);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @Operation(summary = "Delete a user", description = "Delete an existing user by their ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "User not found with ID: 1")
                    )
            )
    })
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) throws UserNotFoundException {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
