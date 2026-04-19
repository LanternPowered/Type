plugins {
//  id("org.cadixdev.licenser") version "0.6.1"
//  id("com.diffplug.spotless") version "6.25.0"
}

allprojects {
//  apply(plugin = "com.diffplug.spotless")

  group = "org.lanternpowered"
  version = "1.0.0-SNAPSHOT"

  repositories {
    mavenCentral()
  }

  tasks.withType<Test>().configureEach {
    useJUnitPlatform()
  }

//  spotless {
//    java {
//      licenseHeaderFile(rootProject.file("HEADER.txt"))
//      target("**/src/**/*.java")
//    }
//    kotlin {
//      licenseHeaderFile(rootProject.file("HEADER.txt"))
//      target("**/src/**/*.kt")
//    }
//  }

//  afterEvaluate {
//    apply(plugin = "org.cadixdev.licenser")
//
//    license {
//      header(rootProject.file("HEADER.txt"))
//      newLine(false)
//      ignoreFailures(false)
//
//      include("**/*.java")
//      include("**/*.kt")
//
//      properties {
//        set("url", "https://www.lanternpowered.org")
//        set("organization", "LanternPowered")
//      }
//    }
//  }
}
