package org.example.tasktrackerserver.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserStatisticsDTO {
    private Long userId;
    private String userName;
    private String role;
    private List<TaskCompletionDTO> completedTasks;
}

