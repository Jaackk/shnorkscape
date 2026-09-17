package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.game.player.client.ui.Native950CacheReader;
import com.rs.network.protocol.modern950.Native950Actions.InterfaceAction;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.io.InputStream;
import java.util.*;

/** Native activity list/details and session favourites; never admits a minigame or starts transport. */
public final class Native950Minigames {
    private static final Properties DATA=load();
    private static final int[][] ROWS=rows();
    private static final int[] ENUMS={6452,6452,8014,8015,8016,8017};
    private static Object verifiedStore;
    private final Player player;
    private final Channel channel;
    private boolean open,filterArmed;
    private int filter,selected;
    public Native950Minigames(Player player,Channel channel){this.player=Objects.requireNonNull(player);this.channel=Objects.requireNonNull(channel);}
    public boolean isOpen(){return open;}
    public int category(){return filter;}
    public int selectedActivity(){return selected<1?-1:ROWS[filter][selected-1];}
    public static int size(int category){return category>=0&&category<ROWS.length?ROWS[category].length:0;}
    public static int activity(int category,int actor){return category>=0&&category<ROWS.length&&actor>=0&&actor<ROWS[category].length?ROWS[category][actor]:-1;}
    public boolean favourite(int structure){int bit=favouriteBit(structure);return bit>=0&&player.getVarsManager().getBitValue(bit)!=0;}
    public void opened(int menu,int page){
        if(menu!=3||page!=4)return;verifyCacheBindings();open=true;filterArmed=false;render();
        for(int component:new int[]{9,31,34,56,73,82})channel.write(Native950Packets.interfaceEvents(1344,component,-1,-1,2));
    }
    public void closed(int menu,int page){
        if(menu!=3||page!=4||!open)return;open=false;closeFilter();
        channel.write(Native950Packets.interfaceEvents(1344,23,0,ROWS[0].length-1,0));
        for(int component:new int[]{9,31,34,56,73,82})channel.write(Native950Packets.interfaceEvents(1344,component,-1,-1,0));
    }
    public boolean handle(InterfaceAction a){
        if(!open||player.getInterfaceManager().getInterfaceParentId(1344)!=(1448<<16|3))return false;
        if(a.interfaceId()==1477&&a.componentId()==896){
            if(!filterArmed)return false;closeFilter();
            if(a.option()==1&&a.itemId()==-1&&a.slot()>=0&&a.slot()<ROWS.length){
                int old=selectedActivity();filter=a.slot();selected=0;
                for(int i=0;i<ROWS[filter].length;i++)if(ROWS[filter][i]==old&&(filter!=1||favourite(old))){selected=i+1;break;}
                render();
            }return true;
        }
        if(a.interfaceId()!=1344)return false;
        if(a.option()!=1||a.itemId()!=-1)return true;
        if(a.componentId()==23){
            int id=activity(filter,a.slot());
            if(id>=0&&(filter!=1||favourite(id))){selected=a.slot()+1;render();}
            return true;
        }
        if(a.slot()!=-1)return true;
        if(a.componentId()==31){filterArmed=true;channel.write(Native950Packets.interfaceEvents(1477,896,0,5,2));return true;}
        if(a.componentId()==34){
            int id=selectedActivity();if(id<0)return true;
            int bit=favouriteBit(id);if(bit<0)return true;
            boolean now=!favourite(id);bit(bit,now?1:0);if(filter==1&&!now)selected=0;render();
            channel.write(Native950Packets.gameMessage(0,now?"Activity added to your favourites for this session.":"Activity removed from your favourites."));return true;
        }
        channel.write(Native950Packets.gameMessage(0,"Minigame participation and travel are not available yet. You can browse activity information and favourites."));return true;
    }
    private void render(){
        bit(20794,filter);var(3233,selected);
        //Hit actors keep the original enum ordinal even when favourites filter hides earlier rows.
        channel.write(Native950Packets.interfaceEvents(1344,23,0,ROWS[0].length-1,0));
        channel.write(Native950Packets.interfaceEvents(1344,23,0,ROWS[filter].length-1,2));
        channel.write(Native950Packets.runClientScript(6743));
        channel.write(Native950Packets.runClientScript(6745));
        //There is no local Spotlight schedule or thaler bonus. Do not show the native template promise.
        channel.write(Native950Packets.interfaceText(1344,3,"Spotlight"));
        channel.write(Native950Packets.interfaceText(1344,13,"Not active on this server"));
        channel.write(Native950Packets.interfaceText(1344,14,""));
    }
    private void closeFilter(){if(filterArmed){channel.write(Native950Packets.runClientScript(10444));channel.write(Native950Packets.interfaceEvents(1477,896,0,5,0));}filterArmed=false;}
    private static int favouriteBit(int id){return Integer.parseInt(DATA.getProperty("favorite."+id,"-1"));}
    private void bit(int id,int value){player.getVarsManager().setVarBit(id,value);channel.write(Native950Packets.varbitLarge(id,value));}
    private void var(int id,int value){player.getVarsManager().setVar(id,value);channel.write(Native950Packets.varp(id,value));}
    public static synchronized void verifyCacheBindings(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Activity browser requires the paired950 cache");
        if(verifiedStore==Cache.STORE)return;Native950CacheReader reader=new Native950CacheReader.Flat();
        for(String key:DATA.stringPropertyNames())if(key.startsWith("pin.")){
            String[] id=key.substring(4).split("/");String hash=reader.sha256(Integer.parseInt(id[0]),Integer.parseInt(id[1]),Integer.parseInt(id[2]));
            if(!DATA.getProperty(key).equals(hash))throw new IllegalStateException("Changed activity browser cache "+key);
        }
        for(int f=0;f<ROWS.length;f++)for(int i=0;i<ROWS[f].length;i++)if(reader.enumInt(ENUMS[f],i+1)!=ROWS[f][i])throw new IllegalStateException("Changed activity row "+f+":"+i);
        verifiedStore=Cache.STORE;
    }
    private static Properties load(){Properties p=new Properties();try(InputStream in=Native950Minigames.class.getResourceAsStream("/native950/minigames-950.properties")){
        if(in==null)throw new IllegalStateException("Missing activity catalog");p.load(in);
    }catch(java.io.IOException ex){throw new IllegalStateException(ex);}if(!"1".equals(p.getProperty("catalog.version")))throw new IllegalStateException("Unknown activity catalog");return p;}
    private static int[][] rows(){int[][] result=new int[6][];for(int f=0;f<result.length;f++){String[] values=DATA.getProperty("category."+f).split(",");result[f]=new int[values.length];for(int i=0;i<values.length;i++)result[f][i]=Integer.parseInt(values[i]);}return result;}
}
