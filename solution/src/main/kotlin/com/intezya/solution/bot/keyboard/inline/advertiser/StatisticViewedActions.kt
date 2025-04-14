package com.intezya.solution.bot.keyboard.inline.advertiser

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.intezya.solution.bot.keyboard.inline.goToMenuInlineMarkup
import java.util.*

fun statisticViewedActionsInlineMarkup(advertiserId: UUID): InlineKeyboardMarkup {
	return InlineKeyboardMarkup.create(
		buttons = listOf(
			listOf(
				InlineKeyboardButton.CallbackData(
					text = "Ежедневная статистика",
					callbackData = "advertiser:stat_daily_0:${advertiserId}",
				),
			),
			listOf(
				InlineKeyboardButton.CallbackData(
					text = "Назад",
					callbackData = "advertiser:view_${advertiserId}",
				), goToMenuInlineMarkup
			),
		)
	)
}
