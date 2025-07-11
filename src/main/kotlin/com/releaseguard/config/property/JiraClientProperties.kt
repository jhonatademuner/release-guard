package com.releaseguard.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "clients.jira")
class JiraClientProperties {
    lateinit var baseUrl: String
    lateinit var email: String
    lateinit var apiToken: String
}