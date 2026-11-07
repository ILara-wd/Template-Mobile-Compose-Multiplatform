import org.jetbrains.kotlin.gradle.dsl.JvmTarget

// Modelos de dominio en Kotlin puro (sin Android ni Compose).
plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}
