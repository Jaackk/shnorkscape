package com.rs.network.packet.impl;

import com.rs.Settings;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.*;
import com.rs.game.Hit.HitLook;
import com.rs.game.activites.CastleWars;
import com.rs.game.activites.Crucible;
import com.rs.game.activites.CrystalChest;
import com.rs.game.activites.PuroPuro;
import com.rs.game.activites.dnd.eviltree.entity.EvilRootObject;
import com.rs.game.activites.dnd.eviltree.entity.EvilSaplingObject;
import com.rs.game.activites.dnd.eviltree.entity.EvilTreeObject;
import com.rs.game.activites.dnd.eviltree.entity.EvilWeedsObject;
import com.rs.game.activites.dungeon_architect.DungeonArchitectDialogue;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.pest.Lander;
import com.rs.game.activites.quest.root_of_evil.RootOfEvil;
import com.rs.game.activites.quest.root_of_evil.VaultController;
import com.rs.game.activites.worldevents.ShootingStar;
import com.rs.game.activities.rots.RiseOfTheSix;
import com.rs.game.activities.rots.RiseOfTheSixController;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.activities.seasonalevents.christmas.SantaListDialogue;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.map.bossInstance.BossInstanceHandler;
import com.rs.game.map.bossInstance.BossInstanceHandler.Boss;
import com.rs.game.map.bossInstance.impl.TheMagisterInstance;
import com.rs.game.map.bossInstance.impl.pz.PZInstance;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.ThievingRandomEvent;
import com.rs.game.npc.slayer.Mogre;
import com.rs.game.player.*;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.QuestManager.Quests;
import com.rs.game.player.BuffDebuffTimersManager.Timer;
import com.rs.game.player.actions.*;
import com.rs.game.player.actions.BonesOnAltar.Bones;
import com.rs.game.player.actions.Cooking.Cookables;
import com.rs.game.player.actions.agility.SerenityPostsHandler;
import com.rs.game.player.actions.crafting.DiamondCrafter;
import com.rs.game.player.actions.crafting.CraftingRs3Dialogue;
import com.rs.game.player.actions.crafting.Looming.Loom;
import com.rs.game.player.actions.crafting.RobustGlass;
import com.rs.game.player.actions.divination.DivinationConvert;
import com.rs.game.player.actions.divination.DivinationConvert.ConvertMode;
import com.rs.game.player.actions.divination.impl.*;
import com.rs.game.player.actions.divination.impl.DivineWoodcutting.DivineTreeDefinitions;
import com.rs.game.player.actions.firemaking.Bonfire;
import com.rs.game.player.actions.herblore.Herblore;
import com.rs.game.player.actions.hunter.TrapAction;
import com.rs.game.player.actions.invention.Manufacture;
import com.rs.game.player.actions.magic.ChargeAirOrb;
import com.rs.game.player.actions.magic.ChargeEarthOrb;
import com.rs.game.player.actions.magic.ChargeFireOrb;
import com.rs.game.player.actions.magic.ChargeWaterOrb;
import com.rs.game.player.actions.mining.EssenceMining;
import com.rs.game.player.actions.mining.Mining;
import com.rs.game.player.actions.mining.MiningBase;
import com.rs.game.player.actions.mining.defs.EssenceDefinitions;
import com.rs.game.player.actions.mining.defs.RockDefinitions;
import com.rs.game.player.actions.runecrafting.SiphonActionNodes;
import com.rs.game.player.actions.smithing.BowlSinging;
import com.rs.game.player.actions.smithing.BowlSinging.CrystalCreation;
import com.rs.game.player.actions.smithing.Smelting;
import com.rs.game.player.actions.smithing.Smithing;
import com.rs.game.player.actions.smithing.Smithing.ForgingInterface;
import com.rs.game.player.actions.smithing.defs.ForgingBar;
import com.rs.game.player.actions.summoning.Summoning;
import com.rs.game.player.actions.thieving.Thieving;
import com.rs.game.player.actions.thieving.def.Stalls;
import com.rs.game.player.actions.woodcutting.Woodcutting;
import com.rs.game.player.actions.woodcutting.WoodcuttingDefinitions.TreeDefinitions;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.content.*;
import com.rs.game.player.content.agility.*;
import com.rs.game.player.content.bank_highscores.RichestBanksD;
import com.rs.game.player.content.barrows.Barrows;
import com.rs.game.player.content.construction.House;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.crystaltriskellion.CrystalTriskelion;
import com.rs.game.player.content.dungeoneering.DungeonConstants;
import com.rs.game.player.content.dungeoneering.DungeonManager;
import com.rs.game.player.content.dungeoneering.DungeonPartyManager;
import com.rs.game.player.content.dungeoneering.rooms.puzzles.FishingFerretRoom;
import com.rs.game.player.content.ectofuntus.Ectofuntus;
import com.rs.game.player.content.eds.EliteDungeonPartyManager;
import com.rs.game.player.content.fistofguthix.FOGManager;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.homearea.HomeAreaHandler;
import com.rs.game.player.content.items.BarrowsAmulet;
import com.rs.game.player.content.packs.portable.PortableStation;
import com.rs.game.player.content.petperks.PetPerk;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.player.content.skillingcontracts.impl.ThievingContractList;
import com.rs.game.player.controllers.*;
import com.rs.game.player.controllers.bossInstance.VoragoInstanceController;
import com.rs.game.player.controllers.pestcontrol.PestControlLobby;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.dialogue.impl.*;
import com.rs.game.player.security.pin.AccountPin;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.io.InputStream;
import com.rs.utils.*;
import com.rs.utils.data.parsers.misc.ShopsDataParser;
import lombok.val;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.rs.game.player.content.Pots.Effects.SUPREME_OVERLOAD_SALVE;
import com.rs.utils.EconomyPrices.AlchTier;
/**
 * Handles all Objects within Edelar.
 *
 * @author Noel
 */
public final class ObjectHandler {


    /**
     * Handles the Object option clicked.
     *
     * @param player The player interacting.
     * @param stream The InputStream packet sent.
     * @param option The option clicked.
     */
    public static void handleOption(final Player player, final InputStream stream, final int option) {
        if (!player.clientHasLoadedMapRegion() || player.isDead()) {
            return;
        }
        if (player.isLocked() || player.getEmotesManager().getNextEmoteEnd() >= Utils.currentTimeMillis()) {
            return;
        }
        boolean forceRun = stream.readUnsignedByte() == 1;
        final int id = stream.readIntV1();
        int x = stream.readUnsignedShort();
        int y = stream.readUnsignedShort128();
        final WorldTile tile = new WorldTile(x, y, player.getPlane());
        final int regionId = tile.getRegionId();
        if (!player.getMapRegionsIds().contains(regionId)) {
            return;
        }
        WorldObject mapObject = World.getObjectWithId(tile, id);
        if (mapObject == null || mapObject.getId() != id) {
            final WorldObject playerObject = player.getPlayerObjectWithId(id, x, y, player.getPlane());
            if (playerObject != null) {
                mapObject = playerObject;
            } else {
                return;
            }
        }
        final WorldObject object = mapObject;
        dispatch(player, object, option, forceRun);
    }

    /**
     * Number of decoded object dispatches (legacy stream path and the native 947
     * router both pass through {@link #dispatch}); smokes read it to prove the
     * 910 handler ran rather than a bypass.
     */
    public static final java.util.concurrent.atomic.AtomicLong DISPATCHES = new java.util.concurrent.atomic.AtomicLong();

    /**
     * Decoded entry point: everything {@link #handleOption(Player, InputStream, int)}
     * does after it has read the wire fields and resolved the object. The legacy
     * stream path delegates here unchanged; the native 947 router
     * ({@code Native950ActionRouter}) resolves the {@link WorldObject} on the
     * player's plane itself and then calls this overload, so one object click
     * runs the same gates, {@code stopAll}, controller hooks and option switch
     * on both clients. The gates are re-applied here (they are idempotent) so a
     * caller that skipped the stream path still cannot act while dead, locked
     * or before the client has the map.
     *
     * @param player   the interacting player
     * @param object   the resolved object (already validated against the map)
     * @param option   1..5 for the option packets, -1 for examine
     * @param forceRun the client's run flag for this click
     */
    public static void dispatch(final Player player, final WorldObject object, final int option, final boolean forceRun) {
        if (!player.clientHasLoadedMapRegion() || player.isDead()) {
            return;
        }
        if (player.isLocked() || player.getEmotesManager().getNextEmoteEnd() >= Utils.currentTimeMillis()) {
            return;
        }
        DISPATCHES.incrementAndGet();
        if (option != -1) {
            player.stopAll();
            if (forceRun) {
                player.setRun(forceRun);
            }
            if (handleRunespanPortal(player, object)) {
                return;
            }
        }

        switch (option) {
            case 1:
                handleOption1(player, object);
                break;
            case 2:
                handleOption2(player, object);
                break;
            case 3:
                handleOption3(player, object);
                break;
            case 4:
                handleOption4(player, object);
                break;
            case 5:
                handleOption5(player, object);
                break;
            case -1:
                handleOptionExamine(player, object);
                break;
        }
    }


    // near the top of ObjectHandler (or above your altar switch)
    private static final int RC_DELAY_TICKS = 1; // tweak your per-batch delay here

    private static boolean handleRunespanPortal(final Player player, final WorldObject object) {
        final int id = object.getId();
        if (id != 79519 && id != 79520)
            return false;
        player.faceObject(object);
        if (id == 79519)
            RunespanController.enterRunespan(player);
        else
            RunespanController.enterMiddleRunespan(player);
        return true;
    }


// get alch price for objecthandler well of goodwill

    public static int getAlchPrice(Item item, boolean lowAlch) {
        int value = item.getDefinitions().getValue();
        double A = 1.0;
        double B = 0.15;
        double K = 294_118; // soft cap constant

        double percentDecay = A - B * Math.log10(Math.max(1, value));
        double percentSoftCap = K / Math.max(1, value);

        double percent = Math.max(percentSoftCap, percentDecay);

        double alchMultiplier = lowAlch ? 0.65 : 0.85;
        double alchValue = value * percent * alchMultiplier;

        return (int) Math.round(alchValue);
    }




