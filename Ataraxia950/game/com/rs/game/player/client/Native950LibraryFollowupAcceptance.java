package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.cache.loaders.BodyDefinitions;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.controllers.Controller;
import com.rs.network.protocol.modern950.Native950Actions;
import io.netty.channel.embedded.EmbeddedChannel;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Real-cache, ephemeral-player acceptance. Never logs in, listens, or loads/saves an account. */
public final class Native950LibraryFollowupAcceptance {
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        System.setProperty(Native950DevelopmentCommands.PROPERTY,"true");System.setProperty(Native950AdminCommands.ACCOUNTS,"loadoutprobe");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World.getInstance().execute(()->{BodyDefinitions.init();check();return null;}).get(90,TimeUnit.SECONDS);
        System.out.println("PASS: global non-curated search, stable claims, nine metadata-validated full loadouts, atomic displacement, custom isolation, permissions and return-to-bank storage");
    }
    private static void check()throws Exception{
        Native950EquipmentLibraryAssets.verify();
        Native950EquipmentCatalogue catalogue=Native950EquipmentCatalogue.current();
        List<Native950DeveloperSearch.Row> universe=Native950DeveloperSearch.current();
        require(universe.size()>30000,"Global universe unexpectedly narrowed");
        long start=System.nanoTime();
        List<Native950EquipmentCatalogue.Entry> logs=Native950DeveloperSearch.find(universe,"LOGS");
        require(logs.stream().anyMatch(e->e.id==1511),"Global search misses ordinary logs");
        require(catalogue.entries.stream().noneMatch(e->e.id==1511),"Fixture must be outside curated tabs");
        System.out.println("Global search: "+universe.size()+" definitions; query+matched hash checks "+((System.nanoTime()-start)/1000000)+"ms");
        try(Fixture a=new Fixture("loadoutprobe");Fixture b=new Fixture("otherprobe")){
            java.lang.reflect.Field presets=a.p.getBank().getClass().getDeclaredField("bankPresets");presets.setAccessible(true);
            Object realPresets=presets.get(a.p.getBank());
            Item[][] bank=a.p.getBank().bankTabs;Native950EquipmentLibrary.open(a.p);
            a.library.handle(text("__devlib:begin"));a.library.handle(text("__devlib:q:logs"));
            require(a.p.getBank().bankTabs==bank,"Search changed bank");
            a.library.handle(text("__devlib:cancel"));a.library.close();
            a.p.getInventory().items.set(0,new Item(995,321));a.p.getEquipment().getItems().set(3,new Item(1277,1));
            a.p.getBank().bankTabs=new Item[][]{{new Item(995,100)}};
            a.p.getBank().restoreNativePreferences(true,73,5);
            Native950DeveloperLoadouts own=new Native950DeveloperLoadouts(),other=new Native950DeveloperLoadouts();
            for(int i=0;i<9;i++){
                Native950DeveloperLoadouts.Loadout loadout=own.get(i);Map<Integer,Long> before=totals(a.p);
                String result=Native950DeveloperLoadouts.apply(a.p,loadout);require(result.startsWith("Equipped"),result);
                Map<Integer,Long> expected=new TreeMap<>(before);add(expected,loadout.inventory);add(expected,loadout.equipment);
                require(totals(a.p).equals(expected),"Loadout destroyed, withdrew or overgranted property: "+loadout.name);
                require(a.p.getInventory().items.getFreeSlots()==0,"Supply inventory not populated");
                System.out.println(loadout.name+": "+Native950CacheItems.definition(loadout.equipment[3].getId()).name+
                        ", T"+Native950CombatStyles.profile(loadout.equipment[3].getId()).tier+", occupied equipment slots="+Arrays.stream(loadout.equipment).filter(Objects::nonNull).count());
            }
            require(a.p.getBank().getWithdrawNotes()&&a.p.getBank().getLastX()==73,"Real bank preferences changed");
            require(realPresets==presets.get(a.p.getBank()),"Real bank preset storage changed");
            require(b.p.getInventory().items.isEmpty()&&b.p.getEquipment().getItems().isEmpty(),"Second player contaminated");
            Native950EquipmentLibrary.open(a.p);
            a.library.handle(button(1,169,5));
            a.library.handle(text("__devlib:begin"));a.library.handle(text("__devlib:q:1511"));
            a.library.handle(text("__devlib:cancel"));
            java.lang.reflect.Field tab=Native950EquipmentLibrary.class.getDeclaredField("selectedTab");tab.setAccessible(true);
            require(tab.getInt(a.library)==5,"Search cancellation lost previous curated tab");
            a.library.handle(button(1,153,-1));
            a.library.handle(button(1,267,10));a.library.handle(button(1,303,-1));
            java.lang.reflect.Field definitions=Native950EquipmentLibrary.class.getDeclaredField("loadouts");definitions.setAccessible(true);
            Native950DeveloperLoadouts uiLoadouts=(Native950DeveloperLoadouts)definitions.get(a.library);
            require(uiLoadouts.get(9)!=null,"Native Overwrite did not capture developer custom slot");
            require(((Native950DeveloperLoadouts)definitions.get(b.library)).get(9)==null,"Native custom save crossed players");
            Map<Integer,Long> uiBefore=totals(a.p),uiExpected=new TreeMap<>(uiBefore);
            add(uiExpected,uiLoadouts.get(9).inventory);add(uiExpected,uiLoadouts.get(9).equipment);
            a.library.handle(button(1,84,-1));require(totals(a.p).equals(uiExpected),"Native Load failed safe item conservation");
            a.library.handle(button(1,84,-1));require(totals(a.p).equals(uiExpected),"Duplicate native Load granted twice in one tick");
            a.library.handle(button(1,267,1));a.library.handle(button(1,303,-1));
            require(uiLoadouts.get(0).equipment[3].getId()==16403,"Native Overwrite changed a built-in");
            a.library.handle(button(1,86,-1));a.library.close();
            require(realPresets==presets.get(a.p.getBank()),"Native preset handlers changed real presets");
            own.capture(9,a.p);require(own.get(9)!=null&&other.get(9)==null,"Custom preset isolation");
            Map<Integer,Long> before=totals(a.p);a.p.getControlerManager().startControler(new Controller(){public void start(){}});
            require(!Native950DeveloperLoadouts.apply(a.p,own.get(0)).startsWith("Equipped"),"Controller bypass");require(before.equals(totals(a.p)),"Refusal changed property");
            a.p.getControlerManager().forceStop();
            a.p.getSkills().setXpWithoutRefresh(Skills.ATTACK,0);
            require(!Native950DeveloperLoadouts.apply(a.p,own.get(0)).startsWith("Equipped"),"Equipment level bypass");require(before.equals(totals(a.p)),"Requirement refusal changed property");
            Native950EquipmentLibrary.open(a.p);a.library.close();
            int coin=-1;for(int i=0;i<a.p.getBank().bankTabs[0].length;i++)if(a.p.getBank().bankTabs[0][i].getId()==995)coin=i;
            a.p.getInventory().items.set(27,null); // Test-owned food removed solely to make one free test slot.
            require(a.c.withdraw(coin,995,1).moved==1,"Real bank could not withdraw after library close");
        }
    }
    private static Native950Actions.StringDialogueAction text(String s){byte[] raw=Arrays.copyOf(s.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1),s.length()+1);return (Native950Actions.StringDialogueAction)Native950Actions.decode(17,raw);}
    private static Native950Actions.InterfaceAction button(int option,int component,int slot)throws Exception{
        java.lang.reflect.Constructor<Native950Actions.InterfaceAction> c=Native950Actions.InterfaceAction.class.getDeclaredConstructor(int.class,int.class,int.class,int.class);c.setAccessible(true);
        return c.newInstance(option,(517<<16)|component,slot,-1);
    }
    private static Map<Integer,Long> totals(Player p){Map<Integer,Long> out=new TreeMap<>();add(out,p.getInventory().items.getItems());add(out,p.getEquipment().getItems().getItems());for(Item[] tab:p.getBank().bankTabs)add(out,tab);return out;}
    private static void add(Map<Integer,Long> map,Item[] items){for(Item item:items)if(item!=null)map.put(item.getId(),map.getOrDefault(item.getId(),0L)+item.getAmount());}
    private static final class Fixture implements AutoCloseable {
        final EmbeddedChannel channel=new EmbeddedChannel(){@Override protected SocketAddress remoteAddress0(){return new InetSocketAddress("127.0.0.1",12345);}};
        final Player p;
        final Native950Containers c;
        final Native950EquipmentLibrary library;
        Fixture(String name){p=Player.createNative950(name,new WorldTile(3217,3258,0),channel);c=new Native950Containers(p,new Native950ItemCatalog(Collections.emptyList()).withLegacyDrops());p.setActive(true);p.setRights(2);for(int i=0;i<Skills.SKILL_COUNT;i++)p.getSkills().setXpWithoutRefresh(i,Skills.getXPForLevel(i,120));Native950Skilling.attach(p,c);library=new Native950EquipmentLibrary(p,channel,()->{});}
        public void close(){library.dispose();Native950Skilling.detach(p);channel.finishAndReleaseAll();}
    }
}
