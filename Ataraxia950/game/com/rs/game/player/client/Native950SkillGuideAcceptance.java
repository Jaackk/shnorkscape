package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.Paths;
import java.util.Arrays;

/** Read-only paired-cache + actual session recipe; no listener, account or live client required. */
public final class Native950SkillGuideAcceptance {
    public static void main(String[] args) throws Exception {
        if(args.length!=1)throw new IllegalArgumentException("Usage: Native950SkillGuideAcceptance <950-flat-cache>");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950SkillGuide.verify();
        int packets=0;
        EmbeddedChannel channel=new EmbeddedChannel();
        try {
            Player player=Player.createNative950("guide-cache-probe",new WorldTile(3217,3258,0),channel);
            player.setActive(true);player.getInterfaceManager().registerNativeOpen(1482,1477,30);
            Native950SkillGuide guide=new Native950SkillGuide(player,channel);
            for(int slot=0;slot<29;slot++) {
                int hash=(1466<<16)|7;
                Native950Actions.InterfaceAction click=(Native950Actions.InterfaceAction)Native950Actions.decode(18,
                    new byte[]{-1,-1,-1,(byte)(hash>>>16),(byte)(hash>>>24),(byte)hash,(byte)(hash>>>8),0,(byte)slot});
                require(guide.handle(click),"Unrecognized skill slot "+slot);
                require(guide.isOpen() && player.getInterfaceManager().getInterfaceParentId(1217)==(1218<<16),"Content not owned");
                boolean selected=false,filled=false;channel.flushOutbound();Object next;
                while((next=channel.readOutbound())!=null) {
                    Native950Packets.Packet packet=(Native950Packets.Packet)next;packets++;
                    selected|=same(packet,Native950Packets.runClientScript(5682,Native950SkillGuide.guideArgument(Native950SkillGuide.skillForCell(slot))));
                    filled|=same(packet,Native950Packets.openSub(1218,0,1217,false));
                }
                require(selected && filled,"Missing selection/content recipe for slot "+slot);
                guide.close();channel.flushOutbound();
                while((next=channel.readOutbound())!=null) {
                    Native950Packets.Packet packet=(Native950Packets.Packet)next;packets++;
                    require(!same(packet,Native950Packets.closeSub(1477,30)),"Scene mount closed");
                }
                require(player.getInterfaceManager().getInterfaceParentId(1482)==((1477<<16)|30),"Scene bookkeeping lost");
                for(int iface:new int[]{1448,1218,1217})require(!player.getInterfaceManager().containsInterface(iface),"Guide child remains open");
            }
            // Exercise changes inside one still-open Hero window, not just reopen cycles.
            require(guide.handle(button(1466,7,0)),"Initial guide request failed");
            channel.flushOutbound();while(channel.readOutbound()!=null)packets++;
            int icons=0;
            for(int component=0;component<265;component++) {
                int skill=Native950SkillGuide.skillForComponent(component);
                if(skill<0)continue;
                icons++;
                require(guide.handle(button(1218,component,-1)),"Guide icon rejected "+component);
                packets+=expect(channel,Native950Packets.runClientScript(5690));
                require(guide.handle(button(1218,262,-1)),"Filter rejected after icon "+component);
                packets+=expect(channel,Native950Packets.runClientScript(5691,Native950SkillGuide.guideArgument(skill)));
                require(player.getInterfaceManager().getInterfaceParentId(1217)==(1218<<16),"Content ownership changed");
            }
            require(icons==29,"Missing current guide icons");
            for(int slot=0;slot<29;slot++) {
                require(guide.handle(button(1466,7,slot)),"External skill change rejected "+slot);
                packets+=expect(channel,Native950Packets.runClientScript(5682,
                        Native950SkillGuide.guideArgument(Native950SkillGuide.skillForCell(slot))),
                        Native950Packets.runClientScript(5690));
            }
            guide.close();channel.flushOutbound();while(channel.readOutbound()!=null)packets++;
        }
        finally { channel.finishAndReleaseAll(); }
        System.out.println("PASS: all29 paired950 skill enum/struct/guide bindings, strict interface/script pins and " + packets + " session packets;29 in-place icon changes and29 external skill changes preserve Hero, and close/reopen preserves the game scene.");
        System.out.println("LIMIT: native client drawing, dragging, dropdowns and Escape remain manual visual checks.");
    }
    private static Native950Actions.InterfaceAction button(int iface,int component,int slot) {
        int hash=(iface<<16)|component;
        return (Native950Actions.InterfaceAction)Native950Actions.decode(18,
                new byte[]{-1,-1,-1,(byte)(hash>>>16),(byte)(hash>>>24),(byte)hash,(byte)(hash>>>8),(byte)(slot>>>8),(byte)slot});
    }
    private static int expect(EmbeddedChannel channel,Native950Packets.Packet... expected) {
        channel.flushOutbound();
        for(Native950Packets.Packet packet:expected) {
            Object actual=channel.readOutbound();
            require(actual instanceof Native950Packets.Packet && same((Native950Packets.Packet)actual,packet),"Unexpected in-place skill update");
        }
        require(channel.readOutbound()==null,"Skill selection rebuilt the Hero layout or remounted its content");
        return expected.length;
    }
    private static boolean same(Native950Packets.Packet a,Native950Packets.Packet b) {return a.type()==b.type() && Arrays.equals(a.payload(),b.payload());}
    private static void require(boolean value,String message) {if(!value)throw new IllegalStateException(message);}
}
