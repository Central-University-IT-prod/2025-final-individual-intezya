package com.intezya.solution.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.intezya.solution.entity.Advertisement
import com.intezya.solution.entity.Targeting
import java.util.*

data class AdvertisementView(
	@field:JsonProperty("campaign_id")
	val id: UUID,

	@field:JsonProperty("advertiser_id")
	val advertiserId: UUID,

	@field:JsonProperty("impressions_limit")
	val impressionsLimit: Int,

	@field:JsonProperty("clicks_limit")
	val clicksLimit: Int,

	@field:JsonProperty("cost_per_impression")
	val costPerImpression: Double,

	@field:JsonProperty("cost_per_click")
	val costPerClick: Double,

	@field:JsonProperty("ad_title")
	val title: String,

	@field:JsonProperty("ad_text")
	val text: String,

	@field:JsonProperty("start_date")
	val startDate: Int,

	@field:JsonProperty("end_date")
	val endDate: Int,

	@field:JsonProperty("targeting")
	val targeting: Targeting,

	@field:JsonProperty("image_url")
	val imageUrl: String? = null,
) {
	fun toEntity(): Advertisement {
		return Advertisement(
			id = id,
			advertiserId = advertiserId,
			title = title,
			text = text,
			impressionsLimit = impressionsLimit,
			clicksLimit = clicksLimit,
			currentImpressions = 0,
			currentClicks = 0,
			costPerImpression = costPerImpression,
			costPerClick = costPerClick,
			targetingGender = targeting.gender,
			targetingAgeFrom = targeting.ageFrom,
			targetingAgeTo = targeting.ageTo,
			targetingLocation = targeting.location,
			startDate = startDate,
			endDate = endDate,
			imageUrl = imageUrl,
		)
	}
}
