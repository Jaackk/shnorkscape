package com.rs.game.player.controllers;

import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.MapBuilder;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.fightkiln.FightKilnNPC;
import com.rs.game.npc.fightkiln.HarAken;
import com.rs.game.npc.fightkiln.TokHaarKetDill;
import com.rs.game.player.ActivityTimersManager.ActivityTimers;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.FadingScreen;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.dropcollection.DropCollectionConstants;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.cutscenes.Cutscene;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;

import java.util.concurrent.TimeUnit;

public class FightKiln extends Controller {

    public static final WorldTile OUTSIDE = new WorldTile(4744, 5172, 0);
    private static final int TOKHAAR_HOK = 15195, TOKHAAR_HOK_SCENE = 15200;
    private static final int[] MUSICS = { 1088, 1082, 1086 };
    /*
     * 0 - south east 1 - south west 2 - north west 3 - north east TokHaar-Hur -
     * 15201 TokHaar-Xil - 15202 TokHaar-Mej - 15203 TokHaar-Ket - 15204
     * TokHaar-Tok-Xil - 15205 TokHaar-Yt-Mejkot - 15206 TokHaar-Ket-Zek - 15207
     * TokHaar-Jad - 15208 TokHaar-Ket-Dill - 15213
     */
    private final int[][] WAVES = { { 15202, 15202, 15205, 15201, 15201 }, // 1
            { 15202, 15202, 15205, 15205, 15201 }, // 2
            { 15202, 15205, 15205, 15205, 15201 }, // 3
            { 15205, 15205, 15205, 15203, 15203 }, // 4
            { 15202, 15205, 15205, 15205, 15213 }, // 5
            { 15202, 15205, 15203, 15203, 15205, 15205 }, // 6
            { 15203, 15205, 15205, 15205, 15202, 15205 }, // 7
            { 15207, 15205, 15205 }, // 8
            { 15205, 15205, 15205, 15205, 15205, 15205 }, // 9
            { 15205, 15208 }, // 10
            { 15203, 15203, 15203, 15203 }, // 11
            { 15203, 15205, 15205, 15203 }, // 12
            { 15203, 15207, 15203 }, // 13
            { 15207, 15207, 15203, 15203 }, // 14
            { 15207, 15207, 15205 }, // 15
            { 15207, 15207, 15205, 15203, 15203 }, // 16
            { 15207, 15207, 15205, 15203, 15206 }, // 17
            { 15207, 15207, 15205, 15206, 15205, 15205 }, // 18
            { 15203, 15203, 15203, 15203, 15203, 15203, 15203, 15203, 15203 }, // 19
            { 15207, 15208 }, // 20
            { 15201, 15201, 15201, 15201, 15201, 15201, 15201, 15201, 15201, 15201, 15201, 15201 }, // 21
            { 15206, 15201, 15204, 15204, 15201 }, // 22
            { 15206, 15206, 15204, 15206, 15201 }, // 23
            { 15206, 15206, 15206, 15205, 15206 }, // 24
            { 15206, 15206, 15205, 15205, 15207 }, // 25
            { 15206, 15206, 15205, 15207, 15207 }, // 26
            { 15204, 15206, 15205, 15204, 15206 }, // 27
            { 15213, 15213, 15207, 15213, 15213, 15213, 15213 }, // 28
            { 15206, 15206, 15206, 15206, 15206, 15206 }, // 29
            { 15206, 15208, 15206, 15206 }, // 30
            { 15205, 15205, 15205, 15205 }, // 31
            { 15206, 15206, 15206, 15206 }, // 32
            { 15207, 15207, 15207, 15207 }, // 33
            { 15205, 15208, 15206 }, // 34
            { 15207, 15206, 15206, 15208 }, // 35
            { 15208, 15208 } // 36
    };
    public int selectedMusic;
    private int[] boundChuncks;
    private Stages stage;
    private boolean logoutAtEnd;
    private boolean login;
    private NPC tokHaarHok;
    private HarAken harAken;
    private int aliveNPCSCount;

