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

    fun getIssue(key: String): SimplifiedJiraIssue {
        if (key.isBlank() ) {
            throw IllegalArgumentException("Issue key must be provided")
        }

        val isUrgent = key.first() == '!'
        val formattedKey = if (isUrgent) key.substring(1) else key

        val response = jiraClient.get("/rest/api/3/issue/$formattedKey", JiraIssue::class.java)

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