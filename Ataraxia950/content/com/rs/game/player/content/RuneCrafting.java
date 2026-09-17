package com.rs.game.player.content;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.SkillsDialogue.ItemNameFilter;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.skillingcontracts.impl.RunecraftingContractList;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

public final class RuneCrafting {

    public final static int[] LEVEL_REQ = {1, 25, 50, 75};

    public final static int RUNE_ESSENCE = 1436, PURE_ESSENCE = 7936, AIR_TIARA = 5527, MIND_TIARA = 5529, WATER_TIARA = 5531, BODY_TIARA = 5533, EARTH_TIARA = 5535, FIRE_TIARA = 5537, COSMIC_TIARA = 5539, NATURE_TIARA = 5541, CHAOS_TIARA = 5543, LAW_TIARA = 5545, DEATH_TIARA = 5547, BLOOD_TIARA = 5549, SOUL_TIARA = 5551, ASTRAL_TIARA = 9106, OMNI_TIARA = 13655;

    public static final int[] POUCH_SIZE = {5, 10, 15, 20};


    // Toggle: false = traditional one-click craft (no looping), true = looping enabled
    private static volatile boolean RC_LOOPING_ENABLED = false;
    public static void setRunecraftingLooping(boolean enabled) { RC_LOOPING_ENABLED = enabled; }



    /**
     * Blank Tiara infusing.
     */
    public static final int[] OBJECTS = {2478, 2481, 2482, 2480, 2483, 2479, 30624, 2487, 2484, 2488, 2485, 2478, 2486};
    private static final int[] TIARA = {AIR_TIARA, EARTH_TIARA, FIRE_TIARA, WATER_TIARA, BODY_TIARA, MIND_TIARA, BLOOD_TIARA, CHAOS_TIARA, COSMIC_TIARA, DEATH_TIARA, LAW_TIARA, SOUL_TIARA, NATURE_TIARA};
    public static final int BASE_RC_MULTIPLIER = 5;

    public static final int[] AIR_RUNE_MULTIPLIERS = {
            11, 2, 22, 3, 33, 4, 44, 5, 55, 6, 66, 7, 77, 8, 88, 9, 99, 10
    };
    public static final int[] MIND_RUNE_MULTIPLIERS = {
            14, 2, 28, 3, 42, 4, 56, 5, 70, 6, 84, 7, 98, 8, 112, 9
    };
    public static final int[] WATER_RUNE_MULTIPLIERS = {
            19, 2, 38, 3, 57, 4, 76, 5, 95, 6, 114, 7
    };
    public static final int[] EARTH_RUNE_MULTIPLIERS = {
            26, 2, 52, 3, 78, 4, 104, 5
    };
    public static final int[] FIRE_RUNE_MULTIPLIERS = {
            35, 2, 70, 3, 105, 4
    };
    public static final int[] BODY_RUNE_MULTIPLIERS = {
            46, 2, 92, 3
    };
    public static final int[] COSMIC_RUNE_MULTIPLIERS = {
            59, 2, 118, 3
    };
    public static final int[] CHAOS_RUNE_MULTIPLIERS = {
            74, 2
    };
    public static final int[] ASTRAL_RUNE_MULTIPLIERS = {
            82, 2
    };
    public static final int[] NATURE_RUNE_MULTIPLIERS = {
            91, 2
    };
    public static final int[] LAW_RUNE_MULTIPLIERS = {
            110, 2
    };
    public static final int[] DEATH_RUNE_MULTIPLIERS = {
            131, 2
    };
    public static final int[] BLOOD_RUNE_MULTIPLIERS = {
            154, 2
    };
    public static final int[] ARMADYL_RUNE_MULTIPLIERS = {
            72, 7, 77, 8, 88, 9, 99, 10
    };

