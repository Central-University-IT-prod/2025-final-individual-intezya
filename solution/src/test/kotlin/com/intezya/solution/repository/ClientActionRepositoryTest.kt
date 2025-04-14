package com.intezya.solution.repository

import com.intezya.solution.entity.ClientAction
import com.intezya.solution.enums.ClientActionType
import io.github.serpro69.kfaker.Faker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import java.util.*
import kotlin.math.round
import kotlin.test.Test
import kotlin.test.assertNotNull


@SpringBootTest
@Import(TestPostgresConfiguration::class)
class ClientActionRepositoryTest {
	@Autowired
	private lateinit var clientActionRepository: ClientActionRepository
	private val faker: Faker = Faker()

	@Test
	fun `should insert advertisement client action query`() {
		val clientId = UUID.randomUUID()

		val action1 = generateRandomClientActionByClientId(clientId)
		val action2 = generateRandomClientActionByClientId(clientId)
		val action3 = generateRandomClientActionByClientId(clientId)
		val action4 = generateRandomClientActionByClientId(clientId)

		insertClientAction(action1)
		insertClientAction(action2)
		insertClientAction(action3)
		insertClientAction(action4)

		val found1 = clientActionRepository.findById(action1.id).orElse(null)
		val found2 = clientActionRepository.findById(action2.id).orElse(null)
		val found3 = clientActionRepository.findById(action3.id).orElse(null)
		val found4 = clientActionRepository.findById(action4.id).orElse(null)

		assertNotNull(found1)
		assertNotNull(found2)
		assertNotNull(found3)
		assertNotNull(found4)

		assert(action1.createdAt == found1.createdAt)
		assert(action2.createdAt == found2.createdAt)
		assert(action3.createdAt == found3.createdAt)
		assert(action4.createdAt == found4.createdAt)

		assert(action4.advertisementId == found4.advertisementId)
		assert(action4.advertisementId == found4.advertisementId)
		assert(action4.advertisementId == found4.advertisementId)
		assert(action4.advertisementId == found4.advertisementId)

		val foundByClientId = clientActionRepository.findByClientId(clientId)

		assert(foundByClientId.size == 4)
	}

	@Test
	fun `should sum impressions cost by advertiserId`() {
		val advertiserId = UUID.randomUUID()

		val action1 = generateRandomClientActionByadvertiserId(advertiserId, actionType = ClientActionType.IMPRESSION)
		val action2 = generateRandomClientActionByadvertiserId(advertiserId, actionType = ClientActionType.IMPRESSION)
		val action3 = generateRandomClientActionByadvertiserId(advertiserId, actionType = ClientActionType.IMPRESSION)
		val action4 = generateRandomClientActionByadvertiserId(advertiserId, actionType = ClientActionType.IMPRESSION)

		val impressionsCost = action1.cost + action2.cost + action3.cost + action4.cost

		insertClientAction(action1)
		insertClientAction(action2)
		insertClientAction(action3)
		insertClientAction(action4)

		val summedCost = clientActionRepository.sumImpressionsCost(advertiserId)

		assert(round(impressionsCost) == round(summedCost))
	}

	@Test
	fun `should sum clicks cost by advertiserId`() {
		val advertiserId = UUID.randomUUID()

		val action1 = generateRandomClientActionByadvertiserId(advertiserId, actionType = ClientActionType.CLICK)
		val action2 = generateRandomClientActionByadvertiserId(advertiserId, actionType = ClientActionType.CLICK)
		val action3 = generateRandomClientActionByadvertiserId(advertiserId, actionType = ClientActionType.CLICK)
		val action4 = generateRandomClientActionByadvertiserId(advertiserId, actionType = ClientActionType.CLICK)

		val clicksCost = action1.cost + action2.cost + action3.cost + action4.cost

		insertClientAction(action1)
		insertClientAction(action2)
		insertClientAction(action3)
		insertClientAction(action4)

		val summedCost = clientActionRepository.sumClicksCost(advertiserId)

		assert(round(clicksCost) == round(summedCost))
	}

	private fun generateRandomClientAction(
		advertiserId: UUID,
		clientId: UUID,
		cost: Double,
		actionType: ClientActionType,
	): ClientAction {
		val advertisementId = UUID.randomUUID()
		return ClientAction(
			id = ClientAction.constructId(clientId, advertisementId, actionType),
			clientId = clientId,
			advertisementId = advertisementId,
			advertiserId = advertiserId,
			actionType = actionType,
			cost = cost,
			createdAt = faker.random.nextInt(),
		)
	}

	//	private fun generateRandomClientAction(advertiserId: UUID, cost: Double): ClientAction {
	//		return generateRandomClientAction(advertiserId, UUID.randomUUID(), cost)
	//	}
	private fun generateRandomClientActionByadvertiserId(
		advertiserId: UUID,
		actionType: ClientActionType,
	): ClientAction {
		return generateRandomClientAction(advertiserId, UUID.randomUUID(), faker.random.nextDouble(), actionType)
	}

	private fun generateRandomClientActionByAdvertiserId(advertiserId: UUID): ClientAction {
		return generateRandomClientAction(
			advertiserId, UUID.randomUUID(), faker.random.nextDouble(), faker.random.nextEnum<ClientActionType>()
		)
	}

	private fun generateRandomClientActionByClientId(clientId: UUID): ClientAction {
		return generateRandomClientAction(
			UUID.randomUUID(), clientId, faker.random.nextDouble(), faker.random.nextEnum<ClientActionType>()
		)
	}

	private fun insertClientAction(clientAction: ClientAction) {
		clientActionRepository.insert(
			clientAction.id,
			clientAction.clientId,
			clientAction.advertisementId,
			clientAction.advertiserId,
			clientAction.actionType,
			clientAction.cost,
			clientAction.createdAt
		)
	}
}
