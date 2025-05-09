package com.releaseguard.service.schedule

import org.springframework.stereotype.Service
import com.releaseguard.domain.schedule.BlockSchedule
import java.io.File
import java.nio.file.Paths
import java.util.*

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class BlockScheduleService {

    private val logger = KotlinLogging.logger {}

    private val objectMapper = jacksonObjectMapper().setTimeZone(TimeZone.getTimeZone("America/Sao_Paulo"))
    private val filePath = Paths.get("data/block-schedule.json").toAbsolutePath().toString()

    fun getAllBlocks(): List<BlockSchedule> {
        val file = File(filePath)
        if (!file.exists() || file.length() == 0L) return emptyList()
        val blocks = objectMapper.readValue<List<BlockSchedule>>(file)
        val filteredBlocks = removeExpiredBlocks(blocks)
        return filteredBlocks
    }

    fun addBlock(newBlock: BlockSchedule) {
        val blocks = getAllBlocks().toMutableList()
        blocks.add(newBlock)
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(File(filePath), blocks)
    }

    fun removeBlockById(id: UUID): Boolean {
        val blocks = getAllBlocks().toMutableList()
        val removed = blocks.removeIf { it.id == id }
        if (removed) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(File(filePath), blocks)
        }
        return removed
    }

    fun checkBranchBlockSchedule(branchName: String): Boolean {
        val blocks = getAllBlocks()
        val currentTime = Date.from(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toInstant())
        val branchBlock = blocks.filter { block ->
            block.branchName == branchName && currentTime.after(block.startTime) && currentTime.before(block.endTime)
        }

        if (branchBlock.isNotEmpty()) {
            logger.info { "[CheckBranchBlockSchedule] Branch is BLOCKED | branchName=$branchName | blockSchedule=$branchBlock" }
            return true
        }

        return true
    }

    private fun removeExpiredBlocks(blocks: List<BlockSchedule>) : List<BlockSchedule> {
        val currentTime = Date.from(ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")).toInstant())
        val filteredBlocks = blocks.filter { block -> currentTime.before(block.endTime) }
        if(filteredBlocks.size != blocks.size) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(File(filePath), filteredBlocks)
        }
        logger.info { "[RemoveExpiredBlockSchedules] Removing expired block schedules" }
        return filteredBlocks
    }


}
