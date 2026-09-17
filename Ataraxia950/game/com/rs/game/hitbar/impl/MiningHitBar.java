package com.rs.game.hitbar.impl;

import com.rs.game.hitbar.HitBar;

/** Two exact, viewer-independent mining gauges; paired cache art is pinned by Native950Hitbars. */
public final class MiningHitBar extends HitBar {
    public static final int STAMINA = 7; // Yellow fill, dark background.
    public static final int PROGRESS = 49; // Blue fill, dark background.
    private final int type, percentage;
    private final boolean removal;
    private MiningHitBar(int type, int percentage, boolean removal) {
        this.type = type;
        this.percentage = Math.max(0, Math.min(100, percentage)) * 255 / 100;
        this.removal = removal;
    }
    public static MiningHitBar stamina(int percentage) { return new MiningHitBar(STAMINA, percentage, false); }
    public static MiningHitBar progress(int percentage) { return new MiningHitBar(PROGRESS, percentage, false); }
    public static MiningHitBar removeStamina() { return new MiningHitBar(STAMINA, 0, true); }
    public static MiningHitBar removeProgress() { return new MiningHitBar(PROGRESS, 0, true); }
    @Override public int getType() { return type; }
    @Override public int getPercentage() { return percentage; }
    public boolean isRemoval() { return removal; }
}
