package com.releaseguard.controller.merge

import com.releaseguard.domain.github.SimplifiedGithubPullRequest
import com.releaseguard.domain.jira.SimplifiedJiraIssue
import com.releaseguard.service.github.GithubService
import com.releaseguard.service.jira.JiraService
import com.releaseguard.service.merge.MergeService
import com.releaseguard.utils.exception.GlobalExceptionHandler
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(MergeController::class)
@Import(MergeControllerTest.TestConfig::class, GlobalExceptionHandler::class)
class MergeControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean fun mergeService(): MergeService = mockk()
        @Bean fun jiraService(): JiraService = mockk()
        @Bean fun githubService(): GithubService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var mergeService: MergeService

    @Autowired
    private lateinit var jiraService: JiraService

    @Autowired
    private lateinit var githubService: GithubService

    @Test
    fun `checkMergeBlockStatus should return true when merge is not blocked`() {
        val issueKey = "JIRA-111"
        val prUrl = "http://github.com/org/repo/pull/1"

        val mockJiraIssue = mockk<SimplifiedJiraIssue>()
        val mockPullRequest = mockk<SimplifiedGithubPullRequest>()

        every { jiraService.findIssue(issueKey, prUrl) } returns mockJiraIssue
        every { githubService.findPullRequest(prUrl) } returns mockPullRequest
        every { mergeService.checkMergeBlockStatus(mockJiraIssue, mockPullRequest) } returns true

        mockMvc.perform(get("/api/merge/block-status")
            .param("issueKey", issueKey)
            .param("pullRequest", prUrl))
            .andExpect(status().isOk)
            .andExpect(content().string("true"))
    }

    @Test
    fun `checkMergeBlockStatus should return false when merge is blocked`() {
        val issueKey = "JIRA-222"
        val prUrl = "http://github.com/org/repo/pull/2"

        val mockJiraIssue = mockk<SimplifiedJiraIssue>()
        val mockPullRequest = mockk<SimplifiedGithubPullRequest>()

        every { jiraService.findIssue(issueKey, prUrl) } returns mockJiraIssue
        every { githubService.findPullRequest(prUrl) } returns mockPullRequest
        every { mergeService.checkMergeBlockStatus(mockJiraIssue, mockPullRequest) } returns false

        mockMvc.perform(get("/api/merge/block-status")
            .param("issueKey", issueKey)
            .param("pullRequest", prUrl))
            .andExpect(status().isOk)
            .andExpect(content().string("false"))
    }

    @Test
    fun `checkMergeBlockStatus should return BAD_REQUEST when no parameters are provided`() {
        mockMvc.perform(get("/api/merge/block-status"))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.message").value("Either issueKey or pullRequest must be provided."))
            .andExpect(jsonPath("$.path").value("/api/merge/block-status"))
    }
}
