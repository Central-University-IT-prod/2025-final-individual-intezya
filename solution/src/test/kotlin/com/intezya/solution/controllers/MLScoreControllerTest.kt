package com.intezya.solution.controllers

import com.intezya.solution.utils.Generate
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ExtendWith(SpringExtension::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("MLScore Controller Tests")
class MLScoreControllerTest(
	@LocalServerPort
	val port: Int,
) {
	private val generator: Generate = Generate(port = port)
	private val serverUrl = "http://localhost:$port"

	@Test
	@DisplayName("Should create ML score with valid data")
	fun `create ML score with valid data`() {
		val clientId = UUID.randomUUID()
		val advertiserId = UUID.randomUUID()

		generator.createClient(clientId)
		generator.createAdvertiser(advertiserId)

		val mlScore = generator.createMLScore(clientId, advertiserId)

		assertNotNull(mlScore["client_id"])
		assertNotNull(mlScore["advertiser_id"])
		assertNotNull(mlScore["score"])

		assertEquals(clientId.toString(), mlScore["client_id"])
		assertEquals(advertiserId.toString(), mlScore["advertiser_id"])
	}

	@Test
	@DisplayName("Should fail when client doesn't exist")
	fun `fail when client doesn't exist`() {
		val nonExistentClientId = UUID.randomUUID()
		val advertiserId = UUID.randomUUID()

		generator.createAdvertiser(advertiserId)

		given().contentType(ContentType.JSON).body(
			generator.createMLScoreJSON(nonExistentClientId, advertiserId).trimIndent()
		).post("$serverUrl/ml-scores").then().statusCode(HttpStatus.NOT_FOUND.value())
	}

	@Test
	@DisplayName("Should fail when advertiser doesn't exist")
	fun `fail when advertiser doesn't exist`() {
		val clientId = UUID.randomUUID()
		val nonExistentAdvertiserId = UUID.randomUUID()

		generator.createClient(clientId)

		given().contentType(ContentType.JSON)
			.body(
				generator.createMLScoreJSON(clientId, nonExistentAdvertiserId).trimIndent()
			)
			.post("$serverUrl/ml-scores")
			.then()
			.statusCode(HttpStatus.NOT_FOUND.value())
			.extract()
			.body()
			.jsonPath()
			.getMap<String, Any>("")
	}

	@Test
	@DisplayName("Should reject invalid score value")
	fun `reject invalid score value`() {
		val clientId = UUID.randomUUID()
		val advertiserId = UUID.randomUUID()

		generator.createClient(clientId)
		generator.createAdvertiser(advertiserId)

		val invalidScoreJson = """
        {
            "client_id": "$clientId",
            "advertiser_id": "$advertiserId",
            "score": -10
        }
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(invalidScoreJson)
			.post("$serverUrl/ml-scores")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@Test
	@DisplayName("Should reject invalid client ID format")
	fun `reject invalid client ID format`() {
		val advertiserId = UUID.randomUUID()

		generator.createAdvertiser(advertiserId)

		val invalidClientIdJson = """
        {
            "client_id": "not-a-uuid",
            "advertiser_id": "$advertiserId",
            "score": 50
        }
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(invalidClientIdJson)
			.post("$serverUrl/ml-scores")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@Test
	@DisplayName("Should reject invalid advertiser ID format")
	fun `reject invalid advertiser ID format`() {
		val clientId = UUID.randomUUID()

		generator.createClient(clientId)

		val invalidAdvertiserIdJson = """
        {
            "client_id": "$clientId",
            "advertiser_id": "not-a-uuid",
            "score": 50
        }
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(invalidAdvertiserIdJson)
			.post("$serverUrl/ml-scores")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@Test
	@DisplayName("Should reject request with missing fields")
	fun `reject request with missing fields`() {
		val clientId = UUID.randomUUID()
		val advertiserId = UUID.randomUUID()

		generator.createClient(clientId)
		generator.createAdvertiser(advertiserId)

		val missingScoreJson = """
        {
            "client_id": "$clientId",
            "advertiser_id": "$advertiserId"
        }
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(missingScoreJson)
			.post("$serverUrl/ml-scores")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@Test
	@DisplayName("Should accept valid score values at boundaries")
	fun `accept valid score values at boundaries`() {
		val clientId = UUID.randomUUID()
		val advertiserId = UUID.randomUUID()

		generator.createClient(clientId)
		generator.createAdvertiser(advertiserId)

		val lowerBoundaryJson = """
        {
            "client_id": "$clientId",
            "advertiser_id": "$advertiserId",
            "score": 0
        }
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(lowerBoundaryJson)
			.post("$serverUrl/ml-scores")
			.then()
			.statusCode(HttpStatus.OK.value())

		// not upper boundary
		val upperBoundaryJson = """
        {
            "client_id": "$clientId",
            "advertiser_id": "$advertiserId",
            "score": 100
        }
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(upperBoundaryJson)
			.post("$serverUrl/ml-scores")
			.then()
			.statusCode(HttpStatus.OK.value())
	}
}
