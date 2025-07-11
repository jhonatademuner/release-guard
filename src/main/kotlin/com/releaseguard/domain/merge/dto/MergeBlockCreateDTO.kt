package com.releaseguard.domain.merge.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class MergeBlockCreateDTO(
    val repo: String,
    val branch: String,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val startTime: LocalDateTime,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val endTime: LocalDateTime,

    val reason: String,
    val createdBy: String
)