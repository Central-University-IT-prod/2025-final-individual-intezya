package com.intezya.solution.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ApplicationConfig {
	@Value("\${google.perspective.api.key}")
	private lateinit var googleApiKey: String

	@Value("\${google.perspective.api.toxicity_threshold:0.7}")
	private var toxicityThreshold: Double = 0.7

	@Value("\${telegram.bot.token}")
	private lateinit var botToken: String

	@Bean
	fun googleApiKey(): String {
		return googleApiKey
	}

	@Bean
	fun toxicityThreshold(): Double {
		return toxicityThreshold
	}

	@Bean
	fun botToken(): String {
		return botToken
	}

	@Bean
	fun webClient(): WebClient {
		return WebClient.builder().baseUrl("https://commentanalyzer.googleapis.com")
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE).build()
	}
}
