package com.intezya.solution.repository

import com.intezya.solution.entity.MLScore
import io.github.serpro69.kfaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.util.*
import kotlin.test.Test
import kotlin.test.assertNotNull

@SpringBootTest
@Import(TestPostgresConfiguration::class)
class MLScoreRepositoryTest {
	@Autowired
	private lateinit var mlScoreRepository: MLScoreRepository
	private val faker = Faker()

	@Test
	fun `should upsert and find ml scores by id`() {
		val mlScore = generateRandomMlScore()

		upsert(mlScore)

		val found = mlScoreRepository.findById(mlScore.id).orElse(null)

		assertNotNull(found)
		assert(mlScore.score == found.score)

		val updatedMlScore = updateScore(mlScore, faker.random.nextInt(1, 100))

		upsert(updatedMlScore)


		val updatedFound = mlScoreRepository.findById(updatedMlScore.id).orElse(null)

		assertNotNull(updatedFound)
		assert(updatedMlScore.score == updatedFound.score)
	}

	private fun generateRandomMlScore(): MLScore {
		val clientId = UUID.randomUUID()
		val advertiserId = UUID.randomUUID()
		return MLScore(
			id = MLScore.constructId(clientId, advertiserId),
			clientId = clientId,
			advertiserId = advertiserId,
			score = faker.random.nextInt(1, 100),
		)
	}

	private fun updateScore(mlScore: MLScore, toSet: Int): MLScore {
		return MLScore(
			id = mlScore.id, clientId = mlScore.clientId, advertiserId = mlScore.advertiserId, score = toSet
		)
	}

	private fun upsert(mlScore: MLScore) {
		mlScoreRepository.upsert(
			mlScore.id,
			mlScore.clientId,
			mlScore.advertiserId,
			mlScore.score,
		)
	}
}
