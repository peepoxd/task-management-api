package com.demo.taskapi.service;

import com.demo.taskapi.dto.PagedResponse;
import com.demo.taskapi.dto.task.CreateTaskRequest;
import com.demo.taskapi.dto.task.TaskResponse;
import com.demo.taskapi.dto.task.UpdateTaskRequest;
import com.demo.taskapi.entity.*;
import com.demo.taskapi.exception.ResourceNotFoundException;
import com.demo.taskapi.repository.ProjectRepository;
import com.demo.taskapi.repository.TaskRepository;
import com.demo.taskapi.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository,
                       UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public TaskResponse findById(Long id) {
        Optional<Task> optional = taskRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Task", id);
        }
        return toResponse(optional.get());
    }

    public PagedResponse<TaskResponse> findByProject(Long projectId, Pageable pageable) {
        Page<Task> page = taskRepository.findByProjectId(projectId, pageable);
        return toPagedResponse(page);
    }

    public PagedResponse<TaskResponse> findByAssignee(Long assigneeId, Pageable pageable) {
        Page<Task> page = taskRepository.findByAssigneeId(assigneeId, pageable);
        return toPagedResponse(page);
    }

    public PagedResponse<TaskResponse> findAll(Pageable pageable) {
        Page<Task> page = taskRepository.findAll(pageable);
        return toPagedResponse(page);
    }

    @Transactional
    public TaskResponse create(CreateTaskRequest request) {
        // หา project
        Optional<Project> projectOpt = projectRepository.findById(request.getProjectId());
        if (projectOpt.isEmpty()) {
            throw new ResourceNotFoundException("Project", request.getProjectId());
        }
        Project project = projectOpt.get();

        // หา assignee (ถ้ามี)
        User assignee = null;
        if (request.getAssigneeId() != null) {
            Optional<User> userOpt = userRepository.findById(request.getAssigneeId());
            if (userOpt.isEmpty()) {
                throw new ResourceNotFoundException("User", request.getAssigneeId());
            }
            assignee = userOpt.get();
        }

        // กำหนด priority (default = MEDIUM)
        TaskPriority priority;
        if (request.getPriority() != null) {
            priority = TaskPriority.valueOf(request.getPriority().toUpperCase());
        } else {
            priority = TaskPriority.MEDIUM;
        }

        Task task = new Task(
                request.getTitle(),
                request.getDescription(),
                TaskStatus.TODO,
                priority,
                project,
                assignee
        );

        task = taskRepository.save(task);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse update(Long id, UpdateTaskRequest request) {
        Optional<Task> optional = taskRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Task", id);
        }
        Task task = optional.get();

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(TaskStatus.valueOf(request.getStatus().toUpperCase()));
        }
        if (request.getPriority() != null) {
            task.setPriority(TaskPriority.valueOf(request.getPriority().toUpperCase()));
        }
        if (request.getAssigneeId() != null) {
            Optional<User> userOpt = userRepository.findById(request.getAssigneeId());
            if (userOpt.isEmpty()) {
                throw new ResourceNotFoundException("User", request.getAssigneeId());
            }
            task.setAssignee(userOpt.get());
        }

        task = taskRepository.save(task);
        return toResponse(task);
    }

    public void delete(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task", id);
        }
        taskRepository.deleteById(id);
    }

    private TaskResponse toResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus().name());
        response.setPriority(task.getPriority().name());
        response.setProjectId(task.getProject().getId());
        response.setProjectName(task.getProject().getName());
        if (task.getAssignee() != null) {
            response.setAssigneeId(task.getAssignee().getId());
            response.setAssigneeUsername(task.getAssignee().getUsername());
        }
        response.setCreatedAt(task.getCreatedAt());
        response.setUpdatedAt(task.getUpdatedAt());
        return response;
    }

    private PagedResponse<TaskResponse> toPagedResponse(Page<Task> page) {
        List<TaskResponse> content = new ArrayList<>();
        for (Task task : page.getContent()) {
            content.add(toResponse(task));
        }

        return new PagedResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
