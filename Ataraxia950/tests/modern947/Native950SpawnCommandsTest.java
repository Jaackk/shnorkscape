package com.rs.game.player.client;

import com.rs.game.ForceMovement;
import com.rs.cores.CoresManager;
import com.rs.cores.Native950TickScheduler;
import java.lang.reflect.Field;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public final class Native950SpawnCommandsTest {
    @Test public void compactFootprintsTouchWithoutOverlappingInEveryFacing(){
        WorldTile anchor=new WorldTile(3200,3200,0);
        for(int size:new int[]{1,2,5,16,64})for(int fx=-1;fx<=1;fx++)for(int fy=-1;fy<=1;fy++){
            if(fx==0&&fy==0)continue;
            java.util.List<WorldTile> placed=new java.util.ArrayList<>();
            for(int index=0;index<5;index++){
                WorldTile next=Native950DiagnosticSpawns.compactTile(anchor,size,fx,fy,index);
                assertFalse(Native950DiagnosticSpawns.overlaps(next,size,anchor,1,0));
                for(WorldTile before:placed)assertFalse(Native950DiagnosticSpawns.overlaps(next,size,before,size,0));
                if(index==0)assertTrue("First footprint must touch the player's edge",Native950DiagnosticSpawns.overlaps(next,size,anchor,1,1));
                placed.add(next);
            }
            assertEquals(size,Math.max(Math.abs(placed.get(0).getX()-placed.get(1).getX()),Math.abs(placed.get(0).getY()-placed.get(1).getY())));
        }
    }
    private String previous;
    private final EmbeddedChannel channel = new EmbeddedChannel() {
        @Override protected SocketAddress remoteAddress0() { return new InetSocketAddress("127.0.0.1",43650); }
    };
    private final Player player = Player.createNative950("spawn-test",new WorldTile(3217,3258,0),channel);
    private int calls, id, type, rotation, amount;
    private boolean npc, repeat, hadWalk;
    private WorldTile tile;
    private final Native950DevelopmentCommands.SpawnActions spawns = new Native950DevelopmentCommands.SpawnActions() {
        public String npc(Player actor,int value,int count,boolean recurring) {
            capture(actor,value,true,-1,0);amount=count;repeat=recurring;return "Spawned NPC";
        }
        public String object(Player actor,int value,int shape,int angle) {
            capture(actor,value,false,shape,angle);return "Spawned object";
        }
    };
    private void capture(Player actor,int value,boolean isNpc,int shape,int angle) {
        assertSame(player,actor);calls++;id=value;npc=isNpc;type=shape;rotation=angle;
        hadWalk=!actor.getWalkSteps().isEmpty();tile=new WorldTile(actor);
    }
    @Before public void setup() {
        previous=System.getProperty(Native950DevelopmentCommands.PROPERTY);
        System.setProperty(Native950DevelopmentCommands.PROPERTY,"true");player.setActive(true);player.setRights(2);
    }
    @After public void cleanup() {
        channel.finishAndReleaseAll();
        if(previous==null)System.clearProperty(Native950DevelopmentCommands.PROPERTY);
        else System.setProperty(Native950DevelopmentCommands.PROPERTY,previous);
    }
    private void run(String text) { Native950DevelopmentCommands.handle(player,channel,text,spawns); }

    @Test public void npcUsesCurrentPositionAsPlacementAnchorAndStopsQueuedWalking() {
        assertTrue(player.addWalkSteps(3218,3258,1,false));
        assertFalse(player.getWalkSteps().isEmpty());
        run(";;npc 42");
        assertEquals(1,calls);assertTrue(npc);assertEquals(42,id);assertEquals(1,amount);assertFalse(repeat);assertFalse(hadWalk);
        assertEquals(3217,tile.getX());assertEquals(3258,tile.getY());assertEquals(0,tile.getPlane());
        assertNull(player.getNextWorldTile());assertNull(player.getNextForceMovement());
    }
    @Test public void objectChoosesCacheShapeByDefaultAndAcceptsExplicitTypeAndRotation() {
        run(";;obj 38787");
        assertEquals(1,calls);assertFalse(npc);assertEquals(38787,id);assertEquals(-1,type);assertEquals(0,rotation);
        run("::OBJ\t140251\t0\t3");
        assertEquals(2,calls);assertEquals(140251,id);assertEquals(0,type);assertEquals(3,rotation);
        run(";;obj 0 22");assertEquals(3,calls);assertEquals(0,id);assertEquals(22,type);assertEquals(0,rotation);
        assertEquals(player.getX(),tile.getX());assertEquals(player.getY(),tile.getY());
    }
    @Test public void malformedOrOutOfRangeRequestsNeverReachWorldMutation() {
        for(String text:new String[]{";;npc",";;npc fish",";;npc -1",";;npc 1 0",";;npc 1 51",";;npc 2147483648",
                ";;npcrepeat",";;npcrepeat 42 0",";;npc repeat",";;npc repeat 42 51",
                ";;obj",";;obj fish",";;obj -1",";;obj 1 -1",";;obj 1 23",";;obj 1 10 -1",
                ";;obj 1 10 4",";;obj 1 10 0 7",";;obj 1 1.5",";;obj 1 10 2147483648"})run(text);
        assertEquals(0,calls);
        assertFalse(Native950DevelopmentCommands.isCommand(";;npcfoo 1"));
        assertFalse(Native950DevelopmentCommands.isCommand(";;object 1"));
    }
    @Test public void developmentModeAndPlayerStateGatesApplyToBothCommands() {
        System.setProperty(Native950DevelopmentCommands.PROPERTY,"false");run(";;npc 42");run(";;obj 38787");
        System.setProperty(Native950DevelopmentCommands.PROPERTY,"true");player.setActive(false);
        run(";;npc 42");run(";;obj 38787");
        player.setActive(true);player.lock(60);run(";;npc 42");run(";;obj 38787");
        assertEquals(0,calls);
    }
    @Test public void npcCountIsBoundedAndRequiresAnAccountGrant() {
        run(";;npc 42 3");assertEquals(1,calls);assertEquals(3,amount);assertFalse(repeat);
        run(";;npcrepeat 42 4");assertEquals(2,calls);assertEquals(4,amount);assertTrue(repeat);
        run(";;npc repeat 42 2");assertEquals(3,calls);assertEquals(2,amount);assertTrue(repeat);
        player.setRights(0);run(";;npc 42 3");assertEquals(3,calls);
    }
    @Test public void pendingTeleportOrForcedMovementCannotSpawnOnTheWrongTile() throws Exception {
        player.setNextWorldTile(new WorldTile(3208,3215,0));run(";;npc 42");run(";;obj 38787");
        player.setNextWorldTile(null);
        Field field=CoresManager.class.getDeclaredField("native947Scheduler");
        field.setAccessible(true);Object previousWheel=field.get(null);
        try {
            field.set(null,new Native950TickScheduler());
            player.setNextForceMovement(new ForceMovement(new WorldTile(3219,3258,0),2,ForceMovement.EAST));
            assertTrue(player.isNative950ForceMovementActive());
            run(";;npc 42");run(";;obj 38787");assertEquals(0,calls);
        } finally {
            player.setNextForceMovement(null);field.set(null,previousWheel);
        }
    }
    @Test public void nonLocalConnectionCannotSpawn() {
        EmbeddedChannel remote=new EmbeddedChannel() {
            @Override protected SocketAddress remoteAddress0() { return new InetSocketAddress("192.0.2.1",43650); }
        };
        try {
            Native950DevelopmentCommands.handle(player,remote,";;npc 42",spawns);
            Native950DevelopmentCommands.handle(player,remote,";;obj 38787",spawns);
            assertEquals(0,calls);
        } finally { remote.finishAndReleaseAll(); }
    }
}
