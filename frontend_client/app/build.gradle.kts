plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Plugin Firebase
}

android {
    namespace = "saru.com.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "saru.com.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
        }
        getByName("release") {
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // Sử dụng BOM để quản lý phiên bản Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))

    // Firebase dependencies (không cần chỉ định phiên bản vì đã có BOM)
    implementation("com.google.firebase:firebase-firestore") // Firestore
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-dynamic-links")
    implementation ("com.google.firebase:firebase-analytics:22.1.0")
    implementation("com.google.firebase:firebase-appcheck")
    implementation("com.google.android.play:integrity:1.4.0")
    debugImplementation ("com.google.firebase:firebase-appcheck-debug:17.1.2") // Hoặc phiên bản mới nhất

    // Thêm Glide để tải hình ảnh
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.play.services.safetynet)
    implementation(libs.firebase.appcheck.playintegrity)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Các dependency khác
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(libs.camera.view)
    implementation(libs.volley)
    implementation(libs.security.crypto)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.roundedimageview)
    implementation(libs.play.services.maps.v1810)
    implementation(libs.play.services.maps.v1820)
    implementation(libs.mongodb.driver.sync)

}