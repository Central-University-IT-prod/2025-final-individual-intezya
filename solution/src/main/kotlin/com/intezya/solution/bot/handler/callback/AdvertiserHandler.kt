package com.intezya.solution.bot.handler.callback

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.intezya.solution.bot.keyboard.inline.advertiser.advertiserViewActionsInlineMarkup
import com.intezya.solution.bot.keyboard.inline.advertiser.statisticViewedActionsInlineMarkup
import com.intezya.solution.bot.utils.paginateItems
import com.intezya.solution.dto.AdvertiserRequest
import com.intezya.solution.dto.DailyStatistic
import com.intezya.solution.dto.Statistic
import com.intezya.solution.entity.Advertiser
import com.intezya.solution.services.AdvertiserService
import com.intezya.solution.services.StatisticService
import io.github.serpro69.kfaker.Faker
import org.springframework.stereotype.Component
import java.util.*

@Component
class AdvertiserHandler(
	private val advertiserService: AdvertiserService,
	private val statisticService: StatisticService,
) {
	companion object {
		private val faker = Faker()
		private const val ITEMS_PER_PAGE = 5
		private const val DEFAULT_ADVERTISER_LIST_TEXT = "Все рекламодатели"
	}

	suspend fun handleAutoCreate(callbackQuery: CallbackQuery, bot: Bot) {
		val (messageId, chatId) = getMessageContext(callbackQuery) ?: return

		val advertiser = generateAdvertiser()
		advertiserService.createOrUpdate(listOf(advertiser))

		sendAdvertiserCreatedMessage(messageId, chatId, bot, advertiser)
	}

	suspend fun handleViewAll(callbackQuery: CallbackQuery, bot: Bot, page: Int) {
		val (messageId, chatId) = getMessageContext(callbackQuery) ?: return

		val advertisers = advertiserService.getAll()
		val markup = createPaginatedAdvertiserList(advertisers, page)

		bot.editMessageText(
			chatId = ChatId.fromId(chatId),
			messageId = messageId,
			text = DEFAULT_ADVERTISER_LIST_TEXT,
			replyMarkup = markup
		)
	}

	suspend fun viewAdvertiser(callbackQuery: CallbackQuery, bot: Bot, advertiserId: UUID) {
		val (messageId, chatId) = getMessageContext(callbackQuery) ?: return

		val advertiser = advertiserService.findById(advertiserId)?.toEntity() ?: run {
			handleAdvertiserNotFound(bot, callbackQuery)
			return
		}

		bot.editMessageText(
			chatId = ChatId.fromId(chatId),
			messageId = messageId,
			text = buildAdvertiserInfoMessage(advertiser),
			replyMarkup = advertiserViewActionsInlineMarkup(advertiserId)
		)
	}

	suspend fun handleStatistic(callbackQuery: CallbackQuery, bot: Bot, advertiserId: UUID) {
		val (messageId, chatId) = getMessageContext(callbackQuery) ?: return

		val statistic = statisticService.advertiserStatistic(advertiserId)

		bot.editMessageText(
			chatId = ChatId.fromId(chatId),
			messageId = messageId,
			text = "Ваша общая статистика:\n${buildStatisticInfoMessage(statistic)}",
			replyMarkup = statisticViewedActionsInlineMarkup(advertiserId)
		)
	}

	suspend fun handleDailyStatistic(
		callbackQuery: CallbackQuery,
		bot: Bot,
		advertiserId: UUID,
		page: Int,
		dateToView: Int = -1,
	) {
		val (messageId, chatId) = getMessageContext(callbackQuery) ?: return

		val statistics = statisticService.advertiserDailyStatistic(advertiserId)
		val text = when {
			dateToView != -1 -> statistics.firstOrNull { it.date == dateToView }
				?.let { buildDailyStatisticInfoMessage(it) } ?: run {
				handleStatisticNotFound(bot, callbackQuery)
				return
			}

			else -> "Выбери день, за который хочешь посмотреть статистику"
		}

		bot.editMessageText(
			chatId = ChatId.fromId(chatId),
			messageId = messageId,
			text = text,
			replyMarkup = createPaginatedDailyStatistics(statistics, page, advertiserId)
		)
	}

	private fun createPaginatedAdvertiserList(advertisers: List<Advertiser>, page: Int) = paginateItems<Advertiser>(
		items = advertisers,
		page = page,
		prefix = "advertiser",
		itemsPerPage = ITEMS_PER_PAGE,
		backData = "menu:advertisers",
		getId = { it.id.toString() },
		getName = { it.name })

	private fun createPaginatedDailyStatistics(
		statistics: List<DailyStatistic>,
		page: Int,
		advertiserId: UUID,
	) = paginateItems<DailyStatistic>(
		items = statistics,
		page = page,
		prefix = "adv:st_$page",
		itemsPerPage = ITEMS_PER_PAGE,
		backData = "advertiser:view_$advertiserId",
		getId = { "${advertiserId}:${it.date}" },
		getName = { "День: ${it.date}" })

	private fun buildStatisticInfoMessage(statistic: Statistic): String = """
        Количество просмотров: ${statistic.impressionsCount}
        Количество переходов: ${statistic.clicksCount}
        Конверсия: ${statistic.conversion}
        Потрачено на просмотры: ${statistic.spentImpressions}
        Потрачено на переходы: ${statistic.spentClicks}
        Потрачено всего: ${statistic.spentTotal}
        """.trimIndent()

	private fun buildDailyStatisticInfoMessage(statistic: DailyStatistic): String = """
        День: ${statistic.date}
        Количество просмотров: ${statistic.impressionsCount}
        Количество переходов: ${statistic.clicksCount}
        Конверсия: ${statistic.conversion}
        Потрачено на просмотры: ${statistic.spentImpressions}
        Потрачено на переходы: ${statistic.spentClicks}
        Потрачено всего: ${statistic.spentTotal}
        """.trimIndent()

	private fun buildAdvertiserInfoMessage(advertiser: Advertiser): String = """
        🔹 ID: ${advertiser.id}
        📌 Название: ${advertiser.name}
        """.trimIndent()

	private fun generateAdvertiser() = AdvertiserRequest(
		id = UUID.randomUUID(), name = faker.name.firstName()
	)

	private suspend fun sendAdvertiserCreatedMessage(
		messageId: Long,
		chatId: Long,
		bot: Bot,
		advertiser: AdvertiserRequest,
	) {
		bot.editMessageText(
			chatId = ChatId.fromId(chatId),
			messageId = messageId,
			text = "Рекламодатель успешно создан!\n${buildAdvertiserInfoMessage(advertiser.toEntity())}",
			replyMarkup = advertiserViewActionsInlineMarkup(advertiser.id)
		)
	}

	private fun getMessageContext(callbackQuery: CallbackQuery): Pair<Long, Long>? {
		val messageId = callbackQuery.message?.messageId ?: return null
		val chatId = callbackQuery.message?.chat?.id ?: return null
		return messageId to chatId
	}

	private suspend fun handleAdvertiserNotFound(bot: Bot, callbackQuery: CallbackQuery) {
		bot.answerCallbackQuery(
			callbackQueryId = callbackQuery.id, text = "Рекламодатель не найден"
		)
	}

	private suspend fun handleStatisticNotFound(bot: Bot, callbackQuery: CallbackQuery) {
		bot.answerCallbackQuery(
			callbackQueryId = callbackQuery.id, text = "Статистика не найдена"
		)
	}
}
