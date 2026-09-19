package com.rs.game.player.client;

import com.google.gson.*;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class Native950WorkspaceStoreTest {
    private JsonObject schema() throws Exception {
        return Native950LayoutFixture.json(Native950LayoutFixture.readPinned(Paths.get("../tools/vulkan-static-probe/snapshot-schema-v4.json"),Native950LayoutFixture.SCHEMA_SHA,256000));
    }
    private Native950WorkspaceStore.Record record(String account,long revision) throws Exception {
        Map<Integer,Integer> values=new TreeMap<>();
        for(JsonElement e:schema().getAsJsonArray("ids"))values.put(e.getAsInt(),0);
        values.put(6056,null);values.put(6057,null);values.put(8372,6);values.put(8373,6);
        return new Native950WorkspaceStore.Record(account,revision,values);
    }
    @Test public void independentStoreRestartRetainsEveryPresetAndAbsentVersusZero() throws Exception {
        Path dir=Files.createTempDirectory("workspace-test-");
        Native950WorkspaceStore a=new Native950WorkspaceStore(dir,schema());
        assertNull(a.load("testa"));
        Native950WorkspaceStore.Record original=record("testa",1);a.save(original);
        Native950WorkspaceStore reopened=new Native950WorkspaceStore(dir,schema());
        assertEquals(original.values,reopened.load("testa").values);
        assertNull(reopened.load("testb"));
        assertNull(reopened.load("testa").values.get(6056));
        assertEquals(Integer.valueOf(0),reopened.load("testa").values.get(3296));
        Map<Integer,Integer> next=new TreeMap<>(original.values);next.put(8372,13);next.put(8373,13);
        reopened.save(new Native950WorkspaceStore.Record("testa",2,next));
        assertEquals(Integer.valueOf(13),a.load("testa").values.get(8372));
        assertEquals(1,a.decode(Files.readAllBytes(a.path("testa").resolveSibling(a.path("testa").getFileName()+".previous")),"testa").revision);
        try{a.save(original);fail("stale save");}catch(java.io.IOException expected){}
        assertEquals(2,a.load("testa").revision);
    }
    @Test public void corruptionTruncationWrongAccountAndExtraBytesFailClosed() throws Exception {
        Native950WorkspaceStore s=new Native950WorkspaceStore(Files.createTempDirectory("workspace-bad-"),schema());
        byte[] b=s.encode(record("testa",1));
        try{s.decode(b,"testb");fail();}catch(java.io.IOException expected){}
        for(int n:new int[]{0,31,b.length-1,b.length+1}) {
            try{s.decode(Arrays.copyOf(b,n),"testa");fail();}catch(java.io.IOException expected){}
        }
        b[30]^=1;try{s.decode(b,"testa");fail();}catch(java.io.IOException expected){}
        assertFalse(Files.exists(s.path("testa")));
    }
    @Test public void schemaAndSelectionAreValidatedBeforeDiskWrites() throws Exception {
        Native950WorkspaceStore s=new Native950WorkspaceStore(Files.createTempDirectory("workspace-schema-"),schema());
        Native950WorkspaceStore.Record good=record("testa",1);
        for(int selected:new int[]{6,7,12,13}) {
            Map<Integer,Integer> values=new TreeMap<>(good.values);values.put(8372,selected);values.put(8373,selected);
            assertEquals(selected,Native950LayoutFixture.workspacePlan(schema(),new Native950WorkspaceStore.Record("testa",1,values).snapshot()).selected);
        }
        Map<Integer,Integer> bad=new TreeMap<>(good.values);bad.remove(3296);bad.put(65535,1);
        try{s.save(new Native950WorkspaceStore.Record("testa",1,bad));fail();}catch(IllegalArgumentException expected){}
        assertNull(s.load("testa"));
    }
    @Test public void restoreIsExplicitDisposableOnlyAndOneShotAfterCompletedRead() throws Exception {
        String old=System.getProperty(Native950DisposableWorkspaceRestore.PROPERTY),dev=System.getProperty(Native950DevelopmentCommands.PROPERTY);
        System.setProperty(Native950DevelopmentCommands.PROPERTY,"true");
        EmbeddedChannel ch=new EmbeddedChannel(){@Override protected SocketAddress remoteAddress0(){return new InetSocketAddress("127.0.0.1",1234);}};
        try {
            Player p=Player.createNative950("layoutgate2",new WorldTile(3217,3258,0),ch);p.setActive(true);
            System.clearProperty(Native950DisposableWorkspaceRestore.PROPERTY);assertFalse(Native950DisposableWorkspaceRestore.allowed(p,ch));
            System.setProperty(Native950DisposableWorkspaceRestore.PROPERTY,"true");assertTrue(Native950DisposableWorkspaceRestore.allowed(p,ch));
            Player other=Player.createNative950("jaxa",new WorldTile(3217,3258,0),ch);other.setActive(true);
            assertFalse(Native950DisposableWorkspaceRestore.allowed(other,ch));
            CompletableFuture<Native950LayoutFixture.Plan> future=new CompletableFuture<>();
            Native950DisposableWorkspaceRestore gate=new Native950DisposableWorkspaceRestore(future);
            assertFalse(gate.finishOnWorldThread(p,ch));assertNull(ch.readOutbound());
            Native950LayoutFixture.Plan plan=Native950LayoutFixture.workspacePlan(schema(),record("layoutgate2",1).snapshot());future.complete(plan);
            assertTrue(gate.finishOnWorldThread(p,ch));ch.flush();
            for(Native950Packets.Packet expected:plan.packets)assertArrayEquals(expected.frame(()->0),((Native950Packets.Packet)ch.readOutbound()).frame(()->0));
            assertArrayEquals(Native950Packets.runClientScript(8741,6).frame(()->0),((Native950Packets.Packet)ch.readOutbound()).frame(()->0));
            assertNull(ch.readOutbound());assertTrue(gate.finishOnWorldThread(p,ch));ch.flush();assertNull(ch.readOutbound());
        } finally {
            ch.finishAndReleaseAll();
            if(old==null)System.clearProperty(Native950DisposableWorkspaceRestore.PROPERTY);else System.setProperty(Native950DisposableWorkspaceRestore.PROPERTY,old);
            if(dev==null)System.clearProperty(Native950DevelopmentCommands.PROPERTY);else System.setProperty(Native950DevelopmentCommands.PROPERTY,dev);
        }
    }
}
