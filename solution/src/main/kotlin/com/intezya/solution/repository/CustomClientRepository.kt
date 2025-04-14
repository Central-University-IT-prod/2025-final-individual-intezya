package com.intezya.solution.repository

import com.intezya.solution.entity.Client

interface CustomClientRepository {
	fun bulkUpsert(clients: List<Client>)
}
