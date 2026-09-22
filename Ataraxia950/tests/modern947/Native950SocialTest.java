package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950SocialTest {
    @Test public void textIsBoundedAndCannotInjectMarkupOrTerminators() {
        assertEquals("[img=1]hi", Native950Social.clean("<img=1>hi\0\n"));
        assertEquals("", Native950Social.clean("\u2603"));
        assertEquals(160, Native950Social.clean(new String(new char[200]).replace('\0', 'a')).length());
        assertEquals("", Native950Social.clean("  "));
    }

    @Test public void followMenuUsesExact950Transforms() {
        assertEquals(99, Native950Packets.playerOption("Follow", 4).type().opcode());
        assertArrayEquals(new byte[]{0,70,111,108,108,111,119,0,(byte)252,(byte)255,127},
                Native950Packets.playerOption("Follow", 4).payload());
    }

    @Test public void chatHonoursMuteIgnoreAndPublicFiltersAndFollowRejectsInvalidTargets() throws Exception {
        EmbeddedChannel a = new EmbeddedChannel(), b = new EmbeddedChannel();
        try {
            Player sender = Player.createNative950("sender", new WorldTile(3200,3200,0), a);
            Player viewer = Player.createNative950("viewer", new WorldTile(3201,3200,0), b);
            viewer.setActive(true);
            assertTrue(Native950Social.followable(sender,viewer));
            assertFalse(Native950Social.followable(sender,sender));
            assertFalse(Native950Social.followable(sender,null));
            assertTrue(Native950Social.receives(sender,viewer));
            viewer.getFriendsIgnores().getIgnores().add(sender.getUsername());
            assertFalse(Native950Social.receives(sender,viewer));
            viewer.getFriendsIgnores().getIgnores().clear();
            java.lang.reflect.Field filter = Player.class.getDeclaredField("publicStatus");
            filter.setAccessible(true); // Avoid the unrelated, unported chat-settings packet in this fixture.
            filter.setInt(viewer,1);
            assertFalse(Native950Social.receives(sender,viewer));
            viewer.getFriendsIgnores().getFriends().add(sender.getUsername());
            assertTrue(Native950Social.receives(sender,viewer));
            filter.setInt(viewer,2);
            assertFalse(Native950Social.receives(sender,viewer));
            assertTrue(Native950Social.receives(sender,sender));
            sender.setMuted(System.currentTimeMillis()+60000);
            assertFalse(Native950Social.speak(sender,"muted",System.currentTimeMillis()));
            assertNull(sender.getNextForceTalk());
        } finally { a.finishAndReleaseAll(); b.finishAndReleaseAll(); }
    }
}
