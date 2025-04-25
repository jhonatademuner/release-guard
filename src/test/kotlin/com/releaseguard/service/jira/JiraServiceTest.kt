package com.releaseguard.service.jira

import com.releaseguard.client.jira.JiraClient
import com.releaseguard.domain.jira.*
import com.releaseguard.service.github.GithubService
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
    private lateinit var githubService: GithubService
    private lateinit var jiraService: JiraService

    @BeforeEach
    fun setup() {
        jiraClient = mockk()
        jiraIssueAssembler = mockk()
        githubService = mockk()
        jiraService = JiraService(jiraClient, jiraIssueAssembler, githubService)
    }

    @Test
    fun `findIssue should throw ResourceNotFoundException if issue key exists but not found`() {
        // Arrange
        val issueKey = "JIRA-123"
        val responseEntity: ResponseEntity<JiraIssue> = ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        every { jiraClient.get("/rest/api/3/issue/$issueKey", JiraIssue::class.java) } returns responseEntity
        // Simulate assembler throwing when response.body is null
        every { jiraIssueAssembler.toSimplified(null, false) } throws ResourceNotFoundException("Issue with key $issueKey not found")

        // Act & Assert
        val exception = assertFailsWith<ResourceNotFoundException> {
            jiraService.findIssue(key = issueKey)
        }
        assertEquals("Issue with key $issueKey not found", exception.message)
    }

    @Test
    fun `findIssue should return SimplifiedJiraIssue when issue key exists`() {
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
        // isUrgent = false
        every { jiraIssueAssembler.toSimplified(jiraIssue, false) } returns simplifiedJiraIssue

        // Act
        val result = jiraService.findIssue(key = issueKey)

        // Assert
        assertEquals(simplifiedJiraIssue, result)
    }

    @Test
    fun `findIssue should return SimplifiedJiraIssue when searching by pullRequest`() {
        // Arrange
        val prUrl = "http://github.com/org/repo/pull/1"
        val jqlResult = JiraJqlResult(
            issues = listOf(
                JiraIssue(
                    key = "JIRA-456",
                    fields = JiraIssueFields(
                        summary = "PR-linked Issue",
                        status = JiraIssueStatus(statusCategory = JiraIssueStatusCategory("In Progress")),
                        issueLinks = mutableListOf()
                    )
                )
            )
        )
        val simplifiedFromJql = SimplifiedJiraIssue(
            key = "JIRA-456",
            summary = "PR-linked Issue",
            status = SimplifiedJiraIssueStatus.IN_PROGRESS
        )

        every {
            jiraClient.get(
                "/rest/api/3/search",
                JiraJqlResult::class.java,
                mapOf("jql" to java.net.URLEncoder.encode("'hidden-pr-url[URL Field]' = '$prUrl'", java.nio.charset.StandardCharsets.UTF_8.toString()))
            )
        } returns ResponseEntity.ok(jqlResult)
        every { jiraIssueAssembler.toSimplified(jqlResult.issues.first()) } returns simplifiedFromJql

        // Act
        val result = jiraService.findIssue(key = null, pullRequestUrl = prUrl)

        // Assert
        assertEquals(simplifiedFromJql, result)
    }

    @Test
    fun `findIssue should throw IllegalArgumentException if no issue found for pullRequest`() {
        // Arrange
        val prUrl = "http://github.com/org/repo/pull/2"
        val emptyJql = JiraJqlResult(issues = emptyList())
        every {
            jiraClient.get(
                "/rest/api/3/search",
                JiraJqlResult::class.java,
                mapOf("jql" to java.net.URLEncoder.encode("'hidden-pr-url[URL Field]' = '$prUrl'", java.nio.charset.StandardCharsets.UTF_8.toString()))
            )
        } returns ResponseEntity.ok(emptyJql)

        // Act & Assert
        val exception = assertFailsWith<ResourceNotFoundException> {
            jiraService.findIssue(key = null, pullRequestUrl = prUrl)
        }
        assertEquals("No issue found for the provided pull request.", exception.message)
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
