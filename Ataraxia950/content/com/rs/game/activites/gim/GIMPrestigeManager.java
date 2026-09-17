package com.rs.game.activites.gim;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.rs.game.item.Item;
import com.rs.game.player.DataInterface;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.InputStringEvent;
import lombok.Setter;
import lombok.val;

import java.io.Serializable;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class GIMPrestigeManager implements Serializable {

    private static final long serialVersionUID = 7310329291961487141L;

    public static final class GIMPrestigeD extends Dialogue {

        @Override
        public void start() {
            sendMenu();
        }

        @Override
        public void run(int interfaceId, int componentId) {
            switch (stage) {
                case -4:
                    sendPlayerDialogue(NORMAL, "That makes sense, thanks a lot!");
                    stage = 0;
                    break;
                case -3:
                    sendNPCDialogue(12319, NORMAL, "Yes and yes, but you must prestige other skills before prestiging the same skill again.",
                            "Easier skills to train will require 2-3 other skills, and hard skills will only require 1 other skill.");
                    stage = -4;
                    break;
                case -2:
                    sendPlayerDialogue(NORMAL, "So I have to train the skill again back to 99? After that, can I prestige it again?");
                    stage = -3;
                    break;
                case 0:
                    end();
                    break;
                case 100:
                    sendMenu();
                    break;
                case 1:
                    if (componentId == OPTION_1) {
                        sendNPCDialogue(12319, NORMAL, "I've been appointed by King Jaedmo to acknowledge extraordinary feats of dedication.",
                                "Prestiging is resetting a maxed skill back to 1.",
                                "When doing this your entire group gains score.");
                        stage = -2;
                    } else if (componentId == OPTION_2) {
                        player.prestigeManager.displayPrestiges();
                    } else if (componentId == OPTION_3) {
                        sendNPCDialogue(12319, NORMAL, "Which skill do you want to prestige?");
                        stage = 2;
                    }
                    break;
                case 2:
                    player.prestigeManager.startPrestige(this);
                    break;
            }
        }

        @Override
        public void finish() {

        }

        private void sendMenu() {
            if (!player.isGroupIronman() || player.isUnregisteredGIM()) {
                sendNPCDialogue(12319, NORMAL, "You must be a registered GIM player in order to prestige.");
                stage = -1;
                return;
            }
            sendOptionsDialogue("Select an option.",
                    "Who are you? What is prestiging?",
                    "Show me my prestiged skills",
                    "Can I prestige a skill?");
            stage = 1;
        }
    }


    /**
     * Skill ID -> Prestige delay.
     */
    private static final ImmutableList<Integer> LIMIT = ImmutableList.of(3, 3, 3, 2, 3, 1, 3, 3, 1, 2, 1, 3, 2, 1, 2, 2, 1, 2, 1, 1, 3, 1, 2, 2, 1, 1, 1);

    private final Multiset<Integer> lastPrestiges = HashMultiset.create(28);
    private final Multiset<Integer> prestiges = HashMultiset.create(28);
    @Setter
    private transient Player player;

    public GIMPrestigeManager(Player player) {
        this.player = player;
    }

    public void startPrestige(Dialogue dialogue) {
        if (!player.isGroupIronman() || player.isUnregisteredGIM()) {
            if (dialogue == null) {
                Dialogue.sendSingleNPCDialogue(player, 12319, Dialogue.NORMAL, "You must be a registered GIM player in order to prestige.");
            }
            return;
        }
        player.sendInputString("Enter the skill", new InputStringEvent() {
            @Override
            public void run(Player player) {
                String enteredSkill = getString().toLowerCase().trim();
                Integer skillId = Skills.NAME_TO_ID.get(enteredSkill);
                if (skillId == null) {
                    String line = "I don't think any skill exists with the name '" + enteredSkill + "'...";
                    if (dialogue != null) {
                        dialogue.sendNPCDialogue(12319, Dialogue.NORMAL, line);
                        dialogue.setStage(100);
                    } else {
                        Dialogue.sendSingleNPCDialogue(player, 12319, Dialogue.NORMAL, line);
                    }
                    return;
                }
                player.prestigeManager.prestige(dialogue, skillId);
            }
        });
    }

    public void prestige(Dialogue dialogue, int skillId) {
        int remaining = lastPrestiges.count(skillId);
        if (remaining > 0) {
            String skillPlural = remaining > 1 ? "skills" : "skill";
            String line = "You need to prestige " + remaining + " other " + skillPlural + " before doing this one again.";
            if (dialogue != null) {
                dialogue.sendNPCDialogue(12319, Dialogue.NORMAL, line);
                dialogue.setStage(100);
            } else {
                Dialogue.sendSingleNPCDialogue(player, 12319, Dialogue.NORMAL, line);
            }
            return;
        }
        int currentLevel = player.getSkills().getLevelForXp(skillId);
        if (currentLevel < 99) {
            String line = "Your skill needs to be level 99 in order for you to prestige it.";
            if (dialogue != null) {
                dialogue.sendNPCDialogue(12319, Dialogue.NORMAL, line);
                dialogue.setStage(100);
            } else {
                Dialogue.sendSingleNPCDialogue(player, 12319, Dialogue.NORMAL, line);
            }
            return;
        }

        boolean block = false;
        for (int index = 0; index < player.getEquipment().getItems().getSize(); index++) {
            Item next = player.getEquipment().getItem(index);
            if (player.getEquipment().isEmpty()) {
                break;
            }
            if (player.getInventory().isFull()) {
                block = true;
                break;
            }
            if (next == null) {
                continue;
            }
            player.getInventory().addItem(next.getId(), next.getAmount());
            player.getEquipment().set(index, null);
        }
        player.getEquipment().refreshItemContainer();
        player.getAppearence().generateAppearenceData();
        if (block) {
            String line = "Please remove all your equipment before prestiging a skill.";
            if (dialogue != null) {
                dialogue.sendNPCDialogue(12319, Dialogue.NORMAL, line);
                dialogue.setStage(100);
            } else {
                Dialogue.sendSingleNPCDialogue(player, 12319, Dialogue.NORMAL, line);
            }
            return;
        }

        player.getSkills().resetLevel(skillId);
        for (int id = 0; id < Skills.SKILL_NAME.length; id++) {
            if (id != skillId) {
                lastPrestiges.remove(id);
            }
        }
        int required = LIMIT.get(skillId);
        lastPrestiges.add(skillId, required);
        prestiges.add(skillId);
        player.gimTracker.incrementPrestiges();
        String line = "You have successfully prestiged the " + Skills.SKILL_NAME[skillId] + " skill!";
        if (dialogue != null) {
            dialogue.sendNPCDialogue(12319, Dialogue.NORMAL, line);
            dialogue.setStage(100);
        } else {
            Dialogue.sendSingleNPCDialogue(player, 12319, Dialogue.NORMAL, line);
        }
    }

    public void displayPrestiges() {
        if (!player.isGroupIronman() || player.isUnregisteredGIM()) {
            Dialogue.sendSingleNPCDialogue(player, 12319, Dialogue.NORMAL, "You must be a registered GIM player in order to view prestiges.");
            return;
        }
        val inter = new DataInterface("<img=33> Your Prestiges");
        inter.add("Total prestiges: " + prestiges.size());
        inter.blankLine();
        for (int id = 0; id < Skills.SKILL_NAME.length; id++) {
            int prestigeCount = prestiges.count(id);
            if (prestigeCount > 0) {
                int blockedCount = lastPrestiges.count(id);
                String skillName = Skills.SKILL_NAME[id];
                if (blockedCount > 0) {
                    inter.add(Colors.DARK_RED + skillName + " ~ " + prestigeCount + " (blocked for " + blockedCount + ")");
                } else {
                    inter.add(Colors.GREEN + skillName + " ~ " + prestigeCount);
                }
            }
        }
        inter.show(player);
    }
}
