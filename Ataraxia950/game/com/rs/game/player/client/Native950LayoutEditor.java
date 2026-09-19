package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.Properties;

/** Owns the server mount while native revision-950 scripts own Edit Mode itself. */
final class Native950LayoutEditor {
    static final int ROOT=1477,HOST=692,INTERFACE=1475;
    static final int SAVE_COMPONENT=43,CANCEL_COMPONENT=20;
    static final int SAVE_WRAPPER=8743,CANCEL_WRAPPER=8746;
    static final int SELECTED_SAVE_TARGET_VARC=139445;
    private static final int UNUSED_SAVE_CALLBACK_ARGUMENT=0;
    private static volatile boolean verified;
    private final Player player;
    private final Channel channel;
    private final Runnable verifier;
    private boolean open;
    private Exit pending=Exit.NONE;

    private enum Exit { NONE, SAVE, CANCEL }

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
        open=true;pending=Exit.NONE;
        Native950BugTest.event(player,"workspace-editor","opened","mount",ROOT+":"+HOST,
                "interface",INTERFACE,"entryOwner","8748->2462","manual3477",false);
    }

    boolean handle(Native950Actions.InterfaceAction action){
        if(!open||action.interfaceId()!=INTERFACE||action.option()!=1
                ||action.slot()!=-1||action.itemId()!=-1)return false;
        if(action.componentId()==SAVE_COMPONENT){requestSave();return true;}
        if(action.componentId()==CANCEL_COMPONENT){requestCancel();return true;}
        return false;
    }

    private void requestSave(){
        if(pending!=Exit.NONE)return;
        pending=Exit.SAVE;
        // CS8743 forwards one callback integer to CS8754. The pinned 950 CS8754
        // never reads that first argument; it reads the selected target directly
        // from client varc139445. Zero is therefore a neutral callback placeholder,
        // not a guessed layout target.
        Native950BugTest.event(player,"workspace-editor","save-target",
                "owner","client-varc","varc",SELECTED_SAVE_TARGET_VARC,"value","client-owned");
        channel.write(Native950Packets.runClientScript(SAVE_WRAPPER,UNUSED_SAVE_CALLBACK_ARGUMENT));
        Native950BugTest.event(player,"workspace-editor","wrapper-invoked","script",SAVE_WRAPPER,
                "argument",UNUSED_SAVE_CALLBACK_ARGUMENT,"argumentUse","ignored-by-pinned-8754");
    }

    void requestCancel(){
        if(!open||pending!=Exit.NONE)return;
        pending=Exit.CANCEL;
        channel.write(Native950Packets.runClientScript(CANCEL_WRAPPER));
        Native950BugTest.event(player,"workspace-editor","wrapper-invoked","script",CANCEL_WRAPPER,
                "confirmationOwner","native-8746");
    }

    /** CLOSE_MODAL reports that the native wrapper has completed its own teardown. */
    boolean consumeNativeClose(){
        if(!open)return false;
        int parent=player.getInterfaceManager().getInterfaceParentId(INTERFACE);
        Native950BugTest.event(player,"workspace-editor","native-close","pending",pending,
                "parent",parent,"expectedParent",ROOT+":"+HOST);
        open=false;
        if(parent==(ROOT<<16|HOST)){
            player.getInterfaceManager().unregisterNativeOpen(INTERFACE);
            Native950BugTest.event(player,"workspace-editor","mount-removed","mount",ROOT+":"+HOST,
                    "method","reconcile-client-close");
        }
        Native950BugTest.event(player,"workspace-editor","edit-state-final",
                "varc",3477,"state","native-2464->0","serverWrite",false);
        pending=Exit.NONE;
        return true;
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
