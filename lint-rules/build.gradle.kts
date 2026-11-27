/**
 * Reglas Detekt propias de Kensho (`Kensho*`, ruleSetId `kensho`).
 *
 * Automatizan las fitness functions de `docs/architecture/01-governance.md §7` (FF-01…FF-13) con las
 * reglas de `03-standards.md`. Catálogo y proceso para añadir reglas: `docs/architecture/08-lint-rules.md §4`.
 *
 * Detekt 1.23 trae su propio compilador (Kotlin 2.0.21): se compila con `apiVersion` 2.0 para que el jar
 * corra sobre ese runtime aunque el proyecto use Kotlin 2.2.
 */
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    // Sin versión: AGP 9 ya pone el plugin de Kotlin (2.2) en el classpath del build.
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        apiVersion = KotlinVersion.KOTLIN_2_0
        languageVersion = KotlinVersion.KOTLIN_2_0
    }
}

dependencies {
    compileOnly(libs.detekt.api)

    testImplementation(libs.detekt.api)
    testImplementation(libs.detekt.test)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.assertj.core)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
