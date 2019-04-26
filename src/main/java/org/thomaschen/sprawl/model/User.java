package org.thomaschen.sprawl.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.thomaschen.sprawl.security.Role;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@EnableScheduling
@JsonIgnoreProperties(value = {"createdAt", "updatedAt"},
        allowGetters = true)
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "userId")
public class User {

    /**
     * Unique identifier for a user.
     */
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    @ApiModelProperty(hidden = true)
    private UUID userId;

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
     * User's email
     */
    private String email;

    /**
     * User's username
     */
    @Column(unique = true)
    private String username;

    /**
     * User's password
     */
    private String password;

    /**
     * User's name
     */
    private String name;

    @ApiModelProperty(hidden = true)
    private Role role;

    /**
     * Hashmap of all messages currently on Board
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "owner", fetch = FetchType.EAGER)
    @MapKey(name = "taskId")
    private Map<UUID, Task> tasks;

    /**
     * No Param Constructor
     */
    public User() {
        this.userId = UUID.randomUUID();
        this.role = Role.USER;
    }

    /**
     * Username Only Constructor
     */
    public User(String username) {
        this.userId = UUID.randomUUID();
        this.username = username;
        this.role = Role.USER;

        this.createdAt = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.updatedAt = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Full Constructor for User's Data
     * @param email of the user
     * @param name of the user
     */
    public User(String email, String name, String username, Role role) {
        this.userId = UUID.randomUUID();
        this.email = email;
        this.name = name;
        this.username = username;
        this.role = role;

        this.createdAt = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.updatedAt = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        this.tasks = new HashMap<>();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void deleteTask(Task task) {
        this.tasks.remove(task.getTaskId());
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User that = (User) o;
        return Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
