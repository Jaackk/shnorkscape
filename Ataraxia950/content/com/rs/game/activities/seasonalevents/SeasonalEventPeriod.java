package com.rs.game.activities.seasonalevents;

import lombok.val;

import java.time.LocalDate;
import java.time.Month;

/**
 * A model representing the seasonal event date range. All ranges are within the current year.
 *
 * @author lare96
 */
public final class SeasonalEventPeriod {

    private final LocalDate from;
    private final LocalDate to;

    public SeasonalEventPeriod(Month fromMonth, int fromDay, Month toMonth, int toDay) {
        val now = LocalDate.now();
        from = LocalDate.of(now.getYear(), fromMonth, fromDay);
        to = LocalDate.of(now.getYear(), toMonth, toDay);
    }

    boolean isWithin(LocalDate date) {
        return date.isEqual(from) || // Date is equal to start date or
                date.isEqual(to) || // Date is equal to end date or
                date.isAfter(from) && date.isBefore(to); // Date is between start and end date.
    }
}