    private static void boostCrystal(final Player player, final int skill) {
        player.getTemporaryAttributtes().put("FightKilnCrystal", Boolean.TRUE);
        if (skill == Skills.RANGE) {
            player.getPackets().sendGameMessage("<col=7E2217>The power of the crystal improves your Ranged prowess, at the expense of your Defence, Strength and Magical ability.");
        } else if (skill == Skills.MAGIC) {
            player.getPackets().sendGameMessage("<col=7E2217>The power of the crystal improves your Magic prowess, at the expense of your Defence, Strength and Ranged ability.");
        } else if (skill == Skills.STRENGTH) {
            player.getPackets().sendGameMessage("<col=7E2217>The power of the crystal improves your Strength prowess, at the expense of your Defence, Ranged and Magical ability.");
        }
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            private int count;

            @Override
            public boolean repeat() {
                try {
                    if (count++ == 7 || !(player.getControlerManager().getControler() instanceof FightKiln)) {
                        player.getTemporaryAttributtes().remove("FightKilnCrystal");
                        player.getPackets().sendGameMessage("<col=7E2217>The power of the crystal dwindles and your " + Skills.SKILL_NAME[skill] + " prowess returns to normal.");
                        player.getSkills().set(Skills.DEFENCE, player.getSkills().getLevelForXp(Skills.DEFENCE));
                        player.getSkills().set(Skills.STRENGTH, player.getSkills().getLevelForXp(Skills.STRENGTH));
                        player.getSkills().set(Skills.RANGE, player.getSkills().getLevelForXp(Skills.RANGE));
                        player.getSkills().set(Skills.MAGIC, player.getSkills().getLevelForXp(Skills.MAGIC));
                        return false;
                    } else {
                        for (int i = 1; i < 7; i++) {
                            if (i == skill || i == 3 || i == 5) {
                                continue;
                            }
                            player.getSkills().set(i, player.getSkills().getLevelForXp(i) / 2);
                        }
                        player.getSkills().set(skill, (int) (player.getSkills().getLevelForXp(skill) * 1.5));
                    }
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
                return true;
            }

        }, 0, 30, TimeUnit.SECONDS);
    }

    public static void enterFightKiln(final Player player, final boolean quickEnter) {
        if (player.getInventory().containsOneItem(23653, 23654, 23655, 23656, 23657, 23658)) {
            return;
        }
        if (!player.isOwner())
            if (!player.isCompletedFightCaves()) {
                player.getDialogueManager().startDialogue("SimplePlayerMessage", "You need to have completed Fight Caves before you can enter the Kiln Cave.");
                return;
            }
        final Familiar familiar = player.getFamiliar();
        if (familiar != null && ((familiar.getBob() != null && familiar.getBob().containsOneItem(23653, 23654, 23655, 23656, 23657, 23658)) || familiar.isFinished())) {
            return;
        }
        if (!quickEnter) {
            player.getDialogueManager().startDialogue("FightKilnDialogue");
        } else {
            player.getControlerManager().startControler("FightKilnControler", 1);
        }
        player.getActivityTimersManager().startTimer(ActivityTimers.FIGHT_KILN);
    }

    public static void useCrystal(final Player player, final int id) {
        if (!(player.getControlerManager().getControler() instanceof FightKiln) || player.getTemporaryAttributtes().get("FightKilnCrystal") != null) {
            return;
        }
        player.getInventory().deleteItem(new Item(id, 1));
        switch (id) {
        case 23653: // invulnerability
            player.getPackets().sendGameMessage("<col=7E2217>>The power of this crystal makes you invulnerable.");
            player.getTemporaryAttributtes().put("FightKilnCrystal", Boolean.TRUE);
            player.setInvulnerable(true);
            CoresManager.getServiceProvider().executeWithDelay(() -> {
                player.getTemporaryAttributtes().remove("FightKilnCrystal");
                player.getPackets().sendGameMessage("<col=7E2217>The power of the crystal dwindles and you're vulnerable once more.");
                player.setInvulnerable(false);
            }, 15, TimeUnit.SECONDS);
            break;
        case 23654: // RESTORATION
            player.heal(player.getMaxHitpoints());
            player.getPrayer().restorePrayer(player.getSkills().getLevelForXp(Skills.PRAYER) * 10);
            player.getPackets().sendGameMessage("<col=7E2217>The power of this crystal heals you fully.");
            break;
        case 23655: // MAGIC
            boostCrystal(player, Skills.MAGIC);
            break;
        case 23656: // RANGED
            boostCrystal(player, Skills.RANGE);
            break;
        case 23657: // STRENGTH
            boostCrystal(player, Skills.STRENGTH);
            break;
        case 23658: // CONSTITUTION
            player.getTemporaryAttributtes().put("FightKilnCrystal", Boolean.TRUE);
            player.setHpBoostMultiplier(0.5);
            player.getEquipment().refreshConfigs(false);
            player.heal(player.getSkills().getLevelForXp(Skills.HITPOINTS) * 5);
            player.getPackets().sendGameMessage("<col=7E2217>The power of this crystal improves your Constitution.");
            CoresManager.getServiceProvider().executeWithDelay(() -> {
                try {
                    player.getTemporaryAttributtes().remove("FightKilnCrystal");
                    player.getPackets().sendGameMessage("<col=7E2217>The power of the crystal dwindles and your constitution prowess returns to normal.");
                    player.setHpBoostMultiplier(0);
                    player.getEquipment().refreshConfigs(false);
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }, 210, TimeUnit.SECONDS);
            break;
        }
    }

    public void buildMap() {
        final int[] map = getMap();
        MapBuilder.copyAllPlanesMap(map[0], map[1], boundChuncks[0], boundChuncks[1], 8);
    }

    public void checkCrystal() {
        if (stage != Stages.RUNNING) {
            return;
        }
        if (aliveNPCSCount == 1) {
            final int[] crystals = getLavaCrystal();
            if (crystals != null) {
                for (final int crystal : crystals) {
                    World.addGroundItem(new Item(crystal), getWorldTile(32, 32));
                }
            }
        }
    }
    public static final int KILN_PET_DR = 50;

    /*
     * logout or not. if didnt logout means lost, 0 logout, 1, normal, 2 tele
     */
    public void exitCave(final int type) {
        stage = Stages.DESTROYING;
        final WorldTile outside = new WorldTile(OUTSIDE, 2); // radomizes alil
        player.getActivityTimersManager().finishTimer(ActivityTimers.FIGHT_KILN, (type == 1), (type == 0));
        if (type == 0) {
            player.setLocation(outside);
            if (getCurrentWave() == 0) {
                removeControler();
            }
        } else if (type == 2) {
            player.setLocation(outside);
            removeControler();
        } else {
            player.setForceMultiArea(false);
            player.getInterfaceManager().removeMinigameHudInterface();
            if (type == 1 || type == 4) {
                player.setNextWorldTile(outside);
                if (type == 4) {
                    World.sendWorldMessage("<img=7><col=CC0000>Server: " + player.getDisplayName() + " " + "has just completed the Fight Kiln minigame.", false);

                    QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/fight_kiln.png\" height=17> " + player.getDisplayName() + " has just completed the Fight Kiln minigame."));
                    final Item reward = (Item) player.getTemporaryAttributtes().get("FightKilnReward");
                    if (((reward.getDefinitions().isNoted() || reward.getDefinitions().isStackable()) && !player.getInventory().containsItem(reward.getId(), 1) && player.getInventory().getFreeSlots() == 0) || (!(reward.getDefinitions().isNoted() || reward.getDefinitions().isStackable()) && player.getInventory().getFreeSlots() == 0)) {
                        player.getBank().addItem(reward, true);
                        player.getPackets().sendGameMessage("Your"+ (reward.getAmount() > 1 ? reward.getAmount() + " x" : "") + " "+ reward.getName()+" has been added to your bank.");
                    } else
                        player.getInventory().addItemDrop(reward.getId(), reward.getAmount());
                    player.getDropCollectionHandler().handleMinigames(DropCollectionConstants.MINIGAME_DATA.FIGHT_KILN, reward.getId());
                    player.setCompletedFightKiln();
                    player.getAchievements().updateProgress(1, AchievementList.COMPLETE_25_FIGHT_KILNS);
                    player.reset();
                    ChristmasSeasonalEvent.awardSmallPresent(player);
                    if (Utils.random(KILN_PET_DR) == 0) {
                        player.addItem(new Item(33814));
                        player.getDropCollectionHandler().handleMinigames(DropCollectionConstants.MINIGAME_DATA.FIGHT_KILN, 33814);
                        World.sendWorldMessage("<img=6><col=ff9900>News: " + player.getDisplayName() + " has received Shrimpy from Fight Kiln!", false);
                        HcimNewsManager.getInstance().addNews(player, "<#player> got the Shrimpy pet from Fight Kiln!");
                    }
                }
            }
            player.getInventory().removeItems(new Item(23653, Integer.MAX_VALUE), new Item(23654, Integer.MAX_VALUE), new Item(23655, Integer.MAX_VALUE), new Item(23656, Integer.MAX_VALUE), new Item(23657, Integer.MAX_VALUE), new Item(23658, Integer.MAX_VALUE));
            removeCrystalEffects();
            removeControler();
        }
        /*
         * 1200 delay because of leaving
         */
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                MapBuilder.destroyMap(boundChuncks[0], boundChuncks[1], 8, 8);
            }
        }, 1);
    }

    @Override
    public void forceClose() {
        if (stage != Stages.RUNNING) {
            return;
        }
        exitCave(2);
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

    private int[] getLavaCrystal() {
        switch (getCurrentWave()) {
        case 1:
        case 13:
        case 25:
            return new int[] { 23653 };
        case 3:
        case 15:
        case 27:
            return new int[] { 23654 };
        case 5:
        case 18:
        case 29:
            return new int[] { 23655 };
        case 7:
        case 19:
        case 31:
            return new int[] { 23656 };
        case 9:
        case 21:
            return new int[] { 23657 };
        case 11:
        case 23:
            return new int[] { 23658 };
        case 35:
            return new int[] { 23657, 23658 };
        default:
            return null;
        }
    }

    public int[] getMap() {
        final int wave = getCurrentWave();
        if (wave < 11) {
            return new int[] { 504, 632 };
        }
        if (wave < 21) {
            return new int[] { 512, 632 };
        }
        if (wave < 31) {
            return new int[] { 520, 632 };
        }
        if (wave < 34) {
            return new int[] { 528, 632 };
        }
        return new int[] { 536, 632 };
    }

    public WorldTile getMaxTile() {
        if (getCurrentWave() < 11) {
            return getWorldTile(49, 49);
        }
        if (getCurrentWave() < 21) {
            return getWorldTile(47, 47);
        }
        if (getCurrentWave() < 31) {
            return getWorldTile(45, 45);
        }
        if (getCurrentWave() < 34) {
            return getWorldTile(43, 43);
        }
        return getWorldTile(41, 41);
    }

    public WorldTile getMinTile() {
        if (getCurrentWave() < 11) {
            return getWorldTile(14, 14);
        }
        if (getCurrentWave() < 21) {
            return getWorldTile(16, 16);
        }
        if (getCurrentWave() < 31) {
            return getWorldTile(18, 18);
        }
        if (getCurrentWave() < 34) {
            return getWorldTile(20, 20);
        }
        return getWorldTile(22, 22);
    }

    public WorldTile getSpawnTile(final int count, final int size) {
        final int position = count % 4;
        switch (position) {
        case 0: // east south
            WorldTile maxTile = getMaxTile();
            WorldTile minTile = getMinTile();
            return new WorldTile(maxTile.getX() - 1 - size, minTile.getY() + 2, 1);
        case 1: // west south
            return getMinTile().transform(2, 2, 0);
        case 2: // west north
            maxTile = getMaxTile();
            minTile = getMinTile();
            return new WorldTile(minTile.getX() + 2, maxTile.getY() - 1 - size, 1);
        case 3: // east north
        default:
            return getMaxTile().transform(-1 - size, -1 - size, 0);
        }
    }

    public WorldTile getTentacleTile() {
        final int corner = Utils.random(4);
        int position = Utils.random(5);
        while (corner != 0 && position == 2) {
            position = Utils.random(5);
        }
        switch (corner) {
        case 0: // north
            return getWorldTile(21 + (position * 5), 42);
        case 1: // south
            return getWorldTile(21 + (position * 5), 20);
        case 2: // east
            return getWorldTile(42, 21 + (position * 5));
        case 3: // west
        default:
            return getWorldTile(20, 21 + (position * 5));
        }
    }

    public WorldTile getWorldTile(final int mapX, final int mapY) {
        return new WorldTile(boundChuncks[0] * 8 + mapX, boundChuncks[1] * 8 + mapY, 1);
    }

    public void hideHarAken() {
        if (stage != Stages.RUNNING) {
            return;
        }
        harAken.resetTimer();
        harAken.setCantInteract(true);
        harAken.setNextAnimation(new Animation(16234));
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                try {
                    if (stage != Stages.RUNNING) {
                        return;
                    }
                    harAken.finish();
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }

        }, 4);
    }

    public void loadCave(final boolean login) {
        final FightKiln kiln = this;
        stage = Stages.LOADING;
        player.lock(); // locks player
        final Runnable event = () -> CoresManager.getServiceProvider().executeNow(() -> {
            final int currentWave = getCurrentWave();
            // finds empty map bounds
            if (boundChuncks == null) {
                boundChuncks = MapBuilder.findEmptyChunkBound(8, 8);
                buildMap();
            } else if (!login && (currentWave == 11 || currentWave == 21 || currentWave == 31 || currentWave == 34)) {
                buildMap();
                player.setForceNextMapLoadRefresh(true);
                player.loadMapRegions();
            }
            // selects a music
            selectedMusic = MUSICS[Utils.random(MUSICS.length)];
            playMusic();
            player.setForceMultiArea(true);
            player.stopAll();
            if (currentWave == 0) { // SCENE 0
                player.getInventory().removeItems(new Item(23653, Integer.MAX_VALUE), new Item(23654, Integer.MAX_VALUE), new Item(23655, Integer.MAX_VALUE), new Item(23656, Integer.MAX_VALUE), new Item(23657, Integer.MAX_VALUE), new Item(23658, Integer.MAX_VALUE));
                player.setNextWorldTile(getWorldTile(31, 51));
                tokHaarHok = new NPC(TOKHAAR_HOK, getWorldTile(30, 36), -1, true, true);
                tokHaarHok.setDirection(Utils.getFaceDirection(0, 1));
                // 1delay because player cant walk while teleing :p,
                // + possible issues avoid
                WorldTasksManager.schedule(new WorldTask() {
                    int count = 0;
                    boolean run;

                    @Override
                    public void run() {
                        if (count == 0) {
                            final WorldTile lookTo = getWorldTile(29, 39);
                            player.getPackets().sendCameraLook(Cutscene.getX(player, lookTo.getX()), Cutscene.getY(player, lookTo.getY()), 3500);
                            final WorldTile posTile = getWorldTile(27, 30);
                            player.getPackets().sendCameraPos(Cutscene.getX(player, posTile.getX()), Cutscene.getY(player, posTile.getY()), 3500);
                            run = player.getRun();
                            player.setRun(false);
                            final WorldTile walkTo = getWorldTile(31, 39);
                            player.addWalkSteps(walkTo.getX(), walkTo.getY(), -1, false);
                            player.closeInterfaceLocked = true;
                        } else if (count == 1) {
                            player.getPackets().sendResetCamera();
                        } else if (count == 2) {
                            player.getDialogueManager().startDialogue("TokHaarHok", 0, TOKHAAR_HOK, kiln);
                            player.setRun(run);
                            stage = Stages.RUNNING;
                            player.unlock(); // unlocks player
                            stop();
                        }
                        count++;
                    }

                }, 1, 6);
            } else if (currentWave == 38) { // SCENE 7, WIN
                player.setNextWorldTile(getWorldTile(38, 25));
                player.setNextFaceWorldTile(getWorldTile(38, 26));
                tokHaarHok = new NPC(TOKHAAR_HOK_SCENE, getWorldTile(37, 30), -1, true, true);
                tokHaarHok.setDirection(Utils.getFaceDirection(0, -1));
                player.getPackets().sendMiniMapStatus(2);
                player.getPackets().sendConfigByFile(3028, 1);
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        final WorldTile lookTo = getWorldTile(40, 28);
                        player.getPackets().sendCameraLook(Cutscene.getX(player, lookTo.getX()), Cutscene.getY(player, lookTo.getY()), 2200);
                        final WorldTile posTile = getWorldTile(29, 28);
                        player.getPackets().sendCameraPos(Cutscene.getX(player, posTile.getX()), Cutscene.getY(player, posTile.getY()), 2500);
                        final HarAken harAken = new HarAken(15211, getWorldTile(45, 26), kiln);
                        harAken.spawn();
                        harAken.sendDeath(player);
                        player.closeInterfaceLocked = true;
                        WorldTasksManager.schedule(new WorldTask() {
                            @Override
                            public void run() {
                                player.getDialogueManager().startDialogue("TokHaarHok", 6, TOKHAAR_HOK_SCENE, kiln);
                            }
                        }, 8);
                    }
                }, 1);
            } else if (currentWave == 37) { // SCENE 6
                player.setNextWorldTile(getWorldTile(38, 25));
                player.setNextFaceWorldTile(getWorldTile(38, 26));
                tokHaarHok = new NPC(TOKHAAR_HOK_SCENE, getWorldTile(37, 30), -1, true, true);
                tokHaarHok.setDirection(Utils.getFaceDirection(0, -1));
                player.getPackets().sendMiniMapStatus(2);
                player.getPackets().sendConfigByFile(3028, 1);
                player.closeInterfaceLocked = true;
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        final WorldTile lookTo = getWorldTile(40, 28);
                        player.getPackets().sendCameraLook(Cutscene.getX(player, lookTo.getX()), Cutscene.getY(player, lookTo.getY()), 2200);
                        final WorldTile posTile = getWorldTile(29, 28);
                        player.getPackets().sendCameraPos(Cutscene.getX(player, posTile.getX()), Cutscene.getY(player, posTile.getY()), 2500);
                        player.getDialogueManager().startDialogue("TokHaarHok", 5, TOKHAAR_HOK_SCENE, kiln);
                    }

                }, 1);
            } else if (currentWave == 34) { // SCENE 5
                teleportPlayerToMiddle();
                player.getPackets().sendMiniMapStatus(2);
                player.getPackets().sendConfigByFile(3028, 1);
                WorldTasksManager.schedule(new WorldTask() {

                    int count = 0;

                    @Override
                    public void run() {
                        if (count == 0) {
                            final WorldTile lookTo = getWorldTile(32, 41);
                            player.getPackets().sendCameraLook(Cutscene.getX(player, lookTo.getX()), Cutscene.getY(player, lookTo.getY()), 1000);
                            final WorldTile posTile = getWorldTile(32, 38);
                            player.getPackets().sendCameraPos(Cutscene.getX(player, posTile.getX()), Cutscene.getY(player, posTile.getY()), 1200);
                        } else if (count == 6) {
                            final WorldTile lookTo = getWorldTile(64, 30);
                            player.getPackets().sendCameraLook(Cutscene.getX(player, lookTo.getX()), Cutscene.getY(player, lookTo.getY()), 3000);
                            final WorldTile posTile = getWorldTile(42, 36);
                            player.getPackets().sendCameraPos(Cutscene.getX(player, posTile.getX()), Cutscene.getY(player, posTile.getY()), 3000);
                            player.setNextWorldTile(getWorldTile(33, 39));
                            player.setNextFaceWorldTile(getWorldTile(32, 39));
                            player.closeInterfaceLocked = true;
                        } else if (count == 12) {
                            stop();
                            tokHaarHok = new NPC(TOKHAAR_HOK_SCENE, getWorldTile(28, 38), -1, true, true);
                            tokHaarHok.setDirection(Utils.getFaceDirection(1, 0));

                            final WorldTile lookTo = getWorldTile(30, 38);
                            player.getPackets().sendCameraLook(Cutscene.getX(player, lookTo.getX()), Cutscene.getY(player, lookTo.getY()), 2500);
                            final WorldTile posTile = getWorldTile(30, 30);
                            player.getPackets().sendCameraPos(Cutscene.getX(player, posTile.getX()), Cutscene.getY(player, posTile.getY()), 3000);
                            player.getDialogueManager().startDialogue("TokHaarHok", 7, TOKHAAR_HOK_SCENE, kiln);
                        }
                        count++;

                    }

                }, 1, 0);
            } else if (currentWave == 31) { // SCENE 4
                player.setNextWorldTile(getWorldTile(21, 21));
                player.setNextFaceWorldTile(getWorldTile(20, 20));
                player.getPackets().sendMiniMapStatus(2);
                player.getPackets().sendConfigByFile(3028, 1);
                player.closeInterfaceLocked = true;
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        final WorldTile lookTo = getWorldTile(20, 17);
                        player.getPackets().sendCameraLook(Cutscene.getX(player, lookTo.getX()), Cutscene.getY(player, lookTo.getY()), 2500);
                        final WorldTile posTile = getWorldTile(25, 26);
                        player.getPackets().sendCameraPos(Cutscene.getX(player, posTile.getX()), Cutscene.getY(player, posTile.getY()), 3000);
                        player.getDialogueManager().startDialogue("TokHaarHok", 4, TOKHAAR_HOK_SCENE, kiln);
                    }

                }, 1);
            } else if (currentWave == 21) { // SCENE 3
                tokHaarHok = new NPC(TOKHAAR_HOK_SCENE, getWorldTile(30, 43), -1, true, true);
                tokHaarHok.setDirection(Utils.getFaceDirection(0, -1));
                teleportPlayerToMiddle();
                player.getPackets().sendMiniMapStatus(2);
                player.getPackets().sendConfigByFile(3028, 1);
                player.closeInterfaceLocked = true;
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        final WorldTile lookTo = getWorldTile(31, 43);
                        player.getPackets().sendCameraLook(Cutscene.getX(player, lookTo.getX()), Cutscene.getY(player, lookTo.getY()), 2500);
                        final WorldTile posTile = getWorldTile(31, 34);
                        player.getPackets().sendCameraPos(Cutscene.getX(player, posTile.getX()), Cutscene.getY(player, posTile.getY()), 4000);
                        player.getDialogueManager().startDialogue("TokHaarHok", 3, TOKHAAR_HOK_SCENE, kiln);
                    }
                }, 1);
            } else if (currentWave == 11) { // SCENE 2
                tokHaarHok = new NPC(TOKHAAR_HOK_SCENE, getWorldTile(45, 45), -1, true, true);
                tokHaarHok.setDirection(Utils.getFaceDirection(-1, -1));
                teleportPlayerToMiddle();
                player.getPackets().sendMiniMapStatus(2);
                player.getPackets().sendConfigByFile(3028, 1);
                player.closeInterfaceLocked = true;
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        final WorldTile lookTo = getWorldTile(45, 45);
                        player.getPackets().sendCameraLook(Cutscene.getX(player, lookTo.getX()), Cutscene.getY(player, lookTo.getY()), 1000);
                        final WorldTile posTile = getWorldTile(38, 37);
                        player.getPackets().sendCameraPos(Cutscene.getX(player, posTile.getX()), Cutscene.getY(player, posTile.getY()), 3000);
                        player.getDialogueManager().startDialogue("TokHaarHok", 2, TOKHAAR_HOK_SCENE, kiln);
                    }

                }, 1);
            } else if (currentWave == 1) { // SCENE 1
                player.getInventory().removeItems(new Item(23653, Integer.MAX_VALUE), new Item(23654, Integer.MAX_VALUE), new Item(23655, Integer.MAX_VALUE), new Item(23656, Integer.MAX_VALUE), new Item(23657, Integer.MAX_VALUE), new Item(23658, Integer.MAX_VALUE));
                tokHaarHok = new NPC(TOKHAAR_HOK_SCENE, getWorldTile(30, 36), -1, true, true);
                tokHaarHok.setDirection(Utils.getFaceDirection(0, 1));
                player.setNextWorldTile(getWorldTile(31, 39));
                player.getPackets().sendMiniMapStatus(2);
                player.getPackets().sendConfigByFile(3028, 1);
                player.setNextFaceWorldTile(tokHaarHok);
                player.closeInterfaceLocked = true;
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        final WorldTile lookTo = getWorldTile(31, 40);
                        player.getPackets().sendCameraLook(Cutscene.getX(player, lookTo.getX()), Cutscene.getY(player, lookTo.getY()), 1000);
                        final WorldTile posTile = getWorldTile(31, 50);
                        player.getPackets().sendCameraPos(Cutscene.getX(player, posTile.getX()), Cutscene.getY(player, posTile.getY()), 3000);
                        player.getDialogueManager().startDialogue("TokHaarHok", 1, TOKHAAR_HOK_SCENE, kiln);
                        stage = Stages.RUNNING;
                        player.unlock();
                    }

                }, 1);
            } else if (login) { // LOGIN during
                kiln.login = login;
                teleportPlayerToMiddle();
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        stage = Stages.RUNNING;
                        player.unlock();
                    }
                }, 1);
            }
        });
        if (!login) {
            FadingScreen.fade(player, event);
        } else {
            event.run();
        }
    }

    /*
     * return false so wont remove script
     */
    @Override
    public boolean login() {
        loadCave(true);
        return false;
    }

    @Override
    public boolean logout() {
        if (stage != Stages.RUNNING) {
            return false;
        }
        exitCave(0);
        return false;
    }

    @Override
    public void magicTeleported(final int type) {
        exitCave(1);
    }

    @Override
    public void moved() {
        if (stage != Stages.RUNNING || !login) {
            return;
        }
        login = false;
        setWaveEvent();
    }

    public void nextWave() {
        if (stage != Stages.RUNNING) {
            return;
        }
        playMusic();
        final int nextWave = getCurrentWave() + 1;
        setCurrentWave(nextWave);
        player.sendMessage("Currently on wave: " + Colors.RED + nextWave + "</col>.");
        if (logoutAtEnd) {
            player.logout(false);
            return;
        } else if (nextWave == 1 || nextWave == 11 || nextWave == 21 || nextWave == 31 || nextWave == 34 || nextWave == 37 || nextWave == 38) {
            harAken = null;
            player.stopAll();
            loadCave(false);
            return;
        }
        setWaveEvent();
    }

    public void playMusic() {
        player.getMusicsManager().playMusic(selectedMusic);
    }

    @Override
    public void process() {
        if (harAken != null) {
            harAken.process();
        }
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

    @Override
    public boolean processItemTeleport(final WorldTile toTile) {
        return false;
    }

    @Override
    public boolean processMagicTeleport(final WorldTile toTile) {
        return false;
    }

    @Override
    public boolean processNPCClick1(final NPC npc) {
        if (npc.getId() == 15195 && getCurrentWave() == 0) {
            player.getDialogueManager().startDialogue("TokHaarHok", 0, TOKHAAR_HOK, this);
            return false;
        }
        return true;
    }

    /**
     * return process normaly
     */
    @Override
    public boolean processObjectClick1(final WorldObject object) {
        if (object.getId() == 68111) {
            if (stage != Stages.RUNNING) {
                return false;
            }
            exitCave(1);
            return false;
        }
        return true;
    }

    @Override
    public boolean processObjectTeleport(final WorldTile toTile) {
        return false;
    }

    private void removeCrystalEffects() {
        player.setInvulnerable(false);
        player.getSkills().restoreSkills();
        player.setHpBoostMultiplier(0);
        player.getEquipment().refreshConfigs(false);
        player.getTemporaryAttributtes().remove("FightKilnCrystal");
    }

    public void removeNPC() {
        if (stage != Stages.RUNNING) {
            return;
        }
        aliveNPCSCount--;
        if (aliveNPCSCount == 0) {
            nextWave();
        }
    }

    public void removeScene() {
        FadingScreen.fade(player, () -> {
            if (stage != Stages.RUNNING) {
                unlockPlayer();
            }
            removeTokHaarTok();
            player.getPackets().sendResetCamera();
            player.getPackets().sendMiniMapStatus(0);
            player.getPackets().sendConfigByFile(3028, 0);
            if (getCurrentWave() == 38) {
                final Item reward = (Item) player.getTemporaryAttributtes().get("FightKilnReward");
                if (reward != null) {
                    win();
                }
            } else {
                teleportPlayerToMiddle();
                setWaveEvent();
            }
        });
    }

    public void removeTokHaarTok() {
        if (tokHaarHok != null) {
            tokHaarHok.finish();
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
                    stop();
                }
                loop++;
            }
        }, 0, 1);
        return false;
    }

    public void setWaveEvent() {
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
        }, 10);
    }

    public void showHarAken() {
        if (harAken == null) {
            harAken = new HarAken(15211, getWorldTile(45, 26), this);
            harAken.setTarget(player);
            harAken.setDirection(Utils.getFaceDirection(-1, -1));
        } else {
            if (stage != Stages.RUNNING) {
                return;
            }
            switch (Utils.random(3)) {
            case 0:
                harAken.setLocation(getWorldTile(29, 17));
                harAken.setDirection(Utils.getFaceDirection(0, 1));
                break;
            case 1:
                harAken.setLocation(getWorldTile(17, 30));
                harAken.setDirection(Utils.getFaceDirection(1, 0));
                break;
            case 2:
                harAken.setLocation(getWorldTile(42, 30));
                harAken.setDirection(Utils.getFaceDirection(-1, 0));
                break;
            }
            harAken.spawn();
        }
        harAken.setCantInteract(false);
        harAken.setNextAnimation(new Animation(16232));
    }

    public void spawn(final int index) {
        final int currentWave = getCurrentWave();
        for (int i = index; i < (index + 4 > WAVES[currentWave - 1].length ? WAVES[currentWave - 1].length : index + 4); i++) {
            final int npcId = WAVES[currentWave - 1][i];
            NPC npc;
            if (npcId == 15213) {
                npc = new TokHaarKetDill(WAVES[currentWave - 1][i], getSpawnTile(i, NPCDefinitions.getNPCDefinitions(npcId).size), this);
            } else {
                npc = new FightKilnNPC(WAVES[currentWave - 1][i], getSpawnTile(i, NPCDefinitions.getNPCDefinitions(npcId).size), this);
            }
            npc.setTarget(player);
        }
    }

    @Override
    public void start() {
        loadCave(false);
    }

    public void startWave() {
        if (stage != Stages.RUNNING) {
            return;
        }
        final int currentWave = getCurrentWave();
        player.getInterfaceManager().sendMinigameHudInterface(316);
        player.getPackets().sendConfigByFile(15151, currentWave);
        if (currentWave > WAVES.length) {
            if (currentWave == 37) {
                aliveNPCSCount = 1;
            }
            return;
        } else if (currentWave == 0) {
            exitCave(1);
            return;
        }
        aliveNPCSCount = WAVES[currentWave - 1].length;
        for (int i = 0; i < WAVES[currentWave - 1].length; i += 4) {
            final int next = i;
            CoresManager.getServiceProvider().executeWithDelay(() -> {
                try {
                    if (stage != Stages.RUNNING) {
                        return;
                    }
                    spawn(next);
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }, (next / 4) * 4000, TimeUnit.MILLISECONDS);
        }
    }

    public void teleportPlayerToMiddle() {
        player.setNextWorldTile(getWorldTile(31, 32));
    }

    public void unlockPlayer() {
        stage = Stages.RUNNING;
        player.unlock(); // unlocks player
    }

    public void win() {
        if (stage != Stages.RUNNING) {
            return;
        }
        exitCave(4);
    }

    private enum Stages {
        LOADING, RUNNING, DESTROYING
    }
}
