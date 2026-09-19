package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.utils.Utils;
import io.netty.channel.Channel;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.Properties;

/** Paired950 quick-options exit surface; the native session alone saves and removes the player. */
public final class Native950ExitUi {
    public static final int ROOT=1477,INTERFACE=1433,WRAPPER=805,HOST=806,ENTRY=99;
    private static volatile boolean verified;
    private final Player player;
    private final Channel channel;
    private final Runnable verifier;
    private final LogoutTransport logoutTransport;
    private final Native950LayoutEditor layoutEditor;
    private boolean open,cacheVerified,confirmation,signingOut,openingCloseAcknowledgement;

    /** The world session owns persistence; implementations must never call legacy realFinish(). */
    interface LogoutTransport { void logout(boolean toLobby); }

    public Native950ExitUi(Player player,Channel channel){
        this(player,channel,Native950ExitUi::verify,toLobby->player.getPackets().sendLogout());
    }
    Native950ExitUi(Player player,Channel channel,Runnable verifier,LogoutTransport transport){
        this.player=Objects.requireNonNull(player,"player");
        this.channel=Objects.requireNonNull(channel,"channel");
        this.verifier=Objects.requireNonNull(verifier,"verifier");
        this.logoutTransport=Objects.requireNonNull(transport,"transport");
        this.layoutEditor=new Native950LayoutEditor(player,channel);
    }
    public boolean isOpen(){return open;}
    public boolean isLayoutEditing(){return layoutEditor.isOpen();}
    public boolean isSigningOut(){return signingOut;}
    /**
     * The paired client emits CLOSE_MODAL while it swaps quick-options state for
     * this server-owned overlay.  It can repeat that acknowledgement as its
     * world-list widget initialises, so retain the overlay until the player uses
     * one of its explicit controls instead of treating the acknowledgement as a
     * request to dismiss it.
     */
    boolean consumeOpeningCloseAcknowledgement(){
        if(!open||confirmation)return false;
        openingCloseAcknowledgement=false;return true;
    }
    boolean consumeLayoutEditorClose(){return layoutEditor.consumeNativeClose();}
    public static boolean isOpenRequest(Native950Actions.InterfaceAction a){
        // enum7716[1004] ->21278 param3507 is the minimap frame's exit actor layer.
        return a.interfaceId()==ROOT&&a.componentId()==ENTRY&&a.slot()==1
                &&a.itemId()==-1&&a.option()==1;
    }
    public void verifyBeforeOpen(){if(!cacheVerified){verifier.run();cacheVerified=true;}}
    public void bootstrap(){
        if(Cache.STORE==null&&!NativeCacheVerification.isEnforced())return;
        verifyBeforeOpen();
        // The old frontend placed1433 in the Dialogue Box overlay. No conversation exists at bootstrap.
        channel.write(Native950Packets.closeSub(ROOT,751));
        if(player.getInterfaceManager().getInterfaceParentId(INTERFACE)==(ROOT<<16|751))
            player.getInterfaceManager().unregisterNativeOpen(INTERFACE);
        channel.write(Native950Packets.interfaceEvents(ROOT,ENTRY,1,1,2));
    }

    public boolean handle(Native950Actions.InterfaceAction a){
        if(signingOut)return isOpenRequest(a)||a.interfaceId()==INTERFACE;
        if(layoutEditor.handle(a))return true;
        if(isOpenRequest(a)){
            verifyBeforeOpen();
            if(!player.isActive()||player.hasFinished()||player.isDead())return true;
            if(open)close();else open();
            return true;
        }
        if(!open)return false;
        if(a.itemId()!=-1||a.option()!=1||a.slot()!=-1)return false;
        if((a.interfaceId()==ROOT&&(a.componentId()==8||a.componentId()==809))
                ||(a.interfaceId()==INTERFACE&&a.componentId()==79)){
            close();return true;
        }
        if(a.interfaceId()!=INTERFACE)return false;
        switch(a.componentId()){
            case 22: close();layoutEditor.open();return true;
            case 43: close();player.sendMessage("For local-server issues, describe what happened and the steps to reproduce it to the server developer.");return true;
            case 66: close();player.sendMessage("World 1 is the only local world currently available.");return true;
            case 69: player.sendMessage("Returning to the lobby is not available yet.");return true;
            case 72:
                confirmation=true;
                // Keep the native confirmation, but do not store an unimplemented "don't show again" preference.
                channel.write(Native950Packets.hideInterface(INTERFACE,62,false));
                channel.write(Native950Packets.hideInterface(INTERFACE,90,true));
                channel.write(Native950Packets.interfaceEvents(INTERFACE,86,-1,-1,2));
                channel.write(Native950Packets.interfaceEvents(INTERFACE,89,-1,-1,2));
                return true;
            case 86:
                if(confirmation)requestLogout(false);
                return true;
            case 89:
                confirmation=false;
                channel.write(Native950Packets.runClientScript(4166));
                return true;
            case 92:
                // Hidden server-owned preference; stale or forged clicks cannot skip confirmation.
                return true;
            default:return false; // management/settings/world-list owners claim their own entries
        }
    }

