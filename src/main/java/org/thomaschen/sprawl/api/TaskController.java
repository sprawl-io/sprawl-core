package org.thomaschen.sprawl.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.thomaschen.sprawl.exception.ResourceNotFoundException;
import org.thomaschen.sprawl.exception.TaskFinishedException;
import org.thomaschen.sprawl.exception.TaskInProgressException;
import org.thomaschen.sprawl.exception.TaskNotInProgressException;
import org.thomaschen.sprawl.model.Task;
import org.thomaschen.sprawl.model.User;
import org.thomaschen.sprawl.repository.TaskRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.thomaschen.sprawl.repository.UserRepository;

import javax.validation.Valid;
import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    UserRepository userRepository;

    // Retrieve current logged in user
    public User getUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails)principal).getUsername();
        } else {
            username =  principal.toString();
        }

        User target = userRepository.findByUsername(username)
                .orElseThrow( () -> new ResourceNotFoundException("User", "username", username));

        if (target == null) {
            throw new ResourceNotFoundException("User", "username", username);
        } else {
            return target;
        }
    }

    // Get all Tasks
    @GetMapping("/all")
    public List<Task> getGenuineAllTasks() {
        return taskRepository.findAll();
    }

    // Get all Tasks
    @GetMapping("/all/finished")
    public List<Task> getAllFinishedTasks() {
        return taskRepository.findByOwnerAndIsFinishedTrueOrderByCreatedAtDesc(this.getUser());
    }

    // Get all Tasks
    @GetMapping("/all/unfinished")
    public List<Task> getAllUnFinishedTasks() {
        return taskRepository.findByOwnerAndIsFinishedFalseOrderByCreatedAtDesc(this.getUser());
    }

    // Get all Tasks
    @GetMapping("/")
    public List<Task> getAllTasks(@RequestParam(value="tags", required=false) String tag) {
        if (tag == null) {
            return taskRepository.findByOwnerAndIsFinishedFalseOrderByCreatedAtDesc(this.getUser());
        } else {
            return taskRepository.findAllByOwnerAndTagsAndIsFinishedFalseOrderByCreatedAtDesc(this.getUser(), tag);
        }
    }

    // Create a Task
    @PostMapping("/")
    public Task createTask(@Valid @RequestBody Task task) {
        task.setOwner(this.getUser());
        return taskRepository.save(task);
    }

    // Get a single Task
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable(value = "id") UUID taskId) {
        return taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
    }

    // Update a Task
    @PutMapping("/{id}")
    public Task updateTask(@PathVariable(value = "id") UUID taskId,
                           @Valid @RequestBody Task taskDetails) {
        Task task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (task.getIsFinished()) {
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
        Task task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (task.getIsFinished()) {
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
        Task task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (task.getIsFinished()) {
            throw new TaskFinishedException("Task", "id", taskId);
        } else if (task.getLastWorkStartAt() == null) {
            throw new TaskNotInProgressException("Task", "id", taskId);
        } else {
            task.stop();
            return taskRepository.save(task);
        }
    }

    @PostMapping("/{id}/finish")
    public ResponseEntity<?> finishTask(@PathVariable(value = "id") UUID taskId) {
        Task task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        if (task.getIsFinished()) {
            throw new TaskFinishedException("Task", "id", taskId);
        } else {
            // Mark Task as Finished
            task.finish();

            taskRepository.save(task);
            return ResponseEntity.ok().build();

        }
    }

    // Delete a Task
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable(value = "id") UUID taskId) {
        Task task = taskRepository.findByTaskId(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));

        User user = userRepository.findByUsername(this.getUser().getUsername())
                .orElseThrow( () -> new ResourceNotFoundException("User", "username", this.getUser().getUsername()));

        user.deleteTask(task);
        userRepository.save(user);
        taskRepository.delete(task);

        return ResponseEntity.ok().build();
    }

     // Get Statistics
    @GetMapping("/stats")
    public String getStats() {
        User user = userRepository.findByUsername(this.getUser().getUsername())
                .orElseThrow( () -> new ResourceNotFoundException("User", "username", this.getUser().getUsername()));

        List<Task> tasks = taskRepository.findByOwnerAndIsFinishedTrueOrderByUpdatedAtAsc(user);

        return Task.getAggregateStatistics(tasks);
    }

    // Get Statistics
    @GetMapping("/stats/timeseries/estimation")
    public String getTimeSeriesEstimation() {
        User user = userRepository.findByUsername(this.getUser().getUsername())
                .orElseThrow( () -> new ResourceNotFoundException("User", "username", this.getUser().getUsername()));

        List<Task> tasks = taskRepository.findByOwnerAndIsFinishedTrueOrderByUpdatedAtAsc(user);

        return Task.getTimeSeriesEstimation(tasks);
    }


    // Get Statistics
    @GetMapping("/stats/timeseries/totaltasks")
    public String getTimeSeriesTotalTasks() {
        User user = userRepository.findByUsername(this.getUser().getUsername())
                .orElseThrow( () -> new ResourceNotFoundException("User", "username", this.getUser().getUsername()));

        List<Task> tasks = taskRepository.findByOwnerAndIsFinishedTrueOrderByUpdatedAtAsc(user);

        return Task.getTimeSeriesTaskCompletedTotals(tasks);
    }


    // Get Statistics
    @GetMapping("/stats/timeseries/estimation/tag")
    public String getTimeSeriesEstimationPerTag() {
        User user = userRepository.findByUsername(this.getUser().getUsername())
                .orElseThrow( () -> new ResourceNotFoundException("User", "username", this.getUser().getUsername()));

        List<Task> allTasks = taskRepository.findByOwnerAndIsFinishedTrueOrderByUpdatedAtAsc(user);

        return Task.getTimeSeriesEstimationByTag(allTasks);
    }




}
