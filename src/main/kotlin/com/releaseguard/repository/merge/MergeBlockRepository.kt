package com.releaseguard.repository.merge

import com.releaseguard.domain.merge.MergeBlock
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

interface MergeBlockRepository : JpaRepository<MergeBlock, UUID> {
    @Query("""
        SELECT b FROM MergeBlock b
        WHERE b.repo = :repo
          AND b.branch= :branch
          AND b.startTime <= :date
          AND b.endTime >= :date
    """
    )
    fun findActiveByRepoAndBranchAt(
        @Param("repo") repo: String,
        @Param("branch") branch: String,
        @Param("date") date: LocalDateTime
    ): List<MergeBlock>

}