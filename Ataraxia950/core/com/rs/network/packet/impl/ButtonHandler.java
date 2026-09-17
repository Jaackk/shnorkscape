package com.rs.network.packet.impl;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.rs.Settings;
import com.rs.cache.loaders.BodyDefinitions;
import com.rs.cache.loaders.ClientScriptMap;
import com.rs.cache.loaders.DBRow;
import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.PuroPuro;
import com.rs.game.activites.Sawmill;
import com.rs.game.activites.duel.DuelArena;
import com.rs.game.activites.duel.DuelControler;
import com.rs.game.activites.pest.CommendationExchange;
import com.rs.game.activities.wildywyrm.WildyWyrmControlPanel;
import com.rs.game.item.Item;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.familiar.Familiar.SpecialAttack;
import com.rs.game.player.ActionBar.DefenceAbilityShortcut;
import com.rs.game.player.ActionBar.HealAbilityShortcut;
import com.rs.game.player.ActionBar.MagicAbilityShortcut;
import com.rs.game.player.ActionBar.MeleeAbilityShortcut;
import com.rs.game.player.ActionBar.RangeAbilityShortcut;
import com.rs.game.player.ActionBar.StrengthAbilityShortcut;
import com.rs.game.player.Bank;
import com.rs.game.player.EmotesManager;
import com.rs.game.player.Equipment;
import com.rs.game.player.Inventory;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.actions.FightPitsViewingOrb;
import com.rs.game.player.actions.HomeTeleport;
import com.rs.game.player.actions.Rest;
import com.rs.game.player.actions.RingTransformation;
import com.rs.game.player.actions.CookingRs3Dialogue;
import com.rs.game.player.actions.crafting.CraftingRs3Dialogue;
import com.rs.game.player.actions.crafting.JewellerySmithing;
import com.rs.game.player.actions.fletching.FletchingRs3Dialogue;
import com.rs.game.player.actions.herblore.HerbloreRs3Dialogue;
import com.rs.game.player.actions.herblore.herbicide.Herbicide;
import com.rs.game.player.actions.magic.lunar.impl.NPCContact;
import com.rs.game.player.actions.magic.lunar.impl.RemoteFarm;
import com.rs.game.player.actions.smithing.Smelting;
import com.rs.game.player.actions.smithing.Smithing.ForgingInterface;
import com.rs.game.player.content.AccountInterfaceManager;
import com.rs.game.player.content.AchievementInterface;
import com.rs.game.player.content.Banks;
import com.rs.game.player.content.DungeonRewardShop;
import com.rs.game.player.content.FairyRing;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.ItemConstants;
import com.rs.game.player.content.ItemSets;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.PlayerLook;
import com.rs.game.player.content.QuestTab;
import com.rs.game.player.content.RS3SkillsDialogue;
import com.rs.game.player.content.SkillCapeCustomizer;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.SlayerTask;
import com.rs.game.player.content.SpiritTree;
import com.rs.game.player.content.TaskTab;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.death.DeathManager;
import com.rs.game.player.content.death.Gravestone;
import com.rs.game.player.content.distinctioncape.DistinctionCapeInterface;
import com.rs.game.player.content.dropcollection.DropCollectionInterface;
import com.rs.game.player.content.dungeoneering.DungManager;
import com.rs.game.player.content.eds.EliteDungeonsManager;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.group.GroupInterface;
import com.rs.game.player.content.interfaces.BossTimerInterface;
import com.rs.game.player.content.interfaces.NPCDropInterface;
import com.rs.game.player.content.interfaces.ReaperBenefitsInterface;
import com.rs.game.player.content.interfaces.Starter.StarterInterface;
import com.rs.game.player.content.interfaces.combinations.CombinationsInterface;
import com.rs.game.player.content.interfaces.keybinds.KeyBindInterface;
import com.rs.game.player.content.interfaces.skillinginterface.SkillingInterface;
import com.rs.game.player.content.interfaces.teleport.TeleportInterface;
import com.rs.game.player.content.items.Bonecrusher;
import com.rs.game.player.content.items.MeilyrRecipes;
import com.rs.game.player.content.maxguild.combatportal.BossPortal;
import com.rs.game.player.content.maxguild.combatportal.BossPortalInterfaceHandler;
import com.rs.game.player.content.petperks.PetPerkInterface;
import com.rs.game.player.content.shops.ShopViewer;
import com.rs.game.player.content.titles.PlayerTitleHandler;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.player.dialogue.impl.BankList;
import com.rs.game.player.dialogue.impl.LevelUp;
import com.rs.game.player.dialogue.impl.OptionSelectionD;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.io.InputStream;
import com.rs.network.packet.PacketRepository;
import com.rs.utils.Colors;
import com.rs.utils.DropDownMenuEvent;
import com.rs.utils.InputIntegerComponentEvent;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;
import com.rs.utils.data.parsers.misc.ShopsDataParser;

public class ButtonHandler {

    private static final int DUNGEONEERING_FLOOR_COUNT = 60;
    private static final int DUNGEONEERING_FLOOR_SELECT_FIRST_COMPONENT = 7;
    private static final int DUNGEONEERING_FLOOR_SELECT_LAST_COMPONENT = 78;
    private static final int[] DUNGEONEERING_FLOOR_ROW_COMPONENT_STARTS = {
            609, 669, 487, 547, 365, 425, 243, 303, 121, 182
    };

    /**
     * handles the buttonsclicks made on a certain interface
     *
     * @param player, player who clicks
     * @param stream, stream info from the client
     * @param packetId, which action
     */


    private static boolean openBankByIndex(Player player, int index) {
        if (index < 0 || index >= player.getBanks().size() || player.getBanks().get(index) == null) {
            player.sendMessage(Colors.RED + "You don't have that bank yet.");
            return false;
        }
        Bank target = player.getBanks().get(index);
        if (player.getInterfaceManager().containsBankInterface())
            player.getInterfaceManager().removeBankInterface();
        player.setBank(target);
        player.getBank().setPlayer(player);
        player.getBank().openBank();
        player.sendMessage("Now viewing " + Colors.WHITE + "Bank " + (index + 1) + "</col>.");
        return true;
    }

    private static int getDungeoneeringFloorComponent(int componentId) {
        int floor = getDungeoneeringFloorSelectComponent(componentId);
        if (floor != -1)
            return floor;
        for (int startComponent : DUNGEONEERING_FLOOR_ROW_COMPONENT_STARTS) {
            if (componentId >= startComponent && componentId < startComponent + DUNGEONEERING_FLOOR_COUNT)
                return (componentId - startComponent) + 1;
        }
        return -1;
    }

    private static int getDungeoneeringFloorSelectComponent(int componentId) {
        if (componentId < DUNGEONEERING_FLOOR_SELECT_FIRST_COMPONENT
                || componentId > DUNGEONEERING_FLOOR_SELECT_LAST_COMPONENT)
            return -1;
        if (componentId >= 11 && (componentId - 11) % 6 == 0)
            return -1;
        int labelCountBeforeComponent = componentId < 11 ? 0 : ((componentId - 11) / 6) + 1;
        int floor = componentId - 6 - labelCountBeforeComponent;
        return floor >= 1 && floor <= DUNGEONEERING_FLOOR_COUNT ? floor : -1;
    }

    private static int getDungeoneeringComplexityComponent(int componentId) {
        if (componentId >= 13 && componentId <= 18)
            return 19 - componentId;
        switch (componentId) {
            case 56:
                return 1;
            case 61:
                return 2;
            case 66:
                return 3;
            case 71:
                return 4;
            case 76:
                return 5;
            case 81:
                return 6;
            default:
                return -1;
        }
    }


    private static String packetName(int id) {
        if (id == PacketRepository.ACTION_BUTTON1_PACKET)  return "1";
        if (id == PacketRepository.ACTION_BUTTON2_PACKET)  return "2";
        if (id == PacketRepository.ACTION_BUTTON3_PACKET)  return "3";
        if (id == PacketRepository.ACTION_BUTTON4_PACKET)  return "4";
        if (id == PacketRepository.ACTION_BUTTON5_PACKET)  return "5";
        if (id == PacketRepository.ACTION_BUTTON6_PACKET)  return "6 (All/Max)";
        if (id == PacketRepository.ACTION_BUTTON7_PACKET)  return "7";
        if (id == PacketRepository.ACTION_BUTTON8_PACKET)  return "8 (Examine)";
        if (id == PacketRepository.ACTION_BUTTON9_PACKET)  return "9 (X)";
        if (id == PacketRepository.ACTION_BUTTON10_PACKET) return "10";
        return String.valueOf(id);
    }



    /**
     * Usernames currently receiving in-chat traces of every button packet they
     * trigger. Driven by ::tracebuttons on|off. Useful for identifying which
     * interface an unknown widget belongs to (e.g. the legacy XP counter orb).
     */
    public static final java.util.Set<String> BUTTON_TRACE_USERS =
            java.util.concurrent.ConcurrentHashMap.newKeySet();

    public static void handleButtons(final Player player, InputStream stream, int packetId) throws ClassNotFoundException {
        int slotId2 = stream.readUnsignedShortLE128();
        int slotId = stream.readUnsignedShort();
        int interfaceHash = stream.readInt();





        int interfaceId = interfaceHash >> 16;
        /**
         * basic componentId, will work for 90% of the interfaces, if not use @link
         * componentIdBig
         */
        final int componentId = interfaceHash & 0xFFF;
        /**
         * components of the same interface always have the same base hash. could be
         * usefull for something later.
         */
        int baseHash = interfaceId << 16;
        /**
         * this is used for components who are bigger than the unsinged byte size (256)
         */
        final int componentIdBig = interfaceHash - baseHash;
        handleButtons(player, interfaceId, componentId, slotId, slotId2, packetId, componentIdBig);
    }

    /**
     * Number of decoded button dispatches (legacy stream path and the native 947
     * router both pass through the decoded overload); smokes read it to prove
     * the 910 handler ran rather than a bypass.
     */
    public static final java.util.concurrent.atomic.AtomicLong DISPATCHES = new java.util.concurrent.atomic.AtomicLong();

    /**
     * Decoded entry point: the whole button dispatch chain after the wire fields
     * have been read. The native 947 router calls this with the 910 interface and
     * component ids it translated from the verified 947 bindings and with the
     * option already mapped to an {@code ACTION_BUTTONn_PACKET} id; the 947
     * component is never masked, so {@code componentIdBig} equals {@code componentId}.
     */
    public static void handleButtons(final Player player, int interfaceId, int componentId, int slotId, int slotId2, int packetId) throws ClassNotFoundException {
        handleButtons(player, interfaceId, componentId, slotId, slotId2, packetId, componentId);
    }

    /**
     * Decoded body shared by the stream path and the native router. The 910 stream
     * path passes the unmasked component as {@code componentIdBig} (only used by the
     * owner debug panel message), exactly as before the extraction.
     */
    public static void handleButtons(final Player player, int interfaceId, final int componentId, int slotId, int slotId2, int packetId, final int componentIdBig) throws ClassNotFoundException {
        DISPATCHES.incrementAndGet();
        if (player != null && BUTTON_TRACE_USERS.contains(player.getUsername())) {
            player.sendMessage("[trace] IF=" + interfaceId + " COMP=" + componentId
                    + " SLOT=" + slotId + " SLOT2=" + slotId2 + " PKT=" + packetId);
        }
        if (interfaceId == 1477 && componentId == 13)
            return;
        player.increaseAFKTimer();
        if (Utils.getInterfaceDefinitionsSize() <= interfaceId)
            return;
        if (player.isDead())
            return;



// debug print ui id

        String pkt = packetName(packetId);

// Safe menu/sub read (won't crash if null)
        int menu123 = -1;
        int[] subs = null;
        try {
            menu123 = player.getInterfaceManager().getCurrentMenu();
            subs = player.getSubMenus();
        } catch (Exception ignored) {}

// Decode 1477 tab (slots 3,7,11,15,19,23 -> tabs 1..6)
        int tab = -1;
        if (interfaceId == 1477 && slotId >= 3 && ((slotId - 3) % 4) == 0) {
            tab = ((slotId - 3) / 4) + 1;
        }

// Decode skill from skills UIs (1466/320)
        int skill = -1;
        if ((interfaceId == 1466 || interfaceId == 320)
                && slotId >= 0 && slotId < Skills.FIXED_SLOTS.length) {
            skill = Skills.FIXED_SLOTS[slotId];
        }

        StringBuilder sb = new StringBuilder(160);
        sb.append("IF=").append(interfaceId)
                .append(" COMP=").append(componentId)
                .append(" SLOT=").append(slotId)
                .append(" SLOT2=").append(slotId2)
                .append(" PKT=").append(packetId).append(" (").append(pkt).append(")")
                .append(" MENU=").append(menu123)
                .append(" SUB=").append(java.util.Arrays.toString(subs));
        if (tab != -1)   sb.append(" TAB=").append(tab);
        if (skill != -1) sb.append(" SKILL=").append(skill);

        System.out.println(sb.toString());



        boolean isLogout = interfaceId == 182 && componentId == 6 || componentId == 13;
        if (!player.getAccountPin().hasEnteredPin() && !isLogout) {
            return;
        }
        if (componentId != 65535 && Utils.getInterfaceDefinitionsComponentsSize(interfaceId) <= componentId)
            return;
        if (player.isOwner() || player.isDev()) {
            player.getPackets().sendPanelBoxMessage("ID: " + interfaceId + "; compId: " + componentIdBig + "; slotId: " + slotId + "; slotId2: " + slotId2 + "; packetId: " + packetId + ".");
            if(Settings.DEBUG)
                System.out.println("ID: " + interfaceId + "; compId: " + componentIdBig + "; slotId: " + slotId + "; slotId2: " + slotId2 + "; packetId: " + packetId + ".");
        }
        if (!player.getControlerManager().processButtonClick(interfaceId, componentId, slotId, slotId2, packetId))
            return;
        if (interfaceId == 948 && player.getDungeoneeringJournals().openJournal(componentId))
            return;
        if (interfaceId == 1708 || interfaceId == 1530 || interfaceId == 1712) {
            player.getInventionManager().handleInterface(interfaceId, componentId, slotId, slotId2);
            return;
        }
        if (player.getRingOfKinship() != null && player.getRingOfKinship().handleButtons(player, interfaceId, componentId, slotId, slotId2, packetId))
            return;
        if (player.getDeathManager() != null) {
            if (player.getDeathManager().handleInterface(interfaceId, componentId, packetId, slotId, slotId2))
                return;
        }
        if (interfaceId == 1284) {
            if (player.getTemporaryAttributtes().get("ViewDrops") != null)
                return;
            if (player.getTemporaryAttributtes().get("telosTrove") == null)
                return;
            if (componentId == 7) {
                Item item = player.getTelosRewards().get(slotId);
                if (item == null)
                    return;
                player.getPackets().sendGameMessage(ItemExaminesDataParser.getExamine(item));
            } else if (componentId == 8)
                player.claimTelosRewards();
            else if (componentId == 9)
                player.continueChallenge();
            return;
        }
        if (interfaceId == 1371) { // skill dialogue rs3
            if (componentId == 28 && ForgingInterface.hasRs3SmithingProducts(player)) {
                ForgingInterface.handleRs3CategorySelection(player, slotId);
            } else if (componentId == 28 && Smelting.hasRs3SmeltingProducts(player)) {
                Smelting.handleRs3CategorySelection(player, slotId);
            } else if (componentId == 28 && CraftingRs3Dialogue.hasRs3CraftingProducts(player)) {
                CraftingRs3Dialogue.handleRs3CategorySelection(player, slotId);
            } else if (componentId == 28 && FletchingRs3Dialogue.hasRs3FletchingProducts(player)) {
                FletchingRs3Dialogue.handleRs3CategorySelection(player, slotId);
            } else if (componentId == 28 && HerbloreRs3Dialogue.hasRs3HerbloreProducts(player)) {
                HerbloreRs3Dialogue.handleRs3CategorySelection(player, slotId);
            } else if (componentId == 28 && CookingRs3Dialogue.hasRs3CookingProducts(player)) {
                CookingRs3Dialogue.handleRs3CategorySelection(player, slotId);
            } else if (componentId == 28) {
                player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                    @Override
                    public void run(Player player) {
                        RS3SkillsDialogue.setCategoryByIndex(player, getSlotId());
                    }
                });
            } else if (componentId == 22 && ForgingInterface.hasRs3SmithingProducts(player))
                ForgingInterface.handleRs3ProductSelection(player, slotId);
            else if (componentId == 22 && Smelting.hasRs3SmeltingProducts(player))
                Smelting.handleRs3ProductSelection(player, slotId);
            else if (componentId == 22 && CraftingRs3Dialogue.hasRs3CraftingProducts(player))
                CraftingRs3Dialogue.handleRs3ProductSelection(player, slotId);
            else if (componentId == 22 && FletchingRs3Dialogue.hasRs3FletchingProducts(player))
                FletchingRs3Dialogue.handleRs3ProductSelection(player, slotId);
            else if (componentId == 22 && HerbloreRs3Dialogue.hasRs3HerbloreProducts(player))
                HerbloreRs3Dialogue.handleRs3ProductSelection(player, slotId);
            else if (componentId == 22 && CookingRs3Dialogue.hasRs3CookingProducts(player))
                CookingRs3Dialogue.handleRs3ProductSelection(player, slotId);
            else if (componentId == 22)
                RS3SkillsDialogue.setProductByIndex(player, (slotId - 1) / 4);
            else if (componentId == 20)
                RS3SkillsDialogue.setCurrentQuantity(player, slotId + 1);
            else if (componentId == RS3SkillsDialogue.CONTINUE_OPTION)
                player.getDialogueManager().continueDialogue(interfaceId, componentId);
            return;
        }
        if (interfaceId == 1370 && componentId == RS3SkillsDialogue.CONTINUE_OPTION) { // skill dialogue rs3
            player.getDialogueManager().continueDialogue(interfaceId, componentId);
            return;
        }
        if(interfaceId == PlayerTitleHandler.INTERFACE_ID){
            PlayerTitleHandler.handelComponents(player,componentId);
            return;
        }
        if (player.getGamblingSession() != null && interfaceId == 9) {
            player.getGamblingSession().handleButtons(player, componentId);
            return;
        } else if (interfaceId == BossTimerInterface.INTERFACE_ID) {
            BossTimerInterface.handleButtons(player, componentId);
        }
        if (interfaceId == 1904) {
            switch (componentId) {
                case 2:
                    if (player.getInterfaceManager().containsChatBoxInter()) {
                        player.getInterfaceManager().closeChatBoxInterface();
                    }
                    if (player.getInterfaceManager().containsInventoryInter()) {
                        player.getInterfaceManager().closeInventoryInterface();
                    }
                    AccountInterfaceManager.sendInterface(player);
                    break;
                case 4:

                    Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(3165, 3477, 0));
                    break;
                case 6:
                    AchievementInterface.open(player);
                    break;
                case 8: // opens skilling teleport
                    SkillingInterface.sendInterface(player);
                    break;
                case 10:
                    player.openTeleportInterface();
                    break;
                case 12:
                    player.getDialogueManager().startDialogue("PlayerCollectionsD");
                    break;
                case 14:
                    //player.getDialogueManager().startDialogue("ServerInfoD");
                    // accesses portable bank

                    //BankList.openBankByIndex(player, 10);



                    break;
                case 16:
                    player.sendMessage("Viewing Sophanem chest contents (interface 1284, component 7)");

                    List<Item> chestLoot = player.getSophanemChestLoot();
                    Item[] loot = chestLoot.toArray(new Item[chestLoot.size()]);

                    if (loot.length == 0) {
                        player.sendMessage("Your Sophanem chest is empty.");
                        return;
                    }

                    if (loot.length > 24) {
                        player.sendMessage("Warning: Your chest contains " + loot.length);
                    }

                    player.getInterfaceManager().sendInterface(1284);

                    // These were missing
                    //player.getPackets().sendInterSetItemsOptionsScript(1284, 7, 99, 8, 3, "Take", "Bank", "Discard", "Examine");
                    //player.getPackets().sendUnlockIComponentOptionSlots(1284, 7, 0, 32, 0, 1, 2, 3);
                    player.getPackets().sendInterSetItemsOptionsScript(1284, 7, 99, 8, 4, "View");
                    player.getPackets().sendUnlockIComponentOptionSlots(1284, 7, 0, 32, 0); // Only option 0 = "View"
                    player.getPackets().sendItems(99, loot);

                    return;

                case 18:
                    //CosmeticsInterface.sendInterface(player);
                    player.getDialogueManager().startDialogue("PlayerCosmeticsD");
                    break;
            }
            return;
        }
        if (interfaceId == ReaperBenefitsInterface.INTERFACE_ID) {
            ReaperBenefitsInterface.handelButtonOptions(player, componentId);
            return;
        }
        if (interfaceId == 88) {
            NPCContact.handleInterface(player, slotId);
            return;
        }
        if(interfaceId == NPCDropInterface.INTERFACE_ID){
            NPCDropInterface.handleButtons(player,componentId);
        }
        if (interfaceId == KeyBindInterface.INTERFACE_ID) {
            KeyBindInterface.handleButtons(player, componentId);
            return;
        }

        if (interfaceId == 88) {
            NPCContact.handleInterface(player, slotId);
            return;
        }
        if(interfaceId == SkillingInterface.INTERFACE_ID){
            SkillingInterface.handelButtonClicks(player,componentId);
            return;
        }

        if (interfaceId == 652) {
            if (componentId == 34) {
                Gravestone.sendBuySelection(player, slotId);
                return;
            }
        }

        if (interfaceId == DropCollectionInterface.INTERFACE_ID) {
            DropCollectionInterface.handleButtons(player, componentId);
            return;
        }
        if (interfaceId == StarterInterface.INTERFACE_ID) {
            StarterInterface.handleButtons(player, componentId);
            return;
        }
        if (interfaceId == 403) {
            Sawmill.handleInterface(player, componentId, packetId);
            // Sawmill.handlePlanksConvertButtons(player, componentId, packetId);
            return;
        }

        if (interfaceId == 131) {
            if (componentId >= 10 && componentId <= 16)
                player.setConvertMemoryType(componentId == 10 ? 2 : ((componentId - 13) / 3));
            return;
        }
        if (interfaceId == TeleportInterface.INTERFACE_ID) {
            player.getTeleportInterface().handleButtons(componentIdBig);
            return;
        }
        if (interfaceId == 676) {
            if (componentId == 15) {
                Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(2273, 4681, 0));
                WorldObject artefact = (WorldObject) player.getTemporaryAttributtes().remove("kbd");
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        player.faceObject(artefact);
                    }
                }, 2);
            }
            player.closeInterfaces();
            return;
        }

        if (player.getDungeoneeringBinds().processButtonClick(interfaceId, componentId))
            return;
        if (WildyWyrmControlPanel.handleInterfaceButtons(player, interfaceId, componentId))
            return;
        if (interfaceId == 1157) {
            AccountInterfaceManager.handleInterface(player, componentId);
            return;
        }
        if (interfaceId == 1006) {
            Herbicide.handleHerbicide(player, componentId);
            return;
        }
        if (interfaceId == 1641) {
            Bonecrusher.handleBonecrusher(player, componentId);
            return;
        }
        /*
         * if (interfaceId == 109) { if (componentId == 58) { if (packetId == 32) {
         * player.getPackets().sendGameMessage(sendLoanItemExamine(slotId2)); return; }
         * player.getTrade().handleCollectButton(player); return; } // return; }
         */
        if (interfaceId == 748) {
            if (componentId == 2) {
                if (player.isOwner())
                    player.addPoisonImmune(180000);
            }
            return;
        }

        if (interfaceId == 1312) {
            if (player.getTemporaryAttributtes().get(Key.DONATION_PERKS_D) != null || player.getTemporaryAttributtes().get(Key.MANUFACTURE_D) != null) {
                player.getDialogueManager().continueDialogue(interfaceId, componentId);
                return;
            }
            if (player.getTemporaryAttributtes().containsKey(BossPortal.RETUNING_COMBAT_PORTAL_KEY) && player.withinDistance(BossPortal.PORTAL_TILE, 3)) {
                BossPortalInterfaceHandler.handleButtons(player, componentId);
                return;
            }
            if (componentId != 29) {
                Banks.handleButton(player, componentId);
                return;
            }
        }
        if (interfaceId == 105 || interfaceId == 107 || interfaceId == 109 || interfaceId == 389 || interfaceId == 651)
            player.getGEManager().handleButtons(interfaceId, componentId, slotId, packetId);
        if (interfaceId == 1021)
            player.coOpSlayer.handleInviteButtons(player, interfaceId, componentId);
        if (interfaceId == 1309)
            player.coOpSlayer.handleCoOpSlayerInterface(player, componentId);
        if (interfaceId == 1312 || interfaceId == 960 || interfaceId == 1263 || interfaceId == 668 || interfaceId == 1074 || (interfaceId == 1082 && player.getTemporaryAttributtes().get("CosmeticsStore") != null)
                || interfaceId == 1048 || interfaceId == 1262 || interfaceId == 382 || interfaceId == 1292 || interfaceId == 793) {
            if (interfaceId == 1263)
                player.getDialogueManager().continueDialogue(interfaceId, componentId, slotId);
            else
                player.getDialogueManager().continueDialogue(interfaceId, componentId);
            if (Settings.DEBUG)
                Logger.getGlobal().info("Continue dialogue - interface: " + interfaceId + "; component: " + componentId + ".");
            return;
        }
        if (interfaceId == 375) {
            if (componentId == 4) {
                RingTransformation.resetTransformation(player);
                player.stopAll();
                return;
            }
        }
        if (interfaceId == 640) {
            if (componentId == 18 || componentId == 22) {
                player.getTemporaryAttributtes().put("WillDuelFriendly", true);
                player.getVarBitManager().sendVar(283, 67108864);
            } else if (componentId == 19 || componentId == 21) {
                player.getTemporaryAttributtes().put("WillDuelFriendly", false);
                player.getVarBitManager().sendVar(283, 134217728);
            } else if (componentId == 20)
                DuelControler.challenge(player);
            return;
        }
        if (interfaceId == DungeonRewardShop.REWARD_SHOP) {
            DungeonRewardShop.handleButtons(player, componentId, slotId, packetId);
            return;

        }

        if (interfaceId == 492 || interfaceId == 493) {
            if (player.getGroup() != null) {
                if (interfaceId == 493)
                    GroupInterface.joinButtons(player, componentId);
                else
                    GroupInterface.recruitButtons(player, componentId);
            }
            return;
        }

        if (interfaceId == 1156) {
            player.getDominionTower().handleButtons(interfaceId, componentId);
            return;
        }
        if (interfaceId == 540) {
            if (componentId == 69)
                PuroPuro.confirmPuroSelection(player);
            else if (componentId == 71)
                ShopsDataParser.openShop(player, 32);
            else
                PuroPuro.handlePuroInterface(player, componentId);
            return;
        }
        if (interfaceId == 1931) {
            if (componentId == 15)
                player.getTreasureTrails().movePuzzlePeice(slotId);
            else if (componentId == 23)
                player.getTreasureTrails().toggleInvertKeyBoard();
            else if (componentId == 31)
                player.getPackets().sendGameMessage(!player.getTreasureTrails().hasCompletedPuzzle() ? "The puzzle doesnt look right." : "Good job! the puzzle looks all right.");
            else if(componentId >= 3 && componentId <= 6)
                player.getTreasureTrails().movePuzzlePeiceByKeyboard(componentId == 3 ? 98 : componentId == 4 ? 99 : componentId == 5 ? 96 : 97);
        } else if (interfaceId == 864) {
            SpiritTree.handleSpiritTree(player, slotId);
        }
