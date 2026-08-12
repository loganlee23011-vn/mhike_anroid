package com.example.mhikeandroid.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Centralized date/time formatting + construction so every screen renders dates the same way. */
public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
    }

    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(date);
    }

    public static Date fromYmd(int year, int month, int dayOfMonth) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, dayOfMonth, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    public static Date withTime(Date date, int hourOfDay, int minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date != null ? date : new Date());
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