    public static int getRuneMultiplier(String runeName) {
        int multiplier = BASE_RC_MULTIPLIER;
        switch (runeName) {
            case "cosmic rune":
            case "chaos rune":
            case "nature rune":
            case "law rune":
            case "death rune":
                multiplier -= 3;
                break;
            case "blood rune":
            case "astral rune":
            case "soul rune":
                multiplier -= 5;
                break;
        }
        if(multiplier <= 0) {
            multiplier = 1;
        }
        return multiplier;
    }

    private static double getAltarExperience(int rune, double fallback) {
        if (rune == -1)
            rune = 556;
        double standard = getStandardAltarExperience(rune);
        if (standard <= 0)
            return fallback;
        return Math.max(standard, fallback);
    }

    public static double getStandardAltarExperience(int rune) {
        switch (rune) {
            case 556:
                return 5;
            case 558:
                return 5.5;
            case 555:
                return 6;
            case 557:
                return 6.5;
            case 554:
                return 7;
            case 559:
                return 7.5;
            case 564:
                return 8;
            case 562:
                return 8.5;
            case 9075:
                return 8.7;
            case 561:
                return 9;
            case 563:
                return 9.5;
            case 560:
                return 10;
            case 565:
                return 10.5;
            case 566:
                return 220;
            case 21773:
                return 10;
            default:
                return 0;
        }
    }

    public static int getRuneOutput(int essenceUsed, int actualLevel, int altarLevel, int... multipliers) {
        int guaranteed = 1;
        int previousLevel = altarLevel;
        int nextLevel = -1;
        for (int i = 0; i < multipliers.length - 1; i += 2) {
            int threshold = multipliers[i];
            int multiplier = multipliers[i + 1];
            if (actualLevel >= threshold) {
                guaranteed = multiplier;
                previousLevel = threshold;
            } else {
                nextLevel = threshold;
                break;
            }
        }
        int output = essenceUsed * guaranteed;
        if (nextLevel != -1) {
            int span = Math.max(1, nextLevel - previousLevel);
            int progress = Math.max(0, actualLevel - previousLevel);
            int extraChance = Math.min(60, (progress * 60) / span);
            for (int i = 0; i < essenceUsed; i++) {
                if (Utils.random(100) < extraChance)
                    output++;
            }
        }
        return output;
    }

    public static boolean rollRunespanSiphonSuccess(int level, int requiredLevel, boolean node) {
        int chance = (node ? 55 : 65) + Math.max(0, level - requiredLevel) * 2;
        return Utils.random(100) < Math.min(95, chance);
    }

    public static double getRunespanFailureXp() {
        return Utils.random(2) == 0 ? 0 : 2;
    }

    public static double getRunecraftingXpModifier(Player player) {
        double modifier = runecrafterSuit(player);
        if (hasEtherealOutfit(player))
            modifier *= 1.06;
        return modifier;
    }

    public static void checkPouch(Player p, int i) {
        if (i < 0)
            return;
        p.sendMessage("This pouch has " + p.getPouches()[i] + " rune essences in it.", false);
    }



    // Add inside RuneCrafting (same class/file)

