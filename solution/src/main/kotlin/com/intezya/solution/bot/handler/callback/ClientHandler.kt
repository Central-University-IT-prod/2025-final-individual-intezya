package com.intezya.solution.bot.handler.callback

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.intezya.solution.bot.keyboard.inline.client.clientViewActionsInlineMarkup
import com.intezya.solution.bot.utils.paginateItems
import com.intezya.solution.dto.ClientRequest
import com.intezya.solution.entity.Client
import com.intezya.solution.enums.Gender
import com.intezya.solution.services.ClientService
import io.github.serpro69.kfaker.Faker
import org.springframework.stereotype.Component
import java.util.*

@Component
class ClientHandler(
	private val clientService: ClientService,
) {
	companion object {
		private val faker = Faker()
		private const val ITEMS_PER_PAGE = 5
		private const val DEFAULT_CLIENT_LIST_TEXT = "Все клиенты"
	}

	suspend fun handleAutoCreate(callbackQuery: CallbackQuery, bot: Bot) {
		val (messageId, chatId) = getMessageContext(callbackQuery) ?: return

		val client = generateClient()
		clientService.createOrUpdate(listOf(client))

		sendClientCreatedMessage(messageId, chatId, bot, client)
	}

	suspend fun handleViewAll(callbackQuery: CallbackQuery, bot: Bot, page: Int) {
		val (messageId, chatId) = getMessageContext(callbackQuery) ?: return

		val clients = clientService.getAll()
		val markup = createPaginatedClientList(clients, page)

		bot.editMessageText(
			chatId = ChatId.fromId(chatId), messageId = messageId, text = DEFAULT_CLIENT_LIST_TEXT, replyMarkup = markup
		)
	}

	suspend fun viewClient(callbackQuery: CallbackQuery, bot: Bot, clientId: UUID) {
		val (messageId, chatId) = getMessageContext(callbackQuery) ?: return

		val client = clientService.findById(clientId)?.toEntity() ?: run {
			handleClientNotFound(bot, callbackQuery)
			return
		}

		bot.editMessageText(
			chatId = ChatId.fromId(chatId),
			messageId = messageId,
			text = buildClientInfoMessage(client),
			replyMarkup = clientViewActionsInlineMarkup(clientId)
		)
	}

	private fun buildClientInfoMessage(client: Client): String = """
        🔹 ID: ${client.id}
        📝 Логин: ${client.login}
        🔢 Возраст: ${client.age}
        🌍 Локация: ${client.location}
        👤 Пол: ${client.gender.translate()}
        """.trimIndent()

	private fun createPaginatedClientList(clients: List<Client>, page: Int) = paginateItems<Client>(
		items = clients,
		page = page,
		prefix = "client",
		itemsPerPage = ITEMS_PER_PAGE,
		backData = "menu:clients",
		getId = { it.id.toString() },
		getName = { it.login })

	private fun generateClient() = ClientRequest(
		id = UUID.randomUUID(),
		login = faker.name.firstName(),
		age = faker.random.nextInt(1, 130),
		location = faker.address.country(),
		gender = faker.random.nextEnum<Gender>()
	)

	private suspend fun sendClientCreatedMessage(
		messageId: Long,
		chatId: Long,
		bot: Bot,
		client: ClientRequest,
	) {
		bot.editMessageText(
			chatId = ChatId.fromId(chatId),
			messageId = messageId,
			text = "Клиент успешно создан!\n${buildClientInfoMessage(client.toEntity())}",
			replyMarkup = clientViewActionsInlineMarkup(client.id)
		)
	}

	private fun getMessageContext(callbackQuery: CallbackQuery): Pair<Long, Long>? {
		val messageId = callbackQuery.message?.messageId ?: return null
		val chatId = callbackQuery.message?.chat?.id ?: return null
		return messageId to chatId
	}

	private suspend fun handleClientNotFound(bot: Bot, callbackQuery: CallbackQuery) {
		bot.answerCallbackQuery(
			callbackQueryId = callbackQuery.id, text = "Клиент не найден"
		)
	}
}
