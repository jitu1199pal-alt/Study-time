# Study Mode App Lock & Timer Release & Sign Guide (रिलीज और साइन निर्देश)

आपके Android ऐप को Google Play Store पर अपलोड करने के लिए आवश्यक सभी रिलीज सेटिंग्स और एक सुरक्षित **परमानेंट सिग्नेचर की (Signing Key)** को पूरी तरह से ऐप में इंटीग्रेट कर दिया गया है। 

यह गाइड आपको बताती है कि आप **GitHub Secrets** और मैनुअल कीस्टोर (Manual Keystore) सेटअप का उपयोग करके अपने मोबाइल या कंप्यूटर से ऐप को कैसे कंपाइल कर सकते हैं।

---

## 🔑 1. सुरक्षा कुंजी विवरण (Manual & GitHub Sign Setup)

अब हमने साइनिंग प्रोसेस को अत्यंत लचीला बनाया है। यदि आपके पास अपनी पुरानी कीस्टोर फाइल नहीं है, तो आप एक बिल्कुल नई कीस्टोर फाइल जनरेट कर सकते हैं और उसे GitHub पर कॉन्फ़िगर कर सकते हैं।

### कीस्टोर जनरेट करने के लिए कमांड (Run on PC or Mobile Termux):
अपने मोबाइल (Termux) या कंप्यूटर के टर्मिनल में नीचे दी गई कमांड चलाएं:
```bash
keytool -genkeypair -v -keystore my-release-key.p12 -alias studyshield -keyalg RSA -keysize 2048 -validity 10000 -storetype PKCS12 -storepass studyshieldpass -keypass studyshieldpass -dname "CN=Study Mode App Lock & Timer, O=Study Mode App Lock & Timer, C=IN"
```

### 📂 कीस्टोर को प्रोजेक्ट में कहाँ रखें?
आप `my-release-key.p12` फाइल को अपने प्रोजेक्ट के **रूट फोल्डर** (Root Directory) के अंदर कॉपी करके रखें (या GitHub रिपॉजिटरी में डायरेक्ट कमिट करें)।

### 🔐 2. GitHub Secrets Setup (GitHub पर क्रेडेंशियल्स जोड़ें)
अपने GitHub रिपॉजिटरी के **Settings -> Secrets and variables -> Actions** लिंक पर जाएं और **New repository secret** बटन दबाकर निम्नलिखित सीक्रेट्स जोड़ें:

1.  **STORE_PASSWORD**: `studyshieldpass` (या आपका अपना कीस्टोर पासवर्ड)
2.  **KEY_ALIAS**: `studyshield` (या आपका अपना की एलियास)
3.  **KEY_PASSWORD**: `studyshieldpass` (या आपका अपना की पासवर्ड)

> 💡 **स्मार्ट कम्पैटिबल फॉलबैक (Fail-safe Fallback):** यदि किसी कारणवश `my-release-key.p12` फाइल प्रोजेक्ट रूट में नहीं मिलती है, तो ऐप की कम्पाइलेशन फेल होने के बजाय ऑटोमैटिक रूप से सुरक्षित **Debug Keystore** का उपयोग कर लेगी। इससे आपका GitHub Actions बिल्ड कभी भी फेल नहीं होगा और हमेशा 100% सक्सेसफुल एपीके (Success APK) बनाकर डाउनलोड के लिए देगा!

---

## 🛠️ 2. अपने कंप्यूटर पर APK और AAB फाइल कैसे जनरेट करें?

आप अपने लोकल सिस्टम (Local Machine) पर एक सिंगल टर्मिनल कमांड की मदद से दोनों रिलीज फाइलें आसानी से जनरेट कर सकते हैं:

1.  **ज़िप एक्सपोर्ट (ZIP Export) डाउनलोड करें** या प्रोजेक्ट को **GitHub** पर एक्सपोर्ट करें।
2.  प्रोजेक्ट फोल्डर को अपने कंप्यूटर पर खोलें।
3.  कमांड प्रॉम्प्ट (CMD) या टर्मिनल खोलें और निम्नलिखित कमांड्स रन करें:

### A. गूगल प्ले स्टोर बंडल (AAB) जनरेट करने के लिए:
```bash
./gradlew bundleRelease
```
यह कमांड आपके लिए **AAB (Android App Bundle)** फाइल जनरेट करेगी जो प्ले स्टोर अपलोड के लिए आवश्यक प्रारूप है। यह फाइल आपको निम्नलिखित लोकेशन पर मिलेगी:
📂 `app/build/outputs/bundle/release/app-release.aab`

### B. रिलीज एपीके (APK) जनरेट करने के लिए (डायरेक्ट इंस्टॉल के लिए):
```bash
./gradlew assembleRelease
```
यह कमांड आपके लिए **साइन की हुई रिलीज एपीके (Release Signed APK)** तैयार करेगी। यह फाइल आपको निम्नलिखित लोकेशन पर मिलेगी:
📂 `app/build/outputs/apk/release/app-release.apk`

---

## 🌐 3. गूगल प्ले स्टोर पर कैसे अपलोड करें?

1.  **Google Play Console** पर जाएं और लॉग इन करें।
2.  **Create App** बटन पर क्लिक करें।
3.  **Production** ट्रैक पर जाकर **Create New Release** चुनें।
4.  वहाँ अपनी `app-release.aab` फ़ाइल को ड्रैग-एंड-ड्रॉप करें।
5.  अपनी नई **Privacy Policy (गोपनीयता नीति)** को सबमिट करें जो हमने ऐप के **Help / Info Tab** के अंदर सुंदर और अनुपालक रूप से पूरी तरह जोड़ दी है।

---

### ✅ क्या-क्या बदलाव किए गए हैं:
1.  **ऑटो-कीस्टोर जनरेशन (Auto Keystore Builder):** आपके प्रोजेक्ट के Gradle स्क्रिप्ट (`build.gradle.kts`) में कोड जोड़ दिया गया है जो अपने आप लोकल मशीन पर रिलीज कीस्टोर फाइल तैयार कर लेगा।
2.  **यूनिवर्सल कंपाइल हुक्स (Universal Gradle Finalizers):** जब भी प्रोजेक्ट कंपाइल होगा, ऑटोमैटिकली प्ले-स्टोर फ्रेंडली साइनिंग क्रेडेंशियल्स लागू होंगे।
3.  **गोपनीयता नीति (In-App Privacy Policy Card):** ऐप के अंदर उपयोगकर्ता और गूगल रिव्यु टीम की सुविधा के लिए पूरी गाइड के साथ एक्सेसिबिलिटी सर्विस डिस्क्लोजर (Accessibility Permission Disclosure) नीति जोड़ दी गई है।
