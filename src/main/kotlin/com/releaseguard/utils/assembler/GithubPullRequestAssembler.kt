package com.releaseguard.utils.assembler

import com.releaseguard.domain.github.GithubPullRequest
import com.releaseguard.domain.github.SimplifiedGithubPullRequest
import org.springframework.stereotype.Component

@Component
class GithubPullRequestAssembler {

    fun toSimplified(pullRequest: GithubPullRequest?): SimplifiedGithubPullRequest {
        val pullRequestData = pullRequest ?: return SimplifiedGithubPullRequest()

        return SimplifiedGithubPullRequest(
            url = pullRequestData.url,
            number = pullRequestData.number,
            title = pullRequestData.title,
            body = pullRequestData.body ?: "",
            labels = toSimplifiedLabels(pullRequestData.labels),
            branch = pullRequestData.head.ref,
            createdAt = pullRequestData.createdAt,
            updatedAt = pullRequestData.updatedAt
        )
    }

    private fun toSimplifiedLabels(labels: List<GithubPullRequest.Label>): List<String> {
        return labels.map { it.name }
    }

}