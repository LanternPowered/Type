import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version libs.versions.kotlin
}

dependencies {
  api(libs.kotlin.reflect)

  testImplementation(libs.jspecify)
  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.launcher)
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = sourceCompatibility
}

tasks.compileTestJava {
  options.release.set(25)
}

kotlin {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_11)
    freeCompilerArgs.set(listOf("-opt-in=kotlin.ExperimentalStdlibApi"))
  }
}

tasks.named<KotlinCompile>("compileTestKotlin") {
  compilerOptions {
    jvmTarget.set(JvmTarget.JVM_25)
  }
}
