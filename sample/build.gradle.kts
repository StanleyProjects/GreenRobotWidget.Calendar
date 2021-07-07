repositories {
    mavenCentral()
    google()
}

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdkVersion(30)
    buildToolsVersion("29.0.3")

    defaultConfig {
        minSdkVersion(16)
        targetSdkVersion(30)
        applicationId = "sp.service.sample"
        versionCode = 9
        versionName = "0.0.9"
        vectorDrawables.useSupportLibrary = true
        multiDexEnabled = true
    }

    sourceSets.all {
        java.srcDir("src/$name/kotlin")
    }

    buildTypes {
        getByName("debug") {
            applicationIdSuffix = ".$name"
            versionNameSuffix = "-$name"
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
}

dependencies {
    implementation(project(":lib"))
    implementation(
        group = "org.jetbrains.kotlin",
        name = "kotlin-stdlib",
        version = "1.5.10"
    )
}
