package com.releaseguard.service.github

import com.releaseguard.client.github.GithubClient
import com.releaseguard.domain.github.GithubPullRequest
import com.releaseguard.domain.github.SimplifiedGithubPullRequest
import com.releaseguard.utils.assembler.GithubPullRequestAssembler
import org.springframework.stereotype.Service

@Service
class GithubService(
    private val githubClient: GithubClient,
    private val githubPullRequestAssembler: GithubPullRequestAssembler
) {

    fun findPullRequest(
        pullRequestUrl: String? = null
    ) : SimplifiedGithubPullRequest  {

        if (pullRequestUrl.isNullOrBlank()) {
            throw IllegalArgumentException("PullRequestUrl must be provided.")
        }
        return findPullRequestByUrl(pullRequestUrl)
    }

    private fun findPullRequestByUrl(
        pullRequestUrl: String
    ) : SimplifiedGithubPullRequest {

        val prInfo = getInfoFromUrl(pullRequestUrl)

        val response = githubClient.get(
            endpoint = "repos/${prInfo.first}/${prInfo.second}/pulls/${prInfo.third}",
            responseType = GithubPullRequest::class.java
        )
        return githubPullRequestAssembler.toSimplified(response.body)
    }

    fun isUrgentPullRequest(pullRequest: SimplifiedGithubPullRequest): Boolean {
        return (pullRequest.labels.contains("urgent")
                || pullRequest.title.first() == '!'
                || pullRequest.body.contains("!urgent", ignoreCase = true))
    }

    private fun getInfoFromUrl(pullRequestUrl: String): Triple<String, String, String> {
        val regex = Regex("https://github\\.com/([^/]+)/([^/]+)/pull/(\\d+)")
        val matchResult = regex.find(pullRequestUrl)
            ?: throw IllegalArgumentException("Invalid pull request URL format.")

        val (repoOwner, repoName, pullRequestNumber) = matchResult.destructured
        return Triple(repoOwner, repoName, pullRequestNumber)
    }

}