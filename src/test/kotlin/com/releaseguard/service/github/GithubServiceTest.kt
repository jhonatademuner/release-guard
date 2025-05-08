package com.releaseguard.service.github

import com.releaseguard.client.github.GithubClient
import com.releaseguard.domain.github.GithubPullRequest
import com.releaseguard.domain.github.SimplifiedGithubPullRequest
import com.releaseguard.utils.assembler.GithubPullRequestAssembler
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.ResponseEntity
import java.time.Instant

class GithubServiceTest {

    private lateinit var githubClient: GithubClient
    private lateinit var githubPullRequestAssembler: GithubPullRequestAssembler
    private lateinit var githubService: GithubService

    @BeforeEach
    fun setup() {
        githubClient = mockk()
        githubPullRequestAssembler = mockk()
        githubService = GithubService(githubClient, githubPullRequestAssembler)
    }

    @Test
    fun `should find pull request by valid URL`() {
        val url = "https://github.com/owner/repo/pull/123"
        val now = Instant.now()

        val githubPr = GithubPullRequest(
            url = url,
            number = 123,
            title = "Test PR",
            body = "Body of the PR",
            labels = listOf(
                GithubPullRequest.Label(name = "bug", color = "f29513", defaultLabel = false),
                GithubPullRequest.Label(name = "urgent", color = "e11d21", defaultLabel = true)
            ),
            head = GithubPullRequest.BranchInfo(label = "owner:feature-branch", ref = "feature-branch"),
            base = GithubPullRequest.BranchInfo(label = "owner:main", ref = "main"),
            createdAt = now,
            updatedAt = now
        )

        val simplifiedPr = SimplifiedGithubPullRequest(
            url = url,
            number = 123,
            title = "Test PR",
            body = "Body of the PR",
            labels = listOf("bug", "urgent"),
            branch = "feature-branch",
            createdAt = now,
            updatedAt = now
        )

        every {
            githubClient.get("repos/owner/repo/pulls/123", GithubPullRequest::class.java)
        } returns ResponseEntity.ok(githubPr)

        every {
            githubPullRequestAssembler.toSimplified(githubPr)
        } returns simplifiedPr

        val result = githubService.findPullRequest(url)

        assertEquals("Test PR", result.title)
        assertEquals("feature-branch", result.branch)
        assertTrue(result.labels.contains("urgent"))

        verify { githubClient.get("repos/owner/repo/pulls/123", GithubPullRequest::class.java) }
        verify { githubPullRequestAssembler.toSimplified(githubPr) }
    }


    @Test
    fun `should throw exception when URL is null`() {
        val exception = assertThrows(IllegalArgumentException::class.java) {
            githubService.findPullRequest(null)
        }
        assertEquals("PullRequestUrl must be provided.", exception.message)
    }

    @Test
    fun `should detect urgent pull request by label`() {
        val pr = SimplifiedGithubPullRequest(
            title = "Fix bug", body = "No urgent mention", labels = listOf("urgent")
        )
        assertTrue(githubService.isUrgentPullRequest(pr))
    }

    @Test
    fun `should detect urgent pull request by title`() {
        val pr = SimplifiedGithubPullRequest(
            title = "!Critical fix", body = "Something", labels = emptyList()
        )
        assertTrue(githubService.isUrgentPullRequest(pr))
    }

    @Test
    fun `should detect urgent pull request by body`() {
        val pr = SimplifiedGithubPullRequest(
            title = "Refactor", body = "Please check this !urgent", labels = emptyList()
        )
        assertTrue(githubService.isUrgentPullRequest(pr))
    }

    @Test
    fun `should not detect non-urgent pull request`() {
        val pr = SimplifiedGithubPullRequest(
            title = "Update docs", body = "Regular PR", labels = listOf("documentation")
        )
        assertFalse(githubService.isUrgentPullRequest(pr))
    }
}
