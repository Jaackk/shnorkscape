package com.rs.game.npc.kalphite;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.HeadIcon;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.Player;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

@SuppressWarnings("serial")
public class KalphiteQueen extends NPC {

    public KalphiteQueen(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, boolean spawned) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        setLureDelay(0);
        setForceAgressive(true);
        requestIconRefresh();
    }

    @Override
    public void handleIngoingHit(Hit hit) {
        boolean secondForm = getId() != 1158 && getId() != 16707;
        if ((secondForm && hit.getLook() == HitLook.MELEE_DAMAGE) || (!secondForm && (hit.getLook() == HitLook.RANGE_DAMAGE || hit.getLook() == HitLook.MAGIC_DAMAGE)))
            hit.setDamage(0);
        super.handleIngoingHit(hit);
    }

    @Override
    public void sendDeath(Entity source) {
        final NPCCombatDefinition defs = getCombatDefinitions();
        resetWalkSteps();
        getCombat().removeTarget();
        setNextAnimation(null);
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                } else if (loop >= defs.getDeathDelay()) {
                    if (getId() == 1158) {
                        setCantInteract(true);
                        transformIntoNPC(1160);
                        setNextGraphics(new Graphics(1055));
                        setNextAnimation(new Animation(6270));
                        WorldTasksManager.schedule(new WorldTask() {

                            @Override
                            public void run() {
                                reset();
                                setCantInteract(false);
                                requestIconRefresh();
                            }

                        }, 5);
                    } else {
                        if (source instanceof Player) {
                            Player plr = (Player) source;
                            if (plr.isGroupIronman()) {
                                plr.gimTracker.incrementBpGained(2);
                            }
                            plr.getActivityTimersManager().finishBossTimer(KalphiteQueen.this);
                            ContractHandler.updateContract(plr, KalphiteQueen.this);
                        }
                        drop();
                        reset();
                        setLocation(getRespawnTile());
                        finish();
                        if (!isSpawned())
                            setRespawnTask();
                        transformIntoNPC(1158);
                    }
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }
    
    private final HeadIcon[][] PRAYER_ICONS = { { new HeadIcon(440, 6) },// Familiar,
            // Magic,
            // Range
{ new HeadIcon(440, 0) },// Melee
};

    
    @Override
    public HeadIcon[] getIcons() {
        return PRAYER_ICONS[getId() == 1160 || getId() == 16708 ? 1 : 0];
    }

}
