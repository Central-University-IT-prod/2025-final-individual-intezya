package com.intezya.solution.dto

data class ContentModerationResult(
	val originalText: String,
	val isSafe: Boolean,
	val toxicityScore: Float,
	val otherScores: Map<String, Float> = emptyMap(),
)
