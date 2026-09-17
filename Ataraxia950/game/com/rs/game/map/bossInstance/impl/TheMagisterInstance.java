package com.rs.game.map.bossInstance.impl;

import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.npc.themagister.TheMagister;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

public class TheMagisterInstance extends BossInstance {

    private transient TheMagister theMagister;
    private boolean battleStarted;
    private WorldObject soulObelisk;

    public TheMagisterInstance(Player owner, InstanceSettings settings) {
        super(owner, settings);
    }

    @Override
    public int[] getMapPos() {
        return new int[] { 273, 855 };
    }

    @Override
    public int[] getMapSize() {
        return new int[] { 2, 2 };
    }

    public Player getPlayer() {
        return getPlayers().isEmpty() ? null : getPlayers().get(0);
    }

    @Override
    public void loadMapInstance() {
        soulObelisk = new WorldObject(109157, 10, 3, getTile(2207, 6885, 0));
        World.spawnObject(soulObelisk);
    }

    public String getInstanceName() {
        return "The Magister";
    }

    @Override
    public void enterInstance(Player player, boolean login) {
        super.enterInstance(player, login);
        player.setForceMultiArea(true);
    }

    @Override
    public void leaveInstance(Player player, int type) {
        player.getInterfaceManager().closeOverlay(true);
        player.setForceMultiArea(false);
        player.removeHPReduction(100);
        super.leaveInstance(player, type);
    }

    @Override
    public void finish() {
        if (theMagister != null)
            theMagister.finish();
        battleStarted = false;
        if (soulObelisk != null && soulObelisk.getId() != 109157) {
            World.removeObject(soulObelisk);
            soulObelisk.setId(109157);
            World.spawnObject(soulObelisk);
        }
        super.finish();
    }

    public TheMagister getTheMagister() {
        return theMagister;
    }

    public void sendMessage(String message) {
        Player player = getPlayer();
        if (theMagister == null || player == null)
            return;
        player.getPackets().sendPlayerMessage(1, 0xFFFFFF, message, true);
    }

    public void updateInterface(boolean sendInterface) {
        Player player = getPlayer();
        if (theMagister == null || player == null)
            return;
        if (sendInterface) {
            player.getInterfaceManager().sendOverlay(945, true);
            for (int i = 23; i < Utils.getInterfaceDefinitionsComponentsSize(945); i++)
                player.getPackets().sendHideIComponent(945, i, true);
            for (int i = 0; i < 6; i++)
                player.getPackets().sendHideIComponent(945, i, i != 4);
            player.getPackets().sendHideIComponent(945, 6, false);
        }
        player.getPackets().sendGlobalString(2381, "The Magister Health (" + Utils.getFormattedNumber(theMagister.getHitpoints()) + "/" + Utils.getFormattedNumber(theMagister.getMaxHitpoints()) + ")");
        player.getPackets().sendGlobalConfig(1233, (int) (((double) theMagister.getHitpoints() / theMagister.getMaxHitpoints()) * 200));
        String message = "";
        message += "<br><col=ff0000>Phase: " + (theMagister.getStage() + 1) + "</col> ";
        if (player.getHPReduction() > 0)
            message += "<br><col=ff0000>Soul drain stacks: " + player.getHPReduction() + "</col> ";
        player.getPackets().sendHideIComponent(945, 23, message.equals(""));
        player.getPackets().sendIComponentText(945, 23, message);
    }

    public void startBattle() {
        if (battleStarted)
            return;
        battleStarted = true;
        soulObelisk.setId(109158);
        World.spawnObject(soulObelisk);
        theMagister = new TheMagister(24765, getTile(new WorldTile(2207, 6872, 0)), this);
        theMagister.setCantInteract(true);
        theMagister.setNextForceTalk(new ForceTalk("You think you can challenge me! I hold the Crossing!"));
        theMagister.setNextAnimation(new Animation(30821));
        theMagister.setNextGraphics(new Graphics(6744));
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                theMagister.setNextAnimation(new Animation(-1));
                theMagister.setNextGraphics(new Graphics(-1));
                theMagister.setCantInteract(false);
                updateInterface(true);
            }
        }, 2);
    }

    public WorldObject getSoulObelisk() {
        return soulObelisk;
    }

    public static void startInstance(Player player) {
        player.setLastBossInstanceSettings(new InstanceSettings(Boss.THE_MAGISTER));
        InstanceSettings settings = player.getLastBossInstanceSettings();
        settings.setSpawnSpeed(BossInstance.FAST);
        settings.setMaxPlayers(settings.getBoss().getMaxPlayers());
        settings.setMinCombat(1);
        settings.setProtection(BossInstance.FFA);
        settings.setCreationTime(Utils.currentTimeMillis());
        BossInstanceHandler.createInstance(player, settings);
    }

    public boolean insideBattleArea(WorldTile tile) {
        WorldTile min = getTile(2194, 6855, 0);
        WorldTile max = getTile(2220, 6887, 0);
        return tile.getX() >= min.getX() && tile.getX() <= max.getX() && tile.getY() >= min.getY() && tile.getY() <= max.getY();
    }
}
