package com.releaseguard.service.jira

import com.releaseguard.client.jira.JiraClient
import com.releaseguard.domain.github.SimplifiedGithubPullRequest
import com.releaseguard.domain.jira.*
import com.releaseguard.service.github.GithubService
import com.releaseguard.utils.assembler.JiraIssueAssembler
import com.releaseguard.utils.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class JiraService(
    private val jiraClient: JiraClient,
    private val jiraIssueAssembler: JiraIssueAssembler,
    private val githubService: GithubService
) {

    fun findIssue(key: String? = null, pullRequestUrl: String? = null): SimplifiedJiraIssue {
        return if (!key.isNullOrBlank()) {
            findIssueByKey(key)
        } else if (!pullRequestUrl.isNullOrBlank()) {
            findIssueByPullRequest(pullRequestUrl)
        } else {
            throw IllegalArgumentException("Either key or pullRequestUrl must be provided.")
        }
    }

    private fun findIssueByKey(key: String) : SimplifiedJiraIssue {
        val isUrgent = key.first() == '!'
        var formattedKey = key
        if (isUrgent) {
            formattedKey = key.substring(1)
        }
        val response = jiraClient.get("/rest/api/3/issue/$formattedKey", JiraIssue::class.java)
        return jiraIssueAssembler.toSimplified(response.body, isUrgent)
    }

    private fun findIssueByPullRequest(pullRequest: String) : SimplifiedJiraIssue {
        val query = "'hidden-pr-url[URL Field]' = '$pullRequest'"
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val params = mapOf("jql" to encodedQuery)

        val response = jiraClient.get("/rest/api/3/search", JiraJqlResult::class.java, params)

        val body = requireNotNull(response.body) { "Jira response body was null." }

        if (body.issues.isEmpty()) {
            throw ResourceNotFoundException("No issue found for the provided pull request URL.")
        }

        return jiraIssueAssembler.toSimplified(body.issues.first())
    }

    fun checkIssueBlockStatus(simplifiedJiraIssue: SimplifiedJiraIssue, pullRequestUrl: String? = null): Boolean {
        if (simplifiedJiraIssue.isUrgent) return true

        if (!pullRequestUrl.isNullOrBlank()){
            val pullRequest = githubService.findPullRequest(pullRequestUrl)
            if(githubService.isUrgentPullRequest(pullRequest)) return true
        }

        for (issue in simplifiedJiraIssue.linkedIssues) {
            if (issue.type == "BLOCKS"
                && (issue.linkDirection == JiraLinkedIssueDirection.INWARD
                && issue.status != SimplifiedJiraIssueStatus.DONE)) {
                return false
            }
        }
        return true
    }
}