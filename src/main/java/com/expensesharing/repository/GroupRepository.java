package com.expensesharing.repository;

import com.expensesharing.entity.Group;
import com.expensesharing.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    List<Group> findByCreatedBy(User user);

    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.user.id = :userId AND m.isActive = true")
    List<Group> findActiveGroupsByUserId(@Param("userId") UUID userId);
}