    private static int craftEssenceOnce(Player player, int rune, int level, double experience,
                                        boolean pureEssOnly, int essCap, boolean doLock,
                                        int... multipliers) {
        if (player.isDead() || player.hasFinished()) return 0;

        int actualLevel = player.getSkills().getLevel(Skills.RUNECRAFTING);
        if (actualLevel < level) {
            player.getDialogueManager().startDialogue("SimpleMessage",
                    "You need a Runecrafting level of " + level + " to craft this rune.");
            return 0;
        }
        if (rune == -1) rune = 556;

        int pure = player.getInventory().getItems().getNumberOf(PURE_ESSENCE);
        int normal = player.getInventory().getItems().getNumberOf(RUNE_ESSENCE);
        int total = pure + (!pureEssOnly ? normal : 0);

        if (rune == 21773) {
            int shards = player.getInventory().getAmountOf(21774);
            total = Math.min(total, shards);
        }

        // NEW: tell the player if they have no essence
        if (total <= 0) {
            player.getDialogueManager().startDialogue("SimpleMessage",
                    "You don't have " + (pureEssOnly ? "pure" : "rune") + " essence.");
            return 0;
        }

        int essToUse = Math.min(essCap, total);
        int usePure = Math.min(pure, essToUse);
        int useNormal = essToUse - usePure;
        if (usePure > 0) player.getInventory().deleteItem(PURE_ESSENCE, usePure);
        if (useNormal > 0) player.getInventory().deleteItem(RUNE_ESSENCE, useNormal);
        int essUsed = usePure + useNormal;
        if (essUsed <= 0) return 0;

        if (rune == 21773) player.getInventory().deleteItem(21774, essUsed);
        if (doLock) player.lock(5);

        double totalXp = getAltarExperience(rune, experience) * essUsed;
        totalXp *= getRunecraftingXpModifier(player);

        player.getSkills().addXp(Skills.RUNECRAFTING, totalXp);

        int out = getRuneOutput(essUsed, actualLevel, level, multipliers);

        String runeName = ItemDefinitions.getItemDefinitions(rune).getName().toLowerCase();

        // Per-rune tracking you already do
        String message = null;
        if (runeName.contains("air")) { player.addAirRunesMade(out); message = com.rs.utils.Colors.RED + Utils.getFormattedNumber(player.getAirRunesMade()) + "</col>"; }
        else if (runeName.contains("water")) { player.addWaterRunesMade(out); message = Colors.RED + Utils.getFormattedNumber(player.getWaterRunesMade()) + "</col>"; }
        else if (runeName.contains("earth")) { player.addEarthRunesMade(out); message = Colors.RED + Utils.getFormattedNumber(player.getEarthRunesMade()) + "</col>"; }
        else if (runeName.contains("mind")) { player.addMindRunesMade(out); message = Colors.RED + Utils.getFormattedNumber(player.getMindRunesMade()) + "</col>"; }
        else if (runeName.contains("fire")) { player.addFireRunesMade(out); message = Colors.RED + Utils.getFormattedNumber(player.getFireRunesMade()) + "</col>"; }
        else if (runeName.contains("body")) { player.addBodyRunesMade(out); message = Colors.RED + Utils.getFormattedNumber(player.getBodyRunesMade()) + "</col>"; }
        else if (runeName.contains("cosmic")) { player.addCosmicRunesMade(out); message = Colors.RED + Utils.getFormattedNumber(player.getCosmicRunesMade()) + "</col>"; }
        else if (runeName.contains("chaos")) { player.addChaosRunesMade(out); message = Colors.RED + Utils.getFormattedNumber(player.getChaosRunesMade()) + "</col>"; }
        else if (runeName.contains("nature")) { player.addNatureRunesMade(out); message = Colors.RED + Utils.getFormattedNumber(player.getNatureRunesMade()) + "</col>"; }
        else if (runeName.contains("law")) { player.addLawRunesMade(out); message = Colors.RED + Utils.getFormattedNumber(player.getLawRunesMade()) + "</col>"; }
        else if (runeName.contains("death")) { player.incrementDeathRunesMade(out); message = Colors.RED + Utils.getFormattedNumber(player.getDeathRunesMade()) + "</col>"; }
        else if (runeName.contains("blood")) { player.addBloodRunesMade(out); message = Colors.RED + Utils.getFormattedNumber(player.getBloodRunesMade()) + "</col>"; }
        else if (runeName.contains("astral")) { player.addAstralRunesMade(out); message = Colors.RED + Utils.getFormattedNumber(player.getAstralRunesMade()) + "</col>"; }
        else if (runeName.contains("soul")) { player.addSoulRunesMade(out); message = Colors.RED + Utils.getFormattedNumber(player.getSoulRunesMade()) + "</col>"; }

        // Your custom global multiplier by rune name

        player.addRunesMade(out);
        RunecraftingContractList.listen(player, rune, out);
        player.setNextGraphics(new Graphics(186, 0, 110));
        player.setNextAnimation(new Animation(791));
        player.getInventory().addItem(rune, out);

        player.sendMessage("You bind the temple's power into " + runeName + "s"
                + (message != null ? ("; runes made: " + message + ";") : "")
                + " total: " + Colors.RED + Utils.getFormattedNumber(player.getRunesMade()) + "</col>.", true);

        if (rune == 565) {
            player.getAchievements().updateProgress(out, AchievementList.RUNECRAFT_500_BLOOD_RUNES);
        }
        return essUsed; // how many ess we consumed this cycle
    }

