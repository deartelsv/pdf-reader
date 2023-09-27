@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "de.artelsv.pdfreader"
    compileSdk = 33

    defaultConfig {
        applicationId = "de.artelsv.pdfreader"
        minSdk = 21
        targetSdk = 33
        versionCode = 1
        version = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        getByName("debug") {
            matchingFallbacks.add("release")
        }
    }

    buildFeatures {
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.androidx.recycler)
    implementation(libs.androidx.appcompat)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "de.artelsv"
            artifactId = "pdf-reader"
            version = "1.0.0"

            afterEvaluate {
                from(components["release"])
            }
        }

//        register<MavenPublication>("debug") {
//            groupId = "de.artelsv"
//            artifactId = "pdf-reader"
//            version = "1.0.0"
//
//            afterEvaluate {
//                from(components["debug"])
//            }
//        }
    }
}
