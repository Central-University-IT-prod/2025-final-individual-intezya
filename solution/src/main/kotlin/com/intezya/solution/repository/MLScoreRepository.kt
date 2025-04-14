package com.intezya.solution.repository

import com.intezya.solution.entity.MLScore
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

private const val UPSERT_ML_SCORE_QUERY = """
	INSERT INTO ml_scores (id, client_id, advertiser_id, score)
	VALUES (:id, :clientId, :advertiserId, :score)
	ON CONFLICT (id) DO UPDATE SET
		score = EXCLUDED.score;
"""

@Repository
interface MLScoreRepository : CrudRepository<MLScore, String> {
	@Modifying
	@Query(UPSERT_ML_SCORE_QUERY)
	fun upsert(
		@Param("id")
		id: String,
		@Param("clientId")
		clientId: UUID,
		@Param("advertiserId")
		advertiserId: UUID,
		@Param("score")
		score: Int,
	): Int

	fun findByClientId(clientId: UUID): List<MLScore>
}
