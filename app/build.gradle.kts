import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Lit la clé PRIM depuis local.properties (fichier non versionné) plutôt que de la coder en dur.
// Ajoute une ligne  PRIM_API_KEY=ta_cle  dans ton local.properties (voir README.md).
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(file.inputStream())
}
val primApiKey: String = localProperties.getProperty("PRIM_API_KEY") ?: ""
val updateRepository: String = localProperties.getProperty("UPDATE_REPOSITORY") ?: ""

android {
    namespace = "com.gstfkz.parismetrogame"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gstfkz.parismetrogame"
        minSdk = 26
        targetSdk = 35
        versionCode = 13
        versionName = "0.9.4b"

        // Clé API PRIM (Île-de-France Mobilités / Navitia) lue depuis local.properties
        // pour ne pas la committer en clair dans le code. Voir README.md.
        buildConfigField("String", "PRIM_API_KEY", "\"$primApiKey\"")
        buildConfigField("String", "UPDATE_REPOSITORY", "\"$updateRepository\"")
    }

    signingConfigs {
        create("release") {
            val ks = System.getenv("KEYSTORE_FILE")
            if (!ks.isNullOrBlank()) {
                storeFile = file(ks)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = System.getenv("KEY_ALIAS")
                keyPassword = System.getenv("KEY_PASSWORD")
                storeType = System.getenv("KEYSTORE_TYPE") ?: "PKCS12"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
