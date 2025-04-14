package com.intezya.solution.services

import com.intezya.solution.dto.ClientRequest
import com.intezya.solution.entity.Client
import com.intezya.solution.repository.ClientRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
class ClientService(
	private val clientRepository: ClientRepository,
) {
	fun createOrUpdate(clients: List<ClientRequest>) {
		clientRepository.bulkUpsert(clients.map(ClientRequest::toEntity))
	}

	fun findById(id: UUID): ClientRequest {
		return clientRepository.findById(id).orElse(null)?.toRequest() ?: throw ResponseStatusException(
			HttpStatus.NOT_FOUND, "Client not found"
		)
	}

	fun getAll(): List<Client> {
		return clientRepository.findAll().toList()
	}
}
