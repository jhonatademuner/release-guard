package com.releaseguard.controller.github

import com.releaseguard.domain.github.SimplifiedGithubPullRequest
import com.releaseguard.service.github.GithubService
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
import java.time.Instant

@WebMvcTest(GithubController::class)
@Import(GithubControllerTest.TestConfig::class, GlobalExceptionHandler::class)
class GithubControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun githubService(): GithubService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var githubService: GithubService

    @Test
    fun `findPullRequest should return OK and simplified PR`() {
        val url = "https://github.com/org/repo/pull/123"
        val now = Instant.now()

        val simplifiedPR = SimplifiedGithubPullRequest(
            url = url,
            number = 123,
            title = "Add login endpoint",
            body = "Closes #45",
            labels = listOf("backend", "feature"),
            branch = "feature/login",
            targetBranch = "main",
            createdAt = now,
            updatedAt = now
        )

        every { githubService.findPullRequest(url) } returns simplifiedPR

        mockMvc.perform(get("/api/github/pull-request").param("pullRequestUrl", url))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.url").value(url))
            .andExpect(jsonPath("$.number").value(123))
            .andExpect(jsonPath("$.title").value("Add login endpoint"))
            .andExpect(jsonPath("$.labels[0]").value("backend"))
            .andExpect(jsonPath("$.labels[1]").value("feature"))
    }

    @Test
    fun `findPullRequest should return BAD_REQUEST when URL is blank`() {
        val url = "  "
        every { githubService.findPullRequest(url) } throws IllegalArgumentException("PullRequestUrl must be provided.")

        mockMvc.perform(get("/api/github/pull-request").param("pullRequestUrl", url))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("PullRequestUrl must be provided."))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
            .andExpect(jsonPath("$.path").value("/api/github/pull-request"))
    }

    @Test
    fun `findPullRequest should return BAD_REQUEST when URL is malformed`() {
        val url = "not_a_url"
        every { githubService.findPullRequest(url) } throws IllegalArgumentException("Invalid pull request URL format.")

        mockMvc.perform(get("/api/github/pull-request").param("pullRequestUrl", url))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Invalid pull request URL format."))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Bad Request"))
    }
}
