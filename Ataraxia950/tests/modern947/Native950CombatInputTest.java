package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.protocol.modern950.Native950Actions;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.zip.CRC32;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

/** Native menu click bytes through the published viewport and combat input boundary. */
public final class Native950CombatInputTest {
    @Rule public TemporaryFolder temp = new TemporaryFolder();
    private Store previous;
    private Player player;
    private NPC npc;
    private Native950NpcView view;
    private Native950Interactions input;
    private Native950GameTransport transport;
    private EmbeddedChannel channel;
    private final Combat combat = new Combat();
    private int legacyCalls, legacyOption;

    @Before public void setup() throws Exception {
        previous = Cache.STORE;
        Cache.STORE = null;
        assertTrue(World.getNPCs().isEmpty());
        transport = new Native950GameTransport(() -> 0, () -> 0, Thread.currentThread(), 16, 16);
        channel = new EmbeddedChannel(transport);
        player = Player.createNative950("melee-input", new WorldTile(3208,3215,0), channel);
        player.setActive(true);
        player.setRunning(true);
        player.setClientHasLoadedMapRegion();
        view = new Native950NpcView(null,null);
        input = new Native950Interactions(player,channel,content(),null,view,() -> {},() -> {},() -> {},combat);
        // NPC handler dispatch stays real; replace only its content body so this input test
        // does not need maps, dialogues, or an unrelated legacy NPC subsystem.
        set(input.router(),"handlers",new Native950ActionRouter.Handlers() {
            @Override public void object(Player p,WorldObject o,int option,boolean run) { fail(); }
            @Override public void npc(Player p,NPC n,int option,boolean run) {
                assertSame(player,p); assertSame(npc,n); assertFalse(run);
                legacyCalls++; legacyOption=option;
            }
            @Override public void button(Player p,int panel,int component,int slot,int item,int packet) { fail(); }
        });
        NPCDefinitions.decodeStrict947(494,menu(2),null); // initialize the legacy static array before the tiny flat cache
        installMenu(2);
        npc = NPC.createNative950(494,new WorldTile(3209,3215,0),1);
        World.addNative950Npc(npc);
        player.getMapRegionsIds().add(npc.getRegionId());
        set(view.viewport(),"masks",Native950NpcViewport.NO_MASKS);
        publish();
    }

    @After public void cleanup() {
        if (npc != null) World.removeNative950Npc(npc);
        if (channel != null) channel.finishAndReleaseAll();
        Cache.STORE = previous;
    }

    @Test public void literalAttackOptionReachesCombatWithoutTheLegacyNpcHandlerOrRunToggle() {
        player.setRun(false);
        click(92,1); // 950 option2, plain modifier1, index BE with biased low byte
        assertEquals(1,combat.attacks);
        assertSame(npc,combat.target);
        assertTrue(combat.active);
        assertEquals(0,combat.stops);
        assertEquals(0,legacyCalls);
        assertFalse(player.getRun());
        assertEquals(0,input.snapshot().rejectedActions);
        assertEquals(0,input.snapshot().unhandledActions);
    }

    @Test public void attackUsesItsCacheMenuOrdinalAndTalkKeepsTheLegacyRoute() throws Exception {
        installMenu(5); // Attack is deliberately not the usual option2 in this fixture.
        click(60,0);
        assertEquals(1,legacyCalls);
        assertEquals(1,legacyOption);
        assertEquals(1,input.router().dispatches("npcOption:1"));
        assertEquals(0,combat.attacks);
        assertEquals(1,combat.stops);
        click(13,0); // option5 was unsupported by the old generic NPC router
        assertEquals(1,combat.attacks);
        assertEquals(1,legacyCalls);
        assertEquals(0,input.snapshot().rejectedActions);
    }

    @Test public void unpublishedNpcCannotStartCombatOrReadMissingMenuMetadata() {
        view.close();
        Cache.STORE = null;
        Object[] ownedStep = {4,3209,3215,false};
        player.getWalkSteps().add(ownedStep);
        click(92,0);
        assertEquals(0,combat.attacks);
        assertEquals(0,combat.stops);
        assertSame(ownedStep,player.getWalkSteps().peek());
        assertEquals(1,input.snapshot().rejectedActions);
    }

    @Test public void lockForceAndInactivePlayerRejectBeforeCancellingOwnedMovement() throws Exception {
        Object[] ownedStep = {4,3209,3215,false};
        player.getWalkSteps().add(ownedStep);
        player.lock(10); click(92,0); player.unlock();
        set(player,Entity.class,"native950ForceMovementActive",true);
        click(92,0);
        set(player,Entity.class,"native950ForceMovementActive",false);
        player.setActive(false); click(92,0); player.setActive(true);
        assertEquals(0,combat.attacks);
        assertEquals(0,combat.stops);
        assertSame(ownedStep,player.getWalkSteps().peek());
        assertEquals(3,input.snapshot().rejectedActions);
    }

    @Test public void controllerRefusalNeverStartsTheCombatService() throws Exception {
        set(player.getControlerManager(),"controler",new Controller() {
            @Override public void start() {}
            @Override public boolean processPlayerOption1(Entity target) { assertSame(npc,target); return false; }
        });
        set(player.getControlerManager(),"inited",true);
        click(92,0);
        assertEquals(0,combat.attacks);
        assertEquals(1,input.snapshot().rejectedActions);
        assertTrue(channel.isActive());
    }

