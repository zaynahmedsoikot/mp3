plugins {

    id ("com.android.application")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")



    id("org.jetbrains.kotlin.android") version "2.0.21" apply true
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"

}



android {

    namespace = "com.example.laapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.laapp"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release  {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "2.0.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }


}

dependencies {

    implementation("com.google.dagger:hilt-android:2.51.1")
    implementation(libs.androidx.palette.ktx)
    kapt("com.google.dagger:hilt-android-compiler:2.51.1")


    implementation (libs.coil)
    implementation ("androidx.core:core-ktx:1.15.0")
    implementation ("androidx.media:media:1.7.0")

    implementation (libs.androidx.media3.session.v103)

    implementation ("androidx.media3:media3-session:1.5.1")
    implementation ("androidx.media3:media3-exoplayer:1.5.1")


    implementation (libs.exoplayer)

    implementation (libs.androidx.core.ktx)




    implementation(libs.androidx.media3.ui.v120)
    implementation(libs.androidx.media3.exoplayer.v120)
    implementation(libs.androidx.media3.session)







    implementation(libs.androidx.animation) // Latest stable version as of Oct 2023
    implementation(libs.androidx.ui.v175)              // Compose UI
    implementation(libs.androidx.foundation) // Foundation for other utilities


    implementation (libs.coil.compose)


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.ui.test.android)
    implementation(libs.androidx.espresso.core)
    implementation(libs.media)
    implementation(libs.androidx.media3.ui)
    implementation(libs.media)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation (libs.ui)
    implementation(libs.exoplayer)
    implementation (libs.androidx.material)
    implementation (libs.ui.tooling.preview)
    implementation (libs.androidx.lifecycle.runtime.ktx.v285)
    implementation (libs.androidx.activity.compose.v192)

    implementation (libs.androidx.media3.exoplayer)
    implementation (libs.androidx.media3.ui)



    // Jetpack Compose dependencies
    implementation(libs.androidx.activity.compose.v172)
    implementation(libs.androidx.ui.v150)
    implementation(libs.androidx.material.v150)
    implementation(libs.androidx.ui.tooling.preview.v150)
    implementation(libs.androidx.lifecycle.runtime.ktx.v261)

    // For permissions handling
    implementation(libs.androidx.activity.ktx)

    // Optional: For Coil image loading (if needed for your project)
    implementation(libs.coil.compose)

    // Test dependencies
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    implementation(libs.accompanist.permissions)
}