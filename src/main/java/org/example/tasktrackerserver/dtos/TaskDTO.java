package org.example.tasktrackerserver.dtos;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private Long projectId;
    private String name;
    private String description;
    private String status;
    private String priority;
    private LocalDateTime deadline;
    private LocalDateTime endTime;
    private LocalDateTime checkTime;
    private Long developerId;
    private Long testerId;

    public TaskDTO(Long id, Long projectId,
                   String name,
                   String description,
                   String status,
                   String priority,
                   LocalDateTime deadline,
                   LocalDateTime endTime,
                   LocalDateTime checkTime,
                   Long developerId,
                   Long testerId) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.status = status;
        this.priority = priority;
        this.deadline = deadline;
        this.endTime = endTime;
        this.checkTime = checkTime;
        this.developerId = developerId;
        this.testerId = testerId;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", projectId='" + projectId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", deadline=" + deadline +
                ", endTime=" + endTime +
                ", checkTime=" + checkTime +
                ", developerId=" + developerId +
                ", testerId=" + testerId +
                '}';
    }
}
