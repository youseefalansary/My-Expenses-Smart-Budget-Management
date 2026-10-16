# مصروفي | My Expenses Smart Budget Management

تطبيق Android عربي لإدارة المصروفات والميزانية.

## أحدث إصدار

- الإصدار: **1.2** (versionCode **3**)
- اسم التطبيق: **مصروفي**
- يتضمن الإصلاح: ثبات المسموح اليومي على ميزانية بداية اليوم، مع ترحيل فائض نهاية اليوم دون تكرار احتسابه.
- [تنزيل APK الإصدار 1.2](release/Masrofi-v1.2-debug.apk)
- [تنزيل ملفات مصدر الإصدار 1.2](release/Masrofi-v1.2-source.zip)

الإصدار السابق محفوظ في `release/Masrofi-v1.1-debug.apk` والنسخ الأقدم باقية في مجلد `release`.

## البناء والاختبارات

يتطلب المشروع Android SDK 36 وJDK 21. شغّل:

```bash
./gradlew assembleDebug testDebugUnitTest
```

لا ترفع مفاتيح API أو ملفات التوقيع الخاصة أو ملف `debug.keystore` إلى المستودع.
