package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.GeneralRequirementMap;
import com.rs.cache.loaders.VarBitDefinitions;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.Paths;
import java.util.Arrays;

/** Offline click-to-state-to-wire test against the paired950 prayer definitions. */
public final class Native950PrayerContractAcceptance {
    public static void main(String[] args)throws Exception{
        Cache.initFlatReadOnly(Paths.get("cache"));
        verifyStateScript();
        EmbeddedChannel c=new EmbeddedChannel();
        try{
            Player p=Player.createNative950("prayer-test",new WorldTile(3294,10129,0),c);p.setActive(true);
            p.getInterfaceManager().registerNativeOpen(1458,1477,1);
            p.getSkills().setXp(Skills.PRAYER,Skills.getXPForLevel(Skills.PRAYER,99));p.getSkills().set(Skills.PRAYER,99);
            p.getSkills().setXp(Skills.DEFENCE,Skills.getXPForLevel(Skills.DEFENCE,99));p.getPrayer().setPrayerpoints(990);
            require(Native950PrayerContract.logicalSlot(false,15)==13,"Melee protection identity");
            require(Native950PrayerContract.logicalSlot(false,22)==19,"Piety identity, beyond old array length");
            require(Native950PrayerContract.logicalSlot(false,16)==-1,"Unsupported necromancy is not an old prayer");
            require("Protect from Melee".equals(p.getPrayer().getPrayerMap(13,false).getStringValue(2794)),"Requirements use the intended prayer");
            click(p,15);require(p.getPrayer().usingPrayer(0,13),"Melee protection activates");
            require(Native950MeleeCombat.prayerAdjustedDamage(null,p,100,com.rs.game.Hit.HitLook.MELEE_DAMAGE)==50,"Protection mitigates native combat");
            require(Native950MeleeCombat.prayerAdjustedDamage(null,p,100,com.rs.game.Hit.HitLook.MAGIC_DAMAGE)==100,"Protection is style-specific");
            c.flush();require(hasPacket(c,Native950Packets.varbitSmall(16747,1)),"Melee icon uses950 bit16747");
            click(p,13);require(p.getPrayer().usingPrayer(0,11)&&!p.getPrayer().usingPrayer(0,13),"Protection mutual exclusion");
            click(p,13);require(!p.getPrayer().hasPrayersOn(),"Protection toggles off");
            click(p,22);require(p.getPrayer().usingPrayer(0,19),"Piety activates from native key22 without array error");
            require(p.getPrayer().getDamageMultiplier(0)>0,"Piety damage bonus is available to combat");
            require(Native950MeleeCombat.prayerAdjustedDamage(p,null,500,com.rs.game.Hit.HitLook.MELEE_DAMAGE)==540,"Piety applies its shared8% damage buff");
            require(Native950MeleeCombat.prayerAdjustedDamage(p,null,500,com.rs.game.Hit.HitLook.MAGIC_DAMAGE)==500,"Offensive prayer is style-specific");
            click(p,16);require(p.getPrayer().usingPrayer(0,19),"Unsupported entry leaves state intact");
            c.flush();while(c.readOutbound()!=null){}
            Native950PrayerContract.sync(p);c.flush();
            require(c.readOutbound()==null,"Unchanged prayer state emits no repeat configs");
            p.getPrayer().drainPrayer(10,true);require(p.getPrayer().getPrayerpoints()==980,"Prayer points drain");
            p.getPrayer().setInfinitePrayer(true);p.getPrayer().drainPrayer(10,true);
            require(p.getPrayer().getPrayerpoints()==980,"Infinite prayer suppresses drain");
            p.getPrayer().setInfinitePrayer(false);p.getPrayer().restorePrayer(10);
            require(p.getPrayer().getPrayerpoints()==990,"Restoration uses existing points owner");
            p.getPrayer().setPrayerBook(true);click(p,13);
            require(p.getPrayer().usingPrayer(1,11),"Curses translate native key13 to Deflect Magic");
            p.getPrayer().closeAllPrayers();
            p.getPrayer().setPrayerBook(false);p.getSkills().setXp(Skills.PRAYER,0);p.getSkills().set(Skills.PRAYER,1);
            click(p,15);require(!p.getPrayer().hasPrayersOn(),"Level requirement retained");
            VarBitDefinitions bits=VarBitDefinitions.getClientVarpBitDefinitions(16747);
            require(bits.baseVar==3272&&bits.startBit==8&&bits.endBit==8,"950 activation bit layout");
            System.out.println("PASS: real950 input identity, toggle, exclusion, Piety, curse selection, unsupported safety, requirements and activation packet.");
        }finally{c.finishAndReleaseAll();}
    }
    private static void click(Player p,int slot){
        int hash=1458<<16|39;
        Native950Prayer.button(p,(Native950Actions.InterfaceAction)Native950Actions.decode(18,new byte[]{-1,-1,-1,
                (byte)(hash>>>16),(byte)(hash>>>24),(byte)hash,(byte)(hash>>>8),(byte)(slot>>>8),(byte)slot}));
        WorldTasksManager.processTasks();WorldTasksManager.processTasks();
    }
    private static boolean hasPacket(EmbeddedChannel c,Native950Packets.Packet expected){
        Object value;boolean found=false;
        while((value=c.readOutbound())!=null)if(value instanceof Native950Packets.Packet){
            Native950Packets.Packet actual=(Native950Packets.Packet)value;
            found|=actual.type()==expected.type()&&Arrays.equals(actual.payload(),expected.payload());
        }return found;
    }
    private static void require(boolean value,String label){if(!value)throw new AssertionError(label);}
    private static void verifyStateScript()throws Exception{
        com.google.gson.JsonObject mapping=new com.google.gson.JsonParser().parse(new String(java.nio.file.Files.readAllBytes(
                Paths.get("protocol-analysis/ui-scripts-950-evidence.json")),"UTF-8")).getAsJsonObject().getAsJsonObject("opcodeMap947to950");
        java.util.Map<Integer,Integer> inverse=new java.util.HashMap<>();
        for(java.util.Map.Entry<String,com.google.gson.JsonElement> e:mapping.entrySet())
            inverse.put(Integer.decode(e.getValue().getAsJsonObject().get("opcode950").getAsString()),Integer.decode(e.getKey()));
        byte[] raw=Cache.STORE.getIndexes()[12].getFile(7083,0);java.nio.ByteBuffer b=java.nio.ByteBuffer.wrap(raw);
        int end=raw.length-(b.getShort(raw.length-2)&65535)-18;
        while(b.get()!=0){}
        java.util.List<int[]> instructions=new java.util.ArrayList<>();
        while(b.position()<end){
            Integer op=inverse.get(b.getShort()&65535);if(op==null)throw new AssertionError("Unmapped7083 opcode");
            int operand;
            if(op==0x511){int type=b.get()&255;
                if(type==2){while(b.get()!=0){}operand=0;}else if(type==0)operand=b.getInt();else throw new AssertionError("Unexpected7083 push");
            }else if(op==0x35e||op==0x51a||op==0x895||op==0x713||op==0x30)operand=b.getInt();
            else if(op==0x495)operand=b.get()&255;else throw new AssertionError("Unexpected7083 opcode "+op);
            instructions.add(new int[]{op,operand});
        }
        b.position(end+16);require((b.get()&255)==1,"Prayer state switch count");int count=b.getShort()&65535;
        java.util.Map<Integer,Integer> stateBits=new java.util.HashMap<>();
        for(int i=0;i<count;i++){
            int structure=b.getInt(),offset=b.getInt();int[] instruction=instructions.get(2+offset);
            require(instruction[0]==0x30&&(instruction[1]&255)==0,"Prayer switch targets a varbit read");
            stateBits.put(structure,instruction[1]>>>8);
        }
        for(int book=0;book<2;book++)for(int slot=0;slot<(book==0?22:38);slot++){
            int structure=Native950PrayerContract.structure(book==1,slot);
            require(stateBits.containsKey(structure)&&stateBits.get(structure)==Native950PrayerContract.activeBit(book==1,slot),
                    "950 prayer bit identity for structure"+structure+": cache="+stateBits.get(structure)+", adapter="+Native950PrayerContract.activeBit(book==1,slot));
        }
        System.out.println("PASS: all60 activation bindings match950 script7083; SHA256="
                +com.rs.game.player.client.ui.Native950Bindings.sha256(raw));
    }
}
