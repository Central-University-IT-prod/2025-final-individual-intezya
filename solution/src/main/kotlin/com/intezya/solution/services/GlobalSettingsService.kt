package com.intezya.solution.services

import com.intezya.solution.entity.GlobalSettingsEntry
import com.intezya.solution.repository.GlobalSettingsRepository
import org.springframework.stereotype.Service

@Service
class GlobalSettingsService(
	private val globalSettingsRepository: GlobalSettingsRepository,
) {
	private var cachedCurrentDate: Int? = null
	private var cachedModerationEnabled: Boolean? = null

	fun setCurrentDate(newDate: Int) {
		globalSettingsRepository.save(
			GlobalSettingsEntry(
				settingsKey = "application_date",
				settingsValue = newDate.toString(),
			)
		)

		cachedCurrentDate = newDate
	}

	fun setModerationState(moderationEnabled: Boolean) {
		globalSettingsRepository.save(
			GlobalSettingsEntry(
				settingsKey = "moderation_enabled",
				settingsValue = moderationEnabled.toString(),
			)
		)

		cachedModerationEnabled = moderationEnabled
	}

	fun getCurrentDate(): Int {
		return cachedCurrentDate ?: globalSettingsRepository.findById("application_date")
			.orElse(null).settingsValue.toInt()
	}

	fun getModerationEnabled(): Boolean {
		return cachedModerationEnabled ?: globalSettingsRepository.findById("moderation_enabled")
			.orElse(null).settingsValue.toBoolean()
	}
}
