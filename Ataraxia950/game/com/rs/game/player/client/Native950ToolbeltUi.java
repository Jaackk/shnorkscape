package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.util.Objects;

/** Current950 surface toolbelt. Its cache scripts own category tabs, scrolling and tool previews. */
public final class Native950ToolbeltUi {
    public static final int INTERFACE=1944,ROOT=1477,HOST=735,WRAPPER=732;
    private final Player player;
    private final Channel channel;
    private boolean open,verified;
    public Native950ToolbeltUi(Player player,Channel channel){this.player=Objects.requireNonNull(player);this.channel=Objects.requireNonNull(channel);}
    public boolean isOpen(){return open;}
    public static boolean isOpenRequest(Native950Actions.InterfaceAction a){
        // CS8471 ->16559(1462:35,3), enum5137[2] -> DB row1200 "Tool belt".
        // Native actor2/child1 serializes as ((actor+15)<<8)|child =0x1101.
        return a.interfaceId()==1462&&a.componentId()==35&&a.slot()==4353&&a.itemId()==-1&&a.option()==1;
    }
    public void bootstrap(){
        // Lightweight packet/serialization fixtures intentionally have no paired cache.
        if(Cache.STORE==null&&!NativeCacheVerification.isEnforced())return;
        verifyBeforeOpen();Native950Toolbelt.sync(player);
        channel.write(Native950Packets.interfaceEvents(1462,35,4353,4353,2));
    }
    public void verifyBeforeOpen(){if(!verified){Native950Toolbelt.verifyCacheBindings();verified=true;}}
    public boolean handle(Native950Actions.InterfaceAction a){
        if(isOpenRequest(a)){
            verifyBeforeOpen();
            if(!Native950Toolbelt.ready(player)){player.sendMessage("You cannot open the tool belt during this action.");return true;}
            if(!open)open();return true;
        }
        if(!open||a.interfaceId()!=INTERFACE)return false;
        if(a.componentId()==102&&a.option()==1&&a.slot()==-1&&a.itemId()==-1){close();return true;}
        if(a.componentId()!=7||a.slot()<0||a.slot()>=Native950Toolbelt.MAX_ITEMS||(a.option()!=1&&a.option()!=2))return true;
        // The hitbox is not an item container; reject forged/stale item-bearing operations.
        if(a.itemId()!=-1)return true;
        if(a.option()==2)player.sendMessage(Native950Toolbelt.remove(player,a.slot()));
        channel.write(Native950Packets.interfaceText(INTERFACE,27,Native950Toolbelt.describe(player,a.slot())));
        return true;
    }
    private void open(){
        Native950Toolbelt.sync(player);
        channel.write(Native950Packets.openSub(ROOT,HOST,INTERFACE,false));
        player.getInterfaceManager().registerNativeOpen(INTERFACE,ROOT,HOST);open=true;
        channel.write(Native950Packets.hideInterface(ROOT,WRAPPER,false));
        channel.write(Native950Packets.runClientScript(14097,0));
        channel.write(Native950Packets.interfaceEvents(INTERFACE,7,0,Native950Toolbelt.MAX_ITEMS-1,6));
        channel.write(Native950Packets.interfaceEvents(INTERFACE,102,-1,-1,2));
        // Bonecrusher/Herbicide/etc toggles depend on unported Slayer unlocks and special actions.
        //12 owns the entire button:17 is only the hitbox;19/20 hold its gear artwork.
        channel.write(Native950Packets.hideInterface(INTERFACE,12,true));
        channel.write(Native950Packets.interfaceEvents(INTERFACE,17,-1,-1,0));
        channel.write(Native950Packets.runClientScript(1364));
    }
    public void close(){
        if(!open)return;open=false;
        channel.write(Native950Packets.closeSub(ROOT,HOST));
        player.getInterfaceManager().unregisterNativeOpen(INTERFACE);
        channel.write(Native950Packets.hideInterface(ROOT,WRAPPER,true));
        channel.write(Native950Packets.runClientScript(1364));
    }
}
