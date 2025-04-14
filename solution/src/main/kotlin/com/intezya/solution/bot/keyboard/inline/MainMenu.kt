package com.intezya.solution.bot.keyboard.inline

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton


val MainMenuInlineMarkup = InlineKeyboardMarkup.create(
	buttons = listOf(
		listOf(
			InlineKeyboardButton.CallbackData("Клиенты", "menu:clients"),
			InlineKeyboardButton.CallbackData("Рекламодатели", "menu:advertisers")
		),
		listOf(
			InlineKeyboardButton.CallbackData("Системное время", "menu:system_time"),
			InlineKeyboardButton.CallbackData("Состояние модерации", "menu:moderation_state")
		),
	)
)
