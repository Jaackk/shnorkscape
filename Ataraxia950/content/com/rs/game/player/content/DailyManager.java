package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.VisWaxManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Random;

/**
 * @author David (Chryonic)
 */
public class DailyManager implements Serializable {
    private static final long serialVersionUID = -8035331642599855251L;
    private transient Player player;
    private DailyTasks currentTask;
    @Getter
    private int taskAmount;
    private long lastDailyTask;
    @Getter
    private int tasksCompleted;
    @Getter
    @Setter
    private boolean hasDaily;

    public enum DailyTasks {

        // Herblore

        // Mining

        MINE_COPPER(14, 1, 50, 102, 800, 436),
        MINE_TIN(14, 1, 49, 108, 800, 438),
        MINE_IRON(14, 15, 51, 106, 800, 440),
        MINE_MITHRIL(14, 55, 52, 86, 800, 447),
        MINE_ADAMANTITE(14, 70, 34, 75, 800, 449),
        MINE_RUNE(14, 85, 24, 37, 800, 451),

        // Firemaking

        BURN_LOGS(11, 1, 50, 100, 800, 1511),
        BURN_OAK_LOGS(11, 15, 50, 100, 800, 1521),
        BURN_WILLOW_LOGS(11, 30, 50, 100, 800, 1519),
        BURN_MAPLE_LOGS(11, 45, 50, 100, 800, 1517),
        BURN_YEW_LOGS(11, 60, 50, 100, 800, 1515),
        BURN_MAGIC_LOGS(11, 75, 50, 100, 800, 1513),

        // Woodcutting

        CUT_LOGS(8, 1, 50, 100, 800, 1511),
        CUT_OAK_LOGS(8, 15, 50, 100, 800, 1521),
        CUT_WIILOW_LOGS(8, 30, 50, 100, 800, 1519),
        CUT_MAPLE_LOGS(8, 45, 50, 100, 800, 1517),
        CUT_YEW_LOGS(8, 60, 50, 100, 800, 1515),
        CUT_MAGIC_LOGS(8, 75, 50, 100, 800, 1513),

        // Cooking

        COOK_SHRIMP(7, 1, 50, 100, 800, 315),
        COOK_LOBSTER(7, 40, 50, 100, 800, 379),
        COOK_SWORDFISH(7, 45, 50, 100, 800, 373),
        COOK_SHARK(7, 80, 50, 100, 800, 385),

        // Fishing

        FISH_SHRIMP(10, 1, 50, 100, 800, 317),
        FISH_ANCHOVIES(10, 15, 50, 100, 800, 321),
        FISH_LOBSTER(10, 40, 50, 100, 800, 377),
        FISH_SWORDFISH(10, 35, 50, 100, 800, 371),
        FISH_SHARK(10, 76, 15, 35, 800, 383),

        // Smithing

        SMITH_BRONZE_BAR(13, 1, 45, 104, 800, 2349),
        SMITH_IRON_BAR(13, 15, 52, 102, 800, 2351),
        SMITH_STEEL_BAR(13, 30, 51, 99, 800, 2353),
        Smith_Mithbar(13, 50, 57, 102, 800, 2359),
        SMITH_ADDY_BAR(13, 70, 46, 82, 800, 2361),
        Smith_Runebar(13, 85, 9, 16, 800, 2363),

        // Thieveing

        Steal_CraftingStall(17, 1, 45, 104, 800, 4874),
        Steal_SilkStall(17, 20, 52, 102, 800, 34383),
        Steal_WineStall(17, 22, 51, 99, 800, 14011),
        Steal_FurStall(17, 35, 57, 102, 800, 34387),
        Steal_SpiceStall(17, 65, 46, 100, 800, 34386),
        Steal_GemStall(17, 75, 50, 100, 800, 34385),
        Steal_ScimmyStall(17, 80, 50, 100, 800, 4878);

        private final int id;
        private final int min;
        private final int max;
        private final int xp;
        private final int levelRequried;
        private final int skill;

        DailyTasks(int skill, int levelRequried, int min, int max, int xp, int id) {
            this.levelRequried = levelRequried;
            this.skill = skill;
            this.min = min;
            this.max = max;
            this.id = id;
            this.xp = xp;
        }

        public int getId() {
            return id;
        }

        public int getMin() {
            return min;
        }

        public int getMax() {
            return max;
        }

        public int getXp() {
            return xp;
        }

        public int getSkill() {
            return skill;
        }

