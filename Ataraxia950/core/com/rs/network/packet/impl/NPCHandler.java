package com.rs.network.packet.impl;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.dialogue.BankConfirmationD;
import com.rs.game.activites.dnd.eviltree.dialogue.EvilTreeHunterD;
import com.rs.game.activites.dnd.eviltree.dialogue.TeleportConfirmationD;
import com.rs.game.activites.dnd.eviltree.entity.EvilTreeHunterNPC;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.activites.gim.GIMPrestigeManager;
import com.rs.game.activites.gim.event.DActiveEvents;
import com.rs.game.activites.gim.guide.DGIMGuide;
import com.rs.game.activites.gim.guide.DGroupSettings;
import com.rs.game.activites.gim.guide.DUnregisteredGIM;
import com.rs.game.activites.pest.CommendationExchange;
import com.rs.game.activites.quest.deathsbounty.GuthixD;
import com.rs.game.activites.quest.root_of_evil.LotteryCoordinatorD;
import com.rs.game.activites.quest.root_of_evil.MisterWigglesD;
import com.rs.game.activites.quest.root_of_evil.QuestioningD;
import com.rs.game.activites.quest.root_of_evil.RootOfEvil;
import com.rs.game.activites.soulwars.Barricade;
import com.rs.game.activities.aod.AoDController;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.activities.seasonalevents.christmas.SantaDialogue;
import com.rs.game.npc.NPC;
import com.rs.game.npc.PetMorphHandler;
import com.rs.game.npc.dungeonnering.DungeoneeringWisp;
import com.rs.game.npc.eds.EliteDungeonNPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.gwd2.gregorovic.ShadowCombat;
import com.rs.game.npc.others.LivingRock;
import com.rs.game.npc.others.randomevent.impl.AgilityRandomEvent;
import com.rs.game.npc.others.randomevent.impl.CookingRandomEvent;
import com.rs.game.npc.others.randomevent.impl.CraftingRandomEvent;
import com.rs.game.npc.others.randomevent.impl.DivinationRandomEvent;
import com.rs.game.npc.others.randomevent.impl.FarmingRandomEvent;
import com.rs.game.npc.others.randomevent.impl.FiremakingRandomEvent;
import com.rs.game.npc.others.randomevent.impl.FishingRandomEvent;
import com.rs.game.npc.others.randomevent.impl.FletchingRandomEvent;
import com.rs.game.npc.others.randomevent.impl.HerbloreRandomEvent;
import com.rs.game.npc.others.randomevent.impl.MiningRandomEvent;
import com.rs.game.npc.others.randomevent.impl.PrayerRandomEvent;
import com.rs.game.npc.others.randomevent.impl.RunecraftingRandomEvent;
import com.rs.game.npc.others.randomevent.impl.SmithingRandomEvent;
import com.rs.game.npc.others.randomevent.impl.SummoningRandomEvent;
import com.rs.game.npc.others.randomevent.impl.ThievingRandomEvent;
import com.rs.game.npc.others.randomevent.impl.WoodcuttingRandomEvent;
import com.rs.game.npc.pet.Pet;
import com.rs.game.npc.slayer.Fungi;
import com.rs.game.npc.slayer.GelatinousAbomination;
import com.rs.game.npc.slayer.Rockslug;
import com.rs.game.npc.slayer.Strykewyrm;
import com.rs.game.player.PerkManager;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Fishing;
import com.rs.game.player.actions.Fishing.FishingSpots;
import com.rs.game.player.actions.Rest;
import com.rs.game.player.actions.divination.Wisp;
import com.rs.game.player.actions.hunter.FlyingEntityHunter;
import com.rs.game.player.actions.mining.LivingMineralMining;
import com.rs.game.player.actions.mining.MiningBase;
import com.rs.game.player.actions.runecrafting.SiphonActionCreatures;
import com.rs.game.player.actions.runecrafting.SiphonDonatorRunesphere;
import com.rs.game.player.actions.thieving.PickPocketAction;
import com.rs.game.player.actions.thieving.def.PickPocketableNPC;
import com.rs.game.player.content.*;
import com.rs.game.player.content.death.DeathPurchaseD;
import com.rs.game.player.content.death.DeathResetDegradeD;
import com.rs.game.player.content.death.DeathStatistics;
import com.rs.game.player.content.death.Gravestone;
import com.rs.game.player.content.dropprediction.DropUtils;
import com.rs.game.player.content.dungeoneering.DungeonConstants;
import com.rs.game.player.content.dungeoneering.rooms.puzzles.SlidingTilesRoom;
import com.rs.game.player.content.homearea.HomeAreaHandler;
import com.rs.game.player.content.items.AncientEffigy;
import com.rs.game.player.content.jujupotions.WitchDoctorD;
import com.rs.game.player.content.jujupotions.WitchDoctorTeleportD;
import com.rs.game.player.content.jujupotions.WitchDoctorTutorialD;
import com.rs.game.player.content.newlottery.DLotteryTalk;
import com.rs.game.player.content.newlottery.Lottery;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.content.xmas.SantaDialogue2;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.controllers.bossInstance.VoragoInstanceController;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.dialogue.impl.*;
import com.rs.game.player.dialogue.impl.slayer.sophanem.MenaphiteGuardD;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.io.InputStream;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.misc.ShopsDataParser;
import com.rs.utils.data.parsers.npcs.NPCExaminesDataParser;
import lombok.val;

import java.util.ArrayList;
import java.util.List;

public class NPCHandler {

    /**
     * Number of decoded NPC dispatches (legacy stream path and the native 947
     * router both pass through the decoded overloads); smokes read it to prove
     * the 910 handler ran rather than a bypass.
     */
    public static final java.util.concurrent.atomic.AtomicLong DISPATCHES = new java.util.concurrent.atomic.AtomicLong();

    /** The decoded option number {@link #dispatch} uses for the examine packet. */
    public static final int EXAMINE_OPTION = -1;

    /**
     * Decoded entry point covering options 1-4 and examine: the legacy stream
     * entries read the wire fields, look the NPC up and delegate to the decoded
     * overloads below; the native 947 router resolves its {@link NPC} through
     * its own visibility model and calls this instead. Unknown options are
     * ignored exactly as an unknown packet would be.
     *
     * @param player   the interacting player
     * @param npc      the resolved NPC (may be null: every overload rejects it)
     * @param option   1..4 or {@link #EXAMINE_OPTION}
     * @param forceRun the client's run flag for this click
     */
    public static void dispatch(final Player player, final NPC npc, final int option, final boolean forceRun) {
        switch (option) {
            case 1:
                handleOption1(player, npc, forceRun);
                break;
            case 2:
                handleOption2(player, npc, forceRun);
                break;
            case 3:
                handleOption3(player, npc, forceRun);
                break;
            case 4:
                handleOption4(player, npc, forceRun);
                break;
            case EXAMINE_OPTION:
                handleExamine(player, npc, forceRun);
                break;
            default:
                break;
        }
    }

    public static void handleOption1(final Player player, final InputStream stream) {
        if (player.isLocked()) {
            return;
        }
        final boolean forceRun = stream.read128Byte() == 1;
        final int npcIndex = stream.readUnsignedShort();

        final NPC npc = World.getNPCs().get(npcIndex);
        handleOption1(player, npc, forceRun);
    }