//        if (interfaceId == 365)
//            player.getTreasureTrails().handleSextant(componentId);
        if (interfaceId == 1252 || interfaceId == 1253 || (interfaceId == 1607 && componentId == 34)) {
            return;
        }
        if (interfaceId == 675) {
            JewellerySmithing.handleButtonClick(player, componentId, packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 1 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 5 : packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 10 : packetId == PacketRepository.ACTION_BUTTON4_PACKET ? 14 : 0);
            return;
        }
        if (interfaceId == 1082) {
            if (player.getTemporaryAttributtes().get("remoteFarm") != null) {
                RemoteFarm.handleRemoteFarming(player, componentId);
                return;
            }
            if (player.getTemporaryAttributtes().get("distinctionreq") == null && player.getTemporaryAttributtes().get("heart") == null && player.getTemporaryAttributtes().get("usingAchievements") == null)
                player.getTitles().handleShop(componentId);
            return;
        }
        if (interfaceId == 746) {
            if (componentId == 75)
                TaskTab.sendTab(player);
            // else if (componentId >= 74 && componentId <= 89)
            // player.lastTabResizable = (componentId - 74);
        }
        if (interfaceId == 161 || interfaceId == 164 || interfaceId == 378) {
            SlayerTask.handleShop(player, interfaceId, componentId);
            return;
        }
        if (interfaceId == 17) {
            if (componentId == 28) {
                player.stopAll();
                DeathManager.sendItemsKeptOnDeathInterface(player, player.getTemporaryAttributtes().remove("wildToggle") == null, false);
            } else {
                if (slotId2 > 0 && slotId2 < 65535) {
                    player.sendMessage(ItemExaminesDataParser.getExamine(new Item(slotId2)));
                    player.sendMessage("Grand Exchange guide price: " + Utils.getFormattedNumber(GrandExchange.getPrice(new Item(slotId2))) + ".");
                }
            }
        }

        if (interfaceId == 1311) {
            // PlayerCustomization.handleButtons(player, componentId);
        }

        if (interfaceId == 1253 && componentId == 2) {
            player.closeInterfaces();
        }
        if (interfaceId == 734) {
            if (componentId == 120)
                FairyRing.confirmRingHash(player);
            else if (componentId >= 84 && componentId <= 89)
                FairyRing.handleDialButtons(player, componentId);
            return;
        }
        if (interfaceId == 735) {
            if (componentId >= 14 && componentId <= 14 + 64)
                FairyRing.sendRingTeleport(player, componentId - 14);
            return;
        } else if (interfaceId == 1011) {
            CommendationExchange.handleButton(player, componentId, slotId);
        } else if (interfaceId == PetPerkInterface.INTERFACE_ID) {
            PetPerkInterface.handleButtons(player, componentId);
        }  else if (interfaceId == CombinationsInterface.INTERFACE_ID) {
            CombinationsInterface.handleButtons(player, componentId);
            return;
        } else if (interfaceId == 729)
            PlayerLook.handleThessaliasMakeOverButtons(player, componentId, slotId);
        else if (interfaceId == 275) {

        } else if ((interfaceId == 590 && componentId == 8) || interfaceId == 464) {
            player.getEmotesManager().useBookEmote(interfaceId == 464 ? componentId : EmotesManager.getId(slotId, packetId));
        } else if (interfaceId == 506) {
            QuestTab.handleTab(player, componentId);
        } else if (interfaceId >= 334 && interfaceId < 337) {
            player.getItemTransaction().handleButtons(player, interfaceId, packetId, componentId, slotId);
        } else if (interfaceId == 300) {
            ForgingInterface.handleIComponents(player, componentId);
        } else if (interfaceId == 206) {
            if (componentId == 13) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    player.getPriceCheckManager().removeItem(slotId, 1);
                else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    player.getPriceCheckManager().removeItem(slotId, 5);
                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    player.getPriceCheckManager().removeItem(slotId, 10);
                else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
                    player.getPriceCheckManager().removeItem(slotId, Integer.MAX_VALUE);
                else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
                    player.sendInputInteger("Enter Amount:", new InputIntegerEvent() {
                        @Override
                        public void run(Player player) {
                            final int value = getInteger();
                            if (value < 0)
                                return;
                            player.getPriceCheckManager().removeItem(slotId, value);
                        }
                    });
                }
            }  else if (componentId == 5) {
                player.getPriceCheckManager().addAllInventory();
            }
        } else if (interfaceId == 207) {
            if (componentId == 0) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    player.getPriceCheckManager().addItem(slotId, 1);
                else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    player.getPriceCheckManager().addItem(slotId, 5);
                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    player.getPriceCheckManager().addItem(slotId, 10);
                else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
                    player.getPriceCheckManager().addItem(slotId, Integer.MAX_VALUE);
                else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
                    player.sendInputInteger("Enter Amount:", new InputIntegerEvent() {
                        @Override
                        public void run(Player player) {
                            final int value = getInteger();
                            if (value < 0)
                                return;
                            player.getPriceCheckManager().addItem(slotId, value);
                        }
                    });
                } else if (packetId == PacketRepository.ACTION_BUTTON9_PACKET)
                    player.getInventory().sendExamine(slotId);
            }
        } else if (interfaceId == 916) {
            SkillsDialogue.handleSetQuantityButtons(player, componentId);
        }  else if (interfaceId == 398) {
//            if (componentId == 19)
//                player.getInterfaceManager().sendSettings();
//            else if (componentId == 15 || componentId == 1)
//                player.getHouse().setBuildMode(componentId == 15);
//            else if (componentId == 25 || componentId == 26)
//                player.getHouse().setArriveInPortal(componentId == 25);
//            else if (componentId == 27)
//                player.getHouse().expelGuests();
//            else if (componentId == 29)
//                House.leaveHouse(player);
        } else if (interfaceId == 402) {
            if (componentId >= 72 && componentId <= 90) {
                player.getHouse().createRoom((componentId - 72) / 9);
            } else if (componentId >= 98) {
                player.getHouse().createRoom(3 + (componentId - 98) / 8);
            }
        } else if (interfaceId == AchievementInterface.INTERFACE_ID) {
            AchievementInterface.handleButtons(player, componentId, slotId);
        } else if (interfaceId == DistinctionCapeInterface.INTERFACE_ID) {
            DistinctionCapeInterface.handleButtons(player, componentId);
        }
        if (interfaceId == 1466 || interfaceId == 320) {
            if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                player.stopAll();
                int lvlupSkill = Skills.FIXED_SLOTS[slotId];
                if  (player.getInterfaceManager().getCurrentMenu() != 0 || player.getSubMenus()[0] + 1 != 2)
                    player.getInterfaceManager().openMenu(0, 2);
                player.getSkills().setSelectedSkillId(lvlupSkill);
                if (lvlupSkill != -1)
                    LevelUp.switchFlash(player, lvlupSkill, false);
            } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET || packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
                int skillId = Skills.FIXED_SLOTS[slotId];
                boolean usingLevel = packetId == PacketRepository.ACTION_BUTTON2_PACKET;
                player.sendInputInteger("Please enter target " + (usingLevel ? "level" : "xp") + " you want to set: ", new InputIntegerEvent() {
                    @Override
                    public void run(Player player) {
                        if (!usingLevel) {
                            int xpTarget = getInteger();
                            if (xpTarget < player.getSkills().getXp(player.getSkills().getSkillIdByTargetId(skillId)) || player.getSkills().getXp(player.getSkills().getSkillIdByTargetId(skillId)) >= 200000000) {
                                return;
                            }
                            if (xpTarget > 200000000) {
                                xpTarget = 200000000;
                            }
                            player.getSkills().setSkillTarget(false, skillId, xpTarget);
                        } else {
                            int levelTarget = getInteger();
                            int curLevel = player.getSkills().getLevel(player.getSkills().getSkillIdByTargetId(skillId));
                            if (curLevel >= (skillId == Skills.DUNGEONEERING || skillId == Skills.INVENTION || skillId == Skills.SLAYER ? 120 : 99)) {
                                return;
                            }
                            if (levelTarget > (skillId == Skills.DUNGEONEERING || skillId == Skills.INVENTION|| skillId == Skills.SLAYER ? 120 : 99)) {
                                levelTarget = skillId == Skills.DUNGEONEERING || skillId == Skills.INVENTION|| skillId == Skills.SLAYER ? 120 : 99;
                            }
                            if (levelTarget < player.getSkills().getLevel(player.getSkills().getSkillIdByTargetId(skillId))) {
                                return;
                            }
                            player.getSkills().setSkillTarget(true, skillId, levelTarget);
                        }
                    }
                });
            } else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) { // clear target
                int skillId = Skills.FIXED_SLOTS[slotId];
                player.getSkills().setSkillTargetEnabled(skillId, false);
                player.getSkills().setSkillTargetValue(skillId, 0);
                player.getSkills().setSkillTargetUsingLevelMode(skillId, false);
            }
        } else if (interfaceId == 1265) {
            ShopViewer shopViewer = (ShopViewer) player.getTemporaryAttributtes().get("Shop");
            if (shopViewer == null)
                return;
            Integer slot = (Integer) player.getTemporaryAttributtes().get("ShopSelectedSlot");
            boolean isBuying = player.getTemporaryAttributtes().get("shop_buying") != null;
            if (componentId == 46 || componentId == 47)
                player.setVerboseShopDisplayMode(componentId == 47);
            if (componentId == 20 || componentId == 144) {
                player.getTemporaryAttributtes().put("ShopSelectedSlot", slotId);
                if (componentId == 20 && !isBuying) {
                    shopViewer.sendInfo(player, slotId, isBuying);
                    player.getPackets().sendConfig(2561, 93);
                    return;
                }
                if (componentId == 144) {
                    shopViewer.handleShop(player, slot, 1);
                } else if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    shopViewer.sendInfo(player, slotId, isBuying);
                } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                    shopViewer.handleShop(player, slotId, 1);
                } else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
                    shopViewer.handleShop(player, slotId, 5);
                } else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
                    shopViewer.handleShop(player, slotId, 10);
                } else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
                    shopViewer.handleShop(player, slotId, 50);
                } else if(packetId == PacketRepository.ACTION_BUTTON6_PACKET) {
                    player.sendInputInteger("How many would you like to buy? (max : 5k)", new InputIntegerEvent() {

                        @Override
                        public void run(Player player) {
                            int amount = getInteger();
                            if (amount <= 0 || amount > 5000) {
                                player.getPackets().sendGameMessage("Please choose a valid amount (1-5000)");
                                return;
                            }
                            shopViewer.handleShop(player, slotId, amount);
                        }

                    });
                } else if (packetId == PacketRepository.ACTION_BUTTON9_PACKET) {
                    if (isBuying) {
                        shopViewer.handleShop(player, slotId, 500);
                    } else {
                        player.sendInputInteger("Enter Amount:", new InputIntegerEvent() {
                            @Override
                            public void run(Player player) {
                                final int value = getInteger();
                                if (value < 0)
                                    return;
                                player.setShopLastViewX(value);
                                shopViewer.refreshLastX();
                                shopViewer.handleShop(player, slotId, player.getShopLastViewX());
                            }
                        });
                    }
                }
            } else if (packetId == PacketRepository.ACTION_BUTTON8_PACKET) {
                shopViewer.sendExamine(player, slotId);
            }
        } else if (interfaceId == 1266) {
            if (componentId == 0) {
                if (packetId == PacketRepository.ACTION_BUTTON9_PACKET && player.getShopLastViewX() == 0) {
                    if (slotId == -1)
                        return;
                    player.getInventory().sendExamine(slotId);
                } else {
                    ShopViewer shopViewer = (ShopViewer) player.getTemporaryAttributtes().get("Shop");
                    if (shopViewer == null)
                        return;
                    player.getPackets().sendConfig(2563, slotId);
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                        shopViewer.sendValue(player, slotId);
                    else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                        shopViewer.sell(player, slotId, 1);
                    else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                        shopViewer.sell(player, slotId, 5);
                    else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
                        shopViewer.sell(player, slotId, 10);
                    else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
                        player.sendInputInteger("Enter Amount:", new InputIntegerEvent() {
                            @Override
                            public void run(Player player) {
                                final int value = getInteger();
                                if (value < 0)
                                    return;
                                player.setShopLastViewX(value);
                                shopViewer.refreshLastX();
                                shopViewer.sell(player, slotId, player.getShopLastViewX());
                            }
                        });
                    } else if (packetId == PacketRepository.ACTION_BUTTON9_PACKET) {
                        shopViewer.sell(player, slotId, player.getShopLastViewX());
                    }
                }
            }
        } else if (interfaceId == 634) {
            if (componentId == 28) {
                Item item = new Item(slotId2);
                player.getPackets().sendGameMessage(ItemExaminesDataParser.getExamine(item));
            }
        } else if (interfaceId == Inventory.INVENTORY_INTERFACE || interfaceId == Inventory.INVENTORY_INTERFACE_2) { // inventory
            if ((interfaceId == Inventory.INVENTORY_INTERFACE && componentId == 7) || (interfaceId == Inventory.INVENTORY_INTERFACE_2 && componentId == 8)) {
                if (slotId > 27)
                    return;
                Item item = player.getInventory().getItem(slotId);
                if (item == null || item.getId() != slotId2)
                    return;
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    InventoryOptionsHandler.handleItemOption1(player, slotId, slotId2, item);
                else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    InventoryOptionsHandler.handleItemOption2(player, slotId, slotId2, item);
                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    InventoryOptionsHandler.handleItemOption3(player, slotId, slotId2, item);
                else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
                    InventoryOptionsHandler.handleItemOption4(player, slotId, slotId2, item);
                else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET)
                    InventoryOptionsHandler.handleItemOption5(player, slotId, slotId2, item);
                else if (packetId == PacketRepository.ACTION_BUTTON6_PACKET)
                    InventoryOptionsHandler.handleItemOption6(player, slotId, slotId2, item);
                else if (packetId == PacketRepository.ACTION_BUTTON7_PACKET)
                    InventoryOptionsHandler.handleItemOption7(player, slotId, slotId2, item);
                else if (packetId == PacketRepository.ACTION_BUTTON8_PACKET)
                    InventoryOptionsHandler.handleItemOption8(player, interfaceId, componentId, slotId, slotId2, item);
            } else if ((interfaceId == Inventory.INVENTORY_INTERFACE && componentId == 1) || (interfaceId == Inventory.INVENTORY_INTERFACE_2 && componentId == 45)) {
                if (slotId == 0) {
                    player.getInventionManager().openBagOfMaterialsInterface();
                } else if (slotId == 1) {// currency pouch

                } else if (slotId == 2) {

                } else if (slotId == 3) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                        player.getPriceCheckManager().openPriceCheck();
                    else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                        player.getMoneyPouch().sendExamine();
                    else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
                        if (player.getInterfaceManager().containsScreenInter() || player.getControlerManager().getControler() instanceof DungeonController) {
                            player.sendMessage("You cannot withdraw your coins right now.");
                            return;
                        }
                        if (player.getMoneyPouch().getTotal() > 2) {
                            player.sendInputInteger("Your money pouch contains " + Utils.getFormattedNumber(player.getMoneyPouch().getTotal()) + "." + "<br>How many would you like to withdraw?", new InputIntegerEvent() {
                                @Override
                                public void run(Player player) {
                                    player.getMoneyPouch().withdrawPouch(getInteger());
                                }
                            });
                        } else if (player.getMoneyPouch().getTotal() == 1) {
                            player.getMoneyPouch().removeMoneyMisc(1);
                        } else
                            player.sendMessage("Your money pouch is empty.");
                    }
                }
            }
        } else if (interfaceId == 742) {
            if (componentId == 46) // close
                player.stopAll();
        } else if (interfaceId == 743) {
            if (componentId == 20) // close
                player.stopAll();
        } else if (interfaceId == 741) {
            if (componentId == 9) // close
                player.stopAll();
        } else if (interfaceId == 750) {
            if (componentId == 4) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    player.toogleRun(!player.isResting());
                    if (player.isResting())
                        player.stopAll();
                } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                    if (player.isResting()) {
                        player.stopAll();
                        return;
                    }
                    long currentTime = Utils.currentTimeMillis();
                    if (player.getEmotesManager().getNextEmoteEnd() >= currentTime) {
                        player.getPackets().sendGameMessage("You can't rest while perfoming an emote.");
                        return;
                    }
                    if (player.getLockDelay() >= currentTime) {
                        player.getPackets().sendGameMessage("You can't rest while perfoming an action.");
                        return;
                    }
                    player.stopAll();
                    player.getActionManager().setAction(new Rest());
                }
            }
        } else if (interfaceId == 11) {
            if (componentId == 19) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    player.getBank().depositItem(slotId, 1, false);
                else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    player.getBank().depositItem(slotId, 5, false);
                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    player.getBank().depositItem(slotId, 10, false);
                else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
                    player.getBank().depositItem(slotId, Integer.MAX_VALUE, false);
                else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
                    player.sendInputInteger("Enter Amount:", new InputIntegerEvent() {
                        @Override
                        public void run(Player player) {
                            final int value = getInteger();
                            if (value < 0)
                                return;
                            player.getBank().setLastX(value);
                            player.getBank().refreshLastX();
                            player.getBank().depositItem(slotId, value, !player.getInterfaceManager().containsInterface(11));
                        }
                    });
                } else if (packetId == PacketRepository.ACTION_BUTTON9_PACKET)
                    player.getInventory().sendExamine(slotId);
            } else if (componentId == 5)
                player.getBank().depositAllInventory(false);
            else if (componentId == 8)
                player.getBank().depositAllEquipment(false);
            else if (componentId == 11)
                player.getBank().depositAllBob(false);
            else if (componentId == 14)
                player.getBank().depositMoneyPouch(false);
        } else if (interfaceId == 767) {
            if (componentId == 10) {
                if (!player.promptList())
                    player.getBank().openBank();
                else
                    player.getDialogueManager().startDialogue("BankList", false);
            }
        } else if (interfaceId == 755) {
            if (componentId == 44) {
                player.getPackets().sendWindowsPane(player.getInterfaceManager().hasRezizableScreen() ? 746 : 548, 2);
                player.setNextAnimationForce(new Animation(22749));
            } else if (componentId == 42) {
                player.getHintIconsManager().removeAll();
                player.getPackets().sendConfig(1159, 1);
            }
        } else if (interfaceId == 20)
            SkillCapeCustomizer.handleSkillCapeCustomizer(player, componentId, packetId);
        else if (interfaceId == 1056) {
            if (componentId == 173)
                player.getInterfaceManager().sendInterface(917);
        } else if (interfaceId == 1163 || interfaceId == 1164 || interfaceId == 1168 || interfaceId == 1170 || interfaceId == 1173)
            player.getDominionTower().handleButtons(interfaceId, componentId);
        else if (interfaceId == 900)
            PlayerLook.handleMageMakeOverButtons(player, componentId);
        else if (interfaceId == 1420)
            PlayerLook.handleCharacterCustomizingButtons(player, componentId, slotId);
        else if (interfaceId == 1108 || interfaceId == 1109 || interfaceId == 1427)
            player.getFriendsIgnores().handleFriendChatButtons(interfaceId, componentId, packetId);
        else if (interfaceId == 1441 || interfaceId == 550 || interfaceId == 235)
            player.getFriendsIgnores().handleFriendListButtons(interfaceId, componentId, packetId);
        else if (interfaceId == 1079)
            player.closeInterfaces();
        else if (interfaceId == 374) {
            if (componentId >= 5 && componentId <= 9)
                player.setNextWorldTile(new WorldTile(FightPitsViewingOrb.ORB_TELEPORTS[componentId - 5]));
            else if (componentId == 15)
                player.stopAll();
        } else if (interfaceId == 1089) {
            if (componentId == 30)
                player.getTemporaryAttributtes().put("clanflagselection", slotId);
            else if (componentId == 26) {
                Integer flag = (Integer) player.getTemporaryAttributtes().remove("clanflagselection");
                player.stopAll();
                if (flag != null)
                    ClansManager.setClanFlagInterface(player, flag);
            }
        } else if (interfaceId == 1096) {
            if (componentId == 26)// +
                ClansManager.viewClammateDetails(player, slotId);
            else if (componentId == 80)// +
                ClansManager.switchGuestsInChatCanEnterInterface(player);
            else if (componentId == 83)// +
                ClansManager.switchGuestsInChatCanTalkInterface(player);
            else if (componentId == 88)// +
                ClansManager.switchRecruitingInterface(player);
            else if (componentId == 77)// +
                ClansManager.switchClanTimeInterface(player);
            else if (componentId == 124)// +
                ClansManager.openClanMottifInterface(player);
            else if (componentId == 121)// +
                ClansManager.openClanMottoInterface(player);
            else if (componentId == 108) {// +
                player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                    @Override
                    public void run(Player player) {
                        ClansManager.setTimeZoneInterface(player, -720 + getSlotId() * 10);
                    }
                });
            } else if (componentId == 64) {// +
                player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                    @Override
                    public void run(Player player) {
                        player.getTemporaryAttributtes().put("editclanmatejob", getSlotId());
                    }
                });
            } else if (componentId == 61) {// +
                player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                    @Override
                    public void run(Player player) {
                        player.getTemporaryAttributtes().put("editclanmaterank", getSlotId());
                    }
                });
            } else if (componentId == 58)// +
                ClansManager.kickClanmate(player);
            else if (componentId == 55)// +
                ClansManager.saveClanmateDetails(player);
            else if (componentId == 34) {// +
                player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                    @Override
                    public void run(Player player) {
                        ClansManager.setWorldIdInterface(player, getSlotId());
                    }
                });
            } else if (componentId == 41)// +
                ClansManager.openForumThreadInterface(player);
            else if (componentId == 140)// +
                ClansManager.openNationalFlagInterface(player);
            else if (componentId == 7)
                ClansManager.showClanSettingsClanMates(player);
            else if (componentId == 10)
                ClansManager.showClanSettingsSettings(player);
            else if (componentId == 13)// +
                ClansManager.showClanSettingsPermissions(player);
