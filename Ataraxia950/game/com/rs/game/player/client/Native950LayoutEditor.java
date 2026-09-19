package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.Properties;

/** Owns the native revision-950 saved-layout editor surface. */
final class Native950LayoutEditor {
    static final int ROOT=1477,HOST=692,INTERFACE=1475,EDIT_MODE_VARC=3477;
    private static volatile boolean verified;
    private final Player player;
    private final Channel channel;
    private final Runnable verifier;
    private boolean open;

    Native950LayoutEditor(Player player,Channel channel){
        this(player,channel,Native950LayoutEditor::verify);
    }
    Native950LayoutEditor(Player player,Channel channel,Runnable verifier){
        this.player=Objects.requireNonNull(player,"player");
        this.channel=Objects.requireNonNull(channel,"channel");
        this.verifier=Objects.requireNonNull(verifier,"verifier");
    }

    boolean isOpen(){return open;}

    void open(){
        if(open)return;
        verifier.run();
        channel.write(Native950Packets.openSub(ROOT,HOST,INTERFACE,true));
        player.getInterfaceManager().registerNativeOpen(INTERFACE,ROOT,HOST);
        channel.write(Native950Packets.varcSmall(EDIT_MODE_VARC,1));
        open=true;
    }

    /** Reconciles a CLOSE_MODAL already performed by the native client. */
    boolean consumeClientClose(){
        if(!open)return false;
        open=false;
        if(player.getInterfaceManager().getInterfaceParentId(INTERFACE)==(ROOT<<16|HOST))
            player.getInterfaceManager().unregisterNativeOpen(INTERFACE);
        channel.write(Native950Packets.varcSmall(EDIT_MODE_VARC,0));
        return true;
    }

    void close(){
        if(!open)return;
        open=false;
        channel.write(Native950Packets.varcSmall(EDIT_MODE_VARC,0));
        if(player.getInterfaceManager().getInterfaceParentId(INTERFACE)!=(ROOT<<16|HOST))return;
        channel.write(Native950Packets.closeSub(ROOT,HOST));
        player.getInterfaceManager().unregisterNativeOpen(INTERFACE);
    }

    static synchronized void verify(){
        if(verified)return;
        if(!Cache.isFlatReadOnly())throw new IllegalStateException("Layout editor requires the paired950 cache");
        Properties pins=new Properties();
        try(InputStream in=Native950LayoutEditor.class.getResourceAsStream("/native950/layout-editor-950.properties")){
            if(in==null)throw new IllegalStateException("Missing950 layout editor cache bindings");
            pins.load(in);
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            for(String key:pins.stringPropertyNames()){
                String[] words=key.split("\\.");
                int index=Integer.parseInt(words[0]),group=Integer.parseInt(words[1]),file=Integer.parseInt(words[2]);
                byte[] data=Cache.STORE.getIndexes()[index].getFile(group,file);
                if(data==null)throw new IllegalStateException("Missing layout editor cache file "+key);
                StringBuilder hex=new StringBuilder();
                for(byte b:digest.digest(data))hex.append(String.format("%02x",b&255));
                if(!hex.toString().equals(pins.getProperty(key)))
                    throw new IllegalStateException("Changed950 layout editor cache file "+key);
            }
        }catch(RuntimeException e){throw e;}catch(Exception e){throw new IllegalStateException("Cannot verify950 layout editor",e);}
        verified=true;
    }
}
