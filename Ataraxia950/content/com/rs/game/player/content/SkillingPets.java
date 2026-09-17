package com.rs.game.player.content;

import com.rs.Settings;
import com.rs.game.World;

import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.controllers.FightCaves;
import com.rs.game.player.controllers.FightKiln;
import com.rs.game.player.controllers.pestcontrol.PestControlGame;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @Author Xenthium.
 */


public class SkillingPets {

    //    private static final boolean debugConfigEnabled = Settings.DEBUG || Settings.TEST_SERVER_MODE;
    private static final boolean combatPetsEnabled = Utils.getItemDefinitionsSize() > 41600; // Cheap way to tell if it's the new cache or not.
    private static final int MAXIMUM_EXP = 10_000; // Limits the maximum exp for things that give large xp drops like lamps etc.

    public static void rollForPetDrop(Player player, int skillId, double exp) {
        PetData petForSkill = PetData.getPetForSkill().get(skillId);
        if (player == null || petForSkill == null || exp < 1 || player.getTemporaryAttributtes().remove("dungxpbuy") != null) {
            return;
        }
        Controller controller = player.getControlerManager().getControler();
        if (controller instanceof DungeonController) {
            exp /= player.getDungeoneeringManager().getParty().getSize();
        }
        exp /= Settings.getExperienceMultiplier(player);
        exp /= petForSkill.getModifier();
        exp *= getVirtualLevelMultiplier(player, skillId);
        if (player.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER)) {
            exp *= 1.5;
        }
        if (exp > MAXIMUM_EXP) {
            exp = MAXIMUM_EXP;
        }
        int chance = (int) (1 / (exp / 50_000_000));
        int roll = ThreadLocalRandom.current().nextInt(chance);
//        if (debugConfigEnabled) {
//            player.sendMessage(Colors.YELLOW + "Chance for pet from " + player.getSkills().getSkillName(skillId) + " xp drop: 1/" + Utils.formatNumber(chance) + " - rolled: " + Utils.formatNumber(roll) + ".", true);
//        }
        if (isCombatSkill(skillId) && !combatPetsEnabled) {
            return;
        }
        if (roll == 0 && !player.hasItem(petForSkill.getPet())) {
            boolean isInMinigame = controller instanceof DungeonController || controller instanceof FightCaves || controller instanceof FightKiln || controller instanceof PestControlGame;
            if (!player.getInventory().hasFreeSlots() || isInMinigame) {
                player.getBank().addItem(petForSkill.getPet(), true);
                player.sendMessage(Colors.GREEN + "While skilling, you find " + petForSkill.getPet().getName() + ", the " + Utils.formatPlayerNameForDisplay(petForSkill.name()) + " pet. " + (isInMinigame ? "However you are currently in a minigame," : "However, you do not have enough inventory space,") + " so it been sent to your bank.");
            } else {
                player.sendMessage(Colors.GREEN + "While skilling, you find " + petForSkill.getPet().getName() + ", the " + Utils.formatPlayerNameForDisplay(petForSkill.name()) + " pet. It has been added to your inventory.");
                player.addItem(petForSkill.getPet());
            }
            player.getAchievements().updateProgress(1, AchievementList.OBTAIN_SKILLING_PET);
            World.sendWorldMessage("<col=4286f4><img=6>News: Congratulations! " + player.getDisplayName() + " has unlocked " + petForSkill.getPet().getName() + ", the " + Utils.formatPlayerNameForDisplay(petForSkill.name()) + " pet!", false);
            HcimNewsManager.getInstance().addNews(player,"<#player> has unlocked " + petForSkill.getPet().getName() + ", the " + Utils.formatPlayerNameForDisplay(petForSkill.name()) + " pet!");
                    QueryExecutor.submit(new News(player, "<b><img src=\"../hiscores/incl/img/skill_icons/" + Skills.SKILL_NAME[skillId] + "-icon.png\" " + "width=17> " + player.getDisplayName() + " has unlocked " + petForSkill.getPet().getName() + ", the " + Utils.formatPlayerNameForDisplay(petForSkill.name()) + " pet!"));
        } else if (roll == 0 && player.hasItem(petForSkill.getPet())) {
            player.getSkills().addXp(skillId, 50_000);
            player.sendMessage("You have a strange feeling you would've been followed. You feel a surge of experience flow through you instead.");
        }
    }

    private static int getVirtualLevelMultiplier(Player player, int skillId) {
        int hiddenBoostXpRequirement = (player.isLegendary() || player.isIronMan() || player.isHCIronMan()) ? 250_000_000 : (player.isExpert() || player.isExpertIronMan()) ? 500_000_000 : (player.isIntermediate() || player.isIntermediateIronMan()) ? 750_000_000 : 1_000_000_000;
        return player.getSkills().getVirtualLevel(skillId) + (player.getSkills().getXp(skillId) >= hiddenBoostXpRequirement ? 200 : 0);
    }

    private static boolean isCombatSkill(int skillId) {
        return skillId == Skills.HITPOINTS || skillId == Skills.ATTACK || skillId == Skills.STRENGTH || skillId == Skills.DEFENCE || skillId == Skills.RANGE || skillId == Skills.PRAYER || skillId == Skills.MAGIC || skillId == Skills.SUMMONING || skillId == Skills.SLAYER;
    }

    public static boolean itemIsSkillingPet(Item item) {
        int itemId = item.getId();
        for (PetData pet : PetData.values()) {
            if (pet.pet.getId() == itemId) {
                return true;
            }
        }
        return false;
    }

    @AllArgsConstructor
    public enum PetData {
        AGILITY(new Item(38075), 25, Skills.AGILITY),
        ATTACK(new Item(48340), 35, Skills.ATTACK),
        CONSTITUTION(new Item(48346), 15, Skills.HITPOINTS),
        CONSTRUCTION(new Item(38077), 35, Skills.CONSTRUCTION),
        COOKING(new Item(38079), 25, Skills.COOKING),
        CRAFTING(new Item(38081), 20, Skills.CRAFTING),
        DEFENCE(new Item(48344), 35, Skills.DEFENCE),
        DIVINATION(new Item(38083), 25, Skills.DIVINATION),
        DUNGEONEERING(new Item(38085), 15, Skills.DUNGEONEERING),
        FARMING(new Item(38087), 25, Skills.FARMING),
        FIREMAKING(new Item(38089), 20, Skills.FIREMAKING),
        FISHING(new Item(38091), 25, Skills.FISHING),
        FLETCHING(new Item(38093), 25, Skills.FLETCHING),
        HERBLORE(new Item(38095), 25, Skills.HERBLORE),
        HUNTER(new Item(38097), 20, Skills.HUNTER),
        INVENTION(new Item(38099), 20, Skills.INVENTION),
        MAGIC(new Item(48352), 35, Skills.MAGIC),
        MINING(new Item(38101), 20, Skills.MINING),
        PRAYER(new Item(48350), 40, Skills.PRAYER),
        RANGED(new Item(48348), 35, Skills.RANGE),
        RUNECRAFTING(new Item(38103), 20, Skills.RUNECRAFTING),
        SLAYER(new Item(38105), 10, Skills.SLAYER),
        SMITHING(new Item(38107), 20, Skills.SMITHING),
        STRENGTH(new Item(48342), 35, Skills.STRENGTH),
        SUMMONING(new Item(48354), 20, Skills.SUMMONING),
        THIEVING(new Item(38109), 20, Skills.THIEVING),
        WOODCUTTING(new Item(38111), 15, Skills.WOODCUTTING);

        private static final HashMap<Integer, PetData> petForSkill = new HashMap<>();

        static {
            Arrays.stream(values()).forEach(petForSkill -> PetData.petForSkill.put(petForSkill.skillId, petForSkill));
        }

        @Getter
        private final Item pet;
        @Getter
        private final int modifier;
        @Getter
        private final int skillId;

        public static HashMap<Integer, PetData> getPetForSkill() {
            return petForSkill;
        }
    }
}
