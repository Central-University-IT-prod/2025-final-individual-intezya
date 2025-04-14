package com.intezya.solution.controllers

import com.intezya.solution.dto.*
import com.intezya.solution.services.AdvertisementService
import com.intezya.solution.services.GenerativeService
import com.intezya.solution.utils.validator.ImageUrlValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*


@RestController
@RequestMapping("/advertisers/{advertiserId}/campaigns")
@Tag(
	name = "Campaigns",
	description = "Управление рекламными кампаниями: создание, обновление, удаление и получение списка кампаний.",
)
class AdvertisementController(
	private val service: AdvertisementService,
	private val imageUrlValidator: ImageUrlValidator,
	private val generativeService: GenerativeService,
) {
	@PostMapping("")
	@Operation(
		summary = "Создание рекламной кампании",
		description = "Создаёт новую рекламную кампанию для указанного рекламодателя.",
	)
	@ApiResponse(responseCode = "201", description = "Рекламная кампания успешно создана.")
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	suspend fun create(
		@PathVariable("advertiserId")
		advertiserId: UUID,
		@Valid
		@RequestBody
		advertisementCreate: AdvertisementCreateRequest,
	): ResponseEntity<Any> {
		imageUrlValidator.validateImageUrl(advertisementCreate.imageUrl)
		val advertisement = service.create(advertiserId, advertisementCreate)
		return ResponseEntity(advertisement, HttpStatus.CREATED)
	}

	@PostMapping("/generate")
	@Operation(
		summary = "Сгенерировать описание",
		description = "Генерирует описание по предоставленному заголовку.",
	)
	@ApiResponse(responseCode = "200", description = "Текст сгенерирован")
	@ApiResponse(responseCode = "503", description = "Сервис недоступен :(")
	suspend fun generateDescription(
		@RequestBody
		@Valid
		request: GenerateDescriptionRequest,
	): ResponseEntity<ResultResponse<String>> {
		if (request.title.isBlank()) return ResponseEntity(HttpStatus.BAD_REQUEST)
		val result = generativeService.advertisementDescription(request.title)
			?: return ResponseEntity(HttpStatus.SERVICE_UNAVAILABLE)
		return ResponseEntity(ResultResponse(result), HttpStatus.OK)
	}

	@GetMapping("/{advertisementId}")
	@Operation(
		summary = "Получение кампании по ID",
		description = "Создаёт новую рекламную кампанию для указанного рекламодателя.",
	)
	@ApiResponse(responseCode = "200", description = "Кампания успешно получена.")
	@ApiResponse(responseCode = "403", description = "Кампания вам не принадлежит!")
	@ApiResponse(responseCode = "404", description = "Кампания не найдена.")
	fun getById(
		@PathVariable("advertiserId")
		advertiserId: UUID,
		@PathVariable("advertisementId")
		advertisementId: UUID,
	): ResponseEntity<AdvertisementView> {
		return ResponseEntity(
			service.getById(advertiserId, advertisementId),
			HttpStatus.OK,
		)
	}

	@DeleteMapping("/{advertisementId}")
	@Operation(
		summary = "Удаление рекламной кампании",
		description = "Удаляет рекламную кампанию рекламодателя по заданному campaignId.",
	)
	@ApiResponse(responseCode = "204", description = "Рекламная кампания успешно удалена.")
	@ApiResponse(responseCode = "403", description = "Кампания вам не принадлежит!")
	@ApiResponse(responseCode = "404", description = "Кампания не найдена.")
	fun deleteById(
		@PathVariable("advertiserId")
		advertiserId: UUID,
		@PathVariable("advertisementId")
		advertisementId: UUID,
	): ResponseEntity<AdvertisementView> {
		return ResponseEntity(
			service.deleteById(advertiserId, advertisementId),
			HttpStatus.NO_CONTENT,
		)
	}

	@PutMapping("/{advertisementId}")
	@Operation(
		summary = "Обновление рекламной кампании",
		description = "Обновляет разрешённые параметры рекламной кампании до её старта.",
	)
	@ApiResponse(responseCode = "200", description = "Рекламная кампания успешно обновлена.")
	@ApiResponse(responseCode = "403", description = "Кампания вам не принадлежит!")
	@ApiResponse(responseCode = "404", description = "Кампания не найдена.")
	fun updateById(
		@PathVariable("advertiserId")
		advertiserId: UUID,
		@PathVariable("advertisementId")
		advertisementId: UUID,
		@Valid
		@RequestBody
		advertisementEdit: AdvertisementEditRequest,
	): ResponseEntity<AdvertisementView> {
		imageUrlValidator.validateImageUrl(advertisementEdit.imageUrl)
		return ResponseEntity(
			service.updateById(advertiserId, advertisementId, advertisementEdit),
			HttpStatus.OK,
		)
	}

	@GetMapping("")
	@Operation(
		summary = "Получение рекламных кампаний рекламодателя c пагинацией",
		description = "Возвращает список рекламных кампаний для указанного рекламодателя с пагинацией.",
	)
	@ApiResponse(responseCode = "200", description = "Список рекламных кампаний рекламодателя.")
	@ApiResponse(responseCode = "204", description = "Список рекламных кампаний рекламодателя пуст.")
	@ApiResponse(responseCode = "404", description = "Рекламодатель не найден.")
	fun getAllByAdvertiserId(
		@PathVariable
		advertiserId: UUID,
		@RequestParam(required = false, defaultValue = "10")
		size: Int,
		@RequestParam(required = false, defaultValue = "0")
		page: Int,
	): ResponseEntity<out Any> {
		val page = PageRequest.of(page, size)

		val result = service.getByAdvertiserIdPaged(advertiserId, page)
		if (result.isEmpty) {
			return ResponseEntity(HttpStatus.NO_CONTENT)
		}
		return ResponseEntity(
			result.content,
			HttpStatus.OK,
		)
	}
}
