package com.mindhub.todolist.dtos;

import com.mindhub.todolist.enums.TaskStatusEnum;
import com.mindhub.todolist.models.Task;

public class TaskDTO {
    private Long id;

    private String title;
    private String description;
    private TaskStatusEnum status;
    private Long userId;

    public TaskDTO(Task task) {
        id = task.getId();
        title = task.getTitle();
        description = task.getDescription();
        status = task.getStatus();
        userId = task.getUser() != null ? task.getUser().getId() : null;
    }

    public TaskDTO() {}

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatusEnum getStatus() {
        return status;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(TaskStatusEnum status) {
        this.status = status;
    }
}
