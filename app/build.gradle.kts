plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.kotlin.serialization)
    id("kotlin-kapt") // Plugin para o Room
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

        // se precisar por causa de muitas dependências
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
        // manter Java 11 se você for usar libs que pedem J11 (ok se não usar google-cloud SDK)
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
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

    // --- Firebase (BOM garante compatibilidade entre as libs Firebase) ---
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.appcheck.debug)

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
    implementation(libs.androidx.lifecycle.livedata.ktx)

    // --- Multidex (se multiDexEnabled = true) ---
    implementation(libs.androidx.multidex)

    // --- OBS: removidas dependências google-cloud-vertexai e grpc-okhttp (não usar no Android) ---
    // implementation("com.google.cloud:google-cloud-vertexai:0.1.0")
    // implementation("io.grpc:grpc-okhttp:1.60.1")

    // **Importante**: NÃO forçar exclusões globais de protobuf aqui — deixe o BOM/Gradle resolver as versões compatíveis.
}
