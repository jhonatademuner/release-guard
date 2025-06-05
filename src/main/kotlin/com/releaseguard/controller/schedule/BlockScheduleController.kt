package com.releaseguard.controller.schedule

import com.releaseguard.domain.schedule.BlockSchedule
import com.releaseguard.service.schedule.BlockScheduleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/block-schedule")
class BlockScheduleController(
    private val blockScheduleService: BlockScheduleService
) {

    @GetMapping
    fun findAll(
        @RequestParam(defaultValue = "0") page : Int,
        @RequestParam(defaultValue = "10") resultsPerPage : Int
    ): List<BlockSchedule> {
        return blockScheduleService.find(page, resultsPerPage)
    }

    @PostMapping
    fun create(@RequestBody block: BlockSchedule): ResponseEntity<Void> {
        blockScheduleService.create(block)
        return ResponseEntity.ok().build()
    }

    @DeleteMapping("/{id}")
    fun removeById(@PathVariable id: UUID): ResponseEntity<BlockSchedule> {
        return  ResponseEntity.ok(blockScheduleService.removeById(id))
    }

}