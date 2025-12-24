package com.expensesharing.repository;

import com.expensesharing.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {
    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, UUID userId);
    List<GroupMember> findByGroupIdAndIsActive(UUID groupId, Boolean isActive);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.user.id = :userId AND gm.isActive = true")
    List<GroupMember> findActiveByUserId(@Param("userId") UUID userId);
}