plugins {
  kotlin("jvm") version libs.versions.kotlin
}

dependencies {
  implementation(libs.kotlin.reflect)

  testImplementation(libs.jspecify)
  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.launcher)
}

kotlin {
  compilerOptions {
    freeCompilerArgs.set(listOf("-opt-in=kotlin.ExperimentalStdlibApi"))
  }
}
