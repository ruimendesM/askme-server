package com.ruimendes.askme

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AskmeApplication

fun main(args: Array<String>) {
	runApplication<AskmeApplication>(*args)
}
