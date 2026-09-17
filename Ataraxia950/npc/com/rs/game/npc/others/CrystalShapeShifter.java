package com.rs.game.npc.others;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

/**
 * @author Tom
 * @date April 24, 2017
 */

@SuppressWarnings("serial")
public class CrystalShapeShifter extends NPC {

    private int phase;

    public CrystalShapeShifter(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        setPhase(0);
        setIntelligentRouteFinder(true);
    }

    @Override
    public void processNPC() {
        super.processNPC();
    }

    @Override
    public void handleIngoingHit(Hit hit) {
        Entity target = hit.getSource();
        if (hit.getLook() == HitLook.MAGIC_DAMAGE && !((Player) target).getPrayer().isRangeProtecting()) {
            setNextAnimationForce(new Animation(27167));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    if (((Player) target).getPrayer().isRangeProtecting())
                        transformIntoNPC(21632);
                    else
                        transformIntoNPC(21631);
                    stop();
                }
            }, 2, 1);
        } else if (hit.getLook() == HitLook.RANGE_DAMAGE && !((Player) target).getPrayer().isMeleeProtecting()) {
            setNextAnimationForce(new Animation(27167));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    if (((Player) target).getPrayer().isMeleeProtecting())
                        transformIntoNPC(21631);
                    else {
                        transformIntoNPC(21630);
                        setRun(true);
                    }
                    stop();
                }
            }, 2, 1);
        } else if (hit.getLook() == HitLook.MELEE_DAMAGE && !((Player) target).getPrayer().isMageProtecting()) {
            setNextAnimationForce(new Animation(27167));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    if (((Player) target).getPrayer().isMageProtecting()) {
                        transformIntoNPC(21630);
                        setRun(true);
                    } else
                        transformIntoNPC(21632);
                    stop();
                }
            }, 2, 1);
        }
        super.handlePrayers(hit);
    }

    @Override
    public void sendDeath(final Entity source) {
        super.sendDeath(source);
        setNextAnimationForce(new Animation(this.getCombatDefinitions().getDeathEmote()));
        setNextGraphics(new Graphics(5760));
    }

    @Override
    public void spawn() {
        super.spawn();
        setNextAnimationForce(new Animation(27136));
    }

    public int getPhase() {
        return phase;
    }

    public void nextPhase() {
        phase++;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

}
