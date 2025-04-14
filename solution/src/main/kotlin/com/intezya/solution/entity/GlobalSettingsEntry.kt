package com.intezya.solution.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("global_settings")
data class GlobalSettingsEntry(
	@Id
	val settingsKey: String,
	val settingsValue: String,
)
