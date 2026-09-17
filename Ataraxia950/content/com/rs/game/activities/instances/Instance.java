package com.rs.game.activities.instances;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.MapBuilder;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.ZGDController;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.Utils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Kris {@link https://www.rune-server.ee/members/kris/ }
 */
public abstract class Instance {

    public List<Player> players;
    public final Player owner;
    public final int instanceDuration, respawnSpeed, playersLimit, bossId, password;
    public int totalSeconds;
    public int[] boundChunks, sizes, chunksToBind;
    public final boolean hardMode;
    public boolean isStable, finished;
    public NPC boss;

    public Instance(Player owner, int instanceDuration, int respawnSpeed, int playersLimit, int password, int bossId, boolean hardMode) {
        players = new CopyOnWriteArrayList<Player>();
        this.owner = owner;
        this.instanceDuration = getTime(owner, instanceDuration);
        this.respawnSpeed = respawnSpeed;
        this.playersLimit = playersLimit;
        this.password = password;
        this.bossId = bossId;
        this.hardMode = hardMode;
        isStable = true;
        addPlayer(owner);
        World.getInstances().add(this);
    }

    public static final int getTime(Player player, int time) {
        if (player.isMasterDonator())
            return (int) (time * 1.3);
        if (player.isUltimateDonator())
            return (int) (time * 1.25);
        if (player.isSupremeDonator())
            return (int) (time * 1.2);
        if (player.isLegendaryDonator())
            return (int) (time * 1.15);
        if (player.isExtremeDonator())
            return (int) (time * 1.1);
        if (player.isDonator())
            return (int) (time * 1.05);
        return time;
    }

    public abstract NPC getBossNPC();

    public abstract WorldTile getWaitingRoomCoords();

    public abstract WorldTile getOutsideCoordinates();

    public final void destroyInstance() {
        MapBuilder.destroyMap(boundChunks[0], boundChunks[1], sizes[0], sizes[1]);
        World.getInstances().remove(this);
        isStable = false;
        finished = true;
        if (boss != null && !boss.hasFinished())
            boss.finish();
    }

    public final void constructInstance() {
        MapBuilder.copyAllPlanesMap(chunksToBind[0], chunksToBind[1], boundChunks[0], boundChunks[1], sizes[0], sizes[1]);
        owner.setNextWorldTile(getWaitingRoomCoords());
        initiateSpawningSequence();
    }

    public void enterInstance(Player player) {
        int count = 0;
        if (getPlayers() != null)
            for (Player p : getPlayers()) {
                if (p == null)
                    continue;
                if (p.hasFinished() || p.isDead())
                    continue;
                if (!p.withinDistance(getWorldTile(0, 0), 100))
                    continue;
                count++;
            }
        if (playersLimit > 0 && count >= playersLimit && !player.equals(owner) && !players.contains(player)) {
            player.sendMessage("This instance is currently full.");
            return;
        }
        if (!isStable) {
            player.sendMessage("The instance isn't stable enough to enter it.");
            return;
        }
        if (password != -1 && !player.getDisplayName().equalsIgnoreCase(owner.getDisplayName())) {
            player.sendInputInteger("instancepasswordenter", new InputIntegerEvent() {
                @Override
                public void run(Player player) {
                    enterInstance(player, getInteger());
                }
            });
            return;
        }
        addPlayer(player);
        player.setNextWorldTile(getWaitingRoomCoords());
        if (this instanceof NexInstance)
            player.getControlerManager().startControler(ZGDController.class.getSimpleName());
    }

    public final void enterInstance(Player player, int password) {
        int count = 0;
        if (getPlayers() != null)
            for (Player p : getPlayers()) {
                if (p == null)
                    continue;
                if (p.hasFinished() || p.isDead())
                    continue;
                if (!p.withinDistance(getWorldTile(0, 0), 100))
                    continue;
                count++;
            }
        if (playersLimit > 0 && count >= playersLimit && !player.equals(owner) && !players.contains(player)) {
            player.sendMessage("This instance is currently full.");
            return;
        }
        if (!isStable) {
            player.sendMessage("The instance isn't stable enough to enter it.");
            return;
        }
        if (password != this.password) {
            player.sendMessage("The password you entered is incorrect.");
            return;
        }
        addPlayer(player);
        player.setNextWorldTile(getWaitingRoomCoords());
        if (this instanceof NexInstance)
            player.getControlerManager().startControler(ZGDController.class.getSimpleName());
    }

    public abstract void performOnSpawn();

    public void initiateSpawningSequence() {
        Instance instance = this;
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            private int seconds;
            private boolean resetSeconds;

            @Override
            public boolean repeat() {
                if (!isStable && players.size() == 0 || (totalSeconds % 60 == 0 && (totalSeconds / 60) == instanceDuration + 5)) {
                    if (players.size() > 0) {
                        players.forEach(player -> {
                            if (player != null && player.getCurrentInstance() == instance)
                                player.setNextWorldTile(getOutsideCoordinates());
                        });
                    }
                    destroyInstance();
                    if (boss != null)
                        boss.finish();
                    return false;
                }
                if (!isOwnerInstance()) {
                    if (boss != null)
                        boss.finish();
                    return false;
                }
                if (seconds == 0 && !finished) {
                    resetSeconds = false;
                    if (boss == null || boss.hasFinished()) {
                        boss = getBossNPC();
                        boss.setForceMultiArea(true);
                        performOnSpawn();
                    }
                }
                if (boss.hasFinished() && !resetSeconds) {
                    seconds = 0 - respawnSpeed;
                    resetSeconds = true;
                }
                if (totalSeconds % 60 == 0 && (totalSeconds / 60) == instanceDuration) {
                    finished = true;
                    players.forEach(player -> player.sendMessage("The instance has ended. No more monsters will be spawned in this instance."));
                }
                if (totalSeconds % 60 == 0 && (totalSeconds / 60) == instanceDuration - 2) {
                    players.forEach(player -> player.sendMessage("The instance will remain open for two more minutes."));
                    isStable = false;
                }
                seconds++;
                totalSeconds++;
                return true;
            }

        }, 0, 1);
    }

    public WorldTile getWorldTile(int x, int y) {
        return new WorldTile((boundChunks[0] * 8) + x, (boundChunks[1] * 8) + y, 0);
    }

    public void addPlayer(Player player) {
        players.add(player);
        player.setForceMultiArea(true);
        player.setCurrentInstance(this);
        if (owner != player)
            player.sendMessage("You've joined " + Utils.formatPlayerNameForDisplay(owner.getDisplayName()) + "'s " + (hardMode ? "hard " : "normal ") + "instance. This instance will remain active for approximately " + getMinutesRemaining() + " minutes.");
    }

    public final boolean removePlayer(Player player) {
        player.setCurrentInstance(null);
        return players.remove(player);
    }

    public final int getMinutesRemaining() {
        return (int) Math.floor(instanceDuration - (totalSeconds / 60));
    }

    public final boolean isHardMode() {
        return hardMode;
    }

    public final int getBoss() {
        return bossId;
    }

    public final int getPassword() {
        return password;
    }

    public final int getPlayersLimit() {
        return playersLimit;
    }

    public final int getRespawnSpeed() {
        return respawnSpeed;
    }

    public final int getInstanceDuration() {
        return instanceDuration;
    }

    public final List<Player> getPlayers() {
        return players;
    }

    public final Player getOwner() {
        return owner;
    }

    public boolean isOwnerInstance() {
        if (getPlayers().isEmpty())
            return true;
        boolean isOwnerInstance = true;
        for (Player p : getPlayers())
            if (p != null && p.getCurrentInstance() != null && p.getCurrentInstance() != this)
                isOwnerInstance = false;
        return isOwnerInstance;
    }

}