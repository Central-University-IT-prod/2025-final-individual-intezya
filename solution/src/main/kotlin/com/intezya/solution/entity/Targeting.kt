package com.intezya.solution.entity

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.intezya.solution.enums.TargetingGender
import com.intezya.solution.utils.deserializer.StrictIntDeserializer
import com.intezya.solution.utils.deserializer.StrictStringDeserializer
import jakarta.validation.constraints.Min

data class Targeting(
	@JsonProperty("gender")
	val gender: TargetingGender? = null,

	@JsonProperty("age_from")
	@field:Min(0)
	@field:JsonDeserialize(using = StrictIntDeserializer::class)
	val ageFrom: Int? = null,

	@JsonProperty("age_to")
	@field:Min(0)
	@field:JsonDeserialize(using = StrictIntDeserializer::class)
	val ageTo: Int? = null,

	@JsonProperty("location")
	@field:JsonDeserialize(using = StrictStringDeserializer::class)
	val location: String? = null,
)
