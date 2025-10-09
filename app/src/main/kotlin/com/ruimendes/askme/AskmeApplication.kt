package com.ruimendes.askme

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class AskmeApplication

fun main(args: Array<String>) {
	runApplication<AskmeApplication>(*args)
}
