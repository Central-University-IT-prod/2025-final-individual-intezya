package com.intezya.solution.repository

import com.intezya.solution.entity.ClientAction
import com.intezya.solution.enums.ClientActionType
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.util.*

private const val INSERT_ADVERTISEMENT_CLIENT_ACTION_QUERY = """
	INSERT INTO client_actions (id, client_id, advertisement_id, advertiser_id, action_type, cost, created_at)
	VALUES (:id, :clientId, :advertisementId,:advertiserId, :actionType, :cost, :createdAt)
"""

private const val SUM_IMPRESSIONS_COST_QUERY = """
    SELECT COALESCE(SUM(cost), 0.0) 
    FROM client_actions 
    WHERE advertiser_id = :advertiserId 
    AND action_type = 'IMPRESSION'
"""

private const val SUM_CLICKS_COST_QUERY = """
    SELECT COALESCE(SUM(cost), 0.0) 
    FROM client_actions 
    WHERE advertiser_id = :advertiserId 
    AND action_type = 'CLICK'
"""

interface ClientActionRepository : CrudRepository<ClientAction, String> {
	fun findByClientId(clientId: UUID): List<ClientAction>
	fun findByAdvertiserId(advertiserId: UUID): List<ClientAction>
	fun findByAdvertisementId(advertisementId: UUID): List<ClientAction>

	@Modifying
	@Query(INSERT_ADVERTISEMENT_CLIENT_ACTION_QUERY)
	fun insert(
		@Param("id")
		id: String,
		@Param("clientId")
		clientId: UUID,
		@Param("advertisementId")
		advertisementId: UUID,
		@Param("advertiserId")
		advertiserId: UUID,
		@Param("actionType")
		actionType: ClientActionType,
		@Param("cost")
		cost: Double,
		@Param("createdAt")
		createdAt: Int,
	): Int

	@Query(SUM_IMPRESSIONS_COST_QUERY)
	fun sumImpressionsCost(
		@Param("advertiserId")
		advertiserId: UUID,
	): Double

	@Query(SUM_CLICKS_COST_QUERY)
	fun sumClicksCost(
		@Param("advertiserId")
		advertiserId: UUID,
	): Double
}
