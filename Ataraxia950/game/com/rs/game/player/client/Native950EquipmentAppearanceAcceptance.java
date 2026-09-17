package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.BodyDefinitions;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/** Current-cache model/BAS and live serializer acceptance; no account writes or listening sockets. */
public final class Native950EquipmentAppearanceAcceptance {
    private static int bodies;
    public static void main(String[] args) throws Exception {
        if(args.length!=1)throw new IllegalArgumentException("Supply the950 flat-cache directory");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World.getInstance().execute(()->{check();return null;}).get(90,TimeUnit.SECONDS);
        System.out.println("PASS: current-cache staff/full-helmet/body/legs/offhand/ammo/ring/pocket/single-gender/wings appearance, hidden slots, normal/combat BAS and rejected conflicting slots; "+bodies+" parsed950 bodies");
        System.out.println("LIMIT: visual model/animation quality needs the real client; no account state was used.");
    }
    private static void check() {
        EmbeddedChannel channel=new EmbeddedChannel();
        try {
            Player player=Player.createNative950("looksprobe",new WorldTile(3217,3258,0),channel);
            for(boolean female:new boolean[]{false,true}) {
                if(female)player.getAppearence().female();else player.getAppearence().male();
                clear(player);Body bare=body(player,female);
                require(bare.slots[8]==2+(female?48:3),"Wrong gender kit baseline");
                for(int id:new int[]{58486,1155,1117,1075,1173,882,1635,3839,20980,41752,41751,30976}) {
                    Native950EquipmentTypes.Type type=Native950EquipmentTypes.resolve(id);
                    require(type!=null,"Missing current-cache equipment type "+id);
                    clear(player);player.getEquipment().getItems().set(type.slot,new Item(id,type.stackable?100:1));
                    Body normal=body(player,female);
                    if(BodyDefinitions.disabledSlots[type.slot]!=1)require(normal.slots[type.slot]==0x800+id,"Wrong worn identity "+id);
                    for(int hidden:new int[]{type.hide1,type.hide2})if(hidden>=0&&hidden!=type.slot&&BodyDefinitions.disabledSlots[hidden]!=1)
                        require(normal.slots[hidden]==0,"Worn item failed to hide body slot "+id+":"+hidden);
                    int expected=type.slot==3||type.slot==5?type.bas:2699;
                    require(normal.bas==expected,"Wrong normal movement set "+id+":"+normal.bas+" expected"+expected);
                    player.getCombatDefinitions().setCombatStance(true);
                    Body combat=body(player,female);
                    require(combat.bas==(type.slot==3||type.slot==5?type.combatBas:2688),"Wrong combat movement set "+id);
                    player.getCombatDefinitions().setCombatStance(false);
                }
                clear(player);
                for(int id:new int[]{58486,1155,1117,1075}) {
                    Native950EquipmentTypes.Type type=Native950EquipmentTypes.resolve(id);
                    player.getEquipment().getItems().set(type.slot,new Item(id,1));
                }
                Body whole=body(player,female);
                require(whole.slots[3]==0x800+58486&&whole.slots[5]==0&&whole.slots[6]==0,"Full outfit conflicts/body hiding");
                require(whole.bas==2697,"Staff should use its actual combat profile normal BAS2697");
                byte[] previous=player.getAppearence().getAppeareanceData().clone();
                long withheld=player.getAppearence().getNative950WithheldBodies();
                player.getEquipment().getItems().set(5,new Item(1173,1));
                player.getAppearence().generateAppearenceData();
                require(player.getAppearence().getNative950WithheldBodies()>withheld,"Conflicting staff+shield was sent");
                require(Arrays.equals(previous,player.getAppearence().getAppeareanceData()),"Rejected gear replaced last valid body");
                clear(player);body(player,female);
            }
        } finally {channel.finishAndReleaseAll();}
    }
    private static void clear(Player player){for(int slot=0;slot<19;slot++)player.getEquipment().getItems().set(slot,null);player.getCombatDefinitions().setCombatStance(false);}
    private static Body body(Player player,boolean female) {
        long withheld=player.getAppearence().getNative950WithheldBodies();player.getAppearence().generateAppearenceData();
        byte[] raw=player.getAppearence().getAppeareanceData();
        require(raw!=null&&raw.length<=255&&player.getAppearence().getNative950WithheldBodies()==withheld,"Live appearance refused valid outfit: "+player.getAppearence().getNative950LastWithheldReason());
        ByteBuf bytes=Unpooled.wrappedBuffer(raw);Body body=new Body();try {
            require(bytes.readUnsignedByte()==(female?1:0)&&bytes.readUnsignedByte()==0,"Wrong gender/render flags");
            for(int slot=0;slot<19;slot++)if(BodyDefinitions.disabledSlots[slot]!=1) {
                int shift=0,next,value=0;do{next=bytes.readUnsignedByte();value|=(next&127)<<shift;shift+=7;require(shift<=28,"Bad slotvarint");}while((next&128)!=0);body.slots[slot]=value;
            }
            require(bytes.readUnsignedShort()==0,"Unexpected customization data");bytes.skipBytes(20);body.bas=bytes.readUnsignedShort();
            while(bytes.readUnsignedByte()!=0){}
            bytes.skipBytes(3);require(bytes.readUnsignedByte()==0&&!bytes.isReadable(),"Misaligned appearance tail");
            bodies++;return body;
        }finally{bytes.release();}
    }
    private static final class Body{final int[] slots=new int[19];int bas;}
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
