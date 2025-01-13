package com.mindhub.todolist.repositories;

import com.mindhub.todolist.models.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByIdAndUserEntityId(Long id, Long userId);
}
