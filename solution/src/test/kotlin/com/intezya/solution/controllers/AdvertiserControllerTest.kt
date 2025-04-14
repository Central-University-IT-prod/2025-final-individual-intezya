package com.intezya.solution.controllers

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
@DisplayName("Advertiser Controller Tests")
class AdvertiserControllerTest(
	@LocalServerPort
	val port: Int,
) {
	private val faker = Faker()
	private val generator: Generate = Generate(port = port)
	private val serverUrl = "http://localhost:$port"

	companion object {
		@JvmStatic
		fun invalidAdvertiserIdProvider(): Stream<Arguments> = Stream.of(
			Arguments.of("qqqqqqqqqqqqq"), Arguments.of(""), Arguments.of("not-a-uuid"), Arguments.of(123)
		)

		@JvmStatic
		fun invalidNameProvider(): Stream<Arguments> = Stream.of(
			Arguments.of(""), Arguments.of(123), Arguments.of(true), Arguments.of(mapOf("key" to "value"))
		)
	}

	private fun verifyAdvertiserExists(advertiserId: UUID) {
		given().get("$serverUrl/advertisers/$advertiserId")
			.then()
			.statusCode(HttpStatus.OK.value())
			.body("advertiser_id", equalTo(advertiserId.toString()))
	}

	@Test
	@DisplayName("Should create valid advertiser")
	fun `create advertiser with valid data`() {
		val advertiserId = UUID.randomUUID()
		val name = faker.company.name()

		val advertisers = given().contentType(ContentType.JSON)
			.body("""[{"advertiser_id": "$advertiserId", "name": "$name"}]""".trimIndent())
			.post("$serverUrl/advertisers/bulk")
			.then()
			.statusCode(HttpStatus.CREATED.value())
			.extract()
			.body()
			.jsonPath()
			.getList<Map<String, Any>>("")

		val advertiser = advertisers.first()

		given().get("$serverUrl/advertisers/${advertiser["advertiser_id"]}")
			.then()
			.statusCode(HttpStatus.OK.value())
			.body("advertiser_id", equalTo(advertiser["advertiser_id"]))
			.body("name", equalTo(advertiser["name"]))
	}

	@Test
	@DisplayName("Should create multiple valid advertisers")
	fun `create multiple valid advertisers`() {
		val advertisersCount = 10
		val advertisersJson = (1..advertisersCount).joinToString(",") {
			"""
            {
                "advertiser_id": "${UUID.randomUUID()}",
                "name": "${faker.company.name()}"
            }
            """.trimIndent()
		}
		val body = "[$advertisersJson]"

		val advertisers = given().contentType(ContentType.JSON)
			.body(body)
			.post("$serverUrl/advertisers/bulk")
			.then()
			.statusCode(HttpStatus.CREATED.value())
			.extract()
			.body()
			.jsonPath()
			.getList<Map<String, Any>>("")

		assertEquals(advertisersCount, advertisers.size)

		advertisers.forEach { advertiser ->
			verifyAdvertiserExists(UUID.fromString(advertiser["advertiser_id"].toString()))
		}
	}

	@ParameterizedTest(name = "Advertiser ID: {0}")
	@MethodSource("invalidAdvertiserIdProvider")
	@DisplayName("Should reject advertiser with invalid ID")
	fun `reject advertiser with invalid id`(advertiserId: Any) {
		val body = """
        [{
            "advertiser_id": ${if (advertiserId is String) "\"$advertiserId\"" else advertiserId},
            "name": "${faker.company.name()}"
        }]
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(body)
			.post("$serverUrl/advertisers/bulk")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@ParameterizedTest(name = "Name: {0}")
	@MethodSource("invalidNameProvider")
	@DisplayName("Should reject advertiser with invalid name")
	fun `reject advertiser with invalid name`(name: Any) {
		val body = """
        [{
            "advertiser_id": "${UUID.randomUUID()}",
            "name": ${if (name is String) "\"$name\"" else name}
        }]
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(body)
			.post("$serverUrl/advertisers/bulk")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@Test
	@DisplayName("Should handle empty advertiser list")
	fun `handle empty advertiser list`() {
		given().contentType(ContentType.JSON)
			.body("[]")
			.post("$serverUrl/advertisers/bulk")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@Test
	@DisplayName("Should handle non-existent advertiser retrieval")
	fun `handle non-existent advertiser retrieval`() {
		val nonExistentId = UUID.randomUUID()

		given().get("$serverUrl/advertisers/$nonExistentId").then().statusCode(HttpStatus.NOT_FOUND.value())
	}

	@Test
	@DisplayName("Should create all advertiser")
	fun `create all advertiser`() {
		val advertiserCount = 5
		for (i in 1..advertiserCount) {
			generator.createAdvertiser(UUID.randomUUID())
		}
	}
}
