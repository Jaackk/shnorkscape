package com.rs.game.player.bots;

import com.rs.utils.Logger;

public abstract class BotScript {

    private BotPlayer bot;
    private BotApi api;
    private boolean running;
    private int delayTicks;

    final void bind(BotPlayer bot) {
        this.bot = bot;
        this.api = new BotApi(bot);
        this.running = true;
        onStart();
    }

    final void pulse() {
        if (!running || bot == null || bot.hasFinished()) {
            return;
        }
        if (delayTicks > 0) {
            delayTicks--;
            return;
        }
        if (!canActWhileBusy() && isBusy()) {
            return;
        }
        try {
            onTick();
        } catch (Throwable e) {
            Logger.getGlobal().catching(e);
            stop();
        }
    }

    protected abstract void onTick();

    protected void onStart() {
    }

    protected void onStop() {
    }

    public String getDebugInfo() {
        return getClass().getSimpleName();
    }

    protected boolean canActWhileBusy() {
        return false;
    }

    protected final void delay(int ticks) {
        delayTicks = Math.max(0, ticks);
    }

    protected final BotPlayer bot() {
        return bot;
    }

    protected final BotApi api() {
        return api;
    }

    public final boolean isRunning() {
        return running;
    }

    public final void stop() {
        if (!running) {
            return;
        }
        running = false;
        onStop();
    }

    private boolean isBusy() {
        return bot.isDead()
                || bot.isLocked()
                || bot.getRouteEvent() != null
                || bot.hasWalkSteps()
                || bot.getActionManager().getAction() != null;
    }
}
