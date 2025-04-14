package com.intezya.solution.entity

import com.intezya.solution.enums.ClientActionType
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table(name = "client_actions")
data class ClientAction(
	@Id
	val id: String,

	val clientId: UUID,
	val advertisementId: UUID,
	val advertiserId: UUID,

	val actionType: ClientActionType,

	val cost: Double,

	val createdAt: Int,
) {
	companion object {
		fun constructId(
			clientId: UUID,
			advertisementId: UUID,
			actionType: ClientActionType,
		): String = "$clientId$advertisementId$actionType"
	}
}
