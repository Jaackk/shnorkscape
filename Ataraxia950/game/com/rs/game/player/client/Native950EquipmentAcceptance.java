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

/** Actual-cache encrypted generic equipment acceptance; all characters and saves remain in RAM. */
public final class Native950EquipmentAcceptance {
    private static int frames;
    public static void main(String[] args)throws Exception {
        require(args.length==1,"Usage: Native950EquipmentAcceptance <950-flat-cache>");
        require(NativeCacheVerification.isEnforced(),"Cache verification must remain enabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        System.setProperty(Native950DevelopmentCommands.PROPERTY,"true");
        System.setProperty(Native950AdminCommands.ACCOUNTS,"hatprobe");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World.getInstance().execute(()->{
            BodyDefinitions.init();
            try(Fixture f=new Fixture()){check(f);}
            try(Fixture f=new Fixture()){developmentAmmo(f);}
            require(World.getPlayers().isEmpty(),"Ephemeral equipment player leaked");
            return null;
        }).get(90,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"World scheduler failed");
        System.out.println("PASS: cache equipment requirements, staff/two-handed and shield swaps, full bags, controller vetoes, stale clicks, ammunition quantities, both genders and RAM save restore; "+frames+" parsed950 frames");
        System.out.println("LIMIT: item rendering still needs the real client; no account file, login or listening socket was used.");
    }
    private static void check(Fixture f) {
        final int staff=58486, arrows=882;
        Native950EquipmentTypes.Type type=Native950EquipmentTypes.resolve(staff);
        require(type!=null&&type.slot==3&&type.twoHanded&&type.requirements.get(com.rs.game.player.Skills.MAGIC)==99,"Staff cache contract");
        for(int id:new int[]{staff,1277,1173,1053,45543,45541,1635,1007,arrows}){
            require(f.catalog.get(id)!=null&&f.catalog.get(id).equipSlot>=0,"Generic equipment missing "+id);f.give(id);
        }
        f.player.getInventory().items.get(f.slot(arrows)).setAmount(100);f.run(2);
        // Boosted current levels cannot satisfy base-XP equipment requirements.
        f.player.getSkills().setXpWithoutRefresh(com.rs.game.player.Skills.MAGIC,0);
        f.player.getSkills().set(com.rs.game.player.Skills.MAGIC,99);
        long rejects=f.input.snapshot().rejectedActions;
        f.button(2,1473,5,f.slot(staff),staff);f.run(2);
        require(f.worn(3)==-1&&f.total(staff)==1&&f.input.snapshot().rejectedActions>rejects,"Staff skipped Magic99 base requirement");
        for(int skill=0;skill<com.rs.game.player.Skills.SKILL_COUNT;skill++)f.player.getSkills().setXpWithoutRefresh(skill,200000000);
        f.player.getControlerManager().startControler(new Controller(){public void start(){}@Override public boolean canEquip(int slot,int id){f.player.getSkills().setXpWithoutRefresh(com.rs.game.player.Skills.MAGIC,0);return true;}});
        f.button(2,1473,5,f.slot(staff),staff);f.run(2);
        require(f.worn(3)==-1&&f.total(staff)==1,"Controller XP change bypassed requirement");
        f.player.getControlerManager().removeControlerWithoutCheck();f.player.getSkills().setXpWithoutRefresh(com.rs.game.player.Skills.MAGIC,200000000);
        f.player.getControlerManager().startControler(new Controller(){public void start(){}@Override public boolean canEquip(int slot,int id){f.player.lock();return true;}});
        f.button(2,1473,5,f.slot(staff),staff);f.run(2);
        require(f.worn(3)==-1&&f.total(staff)==1,"Controller activity change bypassed equipment lock");
        f.player.getControlerManager().removeControlerWithoutCheck();f.player.unlock();
        for(int id:new int[]{1277,1173}){f.button(2,1473,5,f.slot(id),id);f.run(2);}
        require(f.worn(3)==1277&&f.worn(5)==1173,"Initial weapon and shield equip");
        for(int slot=0;slot<28;slot++)if(f.player.getInventory().items.get(slot)==null)f.player.getInventory().items.set(slot,new Item(1511,1));
        f.run(2);int staffSlot=f.slot(staff);f.button(2,1473,5,staffSlot,staff);f.run(2);
        require(f.worn(3)==1277&&f.worn(5)==1173&&f.total(staff)==1,"Full two-handed swap was not atomic");
        f.player.getInventory().items.set(f.slot(1511),null);f.run(2);
        f.player.getControlerManager().startControler(new Controller(){public void start(){}@Override public boolean canRemoveEquip(int slot,int id){return slot!=5;}});
        f.button(2,1473,5,staffSlot,staff);f.run(2);
        require(f.worn(3)==1277&&f.worn(5)==1173,"Conflict removal ignored controller");
        f.player.getControlerManager().removeControlerWithoutCheck();
        f.button(2,1473,5,staffSlot,staff);f.run(2);
        require(f.worn(3)==staff&&f.worn(5)==-1&&f.total(1277)==1&&f.total(1173)==1,"Staff did not displace both pieces");
        require(f.lastEquipment[3]==staff,"Wire equipment missing staff");appearance(f.player,3,staff,false);appearance(f.player,3,staff,true);appearance(f.player,3,staff,false);
        rejects=f.input.snapshot().rejectedActions;f.button(2,1473,5,staffSlot,staff);f.run(2);
        require(f.worn(3)==staff&&f.total(staff)==1&&f.input.snapshot().rejectedActions>rejects,"Stale staff click repeated transaction");
        f.button(2,1473,5,f.slot(1173),1173);f.run(2);
        require(f.worn(3)==-1&&f.worn(5)==1173&&f.total(staff)==1,"Offhand did not displace two-handed staff");
        for(int id:new int[]{1053,45543,45541,1635,1007,arrows}){
            f.button(2,1473,5,f.slot(id),id);f.run(2);int wearSlot=f.catalog.get(id).equipSlot;
            require(f.worn(wearSlot)==id,"Generic current-cache item did not equip "+id);appearance(f.player,wearSlot,id,false);appearance(f.player,wearSlot,id,true);
        }
        f.player.getAppearence().setMale(true);
        require(f.player.getEquipment().getItems().get(13).getAmount()==100,"Ammo equip lost quantity");
        require(Native950Skilling.giveItem(f.player,arrows,50),"Ammo refill grant");f.run(2);
        f.button(2,1473,5,f.slot(arrows),arrows);f.run(2);
        require(f.player.getEquipment().getItems().get(13).getAmount()==150,"Ammo merge lost quantity");
        movementSurvivesEquipment(f);
        Native950Save saved=f.input.saveSnapshot();restore(saved,f.catalog);
        f.button(1,1462,31,13,arrows);f.run(2);
        require(f.worn(13)==-1&&f.player.getInventory().items.getNumberOf(arrows)==150,"Ammo Remove lost quantity");
        require(f.total(staff)==1&&f.total(1173)==1,"Inventory/equipment totals changed");
    }
    private static void movementSurvivesEquipment(Fixture f) {
        for (boolean running : new boolean[]{false,true}) {
            f.player.setRun(running);
            f.player.resetWalkSteps();
            require(f.player.addWalkSteps(f.player.getX()+8,f.player.getY(),8,false),"Could not queue movement fixture");
            com.rs.game.player.content.RouteEvent route = new com.rs.game.player.content.RouteEvent(
                    new WorldTile(f.player.getX()+8,f.player.getY(),0),()->{});
            f.player.setRouteEvent(route);
            Object[] path=f.player.getWalkSteps().toArray();
            f.button(2,1473,5,f.slot(1277),1277);
            require(f.worn(3)==1277,"Moving equipment exchange failed");
            require(Arrays.equals(path,f.player.getWalkSteps().toArray())&&f.player.getRouteEvent()==route
                    &&f.player.getRun()==running,"Equipping cleared the route or changed run mode");
            f.button(1,1462,31,3,1277);
            require(f.worn(3)==-1,"Moving equipment removal failed");
            require(Arrays.equals(path,f.player.getWalkSteps().toArray())&&f.player.getRouteEvent()==route
                    &&f.player.getRun()==running,"Removing equipment cleared the route or changed run mode");
            f.player.setRouteEvent(null);
            int x=f.player.getX();f.run(1);
            require(f.player.getX()==x+(running?2:1)&&f.player.hasWalkSteps(),"Movement did not continue after gear changes");
            f.player.resetWalkSteps();f.run(1);
        }
    }
    private static void developmentAmmo(Fixture f) {
        for(int skill=0;skill<com.rs.game.player.Skills.SKILL_COUNT;skill++)f.player.getSkills().setXpWithoutRefresh(skill,200000000);
        Native950AdminCommands.handle(f.player,f.channel,new String[]{"almighty"});f.run(1);
        require(f.player.isInfiniteAmmunition(),"Almighty did not enable ammunition");
        for(int weapon:new int[]{16337,53351,63325,8880,806,1277}) {
            f.give(weapon);f.button(2,1473,5,f.slot(weapon),weapon);f.run(2);
            require(f.worn(3)==weapon,"Development weapon exchange failed "+weapon);
            int expected=weapon==16337||weapon==53351?882:weapon==63325?877:weapon==8880?8882:-1;
            require(f.lastEquipment[13]==expected,"Wrong native supplied ammo for "+weapon);
            require(f.worn(13)==-1&&f.input.saveSnapshot().equipmentIds()[13]==-1,"Virtual ammunition entered equipment/save");
            if(expected>=0){
                f.button(1,1462,31,13,expected);f.run(1);
                require(f.total(expected)==0,"Virtual ammo removal created an item");
                Native950AdminCommands.handle(f.player,f.channel,new String[]{"infammo"});f.run(1);
                require(f.lastEquipment[13]==-1,"Disabling supply left stale client ammo");
                Native950AdminCommands.handle(f.player,f.channel,new String[]{"infammo"});f.run(1);
                require(f.lastEquipment[13]==expected,"Re-enabling supply did not refresh client ammo");
            }
        }
        f.give(882);f.button(2,1473,5,f.slot(882),882);f.run(1);
        f.button(2,1473,5,f.slot(63325),63325);f.run(1);
        require(f.lastEquipment[13]==877&&f.worn(13)==882,"Supply failed to preserve incompatible owned ammunition");
        Native950AdminCommands.handle(f.player,f.channel,new String[]{"almighty"});f.run(1);
        require(!f.player.isInfiniteAmmunition()&&f.lastEquipment[13]==882&&f.total(882)==1,"Disabling almighty did not restore owned ammunition");
        System.out.println("PASS: almighty ammo follows bows/crossbows, thrown/melee unchanged, toggle refresh, virtual removal refusal, owned ammo/save preservation");
    }
    private static void restore(Native950Save saved,Native950ItemCatalog catalog){
        EmbeddedChannel channel=new EmbeddedChannel();try{
            Player restored=Player.createNative950("hatprobe",new WorldTile(3217,3258,0),channel);
            Native950Containers containers=new Native950Containers(restored,catalog);containers.restore(saved);
            require(Arrays.equals(saved.equipmentIds(),containers.equipmentSnapshot().ids),"Save changed worn IDs");
            require(Arrays.equals(saved.equipmentAmounts(),containers.equipmentSnapshot().amounts),"Save changed worn quantities");
            require(Arrays.equals(saved.inventoryIds(),containers.inventorySnapshot().ids),"Save changed backpack");
            appearance(restored,13,882,false);
        }finally{channel.finishAndReleaseAll();}
    }
    private static void appearance(Player player,int expectedSlot,int id,boolean female){
        player.getAppearence().setMale(!female);long withheld=player.getAppearence().getNative950WithheldBodies();
        player.getAppearence().generateAppearenceData();byte[] body=player.getAppearence().getAppeareanceData();
        require(body!=null&&player.getAppearence().getNative950WithheldBodies()==withheld,"Live native appearance withheld equipment");
        ByteBuf bytes=Unpooled.wrappedBuffer(body);try{
            require(bytes.readUnsignedByte()==(female?1:0)&&bytes.readUnsignedByte()==0,"Appearance flags");
            int[] looks=new int[19];
            for(int slot=0;slot<19;slot++)if(BodyDefinitions.disabledSlots[slot]!=1){
                int value=0,shift=0,next;do{next=bytes.readUnsignedByte();value|=(next&127)<<shift;shift+=7;require(shift<=21,"Appearance varint overflow");}while((next&128)!=0);looks[slot]=value;
            }
            if(BodyDefinitions.disabledSlots[expectedSlot]!=1)require(looks[expectedSlot]==0x800+id,"Wrong item appearance slot "+expectedSlot);
            require(bytes.readUnsignedShort()==0,"Unexpected model customization block");
        }finally{bytes.release();}
    }
    private static final class Fixture implements AutoCloseable{
        final int[] incoming={9,5,0,3},outgoing={59,55,50,53};
        final Native950Isaac clientCipher=new Native950Isaac(incoming),cipher=new Native950Isaac(outgoing);
        final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(incoming),new Native950Isaac(outgoing),Thread.currentThread());
        final EmbeddedChannel channel=new EmbeddedChannel(transport){
            @Override protected java.net.SocketAddress remoteAddress0(){return new java.net.InetSocketAddress("127.0.0.1",43650);}
        };
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
        int worn(int slot){Item item=player.getEquipment().getItems().get(slot);return item==null?-1:item.getId();}
        int total(int id){int total=player.getInventory().items.getNumberOf(id);for(Item item:player.getEquipment().getItems().getItems())if(item!=null&&item.getId()==id)total+=item.getAmount();return total;}
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
                            require(lastEquipment[slot]<0?amount==0:amount>0,"Wrong equipment quantity");}require(!row.isReadable(),"Equipment container tail");}
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
