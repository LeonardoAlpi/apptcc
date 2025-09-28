import org.gradle.kotlin.dsl.java
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-kapt")

}

android {
    namespace = "com.example.meuappfirebase"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.meuappfirebase"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
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
        viewBinding = true
        buildConfig = true // <-- ADICIONADO 1: Habilita o BuildConfig
    }

    packaging {
        pickFirst("META-INF/DEPENDENCIES")
        pickFirst("META-INF/INDEX.LIST")
    }
}

dependencies {
    // --- Dependências Essenciais para XML/Activities ---
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // --- Firebase (BOM garante compatibilidade entre as libs Firebase) ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.appcheck.debug)

    // --- IA do Google (Gemini) ---
    implementation(libs.google.ai.generativeai) // <-- ADICIONADO 2: A dependência correta

    // --- Room ---
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.adapters)
    kapt(libs.room.compiler)

    // --- Rede ---
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    // --- Imagens ---
    implementation(libs.glide)

    // --- Testes ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- Multidex (se multiDexEnabled = true) ---
    implementation(libs.androidx.multidex)

    implementation(libs.androidx.work.runtime.ktx)
}

// <-- ADICIONADO 3: Bloco para ler a API Key do arquivo local.properties
Properties().apply {
    load(FileInputStream(rootProject.file("local.properties")))
    android.defaultConfig.buildConfigField("String", "GEMINI_API_KEY", "\"${getProperty("GEMINI_API_KEY")}\"")
}