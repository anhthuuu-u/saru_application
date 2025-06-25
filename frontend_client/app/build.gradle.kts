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
    implementation ("com.google.firebase:firebase-firestore-ktx:24.9.1")

    // Thêm Glide để tải hình ảnh
    implementation("com.github.bumptech.glide:glide:4.16.0")
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
    implementation (libs.github.glide)
    implementation ("com.google.android.gms:play-services-auth:19.0.0")

}