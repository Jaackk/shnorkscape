package com.rs.network.packet.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import com.rs.utils.EconomyPrices;
import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cores.WorldThread;
import com.rs.game.*;
import com.rs.game.Hit.HitLook;
import com.rs.game.activites.CrystalChest;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dnd.eviltree.entity.EvilTreeHunterNPC;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.quest.root_of_evil.FoundRootD;
import com.rs.game.activites.quest.root_of_evil.RootOfEvil;
import com.rs.game.activites.soulwars.Barricade;
import com.rs.game.activities.dfm.DemonFlashBoss;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.activities.seasonalevents.christmas.Marionette;
import com.rs.game.activities.seasonalevents.christmas.PresentHandler;
import com.rs.game.activities.seasonalevents.christmas.ToyHorsey;
import com.rs.game.item.Item;
import com.rs.game.item.combinations.SlayerHelm;
import com.rs.game.item.floor.LootBeamManager;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Bloodnihil;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.familiar.Familiar.SpecialAttack;
import com.rs.game.npc.others.Revenant;
import com.rs.game.npc.pet.Pet;
import com.rs.game.npc.slayer.DesertLizard;
import com.rs.game.npc.slayer.Fungi;
import com.rs.game.npc.slayer.Gargoyle;
import com.rs.game.npc.slayer.HarpieBugSwarm;
import com.rs.game.npc.slayer.Lizard;
import com.rs.game.npc.slayer.Molanisk;
import com.rs.game.npc.slayer.Rockslug;
import com.rs.game.npc.spiderboss.Araxxor;
import com.rs.game.player.ActionBar.MagicAbilityShortcut;
import com.rs.game.player.Bank;
import com.rs.game.player.Equipment;
import com.rs.game.player.FarmingManager;
import com.rs.game.player.Inventory;
import com.rs.game.player.LendingManager;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.PetPerkManager;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.CMLFletching;
import com.rs.game.player.actions.RingTransformation;
import com.rs.game.player.actions.crafting.AnimaCoreCreation.AnimaCoreData;
import com.rs.game.player.actions.crafting.CraftingRs3Dialogue;
import com.rs.game.player.actions.crafting.GemCutting;
import com.rs.game.player.actions.crafting.GemCutting.Gem;
import com.rs.game.player.actions.crafting.LeatherCrafting;
import com.rs.game.player.actions.crafting.SirenicScaleCrafting;
import com.rs.game.player.actions.crafting.TectonicEnergyCrafting;
import com.rs.game.player.actions.divination.DivinationTransmute;
import com.rs.game.player.actions.divination.DivineObject;
import com.rs.game.player.actions.firemaking.Firemaking;
import com.rs.game.player.actions.fletching.Bamboo;
import com.rs.game.player.actions.fletching.BoltTipFletching;
import com.rs.game.player.actions.fletching.Fletching;
import com.rs.game.player.actions.fletching.FletchingRs3Dialogue;
import com.rs.game.player.actions.fletching.defs.BoltTips;
import com.rs.game.player.actions.fletching.defs.Fletchables;
import com.rs.game.player.actions.fletching.quickshaft.QuickShafter;
import com.rs.game.player.actions.herblore.AutoUnfPotion;
import com.rs.game.player.actions.herblore.HerbCleaning;
import com.rs.game.player.actions.herblore.Herblore;
import com.rs.game.player.actions.herblore.HerbloreRs3Dialogue;
import com.rs.game.player.actions.herblore.herbicide.Herbicide;
import com.rs.game.player.actions.hunter.FlyingEntityHunter;
import com.rs.game.player.actions.hunter.FlyingEntityHunter.FlyingEntities;
import com.rs.game.player.actions.hunter.TrapAction;
import com.rs.game.player.actions.invention.Disassemble;
import com.rs.game.player.actions.invention.JunkRefiner;
import com.rs.game.player.actions.protean.ProteanHunter;
import com.rs.game.player.actions.slayer.SlayerHelmet;
import com.rs.game.player.actions.slayer.SlayerHelmetUpgradeD;
import com.rs.game.player.actions.smithing.Smithing;
import com.rs.game.player.actions.summoning.Summoning;
import com.rs.game.player.actions.summoning.Summoning.Pouches;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.content.*;
import com.rs.game.player.content.Burying.Bone;
import com.rs.game.player.content.GoldenDying.GOLDENITEMS;
import com.rs.game.player.content.GoldenDying.NORMALITEMS;
import com.rs.game.player.content.GoldenDying.WARPRIEST;
import com.rs.game.player.content.ItemSets.Sets;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.barrows.Barrows;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.dungeoneering.journals.Chronicles;
import com.rs.game.player.content.fistofguthix.FistOfGuthix;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.items.AncientEffigy;
import com.rs.game.player.content.items.AraxCrafting;
import com.rs.game.player.content.items.AshScattering;
import com.rs.game.player.content.items.BarrowsAmulet;
import com.rs.game.player.content.items.BirdNests;
import com.rs.game.player.content.items.Bonecrusher;
import com.rs.game.player.content.items.BugLantern;
import com.rs.game.player.content.items.Defenders;
import com.rs.game.player.content.items.Ectoplasmator;
import com.rs.game.player.content.items.EliteOutfits;
import com.rs.game.player.content.items.EliteOutfits.Sentinel;
import com.rs.game.player.content.items.HerbloreBox;
import com.rs.game.player.content.items.HoodedCapes;
import com.rs.game.player.content.items.ItemRecolor;
import com.rs.game.player.content.items.MagicNotepaper;
import com.rs.game.player.content.items.MiningGeode;
import com.rs.game.player.content.items.MysteryBox;
import com.rs.game.player.content.items.OreBox;
import com.rs.game.player.content.items.PrayerBooks;
import com.rs.game.player.content.items.RewardBox;
import com.rs.game.player.content.items.RunePouch;
import com.rs.game.player.content.items.ShadeSkull;
import com.rs.game.player.content.items.ShardsBag;
import com.rs.game.player.content.packs.portable.PortableStation;
import com.rs.game.player.content.packs.protean.ProteanPack;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.content.petperks.ObtainedPet;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.player.content.ports.SuperiorExchange.PortArmor;
import com.rs.game.player.content.skillingcontracts.CoOpSkillingContractManager;
import com.rs.game.player.content.xmas.XmasRiddles;
import com.rs.game.player.content.xmas.XmasRiddles.Riddle;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.controllers.FightKiln;
import com.rs.game.player.controllers.pestcontrol.VoidPatchD;
import com.rs.game.player.controllers.pestcontrol.VoidSwitchD;
import com.rs.game.player.controllers.pestcontrol.VoidUpgradeD;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.dialogue.impl.ClueScrollDyes;
import com.rs.game.player.dialogue.impl.CombinationsD.Combinations;
import com.rs.game.player.dialogue.impl.ExchangeTrailblazerDialogue;
import com.rs.game.player.dialogue.impl.NoteBankD;
import com.rs.game.player.dialogue.impl.PortableSkillingD;
import com.rs.game.player.dialogue.impl.ProteanCraftingD;
import com.rs.game.player.dialogue.impl.RefinedAnimaCoreRepairD;
import com.rs.game.player.dialogue.impl.UziPerkShop;
import com.rs.game.player.dialogue.impl.VixWaxDialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.io.InputStream;
import com.rs.utils.Colors;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.Lend;
import com.rs.utils.Logger;
import com.rs.utils.LoggingSystem;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

import lombok.val;

public class InventoryOptionsHandler {
    private static WorldTile tile;
    private int choosenPrayer;
    private final List<NPC> applicableEntities = new ArrayList<>();

    public static boolean contains(final int id1, final int id2, final Item... items) {
        boolean containsId1 = false;
        boolean containsId2 = false;
        for (final Item item : items) {
            if (item.getId() == id1) {
                containsId1 = true;
            } else if (item.getId() == id2) {
                containsId2 = true;
            }
        }
        return containsId1 && containsId2;
    }

    /*
     * returns the other
     */
    public static Item contains(final int id1, final Item item1, final Item item2) {
        if (item1.getId() == id1) {
            return item2;
        }
        if (item2.getId() == id1) {
            return item1;
        }
        return null;
    }

    public static void dig(final Player player) {
        player.resetWalkSteps();
        player.setNextAnimation(new Animation(830));
        player.lock();
        WorldTasksManager.schedule(new WorldTask() {

            @Override
            public void run() {
                player.unlock();

                if (player.getXmas().inXmas) {
                    if (player.getXmas().riddle == null)
                        return;
                    Riddle ours = player.getXmas().riddle;
                    if (ours.getIndex() == 1 || ours.getIndex() == 2) {
                        if (player.getX() == ours.getCoords()[0] && player.getY() == ours.getCoords()[1]) {
                            XmasRiddles.finishRiddle(player);
                            player.sendMessage(Colors.GREEN + "You have managed to solve the riddle and are greatly rewarded!", false);
                            if (!player.getXmas().finishedRiddles())
                                player.sendMessage(Colors.DCYAN + "You can get another riddle from the Queen of Snow!");
                            if (ours.getIndex() == 1)
                                player.getXmas().riddle1 = true;
                            else
                                player.getXmas().riddle2 = true;
                        }
                        return;
                    }
                }

                if (player.getTreasureTrails().useDig()) {
                    return;
                }
                if (Barrows.dig(player)) {
                    return;
                }
                if (player.getX() == 3005 && player.getY() == 3376 || player.getX() == 2999 && player.getY() == 3375 || player.getX() == 2996 && player.getY() == 3377 || player.getX() == 2989 && player.getY() == 3378 || player.getX() == 2987 && player.getY() == 3387 || player.getX() == 2984 && player.getY() == 3387) {
                    // mole
                    player.setNextWorldTile(new WorldTile(1752, 5137, 0));
                    player.sendMessage("You seem to have dropped down into a network of mole tunnels.");
                    return;
                }
                player.sendMessage("You find nothing.");
            }

        });
    }

