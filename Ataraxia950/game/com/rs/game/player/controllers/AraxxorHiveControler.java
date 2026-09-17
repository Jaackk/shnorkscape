package com.rs.game.player.controllers;

import com.rs.game.player.Player;
import com.rs.utils.Utils;

public class AraxxorHiveControler extends Controller {
    private long spidersDelay;

    @Override
    public void start() {
        spidersDelay = Utils.currentTimeMillis() + 40000;// 10
                                                         // seconds
        sendInterfaces();
    }

    @Override
    public void sendInterfaces() {
        player.getInterfaceManager().sendOverlay(1515, true);
        player.getPackets().sendExecuteScript(7188, 0);
    }

    @Override
    public void process() {
        if (spidersDelay != 0 && Utils.currentTimeMillis() >= spidersDelay) {
            int spiderType = Utils.random(4);
            int moveRot = Utils.random(4);
            int dir = Utils.random(2);
            player.getPackets().sendExecuteScript(7190, moveRot, dir, spiderType);
            spidersDelay = Utils.currentTimeMillis() + 40000;
        }
    }

    @Override
    public void moved() {
        if (!isAtAraxxorHive(player)) {
            player.getInterfaceManager().closeOverlay(true);
            removeControler();
        }
    }

    @Override
    public boolean login() {
        sendInterfaces();
        return false;
    }

    @Override
    public boolean logout() {
        return false;
    }

    public static boolean isAtAraxxorHive(Player player) {
        return player.withinArea(4481, 6256, 4518, 6295);
    }
}
