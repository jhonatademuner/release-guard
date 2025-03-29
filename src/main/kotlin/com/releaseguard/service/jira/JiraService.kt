package com.releaseguard.service.jira

import com.releaseguard.client.jira.JiraClient
import com.releaseguard.domain.jira.*
import com.releaseguard.utils.assembler.JiraIssueAssembler
import com.releaseguard.utils.exception.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class JiraService(
    private val jiraClient: JiraClient,
    private val jiraIssueAssembler: JiraIssueAssembler
) {

    fun getIssue(issueKey: String): SimplifiedJiraIssue {
        if (issueKey.isBlank()) {
            throw IllegalArgumentException("Issue key cannot be empty")
        }
        val response = jiraClient.get("/rest/api/3/issue/$issueKey", JiraIssue::class.java)
        if (response.statusCode == HttpStatus.NOT_FOUND) {
            throw ResourceNotFoundException("Issue with key $issueKey not found")
        }
        return jiraIssueAssembler.toSimplified(response.body)
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