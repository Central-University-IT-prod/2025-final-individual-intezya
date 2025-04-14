package com.intezya.solution.services

import com.intezya.solution.dto.MLScoreRequest
import com.intezya.solution.entity.MLScore
import com.intezya.solution.repository.AdvertiserRepository
import com.intezya.solution.repository.ClientRepository
import com.intezya.solution.repository.MLScoreRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class MLScoreService(
	private val mlScoreRepository: MLScoreRepository,
	private val clientRepository: ClientRepository,
	private val advertiserRepository: AdvertiserRepository,
) {
	fun create(mlScoreCreate: MLScoreRequest) {
		validateEntities(mlScoreCreate.clientId, mlScoreCreate.advertiserId)
		mlScoreRepository.upsert(
			MLScore.constructId(mlScoreCreate.clientId, mlScoreCreate.advertiserId),
			mlScoreCreate.clientId,
			mlScoreCreate.advertiserId,
			mlScoreCreate.score,
		)
	}

	private fun validateEntities(clientId: UUID, advertiserId: UUID) {
		if (!clientRepository.existsById(clientId)) {
			throw ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found")
		}
		if (!advertiserRepository.existsById(advertiserId)) {
			throw ResponseStatusException(HttpStatus.NOT_FOUND, "Advertiser not found")
		}
	}
}
