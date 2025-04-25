package com.releaseguard.config.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class LoggingContextFilter (
    @Value("\${spring.application.name}") private val serviceName: String
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val requestId = request.getHeader("X-Request-ID") ?: UUID.randomUUID().toString()
            val traceId = request.getHeader("X-B3-TraceId") ?: UUID.randomUUID().toString()
            val method = request.method
            val path = request.requestURI

            MDC.put("requestId", requestId)
            MDC.put("traceId", traceId)
            MDC.put("method", method)
            MDC.put("path", path)
            MDC.put("service", serviceName)

            filterChain.doFilter(request, response)
        } finally {
            MDC.clear() // Important to prevent memory leaks
        }
    }
}
