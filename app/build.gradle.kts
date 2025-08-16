plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.accizardlucban"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.accizardlucban"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.core)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    // implementation(libs.maps) // Remove Google Maps dependency
    implementation(libs.location)
    implementation(libs.viewpager2)
    implementation(libs.fragment)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // Mapbox Maps SDK for Android (stable version)
    implementation("com.mapbox.maps:android:11.13.1")

    testImplementation(libs.junit)
    androidTestImplementation(libs.extjunit)
    androidTestImplementation(libs.espresso)

    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
}
