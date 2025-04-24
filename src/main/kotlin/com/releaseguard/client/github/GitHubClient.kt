package com.releaseguard.client.github

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class GithubClient(
    private val restTemplate: RestTemplate,
    @Value("\${github.api-url}") private val apiUrl: String,
    @Value("\${github.api-token}") private val apiToken: String
) {

    private fun createHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("Authorization", "Bearer $apiToken")
        }
    }

    // Helper function to build URI with query params
    private fun buildUri(endpoint: String, params: Map<String, String>): URI {
        val baseUri = URI.create("$apiUrl/$endpoint")
        val uriBuilder = UriComponentsBuilder.fromUri(baseUri)
        params.forEach { (key, value) ->
            uriBuilder.queryParam(key, value)
        }
        return uriBuilder.build(true).toUri() // true = encode query params
    }

    fun <T> get(
        endpoint: String,
        responseType: Class<T>,
        params: Map<String, String> = emptyMap()
    ): ResponseEntity<T> {
        val url = buildUri(endpoint, params)
        val entity = HttpEntity<String>(createHeaders())
        return restTemplate.exchange(url, HttpMethod.GET, entity, responseType)
    }

    fun <T> post(
        endpoint: String,
        request: Any,
        responseType: Class<T>,
        params: Map<String, String> = emptyMap()
    ): ResponseEntity<T> {
        val url = buildUri(endpoint, params)
        val entity = HttpEntity(request, createHeaders())
        return restTemplate.exchange(url, HttpMethod.POST, entity, responseType)
    }

    fun <T> put(
        endpoint: String,
        request: Any,
        responseType: Class<T>,
        params: Map<String, String> = emptyMap()
    ): ResponseEntity<T> {
        val url = buildUri(endpoint, params)
        val entity = HttpEntity(request, createHeaders())
        return restTemplate.exchange(url, HttpMethod.PUT, entity, responseType)
    }

    fun <T> delete(
        endpoint: String,
        responseType: Class<T>,
        params: Map<String, String> = emptyMap()
    ): ResponseEntity<T> {
        val url = buildUri(endpoint, params)
        val entity = HttpEntity<String>(createHeaders())
        return restTemplate.exchange(url, HttpMethod.DELETE, entity, responseType)
    }
}
