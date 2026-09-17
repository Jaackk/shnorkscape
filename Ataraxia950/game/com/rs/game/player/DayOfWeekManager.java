package com.rs.game.player;

import java.io.Serializable;
import java.util.Calendar;
import java.util.TimeZone;

public class DayOfWeekManager implements Serializable {

    private static final long serialVersionUID = -2974262334575995608L;
    private transient Player player;
    private int savedDay;

    public DayOfWeekManager() {
        savedDay = -1;
    }

    public void init() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (savedDay != calendar.get(Calendar.DAY_OF_WEEK)) {
            handleNewDay();
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
                handleNewWeek();
            savedDay = calendar.get(Calendar.DAY_OF_WEEK);
        }
    }

    public void process() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (savedDay != calendar.get(Calendar.DAY_OF_WEEK)) {
            handleNewDay();
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY)
                handleNewWeek();
            savedDay = calendar.get(Calendar.DAY_OF_WEEK);
        }
    }

    private void handleNewDay() {
        player.setSpiderBossEnrage(0);
        if (player.getSkills().getDailyWiseXP() > 0)
            player.getPackets().sendGameMessage("As a new day has started, You have 500k bonus xp remaining from wise perk.");
        player.getSkills().setDailyWiseXP(0);
    }

    private void handleNewWeek() {

    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public int getSavedDay() {
        return savedDay;
    }

}
