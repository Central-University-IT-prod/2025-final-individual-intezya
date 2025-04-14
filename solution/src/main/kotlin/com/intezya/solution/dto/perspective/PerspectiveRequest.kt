package com.intezya.solution.dto.perspective

data class PerspectiveRequest(
	val comment: Comment,
	val requestedAttributes: Map<String, Any>,
)

data class Comment(
	val text: String,
)
