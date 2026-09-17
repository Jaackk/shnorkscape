package com.rs.network.packet;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Index;
import com.rs.cache.loaders.AtmosphereDefinition;
import com.rs.cache.loaders.Class189;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.Animation;
import com.rs.game.DynamicArea;
import com.rs.game.DynamicRegion;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.NewProjectile;
import com.rs.game.Projectile;
import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.ChatMessage;
import com.rs.game.player.content.FriendChatsManager;
import com.rs.game.player.content.HintIcon;
import com.rs.game.player.content.InterfaceManager;
import com.rs.game.player.content.PublicChatMessage;
import com.rs.game.player.content.QuickChatMessage;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.grandExchange.Offer;
import com.rs.game.player.controllers.DungeonController;
import com.rs.network.handler.message.tail.impl.GamePacketWriteEvent;
import com.rs.network.io.OutputStream;
import com.rs.utils.Utils;
import com.rs.utils.WorldInformation;
import com.rs.utils.huffman.Huffman;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import lombok.val;

public class PacketDispatcher {

    private static final int LEGACY_MUSIC_INDEX = 6;
    private static final int LEGACY_MUSIC_EFFECT_INDEX = 11;
    private static final int VORBIS_SOUND_INDEX = 14;
    private static final int MIDI_SONG_PACKET = 129;
    private static final boolean NXT_MUSIC_TEST_PACKETS_ENABLED = false;
    private static final int ANCIENTX_NXT_MUSIC_PACKET = 39;
    private static final boolean ANCIENTX_NXT_MUSIC_TEST_PACKETS_ENABLED = false;
    /**
     * Master kill switch for all music + music-effect packets. Set to false
     * because music playback does not work on this codebase / 910 cache /
     * Java client combo - the opcode 129 path hangs the client in a JS5
     * load loop. See the big comment on Cache.ALIAS_RS3_MUSIC_TO_LEGACY_INDEX
     * for the full investigation. Keep this false unless someone first finds
     * a music opcode the 910 client actually accepts.
     */
    private static final boolean MUSIC_PACKETS_ENABLED = false;

    private final Player player;

    public PacketDispatcher(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void sendDungeonneringRequestMessage(Player p) {
        sendDungeonneringRequestMessage(p, false);
    }

    public void sendDungeonneringRequestMessage(Player p, boolean elite) {
        sendMessage(111, "has invited you to a " + (elite ? "elite " : "") + "dungeon party.", p);
    }

    public void sendAccessMask(Player player, int interfaceId, int componentId, int fromSlot, int toSlot, int settingsHash) {// updated
        OutputStream stream = new OutputStream(14);
        stream.writePacket(player, 155);
        stream.writeIntV2(interfaceId << 16 | componentId);
        stream.writeShortLE(fromSlot);
        stream.writeIntV1(settingsHash);
        stream.writeShort128(toSlot);
        write(stream);
    }

    public void sendIComponentSettings(int interfaceId, int componentId, int fromSlot, int toSlot, int settingsHash) {// updated
        OutputStream stream = new OutputStream(14);
        stream.writePacket(player, 155);
        stream.writeIntV2(interfaceId << 16 | componentId);
        stream.writeShortLE(fromSlot);
        stream.writeIntV1(settingsHash);
        stream.writeShort128(toSlot);
        write(stream);
    }

    public void sendFloorItemMessage(int border, int color, FloorItem item, String message) {
        sendGameMessage(message);
        sendGlobalString(306, message);
        sendGlobalConfig(1699, color);
        sendGlobalConfig(1700, border);
        sendGlobalConfig(1695, 1);
        sendFloorItemInterface(item, true, 746, 0, 1177);
    }

    public void sendInterfaceMessage(int interfaceId, int componentId, int border, int slotId, String message) {
        sendInterfaceMessage(interfaceId, componentId, border, slotId, message, true);
    }

    public void sendInterfaceMessage(int interfaceId, int componentId, int border, int slotId, String message, boolean sendGameMessage) {
        if (sendGameMessage)
            sendGameMessage(message);
        sendExecuteScript(7774, message, interfaceId << 16 | componentId, slotId, border);
    }

    public void sendFloorItemInterface(FloorItem item, boolean nocliped, int windowId, int windowComponentId, int interfaceId) {// updated
        int[] xteas = new int[4];
        OutputStream stream = new OutputStream(30);
        stream.writePacket(player, 121);
        stream.writeIntV1(xteas[0]);
        stream.writeShort(item.getId());
        stream.writeByte128(nocliped ? 1 : 0);
        stream.writeIntV2((windowId << 16) | windowComponentId);
        stream.writeInt(xteas[3]);
        stream.writeInt(xteas[2]);
        stream.writeIntV2((item.getTile().getPlane() << 28) | (item.getTile().getX() << 14) | item.getTile().getY());
        stream.writeIntLE(xteas[1]);
        stream.writeShort(interfaceId);
        write(stream);
    }

    /**
     * This will blackout specified area.
     *
     * @param area = area which will be blackout (0 = unblackout; 1 = blackout orb;
     *             2 = blackout map; 5 = blackout orb and map)
     */
    public void sendMiniMapStatus(int area) {// updated
        OutputStream out = new OutputStream(3);
        out.writePacket(player, 137);
        out.writeByte(area);
        write(out);
    }

    public void sendDungDuoRequestMessage(Player p, boolean friendly) {
        sendMessage(101, "wishes to play a duo Dungeoneering.", p);
    }

    public void sendMoveIComponent(int interfaceId, int componentId, int x, int y) {// updated
        OutputStream stream = new OutputStream(9);
        stream.writePacket(player, 72);
        stream.writeShort(x);
        stream.writeShort128(y);
        stream.writeIntV1(interfaceId << 16 | componentId);
        write(stream);
    }

    public void sendInterfaceConfig(int interfaceId, int componentId, boolean hide) {// updated
        OutputStream stream = new OutputStream(6);
        stream.writePacket(player, 109);
        stream.writeByte(hide ? 1 : 0);
        stream.writeIntV1(interfaceId << 16 | componentId);
        write(stream);
    }

    public void sendHideIComponent(int interfaceId, int componentId, boolean hidden) {// updated
        OutputStream stream = new OutputStream(6);
        stream.writePacket(player, 109);
        stream.writeByte(hidden ? 1 : 0);
        stream.writeIntV1(interfaceId << 16 | componentId);
        write(stream);
    }

    public void sendIComponentTransparency(int widgetID, byte transparency) {
        sendIComponentTransparency(widgetID, 0, transparency);
    }

    public void sendIComponentTransparency(int widgetID, int componentID, byte transparency) {
//        OutputStream stream = new OutputStream(6); TODO no such packet
//        stream.writePacket(player, 159);
//        stream.writeInt(widgetID << 16 | componentID);
//        stream.writeByte(transparency);
//        write(stream);
    }

    public void sendText(int interfaceId, int componentId, Object text) {
        sendIComponentText(interfaceId, componentId, text);
    }

    public void sendIComponentText(int interfaceId, int componentId, Object text) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 181);
        stream.writeString(text.toString());
        stream.writeInt(interfaceId << 16 | componentId);
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendWindowsPane(int id, int type) {// updated
        int[] xteas = new int[4];
        player.getInterfaceManager().setWindowsPane(id);
        OutputStream stream = new OutputStream(20);
        stream.writePacket(player, 35);
        stream.writeShortLE128(id);
        stream.writeIntV1(xteas[2]);
        stream.writeIntV2(xteas[1]);
        stream.writeIntV2(xteas[0]);
        stream.writeByteC(type);
        stream.writeIntLE(xteas[3]);
        write(stream);
    }

    public void sendInterface(boolean clickThrought, int parentUID, int interfaceId) {// updated
        int[] xteas = new int[4];
        OutputStream stream = new OutputStream(24);
        stream.writePacket(player, 38);
        stream.writeShort(interfaceId);
        stream.writeIntV2(xteas[0]);
        stream.writeInt(xteas[1]);
        stream.write128Byte(clickThrought ? 1 : 0);
        stream.writeIntLE(parentUID);
        stream.writeIntLE(xteas[3]);
        stream.writeIntLE(xteas[2]);
        write(stream);
    }

    public void moveInterface(int fromParentUID, int toParentUID) {
        OutputStream stream = new OutputStream(10);
        stream.writePacket(player, 191);
        stream.writeInt(fromParentUID);
        stream.writeIntLE(toParentUID);
        write(stream);
    }

    public void sendPlayerMessage(int border, int color, String message, boolean sendGameMessage) {
        sendPlayerMessage(border, color, player, message, sendGameMessage);
    }

    public void sendPlayerMessageBox(String message) {
        sendPlayerMessage(1, 15263739, player, message, true);
    }

    public void sendPlayerMessage(int border, int color, Player p, String message, boolean sendGameMessage) {
        if (sendGameMessage)
            sendGameMessage(message);
        sendGlobalString(2251, message);
        sendGlobalConfig(1699, color);
        sendGlobalConfig(1700, border);
        sendGlobalConfig(1695, 1);
        sendPlayerInterface(p, true, InterfaceManager.RESIZABLE_WINDOW_ID, 37, 1177);
    }

    public void sendPlayerInterface(Player p, boolean nocliped, int windowId, int windowComponentId, int interfaceId) {// updated
        int[] xteas = new int[4];
        OutputStream stream = new OutputStream(26);
        stream.writePacket(player, 61);
        stream.writeInt(xteas[3]);
        stream.write128Byte(nocliped ? 1 : 0);
        stream.writeShortLE(interfaceId);
        stream.writeIntLE(xteas[0]);
        stream.writeShort(p.getIndex());
        stream.writeIntV1(windowId << 16 | windowComponentId);
        stream.writeIntV1(xteas[2]);
        stream.writeIntV1(xteas[1]);
        write(stream);
    }

    public void sendEntityMessage(int border, int color, Entity entity, String message) {
        sendEntityMessage(border, color, entity, message, true);
    }

    public void sendEntityMessage(int border, int color, Entity entity, String message, boolean sendGameMessage) {
        if (sendGameMessage)
            sendGameMessage(message);
        sendGlobalString(2251, message);
        sendGlobalConfig(1699, color);
        sendGlobalConfig(1700, border);
        sendGlobalConfig(1695, 1);
        sendEntityInterface(entity, true, InterfaceManager.RESIZABLE_WINDOW_ID, 37, 1177);
    }

    public void sendNPCMessage(int border, int color, Entity npc, String message) {// updated
        sendGameMessage(message);
        sendGlobalString(2251, message);
        sendGlobalConfig(1699, color);
        sendGlobalConfig(1700, border);
        sendGlobalConfig(1695, 1);
        sendNPCInterface(npc, true, InterfaceManager.RESIZABLE_WINDOW_ID, 37, 1177);
    }

    public void sendNPCInterface(Entity npc, boolean nocliped, int windowId, int windowComponentId, int interfaceId) {// updated
        int[] xteas = new int[4];
        OutputStream stream = new OutputStream(26);
        stream.writePacket(player, 102);
        stream.writeInt(xteas[2]);
        stream.writeShort(interfaceId);
        stream.writeIntV1(xteas[3]);
        stream.writeIntV2(xteas[0]);
        stream.writeInt(xteas[1]);
        stream.write128Byte(nocliped ? 1 : 0);
        stream.writeIntLE(windowId << 16 | windowComponentId);
        stream.writeShort(npc.getIndex());
        write(stream);
    }

    public void sendObjectMessage(int border, int color, WorldObject object, String message) {// updated
        sendGameMessage(message);
        sendGlobalString(2251, message);
        sendGlobalConfig(1699, color);
        sendGlobalConfig(1700, border);
        sendGlobalConfig(1695, 1);
        sendObjectInterface(object, true, InterfaceManager.RESIZABLE_WINDOW_ID, 37, 1177);
    }

    public void sendObjectInterface(WorldObject object, boolean nocliped, int windowId, int windowComponentId, int interfaceId) {// updated
        int[] xteas = new int[4];
        OutputStream stream = new OutputStream(33);
        stream.writePacket(player, 26);
        stream.write128Byte(nocliped ? 1 : 0);
        stream.writeIntLE(xteas[0]);
        stream.writeIntLE(object.getId());
        stream.writeIntLE((windowId << 16) | windowComponentId);
        stream.writeIntV1(xteas[1]);
        stream.writeIntLE(xteas[3]);
        stream.writeIntV1((object.getPlane() << 28) | (object.getX() << 14) | object.getY());
        stream.writeShortLE128(interfaceId);
        stream.writeByte128((object.getType() << 2) | (object.getRotation() & 0x3));
        stream.writeIntV2(xteas[2]);
        write(stream);
    }

