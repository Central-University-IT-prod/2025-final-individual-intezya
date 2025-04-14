package com.intezya.solution.bot.keyboard.inline.client

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.intezya.solution.bot.keyboard.inline.goToMenuInlineMarkup

val ClientMenuInlineMarkup = InlineKeyboardMarkup.create(
	buttons = listOf(
		listOf(
			InlineKeyboardButton.CallbackData("Создать вручную", "client:create_manual"),
			InlineKeyboardButton.CallbackData("Создать автоматически", "client:create_auto")
		), listOf(
			InlineKeyboardButton.CallbackData("Просмотр всех клиентов", "client:view_p0"),
		), listOf(
			goToMenuInlineMarkup
		)
	)
)
