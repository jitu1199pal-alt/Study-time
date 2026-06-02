import java.io.File

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.example"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val envKeystorePath = System.getenv("KEYSTORE_PATH")
      val keystorePath = if (!envKeystorePath.isNullOrBlank()) envKeystorePath else "${rootDir}/my-release-key.p12"
      val keystoreFile = file(keystorePath)

      if (keystoreFile.exists()) {
        println("Using release keystore at: ${keystoreFile.absolutePath}")
        storeFile = keystoreFile
        storeType = if (keystoreFile.name.endsWith(".p12", ignoreCase = true) || keystoreFile.name.endsWith(".pfx", ignoreCase = true)) "PKCS12" else "JKS"

        val envStorePassword = System.getenv("STORE_PASSWORD")
        storePassword = if (!envStorePassword.isNullOrBlank()) envStorePassword else "studyshieldpass"

        val envKeyAlias = System.getenv("KEY_ALIAS")
        keyAlias = if (!envKeyAlias.isNullOrBlank()) envKeyAlias else "studyshield"

        val envKeyPassword = System.getenv("KEY_PASSWORD")
        keyPassword = if (!envKeyPassword.isNullOrBlank()) envKeyPassword else "studyshieldpass"
      } else {
        println("WARNING: Release keystore file not found at ${keystoreFile.absolutePath}. Falling back to Debug Keystore.")
        storeFile = file("${rootDir}/debug.keystore")
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  // implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}

// Automatically trigger release and bundle copy outputs to release-files
tasks.configureEach {
  if (name == "assembleDebug" || name == "assemble" || name == "build") {
    finalizedBy("copyReleaseBuildsToOutputs")
  }
}

tasks.register("copyReleaseBuildsToOutputs") {
  dependsOn("assembleRelease", "bundleRelease")
  doLast {
    val srcApk = file("${project.buildDir}/outputs/apk/release")
    val srcBundle = file("${project.buildDir}/outputs/bundle/release")
    val destDir = file("${rootDir}/release-files")
    destDir.mkdirs()
    
    var apkCopied = false
    if (srcApk.exists()) {
      srcApk.walkTopDown().forEach { file ->
        if (file.isFile && file.extension == "apk") {
          file.copyTo(File(destDir, "app-release-signed.apk"), overwrite = true)
          apkCopied = true
        }
      }
    }
    
    var aabCopied = false
    if (srcBundle.exists()) {
      srcBundle.walkTopDown().forEach { file ->
        if (file.isFile && file.extension == "aab") {
          file.copyTo(File(destDir, "app-release-signed.aab"), overwrite = true)
          aabCopied = true
        }
      }
    }
    
    // Also create a status.txt to confirm success
    val statusFile = File(destDir, "status.txt")
    statusFile.writeText("Build Completed successfully!\nAPK Copied: $apkCopied\nAAB Copied: $aabCopied\nTimestamp: ${System.currentTimeMillis()}\n")
  }
}

tasks.register("printFiles") {
  doLast {
    rootDir.walkTopDown().forEach { file ->
      if (file.isFile && (file.extension == "png" || file.extension == "webp" || file.extension == "jpg" || file.extension == "jpeg")) {
        println("FOUND_IMAGE: ${file.absolutePath}")
      }
    }
  }
}

