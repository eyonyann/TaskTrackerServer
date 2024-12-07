package org.example.tasktrackerserver.repositories;

import org.example.tasktrackerserver.models.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findById(Long projectId);
    Project save(Project project);
    List<Project> findAll();

    Optional<Project> findByName(String newProjectName);

    Optional<Project> findProjectById(Long projectId);
//    List<Project> findByDeveloperId(Long developerId);

}
