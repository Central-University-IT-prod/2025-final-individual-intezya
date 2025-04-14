package com.intezya.solution.bot.keyboard.inline.advertisement

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.intezya.solution.bot.keyboard.inline.goToMenuInlineMarkup
import java.util.*

fun viewAdvertisementActionsInlineMarkup(advertisementId: UUID, clientId: UUID): InlineKeyboardMarkup {
	return InlineKeyboardMarkup.create(
		buttons = listOf(
			listOf(
				InlineKeyboardButton.CallbackData(
					callbackData = "ad:click_clid=:${clientId}",
					text = "Узнать что дальше...",
				)
			), listOf(
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