        public int getlvlRequired() {
            return levelRequried;
        }
    }

    public int getItemID() {
        return currentTask.getId();
    }

    public double addExp() {
        return currentTask.getXp();
    }

    public int getSkill() {
        return currentTask.getSkill();
    }

    public DailyTasks getTask() {
        return currentTask;
    }

    public int getAmount() {
        return taskAmount;
    }

    public String getName() {
        int i = getItemID();
        ItemDefinitions def = ItemDefinitions.getItemDefinitions(i);
        return def.getName();
    }

    public String getNames() {
        int i = getItemID();
        ObjectDefinitions def = ObjectDefinitions.getObjectDefinitions(i);
        return def.getName();
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void resetTask() {
        setCurrentTask(null, 0);
        addNewDaily(false);
    }

    public void giveDailyTask() {
        if ((Utils.currentTimeMillis() - lastDailyTask) < (24 * 60 * 60 * 1000)) {
           // player.sendMessage("Time until your next daily challenge task: " + format((24 * 60 * 60 * 1000) - (Utils.currentTimeMillis() - lastDailyTask)));
            return;
        }
        lastDailyTask = Utils.currentTimeMillis();
        this.tasksCompleted = 0;
        player.getVisWaxManager().setDailyResets(0);
        if (player.getSkills().getTotalLevel() >= 350 && Utils.getHoursPlayed(player.getTotalPlayTime()) >= 15)
            addNewDaily(false);
    }

    public String displayTask() {
        String message = "[Daily Task] <col=FF003C>You do not have a task.";
        switch (player.getTaskId()) {
            case 7:
                message = "[Daily Task] <col=FFD200>You must cook " + player.getDailyManager().getAmount() + " " + player.getDailyManager().getName() + "'s.";
                break;
            case 8:
                message = "[Daily Task] <col=FFD200>You must cut " + player.getDailyManager().getAmount() + " " + player.getDailyManager().getName() + ".";
                player.setSkillingTask(8);
                break;
            case 10:
                message = "[Daily Task] <col=FFD200>You must fish " + player.getDailyManager().getAmount() + " " + player.getDailyManager().getName() + "'s.";
                break;
            case 11:
                message = "[Daily Task] <col=FFD200>You must burn " + player.getDailyManager().getAmount() + " " + player.getDailyManager().getName() + ".";
                player.setSkillingTask(11);
                break;
            case 13:
                message = "[Daily Task] <col=FFD200>You must smelt " + player.getDailyManager().getAmount() + " " + player.getDailyManager().getName() + "'s.";
                break;
            case 14:
                message = "[Daily Task] <col=FFD200>You must mine " + player.getDailyManager().getAmount() + " " + player.getDailyManager().getName() + "'s.";
                break;
            case 17:
                message = "[Daily Task] <col=FFD200>You must steal " + player.getDailyManager().getAmount() + " times from a " + player.getDailyManager().getNames() + ".";
                break;
        }

        return message;
    }

    public void addNewDaily(boolean reset) {
        if (this.getTasksCompleted() >= 3 || player.getVisWaxManager().getDailyResets() >= 3) {
            player.sm("You have completed 3 tasks today. You will have to wait until tomorrow to get your next one! Time left: " + format((24 * 60 * 60 * 1000) - (Utils.currentTimeMillis() - lastDailyTask)));
            return;
        }

        if (reset) {
            currentTask = null;
            hasDaily = false;

            player.setTaskId(-1);
            player.getDailyManager().getTask(1);
        } else {
            if (isHasDaily()) {
                player.sm(player.getDailyManager().displayTask());
            } else {
                player.setTaskId(-1);
                player.getDailyManager().getTask(1);
            }
        }

    }

    private String format(long time) {
        final int sec = (int) (time / 1000), h = sec / 3600, m = sec / 60 % 60, s = sec % 60;
        return (h < 1 ? "" : (h < 10 ? "0" + h : h) + "h:") + ((m < 1) && h < 1 ? "" : (m < 10 ? "0" + m : m) + "m:")
                + ((s < 1) && m < 1 ? "" : (s < 10 ? "0" + s + "s" : s + "s"));
    }

    public void processTask() {
       /* this.taskAmount--;
        player.setDailyAmount(this.taskAmount);
        if (player.getTaskId() == 17) {
            player.sendMessage("<col=00FFA8>You only have to steal " + taskAmount + " more times to complete your daily task!", true);

        } else {
            player.sendMessage("<col=00FFA8>You only have " + taskAmount + " <col=ff0000>" + player.getDailyManager().getName() + "</col><col=00FFA8> left to go to complete your daily task!", true);
        }
        if (taskAmount <= 0) {
            Item reward = Utils.random(9) >= 7 ? new Item(23716, 1) : new Item(995, 1500000);
            player.sm("<col=00FFA8>You have successfully completed your daily task!");
            player.sm("<col=00FFA8>You have received additional exp and a reward!");
            player.getBank().addItem(reward, true);
            player.getSkills().addXp(getSkill(), addExp());
            player.addDailyTasksCompleted();
            player.sendMessage("You've completed a daily task; tasks done: " + Colors.RED + Utils.getFormattedNumber(player.getDailyTasksCompleted()) + "</col>.");
            player.getAchievements().updateProgress(1, AchievementList.COMPLETE_5_DAILY_TASKS, AchievementList.COMPLETE_20_DAILY_TASKS, AchievementList.COMPLETE_50_DAILY_TASKS, AchievementList.COMPLETE_100_DAILY_TASKS);
            sendVisWaxReward();
            this.tasksCompleted++;
            player.dailyTask = null;
            this.setHasDaily(false);
            player.setTaskId(-1);
            resetTask();
        }*/
    }

    private void sendVisWaxReward() {
        boolean inventory = true;
        Item viswax = new Item(VisWaxManager.VISWAX_ITEM_ID, 5);
        if (!player.getInventory().addItem(viswax)) {
            inventory = false;
            player.getBank().addItem(viswax, true);
        }

        player.sendMessage("Congratulations! You receive " + viswax.getAmount() + " Vis Wax for completing your daily challenging task. It has been added to your " + (inventory ? " inventory." : " bank."));
    }

    public void setCurrentTask(DailyTasks task, int amount) {
        this.currentTask = task;
        this.taskAmount = amount;
    }

    public void getTask(int mode) {
        if (currentTask == null && !isHasDaily()) {
            int pick = new Random().nextInt(DailyTasks.values().length);
            final DailyTasks task = isGoodTask(DailyTasks.values()[pick]);
            int amount = Utils.random(task.getMin(), task.getMax());
            setCurrentTask(task, amount);
            this.setHasDaily(true);
            player.setDailyAmount(amount);
            player.setTaskId(getSkill());
            player.setTaskItemId(task.getId());
            player.dailyTask = task;
            player.sm(displayTask());
        } else {
            player.sm("You already have a task.");
        }
        return;
    }

    private DailyTasks isGoodTask(DailyTasks dailyTasks) {
        /** Level 1 - 50 replacement tasks. */
        int[] easyReplacements = new int[]{0, 6, 12, 18, 22, 27, 33};
        /** Level 50 - 99 replacement tasks. */
        int[] mediumReplacements = new int[]{3, 9, 15, 21, 25, 30, 36};
        /**
         * Blacklisted tasks until system is redone.
         */
        for (DailyTasks tsk : Settings.BLACKLISTED_DAILY_TASKS) {
            if (dailyTasks != tsk) {
                continue;
            } else
                return (DailyTasks.values()[easyReplacements[Utils.random(easyReplacements.length)]]);

        }
        Logger.getGlobal().info("Level: " + player.getSkills().getLevel(dailyTasks.getSkill()) + ">= Required Min: " + dailyTasks.getlvlRequired());
        Logger.getGlobal().info("Level: " + player.getSkills().getLevel(dailyTasks.getSkill()) + "<= Required Max: " + (10 + dailyTasks.getlvlRequired()));
        if (player.getSkills().getLevel(dailyTasks.getSkill()) >= dailyTasks.getlvlRequired() && player.getSkills().getLevel(dailyTasks.getSkill()) <= (25 + dailyTasks.getlvlRequired())) {
            if (dailyTasks == DailyTasks.Steal_GemStall && !player.isDonator()) {
                return (DailyTasks.values()[easyReplacements[Utils.random(easyReplacements.length)]]);
            } else {
                return dailyTasks;
            }
        } else {
            return player.getSkills().getLevel(dailyTasks.getSkill()) >= 50 ?
                    (DailyTasks.values()[mediumReplacements[Utils.random(mediumReplacements.length)]]) : (DailyTasks.values()[easyReplacements[Utils.random(easyReplacements.length)]]);
        }
    }

    /**
     * Gets the lastDailyTask.
     *
     * @return the lastDailyTask
     */
    public long getLastDailyTask() {
        return lastDailyTask;
    }
}