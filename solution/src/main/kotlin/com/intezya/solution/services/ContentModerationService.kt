package com.intezya.solution.services

import com.intezya.solution.dto.ContentModerationResult
import com.intezya.solution.dto.perspective.Comment
import com.intezya.solution.dto.perspective.PerspectiveRequest
import com.intezya.solution.dto.perspective.PerspectiveResponse
import io.netty.channel.ChannelOption
import io.netty.resolver.DefaultAddressResolverGroup
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.io.File
import java.time.Duration

@Service
class ContentModerationService(
	@Value("\${google.perspective.api.key}")
	private val googleApiKey: String,
	@Value("\${google.perspective.api.threshold:0.7}")
	private val toxicityThreshold: Float,
	@Value("\${google.perspective.api.timeout:3000}")
	private val timeoutMs: Long,
	@Value("\${content.moderation.banned-words-file:/tmp/banned-words.txt}")
	private val bannedWordsFile: String,
) {
	private val logger = LoggerFactory.getLogger(ContentModerationService::class.java)
	private var bannedWords: MutableSet<String>
	private val file: File

	init {
		file = File(bannedWordsFile)
		bannedWords = loadBannedWords().toMutableSet()
		logger.info("Loaded ${bannedWords.size} banned words")
	}

	fun addBannedWord(word: String): Boolean {
		return try {
			val normalizedWord = word.trim().lowercase()
			if (normalizedWord.isBlank()) {
				return false
			}

			synchronized(this) {
				if (bannedWords.add(normalizedWord)) {
					saveBannedWords()
					logger.info("Added new banned word: $normalizedWord")
					true
				} else {
					false
				}
			}
		} catch (e: Exception) {
			logger.error("Error adding banned word: $word", e)
			false
		}
	}

	fun removeBannedWord(word: String): Boolean {
		return try {
			val normalizedWord = word.trim().lowercase()
			synchronized(this) {
				if (bannedWords.remove(normalizedWord)) {
					saveBannedWords()
					logger.info("Removed banned word: $normalizedWord")
					true
				} else {
					false
				}
			}
		} catch (e: Exception) {
			logger.error("Error removing banned word: $word", e)
			false
		}
	}

	private fun saveBannedWords() {
		try {
			file.writeText(bannedWords.joinToString("\n"))
		} catch (e: Exception) {
			logger.error("Error saving banned words to file", e)
			throw e
		}
	}

	private fun loadBannedWords(): Set<String> {
		return try {
			if (!file.exists()) {
				val parentDir = file.parentFile
				if (parentDir != null && !parentDir.exists()) {
					parentDir.mkdirs()
				}

				file.createNewFile()
				file.writeText("")
				logger.info("Created new banned words file: ${file.absolutePath}")
			}

			file.readLines().map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
		} catch (e: Exception) {
			logger.error("Failed to load banned words", e)
			emptySet()
		}
	}


	private fun containsBannedWords(text: String): Boolean {
		val normalizedText = text.lowercase()
		return bannedWords.any { bannedWord ->
			normalizedText.contains(bannedWord)
		}
	}

	suspend fun checkAdvertisement(title: String, text: String): Boolean {
		return try {            // First check for banned words
			if (containsBannedWords(title) || containsBannedWords(text)) {
				logger.info("Advertisement contains banned words")
				return false
			}

			val titleFuture = checkContent(title)
			val textFuture = checkContent(text)

			val titleResult = titleFuture.awaitOrDefaultWithTimeout()
			val textResult = textFuture.awaitOrDefaultWithTimeout()

			val titleProfanityScore = titleResult?.otherScores?.get("PROFANITY") ?: 0f
			val textProfanityScore = textResult?.otherScores?.get("PROFANITY") ?: 0f

			titleProfanityScore <= 0.8 && textProfanityScore <= 0.8
		} catch (e: Exception) {
			logger.error("Error checking advertisement content", e)
			false
		}
	}

	private val webClient = WebClient.builder()
		.baseUrl("https://commentanalyzer.googleapis.com")
		.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
		.clientConnector(
			ReactorClientHttpConnector(
				HttpClient.create()
					.resolver(DefaultAddressResolverGroup.INSTANCE)
					.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs.toInt())
					.responseTimeout(Duration.ofMillis(timeoutMs))
			)
		)
		.build()


	private suspend fun Mono<ContentModerationResult>.awaitOrDefaultWithTimeout(): ContentModerationResult? {
		return try {
			this.timeout(Duration.ofMillis(timeoutMs)).onErrorResume { error ->
				logger.error("Error during content moderation", error)
				Mono.empty()
			}.awaitFirstOrNull()
		} catch (e: Exception) {
			logger.error("Timeout or error waiting for moderation result", e)
			null
		}
	}

	private fun checkContent(text: String): Mono<ContentModerationResult> {
		if (text.isBlank()) {
			return Mono.just(
				ContentModerationResult(
					originalText = text, isSafe = true, toxicityScore = 0f, otherScores = emptyMap()
				)
			)
		}

		val requestBody = createRequestBody(text)

		return webClient.post()
			.uri { uriBuilder ->
				uriBuilder.path("/v1alpha1/comments:analyze").queryParam("key", googleApiKey).build()
			}
			.bodyValue(requestBody)
			.retrieve()
			.bodyToMono(PerspectiveResponse::class.java)
			.timeout(Duration.ofMillis(timeoutMs))
			.map { response ->
				val toxicityScore = response.attributeScores["TOXICITY"]?.summaryScore?.value ?: 0f
				val otherScores =
					response.attributeScores.filter { it.key != "TOXICITY" }.mapValues { it.value.summaryScore.value }

				ContentModerationResult(
					originalText = text,
					isSafe = toxicityScore < toxicityThreshold,
					toxicityScore = toxicityScore,
					otherScores = otherScores
				)
			}
			.onErrorResume { error ->
				logger.warn("Error analyzing content: ${error.message}", error)
				Mono.just(
					ContentModerationResult(
						originalText = text, isSafe = false, toxicityScore = 1f, otherScores = emptyMap()
					)
				)
			}
	}

	private fun createRequestBody(text: String): PerspectiveRequest {
		return PerspectiveRequest(
			comment = Comment(text = text), requestedAttributes = mapOf(
				"TOXICITY" to emptyMap<String, Any>(),
				"SEVERE_TOXICITY" to emptyMap(),
				"IDENTITY_ATTACK" to emptyMap(),
				"INSULT" to emptyMap(),
				"PROFANITY" to emptyMap(),
				"THREAT" to emptyMap()
			)
		)
	}
}
