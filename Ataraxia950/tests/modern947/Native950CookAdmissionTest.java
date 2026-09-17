package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.ControlerManager;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.controllers.ControllerHandler;
import com.rs.game.player.controllers.ImpossibleJad;
import com.rs.game.player.dialogue.impl.Cook;
import com.rs.network.protocol.modern950.Native950Actions;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/** Real Cook progression and native reply ownership, without constructing a dynamic region. */
public final class Native950CookAdmissionTest {
    private static final String KEY = "ImpossibleJadControler";
    private static final String UNAVAILABLE = "That adventure is not available yet. Please come back later.";
    private EmbeddedChannel channel;
    private Player player;
    private Native950Interactions interactions;

    @Before public void setup() {
        channel = new EmbeddedChannel();
        player = Player.createNative950("cook-admission", new WorldTile(3209,3215,0), channel);
        player.setActive(true);
        interactions = new Native950Interactions(player,channel,content(),null,null,()->{},()->{});
        CountingJad.constructed = 0;
        CountingJad.last = null;
    }

    @After public void cleanup() { channel.finishAndReleaseAll(); }

    @Test public void completeYesFlowClosesWithoutProgressOrAdventureAndCanReopen() {
        FixedCook cook = start();
        reply(1184,15); // Intro -> Yes/No.
        reply(1188,8);  // Yes -> player stage 1.
        for (int stage=1; stage<=6; stage++) {
            assertEquals(stage,cook.stageValue());
            reply(player.getNative950Dialogues().interfaceId(),15);
        }
        assertEquals(9,cook.stageValue());
        assertEquals(UNAVAILABLE,cook.lastNpcText());
        assertTrue(player.getNative950Dialogues().isOpen(1184));
        assertFalse(player.hasTalkedtoCook());
        assertNoAdventure();
        reply(1184,15);
        assertClosed();
        reply(1184,15); // A late continue cannot start the adventure after close.
        assertNoAdventure();
        FixedCook reopened=start();
        assertEquals(-1,reopened.stageValue());
        assertTrue(player.getNative950Dialogues().isOpen(1184));
        assertFalse(player.hasTalkedtoCook());
        assertEquals(0,interactions.router().handlerFailures());
    }

    @Test public void choosingNoClosesAndLeavesCookProgressUntouched() {
        FixedCook cook=start();
        reply(1184,15);
        reply(1188,13);
        assertClosed();
        assertFalse(cook.npcTexts.contains(UNAVAILABLE));
        assertFalse(player.hasTalkedtoCook());
        assertNoAdventure();
        assertEquals(-1,start().stageValue());
    }

    @Test public void previouslyTalkedPlayerGetsSafeEndingAndRetainsExistingProgress() {
        player.setTalkedToCook();
        FixedCook cook=start();
        assertEquals(8,cook.stageValue());
        reply(1184,15);
        assertEquals(UNAVAILABLE,cook.lastNpcText());
        assertEquals(9,cook.stageValue());
        assertTrue(player.hasTalkedtoCook());
        assertFalse(player.isKilledCulinaromancer());
        assertNoAdventure();
        reply(1184,15);
        assertClosed();
        assertEquals(8,start().stageValue());
    }

    @Test public void completedPlayerGetsSafeEndingWithoutErasingCompletion() {
        player.setKilledCulinaromancer(true);
        FixedCook cook=start();
        assertEquals(8,cook.stageValue());
        reply(1184,15);
        assertEquals(UNAVAILABLE,cook.lastNpcText());
        assertTrue(player.isKilledCulinaromancer());
        assertFalse(player.hasTalkedtoCook());
        assertNoAdventure();
        reply(1184,15);
        assertClosed();
        assertEquals(8,start().stageValue());
    }

    @Test public void forbiddenControllerKeyCannotConstructOrReplaceTheActiveController() throws Exception {
        withRegistration(()->{
            ProbeController current=installCurrent();
            Object[] arguments=player.getControlerManager().getLastControlerArguments();
            player.getControlerManager().startControler(KEY,99);
            assertCurrentUnchanged(current,arguments);
            assertEquals(0,CountingJad.constructed);
            assertNull(player.getNextWorldTile());
        });
    }

