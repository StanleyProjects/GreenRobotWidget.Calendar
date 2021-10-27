repositories {
    mavenCentral()
    google()
}

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = 30
    buildToolsVersion = "30.0.3"

    defaultConfig {
        minSdk = 16
        targetSdk = 30
    }

    sourceSets.all {
        java.srcDir("src/$name/kotlin")
    }
}

dependencies {
    testImplementation("junit:junit:4.13.1")
}
