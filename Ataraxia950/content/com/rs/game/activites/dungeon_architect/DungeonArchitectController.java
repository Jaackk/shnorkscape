package com.rs.game.activites.dungeon_architect;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.controllers.bossInstance.BossInstanceController;

public final class DungeonArchitectController extends BossInstanceController {

    private DungeonArchitectInstance instance;

    @Override
    public void start() {
        super.start();
        instance = (DungeonArchitectInstance) getInstance();
    }

    @Override
    public void forceClose() {
        super.forceClose();
        player.getInterfaceManager().closeOverlay(false);
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        if (object.getId() == 74625) {
            if (!destroy()) {
                player.getControlerManager().forceStop();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        destroy();
        return true;
    }

    @Override
    public boolean logout() {
        destroy();
        return true;
    }

    @Override
    public boolean processCommand(String command, boolean a, boolean b) {
        String cmd = command.toLowerCase();
        return !cmd.equals("killme");
    }

    public boolean destroy() {
        if (player.getUsername().equals(instance.getOwnerUsername())) {
            instance.destroy();
            return true;
        }
        return false;
    }
}