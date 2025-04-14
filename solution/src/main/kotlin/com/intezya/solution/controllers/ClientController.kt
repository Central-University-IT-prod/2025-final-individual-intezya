package com.intezya.solution.controllers

import com.intezya.solution.dto.ClientRequest
import com.intezya.solution.services.ClientService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.util.*

@RestController
@RequestMapping("/clients")
@Tag(name = "Clients", description = "Управление клиентами: создание и обновление информации о клиентах.")
class ClientController(private val service: ClientService) {
	@PostMapping("/bulk")
	@Operation(
		summary = "Массовое создание/обновление клиентов",
		description = "Создаёт новых или обновляет существующих клиентов"
	)
	@ApiResponse(responseCode = "201", description = "Успешное создание/обновление клиентов")
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	fun createOrUpdateClient(
		@Valid
		@RequestBody
		clients: List<@Valid ClientRequest>,
	): ResponseEntity<List<ClientRequest>> {
		if (clients.isEmpty()) {
			throw ResponseStatusException(
				HttpStatus.BAD_REQUEST, "Empty clients list"
			)
		}
		service.createOrUpdate(clients)
		return ResponseEntity(
			clients, HttpStatus.CREATED
		)
	}

	@Operation(
		summary = "Получение клиента по ID",
		description = "Возвращает информацию о клиенте по его ID.",
	)
	@ApiResponse(responseCode = "200", description = "Информация о клиенте успешно получена.")
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	@ApiResponse(responseCode = "404", description = "Клиент не найден")
	@GetMapping("/{id}")
	fun getClientById(
		@PathVariable
		id: UUID,
	): ClientRequest {
		return service.findById(id)
	}
}
