package com.releaseguard.utils.assembler

import com.releaseguard.domain.github.GithubPullRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class GithubPullRequestAssemblerTest {

    private lateinit var assembler: GithubPullRequestAssembler

    @BeforeEach
    fun setup() {
        assembler = GithubPullRequestAssembler()
    }

    @Test
    fun `should convert complete GithubPullRequest to Simplified`() {
        val now = Instant.now()
        val pr = GithubPullRequest(
            url = "https://github.com/owner/repo/pull/42",
            number = 42,
            title = "Fix bug",
            body = "This fixes a major bug",
            labels = listOf(GithubPullRequest.Label(name = "bug", color = "f29513", defaultLabel = false)),
            head = GithubPullRequest.BranchInfo(label = "owner:bugfix", ref = "bugfix"),
            base = GithubPullRequest.BranchInfo(label = "owner:main", ref = "main"),
            createdAt = now,
            updatedAt = now
        )

        val result = assembler.toSimplified(pr)

        assertEquals("Fix bug", result.title)
        assertEquals("This fixes a major bug", result.body)
        assertEquals("bugfix", result.branch)
        assertEquals(listOf("bug"), result.labels)
        assertEquals(now, result.createdAt)
        assertEquals(now, result.updatedAt)
    }

    @Test
    fun `should return empty SimplifiedGithubPullRequest when input is null`() {
        val result = assembler.toSimplified(null)

        assertNotNull(result)
        assertEquals("", result.title)
        assertEquals("", result.body)
        assertTrue(result.labels.isEmpty())
        assertEquals("", result.branch)
        assertEquals(0, result.number)
        assertNull(result.createdAt)
        assertNull(result.updatedAt)
    }

    @Test
    fun `should handle null body in GithubPullRequest`() {
        val now = Instant.now()
        val pr = GithubPullRequest(
            url = "https://github.com/owner/repo/pull/100",
            number = 100,
            title = "Test PR",
            body = null,
            labels = emptyList(),
            head = GithubPullRequest.BranchInfo(label = "owner:test", ref = "test"),
            base = GithubPullRequest.BranchInfo(label = "owner:main", ref = "main"),
            createdAt = now,
            updatedAt = now
        )

        val result = assembler.toSimplified(pr)

        assertEquals("", result.body)
    }
}
