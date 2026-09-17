package com.rs.network.packet;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.duel.DuelArena;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.gim.GIMGroup;
import com.rs.game.activities.gambling.Gambling;
import com.rs.game.activities.seasonalevents.SeasonalEventManager;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.npc.NPC;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.familiar.Familiar.SpecialAttack;
import com.rs.game.npc.others.MirrorbackSpider;
import com.rs.game.player.ActionBar;
import com.rs.game.player.ActionBar.DefenceAbilityShortcut;
import com.rs.game.player.ActionBar.HealAbilityShortcut;
import com.rs.game.player.ActionBar.ItemShortcut;
import com.rs.game.player.ActionBar.MagicAbilityShortcut;
import com.rs.game.player.ActionBar.MeleeAbilityShortcut;
import com.rs.game.player.ActionBar.PrayerShortcut;
import com.rs.game.player.ActionBar.RangeAbilityShortcut;
import com.rs.game.player.ActionBar.StrengthAbilityShortcut;
import com.rs.game.player.ActionBar.SummoningOrbShortcut;
import com.rs.game.player.Inventory;
import com.rs.game.player.LendingManager;
import com.rs.game.player.LoginManager;
import com.rs.game.player.Player;
import com.rs.game.player.Prayer.QuickPrayerPrest;
import com.rs.game.player.TemporaryAttributes.Key;
import com.rs.game.player.actions.PlayerFollow;
import com.rs.game.player.actions.firemaking.Firemaking;
import com.rs.game.player.actions.invention.Disassemble;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.commands.Command;
import com.rs.game.player.commands.CommandHandler;
import com.rs.game.player.bots.BotPlayer;
import com.rs.game.player.bots.trading.BotTrading;
import com.rs.game.player.content.ChatMessage;
import com.rs.game.player.content.Commands;
import com.rs.game.player.content.FriendChatsManager;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.LogicPacket;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.PublicChatMessage;
import com.rs.game.player.content.QuickChatMessage;
import com.rs.game.player.content.RS3SkillsDialogue;
import com.rs.game.player.content.RouteEvent;
import com.rs.game.player.content.SkillCapeCustomizer;
import com.rs.game.player.content.SkillsDialogue;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.grandExchange.GrandExchange;
import com.rs.game.player.content.interfaces.keybinds.KeyBindManager;
import com.rs.game.player.content.items.Ectoplasmator;
import com.rs.game.player.content.trade.Trade;
import com.rs.game.player.controllers.EliteDungeonController;
import com.rs.game.player.controllers.Kalaboss;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.network.io.InputStream;
import com.rs.network.packet.impl.ButtonHandler;
import com.rs.network.packet.impl.InventoryOptionsHandler;
import com.rs.network.packet.impl.NPCHandler;
import com.rs.network.packet.impl.ObjectHandler;
import com.rs.utils.Colors;
import com.rs.utils.DialogueOptionEvent;
import com.rs.utils.IPMute;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.InputNameEvent;
import com.rs.utils.InputStringEvent;
import com.rs.utils.Lend;
import com.rs.utils.Logger;
import com.rs.utils.LoggingSystem;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;
import com.rs.utils.huffman.Huffman;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;

import lombok.Getter;

@SuppressWarnings("unused")
public final class PacketRepository {

    /**
     * The packet sizes
     */
    public static final byte[] PACKET_SIZES = new byte[124];
    /**
     * Walking packets.
     */
    public final static int WALKING_PACKET = 33;
    public final static int MINI_WALKING_PACKET = 78;
    /**
     * Component click packets.
     */
    public final static int ACTION_BUTTON1_PACKET = 111;
    public final static int ACTION_BUTTON2_PACKET = 68;
    public final static int ACTION_BUTTON3_PACKET = 86;
    public final static int ACTION_BUTTON4_PACKET = 29;
    public final static int ACTION_BUTTON5_PACKET = 70;
    public final static int ACTION_BUTTON6_PACKET = 63;
    public final static int ACTION_BUTTON7_PACKET = 50;
    public final static int ACTION_BUTTON8_PACKET = 67;
    public final static int ACTION_BUTTON9_PACKET = 83;
    public final static int ACTION_BUTTON10_PACKET = 66;
    public final static int WORLD_MAP_CLICK = 18;

    /**
     * Player click packets.
     */
    public final static int PLAYER_OPTION_1_PACKET = 117;
    public final static int PLAYER_OPTION_2_PACKET = 62;
    public final static int PLAYER_OPTION_4_PACKET = 102;
    public static final int PLAYER_OPTION_5_PACKET = 49;
    public final static int PLAYER_OPTION_6_PACKET = 4;
    public final static int PLAYER_OPTION_7_PACKET = 54;
    public final static int PLAYER_OPTION_9_PACKET = 6;

    /**
     * Object click packets.
     */
    public final static int OBJECT_CLICK1_PACKET = 12;
    public final static int OBJECT_CLICK2_PACKET = 96;
    public final static int OBJECT_CLICK3_PACKET = 27;
    public final static int OBJECT_CLICK4_PACKET = 61;
    public final static int OBJECT_CLICK5_PACKET = 119;
    public final static int OBJECT_EXAMINE_PACKET = 39;

    /**
     * NPC click packets.
     */
    public final static int NPC_CLICK1_PACKET = 51;
    public final static int ATTACK_NPC_PACKET = 56;
    public final static int NPC_CLICK2_PACKET = 116;
    public final static int NPC_CLICK3_PACKET = 31;
    public final static int NPC_CLICK4_PACKET = 2;
    public static final int NPC_EXAMINE_PACKET = 36;

    /**
     * Item click packets
     * */
    public final static int ITEM_TAKE_PACKET = 26;
    public final static int GROUND_ITEM_OPTION_2_PACKET = 121;
    public final static int GROUND_ITEM_OPTION_EXAMINE = 105;

    /**
     * Friends list packets.
     */
    public final static int ADD_FRIEND_PACKET = 85;
    public final static int REMOVE_FRIEND_PACKET = 35;
    public final static int ADD_IGNORE_PACKET = 13;
    public final static int REMOVE_IGNORE_PACKET = 47;
    public final static int ADD_FRIEND_NOTE_PACKET = 104;

    /**
     * FriendsChat packets.
     */
    public final static int JOIN_FRIEND_CHAT_PACKET = 92;
    public final static int CHANGE_FRIENDCHAT_RANK_PACKET = 73;
    public final static int KICK_FRIEND_CHAT_PACKET = 32;

    /**
     * Chat packets.
     */
    public final static int SEND_FRIEND_MESSAGE_PACKET = 7;
    public final static int SEND_FRIEND_QUICK_CHAT_PACKET = 115;
    public final static int PUBLIC_QUICK_CHAT_PACKET = 17;
    public final static int CHAT_TYPE_PACKET = 44;
    public final static int CHAT_PACKET = 95;


    /**
     * Interface packets.
     */
    private final static int MOUSE_MOVEMENT_DELAY = 97;
    public final static int CLICK_PACKET = 45;
    private final static int CLICK_PACKET_2 = 72;
    public final static int MOUVE_MOUSE_PACKET = 5;
    public final static int IN_OUT_SCREEN_PACKET = 93;
    public final static int DONE_LOADING_REGION_PACKET = 79;
    public final static int RECEIVE_PACKET_COUNT_PACKET = 22;
    private final static int GARBAGE_CLEAR_PACKET = 74;
    public final static int PING_PACKET = 103;
    public final static int DISPLAY_PACKET = 123;
    public final static int MOVE_CAMERA_PACKET = 37;// unused
    public final static int KEY_TYPED_PACKET = 87;
    private final static int NIS_VAR_PACKET = 71;
    private final static int UPDATE_GAMEBAR_PACKET = 88;
    public final static int WORLD_LIST_UPDATE = 77;
    public final static int CLOSE_INTERFACE_PACKET = 57;
    public final static int CANCEL_INPUT_PACKET = 106;
    public final static int DIALOGUE_CONTINUE_PACKET = 98;
    public final static int INTERFACE_ON_OBJECT = 21;
    public final static int COMMANDS_PACKET = 84;
    public final static int INTERFACE_ON_INTERFACE = 58;
    public final static int KICK_CLAN_CHAT_PACKET = -1;// Unused.
    public final static int ENTER_INTEGER_PACKET = 10;
    public final static int ENTER_NAME_PACKET = 101;
    public final static int ENTER_LONG_TEXT_PACKET = 3;
    public final static int SWITCH_INTERFACE_COMPONENTS_PACKET = 122;
    public final static int INTERFACE_ON_PLAYER = 41;
    public final static int INTERFACE_ON_NPC = 113;
    public final static int COLOR_ID_PACKET = 1;
    public final static int INTERFACE_ON_ITEM = 114;
    public static final int FORUM_THREAD_ID_PACKET = 110;
    public final static int OPEN_URL_PACKET = 120;
    public final static int REPORT_ABUSE_PACKET = 11;
    public final static int GRAND_EXCHANGE_ITEM_SELECT_PACKET = 99;

    public static final short[] IGNORED = { };

    static {
        loadPacketSizes();
    }
    @Getter
    private final Player player;
    private int chatType;

    private int incomingOpcode;

    public PacketRepository(Player player) {
        this.player = player;
        this.incomingOpcode = -1;
    }

