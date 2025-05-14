plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.pantallalogin"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.pantallalogin"
        minSdk = 24
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
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    dependencies {
        implementation platform('com.google.firebase:firebase-bom:32.2.0')    // BoM de Firebase (ejemplo de versión)
        implementation 'com.google.firebase:firebase-auth'                   // Firebase Authentication
        implementation 'com.google.firebase:firebase-database'               // Firebase Realtime Database
        implementation 'com.google.android.gms:play-services-auth:20.6.0'    // Google Sign-In (Play services)
        implementation 'com.squareup.retrofit2:retrofit:2.9.0'               // Retrofit HTTP client
        implementation 'com.squareup.retrofit2:converter-gson:2.9.0'         // Converter GSON para Retrofit
        // ... otras dependencias existentes ...
    }

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // RxJava 3 + RxAndroid para llamadas asíncronas a SQLite
    implementation("io.reactivex.rxjava3:rxjava:3.1.3")
    implementation("io.reactivex.rxjava3:rxandroid:3.0.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    plugins {
        id 'com.android.application'
        // ... otros plugins ...
        id 'com.google.gms.google-services'  // Aplicar plugin de Google Services
    }


}
