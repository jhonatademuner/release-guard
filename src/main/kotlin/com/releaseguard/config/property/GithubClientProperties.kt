package com.releaseguard.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "clients.github")
class GithubClientProperties {
    lateinit var baseUrl: String
    lateinit var apiToken: String
}