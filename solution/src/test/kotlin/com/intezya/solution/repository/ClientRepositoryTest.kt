package com.intezya.solution.repository

import com.intezya.solution.entity.Client
import com.intezya.solution.enums.Gender
import io.github.serpro69.kfaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.util.*
import kotlin.test.Test
import kotlin.test.assertNotNull

@SpringBootTest
@Import(TestPostgresConfiguration::class)
class ClientRepositoryTest {
	@Autowired
	private lateinit var clientRepository: ClientRepository
	private val faker = Faker()

	@Test
	fun `should upsert and find clients by id`() {
		val client1 = generateRandomClient()
		val client2 = generateRandomClient()

		clientRepository.bulkUpsert(listOf(client1, client2))

		val found1 = clientRepository.findById(client1.id).orElse(null)
		val found2 = clientRepository.findById(client2.id).orElse(null)

		assertNotNull(found1)
		assertNotNull(found2)

		assert(client1.login == found1.login)
		assert(client2.login == found2.login)

		val updatedClient1 = generateRandomClient()
		val updatedClient2 = generateRandomClient()

		clientRepository.bulkUpsert(listOf(updatedClient1, updatedClient2))

		val updatedFound1 = clientRepository.findById(updatedClient1.id).orElse(null)
		val updatedFound2 = clientRepository.findById(updatedClient2.id).orElse(null)

		assertNotNull(updatedFound1)
		assertNotNull(updatedFound2)

		assert(updatedClient1.login == updatedFound1.login)
		assert(updatedClient2.login == updatedFound2.login)
	}

	private fun generateRandomClient(): Client {
		return Client(
			id = UUID.randomUUID(),
			login = faker.name.toString(),
			age = faker.random.nextInt(1, 100),
			location = faker.address.city(),
			gender = faker.random.nextEnum<Gender>(),
		)
	}
}
