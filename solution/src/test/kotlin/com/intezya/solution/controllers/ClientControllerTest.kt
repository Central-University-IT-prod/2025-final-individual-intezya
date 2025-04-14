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
@DisplayName("Client Controller Tests")
class ClientControllerTest(
	@LocalServerPort
	val port: Int,
) {
	private val faker = Faker()
	private val generator: Generate = Generate(port = port)
	private val serverUrl = "http://localhost:$port"

	companion object {
		@JvmStatic
		fun invalidClientIdProvider(): Stream<Arguments> = Stream.of(
			Arguments.of("6alkfa"), Arguments.of(""), Arguments.of("not-a-uuid"), Arguments.of(123)
		)

		@JvmStatic
		fun invalidLoginProvider(): Stream<Arguments> = Stream.of(
			Arguments.of(""), Arguments.of(mapOf("key" to "value"))
		)

		@JvmStatic
		fun invalidAgeProvider(): Stream<Arguments> = Stream.of(
			Arguments.of(-17), Arguments.of(999), Arguments.of(null)
		)

		@JvmStatic
		fun invalidLocationProvider(): Stream<Arguments> = Stream.of(
			Arguments.of(123222), Arguments.of(true), Arguments.of(null), Arguments.of(mapOf("key" to "value"))
		)

		@JvmStatic
		fun invalidGenderProvider(): Stream<Arguments> = Stream.of(
			Arguments.of("?????"), Arguments.of(14), Arguments.of(true), Arguments.of(null)
		)
	}

	private fun verifyClientExists(clientId: UUID) {
		given().get("$serverUrl/clients/$clientId")
			.then()
			.statusCode(HttpStatus.OK.value())
			.body("client_id", equalTo(clientId.toString()))
	}

	@Test
	@DisplayName("Should create valid client")
	fun `create client with valid data`() {
		val clientId = UUID.randomUUID()
		val login = faker.name.firstName()
		val age = faker.random.nextInt(18, 80)
		val location = faker.address.country()
		val gender = faker.random.nextEnum<Gender>()

		val clients = given().contentType(ContentType.JSON)
			.body(
				"""
                [{
                    "client_id": "$clientId",
                    "login": "$login",
                    "age": $age,
                    "location": "$location",
                    "gender": "$gender"
                }]
            """.trimIndent()
			)
			.post("$serverUrl/clients/bulk")
			.then()
			.statusCode(HttpStatus.CREATED.value())
			.extract()
			.body()
			.jsonPath()
			.getList<Map<String, Any>>("")

		val client = clients.first()

		given().get("$serverUrl/clients/${client["client_id"]}")
			.then()
			.statusCode(HttpStatus.OK.value())
			.body("client_id", equalTo(client["client_id"]))
			.body("login", equalTo(client["login"]))
			.body("age", equalTo(client["age"]))
			.body("location", equalTo(client["location"]))
			.body("gender", equalTo(client["gender"]))
	}

	@Test
	@DisplayName("Should create multiple valid clients")
	fun `create multiple valid clients`() {
		val clientCount = 10
		val clientsJson = (1..clientCount).joinToString(",") {
			"""
            {
                "client_id": "${UUID.randomUUID()}",
                "login": "${faker.name.firstName()}",
                "age": ${faker.random.nextInt(0, 130)},
                "location": "${faker.address.country()}",
                "gender": "${faker.random.nextEnum<Gender>()}"
            }
            """.trimIndent()
		}
		val body = "[$clientsJson]"

		val clients = given().contentType(ContentType.JSON)
			.body(body)
			.post("$serverUrl/clients/bulk")
			.then()
			.statusCode(HttpStatus.CREATED.value())
			.extract()
			.body()
			.jsonPath()
			.getList<Map<String, Any>>("")

		assertEquals(clientCount, clients.size)

		clients.forEach { client ->
			verifyClientExists(UUID.fromString(client["client_id"].toString()))
		}
	}

	@ParameterizedTest(name = "Client ID: {0}")
	@MethodSource("invalidClientIdProvider")
	@DisplayName("Should reject client with invalid ID")
	fun `reject client with invalid id`(clientId: Any) {
		val body = """
        [{
            "client_id": ${if (clientId is String) "\"$clientId\"" else clientId},
            "login": "${faker.name.firstName()}",
            "age": ${faker.random.nextInt(0, 130)},
            "location": "${faker.address.country()}",
            "gender": "${faker.random.nextEnum<Gender>()}"
        }]
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(body)
			.post("$serverUrl/clients/bulk")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@ParameterizedTest(name = "Login: {0}")
	@MethodSource("invalidLoginProvider")
	@DisplayName("Should reject client with invalid login")
	fun `reject client with invalid login`(login: Any) {
		val body = """
        [{
            "client_id": "${UUID.randomUUID()}",
            "login": ${if (login is String) "\"$login\"" else login},
            "age": ${faker.random.nextInt(0, 130)},
            "location": "${faker.address.country()}",
            "gender": "${faker.random.nextEnum<Gender>()}"
        }]
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(body)
			.post("$serverUrl/clients/bulk")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@ParameterizedTest(name = "Age: {0}")
	@MethodSource("invalidAgeProvider")
	@DisplayName("Should reject client with invalid age")
	fun `reject client with invalid age`(age: Any?) {
		val body = """
        [{
            "client_id": "${UUID.randomUUID()}",
            "login": "${faker.name.firstName()}",
            "age": ${age ?: "null"},
            "location": "${faker.address.country()}",
            "gender": "${faker.random.nextEnum<Gender>()}"
        }]
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(body)
			.post("$serverUrl/clients/bulk")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@ParameterizedTest(name = "Location: {0}")
	@MethodSource("invalidLocationProvider")
	@DisplayName("Should reject client with invalid location")
	fun `reject client with invalid location`(location: Any?) {
		val body = """
        [{
            "client_id": "${UUID.randomUUID()}",
            "login": "${faker.name.firstName()}",
            "age": ${faker.random.nextInt(0, 130)},
            "location": ${
			when (location) {
				is String -> "\"$location\""
				null -> "null"
				else -> location
			}
		},
            "gender": "${faker.random.nextEnum<Gender>()}"
        }]
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(body)
			.post("$serverUrl/clients/bulk")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@ParameterizedTest(name = "Gender: {0}")
	@MethodSource("invalidGenderProvider")
	@DisplayName("Should reject client with invalid gender")
	fun `reject client with invalid gender`(gender: Any?) {
		val body = """
        [{
            "client_id": "${UUID.randomUUID()}",
            "login": "${faker.name.firstName()}",
            "age": ${faker.random.nextInt(0, 130)},
            "location": "${faker.address.country()}",
            "gender": ${
			when (gender) {
				is String -> "\"$gender\""
				null -> "null"
				else -> gender
			}
		}
        }]
        """.trimIndent()

		given().contentType(ContentType.JSON)
			.body(body)
			.post("$serverUrl/clients/bulk")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@Test
	@DisplayName("Should handle empty client list")
	fun `handle empty client list`() {
		given().contentType(ContentType.JSON)
			.body("[]")
			.post("$serverUrl/clients/bulk")
			.then()
			.statusCode(HttpStatus.BAD_REQUEST.value())
	}

	@Test
	@DisplayName("Should handle non-existent client retrieval")
	fun `handle non-existent client retrieval`() {
		val nonExistentId = UUID.randomUUID()

		given().get("$serverUrl/clients/$nonExistentId").then().statusCode(HttpStatus.NOT_FOUND.value())
	}

	@Test
	@DisplayName("create retrieve all clients")
	fun `create all clients`() {
		val clientCount = 5
		for (i in 1..clientCount) {
			generator.createClient()
		}
	}

	@Test
	@DisplayName("Should handle edge case of maximum valid age")
	fun `handle edge case of maximum valid age`() {
		val clientId = UUID.randomUUID()
		val maxValidAge = 130  // Assuming 130 is the maximum valid age as per validation rules

		val clients = given().contentType(ContentType.JSON)
			.body(
				"""
                [{
                    "client_id": "$clientId",
                    "login": "${faker.name.firstName()}",
                    "age": $maxValidAge,
                    "location": "${faker.address.country()}",
                    "gender": "${faker.random.nextEnum<Gender>()}"
                }]
            """.trimIndent()
			)
			.post("$serverUrl/clients/bulk")
			.then()
			.statusCode(HttpStatus.CREATED.value())
			.extract()
			.body()
			.jsonPath()
			.getList<Map<String, Any>>("")

		val client = clients.first()
		assertEquals(maxValidAge, client["age"])
	}

	@Test
	@DisplayName("Should handle edge case of minimum valid age")
	fun `handle edge case of minimum valid age`() {
		val clientId = UUID.randomUUID()
		val minValidAge = 0  // Assuming 0 is the minimum valid age as per validation rules

		val clients = given().contentType(ContentType.JSON)
			.body(
				"""
                [{
                    "client_id": "$clientId",
                    "login": "${faker.name.firstName()}",
                    "age": $minValidAge,
                    "location": "${faker.address.country()}",
                    "gender": "${faker.random.nextEnum<Gender>()}"
                }]
            """.trimIndent()
			)
			.post("$serverUrl/clients/bulk")
			.then()
			.statusCode(HttpStatus.CREATED.value())
			.extract()
			.body()
			.jsonPath()
			.getList<Map<String, Any>>("")

		val client = clients.first()
		assertEquals(minValidAge, client["age"])
	}
}
