package com.releaseguard.domain.jira.dto

import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Data

@Data
data class IssueDTO(
    val key: String,
    val fields: IssueFields
){

    data class IssueFields(
        val summary: String,
        val status: IssueStatus,
        @JsonProperty("issuelinks")
        val issueLinks: List<IssueLink>? = null,
    )

    data class IssueStatus(
        val statusCategory: IssueStatusCategory,
    )

    data class IssueStatusCategory(
        val name: String,
    )

    data class IssueLink(
        val type : IssueLinkType,
        val inwardIssue : IssueDTO? = null,
        val outwardIssue : IssueDTO? = null
    )

    data class IssueLinkType(
        val name: String
    )

}
