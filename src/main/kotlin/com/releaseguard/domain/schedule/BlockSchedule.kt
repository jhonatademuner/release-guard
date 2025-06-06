package com.releaseguard.domain.schedule

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "block_schedule")
@EntityListeners(AuditingEntityListener::class)
data class BlockSchedule(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    val id: UUID? = null,

    @Column(name = "branch_name", nullable = false)
    val branchName: String,

    @Column(name = "start_time", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val startTime: LocalDateTime,

    @Column(name = "end_time", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val endTime: LocalDateTime,

    @Column(name = "reason", nullable = false)
    val reason: String,

    @Column(name = "created_by", nullable = false)
    val createdBy: String,

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
