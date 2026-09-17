package com.rs.game.activities;

import com.rs.external.api.json.pojos.DateTime;
import com.rs.utils.TimeUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

@AllArgsConstructor
public class Activity {
    @Getter
    private final String eventName;
    @Getter
    private final String announcement;
    @Getter
    private final DateTime date;

    @Override
    public String toString() {
	val month = date.getMonth().substring(0, 1).toUpperCase() + date.getMonth().substring(1).toLowerCase();
        val minute = TimeUtils.getTwoCharacterNumber(date.getMinute());
        return eventName + " - " + date.getDay() + " " + month + " " + date.getYear() + " @ " + date.getHour() + ":" + minute + " " + date.getMeridies().toString() + " GMT/UTC";
    }
}
