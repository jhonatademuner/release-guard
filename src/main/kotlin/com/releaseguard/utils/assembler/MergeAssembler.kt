package com.releaseguard.utils.assembler

import com.releaseguard.domain.merge.MergeBlock
import com.releaseguard.domain.merge.dto.MergeBlockCreateDTO
import com.releaseguard.domain.merge.dto.MergeBlockDTO
import org.springframework.stereotype.Component

@Component
class MergeAssembler {

    fun toMergeBlockDTO(entity : MergeBlock): MergeBlockDTO {
        return MergeBlockDTO(
            id = entity.id,
            repo = entity.repo,
            branch = entity.branch,
            startTime = entity.startTime,
            endTime = entity.endTime,
            reason = entity.reason,
            createdBy = entity.createdBy,
            createdAt = entity.createdAt
        )
    }

    fun toMergeBlockEntity(dto: MergeBlockDTO): MergeBlock {
        return MergeBlock(
            id = dto.id,
            repo = dto.repo,
            branch = dto.branch,
            startTime = dto.startTime,
            endTime = dto.endTime,
            reason = dto.reason,
            createdBy = dto.createdBy,
            createdAt = dto.createdAt
        )
    }

    fun toMergeBlockEntity(dto: MergeBlockCreateDTO): MergeBlock {
        return MergeBlock(
            repo = dto.repo,
            branch = dto.branch,
            startTime = dto.startTime,
            endTime = dto.endTime,
            reason = dto.reason,
            createdBy = dto.createdBy
        )
    }

    fun toMergeBlockDTOList(entities: List<MergeBlock>): List<MergeBlockDTO> {
        return entities.map { toMergeBlockDTO(it) }
    }

}