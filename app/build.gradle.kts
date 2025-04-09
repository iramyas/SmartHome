plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.example.smarthome"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.smarthome"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    // Core Android & Compose Dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    //implementation("com.google.firebase:firebase-database")
    implementation("androidx.compose.material:material-icons-extended")

//    implementation("androidx.compose.material:material-icons-extended:<version>")

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Firebase Dependencies

    // UPDATE THE VERSION HERE:
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))

    //implementation("androidx.compose.material:material-icons-extended:1.6.7")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")
//    implementation("com.google.firebase:firebase-firestore:24.10.2") {
//        exclude(group = "com.google.firebase", module = "firebase-common")
//    } // Consider using ktx version
    //implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
//    implementation("com.firebaseui:firebase-ui-auth:8.0.2")  Keep this if you are using Firebase UI
    //implementation("com.google.firebase:firebase-database-ktx") // Keep this if you need Realtime Database
//    implementation("com.google.firebase:firebase-common:24.0.0") {
//        version {
//            strictly("24.0.0")
//        }
//    }
    // Google Sign-In
    implementation(libs.googleid)
    implementation(libs.firebase.crashlytics)

    // Testing Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}