package com.releaseguard.domain.jira

import lombok.Data

@Data
data class SimplifiedJiraIssue(
    var key: String = "",
    var summary: String = "",
    var linkedIssues: MutableList<SimplifiedJiraLinkedIssue> = mutableListOf(),
    var status: SimplifiedJiraIssueStatus? = null,
)
