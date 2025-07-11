package com.releaseguard.client.github

import com.releaseguard.client.AbstractClient
import com.releaseguard.config.property.GithubClientProperties
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class GithubClient(
    restTemplate: RestTemplate,
    private val props: GithubClientProperties
) : AbstractClient(restTemplate) {

    override val baseUrl: String
        get() = props.baseUrl

    override fun createHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(props.apiToken)
        }
    }
}
