package com.intezya.solution.utils.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException


class StrictStringDeserializer : JsonDeserializer<String>() {
	override fun deserialize(p: JsonParser, ctxt: DeserializationContext): String {
		if (p.currentToken != JsonToken.VALUE_STRING) {
			throw JsonMappingException.from(
				p, "Expected a string value, got ${p.currentToken}"
			)
		}
		return p.text
	}
}
