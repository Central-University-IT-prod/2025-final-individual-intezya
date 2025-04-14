package com.intezya.solution.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.intezya.solution.entity.Client
import com.intezya.solution.enums.Gender
import com.intezya.solution.utils.deserializer.StrictIntDeserializer
import com.intezya.solution.utils.deserializer.StrictStringDeserializer
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.*

data class ClientRequest(
	@field:NotNull
	@field:JsonProperty("client_id")
	val id: UUID,

	@field:NotBlank
	val login: String,

	@field:NotNull
	@field:Min(0)
	@field:Max(130)
	@field:JsonDeserialize(using = StrictIntDeserializer::class)
	val age: Int,

	@field:NotBlank
	@field:JsonDeserialize(using = StrictStringDeserializer::class)
	val location: String,

	@field:NotNull
	val gender: Gender,
) {
	fun toEntity(): Client = Client(
		id = id, login = login, age = age, location = location, gender = gender
	)
}
