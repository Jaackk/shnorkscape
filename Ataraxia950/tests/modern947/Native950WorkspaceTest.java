package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Set;

import com.rs.network.modern.Native950GameTransport;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
        assertEquals(215, Native950WorkspaceIntegerDescriptor.bootstrapIds().size());
        assertTrue(Native950WorkspaceIntegerDescriptor.ids().containsAll(
                Native950WorkspaceIntegerDescriptor.bootstrapIds()));
    }

    @Test public void exactInitialUploadRequiresPinnedIdsCompletionAndShape() {
        byte[] exact=initialUpload();
        assertEquals(1291,exact.length);
        assertTrue(Native950Workspace.isExactInitialPermanentVariablesUpload(exact));
        byte[] incomplete=exact.clone(); incomplete[0]=0;
        assertFalse(Native950Workspace.isExactInitialPermanentVariablesUpload(incomplete));
        byte[] duplicate=exact.clone(); duplicate[7]=duplicate[1]; duplicate[8]=duplicate[2];
        assertFalse(Native950Workspace.isExactInitialPermanentVariablesUpload(duplicate));
        assertFalse(Native950Workspace.isExactInitialPermanentVariablesUpload(new byte[7]));
    }

    @Test public void attachedSessionAcknowledgesExactInitialUploadOnceAndPersistsNothing() throws Exception {
        Native950GameTransport transport=new Native950GameTransport(()->0,()->0,Thread.currentThread());
        EmbeddedChannel channel=new EmbeddedChannel(transport);
        Player player=Player.createNative950("workspace-ack",new WorldTile(3217,3258,0),channel);
        Native950Session session=new Native950Session(player,channel,transport,
                new Native950World.SceneConfig(3200,3200,0,1,5,0,0,0),null,null,null,null);
        try {
            Field ready=Native950Session.class.getDeclaredField("ready"); ready.setAccessible(true); ready.setBoolean(session,true);
            byte[] payload=initialUpload();
            byte[] frame=new byte[3+payload.length]; frame[0]=14; frame[1]=(byte)(payload.length>>>8); frame[2]=(byte)payload.length;
            System.arraycopy(payload,0,frame,3,payload.length);
            channel.writeInbound(Unpooled.wrappedBuffer(frame));
            ByteBuf acknowledgement=channel.readOutbound();
            byte[] raw=new byte[acknowledgement.readableBytes()]; acknowledgement.readBytes(raw); acknowledgement.release();
            assertArrayEquals(new byte[]{(byte)128,(byte)136},raw);
            assertEquals(1,transport.unhandledFrameCount());
            channel.writeInbound(Unpooled.wrappedBuffer(frame.clone()));
            assertNull("the same session must acknowledge the bootstrap only once",channel.readOutbound());
        } finally { session.close(); channel.finishAndReleaseAll(); }
    }

    private static byte[] initialUpload() {
        Set<Integer> ids=Native950WorkspaceIntegerDescriptor.bootstrapIds();
        byte[] payload=new byte[1+ids.size()*6]; payload[0]=1; int offset=1;
        for(int id:ids){payload[offset++]=(byte)(id>>>8);payload[offset++]=(byte)id;
            payload[offset++]=0;payload[offset++]=0;payload[offset++]=0;payload[offset++]=0;}
        return payload;
    }
}
