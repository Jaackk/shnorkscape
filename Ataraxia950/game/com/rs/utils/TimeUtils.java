package com.rs.utils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TimeUtils {

    /**
     * @return the time between two {@link java.time.LocalDateTime}s as "HH:MM:SS"
     */
    public static String getHHMMSSTime(LocalDateTime actualFrom, LocalDateTime to) {
        LocalDateTime from = actualFrom;

        long hours = ChronoUnit.HOURS.between(from, to);
        from = from.plusHours(hours);

        return getTwoCharacterNumber(hours) + ":" + getMMSSTime(from, to);
    }

    public static String getMMSSTime(LocalDateTime actualFrom, LocalDateTime to) {
        LocalDateTime from = actualFrom;

        long minutes = ChronoUnit.MINUTES.between(from, to);
        from = from.plusMinutes(minutes);

        long seconds = ChronoUnit.SECONDS.between(from, to);

        return getTwoCharacterNumber(minutes) + ":" + getTwoCharacterNumber(seconds);
    }

    public static long getTimeDifferenceMillis(LocalDateTime from, LocalDateTime to) {
        return getTimeDifference(ChronoUnit.MILLIS, from, to);
    }

    public static long getTimeDifference(ChronoUnit unit, LocalDateTime from, LocalDateTime to) {
        return unit.between(from, to);
    }

    public static String getTwoCharacterNumber(long time) {
        return time < 10 ? "0" + time : time + "";
    }
}
