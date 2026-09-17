package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Properties;

/** The950 cache's generic Make-X frame1370 and product screen1371, not the modern forge37. */
final class Native950ProductionInterface {
    static final int FRAME=1370, CONTENT=1371, ROOT=1477, HOST=735, WRAPPER=732;
    static final int MENU=1168,CATEGORY=1169,PRODUCT=1170,NAMES=7881,MAXIMUM=8846,QUANTITY=8847;
    private final Player player;
    private final Channel channel;
    private boolean open;
    private static Object verifiedStore;
    Native950ProductionInterface(Player player){this.player=player;channel=player.getRealChannel();}
    boolean isOpen(){return open;}
    static synchronized void verify(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Native Make-X requires the paired950 cache");
        if(verifiedStore==Cache.STORE)return;
        Properties pins=new Properties();
        try(InputStream input=Native950ProductionInterface.class.getResourceAsStream("/native950/production-make-x-950.properties")){
            if(input==null)throw new IllegalStateException("Missing950 Make-X bindings");pins.load(input);
            for(String key:pins.stringPropertyNames())if(key.startsWith("pin.")){
                String[] parts=key.split("\\.");int index=Integer.parseInt(parts[1]),group=Integer.parseInt(parts[2]),file=Integer.parseInt(parts[3]);
                byte[] raw=Cache.STORE.getIndexes()[index].getFile(group,file);
                if(raw==null)throw new IllegalStateException("Missing950 Make-X binding "+key);
                StringBuilder hash=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(raw))hash.append(String.format("%02x",b&255));
                if(!hash.toString().equals(pins.getProperty(key)))throw new IllegalStateException("Changed950 Make-X binding "+key);
            }
            Native950ProductionUiCatalog.verify();verifiedStore=Cache.STORE;
        }catch(java.io.IOException|java.security.NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
    }
    void open(int root,int names,int category,int product,int maximum,int quantity,String title){
        verify();
        publish(root,names,category,product,maximum,quantity,title);
        channel.write(Native950Packets.runClientScript(11145,512,352,0,0,(ROOT<<16)|WRAPPER));
        channel.write(Native950Packets.runClientScript(11145,512,334,0,0,(ROOT<<16)|HOST));
        channel.write(Native950Packets.openSub(ROOT,HOST,FRAME,false));
        player.getInterfaceManager().registerNativeOpen(FRAME,ROOT,HOST);
        channel.write(Native950Packets.openSub(FRAME,0,CONTENT,true));
        player.getInterfaceManager().registerNativeOpen(CONTENT,FRAME,0);
        open=true;
        channel.write(Native950Packets.hideInterface(ROOT,WRAPPER,false));
        // Preserve pause-button bit0. Setting ordinary op1 bit1 makes both click and Space inert.
        channel.write(Native950Packets.interfaceEvents(FRAME,30,-1,-1,1));
        channel.write(Native950Packets.interfaceEvents(FRAME,32,-1,-1,2));
        channel.write(Native950Packets.interfaceEvents(CONTENT,19,0,7,2));
        channel.write(Native950Packets.interfaceEvents(CONTENT,20,0,59,2));
        channel.write(Native950Packets.interfaceEvents(CONTENT,28,-1,-1,2));
        // The cache always creates four children per enum row, even for hidden products.
        channel.write(Native950Packets.interfaceEvents(CONTENT,22,0,8191,2));
        channel.write(Native950Packets.runClientScript(1364));
    }
    void publish(int root,int names,int category,int product,int maximum,int quantity,String title){
        channel.write(Native950Packets.varp(MENU,root));channel.write(Native950Packets.varp(NAMES,names));
        channel.write(Native950Packets.varp(CATEGORY,category));channel.write(Native950Packets.varp(PRODUCT,product));
        channel.write(Native950Packets.varp(MAXIMUM,maximum));channel.write(Native950Packets.varp(QUANTITY,quantity));
        channel.write(Native950Packets.varcString(2390,title==null?"Create item":title));
    }
    void refresh(int root,int names,int category,int product,int maximum,int quantity,String title){
        publish(root,names,category,product,maximum,quantity,title);
        channel.write(Native950Packets.runClientScript(7117));
        channel.write(Native950Packets.runClientScript(7119));
        channel.write(Native950Packets.runClientScript(7123));
        channel.write(Native950Packets.runClientScript(7147));
    }
    void quantity(int maximum,int quantity){
        channel.write(Native950Packets.varp(MAXIMUM,maximum));channel.write(Native950Packets.varp(QUANTITY,quantity));
        channel.write(Native950Packets.runClientScript(7147));
    }
    void openDropdown(int count){
        //10435 creates root896 text rows;897 artwork forwards op1 through13126.
        channel.write(Native950Packets.interfaceEvents(ROOT,896,0,Math.max(0,count-1),2));
    }
    void closeDropdown(){
        channel.write(Native950Packets.interfaceEvents(ROOT,896,0,1023,0));
        channel.write(Native950Packets.runClientScript(10444));
    }
    void close(){
        if(!open)return;open=false;closeDropdown();
        //1371:15 registers context40. Unregister it directly; do not invoke a generic close callback.
        channel.write(Native950Packets.runClientScript(8841,40,0));
        channel.write(Native950Packets.closeSub(FRAME,0));channel.write(Native950Packets.closeSub(ROOT,HOST));
        player.getInterfaceManager().unregisterNativeOpen(CONTENT);player.getInterfaceManager().unregisterNativeOpen(FRAME);
        channel.write(Native950Packets.hideInterface(ROOT,WRAPPER,true));
        publish(-1,-1,-1,-1,0,0,"");
        channel.write(Native950Packets.runClientScript(11145,512,352,0,0,(ROOT<<16)|WRAPPER));
        channel.write(Native950Packets.runClientScript(8389));channel.write(Native950Packets.runClientScript(1364));
    }
}

