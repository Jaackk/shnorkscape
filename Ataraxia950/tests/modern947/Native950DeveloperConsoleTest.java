package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

public class Native950DeveloperConsoleTest {
    private String enabled,accounts,prefs;private EmbeddedChannel channel;private Player p;private Path dir;
    private Native950DeveloperConsole console;
    @Before public void setup()throws Exception{
        enabled=System.getProperty(Native950DevelopmentCommands.PROPERTY);accounts=System.getProperty(Native950AdminCommands.ACCOUNTS);prefs=System.getProperty("ataraxia950.devPreferences");
        System.setProperty(Native950DevelopmentCommands.PROPERTY,"true");System.setProperty(Native950AdminCommands.ACCOUNTS,"tester");
        dir=Files.createTempDirectory("dev950-");System.setProperty("ataraxia950.devPreferences",dir.toString());
        channel=new EmbeddedChannel(){@Override protected SocketAddress remoteAddress0(){return new InetSocketAddress("127.0.0.1",43594);}};
        p=Player.createNative950("tester",new WorldTile(3217,3258,0),channel);p.setActive(true);
        console=new Native950DeveloperConsole(p,channel,()->{},s->Native950DevelopmentCommands.handle(p,channel,s),()->{});
    }
    @After public void cleanup()throws Exception{
        console.dispose();channel.finishAndReleaseAll();restore(Native950DevelopmentCommands.PROPERTY,enabled);restore(Native950AdminCommands.ACCOUNTS,accounts);restore("ataraxia950.devPreferences",prefs);
        try(java.util.stream.Stream<Path> paths=Files.walk(dir)){for(Path path:(Iterable<Path>)paths.sorted(Comparator.reverseOrder())::iterator)Files.delete(path);}
    }
    private void restore(String key,String value){if(value==null)System.clearProperty(key);else System.setProperty(key,value);}
    @Test public void gamevalBrowserUsesExistingReadyLifecycleAndRejectsStaleActions()throws Exception{
        Native950DeveloperConsole.gamevalsFor(p,"623:27");assertTrue(console.isOpen());
        java.lang.reflect.Field browser=Native950DeveloperConsole.class.getDeclaredField("browser"),query=Native950DeveloperConsole.class.getDeclaredField("query"),waiting=Native950DeveloperConsole.class.getDeclaredField("awaitingNative");
        browser.setAccessible(true);query.setAccessible(true);waiting.setAccessible(true);
        assertEquals("Gameval",browser.get(console));assertEquals("623:27",query.get(console));assertTrue(waiting.getBoolean(console));
        console.handle(notification("__devready"));assertFalse(waiting.getBoolean(console));
        Native950DeveloperConsole.gamevalsFor(p,"residual_soul");assertFalse(waiting.getBoolean(console));assertEquals("residual_soul",query.get(console));
        assertFalse(p.isDevelopmentGodMode());console.close();assertFalse(console.isOpen());
        console.handle(notification("__devready"));assertFalse(console.isOpen());
    }
    @Test public void aliasesSearchAcrossCategoriesAndParameterizedCommandsUseRealHandlers(){
        assertEquals("almighty",Native950DeveloperActions.search("Items","DM",Collections.emptySet()).get(0).id);
        Native950DeveloperActions.Action a=Native950DeveloperActions.find("adrenaline");
        assertEquals(";;adrenaline 67",a.command(Arrays.asList("67")));
        Native950DeveloperActions.execute(p,channel,null,a,Arrays.asList("67"));assertEquals(67,p.getCombatDefinitions().getSpecialAttackPercentage());
        try{a.command(Arrays.asList("101"));fail();}catch(IllegalArgumentException expected){}
        try{Native950DeveloperActions.find("teleto").command(Arrays.asList("nooby;;god"));fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void godAndAlmightyStateComesFromSharedAccountState(){
        Native950DeveloperActions.Action god=Native950DeveloperActions.find("god"),dm=Native950DeveloperActions.find("almighty");
        Native950DeveloperActions.execute(p,channel,null,god,Collections.emptyList());
        assertTrue(Native950DeveloperActions.state(god,p).contains("ON"));assertTrue(Native950DeveloperActions.state(dm,p).contains("OFF"));
        Native950DevelopmentCommands.handle(p,channel,";;dm");assertTrue(Native950DeveloperActions.state(dm,p).contains("ON"));
    }
    @Test public void preferencesAreIndependentAndRoundTripWithoutCharacterSave()throws Exception{
        Set<String> mine=new LinkedHashSet<>(Arrays.asList("heal","infammo"));Native950DeveloperPreferences.save("tester",mine);
        assertEquals(mine,Native950DeveloperPreferences.load("tester"));assertFalse(Native950DeveloperPreferences.load("nooby").contains("infammo"));
        assertEquals(2,Native950DeveloperActions.search("Favourites","",mine).size());
    }
    @Test public void permissionRevocationClosesAndStaleActorsCannotExecute()throws Exception{
        console.open();assertTrue(console.isOpen());
        System.clearProperty(Native950AdminCommands.ACCOUNTS);console.tick();assertFalse(console.isOpen());
        assertFalse(console.handle(button(1448,5,0)));assertFalse(p.isDevelopmentGodMode());
        try{Native950DeveloperActions.execute(p,channel,null,Native950DeveloperActions.find("god"),Collections.emptyList());fail();}catch(IllegalArgumentException expected){}
    }
    @Test public void closePreservesSavedActionBarsAndIndependentSecondPlayer()throws Exception{
        p.getNative950ActionBar().setActiveBar(p,channel,2);
        Player other=Player.createNative950("nooby",new WorldTile(3217,3258,0),channel);other.setActive(true);
        Native950DeveloperConsole second=new Native950DeveloperConsole(other,channel,()->{},s->{throw new AssertionError();},()->{});
        try{
            second.open();assertFalse(second.isOpen());console.open();assertTrue(console.isOpen());
            System.setProperty(Native950AdminCommands.ACCOUNTS,"tester,nooby");second.open();assertTrue(second.isOpen());
            console.handle(button(1477,717,1));assertFalse(console.isOpen());assertEquals(2,p.getNative950ActionBar().activeBar());assertTrue(second.isOpen());
        }finally{second.dispose();}
    }
    @Test public void allDirectoryActionsHaveStableUniqueIdentitiesAndValidConfiguredDefaults(){
        Set<String> ids=new HashSet<>();for(Native950DeveloperActions.Action a:Native950DeveloperActions.all()){
            assertTrue(ids.add(a.id));assertFalse(a.description.isEmpty());
            if(a.configured&&!a.defaults().contains(""))assertTrue(Native950DevelopmentCommands.isCommand(a.command(a.defaults())));
        }
    }
    @Test public void nonceRejectsOldRowsAndPlainButtonCannotRunAction()throws Exception{
        console.open();console.handle(notification("__devready"));java.lang.reflect.Field ep=Native950DeveloperConsole.class.getDeclaredField("epoch");ep.setAccessible(true);
        java.lang.reflect.Field bs=Native950DeveloperConsole.class.getDeclaredField("buttons");bs.setAccessible(true);
        final int[] calls={0};((Map<Integer,Runnable>)bs.get(console)).put(0,()->calls[0]++);
        long epoch=ep.getLong(console);
        console.handle(notification("__devop:"+(epoch-1)+":0"));console.handle(button(1448,5,0));assertEquals(0,calls[0]);
        console.handle(notification("__devop:"+epoch+":0"));assertEquals(1,calls[0]);
        p.lock(10);console.handle(notification("__devop:"+epoch+":0"));assertEquals(1,calls[0]);
        console.close();console.handle(notification("__devop:"+epoch+":0"));assertEquals(1,calls[0]);
    }
    @Test public void cancelledMoveAndLogoutDiscardPendingTileClaim()throws Exception{
        console.open();java.lang.reflect.Field move=Native950DeveloperConsole.class.getDeclaredField("moving"),pending=Native950DeveloperConsole.class.getDeclaredField("placement");move.setAccessible(true);pending.setAccessible(true);
        Native950DeveloperWorldEdits.Edit edit=new Native950DeveloperWorldEdits.Edit(UUID.randomUUID().toString(),"tester","NPC",1,3217,3258,0,-1,0,false,false);
        move.set(console,edit);pending.set(console,new Native950DeveloperPlacement.Request(new Native950DeveloperCatalogue.Entry(new String[]{"NPC","1","Test","1","1","1",""}),1,1,-1,0,false,p,0));
        console.handle(notification("__devcancel:1"));assertFalse(console.isOpen());assertNull(move.get(console));assertNull(pending.get(console));
        console.open();console.dispose();assertFalse(console.isOpen());
    }
    @Test public void worldCapacityFailureReturnsToConsoleInsteadOfDisconnecting()throws Exception{
        console.open();console.handle(notification("__devready"));java.lang.reflect.Field ep=Native950DeveloperConsole.class.getDeclaredField("epoch"),bs=Native950DeveloperConsole.class.getDeclaredField("buttons");ep.setAccessible(true);bs.setAccessible(true);
        ((Map<Integer,Runnable>)bs.get(console)).put(0,()->{throw new IllegalStateException("World is full");});
        console.handle(notification("__devop:"+ep.getLong(console)+":0"));assertTrue(console.isOpen());assertTrue(channel.isOpen());
    }
    @Test public void markerIsSetBeforeRefreshAndClearedBeforeUnmount() {
        console.open();
        List<com.rs.network.protocol.modern950.Native950Packets.Packet> packets=drain();
        int mount=index(packets,com.rs.network.protocol.modern950.Native950Packets.openSub(1477,715,1448,true));
        int marker=index(packets,com.rs.network.protocol.modern950.Native950Packets.runClientScript(21130,"SHNORKSCAPE Developer Console"));
        int refresh=index(packets,com.rs.network.protocol.modern950.Native950Packets.varcLarge(2911,1));
        assertTrue(mount>=0&&marker>mount&&refresh>marker);
        console.close();packets=drain();
        marker=index(packets,com.rs.network.protocol.modern950.Native950Packets.runClientScript(21130,""));
        int unmount=index(packets,com.rs.network.protocol.modern950.Native950Packets.closeSub(1477,715));
        assertTrue(marker>=0&&unmount>marker);
    }
    private List<com.rs.network.protocol.modern950.Native950Packets.Packet> drain(){
        channel.flush();List<com.rs.network.protocol.modern950.Native950Packets.Packet> result=new ArrayList<>();Object o;
        while((o=channel.readOutbound())!=null)if(o instanceof com.rs.network.protocol.modern950.Native950Packets.Packet)result.add((com.rs.network.protocol.modern950.Native950Packets.Packet)o);
        return result;
    }
    private int index(List<com.rs.network.protocol.modern950.Native950Packets.Packet> packets,com.rs.network.protocol.modern950.Native950Packets.Packet expected){
        for(int i=0;i<packets.size();i++)if(packets.get(i).type()==expected.type()&&Arrays.equals(packets.get(i).payload(),expected.payload()))return i;
        return -1;
    }
    @Test public void nativeReadyBarrierPreventsEarlyRenderAndRejectsDuplicateOrClosedAcknowledgement()throws Exception{
        java.lang.reflect.Field ep=Native950DeveloperConsole.class.getDeclaredField("epoch"),bs=Native950DeveloperConsole.class.getDeclaredField("buttons");ep.setAccessible(true);bs.setAccessible(true);
        console.open();long opening=ep.getLong(console);
        assertTrue(((Map<?,?>)bs.get(console)).isEmpty());
        Native950DevelopmentCommands.handle(p,channel,";;god");console.tick();
        assertEquals(opening,ep.getLong(console));assertTrue(((Map<?,?>)bs.get(console)).isEmpty());
        console.handle(notification("__devready"));long rendered=ep.getLong(console);
        assertTrue(rendered>opening);assertFalse(((Map<?,?>)bs.get(console)).isEmpty());
        console.handle(notification("__devready"));assertEquals(rendered,ep.getLong(console));
        console.close();console.handle(notification("__devready"));assertTrue(((Map<?,?>)bs.get(console)).isEmpty());assertFalse(console.isOpen());
        console.open();assertTrue(((Map<?,?>)bs.get(console)).isEmpty());
        console.handle(notification("__devop:"+rendered+":0"));assertTrue(((Map<?,?>)bs.get(console)).isEmpty());
        console.handle(notification("__devready"));assertFalse(((Map<?,?>)bs.get(console)).isEmpty());
    }
    @Test public void placementLedgerRejectsOtherOwnersAndCleansOnlyTemporaryEdits()throws Exception{
        Native950DeveloperPlacement.Request request=new Native950DeveloperPlacement.Request(new Native950DeveloperCatalogue.Entry(new String[]{"NPC","1","Test","1","1","1",""}),1,1,-1,0,false,p,0);
        Player other=Player.createNative950("nooby",new WorldTile(p),channel);other.setActive(true);
        List<Native950DeveloperWorldEdits.Edit> mine=Native950DeveloperWorldEdits.record(p,request,Arrays.asList((Object)new WorldTile(p)));
        List<Native950DeveloperWorldEdits.Edit> theirs=Native950DeveloperWorldEdits.record(other,request,Arrays.asList((Object)new WorldTile(other)));
        try{
            try{Native950DeveloperWorldEdits.delete(other,mine.get(0));fail();}catch(IllegalArgumentException expected){}
            Native950DeveloperWorldEdits.History history=new Native950DeveloperWorldEdits.History();history.created(mine);
            try{history.undo(other);fail();}catch(IllegalArgumentException expected){}
            history.undo(p);assertTrue(history.canRedo());assertTrue(Native950DeveloperWorldEdits.owned("tester").isEmpty());
            assertEquals(1,Native950DeveloperWorldEdits.owned("nooby").size());
        }finally{Native950DeveloperWorldEdits.cleanup(p);Native950DeveloperWorldEdits.cleanup(other);}
    }
    private Object field(String name)throws Exception{java.lang.reflect.Field f=Native950DeveloperConsole.class.getDeclaredField(name);f.setAccessible(true);return f.get(console);}
    private void invoke(String name,Class<?> type,Object argument)throws Exception{java.lang.reflect.Method m=Native950DeveloperConsole.class.getDeclaredMethod(name,type);m.setAccessible(true);m.invoke(console,argument);}
    @Test public void sidebarNpcAndWorldOpenSemanticBrowsers()throws Exception{
        console.open();console.handle(notification("__devready"));
        invoke("navigate",String.class,"NPCs");assertEquals("NPC",field("browser"));
        invoke("navigate",String.class,"World");assertEquals("Object",field("browser"));
        invoke("select",Native950DeveloperActions.Action.class,Native950DeveloperActions.find("obj"));assertEquals("Object",field("browser"));
        invoke("select",Native950DeveloperActions.Action.class,Native950DeveloperActions.find("clearnpcs"));assertEquals("Edits",field("browser"));
    }
    @Test public void rightClickHealRunsSharedHandlerAndReportsInsideConsole()throws Exception{
        console.open();console.handle(notification("__devready"));p.setHitpoints(1);drain();
        Map<Integer,Native950DeveloperActions.Action> rows=(Map<Integer,Native950DeveloperActions.Action>)field("rowActions");
        int actor=rows.entrySet().stream().filter(e->e.getValue().id.equals("heal")).findFirst().get().getKey();long epoch=(Long)field("epoch");
        console.handle(notification("__devop:"+(epoch-1)+":"+actor+":2"));assertEquals(1,p.getHitpoints());
        console.handle(notification("__devop:"+epoch+":"+actor+":2"));assertEquals(p.getMaxHitpoints(),p.getHitpoints());
        assertFalse(((List<?>)field("report")).isEmpty());
        for(com.rs.network.protocol.modern950.Native950Packets.Packet packet:drain())assertNotEquals(com.rs.network.protocol.modern950.Native950Packets.gameMessage(0,"x").type(),packet.type());
    }
    @Test public void singleClickInspectsDoubleClickRunsAndToggleRunsDirectly()throws Exception{
        console.open();console.handle(notification("__devready"));p.setHitpoints(1);
        invoke("select",Native950DeveloperActions.Action.class,Native950DeveloperActions.find("heal"));assertEquals(1,p.getHitpoints());
        invoke("select",Native950DeveloperActions.Action.class,Native950DeveloperActions.find("heal"));assertEquals(p.getMaxHitpoints(),p.getHitpoints());
        invoke("select",Native950DeveloperActions.Action.class,Native950DeveloperActions.find("god"));assertTrue(p.isDevelopmentGodMode());
    }
    @Test public void physicalDoubleClickWithSameRenderTokenExecutesOnlyInspectedActionOnce()throws Exception{
        console.open();console.handle(notification("__devready"));p.setHitpoints(1);
        Map<Integer,Native950DeveloperActions.Action> rows=(Map<Integer,Native950DeveloperActions.Action>)field("rowActions");
        int actor=rows.entrySet().stream().filter(e->e.getValue().id.equals("heal")).findFirst().get().getKey();
        String click="__devop:"+field("epoch")+":"+actor;
        console.handle(notification(click));assertEquals(1,p.getHitpoints());
        console.handle(notification(click));assertEquals(p.getMaxHitpoints(),p.getHitpoints());
        p.setHitpoints(1);console.handle(notification(click));assertEquals(1,p.getHitpoints());
        // Any navigation invalidates the continuation, even if Heal was the last selection.
        invoke("navigate",String.class,"Favourites");rows=(Map<Integer,Native950DeveloperActions.Action>)field("rowActions");
        actor=rows.entrySet().stream().filter(e->e.getValue().id.equals("heal")).findFirst().get().getKey();click="__devop:"+field("epoch")+":"+actor;
        console.handle(notification(click));invoke("navigate",String.class,"NPCs");console.handle(notification(click));assertEquals(1,p.getHitpoints());
    }
    @Test public void outputCaptureIsScopedToOwnerAndRestoredAfterFailure(){
        EmbeddedChannel other=new EmbeddedChannel();Player q=Player.createNative950("nooby",new WorldTile(p),other);
        try{
            List<String> lines=Native950DeveloperOutput.run(p,channel,()->{
                assertFalse(Native950DeveloperOutput.capture(q,"other player"));assertFalse(Native950DeveloperOutput.capture(other,"other channel"));
                assertTrue(Native950DeveloperOutput.capture(p,"own message"));
                assertEquals(Arrays.asList("nested"),Native950DeveloperOutput.run(q,other,()->Native950DeveloperOutput.capture(q,"nested")));
                assertTrue(Native950DeveloperOutput.capture(channel,"own result"));
            });assertEquals(Arrays.asList("own message","own result"),lines);
            try{Native950DeveloperOutput.run(p,channel,()->{throw new IllegalStateException("test");});fail();}catch(IllegalStateException expected){}
            assertFalse(Native950DeveloperOutput.capture(p,"later combat"));assertFalse(Native950DeveloperOutput.capture(channel,"typed command"));
        }finally{other.finishAndReleaseAll();}
    }
    @Test public void indexedSearchFindsNamesIdsAndAuditedSymbolsWithoutLeakingMutableResults(){
        List<Native950DeveloperCatalogue.Entry> named=Native950DeveloperCatalogue.search("NPC","VoRaGo");assertFalse(named.isEmpty());
        assertTrue(named.stream().allMatch(e->e.name.toLowerCase(Locale.ROOT).contains("vorago")));
        assertEquals(6260,Native950DeveloperCatalogue.search("NPC","shnorkscape_general_graardor_6260").get(0).id);
        assertSame(named,Native950DeveloperCatalogue.search("NPC","vorago"));
        try{named.clear();fail();}catch(UnsupportedOperationException expected){}
        List<Native950DeveloperCatalogue.Entry> objects=Native950DeveloperCatalogue.search("Object","bank chest");assertFalse(objects.isEmpty());
        assertTrue(objects.stream().allMatch(e->e.types.length>0));
    }
    private static Native950Actions.StringDialogueAction notification(String text)throws Exception{
        java.lang.reflect.Constructor<Native950Actions.StringDialogueAction> c=Native950Actions.StringDialogueAction.class.getDeclaredConstructor(boolean.class,String.class);c.setAccessible(true);return c.newInstance(false,text);
    }
    private static Native950Actions.InterfaceAction button(int face,int component,int slot)throws Exception{
        java.lang.reflect.Constructor<Native950Actions.InterfaceAction> c=Native950Actions.InterfaceAction.class.getDeclaredConstructor(int.class,int.class,int.class,int.class);c.setAccessible(true);
        return c.newInstance(1,face<<16|component,slot,-1);
    }
}
