package com.intezya.solution.controllers

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
class GlobalExceptionHandler {

	data class ApiError(
		val timestamp: String = java.time.OffsetDateTime.now().toString(),
		val status: Int,
		val error: String,
		val message: String,
		val path: String,
		val details: Map<String, List<String>>? = null,
	)

	@ExceptionHandler(MethodArgumentNotValidException::class)
	fun handleValidationExceptions(
		ex: MethodArgumentNotValidException,
		request: HttpServletRequest,
	): ResponseEntity<ApiError> {
		val errors = ex.bindingResult.fieldErrors.groupBy({ it.field }, { it.defaultMessage ?: "Invalid value" })
		println(errors)
		val apiError = ApiError(
			status = HttpStatus.BAD_REQUEST.value(),
			error = "Validation Failed",
			message = "Invalid request parameters",
			path = request.requestURI,
			details = errors
		)

		return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
	}

	@ExceptionHandler(HttpMessageNotReadableException::class)
	fun handleMessageNotReadable(
		ex: HttpMessageNotReadableException,
		request: HttpServletRequest,
	): ResponseEntity<ApiError> {
		val errors = ex.cause?.message?.let { mapOf("message" to listOf(it)) } ?: emptyMap()
		println(errors)
		val apiError = ApiError(
			status = HttpStatus.BAD_REQUEST.value(),
			error = "Bad Request",
			message = "Malformed JSON request",
			path = request.requestURI,
			details = errors  // for debug

		)

		return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
	}

	@ExceptionHandler(ConstraintViolationException::class)
	fun handleConstraintViolation(
		ex: ConstraintViolationException,
		request: HttpServletRequest,
	): ResponseEntity<ApiError> {
		val errors = ex.constraintViolations.groupBy({ it.propertyPath.last().name }, { it.message })

		val apiError = ApiError(
			status = HttpStatus.BAD_REQUEST.value(),
			error = "Validation Failed",
			message = "Constraint violations",
			path = request.requestURI,
			details = errors
		)

		return ResponseEntity(apiError, HttpStatus.BAD_REQUEST)
	}
}
