package com.rs.game.npc.corp;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.HybridTokenDistributor;

import java.util.ArrayList;

public class CorporealBeast extends NPC {

    private static final long serialVersionUID = -7373466967844952156L;
    private DarkEnergyCore core;

    public CorporealBeast(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final boolean spawned) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        setCapDamage(1000);
        setLureDelay(5000);
        setForceTargetDistance(64);
        setForceFollowClose(true);
        setNoDistanceCheck(true);
        setIntelligentRouteFinder(true);
    }

    @Override
    public boolean isIntelligentRouteFinder() {
        return true;
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 0.67;
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.5;
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        if (Settings.USE_DAMAGE_CAP) {
            if (hit.getDamage() > 1000)
                hit.setDamage(1000);
        }
        if (hit.getSource() instanceof Player) {
            final Player p = (Player) hit.getSource();
            if (p != null && !ItemDefinitions.getItemDefinitions(p.getEquipment().getWeaponId()).getName().contains("spear") && !hit.isInstantKill() && !hit.isSpecialHit()) {
                hit.setDamage(hit.getDamage() / 2);
            }
        } else {
            hit.setDamage(hit.getDamage() / 2);
        }
        super.handleIngoingHit(hit);
    }

    @Override
    public void processNPC() {
        super.processNPC();
        if (isDead()) {
            return;
        }
        final int maxhp = getMaxHitpoints();
        if (maxhp > getHitpoints() && getPossibleTargets().isEmpty()) {
            setCapDamage(1000);
            setHitpoints(maxhp);
        }
    }

    public void removeDarkEnergyCore() {
        if (core == null) {
            return;
        }
        core.finish();
        core = null;
    }

    @Override
    public void sendDeath(final Entity source) {
        super.sendDeath(source);
        if (core != null) {
            core.sendDeath(source);
        }
        if (source instanceof Player) {
            Player plr = (Player) source;
            if (plr.isGroupIronman()) {
                plr.gimTracker.incrementBpGained(6);
            }
            HybridTokenDistributor.rollForToken(plr, HybridTokenDistributor.Activity.CORPOREAL_BEAST);
            plr.getActivityTimersManager().finishBossTimer(CorporealBeast.this);
        }
    }

    public void spawnDarkEnergyCore() {
        if (core != null) {
            return;
        }
        core = new DarkEnergyCore(this);
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        return super.getPossibleTargets(false, true);
    }
}