package com.mindhub.todolist;

import com.mindhub.todolist.enums.RoleEnum;
import com.mindhub.todolist.enums.TaskStatusEnum;
import com.mindhub.todolist.models.Task;
import com.mindhub.todolist.models.UserEntity;
import com.mindhub.todolist.repositories.TaskRepository;
import com.mindhub.todolist.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class TodolistApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodolistApplication.class, args);
	}

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Bean
	public CommandLineRunner initData(UserRepository userRepository, TaskRepository taskRepository) {
		return args -> {
			UserEntity userEntity = new UserEntity("promero", "pabloromerook@gmail.com", passwordEncoder.encode("test123"), RoleEnum.USER);
			userRepository.save(userEntity);
			UserEntity userEntity2 = new UserEntity("promeroo", "pabloromerook2@gmail.com", passwordEncoder.encode("test123"), RoleEnum.ADMIN);
			userRepository.save(userEntity2);
			UserEntity userEntity3 = new UserEntity("promerooo", "pabloromerook3@gmail.com", passwordEncoder.encode("test123"), RoleEnum.USER);
			userRepository.save(userEntity3);

			Task task = new Task("Tarea 1", "Tarea 1", TaskStatusEnum.IN_PROGRESS);
			userEntity.addTask(task);
			taskRepository.save(task);

			Task task2 = new Task("Tarea 3", "Tarea 3", TaskStatusEnum.IN_PROGRESS);
			userEntity.addTask(task2);
			taskRepository.save(task2);

			Task task3 = new Task("Tarea 2", "Tarea 2", TaskStatusEnum.IN_PROGRESS);
			userEntity3.addTask(task3);
			taskRepository.save(task3);

		};
	}

}