//            else if (componentId >= 139 && componentId <= 219) {// +
//                int selectedRank = (componentId - 139) / 5;
//                if (selectedRank == 10)
//                    selectedRank = 125;
//                else if (selectedRank > 5)
//                    selectedRank = 100 + selectedRank - 6;
//                ClansManager.selectPermissionRank(player, selectedRank);
//            } else if (componentId == 233)
//                ClansManager.selectPermissionTab(player, 1);
//            else if (componentId == 242)
//                ClansManager.selectPermissionTab(player, 2);
//            else if (componentId == 250)
//                ClansManager.selectPermissionTab(player, 3);
//            else if (componentId == 2)
//                ClansManager.selectPermissionTab(player, 4);
//            else if (componentId == 10)
//                ClansManager.selectPermissionTab(player, 5);
        } else if (interfaceId == 1105) {
            if (componentId == 9 || componentId == 12)
                ClansManager.setClanMottifTextureInterface(player, componentId == 9, slotId);
            else if (componentId >= 18 && componentId <= 26) {
                if (componentId == 18)
                    ClansManager.openSetMottifColor(player, 0);
                else
                    ClansManager.setMottifColorFromVar(player, 0, ((componentId - 22) / 2) + 1);
            } else if (componentId >= 30 && componentId <= 38) {
                if (componentId == 30)
                    ClansManager.openSetMottifColor(player, 1);
                else
                    ClansManager.setMottifColorFromVar(player, 1, ((componentId - 34) / 2) == 0 ? 0 : ((componentId - 34) / 2) + 1);
            } else if (componentId >= 42 && componentId <= 50){
                if (componentId == 42)
                    ClansManager.openSetMottifColor(player, 2);
                else
                    ClansManager.setMottifColorFromVar(player, 2, ((componentId - 46) / 2) == 2 ? 3 : ((componentId - 46) / 2));
            } else if (componentId >= 54 && componentId <= 62){
                if (componentId == 54)
                    ClansManager.openSetMottifColor(player, 3);
                else
                    ClansManager.setMottifColorFromVar(player, 3, ((componentId - 58) / 2));
            }
            else if (componentId == 76)
                player.stopAll();
        } else if (interfaceId == 1110 || interfaceId == 1440 || interfaceId == 233 || interfaceId == 234) {
            if ((interfaceId == 1110 && componentId == 130) || (interfaceId == 1440 && componentId == 100) || (interfaceId == 234 && componentId == 34))
                ClansManager.joinClanChatChannel(player);
            else if ((interfaceId == 1110 && componentId == 138) || (interfaceId == 1440 && componentId == 108) || (interfaceId == 234 && componentId == 28))
                ClansManager.openClanDetails(player);
            else if ((interfaceId == 1110 && componentId == 154) || (interfaceId == 1440 && componentId == 124) || (interfaceId == 234 && componentId == 16))
                ClansManager.openClanSettings(player);
            else if ((interfaceId == 1110 && componentId == 55) || (interfaceId == 1440 && componentId == 157))
                ClansManager.joinGuestClanChat(player);
            else if ((interfaceId == 1110 && componentId == 17) || (interfaceId == 1440 && componentId == 22) || (interfaceId == 233 && componentId == 10))
                ClansManager.banPlayer(player);
            else if ((interfaceId == 1110 && componentId == 25) || (interfaceId == 1440 && componentId == 29) || (interfaceId == 233 && componentId == 4))
                ClansManager.unbanPlayer(player);
            else if ((interfaceId == 1110 && componentId == 31) || (interfaceId == 1440 && componentId == 13))
                ClansManager.unbanPlayer(player, slotId);
            else if ((interfaceId == 1110 && componentId == 171) || (interfaceId == 1440 && componentId == 140) || (interfaceId == 234 && componentId == 4))
                ClansManager.leaveClan(player);
            else if (interfaceId == 1110 && componentId == 121) {
                player.getInterfaceManager().sendExpandOptionsInterface(234, interfaceId, componentId, 160, 40);
            } else if (interfaceId == 1110 && componentId == 9) {
                player.getInterfaceManager().sendExpandOptionsInterface(233, interfaceId, componentId, 64, 40);
            }
        } else if (interfaceId == 1079) {
            player.closeInterfaces();
        } else if (interfaceId == 1092) {
            if (componentId == 60) {
                player.getInterfaceManager().closeScreenInterface();
                return;
            }
            if (componentId == 59) {
                player.toggleQuickTeleportByDefault();
                return;
            }
            /*
             * String[] lodestoneNames = new String[] { "Lunar Isle", "Al Kharid",
             * "Ardougne", "Burthorpe", "Catherby", "Draynor Village", "Edgeville",
             * "Falador", "Lumbridge", "Port Sarim", "Seer's Village", "Taverley",
             * "Varrock", "Yannile" }; if (componentId != 7 && !player.lodestone[componentId
             * - 38]) { player.sendMessage("You'll need to activate the " +
             * lodestoneNames[componentId - 39] +
             * " lodestone before you can homeport there!"); return; } else if (componentId
             * == 7 && !player.lodestone[0]) { player.
             * sendMessage("You'll need to activate the Bandit Camp lodestone before you can homeport there!"
             * ); return; }
             */
            player.stopAll();
            WorldTile destTile = null;
            switch (componentId) {
                case 9:
                    destTile = HomeTeleport.BANDIT_CAMP;
                    break;
                case 10:
                    destTile = HomeTeleport.LUNAR_ISLE;
                    break;
                case 11:
                    destTile = HomeTeleport.AL_KHARID;
                    break;
                case 12:
                    destTile = HomeTeleport.ARDOUGNE;
                    break;
                case 13:
                    destTile = HomeTeleport.BURTHORPE;
                    break;
                case 14:
                    destTile = HomeTeleport.CATHERBY;
                    break;
                case 15:
                    destTile = HomeTeleport.DRAYNOR_VILLAGE;
                    break;
                case 16:
                    destTile = HomeTeleport.EDGEVILLE;
                    break;
                case 17:
                    destTile = HomeTeleport.FALADOR;
                    break;
                case 18:
                    destTile = HomeTeleport.LUMBRIDGE;
                    break;
                case 19:
                    destTile = HomeTeleport.PORT_SARIM;
                    break;
                case 20:
                    destTile = HomeTeleport.SEERS_VILLAGE;
                    break;
                case 21:
                    destTile = HomeTeleport.TAVERLEY;
                    break;
                case 22:
                    destTile = HomeTeleport.VARROCK;
                    break;
                case 25:
                    destTile = HomeTeleport.YANILLE;
                    break;
                case 26:
                    destTile = HomeTeleport.CANIFIS;
                    break;
                case 27:
                    destTile = HomeTeleport.EAGLES_PEAK;
                    break;
                case 28:
                    destTile = HomeTeleport.FREMENNIK_PROVINCE;
                    break;
                case 29:
                    destTile = HomeTeleport.KARAMJA;
                    break;
                case 30:
                    destTile = HomeTeleport.OOGLOG;
                    break;
                case 31:
                    destTile = HomeTeleport.TIRANNWN;
                    break;
//            case 32:
//                destTile = HomeTeleport.WILDERNESS_VOLCANO;
//                break;
                case 33:
                    destTile = HomeTeleport.ASHDALE;
                    break;
                case 34:
                    if (player.getPerkManager().hasPerkActive(DonationPerk.ELF__S_FRIEND) || player.getSkills().getTotalLevel() >= 2250)
                        destTile = HomeTeleport.PRIFDDINAS;
                    else
                        player.sendMessage("Purchase the 'Elf Fiend' perk or train to 2250 total level to gain access to Prifddinas.");
                    break;
                case 23:
                    destTile = HomeTeleport.MENAPHOS;
                    break;
                case 24:
                    destTile = HomeTeleport.ANACHRONIA;
                    break;
                default:
                    player.sm("This lodestone teleport is disabled.");
                    break;
            }
            if (destTile != null) {
                boolean quickTeleport = (packetId == (player.isQuickTeleportByDefault() ? PacketRepository.ACTION_BUTTON1_PACKET : PacketRepository.ACTION_BUTTON2_PACKET));
                if (quickTeleport && player.getVisWaxManager().getQuickTeleports() <= 0) {
                    quickTeleport = false;
                    player.getPackets().sendGameMessage("You don't have any quick teleport charges remaining.");
                }
                if (quickTeleport)
                    player.getVisWaxManager().sendQuickTeleport(destTile);
                else
                    player.getActionManager().setAction(new HomeTeleport(destTile));
            }
        } else if (interfaceId == 1214)
            player.getSkills().handleSetupXPCounter(componentId);
        else if (interfaceId == 1477) {
            if (componentId == InterfaceManager.getComponentIdByKey(7716, 17, 3507))
                if (slotId == 1)
                    player.getInterfaceManager().closeMinigameTab();
            if (componentId == InterfaceManager.getComponentIdByKey(7716, 1001, 3507)) {// close menu
                if (slotId == 1) {
                    player.getInterfaceManager().closeMenu();
                }
            } else if (componentId == InterfaceManager.getComponentIdByKey(7716, 1002, 3507))
                player.switchLockInterfaceCustomization();
            else if (componentId == InterfaceManager.getComponentIdByKey(7716, 1003, 3507)) {
                if (slotId == 1)
                    player.getCombatDefinitions().switchSheathe();
            } else if (componentId == InterfaceManager.getComponentIdByKey(7716, 1004, 3507)) {// send logout dialogue
                if (slotId == 1)
                    player.getInterfaceManager().openSettings();
            } else if (componentId == InterfaceManager.getComponentIdByKey(7716, 1001, 3509)) {
                player.getInterfaceManager().switchMenu(((slotId - 3) / 4) + 1);
            } else if (componentId == 4) {
                if (player.getInterfaceManager().containsCentralOverlayInterface())
                    player.getInterfaceManager().removeCentralOverlayInterface();
            } else if (componentId == InterfaceManager.DROP_DOWN_MENU_COMPONENT_ID) {
                DropDownMenuEvent event = (DropDownMenuEvent) player.getTemporaryAttributtes().remove("pluginDropdown");
                if (event != null) {
                    event.setSlotId(slotId);
                    event.run(player);
                    return;
                }
                Integer menuOption = (Integer) player.getTemporaryAttributtes().remove(Key.OPTION_MENU);
                if (menuOption == null)
                    return;
                Integer menuSlotId = (Integer) player.getTemporaryAttributtes().remove(Key.OPTION_MENU_SLOT_ID);
                switch (menuOption) {
                    case 365:
                        if (menuSlotId == null)
                            return;
                }
            }
        } else if (interfaceId == 1431) {
            if (player.getInterfaceManager().containsTreasureHunterInterface()) {
                player.getPackets().sendGameMessage("Please finish what you are doing before opening this menu.");
                return;
            }
            if (componentId == 0) {// hero f1
                int menuId = slotId;
                if (menuId == 7) {// settings.
                    if (player.getInterfaceManager().isMenuOpen())
                        player.getInterfaceManager().closeMenu();
                    player.getPackets().sendIComponentSettings(1433, 27, 0, 6, 2);
                    return;
                }
                if (menuId == 5) {
                    player.getInterfaceManager().openExtras();
                    return;
                }
                if (menuId == 29)
                    player.getInterfaceManager().sendTaskSystem();
                if (player.isInLegacyInterfaceMode())
                    return;
                if (menuId == 6)
                    menuId = 8;// rune metrics
                player.getInterfaceManager().openMenu(menuId, player.getSubMenus()[menuId] + 1);
                return;
            }
        } else if (interfaceId == 1433) { // options menu
            if (componentId == 6) {
                int menu = slotId;
                if (slotId == 2)
                    menu = 3;
                else if (slotId == 3)
                    menu = 2;
                if (slotId == 5) {
                    player.getInterfaceManager().openExtras();
                    return;
                }
                player.getInterfaceManager().openMenu(menu, player.getSubMenus()[menu] + 1);
            } else if (componentId == 14) {
                player.getInterfaceManager().openMenu(9, player.getSubMenus()[9] + 1);
            } else if (componentId == 21) {
                player.getInterfaceManager().openEditMode();
            } else if (componentId == 25) {
                player.getInterfaceManager().unlockDropDownMenu(0, 5);
            } else if (componentId == 54 || componentId == 55) {
//                player.setEnjoyPlaying(componentId == 54 ? 2 : 1);
            } else if (componentId == 66) {
                if (player.isUnderCombat()) {
                    player.getPackets().sendGameMessage("You cannot be in combat while logging out.");
                    return;
                }
                if (player.getControlerManager().getControler() instanceof DuelArena) {
                    player.sendMessage(Colors.SALMON + "Finish the fight! Logout is no escape!");
                    return;
                }
                if (!player.getControlerManager().canLogout())
                    return;
                player.stopAll();
                if (!player.hasFinished())
                    player.logout(false);
            } else if (componentId == 69 || componentId == 83) {
                if (!player.isHideLogoutWarning() && componentId != 83)
                    return;
                if (player.isUnderCombat()) {
                    player.getPackets().sendGameMessage("You cannot be in combat while logging out.");
                    return;
                }
                if (player.getControlerManager().getControler() instanceof DuelArena) {
                    player.sendMessage(Colors.SALMON + "Finish the fight! Logout is no escape!");
                    return;
                }
                if (!player.getControlerManager().canLogout())
                    return;
                player.stopAll();
                if (!player.hasFinished())
                    player.logout(false);
            } else if (componentId == 89) {
                player.switchHideLogoutWarning();
            } else if (componentId == 86) {
                player.setHideLogoutWarning(false);
            }
        } else if(interfaceId == 365) {
            int menuId = player.getGameSettingsMenuId();
            if (componentId == 11) {
                int tabId = (menuId == 0 || menuId >= 6 && menuId <= 12) ? 0 : (menuId == 1 || menuId >= 13 && menuId <= 17) ? 1 :
                        (menuId == 2 || menuId >= 18 && menuId <= 22) ? 2 : (menuId == 3 || menuId >= 23 && menuId <= 27) ? 3 : (menuId == 4 || menuId >= 28 && menuId <= 30) ? 4 : 5;
                if (tabId == slotId)
                    player.toggleSettingsTabClosed();
                else
                    player.resetSettingsTabClosed();
                player.setGameSettingsMenuId(slotId);
            } else if (componentId == 9) {
                Integer mapId = (Integer) player.getTemporaryAttributtes().get(Key.CHAT_SETUP);
                if (mapId == null)
                    return;
                player.setChatSetup(slotId);
                player.increaseGameSettingsInteractions();
            } else if(componentId == 16) {
                DBRow dbrow = DBRow.getDBRow(ClientScriptMap.getMap(14569).getIntValue(menuId));
                if (dbrow.getDataInIndex(3) != null && dbrow.getDataInIndex(3)[0] != null)
                    dbrow = DBRow.getDBRow((int) dbrow.getDataInIndex(3)[0]);
                if (dbrow == null || dbrow.getData() == null) {
                    player.increaseGameSettingsInteractions();
                    return;
                }
                int mapId = (int) dbrow.getDataInIndex(2)[slotId];
                switch(mapId) {
                    case 41773:
                        player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                            @Override
                            public void run(Player player) {
                                player.setCameraMode(getSlotId());
                                player.increaseGameSettingsInteractions();
                            }
                        });
                        return;
                    case 4195:
                    case 41766:
                    case 41767:
                    case 41768:
                    case 41769:
                    case 41770:
                    case 41771:
                    case 41663:
                    case 41666:
                    case 41754:
                    case 41755:
                        player.getTemporaryAttributtes().put("scrollBarStructId", mapId);
                        return;
                    case 41772:
                        player.switchLockZoom();
                        break;
                    case 1449:
                        player.toggleDisableCameraShake();
                        break;
                    case 41665:
                        player.switchHideMouseoverText();
                        break;
                    case 40607:
                        player.switchHideExtraPotionInformation();
                        break;
                    case 41653:
                    case 41654:
                        player.switchMouseButtons();
                        break;
                    case 41742:
                        player.switchHideCogsOnAugmentedItems();
                        break;
                    case 41601:
                    case 41603:
                        player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                            @Override
                            public void run(Player player) {
                                player.setPlayerAttackOption(getSlotId());
                                player.increaseGameSettingsInteractions();
                            }
                        });
                        return;
                    case 41602:
                    case 41604:
                        player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                            @Override
                            public void run(Player player) {
                                player.setNPCAttackOption(getSlotId());
                                player.increaseGameSettingsInteractions();
                            }
                        });
                        return;
                    case 41655:
                        player.switchHideFamiliarOptions();
                        break;
                    case 41656:
                        player.switchHideDuelOptionOnPlayers();
                        break;
                    case 41657:
                    case 41658:
                        player.switchClickThroughtChatBoxes();
                        break;
                    case 41737:
                        player.switchHideUpperLeftHoverText();
                        break;
                    case 41662:
                        player.switchDisableShortcutCloseWindow();
                        break;
                    case 41660:
                        player.switchLockInterfaceCustomization();
                        break;
                    case 41659:
                        player.switchSlimHeaders();
                        break;
                    case 41661:
                        player.switchHideTitleBarsWhenLocked();
                        break;
                    case 41740:
                        player.getInterfaceManager().openEditMode(true);
                        break;
                    case 4291:
                        player.setGameSettingsMenuId(31);
                        break;
                    case 41739:
                        player.switchGuidanceSystemHints();
                        break;
                    case 41714:
                        player.switchTaskCompletePopups();
                        break;
                    case 41717:
                        player.getSkills().switchXPPopup();
                        break;
                    case 41718:
                        player.switchSkillTargetBasedXPPopup();
                        break;
                    case 41716:
                        player.switchMakeXProgressWindow();
                        break;
                    case 6817:
                        player.switchDisableHighlightBoostedSkills();
                        break;
                    case 41734:
                        player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                            @Override
                            public void run(Player player) {
                                player.setSlayerCounter(getSlotId() != 0);
                                player.increaseGameSettingsInteractions();
                            }
                        });
                        return;
                    case 41735:
                    case 41736:
                        player.switchSlayerCounter();
                        break;
                    case 41732:
                        player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                            @Override
                            public void run(Player player) {
                                player.setUTCClock(getSlotId());
                                player.increaseGameSettingsInteractions();
                            }
                        });
                        return;
                    case 41664:
                        player.switchDisableDragToDropItems();
                        break;
                    case 41613:
                        player.switchDisableXpLampWarning();
                        break;
                    case 41600:
                        player.setLegacyMode(!player.isInLegacyCombatMode());
                        break;
                    case 41667:
                        player.toggleLegacyInterfaces();
                        break;
                    case 41668:
                        player.toggleLegacyInterfacesSkin();
                        break;
                    case 661:
                        player.switchLegacyMapIcons();
                        break;
                    case 41605:
                    case 41606:
                    case 41607:
                        player.getCombatDefinitions().toggleMeleeExperience(mapId - 41605, true);
                        break;
                    case 41608:
                    case 41609:
                        player.getCombatDefinitions().toggleRangedCombatExperience(mapId - 41608, true);
                        break;
                    case 41610:
                    case 41611:
                        player.getCombatDefinitions().toggleMagicCombatExperience(mapId - 41610, true);
                        break;
                    case 41612:
                        player.switchBlockPvPXP();
                        break;
                    case 41509:
                        player.getCombatDefinitions().switchManualSpellCasting();
                        break;
                    case 41752:
                        player.getSkills().switchVirtualLevels();
                        break;
                    case 41753:
                        player.toggleGoldTrim99();
                        break;
                    case 41651:
                    case 41652:
                        player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                            @Override
                            public void run(Player player) {
                                if (mapId == 41651)
                                    player.setYourHitSplats(0, getSlotId());
                                else
                                    player.setOtherHitSplats(0, getSlotId());
                                player.increaseGameSettingsInteractions();
                            }
                        });
                        return;
                    case 41647:
                    case 41648:
                        player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                            @Override
                            public void run(Player player) {
                                if (mapId == 41647)
                                    player.setYourHitSplats(1, getSlotId());
                                else
                                    player.setOtherHitSplats(1, getSlotId());
                                player.increaseGameSettingsInteractions();
                            }
                        });
                        return;
                    case 41649:
                    case 41650:
                        player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                            @Override
                            public void run(Player player) {
                                if (mapId == 41649)
                                    player.setYourHitSplats(2, getSlotId());
                                else
                                    player.setOtherHitSplats(2, getSlotId());
                                player.increaseGameSettingsInteractions();
                            }
                        });
                        return;
                    case 41743:
                        player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                            @Override
                            public void run(Player player) {
                                player.getHouse().setArriveInPortal(getSlotId() == 1);
                                player.increaseGameSettingsInteractions();
                            }
                        });
                        return;
                    case 41744:
                        player.getHouse().setDoorsOpen(!player.getHouse().isDoorsOpen());
                        break;
                    case 41745:
                        player.getHouse().setBuildMode(!player.getHouse().isBuildMode());
                        break;
                    case 41584:
                        player.toggleDisableEnterQuickChat();
                        break;
                    case 41585:
                        player.switchTimeStamps();
                        break;
                    case 41586:
                        player.switchHidePublicEffects();
                        break;
                    case 41587:
                        player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                            @Override
                            public void run(Player player) {
                                player.setChatPrefix(getSlotId());
                                player.increaseGameSettingsInteractions();
                            }
                        });
                        return;
                    case 41733:
                        player.switchSplitPrivateChat();
                        break;
                    case 41589:
                    case 41590:
                    case 41591:
                    case 41592:
                    case 41593:
                    case 41594:
                    case 41595:
                    case 40950:
                    case 40951:
                    case 40952:
                        player.getPackets().sendIComponentSettings(365, 9, 0, 20, 2);
                        player.getTemporaryAttributtes().put(Key.CHAT_SETUP, mapId);
                        return;
                    case 41588:
                        player.resetChatSetup();
                        break;
                    case 41713:
                        player.switchTooglePlayerNotification();
                        break;
                    case 41503:
                        player.switchToogleAbilityCooldownTimer();
                        break;
                    default:
                        player.getPackets().sendPanelBoxMessage("unhandled structId "+mapId+", ");
                        break;
                }
            } else if(componentId == 17) {
                Integer structId = (Integer) player.getTemporaryAttributtes().remove("scrollBarStructId");
                if (structId == null) {
                    player.increaseGameSettingsInteractions();
                    return;
                }
                switch(structId) {
                    case 4195:
                        player.setZoomSensitivity(slotId);
                        break;
                    case 41766:
                    case 41767:
                        player.setKeyboardSensitivity(structId == 41766, (structId == 41766 ? 100 : 70) + slotId);
                        break;
                    case 41768:
                    case 41769:
                    case 41770:
                    case 41771:
                        player.setMouseSensitivity(structId == 41768 || structId == 41770, (structId == 41768 || structId == 41770 ? 4 : 3) + slotId);
                        break;
                    case 41663:
                        player.setTransparency(slotId);
                        break;
                    case 41666:
                        player.setDynamicBackpackColumns(slotId);
                        break;
                    case 41754:
                        player.setVirtualCapIcon(slotId);
                        break;
                    case 41755:
                        player.setXpCapIcon(slotId);
                        break;
                }
            }
            player.increaseGameSettingsInteractions();
        } else if (interfaceId == 137) {
            if (componentId == 62) {
                if (slotId == 0) {
                    player.getVarsManager().sendVarBit(18796, 1);
                    player.getInterfaceManager().sendExpandOptionsInterface(1468);
                } else if (slotId == 1) {
                    player.getVarsManager().sendVarBit(18796, 2);
                    player.getInterfaceManager().sendExpandOptionsInterface(1468);
                } else if (slotId == 2) {
                    if (player.isChatButtonsInMenu()) {
                        player.getVarsManager().sendVarBit(18796, 2);
                        player.getInterfaceManager().sendExpandOptionsInterface(1468);
                    } else
                        player.switchChatButtonsCollapsed();
                }
            }





            if (componentId == 68) {
                if (slotId == 0) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                        player.setPublicStatus(InterfaceManager.getNextStatus(player.getPublicStatus()));
                    else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                        player.setPublicStatus(0);
                    else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                        player.setPublicStatus(1);
                    else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
                        player.setPublicStatus(2);
                } else if (slotId == 2)
                    player.switchAlwaysChatOnMode();
                else if (slotId == 4) {
//                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
//                        PlayerReporting.report(player);
//                    else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
//                        PlayerReporting.reportAPlayer(player, player.getDisplayName());
//                    else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
//                        PlayerReporting.reportABug(player);
                } else if (slotId == 6) {
                    player.getInterfaceManager().sendExpandOptionsInterface(1468);
                }
            } else if (componentId == 65) {
                int status = packetId == PacketRepository.ACTION_BUTTON1_PACKET ? -1 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 1 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 3 : 2;
                if (slotId == 0) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                        player.setGameStatus(InterfaceManager.getNextStatus(player.getGameStatus()));
                    else
                        player.setGameStatus(status);
                } else if(slotId == 1) {
                    player.toggleBroadCastMessages();
                } else if (slotId == 3) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                        int nextStatus = player.getLocalChatStatus() == 3 ? 0 : (player.getLocalChatStatus() + 1);
                        player.setLocalChatStatus(nextStatus);
                    } else
                        player.setLocalChatStatus(status);
                } else if (slotId == 4) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                        player.getFriendsIgnores().setPrivateStatus(InterfaceManager.getNextStatus(player.getFriendsIgnores().getPrivateStatus()));
                    else
                        player.getFriendsIgnores().setPrivateStatus(status);
                } else if (slotId == 5) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                        player.getFriendsIgnores().setFriendsChatStatus(InterfaceManager.getNextStatus(player.getFriendsIgnores().getFriendsChatStatus()));
                    else
                        player.getFriendsIgnores().setFriendsChatStatus(status);
                } else if (slotId == 6) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                        player.setClanStatus(InterfaceManager.getNextStatus(player.getClanStatus()));
                    else
                        player.setClanStatus(status);
                } else if (slotId == 7) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                        player.setGuestClanStatus(InterfaceManager.getNextStatus(player.getGuestClanStatus()));
                    else
                        player.setGuestClanStatus(status);
                } else if (slotId == 8) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                        player.setGroupStatus(InterfaceManager.getNextStatus(player.getGroupStatus()));
                    else
                        player.setGroupStatus(status);
                } else if (slotId == 9) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                        player.setTradeStatus(InterfaceManager.getNextStatus(player.getTradeStatus()));
                    else
                        player.setTradeStatus(status);
                } else if (slotId == 10) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                        player.setAssistStatus(InterfaceManager.getNextStatus(player.getAssistStatus()));
                    else
                        player.setAssistStatus(status);
                }
            } else if(componentId == 201) {
                if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {//chat badge

                } else if(packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
                    player.switchProfanityFilter();
                } else if(packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
                    player.switchAlwaysChatOnMode();
                } else if(packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
                    player.toggleBroadCastMessages();
                }
            } else if (componentId == 205) {
                int status = packetId == PacketRepository.ACTION_BUTTON1_PACKET ? -1 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 1 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 3 : 2;
                if (status == -1)
                    return;
                player.setGameStatus(status);
            } else if (componentId == 210) {
                int status = packetId == PacketRepository.ACTION_BUTTON1_PACKET ? -1 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 1 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 3 : 2;
                if (status == -1)
                    return;
                player.setLocalChatStatus(status);
            } else if (componentId == 215) {
                int status = packetId == PacketRepository.ACTION_BUTTON1_PACKET ? -1 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 1 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 3 : 2;
                if (status == -1)
                    return;
                player.getFriendsIgnores().setPrivateStatus(status);
            } else if (componentId == 220) {
                int status = packetId == PacketRepository.ACTION_BUTTON1_PACKET ? -1 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 1 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 3 : 2;
                if (status == -1)
                    return;
                player.getFriendsIgnores().setFriendsChatStatus(status);
            } else if (componentId == 225) {
                int status = packetId == PacketRepository.ACTION_BUTTON1_PACKET ? -1 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 1 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 3 : 2;
                if (status == -1)
                    return;
                player.setClanStatus(status);
            } else if (componentId == 230) {
                int status = packetId == PacketRepository.ACTION_BUTTON1_PACKET ? -1 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 1 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 3 : 2;
                if (status == -1)
                    return;
                player.setGuestClanStatus(status);
            } else if (componentId == 235) {
                int status = packetId == PacketRepository.ACTION_BUTTON1_PACKET ? -1 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 1 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 3 : 2;
                if (status == -1)
                    return;
                player.setTradeStatus(status);
            } else if (componentId == 240) {
                int status = packetId == PacketRepository.ACTION_BUTTON1_PACKET ? -1 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 1 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 3 : 2;
                if (status == -1)
                    return;
                player.setAssistStatus(status);
            }  else if (componentId == 245) {
                int status = packetId == PacketRepository.ACTION_BUTTON1_PACKET ? -1 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 1 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 3 : 2;
                if (status == -1)
                    return;
                player.setGroupStatus(status);
            }
        } else if (interfaceId == 517) {

            // Toggle Bank 1 <-> Bank 2 on primary click of component 127
            if (componentId == 127 && packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                if (player.gimBank.isOpen()) {
                    player.sendMessage("Not available for GIM bank.");
                    return;
                }

                // Decide next bank based on current one (only toggles 0 and 1)
                int currentIdx = player.getBanks().indexOf(player.getBank());
                if (currentIdx != 0 && currentIdx != 1) currentIdx = 0;
                int nextIdx = (currentIdx == 0) ? 1 : 0;

                openBankByIndex(player, nextIdx); // helper below
                return;
            }




            if (componentId == 14 || componentId == 33) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().depositItem(slotId, player.gimBank.getCurrentBank().getDefaultInteractionAmount(), true, componentId);
                    } else {
                        player.getBank().depositItem(slotId, player.getBank().getDefaultInteractionAmount(), true, componentId);
                    }
                } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().depositItem(slotId, 1, true, componentId);
                    } else {
                        player.getBank().depositItem(slotId, 1, true, componentId);
                    }
                } else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().depositItem(slotId, 5, true, componentId);
                    } else {
                        player.getBank().depositItem(slotId, 5, true, componentId);
                    }
                } else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().depositItem(slotId, 10, true, componentId);
                    } else {
                        player.getBank().depositItem(slotId, 10, true, componentId);
                    }
                } else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().depositLastAmount(slotId, componentId);
                    } else {
                        player.getBank().depositLastAmount(slotId, componentId);
                    }
                } else if (packetId == PacketRepository.ACTION_BUTTON9_PACKET) {
                    player.sendInputInteger("Enter Amount:", new InputIntegerEvent() {
                        @Override
                        public void run(Player player) {
                            final int value = getInteger();
                            if (value < 0)
                                return;
                            if (player.gimBank.isOpen()) {
                                player.gimBank.getCurrentBank().setLastX(value);
                            } else
                                player.getBank().setLastX(value);
                            if (player.gimBank.isOpen()) {
                                player.gimBank.getCurrentBank().depositItem(slotId, value, true, componentId);
                            } else {
                                player.getBank().depositItem(slotId, value, true, componentId);
                            }
                        }
                    });
                } else if (packetId == PacketRepository.ACTION_BUTTON6_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().depositItem(slotId, Integer.MAX_VALUE, true, componentId);
                    } else {
                        player.getBank().depositItem(slotId, Integer.MAX_VALUE, true, componentId);
                    }
                } else if (componentId == 14 && packetId == PacketRepository.ACTION_BUTTON7_PACKET) {
                    if (slotId < 0 || slotId >= 28)
                        return;
                    Item item = player.getInventory().getItem(slotId);
                    if (item == null)
                        return;
                    ButtonHandler.sendWear(player, slotId, item.getId());
                }  else if (packetId == PacketRepository.ACTION_BUTTON8_PACKET)
                    if (player.gimBank.isOpen())
                        player.gimBank.getCurrentBank().sendExamineInteractionItem(componentId, slotId);
                    else
                        player.getBank().sendExamineInteractionItem(componentId, slotId);
            } else if (componentId == 27) {
                if (slotId < 0 || slotId >= BodyDefinitions.disabledSlots.length)
                    return;
                Item item = player.getEquipment().getItem(slotId);
                if (item == null)
                    return;
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    if (player.gimBank.isOpen())
                        player.gimBank.getCurrentBank().depositItem(slotId, Integer.MAX_VALUE, true, componentId);
                    else
                        player.getBank().depositItem(slotId, Integer.MAX_VALUE, true, componentId);
                } else if (packetId == PacketRepository.ACTION_BUTTON6_PACKET) {
                    ButtonHandler.sendRemove(player, slotId);
                } else if (packetId == PacketRepository.ACTION_BUTTON8_PACKET)
                    player.getPackets().sendInterfaceMessage(517, 27, 0, slotId, ItemExaminesDataParser.getExamine(item));
            } else if (componentId == 29 || componentId == 289) {
                player.getBank().ToggleEquipmentStats();
            } else if (componentId >= 37 && componentId <= 46) {
                if (player.gimBank.isOpen()) {
                    player.sendMessage("This feature is not available for GIM banks.");
                    return;
                }
                int type = (componentId - 37) / 3;
                switch (type) {
                    case 0:
                        player.getBank().depositAllInventory(true);
                        break;
                    case 1:
                        player.getBank().depositAllEquipment(true);
                        break;
                    case 2:
                        player.getBank().depositAllBob(true);
                        break;
                    case 3:
                        player.getBank().depositMoneyPouch(true);
                        break;
                }
            } else if (componentId == 63 || componentId == 64) {
                int option = componentId == 64 ? 10 : packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 1 :
                        packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 2 : packetId == PacketRepository.ACTION_BUTTON4_PACKET ? 3 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 4 :
                                packetId == PacketRepository.ACTION_BUTTON9_PACKET ? 5 : packetId == PacketRepository.ACTION_BUTTON6_PACKET ? 6 : packetId == PacketRepository.ACTION_BUTTON7_PACKET ? 7 :
                                        packetId == PacketRepository.ACTION_BUTTON10_PACKET ? 8 : 9;
                if (player.gimBank.isOpen()) {
                    player.getPackets().sendGameMessage("You cant use presets on gim bank.");
                    player.getPackets().sendConfigByFile(39433, 0);
                    return;
                }
                player.getBank().loadPreset(option, Bank.LOAD_ALL);
            } else if (componentId >= 87 && componentId <= 100 || (componentId == 108)) {
                if (player.gimBank.isOpen()) {
                    player.gimBank.getCurrentBank().setDefaultInteractionAmount(componentId == 97 ? Integer.MAX_VALUE : componentId >= 100 ? 11 : (((componentId - 87) / 3) * 5));
                } else
                    player.getBank().setDefaultInteractionAmount(componentId == 97 ? Integer.MAX_VALUE : componentId >= 100 ? 11 : (((componentId - 87) / 3) * 5));
                if (componentId == 108) {
                    player.sendIComponentInputInteger(517, 105, 10, new InputIntegerComponentEvent() {

                        @Override
                        public void run(Player player) {
                            if (player.gimBank.isOpen()) {
                                player.gimBank.getCurrentBank().setLastX(getInteger());
                            } else
                                player.getBank().setLastX(getInteger());
                        }
                    });
                }
            } else if (componentId == 116) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().toggleLeavePlaceHolders();
                    } else
                        player.getBank().toggleLeavePlaceHolders();
                } else {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().toggleLeavePlaceHolders();
                    } else
                        player.getBank().removeAllPlaceHolders(true);
                }
            } else if (componentId == 120) {
                if (player.gimBank.isOpen()) {
                    player.gimBank.getCurrentBank().switchWithdrawNotes();
                } else
                    player.getBank().switchWithdrawNotes();
            } else if (componentId == 133 || componentId == 134) {
                if (player.gimBank.isOpen()) {
                    player.gimBank.getCurrentBank().setConfigPresetsTab(componentId == 134);
                } else
                    player.getBank().setConfigPresetsTab(componentId == 134);
            } else if(componentId == 147 || componentId == 151) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().setCurrentTab(componentId == 147 ? 0 : slotId - 1);
                    } else
                        player.getBank().setCurrentTab(componentId == 147 ? 0 : slotId - 1);
                } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.sendMessage("This feature is not available for GIM banks.");
                        return;
                    }
                    if (componentId == 147)
                        player.getBank().toggleTabPosition();
                    else
                        player.getBank().deleteTab(slotId);
                } else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.sendMessage("This feature is not available for GIM banks.");
                        return;
                    }
                    if (componentId == 147)
                        player.getBank().resetTabsOrder();
                    else
                        player.getBank().customiseTab(slotId - 2);
                }
            } else if (componentId == 204) {
                if (player.gimBank.isOpen()) {
                    player.sendMessage("This feature is not available for GIM banks.");
                    return;
                }
                player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                    @Override
                    public void run(Player player) {
                        player.getBank().setTabName(getSlotId());
                    }
                });
            } else if (componentId == 207 || componentId == 209) {
                if (player.gimBank.isOpen()) {
                    player.sendMessage("This feature is not available for GIM banks.");
                    return;
                }
                player.getBank().setTabIcon(componentId == 209 ? 0 : slotId == 30 ? 31 : slotId == 0 ? 30 : slotId);
            } else if (componentId == 210) {
                player.getBank().closeCustomiseTab();
            } else if(componentId == 184) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().withdrawDefaultAmount(slotId);
                    } else {
                        player.getBank().withdrawDefaultAmount(slotId);
                    }
                } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().withdrawItem(slotId, 1);
                    } else {
                        player.getBank().withdrawItem(slotId, 1);
                    }
                } else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().withdrawItem(slotId, 5);
                    } else {
                        player.getBank().withdrawItem(slotId, 5);
                    }
                } else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().withdrawItem(slotId, 10);
                    } else {
                        player.getBank().withdrawItem(slotId, 10);
                    }
                } else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().withdrawLastAmount(slotId);
                    } else {
                        player.getBank().withdrawLastAmount(slotId);
                    }
                } else if (packetId == PacketRepository.ACTION_BUTTON9_PACKET) {
                    player.sendInputInteger("Enter Amount:", new InputIntegerEvent() {
                        @Override
                        public void run(Player player) {
                            final int value = getInteger();
                            if (value < 0)
                                return;
                            if (player.gimBank.isOpen()) {
                                player.gimBank.getCurrentBank().setLastX(value);
                            } else
                                player.getBank().setLastX(value);
                            if (player.gimBank.isOpen()) {
                                player.gimBank.getCurrentBank().withdrawItem(slotId, value);
                            } else {
                                player.getBank().withdrawItem(slotId, value);
                            }
                        }
                    });
                } else if (packetId == PacketRepository.ACTION_BUTTON6_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().withdrawItem(slotId, Integer.MAX_VALUE);
                    } else {
                        player.getBank().withdrawItem(slotId, Integer.MAX_VALUE);
                    }
                } else if (packetId == PacketRepository.ACTION_BUTTON7_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().withdrawItemPlaceHolder(slotId);
                    } else {
                        player.getBank().withdrawItemPlaceHolder(slotId);
                    }
                } else if (packetId == PacketRepository.ACTION_BUTTON8_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().sendExamine(slotId);
                    } else {
                        player.getBank().sendExamineBankItem(slotId);
                    }
                } else if (packetId == PacketRepository.ACTION_BUTTON10_PACKET) {
                    if (player.gimBank.isOpen()) {
                        player.gimBank.getCurrentBank().doExtraBankAction(slotId);
                    } else {
                        player.getBank().doExtraBankAction(slotId);
                    }
                }
            } else if (componentId == 225 || componentId == 241) {
                if (componentId == 241)
                    player.getBank().setCurrentFilter(0);
                else
                    player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                        @Override
                        public void run(Player player) {
                            player.getBank().setCurrentFilter(getSlotId());
                        }
                    });
            } else if (componentId >= 51 && componentId <= 59) {
                if (player.gimBank.isOpen())
                    player.gimBank.getCurrentBank().setInteractionTab(componentId == 51 ? 0 : componentId == 59 ? 1 : 2);
                else
                    player.getBank().setInteractionTab(componentId == 51 ? 0 : componentId == 59 ? 1 : 2);
            } else if (componentId == 112) {
                if (player.gimBank.isOpen()) {
                    player.getPackets().sendGameMessage("You cant use presets on gim bank.");
                    player.getPackets().sendConfigByFile(39433, 0);
                    return;
                }
                if (slotId == 0)
                    player.getBank().openBankPresetsTab();
                else
                    player.getBank().loadPreset(slotId - 1, Bank.LOAD_ALL);
            } else if (componentId == 247) {
                if (player.gimBank.isOpen()) {
                    player.getPackets().sendGameMessage("You cant use presets on gim bank.");
                    player.getPackets().sendConfigByFile(39433, 0);
                    return;
                }
                player.getBank().setSelectedPreset(slotId - 1);
            }  else if (componentId == 248 || componentId == 274) {
                if (player.gimBank.isOpen()) {
                    player.getPackets().sendGameMessage("You cant use presets on gim bank.");
                    player.getPackets().sendConfigByFile(39433, 0);
                    return;
                }

                player.getBank().togglePresetIncludeInventoryItems(componentId == 274 ? -1 : slotId);
            } else if (componentId == 249 || componentId == 276) {
                if (player.gimBank.isOpen()) {
                    player.getPackets().sendGameMessage("You cant use presets on gim bank.");
                    player.getPackets().sendConfigByFile(39433, 0);
                    return;
                }
                player.getBank().togglePresetIncludeEquipmentItems(componentId == 276 ? -1 : slotId);
            } else if (componentId == 250 || componentId == 278) {
                if (player.gimBank.isOpen()) {
                    player.getPackets().sendGameMessage("You cant use presets on gim bank.");
                    player.getPackets().sendConfigByFile(39433, 0);
                    return;
                }
                player.getBank().togglePresetFamiliar(componentId == 278 ? -1 : slotId);
            } else if (componentId == 281) {
                if (player.gimBank.isOpen()) {
                    player.getPackets().sendGameMessage("You cant use presets on gim bank.");
                    player.getPackets().sendConfigByFile(39433, 0);
                    return;
                }
                player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                    @Override
                    public void run(Player player) {
                        player.getBank().setPresetName(getSlotId());
                    }
                });
            } else if (componentId == 282) {
                if (player.gimBank.isOpen()) {
                    player.getPackets().sendGameMessage("You cant use presets on gim bank.");
                    player.getPackets().sendConfigByFile(39433, 0);
                    return;
                }
                player.getBank().overwritePreset();
            } else if (componentId == 285) {
                if (player.gimBank.isOpen()) {
                    player.getPackets().sendGameMessage("You cant use presets on gim bank.");
                    player.getPackets().sendConfigByFile(39433, 0);
                    return;
                }
                player.getBank().toggleLoadExactMatch();
            } else if (componentId >= 69 && componentId <= 78) {
                if (player.gimBank.isOpen()) {
                    player.getPackets().sendGameMessage("You cant use presets on gim bank.");
                    player.getPackets().sendConfigByFile(39433, 0);
                    return;
                }
                player.getBank().loadPreset((componentId - 69) / 3);
            } else if (componentId == 258 || componentId == 268) {
                if (player.gimBank.isOpen()) {
                    player.getPackets().sendGameMessage("You cant use presets on gim bank.");
                    player.getPackets().sendConfigByFile(39433, 0);
                    return;
                }
                player.getBank().handlePresetItemOptions(componentId, slotId, packetId);
            }
        } else if (interfaceId == 1462 || interfaceId == 1464) {
            if (player.getInterfaceManager().containsInventoryInter())
                return;
            if ((interfaceId == 1462 && componentId == 31) || (interfaceId == 1464 && componentId == 15))
                player.getEquipment().handleEquipment(interfaceId, componentId, slotId, slotId2, packetId);
            else if ((interfaceId == 1462 && componentId == 40) || (interfaceId == 1464 && componentId == 24)) {
                if (slotId == 0) {
                    if (interfaceId == 1464)
                        player.getInterfaceManager().openMenu(0, 3);
                } else if (slotId == 1) {
//                    player.stopAll();
//                    openItemsKeptOnDeath(player);
                } else if (slotId == 2) {
                    player.getToolBelt().openToolBelt();
                } else if (slotId == 3) {
                    player.getAuraManager().handleEquipmentOptions(false, packetId);
                }
            }
        } else if (interfaceId == 1465) {
            if (componentId == 22) {
                if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    player.getPriceCheckManager().openPriceCheck();
                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    player.getMoneyPouch().sendExamine();
                else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
                    if (player.getInterfaceManager().containsScreenInter() || player.getControlerManager().getControler() instanceof DungeonController) {
                        player.sendMessage("You cannot withdraw your coins right now.");
                        return;
                    }
                    if (player.getMoneyPouch().getTotal() > 2) {
                        player.sendInputInteger("Your money pouch contains " + Utils.getFormattedNumber(player.getMoneyPouch().getTotal()) + "." + "<br>How many would you like to withdraw?", new InputIntegerEvent() {
                            @Override
                            public void run(Player player) {
                                player.getMoneyPouch().withdrawPouch(getInteger());
                            }
                        });
                    } else if (player.getMoneyPouch().getTotal() == 1) {
                        player.getMoneyPouch().removeMoneyMisc(1);
                    } else
                        player.sendMessage("Your money pouch is empty.");
                } else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
                    //wealth eval
                } else if (packetId == PacketRepository.ACTION_BUTTON7_PACKET)
                    player.getInventionManager().openBagOfMaterialsInterface();
            } else if (componentId == 14) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    player.getSkills().switchXPDisplay();
                else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    player.getSkills().switchXPPopup();
                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    player.getInterfaceManager().openMenu(8, 2);
                else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
                    player.switchMakeXProgressWindow();
            } else if (componentId == 27) {
                player.getInterfaceManager().openGameTab(InterfaceManager.NOTES_TAB);
            } else if (componentId == 30) {// group system
                player.getInterfaceManager().openMenu(4, 4);
            } else if (componentId == 11) { // run energy
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    player.toogleRun(!player.isResting());
                    if (player.isResting())
                        player.stopAll();
                } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                    if (player.isResting()) {
                        player.stopAll();
                        return;
                    }
                    long currentTime = Utils.currentTimeMillis();
                    if (player.getEmotesManager().getNextEmoteEnd() >= currentTime) {
                        player.getPackets().sendGameMessage("You can't rest while perfoming an emote.");
                        return;
                    }
                    if (player.getLockDelay() >= currentTime) {
                        player.getPackets().sendGameMessage("You can't rest while perfoming an action.");
                        return;
                    }
                    player.stopAll();
                    player.getActionManager().setAction(new Rest());
                }
            } else if (componentId == 9) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    openWorldMap(player);
                } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                    player.getHintIconsManager().removeAll();
                    player.getPackets().sendConfig(2807, 0);
                } else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
                    player.getInterfaceManager().openFreeCam();
                }
            } else if (componentId == 18) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    player.getDialogueManager().startDialogue("HomeTeleportD");
                } else {
//                    player.stopAll();
//                    HomeTeleport.useLodestone(player, player.getPreviousLodestone());
                }
            }
        } else if (interfaceId == 1422) {
            if (componentId == 12 || componentId == 65 && slotId == 19) {
                player.getHintIconsManager().removeAll();
                player.getPackets().sendConfig(2807, 0);
            } else if (componentId == 65) {
                if (slotId == 1)
                    player.getVarsManager().sendVarBit(14109, player.getVarsManager().getBitValue(14109) == 0 ? 1 : 0);// TODO
                    // uknow.
                else if (slotId == 4)
                    player.getVarsManager().sendVarBit(14110, player.getVarsManager().getBitValue(14110) == 0 ? 1 : 0);// TODO
                    // uknow.
                else if (slotId == 12)
                    player.getVarsManager().sendVarBit(14111, player.getVarsManager().getBitValue(14111) == 0 ? 1 : 0);// TODO
                    // uknow.
                else if (slotId == 16)
                    player.getVarsManager().sendVarBit(14112, player.getVarsManager().getBitValue(14112) == 0 ? 1 : 0);// TODO
                // uknow.
            } else if (componentId == 128) {
                player.closeInterfaces();
            }
        } else if (interfaceId == 1920) {
            if (componentId == 2) {
                player.getInterfaceManager().openSettings();
            } else if (componentId == 7) {
                // XP orb sub-buttons:
                //   ACTION_BUTTON1 = Toggle XP Display  (broken: cache mismatch)
                //   ACTION_BUTTON2 = Toggle XP Pop-up   (works - see Skills#switchXPPopup)
                //   ACTION_BUTTON3 = Setup              (broken: no menu==8 handler)
                //   ACTION_BUTTON4 = Toggle Production Dialog (unrelated)
                // The Display and Setup paths are intercepted because the
                // persistent XP counter widget is unsupported on this build;
                // switchXPDisplay() also force-hides the widget and tells the
                // user. Setup gets its own dedicated message.
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    player.getSkills().switchXPDisplay();
                } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                    player.getSkills().switchXPPopup();
                } else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET) {
                    player.sendMessage("XP counter setup isn't available in this build.");
                } else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
                    player.switchMakeXProgressWindow();
                }
            } else if (componentId == 19)
                player.getInterfaceManager().openGameTab(InterfaceManager.NOTES_TAB);
            else if (componentId == 25) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    player.getInterfaceManager().openMenu(8, 1);
                else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    player.getInterfaceManager().openMenu(8, 2);
                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    player.getInterfaceManager().openMenu(8, 3);
                else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
                    player.getInterfaceManager().openMenu(8, 4);
            } else if (componentId == 22)
                player.getInterfaceManager().openMenu(4, 4);
            else if (componentId == 14) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    player.getPackets().sendRunScript(5557, 1);
                    player.getMoneyPouch().setOpen(!player.getMoneyPouch().isOpen());
                    Logger.getGlobal().info(player.getMoneyPouch().isOpen());
                } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET) {
                    if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsBankInterface()) {
                        player.getPackets().sendGameMessage("Please finish what you're doing before opening the price checker.");
                        return;
                    }
                    player.stopAll();
                    player.getPriceCheckManager().openPriceCheck();
                } else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    player.getMoneyPouch().sendExamine();
                else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET) {
                    if (player.getInterfaceManager().containsScreenInter() || player.getControlerManager().getControler() instanceof DungeonController) {
                        player.sendMessage("You cannot withdraw your coins right now.");
                        return;
                    }
                    if (player.getMoneyPouch().getTotal() > 2) {
                        player.sendInputInteger("Your money pouch contains " + Utils.getFormattedNumber(player.getMoneyPouch().getTotal()) + "." + "<br>How many would you like to withdraw?", new InputIntegerEvent() {
                            @Override
                            public void run(Player player) {
                                player.getMoneyPouch().withdrawPouch(getInteger());
                            }
                        });
                    } else if (player.getMoneyPouch().getTotal() == 1) {
                        player.getMoneyPouch().removeMoneyMisc(1);
                    } else
                        player.sendMessage("Your money pouch is empty.");
                } else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
