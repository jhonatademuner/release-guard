package com.releaseguard.client.github

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.*
import org.springframework.test.web.client.response.MockRestResponseCreators.*
import org.springframework.web.util.UriComponentsBuilder
import kotlin.test.assertEquals

class GitHubClientTest {

    private lateinit var restTemplate: RestTemplate
    private lateinit var server: MockRestServiceServer
    private lateinit var gitHubClient: GitHubClient

    private val apiUrl = "https://api.github.com"
    private val apiToken = "fake-token"

    @BeforeEach
    fun setup() {
        restTemplate = RestTemplate()
        server = MockRestServiceServer.createServer(restTemplate)
        gitHubClient = GitHubClient(restTemplate, apiUrl, apiToken)
    }

    @Test
    fun `should perform GET request`() {
        val expectedBody = """{"key":"value"}"""
        server.expect(requestTo("$apiUrl/repos/test/repo"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header("Authorization", "Bearer $apiToken"))
            .andRespond(withSuccess(expectedBody, MediaType.APPLICATION_JSON))

        val response = gitHubClient.get(
            endpoint = "repos/test/repo",
            responseType = Map::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals("value", response.body?.get("key"))
    }

    @Test
    fun `should perform POST request`() {
        val requestBody = mapOf("name" to "new-repo")
        val expectedResponse = """{"id": 1, "name": "new-repo"}"""

        server.expect(requestTo("$apiUrl/user/repos"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header("Authorization", "Bearer $apiToken"))
            .andExpect(content().json("""{"name":"new-repo"}"""))
            .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON))

        val response = gitHubClient.post(
            endpoint = "user/repos",
            request = requestBody,
            responseType = Map::class.java
        )

        assertEquals(1, response.body?.get("id"))
        assertEquals("new-repo", response.body?.get("name"))
    }

    @Test
    fun `should perform PUT request`() {
        val requestBody = mapOf("description" to "updated")
        val expectedResponse = """{"description": "updated"}"""

        server.expect(requestTo("$apiUrl/repos/test/repo"))
            .andExpect(method(HttpMethod.PUT))
            .andExpect(header("Authorization", "Bearer $apiToken"))
            .andExpect(content().json("""{"description":"updated"}"""))
            .andRespond(withSuccess(expectedResponse, MediaType.APPLICATION_JSON))

        val response = gitHubClient.put(
            endpoint = "repos/test/repo",
            request = requestBody,
            responseType = Map::class.java
        )

        assertEquals("updated", response.body?.get("description"))
    }

    @Test
    fun `should perform DELETE request`() {
        server.expect(requestTo("$apiUrl/repos/test/repo"))
            .andExpect(method(HttpMethod.DELETE))
            .andExpect(header("Authorization", "Bearer $apiToken"))
            .andRespond(withSuccess())

        val response = gitHubClient.delete(
            endpoint = "repos/test/repo",
            responseType = Void::class.java
        )

        assertEquals(HttpStatus.OK, response.statusCode)
    }
}
