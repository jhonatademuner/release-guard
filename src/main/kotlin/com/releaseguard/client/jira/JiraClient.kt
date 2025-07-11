package com.releaseguard.client.jira

import com.releaseguard.client.AbstractClient
import com.releaseguard.config.property.JiraClientProperties
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.util.*

@Component
class JiraClient(
    restTemplate: RestTemplate,
    private val props: JiraClientProperties
) : AbstractClient(restTemplate) {

    override val baseUrl: String
        get() = props.baseUrl

    override fun createHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            val credentials = "${props.email}:${props.apiToken}"
            val encodedAuth = Base64.getEncoder().encodeToString(credentials.toByteArray())
            set("Authorization", "Basic $encodedAuth")
        }
    }
}
