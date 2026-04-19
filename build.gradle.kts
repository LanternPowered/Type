plugins {
  alias(libs.plugins.spotless)
}

allprojects {
  apply(plugin = "com.diffplug.spotless")

  group = "org.lanternpowered"
  version = "1.0.0-SNAPSHOT"

  repositories {
    mavenCentral()
  }

  tasks.withType<Test>().configureEach {
    useJUnitPlatform()
  }

  spotless {
    java {
      target("**/src/**/*.java")
      licenseHeaderFile(rootProject.file("HEADER.txt"))
    }
    kotlin {
      target("**/src/**/*.kt")
      licenseHeaderFile(rootProject.file("HEADER.txt"), "(package|import|@file)")
    }
  }
}
