package com.intezya.solution.bot.handler.callback

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.intezya.solution.bot.keyboard.inline.advertisement.advertisementViewActionsInlineMarkup
import com.intezya.solution.bot.keyboard.inline.advertisement.clickAdvertisementActionsInlineMarkup
import com.intezya.solution.bot.keyboard.inline.advertisement.viewAdvertisementActionsInlineMarkup
import com.intezya.solution.bot.utils.paginateItems
import com.intezya.solution.dto.AdView
import com.intezya.solution.dto.AdvertisementCreateRequest
import com.intezya.solution.dto.AdvertisementView
import com.intezya.solution.dto.AdvertiserRequest
import com.intezya.solution.entity.Advertisement
import com.intezya.solution.entity.Targeting
import com.intezya.solution.enums.TargetingGender
import com.intezya.solution.services.AdvertisementService
import com.intezya.solution.services.AdvertiserService
import com.intezya.solution.services.GenerativeService
import io.github.serpro69.kfaker.Faker
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Component
class AdvertisementHandler(
	private val advertiserService: AdvertiserService,
	private val advertisementService: AdvertisementService,
	private val generativeService: GenerativeService,
) {
	companion object {
		private val faker = Faker()
		private const val ITEMS_PER_PAGE = 3
		private const val AD_ID_START_INDEX = 4
		private const val AD_ID_END_INDEX = 40

		private val IMPRESSIONS_RANGE = 1000..10000
		private val CLICKS_RANGE = 10..1000
		private val COST_RANGE = 1..20
		private val START_DATE_RANGE = 1..20
		private val END_DATE_RANGE = 20..40
		private val AGE_FROM_RANGE = 1..50
		private val AGE_TO_RANGE = 50..130
	}

	suspend fun handleAutoCreate(callbackQuery: CallbackQuery, bot: Bot, advertiserId: UUID) {
		val (messageId, chatId) = getMessageContext(callbackQuery) ?: return
		val createRequest = generateAdvertisement()
		val advertisement = advertisementService.create(advertiserId, createRequest)
		val advertiser = advertiserService.findById(advertisement.advertiserId)
			?: throw IllegalStateException("Advertiser not found")
		try {
			sendAdvertisementCreatedMessage(messageId, chatId, bot, advertisement, advertiser)
		} catch (e: Exception) {
			handleError(bot, callbackQuery, "Ошибка при создании объявления: " + e.message)
		}
	}

	suspend fun handleViewAll(callbackQuery: CallbackQuery, bot: Bot, page: Int, advertiserId: UUID) {
		val (messageId, chatId) = getMessageContext(callbackQuery) ?: return

		try {
			val advertisements = advertisementService.getByAdvertiserId(advertiserId)
			val markup = createPaginatedAdvertisementList(advertisements, page, advertiserId)

			bot.editMessageText(
				chatId = ChatId.fromId(chatId),
				messageId = messageId,
				text = "Ваши рекламные объявления:",
				replyMarkup = markup
			)
		} catch (e: Exception) {
			handleError(bot, callbackQuery, "Ошибка при получении списка объявлений")
		}
	}

	suspend fun handleClientView(callbackQuery: CallbackQuery, bot: Bot, clientId: UUID) {
		val (messageId, chatId) = getMessageContext(callbackQuery) ?: return

		try {
			val advertisement = advertisementService.getEligibleAdvertisement(clientId)
			sendClientAdvertisementView(messageId, chatId, bot, advertisement, clientId)
		} catch (e: ResponseStatusException) {
			handleError(bot, callbackQuery, "Подходящих объявлений не найдено")
		} catch (e: Exception) {
			handleError(bot, callbackQuery, "Ошибка при показе объявления")
		}
	}

	suspend fun handleClientClick(callbackQuery: CallbackQuery, bot: Bot, clientId: UUID) {
		val (messageId, chatId) = getMessageContext(callbackQuery) ?: return

		try {
			val advertisementId = extractAdvertisementId(callbackQuery.message?.text)
			advertisementService.clickAd(clientId, advertisementId)

			sendClickConfirmation(messageId, chatId, bot, advertisementId, clientId)
		} catch (e: Exception) {
			handleError(bot, callbackQuery, "Ошибка при обработке клика")
		}
	}

	suspend fun handleView(callbackQuery: CallbackQuery, bot: Bot, advertisementId: UUID) {
		val (messageId, chatId) = getMessageContext(callbackQuery) ?: return

		try {
			val advertisement = advertisementService.getByIdForTGBot(advertisementId)
				?: throw IllegalStateException("Advertisement not found")

			val advertiser = advertiserService.findById(advertisement.advertiserId)
				?: throw IllegalStateException("Advertiser not found")

			sendAdvertisementDetails(messageId, chatId, bot, advertisement, advertiser)
		} catch (e: Exception) {
			handleError(bot, callbackQuery, "Ошибка при просмотре объявления")
		}
	}

	private suspend fun generateAdvertisement(): AdvertisementCreateRequest {
		val title = faker.marketing.buzzwords()
		var text = ""
		try {
			text = generativeService.advertisementDescription(title) ?: "Не удалось сгенерировать"
		} catch (e: Exception) {
			println(e)
		}
		return AdvertisementCreateRequest(
			title = title,
			text = text,
			impressionsLimit = faker.random.nextInt(IMPRESSIONS_RANGE.first, IMPRESSIONS_RANGE.last),
			clicksLimit = faker.random.nextInt(CLICKS_RANGE.first, CLICKS_RANGE.last),
			costPerImpression = faker.random.nextInt(COST_RANGE.first, COST_RANGE.last).toDouble(),
			costPerClick = faker.random.nextInt(COST_RANGE.first, COST_RANGE.last).toDouble(),
			startDate = faker.random.nextInt(START_DATE_RANGE.first, START_DATE_RANGE.last),
			endDate = faker.random.nextInt(END_DATE_RANGE.first, END_DATE_RANGE.last),
			targeting = generateTargeting()
		)
	}

	private fun generateTargeting() = Targeting(
		gender = faker.random.nextEnum<TargetingGender>(),
		ageFrom = faker.random.nextInt(AGE_FROM_RANGE.first, AGE_FROM_RANGE.last),
		ageTo = faker.random.nextInt(AGE_TO_RANGE.first, AGE_TO_RANGE.last),
		location = faker.address.country()
	)

	private fun createPaginatedAdvertisementList(
		advertisements: List<AdvertisementView>,
		page: Int,
		advertiserId: UUID,
	) = paginateItems<AdvertisementView>(
		items = advertisements,
		page = page,
		prefix = "ads",
		itemsPerPage = ITEMS_PER_PAGE,
		backData = "advertiser:view_$advertiserId",
		getId = { it.id.toString() },
		getName = { it.title })

	private fun getMessageContext(callbackQuery: CallbackQuery): Pair<Long, Long>? {
		val messageId = callbackQuery.message?.messageId ?: return null
		val chatId = callbackQuery.message?.chat?.id ?: return null
		return messageId to chatId
	}

	private fun extractAdvertisementId(text: String?): UUID {
		text ?: throw IllegalArgumentException("Message text is null")
		return UUID.fromString(text.substring(AD_ID_START_INDEX, AD_ID_END_INDEX))
	}

	private suspend fun handleError(bot: Bot, callbackQuery: CallbackQuery, errorMessage: String) {
		bot.answerCallbackQuery(
			callbackQueryId = callbackQuery.id, text = errorMessage
		)
	}

	private suspend fun sendAdvertisementCreatedMessage(
		messageId: Long,
		chatId: Long,
		bot: Bot,
		advertisement: AdvertisementView,
		advertiser: AdvertiserRequest,
	) {
		bot.editMessageText(
			chatId = ChatId.fromId(chatId), messageId = messageId, text = "Объявление успешно создано!\n${
				buildAdvertisementInfoMessage(
					advertisement.toEntity(), advertiser.name
				)
			}", replyMarkup = advertisementViewActionsInlineMarkup(advertisement.id, advertisement.advertiserId)
		)
	}

	private suspend fun sendClientAdvertisementView(
		messageId: Long,
		chatId: Long,
		bot: Bot,
		advertisement: AdView,
		clientId: UUID,
	) {
		bot.editMessageText(
			chatId = ChatId.fromId(chatId),
			messageId = messageId,
			text = buildClientAdvertisementView(advertisement),
			replyMarkup = viewAdvertisementActionsInlineMarkup(advertisement.id, clientId)
		)
	}

	private suspend fun sendClickConfirmation(
		messageId: Long,
		chatId: Long,
		bot: Bot,
		advertisementId: UUID,
		clientId: UUID,
	) {
		bot.editMessageText(
			chatId = ChatId.fromId(chatId),
			messageId = messageId,
			text = "Вы перешли по объявлению!",
			replyMarkup = clickAdvertisementActionsInlineMarkup(advertisementId, clientId)
		)
	}

	private suspend fun sendAdvertisementDetails(
		messageId: Long,
		chatId: Long,
		bot: Bot,
		advertisement: AdvertisementView,
		advertiser: AdvertiserRequest,
	) {
		bot.editMessageText(
			chatId = ChatId.fromId(chatId),
			messageId = messageId,
			text = "Вот ваше объявление:\n${buildAdvertisementInfoMessage(advertisement.toEntity(), advertiser.name)}",
			replyMarkup = advertisementViewActionsInlineMarkup(advertisement.id, advertisement.advertiserId)
		)
	}

	private fun buildClientAdvertisementView(advertisement: AdView): String = """
        ID: ${advertisement.id}
        
        ${advertisement.title}
        ${advertisement.text}
        """.trimIndent()

	private fun buildAdvertisementInfoMessage(advertisement: Advertisement, advertiserName: String): String = """
        🔹 ID объявления: ${advertisement.id}
        🔹 Рекламодатель: $advertiserName
        
        📌 Заголовок: ${advertisement.title}
        📝 Описание: ${advertisement.text}
        
        🎯 Таргетинг:
        - Пол: ${advertisement.targetingGender?.translate() ?: "Не задан"}
        - Возраст: ${advertisement.targetingAgeFrom ?: "Не задан"} - ${advertisement.targetingAgeTo ?: "Не задан"}
        - Локация: ${advertisement.targetingLocation ?: "Не задана"}
        
        📊 Ограничения:
        - Лимит показов: ${advertisement.impressionsLimit}
        - Лимит кликов: ${advertisement.clicksLimit}
        
        📈 Текущие показатели:
        - Показов: ${advertisement.currentImpressions}
        - Кликов: ${advertisement.currentClicks}
        
        💰 Стоимость:
        - Цена за показ: ${advertisement.costPerImpression} ₽
        - Цена за клик: ${advertisement.costPerClick} ₽
        
        ⏳ Период показа:
        - Начало: ${advertisement.startDate}
        - Завершение: ${advertisement.endDate}
        
        🖼 Изображение: ${advertisement.imageUrl ?: "Не загружено"}
        """.trimIndent()
}
