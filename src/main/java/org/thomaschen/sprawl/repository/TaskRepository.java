package org.thomaschen.sprawl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.thomaschen.sprawl.model.Task;
import org.thomaschen.sprawl.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    public List<Task> findByTaskIdAndOwner(UUID id, User owner);
    public List<Task> findByOwner(User owner);
    public Optional<Task> findByTaskId(UUID taskId);
    public List<Task> findAllByOwnerAndTags(User owner, String tag);
    public List<Task> findAllByOwnerAndTagsAndIsFinishedFalseOrderByCreatedAtDesc(User owner, String tag);
    public List<Task> findByOwnerAndIsFinishedFalseOrderByCreatedAtDesc(User owner);
    public List<Task> findByOwnerAndIsFinishedTrueOrderByCreatedAtDesc(User owner);
    public List<Task> findByOwnerAndIsFinishedTrueOrderByUpdatedAtAsc(User owner);
    public List<Task> findAllByOwnerAndTagsContains(User owner, List<String> tags);

    @Modifying
    @Query("update Task t set t.workedTime = ?1 where t.workedTime = ?2")
    public void updateTaskWorkedTimeById(Integer workedTime, UUID id);

}
