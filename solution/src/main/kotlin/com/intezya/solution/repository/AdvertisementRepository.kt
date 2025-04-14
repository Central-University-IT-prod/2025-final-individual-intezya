package com.intezya.solution.repository

import com.intezya.solution.entity.Advertisement
import com.intezya.solution.enums.Gender
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*


private const val FIND_ELIGIBLE_ADVERTISEMENT_QUERY = """
SELECT a.* FROM advertisements a
	WHERE moderated_and_valid = true 
	AND (:currentDate >= a.start_date AND :currentDate <= a.end_date)
	AND (a.current_impressions < a.impressions_limit)
	AND (
		(a.targeting_age_from IS NULL or :age >= a.targeting_age_from )
		AND
		(a.targeting_age_to IS NULL or :age <= a.targeting_age_to)
	)
	AND (
		a.targeting_location IS NULL
		OR a.targeting_location = :location
	)
	AND (
		a.targeting_gender IS NULL 
		OR a.targeting_gender = 'ALL'
		OR a.targeting_gender = :gender
	)
"""

private const val INCREMENT_IMPRESSIONS_QUERY = """
	UPDATE advertisements
	SET current_impressions = current_impressions + 1
	WHERE id = :advertisementId
"""

private const val INCREMENT_CLICKS_QUERY = """
	UPDATE advertisements
	SET current_clicks = current_clicks + 1
	WHERE id = :advertisementId
"""

@Repository
interface AdvertisementRepository : CrudRepository<Advertisement, UUID> {
	fun findByAdvertiserId(advertiserId: UUID, pageable: Pageable): Page<Advertisement>

	fun findByAdvertiserId(advertiserId: UUID): List<Advertisement>

	@Query(FIND_ELIGIBLE_ADVERTISEMENT_QUERY)
	fun findEligibleAdvertisements(
		@Param("age")
		age: Int,
		@Param("location")
		location: String,
		@Param("gender")
		gender: Gender,
		@Param("currentDate")
		currentDate: Int,
	): List<Advertisement>

	@Modifying
	@Query(INCREMENT_IMPRESSIONS_QUERY)
	fun incrementImpressions(
		@Param("advertisementId")
		advertisementId: UUID,
	): Int

	@Modifying
	@Query(INCREMENT_CLICKS_QUERY)
	fun incrementClicks(
		@Param("advertisementId")
		advertisementId: UUID,
	): Int
}
