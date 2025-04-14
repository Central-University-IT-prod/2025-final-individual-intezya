package com.intezya.solution.services

import com.intezya.solution.dto.ClientRequest
import com.intezya.solution.enums.Gender
import com.intezya.solution.repository.ClientRepository
import io.github.serpro69.kfaker.Faker
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.web.server.ResponseStatusException
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(MockKExtension::class)
class ClientServiceTest {
	@MockK
	private lateinit var clientRepository: ClientRepository

	@InjectMockKs
	private lateinit var clientService: ClientService

	private val faker = Faker()

	@BeforeEach
	fun setUp() {
		MockKAnnotations.init(this)
	}

	@Test
	fun `should create clients`() {
		val clientRequest1 = generateRandomClientRequest()
		val clientRequest2 = generateRandomClientRequest()
		val clientRequest3 = generateRandomClientRequest()
		val clientRequest4 = generateRandomClientRequest()
		val clientRequest5 = generateRandomClientRequest()

		val clients = listOf(
			clientRequest1,
			clientRequest2,
			clientRequest3,
			clientRequest4,
			clientRequest5,
		)

		val clientEntities = clients.map { it.toEntity() }
		every { clientRepository.bulkUpsert(any()) } just Runs


		clientService.createOrUpdate(clients)

		verify { clientRepository.bulkUpsert(clientEntities) }
	}

	@Test
	fun `should find client by id`() {
		val clientRequest = generateRandomClientRequest()
		val clientEntity = clientRequest.toEntity()

		every { clientRepository.findById(clientRequest.id) } returns Optional.of(clientEntity)

		val result = clientService.findById(clientRequest.id)

		assertNotNull(result)
		assertEquals(clientRequest.id, result.id)
		assertEquals(clientRequest.login, result.login)
		assertEquals(clientRequest.age, result.age)
		assertEquals(clientRequest.location, result.location)
		assertEquals(clientRequest.gender, result.gender)

		verify { clientRepository.findById(clientRequest.id) }
	}

	@Test
	fun `should throw exception on client not found by id`() {
		val nonExistentId = UUID.randomUUID()

		every { clientRepository.findById(nonExistentId) } returns Optional.empty()

		assertThrows<ResponseStatusException> {
			clientService.findById(nonExistentId)
		}

		verify { clientRepository.findById(nonExistentId) }
	}

	private fun generateRandomClientRequest(): ClientRequest {
		return ClientRequest(
			id = UUID.randomUUID(),
			login = faker.name.toString(),
			age = faker.random.nextInt(1, 100),
			location = faker.address.city(),
			gender = faker.random.nextEnum<Gender>(),
		)
	}
}
