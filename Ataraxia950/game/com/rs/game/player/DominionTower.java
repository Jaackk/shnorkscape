package com.rs.game.player;

import java.io.Serializable;

import com.rs.Settings;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.MapBuilder;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.cutscenes.Cutscene;
import com.rs.game.player.dialogue.impl.DominionFactorRewardD;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.DTRank;
import com.rs.utils.DialogueOptionEvent;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import lombok.Getter;
import lombok.Setter;


public final class DominionTower implements Serializable {

    public static final int ENDURANCE = 1, FREE = 1, MAX_FACTOR = 20000000;
    private static final long serialVersionUID = -5230255619877910203L;
    @SuppressWarnings("unused")
    private static final int[] NORMAL_ARENA = { 456, 768 }, LARGE_PILAR_ARENA = { 456, 776 }, SMALL_PILAR_ARENA = { 456, 784 }, PODIUM_ARENA = { 456, 760 };
    private static final int[] MUSICS = { 1015, 1022, 1018, 1016, 1021 };
    private static final Boss[] BOSSES = { new Boss("Elvarg", "Grrrr", 14548), new Boss("Evil Chicken", "Bwak bwak bwak", 3375), new Boss("Bouncer", "Roaarr", 14483), new Boss("Black knight titan", "You will suffer my wrath!", -1, false, null, NORMAL_ARENA, 14436), new Boss("General Graardor", "CHAAAARGEE", 3220, true, null, NORMAL_ARENA, 6260), new Boss("K'ril Tsutsaroth", "ATTAACK!", 3282, true, null, NORMAL_ARENA, 6203), new Boss("Commander Zilyana", "Saradomin lend me strength!", 3263, true, null, NORMAL_ARENA, 6247), new Boss("Kree'Arra", "Storms align to me!", -1, true, null, NORMAL_ARENA, 6222), new Boss("Jad", null, 2745), new Boss("Barrelchest", null, 5666), new Boss("King Black Dragon", "Grrrr", 50), new Boss("Chaos elemental", "Fus-Ro-Dah!", 3200) };
    public int dominionFactor;
    public int killedBossesCount;
    private transient Player player;
    private transient int[] mapBaseCoords;
    private int nextBossIndex;
    private int progress;
    private long totalScore;
    private boolean talkedWithFace;
    @Getter
    @Setter
    private boolean skipCutscenes;
    private int maxFloorEndurance;

    private transient int rewardPage;

    public DominionTower() {
        nextBossIndex = -1;
    }

    public void setPlayer(final Player player) {
        this.player = player;
    }

    public boolean hasRequiriments() {
        return player.getSkills().getCombatLevelWithSummoning() >= 110;
    }

    public void growFace() {
        player.getPackets().sendSound(7913, 0, 2);
        player.getDialogueManager().startDialogue("SimpleMessage", "The face on the wall groans and cowls at you. Perhaps you should", " talk to it first.");
    }

    public void openModes() {
        if (!hasRequiriments()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You do not meet the requirements to play this minigame!");
            return;
        }
        if (!talkedWithFace) {
            growFace();
            return;
        }
        if (progress == 256) {
            player.sendMessage("You can't go back into the arena, you must go to the next floor on entrance.");
            return;
        }
        player.getInterfaceManager().sendInterface(1164);
        player.getInterfaceManager().refreshInterface(true);
        player.getPackets().sendIComponentText(1164, 1, progress == 0 ? "Ready for a new match" : "Floor progress: " + progress);
    }

