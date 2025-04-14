package com.intezya.solution.repository.impl

import com.intezya.solution.entity.Client
import com.intezya.solution.repository.CustomClientRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository


private const val UPSERT_CLIENT_QUERY = """
	INSERT INTO clients (id, login, age, location, gender)
	VALUES (?, ?, ?, ?, ?)
	ON CONFLICT (id) DO UPDATE SET
	    login = EXCLUDED.login,
	    age = EXCLUDED.age,
	    location = EXCLUDED.location,
	    gender = EXCLUDED.gender;
"""

@Repository
class CustomClientRepositoryImpl(
	private val jdbcTemplate: JdbcTemplate,
) : CustomClientRepository {
	override fun bulkUpsert(clients: List<Client>) {
		val batchArgs: List<Array<Any>> = clients.map {
			arrayOf(
				it.id,
				it.login,
				it.age,
				it.location,
				it.gender.name,
			)
		}
		jdbcTemplate.batchUpdate(UPSERT_CLIENT_QUERY, batchArgs)
	}
}
