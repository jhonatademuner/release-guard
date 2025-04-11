package com.releaseguard.client.jira

import org.springframework.web.util.UriComponentsBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.util.*

@Component
class JiraClient(
    private val restTemplate: RestTemplate,
    @Value("\${jira.instance-url}") private val instanceUrl: String,
    @Value("\${jira.email}") private val email: String,
    @Value("\${jira.api-token}") private val apiToken: String
) {

    private fun createHeaders(): HttpHeaders {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            val auth = "$email:$apiToken"
            val encodedAuth = Base64.getEncoder().encodeToString(auth.toByteArray())
            set("Authorization", "Basic $encodedAuth")
        }
        return headers
    }

    fun <T> get(
        endpoint: String,
        responseType: Class<T>,
        params: Map<String, String> = emptyMap()
    ): ResponseEntity<T> {
        val baseUri = URI.create("$instanceUrl$endpoint")

        val uriBuilder = UriComponentsBuilder
            .fromUri(baseUri)

        params.forEach { (key, value) ->
            uriBuilder.queryParam(key, value)
        }

        val url = uriBuilder.build(true).toUri() // true = encode query params

        val entity = HttpEntity<String>(createHeaders())

        return restTemplate.exchange(url, HttpMethod.GET, entity, responseType)
    }

}
