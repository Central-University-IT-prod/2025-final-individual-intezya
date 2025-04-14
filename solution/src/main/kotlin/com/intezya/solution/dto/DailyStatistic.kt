package com.intezya.solution.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class DailyStatistic(
	@field:JsonProperty("impressions_count")
	val impressionsCount: Int,
	@field:JsonProperty("clicks_count")
	val clicksCount: Int,
	@field:JsonProperty("conversion")
	val conversion: Double,
	@field:JsonProperty("spent_impressions")
	val spentImpressions: Double,
	@field:JsonProperty("spent_clicks")
	val spentClicks: Double,
	@field:JsonProperty("spent_total")
	val spentTotal: Double,
	@field:JsonProperty("date")
	val date: Int,
)