    public static void handleItemOnItem(final Player player, final InputStream stream) {
        final int fromInterfaceHash = stream.readInt();
        final int itemUsedId = stream.readUnsignedShortLE();
        final int toSlot = stream.readUnsignedShort();
        final int toInterfaceHash = stream.readIntV1();
        final int fromSlot = stream.readUnsignedShort();
        final int itemUsedWithId = stream.readUnsignedShort();

        int interfaceId = fromInterfaceHash >> 16;
        int interfaceComponent = fromInterfaceHash - (interfaceId << 16);
        int interfaceId2 = toInterfaceHash >> 16;
        @SuppressWarnings("unused")
        int interface2Component = toInterfaceHash - (interfaceId2 << 16);
        final Item fromItem = player.getInventory().getItem(fromSlot);
        final Item toItem = player.getInventory().getItem(toSlot);
        player.stopAll();
        if ((interfaceId == 1461 || interfaceId == 1460 || interfaceId == 1450 || interfaceId >= 1884 && interfaceId <= 1887) && (interfaceId2 == Inventory.INVENTORY_INTERFACE || interfaceId2 == Inventory.INVENTORY_INTERFACE_2)) {
            if (interfaceComponent == 1)
                player.getActionbar().useAbility(new MagicAbilityShortcut(fromSlot), toItem);
            return;
        }
        if ((interfaceId == 1430 && interfaceComponent >= 64 && interfaceComponent <= 233) && (interfaceId2 == Inventory.INVENTORY_INTERFACE || interfaceId2 == Inventory.INVENTORY_INTERFACE_2)) {
            Item item = player.getInventory().getItem(toSlot);
            if (item == null || item.getId() != itemUsedWithId)
                return;
            player.getActionbar().pushShortcutOnSomething((interfaceComponent - 64) / 13, item);
            return;
        }
        if ((interfaceId >= 1670 && interfaceId <= 1673) && (interfaceId2 == Inventory.INVENTORY_INTERFACE || interfaceId2 == Inventory.INVENTORY_INTERFACE_2)) {
            Item item = player.getInventory().getItem(toSlot);
            if (item == null || item.getId() != itemUsedWithId)
                return;
            int currentBar = player.getActionbar().getMultiActionBar()[interfaceId - 1670] - 1;
            if (interfaceId == 1670 && interfaceComponent >= 18 && interfaceComponent <= 187)
                player.getActionbar().pushShortcutOnSomething(currentBar, (interfaceComponent - 18) / 13, item);
            else if ((interfaceId == 1671 || interfaceId == 1672 || interfaceId == 1673) && interfaceComponent >= 13 && interfaceComponent <= 182)
                player.getActionbar().pushShortcutOnSomething(currentBar, (interfaceComponent - 13) / 13, item);
            return;
        }
        if ((interfaceId == 1506 || interfaceId == 662 || interfaceId == 1430) && (interfaceId2 == Inventory.INVENTORY_INTERFACE || interfaceId2 == Inventory.INVENTORY_INTERFACE_2)) {
            if (player.getFamiliar() != null) {
                player.getFamiliar().setSpecial(true, false);
                if (player.getFamiliar().getSpecialAttack() == SpecialAttack.ITEM) {
                    if (player.getFamiliar().hasSpecialOn()) {
                        player.getFamiliar().submitSpecial(toSlot);
                    }
                }
            }
            return;
        }
        if (interfaceId == Inventory.INVENTORY_INTERFACE && interfaceId == interfaceId2 && !player.getInterfaceManager().containsInventoryInter()) {
            if (toSlot >= 28 || fromSlot >= 28) {
                return;
            }
            final Item usedWith = player.getInventory().getItem(toSlot);
            final Item itemUsed = player.getInventory().getItem(fromSlot);
            if (itemUsed == null || usedWith == null || itemUsed.getId() != itemUsedId || usedWith.getId() != itemUsedWithId) {
                return;
            }
            if (itemUsed == usedWith || usedWith == itemUsed) {
                return;
            }
            if (itemUsed.getId() == 1917 && usedWith.getId() == 6952 || usedWith.getId() == 1917 && itemUsed.getId() == 6952) {
                player.getInventory().deleteItem(1917, 1);
                player.getInventory().deleteItem(6952, 1);
                player.getInventory().addItem(5747, 1);
                player.sendMessage("You pour the truth serum into the beer.");
                return;
            }
            if (player.getInventionManager().augmentItem(itemUsed, fromSlot, usedWith, toSlot) || player.getInventionManager().disolveItem(itemUsed, usedWith) || player.getInventionManager().installGizmo(itemUsed, usedWith) || player.getInventionManager().disolveGizmo(itemUsed, usedWith) || player.getInventionManager().dissolveEquipment(itemUsed, usedWith) || player.getInventionManager().siphonEquipment(itemUsed, usedWith) || player.getInventionManager().seperateEquipment(itemUsed, usedWith))
                return;
            if (ClueScrollDyes.dyeItem(player, itemUsed, usedWith) || ClueScrollDyes.undyeItem(player, itemUsed, usedWith))
                return;
            if (itemUsed.getId() == 41073 && usedWith.getId() == 41083 || itemUsed.getId() == 41083 && usedWith.getId() == 41073) {
                player.sendInputInteger("How many empty divine charge containers would you like to store?", new InputIntegerEvent() {

                    @Override
                    public void run(Player player) {
                        int value = getInteger();
                        if (value <= 0)
                            return;
                        player.getInventionManager().addEmptyDivineCharges(value);
                    }

                });
                return;
            }
            if (Ectoplasmator.useEssence(player, itemUsedWithId, itemUsed) || Ectoplasmator.useEssence(player, itemUsedId, usedWith)) {
                return;
            }
//            if (VialToFlask.oneVialToFlask(player, itemUsed, usedWith) || VialToFlask.oneVialToFlask(player, usedWith, itemUsed)) {
//                return;
//            }
//            if (VialToFlask.oneFlaskToVial(player, itemUsed, usedWith) || VialToFlask.oneFlaskToVial(player, usedWith, itemUsed)) {
//                return;
//            }
            if (!player.getControlerManager().canUseItemOnItem(itemUsed, usedWith)) {
                return;
            }
            if (player.getChargesManagerNew().rechargeItem(itemUsed, usedWith))
                return;
            if (VoidPatchD.patch(player, itemUsed, usedWith)) {
                return;
            }
            final Combinations combination = Combinations.isCombining(itemUsedId, itemUsedWithId);
            if (combination != null) {
                player.getDialogueManager().startDialogue("CombinationsD", combination);
                return;

            }
            if (Pots.mixPot(player, itemUsed, usedWith, fromSlot, toSlot))
                return;
            SlayerHelm.handleItemOnItem(player, itemUsed, usedWith);
            if (CosmeticsHandler.keepSakeItem(player, itemUsed, usedWith)) {
                return;
            }
            if ((itemUsed.getId() == 37822 && usedWith.getId() == 37823) || (usedWith.getId() == 37822 && itemUsed.getId() == 37823)) {
                player.getInventory().deleteItem(37822, 1);
                player.getInventory().deleteItem(37823, 1);
                player.getInventory().addItem(37824, 1);
                player.getDialogueManager().startDialogue("SimpleItemMessage", 37824, 1, "You combine Seiryu's claw with a Seiryu's fang to create a Mizuyari.");
                return;
            }
            if (itemUsed.getId() == 946 && usedWith.getId() == 34528 || usedWith.getId() == 946 && itemUsed.getId() == 34528) {
                player.getDialogueManager().startDialogue("ProteanFletchingD", player.getInventory().getAmountOf(34528) > 60 ? 60 : player.getInventory().getAmountOf(34528), false);
                return;
            }
            if (itemUsed.getId() == 946 && usedWith.getId() == CMLFletching.LOG_ID || usedWith.getId() == 946 && itemUsed.getId() == CMLFletching.LOG_ID) {
                player.getDialogueManager().startDialogue("CMLFletchingD", false);
                return;
            }
            if ((itemUsed.getId() == 31851 || itemUsed.getId() == 31852 || itemUsed.getId() == 6571 || itemUsed.getId() == 6572 || itemUsed.getId() == 1631 || itemUsed.getId() == 1632) && (usedWith.getId() == 39893 || usedWith.getId() == 39895 || usedWith.getId() == 39897 || usedWith.getId() == 39899 || usedWith.getId() == 39901) || (usedWith.getId() == 31851 || usedWith.getId() == 31852 || usedWith.getId() == 6571 || usedWith.getId() == 6572 || usedWith.getId() == 1631 || usedWith.getId() == 1632) && (itemUsed.getId() == 39893 || itemUsed.getId() == 39895 || itemUsed.getId() == 39897 || itemUsed.getId() == 39899 || itemUsed.getId() == 39901)) {
                player.getDialogueManager().startDialogue("GemstoneChargingD", usedWith.getId() > 39000 ? itemUsed : usedWith);
                return;
            }
            final Fletchables fletchables = Fletching.isFletching(usedWith, itemUsed);
            if (fletchables != null) {
                FletchingRs3Dialogue.sendFletchingInterface(player, fletchables, false);
                return;
            }

            // Rune pouches
            if (itemUsed.getId() == 38451 || itemUsed.getId() == 38453 || usedWith.getId() == 38451 || usedWith.getId() == 38453) {
                if (itemUsed.getId() == 38451 && usedWith.getId() == 38453 || itemUsed.getId() == 38453 && usedWith.getId() == 38451) {
                    player.sendMessage("Nothing interesting happens.");
                    return;
                }

                final boolean small = itemUsed.getId() == 38451 || usedWith.getId() == 38451;
                RunePouch.fill(player, (itemUsed.getId() != 38451 && itemUsed.getId() != 38453) ? itemUsed : usedWith, small);
                return;
            }

            if (itemUsed.getId() == 19893 || usedWith.getId() == 19893) {
                if ((usedWith.getId() == 20769 || usedWith.getId() == 20771 || usedWith.getId() == 32152 || usedWith.getId() == 32153) || (itemUsed.getId() == 20769 || itemUsed.getId() == 20771 || itemUsed.getId() == 32152 || itemUsed.getId() == 32153)) {
                    if (player.infusedSpiritCapeEffect) {
                        player.sendMessage("You've already infused the essence of the spirit cape into your completionist's cape.");
                        return;
                    }
                    player.getDialogueManager().startDialogue("InfuseSpiritCapeD");
                    return;
                }
            }

            if (itemUsed.getId() == 27068 && (usedWith.getId() == 11716 || usedWith.getId() == 14484 || usedWith.getId() == 25555) || usedWith.getId() == 27068 && (itemUsed.getId() == 11716 || itemUsed.getId() == 14484 || itemUsed.getId() == 25555)) {
                player.getDialogueManager().startDialogue("ChaoticItemsCreationD", itemUsed.getId() == 27068 ? usedWith.getId() : itemUsed.getId());
                return;
            }

            if (itemUsed.getId() == 31449 && (usedWith.getId() == 25028 || usedWith.getId() == 25031 || usedWith.getId() == 25034) || usedWith.getId() == 31449 && (itemUsed.getId() == 25028 || itemUsed.getId() == 25031 || itemUsed.getId() == 25034)) {
                player.getDialogueManager().startDialogue("ChaoticRemnantUsageD", itemUsed.getId() == 31449 ? usedWith.getId() : itemUsed.getId());
                return;
            }

            if (itemUsed.getId() == 18330 || usedWith.getId() == 18330) {
                if (itemUsed.getId() == 851 || usedWith.getId() == 851) {
                    player.getInventory().deleteItem(18330, 1);
                    player.getInventory().deleteItem(851, 1);
                    player.getInventory().addItem(18331, 1);
                    player.sendMessage("You attach the shieldbow sight to your maple shieldbow.");
                } else if (itemUsed.getId() == 859 || usedWith.getId() == 859) {
                    player.getInventory().deleteItem(18330, 1);
                    player.getInventory().deleteItem(859, 1);
                    player.getInventory().addItem(18332, 1);
                    player.sendMessage("You attach the shieldbow sight to your magic shieldbow.");
                } else if (itemUsed.getId() == 29611 || usedWith.getId() == 29611) {
                    player.getInventory().deleteItem(18330, 1);
                    player.getInventory().deleteItem(29611, 1);
                    player.getInventory().addItem(29634, 1);
                    player.sendMessage("You attach the shieldbow sight to your elder shieldbow.");
                }
                return;
            }

            if (Fungi.refillFungicide(player, itemUsed, usedWith)) {
                return;
            }

            if (itemUsed.getId() == 26517 && usedWith.getId() == 1050 || itemUsed.getId() == 1050 && usedWith.getId() == 26517) {
                player.getInventory().deleteItem(itemUsed.getId(), 1);
                player.getInventory().deleteItem(usedWith.getId(), 1);
                player.getInventory().addItem(36080, 1);
                return;
            }

            if (itemUsed.getId() == 26517 && usedWith.getId() == 30412 || itemUsed.getId() == 30412 && usedWith.getId() == 26517) {
                player.getInventory().deleteItem(itemUsed.getId(), 1);
                player.getInventory().deleteItem(usedWith.getId(), 1);
                player.getInventory().addItem(36079, 1);
                return;
            }
            if (RefinedAnimaCoreRepairD.ConsumableEssenceData.getEssenceItemIds().contains(itemUsedId) && RefinedAnimaCoreRepairD.RefinedArmourData.getRefinedItemIds().contains(itemUsedWithId)) {
                player.getDialogueManager().startDialogue("RefinedAnimaCoreRepairD", usedWith);
                return;
            }
            if (itemUsed.getId() == 18342 && usedWith.getId() == 563 || usedWith.getId() == 18342 && itemUsed.getId() == 563) {
                if (player.getLawRunes() == 1000) {
                    player.sendMessage("Your law staff is already fully charged.");
                    return;
                }
                int chargesToAdd = player.getInventory().getAmountOf(563);
                if (chargesToAdd + player.getLawRunes() > 1000) {
                    chargesToAdd = 1000 - player.getLawRunes();
                }
                player.getInventory().deleteItem(563, chargesToAdd);
                player.setLawRunes(chargesToAdd + player.getLawRunes());
                player.sendMessage("You add " + chargesToAdd + " charges to your law staff.");
                return;
            }

            if (itemUsed.getId() == 18341 && usedWith.getId() == 561 || usedWith.getId() == 18341 && itemUsed.getId() == 561) {
                if (player.getNatureRunes() == 1000) {
                    player.sendMessage("Your nature staff is already fully charged.");
                    return;
                }
                int chargesToAdd = player.getInventory().getAmountOf(561);
                if (chargesToAdd + player.getNatureRunes() > 1000) {
                    chargesToAdd = 1000 - player.getNatureRunes();
                }
                player.getInventory().deleteItem(561, chargesToAdd);
                player.setNatureRunes(chargesToAdd + player.getNatureRunes());
                player.sendMessage("You add " + chargesToAdd + " charges to your nature staff.");
                return;
            }

            if ((itemUsed.getId() == 20680 && usedWith.getId() == 20685) || itemUsed.getId() == 20685 && usedWith.getId() == 20680) {
                if (player.getEquipment().getHatId() == 1506) {
                    player.getInventory().deleteItem(20680, 1);
                    player.getInventory().deleteItem(20685, 1);
                    player.getInventory().addItem(20692, 1);
                    player.sendMessage(Colors.GREEN + "You successfully made a barrel of gunpowder.");
                } else {
                    player.sendMessage(Colors.RED + "You must be wearing a gas mask to make this!");
                }
            }
            if (usedWith.getId() == 33740 && itemUsed.getId() == 1733 || usedWith.getId() == 1733 && itemUsed.getId() == 33740) {
                player.getDialogueManager().startDialogue(ProteanCraftingD.class.getSimpleName(), 33740, player.getInventory().getAmountOf(33740) > 60 ? 60 : player.getInventory().getAmountOf(33740), false);
                return;
            }
//            if (itemUsedId == ImbueItem.IMBUED_GEAR_ID && ImbueItem.itemIsCompatible(itemUsedWithId)) {
//                player.getDialogueManager().startDialogue(ImbueItem.class.getSimpleName(), usedWith, itemUsed);
//                return;
//            }
            if (itemUsed.getId() == 9415) {
                int i = 0;
                final int k = 0;
                String storedConvertString = "";
                int storedIndexPosition = 0;
                int storedDeleteItem;

                if (!usedWith.getName().contains("Warpriest")) {
                    for (final NORMALITEMS normalItems : NORMALITEMS.values()) {
                        for (i = 0; i < 4; i++) {
                            if (normalItems.getId()[i] == usedWith.getId()) {
                                storedConvertString = normalItems.getConvertString();
                                storedIndexPosition = i;
                                storedDeleteItem = normalItems.getId()[i];

                                for (final GOLDENITEMS goldItems : GOLDENITEMS.values()) {
                                    if (goldItems.getConvertString() == storedConvertString) {
                                        player.getInventory().addItem(new Item(goldItems.getId()[storedIndexPosition], 1));
                                        player.getInventory().deleteItem(storedDeleteItem, 1);
                                        player.getInventory().deleteItem(itemUsed.getId(), 1);
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                } else {
                    for (final WARPRIEST warpriest : WARPRIEST.values()) {
                        for (i = 0; i < 6; i++) {
                            if (warpriest.getId()[i] == usedWith.getId()) {
                                storedConvertString = warpriest.getConvertString();
                                storedIndexPosition = i;
                                storedDeleteItem = warpriest.getId()[i];

                                for (final GOLDENITEMS goldItems : GOLDENITEMS.values()) {
                                    if (goldItems.getConvertString() == storedConvertString) {
                                        player.getInventory().addItem(new Item(goldItems.getId()[storedIndexPosition], 1));
                                        player.getInventory().deleteItem(storedDeleteItem, 1);
                                        player.getInventory().deleteItem(itemUsed.getId(), 1);
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }

            if (usedWith.getId() == 9415) {
                int i = 0;
                final int k = 0;
                String storedConvertString = "";
                int storedIndexPosition = 0;
                int storedDeleteItem;

                if (!itemUsed.getName().contains("Warpriest")) {
                    for (final NORMALITEMS normalItems : NORMALITEMS.values()) {
                        for (i = 0; i < 4; i++) {
                            if (normalItems.getId()[i] == itemUsed.getId()) {
                                storedConvertString = normalItems.getConvertString();
                                storedIndexPosition = i;
                                storedDeleteItem = normalItems.getId()[i];

                                for (final GOLDENITEMS goldItems : GOLDENITEMS.values()) {
                                    if (goldItems.getConvertString() == storedConvertString) {
                                        player.getInventory().addItem(new Item(goldItems.getId()[storedIndexPosition], 1));
                                        player.getInventory().deleteItem(storedDeleteItem, 1);
                                        player.getInventory().deleteItem(usedWith.getId(), 1);
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                } else {
                    for (final WARPRIEST warpriest : WARPRIEST.values()) {
                        for (i = 0; i < 6; i++) {
                            if (warpriest.getId()[i] == itemUsed.getId()) {
                                storedConvertString = warpriest.getConvertString();
                                storedIndexPosition = i;
                                storedDeleteItem = warpriest.getId()[i];

                                for (final GOLDENITEMS goldItems : GOLDENITEMS.values()) {
                                    if (goldItems.getConvertString() == storedConvertString) {
                                        player.getInventory().addItem(new Item(goldItems.getId()[storedIndexPosition], 1));
                                        player.getInventory().deleteItem(storedDeleteItem, 1);
                                        player.getInventory().deleteItem(usedWith.getId(), 1);
                                        break;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }

            if (itemUsed.getId() == 34151 || usedWith.getId() == 34151) { // Lava
                // whips
                Item wyrmSpike = itemUsed.getId() == 34151 ? itemUsed : usedWith;
                Item ingriedient = wyrmSpike.getId() == itemUsed.getId() ? usedWith : itemUsed;
                String name = ingriedient.getName().toLowerCase();
                if (!name.equalsIgnoreCase("whip vine") && !name.equalsIgnoreCase("abyssal whip") && !name.equalsIgnoreCase("abyssal vine whip")) {
                    player.getPackets().sendGameMessage("Nothing interesting happens.");
                    return;
                }
                player.getDialogueManager().startDialogue("ItemCreationD", itemUsed.getId(), usedWith.getId(), 34150, "Lava whip");
                return;
            }
            if (itemUsed.getId() == 34153 || usedWith.getId() == 34153) { // Staff of darkness
                if (itemUsed.getId() != 15486 && usedWith.getId() != 15486) {
                    player.getPackets().sendGameMessage("Nothing interesting happens.");
                    return;
                }
                player.getDialogueManager().startDialogue("ItemCreationD", 15486, 34153, 34155, "Staff of Darkness");
                return;
            }
            if (itemUsed.getId() == 34156 || usedWith.getId() == 34156) { // Strykebow
                if (itemUsed.getId() != 11235 && usedWith.getId() != 11235) {
                    player.getPackets().sendGameMessage("Nothing interesting happens.");
                    return;
                }
                player.getDialogueManager().startDialogue("ItemCreationD", 11235, 34156, 34158, "Strykebow");
                return;
            }

            if (itemUsed.getId() == 13887 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13887) {
                player.sendMessage("You discover a Superior piece of Ancient Vesta");
                player.getInventory().deleteItem(13887, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39103, 1);
                return;
            }
            if (itemUsed.getId() == 13893 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13893) {
                player.sendMessage("You discover a Superior piece of Ancient Vesta");
                player.getInventory().deleteItem(13893, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39111, 1);
                return;
            }
            if (itemUsed.getId() == 13899 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13899) {
                player.sendMessage("You discover a Superior piece of Ancient Vesta");
                player.getInventory().deleteItem(13899, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39121, 1);
                return;
            }
            if (itemUsed.getId() == 13905 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13905) {
                player.sendMessage("You discover a Superior piece of Ancient Vesta");
                player.getInventory().deleteItem(13905, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39125, 1);
                return;
            }
            if (itemUsed.getId() == 13884 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13884) {
                player.sendMessage("You discover a Superior piece of Ancient Statius");
                player.getInventory().deleteItem(13884, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39099, 1);
                return;
            }
            if (itemUsed.getId() == 13890 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13890) {
                player.sendMessage("You discover a Superior piece of Ancient Statius");
                player.getInventory().deleteItem(13890, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39107, 1);
                return;
            }
            if (itemUsed.getId() == 13896 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13896) {
                player.sendMessage("You discover a Superior piece of Ancient Statius");
                player.getInventory().deleteItem(13896, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39115, 1);
                return;
            }
            if (itemUsed.getId() == 13902 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13902) {
                player.sendMessage("You discover a Superior piece of Ancient Statius");
                player.getInventory().deleteItem(13902, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39117, 1);
                return;
            }
            if (itemUsed.getId() == 13870 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13870) {
                player.sendMessage("You discover a Superior piece of Ancient Morrigan");
                player.getInventory().deleteItem(13870, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39129, 1);
                return;
            }
            if (itemUsed.getId() == 13873 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13873) {
                player.sendMessage("You discover a Superior piece of Ancient Morrigan");
                player.getInventory().deleteItem(13873, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39133, 1);
                return;
            }
            if (itemUsed.getId() == 13876 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13876) {
                player.sendMessage("You discover a Superior piece of Ancient Morrigan");
                player.getInventory().deleteItem(13876, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39137, 1);
                return;
            }
            if (itemUsed.getId() == 13879 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13879) {
                if (player.getInventory().containsItem(13879, 50) && player.getInventory().containsItem(39047, 1)) {
                    player.getInventory().deleteItem(13879, 50);
                    player.getInventory().deleteItem(39047, 1);
                    player.getInventory().addItem(39139, 1);
                    player.sendMessage("You discover a Superior piece of Ancient Morrigan");
                    return;
                }
            }
            if (itemUsed.getId() == 13883 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13883) {
                if (player.getInventory().containsItem(13883, 50) && player.getInventory().containsItem(39047, 1)) {
                    player.getInventory().deleteItem(13883, 50);
                    player.getInventory().deleteItem(39047, 1);
                    player.getInventory().addItem(39143, 1);
                    player.sendMessage("You discover a Superior piece of Ancient Morrigan");
                    return;
                }
            }
            if (itemUsed.getId() == 13858 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13858) {
                player.sendMessage("You discover a Superior piece of Ancient Zuriel");
                player.getInventory().deleteItem(13858, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39085, 1);
                return;
            }
            if (itemUsed.getId() == 13861 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13861) {
                player.sendMessage("You discover a Superior piece of Ancient Zuriel");
                player.getInventory().deleteItem(13861, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39089, 1);
                return;
            }
            if (itemUsed.getId() == 13864 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13864) {
                player.sendMessage("You discover a Superior piece of Ancient Zuriel");
                player.getInventory().deleteItem(13864, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39093, 1);
                return;
            }
            if (itemUsed.getId() == 13867 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13867) {
                player.sendMessage("You discover a Superior piece of Ancient Zuriel");
                player.getInventory().deleteItem(13867, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39095, 1);
                return;
            }
            if (itemUsed.getId() == 13876 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13876) {
                player.sendMessage("You discover a Superior piece of Ancient Morrigan");
                player.getInventory().deleteItem(13876, 1);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39137, 1);
                return;
            }
            if (itemUsed.getId() == 13879 && usedWith.getId() == 39047 || itemUsed.getId() == 39047 && usedWith.getId() == 13879) {
                int itemCount = player.getInventory().getAmountOf(13879);
                player.sendMessage("You discover a Superior piece of Ancient Morrigan");
                player.getInventory().deleteItem(13879, itemCount);
                player.getInventory().deleteItem(39047, 1);
                player.getInventory().addItem(39139, itemCount);
                return;
            }
            if (itemUsed.getId() == 11848 && usedWith.getId() == 30027 || itemUsed.getId() == 30027 && usedWith.getId() == 11848) {
                if (player.getInventory().containsItem(11848, 1) && (player.getInventory().containsItem(30027, 250))) {
                    World.sendWorldMessage(Colors.LPURPLE + "<shad=000000><img=6>News: " + player.getDisplayName() + " has created a Dharok's bobblehead!", false);
                    player.setNextGraphics(new Graphics(4834));
                    player.getInventory().deleteItem(11848, 1);
                    player.getInventory().deleteItem(30027, 250);
                    player.getInventory().addItem(30032, 1);
                    return;
                }
            }
            if (itemUsed.getId() == 11850 && usedWith.getId() == 30027 || itemUsed.getId() == 30027 && usedWith.getId() == 11850) {
                if (player.getInventory().containsItem(11850, 1) && (player.getInventory().containsItem(30027, 250))) {
                    World.sendWorldMessage(Colors.GREEN + "<shad=000000><img=6>News: " + player.getDisplayName() + " has created a Guthan's bobblehead!", false);
                    player.setNextGraphics(new Graphics(4833));
                    player.getInventory().deleteItem(11850, 1);
                    player.getInventory().deleteItem(30027, 250);
                    player.getInventory().addItem(30033, 1);
                    return;
                }
            }
            if (itemUsed.getId() == 11846 && usedWith.getId() == 30027 || itemUsed.getId() == 30027 && usedWith.getId() == 11846) {
                if (player.getInventory().containsItem(11846, 1) && (player.getInventory().containsItem(30027, 250))) {
                    World.sendWorldMessage(Colors.RED + "<shad=000000><img=6>News: " + player.getDisplayName() + " has created an Ahrim's bobblehead!", false);
                    player.setNextGraphics(new Graphics(4835));
                    player.getInventory().deleteItem(11846, 1);
                    player.getInventory().deleteItem(30027, 250);
                    player.getInventory().addItem(30031, 1);
                    return;
                }
            }
            if (itemUsed.getId() == 11856 && usedWith.getId() == 30027 || itemUsed.getId() == 30027 && usedWith.getId() == 11856) {
                if (player.getInventory().containsItem(11856, 1) && (player.getInventory().containsItem(30027, 250))) {
                    World.sendWorldMessage(Colors.CYAN + "<shad=000000><img=6>News: " + player.getDisplayName() + " has created a Verac's bobblehead!", false);
                    player.setNextGraphics(new Graphics(4836));
                    player.getInventory().deleteItem(11856, 1);
                    player.getInventory().deleteItem(30027, 250);
                    player.getInventory().addItem(30036, 1);
                    return;
                }
            }
            if (itemUsed.getId() == 11852 && usedWith.getId() == 30027 || itemUsed.getId() == 30027 && usedWith.getId() == 11852) {
                if (player.getInventory().containsItem(11852, 1) && (player.getInventory().containsItem(30027, 250))) {
                    World.sendWorldMessage(Colors.LIME + "<shad=000000><img=6>News: " + player.getDisplayName() + " has created a Karil's bobblehead!", false);
                    player.setNextGraphics(new Graphics(4834));
                    player.getInventory().deleteItem(11852, 1);
                    player.getInventory().deleteItem(30027, 250);
                    player.getInventory().addItem(30034, 1);
                    return;
                }
            }
            if (itemUsed.getId() == 11854 && usedWith.getId() == 30027 || itemUsed.getId() == 30027 && usedWith.getId() == 11854) {
                if (player.getInventory().containsItem(11854, 1) && (player.getInventory().containsItem(30027, 250))) {
                    World.sendWorldMessage(Colors.YELLOW + "<shad=000000><img=6>News: " + player.getDisplayName() + " has created a Torag's bobblehead!", false);
                    player.setNextGraphics(new Graphics(4834));
                    player.getInventory().deleteItem(11854, 1);
                    player.getInventory().deleteItem(30027, 250);
                    player.getInventory().addItem(30035, 1);
                    return;
                }
            }
            // Ring of Death & Perfect Ring
            if (itemUsed.getId() == 773 && usedWith.getId() == 31869 || itemUsed.getId() == 31869 && usedWith.getId() == 773) {
                if (player.getSkills().getLevel(Skills.CRAFTING) >= 99) {
                    player.sendMessage("You use your Crafting knowledge to combine the powerful rings");
                    player.getInventory().deleteItem(773, 1);
                    player.getInventory().deleteItem(31869, 1);
                    player.getInventory().addItem(41069, 1);
                    player.getSkills().addXp(Skills.CRAFTING, 500);
                } else {
                    player.sendMessage("You need a Crafting level of 99 to do this!");
                }
                return;
            }
            // Hydrix Jewellery
            if (itemUsed.getId() == 31855 && usedWith.getId() == 6575 || itemUsed.getId() == 6575 && usedWith.getId() == 31855) {
                if (player.getSkills().getLevel(Skills.CRAFTING) >= 79) {
                    player.sendMessage("You combine the Onyx with the Hydrix creating a Hydrix Ring!");
                    player.getInventory().deleteItem(6575, 1);
                    player.getInventory().deleteItem(31855, 1);
                    player.getInventory().addItem(31857, 1);
                    player.getSkills().addXp(Skills.CRAFTING, 280);
                } else {
                    player.sendMessage("You need a Crafting level of 79 to do this!");
                }
                return;
            }
            if (itemUsed.getId() == 31855 && usedWith.getId() == 6577 || itemUsed.getId() == 6577 && usedWith.getId() == 31855) {
                if (player.getSkills().getLevel(Skills.CRAFTING) >= 90) {
                    player.sendMessage("You combine the Onyx with the Hydrix creating a Hydrix Necklace!");
                    player.getInventory().deleteItem(6577, 1);
                    player.getInventory().deleteItem(31855, 1);
                    player.getInventory().addItem(31859, 1);
                    player.getSkills().addXp(Skills.CRAFTING, 290);
                } else {
                    player.sendMessage("You need a Crafting level of 90 to do this!");
                }
                return;
            }
            if (itemUsed.getId() == 31855 && usedWith.getId() == 11130 || itemUsed.getId() == 11130 && usedWith.getId() == 31855) {
                if (player.getSkills().getLevel(Skills.CRAFTING) >= 95) {
                    player.sendMessage("You combine the Onyx with the Hydrix creating a Hydrix Bracelet!");
                    player.getInventory().deleteItem(11130, 1);
                    player.getInventory().deleteItem(31855, 1);
                    player.getInventory().addItem(31865, 1);
                    player.getSkills().addXp(Skills.CRAFTING, 300);
                } else {
                    player.sendMessage("You need a Crafting level of 95 to do this!");
                }
                return;
            }
            if (itemUsed.getId() == 31855 && usedWith.getId() == 6581 || itemUsed.getId() == 6581 && usedWith.getId() == 31855) {
                if (player.getSkills().getLevel(Skills.CRAFTING) >= 99) {
                    player.sendMessage("You combine the Onyx with the Hydrix creating a Hydrix Amulet!");
                    player.getInventory().deleteItem(6581, 1);
                    player.getInventory().deleteItem(31855, 1);
                    player.getInventory().addItem(31863, 1);
                    player.getSkills().addXp(Skills.CRAFTING, 310);
                } else {
                    player.sendMessage("You need a Crafting level of 99 to do this!");
                }
                return;
            }
            if (itemUsed.getId() == 34158 || usedWith.getId() == 34158) { // strykebow
//                // refill
//                if (player.getCharges().getCharges(34158) == 100000 || player.getCharges().getCharges(34158) + 20000 > 100000) {
//                    player.sendMessage(Colors.SALMON + Colors.SHAD + "Your strykebow is already full charged!");
//                    return;
//                }
//                if (itemUsed.getId() != 11235 && usedWith.getId() != 11235) {
//                    return;
//                } else {
//                    player.getInventory().deleteItem(11235, 1);
//                    player.getCharges().setCharges(34158, player.getCharges().getCharges(34158) + 20000);
//                    player.sendMessage(Colors.GREEN + Colors.SHAD + "You have added 20,000 charges to your strykebow!", true);
//                    return;
//                }
            }

            if (itemUsed.getId() == 34155 || usedWith.getId() == 34155) { // staff
//                // of
//                // darkness
//                if (player.getCharges().getCharges(34155) == 100000 || player.getCharges().getCharges(34155) + 20000 > 100000) {
//                    player.sendMessage(Colors.SALMON + Colors.SHAD + "Your staff of darkness is already full charged!");
//                    return;
//                }
//                if (itemUsed.getId() != 15486 && usedWith.getId() != 15486) {
//                    return;
//                } else {
//                    player.getInventory().deleteItem(15486, 1);
//                    player.getCharges().setCharges(34155, player.getCharges().getCharges(34155) + 20000);
//                    player.sendMessage(Colors.GREEN + Colors.SHAD + "You have added 20,000 charges to your Staff of darkness!", true);
//                    return;
//                }
            }
            /** End of lava combo items */

            if (itemUsed.getId() == 34838 || usedWith.getId() == 34838 && (itemUsed.getAttributes() == null && usedWith.getAttributes() == null)) {
                if (itemUsed.getId() != 31725 && usedWith.getId() != 31725) {
                    return;
                } else {
                    player.getDialogueManager().startDialogue(new Dialogue() {

                        @Override
                        public void start() {
                            sendItemDialogue(33625, 1, "Are you sure you want to dye your noxious scythe " + Colors.SHAD + Colors.DCYAN + "Christmas color</col></shad>?");
                            stage = 0;
                        }

                        @Override
                        public void run(final int interfaceId, final int componentId) {
                            switch (stage) {

                            case 0:
                                sendOptionsDialogue("Do you want to do this?", "Yes", "No");
                                stage = 1;
                                break;
                            case 1:
                                switch (componentId) {
                                case OPTION_1:
                                    finish();
                                    player.getInventory().deleteItem(34838, 1);
                                    player.getInventory().deleteItem(31725, 1);
                                    player.getInventory().addItem(33625, 1);
                                    player.getXmas().announceDrop(" has crafted a Christmas-dyed noxious scythe!");
                                    break;
                                case OPTION_2:
                                    finish();
                                    break;
                                }
                                break;
                            }
                        }

                        @Override
                        public void finish() {
                            player.getInterfaceManager().closeChatBoxInterface();
                        }

                    });
                }
            }

            if (SlayerHelmet.uncolour(player, itemUsed, usedWith)) {
                return;
            }
            if (itemUsed.getId() == 27587 && usedWith.getId() == 34921 || usedWith.getId() == 27587 && itemUsed.getId() == 34921) {
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        final int reward = 34925;
                        player.getInventory().deleteItem(itemUsed.getId(), 1);
                        player.getInventory().deleteItem(usedWith.getId(), 1);
                        player.getInventory().addItem(reward, 1);
                        sendItemDialogue(reward, 1, "You have created a modified first age tiara!");
                    }

                    @Override
                    public void run(final int interfaceId, final int componentId) {
                        finish();
                    }

                    @Override
                    public void finish() {
                        player.getInterfaceManager().closeChatBoxInterface();
                    }

                });
                return;
            }
            if (itemUsed.getId() == 25185 && usedWith.getId() == 32277 || usedWith.getId() == 25185 && itemUsed.getId() == 32277) {
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        final int reward = 32281;
                        player.getInventory().deleteItem(itemUsed.getId(), 1);
                        player.getInventory().deleteItem(usedWith.getId(), 1);
                        player.getInventory().addItem(reward, 1);
                        sendItemDialogue(reward, 1, "You have created a modified artisan's bandana!");
                    }

                    @Override
                    public void run(final int interfaceId, final int componentId) {
                        finish();
                    }

                    @Override
                    public void finish() {
                        player.getInterfaceManager().closeChatBoxInterface();
                    }

                });
                return;
            }

            if (itemUsed.getId() == 25190 && usedWith.getId() == 34919 || usedWith.getId() == 25190 && itemUsed.getId() == 34919) {
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        final int reward = 34923;
                        player.getInventory().deleteItem(itemUsed.getId(), 1);
                        player.getInventory().deleteItem(usedWith.getId(), 1);
                        player.getInventory().addItem(reward, 1);
                        sendItemDialogue(reward, 1, "You have created a modified botanist's mask!");
                    }

                    @Override
                    public void run(final int interfaceId, final int componentId) {
                        finish();
                    }

                    @Override
                    public void finish() {
                        player.getInterfaceManager().closeChatBoxInterface();
                    }
                });
                return;
            }

            if (itemUsed.getId() == 29865 && usedWith.getId() == 32275 || usedWith.getId() == 29865 && itemUsed.getId() == 32275) {
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        final int reward = 32279;
                        player.getInventory().deleteItem(itemUsed.getId(), 1);
                        player.getInventory().deleteItem(usedWith.getId(), 1);
                        player.getInventory().addItem(reward, 1);
                        sendItemDialogue(reward, 1, "You have created a modified diviner's headwear!");
                    }

                    @Override
                    public void run(final int interfaceId, final int componentId) {
                        finish();
                    }

                    @Override
                    public void finish() {
                        player.getInterfaceManager().closeChatBoxInterface();
                    }

                });
                return;
            }
            if (itemUsed.getId() == 28995 && usedWith.getId() == 32274 || usedWith.getId() == 28995 && itemUsed.getId() == 32274) {
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        final int reward = 32278;
                        player.getInventory().deleteItem(itemUsed.getId(), 1);
                        player.getInventory().deleteItem(usedWith.getId(), 1);
                        player.getInventory().addItem(reward, 1);
                        sendItemDialogue(reward, 1, "You have created a modified shaman's headdress!");
                    }

                    @Override
                    public void run(final int interfaceId, final int componentId) {
                        finish();
                    }

                    @Override
                    public void finish() {
                        player.getInterfaceManager().closeChatBoxInterface();
                    }

                });
                return;
            }
            if (itemUsed.getId() == 25180 && usedWith.getId() == 34920 || usedWith.getId() == 25180 && itemUsed.getId() == 34920) {
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        final int reward = 34924;
                        player.getInventory().deleteItem(itemUsed.getId(), 1);
                        player.getInventory().deleteItem(usedWith.getId(), 1);
                        player.getInventory().addItem(reward, 1);
                        sendItemDialogue(reward, 1, "You have created a modified sous chef's toque!");
                    }

                    @Override
                    public void run(final int interfaceId, final int componentId) {
                        finish();
                    }

                    @Override
                    public void finish() {
                        player.getInterfaceManager().closeChatBoxInterface();
                    }

                });
                return;
            }
            if (itemUsed.getId() == 25195 && usedWith.getId() == 32276 || usedWith.getId() == 25195 && itemUsed.getId() == 32276) {
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        final int reward = 32280;
                        player.getInventory().deleteItem(itemUsed.getId(), 1);
                        player.getInventory().deleteItem(usedWith.getId(), 1);
                        player.getInventory().addItem(reward, 1);
                        sendItemDialogue(reward, 1, "You have created a modified blacksmith's helmet!");
                    }

                    @Override
                    public void run(final int interfaceId, final int componentId) {
                        finish();
                    }

                    @Override
                    public void finish() {
                        player.getInterfaceManager().closeChatBoxInterface();
                    }

                });
                return;
            }
            if (itemUsed.getId() == 31347 && usedWith.getId() == 34922 || usedWith.getId() == 31347 && itemUsed.getId() == 34922) {
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        final int reward = 34926;
                        player.getInventory().deleteItem(itemUsed.getId(), 1);
                        player.getInventory().deleteItem(usedWith.getId(), 1);
                        player.getInventory().addItem(reward, 1);
                        sendItemDialogue(reward, 1, "You have created a modified farmer's hat!");
                    }

                    @Override
                    public void run(final int interfaceId, final int componentId) {
                        finish();
                    }

                    @Override
                    public void finish() {
                        player.getInterfaceManager().closeChatBoxInterface();
                    }
                });
                return;
            }

            if (SlayerHelmet.recolour(player, itemUsed, usedWith)) {
                return;
            }

            if (ItemRecolor.itemRecolor(player, itemUsedId, itemUsedWithId)) {
                return;
            }
            if (ItemRecolor.itemRecolor(player, itemUsedWithId, itemUsedId)) {
                return;
            }
            if (HoodedCapes.handleHooding(player, itemUsedId, itemUsedWithId)) {
                return;
            }
            if (HoodedCapes.handleHooding(player, itemUsedWithId, itemUsedId)) {
                return;
            }
            if (ShadeSkull.attachSkull(player, itemUsed, usedWith)) {
                return;
            }
            if (WeaponPoison.poisons(player, itemUsed, usedWith, false)) {
                return;
            }
            if (itemUsed.getId() == 1775 && usedWith.getId() == 1785 || usedWith.getId() == 1775 && itemUsed.getId() == 1785) {
                CraftingRs3Dialogue.sendGlassblowingInterface(player);
                return;
            }
            if (PrayerBooks.isGodBook(itemUsedId, false) || PrayerBooks.isGodBook(itemUsedWithId, false)) {
                PrayerBooks.bindPages(player, itemUsed.getName().contains(" page ") ? itemUsedWithId : itemUsedId);
                return;
            }
            if (itemUsed.getId() >= 31721 && itemUsed.getId() <= 31724 && usedWith.getId() >= 31721 && usedWith.getId() <= 31724) {
                player.getDialogueManager().startDialogue("NoxiousCreateD");
                return;
            }
            if (itemUsed.getId() >= 31718 && itemUsed.getId() <= 31720 && usedWith.getId() >= 31718 && usedWith.getId() <= 31720) {
                AraxCrafting.handleSpiderLeg(player);
                return;
            }
            if (usedWith.getId() == 30372) {
                MagicNotepaper.noteViaItem(player, usedWith, itemUsed);
                return;
            }

            if (itemUsed.getId() == 30372) {
                MagicNotepaper.noteViaPaper(player, usedWith, itemUsed);
                return;
            }
            if (itemUsed.getId() == 34972 || itemUsed.getId() == 34974 || itemUsed.getId() == 34976) {
                player.getDialogueManager().startDialogue("GlacorBootUpgrade", usedWith, itemUsed);
                return;
            }
            if (usedWith.getId() == 34972 || usedWith.getId() == 34974 || usedWith.getId() == 34976) {
                player.getDialogueManager().startDialogue("GlacorBootUpgrade", itemUsed, usedWith);
                return;
            }
            if (itemUsed.getId() == 2368 && usedWith.getId() == 2366 || itemUsed.getId() == 2366 && usedWith.getId() == 2368) {
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(1187, 1);
                player.sendMessage("You combine both shield parts into a full square shield!");
                return;
            }
            /** Arcane blood necklace **/
            if (itemUsed.getId() == 32692 && usedWith.getId() == 18335 || usedWith.getId() == 32692 && itemUsed.getId() == 18335) {
                if (player.getSkills().getLevel(Skills.CRAFTING) < 80) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of 80 to do this.");
                    return;
                }
                player.getSkills().addXp(Skills.CRAFTING, 200);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(new Item(32694));
                player.addItemsMade();
                player.sendMessage("You attach the shard onto the necklace; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                return;
            }
            /** Brawler's blood necklace */
            if (itemUsed.getId() == 32692 && usedWith.getId() == 31448 || usedWith.getId() == 32692 && itemUsed.getId() == 31448) {
                if (player.getSkills().getLevel(Skills.CRAFTING) < 80) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of 80 to do this.");
                    return;
                }
                player.getSkills().addXp(Skills.CRAFTING, 200);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(new Item(32700));
                player.addItemsMade();
                player.sendMessage("You attach the shard onto the necklace; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                return;
            }
            /** Farsight's blood necklace */
            if (itemUsed.getId() == 32692 && usedWith.getId() == 31445 || usedWith.getId() == 32692 && itemUsed.getId() == 31445) {
                if (player.getSkills().getLevel(Skills.CRAFTING) < 80) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of 80 to do this.");
                    return;
                }
                player.getSkills().addXp(Skills.CRAFTING, 200);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(new Item(32698));
                player.addItemsMade();
                player.sendMessage("You attach the shard onto the necklace; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                return;
            }
            /** Blood amulet of fury **/
            if (itemUsed.getId() == 32692 && usedWith.getId() == 6585 || usedWith.getId() == 32692 && itemUsed.getId() == 6585) {
                if (player.getSkills().getLevel(Skills.CRAFTING) < 80) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of 80 to do this.");
                    return;
                }
                player.getSkills().addXp(Skills.CRAFTING, 200);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(new Item(32703));
                player.addItemsMade();
                player.sendMessage("You attach the shard onto the amulet; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                return;
            }
            /** Air Battlestaff **/
            if (itemUsed.getId() == 573 && usedWith.getId() == 1391 || usedWith.getId() == 573 && itemUsed.getId() == 1391) {
                if (player.getSkills().getLevel(Skills.CRAFTING) < 66) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of 66 to do this.");
                    return;
                }
                player.getSkills().addXp(Skills.CRAFTING, 137.5);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(new Item(1397));
                player.addItemsMade();
                player.sendMessage("You attach the orb to the battlestaff; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                return;
            }
            /** Water Battlestaff **/
            if (itemUsed.getId() == 571 && usedWith.getId() == 1391 || usedWith.getId() == 571 && itemUsed.getId() == 1391) {
                if (player.getSkills().getLevel(Skills.CRAFTING) < 54) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of 54 to do this.");
                    return;
                }
                player.getSkills().addXp(Skills.CRAFTING, 100);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(new Item(1395));
                player.addItemsMade();
                player.sendMessage("You attach the orb to the battlestaff; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                return;
            }
            /** Earth Battlestaff **/
            if (itemUsed.getId() == 575 && usedWith.getId() == 1391 || usedWith.getId() == 575 && itemUsed.getId() == 1391) {
                if (player.getSkills().getLevel(Skills.CRAFTING) < 58) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of 58 to do this.");
                    return;
                }
                player.getSkills().addXp(Skills.CRAFTING, 112.5);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(new Item(1399));
                player.addItemsMade();
                player.sendMessage("You attach the orb to the battlestaff; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                return;
            }
            /** Fire Battlestaff **/
            if (itemUsed.getId() == 569 && usedWith.getId() == 1391 || usedWith.getId() == 569 && itemUsed.getId() == 1391) {
                if (player.getSkills().getLevel(Skills.CRAFTING) < 62) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of 62 to do this.");
                    return;
                }
                player.getSkills().addXp(Skills.CRAFTING, 125);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(new Item(1393));
                player.addItemsMade();
                player.sendMessage("You attach the orb to the battlestaff; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                return;
            }
            /** Uncut hydrix **/
            if (itemUsed.getId() == 6573 && usedWith.getId() == 31851 || usedWith.getId() == 6573 && itemUsed.getId() == 31851) {
                if (player.getSkills().getLevel(Skills.CRAFTING) < 79) {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of 79 to do this.");
                    return;
                }
                player.getSkills().addXp(Skills.CRAFTING, 19.75);
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(new Item(31853));
                player.addItemsMade();
                player.sendMessage("You smash the onyx onto the hydrix; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                return;
            }
            if (player.getEquipment().getWeaponId() == -1 && itemUsed.getId() == 22448 && usedWith.getId() == 22498) {
                if (player.getInventory().containsItem(22448, 3000) && (player.getInventory().containsItem(554, 15000))) {
                    player.setNextAnimation(new Animation(15434));
                    player.setNextGraphics(new Graphics(2032));
                    player.getInventory().deleteItem(554, 15000);
                    player.getInventory().deleteItem(22448, 3000);
                    player.getInventory().deleteItem(22498, 1);
                    player.getInventory().addItem(22494, 1);
                    return;
                }
            }
            if (itemUsed.getId() == 985 && usedWith.getId() == 987 || itemUsed.getId() == 987 && usedWith.getId() == 985) {
                CrystalChest.makeKey(player);
                return;
            }
            if (itemUsed.getName().startsWith("Royal") && usedWith.getName().startsWith("Royal")) {
                if (player.getInventory().containsItem(24344, 1) && player.getInventory().containsItem(24346, 1) && player.getInventory().containsItem(24342, 1) && player.getInventory().containsItem(24340, 1)) {
                    if (player.getSkills().getLevel(Skills.CRAFTING) >= 70) {
                        player.getInventory().deleteItem(24340, 1);
                        player.getInventory().deleteItem(24342, 1);
                        player.getInventory().deleteItem(24344, 1);
                        player.getInventory().deleteItem(24346, 1);
                        player.getInventory().addItem(24337, 1);
                    } else {
                        player.getDialogueManager().startDialogue("SimpleMessage", "You need at least a crafting level of 70 to make a royal crossbow!");
                    }
                } else {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You'll need these Royal items: torsion sight, spring, frame and bolt stabiliser in order to make a Royal Crossbow!");
                }
                return;
            }
            final int string = 1759; // Stringing necklaces
            if ((itemUsed.getId() == string || usedWith.getId() == string) && (itemUsed.getId() == 1673 || usedWith.getId() == 1673)) {// Gold
                // amulet
                if (player.getSkills().getLevel(Skills.CRAFTING) >= 8) {
                    player.addItemsMade();
                    player.sendMessage("You've successfully made jewellery; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                    player.getInventory().deleteItem(string, 1);
                    player.getInventory().deleteItem(1673, 1);
                    player.getInventory().addItem(1692, 1);
                } else {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You will need a crafting level of 8 to string this amulet!");
                }
                return;
            }
            if ((itemUsed.getId() == string || usedWith.getId() == string) && (itemUsed.getId() == 1675 || usedWith.getId() == 1675)) {// Sapphire
                // amulet
                if (player.getSkills().getLevel(Skills.CRAFTING) >= 24) {
                    player.addItemsMade();
                    player.sendMessage("You've successfully made jewellery; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                    player.getInventory().deleteItem(string, 1);
                    player.getInventory().deleteItem(1675, 1);
                    player.getInventory().addItem(1694, 1);
                } else {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You will need a crafting level of 24 to string this amulet!");
                }
                return;
            }
            if ((itemUsed.getId() == string || usedWith.getId() == string) && (itemUsed.getId() == 1677 || usedWith.getId() == 1677)) {// Emerald
                // amulet
                if (player.getSkills().getLevel(Skills.CRAFTING) >= 31) {
                    player.addItemsMade();
                    player.sendMessage("You've successfully made jewellery; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                    player.getInventory().deleteItem(string, 1);
                    player.getInventory().deleteItem(1677, 1);
                    player.getInventory().addItem(1696, 1);
                } else {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You will need a crafting level of 31 to string this amulet!");
                }
                return;
            }
            if ((itemUsed.getId() == string || usedWith.getId() == string) && (itemUsed.getId() == 1679 || usedWith.getId() == 1679)) {// Ruby
                // amulet
                if (player.getSkills().getLevel(Skills.CRAFTING) >= 50) {
                    player.addItemsMade();
                    player.sendMessage("You've successfully made jewellery; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                    player.getInventory().deleteItem(string, 1);
                    player.getInventory().deleteItem(1679, 1);
                    player.getInventory().addItem(1698, 1);
                } else {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You will need a crafting level of 50 to string this amulet!");
                }
                return;
            }
            if ((itemUsed.getId() == string || usedWith.getId() == string) && (itemUsed.getId() == 1681 || usedWith.getId() == 1681)) {// Diamond
                // amulet
                if (player.getSkills().getLevel(Skills.CRAFTING) >= 70) {
                    player.addItemsMade();
                    player.sendMessage("You've successfully made jewellery; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                    player.getInventory().deleteItem(string, 1);
                    player.getInventory().deleteItem(1681, 1);
                    player.getInventory().addItem(1700, 1);
                } else {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You will need a crafting level of 70 to string this amulet!");
                }
                return;
            }
            if ((itemUsed.getId() == string || usedWith.getId() == string) && (itemUsed.getId() == 1683 || usedWith.getId() == 1683)) {// Dragonstone
                // amulet
                if (player.getSkills().getLevel(Skills.CRAFTING) >= 80) {
                    player.addItemsMade();
                    player.sendMessage("You've successfully made jewellery; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                    player.getInventory().deleteItem(string, 1);
                    player.getInventory().deleteItem(1683, 1);
                    player.getInventory().addItem(1702, 1);
                } else {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You will need a crafting level of 80 to string this amulet!");
                }
                return;
            }
            if ((itemUsed.getId() == string || usedWith.getId() == string) && (itemUsed.getId() == 6579 || usedWith.getId() == 6579)) {// Onyx
                // amulet
                if (player.getSkills().getLevel(Skills.CRAFTING) >= 90) {
                    player.addItemsMade();
                    player.sendMessage("You've successfully made jewellery; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
                    player.getInventory().deleteItem(string, 1);
                    player.getInventory().deleteItem(6579, 1);
                    player.getInventory().addItem(6581, 1);
                } else {
                    player.getDialogueManager().startDialogue("SimpleMessage", "You will need a crafting level of 90 to string this amulet!");
                }
                return;
            }
            if (itemUsed.getId() == 985 && usedWith.getId() == 987 || itemUsed.getId() == 987 && usedWith.getId() == 985) {
                CrystalChest.makeKey(player);
                return;
            } // guthix's book of balance
            if (itemUsed.getId() == 3843 && usedWith.getId() == 3835 || itemUsed.getId() == 3843 && usedWith.getId() == 3836 || itemUsed.getId() == 3843 && usedWith.getId() == 3837 || itemUsed.getId() == 3843 && usedWith.getId() == 3838 || itemUsed.getId() == 3835 && usedWith.getId() == 3843 || itemUsed.getId() == 3836 && usedWith.getId() == 3843 || itemUsed.getId() == 3837 && usedWith.getId() == 3843 || itemUsed.getId() == 3838 && usedWith.getId() == 3843) {
                if (!player.getInventory().containsItem(3835, 1) || !player.getInventory().containsItem(3836, 1) || !player.getInventory().containsItem(3837, 1) || !player.getInventory().containsItem(3838, 1)) {
                    player.sendMessage("You will need all 4 pages in order to complete your book!");
                    return;
                }
                player.sendMessage("You carefully put all 4 pages back in the book.");
                player.getInventory().deleteItem(3843, 1);
                player.getInventory().deleteItem(3835, 1);
                player.getInventory().deleteItem(3836, 1);
                player.getInventory().deleteItem(3837, 1);
                player.getInventory().deleteItem(3838, 1);
                player.getInventory().addItem(3844, 1);
                return;
            } // zamorak's unholy book
            if (itemUsed.getId() == 3841 && usedWith.getId() == 3831 || itemUsed.getId() == 3841 && usedWith.getId() == 3832 || itemUsed.getId() == 3841 && usedWith.getId() == 3833 || itemUsed.getId() == 3841 && usedWith.getId() == 3834 || itemUsed.getId() == 3831 && usedWith.getId() == 3841 || itemUsed.getId() == 3832 && usedWith.getId() == 3841 || itemUsed.getId() == 3833 && usedWith.getId() == 3841 || itemUsed.getId() == 3834 && usedWith.getId() == 3841) {
                if (!player.getInventory().containsItem(3831, 1) || !player.getInventory().containsItem(3832, 1) || !player.getInventory().containsItem(3833, 1) || !player.getInventory().containsItem(3834, 1)) {
                    player.sendMessage("You will need all 4 pages in order to complete your book!");
                    return;
                }
                player.sendMessage("You carefully put all 4 pages back in the book.");
                player.getInventory().deleteItem(3841, 1);
                player.getInventory().deleteItem(3831, 1);
                player.getInventory().deleteItem(3832, 1);
                player.getInventory().deleteItem(3833, 1);
                player.getInventory().deleteItem(3834, 1);
                player.getInventory().addItem(3842, 1);
                return;
            }
            // saradomin's holy book
            if (itemUsed.getId() == 3839 && usedWith.getId() == 3827 || itemUsed.getId() == 3839 && usedWith.getId() == 3828 || itemUsed.getId() == 3839 && usedWith.getId() == 3829 || itemUsed.getId() == 3839 && usedWith.getId() == 3830 || itemUsed.getId() == 3827 && usedWith.getId() == 3839 || itemUsed.getId() == 3828 && usedWith.getId() == 3839 || itemUsed.getId() == 3829 && usedWith.getId() == 3839 || itemUsed.getId() == 3830 && usedWith.getId() == 3839) {
                if (!player.getInventory().containsItem(3827, 1) || !player.getInventory().containsItem(3828, 1) || !player.getInventory().containsItem(3829, 1) || !player.getInventory().containsItem(3830, 1)) {
                    player.sendMessage("You will need all 4 pages in order to complete your book!");
                    return;
                }
                player.sendMessage("You carefully put all 4 pages back in the book.");
                player.getInventory().deleteItem(3839, 1);
                player.getInventory().deleteItem(3827, 1);
                player.getInventory().deleteItem(3828, 1);
                player.getInventory().deleteItem(3829, 1);
                player.getInventory().deleteItem(3830, 1);
                player.getInventory().addItem(3840, 1);
                return;
            }
            // ancient book
            if (itemUsed.getId() == 19616 && usedWith.getId() == 19608 || itemUsed.getId() == 19616 && usedWith.getId() == 19609 || itemUsed.getId() == 19616 && usedWith.getId() == 19610 || itemUsed.getId() == 19616 && usedWith.getId() == 19611 || itemUsed.getId() == 19608 && usedWith.getId() == 19616 || itemUsed.getId() == 19609 && usedWith.getId() == 19616 || itemUsed.getId() == 19610 && usedWith.getId() == 19616 || itemUsed.getId() == 19611 && usedWith.getId() == 19616) {
                if (!player.getInventory().containsItem(19608, 1) || !player.getInventory().containsItem(19609, 1) || !player.getInventory().containsItem(19610, 1) || !player.getInventory().containsItem(19611, 1)) {
                    player.sendMessage("You will need all 4 pages in order to complete your book!");
                    return;
                }
                player.sendMessage("You carefully put all 4 pages back in the book.");
                player.getInventory().deleteItem(19616, 1);
                player.getInventory().deleteItem(19608, 1);
                player.getInventory().deleteItem(19609, 1);
                player.getInventory().deleteItem(19610, 1);
                player.getInventory().deleteItem(19611, 1);
                player.getInventory().addItem(19617, 1);
                return;
            }
            // armadyl's book of law
            if (itemUsed.getId() == 19614 && usedWith.getId() == 19604 || itemUsed.getId() == 19614 && usedWith.getId() == 19605 || itemUsed.getId() == 19614 && usedWith.getId() == 19606 || itemUsed.getId() == 19614 && usedWith.getId() == 19607 || itemUsed.getId() == 19604 && usedWith.getId() == 19614 || itemUsed.getId() == 19605 && usedWith.getId() == 19614 || itemUsed.getId() == 19606 && usedWith.getId() == 19614 || itemUsed.getId() == 19607 && usedWith.getId() == 19614) {
                if (!player.getInventory().containsItem(19604, 1) || !player.getInventory().containsItem(19605, 1) || !player.getInventory().containsItem(19606, 1) || !player.getInventory().containsItem(19607, 1)) {
                    player.sendMessage("You will need all 4 pages in order to complete your book!");
                    return;
                }
                player.sendMessage("You carefully put all 4 pages back in the book.");
                player.getInventory().deleteItem(19614, 1);
                player.getInventory().deleteItem(19604, 1);
                player.getInventory().deleteItem(19605, 1);
                player.getInventory().deleteItem(19606, 1);
                player.getInventory().deleteItem(19607, 1);
                player.getInventory().addItem(19615, 1);
                return;
            }
            // bandos' book of war
            if (itemUsed.getId() == 19612 && usedWith.getId() == 19600 || itemUsed.getId() == 19612 && usedWith.getId() == 19601 || itemUsed.getId() == 19612 && usedWith.getId() == 19602 || itemUsed.getId() == 19612 && usedWith.getId() == 19603 || itemUsed.getId() == 19600 && usedWith.getId() == 19612 || itemUsed.getId() == 19601 && usedWith.getId() == 19612 || itemUsed.getId() == 19602 && usedWith.getId() == 19612 || itemUsed.getId() == 19603 && usedWith.getId() == 19612) {
                if (!player.getInventory().containsItem(19600, 1) || !player.getInventory().containsItem(19601, 1) || !player.getInventory().containsItem(19602, 1) || !player.getInventory().containsItem(19603, 1)) {
                    player.sendMessage("You will need all 4 pages in order to complete your book!");
                    return;
                }
                player.sendMessage("You carefully put all 4 pages back in the book.");
                player.getInventory().deleteItem(19612, 1);
                player.getInventory().deleteItem(19600, 1);
                player.getInventory().deleteItem(19601, 1);
                player.getInventory().deleteItem(19602, 1);
                player.getInventory().deleteItem(19603, 1);
                player.getInventory().addItem(19613, 1);
                return;
            }
            if (itemUsed.getId() == 1765 && usedWith.getId() == 1767 || usedWith.getId() == 1765 && itemUsed.getId() == 1767) {
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        sendItemDialogue(1771, 1, "Are you sure you want to combine these two dyes?");
                        stage = 0;
                    }

                    @Override
                    public void run(final int interfaceId, final int componentId) {
                        switch (stage) {
                        case 0:
                            sendOptionsDialogue("Choose an option", "Yes", "No");
                            stage = 1;
                            break;
                        case 1:
                            finish();
                            switch (componentId) {
                            case OPTION_1:
                                player.getInventory().deleteItem(1765, 1);
                                player.getInventory().deleteItem(1767, 1);
                                player.getInventory().addItem(1771, 1);
                                player.sendMessage(Colors.GREEN + "You combine the two dyes into green!", true);
                                break;
                            case OPTION_2:
                                break;
                            }
                            break;
                        }
                    }

                    @Override
                    public void finish() {
                        player.getInterfaceManager().closeChatBoxInterface();
                    }

                });

                if (itemUsed.getId() == 30027 && itemUsed.getId() == 11848 && itemUsed.getId() == 11848 && itemUsed.getId() == 30027) {
                    {
                        player.setNextGraphics(new Graphics(4834));
                        player.sendMessage("You merge the powerful energy and the Dharok's set!");
                        player.getInventory().deleteItem(30027, 250);
                        player.getInventory().deleteItem(11841, 1);
                        player.getInventory().addItem(30032, 1);
                        return;
                    }
                }

            }
            // dragonbone upgrade kits
            if (itemUsed.getId() == 24352 && usedWith.getId() == 6918 || itemUsed.getId() == 6918 && usedWith.getId() == 24352) {
                player.sendMessage("You attach the upgrade kit on the equipment piece!");
                player.getInventory().deleteItem(24352, 1);
                player.getInventory().deleteItem(6918, 1);
                player.getInventory().addItem(24354, 1);
                return;
            } // dragonbone upgrade kits
            if (itemUsed.getId() == 24352 && usedWith.getId() == 6916 || itemUsed.getId() == 6916 && usedWith.getId() == 24352) {
                player.sendMessage("You attach the upgrade kit on the equipment piece!");
                player.getInventory().deleteItem(24352, 1);
                player.getInventory().deleteItem(6916, 1);
                player.getInventory().addItem(24355, 1);
                return;
            } // dragonbone upgrade kits
            if (itemUsed.getId() == 24352 && usedWith.getId() == 6924 || itemUsed.getId() == 6924 && usedWith.getId() == 24352) {
                player.sendMessage("You attach the upgrade kit on the equipment piece!");
                player.getInventory().deleteItem(24352, 1);
                player.getInventory().deleteItem(6924, 1);
                player.getInventory().addItem(24356, 1);
                return;
            } // dragonbone upgrade kits
            if (itemUsed.getId() == 24352 && usedWith.getId() == 6920 || itemUsed.getId() == 6920 && usedWith.getId() == 24352) {
                player.sendMessage("You attach the upgrade kit on the equipment piece!");
                player.getInventory().deleteItem(24352, 1);
                player.getInventory().deleteItem(6920, 1);
                player.getInventory().addItem(24358, 1);
                return;
            } // dragonbone upgrade kits
            if (itemUsed.getId() == 24352 && usedWith.getId() == 6922 || itemUsed.getId() == 6922 && usedWith.getId() == 24352) {
                player.sendMessage("You attach the upgrade kit on the equipment piece!");
                player.getInventory().deleteItem(24352, 1);
                player.getInventory().deleteItem(6922, 1);
                player.getInventory().addItem(24357, 1);
                return;
            } // dragonbone upgrade kits
            if (itemUsed.getId() == 24352 && usedWith.getId() == 11335 || itemUsed.getId() == 11335 && usedWith.getId() == 24352) {
                player.sendMessage("You attach the upgrade kit on the equipment piece!");
                player.getInventory().deleteItem(24352, 1);
                player.getInventory().deleteItem(11335, 1);
                player.getInventory().addItem(24359, 1);
                return;
            } // dragonbone upgrade kits
            if (itemUsed.getId() == 24352 && usedWith.getId() == 14479 || itemUsed.getId() == 14479 && usedWith.getId() == 24352) {
                player.sendMessage("You attach the upgrade kit on the equipment piece!");
                player.getInventory().deleteItem(24352, 1);
                player.getInventory().deleteItem(14479, 1);
                player.getInventory().addItem(24360, 1);
                return;
            } // dragonbone upgrade kits
            if (itemUsed.getId() == 24352 && usedWith.getId() == 4087 || itemUsed.getId() == 4087 && usedWith.getId() == 24352) {
                player.sendMessage("You attach the upgrade kit on the equipment piece!");
                player.getInventory().deleteItem(24352, 1);
                player.getInventory().deleteItem(4087, 1);
                player.getInventory().addItem(24363, 1);
                return;
            } // dragonbone upgrade kits
            if (itemUsed.getId() == 24352 && usedWith.getId() == 4585 || itemUsed.getId() == 4585 && usedWith.getId() == 24352) {
                player.sendMessage("You attach the upgrade kit on the equipment piece!");
                player.getInventory().deleteItem(24352, 1);
                player.getInventory().deleteItem(4585, 1);
                player.getInventory().addItem(24364, 1);
                return;
            } // dragonbone upgrade kits
            if (itemUsed.getId() == 24352 && usedWith.getId() == 11732 || itemUsed.getId() == 11732 && usedWith.getId() == 24352) {
                player.sendMessage("You attach the upgrade kit on the equipment piece!");
                player.getInventory().deleteItem(24352, 1);
                player.getInventory().deleteItem(11732, 1);
                player.getInventory().addItem(24362, 1);
                return;
            } // dragonbone upgrade kits
            if (itemUsed.getId() == 24352 && usedWith.getId() == 7461 || itemUsed.getId() == 7461 && usedWith.getId() == 24352) {
                player.sendMessage("You attach the upgrade kit on the equipment piece!");
                player.getInventory().deleteItem(24352, 1);
                player.getInventory().deleteItem(7461, 1);
                player.getInventory().addItem(24361, 1);
                return;
            } // godsword shard 1+2 = godsword shards
            if (itemUsed.getId() == 11712 && usedWith.getId() == 11710 || itemUsed.getId() == 11710 && usedWith.getId() == 11712) {
                if (player.getSkills().getLevel(Skills.SMITHING) >= 80) {
                    player.sendMessage("You use your smithing knowledge to attach the 2 shards together!");
                    player.getInventory().deleteItem(11712, 1);
                    player.getInventory().deleteItem(11710, 1);
                    player.getInventory().addItem(11686, 1);
                    player.getSkills().addXp(Skills.SMITHING, 100);
                } else {
                    player.sendMessage("You need a smithing level of 80 to do this!");
                }
                return;
            }
            // Holy elixir + spirit shield = Blessed spirit shield
            if (itemUsed.getId() == 13734 && usedWith.getId() == 13754 || itemUsed.getId() == 13754 && usedWith.getId() == 13734) {
                if (player.getSkills().getLevel(Skills.PRAYER) >= 85) {
                    player.sendMessage("You combine the holy elixir with the the spirit shield to make a Blessed spirit shield!");
                    player.getInventory().deleteItem(13734, 1);
                    player.getInventory().deleteItem(13754, 1);
                    player.getInventory().addItem(13736, 1);
                    player.getSkills().addXp(Skills.PRAYER, 1500);
                } else {
                    player.sendMessage("You need a Prayer level of 85 to bless the shield.");
                }
                return;
            }
            // Blessed spirit shield + Arcane sigil = Arcane spirit shield
            if (itemUsed.getId() == 13736 && usedWith.getId() == 13746 || itemUsed.getId() == 13746 && usedWith.getId() == 13736) {
                if (player.getSkills().getLevel(Skills.PRAYER) >= 90 && player.getSkills().getLevel(Skills.SMITHING) >= 85) {
                    player.sendMessage("You combine the sigil with the spirit shield to make an Arcane spirit shield!");
                    player.getInventory().deleteItem(13736, 1);
                    player.getInventory().deleteItem(13746, 1);
                    player.getInventory().addItem(13738, 1);
                    player.getSkills().addXp(Skills.SMITHING, 1800);
                } else {
                    player.sendMessage("You need a Smithing level of 85 and a Prayer level of 90 to do this.");
                }
                return;
            }
            // Blessed spirit shield + Divine sigil = Divine spirit shield
            if (itemUsed.getId() == 13736 && usedWith.getId() == 13748 || itemUsed.getId() == 13748 && usedWith.getId() == 13736) {
                if (player.getSkills().getLevel(Skills.PRAYER) >= 90 && player.getSkills().getLevel(Skills.SMITHING) >= 85) {
                    player.sendMessage("You combine the sigil with the spirit shield to make a Divine spirit shield!");
                    player.getInventory().deleteItem(13736, 1);
                    player.getInventory().deleteItem(13748, 1);
                    player.getInventory().addItem(13740, 1);
                    player.getSkills().addXp(Skills.SMITHING, 1800);
                } else {
                    player.sendMessage("You need a Smithing level of 85 and a Prayer level of 90 to do this.");
                }
                return;
            }
            // Blessed spirit shield + Elysian sigil = Elysian spirit shield
            if (itemUsed.getId() == 13736 && usedWith.getId() == 13750 || itemUsed.getId() == 13750 && usedWith.getId() == 13736) {
                if (player.getSkills().getLevel(Skills.PRAYER) >= 90 && player.getSkills().getLevel(Skills.SMITHING) >= 85) {
                    player.sendMessage("You combine the sigil with the spirit shield to make an Elysian spirit shield!");
                    player.getInventory().deleteItem(13736, 1);
                    player.getInventory().deleteItem(13750, 1);
                    player.getInventory().addItem(13742, 1);
                    player.getSkills().addXp(Skills.SMITHING, 1800);
                } else {
                    player.sendMessage("You need a Smithing level of 85 and a Prayer level of 90 to do this.");
                }
                return;
            }
            // Blessed spirit shield + Spectral sigil = Spectral spirit shield
            if (itemUsed.getId() == 13736 && usedWith.getId() == 13752 || itemUsed.getId() == 13752 && usedWith.getId() == 13736) {
                if (player.getSkills().getLevel(Skills.PRAYER) >= 90 && player.getSkills().getLevel(Skills.SMITHING) >= 85) {
                    player.sendMessage("You combine the sigil with the spirit shield to make a Spectral spirit shield!");
                    player.getInventory().deleteItem(13736, 1);
                    player.getInventory().deleteItem(13752, 1);
                    player.getInventory().addItem(13744, 1);
                    player.getSkills().addXp(Skills.SMITHING, 1800);
                } else {
                    player.sendMessage("You need a Smithing level of 85 and a Prayer level of 90 to do this.");
                }
                return;
            } // whip vine
            if (itemUsed.getId() == 21369 && usedWith.getId() == 4151 || itemUsed.getId() == 4151 && usedWith.getId() == 21369) {
                player.getInventory().deleteItem(21369, 1);
                player.getInventory().deleteItem(4151, 1);
                player.getInventory().addItem(21371, 1);
                player.sendMessage("You attach the whip vine onto your abyssal whip!");
                return;
            } // Dragon full helm (or)
            if (itemUsed.getId() == 19346 && usedWith.getId() == 11335 || itemUsed.getId() == 11335 && usedWith.getId() == 19346) {
                player.getInventory().deleteItem(11335, 1);
                player.getInventory().deleteItem(19346, 1);
                player.getInventory().addItem(19336, 1);
                player.sendMessage("You upgrade your Dragon full helm to Dragon full helm (or) with the ornament kit!");
                return;
            }
            // Dragon platebody (or)
            if (itemUsed.getId() == 19350 && usedWith.getId() == 14479 || itemUsed.getId() == 14479 && usedWith.getId() == 19350) {
                player.getInventory().deleteItem(14479, 1);
                player.getInventory().deleteItem(19350, 1);
                player.getInventory().addItem(19337, 1);
                player.sendMessage("You upgrade your Dragon platebody to Dragon platebody (or) with the ornament kit!");
                return;
            }
            // Dragon platelegs (or)
            if (itemUsed.getId() == 19348 && usedWith.getId() == 4087 || itemUsed.getId() == 4087 && usedWith.getId() == 19348) {
                player.getInventory().deleteItem(4087, 1);
                player.getInventory().deleteItem(19348, 1);
                player.getInventory().addItem(19338, 1);
                player.sendMessage("You upgrade your Dragon platelegs to Dragon platelegs (or) with the ornament kit!");
                return;
            }
            // Dragon plateskirt (or)
            if (itemUsed.getId() == 19348 && usedWith.getId() == 4585 || itemUsed.getId() == 4585 && usedWith.getId() == 19348) {
                player.getInventory().deleteItem(4585, 1);
                player.getInventory().deleteItem(19348, 1);
                player.getInventory().addItem(19339, 1);
                player.sendMessage("You upgrade your Dragon plateskirt to Dragon plateskirt (or) with the ornament kit!");
                return;
            }
            // Dragon square shield (or)
            if (itemUsed.getId() == 19352 && usedWith.getId() == 1187 || itemUsed.getId() == 1187 && usedWith.getId() == 19352) {
                player.getInventory().deleteItem(1187, 1);
                player.getInventory().deleteItem(19352, 1);
                player.getInventory().addItem(19340, 1);
                player.sendMessage("You upgrade your Dragon square shield to Dragon square shield (or) with the ornament kit!");
                return;
            }
            // Dragon kiteshield (or)
            if (itemUsed.getId() == 24365 && usedWith.getId() == 25312 || itemUsed.getId() == 25312 && usedWith.getId() == 24365) {
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(25320, 1);
                player.sendMessage("You upgrade your Dragon kiteshield to Dragon kiteshield (or) with the ornament kit!");
                return;
            }
            // Dragon kiteshield (sp)
            if (itemUsed.getId() == 24365 && usedWith.getId() == 25314 || itemUsed.getId() == 25314 && usedWith.getId() == 24365) {
                player.getInventory().deleteItem(itemUsed);
                player.getInventory().deleteItem(usedWith);
                player.getInventory().addItem(25321, 1);
                player.sendMessage("You upgrade your Dragon kiteshield to Dragon kiteshield (sp) with the ornament kit!");
                return;
            }

            // Dragon full helm (sp)
            if (itemUsed.getId() == 19354 && usedWith.getId() == 11335 || itemUsed.getId() == 11335 && usedWith.getId() == 19354) {
                player.getInventory().deleteItem(11335, 1);
                player.getInventory().deleteItem(19354, 1);
                player.getInventory().addItem(19341, 1);
                player.sendMessage("You upgrade your Dragon full helm to Dragon full helm (sp) with the ornament kit!");
                return;
            }
            // Dragon platebody (sp)
            if (itemUsed.getId() == 19358 && usedWith.getId() == 14479 || itemUsed.getId() == 14479 && usedWith.getId() == 19358) {
                player.getInventory().deleteItem(14479, 1);
                player.getInventory().deleteItem(19358, 1);
                player.getInventory().addItem(19342, 1);
                player.sendMessage("You upgrade your Dragon platebody to Dragon platebody (sp) with the ornament kit!");
                return;
            }
            // Dragon platelegs (sp)
            if (itemUsed.getId() == 19356 && usedWith.getId() == 4087 || itemUsed.getId() == 4087 && usedWith.getId() == 19356) {
                player.getInventory().deleteItem(4087, 1);
                player.getInventory().deleteItem(19356, 1);
                player.getInventory().addItem(19343, 1);
                player.sendMessage("You upgrade your Dragon platelegs to Dragon platelegs (sp) with the ornament kit!");
                return;
            }
            // Dragon plateskirt (sp)
            if (itemUsed.getId() == 19356 && usedWith.getId() == 4585 || itemUsed.getId() == 4585 && usedWith.getId() == 19356) {
                player.getInventory().deleteItem(4585, 1);
                player.getInventory().deleteItem(19356, 1);
                player.getInventory().addItem(19344, 1);
                player.sendMessage("You upgrade your Dragon plateskirt to Dragon plateskirt (sp) with the ornament kit!");
                return;
            }
            // Dragon square shield (sp)
            if (itemUsed.getId() == 19360 && usedWith.getId() == 1187 || itemUsed.getId() == 1187 && usedWith.getId() == 19360) {
                player.getInventory().deleteItem(1187, 1);
                player.getInventory().deleteItem(19360, 1);
                player.getInventory().addItem(19345, 1);
                player.sendMessage("You upgrade your Dragon square shield to Dragon square shield (sp) with the ornament kit!");
                return;
            }

            // T92 weapon combinations

            if (usedWith.getId() == 37624 || itemUsedId == 37624) {
                boolean usedWithIsDormant = usedWith.getId() == 37624;
                if (usedWithIsDormant && itemUsedId >= 37619 && itemUsedId <= 37621 || (!usedWithIsDormant && usedWith.getId() >= 37619 && usedWith.getId() <= 37621)) {
                    boolean hasRequiredItems = player.getInventory().containsItem(37624, 1);
                    for (int i = 0; i < 3; i++)
                        if (!player.getInventory().containsItem(37619 + i, 1)) {
                            hasRequiredItems = false;
                            break;
                        }
                    if (!hasRequiredItems) {
                        player.getPackets().sendGameMessage("You don't have all of the required items to make a Staff Of the Sliske.");
                        return;
                    }
                    player.getInventory().deleteItem(37624, 1);
                    for (int i = 0; i < 3; i++)
                        player.getInventory().deleteItem(37619 + i, 1);
                    player.getInventory().addItem(37636, 1);
                    player.getPackets().sendGameMessage("You combine all of the orbs to make a Staff Of the Sliske.");
                }
                return;
            }
            if (usedWith.getId() == 37622 || itemUsedId == 37622) {
                boolean usedWithIsDormant = usedWith.getId() == 37622;
                if (usedWithIsDormant && itemUsedId >= 37619 && itemUsedId <= 37621 || (!usedWithIsDormant && usedWith.getId() >= 37619 && usedWith.getId() <= 37621)) {
                    boolean hasRequiredItems = player.getInventory().containsItem(37622, 1);
                    for (int i = 0; i < 3; i++)
                        if (!player.getInventory().containsItem(37619 + i, 1)) {
                            hasRequiredItems = false;
                            break;
                        }
                    if (!hasRequiredItems) {
                        player.getPackets().sendGameMessage("You don't have all of the required items to make a Seren Godbow.");
                        return;
                    }
                    player.getInventory().deleteItem(37622, 1);
                    for (int i = 0; i < 3; i++)
                        player.getInventory().deleteItem(37619 + i, 1);
                    player.getInventory().addItem(37632, 1);
                    player.getPackets().sendGameMessage("You combine all of the orbs to make a Seren Godbow.");
                }
                return;
            }
            if (usedWith.getId() == 37626 || itemUsedId == 37626) {
                boolean usedWithIsDormant = usedWith.getId() == 37626;
                if (usedWithIsDormant && itemUsedId >= 37619 && itemUsedId <= 37621 || (!usedWithIsDormant && usedWith.getId() >= 37619 && usedWith.getId() <= 37621)) {
                    boolean hasRequiredItems = player.getInventory().containsItem(37626, 1);
                    for (int i = 0; i < 3; i++)
                        if (!player.getInventory().containsItem(37619 + i, 1)) {
                            hasRequiredItems = false;
                            break;
                        }
                    if (!hasRequiredItems) {
                        player.getPackets().sendGameMessage("You don't have all of the required items to make a Zaros Godsword.");
                        return;
                    }
                    player.getInventory().deleteItem(37626, 1);
                    for (int i = 0; i < 3; i++)
                        player.getInventory().deleteItem(37619 + i, 1);
                    player.getInventory().addItem(37640, 1);
                    player.getPackets().sendGameMessage("You combine all of the orbs to make a Zaros Godsword.");
                }
                player.getPackets().sendGameMessage("Nothing interesting happens.");
                return;
            }
            // godsword shards 1&2 + 3
            if (itemUsed.getId() == 11686 && usedWith.getId() == 11714 || itemUsed.getId() == 11714 && usedWith.getId() == 11686) {
                if (player.getSkills().getLevel(Skills.SMITHING) >= 80) {
                    player.sendMessage("You use your smithing knowledge to attach the last shard and make a blade!");
                    player.getInventory().deleteItem(11686, 1);
                    player.getInventory().deleteItem(11714, 1);
                    player.getInventory().addItem(11690, 1);
                    player.getSkills().addXp(Skills.SMITHING, 100);
                } else {
                    player.sendMessage("You need a smithing level of 80 to do this!");
                }
                return;
            }
            // godsword shards 2&3 + 1
            if (itemUsed.getId() == 11692 && usedWith.getId() == 11710 || itemUsed.getId() == 11710 && usedWith.getId() == 11692) {
                if (player.getSkills().getLevel(Skills.SMITHING) >= 80) {
                    player.sendMessage("You use your smithing knowledge to attach the last shard and make a blade!");
                    player.getInventory().deleteItem(11692, 1);
                    player.getInventory().deleteItem(11710, 1);
                    player.getInventory().addItem(11690, 1);
                    player.getSkills().addXp(Skills.SMITHING, 100);
                } else {
                    player.sendMessage("You need a smithing level of 80 to do this!");
                }
                return;
            }
            // godsword shards 1&3 + 2
            if (itemUsed.getId() == 11688 && usedWith.getId() == 11712 || itemUsed.getId() == 11712 && usedWith.getId() == 11688) {
                if (player.getSkills().getLevel(Skills.SMITHING) >= 80) {
                    player.sendMessage("You use your smithing knowledge to attach the last shard and make a blade!");
                    player.getInventory().deleteItem(11688, 1);
                    player.getInventory().deleteItem(11712, 1);
                    player.getInventory().addItem(11690, 1);
                    player.getSkills().addXp(Skills.SMITHING, 100);
                } else {
                    player.sendMessage("You need a smithing level of 80 to do this!");
                }
                return;
            }
            // godsword shards 1 + 2
            if (itemUsed.getId() == 11710 && usedWith.getId() == 11712 || itemUsed.getId() == 11712 && usedWith.getId() == 11710) {
                if (player.getSkills().getLevel(Skills.SMITHING) >= 80) {
                    player.sendMessage("You use your smithing knowledge to attach the 2 shards together!");
                    player.getInventory().deleteItem(11710, 1);
                    player.getInventory().deleteItem(11712, 1);
                    player.getInventory().addItem(11686, 1);
                    player.getSkills().addXp(Skills.SMITHING, 100);
                } else {
                    player.sendMessage("You need a smithing level of 80 to do this!");
                }
                return;
            }
            // godsword shards 2 + 3
            if (itemUsed.getId() == 11712 && usedWith.getId() == 11714 || itemUsed.getId() == 11714 && usedWith.getId() == 11712) {
                if (player.getSkills().getLevel(Skills.SMITHING) >= 80) {
                    player.sendMessage("You use your smithing knowledge to attach the 2 shards together!");
                    player.getInventory().deleteItem(11712, 1);
                    player.getInventory().deleteItem(11714, 1);
                    player.getInventory().addItem(11692, 1);
                    player.getSkills().addXp(Skills.SMITHING, 100);
                } else {
                    player.sendMessage("You need a smithing level of 80 to do this!");
                }
                return;
            }
            // godsword shards 1 + 3
            if (itemUsed.getId() == 11710 && usedWith.getId() == 11714 || itemUsed.getId() == 11714 && usedWith.getId() == 11710) {
                if (player.getSkills().getLevel(Skills.SMITHING) >= 80) {
                    player.sendMessage("You use your smithing knowledge to attach the 2 shards together!");
                    player.getInventory().deleteItem(11710, 1);
                    player.getInventory().deleteItem(11714, 1);
                    player.getInventory().addItem(11688, 1);
                    player.getSkills().addXp(Skills.SMITHING, 100);
                } else {
                    player.sendMessage("You need a smithing level of 80 to do this!");
                }
                return;
            }
            // godsword blade+bandos hilt = godsword
            if (itemUsed.getId() == 11690 && usedWith.getId() == 11704 || itemUsed.getId() == 11704 && usedWith.getId() == 11690) {
                player.getInventory().deleteItem(11704, 1);
                player.getInventory().deleteItem(11690, 1);
                player.getInventory().addItem(11696, 1);
                return;
            } // godsword blade+saradomin hilt = godsword
            if (itemUsed.getId() == 11690 && usedWith.getId() == 11706 || itemUsed.getId() == 11706 && usedWith.getId() == 11690) {
                player.getInventory().deleteItem(11706, 1);
                player.getInventory().deleteItem(11690, 1);
                player.getInventory().addItem(11698, 1);
                return;
            } // godsword blade+zamorak hilt = godsword
            if (itemUsed.getId() == 11690 && usedWith.getId() == 11708 || itemUsed.getId() == 11708 && usedWith.getId() == 11690) {
                player.getInventory().deleteItem(11708, 1);
                player.getInventory().deleteItem(11690, 1);
                player.getInventory().addItem(11700, 1);
                return;
            } // godsword blade+armadyl hilt = godsword
            if (itemUsed.getId() == 11690 && usedWith.getId() == 11702 || itemUsed.getId() == 11702 && usedWith.getId() == 11690) {
                player.getInventory().deleteItem(11702, 1);
                player.getInventory().deleteItem(11690, 1);
                player.getInventory().addItem(11694, 1);
                return;
            }
            // reaper ornament
            if (itemUsed.getId() == 41956 && usedWith.getId() == 31872 || itemUsed.getId() == 31872 && usedWith.getId() == 41956) {
                player.getInventory().deleteItem(6585, 1);
                player.getInventory().deleteItem(19333, 1);
                player.getInventory().addItem(19335, 1);
                player.sendMessage("You upgrade your Reaper necklace with an ornament kit!");
                return;
            }
            // souls ornament
            if (itemUsed.getId() == 41961 && usedWith.getId() == 31875 || itemUsed.getId() == 31875 && usedWith.getId() == 41961) {
                player.getInventory().deleteItem(6585, 1);
                player.getInventory().deleteItem(19333, 1);
                player.getInventory().addItem(19335, 1);
                player.sendMessage("You upgrade your Amulet of souls with an ornament kit!");
                return;
            }
            // fury ornament
            if (itemUsed.getId() == 19333 && usedWith.getId() == 6585 || itemUsed.getId() == 6585 && usedWith.getId() == 19333) {
                player.getInventory().deleteItem(6585, 1);
                player.getInventory().deleteItem(19333, 1);
                player.getInventory().addItem(19335, 1);
                player.sendMessage("You upgrade your Amulet of fury with an ornament kit!");
                return;
            }
            if (itemUsed.getId() == 22448 && usedWith.getId() == 22498) {
                if (!player.getInventory().containsItem(22448, 3000) && (player.getInventory().containsItem(554, 15000))) {
                    player.sendMessage("<col=B00000>You need 3000 Polypore spore's to make a Polypore Staff!");
                    return;
                }
            }
            if (itemUsed.getId() == 22448 && usedWith.getId() == 22498) {
                if (!player.getInventory().containsItem(22448, 3000) && (!player.getInventory().containsItem(554, 15000))) {
                    player.sendMessage("<col=B00000>You need 3000 Polypore spore's and 15000 fire runes to make a Polypore Staff!");
                    return;
                }
            }
            if (itemUsed.getId() == 991 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 991) {
                if (Utils.random(10) != 5) {
                    if (Utils.random(3) != 1) {
                        player.getInventory().deleteItem(991, 1);
                        player.getInventory().addItem(989, 1);
                        player.sendMessage("You successfully clean off the mud from the key and get a crystal key.");
                        return;
                    }
                    player.getInventory().deleteItem(991, 1);
                    player.sendMessage("The key was too old and disintigrated during the cleaning.");
                    return;
                }
                player.getInventory().deleteItem(3188, 1);
                player.getInventory().deleteItem(991, 1);
                player.getInventory().addItem(989, 1);
                player.sendMessage("You successfully clean off the mud from the key and get a crystal key.");
                return;
            }
            // removing colors from items
            if (itemUsed.getId() == 24100 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 24100) {
                player.getInventory().deleteItem(24100, 1);
                player.getInventory().addItem(6889, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 24102 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 24102) {
                player.getInventory().deleteItem(24102, 1);
                player.getInventory().addItem(6889, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 24104 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 24104) {
                player.getInventory().deleteItem(24104, 1);
                player.getInventory().addItem(6889, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 24106 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 24106) {
                player.getInventory().deleteItem(24106, 1);
                player.getInventory().addItem(6889, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 22552 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 22552) {
                player.getInventory().deleteItem(22552, 1);
                player.getInventory().addItem(2577, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 22554 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 22554) {
                player.getInventory().deleteItem(22554, 1);
                player.getInventory().addItem(2577, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 22556 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 22556) {
                player.getInventory().deleteItem(22556, 1);
                player.getInventory().addItem(2577, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 22558 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 22558) {
                player.getInventory().deleteItem(22558, 1);
                player.getInventory().addItem(2577, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 24092 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 24092) {
                player.getInventory().deleteItem(24092, 1);
                player.getInventory().addItem(4675, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 24094 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 24094) {
                player.getInventory().deleteItem(24094, 1);
                player.getInventory().addItem(4675, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 24096 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 24096) {
                player.getInventory().deleteItem(24096, 1);
                player.getInventory().addItem(4675, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 24098 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 24098) {
                player.getInventory().deleteItem(24098, 1);
                player.getInventory().addItem(4675, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 20949 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 20949) {
                player.getInventory().deleteItem(20949, 1);
                player.getInventory().addItem(2581, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 20950 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 20950) {
                player.getInventory().deleteItem(20950, 1);
                player.getInventory().addItem(2581, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 20951 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 20951) {
                player.getInventory().deleteItem(20951, 1);
                player.getInventory().addItem(2581, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 22528 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 22528) {
                player.getInventory().deleteItem(22528, 1);
                player.getInventory().addItem(15492, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 22534 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 22534) {
                player.getInventory().deleteItem(22534, 1);
                player.getInventory().addItem(15492, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 22540 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 22540) {
                player.getInventory().deleteItem(22540, 1);
                player.getInventory().addItem(15492, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 22546 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 22546) {
                player.getInventory().deleteItem(22546, 1);
                player.getInventory().addItem(15492, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 15701 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 15701) {
                player.getInventory().deleteItem(15701, 1);
                player.getInventory().addItem(11235, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 15702 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 15702) {
                player.getInventory().deleteItem(15702, 1);
                player.getInventory().addItem(11235, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 15703 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 15703) {
                player.getInventory().deleteItem(15703, 1);
                player.getInventory().addItem(11235, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 15704 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 15704) {
                player.getInventory().deleteItem(15704, 1);
                player.getInventory().addItem(11235, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 15441 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 15441) {
                player.getInventory().deleteItem(15441, 1);
                player.getInventory().addItem(4151, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 15442 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 15442) {
                player.getInventory().deleteItem(15442, 1);
                player.getInventory().addItem(4151, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 15443 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 15443) {
                player.getInventory().deleteItem(15443, 1);
                player.getInventory().addItem(4151, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 15444 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 15444) {
                player.getInventory().deleteItem(15444, 1);
                player.getInventory().addItem(4151, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 22207 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 22207) {
                player.getInventory().deleteItem(22207, 1);
                player.getInventory().addItem(15486, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 22209 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 22209) {
                player.getInventory().deleteItem(22209, 1);
                player.getInventory().addItem(15486, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 22211 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 22211) {
                player.getInventory().deleteItem(22211, 1);
                player.getInventory().addItem(15486, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 22213 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 22213) {
                player.getInventory().deleteItem(22213, 1);
                player.getInventory().addItem(15486, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 21372 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 21372) {
                player.getInventory().deleteItem(21372, 1);
                player.getInventory().addItem(21371, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 21373 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 21373) {
                player.getInventory().deleteItem(21373, 1);
                player.getInventory().addItem(15486, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 21374 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 21374) {
                player.getInventory().deleteItem(21374, 1);
                player.getInventory().addItem(15486, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            if (itemUsed.getId() == 21375 && usedWith.getId() == 3188 || itemUsed.getId() == 3188 && usedWith.getId() == 21375) {
                player.getInventory().deleteItem(21375, 1);
                player.getInventory().addItem(15486, 1);
                player.sendMessage("You use your cleaning cloth to remove the coloring from your item!");
                return;
            }
            // FARMING PLANT POTS
            // TODO proper timers for these + fruit trees
            if (itemUsed.getId() == 5354 && usedWith.getId() == 5312 || itemUsed.getId() == 5312 && usedWith.getId() == 5354) { // acorn
                // -
                // oak
                if (player.getSkills().getLevel(Skills.FARMING) < 15) {
                    player.sendMessage("You need a Farming level of at least 15 in order to do this.");
                    return;
                }
                if (!player.getFarmingManager().checkWaterCan(player)) {
                    player.getSkills().addXp(Skills.FARMING, 1);
                    player.getInventory().deleteItem(itemUsed.getId(), 1);
                    player.getInventory().deleteItem(usedWith.getId(), 1);
                    player.getInventory().addItem(5370, 1);
                }
                return;
            }
            if (itemUsed.getId() == 5354 && usedWith.getId() == 5313 || itemUsed.getId() == 5313 && usedWith.getId() == 5354) { // willow
                if (player.getSkills().getLevel(Skills.FARMING) < 30) {
                    player.sendMessage("You need a Farming level of at least 30 in order to do this.");
                    return;
                }
                if (!player.getFarmingManager().checkWaterCan(player)) {
                    player.getSkills().addXp(Skills.FARMING, 1.1);
                    player.getInventory().deleteItem(itemUsed.getId(), 1);
                    player.getInventory().deleteItem(usedWith.getId(), 1);
                    player.getInventory().addItem(5371, 1);
                }
                return;
            }
            if (itemUsed.getId() == 5354 && usedWith.getId() == 5314 || itemUsed.getId() == 5314 && usedWith.getId() == 5354) { // maple
                if (player.getSkills().getLevel(Skills.FARMING) < 45) {
                    player.sendMessage("You need a Farming level of at least 45 in order to do this.");
                    return;
                }
                if (!player.getFarmingManager().checkWaterCan(player)) {
                    player.getSkills().addXp(Skills.FARMING, 1.2);
                    player.getInventory().deleteItem(itemUsed.getId(), 1);
                    player.getInventory().deleteItem(usedWith.getId(), 1);
                    player.getInventory().addItem(5372, 1);
                }
                return;
            }
            if (itemUsed.getId() == 5354 && usedWith.getId() == 5315 || itemUsed.getId() == 5315 && usedWith.getId() == 5354) { // yew
                if (player.getSkills().getLevel(Skills.FARMING) < 60) {
                    player.sendMessage("You need a Farming level of at least 60 in order to do this.");
                    return;
                }
                if (!player.getFarmingManager().checkWaterCan(player)) {
                    player.getSkills().addXp(Skills.FARMING, 1.3);
                    player.getInventory().deleteItem(itemUsed.getId(), 1);
                    player.getInventory().deleteItem(usedWith.getId(), 1);
                    player.getInventory().addItem(5373, 1);
                }
                return;
            }
            if (itemUsed.getId() == 5354 && usedWith.getId() == 5316 || itemUsed.getId() == 5316 && usedWith.getId() == 5354) { // magic
                if (player.getSkills().getLevel(Skills.FARMING) < 75) {
                    player.sendMessage("You need a Farming level of at least 75 in order to do this.");
                    return;
                }
                if (!player.getFarmingManager().checkWaterCan(player)) {
                    player.getSkills().addXp(Skills.FARMING, 1.4);
                    player.getInventory().deleteItem(itemUsed.getId(), 1);
                    player.getInventory().deleteItem(usedWith.getId(), 1);
                    player.getInventory().addItem(5374, 1);
                }
                return;
            }
            if (itemUsed.getId() == 5354 && usedWith.getId() == 5283 || itemUsed.getId() == 5283 && usedWith.getId() == 5354) { // apple
                if (player.getSkills().getLevel(Skills.FARMING) < 27) {
                    player.sendMessage("You need a Farming level of at least 27 in order to do this.");
                    return;
                }
                if (!player.getFarmingManager().checkWaterCan(player)) {
                    player.getSkills().addXp(Skills.FARMING, 1.4);
                    player.getInventory().deleteItem(itemUsed.getId(), 1);
                    player.getInventory().deleteItem(usedWith.getId(), 1);
                    player.getInventory().addItem(5496, 1);
                }
                return;
            }
            if (itemUsed.getId() == 5354 && usedWith.getId() == 5284 || itemUsed.getId() == 5284 && usedWith.getId() == 5354) { // banana
                if (player.getSkills().getLevel(Skills.FARMING) < 33) {
                    player.sendMessage("You need a Farming level of at least 33 in order to do this.");
                    return;
                }
                if (!player.getFarmingManager().checkWaterCan(player)) {
                    player.getSkills().addXp(Skills.FARMING, 1.4);
                    player.getInventory().deleteItem(itemUsed.getId(), 1);
                    player.getInventory().deleteItem(usedWith.getId(), 1);
                    player.getInventory().addItem(5497, 1);
                }
                return;
            }
            if (itemUsed.getId() == 5354 && usedWith.getId() == 5285 || itemUsed.getId() == 5285 && usedWith.getId() == 5354) { // orange
                if (player.getSkills().getLevel(Skills.FARMING) < 39) {
                    player.sendMessage("You need a Farming level of at least 39 in order to do this.");
                    return;
                }
                if (!player.getFarmingManager().checkWaterCan(player)) {
                    player.getSkills().addXp(Skills.FARMING, 1.4);
                    player.getInventory().deleteItem(itemUsed.getId(), 1);
                    player.getInventory().deleteItem(usedWith.getId(), 1);
                    player.getInventory().addItem(5498, 1);
                }
                return;
            }
            if (itemUsed.getId() == 5354 && usedWith.getId() == 5286 || itemUsed.getId() == 5286 && usedWith.getId() == 5354) { // curry
                if (player.getSkills().getLevel(Skills.FARMING) < 42) {
                    player.sendMessage("You need a Farming level of at least 42 in order to do this.");
                    return;
                }
                if (!player.getFarmingManager().checkWaterCan(player)) {
                    player.getSkills().addXp(Skills.FARMING, 1.4);
                    player.getInventory().deleteItem(itemUsed.getId(), 1);
                    player.getInventory().deleteItem(usedWith.getId(), 1);
                    player.getInventory().addItem(5499, 1);
                }
                return;
            }
            if (itemUsed.getId() == 5354 && usedWith.getId() == 5287 || itemUsed.getId() == 5287 && usedWith.getId() == 5354) { // pineapple
                if (player.getSkills().getLevel(Skills.FARMING) < 51) {
                    player.sendMessage("You need a Farming level of at least 51 in order to do this.");
                    return;
                }
                if (!player.getFarmingManager().checkWaterCan(player)) {
                    player.getSkills().addXp(Skills.FARMING, 1.4);
                    player.getInventory().deleteItem(itemUsed.getId(), 1);
                    player.getInventory().deleteItem(usedWith.getId(), 1);
                    player.getInventory().addItem(5500, 1);
                }
                return;
            }
            if (itemUsed.getId() == 5354 && usedWith.getId() == 5290 || itemUsed.getId() == 5290 && usedWith.getId() == 5354) { // papaya
                if (player.getSkills().getLevel(Skills.FARMING) < 72) {
                    player.sendMessage("You need a Farming level of at least 57 in order to do this.");
                    return;
                }
                if (!player.getFarmingManager().checkWaterCan(player)) {
                    player.getSkills().addXp(Skills.FARMING, 1.4);
                    player.getInventory().deleteItem(itemUsed.getId(), 1);
                    player.getInventory().deleteItem(usedWith.getId(), 1);
                    player.getInventory().addItem(5503, 1);
                }
                return;
            }
            if (itemUsed.getId() == 5354 && usedWith.getId() == 5288 || itemUsed.getId() == 5288 && usedWith.getId() == 5354) { // papaya
                if (player.getSkills().getLevel(Skills.FARMING) < 57) {
                    player.sendMessage("You need a Farming level of at least 57 in order to do this.");
                    return;
                }
                if (!player.getFarmingManager().checkWaterCan(player)) {
                    player.getSkills().addXp(Skills.FARMING, 1.4);
                    player.getInventory().deleteItem(itemUsed.getId(), 1);
                    player.getInventory().deleteItem(usedWith.getId(), 1);
                    player.getInventory().addItem(5501, 1);
                }
                return;
            }
            if (itemUsed.getId() == 5354 && usedWith.getId() == 5289 || itemUsed.getId() == 5289 && usedWith.getId() == 5354) { // palm
                if (player.getSkills().getLevel(Skills.FARMING) < 68) {
                    player.sendMessage("You need a Farming level of at least 68 in order to do this.");
                    return;
                }
                if (!player.getFarmingManager().checkWaterCan(player)) {
                    player.getSkills().addXp(Skills.FARMING, 1.4);
                    player.getInventory().deleteItem(itemUsed.getId(), 1);
                    player.getInventory().deleteItem(usedWith.getId(), 1);
                    player.getInventory().addItem(5502, 1);
                }
                return;
            }
//            if (FlaskDecanting.mixPot(player, fromItem, toItem, fromSlot, toSlot)) {
//                return;
//            }
//            if (PotionDecanting.mixPot(player, fromItem, toItem, fromSlot, toSlot)) {
//                return;
//            }

            if (Firemaking.isFiremaking(player, itemUsedWithId, Firelighter.values.get(itemUsedId))) {
                return;
            } else if (Firemaking.isFiremaking(player, itemUsedId, Firelighter.values.get(itemUsedWithId))) {
                return;
            }
            if (PolyporeDungeon.handleItemOnItem(player, itemUsed, usedWith)) {
                return;
            }
            if (itemUsed.getId() > 0 && usedWith.getId() > 0) {
                final int herbloreVersa = Herblore.isHerbloreSkill(usedWith, itemUsed);
                if (herbloreVersa > -1) {
                    if (HerbloreRs3Dialogue.sendHerbloreInterface(player, itemUsed, usedWith, false))
                        return;
                    player.getDialogueManager().startDialogue("HerbloreD", herbloreVersa, itemUsed, usedWith, false);
                    return;
                }
            }
            if (itemUsed.getId() == LeatherCrafting.NEEDLE.getId() || usedWith.getId() == LeatherCrafting.NEEDLE.getId()) {
                if (LeatherCrafting.handleItemOnItem(player, itemUsed, usedWith)) {
                    return;
                }
            }
            if (Firemaking.isFiremaking(player, itemUsed, usedWith)) {
                return;
            } else if (contains(1755, Gem.OPAL.getUncut(), itemUsed, usedWith)) {
                GemCutting.cut(player, Gem.OPAL);
            } else if (contains(1755, Gem.JADE.getUncut(), itemUsed, usedWith)) {
                GemCutting.cut(player, Gem.JADE);
            } else if (contains(1755, Gem.RED_TOPAZ.getUncut(), itemUsed, usedWith)) {
                GemCutting.cut(player, Gem.RED_TOPAZ);
            } else if (contains(1755, Gem.SAPPHIRE.getUncut(), itemUsed, usedWith)) {
                GemCutting.cut(player, Gem.SAPPHIRE);
            } else if (contains(1755, Gem.EMERALD.getUncut(), itemUsed, usedWith)) {
                GemCutting.cut(player, Gem.EMERALD);
            } else if (contains(1755, Gem.RUBY.getUncut(), itemUsed, usedWith)) {
                GemCutting.cut(player, Gem.RUBY);
            } else if (contains(1755, Gem.DIAMOND.getUncut(), itemUsed, usedWith)) {
                GemCutting.cut(player, Gem.DIAMOND);
            } else if (contains(1755, Gem.DRAGONSTONE.getUncut(), itemUsed, usedWith)) {
                GemCutting.cut(player, Gem.DRAGONSTONE);
            } else if (contains(1755, Gem.ONYX.getUncut(), itemUsed, usedWith)) {
                GemCutting.cut(player, Gem.ONYX);
            } else if (contains(1755, Gem.HYDRIX.getUncut(), itemUsed, usedWith)) {
                GemCutting.cut(player, Gem.HYDRIX);
            } else if (contains(1755, BoltTips.OPAL.getGemId(), itemUsed, usedWith)) {
                BoltTipFletching.boltFletch(player, BoltTips.OPAL);
            } else if (contains(1755, BoltTips.JADE.getGemId(), itemUsed, usedWith)) {
                BoltTipFletching.boltFletch(player, BoltTips.JADE);
            } else if (contains(1755, BoltTips.RED_TOPAZ.getGemId(), itemUsed, usedWith)) {
                BoltTipFletching.boltFletch(player, BoltTips.RED_TOPAZ);
            } else if (contains(1755, BoltTips.SAPPHIRE.getGemId(), itemUsed, usedWith)) {
                BoltTipFletching.boltFletch(player, BoltTips.SAPPHIRE);
            } else if (contains(1755, BoltTips.EMERALD.getGemId(), itemUsed, usedWith)) {
                BoltTipFletching.boltFletch(player, BoltTips.EMERALD);
            } else if (contains(1755, BoltTips.RUBY.getGemId(), itemUsed, usedWith)) {
                BoltTipFletching.boltFletch(player, BoltTips.RUBY);
            } else if (contains(1755, BoltTips.DIAMOND.getGemId(), itemUsed, usedWith)) {
                BoltTipFletching.boltFletch(player, BoltTips.DIAMOND);
            } else if (contains(1755, BoltTips.DRAGONSTONE.getGemId(), itemUsed, usedWith)) {
                BoltTipFletching.boltFletch(player, BoltTips.DRAGONSTONE);
            } else if (contains(1755, BoltTips.ONYX.getGemId(), itemUsed, usedWith)) {
                BoltTipFletching.boltFletch(player, BoltTips.ONYX);
            } else if (contains(1755, BoltTips.HYDRIX.getGemId(), itemUsed, usedWith)) {
                BoltTipFletching.boltFletch(player, BoltTips.HYDRIX);
            } else if (Settings.DEBUG) {
                Logger.getGlobal().info("Used:" + itemUsed.getId() + ", With:" + usedWith.getId());
            }
        }
    }

    public static void handleItemOnNPC(final Player player, final NPC npc, final Item item) {
        if (npc.getId() == 25587) {
            player.setRouteEvent(new RouteEvent(npc.transform(4, 9, 0), () -> {
                player.faceEntity(npc);
                if (!player.getControlerManager().processItemOnNPC(npc, item))
                    return;
                player.getPackets().sendGameMessage("Nothing interesting happens.");
            }));
            return;
        }
        player.setRouteEvent(new RouteEvent(npc, () -> {
            if (!player.getInventory().containsItem(item)) {
                return;
            }

            if (npc == null || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId())) {
                return;
            }
            if (item.getId() == 29749) {
                if (player.getRights() > 0) {
                    player.sendMessage("Killed " + npc.getName() + "</col> at tile " + npc.getLastWorldTile() + ".");
                    npc.sendDeath(null);
                } else {
                    player.getInventory().deleteItem(29749, Integer.MAX_VALUE);
                }
                return;
            }
            if (npc.getId() == 13727 && item.getId() == 41407) {
                player.getDialogueManager().startDialogue("XuanImbuedGearReplaceD");
                return;
            }
            if (npc.getId() == 22889 && item.getId() >= 37619 && item.getId() <= 37621) {
                player.getDialogueManager().startDialogue("Soothsayer", npc.getId(), item);
                return;
            }
            if (npc.getId() == 17143 && (item.getId() == 28437 || item.getId() == 28441)) {
                if (item.getAttributes() != null) {
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "I can only replace tradeable non-dyed version of the ascension bows");
                    return;
                }
                boolean mainHand = item.getId() == 28437;
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Replace " + item.getName() + " to " + (!mainHand ? "mainhand" : "off-hand") + " version", "Nevermind.");
                    }

                    @Override
                    public void run(int interfaceId, int componentId) {
                        end();
                        if (componentId == OPTION_1) {
                            item.setId(mainHand ? 28441 : 28437);
                            player.getInventory().refresh();
                            player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "There you go.");
                        }
                    }

                    @Override
                    public void finish() {
                    }
                });
                return;
            }
            if (SlayerHelmetUpgradeD.upgrade(player, item, npc)) {
                return;
            }
            if (VoidUpgradeD.upgrade(player, npc, item)) {
                return;
            }
            if (VoidSwitchD.switchColour(player, npc, item)) {
                return;
            }
            if (DesertLizard.handleDeath(npc, player, item)) {
                return;
            }
            if (Fungi.handleDeath(npc, player, item)) {
                return;
            }
            if (Gargoyle.handleDeath(npc, player, item)) {
                return;
            }
            if (Lizard.handleDeath(npc, player, item)) {
                return;
            }
            if (Rockslug.handleDeath(npc, player, item)) {
                return;
            }
            if (PetPerkManager.handleItemOnNPC(player, npc, item)) {
                return;
            }
            if (npc instanceof Familiar) {
                if (player.getFamiliar() != npc) {
                    player.getPackets().sendGameMessage("That isn't your familiar.", true);
                    return;
                }
                ((Familiar) npc).addScrolls(item);
                return;
            }
            if (npc instanceof EvilTreeHunterNPC) {
                int itemId = item.getId();
                ItemDefinitions itemDef = ItemDefinitions.getItemDefinitions(itemId);
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
            if (npc.getId() == 9712) {
                if (item.getId() >= 38521 && item.getId() <= 38545) {
                    player.getDialogueManager().startDialogue("ExchangeTrailblazerDialogue", item);
                    return;
                }
            }

            if (npc.getId() == 659 && item.getId() == 41418) {
                player.getDialogueManager().startDialogue("ExchangeVoteTokens", item.getAmount());
                return;
            }

            if (npc.getId() >= 22433 && npc.getId() <= 22436 && item.getId() >= 37100 && item.getId() <= 37103) {
                player.getDialogueManager().startDialogue("SealExchangingD", item.getId(), npc.getId());
                return;
            }

            if (npc instanceof Barricade && item.getId() == 14644) {
                player.faceEntity(npc);
                player.getInventory().deleteItem(item);
                player.lock(1);
                player.setNextAnimation(new Animation(2644));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        World.sendGraphics(player, new Graphics(1028), new WorldTile(npc));
                        ((Barricade) npc).explode();
                    }
                });
                return;
            }
            if (npc.getId() == 519) {
                player.faceEntity(npc);
                npc.faceEntity(player);
                final int charges = player.getPerkManager().hasPerkActive(DonationPerk.GWD_SPECIALIST) ? 20 : 10;
                if (item.getId() == 20120) {
                    if (player.getFrozenKeyCharges() < charges) {
                        final int amount = 4000000 - (player.getSkills().getLevelForXp(Skills.SMITHING) * 40000);
                        if (player.hasMoney(amount)) {
                            player.takeMoney(amount);
                            player.setFrozenKeyCharges((byte) charges);
                            player.getDialogueManager().startDialogue("SimpleNPCMessage", 519, "I've recharged your Frozen key for " + Utils.getFormattedNumber(amount) + " coins.");
                            if (player.getPerkManager().hasPerkActive(DonationPerk.GWD_SPECIALIST)) {
                                player.sendMessage("Your Frozen key has been recharged with 20 charges thanks to GWD Specialist perk.", true);
                            }
                            return;
                        }
                        player.getDialogueManager().startDialogue("SimpleNPCMessage", 519, "You will need " + Utils.getFormattedNumber(amount) + " coins " + "to recharge your Frozen key.");
                        return;
                    }
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", 519, "Your Frozen key is already fully charged.");
                    return;
                }
                player.getChargesManagerNew().sendRechargeItemDialogue(item, 519);
                return;
            }
            if (npc.getId() == 17170) {
                AutoUnfPotion.checkToUnf(player, item.getId());
                return;
            }
            if (npc.getId() == 15753) {
                switch (item.getId()) {
                case 7928:
                    if (!player.easterTitle1) {
                        player.setNextAnimation(new Animation(11490));
                        player.easterTitle1 = true;
                        player.getInventory().deleteItem(item.getId(), 1);
                        player.sendMessage("You feed the easter egg to the easter bunny and in return, you receive an easter title");
                        npc.setNextForceTalk(new ForceTalk("YUM!!"));
                        player.setNextGraphics(new Graphics(4704));
                    } else {
                        player.sendMessage("You have already claimed this title.");
                    }
                    break;

                case 7929:
                    if (!player.easterTitle2) {
                        player.setNextAnimation(new Animation(11490));
                        player.easterTitle2 = true;
                        player.getInventory().deleteItem(item.getId(), 1);
                        player.sendMessage("You feed the easter egg to the easter bunny and in return, you receive an easter title");
                        npc.setNextForceTalk(new ForceTalk("YUM!!"));
                        player.setNextGraphics(new Graphics(4704));
                    } else {
                        player.sendMessage("You have already claimed this title.");
                    }
                    break;
                case 7930:
                    if (!player.easterTitle3) {
                        player.setNextAnimation(new Animation(11490));
                        player.easterTitle3 = true;
                        player.getInventory().deleteItem(item.getId(), 1);
                        player.sendMessage("You feed the easter egg to the easter bunny and in return, you receive an easter title");
                        npc.setNextForceTalk(new ForceTalk("YUM!!"));
                        player.setNextGraphics(new Graphics(4704));
                    } else {
                        player.sendMessage("You have already claimed this title.");
                    }
                    break;
                case 7931:
                    if (!player.easterTitle4) {
                        player.setNextAnimation(new Animation(11490));
                        player.easterTitle4 = true;
                        player.getInventory().deleteItem(item.getId(), 1);
                        player.sendMessage("You feed the easter egg to the easter bunny and in return, you receive an easter title");
                        npc.setNextForceTalk(new ForceTalk("YUM!!"));
                        player.setNextGraphics(new Graphics(4704));
                    } else {
                        player.sendMessage("You have already claimed this title.");
                    }
                    break;
                case 7932:
                    if (!player.easterTitle5) {
                        player.setNextAnimation(new Animation(11490));
                        player.easterTitle5 = true;
                        player.getInventory().deleteItem(item.getId(), 1);
                        player.sendMessage("You feed the easter egg to the easter bunny and in return, you receive an easter title");
                        npc.setNextForceTalk(new ForceTalk("YUM!!"));
                        player.setNextGraphics(new Graphics(4704));
                    } else {
                        player.sendMessage("You have already claimed this title.");
                    }
                    break;
                case 7933:
                    if (!player.easterTitle6) {
                        player.setNextAnimation(new Animation(11490));
                        player.easterTitle6 = true;
                        player.getInventory().deleteItem(item.getId(), 1);
                        player.sendMessage("You feed the easter egg to the easter bunny and in return, you receive an easter title");
                        npc.setNextForceTalk(new ForceTalk("YUM!!"));
                        player.setNextGraphics(new Graphics(4704));
                    } else {
                        player.sendMessage("You have already claimed this title.");
                    }
                    break;
                }

            }
            if (npc.getId() == 18891) {
                if (PortArmor.forId(item.getId()) == null) {
                    player.getDialogueManager().startDialogue("SimpleNPCMessage", npc.getId(), "This item does not have a superior version.");
                    return;
                }
                player.getDialogueManager().startDialogue("SuperiorExchangeD", npc.getId(), item);
                return;
            }
            if (npc.getId() == 5956 && item.getId() == 19711) {
                player.getDialogueManager().startDialogue(new Dialogue() {

                    @Override
                    public void start() {
                        sendItemDialogue(item.getId(), 1, "You trade in your non-working defender for a new one!");
                        stage = 0;
                    }

                    @Override
                    public void run(final int interfaceId, final int componentId) {
                        switch (stage) {
                        case 0:
                            player.getInventory().deleteItem(19711, 1);
                            player.getInventory().addItem(new Item(19712));
                            finish();
                        }

                    }

                    @Override
                    public void finish() {
                        player.getInterfaceManager().closeChatBoxInterface();
                    }

                });
            }
            if (npc.getDefinitions().name.contains("ool leprech")) {
                for (final int produceId : FarmingManager.produces) {
                    if (produceId == item.getId()) {
                        final int num = player.getInventory().getNumberOf(produceId);
                        player.getInventory().deleteItem(produceId, num);
                        player.getInventory().addItem(new Item(ItemDefinitions.getItemDefinitions(produceId).getCertId(), num));
                        player.sendMessage("The Tool Leprechaun has noted some products for you.");
                        return;
                    }
                }
            }
            if (npc.getDefinitions().name.contains("ool leprech")) {
                for (final int produceId : FarmingManager.cleanproduce) {
                    if (produceId == item.getId()) {
                        final int num = player.getInventory().getNumberOf(produceId);
                        player.getInventory().deleteItem(produceId, num);
                        player.getInventory().addItem(new Item(ItemDefinitions.getItemDefinitions(produceId).getCertId(), num));
                        player.sendMessage("The Tool Leprechaun has noted some products for you.");
                        return;
                    }
                }
                if (item.getId() == 1925) {
                    val amount = player.getInventory().getNumberOf(1925);
                    player.getInventory().deleteItem(1925, amount);
                    player.getInventory().addItem(1926, amount);
                    player.sendMessage("The Tool Leprechaun notes the bucket" + (amount > 1 ? "s" : "") + " for you.");
                    return;
                }
                player.sendMessage("The leprechaun can not note that item for you.");
            }
            if (npc instanceof Pet) {
                player.faceEntity(npc);
                player.getPetManager().eat(item.getId(), (Pet) npc);
                return;
            }
        }));
    }

    public static void handleItemOnPlayer(final Player player, final Player usedOn, final int itemId) {
        final long time = Utils.currentTimeMillis();
        if (usedOn == player) {
            return;
        }
        if (player.getLockDelay() >= time || player.getEmotesManager().getNextEmoteEnd() >= time) {
            return;
        }
        player.setRouteEvent(new RouteEvent(usedOn, () -> {
            player.faceEntity(usedOn);
            if (player.getControlerManager().getControler() instanceof DungeonController) {
                if (Foods.isConsumable(new Item(itemId, 1))) {
                    Foods.eat(player, usedOn, new Item(itemId, 1));
                }
                return;
            }
            if (usedOn.getInterfaceManager().containsScreenInter()) {
                player.sendMessage(usedOn.getDisplayName() + " is busy.");
                return;
            }
            if (player.getAttackedByDelay() + 10000 > Utils.currentTimeMillis()) {
                player.sendMessage("You can't do this during combat.");
                return;
            }
            if (usedOn.getAttackedByDelay() + 10000 > Utils.currentTimeMillis()) {
                player.sendMessage("You cannot send a request to a player in combat.");
                return;
            }
            Logger.getGlobal().info(itemId + " ID");
            switch (itemId) {
            case 41430:
                player.getDialogueManager().startDialogue("AtaraxiaDollarsTransferD", usedOn);
                break;
            case 37694:
            case 35998:
            case 25450:
                CoOpSkillingContractManager.sendRequest(player, usedOn);
                return;
            case 962:

                if (player.getInventory().getFreeSlots() < 3 || usedOn.getInventory().getFreeSlots() < 3) {
                    player.sendMessage((player.getInventory().getFreeSlots() < 3 ? "You do" : "The other player does") + " not have enough inventory space to open this cracker.");
                    return;
                }
                if (!player.getInventory().containsOneItem(962)) {
                    return;
                }
                if (player.getUsername().equalsIgnoreCase("youtube")) {
                    return;
                }
                player.getDialogueManager().startDialogue("ChristmasCrackerD", usedOn, itemId);
                break;

            case 20083:
                if (!player.getInventory().containsOneItem(20083)) {
                    return;
                }
                player.getInventory().deleteItem(20083, 1);
                player.getInventory().addItem(20084, 1);
                usedOn.faceEntity(player);
                player.setNextAnimation(new Animation(15153));
                usedOn.setNextAnimation(new Animation(15153));
                break;
            case 4155:
            case 13263:
            case 13281:
            case 13282:
            case 13283:
            case 13284:
            case 13285:
            case 13286:
            case 13287:
            case 13288:
                if (!player.canTrade(usedOn)) {
                    return;
                }
                if (player.getSkills().getLevel(18) < 35 || usedOn.getSkills().getLevel(18) < 35) {
                    if (usedOn.getSkills().getLevel(18) < 35) {
                        player.sendMessage(Colors.SALMON + "This player needs at least level 35 slayer to participate!");
                    }
                    if (player.getSkills().getLevel(18) < 35) {
                        player.sendMessage(Colors.SALMON + "You need at least level 35 slayer to do co-op slayer.");
                    }
                    return;
                }
                if (usedOn.hasGroup == true) {
                    player.sendMessage(Colors.RED + usedOn.getDisplayName() + " is already in a slayer group.");
                    return;
                }
                if (usedOn.hasHost == true) {
                    player.sendMessage(Colors.RED + usedOn.getDisplayName() + " is taking care of another invitation.");
                    return;
                }
                if (player.hasGroup == true) {
                    player.sendMessage(Colors.RED + "You are already in a slayer group.");
                    return;
                }
                if (player.hasInvited == true) {
                    player.sendMessage(Colors.RED + "You already have an invitation pending.");
                    return;
                }

                if (player.getTask() == null && usedOn.getTask() == null) {
                    player.sendMessage("<col=BF00C9>Sending slayer request to " + usedOn.getDisplayName() + "...<col>");
                    player.setSlayerInvite(usedOn.getUsername());
                    player.hasInvited = true;
                    usedOn.hasOngoingInvite = true;
                    usedOn.setSlayerHost(player.getUsername());
                    usedOn.coOpSlayer.sendInvite(usedOn);
                    return;
                }

                player.sendMessage("<col=C43140>You or your partner cannot be on a task in order to co-op.</col>");
                return;
            default:
                player.sendMessage("Nothing interesting happens.");
                break;
            }
        }));
    }

    private static WorldTile getSurgeTile(Player player, int start, int end, boolean increment) {
        return getSurgeTile(player, start, end, increment, null);
    }
    private static int finalRadius(NPC npc) {
        int size = npc.getSize();
        return blackHoleRadius + (size >= 5 ? 3 : size >= 3 ? 2 : 0);
    }
    private static final int blackHoleRadius = 5; // The default radius of the black hole's AoE damage, not including the center tile.
    private  WorldTile blackHoleTile;

    private static WorldTile getSurgeTile(Player player, int start, int end, boolean increment, Entity target) {
        byte[] dirs = Utils.getDirection(player.getDirection());
        WorldTile lastStep = null;
        for (int steps = start; increment ? steps < end : steps > end; steps += (increment ? 1 : -1)) {
            WorldTile step = new WorldTile(player.getX() + (dirs[0] * steps), player.getY() + (dirs[1] * steps), player.getPlane());
            if (target != null && Utils.colides(target.getX(), target.getY(), target.getSize(), step.getX(), step.getY(), player.getSize()) || !player.getControlerManager().addWalkStep(player.getX(), player.getY(), step.getX(), step.getY())
                    /* || !player.clipedProjectile(step, true) */
                    || !World.isTileFree(step.getPlane(), step.getX(), step.getY(), player.getSize()) || !World.canMoveNPC(step, player.getSize()))
                break;

            lastStep = step;
        }
        return lastStep;
    }
    private long codexCooldown;
    private static long BAR_CYCLE;

    private static boolean addInventoryCoinsToPouch(final Player player) {
        if (player.isCanPvp()) {
            player.sendMessage("You cannot access your money pouch within a player-vs-player zone.");
            return true;
        }
        int coins = player.getInventory().getItems().getNumberOf(995);
        if (coins <= 0) {
            return true;
        }
        player.getMoneyPouch().addMoney(coins, true);
        return true;
    }

    public static void handleItemOption1(final Player player, final int slotId, final int itemId, final Item item) {
        if (Settings.DEBUG) {
            Logger.getGlobal().info("Option 1; slotId: " + slotId + "; itemId: " + itemId + ".");
        }
        if (player.getLockDelay() >= Utils.currentTimeMillis() || player.getEmotesManager().getNextEmoteEnd() >= Utils.currentTimeMillis()) {
            return;
        }
        if (player.switchDropMode && !player.lockedShiftDrop) {
            if (player.getDialogueManager().hasDialogue())
                player.getDialogueManager().finishDialogue();
            handleItemOption7(player, slotId, itemId, item);
            return;
        }

        if (!player.getControlerManager().handleItemOption1(item, itemId, slotId)) {
            return;
        }
        if (Smithing.sendUnfinishedProjectDetails(player, item)) {
            player.sendMessage("Use it on an anvil to continue smithing, or on a forge to restore its heat.", true);
            return;
        }
        if (MiningGeode.open(player, itemId, 1) || OreBox.fill(player, itemId)) {
            return;
        }
        if (itemId == 995) {
            addInventoryCoinsToPouch(player);
            return;
        }
        if (EliteArmourCreation.isEliteArmourCreation(player, item))
            return;
        if (JunkRefiner.openJunkRefiner(player, item))
            return;
        if (ToyHorsey.playWith(player, itemId)) {
            return;
        }
        if (Marionette.doJump(player, itemId)) {
            return;
        }
        if (itemId == 48498) {
            ShopsDataParser.openShop(player, 184);
            return;
        }
        if (item.getId() == 48480) {
            TriskKeyBag.fill(player, item);
            return;
        }
        if (item.getId() == 41430) {
            player.getDialogueManager().startDialogue(new UziPerkShop());
            return;
        }
        if (item.getId() == 11950) {
            if (player.isUnderCombat()) {
                player.sendMessage("Now is not the best time for this.");
                return;
            }
            player.setNextAnimation(new Animation(7528));
            player.setNextGraphics(new Graphics(1284));
            player.lock(10);
            return;
        }
        if (itemId == Araxxor.ARAXXOR_EGG) {
            boolean hasBarry = PetPerkUtils.hasObtainedPetById(player, 33809) || player.hasItem(33809);
            boolean hasMallory = PetPerkUtils.hasObtainedPetById(player, 33810) || player.hasItem(33810);
            if (hasBarry) {
                player.getInventory().deleteItem(Araxxor.ARAXXOR_EGG);
                player.getInventory().addItem(Pets.MALLORY.getBabyItemId());
            } else {
                player.getInventory().deleteItem(Araxxor.ARAXXOR_EGG);
                player.getInventory().addItem(Pets.BARRY.getBabyItemId());
            }
            return;
        }
        if (itemId == 43050) { // Kuroryu
            if (player.getInventory().containsItem(43050, 1)) {
                player.getInventory().deleteItem(43050, 1);
                player.getInventory().addItem(43051, 1);
                player.getDialogueManager().startDialogue("SimpleMessage", "As you inspect the " + item.getName() + ", it magically transforms into a creature!");
                player.setNextGraphics(new Graphics(1935));
            }
            return;
        }
        if (itemId == 47926) {




            boolean surge = true;
            if (player.getInventory().containsItem(47926, 1)) {
                player.setNextGraphics(new Graphics(1935));
                player.getPerkManager().unlockPerk(DonationPerk.DOUBLE_SURGE);


            }
            final WorldTile tile = getSurgeTile(player, 0, 10, true);
            if (tile == null || tile.matches(player)) {
                player.getPackets().sendGameMessage("Destination unreachable.");
                return;
            }
            player.lock(2);
            player.setSurgeEscapeCooldown();
            player.setNextAnimation(new Animation(18358));
            player.setNextGraphics(new Graphics(3537, 5, 0));
            player.setNextForceMovement(new ForceMovement(player, 0, tile, 1, Utils.getAngleToForceMovement(Utils.getAngle(tile.getX() - player.getX(), tile.getY() - player.getY()))));
            player.setAttackingDelay(Utils.currentTimeMillis() + 4000);
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    player.getEffectsManager().removeEffect(EffectsManager.EffectType.DISMEMBER);
                    player.setNextWorldTile(tile);
                }
            }, 1);
            return;

        }
        if (itemId == 35152) {

                final long timeVariation = Utils.currentTimeMillis() - player.lastGatherLimit;
            if (timeVariation < (1 * 60 * 60 * 40)) { // 24 hours
                final long toWait = (1 * 60 * 60 * 40) - (Utils.currentTimeMillis() - player.lastGatherLimit);
                player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "Ability CoolDown :" + Utils.millisecsToMinutes(toWait) + " minutes", true);
                return;
            }
            player.gathered = 0;
            player.lastGatherLimit = Utils.currentTimeMillis();

            WorldTasksManager.schedule(new WorldTask() {
                int totalDamage = 0;
                int count = 0;
                int codexCooldown = 0;


                @Override
                public void run() {
                    for (NPC npc : World.getNPCs()) {
                        if (npc != null) {
                            if (npc.getTarget() == null) {
                                npc.setTarget(player);
                            }
                            if (totalDamage >= 7500) {

                                return;
                            }


                            if (npc.getCombat().checkAll() && npc.withinDistance(player, finalRadius(npc))) {
                                int aoeDamage = Utils.random(25, npc.getCombatLevel() >= 300 ? 300 : 80);
                                totalDamage = totalDamage + aoeDamage;
                                npc.applyHit(new Hit(player, aoeDamage, HitLook.MAGIC_DAMAGE));
                            }
                        }
                    }
                    if (totalDamage > 0) {
                        player.sendMessage(Colors.DCYAN + "Total Corruption Blast Ability :AoE damage:" + Utils.formatNumber(totalDamage) + ".", false); // Thought this might be fun to leave in so players can compete.
                    }
                    if (count++ == 8) {

                        stop();

                        return;
                    }


                }

            }, 0, 2);


        }
        if (itemId == 35151) {

            final long timeVariation = Utils.currentTimeMillis() - player.lastGatherLimit1;
            if (timeVariation < (1 * 60 * 60 * 40)) { // 24 hours
                final long toWait = (1 * 60 * 60 * 40) - (Utils.currentTimeMillis() - player.lastGatherLimit1);
                player.getPackets().sendPlayerMessage(1, 15263739, "<col=ffffff>" + "Ability CoolDown :" + Utils.millisecsToMinutes(toWait) + " minutes", true);
                return;
            }
            player.gathered = 0;
            player.lastGatherLimit1 = Utils.currentTimeMillis();

            WorldTasksManager.schedule(new WorldTask() {
                int totalDamage = 0;
                int count = 0;
                int codexCooldown = 0;


                @Override
                public void run() {
                    for (NPC npc : World.getNPCs()) {
                        if (npc != null) {
                            if (npc.getTarget() == null) {
                                npc.setTarget(player);
                            }
                            if (totalDamage >= 7500) {

                                return;
                            }


                            if (npc.getCombat().checkAll() && npc.withinDistance(player, finalRadius(npc))) {
                                int aoeDamage = Utils.random(25, npc.getCombatLevel() >= 300 ? 300 : 80);
                                totalDamage = totalDamage + aoeDamage;
                                npc.applyHit(new Hit(player, aoeDamage, HitLook.RANGE_DAMAGE));
                            }
                        }
                    }
                    if (totalDamage > 0) {
                        player.sendMessage(Colors.DCYAN + "Total Corruption Blast Ability :AoE damage:" + Utils.formatNumber(totalDamage) + ".", false); // Thought this might be fun to leave in so players can compete.
                    }
                    if (count++ == 8) {

                        stop();

                        return;
                    }


                }

            }, 0, 2);


        }

        if (itemId == 42785) {

            if (player.getInventory().containsItem(42785, 1)) {
                player.clickToTeleport = !player.clickToTeleport;
                player.sendMessage("CODEX-ABILITY: " + (!player.clickToTeleport ? "Disabled" : "Enabled"));
            }
                if (player.clickToTeleport) {
                    WorldTasksManager.schedule(new WorldTask() {
                        int totalDamage = 0;
                        int count = 0;
                        int codexCooldown = 0;


                        @Override
                        public void run() {
                            for (NPC npc : World.getNPCs()) {
                                if (npc != null) {
                                    if (npc.getTarget() == null) {
                                        npc.setTarget(player);
                                    }
                                    if (totalDamage >= 7500) {

                                        return;
                                    }


                                    if (Utils.random(10) == 0);
                                    {
                                        if (npc.getCombat().checkAll() && npc.withinDistance(player, finalRadius(npc))) {
                                            int aoeDamage = Utils.random(13, npc.getCombatLevel() >= 300 ? 300 : 24);
                                            totalDamage = totalDamage + aoeDamage;
                                            npc.applyHit(new Hit(player, aoeDamage, HitLook.REGULAR_DAMAGE));
                                        }
                                    }
                                }
                            }
                            if (totalDamage > 0) {
                                player.sendMessage(Colors.DCYAN + "Total Corruption Blast Ability :AoE damage:" + Utils.formatNumber(totalDamage) + ".", false); // Thought this might be fun to leave in so players can compete.
                            }
                            if (count++ == 2) {

                                stop();

                                return;
                            }



                        }

                    }, 0, 1);
                    player.setNextGraphics(new Graphics(3537, 1, 0));
                        player.sendMessage(Colors.DCYAN + "When Enabled This Codex Gives You Damage Buff)" +
                                "!");
                        Settings.static_damage_buff = 2.25;// static damage modifier to all combat stats
                        return;

                }
                if (!player.clickToTeleport) {
                    player.sendMessage(Colors.DCYAN + "When Disabled This Codex Gives You Normal Damage)" +
                            "!");
                    Settings.static_damage_buff = 0.47;
                    player.setNextGraphics(new Graphics(1935));
                    return;
                }

            }





        if (itemId == 43393) {




            boolean surge = true;
            if (player.getInventory().containsItem(43393, 1)) {
                player.setNextGraphics(new Graphics(1937));
                player.getPerkManager().unlockPerk(DonationPerk.DOUBLE_BARGE);


            }
            WorldTile tile = getSurgeTile(player, 0, 10, true);
            if (tile == null || tile.matches(player)) {
                player.getPackets().sendGameMessage("Destination unreachable.");
                return;
            }
            Entity target = player.getCombatDefinitions().getCurrentTarget();
            if (target != null) {
                if (!player.withinDistance(target, 10)) {
                    player.getPackets().sendGameMessage("Too far from target to perform this ability!");
                    return;
                }
                PlayerCombat combat;
                if (player.getActionManager().getAction() instanceof PlayerCombat)
                    combat = (PlayerCombat) player.getActionManager().getAction();
                else {
                    combat = new PlayerCombat(target);
                    PlayerCombat.setWeaponAbilityDelay(player);
                    player.getActionManager().setAction(combat);
                }
                player.faceEntity(target);
                tile = getSurgeTile(player, 0, 10, true, target);
                if (tile == null) {
                    player.getPackets().sendGameMessage("Destination unreachable.");
                    return;
                }
            }
            player.setAttackingDelay(Utils.currentTimeMillis() + 4000);
            player.lock(2);
            player.setNextAnimation(new Animation(18147));
            player.setNextGraphics(new Graphics(3580));
            player.setNextForceMovement(new ForceMovement(player, 0, tile, 1, Utils.getAngleToForceMovement(player.getDirection())));
            final WorldTile lastTile = tile;
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    player.setNextWorldTile(lastTile);
                }
            }, 1);
            return;

        }
        if (itemId == 47929) {




            boolean surge = true;
            if (player.getInventory().containsItem(47929, 1)) {
                player.setNextGraphics(new Graphics(1936));
                player.getPerkManager().unlockPerk(DonationPerk.DOUBLE_ESCAPE);


            }
            final WorldTile tile = getSurgeTile(player, 0, -10, false);
            if (tile == null || tile.matches(player)) {
                player.getPackets().sendGameMessage("Destination unreachable.");
                return;
            }
            player.setNextAnimation(new Animation(18527));
            player.setAttackingDelay(Utils.currentTimeMillis() + 4000);
            player.setNextGraphics(new Graphics(3526, 0, 0));
            player.setNextForceMovement(new ForceMovement(player, 0, tile, 1, Utils.getAngleToForceMovement(Utils.getAngle(player.getX() - tile.getX(), player.getY() - tile.getY()))));
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    player.getEffectsManager().removeEffect(EffectsManager.EffectType.DISMEMBER);
                    player.setNextWorldTile(tile);
                }
            }, 1);
            return;

        }
        if (itemId == 33262) {
            player.getDialogueManager().startDialogue(ShardsBag.BagDialogue, 1);
            return;
        }
        if (item.getId() == 34026) {
            player.getDialogueManager().startDialogue(new PortableSkillingD());
            return;
        }
        if (item.getId() == 36719 || item.getId() == 36721 || item.getId() == 36723) {
            player.getInventionManager().openAddMaterialsInterface(itemId);
            return;
        }
        if (item.getId() == 36390) {
            player.getInventionManager().addDivineCharges(1);
            return;
        }
        if (item.getId() == ChristmasSeasonalEvent.LARGE_PRESENT_ITEM) {
            player.getInventory().deleteItem(item);
            PresentHandler.givePresentReward(player, true);
            return;
        }
        if (item.getId() == ChristmasSeasonalEvent.SMALL_PRESENT_ITEM) {
            ChristmasSeasonalEvent.inspectSmallPresent(player);
            return;
        }
        if (item.getId() == 44612) {
            Dialogue.sendSinglePlayerDialogue(player, Dialogue.SAD, "This must've been for someone on his naughty list...");
            return;
        }
        if (item.getDefinitions().getName().toLowerCase().contains("dungeoneering token box")) {
            int type = item.getId() - 36091;
            int dungLevel = player.getSkills().getLevelForXp(Skills.DUNGEONEERING);
            int amountToGive = type == 0 ? (int) (((4.00 / 9.00) * Math.pow(dungLevel, 2)) + (double) 50) : type == 1 ? (int) (((17.00 / 20.00) * Math.pow(dungLevel, 2)) + (double) 25) : (int) (((5.00 / 4.00) * Math.pow(dungLevel, 2)) + (double) 50);
            player.getInventory().deleteItem(item.getId(), 1);
            player.getDungeoneeringManager().addTokens(amountToGive);
            player.getDialogueManager().startDialogue("SimpleMessage", "You open the box and find " + Utils.formatNumber(amountToGive) + " dungeoneering tokens inside.");
            return;
        }
        if (item.getId() >= 27153 && item.getId() <= 27155) {
            int type = item.getId() - 27153;
            int totalLevel = player.getSkills().getTotalLevel();
            double x = (totalLevel >= 2268 ? (8000.00 + (((double) totalLevel / 27.00) - 84.00) * 133.00) : totalLevel >= 1404 ? (4000.00 + (((double) totalLevel / 27.00) - 52.00) * 125.00) : totalLevel >= 324 ? (1000.00 + (((double) totalLevel / 27.00) - 12.00) * 75.00) : (750.00 + (((double) totalLevel / 27.00) - 1.00) * 22.00));
            int amountToGive = (int) (ThreadLocalRandom.current().nextDouble(0.9, 1.1001) * Math.pow(10, type) * x) * 20;
            player.getInventory().deleteItem(item.getId(), 1);
            player.getInventory().addItem(995, amountToGive);
            player.getDialogueManager().startDialogue("SimpleMessage", "You open the bag and find " + Utils.formatNumber(amountToGive) + " coins inside.");
            return;
        }
        if (item.getId() == 3511) {
            int type = item.getId() - 3511;
            int totalLevel = player.getSkills().getTotalLevel();
            double x = (totalLevel >= 2268 ? (8000.00 + (((double) totalLevel / 27.00) - 84.00) * 133.00) : totalLevel >= 1404 ? (4000.00 + (((double) totalLevel / 27.00) - 52.00) * 125.00) : totalLevel >= 324 ? (1000.00 + (((double) totalLevel / 27.00) - 12.00) * 75.00) : (750.00 + (((double) totalLevel / 27.00) - 1.00) * 22.00));
            int amountToGive = (int) (ThreadLocalRandom.current().nextDouble(0.9, 2.2000) * Math.pow(10, type) * x) * 20;
            player.getInventory().deleteItem(item.getId(), 1);
            player.getInventory().addItem(995, amountToGive);
            player.getDialogueManager().startDialogue("SimpleMessage", "You open the castet and find " + Utils.formatNumber(amountToGive) + " coins inside.");
            return;
        }
        if (item.getId() == 41073) {
            player.sendInputInteger("How many empty divine charge containers would you like to store?", new InputIntegerEvent() {

                @Override
                public void run(Player player) {
                    int value = getInteger();
                    if (value <= 0)
                        return;
                    player.getInventionManager().addEmptyDivineCharges(value);
                }

            });
            return;
        }
        int id = item.getId();
        if (id == GIM.getRewards().getRewardBoxId()) {
            GIM.getRewards().openRewardBox(player);
            return;
        }
        if (id == GIM.getRewards().getRoyalLetterId()) {
            GIM.getRewards().openRoyalLetter(player);
            return;
        }
        if (id == 1842) {
            if (player.getInventory().containsItem(1842, 1)) {
                PetPerkUtils.redeemPet(player, Pets.CHEERLEADER);
            }
            return;
        }
        if (item.getId() == 9919) {
            player.getDialogueManager().startDialogue(new FoundRootD());
            return;
        }
        if (item.getId() == 39584) {
            player.getDialogueManager().startDialogue("PraesulCodex");
            return;
        }
        if (item.getId() == RootOfEvil.DIARY_ID) {
            RootOfEvil.DIARY.show(player);
            return;
        }
        if (item.getId() == 40650) {
            double chance = Math.random();
            if (player.getInventory().getFreeSlots() == 0 && !player.getInventory().containsItem(40651, 1)) {
                player.getPackets().sendGameMessage("You don't have enough inventory space.");
                return;
            }
            int amount = 25;
            if (chance > 0.05 && chance <= 0.25) {
                amount -= 5;
            } else if (chance <= 0.3) {
                amount -= 10;
            } else if (chance <= 0.4) {
                amount -= 15;
            } else {
                amount -= 20;
            }
            player.getInventory().deleteItem(40650, 1);
            player.getInventory().addItem(40651, amount);
            player.getDialogueManager().startDialogue("SimpleItemMessage", 40651, amount, "You found " + amount + " inside the Phylactery.");
            return;
        }
        if (item.getId() == 40651) {
            player.getDialogueManager().startDialogue("BlessingsCreateD");
            return;
        }
        if (item.getId() >= 40652 && item.getId() <= 40654) {
            player.getDialogueManager().startDialogue("KhopeshCraftingD");
            return;
        }
        if (item.getId() == QuickShafter.ID) {
            QuickShafter.execute(player);
            return;
        }
        if (item.getId() == 41078) {
            EvilTree.nurtureEntling(player);
            return;
        }
        if (itemId == 33870) {
            player.getDialogueManager().startDialogue(new Dialogue() {

                @Override
                public void start() {
                    sendDialogue("Releasing The Araxyte pheromone will reset your araxxor fight enrage. Are you sure you want to continue?");
                }

                @Override
                public void run(int interfaceId, int componentId) {
                    switch (stage) {
                    case -1:
                        stage = 0;
                        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Yes, I am sure", "No, nevermind");
                        break;
                    case 0:
                        end();
                        if (componentId == OPTION_1) {
                            player.getInventory().deleteItem(item);
                            player.setSpiderBossEnrage(0);
                            player.getPackets().sendGameMessage("<col=96ff00>The Araxytes are no longer enraged at you.");
                        }
                        break;
                    }
                }

                @Override
                public void finish() {

                }

            });
            return;
        }
        if (Foods.eat(player, item, slotId)) {
            return;
        }
        if (LootBeamManager.handleItemOption(player, itemId)) {
            return;
        }
        if (DivinationTransmute.isDivinationTransmute(player, item))
            return;
        if (PortableStation.isPortableItem(itemId)) {
            PortableStation.deploy(player, item);
            return;
        }

        if (Fletching.isFletching(player, itemId)) {
            return;
        }
        if (itemId >= 41471 && itemId <= 41479 && itemId != 41476) { // 41476 is a regular perk item
            PetPerkUtils.redeemPerkItem(player, itemId);
            return;
        }
        if (ItemDefinitions.getItemDefinitions(itemId).isRingOfKinship()) {
            player.openPartyInterface();
            return;
        }
        // if (DivinationTransmute.isDivinationTransmute(player, item))
        // return;
        if (item.getId() == CosmeticsHandler.KEEP_SAKE_KEY) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You currently have " + player.getEquipment().getKeepSakeItems().size() + " items in your keepsake box.");
            return;
        }
        switch (item.getId()) {
        case 37694:
        case 25450:
        case 26518:
        case 47592:
        case 26492:
            ButtonHandler.sendWear(player, slotId, itemId);
            return;
        case 20047:
            if (player.getInventory().deleteOneItem(new Item(20047, 1))) {
                player.addItem(19995, 50);
                player.sendMessage("You open the juju vial pack.");
            }
            return;
        }
        if (item.getId() >= 34023 && item.getId() <= 34025 || item.getId() == 36813) {
            player.getDialogueManager().startDialogue("ProteanPackOpenD", Utils.findMatching(ProteanPack.VALUES, pack -> pack.getId() == item.getId()));
            return;
        }
        if (item.getId() == 40986) {
            player.getDialogueManager().startDialogue("EvilTreeTeleportD");
            return;
        }
        if (item.getId() == 24778) {
            player.getDialogueManager().startDialogue("EvilSeedsD");
            return;
        }
        if (item.getId() == 6199) {
            MysteryBox.roll(player);
            return;
        }
        if (item.getId() == 36965) {
            player.getInventory().deleteItem(36965, 1);

            final int roll = Utils.random(1000);
            if (roll == 999) {
                player.getInventory().addItem(30412, 1);
                World.sendWorldMessage(Colors.RED + player.getDisplayName() + " has received a Black Santa Hat from the Golden Ticket!", false);
            } else if (roll == 998) {
                player.getInventory().addItem(962, 1);
                player.sendMessage(Colors.LIME + "Woah.. That's lucky.");
                World.sendWorldMessage(Colors.RED + player.getDisplayName() + " has received a Christmas Cracker from the Golden Ticket!", false);
            } else if (roll >= 700) {
                player.getInventory().addItem(6199, 5);
                player.sendMessage(Colors.LIME + "Not bad, almost there!");
            } else {
                player.getInventory().addItem(6199, 2);
                player.sendMessage(Colors.LIME + "Ooof. Better luck next time!");
                return;
            }
        }

        if (Donations.perkBoxes.contains(id)) {
            Donations.handlePerkBox(player, item.getId());
            return;
        }

        if (item.getId() >= 13713 && item.getId() <= 13717) {
            RewardBox.roll(player, item);
            return;
        }
        if (item.getId() == 13593) { // Skilling tome.
            player.getDialogueManager().startDialogue("SkillingTomeD");
            return;
        }
        if (item.getId() == 28904) { // Small prismatic lamp.
            if (player.getSkills().getBonusPrismaticXp() < 1_500_000_000) {
                double xp = player.getSkills().getPrismaticLampXp(0);
                player.sendMessage("You redeem the small prismatic lamp and receive " + Colors.RED + Utils.formatNumber((long) xp) + "</col> bonus XP.");
                player.getSkills().addBonusPrismaticXp(xp);
                player.getInventory().deleteItem(28904, 1);
            } else {
                player.sendMessage("You cannot have more than 1.5B prismatic bonus XP.");
            }
            return;
        }
        if (item.getId() == 28905) { // Medium prismatic lamp.
            if (player.getSkills().getBonusPrismaticXp() < 1_500_000_000) {
                double xp = player.getSkills().getPrismaticLampXp(1);
                player.sendMessage("You redeem the medium prismatic lamp and receive " + Colors.RED + Utils.formatNumber((long) xp) + "</col> bonus XP.");
                player.getSkills().addBonusPrismaticXp(xp);
                player.getInventory().deleteItem(28905, 1);
            } else {
                player.sendMessage("You cannot have more than 1.5B prismatic bonus XP.");
            }
            return;
        }
        if (item.getId() == 28906) { // Large prismatic lamp.
            if (player.getSkills().getBonusPrismaticXp() < 1_500_000_000) {
                double xp = player.getSkills().getPrismaticLampXp(2);
                player.sendMessage("You redeem the large prismatic lamp and receive " + Colors.RED + Utils.formatNumber((long) xp) + "</col> bonus XP.");
                player.getSkills().addBonusPrismaticXp(xp);
                player.getInventory().deleteItem(28906, 1);
            } else {
                player.sendMessage("You cannot have more than 1.5B prismatic bonus XP.");
            }
            return;
        }
        if (item.getId() == 28907) { // Huge prismatic lamp.
            if (player.getSkills().getBonusPrismaticXp() < 1_500_000_000) {
                double xp = player.getSkills().getPrismaticLampXp(3);
                player.sendMessage("You redeem the huge prismatic lamp and receive " + Colors.RED + Utils.formatNumber((long) xp) + "</col> bonus XP.");
                player.getSkills().addBonusPrismaticXp(xp);
                player.getInventory().deleteItem(28907, 1);
            } else {
                player.sendMessage("You cannot have more than 1.5B prismatic bonus XP.");
            }
            return;
        }
        if (item.getId() == 41451) { // Log supply box.
            player.getInventory().deleteItem(41451, 1);
            player.addItem(new Item(29557, 150));
            player.addItem(new Item(8836, 500));
            player.addItem(new Item(6334, 750));
            player.addItem(new Item(1514, 800));
            player.addItem(new Item(1516, 850));
            player.addItem(new Item(1518, 900));
            player.addItem(new Item(1520, 950));
            player.sendMessage("You open the log supply box.");
            if (ThreadLocalRandom.current().nextInt(256) == 0) {
                player.addItem(new Item(29557, 150));
                player.addItem(new Item(8836, 500));
                player.addItem(new Item(6334, 750));
                player.addItem(new Item(1514, 800));
                player.addItem(new Item(1516, 850));
                player.addItem(new Item(1518, 900));
                player.addItem(new Item(1520, 950));
                player.sendMessage(Colors.RED + "The box seems to have more logs than usual!");
            }
            return;
        }
        if (item.getId() == 41452) { // Gem supply box.
            player.sendMessage("You open the gem supply box.");
            player.getInventory().deleteItem(41452, 1);
            if (ThreadLocalRandom.current().nextInt(15_000) == 0) {
                player.addItem(new Item(31852)); // 1/15_000 chance, suck my dick
                player.sendMessage(Colors.RED + "You receive an ultra-rare incomplete hydrix!");
                World.sendWorldMessage(Colors.RED + "Skilling Contracts: " + player.getDisplayName() + " has received an ultra-rare incomplete hydrix from a gem supply box!", false);
            } else if (ThreadLocalRandom.current().nextInt(256) == 0) {
                player.sendMessage(Colors.RED + "You receive a very-rare uncut onyx (x3)!");
                player.addItem(new Item(6572, 3));
                World.sendWorldMessage(Colors.RED + "Skilling Contracts: " + player.getDisplayName() + " has received a very-rare uncut onyx (x3) from a gem supply box!", false);
            } else if (ThreadLocalRandom.current().nextBoolean()) {
                player.addItem(new Item(6572));
                player.sendMessage(Colors.RED + "You receive an onyx!");
            }
            player.addItem(new Item(1632, 250));
            player.addItem(new Item(1618, 250));
            player.addItem(new Item(1620, 250));
            player.addItem(new Item(1622, 250));
            player.addItem(new Item(1624, 250));
            player.addItem(new Item(1630, 250));
            player.addItem(new Item(1628, 250));
            player.addItem(new Item(1626, 250));
            return;
        }
        if (item.getId() == 41453) { // Ore supply box.
            player.getInventory().deleteItem(41453, 1);
            player.addItem(new Item(452, 100));
            player.addItem(new Item(450, 200));
            player.addItem(new Item(448, 300));
            player.addItem(new Item(445, 400));
            player.addItem(new Item(441, 500));
            player.addItem(new Item(454, 1000));
            player.sendMessage("You open the ore supply box.");
            if (ThreadLocalRandom.current().nextInt(256) == 0) {
                player.addItem(new Item(452, 100));
                player.addItem(new Item(450, 200));
                player.addItem(new Item(448, 300));
                player.addItem(new Item(445, 400));
                player.addItem(new Item(441, 500));
                player.addItem(new Item(454, 1000));
                player.sendMessage(Colors.RED + "The box seems to have more ores than usual!");
            }
            return;
        }
        if (item.getId() == 41454) { // Bar supply box.
            player.getInventory().deleteItem(41454, 1);
            player.addItem(new Item(2364, 75));
            player.addItem(new Item(2362, 200));
            player.addItem(new Item(2360, 300));
            player.addItem(new Item(2358, 400));
            player.addItem(new Item(2354, 500));
            player.addItem(new Item(2352, 600));
            player.sendMessage("You open the bar supply box.");
            if (ThreadLocalRandom.current().nextInt(256) == 0) {
                player.addItem(new Item(2364, 75));
                player.addItem(new Item(2362, 200));
                player.addItem(new Item(2360, 300));
                player.addItem(new Item(2358, 400));
                player.addItem(new Item(2354, 500));
                player.addItem(new Item(2352, 600));
                player.sendMessage(Colors.RED + "The box seems to have more bars than usual!");
            }
            return;
        }
        if (item.getId() == 41455) { // Fish supply box.
            player.getInventory().deleteItem(41455, 1);
            player.addItem(new Item(15271, 200));
            player.addItem(new Item(384, 250));
            player.addItem(new Item(7945, 300));
            player.addItem(new Item(3143, 300));
            player.addItem(new Item(372, 450));
            player.addItem(new Item(378, 650));
            player.sendMessage("You open the fish supply barrel.");
            if (ThreadLocalRandom.current().nextInt(256) == 0) {
                player.addItem(new Item(15271, 200));
                player.addItem(new Item(384, 250));
                player.addItem(new Item(7945, 300));
                player.addItem(new Item(3143, 300));
                player.addItem(new Item(372, 450));
                player.addItem(new Item(378, 650));
                player.sendMessage(Colors.RED + "The barrel seems to have more fish than usual!");
            }
            return;
        }
        if (item.getId() == 37753) {
            ShopsDataParser.openShop(player, 1337);
            return;
        }

        if (itemId == 32092) {
            player.getDialogueManager().startDialogue(VixWaxDialogue.class.getSimpleName());
            return;
        }

        if (itemId == 38451 || itemId == 38453) {
            RunePouch.check(player, itemId == 38451);
            return;
        }

        if (itemId == 11159) {
            if (player.getInventory().getFreeSlots() < 7) {
                player.sendMessage("You do not have enough inventory space.");
                return;
            }
            player.getInventory().deleteItem(new Item(11159));
            player.getInventory().addItem(new Item(10150));
            player.getInventory().addItem(new Item(10010));
            player.getInventory().addItem(new Item(10006));
            player.getInventory().addItem(new Item(10031));
            player.getInventory().addItem(new Item(10029));
            player.getInventory().addItem(new Item(596));
            player.getInventory().addItem(new Item(10008));
        }

        for (final Book books : Book.values()) {
            if (item.getId() == books.getItemId()) {
                player.getDialogueManager().startDialogue("BookDialogue", books);
                return;
            }
        }

        if (itemId == 37008) {
            player.getDialogueManager().startDialogue("IngressionFragmentD");
            return;
        }

        if (Molanisk.useBell(player, item)) {
            return;
        }

        if (itemId == 12855) { // Handles the orb object.
            player.getInventory().deleteItem(12855, 1);
            player.setNextAnimation(new Animation(9012));
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    player.setNextAnimation(new Animation(9013));
                    player.setNextWorldTile(FistOfGuthix.FOG_CENTER);
                    stop();
                }
            }, 1);
            return;
        }

        if (player.getGemBag().fill(item.getId())) {
            return;
        }

        if (item.getId() >= 30912 && item.getId() <= 30914) {
            if (player.getInventory().containsItem(30912, 1) && player.getInventory().containsItem(30913, 1) && player.getInventory().containsItem(30914, 1)) {
                player.getInventory().deleteItem(30912, 1);
                player.getInventory().deleteItem(30913, 1);
                player.getInventory().deleteItem(30914, 1);
                player.getInventory().addItem(new Item(30920, 1));
                player.sendMessage("You craft the Silverhawk boot soles, uppers and tongues into a pair of Silverhawk boots.");
            } else {
                player.sendMessage("You need some Silverhawk boot soles, uppers and tongues to craft a pair of Silverhawk boots.");
            }
            return;
        }

        for (final Chronicles c : Chronicles.values()) {
            if (c.getItemId() == item.getId()) {
                player.getDialogueManager().startDialogue("DungeoneeringChroniclesD", c);
                return;
            }
        }

        if (item.getId() == 12644) {
            player.sendMessage("You can't eat this easter egg.");
            return;
        }

        if (item.getId() == 30915) {
            boolean hasBoots = false;
            for (int i = 0; i < 27; i++) {
                if (player.getInventory().getItem(i) == null) {
                    continue;
                }
                if (player.getInventory().getItem(i).getId() >= 30920 && player.getInventory().getItem(i).getId() <= 30924) {
                    final Item boots = player.getInventory().getItem(i);
                    if (boots.getCharges() < 500) {
                        hasBoots = true;
                        if (boots.getCharges() + item.getAmount() <= 500) {
                            player.sendMessage("You charged your Silverhawk boots with " + (boots.getCharges() + item.getAmount()) + " Silverhawk feathers.");
                            player.getInventory().deleteItem(item);
                            boots.setCharges(boots.getCharges() + item.getAmount());
                            break;
                        } else {
                            player.sendMessage("You charged your Silverhawk boots with " + (500 - boots.getCharges()) + " Silverhawk feathers.");
                            player.getInventory().deleteItem(item.getId(), 500 - boots.getCharges());
                            boots.setCharges(500);
                        }
                    }
                }
            }
            if (!hasBoots) {
                player.sendMessage("You have no boots to which you could add charges.");
            }
            return;
        }


        if (itemId == 4079) {
            player.setNextAnimationNoPriority(new Animation(1457));
            return;
        }
        if (item.getId() == 32337) {
            player.getActionManager().setAction(new ProteanHunter(1));
            return;

        }

        if (item.getId() == 33740) {
            player.getDialogueManager().startDialogue(ProteanCraftingD.class.getSimpleName(), 33740, player.getInventory().getAmountOf(33740) > 60 ? 60 : player.getInventory().getAmountOf(33740), false);
            return;
        }

        if (item.getId() == 32843) {
            if (HerbloreRs3Dialogue.sendCombinationPotionInterface(player))
                return;
            player.getDialogueManager().startDialogue("CrystalFlaskD");
            return;
        }

        if (item.getId() == 34528) {
            player.getDialogueManager().startDialogue("ProteanFletchingD", player.getInventory().getAmountOf(34528) > 60 ? 60 : player.getInventory().getAmountOf(34528), false);
            return;
        }

        if (item.getId() == 19675) {
            Herbicide.openHerbicide(player);
            return;
        }

        for (final AnimaCoreData data : AnimaCoreData.values()) {
            if (item.getId() == data.getMaterial()[0]) {
                player.getDialogueManager().startDialogue("AnimaCoreCreationD");
                return;
            }
        }

        if (item.getId() >= 37030 && item.getId() <= 37033) {
            if (item.getId() == 37030 && player.getHeart().getReputation(HeartOfGielinor.ZAROS) < 2000) {
                player.sendMessage("You need at least 2000 Zarosian reputation to create refined armour.");
                return;
            } else if (item.getId() == 37031 && player.getHeart().getReputation(HeartOfGielinor.SLISKE) < 2000) {
                player.sendMessage("You need at least 2000 Sliskean reputation to create refined armour.");
                return;
            } else if (item.getId() == 37032 && player.getHeart().getReputation(HeartOfGielinor.ZAMORAK) < 2000) {
                player.sendMessage("You need at least 2000 Zamorakian reputation to create refined armour.");
                return;
            } else if (item.getId() == 37033 && player.getHeart().getReputation(HeartOfGielinor.SEREN) < 2000) {
                player.sendMessage("You need at least 2000 Serenic reputation to create refined armour.");
                return;
            }
            player.getDialogueManager().startDialogue("RefinedAnimaCoreCreationD");
            return;
        }

        if (item.getId() == 18337) {
            Bonecrusher.openBonecrusher(player);
            return;
        }

        if (item.getId() == 41384) {
            player.getPerkManager().unlockPerk(DonationPerk.THE_BOXER);
            player.handleDonation(20, "The Boxer");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Boxer</col>]. " + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(41384, 1);
            return;
        }
        if (item.getId() == 41385) {
            player.getPerkManager().unlockPerk(DonationPerk.AUBURY__S_APPRENTICE);
            player.handleDonation(10, "Aubury's Apprentice");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Aubury's Apprentice</col>]. " + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(41385, 1);
            return;
        }
        if (item.getId() == 41386) {
            player.getPerkManager().unlockPerk(DonationPerk.SOUL_SIPHONER);
            player.handleDonation(15, "Soul Siphoner");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Soul Siphoner</col>]. " + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(41386, 1);
            return;
        }
        if (item.getId() == 41387) {
            player.getPerkManager().unlockPerk(DonationPerk.FAVORED_FAMILIARS);
            player.handleDonation(10, "Favored Familiars");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Favored Familiars</col>]. " + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(41387, 1);
            return;
        }
        if (item.getId() == 41389) {
            player.getPerkManager().unlockPerk(DonationPerk.THE_STARGAZER);
            player.handleDonation(7, "The Stargazer");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Stargazer</col>]. " + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(41389, 1);
            return;
        }
        if (item.getId() == 41390) {
            player.getPerkManager().unlockPerk(DonationPerk.ARCANE_ALCHEMIST);
            player.handleDonation(5, "Arcane Alchemist");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Arcane Alchemist</col>]. " + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(41390, 1);
            return;
        }
        if (item.getId() == 41388) {
            player.getPerkManager().unlockPerk(DonationPerk.THE_SKIPPER);
            player.handleDonation(15, "The Skipper");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Skipper</col>]. " + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(41388, 1);
            return;
        }
        if (item.getId() == 41391) {
            player.getPerkManager().unlockPerk(DonationPerk.DROP_CATCHER);
            player.handleDonation(20, "Drop Catcher");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Drop Catcher</col>]. " + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(41391, 1);
            return;
        }
        if (item.getId() == 41476) {
            player.getPerkManager().unlockPerk(DonationPerk.PET_TRAINER);
            player.handleDonation(10, "Pet Trainer");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet Trainer</col>]. " + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(41476, 1);
            return;
        }
        if (item.getId() == 48485) {
            player.getPerkManager().unlockPerk(DonationPerk.DUNGEON_ARCHITECT);
            player.handleDonation(8, "Dungeon Architect");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Dungeon Architect</col>]. " + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(48485, 1);
            return;
        }
        if (item.getId() == 41431) {
            player.getTreasureHunter().giveBoughtKeys(5);
            player.handleDonation(2, "x5 Treasure hunter Keys");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "x5 Treasure hunter Keys</col>]. " + "Open the Squeal of Fortune tab to use them.");
            player.getInventory().deleteItem(41431, 1);
            return;
        }
        if (item.getId() == 41432) {
            player.getTreasureHunter().giveBoughtKeys(27);
            player.handleDonation(10, "x25 Treasure hunter Keys");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "x27 Treasure hunter Keys</col>]. " + "Open the Squeal of Fortune tab to use them.");
            player.getInventory().deleteItem(41432, 1);
            return;
        }
        if (item.getId() == 41433) {
            player.getTreasureHunter().giveBoughtKeys(55);
            player.handleDonation(20, "x50 Treasure hunter Keys");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "x55 Treasure hunter Keys</col>]. " + "Open the Squeal of Fortune tab to use them.");
            player.getInventory().deleteItem(41433, 1);
            return;
        }
        if (item.getId() == 41434) {
            player.getTreasureHunter().giveBoughtKeys(175);
            player.handleDonation(50, "x150 Treasure hunter Keys");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "x175 Treasure hunter Keys</col>]. " + "Open the Squeal of Fortune tab to use them.");
            player.getInventory().deleteItem(41434, 1);
            return;
        }
        if (item.getId() == 41435) {
            player.getTreasureHunter().giveBoughtKeys(350);
            player.handleDonation(100, "x300 Treasure hunter Keys");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "x350 Treasure hunter Keys</col>]. " + "Open the Squeal of Fortune tab to use them.");
            player.getInventory().deleteItem(41435, 1);
            return;
        }
        if (item.getId() == 48491) {
            player.setAtaraxiaCoins(+50);
            player.handleDonation(2, "50 Ataraxia coins");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "50 Ataraxia coins</col>]. " + "Open ;;cosmetics to use them");
            player.getInventory().deleteItem(48491, 1);
            return;
        }
        if (item.getId() == 48492) {
            player.setAtaraxiaCoins(+100);
            player.handleDonation(4, "100 Ataraxia coins");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "100 Ataraxia coins</col>]. " + "Open ;;cosmetics to use them");
            player.getInventory().deleteItem(48492, 1);
            return;
        }
        if (item.getId() == 48493) {
            player.setAtaraxiaCoins(+250);
            player.handleDonation(10, "250 Ataraxia coins");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "250 Ataraxia coins</col>]. " + "Open ;;cosmetics to use them");
            player.getInventory().deleteItem(48493, 1);
            return;
        }
        if (item.getId() == 48494) {
            player.setAtaraxiaCoins(+500);
            player.handleDonation(20, "500 Ataraxia coins");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "500 Ataraxia coins</col>]. " + "Open ;;cosmetics to use them");
            player.getInventory().deleteItem(48494, 1);
            return;
        }
        if (item.getId() == 48495) {
            player.setAtaraxiaCoins(+1000);
            player.handleDonation(35, "1000 Ataraxia coins");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "1000 Ataraxia coins</col>]. " + "Open ;;cosmetics to use them");
            player.getInventory().deleteItem(48495, 1);
            return;
        }
        if (item.getId() == 48496) {
            player.setAtaraxiaCoins(+2500);
            player.handleDonation(50, "2500 Ataraxia coins");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "2500 Ataraxia coins</col>]. " + "Open ;;cosmetics to use them");
            player.getInventory().deleteItem(48496, 1);
            return;
        }
        if (item.getId() == 48486) {
            player.addBank(false, new Bank());
            player.handleDonation(8, "+1 Bank");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "+1 bank container</col>]." + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(48486, 1);
            return;
        }
        if (item.getId() == 48487) {
            for (int i = 0; i < 3; i++)
                player.addBank(false, new Bank());
            player.handleDonation(20, "+3 Banks");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "+3 bank containers</col>]." + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(48487, 1);
            return;
        }
        if (item.getId() == 48488) {
            for (int i = 0; i < 8; i++)
                player.addBank(false, new Bank());
            player.handleDonation(50, "+7 Banks");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "+7 bank containers</col>]." + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(48488, 1);
            return;
        }
        if (item.getId() == 48490) {
            player.getInventory().addItem(new Item(41478, 3));
            player.getInventory().addItem(new Item(41477, 3));
            player.getInventory().addItem(new Item(41475, 3));
            player.getInventory().addItem(new Item(41474, 3));
            player.getInventory().addItem(new Item(41473, 3));
            player.getInventory().addItem(new Item(41471, 3));
            player.getInventory().addItem(new Item(30233, 1));
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Pet Perk Bundle</col>].");
            player.getInventory().deleteItem(48490, 1);
            return;
        }
        if (item.getId() == 41425) {
            player.getPerkManager().unlockPerk(DonationPerk.DOMINION_DOMINATION);
            player.handleDonation(15, "Dominion Domination");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Dominion Domination</col>]. " + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(41425, 1);
            return;
        }
        if (item.getId() == 41424) {
            player.getPerkManager().unlockPerk(DonationPerk.THE_EXTERMINATOR);
            player.handleDonation(7, "The Exterminator");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "The Exterminator</col>]. " + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(41424, 1);
            return;
        }
        if (item.getId() == 41423) {
            player.getPerkManager().unlockPerk(DonationPerk.SKILLING_ADDICT);
            player.handleDonation(7, "Skilling Addict");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Skilling Addict</col>]. " + "Type ::perks to see all your game perks.");
            player.getInventory().deleteItem(41423, 1);
            return;
        }
        if (item.getId() == 41426) {
            player.getAnimations().hasHeadMining = true;
            player.handleDonation(2, "Headbutt Mining");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Headbutt Mining</col>]. " + "Type ::cosmetics to enable the override!.");
            player.getInventory().deleteItem(41426, 1);
            return;
        }
        if (item.getId() == 41427) {
            player.getAnimations().hasChiMining = true;
            player.handleDonation(2, "Chi-Blast Mining");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Chi-Blast Mining</col>]. " + "Type ::cosmetics to enable the override!.");
            player.getInventory().deleteItem(41427, 1);
            return;
        }
        if (item.getId() == 41428) {
            player.getAnimations().hasBlastMining = true;
            player.handleDonation(2, "Blast Mining");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Blast Mining</col>]. " + "Type ::cosmetics to enable the override!.");
            player.getInventory().deleteItem(41428, 1);
            return;
        }
        if (item.getId() == 41429) {
            player.getAnimations().hasStrongMining = true;
            player.handleDonation(2, "Strongarm Mining");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Strongarm Mining</col>]. " + "Type ::cosmetics to enable the override!.");
            player.getInventory().deleteItem(41429, 1);
            return;
        }
        if (item.getId() == 41436) {
            player.getAnimations().hasPowerDivination = true;
            player.handleDonation(2, "Power Divination");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Power Divination</col>]. " + "Type ::cosmetics to enable the override!.");
            player.getInventory().deleteItem(41436, 1);
            return;
        }
        if (item.getId() == 41437) {
            player.getAnimations().hasPowerConversion = true;
            player.handleDonation(2, "Powerful Conversion");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Powerful Conversion </col>]. " + "Type ::cosmetics to enable the override!.");
            player.getInventory().deleteItem(41437, 1);
            return;
        }
        if (item.getId() == 41438) {
            player.getAnimations().agileConversion = true;
            player.handleDonation(2, "Agile Conversion");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Agile Conversion</col>]. " + "Type ::cosmetics to enable the override!.");
            player.getInventory().deleteItem(41438, 1);
            return;
        }
        if (item.getId() == 41439) {
            player.getAnimations().hasAgileDivination = true;
            player.handleDonation(2, "Agile Divination");
            player.sendMessage("You've redeemed: [" + Colors.GREEN + "Agile Divination</col>]. " + "Type ::cosmetics to enable the override!.");
            player.getInventory().deleteItem(41439, 1);
            return;
        }
        if (item.getId() >= 33936 && item.getId() <= 33962) {
            if (player.DFMScroll[(item.getId() - 33936) / 2]) {
                player.sendMessage("You've already read this scroll.");
                return;
            }
            player.getDialogueManager().startDialogue("DemonFlashScrollD", (item.getId() - 33936) / 2, item.getId());
            return;
        }
        if (item.getId() == 6983 || item.getId() == 6981) {
            player.getDialogueManager().startDialogue("GraniteSplittingD", item.getId());
            return;
        }

        if (item.getId() == 28547 || item.getId() == 28548 || item.getId() == 28549) {

            if (player.getInventory().containsItem(28547, 1) && player.getInventory().containsItem(28548, 1) && player.getInventory().containsItem(28549, 1)) {
                player.sendMessage("You assemble the completed triskelion.");

                for (int i = 28547; i <= 28549; i++) {
                    player.getInventory().deleteItem(i, 1);
                }

                player.getInventory().addItem(28550, 1);

            } else {
                player.sendMessage("You do not have all the necessary fragments to make a crystal triskelion key.");
            }

            return;
        }

        if (item.getId() == 6979) {
            player.sendMessage("This block of granite is too small to craft into anything.", true);
            return;
        }

        if (itemId == 18829 || itemId == 17489) {
            if (player.getControlerManager().canDropItem(item)) {
            }
            return;
        }

        // Expert skillscape shards bag
        if (itemId == 33262) { // essb option 1
            player.getDialogueManager().startDialogue(ShardsBag.BagDialogue, 0);
            return;
        }

        // Fill coal bag.
        if (itemId == 18339) {
            int coalAmount = player.getInventory().getAmountOf(453);
            if (coalAmount == 0) {
                player.sendMessage("You don't have any coal to add.");
                return;
            }
            if (player.getCoal() == Player.MAX_COAL) {
                player.sendMessage("Your coal bag is full!");
                return;
            }
            if (coalAmount + player.getCoal() > Player.MAX_COAL) {
                coalAmount = Player.MAX_COAL - player.getCoal();
            }
            player.getInventory().deleteInventoryItem(453, coalAmount);
            player.addCoal(coalAmount);
            player.sendMessage("You add " + coalAmount + " coal to your coal bag.");
            return;
        }

        // xmas stuff
        if (itemId == 744) {
            if (!player.getXmas().inXmas) {
                return;
            }
            if (Utils.currentTimeMillis() - player.getXmas().healTime >= 10000) {
                player.sendMessage("time diff: " + (Utils.currentTimeMillis() - player.getXmas().healTime));
                player.getXmas().healTime = Utils.currentTimeMillis();
                player.setNextAnimation(new Animation(9098));
                player.setNextGraphics(new Graphics(84));
                player.heal(150);
            } else {
                player.sendMessage(Colors.SALMON + "You cannot heal again yet");
            }
            return;
        }
        if (itemId == 33611) {
            final int[] rewards = { 22973, 15426, 22985, 34838 };
            player.getDialogueManager().startDialogue(new Dialogue() {

                @Override
                public void start() {
                    sendItemDialogue(33611, 1, "You peek inside the box and face a difficult choice..");
                    stage = 1;
                }

                @Override
                public void run(final int interfaceId, final int componentId) {
                    switch (stage) {
                    case 1:
                        sendOptionsDialogue("Pick a reward!", Colors.check(player.hasItem(new Item(rewards[0]))) + "Sparkles", Colors.check(player.hasItem(new Item(rewards[1]))) + "Candy cane", Colors.check(player.hasItem(new Item(rewards[2]))) + "Christmas wand", Colors.check(player.hasItem(new Item(rewards[3])) || player.hasItem(new Item(33625)) || player.hasItem(new Item(33627))) + "Prismatic dye", "Nevermind");
                        stage = 2;
                        break;
                    case 2:
                        final int item = getOrdinal(componentId);
                        switch (componentId) {
                        case OPTION_1:
                        case OPTION_2:
                        case OPTION_3:
                        case OPTION_4:
                            finish();
                            if (item == 3 && (player.hasItem(new Item(rewards[3])) || player.hasItem(new Item(33625)) || player.hasItem(new Item(33627))) || player.hasItem(new Item(rewards[item]))) {
                                player.sendMessage("You already have this reward. Don't waste your points!", false);
                            } else {
                                player.getInventory().deleteItem(33611, 1);
                                player.getInventory().addItem(rewards[item], 1);
                            }
                            break;
                        case OPTION_5:
                            finish();
                            break;
                        }
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
        if (itemId == 11036) {
            if (!player.getXmas().inXmas) {
                return;
            }

            if (player.getXmas().finishedRiddles()) { // finished all riddles
                player.sendMessage(Colors.GREEN + "You have finished all of the available riddles!", false);
                player.getInventory().deleteItem(new Item(11036));
                return;
            }

            if (player.getXmas().riddle == null && !player.getXmas().finishedRiddles()) {
                player.sendMessage(Colors.RED + "Please return to the Queen of Snow for another riddle!");
                return;
            }

            if (player.getXmas().riddle != null) {
                XmasRiddles.writeInterface(player);
            }
            return;
        }

        // Freezy pet
        if (itemId == 24512 || itemId >= 30746 && itemId <= 30748) {
            final ArrayList<String> options = new ArrayList<String>();
            final String[] freezy = { "Ice", "Desert", "Jungle", "Lava" };
            final int[] rewards = { 0, 0, 0 };
            int count = 0;
            final int index = itemId == 24512 ? 0 : itemId - 30745;
            for (int i = 0; i < freezy.length; i++) {
                if (i != index) {
                    options.add(freezy[i]);
                    rewards[count] = i == 0 ? 24512 : 30745 + i;
                    count++;
                }
            }
            player.getDialogueManager().startDialogue(new Dialogue() {

                @Override
                public void start() {
                    sendOptionsDialogue("What Freezy do you want?", options.get(0), options.get(1), options.get(2));
                    stage = 0;
                }

                @Override
                public void run(final int interfaceId, final int componentId) {
                    switch (stage) {
                    case 0:
                        finish();
                        final int index = getOrdinal(componentId);
                        if (componentId == OPTION_4) {
                            break;
                        }
                        switch (componentId) {
                        case OPTION_1:
                        case OPTION_2:
                        case OPTION_3:
                            player.getInventory().deleteItem(itemId, 1);
                            player.getInventory().addItem(rewards[index], 1);
                            player.sendMessage(Colors.GREEN + "You have swapped your Freezy out for the " + options.get(index) + " type!", true);
                            break;
                        }
                        break;
                    }
                }

                @Override
                public void finish() {
                    player.getInterfaceManager().closeChatBoxInterface();
                }

            });
        }

        if (itemId == 34027) {
            player.getDialogueManager().startDialogue(new Dialogue() {

                @Override
                public void start() {
                    sendItemDialogue(34027, player.getInventory().getAmountOf(34027), "Do you want to teleport to cosmetic store?");
                    stage = 0;
                }

                @Override
                public void run(final int interfaceId, final int componentId) {
                    switch (stage) {
                    case 0:
                        sendOptionsDialogue("Would you like to teleport?", "Yes", "No");
                        stage = 1;
                        break;
                    case 1:
                        finish();
                        switch (componentId) {
                        case OPTION_1:
                            Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(3808, 4517, 1));
                            break;
                        case OPTION_2:
                            break;
                        }
                    }

                }

                @Override
                public void finish() {
                    player.getInterfaceManager().closeChatBoxInterface();
                }

            });
        }

        if (itemId == 36156 || itemId == 36159 || itemId == 36163) {
            player.getDialogueManager().startDialogue(new Dialogue() {

                int upgrade;
                int component;
                int reward;
                final int[] defenders = { 0, 0, 0 };

                @Override
                public void start() {
                    upgrade = (int) parameters[0];
                    for (int i = 0; i <= 2; i++) {
                        defenders[i] = Defenders.getDef(player, upgrade)[i];
                    }
                    sendItemDialogue(upgrade, 1, "You look carefully over the " + new Item(upgrade).getName().toLowerCase() + ".");
                    stage = -1;
                    if (defenders[0] == 0 && defenders[1] == 0 && defenders[2] == 0) {
                        stage = 4;
                    }
                }

                @Override
                public void run(final int interfaceId, final int componentId) {
                    switch (stage) {
                    case -1:
                        sendOptionsDialogue("What type of offhand are you making?", "Melee / Defender", "Ranged / Repriser", "Magic / Rebounder");
                        stage = 0;
                        break;
                    case 0:
                        switch (componentId) {
                        case OPTION_1:
                        case OPTION_2:
                        case OPTION_3:
                            final int choice = componentId == OPTION_1 ? 0 : componentId == OPTION_2 ? 1 : 2;
                            boolean chosen = false;
                            reward = Defenders.getReward(defenders[choice], choice);
                            component = Defenders.getComp(defenders[choice], choice);
                            if (reward == 36176) {
                                for (final int ingredient : Defenders.taintedRepriserIngredients) {
                                    if (player.getInventory().containsItem(ingredient, 1) && !chosen) {
                                        component = ingredient;
                                        chosen = true;
                                    }
                                }
                                if (!chosen) {
                                    sendItemDialogue(36176, 1, "You need Karil's crossbow,  or his main/offhand pistol crossbow to create this");
                                    stage = 3;
                                } else {
                                    sendItemDialogue(36176, 1, "You decide to craft a Tainted repriser!");
                                    player.getInventory().deleteItem(component, 1);
                                    player.getInventory().deleteItem(36156, 1);
                                    player.getInventory().deleteItem(defenders[choice], 1);
                                    player.getInventory().addItem(36176, 1);
                                    player.getAchievements().updateProgress(1, AchievementList.OBTAIN_TAINTED_REPRISER, AchievementList.OBTAIN_BARROW_DEFENDERS);
                                    stage = 1;
                                }
                                break;
                            }
                            if (reward == 36153) {
                                for (final int upgrade : Defenders.barrowsC) {
                                    if (player.getInventory().containsItem(upgrade, 1) && !chosen) {
                                        component = upgrade;
                                        chosen = true;
                                    }
                                }
                                if (!chosen) {
                                    sendItemDialogue(36156, 1, "You need a Dharok's greataxe, Verac's flail," + "Guthan's warspear, or Torag's hammer to make this!");
                                    stage = 3;
                                } else {
                                    sendItemDialogue(36153, 1, "You decide to craft a Corrupted defender!");
                                    player.getInventory().deleteItem(component, 1);
                                    player.getInventory().deleteItem(36156, 1);
                                    player.getInventory().deleteItem(defenders[choice], 1);
                                    player.getInventory().addItem(36153, 1);
                                    player.getAchievements().updateProgress(1, AchievementList.OBTAIN_CORRUPTED_DEFENDER, AchievementList.OBTAIN_BARROW_DEFENDERS);
                                    stage = 1;
                                }
                                break;
                            }
                            if (reward == 36160) {
                                for (final int upgrade : Defenders.drygoresC) {
                                    if (player.getInventory().containsItem(upgrade, 1) && !chosen) {
                                        component = upgrade;
                                        chosen = true;
                                    }
                                }
                                if (!chosen) {
                                    sendItemDialogue(36160, 1, "You need a Drygore mace, Drygore longsword, or Drygore rapier to make this!");
                                    stage = 3;
                                } else {
                                    sendItemDialogue(36160, 1, "You decide to craft a Kalphite defender!");
                                    player.getInventory().deleteItem(component, 1);
                                    player.getInventory().deleteItem(36163, 1);
                                    player.getInventory().deleteItem(defenders[choice], 1);
                                    player.getInventory().addItem(36160, 1);
                                    player.getAchievements().updateProgress(1, AchievementList.OBTAIN_KALPHITE_DEFENDER);
                                    stage = 1;
                                }
                                break;
                            }
                            if (reward != 0 && component != 0 && !player.getInventory().containsItem(component, 1)) {
                                sendItemDialogue(component, 1, "You need a " + new Item(component).getName() + " to make this!");
                                stage = 3;
                            } else {
                                if (reward != 0 && player.getInventory().containsItem(new Item(defenders[choice]))) {
                                    sendItemDialogue(reward, 1, "You decide to make a " + new Item(reward).getName() + "!");
                                    if (player.getInventory().containsItem(component, 1) || (!player.getInventory().containsItem(component, 1) && component == 0)) {
                                        if (component != 0) {
                                            player.getInventory().deleteItem(component, 1);
                                        }
                                        player.getInventory().deleteItem(itemId, 1);
                                        player.getInventory().deleteItem(defenders[choice], 1);
                                        player.getInventory().addItem(reward, 1);
                                        if (reward == 36176)
                                            player.getAchievements().updateProgress(1, AchievementList.OBTAIN_TAINTED_REPRISER, AchievementList.OBTAIN_BARROW_DEFENDERS);
                                        if (reward == 36168)
                                            player.getAchievements().updateProgress(1, AchievementList.OBTAIN_BLIGHTED_REBOUNDER, AchievementList.OBTAIN_BARROW_DEFENDERS);
                                        if (reward == 36157)
                                            player.getAchievements().updateProgress(1, AchievementList.OBTAIN_ANCIENT_DEFENDER, AchievementList.OBTAIN_NEX_DEFENDERS);
                                        if (reward == 36179)
                                            player.getAchievements().updateProgress(1, AchievementList.OBTAIN_ANCIENT_REPRISER, AchievementList.OBTAIN_NEX_DEFENDERS);
                                        if (reward == 36171)
                                            player.getAchievements().updateProgress(1, AchievementList.OBTAIN_ANCIENT_LANTERN, AchievementList.OBTAIN_NEX_DEFENDERS);
                                        if (reward == 36160)
                                            player.getAchievements().updateProgress(1, AchievementList.OBTAIN_KALPHITE_DEFENDER);
                                        if (reward == 36181)
                                            player.getAchievements().updateProgress(1, AchievementList.OBTAIN_KALPHITE_REPRISER);
                                        if (reward == 36173)
                                            player.getAchievements().updateProgress(1, AchievementList.OBTAIN_KALPHITE_REBOUNDER);
                                    } else {
                                        player.sendMessage(Colors.RED + "You need " + Utils.getAorAn(new Item(component).getName()) + new Item(component).getName() + " to make this!");
                                    }
                                    stage = 1;
                                }
                                if (!player.getInventory().containsItem(new Item(defenders[choice]))) {
                                    reward = 0;
                                }
                            }
                            if (reward == 0) {
                                sendItemDialogue(itemId, 1, "You don't have the necessary defender / repriser / rebounder for this!");
                                stage = 3;
                            }
                            break;
                        }
                        break;
                    case 1:
                        finish();
                        break;
                    case 3:
                        finish();
                        break;
                    case 4:
                        finish();
                        player.sendMessage(Colors.RED + "It seems you are missing the neccessary defender, repriser, or rebounder!", false);
                        break;
                    }
                }

                @Override
                public void finish() {
                    player.getInterfaceManager().closeChatBoxInterface();
                }
            }, itemId);
        }

        if (itemId == 35886) {
            final Item[] cacheRewards = { new Item(26750, 10), new Item(23610, 20), new Item(23352, 50), new Item(23400, 20), new Item(37972, 2), new Item(30372, 100), new Item(12163, 100), new Item(12160, 150) };
            final int rewardSize = cacheRewards.length;
            if (player.getInventory().getFreeSlots() >= rewardSize) {
                player.getInventory().deleteItem(35886, 1);
                for (Item cacheReward : cacheRewards) {
                    player.getInventory().addItem(cacheReward);
                }
                player.sendMessage(Colors.CYAN + "You opened up the Supply cache!", true);
            } else {
                player.sendMessage("You need at least " + rewardSize + " free inventory slots to open the Supply cache.");
            }
            return;
        }
        /* Nex AoD Chests */
        if (itemId == 39586) {
            if (player.getInventory().getFreeSlots() >= 6) {
                player.getInventory().deleteItem(39586, 1);
                player.getInventory().addItem(39609, 1);
                player.getInventory().addItem(39610, 1);
                player.getInventory().addItem(39611, 1);
                player.getInventory().addItem(39612, 1);
                player.getInventory().addItem(39613, 1);
                player.getInventory().addItem(25430, 5);
                player.sendMessage(Colors.CYAN + "You find the legendary Cruor robes!", true);
            } else {
                player.sendMessage(Colors.RED + "You need more inventory space for this!");
            }
        }
        if (itemId == 39588) {
            if (player.getInventory().getFreeSlots() >= 6) {
                player.getInventory().deleteItem(39588, 1);
                player.getInventory().addItem(39603, 1);
                player.getInventory().addItem(39604, 1);
                player.getInventory().addItem(39605, 1);
                player.getInventory().addItem(39606, 1);
                player.getInventory().addItem(39607, 1);
                player.getInventory().addItem(25430, 5);
                player.sendMessage(Colors.CYAN + "You find the legendary Glacies robes!", true);
            } else {
                player.sendMessage(Colors.RED + "You need more inventory space for this!");
            }
        }
        if (itemId == 39592) {
            if (player.getInventory().getFreeSlots() >= 6) {
                player.getInventory().deleteItem(39592, 1);
                player.getInventory().addItem(39615, 1);
                player.getInventory().addItem(39616, 1);
                player.getInventory().addItem(39617, 1);
                player.getInventory().addItem(39618, 1);
                player.getInventory().addItem(39619, 1);
                player.getInventory().addItem(25430, 5);
                player.sendMessage(Colors.CYAN + "You find the legendary Fumus Robes!", true);
            } else {
                player.sendMessage(Colors.RED + "You need more inventory space for this!");
            }
        }
        if (itemId == 39590) {
            if (player.getInventory().getFreeSlots() >= 6) {
                player.getInventory().deleteItem(39590, 1);
                player.getInventory().addItem(39597, 1);
                player.getInventory().addItem(39598, 1);
                player.getInventory().addItem(39599, 1);
                player.getInventory().addItem(39600, 1);
                player.getInventory().addItem(39601, 1);
                player.getInventory().addItem(25430, 5);
                player.sendMessage(Colors.CYAN + "You find the legendary Umbra Robes!", true);
            } else {
                player.sendMessage(Colors.RED + "You need more inventory space for this!");
            }
        }

        /** bundle bamboo */
        if (itemId == 37770) {
            if (player.getInventory().containsItem(37770, 5)) {
                player.getActionManager().setAction(new Bamboo(player));
            }
        }

        /** Combatant's cape **/
        if (itemId >= 32069 && itemId <= 32076) {
            final Item[] items = { new Item(32069), new Item(32070), new Item(32071), new Item(32072), new Item(32073), new Item(32074), new Item(32075), new Item(32076) };
            if (!player.getInventory().containsItems(items)) {
                player.sendMessage("You will need these shards in order to combine them into an Expert Skillcape.");
                player.sendMessage("Aerodynamic, Holy, Imbued, Rigid, Sharp, Strong, Summoned and Vital.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(32053));
            if (!player.hasItem(new Item(32057))) {
                player.getInventory().addItem(new Item(32057));
            }
            player.getDialogueManager().startDialogue("SimpleItemMessage", 32053, 1, "You've combined all 8 skill shards into a Combatant's cape and hood!");
            return;
        }
        /** Artisan's cape **/
        if (itemId >= 32077 && itemId <= 32084) {
            final Item[] items = { new Item(32077), new Item(32078), new Item(32079), new Item(32080), new Item(32081), new Item(32082), new Item(32083), new Item(32084) };
            if (!player.getInventory().containsItems(items)) {
                player.sendMessage("You will need these shards in order to combine them into an Expert Skillcape.");
                player.sendMessage("Carved, Designed, Fiery, Fletched, Herbal, Molten, Roasted and Runic.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(32054));
            if (!player.hasItem(new Item(32058))) {
                player.getInventory().addItem(new Item(32058));
            }
            player.getDialogueManager().startDialogue("SimpleItemMessage", 32054, 1, "You've combined all 8 skill shards into an Artisan's cape and hood!");
            return;
        }
        /** Gatherer's cape **/
        if (itemId >= 32063 && itemId <= 32068) {
            final Item[] items = { new Item(32063), new Item(32064), new Item(32065), new Item(32066), new Item(32067), new Item(32068) };
            if (!player.getInventory().containsItems(items)) {
                player.sendMessage("You will need these shards in order to combine them into an Expert Skillcape.");
                player.sendMessage("Balanced, Cultivated, Damp, Mineral, Trapped and Wooden.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(32052));
            if (!player.hasItem(new Item(32056))) {
                player.getInventory().addItem(new Item(32056));
            }
            player.getDialogueManager().startDialogue("SimpleItemMessage", 32052, 1, "You've combined all 6 skill shards into a Gatherer's cape and hood!");
            return;
        }
        /** Support cape **/
        if (itemId >= 32085 && itemId <= 32088) {
            final Item[] items = { new Item(32085), new Item(32086), new Item(32087), new Item(32088) };
            if (!player.getInventory().containsItems(items)) {
                player.sendMessage("You will need these shards in order to combine them into an Expert Skillcape.");
                player.sendMessage("Agile, Daemonheim, Deadly and Hidden.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(32055));
            if (!player.hasItem(new Item(32059))) {
                player.getInventory().addItem(new Item(32059));
            }
            player.getDialogueManager().startDialogue("SimpleItemMessage", 32055, 1, "You've combined all 4 skill shards into a Support cape and hood!");
            return;
        }
        /** Air Battlestaff **/
        if (itemId == 573) {
            if (!player.getInventory().containsItem(new Item(1391))) {
                player.sendMessage("You don't have a Battlestaff to attach the orb to.");
                return;
            }
            if (player.getSkills().getLevel(Skills.CRAFTING) < 66) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of 66 to do this.");
                return;
            }
            player.lock(1);
            player.getSkills().addXp(Skills.CRAFTING, 137.5);
            player.getInventory().deleteItem(itemId, 1);
            player.getInventory().deleteItem(1391, 1);
            player.getInventory().addItem(new Item(1397));
            player.addItemsMade();
            player.sendMessage("You attach the orb to the battlestaff; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
            return;
        }
        /** Water Battlestaff **/
        if (itemId == 571) {
            if (!player.getInventory().containsItem(new Item(1391))) {
                player.sendMessage("You don't have a Battlestaff to attach the orb to.");
                return;
            }
            if (player.getSkills().getLevel(Skills.CRAFTING) < 54) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of 54 to do this.");
                return;
            }
            player.lock(1);
            player.getSkills().addXp(Skills.CRAFTING, 100);
            player.getInventory().deleteItem(itemId, 1);
            player.getInventory().deleteItem(1391, 1);
            player.getInventory().addItem(new Item(1395));
            player.addItemsMade();
            player.sendMessage("You attach the orb to the battlestaff; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
            return;
        }
        /** Earth Battlestaff **/
        if (itemId == 575) {
            if (!player.getInventory().containsItem(new Item(1391))) {
                player.sendMessage("You don't have a Battlestaff to attach the orb to.");
                return;
            }
            if (player.getSkills().getLevel(Skills.CRAFTING) < 58) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of 58 to do this.");
                return;
            }
            player.lock(1);
            player.getSkills().addXp(Skills.CRAFTING, 112.5);
            player.getInventory().deleteItem(itemId, 1);
            player.getInventory().deleteItem(1391, 1);
            player.getInventory().addItem(new Item(1399));
            player.addItemsMade();
            player.sendMessage("You attach the orb to the battlestaff; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
            return;
        }
        /** Fire Battlestaff **/
        if (itemId == 569) {
            if (!player.getInventory().containsItem(new Item(1391))) {
                player.sendMessage("You don't have a Battlestaff to attach the orb to.");
                return;
            }
            if (player.getSkills().getLevel(Skills.CRAFTING) < 62) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of 62 to do this.");
                return;
            }
            player.lock(1);
            player.getSkills().addXp(Skills.CRAFTING, 125);
            player.getInventory().deleteItem(itemId, 1);
            player.getInventory().deleteItem(1391, 1);
            player.getInventory().addItem(new Item(1393));
            player.addItemsMade();
            player.sendMessage("You attach the orb to the battlestaff; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
            return;
        }
        /** Orb of Armadyl **/
        if (itemId == 21776) {
            if (!player.getInventory().containsItem(new Item(21776, 100))) {
                player.sendMessage("You need at least 100 Shards of Armadyl to combine them into an orb.");
                return;
            }
            if (player.getSkills().getLevel(Skills.CRAFTING) < 72) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of 72 to do this.");
                return;
            }
            player.lock(1);
            player.getSkills().addXp(Skills.CRAFTING, 125);
            player.getInventory().deleteItem(itemId, 100);
            player.getInventory().addItem(new Item(21775));
            player.addItemsMade();
            player.sendMessage("You combine 100 shards into an orb; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
            return;
        }
        /** Armadyl battlestaff **/
        if (itemId == 21775) {
            if (!player.getInventory().containsItem(new Item(1391))) {
                player.sendMessage("You need a battlestaff to attach the orb onto.");
                return;
            }
            if (player.getSkills().getLevel(Skills.CRAFTING) < 72) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You need a Crafting level of 72 to do this.");
                return;
            }
            player.lock(1);
            player.getSkills().addXp(Skills.CRAFTING, 150);
            player.getInventory().deleteItem(itemId, 1);
            player.getInventory().deleteItem(1391, 1);
            player.getInventory().addItem(new Item(21777));
            player.addItemsMade();
            player.sendMessage("You attach the orb onto the battlestaff; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
            return;
        }
        if (item.getId() == 35998) {
            player.getDialogueManager().startDialogue("SkillingMasterD", false);
            return;
        }
        if (itemId == 24337) {
            player.sendMessage("You will have to get extremely burnt from the QBD's fire in order to brandish this.");
            return;
        }
        if (itemId == 23193 || itemId == 32845) {
            CraftingRs3Dialogue.sendGlassblowingInterface(player);
            return;
        }
        if (itemId == 28600 || itemId == 28602 || itemId == 28604) {
            if (player.getInventory().containsItems(new Item[] { new Item(28600), new Item(28602), new Item(28604) })) {
                player.getInventory().deleteItem(28600, 1);
                player.getInventory().deleteItem(28602, 1);
                player.getInventory().deleteItem(28604, 1);
                player.getInventory().addItem(28606, 1);
                player.sendMessage("You create a Maul of Omens from your weapon pieces.");
                return;
            } else {
                player.sendMessage("You need all 3 pieces of the " + "Maul to combine it.");
            }
            return;
        }
        /**
         * New Dungeoneering Token pouches.
         */
        if (itemId == 32747) {
            player.getInventory().deleteItem(new Item(itemId, 1));
            player.setDungeoneeringTokens(player.getDungeoneeringTokens() + 10);
            player.sendMessage("You've opened the Dungeoneering Token pouch for 10 tokens.");
            return;
        }
        if (itemId == 32748) {
            player.getInventory().deleteItem(new Item(itemId, 1));
            player.setDungeoneeringTokens(player.getDungeoneeringTokens() + 50);
            player.sendMessage("You've opened the Dungeoneering Token pouch for 50 tokens.");
            return;
        }
        if (itemId == 32749) {
            player.getInventory().deleteItem(new Item(itemId, 1));
            player.setDungeoneeringTokens(player.getDungeoneeringTokens() + 100);
            player.sendMessage("You've opened the Dungeoneering Token pouch for 100 tokens.");
            return;
        }
        if (itemId == 32750) {
            player.getInventory().deleteItem(new Item(itemId, 1));
            player.setDungeoneeringTokens(player.getDungeoneeringTokens() + 500);
            player.sendMessage("You've opened the Dungeoneering Token pouch for 500 tokens.");
            return;
        }
        if (itemId == 7509) {
            if (player.getHitpoints() == 1) {
                player.sendMessage(Colors.SALMON + "Your hitpoints cannot go any lower!", true);
                return;
            }
            if (player.getFoodDelay() > Utils.currentTimeMillis() && itemId != 3144) {
                return;
            }
            if (player.getKaramDelay() > Utils.currentTimeMillis() && itemId == 3144) {
                return;
            }
            player.setNextAnimation(new Animation(829));
            final int damage = player.getHitpoints() - 1;
            if (player.getHitpoints() > 1) {
                player.applyHit(new Hit(player, damage, HitLook.INSTANT_KILL_TYPE));
            }
            player.addFoodDelay(1000);
            return;
        }
        if (itemId == 24352) {
            player.getDialogueManager().startDialogue("SimpleMessage", "This upgrade kit is usually used on infinity magic equipment " + "or dragon armour. The equipment you use it on changes to Dragonbone equipment. You can detach this kit " + "afterwards to get the starting items back.");
            return;
        }
        if (itemId == 19967) {
            Magic.vineTeleport(player, new WorldTile(2952, 2931, 0));
        }
        // Blamish blue shell
        if (itemId == 3351) {
            if (player.getSkills().getLevel(Skills.CRAFTING) < 15) {
                player.sendMessage("You will need a Crafting level of at least 15 to do this.");
                return;
            }
            if (!player.getInventory().containsOneItem(1755)) {
                player.sendMessage("You will need a Chisel in order to do this.");
                return;
            }
            player.lock(1);
            player.getSkills().addXp(Skills.CRAFTING, 32.5);
            player.addItemsMade();
            player.sendMessage("You make a Bruise blue snelm; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
            player.getInventory().deleteItem(new Item(itemId));
            player.getInventory().addItem(new Item(3333));
            return;
        }
        if (itemId == 3361) {
            if (player.getSkills().getLevel(Skills.CRAFTING) < 15) {
                player.sendMessage("You will need a Crafting level of at least 15 to do this.");
                return;
            }
            if (!player.getInventory().containsOneItem(1755)) {
                player.sendMessage("You will need a Chisel in order to do this.");
                return;
            }
            player.lock(1);
            player.getSkills().addXp(Skills.CRAFTING, 32.5);
            player.addItemsMade();
            player.sendMessage("You make a Bruise blue snelm; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
            player.getInventory().deleteItem(new Item(itemId));
            player.getInventory().addItem(new Item(3343));
            return;
        }
        if (itemId == 3347) {
            if (player.getSkills().getLevel(Skills.CRAFTING) < 15) {
                player.sendMessage("You will need a Crafting level of at least 15 to do this.");
                return;
            }
            if (!player.getInventory().containsOneItem(1755)) {
                player.sendMessage("You will need a Chisel in order to do this.");
                return;
            }
            player.lock(1);
            player.getSkills().addXp(Skills.CRAFTING, 32.5);
            player.addItemsMade();
            player.sendMessage("You make a Blood'n'tar snelm; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
            player.getInventory().deleteItem(new Item(3347));
            player.getInventory().addItem(new Item(3329));
            return;
        }
        if (itemId == 3357) {
            if (player.getSkills().getLevel(Skills.CRAFTING) < 15) {
                player.sendMessage("You will need a Crafting level of at least 15 to do this.");
                return;
            }
            if (!player.getInventory().containsOneItem(1755)) {
                player.sendMessage("You will need a Chisel in order to do this.");
                return;
            }
            player.lock(1);
            player.getSkills().addXp(Skills.CRAFTING, 32.5);
            player.addItemsMade();
            player.sendMessage("You make a Blood'n'tar snelm; " + "items crafted: " + Colors.RED + Utils.getFormattedNumber(player.getItemsMade()) + "</col>.", true);
            player.getInventory().deleteItem(new Item(3357));
            player.getInventory().addItem(new Item(3339));
            return;
        }
        if (itemId == 3062) {
            HerbloreBox.open(player, 1);
            return;
        }
        if (itemId == 985 || itemId == 987) {
            CrystalChest.makeKey(player);
            return;
        }
        if (itemId == 28627) {
            player.getDialogueManager().startDialogue("TectonicEnergyCraftingD", null, TectonicEnergyCrafting.ARMOUR);
            return;
        }
        if (itemId == 29863) {
            player.getDialogueManager().startDialogue("SirenicScaleCraftingD", null, SirenicScaleCrafting.ARMOUR);
            return;
        }
        if (itemId >= 31718 && itemId <= 31720) {
            AraxCrafting.handleSpiderLeg(player);
            return;
        }
        if (itemId >= 31721 && itemId <= 31724) {
            player.getDialogueManager().startDialogue("NoxiousCreateD");
            return;
        }

        if (itemId == AncientEffigy.SATED_ANCIENT_EFFIGY || itemId == AncientEffigy.GORGED_ANCIENT_EFFIGY || itemId == AncientEffigy.NOURISHED_ANCIENT_EFFIGY || itemId == AncientEffigy.STARVED_ANCIENT_EFFIGY) {
            player.getDialogueManager().startDialogue("AncientEffigyD", itemId);
            return;
        }

        if (itemId == 15364) {
            if (player.getInventory().addItem(new Item(222, 50))) {
                player.getInventory().deleteItem(itemId, 1);
                player.sendMessage("You've opened an eye of newt pack.");
                return;
            }
            player.sendMessage("Inventory full. Sell, drop or bank something to make space.", true);
            return;
        }
        if (itemId == 15363) {
            if (player.getInventory().addItem(new Item(228, 50))) {
                player.getInventory().deleteItem(itemId, 1);
                player.sendMessage("You've opened a water vial pack.");
                return;
            }
            player.sendMessage("Inventory full. Sell, drop or bank something to make space.", true);
            return;
        }
        if (itemId == 15362) {
            if (player.getInventory().addItem(230, 50)) {
                player.getInventory().deleteItem(15362, 1);
            } else {
                player.sendMessage("Free up some inventory space before you open this.", false);
            }
            return;
        }

        if (LeatherCrafting.handleItemOnItem(player, item, new Item(1733))) {
            return;
        }

//        if (itemId == 18782) {
//            player.getDialogueManager().startDialogue("DragonkinLamp");
//            return;
//        }
        final Pouches pouches = Pouches.forId(itemId);
        if (pouches != null) {
            Summoning.spawnFamiliar(player, pouches);
        }

        if (XPLamps.isSelectable(itemId) || XPLamps.isSkillLamp(itemId) || XPLamps.isOtherLamp(itemId)) {
            XPLamps.processLampClick(player, slotId, itemId);
            return;
        }
        if (PrismaticStars.isSelectable(itemId) || PrismaticStars.isSkillStar(itemId)) {
            PrismaticStars.processStarClick(player, slotId, itemId);
            return;
        }
        if (itemId == 29294) {
            DivineObject.placeDivine(player, itemId, 87285, 34107, 1, 14);
        } else if (itemId == 29295) {
            DivineObject.placeDivine(player, itemId, 87286, 57572, 15, 14);
        } else if (itemId == 29296) {
            DivineObject.placeDivine(player, itemId, 87287, 87266, 30, 14);
        } else if (itemId == 29297) {
            DivineObject.placeDivine(player, itemId, 87288, 87267, 55, 14);
        } else if (itemId == 29298) {
            DivineObject.placeDivine(player, itemId, 87289, 87268, 70, 14);
        } else if (itemId == 29299) {
            DivineObject.placeDivine(player, itemId, 87290, 87269, 85, 14);
        }
        // divine trees
        else if (itemId == 29304) {
            DivineObject.placeDivine(player, itemId, 87295, 87274, 1, 8);
        } else if (itemId == 29305) {
            DivineObject.placeDivine(player, itemId, 87296, 87275, 15, 8);
        } else if (itemId == 29306) {
            DivineObject.placeDivine(player, itemId, 87297, 87276, 30, 8);
        } else if (itemId == 29307) {
            DivineObject.placeDivine(player, itemId, 87298, 87277, 45, 8);
        } else if (itemId == 29308) {
            DivineObject.placeDivine(player, itemId, 87299, 87278, 60, 8);
        } else if (itemId == 29309) {
            DivineObject.placeDivine(player, itemId, 87300, 87279, 75, 8);
        }
        // herblore patch (farming)
        else if (itemId == 29310) {
            DivineObject.placeDivine(player, itemId, 87301, 87280, 9, Skills.FARMING);
        } else if (itemId == 29311) {
            DivineObject.placeDivine(player, itemId, 87302, 87281, 44, Skills.FARMING);
        } else if (itemId == 29312) {
            DivineObject.placeDivine(player, itemId, 87303, 87282, 67, Skills.FARMING);
        }
        // hunting
        else if (itemId == 29300) {
            DivineObject.placeDivine(player, itemId, 87291, 87270, 1, 21);
        } else if (itemId == 29301) {
            DivineObject.placeDivine(player, itemId, 87292, 87271, 1, 21);
        } else if (itemId == 29302) {
            DivineObject.placeDivine(player, itemId, 87293, 87272, 23, 21);
        } else if (itemId == 29303) {
            DivineObject.placeDivine(player, itemId, 87294, 87273, 53, 21);
        }
        // fishing
        else if (itemId == 31080) {
            DivineObject.placeDivine(player, itemId, 90232, 90223, 1, Skills.FISHING);
        } else if (itemId == 31081) {
            DivineObject.placeDivine(player, itemId, 90233, 90224, 10, Skills.FISHING);
        } else if (itemId == 31082) {
            DivineObject.placeDivine(player, itemId, 90234, 90225, 20, Skills.FISHING);
        } else if (itemId == 31083) {
            DivineObject.placeDivine(player, itemId, 90235, 90226, 30, Skills.FISHING);
        } else if (itemId == 31084) {
            DivineObject.placeDivine(player, itemId, 90236, 90227, 40, Skills.FISHING);
        } else if (itemId == 31085) {
            DivineObject.placeDivine(player, itemId, 90237, 90228, 50, Skills.FISHING);
        } else if (itemId == 31086) {
            DivineObject.placeDivine(player, itemId, 90238, 90229, 76, Skills.FISHING);
        } else if (itemId == 31087) {
            DivineObject.placeDivine(player, itemId, 90239, 90230, 85, Skills.FISHING);
        } else if (itemId == 31088) {
            DivineObject.placeDivine(player, itemId, 90240, 90231, 90, Skills.FISHING);
        }
        // div not being used
        else if (itemId == 31310) {
            DivineObject.placeDivine(player, itemId, 66526, 66528, 75, Skills.DIVINATION);
        } else if (itemId == 31311) {
            DivineObject.placeDivine(player, itemId, 66529, 66531, 86, Skills.DIVINATION);
        }
        if (item.getDefinitions().containsOption("Grind") || item.getDefinitions().containsOption("Squeeze")) {
            final int herbloreVersa = Herblore.isHerbloreSkill(new Item(233), item);
            if (herbloreVersa > -1) {
                player.getDialogueManager().startDialogue("HerbloreD", herbloreVersa, item, new Item(233), false);
                return;
            }
        }
        if (itemId == 3325) {
            int unfId = -1;
            int completeId = -1;
            for (Item invItem : player.getInventory().getItems().getItems()) {
                if (invItem == null) {
                    continue;
                }
                if (invItem.getId() == 11505) {
                    unfId = 11505;
                    completeId = 11509;
                    break;
                } else if (invItem.getId() == 11501) {
                    unfId = 11501;
                    completeId = 11433;
                    break;
                }
            }
            if (unfId != -1) {
                Item used = new Item(3325);
                Item with = new Item(unfId);
                player.getDialogueManager().startDialogue("HerbloreD", completeId, used, with, false);
            }
            return;
        }
        if (AshScattering.scatter(player, slotId)) {
            return;
        }
        if (itemId >= 15086 && itemId <= 15100) {
            Dicing.handleRoll(player, itemId, false);
            return;
        }
        if (itemId == 11640) {
            player.getDialogueManager().startDialogue("VoteBookD");
            return;
        } else if (itemId == 1775 || itemId == 23193) {
            CraftingRs3Dialogue.sendGlassblowingInterface(player);
            return;
        }
        if (itemId == 8463) {
            player.getDialogueManager().startDialogue("ConstructionGuide");
            return;
        }
        if (itemId >= 5070 && itemId <= 5074) {
            BirdNests.searchNest(player, itemId);
            return;
        }
        if (itemId == Gem.OPAL.getUncut()) {
            GemCutting.cut(player, Gem.OPAL);
            return;
        }
        if (itemId == Gem.JADE.getUncut()) {
            GemCutting.cut(player, Gem.JADE);
            return;
        }
        if (itemId == Gem.RED_TOPAZ.getUncut()) {
            GemCutting.cut(player, Gem.RED_TOPAZ);
            return;
        }
        if (itemId == Gem.SAPPHIRE.getUncut()) {
            GemCutting.cut(player, Gem.SAPPHIRE);
            return;
        }
        if (itemId == Gem.EMERALD.getUncut()) {
            GemCutting.cut(player, Gem.EMERALD);
            return;
        }
        if (itemId == Gem.RUBY.getUncut()) {
            GemCutting.cut(player, Gem.RUBY);
            return;
        }
        if (itemId == Gem.DIAMOND.getUncut()) {
            GemCutting.cut(player, Gem.DIAMOND);
            return;
        }
        if (itemId == Gem.DRAGONSTONE.getUncut()) {
            GemCutting.cut(player, Gem.DRAGONSTONE);
            return;
        }
        if (itemId == Gem.ONYX.getUncut()) {
            GemCutting.cut(player, Gem.ONYX);
            return;
        }
        if (itemId == Gem.HYDRIX.getUncut()) {
            GemCutting.cut(player, Gem.HYDRIX);
            return;
        }
        if (itemId >= 20121 && itemId <= 20124) {
            if (player.getInventory().containsItem(20121, 1) && player.getInventory().containsItem(20122, 1) && player.getInventory().containsItem(20123, 1) && player.getInventory().containsItem(20124, 1)) {
                player.getInventory().deleteItem(20121, 1);
                player.getInventory().deleteItem(20122, 1);
                player.getInventory().deleteItem(20123, 1);
                player.getInventory().deleteItem(20124, 1);
                player.getInventory().addItem(20120, 1);
                player.setFrozenKeyCharges((byte) (player.getPerkManager().hasPerkActive(DonationPerk.GWD_SPECIALIST) ? 20 : 10));
                player.sendMessage("You've made a Frozen Key out of the individual key parts.");
                return;
            }
            player.sendMessage("The Key parts don't quite fit.. you're missing something.");
            return;
        }
        if (itemId == 20120) {
            player.sendMessage("Your Frozen key has " + Colors.RED + player.getFrozenKeyCharges() + "</col> charges left.");
            return;
        }
        if (itemId == Gem.OPAL.getCut()) {
            BoltTipFletching.boltFletch(player, BoltTips.OPAL);
            return;
        }
        if (itemId == Gem.JADE.getCut()) {
            BoltTipFletching.boltFletch(player, BoltTips.JADE);
            return;
        }
        if (itemId == Gem.RED_TOPAZ.getCut()) {
            BoltTipFletching.boltFletch(player, BoltTips.RED_TOPAZ);
            return;
        }
        if (itemId == Gem.SAPPHIRE.getCut()) {
            BoltTipFletching.boltFletch(player, BoltTips.SAPPHIRE);
            return;
        }
        if (itemId == Gem.EMERALD.getCut()) {
            BoltTipFletching.boltFletch(player, BoltTips.EMERALD);
            return;
        }
        if (itemId == Gem.RUBY.getCut()) {
            BoltTipFletching.boltFletch(player, BoltTips.RUBY);
            return;
        }
        if (itemId == Gem.DIAMOND.getCut()) {
            BoltTipFletching.boltFletch(player, BoltTips.DIAMOND);
            return;
        }
        if (itemId == Gem.DRAGONSTONE.getCut()) {
            BoltTipFletching.boltFletch(player, BoltTips.DRAGONSTONE);
            return;
        }
        if (itemId == Gem.ONYX.getCut()) {
            if (player.getInventory().getNumberOf(Gem.ONYX.getCut()) == 1) {
                player.getDialogueManager().startDialogue("FletchOnyxD");
            } else {
                BoltTipFletching.boltFletch(player, BoltTips.ONYX);
            }
            return;
        }
        if (itemId == Gem.HYDRIX.getCut()) {
            if (player.getInventory().getNumberOf(Gem.HYDRIX.getCut()) == 1) {
                player.getDialogueManager().startDialogue("FletchHydrixD");
            } else {
                BoltTipFletching.boltFletch(player, BoltTips.HYDRIX);
            }
            return;
        }
        if (Pots.pot(player, item, slotId)) {
            return;
        }
        if (itemId == 952) {
            dig(player);
            return;
        }
        if (itemId == 19670) {
            if (player.hasEfficiencyActivated()) {
                player.sendMessage("You've already got one of those activated.");
                return;
            }
            player.setEfficiency(true);
            player.getInventory().deleteItem(19670, 1);
            player.sendMessage(Colors.RED + "<shad=000000>The secret is yours! " + "You unlock the ability to save bars when smithing.");
            return;
        }
        if (itemId == 18839) {
            if (player.hasRigourActivated()) {
                if (player.hasAnguishActivated()) {
                    player.sendMessage("You have already activated both anguish and rigour!");
                    return;
                } else {
                    player.setAnguish(true);
                    player.sendMessage(Colors.RED + "<shad=000000>You activate the scroll, and learn the ability anguish!");
                    player.getInventory().deleteItem(18839, 1);
                    return;
                }
            }
            player.setRigour(true);
            player.getInventory().deleteItem(18839, 1);
            player.sendMessage(Colors.RED + "<shad=000000>You activate the Scroll of Rigour " + "and learn a new prayer ability.");
            return;
        }
        if (itemId == 18343) {
            if (player.hasRenewalActivated()) {
                player.sendMessage("You have already activated your Scroll of Renewal.");
                return;
            }
            player.setRenewal(true);
            player.getInventory().deleteItem(18343, 1);
            player.sendMessage(Colors.RED + "<shad=000000>You activate the Scroll of Renewal " + "and learn a new prayer ability.");
            return;
        }
        if (itemId == 18344) {
            if (player.hasAuguryActivated()) {
                if (player.hasTormentActivated()) {
                    player.sendMessage("You have already activated both torment and augury!");
                    return;
                } else {
                    player.setTorment(true);
                    player.sendMessage(Colors.RED + "<shad=000000>You activate the scroll, and learn the ability torment!");
                    player.getInventory().deleteItem(18344, 1);
                    return;
                }
            }
            player.setAugury(true);
            player.sendMessage(Colors.RED + "<shad=000000>You activate the scroll of augury " + "and learn a new prayer ability.");
            player.getInventory().deleteItem(18344, 1);
            return;
        }
        if (itemId == 18336) {
            if (player.hasLifeActivated()) {
                player.sendMessage("You have already activated your scroll of life.");
                return;
            }
            if (player.getSkills().getLevelForXp(Skills.FARMING) < 25) {
                player.sendMessage("You'll need a Farming level of at least 25 to activate this scroll.");
                return;
            }
            player.setLife(true);
            player.sendMessage(Colors.RED + "<shad=000000>You activate the scroll of life " + "and learn a new farming technique.");
            player.getInventory().deleteItem(itemId, 1);
            return;
        }
        if (itemId == 19890) {
            if (player.hasCleansingActivated()) {
                player.sendMessage("You have already activated your scroll of cleansing.");
                return;
            }
            if (player.getSkills().getLevelForXp(Skills.HERBLORE) < 49) {
                player.sendMessage("You'll need a Herblore level of at least 49 to activate this scroll.");
                return;
            }
            player.setCleansing(true);
            player.sendMessage(Colors.RED + "<shad=000000>You activate the scroll of cleansing " + "and learn a new herblore technique.");
            player.getInventory().deleteItem(itemId, 1);
            return;
        }
        if (itemId == 24154 || itemId == 24155) {
            player.getInventory().deleteItem(item);
            player.getTreasureHunter().giveEarnedSpins(itemId == 24154 ? 1 : 2);
            return;
        }
        if (itemId >= 5509 && itemId <= 5514) {
            int pouch = -1;
            if (itemId == 5509) {
                pouch = 0;
            }
            if (itemId == 5510) {
                pouch = 1;
            }
            if (itemId == 5512) {
                pouch = 2;
            }
            if (itemId == 5514) {
                pouch = 3;
            }
            RuneCrafting.fillPouch(player, pouch);
            return;
        }
        HerbCleaning.Herbs herb = HerbCleaning.getHerb(item.getId());
        if (herb != null && HerbloreRs3Dialogue.sendCleanHerbInterface(player, herb)) {
            return;
        }
        if (HerbCleaning.clean(player, item, slotId)) {
            return;
        }
        final Bone bone = Bone.forId(itemId);
        if (bone != null) {
            Bone.bury(player, slotId);
            return;
        }
        final Sets set = ItemSets.getSet(itemId);
        if (set != null) {
            ItemSets.exchangeSet(player, slotId, set.getId());
            return;
        }
        if (Magic.useTabTeleport(player, itemId)) {
            return;
        }
        if (Magic.useScrollTeleport(player, itemId)) {
            return;
        }
        switch (itemId) {
        case 33527: // Smouldering lamps v.
            player.getDialogueManager().startDialogue("SmoulderingLampD", 0);
            break;
        case 33528:
            player.getDialogueManager().startDialogue("SmoulderingLampD", 1);
            break;
        case 33529:
            player.getDialogueManager().startDialogue("SmoulderingLampD", 2);
            break;
        case 33530:
            player.getDialogueManager().startDialogue("SmoulderingLampD", 3);
            break;
        case 22370:
            Summoning.openDreadnipInterface(player);
            break;
        case 15262:
            player.getInventory().deleteItem(itemId, 1);
            player.addItem(12183, 5000);
            break;
        case 4251:
            Magic.dispatchEctophial(player, item);
            break;
        case 405:
            player.getInventory().deleteItem(405, 1);
            player.getInventory().addItem(995, Utils.random(50000, 1000000));
            player.sendMessage("The casket slowly opens... You receive coins!");
            break;
        case 23814:
            player.getSkills().addXp(Skills.SUMMONING, 400);
            player.getInventory().deleteItem(itemId, slotId);
            break;
        case 23815:
            player.getSkills().addXp(Skills.SUMMONING, 400);
            player.getInventory().deleteItem(itemId, slotId);
            break;
        case 23816:
            player.getSkills().addXp(Skills.SUMMONING, 700);
            player.getInventory().deleteItem(itemId, slotId);
            break;
        case 23722:
            player.getSkills().addXp(Skills.STRENGTH, 400);
            player.getInventory().deleteItem(itemId, slotId);
            break;
        case 23726:
            player.getSkills().addXp(Skills.DEFENCE, 300);
            player.getInventory().deleteItem(itemId, slotId);
            break;
        case 23730:
            player.getSkills().addXp(Skills.RANGE, 300);
            player.getInventory().deleteItem(itemId, slotId);
            break;
        case 23731:
            player.getSkills().addXp(Skills.RANGE, 700);
            player.getInventory().deleteItem(itemId, slotId);
            break;
        case 23735:
            player.getSkills().addXp(Skills.MAGIC, 700);
            player.getInventory().deleteItem(itemId, slotId);
            break;
        case 23734:
            player.getSkills().addXp(Skills.MAGIC, 700);
            player.getInventory().deleteItem(itemId, slotId);
            break;
        case 23727:
            player.getSkills().addXp(Skills.DEFENCE, 700);
            player.getInventory().deleteItem(itemId, slotId);
            break;
        case 23723:
            player.getSkills().addXp(Skills.STRENGTH, 700);
            player.getInventory().deleteItem(itemId, slotId);
            break;
        case 23718:
            player.getSkills().addXp(Skills.ATTACK, 400);
            player.getInventory().deleteItem(itemId, slotId);
            break;
        case 23716:
            player.getSkills().addXp(Skills.DUNGEONEERING, 5000);
            player.getInventory().deleteItem(itemId, slotId);
            break;
        case 20667:
            if (player.getVecnaTimer() > 0) {
                player.getPackets().sendGameMessage("The skull has not yet regained its magical aura. You will need to wait another " + player.getVecnaTimer() / 60000 + " minutes.");
            } else if (player.getVecnaTimer() == 0) {
                player.setVecnaTimer(7 * 60000);
                player.setNextGraphics(new Graphics(738, 0, 94));
                player.setNextAnimation(new Animation(10530));
                player.getSkills().set(Skills.MAGIC, player.getSkills().getLevelForXp(Skills.MAGIC) + 6);
                player.getPackets().sendGameMessage("The skull feeds off the life around you, boosting your magic ability.");
                player.vecnaTimer(7);
            }
            break;
        case 4155:
            player.getDialogueManager().startDialogue("Kuradal", 9085);
            return;
        case 18768:
            // player.getDialogueManager().startDialogue("DonatorBoxD");
            break;
        case 31846:
            player.getDialogueManager().startDialogue("GrimGem");
            return;
        }
        if (itemId >= 23653 && itemId <= 23658) {
            FightKiln.useCrystal(player, itemId);
        } else if (TrapAction.isTrap(player, new WorldTile(player), itemId)) {
            return;
        } else if (item.getDefinitions().getName().startsWith("Burnt")) {
            player.getDialogueManager().startDialogue("SimplePlayerMessage", "Ugh, this is inedible.");
        } else if (player.getTreasureTrails().useItem(item, slotId)) {
            return;
        } else if (itemId == BarrowsAmulet.BARROWS_AMULET_ID) {
            BarrowsAmulet.activate(player);
        } else if (itemId == 2574) {
            player.getTreasureTrails().useSextant();
        } else if (itemId == 2798 || itemId == 3565 || itemId == 3576 || itemId == 19042) {
            player.getTreasureTrails().openPuzzle(itemId);
        } else if (item.getDefinitions().containsInventoryOption(1, "Wear") || item.getName().contains("Ring of slaying") || item.getName().contains("slayer helmet")) {
            final long passedTime = Utils.currentTimeMillis() - WorldThread.WORLD_CYCLE;
            WorldTasksManager.schedule(new WorldTask() {

                @Override
                public void run() {
                    final List<Integer> slots = player.getSwitchItemCache();
                    final int[] slot = new int[slots.size()];
                    for (int i = 0; i < slot.length; i++) {
                        slot[i] = slots.get(i);
                    }
                    player.getSwitchItemCache().clear();
                    if (ButtonHandler.sendWear(player, slot))
                        player.stopAll(false, true, false);
                }
            }, passedTime >= 600 ? 0 : passedTime > 330 ? 1 : 0);
            if (player.getSwitchItemCache().contains(slotId)) {
                return;
            }
            player.getSwitchItemCache().add(slotId);
        }
    }

    public static void handleItemOption2(final Player player, final int slotId, final int itemId, final Item item) {
        if (Settings.DEBUG) {
            Logger.getGlobal().info("Option 2; slotId: " + slotId + "; itemId: " + itemId + ".");
        }
        if (itemId == 995) {
            addInventoryCoinsToPouch(player);
            return;
        }
        if (MiningGeode.openAll(player, itemId) || OreBox.check(player, itemId)) {
            return;
        }
        String npcName = "";
        if (player.getContract() != null) {
            npcName = ContractHandler.getFormattedContractName(player);
        }
        if (Firemaking.isFiremaking(player, itemId)) {
            return;
        }
        if (item.getName().equalsIgnoreCase("junk refiner")) {
            JunkRefiner.check(player, item);
            return;
        }
        if (item.getId() >= 30920 && item.getId() <= 30924) {
            if (item.getCharges() == 0) {
                player.sendMessage("There are no charges left in your Silverhawk boots.");
            } else {
                player.sendMessage("There " + (item.getCharges() == 1 ? "is " : "are ") + item.getCharges() + " charges left in your Silverhawk boots.");
            }
            return;
        }
        if (item.getDefinitions().isMaxCape()) {
            if (!player.isMax()) {
                player.sendMessage("<col=ff0000><shad=000000>You are not worthy enough!");
                return;
            }
            Magic.compCapeTeleport(player, 2276, 3315, 1);
            return;
        }
        switch (itemId) {
        case 20047:
            int slot = player.getInventory().getItemSlot(20047);
            if (slot != -1) {
                int amount = player.getInventory().getItem(slot).getAmount();
                player.getInventory().replaceItem(19995, 50 * amount, slot);
                player.sendMessage("You open " + amount + " juju vial pack(s).");
            }
            return;
        case 26492:
            ChristmasSeasonalEvent.checkCmasAmulet(player, item);
            return;
        }
        if (itemId == 47592) {
            ChristmasSeasonalEvent.addToPresentSack(player, item);
            return;
        }
        if (Marionette.doWalk(player, itemId)) {
            return;
        }
        if (item.getDefinitions().containsInventoryOption(1, "Dig")) {
            if (!player.getInventory().containsItem(952, 1)) {
                player.getPackets().sendGameMessage("You need a spade to do that.");
                return;
            }
            dig(player);
            return;
        }
        if (item.getLowercaseName().startsWith("corrupted slayer helmet")) {
            if (player.getTask() == null) {
                player.sendMessage("You currently do not have a slayer task; speak to Kuradal to get one!");
            } else {
                player.sendMessage("You have <col=ff0000><shad=000000>" + player.getTask().getTaskAmount() + " x " + player.getTask().getName(player).toLowerCase() + "'s</col></shad> left to kill.");
            }
            return;
        }
        if (item.getId() == 48480) {
            TriskKeyBag.withdraw(player, item);
            return;
        }
        if (itemId == 3325 && AshScattering.scatter(player, slotId)) {
            return;
        }
        if (PortableStation.isPortableItem(itemId))
            return;
        if (item.getName().contains("Ring of slaying")) {
            if (player.getTask() == null) {
                player.sendMessage("You currently do not have a slayer task; speak to Kuradal to get one!");
            } else {
                player.sendMessage("You have <col=ff0000><shad=000000>" + player.getTask().getTaskAmount() + " x " + player.getTask().getName(player).toLowerCase() + "'s</col></shad> left to kill.");
            }
            return;
        }

        if (player.getGemBag().withdraw(item.getId())) {
            return;
        }
        if (RefinedAnimaCoreRepairD.ConsumableEssenceData.getEssenceItemIds().contains(itemId)) {
            player.sendMessage("Use the essence on the anima core item you want to repair.");
            return;
        }
        if (itemId == 22370) {
            player.selectedMeleeDreadnip = true;
            player.selectedMagicDreadnip = false;
            player.selectedRangedDreadnip = false;
            player.getPackets().sendGameMessage("You have selected melee dreadnips type.");
            return;
        }

        if (item.getName().toLowerCase().contains("full slayer helm")) {
            player.getDialogueManager().startDialogue("Kuradal", 9085);
            return;
        }

        /**
         * New Dungeoneering Token pouches.
         */
        if (itemId == 32747) {
            final int amount = player.getInventory().getAmountOf(itemId);
            player.getInventory().deleteItem(new Item(itemId, amount));
            player.setDungeoneeringTokens(player.getDungeoneeringTokens() + (10 * amount));
            player.sendMessage("You've opened the Dungeoneering Token pouch " + "for " + Utils.getFormattedNumber(10 * amount) + " tokens.");
            return;
        }
        if (itemId == 32748) {
            final int amount = player.getInventory().getAmountOf(itemId);
            player.getInventory().deleteItem(new Item(itemId, amount));
            player.setDungeoneeringTokens(player.getDungeoneeringTokens() + (50 * amount));
            player.sendMessage("You've opened the Dungeoneering Token pouch " + "for " + Utils.getFormattedNumber(50 * amount) + " tokens.");
            return;
        }
        if (itemId == 32749) {
            final int amount = player.getInventory().getAmountOf(itemId);
            player.getInventory().deleteItem(new Item(itemId, amount));
            player.setDungeoneeringTokens(player.getDungeoneeringTokens() + (100 * amount));
            player.sendMessage("You've opened the Dungeoneering Token pouch " + "for " + Utils.getFormattedNumber(100 * amount) + " tokens.");
            return;
        }
        if (itemId == 32750) {
            final int amount = player.getInventory().getAmountOf(itemId);
            player.getInventory().deleteItem(new Item(itemId, amount));
            player.setDungeoneeringTokens(player.getDungeoneeringTokens() + (500 * amount));
            player.sendMessage("You've opened the Dungeoneering Token pouch " + "for " + Utils.getFormattedNumber(500 * amount) + " tokens.");
            return;
        }

        if (item.getId() == 32337) {
            player.getActionManager().setAction(new ProteanHunter(player.getInventory().getAmountOf(32337) > 60 ? 60 : player.getInventory().getAmountOf(32337)));
            return;
        }
        if (item.getId() == 37694) {
            player.getDialogueManager().startDialogue("SkillingMasterD", false);
            return;
        }
        if (item.getId() == 25450) {
            if (player.getContracts().canTalkToAdvancedMaster(true)) {
                player.getDialogueManager().startDialogue("SkillingMasterD", true);
            }
            return;
        }
        if (itemId == 24337) {
            if (player.getInventory().getFreeSlots() < 2) {
                player.sendMessage("Inventory full. Sell, drop or bank something to make space.", true);
                return;
            }
            player.getInventory().deleteItem(24337, 1);
            player.getInventory().addItem(new Item(24340));
            player.getInventory().addItem(new Item(24342));
            player.getInventory().addItem(new Item(24344));
            player.getInventory().addItem(new Item(24346));
            return;
        }
        if (itemId == 15364) {
            final int packs = player.getInventory().getNumberOf(itemId);
            if (player.getInventory().addItem(new Item(222, packs * 50))) {
                player.getInventory().deleteItem(itemId, packs);
                player.sendMessage("You've opened " + Utils.getFormattedNumber(packs) + " eye of newt packs.");
                return;
            }
            player.sendMessage("Inventory full. Sell, drop or bank something to make space.", true);
            return;
        }
        if (itemId == 15363) {
            final int packs = player.getInventory().getNumberOf(itemId);
            if (player.getInventory().addItem(new Item(228, packs * 50))) {
                player.getInventory().deleteItem(itemId, packs);
                player.sendMessage("You've opened " + Utils.getFormattedNumber(packs) + " water vial packs.");
                return;
            }
            player.sendMessage("Inventory full. Sell, drop or bank something to make space.", true);
            return;
        }
        if (itemId == 15262) {
            final int packs = player.getInventory().getNumberOf(15262);
            if (packs > 0) {
                player.addItem(new Item(12183, packs * 5000));
                player.getInventory().deleteItem(15262, packs);
                player.sendMessage("You've opened " + Utils.getFormattedNumber(packs) + " shard packs.");
            }
            return;
        }
        if (item.getId() == 3062) {
            int give = 10;
            int amount = player.getInventory().getAmountOf(3062);
            if (amount < give) {
                give = amount;
            }
            HerbloreBox.open(player, give);
            return;
        }
        if (item.getId() == 15362) {
            val packs = player.getInventory().getNumberOf(15362);
            if (player.getInventory().addItem(230, packs * 50)) {
                player.getInventory().deleteItem(15362, packs);
                if (packs > 1) {
                    player.sendMessage("You open " + packs + " vial packs and receive " + Utils.getFormattedNumber(packs * 50) + " noted vials.");
                }
            } else {
                player.sendMessage("Free up some inventory space before you open " + (packs == 1 ? "this." : "these."), false);
            }
            return;
        }
        if (itemId == 3125) {
            if (player.getSkills().getLevel(Skills.FIREMAKING) < 30) {
                player.sendMessage("You need a firemaking level of 30 to burn Jogre bones. " + "You could find some other way of burning them.");
                return;
            }
            if (!player.getInventory().containsOneItem(590)) {
                player.sendMessage("You will need a Tinderbox in order to light these bones.");
                return;
            }
            player.getInventory().replaceItem(3127, 1, slotId);
            player.getSkills().addXp(Skills.FIREMAKING, 90);
            return;
        }
        if (itemId == 4079) {
            player.setNextAnimationNoPriority(new Animation(1458));
            return;
        }
        if (itemId == 2803) {
            if (player.getTreasureTrails().useDig()) {
                return;
            }
            return;
        }
        if (RingTransformation.tryTransform(player, itemId)) {
            return;
        }
        if (itemId == 42386) {
            player.getDialogueManager().startDialogue("RingOfRaresD");
            return;
        }

        // Withdraw-one from coal bag.
        if (itemId == 18339) {
            Logger.getGlobal().info(player.getCoal());
            if (player.getCoal() == 0) {
                player.sendMessage("You don't have any coal in your coal bag.");
                return;
            }
            if (!player.getInventory().hasFreeSlots()) {
                player.sendMessage("You don't have any space in your inventory.");
                return;
            }
            player.removeCoal(1);
            player.getInventory().addItem(453, 1, false);
            player.sendMessage("You remove 1 coal from your coal bag.");
            return;
        }

        if (itemId >= 33032 && itemId <= 33042) {
            player.getTemporaryAttributtes().put("replenishment", Boolean.TRUE);
            Pots.pot(player, item, slotId);
            return;
        }

        if (itemId == 19044) {
            player.getTreasureTrails().useItem(item, slotId);
            return;
        }
        if (itemId >= 5509 && itemId <= 5514) {
            int pouch = -1;
            if (itemId == 5509) {
                pouch = 0;
            }
            if (itemId == 5510) {
                pouch = 1;
            }
            if (itemId == 5512) {
                pouch = 2;
            }
            if (itemId == 5514) {
                pouch = 3;
            }
            RuneCrafting.emptyPouch(player, pouch);
            player.stopAll(false, true);
        } else if (itemId >= 15086 && itemId <= 15100) {
            Dicing.handleRoll(player, itemId, true);
            return;
        } else {
            if (player.isEquipDisabled()) {
                return;
            }
            Runnable action = () -> {
                final long passedTime = Utils.currentTimeMillis() - WorldThread.WORLD_CYCLE;
                WorldTasksManager.schedule(new WorldTask() {

                    @Override
                    public void run() {
                        final List<Integer> slots = player.getSwitchItemCache();
                        final int[] slot = new int[slots.size()];
                        for (int i = 0; i < slot.length; i++) {
                            slot[i] = slots.get(i);
                        }
                        player.getSwitchItemCache().clear();
                        if (ButtonHandler.sendWear(player, slot))
                            player.stopAll(false, true, false);
                    }
                }, passedTime >= 600 ? 0 : passedTime > 330 ? 1 : 0);
                if (player.getSwitchItemCache().contains(slotId)) {
                    return;
                }
                player.getSwitchItemCache().add(slotId);
            };
//            int targetSlot = Equipment.getItemSlot(itemId);
//            if (targetSlot == Equipment.SLOT_AURA) {
//                player.getAuraManager().removeAura(action);
//            } else {
            action.run();
//            }

        }
    }

    public static void handleItemOption3(final Player player, final int slotId, final int itemId, final Item item) {
        if (Settings.DEBUG) {
            Logger.getGlobal().info("Option 3; slotId: " + slotId + "; itemId: " + itemId + ".");
        }
        String npcName = "";
        final long time = Utils.currentTimeMillis();
        if (player.getLockDelay() >= time || player.getEmotesManager().getNextEmoteEnd() >= time) {
            return;
        }
        if (OreBox.emptyToMetalBank(player, itemId)) {
            return;
        }
        if (player.getToolBelt().addItem(item))
            return;
        if (item.getId() == 40681 || item.getId() == 18340) {
            item.setId(item.getId() == 18340 ? 40681 : 18340);
            player.getInventory().refresh();
            player.getPackets().sendGameMessage("You convert your item into its " + (item.getId() == 40681 ? "pocket" : "shield") + " slot version.");
            return;
        }
        if (item.getId() == 36390) {// divinecharge
            player.sendInputInteger("Enter amount:", new InputIntegerEvent() {

                @Override
                public void run(Player player) {
                    int value = getInteger();
                    if (value <= 0)
                        return;
                    player.getInventionManager().addDivineCharges(value);
                }

            });
            return;
        }
        if (item.getId() == 41073) {
            player.getInventionManager().addEmptyDivineCharges(item.getAmount());
            return;
        }
        if (item.getId() == 48480) {
            TriskKeyBag.check(player, item);
            return;
        }
        if (item.getId() == 41083) {
            player.getInventionManager().checkVaccumCharges();
            return;
        }
        if (Marionette.doBow(player, itemId)) {
            return;
        }
        if (itemId == 47592) {
            ChristmasSeasonalEvent.checkPresentSack(player, item);
            return;
        }
        final FlyingEntities impJar = FlyingEntities.forItem((short) itemId);
        if (impJar != null) {
            FlyingEntityHunter.openJar(player, impJar, slotId);
        }
        if (item.getName().contains("Ring of slaying")) {
            player.getDialogueManager().startDialogue("Kuradal", 9085);
            return;
        }

        if (item.getId() >= 13845 && item.getId() <= 13857) {
            player.sendMessage("Your " + item.getName() + " have " + (int) Math.ceil((double) item.getCharges() / Revenant.getBrawlersCharges(itemId) * 100) + "% of its charges left.");
            return;
        }

        if (itemId == 31846) {
            if (player.getContract() == null) {
                player.sendMessage("You don't have a contract.");
            } else {
                npcName = NPCDefinitions.getNPCDefinitions(player.getContract().getNpcId()).getName();
                player.sendMessage(Colors.ORANGE + "You have " + player.getContract().getKillAmount() + " x " + npcName + "'s left to kill.");
            }
            return;
        }

        if (itemId == 22370) {
            player.selectedMeleeDreadnip = false;
            player.selectedMagicDreadnip = false;
            player.selectedRangedDreadnip = true;
            player.getPackets().sendGameMessage("You have selected range dreadnips type.");
            return;
        }

        if (item.getId() == 21514) {
            player.sendMessage("Your arcane capacitor has " + item.getCharges() + " charges left in it.");
            return;
        }

        if (item.getId() >= 39893 && item.getId() <= 39901) {
            player.getDialogueManager().startDialogue("AttuneGemstoneArmourD");
            return;
        }

        if (player.getGemBag().inspectBag(item.getId())) {
            return;
        }
        if (item.getDefinitions().isBindItem()) {
            player.getDungeoneeringManager().bind(item, slotId);
            // player.getDungeoneeringBinds().bindItem(item);
            return;
        }
        if (itemId == 33262) {
            ShardsBag.fillShards(player);
            return;
        }

        if (itemId >= 38521 && itemId <= 38540) {
            if (ExchangeTrailblazerDialogue.hasAllGorajanSets(player)) {
                for (int i = 38521; i <= 38540; i++) {
                    player.getInventory().deleteItem(i, 1);
                }
                for (int i = 38541; i <= 38545; i++) {
                    if (player.getInventory().hasFreeSlots()) {
                        player.getInventory().addItem(new Item(i, 1));
                    } else {
                        player.getBank().addItem(new Item(i, 1), true);
                    }
                    player.getGorajanTrailblazer().unlockOutfit(i);
                }
            } else {
                player.sendMessage("You haven't got all the required pieces to combine this outfit.");
            }
            return;
        }

        if (itemId == 30912 || itemId == 30913 || itemId == 30914) {
            player.getInventory().deleteItem(item);
            player.addItem(new Item(30915, 50));
            return;
        }
        if (itemId == 37130 || itemId >= 39298 && itemId <= 39307) {
            player.getDialogueManager().startDialogue("ChicScarfD", itemId, false);
            return;
        }

        if (HarpieBugSwarm.handleLitBugLantern(player, item)) {
            return;
        }

        if (itemId == 18342) {
            player.sendMessage("Your law staff currently holds " + player.getLawRunes() + " charges.");
            return;
        }

        if (itemId == 18341) {
            player.sendMessage("Your nature staff currently holds " + player.getNatureRunes() + " charges.");
            return;
        }

        if (itemId == 29634) {
            if (!player.getInventory().hasFreeSlots()) {
                player.sendMessage("You need some free inventory space to do this.");
                return;
            }
            player.getInventory().deleteItem(29634, 1);
            player.getInventory().addItem(18330, 1);
            player.getInventory().addItem(29611, 1);
            player.sendMessage("You dismantle the shieldbow sight from your elder shieldbow.");
            return;
        }
        if (itemId == 18332) {
            if (!player.getInventory().hasFreeSlots()) {
                player.sendMessage("You need some free inventory space to do this.");
                return;
            }
            player.getInventory().deleteItem(18332, 1);
            player.getInventory().addItem(18330, 1);
            player.getInventory().addItem(859, 1);
            player.sendMessage("You dismantle the shieldbow sight from your magic shieldbow.");
            return;
        }
        if (itemId == 18331) {
            if (!player.getInventory().hasFreeSlots()) {
                player.sendMessage("You need some free inventory space to do this.");
                return;
            }
            player.getInventory().deleteItem(18331, 1);
            player.getInventory().addItem(18330, 1);
            player.getInventory().addItem(851, 1);
            player.sendMessage("You dismantle the shieldbow sight from your maple shieldbow.");
            return;
        }

        // Withdraw-many from coal bag.
        if (itemId == 18339) {
            if (player.getCoal() == 0) {
                player.sendMessage("You don't have any coal in your coal bag.");
                return;
            }
            if (!player.getInventory().hasFreeSlots()) {
                player.sendMessage("You don't have any space in your inventory.");
                return;
            }
            player.sendInputInteger("How many coal would you like to remove?", new InputIntegerEvent() {
                @Override
                public void run(final Player player) {
                    int freeSpace = player.getInventory().getFreeSlots();
                    int value = getInteger();

                    // Set to current coal amount.
                    if (value > player.getCoal()) {
                        value = player.getCoal();
                    }
                    // Set to free inventory spaces.
                    if (value > freeSpace) {
                        value = freeSpace;
                    }
                    player.getInventory().addItem(453, value, false);
                    player.removeCoal(value);
                    player.sendMessage("You remove " + value + " coal from your coal bag.");
                }
            });
            return;
        }

        if (itemId == 15707) {
            Magic.daemonheimTeleport(player, new WorldTile(3448, 3699, 0));
        } else if (item.getDefinitions().isRingOfKinship()) {
            player.getRingOfKinship().quickSwitch(item);
            return;
        }

        if (item.getId() >= 30920 && item.getId() <= 30924) {
            player.getDialogueManager().startDialogue("SilverhawkBootsChanging", item);
            return;
        }

        if (itemId == 4155) {
            if (player.getTask() == null) {
                player.sendMessage("You currently do not have a slayer task; speak to Kuradal to get one!");
            } else {
                player.sendMessage("You have <col=ff0000><shad=000000>" + player.getTask().getTaskAmount() + " x " + player.getTask().getName(player).toLowerCase() + "'s</col></shad> left to kill.");
            }
            return;
        }
        if (item.getName().contains("Full slayer helmet")) {
            player.getInterfaceManager().sendInterface(1309);
            return;
        }
        if (item.getName().toLowerCase().contains("kethsi ring")) {
            player.getDialogueManager().startDialogue("KethsiRing", itemId, true);
            return;
        }
        /*
         * Sunglasses
         */
        if (itemId == 34030 || itemId == 34031 || itemId == 34032 || itemId == 34033 || itemId == 34034 || itemId == 34035 || itemId == 34036) {
            player.getDialogueManager().startDialogue(new Dialogue() {

                final String[] options = { "Normal", "Dark blue", "Green", "Light blue", "Red", "Orange", "Black" };
                final ArrayList<String> options_ = new ArrayList<String>();

                @Override
                public void start() {
                    for (int i = 0; i < options.length; i++) {
                        if (i + 34030 != itemId) {
                            options_.add(options[i]);
                        }
                    }
                    sendOptionsDialogue("Choose a color", options_.get(0), options_.get(1), options_.get(2), options_.get(3), "More options..");
                    stage = 0;
                }

                @Override
                public void run(final int interfaceId, final int componentId) {
                    switch (stage) {
                    case 0:
                        int glassesGet = getOrdinal(componentId);
                        switch (componentId) {
                        case OPTION_1:
                        case OPTION_2:
                        case OPTION_3:
                        case OPTION_4:
                            for (int i = 0; i <= options.length - 1; i++) {
                                if (options[i] == options_.get(glassesGet)) {
                                    sendItemDialogue(34030 + i, 1, "You recolored your sunglasses " + options[i] + "!");
                                    player.getInventory().deleteItem(itemId, 1);
                                    player.getInventory().addItem(34030 + i, 1);
                                    stage = 2;
                                }
                            }
                            break;
                        case OPTION_5:
                            sendOptionsDialogue("Choose a color", options_.get(4), options_.get(5), "Previous options..");
                            stage = 1;
                            break;
                        }
                        break;
                    case 1:
                        switch (componentId) {
                        case OPTION_1:
                        case OPTION_2:
                            glassesGet = componentId == 11 ? 4 : 5;
                            for (int i = 0; i <= options.length - 1; i++) {
                                if (options[i] == options_.get(glassesGet)) {
                                    sendItemDialogue(34030 + i, 1, "You recolored your sunglasses " + options[i] + "!");
                                    player.getInventory().deleteItem(itemId, 1);
                                    player.getInventory().addItem(34030 + i, 1);
                                    stage = 2;
                                }
                            }
                            break;
                        case OPTION_3:
                            sendOptionsDialogue("Choose a color", options_.get(0), options_.get(1), options_.get(2), options_.get(3), "More options..");
                            stage = 0;
                            break;
                        }
                        break;
                    }
                }

                @Override
                public void finish() {
                    player.getInterfaceManager().closeChatBoxInterface();
                }

            });
        }
        if (itemId == 31846) {
            if (player.getContract() == null) {
                if (player.isChooseTask()) {
                    player.getDialogueManager().startDialogue("ReapersChoiceD", false);
                    return;
                }
                ContractHandler.assignPlayerNewContract(player);
                npcName = NPCDefinitions.getNPCDefinitions(player.getContract().getNpcId()).getName();
                player.sendMessage(Colors.ORANGE + "Reaper Contract: " + player.getContract().getKillAmount() + "x " + npcName + ".");
            } else {
                player.sendMessage("You already have an active Reaper contract.");
            }
            return;
        }
        if (itemId >= 31392 && itemId <= 31395) {
            final Item[] items = { new Item(31392), new Item(31393), new Item(31394), new Item(31395) };
            if (!player.getInventory().containsItems(items)) {
                player.sendMessage("You will need all 4 Season cloaks in order to combine them.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(31396));
            player.getDialogueManager().startDialogue("SimpleItemMessage", 31396, 1, "You've combined all 4 Season Cloaks into a Cloak of Seasons!");
            player.sendMessage("Congratulations! You've unlocked '" + Colors.GREEN + "<shad=000000> of Seasons</shad></col>' Loyalty Title.");
            player.setCombinedCloaks();
            return;
        }
        if (itemId == 2803) {
            player.getTreasureTrails().useSextant();
            return;
        }
        if (BugLantern.handleLatern(player, item, slotId)) {
            return;
        }
        if (itemId == 19044) {
            if (player.getTreasureTrails().useDig()) {
                return;
            }
            return;
        }
        /**
         * Golem Outfits
         */
        if (itemId == 31575 || itemId == 31580 || itemId == 31585) {
            final Item[] items = { new Item(31575), new Item(31580), new Item(31585) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need a sapphire golem head, " + "an emerald golem head and a ruby golem head in order to combine them into " + "a magic golem head.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(31590));
            return;
        }
        if (itemId == 31576 || itemId == 31581 || itemId == 31586) {
            final Item[] items = { new Item(31576), new Item(31581), new Item(31586) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need a sapphire golem torso, " + "an emerald golem torso and a ruby golem torso in order to combine them into " + "a magic golem torso.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(31591));
            return;
        }
        if (itemId == 31577 || itemId == 31582 || itemId == 31587) {
            final Item[] items = { new Item(31577), new Item(31582), new Item(31587) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need sapphire golem legs, " + "emerald golem legs and ruby golem legs in order to combine them into " + "magic golem legs.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(31592));
            return;
        }
        if (itemId == 31578 || itemId == 31583 || itemId == 31588) {
            final Item[] items = { new Item(31578), new Item(31583), new Item(31588) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need sapphire golem gloves, " + "emerald golem gloves and ruby golem gloves in order to combine them into " + "magic golem gloves.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(31593));
            return;
        }
        if (itemId == 31579 || itemId == 31584 || itemId == 31589) {
            final Item[] items = { new Item(31579), new Item(31584), new Item(31589) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need sapphire golem boots, " + "emerald golem boots and ruby golem boots in order to combine them into " + "magic golem boots.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(31594));
            return;
        }
        /**
         * Ethereal Outfits
         */
        if (itemId == 32342 || itemId == 32347 || itemId == 32352) {
            final Item[] items = { new Item(32342), new Item(32347), new Item(32352) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need a law ethereal hood, " + "a blood ethereal hood and a death ethereal head in order to combine them into " + "an infinity ethereal head.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(32357));
            return;
        }
        if (itemId == 32343 || itemId == 32348 || itemId == 32353) {
            final Item[] items = { new Item(32343), new Item(32348), new Item(32353) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need a law ethereal body, " + "a blood ethereal body and a death ethereal body in order to combine them into " + "an infinity ethereal body.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(32358));
            return;
        }
        if (itemId == 32344 || itemId == 32349 || itemId == 32354) {
            final Item[] items = { new Item(32344), new Item(32349), new Item(32354) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need law ethereal legs, " + "blood ethereal legs and death ethereal legs in order to combine them into " + "infinity ethereal legs.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(32359));
            return;
        }
        if (itemId == 32345 || itemId == 32350 || itemId == 32355) {
            final Item[] items = { new Item(32345), new Item(32350), new Item(32355) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need law ethereal hands, " + "blood ethereal hands and death ethereal hands in order to combine them into " + "infinity ethereal hands.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(32360));
            return;
        }
        if (itemId == 32346 || itemId == 32351 || itemId == 32356) {
            final Item[] items = { new Item(32346), new Item(32351), new Item(32356) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need law ethereal feet, " + "blood ethereal feet and death ethereal feet in order to combine them into " + "infinity ethereal feet.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(32361));
            return;
        }
        /**
         * Shark Outfits
         */
        if (itemId == 34200 || itemId == 34205 || itemId == 34210) {
            final Item[] items = { new Item(34200), new Item(34205), new Item(34210) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need a regular shark head, " + "a burnt shark head and a tiger shark head to combine them into a fury shark head.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(34215));
            return;
        }
        if (itemId == 34201 || itemId == 34206 || itemId == 34211) {
            final Item[] items = { new Item(34201), new Item(34206), new Item(34211) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need a regular shark body, " + "a burnt shark body and a tiger shark body to combine them into a fury shark body.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(34216));
            return;
        }
        if (itemId == 34202 || itemId == 34207 || itemId == 34212) {
            final Item[] items = { new Item(34202), new Item(34207), new Item(34212) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need regular shark legs, " + "burnt shark legs and tiger shark legs to combine them into fury shark legs.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(34217));
            return;
        }
        if (itemId == 34203 || itemId == 34208 || itemId == 34213) {
            final Item[] items = { new Item(34203), new Item(34208), new Item(34213) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need regular shark hands, " + "burnt shark hands and tiger shark hands to combine them into fury shark hands.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(34218));
            return;
        }
        if (itemId == 34204 || itemId == 34209 || itemId == 34214) {
            final Item[] items = { new Item(34204), new Item(34209), new Item(34214) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need regular shark feet, " + "burnt shark feet and tiger shark feet to combine them into fury shark feet.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(34219));
            return;
        }
        /**
         * Divine simulacrum Outfits
         */
        if (itemId == 35963 || itemId == 35968 || itemId == 35973) {
            final Item[] items = { new Item(35963), new Item(35968), new Item(35973) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need a divination energy head, " + "a divination chronicle head and a divination memory head in order to combine them into " + "an elder divination head.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(35978));
            return;
        }
        if (itemId == 35964 || itemId == 35969 || itemId == 35974) {
            final Item[] items = { new Item(35964), new Item(35969), new Item(35974) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need a divination energy body, " + "a divination chronicle body and a divination memory body in order to combine them into " + "an elder divination body.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(35979));
            return;
        }
        if (itemId == 35965 || itemId == 35970 || itemId == 35975) {
            final Item[] items = { new Item(35965), new Item(35970), new Item(35975) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need divination energy legs, " + "divination chronicle legs and divination memory legs in order to combine them into " + "elder divination legs.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(35980));
            return;
        }
        if (itemId == 35966 || itemId == 35971 || itemId == 35976) {
            final Item[] items = { new Item(35966), new Item(35971), new Item(35976) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need divination energy hands, " + "divination chronicle hands and divination memory hands in order to combine them into " + "elder divination hands.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(35981));
            return;
        }
        if (itemId == 35967 || itemId == 35972 || itemId == 35977) {
            final Item[] items = { new Item(35967), new Item(35972), new Item(35977) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need divination energy feet, " + "divination chronicle feet and divination memory feet in order to combine them into " + "elder divination feet.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(35982));
            return;
        }
        /**
         * Elite Outfits handling - only Sentinel is supported
         */
        if (EliteOutfits.isComponent(itemId)) {
            final Sentinel outfit = Sentinel.getOutfit(item.getName().substring(0, item.getName().indexOf(" ")));
            final String piece = outfit.getPiece(item.getId());
            final Item reward = new Item(Sentinel.getOutfit("nature's").getPiece(piece), 1);
            final Item[] components = { new Item(Sentinel.getOutfit("oaken").getPiece(piece), 1), new Item(Sentinel.getOutfit("willow").getPiece(piece), 1), new Item(Sentinel.getOutfit("maple").getPiece(piece), 1) };
            if (player.getInventory().containsItems(components)) {
                player.getInventory().removeItems(components);
                player.getInventory().addItem(reward);
                player.getDialogueManager().startDialogue("SimpleItemMessage", reward.getId(), 1, Colors.BLUE + Colors.SHAD + "You have combined your items into " + Utils.getAorAn(reward.getName()) + " " + reward.getName() + "!");
            } else {
                player.getDialogueManager().startDialogue("SimpleItemMessage", reward.getId(), 1, Colors.SALMON + Colors.SHAD + "You need a " + components[0].getName() + ", a " + components[1].getName() + ", and a " + components[2].getName() + " to create this!");
            }
            return;
        }
        /*
         * Master camouflage outfit combining
         */
        if (itemId == 37353 || itemId == 37348 || itemId == 37343) {
            final Item[] items = { new Item(37353), new Item(37343), new Item(37348) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need a Desert camouflage head, " + "a Prifddinas camouflage head and a Keldagrim camouflage head in order to combine them into " + "an Master camoflauge head.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(37358));
            return;
        }
        if (itemId == 37349 || itemId == 37354 || itemId == 37344) {
            final Item[] items = { new Item(37349), new Item(37344), new Item(37354) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need a Desert camouflage body, " + "a Prifddinas camouflage body and a Keldagrim camouflage body in order to combine them into " + "an Master camoflauge body.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(37359));
            return;
        }
        if (itemId == 37345 || itemId == 37350 || itemId == 37355) {
            final Item[] items = { new Item(37345), new Item(37350), new Item(37355) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need Desert camouflage legs, " + "Prifddinas camouflage legs, and Keldagrim camouflage legs in order to combine them into " + "Master camoflauge legs.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(37360));
            return;
        }
        if (itemId == 37356 || itemId == 37351 || itemId == 37346) {
            final Item[] items = { new Item(37356), new Item(37351), new Item(37346) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need Desert camouflage gloves, " + "Prifddinas camouflage gloves and Keldagrim camouflage gloves in order to combine them into " + "Master camoflauge gloves.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(37361));
            return;
        }
        if (itemId == 37352 || itemId == 37357 || itemId == 37347) {
            final Item[] items = { new Item(37352), new Item(37357), new Item(37347) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need Desert camouflage feet, " + "Prifddinas camouflage feet and Keldagrim camouflage feet in order to combine them into " + "Master camoflauge feet.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(37362));
            return;
        }
        /** Hunter outfit combinations */
        if (itemId == 41008 || itemId == 41013 || itemId == 41018) {
            final Item[] items = { new Item(41008), new Item(41013), new Item(41018) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need a Desert trapper head, " + "a Jungle trapper head and an Arctic trapper head in order to combine them into " + "a Volcanic trapper head.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(41023));
            return;
        }
        if (itemId == 41009 || itemId == 41014 || itemId == 41019) {
            final Item[] items = { new Item(41009), new Item(41014), new Item(41019) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need a Desert trapper body, " + "a Jungle trapper body and an Arctic trapper body in order to combine them into " + "a Volanic trapper body.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(41024));
            return;
        }
        if (itemId == 41010 || itemId == 41015 || itemId == 41020) {
            final Item[] items = { new Item(41010), new Item(41015), new Item(41020) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need Desert trapper legs, " + "Jungle trapper legs, and Arctic trapper legs in order to combine them into " + "Volcanic trapper legs.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(41025));
            return;
        }
        if (itemId == 41011 || itemId == 41016 || itemId == 41021) {
            final Item[] items = { new Item(41011), new Item(41016), new Item(41021) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need Desert trapper gloves, " + "Jungle trapper gloves and Arctic trapper gloves in order to combine them into " + "Volcanic trapper gloves.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(41026));
            return;
        }
        if (itemId == 41012 || itemId == 41017 || itemId == 41022) {
            final Item[] items = { new Item(41012), new Item(41017), new Item(41022) };
            if (!player.getInventory().containsItems(items)) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You'll need Desert trapper feet, " + "Jungle trapper feet and Arctic trapper feet in order to combine them into " + "Volcanic trapper feet.");
                return;
            }
            player.getInventory().removeItems(items);
            player.getInventory().addItem(new Item(41027));
            return;
        }
        if (item.getDefinitions().containsOption("Check limit")) {
            player.sendMessage(Colors.DARK_RED + "You've currently gathered " + DivineObject.checkPercentage(player) + "% of your daily divine resources.");
            final long timeVariation = Utils.currentTimeMillis() - player.lastGatherLimit;
            if (timeVariation < (24 * 60 * 60 * 1000)) { // 24 hours
                final long toWait = (24 * 60 * 60 * 1000) - (Utils.currentTimeMillis() - player.lastGatherLimit);
                player.sendMessage(Colors.DARK_RED + "Your Divine resource limitation will reset in " + Utils.millisecsToMinutes(toWait) + " minutes.");
                return;
            }
            player.gathered = 0;
            player.lastGatherLimit = Utils.currentTimeMillis();
            return;
        }
        if (PrayerBooks.isGodBook(itemId, true)) {
            PrayerBooks.sermanize(player, itemId);
            return;
        }
        if (itemId == 1963) {
            player.getInventory().deleteItem(1963, 1);
            player.getInventory().addItem(3162, 1);
            player.sendMessage("You use your knife to slice the banana into sliced banana.");
            return;
        }
        if (itemId == 15707) {
            Magic.daemonheimTeleport(player, new WorldTile(3448, 3699, 0));
            return;
        }
        if (item.getDefinitions().containsEquipmentOption("Customise") && SkillCapeCustomizer.isCustomizable(itemId)) {
            SkillCapeCustomizer.startCustomizing(player, itemId);
            return;
        }
        if (itemId >= 15084 && itemId <= 15100) {
            player.getDialogueManager().startDialogue("DiceBag", itemId);
            return;
        }
        if (itemId == 24437 || itemId == 24439 || itemId == 24440 || itemId == 24441) {
            player.getDialogueManager().startDialogue("FlamingSkull", item, slotId);
            return;
        }
        if (Equipment.getItemSlot(itemId) == Equipment.SLOT_AURA) {
            player.getAuraManager().sendTimeRemaining(itemId);
            return;
        }
        // ags
        if (itemId == 11694) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to dismantle your godsword!");
                return;
            }
            player.getInventory().deleteItem(11694, 1);
            player.getInventory().addItem(11702, 1);
            player.getInventory().addItem(11690, 1);
        }

        // dragonbone mage hat
        if (itemId == 24354) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove the upgrade kit!");
                return;
            }
            player.getInventory().deleteItem(24354, 1);
            player.getInventory().addItem(24352, 1);
            player.getInventory().addItem(6918, 1);
        }
        // dragonbone mage top
        if (itemId == 24355) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove the upgrade kit!");
                return;
            }
            player.getInventory().deleteItem(24355, 1);
            player.getInventory().addItem(24352, 1);
            player.getInventory().addItem(6916, 1);
        }
        // dragonbone mage bot
        if (itemId == 24356) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove the upgrade kit!");
                return;
            }
            player.getInventory().deleteItem(24356, 1);
            player.getInventory().addItem(24352, 1);
            player.getInventory().addItem(6924, 1);
        }
        // dragonbone mage boots
        if (itemId == 24358) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove the upgrade kit!");
                return;
            }
            player.getInventory().deleteItem(24358, 1);
            player.getInventory().addItem(24352, 1);
            player.getInventory().addItem(6920, 1);
        }
        // dragonbone mage gloves
        if (itemId == 24357) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove the upgrade kit!");
                return;
            }
            player.getInventory().deleteItem(24357, 1);
            player.getInventory().addItem(24352, 1);
            player.getInventory().addItem(6922, 1);
        }
        // dragonbone full helm
        if (itemId == 24359) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove the upgrade kit!");
                return;
            }
            player.getInventory().deleteItem(24359, 1);
            player.getInventory().addItem(24352, 1);
            player.getInventory().addItem(11335, 1);
        }
        // dragonbone plate
        if (itemId == 24360) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove the upgrade kit!");
                return;
            }
            player.getInventory().deleteItem(24360, 1);
            player.getInventory().addItem(24352, 1);
            player.getInventory().addItem(14479, 1);
        }
        // dragonbone legs
        if (itemId == 24363) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove the upgrade kit!");
                return;
            }
            player.getInventory().deleteItem(24363, 1);
            player.getInventory().addItem(24352, 1);
            player.getInventory().addItem(4087, 1);
        }
        // dragonbone skirt
        if (itemId == 24364) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove the upgrade kit!");
                return;
            }
            player.getInventory().deleteItem(24364, 1);
            player.getInventory().addItem(24352, 1);
            player.getInventory().addItem(4585, 1);
        }
        // dragonbone boots
        if (itemId == 24362) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove the upgrade kit!");
                return;
            }
            player.getInventory().deleteItem(24362, 1);
            player.getInventory().addItem(24352, 1);
            player.getInventory().addItem(11732, 1);
        }
        // dragonbone gloves
        if (itemId == 24361) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove the upgrade kit!");
                return;
            }
            player.getInventory().deleteItem(24361, 1);
            player.getInventory().addItem(24352, 1);
            player.getInventory().addItem(7461, 1);
        }
        // abyssal vine whip
        if (itemId == 21371) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove the whip vine!");
                return;
            }
            player.getInventory().deleteItem(21371, 1);
            player.getInventory().addItem(4151, 1);
            player.getInventory().addItem(21369, 1);
        }
        // Dragon full helm (or)
        if (itemId == 19336) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove your ornament kit!");
                return;
            }
            player.getInventory().deleteItem(19336, 1);
            player.getInventory().addItem(11335, 1);
            player.getInventory().addItem(19346, 1);
        }
        // Dragon platebody (or)
        if (itemId == 19337) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove your ornament kit!");
                return;
            }
            player.getInventory().deleteItem(19337, 1);
            player.getInventory().addItem(14479, 1);
            player.getInventory().addItem(19350, 1);
        }
        // Dragon platelegs (or)
        if (itemId == 19338) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove your ornament kit!");
                return;
            }
            player.getInventory().deleteItem(19338, 1);
            player.getInventory().addItem(4087, 1);
            player.getInventory().addItem(19348, 1);
        }
        // Dragon plateskirt (or)
        if (itemId == 19339) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove your ornament kit!");
                return;
            }
            player.getInventory().deleteItem(19339, 1);
            player.getInventory().addItem(4585, 1);
            player.getInventory().addItem(19348, 1);
        }
        // Dragon sq shield (or)
        if (itemId == 19340) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove your ornament kit!");
                return;
            }
            player.getInventory().deleteItem(19340, 1);
            player.getInventory().addItem(1187, 1);
            player.getInventory().addItem(19352, 1);
        }
        // Dragon full helm (sp)
        if (itemId == 19341) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove your ornament kit!");
                return;
            }
            player.getInventory().deleteItem(19341, 1);
            player.getInventory().addItem(11335, 1);
            player.getInventory().addItem(19354, 1);
        }
        // Dragon platebody (sp)
        if (itemId == 19342) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove your ornament kit!");
                return;
            }
            player.getInventory().deleteItem(19342, 1);
            player.getInventory().addItem(14479, 1);
            player.getInventory().addItem(19358, 1);
        }
        // Dragon platelegs (sp)
        if (itemId == 19343) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove your ornament kit!");
                return;
            }
            player.getInventory().deleteItem(19343, 1);
            player.getInventory().addItem(4087, 1);
            player.getInventory().addItem(19356, 1);
        }
        // Dragon plateskirt (sp)
        if (itemId == 19344) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove your ornament kit!");
                return;
            }
            player.getInventory().deleteItem(19344, 1);
            player.getInventory().addItem(4585, 1);
            player.getInventory().addItem(19356, 1);
        }
        // Dragon sq shield (sp)
        if (itemId == 19345) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove your ornament kit!");
                return;
            }
            player.getInventory().deleteItem(19345, 1);
            player.getInventory().addItem(1187, 1);
            player.getInventory().addItem(19360, 1);
        }
        if (itemId == 4079) {
            player.setNextAnimationNoPriority(new Animation(1459));
            return;
        }
        // Dragon kiteshield (sp)
        if (itemId == 25321) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove your ornament kit!");
                return;
            }
            player.getInventory().deleteItem(item);
            player.getInventory().addItem(24365, 1);
            player.getInventory().addItem(25314, 1);
        }
        // Dragon kiteshield (or)
        if (itemId == 25320) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove your ornament kit!");
                return;
            }
            player.getInventory().deleteItem(item);
            player.getInventory().addItem(24365, 1);
            player.getInventory().addItem(25312, 1);
        }
        if (itemId == 19335) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to remove your ornament kit!");
                return;
            }
            player.getInventory().deleteItem(19335, 1);
            player.getInventory().addItem(6585, 1);
            player.getInventory().addItem(19333, 1);
        }
        if (itemId == 11696) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to dismantle your godsword!");
                return;
            }
            player.getInventory().deleteItem(11696, 1);
            player.getInventory().addItem(11704, 1);
            player.getInventory().addItem(11690, 1);
            return;
        }
        if (itemId == 11698) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to dismantle your godsword!");
                return;
            }
            player.getInventory().deleteItem(11698, 1);
            player.getInventory().addItem(11706, 1);
            player.getInventory().addItem(11690, 1);
            return;
        }
        if (itemId == 11700) {
            if (player.getInventory().getFreeSlots() < 1) {
                player.sendMessage("You need at least 1 free empty inventory slots to dismantle your godsword!");
                return;
            }
            player.getInventory().deleteItem(11700, 1);
            player.getInventory().addItem(11708, 1);
            player.getInventory().addItem(11690, 1);
            return;
        }
        if (itemId == Ectoplasmator.ATTUNED_ECTOPLASMATOR_ID) {
            player.sendMessage("Your attuned ectoplasmator has " + Colors.RED + player.ectoCharges + "</col> charges left.");
            return;
        }
        if (itemId == Ectoplasmator.DEGRADED_ATTUNED_ECTOPLASMATOR_ID) {
            player.sendMessage("This ectoplasmator has degraded.");
            return;
        }

        if (player.getInventionManager().checkAugmentedItem(slotId, item, false) || player.getChargesManagerNew().checkCharges(item)/* || player.getCharges().checkCharges(item) */) {
            return;
        }

    }

    public static void handleItemOption4(final Player player, final int slotId, final int itemId, final Item item) {
        if (Settings.DEBUG) {
            Logger.getGlobal().info("Option 4; slotId: " + slotId + "; itemId: " + itemId + ".");
        }

        if (itemId == 18778) {
            player.sendMessage("It sounds like something is contained within this relic.");
            return;
        }
    }

    public static void handleItemOption5(final Player player, final int slotId, final int itemId, final Item item) {
        if (Settings.DEBUG) {
            Logger.getGlobal().info("Option 5; slotId: " + slotId + "; itemId: " + itemId + ".");
        }
    }

    public static void handleItemOption6(final Player player, final int slotId, final int itemId, final Item item) {
        if (Settings.DEBUG) {
            Logger.getGlobal().info("Option 6; slotId: " + slotId + "; itemId: " + itemId + ".");
        }
        final long time = Utils.currentTimeMillis();
        if (player.getLockDelay() >= time || player.getEmotesManager().getNextEmoteEnd() >= time) {
            return;
        }
        if (item.getId() == 48480) {
            TriskKeyBag.toggleWithdrawToBank(player, item);
            return;
        }
        if (item.getId() == 36720 || item.getId() == 36722 || item.getId() == 36724) {
            player.getInventionManager().openCheckPerksInterface(item);
            return;
        }
        if (Marionette.doDance(player, itemId)) {
            return;
        }
        String name = item.getName().toLowerCase();
        if ((name.contains("hammer-tron") || name.contains("pyro-matic") || name.contains("fishing rod-o-matic")) && item.getInventionData() != null) {
            if (item.getInventionData().getXp() > 0 || item.getInventionData().getGizmosCount() > 0) {
                player.getPackets().sendGameMessage("You can only convert this if it has no experience or gizmos attached to it.");
                return;
            }
            int originalId = item.getUnAugmentedItemId();
            item.setId(originalId);
            item.setInventionData(null);
            player.getInventory().refresh();
            player.getPackets().sendGameMessage("You convert your " + item.getName() + " back to its stackable form.");
            return;
        }
        if ((name.contains("augmented dragon hatchet")) && item.getInventionData() != null) {
            boolean combatType = item.getId() >= 36543 && item.getId() <= 36544;
            item.setId(combatType ? item.getId() + 412 : item.getId() - 412);
            player.getInventory().refresh();
            player.getPackets().sendGameMessage("You convert your " + item.getName() + " to its " + (combatType ? "skilling mode" : "combat mode") + ".");
            return;
        }
        if ((name.contains("augmented crystal hatchet")) && item.getInventionData() != null) {
            boolean combatType = item.getId() >= 36545 && item.getId() <= 36546;
            item.setId(combatType ? item.getId() + 412 : item.getId() - 412);
            player.getInventory().refresh();
            player.getPackets().sendGameMessage("You convert your " + item.getName() + " to its " + (combatType ? "skilling mode" : "combat mode") + ".");
            return;
        }
        if ((name.contains("augmented dragon pickaxe")) && item.getInventionData() != null) {
            boolean combatType = item.getId() >= 36537 && item.getId() <= 36538;
            item.setId(combatType ? item.getId() + 1627 : item.getId() - 1627);
            player.getInventory().refresh();
            player.getPackets().sendGameMessage("You convert your " + item.getName() + " to its " + (combatType ? "skilling mode" : "combat mode") + ".");
            return;
        }
        if (item.getId() == 36390) {
            player.getInventionManager().addDivineCharges(item.getAmount());
            return;
        }
        if (item.getId() == 41083) {
            player.getInventionManager().withdrawDivineCharges();
            return;
        }
        if (itemId == 38451 || itemId == 38453) {
            RunePouch.empty(player, itemId == 38451);
            return;
        }

        if (itemId == 31846) {
            player.getDialogueManager().startDialogue("GrimGem");
            return;
        }

        if (itemId == 22370) {
            player.selectedMeleeDreadnip = false;
            player.selectedMagicDreadnip = true;
            player.selectedRangedDreadnip = false;
            player.getPackets().sendGameMessage("You have selected magic dreadnips type.");
            return;
        }

        if (itemId == 4079) {
            player.setNextAnimationNoPriority(new Animation(1460));
            return;
        }
        if (itemId == 4155) {
            player.getInterfaceManager().sendInterface(1309);
            return;
        }
        if (itemId == 13263) {
            SlayerHelm.disassemble(player);
        }
        if (item.getDefinitions().isBindItem() && !item.getDefinitions().containsOption("Add to tool belt")) {
            player.getDungeoneeringManager().bind(item, slotId);
            // player.getDungeoneeringBinds().bindItem(item);
            return;
        }

        if (item.getId() >= 39893 && item.getId() <= 39901) {
            player.getDialogueManager().startDialogue("GemstoneChargesD");
            return;
        }
        if (item.getId() == 18338) {
            player.getDialogueManager().startDialogue("GemBagEmptyD");
            return;
        }
        if (item.getId() == 18342) {
            final boolean canAdd = player.getInventory().hasFreeSlots() || !player.getInventory().hasFreeSlots() && player.getInventory().containsCoins(563);
            if (!canAdd) {
                player.sendMessage("You need some free inventory space to do this.");
                return;
            }
            if (player.getLawRunes() == 0) {
                player.sendMessage("Your staff is empty.");
                return;
            }
            player.getInventory().addItem(563, player.getLawRunes());
            player.setLawRunes(0);
            player.sendMessage("You empty the law staff from its charges.");
            return;
        }
        if (item.getId() == 18341) {
            final boolean canAdd = player.getInventory().hasFreeSlots() || !player.getInventory().hasFreeSlots() && player.getInventory().containsCoins(561);
            if (!canAdd) {
                player.sendMessage("You need some free inventory space to do this.");
                return;
            }
            if (player.getNatureRunes() == 0) {
                player.sendMessage("Your staff is empty.");
                return;
            }
            player.getInventory().addItem(561, player.getNatureRunes());
            player.setNatureRunes(0);
            player.sendMessage("You empty the nature staff from its charges.");
            return;
        }
        if (player.getControlerManager().getControler() instanceof DungeonController && item.getDefinitions().containsOption(3, "Drop-X")) {
            player.sendInputInteger("Enter the amount how many you wish to drop:", new InputIntegerEvent() {
                @Override
                public void run(final Player player) {
                    int value = getInteger();
                    if (value != 0) {
                        if (value > player.getInventory().getAmountOf(item.getId())) {
                            value = player.getInventory().getAmountOf(item.getId());
                        }
                        player.getInventory().deleteItem(new Item(item.getId(), value));
                        World.permanentlyAddGroundItem(new Item(item.getId(), value), new WorldTile(player));
                    }
                }
            });
            return;
        }
        if (item.getDefinitions().containsOption(3, "Convert") && item.getName().contains("Protean")) {
            player.getDialogueManager().startDialogue("ProteanConvertingD", item);
            return;
        }

        if (item.getId() >= 30920 && item.getId() <= 30924) {
            if (player.isUnderCombat()) {
                player.sendMessage("You need to be out of combat to do this emote.");
                return;
            }
            player.lock(15);
            player.stopAll();
            player.setNextAnimation(new Animation(22794));
            player.setNextGraphics(new Graphics(4604));
            player.setNextGraphics(new Graphics(4603));
            return;
        }

        if (itemId == 34205 || itemId == 34200 || itemId == 34210 || itemId == 34215) {
            player.getDialogueManager().startDialogue("SharkConsumeOption");
            return;
        }
        if (item.getName().toLowerCase().contains("kethsi ring")) {
            player.sendMessage("You cannot recharge this ring.");
            return;
        }
        if (Pots.emptyPot(player, item, slotId)) {
            return;
        }
        if (ShadeSkull.detachSkull(player, item)) {
            return;
        }
        if (itemId == 11113) {
            player.sendMessage("Your Skills necklace has ran out of charges.");
            return;
        }
        if (item.getDefinitions().containsOption("Powder") || item.getDefinitions().containsOption("Grind")) {
            final int herbloreVersa = Herblore.isHerbloreSkill(new Item(233), item);
            if (herbloreVersa > -1) {
                player.getDialogueManager().startDialogue("HerbloreD", herbloreVersa, item, new Item(233), false);
                return;
            }
        }
        if (item.getDefinitions().isCompletionistCape()) {
            player.getDialogueManager().startDialogue("CompCapeD");
            return;
        }
        if (item.getName().contains("Ring of slaying")) {
            player.getInterfaceManager().sendInterface(1309);
            return;
        }
        if (itemId == 15492) {
            if (player.getInventory().getFreeSlots() >= 2) {
                player.getInventory().deleteItem(15492, 1);
                player.getInventory().addItem(13263, 1);
                player.getInventory().addItem(15490, 1);
                player.getInventory().addItem(15488, 1);
                player.sendMessage("You carefully modify the slayer helmet so the extra pieces come off.");
                return;
            }
            player.sendMessage("You will need 2 free inventory slots to do this.");
            return;
        }
        if (item.getName().contains("Bucket of") || item.getName().contains("ompost")) {
            player.getInventory().deleteItem(itemId, 1);
            player.getInventory().addItem(1925, 1);
            player.sendMessage("You empty the contents on the floor.");
            return;
        }
        if (item.getName().startsWith("Super ") || itemId == 6036) {
            player.getInventory().deleteItem(itemId, 1);
            player.getInventory().addItem(229, 1);
            player.sendMessage("You empty the contents on the floor.");
            return;
        }
        if (itemId == 15707 || item.getDefinitions().isRingOfKinship()) {
            player.getRingOfKinship().openInterface(item);
            return;
        }
        if (itemId == 13263) {
            player.sendMessage("Now why on " + Settings.SERVER_NAME + " would I do this..?");
            return;
        }
        if (player.getToolBelt().addItem(item)) {
            return;
        }
        if (player.getDungeoneeringToolbelt().addTool(item)) {
            return;
        } else if (itemId == 1438) {
            RuneCrafting.locate(player, 3127, 3405);
        } else if (itemId == 1440) {
            RuneCrafting.locate(player, 3306, 3474);
        } else if (itemId == 1442) {
            RuneCrafting.locate(player, 3313, 3255);
        } else if (itemId == 1444) {
            RuneCrafting.locate(player, 3185, 3165);
        } else if (itemId == 1446) {
            RuneCrafting.locate(player, 3053, 3445);
        } else if (itemId == 1448) {
            RuneCrafting.locate(player, 2982, 3514);
        } else if (itemId == 2572) {
            player.getInterfaceManager().sendBossKillLog();
        } else if (itemId >= 20653 && itemId <= 20659) {
            player.getDialogueManager().startDialogue("Transportation3", "Miscellania", new WorldTile(2581, 3845, 0), "Grand Exchange", new WorldTile(3164, 3468, 0), itemId);
        } else if (itemId <= 1712 && itemId >= 1706 || itemId >= 10354 && itemId <= 10362) {
            player.getDialogueManager().startDialogue("Transportation", "Edgeville", new WorldTile(3087, 3496, 0), "Karamja", new WorldTile(2918, 3176, 0), "Draynor Village", new WorldTile(3104, 3263, 0), "Al Kharid", new WorldTile(3293, 3163, 0), itemId);
        } else if (itemId == 995) {
            addInventoryCoinsToPouch(player);
        } else if (itemId == 1704 || itemId == 10352) {
            player.getPackets().sendGameMessage("The amulet has ran out of charges.");
        } else if (itemId >= 3853 && itemId <= 3867) {
            player.getDialogueManager().startDialogue("Transportation", "Burthrope Games Room", new WorldTile(2880, 3559, 0), "Barbarian Outpost", new WorldTile(2519, 3571, 0), "Gamers' Grotto", new WorldTile(2970, 9679, 0), "Corporeal Beast", new WorldTile(2886, 4377, 0), itemId);
        }
        if (itemId >= 11118 && itemId <= 11124) {
            player.getDialogueManager().startDialogue("Transportation", "Warriors' Guild", new WorldTile(2880, 3542, 0), "Champions' Guild", new WorldTile(3191, 3367, 0), "Monastery", new WorldTile(3051, 3491, 0), "Ranging Guild", new WorldTile(2655, 3441, 0), itemId);
            return;
        }
        if (itemId >= 2552 && itemId <= 2566) {
            player.getDialogueManager().startDialogue("Transportation", "Duel Arena", new WorldTile(3315, 3234, 0), "Castle Wars", new WorldTile(2442, 3088, 0), "Mobilising Armies", new WorldTile(2413, 2848, 0), "Fist of Guthix", new WorldTile(1679, 5599, 0), itemId);
            return;
        }
        if (itemId >= 11105 && itemId <= 11111) {
            player.getDialogueManager().startDialogue("Transportation", "Fishing Guild", new WorldTile(2614, 3382, 0), "Mining Guild", new WorldTile(3021, 3339, 0), "Crafting Guild", new WorldTile(2933, 3296, 0), "Cooking Guild", new WorldTile(3142, 3440, 0), itemId);
            return;
        }
        if (itemId == 1704 || itemId == 10362) {
            player.sendMessage("The amulet has ran out of charges.");
            return;
        }
    }

    public static void handleItemOption7(final Player player, final int slotId, final int itemId, final Item item) {
        if (Settings.DEBUG) {
            Logger.getGlobal().info("Option 7; slotId: " + slotId + "; itemId: " + itemId + ".");
        }
        final long time = Utils.currentTimeMillis();
        if (player.getLockDelay() >= time || player.getEmotesManager().getNextEmoteEnd() >= time) {
            return;
        }
        if (!player.getControlerManager().canDropItem(item)) {
            return;
        }
        player.stopAll(false);
        if (item.getInventionData() != null) {
            player.getActionManager().setAction(new Disassemble(item));
            return;
        }
        if (player.getAuraManager().isAura(itemId)) {
            if (player.getAuraManager().checkAddAura(itemId))
                player.getInventory().deleteItem(slotId, item);
            return;
        }
        if (LendingManager.isLendedItem(player, item)) {
            final Lend lend = LendingManager.getLend(player);
            if (lend != null && lend.getItem().getDefinitions().getLendId() == item.getId()) {
                player.getDialogueManager().startDialogue("DiscardLend", lend);
            }
            return;
        }

        Pets pet = Pets.forId(itemId);
        if (pet != null) {
            PetPerkUtils.redeemPet(player, pet);
            return;
        }
        /*
         * if (player.getPetManager().spawnPet(itemId, true)) { return; }
         */

        if (item.getDefinitions().isOverSized()) {
            player.sendMessage(Colors.RED + "The item appears to be oversized.", true);
            player.getInventory().deleteItem(item);
            return;
        }
        if (!ItemConstants.isTradeable(item) || item.getDefinitions().isDestroyItem()) {
            player.getDialogueManager().startDialogue("DestroyItemOption", slotId, item);
            return;
        }
        if (player.getSkills().getTotalLevel(player) < 50) {
            player.sendMessage("You need at least a total level of 50 to do this.");
            return;
        }
        if (player.getUsername().equalsIgnoreCase("youtube")) {
            return;
        }
        player.getInventory().deleteItem(slotId, item);
        World.updateGroundItem(item, new WorldTile(player), player, 60, 0, false);
        LoggingSystem.logItemDrop(player, item, player);
//        player.getPackets().sendSound(2739, 0, 1);
    }

    public static void handleItemOption8(final Player player, final int interfaceId, final int componentId, final int slotId, final int itemId, final Item item) {
        // Print itemId to console
        System.out.println("Item ID: " + itemId);

        if (Settings.DEBUG) {
            Logger.getGlobal().info("Option 8; slotId: " + slotId + "; itemId: " + itemId + "; attributes: " + item.getAttributes() + ".");
        }

        if (Smithing.sendUnfinishedProjectDetails(player, item))
            return;

        StringBuilder examineMessage = new StringBuilder();

        // GE price for tradeables (green)
        if (ItemConstants.isTradeable(item)) {
            int geEach = GrandExchange.getPrice(item.getId());
            int amount = item.getAmount();
            long geStack = (long) geEach * (long) amount;

            examineMessage.append("<col=00ff00>GE: ")
                    .append(Utils.formatNumber(geEach)).append(" gp");
            if (amount > 1) {
                examineMessage.append(" (x")
                        .append(Utils.formatNumber(amount))
                        .append(" = ")
                        .append(Utils.formatNumber(geStack))
                        .append(" gp)");
            }
            examineMessage.append("</col>");
        }

// Alch values (gold)
        try {
            int highAlchEach = EconomyPrices.getAlchCoins(item, EconomyPrices.AlchTier.HIGH);
            int amount = item.getAmount();
            long highAlchStack = (long) highAlchEach * (long) amount;

            //examineMessage.append(" | <col=ffd700>Alch Price: ")
                   // .append(Utils.formatNumber(highAlchEach)).append(" gp");
            if (amount > 1) {
                examineMessage.append(" (x")
                        .append(Utils.formatNumber(amount))
                        .append(" = ")
                        .append(Utils.formatNumber(highAlchStack))
                        .append(" gp)");
            }
            examineMessage.append("</col>");

            // If you also want to show low alch, uncomment:
            // int lowAlchEach = getAlchPrice(item, true);
            // long lowAlchStack = (long) lowAlchEach * (long) amount;
            // examineMessage.append(" | <col=ffd700>Low Alch: ")
            //     .append(Utils.formatNumber(lowAlchEach)).append(" gp")
            //     .append(amount > 1 ? " (x" + Utils.formatNumber(amount) + " = " + Utils.formatNumber(lowAlchStack) + " gp)" : "")
            //     .append("</col>");

        } catch (Exception e) {
            if (Settings.DEBUG) e.printStackTrace();
        }


        // Examine text (white)
        examineMessage.append(" | ").append(ItemExaminesDataParser.getExamine(item));

        // Send combined message
        player.getPackets().sendGameMessage(examineMessage.toString());
    }



}