    public static void decodeLogicPacket(final Player player, LogicPacket packet) {
        int packetId = packet.getId();
        InputStream stream = new InputStream(packet.getData());
        if (packetId == NPC_CLICK4_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            NPCHandler.handleOption4(player, stream);
            return;
        }
        if (packetId == WALKING_PACKET || packetId == MINI_WALKING_PACKET) {
            if (!player.getAccountPin().hasEnteredPin() || !player.isActive() || !player.clientHasLoadedMapRegion() || player.isDead() || player.isLocked()) {
                return;
            }
            if (player.isROTSLocked())
                return;
            if (player.getTemporaryAttributtes().get("temp-frozen") == Boolean.TRUE) {
                return;
            }
            int baseX = stream.readUnsignedShortLE();
            boolean forceRun = stream.readUnsignedByte128() == 1;
            int baseY = stream.readUnsignedShortLE128();
            if (!player.getControlerManager().canProcessMovement(baseX, baseY))
                return;
            if (player.getFreezeDelay() >= Utils.currentTimeMillis()) {
                player.closeInterfaces();
                player.sendMessage("A magical force prevents you from moving.", true);
                return;
            }
            if (SeasonalEventManager.isActive(ChristmasSeasonalEvent.class)) {
                ChristmasSeasonalEvent.checkNearbyPresents(player);
            }
            player.stopAll();
            if (forceRun)
                player.setRun(forceRun);
            if (player.isNoclip()) {
                player.addWalkSteps(baseX, baseY, -1, false);
                return;
            }
            int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, player.getX(), player.getY(),
                    player.getPlane(), player.getSize(), new FixedTileStrategy(baseX, baseY), true);
            int[] bufferX = RouteFinder.getLastPathBufferX();
            int[] bufferY = RouteFinder.getLastPathBufferY();
            int last = -1;
            for (int i = steps - 1; i >= 0; i--) {
                if (!player.addWalkSteps(bufferX[i], bufferY[i], 25, true))
                    break;
                last = i;
            }
            if (last != -1) {
                WorldTile tile = new WorldTile(bufferX[last], bufferY[last], player.getPlane());
                player.getPackets().sendMinimapFlag(tile.getXInScene(player), tile.getYInScene(player));
            } else {
                player.getPackets().sendResetMinimapFlag();
            }
            if (player.getGamblingSession() != null) {
                player.getGamblingSession().endAbruptly(player);
            }
        } else if (packetId == WORLD_MAP_CLICK) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            int coordinateHash = stream.readIntV2();
            int x = coordinateHash >> 14;
            int y = coordinateHash & 0x3fff;
            int plane = coordinateHash >> 28;
            Integer hash = (Integer) player.getTemporaryAttributtes().get("worldHash");
            if (hash == null || coordinateHash != hash) {
                player.getTemporaryAttributtes().put("worldHash", coordinateHash);
            } else {
                player.getHintIconsManager().removeAll();
                player.getTemporaryAttributtes().remove("worldHash");
                player.getHintIconsManager().addHintIcon(x, y, plane, 20, 0, 2, -1, true);
                player.getPackets().sendConfig(2807, coordinateHash);
            }

        } else if (packetId == OBJECT_CLICK1_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            ObjectHandler.handleOption(player, stream, 1);
        } else if (packetId == OBJECT_CLICK2_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            ObjectHandler.handleOption(player, stream, 2);
        } else if (packetId == OBJECT_CLICK3_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            ObjectHandler.handleOption(player, stream, 3);
        } else if (packetId == OBJECT_CLICK4_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            ObjectHandler.handleOption(player, stream, 4);
        } else if (packetId == OBJECT_CLICK5_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            ObjectHandler.handleOption(player, stream, 5);
        } else if (packetId == INTERFACE_ON_PLAYER) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive() || !player.clientHasLoadedMapRegion() || player.isDead()) {
                return;
            }
            if (player.getLockDelay() > Utils.currentTimeMillis()) {
                return;
            }
            int playerIndex = stream.readUnsignedShortLE();
            int interfaceHash = stream.readIntLE();
            int slotId2 = stream.readUnsignedShortLE();
            int interfaceSlot = stream.readUnsignedShort128();
            final boolean forceRun = stream.readUnsignedByteC() == 1;
            int interfaceId = interfaceHash >> 16;
            int componentId = interfaceHash & 0xFF;
            if (Utils.getInterfaceDefinitionsSize() <= interfaceId) {
                return;
            }
            if (!player.getInterfaceManager().containsInterface(interfaceId)) {
                return;
            }
            if (componentId == 65535) {
                componentId = -1;
            }
            if (componentId != -1 && Utils.getInterfaceDefinitionsComponentsSize(interfaceId) <= componentId) {
                return;
            }
            Player p2 = World.getPlayers().get(playerIndex);
            if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId())) {
                return;
            }
            if (Settings.DEBUG)
                System.out.println("interface on player - player index:" + playerIndex + ", inter " + interfaceId + ", " + componentId + ", " + interfaceSlot + ", " + slotId2 + " player: " + player.getDisplayName());
            player.stopAll(false);
            if (interfaceId == Inventory.INVENTORY_INTERFACE || interfaceId == Inventory.INVENTORY_INTERFACE_2) {
                Item item = player.getInventory().getItems().get(interfaceSlot);
                if (item == null) {
                    return;
                }
                if (!player.getControlerManager().processItemOnPlayer(p2, item.getId())) {
                    return;
                }
                if (LendingManager.isLendedItem(player, item)) {
                    Lend lend = LendingManager.getLend(player);
                    if (lend == null) {
                        return;
                    }
                    if (!lend.getLender().equals(p2.getUsername())) {
                        player.sendMessage("You can't give your lent item to a stranger...");
                        return;
                    }
                    player.getDialogueManager().startDialogue("LendReturn", lend);
                    return;
                }
                InventoryOptionsHandler.handleItemOnPlayer(player, p2, item.getId());
            }
            switch (interfaceId) {
                case 1430:
                case 1506:
                case 662:
                    if (interfaceId == 1430 && componentId >= 64 && componentId <= 233)
                        player.getActionbar().pushShortcutOnSomething((componentId - 64) / 13, p2);
                    if ((interfaceId == 1430 && (componentId == 35 || componentId == 30)) || (interfaceId == 1506 && (componentId == 18 || componentId == 13))
                            || (interfaceId == 662 && (componentId == 86 || componentId == 69))) {
                        if (player.getFamiliar() == null)
                            return;
                        player.resetWalkSteps();
                        if (player.getFamiliar().getSpecialAttack() != SpecialAttack.ENTITY)
                            return;
                        if (!player.isCanPvp() || !p2.isCanPvp()) {
                            player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
                            return;
                        }
                        if (!player.getFamiliar().canAttack(p2)) {
                            player.getPackets().sendGameMessage("You can only use your familiar in a multi-zone area.");
                            return;
                        } else {
                            player.getFamiliar().setSpecial(componentId == 35 || componentId == 18 || componentId == 86, false);
                            player.getFamiliar().setTarget(p2);
                        }
                    }
                    break;
                case 1670:
                case 1671:
                case 1672:
                case 1673:
                    int currentBar = player.getActionbar().getMultiActionBar()[interfaceId - 1670] - 1;
                    if (interfaceId == 1670 && componentId >= 18 && componentId <= 187)
                        player.getActionbar().pushShortcutOnSomething(currentBar, (componentId - 11) / 13, p2);
                    else if ((interfaceId == 1671 || interfaceId == 1672 || interfaceId == 1673) && componentId >= 13 && componentId <= 182)
                        player.getActionbar().pushShortcutOnSomething(currentBar, (componentId - 13) / 13, p2);
                    break;
                case 1461:
                case 1459:
                case 1884:
                case 1885:
                case 1886:
                case 1887:
                    if (componentId == 1)
                        player.getActionbar().useAbility(new MagicAbilityShortcut(interfaceSlot), p2);
                    break;
                case 1110:
                case 1440:
                case 234:
                    if ((interfaceId == 1110 && componentId == 163) || (interfaceId == 1440 && componentId == 132) || (interfaceId == 234 && componentId == 10)) {
                        ClansManager.invite(player, p2);
                    }
                    break;
            }
        } else if (packetId == INTERFACE_ON_NPC) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive() || !player.clientHasLoadedMapRegion() || player.isDead()) {
                return;
            }
            if (player.getLockDelay() > Utils.currentTimeMillis()) {
                return;
            }
            int npcIndex = stream.readUnsignedShort128();
            int interfaceSlot = stream.readUnsignedShort128();
            int interfaceHash = stream.readIntLE();
            boolean forceRun = stream.readUnsigned128Byte() == 1;
            int itemId = stream.readUnsignedShort();
            int interfaceId = interfaceHash >> 16;
            int componentId = interfaceHash - (interfaceId << 16);
            if (Utils.getInterfaceDefinitionsSize() <= interfaceId) {
                return;
            }
            if (!player.getInterfaceManager().containsInterface(interfaceId)) {
                return;
            }
            if (componentId == 65535) {
                componentId = -1;
            }
            if (componentId != -1 && Utils.getInterfaceDefinitionsComponentsSize(interfaceId) <= componentId) {
                return;
            }
            if (Settings.DEBUG)
                System.out.println("interface on npc - npc index:" + npcIndex + ", inter " + interfaceId + ", " + componentId + ", " + interfaceSlot + ", " + itemId + " player: " + player.getDisplayName());
            NPC npc = World.getNPCs().get(npcIndex);
            if (npc == null)
                return;
            player.stopAll(false);
            if (interfaceId != Inventory.INVENTORY_INTERFACE && interfaceId != Inventory.INVENTORY_INTERFACE_2) {
                if (!npc.getDefinitions().hasAttackOption() || npc instanceof MirrorbackSpider && !Wilderness.isAtWild(npc)) {
                    player.getPackets().sendGameMessage("You can't attack this npc.");
                    return;
                }
            }
            if (interfaceId == Inventory.INVENTORY_INTERFACE || interfaceId == Inventory.INVENTORY_INTERFACE_2) {
                Item item = player.getInventory().getItem(interfaceSlot);
                if (item == null || !player.getControlerManager().processItemOnNPC(npc, item)) {
                    return;
                }
                InventoryOptionsHandler.handleItemOnNPC(player, npc, item);
                return;
            }
            if (npc == null || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()))
                return;

            switch (interfaceId) {
                case 1430:
                case 1506:
                case 662:
                    if (interfaceId == 1430 && componentId >= 64 && componentId <= 233)
                        player.getActionbar().pushShortcutOnSomething((componentId - 64) / 13, npc);
                    if ((interfaceId == 1430 && (componentId == 35 || componentId == 30)) || (interfaceId == 1506 && (componentId == 18 || componentId == 13))
                            || (interfaceId == 662 && (componentId == 86 || componentId == 69))) {
                        if (player.getFamiliar() == null)
                            return;
                        if (player.getFamiliar().getSpecialAttack() != SpecialAttack.ENTITY)
                            return;
                        if (npc instanceof MirrorbackSpider) {
                            if (player.mirrorback == npc) {
                                player.sendMessage("You can't attack your own mirrorback spider.");
                                return;
                            } else if (!Wilderness.isAtWild(player)) {
                                player.sendMessage("You cannot attack a mirrorback spider outside of Wilderness.");
                                return;
                            }
                        }
                        if (npc instanceof Familiar) {
                            Familiar familiar = (Familiar) npc;
                            if (familiar == player.getFamiliar()) {
                                player.getPackets().sendGameMessage("You can't attack your own familiar.");
                                return;
                            }
                            if (!player.getFamiliar().canAttack(familiar.getOwner())) {
                                player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
                                return;
                            }
                        } else if (!npc.getDefinitions().hasAttackOption()) {
                            player.getPackets().sendGameMessage("You can't attack this npc.");
                            return;
                        }
                        if (!player.getFamiliar().canAttack(npc)) {
                            player.getPackets().sendGameMessage("You can only use your familiar in a multi-zone area.");
                            return;
                        } else {
                            player.getFamiliar().setSpecial(componentId == 35 || componentId == 18 || componentId == 86, false);
                            player.getFamiliar().setTarget(npc);
                        }
                    }
                    break;
                case 1670:
                case 1671:
                case 1672:
                case 1673:
                    int currentBar = player.getActionbar().getMultiActionBar()[interfaceId - 1670] - 1;
                    if (interfaceId == 1670 && componentId >= 18 && componentId <= 187)
                        player.getActionbar().pushShortcutOnSomething(currentBar, (componentId - 18) / 13, npc);
                    else if ((interfaceId == 1671 || interfaceId == 1672 || interfaceId == 1673) && componentId >= 13 && componentId <= 182)
                        player.getActionbar().pushShortcutOnSomething(currentBar, (componentId - 13) / 13, npc);
                    break;
                case 1461:
                case 1459:
                case 1884:
                case 1885:
                case 1886:
                case 1887:
                    if (componentId == 1)
                        player.getActionbar().useAbility(new MagicAbilityShortcut(interfaceSlot), npc);
                    break;
            }
        } else if (packetId == ATTACK_NPC_PACKET) {
            /* AttackNPC */
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive() || !player.clientHasLoadedMapRegion() || player.isDead() || player.getLockDelay() > Utils.currentTimeMillis()) {
                return;
            }
            final boolean forceRun = stream.read128Byte() == 1;
            final int npcIndex = stream.readUnsignedShort();
            if (forceRun) {
                player.setRun(forceRun);
            }
            NPC npc = World.getNPCs().get(npcIndex);
            // player.sendMessage("[Attack Click] D-time:
            // "+(player.getAttackedByDelay())+" A-time:
            // "+(player.getAttackingDelay()-Utils.currentTimeMillis())+" to:
            // "+npc.hashCode()+" "+player.isUnderCombat(), true);
            if (npc == null || npc.isDead() || npc.hasFinished() || !player.getMapRegionsIds().contains(npc.getRegionId()) || !npc.getDefinitions().hasAttackOption()) {
                return;
            }
            if (player.isROTSLocked())
                return;
            if (!player.getControlerManager().processPlayerOption1(npc)) {
                return;
            }
            if (!npc.canBeAttacked(player)) {
                return;
            }
            if (npc instanceof MirrorbackSpider) {
                if (player.mirrorback == npc) {
                    player.sendMessage("You can't attack your own mirrorback spider.");
                    return;
                } else if (!Wilderness.isAtWild(player)) {
                    player.sendMessage("You cannot attack a mirrorback spider outside of Wilderness.");
                    return;
                }
            }
            if (npc instanceof Familiar) {
                Familiar familiar = (Familiar) npc;
                if (familiar == player.getFamiliar()) {
                    player.getPackets().sendGameMessage("You can't attack your own familiar.");
                    return;
                }
                if (!familiar.canAttack(player)) {
                    player.getPackets().sendGameMessage("You can't attack this npc.");
                    return;
                }
            } else if (!npc.isForceMultiAttacked()) {
                if (!npc.isAtMultiArea() || !player.isAtMultiArea()) {
                    if (player.getAttackedBy() != null && !player.getAttackedBy().isDead() && player.getAttackedBy() != npc && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
                        player.getPackets().sendGameMessage("You are already in combat.", true);
                        return;
                    }
                    if (npc.getAttackedBy() != player && npc.getAttackedByDelay() > Utils.currentTimeMillis()) {
                        player.getPackets().sendGameMessage("This npc is already in combat.", true);
                        return;
                    }
                }
            } else if (npc.getId() >= 22450 && npc.getId() <= 22452) {
                npc.setForceWalk(null);
                npc.getCombat().setTarget(npc.getTarget());
            }
            player.stopAll(true);
            player.getActionManager().setAction(new PlayerCombat(npc));
        } else if (packetId == NPC_CLICK1_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            NPCHandler.handleOption1(player, stream);
        } else if (packetId == NPC_CLICK2_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            NPCHandler.handleOption2(player, stream);
        } else if (packetId == NPC_CLICK3_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            NPCHandler.handleOption3(player, stream);
        } else if (packetId == INTERFACE_ON_OBJECT) {
            int objectId = stream.readIntV2();
            int slot = stream.readUnsignedShortLE128();
            int itemId = stream.readUnsignedShort();
            int x = stream.readUnsignedShortLE();
            boolean forceRun = stream.readUnsignedByteC() == 1;
            int y = stream.readUnsignedShort();
            int interfaceHash = stream.readIntLE();
            final int interfaceId = interfaceHash >> 16;
            int componentId = interfaceHash - (interfaceId << 16);
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive() || !player.clientHasLoadedMapRegion() || player.isDead() || Utils.getInterfaceDefinitionsSize() <= interfaceId || !player.getInterfaceManager().containsInterface(interfaceId) || player.isDead() || player.isLocked() || player.getEmotesManager().isDoingEmote()) {
                return;
            }
            final WorldTile tile = new WorldTile(x, y, player.getPlane());
            if (!player.getMapRegionsIds().contains(tile.getRegionId())) {
                return;
            }
            WorldObject mapObject = World.getObjectWithId(tile, objectId);
            if (mapObject == null || mapObject.getId() != objectId) {
                final WorldObject playerObject = player.getPlayerObjectWithId(objectId, x, y, player.getPlane());
                if (playerObject != null) {
                    mapObject = playerObject;
                } else {
                    return;
                }
            }
            final WorldObject object = !player.isAtDynamicRegion() ? mapObject : new WorldObject(objectId, mapObject.getType(), mapObject.getRotation(), x, y, player.getPlane());
            player.stopAll(false); // false
            if (forceRun) {
                player.setRun(forceRun);
            }
            switch (interfaceId) {
                case Inventory.INVENTORY_INTERFACE: // inventory
                case Inventory.INVENTORY_INTERFACE_2:
                    final Item item = player.getInventory().getItem(slot);
                    if (item == null || item.getId() != itemId) {
                        return;
                    }
                    ObjectHandler.handleItemOnObject(player, object, interfaceId, item);
                    break;
                case 1430:
                    if (interfaceId == 1430 && componentId >= 64 && componentId <= 233)
                        player.getActionbar().pushShortcutOnSomething((componentId - 64) / 13, object);
                    break;
                case 1670:
                case 1671:
                case 1672:
                case 1673:
                    int currentBar = player.getActionbar().getMultiActionBar()[interfaceId - 1670] - 1;
                    if (interfaceId == 1670 && componentId >= 18 && componentId <= 187)
                        player.getActionbar().pushShortcutOnSomething(currentBar, (componentId - 11) / 13, object);
                    else if ((interfaceId == 1671 || interfaceId == 1672 || interfaceId == 1673) && componentId >= 13 && componentId <= 182)
                        player.getActionbar().pushShortcutOnSomething(currentBar, (componentId - 13) / 13, object);
                    break;
                case 1461:
                case 1459:
                case 1884:
                case 1885:
                case 1886:
                case 1887:
                    if (componentId == 1)
                        player.getActionbar().useAbility(new MagicAbilityShortcut(slot), object);
                    break;
            }
            /** PvP attack packet */
        } else if (packetId == PLAYER_OPTION_1_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive() || !player.clientHasLoadedMapRegion() || player.isDead()) {
                return;
            }
            boolean forceRun = stream.readUnsignedByte128() == 1;
            int playerIndex = stream.readUnsignedShort128();
            Player p2 = World.getPlayers().get(playerIndex);
            if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId())) {
                return;
            }
            if (player.getLockDelay() > Utils.currentTimeMillis() || !player.getControlerManager().canPlayerOption1(p2)) {
                return;
            }
            if (!player.getControlerManager().processPlayerOption1(p2)) {
                return;
            }
            if (player.getTemporaryAttributtes().get(Key.ELITE_DUNGEON_TYPE) != null) {
                boolean eliteDungeon = (boolean) player.getTemporaryAttributtes().remove(Key.ELITE_DUNGEON_TYPE);
                if (player.isInsideAnyDungParty()) {
                    if (p2.getEliteDungeonsManager().invitingPlayer == player)
                        p2.getEliteDungeonsManager().expireInvitation();
                    if (p2.getDungeoneeringManager().invitingPlayer == player)
                        p2.getDungeoneeringManager().expireInvitation();
                    player.getPackets().sendGameMessage("You are already in a party, You can only enter one party at a time.");
                    return;
                }
                player.getTemporaryAttributtes().remove(Key.ELITE_DUNGEON_TYPE);
                if (eliteDungeon)
                    player.getEliteDungeonsManager().invite(p2.getDisplayName());
                else
                    player.getDungeoneeringManager().invite(p2.getDisplayName());
                return;
            }
            if (!Kalaboss.isAtKalaboss(player) && (!player.isCanPvp() || !p2.isCanPvp())) {
                player.getPackets().sendGameMessage("You can only attack players in a player-vs-player area.");
                return;
            }
            if (!p2.isAtMultiArea() || !player.isAtMultiArea()) {
                if (player.getAttackedBy() != p2 && player.getAttackedByDelay() > Utils.currentTimeMillis()) {
                    player.getPackets().sendGameMessage("You are already in combat.", true);
                    return;
                }
                if (p2.getAttackedBy() != player && p2.getAttackedByDelay() > Utils.currentTimeMillis()) {
                    if (p2.getAttackedBy() instanceof NPC) {
                        p2.setAttackedBy(player); // changes enemy to player,
                        // player has priority over
                        // npc on single areas
                    } else {
                        player.getPackets().sendGameMessage("That player is already in combat.", true);
                        return;
                    }
                }
            }
            player.stopAll(true);
            player.getActionManager().setAction(new PlayerCombat(p2));
        } else if (packetId == PLAYER_OPTION_2_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive() || !player.clientHasLoadedMapRegion() || player.isDead()) {
                return;
            }
            boolean forceRun = stream.readUnsignedByte128() == 1;
            int playerIndex = stream.readUnsignedShort128();
            Player p2 = World.getPlayers().get(playerIndex);
            if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId())) {
                return;
            }
            if (player.isROTSLocked())
                return;
            if (player.getLockDelay() > Utils.currentTimeMillis()) {
                return;
            }
            if (player.getControlerManager().getControler() instanceof DuelArena) {
                return;
            }
            player.stopAll(false);
            player.getActionManager().setAction(new PlayerFollow(p2));
        } else if (packetId == PLAYER_OPTION_4_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            boolean forceRun = stream.readUnsignedByte128() == 1;
            int playerIndex = stream.readUnsignedShort128();
            final Player p2 = World.getPlayers().get(playerIndex);
            if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()) || player.getLockDelay() >= Utils.currentTimeMillis() || player == p2) {
                return;
            }

            if (!player.getControlerManager().processPlayerOption4(p2)) {
                return;
            }

            /*
             * Returns out of trade if you or the person you are trading are in any type of
             * combat
             */
            if (player.isUnderCombat() || p2.isUnderCombat()) {
                if (player.isUnderCombat()) {
                    player.sendMessage(Colors.SHAD + "You cannot trade while you are in combat!", false);
                    return;
                }
                if (p2.isUnderCombat()) {
                    player.sendMessage(Colors.SHAD + "You must wait until this player is out of combat!", false);
                }
                return;
            }
            player.stopAll(false);

            if (player.getX() == p2.getX() && player.getY() == p2.getY()) {
                if (!player.addWalkSteps(player.getX() - 1, player.getY(), 1)) {
                    if (!player.addWalkSteps(player.getX() + 1, player.getY(), 1)) {
                        if (!player.addWalkSteps(player.getX(), player.getY() + 1, 1)) {
                            player.addWalkSteps(player.getX(), player.getY() - 1, 1);
                        }
                    }
                }
            }
            if (!p2.withinDistance(player, 14)) {
                player.sendMessage("Unable to find " + p2.getDisplayName() + "; must be within radius.");
                return;
            }
            player.setRouteEvent(new RouteEvent(p2, () -> {
                if (p2.getTemporaryAttributtes().get("GambleTarget") != null) {
                    if (!World.isPvpArea(new WorldTile(player.getX(), player.getY(), player.getPlane())) && player.getControlerManager().getControler() == null) {
                        if (p2.getTemporaryAttributtes().get("GambleTarget") == player) {
                            p2.getTemporaryAttributtes().remove("GambleTarget");
                            int gamblingAmount = (int) p2.getTemporaryAttributtes().get("gamble_amount");
                            if (player.getInventory().getAmountOf(995) >= gamblingAmount || player.getMoneyPouch().getTotal() >= gamblingAmount) {
                                if (player.getInventory().getAmountOf(995) < gamblingAmount) {
                                    player.getPackets().sendRunScript(5561, 0, gamblingAmount);
                                    player.money -= gamblingAmount;
                                    player.refreshMoneyPouch();
                                } else {
                                    player.getInventory().deleteItem(995, gamblingAmount);
                                }
                                if (p2.getInventory().getAmountOf(995) < gamblingAmount) {
                                    p2.getPackets().sendRunScript(5561, 0, gamblingAmount);
                                    p2.money -= gamblingAmount;
                                    p2.refreshMoneyPouch();
                                } else {
                                    p2.getInventory().deleteItem(995, gamblingAmount);
                                }
                                Gambling gambling = new Gambling(p2, player, gamblingAmount);
                                p2.setGamblingSession(gambling);
                                player.setGamblingSession(gambling);
                                gambling.initialize();
                            } else {
                                player.getPackets().sendGameMessage("You don't have enough money!");
                                p2.getPackets().sendGameMessage("Opponent doesn't have enough money!");
                            }
                        }
                    }
                    return;
                }
                if (p2.getTemporaryAttributtes().get("TradeTarget") == player) {
                    p2.getTemporaryAttributtes().remove("TradeTarget");
                    if (p2 instanceof BotPlayer) {
                        player.faceEntity(p2);
                        BotTrading.startTrade(player, (BotPlayer) p2);
                        return;
                    }
                    if (player.canTrade(p2) && p2.canTrade(player)) {
                        player.setItemTransaction(new Trade(player));
                        p2.setItemTransaction(new Trade(p2));
                        player.getItemTransaction().openTransaction(p2);
                        p2.getItemTransaction().openTransaction(player);
                        return;
                    }
                }
                if (p2 instanceof BotPlayer) {
                    player.faceEntity(p2);
                    BotTrading.startTrade(player, (BotPlayer) p2);
                    return;
                }
                player.faceEntity(p2);
                // now check if can trade lmao.
                if (player.canTrade(p2) && p2.canTrade(player)) {
                    player.getTemporaryAttributtes().put("TradeTarget", p2);
                    player.sendMessage("Sending " + p2.getDisplayName() + " a trade request...");
                    p2.getPackets().sendTradeRequestMessage(player);
                }
            }));
        } else if (packetId == PLAYER_OPTION_5_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            boolean forceRun = stream.readUnsignedByte128() == 1;
            int playerIndex = stream.readUnsignedShort128();
            Player target = World.getPlayers().get(playerIndex);
            if (target == null || target.isDead() || target.hasFinished() || !player.getMapRegionsIds().contains(target.getRegionId()) || player.getLockDelay() >= Utils.currentTimeMillis() || player == target) {
                return;
            }
            if (!target.withinDistance(player, 14)) {
                player.sendMessage("Unable to find " + target.getDisplayName() + ".");
                return;
            }
            if (player.getAttackedByDelay() + 5000 > Utils.currentTimeMillis()) {
                player.sendMessage("You can't examine " + target.getDisplayName() + "'s stats until 5 seconds after the end of combat.");
                return;
            }
            player.getPlayerExamineManager().openExamineDetails(target);
        } else if (packetId == PLAYER_OPTION_6_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            boolean forceRun = stream.readUnsignedByte128() == 1;
            int playerIndex = stream.readUnsignedShort128();
            final Player p2 = World.getPlayers().get(playerIndex);
            if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()) || player.isLocked() || player == p2) {
                return;
            } else if (player.getGroup() != null) {
                if (player.getGroup().getLeader() == player) {
                    // 2nd player is busy
                    if (p2.getInterfaceManager().containsScreenInter()) {
                        player.sendMessage("This player is currently busy!");
                        return;
                    }

                    if (p2.getGroup() != null) {
                        player.sendMessage(Colors.SALMON + "This player is already in a group!");
                        return;
                    }

                    if (player.getGroup().getTeam().size() > 3) {
                        player.sendMessage(Colors.SALMON + "Your team is too" + " full to invite another player!");
                        return;
                    }

                    if (!player.withinDistance(p2, 14)) {
                        player.sendMessage("Could not find player: " + p2.getUsername());
                        return;
                    }

                    /* do walk to player and open interface */
                    player.faceEntity(p2);
                    if (!player.withinDistance(p2, 2)) {
                        player.setRouteEvent(new RouteEvent(p2, () -> {
                            p2.group = player.getGroup().recruit(p2);
                        }));
                    } else {
                        p2.group = player.getGroup().recruit(p2);
                    }
                    return;
                }
            } else {
                player.getPackets().sendPlayerOption("Null", 6, false);
                return;
            }
            return;
        } else if (packetId == PLAYER_OPTION_7_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            boolean forceRun = stream.readUnsignedByte128() == 1;
            int playerIndex = stream.readUnsignedShort128();
            final Player p2 = World.getPlayers().get(playerIndex);
            if (p2 == null || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId()) || player.isLocked() || player == p2) {
                return;
            }
            if (player.isStaff()) {
                player.getDialogueManager().startDialogue("OnlineStaffMenu", p2);
            }
            /*
             * if (!World.isPvpArea(new WorldTile(player.getX(), player.getY(),
             * player.getPlane())) && player.getControlerManager().getControler() == null) {
             * player.getTemporaryAttributtes().put("GambleTarget", p2);
             * player.sendInputInteger("How much GP would you like to bet?", new
             * InputIntegerEvent() {
             *
             * @Override public void run(Player player) { final int amount = getInteger();
             * if (amount > 1_000_000_000) {
             * player.getPackets().sendGameMessage("You can't bet over 1B GP!"); return; }
             * if (p2.isIronMan() || p2.isHCIronMan() || p2.isNoviceIronMan() ||
             * p2.isExpertIronMan() || p2.isIntermediateIronMan()) {
             * player.sendMessage("You can't gamble with an ironman!"); return; } try { if
             * (player.getInventory().getNumerOf(995) >= amount ||
             * player.getMoneyPouch().getTotal() >= amount) {
             * player.getPackets().sendGameMessage("Sending " + p2.getDisplayName() +
             * " a request..."); p2.getPackets().sendGambleRequestMessage(player, amount);
             * player.getTemporaryAttributtes().put("gamble_amount", amount); } else {
             * player.getPackets().sendGameMessage("You don't have that much money!"); } }
             * catch (Exception e) {
             * player.getDialogueManager().startDialogue("SimpleMessage",
             * "Invalid format."); } } }); }
             */
        } else if (packetId == PLAYER_OPTION_9_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            boolean forceRun = stream.readUnsignedByte128() == 1;
            int playerIndex = stream.readUnsignedShort128();
            if (Settings.DEBUG) {
                Logger.getGlobal().info(playerIndex);
            }
            Player p2 = World.getPlayers().get(playerIndex);
            if (p2 == null || p2 == player || p2.isDead() || p2.hasFinished() || !player.getMapRegionsIds().contains(p2.getRegionId())) {
                return;
            }
            if (player.isLocked()) {
                return;
            }
            if (forceRun) {
                player.setRun(forceRun);
            }
            player.stopAll();
            if (p2 instanceof BotPlayer) {
                if (!player.withinDistance(p2, 14)) {
                    player.sendMessage("Could not find player: " + p2.getUsername());
                    return;
                }
                player.faceEntity(p2);
                BotTrading.requestItem(player, (BotPlayer) p2);
                return;
            }
            ClansManager.viewInvite(player, p2);
        } else if (packetId == ITEM_TAKE_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            Boolean hasTeleported = (Boolean) player.getTemporaryAttributtes().getOrDefault("teleporting", false);
            if (!player.isActive() || hasTeleported || !player.clientHasLoadedMapRegion() || player.isDead()) {
                return;
            }
            long currentTime = Utils.currentTimeMillis();
            if (player.getLockDelay() > currentTime) {
                return;
            }
            if (!player.hasCompleted()) {
                return;
            }
            final int id = stream.readUnsignedShort();
            int y = stream.readUnsignedShort128();
            int x = stream.readUnsignedShort128();
            int flag = stream.readByteC();
            boolean forceRun = (flag & 0x1) != 0;
            boolean rightClick = (flag & 0x2) != 0;

            final WorldTile tile = new WorldTile(x, y, player.getPlane());
            final int regionId = tile.getRegionId();
            if (!player.getMapRegionsIds().contains(regionId)) {
                return;
            }
            final FloorItem item = World.getRegion(regionId).getGroundItem(id, tile, player);
            if (item == null) {
                player.sendMessage("Item may be bugged, please report to a staff member.");
                player.sendMessage("[id=" + id + ", x=" + x + ", y=" + y + ", z=" + player.getPlane() + "]");
                return;
            }
            if (item.getId() == Ectoplasmator.ATTUNED_ECTOPLASMATOR_ID || item.getId() == Ectoplasmator.ECTOPLASMATOR_ID || item.getId() == Ectoplasmator.DEGRADED_ATTUNED_ECTOPLASMATOR_ID) {
                World.removeGroundItem(item);
                return;
            }
            player.stopAll(false);
            if (forceRun) {
                player.setRun(forceRun);
            }

            player.setRouteEvent(new RouteEvent(item, new Runnable() {
                @Override
                public void run() {
                    try {
                        final FloorItem item = World.getRegion(regionId).getGroundItem(id, tile, player);
                        if (item == null) {
                            return;
                        }
                        if (!player.getControlerManager().canTakeItem(item)) {
                            return;
                        }
                        Boolean hasTeleported = (Boolean) player.getTemporaryAttributtes().getOrDefault("teleporting", false);
                        if (hasTeleported) {
                            return;
                        }
                        if (item.getOwner() == null) {
                            if (item.getId() >= 7928 && item.getId() <= 7933) {
                                QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/easter.png\" height=17> " + player.getDisplayName() + " has just found an Easter Egg."));
                            }
                        }
                        if (item.getOwner() != null) {
                            if (player.isGroupIronman()) {
                                boolean ownDrop = player.getUsername().equalsIgnoreCase(item.getOwner());
                                GIMGroup group = GIM.getGroupForMember(item.getOwner());
                                if (!ownDrop && (group == null || !group.getGroupName().equals(player.gimName))) {
                                    player.sendMessage("You can only pick up items dropped by your group members.");
                                    return;
                                }
                            } else {
                                if (player.getRights() == 2) {
                                    //player.sendMessage("This item was dropped by: " + item.getOwner() + ".");
                                }
                                if (!player.getUsername().equalsIgnoreCase(item.getOwner()) && !player.canTrade(null) && !item.getOwner().equalsIgnoreCase("noel")) {
                                    return;
                                }
                            }
                            if (item.getAttributes() != null && !player.getUsername().equalsIgnoreCase(item.getOwner())) {
                                player.getPackets().sendGameMessage("You can't pick up this item.");
                                return;
                            }
                        }
                        player.setNextFaceWorldTile(tile);
                        player.addWalkSteps(tile.getX(), tile.getY(), 1);
                        World.removeGroundItem(player, item);
                    } catch (Exception e) {
                        Logger.getGlobal().error("Swallowed drop exception!", e);
                    }
                }
            }));
        }
    }

    private static void loadPacketSizes() {
        PACKET_SIZES[0] = 15;
        PACKET_SIZES[1] = 2;
        PACKET_SIZES[2] = 3;
        PACKET_SIZES[3] = -1;
        PACKET_SIZES[4] = 3;
        PACKET_SIZES[5] = -1;
        PACKET_SIZES[6] = 3;
        PACKET_SIZES[7] = -2;
        PACKET_SIZES[8] = -1;
        PACKET_SIZES[9] = -1;
        PACKET_SIZES[10] = 4;
        PACKET_SIZES[11] = -1;
        PACKET_SIZES[12] = 9;
        PACKET_SIZES[13] = -1;
        PACKET_SIZES[14] = 4;
        PACKET_SIZES[15] = 9;
        PACKET_SIZES[16] = 7;
        PACKET_SIZES[17] = -1;
        PACKET_SIZES[18] = 4;
        PACKET_SIZES[19] = 1;
        PACKET_SIZES[20] = -2;
        PACKET_SIZES[21] = 17;
        PACKET_SIZES[22] = 4;
        PACKET_SIZES[23] = -1;
        PACKET_SIZES[24] = -1;
        PACKET_SIZES[25] = 7;
        PACKET_SIZES[26] = 7;
        PACKET_SIZES[27] = 9;
        PACKET_SIZES[28] = -2;
        PACKET_SIZES[29] = 8;
        PACKET_SIZES[30] = 4;
        PACKET_SIZES[31] = 3;
        PACKET_SIZES[32] = -1;
        PACKET_SIZES[33] = 5;
        PACKET_SIZES[34] = -2;
        PACKET_SIZES[35] = -1;
        PACKET_SIZES[36] = 3;
        PACKET_SIZES[37] = 4;
        PACKET_SIZES[38] = -2;
        PACKET_SIZES[39] = 9;
        PACKET_SIZES[40] = 1;
        PACKET_SIZES[41] = 11;
        PACKET_SIZES[42] = -1;
        PACKET_SIZES[43] = 1;
        PACKET_SIZES[44] = 1;
        PACKET_SIZES[45] = 6;
        PACKET_SIZES[46] = -2;
        PACKET_SIZES[47] = -1;
        PACKET_SIZES[48] = 3;
        PACKET_SIZES[49] = 3;
        PACKET_SIZES[50] = 8;
        PACKET_SIZES[51] = 3;
        PACKET_SIZES[52] = -1;
        PACKET_SIZES[53] = 1;
        PACKET_SIZES[54] = 3;
        PACKET_SIZES[55] = 2;
        PACKET_SIZES[56] = 3;
        PACKET_SIZES[57] = 0;
        PACKET_SIZES[58] = 16;
        PACKET_SIZES[59] = 12;
        PACKET_SIZES[60] = -1;
        PACKET_SIZES[61] = 9;
        PACKET_SIZES[62] = 3;
        PACKET_SIZES[63] = 8;
        PACKET_SIZES[64] = -2;
        PACKET_SIZES[65] = -1;
        PACKET_SIZES[66] = 8;
        PACKET_SIZES[67] = 8;
        PACKET_SIZES[68] = 8;
        PACKET_SIZES[69] = 0;
        PACKET_SIZES[70] = 8;
        PACKET_SIZES[71] = -2;
        PACKET_SIZES[72] = 7;
        PACKET_SIZES[73] = -1;
        PACKET_SIZES[74] = 4;
        PACKET_SIZES[75] = -1;
        PACKET_SIZES[76] = 3;
        PACKET_SIZES[77] = 4;
        PACKET_SIZES[78] = 18;
        PACKET_SIZES[79] = 4;
        PACKET_SIZES[80] = 0;
        PACKET_SIZES[81] = 7;
        PACKET_SIZES[82] = 0;
        PACKET_SIZES[83] = 8;
        PACKET_SIZES[84] = -1;
        PACKET_SIZES[85] = -1;
        PACKET_SIZES[86] = 8;
        PACKET_SIZES[87] = -2;
        PACKET_SIZES[88] = 3;
        PACKET_SIZES[89] = 5;
        PACKET_SIZES[90] = 4;
        PACKET_SIZES[91] = 3;
        PACKET_SIZES[92] = -1;
        PACKET_SIZES[93] = 1;
        PACKET_SIZES[94] = -1;
        PACKET_SIZES[95] = -1;
        PACKET_SIZES[96] = 9;
        PACKET_SIZES[97] = -1;
        PACKET_SIZES[98] = 6;
        PACKET_SIZES[99] = 2;
        PACKET_SIZES[100] = 9;
        PACKET_SIZES[101] = -1;
        PACKET_SIZES[102] = 3;
        PACKET_SIZES[103] = 0;
        PACKET_SIZES[104] = -1;
        PACKET_SIZES[105] = 7;
        PACKET_SIZES[106] = 0;
        PACKET_SIZES[107] = 4;
        PACKET_SIZES[108] = -2;
        PACKET_SIZES[109] = -2;
        PACKET_SIZES[110] = -1;
        PACKET_SIZES[111] = 8;
        PACKET_SIZES[112] = 18;
        PACKET_SIZES[113] = 11;
        PACKET_SIZES[114] = 15;
        PACKET_SIZES[115] = -1;
        PACKET_SIZES[116] = 3;
        PACKET_SIZES[117] = 3;
        PACKET_SIZES[118] = 1;
        PACKET_SIZES[119] = 9;
        PACKET_SIZES[120] = -2;
        PACKET_SIZES[121] = 7;
        PACKET_SIZES[122] = 16;
        PACKET_SIZES[123] = 6;
    }

    public static boolean isIgnored(int opcode) {
        for (short ignored : IGNORED) {
            if (ignored == opcode) {
                return true;
            }
        }
        return false;
    }

    public int decode(InputStream buffer) {
        while (buffer.getRemaining() > 0 && !player.hasFinished()) {
            int start = buffer.getOffset();
            int opcode = incomingOpcode = buffer.readPacket(incomingOpcode, player);

            if (opcode < 0 || opcode >= PACKET_SIZES.length) {
                if (Settings.DEBUG)
                    Logger.getGlobal().info("Invalid opcode: " + opcode + ", " + incomingOpcode + ".");
                incomingOpcode = -1;
                return -1; // drop
            }

            int length = PACKET_SIZES[opcode];
            if ((length == -1 && buffer.getRemaining() < 1) || (length == -2 && buffer.getRemaining() < 2))
                return -1;

            if (length == -1)
                length = buffer.readUnsignedByte();
            else if (length == -2)
                length = buffer.readUnsignedShort();

            if (buffer.getRemaining() < length) {
                return -1;
            }

            byte[] data = new byte[length];
            buffer.readBytes(data);
            try {
                processPackets(opcode, length, new InputStream(data));
            } catch (Throwable e) {
                Logger.getGlobal().catching(e);
            }
            incomingOpcode = -1;
        }
        return buffer.getOffset();
    }

    public void processPackets(final int opcode, final int length, InputStream stream) throws ClassNotFoundException {
        player.setLastPacketReceivedTime(Utils.currentTimeMillis());
        if (opcode == PING_PACKET) {
            player.getPackets().sendNoTimeOut();
        } else if (opcode == NIS_VAR_PACKET) {
            boolean unknown = stream.readUnsignedByte() == 1;
            int count = (stream.getLength() - 1) / 6;
            for (int i = 0; i < count; i++) {
                Integer id = stream.readUnsignedShort();
                Integer value = stream.readInt();
                // System.out.println("nisvar: "+id+", "+value);
                if (value == 0)
                    player.getILayoutVars().remove(id);
                else {
                    player.getILayoutVars().put(id, value);
                    // System.out.println("INTERFACE_LAYOUT_VARS.put(" + id +
                    // "," + value + ");");
                }
            }
            // System.out.println(player.getILayoutVars().size()+",
            // "+player.getILayoutVars());
            if (player.getILayoutVars().size() > 1000)
                player.resetILayoutVars();
            player.getPackets().sendStoreServerPermVarcs();
//            player.getPackets().sendServerTickEndPacket();
        } else if (opcode == WORLD_LIST_UPDATE) {
            if (!player.isLobby())
                return;
            int checksum = stream.readInt();
            player.getPackets().sendWorldList(checksum, new int[] { World.getPlayersOnline() });
        } else if (opcode == IN_OUT_SCREEN_PACKET) {
            boolean inScreen = stream.readByte() == 1;
            player.setViewingGame(inScreen);
        } else if (opcode == MOUVE_MOUSE_PACKET || opcode == MOUSE_MOVEMENT_DELAY) {

        }  else if (opcode == CLICK_PACKET) {
            int positionHash = stream.readInt();
            int y = positionHash >> 16; // y;
            int x = positionHash - (y << 16); // x
            int mouseHash = stream.readShort();
            int mouseButton = mouseHash >> 15;
            int time = mouseHash - (mouseButton << 15); // time
        } else if (opcode == CLICK_PACKET_2) {
            stream.readByte();
            stream.readShortLE128();
            stream.readIntV2();
        } else if (opcode == KEY_TYPED_PACKET) {// i guess
            int keyPressed = stream.readByte();
            if (keyPressed == 84 && player.getInterfaceManager().containsInputTextInterface() && player.getTemporaryAttributtes().remove(Key.REMOVE_INPUT_INTER) != null)
                player.getInterfaceManager().removeInputTextInterface();
            if (player.getKeyAction() != null) {
                if (!player.getInterfaceManager().containsInterface(player.getKeyAction().interfaceId)) {
                    player.setKeyAction(null);
                } else if (player.getKeyAction().listenForKey(keyPressed)) {
                    return;
                }
            }
            if (!player.getControlerManager().processKeyPress(keyPressed))
                return;

            if (player.getInterfaceManager().containsChatBoxInter() && player.getDialogueManager().hasDialogue()
                    && player.getPreviousKeyPressed() == 82
                    && player.getInterfaceManager().containsChatBoxInterface(905)) {
                // skills dialogue
                Logger.getGlobal().info("DIALOGUE: " + keyPressed);
                if (player.getPreviousKeyPressed() == 82) {
                    if (keyPressed >= 16 && keyPressed <= 25)// 0-9
                        player.getDialogueManager().continueDialogue(905, keyPressed - 2);
                    else if (keyPressed == 83)// space
                        player.getDialogueManager().continueDialogue(905, 14);// option 0
                }
            } else {
                switch (player.getPreviousKeyPressed()) {
                    case 82:// CRTL key
                        if (!player.getAccountPin().hasEnteredPin()) {
                            return;
                        }
                        if (player.getInterfaceManager().containsBankInterface())
                            return;
                        KeyBindManager.executeAction(player, keyPressed);

                }
            }
            player.setPreviousKeyPressed(keyPressed);
        } else if (opcode == DISPLAY_PACKET) {
            int displayMode = stream.readUnsignedByte();
            int width = stream.readUnsignedShort();
            int height = stream.readUnsignedShort();
            stream.readUnsignedByte();// Something unknown,
            // previous stated as switchScreenMode boolean
            // but it always printed the same value so that's horseshit.
            // Defined it properly below.
//            final boolean switchScreen = displayMode != player.getDisplayMode();
//            if (switchScreen) {
//                player.closeInterfaces();
//                player.setDisplayMode(displayMode);
//                player.getInterfaceManager().removeAll();
//                player.getInterfaceManager().sendInterfaces();
//            }
        } else if (opcode == MOVE_CAMERA_PACKET) {
            // not using it atm
            int y = stream.readShort();
            int x = stream.readShort();
        } else if (opcode == INTERFACE_ON_INTERFACE) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            InventoryOptionsHandler.handleItemOnItem(player, stream);
        } else if (opcode == RECEIVE_PACKET_COUNT_PACKET) {
            stream.readInt();
//            player.getPackets().sendServerTickEndPacket();
        } else if (opcode == DIALOGUE_CONTINUE_PACKET) {
            int junk = stream.readUnsignedShort128();
            int interfaceHash = stream.readInt();

            int interfaceId = interfaceHash >> 16;
            int buttonId = interfaceHash & 0xFFF;
            if (Utils.getInterfaceDefinitionsSize() <= interfaceId)
                return;
            if (!player.isRunning()) {
                return;
            }
            if (Settings.DEBUG) {
                Logger.getGlobal().info("Dialogue: " + interfaceId + ", " + buttonId + ", " + junk);
            }
            int componentId = interfaceHash - (interfaceId << 16);
            if (player.getTemporaryAttributtes().get("pluginOption") != null && player.getTemporaryAttributtes().get("pluginOption") instanceof DialogueOptionEvent) {
                Object event = player.getTemporaryAttributtes().remove("pluginOption");
                ((DialogueOptionEvent) event).setOption(componentId);
                if (player.getInterfaceManager().containsChatBoxInter())
                    player.getInterfaceManager().closeChatBoxInterface();
                ((DialogueOptionEvent) event).run(player);
                return;
            }
            if (interfaceId == 326) {
                switch (componentId) {
                    case 5:
                        if (player.getTemporaryAttributtes().get("teleotherteleport") != null) {
                            WorldTile teletile = (WorldTile) player.getTemporaryAttributtes().remove("teleotherteleport");
                            if (teletile == null) {
                                return;
                            }
                            Magic.sendNormalTeleportSpell(player, 0, 0, teletile);
                            return;
                        }
                        WorldTile teletile = (WorldTile) player.getTemporaryAttributtes().remove("groupteleport");
                        if (teletile == null) {
                            return;
                        }
                        Magic.sendLunarTeleportSpell(player, 0, 0, teletile);
                        break;
                    default:
                        player.getTemporaryAttributtes().remove("groupteleport");
                        player.getInterfaceManager().closeScreenInterface();
                        break;
                }
                return;
            } else if (interfaceId == 905 && componentId == 6) {
                player.sendInputInteger("Type in an amount to make: ", new InputIntegerEvent() {
                    @Override
                    public void run(Player player) {
                        SkillsDialogue.setQuantity(player, getInteger());
                    }
                });
                player.getDialogueManager().restartDialogue();
                return;
            } else if (interfaceId == 1530) {
                player.getInventionManager().handleInterface(interfaceId, buttonId, 0, 0);
            } else {
                if (interfaceId == 1263)
                    player.getDialogueManager().continueDialogue(interfaceId, componentId, junk);
                else
                    player.getDialogueManager().continueDialogue(interfaceId, componentId);
                if (interfaceId == 1048) {
                    player.getInterfaceManager().closeScreenInterface();
                    return;
                }
            }
        } else if (opcode == CLOSE_INTERFACE_PACKET) {
            if (player.isActive() && !player.hasFinished() && !player.isRunning()) {
                LoginManager.sendLogin(player);
                return;
            }
            if (player.getGamblingSession() != null) {
                player.getGamblingSession().endAbruptly(player);
            }
            if (player.getInterfaceManager().containsCentralOverlayInterface())
                player.getInterfaceManager().removeCentralOverlayInterface();
            if (player.getInterfaceManager().containsLoadingScreenInterface(1420)) {
                player.getAppearence().generateAppearenceData();
                player.getPackets().sendAppearenceLook();
                player.getInterfaceManager().closeLoadingScreen();
            }
            player.getInterfaceManager().removeInputTextInterface();
            player.stopAll();
        } else if (opcode == CANCEL_INPUT_PACKET) {
            if (player.isActive() && !player.hasFinished() && !player.isRunning())
                return;
            if (player.getInterfaceManager().containsInputTextInterface())
                player.getInterfaceManager().removeInputTextInterface();
        } else if (opcode == ENTER_INTEGER_PACKET) {
            /**
             * Note to future developers: Do not slap your nasty ass code within this
             * beautiful empty block. If you wish to use this method, simply call
             * player.sendInputIntegerEvent(String, InputIntegerEvent()); method. Slapping
             * different keys and methods here causes issues in the longer run as they will
             * start interfering with each-other when left unanswered.
             */
            if (!player.isRunning() || player.isDead())
                return;
            player.getInterfaceManager().removeInputTextInterface();
            int value = stream.readInt();
            if (player.getTemporaryAttributtes().get("pluginInteger") != null && player.getTemporaryAttributtes().get("pluginInteger") instanceof InputIntegerEvent) {
                InputIntegerEvent event = (InputIntegerEvent) player.getTemporaryAttributtes().remove("pluginInteger");
                event.setInteger(value);
                event.run(player);
            }
        } else if (opcode == ENTER_NAME_PACKET) {
            /**
             * Note to future developers: Do not slap your nasty ass code within this
             * beautiful empty block. If you wish to use this method, simply call
             * player.sendInputNameEvent(String, InputNameEvent()); method. Slapping
             * different keys and methods here causes issues in the longer run as they will
             * start interfering with each-other when left unanswered.
             */
            if (!player.isRunning() || player.isDead())
                return;
            player.getInterfaceManager().removeInputTextInterface();
            final String value = stream.readString();
            if (player.getTemporaryAttributtes().get("pluginString") != null && player.getTemporaryAttributtes().get("pluginString") instanceof InputNameEvent) {
                InputNameEvent event = (InputNameEvent) player.getTemporaryAttributtes().remove("pluginString");
                event.setString(value);
                event.run(player);
            }
        } else if (opcode == ENTER_LONG_TEXT_PACKET) {
            /**
             * Note to future developers: Do not slap your nasty ass code within this
             * beautiful empty block. If you wish to use this method, simply call
             * player.sendInputStringEvent(String, InputStringEvent()); method. Slapping
             * different keys and methods here causes issues in the longer run as they will
             * start interfering with each-other when left unanswered.
             */
            if (!player.isRunning() || player.isDead())
                return;
            player.getInterfaceManager().removeInputTextInterface();
            final String value = stream.readString();
            if (player.getTemporaryAttributtes().get("pluginLongString") != null && player.getTemporaryAttributtes().get("pluginLongString") instanceof InputStringEvent) {
                InputStringEvent event = (InputStringEvent) player.getTemporaryAttributtes().remove("pluginLongString");
                event.setString(value);
                event.run(player);
            } else if (player.getTemporaryAttributtes().remove("SetClanMotto") != null)
                ClansManager.setClanMottoInterface(player, value);
        } else if (opcode == COLOR_ID_PACKET) {
            if (!player.isActive()) {
                return;
            }
            int colorId = stream.readUnsignedShort();
            if (player.getTemporaryAttributtes().get("SkillcapeCustomize") != null) {
                SkillCapeCustomizer.handleSkillCapeCustomizerColor(player, colorId);
            } else if (player.getTemporaryAttributtes().get("MottifCustomize") != null) {
                ClansManager.setMottifColor(player, colorId);
            } else if (player.getTemporaryAttributtes().remove("COSTUME_COLOR_CUSTOMIZE") != null)
                SkillCapeCustomizer.handleCostumeColor(player, colorId);
        } else if (opcode == SWITCH_INTERFACE_COMPONENTS_PACKET) {
            player.increaseAFKTimer();
            int fromSlot = stream.readUnsignedShortLE();
            int toSlotId2 = stream.readUnsignedShortLE();
            int fromSlotId2 = stream.readUnsignedShortLE();
            int fromInterfaceHash = stream.readIntV1();
            int toInterfaceHash = stream.readIntV1();
            int toSlot = stream.readUnsignedShort();

            int toInterfaceId = toInterfaceHash >> 16;
            int toComponentId = toInterfaceHash - (toInterfaceId << 16);
            int fromInterfaceId = fromInterfaceHash >> 16;
            int fromComponentId = fromInterfaceHash - (fromInterfaceId << 16);
            if (Settings.DEBUG) {
                Logger.getGlobal().info("fromSlot: " + fromSlot + ", ToSlot: " + toSlot + ",  toInterfaceHash: " + toInterfaceHash + ", toInterfaceID: " + toInterfaceId + ", toComponent: " + toComponentId + ", frominterface: " + fromInterfaceId + ", from componentId: " + fromComponentId);
            }
            if (Utils.getInterfaceDefinitionsSize() <= fromInterfaceId || Utils.getInterfaceDefinitionsSize() <= toInterfaceId) {
                return;
            }
            if (!player.getInterfaceManager().containsInterface(fromInterfaceId) || !player.getInterfaceManager().containsInterface(toInterfaceId)) {
                return;
            }
            if (fromComponentId != -1 && Utils.getInterfaceDefinitionsComponentsSize(fromInterfaceId) <= fromComponentId) {
                return;
            }
            if (toComponentId != -1 && Utils.getInterfaceDefinitionsComponentsSize(toInterfaceId) <= toComponentId) {
                return;
            }
            if (player.getDungeoneeringBinds().handleInterfaceSwitching(fromInterfaceId, fromComponentId, toComponentId))
                return;
            if (fromInterfaceId == 1708 || toInterfaceId == 1708
                    || (fromInterfaceId == 1712 && toInterfaceId == 1712)) {// TODO
                player.getInventionManager().handleSwitchComponents(fromInterfaceId, fromComponentId, toInterfaceId,
                        toComponentId, fromSlot, toSlot);
                return;
            }
            if (fromInterfaceId == 1371 && toInterfaceId == 1371) {
                if (fromComponentId != toComponentId)
                    return;
                if (fromComponentId == 29) {
                    RS3SkillsDialogue.setCurrentQuantity(player, toSlot);
                    return;
                }
            }
            if ((fromInterfaceId == Inventory.INVENTORY_INTERFACE && fromComponentId == 7 && toInterfaceId == Inventory.INVENTORY_INTERFACE && toComponentId == 7)
                    || (fromInterfaceId == Inventory.INVENTORY_INTERFACE_2 && fromComponentId == 8 && toInterfaceId == Inventory.INVENTORY_INTERFACE_2 && toComponentId == 8)) {
                if (toSlot < 0 || toSlot >= player.getInventory().getItemsContainerSize() || fromSlot >= player.getInventory().getItemsContainerSize()) {
                    return;
                }
                player.getInventory().switchItem(fromSlot, toSlot);
            } else if ((fromInterfaceId == Inventory.INVENTORY_INTERFACE || fromInterfaceId == Inventory.INVENTORY_INTERFACE_2) && ((toInterfaceId == 1462 && toComponentId == 31) || (toInterfaceId == 1464 && toComponentId == 15))) {
                if (fromSlot >= player.getInventory().getItemsContainerSize() || player.getInterfaceManager().containsInventoryInter())
                    return;
                Item item = player.getInventory().getItem(fromSlot);
                if (item == null)
                    return;
                ItemDefinitions defs = ItemDefinitions.getItemDefinitions(item.getId());
                if (!defs.isWearItem())
                    return;
                InventoryOptionsHandler.handleItemOption2(player, fromSlot, item.getId(), item);
            } else if ((fromInterfaceId == Inventory.INVENTORY_INTERFACE && fromComponentId == 7 && toInterfaceId == Inventory.INVENTORY_INTERFACE && toComponentId == 1 && toSlot == 0)
                    || (fromInterfaceId == Inventory.INVENTORY_INTERFACE_2 && fromComponentId == 8 && toInterfaceId == Inventory.INVENTORY_INTERFACE_2 && toComponentId == 45 && toSlot == 0)) {
                if (fromSlot < 0 || fromSlot >= player.getInventory().getItemsContainerSize()) {
                    return;
                }
                Item item = player.getInventory().getItem(fromSlot);
                if (item == null)
                    return;
                player.getActionManager().setAction(new Disassemble(item));
            } else if ((fromInterfaceId == Inventory.INVENTORY_INTERFACE && fromComponentId == 7 && toInterfaceId == 1477 && toComponentId == 19)
                    || (fromInterfaceId == Inventory.INVENTORY_INTERFACE_2 && fromComponentId == 8 && toInterfaceId == 1477&& toComponentId == 19)) {
                if (fromSlot < 0 || fromSlot >= player.getInventory().getItemsContainerSize() || player.isDisableDragToDropItems()) {
                    return;
                }
                Item item = player.getInventory().getItem(fromSlot);
                if (item == null)
                    return;
                InventoryOptionsHandler.handleItemOption7(player, fromSlot, item.getId(), item);
            } else if (((fromInterfaceId == 1462 && fromComponentId == 31) || (fromInterfaceId == 1464 && fromComponentId == 15)) && (toInterfaceId == Inventory.INVENTORY_INTERFACE || toInterfaceId == Inventory.INVENTORY_INTERFACE_2)) {
                if (fromSlot >= player.getEquipment().getItems().getSize() || player.getInterfaceManager().containsInventoryInter())
                    return;

                Item item = player.getEquipment().getItem(fromSlot);
                if (item == null)
                    return;
                ItemDefinitions defs = ItemDefinitions.getItemDefinitions(item.getId());
                if (!defs.isWearItem())
                    return;
                player.getEquipment().handleEquipment(1462, 14, defs.getEquipSlot(), item.getId(), PacketRepository.ACTION_BUTTON1_PACKET);
            }else if ((toInterfaceId == 1430 && (toComponentId >= 64 && toComponentId <= 233)) || (toInterfaceId == 1436 && (toComponentId >= 17 && toComponentId <= 186)) || (toInterfaceId == 1670 && (toComponentId >= 18 && toComponentId <= 187)) || ((toInterfaceId == 1671 || toInterfaceId == 1672 || toInterfaceId == 1673) && (toComponentId >= 13 && toComponentId <= 182))) {
                int toBar = toInterfaceId >= 1670 ? player.getActionbar().getMultiActionBar()[toInterfaceId - 1670] - 1 : player.getActionbar().getCurrentBar();
                int fromIndex = (fromComponentId - (fromInterfaceId == 1670 ? 18 : (fromInterfaceId == 1671 || fromInterfaceId == 1672 || fromInterfaceId == 1673) ? 13  : fromInterfaceId == 1430 ? 64 : 17)) / 13;
                int toIndex = (toComponentId - (toInterfaceId == 1670 ? 18 : (toInterfaceId == 1671 || toInterfaceId == 1672 || toInterfaceId == 1673) ? 13 : toInterfaceId == 1430 ? 64 : 17)) / 13;
                if ((fromInterfaceId == 1430 && (fromComponentId >= 64 && fromComponentId <= 233)) || (fromInterfaceId == 1436 && (fromComponentId >= 17 && fromComponentId <= 186)) || (fromInterfaceId == 1670 && (fromComponentId >= 18 && fromComponentId <= 187)) || ((fromInterfaceId == 1671 || fromInterfaceId == 1672 || fromInterfaceId == 1673) && (fromComponentId >= 13 && fromComponentId <= 182))) {
                    int fromBar = fromInterfaceId >= 1670 ? player.getActionbar().getMultiActionBar()[fromInterfaceId - 1670] - 1 : player.getActionbar().getCurrentBar();
                    player.getActionbar().switchShortcut(fromBar, toBar, fromIndex, toIndex);
                } else if (fromInterfaceId == Inventory.INVENTORY_INTERFACE || fromInterfaceId == Inventory.INVENTORY_INTERFACE_2) {
                    if (fromSlot >= player.getInventory().getItemsContainerSize())
                        return;
                    Item item = player.getInventory().getItem(fromSlot);
                    if (item == null || item.getId() != fromSlotId2)
                        return;
                    player.getActionbar().setShortcut(toBar, toIndex, new ItemShortcut(item));
                } else if ((fromInterfaceId == 1464 && fromComponentId == 15) || (fromInterfaceId == 1464 && fromComponentId == 14)) {
                    if (fromSlot >= player.getEquipment().getItems().getSize())
                        return;
                    Item item = player.getEquipment().getItem(fromSlot);
                    if (item == null || item.getId() != fromSlotId2)
                        return;
                    player.getActionbar().setShortcut(toBar, toIndex, new ItemShortcut(item));
                } else if ((fromInterfaceId == 1461 || fromInterfaceId == 1459 || (fromInterfaceId >= 1884 && fromInterfaceId <= 1887)) && fromComponentId == 1) {
                    player.getActionbar().setShortcut(toBar, toIndex, new MagicAbilityShortcut(fromSlot));
                } else if ((fromInterfaceId == 1883 || fromInterfaceId == 1880) && fromComponentId == 1) {
                    boolean usingDefenceAbilities = !player.getCombatDefinitions().isOnConstitutionMenu();
                    if (ActionBar.getAbilityData(usingDefenceAbilities ? ActionBar.DEFENCE_ABILITY_SHORTCUT : ActionBar.HEAL_ABILITY_SHORTCUT, fromSlot) == null)
                        return;
                    player.getActionbar().setShortcut(toBar, toIndex, usingDefenceAbilities ? new DefenceAbilityShortcut(fromSlot) : new HealAbilityShortcut(fromSlot));
                } else if ((fromInterfaceId == 1452 || fromInterfaceId == 1456) && fromComponentId == 1) {
                    if (ActionBar.getAbilityData(ActionBar.RANGED_ABILITY_SHORTCUT, fromSlot) == null)
                        // spell
                        return;
                    player.getActionbar().setShortcut(toBar, toIndex, new RangeAbilityShortcut(fromSlot));
                }else if ((fromInterfaceId == 1460 && fromComponentId == 1) || (fromInterfaceId == 1450 && fromComponentId == 3)) {
                    boolean usingStrAbilities = player.getCombatDefinitions().isOnStrengthMenu();
                    if (ActionBar.getAbilityData(usingStrAbilities ? ActionBar.STRENGTH_ABILITY_SHORTCUT : ActionBar.MELEE_ABILITY_SHORTCUT, fromSlot) == null) // fake
                        // spell
                        return;
                    player.getActionbar().setShortcut(toBar, toIndex, usingStrAbilities ? new StrengthAbilityShortcut(fromSlot) : new MeleeAbilityShortcut(fromSlot));
                } else if ((fromInterfaceId == 1458 && fromComponentId == 39) || (fromInterfaceId == 1457 && fromComponentId == 15)) {
                    player.getActionbar().setShortcut(toBar, toIndex, new PrayerShortcut(fromSlot, player.getPrayer().isAncientCurses()));
                } else if (fromInterfaceId == 1890 && fromComponentId == 10) {
                    if (player.getPrayer().getPrayerPresets().isEmpty() || player.getPrayer().getPrayerPresets().get(fromSlot) == null)
                        return;
                    player.getActionbar().setShortcut(toBar, toIndex, new PrayerShortcut(fromSlot, player.getPrayer().isAncientCurses(), true));
                } else if (fromInterfaceId == 1890 && fromComponentId == 38) {
                    QuickPrayerPrest preset = player.getPrayer().getCurrentPreset();
                    if (preset == null)
                        return;
                    player.getActionbar().setShortcut(toBar, toIndex, new PrayerShortcut(fromSlot, preset.isAncientcurses()));
                } else if (fromInterfaceId == 1430 && fromComponentId == 17) {
                    if (player.getPrayer().getPrayerPresets().isEmpty() || player.getPrayer().getPrayerPresets().get(0) == null)
                        return;
                    player.getActionbar().setShortcut(toBar, toIndex, new PrayerShortcut(0, player.getPrayer().isAncientCurses(), true));
                } else if (fromInterfaceId == 1430 && fromComponentId == 22) {
                    player.getActionbar().setShortcut(toBar, toIndex, new SummoningOrbShortcut(1));
                }
            } else if ((fromInterfaceId == 1430 && (fromComponentId >= 64 && fromComponentId <= 233)) || (fromInterfaceId == 1436 && (fromComponentId >= 17 && fromComponentId <= 186)) || (fromInterfaceId == 1670 && (fromComponentId >= 18 && fromComponentId <= 187)) || ((fromInterfaceId == 1671 || fromInterfaceId == 1672 || fromInterfaceId == 1673) && (fromComponentId >= 13 && fromComponentId <= 182))) {
                int currentBar = fromInterfaceId >= 1670 ? player.getActionbar().getMultiActionBar()[fromInterfaceId - 1670] - 1 : player.getActionbar().getCurrentBar();
                int fromIndex = (fromComponentId - (fromInterfaceId == 1670 ? 18 : (fromInterfaceId == 1671 || fromInterfaceId == 1672 || fromInterfaceId == 1673) ? 13 : fromInterfaceId == 1430 ? 64 : 17)) / 13;
                if ((toInterfaceId == InterfaceManager.RESIZABLE_WINDOW_ID && toComponentId == 19))
                    player.getActionbar().clearShortcut(currentBar, fromIndex);
            } else if (fromInterfaceId == 517 && toInterfaceId == 517) {
                if (fromComponentId == 14 && toComponentId == 14) {// switch inventory
                    int interactionTab = player.gimBank.isOpen() ? player.gimBank.getCurrentBank().getInteractionTab() : player.getBank().getInteractionTab();
                    if (interactionTab == 2)
                        return;
                    int interactionTabSize = Math.max(player.getInventory().getItems().getItems().length - 1, player.getFamiliar() != null ? (player.getFamiliar().getBOBSize() - 1) : 0);
                    if (toSlot < 0 || toSlot >= interactionTabSize || fromSlot >= interactionTabSize || fromSlot < 0)
                        return;
                    if (interactionTab == 0) //inventory
                        player.getInventory().switchItem(fromSlot, toSlot);
                    else if(player.getFamiliar() != null)
                        player.getFamiliar().getBob().switchItem(fromSlot, toSlot);
                } else if (fromComponentId == 184 && toComponentId == 184) {
                    if (player.gimBank.isOpen())
                        player.gimBank.getCurrentBank().switchItem(fromSlot, toSlot);
                    else
                        player.getBank().switchItem(fromSlot, toSlot);
                } else if (fromComponentId == 184 && (toComponentId == 151 || toComponentId == 147)) {
                    if (player.gimBank.isOpen())
                        player.gimBank.getCurrentBank().switchItem(fromSlot, toSlot, fromComponentId, toComponentId);
                    else
                        player.getBank().switchItem(fromSlot, toSlot, fromComponentId, toComponentId);
                } else if (fromComponentId == 184 && (toComponentId == 187 || toComponentId == 199)) {
                    if (player.gimBank.isOpen())
                        player.gimBank.getCurrentBank().insertItem(fromSlot, toSlot, fromComponentId, toComponentId);
                    else
                        player.getBank().insertItem(fromSlot, toSlot, fromComponentId, toComponentId);
                } else if (fromComponentId == 184 && toComponentId == 14) {
                    if (player.gimBank.isOpen())
                        player.gimBank.getCurrentBank().switchToInventory(fromSlot, toSlot);
                    else
                        player.getBank().switchToInventory(fromSlot, toSlot);
                } else if (fromComponentId == 14 && (toComponentId == 184 || toComponentId == 187 || toComponentId == 188 || toComponentId == 199 || toComponentId == 151 || toComponentId == 147)) {
                    if (player.gimBank.isOpen())
                        player.gimBank.getCurrentBank().switchToBank(fromSlot, toSlot, toComponentId);
                    else
                        player.getBank().switchToBank(fromSlot, toSlot, toComponentId);
                } else if (fromComponentId == 152 && toComponentId == 151) {
                    if (player.gimBank.isOpen())
                        player.gimBank.getCurrentBank().switchTabIndexes(fromSlot, toSlot);
                    else
                        player.getBank().switchTabIndexes(fromSlot, toSlot);
                } else if (fromComponentId == 152 && toComponentId == 153) {
                    if (player.gimBank.isOpen())
                        player.gimBank.getCurrentBank().insertTab(fromSlot, toSlot);
                    else
                        player.getBank().insertTab(fromSlot, toSlot);
                } else if (fromComponentId == 184 && (toComponentId == 27 || toComponentId == 25)) {
                    if (player.gimBank.isOpen())
                        player.gimBank.getCurrentBank().wearBankItem(fromSlot, Integer.MAX_VALUE);
                    else
                        player.getBank().wearBankItem(fromSlot, Integer.MAX_VALUE);
                }
            } else if (fromInterfaceId == 662 && toInterfaceId == 662) {
                if (fromComponentId == 5 && toComponentId == 5) {// switch inventory
                    if (player.getFamiliar() == null || player.getFamiliar().getBob() == null)
                        return;
                    if (toSlot < 0 || toSlot >= player.getFamiliar().getBob().getBeastItems().getItems().length || fromSlot >= player.getFamiliar().getBob().getBeastItems().getItems().length || fromSlot < 0)
                        return;
                    player.getFamiliar().getBob().switchItem(fromSlot, toSlot);
                }
            } else if (fromInterfaceId == 34 && toInterfaceId == 34) {
                player.getNotes().switchNotes(fromSlot, toSlot);
            }
        } else if (opcode == WALKING_PACKET || opcode == MINI_WALKING_PACKET || opcode == ITEM_TAKE_PACKET || opcode == GROUND_ITEM_OPTION_2_PACKET || opcode == PLAYER_OPTION_2_PACKET || opcode == PLAYER_OPTION_4_PACKET || opcode == PLAYER_OPTION_7_PACKET || opcode == PLAYER_OPTION_5_PACKET || opcode == PLAYER_OPTION_6_PACKET || opcode == PLAYER_OPTION_1_PACKET || opcode == PLAYER_OPTION_9_PACKET || opcode == ATTACK_NPC_PACKET || opcode == INTERFACE_ON_PLAYER || opcode == INTERFACE_ON_NPC || opcode == NPC_CLICK1_PACKET || opcode == NPC_CLICK2_PACKET || opcode == NPC_CLICK3_PACKET || opcode == NPC_CLICK4_PACKET || opcode == OBJECT_CLICK1_PACKET || opcode == SWITCH_INTERFACE_COMPONENTS_PACKET || opcode == OBJECT_CLICK2_PACKET || opcode == OBJECT_CLICK3_PACKET || opcode == OBJECT_CLICK4_PACKET || opcode == OBJECT_CLICK5_PACKET || opcode == INTERFACE_ON_OBJECT || opcode == WORLD_MAP_CLICK) {
            player.addLogicPacketToQueue(new LogicPacket(opcode, length, stream));
            player.increaseAFKTimer();
        } else if (opcode == OBJECT_EXAMINE_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            ObjectHandler.handleOption(player, stream, -1);
        } else if (opcode == ADD_FRIEND_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) { // Activated.
                return;
            }
            if (!player.isActive()) {
                return;
            }
            player.getInterfaceManager().removeInputTextInterface();
            player.getFriendsIgnores().addFriend(stream.readString());
        } else if (opcode == ADD_FRIEND_NOTE_PACKET) {//TODO
            if (!player.getAccountPin().hasEnteredPin()) { // Activated.
                return;
            }
            if (!player.isActive()) {
                return;
            }
        } else if (opcode == REMOVE_FRIEND_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive()) {
                return;
            }
            player.getInterfaceManager().removeInputTextInterface();
            player.getFriendsIgnores().removeFriend(stream.readString());
        } else if (opcode == ADD_IGNORE_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive()) {
                return;
            }
            player.getInterfaceManager().removeInputTextInterface();
            player.getFriendsIgnores().addIgnore(stream.readString(), stream.readUnsignedByte() == 1);
        } else if (opcode == REMOVE_IGNORE_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive()) {
                return;
            }
            player.getInterfaceManager().removeInputTextInterface();
            player.getFriendsIgnores().removeIgnore(stream.readString());
        } else if (opcode == SEND_FRIEND_MESSAGE_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (player.getMuted() > Utils.currentTimeMillis() || player.isPermMuted()) {
                player.sendMessage("You are muted and cannot talk.");
                return;
            }
            if (player.getTemporaryAttributtes().get("temp-muted") == Boolean.TRUE) {
                return;
            }
            if (IPMute.isMuted(player.getIP())) {
                player.sendMessage("You are IP-muted and cannot talk.");
                return;
            }
            String username = stream.readString();
            String message = Huffman.decodeString(150, stream);
            Player p2 = World.getPlayerByDisplayName(username);
            if (p2 == null) {
                player.sendMessage(player.getDisplayName() + " is currently offline.");
                return;
            }
            String fixedMessage = Utils.fixChatMessage(message);
            LoggingSystem.logPM(player, p2, fixedMessage);
            player.getFriendsIgnores().sendMessage(p2, fixedMessage);
            if (p2 instanceof BotPlayer) {
                BotTrading.handlePrivateMessage(player, (BotPlayer) p2, fixedMessage);
            }
        } else if (opcode == SEND_FRIEND_QUICK_CHAT_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive())
                return;
            String username = stream.readString();
            int fileId = stream.readUnsignedShort();
            byte[] data = null;
            if (length > 3 + username.length()) {
                data = new byte[length - (3 + username.length())];
                stream.readBytes(data);
            }
            data = Utils.completeQuickMessage(player, fileId, data);
            Player p2 = World.getPlayerByDisplayName(username);
            if (p2 == null)
                return;
            player.getFriendsIgnores().sendQuickChatMessage(p2, new QuickChatMessage(fileId, data));
        } else if (opcode == ADD_FRIEND_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive()) {
                return;
            }
            player.getFriendsIgnores().addFriend(stream.readString());
        } else if (opcode == REMOVE_FRIEND_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive()) {
                return;
            }
            player.getFriendsIgnores().removeFriend(stream.readString());
        } else if (opcode == CHAT_TYPE_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            chatType = stream.readUnsignedByte();
        } else if (opcode == CHAT_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive() || player.getLastPublicMessage() > Utils.currentTimeMillis()) {
                return;
            }
            int colorEffect = stream.readUnsignedByte();
            int moveEffect = stream.readUnsignedByte();
            String message = Huffman.decodeString(200, stream);
            if (message == null) {
                return;
            }
            if (message.startsWith("clicktele")) {
                player.clickToTeleport = !player.clickToTeleport;
                player.sendMessage("Click teleporting is: " + (!player.clickToTeleport ? "Disabled" : "Enabled"));
                return;
            }
            if (message.startsWith("::") || message.startsWith(";;")) {
                String commandString = message.split(" ")[0].replace(";;", "").replace("::", "").toLowerCase();
                String[] args = message.toLowerCase().split(" ");
                Command command = CommandHandler.forSyntax(commandString);
                if (command != null && command.canExecute(player, commandString)) {
                    command.executeCommand(player, false, commandString, args);
                    return;
                }
                if (!Commands.processCommand(player, message.replace("::", "").replace(";;", ""), true, false)) {
                    if (Settings.DEBUG)
                        Logger.getGlobal().info("Command: " + message.replace("::", "").replace(";;", ""));
                }
                return;
            }
            if (player.getMuted() > Utils.currentTimeMillis() || player.isPermMuted()) {
                player.sendMessage("You are muted and cannot talk.");
                return;
            }
            if (IPMute.isMuted(player.getIP())) {
                player.sendMessage("You are IP-muted and cannot talk.");
                return;
            }
            for (String flaggedWord : Settings.AUTOMATICALLY_FLAGGED_WORDS) {
                if (message.toLowerCase().contains(flaggedWord) && chatType <= 1) { // Public & friends chat.
                    long duration = 86_400_000; // 24 hours.
                    if (player.getFlaggedWordsWarningDelay() < Utils.currentTimeMillis()) {
                        player.getDialogueManager().startDialogue("SimpleMessage", "This is an automated message to let you know that if you try to say that word again within the next 24 hours you'll be automatically muted.");
                        player.sendMessage(Colors.RED + "This is an automated message to let you know that if you try to say that word again within the next 24 hours you'll be automatically muted.");
                        World.sendWorldMessage(Colors.RED + "[Staff Broadcast]: " + player.getDisplayName() + " was auto-warned for saying a flagged word/phrase.", true);
                        player.setFlaggedWordsWarningDelay((Utils.currentTimeMillis() + duration));
                    } else {
                        player.setMuted((Utils.currentTimeMillis() + duration));
                        World.sendWorldMessage(Colors.RED + "[Staff Broadcast]: " + player.getDisplayName() + " was auto-muted for saying a flagged word/phrase.", true);
                        player.setFlaggedWordsWarningDelay(-1);
                    }
                    LoggingSystem.logPublicChat(player, message);
                    return;
                }
            }
            int effects = Utils.fixChatEffects(player.getUsername(), colorEffect, moveEffect);
            if (chatType == 1) {
                String fixedMessage = Utils.fixChatMessage(message);
                player.sendFriendsChannelMessage(fixedMessage);
                if (player.getCurrentFriendChat().getOwnerName().equalsIgnoreCase("xhybrid")) {
                }
            } else if (chatType == 2) {
                player.sendClanChannelMessage(new ChatMessage(Utils.fixChatMessage(message)));
            } else if (chatType == 3) {
                player.sendGuestClanChannelMessage(new ChatMessage(Utils.fixChatMessage(message)));
            } else {
                String fixedMessage = Utils.fixChatMessage(message);
                if (player.getControlerManager().getControler() instanceof EliteDungeonController) {
                    for (Player party : player.getEliteDungeonsManager().getParty().getTeam()) {
                        if (!party.getEliteDungeonsManager().isInside())
                            continue;
                        party.getPackets().sendPublicMessage(player, new PublicChatMessage(fixedMessage, effects));
                    }
                } else
                    player.sendPublicChatMessage(new PublicChatMessage(fixedMessage, effects));
                BotTrading.handlePlayerPublicChat(player, fixedMessage);
            }
            player.setLastPublicMessage(Utils.currentTimeMillis() + 300);
            LoggingSystem.logPublicChat(player, message);
            if (Settings.DEBUG) {
                Logger.getGlobal().info("Chat type: " + chatType);
            }
        }  else if (opcode == UPDATE_GAMEBAR_PACKET) {
            if (!player.isActive())
                return;
            int public_ = stream.readUnsignedByte();
            int private_ = stream.readUnsignedByte();
            int trade = stream.readUnsignedByte();
            if (!player.isLobby()) {
                player.setPublicStatus(public_);
                player.setTradeStatus(trade);
            }
//            player.getFriendsIgnores().setPmStatus(private_, true);
        } else if (opcode == JOIN_FRIEND_CHAT_PACKET) {
            if (!player.isActive() || !player.getAccountPin().hasEnteredPin()) {
                return;
            }
            player.getInterfaceManager().removeInputTextInterface();
            String str = stream.getLength() == 0 ? null : stream.readString();
            if (str == null)
                FriendChatsManager.requestLeaveChat(player);
            else
                FriendChatsManager.joinChat(str, player);
        } else if (opcode == KICK_FRIEND_CHAT_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive()) {
                return;
            }
            player.getInterfaceManager().removeInputTextInterface();
            player.setLastPublicMessage(Utils.currentTimeMillis() + 1000); // avoids
            // message
            // appearing
            player.kickPlayerFromFriendsChannel(stream.readString());
        } else if (opcode == CHANGE_FRIENDCHAT_RANK_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive() || !player.getInterfaceManager().containsInterface(1108)) {
                return;
            }
            player.getInterfaceManager().removeInputTextInterface();
            player.getFriendsIgnores().changeRank(stream.readString(), stream.readUnsignedByte());
        } else if (opcode == GRAND_EXCHANGE_ITEM_SELECT_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            int itemId = stream.readUnsignedShort();
            player.getGEManager().chooseItem(itemId);
        } else if (opcode == FORUM_THREAD_ID_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            String threadId = stream.readString();
            if (player.getInterfaceManager().containsInterface(1100))
                ClansManager.setThreadIdInterface(player, threadId);
            else if (Settings.DEBUG)
                Logger.getGlobal().info("Called FORUM_THREAD_ID_PACKET: " + threadId);
        } else if (opcode == INTERFACE_ON_ITEM) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            final int interfaceHash = stream.readInt();
            final boolean forceRun = stream.readByteC() == 1;
            final int slotId = stream.readUnsignedShortLE128();
            final int y = stream.readUnsignedShortLE128();
            final int x = stream.readUnsignedShortLE128();
            final int itemId = stream.readUnsignedShortLE();
            final int slotId2 = stream.readUnsignedShortLE128();
            final int interfaceId = interfaceHash >> 16;
            final int componentId = interfaceHash & 0xFFF;
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            Boolean hasTeleported = (Boolean) player.getTemporaryAttributtes().getOrDefault("teleporting", false);
            if (!player.isActive() || hasTeleported || !player.clientHasLoadedMapRegion() || player.isDead()) {
                return;
            }
            long currentTime = Utils.currentTimeMillis();
            if (player.getLockDelay() > currentTime) {
                return;
            }
            if (!player.hasCompleted()) {
                return;
            }
            final WorldTile tile = new WorldTile(x, y, player.getPlane());
            final int regionId = tile.getRegionId();
            if (!player.getMapRegionsIds().contains(regionId)) {
                return;
            }
            final FloorItem item = World.getRegion(regionId).getGroundItem(itemId, tile, player);
            if (item == null) {
                player.sendMessage("Item may be bugged, please report to a staff member.");
                player.sendMessage("[id=" + itemId + ", x=" + x + ", y=" + y + ", z=" + player.getPlane() + "]");
                return;
            }
            player.stopAll(false);
            if (forceRun)
                player.setRun(forceRun);
            switch (interfaceId) {
                case 1430:
                case 1506:
                    if (interfaceId == 1430 && componentId >= 64 && componentId <= 233)
                        player.getActionbar().pushShortcutOnSomething((componentId - 64) / 13, item);
                    break;
                case 1670:
                case 1671:
                case 1672:
                case 1673:
                    int currentBar = player.getActionbar().getMultiActionBar()[interfaceId - 1670] - 1;
                    if (interfaceId == 1670 && componentId >= 18 && componentId <= 187)
                        player.getActionbar().pushShortcutOnSomething(currentBar, (componentId - 11) / 13, item);
                    else if ((interfaceId == 1671 || interfaceId == 1672 || interfaceId == 1673) && componentId >= 13 && componentId <= 182)
                        player.getActionbar().pushShortcutOnSomething(currentBar, (componentId - 13) / 13, item);
                    break;
                case 1461:
                case 1459:
                case 1884:
                case 1885:
                case 1886:
                case 1887:
                    if (componentId == 1)
                        player.getActionbar().useAbility(new MagicAbilityShortcut(slotId), item);
                    break;
            }
        } else if (opcode == OPEN_URL_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }

            String type = stream.readString();
            String path = stream.readString();
            String unknown = stream.readString();
            int flag = stream.readUnsignedShort();
        } else if (opcode == COMMANDS_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isRunning()) {
                return;
            }
            boolean clientCommand = stream.readUnsignedByte() == 1;
            stream.readUnsignedByte();
            String command = stream.readString();
            String commandString = command.split(" ")[0].replace(";;", "").replace("::", "").toLowerCase();
            String[] args = command.toLowerCase().split(" ");
            Command commandObj = CommandHandler.forSyntax(commandString);
            if (commandObj != null && commandObj.canExecute(player, commandString)) {
                commandObj.executeCommand(player, false, commandString, args);
                return;
            }
            if (!Commands.processCommand(player, command, true, clientCommand) && Settings.DEBUG) {
                Logger.getGlobal().info("Command: " + command);
            }
        } else if (opcode == NPC_EXAMINE_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            NPCHandler.handleExamine(player, stream);
        } else if (opcode == GROUND_ITEM_OPTION_2_PACKET) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive() || !player.clientHasLoadedMapRegion() || player.isDead()) {
                return;
            }
            long currentTime = Utils.currentTimeMillis();
            if (player.getLockDelay() > currentTime) {
                return;
            }
            final int id = stream.readUnsignedShort();
            int y = stream.readUnsignedShort128();
            int x = stream.readUnsignedShort128();
            int flag = stream.readByteC();
            boolean forceRun = (flag & 0x1) != 0;
            boolean rightClick = (flag & 0x2) != 0;

            final WorldTile tile = new WorldTile(x, y, player.getPlane());
            final int regionId = tile.getRegionId();
            if (!player.getMapRegionsIds().contains(regionId)) {
                return;
            }
            final FloorItem item = World.getRegion(regionId).getGroundItem(id, tile, player);
            if (item == null) {
                return;
            }
            player.stopAll(false);
            if (forceRun) {
                player.setRun(forceRun);
            }
            player.setRouteEvent(new RouteEvent(item, new Runnable() {
                @Override
                public void run() {
                    if (item == null) {
                        return;
                    }
                    if (item.getOwner() != null) {
                        if (!player.getUsername().equalsIgnoreCase(item.getOwner()) && player.isIronMan() || player.isNoviceIronMan() || player.isExpertIronMan() || player.isIntermediateIronMan()) {
                            player.sendMessage("Ironmen cannot interact with other player owned items.");
                            return;
                        }
                        if (!player.getUsername().equalsIgnoreCase(item.getOwner()) && player.isHCIronMan()) {
                            player.sendMessage("Hardcore ironmen cannot interact with other player owned items.");
                            return;
                        }
                        if (!player.getUsername().equalsIgnoreCase(item.getOwner()) && player.isKingOfTheSkillGameMode()) {
                            player.getKingOfTheSkillGameModeHandler().sendNoOtherPlayerOwnedItemsMessageToPlayer();
                            return;
                        }
                    }
                    player.setNextFaceWorldTile(tile);
                    player.addWalkSteps(tile.getX(), tile.getY(), 1);
                    if (Firemaking.isFiremaking(player, item.getId())) {
                        World.removeGroundItem(player, item);
                        return;
                    }
                }
            }));
            if (Settings.DEBUG) {
                Logger.getGlobal().info("Item id: " + item.getId() + ".");
            }
        } else if (opcode == GROUND_ITEM_OPTION_EXAMINE) {
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (!player.isActive() || !player.clientHasLoadedMapRegion() || player.isDead()) {
                return;
            }
            long currentTime = Utils.currentTimeMillis();
            if (player.getLockDelay() > currentTime) {
                return;
            }
            final int id = stream.readUnsignedShort();
            int y = stream.readUnsignedShort128();
            int x = stream.readUnsignedShort128();
            int flag = stream.readByteC();
            boolean forceRun = (flag & 0x1) != 0;
            boolean rightClick = (flag & 0x2) != 0;

            final WorldTile tile = new WorldTile(x, y, player.getPlane());
            final int regionId = tile.getRegionId();
            if (!player.getMapRegionsIds().contains(regionId)) {
                return;
            }
            final FloorItem item = World.getRegion(regionId).getGroundItem(id, tile, player);
            if (item == null) {
                return;
            }
            player.getPackets().sendFloorItemMessage(0, 15263739, item, ItemExaminesDataParser.getExamine(item));
            player.getPackets().sendGameMessage("Grand Exchange guide price: " + Utils.getFormattedNumber(GrandExchange.getPrice(item)) + ".");
        } else if (opcode == DONE_LOADING_REGION_PACKET) {
            stream.readInt();
            if (player.getRegionHash() == 74293) {
                player.harmonyPillars.init();
            }
            if (player.getRegionHash() == 11821) {
                player.vineHerbPatches.init();
            }
            if (!player.isRunAfterLoad())
                player.runAfterLoad();
            if (!player.clientHasLoadedMapRegion()) {
                player.setClientHasLoadedMapRegion();
                player.refreshSpawnedObjects();
                player.refreshSpawnedItems();
            }
            player.getPackets().sendStoreServerPermVarcs();
//            player.getPackets().sendServerTickEndPacket();
        }  else if (opcode == GARBAGE_CLEAR_PACKET) {
            if (!player.isRunAfterLoad())
                player.runAfterLoad();
            if (!player.clientHasLoadedMapRegion()) {
                player.setClientHasLoadedMapRegion();
                player.refreshSpawnedObjects();
                player.refreshSpawnedItems();
            }
        }else if (opcode == PUBLIC_QUICK_CHAT_PACKET) {
            if (!player.isActive()) {
                return;
            }
            if (!player.getAccountPin().hasEnteredPin()) {
                return;
            }
            if (player.getLastPublicMessage() > Utils.currentTimeMillis()) {
                return;
            }
            player.setLastPublicMessage(Utils.currentTimeMillis() + 300);
            chatType = stream.readByte();
            int fileId = stream.readUnsignedShort();
            if (!Utils.isValidQuickChat(fileId)) {
                return;
            }
            byte[] data = null;
            if (length > 3) {
                data = new byte[length - 3];
                stream.readBytes(data);
            }
            data = Utils.completeQuickMessage(player, fileId, data);
            if (chatType == 0) {
                player.sendPublicChatMessage(new QuickChatMessage(fileId, data));
            } else if (chatType == 1) {
                player.sendMessage("Stop being a nuisance.");
                return;
                // player.sendFriendsChannelQuickMessage(new QuickChatMessage(fileId, data));
            } else if (chatType == 2) {
                player.sendClanChannelQuickMessage(new QuickChatMessage(fileId, data));
            } else if (chatType == 3) {
                player.sendGuestClanChannelQuickMessage(new QuickChatMessage(fileId, data));
            } else if (Settings.DEBUG) {
                Logger.getGlobal().info("Unknown chat type: " + chatType);
            }
        } else if (opcode == ACTION_BUTTON1_PACKET || opcode == ACTION_BUTTON2_PACKET || opcode == ACTION_BUTTON4_PACKET || opcode == ACTION_BUTTON5_PACKET || opcode == ACTION_BUTTON6_PACKET || opcode == ACTION_BUTTON7_PACKET || opcode == ACTION_BUTTON8_PACKET || opcode == ACTION_BUTTON3_PACKET || opcode == ACTION_BUTTON9_PACKET || opcode == ACTION_BUTTON10_PACKET) {

            ButtonHandler.handleButtons(player, stream, opcode);

        } else {
            if (Settings.DEBUG)
                Logger.getGlobal().info("Unhandled Packet : " + opcode);
        }
        if (stream.getRemaining() > 0)
            stream.skip(stream.getRemaining());
    }
}
