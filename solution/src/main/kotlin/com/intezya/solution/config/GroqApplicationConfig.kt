package com.intezya.solution.config

import io.github.vyfor.groqkt.GroqClient
import io.github.vyfor.groqkt.GroqClient.Companion.BASE_URL
import io.github.vyfor.groqkt.GroqModel
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNamingStrategy
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class GroqApplicationConfig(
	@Value("\${groq.api.key}")
	private val apiKey: String,
	@Value("\${proxy.url}")
	private val proxyUrl: String,
	@Value("\${proxy.username}")
	private val proxyUsername: String,
	@Value("\${proxy.password}")
	private val proxyPassword: String
) {
	@OptIn(ExperimentalSerializationApi::class)
	@Bean
	fun groqClient(): GroqClient {
		System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "")
		System.setProperty("jdk.http.auth.proxying.disabledSchemes", "")

		// Encode proxy credentials for Basic Authentication
		val proxyAuth = Base64.getEncoder().encodeToString("$proxyUsername:$proxyPassword".toByteArray())

		val httpClient = HttpClient(CIO) {
			engine {
				proxy = ProxyBuilder.http(Url(proxyUrl))
			}

			// Install ContentNegotiation with Json configuration
			install(ContentNegotiation) {
				json(Json {
					explicitNulls = false
					encodeDefaults = true
					ignoreUnknownKeys = true
					namingStrategy = JsonNamingStrategy.SnakeCase
					classDiscriminatorMode = ClassDiscriminatorMode.NONE
				})
			}

			install(HttpRequestRetry) {
				retryIf(1) { _, response ->
					when (response.status.value) {
						in 500..599 -> {
							constantDelay()
							true
						}

						HttpStatusCode.TooManyRequests.value -> {
							constantDelay(
								response.headers["retry-after"]?.toLongOrNull()?.times(1000) ?: 0, 0, false
							)
							true
						}

						else -> false
					}
				}
			}

			install(DefaultRequest) {
				url(BASE_URL)
				header("Authorization", "Bearer $apiKey")
				header(HttpHeaders.ContentType, ContentType.Application.Json) // Explicitly set Content-Type
				header("Proxy-Authorization", "Basic $proxyAuth") // Add proxy authentication
			}
		}

		return GroqClient(
			apiKey = apiKey,
		) {
			client = httpClient

			defaults {
				chatCompletion {
					model = GroqModel.entries.random()
					temperature = 0.7
				}
			}
		}
	}
}
