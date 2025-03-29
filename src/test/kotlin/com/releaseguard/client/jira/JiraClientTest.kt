package com.releaseguard.client.jira

import com.releaseguard.domain.jira.JiraIssue
import com.releaseguard.domain.jira.JiraIssueFields
import com.releaseguard.domain.jira.JiraIssueStatus
import com.releaseguard.domain.jira.JiraIssueStatusCategory
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.util.*

class JiraClientTest {

    private lateinit var jiraClient: JiraClient
    private val restTemplate: RestTemplate = mockk()

    private val instanceUrl = "https://jira.example.com"
    private val email = "test@example.com"
    private val apiToken = "fakeToken"

    @BeforeEach
    fun setUp() {
        jiraClient = JiraClient(restTemplate, instanceUrl, email, apiToken)
    }

    @Test
    fun `should fetch Jira issue successfully`() {
        // Arrange
        val endpoint = "/rest/api/2/issue/JIRA-123"
        val mockIssue = JiraIssue(
            key = "JIRA-123",
            fields = JiraIssueFields(
                summary = "Test Issue",
                status = JiraIssueStatus(statusCategory = JiraIssueStatusCategory("To Do")),
                issueLinks = mutableListOf()
            )
        )
        every {
            restTemplate.exchange(
                any<String>(),
                eq(HttpMethod.GET),
                any(),
                eq(JiraIssue::class.java)
            )
        } returns ResponseEntity(mockIssue, HttpStatus.OK)

        // Act
        val response = jiraClient.get(endpoint, JiraIssue::class.java)

        // Assert
        assertEquals(HttpStatus.OK, response.statusCode)
        assertNotNull(response.body)
        assertEquals("JIRA-123", response.body?.key)
        assertEquals("Test Issue", response.body?.fields?.summary)
    }

    @Test
    fun `should return 404 when Jira issue is not found`() {
        // Arrange
        val issueKey = "JIRA-123"
        val endpoint = "/rest/api/3/issue/$issueKey"
        val url = "$instanceUrl$endpoint"

        every { restTemplate.exchange(url, HttpMethod.GET, any(), JiraIssue::class.java) } throws
                HttpClientErrorException.create(
                    HttpStatus.NOT_FOUND,
                    "Not Found",
                    HttpHeaders(),
                    byteArrayOf(),
                    null
                )

        // Act
        val response = jiraClient.get(endpoint, JiraIssue::class.java)

        // Assert
        assertThat(response.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    }


    @Test
    fun `should create correct authorization header`() {
        // Arrange
        val authHeader = jiraClient.javaClass.getDeclaredMethod("createHeaders")
        authHeader.isAccessible = true
        val headers = authHeader.invoke(jiraClient) as org.springframework.http.HttpHeaders

        // Act
        val expectedAuth = Base64.getEncoder().encodeToString("$email:$apiToken".toByteArray())

        // Assert
        assertTrue(headers.containsKey("Authorization"))
        assertEquals("Basic $expectedAuth", headers.getFirst("Authorization"))
    }
}
