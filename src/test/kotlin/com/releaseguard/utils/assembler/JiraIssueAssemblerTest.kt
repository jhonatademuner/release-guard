package com.releaseguard.utils.assembler

import com.releaseguard.domain.jira.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JiraIssueAssemblerTest {

    private lateinit var assembler: JiraIssueAssembler

    @BeforeEach
    fun setUp() {
        assembler = JiraIssueAssembler()
    }

    @Test
    fun `toSimplified returns default instance when input is null`() {
        // Act
        val result = assembler.toSimplified(null)
        // Assert
        // Assuming that default constructor of SimplifiedJiraIssue sets key to null or empty.
        assertNotNull(result)
        assertTrue(result.key.isEmpty(), "Key should be empty on default instance")
        assertTrue(result.summary.isEmpty(), "Summary should be empty on default instance")
        assertTrue(result.linkedIssues.isEmpty(), "Linked issues list should be empty on default instance")
    }

    @Test
    fun `toSimplified maps fields correctly`() {
        // Arrange
        val statusCategory = JiraIssueStatusCategory("In Progress")
        val status = JiraIssueStatus(statusCategory = statusCategory)
        val fields = JiraIssueFields(
            summary = "Test Issue",
            status = status,
            issueLinks = mutableListOf() // no links for this test
        )
        val jiraIssue = JiraIssue(
            key = "JIRA-123",
            fields = fields
        )

        // Act
        val result = assembler.toSimplified(jiraIssue)

        // Assert
        assertEquals("JIRA-123", result.key)
        assertEquals("Test Issue", result.summary)
        // Expecting normalization: "In Progress" -> "IN_PROGRESS"
        assertEquals(SimplifiedJiraIssueStatus.valueOf("IN_PROGRESS"), result.status)
        assertTrue(result.linkedIssues.isEmpty(), "There should be no linked issues")
    }

    @Test
    fun `toSimplified maps linked issues correctly`() {
        // Arrange
        // Create a linked issue (inward)
        val linkedStatusCategory = JiraIssueStatusCategory("Done")
        val linkedStatus = JiraIssueStatus(statusCategory = linkedStatusCategory)
        val linkedFields = JiraIssueFields(
            summary = "Linked Issue",
            status = linkedStatus,
            issueLinks = null
        )
        val inwardIssue = JiraIssue(
            key = "JIRA-456",
            fields = linkedFields
        )
        val linkType = JiraIssueLinkType(name = "blocks")
        val jiraIssueLink = JiraIssueLink(
            type = linkType,
            inwardIssue = inwardIssue,
            outwardIssue = null
        )

        val mainStatusCategory = JiraIssueStatusCategory("In Progress")
        val mainStatus = JiraIssueStatus(statusCategory = mainStatusCategory)
        val mainFields = JiraIssueFields(
            summary = "Main Issue",
            status = mainStatus,
            issueLinks = mutableListOf(jiraIssueLink)
        )
        val jiraIssue = JiraIssue(
            key = "JIRA-123",
            fields = mainFields
        )

        // Act
        val result = assembler.toSimplified(jiraIssue)

        // Assert
        assertEquals("JIRA-123", result.key)
        assertEquals("Main Issue", result.summary)
        assertEquals(SimplifiedJiraIssueStatus.valueOf("IN_PROGRESS"), result.status)
        assertEquals(1, result.linkedIssues.size)

        val simplifiedLinkedIssue = result.linkedIssues.first()
        assertEquals("JIRA-456", simplifiedLinkedIssue.key)
        // The type should be converted to uppercase ("blocks" -> "BLOCKS")
        assertEquals("BLOCKS", simplifiedLinkedIssue.type)
        // Since we used inwardIssue, the direction should be INWARD.
        assertEquals(JiraLinkedIssueDirection.INWARD, simplifiedLinkedIssue.linkDirection)
        // Linked status: "Done" becomes "DONE"
        assertEquals(SimplifiedJiraIssueStatus.valueOf("DONE"), simplifiedLinkedIssue.status)
    }
}
