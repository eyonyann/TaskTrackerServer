package org.example.tasktrackerserver.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String fullname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String salt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    public enum Role {
        ADMIN,
        DEVELOPER,
        TESTER
    }

    @Override
    public String toString() {
        return "User{id=" + id +
                ", username='" + username + '\'' +
                ", fullname='" + fullname + '\'' +
                ", role=" + role +
                ", project=" + (project != null ? project.getName() : "No Project") + '}';
    }
}
