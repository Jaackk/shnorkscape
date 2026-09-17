package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/** Presents the existing DialogueManager's current page on the paired 947 cache. */
public final class Native950Dialogues {
    private static final int ROOT = 1477;
    private static final int HOST = 750; // Struct 21303/3505, Dialogue Box slot 1006.
    private static volatile boolean verified;
    private final Player player;
    private final Channel channel;
    private final Runnable cancelQuantity;
    private final Runnable verifier;
    private int renderedInterface = -1;
    private int optionCount;
    private boolean responsePending;

    public Native950Dialogues(Player player, Channel channel, Runnable cancelQuantity) {
        this(player, channel, cancelQuantity, Native950Dialogues::verify);
    }

    Native950Dialogues(Player player, Channel channel, Runnable cancelQuantity, Runnable verifier) {
        this.player = Objects.requireNonNull(player, "player");
        this.channel = Objects.requireNonNull(channel, "channel");
        this.cancelQuantity = Objects.requireNonNull(cancelQuantity, "cancelQuantity");
        this.verifier = Objects.requireNonNull(verifier, "verifier");
    }

    public static boolean supports(int interfaceId) {
        return interfaceId == 1184 || interfaceId == 1191 || interfaceId == 1188 || interfaceId == 1186;
    }

    public boolean isOpen() { return renderedInterface != -1; }
    public boolean isOpen(int interfaceId) { return isOpen() && renderedInterface == interfaceId; }
    public int interfaceId() { return renderedInterface; }
    public void cancelInput() { cancelQuantity.run(); }

    /** Raw chatbox bridge; options are armed only after options() writes their visible rows. */
    public void open(int interfaceId) {
        if (!supports(interfaceId)) throw new IllegalArgumentException("Unverified 947 dialogue interface " + interfaceId);
        verifier.run(); // Cache mismatch must not close an input, change state or write a packet.
        cancelInput();
        channel.write(Native950Packets.openSub(ROOT, HOST, interfaceId, false));
        // Quantity teardown hides this shared wrapper. Script 1364's dialogue
        // branch changes its input property (0x8BE), not its hidden flag.
        // Reacquire visibility after cancellation and before refreshing layout.
        channel.write(Native950Packets.hideInterface(ROOT, 747, false));
        player.getInterfaceManager().registerNativeOpen(interfaceId, ROOT, HOST);
        renderedInterface = interfaceId;
        optionCount = 0;
        responsePending = interfaceId != 1188;
        refresh();
    }

    public void speech(boolean playerSpeech, int entityId, int animationId, String title,
                       boolean continueEnabled, String... texts) {
        int interfaceId = playerSpeech ? 1191 : 1184;
        if (!playerSpeech && entityId < 0) throw new IllegalArgumentException("NPC id must be nonnegative");
        if (animationId < -1) throw new IllegalArgumentException("Animation id must be -1 or nonnegative");
        Native950Packets.Packet name = Native950Packets.interfaceText(interfaceId, 4, title);
        Native950Packets.Packet text = Native950Packets.interfaceText(interfaceId, 10, join(texts));
        open(interfaceId);
        channel.write(name);
        channel.write(text);
        // Head setters remain isolated from the legacy packet facade. Their
        // paired-cache scripts are verified alongside the presentation below.
        renderHead(playerSpeech, entityId, animationId, interfaceId);
        setContinue(continueEnabled, 11, 15);
    }

    private void renderHead(boolean playerSpeech, int entityId, int animationId, int interfaceId) {
        int hash = (interfaceId << 16) | 8;
        if (playerSpeech) channel.write(Native950Packets.interfacePlayerHead(interfaceId, 8));
        else channel.write(Native950Packets.runClientScript(2374, hash, entityId));
        // 2374 supplies default animation 9806. Always replace it, including -1
        // when the shared dialogue explicitly asks for no animation.
        channel.write(Native950Packets.runClientScript(16429, animationId, hash));
    }

    public void options(String title, String... options) {
        Objects.requireNonNull(options, "options");
        if (options.length < 1 || options.length > 5)
            throw new IllegalArgumentException("A dialogue requires between one and five options");
        Object[] args = new Object[] {options.length, Objects.requireNonNull(title, "title"), "", "", "", "", ""};
        for (int i = 0; i < options.length; i++) {
            String option = Objects.requireNonNull(options[i], "option " + i);
            if (option.isEmpty()) throw new IllegalArgumentException("A visible option must not be empty");
            args[i + 2] = option;
        }
        Native950Packets.Packet render = Native950Packets.runClientScript(5589, args);
        open(1188);
        channel.write(render);
        optionCount = options.length;
        responsePending = true;
    }

    public void message(boolean continueEnabled, String text) {
        Native950Packets.Packet content = Native950Packets.interfaceText(1186, 3, text);
        open(1186);
        channel.write(content);
        setContinue(continueEnabled, 4, 8);
    }

    private void setContinue(boolean enabled, int graphic, int hitTarget) {
        channel.write(Native950Packets.hideInterface(renderedInterface, graphic, !enabled));
        channel.write(Native950Packets.hideInterface(renderedInterface, hitTarget, !enabled));
        responsePending = enabled;
    }

