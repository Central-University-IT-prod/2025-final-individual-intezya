package com.intezya.solution.dto

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.intezya.solution.entity.Advertisement
import com.intezya.solution.entity.Targeting
import com.intezya.solution.utils.deserializer.StrictDoubleDeserializer
import com.intezya.solution.utils.deserializer.StrictIntDeserializer
import com.intezya.solution.utils.deserializer.StrictStringDeserializer
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.*

data class AdvertisementEditRequest(
	@field:NotNull
	@field:Min(1)
	@field:JsonProperty("impressions_limit")
	@field:JsonDeserialize(using = StrictIntDeserializer::class)
	private val impressionsLimit: Int,

	@field:NotNull
	@field:Min(0)
	@field:JsonProperty("clicks_limit")
	@field:JsonDeserialize(using = StrictIntDeserializer::class)
	private val clicksLimit: Int,

	@field:NotNull
	@field:Min(0)
	@field:JsonProperty("cost_per_impression")
	@field:JsonDeserialize(using = StrictDoubleDeserializer::class)
	private val costPerImpression: Double,

	@field:NotNull
	@field:Min(0)
	@field:JsonProperty("cost_per_click")
	@field:JsonDeserialize(using = StrictDoubleDeserializer::class)
	private val costPerClick: Double,

	@field:NotBlank
	@field:JsonProperty("ad_title")
	@field:JsonDeserialize(using = StrictStringDeserializer::class)
	private val title: String,

	@field:NotBlank
	@field:JsonProperty("ad_text")
	@field:JsonDeserialize(using = StrictStringDeserializer::class)
	private val text: String,

	@field:NotNull
	@field:Min(0)
	@field:JsonProperty("start_date")
	@field:JsonDeserialize(using = StrictIntDeserializer::class)
	private val startDate: Int,

	@field:NotNull
	@field:Min(0)
	@field:JsonProperty("end_date")
	@field:JsonDeserialize(using = StrictIntDeserializer::class)
	private val endDate: Int,

	@field:JsonProperty("targeting")
	private val targeting: Targeting? = null,

	@field:JsonProperty("image_url")
	val imageUrl: String? = null,
) {
	@AssertTrue(message = "start_date must be less than or equal to end_date")
	@JsonIgnore
	fun isStartDateValid(): Boolean {
		return startDate <= endDate
	}

	@AssertTrue(message = "clicks_limit must be less than or equal to impressions_limit")
	@JsonIgnore
	fun isClicksLimitValid(): Boolean {
		return clicksLimit <= impressionsLimit
	}


	fun toEntity(
		advertisementId: UUID,
		advertiserId: UUID,
	): Advertisement = Advertisement(
		impressionsLimit = impressionsLimit,
		clicksLimit = clicksLimit,
		costPerImpression = costPerImpression,
		costPerClick = costPerClick,
		title = title,
		text = text,
		startDate = startDate,
		endDate = endDate,
		advertiserId = advertiserId,
		id = advertisementId,
		targetingGender = targeting?.gender,
		targetingAgeFrom = targeting?.ageFrom,
		targetingAgeTo = targeting?.ageTo,
		targetingLocation = targeting?.location,
	)
}
