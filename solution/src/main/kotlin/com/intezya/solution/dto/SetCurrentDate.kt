package com.intezya.solution.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.intezya.solution.utils.deserializer.StrictIntDeserializer
import jakarta.validation.constraints.Min

data class SetCurrentDate(
	@field:JsonProperty("current_date")
	@field:Min(0)
	@field:JsonDeserialize(using = StrictIntDeserializer::class)
	val currentDate: Int,
)
