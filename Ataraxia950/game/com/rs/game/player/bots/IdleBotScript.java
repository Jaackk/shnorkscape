package com.rs.game.player.bots;

import java.util.concurrent.ThreadLocalRandom;

public class IdleBotScript extends BotScript {

    @Override
    protected void onStart() {
        delay(ThreadLocalRandom.current().nextInt(1, 4));
    }

    @Override
    protected void onTick() {
        if (ThreadLocalRandom.current().nextInt(5) == 0) {
            api().walkNear(bot().getSpawnTile(), 3);
        }
        delay(ThreadLocalRandom.current().nextInt(4, 9));
    }
}
