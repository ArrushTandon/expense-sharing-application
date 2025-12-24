package com.expensesharing.repository;

import com.expensesharing.entity.Expense;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    Page<Expense> findByGroupId(UUID groupId, Pageable pageable);

    @Query("SELECT e FROM Expense e WHERE e.group.id = :groupId AND e.createdAt >= :startDate")
    List<Expense> findByGroupIdAndCreatedAtAfter(
            @Param("groupId") UUID groupId,
            @Param("startDate") LocalDateTime startDate
    );

    @Query("SELECT e FROM Expense e JOIN e.splits s WHERE s.user.id = :userId")
    List<Expense> findByParticipantUserId(@Param("userId") UUID userId);
}