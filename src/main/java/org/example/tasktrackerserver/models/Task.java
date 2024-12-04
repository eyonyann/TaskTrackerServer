package org.example.tasktrackerserver.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter
@Setter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id", nullable = true)
    @JsonIgnore
    private User developer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tester_id", nullable = true)
    @JsonIgnore
    private User tester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column
    private LocalDateTime deadline;

    @Column
    private LocalDateTime endTime;

    @Column
    private LocalDateTime checkTime;

    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    public enum Status {
        BACKLOG, IN_PROGRESS, REVIEW, DONE
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", priority='" + priority + '\'' +
                ", deadline=" + deadline +
                ", endTime=" + endTime +
                ", checkTime=" + checkTime +
                ", developerId=" + (developer != null ? developer.getId() : "null") +
                ", testerId=" + (tester != null ? tester.getId() : "null") +
                '}';
    }

}
