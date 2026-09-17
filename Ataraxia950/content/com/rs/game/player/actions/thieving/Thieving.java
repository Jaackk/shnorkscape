package com.rs.game.player.actions.thieving;

import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.ThievingRandomEvent;
import com.rs.game.player.Equipment;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.thieving.def.Stalls;
import com.rs.game.player.content.Pots;
import com.rs.game.player.content.skillingcontracts.impl.ThievingContractList;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.packet.impl.ObjectHandler;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.List;

/**
 * Handles the Thieving Skill
 *
 * @author Dragonkk
 */
public class Thieving {

    public static void checkGuards(Player player) {
        NPC guard = null;
        int lastDistance = -1;
        for (int regionId : player.getMapRegionsIds()) {
            List<Integer> npcIndexes = World.getRegion(regionId).getNPCsIndexes();
            if (npcIndexes == null)
                continue;
            for (int npcIndex : npcIndexes) {
                NPC npc = World.getNPCs().get(npcIndex);
                if (npc == null)
                    continue;
                if (!isGuard(npc.getId()) || npc.isUnderCombat() || npc.isDead() || !npc.withinDistance(player, 4)
                        || !npc.clipedProjectile(player, true))
                    continue;
                int distance = Utils.getDistance(npc.getX(), npc.getY(), player.getX(), player.getY());
                if (lastDistance == -1 || lastDistance > distance) {
                    guard = npc;
                    lastDistance = distance;
                }
            }
        }
        if (guard != null && !player.getPerkManager().hasPerkActive(DonationPerk.SLEIGHT_OF_HAND)) {
            guard.setNextForceTalk(new ForceTalk("Hey, what do you think you are doing!"));
            player.sendMessage("Purchase the Sleight of Hand perk to never get caught.", true);
            guard.setTarget(player);
        }
    }

    private static int getIncreasedChance(Player player) {
        int chance = 0;
        if (Equipment.getItemSlot(Equipment.SLOT_HANDS) == 10075)
            chance += 12;
        if (Equipment.getItemSlot(Equipment.SLOT_CAPE) == 15349)
            chance += 15;
        if(player.jujuPotions.isActive(Pots.Effects.SCENTLESS_JUJU)) {
            chance += 15;
        }
        return chance;
    }

    public static void handleStalls(final Player player, final WorldObject object) {
        if (player.getAttackedBy() != null && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
            player.sendMessage("You can't do this while you're under combat.");
            return;
        }
        for (final Stalls stall : Stalls.values()) {
            if (stall.getObjectId() == object.getId()) {
                final WorldObject emptyStall = new WorldObject(stall.getReplaceObject(), 10, object.getRotation(),
                        object.getX(), object.getY(), object.getPlane());
                if (player.getSkills().getLevel(Skills.THIEVING) < stall.getLevel()) {
                    player.sendMessage("You need a thieving level of " + stall.getLevel() + " to steal from this.",
                            true);
                    return;
                }
                if (!player.getInventory().hasFreeSlots()) {
                    player.sendMessage("Inventory full. To make more room, sell, drop or bank something.", true);
                    return;
                }

                player.setNextAnimation(new Animation(881));
                player.lock(2);

                WorldTasksManager.schedule(new WorldTask() {

                    boolean gaveItems;

                    @Override
                    public void run() {
                        if (!World.containsObjectWithId(object, object.getId()))
                            return;
                        if (!gaveItems) {
                            player.getInventory().addItem(stall.getItem(Utils.getRandom(stall.getItem().length - 1)),
                                    Utils.getRandom(stall.getAmount()));
                            player.getSkills().addXp(Skills.THIEVING, stall.getExperience());
                            player.addTimesStolen();
                            ThievingContractList.listenStall(player, stall);
                            player.sendMessage("You've successfully stolen from this stall; " + "times thieved: "
                                    + Colors.RED + Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.", true);
                            ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
                            gaveItems = true;
                            checkGuards(player);
                            if (player.getDailyManager().getTask() != null) {
                                if (object.getId() == player.getTaskItemId()) {
                                    player.getDailyManager().processTask();
                                }
                            }
							if (Utils.random(50) == 0 && player.hasRandomEvent()) {
								if (!player.followedByRandomEventNPC()) {
									NPC npc = new ThievingRandomEvent(player, player);
									if (npc.withinDistance(player, 14)) {
										player.setCurrentRandomEventNPC(npc);
										player.sendMessage("<col=ff0000>A Rogue appears out of nowhere.");
									}
								}
							}
                            stop();
                        } else {
                            World.spawnObjectTemporary(emptyStall, (int) (1500 * stall.getTime()));
                            stop();
                        }
                    }
                }, 0, 0);
            }
        }
    }

    public static boolean isGuard(int npcId) {
        return npcId == 32 || npcId == 21 || npcId == 2256 || npcId == 23;
    }

    public static boolean pickDoor(Player player, WorldObject object) {
        if (player.getTemporaryAttributtes().get("numbFingers") == null)
            player.getTemporaryAttributtes().put("numbFingers", 0);
        int thievingLevel = player.getSkills().getLevel(Skills.THIEVING);
        int increasedChance = getIncreasedChance(player);
        int decreasedChance = (Integer) player.getTemporaryAttributtes().get("numbFingers");
        int level = Utils.getRandom(thievingLevel + (increasedChance - decreasedChance)) + 1;
        double ratio = level / (Utils.getRandom(45 + 5) + 1);
        if (Math.round(ratio * thievingLevel) < (player.getAttackedByDelay() > 0 ? 50 : 40)
                / player.getAuraManager().getThievingAccurayMultiplier() && !player.getPerkManager().hasPerkActive(DonationPerk.SLEIGHT_OF_HAND)) {
            player.sendMessage("You fail to unlock the door and your hands begin to numb down.", true);
            player.sendMessage("Purchase the Sleight of Hand perk to never fail picking locks.", true);
            player.getTemporaryAttributtes().put("numbFingers", decreasedChance + 1);
            return false;
        }
        player.sendMessage("You successfully unlock the door.");
        ObjectHandler.handleDoor(player, object, 1500 + Utils.getRandom(1000));
        return true;
    }

    /**
     * XP modifier by wearing items.
     *
     * @param player The player.
     * @return the XP modifier.
     */
    public static double outfitBoost(Player player) {
        double xpBoost = 1.0;
        if (player.getEquipment().getHatId() == 21482)
            xpBoost *= 1.10;
        if (player.getEquipment().getChestId() == 21480)
            xpBoost *= 1.10;
        if (player.getEquipment().getLegsId() == 21481)
            xpBoost *= 1.10;
        if (player.getEquipment().getBootsId() == 21483)
            xpBoost *= 1.10;
        if (player.getEquipment().getHatId() == 21482 && player.getEquipment().getChestId() == 21480
                && player.getEquipment().getLegsId() == 21481 && player.getEquipment().getBootsId() == 21483)
            xpBoost *= 1.50;
        if (Skills.hasCamouflageOutfit(player, false))
            xpBoost *= 1.07;
        if (Skills.hasCamouflageOutfit(player, true))
            xpBoost *= 1.;
        return xpBoost;
    }


}