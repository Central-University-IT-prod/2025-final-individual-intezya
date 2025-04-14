package com.intezya.solution.services

import com.intezya.solution.entity.Advertisement
import com.intezya.solution.entity.ClientAction
import com.intezya.solution.enums.ClientActionType
import com.intezya.solution.repository.ClientActionRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class ClientActionService(
	private val clientActionRepository: ClientActionRepository,
	private val globalSettingsService: GlobalSettingsService,
) {
	fun getClientImpressions(clientId: UUID): Set<UUID> {
		return clientActionRepository.findByClientId(clientId).map {
			it.advertisementId
		}.toSet()
	}

	fun createImpression(clientId: UUID, advertisement: Advertisement, cost: Double) {
		requireNotNull(advertisement.id) { "Advertisement ID cannot be null" }

		val impressionId = buildImpressionId(clientId, advertisement.id)
		clientActionRepository.insert(
			id = impressionId,
			clientId = clientId,
			advertisementId = advertisement.id,
			actionType = ClientActionType.IMPRESSION,
			cost = cost,
			createdAt = globalSettingsService.getCurrentDate(),
			advertiserId = advertisement.advertiserId,
		)
	}

	private fun buildImpressionId(
		clientId: UUID,
		advertisementId: UUID,
	): String = ClientAction.constructId(clientId, advertisementId, ClientActionType.IMPRESSION)

	private fun buildClickId(
		clientId: UUID,
		advertisementId: UUID,
	): String = ClientAction.constructId(clientId, advertisementId, ClientActionType.CLICK)

	fun createClick(clientId: UUID, advertisement: Advertisement, cost: Double) {
		requireNotNull(advertisement.id) { "Advertisement ID cannot be null" }

		val clickId = buildClickId(clientId, advertisement.id)
		clientActionRepository.insert(
			id = clickId,
			clientId = clientId,
			advertisementId = advertisement.id,
			actionType = ClientActionType.CLICK,
			cost = cost,
			createdAt = globalSettingsService.getCurrentDate(),
			advertiserId = advertisement.advertiserId,
		)
	}

	fun impressionExists(clientId: UUID, advertisementId: UUID): Boolean = clientActionRepository.existsById(
		buildImpressionId(clientId, advertisementId)
	)
}
