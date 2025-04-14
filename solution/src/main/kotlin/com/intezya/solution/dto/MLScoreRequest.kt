package com.intezya.solution.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.intezya.solution.entity.MLScore
import com.intezya.solution.utils.deserializer.StrictIntDeserializer
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.util.*

data class MLScoreRequest(
	@field:JsonProperty("client_id")
	@field:NotNull
	val clientId: UUID,

	@field:NotNull
	@field:JsonProperty("advertiser_id")
	val advertiserId: UUID,

	@field:NotNull
	@field:Min(0)
	@field:JsonDeserialize(using = StrictIntDeserializer::class)
	val score: Int,
) {
	fun toEntity(): MLScore = MLScore(
		id = clientId.toString() + this@MLScoreRequest.advertiserId.toString(),
		score = score,
		clientId = clientId,
		advertiserId = advertiserId,
	)
}
