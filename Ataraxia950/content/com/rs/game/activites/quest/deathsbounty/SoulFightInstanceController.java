package com.rs.game.activites.quest.deathsbounty;

import com.google.common.base.Stopwatch;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.npc.NPC;
import com.rs.game.player.controllers.bossInstance.BossInstanceController;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.time.LocalDateTime;

public final class SoulFightInstanceController extends BossInstanceController {

    private NPC husbandMich;
    private boolean finished;
    private WorldObject portalCover;
    private SoulFightInstance instance;
    private transient final Stopwatch stopwatch = Stopwatch.createStarted();

    @Override
    public boolean login() {
        BossInstanceHandler.Boss boss = BossInstanceHandler.Boss.Husband_Mich;
        if (boss != null)
            player.setNextWorldTile(new WorldTile(boss.getOutsideTile()));
        removeControler();
        return false;
    }

    @Override
    public boolean logout() {
        super.logout();
        player.getControlerManager().forceStop();
        return true;
    }

    @Override
    public void start() {
        super.start();
        instance = (SoulFightInstance) getInstance();
        portalCover = new WorldObject(0, 10, 0, instance.getTile(3420, 5272, 0));
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.getPackets().sendGraphics(new Graphics(1638), getInstance().getTile(3419, 5277, 0));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        husbandMich = new HusbandMichNPC(SoulFightInstanceController.this);
                        husbandMich.setNextForceTalk(new ForceTalk("You'll never take my soul!"));
                        husbandMich.setBonus(2, 500);
                        husbandMich.setMagicBonuses(1000);
                        husbandMich.setNoDistanceCheck(true);
                        player.faceEntity(husbandMich);
                        husbandMich.setTarget(player);
                        husbandMich.setForceAgressive(true);
                    }
                }, 2);
            }
        }, 3);

        World.spawnObject(portalCover);
    }

    @Override
    public boolean sendDeath() {
        player.getInterfaceManager().closeOverlay(false);
        if (husbandMich != null && !husbandMich.hasFinished())
            husbandMich.finish();
        return super.sendDeath();
    }

    @Override
    public boolean processMagicTeleport(WorldTile toTile) {
        player.getControlerManager().forceStop();
        return true;
    }

    @Override
    public void forceClose() {
        super.forceClose();
        player.getInterfaceManager().closeOverlay(false);
        if (husbandMich != null && !husbandMich.hasFinished())
            husbandMich.finish();
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (World.containsObjectWithId(portalCover, portalCover.getId()))
                    World.removeObject(portalCover);
            }
        }, 3);
    }

    @Override
    public void process() {
        if (instance.isFinished() || player.hasFinished()) {
            player.getControlerManager().forceStop();
            return;
        }
        if (husbandMich != null) {
            if (husbandMich.hasFinished() && !finished) {
                finished = true;
                player.setNextGraphics(new Graphics(1638));
                player.sendMessage(Colors.RED + "His soul has been sent back to death!");
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        if(!player.quests.isCompleted(DeathsBounty.class)) {
                            player.quests.advanceStage(DeathsBounty.class);
                        }
                        player.lastDeathSoul = LocalDateTime.now().plusDays(1);
                        player.getControlerManager().forceStop();
                    }
                }, 3);
                return;
            }
            player.getInterfaceManager().sendOverlay(46, false);
            player.getPackets().sendIComponentText(46, 15, "");
            player.getPackets().sendIComponentText(46, 12, "");
            player.getPackets().sendIComponentText(46, 14, "Health: ");
            player.getPackets().sendIComponentText(46, 11, Colors.RED + Utils.formatNumber(husbandMich.getHitpoints()));
            player.getPackets().sendIComponentText(46, 13, "");
            player.getPackets().sendIComponentText(46, 10, "");
        }
    }
}
