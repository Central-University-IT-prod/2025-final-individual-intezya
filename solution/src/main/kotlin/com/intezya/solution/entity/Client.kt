package com.intezya.solution.entity

import com.intezya.solution.dto.ClientRequest
import com.intezya.solution.enums.Gender
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table(name = "clients")
data class Client(
	@Id
	val id: UUID,

	val login: String,

	val age: Int,
	val location: String,
	val gender: Gender,
) {
	fun toRequest() = ClientRequest(
		id = id,
		login = login,
		age = age,
		location = location,
		gender = gender,
	)
}
