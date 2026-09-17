package com.rs.game.player.content.xmas;

import com.rs.Settings;
import com.rs.game.ForceTalk;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.activities.snowball.game.SnowballFightGame;
import com.rs.game.activities.snowball.game.SnowballFightGameController;
import com.rs.game.activities.snowball.lobby.SnowballLobby;
import com.rs.game.activities.snowball.lobby.SnowballLobbyController;
import com.rs.game.npc.NPC;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;

public class SantaDialogue2 extends Dialogue {

    public static final int SANTA = 8540;
    private NPC npc;

    @Override
    public void start() {
        npc = (NPC) parameters[0];
        Dialogue.closeNoContinueDialogue(player);
        if (!player.getXmas().finishedRiddles() && !Settings.TEST_SERVER_MODE && !Settings.DEBUG && player.getRights() != 2 && !player.isDev()) {
            npc(SANTA, SAD, "Ho ho- oh, sorry, you need to complete all of the riddles before you can join a snowball fight!");
            stage = 99;
            return;
        }
        npc(SANTA, GOOFY_LAUGH, "Ho ho ho! How can I help you, " + Colors.wrap(Colors.BLUE, player.getDisplayName()) + "?");
        stage = 1;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        final SnowballLobby lobby = World.getSnowballLobby();
        final SnowballFightGame game = World.getSnowballFightGame();
        switch(stage) {
            case 1:
                final String message = game != null && game.containsPlayer(player) ? Colors.wrap(Colors.RED, "Leave snowball fight!") :
                        lobby.getPlayers().contains(player) ? Colors.wrap(Colors.RED, "Leave snowball fight lobby!") :
                                Colors.wrap(Colors.GREEN, "Join snowball fight lobby!");
                sendOptionsDialogue("Select an option", message, "Nevermind");
                stage = 2;
                break;

            case 2:
                switch(componentId) {
                    case OPTION_1:
                        if (game != null && game.containsPlayer(player)) {
                            player("I'd like to leave the snowball fight.");
                            stage = 30;
                        } else if(lobby.getPlayers().contains(player)) {
                            player("I'd like to leave the snowball fight lobby.");
                            stage = 20;
                        } else {
                            player("I'd like to join the snowball fight!");
                            stage = 10;
                        }
                        break;

                    case OPTION_2:
                        finish();
                        break;

                }
                break;

            case 5:
                npc(SANTA, SAD, "Ho ho- oh, sorry, you need to complete all of the riddles before you can join a snowball fight!");
                stage = 99;
                break;

            case 10:
                if(SnowballLobby.canEnter(player))
                    SnowballLobby.enterLobby(player);
                else
                    SnowballLobby.sendInvalidEntryMessage(player);

                finish();
                break;

            case 20:
                if (player.getControlerManager().getControler() instanceof SnowballLobbyController) {
                    SnowballLobbyController snowballLobbyController = (SnowballLobbyController) player.getControlerManager().getControler();
                    snowballLobbyController.removeFromLobby(true);
                    player.sendMessage(Colors.DARK_RED + "You have left the snowball fight lobby!");
                    finish();
                }
                break;

            case 30:
                if (player.getControlerManager().getControler() instanceof SnowballFightGameController) {
                    npc.setNextForceTalk(new ForceTalk("I'm gonna put you in that naughty list, " + player.getDisplayName() + "!"));
                    npc.faceEntity(player);
                    player.faceEntity(npc);
                    player.applyHit(new Hit(player.getMaxHitpoints(), Hit.HitLook.REGULAR_DAMAGE));
                    player.sendMessage(Colors.DARK_RED + "Santa decides to finish you off!");
                    finish();
                }
                break;

            case 99:
                finish();
                break;
        }
    }

    @Override
    public void finish() {
        player.getInterfaceManager().closeChatBoxInterface();
    }
}
