package com.intezya.solution

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.text
import com.intezya.solution.bot.handler.callback.*
import org.springframework.stereotype.Service
import java.util.*

@Service
class TelegramBot(
	private val botToken: String,
	private val clientHandler: ClientHandler,
	private val advertiserHandler: AdvertiserHandler,
	private val advertisementHandler: AdvertisementHandler,
	private val mainMenuHandler: MainMenuHandler,
	private val mlScoreHandler: MLScoreHandler
) {
	private val dateStates = mutableMapOf<Long, SetDateState>()
	private val mlScoreClientStates = mutableMapOf<Long, SetMLScoreClientState>()
	private val mlScoreAdvertisersStates = mutableMapOf<Long, SetMLScoreAdvertiserState>()

	enum class SetDateState {
		AWAITING_DATE,
	}

	enum class SetMLScoreClientState {
		AWAITING_SCORE,
	}

	enum class SetMLScoreAdvertiserState {
		AWAITING_SCORE,
	}

	fun startBot() {
		val bot = bot {
			token = botToken

			dispatch {
				text {
					println(mlScoreClientStates)
					message.replyToMessage?.let { replyMessage ->
						val userId = message.from?.id ?: return@let
						message.text ?: return@let

						dateStates[userId]?.let { state ->
							if (state == SetDateState.AWAITING_DATE) {
								mainMenuHandler.handleSetDateCommand(message, bot)
								dateStates.remove(userId)
							}
						}

						mlScoreClientStates[userId]?.let { state ->
							if (state == SetMLScoreClientState.AWAITING_SCORE) {
								try {
									val clientIdString = replyMessage.text!!.substring(4, 40).trim()
									val clientId = UUID.fromString(clientIdString)

									mlScoreHandler.handleSetMLScoreForClientState(message, bot, clientId)
									mlScoreClientStates.remove(userId)
									return@text
								} catch (e: Exception) {
									mlScoreClientStates.remove(userId)
									return@text
								}
							}
						}

						mlScoreAdvertisersStates[userId]?.let { state ->
							if (state == SetMLScoreAdvertiserState.AWAITING_SCORE) {
								try {
									val advertiserIdString = replyMessage.text!!.substring(4, 40).trim()
									val advertiserId = UUID.fromString(advertiserIdString)

									mlScoreHandler.handleSetMLScoreForAdvertiserState(message, bot, advertiserId)
									mlScoreClientStates.remove(userId)
									return@text
								} catch (e: Exception) {
									mlScoreClientStates.remove(userId)
									return@text
								}
							}
						}
					}
				}
				command("start") {
					mainMenuHandler.handleStartCommand(message, bot)
				}
				callbackQuery("start") {
					mainMenuHandler.handleStartCallback(callbackQuery, bot)
				}

				// CLIENTS
				callbackQuery("menu:clients") {
					mainMenuHandler.handleClientsMenuCallback(callbackQuery, bot)
				}

				callbackQuery("client:create_manual") {                    // TODO: Implement manual client creation
				}

				callbackQuery("client:create_auto") {
					clientHandler.handleAutoCreate(callbackQuery, bot)
				}

				callbackQuery("client:set_ml_score") {                    // TODO: Implement ML score setting
				}

				// ADVERTISERS
				callbackQuery("menu:advertisers") {
					mainMenuHandler.handleAdvertisersMenuCallback(callbackQuery, bot)
				}

				callbackQuery("advertiser:create_manual") {                    // TODO: Implement manual advertiser creation
				}

				callbackQuery("advertiser:create_auto") {
					advertiserHandler.handleAutoCreate(callbackQuery, bot)
				}

				// UTILS
				callbackQuery("menu:system_time") {
					mainMenuHandler.handleSystemTimeMenuCallback(callbackQuery, bot)
				}

				callbackQuery("system_time:change") {
					dateStates[callbackQuery.from.id] = SetDateState.AWAITING_DATE
					mainMenuHandler.handleChangeSystemTimeCallback(callbackQuery, bot)
				}

				callbackQuery("menu:moderation_state") {
					mainMenuHandler.handleModerationStateMenuCallback(callbackQuery, bot)
				}

				callbackQuery("moderation_state:change") {
					mainMenuHandler.handleChangeModerationStateCallback(callbackQuery, bot)
				}

				// Dynamic callbacks handling
				callbackQuery {
					val data = callbackQuery.data
					when {                        // CLIENTS
						data.startsWith("client:view_ads:") -> {
							val clientId = UUID.fromString(data.split(":")[2])
							advertisementHandler.handleClientView(callbackQuery, bot, clientId)
						}

						data.startsWith("client:view_p") -> {
							val page = data.split("_")[1].substring(1).toInt()
							clientHandler.handleViewAll(callbackQuery, bot, page)
						}

						data.startsWith("client:view_") -> {
							val clientId = UUID.fromString(data.split("_")[1])
							clientHandler.viewClient(callbackQuery, bot, clientId)
						}

						data.startsWith("mlsCL:view_") -> {
							mlScoreHandler.setMLScoreForClientWithAdvertiser(callbackQuery, bot)
						}

						data.startsWith("advertiser:set_ml_score:") -> {
							val advertiserId = UUID.fromString(data.split(":")[2])
							mlScoreHandler.handleSetMLScoreForAdvertiser(callbackQuery, bot, advertiserId)
							mlScoreAdvertisersStates[callbackQuery.from.id] = SetMLScoreAdvertiserState.AWAITING_SCORE
							println(mlScoreClientStates)
						}

						data.startsWith("mlsAD:view_") -> {
							mlScoreHandler.setMLScoreForAdvertiserWithClient(callbackQuery, bot)
						}

						data.startsWith("client:set_ml_score:") -> {
							val clientId = UUID.fromString(data.split(":")[2])
							mlScoreHandler.handleSetMLScoreForClient(callbackQuery, bot, clientId)
							mlScoreClientStates[callbackQuery.from.id] = SetMLScoreClientState.AWAITING_SCORE
							println(mlScoreClientStates)
						}

						// ADVERTISERS
						data.startsWith("advertiser:statistic") -> {
							val advertiserId = UUID.fromString(data.split(":")[2])
							advertiserHandler.handleStatistic(callbackQuery, bot, advertiserId)
						}

						data.startsWith("advertiser:stat_daily_0") -> {
							val advertiserId = UUID.fromString(data.split(":")[2])
							val page = data.split(":")[1].split("_")[2].toInt()
							advertiserHandler.handleDailyStatistic(callbackQuery, bot, advertiserId, page)
						}

						data.startsWith("adv:st_") -> {
							val advertiserId = UUID.fromString(data.split(":")[2].split("_")[1])
							val page = data.split(":")[1].split("_")[1].toInt()
							val dateToView = data.split(":")[3].toInt()
							advertiserHandler.handleDailyStatistic(callbackQuery, bot, advertiserId, page, dateToView)
						}

						data.startsWith("advertiser:view_p") -> {
							val page = data.split("_")[1].substring(1).toInt()
							advertiserHandler.handleViewAll(callbackQuery, bot, page)
						}

						data.startsWith("advertiser:view_") -> {
							val clientId = UUID.fromString(data.split("_")[1])
							advertiserHandler.viewAdvertiser(callbackQuery, bot, clientId)
						}

						// ADVERTISEMENTS
						data.startsWith("advertiser:create_ad_m") -> {                            // TODO: Implement manual ad creation
						}

						data.startsWith("advertiser:create_ad_a") -> {
							val clientId = UUID.fromString(data.split(":")[2])
							advertisementHandler.handleAutoCreate(callbackQuery, bot, clientId)
						}

						data.startsWith("ads:view_p") -> {
							val page = data.split(":")[1].split("_")[1].substring(1).toInt()
							val advertiserId = UUID.fromString(data.split(":")[2])
							advertisementHandler.handleViewAll(callbackQuery, bot, page, advertiserId)
						}

						data.startsWith("ads:view_") -> {
							val advertisementId = UUID.fromString(data.split("_")[1])
							advertisementHandler.handleView(callbackQuery, bot, advertisementId)
						}

						data.startsWith("ad:click_clid=:") -> {
							val clientId = UUID.fromString(data.split(":")[2])
							advertisementHandler.handleClientClick(callbackQuery, bot, clientId)
						}
					}
				}
			}
		}
		bot.startPolling()
	}
}
