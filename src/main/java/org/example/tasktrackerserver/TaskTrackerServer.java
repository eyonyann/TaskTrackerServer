package org.example.tasktrackerserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(proxyBeanMethods = false)
public class TaskTrackerServer {
    public static void main(String[] args) {
        SpringApplication.run(TaskTrackerServer.class, args);
        System.out.println("Сервер запущен!");
    }
}
