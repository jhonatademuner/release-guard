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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

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
        // Reset state of the mock if needed before each test
    }

    @Test
    fun `should return OK and simplified issue when issue is found`() {
        // Arrange
        val issueKey = "JIRA-123"
        val simplifiedIssue = SimplifiedJiraIssue(
            key = issueKey,
            summary = "Test Issue",
            status = SimplifiedJiraIssueStatus.TO_DO
        )
        every { jiraService.getIssue(issueKey) } returns simplifiedIssue

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/jira/issue/$issueKey"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.key").value(issueKey))
            .andExpect(jsonPath("$.summary").value("Test Issue"))
            .andExpect(jsonPath("$.status").value(SimplifiedJiraIssueStatus.TO_DO.name))
    }

    @Test
    fun `should return NOT_FOUND when issue is not found`() {
        // Arrange
        val issueKey = "JIRA-123"
        every { jiraService.getIssue(issueKey) } throws ResourceNotFoundException("Issue with key $issueKey not found")

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/jira/issue/$issueKey"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Issue with key $issueKey not found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Not Found"))
            .andExpect(jsonPath("$.path").value("/api/jira/issue/$issueKey"))
    }

    @Test
    fun `should return OK and block status when issue is blocked`() {
        // Arrange
        val issueKey = "JIRA-123"
        val simplifiedIssue = SimplifiedJiraIssue(
            key = issueKey,
            summary = "Test Issue",
            status = SimplifiedJiraIssueStatus.TO_DO,
            linkedIssues = mutableListOf(
                SimplifiedJiraLinkedIssue(
                    type = "BLOCKS",
                    linkDirection = JiraLinkedIssueDirection.INWARD,
                    status = SimplifiedJiraIssueStatus.IN_PROGRESS
                )
            )
        )
        every { jiraService.getIssue(issueKey) } returns simplifiedIssue
        every { jiraService.checkIssueBlockStatus(simplifiedIssue) } returns false

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/jira/issue/$issueKey/block-status"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(false))
    }

    @Test
    fun `should return OK and block status when issue is not blocked`() {
        // Arrange
        val issueKey = "JIRA-123"
        val simplifiedIssue = SimplifiedJiraIssue(
            key = issueKey,
            summary = "Test Issue",
            status = SimplifiedJiraIssueStatus.TO_DO,
            linkedIssues = mutableListOf(
                SimplifiedJiraLinkedIssue(
                    type = "BLOCKS",
                    linkDirection = JiraLinkedIssueDirection.INWARD,
                    status = SimplifiedJiraIssueStatus.DONE
                )
            )
        )
        every { jiraService.getIssue(issueKey) } returns simplifiedIssue
        every { jiraService.checkIssueBlockStatus(simplifiedIssue) } returns true

        // Act and Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/jira/issue/$issueKey/block-status"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").value(true))
    }
}
