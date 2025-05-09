package com.releaseguard.service.jira

import com.releaseguard.client.jira.JiraClient
import com.releaseguard.domain.jira.*
import com.releaseguard.service.github.GithubService
import com.releaseguard.utils.assembler.JiraIssueAssembler
import com.releaseguard.utils.exception.ResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class JiraService(
    private val jiraClient: JiraClient,
    private val jiraIssueAssembler: JiraIssueAssembler,
) {

    private val logger = KotlinLogging.logger {}

    fun findIssue(key: String? = null, pullRequestUrl: String? = null): SimplifiedJiraIssue? {
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
        logger.info { "[FindIssue] Fetching JIRA issue by Key | key=$formattedKey" }
        val response = jiraClient.get("/rest/api/3/issue/$formattedKey", JiraIssue::class.java)
        return jiraIssueAssembler.toSimplified(response.body, isUrgent)
    }

    private fun findIssueByPullRequest(pullRequest: String) : SimplifiedJiraIssue? {
        val query = "'hidden-pr-url[URL Field]' = '$pullRequest'"
        val encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString())
        val params = mapOf("jql" to encodedQuery)

        logger.info { "[FindIssue] Fetching JIRA issue by Pull Request | pullRequest=$pullRequest" }
        val response = jiraClient.get("/rest/api/3/search", JiraJqlResult::class.java, params)
        val body = requireNotNull(response.body) { "Jira response body was null." }

        if (body.issues.isEmpty()) throw ResourceNotFoundException("No issue found for the provided pull request.")

        return jiraIssueAssembler.toSimplified(body.issues.first())
    }
}