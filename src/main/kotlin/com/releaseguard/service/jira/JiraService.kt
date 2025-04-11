package com.releaseguard.service.jira

import com.releaseguard.client.jira.JiraClient
import com.releaseguard.domain.jira.*
import com.releaseguard.utils.assembler.JiraIssueAssembler
import org.springframework.stereotype.Service

@Service
class JiraService(
    private val jiraClient: JiraClient,
    private val jiraIssueAssembler: JiraIssueAssembler
) {

    fun getIssue(key: String? = null, pullRequest: String? = null): SimplifiedJiraIssue {
        if (key.isNullOrBlank() && pullRequest.isNullOrBlank()) {
            throw IllegalArgumentException("Either key or pullRequest must be provided")
        }

        val isUrgent = key?.first() == '!'

        val response = jiraClient.get("/rest/api/3/issue/$key", JiraIssue::class.java)

        return jiraIssueAssembler.toSimplified(response.body, isUrgent)
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