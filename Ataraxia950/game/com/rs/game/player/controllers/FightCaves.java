package com.rs.game.player.controllers;

import java.util.List;

import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.MapBuilder;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.fightcaves.FightCavesNPC;
import com.rs.game.npc.fightcaves.TzKekCaves;
import com.rs.game.npc.fightcaves.TzTok_Jad;
import com.rs.game.player.ActivityTimersManager.ActivityTimers;
import com.rs.game.player.Player;
import com.rs.game.player.actions.summoning.Summoning;
import com.rs.game.player.content.dropcollection.DropCollectionConstants;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;

public class FightCaves extends Controller {

    public static final WorldTile OUTSIDE = new WorldTile(4610, 5130, 0);

    private static final int THHAAR_MEJ_JAL = 2617;

    private static final int[] MUSICS = { 1088, 1082, 1086 };
    private final int[][] WAVES = { { 2734 }, { 2734, 2734 }, { 2736 }, { 2736, 2734 }, { 2736, 2734, 2734 }, { 2736, 2736 }, { 2739 }, { 2739, 2734 }, { 2739, 2734, 2734 }, { 2739, 2736 }, { 2739, 2736, 2734 }, { 2739, 2736, 2734, 2734 }, { 2739, 2736, 2736 }, { 2739, 2739 }, { 2741 }, { 2741, 2734 }, { 2741, 2734, 2734 }, { 2741, 2736 }, { 2741, 2736, 2734 }, { 2741, 2736, 2734, 2734 }, { 2741, 2736, 2736 }, { 2741, 2739 }, { 2741, 2739, 2734 }, { 2741, 2739, 2734, 2734 }, { 2741, 2739, 2736 }, { 2741, 2739, 2736, 2734 }, { 2741, 2739, 2736, 2734, 2734 }, { 2741, 2739, 2736, 2736 }, { 2741, 2739, 2739 }, { 2741, 2741 }, { 2743 }, { 2743, 2734 }, { 2743, 2734, 2734 }, { 2743, 2736 }, { 2743, 2736, 2734 }, { 2743, 2736, 2734, 2734 }, { 2743, 2736, 2736 }, { 2743, 2739 }, { 2743, 2739, 2734 }, { 2743, 2739, 2734, 2734 }, { 2743, 2739, 2736 }, { 2743, 2739, 2736, 2734 }, { 2743, 2739, 2736, 2734, 2734 }, { 2743, 2739, 2736, 2736 }, { 2743, 2739, 2739 }, { 2743, 2741 }, { 2743, 2741, 2734 }, { 2743, 2741, 2734, 2734 }, { 2743, 2741, 2736 }, { 2743, 2741, 2736, 2734 }, { 2743, 2741, 2736, 2734, 2734 }, { 2743, 2741, 2736, 2736 }, { 2743, 2741, 2739 }, { 2743, 2741, 2739, 2734 }, { 2743, 2741, 2739, 2734, 2734 }, { 2743, 2741, 2739, 2736 }, { 2743, 2741, 2739, 2736, 2734 }, { 2743, 2741, 2739, 2736, 2734, 2734 }, { 2743, 2741, 2739, 2736, 2736 }, { 2743, 2741, 2739, 2739 }, { 2743, 2741, 2741 }, { 2743, 2743 }, { 2745 } };
    public boolean spawned;
    public int selectedMusic;
    private int[] boundChuncks;
    private Stages stage;
    private boolean logoutAtEnd;
    private boolean login;

