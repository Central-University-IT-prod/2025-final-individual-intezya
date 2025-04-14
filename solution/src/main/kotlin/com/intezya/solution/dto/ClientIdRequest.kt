package com.intezya.solution.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import java.util.*

data class ClientIdRequest(
	@field:JsonProperty("client_id")
	@NotNull
	val clientId: UUID,
)
