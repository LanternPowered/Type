plugins {
  `java-library`
}

dependencies {
  implementation(libs.jspecify)
  compileOnly(libs.checkerframework)

  testImplementation(libs.checkerframework)
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
