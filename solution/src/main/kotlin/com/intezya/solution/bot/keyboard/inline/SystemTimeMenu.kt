package com.intezya.solution.bot.keyboard.inline

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

val SystemTimeMenuInlineMarkup = InlineKeyboardMarkup.create(
	buttons = listOf(
		listOf(
			InlineKeyboardButton.CallbackData("Изменить", "system_time:change"),
		),
		listOf(
			goToMenuInlineMarkup
		),
	)
)
