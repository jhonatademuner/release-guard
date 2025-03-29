package com.releaseguard.service.jira

import com.releaseguard.client.jira.JiraClient
import com.releaseguard.domain.jira.*
import com.releaseguard.utils.assembler.JiraIssueAssembler
import com.releaseguard.utils.exception.ResourceNotFoundException
import io.mockk.*
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
    fun `getIssue should throw IllegalArgumentException if issue key is empty`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            jiraService.getIssue("")
        }
        assertEquals("Issue key cannot be empty", exception.message)
    }

    @Test
    fun `getIssue should throw ResourceNotFoundException if issue is not found`() {
        val issueKey = "JIRA-123"
        every { jiraClient.get("/rest/api/3/issue/$issueKey", JiraIssue::class.java) } returns
                ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        val exception = assertFailsWith<ResourceNotFoundException> {
            jiraService.getIssue(issueKey)
        }
        assertEquals("Issue with key $issueKey not found", exception.message)
    }

    @Test
    fun `getIssue should return SimplifiedJiraIssue when issue exists`() {
        val issueKey = "JIRA-123"
        val jiraIssue = JiraIssue(
            key = issueKey,
            fields = JiraIssueFields(
                summary = "Test Issue",
                status = JiraIssueStatus(statusCategory = JiraIssueStatusCategory("To Do")),
                issueLinks = mutableListOf()
            )
        )

        val simplifiedJiraIssue =
            SimplifiedJiraIssue(key = issueKey, summary = "Test Issue", status = SimplifiedJiraIssueStatus.TO_DO)

        every { jiraClient.get("/rest/api/3/issue/$issueKey", JiraIssue::class.java) } returns
                ResponseEntity.status(HttpStatus.OK).body(jiraIssue)
        every { jiraIssueAssembler.toSimplified(any()) } returns simplifiedJiraIssue

        val result = jiraService.getIssue(issueKey)

        assertEquals(simplifiedJiraIssue, result)
    }

    @Test
    fun `checkIssueBlockStatus should return true if issue key starts with '!'`() {
        val simplifiedJiraIssue = SimplifiedJiraIssue(key = "!BLOCKED", linkedIssues = mutableListOf())
        val result = jiraService.checkIssueBlockStatus(simplifiedJiraIssue)
        assertEquals(true, result)
    }

    @Test
    fun `checkIssueBlockStatus should return false if issue is blocked`() {
        val simplifiedJiraIssue = SimplifiedJiraIssue(
            key = "JIRA-123",
            linkedIssues = mutableListOf(
                SimplifiedJiraLinkedIssue(
                    type = "BLOCKS",
                    linkDirection = JiraLinkedIssueDirection.INWARD,
                    status = SimplifiedJiraIssueStatus.TO_DO,
                    key = "JIRA-124"
                )
            )
        )
        val result = jiraService.checkIssueBlockStatus(simplifiedJiraIssue)
        assertEquals(false, result)
    }

    @Test
    fun `checkIssueBlockStatus should return true if issue is not blocked`() {
        val simplifiedJiraIssue = SimplifiedJiraIssue(
            key = "JIRA-123",
            linkedIssues = mutableListOf(
                SimplifiedJiraLinkedIssue(
                    type = "BLOCKS",
                    linkDirection = JiraLinkedIssueDirection.INWARD,
                    status = SimplifiedJiraIssueStatus.DONE,
                    key = "JIRA-124"
                )
            )
        )
        val result = jiraService.checkIssueBlockStatus(simplifiedJiraIssue)
        assertEquals(true, result)
    }

    @Test
    fun `checkIssueBlockStatus should return true if no block link exists`() {
        val simplifiedJiraIssue = SimplifiedJiraIssue(key = "JIRA-123", linkedIssues = mutableListOf())
        val result = jiraService.checkIssueBlockStatus(simplifiedJiraIssue)
        assertEquals(true, result)
    }
}
