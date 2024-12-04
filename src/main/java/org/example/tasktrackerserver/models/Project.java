package org.example.tasktrackerserver.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "projects")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private LocalDateTime deadline;

    @OneToMany(mappedBy = "project")
    @JsonIgnore
    private List<User> users;

    @Override
    public String toString() {
        StringBuilder userDetails = new StringBuilder();
        if (users != null && !users.isEmpty()) {
            for (User user : users) {
                userDetails.append(user.toString()).append(", ");
            }
            userDetails.setLength(userDetails.length() - 2); // Remove last comma and space
        } else {
            userDetails.append("No users assigned.");
        }

        return "Project{id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", deadline=" + deadline +
                ", users=[" + userDetails.toString() + "]}";
    }
}