    /** Server-bound opcode 15 must name an actually visible, unconsumed static target. */
    public boolean acceptsResponse(int interfaceId, int componentId, int slot) {
        if (!responsePending || !isOpen(interfaceId) || slot != -1) return false;
        if (interfaceId == 1188)
            return componentId >= 8 && (componentId - 8) % 5 == 0 && (componentId - 8) / 5 < optionCount;
        return componentId == (interfaceId == 1186 ? 8 : 15);
    }

    public boolean consumeResponse(int interfaceId, int componentId, int slot) {
        if (!acceptsResponse(interfaceId, componentId, slot)) return false;
        responsePending = false; // Retire this page before entering arbitrary Dialogue.run code.
        return true;
    }

    public void close() {
        int previous = renderedInterface;
        renderedInterface = -1;
        optionCount = 0;
        responsePending = false;
        if (previous == -1) return;
        player.getInterfaceManager().unregisterNativeOpen(previous);
        // The explicit native close is independent of the legacy closeInterfaceLocked flag.
        channel.write(Native950Packets.closeSub(ROOT, HOST));
        refresh();
    }

    private void refresh() { channel.write(Native950Packets.runClientScript(1364)); }

    private static String join(String[] texts) {
        Objects.requireNonNull(texts, "texts");
        StringBuilder out = new StringBuilder();
        for (String text : texts) {
            if (out.length() != 0) out.append(' ');
            out.append(Objects.requireNonNull(text, "text"));
        }
        return out.toString();
    }

    public static synchronized void verify() {
        if (verified) return;
        if (!Cache.isFlatReadOnly()) throw new IllegalStateException("Dialogues require the paired flat cache");
        pin(22, 665, 23, "04707195ae4f9c747512426c1566ecfa80cc2c22cff532fbcca322c6f91ed501");
        pin(3, ROOT, 747, "adc008cab54211505395ce179225d9e4ae3d2af375c6f4701ef82d2c7ea2c2f6");
        pin(3, ROOT, HOST, "691f9fd54ceb8f927c7db515a7d0058eba76d1a2bede5b8757ba4e556acf403c");
        pinInterface(1184, 16, "c4667e3879888ca48d4e439a53ce215dcf0afcf3c6cb4dce147de391e54b8a11");
        pinInterface(1191, 16, "fe68ec70c122b3a011cc38d482c61ef764090b56189cf8de891ed1765b4670d0");
        pinInterface(1188, 41, "da7ff93f528b98a35ccd11ef7d1496f5795c45e63d585f8ca6d17f3823708ea1");
        pinInterface(1186, 9, "3832cc587e75ae99aba21e8cb3af753af01a930df9aa82641cf75a46f3b8d261");
        pin(12, 1364, 0, "fc33d9cd243a268438b22832706f89fc0b86e306e8eee1a85d41b859d8aac1f7");
        pin(12, 14163, 0, "73096ccc4d74431bc538777652bc7462f7ae43fb94c0fcbbe2d5b54ae5a81142");
        pin(12, 5589, 0, "80a1d5c25dcb40dd79145e063c23ad5050caa69863af7fb36f056b344100436f");
        pin(12, 3882, 0, "9689fde71a7483a0a549e5fb2b37a369d37c5b2d8a7a9f0d2a20cd091a76fdc4");
        pin(12, 5592, 0, "582d0d823da06ff6c6f4dd1a4d30ca4410db8966ed524fe7394138fd3bade03e");
        pin(12, 5593, 0, "73e6d9e51e0b17f1b4c876cc8c31a887378ed2b57d2209d56f5a70100b41bd87");
        pin(12, 7800, 0, "6c63ec9695fe706f0ca28fc8dc1a8bc97ebf47b8dccab182d30f915d18f4689f");
        pin(12, 5584, 0, "f433fb95a6536135ed28af0df24aa95d0f6aea148c4d4d84b7c4ccf6516faf56");
        pin(12, 5585, 0, "8448a1e58fcbea04b288c71e0af2c508fab54b6ba1cca302cfc11836e9e17275");
        pin(12, 2374, 0, "facfe3d301e90a505cd8dd43fc08ca4e22e5877a61158be4e750c64ca7307af4");
        pin(12, 16429, 0, "8eaa6f73a7142d657745079cddbcad3a6c2265f2d65cf0dd45f6a21b12ca7cb6");
        verified = true;
    }

    private static void pinInterface(int interfaceId, int count, String expected) {
        MessageDigest digest = digest();
        for (int file = 0; file < count; file++) digest.update(file(3, interfaceId, file));
        requireDigest(digest, expected, "interface " + interfaceId);
    }

    private static void pin(int index, int group, int file, String expected) {
        MessageDigest digest = digest();
        digest.update(file(index, group, file));
        requireDigest(digest, expected, index + "/" + group + "/" + file);
    }

    private static byte[] file(int index, int group, int file) {
        byte[] data = Cache.STORE.getIndexes()[index].getFile(group, file);
        if (data == null) throw new IllegalStateException("Missing dialogue cache file " + index + "/" + group + "/" + file);
        return data;
    }

    private static MessageDigest digest() {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException impossible) { throw new AssertionError(impossible); }
    }

    private static void requireDigest(MessageDigest digest, String expected, String binding) {
        StringBuilder actual = new StringBuilder();
        for (byte value : digest.digest()) actual.append(String.format("%02x", value & 255));
        // Routed through the shared gate so a stale 947-era pin can be downgraded to a
        // report during the 950 port instead of blocking login entirely.
        NativeCacheVerification.requireBinding("Dialogues", binding, expected, actual.toString());
    }
}
