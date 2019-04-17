package org.thomaschen.sprawl.model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
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
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

import java.util.List;
import java.util.TimeZone;
import java.util.UUID;


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
