package com.intezya.solution.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*


@Table("ml_scores")
data class MLScore(
	@Id
	val id: String,

	val clientId: UUID,
	val advertiserId: UUID,

	val score: Int,
) {
	companion object {
		fun constructId(clientId: UUID, advertiserId: UUID): String = "$clientId$advertiserId"
	}
}
