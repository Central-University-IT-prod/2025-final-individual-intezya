package com.intezya.solution.controllers

import com.intezya.solution.dto.AdvertiserRequest
import com.intezya.solution.services.AdvertiserService
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
@RequestMapping("/advertisers")
@Tag(name = "Advertisers", description = "Управление рекламодателями и ML скорами для определения релевантности.")
class AdvertiserController(
	private val advertiserService: AdvertiserService,
) {
	@PostMapping("/bulk")
	@Operation(
		summary = "Массовое создание/обновление рекламодателей",
		description = "Создаёт новых или обновляет существующих рекламодателей"
	)
	@ApiResponse(responseCode = "201", description = "Успешное создание/обновление рекламодателей")
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	fun createOrUpdateAdvertisers(
		@RequestBody
		@Valid
		advertisers: List<AdvertiserRequest>,
	): ResponseEntity<List<AdvertiserRequest>> {
		if (advertisers.isEmpty()) {
			throw ResponseStatusException(
				HttpStatus.BAD_REQUEST, "Empty advertisers list"
			)
		}
		advertiserService.createOrUpdate(advertisers)
		return ResponseEntity(
			advertisers,
			HttpStatus.CREATED,
		)
	}


	@GetMapping("/{advertiserId}")
	@Operation(
		summary = "Получение рекламодателя по ID",
		description = "Возвращает информацию о рекламодателе по его ID.",
	)
	@ApiResponse(responseCode = "200", description = "Информация о рекламодателе успешно получена.")
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	@ApiResponse(responseCode = "404", description = "Рекламодатель не найден")
	fun getAdvertiserById(
		@PathVariable
		advertiserId: UUID,
	): AdvertiserRequest {
		val advertiser = advertiserService.findById(advertiserId) ?: throw ResponseStatusException(
			HttpStatus.NOT_FOUND, "Advertiser not found"
		)

		return advertiser
	}
}
