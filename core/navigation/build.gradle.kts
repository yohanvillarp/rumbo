plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "tech.nikelyh.rumbo.core.navigation"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(libs.androidx.navigation.compose)
}
