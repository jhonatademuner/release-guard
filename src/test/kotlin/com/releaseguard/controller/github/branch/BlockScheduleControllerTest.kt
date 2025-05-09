package com.releaseguard.controller.github.branch

import com.releaseguard.domain.github.branch.BlockSchedule
import com.releaseguard.service.github.branch.BlockScheduleService
import com.releaseguard.utils.exception.GlobalExceptionHandler
import com.releaseguard.utils.exception.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.util.*

@WebMvcTest(BlockScheduleController::class)
@Import(BlockScheduleControllerTest.TestConfig::class, GlobalExceptionHandler::class)
class BlockScheduleControllerTest {

    @TestConfiguration
    class TestConfig {
        @Bean
        fun blockScheduleService(): BlockScheduleService = mockk()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var blockScheduleService: BlockScheduleService

    @Test
    fun `getAllBlocks should return OK and a list of blocks`() {
        val block = BlockSchedule(
            branchName = "branch1",
            startTime = Date(),
            endTime = Date(),
            reason = "Maintenance",
            createdBy = "Jhonata"
        )
        every { blockScheduleService.getAllBlocks() } returns listOf(block)

        mockMvc.perform(MockMvcRequestBuilders.get("/api/block-schedule"))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].branchName").value("branch1"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].reason").value("Maintenance"))
    }

    @Test
    fun `addBlock should return 200 OK when block is added successfully`() {
        val block = BlockSchedule(
            branchName = "branch1",
            startTime = Date(),
            endTime = Date(),
            reason = "Project presentation",
            createdBy = "Jhonata"
        )
        every { blockScheduleService.addBlock(any()) } returns Unit

        mockMvc.perform(MockMvcRequestBuilders.post("/api/block-schedule")
            .contentType("application/json")
            .content("""
                {
                    "branchName": "branch1", 
                    "startTime": "2025-06-25 16:52:15", 
                    "endTime": "2025-06-25 19:52:15", 
                    "reason": "Project presentation", 
                    "createdBy": "Jhonata"
                }
                """))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `removeBlockById should return 200 OK when block is removed successfully`() {
        val blockId = UUID.randomUUID()
        every { blockScheduleService.removeBlockById(blockId) } returns true

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/block-schedule/$blockId"))
            .andExpect(MockMvcResultMatchers.status().isOk)
    }

    @Test
    fun `removeBlockById should return 404 NOT FOUND when block is not found`() {
        val blockId = UUID.randomUUID()
        every { blockScheduleService.removeBlockById(blockId) } returns false

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/block-schedule/$blockId"))
            .andExpect(MockMvcResultMatchers.status().isNotFound)
            .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("BlockSchedule with id $blockId not found"))
            .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404))
            .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Not Found"))
    }
}
