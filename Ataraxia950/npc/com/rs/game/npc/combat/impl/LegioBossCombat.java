package com.rs.game.npc.combat.impl;

import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.CombatScript;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.others.Legios;
import com.rs.game.player.Player;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

/**
 * Handles all six Legio bosses within the Monastery of Ascension.
 *
 * @author Noel edited by ARMAR X K1NG
 */
public class LegioBossCombat extends CombatScript {

    @Override
    public int attack(final NPC npc, final Entity target) {
        if (!(npc instanceof Legios))
            return 0;
        Legios legio = (Legios) npc;
        int attackRotation = legio.getAttackRotation();
        if (legio.getId() == 17154)
            return sendSextusLightningStrikeAttack(legio, target);
        if (legio.getId() == 17150 && legio.getStage() > 0) {
            if (legio.getStage() == 3)
                return sendLightningStrikeAttack(legio, target);
            legio.setAttackRotation(attackRotation + 1 >= (legio.getStage() == 1 ? 4 : 5) ? 0 : attackRotation + 1);
            switch (attackRotation) {
            case 0:
            case 1:
            case 2:
                return sendLightningStrikeAttack(legio, target);
            case 3:
                if (legio.getStage() == 2)
                    return sendLightningStrikeAttack(legio, target);
                return sendAutoAttack(legio, target);
            case 4:
                return sendAutoAttack(legio, target);
            }
        }
        legio.setAttackRotation(attackRotation + 1 >= 3 ? 0 : attackRotation + 1);
        switch (attackRotation) {
        case 0:
        case 1:
            return sendLightningStrikeAttack(legio, target);
        case 2:
            return sendAutoAttack(legio, target);
        }
        return 0;
    }

    public int sendAutoAttack(Legios legio, Entity target) {
        legio.setNextAnimation(new Animation(28702));
        legio.setNextGraphics(new Graphics(3977));
        World.sendProjectileCycles(legio, target, 3978, 65, 25, 30, 40, 5, 10);
        long projectileCycles = 600;
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            @Override
            public boolean repeat() {
                try {
                    target.setNextGraphics(new Graphics(3979));
                    int maxHit = 300;
                    if (legio.getId() == 17149)
                        maxHit *= 1 + (legio.getStage() * 0.15);
                    int damage = CombatScript.getMaxHit(legio, maxHit, NPCCombatDefinitionConstants.MAGE, target);
                    CombatScript.delayHit(legio, 0, target, CombatScript.getMagicHit(legio, damage));
                    return false;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
            }
        }, projectileCycles, 600, TimeUnit.MILLISECONDS);
        return 4;
    }

    public int sendLightningStrikeAttack(Legios legio, Entity target) {
        legio.setNextAnimation(new Animation(28700));
        legio.setNextGraphics(new Graphics(3975));
        WorldTile tile = new WorldTile(target);
        World.sendGraphics(legio, new Graphics(3974, 90, 0), tile);
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            int loop = 0;
            int delay = 0;

            @Override
            public boolean repeat() {
                try {
                    if (loop >= 1200 || legio.isDead() || legio.hasFinished() || target.hasFinished() || target.isDead()) {
                        delay = 0;
                        return false;
                    }
                    boolean onDelay = delay >= loop;
                    if (!onDelay) {
                        int damage = target.matches(tile) ? Utils.random(220, 241) : loop >= 300 && Utils.isOnRange(tile, target, 0, 1, 1) ? Utils.random(180, 200) : loop >= 500 && Utils.isOnRange(tile, target, 1, 1, 1) ? Utils.random(140, 160) : 0;
                        if (damage > 0) {
                            if (target instanceof Player && ((Player) target).getPrayer().isMageProtecting())
                                damage -= Utils.random(20, 30);
                            if (legio.getId() == 17149)
                                damage += (legio.getStage() * 20);
                            CombatScript.delayHit(legio, 0, target, new Hit(legio, damage, HitLook.UNBLOCKABLE_MAGIC_DAMAGE));
                            delay = (target.matches(tile) ? 300 : Utils.isOnRange(tile, target, 0, 1, 1) ? 400 : 600) + loop;
                        }
                    }
                    loop++;
                    return true;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
            }
        }, 1600, 1, TimeUnit.MILLISECONDS);
        return 5;
    }

    public int sendSextusLightningStrikeAttack(Legios legio, Entity target) {
        legio.setNextAnimation(new Animation(28700));
        legio.setNextGraphics(new Graphics(3975));
        WorldTile tile = new WorldTile(target);
        World.sendGraphics(legio, new Graphics(3974, 90, 0), tile);
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            int loop = 0;
            int delay = 0;
            int repeat = 0;

            @Override
            public boolean repeat() {
                try {
                    if ((loop >= 1200 && repeat == 3) || legio.isDead() || legio.hasFinished() || target.hasFinished() || target.isDead()) {
                        delay = 0;
                        return false;
                    }
                    if (loop >= 1200 && repeat < 3) {
                        loop = 0;
                        delay = 0;
                        repeat++;
                        World.sendGraphics(legio, new Graphics(3974), tile);
                    }
                    boolean onDelay = delay >= loop;
                    if (!onDelay) {
                        int damage = target.matches(tile) ? Utils.random(220, 241) : loop >= 300 && Utils.isOnRange(tile, target, 0, 1, 1) ? Utils.random(180, 200) : loop >= 500 && Utils.isOnRange(tile, target, 1, 1, 1) ? Utils.random(140, 160) : 0;
                        if (damage > 0) {
                            if (target instanceof Player && ((Player) target).getPrayer().isMageProtecting())
                                damage -= Utils.random(20, 30);
                            CombatScript.delayHit(legio, 0, target, new Hit(legio, damage, HitLook.UNBLOCKABLE_MAGIC_DAMAGE));
                            delay = (target.matches(tile) ? 300 : Utils.isOnRange(tile, target, 0, 1, 1) ? 400 : 600) + loop;
                        }
                    }
                    loop++;
                    return true;
                } catch (Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
            }
        }, 1600, 1, TimeUnit.MILLISECONDS);
        return 5;
    }

    // bomb attack gfx 3975

    @Override
    public Object[] getKeys() {
        return new Object[] { 17149, 17150, 17151, 17152, 17153, 17154 };
    }
}