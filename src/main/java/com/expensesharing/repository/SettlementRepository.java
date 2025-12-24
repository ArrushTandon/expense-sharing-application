package com.expensesharing.repository;

import com.expensesharing.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, UUID> {
    List<Settlement> findByGroupId(UUID groupId);
    List<Settlement> findByFromUserId(UUID fromUserId);
    List<Settlement> findByToUserId(UUID toUserId);

    @Query("SELECT s FROM Settlement s WHERE s.group.id = :groupId AND s.settledAt >= :startDate")
    List<Settlement> findByGroupIdAndSettledAtAfter(
            @Param("groupId") UUID groupId,
            @Param("startDate") LocalDateTime startDate
    );
}