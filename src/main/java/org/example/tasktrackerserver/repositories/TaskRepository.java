package org.example.tasktrackerserver.repositories;

import jakarta.transaction.Transactional;
import org.example.tasktrackerserver.models.Project;
import org.example.tasktrackerserver.models.Task;
import org.example.tasktrackerserver.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    // Найти задачи по пользователю (исполнителю)
    List<Task> findByDeveloper(User developer);
    List<Task> findByTester(User tester);

    // Найти задачи по проекту
    List<Task> findByProject(Project project);

    // Найти задачи с определенным приоритетом
    List<Task> findByPriority(Task.Priority priority);

    List<Task> findByProjectId(Long projectId);



    @Transactional
    @Modifying
    @Query("UPDATE Task t SET t.tester.id = :newTesterId WHERE t.tester.id = :oldTesterId")
    void updateTesterId(@Param("oldTesterId") Long oldTesterId, @Param("newTesterId") Long newTesterId);
    List<Task> findByTesterId(Long testerId);

    boolean existsByDeveloperId(Long userId);

    List<Task> findByDeveloperAndStatus(User developer, Task.Status status);

    List<Task> findByTesterAndStatus(User tester, Task.Status status);
}
