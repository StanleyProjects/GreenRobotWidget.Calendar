buildscript {
    repositories {
        mavenCentral()
        google()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:4.0.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.72")
    }
}

task<Delete>("clean") {
    delete = setOf(rootProject.buildDir)
}
