package com.intezya.solution.enums

enum class Gender(val value: String) {
	MALE("MALE"),
	FEMALE("FEMALE");

	fun translate(): String {
		return when (this) {
			MALE -> "Мужской"
			FEMALE -> "Женский"
		}
	}
}

enum class TargetingGender(val value: String) {
	MALE("MALE"),
	FEMALE("FEMALE"),
	ALL("ALL");

	fun translate(): String {
		return when (this) {
			MALE -> "Мужской"
			FEMALE -> "Женский"
			ALL -> "Любой"
		}
	}
}
