plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // Plugin Firebase
    id("com.google.firebase.crashlytics")
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
    implementation(platform(libs.firebase.bom))
    // Firebase dependencies (không cần chỉ định phiên bản vì đã có BOM)
    implementation(libs.firebase.firestore) // Firestore
    implementation(libs.firebase.auth.ktx)
    implementation(libs.google.firebase.database)
    implementation(libs.google.firebase.auth)
    implementation (libs.google.firebase.dynamic.links)
    implementation (libs.firebase.analytics)
    implementation(libs.firebase.appcheck)
    implementation(libs.integrity)
    implementation(libs.core)
    implementation(libs.google.firebase.crashlytics)
    debugImplementation ("com.google.firebase:firebase-appcheck-debug:17.1.2") // Hoặc phiên bản mới nhất
    implementation(libs.firebase.crashlytics)

    // Thêm Glide để tải hình ảnh
    implementation(libs.github.glide)
    implementation(libs.play.services.safetynet)
    implementation(libs.firebase.appcheck.playintegrity)
    annotationProcessor(libs.compiler)

    //Dependency tải voucher
    implementation(libs.lifecycle.livedata)
    implementation (libs.viewpager2)
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
    implementation(libs.play.services.maps)
    implementation(libs.mongodb.driver.sync)
    implementation (libs.play.services.auth)


}