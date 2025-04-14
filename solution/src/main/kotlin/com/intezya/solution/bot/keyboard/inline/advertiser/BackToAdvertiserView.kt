package com.intezya.solution.bot.keyboard.inline.advertiser

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.intezya.solution.bot.keyboard.inline.goToMenuInlineMarkup
import java.util.*

fun backToAdvertiserViewInlineMarkup(advertiserId: UUID): InlineKeyboardMarkup {
	return InlineKeyboardMarkup.create(
		buttons = listOf(
			listOf(
				InlineKeyboardButton.CallbackData("Назад", "advertiser:view_${advertiserId}")
			), listOf(
				goToMenuInlineMarkup
			)
		)
	)
}
