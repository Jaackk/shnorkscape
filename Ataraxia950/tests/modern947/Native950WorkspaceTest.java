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

    @Test public void workspaceDiagnosticsCaptureOnlyOpcode14DeltasAndNeverEmit() {
        EmbeddedChannel channel=new EmbeddedChannel();
        Player player=Player.createNative950("workspace-opaque",new WorldTile(3217,3258,0),channel);
        try {
            Native950BugTest.toggle(player);
            Native950Workspace.inboundFrame(player,74,new byte[]{1,2,3,4,5,6,7});
            Native950Workspace.inboundFrame(player,87,new byte[]{9,9,9});
            Native950Workspace.inboundFrame(player,14,new byte[]{1,11,36,0,0,0,1});
            Native950Workspace.inboundFrame(player,14,new byte[]{1,11,36,0,0,0,2});
            String captured=Native950Workspace.pendingPayloads(player);
            assertTrue(captured.contains("changedIds=2852"));
            assertFalse("values must never enter Bug Test telemetry",captured.contains("00000002"));
            assertFalse("chat must never enter workspace payload capture",captured.contains("87"));
            Native950Workspace.marker(player,"candidate");
            assertTrue("diagnostics are observation only",channel.readOutbound()==null);
            Native950BugTest.toggle(player);
        } finally { Native950Workspace.close(player);channel.finishAndReleaseAll(); }
    }

    @Test public void workspaceIntegerDescriptorIsPinnedAndExcludesUnknowns() {
        assertTrue(Native950WorkspaceIntegerDescriptor.size() >= 215);
        assertTrue(Native950WorkspaceIntegerDescriptor.contains(2852));
        assertTrue("3296 must be derived through cache-backed domain-2 varbits", Native950WorkspaceIntegerDescriptor.contains(3296));
        assertFalse(Native950WorkspaceIntegerDescriptor.contains(65535));
        assertTrue(Native950WorkspaceIntegerDescriptor.evidenceFingerprint().matches("[0-9a-f]{64}"));
        assertTrue(Native950WorkspaceIntegerDescriptor.property("script.ids").contains("8707"));
        assertTrue(Native950WorkspaceIntegerDescriptor.property("varbit.ids").contains("19037"));
    }
}
