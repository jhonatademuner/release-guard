package com.releaseguard.domain.jira

data class SimplifiedJiraLinkedIssue(
    var key: String = "",
    var type: String = "",
    var status: SimplifiedJiraIssueStatus? = null,
    var linkDirection: JiraLinkedIssueDirection? = null,
)
