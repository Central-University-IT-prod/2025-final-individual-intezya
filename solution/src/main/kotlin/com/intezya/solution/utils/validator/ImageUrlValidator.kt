package com.intezya.solution.utils.validator

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.server.ResponseStatusException
import java.net.URI
import java.time.Duration


@Component
class ImageUrlValidator(
	private val webClient: WebClient,
) {
	fun validateImageUrl(url: String?) {
		if (url == null) return
		val normalizedUrl = if (!url.startsWith("http")) {
			"https://$url"
		} else {
			url
		}

		try {
			URI(normalizedUrl).toURL()
		} catch (e: Exception) {
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid URL format")
		}

		try {
			webClient.head().uri(normalizedUrl).retrieve().toBodilessEntity().block(Duration.ofSeconds(2))
				?.let { response ->
					val contentType = response.headers.contentType?.toString() ?: ""
					if (!contentType.startsWith("image/")) {
						throw ResponseStatusException(HttpStatus.BAD_REQUEST, "URL does not point to an image")
					}
				}
		} catch (e: Exception) {
			val errorMessage = when (e) {
				is WebClientResponseException -> when (e.statusCode) {
					HttpStatus.NOT_FOUND -> "Image not found"
					else -> "Failed to validate image URL: ${e.statusCode}"
				}

				is ResponseStatusException -> throw e
				else -> "Failed to validate image URL"
			}
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage)
		}
	}
}
