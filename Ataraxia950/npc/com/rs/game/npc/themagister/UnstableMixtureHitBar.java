package com.rs.game.npc.themagister;

import com.rs.game.hitbar.HitBar;
import com.rs.utils.Utils;

public class UnstableMixtureHitBar extends HitBar {
    private long totalTime = 0;
    private final long timeToExplode;

    public UnstableMixtureHitBar(long totalTime, long timeToExplode) {
        this.totalTime = totalTime;
        this.timeToExplode = timeToExplode;
    }

    @Override
    public int getType() {
        return 9;
    }

    @Override
    public int getPercentage() {
        if (Utils.currentTimeMillis() > timeToExplode)
            return 0;
        long timeRemaining = timeToExplode - Utils.currentTimeMillis();
        int percentage = (int) (((((double) timeRemaining / (double) totalTime) * 100) * 255) / 100);
        return percentage;
    }

}
