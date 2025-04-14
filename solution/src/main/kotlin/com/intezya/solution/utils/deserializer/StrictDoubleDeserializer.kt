package com.intezya.solution.utils.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException

class StrictDoubleDeserializer : JsonDeserializer<Double>() {
	override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Double {
		if (p.currentToken != JsonToken.VALUE_NUMBER_FLOAT && p.currentToken != JsonToken.VALUE_NUMBER_INT) {
			throw JsonMappingException.from(
				p, "Expected an float or int number, got ${p.currentToken}"
			)
		}
		return p.doubleValue
	}
}
