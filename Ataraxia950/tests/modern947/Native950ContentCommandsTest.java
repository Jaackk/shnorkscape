package com.rs.game.player.client;

import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

public class Native950ContentCommandsTest {
    private EmbeddedChannel channel;private Player p;private String previous;
    @Before public void setup() {
        previous=System.getProperty(Native950DevelopmentCommands.PROPERTY);System.setProperty(Native950DevelopmentCommands.PROPERTY,"true");
        channel=new EmbeddedChannel(){@Override protected SocketAddress remoteAddress0(){return new InetSocketAddress("127.0.0.1",43650);}};
        p=Player.createNative950("gear-test",new WorldTile(3217,3258,0),channel);p.setActive(true);p.setRights(2);
        List<Native950ItemCatalog.Entry> entries=new ArrayList<>();Set<Integer> ids=new HashSet<>();
        for(String kit:new String[]{"weapons"})for(Item item:Native950ContentCommands.kit(kit))
            if(ids.add(item.getId()))entries.add(new Native950ItemCatalog.Entry(item.getId(),"Test item",item.getId()==58036||item.getId()==58041,new String[5]));
        Native950Skilling.attach(p,new Native950Containers(p,new Native950ItemCatalog(entries)));
    }
    @After public void cleanup(){Native950Skilling.detach(p);channel.finishAndReleaseAll();if(previous==null)System.clearProperty(Native950DevelopmentCommands.PROPERTY);else System.setProperty(Native950DevelopmentCommands.PROPERTY,previous);}
    private void run(String text){Native950DevelopmentCommands.handle(p,channel,text);}
    @Test public void stackedNpcRemovalKeepsEveryOtherInstance() {
        com.rs.game.npc.NPC first=com.rs.game.npc.NPC.createNative950(12353,new WorldTile(3217,3258,0),1);
        com.rs.game.npc.NPC second=com.rs.game.npc.NPC.createNative950(12353,new WorldTile(3217,3258,0),1);
        List<com.rs.game.npc.NPC> roster=new ArrayList<>(Arrays.asList(first,second));
        assertTrue(Native950World.removeExactNpc(roster,second));assertEquals(1,roster.size());assertSame(first,roster.get(0));
        assertFalse(Native950World.removeExactNpc(roster,second));assertSame(first,roster.get(0));
    }
    @Test public void quickLoadoutsReuseLibraryEquipmentAndSuppliesWithoutFood(){
        Item[] eq=new Item[19],inv=new Item[28];eq[3]=new Item(16403,1);inv[0]=new Item(42251,1);inv[1]=new Item(556,10000);
        Item[] quick=Native950ContentCommands.quickItems(new Native950DeveloperLoadouts.Loadout("fixture",inv,eq));
        assertEquals(2,quick.length);assertEquals(16403,quick[0].getId());assertEquals(10000,quick[1].getAmount());
        assertEquals(42251,inv[0].getId());assertNotSame(eq[3],quick[0]);
        for(String[] aliases:new String[][]{{"melee","meleegear"},{"range","rangegear","ragegear"},{"mage","magegear"},{"necro","necrogear"}})
            for(String alias:aliases)assertEquals(Native950ContentCommands.bestStyle(aliases[0]),Native950ContentCommands.bestStyle(alias));
    }
    @Test public void fullInventoryNeverGrantsPartialKit(){
        for(int i=0;i<25;i++)p.getInventory().items.set(i,new Item(51848,1));
        run(";;weapons");assertEquals(25,p.getInventory().getItems().getUsedSlots());
    }
    @Test public void searchUsesActual950NamesAndIncludesVariantIdentity() {
        assertTrue(Native950ContentCommands.search(false,"torva full helm").stream().anyMatch(s->s.startsWith("20135: Torva full helm")));
        assertTrue(Native950ContentCommands.search(false,"torva full helm").stream().anyMatch(s->s.contains("noted; base=20135")));
        assertFalse(Native950ContentCommands.search(true,"goblin").isEmpty());
        assertEquals(Native950ContentCommands.search(true,"GOBLIN"),Native950ContentCommands.search(true,"goblin"));
        assertTrue(Native950ContentCommands.itemMatches("20135",60).stream().anyMatch(e->e.id==20135));
        assertTrue(Native950ContentCommands.itemMatches("TORVA",3).size()<=3);
        assertTrue(Native950ContentCommands.itemMatches("torva full helm",60).stream().anyMatch(e->e.label().contains("ID 20135")));
        assertEquals(20135,Native950ContentCommands.itemMatches("torva full helm",60).get(0).id);
        assertEquals(40,Native950ContentCommands.testingKitItemBrowserEntries().size());
        run(";;search torva 2");run(";;findnpc goblin 1");run(";;gearhelp");
    }
    @Test public void itemBrowserSearchDoesNotInventADeadPagerOrTruncateMatches() {
        assertTrue(Native950ContentCommands.itemMatches("torva",Integer.MAX_VALUE).size()>5);
        assertEquals(40,Native950ContentCommands.testingKitItemBrowserEntries().size());
    }
    @Test public void aliasesAndPermissionAndMalformedRequests() {
        for(String name:new String[]{"search","find","si","itemid","finditem","findnpc","snpc"})assertTrue(Native950DevelopmentCommands.isCommand(";;"+name+" name"));
        assertTrue(Native950DevelopmentCommands.isCommand(";;items"));
        p.setRights(0);run(";;weapons");assertEquals(28,p.getInventory().getFreeSlots());p.setRights(2);
        run(";;gear unknown");run(";;meleegear extra");run(";;search torva -1");run(";;removenpc bad");
        assertEquals(28,p.getInventory().getFreeSlots());run(";;gear weapons");
        assertEquals(1,p.getInventory().getAmountOf(16403));
        assertEquals(0,p.getInventory().getAmountOf(9244));
    }
}
