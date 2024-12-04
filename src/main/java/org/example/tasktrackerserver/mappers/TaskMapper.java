package org.example.tasktrackerserver.mappers;

import org.example.tasktrackerserver.dtos.TaskDTO;
import org.example.tasktrackerserver.models.Task;
import org.example.tasktrackerserver.models.User;
import org.example.tasktrackerserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    private final UserService userService;

    @Autowired
    public TaskMapper(UserService userService) {
        this.userService = userService;
    }

    public Task toTask(TaskDTO taskDTO) {
        Task task = new Task();
        task.setId(taskDTO.getId());
        task.setName(taskDTO.getName());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(Task.Status.valueOf(taskDTO.getStatus()));
        task.setPriority(Task.Priority.valueOf(taskDTO.getPriority()));
        task.setDeadline(taskDTO.getDeadline());
        task.setEndTime(taskDTO.getEndTime());

        if (taskDTO.getDeveloperId() != null) {
            userService.findUserById(taskDTO.getDeveloperId()).ifPresent(task::setDeveloper);
        }

        return task;
    }

}