    private void open(){
        //1477:805 is the cache's dedicated full-screen quick-options wrapper.806 is an empty child
        //beneath its native click shield808; scripts8177/8179 own wrapper visibility and input context.
        // This is an input-blocking confirmation, not a walkable HUD subinterface.
        channel.write(Native950Packets.openSub(ROOT,HOST,INTERFACE,true));
        player.getInterfaceManager().registerNativeOpen(INTERFACE,ROOT,HOST);
        open=true;confirmation=false;openingCloseAcknowledgement=true;
        channel.write(Native950Packets.runClientScript(8177));
        //8177 shows805 before touching legacy274:192 and initializing13831. Ensure the
        //owned roots and modern layout are applied directly, independently of that preamble.
        channel.write(Native950Packets.hideInterface(ROOT,WRAPPER,false));
        channel.write(Native950Packets.hideInterface(ROOT,HOST,false));
        channel.write(Native950Packets.hideInterface(INTERFACE,0,false));
        channel.write(Native950Packets.runClientScript(13831,1));
        System.out.println("[Ataraxia950] Exit menu queued open for player "+player.getIndex()
                +"; parent="+player.getInterfaceManager().getInterfaceParentId(INTERFACE));
        channel.write(Native950Packets.hideInterface(INTERFACE,67,true));
        // Native font/artwork/text and layout come from13831->2935, not910component numbers.
        channel.write(Native950Packets.interfaceEvents(INTERFACE,6,0,6,2));
        for(int component:new int[]{15,22,35,36,43,66,72,79,86,89})
            channel.write(Native950Packets.interfaceEvents(INTERFACE,component,-1,-1,2));
        channel.write(Native950Packets.interfaceEvents(ROOT,809,-1,-1,2));
    }

    private void requestLogout(boolean toLobby){
        String refusal=logoutRefusal(player);
        if(refusal!=null){player.sendMessage(refusal);return;}
        // Invoke controller restrictions before stopping actions; a veto leaves the world unchanged.
        if(!player.getControlerManager().canLogout()){
            player.sendMessage("You cannot log out from this activity right now.");return;
        }
        if(signingOut)return;
        signingOut=true;
        player.getActionManager().forceStop();
        player.resetWalkSteps();player.setRouteEvent(null);
        close();
        // Socket closure runs on Netty's event loop. Inactivity also rejects input already queued
        //in this world tick, before closeFuture schedules the session's checkpoint and cleanup.
        player.setActive(false);
        logoutTransport.logout(toLobby);
    }

    static String logoutRefusal(Player p){
        if(!p.isNative950()||!p.isActive()||p.hasFinished()||p.isDead())return "You cannot log out right now.";
        long now=Utils.currentTimeMillis();
        if(p.isLocked()||p.getLunarDelay()>=now||p.getNextWorldTile()!=null||p.isNative950ForceMovementActive())
            return "You cannot log out while performing an action.";
        if(p.isUnderCombat(10)||p.getAttackingDelay()+10000>now)
            return "You cannot log out until 10 seconds after combat.";
        if(p.getEmotesManager().isDoingEmote())return "You cannot log out while performing an emote.";
        return null;
    }

    public void close(){
        layoutEditor.requestCancel();
        if(!open)return;
        open=false;confirmation=false;openingCloseAcknowledgement=false;
        int parent=player.getInterfaceManager().getInterfaceParentId(INTERFACE);
        System.out.println("[Ataraxia950] Exit menu queued close for player "+player.getIndex()
                +"; parent="+parent+"; owned="+(parent==(ROOT<<16|HOST))
                +"; caller="+new Throwable().getStackTrace()[1]);
        if(parent!=(ROOT<<16|HOST))return;
        channel.write(Native950Packets.runClientScript(8179));
        channel.write(Native950Packets.runClientScript(8180,1,1));
        channel.write(Native950Packets.interfaceEvents(ROOT,8,-1,-1,254));
        channel.write(Native950Packets.closeSub(ROOT,HOST));
        player.getInterfaceManager().unregisterNativeOpen(INTERFACE);
    }

    public static synchronized void verify(){
        if(verified)return;
        if(!Cache.isFlatReadOnly())throw new IllegalStateException("Exit UI requires the paired950 cache");
        Properties pins=new Properties();
        try(InputStream in=Native950ExitUi.class.getResourceAsStream("/native950/exit-ui-950.properties")){
            if(in==null)throw new IllegalStateException("Missing950 exit UI cache bindings");
            pins.load(in);
            MessageDigest digest=MessageDigest.getInstance("SHA-256");
            for(String key:pins.stringPropertyNames()){
                String[] words=key.split("\\.");
                int index=Integer.parseInt(words[0]),group=Integer.parseInt(words[1]),file=Integer.parseInt(words[2]);
                byte[] data=Cache.STORE.getIndexes()[index].getFile(group,file);
                if(data==null)throw new IllegalStateException("Missing exit UI cache file "+key);
                StringBuilder hex=new StringBuilder();
                for(byte b:digest.digest(data))hex.append(String.format("%02x",b&255));
                if(!hex.toString().equals(pins.getProperty(key)))throw new IllegalStateException("Changed950 exit UI cache file "+key);
            }
        }catch(RuntimeException e){throw e;}catch(Exception e){throw new IllegalStateException("Cannot verify950 exit UI",e);}
        verified=true;
    }
}
