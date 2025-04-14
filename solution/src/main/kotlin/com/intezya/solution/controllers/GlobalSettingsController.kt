package com.intezya.solution.controllers

import com.intezya.solution.dto.SetCurrentDate
import com.intezya.solution.services.ContentModerationService
import com.intezya.solution.services.GlobalSettingsService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping
@Tag(
	name = "Utils",
	description = "Различные утилиты для управления внутренними состояниями",
)
class GlobalSettingsController(
	private val systemService: GlobalSettingsService,
	private val contentModerationService: ContentModerationService,
) {
	@PostMapping("/time/advance")
	@Operation(
		summary = "Установка текущей даты",
		description = "Устанавливает текущий день в системе в заданную дату.",
	)
	@ApiResponse(
		responseCode = "200",
		description = "Текущая дата обновлена",
	)
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	fun setCurrentDate(
		@Valid
		@RequestBody
		request: SetCurrentDate,
	): SetCurrentDate {
		systemService.setCurrentDate(request.currentDate)
		return request
	}

	@PostMapping("/moderation")
	@Operation(
		summary = "Управление модерацией",
		description = "Включает/выключает модерацию.",
	)
	@ApiResponse(
		responseCode = "200",
		description = "Модерация включена/выключена",
	)
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	fun setModerationState(
		@RequestParam("enabled")
		moderationEnabled: Boolean,
	): ResponseEntity<Boolean> {
		systemService.setModerationState(moderationEnabled)
		return ResponseEntity(moderationEnabled, HttpStatus.OK)
	}

	@PostMapping("/moderation/banned_word")
	@Operation(
		summary = "Добавление слова в список запрещенных",
		description = "Добавляет слово в список запрещенных.",
	)
	@ApiResponse(
		responseCode = "200",
		description = "Слово добавлено в список запрещенных",
	)
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	fun addBannedWord(
		@RequestParam("word")
		word: String,
	): ResponseEntity<Boolean> {
		contentModerationService.addBannedWord(word)
		return ResponseEntity(true, HttpStatus.OK)
	}

	@DeleteMapping("/moderation/banned_word")
	@Operation(
		summary = "Удаление слова из списка запрещенных",
		description = "Удаляет слово из списка запрещенных.",
	)
	@ApiResponse(
		responseCode = "200",
		description = "Слово удалено из списка запрещенных",
	)
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	fun removeBannedWord(
		@RequestParam("word")
		word: String,
	): ResponseEntity<Boolean> {
		contentModerationService.removeBannedWord(word)
		return ResponseEntity(true, HttpStatus.OK)
	}
}
