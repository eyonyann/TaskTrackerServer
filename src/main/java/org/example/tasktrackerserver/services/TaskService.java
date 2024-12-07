package org.example.tasktrackerserver.services;

import org.example.tasktrackerserver.dtos.TaskDTO;
import org.example.tasktrackerserver.models.Project;
import org.example.tasktrackerserver.models.Task;
import org.example.tasktrackerserver.models.User;
import org.example.tasktrackerserver.repositories.ProjectRepository;
import org.example.tasktrackerserver.repositories.TaskRepository;
import org.example.tasktrackerserver.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProjectRepository projectRepository;

    public List<Task> findAllTasks() {
        return taskRepository.findAll();
    }

    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

//    public Task updateTask(Long taskId, Task task) {
//        Optional<Task> existingTask = taskRepository.findById(taskId);
//        if (existingTask.isPresent()) {
//            task.setId(taskId);
//            return taskRepository.save(task);
//        }
//        throw new RuntimeException("Task not found");
//    }

    public Task updateTask(Long taskId, Task updatedTask) {
        Optional<Task> existingTaskOpt = taskRepository.findById(taskId);
        if (existingTaskOpt.isPresent()) {
            Task existingTask = existingTaskOpt.get();
            existingTask.setStatus(updatedTask.getStatus());
            existingTask.setEndTime(updatedTask.getEndTime());
            existingTask.setCheckTime(updatedTask.getCheckTime());
            existingTask.setDeveloper(updatedTask.getDeveloper());
            return taskRepository.save(existingTask);
        } else {
            throw new IllegalArgumentException("Задача с ID " + taskId + " не найдена");
        }
    }


    public Task setDeveloper(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        task.setDeveloper(user);
        return taskRepository.save(task);
    }


    public Task setTester(Long taskId) {
        // Находим задачу по ID
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Находим список тестировщиков с минимальной нагрузкой
        List<User> testers = userRepository.findTestersWithMinimumTasks(User.Role.TESTER);

        if (testers.isEmpty()) {
            throw new RuntimeException("No testers available");
        }

        // Берём первого тестировщика из списка
        User tester = testers.get(0);

        // Назначаем тестировщика задаче
        task.setTester(tester);
        return taskRepository.save(task);
    }


    public List<Task> getTasksByAssignee(User assignee) {
        return taskRepository.findByDeveloper(assignee);
    }

    public List<Task> getTasksByPriority(Task.Priority priority) {
        return taskRepository.findByPriority(priority);
    }

    // Задачи для разработчика, например, не завершенные или с определенным статусом
    public List<Task> findTasksForDeveloper(String username) {
        User developer = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        // Задачи, назначенные на разработчика, с открытым статусом
        return taskRepository.findByDeveloper(developer);
    }

    // Задачи для тестера, например, с определенным статусом (в процессе тестирования)
    public List<Task> findTasksForTester(String username) {
        User tester = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

        // Задачи, назначенные на тестера, с соответствующим статусом (например, "Test" или "Review")
        return taskRepository.findByDeveloper(tester);
    }

    public User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<Task> getTasksByDeveloperId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        logger.info("Получаем задачи для пользователя с ID: {}", userId);

        List<Task> tasks = taskRepository.findByDeveloper(user);

        logger.info("Найдено задач для пользователя с ID {}: {}", userId, tasks.size());

        return tasks;
    }

    public List<Task> getTasksByTesterId(Long testerId) {
        User tester = userRepository.findById(testerId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        logger.info("Получаем задачи для тестера с ID: {}", testerId);

        List<Task> tasks = taskRepository.findByTester(tester);

        logger.info("Найдено задач для разраба с ID {}: {}", testerId, tasks.size());

        return tasks;
    }

    public List<Task> getTasksByProjectId(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Получаем задачи для данного проекта
        return taskRepository.findByProjectId(projectId);
    }

    public TaskDTO convertToDTO(Task task) {
        Long developerId = task.getDeveloper() != null ? task.getDeveloper().getId() : null;
        Long testerId = task.getTester() != null ? task.getTester().getId() : null;
        return new TaskDTO(
                task.getId(),
                task.getProject().getId(),
                task.getName(),
                task.getDescription(),
                task.getStatus().toString(),
                task.getPriority().toString(),
                task.getDeadline(),
                task.getEndTime(),
                task.getCheckTime(),
                developerId,
                testerId
        );
    }

    public Task convertToTask(TaskDTO taskDTO) {
        Task task = new Task();
        task.setId(taskDTO.getId());
        task.setName(taskDTO.getName());
        task.setDescription(taskDTO.getDescription());

        // Преобразуем строку status и priority в перечисления
        task.setStatus(Task.Status.valueOf(taskDTO.getStatus()));
        task.setPriority(Task.Priority.valueOf(taskDTO.getPriority()));

        task.setDeadline(taskDTO.getDeadline());
        task.setEndTime(taskDTO.getEndTime());
        task.setCheckTime(taskDTO.getCheckTime());

        if (taskDTO.getProjectId() != null) {
            Optional<Project> project = projectRepository.findProjectById(taskDTO.getProjectId());
            task.setProject(project.get());
        } else {
            task.setProject(null);
            System.out.println("Проект не найден");
        }

        if (taskDTO.getDeveloperId() != null) {
            Optional<User> developerOptional = userRepository.findUserById(taskDTO.getDeveloperId());
            task.setDeveloper(developerOptional.get());
        } else {
            task.setDeveloper(null);
            System.out.println("Разработчик не найден");
        }

        if (taskDTO.getTesterId() != null) {
            Optional<User> testerOptional = userRepository.findUserById(taskDTO.getTesterId());
            task.setTester(testerOptional.get());
        } else {
            task.setTester(null);
            System.out.println("Тестировщик не найден");
        }

        return task;
    }


    public TaskDTO getTaskById(Long taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Task not found"));
        return convertToDTO(task);
    }

    public void moveTasksToBacklog(Long userId) {
        // Проверяем, существует ли пользователь
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Логика для перемещения задач в BACKLOG
        if (user.getRole() == User.Role.TESTER) {
            // Если пользователь - тестировщик, перемещаем задачи в статус IN_PROGRESS и удаляем привязку к тестировщику
            List<Task> tasks = taskRepository.findByTester(user);
            for (Task task : tasks) {
                if (task.getStatus() == Task.Status.REVIEW) {
                    task.setStatus(Task.Status.IN_PROGRESS);
                    task.setTester(null); // Убираем тестировщика из задачи
                    taskRepository.save(task);
                }
            }
        } else if (user.getRole() == User.Role.DEVELOPER) {
            // Если пользователь - разработчик, перемещаем задачи в статус BACKLOG и удаляем привязку к разработчику
            List<Task> tasks = taskRepository.findByDeveloper(user);
            for (Task task : tasks) {
                if (task.getStatus() == Task.Status.IN_PROGRESS || task.getStatus() == Task.Status.REVIEW) {
                    task.setStatus(Task.Status.BACKLOG); // Перемещаем задачу в BACKLOG
                    task.setDeveloper(null); // Убираем разработчика из задачи
                    taskRepository.save(task);
                }
            }
        } else {
            // Для других ролей можно добавить свою логику, если нужно
            throw new IllegalArgumentException("Невозможно переместить задачи для этого типа пользователя");
        }
    }

    public boolean hasAssignedTasks(Long userId) {
        return taskRepository.existsByDeveloperId(userId);
    }

}