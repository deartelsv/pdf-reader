// Top-level build file where you can add configuration options common to all sub-projects/modules.
@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
//    id("maven-publish")
}

//publishing {
//    publications {
//        create<MavenPublication>("release") {
//            groupId = "de.artelsv.pdfreader"
//            artifactId = "pdf-reader"
//            version = "1.0.0"
//
//            from(components["java"])
//        }
//    }
//}

true // Needed to make the Suppress annotation work for the plugins block