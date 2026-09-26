package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;
import com.rs.utils.data.parsers.npcs.NPCStatsDataParser;
import io.netty.channel.embedded.EmbeddedChannel;
import java.lang.reflect.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

/**
 * Real-cache Save lifecycle through the actual console and its client notifications:
 * select NPC -> client zoom report -> Save -> select another NPC -> reselect -> assert
 * the resolved default and the exact CS21135 the client receives. No socket or live file.
 */
public final class Native950DeveloperPreviewSaveLifecycleAcceptance {
    static Native950DeveloperConsole console;static EmbeddedChannel channel;
    static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
    static Object field(String n)throws Exception{Field f=Native950DeveloperConsole.class.getDeclaredField(n);f.setAccessible(true);return f.get(console);}
    static void set(String n,Object v)throws Exception{Field f=Native950DeveloperConsole.class.getDeclaredField(n);f.setAccessible(true);f.set(console,v);}
    static void call(String n)throws Exception{Method m=Native950DeveloperConsole.class.getDeclaredMethod(n);m.setAccessible(true);m.invoke(console);}
    static void notify(String text)throws Exception{
        Constructor<Native950Actions.StringDialogueAction> c=Native950Actions.StringDialogueAction.class.getDeclaredConstructor(boolean.class,String.class);c.setAccessible(true);
        console.handle(c.newInstance(false,text));
    }
    static List<Native950Packets.Packet> drain(){
        channel.flush();List<Native950Packets.Packet> r=new ArrayList<>();Object o;
        while((o=channel.readOutbound())!=null)if(o instanceof Native950Packets.Packet)r.add((Native950Packets.Packet)o);return r;
    }
    static boolean sent(List<Native950Packets.Packet> ps,Native950Packets.Packet want){
        for(Native950Packets.Packet p:ps)if(p.type()==want.type()&&Arrays.equals(p.payload(),want.payload()))return true;return false;
    }
    @SuppressWarnings("unchecked")
    static int actorFor(int npc)throws Exception{
        for(Map.Entry<Integer,Native950DeveloperCatalogue.Entry> e:((Map<Integer,Native950DeveloperCatalogue.Entry>)field("rowEntities")).entrySet())if(e.getValue().id==npc)return e.getKey();
        throw new AssertionError("No row for NPC "+npc);
    }
    static void click(int actor)throws Exception{notify("__devop:"+field("epoch")+":"+actor);}
    static String grouped(int n){return String.format(java.util.Locale.US,"%,d",n);}
    static Native950DeveloperPreview preview()throws Exception{return (Native950DeveloperPreview)field("preview");}

