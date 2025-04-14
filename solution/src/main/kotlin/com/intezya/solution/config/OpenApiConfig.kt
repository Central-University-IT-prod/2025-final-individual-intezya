package com.intezya.solution.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
	@Bean
	fun openApi(): OpenAPI {
		return OpenAPI().info(
			Info().title("Advertisement Service").description("PROD Advertisement Serviice API").version("v1.0")
		)
	}
}
