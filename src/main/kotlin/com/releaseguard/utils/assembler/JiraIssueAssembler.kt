package com.releaseguard.utils.assembler

import com.releaseguard.domain.jira.*
import com.releaseguard.domain.jira.dto.IssueDTO
import com.releaseguard.domain.jira.dto.SimplifiedIssueDTO
import com.releaseguard.domain.jira.dto.SimplifiedLinkedIssueDTO
import org.springframework.stereotype.Component

@Component
class JiraIssueAssembler {

    fun toSimplified(issue: IssueDTO, isUrgent: Boolean = false): SimplifiedIssueDTO {
        return SimplifiedIssueDTO(
            key = issue.key,
            summary = issue.fields.summary,
            status = SimplifiedIssueStatus.valueOf(normalizeIssueStatusCategoryName(issue.fields.status.statusCategory.name)),
            linkedIssues = issue.fields.issueLinks?.mapNotNull { toSimplifiedLinkedIssue(it) }?.toMutableList()
                ?: mutableListOf(),
            isUrgent = isUrgent
        )
    }

    private fun toSimplifiedLinkedIssue(issueLink: IssueDTO.IssueLink): SimplifiedLinkedIssueDTO? {
        val internalIssue = issueLink.inwardIssue ?: issueLink.outwardIssue ?: return null
        val direction = if (issueLink.inwardIssue != null) LinkedIssueDirection.INWARD else LinkedIssueDirection.OUTWARD

        return SimplifiedLinkedIssueDTO(
            key = internalIssue.key,
            type = issueLink.type.name.uppercase(),
            linkDirection = direction,
            status = SimplifiedIssueStatus.valueOf(normalizeIssueStatusCategoryName(internalIssue.fields.status.statusCategory.name))
        )
    }

    private fun normalizeIssueStatusCategoryName(name: String): String {
        return name.replace(" ", "_").uppercase()
    }
}

