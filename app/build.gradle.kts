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
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-release-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD") ?: "studyshieldpass"
      keyAlias = System.getenv("KEY_ALIAS") ?: "studyshield"
      keyPassword = System.getenv("KEY_PASSWORD") ?: "studyshieldpass"
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

tasks.register("generateReleaseKeystore") {
  val keystoreFile = file("${rootDir}/my-release-key.jks")
  outputs.file(keystoreFile)
  doLast {
    if (!keystoreFile.exists()) {
      try {
        println("Generating release keystore via keytool...")
        val process = ProcessBuilder(
          "keytool", "-genkeypair",
          "-v",
          "-keystore", keystoreFile.absolutePath,
          "-alias", "studyshield",
          "-keyalg", "RSA",
          "-keysize", "2048",
          "-validity", "10000",
          "-storepass", "studyshieldpass",
          "-keypass", "studyshieldpass",
          "-dname", "CN=StudyShield, O=StudyShield, C=IN"
        ).start()
        val exitCode = process.waitFor()
        if (exitCode == 0) {
          println("Keystore created at: ${keystoreFile.absolutePath}")
        } else {
          throw GradleException("Keytool failed with exit code $exitCode")
        }
      } catch (e: Exception) {
        e.printStackTrace()
        throw GradleException("Failed to run keytool: ${e.message}", e)
      }
    }
  }
}

// Automatically trigger release and bundle compilation when building, and copy outputs to release-files
tasks.configureEach {
  if (name.contains("Release") && name != "generateReleaseKeystore") {
    dependsOn("generateReleaseKeystore")
  }
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

