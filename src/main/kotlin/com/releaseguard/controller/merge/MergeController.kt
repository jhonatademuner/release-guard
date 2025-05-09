package com.releaseguard.controller.merge

import com.releaseguard.service.github.GithubService
import com.releaseguard.service.jira.JiraService
import com.releaseguard.service.merge.MergeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/merge")
class MergeController(
    private val mergeService: MergeService,
    private val jiraService: JiraService,
    private val githubService: GithubService
){

    @GetMapping("/block-status")
    fun checkMergeBlockStatus(
        @RequestParam(required = false) issueKey: String? = null,
        @RequestParam(required = false) pullRequest: String? = null
    ): ResponseEntity<Boolean> {
        if (issueKey.isNullOrBlank() && pullRequest.isNullOrBlank()) {
            throw IllegalArgumentException("Either issueKey or pullRequest must be provided.")
        }
        val simplifiedJiraIssue = jiraService.findIssue(issueKey, pullRequest)
        val simplifiedPullRequest = githubService.findPullRequest(pullRequest)
        val isBlockingFree = mergeService.checkMergeBlockStatus(simplifiedJiraIssue, simplifiedPullRequest)
        return ResponseEntity(isBlockingFree, HttpStatus.OK)
    }

}