package com.mindhub.todolist;

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

@SpringBootApplication
public class TodolistApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodolistApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(UserRepository userRepository, TaskRepository taskRepository) {
		return args -> {
			UserEntity userEntity = new UserEntity("pablooromero", "pablooromero@gmail.com");
			System.out.println(userEntity);
			userRepository.save(userEntity);
			System.out.println(userEntity);

			Task task = new Task("Tarea 1", "Tarea 1", TaskStatusEnum.IN_PROGRESS);
			userEntity.addTask(task);
			taskRepository.save(task);
			System.out.println(task);

		};
	}

}
