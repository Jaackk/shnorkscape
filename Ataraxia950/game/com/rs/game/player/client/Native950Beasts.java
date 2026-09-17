package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.game.player.client.ui.Native950CacheReader;
import com.rs.network.protocol.modern950.Native950Actions.InterfaceAction;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.io.InputStream;
import java.util.*;

/** Current-cache boss information browser. Navigation owns753; this never starts encounters. */
public final class Native950Beasts {
    private static final Properties DATA=load();
    private static final int[] BOSSES=ids(DATA.getProperty("bosses"));
    private static Object verifiedStore;
    private final Player player;
    private final Channel channel;
    private boolean open,category;
    private int selected=0;
    public Native950Beasts(Player player,Channel channel){this.player=Objects.requireNonNull(player);this.channel=Objects.requireNonNull(channel);}
    public boolean isOpen(){return open;}
    public int selectedBoss(){return BOSSES[selected];}
    public static int size(){return BOSSES.length;}
    public void opened(int menu,int page){
        if(menu!=3||page!=5)return;
        verifyCacheBindings();open=true;category=false;
        //753:0 onLoad794 clears the list and installs hooks;3371 actually draws enum9031.
        channel.write(Native950Packets.varp(4517,0));
        channel.write(Native950Packets.runClientScript(3371));
        channel.write(Native950Packets.interfaceEvents(753,5,0,BOSSES.length-1,2));
        channel.write(Native950Packets.interfaceEvents(753,120,-1,-1,2));
        details();
    }
    public void closed(int menu,int page){
        if(menu!=3||page!=5||!open)return;
        open=false;closeCategory();
        channel.write(Native950Packets.interfaceEvents(753,5,0,BOSSES.length-1,0));
        channel.write(Native950Packets.interfaceEvents(753,120,-1,-1,0));
    }
    public boolean handle(InterfaceAction a){
        if(!open||player.getInterfaceManager().getInterfaceParentId(753)!=(1448<<16|3))return false;
        if(a.interfaceId()==1477&&a.componentId()==896){
            if(!category)return false;closeCategory();
            if(a.option()==1&&a.itemId()==-1&&a.slot()>=0&&a.slot()<3){
                if(a.slot()!=0)channel.write(Native950Packets.gameMessage(0,"Boss and Slayer kill records are not available yet. You can browse boss information."));
                channel.write(Native950Packets.varp(4517,0));channel.write(Native950Packets.runClientScript(3371));details();
            }return true;
        }
        if(a.interfaceId()!=753)return false;
        if(a.option()!=1||a.itemId()!=-1)return true;
        if(a.componentId()==5&&a.slot()>=0&&a.slot()<BOSSES.length){selected=a.slot();details();return true;}
        if(a.componentId()==120&&a.slot()==-1){category=true;channel.write(Native950Packets.interfaceEvents(1477,896,0,2,2));return true;}
        // Encounters, achievements, collection progress and transport have separate authoritative systems.
        if(a.slot()==-1&&a.componentId()!=23)channel.write(Native950Packets.gameMessage(0,"That boss action is not available yet. This page currently provides boss information."));
        return true;
    }
    private void details(){
        channel.write(Native950Packets.varc(4485,BOSSES[selected]));
        channel.write(Native950Packets.runClientScript(3869));
    }
    private void closeCategory(){
        if(category){channel.write(Native950Packets.runClientScript(10444));channel.write(Native950Packets.interfaceEvents(1477,896,0,2,0));}
        category=false;
    }
    public static synchronized void verifyCacheBindings(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Boss browser requires the paired950 cache");
        if(verifiedStore==Cache.STORE)return;
        Native950CacheReader reader=new Native950CacheReader.Flat();
        for(String key:DATA.stringPropertyNames())if(key.startsWith("pin.")){
            String[] id=key.substring(4).split("/");String hash=reader.sha256(Integer.parseInt(id[0]),Integer.parseInt(id[1]),Integer.parseInt(id[2]));
            if(!DATA.getProperty(key).equals(hash))throw new IllegalStateException("Changed boss browser cache "+key);
        }
        for(int i=0;i<BOSSES.length;i++)if(reader.enumInt(9031,i)!=BOSSES[i])throw new IllegalStateException("Changed boss row "+i);
        verifiedStore=Cache.STORE;
    }
    private static Properties load(){
        Properties p=new Properties();try(InputStream in=Native950Beasts.class.getResourceAsStream("/native950/beasts-950.properties")){
            if(in==null)throw new IllegalStateException("Missing boss browser catalog");p.load(in);
        }catch(java.io.IOException ex){throw new IllegalStateException(ex);}
        if(!"1".equals(p.getProperty("catalog.version")))throw new IllegalStateException("Unknown boss browser catalog");return p;
    }
    private static int[] ids(String value){String[] raw=value.split(",");int[] ids=new int[raw.length];for(int i=0;i<ids.length;i++)ids[i]=Integer.parseInt(raw[i]);return ids;}
}
