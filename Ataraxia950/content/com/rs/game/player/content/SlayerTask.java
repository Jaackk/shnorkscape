package com.rs.game.player.content;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.Muspahs;
import com.rs.game.player.DataInterface;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.actions.slayer.elite.EliteNPC;
import com.rs.game.player.actions.slayer.sophanemdungeon.SophanemSlayerNPC;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.lootshare.LootShare;
import com.rs.game.player.content.petperks.PetPerk;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import lombok.Data;
import lombok.Getter;
import lombok.val;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SlayerTask implements Serializable {

    private static final long serialVersionUID = -3885979679549716755L;

    /**
     * Represents the Player's slayer partner.
     */
    private transient Player socialPlayer;
    private final Master master;
    private final int taskId;
    private int taskAmount;
    private int amountKilled;

    /**
     * An instance of the SlayerTask enum
     *
     * @param master
     * @param taskId
     * @param taskAmount
     */
    public SlayerTask(final Master master, final int taskId, final int taskAmount) {
        this.master = master;
        this.taskId = taskId;
        this.taskAmount = taskAmount;
    }

    /**
     * Handles setting Slayer Tasks for the player.
     *
     * @param player The player to set.
     * @param master The master to set.
     * @param resetTask If we should reset the task.
     * @return this
     */
    public static SlayerTask random(final Player player, final Master master, final boolean resetTask) {
        final Player partner = World.getPlayerByDisplayName(player.getSlayerPartner());
        SlayerTask task = null;
        List<SlayerTask> possibleTasks = new ArrayList<>();

// TODO rewrite
        while (true) { // :LUL: totally nothing is wrong with this -arham #theAtaraxiaWay
            int random = -1;

            // pick a random integer, but check if they are lvl 115 dungeoneering if its
            // edimmus
            // and check if they have the task banned
            do {
                random = Utils.random(master.data.length);
            } while (random == -1 || random == 47 && player.getSkills().getLevel(24) < 115 || player.checkBannedTask(random));

            final int requiredLevel = (Integer) master.data[random][1];
            final int maxLevel = (Integer) master.data[random][2];
            if (requiredLevel > 99) {
                val requiredXp = Skills.getXPForLevel(Skills.SLAYER, requiredLevel);
                if (player.getSkills().getXp(Skills.SLAYER) < requiredXp) {
                    continue;
                }
            } else {
                if (player.getSkills().getLevel(Skills.SLAYER) < requiredLevel) {
                    continue;
                }
            }
            if (maxLevel > 99) {
                if (maxLevel != 120) {
                    val maxXp = Skills.getXPForLevel(Skills.SLAYER, maxLevel);
                    if (player.getSkills().getXp(Skills.SLAYER) > maxXp) {
                        continue;
                    }
                }
            } else {
                if (player.getSkills().getLevel(Skills.SLAYER) > maxLevel) {
                    continue;
                }
            }
            task = assignTask(player, master, resetTask, random, partner);
            break;
        }
        return task;
    }

    public static SlayerTask assignTask(Player player, Master master, boolean resetTask, int taskId, Player partnerOptional) {
        final Player partner = partnerOptional == null ? World.getPlayerByDisplayName(player.getSlayerPartner()) : partnerOptional;

        final int minimum = (Integer) master.data[taskId][3];
        final int maximum = (Integer) master.data[taskId][4];
        val task = new SlayerTask(master, taskId, Utils.random(minimum, maximum));
        player.setTask(task);
        if (resetTask) {
            player.setTaskStreak(0);
        }
        if (partner != null) {
            if (partner.getTask() == null || resetTask) {
                partner.setTask(task);
                partner.sendMessage("You and your partner, " + player.getDisplayName() + ", have received <col=ff0000><shad=000000>" + partner.getTask().getTaskAmount() + " x " + partner.getTask().getName(partner).toLowerCase() + "</col></shad> to kill.");
                player.sendMessage("You and your partner " + partner.getDisplayName() + " have received <col=ff0000><shad=000000>" + player.getTask().getTaskAmount() + " x " + player.getTask().getName(player).toLowerCase() + "</col></shad> to kill.");
                if (resetTask) {
                    partner.setTaskStreak(0);
                }
            } else {
                player.setTask(null);
                player.sendMessage("Your partner <col=00ff00>" + partner.getDisplayName() + "</col> is still on a task, they have <col=ff0000>" + partner.getTask().getTaskAmount() + "</col> of <col=00ff00>" + partner.getTask().getName(partner).toLowerCase() + "</col> to kill.");
            }
        }
        if (player.getTask() != null) {
            return task;
        }
        return null;
    }

    /**
     * Calculates how many Slayer points should we give.
     *
     * @param player The player to give.
     * @return The points to give.
     */
    private static int givePoints(final Player player) {
        int points = 20;
        if (player.getEquipment().getRingId() == 13281 || player.getEquipment().getRingId() == 41069 || player.getEquipment().getRingId() == 48483) {
            player.sendMessage(Colors.RED + "Your ring glows as you are rewarded with 20 extra slayer points.");
            points += 20;
        }

        int streak = player.getTaskStreak();
        if (streak % 150 == 0) {
            player.sendMessage(Colors.RED + "You have received 300 extra slayer points for completing 150 tasks in a row.");
            points += 300;
        } else if (streak % 50 == 0) {
            player.sendMessage(Colors.RED + "You have received 100 extra slayer points for completing 50 tasks in a row.");
            points += 100;
        } else if (streak % 10 == 0) {
            player.sendMessage(Colors.RED + "You have received 20 extra slayer points for completing 20 tasks in a row.");
            points += 20;
        }
        if (player.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION)) {
            int newPoints = (int) (points * 0.2);
            player.sendMessage(Colors.RED + "You have received " + newPoints + " extra slayer points for the Per'slay'sion perk.");
            points += newPoints;
        }
        return points;
    }

    /**
     * Handles Slayer Task monster killing.
     *
     * @param killer The killer.
     * @param npc The NPC killed.
     */
    public static void onKill(final Player killer, final NPC npc) {
        /*
         * Initialize partner data, can easily decide things after this by deciding this
         * here
         */
        //if (ThreadLocalRandom.current().nextInt(3) == 0 &&
          //      Master.KURADAL_KEYS.containsKey(npc.getLowercaseName())) {
           // Item loot = new Item(7937, ThreadLocalRandom.current().nextInt(3, 5));
            //if (!LootShare.shareLoot(killer, npc, loot)) {
             //   killer.catchDrop(loot, () -> World.updateGroundItem(loot, new WorldTile(npc), killer, 60, 0, true));
           // }
       // }

        Player partner = null;
        if (killer.getSlayerPartner() != null && World.containsPlayer(killer.getSlayerPartner())) {
            partner = World.getPlayerByDisplayName(killer.getSlayerPartner());
        }

        EliteNPC elite = null;

        /**
         * Uncomment the 'true' in the end to make elite monsters spawn 100% of the
         * time.
         */
        if (!(npc instanceof EliteNPC) && Utils.random(1000) == 0) {
            elite = EliteNPC.spawn(killer, npc, npc);
        }

        boolean target = false;
        if (killer.getTask() != null) {
            target = npc.getDefinitions().name.toLowerCase().contains(killer.getTask().getName(killer).toLowerCase());
        }

        if (killer.getTask() != null) {
            final int lvl = killer.getSkills().getLevelForXp(Skills.SLAYER);
            if (elite == null) {
                if (!(npc instanceof EliteNPC) && Utils.random(50) == 0) {
                    elite = EliteNPC.spawn(killer, npc, npc);
                }
            }

            /**
             * TODO: Always spawn during a task part.
             */

            if (target || doCustomSlayerNPC(killer, npc)) {
                /*
                 * Awards the kills for tasks while keeping slayer in mind INFO: Should
                 * decreaseAmount() here if: partner == null : Killer has no partner or they
                 * aren't online partner != null && !World : Killer has an existing offline
                 * partner "" && hashcode : Killer has an online partner, non-concurrent
                 * SlayerTask objects
                 *
                 */
                /* for skipping tasks */
                boolean skip = false;
                Perk trophytakers = killer.getInventionManager().hasPerk(Perks.TROPHY_TAKERS);
                double r = Math.random();
                int trophyTakers = trophytakers == null ? -1 : (r <= (0.02 * trophytakers.getRank() * (trophytakers.hasIncreasedChance() ? 1.1 : 1.0)) ? 1 : r <= (0.03 * trophytakers.getRank() * (trophytakers.hasIncreasedChance() ? 1.1 : 1.0)) ? 0 : -1);
                if (killer.getCurrentPet() != null) {
                    if (killer.getCurrentPet().getPerks().contains(PetPerk.KURADAMN)) {
                        int tier = PetPerkUtils.getPerkTier(PetPerk.KURADAMN, killer.getCurrentPet());
                        int chance = tier == 1 ? 2 : tier == 2 ? 5 : 10;
                        skip = Utils.random(100) <= chance;
                    }
                }
                if (!skip) {
                    if (partner == null) {
                        if (trophyTakers <= 0)
                            killer.getTask().decreaseAmount();
                        if (trophyTakers == 0)
                            killer.getTask().decreaseAmount();
                        killer.updateSlayerCounterInformation();
                    } else {
                        if (partner.getTask() != null) {
                            if (!World.containsPlayer(partner.getUsername()) || World.containsPlayer(partner.getUsername()) && killer.getTask().hashCode() != partner.getTask().hashCode()) {
                                if (trophyTakers <= 0)
                                    killer.getTask().decreaseAmount();
                                if (trophyTakers == 0)
                                    killer.getTask().decreaseAmount();
                            }
                        } else {
                            if (trophyTakers <= 0)
                                killer.getTask().decreaseAmount();
                            if (trophyTakers == 0)
                                killer.getTask().decreaseAmount();
                        }
                        killer.updateSlayerCounterInformation();
                        partner.updateSlayerCounterInformation();
                    }
                    if (trophyTakers >= 0)
                        killer.sm(trophyTakers == 0 ? "Trophy-taker's perk: this kill has added double to your Slayer task tally." : "Trophy-taker's perk: this kill did not contribute to your Slayer task tally.");
                } else {
                    killer.sm("Thanks to your Kuradamn pet perk your slayer count did not decrease.");
                }

                double experience = killer.getTask().getXPAmount(npc) * (npc instanceof EliteNPC ? 10 : 1);
                if (killer.getCurrentPet() != null) {
                    if (killer.getCurrentPet().getPerks().contains(PetPerk.KURADAMN)) {
                        int tier = PetPerkUtils.getPerkTier(PetPerk.KURADAMN, killer.getCurrentPet());
                        int multiplier = tier == 1 ? 3 : tier == 2 ? 6 : 10;
                        experience = (experience + (experience / 100 * multiplier));
                    }
                }
                /*
                 * if (killer.getPetPerkManager().hasActivePetPerk() &&
                 * killer.getPetPerkManager().ifActivePetHasPerk(PetPerks.KURADAMN)) experience
                 * = (experience + (experience *
                 * killer.getPetPerkManager().getProModifierForPerk(PetPerks.KURADAMN)));
                 */
                killer.getSkills().addXp(Skills.SLAYER, experience);
                if (killer.getAnimations().hasBattleCry && killer.getAnimations().battleCry) {
                    killer.setNextAnimation(new Animation(17072));
                }
                if (killer.getTask().getTaskAmount() <= 0 || partner != null && partner.getTask() != null && partner.getTask().getTaskAmount() <= 1) {
                    double finishedExp = lvl * 9.1 * (killer.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION) ? 1.25 : 1) * (npc instanceof EliteNPC ? 10 : 1);

                    /*
                     * if (killer.getPetPerkManager().hasActivePetPerk() &&
                     * killer.getPetPerkManager().ifActivePetHasPerk(PetPerks.KURADAMN)) finishedExp
                     * = (finishedExp + (finishedExp *
                     * killer.getPetPerkManager().getProModifierForPerk(PetPerks.KURADAMN)));
                     */
                    if (killer.getCurrentPet() != null) {
                        if (killer.getCurrentPet().getPerks().contains(PetPerk.KURADAMN)) {
                            int tier = PetPerkUtils.getPerkTier(PetPerk.KURADAMN, killer.getCurrentPet());
                            int multiplier = tier == 1 ? 3 : tier == 2 ? 6 : 10;
                            finishedExp = (finishedExp + (finishedExp / 100 * multiplier));
                        }
                    }
                    killer.getSkills().addXp(Skills.SLAYER, finishedExp);
                    killer.tasksCompleted++;
                    killer.setTaskStreak(killer.getTaskStreak() + 1);
                    int killerPoints = givePoints(killer);
                    killer.sendMessage("You've completed <shad=000000>" + Colors.GREEN + Utils.getFormattedNumber(killer.getTaskStreak()) + "</col></shad> " + "slayer tasks in a row and gain <shad=000000>" + Colors.GREEN + killerPoints + "</col></shad> slayer points.");
                    killer.setSlayerPoints(killer.getSlayerPoints() + killerPoints);
                    killer.getAchievements().updateProgress(1, AchievementList.COMPLETE_25_SLAYER_TASKS, AchievementList.COMPLETE_50_SLAYER_TASKS, AchievementList.COMPLETE_100_SLAYER_TASKS, AchievementList.COMPLETE_500_SLAYER_TASKS);
                    killer.getPackets().sendMusicEffect(62);
                    killer.setTask(null);
                }
            }
            TaskTab.sendTab(killer);
        }
        if (!World.containsPlayer(killer.getSlayerPartner()) || partner == null) {
            return;
        }
        final boolean withinDistance = partner.withinDistance(killer, 14);
        if (killer.getSlayerPartner() != null) {
            if (partner.getTask() != null) {
                if (target || doCustomSlayerNPC(killer, npc)) {
                    killer.getSkills().addXp(Skills.SLAYER, partner.getTask().getXPAmount(npc) / (withinDistance ? 2 : 5) * (npc instanceof EliteNPC ? 10 : 1));
                    partner.getSkills().addXp(Skills.SLAYER, partner.getTask().getXPAmount(npc) * (npc instanceof EliteNPC ? 10 : 1));
                    partner.getTask().decreaseAmount();
                    killer.updateSlayerCounterInformation();
                    partner.updateSlayerCounterInformation();
                    if (partner.getTask().getTaskAmount() <= 0) {
                        final int lvl = partner.getSkills().getLevelForXp(Skills.SLAYER);
                        partner.getSkills().addXp(Skills.SLAYER, (lvl * 9.1) / (withinDistance ? 2 : 5) * (killer.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION) ? 1.25 : 1) * (npc instanceof EliteNPC ? 10 : 1));
                        partner.tasksCompleted++;
                        partner.setTaskStreak(partner.getTaskStreak() + 1);
                        int partnerPoints = givePoints(partner);
                        partner.sendMessage("You've completed <shad=000000>" + Colors.GREEN + Utils.getFormattedNumber(partner.getTaskStreak()) + "</col></shad> " + "slayer tasks in a row and gain <shad=000000>" + Colors.GREEN + partnerPoints + "</col></shad> slayer points.");
                        partner.setSlayerPoints(partner.getSlayerPoints() + partnerPoints);
                        partner.getAchievements().updateProgress(1, AchievementList.COMPLETE_25_SLAYER_TASKS, AchievementList.COMPLETE_50_SLAYER_TASKS, AchievementList.COMPLETE_100_SLAYER_TASKS, AchievementList.COMPLETE_500_SLAYER_TASKS);
                        partner.getPackets().sendMusicEffect(62);
                        partner.setTask(null);
                    }
                }
            }
        }
        TaskTab.sendTab(killer);
        TaskTab.sendTab(partner);
    }

    /**
     * Opens the Slayer Shop. Interface 1308 is the new ID
     *
     * @param player
     */
    public static void openSlayerShop(final Player player) {
        player.getInterfaceManager().sendInterface(164);
        player.getPackets().sendIComponentText(164, 20, " " + player.getSlayerPoints());
        player.getPackets().sendIComponentText(164, 32, "40 points");
        player.getPackets().sendIComponentText(164, 23, "Assignment");
    }

    public static boolean doCustomSlayerNPC(Player killer, NPC npc) {
        boolean target = false;
        if (killer.getTask() == null) {
            return false;
        }
        if (killer.getTask().getName(killer).equals("Gemstone dragon")) {
            if (npc.getId() == 24170 || npc.getId() == 24171 || npc.getId() == 24172) {
                target = true;
            }
        }
        if (killer.getTask().getName(killer).equals("Ascension member")) {
            if (npc.getId() == 17144 || npc.getId() == 17145 || npc.getId() == 17146 || npc.getId() == 17147) {
                target = true;
            }
        }
        if (killer.getTask().getName(killer).equals("Ganodermic creature")) {
            if (npc.getId() == 14696 || npc.getId() == 14698) {
                target = true;
            }
        }
        if (killer.getTask().getName(killer).equals("Soul devourer")) {
            for (SophanemSlayerNPC.SlayerLevelRequirement slayerLevelRequirement : SophanemSlayerNPC.SlayerLevelRequirement.VALUES) {
                if (slayerLevelRequirement.getSlayerMonsterType() == SophanemSlayerNPC.SlayerLevelRequirement.SlayerMonsterType.SOUL_DEVOURER) {
                    if (slayerLevelRequirement.getNpcId() == npc.getId()) {
                        target = true;
                        break;
                    }
                }
            }
        }
        if (killer.getTask().getName(killer).equals("Corrupted creature")) {
            for (SophanemSlayerNPC.SlayerLevelRequirement slayerLevelRequirement : SophanemSlayerNPC.SlayerLevelRequirement.VALUES) {
                if (slayerLevelRequirement.getSlayerMonsterType() == SophanemSlayerNPC.SlayerLevelRequirement.SlayerMonsterType.CORRUPTED_CREATURE) {
                    if (slayerLevelRequirement.getNpcId() == npc.getId()) {
                        target = true;
                        break;
                    }
                }
            }
        }
        if (killer.getTask().getName(killer).equals("Greater demon")) {
            if (npc.getId() == 6204 || npc.getId() == 6203 || npc.getId() == 83) {
                target = true;
            }
        }
        if (killer.getTask().getName(killer).equals("Blue dragon")) {
            if (npc.getId() == 52 || npc.getId() == 55) {
                target = true;
            }
        }
        if (killer.getTask().getName(killer).equals("Jadinko")) {
            if (npc.getId() == 13820 || npc.getId() == 13822 || npc.getId() == 13821) {
                target = true;
            }
        }
        if (killer.getTask().getName(killer).equals("Aviansie")) {
            if (npc.getId() == 6237 || npc.getId() == 6244 || npc.getId() == 6245 || npc.getId() == 6227 || npc.getId() == 6222 || npc.getId() == 6225 || npc.getId() == 6223 || npc.getId() == 6229 || npc.getId() == 6220 || npc.getId() == 6231 || npc.getId() == 6239 || npc.getId() == 6238) {
                target = true;
            }
        }
        if (killer.getTask().getName(killer).equals("Nihil")) {
            if (npc.getId() == 19146 || npc.getId() == 19147 || npc.getId() == 19148 || npc.getId() == 19149) {
                target = true;
            }
        }
        if (killer.getTask().getName(killer).equals("Muspah")) {
            if (npc instanceof Muspahs || npc.getId() == 19150 || npc.getId() == 19151 || npc.getId() == 19152) {
                target = true;
            }
        }
        if (killer.getTask().getName(killer).equals("Crystal shapeshifter")) {
            if (npc.getId() == 21629 || npc.getId() == 21630 || npc.getId() == 21631 || npc.getId() == 21632) {
                target = true;
            }
        }
        if (killer.getTask().getName(killer).equals("Dagannoth king")) {
            if (npc.getId() == 2455 || npc.getId() == 2881 || npc.getId() == 2882 || npc.getId() == 2883) {
                target = true;
            }
        }
        return target;
    }

    /**
     * Used for handling Kuradals Slayer reward shop.
     *
     * @param player The player.
     * @param componentId The interface componentId.
     */
    public static void handleShop(final Player player, final int interfaceId, final int componentId) {
        if (interfaceId == 164) {
            if (componentId == 16) {
                sendLearn(player);
            }
            if (componentId == 17) { // slayer banned list
                // sendAssignment(player);
                player.getInterfaceManager().sendSlayerList();
                return;
            }
            if (componentId == 24) {
                player.getDialogueManager().startDialogue("SlayerRewards", "xp", 40);
                return;
            }
            if (componentId == 26) {
                player.getDialogueManager().startDialogue("SlayerRewards", "ring", 75);
                return;
            }
            if (componentId == 28) {
                player.getDialogueManager().startDialogue("SlayerRewards", "runes", 35);
                return;
            }
            if (componentId == 37) {
                player.getDialogueManager().startDialogue("SlayerRewards", "bolts", 35);
                return;
            }
            if (componentId == 39) {
                player.getDialogueManager().startDialogue("SlayerRewards", "arrows", 35);
                return;
            }
            player.getInterfaceManager().closeChatBoxInterface();
        }

        /* Slayer banned task list */
        if (interfaceId == 161) {
            if (componentId == 14) {
                sendLearn(player);
                return;
            }
            if (componentId == 15) {
                openSlayerShop(player);
                return;
            }

            /* Resetting task */
            if (componentId == 23 || componentId == 26) {
                if (player.getTask() == null) {
                    player.sendMessage(Colors.SALMON + "You can't reset your slayer task if you don't have one!", true);
                    return;
                }
                if (player.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION) || player.getSlayerPoints() >= 10) {
                    player.closeInterfaces();
                    player.getDialogueManager().startDialogue("SlayerRewards", "reset", player.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION) ? 0 : 10);
                } else {
                    player.sendMessage(Colors.SALMON + "You don't have enough slayer points to reset your task!", true);
                }
                return;

            }
            /* Permanently ban your current task */
            if (componentId == 24 || componentId == 27) {
                if (player.getTask() == null) {
                    player.sendMessage(Colors.SALMON + "You can't ban a slayer task if you don't have one!", true);
                    return;
                }
                if (player.getBannedTasks().size() >= 6) {
                    player.sendMessage(Colors.SALMON + "You cannot ban any more slayer tasks, please remove one first!", true);
                    return;
                }
                if (player.getSlayerPoints() >= (player.getPerkManager().hasPerkActive(DonationPerk.PERSLAYSION) ? 120 : 160)) {
                    player.closeInterfaces();
                    player.getDialogueManager().startDialogue("SlayerRewards", "bantask", 150);
                } else {
                    player.sendMessage(Colors.SALMON + "You don't have enough slayer points to do this!", true);
                }
                return;
            }

            /* Remove a banned task from the task list */
            if (componentId >= 37 && componentId <= 42) {
                final int index = componentId - 37;
                // If the player has no banned tasks at all?
                if (player.getBannedTasks() == null) {
                    player.sendMessage(Colors.SALMON + "You currently have no banned tasks to remove!", true);
                    return;
                }
                // If there is no task in the slot
                if (player.getBannedTasks().size() < index + 1) {
                    player.sendMessage(Colors.SALMON + "There isn't a task to remove from this slot!", true);
                    return;
                }
                // Removes the banned task if it exists
                if (player.getBannedTasks().get(index) != null) {
                    player.getDialogueManager().startDialogue("SlayerRewards", "banreset", index);
                    return;
                } else {
                    player.banTask(false, index);
                }
                return;

            }
            player.getInterfaceManager().closeChatBoxInterface();
        }

        if (interfaceId == 378) {
            if (componentId == 14) { // slayer banned list
                // sendAssignment(player);
                player.getInterfaceManager().sendSlayerList();
            }
            if (componentId == 15) {
                openSlayerShop(player);
            }
            if (componentId == 73) {
                player.getDialogueManager().startDialogue("SlayerRewards", "imbueonyx", 300);
                return;
            }
            if (componentId == 74) {
                player.getDialogueManager().startDialogue("SlayerRewards", "imbue", 300);
                return;
            }
            if (componentId == 75) {
                player.getDialogueManager().startDialogue("SlayerRewards", "pet", 0);
                return;
            }
            if (componentId == 76) {
                player.getDialogueManager().startDialogue("SlayerRewards", "helm", 400);
                return;
            }
            if (componentId == 77) {
                player.getDialogueManager().startDialogue("SlayerRewards", "seed", 200);
                return;
            }
            if (componentId == 78) {
                player.getDialogueManager().startDialogue("SlayerRewards", "torso", 200);
                return;
            }
            player.getInterfaceManager().closeChatBoxInterface();
        }
    }

    public static void sendAssignment(final Player player) {
        player.closeInterfaces();
        player.getDialogueManager().startDialogue("KuradalAssignment", 9085); // TODO
    }

    public static void sendLearn(final Player player) {
        player.getInterfaceManager().sendInterface(378);
        player.getPackets().sendIComponentText(378, 79, " " + player.getSlayerPoints());
        player.getPackets().sendIComponentText(378, 82, "Assignment");
        player.getPackets().sendItemOnIComponent(378, 102, 24511, 1); // LAST
        // SLOT
        player.getPackets().sendItemOnIComponent(378, 93, 6731, 1); // FIFTH
        // SLOT
        // START
        player.getPackets().sendItemOnIComponent(378, 94, 6733, 1);
        player.getPackets().sendItemOnIComponent(378, 95, 6735, 1);
        player.getPackets().sendItemOnIComponent(378, 96, 6737, 1); // FIFTH
        // SLOT END
        player.getPackets().sendItemOnIComponent(378, 92, 6575, 1); // FOURTH
        // SLOT
        player.getPackets().sendItemOnIComponent(378, 103, 13263, 1); // slayer
        // helmet
        player.getPackets().sendItemOnIComponent(378, 104, 13263, 1); // slayer
        // helmet
        player.getPackets().sendItemOnIComponent(378, 105, 32625, 1); // SECOND
        // SLOT
        player.getPackets().sendItemOnIComponent(378, 101, 10551, 1); // THIRD
        // SLOT
        player.getPackets().sendIComponentText(378, 83, "Slayer helmet");
        player.getPackets().sendIComponentText(378, 84, "Imbue onyx ring");
        player.getPackets().sendIComponentText(378, 85, "Imbue ring");
        player.getPackets().sendIComponentText(378, 86, "Slayer pets");
        player.getPackets().sendIComponentText(378, 87, "Crystal Weapon Seed");
        player.getPackets().sendIComponentText(378, 88, "Fighter's torso");
        player.getPackets().sendIComponentText(378, 90, "400 points");
        player.getPackets().sendIComponentText(378, 91, "300 points");
        player.getPackets().sendIComponentText(378, 97, "300 points");
        player.getPackets().sendIComponentText(378, 99, "200 points");
        player.getPackets().sendIComponentText(378, 100, "200 points");
        player.getPackets().sendIComponentText(378, 98, "Points may vary");
    }

    /**
     * InterfaceId 1308 hideComponent comoponentId 12 = co-op use food on partner
     * componentId 42 = hides the navigationButtons componentId 472 = co-op aquanite
     * pet componentId 493 = co-op potion effects componentId 495 = co-op strykewyrm
     * pet
     */
    public static void handleNewShop(final Player player) {

    }

    public String getName(final Player player) {
        return (String) master.data[taskId][0];
    }

    public int getTaskId() {
        return taskId;
    }

    public int getTaskAmount() {
        return taskAmount;
    }

    public void decreaseAmount() {
        taskAmount--;
    }

    public int getXPAmount(final NPC npc) {
        final Object obj = master.data[taskId][5];
        final float npcHP = npc.getHitpoints();

        if (obj instanceof Double) {
            return (int) ((npcHP / 10) + (int) Math.round((Double) obj));
        }
        if (obj instanceof Integer) {
            return (int) (npcHP / 10) + (Integer) obj;
        }
        return 0;
    }

    public Master getMaster() {
        return master;
    }

    public int getAmountKilled() {
        return amountKilled;
    }

    public int getAmountLeft() {
        return taskAmount - amountKilled;
    }

    public void setAmountKilled(final int amountKilled) {
        this.amountKilled = amountKilled;
    }

    public Player getSocialPlayer() {
        return socialPlayer;
    }

    public static void displayOptions(Player player) {
        int level = player.getSkills().getLevel(Skills.SLAYER);
        DataInterface inter = new DataInterface("Slayer Tasks");
        for (SlayerTaskData data : Master.KURADAL_VALUES) {
            if (data.reqLevel > level) {
                continue;
            }
            inter.add(Colors.BLUE + "Level " + data.reqLevel + " ~  " + Colors.DARK_RED + data.name);
        }
        inter.show(player);
    }


    @Data
    public static final class SlayerTaskData {
        public final String key;
        public final String name;
        public final int taskId;
        public final int reqLevel;
        public final int maxLevel;
        public final int minimum;
        public final int maximum;
        public final double xp;
    }

    /**
     * An enum containing all available Slayer Tasks.
     *
     * @author Noel
     */
    public enum Master {
        KURADAL(9085, new Object[][]{{"Rock crab", 1, 20, 10, 20, 10.5}, {"Cow", 1, 20, 10, 20, 10.5}, {"Yak", 1, 30, 10, 20, 15},
                {"Crawling hand", 5, 35, 15, 30, 13.5},
                {"Cave crawler", 10, 40, 20, 35, 16.5}, {"Rockslug", 15, 50, 20, 50, 14}, {"Jelly", 35, 70, 35, 70, 38},
                {"Pyrefiend", 30, 65, 30, 70, 20.1}, {"Cockatrice", 25, 75, 30, 70, 14.9}, {"Infernal mage", 45, 90, 30, 70, 61},
                {"Abyssal demon", 85, 120, 70, 200, 203}, {"Nechryael", 80, 120, 50, 150, 199.1}, {"Aberrant spectre", 60, 120, 50, 150, 64.6},
                {"Waterfiend", 60, 120, 50, 150, 227.8}, {"Gargoyle", 75, 120, 50, 120, 148.9}, {"Dark beast", 90, 120, 50, 150, 221.4},
                {"Bloodveld", 50, 90, 50, 120, 50.513}, {"Green dragon", 60, 120, 60, 150, 110.81}, {"Glacor", 95, 120, 10, 30, 700},
                {"Fungal rodent", 25, 60, 20, 40, 50}, {"Grifolaroo", 82, 95, 40, 60, 300.2}, {"Grifolapine", 88, 97, 40, 60, 255.46},
                {"Ganodermic creature", 95, 120, 50, 125, 300.34}, {"Muspah", 76, 120, 70, 120, 356.18}, {"Frost dragon", 90, 120, 25, 120, 135.83},
                {"Ice strykewyrm", 93, 120, 30, 85, 400.76}, {"Jungle strykewyrm", 73, 120, 30, 85, 190}, {"Desert strykewyrm", 77, 120, 30, 85, 320.6},
                {"Iron dragon", 50, 120, 40, 80, 160.5}, {"Steel dragon", 65, 120, 40, 50, 245}, {"Mithril dragon", 75, 120, 45, 55, 375.50},
                {"Adamant dragon", 75, 120, 40, 50, 501.15}, {"Hellhound", 55, 120, 60, 120, 88.1}, {"Greater demon", 50, 120, 60, 150, 102},
                {"Bronze dragon", 30, 90, 30, 110, 145.75}, {"Hill giant", 5, 50, 40, 80, 20.1}, {"Moss giant", 20, 60, 40, 100, 29.5},
                {"Fire giant", 50, 85, 40, 120, 105.7}, {"Turoth", 55, 90, 60, 100, 10}, {"Basilisk", 40, 90, 60, 100, 42.1},
                {"Kurask", 70, 120, 60, 100, 66.7}, {"Black demon", 70, 95, 60, 120, 215.9}, {"Blue dragon", 30, 85, 40, 120, 92.5},
                {"Lesser demon", 50, 75, 60, 130, 47.13}, {"Rune dragon", 90, 120, 45, 74, 600}, {"Aviansie", 40, 120, 100, 200, 300.2},
                {"Ascension member", 81, 120, 90, 200, 170}, {"Aquanite", 78, 120, 120, 240, 150.1}, {"Edimmu", 90, 120, 170, 265, 450.15},
                {"Jadinko", 80, 120, 120, 200, 56.2}, {"Skeleton", 5, 65, 30, 60, 18.243}, {"Fungal mage", 25, 70, 20, 70, 60},
                {"Mature Grotworm", 50, 120, 50, 120, 243.6}, {"Nihil", 76, 120, 40, 90, 306.18}, {"Lava strykewyrm", 94, 120, 20, 60, 575.84},
                {"Airut", 92, 120, 50, 110, 550}, {"Celestial dragon", 90, 120, 40, 100, 555.5}, {"Kal'gerion demon", 90, 120, 40, 80, 555.5},
                {"Automaton", 67, 120, 30, 60, 310.5}, {"Camel warrior", 96, 120, 15, 40, 440.5}, {"Ripper demon", 96, 120, 65, 120, 530},
                {"Crystal shapeshifter", 80, 120, 20, 70, 310.8}, {"Gemstone dragon", 95, 101, 40, 80, 567.1}, {"Dagannoth king", 80, 120, 30, 120, 250},
                {"Corrupted creature", 88, 120, 188, 223, 322.1}, {"Soul devourer", 105, 120, 188, 223, 368.5}});

        public static final ImmutableList<SlayerTaskData> KURADAL_VALUES;
        public static final ImmutableMap<String, SlayerTaskData> KURADAL_KEYS;
        public static final ImmutableMap<Integer, SlayerTaskData> KURADAL_TASK_IDS;

        static {
            List<SlayerTaskData> list = new ArrayList<>();
            int taskId = 0;
            for (Object[] data : Master.KURADAL.data) {
                String key = data[0].toString().toLowerCase();
                Number exp = (Number) data[5];
                list.add(new SlayerTaskData(key, data[0].toString(), taskId++, (Integer) data[1], (Integer) data[2], (Integer) data[3], (Integer) data[4], exp.doubleValue()));
            }
            list.sort(Comparator.comparingInt(o -> o.reqLevel));
            KURADAL_VALUES = ImmutableList.copyOf(list);
            Map<String, SlayerTaskData> keyMap = new LinkedHashMap<>();
            list.forEach(it -> keyMap.put(it.key, it));
            KURADAL_KEYS = ImmutableMap.copyOf(keyMap);

            Map<Integer, SlayerTaskData> taskIdMap = new LinkedHashMap<>();
            list.forEach(it -> taskIdMap.put(it.taskId, it));
            KURADAL_TASK_IDS = ImmutableMap.copyOf(taskIdMap);
        }

        /**
         * name, min slay level, max slay level, min task amount, max task, amount, exp
         */
        private final int id;
        @Getter
        private final Object[][] data;

        Master(final int id, final Object[][] data) {
            this.id = id;
            this.data = data;
        }

        public static Master forId(final int id) {
            for (final Master master : Master.values()) {
                if (master.id == id) {
                    return master;
                }
            }
            return null;
        }

        public int getId() {
            return id;
        }
    }
}