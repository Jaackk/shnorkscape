package com.rs.game.player.actions.slayer;

import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Slayer implements Serializable {

    private static final long serialVersionUID = -1057214838196942673L;
    private transient Player player, coPlayer;
    private final boolean[] abilities;
    private boolean requestedWithPartner;
    @SuppressWarnings("unused")
    private int slayerPoints, coPoints, completedTasks, currentStreak, currentTaskAmount, currentTaskId,
            currentMasterId, slayerHelmUpgrade, coKills, initialAmount, spawnedMaster;

    @SuppressWarnings("unused")
    private static final int FEROCIOUS_RING_UPGRADE = 0, AQUANITES = 1, MUSPAH = 2, NIHILS = 3, GLACORS = 4,
            TORMENTED_DEMONS = 5, NIGHTMARE_CREATURES = 6, FLETCH_BROAD = 7, CRAFT_SLAYER_RING = 8,
            DELIVER_QUICK_BLOWS = 9, CRAFT_SLAYER_HELMETS = 10, FUSE_RINGS_TO_SLAYER_HELM = 11, ICE_STRYKEWYRMS = 12,
            ATTACH_BONECRUSHER = 13, ATTACH_HERBICIDE = 14, ATTACH_SEEDICIDE = 15, ATTACH_CHARMING_IMP = 16;
    private static final int[] CHECKPOINTS = new int[]{1, 2, 3, 4, 5, 10, 20, 30, 40, 50, 75, 100, 125, 150, 175, 200,
            250, 300};

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Slayer() {
        spawnedMaster = 8461;
        abilities = new boolean[17];
        currentTaskId = -1;
    }

    public void assignPartner(Player partner) {
        if (currentTaskId != -1 || partner.getSlayer().currentTaskId != -1) {
            if (!getSlayerTaskData().equals(partner.getSlayer().getSlayerTaskData())) {
                player.sendMessage("You and " + partner.getDisplayName()
                        + " must both either not have an active Slayer task or have the same Slayer task to become Slayer partners.");
                return;
            }
        }
        /**
         * Sets current "progress" of both players' co-slayer if they were both
         * on a task already. This is to ensure no points get lost upon
         * completion of a task.
         */
        if (currentTaskId != -1) {
            requestedWithPartner = true;
            partner.getSlayer().requestedWithPartner = true;
            coKills = initialAmount - currentTaskAmount;
            partner.getSlayer().coKills = partner.getSlayer().initialAmount - partner.getSlayer().currentTaskAmount;
        }
        coPlayer = partner;
        partner.getSlayer().coPlayer = player;
        player.sendMessage("You've created a social Slayer group with " + partner.getDisplayName() + ".");
        partner.sendMessage("You've joined " + player.getDisplayName() + "'s social Slayer group.");
    }

    /**
     * Generates a new slayer/social slayer task. boolean replacementTask ->
     * Used to determine whether the task requested is from Turael as a
     * replacement or not. boolean paidReplacement -> Used to force reset a
     * slayer task without resetting the streak.
     */
    public void requestTask(boolean replacementTask, boolean paidReplacement) {
        if (!paidReplacement) {
            if (replacementTask) {
                if (currentMasterId == 0)
                    return;// Can't request a replacement task from Turael
                if (getSlayerTaskData().getCertainTaskSet(0) != null) {
                    player.sendMessage("Turael refuses to give you a replacement task.");
                    return;
                }
            }
            if (!replacementTask && coPlayer == null && currentTaskId != -1) {
                player.sendMessage("You've already got a Slayer task. " + (currentMasterId == 0 ? ""
                        : " If you wish to change your task for an easier one, speak to Turael, perhaps he can assist you."));
                return;
            }
        }
        if (coPlayer == null) {
            requestedWithPartner = false;
            generateTask(replacementTask);
            player.sendMessage("Your Slayer task is to slay " + currentTaskAmount + " "
                    + SlayerTaskData.values()[currentTaskId].toString() + (currentMasterId == 0 ? ""
                    : " If you wish to change your task for an easier one, speak to Turael, perhaps he can assist you."));
        } else {
            if (!paidReplacement && !replacementTask
                    && (coPlayer.getSlayer().getSlayerTaskData() != null || getSlayerTaskData() != null)) {
                player.sendMessage(
                        "You and your partner both must not have an active Slayer task to obtain a new one.");
                return;
            }
            generateTask(replacementTask);
            requestedWithPartner = true;
            player.sendMessage(getSlayerTask());
            coPlayer.getSlayer().forceTask(currentTaskId, currentTaskAmount);
            coPlayer.sendMessage(coPlayer.getSlayer().getSlayerTask());
        }
    }

    public int getSpawnedMasterId() {
        return spawnedMaster;
    }

    public void setSpawnedMasterId(int id) {
        spawnedMaster = id;
    }

    /**
     * Used to set partner's slayer task to the same task.
     */
    private void forceTask(int id, int amount) {
        currentTaskAmount = amount;
        currentTaskId = id;
    }

    /**
     * Generates a valid task based on player's statistics and current master.
     */
    private void generateTask(boolean replacementTask) {
        if (replacementTask)
            currentStreak = 0;
        int currentMasterId = replacementTask ? 0 : this.currentMasterId;
        List<SlayerTaskData> tasks = new ArrayList<SlayerTaskData>();
        int weight = 0;
        for (SlayerTaskData t : SlayerTaskData.values()) {
            if (player.getSkills().getLevelForXp(Skills.SLAYER) < t.getSlayerRequirement())
                continue;
            for (int i = 0; i < t.getTaskSet().length; i++) {
                if (t.getTaskSet()[i].getSlayerMaster().ordinal() == currentMasterId) {
                    if (!canAssign(t))
                        continue;
                    weight += t.getTaskSet()[i].getWeight();
                    tasks.add(t);
                }
            }
        }
        int randomTask = Utils.random(weight);
        int currentWeight = 0;
        TaskSet task = null;
        for (int i = 0; i < tasks.size(); i++) {
            SlayerTaskData data = tasks.get(i);
            if (data.getCertainTaskSet(currentMasterId).getWeight() + currentWeight >= randomTask) {
                task = data.getCertainTaskSet(currentMasterId);
                currentTaskId = data.ordinal();
                currentTaskAmount = Utils.random(task.getMinimumAmount(), task.getMaximumAmount());
                initialAmount = currentTaskAmount;
                break;
            }
            currentWeight += data.getCertainTaskSet(currentMasterId).getWeight();
        }
    }

    /**
     * Checks the npc's name upon its death to determine whether it was the
     * player's task or not. Handled in reset() method in NPC class, because
     * it's the only class that's not being overran by other classes and it's
     * always ran during npc's death.
     */
    public void checkTask(NPC npc) {
        if (currentTaskId == -1)
            return;
        SlayerTaskData data = SlayerTaskData.values()[currentTaskId];
        for (int i = 0; i < data.getMonsters().length; i++) {
            if (npc.getName().equalsIgnoreCase(data.getMonsters()[i])) {
                player.getSkills().addXp(Skills.SLAYER, npc.getMaxHitpoints() / 10);
                currentTaskAmount--;
                checkAmount(false, data);
                if (coPlayer != null) {
                    coKills++;
                    if (coPlayer.withinDistance(player, 16)) {
                        checkAmount(true, data);
                        coPlayer.getSlayer().currentTaskAmount--;
                    }
                }
                break;
            }
        }
    }

    public boolean removeSlayerPoints(int amount) {
        if (amount > slayerPoints)
            return false;
        slayerPoints -= amount;
        return true;
    }

    /**
     * Using a secondary method for this to lower the size of the code overall,
     * since social slayer requires to check both parties.
     */
	private void checkAmount(boolean partner, SlayerTaskData data) {
		Player player = partner ? coPlayer : this.player;
		for (int c : CHECKPOINTS) {
			if (player.getSlayer().currentTaskAmount == c) {
				player.sendMessage("You're doing great; Only " + player.getSlayer().currentTaskAmount + " " + (player.getSlayer().currentTaskAmount == 1 ? data.getSingularName() : data.toString()) + " left to slay.", true);
				break;
			}
		}
		if (player.getSlayer().currentTaskAmount <= 0) {
			player.sendMessage("You have finished your Slayer task. Talk to " + Utils.formatPlayerNameForDisplay(SlayerMasterData.values()[player.getSlayer().currentMasterId].toString()) + " for a new one.");
			player.getSlayer().currentTaskId = -1;
			player.getSlayer().completedTasks++;
			player.getSlayer().coPoints += player.getSlayer().currentMasterId + 1;
			if (player.getSlayer().currentMasterId != 0) {
				player.getSlayer().currentStreak++;
				float multiplier = coPlayer == null && !requestedWithPartner ? 1 : ((float) coKills / initialAmount);
				if (player.getSlayer().currentStreak % 50 == 0) {
					int amount = (int) (SlayerMasterData.values()[player.getSlayer().currentMasterId].get50thTaskPoints() * multiplier);
					player.getSlayer().slayerPoints += amount;
					if (amount != 0)
						player.sendMessage("You've completed 50 Slayer tasks in a row and receive " + amount + " Slayer points.");
				} else if (player.getSlayer().currentStreak % 10 == 0) {
					int amount = (int) (SlayerMasterData.values()[player.getSlayer().currentMasterId].get10thTaskPoints() * multiplier);
					player.getSlayer().slayerPoints += amount;
					if (amount != 0)
						player.sendMessage("You've completed 10 Slayer tasks in a row and receive " + amount + " Slayer points.");
				} else {
					int amount = (int) (SlayerMasterData.values()[player.getSlayer().currentMasterId].getPointsPerTask() * multiplier);
					player.getSlayer().slayerPoints += amount;
					if (amount != 0)
						player.sendMessage("You've completed your Slayer task and receive " + amount + " Slayer points.");
				}
			}
			player.getAchievements().updateProgress(1, AchievementList.COMPLETE_25_SLAYER_TASKS, AchievementList.COMPLETE_50_SLAYER_TASKS, AchievementList.COMPLETE_100_SLAYER_TASKS, AchievementList.COMPLETE_500_SLAYER_TASKS);
		}
	}

    /**
     * Checks for special tasks, to see if player has the necessary side
     * requirements unlocked to obtain a certain task.
     */
    private boolean canAssign(SlayerTaskData task) {
        return (!task.equals(SlayerTaskData.ICE_STRYKEWYRMS) || (player.hasItem(new Item(6570))
                || player.hasItem(new Item(23659)) || player.hasItem(new Item(31610)) || player.hasItem(new Item(31611))
                || player.hasItem(new Item(31603)) || abilities[ICE_STRYKEWYRMS]))
                && (!task.equals(SlayerTaskData.AQUANITES) || abilities[AQUANITES])
                && (!task.equals(SlayerTaskData.EDIMMUS) || (player.getSkills().getLevelForXp(Skills.DUNGEONEERING) >= 115 && player.hasAccessToPrifddinas()))
                && (!task.equals(SlayerTaskData.GLACORS) || abilities[GLACORS])
                && (!task.equals(SlayerTaskData.KALGERION_DEMONS)
                || player.getSkills().getLevelForXp(Skills.DUNGEONEERING) >= 90)
                && (!task.equals(SlayerTaskData.MUSPAH) || abilities[MUSPAH])
                && (!task.equals(SlayerTaskData.NIHIL) || abilities[NIHILS])
                && (!task.equals(SlayerTaskData.NIGHTMARE_CREATURES) || abilities[NIGHTMARE_CREATURES])
                && (!task.equals(SlayerTaskData.TORMENTED_DEMONS) || abilities[TORMENTED_DEMONS]);
    }

    public SlayerTaskData getSlayerTaskData() {
        if (currentTaskId == -1)
            return null;
        return SlayerTaskData.values()[currentTaskId];
    }

    /**
     * Enchanted gem check option.
     */
    public String getSlayerTask() {
        if (currentTaskId == -1)
            return "You currently have no Slayer task. Speak to one of the Slayer masters to get one.";
        return "Your current Slayer task is to kill " + currentTaskAmount + " "
                + SlayerTaskData.values()[currentTaskId].toString() + ".";
    }

    public int getCurrentAmount() {
        return currentTaskAmount;
    }

    public int getCurrentTask() {
        return currentTaskId;
    }

    public int getSlayerPoints() {
        return slayerPoints;
    }

    public int getCoOpSlayerPoints() {
        return coPoints;
    }

    public int getCompletedTasksAmount() {
        return completedTasks;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getCurrentTaskAmount() {
        return currentTaskAmount;
    }

    public SlayerMasterData getCurrentSlayerMaster() {
        return SlayerMasterData.values()[currentMasterId];
    }
}