# مصروفي | My Expenses Smart Budget Management

تطبيق Android عربي لإدارة المصروفات والميزانية.

## أحدث إصدار

- الإصدار: **1.1** (versionCode **2**)
- اسم التطبيق: **مصروفي**
- [تنزيل APK الإصدار 1.1](release/Masrofi-v1.1-debug.apk)
- [تنزيل ملفات مصدر الإصدار 1.1](release/Masrofi-v1.1-source.zip)

النسخة السابقة محفوظة في `release/Masrofaty-debug.apk`.

## البناء والاختبارات

يتطلب المشروع Android SDK 36 وJDK 21. شغّل:

```bash
./gradlew assembleDebug testDebugUnitTest
```

لا ترفع مفاتيح API أو ملفات التوقيع الخاصة أو ملف `debug.keystore` إلى المستودع.