    public static void main(String[] args)throws Exception{
        Cache.initFlatReadOnly(Paths.get(args[0]));
        NPCCombatDefinitionsDataParser.init();NPCStatsDataParser.init();
        Path dir=Files.createTempDirectory("dev-preview-lifecycle-");
        System.setProperty("ataraxia950.devPreviewOverrides",dir.resolve("overrides.txt").toString());Native950DeveloperPreviewOverrides.reload();
        System.setProperty("ataraxia950.devPreferences",dir.toString());
        System.setProperty(Native950DevelopmentCommands.PROPERTY,"true");System.setProperty(Native950AdminCommands.ACCOUNTS,"tester");
        channel=new EmbeddedChannel(){@Override protected SocketAddress remoteAddress0(){return new InetSocketAddress("127.0.0.1",43594);}};
        Player p=Player.createNative950("tester",new WorldTile(3217,3258,0),channel);p.setActive(true);
        console=new Native950DeveloperConsole(p,channel,()->{},s->{},()->{});
        try{
            console.open();notify("__devready");
            Method nav=Native950DeveloperConsole.class.getDeclaredMethod("navigate",String.class);nav.setAccessible(true);nav.invoke(console,"NPCs");
            set("query","vorago");call("render");
            int vorago=-1,other=-1;
            for(Native950DeveloperCatalogue.Entry e:Native950DeveloperCatalogue.groupedNpcs("vorago")){
                if(vorago<0&&e.name.equalsIgnoreCase("Vorago")&&Native950DeveloperPreview.resolve(e.id).npc>=0)vorago=e.id;
                else if(other<0&&!e.name.equalsIgnoreCase("Vorago")&&Native950DeveloperPreview.resolve(e.id).npc>=0)other=e.id;
            }
            check(vorago>=0&&other>=0,"Need Vorago and another previewable row");
            int automatic=Native950DeveloperPreview.resolve(vorago).zoom,otherAutomatic=Native950DeveloperPreview.resolve(other).zoom;

            // 1. Select Vorago, as the row click does.
            click(actorFor(vorago));drain();
            check(preview().npc==vorago&&preview().zoom==automatic,"Selection did not resolve Vorago's automatic default");
            // 2. The client applies Zoom + three times locally and reports what it shows.
            int shown=automatic-450;long epoch=(Long)field("epoch");
            // Exactly as the live client formats them: native 0x86b groups thousands ("3,050").
            notify("__devzoom:"+epoch+":"+vorago+":"+grouped(automatic-150));notify("__devzoom:"+epoch+":"+vorago+":"+grouped(automatic-300));notify("__devzoom:"+epoch+":"+vorago+":"+grouped(shown));
            notify("__devzoom:"+epoch+":"+vorago+":12a");notify("__devzoom:"+epoch+":"+vorago+":123456");notify("__devzoom:"+epoch+":"+other+":"+grouped(999));notify("__devzoom:"+(epoch-1)+":"+vorago+":"+grouped(700));
            check((Integer)field("currentZoom")==shown,"Server did not record the reported zoom (or accepted a bad/stale/foreign one); has "+field("currentZoom"));
            // 3. Save: the real button, the actor right after Zoom +.
            int saveActor=((int[])field("zoomActors"))[2]+1;click(saveActor);
            List<Native950Packets.Packet> afterSave=drain();
            check(Native950DeveloperPreviewOverrides.get("npc:"+vorago)!=null&&Native950DeveloperPreviewOverrides.get("npc:"+vorago)==shown,"Save stored "+Native950DeveloperPreviewOverrides.get("npc:"+vorago)+", expected "+shown);
            check(Files.readAllLines(dir.resolve("overrides.txt")).contains("npc:"+vorago+"="+shown),"Override file lacks npc:"+vorago+"="+shown);
            for(Native950Packets.Packet pk:afterSave)check(!sent(Collections.singletonList(pk),Native950Packets.runClientScript(21135,(Integer)field("previewChild"),preview().npc,preview().idle,automatic,preview().height,preview().nativeFraming?111:176)),"Save rebuilt the model at the old default");
            // 4. Select another NPC: it keeps its own default.
            click(actorFor(other));drain();
            check(preview().npc==other&&preview().zoom==otherAutomatic,"Other NPC inherited Vorago's saved zoom");
            // 5. Return to Vorago: the saved zoom is resolved and sent to the client.
            click(actorFor(vorago));List<Native950Packets.Packet> reselect=drain();
            Native950DeveloperPreview back=preview();
            check(back.npc==vorago&&back.zoom==shown,"Reselection resolved "+back.zoom+" instead of saved "+shown);
            check(sent(reselect,Native950Packets.runClientScript(21135,(Integer)field("previewChild"),back.npc,back.idle,shown,back.height,back.nativeFraming?111:176)),"Client was not sent the saved zoom in CS21135");
            check(sent(reselect,Native950Packets.runClientScript(21148,((int[])field("zoomActors"))[1],(Integer)field("previewChild"),0,shown,"__devzoom:"+field("epoch")+":"+vorago+":")),"Reset is not bound to the saved zoom");
            check((Integer)field("currentZoom")==shown,"Server zoom after reselection is not the saved zoom");
            // 6. A full redraw (any other control) still restores the saved zoom.
            call("render");check(preview().zoom==shown,"Redraw lost the saved zoom");
            System.out.println("Save lifecycle PASS: Vorago #"+vorago+" automatic "+automatic+" -> saved "+shown+"; other #"+other+" kept "+otherAutomatic+"; reselection sent CS21135 zoom "+shown+"; Reset bound to "+shown+".");
        }finally{console.dispose();channel.finishAndReleaseAll();}
    }
}
