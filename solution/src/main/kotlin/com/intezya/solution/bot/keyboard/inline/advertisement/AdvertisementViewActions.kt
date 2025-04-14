package com.intezya.solution.bot.keyboard.inline.advertisement

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.intezya.solution.bot.keyboard.inline.goToMenuInlineMarkup
import java.util.*

fun advertisementViewActionsInlineMarkup(id: UUID, advertiserId: UUID): InlineKeyboardMarkup {
	return InlineKeyboardMarkup.create(
		buttons = listOf(
			listOf(
				InlineKeyboardButton.CallbackData(
					text = "Просмотр рекламодателя",
					callbackData = "advertiser:view_${advertiserId}", // todo
				),
			), listOf(
				InlineKeyboardButton.CallbackData(
					text = "Статистика",
					callbackData = "advertisement:stat:${id}",
				),
				InlineKeyboardButton.CallbackData(
					text = "Ежедневная статистика",
					callbackData = "advertisement:stat_daily:${id}",
				),
			), listOf(
				goToMenuInlineMarkup
			)
		)
	)
}
