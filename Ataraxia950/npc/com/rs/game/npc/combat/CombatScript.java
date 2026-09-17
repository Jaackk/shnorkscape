package com.rs.game.npc.combat;

import com.rs.Settings;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.combat.rs2.Rs2AtaraxiaNumerics;
import com.rs.game.player.content.Combat;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.concurrent.ThreadLocalRandom;

public abstract class CombatScript {

    public static void delayHit(NPC npc, int delay, final Entity target, Runnable onHit, final Hit... hits) {
        npc.getCombat().addAttackedByDelay(target);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                if (onHit != null)
                    onHit.run();
                for (Hit hit : hits) {
                    NPC npc = (NPC) hit.getSource();
                    if (npc.isDead() || npc.hasFinished() || target.isDead() || target.hasFinished())
                        return;
                    target.applyHit(hit);
                    npc.getCombat().doDefenceEmote(target);
                    if (target instanceof Player) {
                        Player p2 = (Player) target;
                        // p2.closeInterfaces();
                        if (p2.getCombatDefinitions().isAutoRetaliate() && !p2.getActionManager().hasSkillWorking() && !p2.hasWalkSteps())
                            p2.getActionManager().setAction(new PlayerCombat(npc));
                    } else {
                        NPC n = (NPC) target;
                        if (!n.isUnderCombat() || n.canBeAttackedByAutoRetaliate())
                            n.setTarget(npc);
                    }
                }
            }
        }, delay);
    }

    public static void delayHit(NPC npc, int delay, final Entity target, final Hit... hits) {
        delayHit(npc, delay, target, null, hits);
    }

    public static void delayIndirectHit(int delay, final Player target, int dmg, HitLook look, NPC dummyBoss) {
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {

                if (!target.isDead() && !target.hasFinished()) {
                    Hit hit = new Hit(null, dmg, look);
                    target.applyHit(hit);
                    target.getActionManager().setAction(new PlayerCombat(dummyBoss));
                }
            }

        }, delay);
    }

    public static Hit getMagicHit(NPC npc, int damage) {
        return new Hit(npc, damage, HitLook.MAGIC_DAMAGE);
    }

    public static Hit getMeleeHit(NPC npc, int damage) {
        return new Hit(npc, damage, HitLook.MELEE_DAMAGE);
    }

    public static int getMaxHit(NPC npc, int attackType, Entity target) {
        return getRandomMaxHit(npc, npc.getMaxHit(attackType), attackType, target);
    }

    public static int getMaxHit(NPC npc, int attackType, Entity target, boolean doesntMiss) {
        return getRandomMaxHit(npc, npc.getMaxHit(attackType), attackType, target, doesntMiss);
    }
    public static int getMaxHit(NPC npc, int attackType, Entity target, double damageMod, boolean doesntMiss) {
        return getRandomMaxHit(npc, npc.getMaxHit(attackType), attackType, target, damageMod, doesntMiss);
    }

    public static int getRandomMaxHit(NPC npc, int maxHit, int attackStyle, Entity target) {
        return getRandomMaxHit(npc, maxHit, attackStyle, target, 1.0, false);
    }

    public static int getRandomMaxHit(NPC npc, int maxHit, int attackStyle, double dmgModifier, Entity target) {
        return getRandomMaxHit(npc, maxHit, attackStyle, target, dmgModifier, false);
    }

    public static int getRandomMaxHit(NPC npc, int maxHit, int attackStyle, Entity target, boolean doesntMiss) {
        return getRandomMaxHit(npc, maxHit, attackStyle, target, 1.0, doesntMiss);
    }

    public static int getRandomMaxHit(NPC npc, int maxHit, int attackStyle, Entity target, double dmgModifier, boolean doesntMiss) {
        attackStyle = attackStyle == NPCCombatDefinitionConstants.RANGE ? Combat.RANGE_TYPE : attackStyle == NPCCombatDefinitionConstants.MAGE ? Combat.MAGIC_TYPE : attackStyle == NPCCombatDefinitionConstants.MELEE ? Combat.MELEE_TYPE : Combat.ALL_TYPE;
        double hitChance = 0;
        if (!doesntMiss) {
            hitChance = Combat.getHitChance(npc, target, attackStyle, true);
            if (npc.getTemporaryModifiersManager().hasActiveModifier(Key.HIT_CHANCE_MODIFIER))
                hitChance += npc.getTemporaryModifiersManager().getModifier(Key.HIT_CHANCE_MODIFIER);
            if (Math.random() * 100 > hitChance)
                return 0;
        }

        double damageModifier = dmgModifier;
        if (npc.getTemporaryModifiersManager().hasActiveModifier(Key.DAMAGE_DEALT_MODIFIER))
            damageModifier += npc.getTemporaryModifiersManager().getModifier(Key.DAMAGE_DEALT_MODIFIER);
        if (target.getTemporaryModifiersManager().hasActiveModifier(Key.DAMAGE_RECIEVED_MODIFIER))
            damageModifier += target.getTemporaryModifiersManager().getModifier(Key.DAMAGE_RECIEVED_MODIFIER);
        damageModifier -= Combat.getDamageDebuff(npc, attackStyle);
        if (Settings.RS2_COMBAT) {
            /*
             * Keep the scripted maxHit value (so boss/NPC tuning is unchanged)
             * but use the pre-EOC 0..max uniform distribution for the rolled
             * damage. Accuracy was already rolled above, so cannotMiss=true
             * skips the duplicate roll inside rollNpcScriptedHit.
             */
            return Rs2AtaraxiaNumerics.rollNpcScriptedHit(
                    npc, target, maxHit, attackStyle, damageModifier, true);
        }
        maxHit *= damageModifier;
        double r = ThreadLocalRandom.current().nextDouble(0.3, 1.01);
        int damage = (int) ((double) maxHit * r);
//        System.out.println(maxHit);
//        System.out.println(damage);
        return damage;
    }

    public static Hit getRangeHit(NPC npc, int damage) {
        return new Hit(npc, damage, HitLook.RANGE_DAMAGE);
    }

    public static Hit getRegularHit(NPC npc, int damage) {
        return new Hit(npc, damage, HitLook.REGULAR_DAMAGE);
    }

    /*
     * Returns Move Delay
     */
    public abstract int attack(NPC npc, Entity target);

    /*
     * Returns ids and names
     */
    public abstract Object[] getKeys();

    /**
     * Sends sound.
     *
     * @param soundId the Sound ID.
     * @param player The player.
     * @param target The target.
     */
    public void playSound(int soundId, Player player, Entity target) {
        if (soundId == -1)
            return;
        player.getPackets().sendSound(soundId, 0, 1);
        if (target instanceof Player) {
            Player p2 = (Player) target;
            p2.getPackets().sendSound(soundId, 0, 1);
        }
    }

    public static int getMaxHit(NPC npc, int maxHit, int attackType, Entity target) {
        return CombatScript.getRandomMaxHit(npc, maxHit, attackType, target);
    }
}