    @Test public void forbiddenControllerInstanceCannotStartOrReplaceTheActiveController() {
        ProbeController current=installCurrent();
        Object[] arguments=player.getControlerManager().getLastControlerArguments();
        CountingJad blocked=new CountingJad();
        player.getControlerManager().startControler(blocked,99);
        assertCurrentUnchanged(current,arguments);
        assertEquals(0,blocked.starts);
        assertNull(blocked.getPlayer());
        assertNull(player.getNextWorldTile());
    }

    @Test public void savedForbiddenKeyIsRetiredBeforeConstructionOrLogin() throws Exception {
        withRegistration(()->{
            ControlerManager manager=player.getControlerManager();
            set(manager,"lastControler",KEY);
            manager.setLastControlerArguments(new Object[] {5});
            manager.login();
            assertEquals(0,CountingJad.constructed);
            assertNull(manager.getControler());
            assertNull(manager.getLastControler());
            assertNull(manager.getLastControlerArguments());
            assertTrue(manager.canWalk());
            assertNull(player.getNextWorldTile());
            assertFalse(player.hasTalkedtoCook());
        });
    }

    @Test public void otherProfilesStillAdmitTheSameControllerKeyInstanceAndSavedLogin() throws Exception {
        withRegistration(()->{
            for (ClientProfile profile : new ClientProfile[] {ClientProfile.LEGACY_910,ClientProfile.NATIVE_947}) {
                profile(profile);
                ControlerManager manager=player.getControlerManager();
                manager.startControler(KEY,3);
                CountingJad keyed=CountingJad.last;
                assertSame(keyed,manager.getControler());
                assertEquals(1,keyed.starts);
                assertEquals(KEY,manager.getLastControler());
                manager.forceStop();
                CountingJad instance=new CountingJad();
                manager.startControler(instance,4);
                assertSame(instance,manager.getControler());
                assertEquals(1,instance.starts);
                manager.forceStop();
                set(manager,"lastControler",KEY);
                manager.setLastControlerArguments(new Object[] {6});
                manager.login();
                assertSame(CountingJad.last,manager.getControler());
                assertEquals(1,CountingJad.last.logins);
                assertEquals(KEY,manager.getLastControler());
                manager.forceStop();
            }
        });
    }

    @Test public void otherProfilesKeepOriginalCookAcceptanceAtStageSix() throws Exception {
        for (ClientProfile profile : new ClientProfile[] {ClientProfile.LEGACY_910,ClientProfile.NATIVE_947}) {
            profile(profile);
            set(player,"talkedtoCook",false);
            FixedCook cook=new FixedCook(false);
            cook.setPlayer(player);
            cook.setStage(6);
            cook.run(1184,15);
            assertTrue(player.hasTalkedtoCook());
            assertEquals(7,cook.stageValue());
            assertFalse(cook.npcTexts.contains(UNAVAILABLE));
        }
    }

