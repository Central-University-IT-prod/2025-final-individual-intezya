package com.intezya.solution.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class AdView(
	@field:JsonProperty("ad_id")
	val id: UUID,

	@field:JsonProperty("advertiser_id")
	val advertiserId: UUID,

	@field:JsonProperty("ad_title")
	val title: String,

	@field:JsonProperty("ad_text")
	val text: String,

	@field:JsonProperty("image_url")
	private val imageUrl: String? = null,
)
