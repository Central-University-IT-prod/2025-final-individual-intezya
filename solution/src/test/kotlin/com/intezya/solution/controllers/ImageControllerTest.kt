package com.intezya.solution.controllers

import com.intezya.solution.utils.Generate
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Image Controller Tests")
class ImageControllerTest(
	@LocalServerPort
	val port: Int,
) {
	private val generator: Generate = Generate(port = port)
	private val serverUrl = "http://localhost:$port"

	private fun verifyAdvertisementImageExists(advertiserId: UUID, campaignId: Any) {
		given().get("$serverUrl/advertisers/$advertiserId/campaigns/$campaignId/image")
			.then()
			.statusCode(HttpStatus.OK.value())
			.contentType(equalTo("image/jpeg"))
	}

	@Test
	@DisplayName("Should get valid advertisement image")
	fun `create advertisement with valid data and image URL`() {
		val advertiserId = UUID.randomUUID()

		val advertisement = generator.createAdvertisement(
			advertiserId, generator.createValidAdJson(
				imageUrl = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_light_color_272x92dp.png"
			)
		)

		val campaignId = advertisement["campaign_id"] ?: throw IllegalStateException("Campaign ID is null")

		verifyAdvertisementImageExists(advertiserId, campaignId)
	}

	@Test
	@DisplayName("Shouldn't get image of unexisting advertisement")
	fun `try to get image of unexisting advertisement`() {
		given().get("$serverUrl/advertisers/${UUID.randomUUID()}/campaigns/${UUID.randomUUID()}/image")
			.then()
			.statusCode(HttpStatus.NOT_FOUND.value())
	}

	@Test
	@DisplayName("Shouldn't get image of not owned advertisement")
	fun `try to get image of not owned advertisement`() {
		val advertiserId = UUID.randomUUID()

		val advertisement = generator.createAdvertisement(
			advertiserId, generator.createValidAdJson(
				imageUrl = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_light_color_272x92dp.png"
			)
		)

		val campaignId = advertisement["campaign_id"] ?: throw IllegalStateException("Campaign ID is null")

		given().get("$serverUrl/advertisers/${UUID.randomUUID()}/campaigns/$campaignId/image")
			.then()
			.statusCode(HttpStatus.FORBIDDEN.value())
	}

	@Test
	@DisplayName("Shouldn't get image of advertisement without image")
	fun `try to get image of advertisement without image`() {
		val advertiserId = UUID.randomUUID()

		val advertisement = generator.createAdvertisement(
			advertiserId, generator.createValidAdJson()
		)

		val campaignId = advertisement["campaign_id"] ?: throw IllegalStateException("Campaign ID is null")

		given().get("$serverUrl/advertisers/${advertiserId}/campaigns/$campaignId/image")
			.then()
			.statusCode(HttpStatus.NO_CONTENT.value())
	}
}
