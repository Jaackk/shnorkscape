package com.rs.game.player.actions.firemaking;

import com.google.common.collect.Iterables;
import com.rs.game.Animation;
import com.rs.game.player.client.Native950Firemaking;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.FiremakingRandomEvent;
import com.rs.game.player.Equipment;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.BuffDebuffTimersManager.Timer;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.firemaking.defs.Log;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.content.interfaces.potiontimers.PotionTimer;
import com.rs.game.player.content.interfaces.potiontimers.PotionTimerInterface;
import com.rs.game.player.content.packs.portable.PortableStation;
import com.rs.game.player.content.skillingcontracts.impl.FiremakingContractList;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Bonfire.java | 11:27:12 AM
 *
 * @author Chryonic
 * @date Apr 15, 2017
 */
public class Bonfire extends Action {

    private final Log log;
    private final WorldObject object;
    private final boolean portable;

    private int count, ticks;

    public Bonfire(Log log, WorldObject object, boolean portable) {
        this.log = log;
        this.object = object;
        this.portable = portable;
    }

    public static boolean addLog(Player player, WorldObject object, Item item) {
        if (player.isNative950()) {
            Log selected=item == null ? null : Log.forId(item.getId());
            return Native950Firemaking.checkBonfire(player,selected,object)
                    && player.getActionManager().setAction(new Bonfire(selected,object,false));
        }
        for (Log log : Log.values())
            if (log.getLogId() == item.getId()) {
                player.getActionManager().setAction(new Bonfire(log, object, PortableStation.isPortableObject(object)));
                return true;
            }
        return false;
    }

    public static boolean addLogs(Player player, WorldObject object) {
        if (player.isNative950()) {
            for (Item item : player.getInventory().getItems().getItems()) {
                if (item == null) continue;
                Log available=Log.forId(item.getId());
                if (Native950Firemaking.checkBonfire(player,available,object))
                    return player.getActionManager().setAction(new Bonfire(available,object,false));
            }
            player.sendMessage("You do not have any usable logs to add to this fire.");
            return false;
        }
        List<Log> possibleLogs = new ArrayList<>();
        for (Log log : Log.VALUES) {
			if (player.getInventory().containsItem(log.getLogId(), 1))
				possibleLogs.add(log);
		}
        if (possibleLogs.isEmpty()) {
            player.sendMessage("You do not have any logs to add to this fire.");
            return false;
        } else if (possibleLogs.size() == 1) {
            player.getActionManager().setAction(new Bonfire(possibleLogs.get(0), object, PortableStation.isPortableObject(object)));
			return true;
		} else {
            player.getDialogueManager().startDialogue("BonfireD", Iterables.toArray(possibleLogs, Log.class), object);
            return true;
        }
    }

    public static double getBonfireBoostMultiplier(Player player) {
        int fmLvl = player.getSkills().getLevel(Skills.FIREMAKING);
        if (fmLvl >= 90)
            return 1.1;
        if (fmLvl >= 80)
            return 1.09;
        if (fmLvl >= 70)
            return 1.08;
        if (fmLvl >= 60)
            return 1.07;
        if (fmLvl >= 50)
            return 1.06;
        if (fmLvl >= 40)
            return 1.05;
        if (fmLvl >= 30)
            return 1.04;
        if (fmLvl >= 20)
            return 1.03;
        if (fmLvl >= 10)
            return 1.02;
        return 1.01;
    }

    private boolean checkAll(Player player) {
        if (player.isNative950()) return Native950Firemaking.checkBonfire(player,log,object);
        if (!World.containsObjectWithId(object, object.getId()))
            return false;
        if (!player.getInventory().containsItem(log.getLogId(), 1))
            return false;
        if (player.getSkills().getLevel(Skills.FIREMAKING) < log.getLevel()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need level " + log.getLevel() + " Firemaking to add these logs to a bonfire.");
            return false;
        }
        if (player.clickedObject != null) {
            return World.containsObjectWithId(player.clickedObject, player.clickedObject.getId());
        }
        return true;
    }

