package com.rs.game.map.bossInstance.impl.pz;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Logger;

import lombok.Getter;
import lombok.Setter;
import lombok.val;

public class PZInstance extends BossInstance {

    private final InstanceType instanceType;
    private WorldObject exitPortal;
    private final List<NPC> npcs;
    @Getter
    @Setter
    private boolean sentNoTimeRemainingMessage;

    private static final Class<?>[] ARGUMENTS = new Class<?>[] { int.class, WorldTile.class, PZInstance.class };

    public PZInstance(Player owner, InstanceSettings settings, InstanceType instanceType) {
        super(owner, settings);
        this.instanceType = instanceType;
        npcs = new ArrayList<NPC>();
    }

    @Override
    public void enterInstance(Player player, boolean login) {
        synchronized (BossInstanceHandler.LOCK) {
//            player.setForceMultiArea(true);
            if (!login)
                player.useStairs(-1, getTile(getSettings().getBoss().getInsideTile()), 0, 2);
            getPlayers().add(player);
            playMusic(player);
            player.getPackets().sendGameMessage("Welcome to this session against <col=00FFFF>" + NPCDefinitions.getNPCDefinitions(instanceType.getId()).getName() + "</col>. This arena will expire in " + (getSettings().getTimeRemaining() / 60000) + " minutes.");
            if (!login)
                player.getControlerManager().startControler(getSettings().getBoss().getControllerName(), this);
            player.setLastBossInstanceKey(getOwner() == null ? null : getOwner().getUsername());
        }
    }

    @Override
    public void leaveInstance(Player player, int type) {
        WorldTile leaveTile = player.isDiamondDonor() ? new WorldTile(2085, 4471, 0) : new WorldTile(4144, 5853, 0);
        if (type == EXITED)
            player.useStairs(-1, leaveTile, 0, 2);
        else if (type == LOGGED_OUT && !isPublic())
            player.setLocation(leaveTile);
        player.getMusicsManager().reset();
        getPlayers().remove(player);
        if (getPlayers().isEmpty())
            finish();
    }

    @Override
    public int[] getMapPos() {
        return new int[] { 376, 752 };
    }

    @Override
    public int[] getMapSize() {
        return new int[] { 1, 1 };
    }

    @Override
    public void loadMapInstance() {
        for (val tile : instanceType.getSpawnLocations()) {
            try {
                val instancedTile = getTile(tile);
                npcs.add(instanceType.getNpc().getDeclaredConstructor(ARGUMENTS).newInstance(instanceType.getId(), instancedTile, this));
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                Logger.getGlobal().catching(e);
            }
        }
        exitPortal = new WorldObject(4389, 10, 0, getTile(new WorldTile(3038, 6036, 0)));
        World.spawnObject(exitPortal);
    }

    @Override
    public void finish() {
        if (World.containsObjectWithId(exitPortal, exitPortal.getId()))
            World.removeObject(exitPortal);
        for (NPC npc : npcs)
            npc.finish();
        super.finish();
    }

    public static final void open(final Player player) {
        player.getDialogueManager().startDialogue("InstanceDialogue");
    }

}
