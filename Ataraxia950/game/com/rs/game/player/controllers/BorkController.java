package com.rs.game.player.controllers;

import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.MapInstance;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.Bork;
import com.rs.game.player.MusicsManager;
import com.rs.game.player.Player;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.cutscenes.Cutscene;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.util.concurrent.TimeUnit;

/**
 * Handles the Bork's Controller.
 *
 * @author Noel
 */
public class BorkController extends Controller {

    private static final WorldTile OUTSIDE = new WorldTile(3143, 5545, 0);
    private MapInstance instance;
    private NPC surokMagis;
    private Bork bork;
    private boolean earthquake;
    private int timer;

    public static boolean canKillBork(Player player) {
        return Utils.currentTimeMillis()
                - player.getLastBork() > ((player.isUltimateDonator() ? 1
                : player.isSupremeDonator() ? 2
                : player.isLegendaryDonator() ? 3
                : player.isExtremeDonator() ? 4 : player.isDonator() ? 5 : 6)
                * 60 * 60 * 1000)
                || Settings.DEBUG;
    }

    public static void enterBork(Player player) {
        if (!canKillBork(player)) {
            player.getDialogueManager().startDialogue("SimpleMessage",
                    "The portal appears to have stopped working for now. " + "Perhaps you should return later?");
            return;
        }
        player.getControlerManager().startControler("BorkController");
    }

    @Override
    public void start() {
        enter();
    }

    @Override
    public boolean processObjectClick1(WorldObject object) {
        if (object.getId() == 77745) {
            leave(2);
            return false;
        }
        return true;
    }

    @Override
    public boolean processNPCClick1(NPC npc) {
        if (npc.getId() == 7136 || npc.getId() == 7137) {
            player.getPackets().sendGameMessage("Your attack has no effect.");
            return false;
        }
        return true;
    }

    public void enter() {
        instance = new MapInstance(385, 690);
        player.lock();
        player.setNextAnimation(new Animation(17803));
        final long time = FadingScreen.fade(player);
        instance.load(() ->
                FadingScreen.unfade(player, time, () -> {
                    player.setNextWorldTile(instance.getTile(25, 17));
                    surokMagis = World.spawnNPC(player.getLastBork() != 0 ? 7137 : 7136, instance.getTile(22, 20),
                            -1, true, true);
                    bork = new Bork(instance.getTile(12, 15), BorkController.this);
                    sendCutscene(692, 10000);
                    player.setForceMultiArea(true);
                    bork.setForceMultiArea(true);
                    surokMagis.setCantInteract(true);
                }));
    }

    // 0 - logout
    // 1 - teleport / death
    // 2 - leave
    public void leave(int type) {
        player.setForceMultiArea(false);
        player.stopAll();
        if (type != 0) {
            if (type == 1)
                player.lock(3);
            else
                player.useStairs(17803, OUTSIDE, 2, 3);
            player.getMusicsManager().reset();
            if (earthquake)
                player.getPackets().sendStopCameraShake();
            removeControler();
        } else
            player.setLocation(OUTSIDE);
        instance.destroy(null);
    }

    @Override
    public boolean logout() {
        leave(0);
        return true;
    }

    @Override
    public boolean login() {
        player.setNextWorldTile(OUTSIDE);
        return true; // shouldnt happen
    }

    @Override
    public void magicTeleported(int type) {
        leave(1);
    }

    @Override
    public boolean sendDeath() {
        player.lock(8);
        player.stopAll();
        if (player.isGroupIronman()) {
            player.gimTracker.incrementDeaths("<#player> died while fighting Bork");
        }
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    player.setNextAnimation(player.getDeathAnimation());
                    player.deathItemsManager.handleDeath();
                } else if (loop == 1) {
                    player.getPackets().sendGameMessage("Oh dear, you have died.");
                } else if (loop == 3) {
                    leave(1);
                } else if (loop == 4) {
                    player.getPackets().sendMusicEffect(MusicsManager.DEATH_MUSIC_EFFECT);
                    stop();
                }
                loop++;
            }
        }, 0, 1);
        return false;

    }

    public void startFight() {
        player.unlock();
        bork.setCantInteract(false);
        surokMagis.setTarget(player);
    }

    public void startEarthquake() {
        player.unlock();
        player.getPackets().sendGameMessage("Something is shaking the whole cavern! You should get out of here quick!");
        player.getPackets().sendCameraShake(3, 12, 25, 12, 25);
        earthquake = true;
        timer = 30;
    }

    @Override
    public void process() {
        if (!earthquake)
            return;
        if (timer > 0) {
            timer--;
            return;
        }
        player.applyHit(new Hit(player, Utils.random(499) + 1, HitLook.REGULAR_DAMAGE));
        timer = 30;
    }

    public void spawnMinions() {
        player.getPackets().sendGameMessage("Bork strikes the ground with his axe.");
        sendCutscene(691, 4200);
    }

    public void killBork() {
        bork = null;
        sendCutscene(693, 5400);
        surokMagis.setCantInteract(true);
        surokMagis.setNextFaceEntity(null);
        surokMagis.setNextWorldTile(instance.getTile(22, 20));
        player.setLastBork(Utils.currentTimeMillis());
        player.sendMessage("You've killed the Bork! Total Bork kills: " + Colors.RED
                + Utils.getFormattedNumber(player.increaseKillStatistics("bork", true)) + "</col>.");
    }

    public void sendCutscene(final int interfaceId, long time) {
        player.lock();
        player.getMusicsManager().forcePlayMusic(587);
        player.getInterfaceManager().sendLoadingScreen(interfaceId);
        player.getPackets().sendBlackOut(2);
        CoresManager.getServiceProvider().executeWithDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    player.getInterfaceManager().closeLoadingScreen();
                    if (interfaceId == 692) {
                        player.resetReceivedDamage();
                        player.getDialogueManager().startDialogue("SurokMagis", surokMagis.getId(),
                                BorkController.this);
                        player.getPackets().sendBlackOut(0);
                    } else if (interfaceId == 693) {
                        WorldTile lookTo = instance.getTile(22, 22);
                        player.getPackets().sendCameraLook(Cutscene.getX(player, lookTo.getX()),
                                Cutscene.getY(player, lookTo.getY()), 1000);
                        WorldTile posTile = instance.getTile(22, 15);
                        player.getPackets().sendCameraPos(Cutscene.getX(player, posTile.getX()),
                                Cutscene.getY(player, posTile.getY()), 1500);
                        surokMagis.setNextFaceWorldTile(posTile);
                        surokMagis.setNextForceTalk(new ForceTalk("Zamorak, avenge me!"));
                        WorldTasksManager.schedule(new WorldTask() {

                            boolean teleported;

                            @Override
                            public void run() {
                                if (!teleported) {
                                    surokMagis.setNextAnimation(new Animation(8939));
                                    surokMagis.setNextGraphics(new Graphics(1576));
                                    teleported = true;
                                } else {
                                    surokMagis.finish();
                                    surokMagis = null;
                                    player.getDialogueManager().startDialogue("SurokMagisT", BorkController.this);
                                    player.getPackets().sendBlackOut(0);
                                    player.getPackets().sendResetCamera();
                                    stop();
                                }
                            }

                        }, 6, 3);
                    } else {
                        if (interfaceId == 691)
                            bork.setMinions();
                        player.unlock();
                        player.getPackets().sendBlackOut(0);
                    }
                } catch (Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, time, TimeUnit.MILLISECONDS);
    }
}