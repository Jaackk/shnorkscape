package com.rs.game.player.controllers;

import com.rs.game.Entity;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.content.dungeoneering.DungManager;
import com.rs.game.player.content.eds.EliteDungeonPartyManager;
import com.rs.game.player.content.eds.EliteDungeonsManager;

public class EliteDungeonsLobby extends Controller {

    private boolean showingOption;

    @Override
    public boolean processPlayerOption1(Entity target) {
        Object oManager = player.getEliteDungeonsManager().getParty() != null ? player.getEliteDungeonsManager() : player.getDungeoneeringManager().getParty() != null ? player.getDungeoneeringManager() : null;
        if (target instanceof Player) {
            Player p2 = (Player) target;
            if (player.isInsideAnyDungParty()) {
                if (oManager instanceof EliteDungeonsManager) {
                    EliteDungeonsManager manager = ((EliteDungeonsManager) oManager);
                    if (manager.getParty().isLeader(player))
                        player.getEliteDungeonsManager().invite(p2.getDisplayName());
                    else
                        player.getPackets().sendGameMessage("Only the party leader can invite players.");
                } else if (oManager instanceof DungManager) {
                    DungManager manager = ((DungManager) oManager);
                    if (manager.getParty().isLeader(player))
                        player.getDungeoneeringManager().invite(p2.getDisplayName());
                    else
                        player.getPackets().sendGameMessage("Only the party leader can invite players.");
                }
                return false;
            } else {
                if (player.getTemporaryAttributtes().get(Key.ELITE_DUNGEON_TYPE) != null) {
                    boolean eliteDungeon = (boolean) player.getTemporaryAttributtes().remove(Key.ELITE_DUNGEON_TYPE);
                    if (player.isInsideAnyDungParty()) {
                        if (p2.getEliteDungeonsManager().invitingPlayer == player)
                            p2.getEliteDungeonsManager().expireInvitation();
                        if (p2.getDungeoneeringManager().invitingPlayer == player)
                            p2.getDungeoneeringManager().expireInvitation();
                        player.getPackets().sendGameMessage("You are already in a party, You can only enter one party at a time.");
                        return false;
                    }
                    if (eliteDungeon)
                        player.getEliteDungeonsManager().invite(p2.getDisplayName());
                    else
                        player.getDungeoneeringManager().invite(p2.getDisplayName());
                    return false;
                } else {
                    player.getPackets().sendGameMessage("You need to be in a party to invite players.");
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        if (object.getId() == 103952) {
            EliteDungeonPartyManager.enterDungeon(player, 1);
            return false;
        }
        if (object.getId() == 111737) {
            EliteDungeonPartyManager.enterDungeon(player, 2);
            return false;
        }
        if (object.getId() == 5999) {
            EliteDungeonPartyManager.enterDungeon(player, 3);
            return false;
        }
        if (object.getDefinitions().getActualName().equalsIgnoreCase("treasure chest")) {
            player.getEliteDungeonsManager().openRewardsChest();
            return false;
        }
        return true;
    }

    @Override
    public void forceClose() {
        super.forceClose();
        setInviteOption(false);
    }

    @Override
    public boolean login() {
        moved();
        return false;
    }

    @Override
    public boolean logout() {
        return false; // so doesnt remove script
    }

    @Override
    public void moved() {
        if (!isAtEliteDungeonLobbyAreas(player)) {
            setInviteOption(false);
            removeControler();

        } else
            setInviteOption(true);
    }

    @Override
    public boolean sendDeath() {
        setInviteOption(false);
        removeControler();
        return true;
    }

    public void setInviteOption(boolean show) {
        if (show == showingOption)
            return;
        showingOption = show;
        player.getPackets().sendPlayerOption(show ? "Invite" : "null", 1, false);
    }

    @Override
    public void start() {
        setInviteOption(true);
    }

    public static boolean isAtEliteDungeonLobbyAreas(WorldTile tile) {
        return (tile.getX() >= 2081 && tile.getY() >= 11339 && tile.getX() <= 2110 && tile.getY() <= 11360)// ed1
                || (tile.getX() >= 3360 && tile.getY() >= 3875 && tile.getX() <= 3385 && tile.getY() <= 3895)// ed2
                || (tile.getX() >= 3500 && tile.getY() >= 3684 && tile.getX() <= 3522 && tile.getY() <= 3704);// ed3
    }

    @Override
    public boolean processButtonClick(int interfaceId, int componentId, int slotId, int itemId, int packetId) {
        if (interfaceId == 168) {
            player.getEliteDungeonsManager().HandleButtons(interfaceId, componentId, slotId, itemId, packetId);
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick2(WorldObject object) {
        if (object.getDefinitions().getActualName().equalsIgnoreCase("treasure chest")) {
            player.getEliteDungeonsManager().toggleAutoLoot();
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick3(WorldObject object) {
        if (object.getDefinitions().getActualName().equalsIgnoreCase("treasure chest")) {
            player.getPackets().sendGameMessage("This option will be added when more elite dungeons are coded.");
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick4(WorldObject object) {
        if (object.getDefinitions().getActualName().equalsIgnoreCase("treasure chest")) {
            if (!player.promptList()) {
                player.getBank().openBank();
            } else {
                player.getDialogueManager().startDialogue("BankList", false);
            }
            return false;
        }
        return true;
    }
}
