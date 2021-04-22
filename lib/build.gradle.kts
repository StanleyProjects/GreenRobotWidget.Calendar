repositories {
    mavenCentral()
    google()
}

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdkVersion(30)
    buildToolsVersion("29.0.3")

    defaultConfig {
        minSdkVersion(16)
        targetSdkVersion(30)
    }

    sourceSets.all {
        java.srcDir("src/$name/kotlin")
    }
}

dependencies {
    implementation(
        group = "org.jetbrains.kotlin",
        name = "kotlin-stdlib",
        version = "1.3.72"
    )
}
