package com.releaseguard.service.merge

import com.releaseguard.domain.github.SimplifiedGithubPullRequest
import com.releaseguard.domain.jira.*
import com.releaseguard.service.github.GithubService
import com.releaseguard.service.schedule.BlockScheduleService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class MergeServiceTest {

    private lateinit var githubService: GithubService
    private lateinit var blockScheduleService: BlockScheduleService
    private lateinit var mergeService: MergeService

    @BeforeEach
    fun setup() {
        githubService = mockk()
        blockScheduleService = mockk()
        mergeService = MergeService(githubService, blockScheduleService)
    }

    @Test
    fun `checkMergeBlockStatus should return true if Jira issue is urgent`() {
        // Arrange
        val jiraIssue = SimplifiedJiraIssue(
            key = "JIRA-1",
            summary = "Urgent Issue",
            status = SimplifiedJiraIssueStatus.TO_DO,
            isUrgent = true
        )

        // Act
        val result = mergeService.checkMergeBlockStatus(simplifiedJiraIssue = jiraIssue)

        // Assert
        assertEquals(true, result)
    }

    @Test
    fun `checkMergeBlockStatus should return false if Jira issue has an inward BLOCKS link not done`() {
        // Arrange
        val jiraIssue = SimplifiedJiraIssue(
            key = "JIRA-2",
            summary = "Blocked Issue",
            status = SimplifiedJiraIssueStatus.TO_DO,
            linkedIssues = mutableListOf(
                SimplifiedJiraLinkedIssue(
                    key = "JIRA-3",
                    type = "BLOCKS",
                    linkDirection = JiraLinkedIssueDirection.INWARD,
                    status = SimplifiedJiraIssueStatus.IN_PROGRESS
                )
            )
        )

        // Act
        val result = mergeService.checkMergeBlockStatus(simplifiedJiraIssue = jiraIssue)

        // Assert
        assertEquals(false, result)
    }

    @Test
    fun `checkMergeBlockStatus should return true if all inward BLOCKS issues are done`() {
        // Arrange
        val jiraIssue = SimplifiedJiraIssue(
            key = "JIRA-4",
            summary = "Clean Issue",
            status = SimplifiedJiraIssueStatus.TO_DO,
            linkedIssues = mutableListOf(
                SimplifiedJiraLinkedIssue(
                    key = "JIRA-5",
                    type = "BLOCKS",
                    linkDirection = JiraLinkedIssueDirection.INWARD,
                    status = SimplifiedJiraIssueStatus.DONE
                )
            )
        )

        // Act
        val result = mergeService.checkMergeBlockStatus(simplifiedJiraIssue = jiraIssue)

        // Assert
        assertEquals(true, result)
    }

    @Test
    fun `checkMergeBlockStatus should return false if branch is blocked by schedule`() {
        // Arrange
        val pr = SimplifiedGithubPullRequest(
            url = "https://github.com/org/repo/pull/10",
            targetBranch = "main"
        )

        every { blockScheduleService.checkBranchBlockSchedule("main") } returns true
        every { githubService.isUrgentPullRequest(pr) } returns false

        // Act
        val result = mergeService.checkMergeBlockStatus(pullRequest = pr)

        // Assert
        assertEquals(true, result)
    }

    @Test
    fun `checkMergeBlockStatus should return true if PR is urgent`() {
        // Arrange
        val pr = SimplifiedGithubPullRequest(
            url = "https://github.com/org/repo/pull/11",
            targetBranch = "develop"
        )

        every { blockScheduleService.checkBranchBlockSchedule("develop") } returns false
        every { githubService.isUrgentPullRequest(pr) } returns true

        // Act
        val result = mergeService.checkMergeBlockStatus(pullRequest = pr)

        // Assert
        assertEquals(true, result)
    }

    @Test
    fun `checkMergeBlockStatus should return true if no block reason found`() {
        // Arrange
        val pr = SimplifiedGithubPullRequest(
            url = "https://github.com/org/repo/pull/12",
            targetBranch = "release"
        )

        every { blockScheduleService.checkBranchBlockSchedule("release") } returns false
        every { githubService.isUrgentPullRequest(pr) } returns false

        // Act
        val result = mergeService.checkMergeBlockStatus(pullRequest = pr)

        // Assert
        assertEquals(true, result)
    }
}
