package com.intezya.solution.controllers

import com.intezya.solution.services.StatisticService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("/stats")
@Tag(
	name = "Statistics",
	description = "Получение статистики по кампаниям и рекламодателям, а также ежедневной статистики.",
)
class StatisticController(
	private val statisticService: StatisticService,
) {
	@GetMapping("/campaigns/{advertisementId}/daily")
	@Operation(
		summary = "Получение ежедневной статистики по рекламной кампании",
		description = "Возвращает массив ежедневной статистики для указанной рекламной кампании.",
	)
	@ApiResponse(responseCode = "200", description = "Ежедневная статистика по рекламной кампании успешно получена.")
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	fun advertisementDailyStatistic(
		@PathVariable("advertisementId")
		advertisementId: UUID,
	) = statisticService.advertisementDailyStatistic(advertisementId)

	@GetMapping("/campaigns/{advertisementId}")
	@Operation(
		summary = "Получение статистики по рекламной кампании",
		description = """
		Возвращает агрегированную статистику (показы, переходы, затраты и конверсию) для заданной рекламной кампании.
		""",
	)
	@ApiResponse(responseCode = "200", description = "Статистика по рекламной кампании успешно получена.")
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	fun advertisementStatistic(
		@PathVariable("advertisementId")
		advertisementId: UUID,
	) = statisticService.advertisementStatistic(advertisementId)

	@GetMapping("/advertisers/{advertiserId}/campaigns/daily")
	@Operation(
		summary = "Получение ежедневной агрегированной статистики по всем кампаниям рекламодателя",
		description = """
			Возвращает массив ежедневной сводной статистики по всем рекламным кампаниям заданного рекламодателя.
		""",
	)
	@ApiResponse(
		responseCode = "200", description = "Ежедневная агрегированная статистика успешно получена."
	)
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	fun advertiserDailyStatistic(
		@PathVariable("advertiserId")
		advertiserId: UUID,
	) = statisticService.advertiserDailyStatistic(advertiserId)

	@GetMapping("/advertisers/{advertiserId}/campaigns")
	@Operation(
		summary = "Получение агрегированной статистики по всем кампаниям рекламодателя",
		description = """
			Возвращает сводную статистику по всем рекламным кампаниям, принадлежащим заданному рекламодателю.
		""",
	)
	@ApiResponse(
		responseCode = "200",
		description = "Агрегированная статистика по всем кампаниям рекламодателя успешно получена."
	)
	@ApiResponse(responseCode = "400", description = "Невалидные входные данные")
	fun advertiserStatistic(
		@PathVariable("advertiserId")
		advertiserId: UUID,
	) = statisticService.advertiserStatistic(advertiserId)
}