    public void handleButtons(final int interfaceId, final int componentId) {
        if (interfaceId == 1163) {
            if (componentId == 89) {
                player.closeInterfaces();
            }

        } else if (interfaceId == 1160) {
            if (componentId == 55) {
                openRewards();
            }
        } else if (interfaceId == 1168) {
            if (componentId == 255) {
                player.closeInterfaces();
            }
        } else if (interfaceId == 1156) {
            if (rewardPage == 0) {
                if (componentId == 88) {
                    if (player.getDominionTower().getKilledBossesCount() < 100) {
                        player.sendMessage("You need to have at least 100 DT Kills to obtain this.");
                        return;
                    }
                    if (player.hasItem(new Item(22358)) || player.hasItem(new Item(22359)) || player.hasItem(new Item(22360)) || player.hasItem(new Item(22361))) {
                        player.sendMessage("You already have this reward.");
                        player.getPackets().sendVoice(7894);
                        return;
                    }
                    player.getDialogueManager().startDialogue("Goliaths");
                    return;
                } else if (componentId == 115) {
                    if (player.getDominionTower().getKilledBossesCount() < 100) {
                        player.sendMessage("You need to have at least 100 DT Kills to obtain this.");
                        return;
                    }
                    if (player.hasItem(new Item(22366)) || player.hasItem(new Item(22367)) || player.hasItem(new Item(22368)) || player.hasItem(new Item(22369))) {
                        player.sendMessage("You already have this reward.");
                        player.getPackets().sendVoice(7894);
                        return;
                    }
                    player.getDialogueManager().startDialogue("SpellCasters");
                    return;
                } else if (componentId == 139) {
                    if (player.getDominionTower().getKilledBossesCount() < 100) {
                        player.sendMessage("You need to have at least 100 DT Kills to obtain this.");
                        return;
                    }
                    if (player.hasItem(new Item(22362)) || player.hasItem(new Item(22363)) || player.hasItem(new Item(22364)) || player.hasItem(new Item(22365))) {
                        player.sendMessage("You already have this reward.");
                        player.getPackets().sendVoice(7894);
                        return;
                    }
                    player.getDialogueManager().startDialogue("Swifts");
                    return;
                } else if (componentId == 112) {
                    if (player.getDominionTower().getKilledBossesCount() < 150) {
                        player.sendMessage("You need to have at least 150 DT Kills to obtain this.");
                        return;
                    }
                    if (player.getInventory().containsItem(22346, 1) || player.getEquipment().getWeaponId() == 22346 || player.getBank().getItem(22346) != null) {
                        player.sendMessage("You already have this reward.");
                        player.getPackets().sendVoice(7894);
                        return;
                    }
                    player.getInventory().addItem(22346, 1);

                    return;
                } else if (componentId == 118) {
                    if (player.getDominionTower().getKilledBossesCount() < 125) {
                        player.sendMessage("You need to have at least 125 DT Kills to obtain this.");
                        return;
                    }
                    if (player.getInventory().containsItem(22347, 1) || player.getEquipment().getWeaponId() == 22347 || player.getBank().getItem(22347) != null) {
                        player.sendMessage("You already have this reward.");
                        player.getPackets().sendVoice(7894);
                        return;
                    }
                    player.getInventory().addItem(22347, 1);

                    return;
                } else if (componentId == 136) {
                    if (player.getDominionTower().getKilledBossesCount() < 200) {
                        player.sendMessage("You need to have at least 200 DT Kills to obtain this.");
                        return;
                    }
                    if (player.getInventory().containsItem(22348, 1) || player.getEquipment().getWeaponId() == 22348 || player.getBank().getItem(22348) != null) {
                        player.sendMessage("You already have this reward.");
                        player.getPackets().sendVoice(7894);
                        return;
                    }
                    player.getInventory().addItem(22348, 1);
                    return;
                } else if (componentId == 105) {
                    rewardPage = 1;
                    openItemRewards();
                    return;
                } else if (componentId == 106) {
                    rewardPage = 1;
                    openItemRewards();
                    return;
                }
            } else if (rewardPage == 1) {
                if (componentId == 88) {
                    if (player.getDominionTower().getKilledBossesCount() < 200) {
                        player.sendMessage("You need to have at least 200 DT kills to obtain these.");
                        return;
                    }
                    if (dominionFactor < 50_000) {
                        player.sendMessage("You need to have at least 50,000 Dominion Tower factor to buy this.");
                        return;
                    }
                    dominionFactor -= 50_000;
                    if (dominionFactor < 0)
                        dominionFactor = 0;

                    player.addItem(new Item(22370, 75));
                    openItemRewards();
                    return;
                } else if (componentId == 115) {
                    if (dominionFactor < 20_000_000) {
                        player.sendMessage("You need to have at least 20,000,000 Dominion Tower factor to buy this.");
                        return;
                    }

                    if (player.hasItem(40738)) {
                        player.sendMessage("You already have this reward.");
                        player.getPackets().sendVoice(7894);
                        return;
                    }

                    dominionFactor -= 20_000_000;
                    if (dominionFactor < 0)
                        dominionFactor = 0;

                    player.addItem(new Item(40738, 1));
                    World.sendWorldMessage("<col=4286f4><img=6>News: " + player.getDisplayName() + " has just purchased the Fire lycan (adult) companion from the Dominion Tower rewards for 20,000,000 factor!", false);
                    return;
                } else if (componentId == 105) {
                    rewardPage = 0;
                    openItemRewards();
                    return;
                } else if (componentId == 106) {
                    rewardPage = 0;
                    openItemRewards();
                    return;
                }
            }
        } else if (interfaceId == 1170) {
            if (componentId == 85) {
                player.closeInterfaces();
            }
        } else if (interfaceId == 1173) {
            if (componentId == 58) {
                player.closeInterfaces();
            } else if (componentId == 59) {
                startEnduranceMode();
            }
        } else if (interfaceId == 1164) {
            if (componentId == 0) {
                player.closeInterfaces();
                openEnduranceMode();
            } else {
                player.sendMessage("Only Endurance Mode is currently working.");
            }
        }
    }

