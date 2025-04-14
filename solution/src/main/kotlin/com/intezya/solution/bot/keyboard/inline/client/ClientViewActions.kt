package com.intezya.solution.bot.keyboard.inline.client

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.intezya.solution.bot.keyboard.inline.goToMenuInlineMarkup
import java.util.*

fun clientViewActionsInlineMarkup(id: UUID): InlineKeyboardMarkup {
	return InlineKeyboardMarkup.create(
		buttons = listOf(
			listOf(
				InlineKeyboardButton.CallbackData("Установить ML Score", "client:set_ml_score:${id}"),
				InlineKeyboardButton.CallbackData("Посмотреть рекламу", "client:view_ads:${id}"),
			), listOf(
				InlineKeyboardButton.CallbackData("Назад", "menu:clients"), goToMenuInlineMarkup
			)
		)
	)
}
