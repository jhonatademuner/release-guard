package com.releaseguard

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties
class ReleaseGuardApplication

fun main(args: Array<String>) {
	runApplication<ReleaseGuardApplication>(*args)
}
