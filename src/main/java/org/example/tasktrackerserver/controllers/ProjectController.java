package org.example.tasktrackerserver.controllers;

import org.example.tasktrackerserver.dtos.ProjectDTO;
import org.example.tasktrackerserver.dtos.ProjectStatisticsDTO;
import org.example.tasktrackerserver.models.Project;
import org.example.tasktrackerserver.models.Task;
import org.example.tasktrackerserver.models.User;
import org.example.tasktrackerserver.services.ProjectService;
import org.example.tasktrackerserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@CrossOrigin(origins = "http://localhost:8080")
public class ProjectController {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @GetMapping("/project_info")
    public ResponseEntity<ProjectDTO> getProjectByUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getDetails();

        Optional<User> user = userService.findUserById(userId);

        if (user.isPresent() && user.get().getProject() != null) {
            Project project = user.get().getProject();

            // Printing project object to see full details
            System.out.println(project); // Will now use the overridden toString() method

            ProjectDTO projectDTO = new ProjectDTO(
                    project.getId(),
                    project.getName(),
                    project.getDescription(),
                    project.getDeadline()
            );

            // You can print the ProjectDTO fields as well, just for debug
            System.out.println(projectDTO.getName() + " " + projectDTO.getDescription() + " " + projectDTO.getDeadline() + " " + projectDTO.getId());

            return ResponseEntity.ok(projectDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/{userId}/project_info")
    public ResponseEntity<ProjectDTO> getProjectByUserId(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Optional<User> user = userService.findUserById(userId);

        if (user.isPresent() && user.get().getProject() != null) {
            Project project = user.get().getProject();

            // Printing project object to see full details
            System.out.println(project); // Will now use the overridden toString() method

            ProjectDTO projectDTO = new ProjectDTO(
                    project.getId(),
                    project.getName(),
                    project.getDescription(),
                    project.getDeadline()
            );

            // You can print the ProjectDTO fields as well, just for debug
            System.out.println(projectDTO.getName() + " " + projectDTO.getDescription() + " " + projectDTO.getDeadline() + " " + projectDTO.getId());

            return ResponseEntity.ok(projectDTO);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getDetails();

        // Проверяем, существует ли пользователь и имеет ли он роль ADMIN
        Optional<User> user = userService.findUserById(userId);
        if (user.isEmpty() || user.get().getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Преобразуем список проектов в ProjectDTO
        List<Project> projects = projectService.findAllProjects();
        List<ProjectDTO> projectDTOs = projects.stream()
                .map(project -> new ProjectDTO(
                        project.getId(),
                        project.getName(),
                        project.getDescription(),
                        project.getDeadline()
                ))
                .toList();

        return ResponseEntity.ok(projectDTOs);
    }





//    @GetMapping("/role")
//    public List<Task> getAllTasks() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String username = authentication.getName();
//        System.out.println("Запрос задач для пользователя: " + username);
//        System.out.println("инфа: " + authentication.getAuthorities().toString());
//
//        if (authentication.getAuthorities().toString().contains("ADMIN")) {
//            return taskService.findAllTasks();
//        } else if (authentication.getAuthorities().toString().contains("DEVELOPER")) {
//            return taskService.findTasksForDeveloper(username);
//        } else if (authentication.getAuthorities().toString().contains("TESTER")) {
//            return taskService.findTasksForTester(username);
//        }
//        return List.of();
//    }


    @GetMapping("/statistics")
    public ResponseEntity<List<ProjectStatisticsDTO>> getAllProjectsStatistics() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getDetails();

        Optional<User> user = userService.findUserById(userId);
        if (user.isEmpty() || user.get().getRole() != User.Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<ProjectStatisticsDTO> statistics = projectService.getAllProjectsStatistics();
        System.out.println("Запрашиваема статистика " + statistics);
        return ResponseEntity.ok(statistics);
    }



    @PostMapping
    public Project createProject(@RequestBody Project project) throws NoSuchAlgorithmException {
        return projectService.createProject(project);
    }

    @PutMapping("/{projectId}")
    public Project updateProject(@PathVariable Long projectId, @RequestBody Project project) {
        return projectService.updateProject(projectId, project);
    }
}
