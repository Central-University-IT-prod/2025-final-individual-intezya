package com.intezya.solution

import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

@SpringBootApplication
@EnableJdbcRepositories
class SolutionApplication

fun main(args: Array<String>) {
	val dotenv = dotenv {
		directory = "./"
		filename = ".env"
		ignoreIfMissing = true
	}

	dotenv.entries().forEach {
		System.setProperty(it.key, it.value)
	}

	val context = runApplication<SolutionApplication>(*args)
	val botService = context.getBean(TelegramBot::class.java)
	botService.startBot()
}
