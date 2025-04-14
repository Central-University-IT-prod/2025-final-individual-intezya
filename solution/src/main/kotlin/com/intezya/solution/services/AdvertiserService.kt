package com.intezya.solution.services

import com.intezya.solution.dto.AdvertiserRequest
import com.intezya.solution.entity.Advertiser
import com.intezya.solution.repository.AdvertiserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class AdvertiserService(
	private val advertiserRepository: AdvertiserRepository,
) {
	fun createOrUpdate(advertisers: List<AdvertiserRequest>) {
		advertiserRepository.bulkUpsert(
			advertisers.map(AdvertiserRequest::toEntity)
		)
	}

	fun findById(id: UUID): AdvertiserRequest? {
		val advertiser = advertiserRepository.findById(id).orElse(null) ?: throw ResponseStatusException(
			HttpStatus.NOT_FOUND, "Advertiser not found"
		)

		return AdvertiserRequest(
			id = advertiser.id,
			name = advertiser.name,
		)
	}

	fun getAll(): List<Advertiser> {
		return advertiserRepository.findAll().toList()
	}
}
