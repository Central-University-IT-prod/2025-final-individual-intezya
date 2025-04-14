package com.intezya.solution.bot.handler.callback

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.intezya.solution.bot.keyboard.inline.advertiser.backToAdvertiserViewInlineMarkup
import com.intezya.solution.bot.keyboard.inline.client.backToClientViewInlineMarkup
import com.intezya.solution.bot.utils.paginateItems
import com.intezya.solution.dto.MLScoreRequest
import com.intezya.solution.entity.Advertiser
import com.intezya.solution.entity.Client
import com.intezya.solution.services.AdvertiserService
import com.intezya.solution.services.ClientService
import com.intezya.solution.services.MLScoreService
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component
import java.util.*

@Component
class MLScoreHandler(
	private val advertiserService: AdvertiserService,
	private val mlScoreService: MLScoreService,
	private val clientService: ClientService,
) {
	companion object {
		private const val ERROR_EMOJI = "😕"
	}

	fun handleSetMLScoreForClient(callbackQuery: CallbackQuery, bot: Bot, clientId: UUID) {
		runBlocking {
			val messageId = callbackQuery.message?.messageId ?: return@runBlocking
			val chatId = callbackQuery.message?.chat?.id ?: return@runBlocking

			bot.editMessageText(
				chatId = ChatId.fromId(chatId),
				messageId = messageId,
				text = """
					ID: $clientId
					
					Введи ML score в виде числа, ответив на это сообщение
				""".trimIndent(),
				replyMarkup = backToClientViewInlineMarkup(clientId),
			)
		}
	}

	fun handleSetMLScoreForClientState(message: Message, bot: Bot, clientId: UUID) {
		runBlocking {
			val messageId = message.messageId
			val chatId = message.chat.id
			var score: Int
			val messageIdToEdit = message.replyToMessage?.messageId


			if (messageIdToEdit == null) {
				clearChat(messageId, chatId, bot)
				return@runBlocking
			}

			try {
				score = message.text!!.trim().toInt()
			} catch (e: Exception) {
				bot.editMessageText(
					chatId = ChatId.fromId(chatId),
					messageId = messageIdToEdit,
					text = "$ERROR_EMOJI Некорректное значение :(",
					replyMarkup = backToClientViewInlineMarkup(clientId),
				)
				clearChat(messageId, chatId, bot, messageIdToEdit)
				return@runBlocking
			}

			val advertisers = advertiserService.getAll()
			val markup = createPaginatedAdvertiserList(advertisers, 0, clientId)

			val res = bot.editMessageText(
				chatId = ChatId.fromId(chatId),
				messageId = messageIdToEdit,
				text = "Отлично! Теперь выбери рекламодателя \n(ID клиента: $clientId, ML score: $score)",
				replyMarkup = markup,
			)
			clearChat(messageId, chatId, bot, messageIdToEdit)
			if (res.first?.code() != 200) {
				return@runBlocking
			}
		}
	}

	fun setMLScoreForClientWithAdvertiser(callbackQuery: CallbackQuery, bot: Bot) {
		runBlocking {
			val data = callbackQuery.message!!.text!!.split(", ")
			val clientId = UUID.fromString(data[0].split(": ")[1])
			val score = data[1].split(": ")[1].split(")")[0].toInt()
			val messageId = callbackQuery.message?.messageId ?: return@runBlocking
			val chatId = callbackQuery.message?.chat?.id ?: return@runBlocking
			val advertiserId = UUID.fromString(callbackQuery.data.split("_")[1])

			if (score < 0) {
				bot.editMessageText(
					chatId = ChatId.fromId(chatId),
					messageId = messageId,
					text = "ML Score не может быть меньше 0!",
					replyMarkup = backToClientViewInlineMarkup(clientId),
				)
				return@runBlocking
			}


			val mlScore = MLScoreRequest(
				clientId = clientId,
				advertiserId = advertiserId,
				score = score,
			)

			mlScoreService.create(mlScore)

			bot.editMessageText(
				chatId = ChatId.fromId(chatId),
				messageId = messageId,
				text = "Успешно!",
				replyMarkup = backToClientViewInlineMarkup(clientId),
			)
		}
	}

	fun handleSetMLScoreForAdvertiser(callbackQuery: CallbackQuery, bot: Bot, advertiserId: UUID) {
		runBlocking {
			val messageId = callbackQuery.message?.messageId ?: return@runBlocking
			val chatId = callbackQuery.message?.chat?.id ?: return@runBlocking

			bot.editMessageText(
				chatId = ChatId.fromId(chatId),
				messageId = messageId,
				text = """
					ID: $advertiserId
					
					Введи ML score в виде числа, ответив на это сообщение
				""".trimIndent(),
				replyMarkup = backToAdvertiserViewInlineMarkup(advertiserId),
			)
		}
	}

	fun handleSetMLScoreForAdvertiserState(message: Message, bot: Bot, advertiserId: UUID) {
		runBlocking {
			val messageId = message.messageId
			val chatId = message.chat.id
			var score: Int
			val messageIdToEdit = message.replyToMessage?.messageId


			if (messageIdToEdit == null) {
				clearChat(messageId, chatId, bot)
				return@runBlocking
			}

			try {
				score = message.text!!.trim().toInt()
			} catch (e: Exception) {
				bot.editMessageText(
					chatId = ChatId.fromId(chatId),
					messageId = messageIdToEdit,
					text = "$ERROR_EMOJI Некорректное значение :(",
					replyMarkup = backToAdvertiserViewInlineMarkup(advertiserId),
				)
				clearChat(messageId, chatId, bot, messageIdToEdit)
				return@runBlocking
			}

			val clients = clientService.getAll()
			val markup = createPaginatedClientList(clients, 0, advertiserId)

			val res = bot.editMessageText(
				chatId = ChatId.fromId(chatId),
				messageId = messageIdToEdit,
				text = "Отлично! Теперь выбери клиента  \n(ID рекламодателя: $advertiserId, ML score: $score)",
				replyMarkup = markup,
			)
			clearChat(messageId, chatId, bot, messageIdToEdit)
			if (res.first?.code() != 200) {
				return@runBlocking
			}
		}
	}

	fun setMLScoreForAdvertiserWithClient(callbackQuery: CallbackQuery, bot: Bot) {
		runBlocking {
			val data = callbackQuery.message!!.text!!.split(", ")
			val advertiserId = UUID.fromString(data[0].split(": ")[1])
			val score = data[1].split(": ")[1].split(")")[0].toInt()
			val messageId = callbackQuery.message?.messageId ?: return@runBlocking
			val chatId = callbackQuery.message?.chat?.id ?: return@runBlocking
			val clientId = UUID.fromString(callbackQuery.data.split("_")[1])

			if (score < 0) {
				bot.editMessageText(
					chatId = ChatId.fromId(chatId),
					messageId = messageId,
					text = "ML Score не может быть меньше 0!",
					replyMarkup = backToAdvertiserViewInlineMarkup(advertiserId),
				)
				return@runBlocking
			}


			val mlScore = MLScoreRequest(
				clientId = clientId,
				advertiserId = advertiserId,
				score = score,
			)

			mlScoreService.create(mlScore)

			bot.editMessageText(
				chatId = ChatId.fromId(chatId),
				messageId = messageId,
				text = "Успешно!",
				replyMarkup = backToAdvertiserViewInlineMarkup(advertiserId),
			)
		}
	}

	private fun clearChat(messageId: Long, chatId: Long, bot: Bot, vararg toExclude: Long? = emptyArray()) {
		for (i in messageId downTo 1) {
			if (i in toExclude) {
				continue
			}
			val result = bot.deleteMessage(
				chatId = ChatId.fromId(chatId),
				messageId = i,
			)
			if (result.isError) {
				break
			}
		}
	}

	private fun createPaginatedAdvertiserList(advertisers: List<Advertiser>, page: Int, clientId: UUID) =
		paginateItems<Advertiser>(
			items = advertisers,
			page = page,
			prefix = "mlsCL",
			itemsPerPage = 3,
			backData = "client:view_${clientId}",
			getId = { it.id.toString() },
			getName = { it.name })

	private fun createPaginatedClientList(clients: List<Client>, page: Int, clientId: UUID) = paginateItems<Client>(
		items = clients,
		page = page,
		prefix = "mlsAD",
		itemsPerPage = 3,
		backData = "advertiser:view_${clientId}",
		getId = { it.id.toString() },
		getName = { it.login })

}
