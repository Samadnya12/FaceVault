plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.apptest.ml1"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
        aaptOptions {
            noCompress("tflite")
        }

        // If you are using ML Model Binding to import models via UI:
        buildFeatures {
            mlModelBinding = true
        }
        packaging {
            jniLibs {
                useLegacyPackaging = false
            }
        }
    }

    defaultConfig {
        applicationId = "com.apptest.ml1"
        minSdk = 24
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("com.google.mlkit:face-detection:16.1.7")

//    implementation("com.google.android.gms:play-services-mlkit-face-detection:17.1.0")

    implementation ("androidx.camera:camera-core:1.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-view:1.3.0")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

//    implementation("org.tensorflow:tensorflow-lite:2.14.0")
//    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")
//    implementation("org.tensorflow:tensorflow-lite-gpu:2.14.0")
//
//    // Use the Play Services version to avoid namespace conflicts
//    implementation("com.google.android.gms:play-services-tflite-java:16.0.1")
//    implementation("com.google.android.gms:play-services-tflite-gpu:16.2.0")
//
//    // Support library is usually fine, but keep it at a compatible version
//    implementation("org.tensorflow:tensorflow-lite-support:0.4.4")

    implementation("org.tensorflow:tensorflow-lite:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-gpu:2.16.1")
    implementation("org.tensorflow:tensorflow-lite-api:2.16.1")



}