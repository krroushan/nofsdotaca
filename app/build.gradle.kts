import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    id("kotlin-parcelize")
}

/**
 * Mobile API base URL (must end with `/`).
 * Override for a physical device against local Next:
 *   API_BASE_URL=http://192.168.x.x:3000/api/mobile/
 * in RestaurantApp/local.properties (gitignored).
 */
fun loadApiBaseUrlOverride(): String? {
    val props = Properties()
    val file = rootProject.file("local.properties")
    if (!file.exists()) return null
    file.inputStream().use { props.load(it) }
    val raw = props.getProperty("API_BASE_URL")?.trim().orEmpty()
    if (raw.isEmpty()) return null
    return if (raw.endsWith("/")) raw else "$raw/"
}

val apiBaseUrlOverride: String? = loadApiBaseUrlOverride()
val debugApiBaseUrl: String =
    apiBaseUrlOverride ?: "http://192.168.1.4:3000/api/mobile/"
val releaseApiBaseUrl: String = "https://pos.prashantpizza.in/api/mobile/"

android {
    namespace = "com.prashantpizza.nofsdotaca"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.prashantpizza.nofsdotaca"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Default placeholder; buildTypes override.
        manifestPlaceholders["usesCleartextTraffic"] = "false"
    }

    buildTypes {
        debug {
            buildConfigField("String", "API_BASE_URL", "\"$debugApiBaseUrl\"")
            manifestPlaceholders["usesCleartextTraffic"] = "true"
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "API_BASE_URL", "\"$releaseApiBaseUrl\"")
            manifestPlaceholders["usesCleartextTraffic"] = "false"
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
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    
    // LocalBroadcastManager
    implementation(libs.androidx.localbroadcastmanager)
    
    // AppCompat for AlertDialog
    implementation("androidx.appcompat:appcompat:1.7.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
