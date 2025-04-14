package com.intezya.solution.controllers

import com.intezya.solution.enums.Gender
import com.intezya.solution.utils.Generate
import io.github.serpro69.kfaker.Faker
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers.equalTo
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
import java.util.*
import java.util.stream.Stream
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Advertisement Controller Tests")
class AdvertisementControllerTest(
	@LocalServerPort
	val port: Int,
) {
	private val generator: Generate = Generate(port = port)
	private val faker: Faker = Faker()
	private val serverUrl = "http://localhost:$port"

	private fun verifyAdvertisementExists(advertiserId: UUID, campaignId: Any) {
		given().get("$serverUrl/advertisers/$advertiserId/campaigns/$campaignId")
			.then()
			.statusCode(HttpStatus.OK.value())
			.body("campaign_id", equalTo(campaignId))
			.body("advertiser_id", equalTo(advertiserId.toString()))
	}

	companion object {
		@JvmStatic
		fun invalidLimitsProvider(): Stream<Arguments> = Stream.of(
			Arguments.of(1, 10000), Arguments.of(-1, 0), Arguments.of(0, -1)
		)

		@JvmStatic
		fun invalidCostProvider(): Stream<Arguments> = Stream.of(
			Arguments.of(-1234980.0, 43984389.0), Arguments.of(1234980.0, -1233.0)
		)

		@JvmStatic
		fun invalidTitleTextProvider(): Stream<Arguments> = Stream.of(
			Arguments.of("", ""), Arguments.of(1, ""), Arguments.of("", 1), Arguments.of(1, 1)
		)

		@JvmStatic
		fun invalidDateProvider(): Stream<Arguments> = Stream.of(
			Arguments.of(-1, -1), Arguments.of(100, 10), Arguments.of(100, -1), Arguments.of(-1, 100)
		)

		@JvmStatic
		fun invalidAgeFromProvider(): Stream<Arguments> = Stream.of(
			Arguments.of(-123), Arguments.of("lol!"), Arguments.of("11")
		)

		@JvmStatic
		fun invalidAgeToProvider(): Stream<Arguments> = Stream.of(
			Arguments.of("age_to"), Arguments.of(-123), Arguments.of("123")
		)
	}

	@Test
	@DisplayName("Should create valid advertisement")
	fun `create advertisement with valid data`() {
		val advertiserId = UUID.randomUUID()

		val advertisement = generator.createAdvertisement(advertiserId, generator.createValidAdJson())

		val campaignId = advertisement["campaign_id"] ?: throw IllegalStateException("Campaign ID is null")
		verifyAdvertisementExists(advertiserId, campaignId)
	}

	@Test
	@DisplayName("Should create valid advertisement with URL")
	fun `create advertisement with valid data and image URL`() {
		val advertiserId = UUID.randomUUID()

		val advertisement = generator.createAdvertisement(
			advertiserId, generator.createValidAdJson(
				imageUrl = "https://www.google.com/images/branding/googlelogo/2x/googlelogo_light_color_272x92dp.png"
			)
		)

		val campaignId = advertisement["campaign_id"] ?: throw IllegalStateException("Campaign ID is null")
		verifyAdvertisementExists(advertiserId, campaignId)
	}

	@Test
	@DisplayName("Should create valid advertisement without targeting")
	fun `create advertisement without targeting`() {
		val advertiserId = UUID.randomUUID()

		val advertisement = generator.createAdvertisement(
			advertiserId, generator.createValidAdJson(targeting = emptyMap())
		)

		val campaignId = advertisement["campaign_id"] ?: throw IllegalStateException("Campaign ID is null")
		verifyAdvertisementExists(advertiserId, campaignId)
	}

	@ParameterizedTest(name = "Impressions limit: {0}, Clicks limit: {1}")
	@MethodSource("invalidLimitsProvider")
	@DisplayName("Should reject invalid limits")
	fun `reject advertisement with invalid limits`(impressionsLimit: Int, clicksLimit: Int) {
		val advertiserId = UUID.randomUUID()

		given().contentType(ContentType.JSON).body(
			generator.createValidAdJson(
				impressionsLimit = impressionsLimit, clicksLimit = clicksLimit
			).trimIndent()
		).post("$serverUrl/advertisers/$advertiserId/campaigns").then().statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@ParameterizedTest(name = "Cost per impression: {0}, Cost per click: {1}")
	@MethodSource("invalidCostProvider")
	@DisplayName("Should reject invalid costs")
	fun `reject advertisement with invalid costs`(costPerImpression: Double, costPerClick: Double) {
		val advertiserId = UUID.randomUUID()

		given().contentType(ContentType.JSON).body(
			generator.createValidAdJson(
				costPerImpression = costPerImpression, costPerClick = costPerClick
			).trimIndent()
		).post("$serverUrl/advertisers/$advertiserId/campaigns").then().statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@ParameterizedTest(name = "Title: {0}, Text: {1}")
	@MethodSource("invalidTitleTextProvider")
	@DisplayName("Should reject invalid title or text")
	fun `reject advertisement with invalid title or text`(title: Any, text: Any) {
		val advertiserId = UUID.randomUUID()

		val json = """
        {
            "impressions_limit": ${faker.random.nextInt(100, 10000)},
            "clicks_limit": ${faker.random.nextInt(10, 1000)},
            "cost_per_impression": ${
			faker.random.nextInt(1, 20).toDouble()
		},
            "cost_per_click": ${
			faker.random.nextInt(1, 20).toDouble()
		},
            "ad_title": ${if (title is String) "\"$title\"" else title},
            "ad_text": ${if (text is String) "\"$text\"" else text},
            "start_date": ${faker.random.nextInt(1, 20)},
            "end_date": ${faker.random.nextInt(20, 40)},
            "targeting": {}
        }
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(json)
			.post("$serverUrl/advertisers/$advertiserId/campaigns")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@ParameterizedTest(name = "Start date: {0}, End date: {1}")
	@MethodSource("invalidDateProvider")
	@DisplayName("Should reject invalid dates")
	fun `reject advertisement with invalid dates`(startDate: Int, endDate: Int) {
		val advertiserId = UUID.randomUUID()

		given().contentType(ContentType.JSON).body(
			generator.createValidAdJson(
				startDate = startDate, endDate = endDate
			).trimIndent()
		).post("$serverUrl/advertisers/$advertiserId/campaigns").then().statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@Test
	@DisplayName("Should reject invalid gender")
	fun `reject advertisement with invalid gender in target`() {
		val advertiserId = UUID.randomUUID()

		val json = """
        {
            "impressions_limit": ${faker.random.nextInt(100, 10000)},
            "clicks_limit": ${faker.random.nextInt(10, 1000)},
            "cost_per_impression": ${
			faker.random.nextInt(1, 20).toDouble()
		},
            "cost_per_click": ${
			faker.random.nextInt(1, 20).toDouble()
		},
            "ad_title": "${faker.name.name()}",
            "ad_text": "${faker.lorem.words()}",
            "start_date": ${faker.random.nextInt(1, 20)},
            "end_date": ${faker.random.nextInt(20, 40)},
            "targeting": {
                "age_from": ${faker.random.nextInt(1, 50)},
                "age_to": ${faker.random.nextInt(50, 130)},
                "location": "${faker.address.country()}",
                "gender": "UNDEFINED"
            }
        }
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(json)
			.post("$serverUrl/advertisers/$advertiserId/campaigns")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@Test
	@DisplayName("Should reject invalid location")
	fun `reject advertisement with invalid location in target`() {
		val advertiserId = UUID.randomUUID()

		val json = """
        {
            "impressions_limit": ${faker.random.nextInt(100, 10000)},
            "clicks_limit": ${faker.random.nextInt(10, 1000)},
            "cost_per_impression": ${
			faker.random.nextInt(1, 20).toDouble()
		},
            "cost_per_click": ${
			faker.random.nextInt(1, 20).toDouble()
		},
            "ad_title": "${faker.name.name()}",
            "ad_text": "${faker.lorem.words()}",
            "start_date": ${faker.random.nextInt(1, 20)},
            "end_date": ${faker.random.nextInt(20, 40)},
            "targeting": {
                "age_from": ${faker.random.nextInt(1, 50)},
                "aget_to": ${faker.random.nextInt(50, 130)},
                "location": 12890341498,
                "gender": "${faker.random.nextEnum<Gender>()}"
            }
        }
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(json)
			.post("$serverUrl/advertisers/$advertiserId/campaigns")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@ParameterizedTest(name = "Age from: {0}")
	@MethodSource("invalidAgeFromProvider")
	@DisplayName("Should reject invalid age_from")
	fun `reject advertisement with invalid age_from in target`(ageFrom: Any) {
		val advertiserId = UUID.randomUUID()

		val json = """
        {
            "impressions_limit": ${faker.random.nextInt(100, 10000)},
            "clicks_limit": ${faker.random.nextInt(10, 1000)},
            "cost_per_impression": ${
			faker.random.nextInt(1, 20).toDouble()
		},
            "cost_per_click": ${
			faker.random.nextInt(1, 20).toDouble()
		},
            "ad_title": "${faker.name.name()}",
            "ad_text": "${faker.lorem.words()}",
            "start_date": ${faker.random.nextInt(1, 20)},
            "end_date": ${faker.random.nextInt(20, 40)},
            "targeting": {
                "age_from": ${
			when (ageFrom) {
				is String -> "\"$ageFrom\""
				else -> ageFrom
			}
		},
                "age_to": ${faker.random.nextInt(50, 130)},
                "location": "${faker.address.country()}",
                "gender": "${faker.random.nextEnum<Gender>()}"
            }
        }
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(json)
			.post("$serverUrl/advertisers/$advertiserId/campaigns")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@ParameterizedTest(name = "Age to: {0}")
	@MethodSource("invalidAgeToProvider")
	@DisplayName("Should reject invalid age_to")
	fun `reject advertisement with invalid age_to in target`(ageTo: Any) {
		val advertiserId = UUID.randomUUID()

		val json = """
        {
            "impressions_limit": ${faker.random.nextInt(100, 10000)},
            "clicks_limit": ${faker.random.nextInt(10, 1000)},
            "cost_per_impression": ${
			faker.random.nextInt(1, 20).toDouble()
		},
            "cost_per_click": ${
			faker.random.nextInt(1, 20).toDouble()
		},
            "ad_title": "${faker.name.name()}",
            "ad_text": "${faker.lorem.words()}",
            "start_date": ${faker.random.nextInt(1, 20)},
            "end_date": ${faker.random.nextInt(20, 40)},
            "targeting": {
                "age_from": ${faker.random.nextInt(1, 50)},
                "age_to": ${
			when (ageTo) {
				is String -> "\"$ageTo\""
				else -> ageTo
			}
		},
                "location": "${faker.address.country()}",
                "gender": "${faker.random.nextEnum<Gender>()}"
            }
        }
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(json)
			.post("$serverUrl/advertisers/$advertiserId/campaigns")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@Test
	@DisplayName("Should delete advertisement")
	fun `delete advertisement successfully`() {
		val advertiserId = UUID.randomUUID()

		val advertisement = generator.createAdvertisement(advertiserId, generator.createValidAdJson())

		val campaignId = advertisement["campaign_id"] ?: throw IllegalStateException("Campaign ID is null")
		verifyAdvertisementExists(advertiserId, campaignId)

		given().delete("$serverUrl/advertisers/$advertiserId/campaigns/${advertisement["campaign_id"]}")
			.then()
			.statusCode(HttpStatus.NO_CONTENT.value())
	}

	@Test
	@DisplayName("Should fail when deleting advertisement from other advertiser")
	fun `fail when deleting advertisement from other advertiser`() {
		val advertiserId = UUID.randomUUID()
		val otheradvertiserId = UUID.randomUUID()

		val advertisement = generator.createAdvertisement(advertiserId, generator.createValidAdJson())

		val campaignId = advertisement["campaign_id"] ?: throw IllegalStateException("Campaign ID is null")
		verifyAdvertisementExists(advertiserId, campaignId)

		verifyAdvertisementExists(advertiserId, campaignId)

		given().delete("$serverUrl/advertisers/$otheradvertiserId/campaigns/${advertisement["campaign_id"]}")
			.then()
			.statusCode(HttpStatus.FORBIDDEN.value())
	}

	@Test
	@DisplayName("Should fail when accessing advertisement from other advertiser")
	fun `fail when accessing advertisement from other advertiser`() {
		val advertiserId = UUID.randomUUID()
		val otherAdvertiserId = UUID.randomUUID()

		val advertisement = generator.createAdvertisement(advertiserId, generator.createValidAdJson())

		given().get("$serverUrl/advertisers/$otherAdvertiserId/campaigns/${advertisement["campaign_id"]}")
			.then()
			.statusCode(HttpStatus.FORBIDDEN.value())
	}

	@Test
	@DisplayName("Should return all advertiser advertisements")
	fun `return all advertiser advertisements`() {
		val advertiserId = UUID.randomUUID()
		val otherAdvertiserId = UUID.randomUUID()

		generator.createAdvertiser(advertiserId)
		generator.createAdvertiser(otherAdvertiserId)

		generator.createAdvertisement(advertiserId, generator.createValidAdJson())
		generator.createAdvertisement(advertiserId, generator.createValidAdJson())
		generator.createAdvertisement(advertiserId, generator.createValidAdJson())
		generator.createAdvertisement(otherAdvertiserId, generator.createValidAdJson())

		given().get("$serverUrl/advertisers/$advertiserId/campaigns")
			.then()
			.statusCode(HttpStatus.OK.value())
			.extract()
			.jsonPath()
			.getList<Map<String, Any>>("")

		val res2 = given().get("$serverUrl/advertisers/$otherAdvertiserId/campaigns")
			.then()
			.statusCode(HttpStatus.OK.value())
			.extract()
			.jsonPath()
			.getList<Map<String, Any>>("")

		assertEquals(1, res2.size)
	}
}
