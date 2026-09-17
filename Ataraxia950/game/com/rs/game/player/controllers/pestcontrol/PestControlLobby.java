package com.rs.game.player.controllers.pestcontrol;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.pest.Lander;
import com.rs.game.player.controllers.Controller;
import com.rs.utils.Utils;

public final class PestControlLobby extends Controller {

    private int landerId;

    @Override
    public boolean canSummonFamiliar() {
        player.getPackets().sendGameMessage("You feel it's best to keep your familiar away during this game.");
        return false;
    }

    @Override
    public void forceClose() {
        player.getInterfaceManager().removeMinigameHudInterface();
        Lander.getLanders()[landerId].exitLander(player);
    }

    @Override
    public boolean logout() {
        Lander.getLanders()[landerId].remove(player);// to stop the timer in the
        // lander and prevent
        // future errors
        return false;
    }

    @Override
    public void magicTeleported(int teleType) {
        player.getControlerManager().forceStop();
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        player.getControlerManager().forceStop();
        return true;
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        player.getControlerManager().forceStop();
        return true;
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        switch (object.getId()) {
            case 14314:
            case 25629:
            case 25630:
            case 102761:
            case 102758:
            case 102760:
                player.getDialogueManager().startDialogue("LanderD");
                return true;
        }
        return true;
    }

    @Override
    public void sendInterfaces() {
        Lander lander = Lander.getLanders()[landerId];
        player.getPackets().sendIComponentText(407, 3, Utils.fixChatMessage(lander.toString()));
        int seconds = (lander.getTimer().getSeconds());
        player.getPackets().sendIComponentText(407, 13, "Next Departure: " + (lander.confirmationPeriod ? "Awaiting confirmation..." : seconds + " seconds"));
        player.getPackets().sendIComponentText(407, 14, "Player's Ready: " + lander.getPlayers().size());
        player.getPackets().sendIComponentText(407, 15, "(Need 5 to 25 players)");
        player.getPackets().sendIComponentText(407, 16, "Commendations: " + player.getPestPoints());
        player.getInterfaceManager().sendMinigameHudInterface(407);
    }

    @Override
    public void start() {
        this.landerId = (Integer) getArguments()[0];
        sendInterfaces();
    }
}