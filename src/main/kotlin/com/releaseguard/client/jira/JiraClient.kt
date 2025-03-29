package com.releaseguard.client.jira

import com.releaseguard.domain.jira.JiraIssue
import com.releaseguard.utils.exception.ResourceNotFoundException
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
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

    fun <T> get(endpoint: String, responseType: Class<T>): ResponseEntity<T> {
        val url = "$instanceUrl$endpoint"
        val entity = HttpEntity<String>(createHeaders())

        return try {
            restTemplate.exchange(url, HttpMethod.GET, entity, responseType)
        } catch (ex: HttpClientErrorException.NotFound) {
            ResponseEntity(null, HttpStatus.NOT_FOUND) // Ensure it properly catches and returns 404
        } catch (ex: HttpClientErrorException) {
            ResponseEntity(null, HttpStatus.INTERNAL_SERVER_ERROR) // Handle other client errors gracefully
        }
    }


}
