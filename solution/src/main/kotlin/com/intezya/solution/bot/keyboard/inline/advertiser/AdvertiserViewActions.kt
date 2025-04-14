package com.intezya.solution.bot.keyboard.inline.advertiser

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.intezya.solution.bot.keyboard.inline.goToMenuInlineMarkup
import java.util.*

fun advertiserViewActionsInlineMarkup(id: UUID): InlineKeyboardMarkup {
	return InlineKeyboardMarkup.create(
		buttons = listOf(
			listOf(
				InlineKeyboardButton.CallbackData(
					text = "Создать объявление вручную",
					callbackData = "advertiser:create_ad_m:${id}",
				),
				InlineKeyboardButton.CallbackData(
					text = "Создать объявление автоматически",
					callbackData = "advertiser:create_ad_a:${id}",
				),

				), listOf(
				InlineKeyboardButton.CallbackData(
					text = "Посмотреть объявления",
					callbackData = "ads:view_p0:${id}",
				),
				InlineKeyboardButton.CallbackData(
					text = "Установить ML Score",
					callbackData = "advertiser:set_ml_score:${id}",
				),
			), listOf(
				InlineKeyboardButton.CallbackData(
					text = "Статистика",
					callbackData = "advertiser:statistic:${id}",
				),
				InlineKeyboardButton.CallbackData(
					text = "Ежедневная статистика",
					callbackData = "advertiser:stat_daily_0:${id}",
				),
			), listOf(
				InlineKeyboardButton.CallbackData(
					text = "Назад",
					callbackData = "menu:advertisers",
				), goToMenuInlineMarkup
			)
		)
	)
}
