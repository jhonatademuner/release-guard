package com.releaseguard.controller.jira

import com.releaseguard.domain.jira.SimplifiedJiraIssue
import com.releaseguard.service.jira.JiraService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.*
import org.springframework.web.bind.annotation.RequestMapping

@RestController
@RequestMapping("/api/jira")
class JiraController (private val jiraService: JiraService) {

    @GetMapping("/issue/{issueKey}")
    fun getSimplifiedIssue(@PathVariable issueKey: String): ResponseEntity<SimplifiedJiraIssue> {
        val simplifiedJiraIssue = jiraService.getIssue(issueKey)
        return ResponseEntity(simplifiedJiraIssue, HttpStatus.OK)
    }

    @GetMapping("/issue/{issueKey}/block-status")
    fun checkIssueBlockStatus(@PathVariable issueKey: String): ResponseEntity<Boolean> {
        val simplifiedJiraIssue = jiraService.getIssue(issueKey)
        val isBlockingFree = jiraService.checkIssueBlockStatus(simplifiedJiraIssue)
        return ResponseEntity(isBlockingFree, HttpStatus.OK)
    }

}
