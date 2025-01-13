package com.mindhub.todolist.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/welcome")
public class AppController {

    @Operation(summary = "Welcome message", description = "Get a welcome message")
    @ApiResponse(responseCode = "200", description = "Welcome message received")
    @GetMapping
    public String getWelcome() {
        return "Welcome to Pablo's Todo List!";
    }
}
