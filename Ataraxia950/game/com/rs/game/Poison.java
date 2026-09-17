package com.rs.game;

import com.rs.game.Hit.HitLook;
import com.rs.game.player.Player;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

import java.io.Serializable;

public final class Poison implements Serializable {

    private static final long serialVersionUID = -6324477860776313690L;

    private transient Entity entity;
    private int poisonDamage;
    private int poisonCount;

    public Entity getEntity() {
        return entity;
    }

    public int getPoisonDamage() {
        return poisonDamage;
    }

    public void setPoisonDamage(int amount) {
        this.poisonDamage = amount;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public boolean isPoisoned() {
        return poisonDamage >= 1;
    }

    public void makePoisoned(int startDamage) {
        makePoisoned(startDamage, 0);
    }

    /**
     * lowerTheImmunity -> in milliseconds
     */
    public void makePoisoned(int startDamage, long lowerTheImmunity) {
        if (poisonDamage > startDamage)
            return;
        if (entity instanceof Player) {
            Player player = ((Player) entity);
            if (player.getPoisonImmune() > Utils.currentTimeMillis()) {
                if (lowerTheImmunity > 0) {
                    player.setPoisonImmune((player.getPoisonImmune() - lowerTheImmunity) < 0 ? 0 : (player.getPoisonImmune() - lowerTheImmunity));
                    player.getPackets().sendGameMessage("Your immunity to poison time has been lowered!", true);
                }
                return;
            }
            Perk venomBlood = player.getInventionManager().hasPerk(Perks.VENOMBLOOD);
            if (venomBlood != null) {
                player.getPackets().sendGameMessage("Your Venom Blood Perk prevented you from getting poisoned.", true);
                return;
            }
            if (player.getEquipment().getShieldId() == 18340 || player.getEquipment().getPocketId() == 40681)
                return;
            if (poisonDamage == 0 && !isPoisoned())
                player.sendMessage("You are poisoned.");
        }
        poisonDamage = startDamage;
        refresh();
    }

    public void processPoison() {
        if (!entity.isDead() && isPoisoned()) {
            if (entity instanceof Player && ((Player) entity).getPoisonImmune() > Utils.currentTimeMillis()) {
                reset();
                return;
            }
            if (poisonCount > 0) {
                poisonCount--;
                return;
            }
            boolean heal = false;
            if (entity instanceof Player) {
                Player player = ((Player) entity);
                // inter opened we dont poison while inter opened like at rs
                if (player.getInterfaceManager().containsScreenInter())
                    return;
                if (player.getAuraManager().hasPoisonPurge())
                    heal = true;
                Perk venomBlood = player.getInventionManager().hasPerk(Perks.VENOMBLOOD);
                if (!heal && venomBlood != null) {
                    player.getPackets().sendGameMessage("Your Venom Blood Perk nulls the poison you have.", true);
                    reset();
                    return;
                }
            }
            entity.applyHit(new Hit(entity, poisonDamage, heal ? HitLook.HEALED_DAMAGE : HitLook.POISON_DAMAGE));
            poisonDamage -= 2;
            if (isPoisoned()) {
                poisonCount = 30;
                return;
            }
            reset();
        }
    }

    public void refresh() {
        if (entity instanceof Player) {
            Player player = ((Player) entity);
            player.getPackets().sendConfig(722, isPoisoned() ? 1 : 0);
        }
    }

    public void reset() {
        poisonDamage = 0;
        poisonCount = 0;
        refresh();
    }

    public void cureGroup(final Player player, final Player p2) {
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                p2.setNextGraphics(new Graphics(745, 0, 100));
                p2.getPoison().reset();
                p2.getPackets().sendGameMessage("Your afflictions have been cured by " + player.getDisplayName() + ".");

            }
        }, 1);
    }
}
