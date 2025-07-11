package com.releaseguard.controller.merge

import com.releaseguard.domain.merge.MergeBlock
import com.releaseguard.domain.merge.dto.MergeBlockCreateDTO
import com.releaseguard.domain.merge.dto.MergeBlockDTO
import com.releaseguard.domain.merge.dto.MergeCheckDTO
import com.releaseguard.service.github.GithubService
import com.releaseguard.service.jira.JiraService
import com.releaseguard.service.merge.MergeService
import org.apache.coyote.Response
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api")
class MergeController(
    private val mergeService: MergeService,
    private val jiraService: JiraService,
    private val githubService: GithubService
){

    @PostMapping("/v1/merge-check")
    fun checkMerge(
        @RequestParam(required = true) pr: String
    ): ResponseEntity<MergeCheckDTO> {
        val simplifiedJiraIssue = jiraService.findIssue( pr)
        val simplifiedPullRequest = githubService.findPullRequest(pr)
        return ResponseEntity.status(HttpStatus.OK)
            .body(mergeService.checkMerge(simplifiedJiraIssue, simplifiedPullRequest))
    }

    @GetMapping("/v1/merge-block")
    fun findAll(
        @RequestParam(defaultValue = "0") page : Int,
        @RequestParam(defaultValue = "10") size : Int
    ): ResponseEntity<List<MergeBlockDTO>> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(mergeService.findAll(page, size))
    }

    @GetMapping("/v1/merge-block/{id}")
    fun findById(@PathVariable id: UUID): ResponseEntity<MergeBlockDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(mergeService.findById(id))
    }

    @PostMapping("/v1/merge-block")
    fun create(@RequestBody block: MergeBlockCreateDTO): ResponseEntity<MergeBlockDTO> {
        return ResponseEntity.status(HttpStatus.OK)
            .body(mergeService.create(block))
    }

    @DeleteMapping("/v1/merge-block/{id}")
    fun removeById(@PathVariable id: UUID): ResponseEntity<Void> {
        mergeService.delete(id)
        return  ResponseEntity.status(HttpStatus.OK).build()
    }

}