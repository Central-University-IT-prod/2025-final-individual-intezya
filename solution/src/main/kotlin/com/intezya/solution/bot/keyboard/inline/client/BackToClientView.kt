package com.intezya.solution.bot.keyboard.inline.client

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.intezya.solution.bot.keyboard.inline.goToMenuInlineMarkup
import java.util.*

fun backToClientViewInlineMarkup(clientId: UUID): InlineKeyboardMarkup {
	return InlineKeyboardMarkup.create(
		buttons = listOf(
			listOf(
				InlineKeyboardButton.CallbackData("Назад", "client:view_${clientId}")
			), listOf(
				goToMenuInlineMarkup
			)
		)
	)
}