    private static boolean emptyAnyPouchToInventory(Player p) {
        // drain largest first
        for (int i = POUCH_SIZE.length - 1; i >= 0; i--) {
            int toAdd = Math.min(p.getPouches()[i], p.getInventory().getFreeSlots());
            if (toAdd > 0) {
                p.getInventory().addItem(RUNE_ESSENCE, toAdd);
                p.getPouches()[i] -= toAdd;
                return true;
            }
        }
        return false;
    }

    /** Start a looping RC action that repeats until you run out of essence (inv+pouches). */
    public static void craftEssenceLoop(Player player, int rune, int level, double experience,
                                        boolean pureEssOnly, int delayTicks, int... multipliers) {
        if (!RC_LOOPING_ENABLED) {
            // Single, traditional craft: only whats currently in inventory (no pouches)
            craftEssenceOnce(player, rune, level, experience, pureEssOnly,
                    Integer.MAX_VALUE, /*doLock=*/true, multipliers);
            return;
        }
        // Old behavior kept for later re-enable
        player.getActionManager().setAction(new RuneCraftAction(
                rune, level, experience, pureEssOnly, delayTicks, multipliers));
    }


    /** Repeats runecrafting every delayTicks until out of essence. */
    private static final class RuneCraftAction extends com.rs.game.player.actions.Action {
        private final int rune, level, delayTicks;
        private final double xpPerEss;
        private final boolean pureOnly;
        private final int[] multipliers;

        RuneCraftAction(int rune, int level, double xpPerEss, boolean pureOnly,
                        int delayTicks, int... multipliers) {
            this.rune = rune;
            this.level = level;
            this.xpPerEss = xpPerEss;
            this.pureOnly = pureOnly;
            this.delayTicks = Math.max(1, delayTicks);
            this.multipliers = multipliers;
        }

        @Override
        public boolean start(Player player) {
            return player.getSkills().getLevel(Skills.RUNECRAFTING) >= level;
        }

        @Override
        public boolean process(Player player) {
            // Keep going if we still have essence in inv or pouches
            boolean hasInvEss = player.getInventory().containsOneItem(PURE_ESSENCE)
                    || (!pureOnly && player.getInventory().containsOneItem(RUNE_ESSENCE));
            if (hasInvEss) return true;

            // Try emptying pouches into inventory if no inv essence
            if (player.getInventory().getFreeSlots() > 0) {
                boolean emptied = emptyAnyPouchToInventory(player);
                if (emptied) return true;
            }
            // No essence anywhere -> stop
            return false;
        }

        @Override
        public int processWithDelay(Player player) {
            // If still no essence in inv, try again to empty (handles free slot changes)
            boolean hasInvEss = player.getInventory().containsOneItem(PURE_ESSENCE)
                    || (!pureOnly && player.getInventory().containsOneItem(RUNE_ESSENCE));
            if (!hasInvEss && player.getInventory().getFreeSlots() > 0) {
                emptyAnyPouchToInventory(player);
            }

            int used = craftEssenceOnce(player, rune, level, xpPerEss, pureOnly,
                    28, /*doLock=*/false, multipliers);

            if (used <= 0) return -1; // stop
            return delayTicks; // repeat after N ticks (your "delay timer")
        }

