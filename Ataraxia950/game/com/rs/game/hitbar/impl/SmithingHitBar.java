package com.rs.game.hitbar.impl;

import com.rs.game.hitbar.HitBar;

public class SmithingHitBar extends HitBar {

    private static final int HEAT_BAR_TYPE = 5;
    private static final int PROGRESS_BAR_TYPE = 7;

    private final int type;
    private final int percentage;

    private SmithingHitBar(int type, int percentage) {
        this.type = type;
        this.percentage = Math.max(0, Math.min(100, percentage));
    }

    public static SmithingHitBar heat(int percentage) {
        return new SmithingHitBar(HEAT_BAR_TYPE, percentage);
    }

    public static SmithingHitBar progress(int percentage) {
        return new SmithingHitBar(PROGRESS_BAR_TYPE, percentage);
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public int getPercentage() {
        return percentage * 255 / 100;
    }
}
