package com.demo.taskapi.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateTaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String priority; // LOW, MEDIUM, HIGH

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long assigneeId;

    public CreateTaskRequest() {}

    public CreateTaskRequest(String title, String description, String priority,
                             Long projectId, Long assigneeId) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.projectId = projectId;
        this.assigneeId = assigneeId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getAssigneeId() { return assigneeId; }
    public void setAssigneeId(Long assigneeId) { this.assigneeId = assigneeId; }
}
