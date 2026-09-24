package com.rs.game.player.client;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.NewProjectile;
import com.rs.game.Projectile;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.ItemsContainer;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.content.ChatMessage;
import com.rs.game.player.content.HintIcon;
import com.rs.game.player.content.PublicChatMessage;
import com.rs.game.player.content.QuickChatMessage;
import com.rs.game.player.content.clans.ClansManager;
import com.rs.game.player.content.grandExchange.Offer;
import com.rs.network.io.OutputStream;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.packet.PacketDispatcher;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.utils.Utils;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

/**
 * The 947 face of {@link PacketDispatcher}. Every public method of the 910
 * serializer is overridden into exactly one of three tiers:
 *
 * <ul>
 * <li><b>REAL</b>: routed through a verified {@link Native950Packets} writer after
 *     id rebinding via {@link Native950IdMap}. The writer range-checks its input;
 *     an {@link IllegalArgumentException} becomes a counted, logged-once drop so a
 *     bad id from 910 content can never kill the world tick.</li>
 * <li><b>NO-OP</b>: cosmetic families whose 947 opcode is unverified (sound,
 *     camera, hint icons, minimap, component sprites and models, ...). They only
 *     bump a per-method counter. Each becomes REAL the day its packet is verified.</li>
 * <li><b>STRICT</b>: state-carrying families that would desynchronise the client
 *     if silently dropped (zone-relative packets, varcs, friends/clan/PM, skill
 *     stats, active-interface openers...). They throw
 *     {@link UnsupportedOperationException} while {@link #strict} is true
 *     (tests and smokes) and are counted and dropped when it is false
 *     (exploratory runs).</li>
 * </ul>
 *
 * <p>Packets are written to the player's channel without flushing;
 * {@link Native950Session} owns REBUILD/PLAYER_INFO/NPC_INFO/keepalive and the
 * single per-tick {@code tickEnd()} flush, which is why the session-owned
 * methods here are no-ops. The legacy {@link #write(OutputStream)} egress is
 * sealed: no 910 bytes can leave through this object.
 */
public class Native950PacketDispatcher extends PacketDispatcher {

    /** JVM property read once at class init: {@code -Dataraxia947.strict=false} starts lenient. */
    public static final String STRICT_PROPERTY = "ataraxia947.strict";

    /**
     * True = STRICT tier throws; false = STRICT tier counts and drops. Initialised
     * from {@link #STRICT_PROPERTY} (default true: tests and smokes fail closed) and
     * still toggleable at runtime through {@link #setStrict(boolean)}.
     */
    public static volatile boolean strict = Boolean.parseBoolean(System.getProperty(STRICT_PROPERTY, "true"));

    /** Reason recorded when a resolver returns -1 for an id (P2 binding table rejection). */
    static final String UNBOUND_ID = "id is not bound in the 947 table";

    /**
     * Thrown inside a writer body when {@link Native950IdMap} resolves an id to -1
     * (the P2 resolver rejects it). It is an IllegalArgumentException so
     * {@link #real} turns it into a counted drop: never a throw, never a wire write.
     */
    static final class UnboundIdException extends IllegalArgumentException {
        private static final long serialVersionUID = 1L;
        UnboundIdException(String kind, int id) { super(kind + " " + id + ": " + UNBOUND_ID); }
    }

    /** Returns the resolved id or throws {@link UnboundIdException} for -1. */
    static int bound(String kind, int original, int resolved) {
        if (resolved < 0) throw new UnboundIdException(kind, original);
        return resolved;
    }

    private static final int LOG_ONCE_LIMIT = 512;
    private static final Set<String> LOGGED = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private static volatile boolean nisVarsWarned;

    /** The single verified game-bar varbit; see the P1 backlog entry. */
    private static final int GAME_BAR_VARBIT = 18797;

    private final Counters counters = new Counters();

    public Native950PacketDispatcher(Player player) {
        super(player);
    }

    public static boolean isStrict() { return strict; }
    public static void setStrict(boolean value) { strict = value; }

    /** Per-method tier counters for {@code Native950Session.Snapshot} and tests. */
    public Counters counters() { return counters; }

    // ---------------------------------------------------------------- egress

    /** Legacy 910 bytes never leave a native player. */
    @Override
    protected ChannelFuture write(OutputStream stream) {
        throw new IllegalStateException("Legacy 910 packet bytes are forbidden for native 947 players");
    }

    /** Reason given for the drop when {@link #send} finds no attached 947 transport. */
    static final String TRANSPORT_NOT_ATTACHED = "transport not attached";

    /**
     * Writes one verified packet without flushing. The channel is looked up per
     * call, and it only counts as attached while it is active AND carries the
     * {@link Native950GameTransport}: {@code Player.createNative950} always has a
     * channel, but {@code Native950World.attachOnWorld} adds the transport on the
     * event loop AFTER the player exists, so a facade write in that window (or
     * after the channel closed) would otherwise vanish into the login pipeline
     * and be mis-counted as sent. A write whose future fails afterwards (the
     * transport rejects it, the channel closes first) is counted as a drop from
     * the event loop; {@link Counters} is thread-safe.
     *
     * @return false when there is no attached transport to write to (counted as a drop by callers)
     */
    protected boolean send(Native950Packets.Packet packet) {
        return send("send", packet);
    }

