package com.rs.game.activities.seasonalevents.impl;

import com.rs.game.activities.seasonalevents.SeasonalEvent;
import com.rs.game.activities.seasonalevents.SeasonalEventPeriod;

import java.time.Month;

public class ValentinesDaySeasonalEvent extends SeasonalEvent {
    @Override
    protected void start() {

    }

    @Override
    protected void end() {

    }

    @Override
    protected SeasonalEventPeriod computePeriod() {
        return new SeasonalEventPeriod(Month.FEBRUARY, 13, Month.FEBRUARY, 15);
    }
}
