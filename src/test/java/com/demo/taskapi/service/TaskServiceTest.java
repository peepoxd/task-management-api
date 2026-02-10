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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Project testProject;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = new User("testuser", "encoded-password", "test@demo.com", Role.USER);
        testUser.setId(1L);

        testProject = new Project("Test Project", "Description", testUser);
        testProject.setId(1L);

        testTask = new Task("Test Task", "Task description", TaskStatus.TODO,
                TaskPriority.HIGH, testProject, testUser);
        testTask.setId(1L);
    }

    @Test
    void findById_Found_ReturnsTaskResponse() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        TaskResponse response = taskService.findById(1L);

        assertThat(response.getTitle()).isEqualTo("Test Task");
        assertThat(response.getStatus()).isEqualTo("TODO");
        assertThat(response.getPriority()).isEqualTo("HIGH");
        assertThat(response.getProjectId()).isEqualTo(1L);
    }

    @Test
    void findById_NotFound_ThrowsException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Task not found with id: 99");
    }

    @Test
    void findAll_ReturnsPaginatedResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(List.of(testTask), pageable, 1);

        when(taskRepository.findAll(pageable)).thenReturn(page);

        PagedResponse<TaskResponse> response = taskService.findAll(pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.isLast()).isTrue();
    }

    @Test
    void create_ValidRequest_ReturnsTaskResponse() {
        CreateTaskRequest request = new CreateTaskRequest(
                "New Task", "Description", "HIGH", 1L, 1L);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        TaskResponse response = taskService.create(request);

        assertThat(response.getTitle()).isEqualTo("New Task");
        assertThat(response.getPriority()).isEqualTo("HIGH");
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void create_ProjectNotFound_ThrowsException() {
        CreateTaskRequest request = new CreateTaskRequest(
                "New Task", "Description", "HIGH", 99L, null);

        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Project not found");
    }

    @Test
    void update_ValidRequest_ReturnsUpdatedTask() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setStatus("IN_PROGRESS");
        request.setPriority("LOW");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        TaskResponse response = taskService.update(1L, request);

        assertThat(response).isNotNull();
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void delete_Exists_DeletesTask() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.delete(1L);

        verify(taskRepository).deleteById(1L);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(taskRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
