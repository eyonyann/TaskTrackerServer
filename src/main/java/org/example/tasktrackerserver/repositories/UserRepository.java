package org.example.tasktrackerserver.repositories;

import org.example.tasktrackerserver.models.Project;
import org.example.tasktrackerserver.models.Task;
import org.example.tasktrackerserver.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Найти пользователя по имени пользователя
    Optional<User> findByUsername(String username);

    Optional<User> findUserById(Long userId);

    List<User> findByProjectId(Long projectId);

    // Найти пользователей по роли
    List<User> findByRole(User.Role role);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN Task t ON t.tester = u " +
            "WHERE u.role = :role " +
            "GROUP BY u " +
            "ORDER BY COUNT(t) ASC")
    Optional<User> findTesterWithMinimumTasks(@Param("role") User.Role role);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN Task t ON t.tester = u " +
            "WHERE u.role = :role " +
            "GROUP BY u " +
            "ORDER BY COUNT(t) ASC")
    List<User> findTestersWithMinimumTasks(@Param("role") User.Role role);
    Optional<User> findFirstByRoleAndIdNot(User.Role role, Long excludedUserId);
    long countByRole(User.Role role);
}
