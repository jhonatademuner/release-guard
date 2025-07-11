package com.releaseguard.domain.jira.dto

import com.releaseguard.domain.jira.LinkedIssueDirection
import com.releaseguard.domain.jira.SimplifiedIssueStatus

data class SimplifiedLinkedIssueDTO(
    var key: String,
    var type: String,
    var status: SimplifiedIssueStatus,
    var linkDirection: LinkedIssueDirection,
)
