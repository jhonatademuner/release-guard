package com.releaseguard.repository.schedule

import com.releaseguard.domain.schedule.BlockSchedule
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Date
import java.util.UUID

interface BlockScheduleRepository : JpaRepository<BlockSchedule, UUID> {
    @Query("""
        SELECT b FROM BlockSchedule b
        WHERE b.branchName = :branchName
          AND b.startTime <= :date
          AND b.endTime >= :date
    """
    )
    fun findActiveBlocksByBranchAndDate(
        @Param("branchName") branchName: String,
        @Param("date") date: Date
    ): List<BlockSchedule>

}