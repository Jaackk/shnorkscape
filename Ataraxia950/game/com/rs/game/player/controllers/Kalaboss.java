package com.rs.game.player.controllers;

import com.rs.game.Entity;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.content.dungeoneering.DungManager;
import com.rs.game.player.content.eds.EliteDungeonsManager;

public class Kalaboss extends Controller {

    private boolean showingOption;

    public static boolean isAtKalaboss(WorldTile tile) {
        return tile.getX() >= 3385 && tile.getX() <= 3513 && tile.getY() >= 3605 && tile.getY() <= 3794;
    }

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
        if (player.getX() == 3385 && player.getY() == 3615) {
            setInviteOption(false);
            removeControler();
            player.getControlerManager().startControler("Wilderness");
        } else {
            if (!isAtKalaboss(player)) {
                setInviteOption(false);
                removeControler();
            } else
                setInviteOption(true);
        }
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
}
