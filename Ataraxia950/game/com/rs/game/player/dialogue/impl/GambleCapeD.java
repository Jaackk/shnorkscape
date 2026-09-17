package com.rs.game.player.dialogue.impl;

import com.google.common.collect.ImmutableMap;
import com.rs.game.activites.gim.GIM;
import com.rs.game.item.Item;
import com.rs.game.player.content.dropcollection.DropCollectionConstants;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.controllers.FightCaves;
import com.rs.game.player.controllers.FightKiln;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class GambleCapeD extends Dialogue {

    private enum GambleType {
        JAD("Jad"),
        KILN("Kiln");

        private final String name;

        GambleType(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    // ITEM ID -> Pet rolls
    private static final ImmutableMap<Integer, Integer> JAD_CAPES = ImmutableMap.of(
            6570, 1,
            23659, 2,
            31610, 2,
            31611, 2,
            31603, 8
    );

    private static final ImmutableMap<Integer, Integer> KILN_CAPES = ImmutableMap.of(
            23659, 1,
            31610, 1,
            31611, 1,
            31603, 5
    );

    @Override
    public void start() {
        sendOptionsDialogue("Select an option.",
                "I want to gamble capes for a jad pet.",
                "I want to gamble capes for a kiln pet.");
        stage = 0;
    }

    @Override
    public void run(int interfaceId, int componentId) {
        switch (stage) {
            case 0:
                stage = 1;
                player.lock();
                int rolls = 0;
                GambleType type = null;
                if (componentId == OPTION_1) {
                    rolls = computeRolls(JAD_CAPES);
                    type = GambleType.JAD;
                } else if (componentId == OPTION_2) {
                    rolls = computeRolls(KILN_CAPES);
                    type = GambleType.KILN;
                }
                if (rolls == 0) {
                    player.unlock();
                    sendNPCDialogue(15161, NORMAL, "You do not have any capes you can gamble for a " + type + " pet!");
                    break;
                }
                player.getInterfaceManager().closeChatBoxInterface();
                rollForPet(type, rolls);
                break;
            case 1:
                end();
                break;
        }
    }

    @Override
    public void finish() {

    }

    private void rollForPet(GambleType type, int rolls) {
        boolean gotPet = computeGotPet(type, rolls);
        Dialogue.sendNPCDialogueNoContinue(player, 15161, NORMAL, "Rolling your cape(s) for a " + type + " pet... [Rolls: " + rolls + "]");
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.unlock();
                Dialogue.closeNoContinueDialogue(player);
                if (gotPet) {
                    Pets pet = getPet(type);
                    int id = pet.getBabyItemId();
                    player.addItem(new Item(id, 1));
                    sendItemDialogue(id, 1, "You have received a "+pet.getName()+" pet!");
                    HcimNewsManager.getInstance().addNews(player, "<#player> got the "+pet.getName()+" pet");
                            player.getDropCollectionHandler().handleMinigames(type == GambleType.KILN ? DropCollectionConstants.MINIGAME_DATA.FIGHT_KILN : DropCollectionConstants.MINIGAME_DATA.FIGHT_CAVES, id);
                } else {
                    sendNPCDialogue(15161, NORMAL, "Unfortunately, the odds were not in your favor this time.");
                }
            }
        }, 5);
    }

    private boolean computeGotPet(GambleType type, int rolls) {
        int dropRate = getDropRate(type) * 2;
        boolean gotPet = false;
        for (int loops = 0; loops < rolls; loops++) {
            gotPet = ThreadLocalRandom.current().nextInt(dropRate) == 0;
            if (gotPet) {
                break;
            }
        }
        return gotPet;
    }

    private int getDropRate(GambleType type) {
        switch (type) {
            case JAD:
                return FightCaves.JAD_PET_DR;
            case KILN:
                return FightKiln.KILN_PET_DR;
            default:
                throw new IllegalStateException("Invalid gamble type!");
        }
    }

    private Pets getPet(GambleType type) {
        switch (type) {
            case JAD:
                return Pets.TZREK_JAD;
            case KILN:
                return Pets.SHRIMPY;
            default:
                throw new IllegalStateException("Invalid gamble type!");
        }
    }

    private int computeRolls(ImmutableMap<Integer, Integer> capeMap) {
        int rolls = 0;
        Set<Integer> delete = new HashSet<>(8);
        for (Item item : player.getInventory().getItemArray()) {
            if (item == null) {
                continue;
            }
            Integer rollAmount = capeMap.get(item.getId());
            if (rollAmount != null) {
                delete.add(item.getId());
                rolls += rollAmount;
            }
        }
        for (int id : delete) {
            player.getInventory().deleteAllId(id);
        }
        return rolls;
    }
}
