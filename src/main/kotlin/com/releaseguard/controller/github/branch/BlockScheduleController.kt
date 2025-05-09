package com.releaseguard.controller.github.branch

import com.releaseguard.domain.github.branch.BlockSchedule
import com.releaseguard.service.github.branch.BlockScheduleService
import com.releaseguard.utils.exception.ResourceNotFoundException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/block-schedule")
class BlockScheduleController(
    private val blockScheduleService: BlockScheduleService
) {

    @GetMapping
    fun getAllBlocks(): List<BlockSchedule> {
        return blockScheduleService.getAllBlocks()
    }

    @PostMapping
    fun addBlock(@RequestBody block: BlockSchedule): ResponseEntity<Void> {
        blockScheduleService.addBlock(block)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{id}")
    fun removeBlockById(@PathVariable id: UUID): ResponseEntity<Void> {
        val removed = blockScheduleService.removeBlockById(id)
        if (removed) return  ResponseEntity.ok().build()
        throw ResourceNotFoundException("BlockSchedule with id $id not found")
    }

}