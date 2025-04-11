package com.releaseguard.controller.jira

import com.releaseguard.domain.jira.SimplifiedJiraIssue
import com.releaseguard.service.jira.JiraService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.*
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@RestController
@RequestMapping("/api/jira")
class JiraController (private val jiraService: JiraService) {

    @GetMapping("/issue")
    fun getSimplifiedIssue(
        @RequestParam(required = false) key: String? = null,
        @RequestParam(required = false) pullRequest: String? = null
    ): ResponseEntity<SimplifiedJiraIssue> {
        val simplifiedJiraIssue = jiraService.getIssue(key, pullRequest)
        return ResponseEntity(simplifiedJiraIssue, HttpStatus.OK)
    }

    @GetMapping("/issue/block-status")
    fun checkIssueBlockStatus(
        @RequestParam(required = false) key: String? = null,
        @RequestParam(required = false) pullRequest: String? = null
    ): ResponseEntity<Boolean> {
        val simplifiedJiraIssue = jiraService.getIssue(key, pullRequest)
        val isBlockingFree = jiraService.checkIssueBlockStatus(simplifiedJiraIssue)
        return ResponseEntity(isBlockingFree, HttpStatus.OK)
    }

}
