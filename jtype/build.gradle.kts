plugins {
  `java-library`
}

dependencies {
  api(libs.jspecify)

  testImplementation(platform(libs.junit.bom))
  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.launcher)
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = sourceCompatibility
}
