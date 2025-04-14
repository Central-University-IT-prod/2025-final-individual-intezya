package com.intezya.solution.controllers

import com.intezya.solution.utils.Generate
import io.github.serpro69.kfaker.Faker
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDate
import java.util.*
import java.util.stream.Stream
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Ads Controller Tests")
class AdsControllerTest(
	@LocalServerPort
	val port: Int,
) {
	private val faker = Faker()
	private val generator: Generate = Generate(port = port)
	private val serverUrl = "http://localhost:$port"

	companion object {
		@JvmStatic
		fun invalidClientIdProvider(): Stream<Arguments> = Stream.of(
			Arguments.of("not-a-uuid"), Arguments.of(""), Arguments.of("123abc"), Arguments.of(123)
		)

		@JvmStatic
		fun invalidAdvertisementIdProvider(): Stream<Arguments> = Stream.of(
			Arguments.of("not-a-uuid"), Arguments.of(""), Arguments.of("123abc"), Arguments.of(123)
		)
	}


	private fun createValidAdvertisement(advertiserId: UUID): UUID {
		val advertisementJson = """
            {
                "ad_title": "${faker.commerce.productName()}",
                "ad_text": "${faker.marketing.buzzwords()}",
                "cost_per_impression": ${
			faker.random.nextInt(1, 20).toDouble()
		},
                "cost_per_click": ${
			faker.random.nextInt(1, 20).toDouble()
		},
                "impressions_limit": 1000,
                "clicks_limit": 100,
                "start_date": ${
			LocalDate.now().toEpochDay()
		},
                "end_date": ${
			LocalDate.now().plusDays(30).toEpochDay()
		},
                "targeting": {
					"age_from": 18,
					"age_to": 65,
	                "location": null,
	                "gender": "ALL"
				}
            }
        """.trimIndent()

		val adResponse = given().contentType(ContentType.JSON)
			.body(advertisementJson)
			.post("$serverUrl/advertisers/$advertiserId/campaigns")
			.then()
			.statusCode(HttpStatus.CREATED.value())
			.extract()
			.body()
			.jsonPath()
			.getMap<String, Any>("")

		return UUID.fromString(adResponse["campaign_id"].toString())
	}

	//	@Test
	//	@DisplayName("Should get eligible advertisement for valid client")
	//	fun `get eligible advertisement for valid client`() {
	//		val clientId = UUID.randomUUID()
	//		generator.createClient(clientId = clientId)
	//		val advertiserId = UUID.randomUUID()
	//		generator.createAdvertiser(advertiserId)
	//		createValidAdvertisement(advertiserId)
	//
	//		val adResponse =
	//			given().contentType(ContentType.JSON).queryParam("client_id", clientId).get("$serverUrl/ads").then()
	//				.statusCode(HttpStatus.OK.value()).extract().body().jsonPath().getMap<String, Any>("")
	//
	//		assertNotNull(adResponse["ad_id"])
	//		assertNotNull(adResponse["advertiser_id"])
	//		assertNotNull(adResponse["ad_title"])
	//		assertNotNull(adResponse["ad_text"])
	//	}

	@ParameterizedTest(name = "Client ID: {0}")
	@MethodSource("invalidClientIdProvider")
	@DisplayName("Should reject get ad request with invalid client ID")
	fun `reject get ad request with invalid client ID`(clientId: Any) {
		given().contentType(ContentType.JSON)
			.queryParam("client_id", clientId)
			.get("$serverUrl/ads")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@ParameterizedTest(name = "Client ID: {0}")
	@MethodSource("invalidClientIdProvider")
	@DisplayName("Should reject ad click with invalid client ID")
	fun `reject ad click with invalid client ID`(clientId: Any) {
		val advertisementId = UUID.randomUUID()

		val requestBody = """
            {
                "client_id": ${if (clientId is String) "$clientId" else clientId}
            }
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(requestBody)
			.post("$serverUrl/ads/$advertisementId/click")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@ParameterizedTest(name = "Advertisement ID: {0}")
	@MethodSource("invalidAdvertisementIdProvider")
	@DisplayName("Should reject ad click with invalid advertisement ID")
	fun `reject ad click with invalid advertisement ID`(advertisementId: Any) {
		val clientId = UUID.randomUUID()
		generator.createClient(clientId = clientId)

		val requestBody = """
            {
                "client_id": "$clientId"
            }
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(requestBody)
			.post("$serverUrl/ads/$advertisementId/click")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@Test
	@DisplayName("Should reject ad click for non-existent advertisement")
	fun `reject ad click for non-existent advertisement`() {
		val clientId = UUID.randomUUID()
		generator.createClient(clientId = clientId)
		val nonExistentAdId = UUID.randomUUID()

		val requestBody = """
            {
                "client_id": "$clientId"
            }
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(requestBody)
			.post("$serverUrl/ads/$nonExistentAdId/click")
			.then()
			.statusCode(HttpStatus.NOT_FOUND.value())
	}

	@Test
	@DisplayName("Should reject ad click for advertisement not viewed by client")
	fun `reject ad click for advertisement not viewed by client`() {
		val clientId = UUID.randomUUID()
		generator.createClient(clientId = clientId)
		val advertiserId = UUID.randomUUID()
		generator.createAdvertiser(advertiserId)
		val advertisementId = createValidAdvertisement(advertiserId)

		val requestBody = """
            {
                "client_id": "$clientId"
            }
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(requestBody)
			.post("$serverUrl/ads/$advertisementId/click")
			.then()
			.statusCode(HttpStatus.FORBIDDEN.value())
	}
}
