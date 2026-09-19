package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

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

    @Test public void workspaceFrameCaptureIsDisabledUntilBugTestModeIsExplicitlyEnabled() {
        EmbeddedChannel channel=new EmbeddedChannel();
        Player player=Player.createNative950("workspace-quiet",new WorldTile(3217,3258,0),channel);
        try {
            Native950Workspace.unhandledFrame(player,33,new byte[]{1,2,3,4});
            Native950Workspace.marker(player,"quiet");
            assertTrue("passive diagnostics must never emit packets",channel.readOutbound()==null);
        } finally { Native950Workspace.close(player);channel.finishAndReleaseAll(); }
    }

    @Test public void workspaceDiagnosticsNeverEmitOrDecodeCandidatePayloads() {
        EmbeddedChannel channel=new EmbeddedChannel();
        Player player=Player.createNative950("workspace-opaque",new WorldTile(3217,3258,0),channel);
        try {
            Native950BugTest.toggle(player);
            Native950Workspace.inboundFrame(player,74,new byte[]{1,2,3,4,5,6,7});
            Native950Workspace.inboundFrame(player,69,new byte[]{9,9,9});
            Native950Workspace.marker(player,"candidate");
            assertTrue("diagnostics are observation only",channel.readOutbound()==null);
            assertFalse("chat must not become a workspace candidate",Native950Workspace.status(player).contains("chat"));
            Native950BugTest.toggle(player);
        } finally { Native950Workspace.close(player);channel.finishAndReleaseAll(); }
    }
}
