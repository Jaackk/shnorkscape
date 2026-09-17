package com.rs.game.player.dialogue.impl;

import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GeneralTafaniD extends Dialogue {

    public static final int NPC_ID = 961;
    private final List<String> words = new ArrayList<>(Arrays.asList("Now", "I", "am", "become", "death", "the", "destroyer", "of", "worlds"));

    public static void restorePlayer(final Player player) {
        player.heal(player.getMaxHitpoints());
        if (player.getPoison().isPoisoned()) {
            player.getPoison().reset();
        }
        if (player.getMoneySpent() >= 50) {
            player.getSkills().restoreSummoning();
            player.getPrayer().restorePrayer(player.getSkills().getLevelForXp(Skills.PRAYER) * 10);
            if (player.isDiamondDonor()) {
                player.getCombatDefinitions().resetSpecialAttack();
            }
        }
        player.setNextGraphics(new Graphics(1295));
    }

    @Override
    public void start() {
        if (Utils.random(3) == 0 && !player.isDiscoveredEasterEgg()) {
            sendNPCDialogue(NPC_ID, Dialogue.UNSURE, "Actually, before you ask me to heal you, can you help me with this problem I'm having?");
            stage = 10;
        } else {
            sendNPCDialogue(NPC_ID, Dialogue.NORMAL, "Hello " + player.getDisplayName() + ", would you like me to restore your hitpoints" + (player.isDiamondDonor() ? ", special attack, prayer, summoning" : player.getMoneySpent() >= 50 ? ", prayer, summoning" : "") + " and cure any poisons for you?");
        }
    }

    @Override
    public void run(int interfaceId, int componentId) {

        switch (stage) {
            case -1:
                if (player.isDiscoveredEasterEgg() && !player.isCompletedEasterEgg()) {
                    sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Yes please", "No thank you", "What are the words again?", "I figured the sentence out");
                } else {
                    sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Yes please", "No thank you");
                }
                stage = 0;
                break;

            case 0:
                switch (componentId) {
                    case OPTION_1:
                        restorePlayer(player);
                        end();
                        break;
                    case OPTION_2:
                        end();
                        break;
                    case OPTION_3:
                        Collections.shuffle(words);
                        sendNPCDialogue(NPC_ID, Dialogue.NORMAL, "The words are:<br>" + words);
                        stage = 1;
                        break;
                    case OPTION_4:
                        player.sendInputString("Enter the sentence the words form:", new InputStringEvent() {
                            @Override
                            public void run(Player player) {
                                String answer = getString().trim();
                                if (answer.equalsIgnoreCase("now i am become death the destroyer of worlds")) {
                                    player.setNextAnimationForce(new Animation(866));
                                    player.setNextGraphics(new Graphics(1765));
                                    player.setNextForceTalk(new ForceTalk("I got it!"));
                                    player.getDialogueManager().startDialogue("SimpleMessage", "Congrats, you've discovered some useless easter egg I added.<br>- Xen.");
                              /*      for (Player p : World.getPlayers()) {
                                        if (!p.isCompletedEasterEgg()) {
                                            p.sendMessage(Colors.BLACK + "Someone discovered an easter egg.", true);
                                        }
                                    }
                              */
                                    player.setCompletedEasterEgg(true);
                                } else {
                                    List<String> letters = new ArrayList<>(Arrays.asList("J", "e", "n", "o", "v", "a"));
                                    for (int i = 0; i < 26; i++) {
                                        letters.add(String.valueOf((char) ((Utils.randomBool() ? 97 : 65) + i)));
                                    }
                                    Collections.shuffle(letters);
                                    StringBuilder sb = new StringBuilder();
                                    for (String letter : letters) {
                                        sb.append(letter);
                                    }
                                    player.getDialogueManager().startDialogue("SimpleMessage", "Wroooooooooooong.<br>Keep trying." + (Utils.randomBool() ? "<br><br>If you find the word hidden in this, pm Xenthium:<br>" + sb : ""));
                                }
                            }
                        });
                        break;
                }
                break;

            case 10:
                sendPlayerDialogue(player, Dialogue.UNSURE, "Err, I've been asked stranger things...<br>Sure, what's up?");
                stage++;
                break;
            case 11:
                Collections.shuffle(words);
                player.setDiscoveredEasterEgg(true);
                sendNPCDialogue(NPC_ID, Dialogue.UNSURE, "Well, I actually need you to arrange these words to form the correct sentence.<br>Talk to me once you've done it.<br>" + words);
                stage = 1;
                break;
            case 1:
                end();
                break;
        }

    }

    @Override
    public void finish() {

    }

}
