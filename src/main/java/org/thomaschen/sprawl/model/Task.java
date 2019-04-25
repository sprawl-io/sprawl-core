package org.thomaschen.sprawl.model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.thomaschen.sprawl.exception.TaskInProgressException;
import org.thomaschen.sprawl.exception.TaskNotInProgressException;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"},
        allowGetters = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "taskId")
public class Task implements Serializable {

    /**
     * Unique identifier for task.
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    @ApiModelProperty(hidden = true)
    private UUID taskId;

    /**
     * board that owns the message
     */
    @ManyToOne
    @ApiModelProperty(hidden = true)
    @JsonIdentityReference(alwaysAsId = true)
    private User owner;

    /**
     * Title of the task.
     */
    @NotBlank
    private String title;

    /**
     * Body of the task.
     */
    @NotBlank
    private String body;

    /**
     * Creation Date/Time of the task.
     */
    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    @ApiModelProperty(hidden = true)
    private Calendar createdAt;

    /**
     * Last Modified Date/time
     */
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    @ApiModelProperty(hidden = true)
    private Calendar updatedAt;

    /**
     * The Last Date/Time work started at
     */
    @Temporal(TemporalType.TIMESTAMP)
    @ApiModelProperty(hidden = true)
    private Calendar lastWorkStartAt;

    /**
     * Expected duration required to complete task.
     */
    @NotNull(message = "The above field must not be omitted.")
    @Min(value = 0L, message = "The value must be positive")
    private Long expDuration;

    /**
     * Actual duration worked on task in seconds.
     */
    @NotNull(message = "The above field must not be omitted.")
    @Min(value = 0L, message = "The value must be positive")
    private Long workedTime;

    @Column(nullable = false)
    @ApiModelProperty(hidden = true)
    private Boolean isFinished = false;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> tags;

    /**
     * Default Constructor
     */
    public Task() {
    }

    /**
     * Constructor for Entry class.
     * @param title the title of the task
     * @param body the body of the task
     * @param duration the expected time to complete the task
     */
    public Task(final User owner, final String title, final String body, final long duration, final List<String> tags) {
        this.taskId = UUID.randomUUID();
        this.owner = owner;

        this.title = title;
        this.body = body;

        this.expDuration = duration;
        this.workedTime = 0L;

        this.createdAt = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.updatedAt = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.lastWorkStartAt = null;

        this.isFinished = false;
        this.tags = tags;
    }

    /**
     * Start Working on this Task.
     */
    public void start() {
        if (this.lastWorkStartAt == null && !this.getIsFinished()) {
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            this.setLastWorkStartAt(now);
        } else {
            throw new TaskInProgressException("Task", "taskId", this.getTaskId());
        }
    }

    /**
     * Stop Working on this Task.
     */
    public void stop() {

        if (this.lastWorkStartAt != null && !this.getIsFinished()) {
            Calendar current = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            this.addWorkedTime(getDurationInSeconds(this.getLastWorkStartAt(), current));
            this.setLastWorkStartAt(null);
        } else {
            throw new TaskNotInProgressException("Task", "taskId", this.getTaskId());
        }
    }

    /**
     * Finish Task
     */
    public void finish() {
        if (this.getLastWorkStartAt() != null) {
            this.stop();
        }
        this.isFinished = true;
    }

    /**
     * Utility method which calculates seconds between two Calendar objects.
     * @param start first time object
     * @param stop second time object
     */
    public static long getDurationInSeconds(Calendar start, Calendar stop) {
        return ChronoUnit.SECONDS.between(start.toInstant(), stop.toInstant());
    }

    /**
     * Adds provided duration in seconds to existing workedTime.
     * @param workedTime the additional time to be added
     */
    public void addWorkedTime(long workedTime) {
        this.workedTime += workedTime;
    }


