package com.releaseguard.service.jira

import com.releaseguard.client.jira.JiraClient
import com.releaseguard.domain.jira.*
import com.releaseguard.utils.assembler.JiraIssueAssembler
import com.releaseguard.utils.exception.ResourceNotFoundException
import org.springframework.stereotype.Service
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Service
class JiraService(
    private val jiraClient: JiraClient,
    private val jiraIssueAssembler: JiraIssueAssembler
) {

    fun findIssue(key: String? = null, pullRequest: String? = null): SimplifiedJiraIssue {
        return if (key.isNullOrBlank()) {
            findIssueByPullRequest(pullRequest!!)
        } else {
            findIssueByKey(key)
        }
    }

    private fun findIssueByKey(key: String) : SimplifiedJiraIssue {
        val isUrgent = key.first() == '!'
        val response = jiraClient.get("/rest/api/3/issue/$key", JiraIssue::class.java)
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

    fun checkIssueBlockStatus(simplifiedJiraIssue: SimplifiedJiraIssue): Boolean {
        if (simplifiedJiraIssue.key.first() == '!') return true
        var isBlockingFree = true
        for (issue in simplifiedJiraIssue.linkedIssues) {
            if (issue.type == "BLOCKS" && issue.linkDirection == JiraLinkedIssueDirection.INWARD && issue.status != SimplifiedJiraIssueStatus.DONE) {
                isBlockingFree = false
                break
            }
        }
        return isBlockingFree
    }
}