    public static void enterFightCaves(final Player player, boolean skip) {
        if (player.getFamiliar() != null || player.getPet() != null || Summoning.hasPouch(player) || Pets.hasPet(player)) {
            player.getDialogueManager().startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL, "No Kimit-Zil in the pits! This is a challenge for you, not your friends!");
            player.sendMessage("Maybe you should dismiss your pet before entering the fight caves!");
            return;
        }
        player.getControlerManager().startControler("FightCavesControler", skip ? 32 : 1);
        player.getActivityTimersManager().startTimer(ActivityTimers.FIGHT_CAVES);
    }

    public void playMusic() {
        player.getMusicsManager().playMusic(selectedMusic);
    }

    @Override
    public void start() {
        loadCave(false);
    }

    @Override
    public boolean processButtonClick(final int interfaceId, final int componentId, final int slotId, final int slotId2, final int packetId) {
        return stage == Stages.RUNNING;
    }

    @Override
    public boolean canLogout() {
        if (!logoutAtEnd) {
            logoutAtEnd = true;
            player.sendMessage("<col=ff0000>You will be logged out automatically at the end of this wave.");
            player.sendMessage("<col=ff0000>If you log out sooner, you will have to repeat this wave.");
        } else {
            player.logout(false);
        }
        return false;
    }

    /**
     * return process normaly
     */
    @Override
    public boolean processObjectClick1(final WorldObject object) {
        if (object.getId() == 9357) {
            if (stage != Stages.RUNNING) {
                return false;
            }
            exitCave(1);
            return false;
        }
        return true;
    }

    /*
     * return false so wont remove script
     */
    @Override
    public boolean login() {
        loadCave(true);
        return false;
    }

    public void loadCave(final boolean login) {
        this.login = login;
        stage = Stages.LOADING;
        player.lock(5);
        CoresManager.getServiceProvider().executeNow(() -> {
            boundChuncks = MapBuilder.findEmptyChunkBound(8, 8);
            MapBuilder.copyAllPlanesMap(552, 640, boundChuncks[0], boundChuncks[1], 8);
            selectedMusic = MUSICS[Utils.random(MUSICS.length)];
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.setNextWorldTile(!login ? getWorldTile(46, 61) : getWorldTile(32, 32));
                    if (!login) {
                        final WorldTile walkTo = getWorldTile(32, 32);
                        player.addWalkSteps(walkTo.getX(), walkTo.getY());
                    }
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL, "You're on your own now, JalYt.<br>Prepare to fight for your life!");
                    player.setForceMultiArea(true);
                    playMusic();
                    player.unlock(); // unlocks player
                    stage = Stages.RUNNING;
                }

            }, 1);
            if (!login) {
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        if (stage != Stages.RUNNING) {
                            return;
                        }
                        try {
                            startWave();
                        } catch (final Throwable t) {
                            Logger.getGlobal().catching(t);
                        }
                    }
                }, 10);
            }
        });
    }

    public WorldTile getSpawnTile() {
        switch (Utils.random(5)) {
        case 0:
            return getWorldTile(11, 16);
        case 1:
            return getWorldTile(51, 25);
        case 2:
            return getWorldTile(10, 50);
        case 3:
            return getWorldTile(46, 49);
        case 4:
        default:
            return getWorldTile(32, 30);
        }
    }

    @Override
    public void moved() {
        if (stage != Stages.RUNNING || !login) {
            return;
        }
        login = false;
        setWaveEvent();
    }

    public void startWave() {
        final int currentWave = getCurrentWave();
        if (currentWave > WAVES.length) {
            win();
            return;
        }
        player.getInterfaceManager().sendMinigameHudInterface(316);
        player.getPackets().sendConfigByFile(15151, currentWave);
        player.getPackets().sendHideIComponent(316, 1, currentWave == 0);
        player.getPackets().sendHideIComponent(316, 5, currentWave == 0);
        player.sendMessage("Currently on wave: " + Colors.RED + currentWave + "</col>.");
        if (stage != Stages.RUNNING) {
            return;
        }
        for (final int id : WAVES[currentWave - 1]) {
            NPC npc;
            if (id == 2736) {
                npc = new TzKekCaves(id, getSpawnTile());
            } else if (id == 2745) {
                npc = new TzTok_Jad(id, getSpawnTile(), this);
            } else {
                npc = new FightCavesNPC(id, getSpawnTile());
            }
            npc.setTarget(player);
        }
        spawned = true;
    }

    public void spawnHealers() {
        if (stage != Stages.RUNNING) {
            return;
        }
        for (int i = 0; i < 4; i++) {
            new FightCavesNPC(2746, getSpawnTile());
        }
    }

    public void win() {
        if (stage != Stages.RUNNING) {
            return;
        }
        exitCave(4);
    }

    public void nextWave() {
        playMusic();
        setCurrentWave(getCurrentWave() + 1);
        if (logoutAtEnd) {
            player.logout(false);
            return;
        }
        setWaveEvent();
    }

    public void setWaveEvent() {
        if (getCurrentWave() == 63) {
            player.getDialogueManager().startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL, "Look out, here comes TzTok-Jad!");
        }
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    if (stage != Stages.RUNNING) {
                        return;
                    }
                    startWave();
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        });
    }

    @Override
    public void process() {
        if (spawned) {
            if (!player.withinDistance(getWorldTile(32, 32), 70)) {
                return;
            }
            final List<Integer> npcs = World.getRegion(player.getRegionId()).getNPCsIndexes();
            int npcsAlive = 0;
            if (npcs != null && !npcs.isEmpty())
                for (int npcIndex : npcs) {
                    NPC npc = World.getNPCs().get(npcIndex);
                    if (!(npc instanceof FightCavesNPC))
                        continue;
                    if (npc != null && !npc.hasFinished() && !npc.isDead()) 
                        npcsAlive++;
                }
            if (npcsAlive == 0) {
                spawned = false;
                nextWave();
            }
        }
    }

    @Override
    public boolean sendDeath() {
        player.lock(7);
        player.stopAll();
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    player.setNextAnimation(new Animation(836));
                } else if (loop == 1) {
                    player.sendMessage("You have been defeated!");
                } else if (loop == 3) {
                    player.reset();
                    exitCave(1);
                    player.setNextAnimation(new Animation(-1));
                } else if (loop == 4) {
                    player.getPackets().sendMusicEffect(90);
                    player.unlock();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
        return false;
    }

    @Override
    public void magicTeleported(final int type) {
        exitCave(1);
    }

    /*
     * logout or not. if didnt logout means lost; 0 logout; 1 normal exit; 2 tele
     */
    public void exitCave(final int type) {
        stage = Stages.DESTROYING;
        player.getActivityTimersManager().finishTimer(ActivityTimers.FIGHT_CAVES, (type == 1), (type == 0));
        if (type == 0 || type == 2) {
            player.setNextWorldTile(new WorldTile(player.getHomeTile()));
        } else {
            player.setForceMultiArea(false);
            player.getInterfaceManager().removeMinigameHudInterface();
            if (type == 1 || type == 4) {
                player.setNextWorldTile(new WorldTile(player.getHomeTile()));
                if (type == 4) {
                    player.setCompletedFightCaves();
                    player.reset();
                    player.increaseKillStatistics("fight caves", true);
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL, "You even defeated TzTok-Jad, I am most impressed! Please accept this gift as a reward.");
                    World.sendWorldMessage("<img=7><col=CC0000>Server: " + player.getDisplayName() + " has just completed the Fight Caves minigame.", false);

                    QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/fire_cape.png\" height=17> " + player.getDisplayName() + " has just completed the Fight Caves minigame."));

                    addReward(player);
                } else if (getCurrentWave() <= 5) {
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL, "Well I suppose you tried... better luck next time.");
                } else {
                    final Item tokkul = new Item(6529, getCurrentWave() * 8032 / WAVES.length);
                    tokkul.setAmount((int) (tokkul.getAmount() * Settings.getDropQuantityRate(player)));
                    player.addItem(tokkul);
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", THHAAR_MEJ_JAL, "Well done in the cave, here, take TokKul as reward.");
                }
            }
            removeControler();
        }
        /*
         * 30 minutes before destroying the map
         */
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                MapBuilder.destroyMap(boundChuncks[0], boundChuncks[1], 8, 8);
            }
        }, 10);
    }

    /*
     * gets worldtile inside the map
     */
    public WorldTile getWorldTile(final int mapX, final int mapY) {
        return new WorldTile(boundChuncks[0] * 8 + mapX, boundChuncks[1] * 8 + mapY, 0);
    }

    /*
     * return false so wont remove script
     */
    @Override
    public boolean logout() {
        if (stage != Stages.RUNNING) {
            return false;
        }
        exitCave(0);
        return false;

    }

    public int getCurrentWave() {
        if (getArguments() == null || getArguments().length == 0) {
            return 0;
        }
        return (Integer) getArguments()[0];
    }

    public void setCurrentWave(final int wave) {
        if (getArguments() == null || getArguments().length == 0) {
            setArguments(new Object[1]);
        }
        getArguments()[0] = wave;
    }

    @Override
    public void forceClose() {
        if (stage != Stages.RUNNING) {
            return;
        }
        player.getInterfaceManager().removeMinigameHudInterface();
        exitCave(2);
    }

    public static final int JAD_PET_DR = 50;

    private void addReward(final Player player) {
        player.addItem(new Item(6570));
        player.getDropCollectionHandler().handleMinigames(DropCollectionConstants.MINIGAME_DATA.FIGHT_CAVES, 6570);
        final Item tokkul = new Item(6529, 16064);
        tokkul.setAmount((int) (tokkul.getAmount() * Settings.getDropQuantityRate(player)));
        player.addItem(tokkul);
        ChristmasSeasonalEvent.awardSmallPresent(player);
        if (Utils.random(JAD_PET_DR) == 0) {
            player.addItem(new Item(21512));
            player.getDropCollectionHandler().handleMinigames(DropCollectionConstants.MINIGAME_DATA.FIGHT_CAVES, 21512);
            World.sendWorldMessage("<img=6><col=ff9900>News: " + player.getDisplayName() + " has received the TzTok-Jad Pet", false);
            HcimNewsManager.getInstance().addNews(player, "<#player> got the TzTok-Jad pet");
        }
    }

    private enum Stages {
        LOADING, RUNNING, DESTROYING
    }

    @Override
    public boolean canSummonFamiliar() {
        player.getPackets().sendGameMessage("You can't summon familiars/pets in here.");
        return false;
    }

}
