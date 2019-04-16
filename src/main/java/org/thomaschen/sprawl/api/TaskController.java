package org.thomaschen.sprawl.api;

import org.springframework.http.ResponseEntity;
import org.thomaschen.sprawl.exception.ResourceNotFoundException;
import org.thomaschen.sprawl.exception.TaskFinishedException;
import org.thomaschen.sprawl.exception.TaskInProgressException;
import org.thomaschen.sprawl.exception.TaskNotInProgressException;
import org.thomaschen.sprawl.model.Task;
import org.thomaschen.sprawl.repository.TaskRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/task")
public class TaskController {
    @Autowired
    TaskRepository taskRepository;

    // Get all Tasks
    @GetMapping("/")
    public List<Task> getAllTasks(Principal principal) {
        return taskRepository.findByUser(principal.getName());
    }

    // Create a Task
    @PostMapping("/")
    public Task createTask(@Valid @RequestBody Task task, Principal principal) {
        task.setUser(principal.getName());
        return taskRepository.save(task);
    }

    // Get a single Task
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable(value = "id") UUID taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
    }

    // Update a Task
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable(value = "id") UUID taskId,
                           @Valid @RequestBody Task taskDetails) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (task.getFinished()) {
            throw new TaskFinishedException("Task", "id", taskId);
        }

        task.setTitle(taskDetails.getTitle());
        task.setBody(taskDetails.getBody());
        task.setExpDuration(taskDetails.getExpDuration());
        task.setWorkedTime(taskDetails.getWorkedTime());

        Task updatedTask = taskRepository.save(task);
        return updatedTask;
    }

    @PostMapping("/{id}/start")
    public Task startTask(@PathVariable(value = "id") UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (task.getFinished()) {
            throw new TaskFinishedException("Task", "id", taskId);
        } else if (task.getLastWorkStartAt() != null) {
            throw new TaskInProgressException("Task", "id", taskId);
        } else {
            task.start();
            return taskRepository.save(task);
        }
    }

    @PostMapping("/{id}/stop")
    public Task stopTask(@PathVariable(value = "id") UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (task.getFinished()) {
            throw new TaskFinishedException("Task", "id", taskId);
        } else if (task.getLastWorkStartAt() == null) {
            throw new TaskNotInProgressException("Task", "id", taskId);
        } else {
            task.stop();
            return taskRepository.save(task);
        }
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<?> finishTask(@PathVariable(value = "id") UUID taskId, Principal principal) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (task.getFinished()) {
            throw new TaskFinishedException("Task", "id", taskId);
        } else {
            task.finish();

            // Remove task from user's tasks
            taskRepository.delete(task);
            return ResponseEntity.ok().build();

        }
    }

    // Delete a Task
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable(value = "id") UUID taskId, Principal principal) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        taskRepository.delete(task);

        return ResponseEntity.ok().build();
    }



}
