package com.releaseguard.service.jira

import com.releaseguard.client.jira.JiraClient
import com.releaseguard.domain.jira.dto.IssueDTO
import com.releaseguard.domain.jira.dto.IssueSearchDTO
import com.releaseguard.domain.jira.dto.SimplifiedIssueDTO
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

    fun findIssue(pullRequestUrl: String? = null): SimplifiedIssueDTO? {
        return if (!pullRequestUrl.isNullOrBlank()) {
            findIssueByPullRequest(pullRequestUrl)
        } else {
            throw IllegalArgumentException("Either key or pullRequestUrl must be provided.")
        }
    }

    private fun findIssueByPullRequest(pullRequest: String): SimplifiedIssueDTO? {
        val commentQuery = "\"[RELEASE-GUARD] Pull Request: $pullRequest\""
        val jql = """comment ~ '$commentQuery'"""
        val encodedJql = URLEncoder.encode(jql, StandardCharsets.UTF_8.toString())

        val params = mapOf("jql" to encodedJql)

        logger.info { "[FindIssue] Fetching JIRA issue by Pull Request | pullRequest=$pullRequest" }
        val response = jiraClient.get("/rest/api/3/search", IssueSearchDTO::class.java, params)
        val body = requireNotNull(response.body) { "Jira response body was null." }

        if (body.issues.isEmpty()) throw ResourceNotFoundException("No issue found for the provided pull request.")

        return jiraIssueAssembler.toSimplified(body.issues.first())
    }


}