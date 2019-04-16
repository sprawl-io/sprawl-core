package org.thomaschen.sprawl.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

import java.util.TimeZone;
import java.util.UUID;


@Entity
@Table(name = "tasks")
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"},
        allowGetters = true)
public class Task implements Serializable {

    /**
     * Unique identifier for task.
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    /**
     * User who owns the task.
     */
    private String user;

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
    private Calendar createdAt;

    /**
     * Last Modified Date/time
     */
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @LastModifiedDate
    private Calendar updatedAt;

    /**
     * The Last Date/Time work started at
     */
    @Temporal(TemporalType.TIMESTAMP)
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
    private Boolean isFinished = false;

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
    public Task(final String title, final String body, final long duration) {
        this.id = UUID.randomUUID();

        this.title = title;
        this.body = body;

        this.expDuration = duration;
        this.workedTime = 0L;

        this.createdAt = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.updatedAt = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.lastWorkStartAt = null;

        this.isFinished = false;
    }

    /**
     * Accessor method for UUID of Entry.
     * @return the UUID of the task
     */
    public UUID getId() {
        return id;
    }

    /**
     * Accessor method for the Title of the task.
     * @return the Title of the task
     */
    public String getTitle() {
        return title;
    }

    /**
     * Mutator method for Title of the task.
     * Updates the lastModifiedTime field when called
     * @param title the new title of the task
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Accessor method for Body of the task.
     * @return the Body of the task
     */
    public String getBody() {
        return body;
    }

    /**
     * Mutator method for the Body of the task.
     * Updates the lastModifiedTime field when called
     * @param body the new body of the task
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Accessor method for the time the Entry was initially created.
     * @return the creation time of the Entry in the form of a LocalDateTime object
     */
    public Calendar getCreateTime() {
        return createdAt;
    }


    /**
     * Accessor method for the expected time of the Entry.
     * @return the expected time in the form of a Period object
     */
    public long getExpDuration() {
        return expDuration;
    }

    /**
     * Mutator method for expected duration of the Entry.
     * @param duration the expected duration
     */
    public void setExpDuration(long duration) {
        this.expDuration = duration;
    }

    /**
     * Accessor method for the worked time of the Entry.
     * Does not account for currently running time - only recorded stopwatch epochs
     * @return the non running worked time
     */
    public long getWorkedTime() {
        return workedTime;
    }

    /**
     * Mutator method for setting time worked.
     * @param workedTime the new total time worked
     */
    public void setWorkedTime(long workedTime) {
        this.workedTime = workedTime;
    }

    /**
     * Adds provided duration in seconds to existing workedTime.
     * @param workedTime the additional time to be added
     */
    public void addWorkedTime(long workedTime) {
        this.workedTime += workedTime;
    }

    /**
     * Get the last modification date of the task.
     * @return the Calendar date the task was last updated
     */
    public Calendar getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Set the last modification date of the task
     * @param updatedAt the time it was changed
     */
    public void setUpdatedAt(Calendar updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Get the time work last started
     * @return the time work last started
     */
    public Calendar getLastWorkStartAt() {
        return lastWorkStartAt;
    }

    /**
     * Set the time work last started
     * @param lastWorkStartAt the time work last started
     */
    public void setLastWorkStartAt(Calendar lastWorkStartAt) {
        this.lastWorkStartAt = lastWorkStartAt;
    }

    /**
     * Accessor method for user
     * @return the name of the user
     */
    public String getUser() {
        return user;
    }

    /**
     * Mutator method for user
     * @param user the new name of the user
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * Check whether the Task is finished
     * @return true if finished, false if not
     */
    public Boolean getFinished() {
        return isFinished;
    }

    /**
     * Start Working on this Task.
     */
    public void start() {
        if (this.lastWorkStartAt == null && !this.getFinished()) {
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            this.setLastWorkStartAt(now);
        } else {
            throw new TaskInProgressException("Task", "id", this.getId());
        }
    }

    /**
     * Stop Working on this Task.
     */
    public void stop() {

        if (this.lastWorkStartAt != null && !this.getFinished()) {
            Calendar current = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            this.addWorkedTime(getDurationInSeconds(this.getLastWorkStartAt(), current));
            this.setLastWorkStartAt(null);
        } else {
            throw new TaskNotInProgressException("Task", "id", this.getId());
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
     * Equals method
     * @param o the object to be tested
     * @return true if equal, false if not
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Task)) {
            return false;
        }
        return this.getId() != null && this.getId().equals(((Task) o).getId());
    }


}
