package com.intezya.solution.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table(name = "advertisers")
data class Advertiser(
	@Id
	val id: UUID,

	val name: String,
)
