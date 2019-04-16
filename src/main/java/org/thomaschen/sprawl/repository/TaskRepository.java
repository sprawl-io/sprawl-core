package org.thomaschen.sprawl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.thomaschen.sprawl.model.Task;

import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    public List<Task> findByIdAndUser(UUID id, String user);

    public List<Task> findByUser(String user);

    @Modifying
    @Query("update Task t set t.workedTime = ?1 where t.workedTime = ?2")
    public void updateTaskWorkedTimeById(Integer workedTime, UUID id);

}
