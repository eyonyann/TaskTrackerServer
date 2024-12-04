package org.example.tasktrackerserver.controllers;

import org.example.tasktrackerserver.dtos.UserDTO;
import org.example.tasktrackerserver.models.Task;
import org.example.tasktrackerserver.models.User;
import org.example.tasktrackerserver.security.PasswordUtil;
import org.example.tasktrackerserver.services.TaskService;
import org.example.tasktrackerserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.findAllUsers();
    }

    @GetMapping("/user_info")
    public ResponseEntity<UserDTO> getUserInfo(@RequestParam(required = false) Long userId) {
        if (userId == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            userId = (Long) authentication.getDetails();
        }

        Optional<User> user = userService.findUserById(userId);

        if (user.isPresent()) {
            UserDTO userDTO = new UserDTO(
                    user.get().getId(),
                    user.get().getUsername(),
                    user.get().getFullname(),
                    user.get().getRole()
            );
            return ResponseEntity.ok(userDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/current")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getDetails();
        Optional<User> user = userService.findUserById(userId);

        if (user.isPresent()) {
            UserDTO userDTO = new UserDTO(
                    user.get().getId(),
                    user.get().getUsername(),
                    user.get().getFullname(),
                    user.get().getRole()
            );
            return ResponseEntity.ok(userDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping
    public User createUser(@RequestBody User user) throws NoSuchAlgorithmException {
        return userService.createUser(user);
    }

    @PutMapping("{userId}/role")
    public ResponseEntity<User> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> requestBody) {
        try {
            // Получаем новую роль из тела запроса
            String newRole = requestBody.get("role");
            if (newRole == null || newRole.isBlank()) {
                return ResponseEntity.badRequest().body(null);
            }

            // Обновляем роль пользователя через сервис
            User updatedUser = userService.updateUserRole(userId, newRole);

            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @PutMapping("{userId}/project")
    public ResponseEntity<User> updateUserProject(
            @PathVariable Long userId,
            @RequestBody Map<String, String> requestBody) {
        try {
            // Получаем имя нового проекта из тела запроса
            String newProjectName = requestBody.get("projectName");
            if (newProjectName == null || newProjectName.isBlank()) {
                return ResponseEntity.badRequest().body(null);
            }

            // Обновляем проект пользователя через сервис
            User updatedUser = userService.updateUserProject(userId, newProjectName);

            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("{userId}/delete")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        try {
            // Найти пользователя по ID
            User user = userService.findUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден."));

            // Отключаем проект для пользователя, если есть
            user.setProject(null);

            switch (user.getRole()) {
                case TESTER:
                    List<Task> testerTasks = taskService.getTasksByTesterId(userId);
                    if (!testerTasks.isEmpty()) {
                        for (Task task : testerTasks) {
                            if (task.getStatus() == Task.Status.DONE) {
                                task.setTester(null); // Убираем тестера
                            } else if (task.getStatus() == Task.Status.REVIEW) {
                                task.setTester(null); // Убираем тестера
                                task.setStatus(Task.Status.IN_PROGRESS); // Возвращаем задачу в статус "В процессе"
                            }
                            taskService.updateTask(task.getId(), task); // Обновляем задачу
                        }
                    }
                    break;

                case ADMIN:
                    if (userService.countAdmins() > 1) {
                        break;
                    } else {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Нельзя удалить последнего администратора.");
                    }

                case DEVELOPER:
                    List<Task> developerTasks = taskService.getTasksByDeveloperId(userId);
                    if (!developerTasks.isEmpty()) {
                        for (Task task : developerTasks) {
                            if (task.getStatus() == Task.Status.DONE) {
                                task.setDeveloper(null); // Убираем разработчика
                            } else {
                                task.setStatus(Task.Status.BACKLOG); // Возвращаем задачу в статус "В бэклог"
                                task.setDeveloper(null); // Убираем разработчика
                                task.setEndTime(null); // Очистка времени завершения
                            }
                            taskService.updateTask(task.getId(), task); // Обновляем задачу
                        }
                    }
                    break;

                default:
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Неизвестная роль пользователя.");
            }

            // После обработки всех задач, удаляем пользователя
            userService.deleteUser(userId);
            return ResponseEntity.ok("Пользователь успешно удален.");

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при удалении пользователя.");
        }
    }








    @PostMapping("/user_info")
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(user);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/is_this_user/{userId}")
    public Boolean isThisUserInSystem(@PathVariable Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long systemUserId = (Long) authentication.getDetails();
        return userId.equals(systemUserId);
    }


    @PostMapping("/verify_password")
    public ResponseEntity<?> verifyPassword(@RequestBody String currentPassword, Authentication authentication) throws NoSuchAlgorithmException {
        Long userId = (Long) authentication.getDetails();
        Optional<User> user = userService.findUserById(userId);

        if (user.isPresent() && PasswordUtil.hashPassword(currentPassword, user.get().getSalt()).equals(user.get().getPassword())) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

}
