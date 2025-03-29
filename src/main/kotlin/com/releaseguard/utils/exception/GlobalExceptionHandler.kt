package com.releaseguard.utils.exception

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.util.*

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<Any> {
        val path = (request as? ServletWebRequest)?.request?.requestURI

        val errorResponse = ErrorResponse(
            timestamp = Date(),
            status = HttpStatus.NOT_FOUND.value(),
            error = HttpStatus.NOT_FOUND.reasonPhrase,
            message = ex.message,
            path = path
        )

        return ResponseEntity(errorResponse, HttpHeaders(), HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<Any> {
        val path = (request as? ServletWebRequest)?.request?.requestURI

        val errorResponse = ErrorResponse(
            timestamp = Date(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = ex.message,
            path = path
        )

        return ResponseEntity(errorResponse, HttpHeaders(), HttpStatus.BAD_REQUEST)
    }
}