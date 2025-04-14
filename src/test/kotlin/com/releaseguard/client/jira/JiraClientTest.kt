package com.releaseguard.client.jira

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import java.util.*
import kotlin.test.assertEquals

class JiraClientTest {

    private lateinit var restTemplate: RestTemplate
    private lateinit var server: MockRestServiceServer
    private lateinit var jiraClient: JiraClient

    private val instanceUrl = "https://your-instance.atlassian.net/rest/api/3"
    private val email = "user@example.com"
    private val apiToken = "fake-token"

    private val encodedAuth: String
        get() = Base64.getEncoder().encodeToString("$email:$apiToken".toByteArray())

    @BeforeEach
    fun setup() {
        restTemplate = RestTemplate()
        server = MockRestServiceServer.createServer(restTemplate)
        jiraClient = JiraClient(restTemplate, instanceUrl, email, apiToken)
    }

    @Test
    fun `should perform GET request`() {
        val expectedBody = """{"issue":"JRA-123"}"""
        server.expect(requestTo("$instanceUrl/issue/JRA-123"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Basic $encodedAuth"))
            .andRespond(withSuccess(expectedBody, MediaType.APPLICATION_JSON))

        val response = jiraClient.get(
            endpoint = "issue/JRA-123",
            responseType = Map::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("JRA-123", response.body?.get("issue"))
    }

    @Test
    fun `should perform POST request`() {
        val requestBody = mapOf("fields" to mapOf("summary" to "Test Issue"))
        val expectedResponse = """{"id": "10000", "key": "JRA-123"}"""

        server.expect(requestTo("$instanceUrl/issue"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Basic $encodedAuth"))
            .andExpect(content().json("""{"fields":{"summary":"Test Issue"}}"""))
            .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON))

        val response = jiraClient.post(
            endpoint = "issue",
            request = requestBody,
            responseType = Map::class.java
        )

        assertEquals("10000", response.body?.get("id"))
        assertEquals("JRA-123", response.body?.get("key"))
    }

    @Test
    fun `should perform PUT request`() {
        val requestBody = mapOf("fields" to mapOf("summary" to "Updated summary"))
        val expectedResponse = """{"updated": true}"""

        server.expect(requestTo("$instanceUrl/issue/JRA-123"))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(header("Authorization", "Basic $encodedAuth"))
            .andExpect(content().json("""{"fields":{"summary":"Updated summary"}}"""))
            .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON))

        val response = jiraClient.put(
            endpoint = "issue/JRA-123",
            request = requestBody,
            responseType = Map::class.java
        )

        assertEquals(true, response.body?.get("updated"))
    }

    @Test
    fun `should perform DELETE request`() {
        server.expect(requestTo("$instanceUrl/issue/JRA-123"))
            .andExpect(method(HttpMethod.DELETE))
            .andExpect(header("Authorization", "Basic $encodedAuth"))
            .andRespond(withSuccess())

        val response = jiraClient.delete(
            endpoint = "issue/JRA-123",
            responseType = Void::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
    }
}
