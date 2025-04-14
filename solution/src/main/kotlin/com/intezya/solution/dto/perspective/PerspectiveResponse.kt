package com.intezya.solution.dto.perspective

data class PerspectiveResponse(
	val attributeScores: Map<String, AttributeScore>,
)

data class AttributeScore(
	val summaryScore: SummaryScore,
)

data class SummaryScore(
	val value: Float,
	val type: String,
)
