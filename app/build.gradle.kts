
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    id("jacoco")
}

android {
    namespace = "com.bhtravel.app"

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.bhtravel.app"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
        }

        release {
            optimization {
                enable = true
                packageScope = setOf(
                    "androidx.**",
                    "kotlin.**",
                    "kotlinx.**"
                )
            }
            testOptions {
                unitTests {
                    isIncludeAndroidResources = true
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.glide)

    testImplementation(libs.junit)

    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.test.core)

    testImplementation(libs.junit)

    // Robolectric - Android Activity unit tests
    testImplementation("org.robolectric:robolectric:4.17")
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*"
    )

    val debugTree = fileTree(
        layout.buildDirectory.dir("intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes")
    ) {
        exclude(fileFilter)
    }

    classDirectories.setFrom(debugTree)

    sourceDirectories.setFrom(
        files(
            "src/main/java",
            "src/main/kotlin"
        )
    )

    executionData.setFrom(
        fileTree(layout.buildDirectory) {
            include(
                "**/*.exec",
                "**/*.ec"
            )
        }
    )
}
