package com.releaseguard.client.jira

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.*

@Component
class JiraClient(
    private val restTemplate: RestTemplate,
    @Value("\${jira.api-instance-url}") private val instanceUrl: String,
    @Value("\${jira.api-email}") private val email: String,
    @Value("\${jira.api-token}") private val apiToken: String
) {

    private fun createHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            val auth = "$email:$apiToken"
            val encodedAuth = Base64.getEncoder().encodeToString(auth.toByteArray())
            set("Authorization", "Basic $encodedAuth")
        }
    }

    private fun buildUri(endpoint: String, params: Map<String, String>): URI {
        val baseUri = URI.create("$instanceUrl/$endpoint")
        val uriBuilder = UriComponentsBuilder.fromUri(baseUri)
        params.forEach { (key, value) ->
            uriBuilder.queryParam(key, value)
        }
        return uriBuilder.build(true).toUri()
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