    /** Decoded body of option 1; the stream entry above delegates here unchanged. */
    public static void handleOption1(final Player player, final NPC npc, final boolean forceRun) {
        if (player.isLocked()) {
            return;
        }
        if (npc == null || npc.isCantInteract() || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId())) {
            return;
        }
        DISPATCHES.incrementAndGet();
        player.stopAll(false);
        if (forceRun)
            player.setRun(forceRun);
        if (!HomeAreaHandler.playerCanInteractWithNpc(player, npc)) {
            HomeAreaHandler.sendCanNotUseNpcOrObjectMessageToPlayer(player, npc.getName(), HomeAreaHandler.Type.NPC);
            return;
        }
        if (npc.getId() == 733 &&
                player.getX() >= 3218 &&
                player.getX() <= 3226 &&
                player.getY() >= 3393 &&
                player.getY() <= 3400 &&
                player.getPlane() == 0) {
            player.setRouteEvent(new RouteEvent(new WorldTile(3223, 3399), () -> {
                player.faceEntity(npc);
                player.getDialogueManager().startDialogue(new Dialogue() {
                    @Override
                    public void start() {
                        sendPlayerDialogue(HAPPY, "Aye bartender! A pint of your finest ale please.");
                        stage = 0;
                    }

                    @Override
                    public void run(int interfaceId, int componentId) {
                        switch (stage) {
                            case 0:
                                sendNPCDialogue(733, HAPPY, "Of course. That'll be 500 coins.");
                                stage = 1;
                                break;
                            case 1:
                                if (player.removeMoney(500)) {
                                    sendPlayerDialogue(HAPPY, "Here you go...");
                                    player.addItem(1917, 1);
                                    stage = 2;
                                } else {
                                    sendPlayerDialogue(HAPPY, "I can't afford it!");
                                    stage = 3;
                                }
                                break;
                            case 2:
                                sendNPCDialogue(733, HAPPY, "Enjoy it!");
                                stage = 3;
                                break;
                            case 3:
                                end();
                                break;
                        }
                    }

                    @Override
                    public void finish() {

                    }
                });
            }));
            return;
        }
        val southWest = RootOfEvil.SOUTH_WEST_QUESTIONING;
        val northEast = RootOfEvil.NORTH_EAST_QUESTIONING;
        if (player.quests.getCurrentStage(RootOfEvil.class) == 3 &&
                npc.getX() >= southWest.getX() &&
                npc.getX() <= northEast.getX() &&
                npc.getPlane() == northEast.getPlane() &&
                npc.getY() >= southWest.getY() &&
                npc.getY() <= northEast.getY() &&
                npc.getPlane() == southWest.getPlane() &&
                !player.questionedNpcs.contains(npc.getId()) &&
                (npc.getDefinitions().hasOption("Talk") ||
                        npc.getDefinitions().hasOption("Talk-to") ||
                        npc.getDefinitions().hasOption("Talk to"))) {
            player.setRouteEvent(new RouteEvent(npc, () -> {
                player.faceEntity(npc);
                if (npc.getName().equalsIgnoreCase("lottery coordinator")) {
                    player.getDialogueManager().startDialogue(new LotteryCoordinatorD());
                } else {
                    player.getDialogueManager().startDialogue(new QuestioningD(npc));
                }
            }));
            return;
        }
        if (npc.getId() >= 2269 && npc.getId() <= 2272) {
            if (!player.getXmas().intro)
                return;

            if (!player.getInventory().containsItem(33590, 1) && player.getEquipment().getWeaponId() != 33590) {
                player.sendMessage("You need a snowball equipped or in your inventory to do this!", true);
                return;
            }

            if (player.getXmas().getDistance(npc) >= 100) {
                player.sendMessage("You are too far away from this snowman!", true);
                return;
            }

            if (player.getXmas().inThrow)
                return;

            if (player.withinDistance(npc, 7))
                player.getXmas().throwSnowball(npc);
            else {
                player.addWalkSteps(npc.getX(), npc.getY());
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        player.getXmas().throwSnowball(npc);
                    }
                }, 0);
            }

            return;
        }

        if (npc.getId() == 26550) {

            int spent = player.getMoneySpent();

                    List<String> options = new ArrayList<String>();
                    options.add("Vote Shop");
                    options.add("Vote Perk Shop");

                    player.getDialogueManager().startDialogue("OptionSelectionD", "Where do you want to go?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                        int page = selector.getPage();
                        switch (page) {
                            case 0:
                                selector.close();
                                switch (option) {
                                    case OptionSelectionD.OptionSelector.OPTION_1:

                                        if (spent <= 4) {

                                            player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You Need A Donation Rank of 5 or More To Pass This Door", true);


                                            return;
                                        }

                                        ShopsDataParser.openShop(player, 555);
                                        return;

                                    case OptionSelectionD.OptionSelector.OPTION_2:

                                        if (spent <= 4) {

                                            player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You Need A Donation Rank of 5 or More To Pass This Door", true);


                                            return;
                                        }

                                        ShopsDataParser.openShop(player, 556);
                                    return;

                                }
                                return;
                        }

                    });

        }
        if (npc.getId() == 2290) {
            ShopsDataParser.openShop(player, 171);
            return;
        }

        if (npc.getId() == 24003) {
            player.stopAll(true, true, true);
            player.faceEntity(npc);
            AoDController.startFight(player);
            return;
        }

        if (npc.getId() == 15440) {
            player.getActionManager().setAction(new SiphonDonatorRunesphere(npc));
            return;
        }

        if (npc.getId() == 4250) {
            player.setRouteEvent(new RouteEvent(npc, () -> {
                npc.faceEntity(player);
                player.faceEntity(npc);
                player.getInterfaceManager().sendInterface(403);
            }, true));
            return;
        }
        if (npc.getId() == 22542) {
            player.setRouteEvent(new RouteEvent(npc, () -> {
                npc.faceEntity(player);
                player.faceEntity(npc);
                ShopsDataParser.openShop(player, 557);

            }, true));
            return;
        }

        if (npc.getId() >= 22447 && npc.getId() <= 22449) {
            player.getActionManager().setAction(new ShadowCombat(npc));
            return;
        }
        if (npc.getId() == 15161) {
            if (!player.withinDistance(npc, 2)) {
                player.setRouteEvent(new RouteEvent(npc, () -> {
                    npc.faceEntity(player);
                    player.faceEntity(npc);
                    player.getDialogueManager().startDialogue("TzhaarMejJeh");
                }));
                return;
            }
            npc.faceEntity(player);
            player.faceEntity(npc);
            player.getDialogueManager().startDialogue("TzhaarMejJeh");
        }

        if (npc.getId() == SantaDialogue2.SANTA) {
            if (!player.withinDistance(npc, 3)) {
                player.setRouteEvent(new RouteEvent(npc, () -> {
                    npc.faceEntity(player);
                    player.faceEntity(npc);
                    player.getDialogueManager().startDialogue("SantaDialogue2", npc);
                }));
                return;
            }

            npc.faceEntity(player);
            player.faceEntity(npc);
            player.getDialogueManager().startDialogue("SantaDialogue2", npc);
        }



        if (npc.getId() == 16927) {
            if (!player.withinDistance(npc, 2)) {
                player.setRouteEvent(new RouteEvent(npc, () -> {
                    npc.faceEntity(player);
                    player.faceEntity(npc);
                    player.getDialogueManager().startDialogue("ContractDialogue");
                }));
                return;
            }
            npc.faceEntity(player);
            player.faceEntity(npc);
            player.getDialogueManager().startDialogue("ContractDialogue");
        }
        if (npc.getId() == 22532) {
            if (player.withinDistance(npc, 2)) {
                player.setRouteEvent(new RouteEvent(npc, () -> {
                    npc.faceEntity(player);
                    player.faceEntity(npc);
                    player.getDialogueManager().startDialogue("blackcoins");
                }));
                return;
            }
        }
        if (npc.getId() == 17495) {
            if (player.withinDistance(npc, 2)) {
                player.setRouteEvent(new RouteEvent(npc, () -> {
                    npc.faceEntity(player);
                    player.faceEntity(npc);
                    player.getDialogueManager().startDialogue("thievingoutfit");
                }));
                return;
            }
        }
        if (npc.getId() == 3709) {
            if (!player.withinDistance(npc, 2)) {
                player.setRouteEvent(new RouteEvent(npc, () -> {
                    npc.faceEntity(player);
                    player.faceEntity(npc);
                    player.getDialogueManager().startDialogue("IronmanModeD");
                }));
                return;
            }
            npc.faceEntity(player);
            player.faceEntity(npc);
            player.getDialogueManager().startDialogue("IronmanModeD");
        }
        if (npc.getId() == 24854) {
            if (player.withinDistance(npc, 3)) {
                player.getGEManager().openGrandExchange();
                npc.faceEntity(player);
                player.faceEntity(npc);
                return;
            }
        }
        if (npc.getName().toLowerCase().contains("grand exchange")) {
            if (player.withinDistance(npc, 2)) {
                player.getGEManager().openGrandExchange();
                npc.faceEntity(player);
                player.faceEntity(npc);
                return;
            }
        }

        if (SlidingTilesRoom.handleSlidingBlock(player, npc)) {
            return;
        }


        // opens banks 1 and bank 2
        // Hard-map specific NPCs to specific banks
        if (npc.getId() == 24856) {
            if (player.withinDistance(npc, 3)) {
            // opens Bank 1
            BankList.openBankByIndex(player, 0);
            return;
        } }


        if (npc.getId() == 24855) { // opens Bank 2
            if (player.withinDistance(npc, 3)) {
                // opens Bank 2
                BankList.openBankByIndex(player, 1);
                return;
            } }




        if (npc.getName().toLowerCase().contains("bank")) {
            if (player.withinDistance(npc, 2)) {
                npc.faceEntity(player);
                player.faceEntity(npc);
                if (!player.promptList()) {
                    player.getDialogueManager().startDialogue("BankList");
                    //player.getBank().openBank();
                } else {
                    player.getDialogueManager().startDialogue("BankList", false);
                }
                return;
            }
        }
        if (SiphonActionCreatures.siphon(player, npc)) {
            return;
        }
        if (npc.getId() == 733) {
            if (player.getTreasureTrails().useNPC(npc)) {
                return;
            }
        }
        if (npc.getId() == 17161 || npc.getId() == 17162) {
            if (player.getControlerManager().getControler() == null || !(player.getControlerManager().getControler() instanceof VoragoInstanceController)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "Vorago is not responding to your calls looks like he can't see you in the room. Try leaving and re-entering the instance again.");
                return;
            }
        }

        if (npc.getId() == 8841) {
            player.setRouteEvent(new RouteEvent(npc.getX() == 3615 && npc.getY() == 5110 ? new WorldTile(3614, 5109, 0) : npc.getX() == 3628 && npc.getY() == 5136 ? new WorldTile(3627, 5137, 0) : npc, () -> {
                player.faceEntity(npc);
                final FishingSpots spot = FishingSpots.forId(npc.getId() | 1 << 24);
                if (spot != null) {
                    player.getActionManager().setAction(new Fishing(spot, npc));
                    return; // its a spot, they wont face us
                }
            }, true));
            return;
        }
        player.setRouteEvent(new RouteEvent(npc, () -> {
            npc.resetWalkSteps();
            player.faceEntity(npc);
            if (!player.getControlerManager().processNPCClick1(npc)) {
                return;
            }

            if (PrifddinasCity.handleNPCOption1(player, npc)) {
                return;
            }
            if (player.getPet() == npc && player.getPet().getType() == Pets.ENTLING) {
                EvilTree.sendEntlingMessage(player);
                return;
            }
            if (FlowerGirlD.listen(player, npc.getId())) {
                return;
            }
            if (npc.getId() == 16971) {
                player.getDialogueManager().startDialogue(new GuthixD());
                return;
            }
            if (npc.getId() == 17508) {
                player.getDialogueManager().startDialogue(new WitchDoctorD());
                return;
            }
            if (npc.getId() == 25616) {
                player.getDialogueManager().startDialogue("BryllThoksdottirD", npc.getId());
                return;
            }
            if (npc instanceof EvilTreeHunterNPC) {
                EvilTreeHunterNPC treeHunter = (EvilTreeHunterNPC) npc;
                player.getDialogueManager().startDialogue(new EvilTreeHunterD(treeHunter.getTree()));
                return;
            } else if (npc.getId() == 13790) {
                player.getDialogueManager().startDialogue(new EvilTreeHunterD(null));
                return;
            }
            if (npc.getId() == ChristmasSeasonalEvent.SANTA_NPC) {
                player.getDialogueManager().startDialogue(new SantaDialogue());
                return;
            }
            if (npc.getId() == 12319) {
                player.getDialogueManager().startDialogue(new GIMPrestigeManager.GIMPrestigeD());
                return;
            }
            if (npc.getId() == 12320) {
                player.getDialogueManager().startDialogue(new DGIMGuide());
                return;
            }

            if (npc.getId() == 14386) {
                player.getDialogueManager().startDialogue("DeathMainD");
                return;
            }
            if (npc.getId() == 943) {
                player.getDialogueManager().startDialogue("SkillingMasterD", false);
                return;
            }
            if (npc.getId() == 219) {
                if (player.getContracts().canTalkToAdvancedMaster(true)) {
                    player.getDialogueManager().startDialogue("SkillingMasterD", true);
                }
                return;
            }
            if (npc.getId() == 22889) {
                player.getDialogueManager().startDialogue("Soothsayer", npc.getId());
                return;
            }
            if (npc.getId() == 36) {
                player.getDialogueManager().startDialogue("WysonTheGardenerD");
                return;
            }
            final FishingSpots spot = FishingSpots.forId(npc.getId() | 1 << 24);
            if (spot != null) {
                player.getActionManager().setAction(new Fishing(spot, npc));
                return; // its a spot, they wont face us
            } else if (npc.getId() >= 8928 && npc.getId() <= 8930) {
                player.getActionManager().setAction(new LivingMineralMining((LivingRock) npc));
                return;
            }
            if (npc.getId() == 24169) {
                player.getDialogueManager().startDialogue("KelharD", 1);
                return;
            }
            /*
             * if (npc.getId() == 17161) {
             * player.getDialogueManager().startDialogue("VoragoD", npc.getId()); return; }
             */
            if (npc instanceof Gravestone) {
                final Gravestone gsh = (Gravestone) npc;
                if (gsh.getPlayer() == player) {
                    player.getDialogueManager().startDialogue("InspectGravestone", gsh);
                } else {
                    gsh.sendGraveInscription(player);
                }
                return;
            }

            if (npc.getId() == 2059) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("ZavisticRavreD");
                return;
            }

            if (npc.getId() == 22436) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("VindictasChosen", 1);
                return;
            }
            if (npc.getId() == 15417) {
                player.getDialogueManager().startDialogue(WizardFinixHomeD.class.getSimpleName(), npc.getId());
                return;
            }
            if (npc.getId() == 22435) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("HelwyrsChosen", 1);
                return;
            }
            if (npc.getId() == 22433) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("GregorovicsChosen", 1);
                return;
            }
            if (npc.getId() == 22434) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("FuriesChosen", 1);
                return;
            }
            if (npc.getId() == 22437) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("FengTheBountyMaster", 1);
                return;
            }
            if (npc.getId() == 961) {
                player.getDialogueManager().startDialogue("GeneralTafaniD");
                return;
            }
            if (npc.getId() == 16825) {
                player.getDialogueManager().startDialogue("GorajoHoardstalkerD");
                return;
            }

            if (npc.getId() == 456) {
                Gravestone.sendInterface(player);
                return;
            }

            if (npc.getId() == 418) {
                player.getDialogueManager().startDialogue("EvilTreeLeprechaunD");
                return;
            }

            if (GelatinousAbomination.handleDeath(npc, player)) {
                return;
            }
            if (Fungi.pickFungi(player, npc)) {
                return;
            }

            /*
             * if (npc.getId() == 9085) {
             * player.getDialogueManager().startDialogue("SlayerMasterD", npc, false);
             * return; }
             */

            if (npc.getId() == 3781) {
                player.getDialogueManager().startDialogue("PCSquire", 1);
                return;
            }

            if (npc.getId() == 20629) {
                player.getDialogueManager().startDialogue("WildyWyrmD");
                return;
            }

            if (npc.getId() == Lottery.getSingleton().coordinatorId) {
                int currentStage = player.quests.getCurrentStage(RootOfEvil.class);
                if (currentStage == 5 || currentStage == 6) {
                    player.getDialogueManager().startDialogue(new Dialogue() {

                        @Override
                        public void start() {
                            sendOptionsDialogue("Select an option.",
                                    "Talk about " + Colors.RED + "Root of Evil</col>",
                                    "Other");
                            stage = 0;
                        }

                        @Override
                        public void run(int interfaceId, int componentId) {
                            if (stage == 0) {
                                if (componentId == OPTION_1) {
                                    player.getDialogueManager().startDialogue(new LotteryCoordinatorD());
                                } else if (componentId == OPTION_2) {
                                    player.getDialogueManager().startDialogue("DLotteryTalk");

                                }
                            }
                        }

                        @Override
                        public void finish() {

                        }
                    });
                } else {
                    player.getDialogueManager().startDialogue("DLotteryTalk");
                }
                return;
            }

            if (npc.getId() == 7600) {
                player.getDialogueManager().startDialogue("FiaraD");
                npc.faceEntity(player);
                return;
            }

            if (npc.getId() == DungeonConstants.SMUGGLER) {
                player.getDialogueManager().startDialogue("SmugglerD");
                return;
            }

            if (npc.getId() == 9712) {
                player.getDialogueManager().startDialogue("DungeoneeringTutor");
                return;
            }
            if (npc.getId() == 279) {
                String[] options = {"Bronze Donator Store", "Platinum Donator Store"};
                player.getDialogueManager().startDialogue("OptionSelectionD", "Which shop would you like to Open?", options, options, (OptionSelectionD.SelectionEvent) (selector, option) -> {
                    int page = selector.getPage();
                    switch (page) {
                        case 0:
                            selector.close();
                            switch (option) {
                                case OptionSelectionD.OptionSelector.OPTION_1:
                                    if (player.isBronzeDonor()) {
                                        ShopsDataParser.openShop(player, 159);
                                    } else {
                                        player.sendMessage(Colors.RED + Colors.SHAD + "You need to be a Bronze donator to open this shop!");
                                    }
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_2:
                                    if (player.isPlatinumDonor()) {
                                        ShopsDataParser.openShop(player, 169);
                                    } else {
                                        player.sendMessage(Colors.RED + Colors.SHAD + "You need to be a Platinum donator to open this shop!");
                                    }
                            }
                            break;
                    }
                });
            }
            if (npc.getId() == 2778) {
                ShopsDataParser.openShop(player, 92);
                return;
            }
            if (npc.getName().toLowerCase().contains("bank") || npc.getId() == 15194 || npc.getId() == 13455 || npc.getId() == 2617) {
                if (!player.promptList()) {
                    player.getDialogueManager().startDialogue("BankList");
                    //player.getBank().openBank();
                } else {
                    player.getDialogueManager().startDialogue("BankList", false);
                }
                return;
            }
            if (npc.getName().toLowerCase().contains("musician")) {
                final long currentTime1 = Utils.currentTimeMillis();
                if (player.getEmotesManager().getNextEmoteEnd() >= currentTime1 || player.getLockDelay() >= currentTime1) {
                    player.sendMessage("You can't rest while perfoming an emote.");
                    return;
                }
                player.stopAll();
                player.getActionManager().setAction(new Rest());
                return;
            }
            if (npc.getName().toLowerCase().contains("impling") || npc.getName().equalsIgnoreCase("ruby harvest") || npc.getName().equalsIgnoreCase("sapphire glacialis") || npc.getName().equalsIgnoreCase("snowy knight") || npc.getName().equalsIgnoreCase("black warlock")) {
                FlyingEntityHunter.captureFlyingEntity(player, npc);
                return;
            }
            if (npc instanceof Pet) {
                final Pet pet = (Pet) npc;
                if (pet != player.getPet()) {
                    player.sendMessage("This isn't your pet.", true);
                    return;
                }
                if (npc.getId() < 15980 || npc.getId() == 17086 || npc.getId() >= 18915 && npc.getId() <= 18917 || npc.getId() == 22503 || npc.getId() == 18823) {
                    if (!player.getInventory().hasFreeSlots()) {
                        player.sendMessage("You do not have enough inventory space to pickup your pet.", true);
                        return;
                    }
                    player.setNextAnimation(new Animation(827));
                    pet.pickup();
                    return;
                }
                pet.sendInteract();
                return;
            }
            if (npc instanceof Familiar) {
                if (player.getFamiliar() != npc) {
                    player.getPackets().sendGameMessage("That isn't your familiar.", true);
                    return;
                }
                if (npc.getDefinitions().getOption(0) != null && npc.getDefinitions().getOption(0).equalsIgnoreCase("Interact")) {
                    player.getDialogueManager().startDialogue("FamiliarInteractD", npc);
                    return;
                }
                if (npc.getDefinitions().hasOption("store")) {
                    player.getFamiliar().store();
                } else if (npc.getDefinitions().hasOption("cure")) {
                    if (!player.getPoison().isPoisoned()) {
                        player.sendMessage("You're already healthy.");
                        return;
                    } else {
                        player.getFamiliar().drainSpecial(2);
                        player.addPoisonImmune(120);
                    }
                }
                return;
            }
            if (npc.hasMenuOption("Listen-to")) {
                if (player.isResting()) {
                    player.stopAll();
                    return;
                }
                final long currentTime2 = Utils.currentTimeMillis();
                if (player.getEmotesManager().getNextEmoteEnd() >= currentTime2) {
                    player.getPackets().sendGameMessage("You can't rest while perfoming an emote.");
                    return;
                }
                if (player.getLockDelay() >= currentTime2) {
                    player.getPackets().sendGameMessage("You can't rest while perfoming an action.");
                    return;
                }
                player.stopAll();
                player.getActionManager().setAction(new Rest());
            }
            npc.faceEntity(player);
            if (npc.getId() == MenaphiteGuardD.NPC_ID) {
                player.getDialogueManager().startDialogue(MenaphiteGuardD.class.getSimpleName());
                return;
            }
            if (npc instanceof Wisp) {
                final Wisp wisp1 = (Wisp) npc;
                wisp1.harvest(player);
                return;
            }
            if (npc instanceof DungeoneeringWisp) {
                final DungeoneeringWisp wisp2 = (DungeoneeringWisp) npc;
                wisp2.harvest(player);
                return;
            }
            if (npc.getId() == 21633) {
                player.getDialogueManager().startDialogue("AngofD");
                npc.faceEntity(player);
                return;
            }
            if (npc.getId() == 13651) {
                player.getDialogueManager().startDialogue("EasterDialogue");
                npc.faceEntity(player);
                return;
            }
            if (npc.getId() == 8091) {
                player.getDialogueManager().startDialogue("StarSpriteD");
                return;
            }
            if (npc.getId() == 18516) {
                player.getDialogueManager().startDialogue("ContractDialogue");
                return;
            }
            if (npc.getId() == 6524) {
                player.getDialogueManager().startDialogue("BobBarterD", npc.getId(), 1);
                return;
            }
            if (npc.getId() == 3404) {
                player.getDialogueManager().startDialogue("TeplinMacaganD", npc.getId(), 1);
                return;
            }
            if (npc.getId() == 17143) {
                player.getDialogueManager().startDialogue("Ocellus", npc.getId(), (byte) 1);
                return;
            }
            if (npc.getId() == 2371) {
                player.getDialogueManager().startDialogue(new MisterWigglesD());
                return;
            }
            if (npc.getId() == 14381) {
                if (player.withinDistance(npc, 2)) {
                    npc.faceEntity(player);
                    player.faceEntity(npc);
                }
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        sendNPCDialogue(npc.getId(), CALM, "Hey " + player.getDisplayName() + ", how can I help you?");
                        stage = 0;
                    }

                    @Override
                    public void run(final int interfaceId, final int componentId) {
                        switch (stage) {
                            case 0:
                                sendOptionsDialogue("Choose an option", "Check for cosmetic rewards", "Reward list", "Walk away");
                                stage = 1;
                                break;
                            case 1:
                                switch (componentId) {
                                    case OPTION_1:
                                        final String sfCol = AncientEffigy.getEffigies(player) >= 25 ? Colors.GREEN : Colors.RED;
                                        final String fbCol = player.getInventory().containsItem(6723, 100000) ? Colors.GREEN : Colors.RED;
                                        sendOptionsDialogue("Choose a reward", sfCol + "Sack of effigies", fbCol + "Fishbowl challenge");
                                        stage = 11;
                                        break;
                                    case OPTION_2:
                                        sendOptionsDialogue("Choose a reward to view the requirements", "Sack of effigies", "Fishbowl challenge", "Walk away");
                                        stage = 3;
                                        break;
                                    case OPTION_3:
                                        finish();
                                        break;
                                }
                                break;
                            case 11:
                                switch (componentId) {
                                    case OPTION_1:
                                        if (AncientEffigy.getEffigies(player) > 25) {
                                            sendNPCDialogue(npc.getId(), CALM, "You have " + (AncientEffigy.getEffigies(player) - 25) + " too many effigies!");
                                            stage = 99;
                                            break;
                                        }
                                        if (AncientEffigy.canSack(player)) {
                                            sendOptionsDialogue("Are you sure?", "Yes", "No");
                                            stage = 14;
                                        } else {
                                            sendNPCDialogue(npc.getId(), CALM, "You need 25 effigies to do that!");
                                            stage = 99;
                                        }
                                        break;
                                    case OPTION_2:
                                        if (player.getInventory().containsItem(6723, 100000)) {
                                            sendOptionsDialogue("Are you sure?", "Yes", "No");
                                            stage = 15;
                                        } else {
                                            sendNPCDialogue(npc.getId(), CALM, "You need 100,000 fishbowls for that!");
                                            stage = 99;
                                        }
                                        break;
                                }
                                break;
                            case 14:
                                switch (componentId) {
                                    case OPTION_1:
                                        finish();
                                        AncientEffigy.effigySack(player);
                                        break;
                                    case OPTION_2:
                                        finish();
                                        break;
                                }
                                break;
                            case 15:
                                switch (componentId) {
                                    case OPTION_1:
                                        if (player.getItemsMade() < 100000) {
                                            sendNPCDialogue(npc.getId(), ANGRY, "Hey, you didn't craft all of these yourself!");
                                            stage = 16;
                                            break;
                                        }
                                        final int[] rewards = {7534, 7535, 8929, 9634, 9636, 9638};
                                        finish();
                                        if (player.getInventory().getFreeSlots() >= 6) {
                                            player.getInventory().deleteItem(6723, 100000);
                                            for (final int items : rewards) {
                                                player.getInventory().addItem(items, 1);
                                            }
                                            World.sendWorldMessage("<img=6>" + Colors.CYAN + "<shad=000000>News: " + player.getDisplayName() + " has completed the fishbowl challenge!", false);
                                        } else {
                                            player.sendMessage(Colors.RED + "You need at least 6 inventory spaces!");
                                        }
                                        break;
                                    case OPTION_2:
                                        finish();
                                        break;
                                }
                                break;
                            case 16:
                                finish();
                                player.sendMessage(Colors.RED + "You need 100,000 items crafted total to complete the challenge.");
                                break;
                            case 3:
                                switch (componentId) {
                                    case OPTION_1:
                                        sendNPCDialogue(npc.getId(), CALM, "Bring me 25 effigies and I'll exchange it for a sack of effigies!");
                                        break;
                                    case OPTION_2:
                                        sendNPCDialogue(npc.getId(), CALM, "For 100,000 fishbowls, I'll give you a unique diving set!");
                                        player.sendMessage(Colors.GREEN + "Set contains: vyrewatch set, crabclaw and hook, diving apparatus, and fishbowl helmet.");
                                        break;
                                    case OPTION_3:
                                        finish();
                                }
                                stage = 99;
                                break;
                            case 99:
                                finish();
                                break;
                        }
                    }

                    @Override
                    public void finish() {
                        player.getInterfaceManager().closeChatBoxInterface();
                    }

                });
                return;
            }
            if (npc.getName().toLowerCase().contains("grand exchange")) {
                player.getGEManager().openGrandExchange();
                return;
            }

            if (npc.getId() == 17170) {
                if (!player.isBronzeDonor() && !player.isDonator()) {
                    player.sendMessage(Colors.RED + Colors.SHAD + "You need to be a donator to talk to him.");
                    return;
                }
                player.getDialogueManager().startDialogue("AutoUnfPotionD");

                /*
                 * player.setRouteEvent(new RouteEvent(npc, () -> { npc.faceEntity(player);
                 * player.faceEntity(npc); }, true));
                 */
                return;
            }

            if (player.getTreasureTrails().useNPC(npc)) {
                return;
            }
            if (npc.getId() == 2824) {
                HideTanning.tanHides(player, false);
                return;
            }
            if (npc.getId() == 587) {
                player.getDialogueManager().startDialogue("Jatix", npc.getId(), 1);
                return;
            }
            if (npc.getId() == 5941) {
                player.getDialogueManager().startDialogue("SimplePlayerMessage", "Better not to disturb him while he's at work.");
                return;
            }
            if (npc.getId() == 6539) {
                if (ArtefactExchangingD.getCostOfArtefacts(player) > 0) {
                    player.getDialogueManager().startDialogue("ArtefactExchangingD");
                } else {
                    player.getDialogueManager().startDialogue("NastrothD", npc.getId());
                }
                return;
            }
            if (npc.getId() == 12378) {
                player.getDialogueManager().startDialogue("PeteJackNigga", 12378);
                return;
            }
            if (npc.getId() == 13727) {
                player.getDialogueManager().startDialogue("XuanD", npc.getId(), 1);
                return;
            }
            if (npc.getId() == 5913) {
                player.getDialogueManager().startDialogue("Aubury", npc);
                return;
            }
            if (npc.getId() == 18808) {
                player.getDialogueManager().startDialogue("SolomonD", npc.getId());
                return;
            }
            if (npc.getName().toLowerCase().contains("estate agent")) {
                player.getDialogueManager().startDialogue("EstateAgent", npc.getId());
                return;
            }
            if (npc.getId() == 9400) {
                player.getDialogueManager().startDialogue("SantaClause", npc.getId());
                return;
            }
            if (npc.getId() == 5113) {
                player.getDialogueManager().startDialogue("HunterExpertD", npc.getId());
                return;
            }
            if (npc.getId() == 19519) {
                player.getDialogueManager().startDialogue("ElfHermitD", npc);
                return;
            }
            if (npc.getId() == 18198) {
                player.getDialogueManager().startDialogue("MayStormbrewerD", npc.getId(), false);
                return;
            }
            if (npc.getId() == 659) {
                player.getDialogueManager().startDialogue("PartyPete", 659);
                return;
            }
            if (npc.getId() == 9711) {
                player.getDialogueManager().startDialogue("RewardsTraderD", npc.getId(), 1);
                return;
            }
            if (npc.getId() == 14620) {
                player.getDialogueManager().startDialogue("MerchantD", npc.getId());
                return;
            }
            if (npc.getId() == 2253) {
                player.getDialogueManager().startDialogue("WiseOldMan", npc.getId());
                return;
            }
            if (npc.getId() == 756) {
                UziPerkShop.openShop(player);
                return;
            }
            if (npc.getId() == 1686) {
                player.getDialogueManager().startDialogue("GhostDisciple", npc.getId());
            }
            if (npc.getName().toLowerCase().contains("trial announcer")) {
                player.getDialogueManager().startDialogue("TrialAnnouncerD", npc.getId());
                return;
            }
            if (npc.getId() == 15158) {
                player.getDialogueManager().startDialogue("AIOShop", npc.getId());
                return;
            }
             if (npc.getId() == 25190) {
              player.getDialogueManager().startDialogue("deepsea", npc.getId());
                return;
            }
            if (npc.getId() == 554) {
                player.getDialogueManager().startDialogue("FancyDressShopOwner", npc.getId());
                return;
            }
            if (npc.getId() == 9085) {
                player.getDialogueManager().startDialogue("Kuradal", npc.getId());
                return;
            }
            if (npc.getId() == 6893) {
                player.getDialogueManager().startDialogue("PetShopOwner", npc.getId());
                return;
            }
            if (npc.getId() == 594) {
                player.getDialogueManager().startDialogue("NurmofD", npc.getId());
                return;
            }
            if (npc.getId() == 340) {
                player.getDialogueManager().startDialogue(Dicer.class.getSimpleName(), npc);
                return;
            }
            if (npc.getId() == 669) {



                        npc.faceEntity(player);
                        player.faceEntity(npc);
                ShopsDataParser.openShop(player, 1337);


            }





            if (npc.getId() == 17114) {
                if (player.isGroupIronman()) {
                    player.sendMessage("Group Irons cannot access this shop.");
                    return;
                }
                ShopsDataParser.openShop(player, 180);
            }
            switch (npc.getName().toLowerCase()) {
                case "void knight":
                    player.getDialogueManager().startDialogue("VoidNPCD");
                    // CommendationExchange.openExchangeShop(player);
                    break;
                case "shopkeeper":
                case "shop assistant":
                    player.getDialogueManager().startDialogue("GeneralStore", npc.getId(), 1);
                    break;
                case "sheep":
                    SheepShearing.shearAttempt(player, npc);
                    break;
                case "tool leprechaun":
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "Uhh.. These giants just ruin everything...");
                    break;
            }
            if (npc.getId() == 15451 && npc instanceof FiremakingRandomEvent) {
                final FiremakingRandomEvent spirit1 = (FiremakingRandomEvent) npc;
                spirit1.giveReward(player);
                return;
            }
            if (npc.getId() == 17169 && npc instanceof PrayerRandomEvent) {
                final PrayerRandomEvent monk = (PrayerRandomEvent) npc;
                monk.giveReward(player);
                return;
            }
            if (npc.getId() == 7954 && npc instanceof CraftingRandomEvent) {
                final CraftingRandomEvent crafting = (CraftingRandomEvent) npc;
                crafting.giveReward(player);
                return;
            }
            if (npc.getId() == 2170 && npc instanceof FarmingRandomEvent) {
                final FarmingRandomEvent farming = (FarmingRandomEvent) npc;
                farming.giveReward(player);
                return;
            }
            if (npc.getId() == 15105 && npc instanceof FletchingRandomEvent) {
                final FletchingRandomEvent fletch = (FletchingRandomEvent) npc;
                fletch.giveReward(player);
                return;
            }
            if (npc.getId() == 14 && npc instanceof MiningRandomEvent) {
                final MiningRandomEvent mining = (MiningRandomEvent) npc;
                mining.giveReward(player);
                return;
            }
            if (npc.getId() == 15419 && npc instanceof RunecraftingRandomEvent) {
                final RunecraftingRandomEvent wizard = (RunecraftingRandomEvent) npc;
                wizard.giveReward(player);
                return;
            }
            if (npc.getId() == 17347 && npc instanceof HerbloreRandomEvent) {
                final HerbloreRandomEvent herblore = (HerbloreRandomEvent) npc;
                herblore.giveReward(player);
                return;
            }
            if (npc.getId() == 8122 && npc instanceof ThievingRandomEvent) {
                final ThievingRandomEvent rogue = (ThievingRandomEvent) npc;
                rogue.giveReward(player);
                return;
            }
            if (npc.getId() == 2551 && npc instanceof SmithingRandomEvent) {
                final SmithingRandomEvent smithing = (SmithingRandomEvent) npc;
                smithing.giveReward(player);
                return;
            }
            if (npc.getId() == 5447 && npc instanceof AgilityRandomEvent) {
                final AgilityRandomEvent penguin = (AgilityRandomEvent) npc;
                penguin.giveReward(player);
                return;
            }
            if (npc.getId() == 18204 && npc instanceof DivinationRandomEvent) {
                final DivinationRandomEvent chronicle = (DivinationRandomEvent) npc;
                chronicle.giveReward(player);
                return;
            }
            if (npc.getId() == 11454 && npc instanceof FishingRandomEvent) {
                final FishingRandomEvent fishing = (FishingRandomEvent) npc;
                fishing.giveReward(player);
                return;
            }
            if (npc.getId() == 5910 && npc instanceof CookingRandomEvent) {
                final CookingRandomEvent cook = (CookingRandomEvent) npc;
                cook.giveReward(player);
                return;
            }
            if (npc.getId() == 16887 && npc instanceof SummoningRandomEvent) {
                final SummoningRandomEvent summoner = (SummoningRandomEvent) npc;
                summoner.giveReward(player);
                return;
            }
            if (npc.getId() == 1051 && npc instanceof WoodcuttingRandomEvent) {
                final WoodcuttingRandomEvent spirit2 = (WoodcuttingRandomEvent) npc;
                spirit2.giveReward(player);
                return;
            } else if (npc.getName().toLowerCase().contains("grand exchange")) {
                player.getDialogueManager().startDialogue("Fiara");
            } else if (npc.getId() == 1597) {
                player.getDialogueManager().startDialogue("Vannaka");
            } else if (npc.getId() == 9462 || npc.getId() == 9464 || npc.getId() == 9466 || npc.getId() == 2417) {
                Strykewyrm.handleStomping(player, npc);
            } else if (npc.getId() == 6139) {
                player.getDialogueManager().startDialogue("StarterTutorialD");
            } else if (npc.getId() == 5141) {
                player.getDialogueManager().startDialogue("UgiD", npc);
            } else if (npc.getId() == 8541) {
                player.getDialogueManager().startDialogue("HeadImpD", npc.getId());
            } else if (npc.getId() == 15583) {
                player.getDialogueManager().startDialogue("GuardD", npc.getId());
            } else if (npc.getId() == 549) {
                player.getDialogueManager().startDialogue("HorvikD", npc.getId());
            } else if (npc.getId() == 537) {
                player.getDialogueManager().startDialogue("ScavvoD", npc.getId());
            } else if (npc.getId() == 550) {
                player.getDialogueManager().startDialogue("LoweD", npc.getId());
            } else if (npc.getId() == 557) {
                player.getDialogueManager().startDialogue("WydinD", npc.getId());
            } else if (npc.getId() == 546) {
                player.getDialogueManager().startDialogue("ZaffD", npc.getId());
            } else if (npc.getId() == 519) {
                player.getDialogueManager().startDialogue("Bob", npc.getId(), false);
            } else if (npc.getId() == 9707) {
                player.getDialogueManager().startDialogue("FremennikShipmaster", npc.getId(), true);
            } else if (npc.getId() == 9708) {
                player.getDialogueManager().startDialogue("FremennikShipmaster", npc.getId(), false);
            } else if (npc.getId() == 278) {
                player.getDialogueManager().startDialogue("Cook", npc.getId());
            } else if (npc.getId() == 598) {
                player.getDialogueManager().startDialogue("Hairdresser", npc.getId());
            } else if (npc.getId() == 548) {
                player.getDialogueManager().startDialogue("Thessalia", npc.getId());
            } else if (npc.getId() == 2676) {
                player.getDialogueManager().startDialogue("MakeOverMage", npc.getId(), 0);
            } else if (npc.getId() == 6988) {
                player.getDialogueManager().startDialogue("SimpleNPCMessage", 6988, "We don't have anything to talk about. If you wish to buy items, then trade me.. If not - get lost!");
            } else {
                // player.sendMessage("Nothing interesting happens.");
                if (Settings.DEBUG) {
                    Logger.getGlobal().info("cliked 1 at npc id : " + npc.getId() + ", " + npc.getX() + ", " + npc.getY() + ", " + npc.getPlane());
                }
            }
        }));
    }

    public static void handleOption2(final Player player, final InputStream stream) {
        if (player.isLocked()) {
            return;
        }
        final boolean forceRun = stream.read128Byte() == 1;
        final int npcIndex = stream.readUnsignedShort();
        final NPC npc = World.getNPCs().get(npcIndex);
        handleOption2(player, npc, forceRun);
    }

    /** Decoded body of option 2; the stream entry above delegates here unchanged. */
    public static void handleOption2(final Player player, final NPC npc, final boolean forceRun) {
        if (player.isLocked()) {
            return;
        }
        if (npc == null || !player.getMapRegionsIds().contains(npc.getRegionId())) {
            return;
        }
        DISPATCHES.incrementAndGet();
        player.stopAll(false);
        if (forceRun) {
            player.setRun(forceRun);
        }
        if (player.isOwner()) {
            player.getPackets().sendPanelBoxMessage("Option 2: " + npc);
        }
        if (!HomeAreaHandler.playerCanInteractWithNpc(player, npc)) {
            HomeAreaHandler.sendCanNotUseNpcOrObjectMessageToPlayer(player, npc.getName(), HomeAreaHandler.Type.NPC);
            return;
        }
        if (npc.getDefinitions().name.toLowerCase().contains("bank") || npc.getId() == 15194) {
            if (player.withinDistance(npc, 2)) {
                npc.faceEntity(player);
                player.faceEntity(npc);
                player.getDialogueManager().startDialogue("Banker", npc.getId());
                return;
            }
        }
        if (npc.getName().toLowerCase().contains("diango")) {
            npc.faceEntity(player);
            player.faceEntity(npc);
            ShopsDataParser.openShop(player, 63);
            return;
        }

        if (npc.getName().toLowerCase().contains("grand exchange")) {
            if (player.withinDistance(npc, 2)) {
                player.getDialogueManager().startDialogue("GrandExchangeClerkD", npc.getId());
                npc.faceEntity(player);
                player.faceEntity(npc);
                return;
            }
        }
        if (npc.getId() == 25508) {
            player.setRouteEvent(new RouteEvent(npc, () -> {
                npc.faceEntity(player);
                player.faceEntity(npc);
                player.getDialogueManager().startDialogue("solakgate", npc);
            }, true));
            return;
        }

        if (npc.getId() == DungeonConstants.SMUGGLER) {
            ShopsDataParser.openShop(player, 200);
            return;
        }

        // this is here only for marking npcs in dung.
        if (player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof DungeonController && npc.getDefinitions().hasOption("Mark")) {
            if (!player.getControlerManager().processNPCClick2(npc)) {
                return;
            }
        }
        if (npc instanceof EliteDungeonNPC && npc.getDefinitions().hasOption("Mark")) {
            player.faceEntity(npc);
            if (!player.getControlerManager().processNPCClick2(npc))
                return;
        }

        player.setRouteEvent(new RouteEvent(npc, () -> {
            npc.resetWalkSteps();
            player.faceEntity(npc);
            if (Rockslug.handleDeath(npc, player, null)) {
                return;
            }
            if (npc == null || npc.isCantInteract() || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId())) {
                return;
            }
            if (player.getPet() == npc && player.getPet().getType() == Pets.ENTLING) {
                EvilTree.nurtureEntling(player);
                return;
            }
            final FishingSpots spot = FishingSpots.forId(npc.getId() | (2 << 24));
            if (spot != null) {
                player.getActionManager().setAction(new Fishing(spot, npc));
                return;
            } else if (npc instanceof Gravestone) {
                final Gravestone grave = (Gravestone) npc;
                grave.repair(player, false);
                return;
            }

            if (npc instanceof EvilTreeHunterNPC) {
                player.sendMessage("You are already here!");
                return;
            } else if (npc.getId() == 13790) {
                player.getDialogueManager().startDialogue(new TeleportConfirmationD(false));
                return;
            }
            if (npc.getId() == 36) {
                WysonTheGardenerD.exchange(player);
                return;
            }
            if (npc.getId() == 24169) {
                player.getDialogueManager().startDialogue("KelharD", 2);
                return;
            }
            if (npc.getId() == 17508) {
                ShopsDataParser.openShop(player, 183);
                return;
            }
             if (npc.getId() == 25190) {
              player.getDialogueManager().startDialogue("deepsea", 2);
                return;
            }
            if (npc.getId() == 14386) {
                player.getDialogueManager().startDialogue(new DeathPurchaseD());
                return;
            }
            if (npc.getId() == ChristmasSeasonalEvent.SANTA_NPC) {
                ChristmasSeasonalEvent.giveSantaPresents(player);
                return;
            }
            if (npc.getId() == 22436) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("VindictasChosen", 2);
                return;
            }
            if (npc.getId() == Lottery.getSingleton().coordinatorId) {
                DLotteryTalk.placeBet(player);
                return;
            }
            if (npc.getId() == 22435) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("HelwyrsChosen", 2);
                return;
            }
            if (npc.getId() == 15417) {
                ShopsDataParser.openShop(player, 170);
                return;
            }
            if (npc.getId() == 22433) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("GregorovicsChosen", 2);
                return;
            }
            if (npc.getId() == 12320) {
                player.getDialogueManager().startDialogue(new DActiveEvents().dontLoop());
                return;
            }
            if (npc.getId() == 12319) {
                player.prestigeManager.displayPrestiges();
                return;
            }
            if (npc.getId() == 22434) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("FuriesChosen", 2);
                return;
            }
            if (npc.getId() == 22437) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("FengTheBountyMaster", 2);
                return;
            }
            if (npc.getId() == 17161) {
                if (player.getControlerManager().getControler() == null || !(player.getControlerManager().getControler() instanceof VoragoInstanceController)) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "Vorago is not responding to your calls looks like he can't see you in the room. Try leaving and re-entering the instance again.");
                    return;
                }
            }
            final PickPocketableNPC pocket = PickPocketableNPC.get(npc.getId());
            if (pocket != null) {
                player.getActionManager().setAction(new PickPocketAction(npc, pocket));
                return;
            }
            if (npc.getId() == 9712) {
                player.getDungeoneeringJournals().sendInterface();
                return;
            }

            if (npc.getId() == 594) {
                ShopsDataParser.openShop(player, 159);
                return;
            }

            if (npc.getId() == 3781) {
                player.getDialogueManager().startDialogue("PCSquire", 2);
                return;
            }
            if (npc.getId() == 943) {
                player.getContracts().checkedAssignContract(false, 943);
                return;
            }
            if (npc.getId() == 219) {
                if (player.getContracts().canTalkToAdvancedMaster(true)) {
                    player.getContracts().checkedAssignContract(true, 219);
                }
                return;
            }
            if (npc.getId() == 961) {
                npc.faceEntity(player);
                player.faceEntity(npc);
                GeneralTafaniD.restorePlayer(player);
                return;
            }

            /*
             * if (npc.getId() == 9085) {
             * player.getDialogueManager().startDialogue("SlayerMasterD", npc, true);
             * return; }
             */

            if (npc.getId() == 21633) {
                ShopsDataParser.openShop(player, 65);
            }
            if (npc.getId() == MenaphiteGuardD.NPC_ID) {
                ShopsDataParser.openShop(player, 1811);
                return;
            }
            if (player.getTreasureTrails().useNPC(npc)) {
                return;
            }
            if (npc.getId() == 519) {
                ShopsDataParser.openShop(player, 16);
                return;
            }
            /*
             * if (npc.getId() == 17161) {
             * player.getDialogueManager().startDialogue("VoragoChallenge"); return; }
             */
            if (npc instanceof Barricade) {
                player.setNextAnimation(new Animation(16700));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        ((Barricade) npc).litFire();
                    }
                }, 2);
                return;
            }
            if (npc instanceof Familiar) {
                if (npc.getDefinitions().getOption(1) != null && npc.getDefinitions().getOption(1).equalsIgnoreCase("Interact")) {
                    if (player.getFamiliar() != npc) {
                        player.getPackets().sendGameMessage("That isn't your familiar.", true);
                        return;
                    }
                    player.getDialogueManager().startDialogue("FamiliarInteractD", npc);
                    // if (npc.getId() == 7341 || npc.getId() == 7342) {
                    // player.getDialogueManager().startDialogue("FireTitan");
                    // } else {
                    // player.getDialogueManager().startDialogue("SimplePlayerMessage",
                    // "Why would I talk to a familiar? That's just
                    // weird.");
                    // }
                }
                return;
            }
            if (npc instanceof Pet) {
                final Pet pet = (Pet) npc;
                if (npc.getId() == 20390) {
                    player.lock();
                    player.faceEntity(pet);
                    pet.faceEntity(player);
                    WorldTasksManager.schedule(new WorldTask() {
                        int loop = 0;

                        @Override
                        public void run() {
                            if (loop == 0) {
                                player.setNextAnimation(new Animation(1351));
                                pet.setNextForceTalk(new ForceTalk("UwU I hope you enjoy!"));
                            } else if (loop == 3) {
                                pet.setNextAnimation(new Animation(Utils.randomFrom(Pet.CHEERLEADER_DANCE)));
                            } else if (loop == 5) {
                                pet.setNextAnimation(new Animation(Utils.randomFrom(Pet.CHEERLEADER_DANCE)));
                            } else if (loop == 7) {
                                pet.setNextAnimation(new Animation(Utils.randomFrom(Pet.CHEERLEADER_DANCE)));
                            } else if (loop == 9) {
                                pet.setNextAnimation(new Animation(Utils.randomFrom(Pet.CHEERLEADER_DANCE)));
                            } else if (loop == 11) {
                                pet.setNextAnimation(new Animation(Utils.randomFrom(Pet.CHEERLEADER_DANCE)));
                            } else if (loop == 13) {
                                pet.faceEntity(player);
                            } else if (loop == 14) {
                                pet.setNextForceTalk(new ForceTalk("Thank you " + player.getDisplayName() + "-san!"));
                                pet.setNextAnimation(new Animation(194));
                            } else if (loop == 16) {
                                player.unlock();
                                player.getAppearence().setRenderEmote(-1);
                                player.setNextAnimation(new Animation(-1));
                            }
                            loop++;
                        }
                    }, 1, 1);
                    return;
                }
                if (npc.getId() < 15980) {
                    if (pet != player.getPet()) {
                        player.sendMessage("This isn't your pet.", true);
                        return;
                    }
                    player.getDialogueManager().startDialogue("SimplePlayerMessage", "Why would I do that? That's just plain weird.");
                    return;
                }
                if (pet != player.getPet()) {
                    player.sendMessage("This isn't your pet.", true);
                    return;
                }
                if (!player.getInventory().hasFreeSlots()) {
                    player.sendMessage("You do not have enough inventory space to pickup your pet.", true);
                    return;
                }
                player.setNextAnimation(new Animation(827));
                pet.pickup();
                return;
            }
            npc.faceEntity(player);
            if (!player.getControlerManager().processNPCClick2(npc)) {
                return;
            }
            if (PrifddinasCity.handleNPCOption2(player, npc)) {
                return;
            }
            if (npc.getId() == 6524) {
                player.getDialogueManager().startDialogue("BobBarterD", npc.getId(), 2);
                return;
            }
            if (npc.getId() == 3404) {
                player.getDialogueManager().startDialogue("TeplinMacaganD", npc.getId(), 2);
                return;
            }
            if (ShopsDataParser.openShopByNpc(player, npc.getId())) {
                return;
            }
            switch (npc.getDefinitions().name.toLowerCase()) {
                case "void knight":
                    CommendationExchange.openExchangeShop(player);
                    break;
                case "shopkeeper":
                case "shop assistant":
                    ShopsDataParser.openShop(player, 1);
                    break;
                case "tool leprechaun":
                    ShopsDataParser.openShop(player, 13);
                    break;
                case "trial announcer":
                    TrialAnnouncerD.teleport(player);
                    break;
            }
            if (npc.getId() == 15194 || npc.getId() == 2617) {
                if (!player.promptList()) {
                    player.getDialogueManager().startDialogue("BankList");
                    //player.getBank().openBank();
                } else {
                    player.getDialogueManager().startDialogue("BankList", false);
                }
            }
            if (npc.getId() == 9707) {
                FremennikShipmaster.sail(player, true);
            } else if (npc.getId() == 9708) {
                FremennikShipmaster.sail(player, false);
            } else if (npc.getDefinitions().name.toLowerCase().contains("bank") || npc.getId() == 15194 || npc.getId() == 13455) {
                player.getDialogueManager().startDialogue("Banker", npc.getId());
            } else if (npc.getId() == 6893) {
                PetShopOwner.openShop(player);
            } else if (npc.getId() == 538) {
                ShopsDataParser.openShop(player, 2);
            } else if (npc.getId() == 549) {
                ShopsDataParser.openShop(player, 3);
            } else if (npc.getId() == 537) {
                player.getDialogueManager().startDialogue("ScavvoD");
            } else if (npc.getId() == 554) {
                ShopsDataParser.openShop(player, 5);
            } else if (npc.getId() == 557) {
                ShopsDataParser.openShop(player, 6);
            } else if (npc.getId() == 546) {
                ShopsDataParser.openShop(player, 7);
            } else if (npc.getId() == 550) {
                ShopsDataParser.openShop(player, 8);
            } else if (npc.getId() == 659) {
                ShopsDataParser.openShop(player, 47);
            } else if (npc.getId() == 14620) {
                ShopsDataParser.openShop(player, 51);
            } else if (npc.getId() == 2620) {
                ShopsDataParser.openShop(player, 30);
            } else if (npc.getId() == 2622) {
                ShopsDataParser.openShop(player, 29);
            } else if (npc.getId() == 2623) {
                ShopsDataParser.openShop(player, 28);
            } else if (npc.getId() == 5113) {
                ShopsDataParser.openShop(player, 31);
            } else if (npc.getId() == 9711) {
                DungeonRewardShop.openRewardShop(player);
            } else if (npc.getId() == 5913) {
                ShopsDataParser.openShop(player, 7);
            } else if (npc.getId() == 548) {
                ShopsDataParser.openShop(player, 21);
            } else if (npc.getId() == 6988) {
                player.getDialogueManager().startDialogue("SummoningStoreD", npc.getId());
            } else if (npc.getId() == 598) {
                PlayerLook.openCharacterCustomizing(player);
            } else if (npc.getId() == 2676) {
                PlayerLook.openMageMakeOver(player);
            }
            if (npc.getName().toLowerCase().contains("grand exchange")) {
                player.getDialogueManager().startDialogue("GrandExchangeClerkD", npc.getId());
                return;
            }
            if (npc.getId() == 2824) {
                player.getDialogueManager().startDialogue("EllisD", npc.getId());
                return;
            }
            if (npc.getId() == 1686) {
                if (player.getInventory().hasFreeSlots() && player.tokensUnclaimed > 0) {
                    player.getInventory().addItem(4278, player.tokensUnclaimed);
                    player.tokensUnclaimed = 0;
                }
            }
            if (npc.getId() == 9085) {
                player.getDialogueManager().startDialogue("KuradalGetTask", npc.getId());
                return;
            }

            if (npc.getId() == 13727) {
                player.getDialogueManager().startDialogue("XuanD", npc.getId(), 4);
                return;
            }
            if (npc.getId() == 17143) {
                player.getDialogueManager().startDialogue("Ocellus", npc.getId(), (byte) 2);
                return;
            }
            if (npc.getId() == 587) {
                player.getDialogueManager().startDialogue("Jatix", npc.getId(), 0);
                return;
            }
            /** Sailor (Travel to Miscellania) **/
            if (npc.getId() == 1304) {
                player.lock();
                player.sendMessage("You board the ship..", true);
                FadingScreen.fade(player, 0, () -> {
                    player.setNextWorldTile(new WorldTile(2581, 3845, 0));
                    player.sendMessage("The Sailor has taken you to Miscellania.", true);
                    player.unlock();
                });
                return;
            }
            /** Sailor (Travel to Rellekka) **/
            if (npc.getId() == 1385) {
                player.lock();
                player.sendMessage("You board the ship..", true);
                FadingScreen.fade(player, 0, () -> {
                    player.setNextWorldTile(new WorldTile(2630, 3693, 0));
                    player.sendMessage("The Sailor has taken you back to Rellekka.", true);
                    player.unlock();
                });
                return;
            } else {
                // player.sendMessage("Nothing interesting happens.");
                if (Settings.DEBUG) {
                    Logger.getGlobal().info("cliked 2 at npc id : " + npc.getId() + ", " + npc.getX() + ", " + npc.getY() + ", " + npc.getPlane());
                }
            }
        }));
    }

    public static void handleOption3(final Player player, final InputStream stream) {
        if (player.isLocked()) {
            return;
        }
        final boolean forceRun = stream.read128Byte() == 1;
        final int npcIndex = stream.readUnsignedShort();
        final NPC npc = World.getNPCs().get(npcIndex);
        handleOption3(player, npc, forceRun);
    }

    /** Decoded body of option 3; the stream entry above delegates here unchanged. */
    public static void handleOption3(final Player player, final NPC npc, final boolean forceRun) {
        if (player.isLocked()) {
            return;
        }
        if (npc == null || npc.isCantInteract() || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId())) {
            return;
        }
        DISPATCHES.incrementAndGet();
        player.stopAll(false);
        if (forceRun) {
            player.setRun(forceRun);
        }
        if (player.isOwner()) {
            player.getPackets().sendPanelBoxMessage("Option 3: " + npc);
        }
        if (!HomeAreaHandler.playerCanInteractWithNpc(player, npc)) {
            HomeAreaHandler.sendCanNotUseNpcOrObjectMessageToPlayer(player, npc.getName(), HomeAreaHandler.Type.NPC);
            return;
        }
        if (npc.getDefinitions().name.toLowerCase().contains("banker") || npc.getId() == 15194) {
            if (player.withinDistance(npc, 2)) {
                npc.faceEntity(player);
                player.getGEManager().openCollectionBox();
                player.faceEntity(npc);
                return;
            }
        }
        if (npc.getName().toLowerCase().contains("grand exchange")) {
            if (player.withinDistance(npc, 2)) {
                player.getGEManager().openHistory();
                npc.faceEntity(player);
                player.faceEntity(npc);
                return;
            }
        }
        player.setRouteEvent(new RouteEvent(npc, () -> {
            npc.resetWalkSteps();
            if (!player.getControlerManager().processNPCClick3(npc)) {
                return;
            }
            player.faceEntity(npc);
            if (PrifddinasCity.handleNPCOption3(player, npc)) {
                return;
            }
            if (player.getPet() == npc && player.getPet().getType() == Pets.ENTLING) {
                EvilTree.transformEntling(player);
                return;
            }
            if (npc.getId() >= 8837 && npc.getId() <= 8839) {
                MiningBase.prospect(player, "You examine the remains...", "The remains contain traces of living minerals.");
                return;
            }
            if (npc.getId() == 17508) {
                player.getDialogueManager().startDialogue(new WitchDoctorTeleportD());
                return;
            }
            if (npc.getId() == 25508) {
                if (player.withinDistance(npc, 2)) {
                    npc.faceEntity(player);
                    player.faceEntity(npc);
                    player.getDialogueManager().startDialogue("SimpleMessage",
                            "Hey Xhybrid For Now Im ONLY Allowing One Person At A Time | KillCount Is Required!");
                }
                return;
            }
            if (npc.getId() == Lottery.getSingleton().coordinatorId) {
                DLotteryTalk.viewStats(player);
                return;
            }
            if (npc.getId() == 12320) {
                if (player.isUnregisteredGIM()) {
                    player.getDialogueManager().startDialogue(new DUnregisteredGIM());
                    return;
                }
                if (!player.isGroupIronman()) {
                    Dialogue.sendSingleNPCDialogue(player, 12320, Dialogue.NORMAL,
                            "You aren't a Group Ironman, stop wasting my time peasant.");
                    return;
                }
                GIM.getRewards().claim(player);
                return;
            }
            if (npc.getId() == 12319) {
                player.prestigeManager.startPrestige(null);
                return;
            }
            if (npc instanceof Pet) {
                final Pet pet = (Pet) npc;
                if (npc.getId() == 20390) {
                    if (pet != player.getPet()) {
                        player.sendMessage("This isn't your pet.", true);
                        return;
                    }
                    if (!player.getInventory().hasFreeSlots()) {
                        player.sendMessage("You do not have enough inventory space to pickup your pet.", true);
                        return;
                    }
                    player.setNextAnimation(new Animation(827));
                    pet.pickup();
                    return;
                }
            }
            if (npc instanceof EvilTreeHunterNPC) {
                EvilTreeHunterNPC treeHunter = (EvilTreeHunterNPC) npc;
                player.getDialogueManager().startDialogue(new BankConfirmationD(treeHunter.getTree(), false));
                return;
            } else if (npc.getId() == 13790) {
                player.sendMessage("He's probably too busy for that right now.");
                return;
            }

            if (npc instanceof Gravestone) {
                final Gravestone grave = (Gravestone) npc;
                grave.repair(player, true);
                return;
            }
            if (npc.getId() == 24169) {
                player.getDialogueManager().startDialogue("KelharD", 3);
                return;
            }
            if (npc.getDefinitions().name.contains("Banker") || npc.getDefinitions().name.contains("banker") || npc.getId() == 15194 || npc.getId() == 2617 || npc.getId() == 13455) {
                npc.faceEntity(player);
                player.getGEManager().openCollectionBox();
                player.faceEntity(npc);
            }
            if (npc.getId() == 22436) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("VindictasChosen", 3);
                return;
            }
            if (npc.getId() == 14386) {
                DeathStatistics.getInstance().openClaim(player);
                return;
            }
            if (npc.getId() == 943) {
                if (player.unlockedSkillingShop) {
                    ShopsDataParser.openShop(player, 181);
                } else {
                    player.sendMessage("You have not yet unlocked her shop. Talk to her first.");
                }
                return;
            }
            if (npc.getId() == 219) {
                if (player.unlockedAdvancedSkillingShop && player.getContracts().canTalkToAdvancedMaster(true)) {
                    ShopsDataParser.openShop(player, 182);
                } else {
                    player.sendMessage("You have not yet unlocked his shop. Talk to him first.");
                }
                return;
            }
            if (npc.getId() == 22435) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("HelwyrsChosen", 3);
                return;
            }
            if (npc.getId() == 22433) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("GregorovicsChosen", 3);
                return;
            }
            if (npc.getId() == 519) {
                player.getDialogueManager().startDialogue("Bob", npc.getId(), true);
                return;
            }
            if (npc.getId() == 22434) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("FuriesChosen", 3);
                return;
            }
            if (npc.getId() == 22437) {
                npc.faceEntity(player);
                player.getDialogueManager().startDialogue("FengTheBountyMaster", 3);
                return;
            }
            if (npc.getId() == 554) {
                CustomFurClothing.openInterface(player);
            }
            if (npc.getId() == 9085) {
                ShopsDataParser.openShop(player, 14);
            }
            if (npc.getName().toLowerCase().contains("grand exchange")) {
                player.getGEManager().openHistory();
                return;
            }
            if (npc.getId() == 9712) {
                player.getDialogueManager().startDialogue("GorajanTrailblazerOutfitDialogue");
            }
            if (npc.getId() == 18198) {
                player.getDialogueManager().startDialogue("MayStormbrewerD", npc.getId(), true);
                return;
            }
            if (npc.getId() == 9711) {
                player.getDialogueManager().startDialogue("RewardsTraderD", npc.getId(), 3);
                return;
            }
            if (npc.getId() == 5913) {
                ShopsDataParser.openShop(player, 7);
                return;
            }

            if (npc.getId() == 9398) {
                player.getDialogueManager().startDialogue("XmasDialogue");
                if (!player.withinDistance(npc, 2)) {
                    player.setRouteEvent(new RouteEvent(npc, () -> {
                        npc.faceEntity(player);
                        player.faceEntity(npc);
                        player.getDialogueManager().startDialogue("XmasDialogue");
                    }));
                    return;
                }
                npc.faceEntity(player);
                player.faceEntity(npc);
                player.getDialogueManager().startDialogue("XmasDialogue");
            }
            if (npc.getId() == 17143) {
                player.getDialogueManager().startDialogue("Ocellus", npc.getId(), (byte) 3);
                return;
            }
            npc.faceEntity(player);
            if (npc.getId() == 6524) {
                player.getDialogueManager().startDialogue("BobBarterD", npc.getId(), 3);
                return;
            }
            if (npc.getId() == 3404) {
                player.getDialogueManager().startDialogue("TeplinMacaganD", npc.getId(), 3);
                return;
            }
            if (npc.getId() == 548) {
                PlayerLook.openThessaliasMakeOver(player);
            } else if (npc.getId() == 6892 || npc.getId() == 6893) {
                PetShopOwner.sellShards(player);
            } else if (npc.getId() == 6988) {
                player.getDialogueManager().startDialogue("SimpleNPCMessage", 6988, "My enchanting skills are depleted.. Get lost!");
            } else if (npc.getId() == 14620) {
                player.getDialogueManager().startDialogue("SimpleNPCMessage", 14620, "The gentleman wants to know if we can store flowers " + "for them. We don't store flowers for folks anymore.");
            } else if (npc.getDefinitions().name.contains("Tool leprechaun")) {
                player.getDialogueManager().startDialogue("ToolLeprechaunTeleD");
            } else if (npc.getId() == 5532) {
                npc.setNextForceTalk(new ForceTalk("Senventior Disthinte Molesko!"));
                player.getControlerManager().startControler("SorceressGarden");
            }
        }));
        if (Settings.DEBUG) {
            Logger.getGlobal().info("cliked 3 at npc id : " + npc.getId() + ", " + npc.getX() + ", " + npc.getY() + ", " + npc.getPlane());
        }
    }

    public static void handleOption4(final Player player, final InputStream stream) {
        if (player.isLocked()) {
            return;
        }
        final boolean forceRun = stream.read128Byte() == 1;
        final int npcIndex = stream.readUnsignedShort();
        final NPC npc = World.getNPCs().get(npcIndex);
        handleOption4(player, npc, forceRun);
    }

    /** Decoded body of option 4; the stream entry above delegates here unchanged. */
    public static void handleOption4(final Player player, final NPC npc, final boolean forceRun) {
        if (player.isLocked()) {
            return;
        }
        if (npc == null || npc.isCantInteract() || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId())) {
            return;
        }
        DISPATCHES.incrementAndGet();
        if (player.isOwner()) {
            player.getPackets().sendPanelBoxMessage("Option 4: " + npc);
        }
        player.stopAll(false);
        if (forceRun) {
            player.setRun(forceRun);
        }
        if (!HomeAreaHandler.playerCanInteractWithNpc(player, npc)) {
            HomeAreaHandler.sendCanNotUseNpcOrObjectMessageToPlayer(player, npc.getName(), HomeAreaHandler.Type.NPC);
            return;
        }
        if (npc.getName().toLowerCase().contains("grand exchange")) {
            if (player.withinDistance(npc, 2)) {
                player.getGEManager().openItemSets();
                npc.faceEntity(player);
                player.faceEntity(npc);
                return;
            }
        }
        player.setRouteEvent(new RouteEvent(npc, () -> {
            npc.resetWalkSteps();
            if (!player.getControlerManager().processNPCClick4(npc)) {
                return;
            }
            player.faceEntity(npc);
            npc.faceEntity(player);
            if (PrifddinasCity.handleNPCOption4(player, npc)) {
                return;
            }
            if (npc.getId() == 12320) {
                if (player.isUnregisteredGIM()) {
                    player.getDialogueManager().startDialogue(new DUnregisteredGIM());
                    return;
                }
                if (!player.isGroupIronman()) {
                    Dialogue.sendSingleNPCDialogue(player, 12320, Dialogue.NORMAL,
                            "You aren't a Group Ironman, stop wasting my time peasant.");
                    return;
                }
                GIMGroup group = GIM.getGroupData().get(player.gimKey.getGroupKey());
                if (group == null) {
                    player.sendMessage("There was an issue fetching your group data. Please try again in a few minutes.");
                    return;
                }
                if (!group.getLeaderName().equals(player.getUsername())) {
                    player.sendMessage("Only group leaders can access the group settings.");
                    return;
                }
                player.getDialogueManager().startDialogue(new DGroupSettings());
                return;
            }
            if (npc.getId() == 14386) {
                DeathResetDegradeD.start(player, player.deathItemsManager.getDegradePercentageInt());
                return;
            }
            if (npc.getId() == 3404) {
                player.getDialogueManager().startDialogue("TeplinMacaganD", npc.getId(), 4);
                return;
            }
            if (npc.getId() == 17508) {
                player.getDialogueManager().startDialogue(new WitchDoctorTutorialD());
                return;
            }
            if (npc.getId() == 6524) {
                player.getDialogueManager().startDialogue("BobBarterD", npc.getId(), 4);
                return;
            }
            if (npc.getName().toLowerCase().contains("grand exchange")) {
                player.getGEManager().openItemSets();
                return;
            }
            if (npc.getId() == 9085) {
                SlayerTask.openSlayerShop(player);
                return;
            }
            if (npc.getId() == Lottery.getSingleton().coordinatorId) {
                DLotteryTalk.claimWinnings(player);
                return;
            }
            if (npc.getId() == 943) {
                player.getDialogueManager().startDialogue("SpendPointsD", false);
                return;
            }
            if (npc.getId() == 219) {
                if (player.getContracts().canTalkToAdvancedMaster(true)) {
                    player.getDialogueManager().startDialogue("SpendPointsD", true);
                }
                return;
            }
            if (npc.getId() == 5913) {
                Aubury.teleportToEssenceMines(player, npc);
                return;
            }
            return;
        }));
        return;
    }

    public static void handleExamine(final Player player, final InputStream stream) {
        final boolean forceRun = stream.read128Byte() == 1;
        final int npcIndex = stream.readUnsignedShort();

        final NPC npc = World.getNPCs().get(npcIndex);
        handleExamine(player, npc, forceRun);
    }

    /** Decoded body of examine; the stream entry above delegates here unchanged. */
    public static void handleExamine(final Player player, final NPC npc, final boolean forceRun) {
        if (npc == null || npc.isCantInteract() || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId())) {
            return;
        }
        DISPATCHES.incrementAndGet();
        if (!player.getControlerManager().processNPCExamine(npc)) {
            return;
        }
        if (forceRun) {
            player.setRun(forceRun);
        }

        if (!player.isUnderCombat()) {
            if (player.isHasDropTableEnabled() && npc.getDefinitions().hasAttackOption() && !(npc instanceof Pet)) {
                DropUtils.sendNPCDrops(player, npc.getName());
                return;
            }
        }

        if (player.isUnderCombat()) {
            player.getPackets().sendNPCMessage(0, 15263739, npc, npc.getName() + ": " + npc.getHitpoints() + " hp left!");
            Logger.getGlobal().info(npc.getId());
            return;
        }

        if (npc.getId() == 340 && npc.getX() == 2442 && npc.getY() == 3090) {
            player.getPackets().sendNPCMessage(0, 15263739, npc, "Can I actually trust him?");
            return;
        }

        if (npc.getId() == GeneralTafaniD.NPC_ID) {
            player.getPackets().sendNPCMessage(0, 15263739, npc, "<img=26>" + NPCExaminesDataParser.getExamine(npc));
            return;
        }

        String message = null;
        if (npc instanceof Pet && ((Pet) npc).getOwner() != null) {
            final Player owner = ((Pet) npc).getOwner();
            switch (npc.getName().toLowerCase()) {
                case "barry":
                case "mallory":
                case "bill":
                case "dave":
                case "gavin":
                case "lana":
                case "pete":
                case "steve":
                    message = owner.getDisplayName() + " has done " + owner.getKillStatistics(71) + " Araxxor kills.";
                    break;
                case "chick'arra":
                    message = owner.getDisplayName() + " has done " + owner.getKillStatistics(3) + " Kree'Arra kills.";
                    break;
                case "commander miniana":
                    message = owner.getDisplayName() + " has done " + owner.getKillStatistics(4) + " Commander Zilyana kills.";
                    break;
                case "general awwdor":
                    message = owner.getDisplayName() + " has done " + owner.getKillStatistics(1) + " General Graardor kills.";
                    break;
                case "k'ril tinyroth":
                    message = owner.getDisplayName() + " has done " + owner.getKillStatistics(2) + " K'ril Tsutsaroth kills.";
                    break;
                case "nexterminator":
                    message = owner.getDisplayName() + " has done " + owner.getKillStatistics(5) + " Nex kills.";
                    break;
                case "molly":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(69) + " Giant mole kills.");
                    break;
                case "shrimpy":
                    message = (owner.getDisplayName() + " has done " + -1 + " Har'Aken kills.");
                    break;
                case "king black dragonling":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(8) + " King black dragon kills.");
                    break;
                case "queen black dragonling":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(7) + " Queen black dragon kills.");
                    break;
                case "kalphite grublet":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(54) + " Kalphite queen kills.");
                    break;
                case "kalphite grubling":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(90) + " Kalphite king kills.");
                    break;
                case "corporeal puppy":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(6) + " Corporeal beast kills.");
                    break;
                case "ellie":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(10) + " Chaos elemental kills.");
                    break;
                case "legio primulus":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(83) + " Legio Primus kills.");
                    break;
                case "legio secundulus":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(84) + " Legio Secundus kills.");
                    break;
                case "legio tertiolus":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(85) + " Legio Tertius kills.");
                    break;
                case "legio quartulus":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(86) + " Legio Quartus kills.");
                    break;
                case "legio quintulus":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(87) + " Legio Quintus kills.");
                    break;
                case "legio sextulus":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(88) + " Legio Sextus kills.");
                    break;
                case "rex hatchling":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(70) + " Dagannoth Rex kills.");
                    break;
                case "prime hatchling":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(69) + " Dagannoth Prime kills.");
                    break;
                case "supreme hatchling":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(68) + " Dagannoth Supreme kills.");
                    break;
                case "vitalis":
                case "bombi":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(93) + " Vorago kills.");
                    break;
                case "greg":
                    message = (owner.getDisplayName() + " has done " + (owner.getKillStatistics(123) + owner.getKillStatistics(124)) + " Gregorovic kills.");
                    break;
                case "lilwyr":
                    message = (owner.getDisplayName() + " has done " + (owner.getKillStatistics(113) + owner.getKillStatistics(114)) + " Helwyr kills.");
                    break;
                case "ava":
                case "nylessa":
                    message = (owner.getDisplayName() + " has done " + (owner.getKillStatistics(121) + owner.getKillStatistics(122)) + " Twin Furies' kills.");
                    break;
                case "vindiddy":
                case "rawrvek":
                    message = (owner.getDisplayName() + " has done " + (owner.getKillStatistics(115) + owner.getKillStatistics(120)) + " Vindicta & Gorvek kills.");
                    break;
                case "tess":
                    message = (owner.getDisplayName() + " has done " + owner.getKillStatistics(133) + " Telos kills.");
                    break;
                case "reeves":
                    message = (owner.getDisplayName() + " has done -1 Nex (AoD) kills.");
                    break;
                case "diddyzag":
                    message = (owner.getDisplayName() + " has done -1 Durzag kills.");
                    break;
                case "yakaminu":
                    message = (owner.getDisplayName() + " has done -1 Yakamaru kills.");
                    break;
                case "eddy":
                    message = (owner.getDisplayName() + " has done " + (owner.getKillStatistics(96) + " Edimmu kills."));
                    break;
                case "unstable glacyte":
                    message = (owner.getDisplayName() + " has done " + (owner.getKillStatistics(29) + " Glacor kills."));
                    break;
                case "sapping glacyte":
                    message = (owner.getDisplayName() + " has done " + (owner.getKillStatistics(29) + " Glacor kills."));
                    break;
                case "enduring glacyte":
                    message = (owner.getDisplayName() + " has done " + (owner.getKillStatistics(29) + " Glacor kills."));
                    break;
                case "the minister":
                    message = (owner.getDisplayName() + " has killed The magister " + (owner.getKillStatistics(136) + " time" + (owner.getKillStatistics(136) > 1 ? "s" : "") + "."));
                    break;
                /**
                 * Skilling pets
                 */
                case "rue":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.RUNECRAFTING)) + "xp in Runecrafting.");
                    break;
                case "rocky":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.MINING)) + "xp in Mining.");
                    break;
                case "herbert":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.HERBLORE)) + "xp in Herblore.");
                    break;
                case "gemi":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.CRAFTING)) + "xp in Crafting.");
                    break;
                case "ralph":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.THIEVING)) + "xp in Thieving.");
                    break;
                case "willow":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.DIVINATION)) + "xp in Divination.");
                    break;
                case "ramsay":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.COOKING)) + "xp in Cooking.");
                    break;
                case "sifu":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.ATTACK)) + "xp in Attack.");
                    break;
                case "morty":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.HITPOINTS)) + "xp in Constitution.");
                    break;
                case "wallace":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.DEFENCE)) + "xp in Defence.");
                    break;
                case "gordie":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.DUNGEONEERING)) + "xp in Dungeoneering.");
                    break;
                case "brains":
                    message = owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.FARMING)) + "xp in Farming.";
                    break;
                case "bernie":
                    message = owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.FIREMAKING)) + "xp in Firemaking.";
                    break;
                case "bubbles":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.FISHING)) + "xp in Fishing.");
                    break;
                case "flo":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.FLETCHING)) + "xp in Fletching.");
                    break;
                case "ace":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.HUNTER)) + "xp in Hunter.");
                    break;
                case "newton":
                    message = owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.MAGIC)) + "xp in Magic.";
                    break;
                case "ghostly":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.PRAYER)) + "xp in Prayer.");
                    break;
                case "crabbe":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.SLAYER)) + "xp in Slayer.");
                    break;
                case "sparky":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.RANGE)) + "xp in Ranged.");
                    break;
                case "shamini":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.SUMMONING)) + "xp in Summoning.");
                    break;
                case "smithy":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.SMITHING)) + "xp in Smithy.");
                    break;
                case "kangali":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.STRENGTH)) + "xp in Strength.");
                    break;
                case "woody":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.WOODCUTTING)) + "xp in Woody.");
                    break;
                case "baby yaga's house":
                    message = (owner.getDisplayName() + " has " + Math.round(owner.getSkills().getXp(Skills.CONSTRUCTION)) + "xp in Construction.");
                    break;

            }
        }

        String npcInfo = " (ID: " + npc.getId() + ", X: " + npc.getX() + ", Y: " + npc.getY() + ")";
        String examineText = (message == null ? NPCExaminesDataParser.getExamine(npc) : message);
        player.getPackets().sendNPCMessage(0, 15263739, npc, examineText);
        System.out.println(npcInfo);
        player.getPetPerkManager().sendPerksExamine(npc);

        if (Settings.DEBUG) {
            Logger.getGlobal().info("Examined npc: " + npc);
        }
        if (player.isOwner()) {
            player.getPackets().sendPanelBoxMessage("Examined npc: " + npc);
        }
    }
}
