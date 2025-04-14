package com.releaseguard.service.jira

import com.releaseguard.client.jira.JiraClient
import com.releaseguard.domain.jira.*
import com.releaseguard.utils.assembler.JiraIssueAssembler
import com.releaseguard.utils.exception.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class JiraServiceTest {

    private lateinit var jiraClient: JiraClient
    private lateinit var jiraIssueAssembler: JiraIssueAssembler
    private lateinit var jiraService: JiraService

    @BeforeEach
    fun setup() {
        jiraClient = mockk()
        jiraIssueAssembler = mockk()
        jiraService = JiraService(jiraClient, jiraIssueAssembler)
    }

    @Test
    fun `getIssue should throw IllegalArgumentException if both key and pullRequest are empty`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            jiraService.getIssue("")
        }
        assertEquals("Issue key must be provided", exception.message)
    }

    @Test
    fun `getIssue should throw ResourceNotFoundException if issue is not found`() {
        // Arrange
        val issueKey = "JIRA-123"
        val responseEntity: ResponseEntity<JiraIssue> = ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        every { jiraClient.get("/rest/api/3/issue/$issueKey", JiraIssue::class.java) } returns responseEntity
        // Assume that the assembler throws ResourceNotFoundException when the JiraIssue is null.
        every { jiraIssueAssembler.toSimplified(null, false) } throws ResourceNotFoundException("Issue with key $issueKey not found")

        // Act & Assert
        val exception = assertFailsWith<ResourceNotFoundException> {
            jiraService.getIssue(issueKey)
        }
        assertEquals("Issue with key $issueKey not found", exception.message)
    }

    @Test
    fun `getIssue should return SimplifiedJiraIssue when issue exists`() {
        // Arrange
        val issueKey = "JIRA-123"
        val jiraIssue = JiraIssue(
            key = issueKey,
            fields = JiraIssueFields(
                summary = "Test Issue",
                status = JiraIssueStatus(statusCategory = JiraIssueStatusCategory("To Do")),
                issueLinks = mutableListOf()
            )
        )
        val simplifiedJiraIssue = SimplifiedJiraIssue(
            key = issueKey,
            summary = "Test Issue",
            status = SimplifiedJiraIssueStatus.TO_DO
        )

        val responseEntity = ResponseEntity.status(HttpStatus.OK).body(jiraIssue)
        every { jiraClient.get("/rest/api/3/issue/$issueKey", JiraIssue::class.java) } returns responseEntity

        // In this case, isUrgent is false because key does not start with '!'
        every { jiraIssueAssembler.toSimplified(jiraIssue, false) } returns simplifiedJiraIssue

        // Act
        val result = jiraService.getIssue(issueKey)

        // Assert
        assertEquals(simplifiedJiraIssue, result)
    }

    @Test
    fun `checkIssueBlockStatus should return true if issue key starts with '!'`() {
        // Arrange
        val simplifiedJiraIssue = SimplifiedJiraIssue(
            key = "!BLOCKED",
            summary = "Blocked Issue",
            status = SimplifiedJiraIssueStatus.TO_DO,
            linkedIssues = mutableListOf()
        )

        // Act
        val result = jiraService.checkIssueBlockStatus(simplifiedJiraIssue)

        // Assert
        assertEquals(true, result)
    }

    @Test
    fun `checkIssueBlockStatus should return false if issue is blocked`() {
        // Arrange
        val simplifiedJiraIssue = SimplifiedJiraIssue(
            key = "JIRA-123",
            summary = "Test Issue",
            status = SimplifiedJiraIssueStatus.TO_DO,
            linkedIssues = mutableListOf(
                SimplifiedJiraLinkedIssue(
                    key = "JIRA-124",
                    type = "BLOCKS",
                    linkDirection = JiraLinkedIssueDirection.INWARD,
                    status = SimplifiedJiraIssueStatus.TO_DO
                )
            )
        )

        // Act
        val result = jiraService.checkIssueBlockStatus(simplifiedJiraIssue)

        // Assert
        assertEquals(false, result)
    }

    @Test
    fun `checkIssueBlockStatus should return true if issue is not blocked`() {
        // Arrange
        val simplifiedJiraIssue = SimplifiedJiraIssue(
            key = "JIRA-123",
            summary = "Test Issue",
            status = SimplifiedJiraIssueStatus.TO_DO,
            linkedIssues = mutableListOf(
                SimplifiedJiraLinkedIssue(
                    key = "JIRA-124",
                    type = "BLOCKS",
                    linkDirection = JiraLinkedIssueDirection.INWARD,
                    status = SimplifiedJiraIssueStatus.DONE
                )
            )
        )

        // Act
        val result = jiraService.checkIssueBlockStatus(simplifiedJiraIssue)

        // Assert
        assertEquals(true, result)
    }

    @Test
    fun `checkIssueBlockStatus should return true if no block link exists`() {
        // Arrange
        val simplifiedJiraIssue = SimplifiedJiraIssue(
            key = "JIRA-123",
            summary = "Test Issue",
            status = SimplifiedJiraIssueStatus.TO_DO,
            linkedIssues = mutableListOf()
        )

        // Act
        val result = jiraService.checkIssueBlockStatus(simplifiedJiraIssue)

        // Assert
        assertEquals(true, result)
    }
}
