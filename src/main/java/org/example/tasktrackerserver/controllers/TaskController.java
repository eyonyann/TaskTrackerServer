package org.example.tasktrackerserver.controllers;

import org.example.tasktrackerserver.dtos.TaskDTO;
import org.example.tasktrackerserver.models.Task;
import org.example.tasktrackerserver.models.User;
import org.example.tasktrackerserver.services.ProjectService;
import org.example.tasktrackerserver.services.TaskService;
import org.example.tasktrackerserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "http://localhost:8080")
public class TaskController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private UserService userService;

    @GetMapping
    public List<TaskDTO> getTasksByUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getDetails();
        User user = userService.findUserById(userId).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Если роль ADMIN, возвращаем задачи из всех проектов
        if (user.getRole().toString().equals("ADMIN")) {
            return projectService.findAllProjects() // Получаем все проекты
                    .stream()
                    .flatMap(project -> taskService.getTasksByProjectId(project.getId()).stream()) // Получаем задачи каждого проекта
                    .map(taskService::convertToDTO) // Конвертируем задачи в DTO
                    .toList();
        }
        // Если роль DEVELOPER, возвращаем задачи, связанные с разработчиком
        else if (user.getRole().toString().equals("DEVELOPER")) {
            return taskService.getTasksByDeveloperId(userId)
                    .stream()
                    .map(taskService::convertToDTO)
                    .toList();
        }
        // Если роль TESTER, возвращаем задачи, связанные с тестировщиком
        else if (user.getRole().toString().equals("TESTER")) {
            return taskService.getTasksByTesterId(userId)
                    .stream()
                    .map(taskService::convertToDTO)
                    .toList();
        }

        // Если роль не определена, возвращаем пустой список
        return List.of();
    }



    @GetMapping("/{projectId}")
    public List<TaskDTO> getTasksByProjectId(@PathVariable Long projectId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getDetails();

        Optional<User> user = userService.findUserById(userId);

        if (user.isEmpty() || user.get().getProject() == null) {
            throw new RuntimeException("User or associated project not found");
        }

        Long userProjectId = user.get().getProject().getId();

        // Проверка на соответствие projectId пользователя и переданного параметра
        if (!userProjectId.equals(projectId)) {
            throw new RuntimeException("User does not have access to this project");
        }

        System.out.println("Project ID: " + userProjectId);
        return taskService.getTasksByProjectId(userProjectId)
                .stream()
                .map(taskService::convertToDTO)
                .toList();
    }


    @GetMapping("/task_info/{taskId}")
    public TaskDTO getTaskById(@PathVariable Long taskId) {
        return taskService.getTaskById(taskId);
    }



    @GetMapping("/role")
    public List<TaskDTO> getAllTasks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        System.out.println("Запрос задач для пользователя: " + username);
        System.out.println("инфа: " + authentication.getAuthorities().toString());

        if (authentication.getAuthorities().toString().contains("ADMIN")) {
            return taskService.findAllTasks()
                    .stream()
                    .map(taskService::convertToDTO)
                    .toList();
        } else if (authentication.getAuthorities().toString().contains("DEVELOPER")) {
            return taskService.findTasksForDeveloper(username)
                    .stream()
                    .map(taskService::convertToDTO)
                    .toList();
        } else if (authentication.getAuthorities().toString().contains("TESTER")) {
            return taskService.findTasksForTester(username)
                    .stream()
                    .map(taskService::convertToDTO)
                    .toList();
        }
        return List.of();
    }



    @PutMapping("/move_to_backlog/{userId}")
    public ResponseEntity<Void> moveTasksToBacklog(@PathVariable Long userId) {
        try {
            taskService.moveTasksToBacklog(userId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }



    @PostMapping
    public Task createTask(@RequestBody TaskDTO taskDTO) {
        System.out.println(taskDTO);
        Task task = taskService.convertToTask(taskDTO);
        System.out.println(taskDTO);
        return taskService.createTask(task);
    }

    @PutMapping("/{taskId}")
    public Task updateTask(@PathVariable Long taskId, @RequestBody TaskDTO taskDTO) {
        System.out.println("Полученный JSON: " + taskDTO);
        Task task = taskService.convertToTask(taskDTO);
        System.out.println("Преобразованная задача: " + task);
        System.out.println("Задача  с айди разраба " + (task.getDeveloper() != null ? task.getDeveloper().getId() : "null") + " будет обновлена");
        return taskService.updateTask(taskId, task);
    }


    @PutMapping("/{taskId}/set_developer")
    public Task setDeveloperForTask(@PathVariable Long taskId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getDetails();
        return taskService.setDeveloper(taskId, userId);
    }

    @PutMapping("/{taskId}/set_tester")
    public Task setTesterForTask(@PathVariable Long taskId) {
        return taskService.setTester(taskId);
    }
}
