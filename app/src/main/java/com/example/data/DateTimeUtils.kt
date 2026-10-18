package com.example.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object DateTimeUtils {

    fun getCurrentMillis(): Long = System.currentTimeMillis()

    fun getTodayIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getYesterdayIso(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    fun getDayOfWeekName(millis: Long = System.currentTimeMillis(), isArabic: Boolean = true): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        val day = cal.get(Calendar.DAY_OF_WEEK)
        return if (isArabic) {
            when (day) {
                Calendar.SATURDAY -> "السبت"
                Calendar.SUNDAY -> "الأحد"
                Calendar.MONDAY -> "الإثنين"
                Calendar.TUESDAY -> "الثلاثاء"
                Calendar.WEDNESDAY -> "الأربعاء"
                Calendar.THURSDAY -> "الخميس"
                Calendar.FRIDAY -> "الجمعة"
                else -> "اليوم"
            }
        } else {
            when (day) {
                Calendar.SATURDAY -> "Saturday"
                Calendar.SUNDAY -> "Sunday"
                Calendar.MONDAY -> "Monday"
                Calendar.TUESDAY -> "Tuesday"
                Calendar.WEDNESDAY -> "Wednesday"
                Calendar.THURSDAY -> "Thursday"
                Calendar.FRIDAY -> "Friday"
                else -> "Today"
            }
        }
    }

    fun getDayOfWeekFromIso(iso: String, isArabic: Boolean = true): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(iso)
            if (date != null) {
                getDayOfWeekName(date.time, isArabic)
            } else {
                if (isArabic) "اليوم" else "Today"
            }
        } catch (e: Exception) {
            if (isArabic) "اليوم" else "Today"
        }
    }

    fun formatTime(millis: Long = System.currentTimeMillis(), isArabic: Boolean = true): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        val hour24 = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
        val amPm = if (hour24 < 12) {
            if (isArabic) "ص" else "AM"
        } else {
            if (isArabic) "م" else "PM"
        }
        return String.format(Locale.US, "%02d:%02d %s", hour12, minute, amPm)
    }

    fun formatFullDateTime(
        millis: Long = System.currentTimeMillis(),
        isoDate: String = "",
        isArabic: Boolean = true
    ): String {
        val finalMillis = if (millis > 0) millis else System.currentTimeMillis()
        val dayName = getDayOfWeekName(finalMillis, isArabic)
        val timeStr = formatTime(finalMillis, isArabic)

        val dateDisplay = if (isoDate.isNotBlank()) {
            isoDate
        } else {
            val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.US)
            sdf.format(Date(finalMillis))
        }

        return if (isArabic) {
            "$dayName $dateDisplay • $timeStr"
        } else {
            "$dayName $dateDisplay • $timeStr"
        }
    }

    fun calculateDaysBetween(startDateIso: String, endDateIso: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val start = sdf.parse(startDateIso)
            val end = sdf.parse(endDateIso)
            if (start != null && end != null) {
                val diffMillis = end.time - start.time
                val days = TimeUnit.MILLISECONDS.toDays(diffMillis).toInt()
                kotlin.math.max(1, days)
            } else {
                30
            }
        } catch (e: Exception) {
            30
        }
    }

    fun addDaysToIso(startDateIso: String, days: Int): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()
            val start = sdf.parse(startDateIso)
            if (start != null) {
                cal.time = start
            }
            cal.add(Calendar.DAY_OF_YEAR, days)
            sdf.format(cal.time)
        } catch (e: Exception) {
            getTodayIso()
        }
    }
}
