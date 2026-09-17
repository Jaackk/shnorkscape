package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.BodyDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Controller;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/** Actual-cache encrypted Wear/Remove and original handlers; all characters and saves remain in RAM. */
public final class Native950CosmeticEquipmentAcceptance {
    private static int frames;
    public static void main(String[] args)throws Exception {
        require(args.length==1,"Usage: Native950CosmeticEquipmentAcceptance <950-flat-cache>");
        require(NativeCacheVerification.isEnforced(),"Cache verification must remain enabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World.getInstance().execute(()->{
            require(Native950CosmeticEquipment.verifyCache(),"Actual cosmetic definitions/models failed");
            try(Fixture f=new Fixture()){check(f);}
            require(World.getPlayers().isEmpty(),"Ephemeral equipment player leaked");
            return null;
        }).get(90,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"World scheduler failed");
        System.out.println("PASS: six partyhat Wear/swap/Remove handlers, encrypted stale and unported rejection, controller/full-inventory guards, both-gender appearance, neutral melee and RAM save restore; "+frames+" parsed950 frames");
        System.out.println("LIMIT: item rendering still needs the real client; no account file, login or listening socket was used.");
    }
    private static void check(Fixture f) {
        for(int id=1038;id<=1048;id+=2){
            Native950ItemCatalog.Entry entry=f.catalog.get(id);
            require(entry!=null&&entry.equipSlot==0&&entry.equipOption==2,"Missing partyhat equipment capability "+id);
            require(Native950InventoryMenu.usesOrdinaryOperations(id),"Partyhat uses an unreviewed special menu "+id);
            f.give(id);
        }
        for(int id:new int[]{1039}){
            require(f.catalog.get(id)!=null&&f.catalog.get(id).equipSlot==-1,"Unreviewed or noted item acquired wear capability "+id);f.give(id);
        }
        f.run(2);
        int[] bare=appearance(f.player,-1,false);
        for(int id=1038;id<=1048;id+=2){
            int clicked=f.slot(id);f.button(2,1473,5,clicked,id);f.run(3);
            require(f.hat()==id,"Actual deferred Wear did not equip "+id);
            require(f.lastEquipment!=null&&f.lastEquipment[0]==id,"Equipment container94 did not publish "+id);
            int[] male=appearance(f.player,id,false),female=appearance(f.player,id,true);
            require(male[8]==bare[8]&&male[11]==bare[11]&&female[8]==bare[8],"Cosmetic hat unexpectedly hid a body kit");
            appearance(f.player,id,false);
            Native950MeleeCombat.Loadout loadout=Native950MeleeCombat.loadout(f.player);
            require(loadout.attackBonus==0&&loadout.strengthBonus==0&&loadout.defenceBonus==0,"Cosmetic hat changed melee bonuses");
            for(int other=1038;other<=1048;other+=2)require(f.total(other)==1,"Wear swap changed total quantity "+other);
            long rejected=f.input.snapshot().rejectedActions;
            f.button(2,1473,5,clicked,id);f.run(2);
            require(f.hat()==id&&f.input.snapshot().rejectedActions>rejected,"Stale Wear was accepted");
        }
        Native950Save saved=f.input.saveSnapshot();
        require(saved.equipmentIds()[0]==1048,"Save snapshot omitted worn partyhat");
        f.player.getControlerManager().startControler(new Controller(){
            public void start(){}
            @Override public boolean canEquip(int slot,int id){return false;}
        });
        f.button(2,1473,5,f.slot(1044),1044);f.run(3);
        require(f.hat()==1048&&f.total(1044)==1,"Original controller canEquip veto was ignored");
        f.player.getControlerManager().removeControlerWithoutCheck();
        for(int id:new int[]{1039}){
            long rejected=f.input.snapshot().rejectedActions;f.button(2,1473,5,f.slot(id),id);f.run(2);
            require(f.hat()==1048&&f.total(id)==1&&f.input.snapshot().rejectedActions>rejected,"Unported Wear changed equipment "+id);
        }
        // The neutral exception must not let arbitrary carried items bypass combat validation.
        for(int id:new int[]{1039,314}){
            f.player.getEquipment().getItems().set(0,new Item(id,1));boolean rejected=false;
            try{Native950MeleeCombat.loadout(f.player);}catch(IllegalArgumentException expected){rejected=true;}
            finally{f.player.getEquipment().getItems().set(0,new Item(1048,1));}
            require(rejected,"Non-cosmetic invalid equipment bypassed loadout checks "+id);
        }
        for(int slot=0;slot<28;slot++)if(f.player.getInventory().items.get(slot)==null)f.player.getInventory().items.set(slot,new Item(1511,1));
        f.run(2);int greenSlot=f.slot(1044);f.button(2,1473,5,greenSlot,1044);f.run(3);
        require(f.hat()==1044&&f.player.getInventory().items.get(greenSlot).getId()==1048,"Full backpack did not swap displaced hat into source slot");
        f.button(1,1462,31,0,1044);f.run(2);
        require(f.hat()==1044&&f.total(1044)==1,"Full-backpack Remove lost equipment");
        f.player.getInventory().items.set(f.slot(1511),null);f.run(2);
        f.button(1,1462,31,0,1044);f.run(2);
        require(f.hat()==-1&&f.total(1044)==1,"Ordinary Remove did not return partyhat exactly once");
        require(f.lastEquipment[0]==-1,"Remove did not clear native equipment container");appearance(f.player,-1,false);
        restore(saved,f.catalog);
    }
    private static void restore(Native950Save saved,Native950ItemCatalog catalog){
        EmbeddedChannel channel=new EmbeddedChannel();
        try{
            Player restored=Player.createNative950("hatprobe",new WorldTile(3217,3258,0),channel);
            Native950Containers containers=new Native950Containers(restored,catalog);containers.restore(saved);
            require(Arrays.equals(saved.equipmentIds(),containers.equipmentSnapshot().ids),"RAM save restore changed worn slots");
            require(Arrays.equals(saved.inventoryIds(),containers.inventorySnapshot().ids),"RAM save restore changed backpack");
            appearance(restored,1048,false);
            Native950MeleeCombat.loadout(restored);
        }finally{channel.finishAndReleaseAll();}
    }
    private static int[] appearance(Player player,int id,boolean female){
        player.getAppearence().setMale(!female);long withheld=player.getAppearence().getNative950WithheldBodies();
        player.getAppearence().generateAppearenceData();byte[] body=player.getAppearence().getAppeareanceData();
        require(body!=null&&player.getAppearence().getNative950WithheldBodies()==withheld,"Live native appearance withheld cosmetic item");
        ByteBuf bytes=Unpooled.wrappedBuffer(body);try{
            require(bytes.readUnsignedByte()==(female?1:0)&&bytes.readUnsignedByte()==0,"Unexpected appearance flags");
            int[] looks=new int[19];
            for(int slot=0;slot<19;slot++)if(BodyDefinitions.disabledSlots[slot]!=1){
                int value=0,shift=0,next;do{next=bytes.readUnsignedByte();value|=(next&127)<<shift;shift+=7;require(shift<=21,"Appearance varint overflow");}while((next&128)!=0);looks[slot]=value;
            }
            require(looks[0]==(id<0?0:0x800+id),"Wrong partyhat item in live appearance body");
            require(bytes.readUnsignedShort()==0,"Unexpected model customization block");
            return looks;
        }finally{bytes.release();}
    }
    private static final class Fixture implements AutoCloseable{
        final int[] incoming={9,5,0,3},outgoing={59,55,50,53};
        final Native950Isaac clientCipher=new Native950Isaac(incoming),cipher=new Native950Isaac(outgoing);
        final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(incoming),new Native950Isaac(outgoing),Thread.currentThread());
        final EmbeddedChannel channel=new EmbeddedChannel(transport);
        final Player player;final Native950Interactions input;final Native950ItemCatalog catalog;
        int[] lastEquipment;
        Fixture(){
            player=Player.createNative950("hatprobe",new WorldTile(3217,3258,0),channel);player.setActive(true);player.setRunning(true);
            Native950World.installVarpSink(player);World.addNative950Player(player,1);World.updateEntityRegion(player);player.loadMapRegions();player.setClientHasLoadedMapRegion();
            catalog=new Native950ItemCatalog(Collections.<Native950ItemCatalog.Entry>emptyList()).withLegacyDrops();
            Native950Content.BankUi bank=new Native950Content.BankUi(517,201,15,317,39,new int[11],new int[11],Collections.emptyList(),Collections.emptyList());
            Native950Content.EquipmentUi equipment=new Native950Content.EquipmentUi(1462,31,94,Collections.emptyList());
            input=new Native950Interactions(player,channel,new Native950Content(catalog,bank,null,equipment,new Native950Appearance(new int[]{0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,1,0})),null,null);
            for(int slot=0;slot<28;slot++)player.getInventory().items.set(slot,null);input.bootstrap();drain();
        }
        int hat(){Item item=player.getEquipment().getItems().get(0);return item==null?-1:item.getId();}
        int total(int id){return player.getInventory().items.getNumberOf(id)+(hat()==id?1:0);}
        int slot(int id){int slot=player.getInventory().items.lookupSlot(id);require(slot>=0,"Missing fixture item "+id);return slot;}
        void give(int id){require(Native950Skilling.giveItem(player,id,1),"Could not add fixture item "+id);}
        void run(int count){for(int step=0;step<count;step++){Native950World.pumpSchedulers();input.beginTick();player.processEntity();player.processEntityUpdate();input.afterMovement();channel.write(Native950Packets.tickEnd());drain();player.resetMasks();}}
        void button(int option,int face,int component,int slot,int id){
            int hash=(face<<16)|component;ByteBuf b=Unpooled.buffer(10);b.writeByte(((option==2?122:18)+clientCipher.getAsInt())&255);b.writeMedium(id);
            b.writeByte(hash>>>16);b.writeByte(hash>>>24);b.writeByte(hash);b.writeByte(hash>>>8);b.writeShort(slot);
            channel.writeInbound(b);channel.runPendingTasks();require(transport.drainActions(input::handle)==1,"Encrypted equipment click missed router");
        }
        void drain(){channel.flush();channel.runPendingTasks();Object message;
            while((message=channel.readOutbound())!=null)try{
                require(message instanceof ByteBuf,"Transport emitted nonbytes");ByteBuf b=(ByteBuf)message;
                while(b.isReadable()){
                    int opcode=(b.readUnsignedByte()-cipher.getAsInt())&255;
                    if(opcode>=128)opcode=((opcode-128)<<8)|((b.readUnsignedByte()-cipher.getAsInt())&255);
                    ServerPacket kind=null;for(ServerPacket row:ServerPacket.values())if(row.opcode()==opcode){kind=row;break;}
                    require(kind!=null,"Unknown950 output opcode "+opcode);int length=kind.size();if(length==-1)length=b.readUnsignedByte();else if(length==-2)length=b.readUnsignedShort();
                    require(length>=0&&length<=b.readableBytes(),"Malformed950 equipment frame "+kind);
                    if(kind==ServerPacket.UPDATE_INV_FULL&&length>=5){
                        ByteBuf row=b.slice(b.readerIndex(),length);
                        int container=row.readUnsignedShort();row.readUnsignedByte();int size=row.readUnsignedShort();
                        if(container==94){require(size==19,"Wrong equipment container size");lastEquipment=new int[19];for(int slot=0;slot<19;slot++){
                            lastEquipment[slot]=row.readUnsignedMedium()-1;int amount=row.readUnsignedByte();if(amount==255)amount=row.readInt();
                            require(amount==(lastEquipment[slot]<0?0:1),"Wrong equipment quantity");}require(!row.isReadable(),"Equipment container tail");}
                    }
                    b.skipBytes(length);frames++;
                }
            }finally{ReferenceCountUtil.release(message);}
            channel.checkException();require(channel.isActive()&&transport.terminalFailure()==null,"Equipment transport disconnected: "+transport.terminalFailure());
        }
        public void close(){input.close();World.removeNative950Player(player);channel.finishAndReleaseAll();}
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}