plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.den.shak.pq"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.den.shak.pq"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "0.1 alpha"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.play.services.maps)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.maps.mobile)
    implementation(libs.camera.camera2)
    implementation(libs.aws.android.sdk.s3)
    implementation(libs.picasso)

}