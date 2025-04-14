package com.intezya.solution.controllers

import com.intezya.solution.dto.MLScoreRequest
import com.intezya.solution.services.MLScoreService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ml-scores")
@Tag(name = "Advertisers", description = "Управление рекламодателями и ML скорами для определения релевантности.")
class MLScoreController(
	private val mlScoreService: MLScoreService,
) {
	@PostMapping()
	@Operation(
		summary = "Добавление или обновление ML скора",
		description = "Добавляет или обновляет ML скор для указанной пары клиент-рекламодатель."
	)
	@ApiResponse(responseCode = "200", description = "ML скор успешно добавлен или обновлён.")
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	@ApiResponse(responseCode = "404", description = "Рекламодатель/клиент не найден")
	fun create(
		@Valid
		@RequestBody
		request: MLScoreRequest,
	): MLScoreRequest {
		mlScoreService.create(request)
		return request
	}
}
