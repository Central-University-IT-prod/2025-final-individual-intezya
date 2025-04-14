package com.intezya.solution.entity

import com.intezya.solution.dto.AdView
import com.intezya.solution.dto.AdvertisementView
import com.intezya.solution.enums.TargetingGender
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table(name = "advertisements")
data class Advertisement(
	@Id
	val id: UUID? = null,

	val advertiserId: UUID,

	var title: String,
	var text: String,

	var impressionsLimit: Int,
	var clicksLimit: Int,

	var currentImpressions: Int = 0,
	var currentClicks: Int = 0,

	var costPerImpression: Double,
	var costPerClick: Double,

	var targetingGender: TargetingGender? = null,
	var targetingAgeFrom: Int? = null,
	var targetingAgeTo: Int? = null,
	var targetingLocation: String? = null,

	var startDate: Int,
	var endDate: Int,

	var imageUrl: String? = null,

	var moderatedAndValid: Boolean = true,
) {
	fun toAdvertisementView(): AdvertisementView = AdvertisementView(
		id = id!!,
		advertiserId = advertiserId,
		impressionsLimit = impressionsLimit,
		clicksLimit = clicksLimit,
		costPerImpression = costPerImpression,
		costPerClick = costPerClick,
		title = title,
		text = text,
		startDate = startDate,
		endDate = endDate,
		targeting = Targeting(
			gender = targetingGender,
			ageFrom = targetingAgeFrom,
			ageTo = targetingAgeTo,
			location = targetingLocation,
		),
		imageUrl = imageUrl,
	)

	fun toAdView(): AdView = AdView(
		id = id!!,
		advertiserId = advertiserId,
		title = title,
		text = text,
		imageUrl = imageUrl,
	)

	fun isStarted(currentDate: Int): Boolean {
		return currentDate in startDate..endDate
	}

	private fun conversionRate(): Double {
		return if (currentImpressions == 0) {
			0.0
		} else {
			currentClicks.toDouble() / currentImpressions
		}
	}

	fun targetingPrecision(): Double = listOf(
		targetingLocation != null,
		targetingAgeFrom != null,
		targetingAgeTo != null,
		targetingGender != null && targetingGender != TargetingGender.ALL
	).count { it } / 4.0

	fun potentialEarn(isNewImpression: Boolean): Double {
		return (if (isNewImpression) 1 else 0) * costPerImpression + costPerClick * conversionRate()
	}

	fun impressionRate(): Double = currentImpressions.toDouble() / impressionsLimit

	fun clickRate(): Double {
		return if (clicksLimit == 0) 0.0 else currentClicks.toDouble() / clicksLimit
	}
}
