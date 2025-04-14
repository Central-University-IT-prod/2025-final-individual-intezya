package com.intezya.solution.bot.handler.callback

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.CallbackQuery
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.Message
import com.intezya.solution.bot.keyboard.inline.MainMenuInlineMarkup
import com.intezya.solution.bot.keyboard.inline.ModerationStateMenuInlineMarkup
import com.intezya.solution.bot.keyboard.inline.SystemTimeMenuInlineMarkup
import com.intezya.solution.bot.keyboard.inline.advertiser.AdvertiserMenuInlineMarkup
import com.intezya.solution.bot.keyboard.inline.client.ClientMenuInlineMarkup
import com.intezya.solution.bot.keyboard.inline.goToMenuInlineMarkup
import com.intezya.solution.services.GlobalSettingsService
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class MainMenuHandler(
	private val globalSettingsService: GlobalSettingsService,
) {
	companion object {
		private const val GREETING_EMOJI = "👋"
		private const val CLIENTS_EMOJI = "👥"
		private const val ADVERTISERS_EMOJI = "📣"
		private const val MODERATION_EMOJI = "🛡️"
		private const val TIME_EMOJI = "⏰"
		private const val ERROR_EMOJI = "😕"
		private const val ENABLED_EMOJI = "✅"
		private const val DISABLED_EMOJI = "❌"
		private const val MEMO_EMOJI = "📝"
	}

	fun handleStartCommand(message: Message, bot: Bot) {
		runBlocking {
			bot.sendMessage(
				chatId = ChatId.fromId(message.chat.id),
				text = """
                $GREETING_EMOJI Привет! Выбери категорию действий, чтобы продолжить
            """.trimIndent(),
				replyMarkup = MainMenuInlineMarkup,
			)
			clearChat(message.messageId, message.chat.id, bot)
		}
	}

	fun handleStartCallback(callbackQuery: CallbackQuery, bot: Bot) {
		runBlocking {
			val messageId = callbackQuery.message?.messageId ?: return@runBlocking
			val chatId = callbackQuery.message?.chat?.id ?: return@runBlocking
			bot.editMessageText(
				chatId = ChatId.fromId(chatId),
				messageId = messageId,
				text = "$GREETING_EMOJI Привет! Выбери категорию действий, чтобы продолжить",
				replyMarkup = MainMenuInlineMarkup,
			)
		}
	}

	fun handleClientsMenuCallback(callbackQuery: CallbackQuery, bot: Bot) {
		runBlocking {
			val messageId = callbackQuery.message?.messageId ?: return@runBlocking
			val chatId = callbackQuery.message?.chat?.id ?: return@runBlocking
			bot.editMessageText(
				chatId = ChatId.fromId(chatId),
				messageId = messageId,
				text = "$CLIENTS_EMOJI Тут ты можешь выполнить операции над клиентами",
				replyMarkup = ClientMenuInlineMarkup,
			)
		}
	}

	fun handleAdvertisersMenuCallback(callbackQuery: CallbackQuery, bot: Bot) {
		runBlocking {
			val messageId = callbackQuery.message?.messageId ?: return@runBlocking
			val chatId = callbackQuery.message?.chat?.id ?: return@runBlocking
			bot.editMessageText(
				chatId = ChatId.fromId(chatId),
				messageId = messageId,
				text = "$ADVERTISERS_EMOJI Тут ты можешь выполнить операции над рекламодателями",
				replyMarkup = AdvertiserMenuInlineMarkup,
			)
		}
	}

	fun handleModerationStateMenuCallback(callbackQuery: CallbackQuery, bot: Bot) {
		runBlocking {
			val messageId = callbackQuery.message?.messageId ?: return@runBlocking
			val chatId = callbackQuery.message?.chat?.id ?: return@runBlocking
			val moderationEnabled = globalSettingsService.getModerationEnabled()
			val statusEmoji = if (moderationEnabled) ENABLED_EMOJI else DISABLED_EMOJI
			val moderationStatus = if (moderationEnabled) "включена" else "выключена"
			bot.editMessageText(
				chatId = ChatId.fromId(chatId),
				messageId = messageId,
				text = "$MODERATION_EMOJI Модерация: $statusEmoji $moderationStatus",
				replyMarkup = ModerationStateMenuInlineMarkup,
			)
		}
	}

	fun handleChangeModerationStateCallback(callbackQuery: CallbackQuery, bot: Bot) {
		runBlocking {
			val messageId = callbackQuery.message?.messageId ?: return@runBlocking
			val chatId = callbackQuery.message?.chat?.id ?: return@runBlocking
			val currentState = globalSettingsService.getModerationEnabled()
			globalSettingsService.setModerationState(!currentState)
			val newState = globalSettingsService.getModerationEnabled()
			val statusEmoji = if (newState) ENABLED_EMOJI else DISABLED_EMOJI
			val moderationStatus = if (newState) "включена" else "выключена"
			bot.editMessageText(
				chatId = ChatId.fromId(chatId),
				messageId = messageId,
				text = "$MODERATION_EMOJI Модерация: $statusEmoji $moderationStatus",
				replyMarkup = ModerationStateMenuInlineMarkup,
			)
		}
	}

	fun handleSystemTimeMenuCallback(callbackQuery: CallbackQuery, bot: Bot) {
		runBlocking {
			val messageId = callbackQuery.message?.messageId ?: return@runBlocking
			val chatId = callbackQuery.message?.chat?.id ?: return@runBlocking
			bot.editMessageText(
				chatId = ChatId.fromId(chatId),
				messageId = messageId,
				text = "$TIME_EMOJI Текущее системное время: день ${globalSettingsService.getCurrentDate()}",
				replyMarkup = SystemTimeMenuInlineMarkup,
			)
		}
	}

	fun handleSetDateCommand(message: Message, bot: Bot) {
		runBlocking {
			val messageId = message.messageId
			val chatId = message.chat.id
			var date: Int
			val messageIdToEdit = message.replyToMessage?.messageId

			if (messageIdToEdit == null) {
				clearChat(messageId, chatId, bot)
				return@runBlocking
			}

			try {
				date = message.text!!.trim().toInt()
			} catch (e: Exception) {
				bot.editMessageText(
					chatId = ChatId.fromId(chatId),
					messageId = messageIdToEdit,
					text = "$ERROR_EMOJI Некорректная дата :(",
					replyMarkup = InlineKeyboardMarkup.create(listOf(goToMenuInlineMarkup)),
				)
				clearChat(messageId, chatId, bot, messageIdToEdit)
				return@runBlocking
			}

			val res = bot.editMessageText(
				chatId = ChatId.fromId(chatId),
				messageId = messageIdToEdit,
				text = "$TIME_EMOJI Текущее системное время: день $date",
				replyMarkup = SystemTimeMenuInlineMarkup,
			)
			clearChat(messageId, chatId, bot, messageIdToEdit)
			if (res.first?.code() != 200) {
				return@runBlocking
			}
			globalSettingsService.setCurrentDate(date)
		}
	}

	fun handleChangeSystemTimeCallback(callbackQuery: CallbackQuery, bot: Bot) {
		runBlocking {
			val messageId = callbackQuery.message?.messageId ?: return@runBlocking
			val chatId = callbackQuery.message?.chat?.id ?: return@runBlocking
			bot.editMessageText(
				chatId = ChatId.fromId(chatId),
				messageId = messageId,
				text = "$MEMO_EMOJI Введи дату в виде числа, ответив на это сообщение",
				replyMarkup = InlineKeyboardMarkup.create(listOf(goToMenuInlineMarkup)),
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
}
