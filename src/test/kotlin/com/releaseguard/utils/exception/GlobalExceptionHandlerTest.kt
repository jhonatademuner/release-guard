package com.releaseguard.utils.exception

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import java.nio.charset.Charset

class GlobalExceptionHandlerTest {

    private lateinit var exceptionHandler: GlobalExceptionHandler
    private lateinit var webRequest: WebRequest

    @BeforeEach
    fun setUp() {
        exceptionHandler = GlobalExceptionHandler()
        // Create a relaxed mock for HttpServletRequest and wrap it in a ServletWebRequest.
        val httpServletRequest: HttpServletRequest = mockk(relaxed = true)
        every { httpServletRequest.requestURI } returns "/test/uri"
        webRequest = ServletWebRequest(httpServletRequest)
    }

    @Test
    fun `handleResourceNotFoundException returns proper 404 response`() {
        // Arrange
        val exceptionMessage = "Resource not found"
        val exception = ResourceNotFoundException(exceptionMessage)

        // Act
        val responseEntity = exceptionHandler.handleNotFoundExceptions(exception, webRequest)

        // Assert
        assertThat(responseEntity.statusCode.value()).isEqualTo(HttpStatus.NOT_FOUND.value())
        val errorResponse = responseEntity.body as ErrorResponse
        assertThat(errorResponse.status).isEqualTo(HttpStatus.NOT_FOUND.value())
        assertThat(errorResponse.error).isEqualTo(HttpStatus.NOT_FOUND.reasonPhrase)
        assertThat(errorResponse.message).isEqualTo(exceptionMessage)
        assertThat(errorResponse.path).isEqualTo("/test/uri")
        assertThat(errorResponse.timestamp).isNotNull
    }

    @Test
    fun `handleHttpClientErrorNotFoundException returns proper 404 response with valid JSON`() {
        // Arrange: simulate a JSON response with an "errorMessages" array.
        val validJson = """{"errorMessages":["Specific error message"]}"""
        val exception = HttpClientErrorException.NotFound.create(
            HttpStatus.NOT_FOUND,
            HttpStatus.NOT_FOUND.reasonPhrase,
            HttpHeaders(),
            validJson.toByteArray(Charset.defaultCharset()),
            Charset.defaultCharset()
        )

        // Act
        val responseEntity = exceptionHandler.handleNotFoundExceptions(exception, webRequest)

        // Assert: It should extract the first error message from the array.
        assertThat(responseEntity.statusCode.value()).isEqualTo(HttpStatus.NOT_FOUND.value())
        val errorResponse = responseEntity.body as ErrorResponse
        assertThat(errorResponse.message).isEqualTo("Specific error message")
        assertThat(errorResponse.status).isEqualTo(HttpStatus.NOT_FOUND.value())
        assertThat(errorResponse.error).isEqualTo(HttpStatus.NOT_FOUND.reasonPhrase)
        assertThat(errorResponse.path).isEqualTo("/test/uri")
        assertThat(errorResponse.timestamp).isNotNull
    }

    @Test
    fun `handleHttpClientErrorNotFoundException returns proper 404 response with invalid JSON`() {
        // Arrange: simulate an invalid JSON response so JSON parsing fails.
        val invalidJson = "Not a JSON"
        val exception = HttpClientErrorException.NotFound.create(
            HttpStatus.NOT_FOUND,
            HttpStatus.NOT_FOUND.reasonPhrase,
            HttpHeaders(),
            invalidJson.toByteArray(Charset.defaultCharset()),
            Charset.defaultCharset()
        )

        // Act
        val responseEntity = exceptionHandler.handleNotFoundExceptions(exception, webRequest)

        // Assert: should fall back to default message since JSON parsing fails.
        assertThat(responseEntity.statusCode.value()).isEqualTo(HttpStatus.NOT_FOUND.value())
        val errorResponse = responseEntity.body as ErrorResponse
        assertThat(errorResponse.message).isEqualTo("Resource not found")
        assertThat(errorResponse.status).isEqualTo(HttpStatus.NOT_FOUND.value())
        assertThat(errorResponse.error).isEqualTo(HttpStatus.NOT_FOUND.reasonPhrase)
        assertThat(errorResponse.path).isEqualTo("/test/uri")
        assertThat(errorResponse.timestamp).isNotNull
    }

    @Test
    fun `handleIllegalArgumentException returns proper 400 response`() {
        // Arrange
        val exceptionMessage = "Invalid argument"
        val exception = IllegalArgumentException(exceptionMessage)

        // Act
        val responseEntity = exceptionHandler.handleIllegalArgumentException(exception, webRequest)

        // Assert
        assertThat(responseEntity.statusCode.value()).isEqualTo(HttpStatus.BAD_REQUEST.value())
        val errorResponse = responseEntity.body as ErrorResponse
        assertThat(errorResponse.status).isEqualTo(HttpStatus.BAD_REQUEST.value())
        assertThat(errorResponse.error).isEqualTo(HttpStatus.BAD_REQUEST.reasonPhrase)
        assertThat(errorResponse.message).isEqualTo(exceptionMessage)
        assertThat(errorResponse.path).isEqualTo("/test/uri")
        assertThat(errorResponse.timestamp).isNotNull
    }
}
