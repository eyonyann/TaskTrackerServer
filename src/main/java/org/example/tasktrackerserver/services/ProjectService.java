package org.example.tasktrackerserver.services;

import org.example.tasktrackerserver.dtos.ProjectStatisticsDTO;
import org.example.tasktrackerserver.dtos.TaskCompletionDTO;
import org.example.tasktrackerserver.dtos.UserStatisticsDTO;
import org.example.tasktrackerserver.models.Project;
import org.example.tasktrackerserver.models.Task;
import org.example.tasktrackerserver.models.User;
import org.example.tasktrackerserver.repositories.TaskRepository;
import org.example.tasktrackerserver.repositories.UserRepository;
import org.example.tasktrackerserver.repositories.ProjectRepository;
import org.example.tasktrackerserver.security.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    public List<Project> findAllProjects() {
        return projectRepository.findAll();
    }

    public Project createProject(Project project) {
        return projectRepository.save(project);
    }

    public Project updateProject(Long projectId, Project project) {
        Optional<Project> existingProject = projectRepository.findById(projectId);
        if (existingProject.isPresent()) {
            project.setId(projectId);
            return projectRepository.save(project);
        }
        throw new RuntimeException("Task not found");
    }

    public Optional<Project> findProjectById(Long projectId) {
        return projectRepository.findById(projectId);
    }

    public Optional<Project> findProjectByProjectId(Long projectId) {
        return projectRepository.findById(projectId);
    }


    public List<ProjectStatisticsDTO> getAllProjectsStatistics() {
        List<Project> projects = projectRepository.findAll(); // Получаем все проекты

        return projects.stream().map(project -> {
            List<User> users = userRepository.findByProjectId(project.getId());

            List<UserStatisticsDTO> userStatistics = users.stream().map(user -> {
                List<Task> completedTasks;

                // Для разработчика смотрим по endTime, для тестера — по checkTime
                if (user.getRole() == User.Role.DEVELOPER) {
                    completedTasks = taskRepository.findByDeveloperAndStatus(user, Task.Status.DONE);
                } else if (user.getRole() == User.Role.TESTER) {
                    completedTasks = taskRepository.findByTesterAndStatus(user, Task.Status.DONE);
                } else {
                    completedTasks = new ArrayList<>();
                }

                // Группируем задачи по дате выполнения
                Map<LocalDateTime, Long> tasksGroupedByDate = completedTasks.stream()
                        .filter(task -> {
                            // Для разработчиков проверяем endTime, для тестеров — checkTime
                            if (user.getRole() == User.Role.DEVELOPER) {
                                return task.getEndTime() != null;
                            } else if (user.getRole() == User.Role.TESTER) {
                                return task.getCheckTime() != null;
                            }
                            return false;
                        })
                        .collect(Collectors.groupingBy(task -> {
                            if (user.getRole() == User.Role.DEVELOPER) {
                                return task.getEndTime();
                            } else if (user.getRole() == User.Role.TESTER) {
                                return task.getCheckTime();
                            }
                            return null; // Не будет вызвано для других ролей
                        }, Collectors.counting()));

                // Создаем список DTO для каждой даты и количества выполненных задач
                List<TaskCompletionDTO> taskCompletions = tasksGroupedByDate.entrySet().stream()
                        .map(entry -> new TaskCompletionDTO(entry.getKey(), entry.getValue().intValue()))
                        .toList();

                return new UserStatisticsDTO(
                        user.getId(),
                        user.getFullname(),
                        user.getRole().toString(),
                        taskCompletions
                );
            }).toList();

            return new ProjectStatisticsDTO(
                    project.getId(),
                    project.getName(),
                    userStatistics
            );
        }).toList();
    }

}

