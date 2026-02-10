package com.demo.taskapi.service;

import com.demo.taskapi.dto.project.CreateProjectRequest;
import com.demo.taskapi.dto.project.ProjectResponse;
import com.demo.taskapi.entity.Project;
import com.demo.taskapi.entity.User;
import com.demo.taskapi.exception.ResourceNotFoundException;
import com.demo.taskapi.repository.ProjectRepository;
import com.demo.taskapi.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @Cacheable(value = "projects", key = "#id")
    public ProjectResponse findById(Long id) {
        Optional<Project> optional = projectRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Project", id);
        }
        return toResponse(optional.get());
    }

    public List<ProjectResponse> findAll() {
        List<Project> projects = projectRepository.findAll();
        List<ProjectResponse> result = new ArrayList<>();
        for (Project project : projects) {
            result.add(toResponse(project));
        }
        return result;
    }

    public List<ProjectResponse> findByOwner(Long ownerId) {
        List<Project> projects = projectRepository.findByOwnerId(ownerId);
        List<ProjectResponse> result = new ArrayList<>();
        for (Project project : projects) {
            result.add(toResponse(project));
        }
        return result;
    }

    @Transactional
    public ProjectResponse create(CreateProjectRequest request, String username) {
        Optional<User> optional = userRepository.findByUsername(username);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("User not found: " + username);
        }
        User owner = optional.get();

        Project project = new Project(request.getName(), request.getDescription(), owner);
        project = projectRepository.save(project);
        return toResponse(project);
    }

    @Transactional
    @CacheEvict(value = "projects", key = "#id")
    public ProjectResponse update(Long id, CreateProjectRequest request) {
        Optional<Project> optional = projectRepository.findById(id);
        if (optional.isEmpty()) {
            throw new ResourceNotFoundException("Project", id);
        }
        Project project = optional.get();

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project = projectRepository.save(project);
        return toResponse(project);
    }

    @CacheEvict(value = "projects", key = "#id")
    public void delete(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new ResourceNotFoundException("Project", id);
        }
        projectRepository.deleteById(id);
    }

    private ProjectResponse toResponse(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getOwner().getUsername(),
                project.getTasks().size(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
