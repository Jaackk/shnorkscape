package com.rs.game.player.content.gamemode;

import com.rs.game.player.Player;

// todo think of a better name than "Handler"

/**
 * The consequences for the success of the {@link KingOfTheSkillVerifier}'s verifications or an action that is executed
 * directly as a result of the King of the Skill game mode are contained in this class.
 */
public class KingOfTheSkillGameModeHandler {
    private final Player player;

    public KingOfTheSkillGameModeHandler(Player player) {
        this.player = player;
    }

    public void sendNoDonationsMessageToPlayer() {
        player.sendMessage("As a player with the King of the Skill game mode, you cannot purchase items from the store" +
                " to enhance your experience! Please contact an administrator in regards to this.");
    }

    public void sendNoVotesMessageToPlayer() {
        player.sendMessage("Players playing the King of the Skill game mode cannot claim votes.");
    }

    public void sendNoSquealMessageToPlayer() {
        player.sendMessage("You cannot use the Squeal of Fortune while playing the King of the Skill game mode.");
    }

    public void sendNoClanBankMessageToPlayer() {
        player.sm("Players playing the King of the Skill game mode cannot access clan banks.");
    }

    public void sendNoDungeoneeringWithPartnerMessageToPlayer() {
        player.sendMessage("Players playing the King of the Skill game mode can only dungeoneer alone.");
    }

    public void sendNoCoOpSlayerToPlayer() {
        player.sendMessage("Players playing the King of the Skill game mode cannot do Co-op slayer.");
    }

    public void sendNoOtherPlayerOwnedItemsMessageToPlayer() {
        player.sendMessage("Players playing the King of the Skill game mode cannot interact with other player " +
                "owned items.");
    }

    public void sendVagueNoMessageToPlayer() {
        player.sendMessage("You can not do this in the King of the Skill game mode.");
    }

    public void sendVagueNoMessageToPlayerAboutOtherPlayer(Player otherPlayer) {
        player.sendMessage(otherPlayer.getDisplayName() + " is playing the King of the Skill game mode and can not " +
                "do this.");
    }

    public void terminatePlayer() {
        player.logout(true);
    }
}