    public void sendLogout() {// updated
        OutputStream stream = new OutputStream(3);
        stream.writePacket(player, 168);
        stream.writeByte(0);// new class unknown
        ChannelFuture future = write(stream);
        if (future != null) {
            future.addListener(ChannelFutureListener.CLOSE);
        } else if (!player.isBot() && player.getRealChannel() != null) {
            player.getRealChannel().close();
        }
    }

    public void sendStillProjectile(WorldTile receiver, WorldTile startTile, WorldTile endTile, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset, int creatorSize) {// updated
        OutputStream stream = createWorldTileStream(startTile);
        stream.writePacket(player, 13);
        int localX = startTile.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int localY = startTile.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int offsetX = localX - (localX >> 3 << 3);
        int offsetY = localY - (localY >> 3 << 3);
        stream.writeByte(offsetX << 3 | offsetY);
        stream.writeByte(endTile.getX() - startTile.getX());
        stream.writeByte(endTile.getY() - startTile.getY());
        if (receiver instanceof Entity)
            stream.writeShort(receiver == null ? 0 : receiver instanceof Player ? -(((Entity) receiver).getIndex() + 1) : ((Entity) receiver).getIndex() + 1);
        else if (startTile instanceof Entity)
            stream.writeShort(startTile == null ? 0 : startTile instanceof Player ? -(((Entity) startTile).getIndex() + 1) : ((Entity) startTile).getIndex() + 1);
        else
            stream.writeShort(0);
        stream.writeShort(gfxId);
        stream.writeByte(startHeight);
        stream.writeByte(endHeight);
        stream.writeShort(delay);
        stream.writeShort(speed);
        stream.writeByte(curve);
        stream.writeShort(creatorSize * 64 + startDistanceOffset * 64);
        stream.writeShort(0);// new
        write(stream);
    }

    public void sendMinimapFlag(int x, int y) {// updated
        OutputStream stream = new OutputStream(3);
        stream.writePacket(player, 94);
        stream.write128Byte(x);
        stream.write128Byte(y);
        write(stream);
    }

    public void sendResetMinimapFlag() {
        sendMinimapFlag(255, 255);
    }

    public void sendClanChannel(ClansManager manager, boolean myClan) {// updated
        OutputStream stream = new OutputStream(manager == null ? 4 : manager.getClanChannelDataBlock().length + 4);
        stream.writePacketVarShort(player, 59);
        stream.writeByte(myClan ? 1 : 0);
        if (manager != null) {
            stream.writeBytes(manager.getClanChannelDataBlock());
        }
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendClanSettings(ClansManager manager, boolean myClan) {// updated
        OutputStream stream = new OutputStream(manager == null ? 4 : manager.getClanSettingsDataBlock().length + 4);
        stream.writePacketVarShort(player, 101);
        stream.writeByte(myClan ? 1 : 0);
        if (manager != null) {
            stream.writeBytes(manager.getClanSettingsDataBlock());
        }
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendClanInviteMessage(Player p) {
        sendMessage(117, p.getDisplayName() + " is inviting you to join their clan.", p);
    }

    public void sendHideComponents(int interfaceId, boolean hide, int... components) {
        for (int i : components) {
            sendHideIComponent(interfaceId, i, hide);
        }
    }

    public void sendEmptyTextToComponents(int interfaceId, int... components) {
        for (int i : components)
            sendText(interfaceId, i, "");
    }

    public void sendIComponentSprite(int interfaceId, int componentId, int spriteId) {// updated
        OutputStream stream = new OutputStream(10);
        stream.writePacket(player, 183);
        stream.writeIntLE(spriteId);
        stream.writeIntV2(interfaceId << 16 | componentId);
        write(stream);
    }

    public void receiveClanChatQuickMessage(boolean myClan, String display, int rights, QuickChatMessage message) {// updated
        Player other = World.getPlayerByDisplayName(display);
        if (player.getFriendsIgnores().getIgnores().equals(other.getUsername()) && other.getRights() != 2) {
            return;
        }
        OutputStream stream = new OutputStream();
        stream.writePacketVarByte(player, 89);
        stream.writeByte(myClan ? 1 : 0);
        stream.writeString(display);
        for (int i = 0; i < 5; i++) {
            stream.writeByte(Utils.getRandom(255));
        }
        stream.writeByte(rights);
        stream.writeShort(message.getFileId());
        if (message.getMessage() != null) {
            stream.writeBytes(message.getMessage().getBytes());
        }
        stream.endPacketVarByte();
        write(stream);
    }

    public void receiveClanChatMessage(boolean myClan, String display, int rights, ChatMessage message) {// updated
        Player other = World.getPlayerByDisplayName(display);
        if (player.getFriendsIgnores().getIgnores().equals(other.getUsername()) && other.getRights() != 2) {
            return;
        }
        OutputStream stream = new OutputStream();
        stream.writePacketVarByte(player, 22);
        stream.writeByte(myClan ? 1 : 0);
        stream.writeString(display);
        for (int i = 0; i < 5; i++) {
            stream.writeByte(Utils.getRandom(255));
        }
        stream.writeByte(rights);
        Huffman.encodeString(stream, message.getMessage(false));
        stream.endPacketVarByte();
        write(stream);
    }

    @Deprecated
    public void sendVar(int id, int value) {
        sendConfig(id, value);
    }

    @Deprecated
    public void sendVarBit(int id, int value) {
        sendConfigByFile(id, value);
    }

    public void sendGrandExchangeOffer(Offer offer) {// updated
        OutputStream stream = new OutputStream(22);
        stream.writePacket(player, 28);
        stream.writeByte(0);
        stream.writeByte(offer.getSlot());
        stream.writeByte(offer.getStage());
        if (offer.forceRemove()) {
            stream.skip(18);
        } else {
            stream.writeShort(offer.getId());
            stream.writeInt(offer.getPrice());
            stream.writeInt(offer.getAmount());
            stream.writeInt(offer.getTotalAmountSoFar());
            stream.writeInt(offer.getTotalPriceSoFar());
        }
        write(stream);
    }

    public void sendRunScriptBlank(int scriptId) {
        sendRunScript(scriptId);
    }

    public void sendFilteredGameMessage(boolean filter, String text, Object... args) {
        sendMessage(filter ? 109 : 0, String.format(text, args), null);
    }

    public void refreshWeight() {// updated
        final OutputStream stream = new OutputStream(4);
        stream.writePacket(player, 169);
        stream.writeShort((int) player.getWeight());
        write(stream);
    }

    public static void sendScroll(Player player, String title, String... scrollMessage) {
        // Until fix interface 1245 length
        // player.getPackets().sendIComponentText(275, 2, title);
        player.getPackets().sendIComponentText(275, 1, "      " + title);
        // int scrollChild = 12;
        int scrollChild = 10;
        for (final String line : scrollMessage) {
            if (line == null) {
                break;
            }
            if (scrollChild >= 300) {
                break;
            }
            String text = line;
            if (text.contains(">")) {
                text = line.split(">")[line.split(">").length - 1];
            }
            if (text.length() > 60) {// borrowing this a sec
                String color = line.contains("col=") && line.contains(">") ? "<col=" + line.split("col=")[1].split(">")[0] + ">" : "";
                String shade = line.contains("shad=") && line.contains(">") ? "<shad=" + line.split("shad=")[1].split(">")[0] + ">" : "";
                // String colors = line.contains("<") && line.contains(">") ?
                // "<"+line.split("<")[1].split(">")[0]+">" : "";
                String[] strings = Utils.splitString(60, text);
                player.getPackets().sendIComponentText(275, scrollChild, color + shade + strings[0]);
                scrollChild++;
                player.getPackets().sendIComponentText(275, scrollChild, color + shade + strings[1]);
                scrollChild++;
            } else {
                player.getPackets().sendIComponentText(275, scrollChild, line);
                scrollChild++;
            }
        }
        for (int i = scrollChild; i < scrollChild + 50; i++) {
            if (i >= 300) {
                break;
            }
            player.getPackets().sendIComponentText(275, i, " ");
        }
        player.getInterfaceManager().sendInterface(275);
        return;
    }

    public void sendWarning(String string) {
        // id, border, posX, posY
        sendRunScript(1211, 1, -20, -20, "<col=FF0000>WARNING: <col=ffffff>" + string);
    }

    /**
     * NEW SHIT
     **/

    public void sendPlayerUnderNPCPriority(boolean priority) {// updated this is incorrect (send show face here is the correct name)
        OutputStream stream = new OutputStream(2);
        stream.writePacket(player, 47);
        stream.writeByteC(priority ? 1 : 0);
        write(stream);
    }

    public void sendHintIcon(HintIcon icon) {// updated
        OutputStream stream = new OutputStream(15);
        stream.writePacket(player, 78);
        stream.writeByte(icon.getTargetType() & 0x1f | icon.getIndex() << 5);
        if (icon.getTargetType() == 0) {
            stream.skip(13);
        } else {
            stream.writeByte(icon.getArrowType());
            if (icon.getTargetType() == 1 || icon.getTargetType() == 10) {
                stream.writeShort(icon.getTargetIndex());
                stream.writeShort(2500); // how often the arrow flashes, 2500
                // ideal, 0 never
                stream.skip(4);
            } else if (icon.getTargetType() >= 2 && icon.getTargetType() <= 6) { // directions
                stream.writeByte(icon.getPlane()); // unknown
                stream.writeShort(icon.getCoordX());
                stream.writeShort(icon.getCoordY());
                stream.writeByte(icon.getDistanceFromFloor() * 4 >> 2);
                stream.writeShort(-1); // distance to start showing on minimap,
                // 0 doesnt show, -1 infinite
            }
            stream.writeInt(icon.getModelId());
        }
        write(stream);
    }

    /**
     * Sends settings to the client.
     */
    public void sendSettingPacket(int setting, boolean value) {
//        OutputStream stream = new OutputStream(10); TODO custom packet
//        stream.writePacket(player, 160);
//        stream.writeByte(setting);
//        stream.writeByte(value ? 1 : 0);
//        write(stream);
    }

    public void sendCameraShake(int slotId, int b, int c, int d, int e) {// updated
        OutputStream stream = new OutputStream(8);
        stream.writePacket(player, 171);
        stream.write128Byte(d);
        stream.writeByteC(slotId);
        stream.writeByte(c);
        stream.writeByte(b);
        stream.writeShort128(e);
        write(stream);
    }

    public void sendStopCameraShake() {// updated (correct name cam_reset)
        OutputStream stream = new OutputStream(1);
        stream.writePacket(player, 104);
        write(stream);
    }

    public void sendIComponentModel(int interfaceId, int componentId, int modelId) {// updated
        OutputStream stream = new OutputStream(9);
        stream.writePacket(player, 116);
        stream.writeInt(interfaceId << 16 | componentId);
        stream.writeInt(modelId);
        write(stream);
    }

    public void sendRemoveGroundItem(FloorItem item) {// updated
        OutputStream stream = createWorldTileStream(item.getTile());
        int localX = item.getTile().getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int localY = item.getTile().getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int offsetX = localX - (localX >> 3 << 3);
        int offsetY = localY - (localY >> 3 << 3);
        stream.writePacket(player, 49);
        stream.writeShort(item.getId());
        stream.write128Byte((offsetX << 4) | offsetY);
        if (item.hasLootbeam())
            sendGraphics(new Graphics(-1), item.getTile());
        write(stream);
    }

    public void sendGroundItem(FloorItem item) {// updated
        if (item.getOwner() != null) {
            if (World.getPlayer(item.getOwner()) != null) {
                Player owner = World.getPlayer(item.getOwner());
                if (owner.getControlerManager().getControler() instanceof DungeonController) {
                    if (!(player.getControlerManager().getControler() instanceof DungeonController))
                        return;
                }
            }
        }
        OutputStream stream = createWorldTileStream(item.getTile());
        int localX = item.getTile().getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int localY = item.getTile().getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int offsetX = localX - (localX >> 3 << 3);
        int offsetY = localY - (localY >> 3 << 3);
        stream.writePacket(player, 85);
        stream.write128Byte((offsetX << 4) | offsetY);
        stream.writeShortLE128(item.getId());
        stream.writeShortLE(item.getAmount());
        write(stream);
        if (item.hasLootbeam()) {
            sendGraphics(player.getLootBeamManager().getCurrentLootBeamType().getGraphics(), item.getTile());
        }
    }

    public void sendTestProjectile(NewProjectile projectile) {// updated
        OutputStream stream = createWorldTileStream(projectile.getFrom());
        stream.writePacket(player, 13);
        int localX = projectile.getFrom().getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int localY = projectile.getFrom().getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int offsetX = localX - ((localX >> 3) << 3);
        int offsetY = localY - ((localY >> 3) << 3);
        stream.writeByte(offsetX << 3 | offsetY);
        stream.writeByte(projectile.getTo().getX() - projectile.getFrom().getX());
        stream.writeByte(projectile.getTo().getY() - projectile.getFrom().getY());
        Entity receiver = projectile.getTo() instanceof Entity ? (Entity) projectile.getTo() : null;
        final Entity sender = projectile.getFrom() instanceof Entity ? (Entity) projectile.getFrom() : null;
        stream.writeShort(receiver == null ? 0 : receiver instanceof Player ? -(receiver.getIndex() + 1) : receiver.getIndex() + 1);
        stream.writeShort(projectile.getGraphicsId());
        stream.writeByte(projectile.getStartHeight());
        stream.writeByte(projectile.getEndHeight());
        stream.writeShort(projectile.getDelay());
        int duration = Utils.getDistance(projectile.getFrom().getX(), projectile.getFrom().getY(), projectile.getTo().getX(), projectile.getTo().getY()) * 30 / (projectile.getSpeed() / 10 < 1 ? 1 : projectile.getSpeed() / 10) + projectile.getDelay();
        stream.writeShort(duration);
        stream.writeByte(projectile.getSlope());
        stream.writeShort((sender == null ? 0 : sender.getSize()) * 64 + projectile.getDistanceOffset() * 64);
        stream.writeShort(0);// new
        write(stream);
    }

    public void sendProjectile(Entity receiver, WorldTile startTile, WorldTile endTile, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset, int creatorSize) {// updated
        OutputStream stream = createWorldTileStream(startTile);
        stream.writePacket(player, 13);
        int localX = startTile.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int localY = startTile.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int offsetX = localX - (localX >> 3 << 3);
        int offsetY = localY - (localY >> 3 << 3);
        stream.writeByte(offsetX << 3 | offsetY);
        stream.writeByte(endTile.getX() - startTile.getX());
        stream.writeByte(endTile.getY() - startTile.getY());
        stream.writeShort(receiver == null ? 0 : receiver instanceof Player ? -(receiver.getIndex() + 1) : receiver.getIndex() + 1);
        stream.writeShort(gfxId);
        stream.writeByte(startHeight);
        stream.writeByte(endHeight);
        stream.writeShort(delay);
        int duration = Utils.getDistance(startTile.getX(), startTile.getY(), endTile.getX(), endTile.getY()) * 30 / (speed / 10 < 1 ? 1 : speed / 10) + delay;
        stream.writeShort(duration);
        stream.writeByte(curve);
        stream.writeShort(creatorSize * 64 + startDistanceOffset * 64);
        stream.writeShort(0);// new
        write(stream);
    }

    public void sendUnlockIComponentOptionSlots(int interfaceId, int componentId, int fromSlot, int toSlot, boolean unlockEvent, int... optionsSlots) {
        int settingsHash = unlockEvent ? 1 : 0;
        for (final int slot : optionsSlots)
            settingsHash |= 2 << slot;
        sendIComponentSettings(interfaceId, componentId, fromSlot, toSlot, settingsHash);
    }

    public void sendUnlockIComponentOptionSlots(int interfaceId, int componentId, int fromSlot, int toSlot, int... optionsSlots) {
        this.sendUnlockIComponentOptionSlots(interfaceId, componentId, fromSlot, toSlot, false, optionsSlots);
    }

    public void sendInterFlashScript(int interfaceId, int componentId, int width, int height, int slot) {
        Object[] parameters = new Object[4];
        int index = 0;
        parameters[index++] = slot;
        parameters[index++] = height;
        parameters[index++] = width;
        parameters[index++] = interfaceId << 16 | componentId;
        sendRunScript(143, parameters);
    }

    public void sendInterSetItemsOptionsScript(int interfaceId, int componentId, int key, int width, int height, String... options) {
        sendInterSetItemsOptionsScript(interfaceId, componentId, key, false, width, height, options);
    }

    public void sendSpoilsInterface(int interfaceId, int componentId, int key, int width, int height, String... options) {
        Object[] parameters = new Object[6 + options.length];
        int index = 0;
        for (int count = options.length - 1; count >= 0; count--)
            parameters[index++] = options[count];
        parameters[index++] = -1;
        parameters[index++] = 0;
        parameters[index++] = height;
        parameters[index++] = width;
        parameters[index++] = key;
        parameters[index++] = interfaceId << 16 | componentId;
        sendRunScript(149, parameters);
    }

    public void sendInterSetItemsOptionsScript(int interfaceId, int componentId, int key, boolean negativeKey, int width, int height, String... options) {
        Object[] parameters = new Object[6 + options.length];
        int index = 0;
        for (int count = options.length - 1; count >= 0; count--) {
            parameters[index++] = options[count];
        }
        parameters[index++] = -1; // dunno but always this
        parameters[index++] = 0;// dunno but always this, maybe startslot?
        parameters[index++] = height;
        parameters[index++] = width;
        parameters[index++] = key;
        parameters[index++] = interfaceId << 16 | componentId;
        sendRunScript(negativeKey ? 695 : 150, parameters); // scriptid 150 does
        // that the method
        // name says*/
    }

    public void sendExecuteScript(int scriptId, Object... params) {
        List<Object> l = Arrays.asList(params);
        Collections.reverse(l);
        sendRunScript(scriptId, l.toArray());
    }

    public void sendExecuteScriptReverse(int scriptId, Object... params) {
        sendRunScript(scriptId, params);
    }

    public void sendRunScript(int scriptId, Object... params) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 156);
        StringBuilder parameterTypes = new StringBuilder();
        if (params != null) {
            for (int count = params.length - 1; count >= 0; count--) {
                if (params[count] instanceof String) {
                    parameterTypes.append("s"); // string
                } else {
                    parameterTypes.append("i"); // integer
                }
            }
        }
        stream.writeString(parameterTypes.toString());
        if (params != null) {
            int index = 0;
            for (int count = parameterTypes.length() - 1; count >= 0; count--) {
                if (parameterTypes.charAt(count) == 's') {
                    stream.writeString((String) params[index++]);
                } else {
                    stream.writeInt((Integer) params[index++]);
                }
            }
        }
        stream.writeInt(scriptId);
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendGlobalConfig(int id, int value) {// updated
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            sendGlobalConfigLarge(id, value);
        } else {
            sendGlobalConfigSmall(id, value);
        }
    }