        @Override
        public void stop(Player player) {
            // nothing
        }
    }




    public static void craftEssence(Player player, int rune, int level, double experience, boolean pureEssOnly, int... multipliers) {
        if (player.isLocked())
            return;
        int actualLevel = player.getSkills().getLevel(Skills.RUNECRAFTING);
        if (actualLevel < level) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need a Runecrafting level of " + level + " to craft this rune.");
            return;
        }
        int runes = player.getInventory().getItems().getNumberOf(PURE_ESSENCE);
        int normalEss = player.getInventory().getItems().getNumberOf(RUNE_ESSENCE);
        // Armadyl runes
        if (rune == 556) {
            int shards = player.getInventory().getAmountOf(21774);
            if (shards > 0) {
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        int[] products = {556, 21773};
                        SkillsDialogue.sendSkillsDialogue(player, SkillsDialogue.MAKE,
                                "How many would you like to make?<br>Choose a number, then click the item to begin.", 28, products,
                                new ItemNameFilter() {
                                    int count = 0;

                                    @Override
                                    public String rename(String name) {
                                        int productId = products[count++];
                                        int levelToMake = productId == 556 ? 1 : 72;
                                        if (player.getSkills().getLevel(Skills.CRAFTING) < levelToMake)
                                            name = "<col=ff0000>" + name + "<br><col=ff0000>Level " + levelToMake;
                                        return name;
                                    }
                                });
                    }

                    @Override
                    public void run(int interfaceId, int componentId) {
                        end();
                        int[] products = {556, 21773};
                        int productId = products[SkillsDialogue.getItemSlot(componentId)];
                        int levelToMake = productId == 556 ? 1 : 72;
                        if (productId == 556)
                            RuneCrafting.craftEssence(player, -1, levelToMake, 5, false, AIR_RUNE_MULTIPLIERS);
                        else
                            RuneCrafting.craftEssence(player, productId, levelToMake, 10, true, ARMADYL_RUNE_MULTIPLIERS);

                    }

                    @Override
                    public void finish() {
                    }
                });
                return;
            }
        }
        if (rune == -1)
            rune = 556;
        if (rune == 21773) {
            int shards = player.getInventory().getAmountOf(21774);
            runes = Math.min(runes, shards);
            if (runes == 0) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You don't have " + (pureEssOnly ? "pure" : "rune") + " essence.");
                return;
            }
        }
        int totalNumOfEss = runes + (!pureEssOnly ? normalEss : 0);
        if (totalNumOfEss == 0) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You don't have " + (pureEssOnly ? "pure" : "rune") + " essence.");
            return;
        }

// Limit essence to 28 maximum
        int essToUse = Math.min(28, totalNumOfEss);

