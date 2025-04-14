package com.intezya.solution.repository.impl

import com.intezya.solution.entity.Advertiser
import com.intezya.solution.repository.CustomAdvertiserRepository
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

private const val UPSERT_ADVERTISER_QUERY = """
	INSERT INTO advertisers (id, name)
	VALUES (?, ?)
	ON CONFLICT (id) DO UPDATE SET
		name = EXCLUDED.name;
"""


@Repository
class CustomAdvertiserRepositoryImpl(
	private val jdbcTemplate: JdbcTemplate,
) : CustomAdvertiserRepository {
	override fun bulkUpsert(advertisers: List<Advertiser>) {
		val batchArgs: List<Array<Any>> = advertisers.map { arrayOf(it.id, it.name) }
		jdbcTemplate.batchUpdate(UPSERT_ADVERTISER_QUERY, batchArgs)
	}
}
