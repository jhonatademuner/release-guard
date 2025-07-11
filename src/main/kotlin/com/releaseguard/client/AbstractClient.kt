package com.releaseguard.client

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

abstract class AbstractClient(
    private val restTemplate: RestTemplate,
) {

    protected abstract val baseUrl: String

    protected open fun createHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
        }
    }

    // Helper function to build URI with query params
    private fun buildUri(endpoint: String, params: Map<String, Any?>): URI {
        val baseUri = URI.create("$baseUrl/$endpoint")
        val uriBuilder = UriComponentsBuilder.fromUri(baseUri)
        params.forEach { (key, value) ->
            uriBuilder.queryParam(key, value)
        }
        return uriBuilder.build(true).toUri() // true = encode query params
    }

    fun <T> get(endpoint: String, responseType: Class<T>, params: Map<String, String> = emptyMap()): ResponseEntity<T> {
        return restTemplate.exchange(
            buildUri(endpoint, params),
            HttpMethod.GET,
            HttpEntity<String>(createHeaders()),
            responseType
        )
    }

    fun <T> post(
        endpoint: String,
        request: Any,
        responseType: Class<T>,
        params: Map<String, String> = emptyMap()
    ): ResponseEntity<T> {
        return restTemplate.exchange(
            buildUri(endpoint, params),
            HttpMethod.POST,
            HttpEntity(request, createHeaders()),
            responseType
        )
    }

    fun <T> put(
        endpoint: String,
        request: Any,
        responseType: Class<T>,
        params: Map<String, String> = emptyMap()
    ): ResponseEntity<T> {
        return restTemplate.exchange(
            buildUri(endpoint, params),
            HttpMethod.PUT,
            HttpEntity(request, createHeaders()),
            responseType
        )
    }

    fun <T> delete(
        endpoint: String,
        responseType: Class<T>,
        params: Map<String, String> = emptyMap()
    ): ResponseEntity<T> {
        return restTemplate.exchange(
            buildUri(endpoint, params),
            HttpMethod.DELETE,
            HttpEntity<String>(createHeaders()),
            responseType
        )
    }

}