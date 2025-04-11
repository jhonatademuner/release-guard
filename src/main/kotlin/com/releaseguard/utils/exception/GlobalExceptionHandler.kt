package com.releaseguard.utils.exception

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.util.*

@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [ResourceNotFoundException::class, HttpClientErrorException.NotFound::class])
    fun handleNotFoundExceptions(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<Any> {
        val path = (request as? ServletWebRequest)?.request?.requestURI

        val message = when (ex) {
            is HttpClientErrorException.NotFound -> {
                try {
                    val json: JsonNode = jacksonObjectMapper().readTree(ex.responseBodyAsString)
                    json["errorMessages"]?.firstOrNull()?.asText() ?: "Resource not found"
                } catch (e: Exception) {
                    "Resource not found"
                }
            }
            else -> ex.message ?: "Resource not found"
        }

        val errorResponse = ErrorResponse(
            timestamp = Date(),
            status = HttpStatus.NOT_FOUND.value(),
            error = HttpStatus.NOT_FOUND.reasonPhrase,
            message = message,
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