# مصروفاتي | My Expenses Smart Budget Management

مشروع Android لتطبيق إدارة المصروفات. الاسم الظاهر للتطبيق على الجهاز: **مصروفاتي**.

## تنزيل التطبيق

حمّل نسخة التثبيت التجريبية من [Masrofaty-debug.apk](release/Masrofaty-debug.apk).

## بناء المشروع

افتح هذا المستودع في Android Studio، ثم اختر **Build > Build APK(s)** أو نفّذ:

```bash
./gradlew assembleDebug
```

المتطلبات: Android SDK 36 وJDK 21. لا تضع مفاتيح API أو ملفات التوقيع الخاصة في المستودع. استخدم `.env.example` كمرجع لإعداد مفاتيح التطوير محليًا عند الحاجة.