    /**
     * Equals method
     * @param o the object to be tested
     * @return true if equal, false if not
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Task)) {
            return false;
        }
        return this.getTaskId() != null && this.getTaskId().equals(((Task) o).getTaskId());
    }


    public static int getTotalOver(List<Task> tasks) {
        int totalOver = 0;

        for (Task task : tasks) {
            if (task.getWorkedTime() > task.getExpDuration()) {
                totalOver++;
            }
        }

        return totalOver;
    }

    public static int getTotalUnder(List<Task> tasks) {
        int totalUnder = 0;

        for (Task task : tasks) {
            if (task.getWorkedTime() < task.getExpDuration()) {
                totalUnder++;
            }
        }

        return totalUnder;
    }

    public static double getAverageWorkedTime(List<Task> tasks) {
        double avgTime = 0.0;
        for (Task task : tasks) {
            avgTime += task.getWorkedTime();
        }

        return avgTime/tasks.size();
    }

    public static double getEstAccuracy(List<Task> tasks) {
        double estAccuracy = 0.0;
        for (Task task : tasks) {
            estAccuracy += (double) task.getWorkedTime()/ (double)task.getExpDuration();
        }

        return estAccuracy / tasks.size();
    }

    public static double getTodaysAccuracy(List<Task> tasks) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String today = sdf.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime());

        double estAccuracy = 0.0;
        int count = 0;
        for (Task task : tasks) {
            String date = sdf.format(task.getUpdatedAt().getTime());
            if (today.equals(date)) {
                estAccuracy += (double) task.getWorkedTime()/ (double)task.getExpDuration();
                count++;
            }
        }

        return estAccuracy / count;
    }

    public static double getTodaysWorkedTime(List<Task> tasks) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        String today = sdf.format(Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime());

        double totalWorkedTime = 0.0;
        for (Task task : tasks) {
            String date = sdf.format(task.getUpdatedAt().getTime());
            if (today.equals(date)) {
                totalWorkedTime += task.getWorkedTime();
            }
        }

        return totalWorkedTime;
    }

    public static ArrayNode getTimeSeriesOfTaskEstFactor(List<Task> tasks) {
        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();

        double runSumEstFactor = 0.0;
        int runTotal = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));


        for (Task task : tasks) {
            JsonNode taskDataPt = mapper.createObjectNode();

            double currEstFactor = (double) task.getWorkedTime() / (double) task.getExpDuration();
            runSumEstFactor = (runSumEstFactor * runTotal + currEstFactor) / (runTotal + 1);
            String date = sdf.format(task.getCreatedAt().getTime());

            ((ObjectNode) taskDataPt).put("name", date);
            ((ObjectNode) taskDataPt).put("value", runSumEstFactor);

            arrayNode.add(taskDataPt);

            runTotal++;
        }

        return arrayNode;
    }

    public static ArrayNode getTimeSeriesOfTaskCompletion(List<Task> tasks) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Map<String, Integer> totalMap = new TreeMap<>();
        for (Task task : tasks) {

            String date = sdf.format(task.getUpdatedAt().getTime());

            if (totalMap.containsKey(date)) {
                totalMap.put(date, totalMap.get(date) + 1);
            } else {
                totalMap.put(date, 1);
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        ArrayNode arrayNode = mapper.createArrayNode();
        for(Map.Entry<String, Integer> entry : totalMap.entrySet()) {
            ObjectNode taskDataPt = mapper.createObjectNode();
            taskDataPt.put("name", entry.getKey());
            taskDataPt.put("value", entry.getValue());
            arrayNode.add(taskDataPt);
        }

        return arrayNode;
    }

    public static String getAggregateStatistics(List<Task> tasks) {
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode stats = mapper.createObjectNode();

        stats.put("totalTasks", tasks.size());
        stats.put("totalOver", Task.getTotalOver(tasks));
        stats.put("totalUnder", Task.getTotalUnder(tasks));
        stats.put("avgWorkedTime",Task.getAverageWorkedTime(tasks));
        stats.put("avgEstFactor", Task.getEstAccuracy(tasks));
        stats.put("todaysEstFactor", Task.getTodaysAccuracy(tasks));
        stats.put("todaysWorkedTime", Task.getTodaysWorkedTime(tasks));

        ArrayNode taskEstPts = Task.getTimeSeriesOfTaskEstFactor(tasks);
        ArrayNode totalPts = Task.getTimeSeriesOfTaskCompletion(tasks);


        stats.putArray("taskEstPts");
        stats.set("taskEstPts", taskEstPts);

        stats.putArray("totalPts");
        stats.set("totalPts", totalPts);

        String statsStr = "";
        try {
            statsStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(stats);
        } catch (JsonProcessingException jpe) {
            System.err.println(jpe.toString());
        }

        return statsStr;
    }



    public UUID getTaskId() {
        return taskId;
    }

    public void setTaskId(UUID taskId) {
        this.taskId = taskId;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Calendar getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Calendar createdAt) {
        this.createdAt = createdAt;
    }

    public Calendar getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Calendar updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Calendar getLastWorkStartAt() {
        return lastWorkStartAt;
    }

    public void setLastWorkStartAt(Calendar lastWorkStartAt) {
        this.lastWorkStartAt = lastWorkStartAt;
    }

    public Long getExpDuration() {
        return expDuration;
    }

    public void setExpDuration(Long expDuration) {
        this.expDuration = expDuration;
    }

    public Long getWorkedTime() {
        return workedTime;
    }

    public void setWorkedTime(Long workedTime) {
        this.workedTime = workedTime;
    }

    public Boolean getIsFinished() {
        return isFinished;
    }

    public void setIsFinished(Boolean finished) {
        isFinished = finished;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }

    public void removeTag(String tag) {
        this.tags.remove(tag);
    }
}
