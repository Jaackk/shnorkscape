package com.rs.game.map.bossInstance.impl;

import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.InstanceSettings;
import com.rs.game.npc.others.Legios;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

import static com.rs.game.MapBuilder.destroyRegion;

public class LegiosInstance extends BossInstance {

    private Legios legio;

    public LegiosInstance(Player owner, InstanceSettings settings) {
        super(owner, settings);
    }

    @Override
    public int[] getMapPos() {
        switch (getBoss()) {
        case LEGIO_PRIMUS:
            return new int[] { 123, 78 };
        case LEGIO_SECUNDUS:
            return new int[] { 136, 83 };
        case LEGIO_TERTIUS:
            return new int[] { 136, 79 };
        case LEGIO_QUARTUS:
            return new int[] { 143, 75 };
        case LEGIO_QUINTUS:
            return new int[] { 148, 77 };
        case LEGIO_SEXTUS:
            return new int[] { 145, 73 };
        default:
            return null;
        }
    }

    @Override
    public int[] getMapSize() {
        return new int[] { 1, 1 };
    }

    @Override
    public void loadMapInstance() {
        switch (getBoss()) {
        case LEGIO_PRIMUS:
            legio = new Legios(17149, getTile(new WorldTile(1018, 644, 1)), this);
            break;
        case LEGIO_SECUNDUS:
            legio = new Legios(17150, getTile(new WorldTile(1106, 683, 1)), this);
            break;
        case LEGIO_TERTIUS:
            legio = new Legios(17151, getTile(new WorldTile(1107, 652, 1)), this);
            break;
        case LEGIO_QUARTUS:
            legio = new Legios(17152, getTile(new WorldTile(1166, 626, 1)), this);
            break;
        case LEGIO_QUINTUS:
            legio = new Legios(17153, getTile(new WorldTile(1202, 630, 1)), this);
            break;
        case LEGIO_SEXTUS:
            legio = new Legios(17154, getTile(new WorldTile(1184, 606, 1)), this);
            break;
        default:
            break;
        }
    }

    @Override
    public void enterInstance(Player player, boolean login) {
        synchronized (BossInstanceHandler.LOCK) {
            player.useStairs(-1, getTile(getSettings().getBoss().getInsideTile()), 0, 2);
            getPlayers().remove(player);
            getPlayers().add(player);
            playMusic(player);
            player.setForceMultiArea(true);
            if (!isPublic()) {
                player.getPackets().sendGameMessage("Welcome to this session against <col=00FFFF>" + getInstanceName() + "</col>. This arena will expire in " + (getSettings().getTimeRemaining() / 60000) + " minutes.");
            }
            if (!login)
                player.getControlerManager().startControler(getSettings().getBoss().getControllerName(), this);
            player.setLastBossInstanceKey(getOwner() == null ? null : getOwner().getUsername());
        }
    }

    @Override
    public String getInstanceName() {
        return Utils.formatPlayerNameForDisplay(getSettings().getBoss().name());
    }

    @Override
    public void leaveInstance(Player player, int type) {
        synchronized (BossInstanceHandler.LOCK) {
            player.stopAll();
            player.lock();
            player.setForceMultiArea(false);
            player.resetCombat();
            if (getPlayers().size() <= 1 && !this.isPublic()) {
                destroyRegion(player.getRegionId());
            }
            if (type == EXITED)
                player.useStairs(-1, getSettings().getBoss().getOutsideTile(), 0, 2);
            else if (type == LOGGED_OUT)
                player.setLocation(getSettings().getBoss().getOutsideTile());
            player.getMusicsManager().reset();
            getPlayers().remove(player);
            if (getPlayers().isEmpty()) {
                finish();
            }
            player.unlock();
        }
    }

    @Override
    public void finish() {
        if (legio != null && !legio.hasFinished())
            legio.finish();
        legio = null;
        super.finish();
    }

    public Player getPlayer() {
        return getPlayers().isEmpty() ? null : getPlayers().get(0);
    }

    public Legios getLegio() {
        return legio;
    }

    public int getLegioId() {
        switch (getBoss()) {
        case LEGIO_PRIMUS:
            return 17149;
        case LEGIO_SECUNDUS:
            return 17150;
        case LEGIO_TERTIUS:
            return 17151;
        case LEGIO_QUARTUS:
            return 17152;
        case LEGIO_QUINTUS:
            return 17153;
        case LEGIO_SEXTUS:
            return 17154;
        default:
            return -1;
        }
    }

}