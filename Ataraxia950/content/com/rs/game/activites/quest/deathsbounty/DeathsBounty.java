package com.rs.game.activites.quest.deathsbounty;

import com.rs.Settings;
import com.rs.game.activites.quest.AbstractQuest;
import com.rs.game.activites.quest.QuestStage;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Colors;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class DeathsBounty extends AbstractQuest {

    public static final int DEFENCE_REQ = 50;
    public static final int STRENGTH_REQ = 60;
    public static final int DIVINATION_REQ = 70;
    public static final int PRAYER_REQ = 43;
    public static final int SOUL_URN_ID = 11183;
    public static final int HUSBAND_MICH_ID = 8348;
    private static final long serialVersionUID = -3633047317674562552L;

    public static boolean activateDeathDialogue(Player player, int questStage) {
        return questStage == -1 && canStart(player) ||
                questStage == 3 ||
                questStage == 0;
    }

    public static boolean canStart(Player plr) {
        Skills skills = plr.getSkills();
        return skills.getLevelForXp(Skills.DEFENCE) >= DEFENCE_REQ &&
                skills.getLevelForXp(Skills.STRENGTH) >= STRENGTH_REQ &&
                skills.getLevelForXp(Skills.DIVINATION) >= DIVINATION_REQ &&
                skills.getLevelForXp(Skills.PRAYER) >= PRAYER_REQ;
    }

    public static void dropUrn(Player plr) {
        if (ThreadLocalRandom.current().nextInt(Settings.TEST_SERVER_MODE ? 3 : 64) == 0) {
            int questStage = plr.quests.getCurrentStage(DeathsBounty.class);
            boolean dropForStage = questStage == 0 || questStage == 1 || questStage == 2;
            if (dropForStage &&
                    plr.getInventory().hasFreeSlots() &&
                    !plr.hasItem(SOUL_URN_ID)) {
                String message = questStage == 0 ? "You find a Soul urn! You should show it to Death." : "You find a Soul urn!";
                plr.sendMessage(Colors.RED + message);
                plr.getInventory().addItem(SOUL_URN_ID, 1);
                plr.getActionManager().forceStop();
            }
        }
    }

    @Override
    public void build(List<QuestStage> stages) {

        // Start searching divine location.
        stages.add((player, quest) -> new String[]{
                bold("Death") + " says that someone has cheated death. I should gather from",
                bold("divine locations") + " until I find a " + bold("Soul urn") + "."
        });

        // He tells you to take it to Guthix.
        stages.add((player, quest) -> new String[]{
                bold("Death") + " says I should bring the " + bold("Soul urn") + " to " + bold("Guthix") + ".",
                "I can find him in " + bold("Guthix's cave") + "."
        });

        // After the urn is given to Guthix.
        stages.add((player, quest) -> new String[]{
                bold("Guthix") + " says the imprinted name on the " + bold("Soul urn") + " is " + bold("Husband Mich") + "!",
                "I should talk to " + bold("Guthix") + " when I'm ready to fight him for his soul."
        });

        // After Husband Mich is killed.
        stages.add((player, quest) -> new String[]{
                "I've slain " + bold("Husband Mich") + " and balance has been restored.",
                "I should go tell " + bold("Death") + " the good news."
        });
    }

    @Override
    public String[] initialDescription() {
        return new String[]{
                bold("I can start this quest by talking to Death."),
                "",
                bold("Length:") + " Short",
                "",
                bold("Difficulty:") + " Easy",
                "",
                bold("Requirements:"),
                DEFENCE_REQ + " Attack",
                STRENGTH_REQ + " Strength",
                DIVINATION_REQ + " Divination",
                PRAYER_REQ + " Prayer"
        };
    }

    @Override
    public void onComplete(Player player) {
        player.getSkills().addXp(Skills.DIVINATION, 50_000);
        player.getSkills().addXp(Skills.DEFENCE, 100_000);
        player.getSkills().addXp(Skills.MAGIC, 100_000);
    }

    @Override
    public String[] rewardDescription() {
        return new String[]{
                "2 Quest points",
                "50K Divination XP",
                "100K Defence/Magic XP",
                "Temporary 10% dmg boost during reaper tasks (24h)",
        };
    }

    @Override
    public int questPoints() {
        return 2;
    }

    @Override
    public Item completionDisplayItem() {
        return new Item(SOUL_URN_ID);
    }

    @Override
    public String name() {
        return "Deaths' Bounty";
    }
}
