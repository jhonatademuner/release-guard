package com.releaseguard.utils.exception

import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest

class GlobalExceptionHandlerTest {

 private lateinit var exceptionHandler: GlobalExceptionHandler
 private lateinit var webRequest: WebRequest

 @BeforeEach
 fun setUp() {
  exceptionHandler = GlobalExceptionHandler()

  // Create a relaxed mock for HttpServletRequest and ServletWebRequest
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
  val responseEntity = exceptionHandler.handleResourceNotFoundException(exception, webRequest)

  // Assert
  assertThat(responseEntity.statusCode.value()).isEqualTo(404)
  val errorResponse = responseEntity.body as ErrorResponse
  assertThat(errorResponse.status).isEqualTo(404)
  assertThat(errorResponse.error).isEqualTo("Not Found")
  assertThat(errorResponse.message).isEqualTo(exceptionMessage)
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
  assertThat(responseEntity.statusCode.value()).isEqualTo(400)
  val errorResponse = responseEntity.body as ErrorResponse
  assertThat(errorResponse.status).isEqualTo(400)
  assertThat(errorResponse.error).isEqualTo("Bad Request")
  assertThat(errorResponse.message).isEqualTo(exceptionMessage)
  assertThat(errorResponse.path).isEqualTo("/test/uri")
  assertThat(errorResponse.timestamp).isNotNull
 }
}
