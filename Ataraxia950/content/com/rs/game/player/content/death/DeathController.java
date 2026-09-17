package com.rs.game.player.content.death;

import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.npc.NPC;
import com.rs.game.player.controllers.bossInstance.BossInstanceController;

/**
 * @author lare96
 */
public final class DeathController extends BossInstanceController {

    @Override
    public boolean login() {
        Boss boss = Boss.Death;
        if (boss != null)
            player.setNextWorldTile(new WorldTile(boss.getOutsideTile()));
        removeControler();
        return false;
    }

    @Override
    public boolean logout() {
        getDeathInstance().leaveInstance(player, BossInstance.LOGGED_OUT);
        removeControler();
        return true;
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        if (!player.heardDeathsWarning) {
            player.sendMessage("You should speak with Death before leaving.");
            return false;
        }
        removeControler();
        player.setNextWorldTile(new WorldTile(3222, 3218, 0));
        return true;
    }

    @Override
    public boolean processItemTeleport(WorldTile toTile) {
        if (!player.heardDeathsWarning) {
            player.sendMessage("You should speak with Death before leaving.");
            return false;
        }
        removeControler();
        player.setNextWorldTile(new WorldTile(3222, 3218, 0));
        return true;
    }

    @Override
    public boolean processObjectTeleport(WorldTile toTile) {
        if (!player.heardDeathsWarning) {
            player.sendMessage("You should speak with Death before leaving.");
            return false;
        }
        removeControler();
        player.setNextWorldTile(new WorldTile(3222, 3218, 0));

        return true;
    }

    public DeathInstance getDeathInstance() {
        return (DeathInstance) getInstance();
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        if (object.getId() == 45803) {
            if (!player.heardDeathsWarning) {
                player.sendMessage("You should speak with Death before leaving.");
                return false;
            }
            removeControler();
            player.setNextWorldTile(new WorldTile(3222, 3218, 0));
            return false;
        }
        return true;
    }

    @Override
    public boolean processNPCClick1(NPC npc) {
        if (npc.getId() == 14386) {
            if (player.heardDeathsWarning) {
                player.getDialogueManager().startDialogue("DeathMainD");
            } else {
                player.getDialogueManager().startDialogue("DeathWarningD");
            }
            return false;
        }
        return true;
    }
}