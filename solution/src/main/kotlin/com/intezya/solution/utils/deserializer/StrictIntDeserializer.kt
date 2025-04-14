package com.intezya.solution.utils.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException

class StrictIntDeserializer : JsonDeserializer<Int>() {
	override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Int {
		if (p.currentToken != JsonToken.VALUE_NUMBER_INT) {
			throw JsonMappingException.from(
				p, "Expected an integer number, got ${p.currentToken}"
			)
		}
		return p.intValue
	}
}
