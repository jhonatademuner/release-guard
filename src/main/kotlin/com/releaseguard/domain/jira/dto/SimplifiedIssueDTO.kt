package com.releaseguard.domain.jira.dto

import com.releaseguard.domain.jira.SimplifiedIssueStatus
import lombok.Data

@Data
data class SimplifiedIssueDTO(
    var key: String,
    var summary: String,
    var linkedIssues: List<SimplifiedLinkedIssueDTO> = mutableListOf(),
    var status: SimplifiedIssueStatus,
    var isUrgent: Boolean = false,
)
