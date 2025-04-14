package com.intezya.solution.bot.utils

import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton

private const val INVALID_CALLBACK_DATA = "invalid"

fun <T> paginateItems(
	items: List<T>,
	page: Int,
	prefix: String,
	itemsPerPage: Int = 3,
	backData: String,
	getId: (T) -> String,
	getName: (T) -> String,
): InlineKeyboardMarkup {
	require(page >= 0) { "Page number must be non-negative" }
	require(itemsPerPage > 0) { "Items per page must be positive" }

	val totalPages = (items.size + itemsPerPage - 1) / itemsPerPage
	val safePage = page.coerceIn(0, maxOf(0, totalPages - 1))
	val start = safePage * itemsPerPage
	val end = minOf(start + itemsPerPage, items.size)

	val itemButtons = items.subList(start, end).map { item ->
		InlineKeyboardButton.CallbackData(
			text = getName(item),
			callbackData = buildCallbackData(prefix, "view", getId(item)),
		)
	}

	val navigationButtons = buildNavigationButtons(
		prefix = prefix,
		currentPage = safePage,
		totalPages = totalPages,
		backData = backData,
	)

	return InlineKeyboardMarkup.create(
		buttons = listOf(itemButtons, navigationButtons)
	)
}

private fun buildCallbackData(prefix: String, action: String, id: String): String = "$prefix:${action}_$id"

private fun buildNavigationButtons(
	prefix: String,
	currentPage: Int,
	totalPages: Int,
	backData: String,
): List<InlineKeyboardButton> = buildList {
	add(
		if (currentPage > 0) {
			InlineKeyboardButton.CallbackData(
				text = "←", callbackData = "$prefix:view_p${currentPage - 1}"
			)
		} else {
			InlineKeyboardButton.CallbackData(
				text = "•", callbackData = INVALID_CALLBACK_DATA
			)
		}
	)

	add(
		InlineKeyboardButton.CallbackData(
			text = "Назад", callbackData = backData
		)
	)

	add(
		if (currentPage < totalPages - 1) {
			InlineKeyboardButton.CallbackData(
				text = "→", callbackData = "$prefix:view_p${currentPage + 1}"
			)
		} else {
			InlineKeyboardButton.CallbackData(
				text = "•", callbackData = INVALID_CALLBACK_DATA
			)
		}
	)
}
