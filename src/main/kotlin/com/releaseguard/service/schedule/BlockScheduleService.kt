package com.releaseguard.service.schedule

import org.springframework.stereotype.Service
import com.releaseguard.domain.schedule.BlockSchedule
import java.util.*

import com.releaseguard.repository.schedule.BlockScheduleRepository
import com.releaseguard.utils.exception.ResourceNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.PageRequest

@Service
class BlockScheduleService(
    private val blockScheduleRepository: BlockScheduleRepository
) {

    private val logger = KotlinLogging.logger {}

    fun find(page: Int, resultsPerPage: Int): List<BlockSchedule> {
        return blockScheduleRepository.findAll(PageRequest.of(page, resultsPerPage)).content
    }

    fun create(newBlock: BlockSchedule) {
        blockScheduleRepository.save(newBlock)
    }

    fun removeById(id: UUID): BlockSchedule {
        val blockSchedule : BlockSchedule = blockScheduleRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("BlockSchedule with id $id not found") }

        blockScheduleRepository.deleteById(id)
        return blockSchedule
    }

    fun checkBranchMergeAvailability(branchName: String): Boolean {
        val blocks : List<BlockSchedule> = blockScheduleRepository.findActiveBlocksByBranchAndDate(branchName, Date())

        if (blocks.isNotEmpty()) {
            logger.info { "[CheckBranchBlockSchedule] Branch is BLOCKED | branchName=$branchName" }
            return false
        }

        return true
    }
}
