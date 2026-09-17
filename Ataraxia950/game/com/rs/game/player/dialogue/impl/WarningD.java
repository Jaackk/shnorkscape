package com.rs.game.player.dialogue.impl;

import com.rs.game.player.dialogue.Dialogue;

public class WarningD extends Dialogue {

    private Runnable run;

    @Override
    public void start() {
        int id = (int) parameters[1];
        run = (Runnable) parameters[4];
        if (player.getDoomsayerManager().isWarningOff(id)) {
            end();
            run.run();
            return;
        }
        player.getDoomsayerManager().openWarning((int) parameters[0], id, (String) parameters[2], (String) parameters[3]);
    }

    @Override
    public void run(int interfaceId, int componentId) {
        if (interfaceId == 1262 || interfaceId == 382 || interfaceId == 1292 || interfaceId == 793) {
            if ((interfaceId == 1262 && componentId == 2) || (interfaceId == 382 && componentId == 8) || (interfaceId == 1292 && componentId == 4) || (interfaceId == 793 && componentId == 18)) {
                player.stopAll();
                run.run();
            } else if ((interfaceId == 1262 && componentId == 3) || (interfaceId == 1292 && componentId == 5) || (interfaceId == 793 && componentId == 16)) {
                player.stopAll();
                end();
            } else if (interfaceId == 1262 && componentId == 12)
                player.getDoomsayerManager().toogleCurrentWarning();
            else if ((interfaceId == 793 && componentId == 12)) {
                player.getDoomsayerManager().toogleCurrentWarning();
                player.stopAll();
                run.run();
            }
        }
    }

    @Override
    public void finish() {

    }
}