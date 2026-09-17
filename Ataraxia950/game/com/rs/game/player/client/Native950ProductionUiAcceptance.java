package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Actual paired-cache, encrypted input, ordinary ActionManager and native XP/save acceptance. */
public final class Native950ProductionUiAcceptance {
 private static int frames,ticks;
 public static void main(String[] args)throws Exception{
  require(args.length==1,"Usage: Native950ProductionUiAcceptance <950-flat-cache>");require(NativeCacheVerification.isEnforced(),"Verification disabled");
  System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");Cache.initFlatReadOnly(Paths.get(args[0]));
  Native950World.getInstance().execute(()->{Native950Crafting.verifyCacheBindings();try(Fixture f=new Fixture()){check(f);}return null;}).get(120,TimeUnit.SECONDS);
  require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler failed");System.out.println("PASS: native Make-X routes, "+ticks+" ticks, "+frames+" decoded encrypted950 frames; no account write, listener or authentication.");
 }
 private static void check(Fixture f){
  Native950ProductionInterface.verify();
  f.clear();f.level(Skills.FLETCHING,99);f.give(946,1);f.give(1511,5);f.run(2);f.use(946,1511);
  f.drain();require(f.makeEventMask==1,"Make-X overwrote the native pause-button mask");
  Native950ProductionMenu menu=f.menu();require(menu.isNativeOpen(),"Logs/knife did not open native Make-X");
  require(menu.currentCategoryId()==6947,"Wrong normal wood category");select(f,50);
  int before=menu.selectedDisplayId();f.nativeButton(1371,22,1,1607);require(menu.selectedDisplayId()==before,"Forged item changed selected recipe");
  f.nativeButton(1371,20,1,-1);require(menu.selectedQuantity()==2,"Slider child did not choose two batches");
  f.nativeButton(1370,30,-1,-1);require(menu.isNativeOpen(),"Ordinary IF_BUTTON incorrectly submitted the pause target");
  f.response(1188,8);require(menu.isNativeOpen(),"Old dialogue reply submitted native Make-X");
  f.response(1370,30);require(!menu.isOpen(),"Make did not close modal ownership");f.run(15);
  require(f.amount(50)==2&&f.amount(1511)==3&&f.amount(946)==1,"NativeMake did not preserve real Fletching callback/materials/tool");
  f.response(1370,30);f.run(10);require(f.amount(50)==2,"Stale Make duplicated production");
  f.clear();f.level(Skills.FLETCHING,99);f.give(946,1);f.give(1521,2);f.run(2);f.use(946,1521);
  require(menu.isNativeOpen()&&menu.currentCategoryId()==6949,"Oak opened another wood category");select(f,52);
  require(menu.selectedDisplayId()==34672,"Oak shafts lost alias/material context");f.nativeButton(1371,20,0,-1);f.response(1370,30);f.run(10);
  require(f.amount(52)==30&&f.amount(1521)==1,"Oak shaft proxy created the wrong output or yield");
  f.clear();f.level(Skills.CRAFTING,99);f.give(1755,1);f.give(1623,2);f.run(2);f.use(1755,1623);
  require(menu.isNativeOpen()&&menu.currentCategoryId()==6983,"Uncut gem did not open native gem grid");select(f,1607);
  f.nativeButton(1370,32,-1,-1);require(!menu.isOpen(),"NativeClose retained owner");f.response(1370,30);f.run(10);
  require(f.amount(1623)==2&&f.amount(1607)==0,"Close did not cancel pending gem recipe");
  f.use(1755,1623);select(f,1607);f.input.walking();f.response(1370,30);f.run(10);
  require(!menu.isOpen()&&f.amount(1623)==2,"Movement did not cancel Make-X");
  f.use(1755,1623);select(f,1607);int gemSlot=f.slot(1623);f.player.getInventory().items.set(gemSlot,null);
  for(int i=0;i<28;i++){Item item=f.player.getInventory().items.get(i);if(item!=null&&item.getId()==1623)f.player.getInventory().items.set(i,null);}
  f.response(1370,30);f.run(5);require(f.amount(1607)==0&&menu.isNativeOpen(),"Changed materials started invalid crafting");
  f.clear();f.level(Skills.FLETCHING,99);f.give(39,1500);f.give(53,1500);f.run(2);f.use(39,53);
  require(menu.isNativeOpen(),"Arrow tipping did not open native Make-X");select(f,882);require(menu.maximumQuantity()==60,"Native batching not bounded at60");
  f.nativeButton(1371,20,60,-1);require(menu.selectedQuantity()==60,"Out-of-range slider modified quantity");
  f.nativeButton(1371,20,0,-1);f.response(1370,30);f.run(10);require(f.amount(882)==15,"Native arrow recipe lost batch multiplier");
  f.clear();f.level(Skills.FLETCHING,99);f.give(946,1);f.give(1511,2);f.give(1521,2);f.run(2);
  Native950Production.Recipe normal=null,oak=null;for(Native950Production.Recipe recipe:Native950Production.recipes())if(recipe.produced().length>0){if(recipe.produced()[0].getId()==50)normal=recipe;if(recipe.produced()[0].getId()==54)oak=recipe;}
  require(normal!=null&&oak!=null,"Missing wood recipes");menu.open("Wood types",Arrays.asList(normal,oak));require(menu.isNativeOpen(),"Common native category root was not selected");
  int oakIndex=-1;com.rs.cache.loaders.rs3.RS3ClientScriptMap root=com.rs.cache.loaders.rs3.RS3ClientScriptMap.getMap(6939);for(int i=0;i<root.getSize();i++)if(root.getIntValue(i)==6949)oakIndex=i;
  require(oakIndex>=0,"Oak category ordinal missing");int firstCategory=menu.currentCategoryId();f.nativeButton(1477,896,oakIndex,-1);require(menu.currentCategoryId()==firstCategory,"Unarmed shared dropdown changed category");
  f.nativeButton(1371,28,-1,-1);f.nativeButton(1477,896,oakIndex,-1);require(menu.currentCategoryId()==6949,"Armed native dropdown did not select oak");
  select(f,54);f.nativeButton(1371,20,0,-1);f.response(1370,30);f.run(10);require(f.amount(54)==1&&f.amount(1521)==1&&f.amount(1511)==2,"Dropdown started wrong recipe/materials");
  f.clear();final int[] called={0};menu.openChoices("Non-production action",Collections.singletonList(new Native950ProductionMenu.Choice("Inspect",()->called[0]++)));
  require(menu.isOpen()&&!menu.isNativeOpen(),"Non-production action no longer has dialogue fallback");f.response(1188,8);require(called[0]==1&&!menu.isOpen(),"Non-production callback failed");
  System.out.println("PASS: real Crafting/Fletching Make-X, alias context, quantities, batch output, close/walk cancellation, stale/forged packets, changed materials and semantic dialogue fallback");
 }
 private static void select(Fixture f,int output){
  Native950ProductionMenu menu=f.menu();Native950ProductionUiCatalog.Category category=Native950ProductionUiCatalog.byId(menu.currentCategoryId());
  require(category!=null,"No native category");for(int i=0;i<category.displayIds.length;i++)if(Native950ProductionUiCatalog.outputId(category.displayIds[i])==output){f.nativeButton(1371,22,1+4*i,category.displayIds[i]);require(Native950ProductionUiCatalog.outputId(menu.selectedDisplayId())==output,"Selection rejected output "+output);return;}
  throw new AssertionError("Category lacks output "+output);
 }
    static final class Fixture implements AutoCloseable {
        final int[] incoming={9,5,0,2},outgoing={59,55,50,52};
        final Native950Isaac clientCipher=new Native950Isaac(incoming),cipher=new Native950Isaac(outgoing);
        final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(incoming),new Native950Isaac(outgoing),Thread.currentThread());
        final EmbeddedChannel channel=new EmbeddedChannel(transport);
        final List<ServerPacket> output=new ArrayList<>();
        final Player player;final Native950Interactions input;
        Fixture(){
            player=Player.createNative950("craft-route",new WorldTile(3217,3258,0),channel);
            player.setActive(true);player.setRunning(true);Native950World.installVarpSink(player);
            World.addNative950Player(player,1);World.updateEntityRegion(player);player.loadMapRegions();player.setClientHasLoadedMapRegion();
            Native950ItemCatalog catalog=new Native950ItemCatalog(Collections.<Native950ItemCatalog.Entry>emptyList()).withLegacyDrops();
            Native950Content.BankUi bank=new Native950Content.BankUi(517,201,15,317,39,new int[11],new int[11],Collections.emptyList(),Collections.emptyList());
            input=new Native950Interactions(player,channel,new Native950Content(catalog,bank),null,null);
            for(int slot=0;slot<28;slot++)player.getInventory().items.set(slot,null);
            input.bootstrap();drain();output.clear();
        }
        final List<com.rs.game.WorldObject> stations=new ArrayList<>();
        void clear(){input.walking();player.resetWalkSteps();player.resetMasks();for(int i=0;i<28;i++)player.getInventory().items.set(i,null);run(2);}
        com.rs.game.WorldObject station(int id){
            for(int y=3255;y<3290;y++)for(int x=3205;x<3250;x++){
                com.rs.game.WorldObject o=new com.rs.game.WorldObject(id,10,0,x,y,0);boolean clear=true;
                for(int dx=-1;dx<=o.getDefinitions().sizeX;dx++)for(int dy=-1;dy<=o.getDefinitions().sizeY;dy++)if(!World.isFloorFree(0,x+dx,y+dy,1))clear=false;
                if(!clear)continue;World.spawnObject(o);stations.add(o);
                for(int dx=-1;dx<=o.getDefinitions().sizeX;dx++)for(int dy=-1;dy<=o.getDefinitions().sizeY;dy++){
                    if(!World.isFloorFree(0,x+dx,y+dy,1))continue;player.setLocation(new WorldTile(x+dx,y+dy,0));World.updateEntityRegion(player);player.resetMasks();
                    if(Native950Mining.current(o)&&Native950Mining.inReach(player,o)){run(2);return o;}
                }
            }throw new AssertionError("Cannot place station "+id);
        }
        void object(com.rs.game.WorldObject o,int option){int[] opcodes={34,48,24,41,73};ByteBuf b=Unpooled.buffer(10);b.writeByte((opcodes[option-1]+clientCipher.getAsInt())&255);b.writeByte(o.getY()+128);b.writeByte(o.getY()>>>8);b.writeInt(o.getId());b.writeByte(o.getX()>>>8);b.writeByte(o.getX()+128);b.writeByte(0);send(b);}
        void moveAway(com.rs.game.WorldObject object) {
            for(int radius=5;radius<=8;radius++)for(int dx=-radius;dx<=radius;dx++)for(int dy=-radius;dy<=radius;dy++) {
                if(Math.max(Math.abs(dx),Math.abs(dy))!=radius)continue;
                int x=object.getX()+dx,y=object.getY()+dy;
                if(!World.isFloorFree(object.getPlane(),x,y,1))continue;
                int route=com.rs.game.route.RouteFinder.findRoute(com.rs.game.route.RouteFinder.WALK_ROUTEFINDER,x,y,object.getPlane(),1,
                        new com.rs.game.route.strategy.ObjectStrategy(object),false);
                if(route<=0||com.rs.game.route.RouteFinder.lastIsAlternative())continue;
                player.setLocation(new WorldTile(x,y,object.getPlane()));World.updateEntityRegion(player);player.resetMasks();run(2);return;
            }
            throw new AssertionError("No reachable distant Use approach for "+object.getId());
        }
        void useObject(int source,int slot,int hash,com.rs.game.WorldObject object) {
            ByteBuf b=Unpooled.buffer(19);b.writeByte((90+clientCipher.getAsInt())&255);
            b.writeByte(0);b.writeByte(object.getX()>>>8);b.writeByte(object.getX()+128);b.writeShort(slot);
            b.writeByte(object.getY()+128);b.writeByte(object.getY()>>>8);b.writeMediumLE(source);b.writeIntLE(object.getId());
            b.writeByte(hash>>>8);b.writeByte(hash);b.writeByte(hash>>>24);b.writeByte(hash>>>16);send(b);
        }
        void give(int id,int amount){require(Native950Skilling.giveItem(player,id,amount),"Fixture cannot give actual950 item "+id);}
        void level(int skill,int level){player.getSkills().setXpWithoutRefresh(skill,Skills.getXPForLevel(skill,level));player.getSkills().setLevelWithoutRefresh(skill,level);}
        double xp(int skill){return player.getSkills().getXp(skill);}
        int amount(int id){return player.getInventory().items.getNumberOf(id);}
        int slot(int id){int slot=player.getInventory().items.lookupSlot(id);require(slot>=0,"Missing inventory endpoint "+id);return slot;}
        void run(int count){for(int i=0;i<count;i++){ticks++;input.beginTick();player.processEntity();player.processEntityUpdate();input.afterMovement();channel.write(Native950Packets.tickEnd());drain();player.resetMasks();}}
        boolean nativeQuantityStep;
        Native950ProductionMenu menu(){try{java.lang.reflect.Field f=Native950Interactions.class.getDeclaredField("productionMenu");f.setAccessible(true);return (Native950ProductionMenu)f.get(input);}catch(ReflectiveOperationException ex){throw new AssertionError(ex);}}
        void choose(int row){
            Native950ProductionMenu m=menu();
            if(!m.isNativeOpen()){nativeQuantityStep=false;response(1188,8+5*row);return;}
            if(!nativeQuantityStep){
                require(row==0,"Native fixture needs explicit product selection for row "+row);
                Native950ProductionUiCatalog.Category c=Native950ProductionUiCatalog.byId(m.currentCategoryId());
                require(c!=null,"Unknown native category");int ordinal=-1;for(int i=0;i<c.displayIds.length;i++)if(c.displayIds[i]==m.selectedDisplayId()){ordinal=i;break;}
                require(ordinal>=0,"Selected native product missing from cache category");
                nativeButton(1371,22,1+4*ordinal,m.selectedDisplayId());nativeQuantityStep=true;
            }else{
                int amount=row==3?m.maximumQuantity():row==0?1:row==1?5:row==2?10:0;
                require(amount>0,"Unsupported native quantity choice");amount=Math.min(amount,m.maximumQuantity());
                nativeButton(1371,20,amount-1,-1);response(1370,30);nativeQuantityStep=false;
            }
        }
        void nativeButton(int face,int component,int actor,int item){int hash=(face<<16)|component;ByteBuf b=Unpooled.buffer(10);b.writeByte((18+clientCipher.getAsInt())&255);b.writeMedium(item);b.writeByte(hash>>>16);b.writeByte(hash>>>24);b.writeByte(hash);b.writeByte(hash>>>8);b.writeShort(actor);send(b);}
        void response(int face,int component){ByteBuf b=Unpooled.buffer(7);b.writeByte((101+clientCipher.getAsInt())&255);b.writeInt((face<<16)|component);b.writeByte(255);b.writeByte(127);send(b);}
        void button(int id,int option){int hash=(1473<<16)|5,slot=slot(id);int[] opcodes={18,122,89,100,81};
            ByteBuf b=Unpooled.buffer(10);b.writeByte((opcodes[option-1]+clientCipher.getAsInt())&255);b.writeMedium(id);
            b.writeByte(hash>>>16);b.writeByte(hash>>>24);b.writeByte(hash);b.writeByte(hash>>>8);b.writeShort(slot);send(b);}
        void use(int source,int target){int src=slot(source),dst=slot(target),hash=(1473<<16)|5;
            ByteBuf b=Unpooled.buffer(19);b.writeByte((69+clientCipher.getAsInt())&255);
            b.writeByte(src>>>8);b.writeByte(src+128);b.writeByte(source>>>16);b.writeByte(source);b.writeByte(source>>>8);
            b.writeByte(hash>>>8);b.writeByte(hash);b.writeByte(hash>>>24);b.writeByte(hash>>>16);b.writeShort(dst);
            b.writeByte(hash>>>8);b.writeByte(hash);b.writeByte(hash>>>24);b.writeByte(hash>>>16);b.writeMediumLE(target);send(b);}
        void send(ByteBuf bytes){channel.writeInbound(bytes);channel.runPendingTasks();require(transport.drainActions(input::handle)==1,"Encrypted production click failed to reach router");}
        boolean hasStat(int skill){return statSkills.contains(skill);}
        final Set<Integer> statSkills=new HashSet<>();
        int makeEventMask=-1;
        int count(ServerPacket kind){int count=0;for(ServerPacket row:output)if(row==kind)count++;return count;}
        void drain(){channel.flush();channel.runPendingTasks();Object message;
            while((message=channel.readOutbound())!=null)try{
                require(message instanceof ByteBuf,"Transport emitted nonbytes");ByteBuf bytes=(ByteBuf)message;
                while(bytes.isReadable()){
                    int opcode=(bytes.readUnsignedByte()-cipher.getAsInt())&255;
                    if(opcode>=128)opcode=((opcode-128)<<8)|((bytes.readUnsignedByte()-cipher.getAsInt())&255);
                    ServerPacket kind=null;for(ServerPacket row:ServerPacket.values())if(row.opcode()==opcode){kind=row;break;}
                    require(kind!=null,"Unrecognized950 opcode "+opcode);int length=kind.size();
                    if(length==-1)length=bytes.readUnsignedByte();else if(length==-2)length=bytes.readUnsignedShort();
                    require(length>=0&&length<=bytes.readableBytes(),"Bad950 frame length "+kind);
                    if(kind==ServerPacket.IF_SETEVENTS&&length==12&&bytes.getIntLE(bytes.readerIndex()+8)==((1370<<16)|30)){
                        int at=bytes.readerIndex();makeEventMask=bytes.getUnsignedByte(at+1)|(bytes.getUnsignedByte(at)<<8)|(bytes.getUnsignedByte(at+3)<<16)|(bytes.getUnsignedByte(at+2)<<24);
                        require(makeEventMask==1,"Make target must preserve cache pause bit0");
                    }
                    if(kind==ServerPacket.UPDATE_STAT&&length>0)statSkills.add((-bytes.getUnsignedByte(bytes.readerIndex()))&255);
                    bytes.skipBytes(length);output.add(kind);frames++;
                }
            }finally{ReferenceCountUtil.release(message);}
            channel.checkException();require(channel.isActive()&&transport.terminalFailure()==null,"Production transport failed: "+transport.terminalFailure());
        }
        public void close(){input.close();for(com.rs.game.WorldObject o:stations)World.removeObject(o);World.removeNative950Player(player);channel.finishAndReleaseAll();}
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
