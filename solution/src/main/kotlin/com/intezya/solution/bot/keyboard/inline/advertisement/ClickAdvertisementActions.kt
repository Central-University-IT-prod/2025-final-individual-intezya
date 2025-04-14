package com.intezya.solution.bot.keyboard.inline.advertisement

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.intezya.solution.bot.keyboard.inline.goToMenuInlineMarkup
import java.util.*


fun clickAdvertisementActionsInlineMarkup(advertisementId: UUID, clientId: UUID): InlineKeyboardMarkup {
	return InlineKeyboardMarkup.create(
		buttons = listOf(
			listOf(
				InlineKeyboardButton.CallbackData(
					callbackData = "client:view_${clientId}",
					text = "Назад",
				)
			), listOf(
				goToMenuInlineMarkup
			)
		)
	)
}
