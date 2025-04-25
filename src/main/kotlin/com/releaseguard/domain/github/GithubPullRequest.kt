package com.releaseguard.domain.github

import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data
import java.time.Instant

@Data
data class GithubPullRequest(
    val url: String,
    val number: Int,
    val title: String,
    val body: String?,
    val labels: List<Label> = mutableListOf(),
    val head: Head,
    @JsonProperty("created_at")
    val createdAt: Instant,
    @JsonProperty("updated_at")
    val updatedAt: Instant,
) {
    data class Label(
        val name: String,
        val color: String,
        @JsonProperty("default")
        val defaultLabel: Boolean,
    )

    data class Head(
        val label: String,
        val ref: String,
    )
}


