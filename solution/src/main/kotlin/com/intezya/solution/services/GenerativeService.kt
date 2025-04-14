package com.intezya.solution.services

import io.github.vyfor.groqkt.GroqClient
import org.springframework.stereotype.Service

private const val COMPLETION_SYSTEM_TEXT = """
Вы - ассистент, который пишет увлекательные и профессиональные описания для рекламных объявлений.
Ваша задача - создать интересное и завлекающее описание на основе заголовка объявления."""
private const val COMPLETION_USER_TEXT = """
Сгенерируйте увлекательное описание рекламной кампании НА РУССКОМ ЯЗЫКЕ для бизнеса на основе заголовка: """

@Service
class GenerativeService(
	private val groqClient: GroqClient,
) {
	suspend fun advertisementDescription(info: String): String? {
		var response = groqClient.chat {
			messages {
				system(COMPLETION_SYSTEM_TEXT)
				text(COMPLETION_USER_TEXT + info)
			}
		}
		if (response.isFailure) {
			response = groqClient.chat {
				messages {
					system(COMPLETION_SYSTEM_TEXT)
					text(COMPLETION_USER_TEXT + info)
				}
			}
		}
		val res = response.getOrNull() ?: return null
		println(res.data.choices[0].message.content)
		return res.data.choices[0].message.content?.let { cleanText(it) }
	}

	private fun cleanText(input: String): String {
		return input.removeSurrounding("\"").replace("\\\"", "\"").replace(Regex("""\s+"""), " ").trim()
	}
}
