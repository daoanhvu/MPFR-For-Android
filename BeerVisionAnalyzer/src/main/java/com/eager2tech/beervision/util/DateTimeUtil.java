package com.eager2tech.beervision.util;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;

public final class DateTimeUtil {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final String[] DAYS_IN_WEEK = {"Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday", "Sunday"};

    public static String convertIntToDays(int val) {
        StringBuilder bits = new StringBuilder(Integer.toString(val, 2));
        int l = bits.length();
        if(l < 7) {
            int d = 7 - l;
            for(int i=0; i<d; i++) {
                bits.insert(0, "0");
            }
            l += d;
        }
        StringBuilder days = new StringBuilder();
        for(int i=0; i<l; i++) {
            if(bits.charAt(i) == '1') {
                days.append(DAYS_IN_WEEK[i]).append(", ");
            }
        }
        String daysStr = days.toString();
        l = daysStr.length();

        return daysStr.substring(0, l-2);
    }

    public static char[] convertWorkingDayMaskToBits(int val) {
        StringBuilder bits = new StringBuilder(Integer.toString(val, 2));
        int l = bits.length();
        if(l < 7) {
            int d = 7 - l;
            for(int i=0; i<d; i++) {
                bits.insert(0, "0");
            }
        }
        return bits.toString().toCharArray();
    }

    public static Long[] getStartEndDay(LocalDate nowDate, ZoneId zoneId) {
        LocalDate tomorrow = nowDate.plusDays(1L);
        ZonedDateTime fromTime = nowDate.atStartOfDay(zoneId);
        ZonedDateTime toTime = tomorrow.atStartOfDay(zoneId)
                .minusSeconds(1L);
        Long[] result = new Long[2];
        result[0] = fromTime.toInstant().toEpochMilli();
        result[1] = toTime.toInstant().toEpochMilli();
        return result;
    }

    /**
     *
     * @param aDate
     * @param time a string with format HH:mm:ss
     * @return
     */
    public static LocalDateTime getLocalDateTime(LocalDate aDate, String time, DateTimeFormatter timeFormatter) {
        String monthStr = aDate.getMonthValue() < 10 ? "0" + aDate.getMonthValue() : String.valueOf(aDate.getMonthValue());
        String ddStr = aDate.getDayOfMonth() < 10 ? "0" + aDate.getDayOfMonth() : String.valueOf(aDate.getDayOfMonth());
        // build String of format yyyy-MM-dd + " " + fromTime=
        final String yyyyMMdd = aDate.getYear() + "-" + monthStr + "-" + ddStr + " ";
        final String fromTime24H = yyyyMMdd + time;
        LocalDateTime resultDateTime = LocalDateTime.parse(fromTime24H, timeFormatter);
        return resultDateTime;
    }

    public static LocalDateTime getLocalDateTime(String yyyyMMdd, String time, DateTimeFormatter timeFormatter) {
        // build String of format yyyy-MM-dd + " " + fromTime=
        final String fromTime24H = yyyyMMdd + time;
        LocalDateTime resultDateTime = LocalDateTime.parse(fromTime24H, timeFormatter);
        return resultDateTime;
    }

    /**
     * Adjusts the given date to a new date that marks the beginning of the week where the
     * given date is located. If "Monday" is the first day of the week and the given date
     * is a "Wednesday" then this method will return a date that is two days earlier than the
     * given date.
     *
     * @param date           the date to adjust
     * @param firstDayOfWeek the day of week that is considered the start of the week ("Monday" in Germany, "Sunday" in the US)
     * @return the date of the first day of the week
     * @see #adjustToLastDayOfWeek(LocalDate, DayOfWeek)
     */
    public static LocalDate adjustToFirstDayOfWeek(LocalDate date, DayOfWeek firstDayOfWeek) {
        LocalDate newDate = date.with(DAY_OF_WEEK, firstDayOfWeek.getValue());
        if (newDate.isAfter(date)) {
            newDate = newDate.minusWeeks(1);
        }

        return newDate;
    }

    /**
     * Adjusts the given date to a new date that marks the end of the week where the
     * given date is located. If "Monday" is the first day of the week and the given date
     * is a "Wednesday" then this method will return a date that is four days later than the
     * given date. This method calculates the first day of the week and then adds six days
     * to it.
     *
     * @param date           the date to adjust
     * @param firstDayOfWeek the day of week that is considered the start of the week ("Monday" in Germany, "Sunday" in the US)
     * @return the date of the first day of the week
     * @see #adjustToFirstDayOfWeek(LocalDate, DayOfWeek)
     */
    public static LocalDate adjustToLastDayOfWeek(LocalDate date, DayOfWeek firstDayOfWeek) {
        LocalDate startOfWeek = adjustToFirstDayOfWeek(date, firstDayOfWeek);
        return startOfWeek.plusDays(6);
    }
}
