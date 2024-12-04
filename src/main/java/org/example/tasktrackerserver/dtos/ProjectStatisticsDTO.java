package org.example.tasktrackerserver.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectStatisticsDTO {
    private Long projectId;
    private String projectName;
    private List<UserStatisticsDTO> users;
}