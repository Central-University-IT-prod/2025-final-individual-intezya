package com.intezya.solution.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.intezya.solution.entity.Advertiser
import com.intezya.solution.utils.deserializer.StrictStringDeserializer
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.*


data class AdvertiserRequest(
	@field:NotNull
	@field:JsonProperty("advertiser_id")
	val id: UUID,

	@field:NotBlank
	@field:JsonDeserialize(using = StrictStringDeserializer::class)
	val name: String,
) {
	fun toEntity(): Advertiser = Advertiser(
		id = id,
		name = name,
	)
}
