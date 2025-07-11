package com.releaseguard.service.github

import com.releaseguard.client.github.GithubClient
import com.releaseguard.domain.github.dto.PullRequestDTO
import com.releaseguard.domain.github.dto.SimplifiedPullRequestDTO
import com.releaseguard.utils.assembler.GithubPullRequestAssembler
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class GithubService(
    private val githubClient: GithubClient,
    private val githubPullRequestAssembler: GithubPullRequestAssembler
) {

    private val logger = KotlinLogging.logger {}

    fun findPullRequest(
        pullRequestUrl: String
    ) : SimplifiedPullRequestDTO {

        val prInfo = getInfoFromUrl(pullRequestUrl)

        logger.info { "[FindPullRequest] Fetching GitHub pull request by URL | pullRequestUrl=$pullRequestUrl" }
        val response = githubClient.get(
            endpoint = "repos/${prInfo.first}/${prInfo.second}/pulls/${prInfo.third}",
            responseType = PullRequestDTO::class.java
        )

        val pullRequest: PullRequestDTO = response.body
            ?: throw IllegalArgumentException("Failed to fetch pull request from GitHub: $pullRequestUrl")

        return githubPullRequestAssembler.toSimplified(pullRequest)
    }

    fun isUrgentPullRequest(pullRequest: SimplifiedPullRequestDTO): Boolean {
        val pullRequestUrl = pullRequest.url

        if (pullRequest.labels.contains("urgent")){
            logger.info { "[IsUrgentPullRequest] Pull request is marked as URGENT | pullRequest=$pullRequestUrl | reason=LABEL_FLAG" }
            return true
        }

        if (pullRequest.title.first() == '!') {
            logger.info { "[IsUrgentPullRequest] Pull request is marked as URGENT | pullRequest=$pullRequestUrl | reason=TITLE_FLAG" }
            return true
        }

        if (pullRequest.body.contains("!urgent", ignoreCase = true)) {
            logger.info { "[IsUrgentPullRequest] Pull request is marked as URGENT | pullRequest=$pullRequestUrl | reason=BODY_FLAG" }
            return true
        }

        return false
    }

    private fun getInfoFromUrl(pullRequestUrl: String): Triple<String, String, String> {
        val regex = Regex("https://github\\.com/([^/]+)/([^/]+)/pull/(\\d+)") // Example: https://github.com/owner/repo/pull/123

        val matchResult = regex.find(pullRequestUrl)
            ?: throw IllegalArgumentException("Invalid pull request URL format.")

        val (repoOwner, repoName, pullRequestNumber) = matchResult.destructured
        val result = Triple(repoOwner, repoName, pullRequestNumber)

        logger.debug { "[GetInfoFromUrl] Extracted info from URL | pullRequestUrl=$pullRequestUrl | repoOwner=${result.first} | repoName=${result.second} | pullRequestNumber=${result.third}" }

        return result
    }

}