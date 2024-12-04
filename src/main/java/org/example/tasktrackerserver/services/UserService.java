package org.example.tasktrackerserver.services;

import org.example.tasktrackerserver.models.Project;
import org.example.tasktrackerserver.models.Task;
import org.example.tasktrackerserver.models.User;
import org.example.tasktrackerserver.repositories.ProjectRepository;
import org.example.tasktrackerserver.repositories.TaskRepository;
import org.example.tasktrackerserver.repositories.UserRepository;
import org.example.tasktrackerserver.security.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) throws NoSuchAlgorithmException {
        String salt = PasswordUtil.generateSalt();
        String hashedPassword = PasswordUtil.hashPassword(user.getPassword(), salt);
        user.setSalt(salt);
        user.setPassword(hashedPassword);
        return userRepository.save(user);
    }


    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findUserById(Long userId) {
        return userRepository.findUserById(userId);
    }

    public User updateUserProject(Long userId, String newProjectName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        // Проверяем текущий проект пользователя
        Project currentProject = user.getProject();

        if (currentProject != null && currentProject.getName().equals(newProjectName)) {
            throw new IllegalArgumentException("Пользователь уже находится в этом проекте.");
        }

        // Получаем новый проект из базы данных
        Project newProject = (Project) projectRepository.findByName(newProjectName)
                .orElseThrow(() -> new IllegalArgumentException("Проект не найден: " + newProjectName));

        // Обновляем проект у пользователя
        user.setProject(newProject);

        return userRepository.save(user);
    }


    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    public User updateUser(User user) throws NoSuchAlgorithmException {
        // Находим пользователя по ID
        Optional<User> existingUserOpt = userRepository.findUserById(user.getId());
        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("Пользователь не найден");
        }

        User existingUser = existingUserOpt.get();

        // Проверяем, нужно ли обновить пароль
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            // Генерируем новую соль и хешируем новый пароль
            String newSalt = PasswordUtil.generateSalt();
            String newHashedPassword = PasswordUtil.hashPassword(user.getPassword(), newSalt);
            existingUser.setSalt(newSalt);
            existingUser.setPassword(newHashedPassword);
        }

        // Обновляем остальные поля
        existingUser.setFullname(user.getFullname());
        existingUser.setUsername(user.getUsername());

        // Сохраняем обновленную информацию в репозиторий
        return userRepository.save(existingUser);
    }

    public User updateUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
        User.Role role = User.Role.valueOf(newRole);
        if (newRole.equals(User.Role.ADMIN.toString())) {
            user.setProject(null);
        }
        user.setRole(role);
        return userRepository.save(user);
    }


    public void deleteUserById(Long userId, Long newTesterId) {
        // Проверяем, существует ли пользователь
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));


        taskRepository.updateTesterId(userId, newTesterId);

        // Удаляем пользователя
        userRepository.delete(user);
    }

    // Найти другого тестера
    public Optional<User> findAnotherTester(Long excludedTesterId) {
        return userRepository.findFirstByRoleAndIdNot(User.Role.TESTER, excludedTesterId);
    }

    // Переназначить задачи
    public void reassignTasksFromTester(Long testerId, Long newTesterId) {
        List<Task> tasks = taskRepository.findByTesterId(testerId);
        for (Task task : tasks) {
            User newTester = findUserById(newTesterId).get();
            task.setTester(newTester);
        }
        taskRepository.saveAll(tasks);
    }

    // Посчитать администраторов
    public long countAdmins() {
        return userRepository.countByRole(User.Role.ADMIN);
    }

//    // Удалить разработчика из проектов
//    public void removeDeveloperFromProjects(Long developerId) {
//        List<Project> projects = projectRepository.findByDeveloperId(developerId);
//        for (Project project : projects) {
//            project.removeDeveloper(developerId);
//        }
//        projectRepository.saveAll(projects);
//    }

    // Удалить пользователя
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }


}