    /** Same as {@link #send(Native950Packets.Packet)} with the facade method name for the counters. */
    protected boolean send(final String method, Native950Packets.Packet packet) {
        Channel channel = getPlayer().getRealChannel();
        if (!isTransportAttached(channel)) return false;
        channel.write(packet).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) {
                if (future.isSuccess()) return;
                counters.recordDropped(method);
                logOnce("[Ataraxia950] Write of " + method + " failed after being counted as sent: " + future.cause());
            }
        });
        return true;
    }

    /** True only for a live channel whose pipeline already holds the 947 game transport. */
    static boolean isTransportAttached(Channel channel) {
        return channel != null && channel.isActive() && channel.pipeline().get(Native950GameTransport.class) != null;
    }

    // ---------------------------------------------------------------- tiers

    /** Builds one verified packet; may throw IllegalArgumentException for out-of-range ids. */
    private interface Body {
        Native950Packets.Packet build();
    }

    private void real(String method, Body body, Object... args) {
        Native950Packets.Packet packet;
        try {
            packet = body.build();
        } catch (IllegalArgumentException rejected) {
            drop(method, "writer rejected " + rejected.getMessage(), args);
            return;
        }
        if (send(method, packet)) counters.recordSent(method);
        else drop(method, TRANSPORT_NOT_ATTACHED, args);
    }

    /**
     * Counted NO-OP. Callers that carry an id/value MUST pass them: a NO-OP that
     * discards its arguments silently is indistinguishable from any other call to
     * the same method, which is exactly what the "unknown = counted and logged"
     * rule forbids. With arguments the id is recorded in a per-method histogram
     * ({@link Counters#noopIds(String)}) and logged once, the same shape
     * {@link #drop} uses, so the unmatched-id inventory in
     * {@code notes/P5-router.md} can be checked against a smoke run.
     *
     * <p>The argument-less form stays for the cosmetic families that carry no id
     * worth recording (camera, sound, minimap flag, ...).
     */
    private void noop(String method, Object... args) {
        counters.recordNoop(method);
        if (args == null || args.length == 0) return;
        counters.recordDiscardedId(method, args[0]);
        counters.recordDiscardedTarget(method, args[0], args.length > 1 ? args[1] : null);
        logOnce("[Ataraxia950] No-op " + method + describe(args)
                + ": no verified 947 packet for this family, value discarded");
    }

    private void strict(String method, String reason, Object... args) {
        counters.recordStrict(method);
        if (strict)
            throw new UnsupportedOperationException(method + " is not verified for 947: " + reason);
        drop(method, "strict tier disabled, " + reason, args);
    }

    /**
     * Counted drop. Like {@link #noop} it records the first argument in the
     * discarded-id histogram: M3 promoted the varc and stat families out of the
     * NO-OP tier, so from here on an unbound id is discarded as a DROP, and the
     * "unknown = counted and logged, with the id still identifiable" rule the M2b
     * unmatched-id inventory rests on has to hold for this tier too.
     */
    private void drop(String method, String reason, Object... args) {
        counters.recordDropped(method);
        if (args != null && args.length > 0) {
            counters.recordDiscardedId(method, args[0]);
            counters.recordDiscardedTarget(method, args[0], args.length > 1 ? args[1] : null);
        }
        logOnce("[Ataraxia950] Dropped " + method + describe(args) + ": " + reason);
    }

    private static void degraded(String method) {
        logOnce("[Ataraxia950] " + method + " degraded to a plain game message (IF_OPENSUB_ACTIVE unverified)");
    }

    private static void logOnce(String message) {
        if (LOGGED.size() >= LOG_ONCE_LIMIT) return;
        if (LOGGED.add(message)) System.out.println(message);
    }

    private static String describe(Object[] args) {
        if (args == null || args.length == 0) return "";
        StringBuilder out = new StringBuilder("(");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) out.append(", ");
            Object arg = args[i];
            if (arg instanceof Object[]) out.append(java.util.Arrays.deepToString((Object[]) arg));
            else if (arg instanceof int[]) out.append(java.util.Arrays.toString((int[]) arg));
            else out.append(arg);
        }
        return out.append(')').toString();
    }

    // ---------------------------------------------------------------- shared writers

    private void message(String method, final int type, final String text, final Player sender) {
        if(sender==null&&Native950DeveloperOutput.capture(getPlayer(),text))return;
        final String senderName = sender == null ? null : Utils.formatPlayerNameForDisplay(sender.getDisplayName());
        final String senderAlias = sender == null ? null : sender.getDisplayName();
        final String body = String.valueOf(text);
        real(method, new Body() {
            @Override public Native950Packets.Packet build() {
                return Native950Packets.gameMessage(type, senderName, senderAlias, body);
            }
        }, type, text);
    }

    // Resolver adapters: a -1 from Native950IdMap becomes a counted drop via UnboundIdException.
    static int iface(int interfaceId) { return bound("interface", interfaceId, Native950IdMap.interfaceId(interfaceId)); }
    static int comp(int interfaceId, int componentId) { return bound("component", componentId, Native950IdMap.componentId(interfaceId, componentId)); }
    static int varpId(int id) { return bound("varp", id, Native950IdMap.varp(id)); }
    static int varbitId(int id) { return bound("varbit", id, Native950IdMap.varbit(id)); }
    static int varcId(int id) { return bound("varc", id, Native950IdMap.varc(id)); }
    static int scriptId(int id) { return bound("script", id, Native950IdMap.script(id)); }
    /** Packed 947 hash for cs2 arguments; either half unbound makes the whole packet a counted drop. */
    static int boundHash(int interfaceId, int componentId) { return (iface(interfaceId) << 16) | (comp(interfaceId, componentId) & 0xffff); }

    private void text(String method, final int interfaceId, final int componentId, Object text) {
        final String value = String.valueOf(text);
        real(method, new Body() {
            @Override public Native950Packets.Packet build() {
                return Native950Packets.interfaceText(iface(interfaceId), comp(interfaceId, componentId), value);
            }
        }, interfaceId, componentId, value);
    }

    private void hide(String method, final int interfaceId, final int componentId, final boolean hidden) {
        real(method, new Body() {
            @Override public Native950Packets.Packet build() {
                return Native950Packets.hideInterface(iface(interfaceId), comp(interfaceId, componentId), hidden);
            }
        }, interfaceId, componentId, hidden);
    }

    private void events(String method, final int interfaceId, final int componentId, final int fromSlot,
                        final int toSlot, final int settings) {
        real(method, new Body() {
            @Override public Native950Packets.Packet build() {
                return Native950Packets.interfaceEvents(iface(interfaceId), comp(interfaceId, componentId), fromSlot, toSlot, settings);
            }
        }, interfaceId, componentId, fromSlot, toSlot, settings);
    }

    private void closeSub(String method, final int packedHash) {
        real(method, new Body() {
            @Override public Native950Packets.Packet build() {
                return Native950Packets.closeSub(iface(packedHash >>> 16), comp(packedHash >>> 16, packedHash & 0xffff));
            }
        }, packedHash >>> 16, packedHash & 0xffff);
    }

    /**
     * Player variables. VARP_SMALL (opcode 10) is CONFIRMED in Protocol S
     * ({@code vars/VARP_SMALL.md}) and reaches -128..127; VARP_LARGE (opcode 111)
     * carries the full 32-bit form but is NOT in {@code verifiedNames.toml} -
     * {@code PROTOCOL-S-SUMMARY.md} section 3 lists it as "not promoted for lack
     * of a verdict this round". Everything that fits therefore goes on the
     * CONFIRMED opcode, which covers every M3 varp except adrenaline (679,
     * 0..1000); only a value the small form cannot carry falls back to 111, and
     * that fallback is the one wire form in this area still waiting on a verdict.
     * The id is resolved through the binding table first either way, so an
     * unbound varp is a counted drop rather than a wire write.
     */
    private void varp(String method, final int id, final int value) {
        real(method, new Body() {
            @Override public Native950Packets.Packet build() {
                int resolved = varpId(id);
                return value >= -128 && value <= 127
                        ? Native950Packets.varpSmall(resolved, value)
                        : Native950Packets.varp(resolved, value);
            }
        }, id, value);
    }

    /**
     * Varbits, always on VARBIT_LARGE (opcode 71). Its layout is CONFIRMED
     * ({@code vars/VARBIT_LARGE.md}, parser 0x140140FF0) and its little-endian
     * 32-bit value covers every varbit width, so three extra bytes buy the whole
     * range on verified ground. The byte-sized VARBIT_SMALL (opcode 50) is
     * deliberately NOT used here: it is absent from {@code verifiedNames.toml}
     * and {@code PROTOCOL-S-SUMMARY.md} section 3 records it as "not promoted for
     * lack of a verdict this round", and four of M3's seven login vars (hitpoints
     * 1668 below 25.6 life points, prayer 16736 below 25.6 points, summoning
     * 41524 at low levels, virtual levels 19007) would otherwise ride on it.
     * {@code Native950Packets.varbitSmall} keeps its writer and its byte test for
     * the day opcode 50 gets a verdict. The id is still resolved through the
     * binding table first, so a varbit with no 947 binding is a counted drop, not
     * a wire write that happens to have a verified opcode.
     */
    private void varbit(String method, final int id, final int value) {
        real(method, new Body() {
            @Override public Native950Packets.Packet build() {
                return Native950Packets.varbitLarge(varbitId(id), value);
            }
        }, id, value);
    }

    /**
     * Client variables (CLIENT_SETVARC_SMALL 1 / _LARGE 112). The 910 caller picks
     * the form by range exactly as {@link Native950Packets#varc} does, so
     * {@code sendGlobalConfig} and the two explicit forms all land on the same
     * stored int. {@link #varcId} rejects every id the binding table does not
     * declare: the 947 varc space is not the 910 one, so a verified opcode is not
     * by itself a licence to write a 910 varc id.
     */
    private void varc(String method, final int id, final int value, final boolean forceLarge, final boolean forceSmall) {
        real(method, new Body() {
            @Override public Native950Packets.Packet build() {
                int resolved = varcId(id);
                if (forceLarge) return Native950Packets.varcLarge(resolved, value);
                if (forceSmall) return Native950Packets.varcSmall(resolved, value);
                return Native950Packets.varc(resolved, value);
            }
        }, id, value);
    }

    /** CLIENT_SETVARCSTR_SMALL 67 / _LARGE 15; the writer picks the form by encoded length. */
    private void varcString(String method, final int id, String text) {
        final String value = text == null ? "" : text; // 910 sendGlobalString maps null to the empty string
        real(method, new Body() {
            @Override public Native950Packets.Packet build() {
                return Native950Packets.varcString(varcId(id), value);
            }
        }, id, value);
    }

    /** Arguments are in natural (script-parameter) order; only Integer/String survive the writer. */
    private void script(String method, final int scriptId, final Object[] naturalOrderArgs) {
        final Object[] args = naturalOrderArgs == null ? new Object[0] : naturalOrderArgs.clone();
        real(method, new Body() {
            @Override public Native950Packets.Packet build() {
                return Native950Packets.runClientScript(scriptId(scriptId), args);
            }
        }, scriptId, args);
    }

    /**
     * 910 {@code sendRunScript} writes the type string reversed and the values in
     * call order, so the client script receives the parameters reversed;
     * {@code sendExecuteScript} pre-reverses them to get natural order. The native
     * writer takes natural order, so this is the sendRunScript adapter.
     */
    private static Object[] reversed(Object[] params) {
        if (params == null) return new Object[0];
        Object[] out = new Object[params.length];
        for (int i = 0; i < params.length; i++) out[i] = params[params.length - 1 - i];
        return out;
    }

    private static void warnNisVarsOnce() {
        if (nisVarsWarned) return;
        nisVarsWarned = true;
        System.out.println("[Ataraxia950] Per-item NIS variables are not forwarded to 947 containers (parameter records disabled)");
    }

    /** Legacy negative keys mean "secondary" and reach the 910 wire as an unsigned short. */
    private static int containerKey(int key) {
        return bound("container", key, Native950IdMap.container(key)) & 0xffff;
    }

    private static int[][] slotsOf(Item[] items) {
        int[] ids = new int[items.length], amounts = new int[items.length];
        for (int i = 0; i < items.length; i++) {
            Item item = items[i];
            if (item == null || item.getId() < 0 || item.getAmount() <= 0) {
                ids[i] = -1;
                amounts[i] = 0;
            } else {
                ids[i] = item.getId();
                amounts[i] = item.getAmount();
            }
        }
        return new int[][] {ids, amounts};
    }

    private void containerFull(String method, final int key, final boolean secondary, Item[] items) {
        warnNisVarsOnce();
        final int[][] slots = slotsOf(items == null ? new Item[0] : items);
        real(method, new Body() {
            @Override public Native950Packets.Packet build() {
                return Native950Packets.inventoryFull(containerKey(key), secondary, slots[0], slots[1]);
            }
        }, key, secondary, slots[0].length);
    }

    // ================================================================ REAL: messages

    @Override public void sendGameMessage(String text) { message("sendGameMessage", 0, text, null); }
    @Override public void sendGameMessage(String text, boolean filter) { message("sendGameMessage", filter ? 109 : 0, text, null); }
    @Override public void sendFilteredGameMessage(boolean filter, String text, Object... args) {
        message("sendFilteredGameMessage", filter ? 109 : 0, String.format(text, args), null);
    }
    @Override public void sendPanelBoxMessage(String text) { message("sendPanelBoxMessage", getPlayer().getRights() > 0 ? 99 : 0, text, null); }
    @Override public void sendConsoleMessage(String text) { message("sendConsoleMessage", 99, text, null); }
    @Override public void sendDebugMessageToConsole(String text) { message("sendDebugMessageToConsole", 99, text, null); }
    @Override public void sendMessage(int type, String text, Player p) { message("sendMessage", type, text, p); }
    @Override public void sendNPCMessage(int border, NPC npc, String message) { message("sendNPCMessage", 0, message, null); }
    @Override public void sendTradeRequestMessage(Player p) { message("sendTradeRequestMessage", 100, "wishes to trade with you.", p); }
    @Override public void sendGambleRequestMessage(Player p, int value) {
        message("sendGambleRequestMessage", 100, "wishes to gamble "
                + java.text.NumberFormat.getNumberInstance(java.util.Locale.US).format(value) + " GP against you.", p);
    }
    @Override public void sendClanWarsRequestMessage(Player p) { message("sendClanWarsRequestMessage", 101, "wishes to challenge your clan to a clan war.", p); }
    @Override public void sendDuelChallengeRequestMessage(Player p, boolean friendly) {
        message("sendDuelChallengeRequestMessage", 101, "wishes to duel with you(" + (friendly ? "friendly" : "stake") + ").", p);
    }
    @Override public void sendDungeonneringRequestMessage(Player p) { sendDungeonneringRequestMessage(p, false); }
    @Override public void sendDungeonneringRequestMessage(Player p, boolean elite) {
        message("sendDungeonneringRequestMessage", 111, "has invited you to a " + (elite ? "elite " : "") + "dungeon party.", p);
    }
    @Override public void sendDungDuoRequestMessage(Player p, boolean friendly) { message("sendDungDuoRequestMessage", 101, "wishes to play a duo Dungeoneering.", p); }
    @Override public void sendClanInviteMessage(Player p) { message("sendClanInviteMessage", 117, p.getDisplayName() + " is inviting you to join their clan.", p); }

    // Overlay/entity message boxes need IF_OPENSUB_ACTIVE_* (unverified): degrade to plain text.
    @Override public void sendPlayerMessage(int border, int color, String message, boolean sendGameMessage) {
        degraded("sendPlayerMessage"); message("sendPlayerMessage", 0, message, null);
    }
    @Override public void sendPlayerMessage(int border, int color, Player p, String message, boolean sendGameMessage) {
        degraded("sendPlayerMessage"); message("sendPlayerMessage", 0, message, null);
    }
    @Override public void sendPlayerMessageBox(String message) { degraded("sendPlayerMessageBox"); message("sendPlayerMessageBox", 0, message, null); }
    @Override public void sendEntityMessage(int border, int color, Entity entity, String message) {
        degraded("sendEntityMessage"); message("sendEntityMessage", 0, message, null);
    }
    @Override public void sendEntityMessage(int border, int color, Entity entity, String message, boolean sendGameMessage) {
        degraded("sendEntityMessage"); message("sendEntityMessage", 0, message, null);
    }
    @Override public void sendNPCMessage(int border, int color, Entity npc, String message) {
        degraded("sendNPCMessage"); message("sendNPCMessage", 0, message, null);
    }
    @Override public void sendObjectMessage(int border, int color, WorldObject object, String message) {
        degraded("sendObjectMessage"); message("sendObjectMessage", 0, message, null);
    }
    @Override public void sendOverlayMessage(int border, String message, boolean sendGameMessage) {
        degraded("sendOverlayMessage"); message("sendOverlayMessage", 0, message, null);
    }
    @Override public void sendFloorItemMessage(int border, int color, FloorItem item, String message) {
        degraded("sendFloorItemMessage"); message("sendFloorItemMessage", 0, message, null);
    }

    // ================================================================ REAL: interface text / hide / events

    @Override public void sendText(int interfaceId, int componentId, Object text) { text("sendText", interfaceId, componentId, text); }
    @Override public void sendIComponentText(int interfaceId, int componentId, Object text) { text("sendIComponentText", interfaceId, componentId, text); }
    @Override public void sendEmptyTextToComponents(int interfaceId, int... components) {
        for (int component : components) text("sendEmptyTextToComponents", interfaceId, component, "");
    }

    @Override public void sendHideIComponent(int interfaceId, int componentId, boolean hidden) { hide("sendHideIComponent", interfaceId, componentId, hidden); }
    @Override public void sendInterfaceConfig(int interfaceId, int componentId, boolean hide) { hide("sendInterfaceConfig", interfaceId, componentId, hide); }
    @Override public void sendHideComponents(int interfaceId, boolean hide, int... components) {
        for (int component : components) hide("sendHideComponents", interfaceId, component, hide);
    }

    @Override public void sendIComponentSettings(int interfaceId, int componentId, int fromSlot, int toSlot, int settingsHash) {
        events("sendIComponentSettings", interfaceId, componentId, fromSlot, toSlot, settingsHash);
    }
    @Override public void sendAccessMask(Player player, int interfaceId, int componentId, int fromSlot, int toSlot, int settingsHash) {
        events("sendAccessMask", interfaceId, componentId, fromSlot, toSlot, settingsHash);
    }
    @Override public void sendUnlockIComponentOptionSlots(int interfaceId, int componentId, int fromSlot, int toSlot, boolean unlockEvent, int... optionsSlots) {
        int settingsHash = unlockEvent ? 1 : 0;
        for (int slot : optionsSlots) settingsHash |= 2 << slot;
        events("sendUnlockIComponentOptionSlots", interfaceId, componentId, fromSlot, toSlot, settingsHash);
    }
    @Override public void sendUnlockIComponentOptionSlots(int interfaceId, int componentId, int fromSlot, int toSlot, int... optionsSlots) {
        sendUnlockIComponentOptionSlots(interfaceId, componentId, fromSlot, toSlot, false, optionsSlots);
    }

    // ================================================================ REAL: open / close

    /** 910 clickThrough is the 947 walkable flag; the parent UID is a packed 910 hash. */
    @Override public void sendInterface(final boolean clickThrought, final int parentUID, final int interfaceId) {
        real("sendInterface", new Body() {
            @Override public Native950Packets.Packet build() {
                return Native950Packets.openSub(iface(parentUID >>> 16), comp(parentUID >>> 16, parentUID & 0xffff),
                        iface(interfaceId), clickThrought);
            }
        }, clickThrought, parentUID >>> 16, parentUID & 0xffff, interfaceId);
    }

    /** The 910 type byte has no verified 947 counterpart (the IF_OPENTOP body zeroes it). */
    @Override public void sendWindowsPane(final int id, int type) {
        if (getPlayer().getInterfaceManager() != null) getPlayer().getInterfaceManager().setWindowsPane(id);
        real("sendWindowsPane", new Body() {
            @Override public Native950Packets.Packet build() {
                return Native950Packets.openTop(iface(id));
            }
        }, id, type);
    }

    @Override public void closeInterface(int parentUID) { closeSub("closeInterface", parentUID); }
    @Override public void closeInterface(int windowId, int windowComponentId) { closeSub("closeInterface", (windowId << 16) | (windowComponentId & 0xffff)); }

    // ================================================================ REAL: vars

    @Override public void sendConfig(int id, int value) { varp("sendConfig", id, value); }
    @Override public void sendConfig1(int id, int value) { varp("sendConfig1", id, value); }
    @Override public void sendConfig2(int id, int value) { varp("sendConfig2", id, value); }
    @Override public void sendVar(int id, int value) { varp("sendVar", id, value); }

    @Override public void sendConfigByFile(int fileId, int value) { varbit("sendConfigByFile", fileId, value); }
    @Override public void sendConfigByFile(int fileId, int value, boolean newDefs) { varbit("sendConfigByFile", fileId, value); }
    @Override public void sendConfigByFile1(int fileId, int value, boolean newDefs) { varbit("sendConfigByFile1", fileId, value); }
    @Override public void sendConfigByFile2(int fileId, int value, boolean newDefs) { varbit("sendConfigByFile2", fileId, value); }
    @Override public void sendVarBit(int id, int value) { varbit("sendVarBit", id, value); }

    /** Only varbit 18797 is verified in 947; the remaining ~100 game-bar varbits are counted drops. */
    @Override public void sendGameBarStages() {
        boolean isGameOn = getPlayer().getGameStatus() != 2;
        varbit("sendGameBarStages", GAME_BAR_VARBIT, isGameOn ? 1 : 0);
        drop("sendGameBarStages", "remaining game-bar varbits and the 910 status packets are unverified");
    }

    // ================================================================ REAL: client scripts

    @Override public void sendExecuteScript(int scriptId, Object... params) { script("sendExecuteScript", scriptId, params); }
    @Override public void sendRunScript(int scriptId, Object... params) { script("sendRunScript", scriptId, reversed(params)); }
    @Override public void sendExecuteScriptReverse(int scriptId, Object... params) { script("sendExecuteScriptReverse", scriptId, reversed(params)); }
    @Override public void sendRunScriptBlank(int scriptId) { script("sendRunScriptBlank", scriptId, new Object[0]); }

    @Override public void sendInterfaceMessage(int interfaceId, int componentId, int border, int slotId, String message) {
        sendInterfaceMessage(interfaceId, componentId, border, slotId, message, true);
    }
    @Override public void sendInterfaceMessage(int interfaceId, int componentId, int border, int slotId, String message, boolean sendGameMessage) {
        if (sendGameMessage) message("sendInterfaceMessage", 0, message, null);
        script("sendInterfaceMessage", 7774, new Object[] {message, boundHash(interfaceId, componentId), slotId, border});
    }
    @Override public void sendBobInventoryMessage(int border, int slotId, String message) { sendInterfaceMessage(662, 5, border, slotId, message); }
    @Override public void sendMainInterfaceMessage(int border, String message, boolean sendGameMessage) {
        script("sendMainInterfaceMessage", 1211, new Object[] {message, 5, -120, border});
        if (sendGameMessage) message("sendMainInterfaceMessage", 0, (border == 0 ? "" : "<col=ff0000>") + message, null);
    }
    @Override public void sendWarning(String string) {
        script("sendWarning", 1211, new Object[] {"<col=FF0000>WARNING: <col=ffffff>" + string, -20, -20, 1});
    }
    @Override public void sendInterFlashScript(int interfaceId, int componentId, int width, int height, int slot) {
        script("sendInterFlashScript", 143, new Object[] {boundHash(interfaceId, componentId), width, height, slot});
    }
    @Override public void sendInterSetItemsOptionsScript(int interfaceId, int componentId, int key, int width, int height, String... options) {
        sendInterSetItemsOptionsScript(interfaceId, componentId, key, false, width, height, options);
    }
    @Override public void sendInterSetItemsOptionsScript(int interfaceId, int componentId, int key, boolean negativeKey, int width, int height, String... options) {
        script("sendInterSetItemsOptionsScript", negativeKey ? 695 : 150, itemOptions(interfaceId, componentId, key, width, height, options));
    }
    @Override public void sendSpoilsInterface(int interfaceId, int componentId, int key, int width, int height, String... options) {
        script("sendSpoilsInterface", 149, itemOptions(interfaceId, componentId, key, width, height, options));
    }
    /** Natural order of the 910 item-options composites: hash, key, width, height, 0, -1, options... */
    private static Object[] itemOptions(int interfaceId, int componentId, int key, int width, int height, String[] options) {
        Object[] args = new Object[6 + options.length];
        args[0] = boundHash(interfaceId, componentId);
        args[1] = key; args[2] = width; args[3] = height; args[4] = 0; args[5] = -1;
        System.arraycopy(options, 0, args, 6, options.length);
        return args;
    }
    @Override public void sendPouchInfusionOptionsScript(boolean dung, int interfaceId, int componentId, int slotLength, int width, int height, String... options) {
        script("sendPouchInfusionOptionsScript", 757, infusionOptions(dung, interfaceId, componentId, slotLength, width, height, options));
    }
    @Override public void sendScrollInfusionOptionsScript(boolean dung, int interfaceId, int componentId, int slotLength, int width, int height, String... options) {
        script("sendScrollInfusionOptionsScript", 763, infusionOptions(dung, interfaceId, componentId, slotLength, width, height, options));
    }
    /** Natural order of the 910 infusion composites: options..., hash, width, height, b, a. */
    private static Object[] infusionOptions(boolean dung, int interfaceId, int componentId, int slotLength, int width, int height, String[] options) {
        Object[] args = new Object[5 + options.length];
        System.arraycopy(options, 0, args, 0, options.length);
        int index = options.length;
        args[index++] = boundHash(interfaceId, componentId);
        args[index++] = width;
        args[index++] = height;
        args[index++] = dung ? 1100 : 1;
        args[index] = dung ? 1159 : slotLength;
        return args;
    }
    @Override public void sendrefreshComponentScript(int interfaceId, int componentId) {
        script("sendrefreshComponentScript", 3087, new Object[] {boundHash(interfaceId, componentId)});
    }
    @Override public void sendInputNameScript(String message) { inputScript("sendInputNameScript", 109, message); }
    @Override public void sendInputIntegerScript(String message) { inputScript("sendInputIntegerScript", 108, message); }
    @Override public void sendInputLongTextScript(String message) { inputScript("sendInputLongTextScript", 110, message); }
    private void inputScript(String method, int scriptId, String message) {
        // The native constructor does not build an InterfaceManager; the input box itself
        // is opened through this facade when one exists.
        if (getPlayer().getInterfaceManager() != null) getPlayer().getInterfaceManager().sendInputTextInterface();
        script(method, scriptId, new Object[] {message});
    }

    // ================================================================ REAL: containers

    @Override public void sendItems(int key, ItemsContainer<Item> items) { containerFull("sendItems", key, key < 0, items.getItems()); }
    @Override public void sendItems(int key, boolean negativeKey, ItemsContainer<Item> items) { containerFull("sendItems", key, negativeKey, items.getItems()); }
    @Override public void sendItems(int key, Item[] items) { containerFull("sendItems", key, key < 0, items); }
    @Override public void sendItems(int key, boolean negativeKey, Item[] items) { containerFull("sendItems", key, negativeKey, items); }
    @Override public void sendItems(int key, boolean negativeKey, int... items) {
        Item[] wrapped = new Item[items.length];
        for (int i = 0; i < items.length; i++) wrapped[i] = items[i] < 0 ? null : new Item(items[i], 1);
        containerFull("sendItems", key, negativeKey, wrapped);
    }
    @Override public void resetItems(int key, boolean negativeKey, int size) { containerFull("resetItems", key, negativeKey, new Item[size]); }

    @Override public void sendUpdateItems(int key, ItemsContainer<Item> items, int... slots) { sendUpdateItems(key, items.getItems(), slots); }
    @Override public void sendUpdateItems(int key, Item[] items, int... slots) { sendUpdateItems(key, key < 0, items, slots); }
    @Override public void sendUpdateItems(final int key, final boolean negativeKey, Item[] items, int... slots) {
        warnNisVarsOnce();
        int count = 0;
        for (int slot : slots) if (slot >= 0 && slot < items.length) count++;
        final int[] slotIds = new int[count], ids = new int[count], amounts = new int[count];
        int index = 0;
        for (int slot : slots) {
            if (slot < 0 || slot >= items.length) continue; // 910 skips out-of-range slots too
            Item item = items[slot];
            slotIds[index] = slot;
            if (item == null || item.getId() < 0 || item.getAmount() <= 0) {
                ids[index] = -1; amounts[index] = 0;
            } else {
                ids[index] = item.getId(); amounts[index] = item.getAmount();
            }
            index++;
        }
        real("sendUpdateItems", new Body() {
            @Override public Native950Packets.Packet build() {
                return Native950Packets.inventorySlots(containerKey(key), negativeKey, slotIds, ids, amounts);
            }
        }, key, negativeKey, slotIds);
    }

    // ================================================================ REAL: session

    /** Native202 resets the client session; final checkpoint remains owned by Native950Session.close. */
    @Override public void sendLogout() {
        Channel channel = getPlayer().getRealChannel();
        if (channel == null) { drop("sendLogout", "no channel attached"); return; }
        counters.recordSent("sendLogout");
        getPlayer().setActive(false);
        // Closing immediately can discard pending bytes on a real asynchronous socket.
        // The close future still drives the world's one save/remove path, even on write failure.
        channel.writeAndFlush(Native950Packets.logoutFull()).addListener(ChannelFutureListener.CLOSE);
    }

    // ================================================================ NO-OP: owned by Native950Session

    @Override public void sendServerTickEndPacket() { noop("sendServerTickEndPacket"); }
    @Override public void sendNoTimeOut() { noop("sendNoTimeOut"); }
    @Override public void sendLocalPlayersUpdate() { noop("sendLocalPlayersUpdate"); }
    @Override public void sendLocalNPCsUpdate() { noop("sendLocalNPCsUpdate"); }
    @Override public void sendWorldList(int clientChecksum, int[] online) { noop("sendWorldList"); }
    /** Player.loadMapRegions already raises the native map-refresh flag; there is no public setter. */
    @Override public void sendMapRegion() { noop("sendMapRegion"); }

    // ================================================================ NO-OP: cosmetic, opcode unverified

    @Override public void sendSound(int id, int delay, int effectType) { noop("sendSound"); }
    @Override public void sendVoice(int id) { noop("sendVoice"); }
    @Override public void resetSounds() { noop("resetSounds"); }
    @Override public void sendVorbisSound(int id, int delay) { noop("sendVorbisSound"); }
    @Override public void sendVorbisSound(int id, int delay, int volume) { noop("sendVorbisSound"); }
    @Override public void sendAudioVolumeBootstrap() { noop("sendAudioVolumeBootstrap"); }
    @Override public boolean sendNxtVorbisAudioFallback(int id, int delay, int volume, boolean report) { noop("sendNxtVorbisAudioFallback"); return false; }
    @Override public void sendVorbisSpeachSound(int id, int delay) { noop("sendVorbisSpeachSound"); }
    @Override public void sendMusicEffect(int id) { noop("sendMusicEffect"); }
    @Override public void sendMusicEffectTest(int id) { noop("sendMusicEffectTest"); }
    @Override public void sendMusic(int id) { noop("sendMusic"); }
    @Override public void sendMusic(int id, int delay, int volume) { noop("sendMusic"); }
    @Override public void sendMusicTest(int id, int delay, int volume) { noop("sendMusicTest"); }
    @Override public void sendNxtMusicTest(int id, int volume) { noop("sendNxtMusicTest"); }
    @Override public void sendNxtMusicEffectTest(int id) { noop("sendNxtMusicEffectTest"); }
    @Override public void sendAncientXNxtMusicTest(int id, int delay, int volume) { noop("sendAncientXNxtMusicTest"); }
    @Override public boolean canSendLegacyMusicArchive(int archiveId) { noop("canSendLegacyMusicArchive"); return false; }
    @Override public boolean canSendLegacyMusicEffectArchive(int archiveId) { noop("canSendLegacyMusicEffectArchive"); return false; }
    @Override public boolean canSendNxtMusicArchive(int archiveId) { noop("canSendNxtMusicArchive"); return false; }
    @Override public boolean canSendVorbisSoundArchive(int archiveId) { noop("canSendVorbisSoundArchive"); return false; }

    @Override public void sendCameraShake(int slotId, int b, int c, int d, int e) { noop("sendCameraShake"); }
    @Override public void sendStopCameraShake() { noop("sendStopCameraShake"); }
    @Override public void sendCameraLook(int viewLocalX, int viewLocalY, int viewZ) { noop("sendCameraLook"); }
    @Override public void sendCameraLook(int viewLocalX, int viewLocalY, int viewZ, int speed1, int speed2) { noop("sendCameraLook"); }
    @Override public void sendResetCamera() { noop("sendResetCamera"); }
    @Override public void sendCameraRotation(int x, int y) { noop("sendCameraRotation"); }
    @Override public void sendCameraPos(int moveLocalX, int moveLocalY, int moveZ) { noop("sendCameraPos"); }
    @Override public void sendCameraPos(int moveLocalX, int moveLocalY, int moveZ, int speed1, int speed2) { noop("sendCameraPos"); }
    @Override public void sendCutscene(int id) { noop("sendCutscene"); }
    @Override public void sendBlackOut(int area) { noop("sendBlackOut"); }
    @Override public void sendMiniMapStatus(int area) { noop("sendMiniMapStatus"); }
    @Override public void sendMinimapFlag(int x, int y) { noop("sendMinimapFlag"); }
    @Override public void sendResetMinimapFlag() { noop("sendResetMinimapFlag"); }
    @Override public void sendHintIcon(HintIcon icon) { noop("sendHintIcon"); }

    @Override public void sendOpenURL(String url) { noop("sendOpenURL"); }
    @Override public void sendSetMouse(String walkHereReplace, int cursor) { noop("sendSetMouse"); }
    @Override public void sendClientConsoleCommand(String command) { noop("sendClientConsoleCommand"); }
    @Override public void sendStoreServerPermVarcs() { noop("sendStoreServerPermVarcs"); }
    @Override public void sendSystemUpdate(int delay) { noop("sendSystemUpdate"); }
    @Override public void sendPlayerOption(String option, int slot, boolean top) { noop("sendPlayerOption"); }
    @Override public void sendPlayerOption(String option, int slot, boolean top, int cursor) { noop("sendPlayerOption"); }
    @Override public void sendPlayerUnderNPCPriority(boolean priority) { noop("sendPlayerUnderNPCPriority"); }
    @Override public void sendCurrentTarget(Entity target) { noop("sendCurrentTarget"); }
    @Override public void sendTileMessage(String message, WorldTile tile, int color) { noop("sendTileMessage"); }
    @Override public void sendTileMessage(String message, WorldTile tile, int delay, int height, int color) { noop("sendTileMessage"); }
    @Override public void sendEnvironmentOverridePacket(int regionId) { noop("sendEnvironmentOverridePacket"); }

    @Override public void sendIComponentSprite(int interfaceId, int componentId, int spriteId) { noop("sendIComponentSprite"); }
    @Override public void sendIComponentModel(int interfaceId, int componentId, int modelId) { noop("sendIComponentModel"); }
    @Override public void sendIComponentAnimation(int emoteId, int interfaceId, int componentId) { noop("sendIComponentAnimation"); }
    @Override public void sendIComponentColour(int interfaceId, int componentId, int colour) { noop("sendIComponentColour"); }
    @Override public void ifRetex(int widget, int component, int slot, int id, boolean hidden) { noop("ifRetex"); }
    @Override public void sendMoveIComponent(int interfaceId, int componentId, int x, int y) { noop("sendMoveIComponent"); }
    @Override public void moveInterface(int fromParentUID, int toParentUID) { noop("moveInterface"); }
    @Override public void sendItemOnIComponent(int interfaceid, int componentId, int id) { noop("sendItemOnIComponent"); }
    @Override public void sendItemOnIComponent(int interfaceid, int componentId, int id, int amount) { noop("sendItemOnIComponent"); }
    @Override public void sendEntityOnIComponent(boolean isPlayer, int entityId, int interfaceId, int componentId) { noop("sendEntityOnIComponent"); }
    @Override public void sendPlayerOnIComponent(int interfaceId, int componentId) { noop("sendPlayerOnIComponent"); }
    @Override public void sendNPCOnIComponent(int interfaceId, int componentId, int npcId) { noop("sendNPCOnIComponent"); }
    @Override public void sendOtherPlayerOnIComponent(int interfaceId, int componentId, Player p2) { noop("sendOtherPlayerOnIComponent"); }
    @Override public void sendCustomPlayerOnIComponent(int interfaceId, int componentId, int customIndex) { noop("sendCustomPlayerOnIComponent"); }
    @Override public void sendAppearenceLook() { noop("sendAppearenceLook"); }
    @Override public void sendAppearanceLook(Item[] items, int[] look) { noop("sendAppearanceLook"); }
    @Override public void sendCustomPlayerAppearanceLook(Item[] items, int[] look, int customIndex) { noop("sendCustomPlayerAppearanceLook"); }

    // Already empty in 910; counted so the census stays honest.
    @Override public void sendIComponentTransparency(int widgetID, byte transparency) { noop("sendIComponentTransparency"); }
    @Override public void sendIComponentTransparency(int widgetID, int componentID, byte transparency) { noop("sendIComponentTransparency"); }
    @Override public void sendSettingPacket(int setting, boolean value) { noop("sendSettingPacket"); }
    @Override public void sendRandomOnIComponent(int interfaceId, int componentId, int id) { noop("sendRandomOnIComponent"); }
    @Override public void sendFaceOnIComponent(int interfaceId, int componentId, int look1, int look2, int look3) { noop("sendFaceOnIComponent"); }
    @Override public void sendUnlockIgnoreList() { noop("sendUnlockIgnoreList"); }
    @Override public void sendItemsLook() { noop("sendItemsLook"); }

    // ================================================================ STRICT: entity / zone-relative

    @Override public void sendSlayerMasterUpdate(NPC n, int id) { strict("sendSlayerMasterUpdate", "NPC_INFO masks are session-owned", id); }
    @Override public void sendGraphics(Graphics graphics, Object target) { strict("sendGraphics", "entity/tile spotanim opcode unverified", graphics == null ? null : graphics.getId()); }
    @Override public void sendDynamicMapRegion(boolean sendLswp) { strict("sendDynamicMapRegion", "REBUILD_REGION unverified", sendLswp); }

    /** In lenient mode the returned stream is a harmless sink: {@link #write} refuses it anyway. */
    @Override public OutputStream createWorldTileStream(WorldTile tile) {
        strict("createWorldTileStream", "legacy raw zone streams are sealed; native ground items are session-owned", tile);
        return new OutputStream(4);
    }
    // Region/FloorItem broadcasts are observed by Native950GroundItemsView after all world
    // mutations. Sending immediately here would duplicate equal-ID piles and race scene rebuilds.
    @Override public void sendGroundItem(FloorItem item) { noop("sendGroundItem"); }
    @Override public void sendRemoveGroundItem(FloorItem item) { noop("sendRemoveGroundItem"); }
    // Region owns object mutation and collision; the session publishes its final state once.
    @Override public void sendSpawnedObject(WorldObject object) { noop("sendSpawnedObject"); }
    @Override public void addSpawnedObject(WorldObject object) { strict("addSpawnedObject", "private player-only objects are not admitted to the shared Region projection", object == null ? null : object.getId()); }
    @Override public void sendDestroyObject(WorldObject object) { noop("sendDestroyObject"); }
    @Override public void sendObjectAnimation(WorldObject object, Animation animation) { strict("sendObjectAnimation", "LOC_ANIM unverified", object == null ? null : object.getId()); }
    @Override public void sendStillProjectile(WorldTile receiver, WorldTile startTile, WorldTile endTile, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset, int creatorSize) {
        strict("sendStillProjectile", "MAP_PROJANIM unverified", gfxId);
    }
    @Override public void sendProjectile(Entity receiver, WorldTile startTile, WorldTile endTile, int gfxId, int startHeight, int endHeight, int speed, int delay, int curve, int startDistanceOffset, int creatorSize) {
        strict("sendProjectile", "MAP_PROJANIM unverified", gfxId);
    }
    @Override public void sendTestProjectile(NewProjectile projectile) { strict("sendTestProjectile", "MAP_PROJANIM unverified"); }
    @Override public void sendTestProjectile(Projectile projectile) { strict("sendTestProjectile", "MAP_PROJANIM unverified"); }
    @Override public void sendStillProjectileNew(WorldTile from, int fromSizeX, int fromSizeY, WorldTile to, int toSizeX, int toSizeY, Entity lockOn, int gfxId, int startHeight, int endHeight, int startTime, int endTime) {
        strict("sendStillProjectileNew", "MAP_PROJANIM unverified", gfxId);
    }
    @Override public void sendProjectileProperNew(WorldTile from, int fromSizeX, int fromSizeY, WorldTile to, int toSizeX, int toSizeY, Entity lockOn, int gfxId, int startHeight, int endHeight, int startTime, int endTime, int slope, int angle) {
        strict("sendProjectileProperNew", "MAP_PROJANIM unverified", gfxId);
    }

    // ================================================================ STRICT: active-interface openers

    @Override public void sendFloorItemInterface(FloorItem item, boolean nocliped, int windowId, int windowComponentId, int interfaceId) {
        strict("sendFloorItemInterface", "IF_OPENSUB_ACTIVE_OBJ unverified", interfaceId);
    }
    @Override public void sendPlayerInterface(Player p, boolean nocliped, int windowId, int windowComponentId, int interfaceId) {
        strict("sendPlayerInterface", "IF_OPENSUB_ACTIVE_PLAYER unverified", interfaceId);
    }
    @Override public void sendNPCInterface(Entity npc, boolean nocliped, int windowId, int windowComponentId, int interfaceId) {
        strict("sendNPCInterface", "IF_OPENSUB_ACTIVE_NPC unverified", interfaceId);
    }
    @Override public void sendObjectInterface(WorldObject object, boolean nocliped, int windowId, int windowComponentId, int interfaceId) {
        strict("sendObjectInterface", "IF_OPENSUB_ACTIVE_LOC unverified", interfaceId);
    }
    @Override public void sendEntityInterface(Entity entity, boolean clickThrought, int windowId, int windowComponentId, int interfaceId) {
        strict("sendEntityInterface", "IF_OPENSUB_ACTIVE_* unverified", interfaceId);
    }

    // ================================================================ REAL (M3): stats, vitals, run energy
    //
    // P4 parked these at NO-OP+counter because their packets were unverified.
    // Protocol S verified all of them (UPDATE_STAT 66, CLIENT_SETVARC_SMALL 1 /
    // _LARGE 112, CLIENT_SETVARCSTR_SMALL 67 / _LARGE 15, UPDATE_RUNENERGY 116,
    // UPDATE_RUNWEIGHT 108), so M3 promotes them to REAL. A verified opcode is not
    // a licence to write a 910 id: every varc still goes through the binding table
    // and is a counted drop while it is undeclared, which today is every varc the
    // 910 content emits (see notes/M3-bindings.md section 3).

    /**
     * Highest skill index enum 680 shares with Ataraxia. The 947 enum has 29 entries
     * and, since the skill model was rebuilt against the cache stat definitions, so
     * does {@code Skills.SKILL_NAME} - 27 Archaeology and 28 Necromancy included - so
     * this simply follows {@code Skills.SKILL_COUNT} and never needs editing again.
     * Anything outside the range is still a counted drop rather than a byte with an
     * invented meaning: the client does not bounds-check the skill index, so a wrong
     * one writes a real stat slot, and the live stat table holds exactly one entry per
     * stat definition. (ui/SKILLS_TAB.md s5, ui/STAT_DEFINITIONS.md s7.)
     */
    static final int MAX_MODELLED_SKILL = com.rs.game.player.Skills.SKILL_COUNT - 1;

    /**
     * The client stores WHOLE experience in the live stat table and setter 0x140369C10
     * clamps a flag-zero entry - which is what that table holds, built at 0x1400CDF25 -
     * at 0x0BEBC200 = 200,000,000, so that is the highest value it can hold. The value
     * is clamped here instead of being handed to the writer, which would reject it and
     * drop the whole stat update: a clamped bar is what the client would show anyway,
     * a dropped packet leaves the skill blank. {@code Skills.MAXIMUM_EXP} is now the
     * same 200,000,000, so this clamp is normally a no-op and only catches a value that
     * reached the engine some other way.
     */
    static final int MAX_TRANSMITTABLE_XP = 200000000;

    /**
     * UPDATE_STAT (opcode 66) carries the experience and the BOOSTED/DRAINED current
     * level; the client derives the base level from the experience itself (script
     * 11849 over enum 10866) and computes total and combat level on its own, so the
     * base level must never be sent. {@code Skills.getLevel} is the boosted value and
     * {@code Skills.getLevelForXp} is the base, matching the 910 packet's own choice.
     */
    @Override public void sendSkillLevel(final int skill) {
        if (skill < 0 || skill > MAX_MODELLED_SKILL) {
            drop("sendSkillLevel", "skill " + skill + " is outside the 0.." + MAX_MODELLED_SKILL
                    + " range enum 680 shares with Ataraxia", skill);
            return;
        }
        com.rs.game.player.Skills skills = getPlayer().getSkills();
        if (skills == null) { drop("sendSkillLevel", "the player has no Skills instance", skill); return; }
        final int level = skills.getLevel(skill);
        double raw = skills.getXp(skill);
        final int experience = raw <= 0 ? 0 : raw >= MAX_TRANSMITTABLE_XP ? MAX_TRANSMITTABLE_XP : (int) raw;
        real("sendSkillLevel", new Body() {
            @Override public Native950Packets.Packet build() {
                return Native950Packets.updateStat(skill, level, experience);
            }
        }, skill, level, experience);
    }

    /** UPDATE_RUNENERGY (opcode 116): one untransformed percentage byte read back by cs2 op 731. */
    @Override public void sendRunEnergy() {
        int energy = getPlayer().getRunEnergy();
        final int percent = energy < 0 ? 0 : energy > 100 ? 100 : energy;
        real("sendRunEnergy", new Body() {
            @Override public Native950Packets.Packet build() {
                return Native950Packets.runEnergy(percent);
            }
        }, percent);
    }

    /** UPDATE_RUNWEIGHT (opcode 108): big-endian signed short in kilograms, as the 910 packet sent. */
    @Override public void refreshWeight() {
        double weight = getPlayer().getWeight();
        final int kilograms = weight <= -32768 ? -32768 : weight >= 32767 ? 32767 : (int) weight;
        real("refreshWeight", new Body() {
            @Override public Native950Packets.Packet build() {
                return Native950Packets.runWeight(kilograms);
            }
        }, kilograms);
    }

    @Override public void sendGlobalConfig(int id, int value) { varc("sendGlobalConfig", id, value, false, false); }
    @Override public void sendGlobalConfigSmall(int id, int value) { varc("sendGlobalConfigSmall", id, value, false, true); }
    @Override public void sendGlobalConfigLarge(int id, int value) { varc("sendGlobalConfigLarge", id, value, true, false); }
    /** 910 {@code sendCSVarInteger} delegates to {@code sendGlobalConfig}; so does this. */
    @Override public void sendCSVarInteger(int id, int value) { varc("sendCSVarInteger", id, value, false, false); }
    @Override public void sendGlobalString(int id, String string) { varcString("sendGlobalString", id, string); }
    @Override public void sendCSVarString(int id, String string) { varcString("sendCSVarString", id, string); }

    // ================================================================ STRICT: input boxes / GE

    @Override public void sendIComponentInputInteger(int interfaceId, int componentId, int length) { strict("sendIComponentInputInteger", "input box varcs 2235..2237 unverified", interfaceId, componentId); }
    @Override public void sendIComponentInputText(int interfaceId, int componentId, int length) { strict("sendIComponentInputText", "input box varcs 2235..2237 unverified", interfaceId, componentId); }
    @Override public void sendGEItemSearch() { strict("sendGEItemSearch", "input box varcs 2235..2237 unverified"); }
    @Override public void sendGrandExchangeOffer(Offer offer) { strict("sendGrandExchangeOffer", "UPDATE_GE_OFFER unverified"); }

    // ================================================================ STRICT: social

    @Override public void sendPublicMessage(Player p, PublicChatMessage message) { strict("sendPublicMessage", "MESSAGE_PUBLIC unverified"); }
    @Override public void sendPublicMessage(Player p, PublicChatMessage message, int customIcon) { strict("sendPublicMessage", "MESSAGE_PUBLIC unverified"); }
    @Override public void sendFriendsChatChannel() { strict("sendFriendsChatChannel", "friend chat opcodes unverified"); }
    @Override public void sendFriends() { strict("sendFriends", "UPDATE_FRIENDLIST unverified"); }
    @Override public void sendFriend(String username, String displayName, int world, boolean putOnline, boolean warnMessage) { strict("sendFriend", "UPDATE_FRIENDLIST unverified", username); }
    @Override public void sendFriend(String username, String displayName, int world, boolean putOnline, boolean warnMessage, OutputStream stream) { strict("sendFriend", "UPDATE_FRIENDLIST unverified", username); }
    @Override public void sendIgnores() { strict("sendIgnores", "UPDATE_IGNORELIST unverified"); }
    @Override public void sendIgnore(String name, String display, boolean updateName) { strict("sendIgnore", "UPDATE_IGNORELIST unverified", name); }
    @Override public void sendPrivateMessage(String username, String message) { strict("sendPrivateMessage", "MESSAGE_PRIVATE_ECHO unverified", username); }
    @Override public void receivePrivateMessage(String name, String display, int rights, String message) { strict("receivePrivateMessage", "MESSAGE_PRIVATE unverified", name); }
    @Override public void receivePrivateChatQuickMessage(String name, String display, int rights, QuickChatMessage message) { strict("receivePrivateChatQuickMessage", "MESSAGE_QUICKCHAT_PRIVATE unverified", name); }
    @Override public void sendPrivateQuickMessageMessage(String username, QuickChatMessage message) { strict("sendPrivateQuickMessageMessage", "MESSAGE_QUICKCHAT_PRIVATE_ECHO unverified", username); }
    @Override public void receiveFriendChatMessage(String name, String display, int rights, String chatName, String message) { strict("receiveFriendChatMessage", "MESSAGE_FRIENDCHANNEL unverified", name); }
    @Override public void receiveFriendChatQuickMessage(String name, String display, int rights, String chatName, QuickChatMessage message) { strict("receiveFriendChatQuickMessage", "MESSAGE_QUICKCHAT_FRIENDCHANNEL unverified", name); }
    @Override public void sendClanChannel(ClansManager manager, boolean myClan) { strict("sendClanChannel", "CLANCHANNEL_FULL unverified", myClan); }
    @Override public void sendClanSettings(ClansManager manager, boolean myClan) { strict("sendClanSettings", "CLANSETTINGS_FULL unverified", myClan); }
    @Override public void receiveClanChatQuickMessage(boolean myClan, String display, int rights, QuickChatMessage message) { strict("receiveClanChatQuickMessage", "MESSAGE_QUICKCHAT_CLANCHANNEL unverified", display); }
    @Override public void receiveClanChatMessage(boolean myClan, String display, int rights, ChatMessage message) { strict("receiveClanChatMessage", "MESSAGE_CLANCHANNEL unverified", display); }
    @Override public void sendOtherGameBarStages() { strict("sendOtherGameBarStages", "CHAT_FILTER_SETTINGS unverified"); }
    @Override public void sendPrivateGameBarStage() { strict("sendPrivateGameBarStage", "CHAT_FILTER_SETTINGS_PRIVATECHAT unverified"); }

    // ================================================================ counters

    /**
     * Thread-safe per-method tallies. {@code sent} counts REAL packets handed to
     * the channel, {@code noops} the cosmetic tier, {@code dropped} writer rejections,
     * missing channels and lenient STRICT hits, and {@code strictHits} every STRICT
     * call regardless of mode (so "strict-tier counters == 0" is checkable in both).
     */
    public static final class Counters {
        private final ConcurrentHashMap<String, AtomicLong> sent = new ConcurrentHashMap<String, AtomicLong>();
        private final ConcurrentHashMap<String, AtomicLong> noops = new ConcurrentHashMap<String, AtomicLong>();
        private final ConcurrentHashMap<String, AtomicLong> dropped = new ConcurrentHashMap<String, AtomicLong>();
        private final ConcurrentHashMap<String, AtomicLong> strictHits = new ConcurrentHashMap<String, AtomicLong>();
        /** method -> (discarded id -> count), so a counted NO-OP or drop still names what it swallowed. */
        private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, AtomicLong>> noopIds =
                new ConcurrentHashMap<String, ConcurrentHashMap<Integer, AtomicLong>>();
        /**
         * The same discarded traffic keyed by <b>method and target</b>, where a target is
         * "interface:component" when the call named both and just the id when it named one.
         *
         * <p>Why this exists alongside the id histogram: bucketing by method alone answers "a
         * call failed" but never "which panel is broken". A session that drops twelve
         * sendIComponentText calls tells you nothing actionable; the same session bucketed by
         * target names twelve components and is a backlog. That is the difference between a
         * login-and-walk being an impression and being a measurement, which is the whole point
         * of the counted-drop rule.
         */
        private final ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>> discardedTargets =
                new ConcurrentHashMap<String, ConcurrentHashMap<String, AtomicLong>>();
        /**
         * The dispatcher methods whose first two arguments really are (interface, component), and
         * so the only ones whose drops may be recorded as a pair.
         *
         * <p>This is an explicit list rather than "the first two arguments are both ints" on
         * purpose. The var family also takes two ints, but they are an id and a <b>value</b>:
         * inferring would file {@code sendVarBit(41524, 300)} under target "41524:300", which is
         * wrong, and would grow a fresh histogram row for every value ever written.
         */
        private static final java.util.Set<String> COMPONENT_SHAPED =
                Collections.unmodifiableSet(new java.util.HashSet<String>(java.util.Arrays.asList(
                        "sendIComponentText", "sendEmptyTextToComponents", "sendHideIComponent",
                        "sendInterfaceConfig", "sendHideComponents", "sendText",
                        "sendIComponentSettings", "sendIComponentSprite", "sendIComponentModel")));

        Counters() { }

        void recordSent(String method) { bump(sent, method); }
        void recordNoop(String method) { bump(noops, method); }

        /**
         * Records the id a counted NO-OP or a counted drop discarded; ignores a
         * non-numeric first argument. Both tiers share one histogram because both
         * mean "this id never reached the 947 wire", which is the question the
         * unmatched-id inventory asks.
         */
        /**
         * Records the (interface, component) a counted NO-OP or drop discarded. The pair form is
         * used only when the first two arguments are both numeric, which is exactly the
         * convention every interface-shaped dispatcher method already follows; anything else
         * records the single id so no drop is left unattributed.
         */
        void recordDiscardedTarget(String method, Object first, Object second) {
            if (!(first instanceof Integer)) return;
            String target = second instanceof Integer && COMPONENT_SHAPED.contains(method)
                    ? (first + ":" + second) : String.valueOf(first);
            ConcurrentHashMap<String, AtomicLong> perMethod = discardedTargets.get(method);
            if (perMethod == null) {
                ConcurrentHashMap<String, AtomicLong> fresh = new ConcurrentHashMap<String, AtomicLong>();
                perMethod = discardedTargets.putIfAbsent(method, fresh);
                if (perMethod == null) perMethod = fresh;
            }
            AtomicLong counter = perMethod.get(target);
            if (counter == null) {
                AtomicLong fresh = new AtomicLong();
                counter = perMethod.putIfAbsent(target, fresh);
                if (counter == null) counter = fresh;
            }
            counter.incrementAndGet();
        }

        /** Sorted, immutable target histogram for one method. */
        public Map<String, Long> discardedTargets(String method) {
            ConcurrentHashMap<String, AtomicLong> perMethod = discardedTargets.get(method);
            TreeMap<String, Long> out = new TreeMap<String, Long>();
            if (perMethod != null)
                for (Map.Entry<String, AtomicLong> e : perMethod.entrySet()) out.put(e.getKey(), e.getValue().get());
            return Collections.unmodifiableMap(out);
        }

        /** The whole discarded-target inventory, method by method. */
        public Map<String, Map<String, Long>> discardedTargetsByMethod() {
            TreeMap<String, Map<String, Long>> out = new TreeMap<String, Map<String, Long>>();
            for (String method : discardedTargets.keySet()) out.put(method, discardedTargets(method));
            return Collections.unmodifiableMap(out);
        }

        /**
         * The drop census as one line per method, each naming its targets worst first. This is
         * the line to read after a login-and-walk session: every entry is a panel to fix.
         */
        public String discardedTargetReport() {
            Map<String, Map<String, Long>> all = discardedTargetsByMethod();
            if (all.isEmpty()) return "no discarded targets";
            StringBuilder text = new StringBuilder();
            for (Map.Entry<String, Map<String, Long>> method : all.entrySet()) {
                if (text.length() > 0) text.append("; ");
                text.append(method.getKey()).append('{');
                boolean first = true;
                for (Map.Entry<String, Long> target : sortedByCount(method.getValue())) {
                    if (!first) text.append(", ");
                    first = false;
                    text.append(target.getKey()).append('=').append(target.getValue());
                }
                text.append('}');
            }
            return text.toString();
        }

        private static java.util.List<Map.Entry<String, Long>> sortedByCount(Map<String, Long> counts) {
            java.util.List<Map.Entry<String, Long>> out =
                    new java.util.ArrayList<Map.Entry<String, Long>>(counts.entrySet());
            Collections.sort(out, new java.util.Comparator<Map.Entry<String, Long>>() {
                @Override public int compare(Map.Entry<String, Long> a, Map.Entry<String, Long> b) {
                    int byCount = b.getValue().compareTo(a.getValue());
                    return byCount != 0 ? byCount : a.getKey().compareTo(b.getKey());
                }
            });
            return out;
        }

        void recordDiscardedId(String method, Object id) {
            if (!(id instanceof Integer)) return;
            ConcurrentHashMap<Integer, AtomicLong> perMethod = noopIds.get(method);
            if (perMethod == null) {
                ConcurrentHashMap<Integer, AtomicLong> fresh = new ConcurrentHashMap<Integer, AtomicLong>();
                perMethod = noopIds.putIfAbsent(method, fresh);
                if (perMethod == null) perMethod = fresh;
            }
            AtomicLong counter = perMethod.get(id);
            if (counter == null) {
                AtomicLong fresh = new AtomicLong();
                counter = perMethod.putIfAbsent((Integer) id, fresh);
                if (counter == null) counter = fresh;
            }
            counter.incrementAndGet();
        }

        /** Sorted, immutable id histogram for one method's discarded traffic; alias of {@link #noopIds(String)}. */
        public Map<Integer, Long> discardedIds(String method) { return noopIds(method); }

        /** The whole discarded-id inventory; alias of {@link #noopIdsByMethod()}. */
        public Map<String, Map<Integer, Long>> discardedIdsByMethod() { return noopIdsByMethod(); }

        /**
         * Sorted, immutable id histogram for one method (empty when it carries no id).
         * Covers the counted NO-OP tier and, since M3 promoted the varc/stat families,
         * the counted drop tier as well; the name is kept for the existing snapshot
         * and smoke consumers. Prefer {@link #discardedIds(String)} in new code.
         */
        public Map<Integer, Long> noopIds(String method) {
            ConcurrentHashMap<Integer, AtomicLong> perMethod = noopIds.get(method);
            TreeMap<Integer, Long> out = new TreeMap<Integer, Long>();
            if (perMethod != null)
                for (Map.Entry<Integer, AtomicLong> e : perMethod.entrySet()) out.put(e.getKey(), e.getValue().get());
            return Collections.unmodifiableMap(out);
        }

        /** The whole discarded-id inventory, method by method. */
        public Map<String, Map<Integer, Long>> noopIdsByMethod() {
            TreeMap<String, Map<Integer, Long>> out = new TreeMap<String, Map<Integer, Long>>();
            for (String method : noopIds.keySet()) out.put(method, noopIds(method));
            return Collections.unmodifiableMap(out);
        }

        void recordDropped(String method) { bump(dropped, method); }
        void recordStrict(String method) { bump(strictHits, method); }

        public long sent(String method) { return get(sent, method); }
        public long noops(String method) { return get(noops, method); }
        public long dropped(String method) { return get(dropped, method); }
        public long strictHits(String method) { return get(strictHits, method); }

        public long totalSent() { return total(sent); }
        public long totalNoops() { return total(noops); }
        public long totalDropped() { return total(dropped); }
        public long totalStrictHits() { return total(strictHits); }

        /** Sorted, immutable copies for diagnostics snapshots. */
        public Map<String, Long> sentByMethod() { return copy(sent); }
        public Map<String, Long> noopsByMethod() { return copy(noops); }
        public Map<String, Long> droppedByMethod() { return copy(dropped); }
        public Map<String, Long> strictHitsByMethod() { return copy(strictHits); }

        private static void bump(ConcurrentHashMap<String, AtomicLong> map, String method) {
            AtomicLong counter = map.get(method);
            if (counter == null) {
                AtomicLong fresh = new AtomicLong();
                counter = map.putIfAbsent(method, fresh);
                if (counter == null) counter = fresh;
            }
            counter.incrementAndGet();
        }

        private static long get(ConcurrentHashMap<String, AtomicLong> map, String method) {
            AtomicLong counter = map.get(method);
            return counter == null ? 0 : counter.get();
        }

        private static long total(ConcurrentHashMap<String, AtomicLong> map) {
            long total = 0;
            for (AtomicLong counter : map.values()) total += counter.get();
            return total;
        }

        private static Map<String, Long> copy(ConcurrentHashMap<String, AtomicLong> map) {
            TreeMap<String, Long> out = new TreeMap<String, Long>();
            for (Map.Entry<String, AtomicLong> entry : map.entrySet()) out.put(entry.getKey(), entry.getValue().get());
            return Collections.unmodifiableMap(out);
        }

        @Override public String toString() {
            return "Native950 packets[sent=" + totalSent() + ",noops=" + totalNoops()
                    + ",dropped=" + totalDropped() + ",strict=" + totalStrictHits() + "]";
        }
    }
}
