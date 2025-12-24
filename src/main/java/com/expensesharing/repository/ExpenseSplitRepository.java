package com.expensesharing.repository;

import com.expensesharing.entity.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, UUID> {
    List<ExpenseSplit> findByUserId(UUID userId);
    List<ExpenseSplit> findByExpenseId(UUID expenseId);

    @Query("SELECT es FROM ExpenseSplit es WHERE es.expense.group.id = :groupId")
    List<ExpenseSplit> findByExpenseGroupId(@Param("groupId") UUID groupId);

    @Query("SELECT es FROM ExpenseSplit es WHERE es.user.id = :userId AND es.paid = false")
    List<ExpenseSplit> findUnpaidByUserId(@Param("userId") UUID userId);
}