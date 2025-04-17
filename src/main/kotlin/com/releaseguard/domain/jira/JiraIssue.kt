package com.releaseguard.domain.jira

import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data

@Data
data class JiraIssue(
    val key: String,
    val fields: JiraIssueFields
)

@Data
data class JiraIssueFields(
    val summary: String,
    val status: JiraIssueStatus,
    @JsonProperty("issuelinks")
    val issueLinks: List<JiraIssueLink>? = null,
)

@Data
data class JiraIssueStatus(
    val statusCategory: JiraIssueStatusCategory,
)

@Data
data class JiraIssueStatusCategory(
    val name: String,
)

@Data
data class JiraIssueLink(
    val type : JiraIssueLinkType,
    val inwardIssue : JiraIssue? = null,
    val outwardIssue : JiraIssue? = null
)

@Data
data class JiraIssueLinkType(
    val name: String
)

@Data
data class JiraJqlResult(
    val issues: List<JiraIssue>
)
