package com.releaseguard.domain.github.branch

import com.fasterxml.jackson.annotation.JsonFormat
import lombok.Data
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@Data
data class BlockSchedule(
    val id: UUID = UUID.randomUUID(),
    val branchName: String,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Sao_Paulo")
    val startTime: Date,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Sao_Paulo")
    val endTime: Date,

    val reason: String,
    val createdBy: String,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "America/Sao_Paulo")
    val createdAt: Date = Date.from(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toInstant())
)