    /**
     * Handles the Object's First option.
     *
     * @param player The player interacting.
     * @param object The object interacted with.
     */
    private static void handleOption1(final Player player, final WorldObject object) {
        final ObjectDefinitions objectDef = object.getDefinitions();
        final String objectName = objectDef.name.toLowerCase();
        final int id = object.getId();
        final int objectX = object.getX();
        final int objectY = object.getY();
        final int objectPlane = object.getPlane();

        final int playerX = player.getX();
        final int playerY = player.getY();

        if (player.isOwner() || Settings.DEBUG || Settings.TEST_SERVER_MODE) {
            player.getPackets().sendPanelBoxMessage("Option 1:" + object.getId() + "; " + object.getX() + " " + object.getY() + " " + object.getPlane());
        }

        if (player.isLocked()) {
            return;
        }
        if (!HomeAreaHandler.playerCanInteractWithObject(player, object)) {
            HomeAreaHandler.sendCanNotUseNpcOrObjectMessageToPlayer(player, object.getDefinitions().getName(), HomeAreaHandler.Type.OBJECT);
            return;
        }
        if (handleRunespanPortal(player, object)) {
            return;
        }
        if (object.getId() >= 91667 && object.getId() <= 91669) {// araxxor web
            player.stopAll();
            player.setRouteEvent(new RouteEvent(object, new Runnable() {
                @Override
                public void run() {
                    if (!player.getControlerManager().processObjectClick1(object))
                        return;
                }
            }, true));
            return;
        }
        if (id == 66904)
            BurthopeAgility.swingOnRopeSwing(player, object);
        if (object.getId() == 103559) {
            player.setRouteEvent(new RouteEvent(new WorldTile(3199, 6961, 1), new Runnable() {
                @Override
                public void run() {
                    player.setNextWorldTile(new WorldTile(3821, 7048, 0));
                }
            }));
            return;
        }

        if (object.getId() == 111439 || object.getId() == 111440) {
            boolean outside = player.getY() > object.getY();
            player.setRouteEvent(new RouteEvent(object.transform(object.getId() == 111439 ? 1 : 2, outside ? 5 : -5, 0), new Runnable() {
                @Override
                public void run() {
                    player.faceObject(object);
                    if (!player.getControlerManager().processObjectClick1(object))
                        return;
                }
            }));
            return;
        }
        if (object.getId() == 5999) {


            player.setRouteEvent(new RouteEvent(object, () -> {


                if (!player.getControlerManager().processObjectClick1(object))
                    return;
                EliteDungeonPartyManager.enterDungeon(player, 3);
                player.faceObject(object);

            }, true));
            return;
        }

        if (object.getId() == 113894) {
            player.setRouteEvent(new RouteEvent(object, () -> {

                FadingScreen.fade(player, 0, () -> {
                    player.setNextAnimation(new Animation(29010));
                    player.setNextWorldTile(new WorldTile(3094, 6122, 0));

                    player.getControlerManager().startControler("TrexController");
                });
            }, false));
            return;
        }


        if (object.getId() == 109776) {
            player.setRouteEvent(new RouteEvent(object, () -> {
                player.faceObject(object);

                // Optional: double-check distance
                if (!player.withinDistance(object, 4)) {
                    player.sendMessage("You need to be closer to use this.");
                    return;
                }

                player.getInventory().convertInventoryToGold(); // false = high alch
            }, true));
            return;
        }



        // process items inventory to gold



        if (object.getId() == 89309) {

            if (player.getSkills().getXp(Skills.THIEVING) < 38737661) {
                player.sendMessage("You need a Thieving level of 110 to thieve from this stall.");
                return;
            }
            if (player.getThievingDelay() > Utils.currentTimeMillis() || player.getActionManager().getActionDelay() != 0) {
                return;
            }
            final Item[] rewards = {new Item(15270), new Item(42311), new Item(30372), new Item(7937), new Item(12163), new Item(452), new Item(3139), new Item(3511), new Item(44803), new Item(43785),};
            final Item item6 = rewards[Utils.random(rewards.length)];
            if (item6.getDefinitions().isStackable()) {
                item6.setAmount(Utils.random(1, 14));
            }
            if (item6.getDefinitions().getId() == 452) {
                item6.setAmount(Utils.random(1, 4));
            }
            if (item6.getDefinitions().getId() == 3139) {
                item6.setAmount(Utils.random(3, 8));
            }
            if (item6.getDefinitions().getId() == 7937) {
                item6.setAmount(Utils.random(91, 188));
            }
            if (player.getEquipment().getCapeId() == 9778 || player.getEquipment().getCapeId() == 34597) {
                if (item6.getDefinitions().getId() == 7937 || item6.getDefinitions().getId() == 3139 || item6.getDefinitions().getId() == 452) {
                    player.getBank().addItem(new Item(item6), true);
                    player.sendMessage(Colors.ORANGE + "You Gained Extra Loot From Your Thieving Cape & Has Been Sent To Bank", true);
                }
            }
            if (player.getInventory().getFreeSlots() < 2) {
                player.sendMessage("You need atleast 2 spots free to thieve from this stall");
                return;
            }
            if (player.getInventory().addItem(item6)) {
                if (Utils.random(450) == 0) {
                    player.getInventory().addItem(6571, 1);
                    World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received an Uncut Onyx from Thieving!", false);
                    return;
                }
                if (Utils.random(250) == 0) {
                    player.getInventory().addItem(6199, 1);
                    World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a Mystery box from Thieving!", false);
                    return;
                }
                if (Utils.random(150) == 0) {
                    player.getInventory().addItem(36965, 1);
                    World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a Golden ticket from the loot safe!", false);
                    return;
                }
                if (Utils.random(350) == 0) {
                    player.getInventory().addItem(34023, 1);
                    World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Large Protean Pack from Thieving!", false);
                    return;
                }
                final int amount6 = player.getSkills().getLevelForXp(Skills.THIEVING) * 750;
                player.addMoney(Utils.random(1, amount6));
                player.addWalkSteps(5462, 2361);
                player.faceObject(object);
                player.setNextAnimation(new Animation(3692));
                player.setNextGraphics(new Graphics(3304));

                player.setThievingDelay(Utils.currentTimeMillis() + 2500);
                player.getContracts().recordAction(Skills.THIEVING, 12);
                player.getSkills().addXp(Skills.THIEVING, 350);
                player.addTimesStolen();
                if (player.getDailyManager().getTask() != null) {
                    if (object.getId() == player.getTaskItemId()) {
                        player.getDailyManager().processTask();
                    }
                }
                player.sendMessage("You've successfully stolen from this stall; times thieved: " + Colors.RED + Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.", true);
                if (Utils.random(25) == 0 && player.hasRandomEvent()) {
                    if (!player.followedByRandomEventNPC()) {
                        NPC npc = new ThievingRandomEvent(player, player);
                        if (npc.withinDistance(player, 14)) {
                            player.setCurrentRandomEventNPC(npc);
                            player.sendMessage("<col=ff0000>A Rogue appears out of nowhere.");
                        }
                    }
                }
            } else {
                player.sendMessage("You do not have enough inventory space to do this.", true);
            }
            return;

        }





















        if (object.getId() == 104736) {
                if (player.getSkills().getXp(Skills.THIEVING) < 63575937) {
                    player.sendMessage("You need a Thieving level of 115 to thieve from this stall.");
                    return;
                }
                if (player.getThievingDelay() > Utils.currentTimeMillis() || player.getActionManager().getActionDelay() != 0) {
                    return;
                }
                final Item[] rewards = {new Item(29324), new Item(9245), new Item(565), new Item(39922), new Item(37941), new Item(42311), new Item(44814), new Item(44815),
                        new Item(15271), new Item(214), new Item(1632), new Item(1616),};
                final Item item6 = rewards[Utils.random(rewards.length)];
                if (item6.getDefinitions().isStackable()) {
                    item6.setAmount(Utils.random(1, 45));
                }
                if (item6.getDefinitions().getId() == 15271) {
                    item6.setAmount(Utils.random(1, 22));
                }
                if (item6.getDefinitions().getId() == 214) {
                    item6.setAmount(Utils.random(1, 22));
                }
                if (item6.getDefinitions().getId() == 1632) {
                    item6.setAmount(Utils.random(5, 28));
                }
            if (item6.getDefinitions().getId() == 1616) {
                item6.setAmount(Utils.random(1, 11));
            }
                if (player.getInventory().getFreeSlots() < 2) {
                    player.sendMessage("You need atleast 2 spots free to thieve from this stall");
                    return;
                }
            player.setRouteEvent(new RouteEvent(object, () -> {
                player.faceObject(object);
                WorldTasksManager.schedule(new WorldTask() {
                    int loop;
                    public void run() {
                        if (loop == 0) {
                            if (player.getEquipment().getCapeId() == 9778 || player.getEquipment().getCapeId() == 34597) {
                                if (item6.getDefinitions().isNoted()) {
                                    player.getBank().addItem(new Item(item6), true);
                                    player.sendMessage(Colors.DCYAN + "You Gained Extra Loot From Your Thieving Cape & Has Been Sent To Bank", true);
                                }
                            }

                            player.setNextAnimation(new Animation(27720));
                            player.setThievingDelay(Utils.currentTimeMillis() + 4000);
                            loop++;
                        } else if (loop == 1) { //change to a lower number if the wait time is too long
                            //put your code here
                                if (player.getInventory().addItem(item6)) {
                                    if (Utils.random(450) == 0) {
                                        player.getInventory().addItem(6571, 1);
                                        World.sendWorldMessage(Colors.DCYAN + "<shad=000000><img=6>News: " + player.getDisplayName() + " received an Uncut Onyx from Thieving!", false);
                                        return;
                                    }
                                    if (Utils.random(250) == 0) {
                                        player.getInventory().addItem(6199, 1);
                                        World.sendWorldMessage(Colors.DCYAN + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a Mystery box from Thieving!", false);
                                        return;
                                    }
                                    if (Utils.random(150) == 0) {
                                        player.getInventory().addItem(36965, 1);
                                        World.sendWorldMessage(Colors.DCYAN + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a Golden ticket from the loot safe!", false);
                                        return;
                                    }
                                    if (Utils.random(350) == 0) {
                                        player.getInventory().addItem(34023, 1);
                                        World.sendWorldMessage(Colors.DCYAN + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Large Protean Pack from Thieving!", false);
                                        return;
                                    }
                                    final int amount6 = player.getSkills().getLevelForXp(Skills.THIEVING) * 800;
                                    player.addMoney(Utils.random(1, amount6));
                                    player.faceObject(object);
                                    player.setNextAnimation(new Animation(27721));
                                    player.getContracts().recordAction(Skills.THIEVING, 12);
                                    player.getSkills().addXp(Skills.THIEVING, 420);
                                    player.addTimesStolen();
                                if (player.getDailyManager().getTask() != null) {
                                    if (object.getId() == player.getTaskItemId()) {
                                        player.getDailyManager().processTask();
                                    }
                                }
                                player.sendMessage("You've successfully stolen from this stall; times thieved: " + Colors.RED + Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.", true);
                                if (Utils.random(20) == 0 && player.hasRandomEvent()) {
                                    if (!player.followedByRandomEventNPC()) {
                                        NPC npc = new ThievingRandomEvent(player, player);
                                        if (npc.withinDistance(player, 14)) {
                                            player.setCurrentRandomEventNPC(npc);
                                            player.sendMessage("<col=ff0000>A Rogue appears out of nowhere.");
                                        }
                                    }
                                }
                            } else {
                                player.sendMessage("You do not have enough inventory space to do this.", true);
                            }
                            loop++;
                        }
                    }
                }, 0, 20);
            }, true));
            return;
        }

        //furnace option 1

        // Furnace  Option 1 (Smelt)
        if (object.getId() == 113261) {
            player.setRouteEvent(new RouteEvent(object, () -> {
                player.faceObject(object);

                // Respect controllers just like your other handlers
                if (!player.getControlerManager().processObjectClick1(object))
                    return;

                 //ptional: enforce proximity like your gold converter
                 if (!player.withinDistance(object, 2)) {
                     player.sendMessage("You need to be closer to use this.");
                     return;
                 }

                Smelting.sendRs3SmeltingInterface(player, object);
            }, true));
            return;
        }



        if (object.getId() == 111940) {
            player.stopAll(true);
            player.setRouteEvent(new RouteEvent(object, () -> {
                if (!player.getControlerManager().processObjectClick1(object))
                    return;
                player.faceObject(object);
                player.setNextWorldTile(new WorldTile(3370, 3887, 0));
                player.sendMessage("You've Have Left Dragonkin Laboratory");
            }, false));
            return;
        }
        if (object.getId() == 111737) {


            player.setRouteEvent(new RouteEvent(object, () -> {


                if (!player.getControlerManager().processObjectClick1(object))
                    return;
                EliteDungeonPartyManager.enterDungeon(player, 2);
                player.faceObject(object);

            }, true));
            return;
        }

        if (object.getId() == 6150) {


            player.setRouteEvent(new RouteEvent(object, () -> {

                player.setNextWorldTile(new WorldTile(3511, 3693, 0));
                player.faceObject(object);
            }, true));
            return;
        }
        if (object.getId() == 2273) {
            player.stopAll(true);
            player.setRouteEvent(new RouteEvent(object, () -> {
                player.faceObject(object);
                player.setNextWorldTile(new WorldTile(3972, 5553, 0));
                player.sendMessage("You've arrived at old dungeoneering.");
            }, false));
            return;
        }
        if (object.getId() == 103520) {
            player.setRouteEvent(new RouteEvent(new WorldTile(object.getX() + 1, object.getY() + 3, object.getPlane()), () -> {
                player.stopAll();
                player.faceObject(object);
                if (!player.getControlerManager().processObjectClick1(object))
                    return;
                player.setNextWorldTile(new WorldTile(3199, 6961, 1));
            }));
            return;
        }
        if (object.getId() == 103568) {// Telos exit
            player.stopAll();
            player.faceObject(object);
            if (!player.getControlerManager().processObjectClick1(object))
                return;
            return;
        }
        if (id == 10536) { // Khazari stepping tone.
            if (!Agility.hasLevel(player, 74)) {
                return;
            }
            val steppingStoneTile = new WorldTile(2860, 2974, 0);
            val northTile = new WorldTile(2860, 2977, 0);
            val southTile = new WorldTile(2860, 2971, 0);
            if (playerX == northTile.getX() && playerY == northTile.getY()) {
                player.lock();
                player.sendMessage("You cross the stepping stone to the other side.");
                player.faceObject(object);
                player.setNextAnimation(new Animation(741));
                player.setNextForceMovement(new ForceMovement(steppingStoneTile, 2, ForceMovement.SOUTH));
                player.setNextWorldTile(steppingStoneTile);
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.setNextAnimation(new Animation(741));
                        player.setNextForceMovement(new ForceMovement(southTile, 2, ForceMovement.SOUTH));
                        player.unlock();
                        player.setNextWorldTile(southTile);
                    }
                }, 2);
                return;
            } else if (playerX == southTile.getX() && playerY == southTile.getY()) {
                player.lock();
                player.sendMessage("You cross the stepping stone to the other side.");
                player.faceObject(object);
                player.setNextAnimation(new Animation(741));
                player.setNextForceMovement(new ForceMovement(steppingStoneTile, 2, ForceMovement.NORTH));
                player.setNextWorldTile(steppingStoneTile);
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.setNextAnimation(new Animation(741));
                        player.setNextForceMovement(new ForceMovement(northTile, 2, ForceMovement.NORTH));
                        player.unlock();
                        player.setNextWorldTile(northTile);
                    }
                }, 2);
                return;
            }

        }
        if (id == 101305 && playerX == 3069 && playerY == 10252) {
            val tile = new WorldTile(3069, 10250, 0);
            player.lock();
            player.setNextAnimation(new Animation(839));
            player.setNextForceMovement(new ForceMovement(tile, 2, ForceMovement.SOUTH));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.unlock();
                    player.setNextWorldTile(tile);
                }
            }, 1);
            return;
        } else if (id == 101305 && playerX == 3069 && playerY == 10250) {
            val tile = new WorldTile(3069, 10252, 0);
            player.lock();
            player.setNextAnimation(new Animation(839));
            player.setNextForceMovement(new ForceMovement(tile, 2, ForceMovement.NORTH));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.unlock();
                    player.setNextWorldTile(tile);
                }
            }, 1);
            return;
        }
        if (id == 1816) {
            Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(2273, 4681, 0));
            return;
        }
        if (id == 43529) {
            player.addWalkSteps(2485, 3418);
            WorldTasksManager.schedule(new WorldTask() {

                int ticks = 0;

                @Override
                public void run() {
                    ticks++;
                    if (ticks == 1) {
                        player.faceObject(object);
                    } else if (ticks == 2) {
                        GnomeAgility.preSwing(player, object);
                    }
                }

            }, 0, 1);
            return;
        }
        if (SiphonActionNodes.siphion(player, object)) {
            return;
        }
        if (id == SerenityPostsHandler.SERENITYPOST_OBJECT_ID) {
            SerenityPostsHandler.jumpOnSereinityPost(player, object);
            return;
        }
        if (object.getId() == 20415) {
            player.stopAll(true);
            player.setRouteEvent(new RouteEvent(object, () -> {
                player.faceObject(object);
                if (player.getSkills().getLevel(Skills.DUNGEONEERING) >= 100) {
                    Magic.vineTeleport(player, new WorldTile(1168, 4615, 0));
                } else {
                    player.sendMessage("You need a Dungeoneering level of 100 or greater to use this resource dungeon.");
                }
            }));
            return;
        }
        if (object.getId() == 20416) {
            player.stopAll(true);
            player.setRouteEvent(new RouteEvent(object, () -> {
                player.faceObject(object);
                Magic.vineTeleport(player, new WorldTile(3434, 3531, 0));
            }));
            return;
        }
        if (object.getId() == 114193) {
            player.stopAll(true);
            player.setRouteEvent(new RouteEvent(object, () -> {

                     player.faceObject(object);
                     Magic.mineTeleport(player, new WorldTile(2904, 5203, 0));

            }));
            return;
        }
        if (id == 99373) {
            player.stopAll(true);
            player.setRouteEvent(new RouteEvent(object, () -> {
                player.addWalkSteps(2266, 3402, 2, false);
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.setNextFaceWorldTile(new WorldTile(2268, 3400, 1));
                        PrifddinasAgilityCourse.jumpToOutcrop(player, object);
                    }
                });
            }, false));
            return;
        }
        if (object.getX() == 5440 && object.getY() == 2359) {

            int spent = player.getMoneySpent();
            player.setRouteEvent(new RouteEvent(object, () -> {
                if (player.getX() == 5440 && player.getY() == 2358) {
                    List<String> options = new ArrayList<String>();
                    options.add("Fishing Guild Entrance");

                    player.getDialogueManager().startDialogue("OptionSelectionD", "Where do you want to go?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                        int page = selector.getPage();
                        switch (page) {
                            case 0:
                                selector.close();
                                switch (option) {
                                    case OptionSelectionD.OptionSelector.OPTION_1:
                                        if (spent <= 5) {

                                            player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You Need A Donation Rank of 5 or More To Pass This Door", true);


                                            return;
                                        }
                                        // FadingScreen.fade(player, 0, () -> {
                                        Magic.mineTeleport(player, new WorldTile(5440, 2360, 0));

                                        //  });
                                        break;

                                }
                                return;
                        }

                    });
                }
                else if (player.getX() == 5440 && player.getY() == 2360)
                {
                    List<String> options = new ArrayList<String>();
                    options.add("Home Teleport");

                    player.getDialogueManager().startDialogue("OptionSelectionD", "Where do you want to go?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                        int page = selector.getPage();
                        switch (page) {
                            case 0:
                                selector.close();
                                switch (option) {
                                    case OptionSelectionD.OptionSelector.OPTION_1:

                                        FadingScreen.fade(player, 0, () -> {
                                            player.setNextWorldTile(new WorldTile(5424, 2339, 0));


                                        });
                                        break;

                                }
                                return;
                        }

                    });
                }
            }, false));



            return;
        }
        if (object.getX() == 5426 && object.getY() == 2358) {

            int spent = player.getMoneySpent();
            player.setRouteEvent(new RouteEvent(object, () -> {
                if (player.getX() == 5426 && player.getY() == 2357) {
                    List<String> options = new ArrayList<String>();
                    options.add("Woodcutting Guild Entrance");

                    player.getDialogueManager().startDialogue("OptionSelectionD", "Where do you want to go?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                        int page = selector.getPage();
                        switch (page) {
                            case 0:
                                selector.close();
                                switch (option) {
                                    case OptionSelectionD.OptionSelector.OPTION_1:
                                        if (spent <= 5) {

                                            player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You Need A Donation Rank of 5 or More To Pass This Door", true);


                                            return;
                                        }
                                        // FadingScreen.fade(player, 0, () -> {
                                        Magic.mineTeleport(player, new WorldTile(5426, 2359, 0));

                                        //  });
                                        break;

                                }
                                return;
                        }

                    });
                }
                else if (player.getX() == 5426 && player.getY() == 2359)
                {
                    List<String> options = new ArrayList<String>();
                    options.add("Home Teleport");

                    player.getDialogueManager().startDialogue("OptionSelectionD", "Where do you want to go?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                        int page = selector.getPage();
                        switch (page) {
                            case 0:
                                selector.close();
                                switch (option) {
                                    case OptionSelectionD.OptionSelector.OPTION_1:

                                        FadingScreen.fade(player, 0, () -> {
                                            player.setNextWorldTile(new WorldTile(5424, 2339, 0));


                                        });
                                        break;

                                }
                                return;
                        }

                    });
                }
            }, false));



            return;
        }
        if (object.getX() == 5456 && object.getY() == 2353) {

            int spent = player.getMoneySpent();
            player.setRouteEvent(new RouteEvent(object, () -> {
                if (player.getX() == 5455) {
                    List<String> options = new ArrayList<String>();
                    options.add("Thieving Guild Entrance");

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
                                        // FadingScreen.fade(player, 0, () -> {
                                        Magic.mineTeleport(player, new WorldTile(5457, 2353, 0));

                                        //  });
                                        break;

                                }
                                return;
                        }

                    });
                }
                else  if (player.getX() == 5457)
                {
                    List<String> options = new ArrayList<String>();
                    options.add("Home Teleport");

                    player.getDialogueManager().startDialogue("OptionSelectionD", "Where do you want to go?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                        int page = selector.getPage();
                        switch (page) {
                            case 0:
                                selector.close();
                                switch (option) {
                                    case OptionSelectionD.OptionSelector.OPTION_1:

                                        FadingScreen.fade(player, 0, () -> {
                                            player.setNextWorldTile(new WorldTile(5424, 2339, 0));


                                        });
                                        break;

                                }
                                return;
                        }

                    });
                }
            }, false));



            return;
        }
        if (object.getX() == 5438 && object.getY() == 2342) {
            int spent = player.getMoneySpent();

            if (spent <= 25) {

                player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You Need A Donation Rank of 25 or more to use this Portable Fletcher", true);


                return;
            }
        }
        if (object.getX() == 5439 && object.getY() == 2347) {
            int spent = player.getMoneySpent();

            if (spent <= 15) {

                player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You Need A Donation Rank of 15 or more to use this Portable Well", true);


                return;
            }
        }
        if (object.getX() == 5435 && object.getY() == 2345) {
            int spent = player.getMoneySpent();

            if (spent <= 6) {

                player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You Need A Donation Rank of 6 or more to use this Supply Table", true);


                return;
            }
        }
        if (object.getX() == 5428 && object.getY() == 2345) {
            int spent = player.getMoneySpent();

            if (spent <= 10) {

                player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You Need A Donation Rank of 10 or more to use this Resource Chest", true);


                return;
            }
        }
        if (object.getX() == 5464 && object.getY() == 2335) {
            int spent = player.getMoneySpent();

            if (spent <= 2) {

                player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You Need A Donation Rank of 2 or more to use this Harp", true);


                return;
            }
        }
        if (object.getX() == 5471 && object.getY() == 2339) {
            int spent = player.getMoneySpent();

            if (spent <= 10) {

                player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You Need A Donation Rank of 10 or more to use this Yu'biusk Portal", true);


                return;
            }
        }
        if (object.getX() == 5439 && object.getY() == 2339) {
            int spent = player.getMoneySpent();

            if (spent <= 20) {

                player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You Need A Donation Rank of 20 or more to use this Portable Crafter", true);


                return;
            }
        }
        if (object.getX() == 5438 && object.getY() == 2337) {
            int spent = player.getMoneySpent();

            if (spent <= 35) {

                player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You Need A Donation Rank of 35 or more to use Portable Forge", true);


                return;
            }
        }
        if (object.getX() == 5433 && object.getY() == 2363) {

            if (player.getSkills().getXp(Skills.WOODCUTTING) < 104273167) {
                player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You need a level of 120 WOODCUTTING in order to USE This BANK." , true);


                return;
            }
        }
        if (object.getX() == 5444 && object.getY() == 2360) {

            if (player.getSkills().getXp(Skills.FISHING) < 104273167) {
                player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You need a level of 120 FISHING in order to USE This BANK." , true);


                return;
            }
        }
        if (object.getX() == 5465 && object.getY() == 2360) {
            if (player.getSkills().getXp(Skills.THIEVING) < 104273167) {
                player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You need a level of 120 THIEVING in order to USE This BANK." , true);


                return;
            }
        }

        if (object.getX() == 5462 && object.getY() == 2320) {
            if (player.getSkills().getXp(Skills.MINING) < 104273167) {
                        player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You need a level of 120 Mining in order to USE This BANK." , true);


                        return;
                    }
        }
                if (object.getX() == 5458 && object.getY() == 2319) {

                    int spent = player.getMoneySpent();
                    player.setRouteEvent(new RouteEvent(object, () -> {
                        if (player.getX() == 5457) {
                            List<String> options = new ArrayList<String>();
                            options.add("Mining Guild Entrance");

                            player.getDialogueManager().startDialogue("OptionSelectionD", "Where do you want to go?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                                int page = selector.getPage();
                                switch (page) {
                                    case 0:
                                        selector.close();
                                        switch (option) {
                                            case OptionSelectionD.OptionSelector.OPTION_1:
                                                if (spent <= 5){

                                                    player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You Need A Donation Rank of 5 or More To Pass This Door" , true);


                                                    return;
                                                }
                                                // FadingScreen.fade(player, 0, () -> {
                                                Magic.mineTeleport(player, new WorldTile(5459, 2319, 0));

                                                //  });
                                                break;

                                        }
                                        return;
                                }

                            });
                        }
                        else  if (player.getX() == 5459)
                        {
                            List<String> options = new ArrayList<String>();
                            options.add("Home Teleport");

                            player.getDialogueManager().startDialogue("OptionSelectionD", "Where do you want to go?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                                int page = selector.getPage();
                                switch (page) {
                                    case 0:
                                        selector.close();
                                        switch (option) {
                                            case OptionSelectionD.OptionSelector.OPTION_1:

                                                FadingScreen.fade(player, 0, () -> {
                                                    player.setNextWorldTile(new WorldTile(5424, 2339, 0));


                                                 });
                                                break;

                                        }
                                        return;
                                }

                            });
                        }
                    }, false));



                    return;
                }










        if (id == 6945 && !player.isUnderCombat()) { // BOAT CROSS FOR ED3
            player.setRouteEvent(new RouteEvent(object, () -> {
                player.faceObject(object);
                WorldTasksManager.schedule(new WorldTask() {
                    int loop;
                    public void run() {
                        if (loop == 0) {
                            player.setNextAnimation(new Animation(29009));
                            loop++;

                        } else if (loop == 1) { //change to a lower number if the wait time is too long
                            //put your code here
                            player.setNextAnimation(new Animation(29010));
                            player.setNextWorldTile(new WorldTile(5491, 9066, 2));
                            loop++;
                        }
                    }
                }, 0, 1);
            }, true));
            return;
        }
        if (id == 10230) { // Dag King stairs down
            player.stopAll(true);
            player.setRouteEvent(new RouteEvent(new WorldTile(1912, 4367, 0), () -> {
                player.faceObject(object);
                player.getDialogueManager().startDialogue("DagannothKingsInstanceD", 2880);
            }, false));
            return;
        }
        if (id == 88063) {
            player.stopAll(true);
            WorldTile tile = null;
            if (object.getRotation() == 3 || object.getRotation() == 1) {
                tile = new WorldTile(object.getX() + 2, player.getY() < object.getY() ? (object.getY() - 2) : (object.getY() + 2), object.getPlane());
            } else {
                tile = new WorldTile(player.getX() < object.getX() ? (object.getX() - 2) : (object.getX() + 2), object.getY() + 2, object.getPlane());
            }
            player.setRouteEvent(new RouteEvent(player.getDistance(object) < player.getDistance(tile) ? object : tile, () -> {
                if (player.getControlerManager().getControler() != null) {
                    if (player.getControlerManager().getControler() instanceof RiseOfTheSixController) {
                        final RiseOfTheSixController rots = (RiseOfTheSixController) player.getControlerManager().getControler();
                        final RiseOfTheSix instance = rots.getInstance();
                        if (!instance.canUseObstacle(player, object)) {
                            player.sendMessage("Someone else is already using this obstacle.");
                            return;
                        }
                        instance.addObstacle(Utils.currentTimeMillis() + 5000, object);
                    }
                }
                WorldTile toTile = null;
                player.lockROTS();
                player.setNextAnimation(new Animation(751));
                switch (object.getRotation()) {
                    case 0:
                    case 2:
                        toTile = new WorldTile(player.getX() > object.getX() ? (object.getX() - 2) : (object.getX() + 2), object.getY() + 2, object.getPlane());
                        break;
                    default:
                        toTile = new WorldTile(object.getX() + 2, player.getY() > object.getY() ? (object.getY() - 2) : (object.getY() + 2), object.getPlane());
                        break;
                }
                final WorldTile tile1 = toTile;
                player.setNextForceMovement(new NewForceMovement(player, 0, toTile, 3, Utils.getFaceDirection(toTile.getX() - player.getX(), toTile.getY() - player.getY())));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        World.removeObject(object);
                        player.setNextWorldTile(tile1);
                        player.unlockROTS();
                    }
                }, 1);
            }, false));
            return;
        }

        if (id == 88061) {
            player.stopAll(true);
            WorldTile tile = null;
            if (object.getRotation() == 3 || object.getRotation() == 1) {
                tile = new WorldTile(object.getX(), player.getY() < object.getY() ? (object.getY() - 1) : (object.getY() + 3), object.getPlane());
            } else {
                tile = new WorldTile(player.getX() < object.getX() ? (object.getX() - 1) : (object.getX() + 3), object.getY(), object.getPlane());
            }
            player.setRouteEvent(new RouteEvent(player.getDistance(object) < player.getDistance(tile) ? (object.getRotation() == 0 || object.getRotation() == 2 ? new WorldTile(object.getRotation() == 0 ? (object.getX() - 1) : (object.getX() + 1), object.getY(), object.getPlane()) : new WorldTile(object.getX(), object.getRotation() == 1 ? (object.getY() - 1) : (object.getY() + 1), object.getPlane())) : tile, () -> {
                if (player.getControlerManager().getControler() != null) {
                    if (player.getControlerManager().getControler() instanceof RiseOfTheSixController) {
                        final RiseOfTheSixController rots = (RiseOfTheSixController) player.getControlerManager().getControler();
                        final RiseOfTheSix instance = rots.getInstance();
                        if (!instance.canUseObstacle(player, object)) {
                            player.sendMessage("Someone else is already using this obstacle.");
                            return;
                        }
                        instance.addObstacle(Utils.currentTimeMillis() + 2000, object);
                    }
                }
                int x1, y1, face;
                player.lockROTS();
                player.setNextAnimation(new Animation(9908));
                x1 = (object.getRotation() == 0 || object.getRotation() == 2) ? (player.getX() > object.getX() ? object.getX() - 1 : object.getX() + 3) : object.getX();
                y1 = (object.getRotation() == 1 || object.getRotation() == 3) ? (player.getY() > object.getY() ? object.getY() - 1 : object.getY() + 3) : object.getY();
                face = (object.getRotation() == 0 || object.getRotation() == 2) ? player.getX() > object.getX() ? ForceMovement.WEST : ForceMovement.EAST : player.getY() > object.getY() ? ForceMovement.SOUTH : ForceMovement.NORTH;
                final WorldTile toTile = new WorldTile(x1, y1, object.getPlane());
                player.setNextForceMovement(new ForceMovement(player, 0, toTile, 4, face));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.setNextAnimation(new Animation(-1));
                        player.setNextWorldTile(toTile);
                        player.unlockROTS();
                    }
                }, 3);
            }, false));
            return;
        }
        if (id == 88065) {
            player.stopAll(true);
            WorldTile tile = null;
            final int rotation = object.getRotation();
            if (rotation == 3 || rotation == 1) {
                tile = new WorldTile(object.getX() + 1, player.getY() < object.getY() ? (object.getY() - 2) : (object.getY() + 2), object.getPlane());
            } else {
                tile = new WorldTile(player.getX() < object.getX() ? (object.getX() - 2) : (object.getX() + 2), object.getY() + 1, object.getPlane());
            }
            player.setRouteEvent(new RouteEvent(player.getDistance(object) < player.getDistance(tile) ? (object.getRotation() == 0 || object.getRotation() == 2 ? new WorldTile(object.getX() - 1, object.getY(), object.getPlane()) : new WorldTile(object.getX() + 1, object.getY(), object.getPlane())) : tile, () -> {
                if (player.getControlerManager().getControler() != null) {
                    if (player.getControlerManager().getControler() instanceof RiseOfTheSixController) {
                        final RiseOfTheSixController rots = (RiseOfTheSixController) player.getControlerManager().getControler();
                        final RiseOfTheSix instance = rots.getInstance();
                        if (!instance.canUseObstacle(player, object)) {
                            player.sendMessage("Someone else is already using this obstacle.");
                            return;
                        }
                        instance.addObstacle(Utils.currentTimeMillis() + 5000, object);
                    }
                }
                final int destX, destY;
                player.lockROTS();
                player.setNextAnimation(new Animation(741));
                destX = (object.getRotation() == 1 || object.getRotation() == 3) ? object.getX() + 1 : object.getX();
                destY = (object.getRotation() == 0 || object.getRotation() == 2) ? object.getY() + 1 : object.getY();
                final WorldTile toTile = new WorldTile(destX, destY, object.getPlane());
                final int tileOffsetX = destX - player.getX();
                final int tileOffsetY = destY - player.getY();
                player.setNextForceMovement(new NewForceMovement(player, 0, toTile, 1, Utils.getFaceDirection(toTile.getX() - player.getX(), toTile.getY() - player.getY())));
                WorldTasksManager.schedule(new WorldTask() {
                    private int ticks;
                    private WorldTile tile;

                    @Override
                    public void run() {
                        if (ticks == 0) {
                            player.setNextWorldTile(toTile);
                        } else if (ticks == 1) {
                            player.setNextAnimation(new Animation(741));
                            tile = new WorldTile(toTile.getX() + tileOffsetX, toTile.getY() + tileOffsetY, player.getPlane());
                            player.setNextForceMovement(new NewForceMovement(player, 0, tile, 1, Utils.getFaceDirection(tile.getX() - player.getX(), tile.getY() - player.getY())));
                        } else if (ticks == 2) {
                            World.removeObject(object);
                            player.setNextAnimation(new Animation(-1));
                            player.setNextWorldTile(tile);
                            player.unlockROTS();
                            stop();
                        }
                        ticks++;
                    }
                }, 0, 0);
            }, false));
            return;
        }
        if (AgilityManager.handleShortcut(player, object)) {
            return;
        }
        if (object.getId() == 75463) { // Armadyl follower room
            if (!(player.getControlerManager().getControler() instanceof GodWars)) {
                return;
            }
            player.setRouteEvent(new RouteEvent(player.getY() > 5278 ? new WorldTile(2872, 5280, 0) : new WorldTile(2872, 5272, 0), () -> {
                player.lock(1);
                if (player.getY() >= 5280) {
                    if (player.getSkills().getLevelForXp(Skills.RANGE) < 70) {
                        player.sendMessage("You need a level of 70 Ranged in order to do this.");
                        return;
                    }
                    player.useStairs(-1, new WorldTile(2872, 5272, 0), 0, 0);
                    return;
                }
                player.useStairs(-1, new WorldTile(2872, 5280, 0), 0, 0);
            }, false));
            return;
        }
        if (object.getId() == 104453) {
            player.stopAll();
            player.setRouteEvent(new RouteEvent(object, () -> {
                player.faceObject(object);
                player.setNextWorldTile(new WorldTile(3044, 3923, 0));
            }, false));
        }
        if (object.getId() == 45802) {
            player.stopAll(true);
            player.setRouteEvent(new RouteEvent(new WorldTile(414, 674, 0), () -> {
                player.faceObject(object);
                player.getDialogueManager().startDialogue("ContractDialogue");
            }, false));
            return;
        }
        if (DiamondZonePortalHandler.processObjectClick(player, object)) {
            return;
        }
        //burthope agility
        else if (id == 66894)
            BurthopeAgility.walkLog(player);
        else if (id == 66912)
            BurthopeAgility.climbWall(player);
        else if (id == 66909)
            BurthopeAgility.walkAcrossBalancingLedge(player, object);
        else if (id == 66902)
            BurthopeAgility.climbOverObstacleWall(player, object);
        else if (id == 66897)
            BurthopeAgility.swingAcrossMonkeyBars(player, object);
        else if (id == 66910)
            BurthopeAgility.jumpDownLedge(player, object);
        if (id == 22545) {
            if (!player.getInventory().containsItem(10952, 1)) {
                player.sendMessage("You need a Slayer bell to lure Molanisks.");
                return;
            }
            if (player.getSkills().getLevel(Skills.SLAYER) < 39) {
                player.sendMessage("You need a Slayer level of at least 39 to ring the bell.");
                return;
            }
            player.lock(3);
            player.faceObject(object);
            player.setNextAnimation(new Animation(6083));
            player.sendMessage("The bell re-sounds loudly throughout the cavern.", true);
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    final NPC npc = World.spawnNPC(5751, object, -1, true);
                    npc.getCombat().setTarget(player);
                    final WorldObject o = new WorldObject(object);
                    o.setId(22544);
                    World.spawnTemporaryObject(o, 30000);
                }
            }, 1);
            return;
        }

        if (id == 101766) {
            player.setRouteEvent(new RouteEvent(object, () -> {
                player.lock(5);
                final WorldObject lift = new WorldObject(101865, 10, 3, new WorldTile(3198, 6935, 1));
                FadingScreen.fade(player, 0, () -> {
                    player.setNextWorldTile(new WorldTile(3198, 6940, 1));
                    player.setNextAnimation(new Animation(26446));
                    player.getControlerManager().startControler("GodWars2");
                    player.faceObject(lift);
                });
                return;
            }, true));
            return;
        } else if (object.getId() == 84960) {// Exit sphere
            final VoragoInstanceController controler = (VoragoInstanceController) player.getControlerManager().getControler();
            if (controler == null) {
                return;
            }
            player.faceObject(object);
            player.getDialogueManager().startDialogue(new Dialogue() {
                @Override
                public void start() {
                    sendOptionsDialogue("ARE YOU READY TO LEAVE?", "Leave.", "Stay here.");
                }

                @Override
                public void run(final int interfaceId, final int componentId) {
                    if (componentId == OPTION_1) {
                        controler.resetAttributesOnLeaveBattle();
                        controler.getVoragoInstance().handleExitSphereClick(player);
                    }
                    end();
                }

                @Override
                public void finish() {
                }
            });
            return;
        }
        player.setRouteEvent(new RouteEvent(object, () -> {
            player.stopAll();
            player.faceObject(object);
            if (!player.getControlerManager().processObjectClick1(object)) {
                return;
            }
            if (CastleWars.handleObjects(player, id)) {
                return;
            }
            if (Ectofuntus.manageObjects(player, object.getId())) {
                return;
            }
            if(player.vineHerbPatches.handleOption1(object)) {
                return;
            }
            if(player.harmonyPillars.handleOption1(object)) {
                return;
            }
            if (ResourceDungeons.handleObjects(player, id)) {
                return;
            }
            if (PrifddinasCity.handleObjectOption1(player, object)) {
                return;
            }
            if(object.getId() == 48675) {
                player.getDialogueManager().startDialogue(new RichestBanksD());
                return;
            }
            if (object.getId() == 26286 || object.getId() == 26289 || object.getId() == 26288 || object.getId() == 26287) {
                player.lock(1);
                if (player.getAttackedByDelay() + 15000 > Utils.currentTimeMillis()) {
                    player.sendMessage("You can't use this altar until 15 seconds after the end of combat.", true);
                    return;
                }
                final int maxPrayer = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
                if (player.getPrayer().getPrayerpoints() < maxPrayer) {
                    player.lock(1);
                    player.sendMessage("The gods have recharged your prayer points.", true);
                    player.setNextAnimation(new Animation(645));
                    player.getPrayer().restorePrayer(maxPrayer);
                } else
                    player.sendMessage("You already have full prayer points.", true);
                return;
            }
            if (id == 82049) {
                if (player.isKalphiteLairEntranceSetted())
                    player.setNextWorldTile(new WorldTile(3420, 9510, 0));
                else if (player.getInventory().containsItem(954, 1)) {
                    player.getInventory().deleteItem(954, 1);
                    player.setKalphiteLairEntrance();
                } else
                    player.getPackets().sendGameMessage("You need a rope to climb down.");
                return;
            }
            if (id == 95001) {
                ChristmasSeasonalEvent.findLargePresent(player, object);
                return;
            }
            if (id == 88923) {
                player.getDialogueManager().startDialogue(new SantaListDialogue());
                return;
            }
            if (id == 100873) {
                player.getInventionManager().openDiscoveryInterface();
                return;
            }
            if (id == 66006) {
                ChristmasSeasonalEvent.inspectSnowman(player);
                return;
            }
            if (id == 95024) {
                ChristmasSeasonalEvent.lookUnderTree(player);
                return;
            }
            if (id == 100874) {
                int[] skillIds = {Skills.CRAFTING, Skills.DIVINATION, Skills.SMITHING};
                for (int skillId : skillIds) {
                    if (player.getSkills().getLevelForXp(skillId) < 80) {
                        player.getPackets().sendGameMessage(
                                "You need a " + Skills.SKILL_NAME[skillId] + " level of " + 80 + " to do that.");
                        return;
                    }
                }
                Manufacture.openManufactureDialogue(player);
                return;
            }
            if (id == 17223) {
                FadingScreen.fade(player, 600, () -> player.setNextWorldTile(new WorldTile(3005, 3203, 0)));
                return;
            }
            if (id == 3192) {
                GIM.getHighscores().displayLeaderboard(player);
                return;
            }
            if (id == 1118) {
                if (player.quests.getCurrentStage(RootOfEvil.class) == 7) {
                    player.getControlerManager().startControler(new VaultController());
                    return;
                } else if (player.quests.isCompleted(RootOfEvil.class)) {
                    FadingScreen.fade(player, 600, () -> {

                    });
                } else {
                    player.sendMessage("Nothing to see here. Just a bush.");
                }
                return;
            }
            if (id == 64362) {
                player.useStairs(828, new WorldTile(4653, 5389, 1), 1, 2);
                return;
            }

            if (id == 9140) {
                GrandExchange.viewOffersFor(player);
                return;
            }
            if (object.getId() == 109322) {
                TheMagisterInstance.startInstance(player);
                return;
            }
            if (id == 54408) {
                player.gimBank.open();
                return;
            }
            if (object.getId() == 16082) {
                player.getDialogueManager().startDialogue(new DungeonArchitectDialogue());
                return;
            }
            if (object instanceof EvilRootObject) {
                val root = (EvilRootObject) object;
                root.chop(player);
                return;
            } else if (object instanceof EvilTreeObject) {
                val tree = (EvilTreeObject) object;
                if (tree.isDead()) {
                    tree.giveRewards(player);
                } else {
                    tree.chop(player);
                }
                return;
            } else if (object instanceof EvilSaplingObject) {
                val sapling = (EvilSaplingObject) object;
                sapling.nurture(player);
                return;
            } else if (object instanceof EvilWeedsObject) {
                val patch = (EvilWeedsObject) object;
                if (patch.isAshes()) {
                    patch.takeDust(player);
                } else {
                    patch.rake(player);
                }
                return;
            }
            if (id == 109364 || id == 109350) {
                if (player.getY() == 2812 || player.getX() == 2813) {
                    player.setNextWorldTile(new WorldTile(player.getX(), 2819, 0));
                }
                if (player.getY() == 2819 || player.getY() == 2820) {
                    player.setNextWorldTile(new WorldTile(player.getX(), 2812, 0));
                }
                return;
            }
            if (id == 103521) {
                if (player.getCurrentTelosReward() != null) {
                    player.getPackets().sendGameMessage("You have an unclaimed reward please claim it, or select continue challenge from font.");
                    return;
                }
                BossInstanceHandler.enterInstance(player, Boss.Telos);
                return;
            }







            if (object.getId() == 103565) {
                if (player.getCurrentTelosReward() == null) {
                    player.getPackets().sendGameMessage("You don't have any new rewards to claim.");
                    return;
                }
                player.openTelosTrove();
                return;
            }
            if (id == 69197 || id == 69198) {
                if (player.getY() == 3492) {
                    player.lock();
                    FadingScreen.fade(player, 0, () -> {
                        player.setNextWorldTile(new WorldTile(2466, 3491, 0));
                        player.unlock();
                    });
                } else if (player.getY() == 3491) {
                    player.lock();
                    FadingScreen.fade(player, 0, () -> {
                        player.setNextWorldTile(new WorldTile(2466, 3493, 0));
                        player.unlock();
                    });
                }
                return;
            } else if (id == 2647 && objectX == 2933 && objectY == 3289 && objectPlane == 0) {
                if (player.getY() == 3288) {
                    player.lock();
                    FadingScreen.fade(player, 0, () -> {
                        player.setNextWorldTile(new WorldTile(2933, 3289, 0));
                        player.unlock();
                    });
                } else if (player.getY() == 3289) {
                    player.lock();
                    FadingScreen.fade(player, 0, () -> {
                        player.setNextWorldTile(new WorldTile(2933, 3288, 0));
                        player.unlock();
                    });
                }
            }
            if (id == 7138) {
                player.setNextWorldTile(new WorldTile(2851, 3809, 2));
                return;
            }

            if (id == 2332) {
                if (player.getX() == 2906) {
                    final boolean running = player.getRun();
                    player.setRunHidden(false);
                    WorldTile to = new WorldTile(2910, 3049, 0);
                    player.lock();
                    player.sendMessage("You start to walk across the log...");
                    player.addWalkSteps(to.getX(), to.getY(), -1, false);
                    WorldTasksManager.schedule(new WorldTask() {
                        boolean secondLoop;

                        @Override
                        public void run() {
                            if (!secondLoop) {
                                secondLoop = true;
                                player.getAppearence().setRenderEmote(155);
                            } else {
                                player.getAppearence().setRenderEmote(-1);
                                player.setRunHidden(running);
                                player.sendMessage("... and make it safely to the other side.", true);
                                player.unlock();
                                stop();
                            }
                        }
                    }, 0, 2);
                } else if (player.getX() == 2910) {
                    final boolean running = player.getRun();
                    player.setRunHidden(false);
                    final WorldTile to = new WorldTile(2906, 3049, 0);

                    player.lock();
                    player.sendMessage("You start to walk across the log...");
                    player.addWalkSteps(to.getX(), to.getY(), -1, false);
                    WorldTasksManager.schedule(new WorldTask() {
                        boolean secondLoop;

                        @Override
                        public void run() {
                            if (!secondLoop) {
                                secondLoop = true;
                                player.getAppearence().setRenderEmote(155);
                            } else {
                                player.setRunHidden(running);
                                player.getAppearence().setRenderEmote(-1);
                                player.sendMessage("... and make it safely to the other side.", true);
                                player.unlock();
                                stop();
                            }
                        }
                    }, 0, 2);
                }
            }
            if (id == 62426) {
                player.getDialogueManager().startDialogue("SimpleMessage", "Rest in Peace Happy Nut - 12th January, 1996 - 21st July, 2016");
                return;
            }

            if (id == 90986) {
                PZInstance.open(player);
                return;
            }

            if (id == 89158) {
                player.getDialogueManager().startDialogue(GiantMoleInstanceD.class.getSimpleName(), 18932);
                return;
            }

            if (id == 87306) {
                player.getActionManager().setAction(new DivinationConvert(player, new Object[]{player.getConvertMode()}));
                player.closeInterfaces();
                return;
            }

            if (id >= 10851 && id <= 10888) {
                AgilityPyramid.handlePyramidObjects(player, object);
                return;
            }

            if (id == 87998) {
                FadingScreen.fade(player, 6, () -> {
                    player.unlock();
                    player.setNextWorldTile(new WorldTile(3540, 3311, 0));
                    player.setNextFaceWorldTile(new WorldTile(3541, 3311, 0));
                    player.setNextAnimation(new Animation(21922));
                });
                return;
            }

            /* Start of the Fist of Guthix object handling */
            if (id == 30224) {
                FOGManager.get().getFOGInstance().enterPassageway(player);
                return;
            }
            if (id == 34548) {// TODO
                final int rot1 = object.getRotation();
                player.useStairs(-1, new WorldTile(object.getX() + (rot1 == 3 ? 1 : rot1 == 2 ? -1 : 0), object.getY() + (rot1 == 3 ? 2 : rot1 == 2 ? 1 : 0), player.getPlane() + 1), 0, 1);
                return;
            }
            if (object.getId() == 11005) {
                if (object.getX() == 1374 && object.getY() == 5552) {

                    if (player.getX() <= 1374) {
                        if (player.getSkills().getLevel(Skills.SLAYER) < 104) {
                            player.getPackets().sendPlayerMessage(1, 15263739, "<col=0867af>" + "You need a Slayer level of 104 to Pass This Magic Door." , true);

                            return;
                        }
                        player.getPackets().sendPlayerMessage(1, 15263739, "<col=0867af>" + "Welcome To Lost Grove: " , true);
                            player.dungKills = 0;
                            player.inDungeoneering = true;
                            player.addWalkSteps(1374, 5554, 2, false);
                            player.getControlerManager().startControler("SolakController");
                            player.lock(1);
                        }

                    }
                if (object.getX() == 3978 && object.getY() == 5552) {
                    if (player.getX() <= 3977) {
                        player.dungKills = 0;
                        player.inDungeoneering = true;
                        player.addWalkSteps(3979, 5552, 2, false);
                        player.getControlerManager().startControler("Dungeoneering");
                        player.lock(1);
                    }
                }

                return;
            }
            if (object.getId() == 57264) {
                player.getDialogueManager().startDialogue("SimpleMessage", "Vita brevis breviter in brevi finietur,<br>" + "Mors venit velociter quae neminem veretur,<br>" + "Omnia mors permit et nulli miseretur.");
                return;
            } else if (object.getId() == 100801 && Settings.AOD_ENABLED) {
                player.getDialogueManager().startDialogue("AoDInstanceD");
                return;
            }

            if (object.getId() == 106643) {
                if (player.getSkills().getLevel(Skills.AGILITY) < 94) {
                    player.sendMessage("You need an Agility level of at least 94 to use this shortcut.");
                    return;
                }
                player.setNextAnimation(new Animation(10733));
                player.lock();
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        final long time = FadingScreen.fade(player, 1000);
                        FadingScreen.unfade(player, time, 500, () -> {
                            player.setNextWorldTile(new WorldTile(3007, 9009, 0));
                            player.unlock();
                        });
                    }

                });

                return;
            } else if (object.getId() == 106644) {
                if (player.getSkills().getLevel(Skills.AGILITY) < 94) {
                    player.sendMessage("You need an Agility level of at least 94 to use this shortcut.");
                    return;
                }
                player.setNextAnimation(new Animation(10733));
                player.lock();
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        final long time = FadingScreen.fade(player, 1000);
                        FadingScreen.unfade(player, time, 500, () -> {
                            player.setNextWorldTile(new WorldTile(2974, 8979, 0));
                            player.unlock();
                        });
                    }
                });
                return;
            } else if (object.getId() == 106645) {
                player.setNextAnimation(new Animation(828));
                player.lock();
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        final long time = FadingScreen.fade(player, 1000);
                        FadingScreen.unfade(player, time, 500, () -> {
                            player.setNextWorldTile(new WorldTile(2838, 9397, 0));
                            player.unlock();
                        });
                    }
                });
                return;
            } else if (object.getId() == 106646) {
                if (player.getTask() != null && !player.getTask().getName(player).equals("Gemstone dragon") && player.getGemstoneKC() <= 0 || player.getTask() == null && player.getGemstoneKC() <= 0) {
                    for (final Integer n : World.getRegion(player.getRegionId()).getNPCsIndexes()) {
                        final NPC kelhar = World.getNPCs().get(n);
                        if (kelhar == null) {
                            continue;
                        }
                        if (kelhar.getId() == 24169) {
                            kelhar.faceEntity(player);
                        }
                    }
                    player.getDialogueManager().startDialogue("KelharD", 4);
                    return;
                }
                player.setNextAnimation(new Animation(827));
                player.lock();
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        final long time = FadingScreen.fade(player, 1000);
                        FadingScreen.unfade(player, time, 500, () -> {
                            player.setNextWorldTile(new WorldTile(2970, 8976, 0));
                            player.unlock();
                        });
                    }
                });
                return;
            } else if (object.getId() == 23584) {
                return;
            }

            if (id == 34550) {
                final int rot2 = object.getRotation();
                player.useStairs(-1, new WorldTile(object.getX() + (rot2 == 2 ? 2 : 0), object.getY() + (rot2 == 2 ? 1 : 0), player.getPlane() - 1), 0, 1);
                return;
            }
            if (id == 30144) {
                player.getDialogueManager().startDialogue("LeaveFistOfGuthix");
                return;
            }
            if (id == 30203) {
                FOGManager.get().getFOGInstance().managePortalExit(player);
                return;
            }
            if (id == 20608) {
                FOGManager.get().getFOGInstance().managePortalEntering(player);
                return;
            }

            if (id == 34893) {
                if (player.getSkills().getLevel(Skills.SMITHING) < 56) {
                    player.sendMessage("You need at least level 56 Smithing and two iron bars to repair these pegs.");
                    return;
                }
                if (!player.getInventory().containsItem(2351, 2)) {
                    player.sendMessage("You need at least two iron bars to repair these pegs.");
                    return;
                }
                if (player.getSkills().getLevel(Skills.SMITHING) < 56) {
                    player.sendMessage("You need at least level 56 Smithing to repair these pegs.");
                    return;
                }
                player.sendMessage("You begin repairing the pegs...", true);
                player.setNextAnimation(new Animation(898));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.setHasRepairedBurthorpePegs();
                        player.getPackets().addSpawnedObject(new WorldObject(34904, 10, 0, 2935, 3558, 0));
                    }
                }, 1);
            }

            if (id == 30146) {
                FOGManager.get().getFOGInstance().handlePortalEntering(object, player);
                return;
            }
            if (id == 30143) {
                FOGManager.get().getFOGInstance().grabStone(player);
                return;
            }

            if (id == 30141) {
                FOGManager.get().getFOGInstance().handleBarriers(object, player);
                return;
            }
            if (object.getId() == 101910) {
                player.getDialogueManager().startDialogue("VindictaInstanceD", 22460);
                return;
            }

            /* End of the Fist of Guthix object handling */

            // vorago
            if (id == 17819) {
                player.getDialogueManager().startDialogue("VoragoInstanceD");
                return;
            }
            if (id == 84829) {
                player.getDialogueManager().startDialogue("VoragoSignpostD");
                return;
            }
            if (id == 84909) {// vorago graveyard exit
                player.useStairs(828, new WorldTile(2972, 3431, 0), 1, 2);
                return;
            }

            if (id == 50552) {
                if (player.getControlerManager().getControler() instanceof DungeonController) {
                    player.getControlerManager().removeControlerWithoutCheck();
                }
                player.setNextForceMovement(new ForceMovement(object, 1, ForceMovement.NORTH));
                player.getControlerManager().startControler("Kalaboss");
                player.useStairs(13760, new WorldTile(3454, 3725, 0), 2, 3);
                return;
            }

            if (id == 81736) {
                final Item rocktailSoup = new Item(15272, 1);
                if (player.getInventory().containsItem(rocktailSoup)) {
                    final Cookables cook2 = Cooking.isCookingSkill(rocktailSoup);
                    if (cook2 == null)
                        return;
                    Cooking.performPortableAction(player, object, cook2);
                }
                return;

            }
            if (id == 90094) {
                player.getDialogueManager().startDialogue("MuspahInstanceD");
                return;
            }

            if (id == 89897) {
                player.getDialogueManager().startDialogue("NihilInstanceD");
                return;
            }

            /* Christmas event */
            if (id == 65958) {
                player.faceObject(object);
                if (player.getControlerManager().getControler() == null) {
                    player.getControlerManager().startControler("XmasController");
                }
            }
            // home stairs -- dorgesh-kaan
            if (id == 22937) { // downstairs > up
            }
            if (id == 22940) { // upstairs > down

            }

            // doors in sanguine home area cheaphax
            if (id == 60832 || id == 60834 || id == 60836) {
                if (object != null) {
                    World.removeObject(object, true);
                }
            }

            if (object.getId() == 93381) {
                if (OwnedObjectManager.isPlayerObject(player, object)) {
                    player.lock(2);
                    player.setNextAnimation(new Animation(7270));
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            OwnedObjectManager.removeObject(player, object);
                            player.getInventory().addItem(new Item(32337, 1));
                        }
                    }, 1);
                }
                return;
            }

            // home stairs -- lleyta
            if (id == 91173) { // lleyta upstairs > down
                if (object.getX() == 2332 && object.getY() == 3185) { // general
                    // store
                    // lleyta
                    if (player.addWalkSteps(2333, 3185)) {
                        player.faceObject(object);
                        if (player.withinDistance(new WorldTile(2332, 3185, 1))) {
                            player.setNextWorldTile(new WorldTile(2333, 3185, 0));
                        }
                    }
                }

                if (object.getX() == 2332 && object.getY() == 3157) { // south
                    // cooking
                    // room
                    if (player.addWalkSteps(2334, 3158)) {
                        player.faceObject(object);
                    }
                    if (player.withinDistance(new WorldTile(2332, 3158, 1))) {
                        player.setNextWorldTile(new WorldTile(2333, 3159, 0));
                    }
                }

                if (object.getX() == 2328 && object.getY() == 3176) { // top
                    // floor
                    // tower
                    // north
                    if (player.addWalkSteps(2328, 3176)) {
                        player.faceObject(object);
                    }
                    if (player.withinDistance(new WorldTile(2328, 3176, 2))) {
                        player.setNextWorldTile(new WorldTile(2327, 3176, 1));
                    }
                }
                if (object.getX() == 2325 && object.getY() == 3168) { // top
                    // floor
                    // tower
                    // south
                    if (player.addWalkSteps(2326, 3168)) {
                        player.faceObject(object);
                    }
                    if (player.withinDistance(new WorldTile(2326, 3168, 2))) {
                        player.setNextWorldTile(new WorldTile(2326, 3168, 1));
                    }
                }
            }

            if (id == 91145) { // left-curved staircase down
                if (player.addWalkSteps(2343, 3175)) {
                    player.faceObject(object);
                }
                if (player.withinDistance(new WorldTile(object.getX(), object.getY(), object.getPlane()))) {
                    player.setNextWorldTile(new WorldTile(2337, 3174, 0));
                }
            }
            if (id == 91146) { // right-curved staircase down
                if (player.addWalkSteps(2343, 3168)) {
                    player.faceObject(object);
                }
                if (player.withinDistance(new WorldTile(object.getX(), object.getY(), object.getPlane()))) {
                    player.setNextWorldTile(new WorldTile(2337, 3169, 0));
                }
            }
            if (id == 91147) { // left-curved staircase up
                if (player.addWalkSteps(2343, 3175)) {
                    player.faceObject(object);
                }
                if (player.withinDistance(new WorldTile(object.getX(), object.getY(), object.getPlane()))) {
                    player.setNextWorldTile(new WorldTile(2343, 3175, 1));
                }
            }
            if (id == 91148) { // right-curved staircase up
                if (player.withinDistance(new WorldTile(2337, 3171, object.getPlane()))) {
                    player.setNextWorldTile(new WorldTile(2343, 3168, 1));
                }
            }

            if (id == 84472) {
                final CrystalTriskelion ct = new CrystalTriskelion();

                if (player.getInventory().containsItem(28550, 1)) {
                    player.getInventory().deleteItem(28550, 1);
                    player.triskKeyReedem++;
                    player.setNextAnimation(new Animation(11490));

                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            World.sendObjectAnimation(object, new Animation(20308));
                            WorldTasksManager.schedule(new WorldTask() {
                                @Override
                                public void run() {
                                    ct.generateRewards(player);
                                    String end = player.triskKeyReedem > 1 ? "keys" : "key";
                                    player.sendMessage(Colors.RED + "You have redeemed " + player.triskKeyReedem + " triskelion " + end + ".");
                                    WorldTasksManager.schedule(new WorldTask() {
                                        @Override
                                        public void run() {
                                            World.sendObjectAnimation(object, new Animation(20307));
                                        }
                                    }, 3);
                                }
                            }, 3);
                        }
                    });
                } else {
                    player.sendMessage("You do not have a completed crystal triskelion key.");
                }

            }

            if (id == 91171) { // upstairs walk in lleyta
                if (object.getX() == 2332 && object.getY() == 3157) {
                    if (player.withinDistance(new WorldTile(object.getX(), object.getY(), object.getPlane()))) {
                        player.setNextWorldTile(new WorldTile(2334, 3158, 1));
                    }
                }
                if (object.getX() == 2332 && object.getY() == 3185) {
                    if (player.withinDistance(new WorldTile(object.getX(), object.getY(), object.getPlane()))) {
                        player.setNextWorldTile(new WorldTile(2333, 3184, 1));
                    }
                }
                if (object.getX() == 2328 && object.getY() == 3176) {
                    if (player.withinDistance(new WorldTile(object.getX(), object.getY(), object.getPlane()))) {
                        player.setNextWorldTile(new WorldTile(2327, 3176, 1));
                    }
                }
                if (object.getX() == 2325 && object.getY() == 3168) {
                    if (player.withinDistance(new WorldTile(object.getX(), object.getY(), object.getPlane()))) {
                        player.setNextWorldTile(new WorldTile(2327, 3168, 1));
                    }
                }
            }
            if (id == 2296 && objectY == 3477) {
                if (!Agility.hasLevel(player, 20)) {
                    return;
                }
                player.lock(5);
                player.addWalkSteps(object.getX() + (object.getRotation() == 3 ? 4 : -4), object.getY(), -1, false);
                player.sendMessage("You walk carefully across the slippery log...", true);
                WorldTasksManager.schedule(new WorldTask() {
                    boolean secondloop;

                    @Override
                    public void run() {
                        if (!secondloop) {
                            secondloop = true;
                            player.getAppearence().setRenderEmote(155);
                        } else {
                            player.getAppearence().setRenderEmote(-1);
                            player.getSkills().addXp(Skills.AGILITY, 7.5);
                            player.sendMessage("... and make it safely to the other side.", true);
                            stop();
                        }
                    }
                }, 0, 3);
            }

            /**
             * Fally castle up
             */
            if (id == 11724) {
                player.useStairs(-1, new WorldTile(2968, 3348, 1), 1, 0);
                return;
            }
            /**
             * Fally castle down
             */
            if (id == 11725) {
                player.useStairs(-1, new WorldTile(2971, 3347, 0), 1, 0);
                return;
            }

            if (id == 71902) {
                player.useStairs(-1, new WorldTile(2968, 3219, 1), 1, 0);
                return;
            }
            if (id == 71903) {
                player.useStairs(-1, new WorldTile(2964, 3219, 0), 1, 0);
                return;
            }
            if (id == 47364) {
                player.useStairs(-1, new WorldTile(3108, 3366, 1), 1, 0);
                return;
            }
            if (id == 47657) {
                player.useStairs(-1, new WorldTile(3108, 3361, 0), 1, 0);
                return;
            }
            if (id == 100254) {
                player.useStairs(-1, new WorldTile(3352, 3148, 0), 1, 0);
            }
            if (id == 100255) {
                player.useStairs(-1, new WorldTile(5160, 7590, 0), 1, 0);
            }

            // End of halloween items
            // Invention guild - go inside
            if (id == 100850) {
                if (player.getSkills().getLevelForXp(Skills.DIVINATION) < 80 || player.getSkills().getLevelForXp(Skills.SMITHING) < 80 || player.getSkills().getLevelForXp(Skills.CRAFTING) < 80) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "The Invention guild is only available to the Elite.");
                    player.sendMessage("You need level 80 in Divination, Smithing and Crafting to go in here.");
                    return;
                }
                player.useStairs(-1, new WorldTile(6169, 1038, 0), 0, 0);
                return;
            }
            // Invention guild - go outside
            if (id == 100937) {
                player.useStairs(-1, new WorldTile(2997, 3439, 0), 0, 0);
                return;
            }
            if (id == 1757 && objectX == 2594 && objectY == 9485) {
                player.useStairs(828, new WorldTile(2594, 3086, 0), 1, 2);
                return;
            }
            if (id == 2606 && objectX == 2836 && objectY == 9600) {
                if (World.isSpawnedObject(object)) {
                    return;
                }
                player.lock(1);
                final WorldObject opened = new WorldObject(object.getId(), object.getType(), object.getRotation() - 1, object.getX(), object.getY(), object.getPlane());
                World.spawnObjectTemporary(opened, 1200);
                player.addWalkSteps(2836, player.getY() == objectY ? objectY - 1 : objectY, 1, false);
                return;
            }
            if (id == 25213 && objectX == 2833 && objectY == 9657) {
                player.useStairs(828, new WorldTile(2834, 3258, 0), 1, 2);
                return;
            }
            if (id == 25154) {
                player.useStairs(827, new WorldTile(2834, 9657, 0), 1, 2);
                return;
            }
            if (object.getDefinitions().getName().toLowerCase().contains("spirit tree") || object.getId() == 26723) {
                player.getDialogueManager().startDialogue("SpiritTreeD", (object.getId() == 68973 || object.getId() == 68974) ? 3637 : 3636);
                return;
            } else if (id == 77745 || id == 28779) {
                if (objectX == 3142 && objectY == 5545) {
                    BorkController.enterBork(player);
                    return;
                }
                player.addWalkSteps(object.getX(), object.getY(), -1, false);
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        if (getRepeatedTele(player, 3285, 5474, 0, 3286, 5470, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3302, 5469, 0, 3290, 5463, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3280, 5460, 0, 3273, 5460, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3299, 5450, 0, 3296, 5455, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3283, 5448, 0, 3287, 5448, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3260, 5491, 0, 3266, 5446, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3239, 5498, 0, 3244, 5495, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3238, 5507, 0, 3232, 5501, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3222, 5488, 0, 3218, 5497, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3222, 5474, 0, 3224, 5479, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3215, 5475, 0, 3218, 5478, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3210, 5477, 0, 3208, 5471, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3212, 5452, 0, 3214, 5456, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3235, 5457, 0, 3229, 5454, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3204, 5445, 0, 3197, 5448, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3191, 5495, 0, 3194, 5490, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3185, 5478, 0, 3191, 5482, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3186, 5472, 0, 3192, 5472, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3189, 5444, 0, 3187, 5460, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3178, 5460, 0, 3168, 5456, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3171, 5478, 0, 3167, 5478, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3171, 5473, 0, 3167, 5471, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3142, 5489, 0, 3141, 5480, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3142, 5462, 0, 3154, 5462, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3155, 5449, 0, 3143, 5443, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3303, 5477, 0, 3299, 5484, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3318, 5481, 0, 3322, 5480, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3307, 5496, 0, 3317, 5496, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3265, 5491, 0, 3260, 5491, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3297, 5510, 0, 3300, 5514, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3325, 5518, 0, 3323, 5531, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3321, 5554, 0, 3315, 5552, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3291, 5555, 0, 3285, 5556, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3285, 5508, 0, 3280, 5501, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3285, 5527, 0, 3282, 5531, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3289, 5532, 0, 3288, 5536, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3266, 5552, 0, 3262, 5552, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3268, 5534, 0, 3261, 5536, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3248, 5547, 0, 3253, 5561, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3256, 5561, 0, 3252, 5543, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3244, 5526, 0, 3241, 5529, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3230, 5547, 0, 3226, 5553, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3206, 5553, 0, 3204, 5546, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3211, 5533, 0, 3214, 5533, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3208, 5527, 0, 3211, 5523, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3201, 5531, 0, 3197, 5529, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3202, 5516, 0, 3196, 5512, 0)) {
                            return;
                        }
                        if (getRepeatedTele(player, 3197, 5529, 0, 3201, 5531, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3165, 5515, 0, 3173, 5530, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3156, 5523, 0, 3152, 5520, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3148, 5533, 0, 3153, 5537, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3143, 5535, 0, 3147, 5541, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3158, 5561, 0, 3162, 5557, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3162, 5545, 0, 3166, 5553, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3168, 5541, 0, 3171, 5542, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3190, 5549, 0, 3190, 5554, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3180, 5557, 0, 3174, 5558, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3190, 5519, 0, 3190, 5515, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3185, 5518, 0, 3181, 5517, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3196, 5512, 0, 3202, 5516, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3159, 5501, 0, 3169, 5510, 0)) {
                            return;
                        } else if (getRepeatedTele(player, 3182, 5530, 0, 3187, 5531, 0)) {
                            return;
                        }
                    }
                });
            }
            if (id == 56805) {
                // player.setNextAnimation(new Animation(839));
                final boolean outSide1 = player.getY() > object.getY();
                player.setNextWorldTile(new WorldTile(player.getX(), outSide1 ? object.getY() - 1 : object.getY() + 1, 0));

            }
            if (id == 26130 || id == 26131 || id == 68977 || id == 2306 || id == 26081 || id == 26082 || id == 1551) {
                handleGate(player, object, 60000);
            }
            if (id == 2514 || id == 1600 || id == 1601) {
                handleDoor(player, object);
            }
            if (id == 35391 || id == 2832) {
                if (!Agility.hasLevel(player, id == 2832 ? 20 : 41)) {
                    return;
                }
                player.addWalkSteps(objectX, objectY);
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        final boolean isTravelingWest = id == 2832 ? player.getX() >= 2508 : (objectX == 2834 && objectY == 3626) ? player.getX() >= 2834 : player.getX() >= 2900;
                        player.useStairs(3303, new WorldTile((isTravelingWest ? -2 : 2) + player.getX(), player.getY(), 0), 4, 5, null, true);
                    }
                });
                return;
            }
            if (id == 2830 || id == 2831) {
                player.useStairs(-1, new WorldTile(player.getX(), id == 2831 ? 3026 : 3029, 0), 1, 2);
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        player.getDialogueManager().startDialogue("SimplePlayerMessage", "Phew! I barely made it.");
                        stop();
                    }
                }, 1);
            }
            if (id == 76499) { // Al'kharid palace entrance
                handleDoor(player, object);
                return;
            }
            if (id == 4627) {
                player.useStairs(-1, new WorldTile(2893, 3567, 0), 1, 2);
            }
            if (id == 32270) { // Yanille staircase - up
                player.useStairs(-1, new WorldTile(2606, 3079, 0), 0, 1);
                return;
            }
            if (id == 32271) { // Yanille staircase - down
                player.useStairs(827, new WorldTile(2602, 9478, 0), 2, 3);
                return;
            }
            if (id == 2559) { // Yanille basement door
                handleDoor(player, object);
                return;
            }
            if (id == 10229) { // Dag King stairs up
                player.useStairs(828, new WorldTile(1912, 4367, 0), 1, 2);
                return;
            }
            if (id == 97412) {
                boolean outSide = object.getY() < player.getY();
                player.setNextWorldTile(new WorldTile(object.getX(),
                        ((outSide ? (object.getY() - 1) : (object.getY() + 1))), object.getPlane()));
                return;
            }
            if (id == 24367) { // Varrock palace staircase up
                player.useStairs(-1, new WorldTile(3213, 3476, 1), 0, 1);
                return;
            }
            if (id == 24359) { // Varrock palace staircase down
                player.useStairs(-1, new WorldTile(3213, 3472, 0), 0, 1);
                return;
            }
            if (id == 66973) {
                player.useStairs(-1, new WorldTile(2206, 4934, 1), 1, 2);
            }
            if (id == 4620) {
                player.useStairs(-1, new WorldTile(2207, 4938, 0), 1, 2);
            }
            if (id == 4622 && objectX == 2212) {
                player.useStairs(-1, new WorldTile(2212, 4944, 1), 1, 2);
            }
            if (id == 42794) {
                player.useStairs(-1, new WorldTile(object.getX(), object.getY() + 7, 0), 0, 1);
            }
            if (id == 42795) {
                player.useStairs(-1, new WorldTile(object.getX(), object.getY() - 6, 0), 0, 1);
            }
            if (id == 48188) {
                player.useStairs(-1, new WorldTile(3435, 5646, 0), 0, 1);
            }
            if (id == 48189) {
                player.useStairs(-1, new WorldTile(3509, 5515, 0), 0, 1);
            }
            if (id == 492 && objectX == 2856 && objectY == 3168) {
                // underground
                player.useStairs(827, new WorldTile(2856, 9570, 0), 1, 2);
            }
            if (id == 1764 && objectX == 2856 && objectY == 9569) {
                // upperground
                player.useStairs(828, new WorldTile(2856, 3170, 0), 1, 2);
            }
            if (id == 68134) {
                player.useStairs(-1, new WorldTile(4667, 5059, 0), 0, 1);
            }
            if (id == 68135) {
                player.useStairs(-1, new WorldTile(2845, 3170, 0), 0, 1);
            }
            if (id == 84702) { // Monastery of Ascension entrance
                if (player.getSkills().getLevel(Skills.SLAYER) < 81) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You will need at least a Slayer level of 81 to enter the Ascension's Dungeon.");
                    return;
                }
                player.useStairs(-1, new WorldTile(1095, 579, 1), 0, 1);
            }
            if (id == 84724) {
                player.useStairs(-1, new WorldTile(2500, 2887, 0), 0, 1);
            }
            if (id >= 84726 && id <= 84731) { // Legio boss entrances
                player.getDialogueManager().startDialogue("LegioLaboratoryD", object);
                return;
            }
            if (id == 3379) {
                player.useStairs(-1, new WorldTile(2646, 9378, 0), 0, 1);
            }
            if (id == 32069) {
                player.useStairs(-1, new WorldTile(2631, 2997, 0), 0, 1);
            }
            if (id == 2468) {
                if (objectX == 2655 && objectY == 4829 && objectPlane == 0) {
                    player.setNextWorldTile(new WorldTile(3307, 3476, 0));
                }
                return;
            }
            if (id == 2477) {
                if (objectX == 2597 && objectY == 3155) {
                    player.setNextWorldTile(new WorldTile(2468, 4889, 1));
                } else if (objectX == 2468 && objectY == 4888 && objectPlane == 1) {
                    player.setNextWorldTile(new WorldTile(4127, 5848,0));
                } else if (objectX == 2468 && objectY == 4888) {
                    player.setNextWorldTile(new WorldTile(2597, 3156, 0));
                } else {
                    player.sendMessage("This portal does not lead anywhere.");
                }
                return;
            }
            if (id == 69499) { // east of yanille - hazelmere (clue scrolls)
                // - up
                if (objectX == 2525 && objectY == 3162) {
                    player.useStairs(828, new WorldTile(2526, 3162, 1), 1, 2);
                    return;
                }
                if (object.getX() == 2476 && object.getY() == 3463) {
                    player.useStairs(828, new WorldTile(2477, 3463, 1), 1, 2);
                    return;
                }
                player.useStairs(828, new WorldTile(2677, 3086, 1), 1, 2);
                return;
            }

            if (id == 19171) {
                final boolean outSide2 = player.getX() <= object.getX();
                player.useStairs(-1, new WorldTile(outSide2 ? object.getX() + 1 : object.getX(), player.getY(), 0), 1, 2);
                return;
            }
            if (id == 69502) { // east of yanille - hazelmere (clue scrolls)
                if (object.getX() == 2476 && object.getY() == 3463) {
                    player.useStairs(827, new WorldTile(2477, 3463, 0), 1, 2);
                    return;
                } // - down
                player.useStairs(827, new WorldTile(2677, 3086, 0), 1, 2);
                return;
            }
            if (id == 26118) { // seer's village spinning wheel house
                player.useStairs(828, new WorldTile(2714, 3472, 3), 1, 2);
                return;
            }
            if (id == 26119) { // seer's village spinning wheel house
                player.useStairs(828, new WorldTile(2714, 3472, 1), 1, 2);
                return;
            }
            if (id == 31299) { // market signpost
                CrystalChest.openRewardsInterface(player);
                return;
            }
            if (id == 2186) { // tree gnome village - loose railing
                if (player.getY() <= 3160) {
                    player.useStairs(-1, new WorldTile(2515, 3161, 0), 0, 0);
                } else {
                    player.useStairs(-1, new WorldTile(2515, 3160, 0), 0, 0);
                }
                return;
            }
            if (PortableStation.isPortableObject(object)) {
                PortableStation.handleObjectClick1(player, object);
                return;
            }
            if (id == 15516 || id == 15514 || id == 45206 || id == 45208 || id == 45210 || id == 45212) {
                handleDoor(player, object, 60000);
                return;
            }
            // Gnome stronghold big door
            if (id == 68983) {
                player.setNextWorldTile(new WorldTile(2462, (player.getY() <= 3383 ? 3384 : 3383), 0));
                return;
            }
            if (id == 14922) {
                player.setNextWorldTile(new WorldTile(2344, (player.getY() <= 3652 ? 3655 : 3650), 0));
                return;
            }
            if (id == 14929 || id == 14931) {
                handleDoor(player, object, 60000);
                return;
            }
            // White Wolf Mountain cut
            if (id == 56) {
                player.useStairs(-1, new WorldTile(2879, 3465, 0), 0, 1);
                return;
            } else if (id == 66990) {
                player.useStairs(-1, new WorldTile(2875, 9880, 0), 0, 1);
            } else if (id == 2811 || id == 2812) {
                player.useStairs(id == 2812 ? 827 : -1, id == 2812 ? new WorldTile(2501, 2989, 0) : new WorldTile(2574, 3029, 0), 1, 2);
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        player.getDialogueManager().startDialogue("SimplePlayerMessage", "Wow! That tunnel went a long way.");
                    }
                });
            } else if (id == 2890 || id == 2893) {
                if (player.getEquipment().getWeaponId() != 975 && !player.getInventory().containsOneItem(975, 1)) {
                    player.sendMessage("You need a machete in order to cutt through the terrain.");
                    return;
                }
                player.setNextAnimation(new Animation(910));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        if (Utils.random(3) == 0) {
                            player.sendMessage("You fail to slash through the terrain.");
                            return;
                        }
                        final WorldObject o = new WorldObject(object);
                        o.setId(id + 1);
                        World.spawnObjectTemporary(o, 5000);
                    }
                });
            } else if (id == 2231) {
                player.useStairs(-1, new WorldTile(objectX == 2792 ? 2795 : 2791, 2979, 0), 1, 2, objectX == 2792 ? "You climb down the slope." : "You climb up the slope.");
            } else if (id == 23157) {
                player.useStairs(-1, new WorldTile(2729, 3734, 0), 1, 2);
            } else if (id == 492 && objectX == 2856 && objectY == 3168) {
                // underground and
                // crandor
                player.useStairs(827, new WorldTile(2856, 9570, 0), 1, 2);
            } else if (id == 1764 && objectX == 2856 && objectY == 9569) {
                player.useStairs(828, new WorldTile(2855, 3169, 0), 1, 2);
            } else if (id == 2158) {
                player.useStairs(-1, new WorldTile(3104, 3163, 2), 0, 1);
            } else if (id == 2157) {
                player.useStairs(-1, new WorldTile(2908, 3332, 2), 0, 1);
            } else if (id == 2156) {
                player.useStairs(-1, new WorldTile(2702, 3405, 3), 0, 1);
            } else if (id == 1754 && objectX == 2594 && objectY == 3085) {
                player.useStairs(827, new WorldTile(2594, 9486, 0), 1, 2);
            } else if (id == 1722 && objectX == 2590 && objectY == 3089) {
                player.useStairs(-1, new WorldTile(2590, 3092, 1), 0, 1);
            } else if (id == 1723 && objectX == 2590 && objectY == 3090) {
                player.useStairs(-1, new WorldTile(2590, 3088, 0), 0, 1);
            } else if (id == 1722 && objectX == 2590 && objectY == 3084) {
                player.useStairs(-1, new WorldTile(2590, 3087, 2), 0, 1);
            } else if (id == 1723 && objectX == 2590 && objectY == 3085) {
                player.useStairs(-1, new WorldTile(2591, 3083, 1), 0, 1);
            }
            if (id == 54) {
                player.useStairs(-1, new WorldTile(2820, 3486, 0), 0, 1);
                return;
            }
            if (id == 55) {
                player.useStairs(-1, new WorldTile(2821, 9882, 0), 0, 1);
                return;
            }
            if (id == 1596 || id == 1597) {
                handleGate(player, object, 60000);
                return;
            } else if (id == 73681) {
                final WorldTile dest = new WorldTile(player.getX() == 2595 ? 2598 : 2595, 3608, 0);
                player.setNextForceMovement(new NewForceMovement(player, 1, dest, 2, Utils.getAngle(dest.getX() - player.getX(), dest.getY() - player.getY())));
                player.useStairs(-1, dest, 1, 2);
                player.setNextAnimation(new Animation(769));
            }
            // sabbot lair
            else if (id == 34395) {
                player.useStairs(-1, new WorldTile(2808, 10002, 0), 0, 1);
            } else if (id == 77053) {
                player.useStairs(-1, new WorldTile(2795, 3616, 0), 0, 1);
            } else if (id >= 2889 && id <= 2892) {
                final boolean isNorth = player.getY() >= 2940;
                player.useStairs(-1, player.transform(0, isNorth ? -8 : 8, 0), 2, 3, "You slash your way through the marsh and get to the other side.");
            } else if (id == 32738) {
                player.useStairs(-1, new WorldTile(2858, 3577, 0), 0, 1);
            } else if (id == 4918 && objectX == 3445 && objectY == 3236) {
                player.useStairs(4853, new WorldTile(player.getX() == 3446 ? 3444 : 3446, object.getY(), 0), 2, 3);
            } else if (id == 12776) {
                player.useStairs(4853, new WorldTile(player.getX() == 3474 ? 3473 : 3474, object.getY(), 0), 2, 3);
            } else if (id == 17757 || id == 17760) {
                player.useStairs(4853, new WorldTile(object.getX(), object.getY() == player.getY() ? 3243 : 3244, 0), 2, 3);
            } else if (id == 20979) {
                player.useStairs(-1, new WorldTile(3149, 4666, 0), 0, 1);
            } else if (id == 4913 && objectX == 3440 && objectY == 3232) {
                player.useStairs(-1, new WorldTile(3436, 9637, 0), 0, 1);
            } else if (id == 4920 && objectX == 3437 && objectY == 9637) {
                player.useStairs(-1, new WorldTile(3441, 3232, 0), 0, 1);
            } else if (id == 3522 && objectY == 3329) {
                player.useStairs(-1, new WorldTile(objectX, 3332, 0), 0, 1);
            } else if (id == 3522 && objectY == 3331) {
                player.useStairs(-1, new WorldTile(objectX, 3329, 0), 0, 1);
            } else if ((id == 9738 || id == 9330)) {
                final boolean outSide3 = player.getX() > object.getX();
                player.setNextWorldTile(new WorldTile(outSide3 ? object.getX() - 1 : object.getX() + 1, player.getY(), 0));

            } else if ((id == 18411)) {
                if (object.getX() < player.getX() && object.getY() == player.getY()) {
                    player.setNextWorldTile(new WorldTile((player.getX() - 0), (player.getY()), +1));

                } else if (object.getX() > player.getX() && object.getY() == player.getY()) {
                    player.setNextWorldTile(new WorldTile((player.getX() - 0), (player.getY()), -1));
                } else {
                    player.sendMessage("You can't reach that.");
                    return;
                }
            } else if (id == 4914 && objectX == 3430 && objectY == 3233) {
                player.useStairs(-1, new WorldTile(3405, 9631, 0), 0, 1);
            } else if (id == 5083 && objectX == 2743 && objectY == 3153) {
                player.useStairs(-1, new WorldTile(2711, 9564, 0), 0, 1);
            } else if (id == 77421 && objectX == 2711 && objectY == 9563) {
                player.useStairs(-1, new WorldTile(2745, 3152, 0), 0, 1);
            } else if (id == 4921 && objectX == 3404 && objectY == 9631) {
                player.useStairs(-1, new WorldTile(3429, 3233, 0), 0, 1);
            } else if (id == 20524 && objectX == 3408 && objectY == 9623) {
                player.useStairs(-1, new WorldTile(3428, 3225, 0), 0, 1);
            } else if (id == 4915 && objectX == 3429 && objectY == 3225) {
                player.useStairs(-1, new WorldTile(3409, 9623, 0), 0, 1);
            } else if (id == 28515) {
                player.useStairs(4853, new WorldTile(3420, 2803, 1), 2, 3);
            } else if (id == 89742) { // rune dragons - enter
                player.lock();
                player.setNextAnimation(new Animation(23099));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        FadingScreen.fade(player, 0, () -> {
                            player.unlock();
                            player.setNextAnimation(new Animation(-1));
                            player.setNextWorldTile(new WorldTile(4767, 6078, 1));
                        });
                    }
                }, 0);
            } else if (id == 97217) { // rune dragons - exit
                player.lock();
                player.setNextAnimation(new Animation(23099));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        FadingScreen.fade(player, 0, () -> {
                            player.unlock();
                            player.setNextAnimation(new Animation(-1));
                            player.setNextWorldTile(new WorldTile(2367, 3354, 0));
                        });
                    }
                }, 0);
            } else if (id == 97054) { // adamant dragons - enter
                player.lock();
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        FadingScreen.fade(player, 0, () -> {
                            player.unlock();
                            player.setNextWorldTile(new WorldTile(4512, 6045, 0));
                        });
                    }
                }, 0);
            } else if (id == 97055) { // adamant dragons - exit
                player.lock();
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        FadingScreen.fade(player, 0, () -> {
                            player.unlock();
                            player.setNextWorldTile(new WorldTile(2688, 9482, 0));
                        });
                    }
                }, 0);
            } else if (id == 97056) {
                if (player.getSkills().getLevelForXp(Skills.SLAYER) < 70) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You need a Slayer level of at least 70 to pass this magical barrier.");
                    return;
                }
                player.setNextWorldTile(new WorldTile((player.getX() <= 4530 ? 4532 : 4530), 6029, 0));
            } else if (id == 97057) {
                if (player.getSkills().getLevelForXp(Skills.SLAYER) < 80) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You need a Slayer level of at least 80 to pass this magical barrier.");
                    return;
                }
                player.setNextWorldTile(new WorldTile(4531, (player.getY() <= 6062 ? 6064 : 6062), 0));
            } else if (id == 97183) {
                if (player.getSkills().getLevelForXp(Skills.SLAYER) < 80) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You need a Slayer level of at least 80 to pass this magical barrier.");
                    return;
                }
                player.setNextWorldTile(new WorldTile(4512, (player.getY() <= 6051 ? 6053 : 6051), 0));
            } else if (id == 97184) {
                if (player.getSkills().getLevelForXp(Skills.SLAYER) < 90) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You need a Slayer level of at least 90 to pass this magical barrier.");
                    return;
                }
                if (object.getX() == 4501) {
                    player.setNextWorldTile(new WorldTile((player.getX() >= 4502 ? 4500 : 4502), 6023, 0));
                } else if (object.getX() == 4491) {
                    player.setNextWorldTile(new WorldTile(4492, (player.getY() <= 6054 ? 6056 : 6054), 0));
                } else {
                    player.sendMessage("Nothing interesting happens.");
                }
            }
            // Baxtorian falls
            else if (id == 2020) {
                player.useStairs(4855, new WorldTile(2511, 3463, 0), 1, 2);
            } else if (id == 2022) {
                player.sendMessage("You get in the barrel and start shaking to fall down..");
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        FadingScreen.fade(player, 0, () -> {
                            player.setNextWorldTile(new WorldTile(2532, 3415, 0));
                            player.sendMessage("The barrel has finally stopped; " + "you've found yourself in a strange place.");
                        });
                    }
                }, 4);
            }
            // Baxtorian falls dungeon entrance
            else if (id == 37247) {
                FadingScreen.fade(player, 0, () -> player.setNextWorldTile(new WorldTile(2575, 9861, 0)));
            }
            // Baxtorian falls dungeon exit
            else if (id == 32711) {
                FadingScreen.fade(player, 0, () -> player.setNextWorldTile(new WorldTile(2511, 3463, 0)));
                return;
            } else if (id == 21306 && objectX == 2317 && objectY == 3824) {
                if (!Agility.hasLevel(player, 40)) {
                    return;
                }
                player.lock(5);
                player.addWalkSteps(2317, 3832, -1, false);
            } else if (id == 21307 && objectX == 2317 && objectY == 3831) {
                if (!Agility.hasLevel(player, 40)) {
                    return;
                }
                player.lock(5);
                player.addWalkSteps(2317, 3823, -1, false);
            } else if (id == 21308) {
                player.lock(5);
                player.addWalkSteps(2343, 3829, -1, false);
            } else if (id == 21309) {
                player.lock(5);
                player.addWalkSteps(2343, 3820, -1, false);
            } else if (id == 67966) {
                player.lock();
                player.setNextAnimation(new Animation(6723));
                player.setNextForceMovement(new NewForceMovement(new WorldTile(player), 4, new WorldTile(2512, 3509, 0), 7, Utils.getAngle(0, -1)));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        FadingScreen.fade(player, 0, () -> {
                            player.sendMessage("You are swept, out of control, thought horrific underwater currents.");
                            player.sendMessage("You are swirled beneath the water, dashed against sharp rocks.");
                            player.sendMessage("Mystical forces guide you into a cavern below the whirpool.");
                            player.setNextWorldTile(new WorldTile(1763, 5365, 1));
                            player.lock(1);
                        });
                    }
                }, 4);
            } else if (id == 25216) {
                player.lock();
                FadingScreen.fade(player, 0, () -> {
                    player.sendMessage("You are swept, out of control, thought horrific underwater currents.");
                    player.sendMessage("You are swirled beneath the water, dashed against sharp rocks.");
                    player.sendMessage("You find yourself on the banks of the river, far below the lake.");
                    player.setNextWorldTile(new WorldTile(2531, 3446, 0));
                    player.lock(1);
                });
                // Kuradal dungeon
            }
            // scabaras florest
            else if (id == 28515) {
                player.useStairs(4853, new WorldTile(3420, 2803, 1), 2, 3);
            } else if (id == 28516) {
                player.useStairs(4853, new WorldTile(3420, 2801, 0), 2, 3);
            } else if (id == 41435 && objectX == 2732 && objectY == 3377) {
                player.useStairs(-1, new WorldTile(2732, 3380, 1), 0, 1);
            } else if (id == 41436 && objectX == 2732 && objectY == 3378) {
                player.useStairs(-1, new WorldTile(2732, 3376, 0), 0, 1);
            } else if (id == 41425 && objectX == 2724 && objectY == 3374) {
                player.useStairs(-1, new WorldTile(2720, 9775, 0), 0, 1);
            } else if (id == 32048 && objectX == 2717 && objectY == 9773) {
                player.useStairs(-1, new WorldTile(2723, 3375, 0), 0, 1);
            } else if (id == 1987) {
                player.useStairs(-1, new WorldTile(2513, 3480, 0), 1, 2, "The raft is pulled down by the strong currents.");
            } else if (id == 10283 || id == 1996) {
                player.setNextWorldTile(new WorldTile(player.getX(), id == 10283 ? 3467 : 3476, 0));
            }
            if (id == 9270) {
                if (PetPerkUtils.getPerkTier(PetPerk.OVERLOADED, player.getCurrentPet()) == 3) {
                    player.sm("Thanks to your Tier 3 Overloaded perk your overload converted itself to an Supreme overload salve.");
                    SUPREME_OVERLOAD_SALVE.extra(player);
                    return;
                }
                if (player.getOverloadDelay() > 0) {
                    player.sendMessage("You may only use the supply table every five minutes.");
                    return;
                }
                if ((player.getHitpoints() <= 500 && (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED))) || player.getOverloadDelay() > 480) {
                    player.sendMessage("You need more than 500 life points to survive the power of overload.");
                    return;
                }
                player.addSuperAntiFire((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 360000);
                player.setPrayerRenewalDelay((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 501);
                player.setOverloadDelay((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 501);
                player.getBuffDebuffTimersManager().addTimer(Timer.OVERLOADED, ((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 501) * 600);
                player.getBuffDebuffTimersManager().addTimer(Timer.SUPER_ANTIFIRE_ACTIVE, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 360000);
                player.getBuffDebuffTimersManager().addTimer(Timer.PRAYER_RENEW_ACTIVE, ((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 501) * 600);
                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE)) {
                    player.sendMessage("Overload, prayer renewal & super antifire duration doubled thanks to Herbivore perk.", true);
                }
                if (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED))
                    WorldTasksManager.schedule(new WorldTask() {
                        int count = 4;

                        @Override
                        public void run() {
                            if (count == 0) {
                                stop();
                            }
                            player.setNextAnimation(new Animation(3170));
                            player.setNextGraphics(new Graphics(560));
                            player.applyHit(new Hit(player, 100, HitLook.REGULAR_DAMAGE, 0));
                            count--;
                        }
                    }, 0, 2);
                return;
            }

            if (id == 66437) {
                if (PetPerkUtils.getPerkTier(PetPerk.OVERLOADED, player.getCurrentPet()) == 3) {
                    player.sm("Thanks to your Tier 3 Overloaded perk your overload converted itself to an Supreme overload salve.");
                    SUPREME_OVERLOAD_SALVE.extra(player);
                    return;
                }
                if (player.getSupremeOverloadDelay() > 0) {
                    player.sendMessage("You may only use the resource chest every five minutes.");
                    return;
                }
                if ((player.getHitpoints() <= 500 && (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED))) || player.getSupremeOverloadDelay() > 480) {
                    player.sendMessage("You need more than 500 life points to survive the power of overload.");
                    return;
                }
                player.addSuperAntiFire((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 360000);
                player.addPoisonImmune((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 346000);
                player.setPrayerRenewalDelay((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 501);
                player.setSupremeOverloadDelay((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 501);
                player.getBuffDebuffTimersManager().addTimer(Timer.SUPREME_OVERLOAD_POTION_ACTIVE, ((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 501) * 600);
                player.getBuffDebuffTimersManager().addTimer(Timer.SUPER_ANTIFIRE_ACTIVE, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 360000);
                player.getBuffDebuffTimersManager().addTimer(Timer.ANTIPOISON_ACTIVE, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 346000);
                player.getBuffDebuffTimersManager().addTimer(Timer.PRAYER_RENEW_ACTIVE, ((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 501) * 600);
                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE)) {
                    player.sendMessage("Supreme Overload, prayer renewal, super antipoison & super antifire duration doubled thanks to Herbivore perk.", true);
                }
                if (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED))
                    WorldTasksManager.schedule(new WorldTask() {
                        int count = 4;

                        @Override
                        public void run() {
                            if (count == 0) {
                                stop();
                            }
                            player.setNextAnimation(new Animation(3170));
                            player.setNextGraphics(new Graphics(560));
                            player.applyHit(new Hit(player, 100, HitLook.REGULAR_DAMAGE, 0));
                            count--;
                        }
                    }, 0, 2);
                return;
            }
            if (id == 84715) {
                if (object.getY() == 611) {
                    if (!Agility.hasLevel(player, 80)) {
                        return;
                    }
                    player.useStairs(-1, new WorldTile(player.getX() <= 1132 ? 1135 : 1132, player.getY(), 1), 0, 1);
                } else if (object.getY() >= 652 && object.getY() <= 654 && !(object.getX() == 1065 || object.getX() == 1067)) {
                    if (!Agility.hasLevel(player, 90)) {
                        return;
                    }
                    player.useStairs(-1, new WorldTile(player.getX(), player.getY() <= 652 ? 654 : 652, 1), 0, 1);
                } else if (object.getX() >= 1150 && object.getX() <= 1152) {
                    if (!Agility.hasLevel(player, 90)) {
                        return;
                    }
                    player.useStairs(-1, new WorldTile(player.getX() <= 1150 ? 1152 : 1150, player.getY(), 1), 0, 1);
                } else if (object.getX() == 1096) {
                    if (!Agility.hasLevel(player, 70)) {
                        return;
                    }
                    player.useStairs(-1, new WorldTile(player.getX(), player.getY() <= 626 ? 628 : 626, 1), 0, 1);
                } else if (object.getX() == 1065 || object.getX() == 1067) {
                    if (!Agility.hasLevel(player, 60)) {
                        return;
                    }
                    player.useStairs(-1, new WorldTile(player.getX() <= 1065 ? 1067 : 1065, player.getY(), 1), 0, 1);
                }
                return;
            }
            if (id == 2322 || id == 2323) {
                if (!Agility.hasLevel(player, 10)) {
                    return;
                }
                player.lock(4);
                player.setNextAnimation(new Animation(751));
                World.sendObjectAnimation(player, object, new Animation(497));
                final WorldTile toTile3 = new WorldTile(id == 2323 ? 2709 : 2704, objectY, object.getPlane());
                player.setNextForceMovement(new ForceMovement(player, 1, toTile3, 3, id == 2323 ? ForceMovement.EAST : ForceMovement.WEST));
                player.getSkills().addXp(Skills.AGILITY, 22);
                player.sendMessage("You skilfully swing across.", true);
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        player.setNextWorldTile(toTile3);
                        stop();
                    }
                }, 1);
                return;
            }
            if (id == 24369 || id == 24370 || id == 24560) {
                handleGate(player, object);
                return;
            }
            if (id == 29316 || id == 29320) {
                handleGate(player, object);
                return;
            }
            if (id == 2788 || id == 2789) {
                final boolean isEntering1 = player.getX() >= 2504;
                player.addWalkSteps(objectX + (isEntering1 ? -1 : 0), objectY, 1, false);
                handleGate(player, object, 600);
                return;
            }
            if (id == 15604 || id == 15605) {
                final boolean isEntering2 = player.getX() >= 2555;
                player.addWalkSteps(objectX + (isEntering2 ? -1 : +1), objectY, 1, false);
                handleGate(player, object, 600);
                return;
            }
            if (id == 29099) {
                if (!Agility.hasLevel(player, 29)) {
                    return;
                }
                player.useStairs(1133, new WorldTile(2596, player.getY() <= 2869 ? 2871 : 2869, 0), 1, 2);
                return;
            }
            if (id == 3944 || id == 3945) {
                player.addWalkSteps(objectX, objectY + (player.getY() >= 3335 ? -1 : 1), -1, false);
            }
            if (id == 31149) {
                final boolean isEntering3 = player.getX() <= 3295;
                player.useStairs(isEntering3 ? 9221 : 9220, new WorldTile(objectX + (isEntering3 ? 1 : 0), objectY, 0), 1, 2);
                return;
            }
            if (id == 2333 || id == 2334 || id == 2335) {
                if (!Agility.hasLevel(player, 30)) {
                    return;
                }
                player.setNextAnimation(new Animation(741));
                player.setNextForceMovement(new NewForceMovement(object, 1, object, 2, Utils.getAngle(object.getX() - player.getX(), object.getY() - player.getY())));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        player.setNextWorldTile(object);
                    }
                });
            }
            if (id == 90223) {
                player.getActionManager().setAction(new DivineFishing(DivineFishing.DivineFishingSpots.DIVINE_CRAYFISH, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 90224) {
                player.getActionManager().setAction(new DivineFishing(DivineFishing.DivineFishingSpots.DIVINE_HERRING, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 90225) {
                player.getActionManager().setAction(new DivineFishing(DivineFishing.DivineFishingSpots.DIVINE_TROUT, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 90226) {
                player.getActionManager().setAction(new DivineFishing(DivineFishing.DivineFishingSpots.DIVINE_SALMON, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 90227) {
                player.getActionManager().setAction(new DivineFishing(DivineFishing.DivineFishingSpots.DIVINE_LOBSTER, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 90228) {
                player.getActionManager().setAction(new DivineFishing(DivineFishing.DivineFishingSpots.DIVINE_SWORDFISH, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 90229) {
                player.getActionManager().setAction(new DivineFishing(DivineFishing.DivineFishingSpots.DIVINE_SHARK, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 90230) {
                player.getActionManager().setAction(new DivineFishing(DivineFishing.DivineFishingSpots.DIVINE_CAVEFISH, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 90231) {
                player.getActionManager().setAction(new DivineFishing(DivineFishing.DivineFishingSpots.DIVINE_ROCKTAIL, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 87280) {
                player.getActionManager().setAction(new DivineHerblore(DivineHerblore.DivineHerbSpots.DIVINE_HERB_I, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 87281) {
                player.getActionManager().setAction(new DivineHerblore(DivineHerblore.DivineHerbSpots.DIVINE_HERB_II, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 87282) {
                player.getActionManager().setAction(new DivineHerblore(DivineHerblore.DivineHerbSpots.DIVINE_HERB_III, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 87270) {
                player.getActionManager().setAction(new DivineHunting(DivineHunting.DivineHuntingSpots.DIVINE_KEBBIT_BURROW, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 87271) {
                player.getActionManager().setAction(new DivineHunting(DivineHunting.DivineHuntingSpots.DIVINE_BIRD_SNARE, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 87272) {
                player.getActionManager().setAction(new DivineHunting(DivineHunting.DivineHuntingSpots.DIVINE_DEADFALL_TRAP, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 87273) {
                player.getActionManager().setAction(new DivineHunting(DivineHunting.DivineHuntingSpots.DIVINE_BOX_TRAP, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 66528) {
                player.getActionManager().setAction(new DivineSimulacrum(DivineSimulacrum.DivineSimulacrumSpots.DIVINE_SIMULACRUM_I, objectX, objectY, objectPlane, object));
                return;
            }
            if (id == 66531) {
                player.getActionManager().setAction(new DivineSimulacrum(DivineSimulacrum.DivineSimulacrumSpots.DIVINE_SIMULACRUM_II, objectX, objectY, objectPlane, object));
                return;
            }
            /**
             * Slayer Tower.
             */
            if (id == 82728) { /** Entrance **/
                if (player.getY() <= 3538) {
                    player.setNextWorldTile(new WorldTile(3423, 3540, 0));
                } else {
                    player.setNextWorldTile(new WorldTile(3423, 3538, 0));
                }
                return;
            }
            if (id == 82666 || id == 82669) { /** Corner staircase **/
                player.useStairs(-1, new WorldTile(player.getX(), player.getY(), 1), 0, 0);
                return;
            }
            if (id == 82488) { /** West Stairs - Up **/
                player.useStairs(-1, new WorldTile(3411, player.getY(), 3), 0, 0);
                return;
            }
            if (id == 82489) { /** East Stairs - Up **/
                player.useStairs(-1, new WorldTile(3435, player.getY(), 3), 0, 0);
                return;
            }
            if (id == 82490) { /** West Stairs - Down **/
                player.useStairs(-1, new WorldTile(3415, player.getY(), 2), 0, 0);
                return;
            }
            if (id == 82491) { /** East Stairs - Down **/
                player.useStairs(-1, new WorldTile(3431, player.getY(), 2), 0, 0);
                return;
            }
            if (id == 82605) { /** Cross Plank - shortcut **/
                if (player.getSkills().getLevel(Skills.AGILITY) < 71) {
                    player.sendMessage("You need a level of 71 Agility to use this shortcut.");
                    return;
                }
                if (player.getY() <= 3546) {
                    player.useStairs(-1, new WorldTile(3416, 3550, 2), 0, 0);
                    return;
                }
                player.useStairs(-1, new WorldTile(3416, 3545, 2), 0, 0);
                return;
            }
            if (id == 82432) {
                handleDoor(player, object);
                return;
            }
            /** End of Slayer Tower **/
            if (id == 2623) { // taverly dungeon door
                handleDoor(player, object);
                return;
            }
            if (id == 112747) {
                player.getDialogueManager().startDialogue("CompCape", object);
                return;
            }
            if (id == 9038 || id == 9039) {
                final WorldTile destination = new WorldTile((player.getX() <= 2816 ? 2817 : 2816), player.getY(), 0);
                player.useStairs(-1, destination, 0, 0);
                return;
            }
            if (id == 75491) {
                player.useStairs(-1, new WorldTile(3088, 3492, 0), 0, 0);
                return;
            }
            if (id == 67968 || id == 94067) {
                RobustGlass.addSandstone(player);
                return;
            }
            if (id == 63093) { // enter poly cave
                player.useStairs(832, new WorldTile(4620, 5458, 3), 2, 2);
                return;
            }
            if (id == 114205) { // enter artisans DS
                player.useStairs(-1, new WorldTile(3067, 9710, 0), 1, 1);
                return;
            }
            if (id == 114204) { // enter artisans DS
                player.useStairs(-1, new WorldTile(3067, 9710, 0), 1, 1);
                return;
            }
            if (id == 114203) { // enter artisans DS
                player.useStairs(-1, new WorldTile(3067, 9710, 0), 1, 1);
                return;
            }
            if (id == 114523) { // enter artisans UP
                player.useStairs(-1, new WorldTile(3061, 3335, 0), 1, 1);
                return;
            }
            if (id == 75882) {
                if (player.getX() >= 3335) {
                    player.useStairs(-1, new WorldTile(3332, player.getY(), 0), 0, 0);
                } else {
                    player.useStairs(-1, new WorldTile(3335, player.getY(), 0), 0, 0);
                }
                return;
            }

            if (id == 48496) {
                player.getDungeoneeringManager().enterDungeon(true, false, false);
                return;
            }



            if (id == 2408) {
                player.useStairs(828, new WorldTile(2823, 9771, 0), 1, 1);
                return;
            }
            // nomads requiem
            if (id == 18425 && !player.getQuestManager().completedQuest(Quests.NOMADS_REQUIEM)) {
                NomadsRequiem.enterNomadsRequiem(player);
                // player.sendMessage("Nomad's Requiem is currently
                // disabled, sorry.");
            }
            if (id == 42219) {
                if (player.getFamiliar() != null) {
                    player.getFamiliar().dissmissFamiliar(false);
                    player.getFamiliar().sendDeath(null);
                }

                player.getControlerManager().startControler("AreaController");
                player.useStairs(-1, new WorldTile(1886, 3178, 0), 0, 1);
                if (player.getQuestManager().getQuestStage(Quests.NOMADS_REQUIEM) == -2) {
                    player.getQuestManager().setQuestStageAndRefresh(Quests.NOMADS_REQUIEM, 0);
                }
            }
            if (id == 63094) { // exit poly cave
                player.useStairs(832, new WorldTile(3410, 3329, 0), 2, 2);
                return;
            }
            // Rise of the six - jump in well object
            if (id == 87997) {
                player.getDialogueManager().startDialogue("RiseOfTheSixEnterD");
                return;
            }

            if (player.getTreasureTrails().useObject(object)) {
                return;
            }



            if (BrimhDungeon.handleObjects(player, id)) {
                return;
            }
            if (id == 51061 || id == 8749 || id == 61336 || id == 97069) {
                final int maxPrayer1 = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
                if (player.getPrayer().getPrayerpoints() < maxPrayer1) {
                    player.setNextAnimation(new Animation(645));
                    player.getPrayer().restorePrayer(maxPrayer1);
                    player.sendMessage("You've recharged your prayer points.");
                }
                player.getDialogueManager().startDialogue("AltarSwapD");
                return;
            }
            /**
             * Multiple Hiscores scoreboard.
             */
            if (id == 30205) {
                player.getDialogueManager().startDialogue("ScoreboardD");
                return;
            }
            /**
             * Zanaris shed / Fairy rings.
             */
            if (id == 2406) {
                if (FairyRing.checkAll(player)) {
                    player.useStairs(-1, new WorldTile(2452, 4473, 0), 1, 2);
                } else {
                    handleDoor(player, object);
                }
                return;
            }
            if (id == 16944) {
                FairyRing.openRingInterface(player, object, false);
                return;
            }
            /**
             * fremennik dungeon
             */
            if (id == 44339) {
                if (player.getSkills().getLevelForXp(Skills.AGILITY) >= 81) {
                    if (player.getX() >= 2775) {
                        player.setNextWorldTile(new WorldTile(player.getX() - 7, player.getY(), player.getPlane()));
                    } else {
                        player.setNextWorldTile(new WorldTile(player.getX() + 7, player.getY(), player.getPlane()));
                    }
                } else {
                    player.sendMessage("You need at least a level of 81 agility to use this shortcut.");
                }
                return;
            }
            if (id == 77052) {
                if (player.getSkills().getLevelForXp(Skills.AGILITY) >= 62) {
                    if (player.getX() <= 2730) {
                        player.setNextWorldTile(new WorldTile(player.getX() + 5, player.getY(), player.getPlane()));
                    } else {
                        player.setNextWorldTile(new WorldTile(player.getX() - 5, player.getY(), player.getPlane()));
                    }
                } else {
                    player.sendMessage("You need at least a level of 62 agility to use this shortcut.");
                }
                return;
            }
            /**
             * Ancient cavern stairs.
             */
            if (id == 25339) {
                player.setNextWorldTile(new WorldTile(1778, 5343, 1));
                return;
            }
            if (id == 25340) {
                player.setNextWorldTile(new WorldTile(1778, 5346, 0));
                return;
            }
            if (id == 25336) {
                player.setNextWorldTile(new WorldTile(1768, 5366, 1));
                return;
            } else if (id == 24991) {
                player.getControlerManager().startControler("PuroPuro");
            } else if (id == 2873 || id == 2874 || id == 2875) {
                player.sendMessage("You kneel and begin to chant to " + objectDef.name.replace("Statue of ", "") + "...");
                player.setNextAnimation(new Animation(645));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        player.getDialogueManager().startDialogue("SimpleMessage", "You feel a rush of energy charge through your veins. " + "Suddenly a cape appears before you.");
                        World.addGroundItem(new Item(id == 2873 ? 2412 : id == 2874 ? 2414 : 2413), new WorldTile(object.getX(), object.getY() - 1, 0));
                    }
                }, 3);
            }
            if (object.getId() == 77834) {
                player.getDialogueManager().startDialogue("WarningD", DoomsayerManager.NORMAL_WARNING, 36,
                        "This artefact will transport you directly to the King Black Dragon's Lair. The King Black Dragon's Lair is extremely <col=ff0000>dangerous</col>. Are you sure you want to continue?<br><br>(A Combat level of 50+ is recommended)",
                        null, (Runnable) () -> {
                            Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(2273, 4681, 0));
                            WorldObject artefact = (WorldObject) player.getTemporaryAttributtes().remove("kbd");
                            WorldTasksManager.schedule(new WorldTask() {
                                @Override
                                public void run() {
                                    player.faceObject(artefact);
                                }
                            }, 2);
                        });
                player.getTemporaryAttributtes().put("kbd", object);
                return;
            } else if (id >= 65616 && id <= 65622) {
                WildernessObelisk.activateObelisk(id, player);
            } else if (id == 69827) {
                player.activateLodeStone(object, player);
            } else if (id == 69828) {
                player.activateLodeStone(object, player);
            } else if (id == 69829) {
                player.activateLodeStone(object, player);
            } else if (id == 69830) {
                player.activateLodeStone(object, player);
            } else if (id == 69831) {
                player.activateLodeStone(object, player);
            } else if (id == 69832) {
                player.activateLodeStone(object, player);
            } else if (id == 69833) {
                player.activateLodeStone(object, player);
            } else if (id == 69834) {
                player.activateLodeStone(object, player);
            } else if (id == 69835) {
                player.activateLodeStone(object, player);
            } else if (id == 69837) {
                player.activateLodeStone(object, player);
            } else if (id == 69838) {
                player.activateLodeStone(object, player);
            } else if (id == 69839) {
                player.activateLodeStone(object, player);
            } else if (id == 69840) {
                player.activateLodeStone(object, player);
            } else if (id == 69841) {
                player.activateLodeStone(object, player);
            }
            if (player.getFarmingManager().isFarming(object, null, 1)) {
                return;
            }
            if (object.getId() == 48189 || object.getId() == 42891) {
                player.setNextWorldTile(new WorldTile(2736, 3731, 0));
                return;
            }
            if (object.getId() == 42793) {
                player.setNextWorldTile(new WorldTile(3485, 5511, 0));
                return;
            }
            if (object.getId() == 48188) {
                player.setNextWorldTile(new WorldTile(3435, 5646, 0));
                return;
            }
            if (id == 24360) {
                player.setNextWorldTile(new WorldTile(3190, 9833, 0));
                return;
            }
            if (id == 24365) {
                player.setNextWorldTile(new WorldTile(3188, 3433, 0));
                return;
            }
            if (object.getId() == 66796) {
                player.setNextWorldTile(new WorldTile(2840, 3534, 2));
                return;
            }
            if (TrapAction.isTrap(player, object, id) || TrapAction.isTrap(player, object)) {
                return;
            }
            if (object.getId() == 77834) {
                player.getDialogueManager().startDialogue("KbdTeleport", object);
                return;
            }
            if (object.getId() == 52860) {
                if (player.getSkills().getLevelForXp(Skills.MINING) < 75) {
                    player.sendMessage("You need atleast 75 mining to enter that dungeon.");
                    return;
                }
                player.setNextWorldTile(new WorldTile(1181, 4515, 0));
                return;
            }
            if (object.getId() == 52872) {
                player.setNextWorldTile(new WorldTile(3298, 3307, 0));
                return;
            }
            if (id == 62677) {
                player.getDominionTower().talkToFace();
                return;
            }

            if (id == 38669) {
                ShootingStar.openNoticeboard(player);
                return;
            }
            if (id == 25591 || id == 7092) {
                ShootingStar.openTelescope(player);
                return;
            }
            if (object.getId() == 16543) {
                /*
                 * if (player.getSkills().getLevelForXp(Skills.THIEVING) < 85) {
                 * player.sendMessage("You need atleast 85 thieving to enter the pyramid.");
                 * return; } player.getControlerManager().startControler("PyramidPlunder");
                 */
                player.sendMessage("Disabled for the time being, sorry for the inconvinience.");
                return;
            }
            // Reaper portal to home
            if (object.getId() == 96782) {
                player.lock();
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        FadingScreen.fade(player, 0, () -> {
                            player.unlock();
                            player.setNextWorldTile(player.getHomeTile());
                        });
                    }
                }, 0);
                return;
            }
            // Enter reaper portal
            if (object.getId() == 92120) {
                player.lock();
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        FadingScreen.fade(player, 0, () -> {
                            player.unlock();
                            player.setNextWorldTile(new WorldTile(414, 652, 0));
                        });
                    }
                }, 0);
                return;
            }
            if (id == 9312 || id == 9311) {
                if (player.getSkills().getLevel(Skills.AGILITY) < 21) {
                    player.sendMessage("You need 21 agility to use this shortcut.");
                    return;
                }
                final WorldTile toTile1 = new WorldTile(3141, 3515, player.getPlane());
                final WorldTile toTile2 = new WorldTile(object.getId() == 9312 ? 3139 : 3143, object.getId() == 9312 ? 3517 : 3513, player.getPlane());
                player.lock(4);
                player.setNextAnimation(new Animation(2589));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.setNextWorldTile(toTile1);
                        player.setNextAnimation(new Animation(2590));
                        stop();
                    }
                }, 2);
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.setNextWorldTile(toTile2);
                        player.setNextAnimation(new Animation(2591));
                        stop();
                    }
                }, 4);
                return;
            }
            if (id == 39468) {
                player.setNextWorldTile(new WorldTile(1745, 5325, 0));
                return;
            }
            if (id == 26341) {
                player.useStairs(828, new WorldTile(2882, 5311, 0), 0, 0);
                player.getControlerManager().startControler("GodWars");
                return;
            }
            if (id == 25337) {
                player.setNextWorldTile(new WorldTile(1694, 5296, 1));
                return;
            }
            if (id == 25338) {
                player.setNextWorldTile(new WorldTile(1772, 5366, 0));
                return;
            }

            if (id == 3219) {
                player.getPorts().enterPorts();
                return;
            }
            if (id == 70812) {
                player.getControlerManager().startControler("QueenBlackDragonControler");
                return;
            }
            if (id == 47237) {
                if (player.getSkills().getLevel(Skills.AGILITY) < 90) {
                    player.sendMessage("You need 90 agility to use this shortcut.");
                    return;
                }
                if (player.getX() == 1641 && player.getY() == 5260 || player.getX() == 1641 && player.getY() == 5259 || player.getX() == 1640 && player.getY() == 5259) {
                    player.setNextWorldTile(new WorldTile(1641, 5268, 0));
                } else {
                    player.setNextWorldTile(new WorldTile(1641, 5260, 0));
                }
            }
            if (id == 66115 || id == 66116) {
                player.resetWalkSteps();
                player.setNextAnimation(new Animation(830));
                player.lock();
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.unlock();
                        if (Barrows.dig(player)) {
                            return;
                        }
                        player.sendMessage("You find nothing.");
                    }
                });
                return;
            }
            if (id == 47232) {
                player.setNextWorldTile(new WorldTile(1661, 5257, 0));
                return;
            }
            if (id == 2473) {
                if (objectX == 2400 && objectY == 4834 && objectPlane == 0) {
                    player.setNextWorldTile(new WorldTile(2869, 3017, 0));
                } else {
                    player.setNextWorldTile(new WorldTile(3039, 4834, 0));
                }
                return;
            }
            if (id == 2460) {
                if (objectX == 2868 && objectY == 3018 && objectPlane == 0) {
                    player.setNextWorldTile(new WorldTile(2400, 4835, 0));
                }
                return;
            }
            if (id == 2474) {
                if (objectX == 2282 && objectY == 4737 && objectPlane == 0) {
                    player.setNextWorldTile(new WorldTile(3058, 3590, 0));
                } else if (objectX == 2521 && objectY == 4833 && objectPlane == 0) {
                    player.setNextWorldTile(new WorldTile(3051, 3445, 0));
                } else if (objectX == 2576 && objectY == 4846 && objectPlane == 0) {
                    player.setNextWorldTile(new WorldTile(3311, 3256, 0));
                } else if (objectX == 3495 && objectY == 4832 && objectPlane == 0) {
                    player.setNextWorldTile(new WorldTile(3163, 3185, 0));
                } else if (objectX == 2841 && objectY == 4828 && objectPlane == 0) {
                    player.setNextWorldTile(new WorldTile(3128, 3403, 0));
                }
                return;
            }
            if (id == 2466) {
                if (objectX == 2793 && objectY == 4827 && objectPlane == 0) {
                    player.setNextWorldTile(new WorldTile(2983, 3512, 0));
                }
                return;
            }

            if (id == 2461) {
                if (objectX == 3059 && objectY == 3590 && objectPlane == 0) {
                    player.setNextWorldTile(new WorldTile(2281, 4837, 0));
                }
                return;
            }
            if (id == 2471) {
                player.setNextWorldTile(new WorldTile(2410, 4377, 0));
                return;
            }
            if (id == 2458) {
                if (objectX == 2407 && objectY == 4376 && objectPlane == 0) {
                    player.setNextWorldTile(new WorldTile(2162, 4833, 0));
                }
                return;
            }
            if (id == 64294) {
                int jumpStage1;
                if (player.getX() == 4685 && player.getY() == 5476) {
                    jumpStage1 = 4685 - 4;
                } else {
                    jumpStage1 = 4658 + 4;
                }
                final WorldTile toTile4 = new WorldTile(jumpStage1, player.getY(), player.getPlane());
                player.lock(4);
                player.setNextAnimation(new Animation(15461));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.setNextForceMovement(new ForceMovement(player, 0, toTile4, 2, ForceMovement.EAST));
                        player.setNextWorldTile(toTile4);
                        player.setNextAnimation(new Animation(-1));
                        player.unlock();
                        stop();
                    }
                }, 2);
                return;
            }
            if (id == 64295) {
                int jumpStage2;
                if (player.getX() == 4681 && player.getY() == 5476) {
                    jumpStage2 = 4681 + 4;
                } else {
                    jumpStage2 = 4663 - 5;
                }
                final WorldTile toTile5 = new WorldTile(jumpStage2, player.getY(), player.getPlane());
                player.lock(4);
                player.setNextAnimation(new Animation(15461));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.setNextForceMovement(new ForceMovement(player, 0, toTile5, 2, ForceMovement.EAST));
                        player.setNextWorldTile(toTile5);
                        player.setNextAnimation(new Animation(-1));
                        player.unlock();
                        stop();
                    }
                }, 2);
                return;
            }
            if (id == 64360) {
                player.lock(3);
                player.setNextAnimation(new Animation(15458));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.setNextAnimation(new Animation(15457));
                        player.setNextWorldTile(new WorldTile(4630, 5452, 2));
                        player.setNextAnimation(new Animation(-1));
                        player.unlock();
                        stop();
                    }
                }, 1);
            }
            if (id == 64359) {
                player.lock(3);
                player.setNextAnimation(new Animation(15458));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.setNextAnimation(new Animation(15457));
                        player.setNextWorldTile(new WorldTile(player.getX(), player.getY(), player.getPlane() - 1));
                        player.setNextAnimation(new Animation(-1));
                        player.unlock();
                        stop();
                    }
                }, 1);
                return;
            }
            if (id == 64361) {
                player.lock(3);
                player.setNextAnimation(new Animation(15456));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        if (player.getX() == 4630 && player.getY() == 5452 || player.getX() == 4628 && player.getY() == 5451) {
                            player.setNextWorldTile(new WorldTile(4629, 5454, 3));
                        } else {
                            player.setNextWorldTile(new WorldTile(player.getX(), player.getY(), player.getPlane() + 1));
                        }
                        player.setNextWorldTile(new WorldTile(player.getX(), player.getY(), player.getPlane() + 1));
                        player.setNextAnimation(new Animation(-1));
                        player.unlock();
                        stop();
                    }
                }, 1);
                return;
            }
            if (id == 12266) {
                player.getDialogueManager().startDialogue("SimplePlayerMessage", "This trapdoor seems to be stuck..");
                return;
            }
            if (id == 44392) {
                player.getDialogueManager().startDialogue("SimplePlayerMessage", "Books.. books... boooks.... Well, this is quite boring.");
                return;
            }
            if (id == 12349 || id == 12350 || id == 7315) {
                if (!player.hasTalkedtoCook()) {
                    player.getDialogueManager().startDialogue("SimplePlayerMessage", "I should probably talk to the Cook at home before venturing in here.");
                    return;
                }
                if (player.isKilledCulinaromancer()) {
                    player.getDialogueManager().startDialogue("RfdPortal", true);
                    return;
                }
                player.getControlerManager().startControler("ImpossibleJadControler");
                return;
            }
            if (id == 15653) {// warriors guild entrance
                if (player.getX() >= 2877) {
                    WarriorsGuild.canEnter(player);
                } else {
                    WarriorsGuild.exit(player);
                    player.getControlerManager().forceStop();
                }
                return;
            }
            if (id == 34234) {
                player.sendMessage("This door seems to be stuck; I should try the other one.");
                return;
            }
            if (id == 22119) {
                player.getControlerManager().startControler("BarrelchestControler");
                return;
            }
            /**
             * Port objects.
             */
            if (id == 81427) { // Stairs NE Building Northside - up
                player.useStairs(-1, new WorldTile(4078, 7294, 1), 0, 0);
                return;
            }
            if (id == 81428) { // Stairs NE Building Northside - down
                player.useStairs(-1, new WorldTile(4081, 7291, 0), 0, 0);
                return;
            }
            if (id == 81198) { // Stairs NE Building Eastside - up
                player.useStairs(-1, new WorldTile(4091, 7285, 1), 0, 0);
                return;
            }
            if (id == 81199 && (object.getX() == 4091 && object.getY() == 7267 && object.getPlane() == 1)) {
                player.useStairs(-1, new WorldTile(4093, 7266, 0), 0, 0); // SE
                // building
                // down
            }
            if (id == 81199 && (object.getX() == 4092 && object.getY() == 7283 && object.getPlane() == 1)) {
                player.useStairs(-1, new WorldTile(4094, 7282, 0), 0, 0);// NE
                // building
                // down
                return;
            }
            if (id == 81181 && (object.getX() == 4091 && object.getY() == 7267 && object.getPlane() == 0)) { // Stairs
                // SE
                // Building
                // Eastside
                // -
                // up
                player.useStairs(-1, new WorldTile(4090, 7269, 1), 0, 0);
                return;
            }
            if (id == 81124) { // ladder SW building up
                player.useStairs(-1, new WorldTile(4039, 7275, 1), 0, 0);
                return;
            }
            if (id == 81125) { // ladder SW building down
                player.useStairs(-1, new WorldTile(4038, 7276, 0), 0, 0);
                return;
            }
            if (id == 81214) { // Stairs bar up
                player.useStairs(-1, new WorldTile(4052, 7293, 1), 0, 0);
                return;
            }
            if (id == 81215) { // Stairs bar down
                player.useStairs(-1, new WorldTile(4049, 7290, 0), 0, 0);
                return;
            }
            if (id == 91557) {
                player.useStairs(-1, new WorldTile(4512, 6289, 1), 1, 2);
                return;
            }
            if (id == 91500) {
                player.getDialogueManager().startDialogue("SpiderBossInstanceD", Boss.Spider_Boss);
                return;
            }
            // arraxor exit cave
            if (id == 91553) {
                player.useStairs(828, new WorldTile(3699, 3420, 0), 1, 2);
                return;
            }
            if (id == 91661) {// arraxor gap
                player.lock();
                player.setNextAnimation(new Animation(10738));
                final WorldTile toTile = (player.getX() > object.getX() ? new WorldTile(object.getX() - 1, object.getY() + 1, object.getPlane()) : new WorldTile(object.getX() + 4, object.getY() + 2, object.getPlane()));
                player.setNextForceMovement(new ForceMovement(player, 1, toTile, 3, player.getX() > object.getX() ? ForceMovement.WEST : ForceMovement.EAST));
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        player.setNextWorldTile(toTile);
                        player.unlock();
                    }

                }, (AnimationDefinitions.getAnimationDefinitions(10738).getEmoteClientCycles() / 30));
                return;
            }
            if (id == 2403) {
                ShopsDataParser.openShop(player, player.isKilledCulinaromancer() ? 45 : player.isKilledDessourt() ? 44 : player.isKilledKaramel() ? 43 : player.isKilledFlambeed() ? 42 : player.isKilledAgrithNaNa() ? 41 : 40);
                return;
            }
            if (id == 47231) {
                player.setNextWorldTile(new WorldTile(1685, 5287, 1));
            }
            if (id == 61584) {
                if (player.getX() == 4205 && player.getY() == 5751) {
                    player.addWalkSteps(4206, 5751, 1, false);
                } else if (player.getX() == 4206 && player.getY() == 5751) {
                    player.addWalkSteps(4205, 5751, 1, false);
                }
            }
            if (id == 47236) {
                if (player.getX() >= 1657 && player.getX() <= 1659 && player.getY() == 5303) {
                    player.addWalkSteps(player.getX(), player.getY() + 1, 1, false);
                }
                if (player.getX() >= 1657 && player.getX() <= 1659 && player.getY() == 5304) {
                    player.addWalkSteps(player.getX(), player.getY() - 1, 1, false);
                }
                if (player.getX() == 1650 && player.getY() == 5281 || player.getX() == 1651 && player.getY() == 5281 || player.getX() == 1650 && player.getY() == 5281) {
                    player.addWalkSteps(1651, 5280, 1, false);
                }
                if (player.getX() == 1652 && player.getY() == 5280 || player.getX() == 1651 && player.getY() == 5280 || player.getX() == 1653 && player.getY() == 5280) {
                    player.addWalkSteps(1651, 5281, 1, false);
                }
                if (player.getX() == 1650 && player.getY() == 5301 || player.getX() == 1650 && player.getY() == 5302 || player.getX() == 1650 && player.getY() == 5303) {
                    player.addWalkSteps(1649, 5302, 1, false);
                }
                if (player.getX() == 1649 && player.getY() == 5303 || player.getX() == 1649 && player.getY() == 5302 || player.getX() == 1649 && player.getY() == 5301) {
                    player.addWalkSteps(1650, 5302, 1, false);
                }
                if (player.getX() == 1626 && player.getY() == 5301 || player.getX() == 1626 && player.getY() == 5302 || player.getX() == 1626 && player.getY() == 5303) {
                    player.addWalkSteps(1625, 5302, 1, false);
                }
                if (player.getX() == 1625 && player.getY() == 5301 || player.getX() == 1625 && player.getY() == 5302 || player.getX() == 1625 && player.getY() == 5303) {
                    player.addWalkSteps(1626, 5302, 1, false);
                }
                if (player.getX() == 1609 && player.getY() == 5289 || player.getX() == 1610 && player.getY() == 5289 || player.getX() == 1611 && player.getY() == 5289) {
                    player.addWalkSteps(1610, 5288, 1, false);
                }
                if (player.getX() == 1609 && player.getY() == 5288 || player.getX() == 1610 && player.getY() == 5288 || player.getX() == 1611 && player.getY() == 5288) {
                    player.addWalkSteps(1610, 5289, 1, false);
                }
                if (player.getX() == 1606 && player.getY() == 5265 || player.getX() == 1605 && player.getY() == 5265 || player.getX() == 1604 && player.getY() == 5265) {
                    player.addWalkSteps(1605, 5264, 1, false);
                }
                if (player.getX() == 1606 && player.getY() == 5264 || player.getX() == 1605 && player.getY() == 5264 || player.getX() == 1604 && player.getY() == 5264) {
                    player.addWalkSteps(1605, 5265, 1, false);
                }
                if (player.getX() == 1634 && player.getY() == 5254 || player.getX() == 1634 && player.getY() == 5253 || player.getX() == 1634 && player.getY() == 5252) {
                    player.addWalkSteps(1635, 5253, 1, false);
                }
                if (player.getX() == 1635 && player.getY() == 5254 || player.getX() == 1635 && player.getY() == 5253 || player.getX() == 1635 && player.getY() == 5252) {
                    player.addWalkSteps(1634, 5253, 1, false);
                }
                return;
            }
            if (id == 47233) {
                if (player.getSkills().getLevel(Skills.AGILITY) < 80) {
                    player.sendMessage("You need 80 agility to use this shortcut.");
                    return;
                }
                if (player.getX() == 1633 && player.getY() == 5294) {
                    return;
                }
                player.lock(3);
                player.setNextAnimation(new Animation(4853));
                final WorldTile toTile6 = new WorldTile(object.getX(), object.getY() + 1, object.getPlane());
                player.setNextForceMovement(new ForceMovement(player, 0, toTile6, 2, ForceMovement.EAST));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.setNextWorldTile(toTile6);
                    }
                }, 1);
            }
            if (id == 29958) {
                if (player.getSkills().getLevel(23) < player.getSkills().getLevelForXp(23)) {
                    player.lock(5);
                    player.sendMessage("You feel the obelisk", true);
                    player.setNextAnimation(new Animation(8502));
                    player.setNextGraphics(new Graphics(1308));
                    WorldTasksManager.schedule(new WorldTask() {

                        @Override
                        public void run() {
                            player.getSkills().restoreSummoning();
                            player.sendMessage("...and recharge all your skills.", true);
                        }
                    }, 2);
                }
                return;
            } else if (id == 2350 && (object.getX() == 3352 && object.getY() == 3417 && object.getPlane() == 0)) {
                player.useStairs(832, new WorldTile(3177, 5731, 0), 1, 2);
            } else if (id == 2353 && (object.getX() == 3177 && object.getY() == 5730 && object.getPlane() == 0)) {
                player.useStairs(828, new WorldTile(3353, 3416, 0), 1, 2);
            } else if (id == 11554 || id == 11552) {
                player.sendMessage("That rock is currently unavailable.");
            } else if (id == 38279) {
                player.getDialogueManager().startDialogue("RunespanPortalD");
            } else if (id == 2491) {
                player.getActionManager().setAction(new EssenceMining(object, player.getSkills().getLevel(Skills.MINING) < 30 ? EssenceDefinitions.Rune_Essence : EssenceDefinitions.Pure_Essence));
            } else if (id == 2481) { // earth
                RuneCrafting.craftEssenceLoop(player, 557, 9, 6.5, false, RC_DELAY_TICKS,
                    RuneCrafting.EARTH_RUNE_MULTIPLIERS);

        } else if (id == 2478) { // Air
            RuneCrafting.craftEssenceLoop(player, 556, 1, 5, false, RC_DELAY_TICKS,
                    RuneCrafting.AIR_RUNE_MULTIPLIERS);

        } else if (id == 2479) { // Mind
            RuneCrafting.craftEssenceLoop(player, 558, 1, 5.5, false, RC_DELAY_TICKS,
                    RuneCrafting.MIND_RUNE_MULTIPLIERS);

        } else if (id == 2480) { // Water
            RuneCrafting.craftEssenceLoop(player, 555, 5, 6, false, RC_DELAY_TICKS,
                    RuneCrafting.WATER_RUNE_MULTIPLIERS);

        } else if (id == 2482) { // Fire
            RuneCrafting.craftEssenceLoop(player, 554, 14, 7, false, RC_DELAY_TICKS,
                    RuneCrafting.FIRE_RUNE_MULTIPLIERS);

        } else if (id == 2483) { // Body
            RuneCrafting.craftEssenceLoop(player, 559, 20, 7.5, false, RC_DELAY_TICKS,
                    RuneCrafting.BODY_RUNE_MULTIPLIERS);

        } else if (id == 2484) { // Cosmic (pure only)
            RuneCrafting.craftEssenceLoop(player, 564, 27, 8, true, RC_DELAY_TICKS,
                    RuneCrafting.COSMIC_RUNE_MULTIPLIERS);

        } else if (id == 2487) { // Chaos (pure only)
            RuneCrafting.craftEssenceLoop(player, 562, 35, 8.5, true, RC_DELAY_TICKS,
                    RuneCrafting.CHAOS_RUNE_MULTIPLIERS);

        } else if (id == 17010) { // Astral (pure only)
            RuneCrafting.craftEssenceLoop(player, 9075, 40, 8.7, true, RC_DELAY_TICKS,
                    RuneCrafting.ASTRAL_RUNE_MULTIPLIERS);

        } else if (id == 2486) { // nature
            RuneCrafting.craftEssenceLoop(player, 561, 44, 9, true, RC_DELAY_TICKS,
                    RuneCrafting.NATURE_RUNE_MULTIPLIERS);

        } else if (id == 2485) { // law
            RuneCrafting.craftEssenceLoop(player, 563, 54, 9.5, true, RC_DELAY_TICKS,
                    RuneCrafting.LAW_RUNE_MULTIPLIERS);

        } else if (id == 2488) { // death
            RuneCrafting.craftEssenceLoop(player, 560, 65, 10, true, RC_DELAY_TICKS,
                    RuneCrafting.DEATH_RUNE_MULTIPLIERS);

        } else if (id == 30624) { // blood
            RuneCrafting.craftEssenceLoop(player, 565, 77, 10.5, true, RC_DELAY_TICKS,
                    RuneCrafting.BLOOD_RUNE_MULTIPLIERS);

        } else if (id == 26847) {
            RuneCrafting.craftSoulRunes(player);
        } else if (id == 2452) {
                final int hatId1 = player.getEquipment().getHatId();
                if (hatId1 == RuneCrafting.AIR_TIARA || hatId1 == RuneCrafting.OMNI_TIARA || player.getInventory().containsItem(1438, 1)) {
                    RuneCrafting.enterAirAltar(player);
                }
            } else if (id == 2455) {
                final int hatId2 = player.getEquipment().getHatId();
                if (hatId2 == RuneCrafting.EARTH_TIARA || hatId2 == RuneCrafting.OMNI_TIARA || player.getInventory().containsItem(1440, 1)) {
                    RuneCrafting.enterEarthAltar(player);
                }
            } else if (id == 2456) {
                final int hatId3 = player.getEquipment().getHatId();
                if (hatId3 == RuneCrafting.FIRE_TIARA || hatId3 == RuneCrafting.OMNI_TIARA || player.getInventory().containsItem(1442, 1)) {
                    RuneCrafting.enterFireAltar(player);
                }
            } else if (id == 2454) {
                final int hatId4 = player.getEquipment().getHatId();
                if (hatId4 == RuneCrafting.WATER_TIARA || hatId4 == RuneCrafting.OMNI_TIARA || player.getInventory().containsItem(1444, 1)) {
                    RuneCrafting.enterWaterAltar(player);
                }
            } else if (id == 2457) {
                final int hatId5 = player.getEquipment().getHatId();
                if (hatId5 == RuneCrafting.BODY_TIARA || hatId5 == RuneCrafting.OMNI_TIARA || player.getInventory().containsItem(1446, 1)) {
                    RuneCrafting.enterBodyAltar(player);
                }
            } else if (id == 2453) {
                final int hatId6 = player.getEquipment().getHatId();
                if (hatId6 == RuneCrafting.MIND_TIARA || hatId6 == RuneCrafting.OMNI_TIARA || player.getInventory().containsItem(1448, 1)) {
                    RuneCrafting.enterMindAltar(player);
                }
            } else if (id == 47120) { // zaros altar
                // recharge if needed
                if (player.getPrayer().getPrayerpoints() < player.getSkills().getLevelForXp(Skills.PRAYER) * 10) {
                    player.lock(12);
                    player.setNextAnimation(new Animation(12563));
                    player.getPrayer().setPrayerpoints((int) ((player.getSkills().getLevelForXp(Skills.PRAYER) * 10) * 1.15));
                    player.getPrayer().refreshPrayerPoints();
                }
                player.getDialogueManager().startDialogue("ZarosAltar");
            } else if (id == 19222) {
                if (player.getY() >= 3621) {
                    Falconry.beginFalconry(player);
                }
            } else if (id == 36786) {
                player.getDialogueManager().startDialogue("Banker", 4907);
            } else if (id == 42377 || id == 42378) {
                player.getDialogueManager().startDialogue("Banker", 2759);
            } else if (id == 42217 || id == 782 || id == 34752) {
                player.getDialogueManager().startDialogue("Banker", 553);
            } else if (id == 57437 || id == 104002) {
                if (!player.promptList()) {
                    player.getBank().openBank();
                } else {
                    player.getDialogueManager().startDialogue("BankList", false);
                }
            } else if (id == 42425 && object.getX() == 3220 && object.getY() == 3222) { // zaros
                // portal
                player.useStairs(10256, new WorldTile(3353, 3416, 0), 4, 5, "And you find yourself into a digsite.");
                player.addWalkSteps(3222, 3223, -1, false);
                player.sendMessage("You examine portal and it aborves you...");
            } else if (id == 9356) {
                player.lock(3);
                FightCaves.enterFightCaves(player, false);
                return;
            }
            if (id == 68107) {
                FightKiln.enterFightKiln(player, false);
            } else if (id == 46500 && object.getX() == 3351 && object.getY() == 3415) { // zaros
                // portal
                player.useStairs(-1, new WorldTile(player.getHomeTile().getX(), player.getHomeTile().getY(), player.getHomeTile().getPlane()), 2, 3, "You found your way back to home.");
                player.addWalkSteps(3351, 3415, -1, false);
                /**
                 * Taverley Dungeon.
                 */
            } else if (id == 74866) {
                player.useStairs(828, new WorldTile(2842, 3423, 0), 1, 1);
            } else if (id == 74867) {
                player.useStairs(828, new WorldTile(2842, 9825, 0), 1, 1);
            } else if (id == 77098) {
                final int x1 = player.getX() <= 2897 ? 2899 : 2897;
                player.setNextWorldTile(new WorldTile(x1, player.getY(), 0));
            } else if (id == 9293) {
                if (player.getSkills().getLevel(Skills.AGILITY) < 70) {
                    player.sendMessage("You need an agility level of 70 to use this obstacle.", true);
                    return;
                }
                final int x2 = player.getX() == 2886 ? 2892 : 2886;
                player.useStairs(844, new WorldTile(x2, 9799, 0), 1, 1, "You've successfully squeeze through the pipe.");
            } else if (id == 9294) {
                if (player.getSkills().getLevel(Skills.AGILITY) < 80) {
                    player.sendMessage("You need an agility level of 80 to use this obstacle.", true);
                    return;
                }
                if (player.getX() == 2880 && player.getY() == 9814) {
                    player.useStairs(1603, new WorldTile(2878, 9812, 0), 1, 1, "You've successfully jumped over the strange floor.");
                } else {
                    player.useStairs(1603, new WorldTile(2880, 9814, 0), 1, 1, "You've successfully jumped over the strange floor.");
                }
            } else if (id >= 15477 && id <= 15482) {
                player.getDialogueManager().startDialogue("HousePortal");
            } else if (id == 74864) {
                player.useStairs(-1, new WorldTile(2885, 3395, 0), 0, 0);
            } else if (id == 66991) {
                player.useStairs(-1, new WorldTile(2885, 9795, 0), 0, 0);
            } else if (id == 29370 && (object.getX() == 3150 || object.getX() == 3153) && object.getY() == 9906) { // edgeville
                // dung
                // cut
                if (player.getSkills().getLevel(Skills.AGILITY) < 53) {
                    player.sendMessage("You need an agility level of 53 to use this obstacle.");
                    return;
                }
                final boolean running = player.getRun();
                player.setRunHidden(false);
                player.lock(8);
                player.addWalkSteps(objectX == 3150 ? 3155 : 3149, 9906, -1, false);
                player.sendMessage("You pulled yourself through the pipes.", true);
                WorldTasksManager.schedule(new WorldTask() {
                    boolean secondloop;

                    @Override
                    public void run() {
                        if (!secondloop) {
                            secondloop = true;
                            player.getAppearence().setRenderEmote(295);
                        } else {
                            player.getAppearence().setRenderEmote(-1);
                            player.setRunHidden(running);
                            player.getSkills().addXp(Skills.AGILITY, 7);
                            stop();
                        }
                    }
                }, 0, 5);
            }
            // start forinthry dungeon
            else if (id == 18341 && object.getX() == 3036 && object.getY() == 10172) {
                player.useStairs(-1, new WorldTile(3039, 3765, 0), 0, 1);
            } else if (id == 20599 && object.getX() == 3038 && object.getY() == 3761) {
                player.useStairs(-1, new WorldTile(3037, 10171, 0), 0, 1);
            } else if (id == 18342 && object.getX() == 3075 && object.getY() == 10057) {
                player.useStairs(-1, new WorldTile(3071, 3649, 0), 0, 1);
            } else if (id == 20600 && object.getX() == 3072 && object.getY() == 3648) {
                player.useStairs(-1, new WorldTile(3077, 10058, 0), 0, 1);
            } else if (id == 8689) {
                player.getActionManager().setAction(new CowMilkingAction());
            } else if (id == 42220) {
                player.useStairs(-1, new WorldTile(3082, 3475, 0), 0, 1);
            } else if (id == 30942 && object.getX() == 3019 && object.getY() == 3450) {
                player.useStairs(828, new WorldTile(3020, 9850, 0), 1, 2);
            } else if (id == 6226 && object.getX() == 3019 && object.getY() == 9850) {
                player.useStairs(833, new WorldTile(3018, 3450, 0), 1, 2);
            } else if (id == 31002 && player.getQuestManager().completedQuest(Quests.PERIL_OF_ICE_MONTAINS)) {
                player.useStairs(833, new WorldTile(2998, 3452, 0), 1, 2);
            } else if (id == 31012 && player.getQuestManager().completedQuest(Quests.PERIL_OF_ICE_MONTAINS)) {
                player.useStairs(828, new WorldTile(2996, 9845, 0), 1, 2);
            } else if (id == 30943 && object.getX() == 3059 && object.getY() == 9776) {
                player.useStairs(-1, new WorldTile(3061, 3376, 0), 0, 1);
            } else if (id == 114255 && object.getX() == 3059 && object.getY() == 3376) {
                player.useStairs(-1, new WorldTile(3058, 9776, 0), 0, 1);
            } else if (id == 2112 && object.getX() == 3046 && object.getY() == 9756) {
                if (player.getSkills().getLevelForXp(Skills.MINING) < 60) {
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", MiningGuildDwarf.getClosestDwarfID(player), "Sorry, but you need level 60 Mining to go in there.");
                    return;
                }
                handleDoor(player, object);
            } else if (id == 2113) {
                if (player.getSkills().getLevelForXp(Skills.MINING) < 60) {
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", MiningGuildDwarf.getClosestDwarfID(player), "Sorry, but you need level 60 Mining to go in there.");
                    return;
                }
                player.useStairs(-1, new WorldTile(3021, 9739, 0), 0, 1);
            } else if (id == 6226 && object.getX() == 3019 && object.getY() == 9740) {
                player.useStairs(828, new WorldTile(3019, 3341, 0), 1, 2);
            } else if (id == 6226 && object.getX() == 3019 && object.getY() == 9738) {
                player.useStairs(828, new WorldTile(3019, 3337, 0), 1, 2);
            } else if (id == 6226 && object.getX() == 3018 && object.getY() == 9739) {
                player.useStairs(828, new WorldTile(3017, 3339, 0), 1, 2);
            } else if (id == 6226 && object.getX() == 3020 && object.getY() == 9739) {
                player.useStairs(828, new WorldTile(3021, 3339, 0), 1, 2);
            } else if (id == 30963) {
                if (!player.promptList()) {
                    player.getBank().openBank();
                } else {
                    player.getDialogueManager().startDialogue("BankList", false);
                }
            } else if (id == 6045) {
                player.sendMessage("You search the cart but find nothing.");
            } else if (id == 5906) {
                if (player.getSkills().getLevel(Skills.AGILITY) < 42) {
                    player.sendMessage("You need an agility level of 42 to use this obstacle.");
                    return;
                }
                player.lock();
                WorldTasksManager.schedule(new WorldTask() {
                    int count = 0;

                    @Override
                    public void run() {
                        if (count == 0) {
                            player.setNextAnimation(new Animation(2594));
                            final WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -2 : +2), object.getY(), 0);
                            player.setNextForceMovement(new ForceMovement(tile, 4, Utils.getMoveDirection(tile.getX() - player.getX(), tile.getY() - player.getY())));
                        } else if (count == 2) {
                            final WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -2 : +2), object.getY(), 0);
                            player.setNextWorldTile(tile);
                        } else if (count == 5) {
                            player.setNextAnimation(new Animation(2590));
                            final WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -5 : +5), object.getY(), 0);
                            player.setNextForceMovement(new ForceMovement(tile, 4, Utils.getMoveDirection(tile.getX() - player.getX(), tile.getY() - player.getY())));
                        } else if (count == 7) {
                            final WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -5 : +5), object.getY(), 0);
                            player.setNextWorldTile(tile);
                        } else if (count == 10) {
                            player.setNextAnimation(new Animation(2595));
                            final WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -6 : +6), object.getY(), 0);
                            player.setNextForceMovement(new ForceMovement(tile, 4, Utils.getMoveDirection(tile.getX() - player.getX(), tile.getY() - player.getY())));
                        } else if (count == 12) {
                            final WorldTile tile = new WorldTile(object.getX() + (object.getRotation() == 2 ? -6 : +6), object.getY(), 0);
                            player.setNextWorldTile(tile);
                        } else if (count == 14) {
                            stop();
                            player.unlock();
                        }
                        count++;
                    }

                }, 0, 0);
                // BarbarianOutpostAgility start
            } else if (id == 20210) {
                BarbarianOutpostAgility.enterObstaclePipe(player, object);
            } else if (id == 43526) {
                BarbarianOutpostAgility.swingOnRopeSwing(player, object);
            } else if (id == 43595 && objectX == 2550 && objectY == 3546) {
                BarbarianOutpostAgility.walkAcrossLogBalance(player, object);
            } else if (id == 20211 && objectX == 2538 && objectY == 3545) {
                BarbarianOutpostAgility.climbObstacleNet(player, object);
            } else if (id == 2302 && objectX == 2535 && objectY == 3547) {
                BarbarianOutpostAgility.walkAcrossBalancingLedge(player, object);
            } else if (id == 1948) {
                BarbarianOutpostAgility.climbOverCrumblingWall(player, object);
            } else if (id == 43533) {
                BarbarianOutpostAgility.runUpWall(player, object);
            } else if (id == 43597) {
                BarbarianOutpostAgility.climbUpWall(player, object);
            } else if (id == 43587) {
                BarbarianOutpostAgility.fireSpringDevice(player, object);
            } else if (id == 43527) {
                BarbarianOutpostAgility.crossBalanceBeam(player, object);
            } else if (id == 43531) {
                BarbarianOutpostAgility.jumpOverGap(player, object);
            } else if (id == 43532) {
                BarbarianOutpostAgility.slideDownRoof(player, object);
            } else if (id == 45077) {
                player.lock();
                if (player.getX() != object.getX() || player.getY() != object.getY()) {
                    player.addWalkSteps(object.getX(), object.getY(), -1, false);
                }
                WorldTasksManager.schedule(new WorldTask() {

                    private int count;

                    @Override
                    public void run() {
                        if (count == 0) {
                            player.setNextFaceWorldTile(new WorldTile(object.getX() - 1, object.getY(), 0));
                            player.setNextAnimation(new Animation(12216));
                            player.unlock();
                        } else if (count == 2) {
                            player.setNextWorldTile(new WorldTile(3651, 5122, 0));
                            player.setNextFaceWorldTile(new WorldTile(3651, 5121, 0));
                            player.setNextAnimation(new Animation(12217));
                        } else if (count == 5) {
                            player.unlock();
                            stop();
                        }
                        count++;
                    }

                }, 1, 0);
            } else if (id == 113081 || id == 113082 || (object.getDefinitions() != null && object.getDefinitions().name.replace("-", " ").equalsIgnoreCase("banite rock") && id != 113080)) {
                player.getActionManager().setAction(new Mining(object, RockDefinitions.BANE_ORE));
            } else if (id == 113010) {
                player.getActionManager().setAction(new Mining(object, RockDefinitions.LRC_Gold_Ore));
            } else if (id == 113009) {
                player.getActionManager().setAction(new Mining(object, RockDefinitions.LRC_Coal_Ore));
            } else if (id != 112998 && object.getDefinitions() != null && object.getDefinitions().name.replace("-", " ").toLowerCase().contains(" gem rock")) {
                player.getActionManager().setAction(new Mining(object, RockDefinitions.GEM_ROCK));
            } else if (id == 113011) {
                player.getActionManager().setAction(new Mining(object, RockDefinitions.Red_Sandstone));
            } else if (object.getDefinitions() != null && object.getDefinitions().name.replace("-", " ").equalsIgnoreCase("blurite rock")) {
                if (player.isGroupIronman()) {
                    player.sendMessage("Group Ironmen cannot mine this!");
                    return;
                }
                player.getActionManager().setAction(new Mining(object, RockDefinitions.Donor_Ore));
            } else if (id == 113080) {
                player.getActionManager().setAction(new Mining(object, RockDefinitions.Protean_Ore));
            }
            else if (id == 113020 || id == 113021 || id == 11302) {
                player.getActionManager().setAction(new Mining(object, RockDefinitions.DARK_ORE));
            }
            else if (id == 113181 || id == 113179 || id == 113116 || id == 113118) {
                player.getActionManager().setAction(new Mining(object, RockDefinitions.LUMINITE_ORE));
            }
            else if (id == 113128 || id == 113130) {
                player.getActionManager().setAction(new Mining(object, RockDefinitions.ORICHALCITE_ORE));
            }
            else if (id == 113208 || id == 113206 || id == 113207) {
                player.getActionManager().setAction(new Mining(object, RockDefinitions.NECRITE_ORE));
            }
            else if (id == 113138 || id == 113139 || id == 113137) {
                player.getActionManager().setAction(new Mining(object, RockDefinitions.PHASMATITE_ORE));
            }
            else if (id == 113131 || id == 113133 || id == 113132 || id == 113118 || id == 113072
                    || id == 113071 || id == 113073) {
                player.getActionManager().setAction(new Mining(object, RockDefinitions.DRAKOLITH_ORE));
            }
            else if (id == 113017 || id == 113018 || id == 113019) {
                player.getActionManager().setAction(new Mining(object, RockDefinitions.LIGHT_ORE));
            }else if (id == 27571) {
                player.getActionManager().setAction(new Mining(object, RockDefinitions.DZ_CRYSTAL));
            } else if (id == 28073) {
                player.getActionManager().setAction(new Mining(object, RockDefinitions.DZ_SANDSTONE));
            } else if (id == 45078) {
                player.useStairs(2413, new WorldTile(3012, 9832, 0), 2, 2);
            } else if (id == 45079) {
                player.getBank().openDepositBox();
            } else if (id == 24357 && object.getX() == 3188 && object.getY() == 3355) {
                player.useStairs(-1, new WorldTile(3189, 3354, 1), 0, 1);
            } else if (id == 24359 && object.getX() == 3188 && object.getY() == 3355) {
                player.useStairs(-1, new WorldTile(3189, 3358, 0), 0, 1);
            } else if (id == 1805 && object.getX() == 3191 && object.getY() == 3363) {
                handleDoor(player, object);
            } else if (id == 29355 && object.getX() == 3230 && object.getY() == 9904) {
                // dung
                player.useStairs(828, new WorldTile(3229, 3503, 0), 1, 2);
            } else if (id == 24264) {
                player.useStairs(833, new WorldTile(3229, 9904, 0), 1, 2);
            } else if (id == 24366) {
                player.useStairs(828, new WorldTile(3237, 3459, 0), 1, 2);
            } else if (id == 882 && object.getX() == 3237 && object.getY() == 3458) {
                player.useStairs(833, new WorldTile(3237, 9858, 0), 1, 2);
            } else if (id == 29355 && object.getX() == 3097 && object.getY() == 9868) {
                // dungeon
                player.useStairs(828, new WorldTile(3096, 3468, 0), 1, 2);
            } else if (id == 26934) {
                player.useStairs(833, new WorldTile(3097, 9868, 0), 1, 2);
            } else if (id == 29355 && object.getX() == 3088 && object.getY() == 9971) {
                player.useStairs(828, new WorldTile(3087, 3571, 0), 1, 2);
            } else if (id == 65453) {
                player.useStairs(833, new WorldTile(3089, 9971, 0), 1, 2);
            } else if (id == 12389 && object.getX() == 3116 && object.getY() == 3452) {
                player.useStairs(833, new WorldTile(3117, 9852, 0), 1, 2);
            } else if (id == 29355 && object.getX() == 3116 && object.getY() == 9852) {
                player.useStairs(833, new WorldTile(3115, 3452, 0), 1, 2);
            } else if (id == 69514) {
                GnomeAgility.RunGnomeBoard(player, object);
            } else if (id == 69389) {
                GnomeAgility.JumpDown(player, object);
            } else if (id == 69526) {
                GnomeAgility.walkGnomeLog(player);
            } else if (id == 69383) {
                GnomeAgility.climbGnomeObstacleNet(player);
            } else if (id == 69506) {
                GnomeAgility.climbUpTree(player);
            } else if (id == 69508) {
                GnomeAgility.climbUpGnomeTreeBranch(player);
            } else if (id == 2312) {
                GnomeAgility.walkGnomeRope(player);
            } else if (id == 4059) {
                GnomeAgility.walkBackGnomeRope(player);
            } else if (id == 69507) {
                GnomeAgility.climbDownGnomeTreeBranch(player);
            } else if (id == 69384) {
                GnomeAgility.climbGnomeObstacleNet2(player);
            } else if (id == 69377 || id == 69378) {
                GnomeAgility.enterGnomePipe(player, object.getX(), object.getY());
            } else if (id == 65365) {
                WildernessCourseAgility.walkGate(player, object);
            } else if (id == 65367) {
                WildernessCourseAgility.walkBackGate(player, object);
            } else if (id == 65362) {
                WildernessCourseAgility.enterObstaclePipe(player, object);
            } else if (id == 65734) {
                WildernessCourseAgility.climbCliff(player, object);
            } else if (id == 64696) {
                WildernessCourseAgility.swingOnRopeSwing(player, object);
            } else if (id == 64699) {
                WildernessCourseAgility.steppingStone(player, object);
            } else if (id == 64698) {
                WildernessCourseAgility.walkAcrossLogBalance(player);
            } else if (Wilderness.isDitch(id)) {// wild ditch
                player.getDialogueManager().startDialogue("WildernessDitch", object);
            } else if (id == 42611) {// Magic Portal
                player.getDialogueManager().startDialogue("MagicPortal");
            } else if (id == 27254) {// Edgeville portal
                player.sendMessage("You enter the portal...");
                player.useStairs(10584, new WorldTile(3087, 3488, 0), 2, 3, "..and are transported to Edgeville.");
                player.addWalkSteps(1598, 4506, -1, false);
            } else if (id == 12202) {// mole entrance
                if (!player.getInventory().containsItem(952, 1)) {
                    player.sendMessage("You need a spade to dig this.");
                    return;
                }
                if (player.getX() != object.getX() || player.getY() != object.getY()) {
                    player.lock();
                    player.addWalkSteps(object.getX(), object.getY());
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            InventoryOptionsHandler.dig(player);
                        }

                    }, 1);
                } else {
                    InventoryOptionsHandler.dig(player);
                }
            } else if (id == 12230 && object.getX() == 1752 && object.getY() == 5136) {// mole
                // exit
                player.setNextWorldTile(new WorldTile(2986, 3316, 0));
            } else if (id == 15522) {// portal sign
                if (player.withinDistance(new WorldTile(1598, 4504, 0), 1)) {// PORTAL
                    // 1
                    player.getInterfaceManager().sendInterface(327);
                    player.getPackets().sendIComponentText(327, 13, "Edgeville");
                    player.getPackets().sendIComponentText(327, 14, "This portal will take you to edgeville. There " + "you can multi pk once past the wilderness ditch.");
                }
                if (player.withinDistance(new WorldTile(1598, 4508, 0), 1)) {// PORTAL
                    // 2
                    player.getInterfaceManager().sendInterface(327);
                    player.getPackets().sendIComponentText(327, 13, "Mage Bank");
                    player.getPackets().sendIComponentText(327, 14, "This portal will take you to the mage bank. " + "The mage bank is a 1v1 deep wilderness area.");
                }
                if (player.withinDistance(new WorldTile(1598, 4513, 0), 1)) {// PORTAL
                    // 3
                    player.getInterfaceManager().sendInterface(327);
                    player.getPackets().sendIComponentText(327, 13, "Magic's Portal");
                    player.getPackets().sendIComponentText(327, 14, "This portal will allow you to teleport to areas that " + "will allow you to change your magic spell book.");
                }
            } else if (id == 38811 || id == 37929) {// corp beast
                if (player.getX() < object.getX() && object.getId() == 38811) {
                    player.getDialogueManager().startDialogue("WarningD", DoomsayerManager.NORMAL_WARNING, 30,
                            "The Beast is incredibly powerful and will easily kill all but the most skilled warriors.<br><br>Due to the beast's soul devouring powers<br><col=ff0000>YOU WILL NOT GET A GRAVESTONE ON DEATH</col><br><br>Are you sure that you wish to enter?",
                            "Enter the cavern",
                            (Runnable) () -> {
                                player.stopAll();
                                player.closeInterfaces();
                                player.setNextWorldTile(new WorldTile(player.getX() + 4, player.getY(), player.getPlane()));
                            });
                } else if (object.getX() == 2918 && object.getY() == 4382) {
                    player.stopAll();
                    player.setNextWorldTile(new WorldTile(player.getX() == 2921 ? 2917 : 2921, player.getY(), player.getPlane()));
                } else if (player.getX() > object.getX() && object.getId() == 38811) {
                    final int y1 = player.getY() < object.getY() + 1 ? object.getY() + 1 : player.getY() > object.getY() + 4 ? object.getY() + 4 : player.getY();
                    player.setNextWorldTile(new WorldTile(player.getX() - 4, y1, player.getPlane()));
                }
                return;
            } // kalphite king
            else if (id == 82014) {
                /*
                 * player.lock(); player.setNextAnimation(new Animation(19498));
                 * WorldTasksManager.schedule(new WorldTask() { // to remove at // // same //
                 * time // // it // //teleports
                 *
                 * @Override public void run() { player.setNextAnimation(new Animation(-1));
                 * player.setNextWorldTile(new WorldTile(2974, 1746, 0)); player.unlock();
                 * stop(); } }, 3);
                 */
                player.getDialogueManager().startDialogue("KalphiteKingInstanceD");
                return;
            } else if (id == 37928 && object.getX() == 2883 && object.getY() == 4370) {
                player.stopAll();
                player.setNextWorldTile(new WorldTile(3214, 3782, 0));
                player.getControlerManager().startControler("Wilderness");
            } else if (id == 38815 && object.getX() == 3209 && object.getY() == 3780 && object.getPlane() == 0) {
                if (player.getSkills().getLevelForXp(Skills.WOODCUTTING) < 37 || player.getSkills().getLevelForXp(Skills.MINING) < 45 || player.getSkills().getLevelForXp(Skills.SUMMONING) < 23 || player.getSkills().getLevelForXp(Skills.FIREMAKING) < 47 || player.getSkills().getLevelForXp(Skills.PRAYER) < 55) {
                    player.sendMessage("You need 23 Summoning, 37 Woodcutting, 45 Mining, 47 Firemaking and 55 Prayer to enter this dungeon.");
                    return;
                }
                player.stopAll();
                player.setNextWorldTile(new WorldTile(2885, 4372, 0));
                player.getControlerManager().forceStop();
            } else if (id == 2079) {
                CrystalChest.openChest(object, player);
            } else if (id == 20600) {
                player.setNextWorldTile(new WorldTile(3077, 10058, 0));
            } else if (id == 18342) {
                player.setNextWorldTile(new WorldTile(3071, 3649, 0));
            } else if (id == 52859) {
                if (player.getSkills().getLevel(Skills.DUNGEONEERING) < 85) {
                    player.getPackets().sendGameMessage("You need a level of 85 Dungeoneering to enter that dungeon");
                    return;
                }
                Magic.sendNormalTeleportSpell(player, 0, 0.0D, new WorldTile(1297, 4510, 0));
            } else if (id == 2475) {
                if (objectX == 2208 && objectY == 4829 && objectPlane == 0) {
                    player.setNextWorldTile(new WorldTile(4127, 5848, 0));
                } else {
                    Magic.sendNormalTeleportSpell(player, 0, 0.0D, new WorldTile(3186, 5725, 0));
                    player.getControlerManager().startControler("FunPk");
                }
                return;
            } else if (id == 2472) {
                if (objectX == 2464 && objectY == 4817 && objectPlane == 0) {
                    player.setNextWorldTile(new WorldTile(2858, 3379, 0));
                }
                return;
            } else if (id == 4493) {
                player.setNextWorldTile(new WorldTile(3433, 3538, 1));
            } else if ((id == 5282)) {
                player.getEctophial().refillEctophial(player);
            } else if (id == 4494) {
                player.setNextWorldTile(new WorldTile(3438, 3538, 0));
            } else if (id == 4496) {
                player.setNextWorldTile(new WorldTile(3412, 3541, 1));
            } else if (id == 4495) {
                player.setNextWorldTile(new WorldTile(3417, 3541, 2));
            } else if (id == 9319) {
                player.setNextAnimation(new Animation(828));
                if (object.getX() == 3447 && object.getY() == 3576 && object.getPlane() == 1) {
                    player.setNextWorldTile(new WorldTile(3446, 3576, 2));
                }
                if (object.getX() == 3422 && object.getY() == 3550 && object.getPlane() == 0) {
                    player.setNextWorldTile(new WorldTile(3422, 3551, 1));
                }
                player.stopAll();
            } else if (id == 9320) {
                player.setNextAnimation(new Animation(828));
                if (object.getX() == 3447 && object.getY() == 3576 && object.getPlane() == 2) {
                    player.setNextWorldTile(new WorldTile(3446, 3576, 1));
                }
                if (object.getX() == 3422 && object.getY() == 3550 && object.getPlane() == 1) {
                    player.setNextWorldTile(new WorldTile(3422, 3551, 0));
                }
                player.stopAll();
            } else if (id == 52875) {
                Magic.sendNormalTeleportSpell(player, 0, 0.0D, new WorldTile(3033, 9598, 0));
            } else if (id == 1817 && object.getX() == 2273 && object.getY() == 4680) { // kbd
                // lever
                Magic.pushLeverTeleport(player, new WorldTile(3067, 10254, 0));
            } else if (id == 1816 && object.getX() == 3067 && object.getY() == 10252) { // kbd
                // out
                // lever
                Magic.pushLeverTeleport(player, new WorldTile(2273, 4681, 0));
                player.getControlerManager().forceStop();
            } else if (id == 32015 && object.getX() == 3069 && object.getY() == 10256) { // kbd
                // stairs
                player.useStairs(828, new WorldTile(3017, 3848, 0), 1, 2);
                player.getControlerManager().startControler("Wilderness");
            } else if (id == 1765 && object.getX() == 3017 && object.getY() == 3849) { // kbd
                // out
                // stairs
                player.stopAll();
                player.setNextWorldTile(new WorldTile(3069, 10255, 0));
            } else if (id == 14315 || id == 102758) {
                if (player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof PestControlLobby) {
                    return;
                }
                if (Lander.canEnter(player, 0)) {
                    return;
                }
            } else if (id == 25631 || id == 102760) {
                if (player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof PestControlLobby) {
                    return;
                }
                if (Lander.canEnter(player, 1)) {
                    return;
                }
            } else if (id == 25632 || id == 102761) {
                if (player.getControlerManager().getControler() != null && player.getControlerManager().getControler() instanceof PestControlLobby) {
                    return;
                }
                if (Lander.canEnter(player, 2)) {
                    return;
                }
            } else if (id == 2459) {
                if (objectX == 2857 && objectY == 3380 && objectPlane == 0) {
                    player.setNextWorldTile(new WorldTile(2464, 4818, 0));
                }
                return;
            } else if (id == 5959) {
                if (player.getX() == 3089 || player.getX() == 3090) {
                    Magic.pushLeverTeleport(player, new WorldTile(2539, 4712, 0));
                }
            } else if (id == 5960) {
                Magic.pushLeverTeleport(player, new WorldTile(3089, 3957, 0));
            } else if (id == 1814) {
                Magic.pushLeverTeleport(player, new WorldTile(3155, 3923, 0));
            } else if (id == 1815) {
                Magic.pushLeverTeleport(player, new WorldTile(2561, 3311, 0));
            } else if (id == 62675) {
                player.getCutscenesManager().play("DTPreview");
            } else if (id == 62681) {
                player.getDominionTower().viewScoreBoard();
            } else if (id == 62678 || id == 62679) {
                // player.getDominionTower().openModes();
                /**
                 * Since only 1 mode working we make it open endurance straight away
                 **/
                player.getDominionTower().openEnduranceMode();
            } else if (id == 62688) {
                player.getDialogueManager().startDialogue("DTClaimRewards");
            } else if (id == 62680) {
                player.getDominionTower().openBankChest();
            } else if (id == 48797) {
                player.getControlerManager().startControler("Dungeoneering");
            } else if (id == 48683) {
                player.useStairs(-1, new WorldTile(3450, 3726, 0), 0, 1);
            } else if (id == 62676) { // dominion exit
                player.useStairs(-1, new WorldTile(3374, 3093, 0), 0, 1);
            } else if (id == 62674) { // dominion entrance
                player.useStairs(-1, new WorldTile(3744, 6405, 0), 0, 1);
            } else if (id == 5847) {
                player.useStairs(-1, new WorldTile(2761, 3653, 0), 0, 1);
            } else if (id == 65349) {
                player.useStairs(-1, new WorldTile(3044, 10325, 0), 0, 1);
            } else if (id == 104453) {
                player.useStairs(-1, new WorldTile(3044, 3927, 0), 0, 1);
            } else if (id == 32048 && object.getX() == 3043 && object.getY() == 10328) {
                player.useStairs(-1, new WorldTile(3045, 3927, 0), 0, 1);
            } else if (id == 26194) {
                player.getDialogueManager().startDialogue("PartyRoomLever");
            } else if (id == 61190 || id == 61191 || id == 61192 || id == 61193) {
                if (objectDef.containsOption(0, "Chop down")) {
                    player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.NORMAL, null));
                }

            } else if (id == 104007) {
                player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.BAMBOO, null));
            } else if (id == 12290) {
                player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.JADE_ROOT_HEALTHY, null));
            } else if (id == 12291) {
                player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.JADE_ROOT_MUTATED, null));
            } else if (id == 70001) {
                player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.BLOOD, null));
            } else if (id == 9036) {
                player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.TEAK, null));
            } else if (id == 70076) {
                player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.MAHOGANY, null));
            } else if (id == 87536 || id == 87537 || id == 87538) {
                player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.CRYSTAL_TREE_SHARD, null));
            } else if (id >= 87508 && id <= 87530) {
                player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.ELDER, null));
            } else if (id == 20573) {
                player.getControlerManager().startControler("RefugeOfFear");
            } else if (id == 27310) {
                FairyRing.openRingInterface(player, object, id == 12128);
            } else if (id == 43808) {
                player.getBank().openBank();
                return;
            } else if (id == 3044) {
                player.getDialogueManager().startDialogue("SmeltingD", object);
            } else if (id == 67050) {
                player.useStairs(-1, new WorldTile(3359, 6110, 0), 0, 1);
            } else if (id == 67053) {
                player.useStairs(-1, new WorldTile(3120, 3519, 0), 0, 1);
            } else if (id == 67051) {
                player.getDialogueManager().startDialogue("Marv", false);
                /*
                 * } else if (id == 67052) { Crucible.enterCrucibleEntrance(player);
                 */
            } else if (id == 92119) {
                player.lock();
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        FadingScreen.fade(player, 0, () -> {
                            player.unlock();
                            player.setNextWorldTile(new WorldTile(414, 652, 0));
                        });
                    }
                }, 0);
            } else {
                switch (objectDef.name.toLowerCase()) {
                    case "singing bowl":
                        final CrystalCreation crystal = CrystalCreation.getCrystal(player);
                        if (crystal != null) {
                            player.getDialogueManager().startDialogue("BowlSingingD", crystal);
                        } else {
                            player.getDialogueManager().startDialogue("SimpleMessage", "You don't have the proper materials on you to make crystal items.");
                        }
                        break;
                    case "obelisk":
                    case "summoning obelisk":
                        if (player.getRegionId() == 11573 || player.getRegionId() == 15466) {
                            Summoning.sendPouchInterface(player, object);
                        }
                        break;
                    case "potter's wheel":
                        CraftingRs3Dialogue.sendPotteryWheelInterface(player);
                        break;
                    case "pottery oven":
                        CraftingRs3Dialogue.sendPotteryFurnaceInterface(player);
                        break;
                    case "fairy ring":
                    case "enchanted land":
                        FairyRing.openRingInterface(player, object, id == 12128);
                        break;
                    case "air rift":
                        RuneCrafting.craftEssenceLoop(player, 556, 1, 12.5, false, RC_DELAY_TICKS,
                                RuneCrafting.AIR_RUNE_MULTIPLIERS);
                        break;

                    case "mind rift":
                        RuneCrafting.craftEssenceLoop(player, 558, 1, 13.75, false, RC_DELAY_TICKS,
                                RuneCrafting.MIND_RUNE_MULTIPLIERS);
                        break;

                    case "water rift":
                        RuneCrafting.craftEssenceLoop(player, 555, 5, 15, false, RC_DELAY_TICKS,
                                RuneCrafting.WATER_RUNE_MULTIPLIERS);
                        break;

                    case "earth rift":
                        RuneCrafting.craftEssenceLoop(player, 557, 9, 16.25, false, RC_DELAY_TICKS,
                                RuneCrafting.EARTH_RUNE_MULTIPLIERS);
                        break;

                    case "fire rift":
                        RuneCrafting.craftEssenceLoop(player, 554, 14, 17.5, false, RC_DELAY_TICKS,
                                RuneCrafting.FIRE_RUNE_MULTIPLIERS);
                        break;

                    case "body rift":
                        RuneCrafting.craftEssenceLoop(player, 559, 20, 18.75, false, RC_DELAY_TICKS,
                                RuneCrafting.BODY_RUNE_MULTIPLIERS);
                        break;

                    case "cosmic rift":
                        RuneCrafting.craftEssenceLoop(player, 564, 27, 20, true, RC_DELAY_TICKS,
                                RuneCrafting.COSMIC_RUNE_MULTIPLIERS);
                        break;

                    case "chaos rift":
                        RuneCrafting.craftEssenceLoop(player, 562, 35, 21.25, true, RC_DELAY_TICKS,
                                RuneCrafting.CHAOS_RUNE_MULTIPLIERS);
                        break;

                    case "astral rift": // optional, if you want it available as a command
                        RuneCrafting.craftEssenceLoop(player, 9075, 40, 21.75, true, RC_DELAY_TICKS,
                                RuneCrafting.ASTRAL_RUNE_MULTIPLIERS);
                        break;

                    case "nature rift":
                        RuneCrafting.craftEssenceLoop(player, 561, 44, 22.5, true, RC_DELAY_TICKS,
                                RuneCrafting.NATURE_RUNE_MULTIPLIERS);
                        break;

                    case "law rift":
                        RuneCrafting.craftEssenceLoop(player, 563, 54, 23.75, true, RC_DELAY_TICKS,
                                RuneCrafting.LAW_RUNE_MULTIPLIERS);
                        break;

                    case "death rift":
                        RuneCrafting.craftEssenceLoop(player, 560, 65, 25, true, RC_DELAY_TICKS,
                                RuneCrafting.DEATH_RUNE_MULTIPLIERS);
                        break;

                    case "blood rift":
                        RuneCrafting.craftEssenceLoop(player, 565, 77, 26.25, true, RC_DELAY_TICKS,
                                RuneCrafting.BLOOD_RUNE_MULTIPLIERS);
                        break;

                    case "passageway":
                        /*
                         * for(Player p : battleTerrace.waiting) { if(p == player) {
                         * battleTerraceWaiting.leaveGame(player); return; } continue; }
                         * battleTerrace.joinLobby(player);
                         */
                        break;
                    case "trapdoor":
                    case "manhole":
                        if (objectDef.containsOption(0, "Open")) {
                            final WorldObject openedHole = new WorldObject(object.getId() + 1, object.getType(), object.getRotation(), object.getX(), object.getY(), object.getPlane());
                            player.faceObject(openedHole);
                            World.spawnObjectTemporary(openedHole, 60000);
                        } else {
                            player.sendMessage("It won't budge!");
                        }
                        break;
                    case "closed chest":
                        if (objectDef.containsOption(0, "Open")) {
                            player.setNextAnimation(new Animation(536));
                            player.lock(2);
                            final WorldObject openedChest = new WorldObject(object.getId() + 1, object.getType(), object.getRotation(), object.getX(), object.getY(), object.getPlane());
                            player.faceObject(openedChest);
                            World.spawnObjectTemporary(openedChest, 60000);
                        }
                        break;
                    case "open chest":
                        if (objectDef.containsOption(0, "Search")) {
                            player.sendMessage("You search the chest but find nothing.");
                        }
                        break;
                    case "spiderweb":
                        if (object.getRotation() == 2) {
                            player.lock(2);
                            if (Utils.getRandom(1) == 0) {
                                player.addWalkSteps(player.getX(), player.getY() < objectY ? object.getY() + 2 : object.getY() - 1, -1, false);
                                player.sendMessage("You squeeze though the web.");
                            } else {
                                player.getPackets().sendGameMessage("You fail to squeeze though the web; perhaps you should try again.");
                            }
                        }
                        break;
                    case "bank deposit box":
                        player.getBank().openDepositBox();
                        break;
                    case "web":
                        if (objectDef.containsOption(0, "Slash")) {
                            if (player.getEquipment().getWeaponId() > 0 || player.getInventory().containsOneItem(946)) {
                                slashWeb(player, object);
                            } else {
                                player.sendMessage("You need something sharp to hit through the web.");
                            }
                        }
                        break;
                    case "anvil":
                    case "forge":
                    case "burial anvil":
                    case "burial forge":
                    case "barbarian anvil":
                    case "barbarian forge":
                    case "fremennik anvil":
                    case "fremennik forge":
                        if (objectDef.containsOption(0, "Smith") || isRs3SmithingStation(object.getId())) {
                            if (Smithing.handleSmithingStationClick(player, object))
                                return;
                            if (player.getInventory().containsItem(30027, 1)) {
                                player.getDialogueManager().startDialogue("MalevolentSmithingD", object);
                                return;
                            }
                            ForgingInterface.sendSmithingBarSelection(player, object);
                        }
                        break;
                    case "tin rock":
                        player.getActionManager().setAction(new Mining(object, RockDefinitions.Tin_Ore));
                        break;
                    case "crashed star":
                        if (objectDef.containsOption(0, "Mine")) {
                            ShootingStar.mine(player, object);
                        }
                        break;
                    case "gold rock":
                        player.getActionManager().setAction(new Mining(object, RockDefinitions.Gold_Ore));
                        break;
                    case "iron rock":
                        player.getActionManager().setAction(new Mining(object, RockDefinitions.Iron_Ore));
                        break;
                    case "silver rock":
                        player.getActionManager().setAction(new Mining(object, RockDefinitions.Silver_Ore));
                        break;
                    case "coal rock":
                        player.getActionManager().setAction(new Mining(object, RockDefinitions.Coal_Ore));
                        break;
                    case "clay rock":
                        player.getActionManager().setAction(new Mining(object, RockDefinitions.CLAY));
                        break;
                    case "copper rock":
                        player.getActionManager().setAction(new Mining(object, RockDefinitions.Copper_Ore));
                        break;
                    case "adamantite rock":
                        player.getActionManager().setAction(new Mining(object, RockDefinitions.Adamant_Ore));
                        break;
                    case "runite rock":
                        player.getActionManager().setAction(new Mining(object, RockDefinitions.Runite_Ore));
                        break;
                    case "granite rock":
                        player.getActionManager().setAction(new Mining(object, RockDefinitions.Granite_Ore));
                        break;
                    case "sandstone rock":
                        player.getActionManager().setAction(new Mining(object, RockDefinitions.Sandstone_Ore));
                        break;
                    case "mithril rock":
                        player.getActionManager().setAction(new Mining(object, RockDefinitions.Mithril_Ore));
                        break;
                    case "divine runite rock":
                        player.getActionManager().setAction(new DivineMining(object, DivineMining.RockDefinitions.DIVINE_RUNE_ORE));
                        break;
                    case "divine adamantite rock":
                        player.getActionManager().setAction(new DivineMining(object, DivineMining.RockDefinitions.DIVINE_ADAMANTITE_ORE));
                        break;
                    case "divine mithril rock":
                        player.getActionManager().setAction(new DivineMining(object, DivineMining.RockDefinitions.DIVINE_MITHRIL_ORE));
                        break;
                    case "divine coal rock":
                        player.getActionManager().setAction(new DivineMining(object, DivineMining.RockDefinitions.DIVINE_COAL_ORE));
                        break;
                    case "divine iron rock":
                        player.getActionManager().setAction(new DivineMining(object, DivineMining.RockDefinitions.DIVINE_IRON_ORE));
                        break;
                    case "divine bronze rock":
                        player.getActionManager().setAction(new DivineMining(object, DivineMining.RockDefinitions.DIVINE_BRONZE_ORE));
                        break;
                    case "deposit box":
                        if (objectDef.containsOption(0, "Deposit")) {
                            player.getBank().openDepositBox();
                        }
                        break;
                    case "bank":
                    case "bank chest":
                    case "bank booth":
                    case "counter":
                    case "darkmeyer treasury":
                        if (objectDef.containsOption(0, "Bank") || objectDef.containsOption(0, "Use")) {
                            if (!player.promptList()) {
                                player.getBank().openBank();
                            } else {
                                player.getDialogueManager().startDialogue("BankList", false);
                            }
                        }
                        break;
                    // Woodcutting start
                    case "tree":
                        if (objectDef.containsOption(0, "Chop down")) {
                            player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.NORMAL, null));
                        }
                        break;
                    case "evergreen":
                        if (objectDef.containsOption(0, "Chop down")) {
                            player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.EVERGREEN, null));
                        }
                        break;
                    case "dead tree":
                        if (objectDef.containsOption(0, "Chop down")) {
                            player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.DEAD, null));
                        }
                        break;
                    case "oak":
                        if (objectDef.containsOption(0, "Chop down")) {
                            player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.OAK, null));
                        }
                        break;
                    case "willow":
                        if (objectDef.containsOption(0, "Chop down")) {
                            player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.WILLOW, null));
                        }
                        break;
                    case "maple tree":
                        if (objectDef.containsOption(0, "Chop down")) {
                            player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.MAPLE, null));
                        }
                        break;
                    case "ivy":
                        if (objectDef.containsOption(0, "Chop")) {
                            player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.IVY, null));
                        }
                        break;
                    case "yew":
                        if (objectDef.containsOption(0, "Chop down")) {
                            player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.YEW, null));
                        }
                        break;
                    case "magic tree":
                        if (objectDef.containsOption(0, "Chop down")) {
                            player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.MAGIC, null));
                        }
                        break;
                    case "dramen tree":
                        if (objectDef.containsOption(0, "Chop down")) {
                            player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.DONOR_TREE, null));
                        }
                        break;
                    case "dream tree":
                        if (objectDef.containsOption(0, "Chop")) {
                            player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.DREAM_TREE, null));
                        }
                        break;
                    case "cursed magic tree":
                        if (objectDef.containsOption(0, "Chop down")) {
                            player.getActionManager().setAction(new Woodcutting(object, TreeDefinitions.CURSED_MAGIC, null));
                        }
                        break;
                    case "scrimshaw crafter":
                        if (objectDef.containsOption(0, "Craft")) {
                            player.getActionManager().setAction(new DiamondCrafter(object));
                        }
                        break;
                    case "divine magic tree":
                        if (objectDef.containsOption(0, "Chop")) {
                            player.getActionManager().setAction(new DivineWoodcutting(object, DivineTreeDefinitions.DIVINE_MAGIC));
                        }
                        break;
                    case "divine tree":
                        if (objectDef.containsOption(0, "Chop")) {
                            player.getActionManager().setAction(new DivineWoodcutting(object, DivineTreeDefinitions.DIVINE_NORMAL));
                        }
                        break;
                    case "divine oak tree":
                        if (objectDef.containsOption(0, "Chop")) {
                            player.getActionManager().setAction(new DivineWoodcutting(object, DivineTreeDefinitions.DIVINE_OAK));
                        }
                        break;
                    case "divine willow tree":
                        if (objectDef.containsOption(0, "Chop")) {
                            player.getActionManager().setAction(new DivineWoodcutting(object, DivineTreeDefinitions.DIVINE_WILLOW));
                        }
                        break;
                    case "divine maple tree":
                        if (objectDef.containsOption(0, "Chop")) {
                            player.getActionManager().setAction(new DivineWoodcutting(object, DivineTreeDefinitions.DIVINE_MAPLE));
                        }
                        break;
                    case "divine yew tree":
                        if (objectDef.containsOption(0, "Chop")) {
                            player.getActionManager().setAction(new DivineWoodcutting(object, DivineTreeDefinitions.DIVINE_YEW));
                        }
                        break;
                    // Woodcutting end
                    case "gate":
                    case "large door":
                    case "metal door":
                        if (objectDef.containsOption(0, "Open")) {
                            if (!handleGate(player, object)) {
                                handleDoor(player, object);
                            }
                        }
                        break;
                    case "door":
                        if ((objectDef.containsOption(0, "Open") || objectDef.containsOption(0, "Unlock"))) {
                            handleDoor(player, object);
                        }
                        break;
                    case "ladder":
                        handleLadder(player, object, 1);
                        break;
                    case "stairs":
                    case "staircase":
                        handleStaircases(player, object, 1);
                        break;
                    case "small obelisk":
                        if (objectDef.containsOption(0, "Renew-points")) {
                            final int summonLevel = player.getSkills().getLevelForXp(Skills.SUMMONING);
                            if (player.getSkills().getLevel(Skills.SUMMONING) < summonLevel) {
                                player.lock(3);
                                player.setNextAnimation(new Animation(8502));
                                player.getSkills().set(Skills.SUMMONING, summonLevel);
                                player.sendMessage("You've recharged your Summoning points.", true);
                            } else {
                                player.sendMessage("You already have full Summoning points.");
                            }
                        }
                        break;
                    case "tansymum":
                        if (!player.getInventory().containsItem(590, 1)) {
                            player.getPackets().sendGameMessage("You need a tinderbox to do that.");
                            return;
                        }
                        if (player.getInventory().getFreeSlots() == 0) {
                            player.getPackets().sendGameMessage("Not enough space in your inventory.");
                            return;
                        }
                        player.lock();
                        player.setNextAnimation(new Animation(827));
                        WorldTasksManager.schedule(new WorldTask() {
                            @Override
                            public void run() {
                                player.getInventory().addItem(new Item(12580, 1));
                                player.unlock();
                            }

                        });
                        break;
                    case "altar":
                        if (objectDef.containsOption(0, "Pray") || objectDef.containsOption(0, "Pray-at")) {
                            final int maxPrayer2 = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
                            if (player.getPrayer().getPrayerpoints() < maxPrayer2) {
                                player.lock(5);
                                player.sendMessage("You pray to the gods...", true);
                                player.setNextAnimation(new Animation(645));
                                WorldTasksManager.schedule(new WorldTask() {
                                    @Override
                                    public void run() {
                                        player.getPrayer().restorePrayer(maxPrayer2);
                                        player.sendMessage("...and recharged your prayer.", true);
                                    }
                                }, 2);
                            } else {
                                player.sendMessage("You already have full prayer.");
                            }
                            if (id == 6552) {
                                player.getDialogueManager().startDialogue("AncientAltar");
                            }
                        }
                        break;
                    default:

                        break;
                }
            }
            if (Settings.DEBUG) {
                Logger.getGlobal().info("clicked 1 at object id : " + id + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane() + ", " + object.getType() + ", " + object.getRotation() + ", " + object.getDefinitions().name);
            }
        }));
    }

    public static boolean handleGate(final Player player, final WorldObject object, final long delay) {
        if (World.isSpawnedObject(object)) {
            return false;
        }
        if (object.getRotation() == 0) {
            boolean south = true;
            WorldObject otherDoor = World.getObjectWithType(new WorldTile(object.getX(), object.getY() + 1, object.getPlane()), object.getType());
            if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                otherDoor = World.getObjectWithType(new WorldTile(object.getX(), object.getY() - 1, object.getPlane()), object.getType());
                if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                    return false;
                }
                south = false;
            }
            final WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY(), object.getPlane());
            final WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(), otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
            if (south) {
                openedDoor1.moveLocation(-1, 0, 0);
                openedDoor1.setRotation(3);
                openedDoor2.moveLocation(-1, 0, 0);
            } else {
                openedDoor1.moveLocation(-1, 0, 0);
                openedDoor2.moveLocation(-1, 0, 0);
                openedDoor2.setRotation(3);
            }

            if (World.removeObjectTemporary(object, delay) && World.removeObjectTemporary(otherDoor, delay)) {
                player.faceObject(openedDoor1);
                World.spawnObjectTemporary(openedDoor1, delay);
                World.spawnObjectTemporary(openedDoor2, delay);
                return true;
            }
        } else if (object.getRotation() == 2) {

            boolean south = true;
            WorldObject otherDoor = World.getObjectWithType(new WorldTile(object.getX(), object.getY() + 1, object.getPlane()), object.getType());
            if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                otherDoor = World.getObjectWithType(new WorldTile(object.getX(), object.getY() - 1, object.getPlane()), object.getType());
                if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                    return false;
                }
                south = false;
            }
            final WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY(), object.getPlane());
            final WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(), otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
            if (south) {
                openedDoor1.moveLocation(1, 0, 0);
                openedDoor2.setRotation(1);
                openedDoor2.moveLocation(1, 0, 0);
            } else {
                openedDoor1.moveLocation(1, 0, 0);
                openedDoor1.setRotation(1);
                openedDoor2.moveLocation(1, 0, 0);
            }
            if (World.removeObjectTemporary(object, delay) && World.removeObjectTemporary(otherDoor, delay)) {
                player.faceObject(openedDoor1);
                World.spawnObjectTemporary(openedDoor1, delay);
                World.spawnObjectTemporary(openedDoor2, delay);
                return true;
            }
        } else if (object.getRotation() == 3) {

            boolean right = true;
            WorldObject otherDoor = World.getObjectWithType(new WorldTile(object.getX() - 1, object.getY(), object.getPlane()), object.getType());
            if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                otherDoor = World.getObjectWithType(new WorldTile(object.getX() + 1, object.getY(), object.getPlane()), object.getType());
                if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                    return false;
                }
                right = false;
            }
            final WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY(), object.getPlane());
            final WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(), otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
            if (right) {
                openedDoor1.moveLocation(0, -1, 0);
                openedDoor2.setRotation(0);
                openedDoor1.setRotation(2);
                openedDoor2.moveLocation(0, -1, 0);
            } else {
                openedDoor1.moveLocation(0, -1, 0);
                openedDoor1.setRotation(0);
                openedDoor2.setRotation(2);
                openedDoor2.moveLocation(0, -1, 0);
            }
            if (World.removeObjectTemporary(object, delay) && World.removeObjectTemporary(otherDoor, delay)) {
                player.faceObject(openedDoor1);
                World.spawnObjectTemporary(openedDoor1, delay);
                World.spawnObjectTemporary(openedDoor2, delay);
                return true;
            }
        } else if (object.getRotation() == 1) {

            boolean right = true;
            WorldObject otherDoor = World.getObjectWithType(new WorldTile(object.getX() - 1, object.getY(), object.getPlane()), object.getType());
            if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                otherDoor = World.getObjectWithType(new WorldTile(object.getX() + 1, object.getY(), object.getPlane()), object.getType());
                if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                    return false;
                }
                right = false;
            }
            final WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY(), object.getPlane());
            final WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(), otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
            if (right) {
                openedDoor1.moveLocation(0, 1, 0);
                openedDoor1.setRotation(0);
                openedDoor2.moveLocation(0, 1, 0);
            } else {
                openedDoor1.moveLocation(0, 1, 0);
                openedDoor2.setRotation(0);
                openedDoor2.moveLocation(0, 1, 0);
            }
            if (World.removeObjectTemporary(object, delay) && World.removeObjectTemporary(otherDoor, delay)) {
                player.faceObject(openedDoor1);
                World.spawnObjectTemporary(openedDoor1, delay);
                World.spawnObjectTemporary(openedDoor2, delay);
                return true;
            }
        }
        return false;
    }

    private static void handleOption2(final Player player, final WorldObject object) {
        final ObjectDefinitions objectDef = object.getDefinitions();

        final int id = object.getId();
        if (player.isOwner()) {
            player.getPackets().sendPanelBoxMessage("Option 2:" + object.getId() + "; " + object.getX() + " " + object.getY() + " " + object.getPlane());
        }
        if (player.isLocked()) {
            return;
        }
        if (!HomeAreaHandler.playerCanInteractWithObject(player, object)) {
            HomeAreaHandler.sendCanNotUseNpcOrObjectMessageToPlayer(player, object.getDefinitions().getName(), HomeAreaHandler.Type.OBJECT);
            return;
        }
        if (isMetalBankObject(id)) {
            player.setRouteEvent(new RouteEvent(object, () -> {
                player.stopAll();
                player.faceObject(object);
                if (!player.getControlerManager().processObjectClick2(object)) {
                    return;
                }
                MetalBank.depositInventory(player);
            }, true));
            return;
        }
        if (object.getId() == 103568) {// Telos exit
            player.stopAll();
            player.faceObject(object);
            if (!player.getControlerManager().processObjectClick2(object))
                return;
            return;
        }
        if (object.getId() == 113894) {
            player.setRouteEvent(new RouteEvent(object, () -> {
                if (player.getX() == 5427 || player.getX() == 5426 || player.getX() == 5428) {
                    List<String> options = new ArrayList<String>();
                    options.add("T-Rex Boss");
                    player.getDialogueManager().startDialogue("OptionSelectionD", "what boss would you like?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                        int page = selector.getPage();
                        switch (page) {
                            case 0:
                                selector.close();
                                switch (option) {
                                    case OptionSelectionD.OptionSelector.OPTION_1:
                                        if (player.getSkills().getXp(Skills.SLAYER) < 104273167 && player.getSkills().getXp(Skills.STRENGTH) < 104273167) {
                                            player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "You Need Slayer & Strength Level Of 120 To teleport", true);
                                            return;
                                        }
                                        FadingScreen.fade(player, 0, () -> {
                                            player.setNextAnimation(new Animation(29010));
                                            player.setNextWorldTile(new WorldTile(3085, 6172, 0));
                                            player.getControlerManager().startControler("TrexController");
                                        });
                                        break;
                                }
                                return;
                        }
                    });
                }
            }, false));
            return;
        }
        if (object.getId() == 110591) {
            player.getBank().openBank();
            return;
        }
        if (id == 45802) {
            player.stopAll(true);
            player.setRouteEvent(new RouteEvent(new WorldTile(414, 674, 0), () -> {
                player.faceObject(object);
                if (player.getContract() == null) {
                    if (player.isChooseTask()) {
                        player.getDialogueManager().startDialogue("ReapersChoiceD", true);
                        return;
                    }
                    ContractHandler.assignPlayerNewContract(player);
                    player.sendMessage(Colors.ORANGE + "Reaper Contract: " + player.getContract().getKillAmount() + "x " + NPCDefinitions.getNPCDefinitions(player.getContract().getNpcId()).getName() + ".");
                } else {
                    player.sendMessage("You already have an active Reaper contract.");
                }
            }, false));
            return;
        }
        player.setRouteEvent(new RouteEvent(object, () -> {
            player.stopAll();
            player.faceObject(object);
            if (!player.getControlerManager().processObjectClick2(object)) {
                return;
            }
            if (Pickables.handlePickable(player, object)) {
                return;
            }
            if (PrifddinasCity.handleObjectOption2(player, object)) {
                return;
            }
            if(player.vineHerbPatches.handleOption2(object)) {
                return;
            }
            if(player.harmonyPillars.handleOption2(object)) {
                return;
            }
            if (object.getDefinitions().getName().equalsIgnoreCase("energy rift")) {
                player.getInterfaceManager().sendInterface(131);
                return;
            }
            if (object instanceof EvilTreeObject) {
                val tree = (EvilTreeObject) object;
                if (tree.isDead()) {
                    // todo
                } else {
                    tree.lightFire(player);
                }
                return;
            } else if (object instanceof EvilSaplingObject) {
                val sapling = (EvilSaplingObject) object;
                sapling.inspect(player);
                return;
            } else if (object instanceof EvilWeedsObject) {
                val patch = (EvilWeedsObject) object;
                patch.inspect(player);
                return;
            }
            if(object.getId() == 48675) {
                RichestBanksD.showRegular(player);
                return;
            }

            if (id == 43808) {
                player.getGEManager().openGrandExchange();
                return;
            }
            if (id == 9140) {
                GrandExchange.viewLastOffer(player);
                return;
            }
            if (id == 88923) {
                ChristmasSeasonalEvent.viewHints(player);
                return;
            }
            if (object.getDefinitions().getName().toLowerCase().contains("loom")) {
                final Loom loom = Loom.getBar(player);
                if (loom != null) {
                    player.getDialogueManager().startDialogue("LoomingD", loom);
                }
                return;
            }
            if (id == 3192) {
                GIM.getHighscores().searchGroup(player);
                return;
            }
            if (id == 54408) {
                player.gimBank.displayHistory();
                return;
            }
            if (id == 82481) {// slayer tower floor to top
                player.useStairs(-1, new WorldTile(3415, 3558, 2), 0, 1);
                return;
            }
            if (id == 82485) {// slayer tower top to floor
                player.useStairs(-1, new WorldTile(3415, 3558, 0), 0, 1);
                return;
            }
            if (id == 17819) {// vorago graveyard
                player.useStairs(-1, new WorldTile(3039, 6182, 0), 0, 1);
                return;
            } else if (id == 9356) {
                if(player.isBronzeDonor()) {
                    player.lock(3);
                    FightCaves.enterFightCaves(player, true);
                } else {
                    player.sendMessage("You must be at least a Bronze donator to skip early FC waves.");
                }
                return;
            }
            switch (objectDef.name.toLowerCase()) {
                case "singing bowl":
                    player.sendMessage("Use the item you want to revert on the singing bowl.");
                    return;
                case "deposit box":
                case "bank deposit box":
                    player.getBank().depositAllInventory(false);
                    break;
                case "summoning obelisk":
                case "obelisk":
                    if (objectDef.containsOption(1, "Renew-points")) {
                        if (player.getSkills().getLevel(23) < player.getSkills().getLevelForXp(23)) {
                            player.lock(5);
                            player.sendMessage("You feel the obelisk..", true);
                            player.setNextAnimation(new Animation(8502));
                            player.setNextGraphics(new Graphics(1308));
                            WorldTasksManager.schedule(new WorldTask() {

                                @Override
                                public void run() {
                                    player.getSkills().restoreSummoning();
                                    player.sendMessage("..and recharge your Summoning points.", true);
                                }
                            }, 2);
                        } else {
                            player.sendMessage("You already have full Summoning points.", true);
                        }
                        return;
                    }
                    break;
            }
            if (object.getId() == 91172) {
                if (object.getX() == 2328 && object.getY() == 3176) {
                    if (player.withinDistance(new WorldTile(object.getX(), object.getY(), object.getPlane()))) {
                        player.setNextWorldTile(new WorldTile(2327, 3176, 2));
                    }
                }
                if (object.getX() == 2325 && object.getY() == 3168) {
                    if (player.withinDistance(new WorldTile(object.getX(), object.getY(), object.getPlane()))) {
                        player.setNextWorldTile(new WorldTile(2327, 3168, 2));
                    }
                }
            }

            if (object.getDefinitions().getName().toLowerCase().contains("spirit tree") || object.getId() == 26723) {
                SpiritTree.openInterface(player, object.getId() != 68973 && object.getId() != 68974);
                return;
            }
            if (object.getDefinitions().name.equalsIgnoreCase("furnace") || object.getId() == 11666) {
                player.getDialogueManager().startDialogue("SmeltingD", object);
            } else if (id == 17010) {
                player.getDialogueManager().startDialogue("LunarAltar");
            }
            if (object.getDefinitions().name.toLowerCase().contains("spinning")) {
                CraftingRs3Dialogue.sendSpinningInterface(player);
                return;
            }

            final Animation THIEVING_ANIMATION = new Animation(881);
            final Animation THIEVING_ANIMATION1 = new Animation(3692);


            if (id == 90271 || id == 100955) {
                if (!player.promptList()) {
                    player.getBank().openBank();
                } else {
                    player.getDialogueManager().startDialogue("BankList", false);
                }
                return;
            }
            if (id == 38811) { // corp beast
                player.getCutscenesManager().play("CorporealBeastScene");
                return;
            }
            if (id == 87306) {
                player.getActionManager().setAction(new DivinationConvert(player, new Object[]{ConvertMode.CONVERT_TO_ENERGY}));
                return;
            }
            if (id == 83954) {
                if (!player.promptList()) {
                    player.getBank().openBank();
                } else {
                    player.getDialogueManager().startDialogue("BankList", false);
                }
                return;
            }
            if (player.getTreasureTrails().useObject(object)) {
                return;
            }
            if (PortableStation.isPortableObject(object)) {
                PortableStation.handleObjectClick2(player, object);
                return;
            }
            if (id == 70812) {
                player.getControlerManager().startControler("QueenBlackDragonControler");
                return;
            }
            /**
             * Slayer Tower.
             */
            if (id == 82667 || id == 82483) { /** Staircases **/
                player.useStairs(-1, new WorldTile(player.getX(), player.getY(), 2), 0, 0);
                return;
            }

            if (id == 87997) {
                FadingScreen.fade(player, 6, () -> {
                    player.unlock();
                    player.setNextWorldTile(new WorldTile(2399, 6054, 1));
                });
                return;
            }
            if (id == 51061 || id == 8749 || id == 61336) {
                final int maxPrayer = player.getSkills().getLevelForXp(Skills.PRAYER) * 10;
                if (player.getPrayer().getPrayerpoints() < maxPrayer) {
                    player.setNextAnimation(new Animation(645));
                    player.getPrayer().restorePrayer(maxPrayer);
                    player.sendMessage("You've recharged your prayer points.");
                } else {
                    player.sendMessage("You already have full Prayer points.");
                }
                return;
            }
            if (player.getFarmingManager().isFarming(object, null, 2)) {
                return;
            }
            if (id == 6162) { // Diamond stall
                final Item[] items7 = {new Item(30037), new Item(32337), new Item(33740), new Item(34528), new Item(31350), new Item(1618), new Item(1632), new Item(1631), new Item(1631), new Item(1617), new Item(30915), new Item(34728), new Item(34730), new Item(34727), new Item(34729)};
                if (player.getSkills().getLevel(Skills.THIEVING) < 98) {
                    player.sendMessage("You need a Thieving level of 98 to thieve from this stall.");
                    return;
                }
                if (player.getThievingDelay() > Utils.currentTimeMillis() || player.getActionManager().getActionDelay() != 0) {
                    return;
                }
                final Item item7 = items7[Utils.random(items7.length)];
                    if (item7.getDefinitions().isStackable()) {
                            item7.setAmount(Utils.random(1, 4));
                    }
                    if (player.getEquipment().getCapeId() == 9778 || player.getEquipment().getCapeId() == 34597) {
                        if (item7.getDefinitions().isNoted()) {
                            player.getBank().addItem(new Item(item7), true);
                            player.sendMessage(Colors.ORANGE + "You Gained Extra Loot From Your Thieving Cape & Has Been Sent To Bank", true);
                        }
                    }
                if (player.getInventory().getFreeSlots() < 2) {
                    player.sendMessage("You need atleast 2 spots free to thieve from this stall");
                    return;
                }
                if (player.getInventory().addItem(item7)) {
                    if (!player.getPerkManager().hasPerkActive(DonationPerk.SLEIGHT_OF_HAND)) {
                        player.applyHit(new Hit(player, Utils.random(1, 6), HitLook.REGULAR_DAMAGE, 1));
                    }
                    final int amount7 = player.getSkills().getLevelForXp(Skills.THIEVING) * 750;
                    player.addMoney(Utils.random(1, amount7));
                    player.setNextAnimation(THIEVING_ANIMATION);
                    player.setThievingDelay(Utils.currentTimeMillis() + 1900);
                    player.getSkills().addXp(Skills.THIEVING, 375);
                    player.addTimesStolen();
                    if (player.getTimesStolen() == 1500 || player.getTimesStolen() == 1750 || player.getTimesStolen() == 2000 || player.getTimesStolen() == 2250
                            || player.getTimesStolen() == 2500 || player.getTimesStolen() == 2750 || player.getTimesStolen() == 3000 || player.getTimesStolen() == 3250
                            || player.getTimesStolen() == 3500 || player.getTimesStolen() == 3750 || player.getTimesStolen() == 4000) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received an Uncut Onyx from Thieving!", false);
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a Mystery box from Thieving!", false);
                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Mystery box from Thieving!", false);
                        return;
                    }
                    if (Utils.random(750) == 0) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/750 Uncut Onyx from Thieving!", false);
                        return;
                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(34023, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Small Protean Pack from Thieving!", false);
                        return;
                    }
                    player.getContracts().recordAction(Skills.THIEVING, 13);
                    player.sendMessage("You've successfully stolen from this stall; " + "times thieved: " + Colors.RED + Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.", true);
                    if (Utils.random(35) == 0 && player.hasRandomEvent()) {
                        if (!player.followedByRandomEventNPC()) {
                            NPC npc = new ThievingRandomEvent(player, player);
                            if (npc.withinDistance(player, 14)) {
                                player.setCurrentRandomEventNPC(npc);
                                player.sendMessage("<col=ff0000>A Rogue appears out of nowhere.");
                            }
                        }
                    }
                } else {
                    player.sendMessage("You do not have enough inventory space to do this.", true);
                }
                return;
            }
            if (id == 6164) { // level 80
                final Item[] items7 = {new Item(9382), new Item(8868), new Item(7650), new Item(2355), new Item(2253)};
                if (player.getSkills().getLevel(Skills.THIEVING) < 80) {
                    player.sendMessage("You need a Thieving level of 80 to thieve from this stall.");
                    return;
                }
                if (player.getThievingDelay() > Utils.currentTimeMillis() || player.getActionManager().getActionDelay() != 0) {
                    return;
                }
                final Item item7 = items7[Utils.random(items7.length)];
                if (item7.getDefinitions().isStackable()) {
                    item7.setAmount(Utils.random(1, 2));
                }
                if (player.getEquipment().getCapeId() == 9778) {
                    if (item7.getDefinitions().isNoted()) {
                        item7.setAmount(Utils.random(3, 9));

                        player.getBank().addItem(new Item(item7), true);
                        player.sendMessage(Colors.ORANGE + "You Gained Extra Loot From Your Thieving Cape & Has Been Sent To Bank", true);
                    }


                }
                if (player.getInventory().getFreeSlots() < 2) {
                    player.sendMessage("You need atleast 2 spots free to thieve from this stall");
                    return;
                }
                if (player.getInventory().addItem(item7)) {
                    if (!player.getPerkManager().hasPerkActive(DonationPerk.SLEIGHT_OF_HAND)) {
                        player.applyHit(new Hit(player, Utils.random(1, 6), HitLook.REGULAR_DAMAGE, 1));
                    }
                    final int amount7 = player.getSkills().getLevelForXp(Skills.THIEVING) * 191;
                    player.addMoney(Utils.random(1, amount7));
                    player.setNextAnimation(THIEVING_ANIMATION);
                    player.setThievingDelay(Utils.currentTimeMillis() + 1900);
                    player.getSkills().addXp(Skills.THIEVING, 90);
                    player.addTimesStolen();
                    if (player.getTimesStolen() == 1500 || player.getTimesStolen() == 1750 || player.getTimesStolen() == 2000 || player.getTimesStolen() == 2250
                            || player.getTimesStolen() == 2500 || player.getTimesStolen() == 2750 || player.getTimesStolen() == 3000 || player.getTimesStolen() == 3250
                            || player.getTimesStolen() == 3500 || player.getTimesStolen() == 3750 || player.getTimesStolen() == 4000) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received an Uncut Onyx from Thieving!", false);
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a Mystery box from Thieving!", false);

                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Mystery box from Thieving!", false);
                        return;
                    }
                    if (Utils.random(750) == 0) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/750 Uncut Onyx from Thieving!", false);
                        return;
                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(34023, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Small Protean Pack from Thieving!", false);
                        return;
                    }
                    player.getContracts().recordAction(Skills.THIEVING, 13);
                    player.sendMessage("You've successfully stolen from this stall; " + "times thieved: " + Colors.RED + Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.", true);
                    if (Utils.random(35) == 0 && player.hasRandomEvent()) {
                        if (!player.followedByRandomEventNPC()) {
                            NPC npc = new ThievingRandomEvent(player, player);
                            if (npc.withinDistance(player, 14)) {
                                player.setCurrentRandomEventNPC(npc);
                                player.sendMessage("<col=ff0000>A Rogue appears out of nowhere.");
                            }
                        }
                    }
                } else {
                    player.sendMessage("You do not have enough inventory space to do this.", true);
                }
                return;
            }
            /**
             * Crafting stall.
             */
            if (id == 4874) {
                final Item[] items1 = {new Item(1739), new Item(1737), new Item(1779), new Item(1776), new Item(6287), new Item(1635), new Item(1734), new Item(1623), new Item(1625), new Item(1627), new Item(1629), new Item(1631), new Item(1621), new Item(1619), new Item(1617)};
                if (player.getThievingDelay() > Utils.currentTimeMillis() || player.getActionManager().getActionDelay() != 0) {
                    return;
                }
                final Item item1 = items1[Utils.random(items1.length)];
                if (item1.getDefinitions().isStackable()) {
                    item1.setAmount(Utils.random(1, 5));
                }
                if (player.getEquipment().getCapeId() == 9778) {
                    if (item1.getDefinitions().isNoted()) {
                        item1.setAmount(Utils.random(3, 9));

                        player.getBank().addItem(new Item(item1), true);
                        player.sendMessage(Colors.ORANGE + "You Gained Extra Loot From Your Thieving Cape & Has Been Sent To Bank", true);
                    }


                }
                if (player.getInventory().getFreeSlots() < 2) {
                    player.sendMessage("You need atleast 2 spots free to thieve from this stall");
                    return;
                }
                if (player.getInventory().addItem(item1)) {
                    if (!player.getPerkManager().hasPerkActive(DonationPerk.SLEIGHT_OF_HAND)) {
                        player.applyHit(new Hit(player, Utils.random(1, 3), HitLook.REGULAR_DAMAGE, 1));
                    }
                    final int amount1 = player.getSkills().getLevelForXp(Skills.THIEVING) * 52;
                    player.addMoney(Utils.random(1, amount1));
                    player.setNextAnimation(THIEVING_ANIMATION);
                    player.setThievingDelay(Utils.currentTimeMillis() + 1900);
                    player.getSkills().addXp(Skills.THIEVING, 25);
                    player.addTimesStolen();
                    ThievingContractList.listenStall(player, Stalls.CRAFTING);
                    if (player.getTimesStolen() == 1500 || player.getTimesStolen() == 1750 || player.getTimesStolen() == 2000 || player.getTimesStolen() == 2250
                            || player.getTimesStolen() == 2500 || player.getTimesStolen() == 2750 || player.getTimesStolen() == 3000 || player.getTimesStolen() == 3250
                            || player.getTimesStolen() == 3500 || player.getTimesStolen() == 3750 || player.getTimesStolen() == 4000) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received an Uncut Onyx from Thieving!", false);
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a Mystery box from Thieving!", false);

                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Mystery box from Thieving!", false);
                        return;
                    }
                    if (Utils.random(750) == 0) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/750 Uncut Onyx from Thieving!", false);
                        return;
                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(34023, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Small Protean Pack from Thieving!", false);
                        return;
                    }
                    if (player.getDailyManager().getTask() != null) {
                        if (object.getId() == player.getTaskItemId()) {
                            player.getDailyManager().processTask();
                        }
                    }
                    player.sendMessage("You've successfully stolen from this stall; " + "times thieved: " + Colors.RED + Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.", true);
                    if (Utils.random(35) == 0 && player.hasRandomEvent()) {
                        if (!player.followedByRandomEventNPC()) {
                            NPC npc = new ThievingRandomEvent(player, player);
                            if (npc.withinDistance(player, 14)) {
                                player.setCurrentRandomEventNPC(npc);
                                player.sendMessage("<col=ff0000>A Rogue appears out of nowhere.");
                            }
                        }
                    }
                } else {
                    player.sendMessage("You do not have enough inventory space to do this.", true);
                }
                return;
            }
            /**
             * Food stall.
             */
            if (id == 4875) {
                final Item[] items2 = {new Item(379), new Item(385), new Item(373), new Item(7946), new Item(391), new Item(1963), new Item(315), new Item(319), new Item(347), new Item(325), new Item(361), new Item(365), new Item(333), new Item(329), new Item(351), new Item(339)};
                if (player.getSkills().getLevel(Skills.THIEVING) < 30) {
                    player.sendMessage("You need a Thieving level of 30 to thieve from this stall.");
                    return;
                }
                if (player.getThievingDelay() > Utils.currentTimeMillis() || player.getActionManager().getActionDelay() != 0) {
                    return;
                }
                if (player.getInventory().getFreeSlots() < 2) {
                    player.sendMessage("You need atleast 2 spots free to thieve from this stall");
                    return;
                }
                final Item item2 = items2[Utils.random(items2.length)];
                if (item2.getDefinitions().isStackable()) {
                    item2.setAmount(Utils.random(1, 5));
                }
                if (player.getEquipment().getCapeId() == 9778) {
                    if (item2.getDefinitions().isNoted()) {
                        item2.setAmount(Utils.random(3, 9));

                        player.getBank().addItem(new Item(item2), true);
                        player.sendMessage(Colors.ORANGE + "You Gained Extra Loot From Your Thieving Cape & Has Been Sent To Bank", true);
                    }


                }
                if (player.getInventory().addItem(item2)) {
                    if (!player.getPerkManager().hasPerkActive(DonationPerk.SLEIGHT_OF_HAND)) {
                        player.applyHit(new Hit(player, Utils.random(1, 4), HitLook.REGULAR_DAMAGE, 1));
                    }
                    final int amount2 = player.getSkills().getLevelForXp(Skills.THIEVING) * 102;
                    player.addMoney(Utils.random(1, amount2));
                    player.setNextAnimation(THIEVING_ANIMATION);
                    player.setThievingDelay(Utils.currentTimeMillis() + 1900);
                    player.getSkills().addXp(Skills.THIEVING, 50);
                    player.addTimesStolen();
                    ThievingContractList.listenStall(player, Stalls.MONKEY_FOOD);
                    if (player.getTimesStolen() == 1500 || player.getTimesStolen() == 1750 || player.getTimesStolen() == 2000 || player.getTimesStolen() == 2250
                            || player.getTimesStolen() == 2500 || player.getTimesStolen() == 2750 || player.getTimesStolen() == 3000 || player.getTimesStolen() == 3250
                            || player.getTimesStolen() == 3500 || player.getTimesStolen() == 3750 || player.getTimesStolen() == 4000) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received an Uncut Onyx from Thieving!", false);
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a Mystery box from Thieving!", false);

                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Mystery box from Thieving!", false);
                        return;
                    }
                    if (Utils.random(750) == 0) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/750 Uncut Onyx from Thieving!", false);
                        return;
                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(34023, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Small Protean Pack from Thieving!", false);
                        return;
                    }
                    if (player.getDailyManager().getTask() != null) {
                        if (object.getId() == player.getTaskItemId()) {
                            player.getDailyManager().processTask();
                        }
                    }
                    player.sendMessage("You've successfully stolen from this stall; " + "times thieved: " + Colors.RED + Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.", true);
                    if (Utils.random(35) == 0 && player.hasRandomEvent()) {
                        if (!player.followedByRandomEventNPC()) {
                            NPC npc = new ThievingRandomEvent(player, player);
                            if (npc.withinDistance(player, 14)) {
                                player.setCurrentRandomEventNPC(npc);
                                player.sendMessage("<col=ff0000>A Rogue appears out of nowhere.");
                            }
                        }
                    }
                } else {
                    player.sendMessage("You do not have enough inventory space to do this.", true);
                }
                return;
            }
            /**
             * General stall.
             */
            if (id == 4876) {
                final Item[] items3 = {new Item(1391), new Item(2357), new Item(1776)};
                if (player.getSkills().getLevel(Skills.THIEVING) < 65) {
                    player.sendMessage("You need a Thieving level of 65 to thieve from this stall.");
                    return;
                }
                if (player.getInventory().getFreeSlots() < 2) {
                    player.sendMessage("You need atleast 2 spots free to thieve from this stall");
                    return;
                }
                if (player.getThievingDelay() > Utils.currentTimeMillis() || player.getActionManager().getActionDelay() != 0) {
                    return;
                }
                final Item item3 = items3[Utils.random(items3.length)];
                if (item3.getDefinitions().isStackable()) {
                    item3.setAmount(Utils.random(1, 5));
                }
                if (player.getEquipment().getCapeId() == 9778) {
                    if (item3.getDefinitions().isNoted()) {
                        item3.setAmount(Utils.random(3, 9));

                        player.getBank().addItem(new Item(item3), true);
                        player.sendMessage(Colors.ORANGE + "You Gained Extra Loot From Your Thieving Cape & Has Been Sent To Bank", true);
                    }


                }
                if (player.getInventory().addItem(item3)) {
                    if (!player.getPerkManager().hasPerkActive(DonationPerk.SLEIGHT_OF_HAND)) {
                        player.applyHit(new Hit(player, Utils.random(1, 5), HitLook.REGULAR_DAMAGE, 1));
                    }
                    final int amount3 = player.getSkills().getLevelForXp(Skills.THIEVING) * 152;
                    player.addMoney(Utils.random(1, amount3));
                    player.setNextAnimation(THIEVING_ANIMATION);
                    player.setThievingDelay(Utils.currentTimeMillis() + 1900);
                    player.getSkills().addXp(Skills.THIEVING, 75);
                    player.addTimesStolen();
                    ThievingContractList.listenStall(player, Stalls.MONKEY_GENERAL);
                    if (player.getTimesStolen() == 1500 || player.getTimesStolen() == 1750 || player.getTimesStolen() == 2000 || player.getTimesStolen() == 2250
                            || player.getTimesStolen() == 2500 || player.getTimesStolen() == 2750 || player.getTimesStolen() == 3000 || player.getTimesStolen() == 3250
                            || player.getTimesStolen() == 3500 || player.getTimesStolen() == 3750 || player.getTimesStolen() == 4000) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received an Uncut Onyx from Thieving!", false);
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a Mystery box from Thieving!", false);

                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Mystery box from Thieving!", false);
                        return;
                    }
                    if (Utils.random(750) == 0) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/750 Uncut Onyx from Thieving!", false);
                        return;
                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(34023, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Small Protean Pack from Thieving!", false);
                        return;
                    }
                    if (player.getDailyManager().getTask() != null) {
                        if (object.getId() == player.getTaskItemId()) {
                            player.getDailyManager().processTask();
                        }
                    }
                    player.sendMessage("You've successfully stolen from this stall; " + "times thieved: " + Colors.RED + Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.", true);
                    if (Utils.random(35) == 0 && player.hasRandomEvent()) {
                        if (!player.followedByRandomEventNPC()) {
                            NPC npc = new ThievingRandomEvent(player, player);
                            if (npc.withinDistance(player, 14)) {
                                player.setCurrentRandomEventNPC(npc);
                                player.sendMessage("<col=ff0000>A Rogue appears out of nowhere.");
                            }
                        }
                    }
                } else {
                    player.sendMessage("You do not have enough inventory space to do this.", true);
                }
                return;
            }
            /**
             * Magic stall.
             */
            if (id == 4877) {
                final Item[] items4 = {new Item(562), new Item(558), new Item(556), new Item(564), new Item(7936), new Item(7936), new Item(7936), new Item(7936), new Item(565), new Item(554), new Item(561)};
                if (player.getSkills().getLevel(Skills.THIEVING) < 85) {
                    player.sendMessage("You need a Thieving level of 85 to thieve from this stall.");
                    return;
                }
                if (player.getThievingDelay() > Utils.currentTimeMillis() || player.getActionManager().getActionDelay() != 0) {
                    return;
                }
                final Item item4 = items4[Utils.random(items4.length)];
                if (item4.getDefinitions().isStackable()) {
                    item4.setAmount(Utils.random(20, 77));
                }
                if (player.getEquipment().getCapeId() == 9778) {
                    if (item4.getDefinitions().isNoted()) {
                        item4.setAmount(Utils.random(3, 9));

                        player.getBank().addItem(new Item(item4), true);
                        player.sendMessage(Colors.ORANGE + "You Gained Extra Loot From Your Thieving Cape & Has Been Sent To Bank", true);
                    }


                }
                if (player.getInventory().getFreeSlots() < 2) {
                    player.sendMessage("You need atleast 2 spots free to thieve from this stall");
                    return;
                }
                if (player.getInventory().addItem(item4)) {
                    if (!player.getPerkManager().hasPerkActive(DonationPerk.SLEIGHT_OF_HAND)) {
                        player.applyHit(new Hit(player, Utils.random(1, 6), HitLook.REGULAR_DAMAGE, 1));
                    }
                    final int amount4 = player.getSkills().getLevelForXp(Skills.THIEVING) * 202;
                    player.addMoney(Utils.random(1, amount4));
                    player.setNextAnimation(THIEVING_ANIMATION);
                    player.setThievingDelay(Utils.currentTimeMillis() + 1900);
                    player.getSkills().addXp(Skills.THIEVING, 100);
                    player.addTimesStolen();
                    ThievingContractList.listenStall(player, Stalls.MAGIC_STALL);
                    if (player.getTimesStolen() == 1500 || player.getTimesStolen() == 1750 || player.getTimesStolen() == 2000 || player.getTimesStolen() == 2250
                            || player.getTimesStolen() == 2500 || player.getTimesStolen() == 2750 || player.getTimesStolen() == 3000 || player.getTimesStolen() == 3250
                            || player.getTimesStolen() == 3500 || player.getTimesStolen() == 3750 || player.getTimesStolen() == 4000) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received an Uncut Onyx from Thieving!", false);
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a Mystery box from Thieving!", false);

                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Mystery box from Thieving!", false);
                        return;
                    }
                    if (Utils.random(750) == 0) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/750 Uncut Onyx from Thieving!", false);
                        return;
                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(34023, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Small Protean Pack from Thieving!", false);
                        return;
                    }
                    if (player.getDailyManager().getTask() != null) {
                        if (object.getId() == player.getTaskItemId()) {
                            player.getDailyManager().processTask();
                        }
                    }
                    player.sendMessage("You've successfully stolen from this stall; " + "times thieved: " + Colors.RED + Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.", true);
                    if (Utils.random(35) == 0 && player.hasRandomEvent()) {
                        if (!player.followedByRandomEventNPC()) {
                            NPC npc = new ThievingRandomEvent(player, player);
                            if (npc.withinDistance(player, 14)) {
                                player.setCurrentRandomEventNPC(npc);
                                player.sendMessage("<col=ff0000>A Rogue appears out of nowhere.");
                            }
                        }
                    }
                } else {
                    player.sendMessage("You do not have enough inventory space to do this.", true);
                }
                return;
            }
            /**
             * Scimitar stall.
             */
            if (id == 4878) {
                final Item[] items5 = {new Item(1323), new Item(1325), new Item(1327), new Item(1329), new Item(1331), new Item(1333), new Item(4587), new Item(6611)};
                if (player.getSkills().getLevel(Skills.THIEVING) < 95) {
                    player.sendMessage("You need a Thieving level of 95 to thieve from this stall.");
                    return;
                }
                if (player.getThievingDelay() > Utils.currentTimeMillis() || player.getActionManager().getActionDelay() != 0) {
                    return;
                }
                if (player.getInventory().getFreeSlots() < 2) {
                    player.sendMessage("You need atleast 2 spots free to thieve from this stall");
                    return;
                }
                final Item item5 = items5[Utils.random(items5.length)];
                if (item5.getDefinitions().isStackable()) {
                    item5.setAmount(Utils.random(1, 5));
                }
                if (player.getEquipment().getCapeId() == 9778) {
                    if (item5.getDefinitions().isNoted()) {
                        item5.setAmount(Utils.random(3, 9));

                        player.getBank().addItem(new Item(item5), true);
                        player.sendMessage(Colors.ORANGE + "You Gained Extra Loot From Your Thieving Cape & Has Been Sent To Bank", true);
                    }


                }
                if (player.getInventory().addItem(item5)) {
                    if (!player.getPerkManager().hasPerkActive(DonationPerk.SLEIGHT_OF_HAND)) {
                        player.applyHit(new Hit(player, Utils.random(1, 7), HitLook.REGULAR_DAMAGE, 1));
                    }
                    final int amount5 = player.getSkills().getLevelForXp(Skills.THIEVING) * 252;
                    player.addMoney(Utils.random(1, amount5));
                    player.setNextAnimation(THIEVING_ANIMATION);
                    player.setThievingDelay(Utils.currentTimeMillis() + 1900);
                    player.getSkills().addXp(Skills.THIEVING, 125);
                    player.addTimesStolen();
                    if (player.getTimesStolen() == 1500 || player.getTimesStolen() == 1750 || player.getTimesStolen() == 2000 || player.getTimesStolen() == 2250
                            || player.getTimesStolen() == 2500 || player.getTimesStolen() == 2750 || player.getTimesStolen() == 3000 || player.getTimesStolen() == 3250
                            || player.getTimesStolen() == 3500 || player.getTimesStolen() == 3750 || player.getTimesStolen() == 4000) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received an Uncut Onyx from Thieving!", false);
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a Mystery box from Thieving!", false);

                    }
                    ThievingContractList.listenStall(player, Stalls.SCIMITAR_STALL);
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Mystery box from Thieving!", false);
                        return;
                    }
                    if (Utils.random(750) == 0) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/750 Uncut Onyx from Thieving!", false);
                        return;
                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(34023, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Small Protean Pack from Thieving!", false);
                        return;
                    }
                    if (player.getDailyManager().getTask() != null) {
                        if (object.getId() == player.getTaskItemId()) {
                            player.getDailyManager().processTask();
                        }
                    }
                    player.sendMessage("You've successfully stolen from this stall; " + "times thieved: " + Colors.RED + Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.", true);
                    if (Utils.random(35) == 0 && player.hasRandomEvent()) {
                        if (!player.followedByRandomEventNPC()) {
                            NPC npc = new ThievingRandomEvent(player, player);
                            if (npc.withinDistance(player, 14)) {
                                player.setCurrentRandomEventNPC(npc);
                                player.sendMessage("<col=ff0000>A Rogue appears out of nowhere.");
                            }
                        }
                    }
                } else {
                    player.sendMessage("You do not have enough inventory space to do this.", true);
                }
                return;
            }
            /**
             * Donator Zone Gem stall.
             */
            if (id == 34385) {
                if (player.getSkills().getLevel(Skills.THIEVING) < 90) {
                    player.sendMessage("You need a Thieving level of 90 to thieve from this stall.");
                    return;
                }
                if (player.getThievingDelay() > Utils.currentTimeMillis() || player.getActionManager().getActionDelay() != 0) {
                    return;
                }
                final Item[] rewards = {new Item(1602), new Item(1604), new Item(1606), new Item(1608), new Item(1631), new Item(1616), new Item(1617), new Item(1619), new Item(1621), new Item(1622), new Item(1624),};
                final Item item6 = rewards[Utils.random(rewards.length)];
                if (item6.getDefinitions().isStackable()) {
                    item6.setAmount(Utils.random(1, 3));
                }
                if (player.getEquipment().getCapeId() == 9778) {
                    if (item6.getDefinitions().isNoted()) {
                        item6.setAmount(Utils.random(3, 9));

                        player.getBank().addItem(new Item(item6), true);
                        player.sendMessage(Colors.ORANGE + "You Gained Extra Loot From Your Thieving Cape & Has Been Sent To Bank", true);
                    }


                }
                if (player.getInventory().getFreeSlots() < 2) {
                    player.sendMessage("You need atleast 2 spots free to thieve from this stall");
                    return;
                }
                if (player.getInventory().addItem(item6)) {
                    if (!player.getPerkManager().hasPerkActive(DonationPerk.SLEIGHT_OF_HAND)) {
                        player.applyHit(new Hit(player, Utils.random(1, 5), HitLook.REGULAR_DAMAGE, 1));
                    }
                    if (player.getTimesStolen() == 1500 || player.getTimesStolen() == 1750 || player.getTimesStolen() == 2000 || player.getTimesStolen() == 2250
                            || player.getTimesStolen() == 2500 || player.getTimesStolen() == 2750 || player.getTimesStolen() == 3000 || player.getTimesStolen() == 3250
                            || player.getTimesStolen() == 3500 || player.getTimesStolen() == 3750 || player.getTimesStolen() == 4000) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received an Uncut Onyx from Thieving!", false);
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a Mystery box from Thieving!", false);

                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Mystery box from Thieving!", false);
                        return;
                    }
                    if (Utils.random(750) == 0) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/750 Uncut Onyx from Thieving!", false);
                        return;
                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(34023, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Small Protean Pack from Thieving!", false);
                        return;
                    }
                    final int amount6 = player.getSkills().getLevelForXp(Skills.THIEVING) * 345;
                    player.addMoney(Utils.random(1, amount6));
                    player.setNextAnimation(THIEVING_ANIMATION);
                    player.setThievingDelay(Utils.currentTimeMillis() + 2100);
                    player.getContracts().recordAction(Skills.THIEVING, 12);
                    player.getSkills().addXp(Skills.THIEVING, 125);
                    player.addTimesStolen();
                    if (player.getDailyManager().getTask() != null) {
                        if (object.getId() == player.getTaskItemId()) {
                            player.getDailyManager().processTask();
                        }
                    }
                    player.sendMessage("You've successfully stolen from this stall; times thieved: " + Colors.RED + Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.", true);
                    if (Utils.random(35) == 0 && player.hasRandomEvent()) {
                        if (!player.followedByRandomEventNPC()) {
                            NPC npc = new ThievingRandomEvent(player, player);
                            if (npc.withinDistance(player, 14)) {
                                player.setCurrentRandomEventNPC(npc);
                                player.sendMessage("<col=ff0000>A Rogue appears out of nowhere.");
                            }
                        }
                    }
                } else {
                    player.sendMessage("You do not have enough inventory space to do this.", true);
                }
                return;

            }
            if (id == 75887) {
                if (player.getSkills().getLevel(Skills.THIEVING) < 95) {
                    player.sendMessage("You need a Thieving level of 95 to thieve from this stall.");
                    return;
                }
                if (player.getThievingDelay() > Utils.currentTimeMillis() || player.getActionManager().getActionDelay() != 0) {
                    return;
                }
                final Item[] rewards = {new Item(23362), new Item(23618), new Item(23406), new Item(212), new Item(214), new Item(208), new Item(1993), new Item(239), new Item(3049), new Item(219), new Item(2485), new Item(5299), new Item(37966),};
                final Item item6 = rewards[Utils.random(rewards.length)];
                if (item6.getDefinitions().isStackable()) {
                    item6.setAmount(Utils.random(1, 8));
                }
                if (player.getEquipment().getCapeId() == 9778) {
                    if (item6.getDefinitions().isNoted()) {
                        item6.setAmount(Utils.random(3, 9));

                        player.getBank().addItem(new Item(item6), true);
                        player.sendMessage(Colors.ORANGE + "You Gained Extra Loot From Your Thieving Cape & Has Been Sent To Bank", true);
                    }


                }
                if (player.getInventory().getFreeSlots() < 2) {
                    player.sendMessage("You need atleast 2 spots free to thieve from this stall");
                    return;
                }
                if (player.getInventory().addItem(item6)) {
                    if (!player.getPerkManager().hasPerkActive(DonationPerk.SLEIGHT_OF_HAND)) {
                        player.applyHit(new Hit(player, Utils.random(1, 5), HitLook.REGULAR_DAMAGE, 1));
                    }
                    if (player.getTimesStolen() == 1500 || player.getTimesStolen() == 1750 || player.getTimesStolen() == 2000 || player.getTimesStolen() == 2250
                    || player.getTimesStolen() == 2500 || player.getTimesStolen() == 2750 || player.getTimesStolen() == 3000 || player.getTimesStolen() == 3250
                    || player.getTimesStolen() == 3500 || player.getTimesStolen() == 3750 || player.getTimesStolen() == 4000 || player.getTimesStolen() == 4250 || player.getTimesStolen() == 4500 || player.getTimesStolen() == 4750 || player.getTimesStolen() == 5000
                            || player.getTimesStolen() == 5250 || player.getTimesStolen() == 5500 || player.getTimesStolen() == 5750 || player.getTimesStolen() == 6000) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received an Uncut Onyx from Thieving!", false);
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a Mystery box from Thieving!", false);

                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(6199, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Mystery box from Thieving!", false);
                        return;
                    }
                    if (Utils.random(750) == 0) {
                        player.getInventory().addItem(6571, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/750 Uncut Onyx from Thieving!", false);
                        return;
                    }
                    if (Utils.random(350) == 0) {
                        player.getInventory().addItem(34023, 1);
                        World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received a 1/350 Small Protean Pack from Thieving!", false);
                        return;
                    }
                    final int amount6 = player.getSkills().getLevelForXp(Skills.THIEVING) * 515;
                    player.addMoney(Utils.random(1, amount6));
                    player.setNextAnimation(THIEVING_ANIMATION);
                    player.setThievingDelay(Utils.currentTimeMillis() + 2100);
                    player.getContracts().recordAction(Skills.THIEVING, 12);
                    player.getSkills().addXp(Skills.THIEVING, 175);
                    player.addTimesStolen();
                    if (player.getDailyManager().getTask() != null) {
                        if (object.getId() == player.getTaskItemId()) {
                            player.getDailyManager().processTask();
                        }
                    }
                    player.sendMessage("You've successfully stolen from this stall; times thieved: " + Colors.RED + Utils.getFormattedNumber(player.getTimesStolen()) + "</col>.", true);
                    if (Utils.random(35) == 0 && player.hasRandomEvent()) {
                        if (!player.followedByRandomEventNPC()) {
                            NPC npc = new ThievingRandomEvent(player, player);
                            if (npc.withinDistance(player, 14)) {
                                player.setCurrentRandomEventNPC(npc);
                                player.sendMessage("<col=ff0000>A Rogue appears out of nowhere.");
                            }
                        }
                    }
                } else {
                    player.sendMessage("You do not have enough inventory space to do this.", true);
                }
                return;

            }
          else if (id == 62680) {
                player.getGEManager().openCollectionBox();
            } else if (id == 62677) {
                player.getDominionTower().openRewards();
            } else if (id >= 15477 && id <= 15482) {
                if (player.hasHouse) {
                    player.getHouse().setBuildMode(false);
                    player.getHouse().enterMyHouse();
                    return;
                }
                player.sendMessage("You must first purchase a house from the Estate Agent.");
            } else if (id == 62688) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You've a Dominion Factor of " + player.getDominionTower().getTotalScore() + ".");
            } else if (id == 68107) {
                FightKiln.enterFightKiln(player, true);
            } else if (id == 34384 || id == 34383 || id == 14011 || id == 7053 || id == 34387 || id == 34386 || id == 635) {
                Thieving.handleStalls(player, object);
            } else if (id == 2418) {
                PartyRoom.openPartyChest(player);
            } else if (id == 67051) {
                player.getDialogueManager().startDialogue("Marv", true);
            } else {
                switch (objectDef.name.toLowerCase()) {
                    case "crashed star":
                        if (objectDef.containsOption("Prospect")) {
                            ShootingStar.prospect(player);
                        }
                        break;
                    case "bank":
                    case "bank chest":
                    case "bank booth":
                    case "counter":
                    case "darkmeyer treasury":
                        // 947 cache: the Lumbridge bank chest (79036) names its second
                        // option "Use", not "Bank" as the 910 cache did. Only native players
                        // take that spelling; the legacy comparison is untouched.
                        if (objectDef.containsOption(1, "Bank") || (player.isNative950() && objectDef.containsOption(1, "Use"))) {
                            if (!player.promptList()) {
                                player.getBank().openBank();
                            } else {
                                player.getDialogueManager().startDialogue("BankList", false);
                            }
                        }
                        break;
                    case "gates":
                    case "gate":
                    case "metal door":
                        if (object.getType() == 0 && objectDef.containsOption(1, "Open")) {
                            handleGate(player, object);
                        }
                        break;
                    case "door":
                        if (object.getType() == 0 && objectDef.containsOption(1, "Open")) {
                            handleDoor(player, object);
                        }
                        break;
                    case "ladder":
                        handleLadder(player, object, 2);
                        break;
                    case "staircase":
                        handleStaircases(player, object, 2);
                        break;
                    default:
                        // player.sendMessage("Nothing
                        // interesting happens.");
                        break;
                }
            }
            if (Settings.DEBUG) {
                Logger.getGlobal().info("clicked 2 at object id : " + id + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane());
            }
        }));
    }

    private static void handleOption3(final Player player, final WorldObject object) {
        final ObjectDefinitions objectDef = object.getDefinitions();
        final int id = object.getId();
        if (player.isOwner()) {
            player.getPackets().sendPanelBoxMessage("Option 3:" + object.getId() + "; " + object.getX() + " " + object.getY() + " " + object.getPlane());
        }
        if (player.isLocked()) {
            return;
        }
        if (!HomeAreaHandler.playerCanInteractWithObject(player, object)) {
            HomeAreaHandler.sendCanNotUseNpcOrObjectMessageToPlayer(player, object.getDefinitions().getName(), HomeAreaHandler.Type.OBJECT);
            return;
        }
        if (isMetalBankObject(id)) {
            player.setRouteEvent(new RouteEvent(object, () -> {
                player.stopAll();
                player.faceObject(object);
                if (!player.getControlerManager().processObjectClick3(object)) {
                    return;
                }
                MetalBank.openWithdraw(player);
            }, true));
            return;
        }
        if (object.getId() == 45802) {
            player.stopAll(true);
            player.setRouteEvent(new RouteEvent(new WorldTile(414, 674, 0), () -> {
                player.faceObject(object);
                ShopsDataParser.openShop(player, 58);
            }, false));
            return;
        }

        if (object.getId() == 6775) {
            player.setNextWorldTile(new WorldTile(3565, 3288, 0));
            player.getControlerManager().removeControlerWithoutCheck();
            return;
        }
        
        player.setRouteEvent(new RouteEvent(object, () -> {
            player.stopAll();
            player.faceObject(object);
            if (!player.getControlerManager().processObjectClick3(object)) {
                return;
            }
            if (PrifddinasCity.handleObjectOption3(player, object)) {
                return;
            }
            if (PortableStation.isPortableObject(object)) {
                PortableStation.handleObjectClick3(player, object);
                return;
            }
            if(player.vineHerbPatches.handleOption3(object)) {
                return;
            }
            if(player.harmonyPillars.handleOption3(object)) {
                return;
            }
            if(object.getId() == 48675) {
                RichestBanksD.showIm(player);
                return;
            }
            if (object instanceof EvilTreeObject) {
                val tree = (EvilTreeObject) object;
                tree.inspect(player);
                return;
            }
            if (id == 43808) {
                player.getInterfaceManager().sendInterface(1937);
                return;
            }
            if (id == 3192) {
                GIM.getHighscores().searchPlayer(player);
                return;
            }
            if (id == 62688) {
                player.sendMessage("Dominion Tower rewards are obtainable from the Tower Head downstairs.");
            }
            if (id == 72927 || id == 72933) {
                Crucible.leaveArena(player);
            }
            if (id == 87306) {
                player.getActionManager().setAction(new DivinationConvert(player, new Object[]{ConvertMode.CONVERT_TO_XP}));
                return;
            }
            if (id == 88923) {
                ChristmasSeasonalEvent.viewNiceHighscores(player);
                return;
            }
            if (object.getId() == 38811) {
                player.getDialogueManager().startDialogue("CorporealBeastInstanceD", 8133);
                return;
            }
            final String objectName = objectDef.name.toLowerCase();

            if (objectName.contains("bank") || objectName.contains("counter") || objectName.contains("darkmeyer treasury")) {
                player.getGEManager().openCollectionBox();
                return;
            }
            if (object.getId() == 91172) {
                if (object.getX() == 2328 && object.getY() == 3176) {
                    if (player.withinDistance(new WorldTile(object.getX(), object.getY(), object.getPlane()))) {
                        player.setNextWorldTile(new WorldTile(2327, 3176, 0));
                    }
                }
                if (object.getX() == 2325 && object.getY() == 3168) {
                    if (player.withinDistance(new WorldTile(object.getX(), object.getY(), object.getPlane()))) {
                        player.setNextWorldTile(new WorldTile(2327, 3168, 0));
                    }
                }
            }

            /**
             * Slayer Tower.
             */
            if (id == 82667 || id == 82483) { /** Staircases **/
                player.useStairs(-1, new WorldTile(player.getX(), player.getY(), 0), 0, 0);
                return;
            } else if (id >= 15477 && id <= 15482) {
                if (player.hasHouse) {
                    player.getHouse().setBuildMode(true);
                    player.getHouse().enterMyHouse();
                    return;
                }
                player.sendMessage("You must first purchase a house from the Estate Agent.");
            }
            if (player.getFarmingManager().isFarming(object, null, 3)) {
                return;
            }
            switch (objectDef.name.toLowerCase()) {
                case "gate":
                case "metal door":
                    if (object.getType() == 0 && objectDef.containsOption(2, "Open")) {
                        handleGate(player, object);
                    }
                    break;

                case "door":
                    if (object.getType() == 0 && objectDef.containsOption(2, "Open")) {
                        handleDoor(player, object);
                    }
                    break;
                case "ladder":
                    handleLadder(player, object, 3);
                    break;
                case "staircase":
                    handleStaircases(player, object, 3);
                    break;
                default:
                    // player.sendMessage("Nothing interesting happens.");
                    break;
            }
            if (Settings.DEBUG) {
                Logger.getGlobal().info("clicked 3 at object id : " + id + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane() + ", ");
            }
        }));
    }

    private static void handleOption4(final Player player, final WorldObject object) {
        final ObjectDefinitions objectDef = object.getDefinitions();
        final int id = object.getId();
        if (player.isOwner()) {
            player.getPackets().sendPanelBoxMessage("Option 4:" + object.getId() + "; " + object.getX() + " " + object.getY() + " " + object.getPlane());
        }
        if (player.isLocked()) {
            return;
        }
        if (!HomeAreaHandler.playerCanInteractWithObject(player, object)) {
            HomeAreaHandler.sendCanNotUseNpcOrObjectMessageToPlayer(player, object.getDefinitions().getName(), HomeAreaHandler.Type.OBJECT);
            return;
        }
        player.setRouteEvent(new RouteEvent(object, () -> {
            player.stopAll();
            player.faceObject(object);
            if (!player.getControlerManager().processObjectClick4(object)) {
                return;
            }
            if (objectDef.name.contains("Bank") && objectDef.containsOption("PIN-settings")) {
                AccountPin.openSettings(player);
                return;
            }
            if (PortableStation.isPortableObject(object)) {
                PortableStation.handleObjectClick4(player, object);
                return;
            }
            if(player.vineHerbPatches.handleOption4(object)) {
                return;
            }
            if(player.harmonyPillars.handleOption4(object)) {
                return;
            }
            if(object.getId() == 48675) {
                RichestBanksD.showHcim(player);
                return;
            }
            if (object.getId() == 110591) {

                    player.setNextWorldTile(new WorldTile(2595, 3411, 0));
                    player.faceObject(object);

                return;
            }
            // living rock Caverns
            if (id == 88923) {
                ChristmasSeasonalEvent.viewNaughtyHighscores(player);
                return;
            }
            if (id == 113017 || id == 113018 || id == 113019) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Light Animica Rock | Level 99 For %15 Rate | Rock Rate %7 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | If Your Pickaxe is Under 90, it has a chance to lower your ore rate", true);
                return;
            }
            if (id == 113073 || id == 113072 || id == 113071 || id == 113133 || id == 113131 || id == 113132) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Drakolith Rock | Level 75 For %15 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | Higher Pickaxe Tier Has A Chance To Higher Your Ore Rate", true);
                return;
            }
            if (id == 113130 || id == 113128) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Orichalcite Rock | Level 75 For %15 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | Higher Pickaxe Tier Has A Chance To Higher Your Ore Rate", true);
                return;
            }
            if (id == 113020 || id == 113021 || id == 113022) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Dark Animica Rock | Level 99 For %15 Rate | Rock Rate %7 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | If Your Pickaxe is Under 90, it has a chance to lower your ore rate", true);
                return;
            }
            if (id == 113142 || id == 113204 || id == 113203 || id == 113080 || id == 113140 || id == 113205) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Banite Rock | Level 85 For %5 Rate | Rock Rate %14 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | If Your Pickaxe is Under 80, it has a chance to lower your ore rate", true);
                return;
            }
            if (id == 113016) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>seren stone rock | Level 95 For %5 Rate | Rock Rate %7 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | If Your Pickaxe is Under 89, it has a chance to lower your ore rate", true);
                return;
            }
            if (id == 113206 || id == 113207 || id == 113208) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Necrite rock | Level 80 For %5 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | If Your Pickaxe is Under 89, it has a chance to lower your ore rate", true);
                return;
            }
            if (id == 113137 || id == 113138 || id == 113139) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Phasmatite rock | Level 80 For %5 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | If Your Pickaxe is Under 89, it has a chance to lower your ore rate", true);
                return;
            }
            if (id == 113052 || id == 113050 || id == 113051 || id == 112926 || id == 112925) {

                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>mithril rock | Level 45 For %15 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | Higher Pickaxe Tier Has A Chance To Higher Your Ore Rate", true);
                return;
            }
            if (id == 113055 || id == 113053 || id == 113054 || id == 112942 || id == 112941 || id == 113177 || id == 113178) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>adamantite rock | Level 55 For %15 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | Higher Pickaxe Tier Has A Chance To Higher Your Ore Rate", true);
                return;
            }
            if (id == 112896) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Donor Ore Rock | Level 40 For %10 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | Higher Pickaxe Tier Has A Chance To Higher Your Ore Rate", true);
                return;
            }
            if (id == 113045 || id == 113044 || id == 113046 || id == 113164 || id == 113166 || id == 113165) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Silver Rock | Level 35 For %10 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | Higher Pickaxe Tier Has A Chance To Higher Your Ore Rate", true);
                return;
            }
            if (id == 113184 || id == 113182 || id == 113183 || id == 113059 || id == 113061 || id == 113060) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Gold Rock | Level 55 For %10 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | Higher Pickaxe Tier Has A Chance To Higher Your Ore Rate", true);
                return;
            }
            if (id == 113116 || id == 113179 || id == 113181 || id == 113118) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Luminite Rock | Level 55 For %10 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | Higher Pickaxe Tier Has A Chance To Higher Your Ore Rate", true);
                return;
            }
            if (id == 113146 || id == 113148 || id == 113147 || id == 113026 || id == 113027) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Copper Rock | Level 35 For %50 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | Higher Pickaxe Tier Has A Chance To Higher Your Ore Rate", true);
                return;
            }
            if (id == 113154 || id == 113152 || id == 113153) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>clay Rock | Level 35 For %50 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | Higher Pickaxe Tier Has A Chance To Higher Your Ore Rate", true);
                return;
            }
            if (id == 113150 || id == 113151 || id == 113149 || id == 113031) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Tin Rock | Level 35 For %50 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | Higher Pickaxe Tier Has A Chance To Higher Your Ore Rate", true);
                return;
            }
            if (id == 113040 || id == 113038 || id == 113039) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Iron Rock | Level 35 For %50 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | Higher Pickaxe Tier Has A Chance To Higher Your Ore Rate", true);
                return;
            }
            if (id == 112963 || id == 112964 || id == 113067 || id == 113065) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Runite Rock | Level 65 For %15 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | Higher Pickaxe Tier Has A Chance To Higher Your Ore Rate", true);
                return;
            }
            if (id == 112999 || id == 112998) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Gem Rock | Level 60 For %15 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | Higher Pickaxe Tier Has A Chance To Higher Your Ore Rate", true);
                return;
            }
            if (id == 113043 || id == 113041 || id == 113042 || id == 112912 || id == 112911) {
                player.getPackets().sendPlayerMessage(1, 15263739, player, "<col=0867af>Coal Rock | Level 45 For %15 Rate | Rock Rate %17 | Tier 90-92 pickaxes, Ore Rate %7 - %10 | Higher Pickaxe Tier Has A Chance To Higher Your Ore Rate", true);
                return;
            }
            if (id == 87306) {
                player.getActionManager().setAction(new DivinationConvert(player, new Object[]{ConvertMode.CONVERT_TO_MORE_XP}));
                return;
            }
            if (id == 3192) {
                GIM.getHighscores().displayPreviousWinners(player);
                return;
            }
            if (id == 43808) {
                WellOfGoodWill.give(player);
                return;
            }

            if (id == 113010) {
                MiningBase.prospect(player, "This rock contains a large concentration of gold.");
            } else if (id == 113009) {
                MiningBase.prospect(player, "This rock contains a large concentration of coal.");
            } else if (id >= 15477 && id <= 15482) {
                player.sendInputName("Please enter the name of the player:", new InputNameEvent() {
                    @Override
                    public void run(final Player player) {
                        House.enterHouse(player, getString());
                    }
                });
            } else if (player.getFarmingManager().isFarming(object, null, 4)) {
                return;
            } else {
                switch (objectDef.name.toLowerCase()) {
                    case "crashed star":
                        if (objectDef.containsOption("Prospect")) {
                            ShootingStar.prospect(player);
                        }
                        break;
                    default:
                        // player.sendMessage("Nothing interesting happens.");
                        break;
                }
            }
            if (Settings.DEBUG) {
                Logger.getGlobal().info("clicked 4 at object id : " + id + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane() + ", ");
            }
        }));
    }

    private static void handleOption5(final Player player, final WorldObject object) {
        final ObjectDefinitions objectDef = object.getDefinitions();
        final int id = object.getId();
        if (player.isOwner()) {
            player.getPackets().sendPanelBoxMessage("Option 5:" + object.getId() + "; " + object.getX() + " " + object.getY() + " " + object.getPlane());
        }
        if (player.isLocked()) {
            return;
        }
        if (!HomeAreaHandler.playerCanInteractWithObject(player, object)) {
            HomeAreaHandler.sendCanNotUseNpcOrObjectMessageToPlayer(player, object.getDefinitions().getName(), HomeAreaHandler.Type.OBJECT);
            return;
        }
        player.setRouteEvent(new RouteEvent(object, () -> {
            player.stopAll();
            player.faceObject(object);
            if (!player.getControlerManager().processObjectClick5(object)) {
                return;
            }
            /*
             * if (id == 82016) { // Kalphite King stairs - exit. player.lock();
             * player.setNextAnimation(new Animation(19499)); WorldTasksManager.schedule(new
             * WorldTask() {
             *
             * @Override public void run() { player.setNextAnimation(new Animation(-1));
             * player.setNextWorldTile(new WorldTile(2971, 1656, 0)); player.unlock();
             * stop(); } }, 3); return; }
             */
            if (objectDef.containsOption("Push-through")) {
                PuroPuro.pushThrough(player, object);
                return;
            }
            if(object.getId() == 48675) {
                RichestBanksD.showGim(player);
                return;
            }
            if (id == -1) {
                // unused
            } else {
                switch (objectDef.name.toLowerCase()) {
                    case "fire":
                        if (objectDef.containsOption(4, "Use")) {
                            if(!Bonfire.addLogs(player, object)) {
                                Cooking.useCook(player, object);
                            }
                        }
                        break;
                    case "fire pit":
                        if (objectDef.containsOption(1, "Add logs")) {
                            Bonfire.addLogs(player, object);
                        }
                    default:
                        player.sendMessage("Nothing interesting happens.", true);
                        break;
                }
            }
            if (Settings.DEBUG) {
                Logger.getGlobal().info("clicked 5 at object id : " + id + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane() + ", ");
            }
        }));
    }

    public static void handleItemOnObject(final Player player, final WorldObject object, final int interfaceId, final Item item) {
        final int itemId = item.getId();
        final ObjectDefinitions objectDef = object.getDefinitions();
        final ItemDefinitions itemDef = item.getDefinitions();
        if (itemId == 11782 && player.isOwner()) {
            World.removeObject(object, true);
            player.sendMessage("You deleted " + object.getId() + " at " + object.getX() + "/" + object.getY());
        }
        if (itemId == 1037 && player.isOwner()) {
            if (object.getRotation() + 1 < 4) {
                object.setRotation(object.getRotation() + 1);
            } else {
                object.setRotation(0);
            }
            player.sendMessage("You have rotated " + object.getId() + " at " + object.getX() + "/" + object.getY());
        }
        if (player.isOwner()) {
            player.getPackets().sendPanelBoxMessage("Item: " + item.getId() + "; used on: " + object.getId() + "; " + object.getX() + " " + object.getY() + " " + object.getPlane());
        }
        if (!HomeAreaHandler.playerCanInteractWithObject(player, object)) {
            HomeAreaHandler.sendCanNotUseNpcOrObjectMessageToPlayer(player, object.getDefinitions().getName(), HomeAreaHandler.Type.OBJECT);
            return;
        }
        final String objName = object.getDefinitions().getName().toLowerCase();
        if (objName.contains("tile") || objName.contains("hole") || objName.contains("pressure plate")) {
            FishingFerretRoom.handleFerretThrow(player, object, item);
            return;
        }

        if (object.getId() == 1996) {
            if (item.getId() != 954) {
                return;
            }
            for (int i = 0; i < 7; i++) {
                final WorldObject o = new WorldObject(object);
                o.setId(1998);
                o.setLocation(o.getX(), 3475 - i, o.getPlane());
                player.getPackets().sendSpawnedObject(o);
            }
            final int amt = player.getInventory().getAmountOf(itemId);
            switch (object.getId()) {
                case 36181:
                    if (itemId == 17413) {
                        for (int i = 1; i <= amt; i++) {
                            player.getSkills().addXp(Skills.HERBLORE, 50);
                            player.getInventory().deleteItem(17413, 1);
                            player.getInventory().addItem(17568, 1);
                            player.sendMessage(Colors.ORANGE + "You siphon some blood into the water. It seems to be drained of red blood cells.", true);
                        }
                    } else {
                        player.sendMessage(Colors.RED + "I don't think that goes here..");
                    }
                    break;

                case 10158:
                    if (itemId == 17568) {
                        if (!(player.getSkills().getLevel(Skills.HERBLORE) >= 55)) {
                            player.sendMessage(Colors.RED + "You need at least 55 herblore to make this mixture!", true);
                        } else {
                            for (int i = 1; i <= amt; i++) {
                                player.getSkills().addXp(Skills.HERBLORE, 175);
                                player.getInventory().deleteItem(17568, 1);
                                player.getInventory().addItem(17592, 1);
                                player.sendMessage(Colors.ORANGE + "You mix the liquids into a stronger dose.", true);
                            }
                        }
                    } else {
                        player.sendMessage(Colors.RED + "I don't think that goes here..", true);
                    }
            }

            final WorldObject o = new WorldObject(object);
            o.setId(1997);
            player.getPackets().sendSpawnedObject(o);
            player.getAppearence().setRenderEmote(188);
            player.setNextForceMovement(new ForceMovement(object, 8, ForceMovement.SOUTH));
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    for (int i = 0; i < 7; i++) {
                        final WorldObject o = new WorldObject(object);
                        o.setId(1998);
                        o.setLocation(o.getX(), 3475 - i, o.getPlane());
                        player.getPackets().sendDestroyObject(o);
                    }
                    final WorldObject o = new WorldObject(object);
                    o.setId(1996);
                    player.getPackets().sendSpawnedObject(o);
                    player.getAppearence().setRenderEmote(-1);
                    player.setNextWorldTile(new WorldTile(2513, 3468, 0));
                }
            }, 7);
        }
        player.setRouteEvent(new RouteEvent(object, () -> {
            player.faceObject(object);
            if (!player.getControlerManager().handleItemOnObject(object, item)) {
                return;
            }


            // --- Single-item -> gold (item-on-object) ---
            if (object.getId() == 109776) { // your converter object
                if (item.getId() == 995) {
                    player.sendMessage("That's already coins.");
                    return;
                }


                int valueEach;
                try {
                    valueEach = EconomyPrices.getAlchCoins(item, AlchTier.TABLE);
                } catch (Exception e) {
                    player.sendMessage("That item can't be converted to coins.");
                    return;
                }

                if (valueEach <= 0) {
                    player.sendMessage("That item can't be converted to coins.");
                    return;
                }

                // convert exactly ONE of the item used
                final int convertQty = player.getInventory().getAmountOf(item.getId());
                if (!player.getInventory().containsItem(item.getId(), convertQty)) {
                    player.sendMessage("You don't have that item anymore.");
                    return;
                }

                player.getInventory().deleteItem(item.getId(), convertQty);
                player.getMoneyPouch().addMoneyMisc(valueEach * convertQty);

                // Optional: add alch animation/sound if you have them
                // player.setNextAnimation(new Animation(713));

                int totalCoins;
                try {
                    totalCoins = Math.multiplyExact(valueEach, convertQty); // throws if overflow
                } catch (ArithmeticException ex) {
                    totalCoins = Integer.MAX_VALUE; // or clamp to pouch space if you track that
                }

                player.sendMessage(
                        "You convert " + Utils.getFormattedNumber(convertQty) + " � " + item.getDefinitions().getName()
                                + " into " + Utils.getFormattedNumber(totalCoins) + (totalCoins == 1 ? " coin." : " coins.")
                );
            }




            if (player.getFarmingManager().isFarming(object, item, 0)) {
                return;
            }
            if (PrifddinasCity.handleItemOnObject(player, object, item)) {
                return;
            }
            if (Mogre.spawnMogre(player, item, object)) {
                return;
            }
            if(item.getId() == 32665 &&
                    object.getId() == 94331 && player.harmonyPillars.plant(object)) {
                return;
            }
            if(object.getId() == 56697 && player.vineHerbPatches.plant(item.getId(), object)) {
                return;
            }
            if (Ectofuntus.manageIOB(player, itemId, object.getId())) {
                return;
            }
            if (itemId == BarrowsAmulet.BARROWS_AMULET_ID) {
                BarrowsAmulet.use(player, object.getId(), true);
                return;
            }
            if (object.getDefinitions().getName().equalsIgnoreCase("Armour stand")) {
                player.getChargesManagerNew().sendRechargeItemDialogue(item, -1);
                return;
            }
            if (objName.contains("bank")) {
                if (itemDef.isStackable() && !itemDef.isNoted()) {
                    player.sendMessage("Stackable items cannot be exchanged for bank notes.");
                    return;
                }
                if (player.getInventory().getAmountOf(itemId) == 1) {
                    NoteBankD.exchange(player, item);
                } else {
                    player.getDialogueManager().startDialogue("NoteBankD", itemId);
                }
                return;
            }
            if (object.getDefinitions().getName().toLowerCase().contains("singing bowl")) {
                BowlSinging.revertToCrystal(player, item);
                return;
            }
            if (object.getId() == 41467) {
                if (item.getId() >= 7928 && item.getId() <= 7933) {
                    if (player.startedEasterEvent) {
                        player.setNextAnimation(new Animation(11490));
                        player.sendMessage("You insert an easter egg into the cannon and fire it up.");
                        player.getInventory().deleteItem(item.getId(), 1);
                        CoresManager.getServiceProvider().executeWithDelay(() -> {
                            final NPC goodEasterBunny = World.findNPC(15754);
                            World.sendProjectile(object, object, goodEasterBunny, 979, 25, 25, 20, 0, 0, 2);
                            World.sendProjectile(object, object, goodEasterBunny, 2036, 25, 25, 20, 0, 0, 2);

                            CoresManager.getServiceProvider().executeWithDelay(() -> {
                                World.sendGraphics(goodEasterBunny, new Graphics(4868), goodEasterBunny);
                                goodEasterBunny.applyHit(new Hit(goodEasterBunny, 30, HitLook.HEALED_DAMAGE));
                                final int easterPoints = Utils.random(23, 30);
                                player.sendMessage("You have earned " + easterPoints + " easter points!");
                                player.easterPoints += easterPoints;
                                player.sendMessage("Total Easter Points: " + player.easterPoints);
                            }, 2500, TimeUnit.MILLISECONDS);
                        }, 4, TimeUnit.SECONDS);
                    } else {
                        player.sendMessage("You probably shouldn't touch this before talking to the easter bunny.");
                    }
                }

            }

            if (itemId == 954 && object.getId() == 28494) {
                player.setAttachedAgilityPyramidRope();
                player.getPackets().addSpawnedObject(new WorldObject(28488, 10, 1, 3382, 2824, 1));
                player.getPackets().addSpawnedObject(new WorldObject(28490, 10, 1, 3382, 2825, 0));
                player.sendMessage("You attach the rope to the rock.");
                player.getInventory().deleteItem(954, 1);
                return;
            }

            if (itemId >= 1438 && itemId <= 1450) {
                for (int index = 0; index < RuneCrafting.OBJECTS.length; index++) {
                    if (RuneCrafting.OBJECTS[index] == object.getId()) {
                        RuneCrafting.infuseTiara(player, index);
                        break;
                    }
                }
            } else if (object.getId() == 28352 || object.getId() == 28550) {
                Incubator.useEgg(player, itemId);
            } else if (itemId == 1438 && object.getId() == 2452) {
                RuneCrafting.enterAirAltar(player);
            } else if (itemId == 1440 && object.getId() == 2455) {
                RuneCrafting.enterEarthAltar(player);
            } else if (itemId == 1442 && object.getId() == 2456) {
                RuneCrafting.enterFireAltar(player);
            } else if (itemId == 1444 && object.getId() == 2454) {
                RuneCrafting.enterWaterAltar(player);
            } else if (itemId == 1446 && object.getId() == 2457) {
                RuneCrafting.enterBodyAltar(player);
            } else if (itemId == 1448 && object.getId() == 2453) {
                RuneCrafting.enterMindAltar(player);

            } else if (object.getId() == 733 || object.getId() == 64729) {
                player.setNextAnimation(new Animation(-1));
                slashWeb(player, object);
            } else if (object.getId() == 48803 && itemId == 954) {
                if (player.isKalphiteLairSetted()) {
                    return;
                }
                player.getInventory().deleteItem(954, 1);
                player.setKalphiteLair();
            } else if (PortableStation.isPortableObject(object) && itemId != Herblore.EMPTY_VIAL) {
                PortableStation.handleObjectClick1(player, object);
            } else if (object.getId() == 43808 && itemId == 995) {
                WellOfGoodWill.give(player);
            } else if (object.getId() == 11666 || object.getId() == 88261) { // Falador
                // furnace
                if (itemId == 2357) {
                    CraftingRs3Dialogue.sendGoldJewelleryInterface(player);
                    return;
                }
                if (itemId == 2355) {
                    CraftingRs3Dialogue.sendSilverCraftingInterface(player, object);
                    return;
                }
                player.getDialogueManager().startDialogue("SmeltingD", object);
                return;
            } else if (object.getId() == 82049 && itemId == 954) {
                if (player.isKalphiteLairEntranceSetted()) {
                    return;
                }
                player.getInventory().deleteItem(954, 1);
                player.setKalphiteLairEntrance();
            } else if (object.getId() == 51061 || object.getId() == 8749 || object.getId() == 61336 || object.getId() == 97069 || object.getId() == 13197 || object.getId() == 24343 || object.getId() == 61) {
                final Bones bone = BonesOnAltar.isGood(item);
                if (bone != null) {
                    //player.getDialogueManager().startDialogue("PrayerD", bone, object);
                    player.getActionManager().setAction(new BonesOnAltar(object, item));
                    return;
                } else {
                    player.sendMessage("Nothing interesting happens.");

                }
                return;
            } else {
                switch (objectDef.name.toLowerCase()) {

                    case "anvil":
                    case "forge":
                    case "burial anvil":
                    case "burial forge":
                    case "barbarian anvil":
                    case "barbarian forge":
                    case "fremennik anvil":
                    case "fremennik forge":
                        if (Smithing.handleUnfinishedItemOnSmithingStation(player, item, object))
                            return;
                        if (player.getInventory().containsItem(new Item(31350))) {
                            player.getDialogueManager().startDialogue("ProteanSmithingD", player.getInventory().getAmountOf(31350) > 60 ? 60 : player.getInventory().getAmountOf(31350), false);
                            return;
                        }
                        final ForgingBar bar = ForgingBar.forId(itemId);
                        if (bar == ForgingBar.DRACONIC_VISAGE || itemId == 1540) {
                            player.getDialogueManager().startDialogue("DFSSmithingD");
                            return;
                        }
                        if (itemId == 14472 || itemId == 14474 || itemId == 14476) {
                            player.getDialogueManager().startDialogue("DPlateBodySmithingD");
                            return;
                        }
                        if (bar != null) {
                            ForgingInterface.sendSmithingInterface(player, bar, object);
                        }
                        break;

                    /**
                     * Charging unpowered orbs.
                     */
                    case "obelisk of air":
                        if (itemId == 567) {
                            player.getActionManager().setAction(new ChargeAirOrb());
                            return;
                        }
                        player.sendMessage("Nothing interesting happens.", true);
                        return;

                    case "obelisk of water":
                        if (itemId == 567) {
                            player.getActionManager().setAction(new ChargeWaterOrb());
                            return;
                        }
                        player.sendMessage("Nothing interesting happens.", true);
                        return;

                    case "obelisk of earth":
                        if (itemId == 567) {
                            player.getActionManager().setAction(new ChargeEarthOrb());
                            return;
                        }
                        player.sendMessage("Nothing interesting happens.", true);
                        return;

                    case "obelisk of fire":
                        if (itemId == 567) {
                            player.getActionManager().setAction(new ChargeFireOrb());
                            return;
                        }
                        player.sendMessage("Nothing interesting happens.", true);
                        return;
                    case "water barrel":
                    case "sink":
                    case "fountain":
                    case "well":
                    case "pump":
                    case "water trough":
                    case "portable well":
                        WaterFilling.isFilling(player, itemId, false);
                        break;
                    case "cooking station":
                        final Cookables cook2 = Cooking.isCookingSkill(item);
                        if (cook2 != null) {
                            if (item.getId() == 15272) {
                                Cooking.performPortableAction(player, object, cook2);
                                return;
                            }
                        } else {
                            player.getDialogueManager().startDialogue("SimpleMessage", "You can't use that item on the cooking station.");
                        }
                        break;
                    case "fire":
                    case "lava crater":
                        Cookables cook = Cooking.isCookingSkill(item);
                        if (Bonfire.addLog(player, object, item)) {
                            return;
                        } else if (cook != null) {
                            Cooking.performPortableAction(player, object, cook);
                            return;
                        } else {
                            player.getDialogueManager().startDialogue("SimpleMessage", "You can't use that item on the fire.");
                        }
                        break;
                    case "furnace":
                        if (itemId == 2357) {
                            CraftingRs3Dialogue.sendGoldJewelleryInterface(player);
                            return;
                        }
                        if (itemId == 2355) {
                            CraftingRs3Dialogue.sendSilverCraftingInterface(player, object);
                            return;
                        }
                        break;
                    case "grill":
                    case "range":
                    case "cooking range":
                    case "stove":
                    case "null":
                        if (objectDef.name.equalsIgnoreCase("null") && object.getId() != 91162) {
                            return;
                        }
                        if (itemId == 2132 || itemId == 2134 || itemId == 2136) {
                            final Dialogue dialogue = new Dialogue() {

                                @Override
                                public void start() {
                                    sendOptionsDialogue("Select an Option", "Dry into sinew", "Regular cooking");
                                }

                                @Override
                                public void run(final int interfaceId, final int componentId) {
                                    end();
                                    switch (componentId) {
                                        case OPTION_1:
                                            player.getActionManager().setAction(new SinewCooking());
                                            break;
                                        case OPTION_2:
                                            final Cookables cook = Cooking.isCookingSkill(item);
                                            if (cook != null) {
                                                Cooking.performPortableAction(player, object, cook);
                                                return;
                                            }
                                            player.getDialogueManager().startDialogue("SimpleMessage", "You can't cook that on a " + objectDef.name + ".");
                                            break;
                                    }
                                }

                                @Override
                                public void finish() {
                                }

                            };
                            player.getDialogueManager().startDialogue(dialogue);
                            return;
                        }
                        cook = Cooking.isCookingSkill(item);
                        if (cook != null) {
                            Cooking.performPortableAction(player, object, cook);
                            return;
                        }
                        player.getDialogueManager().startDialogue("SimpleMessage", "You can't cook that on a " + objectDef.name + ".");
                        break;
                    default:
                        break;
                }
                if (Settings.DEBUG) {
                    Logger.getGlobal().info("Item on object: " + object.getId() + " named " + object.getDefinitions().getName());
                }
            }
        }));
    }

    private static void handleOptionExamine(final Player player, final WorldObject object) {


        if (!player.getControlerManager().processObjectExamine(object)) {
            return;
        }
        player.getPackets().sendObjectMessage(0, 15263739, object, "It's a " + object.getDefinitions().name + ".");
        Logger.getGlobal().info("examined object id : " + object.getId() + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane() + ", " + object.getType() + ", " + object.getRotation() + ", " + object.getDefinitions().name + ", varbitId=" + object.getDefinitions().configFileId + ", varId=" + object.getDefinitions().configId);

        player.getPackets().sendResetMinimapFlag();

        // player.sendMessage("It's an " + object.getDefinitions().name + ".");
        if (Settings.DEBUG) {
            Logger.getGlobal().info("examined object id : " + object.getId() + ", " + object.getX() + ", " + object.getY() + ", " + object.getPlane() + ", " + object.getType() + ", " + object.getRotation() + ", " + object.getDefinitions().name + ", varbitId=" + object.getDefinitions().configFileId + ", varId=" + object.getDefinitions().configId);
        }
    }

    public static boolean handleDoor(final Player player, final WorldObject object, final long timer) {
        if (World.isSpawnedObject(object)) {
            return false;
        }
        final WorldObject openedDoor = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY(), object.getPlane());
        if (object.getRotation() == 0) {
            openedDoor.moveLocation(-1, 0, 0);
        } else if (object.getRotation() == 1) {
            openedDoor.moveLocation(0, 1, 0);
        } else if (object.getRotation() == 2) {
            openedDoor.moveLocation(1, 0, 0);
        } else if (object.getRotation() == 3) {
            openedDoor.moveLocation(0, -1, 0);
        }
        if (World.removeObjectTemporary(object, timer, true)) {
            player.faceObject(openedDoor);
            World.spawnObjectTemporary(openedDoor, timer);
            return true;
        }
        return false;
    }

    public static boolean handleDoor(final Player player, final WorldObject object) {
        return handleDoor(player, object, 60000);
    }

    private static boolean handleStaircases(final Player player, final WorldObject object, final int optionId) {
        final String option = object.getDefinitions().getOption(optionId);
        if (option.equalsIgnoreCase("Climb-up")) {
            if (player.getPlane() == 3) {
                return false;
            }
            player.useStairs(-1, new WorldTile(player.getX(), player.getY(), player.getPlane() + 1), 0, 1);
        } else if (option.equalsIgnoreCase("Climb-down")) {
            if (player.getPlane() == 0) {
                return false;
            }
            player.useStairs(-1, new WorldTile(player.getX(), player.getY(), player.getPlane() - 1), 0, 1);
        } else if (option.equalsIgnoreCase("Climb")) {
            if (player.getPlane() == 3 || player.getPlane() == 0) {
                return false;
            }
            player.getDialogueManager().startDialogue("ClimbNoEmoteStairs", new WorldTile(player.getX(), player.getY(), player.getPlane() + 1), new WorldTile(player.getX(), player.getY(), player.getPlane() - 1), "Go up the stairs.", "Go down the stairs.");
        } else {
            return false;
        }
        return false;
    }

    private static boolean handleLadder(final Player player, final WorldObject object, final int optionId) {

        // Edits
        if (object.getId() == 39191 && object.getX() == 3241 && object.getY() == 9990) {
            player.setNextAnimation(new Animation(828));
            Magic.sendObjectTeleportSpell(player, true, player.getHomeTile());
            return true;
        }

        final String option = object.getDefinitions().getOption(optionId);
        if (option.equalsIgnoreCase("Climb-up") || option.equalsIgnoreCase("Climb up")) {
            if (player.getPlane() == 3) {
                return false;
            }
            player.useStairs(828, new WorldTile(player.getX(), player.getY(), player.getPlane() + 1), 1, 2);
        } else if (option.equalsIgnoreCase("Climb-down") || option.equalsIgnoreCase("Climb down")) {
            if (player.getPlane() == 0) {
                return false;
            }
            player.useStairs(828, new WorldTile(player.getX(), player.getY(), player.getPlane() - 1), 1, 2);
        } else if (option.equalsIgnoreCase("Climb")) {
            if (player.getPlane() == 3 || player.getPlane() == 0) {
                return false;
            }
            player.getDialogueManager().startDialogue("ClimbEmoteStairs", new WorldTile(player.getX(), player.getY(), player.getPlane() + 1), new WorldTile(player.getX(), player.getY(), player.getPlane() - 1), "Climb up the ladder.", "Climb down the ladder.", 828);
        } else {
            return false;
        }
        return true;
    }

    private static void slashWeb(final Player player, final WorldObject object) {
        boolean usingKnife = false;
        int defs = player.getCombatDefinitions().getStyle(false);
        if (defs != Combat.SLASH_STYLE) {
            if (!player.getInventory().containsOneItem(946)) {
                player.sendMessage("You need something sharp to cut this with.");
                return;
            }
            usingKnife = true;
        }
        final int weaponEmote = PlayerCombat.getAttackAnimation(player, true);
        final int knifeEmote = 3747;
        player.setNextAnimation(new Animation(usingKnife ? knifeEmote : weaponEmote));
        if (Utils.getRandom(1) == 0) {
            World.spawnObjectTemporary(new WorldObject(object.getId() + 1, object.getType(), object.getRotation(), object.getX(), object.getY(), object.getPlane()), 60000);
            player.sendMessage("You slash through the web!");
        } else {
            player.sendMessage("You fail to cut through the web.");
        }
    }

    private static boolean isRs3SmithingStation(int objectId) {
        switch (objectId) {
            case 113258: // Anvil
            case 113259: // Forge
            case 113267: // Burial Forge
            case 113268: // Burial Anvil
            case 113269: // Barbarian anvil
            case 113270: // Barbarian forge
            case 113271: // Fremennik Anvil
            case 113272: // Fremennik Forge
                return true;
            default:
                return false;
        }
    }

    private static boolean isMetalBankObject(int objectId) {
        return objectId == 113258 || objectId == 113261;
    }

    public static boolean handleGate(final Player player, final WorldObject object) {
        if (World.isSpawnedObject(object)) {
            return false;
        }
        if (object.getRotation() == 0) {
            boolean south = true;
            WorldObject otherDoor = World.getObjectWithType(new WorldTile(object.getX(), object.getY() + 1, object.getPlane()), object.getType());
            if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                otherDoor = World.getObjectWithType(new WorldTile(object.getX(), object.getY() - 1, object.getPlane()), object.getType());
                if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                    return false;
                }
                south = false;
            }
            final WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY(), object.getPlane());
            final WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(), otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
            if (south) {
                openedDoor1.moveLocation(-1, 0, 0);
                openedDoor1.setRotation(3);
                openedDoor2.moveLocation(-1, 0, 0);
            } else {
                openedDoor1.moveLocation(-1, 0, 0);
                openedDoor2.moveLocation(-1, 0, 0);
                openedDoor2.setRotation(3);
            }

            if (World.removeObjectTemporary(object, 60000, true) && World.removeObjectTemporary(otherDoor, 60000, true)) {
                player.faceObject(openedDoor1);
                World.spawnObjectTemporary(openedDoor1, 60000);
                World.spawnObjectTemporary(openedDoor2, 60000);
                return true;
            }
        } else if (object.getRotation() == 2) {

            boolean south = true;
            WorldObject otherDoor = World.getObjectWithType(new WorldTile(object.getX(), object.getY() + 1, object.getPlane()), object.getType());
            if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                otherDoor = World.getObjectWithType(new WorldTile(object.getX(), object.getY() - 1, object.getPlane()), object.getType());
                if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                    return false;
                }
                south = false;
            }
            final WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY(), object.getPlane());
            final WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(), otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
            if (south) {
                openedDoor1.moveLocation(1, 0, 0);
                openedDoor2.setRotation(1);
                openedDoor2.moveLocation(1, 0, 0);
            } else {
                openedDoor1.moveLocation(1, 0, 0);
                openedDoor1.setRotation(1);
                openedDoor2.moveLocation(1, 0, 0);
            }
            if (World.removeObjectTemporary(object, 60000, true) && World.removeObjectTemporary(otherDoor, 60000, true)) {
                player.faceObject(openedDoor1);
                World.spawnObjectTemporary(openedDoor1, 60000);
                World.spawnObjectTemporary(openedDoor2, 60000);
                return true;
            }
        } else if (object.getRotation() == 3) {

            boolean right = true;
            WorldObject otherDoor = World.getObjectWithType(new WorldTile(object.getX() - 1, object.getY(), object.getPlane()), object.getType());
            if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                otherDoor = World.getObjectWithType(new WorldTile(object.getX() + 1, object.getY(), object.getPlane()), object.getType());
                if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                    return false;
                }
                right = false;
            }
            final WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY(), object.getPlane());
            final WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(), otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
            if (right) {
                openedDoor1.moveLocation(0, -1, 0);
                openedDoor2.setRotation(0);
                openedDoor1.setRotation(2);
                openedDoor2.moveLocation(0, -1, 0);
            } else {
                openedDoor1.moveLocation(0, -1, 0);
                openedDoor1.setRotation(0);
                openedDoor2.setRotation(2);
                openedDoor2.moveLocation(0, -1, 0);
            }
            if (World.removeObjectTemporary(object, 60000, true) && World.removeObjectTemporary(otherDoor, 60000, true)) {
                player.faceObject(openedDoor1);
                World.spawnObjectTemporary(openedDoor1, 60000);
                World.spawnObjectTemporary(openedDoor2, 60000);
                return true;
            }
        } else if (object.getRotation() == 1) {

            boolean right = true;
            WorldObject otherDoor = World.getObjectWithType(new WorldTile(object.getX() - 1, object.getY(), object.getPlane()), object.getType());
            if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                otherDoor = World.getObjectWithType(new WorldTile(object.getX() + 1, object.getY(), object.getPlane()), object.getType());
                if (otherDoor == null || otherDoor.getRotation() != object.getRotation() || otherDoor.getType() != object.getType() || !otherDoor.getDefinitions().name.equalsIgnoreCase(object.getDefinitions().name)) {
                    return false;
                }
                right = false;
            }
            final WorldObject openedDoor1 = new WorldObject(object.getId(), object.getType(), object.getRotation() + 1, object.getX(), object.getY(), object.getPlane());
            final WorldObject openedDoor2 = new WorldObject(otherDoor.getId(), otherDoor.getType(), otherDoor.getRotation() + 1, otherDoor.getX(), otherDoor.getY(), otherDoor.getPlane());
            if (right) {
                openedDoor1.moveLocation(0, 1, 0);
                openedDoor1.setRotation(0);
                openedDoor2.moveLocation(0, 1, 0);
            } else {
                openedDoor1.moveLocation(0, 1, 0);
                openedDoor2.setRotation(0);
                openedDoor2.moveLocation(0, 1, 0);
            }
            if (World.removeObjectTemporary(object, 60000, true) && World.removeObjectTemporary(otherDoor, 60000, true)) {
                player.faceObject(openedDoor1);
                World.spawnObjectTemporary(openedDoor1, 60000);
                World.spawnObjectTemporary(openedDoor2, 60000);
                return true;
            }
        }
        return false;
    }

    private static boolean getRepeatedTele(final Player player, final int x1, final int y1, final int p1, final int x2, final int y2, final int p2) {
        if (player.getX() == x1 && player.getY() == y1) {
            Magic.sendTeleportSpell(player, 17803, -1, 3447, -1, 1, 0.0, new WorldTile(x2, y2, p2), 2, false, Magic.OBJECT_TELEPORT);
            return true;
        } else if (player.getX() == x2 && player.getY() == y2) {
            Magic.sendTeleportSpell(player, 17803, -1, 3447, -1, 1, 0.0, new WorldTile(x1, y1, p1), 2, false, Magic.OBJECT_TELEPORT);
            return true;
        }
        return false;
    }
}
