package com.intezya.solution.services

import com.intezya.solution.dto.*
import com.intezya.solution.entity.Advertisement
import com.intezya.solution.repository.AdvertisementRepository
import com.intezya.solution.repository.AdvertiserRepository
import com.intezya.solution.repository.MLScoreRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class AdvertisementService(
	private val advertisementRepository: AdvertisementRepository,
	private val clientService: ClientService,
	private val advertiserRepository: AdvertiserRepository,
	private val mlScoreRepository: MLScoreRepository,
	private val clientActionService: ClientActionService,
	private val minioService: MinioService,
	private val contentModerationService: ContentModerationService,
	private val globalSettingsService: GlobalSettingsService,
) {
	companion object {
		private const val POTENTIAL_EARN_WEIGHT = 1
		private const val ML_SCORE_WEIGHT = 0.45
		private const val RATE_WEIGHT = 0.3
		private const val PRECISION_WEIGHT = 0.4
		private const val RATE_ADJUSTMENT = 0.25
	}

	private data class ScoredAdvertisement(
		val advertisement: Advertisement,
		val score: Double,
		val isNewImpression: Boolean,
	)

	suspend fun create(advertiserId: UUID, request: AdvertisementCreateRequest): AdvertisementView {
		request.imageUrl.takeIf { it?.isNotBlank() == true }?.let {
			minioService.asyncUploadImage(it)
		}

		var entity = request.toEntity(advertiserId)

		if (globalSettingsService.getModerationEnabled() && !contentModerationService.checkAdvertisement(
				request.title, request.text
			)
		) {
			entity.moderatedAndValid = false
		}

		return entity.let(advertisementRepository::save).toAdvertisementView()
	}

	@Transactional
	fun getById(advertiserId: UUID, advertisementId: UUID): AdvertisementView = validateOwnership(
		advertiserId, advertisementId
	).toAdvertisementView()

	fun getForImage(advertisementId: UUID): AdvertisementView {
		val advertisement = advertisementRepository.findById(advertisementId).orElse(null)
			?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Advertisement not found")
		return advertisement.toAdvertisementView()
	}

	@Transactional
	fun deleteById(advertiserId: UUID, advertisementId: UUID): AdvertisementView = validateOwnership(
		advertiserId, advertisementId
	).also {
		advertisementRepository.deleteById(advertisementId)
	}.toAdvertisementView()

	@Transactional
	fun updateById(
		advertiserId: UUID,
		advertisementId: UUID,
		request: AdvertisementEditRequest,
	): AdvertisementView {
		val existingAd = validateOwnership(advertiserId, advertisementId)

		val updatedAd = request.toEntity(advertisementId, advertiserId)
		val campaignStarted = existingAd.isStarted(globalSettingsService.getCurrentDate())

		if (campaignStarted && !updatedAd.checkStartedAdPropertiesPreserved(existingAd)) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot update started advertisement")
		}

		request.imageUrl.takeIf { it?.isNotBlank() == true }?.let {
			minioService.asyncUploadImage(it)
		}

		return advertisementRepository.save(updatedAd).toAdvertisementView()
	}

	@Transactional
	fun getByAdvertiserIdPaged(advertiserId: UUID, pageable: Pageable): Page<AdvertisementView> {
		validateAdvertiser(advertiserId)
		return advertisementRepository.findByAdvertiserId(advertiserId, pageable)
			.map(Advertisement::toAdvertisementView)
	}


	@Transactional
	fun getByAdvertiserId(advertiserId: UUID): List<AdvertisementView> {
		validateAdvertiser(advertiserId)
		return advertisementRepository.findByAdvertiserId(advertiserId).map(Advertisement::toAdvertisementView)
	}

	@Transactional
	fun getEligibleAdvertisement(clientId: UUID): AdView {
		val client = clientService.findById(clientId)
		val eligibleAds = findEligibleAds(client).ifEmpty {
			throw ResponseStatusException(
				HttpStatus.NOT_FOUND, "No eligible advertisement found"
			)
		}

		val scoredAds = scoreAdvertisements(
			eligibleAds = eligibleAds,
			clientImpressions = clientActionService.getClientImpressions(clientId),
			mlScores = getMLScoresMap(clientId)
		)

		return selectAndProcessAdvertisement(clientId, scoredAds)
	}

	@Transactional
	fun clickAd(clientId: UUID, advertisementId: UUID) {
		val advertisement = advertisementRepository.findById(advertisementId).orElse(null)
			?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Advertisement not found")

		if (!clientActionService.impressionExists(clientId, advertisementId)) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Advertisement not viewed by client")
		}

		processNewClick(clientId, advertisement)
	}

	fun getByIdForTGBot(advertisementId: UUID): AdvertisementView? {
		return advertisementRepository.findById(advertisementId).orElse(null).toAdvertisementView()
	}

	private fun validateOwnership(advertiserId: UUID, advertisementId: UUID): Advertisement {
		val advertisement = advertisementRepository.findById(advertisementId)
			.orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Advertisement not found") }

		if (advertisement.advertiserId != advertiserId) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to access this resource")
		}

		return advertisement
	}

	private fun validateAdvertiser(advertiserId: UUID) {
		if (!advertiserRepository.existsById(advertiserId)) {
			throw ResponseStatusException(HttpStatus.NOT_FOUND, "Advertiser not found")
		}
	}

	private fun Advertisement.checkStartedAdPropertiesPreserved(existingAd: Advertisement): Boolean {
		return with(existingAd) {
			listOf(
				impressionsLimit == this@checkStartedAdPropertiesPreserved.impressionsLimit,
				clicksLimit == this@checkStartedAdPropertiesPreserved.clicksLimit,
				startDate == this@checkStartedAdPropertiesPreserved.startDate,
				endDate == this@checkStartedAdPropertiesPreserved.endDate
			).all { it }
		}
	}

	@Transactional
	fun findEligibleAds(client: ClientRequest): List<Advertisement> =
		advertisementRepository.findEligibleAdvertisements(
			age = client.age,
			location = client.location,
			gender = client.gender,
			currentDate = globalSettingsService.getCurrentDate(),
		)

	private fun getMLScoresMap(clientId: UUID): Map<UUID, Double> =
		mlScoreRepository.findByClientId(clientId).associateBy({ it.advertiserId }, { it.score.toDouble() })

	private fun scoreAdvertisements(
		eligibleAds: List<Advertisement>,
		clientImpressions: Set<UUID>,
		mlScores: Map<UUID, Double>,
	): List<ScoredAdvertisement> = eligibleAds.map { ad ->
		val isNewImpression = !clientImpressions.contains(ad.id)
		ScoredAdvertisement(
			advertisement = ad,
			score = calculateProfitScore(ad, isNewImpression, mlScores),
			isNewImpression = isNewImpression
		)
	}

	private fun calculateProfitScore(
		ad: Advertisement,
		isNewImpression: Boolean,
		mlScores: Map<UUID, Double>,
	): Double {
		val targetingPrecision = ad.targetingPrecision()
		val potentialEarn = ad.potentialEarn(isNewImpression)
		val mlScore = mlScores[ad.advertiserId] ?: 0.0
		val maxRate = maxOf(ad.impressionRate(), ad.clickRate())
		val precisionFactor = (1 / (ad.impressionRate() + RATE_ADJUSTMENT)) * targetingPrecision

		return POTENTIAL_EARN_WEIGHT * potentialEarn + ML_SCORE_WEIGHT * mlScore + RATE_WEIGHT * maxRate + PRECISION_WEIGHT * precisionFactor
	}

	private fun selectAndProcessAdvertisement(
		clientId: UUID,
		scoredAds: List<ScoredAdvertisement>,
	): AdView {
		val selected = scoredAds.maxBy { it.score }
		val advertisement = selected.advertisement

		if (selected.isNewImpression) {
			processNewImpression(clientId, advertisement)
		}

		return advertisement.toAdView()
	}

	private fun processNewImpression(clientId: UUID, advertisement: Advertisement) {
		requireNotNull(advertisement.id) { "Advertisement ID cannot be null" }

		if (advertisement.currentImpressions >= advertisement.impressionsLimit) {
			throw ResponseStatusException(HttpStatus.FORBIDDEN, "Impressions limit reached")
		}

		clientActionService.createImpression(
			clientId = clientId,
			advertisement = advertisement,
			cost = advertisement.costPerImpression,
		)
		advertisementRepository.incrementImpressions(advertisement.id)
	}

	private fun processNewClick(clientId: UUID, advertisement: Advertisement) {
		requireNotNull(advertisement.id) { "Advertisement ID cannot be null" }

		clientActionService.createClick(
			clientId = clientId,
			advertisement = advertisement,
			cost = advertisement.costPerClick,
		)
		advertisementRepository.incrementClicks(advertisement.id)
	}
}
