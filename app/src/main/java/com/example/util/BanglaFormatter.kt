package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object BanglaFormatter {

    private val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

    fun toBanglaDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(banglaDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun toEnglishDigits(input: String): String {
        val sb = StringBuilder()
        for (ch in input) {
            val idx = banglaDigits.indexOf(ch)
            if (idx != -1) {
                sb.append(idx)
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatCurrency(amount: Double, includeSymbol: Boolean = true): String {
        val formatter = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
        val formattedEng = formatter.format(amount)
        val banglaFormatted = toBanglaDigits(formattedEng)
        return if (includeSymbol) "৳ $banglaFormatted" else banglaFormatted
    }

    fun formatDateHeader(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        calendar.timeInMillis = timestamp

        return when {
            isSameDay(calendar, today) -> "আজ"
            isSameDay(calendar, yesterday) -> "গতকাল"
            else -> {
                val day = toBanglaDigits(calendar.get(Calendar.DAY_OF_MONTH).toString())
                val monthName = getBanglaMonthName(calendar.get(Calendar.MONTH))
                val year = toBanglaDigits(calendar.get(Calendar.YEAR).toString())
                "$day $monthName $year"
            }
        }
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        val timeStr = sdf.format(Date(timestamp))
        return toBanglaDigits(timeStr)
    }

    fun formatFullDateTime(timestamp: Long): String {
        return "${formatDateHeader(timestamp)}, ${formatTime(timestamp)}"
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun getBanglaMonthName(month: Int): String {
        return when (month) {
            Calendar.JANUARY -> "জানুয়ারি"
            Calendar.FEBRUARY -> "ফেব্রুয়ারি"
            Calendar.MARCH -> "মার্চ"
            Calendar.APRIL -> "এপ্রিল"
            Calendar.MAY -> "মে"
            Calendar.JUNE -> "জুন"
            Calendar.JULY -> "জুলাই"
            Calendar.AUGUST -> "আগস্ট"
            Calendar.SEPTEMBER -> "সেপ্টেম্বর"
            Calendar.OCTOBER -> "অক্টোবর"
            Calendar.NOVEMBER -> "নভেম্বর"
            Calendar.DECEMBER -> "ডিসেম্বর"
            else -> ""
        }
    }

    fun isValidBdPhone(phone: String): Boolean {
        val engPhone = toEnglishDigits(phone).replace("\\s+".toRegex(), "").replace("-", "")
        // Valid BD numbers: 11 digits starting with 01
        return engPhone.matches("^01[3-9]\\d{8}$".toRegex())
    }
}
