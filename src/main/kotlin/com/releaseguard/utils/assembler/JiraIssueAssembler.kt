package com.releaseguard.utils.assembler

import com.releaseguard.domain.jira.*
import org.springframework.stereotype.Component

@Component
class JiraIssueAssembler {

    fun toSimplified(issue: JiraIssue?, isUrgent: Boolean = false): SimplifiedJiraIssue {
        val issueData = issue ?: return SimplifiedJiraIssue()

        return SimplifiedJiraIssue(
            key = issueData.key,
            summary = issueData.fields.summary,
            status = SimplifiedJiraIssueStatus.valueOf(normalizeIssueStatusCategoryName(issueData.fields.status.statusCategory.name)),
            linkedIssues = issueData.fields.issueLinks?.mapNotNull { toSimplifiedLinkedIssue(it) }?.toMutableList() ?: mutableListOf(),
            isUrgent = isUrgent
        )
    }

    private fun toSimplifiedLinkedIssue(issueLink: JiraIssueLink): SimplifiedJiraLinkedIssue? {
        val internalIssue = issueLink.inwardIssue ?: issueLink.outwardIssue ?: return null
        val direction = if (issueLink.inwardIssue != null) JiraLinkedIssueDirection.INWARD else JiraLinkedIssueDirection.OUTWARD

        return SimplifiedJiraLinkedIssue(
            key = internalIssue.key,
            type = issueLink.type.name.uppercase(),
            linkDirection = direction,
            status = SimplifiedJiraIssueStatus.valueOf(normalizeIssueStatusCategoryName(internalIssue.fields.status.statusCategory.name))
        )
    }

    private fun normalizeIssueStatusCategoryName(name: String): String {
        return name.replace(" ", "_").uppercase()
    }
}

