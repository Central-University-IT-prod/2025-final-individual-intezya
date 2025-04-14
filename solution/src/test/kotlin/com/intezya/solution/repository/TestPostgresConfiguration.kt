package com.intezya.solution.repository

import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

@TestConfiguration
class TestPostgresConfiguration {
	companion object {
		// Keep container as static field to reuse across tests
		private val postgres = PostgreSQLContainer("postgres:17").apply {
			start()
		}
	}

	@Bean
	fun dataSource(): DataSource {
		return HikariDataSource().apply {
			jdbcUrl = postgres.jdbcUrl
			username = postgres.username
			password = postgres.password
			maximumPoolSize = 5
		}
	}
}
