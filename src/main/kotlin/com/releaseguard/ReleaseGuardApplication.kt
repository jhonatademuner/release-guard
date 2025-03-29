package com.releaseguard

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReleaseGuardApplication

fun main(args: Array<String>) {
	runApplication<ReleaseGuardApplication>(*args)
}
