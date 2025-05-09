package com.releaseguard.controller.github

import com.releaseguard.domain.github.SimplifiedGithubPullRequest
import com.releaseguard.service.github.GithubService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/github")
class GithubController (
    private val githubService: GithubService
) {

    @GetMapping("/pull-request")
    fun getSimplifiedPullRequest(
        @RequestParam(required = true) pullRequestUrl: String
    ): ResponseEntity<SimplifiedGithubPullRequest> {
        val simplifiedPullRequest = githubService.findPullRequest(pullRequestUrl)
        return ResponseEntity(simplifiedPullRequest, HttpStatus.OK)
    }

}