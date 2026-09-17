package com.rs.game.player.dialogue.impl.slayer.sophanem;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.actions.slayer.sophanemdungeon.SophanemSlayerNPC;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.data.parsers.misc.ShopsDataParser;
import lombok.val;

public class MenaphiteGuardD extends Dialogue {
    public static final int NPC_ID = 24591;
    private static final int FIRST_TIME_ENTERED_STARTING_STAGE = 0;
    private static final int REGULAR_STARTING_STAGE = 9;

    @Override
    public void start() {
        if (!player.hasEnteredSophanemBefore()) {
            sendNPCDialogue(NPC_ID, NORMAL, "Halt there! Ahead of here lies only death.");
            stage = FIRST_TIME_ENTERED_STARTING_STAGE;
        } else if (player.hasEnteredSophanemBefore()) {
            sendNPCDialogue(NPC_ID, NORMAL, "Halt there! Ahead of here lies only death.");
            stage = REGULAR_STARTING_STAGE;
        }
    }

    public static void openShop(Player player) {
        val shopId = 66;
        ShopsDataParser.openShop(player, shopId);
    }

    private static void enterSophanemSlayerDungeon(Player player) {
        val transportTime = 2;
        val sophanemSlayerDungeonTile = new WorldTile(2384, 6793, 3);

        player.lock();
        FadingScreen.fade(player, transportTime, () -> {
            player.setNextWorldTile(sophanemSlayerDungeonTile);
            player.getControlerManager().startControler("SophanemSlayerDungeon");
            player.unlock();
        });
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case -1:
                end();
                break;
            case FIRST_TIME_ENTERED_STARTING_STAGE:
                sendPlayerDialogue(NORMAL, "What's in there?");
                stage = FIRST_TIME_ENTERED_STARTING_STAGE + 1;
                break;
            case FIRST_TIME_ENTERED_STARTING_STAGE + 1:
                sendNPCDialogue(NPC_ID, NORMAL, "Unnatural creatures. There is a power in there that chills me " +
                        "just thinking about it. Just stood here, I can feel it creeping over me; it makes me just " +
                        "want to up and abandon my post and run back to my family.");
                stage = FIRST_TIME_ENTERED_STARTING_STAGE + 2;
                break;
            case FIRST_TIME_ENTERED_STARTING_STAGE + 2:
                sendPlayerDialogue(NORMAL, "It can't be that bad. Can I just take a look?");
                stage = FIRST_TIME_ENTERED_STARTING_STAGE + 3;
                break;
            case FIRST_TIME_ENTERED_STARTING_STAGE + 3:
                sendNPCDialogue(NPC_ID, NORMAL, "Hmm, yes, you do look like you can handle yourself, but you can't " +
                        "go in there just yet. These creatures do not fall as normal beasts do, they exist on the " +
                        "boundary of life and death. So the priests say, anyway.");
                stage = FIRST_TIME_ENTERED_STARTING_STAGE + 4;
                break;
            case FIRST_TIME_ENTERED_STARTING_STAGE + 4:
                sendNPCDialogue(NPC_ID, NORMAL, "There is an item, though - the feather of Ma'at. They may look " +
                        "like normal feathers, but they contain some magic...or perhaps a curse, I do not truly know." +
                        " With a feather upon you, these creatures will fall to your blows.");
                stage = FIRST_TIME_ENTERED_STARTING_STAGE + 5;
                break;
            case FIRST_TIME_ENTERED_STARTING_STAGE + 5:
                sendPlayerDialogue(NORMAL, "Where can I get these feathers?");
                stage = FIRST_TIME_ENTERED_STARTING_STAGE + 6;
                break;
            case FIRST_TIME_ENTERED_STARTING_STAGE + 6:
                sendNPCDialogue(NPC_ID, NORMAL, "There are two places. I'll only tell you one. The first is from me - " +
                        "you didn't think they would leave me to guard without them, did you? I can sell them to you, " +
                        "of course, for a cost.");
                stage = FIRST_TIME_ENTERED_STARTING_STAGE + 7;
                player.setEnteredSophanemBefore(true);
                break;
            case FIRST_TIME_ENTERED_STARTING_STAGE + 7:
                sendOptionsDialogue("Select an Option", "Can I buy some feathers of Ma'at?", "Enter the Sophanem Slayer Dungeon", "I'll be on my way, then.");
                stage = FIRST_TIME_ENTERED_STARTING_STAGE + 8;
                break;
            case FIRST_TIME_ENTERED_STARTING_STAGE + 8:
                switch (componentId) {
                    case OPTION_1:
                        ShopsDataParser.openShop(player, 1811);
                        end();
                        break;
                    case OPTION_2:
                        end();
                        enterSophanemSlayerDungeon(player);
                        break;
                    case OPTION_3:
                        sendNPCDialogue(NPC_ID, NORMAL, "Take care.");
                        stage = -1;
                        break;
                }
                break;
            case REGULAR_STARTING_STAGE:
                sendOptionsDialogue("Select an Option", "What's in there?",
                        "Enter the Sophanem Slayer Dungeon",
                        "Can I buy some feathers of Ma'at?",
                        "I'll be on my way, then.");
                stage = REGULAR_STARTING_STAGE + 1;
                break;
            case REGULAR_STARTING_STAGE + 1:
                switch (componentId) {
                    case OPTION_1:
                        sendNPCDialogue(NPC_ID, NORMAL, "Unnatural creatures. There is a power in there that " +
                                "chills me just thinking about it. Just stood here, I can feel it creeping over me; " +
                                "it makes me just want to up and abandon my post and run back to my family.");
                        stage = REGULAR_STARTING_STAGE + 2;
                        break;
                    case OPTION_2:
                        end();
                        enterSophanemSlayerDungeon(player);
                        break;
                    case OPTION_3:
                        end();
                        openShop(player);
                        break;
                    case OPTION_4:
                        sendNPCDialogue(NPC_ID, NORMAL, "Take care.");
                        stage = -1;
                        break;
                }
                break;
            case REGULAR_STARTING_STAGE + 2:
                sendPlayerDialogue(NORMAL, "It can't be that bad. Can I just take a look?");
                stage = REGULAR_STARTING_STAGE + 3;
                break;
            case REGULAR_STARTING_STAGE + 3:
                if (hasFeatherOfMaat()) {
                    sendNPCDialogue(NPC_ID, NORMAL, "Hmm, yes, you do look like you can handle yourself. Very well, " +
                            "proceed if you wish, but I take no responsibility for any pain, suffering or loss of " +
                            "limbs that may occur while you are in there.");
                    stage = REGULAR_STARTING_STAGE;
                } else {
                    sendNPCDialogue(NPC_ID, NORMAL, "Hmm, yes, you do look like you can handle yourself, but you can't " +
                            "go in there just yet. These creatures do not fall as normal beasts do, they exist on the " +
                            "boundary of life and death. So the priests say, anyway.");
                    stage = FIRST_TIME_ENTERED_STARTING_STAGE + 4;
                }
                break;

        }
    }

    private boolean hasFeatherOfMaat() {
        return player.getInventory().containsItem(SophanemSlayerNPC.FEATHER_OF_MAAT);
    }

    @Override
    public void finish() {

    }
}
