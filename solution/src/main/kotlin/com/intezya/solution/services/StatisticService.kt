package com.intezya.solution.services

import com.intezya.solution.dto.DailyStatistic
import com.intezya.solution.dto.Statistic
import com.intezya.solution.entity.ClientAction
import com.intezya.solution.enums.ClientActionType
import com.intezya.solution.repository.ClientActionRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class StatisticService(
	private val clientActionRepository: ClientActionRepository,
) {
	fun advertiserStatistic(advertiserId: UUID): Statistic =
		calculateStatistic(clientActionRepository.findByAdvertiserId(advertiserId))

	fun advertiserDailyStatistic(advertiserId: UUID): List<DailyStatistic> =
		calculateDailyStatistic(clientActionRepository.findByAdvertiserId(advertiserId))

	fun advertisementStatistic(advertisementId: UUID): Statistic =
		calculateStatistic(clientActionRepository.findByAdvertisementId(advertisementId))

	fun advertisementDailyStatistic(advertisementId: UUID): List<DailyStatistic> =
		calculateDailyStatistic(clientActionRepository.findByAdvertisementId(advertisementId))

	private fun calculateStatistic(clientActions: List<ClientAction>): Statistic {
		val (impressions, clicks) = countActions(clientActions)
		val (spentImpressions, spentClicks) = calculateSpent(clientActions)
		val conversion = calculateConversion(impressions, clicks)

		return Statistic(
			impressionsCount = impressions,
			clicksCount = clicks,
			conversion = conversion,
			spentImpressions = spentImpressions,
			spentClicks = spentClicks,
			spentTotal = spentImpressions + spentClicks
		)
	}

	private fun calculateDailyStatistic(clientActions: List<ClientAction>): List<DailyStatistic> =
		clientActions.groupBy { it.createdAt }.map { (date, actions) ->
			val (impressions, clicks) = countActions(actions)
			val (spentImpressions, spentClicks) = calculateSpent(actions)
			val conversion = calculateConversion(impressions, clicks)

			DailyStatistic(
				impressionsCount = impressions,
				clicksCount = clicks,
				conversion = conversion,
				spentImpressions = spentImpressions,
				spentClicks = spentClicks,
				spentTotal = spentImpressions + spentClicks,
				date = date,
			)
		}.sortedBy { it.date }

	private fun countActions(actions: List<ClientAction>): Pair<Int, Int> = Pair(
		actions.count { it.actionType == ClientActionType.IMPRESSION },
		actions.count { it.actionType == ClientActionType.CLICK })

	private fun calculateSpent(actions: List<ClientAction>): Pair<Double, Double> = Pair(
		actions.filter { it.actionType == ClientActionType.IMPRESSION }.sumOf { it.cost },
		actions.filter { it.actionType == ClientActionType.CLICK }.sumOf { it.cost })

	private fun calculateConversion(impressions: Int, clicks: Int): Double =
		if (impressions > 0) (clicks.toDouble() / impressions) * 100 else 0.0
}
