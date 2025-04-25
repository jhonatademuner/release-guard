package com.releaseguard.domain.github

import lombok.Data
import java.time.Instant

@Data
data class SimplifiedGithubPullRequest(
    var url: String = "",
    var number: Int = 0,
    var title: String = "",
    var body: String = "",
    var labels: List<String> = mutableListOf(),
    var branch: String = "",
    var createdAt: Instant? = null,
    var updatedAt: Instant? = null,
)