    public void startEnduranceMode() {
        if (progress == 256) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You have some dominion factor which you must exchange before ", "starting another match.");
            player.sendMessage("You can't go back into the arena, you must go to the next floor on entrance.");
            return;
        }
        createArena(ENDURANCE);
    }

    public void createArena(final int mode) {
        player.closeInterfaces();
        player.lock(15);
        CoresManager.getServiceProvider().executeNow(() -> {
            try {
                final boolean needDestroy = mapBaseCoords != null;
                final int[] oldMapBaseCoords = mapBaseCoords;
                mapBaseCoords = MapBuilder.findEmptyChunkBound(8, 8);
                MapBuilder.copyAllPlanesMap(BOSSES[getNextBossIndex()].arena[0], BOSSES[getNextBossIndex()].arena[1], mapBaseCoords[0], mapBaseCoords[1], 8);
                teleportToArena(mode);
                if (needDestroy) {
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            CoresManager.getServiceProvider().executeNow(() -> {
                                try {
                                    MapBuilder.destroyMap(oldMapBaseCoords[0], oldMapBaseCoords[1], 8, 8);
                                } catch (final Exception | Error e1) {
                                    Logger.getGlobal().catching(e1);
                                }
                            });
                        }
                    });
                }
            } catch (final Exception | Error e3) {
                Logger.getGlobal().catching(e3);
            }

        });
    }

    private void teleportToArena(final int mode) {
        player.setNextFaceWorldTile(new WorldTile(getBaseX() + 11, getBaseY() + 29, 0));
        player.getControlerManager().startControler("DTControler", mode);
        player.setNextWorldTile(new WorldTile(getBaseX() + 10, getBaseY() + 29, 2));
        player.getMusicsManager().playMusic(MUSICS[Utils.getRandom(MUSICS.length - 1)]);
        player.unlock();
    }

    public String getStartFightText(final int message) {
        switch (message) {
        case 0:
            return "Argh! Lets get this over with!";
        case 1:
            return "Prepare to die!";
        case 2:
            return "I'll crush you!";
        case 3:
            return "You don't stand a chance!";
        default:
            return "Bring it on, weakling!";
        }
    }

    public int getNextBossIndex() {
        if (nextBossIndex < 0 || nextBossIndex >= BOSSES.length) {
            selectBoss();
        }
        return nextBossIndex;
    }

    public void startFight(final NPC[] bosses) {
        for (final NPC boss : bosses) {
            boss.setCantInteract(true);
            boss.setNextFaceWorldTile(new WorldTile(boss.getX() - 1, boss.getY(), 0));
        }
        player.lock(15);
        player.setNextWorldTile(new WorldTile(getBaseX() + 25, getBaseY() + 32, 2));
        player.setNextFaceWorldTile(new WorldTile(getBaseX() + 26, getBaseY() + 32, 0));
        final int index = getNextBossIndex();
        WorldTasksManager.schedule(new WorldTask() {

            private int count;

            @Override
            public void run() {
                if (player.getPerkManager().hasPerkActive(DonationPerk.DOMINION_DOMINATION)) {
                    if (skipCutscenes) {
                        startBattle(bosses, true);
                        stop();
                        return;
                    } else if (count == 0) {
                        player.sendMessage(Colors.RED + "Since you have the \"Dominion Domination\" perk, you are " + "eligible to skip the cutscenes! Do ;;SkipDTCutscene to toggle it!");
                    }
                }
                if (count == 0) {
                    player.getInterfaceManager().sendOverlay(1172, false);
                    player.getPackets().sendHideIComponent(1172, 2, true);
                    player.getPackets().sendHideIComponent(1172, 7, true);
                    player.getPackets().sendIComponentText(1172, 4, player.getDisplayName());
                    player.getPackets().sendConfigByFile(3028, 1);
                    player.getPackets().sendCameraPos(Cutscene.getX(player, getBaseX() + 25), Cutscene.getY(player, getBaseY() + 38), 1800);
                    player.getPackets().sendCameraLook(Cutscene.getX(player, getBaseX() + 25), Cutscene.getY(player, getBaseY() + 29), 800);
                    player.getPackets().sendCameraPos(Cutscene.getX(player, getBaseX() + 32), Cutscene.getY(player, getBaseY() + 38), 1800, 6, 6);
                } else if (count == 1) {
                    player.setNextForceTalk(new ForceTalk(getStartFightText(Utils.getRandom(1))));
                } else if (count == 3) {
                    player.getPackets().sendHideIComponent(1172, 2, false);
                    player.getPackets().sendHideIComponent(1172, 5, true);
                    player.getPackets().sendIComponentText(1172, 6, BOSSES[index].name);
                    player.getPackets().sendConfigByFile(3028, 0);
                    player.getPackets().sendCameraPos(Cutscene.getX(player, getBaseX() + 35), Cutscene.getY(player, getBaseY() + 37), 1800);
                    player.getPackets().sendCameraLook(Cutscene.getX(player, getBaseX() + 35), Cutscene.getY(player, getBaseY() + 28), 800);
                    player.getPackets().sendCameraPos(Cutscene.getX(player, getBaseX() + 42), Cutscene.getY(player, getBaseY() + 37), 1800, 6, 6);
                } else if (count == 4) {
                    if (BOSSES[index].text != null) {
                        bosses[0].setNextForceTalk(new ForceTalk(BOSSES[index].text));
                    }
                    if (BOSSES[index].voice != -1) {
                        player.getPackets().sendVoice(BOSSES[index].voice);
                    }
                } else if (count == 6) {

                    player.getControlerManager().sendInterfaces();
                    player.getInterfaceManager().sendOverlay(1172, false);
                    player.getPackets().sendHideIComponent(1172, 2, true);
                    player.getPackets().sendHideIComponent(1172, 5, true);
                    player.getPackets().sendIComponentText(1172, 8, "Fight!");
                    player.getPackets().sendHideIComponent(1172, 10, true);
                    // player.getPackets().sendCameraLook(Cutscene.getX(player,
                    // getBaseX() + 32),
                    // Cutscene.getY(player, getBaseY() + 36), 0);
                    // player.getPackets().sendCameraPos(Cutscene.getX(player,
                    // getBaseX() + 32),
                    // Cutscene.getY(player, getBaseY() + 16), 5000);
                    player.getPackets().sendVoice(7882);
                } else if (count == 8) {
                    // if (nextBossIndex != -1 && BOSSES[index].item != null)
                    // World.addGroundItem(BOSSES[index].item, new
                    // WorldTile(getBaseX() + 26, getBaseY() + 33, 2));
                    
                    player.getInterfaceManager().closeOverlay(false);
                    startBattle(bosses, false);
                    player.getPackets().sendResetCamera();
                    for (final NPC boss : bosses) {
                        boss.setCantInteract(false);
                        boss.setTarget(player);
                    }
                    player.unlock();
                    stop();
                }
                count++;
            }

        }, 0, 1);
    }

    private void startBattle(final NPC[] bosses, boolean skipped) {
        if (skipped) {
            player.getControlerManager().sendInterfaces();
        }
        player.getPackets().sendResetCamera();
        for (final NPC boss : bosses) {
            boss.setCantInteract(false);
            boss.setTarget(player);
        }
        player.unlock();
    }

    public void removeItem() {
        if (nextBossIndex == -1) {
            return;
        }
        if (BOSSES[nextBossIndex].item != null) {
            player.getInventory().deleteItem(BOSSES[nextBossIndex].item.getId(), BOSSES[nextBossIndex].item.getAmount());
            player.getEquipment().deleteItem(BOSSES[nextBossIndex].item.getId(), BOSSES[nextBossIndex].item.getAmount());
            player.getAppearence().generateAppearenceData();
        }
    }

    public void loss(final int mode) {
        nextBossIndex = -1;
        player.lock(15);
        player.setNextWorldTile(new WorldTile(getBaseX() + 35, getBaseY() + 31, 2));
        player.setNextFaceWorldTile(new WorldTile(player.getX() + 1, player.getY(), 0));

        WorldTasksManager.schedule(new WorldTask() {
            int count;

            @Override
            public void run() {
                int nextCount = 4;
                if (player.getPerkManager().hasPerkActive(DonationPerk.DOMINION_DOMINATION) && skipCutscenes) {
                    nextCount = 1;
                }
                if (count == 0) {
                    player.setNextAnimation(new Animation(836));
                    player.getInterfaceManager().removeMinigameHudInterface();
                    player.getInterfaceManager().sendInterface(1172);
                    player.getPackets().sendHideIComponent(1172, 2, true);
                    player.getPackets().sendHideIComponent(1172, 5, true);
                    player.getPackets().sendIComponentText(1172, 8, "DEFEATED!");
                    player.getPackets().sendIComponentText(1172, 10, "You leave with a dominion factor of: " + dominionFactor);
                    if (player.getPerkManager().hasPerkActive(DonationPerk.DOMINION_DOMINATION) && skipCutscenes) {
                        player.getPackets().sendCameraPos(Cutscene.getX(player, getBaseX() + 35), Cutscene.getY(player, getBaseY() + 37), 2500);
                        player.getPackets().sendCameraLook(Cutscene.getX(player, getBaseX() + 35), Cutscene.getY(player, getBaseY() + 28), 800);
                        player.getPackets().sendCameraPos(Cutscene.getX(player, getBaseX() + 42), Cutscene.getY(player, getBaseY() + 37), 2500, 6, 6);
                    }
                    player.getPackets().sendVoice(7874);
                } else if (count == nextCount) {
                    player.setForceMultiArea(false);
                    player.reset(true, false);
                    player.setNextAnimation(new Animation(-1));
                    player.getInterfaceManager().closeOverlay(false);
                    player.getPackets().sendResetCamera();
                    player.unlock();
                    destroyArena(false, mode);
                    stop();
                }
                count++;
            }
        }, 0, 1);
    }

    public void win(final int mode) {
        removeItem();
        final int factor = getBossesTotalLevel() * 10;
        progress++;
        if (progress > maxFloorEndurance) {
            maxFloorEndurance = progress;
        }
        if (Settings.SUPERLOG) {
            Logger.getGlobal().info("[DOMTOWER]: " + player.getDisplayName() + " had " + killedBossesCount + " kills");
        }
        killedBossesCount += player.getPerkManager().hasPerkActive(DonationPerk.THE_MINI___GAMER) && player.getPerkManager().hasPerkActive(DonationPerk.DOMINION_DOMINATION) ? 3 : player.getPerkManager().hasPerkActive(DonationPerk.THE_MINI___GAMER) || player.getPerkManager().hasPerkActive(DonationPerk.DOMINION_DOMINATION) ? 2 : 1;
        if (Settings.SUPERLOG) {
            Logger.getGlobal().info("[DOMTOWER]: " + player.getDisplayName() + " now has " + killedBossesCount + " kills");
        }
        dominionFactor += player.getPerkManager().hasPerkActive(DonationPerk.DOMINION_DOMINATION) ? (factor * 2.5) : factor;
        totalScore += player.getPerkManager().hasPerkActive(DonationPerk.DOMINION_DOMINATION) ? (factor * 2.5) : factor;
        if (dominionFactor > MAX_FACTOR) {
            dominionFactor = MAX_FACTOR;
            player.sendMessage("You've reached the maximum Dominion Factor you can get!");
        }
        DTRank.checkRank(player, mode, BOSSES[getNextBossIndex()].name);
        nextBossIndex = -1;
        player.lock(15);
        player.setNextWorldTile(new WorldTile(getBaseX() + 35, getBaseY() + 31, 2));
        player.setNextFaceWorldTile(new WorldTile(getBaseX() + 36, getBaseY() + 31, 0));
        ChristmasSeasonalEvent.awardSmallPresent(player);
        WorldTasksManager.schedule(new WorldTask() {

            private int count;

            @Override
            public void run() {
                int nextCount = 4;
                if (player.getPerkManager().hasPerkActive(DonationPerk.DOMINION_DOMINATION) && skipCutscenes) {
                    nextCount = 1;
                }
                if (count == 0) {
                    player.getInterfaceManager().removeMinigameHudInterface();
                    player.getInterfaceManager().sendInterface(1172);
                    player.getPackets().sendHideIComponent(1172, 2, true);
                    player.getPackets().sendHideIComponent(1172, 5, true);
                    player.getPackets().sendIComponentText(1172, 8, "YOU WIN!");
                    player.getPackets().sendIComponentText(1172, 10, "Your dominion factor is now: " + dominionFactor);
                    if (player.getPerkManager().hasPerkActive(DonationPerk.DOMINION_DOMINATION) && skipCutscenes) {
                        player.getPackets().sendCameraPos(Cutscene.getX(player, getBaseX() + 35), Cutscene.getY(player, getBaseY() + 37), 2500);
                        player.getPackets().sendCameraLook(Cutscene.getX(player, getBaseX() + 35), Cutscene.getY(player, getBaseY() + 28), 800);
                        player.getPackets().sendCameraPos(Cutscene.getX(player, getBaseX() + 42), Cutscene.getY(player, getBaseY() + 37), 2500, 6, 6);
                    }
                    player.getPackets().sendVoice(7897);
                } else if (count == nextCount) {
                    player.reset(true, false, false);
                    player.getInterfaceManager().closeOverlay(false);
                    player.getPackets().sendResetCamera();
                    player.unlock();
                    stop();
                }
                count++;
            }
        }, 0, 1);

        player.getAchievements().updateProgress(player.getPerkManager().hasPerkActive(DonationPerk.THE_MINI___GAMER) ? 2 : 1, AchievementList.REACH_20_DOMINION_TOWER_KC);

    }

    public void destroyArena(final boolean logout, final int mode) {
        final WorldTile tile = new WorldTile(3744, 6425, 0);
        if (logout) {
            player.setLocation(tile);
        } else {
            player.getControlerManager().removeControlerWithoutCheck();
            player.lock(15);
            player.setNextWorldTile(tile);
            if (mode == ENDURANCE) {
                progress = 0;
            }
        }
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                CoresManager.getServiceProvider().executeNow(() -> {
                    try {
                        MapBuilder.destroyMap(mapBaseCoords[0], mapBaseCoords[1], 8, 8);
                        if (!logout) {
                            mapBaseCoords = null;
                            player.unlock();
                        }
                    } catch (final Exception e1) {
                        Logger.getGlobal().error("Failed destroying Dominion Tower map.", e1);
                    }
                });
            }
        }, 1);
    }

    public NPC[] createBosses() {
        final NPC[] bosses = new NPC[BOSSES[getNextBossIndex()].ids.length];
        for (int i = 0; i < BOSSES[getNextBossIndex()].ids.length; i++) {
            bosses[i] = World.spawnNPC(BOSSES[getNextBossIndex()].ids[i], new WorldTile(getBaseX() + 37 + (i * 2), getBaseY() + 31, 2), -1, true, true);
        }
        return bosses;
    }

    public int getBaseX() {
        return mapBaseCoords[0] << 3;
    }

    public int getBaseY() {
        return mapBaseCoords[1] << 3;
    }

    public void selectBoss() {
        if (nextBossIndex < 0 || nextBossIndex >= BOSSES.length) {
            nextBossIndex = Utils.random(BOSSES.length);
        }
    }

    public void openEnduranceMode() {
        selectBoss();
        final int factorGain = getBossesTotalLevel() * 10;
        player.getInterfaceManager().sendCentralInterfaceLargeInterface(1173);
        player.getPackets().sendIComponentText(1173, 25, BOSSES[getNextBossIndex()].name); // current
        player.getPackets().sendIComponentText(1173, 38, String.valueOf(progress + 1)); // current
        int amountKillsOnWin = player.getPerkManager().hasPerkActive(DonationPerk.THE_MINI___GAMER) && player.getPerkManager().hasPerkActive(DonationPerk.DOMINION_DOMINATION) ? 3 : player.getPerkManager().hasPerkActive(DonationPerk.THE_MINI___GAMER) || player.getPerkManager().hasPerkActive(DonationPerk.DOMINION_DOMINATION) ? 2 : 1;
        player.getPackets().sendIComponentText(1173, 52, "<col=00ff00>If you succeed to slay " + BOSSES[getNextBossIndex()].name + ", " + "I will grant you " + amountKillsOnWin + " Dominion tower " + "boss kill" + (amountKillsOnWin > 1 ? "s" : "") + ". You currently have " + getKilledBossesCount() + " boss kills." + "<br><br><col=00ff00>On death, you won't loose or gain any points, nor you will loose your equipment. " + "<br><br>" + Colors.RED + "Don't die in there. " + "<br><br>" + Colors.RED + "Good luck, warrior!");
        player.getPackets().sendIComponentText(1173, 29, "" + dominionFactor);
        player.getPackets().sendIComponentText(1173, 31, "" + factorGain);
        player.getPackets().sendIComponentText(1173, 33, "" + Math.round(dominionFactor + factorGain));
    }

    public int getBossesTotalLevel() {
        int level = 0;
        for (final int id : BOSSES[getNextBossIndex()].ids) {
            level = +NPCDefinitions.getNPCDefinitions(id).combatLevel;
        }
        return level;
    }

    public void talkToFace() {
        talkToFace(false);
    }

    public void talkToFace(final boolean fromDialogue) {
        if (!hasRequiriments()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need at least level 110 combat to play this minigame!");
            return;
        }
        if (!talkedWithFace) {
            player.getDialogueManager().startDialogue("StrangeFace");
        } else {
            if (!fromDialogue) {
                player.getPackets().sendVoice(7893);
            }
            player.getInterfaceManager().sendInterface(1160);
        }
    }

    public void openRewards() {
        if (!talkedWithFace) {
            talkToFace();
            return;
        }

        player.sendOptionsDialogue("Which reward set?", new String[] { "Item Rewards", "Experience Rewards" }, new DialogueOptionEvent() {

            @Override
            public void run(Player player) {
                int option = getOption();
                if (option == OPTION_1) {
                    openItemRewards();
                } else if (option == OPTION_2)
                    player.getDialogueManager().startDialogue(DominionFactorRewardD.class.getSimpleName());
            }
        });
    }

    private void openItemRewards() {
        player.getInterfaceManager().sendInterface(1156);
        player.getPackets().sendVoice(7893);
//        player.getPackets().sendConfig(2414, -1);
        // hide scroll buttons
        player.getPackets().sendHideIComponent(1156, 96, false);
        player.getPackets().sendHideIComponent(1156, 97, true);
        player.getPackets().sendHideIComponent(1156, 98, true);
        player.getPackets().sendHideIComponent(1156, 99, true);
        
        player.getPackets().sendHideIComponent(1156, 100, true);
        player.getPackets().sendHideIComponent(1156, 102, true);
        player.getPackets().sendHideIComponent(1156, 103, true);
        player.getPackets().sendHideIComponent(1156, 104, true);
        player.getPackets().sendHideIComponent(1156, 108, false);
        player.getPackets().sendHideIComponent(1156, 109, false);
        player.getPackets().sendHideIComponent(1156, 113, false);
        player.getPackets().sendHideIComponent(1156, 114, false);
        player.getPackets().sendHideIComponent(1156, 137, false);
        player.getPackets().sendHideIComponent(1156, 138, false);
        player.getPackets().sendHideIComponent(1156, 110, false);
        player.getPackets().sendHideIComponent(1156, 111, false);
        player.getPackets().sendHideIComponent(1156, 116, false);
        player.getPackets().sendHideIComponent(1156, 117, false);
        player.getPackets().sendHideIComponent(1156, 134, false);
        player.getPackets().sendHideIComponent(1156, 135, false);

        player.getPackets().sendIComponentText(1156, 190, "Dominion Tower rewards");

        if (rewardPage == 0) {
            player.getPackets().sendIComponentText(1156, 108, "Goliath Gloves <col=B22222>(100 Dominion Tower Kills)");
            player.getPackets().sendIComponentText(1156, 109, "One of the best gloves in-game for Melee");
            player.getPackets().sendIComponentText(1156, 113, "Spellcaster Gloves <col=B22222>(100 Dominion Tower Kills)");
            player.getPackets().sendIComponentText(1156, 114, "One of the best gloves in-game for Mage");
            player.getPackets().sendIComponentText(1156, 137, "Swift Gloves <col=B22222>(100 Dominion Tower Kills)");
            player.getPackets().sendIComponentText(1156, 138, "One of the best gloves in-game for Ranging");
            player.getPackets().sendIComponentText(1156, 110, "Dominion Sword <col=B22222>(150 Dominion Tower Kills)");
            player.getPackets().sendIComponentText(1156, 111, "One of the best weapons in-game for Melee");
            player.getPackets().sendIComponentText(1156, 116, "Dominion Staff <col=B22222>(125 Dominion Tower Kills)");
            player.getPackets().sendIComponentText(1156, 117, "One of the strongest staves in-game for Mage");
            player.getPackets().sendIComponentText(1156, 134, "Dominion crossbow <col=B22222>(200 Dominion Tower Kills)");
            player.getPackets().sendIComponentText(1156, 135, "One of the best crossbows in-game for Ranging");

//		player.getPackets().sendHideIComponent(1156, 105, true);
//		player.getPackets().sendHideIComponent(1156, 106, true);//left scroll

            // remove claim buttons if can't claim the reward
            player.getPackets().sendHideIComponent(1156, 88, getKilledBossesCount() < 100);
            player.getPackets().sendHideIComponent(1156, 115, getKilledBossesCount() < 100);
            player.getPackets().sendHideIComponent(1156, 139, getKilledBossesCount() < 100);
            player.getPackets().sendHideIComponent(1156, 112, getKilledBossesCount() < 150);
            player.getPackets().sendHideIComponent(1156, 136, getKilledBossesCount() < 200);
            player.getPackets().sendHideIComponent(1156, 118, getKilledBossesCount() < 125);
            player.getPackets().sendHideIComponent(1156, 198, true); // removes lock
            player.getPackets().sendHideIComponent(1156, 246, true); // removes lock
            // TODO Find second (from bottom) lock removal component id

            // overwrite text
            player.getPackets().sendIComponentText(1156, 90, Colors.GREEN + "Claim");
            player.getPackets().sendIComponentText(1156, 206, Colors.GREEN + "Claim");
            player.getPackets().sendIComponentText(1156, 254, Colors.GREEN + "Claim");
            player.getPackets().sendIComponentText(1156, 200, Colors.GREEN + "Claim");
            player.getPackets().sendIComponentText(1156, 212, Colors.GREEN + "Claim");
            player.getPackets().sendIComponentText(1156, 248, Colors.GREEN + "Claim");
        } else if (rewardPage == 1) {
            player.getPackets().sendIComponentText(1156, 108, "Dreadnip x75 <col=B22222>(50k Dominion Tower Factor & 200 Kills)");
            player.getPackets().sendIComponentText(1156, 109, "It eats monsters for breakfast.");
            player.getPackets().sendIComponentText(1156, 113, "Fire lycan (adult) <col=B22222>(20M Dominion Tower Factor)");
            player.getPackets().sendIComponentText(1156, 114, "A small fire lycan, don't touch.");
            player.getPackets().sendHideIComponent(1156, 137, true);
            player.getPackets().sendHideIComponent(1156, 138, true);
            player.getPackets().sendHideIComponent(1156, 110, true);
            player.getPackets().sendHideIComponent(1156, 111, true);
            player.getPackets().sendHideIComponent(1156, 116, true);
            player.getPackets().sendHideIComponent(1156, 117, true);
            player.getPackets().sendHideIComponent(1156, 134, true);
            player.getPackets().sendHideIComponent(1156, 135, true);
            player.getPackets().sendHideIComponent(1156, 115, true);
            player.getPackets().sendHideIComponent(1156, 139, true);
            player.getPackets().sendHideIComponent(1156, 112, true);
            player.getPackets().sendHideIComponent(1156, 136, true);
            player.getPackets().sendHideIComponent(1156, 118, true);
            player.getPackets().sendHideIComponent(1156, 198, true); // removes lock
            player.getPackets().sendHideIComponent(1156, 246, true); // removes lock

            player.getPackets().sendHideIComponent(1156, 88, dominionFactor < 50_000);
            player.getPackets().sendIComponentText(1156, 90, Colors.GREEN + "Claim");

            player.getPackets().sendHideIComponent(1156, 115, dominionFactor < 20_000_000);
            player.getPackets().sendIComponentText(1156, 206, Colors.GREEN + "Claim");
        }
    }

    public void openBankChest() {
        if (!talkedWithFace) {
            growFace();
            return;
        }
        if (!player.promptList()) {
            player.getBank().openBank();
        } else {
            player.getDialogueManager().startDialogue("BankList", false);
        }
    }

    public void viewScoreBoard() {
        DTRank.showRanks(player);
    }

    public boolean isTalkedWithFace() {
        return talkedWithFace;
    }

    public void setTalkedWithFace(final boolean talkedWithFace) {
        this.talkedWithFace = talkedWithFace;
    }

    public int getProgress() {
        return progress;
    }

    public long getTotalScore() {
        return totalScore;
    }

    public int getDominionFactor() {
        return dominionFactor;
    }

    public Boss getNextBoss() {
        return BOSSES[getNextBossIndex()];
    }

    public int getMaxFloorEndurance() {
        return maxFloorEndurance;
    }

    public int getKilledBossesCount() {
        return killedBossesCount;
    }

    public static final class Boss {

        private final String name;
        private final String text;
        private final int[] ids;
        private final boolean forceMulti;
        private final Item item;
        private final int voice;
        private final int[] arena;

        public Boss(final String name, final String text, final int... ids) {
            this(name, text, -1, false, null, NORMAL_ARENA, ids);
        }

        public Boss(final String name, final String text, final int voice, final boolean forceMulti, final Item item, final int[] arena, final int... ids) {
            this.name = name;
            this.text = text;
            this.forceMulti = forceMulti;
            this.ids = ids;
            this.item = item;
            this.voice = voice;
            this.arena = arena;
        }

        public boolean isForceMulti() {
            return forceMulti;
        }

        public String getName() {
            return name;
        }
    }
}