    public void sendGlobalConfigSmall(int id, int value) {// updated
        OutputStream stream = new OutputStream(5);
        stream.writePacket(player, 149);
        stream.writeByte(value);
        stream.writeShort128(id);
        write(stream);
    }

    public void sendGlobalConfigLarge(int id, int value) {// updated
        OutputStream stream = new OutputStream(8);
        stream.writePacket(player, 128);
        stream.writeShortLE128(id);
        stream.writeInt(value);
        write(stream);
    }

    public void sendConfig(int id, int value) {
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
            sendConfig2(id, value);
        } else {
            sendConfig1(id, value);
        }
    }

    public void sendConfig1(int id, int value) {// updated
        OutputStream stream = new OutputStream(5);
        stream.writePacket(player, 157);
        stream.writeByteC(value);
        stream.writeShort(id);
        write(stream);
    }

    public void sendConfig2(int id, int value) {// updated
        OutputStream stream = new OutputStream(7);
        stream.writePacket(player, 50);
        stream.writeShortLE128(id);
        stream.writeIntV2(value);
        write(stream);
    }

    public void sendConfigByFile(int fileId, int value, boolean newDefs) {
        if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE)
            sendConfigByFile2(fileId, value, newDefs);
        else
            sendConfigByFile1(fileId, value, newDefs);
    }

    public void sendConfigByFile(int fileId, int value) {
        sendConfigByFile(fileId, value, false);
    }

    public void sendConfigByFile1(int fileId, int value, boolean newDefs) {// updated
        OutputStream stream = new OutputStream(4);
        stream.writePacket(player, 44);
        stream.writeByte(value);
        stream.writeShortLE(fileId);
//        stream.writeByte(newDefs ? 1 : 0);
        write(stream);
    }

    public void sendConfigByFile2(int fileId, int value, boolean newDefs) {// updated
        OutputStream stream = new OutputStream(8);
        stream.writePacket(player, 142);
        stream.writeShort(fileId);
        stream.writeIntV2(value);
//        stream.writeByte(newDefs ? 1 : 0);
        write(stream);
    }

    public void sendRunEnergy() {// updated
        OutputStream stream = new OutputStream(3);
        stream.writePacket(player, 161);
        stream.writeByte(player.getRunEnergy());
        write(stream);
    }

    public void sendIComponentAnimation(int emoteId, int interfaceId, int componentId) {// updated
        OutputStream stream = new OutputStream(9);
        stream.writePacket(player, 103);
        stream.writeIntV1(interfaceId << 16 | componentId);
        stream.writeIntV1(emoteId);
        write(stream);
    }

    public void sendItemOnIComponent(int interfaceid, int componentId, int id) {
        sendItemOnIComponent(interfaceid, componentId, id, 1);
    }

    public void sendItemOnIComponent(int interfaceid, int componentId, int id, int amount) {// updated
        OutputStream stream = new OutputStream(12);
        stream.writePacket(player, 158);
        stream.writeIntV2(interfaceid << 16 | componentId);
        stream.writeInt(amount);
        stream.writeShortLE128(id);
        write(stream);

    }

    public void sendEntityOnIComponent(boolean isPlayer, int entityId, int interfaceId, int componentId) {
        if (isPlayer) {
            sendPlayerOnIComponent(interfaceId, componentId);
        } else {
            sendNPCOnIComponent(interfaceId, componentId, entityId);
        }
    }

    public void sendObjectAnimation(WorldObject object, Animation animation) {// updated
        OutputStream stream = new OutputStream(11);
        stream.writePacket(player, 84);
        stream.writeByte((object.getType() << 2) + (object.getRotation() & 0x3));
        stream.writeByte(0);// unknown
        stream.writeIntV1(object.getTileHash());
        stream.writeIntLE(animation.getIds()[0]);
        write(stream);
    }

    public void sendTileMessage(String message, WorldTile tile, int color) {
        sendTileMessage(message, tile, 5000, 255, color);
    }

    public void sendTileMessage(String message, WorldTile tile, int delay, int height, int color) {// updated
        OutputStream stream = createWorldTileStream(tile);
        stream.writePacketVarByte(player, 113);
        stream.writeByte(0);
        int localX = tile.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int localY = tile.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int offsetX = localX - ((localX >> 3) << 3);
        int offsetY = localY - ((localY >> 3) << 3);
        stream.writeByte((offsetX << 4) | offsetY);
        stream.writeShort(delay / 30);
        stream.writeByte(height);
        stream.write24BitInteger(color);
        stream.writeString(message);
        stream.endPacketVarByte();
        write(stream);
    }

    public OutputStream createWorldTileStream(WorldTile tile) {// updated (correct name UPDATE_ZONE_PARTIAL_FOLLOWS)
        OutputStream stream = new OutputStream(4);
        stream.writePacket(player, 56);
        stream.writeByte128(tile.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize()) >> 3);
        stream.writeByte128(tile.getPlane());
        stream.write128Byte(tile.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize()) >> 3);
        return stream;
    }

    public void sendSpawnedObject(WorldObject object) {// updated
        int chunkRotation = World.getRotation(object.getPlane(), object.getX(), object.getY());
        if (chunkRotation == 1) {
            object = new WorldObject(object);
            ObjectDefinitions defs = ObjectDefinitions.getObjectDefinitions(object.getId());
            object.moveLocation(0, -(defs.getSizeY() - 1), 0);
        } else if (chunkRotation == 2) {
            object = new WorldObject(object);
            ObjectDefinitions defs = ObjectDefinitions.getObjectDefinitions(object.getId());
            object.moveLocation(-(defs.getSizeY() - 1), 0, 0);
        }
        OutputStream stream = createWorldTileStream(object);
        int localX = object.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int localY = object.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int offsetX = localX - (localX >> 3 << 3);
        int offsetY = localY - (localY >> 3 << 3);
        stream.writePacketVarByte(player, 139);
        stream.writeByte((object.getType() << 2) + (object.getRotation() & 0x3));
        stream.writeIntV2(object.getId());
        stream.writeByteC((offsetX << 4) | offsetY);
        stream.endPacketVarByte();
        write(stream);
    }

    public void addSpawnedObject(WorldObject object) {// updated
        if (object == null)
            return;
        int chunkRotation = World.getRotation(object.getPlane(), object.getX(), object.getY());
        if (chunkRotation == 1) {
            object = new WorldObject(object);
            ObjectDefinitions defs = ObjectDefinitions.getObjectDefinitions(object.getId());
            object.moveLocation(0, -(defs.getSizeY() - 1), 0);
        } else if (chunkRotation == 2) {
            object = new WorldObject(object);
            ObjectDefinitions defs = ObjectDefinitions.getObjectDefinitions(object.getId());
            object.moveLocation(-(defs.getSizeY() - 1), 0, 0);
        }
        OutputStream stream = createWorldTileStream(object);
        int localX = object.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int localY = object.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int offsetX = localX - (localX >> 3 << 3);
        int offsetY = localY - (localY >> 3 << 3);
        stream.writePacketVarByte(player, 139);
        stream.writeByte((object.getType() << 2) + (object.getRotation() & 0x3));
        stream.writeIntV2(object.getId());
        stream.writeByteC((offsetX << 4) | offsetY);
        val found = player.getPlayerObjectWithType(object.getType(), object.getX(), object.getY(), object.getPlane());
        if (found != null)
            player.removePlayerObject(found);
        player.addPlayerObject(object);
        stream.endPacketVarByte();
        write(stream);
    }

    public void sendDestroyObject(WorldObject object) {// updated
        int chunkRotation = World.getRotation(object.getPlane(), object.getX(), object.getY());
        if (chunkRotation == 1) {
            object = new WorldObject(object);
            ObjectDefinitions defs = ObjectDefinitions.getObjectDefinitions(object.getId());
            object.moveLocation(0, -(defs.getSizeY() - 1), 0);
        } else if (chunkRotation == 2) {
            object = new WorldObject(object);
            ObjectDefinitions defs = ObjectDefinitions.getObjectDefinitions(object.getId());
            object.moveLocation(-(defs.getSizeY() - 1), 0, 0);
        }
        OutputStream stream = createWorldTileStream(object);
        int localX = object.getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int localY = object.getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int offsetX = localX - (localX >> 3 << 3);
        int offsetY = localY - (localY >> 3 << 3);
        stream.writePacket(player, 45);
        stream.writeByte((object.getType() << 2) + (object.getRotation() & 0x3));
        stream.write128Byte((offsetX << 4) | offsetY);
        write(stream);
    }

    public void sendPlayerOnIComponent(int interfaceId, int componentId) {// updated (correct name IF_SETPLAYERHEAD)
        OutputStream stream = new OutputStream(5);
        stream.writePacket(player, 43);
        stream.writeInt(interfaceId << 16 | componentId);
        write(stream);
    }

    public void sendNPCOnIComponent(int interfaceId, int componentId, int npcId) {// updated (correct name IF_SETNPCHEAD)
        OutputStream stream = new OutputStream(10);
        stream.writePacket(player, 189);
        stream.writeIntLE(interfaceId << 16 | componentId);
        stream.writeIntV1(npcId);
        write(stream);

    }

    public void sendRandomOnIComponent(int interfaceId, int componentId, int id) {
        /*
         * OutputStream stream = new OutputStream(); stream.writePacket(player, 235);*
         * stream.writeShort(id); stream.writeIntV1(interfaceId << 16 | componentId);
         * stream.writeShort(interPacketsCount++); //write(stream);
         */
    }

    public void sendFaceOnIComponent(int interfaceId, int componentId, int look1, int look2, int look3) {
        /*
         * OutputStream stream = new OutputStream(); stream.writePacket(player, 192);*
         * stream.writeIntV2(interfaceId << 16 | componentId);
         * stream.writeShortLE128(interPacketsCount++); stream.writeShortLE128(look1);
         * stream.writeShortLE128(look2); stream.writeShort128(look2); //write(stream);
         */
    }

    public void sendFriendsChatChannel() {// updated
        FriendChatsManager manager = player.getCurrentFriendChat();
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 23);
        stream.endPacketVarShort();
        write(stream);
        stream = new OutputStream(manager == null ? 3 : manager.getDataBlock().length + 3);
        stream.writePacketVarShort(player, 23);
        if (manager != null) {
            stream.writeBytes(manager.getDataBlock());
        }
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendFriends() {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 46);
        for (String username : player.getFriendsIgnores().getFriends()) {
            String displayName;
            Player p2 = World.getPlayerByDisplayName(username);
            if (p2 != null) {
                displayName = p2.getDisplayName();
            } else {
                displayName = Utils.formatPlayerNameForDisplay(username);
            }
            player.getPackets().sendFriend(Utils.formatPlayerNameForDisplay(username), displayName, 1, p2 != null && player.getFriendsIgnores().isOnline(p2), false, stream);
        }
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendFriend(String username, String displayName, int world, boolean putOnline, boolean warnMessage) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 46);
        sendFriend(username, displayName, world, putOnline, warnMessage, stream);
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendFriend(String username, String displayName, int world, boolean putOnline, boolean warnMessage, OutputStream stream) {// updated
        stream.writeByte(warnMessage ? 0 : 1);
        stream.writeString(displayName);
        stream.writeString(displayName.equals(username) ? "" : username);
        stream.writeShort(putOnline ? world : 0);
        stream.writeByte(player.getFriendsIgnores().getRank(Utils.formatPlayerNameForProtocol(username)));
        stream.writeByte(0);
        if (putOnline) {
            stream.writeString(Settings.SERVER_NAME);
            stream.writeByte(0);
            stream.writeInt(0);
        }
        stream.writeString("");// note
    }

    public void sendIgnores() {
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 174);
        for (String username : player.getFriendsIgnores().getIgnores()) {
            String display;
            Player p2 = World.getPlayerByDisplayName(username);
            if (p2 != null) {
                display = p2.getDisplayName();
            } else {
                display = Utils.formatPlayerNameForDisplay(username);
            }
            String name = Utils.formatPlayerNameForDisplay(username);
            stream.writeByte(0x0);
            stream.writeString(display.equals(name) ? name : display);
            stream.writeString(display.equals(name) ? "" : name);
            stream.writeString("");
        }
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendIgnore(String name, String display, boolean updateName) {
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 174);
        stream.writeByte(0x2);
        stream.writeString(display.equals(name) ? name : display);
        stream.writeString(display.equals(name) ? "" : name);
        stream.writeString("");
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendPrivateMessage(String username, String message) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 7);
        stream.writeString(username);
        Huffman.encodeString(stream, message);
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendGameBarStages() {
        boolean isGameOn = player.getGameStatus() != 2;
        player.getPackets().sendConfigByFile(18797, isGameOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18805, player.getGameStatus());
        if (player.getOtherChatsGameStatus() == null)
            player.toggleOtherChatsGameStatus(0, 0);
        boolean isPrivGameOn = player.getOtherChatsGameStatus()[0] != 2;
        player.getPackets().sendConfigByFile(18812, isPrivGameOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18820, player.getOtherChatsGameStatus()[0]);
        boolean isFriendsGameOn = player.getOtherChatsGameStatus()[1] != 2;
        player.getPackets().sendConfigByFile(18827, isFriendsGameOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18835, player.getOtherChatsGameStatus()[1]);
        boolean isClanGameOn = player.getOtherChatsGameStatus()[2] != 2;
        player.getPackets().sendConfigByFile(18842, isClanGameOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18850, player.getOtherChatsGameStatus()[2]);
        boolean isClanGuestGameOn = player.getOtherChatsGameStatus()[3] != 2;
        player.getPackets().sendConfigByFile(18857, isClanGuestGameOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18865, player.getOtherChatsGameStatus()[3]);
        boolean isTradeGameOn = player.getOtherChatsGameStatus()[4] != 2;
        player.getPackets().sendConfigByFile(20813, isTradeGameOn ? 1 : 0);
        player.getPackets().sendConfigByFile(20821, player.getOtherChatsGameStatus()[4]);
        boolean isGroupGameOn = player.getOtherChatsGameStatus()[5] != 2;
        player.getPackets().sendConfigByFile(24563, isGroupGameOn ? 1 : 0);
        player.getPackets().sendConfigByFile(24571, player.getOtherChatsGameStatus()[5]);




        boolean isTradeOn = player.getTradeStatus() != 2;
        player.getPackets().sendConfigByFile(18798, isTradeOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18806, player.getTradeStatus());
        player.getPackets().sendConfigByFile(18813, isTradeOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18821, player.getTradeStatus());
        player.getPackets().sendConfigByFile(18828, isTradeOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18836, player.getTradeStatus());
        player.getPackets().sendConfigByFile(18843, isTradeOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18851, player.getTradeStatus());
        player.getPackets().sendConfigByFile(18858, isTradeOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18866, player.getTradeStatus());
        player.getPackets().sendConfigByFile(20814, isTradeOn ? 1 : 0);
        player.getPackets().sendConfigByFile(20822, player.getTradeStatus());
        player.getPackets().sendConfigByFile(24564, isTradeOn ? 1 : 0);
        player.getPackets().sendConfigByFile(24572, player.getTradeStatus());

        boolean isAssistOn = player.getAssistStatus() != 2;
        player.getPackets().sendConfigByFile(18799, isAssistOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18807, player.getAssistStatus());
        player.getPackets().sendConfigByFile(18814, isAssistOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18822, player.getAssistStatus());
        player.getPackets().sendConfigByFile(18829, isAssistOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18837, player.getAssistStatus());
        player.getPackets().sendConfigByFile(18844, isAssistOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18852, player.getAssistStatus());
        player.getPackets().sendConfigByFile(18859, isAssistOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18867, player.getAssistStatus());
        player.getPackets().sendConfigByFile(20815, isAssistOn ? 1 : 0);
        player.getPackets().sendConfigByFile(20823, player.getAssistStatus());
        player.getPackets().sendConfigByFile(24565, isAssistOn ? 1 : 0);
        player.getPackets().sendConfigByFile(24573, player.getAssistStatus());

        boolean isPersonalOn = player.getFriendsIgnores().getPrivateStatus() != 2;
        player.getPackets().sendConfigByFile(18801, isPersonalOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18809, player.getFriendsIgnores().getPrivateStatus());
        player.getPackets().sendConfigByFile(18816, 1);
        player.getPackets().sendConfigByFile(18824, player.getFriendsIgnores().getPrivateStatus() >= 2 ? 1 : player.getFriendsIgnores().getPrivateStatus());
        player.getPackets().sendConfigByFile(18831, isPersonalOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18839, player.getFriendsIgnores().getPrivateStatus());
        player.getPackets().sendConfigByFile(18846, isPersonalOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18854, player.getFriendsIgnores().getPrivateStatus());
        player.getPackets().sendConfigByFile(18861, isPersonalOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18869, player.getFriendsIgnores().getPrivateStatus());
        player.getPackets().sendConfigByFile(20817, isPersonalOn ? 1 : 0);
        player.getPackets().sendConfigByFile(20825, player.getFriendsIgnores().getPrivateStatus());
        player.getPackets().sendConfigByFile(24567, isPersonalOn ? 1 : 0);
        player.getPackets().sendConfigByFile(24575, player.getFriendsIgnores().getPrivateStatus());


        boolean isFriendsOn = player.getFriendsIgnores().getFriendsChatStatus() != 2;
        player.getPackets().sendConfigByFile(18802, isFriendsOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18810, player.getFriendsIgnores().getFriendsChatStatus());
        player.getPackets().sendConfigByFile(18817, isFriendsOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18825, player.getFriendsIgnores().getFriendsChatStatus());
        player.getPackets().sendConfigByFile(18832, 1);
        player.getPackets().sendConfigByFile(18840, player.getFriendsIgnores().getFriendsChatStatus() >= 2 ? 1 : player.getFriendsIgnores().getFriendsChatStatus());
        player.getPackets().sendConfigByFile(18847, isFriendsOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18855, player.getFriendsIgnores().getFriendsChatStatus());
        player.getPackets().sendConfigByFile(18862, isFriendsOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18870, player.getFriendsIgnores().getFriendsChatStatus());
        player.getPackets().sendConfigByFile(20818, isFriendsOn ? 1 : 0);
        player.getPackets().sendConfigByFile(20826, player.getFriendsIgnores().getFriendsChatStatus());
        player.getPackets().sendConfigByFile(24568, isFriendsOn ? 1 : 0);
        player.getPackets().sendConfigByFile(24576, player.getFriendsIgnores().getFriendsChatStatus());


        boolean isClanOn = player.getClanStatus() != 2;
        boolean isGuestClanOn = player.getGuestClanStatus() != 2;
        player.getPackets().sendConfigByFile(18803, isClanOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18804, isGuestClanOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18811, player.getClanStatus());
        player.getPackets().sendConfigByFile(18818, isClanOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18819, isGuestClanOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18826, player.getClanStatus());
        player.getPackets().sendConfigByFile(18833, isClanOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18834, isGuestClanOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18841, player.getClanStatus());
        player.getPackets().sendConfigByFile(18848, 1);
        player.getPackets().sendConfigByFile(18849, 1);
        player.getPackets().sendConfigByFile(18856, player.getClanStatus());
        player.getPackets().sendConfigByFile(18863, isClanOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18864, isGuestClanOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18871, player.getClanStatus());
        player.getPackets().sendConfigByFile(20819, isClanOn ? 1 : 0);
        player.getPackets().sendConfigByFile(20820, isGuestClanOn ? 1 : 0);
        player.getPackets().sendConfigByFile(20827, player.getClanStatus());
        player.getPackets().sendConfigByFile(24569, isClanOn ? 1 : 0);
        player.getPackets().sendConfigByFile(24570, isGuestClanOn ? 1 : 0);
        player.getPackets().sendConfigByFile(24577, player.getClanStatus());

        boolean isBadgeOn = player.getChatBadgeStatus() != 1;
        player.getPackets().sendConfigByFile(21020, isBadgeOn ? 0 : 1);

        boolean hideLocalChat = player.getLocalChatStatus() == 3;
        player.getPackets().sendConfigByFile(36983, hideLocalChat ? 1 : 0);
        boolean isLocalChatOn = player.getLocalChatStatus() != 2;
        player.getPackets().sendConfigByFile(18800, isLocalChatOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18808, player.getLocalChatStatus());
        player.getPackets().sendConfigByFile(18815, isLocalChatOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18823, player.getLocalChatStatus());
        player.getPackets().sendConfigByFile(18830, isLocalChatOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18838, player.getLocalChatStatus());
        player.getPackets().sendConfigByFile(18845, isLocalChatOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18853, player.getLocalChatStatus());
        player.getPackets().sendConfigByFile(18860, isLocalChatOn ? 1 : 0);
        player.getPackets().sendConfigByFile(18868, player.getLocalChatStatus());
        player.getPackets().sendConfigByFile(20816, isLocalChatOn ? 1 : 0);
        player.getPackets().sendConfigByFile(20824, player.getLocalChatStatus());
        player.getPackets().sendConfigByFile(24566, isLocalChatOn ? 1 : 0);
        player.getPackets().sendConfigByFile(24574, player.getLocalChatStatus());

        boolean isGroupChatOn = player.getGroupStatus() != 2;

        player.getPackets().sendGlobalConfig(4505, player.getGroupStatus());
        player.getPackets().sendConfigByFile(24578, isGroupChatOn ? 1 : 0);
        player.getPackets().sendConfigByFile(24586, player.getGroupStatus());
        player.getPackets().sendConfigByFile(24579, isGroupChatOn ? 1 : 0);
        player.getPackets().sendConfigByFile(24587, player.getGroupStatus());
        player.getPackets().sendConfigByFile(24580, isGroupChatOn ? 1 : 0);
        player.getPackets().sendConfigByFile(24588, player.getGroupStatus());
        player.getPackets().sendConfigByFile(24581, isGroupChatOn ? 1 : 0);
        player.getPackets().sendConfigByFile(24589, player.getGroupStatus());
        player.getPackets().sendConfigByFile(24582, isGroupChatOn ? 1 : 0);
        player.getPackets().sendConfigByFile(24590, player.getGroupStatus());
        player.getPackets().sendConfigByFile(24583, isGroupChatOn ? 1 : 0);
        player.getPackets().sendConfigByFile(24591, player.getGroupStatus());
        player.getPackets().sendConfigByFile(24585, 1);
        player.getPackets().sendConfigByFile(24592, player.getGroupStatus());

        player.getPackets().sendConfigByFile(20828, player.isBroadCastMessages() ? 1 : 0);
        player.getPackets().sendConfigByFile(20829, player.isBroadCastMessages() ? 1 : 0);
        player.getPackets().sendConfigByFile(20830, player.isBroadCastMessages() ? 1 : 0);
        player.getPackets().sendConfigByFile(20831, player.isBroadCastMessages() ? 1 : 0);
        player.getPackets().sendConfigByFile(20832, player.isBroadCastMessages() ? 1 : 0);
        player.getPackets().sendConfigByFile(20833, player.isBroadCastMessages() ? 1 : 0);
        player.getPackets().sendConfigByFile(24593, player.isBroadCastMessages() ? 1 : 0);

        sendOtherGameBarStages();
        sendPrivateGameBarStage();
    }

    public void sendOtherGameBarStages() {// updated
        OutputStream stream = new OutputStream(4);
        stream.writePacket(player, 138);
        stream.writeByteC(player.getTradeStatus());
        stream.writeByte(player.getPublicStatus());
        write(stream);
    }

    public void sendPrivateGameBarStage() {// updated
        OutputStream stream = new OutputStream(2);
        stream.writePacket(player, 74);
        stream.writeByte(player.getFriendsIgnores().getPrivateStatus());
        write(stream);
    }

    public void receivePrivateMessage(String name, String display, int rights, String message) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 90);
        stream.writeByte(name.equals(display) ? 0 : 1);
        stream.writeString(display);
        if (!name.equals(display)) {
            stream.writeString(name);
        }
        for (int i = 0; i < 5; i++) {
            stream.writeByte(Utils.getRandom(255));
        }
        stream.writeByte(rights);
        Huffman.encodeString(stream, message);
        stream.endPacketVarShort();
        write(stream);
    }

    // 131 clan chat quick message

    public void receivePrivateChatQuickMessage(String name, String display, int rights, QuickChatMessage message) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarByte(player, 80);
        stream.writeByte(name.equals(display) ? 0 : 1);
        stream.writeString(display);
        if (!name.equals(display)) {
            stream.writeString(name);
        }
        for (int i = 0; i < 5; i++) {
            stream.writeByte(Utils.getRandom(255));
        }
        stream.writeByte(rights);
        stream.writeShort(message.getFileId());
        if (message.getMessage() != null) {
            stream.writeBytes(message.getMessage().getBytes());
        }
        stream.endPacketVarByte();
        write(stream);
    }

    public void sendPrivateQuickMessageMessage(String username, QuickChatMessage message) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarByte(player, 93);
        stream.writeString(username);
        stream.writeShort(message.getFileId());
        if (message.getMessage() != null) {
            stream.writeBytes(message.getMessage().getBytes());
        }
        stream.endPacketVarByte();
        write(stream);
    }

    public void receiveFriendChatMessage(String name, String display, int rights, String chatName, String message) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarByte(player, 36);
        stream.writeByte(name.equals(display) ? 0 : 1);
        stream.writeString(display);
        if (!name.equals(display)) {
            stream.writeString(name);
        }
        stream.writeString(chatName);
        for (int i = 0; i < 5; i++) {
            stream.writeByte(Utils.getRandom(255));
        }
        stream.writeByte(rights);
        Huffman.encodeString(stream, message);
        stream.endPacketVarByte();
        write(stream);
    }

    public void receiveFriendChatQuickMessage(String name, String display, int rights, String chatName, QuickChatMessage message) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarByte(player, 145);
        stream.writeByte(name.equals(display) ? 0 : 1);
        stream.writeString(display);
        if (!name.equals(display))
            stream.writeString(name);
        stream.writeString(chatName);
        for (int i = 0; i < 5; i++)
            stream.writeByte(Utils.getRandom(255));
        Player p = World.getPlayer(name);
        stream.writeByte(p != null && p.isOwner() ? 2 : rights == 1 ? 1 : 0);
        stream.writeShort(message.getFileId());
        if (message.getMessage() != null) {
            stream.writeBytes(message.getMessage().getBytes());
        }
        stream.endPacketVarByte();
        write(stream);
    }

    /*
     * useless, sending friends unlocks it
     */
    public void sendUnlockIgnoreList() {
//        OutputStream stream = new OutputStream(1);
//        stream.writePacket(player, 18);
//        write(stream);
    }

    /*
     * dynamic map region
     */
    public void sendDynamicMapRegion(boolean sendLswp) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 9);
        if (sendLswp)
            player.getLocalPlayerUpdate().init(stream);
        int chunkX = player.getChunkX();
        int chunkY = player.getChunkY();
        stream.writeByteC(player.isUsingNXT() ? 1 : 2);
        stream.writeByte(player.getNPCViewDistanceBits());
        stream.writeByte128(player.getMapSize());
        stream.writeShortLE128(chunkX);
        stream.writeShortLE128(chunkY);
        stream.writeByteC(player.isForceNextMapLoadRefresh() ? 1 : 0);
        if (player.isUsingNXT()) {
            DynamicArea area = DynamicArea.getDynamicArea(player.getRegionId());
            stream.writeShort(area.getBaseChunks()[0]);
            stream.writeShort(area.getBaseChunks()[1]);
            int width = area.getWidthHeight()[0];
            int heigth = area.getWidthHeight()[1];
            stream.writeByte(width);
            stream.writeByte(heigth);
            stream.initBitAccess();
            for (int plane = 0; plane < 4; plane++) {
                for (int offsetX = 0; offsetX < width; offsetX++) {
                    for (int offsetY = 0; offsetY < heigth; offsetY++) {
                        int regionX = (area.getBaseChunks()[0] + offsetX) / 8;
                        int regionY = (area.getBaseChunks()[1] + offsetY) / 8;
                        int regionId = (regionX << 8) + regionY;
                        Region region = World.getRegion(regionId);
                        if (region instanceof DynamicRegion) { // generated map
                            DynamicRegion dynamicRegion = (DynamicRegion) region;
                            int[] pallete = dynamicRegion.getRegionCoords()[plane][offsetX % 8][offsetY % 8];
                            int newChunkX = pallete[0];
                            int newChunkY = pallete[1];
                            int newPlane = pallete[2];
                            int rotation = pallete[3];
                            if (newChunkX == 0 || newChunkY == 0)
                                stream.writeBits(1, 0);
                            else {
                                stream.writeBits(1, 1);
                                stream.writeBits(26, (rotation << 1) | (newPlane << 24) | (newChunkX << 14) | (newChunkY << 3));
                            }
                        } else
                            stream.writeBits(1, 0);
                    }
                }
            }
            stream.finishBitAccess();
            stream.endPacketVarShort();
            write(stream);
            return;
        }
        stream.initBitAccess();
        int mapHash = Settings.MAP_SIZES[player.getMapSize()] >> 4;
        for (int plane = 0; plane < 4; plane++) {
            for (int thisRegionX = chunkX - mapHash; thisRegionX <= chunkX + mapHash; thisRegionX++) { // calcs
                for (int thisRegionY = chunkY - mapHash; thisRegionY <= chunkY + mapHash; thisRegionY++) { // calcs
                    int regionId = (thisRegionX / 8 << 8) + thisRegionY / 8;
                    Region region = World.getRegions().get(regionId);
                    int realRegionX;
                    int realRegionY;
                    int realPlane;
                    int rotation;
                    if (region instanceof DynamicRegion) { // generated map
                        DynamicRegion dynamicRegion = (DynamicRegion) region;
                        int[] regionCoords = dynamicRegion.getRegionCoords()[plane][thisRegionX - thisRegionX / 8 * 8][thisRegionY - thisRegionY / 8 * 8];
                        realRegionX = regionCoords[0];
                        realRegionY = regionCoords[1];
                        realPlane = regionCoords[2];
                        rotation = regionCoords[3];
                    } else { // real map
                        // base region + difference * 8 so gets real region
                        // coords
                        realRegionX = thisRegionX;
                        realRegionY = thisRegionY;
                        realPlane = plane;
                        rotation = 0;// no rotation
                    }
                    // invalid region, not built region
                    if (realRegionX == 0 || realRegionY == 0) {
                        stream.writeBits(1, 0);
                    } else {
                        stream.writeBits(1, 1);
                        stream.writeBits(26, rotation << 1 | realPlane << 24 | realRegionX << 14 | realRegionY << 3);
                    }

                }
            }
        }
        stream.finishBitAccess();
        stream.endPacketVarShort();
        write(stream);
    }

    /*
     * normal map region
     */
    public void sendMapRegion() {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 88);
        if (!player.isActive()) {
            player.getLocalPlayerUpdate().init(stream);
        }
        stream.writeByteC(player.isForceNextMapLoadRefresh() ? 1 : 0);
        stream.writeShort(player.getChunkX());
        stream.writeByte128(player.getMapRegionsIds().size());
        stream.writeShort(player.getChunkY());
        stream.writeByte128(player.getNPCViewDistanceBits());
        stream.writeByte(player.getMapSize());
        stream.writeShort(player.getRegionKey());
        stream.writeShort(Short.MIN_VALUE);
        stream.writeShort(Short.MIN_VALUE);
        stream.writeShort(Short.MAX_VALUE);
        stream.writeShort(Short.MAX_VALUE);
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendCutscene(int id) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 136);
        stream.writeShort(id);
        stream.writeShort(20);
        byte[] appearence = player.getAppearence().getAppeareanceData();
        stream.writeByte(appearence.length);
        stream.writeBytes(appearence);
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendPlayerOption(String option, int slot, boolean top) {
        sendPlayerOption(option, slot, top, -1);
    }

    public void sendPublicMessage(Player p, PublicChatMessage message) {
        sendPublicMessage( p,  message, -1);
    }

    public void sendPublicMessage(Player p, PublicChatMessage message, int customIcon) {// updated

        if (message.getMessage().contains("^^^")) //this is a crash? or causing one..
            return;

        char[] crashStrings = { '<', '>', '=', '+', '6', '7', '8', '9', '\\', '/', '%', '{', '}', '^', '&', '*', '(', ')', '_', '-', '~', ',', '.', '?', '\"', '$', '@', '"', '!'  };

        int totalCrashStrings = 0;
        //loops through each one of these characters
        for (char character : crashStrings) {
            int charNum = 0;

            //loops through each character in the string message
            for (char chaString : message.getMessage().toCharArray()) {

                if (chaString == character) {//checks if the current cha array index equals the crashString char.
                    charNum++;
                    totalCrashStrings++;
                }
            }

            if (charNum > 25)// Prevents >=25 of these from being added.
                return;

        }

        //Prevents multiple type of char strings from bypassing the system detection. No more than 25 of any of these chars cannot be parsed.
        if (totalCrashStrings > 25) return;

        OutputStream stream = new OutputStream();
        stream.writePacketVarByte(player, 8);
        stream.writeShort(p.getIndex());
        stream.writeShort(message.getEffects());
        stream.writeByte(customIcon == -1 ? p.getMessageIcon() >= 8 ? p.getMessageIcon() - 5 : p.getMessageIcon() : customIcon);
        if (message instanceof QuickChatMessage) {
            QuickChatMessage qcMessage = (QuickChatMessage) message;
            stream.writeShort(qcMessage.getFileId());
            if (qcMessage.getMessage() != null) {
                stream.writeBytes(message.getMessage().getBytes());
            }
        } else {
            byte[] chatStr = new byte[250];
            chatStr[0] = (byte) message.getMessage().length();
            int offset = 1 + Huffman.encodeString(stream, message.getMessage());
            stream.writeBytes(chatStr, 0, offset);
        }
        stream.endPacketVarByte();
        write(stream);
    }

    public void sendPlayerOption(String option, int slot, boolean top, int cursor) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarByte(player, 14);
        stream.writeByte128(top ? 1 : 0);
        stream.writeString(option);
        stream.write128Byte(slot);
        stream.writeShort(cursor);
        stream.endPacketVarByte();
        write(stream);
    }

    /*
     * sends local players update
     */
    public void sendLocalPlayersUpdate() {
        write(player.getLocalPlayerUpdate().createPacketAndProcess());
    }

    /*
     * sends local npcs update
     */
    public void sendLocalNPCsUpdate() {
        write(player.getLocalNPCUpdate().createPacketAndProcess());
    }

    public void sendSlayerMasterUpdate(NPC n, int id) {
        sendGraphics(new Graphics(188), n);
        write(player.getLocalNPCUpdate().updateSlayerMasters(n, id));
    }

    public void sendGraphics(Graphics graphics, Object target) {// updated
        OutputStream stream = new OutputStream(14);
        int hash = 0;
        if (target instanceof Player) {
            Player p = (Player) target;
            hash = p.getIndex() & 0xffff | 1 << 28;
        } else if (target instanceof NPC) {
            NPC n = (NPC) target;
            hash = n.getIndex() & 0xffff | 1 << 29;
        } else {
            WorldTile tile = (WorldTile) target;
            hash = tile.getPlane() << 28 | tile.getX() << 14 | tile.getY() & 0x3fff | 1 << 30;
        }
        stream.writePacket(player, 178);
        stream.write128Byte(1); // slot id used for entitys
        stream.writeShortLE(graphics.getId());
        stream.writeByte(graphics.getSettings2Hash());
        stream.writeShort128(graphics.getHeight());
        stream.writeIntV1(hash);
        stream.writeShortLE128(graphics.getSpeed());
        write(stream);
    }

    public void closeInterface(int parentUID) {// updated
        OutputStream stream = new OutputStream(6);
        stream.writePacket(player, 166);
        stream.writeIntV1(parentUID);
        write(stream);
    }

    public void closeInterface(int windowId, int windowComponentId) {// updated
        OutputStream stream = new OutputStream(6);
        stream.writePacket(player, 166);
        stream.writeIntV1(windowId << 16 | windowComponentId);
        write(stream);
    }

    public void sendSystemUpdate(int delay) {// updated
        OutputStream stream = new OutputStream(4);
        stream.writePacket(player, 177);
        stream.writeShort((int) (delay * 1.6));
        write(stream);
    }

    public void sendUpdateItems(int key, ItemsContainer<Item> items, int... slots) {
        sendUpdateItems(key, items.getItems(), slots);
    }

    public void sendUpdateItems(int key, Item[] items, int... slots) {
        sendUpdateItems(key, key < 0, items, slots);
    }

    public void sendUpdateItems(int key, boolean negativeKey, Item[] items, int... slots) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 16);
        stream.writeShort(key);
        int mask = 0x0;
        if (negativeKey) {
            mask |= 0x1;
        }
        mask |= 0x2;
        stream.writeByte(mask);
        for (int slotId : slots) {
            if (slotId >= items.length) {
                continue;
            }
            stream.writeSmart(slotId);
            int id = -1;
            int amount = 0;
            Item item = items[slotId];
            if (item != null) {
                id = item.getId();
                amount = item.getAmount();
            }
            stream.writeShort(id + 1);
            if (id != -1) {
                stream.writeByte(amount >= 255 ? 255 : amount);
                if (amount >= 255) {
                    stream.writeInt(amount);
                }
                if (item != null)
                    item.initVarsManager(player);
                int length = item == null ? 0 : item.getVarsManager().getVarsLength();
                stream.writeByte(length);
                if (length > 0) {
                    while (length-- > 0) {
                        int nisVarId = item.getVarsManager().getVarId(length);
                        Object value = item.getVarsManager().getValue(nisVarId);
                        stream.writeShort(nisVarId);
                        if (value instanceof Integer)
                            stream.writeInt((int) value);
                        else if (value instanceof Long)
                            stream.writeLong((long) value);
                        else if (value instanceof String)
                            stream.writeVersionedString((String) value);
                        else {

                        }
                    }
                }
//                writeDyeData(stream, item);
            }
        }
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendGlobalString(int id, String string) {// updated
        if (string == null) {
            string = "";
        }
        OutputStream stream = new OutputStream();
        if (string.length() > 253) {
            stream.writePacketVarShort(player, 21);
            stream.writeString(string);
            stream.writeShortLE128(id);
            stream.endPacketVarShort();
        } else {
            stream.writePacketVarByte(player, 30);
            stream.writeShort128(id);
            stream.writeString(string);
            stream.endPacketVarByte();
        }
        write(stream);
    }

    public void sendCSVarInteger(int id, int value) {
        sendGlobalConfig(id, value);
    }

    public void sendCSVarString(int id, String string) {
        sendGlobalString(id, string);
    }

    public void sendItems(int key, ItemsContainer<Item> items) {
        sendItems(key, key < 0, items);
    }

    public void sendItems(int key, boolean negativeKey, ItemsContainer<Item> items) {
        sendItems(key, negativeKey, items.getItems());
    }

    public void sendItems(int key, Item[] items) {
        sendItems(key, key < 0, items);
    }

    public void resetItems(int key, boolean negativeKey, int size) {
        sendItems(key, negativeKey, new Item[size]);
    }

    public static void sendItemsFull(Player player, int interfaceId, int key, int component, Item... items) {
        if (items == null)
            return;
        player.getPackets().sendItems(key, false, items);
        player.getPackets().sendInterSetItemsOptionsScript(interfaceId, component, key, 4, 7, "Examine");
        player.getPackets().sendUnlockIComponentOptionSlots(interfaceId, component, 0, 160, 0);

    }

    public static void sendItemsFull(Player player, int interfaceId, int key, int component, int width, int height, int... items) {
        if (items == null)
            return;
        player.getPackets().sendItems(key, false, items);
        player.getPackets().sendInterSetItemsOptionsScript(interfaceId, component, key, width, height, "Examine");
        player.getPackets().sendUnlockIComponentOptionSlots(interfaceId, component, 0, 160, 0);

    }

    public void sendItems(int key, boolean negativeKey, int... items) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 6);
        stream.writeShort(key); // negativeKey ? -key : key
        stream.writeByte(negativeKey ? 1 : 0);
        stream.writeShort(items.length);
        for (int index = 0; index < items.length; index++) {
            int id = items[index];
            int amount = 1;
            stream.writeShort(id + 1);
            stream.writeByte(amount >= 255 ? 255 : amount);
            if (amount >= 255) {
                stream.writeInt(amount);
            }
//            writeDyeData(stream, null);
        }
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendItems(int key, boolean negativeKey, Item[] items) {// updated
        OutputStream stream = new OutputStream();
        int mask = 0x0;
        if (negativeKey) {
            mask |= 0x1;
        }
        mask |= 0x2;
        stream.writePacketVarShort(player, 6);
        stream.writeShort(key); // negativeKey ? -key : key
        stream.writeByte(mask);
        stream.writeShort(items.length);
        for (int index = 0; index < items.length; index++) {
            Item item = items[index];
            int id = -1;
            int amount = 0;
            if (item != null) {
                id = item.getId();
                amount = item.getAmount();
            }
            stream.writeShort(id + 1);
            stream.writeByte(amount >= 255 ? 255 : amount);
            if (amount >= 255) {
                stream.writeInt(amount);
            }
            if (item != null)
                item.initVarsManager(player);
            int length = item == null ? 0 : item.getVarsManager().getVarsLength();
            stream.writeByte(length);
            if (length > 0) {
                while (length-- > 0) {
                    int nisVarId = item.getVarsManager().getVarId(length);
                    Object value = item.getVarsManager().getValue(nisVarId);
                    stream.writeShort(nisVarId);
                    if (value instanceof Integer)
                        stream.writeInt((int) value);
                    else if (value instanceof Long)
                        stream.writeLong((long) value);
                    else if (value instanceof String)
                        stream.writeVersionedString((String) value);
                    else {

                    }
                }
            }
//            writeDyeData(stream, item);
        }
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendNPCMessage(int border, NPC npc, String message) {
        sendGameMessage(message);
    }

    public void sendGameMessage(String text) {
        sendGameMessage(text, false);
    }

    public void sendGameMessage(String text, boolean filter) {
        sendMessage(filter ? 109 : 0, text, null);
    }

    public void sendPanelBoxMessage(String text) {
        sendMessage(player.getRights() > 0 ? 99 : 0, text, null);
    }

    public void sendConsoleMessage(String text) {
        sendMessage(99, text, null);
    }

    public void sendDebugMessageToConsole(String text) {
        sendMessage(99, text, null);
    }

    public void sendTradeRequestMessage(Player p) {
        sendMessage(100, "wishes to trade with you.", p);
    }

    public void sendGambleRequestMessage(Player p, int value) {
        sendMessage(100, "wishes to gamble " + NumberFormat.getNumberInstance(Locale.US).format(value) + " GP against you.", p);
    }

    public void sendClanWarsRequestMessage(Player p) {
        sendMessage(101, "wishes to challenge your clan to a clan war.", p);
    }

    public void sendDuelChallengeRequestMessage(Player p, boolean friendly) {
        sendMessage(101, "wishes to duel with you(" + (friendly ? "friendly" : "stake") + ").", p);
    }

    public void sendMessage(int type, String text, Player p) {// updated
        if (!player.hasCompleted()) {
            return;
        }
        int maskData = 0;
        if (p != null) {
            maskData |= 0x1;
            maskData |= 0x2;
        }
        OutputStream stream = new OutputStream();
        stream.writePacketVarByte(player, 120);
        stream.writeSmart(type);
        stream.writeInt(player.getTileHash()); // junk, not used by client
        stream.writeByte(maskData);
        if ((maskData & 0x1) != 0) {
            stream.writeString(Utils.formatPlayerNameForDisplay(p.getDisplayName()));
            stream.writeString(p.getDisplayName());
        }
        stream.writeString(text);
        stream.endPacketVarByte();
        write(stream);
    }

    // effect type 1 or 2(index4 or index14 format, index15 format unusused by
    // jagex for now)
    public void sendSound(int id, int delay, int effectType) {
        if (effectType == 1) {
            sendVorbisSound(id, delay);
        } else if (effectType == 2) {
            sendVorbisSpeachSound(id, delay);
        }
    }

    public void sendVoice(int id) {
        resetSounds();
        sendSound(id, 0, 2);
    }

    public void resetSounds() {// updated
        OutputStream stream = new OutputStream(1);
        stream.writePacket(player, 190);
        write(stream);
    }

    public void sendVorbisSound(int id, int delay) {// updated
        sendVorbisSound(id, delay, 255);
    }

    public void sendVorbisSound(int id, int delay, int volume) {// updated
        OutputStream stream = new OutputStream(9);
        stream.writePacket(player, 37);
        stream.writeShort(id);
        stream.writeByte(1);// repeated amount
        stream.writeShort(delay);
        stream.writeByte(clampAudioVolume(volume));
        stream.writeShort(256);
        write(stream);
    }

    public void sendAudioVolumeBootstrap() {
        sendRunScript(1764, 12451857, 12451853, 20, 0);
        sendConfigByFile(38837, 0);
        for (int i = 0; i < 4; i++) {
            sendConfigByFile(38838 + i, 0);
        }
    }

    public boolean sendNxtVorbisAudioFallback(int id, int delay, int volume, boolean report) {
        if (id < 0) {
            resetSounds();
            return true;
        }
        if (!player.isUsingNXT()) {
            if (report) {
                player.sendMessage("Not sending NXT Vorbis fallback " + id + ": this session is not marked as NXT.");
            }
            return false;
        }
        if (!canSendVorbisSoundArchive(id)) {
            if (report) {
                player.sendMessage("Not sending NXT Vorbis fallback " + id + ": cache index "
                        + VORBIS_SOUND_INDEX + " has no readable archive data.");
            }
            return false;
        }
        sendAudioVolumeBootstrap();
        sendVorbisSound(id, delay, volume);
        if (report) {
            player.sendMessage("Sent NXT-safe Vorbis audio fallback id=" + id + ", delay=" + delay
                    + ", volume=" + clampAudioVolume(volume) + ".");
        }
        return true;
    }

    public void sendVorbisSpeachSound(int id, int delay) {// updated
        OutputStream stream = new OutputStream(8);
        stream.writePacket(player, 143);
        stream.writeShort(id);
        stream.writeByte(1); // amt of times it repeats
        stream.writeShort(delay);
        stream.writeByte(255); // volume
        write(stream);
    }

    public void sendMusicEffect(int id) {
        if (!MUSIC_PACKETS_ENABLED) {
            return;
        }
        sendMusicEffectTest(id);
    }

    public void sendMusicEffectTest(int id) {
        if (!canSendLegacyMusicEffectArchive(id)) {
            player.sendMessage("Not sending legacy music effect archive " + id + ": cache index "
                    + LEGACY_MUSIC_EFFECT_INDEX + " has no readable archive data.");
            return;
        }
        OutputStream stream = new OutputStream(7);
        stream.writePacket(player, 9);
        stream.write128Byte(255); // volume
        stream.write24BitIntegerV2(0);
        stream.writeShort(id);
        write(stream);
    }

    public void sendMusic(int id) {
        sendMusic(id, 100, 255);
    }

    public void sendMusic(int id, int delay, int volume) {
        if (!MUSIC_PACKETS_ENABLED) {
            return;
        }
        sendMusicTest(id, delay, volume);
    }

    public void sendMusicTest(int id, int delay, int volume) {
        if (!canSendLegacyMusicArchive(id)) {
            player.sendMessage("Not sending legacy music archive " + id + ": cache index " + LEGACY_MUSIC_INDEX
                    + " has no readable archive data.");
            return;
        }
        OutputStream stream = new OutputStream(5);
        stream.writePacket(player, MIDI_SONG_PACKET);
        stream.writeByte(delay);
        stream.writeShortLE128(id);
        stream.writeByte128(volume);
        write(stream);
    }

    public void sendNxtMusicTest(int id, int volume) {
        if (!NXT_MUSIC_TEST_PACKETS_ENABLED) {
            player.sendMessage("NXT music test packets are disabled: opcode 70 is not valid for this NXT client.");
            return;
        }
        if (!player.isUsingNXT()) {
            player.sendMessage("Not sending NXT music archive " + id + ": this session is not marked as NXT.");
            return;
        }
        if (!canSendNxtMusicArchive(id)) {
            player.sendMessage("Not sending NXT music archive " + id + ": cache index " + Cache.RS3_MUSIC_INDEX
                    + " has no readable archive data.");
            return;
        }
        OutputStream stream = new OutputStream(4);
        stream.writePacket(player, 70);
        stream.writeByte128(volume);
        stream.writeShort128(id);
        write(stream);
    }

    public void sendNxtMusicEffectTest(int id) {
        if (!NXT_MUSIC_TEST_PACKETS_ENABLED) {
            player.sendMessage("NXT music effect test packets are disabled: opcode 9 is not verified for this NXT client.");
            return;
        }
        if (!player.isUsingNXT()) {
            player.sendMessage("Not sending NXT music effect archive " + id + ": this session is not marked as NXT.");
            return;
        }
        if (!canSendNxtMusicArchive(id)) {
            player.sendMessage("Not sending NXT music effect archive " + id + ": cache index " + Cache.RS3_MUSIC_INDEX
                    + " has no readable archive data.");
            return;
        }
        OutputStream stream = new OutputStream(4);
        stream.writePacket(player, 9);
        stream.writeShortLE(id);
        write(stream);
    }

    public void sendAncientXNxtMusicTest(int id, int delay, int volume) {
        if (!ANCIENTX_NXT_MUSIC_TEST_PACKETS_ENABLED) {
            player.sendMessage("AncientX NXT music test packets are disabled: opcode 39 crashed this client.");
            return;
        }
        if (!player.isUsingNXT()) {
            player.sendMessage("Not sending AncientX NXT music archive " + id + ": this session is not marked as NXT.");
            return;
        }
        if (!canSendNxtMusicArchive(id)) {
            player.sendMessage("Not sending AncientX NXT music archive " + id + ": cache index " + Cache.RS3_MUSIC_INDEX
                    + " has no readable archive data.");
            return;
        }
        OutputStream stream = new OutputStream(5);
        stream.writePacket(player, ANCIENTX_NXT_MUSIC_PACKET);
        stream.writeByte(delay);
        stream.writeShortLE128(id);
        stream.writeByte128(volume);
        write(stream);
    }

    public boolean canSendLegacyMusicArchive(int archiveId) {
        return archiveId == -1 || canReadCacheArchive(LEGACY_MUSIC_INDEX, archiveId);
    }

    public boolean canSendLegacyMusicEffectArchive(int archiveId) {
        return canReadCacheArchive(LEGACY_MUSIC_EFFECT_INDEX, archiveId);
    }

    public boolean canSendNxtMusicArchive(int archiveId) {
        return canReadCacheArchive(Cache.RS3_MUSIC_INDEX, archiveId);
    }

    public boolean canSendVorbisSoundArchive(int archiveId) {
        return canReadCacheArchive(VORBIS_SOUND_INDEX, archiveId);
    }

    private boolean canReadCacheArchive(int indexId, int archiveId) {
        if (archiveId < 0 || Cache.STORE == null || Cache.STORE.getIndexes() == null
                || Cache.STORE.getIndexes().length <= indexId) {
            return false;
        }
        Index index = Cache.getJs5Index(indexId);
        return index != null && index.archiveExists(archiveId) && index.getMainFile().getArchiveData(archiveId) != null;
    }

    private static int clampAudioVolume(int volume) {
        return Math.max(0, Math.min(255, volume));
    }

    public void sendSkillLevel(int skill) {// updated
        OutputStream stream = new OutputStream(7);
        stream.writePacket(player, 33);
        stream.writeInt((int) player.getSkills().getXp(skill));
        stream.writeByte(player.getSkills().getLevel(skill));
        stream.writeByteC(skill);
//        stream.writeByte(0);
        write(stream);
    }

    // CUTSCENE PACKETS START

    /**
     * This will blackout specified area.
     *
     * @param byte area = area which will be blackout (0 = unblackout; 1 = blackout
     *        orb; 2 = blackout map; 5 = blackout orb and map)
     */
    public void sendBlackOut(int area) {// updated
        OutputStream out = new OutputStream(3);
        out.writePacket(player, 137);
        out.writeByte(area);
        write(out);
    }

    // instant
    public void sendCameraLook(int viewLocalX, int viewLocalY, int viewZ) {
        sendCameraLook(viewLocalX, viewLocalY, viewZ, -1, -1);
    }

    public void sendCameraLook(int viewLocalX, int viewLocalY, int viewZ, int speed1, int speed2) {// updated
        OutputStream stream = new OutputStream(7);
        stream.writePacket(player, 5);
        stream.writeShort128(viewZ >> 2);
        stream.write128Byte(viewLocalY);
        stream.write128Byte(speed2);
        stream.writeByte(speed1);
        stream.writeByteC(viewLocalX);
        write(stream);
    }

    public void sendResetCamera() {// updated
        OutputStream stream = new OutputStream(1);
        stream.writePacket(player, 87);
        write(stream);
    }

    public void sendCameraRotation(int x, int y) {// updated
        OutputStream stream = new OutputStream(6);
        stream.writePacket(player, 164);
        stream.writeShort(y);
        stream.writeShort128(x);
        write(stream);
    }

    public void sendCameraPos(int moveLocalX, int moveLocalY, int moveZ) {
        sendCameraPos(moveLocalX, moveLocalY, moveZ, -1, -1);
    }

    public void sendClientConsoleCommand(String command) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarByte(player, 117);
        stream.writeString(command);
        stream.endPacketVarByte();
    }

    public void sendOpenURL(String url) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 60);
        stream.writeByte(0);// unknown boolean
        stream.writeString(url);
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendSetMouse(String walkHereReplace, int cursor) {// updated
        OutputStream stream = new OutputStream();
        stream.writePacketVarByte(player, 18);
        stream.writeString(walkHereReplace);
        stream.writeShort(cursor);
        stream.endPacketVarByte();
        write(stream);
    }

    public void sendItemsLook() {
        // OutputStream stream = new OutputStream(2);
        // stream.writePacket(player, 159);
        // stream.writeByte(player.isOldItemsLook() ? 1 : 0);
        // write(stream);
    }

    public void sendCameraPos(int moveLocalX, int moveLocalY, int moveZ, int speed1, int speed2) {// updated
        OutputStream stream = new OutputStream(7);
        stream.writePacket(player, 98);
        stream.writeByte(speed1);
        stream.write128Byte(speed2);
        stream.writeByte(moveLocalX);
        stream.writeShort(moveZ >> 2);
        stream.writeByte(moveLocalY);
        write(stream);
    }

    public void ifRetex(int widget, int component, int slot, int id, boolean hidden) {// updated
        OutputStream buffer = new OutputStream(10);
        buffer.writePacket(player, 108);
        buffer.writeShortLE(id);
        buffer.writeByte(hidden ? 1 : 0);
        buffer.writeIntLE(widget << 16 | component);
        buffer.writeShort128(slot);
        write(buffer);
    }

    /**
     * The only legacy egress. Protected (not private) so the native 947 facade
     * can override it to refuse 910 bytes; legacy behaviour is unchanged.
     */
    protected ChannelFuture write(OutputStream stream) {
        if (player.isBot() || player.getRealChannel() == null) {
            return null;
        }
        return player.getRealChannel().writeAndFlush(new GamePacketWriteEvent(Unpooled.wrappedBuffer(stream.getBuffer(), 0, stream.getOffset())));
    }

    public void sendNoTimeOut() {// updated
        // Disabled: the active client does not accept the candidate no-timeout packet.
        // Sending the wrong zero-length packet here can immediately bounce the session.
    }

    public void sendEntityInterface(Entity entity, boolean clickThrought, int windowId, int windowComponentId, int interfaceId) {
        if (entity instanceof NPC)
            sendNPCInterface(entity, clickThrought, windowId, windowComponentId, interfaceId);
        else
            sendPlayerInterface((Player) entity, clickThrought, windowId, windowComponentId, interfaceId);
    }

    public void sendOverlayMessage(int border, String message, boolean sendGameMessage) {
        sendEntityMessage(border, 15263739, player, message);
    }

    public void sendStillProjectileNew(WorldTile from, int fromSizeX, int fromSizeY, WorldTile to, int toSizeX, int toSizeY, Entity lockOn, int gfxId, int startHeight, int endHeight, int startTime, int endTime) {// updated
        WorldTile src = new WorldTile(((from.getX() << 1) + fromSizeX) >> 1, ((from.getY() << 1) + fromSizeY) >> 1, from.getPlane());
        WorldTile dst = new WorldTile(((to.getX() << 1) + toSizeX) >> 1, ((to.getY() << 1) + toSizeY) >> 1, to.getPlane());
        OutputStream stream = createWorldTileStream(src);
        stream.writePacket(player, 13);
        stream.writeByte(((src.getX() & 0x7) << 3) | (src.getY() & 0x7));
        stream.writeByte(dst.getX() - src.getX());
        stream.writeByte(dst.getY() - src.getY());
        stream.writeShort(lockOn == null ? 0 : (lockOn instanceof Player ? -(lockOn.getIndex() + 1) : lockOn.getIndex() + 1));
        stream.writeShort(gfxId);
        stream.writeByte(startHeight);
        stream.writeByte(endHeight);
        stream.writeShort(startTime);
        stream.writeShort(endTime);
        stream.writeByte(0);
        stream.writeShort(0);
        stream.writeShort(0);// new
        write(stream);
    }

    public void sendProjectileProperNew(WorldTile from, int fromSizeX, int fromSizeY, WorldTile to, int toSizeX, int toSizeY, Entity lockOn, int gfxId, int startHeight, int endHeight, int startTime, int endTime, int slope, int angle) {// updated
        WorldTile src = new WorldTile(((from.getX() << 1) + fromSizeX) >> 1, ((from.getY() << 1) + fromSizeY) >> 1, from.getPlane());
        WorldTile dst = new WorldTile(((to.getX() << 1) + toSizeX) >> 1, ((to.getY() << 1) + toSizeY) >> 1, to.getPlane());
        OutputStream stream = createWorldTileStream(src);
        stream.writePacket(player, 13);
        stream.writeByte(((src.getX() & 0x7) << 3) | (src.getY() & 0x7));
        stream.writeByte(dst.getX() - src.getX());
        stream.writeByte(dst.getY() - src.getY());
        stream.writeShort(lockOn == null ? 0 : (lockOn instanceof Player ? -(lockOn.getIndex() + 1) : lockOn.getIndex() + 1));
        stream.writeShort(gfxId);
        stream.writeByte(startHeight);
        stream.writeByte(endHeight);
        stream.writeShort(startTime);
        stream.writeShort(endTime);
        stream.writeByte(angle);
        stream.writeShort(slope);
        stream.writeShort(0);// new
        write(stream);
    }

    /**
     * @param projectile
     */
    public void sendTestProjectile(Projectile projectile) {// updated
        OutputStream stream = createWorldTileStream(projectile.getFrom());
        stream.writePacket(player, 13);
        int localX = projectile.getFrom().getLocalX(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int localY = projectile.getFrom().getLocalY(player.getLastLoadedMapRegionTile(), player.getMapSize());
        int offsetX = localX - ((localX >> 3) << 3);
        int offsetY = localY - ((localY >> 3) << 3);
        stream.writeByte(offsetX << 3 | offsetY);
        stream.writeByte(projectile.getTo().getX() - projectile.getFrom().getX());
        stream.writeByte(projectile.getTo().getY() - projectile.getFrom().getY());
        Entity receiver = projectile.getTo() instanceof Entity ? (Entity) projectile.getTo() : null;
        stream.writeShort(receiver == null ? 0 : receiver instanceof Player ? -(receiver.getIndex() + 1) : receiver.getIndex() + 1);
        stream.writeShort(projectile.getGraphicId());
        stream.writeByte(projectile.getStartHeight());
        stream.writeByte(projectile.getEndHeight());
        stream.writeShort(projectile.getStartTime());
        stream.writeShort(projectile.getEndTime());
        stream.writeByte(projectile.getAngle());
        stream.writeShort(projectile.getSlope());
        stream.writeShort(0);// new
        write(stream);
    }

    public void sendIComponentColour(int interfaceId, int componentId, int colour) {// updated
        OutputStream stream = new OutputStream(8);
        stream.writePacket(player, 140);
        stream.writeIntV2(interfaceId << 16 | componentId);
        stream.writeShortLE128(colour);
        write(stream);
    }

    public void sendPouchInfusionOptionsScript(boolean dung, int interfaceId, int componentId, int slotLength, int width, int height, String... options) {
        Object[] parameters = new Object[5 + options.length];
        int index = 0;
        if (dung) {
            parameters[index++] = 1159;
            parameters[index++] = 1100;
        } else {
            parameters[index++] = slotLength;
            parameters[index++] = 1;
        }
        parameters[index++] = height;
        parameters[index++] = width;
        parameters[index++] = interfaceId << 16 | componentId;
        for (int count = options.length - 1; count >= 0; count--)
            parameters[index++] = options[count];
        sendRunScript(757, parameters);
    }

    public void sendScrollInfusionOptionsScript(boolean dung, int interfaceId, int componentId, int slotLength, int width, int height, String... options) {
        Object[] parameters = new Object[5 + options.length];
        int index = 0;
        if (dung) {
            parameters[index++] = 1159;
            parameters[index++] = 1100;
        } else {
            parameters[index++] = slotLength;
            parameters[index++] = 1;
        }
        parameters[index++] = height;
        parameters[index++] = width;
        parameters[index++] = interfaceId << 16 | componentId;
        for (int count = options.length - 1; count >= 0; count--)
            parameters[index++] = options[count];
        sendRunScript(763, parameters);
    }

    public void sendIComponentInputInteger(int interfaceId, int componentId, int length) {
        player.getPackets().sendGlobalConfig(2235, (interfaceId << 16 | componentId));
        player.getPackets().sendGlobalConfig(2236, 7);
        player.getPackets().sendGlobalConfig(2237, length);
    }

    public void sendIComponentInputText(int interfaceId, int componentId, int length) {
        player.getPackets().sendGlobalConfig(2235, (interfaceId << 16 | componentId));
        player.getPackets().sendGlobalConfig(2236, 9);
        player.getPackets().sendGlobalConfig(2237, length);
    }

    public static void writeDyeData(OutputStream stream, Item item) {
//        ItemDye dyeData = item == null ? null : item.getDyeData();
//        stream.writeByte(dyeData != null ? 1 : 0);
//        if (dyeData != null) {
//            int itemFlag = 0;
//            if (dyeData.suffix != null)
//                itemFlag |= 0x10;
//            stream.writeByte(itemFlag);
//            if (dyeData.suffix != null)
//                stream.writeString(dyeData.suffix);
//        }
    }

    public void sendStoreServerPermVarcs() {
        OutputStream stream = new OutputStream(2);
        stream.writePacket(player, 148);
        write(stream);
    }

    public void sendWorldList(int clientChecksum, int[] online) {
        int lowestWorldId = Integer.MAX_VALUE;
        int highestWorldId = Integer.MIN_VALUE;
        int ourChecksum = Settings.WORLDS_INFORMATION.length;
        for (int i = 0; i < Settings.WORLDS_INFORMATION.length; i++) {
            WorldInformation world = Settings.WORLDS_INFORMATION[i];
            ourChecksum += (ourChecksum * world.hashCode());
            if (world.getId() < lowestWorldId)
                lowestWorldId = world.getId();
            if (world.getId() > highestWorldId)
                highestWorldId = world.getId();
        }
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 167);
        stream.writeByte(1); // 0 - buffer only, 1 - parse
        stream.writeByte(2); // list version
        stream.writeByte(ourChecksum != clientChecksum ? 1 : 0);
        if (ourChecksum != clientChecksum) {
            stream.writeSmart(Settings.WORLDS_INFORMATION.length); // number of
            // locations
            for (int i = 0; i < Settings.WORLDS_INFORMATION.length; i++) {
                WorldInformation world = Settings.WORLDS_INFORMATION[i];
                stream.writeSmart(world.getCountryFlagID());
                stream.writeVersionedString(world.getCountryName());

            }
            stream.writeSmart(lowestWorldId);
            stream.writeSmart(highestWorldId);
            stream.writeSmart(Settings.WORLDS_INFORMATION.length); // number of
            // worlds
            for (int i = 0; i < Settings.WORLDS_INFORMATION.length; i++) {
                WorldInformation world = Settings.WORLDS_INFORMATION[i];
                stream.writeSmart(world.getId() - lowestWorldId);
                stream.writeByte(i); // world location index
                stream.writeInt(world.getFlags());
                stream.writeSmart(0); // if not 0 also sends a string which i
                // dont know what is for yet
                stream.writeVersionedString(world.getActivity());
                stream.writeVersionedString(world.getIp());
            }
            stream.writeInt(ourChecksum);
        }
        for (int i = 0; i < Settings.WORLDS_INFORMATION.length; i++) {
            WorldInformation world = Settings.WORLDS_INFORMATION[i];
            stream.writeSmart(world.getId() - lowestWorldId);
            if (online[i] == -1)
                stream.writeShort(65535);
            else
                stream.writeShort(online[i]);
        }
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendServerTickEndPacket() {
        OutputStream stream = new OutputStream(1);
        stream.writePacket(player, 83);
        write(stream);
    }

    public void sendInputNameScript(String message) {
        player.getInterfaceManager().sendInputTextInterface();
        sendExecuteScript(109, message);
    }

    public void sendInputIntegerScript(String message) {
        player.getInterfaceManager().sendInputTextInterface();
        sendExecuteScript(108, message);
    }

    public void sendInputLongTextScript(String message) {
        player.getInterfaceManager().sendInputTextInterface();
        sendExecuteScript(110, message);
    }

    public void sendMainInterfaceMessage(int border, String message, boolean sendGameMessage) {
        sendExecuteScript(1211, message, 5, -120, border);
        if (sendGameMessage)
            sendGameMessage((border == 0 ? "" : "<col=ff0000>") + message);
    }

    public void sendBobInventoryMessage(int border, int slotId, String message) {
        sendInterfaceMessage(662, 5, border, slotId, message);
    }

    public void sendrefreshComponentScript(int interfaceId, int componentId) {
        player.getPackets().sendExecuteScript(3087, InterfaceManager.getComponentUId(interfaceId, componentId));
    }

    public void sendCurrentTarget(Entity target) {
        OutputStream stream = new OutputStream(3);
        stream.writePacket(player, 150);
        stream.writeShort128(target == null ? 0 : (target instanceof Player ? -(target.getIndex() + 1) : target.getIndex() + 1));
        write(stream);
    }

    public void sendGEItemSearch() {
        player.getPackets().sendGlobalConfig(2235, (105 << 16 | 11));
        player.getPackets().sendGlobalConfig(2236, 10);
        player.getPackets().sendGlobalConfig(2237, 40);
    }

    public void sendOtherPlayerOnIComponent(int interfaceId, int componentId, Player p2) {
        OutputStream stream = new OutputStream(11);
        stream.writePacket(player, 100);
        stream.writeIntLE(p2.getDisplayName().hashCode());
        stream.writeShortLE128(p2.getIndex());
        stream.writeInt(interfaceId << 16 | componentId);
        write(stream);
    }

    public void sendAppearenceLook() {
        sendAppearanceLook(player.getEquipment().getCosmeticItems().getItems(), player.getAppearence().getBodyStyle());
    }

    public void sendAppearanceLook(Item[] items, int[] look) {
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 105);
        stream.writeByte(player.getAppearence().isMale() ? 0 : 1);
        stream.writeBytes(player.getAppearence().getAppearenceLook(items, look));
        stream.endPacketVarShort();
        write(stream);
    }

    public void sendCustomPlayerOnIComponent(int interfaceId, int componentId, int customIndex) {
        OutputStream stream = new OutputStream(7);
        stream.writePacket(player, 180);
        stream.writeByte128(customIndex);
        stream.writeIntV1(interfaceId << 16 | componentId);
        write(stream);
    }

    public void sendCustomPlayerAppearanceLook(Item[] items, int[] look, int customIndex) {
        OutputStream stream = new OutputStream();
        stream.writePacketVarShort(player, 67);
        stream.writeByte(customIndex);
        stream.writeByte(player.getAppearence().isMale() ? 0 : 1);
        stream.writeBytes(player.getAppearence().getAppearenceLook(items, look));
        stream.endPacketVarShort();
        write(stream);
    }


    public void sendEnvironmentOverridePacket(int regionId) {
        AtmosphereDefinition def = AtmosphereDefinition.getAtmosphereDefinitions(regionId);
        OutputStream stream = new OutputStream();
        stream.writePacketVarByte(player, 1);
        if (def == null) {
            stream.writeInt(0);
            stream.writeShort(5000);
        } else {
            int flag = def.flag;
            stream.writeInt(flag);
            if (0 != (flag & Class189.aClass703_8981.anInt8984))
                stream.writeInt(def.anInt8551);
            if (0 != (flag & Class189.aClass703_8960.anInt8984))
                stream.writeInt(Float.floatToIntBits(def.aFloat8547));
            if (0 != (flag & Class189.aClass703_8961.anInt8984))
                stream.writeInt(Float.floatToIntBits(def.aFloat8538));
            if (0 != (flag & Class189.aClass703_8965.anInt8984))
                stream.writeInt(Float.floatToIntBits(def.aFloat8540));
            if ((flag & Class189.aClass703_8963.anInt8984) != 0) {
                stream.writeInt(Float.floatToIntBits(def.aClass462_8537.x));
                stream.writeInt(Float.floatToIntBits(def.aClass462_8537.y));
                stream.writeInt(Float.floatToIntBits(def.aClass462_8537.z));
            }
            if ((flag & Class189.aClass703_8972.anInt8984) != 0)
                stream.writeInt(def.anInt8542);
            if ((flag & Class189.aClass703_8964.anInt8984) != 0)
                stream.writeShort(def.anInt8543);
            if ((flag & Class189.aClass703_8966.anInt8984) != 0)
                stream.writeShort(def.i_23_);
            stream.writeShort(1);
        }
        stream.endPacketVarByte();
        write(stream);
    }
}
