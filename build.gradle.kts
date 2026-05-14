plugins {
	java
	id("org.springframework.boot") version "3.5.14"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("com.h2database:h2")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	// Cucumber
	testImplementation("io.cucumber:cucumber-java:7.18.1")
	testImplementation("io.cucumber:cucumber-spring:7.18.1")
	testImplementation("io.cucumber:cucumber-junit-platform-engine:7.18.1")
	testImplementation("org.junit.platform:junit-platform-suite")
}

tasks.withType<Test> {
	useJUnitPlatform()
}