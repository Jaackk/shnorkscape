package com.rs.game.player.content.skillingcontracts;

import com.rs.game.player.Player;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class CoOpSkillingContractManager {

    public static void sendRequest(Player player, Player usedOn) {
        if(player.isGroupIronman() && !player.canGimInteractWith(usedOn)) {
            player.sendGimCantInteract();
        } else if(usedOn.isGroupIronman() && !usedOn.canGimInteractWith(player)) {
            player.sendMessage("Ironmen cannot do co-op skilling contracts.");
        } else if (usedOn.isCoOpSkillingRequesting() && usedOn.coOpRequest.requesting.equals(player) && usedOn.isActive()) {
            usedOn.getContracts().assignCoOpContract();
        } else if (player.isCoOpSkillingRequesting()) {
            player.sendMessage("You have already sent a co-op skilling request to someone within the last minute.");
        } else if (player.getContracts().current != null) {
            player.sendMessage("You'll need to finish your current contract first.");
        } else if (usedOn.getContracts().current != null) {
            player.sendMessage("They already have a skilling contract.");
        } else if (player.isKingOfTheSkillGameMode()) {
            player.getKingOfTheSkillGameModeHandler().sendVagueNoMessageToPlayer();
        } else if (usedOn.isKingOfTheSkillGameMode()) {
            player.getKingOfTheSkillGameModeHandler().sendVagueNoMessageToPlayerAboutOtherPlayer(usedOn);
        } else if ((player.isATypeOfIronman() || usedOn.isATypeOfIronman()) && !player.isGroupIronman() && !usedOn.isGroupIronman()) {
            player.sendMessage("Ironmen cannot do co-op skilling contracts.");
        } else if (usedOn.getInterfaceManager().containsScreenInter() || usedOn.isUnderCombat()) {
            player.sendMessage("That player is busy.");
        } else if (player.getInterfaceManager().containsScreenInter() || player.isUnderCombat()) {
            player.sendMessage("You are busy.");
        } else {
            boolean hasAdvanced = player.getContracts().canTalkToAdvancedMaster(false);
            boolean otherHasAdvanced = usedOn.getContracts().canTalkToAdvancedMaster(false);
            if (!usedOn.containsOneItem(25450) && !usedOn.containsOneItem(37694) && !usedOn.containsOneItem(35998)) {
                player.sendMessage("That player does not have a skilling backpack/gem.");
                return;
            }
            player.getWalkSteps().clear();
            player.getDialogueManager().startDialogue("StartCoOpPartyD", usedOn, otherHasAdvanced, hasAdvanced);
        }
    }
}