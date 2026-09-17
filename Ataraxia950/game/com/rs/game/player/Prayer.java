package com.rs.game.player;

import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.game.*;
import com.rs.game.Hit.HitLook;
import com.rs.game.activites.clanwars.ClanWars;
import com.rs.game.activites.clanwars.ClanWars.Rules;
import com.rs.game.npc.NPC;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.Utils;
import io.netty.util.internal.ThreadLocalRandom;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Prayer implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2082861520556582824L;

    public static final boolean NORMAL_PRAYERS = false;
    public static final boolean ANCIENT_CURSES = true;

    private final static int[][][] closePrayers = { { // normal prayer book
            { 0 }, // defence rating, 0
            { 1, 3, 5 }, // strength, 1
            { 2, 4, 6 }, // attack rating, 2
            { 17, 19, 20, 21 }, // all rating / str / def, 3
            { 10 }, // summon prayer, 4
            { 11, 12, 13 }, // protect prayers, 5
            { 14, 15, 16 }, // other headicon prayers, 6
            { 8, 18 } // recover hp prayers, 7
    }, { // ancient prayer book
            { 1, 2, 3, 4, 5, 6, 7, 8 }, // saps, 0
            { 14, 15, 16, 17, 18, 19, 22, 23, 24 }, // leeches, 1
            { 32, 33, 34, 31, 35, 36, 37 }, // turmoils,
            // 2
            { 10 }, // summon prayer, 3
            { 11, 12, 13 }, // protect prayers, 4
            { 27, 30 }, // other headicon prayers, 5
            { 21, 20 }, // dark & light
            // forms,6
            { 26, 28 }, // head icons & buffs 7
    } };

    private final static double[][] prayerDrainRate = { { -1, -1, -1, -1, -1, -1, -1, 6, 6, 6, 0.4, 0.4, 0.4, 0.4, 0.4, 0.4, 0.3, 0.4, 0.6, 0.3, 0.3, 0.3 }, { 6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 0.6, 1.2, 0.4, 0.4, 0.4, 0.4, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.2, 0.2, 0.3, 0.3, 0.3, 0.6, 0.2, 0.3, 0.2, 0.6, 0.2, 0.2, 0.2, 0.2, 0.2, 0.15, 0.15, 0.15 } };

    public static double prayerDrainRateMod = 1.50;

    private transient Player player;
    private transient boolean[][] onPrayers;
    //    private transient boolean usingQuickPrayer;
    private transient int onPrayersCount;

    private boolean[][] quickPrayers;
    private int prayerpoints;
    private boolean ancientcurses;
    private transient long[] nextDrain;
    private transient boolean prayerBookBefore;

    public static final int MELEE_LEVEL = 0, MELEE_DAMAGE = 1, RANGE_LEVEL = 2, RANGE_DAMAGE = 3, MAGIC_LEVEL = 4, MAGIC_DAMAGE = 5, DEFENCE_LEVEL = 6;
    private transient int[][] buffs;

    public void adjustStat() {
        int meleeLevel = buffs[0][MELEE_LEVEL] + buffs[1][MELEE_LEVEL];
        int meleeDamage = buffs[0][MELEE_DAMAGE] + buffs[1][MELEE_DAMAGE];
        int rangeLevel = buffs[0][RANGE_LEVEL] + buffs[1][RANGE_LEVEL];
        int rangeDamage = buffs[0][RANGE_DAMAGE] + buffs[1][RANGE_DAMAGE];
        int magiclevel = buffs[0][MAGIC_LEVEL] + buffs[1][MAGIC_LEVEL];
        int magicDamage = buffs[0][MAGIC_DAMAGE] + buffs[1][MAGIC_DAMAGE];
        int defenceLevel = buffs[0][DEFENCE_LEVEL] + buffs[1][DEFENCE_LEVEL];
        player.getPackets().sendConfig(1029, meleeLevel);
        player.getPackets().sendConfig(1030, meleeDamage);
        player.getPackets().sendConfig(1031, rangeLevel);
        player.getPackets().sendConfig(1032, rangeDamage);
        player.getPackets().sendConfig(1033, magiclevel);
        player.getPackets().sendConfig(1034, magicDamage);
        player.getPackets().sendConfig(1035, defenceLevel);
        if (!hasPrayersOn())
            player.getPackets().sendConfigByFile(38915, 0);
    }

    public void refreshStaticPrayerBuffs() {
        buffs[0][MELEE_LEVEL] = usingPrayer(0, 2) ? getBuffByLevel() : usingPrayer(0, 17) ? 7 : usingPrayer(0, 19) ? 8 : usingPrayer(1, 14) ? 2 : usingPrayer(1, 32) ? 10 : usingPrayer(1, 35) ? 12 : 0;
        buffs[0][MELEE_DAMAGE] = usingPrayer(0, 1) ? getBuffByLevel() : usingPrayer(0, 17) ? 7 : usingPrayer(0, 19) ? 8 : usingPrayer(1, 22) ? 2 : usingPrayer(1, 32) ? 10 : usingPrayer(1, 35) ? 12 : 0;
        buffs[0][RANGE_LEVEL] = usingPrayer(0, 4) ? getBuffByLevel() : usingPrayer(0, 20) ? 8 : usingPrayer(1, 15) ? 2 : usingPrayer(1, 33) ? 10 : usingPrayer(1, 36) ? 12 : 0;
        buffs[0][RANGE_DAMAGE] = usingPrayer(0, 3) ? getBuffByLevel() : usingPrayer(0, 20) ? 8 : usingPrayer(1, 16) ? 2 : usingPrayer(1, 33) ? 10 : usingPrayer(1, 36) ? 12 : 0;
        buffs[0][MAGIC_LEVEL] = usingPrayer(0, 6) ? getBuffByLevel() : usingPrayer(0, 21) ? 8 : usingPrayer(1, 17) ? 2 : usingPrayer(1, 34) ? 10 : usingPrayer(1, 37) ? 12 : 0;
        buffs[0][MAGIC_DAMAGE] = usingPrayer(0, 5) ? getBuffByLevel() : usingPrayer(0, 21) ? 8 : usingPrayer(1, 18) ? 2 : usingPrayer(1, 34) ? 10 : usingPrayer(1, 37) ? 12 : 0;
        buffs[0][DEFENCE_LEVEL] = usingPrayer(0, 0) ? getBuffByLevel() : usingPrayer(0, 17) ? 7 : usingPrayer(0, 19) || usingPrayer(0, 20) || usingPrayer(0, 21) ? 8 : usingPrayer(1, 19) ? 2 : (usingPrayer(1, 32) || usingPrayer(1, 33) || usingPrayer(1, 34)) ? 10 : (usingPrayer(1, 35) || usingPrayer(1, 36) || usingPrayer(1, 37)) ? 12 : 0;
        adjustStat();
    }

    public int getBuffByLevel() {
        int level = player.getSkills().getLevelForXp(Skills.PRAYER);
        return level >= 28 ? 6 : level >= 10 ? 4 : 2;
    }

    public double getDrainRateByLevel() {
        int level = player.getSkills().getLevelForXp(Skills.PRAYER);
        return level >= 28 ? 0.6 : level >= 10 ? 1.5 : 6;
    }

    public void handleOutgoingHit(Hit hit, Entity target) {
        if (hit.getDamage() == 0 || (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE))
            return;
        if (usingPrayer(1, 30)) { // soul split
            World.sendProjectile(player, target, 2263, 11, 11, 20, 5, 0, 0);
            int damage = hit.getDamage() * 10;
            int amountToHeal = 0;
            for (int i = 0; i < 3; i++) {
                amountToHeal += (int) ((double) (i <= 1 ? (damage >= 2000 ? 2000 : damage) : (damage)) * (i == 2 ? 0.0125 : i == 1 ? 0.05 : 0.10) * 1.5);
                damage = damage - 2000 <= 0 ? 0 : damage - 2000;
                if (damage == 0)
                    break;
            }
            amountToHeal = (int) Math.ceil((double) amountToHeal / 10.00);
            /** Amulet of Souls **/
            if ((player.getEquipment().getAmuletId() == 31875 || player.getEquipment().getAmuletId() == 48484) && Utils.random(2) == 0)
                amountToHeal = amountToHeal + (int) Math.ceil((double) amountToHeal * ThreadLocalRandom.current().nextDouble(0.25, 0.5001));
            if (player.getPerkManager().hasPerkActive(DonationPerk.SOUL_SIPHONER))
                amountToHeal += Math.ceil((double) amountToHeal * 0.25d);
            player.heal(amountToHeal);
            if (target instanceof Player)
                ((Player) target).getPrayer().drainPrayer((int) Math.ceil((double) hit.getDamage() / 45.00));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    target.setNextGraphics(new Graphics(2264));
                    if (hit.getDamage() > 0) {
                        World.sendProjectile(target, player, 2263, 11, 11, 20, 5, 0, 0);
                    }
                }
            }, 1);
        }
        if (usingPrayer(0, 16) && target instanceof Player) { // smite
            final int drain = hit.getDamage() / 4;
            if (drain > 0) {
                ((Player) target).getPrayer().drainPrayer(drain);
            }
        }
        int increment = 1;
        Perk flanking = player.getInventionManager().hasPerk(Perks.FLANKING);
        if (flanking != null)
            increment = 2;
        if (hit.getLook() == HitLook.MELEE_DAMAGE) {
            if (usingPrayer(1, 1)) { // sap warrior
                boolean increased = target.increasePrayerDebuff(player, MELEE_LEVEL, 2, 6);
                if (increased) {
                    player.setNextAnimation(new Animation(12569));
                    player.setNextGraphics(new Graphics(2214));
                    World.sendProjectile(player, target, 2215, 35, 35, 20, 5, 0, 0);
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            target.setNextGraphics(new Graphics(2216));
                        }
                    }, 1);
                }
            }
            if (usingPrayer(1, 8)) { // sap strength
                boolean increased = target.increasePrayerDebuff(player, MELEE_DAMAGE, target instanceof NPC ? 3 : 2, target instanceof NPC ? 9 : 6);
                if (increased) {
                    player.setNextAnimation(new Animation(12569));
                    player.setNextGraphics(new Graphics(2214));
                    World.sendProjectile(player, target, 2215, 35, 35, 20, 5, 0, 0);
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            target.setNextGraphics(new Graphics(2216));
                        }
                    }, 1);
                }
            }
            if (usingPrayer(1, 14)) {// leech attack
                boolean doAnimation = buffs[1][MELEE_LEVEL] + increment <= 3;
                target.increasePrayerDebuff(player, MELEE_LEVEL, 4, 8);
                if (doAnimation) {
                    player.setNextAnimation(new Animation(12575));
                    World.sendProjectile(player, target, 2231, 35, 35, 20, 5, 0, 0);
                    buffs[1][MELEE_LEVEL] = buffs[1][MELEE_LEVEL] + increment > 3 ? 3 : buffs[1][MELEE_LEVEL] + increment;
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            target.setNextGraphics(new Graphics(2232));
                        }
                    }, 1);
                }
            }
            if (usingPrayer(1, 22)) {// leech strength
                boolean doAnimation = buffs[1][MELEE_DAMAGE] + increment <= 6;
                int minValue = target instanceof NPC ? 6 : 4;
                int maxValue = target instanceof NPC ? 12 : 8;
                target.increasePrayerDebuff(player, MELEE_DAMAGE, minValue, maxValue);
                if (doAnimation) {
                    player.setNextAnimation(new Animation(12575));
                    World.sendProjectile(player, target, 2231, 35, 35, 20, 5, 0, 0);
                    buffs[1][MELEE_DAMAGE] = buffs[1][MELEE_DAMAGE] + increment > 6 ? 6 : buffs[1][MELEE_DAMAGE] + increment;
                    WorldTasksManager.schedule(new WorldTask() {

                        @Override
                        public void run() {
                            target.setNextGraphics(new Graphics(2232));
                        }
                    }, 1);
                }
            }
            if (usingPrayer(1, 32) || usingPrayer(1, 35)) {// turmoil / malev
                target.increasePrayerDebuff(player, MELEE_LEVEL, 6, 10);
                target.increasePrayerDebuff(player, MELEE_DAMAGE, target instanceof NPC ? 9 : 6, target instanceof NPC ? 15 : 10);
                target.increasePrayerDebuff(player, DEFENCE_LEVEL, 6, 10);
                buffs[1][MELEE_LEVEL] = buffs[1][MELEE_LEVEL] + increment > 10 ? 10 : buffs[1][MELEE_LEVEL] == 0 ? 6 : buffs[1][MELEE_LEVEL] + increment;
                buffs[1][DEFENCE_LEVEL] = buffs[1][DEFENCE_LEVEL] + increment > 10 ? 10 : buffs[1][DEFENCE_LEVEL] == 0 ? 6 : buffs[1][DEFENCE_LEVEL] + increment;
                buffs[1][MELEE_DAMAGE] = buffs[1][MELEE_DAMAGE] + increment > (target instanceof NPC ? 15 : 10) ? (target instanceof NPC ? 15 : 10) : buffs[1][MELEE_DAMAGE] == 0 ? (target instanceof NPC ? 9 : 6) : buffs[1][MELEE_DAMAGE] + increment;
            }
        } else if (hit.getLook() == HitLook.RANGE_DAMAGE) {
            if (usingPrayer(1, 2)) {// sap range
                boolean increased = target.increasePrayerDebuff(player, RANGE_LEVEL, 2, 6);
                if (increased) {
                    player.setNextAnimation(new Animation(12569));
                    player.setNextGraphics(new Graphics(2217));
                    World.sendProjectile(player, target, 2218, 35, 35, 20, 5, 0, 0);
                    WorldTasksManager.schedule(new WorldTask() {

                        @Override
                        public void run() {
                            target.setNextGraphics(new Graphics(2219));
                        }
                    }, 1);
                }
            }
            if (usingPrayer(1, 3)) {// sap range str
                boolean increased = target.increasePrayerDebuff(player, RANGE_DAMAGE, target instanceof NPC ? 3 : 2, target instanceof NPC ? 9 : 6);
                if (increased) {
                    player.setNextAnimation(new Animation(12569));
                    player.setNextGraphics(new Graphics(2217));
                    World.sendProjectile(player, target, 2218, 35, 35, 20, 5, 0, 0);
                    WorldTasksManager.schedule(new WorldTask() {

                        @Override
                        public void run() {
                            target.setNextGraphics(new Graphics(2219));
                        }
                    }, 1);
                }
            }
            if (usingPrayer(1, 15)) {// leech range
                target.increasePrayerDebuff(player, RANGE_LEVEL, 4, 8);
                boolean doAnimation = buffs[1][RANGE_DAMAGE] + increment <= 6 || buffs[1][RANGE_LEVEL] + increment <= 3;
                if (doAnimation) {
                    player.setNextAnimation(new Animation(12575));
                    World.sendProjectile(player, target, 2236, 35, 35, 20, 5, 0, 0);
                    buffs[1][RANGE_LEVEL] = buffs[1][RANGE_LEVEL] + increment > 3 ? 3 : buffs[1][RANGE_LEVEL] + increment;
                    buffs[1][RANGE_DAMAGE] = buffs[1][RANGE_DAMAGE] + increment > 6 ? 6 : buffs[1][RANGE_DAMAGE] + increment;
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            target.setNextGraphics(new Graphics(2238));
                        }
                    });
                }
            }
            if (usingPrayer(1, 16)) {// leech range str
                target.increasePrayerDebuff(player, RANGE_DAMAGE, target instanceof NPC ? 6 : 4, target instanceof NPC ? 12 : 8);
                boolean doAnimation = buffs[1][RANGE_DAMAGE] + increment <= 6 || buffs[1][RANGE_LEVEL] + increment <= 3;
                if (doAnimation) {
                    player.setNextAnimation(new Animation(12575));
                    World.sendProjectile(player, target, 2236, 35, 35, 20, 5, 0, 0);
                    buffs[1][RANGE_LEVEL] = buffs[1][RANGE_LEVEL] + increment > 3 ? 3 : buffs[1][RANGE_LEVEL] + increment;
                    buffs[1][RANGE_DAMAGE] = buffs[1][RANGE_DAMAGE] + increment > 6 ? 6 : buffs[1][RANGE_DAMAGE] + increment;
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            target.setNextGraphics(new Graphics(2238));
                        }
                    });
                }
            }
            if (usingPrayer(1, 33) || usingPrayer(1, 36)) {// anguish / desolation
                target.increasePrayerDebuff(player, RANGE_LEVEL, 6, 10);
                target.increasePrayerDebuff(player, RANGE_DAMAGE, target instanceof NPC ? 9 : 6, target instanceof NPC ? 15 : 10);
                target.increasePrayerDebuff(player, DEFENCE_LEVEL, 6, 10);
                buffs[1][RANGE_LEVEL] = buffs[1][RANGE_LEVEL] + increment > 10 ? 10 : buffs[1][RANGE_LEVEL] == 0 ? 6 : buffs[1][RANGE_LEVEL] + increment;
                buffs[1][DEFENCE_LEVEL] = buffs[1][DEFENCE_LEVEL] + increment > 10 ? 10 : buffs[1][DEFENCE_LEVEL] == 0 ? 6 : buffs[1][DEFENCE_LEVEL] + increment;
                buffs[1][RANGE_DAMAGE] = buffs[1][RANGE_DAMAGE] + increment > (target instanceof NPC ? 15 : 10) ? (target instanceof NPC ? 15 : 10) : buffs[1][RANGE_DAMAGE] == 0 ? (target instanceof NPC ? 9 : 6) : buffs[1][RANGE_DAMAGE] + increment;
            }
        } else if (hit.getLook() == HitLook.MAGIC_DAMAGE) {
                if (usingPrayer(1, 4)) { // sap mage
                    boolean increased = target.increasePrayerDebuff(player, MAGIC_LEVEL, 2, 6);
                    mageDefectMessage(increased);
                    if (increased) {
                        player.setNextAnimation(new Animation(12569));
                        player.setNextGraphics(new Graphics(2220));
                        World.sendProjectile(player, target, 2221, 35, 35, 20, 5, 0, 0);
                        WorldTasksManager.schedule(new WorldTask() {

                            @Override
                            public void run() {
                                target.setNextGraphics(new Graphics(2222));
                            }
                        }, 1);
                    }
                }
            if (usingPrayer(1, 5)) { // sap mage str
                boolean increased = target.increasePrayerDebuff(player, MAGIC_DAMAGE, target instanceof NPC ? 3 : 2, target instanceof NPC ? 9 : 6);
                mageDefectMessage(increased);
                if (increased) {
                    player.setNextAnimation(new Animation(12569));
                    player.setNextGraphics(new Graphics(2220));
                    World.sendProjectile(player, target, 2221, 35, 35, 20, 5, 0, 0);
                    WorldTasksManager.schedule(new WorldTask() {

                        @Override
                        public void run() {
                            target.setNextGraphics(new Graphics(2222));
                        }
                    }, 1);
                }
            }
            if (usingPrayer(1, 17)) {// leech mage
                boolean increased = target.increasePrayerDebuff(player, MAGIC_LEVEL, 4, 8);
                mageDefectMessage(increased);
                boolean doAnimation = buffs[1][MAGIC_DAMAGE] + increment <= 6 || buffs[1][MAGIC_LEVEL] + increment <= 3;
                if (doAnimation) {
                    player.setNextAnimation(new Animation(12575));
                    World.sendProjectile(player, target, 2240, 35, 35, 20, 5, 0, 0);
                    buffs[1][MAGIC_LEVEL] = buffs[1][MAGIC_LEVEL] + increment > 3 ? 3 : buffs[1][MAGIC_LEVEL] + increment;
                    buffs[1][MAGIC_DAMAGE] = buffs[1][MAGIC_DAMAGE] + increment > 6 ? 6 : buffs[1][MAGIC_DAMAGE] + increment;
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            target.setNextGraphics(new Graphics(2242));
                        }
                    }, 1);
                }
            }
            if (usingPrayer(1, 18)) {// leech mage str
                boolean increased = target.increasePrayerDebuff(player, MAGIC_DAMAGE, target instanceof NPC ? 6 : 4, target instanceof NPC ? 12 : 8);
                mageDefectMessage(increased);
                boolean doAnimation = buffs[1][MAGIC_DAMAGE] + increment <= 6 || buffs[1][MAGIC_LEVEL] + increment <= 3;
                if (doAnimation) {
                    player.setNextAnimation(new Animation(12575));
                    World.sendProjectile(player, target, 2240, 35, 35, 20, 5, 0, 0);
                    buffs[1][MAGIC_LEVEL] = buffs[1][MAGIC_LEVEL] + increment > 3 ? 3 : buffs[1][MAGIC_LEVEL] + increment;
                    buffs[1][MAGIC_DAMAGE] = buffs[1][MAGIC_DAMAGE] + increment > 6 ? 6 : buffs[1][MAGIC_DAMAGE] + increment;
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            target.setNextGraphics(new Graphics(2242));
                        }
                    }, 1);
                }
            }
            if (usingPrayer(1, 34) || usingPrayer(1, 37)) {// torment / affliction
                boolean increased = target.increasePrayerDebuff(player, MAGIC_LEVEL, 6, 10) || target.increasePrayerDebuff(player, MAGIC_DAMAGE, target instanceof NPC ? 9 : 6, target instanceof NPC ? 15 : 10) || target.increasePrayerDebuff(player, DEFENCE_LEVEL, 6, 10);
                buffs[1][MAGIC_LEVEL] = buffs[1][MAGIC_LEVEL] + increment > 10 ? 10 : buffs[1][MAGIC_LEVEL] == 0 ? 6 : buffs[1][MAGIC_LEVEL] + increment;
                buffs[1][DEFENCE_LEVEL] = buffs[1][DEFENCE_LEVEL] + increment > 10 ? 10 : buffs[1][DEFENCE_LEVEL] == 0 ? 6 : buffs[1][DEFENCE_LEVEL] + increment;
                buffs[1][MAGIC_DAMAGE] = buffs[1][MAGIC_DAMAGE] + increment > (target instanceof NPC ? 15 : 10) ? (target instanceof NPC ? 15 : 10) : buffs[1][MAGIC_DAMAGE] == 0 ? (target instanceof NPC ? 9 : 6) : buffs[1][MAGIC_DAMAGE] + increment;
                mageDefectMessage(increased);
            }
        }
        if (usingPrayer(1, 7)) { // sap defence
            boolean increased = target.increasePrayerDebuff(player, DEFENCE_LEVEL, 2, 6);
            if (increased) {
                player.setNextAnimation(new Animation(12569));
                player.setNextGraphics(new Graphics(2214));
                World.sendProjectile(player, target, 2215, 35, 35, 20, 5, 0, 0);
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        target.setNextGraphics(new Graphics(2216));
                    }
                }, 1);
            }
        }
        if (usingPrayer(1, 19)) { // leech defence
            boolean increased = target.increasePrayerDebuff(player, DEFENCE_LEVEL, 4, 8);
            player.getPackets().sendGameMessage(increased ? "Your curse drains defence from the enemy." : "Your opponent has been weakened so much that your curse has no effect.", true);
            boolean doAnimation = buffs[1][DEFENCE_LEVEL] + increment <= 3;
            if (doAnimation) {
                player.setNextAnimation(new Animation(12575));
                World.sendProjectile(player, target, 2244, 35, 35, 20, 5, 0, 0);
                buffs[1][DEFENCE_LEVEL] = buffs[1][DEFENCE_LEVEL] + increment > 3 ? 3 : buffs[1][DEFENCE_LEVEL] + increment;
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        target.setNextGraphics(new Graphics(2246));
                    }
                }, 1);
            }
        }
        if (usingPrayer(1, 23) && target instanceof Player && Math.random() <= 0.3) {// leech energy
            if (((Player) target).getRunEnergy() <= 0) {
                player.getPackets().sendGameMessage("Your opponent has been weakened so much that your leech curse has no effect.", true);
            } else {
                player.setRunEnergy(player.getRunEnergy() > 90 ? 100 : player.getRunEnergy() + 10);
                ((Player) target).setRunEnergy(player.getRunEnergy() > 10 ? ((Player) target).getRunEnergy() - 10 : 0);
            }
            player.setNextAnimation(new Animation(12575));
            World.sendProjectile(player, target, 2256, 35, 35, 20, 5, 0, 0);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    target.setNextGraphics(new Graphics(2258));
                }
            }, 1);
        }
        if (usingPrayer(1, 24) && target instanceof Player && Math.random() <= 0.3) {// leech special attack
            if (!(((Player) target).getCombatDefinitions().getSpecialAttackPercentage() <= 0)) {
                player.getCombatDefinitions().restoreSpecialAttack();
                ((Player) target).getCombatDefinitions().decreaseSpecialAttack(10);
            }
            player.setNextAnimation(new Animation(12575));
            World.sendProjectile(player, target, 2252, 35, 35, 20, 5, 0, 0);
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    target.setNextGraphics(new Graphics(2254));
                }
            }, 1);
        }
        if (usingPrayer(1, 6) && target instanceof Player && Math.random() <= 0.3) { // sap spec
            player.setNextAnimation(new Animation(12569));
            player.setNextGraphics(new Graphics(2223));
            if (!(((Player) target).getCombatDefinitions().getSpecialAttackPercentage() <= 0)) {
                ((Player) target).getCombatDefinitions().decreaseSpecialAttack(10);
            }
            World.sendProjectile(player, target, 2224, 35, 35, 20, 5, 0, 0);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    target.setNextGraphics(new Graphics(2225));
                }
            }, 1);
        }

        if ((usingPrayer(1, 6) || usingPrayer(1, 24)) && target instanceof Player && Math.random() <= 0.3) {
            player.getPackets().sendGameMessage("Your opponent has been weakened so much that your sap curse has no effect.", true);
        }
        adjustStat();
    }

    public void closePrayers(int prayerId) {
        if (ancientcurses) {
            if (prayerId == 10) {
                if (buffs[1][MELEE_LEVEL] > 0)
                    player.getPackets().sendGameMessage("Your Attack is now unaffected by sap and leech curses.", true);
                buffs[1][MELEE_LEVEL] = 0;
            } else if (prayerId == 11) {
                if (buffs[1][RANGE_LEVEL] > 0 || buffs[1][RANGE_DAMAGE] > 0)
                    player.getPackets().sendGameMessage("Your Ranged is now unaffected by sap and leech curses.", true);
                buffs[1][RANGE_LEVEL] = 0;
                buffs[1][RANGE_DAMAGE] = 0;
            } else if (prayerId == 12) {
                if (buffs[1][MAGIC_LEVEL] > 0 || buffs[1][MAGIC_DAMAGE] > 0)
                    player.getPackets().sendGameMessage("Your Magic is now unaffected by sap and leech curses.", true);
                buffs[1][MAGIC_LEVEL] = 0;
                buffs[1][MAGIC_DAMAGE] = 0;
            } else if (prayerId == 13) {
                if (buffs[1][DEFENCE_LEVEL] > 0)
                    player.getPackets().sendGameMessage("Your Defence is now unaffected by sap and leech curses.", true);
                buffs[1][DEFENCE_LEVEL] = 0;
            } else if (prayerId == 14) {
                if (buffs[1][MELEE_DAMAGE] > 0)
                    player.getPackets().sendGameMessage("Your Melee Damage is now unaffected by sap and leech curses.", true);
                buffs[1][MELEE_DAMAGE] = 0;
            } else if (prayerId == 19 || prayerId == 22) {
                buffs[1][MELEE_LEVEL] = 0;
                buffs[1][MELEE_DAMAGE] = 0;
                buffs[1][DEFENCE_LEVEL] = 0;
            } else if (prayerId == 20 || prayerId == 23) {
                buffs[1][RANGE_LEVEL] = 0;
                buffs[1][RANGE_DAMAGE] = 0;
                buffs[1][DEFENCE_LEVEL] = 0;
            } else if (prayerId == 21 || prayerId == 24) {
                buffs[1][MAGIC_LEVEL] = 0;
                buffs[1][MAGIC_DAMAGE] = 0;
                buffs[1][DEFENCE_LEVEL] = 0;
            }
        }
        adjustStat();
    }

    public int getPrayerHeadIcon() {
        if (onPrayersCount == 0)
            return -1;
        int value = -1;
        if (!ancientcurses) {
            if (usingPrayer(0, 10))
                value += 8;
            if (usingPrayer(0, 11))
                value += 3;
            else if (usingPrayer(0, 12))
                value += 2;
            else if (usingPrayer(0, 13))
                value += 1;
            else if (usingPrayer(0, 14))
                value += 4;
            else if (usingPrayer(0, 15))
                value += 6;
            else if (usingPrayer(0, 16))
                value += 5;
        } else {
            if (usingPrayer(1, 10)) {
                value += 16;
                if (usingPrayer(1, 12))
                    value += 2;
                else if (usingPrayer(1, 11))
                    value += 3;
                else if (usingPrayer(1, 13))
                    value += 1;
            } else if (usingPrayer(1, 11))
                value += 14;
            else if (usingPrayer(1, 12))
                value += 15;
            else if (usingPrayer(1, 13))
                value += 13;
            else if (usingPrayer(1, 27))
                value += 20;
            else if (usingPrayer(1, 30))
                value += 21;
            else if (usingPrayer(1, 26))
                value += 33;
            else if (usingPrayer(1, 28))
                value += 34;
        }
        return value;
    }

    public void mageDefectMessage(boolean increased) {
        if (usingPrayer(1, 4) || usingPrayer(1, 17) || usingPrayer(1, 5) || usingPrayer(1, 18) || usingPrayer(1, 18) || usingPrayer(1, 34) || usingPrayer(1, 37)) {
            player.getPackets().sendGameMessage(increased ? "Your curse drains mage level, mage damage, and defence from the enemy." : "Your opponent has been weakened so much that your curse has no effect.", true);
        }
    }

    @Getter
    private transient boolean quickPrayerOn;

    public void handlePrayerOrbOption(int packetId) {
        if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
            delaySwitchQuickPrayers();
        } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
            openPrayerPresetsInterface();
        } else {
            int clickedIndex = packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON4_PACKET ? 1 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 2 : packetId == PacketRepository.ACTION_BUTTON9_PACKET ? 3 : packetId == PacketRepository.ACTION_BUTTON6_PACKET ? 4 : packetId == PacketRepository.ACTION_BUTTON7_PACKET ? 5 : packetId == PacketRepository.ACTION_BUTTON10_PACKET ? 6 : 7;
            delaySwitchQuickPrayers(clickedIndex);
        }
    }

    public void delaySwitchQuickPrayers() {
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                if (player.isDead() || player.isLocked())
                    return;
                switchQuickPrayers();
            }
        });
    }

    public void delaySwitchQuickPrayers(int presetIndex) {
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                if (player.isDead() || player.isLocked())
                    return;
                switchQuickPrayers(presetIndex);
            }
        });
    }

    public void switchQuickPrayers() {
        switchQuickPrayers(leftClickOption == -1337 ? lastUsed : leftClickOption);
    }

    public void switchQuickPrayers(int quickPrayerPresetIndex) {
        if (prayerPresets.isEmpty()) {
            player.getPackets().sendMainInterfaceMessage(1, "You must create a prayer preset before you can use quick prayers.", true);
            return;
        }
        if (quickPrayerPresetIndex >= prayerPresets.size()) {
            player.getPackets().sendMainInterfaceMessage(1, "Quick preset " + (quickPrayerPresetIndex + 1) + " does not exist.", true);
            return;
        }
        QuickPrayerPrest preset = prayerPresets.get(quickPrayerPresetIndex);
        boolean[] onPrayers = preset.getOnPrayers();
        if (ancientcurses != preset.isAncientcurses()) {
            player.getPackets().sendMainInterfaceMessage(1, "You need to be on " + (preset.isAncientcurses() ? "ancient curses " : "normal ") + " prayer book to use this preset.", true);
            return;
        }
        boolean hasQuickPrayers = false;
        for (boolean prayer : onPrayers) {
            if (prayer) {
                hasQuickPrayers = true;
                break;
            }
        }
        if (!hasQuickPrayers) {
            player.getPackets().sendMainInterfaceMessage(1, "You don't have any quick prayers selected.<br>Right-click the prayer button on the action bar to select some.", true);
            return;
        }
        if (!checkPrayer())
            return;
        if (player.getCurrentFriendChat() != null) {
            ClanWars war = player.getCurrentFriendChat().getClanWars();
            if (war != null && war.get(Rules.NO_PRAYER) && (war.getFirstPlayers().contains(player) || war.getSecondPlayers().contains(player))) {
                player.getPackets().sendGameMessage("Prayer has been disabled during this war.");
                return;
            }
        }
        boolean willBeOn = lastUsed != quickPrayerPresetIndex || !quickPrayerOn;
        lastUsed = quickPrayerPresetIndex;
        quickPrayerOn = willBeOn;
        if (hasPrayersOn())
            closeAllPrayers(false);
        quickPrayerOn = willBeOn;
        if (quickPrayerOn) {
            int index = 0;
            for (boolean prayer : onPrayers) {
                if (prayer)
                    usePrayer(index, false);
                index++;
            }
            player.getPackets().sendGlobalConfig(182, 1);
            recalculatePrayer();
        }
    }

    private void closePrayers(int[]... prayers) {
        closePrayers(false, prayers);
    }

    private void closePrayers(boolean usingQuickPrayer, int[]... prayers) {
        if (prayerPresets.isEmpty())
            usingQuickPrayer = false;
        QuickPrayerPrest preset = !usingQuickPrayer ? null : prayerPresets.get(selectedPresetIndex);
        usingQuickPrayer = preset != null;
        for (int[] prayer : prayers)
            for (int prayerId : prayer)
                if (usingQuickPrayer)
                    preset.onPrayers[prayerId] = false;
                else {
                    if (onPrayers[getPrayerBook()][prayerId])
                        onPrayersCount--;
                    onPrayers[getPrayerBook()][prayerId] = false;
                    int mapId = getPrayerMap(prayerId, getPrayerBook() == 1).getId();
                    player.getPackets().sendExecuteScript(10624, mapId, 0);
                    closePrayers(prayerId);
                }
        if (!usingQuickPrayer)
            player.getAppearence().generateIconsData();
    }

    public void delayUsePrayer(final int prayerId, final boolean usingQuickPrayer) {
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                if (player.isDead() || player.isLocked())
                    return;
                switchPrayer(prayerId, usingQuickPrayer);
            }
        });
    }

    public void switchPrayer(int prayerId, boolean usingQuickPrayer) {
        if (!usingQuickPrayer && !checkPrayer())
            return;
        usePrayer(prayerId, usingQuickPrayer);
        if (usingQuickPrayer)
            refreshPresetPrayers();
        else
            recalculatePrayer();
    }

    private boolean usePrayer(int prayerId, boolean usingQuickPrayer) {
        if (prayerPresets.isEmpty())
            usingQuickPrayer = false;
        if (!ancientcurses) {
            if (prayerId == 3 || prayerId == 4)
                prayerId = prayerId == 3 ? 4 : 3;
            if (prayerId == 5 || prayerId == 6)
                prayerId = prayerId == 5 ? 6 : 5;
        }
        QuickPrayerPrest preset = !usingQuickPrayer ? null : prayerPresets.get(selectedPresetIndex);
        usingQuickPrayer = preset != null;
        if (prayerId < 0 || prayerId >= ClientScriptMap.getMap((usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()) == 0 ? 6759 : 6760).getSize())
            return false;
        Perk antitheism = player.getInventionManager().hasPerk(Perks.ANTITHEISM);
        if (antitheism != null && (prayerId == 11 || prayerId == 12 || prayerId == 13 || prayerId == 10)) {
            player.getPackets().sendGameMessage("The Antitheism perk prevents you from using that " + ((usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()) == 0 ? "prayer" : "curse") + ".");
            return false;
        }
        if (prayerId == 35 || prayerId == 36 || prayerId == 37) {
            for (int i = 0; i < this.codexPrayers.length; i++)
                if (prayerId == (35 + i) && !codexPrayers[i]) {
                    player.getPackets().sendGameMessage("You need to unlock this prayer first.");
                    return false;
                }
        }
        if ((usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()) == 1 && (prayerId == 20 || prayerId == 21 || prayerId == 25 || prayerId == 26 || prayerId == 28 || prayerId == 29 || prayerId == 31))
            return false;
        if (!hasRequiredLevel(prayerId, (usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()) == 1))
            return false;
        boolean usingProtectionPrayers = ancientcurses && prayerId >= 10 && prayerId <= 13;
        boolean usingLmsDisabledCurses = ancientcurses && (prayerId == 27 || prayerId == 30 || prayerId >= 32);
        if (player.getPrayerDelay() >= Utils.currentTimeMillis() && usingProtectionPrayers) {
            player.getPackets().sendGameMessage("You are currently injured and cannot use protection prayers!");
            return false;
        }
        if (player.getCurrentFriendChat() != null) {
            ClanWars war = player.getCurrentFriendChat().getClanWars();
            if (war != null && war.get(Rules.NO_PRAYER) && (war.getFirstPlayers().contains(player) || war.getSecondPlayers().contains(player))) {
                player.getPackets().sendGameMessage("Prayer has been disabled during this war.");
                return false;
            }
        }
        int mapId = getPrayerMap(prayerId, (usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()) == 1).getId();
        if (!usingQuickPrayer) {
            if (onPrayers[getPrayerBook()][prayerId]) {
                onPrayers[getPrayerBook()][prayerId] = false;
                player.getPackets().sendExecuteScript(10624, mapId, 0);
                refreshStaticPrayerBuffs();
                closePrayers(prayerId);
                onPrayersCount--;
                player.getAppearence().generateIconsData();
//                player.getPackets().sendSound(2663, 0, 1);
                return true;
            }
        } else {
            if (preset.onPrayers[prayerId]) {
                preset.onPrayers[prayerId] = false;
                return true;
            }
        }
        boolean needAppearenceGenerate = false;
        if ((usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()) == 0) {
            switch (prayerId) {
                case 0:
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][3]);
                    break;
                case 1:
                case 3:
                case 5:
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][1], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][3]);
                    break;
                case 2:
                case 4:
                case 6:
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][2], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][3]);
                    break;
                case 10:
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][6]);
                    needAppearenceGenerate = true;
                    break;
                case 11:
                case 12:
                case 13:
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][5], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][6]);
                    needAppearenceGenerate = true;
                    break;
                case 14:
                case 15:
                case 16:
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][4], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][5], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][6]);
                    needAppearenceGenerate = true;
                    break;
                case 17:
                case 19:
                case 20:
                case 21:
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][0], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][1], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][2], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][3]);
                    break;
                case 8:
                case 18:
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][7]);
                    break;
                default:
                    break;
            }
        } else {
            switch (prayerId) {
                case 0:
                    if (!usingQuickPrayer) {
                        player.setNextAnimation(new Animation(12567));
                        player.setNextGraphics(new Graphics(2213));
                    }
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][1], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][2]);
                    break;
                case 9:
                    if (!usingQuickPrayer) {
                        player.setNextAnimation(new Animation(12589));
                        player.setNextGraphics(new Graphics(2266));
                    }
                    break;
                case 10:
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][5], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][7]);
                    needAppearenceGenerate = true;
                    break;
                case 11:
                case 12:
                case 13:
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][4], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][5], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][7]);
                    needAppearenceGenerate = true;
                    break;
                case 26:
                case 27:
                case 28:
                case 30:
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][3], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][4], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][5], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][7]);
                    needAppearenceGenerate = true;
                    break;
                case 14:
                case 15:
                case 16:
                case 17:
                case 18:
                case 19:
                case 22:
                case 23:
                case 24:
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][0], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][2]);
                    break;
                case 20:
                case 21:
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][6]);
                    break;
                case 29:
                    if (!usingQuickPrayer) {
                        player.setNextAnimationNoPriority(new Animation(12565));
                        player.setNextGraphics(new Graphics(2226));
                    }
                    break;
                case 31:
                case 32:
                case 33:
                case 34:
                    if (!usingQuickPrayer) {
                        player.setNextAnimationNoPriority(new Animation(12565));
                        player.setNextGraphics(new Graphics(2226));
                    }
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][0], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][1], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][2]);
                    break;
                case 35:
                case 36:
                case 37:
                    if (!usingQuickPrayer) {
                        player.setNextAnimation(new Animation(30131));
                        player.setNextGraphics(new Graphics(6528));
                    }
                    closePrayers(usingQuickPrayer, closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][0], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][1], closePrayers[usingQuickPrayer ? preset.getPrayerBook() : getPrayerBook()][2]);
                    break;
                default:
                    break;
            }
        }
        if (usingQuickPrayer) {
            preset.onPrayers[prayerId] = true;
            return true;
        }
        onPrayers[getPrayerBook()][prayerId] = true;
        player.getPackets().sendExecuteScript(10624, mapId, 1);
        refreshStaticPrayerBuffs();
        resetDrainPrayer(prayerId);
        onPrayersCount++;
        if (needAppearenceGenerate)
            player.getAppearence().generateIconsData();
        if (player.getNextAnimation() == null)
            player.setNextAnimationNoPriority(new Animation((player.getCombatDefinitions().isCombatStance() && !player.isInLegacyCombatMode()) ? 18010 : 18018));
        return true;
    }

    public void processPrayer() {
        if (!hasPrayersOn())
            return;
    }

    // 600

    public void processPrayerDrain() {
        if (infinitePrayer) return;
        if (!hasPrayersOn())
            return;
        int prayerBook = getPrayerBook();
        int drain = 0;
        int prayerPoints = player.getCombatDefinitions().getBonuses()[CombatDefinitions.PRAYER_B];
        int hatId = player.getEquipment().getHatId();
        if (hatId >= 18744 && hatId <= 18746) // hallos give hidden effect 15pray bonus
            prayerPoints += 15;
        for (int index = 0; index < onPrayers[prayerBook].length; index++) {
            if (onPrayers[prayerBook][index]) {
                long drainTimer = nextDrain[index];
                if (drainTimer != 0 && drainTimer <= Utils.currentTimeMillis()) {
                    double drainRate = prayerDrainRate[getPrayerBook()][index] == -1 ? getDrainRateByLevel() : prayerDrainRate[getPrayerBook()][index];
                    int rate = (int) (((drainRate * prayerDrainRateMod * 100000) / (double) (100 - prayerPoints)));
                    drain++;
                    nextDrain[index] = Utils.currentTimeMillis() + rate;
                }
            }
        }
        if (drain > 0) {
            player.getPackets().sendConfigByFile(38915, drain);
            drainPrayer(drain);
            if (!checkPrayer())
                closeAllPrayers();
        }
    }

    public void resetDrainPrayer(int index) {
        int prayerBonus = player.getCombatDefinitions().getBonuses()[CombatDefinitions.PRAYER_B];
        int hatId = player.getEquipment().getHatId();
        if (hatId >= 18744 && hatId <= 18746) // hallos give hidden effect
            // 15pray bonus
            prayerBonus += 15;
        double drainRate = prayerDrainRate[getPrayerBook()][index] == -1 ? getDrainRateByLevel() : prayerDrainRate[getPrayerBook()][index];
        int rate = (int) (((drainRate * prayerDrainRateMod * 100000) / (double) (100 - prayerBonus)));
        nextDrain[index] = Utils.currentTimeMillis() + rate;

    }

    public int getOnPrayersCount() {
        return onPrayersCount;
    }

    public void closeAllPrayers(boolean reset) {
        onPrayers = new boolean[][] { new boolean[22], new boolean[38] };
        buffs = new int[2][7];
        onPrayersCount = 0;
        if (reset) {
            player.getPackets().sendGlobalConfig(182, 0);
            this.quickPrayerOn = false;
        }
        disableAllPrayerMaps();
        recalculatePrayer();
        player.getAppearence().generateIconsData();
        adjustStat();
    }

    public void closeAllPrayers() {
        closeAllPrayers(true);
    }

    public boolean hasPrayersOn() {
        return onPrayersCount > 0;
    }

    private boolean checkPrayer() {
        if (prayerpoints <= 0) {
            player.getPackets().sendGameMessage("Please recharge your prayer at the Lumbridge Church.");
            return false;
        }
        return true;
    }

    private int getPrayerBook() {
        return ancientcurses == false ? 0 : 1;
    }

    private void recalculatePrayer() {
        recalculatePrayer(ancientcurses);
        adjustStat();
        if (!hasPrayersOn())
            quickPrayerOn = false;
        player.getPackets().sendConfigByFile(5941, quickPrayerOn ? 1 : 0);
        player.updateBuffs();
    }

    private static final int[][] VARBIT_SLOTS = { { 0, 1, 2, 7, 8, 9, 11, 12, 13, 14, 15, 16, 3, 5, 4, 6, 10, 17, 19, 18, 21, 20 }, { 0, 1, 2, 4, 6, 9, 10, 11, 12, 13, 14, 15, 17, 19, 22, 23, 24, 27, 30, 32, 16, 18, 33, 34, 5, 3, 8, 7, 31, 20, 21, 26, /* 5859 start */ 28, 25, 29, 35, 36, 37 } };

    private void recalculatePrayer(boolean ancientCurses) {
        boolean[] book = onPrayers[ancientCurses ? 1 : 0];
        int value = 0;
        int value2 = 0;
        for (int slot = 0; slot < book.length; slot++) {
            int index = getIndex(ancientCurses, slot);// get index in var bits.
            if (index >= 32)
                value2 |= (book[slot] ? 1 : 0) << ((index - 32) <= 2 ? (index - 32) : (index - 27));
            if (index < 32)
                value |= (book[slot] ? 1 : 0) << index;
        }
        player.getPackets().sendConfig((ancientCurses ? 3275 : 3272), value);
        player.getPackets().sendConfig(5859, value2);
    }

    private int getIndex(boolean ancientCurses, int slot) {
        for (int i = 0; i < VARBIT_SLOTS[ancientCurses ? 1 : 0].length; i++)
            if (VARBIT_SLOTS[ancientCurses ? 1 : 0][i] == slot)
                return i;
        return -1;
    }

    public void refresh() {
        player.getPackets().sendConfigByFile(16789, ancientcurses ? 1 : 0);
        unlockPrayerBookButtons(false);
    }

    public void init() {
        if (codexPrayers == null)
            codexPrayers = new boolean[3];
        if (prayerPresets == null)
            prayerPresets = new ArrayList<QuickPrayerPrest>(10);
        player.getPackets().sendConfigByFile(11105, 90);
        player.getPackets().sendConfigByFile(11319, 8);// piety, etc
//        player.getPackets().sendConfigByFile(29077, 120); // green curses
        player.getPackets().sendConfigByFile(2369, 1);// rapid ren
        refreshCodexPrayers();
        refresh();
        adjustStat();
        refreshFilter();
        refreshShowActivePrayersOnly();
        refreshCurrentPreset();
        refreshPresetsPrayers();
        refreshPresets();
    }

    public void unlockPrayerBookButtons(boolean menu) {
        player.getPackets().sendIComponentSettings(menu ? 1457 : 1458, menu ? 15 : 39, 0, 50, 12845058);
    }

    public void setPrayerBook(boolean ancientcurses) {
        closeAllPrayers();
        this.ancientcurses = ancientcurses;
        refresh();
    }

    public Prayer() {
        quickPrayers = new boolean[][] { new boolean[22], new boolean[38] };
        codexPrayers = new boolean[3];
        buffs = new int[2][7];
        prayerpoints = 10;
        prayerPresets = new ArrayList<QuickPrayerPrest>(10);
    }

    public void setPlayer(Player player) {
        this.player = player;
        onPrayers = new boolean[][] { new boolean[22], new boolean[38] };
        buffs = new int[2][7];
        if (codexPrayers == null)
            codexPrayers = new boolean[3];
        if (quickPrayers[0].length != 22 || quickPrayers[1].length != 38)
            quickPrayers = new boolean[][] { new boolean[22], new boolean[38] };
        nextDrain = new long[38];
        if (prayerPresets == null)
            prayerPresets = new ArrayList<QuickPrayerPrest>(10);
    }

    public boolean isAncientCurses() {
        return ancientcurses;
    }

    public boolean usingPrayer(int book, int prayerId) {
        return onPrayers[book][prayerId];
    }

    public int getPrayerpoints() {
        return prayerpoints;
    }

    public void setPrayerpoints(int prayerpoints) {
        if (infinitePrayer && prayerpoints < this.prayerpoints) return;
        this.prayerpoints = prayerpoints;
    }

    private transient boolean infinitePrayer;
    public boolean isInfinitePrayer() { return infinitePrayer; }
    public void setInfinitePrayer(boolean enabled) { infinitePrayer = enabled; }

    public void refreshPrayerPoints() {
        player.getPackets().sendConfigByFile(16736, prayerpoints * 10);
    }

    public boolean hasFullPrayerpoints() {
        return getPrayerpoints() >= player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
    }

    public void drainPrayer(int amount) {
        drainPrayer(amount, false);
    }

    public void drainPrayer(int amount, boolean skipPerkCheck) {
        if (infinitePrayer) return;
        if (!skipPerkCheck && player.getPerkManager().hasPerkActive(DonationPerk.PRAYER_BETRAYER)) {
            if (Utils.random(4) == 0)
                return;
        }
        if ((prayerpoints - amount) >= 0)
            prayerpoints -= amount;
        else
            prayerpoints = 0;
        refreshPrayerPoints();
    }

    public void restorePrayer(int amount) {
        int maxPrayer = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
        if ((prayerpoints + amount) <= maxPrayer)
            prayerpoints += amount;
        else
            prayerpoints = maxPrayer;
        refreshPrayerPoints();
    }

    public void reset() {
        closeAllPrayers();
        prayerpoints = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
        refreshPrayerPoints();
    }

    public void drainPrayer() {
        if (infinitePrayer) return;
        prayerpoints = 0;
        refreshPrayerPoints();
    }

    public boolean isProtectingItem() {
        return ancientcurses ? usingPrayer(1, 0) : usingPrayer(0, 9);
    }

    public boolean isUsingProtectionPrayer() {
        return isMageProtecting() || isRangeProtecting() || isMeleeProtecting();
    }

    public boolean isMageProtecting() {
        return ancientcurses ? usingPrayer(1, 11) : usingPrayer(0, 11);
    }

    public boolean isRangeProtecting() {
        return ancientcurses ? usingPrayer(1, 12) : usingPrayer(0, 12);
    }

    public boolean isMeleeProtecting() {
        return ancientcurses ? usingPrayer(1, 13) : usingPrayer(0, 13);
    }

    public void setPrayerBookBefore(boolean ancientCurses) {
        this.prayerBookBefore = ancientCurses;
    }

    public boolean getPrayerBookBefore() {
        return prayerBookBefore;
    }

    public void closeProtectionPrayers() {
        if (getPrayerBook() == 1)
            closePrayers(closePrayers[1][3], closePrayers[1][4]);
        else
            closePrayers(closePrayers[0][4], closePrayers[0][5]);
        recalculatePrayer();
        player.getAppearence().generateIconsData();
    }

    public void closeDamageProtectionPrayers() {
        if (getPrayerBook() == 1)
            closePrayers(closePrayers[1][4]);
        else
            closePrayers(closePrayers[0][5]);
        recalculatePrayer();
        player.getAppearence().generateIconsData();
    }

    private boolean[] codexPrayers;// melee/range/magic

    public boolean[] getCodexPrayers() {
        return codexPrayers;
    }

    public void unlockCodexPrayer(int index) {
        codexPrayers[index] = true;
        refreshCodexPrayers();
    }

    public int getStatBonuses(int skillId) {
        int meleeLevel = buffs[0][MELEE_LEVEL] + buffs[1][MELEE_LEVEL];
        int rangeLevel = buffs[0][RANGE_LEVEL] + buffs[1][RANGE_LEVEL];
        int magiclevel = buffs[0][MAGIC_LEVEL] + buffs[1][MAGIC_LEVEL];
        int defenceLevel = buffs[0][DEFENCE_LEVEL] + buffs[1][DEFENCE_LEVEL];
        return (skillId == Skills.ATTACK ? meleeLevel : skillId == Skills.RANGE ? rangeLevel : skillId == Skills.MAGIC ? magiclevel : skillId == Skills.DEFENCE ? defenceLevel : 0);
    }

    public int getDebuffStats(int skillId) {
        int debuffIndex = skillId == Skills.ATTACK ? MELEE_LEVEL : skillId == Skills.RANGE ? RANGE_LEVEL : skillId == Skills.MAGIC ? MAGIC_LEVEL : skillId == Skills.DEFENCE ? DEFENCE_LEVEL : -1;
        if (debuffIndex == -1)
            return 0;
        int debuffAmount = player.getPrayerDebuff(debuffIndex);
        return debuffAmount > 10 ? 10 : debuffAmount;
    }

    public double getDamageMultiplier(int combatStyle) {
        return (double) (combatStyle == Combat.ALL_TYPE ? 0 : combatStyle == Combat.MELEE_TYPE ? buffs[0][MELEE_DAMAGE] + buffs[1][MELEE_DAMAGE] : combatStyle == Combat.RANGE_TYPE ? buffs[0][RANGE_DAMAGE] + buffs[1][RANGE_DAMAGE] : buffs[0][MAGIC_DAMAGE] + buffs[1][MAGIC_DAMAGE]) / 100.00;
    }

    public boolean hasRequiredLevel(int slotId, boolean ancient) {
        return hasRequiredLevel(slotId, ancient, true);
    }

    public boolean hasRequiredLevel(int slotId, boolean ancient, boolean sendMessage) {
        GeneralRequirementMap map = getPrayerMap(slotId, ancient);
        int requiredPrayerLevel = map.getIntValue(2807);
        int extraSkillId = map.getValues().containsKey((long) 6743) ? map.getIntValue(6743) : -1;
        boolean hasRequirements = player.getSkills().getLevelForXp(Skills.PRAYER) >= requiredPrayerLevel;
        if (extraSkillId != -1 && hasRequirements) {
            int extraLevelRequired = map.getIntValue(6744);
            hasRequirements = player.getSkills().getLevelForXp(extraSkillId) >= extraLevelRequired;
        }
        if (!hasRequirements)
            player.getPackets().sendGameMessage(map.getStringValue(2808));
        return hasRequirements;
    }

    public GeneralRequirementMap getPrayerMap(int slotId, boolean ancient) {
        ClientScriptMap map = ClientScriptMap.getMap(ancient ? 6760 : 6759);
        int level = player.getSkills().getLevelForXp(Skills.PRAYER);
        GeneralRequirementMap baseGMap = GeneralRequirementMap.getMap(map.getIntValue(slotId));
        if (baseGMap.getIntValue(2961) == 1) {
            if (level >= baseGMap.getIntValue(2967)) {
                return GeneralRequirementMap.getMap(baseGMap.getIntValue(2968));
            }
            if (level >= baseGMap.getIntValue(2965)) {
                return GeneralRequirementMap.getMap(baseGMap.getIntValue(2966));
            }
            return GeneralRequirementMap.getMap(baseGMap.getIntValue(2964));
        }
        return baseGMap;
    }

    public void disableAllPrayerMaps() {
        for (int i = 0; i < 2; i++) {
            ClientScriptMap map = ClientScriptMap.getMap(6759 + i);
            for (Object value : map.getValues().values()) {
                GeneralRequirementMap baseGMap = GeneralRequirementMap.getMap((int) value);
                if (baseGMap.getIntValue(2961) == 1) {
                    if (baseGMap.getIntValue(2968) != 0)
                        player.getPackets().sendExecuteScript(10624, baseGMap.getIntValue(2968), 0);
                    if (baseGMap.getIntValue(2966) != 0)
                        player.getPackets().sendExecuteScript(10624, baseGMap.getIntValue(2966), 0);
                    if (baseGMap.getIntValue(2964) != 0)
                        player.getPackets().sendExecuteScript(10624, baseGMap.getIntValue(2964), 0);
                } else
                    player.getPackets().sendExecuteScript(10624, baseGMap.getId(), 0);
            }
        }
    }


    private void refreshCodexPrayers() {
        if (player.getRights() == 2)
            Arrays.fill(codexPrayers, true);
        for (int i = 0; i < codexPrayers.length; i++)
            player.getPackets().sendConfigByFile(34890 + i, codexPrayers[i] ? 1 : 0);
    }

    private List<QuickPrayerPrest> prayerPresets;
    private int selectedPresetIndex;

    public List<QuickPrayerPrest> getPrayerPresets() {
        return prayerPresets;
    }

    public void openPrayerPresetsInterface() {
        player.getInterfaceManager().sendCentralOverlayInterface(1890);
        if (selectedPresetIndex >= prayerPresets.size())
            selectedPresetIndex = prayerPresets.size() - 1 <= 0 ? 0 : prayerPresets.size() - 1;
        unlockButtons();
        refreshCurrentPreset();
        refreshPresetsPrayers();
        refreshPresets();
    }

    public void createPreset(int nameSlot, boolean ancientcurses) {
        QuickPrayerPrest preset = new QuickPrayerPrest(nameSlot, ancientcurses);
        if (prayerPresets.size() == 10) {
            player.getPackets().sendGameMessage("You can't have more than 10 prayer presets.");
            return;
        }
        prayerPresets.add(preset);
        selectedPresetIndex = prayerPresets.size() - 1;
        player.getInterfaceManager().removeCentralOverlayInterface();
        openPrayerPresetsInterface();
    }

    public void deletePreset(int index) {
        if (index >= prayerPresets.size())
            return;
        if (selectedPresetIndex == index && index == prayerPresets.size() - 1)
            selectedPresetIndex = prayerPresets.size() - 2 <= 0 ? 0 : prayerPresets.size() - 2;
        else if (selectedPresetIndex >= prayerPresets.size())
            selectedPresetIndex = 0;
        prayerPresets.remove(index);
        player.getInterfaceManager().removeCentralOverlayInterface();
        openPrayerPresetsInterface();
    }

    public void editPreset(int index, int nameSlot) {
        if (index >= prayerPresets.size())
            return;
        prayerPresets.get(index).setNameSlot(nameSlot);
        refreshCurrentPreset();
        refreshPresetsPrayers();
        refreshLeftClickOption();
        refreshPresets();
    }

    public void editLeftClickOption(int leftClickOption) {
        this.leftClickOption = leftClickOption;
        refreshLeftClickOption();
    }

    private void refreshLeftClickOption() {
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.getPackets().sendExecuteScript(1023, leftClickOption == -1337 ? 2 : leftClickOption == selectedPresetIndex ? 1 : 0);
            }
        });
    }

    public void selectPreset(int index) {
        selectedPresetIndex = index;
        refreshCurrentPreset();
        refreshPresetPrayers(index);
        refreshLeftClickOption();
    }

    public void refreshPresets() {
        refreshCurrentPreset();
        for (int index = 0; index < 10; index++) {
            int value = 0;
            for (int shift = 0; shift < ((index == 2) ? 2 : 4); shift++) {
                int presetIndex = (index * 4) + shift;
                if (presetIndex >= prayerPresets.size()) {
                    value |= 0 << (8 * shift);
                    continue;
                }
                QuickPrayerPrest preset = prayerPresets.get(presetIndex);
                if (preset == null) {
                    value |= 0 << (8 * shift);
                    continue;
                }
                value |= preset.getNameSlot() << (8 * shift);
            }
            player.getPackets().sendConfig(7054 + index, value);
        }
        for (int i = 0; i < prayerPresets.size(); i++) {
            QuickPrayerPrest preset = i >= prayerPresets.size() ? null : prayerPresets.get(i);
            player.getPackets().sendConfigByFile(36842 + i, preset == null || !preset.isAncientcurses() ? 0 : 1);
        }
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.getPackets().sendExecuteScript(8050, InterfaceManager.getComponentUId(1430, 16));
                player.getPackets().sendExecuteScript(8050, InterfaceManager.getComponentUId(1505, 2));
            }

        });
    }

    public void refreshCurrentPreset() {
        if (selectedPresetIndex >= prayerPresets.size())
            selectedPresetIndex = prayerPresets.size() - 1 <= 0 ? 0 : prayerPresets.size() - 1;
        QuickPrayerPrest preset = selectedPresetIndex>= prayerPresets.size() ? null : prayerPresets.get(selectedPresetIndex);
        player.getPackets().sendConfigByFile(36842 + selectedPresetIndex, preset == null || !preset.isAncientcurses() ? 0 : 1);
        player.getPackets().sendConfigByFile(36829, prayerPresets.size());
        player.getPackets().sendConfigByFile(36830, selectedPresetIndex);
    }

    public QuickPrayerPrest getCurrentPreset() {
        refreshCurrentPreset();
        return prayerPresets.isEmpty() ? null : prayerPresets.get(selectedPresetIndex);
    }

    public void refreshPresetsPrayers() {
        for (int index = 0; index < prayerPresets.size(); index++)
            refreshPresetPrayers(index);
    }

    public void refreshPresetPrayers() {
        if (selectedPresetIndex >= prayerPresets.size())
            selectedPresetIndex = prayerPresets.size() - 1 <= 0 ? 0 : prayerPresets.size() - 1;
        refreshPresetPrayers(selectedPresetIndex);
    }

    public void refreshPresetPrayers(int index) {
        if (prayerPresets.isEmpty())
            return;
        if (index >= prayerPresets.size())
            index = prayerPresets.size() - 1 <= 0 ? 0 : prayerPresets.size() - 1;
        QuickPrayerPrest preset = prayerPresets.get(index);
        int value = 0;
        int value2 = 0;
        if (preset != null) {
            boolean[] book = preset.getOnPrayers();
            for (int slot = 0; slot < book.length; slot++) {
                int pIndex = getIndex1(preset.isAncientcurses(), slot);
                if (pIndex >= 32)
                    value2 |= (book[slot] ? 1 : 0) << (pIndex - 32);
                if (pIndex < 32)
                    value |= (book[slot] ? 1 : 0) << pIndex;
            }
        }
        // update the value's slots the varbits are correct for both curses and
        // normal (only this is left)
        player.getPackets().sendConfig(7058 + (index * 2), value);
        player.getPackets().sendConfig(7059 + (index * 2), value2);
    }

    private int getIndex1(boolean ancientCurses, int slot) {
        for (int i = 0; i < VARBIT_SLOTS_1[ancientCurses ? 1 : 0].length; i++)
            if (VARBIT_SLOTS_1[ancientCurses ? 1 : 0][i] == slot)
                return i;
        return -1;
    }

    private static final int[][] VARBIT_SLOTS_1 = { { 0, 1, 2, 4, 3, 6, 5, 7, 8, 9, 11, 12, 13, 14, 15, 16, 10, 17, 19, 18, 21, 20 }, { 0, 1, 2, 4, 6, 9, 10, 11, 12, 13, 14, 15, 17, 19, 22, 23, 24, 27, 30, 32, 3, 16, 5, 18, 34, 33, 8, 7, 31, 20, 21, 26, 28, 25, 29, 35, 36, 37 } };

    public void unlockButtons() {
        player.getPackets().sendIComponentSettings(1890, 4, 0, 9, 2);
        player.getPackets().sendIComponentSettings(1890, 6, 0, 9, 2);
        player.getPackets().sendIComponentSettings(1890, 7, 0, 9, 2);
        player.getPackets().sendIComponentSettings(1890, 10, 0, 9, 12845058);
        player.getPackets().sendIComponentSettings(1890, 19, 0, 1, 2);
        player.getPackets().sendIComponentSettings(1890, 23, 0, 500, 2);
        player.getPackets().sendIComponentSettings(1890, 38, 0, 100, 12845058);
        player.getPackets().sendIComponentSettings(1890, 48, 0, 1, 2);
        player.getPackets().sendIComponentSettings(1890, 53, 0, 1, 2);
        player.getPackets().sendIComponentSettings(1890, 54, 0, 1, 2);
        player.getPackets().sendIComponentSettings(1890, 76, 0, 1, 2);
        player.getPackets().sendIComponentSettings(1890, 80, 0, 1, 2);
        player.getPackets().sendIComponentSettings(1890, 81, 0, 1, 2);
        player.getPackets().sendIComponentSettings(1890, 90, 0, 1, 2);
        player.getPackets().sendIComponentSettings(1890, 99, 0, 500, 2);
    }

    public int getSelectedPresetIndex() {
        return selectedPresetIndex;
    }

    public static final class QuickPrayerPrest implements Serializable {

        private static final long serialVersionUID = -9035811928807990124L;

        private int nameSlot;
        private final boolean ancientcurses;
        private final boolean[] onPrayers;

        public QuickPrayerPrest(int nameSlot, boolean ancientcurses) {
            this.nameSlot = nameSlot;
            this.ancientcurses = ancientcurses;
            onPrayers = new boolean[ancientcurses ? 38 : 22];
        }

        public int getNameSlot() {
            return nameSlot;
        }

        public void setNameSlot(int nameSlot) {
            this.nameSlot = nameSlot;
        }

        public boolean isAncientcurses() {
            return ancientcurses;
        }

        public boolean[] getOnPrayers() {
            return onPrayers;
        }

        private int getPrayerBook() {
            return isAncientcurses() ? 1 : 0;
        }
    }

    private boolean filter;

    public void toggleFilter() {
        filter = !filter;
        refreshFilter();
    }

    private void refreshFilter() {
        player.getPackets().sendConfigByFile(45115, filter ? 1 : 0);
    }

    private boolean showActivePrayersOnly;

    public void toggleShowActivePrayersOnly() {
        showActivePrayersOnly = !showActivePrayersOnly;
        refreshShowActivePrayersOnly();
    }

    public void refreshShowActivePrayersOnly() {
        player.getPackets().sendConfigByFile(45116, showActivePrayersOnly ? 1 : 0);
    }

    private int leftClickOption, lastUsed;

}
