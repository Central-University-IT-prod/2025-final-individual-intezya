package com.intezya.solution.controllers

import com.intezya.solution.dto.AdView
import com.intezya.solution.dto.ClientIdRequest
import com.intezya.solution.services.AdvertisementService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/ads")
@Tag(
	name = "Ads",
	description = "Показ рекламных объявлений клиентам и фиксация кликов.",
)
class AdsController(
	private val advertisementService: AdvertisementService,
) {
	@GetMapping
	@Operation(
		summary = "Получение рекламного объявления для клиента",
		description = "Возвращает рекламное объявление, подходящее для показа клиенту с учетом таргетинга и ML скора.",
	)
	@ApiResponse(responseCode = "200", description = "Рекламное объявление успешно возвращено.")
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	@ApiResponse(responseCode = "404", description = "Подходящая реклама не найдена")
	fun getAd(
		@RequestParam("client_id")
		clientId: UUID,
	): AdView {
		return advertisementService.getEligibleAdvertisement(clientId)
	}

	@PostMapping("{advertisementId}/click")
	@Operation(
		summary = "Фиксация перехода по рекламному объявлению",
		description = "Фиксирует клик (переход) клиента по рекламному объявлению.",
	)
	@ApiResponse(responseCode = "204", description = "Переход по рекламному объявлению успешно зафиксирован.")
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	@ApiResponse(responseCode = "403", description = "Реклама ещё не просмотрена!")
	@ApiResponse(responseCode = "404", description = "Реклама не найдена")
	fun clickAd(
		@RequestBody
		@Valid
		clientIdRequest: ClientIdRequest,
		@PathVariable
		advertisementId: UUID,
	): ResponseEntity<Any> {
		advertisementService.clickAd(clientIdRequest.clientId, advertisementId)
		return ResponseEntity(HttpStatus.NO_CONTENT)
	}
}
