package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;

/** Native sender-derived command bytes through framing, Huffman and the real command handler. */
public final class Native950DevelopmentCommandWireTest {
    // Single native -1 frame length. See protocol-analysis/chat-framing-950-derived.md.
    private static final byte[] STATUS_WIRE = hex("57 0d 00 00 0c 06 18 18 6b 15 b8 ef 27 7b 40");
    // Actual 950 cache10/1/0, also byte-identical to the 947 table. No synthetic chat codec.
    private static final String HUFFMAN =
            "1616161616161516161416161615161616161616161616161616161616161616"
            + "03081610161011070d0d0d10070a06100a0b0c0c0c0c0d0d0e0e0b0e130f1108"
            + "0b090a0a0a0a0b0a09070c0b0a0a090a0a0c0a09080c0c090e080c111011160d"
            + "150407060503060605040a0705060404060a0504040507060a060a1613160e16"
            + "1616161616161616161616161616161616161616161616161616161616161616"
            + "1616161616161616161616161616161616161616161616161616161616161616"
            + "1616161616161616161616161616161616161616161616161616161616161616"
            + "1616161616161616161616161616161616161616161616151615161616151616";

    @Test public void literalNativeStatusCommandReachesTheLocalHandlerAndReplies() throws Exception {
        exercise(false);
    }

    @Test public void fragmentedEncryptedNativeStatusCommandStillRunsExactlyOnce() throws Exception {
        exercise(true);
    }

    private static void exercise(boolean encryptedAndFragmented) throws Exception {
        String previous = System.getProperty(Native950DevelopmentCommands.PROPERTY);
        System.setProperty(Native950DevelopmentCommands.PROPERTY, "true");
        byte[] table = hex(HUFFMAN);
        assertArrayEquals(hex("779460467727aac19739d70d6b21afa911f01e1a66f7d826a2f0b79836111620"),
                MessageDigest.getInstance("SHA-256").digest(table));
        Native950Actions.resetHuffman();
        Native950Actions.installHuffman(table);
        int[] seed = {9, 5, 0, 1};
        Native950GameTransport transport = new Native950GameTransport(
                encryptedAndFragmented ? new Native950Isaac(seed) : () -> 0,
                () -> 0, Thread.currentThread(), 8, 8);
        EmbeddedChannel channel = new EmbeddedChannel(transport) {
            @Override protected SocketAddress remoteAddress0() {
                return new InetSocketAddress("127.0.0.1", 43650);
            }
        };
        try {
            Player player = Player.createNative950("command-wire", new WorldTile(3217, 3258, 0), channel);
            player.setActive(true);
            Native950Interactions interactions = new Native950Interactions(player, channel, content());
            String reply = "Tile 3217, 3258, 0; player masks " + Native950EntityMasks.playerBlocks()
                    + "; NPC masks " + Native950EntityMasks.npcBlocks()
                    + "; refused masks " + Native950EntityMasks.refusals() + ".";
            byte[] wire = STATUS_WIRE.clone();
            if (encryptedAndFragmented) wire[0] += new Native950Isaac(seed).getAsInt();
            if (encryptedAndFragmented) {
                for (byte value : wire) channel.writeInbound(Unpooled.wrappedBuffer(new byte[] {value}));
            } else channel.writeInbound(Unpooled.wrappedBuffer(wire));
            assertTrue("Typing a valid command must not disconnect", channel.isActive());
            assertNull(transport.terminalFailure());
            assertEquals(1, transport.pendingActions());
            assertEquals(0, transport.chatFramesDropped());
            assertEquals(0, transport.unhandledFrameCount());
            assertEquals(1, transport.drainActions(interactions::handle));
            Native950Interactions.State state = interactions.snapshot();
            assertEquals(1, state.commandsRun);
            assertEquals(0, state.commandsRefused);
            assertEquals(0, state.commandFailures);
            assertEquals(0, state.unhandledActions);
            assertEquals(0, transport.pendingActions());
            channel.flush();
            assertArrayEquals("The actual local status branch must reply, not silently discard chat",
                    Native950Packets.gameMessage(0, reply).frame(() -> 0), output(channel));
            channel.checkException();
            assertTrue(channel.isActive());
        } finally {
            channel.finishAndReleaseAll();
            Native950Actions.resetHuffman();
            if (previous == null) System.clearProperty(Native950DevelopmentCommands.PROPERTY);
            else System.setProperty(Native950DevelopmentCommands.PROPERTY, previous);
        }
    }

    private static Native950Content content() {
        Native950ItemCatalog items = new Native950ItemCatalog(Arrays.asList(
                new Native950ItemCatalog.Entry(995, "Coins", true, new String[] {"Add to pouch"}),
                new Native950ItemCatalog.Entry(1511, "Logs", false, new String[] {"Craft"}),
                new Native950ItemCatalog.Entry(315, "Shrimps", false, new String[] {"Eat"})));
        int[] amounts = new int[11]; amounts[1] = amounts[2] = 1;
        return new Native950Content(items, new Native950Content.BankUi(517, 201, 15, 317, 39,
                amounts, amounts, Collections.emptyList(), Collections.emptyList(), 6));
    }
    private static byte[] output(EmbeddedChannel channel) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Object value;
        while ((value = channel.readOutbound()) != null) {
            assertTrue(value instanceof ByteBuf);
            ByteBuf bytes = (ByteBuf)value;
            try {
                byte[] chunk = new byte[bytes.readableBytes()]; bytes.readBytes(chunk);
                output.write(chunk, 0, chunk.length);
            } finally { bytes.release(); }
        }
        return output.toByteArray();
    }
    private static byte[] hex(String value) {
        String text = value.replace(" ", ""); byte[] bytes = new byte[text.length() / 2];
        for (int i = 0; i < bytes.length; i++) bytes[i] = (byte)Integer.parseInt(text.substring(i * 2, i * 2 + 2), 16);
        return bytes;
    }
}