    @Override
    public boolean process(Player player) {
        if (player.isNative950()) return checkAll(player);
        if (log == Log.PROTEAN && ticks == 0)
            return false;
        if (checkAll(player)) {
            player.setNextFaceWorldTile(object);
            if (Utils.random(player.getPerkManager().hasPerkActive(DonationPerk.THE_PYROMANIAC) ? 375 : 500) == 0 && player.hasRandomEvent()) {
                if (!player.followedByRandomEventNPC()) {
                    NPC npc = new FiremakingRandomEvent(new WorldTile(object, 1), player);
                    if (npc.withinDistance(player, 14)) {
                        player.setCurrentRandomEventNPC(npc);
                        player.sendMessage("<col=ff0000>A fire spirit emerges from the bonfire.");
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int processWithDelay(Player player) {
        if (player.isNative950()) return Native950Firemaking.burnBonfireLog(player,log,object);
        if (log == Log.PROTEAN) {
            ticks--;
            if (ticks == 0)
                player.sendMessage("Your arms grew tired of throwing protean logs onto the fire but you're ready to go again now.");
        }
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null && (weapon.getName().toLowerCase().contains("pyro-matic") || weapon.getName().toLowerCase().contains("crystal tinderbox"));
        if (!hasAugmentedTool)
            weapon = null;
        Perk pyromaniac = weapon != null && log != Log.PROTEAN && !portable ? player.getInventionManager().hasPerk(weapon, Perks.FURNACE) : null;
        int amountToRemove = pyromaniac != null && Math.random() <= (0.001 * (double) pyromaniac.getRank() * (pyromaniac.hasIncreasedChance() ? 1.15 : 1.00)) ? player.getInventory().getAmountOf(log.getLogId()) : 1;
        if (amountToRemove > 1)
            player.getPackets().sendGameMessage("<col=00FF00>Your pyromaniac perk allows you to burn all your logs at once!");

        player.closeInterfaces();
        for (int i = 0; i < amountToRemove; i++)
            player.addLogsBurned();
        if (player.getSkillingTask() == 11) {
            if (player.getDailyManager().getTask() != null) {
                if (log.getLogId() == player.getTaskItemId()) {
                    player.getDailyManager().processTask();
                }
            }
        }

        // portables give a 10% chance to save resource.
        if (!portable || Utils.random(9) != 4)
            player.getInventory().deleteItem(log.getLogId(), amountToRemove);
        else
            player.sendMessage(Colors.GOLD + "<shad=000000>The portable brazier saves you a log.", true);
        for (int i = 0; i < amountToRemove; i++)
            FiremakingContractList.listen(player, log);
        if (log != Log.PROTEAN) {
            double xp = Firemaking.increasedExperience(player, log.getExperience()) * (player.getPerkManager().hasPerkActive(DonationPerk.THE_PYROMANIAC) ? 1.2 : 1) * (portable ? 1.1 : 1) * amountToRemove;
            Perk tinker = weapon != null ? player.getInventionManager().hasPerk(weapon, Perks.TINKER) : null;
            boolean tinkerActive = tinker != null && Math.random() <= (0.05 * (double) tinker.getRank() * (tinker.hasIncreasedChance() ? 1.15 : 1.00));
            if (tinkerActive) {
                xp *= 1.25;
                player.getPackets().sendGameMessage("<col=00ff00>Your tinker perk allows you to carry out higher quality work, awarding extra XP.");
            }
            player.getInventionManager().processSkillXp(Skills.FIREMAKING, xp / 2.00, weapon);
            player.getSkills().addXp(Skills.FIREMAKING, xp);
        } else {
            double xp = Firemaking.increasedExperience(player, player.getSkills().getLevelForXp(Skills.FIREMAKING) * 7.96) * (player.getPerkManager().hasPerkActive(DonationPerk.THE_PYROMANIAC) ? 1.2 : 1) * (portable ? 1.1 : 1);
            Perk tinker = weapon != null ? player.getInventionManager().hasPerk(weapon, Perks.TINKER) : null;
            boolean tinkerActive = tinker != null && Math.random() <= (0.05 * (double) tinker.getRank() * (tinker.hasIncreasedChance() ? 1.15 : 1.00));
            if (tinkerActive) {
                xp *= 1.25;
                player.getPackets().sendGameMessage("<col=00ff00>Your tinker perk allows you to carry out higher quality work, awarding extra XP.");
            }
            player.getInventionManager().processSkillXp(Skills.FIREMAKING, xp, weapon);
            player.getSkills().addXp(Skills.FIREMAKING, xp);
        }
        if (player.getAnimations().bombFiremaking && player.getAnimations().hasBombFiremaking) {
            player.setNextAnimation(new Animation(16699));
            player.setNextGraphics(new Graphics(3461));
        } else if (player.getAnimations().partyhatFiremaking && player.getAnimations().hasPartyhatFiremaking) {
            player.setNextAnimation(new Animation(16699));
            player.setNextGraphics(new Graphics(Utils.random(4726, 4735)));
        } else {
            player.setNextAnimation(new Animation(16703));
            player.setNextGraphics(new Graphics(log.getBonfireGFX()));
        }
        player.getPackets().sendGameMessage("You add a log to the fire; logs burned " + Colors.RED + Utils.getFormattedNumber(player.getLogsBurned()) + "</col>.", true);
        if ((portable || count++ == 4)) {
            if (player.getLastBonfire() == 0) {
                int percentage = (int) (getBonfireBoostMultiplier(player) * 100 - 100);
                player.sendMessage("<col=00ff00>The bonfire's warmth increases your maximum health by " + percentage + "%. This will last " + log.getBoostTime() + " minutes.");
                player.setLastBonfire(log.getBoostTime() * 100);
                player.getEquipment().refreshConfigs(false);
                player.getBuffDebuffTimersManager().addTimer(Timer.BONFIRE_BOOST_ACTIVE, (log.getBoostTime() * 100) * 600);
            } else {
                player.setLastBonfire(log.getBoostTime() * 100);
                player.getBuffDebuffTimersManager().addTimer(Timer.BONFIRE_BOOST_ACTIVE, (log.getBoostTime() * 100) * 600);
            }

            ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
        }
        player.faceObject(object);
        Perk rapid = weapon != null ? player.getInventionManager().hasPerk(weapon, Perks.RAPID) : null;
        boolean rapidActive = rapid != null && Math.random() <= (0.05 * (double) rapid.getRank() * (rapid.hasIncreasedChance() ? 1.15 : 1.00));
        if (rapidActive)
            player.getPackets().sendGameMessage("<col=00FF00>Your rapid perk speeds up the action process.");
        return rapidActive ? 4 : 6;
    }

    @Override
    public boolean start(Player player) {
        if (player.isNative950()) {
            if (portable || !checkAll(player)) return false;
            player.setNextFaceWorldTile(object);
            setActionDelay(player,0);
            return true;
        }
        if (checkAll(player)) {
            player.getAppearence().setRenderEmote(2498);
            if (log == Log.PROTEAN)
                ticks = 60;
            return true;
        }
        return false;
    }

    @Override
    public void stop(final Player player) {
        if (player.isNative950()) { Native950Firemaking.stopBonfire(player); return; }
        player.getEmotesManager().setNextEmoteEnd(2400);
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.setNextAnimation(new Animation(16702));
                player.getAppearence().setRenderEmote(-1);
            }
        }, 3);
        player.clickedObject = null;
    }

}