# Study Mode App Lock & Timer Release & Sign Guide

All release configurations and a permanent, secure **Signing Key** have been successfully integrated into your Android application to allow direct uploading to the Google Play Store.

This guide details how to compile the application from your computer or mobile device using **GitHub Secrets** and a manual Keystore setup.

---

## 🔑 1. Security Signing Key Setup (Manual & GitHub Sign Setup)

The signing process is highly flexible. If you do not have your old Keystore file, you can easily generate a new Keystore and configure it on GitHub.

### Keystore Generation Command (Run on PC or Mobile Termux):
Run the following command in your terminal or command prompt:
```bash
keytool -genkeypair -v -keystore my-release-key.p12 -alias studyshield -keyalg RSA -keysize 2048 -validity 10000 -storetype PKCS12 -storepass studyshieldpass -keypass studyshieldpass -dname "CN=Study Mode App Lock & Timer, O=Study Mode App Lock & Timer, C=IN"
```

### 📂 Where to place the Keystore?
Copy the generated `my-release-key.p12` file directly into the **root folder** of your project (or commit it to your GitHub repository root).

### 🔐 2. GitHub Secrets Setup
Navigate to your GitHub repository -> **Settings -> Secrets and variables -> Actions**, click on **New repository secret**, and add the following keys:

1.  **STORE_PASSWORD**: `studyshieldpass` (or your custom keystore password)
2.  **KEY_ALIAS**: `studyshield` (or your custom key alias)
3.  **KEY_PASSWORD**: `studyshieldpass` (or your custom key password)

> 💡 **Smart Compatible Fallback:** If your `my-release-key.p12` file is missing from the project root, the build process will automatically fall back to a secure **Debug Keystore** instead of failing. This guarantees that your GitHub Actions builds always succeed and produce clean, downloadable APKs!

---

## 🛠️ 2. How to Generate APK and AAB on Your Computer

You can build both release formats locally with single-line terminal commands:

1.  **Download the ZIP Export** or export the project to **GitHub**.
2.  Open the project folder on your computer.
3.  Open a terminal (or active command prompt) and run:

### A. For Play Store Release Bundle (AAB):
```bash
./gradlew bundleRelease
```
This generates the **AAB (Android App Bundle)** required for Play Store uploading. It can be found at:
📂 `app/build/outputs/bundle/release/app-release.aab`

### B. For Standalone Release Installer (APK):
```bash
./gradlew assembleRelease
```
This compiles a **Signed Release APK** for direct manual installs, which can be found at:
📂 `app/build/outputs/apk/release/app-release.apk`

---

## 🌐 3. Steps to Upload to Google Play Store

1.  Log in to your **Google Play Console**.
2.  Click **Create App** and configure basic fields.
3.  Navigate to **Production** and select **Create New Release**.
4.  Drag and drop your `app-release.aab` bundle.
5.  Submit your compliant **Privacy Policy URL**. An elegant, interactive privacy policy card containing your Accessibility Service Disclosure has been added under the **Help / Info** tab in-app for play store reviews.

---

### ✅ Implemented Upgrades:
1.  **Auto Keystore Builder:** Code is integrated into `build.gradle.kts` to automate local release key building and detection.
2.  **Universal Gradle Hooks:** Automatic signing fallback prevents errors while ensuring clean Play Store compliance.
3.  **In-App Privacy Policy:** Beautiful English policy layouts featuring precise compliance disclosures for reviews and active users.
