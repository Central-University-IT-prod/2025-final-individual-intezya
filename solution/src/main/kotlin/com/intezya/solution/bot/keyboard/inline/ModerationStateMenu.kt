package com.intezya.solution.bot.keyboard.inline

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

val ModerationStateMenuInlineMarkup = InlineKeyboardMarkup.create(
	buttons = listOf(
		listOf(
			InlineKeyboardButton.CallbackData("Изменить", "moderation_state:change"),
		),
		listOf(
			goToMenuInlineMarkup
		),
	)
)
