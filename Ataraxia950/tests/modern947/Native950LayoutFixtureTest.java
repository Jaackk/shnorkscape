package com.rs.game.player.client;

import com.google.gson.*;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.*;
import java.net.*;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class Native950LayoutFixtureTest {
    private Path root() { return Paths.get("..").toAbsolutePath().normalize(); }
    private JsonObject schema() throws Exception {
        return Native950LayoutFixture.json(Native950LayoutFixture.readPinned(root().resolve("tools/vulkan-static-probe/snapshot-schema-v4.json"),Native950LayoutFixture.SCHEMA_SHA,256000));
    }
    private JsonObject fixture() throws Exception {
        Path p=root().resolve("logs/workspace-static-v4-35192-A.json");
        assumeTrue("Private fixture stays local",Files.exists(p));
        return Native950LayoutFixture.json(Native950LayoutFixture.readPinned(p,Native950LayoutFixture.FIXTURE_SHA,256000));
    }
    @Test public void completePinnedFixtureBuildsOnlyCustomAndMaskedMetadataSetters() throws Exception {
        assumeTrue(Files.exists(root().resolve("logs/workspace-static-v4-35192-A.json")));
        Native950LayoutFixture.Plan p=Native950LayoutFixture.load(root());
        assertEquals(182,p.integerIds.size()); //178 Custom1 parents plus four integer metadata.
        assertTrue(p.integerIds.contains(3296));
        assertFalse(p.integerIds.contains(6056)); //absent, not zero
        assertFalse(p.integerIds.contains(6057));
        assertFalse(p.integerIds.contains(2920)); //active matrix not replayed
        assertFalse(p.integerIds.contains(3295)); //mask setter only
        for(int value:p.bitValues.values())assertTrue(value>=0&&value<=31);
        assertArrayEquals(Native950Packets.runClientScript(8741,6).frame(()->0),Native950LayoutFixture.application().frame(()->0));
        for(Native950Packets.Packet packet:p.packets)
            assertNotEquals("No scripts during staging",Native950LayoutFixture.application().type(),packet.type());
    }
    @Test public void malformedIncompleteAndNonintegerFixturesFailBeforePackets() throws Exception {
        JsonObject s=schema();
        JsonObject f=fixture(); f.getAsJsonArray("items").remove(0); reject(s,f);
        f=fixture(); f.getAsJsonArray("items").get(0).getAsJsonObject().addProperty("stable",false);reject(s,f);
        f=fixture(); f.getAsJsonArray("items").get(0).getAsJsonObject().addProperty("variantTag",2);reject(s,f);
        f=fixture(); f.getAsJsonArray("items").get(0).getAsJsonObject().addProperty("int32",2147483648L);reject(s,f);
        f=fixture(); f.getAsJsonArray("items").get(0).getAsJsonObject().addProperty("id",65535);reject(s,f);
    }
    private void reject(JsonObject s,JsonObject f) {
        try { Native950LayoutFixture.plan(s,f); fail("accepted malformed fixture"); }
        catch(IllegalArgumentException expected) { } catch(ArithmeticException expected) { }
    }
    @Test public void hashAndSizeRefuseChangedEvidence() throws Exception {
        Path p=root().resolve("tools/vulkan-static-probe/snapshot-schema-v4.json");
        try{Native950LayoutFixture.readPinned(p,"00",256000);fail();}catch(java.io.IOException expected){}
        try{Native950LayoutFixture.readPinned(p,Native950LayoutFixture.SCHEMA_SHA,1);fail();}catch(java.io.IOException expected){}
    }
    @Test public void accountRestrictionCannotBeOverriddenByAnAdminName() {
        assertTrue(Native950LayoutFixture.target("layoutgate2"));
        for(String n:new String[]{"jaxa","layoutgate1","layoutgate3","layoutgate2 ",null})assertFalse(Native950LayoutFixture.target(n));
        assertTrue(Native950DevelopmentCommands.isCommand(";;layoutfixture stage"));
    }
    @Test public void applyBeforeStageAndOtherAccountsSendNoSetters() {
        String old=System.getProperty(Native950DevelopmentCommands.PROPERTY);
        System.setProperty(Native950DevelopmentCommands.PROPERTY,"true");
        try {
            for(String name:new String[]{"jaxa","layoutgate1","layoutgate2"}) {
                EmbeddedChannel ch=new EmbeddedChannel(){@Override protected SocketAddress remoteAddress0(){return new InetSocketAddress("127.0.0.1",1234);}};
                Player p=Player.createNative950(name,new WorldTile(3217,3258,0),ch);p.setActive(true);
                Native950LayoutFixture.handle(p,ch,new String[]{"layoutfixture","apply"});ch.flush();
                Native950Packets.Packet response=ch.readOutbound();
                assertNotNull(response);
                assertEquals(Native950Packets.gameMessage(0,"x").type(),response.type());
                assertNull(ch.readOutbound());ch.finishAndReleaseAll();
            }
        } finally {if(old==null)System.clearProperty(Native950DevelopmentCommands.PROPERTY);else System.setProperty(Native950DevelopmentCommands.PROPERTY,old);}
    }
    @Test public void stageAndApplyAreSeparateOneShotSessionActions() throws Exception {
        assumeTrue(Files.exists(root().resolve("logs/workspace-static-v4-35192-A.json")));
        String old=System.getProperty(Native950DevelopmentCommands.PROPERTY);
        System.setProperty(Native950DevelopmentCommands.PROPERTY,"true");
        EmbeddedChannel ch=new EmbeddedChannel(){@Override protected SocketAddress remoteAddress0(){return new InetSocketAddress("127.0.0.1",1234);}};
        try {
            Player p=Player.createNative950("layoutgate2",new WorldTile(3217,3258,0),ch);p.setActive(true);
            Native950LayoutFixture.Plan expected=Native950LayoutFixture.load(root());
            Native950DevelopmentCommands.handle(p,ch,";;layoutfixture stage");ch.flush();
            for(Native950Packets.Packet packet:expected.packets) {
                Native950Packets.Packet actual=ch.readOutbound();
                assertArrayEquals(packet.frame(()->0),actual.frame(()->0));
            }
            assertNotNull(ch.readOutbound());assertNull(ch.readOutbound());
            Native950DevelopmentCommands.handle(p,ch,";;layoutfixture stage");ch.flush();
            assertNotNull(ch.readOutbound());assertNull(ch.readOutbound());
            Native950DevelopmentCommands.handle(p,ch,";;layoutfixture apply");ch.flush();
            Native950Packets.Packet script=ch.readOutbound();
            assertArrayEquals(Native950LayoutFixture.application().frame(()->0),script.frame(()->0));
            assertNotNull(ch.readOutbound());assertNull(ch.readOutbound());
            Native950DevelopmentCommands.handle(p,ch,";;layoutfixture apply");ch.flush();
            assertNotNull(ch.readOutbound());assertNull(ch.readOutbound());
        } finally {
            ch.finishAndReleaseAll();
            if(old==null)System.clearProperty(Native950DevelopmentCommands.PROPERTY);else System.setProperty(Native950DevelopmentCommands.PROPERTY,old);
        }
    }
}
