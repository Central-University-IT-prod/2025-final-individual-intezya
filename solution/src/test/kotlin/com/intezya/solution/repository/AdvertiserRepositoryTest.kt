package com.intezya.solution.repository

import com.intezya.solution.entity.Advertiser
import io.github.serpro69.kfaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.util.*
import kotlin.test.Test
import kotlin.test.assertNotNull

@SpringBootTest
@Import(TestPostgresConfiguration::class)
class AdvertiserRepositoryTest {
	@Autowired
	private lateinit var advertiserRepository: AdvertiserRepository
	private val faker = Faker()


	@Test
	fun `should upsert and find advertisers by id`() {
		val advertiser1 = generateRandomAdvertiser()
		val advertiser2 = generateRandomAdvertiser()

		advertiserRepository.bulkUpsert(listOf(advertiser1, advertiser2))

		val found1 = advertiserRepository.findById(advertiser1.id).orElse(null)
		val found2 = advertiserRepository.findById(advertiser2.id).orElse(null)

		assertNotNull(found1)
		assertNotNull(found2)

		assert(advertiser1.name == found1.name)
		assert(advertiser2.name == found2.name)

		val updatedAdvertiser1 = generateRandomAdvertiser()
		val updatedAdvertiser2 = generateRandomAdvertiser()

		advertiserRepository.bulkUpsert(listOf(updatedAdvertiser1, updatedAdvertiser2))

		val updatedFound1 = advertiserRepository.findById(updatedAdvertiser1.id).orElse(null)
		val updatedFound2 = advertiserRepository.findById(updatedAdvertiser2.id).orElse(null)

		assertNotNull(updatedFound1)
		assertNotNull(updatedFound2)

		assert(updatedAdvertiser1.name == updatedFound1.name)
		assert(updatedAdvertiser2.name == updatedFound2.name)
	}

	private fun generateRandomAdvertiser(): Advertiser {
		return Advertiser(
			id = UUID.randomUUID(),
			name = faker.name.toString(),
		)
	}
}
