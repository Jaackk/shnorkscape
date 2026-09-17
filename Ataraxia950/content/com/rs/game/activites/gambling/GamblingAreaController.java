package com.rs.game.activites.gambling;

import com.rs.cores.CoresManager;
import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.activites.gambling.flowerpoker.FlowerPokerSession;
import com.rs.game.activites.gambling.flowerpoker.session.FlowerPokerSetup;
import com.rs.game.player.Player;
import com.rs.game.player.content.FriendChatsManager;
import com.rs.game.player.content.Magic;
import com.rs.game.player.controllers.Controller;

public class GamblingAreaController extends Controller {
    @Override
    public void start() {
        Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(2447, 3090, 0));
        addPlayerOptions();
        if (player.getCurrentFriendChat() != null && !player.getCurrentFriendChatOwner().equals("gambling"))
            player.getCurrentFriendChat().leaveChat(player, false);
        FriendChatsManager.joinChat("gambling", player);
    }

    @Override
    public boolean login() {
        CoresManager.getServiceProvider().executeWithDelay(this::addPlayerOptions, 1);
        return false;
    }

    private void removePlayerOptions() {
        player.getPackets().sendPlayerOption("null", 1, false);
        player.getPackets().sendPlayerOption("Trade with", 4, false);
    }

    private void addPlayerOptions() {
        player.getPackets().sendPlayerOption("Gamble with", 1, false);
        player.getPackets().sendPlayerOption("null", 4, false);
    }

    @Override
    public boolean processPlayerOption1(Entity target) {
        if (target instanceof Player) {
            Player p2 = (Player) target;
            if (p2.getControlerManager().getControler() instanceof GamblingAreaController) {
                if (!player.isOwner()) {
                    if (player.getCurrentFriendChat() == null || !player.getCurrentFriendChatOwner().equals("gambling")) {
                        player.sendMessage("You need to be in the 'Gambling' Friends chat to gamble with somebody.");
                        return false;
                    }
                    if (p2.getCurrentFriendChat() == null || !p2.getCurrentFriendChatOwner().equals("gambling")) {
                        player.sendMessage("The other player needs to be in the 'Gambling' Friends chat.");
                        return false;
                    }
                }
                if (p2.getTemporaryAttributtes().getOrDefault(FlowerPokerSession.BETTING_FLOWER_POKER_KEY, false) == Boolean.TRUE || p2.getTemporaryAttributtes().getOrDefault("ADDING_DICE", false) == Boolean.TRUE) {
                    player.sendMessage("That player is currently busy.");
                    return false;
                }
                player.getDialogueManager().startDialogue("GambleWithDialogue", p2);
            }
            return false;
        }
        return super.processPlayerOption1(target);
    }

    @Override
    public boolean processPlayerOption4(Player target) {
        Player targetTempAttribute = (Player) target.getTemporaryAttributtes().get(FlowerPokerSession.FLOWER_POKER_REQUEST_TARGET_KEY);
        if (targetTempAttribute != null) {
            handleFlowerPokerRequest(player, target);
        }
        return false;
    }

    static void handleFlowerPokerRequest(Player player, Player target) {
        if (!target.getInterfaceManager().containsScreenInter()) {
            target.getTemporaryAttributtes().remove(FlowerPokerSession.FLOWER_POKER_REQUEST_TARGET_KEY);
            FlowerPokerSession flowerPokerSession = new FlowerPokerSetup(player, target);
            player.setFlowerPokerSession(flowerPokerSession);
            target.setFlowerPokerSession(flowerPokerSession);
            player.faceEntity(target);
            target.faceEntity(player);
            flowerPokerSession.start();
        } else {
            player.sendMessage("That player is currently busy.");
        }
    }

    private void leave() {
        removePlayerOptions();
        removeControler();
    }

    @Override
    public boolean logout() {
        return false;
    }

    @Override
    public void forceClose() {
        leave();
    }

    @Override
    public boolean processMagicTeleport(WorldTile to) {
        leave();
        return true;
    }

    @Override
    public boolean processItemTeleport(WorldTile to) {
        leave();
        return true;
    }

    @Override
    public boolean sendDeath() {
        leave();
        return true;
    }
}
