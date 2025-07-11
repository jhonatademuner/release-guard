package com.releaseguard.utils.assembler

import com.releaseguard.domain.github.dto.PullRequestDTO
import com.releaseguard.domain.github.dto.SimplifiedPullRequestDTO
import org.springframework.stereotype.Component

@Component
class GithubPullRequestAssembler {

    fun toSimplified(pullRequest: PullRequestDTO): SimplifiedPullRequestDTO {
        return SimplifiedPullRequestDTO(
            url = pullRequest.url,
            number = pullRequest.number,
            title = pullRequest.title,
            body = pullRequest.body ?: "",
            labels = toSimplifiedLabels(pullRequest.labels),
            branch = pullRequest.head.ref,
            repo = pullRequest.head.repo.name,
            targetBranch = pullRequest.base.ref,
            createdAt = pullRequest.createdAt,
            updatedAt = pullRequest.updatedAt
        )
    }

    private fun toSimplifiedLabels(labels: List<PullRequestDTO.Label>): List<String> {
        return labels.map { it.name }
    }

}