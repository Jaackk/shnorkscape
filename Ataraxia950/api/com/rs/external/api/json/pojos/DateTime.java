package com.rs.external.api.json.pojos;

import lombok.Data;
import lombok.val;

import java.time.LocalDateTime;
import java.time.Month;

@Data
public class DateTime {
    private final String month;
    private final int day;
    private final int year;
    private final int hour;
    private final int minute;
    private final Meridies meridies;

    public LocalDateTime asLocalDateTime() {
        val hour = meridies == Meridies.PM ? getHour() == 12 ? 12 : getHour() + 12
                                : getHour() == 12 ? 0 : getHour();
        return LocalDateTime.of(year, Month.valueOf(month), day, hour, minute);
    }
}
