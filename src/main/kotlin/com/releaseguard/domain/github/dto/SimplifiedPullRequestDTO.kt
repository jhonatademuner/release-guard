package com.releaseguard.domain.github.dto

import lombok.Data
import java.time.Instant

@Data
data class SimplifiedPullRequestDTO(
    var url: String,
    var number: Int,
    var title: String,
    var body: String,
    var labels: List<String> = mutableListOf(),
    var branch: String,
    var repo: String,
    var targetBranch: String,
    var createdAt: Instant,
    var updatedAt: Instant,
)