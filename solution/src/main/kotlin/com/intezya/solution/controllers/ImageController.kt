package com.intezya.solution.controllers

import com.intezya.solution.services.ImageService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping
@Tag(
	name = "Campaigns",
	description = "Управление рекламными кампаниями: создание, обновление, удаление и получение списка кампаний.",
)
class ImageController(
	private val imageService: ImageService,
) {
	@GetMapping("/advertisers/{advertiserId}/campaigns/{advertisementId}/image")
	@Operation(
		summary = "Получение картинки рекламной кампании",
		description = "Возвращает картинку рекламной кампании в формате JPEG.",
	)
	@ApiResponse(
		responseCode = "200",
		description = "Картинка успешно получена.",
		content = [Content(mediaType = "image/jpeg")],
	)
	@ApiResponse(responseCode = "204", description = "Рекламная кампания не имеет картинки.")
	@ApiResponse(responseCode = "404", description = "Рекламная кампания не найдена.")
	fun getImage(
		@PathVariable("advertiserId")
		advertiserId: UUID,
		@PathVariable("advertisementId")
		advertisementId: UUID,
	): ResponseEntity<ByteArray> {
		val imageStream = imageService.getAdvertisementImage(advertiserId)

		val imageBytes = imageStream.use { it.readBytes() }

		return if (imageBytes.isEmpty()) {
			ResponseEntity(HttpStatus.NO_CONTENT)
		} else {
			val headers = HttpHeaders().apply {
				contentType = MediaType.IMAGE_JPEG
				contentLength = imageBytes.size.toLong()
			}
			ResponseEntity(imageBytes, headers, HttpStatus.OK)
		}

	}
}
