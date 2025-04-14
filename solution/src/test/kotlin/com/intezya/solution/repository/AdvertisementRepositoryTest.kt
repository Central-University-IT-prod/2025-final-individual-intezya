package com.intezya.solution.repository

import com.intezya.solution.entity.Advertisement
import com.intezya.solution.enums.Gender
import com.intezya.solution.enums.TargetingGender
import io.github.serpro69.kfaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.util.*
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@Import(TestPostgresConfiguration::class)
class AdvertisementRepositoryTest {
	@Autowired
	private lateinit var advertisementRepository: AdvertisementRepository
	private val faker = Faker()

	@Test
	fun `should find created eligible advertisement by targeting`() {
		var advertisement = generateRandomAdvertisement()
		advertisement = advertisementRepository.save(advertisement)

		val found = advertisementRepository.findEligibleAdvertisements(
			age = advertisement.targetingAgeFrom!!,
			location = advertisement.targetingLocation!!,
			gender = Gender.MALE,
			currentDate = advertisement.startDate,
		)

		assertNotNull(found)
		assert(found.size == 1)

		assert(found[0].id == advertisement.id)
		assert(found[0].text == advertisement.text)
	}

	@Test
	fun `shouldn't find created non-eligible advertisement by targeting`() {
		val advertisement = generateRandomAdvertisement()
		advertisementRepository.save(advertisement)

		val found = advertisementRepository.findEligibleAdvertisements(
			age = -1000,
			location = faker.random.randomString(32),
			gender = Gender.FEMALE,
			currentDate = -100,
		)

		assert(found.isEmpty())
	}

	@Test
	fun `should increment impressions of created advertisement`() {
		var advertisement = generateRandomAdvertisement()
		advertisement = advertisementRepository.save(advertisement)

		val modifiedCount = advertisementRepository.incrementImpressions(advertisement.id!!)
		val found = advertisementRepository.findById(advertisement.id).orElse(null)

		assertNotNull(found)
		assertNotNull(modifiedCount == 1)
		assert(advertisement.currentImpressions + 1 == found.currentImpressions)
	}

	@Test
	fun `shouldn't increment impressions of not created advertisement`() {
		val modifiedCount = advertisementRepository.incrementImpressions(UUID.randomUUID())
		assertNotNull(modifiedCount == 0)
	}

	@Test
	fun `should increment clicks of created advertisement`() {
		var advertisement = generateRandomAdvertisement()
		advertisement = advertisementRepository.save(advertisement)

		val modifiedCount = advertisementRepository.incrementClicks(advertisement.id!!)
		val found = advertisementRepository.findById(advertisement.id).orElse(null)

		assertNotNull(found)
		assertNotNull(modifiedCount == 1)
		assert(advertisement.currentClicks + 1 == found.currentClicks)
	}

	@Test
	fun `shouldn't increment clicks of not created advertisement`() {
		val modifiedCount = advertisementRepository.incrementClicks(UUID.randomUUID())
		assertNotNull(modifiedCount == 0)
	}

	@Test
	fun `should not found not created advertisement`() {
		val found = advertisementRepository.findById(UUID.randomUUID()).orElse(null)

		assertNull(found)
	}

	private fun generateRandomAdvertisement(): Advertisement {
		val ageFrom = faker.random.nextInt(0, 50)
		val ageTo = faker.random.nextInt(ageFrom, ageFrom + 50)

		val startDate = faker.random.nextInt(0, 50)
		val endDate = faker.random.nextInt(startDate, startDate + 50)

		return Advertisement(
			advertiserId = UUID.randomUUID(),
			title = faker.random.randomString(length = 32),
			text = faker.random.randomString(length = 128),
			impressionsLimit = faker.random.nextInt(10, 1000),
			clicksLimit = faker.random.nextInt(10, 1000),
			costPerImpression = faker.random.nextDouble(),
			costPerClick = faker.random.nextDouble(),
			targetingGender = TargetingGender.MALE,
			targetingAgeFrom = ageFrom,
			targetingAgeTo = ageTo,
			targetingLocation = faker.address.city(),
			startDate = startDate,
			endDate = endDate,
		)
	}
}
