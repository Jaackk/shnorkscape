package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/** Regression coverage for passive workspace diagnostics: no packet is emitted or replayed. */
public final class Native950WorkspaceTest {
    @Test public void windowReportsRemainObservationalAndExposeViewportStatus() {
        EmbeddedChannel channel=new EmbeddedChannel();
        Player player=Player.createNative950("workspace",new WorldTile(3217,3258,0),channel);
        try {
            Native950Actions.WindowReportAction report=(Native950Actions.WindowReportAction)Native950Actions.decode(9,
                    new byte[]{3,5,0,3,(byte)0xd0,0});
            Native950Workspace.windowReport(player,report);
            assertTrue(Native950Workspace.status(player).contains("1280x976 mode 3"));
            assertTrue("diagnostic capture must not write gameplay packets",channel.readOutbound()==null);
        } finally { Native950Workspace.close(player);channel.finishAndReleaseAll(); }
    }
}
