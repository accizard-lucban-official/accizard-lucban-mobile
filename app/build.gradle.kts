plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

// Force use of Java 17 toolchain to avoid Java 25 compatibility issues
kotlin {
    jvmToolchain(17)
}

android {
    namespace = "com.example.accizardlucban"
    compileSdk = 34
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }

    defaultConfig {
        applicationId = "com.example.accizardlucban"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        multiDexEnabled = true

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
        debug {
            isMinifyEnabled = false
        }
    }
    
    // Fix for "string too large" error - enable resource shrinking
    buildFeatures {
        buildConfig = true
    }
    
    packaging {
        resources {
            pickFirsts += "**/META-INF/NOTICE.md"
            pickFirsts += "**/META-INF/LICENSE.md"
            pickFirsts += "**/META-INF/LICENSE.txt"
            pickFirsts += "**/META-INF/NOTICE.txt"
            pickFirsts += "**/META-INF/DEPENDENCIES"
            pickFirsts += "**/META-INF/INDEX.LIST"
            pickFirsts += "**/META-INF/io.netty.versions.properties"
            pickFirsts += "**/META-INF/native-image/**"
        }
    }
    
    lint {
        abortOnError = false
        checkReleaseBuilds = false
        ignoreWarnings = true
    }

}

dependencies {
    // Multidex support
    implementation("androidx.multidex:multidex:2.0.1")
    
    // AndroidX Core Libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.core)
    implementation(libs.recyclerview)
    implementation(libs.cardview)
    implementation(libs.viewpager2)
    implementation(libs.fragment)
    implementation(libs.swiperefreshlayout)
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")

    // Location Services (using only one location service to avoid conflicts)
    implementation(libs.location)

    // Mapbox Maps SDK for Android (primary mapping solution)
    implementation(libs.mapbox)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.extjunit)
    androidTestImplementation(libs.espresso)

    // Firebase dependencies
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.storage)
    implementation("com.google.firebase:firebase-messaging")

    implementation("com.google.firebase:firebase-bom:34.5.0")
    
    // JavaMail API for email functionality
    implementation(libs.android.mail)
    implementation(libs.android.activation)

    // Chart library
    implementation(libs.mpandroidchart)
    
    // CircleImageView for circular profile pictures
    implementation("de.hdodenhof:circleimageview:3.1.0")
    
    // Weather API dependencies
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
}