    private FixedCook start() {
        FixedCook cook=new FixedCook(true);
        player.getDialogueManager().startDialogue(cook,278);
        assertSame(cook,player.getDialogueManager().getDialogue());
        return cook;
    }
    private void reply(int panel,int component) {
        // Literal native101 field order: component hash BE, then slot 0xffff as ff 7f.
        interactions.handle(Native950Actions.decode(101,new byte[] {
                (byte)(panel>>>8),(byte)panel,(byte)(component>>>8),(byte)component,(byte)0xff,0x7f}));
    }
    private void assertClosed() {
        assertFalse(player.getDialogueManager().hasDialogue());
        assertFalse(player.getNative950Dialogues().isOpen());
        assertFalse(player.getInterfaceManager().containsChatBoxInter());
    }
    private void assertNoAdventure() {
        assertEquals(3209,player.getX());assertEquals(3215,player.getY());assertEquals(0,player.getPlane());
        assertNull(player.getNextWorldTile());
        assertNull(player.getControlerManager().getControler());
        assertNull(player.getControlerManager().getLastControler());
        assertNull(player.getControlerManager().getLastControlerArguments());
    }
    private ProbeController installCurrent() {
        ProbeController current=new ProbeController();
        player.getControlerManager().startControler(current,"keep-me");
        assertEquals(1,current.starts);
        return current;
    }
    private void assertCurrentUnchanged(ProbeController current,Object[] arguments) {
        ControlerManager manager=player.getControlerManager();
        assertSame(current,manager.getControler());
        assertSame(arguments,manager.getLastControlerArguments());
        assertEquals("ProbeController",manager.getLastControler());
        assertEquals(0,current.closes);
        assertFalse(manager.canWalk()); // Still initialized and consulting the old controller.
    }
    private void profile(ClientProfile profile) throws Exception {
        Constructor<ClientSession> ctor=ClientSession.class.getDeclaredConstructor(ClientProfile.class);
        ctor.setAccessible(true);
        set(player,"clientSession",ctor.newInstance(profile));
        assertEquals(profile,player.getClientProfile());
    }
    private static void set(Object target,String name,Object value) throws Exception {
        Field field=target.getClass().getDeclaredField(name);field.setAccessible(true);field.set(target,value);
    }
    private interface Exercise { void run() throws Exception; }
    @SuppressWarnings("unchecked") private static void withRegistration(Exercise exercise) throws Exception {
        Field field=ControllerHandler.class.getDeclaredField("handledControlers");field.setAccessible(true);
        Map<Object,Class<Controller>> registry=(Map<Object,Class<Controller>>)field.get(null);
        Class<Controller> previous=registry.put(KEY,(Class<Controller>)(Class<?>)CountingJad.class);
        try { exercise.run(); }
        finally { if(previous==null)registry.remove(KEY);else registry.put(KEY,previous); }
    }
    public static final class CountingJad extends ImpossibleJad {
        static int constructed; static CountingJad last;
        int starts,logins,closes;
        public CountingJad(){constructed++;last=this;}
        @Override public void start(){starts++;}
        @Override public boolean login(){logins++;return false;}
        @Override public void forceClose(){closes++;}
    }
    private static final class ProbeController extends Controller {
        int starts,closes;
        @Override public void start(){starts++;}
        @Override public void forceClose(){closes++;}
        @Override public boolean canWalk(){return false;}
    }
    private static final class FixedCook extends Cook {
        final List<String> npcTexts=new ArrayList<>();
        private final boolean render;
        FixedCook(boolean render){this.render=render;}
        int stageValue(){return stage;}
        String lastNpcText(){return npcTexts.get(npcTexts.size()-1);}
        @Override public boolean sendNPCDialogue(int npcId,int animation,String... text) {
            npcTexts.add(String.join(" ",text));
            // Only cache name lookup is replaced. Real Cook state and native speech bridge remain.
            return !render || sendEntityDialogue(IS_NPC,"Cook",npcId,animation,text);
        }
        @Override public boolean sendPlayerDialogue(int animation,String... text) {
            return !render || super.sendPlayerDialogue(animation,text);
        }
    }
    private static Native950Content content() {
        Native950ItemCatalog items=new Native950ItemCatalog(Arrays.asList(
                new Native950ItemCatalog.Entry(995,"Coins",true,new String[]{"Add to pouch"}),
                new Native950ItemCatalog.Entry(1511,"Logs",false,new String[]{"Craft"}),
                new Native950ItemCatalog.Entry(315,"Shrimps",false,new String[]{"Eat"})));
        int[] amounts=new int[11];amounts[1]=amounts[2]=1;amounts[3]=5;amounts[4]=10;amounts[7]=Integer.MAX_VALUE;
        return new Native950Content(items,new Native950Content.BankUi(517,201,15,317,39,
                amounts,amounts,Collections.emptyList(),Collections.emptyList()));
    }
}