    @Test public void unavailableTargetAndServiceRefusalsAreCountedWithoutDisconnecting() {
        npc.setCantInteract(true); click(92,0); npc.setCantInteract(false);
        assertEquals(0,combat.attacks);
        combat.refusal="That NPC is already in combat";
        click(92,0);
        assertEquals(1,combat.attacks);
        assertFalse(combat.active);
        assertEquals(2,input.snapshot().rejectedActions);
        assertEquals(0,input.snapshot().unhandledActions);
        assertTrue(channel.isActive());
    }

    @Test public void walkingOtherNpcActionsObjectsAndCloseReleaseCombatBeforeNewRoutes() {
        click(92,0);
        player.getWalkSteps().add(new Object[]{4,3209,3215,false});
        input.walking();
        assertFalse(combat.active); assertTrue(player.getWalkSteps().isEmpty());
        click(92,0); click(60,0);
        assertFalse(combat.active); assertEquals(1,legacyCalls);
        click(92,0);
        // Unsupported object still represents a deliberate noncombat action; no map lookup.
        input.handle(Native950Actions.decode(34,new byte[]{(byte)15,12,0,0,0,1,12,(byte)8,0}));
        assertFalse(combat.active);
        click(92,0); input.close();
        assertFalse(combat.active);
        assertEquals(4,combat.stops);
    }

    @Test public void dialogueContinuationDoesNotCancelCombatAgain() {
        final int[] replies={0};
        player.getDialogueManager().startDialogue(new Dialogue() {
            @Override public void start(){sendDialogue("Continue input fixture.");}
            @Override public void run(int panel,int component){replies[0]++;end();}
            @Override public void finish(){}
        });
        // 950 dialogue click101: hash BE, slot BE with its low byte biased by128. This is a continuation, not a new target.
        input.handle(Native950Actions.decode(101,ByteBuffer.allocate(6).putInt((1186<<16)|8).putShort((short)0xff7f).array()));
        assertEquals(1,replies[0]);
        assertEquals(0,combat.stops);
    }

    private void publish(){view.viewport().frame(player,15,false,Collections.singletonList(npc)); assertTrue(view.canInteract(player,npc.getIndex()));}
    private void click(int opcode,int modifier) {
        int index=npc.getIndex();
        channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{(byte)opcode,(byte)modifier,(byte)(index>>>8),(byte)(index+128)}));
        assertNull(transport.terminalFailure());
        assertEquals(1,transport.drainActions(input::handle));
        assertTrue(channel.isActive());
    }
    private final class Combat implements Native950Interactions.CombatActions {
        int attacks,stops; boolean active; NPC target; String refusal;
        @Override public String attack(Player p,NPC n){assertSame(player,p);attacks++;target=n;active=refusal==null;return refusal;}
        @Override public void stop(Player p){assertSame(player,p);stops++;if(active)p.resetWalkSteps();active=false;target=null;}
    }
    private void installMenu(int attackOption) throws Exception {
        Path root=temp.newFolder().toPath();
        byte[] data=menu(attackOption), group=container(data);
        Files.createDirectories(root.resolve("255")); Files.createDirectories(root.resolve("18"));
        CRC32 crc=new CRC32();crc.update(group);
        byte[] reference=ByteBuffer.allocate(22).put((byte)7).putInt(1).put((byte)0).putShort((short)1)
                .putShort((short)(494>>>7)).putInt((int)crc.getValue()).putInt(9).putShort((short)1).putShort((short)(494&127)).array();
        Files.write(root.resolve("255/18.dat"),container(reference));
        Files.write(root.resolve("18/"+(494>>>7)+".dat"),group);
        Cache.STORE=Store.openFlatReadOnly(root);
    }
    private static byte[] menu(int attackOption) throws Exception {
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);
        out.writeByte(2);out.writeBytes("Input fixture");out.writeByte(0);
        out.writeByte(30);out.writeBytes("Talk-to");out.writeByte(0);
        out.writeByte(29+attackOption);out.writeBytes("Attack");out.writeByte(0);
        out.writeByte(12);out.writeByte(1);out.writeByte(0);return bytes.toByteArray();
    }
    private static byte[] container(byte[] payload){return ByteBuffer.allocate(payload.length+5).put((byte)0).putInt(payload.length).put(payload).array();}
    private static void set(Object target,String name,Object value) throws Exception {set(target,target.getClass(),name,value);}
    private static void set(Object target,Class<?> type,String name,Object value) throws Exception {Field f=type.getDeclaredField(name);f.setAccessible(true);f.set(target,value);}
    private static Native950Content content() {
        Native950ItemCatalog items=new Native950ItemCatalog(Arrays.asList(
                new Native950ItemCatalog.Entry(995,"Coins",true,new String[]{"Add to pouch"}),
                new Native950ItemCatalog.Entry(1511,"Logs",false,new String[]{"Craft"}),
                new Native950ItemCatalog.Entry(315,"Shrimps",false,new String[]{"Eat"})));
        int[] amounts=new int[11];amounts[1]=amounts[2]=1;
        return new Native950Content(items,new Native950Content.BankUi(517,201,15,317,39,amounts,amounts,Collections.emptyList(),Collections.emptyList(),6));
    }
}
