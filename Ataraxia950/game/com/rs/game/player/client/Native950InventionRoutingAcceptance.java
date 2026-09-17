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
public final class Native950InventionRoutingAcceptance {
    private static int frames,ticks;
    public static void main(String[] args)throws Exception {
        require(args.length==1,"Usage: Native950InventionRoutingAcceptance <950-flat-cache>");
        require(NativeCacheVerification.isEnforced(),"Cache verification must remain enabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World.getInstance().execute(()->{
            Native950Invention.verifyCacheBindings();Native950Archaeology.verifyCacheBindings();Native950Divination.verifyCacheBindings();Native950Familiars.verifyCacheBindings();
            try(Fixture f=new Fixture()){check(f);}
            return null;
        }).get(120,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"World scheduler failed");
        System.out.println("PASS: "+(Native950Invention.recipes().size()+Native950Archaeology.recipes().size())+" pinned additional recipes, "+ticks+" original engine ticks, "+frames+" decoded encrypted950 frames; no account file, listener or authentication");
        System.out.println("LIMIT: menu pixels and animation rendering still require the real client.");
    }
    private static int option(List<Native950ProductionMenu.Choice> rows,String name){for(int i=0;i<rows.size();i++)if(rows.get(i).label.startsWith(name))return i;throw new AssertionError("Missing option "+name);}
    private static void choose(Fixture f,int index){for(int page=0;page<index/3;page++)f.choose(3);f.choose(index%3);}
    private static void check(Fixture f) {
        f.level(Skills.CRAFTING,99);f.level(Skills.DIVINATION,99);f.level(Skills.SMITHING,99);f.level(Skills.INVENTION,120);f.level(Skills.ARCHAEOLOGY,1);
        checkInventoryFlows(f);
        com.rs.game.WorldObject inventor=f.station(100874);f.give(1513,2);f.run(2);require(f.pouchUnlocked,"Pouch unlock never reached encrypted output");
        int index=option(Native950Invention.choices(f.player,inventor),"Disassemble Magic logs");
        f.moveAway(inventor);double xp=f.xp(Skills.INVENTION);f.object(inventor,1);require(f.player.hasWalkSteps(),"Invention click did not approach");require(f.amount(1513)==2,"Distant click consumed logs");f.run(40);choose(f,index);f.choose(0);f.run(14);require(f.amount(1513)==1&&f.xp(Skills.INVENTION)>xp,"Encrypted disassembly menu/quantity failed");
        f.choose(0);f.choose(0);f.run(4);require(f.amount(1513)==1,"Retired disassembly response consumed another log");
        // Anywhere disassembly through the encrypted native pouch drop, including noted stacks.
        f.clear();f.give(1514,5);f.run(2);int noteSlot=f.slot(1514);
        require(Native950Invention.baseItem(1514)==1513,"950 note link failed");
        com.rs.game.player.actions.Action owned=new com.rs.game.player.actions.Action(){
            public boolean start(Player p){return true;}public boolean process(Player p){return true;}
            public int processWithDelay(Player p){return 10;}public void stop(Player p){p.unlock();}
        };
        require(f.player.getActionManager().setAction(owned),"Cannot set owned action");f.player.lock();
        f.pouchDrop(noteSlot,1514);require(f.player.isLocked(),"Pouch cancelled and unlocked owned obstacle");
        f.player.unlock();f.player.getActionManager().forceStop();f.run(2);
        f.pouchDrop(noteSlot,1514);require(f.amount(1514)==5,"Drop consumed before quantity confirmation");
        f.choose(0);f.choose(0);f.run(12);require(f.amount(1514)==4,"Noted pouch disassembly failed");
        f.pouchDrop(noteSlot,1514);f.choose(0);Item original=f.player.getInventory().getItem(noteSlot);
        f.player.getInventory().items.set(noteSlot,new Item(1514,4));f.choose(0);f.run(12);
        require(f.amount(1514)==4,"Replaced selected instance survived stale confirmation");
        f.pouchDrop(noteSlot,1514);f.choose(0);f.input.walking();f.choose(0);f.run(8);
        require(f.amount(1514)==4,"Walking failed to cancel disassembly prompt");
        f.pouchDrop(noteSlot,1513);f.choose(0);f.choose(0);f.run(8);
        require(f.amount(1514)==4,"Forged item claim consumed notes");
        require(Native950Invention.startDisassemble(f.player,null,1514,60),"Anywhere start failed");
        f.player.getActionManager().forceStop();f.run(8);require(f.amount(1514)==4,"Cancelled action consumed notes");
        f.clear();inventor=f.station(100874);Native950Invention.Recipe recipe=Native950Invention.recipes().get(0);Native950Invention.restoreMaterials(f.player,recipe.componentCosts);index=option(Native950Invention.choices(f.player,inventor),"Manufacture Charge pack");f.object(inventor,1);f.run(2);choose(f,index);f.choose(0);f.run(14);require(f.amount(36389)==1,"Encrypted manufacture menu failed");
        f.clear();inventor=f.station(100873);f.level(Skills.INVENTION,33);
        require(Native950InventionResearch.tier(f.player)==0,"Free research tier");
        require(!Native950InventionResearch.start(f.player,inventor,1),"Below-level research accepted");
        f.level(Skills.INVENTION,34);xp=f.xp(Skills.INVENTION);
        index=option(Native950Invention.choices(f.player,inventor),"Research junk reduction 1 ");f.object(inventor,1);f.run(2);choose(f,index);f.run(8);
        require(Native950InventionResearch.tier(f.player)==1&&f.xp(Skills.INVENTION)>xp,"Normal Discover research failed");
        xp=f.xp(Skills.INVENTION);f.choose(1);f.run(2);require(f.xp(Skills.INVENTION)==xp,"Stale research response granted XP");
        f.level(Skills.INVENTION,120);require(Native950InventionResearch.start(f.player,inventor,2),"Research cancellation setup");
        f.input.walking();f.run(8);require(Native950InventionResearch.tier(f.player)==1,"Cancelled study unlocked research");
        for(int tier=2;tier<=9;tier++){
            index=option(Native950Invention.choices(f.player,inventor),"Research junk reduction "+tier+" ");
            f.object(inventor,1);f.run(2);choose(f,index);f.run(8);
            require(Native950InventionResearch.tier(f.player)==tier,"Research progression failed "+tier);
        }
        require(!Native950InventionResearch.start(f.player,inventor,9),"Research can be replayed");
        require(Math.abs(Native950InventionResearch.effectiveJunk(50.5,9)-40.4)<.00001,"Wrong reduction formula");
        System.out.println("PASS normal encrypted Discover flow for all nine950 research rows; level gate, cancellation, stale responses, exactly-once unlock/XP and global reduction");
        f.clear();com.rs.game.WorldObject remains=f.station(116393);f.give(49539,1);f.run(2);f.moveAway(remains);xp=f.xp(Skills.ARCHAEOLOGY);f.object(remains,1);require(f.player.hasWalkSteps(),"Excavate click did not approach");require(f.amount(49444)==0,"Remote excavation granted resources");f.run(65);require(f.amount(49741)>0&&f.xp(Skills.ARCHAEOLOGY)>xp,"Encrypted level1 excavation did not produce damaged artefact");
        com.rs.game.WorldObject bench=f.station(115421);f.moveAway(bench);f.object(bench,2);require(f.player.hasWalkSteps(),"Store click did not approach");f.run(40);require(f.amount(49444)==0&&f.amount(49445)==0&&Native950Archaeology.materials(f.player)[0]>=5,"Encrypted Store did not move backpack materials into storage");
        index=option(Native950Archaeology.choices(f.player,bench),"Centurion's dress sword (");f.object(bench,1);f.run(2);choose(f,index);f.choose(0);f.run(14);require(f.amount(49742)==1,"Encrypted Restore menu/quantity failed");
        require(f.hasStat(Skills.INVENTION)&&f.hasStat(Skills.ARCHAEOLOGY),"Native XP output missing");
        System.out.println("PASS encrypted950 Invention approach -> disassemble/quantity/stale response/manufacture and Archaeology approach -> excavate -> Store option2 -> restore/quantity; actual materials and native XP");
    }
    private static void checkInventoryFlows(Fixture f){
        f.clear();Native950Divination.restoreBoons(f.player,null);
        Native950Divination.Boon boon=Native950Divination.boonRecipes().get(0);int energy=boon.energyIds()[0];f.level(Skills.DIVINATION,boon.level);
        require(Native950InventoryMenu.usesOrdinaryOperations(energy)&&Native950InventoryMenu.ordinaryCacheOption(1)==1
                &&"Weave".equals(Native950Skilling.itemType(f.player,energy).option(1)),"950 energy Weave is not ordinary operation1");
        f.give(energy,boon.cost);f.run(2);int slot=f.slot(energy),before=f.count(ServerPacket.UPDATE_INV_PARTIAL)+f.count(ServerPacket.UPDATE_INV_FULL);double xp=f.xp(Skills.DIVINATION);
        List<Native950ProductionMenu.Choice> choices=Native950Divination.weaveChoices(f.player,slot,energy,1);int choice=option(choices,boon.name);
        require(choices.size()==1,"Pale energy should have exactlyone supported boon");f.expectedWeaveChoices=Native950Packets.runClientScript(5589,2,"Divination boons",choices.get(0).label,"Cancel","","","").payload();f.button(energy,1);f.run(1);
        require(f.amount(energy)==boon.cost&&!Native950Divination.boons(f.player)[boon.index],"Weave consumed energy before selection");
        require(f.weaveChoicesRendered,"Encrypted Weave click did not emit the exact native5589 choice payload");
        choose(f,choice);f.run(5);
        require(f.amount(energy)==0&&Native950Divination.boons(f.player)[boon.index]&&f.xp(Skills.DIVINATION)>xp,"Encrypted inventory Weave did not unlock boon");
        require(f.hasStat(Skills.DIVINATION)&&f.count(ServerPacket.UPDATE_INV_PARTIAL)+f.count(ServerPacket.UPDATE_INV_FULL)>before,"Weave consumption/XP did not reach encrypted output");
        Native950SkillProgress captured=Native950SkillProgress.capture(f.player);require(captured.boons()[boon.index],"Unlocked boon missing from skill snapshot");
        com.rs.game.player.actions.divination.MemoryInfo memory=com.rs.game.player.actions.divination.MemoryInfo.values()[boon.index];
        com.rs.game.player.actions.divination.DivinationConvert.ConvertMode mode=com.rs.game.player.actions.divination.DivinationConvert.ConvertMode.CONVERT_TO_XP;
        double base=com.rs.game.player.actions.divination.DivinationConvert.ordinaryXp(memory,false,mode,false,false);
        double boosted=com.rs.game.player.actions.divination.DivinationConvert.ordinaryXp(memory,false,mode,false,captured.boons()[boon.index]);
        require(Math.abs(boosted-base*1.1)<.000001,"Routed boon snapshot does not enable10percent conversion benefit");
        xp=f.xp(Skills.DIVINATION);f.choose(0);f.run(4);require(f.xp(Skills.DIVINATION)==xp,"Retired Weave response awarded twice");
        // Open a second normal Weave selection, replace the source, and deliver its old response.
        f.clear();Native950Divination.restoreBoons(f.player,null);f.give(energy,boon.cost);f.run(2);slot=f.slot(energy);f.button(energy,1);f.run(1);
        f.player.getInventory().items.set(slot,new Item(energy,boon.cost));f.choose(choice);f.run(5);
        require(!Native950Divination.boons(f.player)[boon.index]&&f.amount(energy)==boon.cost,"Encrypted stale Weave selection consumed replacement stack");
        f.clear();f.level(Skills.DIVINATION,99);f.level(Skills.SUMMONING,99);Native950Familiars.clear(f.player);
        int pouch=12047;Native950Familiars.Profile familiar=Native950Familiars.profile(pouch);require(familiar!=null,"950 spirit wolf pouch profile unavailable");
        require(Native950InventoryMenu.usesOrdinaryOperations(pouch)&&"Summon".equals(Native950Skilling.itemType(f.player,pouch).option(1)),"950 Summon is not ordinary operation1");
        f.give(pouch,1);f.run(2);before=f.count(ServerPacket.UPDATE_INV_PARTIAL)+f.count(ServerPacket.UPDATE_INV_FULL);f.button(pouch,1);f.run(2);
        int[] active=Native950Familiars.snapshot(f.player);com.rs.game.npc.NPC follower=Native950Familiars.current(f.player);
        require(f.amount(pouch)==0&&active[0]==pouch&&active[1]>0&&active[1]<=familiar.durationTicks,"Encrypted inventory Summon did not consume pouch/start lifetime");
        require(follower!=null&&follower.getId()==familiar.npcId&&Native950Familiars.isFamiliar(follower)&&World.getNPCs().get(follower.getIndex())==follower,"Summon did not publish owned current950 NPC");
        require(f.count(ServerPacket.UPDATE_INV_PARTIAL)+f.count(ServerPacket.UPDATE_INV_FULL)>before,"Summon pouch removal missing from encrypted output");
        require(Native950SkillProgress.capture(f.player).familiar()[0]==pouch,"Summoned familiar missing from skill snapshot");
        Native950Familiars.onLogout(f.player);require(!Native950Familiars.isFamiliar(follower)&&!Native950World.getInstance().nativeNpcs().contains(follower),"Probe logout leaked familiar NPC");Native950Familiars.clear(f.player);f.clear();
        System.out.println("PASS encrypted inventory Weave operation1 -> native choice -> boon cost/unlock/XP/output/snapshot/10percent effect; stale selection and duplicate response refusal; Summon operation1 -> pouch removal/owned950 familiar/lifetime/snapshot/logout cleanup");
    }
    private static final class Fixture implements AutoCloseable {
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
        void pouchDrop(int slot,int id){
            int source=(1473<<16)|5,target=Native950InventionUi.HASH;
            ByteBuf b=Unpooled.buffer(19);b.writeByte((12+clientCipher.getAsInt())&255);b.writeShortLE(slot);
            b.writeByte(source>>>8);b.writeByte(source);b.writeByte(source>>>24);b.writeByte(source>>>16);b.writeMediumLE(id);
            b.writeShortLE(Native950InventionUi.SLOT);b.writeByte(target>>>16);b.writeByte(target>>>24);b.writeByte(target);b.writeByte(target>>>8);
            b.writeByte(255);b.writeByte(255);b.writeByte(255);send(b);
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
        boolean pouchUnlocked,weaveChoicesRendered;byte[] expectedWeaveChoices;
        final Set<Integer> statSkills=new HashSet<>();
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
                    if(kind==ServerPacket.RUNCLIENTSCRIPT&&expectedWeaveChoices!=null){byte[] payload=new byte[length];bytes.getBytes(bytes.readerIndex(),payload);if(Arrays.equals(payload,expectedWeaveChoices))weaveChoicesRendered=true;}
                    if(kind==ServerPacket.UPDATE_STAT&&length>0)statSkills.add((-bytes.getUnsignedByte(bytes.readerIndex()))&255);
                    if(kind==ServerPacket.VARP_LARGE){byte[] raw=new byte[length];bytes.getBytes(bytes.readerIndex(),raw);if(Arrays.equals(raw,Native950Packets.varp(5987,128).payload()))pouchUnlocked=true;}
                    bytes.skipBytes(length);output.add(kind);frames++;
                }
            }finally{ReferenceCountUtil.release(message);}
            channel.checkException();require(channel.isActive()&&transport.terminalFailure()==null,"Production transport failed: "+transport.terminalFailure());
        }
        public void close(){Native950Familiars.onLogout(player);input.close();for(com.rs.game.WorldObject o:stations)World.removeObject(o);World.removeNative950Player(player);channel.finishAndReleaseAll();}
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
