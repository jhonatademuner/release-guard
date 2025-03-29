package com.releaseguard.utils.exception;

import lombok.Data
import java.util.*

@Data
data class ErrorResponse(
    val timestamp: Date,
    val status: Int,
    val error: String,
    val message: String?,
    val path: String?
)