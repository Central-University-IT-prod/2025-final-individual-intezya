plugins {
	kotlin("jvm") version "2.1.10"
	kotlin("plugin.spring") version "2.1.10"
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.serialization") version "2.1.10"
}

group = "com.intezya"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
}

dependencies {    //	Spring Boot и Spring Framework
	implementation("org.springframework.boot:spring-boot:3.4.2")
	implementation("org.springframework.boot:spring-boot-autoconfigure:3.4.2")
	implementation("org.springframework.boot:spring-boot-starter:3.4.2")
	implementation("org.springframework.boot:spring-boot-starter-actuator:3.4.2")
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc:3.4.2")
	implementation("org.springframework.boot:spring-boot-starter-validation:3.4.2")
	implementation("org.springframework.boot:spring-boot-starter-web:3.4.2")
	implementation("org.springframework.boot:spring-boot-starter-webflux:3.4.2")
	"developmentOnly"("org.springframework.boot:spring-boot-devtools")
	implementation("org.springframework:spring-beans:6.2.2")
	implementation("org.springframework:spring-context:6.2.2")
	implementation("org.springframework:spring-core:6.2.2")
	implementation("org.springframework:spring-jdbc:6.2.2")
	implementation("org.springframework:spring-web:6.2.2")
	implementation("org.springframework:spring-webflux:6.2.2")
	implementation("org.springframework.data:spring-data-commons:3.4.2")
	implementation("org.springframework.data:spring-data-jdbc:3.4.2")
	implementation("org.springframework.data:spring-data-relational:3.4.2")

	// OpenAPI 3.0 dependencies
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.5")

	//	Jackson (JSON обработка)

	implementation("com.fasterxml.jackson.core:jackson-annotations:2.18.2")
	implementation("com.fasterxml.jackson.core:jackson-core:2.18.2")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	//	Netty и Reactor (асинхронность, WebFlux)

	implementation("io.netty:netty-resolver:4.1.117.Final")
	implementation("io.netty:netty-transport:4.1.117.Final")
	implementation("io.projectreactor:reactor-core:3.7.2")
	implementation("io.projectreactor.netty:reactor-netty:1.2.2")
	implementation("io.projectreactor.netty:reactor-netty-core:1.2.2")
	implementation("io.projectreactor.netty:reactor-netty-http:1.2.2")
	implementation("org.reactivestreams:reactive-streams:1.0.4")

	//	Kotlin и Coroutines

	implementation("org.jetbrains.kotlin:kotlin-reflect:2.1.10")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.8.1")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json")

	//	Документация (OpenAPI, Swagger)

	implementation("org.springdoc:springdoc-openapi-ui:1.8.0")

	//	База данных (PostgreSQL, HikariCP, Testcontainers)

	runtimeOnly("org.postgresql:postgresql")
	testImplementation("com.zaxxer:HikariCP:5.1.0")
	testImplementation("org.testcontainers:junit-jupiter:1.19.5")
	testImplementation("org.testcontainers:postgresql:1.19.5")
	testImplementation("org.testcontainers:testcontainers:1.19.5")

	//	Валидация

	implementation("jakarta.validation:jakarta.validation-api:3.0.2")

	//	Dotenv (работа с переменными окружения)

	implementation("io.github.cdimascio:dotenv-kotlin:6.4.0")

	//	Дополнительные библиотеки

	implementation("io.github.vyfor:groq-kt:0.1.0")
	implementation("io.ktor:ktor-client-okhttp:3.1.0")
	implementation("io.ktor:ktor-client-cio:3.1.0")
	implementation("io.ktor:ktor-client-content-negotiation:3.1.0")
	implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.0")
	implementation("org.apache.tomcat.embed:tomcat-embed-core:10.1.34")
	implementation("org.slf4j:slf4j-api:2.0.16")
	implementation("io.minio:minio:8.5.17")
	implementation("io.github.serpro69:kotlin-faker:1.16.0")

	//	Telegram Bot API

	implementation("io.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.3.0")

	//	Retrofit (HTTP клиент)

	implementation("com.squareup.retrofit2:retrofit:2.9.0")
	implementation("com.squareup.retrofit2:converter-gson:2.9.0")

	// Тестирование

	testImplementation("com.h2database:h2")
	testImplementation("io.mockk:mockk:1.13.5")
	testImplementation("io.mockk:mockk-dsl:1.13.5")
	testImplementation("io.rest-assured:json-path:5.5.0")
	testImplementation("io.rest-assured:kotlin-extensions:5.3.0")
	testImplementation("io.rest-assured:rest-assured:5.5.0")
	testImplementation("org.hamcrest:hamcrest:2.2")
	testImplementation("org.jetbrains.kotlin:kotlin-test:2.1.10")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.4")
	testImplementation("org.junit.jupiter:junit-jupiter-params:5.11.4")
	testImplementation("org.springframework.boot:spring-boot-starter-test:3.4.2")
	testImplementation("org.springframework.boot:spring-boot-test:3.4.2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

sourceSets {
	test {
		resources {
			srcDir("src/main/resources")
		}
	}
}
