package com.releaseguard.service.merge

import com.releaseguard.domain.github.dto.SimplifiedPullRequestDTO
import com.releaseguard.domain.jira.LinkedIssueDirection
import com.releaseguard.domain.jira.dto.SimplifiedIssueDTO
import com.releaseguard.domain.jira.SimplifiedIssueStatus
import com.releaseguard.domain.jira.dto.SimplifiedLinkedIssueDTO
import com.releaseguard.domain.merge.MergeBlock
import com.releaseguard.domain.merge.dto.MergeBlockCreateDTO
import com.releaseguard.domain.merge.dto.MergeBlockDTO
import com.releaseguard.domain.merge.dto.MergeCheckDTO
import com.releaseguard.repository.merge.MergeBlockRepository
import com.releaseguard.service.github.GithubService
import com.releaseguard.utils.assembler.MergeAssembler
import com.releaseguard.utils.exception.ResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

@Service
class MergeService(
    private val githubService: GithubService,
    private val mergeBlockRepository: MergeBlockRepository,
    private val mergeAssembler: MergeAssembler
) {

    private val logger = KotlinLogging.logger {}

    fun findAll(page: Int, size: Int): List<MergeBlockDTO> {
        val entityList: List<MergeBlock> = mergeBlockRepository.findAll(PageRequest.of(page, size)).content
        return mergeAssembler.toMergeBlockDTOList(entityList)
    }

    fun findById(id: UUID): MergeBlockDTO {
        val entity: MergeBlock = mergeBlockRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("MergeBlock with id $id not found") }
        return mergeAssembler.toMergeBlockDTO(entity)
    }

    fun create(dto: MergeBlockCreateDTO): MergeBlockDTO {
        val entity: MergeBlock = mergeAssembler.toMergeBlockEntity(dto)
        return mergeAssembler.toMergeBlockDTO(mergeBlockRepository.save(entity))
    }

    fun delete(id: UUID): Unit {
        val entity: MergeBlock = mergeBlockRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("MergeBlock with id $id not found") }
        mergeBlockRepository.delete(entity)
    }

    fun checkMerge(simplifiedIssueDTO: SimplifiedIssueDTO?, pullRequest: SimplifiedPullRequestDTO): MergeCheckDTO {

        val mergeCheckDTO = MergeCheckDTO(
            repo = pullRequest.repo,
            branch = pullRequest.targetBranch,
            pullRequest = pullRequest.url
        )



        if (simplifiedIssueDTO != null) {
            if (simplifiedIssueDTO.isUrgent) {
                logger.info { "[CheckMergeBlockStatus] Issue is marked as URGENT | issueKey=${simplifiedIssueDTO.key} | reason=ISSUE_KEY_FLAG" }
                return setMergeCheckReason(mergeCheckDTO, "Issue is marked as URGENT", true)
            }

            val blockingReason = isLinkedIssueBlocking(simplifiedIssueDTO)
            if( blockingReason != null) {
                logger.info { "[CheckMergeBlockStatus] Issue is BLOCKED | issueKey=${simplifiedIssueDTO.key} | reason=$blockingReason" }
                return setMergeCheckReason(mergeCheckDTO, blockingReason, false)
            }
        }

        if (githubService.isUrgentPullRequest(pullRequest)) return setMergeCheckReason(mergeCheckDTO, "Pull request is marked as URGENT", true)
        if (isScheduledBlock(pullRequest.repo, pullRequest.targetBranch)) return setMergeCheckReason(mergeCheckDTO, "Merge blocked by a scheduled block", true)

        val issueLog = if (simplifiedIssueDTO != null) " | issueKey=${simplifiedIssueDTO.key}" else ""

        logger.info { "[CheckMergeBlockStatus] Merge is NOT BLOCKED | pullRequest=${pullRequest.url}$issueLog" }
        return mergeCheckDTO
    }

    private fun isLinkedIssueBlocking(issueDto : SimplifiedIssueDTO):  String? {
        for (issue in issueDto.linkedIssues) {
            if (issue.type == "BLOCKS" && issue.linkDirection == LinkedIssueDirection.INWARD && issue.status != SimplifiedIssueStatus.DONE) {
                return "Issue is BLOCKED by ${issue.key} with status ${issue.status}"
            }
        }
        return null
    }

    private fun isScheduledBlock(repo: String, branch: String): Boolean {
        val blocks: List<MergeBlock> =
            mergeBlockRepository.findActiveByRepoAndBranchAt(repo, branch, LocalDateTime.now())

        if (blocks.isNotEmpty()) {
            logger.info { "[checkMergeBlock] Branch is BLOCKED | branch=$branch" }
            return true
        }

        return false
    }

    private fun setMergeCheckReason(dto: MergeCheckDTO, reason: String, mergeAvailability: Boolean): MergeCheckDTO {
        dto.mergeAvailability = mergeAvailability
        dto.reason = reason
        return dto
    }

}