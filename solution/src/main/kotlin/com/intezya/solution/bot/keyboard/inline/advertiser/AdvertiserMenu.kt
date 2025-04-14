package com.intezya.solution.bot.keyboard.inline.advertiser

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.intezya.solution.bot.keyboard.inline.goToMenuInlineMarkup

val AdvertiserMenuInlineMarkup = InlineKeyboardMarkup.create(
	buttons = listOf(
		listOf(
			InlineKeyboardButton.CallbackData(
				text = "Создать вручную",
				callbackData = "advertiser:create_manual",
			), InlineKeyboardButton.CallbackData(
				text = "Создать автоматически",
				callbackData = "advertiser:create_auto",
			)
		), listOf(
			InlineKeyboardButton.CallbackData(
				text = "Просмотр всех рекламодателей",
				callbackData = "advertiser:view_p0",
			),
		), listOf(
			goToMenuInlineMarkup
		)
	)
)
