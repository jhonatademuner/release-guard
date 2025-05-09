package com.releaseguard.service.merge

import com.releaseguard.domain.github.SimplifiedGithubPullRequest
import com.releaseguard.domain.jira.JiraLinkedIssueDirection
import com.releaseguard.domain.jira.SimplifiedJiraIssue
import com.releaseguard.domain.jira.SimplifiedJiraIssueStatus
import com.releaseguard.service.github.GithubService
import com.releaseguard.service.github.branch.BlockScheduleService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class MergeService(
    private val githubService: GithubService,
    private val blockScheduleService: BlockScheduleService
) {

    private val logger = KotlinLogging.logger {}

    fun checkMergeBlockStatus(simplifiedJiraIssue: SimplifiedJiraIssue? = null, pullRequest: SimplifiedGithubPullRequest? = null): Boolean {

        if (simplifiedJiraIssue != null) {
            val issueKey = simplifiedJiraIssue.key

            if (simplifiedJiraIssue.isUrgent) {
                logger.info { "[CheckMergeBlockStatus] Issue is marked as URGENT | issueKey=$issueKey | reason=ISSUE_KEY_FLAG" }
                return true
            }

            for (issue in simplifiedJiraIssue.linkedIssues) {
                val isBlocking = issue.type == "BLOCKS" &&
                        issue.linkDirection == JiraLinkedIssueDirection.INWARD &&
                        issue.status != SimplifiedJiraIssueStatus.DONE

                if (isBlocking) {
                    logger.info { "[CheckMergeBlockStatus] Issue is BLOCKED | issueKey=$issueKey | blockingIssue=${issue.key} | blockingStatus=${issue.status}" }
                    return false
                }
            }
        }

        if (pullRequest != null){
            if(blockScheduleService.checkBranchBlockSchedule(pullRequest.targetBranch)) return true
            if(githubService.isUrgentPullRequest(pullRequest)) return true
        }

        val issueLog = if (simplifiedJiraIssue != null) " | issueKey=${simplifiedJiraIssue.key}" else ""
        val pullRequestLog = if (pullRequest != null) " | pullRequest=${pullRequest.url}" else ""

        logger.info { "[CheckMergeBlockStatus] Merge is NOT BLOCKED$issueLog$pullRequestLog" }
        return true
    }

}