// Now delete only 28 essence total (prioritize pure essence first)
        int usedPureEss = Math.min(runes, essToUse);
        int usedNormalEss = essToUse - usedPureEss;

        if (usedPureEss > 0)
            player.getInventory().deleteItem(PURE_ESSENCE, usedPureEss);
        if (usedNormalEss > 0)
            player.getInventory().deleteItem(RUNE_ESSENCE, usedNormalEss);

        runes = usedPureEss + usedNormalEss;

        if (runes == 0) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You don't have " + (pureEssOnly ? "pure" : "rune") + " essence.");
            return;
        }
        if (rune == 21773)
            player.getInventory().deleteItem(21774, runes);
        player.lock(5);
        double totalXp = getAltarExperience(rune, experience) * runes;
        totalXp *= getRunecraftingXpModifier(player);
        player.getSkills().addXp(Skills.RUNECRAFTING, totalXp);
        runes = getRuneOutput(runes, actualLevel, level, multipliers);

        String runeName = ItemDefinitions.getItemDefinitions(rune).getName().toLowerCase();
        String message = null;
        if (runeName.contains("air")) {
            player.addAirRunesMade(runes);
            message = Colors.RED + Utils.getFormattedNumber(player.getAirRunesMade()) + "</col>";
        } else if (runeName.contains("water")) {
            player.addWaterRunesMade(runes);
            message = Colors.RED + Utils.getFormattedNumber(player.getWaterRunesMade()) + "</col>";
        } else if (runeName.contains("earth")) {
            player.addEarthRunesMade(runes);
            message = Colors.RED + Utils.getFormattedNumber(player.getEarthRunesMade()) + "</col>";
        } else if (runeName.contains("mind")) {
            player.addMindRunesMade(runes);
            message = Colors.RED + Utils.getFormattedNumber(player.getMindRunesMade()) + "</col>";
        } else if (runeName.contains("fire")) {
            player.addFireRunesMade(runes);
            message = Colors.RED + Utils.getFormattedNumber(player.getFireRunesMade()) + "</col>";
        } else if (runeName.contains("body")) {
            player.addBodyRunesMade(runes);
            message = Colors.RED + Utils.getFormattedNumber(player.getBodyRunesMade()) + "</col>";
        } else if (runeName.contains("cosmic")) {
            player.addCosmicRunesMade(runes);
            message = Colors.RED + Utils.getFormattedNumber(player.getCosmicRunesMade()) + "</col>";
        } else if (runeName.contains("chaos")) {
            player.addChaosRunesMade(runes);
            message = Colors.RED + Utils.getFormattedNumber(player.getChaosRunesMade()) + "</col>";
        } else if (runeName.contains("nature")) {
            player.addNatureRunesMade(runes);
            message = Colors.RED + Utils.getFormattedNumber(player.getNatureRunesMade()) + "</col>";
        } else if (runeName.contains("law")) {
            player.addLawRunesMade(runes);
            message = Colors.RED + Utils.getFormattedNumber(player.getLawRunesMade()) + "</col>";
        } else if (runeName.contains("death")) {
            player.incrementDeathRunesMade(runes);
            message = Colors.RED + Utils.getFormattedNumber(player.getDeathRunesMade()) + "</col>";
        } else if (runeName.contains("blood")) {
            player.addBloodRunesMade(runes);
            message = Colors.RED + Utils.getFormattedNumber(player.getBloodRunesMade()) + "</col>";
        } else if (runeName.contains("astral")) {
            player.addAstralRunesMade(runes);
            message = Colors.RED + Utils.getFormattedNumber(player.getAstralRunesMade()) + "</col>";
        } else if (runeName.contains("soul")) {
            player.addSoulRunesMade(runes);
            message = Colors.RED + Utils.getFormattedNumber(player.getSoulRunesMade()) + "</col>";
        }

        player.addRunesMade(runes);
        RunecraftingContractList.listen(player, rune, runes);
        player.setNextGraphics(new Graphics(186, 0, 110));
        player.setNextAnimation(new Animation(791));
        player.getInventory().addItem(rune, runes);
        player.sendMessage("You bind the temple's power into " + runeName + "s;" + (message != null ? (" runes made: " + message + ";") : "") + " total: " + Colors.RED + Utils.getFormattedNumber(player.getRunesMade()) + "</col>.", true);
        if (rune == 565) {
            player.getAchievements().updateProgress(runes, AchievementList.RUNECRAFT_500_BLOOD_RUNES);
        }
    }

    public static void craftSoulRunes(Player player) {
        if (player.isLocked())
            return;
        int actualLevel = player.getSkills().getLevel(Skills.RUNECRAFTING);
        if (actualLevel < 90) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need a Runecrafting level of 90 to craft this rune.");
            return;
        }
        int pureEssence = player.getInventory().getItems().getNumberOf(PURE_ESSENCE);
        int runes = pureEssence / 4;
        if (runes <= 0) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need at least four pure essence to charge the soul altar.");
            return;
        }

        int essenceUsed = runes * 4;
        player.getInventory().deleteItem(PURE_ESSENCE, essenceUsed);
        player.lock(5);

        double totalXp = getAltarExperience(566, 220) * runes;
        totalXp *= getRunecraftingXpModifier(player);
        player.getSkills().addXp(Skills.RUNECRAFTING, totalXp);

        player.addSoulRunesMade(runes);
        player.addRunesMade(runes);
        RunecraftingContractList.listen(player, 566, runes);
        player.setNextGraphics(new Graphics(186, 0, 110));
        player.setNextAnimation(new Animation(791));
        player.getInventory().addItem(566, runes);

        String message = Colors.RED + Utils.getFormattedNumber(player.getSoulRunesMade()) + "</col>";
        player.sendMessage("You charge the soul altar with pure essence and bind its power into soul runes; runes made: "
                + message + "; total: " + Colors.RED + Utils.getFormattedNumber(player.getRunesMade()) + "</col>.", true);
    }

    public static void emptyPouch(Player p, int i) {
        if (i < 0)
            return;
        int toAdd = p.getPouches()[i];
        if (toAdd > p.getInventory().getFreeSlots())
            toAdd = p.getInventory().getFreeSlots();
        if (toAdd > 0) {
            p.getInventory().addItem(1436, toAdd);
            p.getPouches()[i] -= toAdd;
        }
        if (toAdd == 0) {
            p.getPackets().sendGameMessage("Your pouch has no essence left in it.", false);
            return;
        }
    }

    public static void enterAirAltar(Player player) {
        enterAltar(player, new WorldTile(2841, 4829, 0));
    }

    private static void enterAltar(Player player, WorldTile dest) {
        player.getPackets().sendGameMessage("A mysterious force grabs hold of you.");
        player.useStairs(-1, dest, 0, 1);
    }

    public static void enterBodyAltar(Player player) {
        enterAltar(player, new WorldTile(2522, 4825, 0));
    }

    public static void enterEarthAltar(Player player) {
        enterAltar(player, new WorldTile(2655, 4830, 0));
    }

    public static void enterFireAltar(Player player) {
        enterAltar(player, new WorldTile(2574, 4848, 0));
    }

    public static void enterMindAltar(Player player) {
        enterAltar(player, new WorldTile(2792, 4827, 0));
    }

    public static void enterWaterAltar(Player player) {
        enterAltar(player, new WorldTile(3482, 4838, 0));
    }

    public static void fillPouch(Player p, int i) {
        if (i < 0)
            return;
        if (LEVEL_REQ[i] > p.getSkills().getLevel(Skills.RUNECRAFTING)) {
            p.getPackets().sendGameMessage("You need a runecrafting level of " + LEVEL_REQ[i] + " to fill this pouch.", false);
            return;
        }
        int essenceToAdd = POUCH_SIZE[i] - p.getPouches()[i];
        if (essenceToAdd > p.getInventory().getItems().getNumberOf(1436))
            essenceToAdd = p.getInventory().getItems().getNumberOf(1436);
        if (essenceToAdd > POUCH_SIZE[i] - p.getPouches()[i])
            essenceToAdd = POUCH_SIZE[i] - p.getPouches()[i];
        if (essenceToAdd > 0) {
            p.getInventory().deleteItem(1436, essenceToAdd);
            p.getPouches()[i] += essenceToAdd;
        }
        if (!p.getInventory().containsOneItem(1436)) {
            p.getPackets().sendGameMessage("You don't have any essence with you.", false);
            return;
        }
        if (essenceToAdd == 0) {
            p.getPackets().sendGameMessage("Your pouch is full.", false);
            return;
        }
    }

    public static boolean hasRcingSuit(Player player) {
        return player.getEquipment().getHatId() == 21485 && player.getEquipment().getChestId() == 21484 && player.getEquipment().getLegsId() == 21486 && player.getEquipment().getBootsId() == 21487;
    }

    /**
     * Ethereal Outfits. http://runescape.wikia.com/wiki/Infinity_ethereal_outfit
     */
    public static boolean hasEtherealOutfit(Player player) {
        if (player.getEquipment().getHatId() == 32342 && player.getEquipment().getChestId() == 32343 && player.getEquipment().getLegsId() == 32344 && player.getEquipment().getGlovesId() == 32345 && player.getEquipment().getBootsId() == 32346)
            return true;
        if (player.getEquipment().getHatId() == 32347 && player.getEquipment().getChestId() == 32348 && player.getEquipment().getLegsId() == 32349 && player.getEquipment().getGlovesId() == 32350 && player.getEquipment().getBootsId() == 32351)
            return true;
        if (player.getEquipment().getHatId() == 32352 && player.getEquipment().getChestId() == 32353 && player.getEquipment().getLegsId() == 32354 && player.getEquipment().getGlovesId() == 32355 && player.getEquipment().getBootsId() == 32356)
            return true;
        return player.getEquipment().getHatId() == 32357 && player.getEquipment().getChestId() == 32358 && player.getEquipment().getLegsId() == 32359 && player.getEquipment().getGlovesId() == 32360 && player.getEquipment().getBootsId() == 32361;
    }

    /**
     * XP modifier by wearing items.
     *
     * @param player The player.
     * @return the XP modifier.
     */
    public static double runecrafterSuit(Player player) {
        double xpBoost = 1.0;
        if (player.getEquipment().getHatId() == 21485)
            xpBoost *= 1.01;
        if (player.getEquipment().getChestId() == 21484)
            xpBoost *= 1.01;
        if (player.getEquipment().getLegsId() == 21486)
            xpBoost *= 1.01;
        if (player.getEquipment().getBootsId() == 21487)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 21485 && player.getEquipment().getChestId() == 21484 && player.getEquipment().getLegsId() == 21486 && player.getEquipment().getBootsId() == 21487)
            xpBoost *= 1.01;
        return xpBoost;
    }

    public static boolean isTiara(int id) {
        return id == AIR_TIARA || id == MIND_TIARA || id == WATER_TIARA || id == BODY_TIARA || id == EARTH_TIARA || id == FIRE_TIARA || id == COSMIC_TIARA || id == NATURE_TIARA || id == CHAOS_TIARA || id == LAW_TIARA || id == DEATH_TIARA || id == BLOOD_TIARA || id == SOUL_TIARA || id == ASTRAL_TIARA || id == OMNI_TIARA;
    }

    public static void locate(Player p, int xPos, int yPos) {
        String x = "";
        String y = "";
        int absX = p.getX();
        int absY = p.getY();
        if (absX >= xPos)
            x = "west";
        if (absY > yPos)
            y = "South";
        if (absX < xPos)
            x = "east";
        if (absY <= yPos)
            y = "North";
        p.getPackets().sendGameMessage("The talisman pulls towards " + y + "-" + x + ".", false);
    }

    private static final double[][] tiaraXP = new double[][]{{5527, 25}, {5529, 27.5}, {5531, 30}, {5535, 32.5}, {5537, 35}, {5533, 37.5}, {5539, 40}, {5543, 42.5}, {5541, 45}, {5545, 47.5}, {5547, 50}

    };

    public static void infuseTiara(Player player, int index) {// up to blood
        // tiara
        int tiaraId = TIARA[index];
        int talismanId = (index * 2) + 1438;
        if (player.getInventory().containsItem(5525, 1) && player.getInventory().containsItem(talismanId, 1)) {
            player.getInventory().deleteItem(new Item(talismanId, 1));
            player.getInventory().deleteItem(new Item(5525, 1));
            for (double[] experience : tiaraXP) {
                if (experience[0] == tiaraId) {
                    player.getSkills().addXp(Skills.RUNECRAFTING, experience[1]);
                    break;
                }
            }
            player.getInventory().addItem(new Item(tiaraId));
            player.getPackets().sendGameMessage("You infuse the tiara with the power of your talisman.");
        }
    }
}
