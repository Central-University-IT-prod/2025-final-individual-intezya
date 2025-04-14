package com.intezya.solution.utils

import com.intezya.solution.enums.Gender
import io.github.serpro69.kfaker.Faker
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("Generate util")
class Generate(
	private val faker: Faker = Faker(),
	@LocalServerPort
	val port: Int,
) {
	private val serverUrl = "http://localhost:$port"

	fun createValidAdJson(
		impressionsLimit: Int = faker.random.nextInt(1000, 10000),
		clicksLimit: Int = faker.random.nextInt(10, 1000),
		costPerImpression: Double = faker.random.nextInt(1, 20).toDouble(),
		costPerClick: Double = faker.random.nextInt(1, 20).toDouble(),
		adTitle: String = faker.marketing.buzzwords(),
		adText: String = faker.marketing.buzzwords(),
		startDate: Int = faker.random.nextInt(1, 20),
		endDate: Int = faker.random.nextInt(20, 40),
		targeting: Map<String, Any>? = createDefaultTargeting(),
		imageUrl: String? = null,
	): String {
		val imageUrlSection = if (imageUrl != null) """, "image_url": "$imageUrl"""" else ""

		return """
        {
            "impressions_limit": $impressionsLimit,
            "clicks_limit": $clicksLimit,
            "cost_per_impression": $costPerImpression,
            "cost_per_click": $costPerClick,
            "ad_title": "$adTitle",
            "ad_text": "$adText",
            "start_date": $startDate,
            "end_date": $endDate,
            "targeting": ${targeting?.let { createTargetingJson(it) } ?: "{}"}$imageUrlSection
        }
        """
	}

	fun createDefaultTargeting(): Map<String, Any> = mapOf(
		"age_from" to faker.random.nextInt(1, 50),
		"age_to" to faker.random.nextInt(50, 130),
		"location" to faker.address.country(),
		"gender" to faker.random.nextEnum<Gender>().toString()
	)

	fun createTargetingJson(targeting: Map<String, Any>): String {
		if (targeting.isEmpty()) return "{}"

		val fields = targeting.entries.joinToString(", ") { (key, value) ->
			when (value) {
				is String -> "\"$key\": \"$value\""
				else -> "\"$key\": $value"
			}
		}

		return "{$fields}"
	}

	fun createAdvertisement(advertiserId: UUID, adJson: String): Map<String, Any> {
		return given().contentType(ContentType.JSON).body(adJson.trimIndent())
			.post("$serverUrl/advertisers/$advertiserId/campaigns").then().statusCode(HttpStatus.CREATED.value())
			.extract().body().jsonPath().getMap<String, Any>("")
	}

	fun createAdvertiser(advertiserId: UUID, name: String = faker.company.name()): Map<String, Any> {
		val advertiser = given().contentType(ContentType.JSON)
			.body("""[{"advertiser_id": "$advertiserId", "name": "$name"}]""".trimIndent())
			.post("$serverUrl/advertisers/bulk").then().statusCode(HttpStatus.CREATED.value()).extract().body()
			.jsonPath().getList<Map<String, Any>>("")

		return advertiser.first()
	}

	fun createClient(
		clientId: UUID = UUID.randomUUID(),
		login: String = faker.name.firstName(),
		age: Int = faker.random.nextInt(18, 80),
		location: String = faker.address.country(),
		gender: Gender = faker.random.nextEnum<Gender>(),
	): Map<String, Any> {
		val clients = given().contentType(ContentType.JSON).body(
			"""
                [{
                    "client_id": "$clientId",
                    "login": "$login",
                    "age": $age,
                    "location": "$location",
                    "gender": "$gender"
                }]
            """.trimIndent()
		).post("$serverUrl/clients/bulk").then().statusCode(HttpStatus.CREATED.value()).extract().body().jsonPath()
			.getList<Map<String, Any>>("")

		return clients.first()
	}

	fun createMLScoreJSON(
		clientId: UUID = UUID.randomUUID(),
		advertiserId: UUID = UUID.randomUUID(),
		score: Int = faker.random.nextInt(1, 100),
	): String {
		return """
        {
            "client_id": "$clientId",
            "advertiser_id": "$advertiserId",
            "score": $score
        }
        """
	}

	fun createMLScore(
		clientId: UUID = UUID.randomUUID(),
		advertiserId: UUID = UUID.randomUUID(),
		score: Int = faker.random.nextInt(1, 100),
	): Map<String, Any> {
		return given().contentType(ContentType.JSON).body(createMLScoreJSON(clientId, advertiserId, score).trimIndent())
			.post("$serverUrl/ml-scores").then().statusCode(HttpStatus.OK.value())  // Returning 200 as per spec
			.extract().body().jsonPath().getMap<String, Any>("")
	}
}
