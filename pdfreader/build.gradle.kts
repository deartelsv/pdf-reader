@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    id("maven-publish")
}

android {
    namespace = "de.artelsv.pdfreader"
    compileSdk = 33

    defaultConfig {
        aarMetadata {
            minCompileSdk = 21
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testFixtures {
        enable = true
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
        multipleVariants {
            allVariants()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.androidx.recycler)
    implementation(libs.androidx.appcompat)
    implementation(libs.commons.io)
    implementation(libs.okHttp)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "de.artelsv"
            artifactId = "pdf-reader"
            version = (findProperty("version") as String?) ?: "1.0.0"

            afterEvaluate {
                from(components["release"])
            }
        }

        register<MavenPublication>("debug") {
            groupId = "de.artelsv"
            artifactId = "pdf-reader"
            version = (findProperty("version") as String?) ?: "1.0.0"

            afterEvaluate {
                from(components["debug"])
            }
        }
    }
}