//                    if (player.getInterfaceManager().containsScreenInterface() || player.getInterfaceManager().containsBankInterface()) {
//                        player.getPackets().sendGameMessage("Please finish what you're doing before opening the wealth evaluator.");
//                        return;
//                    }
//                    player.stopAll();
//                    player.getInterfaceManager().getWealth();
                } else if (packetId == PacketRepository.ACTION_BUTTON6_PACKET) {
                    player.getInventionManager().openBagOfMaterialsInterface();
                }
            }
        } else if (interfaceId == 1506) {
            if (componentId == 6)
                Familiar.selectLeftOption(player);
            else if (componentId == 9) {
                if (player.getFamiliar() != null)
                    player.getFamiliar().getBob().takeBob();
            }else if (componentId == 10) {
                if (player.getFamiliar() != null)
                    player.getFamiliar().renewFamiliar();
            } else if (componentId == 25) {
                if (player.getFamiliar() != null)
                    player.getFamiliar().call();
            } else if (componentId == 30)
                player.getDialogueManager().startDialogue("DismissD");
            else if (componentId == 27 || componentId == 26) {
                player.getInterfaceManager().openGameTab(InterfaceManager.SUMMONING_TAB);
                if (player.getFamiliar() != null)
                    player.getFamiliar().sendFollowerDetails();
                if (player.getPet() != null)
                    player.getPet().sendFollowerDetails();
            } else if (componentId == 18) {
                if (player.getFamiliar() == null)
                    return;
                if (player.getFamiliar().getSpecialAttack() == SpecialAttack.CLICK)
                    player.getFamiliar().setSpecial(true, false);
                if (player.getFamiliar().hasSpecialOn())
                    player.getFamiliar().submitSpecial(player);
            } else if (componentId == 29) {
                if (player.getPet() != null)
                    player.getPet().call();
            } else if (componentId == 33) {
                player.getInterfaceManager().openGameTab(InterfaceManager.SUMMONING_TAB);
                if (player.getFamiliar() != null)
                    player.getFamiliar().sendFollowerDetails();
                if (player.getPet() != null)
                    player.getPet().sendFollowerDetails();
            } else if (componentId == 34) {
                PetPerkInterface.sendInterface(player);
            } else if (componentId == 14) {
                if (player.getPet() != null)
                    player.getPet().sendInteract();
                if (player.getFamiliar() != null)
                    player.getDialogueManager().startDialogue("FamiliarInteractD", player.getFamiliar());
            }
        } else if (interfaceId == 880) {
            if (componentId == 28) {
                player.setSummoningLeftClickOption(slotId);
            } else if (componentId == 2) {
                player.setPetLeftClickOption(slotId);
            } else if (componentId == 9) {
                player.setLegendaryPetLeftClickOption(slotId);
            } else if (componentId == 20) {
                player.getInterfaceManager().closeScreenInterface();
                player.refreshSummoningOrbOption();
            }
        } else if (interfaceId == 662) {
            if (player.getFamiliar() == null) {
                if (player.getPet() == null) {
                    return;
                }
                if (componentId == 41)
                    player.getPet().call();
                else if (componentId == 44)
                    player.getDialogueManager().startDialogue("DismissD");
                return;
            }
            if (componentId == 41 || componentId == 9) {
                if (player.getFamiliar() != null) {
                    player.getFamiliar().call();
                }
            } else if (componentId == 44 || componentId == 21) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    player.getDialogueManager().startDialogue("DismissD");
                else
                    player.getFamiliar().sendDeath(player);
            } else if (componentId == 47 || componentId == 29)
                player.getFamiliar().switchDisplayInv();
            else if (componentId == 71 || componentId == 12)
                player.getFamiliar().takeBob();
            else if (componentId == 74 || componentId == 15)
                player.getFamiliar().giveBob();
            else if (componentId == 77 || componentId == 18)
                player.getFamiliar().renewFamiliar();
            else if (componentId == 86 || componentId == 27) {
                if (player.getFamiliar().getSpecialAttack() == SpecialAttack.CLICK)
                    player.getFamiliar().setSpecial(true, false);
                if (player.getFamiliar().hasSpecialOn())
                    player.getFamiliar().submitSpecial(player);
            } else if (componentId == 88) {
                if (player.getFamiliar() == null)
                    return;
                player.getDialogueManager().startDialogue(new Dialogue() {
                    @Override
                    public void start() {
                        sendOptionsDialogue("Select an Option", "Set special usage speed", "Permanently set special usage speed", "Nevermind.");

                    }
                    @Override
                    public void run(int interfaceId, int componentId) {
                        switch(stage) {
                            case -1:
                                if (componentId == OPTION_1) {
                                    stage = 0;
                                    sendOptionsDialogue("Select a Speed", "Slow", "Medium", "Fast");
                                } else if (componentId == OPTION_2) {
                                    stage = 1;
                                    sendOptionsDialogue("Select a Speed", "Slow", "Medium", "Fast", "Remove permanent speed");
                                } else
                                    end();
                                break;
                            case 0:
                                if (componentId == OPTION_1) {
                                    if (player.getFamiliar() != null)
                                        player.getFamiliar().setSpecialSpeed(1);
                                } else if (componentId == OPTION_2) {
                                    if (player.getFamiliar() != null)
                                        player.getFamiliar().setSpecialSpeed(2);
                                } else if (componentId == OPTION_3) {
                                    if (player.getFamiliar() != null)
                                        player.getFamiliar().setSpecialSpeed(3);
                                }
                                end();
                                break;
                            case 1:
                                if (componentId == OPTION_1) {
                                    if (player.getFamiliar() != null)
                                        player.getFamiliar().setSpecialSpeed(1);
                                    player.alwaysUseSpecialSpeed = 1;
                                } else if (componentId == OPTION_2) {
                                    if (player.getFamiliar() != null)
                                        player.getFamiliar().setSpecialSpeed(2);
                                    player.alwaysUseSpecialSpeed = 2;
                                } else if (componentId == OPTION_3) {
                                    if (player.getFamiliar() != null)
                                        player.getFamiliar().setSpecialSpeed(3);
                                    player.alwaysUseSpecialSpeed = 3;
                                } else if (componentId == OPTION_4)
                                    player.alwaysUseSpecialSpeed = 0;
                                end();
                                break;
                        }
                    }
                    @Override
                    public void finish() {
                    }
                });
            } else if (componentId == 5) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    player.getFamiliar().getBob().removeItem(slotId, 1);
                else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    player.getFamiliar().getBob().removeItem(slotId, 5);
                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    player.getFamiliar().getBob().removeItem(slotId, 10);
                else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
                    player.getFamiliar().getBob().removeItem(slotId, Integer.MAX_VALUE);
                else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
                    player.sendInputInteger("Enter Amount:", new InputIntegerEvent() {

                        @Override
                        public void run(Player player) {
                            if (getInteger() <= 0)
                                return;
                            player.getFamiliar().getBob().removeItem(slotId, getInteger());
                        }
                    });
                } else if (packetId == PacketRepository.ACTION_BUTTON8_PACKET) {
                    Item item = player.getFamiliar().getBob().getBeastItems().get(slotId);
                    if (item == null)
                        return;
                    player.getPackets().sendBobInventoryMessage(0, slotId, ItemExaminesDataParser.getExamine(item));
                    if (ItemConstants.isTradeable(item))
                        player.getPackets().sendGameMessage("GE guide price: " + Utils.formatNumber(GrandExchange.getPrice(item.getId())) + " gp.");
                }
            }
        } else if (interfaceId == 671) {
            if (player.getFamiliar() == null || player.getFamiliar().getBob() == null)
                return;
            if (componentId == 27) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    player.getFamiliar().getBob().removeItem(slotId, 1);
                else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    player.getFamiliar().getBob().removeItem(slotId, 5);
                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    player.getFamiliar().getBob().removeItem(slotId, 10);
                else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
                    player.getFamiliar().getBob().removeItem(slotId, Integer.MAX_VALUE);
                else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
                    player.sendInputInteger("Enter Amount:", new InputIntegerEvent() {
                        @Override
                        public void run(Player player) {
                            if (player.getFamiliar() == null || player.getFamiliar().getBob() == null)
                                return;
                            final int value = getInteger();
                            if (value < 0) {
                                return;
                            }
                            player.getFamiliar().getBob().removeItem(slotId, value);
                        }
                    });
                }
            } else if (componentId == 32) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    player.getFamiliar().getBob().addItem(slotId, 1);
                else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    player.getFamiliar().getBob().addItem(slotId, 5);
                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    player.getFamiliar().getBob().addItem(slotId, 10);
                else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
                    player.getFamiliar().getBob().addItem(slotId, Integer.MAX_VALUE);
                else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET) {
                    player.sendInputInteger("Enter Amount:", new InputIntegerEvent() {
                        @Override
                        public void run(Player player) {
                            if (player.getFamiliar() == null || player.getFamiliar().getBob() == null)
                                return;
                            final int value = getInteger();
                            if (value < 0) {
                                return;
                            }
                            player.getFamiliar().getBob().addItem(slotId, value);
                        }
                    });
                }
            } else if (componentId == 0) {
                player.getFamiliar().takeBob();
            } else if (componentId == 1) {
                player.getFamiliar().giveBob();
            }
        } else if (interfaceId == 429) {
            if (componentId == 5) {
                player.getMusicsManager().toggleGlobalMute();
            } else if (componentId == 15 || componentId == 34 || componentId == 53 || componentId == 72) {
                player.getMusicsManager().switchIsTypeMuted((componentId - 15) / 19);
            }
        } else if (interfaceId == 187) {
//            if (componentId == 1) {
//                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
//                    player.getMusicsManager().playAnotherMusic(slotId / 2);
//                else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
//                    player.getMusicsManager().sendHint(slotId / 2);
//                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
//                    player.getMusicsManager().addToPlayList(slotId / 2);
//                else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
//                    player.getMusicsManager().removeFromPlayList(slotId / 2);
//            } else if (componentId == 4)
//                player.getMusicsManager().addPlayingMusicToPlayList();
//            else if (componentId == 11)
//                player.getMusicsManager().switchPlayListOn();
//            else if (componentId == 12)
//                player.getMusicsManager().clearPlayList();
//            else if (componentId == 14)
//                player.getMusicsManager().switchShuffleOn();
        } else if (interfaceId == 1430) {
            if (componentId == 11) {//cure poison

            } else if (componentId == 55) {
                submitSpecialRequest(player);
            } else if (componentId == 16) {
                player.getPrayer().handlePrayerOrbOption(packetId);
            } else if (componentId == 50) {// open pet interface
                player.getInterfaceManager().openGameTab(InterfaceManager.SUMMONING_TAB);
                if (player.getFamiliar() != null)
                    player.getFamiliar().sendFollowerDetails();
                if (player.getPet() != null)
                    player.getPet().sendFollowerDetails();
            }  else if (componentId == 51) {// summon pet
                PetPerkInterface.sendInterface(player);
            }  else if (componentId == 23) {
                Familiar.selectLeftOption(player);
            } else if (componentId == 31 || componentId == 42) {// Interact
                if (player.getPet() != null)
                    player.getPet().sendInteract();
                else if (player.getFamiliar() != null)
                    player.getDialogueManager().startDialogue("FamiliarInteractD", player.getFamiliar());
            } else if (componentId == 43 || componentId == 44) {
                player.getInterfaceManager().openGameTab(InterfaceManager.SUMMONING_TAB);
                if (player.getFamiliar() != null)
                    player.getFamiliar().sendFollowerDetails();
                if (player.getPet() != null)
                    player.getPet().sendFollowerDetails();
            } else if (componentId == 26 || componentId == 39) {// restore points

            } else if (componentId == 27 || componentId == 37)
                player.getFamiliar().getBob().takeBob();
            else if (componentId == 28 || componentId == 38)
                player.getFamiliar().getBob().giveBob();
            else if (componentId == 29 || componentId == 40)
                player.getFamiliar().renewFamiliar();
            else if (componentId == 46 || componentId == 48) {
                if (player.getFamiliar() != null)
                    player.getFamiliar().call();
                if (player.getPet() != null)
                    player.getPet().call();
            } else if (componentId == 49 || componentId == 47)
                player.getDialogueManager().startDialogue("DismissD");
            else if (componentId == 35 || componentId == 37) {
                if (player.getFamiliar().getSpecialAttack() == SpecialAttack.CLICK)
                    player.getFamiliar().setSpecial(true, false);
                if (player.getFamiliar().hasSpecialOn())
                    player.getFamiliar().submitSpecial(player);
            } else if (componentId == 57) {
                player.getCombatDefinitions().switchAutoRelatie();
            } else if (componentId >= 64 && componentId <= 233) {
                player.getActionbar().pushShortcut((componentId - 64) / 13, slotId2, packetId);
            } else if (componentId == 258)
                player.getActionbar().decreaseCurrentBar();
            else if (componentId == 259)
                player.getActionbar().increaseCurrentBar();
            else if (componentId == 254) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    player.getActionbar().setCurrentBar(0);
                else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    player.getActionbar().setCurrentBar(1);
                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    player.getActionbar().setCurrentBar(2);
                else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
                    player.getActionbar().setCurrentBar(3);
                else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET)
                    player.getActionbar().setCurrentBar(4);
                else if (packetId == PacketRepository.ACTION_BUTTON9_PACKET)
                    player.getActionbar().setCurrentBar(5);
                else if (packetId == PacketRepository.ACTION_BUTTON6_PACKET)
                    player.getActionbar().setCurrentBar(6);
                else if (packetId == PacketRepository.ACTION_BUTTON7_PACKET)
                    player.getActionbar().setCurrentBar(7);
                else if (packetId == PacketRepository.ACTION_BUTTON10_PACKET)
                    player.getActionbar().setCurrentBar(8);
                else if (packetId == PacketRepository.ACTION_BUTTON8_PACKET)
                    player.getActionbar().setCurrentBar(9);
            } else if (componentId == 262)
                player.getActionbar().switchLockBar();
        } else if (interfaceId == 1436) {
            if (componentId >= 17 && componentId <= 186) {
                player.getActionbar().pushShortcut((componentId - 17) / 13, slotId2, packetId);
            } else if (componentId == 13)
                player.getActionbar().decreaseCurrentBar();
            else if (componentId == 14)
                player.getActionbar().increaseCurrentBar();
            else if (componentId == 11) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    player.getActionbar().setCurrentBar(0);
                else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    player.getActionbar().setCurrentBar(1);
                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    player.getActionbar().setCurrentBar(2);
                else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
                    player.getActionbar().setCurrentBar(3);
                else if (packetId == PacketRepository.ACTION_BUTTON5_PACKET)
                    player.getActionbar().setCurrentBar(4);
                else if (packetId == PacketRepository.ACTION_BUTTON6_PACKET)
                    player.getInterfaceManager().openMenu(8, 3);
            } else if (componentId == 15) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    player.getActionbar().helpTrashCan();
                else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    player.getDialogueManager().startDialogue("ClearActionBar");
            } else if (componentId == 7)
                player.getActionbar().switchBlockIncomingShareOffers();
        } else if (interfaceId >= 1670 && interfaceId <= 1673) {
            int currentBar = player.getActionbar().getMultiActionBar()[interfaceId - 1670] - 1;
            if (interfaceId == 1670 && componentId >= 18 && componentId <= 187) {
                player.getActionbar().pushShortcut(currentBar, (componentId - 18) / 13, slotId2, packetId);
            } else if ((interfaceId == 1671 || interfaceId == 1672 || interfaceId == 1673) && componentId >= 13 && componentId <= 182) {
                player.getActionbar().pushShortcut(currentBar, (componentId - 13) / 13, slotId2, packetId);
            } else if (componentId == 5 || componentId == 6) {
                int bar = packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 1 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 2 : packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 3 : packetId == PacketRepository.ACTION_BUTTON4_PACKET ? 4 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 5 :
                        packetId == PacketRepository.ACTION_BUTTON9_PACKET ? 6 :packetId == PacketRepository.ACTION_BUTTON6_PACKET ? 7 :packetId == PacketRepository.ACTION_BUTTON7_PACKET ? 8 : packetId == PacketRepository.ACTION_BUTTON10_PACKET ? 9 : packetId == PacketRepository.ACTION_BUTTON8_PACKET ? 10 : -1;
                if (componentId == 5 && packetId == PacketRepository.ACTION_BUTTON9_PACKET)
                    bar = -1;
                if (bar == -1)
                    return;
                player.getActionbar().setMultiActionBar(interfaceId - 1670, bar);
            }
        } else if (interfaceId == 1218) {
            player.getSkills().sendSkillMenu(componentId);
        } else if (interfaceId == 1458 || interfaceId == 1457) {
            if ((interfaceId == 1458 && componentId == 39) || (interfaceId == 1457 && componentId == 15))
                player.getPrayer().delayUsePrayer(slotId, false);
            else if ((interfaceId == 1458 && componentId == 26) || (interfaceId == 1457 && componentId == 28)) {
                player.getPrayer().openPrayerPresetsInterface();
            } else if ((interfaceId == 1458 && componentId == 32) || (interfaceId == 1457 && componentId == 34))
                player.getPrayer().toggleFilter();
            else if ((interfaceId == 1458 && componentId == 47) || (interfaceId == 1457 && componentId == 42))
                player.getPrayer().toggleShowActivePrayersOnly();
            else if ((interfaceId == 1458 && componentId == 18) || (interfaceId == 1457 && componentId == 51)) {
                if (player.getPrayer().hasPrayersOn()) {
                    player.getPrayer().closeAllPrayers();
                } else
                    player.getPrayer().delaySwitchQuickPrayers();
            }
        } else if (interfaceId == 1505) {
            if (componentId == 2)
                player.getPrayer().handlePrayerOrbOption(packetId);
        } else if (interfaceId == 1890) {// prayer presets
            if (componentId == 4) {
                player.getPrayer().selectPreset(slotId);
            } else if (componentId == 6) {
                player.getPrayer().deletePreset(slotId);
            } else if (componentId == 7) {
                player.getPrayer().selectPreset(slotId);
                player.getTemporaryAttributtes().put(Key.EDIT_PRAYER_PRESET, slotId);
            } else if (componentId == 21)
                player.getInterfaceManager().removeCentralOverlayInterface();
            else if (componentId == 23 || componentId == 99) {
                player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                    @Override
                    public void run(Player player) {
                        player.getTemporaryAttributtes().put(Key.PRAYER_PRESET_NAME_SLOT, getSlotId());
                    }
                });
            } else if (componentId == 38)
                player.getPrayer().delayUsePrayer(slotId, true);
            else if (componentId == 48) {
                Integer presetType = (Integer) player.getTemporaryAttributtes().remove(Key.PRAYER_PRESET_TYPE);
                Integer nameSlot = (Integer) player.getTemporaryAttributtes().remove(Key.PRAYER_PRESET_NAME_SLOT);
                if (nameSlot == null || nameSlot == 0)
                    nameSlot = 1;
                if (presetType == null)
                    presetType = 0;
                player.getPrayer().createPreset(nameSlot, presetType == 1);
            } else if (componentId == 53 || componentId == 54)
                player.getTemporaryAttributtes().put(Key.PRAYER_PRESET_TYPE, componentId == 54 ? 1 : 0);
            else if (componentId == 81 || componentId == 90) {
                Integer index = (Integer) player.getTemporaryAttributtes().get(Key.EDIT_PRAYER_PRESET);
                if (index == null)
                    index = player.getPrayer().getSelectedPresetIndex();
                player.getPrayer().editLeftClickOption(componentId == 90 ? -1337 : index);
            } else if (componentId == 62 || componentId == 86) {
                player.getTemporaryAttributtes().remove(Key.EDIT_PRAYER_PRESET);
                player.getTemporaryAttributtes().remove(Key.PRAYER_PRESET_TYPE);
                player.getTemporaryAttributtes().remove(Key.PRAYER_PRESET_NAME_SLOT);
                player.getTemporaryAttributtes().remove(Key.OPTION_MENU);
                player.getTemporaryAttributtes().remove(Key.EDIT_PRAYER_PRESET);
            } else if (componentId == 76) {
                Integer nameSlot = (Integer) player.getTemporaryAttributtes().remove(Key.PRAYER_PRESET_NAME_SLOT);
                Integer index = (Integer) player.getTemporaryAttributtes().remove(Key.EDIT_PRAYER_PRESET);
                if (nameSlot == null || nameSlot == 0)
                    nameSlot = 1;
                if (index == null)
                    index = player.getPrayer().getSelectedPresetIndex();
                player.getPrayer().editPreset(index, nameSlot);
            }
        } else if (interfaceId == 1929) {
            if (componentId == 167) {
                player.getInterfaceManager().removeCentralOverlayInterface();
            } else if (componentId == 95) {
                player.getAuraManager().selectAura(slotId);
            } else if (componentId == 16) {
                player.getAuraManager().buyOrActivateAuraAura();
            } else if (componentId == 7) {
                player.getAuraManager().toggleFavouriteAura();
            } else if (componentId == 153 || componentId == 162)
                player.getDialogueManager().continueDialogue(interfaceId, componentId);
            else if(componentId == 47 || componentId == 38) {
                player.getAuraManager().extendAura(componentId == 38);
            } else if (componentId >= 22 && componentId <= 27)
                player.getAuraManager().resetAura(slotId2);
            else if (componentId == 122)
                player.getAuraManager().toggleShowListView();
            else if (componentId == 125)
                player.getAuraManager().toggleHideDismissAuraWaringMessage();
            else if (componentId == 128)
                player.getAuraManager().toggleOpenOnFavourites();
            else if (componentId == 116)
                player.getAuraManager().toggleAutoExtendUnFav();
            else if (componentId == 119)
                player.getAuraManager().toggleAutoExtendFav();
            else if (componentId == 107 || componentId == 103)
                player.getAuraManager().setAutoExtendDoubleTime(componentId == 103);
        } else if (interfaceId == 1503) {
            if (componentId == 13)
                submitSpecialRequest(player);
            else if (componentId >= 37 && componentId <= 45) {
                player.getCombatDefinitions().setCombatExperienceStyle((componentId - 37) / 4);
            } else if (componentId == 50)
                player.getCombatDefinitions().switchAutoRelatie();
            else if (componentId == 11) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    player.getCombatDefinitions().switchSheathe();
                else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    player.switchBuffTimer();
            }
        } else if (interfaceId == 1617) {
            if (componentId == 0) {
                if (slotId == 0 || slotId == 2)
                    player.getCombatDefinitions().setStrengthMenu(slotId == 2 ? 1 : 0);
                else if (slotId >= 16 && slotId <= 22)
                    player.getCombatDefinitions().setMagicAbilityMenu((slotId - 16) / 2);
                else if (slotId == 24 || slotId == 26)
                    player.getCombatDefinitions().setDefenceMenu(slotId == 26 ? 1 : 0);
            }

        } else if (interfaceId == 1461 || interfaceId == 1459) {
            if (componentId == 7)  {
                if(slotId == 11)
                    player.getCombatDefinitions().toggleFilteredAbilities();
                else
                    player.getCombatDefinitions().setMagicAbilityMenu(slotId - 7);
            } else if(componentId == 1)
                player.getActionbar().useAbility(new MagicAbilityShortcut(slotId), packetId);
        } else if (interfaceId == 1452 || interfaceId == 1456) {
            if (componentId == 7)
                if(slotId == 11)
                    player.getCombatDefinitions().toggleFilteredAbilities();
            if (componentId == 1)
                player.getActionbar().useAbility(new RangeAbilityShortcut(slotId), packetId);
        } else if (interfaceId == 1460 || interfaceId == 1450) {
            if ((interfaceId == 1460 && componentId == 5) || (interfaceId == 1450 && componentId == 0)) {
                if(slotId == 11)
                    player.getCombatDefinitions().toggleFilteredAbilities();
                else
                    player.getCombatDefinitions().setIsOnStrengthMenu(slotId == 8);
            }
            if ((interfaceId == 1460 && componentId == 1) || (interfaceId == 1450 && componentId == 3))
                player.getActionbar().useAbility(player.getCombatDefinitions().isOnStrengthMenu() ? new StrengthAbilityShortcut(slotId) : new MeleeAbilityShortcut(slotId), packetId);
        } else if (interfaceId == 1880 || interfaceId == 1883) {
            if (componentId == 1)
                player.getActionbar().useAbility(player.getCombatDefinitions().isOnConstitutionMenu() ? new HealAbilityShortcut(slotId) : new DefenceAbilityShortcut(slotId), packetId);
            else if (componentId == 7)  {
                if(slotId == 11)
                    player.getCombatDefinitions().toggleFilteredAbilities();
                else
                    player.getCombatDefinitions().setIsOnConstitutionMenu(slotId == 8);
            }
        } else if (interfaceId >= 1884 && interfaceId <= 1887) {
            if(componentId == 7)
                if(slotId == 11)
                    player.getCombatDefinitions().toggleFilteredAbilities();
            if (componentId == 1)
                player.getActionbar().useAbility(new MagicAbilityShortcut(slotId), packetId);
        } else if (interfaceId == 523|| interfaceId == 522) {
            if (componentId == 107 || componentId == 46)
                player.getInterfaceManager().sendMagicAbilities();
        } else if (interfaceId == 91) {
            Object oManager = player.getEliteDungeonsManager().getParty() != null ? player.getEliteDungeonsManager() : player.getDungeoneeringManager().getParty() != null ? player.getDungeoneeringManager() : null;
            if (componentId == 35) {
                player.sendChoosePartyTypeDialogue();
                return;
            }
            if (oManager instanceof DungManager) {
                DungManager manager = (DungManager) oManager;
                if (componentId >= 19 && componentId <= 32) {
                    int playerIndex = (componentId - 19) / 3;
                    if ((componentId & 0x3) != 0)
                        manager.pressOption(playerIndex, packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 1 : 2);
                    else
                        manager.pressOption(playerIndex, 3);
                } else if (componentId == 35)
                    manager.formParty();
                else if (componentId == 17 || componentId == 34)
                    manager.checkLeaveParty();
                else if (componentId == 36)
                    manager.invite();
                else if (componentId == 1)
                    manager.changeComplexity();
                else if (componentId == 3)
                    manager.changeFloor();
                else if (componentId == 0)
                    manager.openResetProgress();
                else if (componentId == 42)
                    manager.switchGuideMode();
            } else  if (oManager instanceof EliteDungeonsManager) {
                EliteDungeonsManager manager = (EliteDungeonsManager) oManager;
                if (componentId >= 19 && componentId <= 32) {
                    int playerIndex = (componentId - 19) / 3;
                    if ((componentId & 0x3) != 0)
                        manager.pressOption(playerIndex, packetId == PacketRepository.ACTION_BUTTON1_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 1 : 2);
                    else
                        manager.pressOption(playerIndex, 3);
                } else if (componentId == 35)
                    manager.formParty();
                else if (componentId == 17 || componentId == 34)
                    manager.checkLeaveParty();
                else if (componentId == 36)
                    manager.invite();
            }
        } else if (interfaceId == 949) {
            Boolean eliteDungeon = (Boolean) player.getTemporaryAttributtes().get(Key.ELITE_DUNGEON_TYPE);
            if (eliteDungeon == null) {
                player.getPackets().sendGameMessage("Something went wrong, please try again.");
                player.closeInterfaces();
                return;
            }
            if (componentId == 61) {
                if (eliteDungeon)
                    player.getEliteDungeonsManager().acceptInvite();
                else
                    player.getDungeoneeringManager().acceptInvite();
            }
            else if (componentId == 75 || componentId == 115 || componentId == 68)
                player.closeInterfaces();
        } else if (interfaceId == 938) {
            // Confirm is 71 in the current cache (39 kept for the old layout).
            // 71 must be matched here BEFORE the helper, because the helper's old
            // fallback maps componentId=71 to complexity 4 — which is exactly why
            // pressing confirm used to "reset back to 4 and not proceed".
            if (componentId == 39 || componentId == 71)
                player.getDungeoneeringManager().confirmComplexity();
            else {
                int complexity = getDungeoneeringComplexityComponent(componentId);
                if (complexity != -1)
                    player.getDungeoneeringManager().selectComplexity(complexity);
            }
        } else if (interfaceId == 947) {
            if (componentId == 721 || componentId == 733 || componentId == 254) {
               player.getDungeoneeringManager().confirmFloor();
            } else {
                int floor = getDungeoneeringFloorComponent(componentId);
                if (floor != -1)
                    player.getDungeoneeringManager().selectFloor(floor);
            }
        } else if (interfaceId == 1719) {
            if (componentId == 6) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    ItemSets.sendComponents(player, slotId2);
                } else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    ItemSets.exchangeSet(player, slotId2);
                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    ItemSets.examineSet(player, slotId2);
            }
        } else if (interfaceId == 1721 && player.getInterfaceManager().containsInterface(1719)) {
            if (componentId == 7) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                    ItemSets.sendComponentsBySlot(player, slotId, slotId2);
                else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    ItemSets.exchangeSet(player, slotId, slotId2);
            }
        } else if (interfaceId == 1490) {
            if (componentId == 28)
                player.toggleTargetInfomationUnDocked();
        } else if (interfaceId == 1559) {
            for(int i = 123; i <= 131;i++)
                player.getPackets().sendHideIComponent(1559, i, true);
        } else if (interfaceId == 1560) {
            if (componentId == 25)
                player.getPlayerExamineManager().closeExamineDetails();
            else if (componentId == 30 || (componentId == 66 || componentId == 57) || (componentId == 84 || componentId == 39) || (componentId == 75 || componentId == 48) || (componentId == 111 || componentId == 120)) {
                player.getPlayerExamineManager().openTab(componentId == 30 ? 0 : componentId == 66 || componentId == 57  ? 1 : componentId == 84 || componentId == 39 ? 2 : componentId == 75 || componentId == 48 ? 3 : 4);
            }
        } else if (interfaceId == 1561) {
            if (componentId == 21)
                player.getPlayerExamineManager().changePersonalMessage();
            else if (componentId == 35)
                player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                    @Override
                    public void run(Player player) {
                        player.getPlayerExamineManager().setStatus(getSlotId());
                    }
                });
            else if (componentId == 40)
                player.getPlayerExamineManager().clearPersonalMessage();
            else if (componentId == 45)
                player.sendDropDownMenuEvent(new DropDownMenuEvent() {

                    @Override
                    public void run(Player player) {
                        player.getPlayerExamineManager().setPrivacy(getSlotId());
                    }
                });
        } else if (interfaceId == 1446) {
            if(componentId == 94) {
                player.getInterfaceManager().closeMenu();
                player.getPlayerExamineManager().openExamineSettings();
            }
        } else if (interfaceId == 1311 || interfaceId == 1843 || interfaceId == 1627 || interfaceId == 1830 || interfaceId == 1845) {
            if (interfaceId == 1845)
                player.getDialogueManager().continueDialogue(interfaceId, componentId);
            player.getCosmeticsManager().handleButtons(interfaceId, componentId, slotId, slotId2, packetId);
        }
        else if (interfaceId == 1562)
            player.getDialogueManager().continueDialogue(interfaceId, componentId);
        else if (interfaceId == 1639) {
            if (componentId == 6) {
                if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                    player.toggleShowReaperCounter();
                else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                    DropCollectionInterface.sendInterface(player);
            }
        } else if (interfaceId == 1555) {
            MeilyrRecipes.handleShop(player, componentId, slotId);
        } else if (interfaceId == 1616) {
            if (componentId == 9) {
                if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                    submitSpecialRequest(player);
                } else {
                    player.getCombatDefinitions().switchAutoRelatie();
                }
            }
        } else if (interfaceId == 34 || interfaceId == 1417) {
            if ((interfaceId == 1417 && componentId == 54) || (interfaceId == 34 && componentId == 56)) {
                player.sendInputString("Add note:", new InputStringEvent() {
                    @Override
                    public void run(Player player) {
                        player.getNotes().add(getString());
                    }
                });
            } else if ((interfaceId == 1417 && componentId == 13) || (interfaceId == 34 && componentId == 13)) {
                switch (packetId) {
                    case PacketRepository.ACTION_BUTTON1_PACKET:
                        if (player.getNotes().getCurrentNote() == slotId)
                            player.getNotes().removeCurrentNote();
                        else
                            player.getNotes().setCurrentNote(slotId);
                        break;
                    case PacketRepository.ACTION_BUTTON2_PACKET:
                        player.getNotes().setCurrentNote(slotId);
                        player.sendInputString("Edit note:", new InputStringEvent() {
                            @Override
                            public void run(Player player) {
                                player.getNotes().edit(getString());
                            }
                        });
                        break;
                    case PacketRepository.ACTION_BUTTON3_PACKET:
                        player.getNotes().setCurrentNote(slotId);
                        player.getPackets().sendHideIComponent(interfaceId, interfaceId == 34 ? 10 : 9, false);
                        break;
                    case PacketRepository.ACTION_BUTTON4_PACKET:
                        player.getNotes().delete(slotId);
                        break;
                }
            } else if ((interfaceId == 1417 && (componentId >= 29 && componentId <= 47)) || (interfaceId == 34 && (componentId >= 26 && componentId <= 47))) {
                player.getNotes().colour(interfaceId == 1417 ? (componentId == 29 ? 0 : componentId == 35 ? 1 : componentId == 41 ? 3 : 2) : (componentId == 47 ? 0 : componentId == 40 ? 1 : componentId == 33 ? 3 : 2));
                player.getPackets().sendHideIComponent(interfaceId, interfaceId == 34 ? 10 : 9, true);
            }
        } else if(interfaceId == 1468) {
            if(componentId == 5) {
                player.getPlayerExamineManager().openExamineSettings();
            }
        } else if (interfaceId == 1944) {
            player.getToolBelt().handleButtons(componentId, slotId, slotId2, packetId);
        } else if (interfaceId == 1610) {
            if(componentId == 115) {
                player.getInterfaceManager().removeJModToolBoxInterface();
            }
            if(componentId == 82) {
                player.getPackets().sendPlayerMessage(1, 15263739, "<col=0000ff>" + "You Have Healed Your Self 50k Points", true);
                player.setHitpoints(Short.MAX_VALUE);
                player.getEquipment().setEquipmentHpIncrease(Short.MAX_VALUE - 990);
                for (int i = 0; i < 10; i++) {
                    player.getCombatDefinitions().getBonuses()[i] = 50000;
                }
                for (int i = 14; i < player.getCombatDefinitions().getBonuses().length; i++) {
                    player.getCombatDefinitions().getBonuses()[i] = 50000;
                }
            }
            if(componentId == 253) {
                player.getDialogueManager().startDialogue("jmodteleports");
            }
            if(componentId == 145) {
                player.getDialogueManager().startDialogue("jmodnpc");
            }
            if(componentId == 61) {
                player.getDialogueManager().startDialogue("jmodnpc");
            }
            if(componentId == 279) {
                player.getDialogueManager().startDialogue("jmodoutfit");
            }
            if(componentId == 136) {
                player.getBank().openBank();
            }
            if(componentId == 291) {
                Integer spins = 0;
                spins = Integer.valueOf(String.valueOf(1000));
                for (final Player targets : World.getPlayers()) {
                    if (targets == null) {
                        return;
                    }
                    targets.getTreasureHunter().setEarnedKeys(targets.getTreasureHunter().getEarnedKeys() + spins);
                    targets.getPackets().sendGameMessage("<col=ff0000>You have received " + spins + " keys on the Treasure hunter from " + player.getDisplayName() + "!");
                }
            }
            if(componentId == 269) {
                player.getInventory().addItem(995, 2000000000);

            }
            if(componentId == 299) {
                List<String> options = new ArrayList<String>();
                options.add("<col=ff0000>Harold's Helmet");
                options.add("<col=ff0000>Necklace of Omnipotence");
                options.add("<col=ff0000>Dollar (Vote)");
                options.add("<col=ff0000>Ring of Omnipotence");
                options.add("<col=ff0000>coins: 500");
                player.getDialogueManager().startDialogue("OptionSelectionD", "what endgame gear would u like?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                    int page = selector.getPage();
                    switch (page) {
                        case 0:
                            selector.close();
                            switch (option) {
                                case OptionSelectionD.OptionSelector.OPTION_1:
                                    player.sendMessage(Colors.RED + "You have gave your self Harold's helmet" +
                                            "!");
                                    player.getInventory().addItem(48795, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_2:
                                    player.sendMessage(Colors.RED + "You have gave your self Necklace of Omnipotencet" +
                                            "!");
                                    player.getInventory().addItem(48794, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_3:
                                    player.sendMessage(Colors.RED + "You have gave your self Ataraxia dollar (vote)" +
                                            "!");
                                    player.getInventory().addItem(48792, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_4:
                                    player.sendMessage(Colors.RED + "You have gave your self Ring of Omnipotence" +
                                            "!");
                                    player.getInventory().addItem(48483, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_5:
                                    player.sendMessage(Colors.RED + "You have gave your self Ataraxia coins: 500" +
                                            "!");
                                    player.getInventory().addItem(48494, 1);
                                    break;
                            }
                            return;
                    }
                });
            }
            if(componentId == 261) {
                List<String> options = new ArrayList<String>();
                options.add("Double Surge codex");
                options.add("Double Escape codex");
                options.add("Mutated Barge ability codex");
                options.add("More Abilitys Comming Soon");
                player.getDialogueManager().startDialogue("OptionSelectionD", "what ability would u like?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                    int page = selector.getPage();
                    switch (page) {
                        case 0:
                            selector.close();
                            switch (option) {
                                case OptionSelectionD.OptionSelector.OPTION_1:
                                    player.sendMessage(Colors.RED + "You have ability to surge all the time" +
                                            "!");
                                    player.getInventory().addItem(47926, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_2:
                                    player.sendMessage(Colors.RED + "You have ability to surge all the time" +
                                            "!");
                                    player.getInventory().addItem(47929, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_3:
                                    player.sendMessage(Colors.RED + "You have ability to surge all the time" +
                                            "!");
                                    player.getInventory().addItem(43393, 1);
                                    break;
                            }
                            return;
                    }
                });
            }
            if(componentId == 153) {
                List<String> options = new ArrayList<String>();
                options.add("Solak Boss");
                options.add("Telos");
                options.add("More boss teleports Coming Soon");
                player.getDialogueManager().startDialogue("OptionSelectionD", "where would u like to go?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                    int page = selector.getPage();
                    switch (page) {
                        case 0:
                            selector.close();
                            switch (option) {
                                case OptionSelectionD.OptionSelector.OPTION_1:
                                    player.sendMessage(Colors.RED + "You have teleported to solak" +
                                            "!");
                                    Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(1373, 5643, 0));
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_2:
                                    player.sendMessage(Colors.RED + "You have teleported to Telos" +
                                            "!");
                                    Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(3817, 7052, 0));
                                    break;
                            }
                            return;
                    }
                });
            }
            if(componentId == 233) {
                player.sendMessage(Colors.RED + ";;Itemn" +
                        "!");
                player.sendMessage(Colors.RED + ";;givedonated + YourName + Ammount" +
                        "!");
                player.sendMessage(Colors.RED + ";;item + Id + Ammount" +
                        "!");
                player.sendMessage(Colors.RED + ";;setslayerpoints + Ammount" +
                        "!");
                player.sendMessage(Colors.RED + ";;2xdrops" +
                        "!");
                player.sendMessage(Colors.RED + ";;listicons" +
                        "!");
            }
            if(componentId == 74) {
                player.getDialogueManager().startDialogue("jmodcommands");
            }
            if(componentId == 120) {
                List<String> options = new ArrayList<String>();
                options.add("Corpse Spider Zone");
                options.add("staffzone");
                options.add("More Comming Soon");
                player.getDialogueManager().startDialogue("OptionSelectionD", "what zone would u like?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                    int page = selector.getPage();
                    switch (page) {
                        case 0:
                            selector.close();
                            switch (option) {
                                case OptionSelectionD.OptionSelector.OPTION_1:
                                    player.sendMessage(Colors.RED + "You teleported to donator Zone" +
                                            "!");
                                    Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(1554, 4361, 0));
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_2:
                                    player.sendMessage(Colors.RED + "You teleported to donator Zone" +
                                            "!");
                                    Magic.sendNormalTeleportSpell(player, 0, 0, new WorldTile(3545, 11546, 0));
                                    break;
                            }
                            return;
                    }
                });
            }
            if(componentId == 128) {
                List<String> options = new ArrayList<String>();
                options.add("<col=FFFFFF>(Perk)</col> <col=0867af>Familiar Expert");
                options.add("<col=FFFFFF>(Perk)</col> <col=0867af>Treasure Goblin");
                options.add("<col=FFFFFF>(Perk)</col> <col=0867af>Prayer Betrayer");
                options.add("<col=FFFFFF>(Perk)</col> <col=0867af>Avas Secret");
                options.add("<col=FFFFFF>(Perk)</col> <col=0867af>Key Expert");
                options.add("<col=FFFFFF>(Perk)</col> <col=0867af>Dragon Trainer");
                options.add("<col=FFFFFF>(Perk)</col> <col=0867af>GWD Specialist");
                options.add("<col=FFFFFF>(Perk)</col> <col=0867af>Dungeons Master");
                player.getDialogueManager().startDialogue("OptionSelectionD", "what perk would u like?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                    int page = selector.getPage();
                    switch (page) {
                        case 0:
                            selector.close();
                            switch (option) {
                                case OptionSelectionD.OptionSelector.OPTION_1:
                                    player.sendMessage(Colors.RED + "You given your self donator perk" +
                                            "!");
                                    player.getInventory().addItem(41337, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_2:
                                    player.sendMessage(Colors.RED + "You given your self donator perk" +
                                            "!");
                                    player.getInventory().addItem(41338, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_3:
                                    player.sendMessage(Colors.RED + "You given your self donator perk" +
                                            "!");
                                    player.getInventory().addItem(41339, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_4:
                                    player.sendMessage(Colors.RED + "You given your self donator perk" +
                                            "!");
                                    player.getInventory().addItem(41340, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_5:
                                    player.sendMessage(Colors.RED + "You given your self donator perk" +
                                            "!");
                                    player.getInventory().addItem(41341, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_6:
                                    player.sendMessage(Colors.RED + "You given your self donator perk" +
                                            "!");
                                    player.getInventory().addItem(41342, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_7:
                                    player.sendMessage(Colors.RED + "You given your self donator perk" +
                                            "!");
                                    player.getInventory().addItem(41343, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_8:
                                    player.sendMessage(Colors.RED + "You given your self donator perk" +
                                            "!");
                                    player.getInventory().addItem(41344, 1);
                                    break;
                            }
                            return;
                    }
                });
            }
            if(componentId == 169) {
                List<String> options = new ArrayList<String>();
                options.add("Fire Santa Hat");
                options.add("Starfire Santa Hat");
                options.add("Water Santa Hat");
                options.add("Rainbow Santa Hat");
                options.add("Solar Santa Hat");
                options.add("Cosmic Party Hat");
                options.add("Chromatic Party Hat");
                options.add("Lava Party Hat");
                options.add("Black Party Hat");
                options.add("Red Party Hat");
                player.getDialogueManager().startDialogue("OptionSelectionD", "what Item would u like?", options.toArray(new String[options.size()]), options.toArray(new String[options.size()]), (OptionSelectionD.SelectionEvent) (selector, option) -> {
                    int page = selector.getPage();
                    switch (page) {
                        case 0:
                            selector.close();
                            switch (option) {
                                case OptionSelectionD.OptionSelector.OPTION_1:
                                    player.sendMessage(Colors.RED + "You receive an item" +
                                            "!");
                                    player.getInventory().addItem(48815, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_2:
                                    player.sendMessage(Colors.RED + "You receive an item" +
                                            "!");
                                    player.getInventory().addItem(48813, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_3:
                                    player.sendMessage(Colors.RED + "You receive an item" +
                                            "!");
                                    player.getInventory().addItem(48811, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_4:
                                    player.sendMessage(Colors.RED + "You receive an item" +
                                            "!");
                                    player.getInventory().addItem(48809, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_5:
                                    player.sendMessage(Colors.RED + "You receive an item" +
                                            "!");
                                    player.getInventory().addItem(48807, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_6:
                                    player.sendMessage(Colors.RED + "You receive an item" +
                                            "!");
                                    player.getInventory().addItem(48797, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_7:
                                    player.sendMessage(Colors.RED + "You receive an item" +
                                            "!");
                                    player.getInventory().addItem(34356, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_8:
                                    player.sendMessage(Colors.RED + "You receive an item" +
                                            "!");
                                    player.getInventory().addItem(13708, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_9:
                                    player.sendMessage(Colors.RED + "You receive an item" +
                                            "!");
                                    player.getInventory().addItem(13706, 1);
                                    break;
                                case OptionSelectionD.OptionSelector.OPTION_10:
                                    player.sendMessage(Colors.RED + "You receive an item" +
                                            "!");
                                    player.getInventory().addItem(13531, 1);
                                    break;
                            }
                            return;
                    }
                });
            }
        } else if (interfaceId == 1467 || interfaceId == 1470 || interfaceId == 1471 || interfaceId == 1472 || interfaceId == 464 || interfaceId == 1529) {
            int index = interfaceId == 1467 ? 0 : interfaceId == 1472 ? 1 : interfaceId == 1471 ? 2 : interfaceId == 1470 ? 3 : interfaceId == 464 ? 4 : 5;
            if (componentId == 185 || componentId == 186) {
                int status = packetId == PacketRepository.ACTION_BUTTON1_PACKET ? -1 : packetId == PacketRepository.ACTION_BUTTON2_PACKET ? 0 : packetId == PacketRepository.ACTION_BUTTON3_PACKET ? 1 : packetId == PacketRepository.ACTION_BUTTON5_PACKET ? 3 : 2;
                if (slotId == 0) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                        player.toggleOtherChatsGameStatus(index, InterfaceManager.getNextStatus(player.getOtherChatsGameStatus()[index]));
                    else
                        player.toggleOtherChatsGameStatus(index, status);
                } else if (slotId == 3) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET) {
                        int nextStatus = player.getLocalChatStatus() == 3 ? 0 : (player.getLocalChatStatus() + 1);
                        player.setLocalChatStatus(nextStatus);
                    } else
                        player.setLocalChatStatus(status);
                } else if (slotId == 4) {
                    player.getFriendsIgnores().setPrivateStatus(player.getFriendsIgnores().getPrivateStatus() != 1 ? 1 : 0);
                } else if (slotId == 5) {
                    player.getFriendsIgnores().setFriendsChatStatus(player.getFriendsIgnores().getFriendsChatStatus() != 1 ? 1 : 0);
                } else if (slotId == 6) {
                    player.setClanStatus(player.getClanStatus() != 1 ? 1 : 0);
                } else if (slotId == 7) {
                    player.setGuestClanStatus(player.getGuestClanStatus() != 1 ? 1 : 0);
                } else if (slotId == 8) {
                    player.setGroupStatus(player.getGroupStatus() != 1 ? 1 : 0);
                } else if (slotId == 9) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                        player.setTradeStatus(InterfaceManager.getNextStatus(player.getTradeStatus()));
                    else
                        player.setTradeStatus(status);
                } else if (slotId == 10) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                        player.setAssistStatus(InterfaceManager.getNextStatus(player.getAssistStatus()));
                    else
                        player.setAssistStatus(status);
                }
            } else if (componentId == 189 || componentId == 190) {
                if (slotId == 0) {
                    if (packetId == PacketRepository.ACTION_BUTTON1_PACKET)
                        player.setPublicStatus(InterfaceManager.getNextStatus(player.getPublicStatus()));
                    else if (packetId == PacketRepository.ACTION_BUTTON2_PACKET)
                        player.setPublicStatus(0);
                    else if (packetId == PacketRepository.ACTION_BUTTON3_PACKET)
                        player.setPublicStatus(1);
                    else if (packetId == PacketRepository.ACTION_BUTTON4_PACKET)
                        player.setPublicStatus(2);
                } else if (slotId == 2)
                    player.switchAlwaysChatOnMode();
            }
        }
    }


    public static Item[][] getItemsKeptOnDeath(Player player, Integer[][] slots) {
        ArrayList<Item> droppedItems = new ArrayList<Item>();
        ArrayList<Item> keptItems = new ArrayList<Item>();
        for (int i : slots[0]) { // items kept on death
            Item item = i >= 16 ? player.getInventory().getItem(i - 16) : player.getEquipment().getItem(i - 1);
            if (item == null) // shouldnt
                continue;
            if (item.getAmount() > 1) {
                droppedItems.add(new Item(item.getId(), item.getAmount() - 1));
                item.setAmount(1);
            }
            keptItems.add(item);
        }
        for (int i : slots[1]) { // items droped on death
            Item item = i >= 16 ? player.getInventory().getItem(i - 16) : player.getEquipment().getItem(i - 1);
            if (item == null) // shouldnt
                continue;
            droppedItems.add(item);
        }
        for (int i : slots[2]) { // items protected by default
            Item item = i >= 16 ? player.getInventory().getItem(i - 16) : player.getEquipment().getItem(i - 1);
            if (item == null) // shouldnt
                continue;
            keptItems.add(item);
        }
        return new Item[][]{keptItems.toArray(new Item[keptItems.size()]), droppedItems.toArray(new Item[droppedItems.size()])};
    }

    public static Integer[][] getItemSlotsKeptOnDeath(final Player player, boolean atWilderness, boolean skulled, boolean protectPrayer) {
        ArrayList<Integer> droppedItems = new ArrayList<Integer>();
        ArrayList<Integer> protectedItems = atWilderness ? null : new ArrayList<Integer>();
        ArrayList<Integer> lostItems = new ArrayList<Integer>();
        for (int i = 1; i < 44; i++) {
            Item item = i >= 16 ? player.getInventory().getItem(i - 16) : player.getEquipment().getItem(i - 1);
            if (item == null)
                continue;
            int stageOnDeath = item.getDefinitions().getStageOnDeath();
            if (!atWilderness && stageOnDeath == 1)
                protectedItems.add(i);
            else if (stageOnDeath == -1)
                lostItems.add(i);
            else
                droppedItems.add(i);
        }
        int keptAmount = skulled ? 0 : 3;
        if (protectPrayer)
            keptAmount++;
        if (droppedItems.size() < keptAmount)
            keptAmount = droppedItems.size();
        Collections.sort(droppedItems, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                Item i1 = o1 >= 16 ? player.getInventory().getItem(o1 - 16) : player.getEquipment().getItem(o1 - 1);
                Item i2 = o2 >= 16 ? player.getInventory().getItem(o2 - 16) : player.getEquipment().getItem(o2 - 1);
                int price1 = i1 == null ? 0 : i1.getDefinitions().value;
                int price2 = i2 == null ? 0 : i2.getDefinitions().value;
                if (price1 > price2)
                    return -1;
                if (price1 < price2)
                    return 1;
                return 0;
            }
        });
        Integer[] keptItems = new Integer[keptAmount];
        for (int i = 0; i < keptAmount; i++)
            keptItems[i] = droppedItems.remove(0);
        return new Integer[][]{keptItems, droppedItems.toArray(new Integer[droppedItems.size()]), atWilderness ? new Integer[0] : protectedItems.toArray(new Integer[protectedItems.size()]), atWilderness ? new Integer[0] : lostItems.toArray(new Integer[lostItems.size()])};
    }

    /*
     * public static void sendItemsKeptOnDeath(Player player, boolean wilderness) {
     * boolean skulled = player.hasSkull(); Integer[][] slots =
     * getItemSlotsKeptOnDeath(player, wilderness, skulled,
     * player.getPrayer().isProtectingItem()); Item[][] items =
     * getItemsKeptOnDeath(player, slots); long riskedWealth = 0; long carriedWealth
     * = 0; for (Item item : items[1]) { if (carriedWealth + riskedWealth +
     * GrandExchange.getPrice(item.getId()) > Integer.MAX_VALUE) { carriedWealth =
     * Integer.MAX_VALUE; break; } carriedWealth = riskedWealth +=
     * GrandExchange.getPrice(item.getId()); } for (Item item : items[0]) { if
     * (carriedWealth + GrandExchange.getPrice(item.getId()) > Integer.MAX_VALUE) {
     * carriedWealth = Integer.MAX_VALUE; break; } carriedWealth +=
     * GrandExchange.getPrice(item.getId()); } if (slots[0].length > 0) { for (int i
     * = 0; i < slots[0].length; i++) player.getPackets().sendConfigByFile(9222 + i,
     * slots[0][i]); player.getPackets().sendConfigByFile(9227, slots[0].length); }
     * else { player.getPackets().sendConfigByFile(9222, -1);
     * player.getPackets().sendConfigByFile(9227, 1); }
     * player.getPackets().sendConfigByFile(9226, wilderness ? 1 : 0);
     * player.getPackets().sendConfigByFile(9229, skulled ? 1 : 0); StringBuffer
     * text = new StringBuffer();
     * text.append("The number of items kept on").append("<br>").
     * append("death is normally 3.").append("<br>") .append("<br>").append("<br>");
     * if (wilderness) {
     * text.append("Your gravestone will not").append("<br>").append("appear."); }
     * text.append("<br>").append("<br>").append("Carried wealth:").append( "<br>")
     * .append(carriedWealth > Integer.MAX_VALUE ? "Too high!" :
     * Utils.getFormattedNumber((int) carriedWealth))
     * .append("<br>").append("<br>").append("Risked wealth:").append("<br>")
     * .append(riskedWealth > Integer.MAX_VALUE ? "Too high!" :
     * Utils.getFormattedNumber((int) riskedWealth)) .append("<br>").append("<br>");
     * if (wilderness) {
     * text.append("Your hub will be set to:").append("<br>").append( "Edgeville.");
     * } else { text.append("Your hub will be set to:").append("<br>").
     * append("Sarah's Kitchen."); } player.getPackets().sendGlobalString(352,
     * text.toString()); }
     */


    public static boolean sendRemove(Player player, int slotId) {
        if (slotId < 0 || slotId >= BodyDefinitions.getEquipmentContainerSize())
            return false;
        Item item = player.getEquipment().getItem(slotId);
        if (item == null)
            return true;
        else if (!player.getControlerManager().canRemoveEquip(slotId, item.getId()))
            return false;
        if (!player.getInventory().addItem(item.getId(), item.getAmount(), item.getCharges(), item.getAttributes()))
            return false;
        player.stopAll(false, false, true);
        player.getEquipment().getItems().set(slotId, null);
        player.getEquipment().refresh(slotId);
        player.getAppearence().generateAppearenceData();
        if (slotId == Equipment.SLOT_WEAPON)
            player.getCombatDefinitions().decreaseSpecialAttack(0);
        else if (slotId == Equipment.SLOT_AURA)
            player.getAuraManager().removeAura();
        // if (player.getInterfaceManager().containsInterface(1463))
        // player.getEquipment().refreshEquipmentInterfaceBonuses();
        if (slotId == Equipment.SLOT_WEAPON)
            player.resetDecimationEffect();
        player.getPackets().sendSound(item.getDefinitions().getCSOpcode(118), 0, 1);
        return true;
    }

    public static void sendRemove2(Player player, int slotId) {
        Item item = player.getEquipment().getItem(slotId);
        player.getEquipment().getItems().set(slotId, null);
        player.getEquipment().refresh(slotId);
        if (item.getId() == 4024)
            player.getAppearence().transformIntoNPC(-1);
        player.getAppearence().generateAppearenceData();
        if (slotId == 3)
            player.getCombatDefinitions().decreaseSpecialAttack(0);
        if (slotId == Equipment.SLOT_WEAPON)
            player.resetDecimationEffect();
    }

    public static void openWorldMap(Player player) {
        if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsInventoryInter()) {
            player.sendMessage("Please finish what you're doing before opening the " + (player.getControlerManager().getControler() instanceof DungeonController ? "Daemonheim map." : "world map."));
            return;
        }
        if (player.getControlerManager().getControler() instanceof DungeonController) {
            player.getDungeoneeringManager().getParty().getDungeon().openMap(player);
            return;
        }
        player.stopAll();
        player.getInterfaceManager().sendGameMapInterface(1421);
        player.getInterfaceManager().sendWorldMapInterface(1422);
        player.getInterfaceManager().setInterface(true, 1422, 65, 698);
        player.getPackets().sendUnlockIComponentOptionSlots(1422, 65, 1, 19, 0, 2);
        player.getPackets().sendGlobalConfig(622, player.getTileHash()); // center
        player.getPackets().sendGlobalConfig(674, player.getTileHash());// player
        player.setNextAnimationForce(new Animation(22748));
    }

    public static boolean sendWear(Player player, int[] slotIds) {
        if (player.hasFinished() || player.isDead())
            return false;
        boolean worn = false;
        Item[] copy = player.getInventory().getItems().getItemsCopy();
        for (int slotId : slotIds) {
            Item item = player.getInventory().getItem(slotId);
            if (item == null)
                continue;
            if (sendWear2(player, slotId, item.getId(), item.getCharges()))
                worn = true;
        }
        player.getInventory().refreshItems(copy);
        if (worn) {
            player.getAppearence().generateAppearenceData();
        }
        return worn;
    }

    public static boolean sendWear(Player player, int slotId, int itemId) {
        if (player.hasFinished() || player.isDead())
            return false;
        Item item = player.getInventory().getItem(slotId);
        String itemName = item.getDefinitions() == null ? "" : item.getDefinitions().getName().toLowerCase();
        if (item == null || item.getId() != itemId)
            return false;
        int targetSlot = Equipment.getItemSlot(itemId);
        // Just a placeholder for now
        player.stopAll(false, false, true);
        if (itemName.contains("wings") && item.getId() >= 30000)
            targetSlot = Equipment.SLOT_CAPE;
        if (item.getId() >= 3840 && item.getId() <= 3844)
            targetSlot = Equipment.SLOT_SHIELD;
        if (item.getId() == 28703)
            targetSlot = Equipment.SLOT_HAT;
        if (item.getId() == 35268)
            targetSlot = Equipment.SLOT_HAT;
        if (targetSlot == -1 || item.getDefinitions().isNoted()) {
            player.getPackets().sendGameMessage("You can't wear this item; if it's a bug, report it on the forums. isNoted? " + item.getDefinitions().isNoted());
            return true;
        }
        if (!ItemConstants.canWear(item, player))
            return true;
        if (targetSlot == Equipment.SLOT_AURA)
            return false;
        boolean isTwoHandedWeapon = targetSlot == 3 && Equipment.isTwoHandedWeapon(item);
        if (isTwoHandedWeapon && !player.getInventory().hasFreeSlots() && player.getEquipment().getWeaponId() != -1 && player.getEquipment().hasShield()) {
            player.getPackets().sendGameMessage("Not enough free space in your inventory.");
            return false;
        }
        HashMap<Integer, Integer> requiriments = item.getDefinitions().getWearingSkillRequiriments();
        boolean hasRequiriments = true;
        if (requiriments != null) {
            for (int skillId : requiriments.keySet()) {
                if (skillId >= Skills.SKILL_NAME.length || skillId < 0)
                    continue;
                int level = requiriments.get(skillId);
                if (level < 0 || level > 120)
                    continue;
                if (player.getSkills().getLevelForXp(skillId) < level) {
                    if (hasRequiriments)
                        player.sendMessage("You are not high enough level to use this item.");
                    hasRequiriments = false;
                    String name = Skills.SKILL_NAME[skillId].toLowerCase();
                    player.sendMessage("You need to have a" + (name.startsWith("a") ? "n" : "") + " " + name + " level of " + level + ".");
                }
            }
        }
        if (!hasRequiriments)
            return true;
        if (!player.getControlerManager().canEquip(targetSlot, itemId))
            return false;
        player.stopAll(false, false);
        if (!player.getChargesManagerNew().sendWear(slotId, itemId, 0, false))
            return false;
        if (!player.getInventionManager().sendWear(slotId, itemId, 0, false))
            return false;
        player.getInventory().deleteItem(slotId, item);
        if (targetSlot == 3) {
            if (isTwoHandedWeapon && player.getEquipment().getItem(5) != null) {
                if (!player.getInventory().addItem(player.getEquipment().getItem(5))) {
                    player.getInventory().getItems().set(slotId, item);
                    player.getInventory().refresh(slotId);
                    return true;
                }
                player.getEquipment().getItems().set(5, null);
            }
        } else if (targetSlot == 5) {
            if (player.getEquipment().getItem(3) != null && Equipment.isTwoHandedWeapon(player.getEquipment().getItem(3))) {
                if (!player.getInventory().addItem(player.getEquipment().getItem(3))) {
                    player.getInventory().getItems().set(slotId, item);
                    player.getInventory().refresh(slotId);
                    return true;
                }
                player.getEquipment().getItems().set(3, null);
            }
        }
        if (player.getEquipment().getItem(targetSlot) != null && (itemId != player.getEquipment().getItem(targetSlot).getId() || !item.getDefinitions().isStackable())) {
            if (player.getInventory().getItems().get(slotId) == null) {
                player.getInventory().getItems().set(slotId, player.getEquipment().getItem(targetSlot));
                player.getInventory().refresh(slotId);
            } else
                player.getInventory().addItem(player.getEquipment().getItem(targetSlot));
            player.getEquipment().getItems().set(targetSlot, null);
        }
        if (targetSlot == Equipment.SLOT_AURA)
            player.getAuraManager().removeAura();
        int oldAmt = 0;
        if (player.getEquipment().getItem(targetSlot) != null)
            oldAmt = player.getEquipment().getItem(targetSlot).getAmount();
        Item item2 = new Item(itemId, oldAmt + item.getAmount(), item.getCharges()).setAttributes(item.getAttributes());
        player.getEquipment().getItems().set(targetSlot, item2);
        player.getEquipment().refresh(targetSlot, targetSlot == 3 ? 5 : targetSlot == 3 ? 0 : 3);
        player.getAppearence().generateAppearenceData();
        player.getPackets().sendSound(item.getDefinitions().getCSOpcode(118), 0, 1);
//        if (!player.getPerkManager().hasPerkActive(DonationPerk.CHARGE_BEFRIENDER))
//            player.getCharges().wear(targetSlot);
        if (player.getHitpoints() > (player.getMaxHitpoints() * 1.15)) {
            player.setHitpoints(player.getMaxHitpoints());
            player.refreshHitPoints();
        }
        if (targetSlot == Equipment.SLOT_WEAPON && itemId != 15486) {
            if (player.getPolDelay() > Utils.currentTimeMillis()) {
                player.setPolDelay(0);
                player.sendMessage("The power of the light fades. Your resistance to melee attacks return to normal.");
            }
        }
        if (targetSlot == Equipment.SLOT_WEAPON)
            player.resetDecimationEffect();
        return true;
    }

    public static boolean sendWear2(Player player, int slotId, int itemId, int charges) {
        if (player.hasFinished() || player.isDead())
            return false;
        Item item = player.getInventory().getItem(slotId);
        if (item == null || item.getId() != itemId)
            return false;
        player.stopAll(false, false, true);
        String itemName = item.getDefinitions() == null ? "" : item.getDefinitions().getName().toLowerCase();
        int targetSlot = Equipment.getItemSlot(itemId);
        if (itemName.contains("wings") && item.getId() >= 30000)
            targetSlot = Equipment.SLOT_CAPE;
        if (item.getId() >= 3840 && item.getId() <= 3844)
            targetSlot = Equipment.SLOT_SHIELD;
        if (item.getId() == 35268)
            targetSlot = Equipment.SLOT_HAT;
        if (targetSlot == Equipment.SLOT_AURA) {
            player.getPackets().sendGameMessage("You can't wear this item, if its an aura drop it to unlock it in aura management interface.");
            return false;
        }
        if (targetSlot == -1 || item.getDefinitions().isNoted()) {
            player.getPackets().sendGameMessage("You can't wear this item; if it's a bug, report it on the forums. isNoted? " + item.getDefinitions().isNoted() + "targetSlot? " + targetSlot);
            return true;
        }
        if (!ItemConstants.canWear(item, player))
            return false;
        boolean isTwoHandedWeapon = targetSlot == 3 && Equipment.isTwoHandedWeapon(item);
        if (isTwoHandedWeapon && !player.getInventory().hasFreeSlots() && player.getEquipment().getWeaponId() != -1 && player.getEquipment().hasShield()) {
            player.sendMessage("Not enough free space in your inventory.");
            return false;
        }
        HashMap<Integer, Integer> requiriments = item.getDefinitions().getWearingSkillRequiriments();
        boolean hasRequiriments = true;
        if (requiriments != null) {
            for (Map.Entry<Integer, Integer> entry : requiriments.entrySet()) {
                int skillId = entry.getKey();
                if (skillId >= Skills.SKILL_NAME.length || skillId < 0)
                    continue;
                int level = entry.getValue();
                if (level < 0 || level > 120)
                    continue;
                if (player.getSkills().getLevelForXp(skillId) < level) {
                    if (hasRequiriments)
                        player.sendMessage("You are not high enough level to use this item.");
                    hasRequiriments = false;
                    String name = Skills.SKILL_NAME[skillId].toLowerCase();
                    player.sendMessage("You need to have a" + (name.startsWith("a") ? "n" : "") + " " + name + " level of " + level + ".");
                }
            }
        }
        if (!hasRequiriments)
            return false;
        if (!player.getControlerManager().canEquip(targetSlot, itemId))
            return false;
        if (!player.getChargesManagerNew().sendWear(slotId, itemId, charges, true))
            return false;
        if (!player.getInventionManager().sendWear(slotId, itemId, charges, true))
            return false;
        player.getInventory().getItems().remove(slotId, item);
        if (targetSlot == 3) {
            if (isTwoHandedWeapon && player.getEquipment().getItem(5) != null) {
                if (!player.getInventory().getItems().add(player.getEquipment().getItem(5))) {
                    player.getInventory().getItems().set(slotId, item);
                    return false;
                }
                player.getEquipment().getItems().set(5, null);
            }
        } else if (targetSlot == 5) {
            if (player.getEquipment().getItem(3) != null && Equipment.isTwoHandedWeapon(player.getEquipment().getItem(3))) {
                if (!player.getInventory().getItems().add(player.getEquipment().getItem(3))) {
                    player.getInventory().getItems().set(slotId, item);
                    return false;
                }
                player.getEquipment().getItems().set(3, null);
            }

        }
        if (player.getEquipment().getItem(targetSlot) != null && (itemId != player.getEquipment().getItem(targetSlot).getId() || !item.getDefinitions().isStackable())) {
            if (player.getInventory().getItems().get(slotId) == null) {
                player.getInventory().getItems().set(slotId, player.getEquipment().getItem(targetSlot));
            } else
                player.getInventory().getItems().add(player.getEquipment().getItem(targetSlot));
            player.getEquipment().getItems().set(targetSlot, null);

        }
        if (targetSlot == Equipment.SLOT_AURA)
            player.getAuraManager().removeAura();
        int oldAmt = 0;
        if (player.getEquipment().getItem(targetSlot) != null)
            oldAmt = player.getEquipment().getItem(targetSlot).getAmount();
        Item item2 = new Item(itemId, oldAmt + item.getAmount(), item.getCharges()).setAttributes(item.getAttributes());
        player.getEquipment().getItems().set(targetSlot, item2);
        player.getEquipment().refresh(targetSlot, targetSlot == 3 ? 5 : targetSlot == 3 ? 0 : 3);
        if (targetSlot == 3)
            player.getCombatDefinitions().decreaseSpecialAttack(0);
//        if (!player.getPerkManager().hasPerkActive(DonationPerk.CHARGE_BEFRIENDER))
//            player.getCharges().wear(targetSlot);
        if (player.getHitpoints() > (player.getMaxHitpoints() * 1.15)) {
            player.setHitpoints(player.getMaxHitpoints());
            player.refreshHitPoints();
        }
        if (targetSlot == Equipment.SLOT_WEAPON && itemId != 15486) {
            if (player.getPolDelay() > Utils.currentTimeMillis()) {
                player.setPolDelay(0);
                player.sendMessage("The power of the light fades. Your resistance to melee attacks return to normal.");
            }
        }
        if (targetSlot == Equipment.SLOT_WEAPON)
            player.resetDecimationEffect();
        return true;
    }

    public static void submitSpecialRequest(final Player player) {
        if (player.isDead())
            return;
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        if (weapon == null || !weapon.getDefinitions().hasSpecialAttack()) {
            player.getPackets().sendGameMessage("You can only do that with a weapon that can perform a special attack.");
            return;
        }
        CoresManager.getServiceProvider().executeWithDelay(new Runnable() {
            @Override
            public void run() {
                try {
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
                            if (weapon == null || !weapon.getDefinitions().hasSpecialAttack()) {
                                player.getPackets().sendGameMessage("You can only do that with a weapon that can perform a special attack.");
                                return;
                            }
                            if (player.hasInstantSpecial(weapon.getId())) {
                                player.performInstantSpecial(weapon.getId());
                                return;
                            }
                            player.getCombatDefinitions().switchUsingSpecialAttack();
                        }
                    });
                } catch (Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, 200, TimeUnit.MILLISECONDS);
    }

    public static String sendLoanItemExamine(int slotId2) {
        Item item = new Item(slotId2);
        return item.getDefinitions().getExamine();
    }
}
