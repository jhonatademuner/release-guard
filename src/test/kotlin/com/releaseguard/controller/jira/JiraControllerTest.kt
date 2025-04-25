package com.releaseguard.controller.jira

import com.releaseguard.domain.jira.JiraLinkedIssueDirection
import com.releaseguard.domain.jira.SimplifiedJiraIssue
import com.releaseguard.domain.jira.SimplifiedJiraIssueStatus
import com.releaseguard.domain.jira.SimplifiedJiraLinkedIssue
import com.releaseguard.service.jira.JiraService
import com.releaseguard.utils.exception.GlobalExceptionHandler
import com.releaseguard.utils.exception.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(JiraController::class)
@Import(JiraControllerTest.TestConfig::class, GlobalExceptionHandler::class)
class JiraControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun jiraService(): JiraService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var jiraService: JiraService

    @BeforeEach
    fun setUp() {
        // reset mocks if needed
    }

    @Test
    fun `findIssue by key should return OK and simplified issue`() {
        val issueKey = "JIRA-123"
        val simplifiedIssue = SimplifiedJiraIssue(
            key = issueKey,
            summary = "Test Issue",
            status = SimplifiedJiraIssueStatus.TO_DO
        )
        every { jiraService.findIssue(key = issueKey, pullRequestUrl = null) } returns simplifiedIssue

        mockMvc.perform(get("/api/jira/issue").param("key", issueKey))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value(issueKey))
            .andExpect(jsonPath("$.summary").value("Test Issue"))
            .andExpect(jsonPath("$.status").value(SimplifiedJiraIssueStatus.TO_DO.name))
    }

    @Test
    fun `findIssue by pullRequest should return OK and simplified issue`() {
        val prUrl = "http://github.com/org/repo/pull/1"
        val simplifiedIssue = SimplifiedJiraIssue(
            key = "PR-456",
            summary = "PR Issue",
            status = SimplifiedJiraIssueStatus.IN_PROGRESS
        )
        every { jiraService.findIssue(key = null, pullRequestUrl = prUrl) } returns simplifiedIssue

        mockMvc.perform(get("/api/jira/issue").param("pullRequest", prUrl))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value("PR-456"))
            .andExpect(jsonPath("$.summary").value("PR Issue"))
            .andExpect(jsonPath("$.status").value(SimplifiedJiraIssueStatus.IN_PROGRESS.name))
    }

    @Test
    fun `findIssue should return BAD_REQUEST when no parameters provided`() {
        mockMvc.perform(get("/api/jira/issue"))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `findIssue should return NOT_FOUND when issue not found`() {
        val issueKey = "JIRA-404"
        every { jiraService.findIssue(key = issueKey, pullRequestUrl = null) } throws ResourceNotFoundException("Issue with key $issueKey not found")

        mockMvc.perform(get("/api/jira/issue").param("key", issueKey))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Issue with key $issueKey not found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.path").value("/api/jira/issue"))
    }

    @Test
    fun `blockStatus by key should return OK and false when issue is blocked`() {
        val issueKey = "JIRA-123"
        val simplifiedIssue = SimplifiedJiraIssue(
            key = issueKey,
            summary = "Test",
            status = SimplifiedJiraIssueStatus.TO_DO,
            linkedIssues = mutableListOf(
                SimplifiedJiraLinkedIssue(
                    type = "BLOCKS",
                    linkDirection = JiraLinkedIssueDirection.INWARD,
                    status = SimplifiedJiraIssueStatus.IN_PROGRESS
                )
            )
        )
        every { jiraService.findIssue(key = issueKey, pullRequestUrl = null) } returns simplifiedIssue
        every { jiraService.checkIssueBlockStatus(simplifiedIssue) } returns false

        mockMvc.perform(get("/api/jira/issue/block-status").param("key", issueKey))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(false))
    }

    @Test
    fun `blockStatus by key should return OK and true when issue is not blocked`() {
        val issueKey = "JIRA-123"
        val simplifiedIssue = SimplifiedJiraIssue(
            key = issueKey,
            summary = "Test",
            status = SimplifiedJiraIssueStatus.TO_DO,
            linkedIssues = mutableListOf(
                SimplifiedJiraLinkedIssue(
                    type = "BLOCKS",
                    linkDirection = JiraLinkedIssueDirection.INWARD,
                    status = SimplifiedJiraIssueStatus.DONE
                )
            )
        )
        every { jiraService.findIssue(key = issueKey, pullRequestUrl = null) } returns simplifiedIssue
        every { jiraService.checkIssueBlockStatus(simplifiedIssue) } returns true

        mockMvc.perform(get("/api/jira/issue/block-status").param("key", issueKey))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(true))
    }

    @Test
    fun `blockStatus should return BAD_REQUEST when no parameters provided`() {
        mockMvc.perform(get("/api/jira/issue/block-status"))
            .andExpect(status().isBadRequest)
    }
}
