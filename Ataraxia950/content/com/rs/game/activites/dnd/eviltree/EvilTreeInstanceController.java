package com.rs.game.activites.dnd.eviltree;

import com.google.common.collect.ImmutableList;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.dnd.eviltree.dialogue.DestroyInstanceD;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.bossInstance.BossInstanceController;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class EvilTreeInstanceController extends BossInstanceController {

    private static final ImmutableList<String> MESSAGES = ImmutableList.of(
            "Friends can join ET instances by speaking with the Evil Tree Hunter at ;;home!",
            "Evil tree types range from Normal -> Elder, which have 8k and 20k HP respectively.",
            "You can use items on the Evil Tree Hunter, and he will note/unnote them for free.",
            "You can destroy your instances by right clicking the portal or using ;;destroyeviltree."
    );
    private EvilTreeInstance instance;
    private int stumpTicks;
    private int msgTicks;

    @Override
    public void start() {
        super.start();
        instance = (EvilTreeInstance) getInstance();
        EvilTree.openInter(player, instance.getTree());
        instance.deleteStump();
        stumpTicks = 50;
        msgTicks = 100;
    }


    @Override
    public boolean processObjectClick1(WorldObject object) {
        if (object.getId() == 15645) {
            if (instance.getTree().getTreeObject() == null || instance.getTree().allRewardsClaimed()) {
                instance.destroy("The instance has been destroyed because all rewards were claimed.");
            } else {
                player.getInterfaceManager().removeMinigameHudInterface();
                instance.leaveInstance(player, BossInstance.EXITED);
                player.getControlerManager().forceStop();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectClick2(WorldObject object) {
        if (object.getId() == 15645) {
            String owner = instance.getOwnerUsername();
            if (owner.equalsIgnoreCase(player.getUsername())) {
                player.getDialogueManager().startDialogue(new DestroyInstanceD());
            } else {
                player.sendMessage("Only " + owner + " can do this.");
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        player.sendMessage("Please use the portal to exit the instance.");
        return false;
    }

    @Override
    public boolean login() {
        instance = (EvilTreeInstance) BossInstanceHandler.joinInstance(player, Boss.Evil_Tree, player.getLastBossInstanceKey(), false);
        if (instance != null) {
            EvilTree.openInter(player, instance.getTree());
            return false;
        }
        return true;
    }

    @Override
    public boolean logout() {
        player.sendMessage("Please use the portal to exit the instance.");
        return false;
    }

    @Override
    public void forceClose() {
        super.forceClose();
        player.getInterfaceManager().removeMinigameHudInterface();
    }

    @Override
    public void process() {
        if (--stumpTicks <= 0) {
            // For some reason this stump just keeps reappearing, so delete it every 50 ticks.
            instance.deleteStump();
            stumpTicks = 50;
        }
        if (--msgTicks <= 0) {
            for (Player player : instance.getPlayers()) {
                if (player != null) {
                    player.sendMessage(Colors.CYAN + "[Instance]: " + Utils.randomFrom(MESSAGES));
                }
            }
            msgTicks = 400;
        }

        if (player.evilTreeInter && !instance.getTree().isAlive()) {
            player.getInterfaceManager().removeMinigameHudInterface();
            return;
        }
        EvilTree.updateInter(player, this, instance.getTree());
    }

    @Override
    public boolean sendDeath() {
        EvilTreeHandler.clearDamage(player, instance.getTree());
        return true;
    }
}