package com.rs.game.player.controllers;

import com.rs.game.Animation;
import com.rs.game.MapBuilder;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.season.GIMDiscordManager;
import com.rs.game.npc.rfd.AgrithNaNa;
import com.rs.game.npc.rfd.Culinaromancer;
import com.rs.game.npc.rfd.Dessourt;
import com.rs.game.npc.rfd.Flambeed;
import com.rs.game.npc.rfd.Karamel;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;

public class ImpossibleJad extends Controller {

    public static final WorldTile OUTSIDE = new WorldTile(4610, 5130, 0);
    public boolean spawned;
    private int[] boundChuncks;
    private Stages stage;

    /*
     * logout or not. if didnt logout means lost, 0 logout, 1, normal, 2 tele
     */
    public void exitCave() {
        stage = Stages.DESTROYING;
        player.setNextWorldTile(new WorldTile(1868, 5348, 0));
        player.setForceMultiArea(false);
        player.reset();
        removeControler();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                MapBuilder.destroyMap(boundChuncks[0], boundChuncks[1], 8, 8);
            }
        }, 1);
    }

    @Override
    public void forceClose() {
        /*
         * shouldnt happen
         */
        if (stage != Stages.RUNNING)
            return;
        exitCave();
    }

    public int getCurrentWave() {
        if (getArguments() == null || getArguments().length == 0)
            return 0;
        return (Integer) getArguments()[0];
    }

    public void setCurrentWave(int wave) {
        if (getArguments() == null || getArguments().length == 0)
            this.setArguments(new Object[1]);
        getArguments()[0] = wave;
    }

    public WorldTile getSpawnTile() {
        return getWorldTile(8, 9);
    }

    /*
     * gets worldtile inside the map
     */
    public WorldTile getWorldTile(int mapX, int mapY) {
        return new WorldTile(boundChuncks[0] * 8 + mapX, boundChuncks[1] * 8 + mapY, 2);
    }

    public void loadCave() {
        stage = Stages.LOADING;
        player.lock(); // locks player
        boundChuncks = MapBuilder.findEmptyChunkBound(8, 8);
        MapBuilder.copyAllPlanesMap(237, 669, boundChuncks[0], boundChuncks[1], 8);
        player.setNextWorldTile(getWorldTile(9, 8));
        player.setForceMultiArea(true);
        player.unlock(); // unlocks player
        stage = Stages.RUNNING;
        startWave();
    }

    @Override
    public boolean login() {
        loadCave();
        return false;
    }

    /*
     * return false so wont remove script
     */
    @Override
    public boolean logout() {
        /*
         * only can happen if dungeon is loading and system update happens
         */
        if (stage != Stages.RUNNING)
            return false;
        exitCave();
        return false;

    }

    @Override
    public void magicTeleported(int type) {
        exitCave();
    }

    @Override
    public void moved() {
        if (stage != Stages.RUNNING)
            return;
    }

    @Override
    public boolean processButtonClick(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
        return stage == Stages.RUNNING;
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        return false;
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        return false;
    }

    /**
     * return process normaly
     */
    @Override
    public boolean processObjectClick1(WorldObject object) {
        if (object.getId() == 7315) {
            if (stage != Stages.RUNNING)
                return false;
            exitCave();
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectTeleport(WorldTile toTile) {
        return false;
    }

    public void removeNPC() {
        if (!player.isKilledAgrithNaNa()) {
            player.setKilledAgrithNaNa(true);
        } else if (!player.isKilledFlambeed()) {
            player.setKilledFlamBeed(true);
        } else if (!player.isKilledFlambeed()) {
            player.setKilledKaramel(true);
        } else if (!player.isKilledKaramel()) {
            player.setKilledKaramel(true);
        } else if (!player.isKilledDessourt()) {
            player.setKilledDessourt(true);
        }
        startWave();
    }

    @Override
    public boolean sendDeath() {
        player.lock(7);
        player.stopAll();
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    player.setNextAnimation(new Animation(836));
                } else if (loop == 1) {
                    player.getPackets().sendGameMessage("Oh dear you have died!");
                } else if (loop == 3) {
                    player.reset();
                    exitCave();
                    player.setNextAnimation(new Animation(-1));
                } else if (loop == 4) {
                    player.getPackets().sendMusicEffect(90);
                    stop();
                }
                loop++;
            }
        }, 0, 1);
        return false;
    }

    @Override
    public void start() {
        if (player.isKilledCulinaromancer()) {
            removeControler();
            return;
        }
        player.getPrayer().closeAllPrayers();
        loadCave();
    }

    public void startWave() {
        if (stage != Stages.RUNNING)
            return;
        if (!player.isKilledAgrithNaNa()) {
            AgrithNaNa npc = new AgrithNaNa(3493, getSpawnTile(), this);
            npc.setNextAnimation(new Animation(-1));
            return;
        }
        if (!player.isKilledFlambeed()) {
            Flambeed npc = new Flambeed(3494, getSpawnTile(), this);
            npc.setNextAnimation(new Animation(-1));
            return;
        }
        if (!player.isKilledKaramel()) {
            Karamel npc = new Karamel(3495, getSpawnTile(), this);
            npc.setNextAnimation(new Animation(-1));
            return;
        }
        if (!player.isKilledDessourt()) {
            Dessourt npc = new Dessourt(3496, getSpawnTile(), this);
            npc.setNextAnimation(new Animation(-1));
            return;
        }
        if (!player.isKilledCulinaromancer()) {
            Culinaromancer npc = new Culinaromancer(3491, getSpawnTile(), this);
            npc.setNextAnimation(new Animation(-1));
            return;
        }
    }

    public void win() {
        if (stage != Stages.RUNNING)
            return;
        player.setKilledCulinaromancer(true);
        player.setNextAnimation(new Animation(862));
        player.getInterfaceManager().sendInterface(277);
        player.getPackets().sendIComponentText(277, 4, "You have completed Recipe for Disaster!");
        player.getPackets().sendIComponentText(277, 9, "Full access to the Cunliaromancer's Chest.");
        player.getPackets().sendIComponentText(277, 7, "1");
        player.getPackets().sendIComponentText(277, 10, "");
        player.getPackets().sendIComponentText(277, 10, "Requirement for Completionist Cape.");
        player.getPackets().sendIComponentText(277, 11, "");
        player.getPackets().sendIComponentText(277, 12, "");
        player.getPackets().sendIComponentText(277, 13, "");
        player.getPackets().sendIComponentText(277, 14, "");
        player.getPackets().sendIComponentText(277, 15, "");
        player.getPackets().sendIComponentText(277, 16, "");
        player.getPackets().sendIComponentText(277, 17, "");
        player.getPackets().sendItemOnIComponent(277, 5, 7462, 1);
        exitCave();

        QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/rfd.png\" height=17> " + player.getDisplayName() + " has just completed the Recipe for Disaster Mini-Quest."));
        //GIM.getDiscordManager().addHighlight(player, "<#player> completed the Recipe for Disaster Mini-Quest!");
    }

    private enum Stages {
        LOADING, RUNNING, DESTROYING
    }
}