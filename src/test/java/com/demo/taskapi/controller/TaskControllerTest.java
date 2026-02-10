package com.demo.taskapi.controller;

import com.demo.taskapi.dto.PagedResponse;
import com.demo.taskapi.dto.task.CreateTaskRequest;
import com.demo.taskapi.dto.task.TaskResponse;
import com.demo.taskapi.exception.GlobalExceptionHandler;
import com.demo.taskapi.exception.ResourceNotFoundException;
import com.demo.taskapi.security.JwtTokenProvider;
import com.demo.taskapi.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_ReturnsPaginatedTasks() throws Exception {
        TaskResponse task = createSampleTaskResponse();
        PagedResponse<TaskResponse> pagedResponse = new PagedResponse<>(
                List.of(task), 0, 10, 1, 1, true);

        when(taskService.findAll(any())).thenReturn(pagedResponse);

        mockMvc.perform(get("/api/v1/tasks")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Task"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void getById_Found_ReturnsTask() throws Exception {
        TaskResponse task = createSampleTaskResponse();

        when(taskService.findById(1L)).thenReturn(task);

        mockMvc.perform(get("/api/v1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void getById_NotFound_Returns404() throws Exception {
        when(taskService.findById(99L))
                .thenThrow(new ResourceNotFoundException("Task", 99L));

        mockMvc.perform(get("/api/v1/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    void create_ValidInput_Returns201() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
                "New Task", "Description", "HIGH", 1L, null);
        TaskResponse response = createSampleTaskResponse();

        when(taskService.create(any(CreateTaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void create_MissingTitle_Returns400() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
                "", "Description", "HIGH", 1L, null);

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    private TaskResponse createSampleTaskResponse() {
        TaskResponse task = new TaskResponse();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setStatus("TODO");
        task.setPriority("HIGH");
        task.setProjectId(1L);
        task.setProjectName("Test Project");
        task.setAssigneeId(1L);
        task.setAssigneeUsername("testuser");
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        return task;
    }
}
