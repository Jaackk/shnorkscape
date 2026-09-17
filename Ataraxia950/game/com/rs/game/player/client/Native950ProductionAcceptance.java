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
public final class Native950ProductionAcceptance {
    private static int frames,ticks;
    public static void main(String[] args)throws Exception {
        require(args.length==1,"Usage: Native950ProductionAcceptance <950-flat-cache>");
        require(NativeCacheVerification.isEnforced(),"Cache verification must remain enabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World.getInstance().execute(()->{
            for(Native950Production.Recipe recipe:Native950Production.recipes())
                require(Native950Production.verified(recipe),"Unverified actual950 recipe: "+recipe.label);
            try(Fixture f=new Fixture()){check(f);}
            return null;
        }).get(120,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"World scheduler failed");
        System.out.println("PASS: "+Native950Production.recipes().size()+" pinned recipes, "+ticks+" original engine ticks, "+frames+" decoded encrypted950 frames; no account file, listener or authentication");
        System.out.println("LIMIT: menu pixels and animation rendering still require the real client.");
    }
    private static void check(Fixture f) {
        f.give(946,1);f.give(1511,3);
        double fletching=f.xp(Skills.FLETCHING);
        f.use(946,1511);f.response(1188,12); // malformed non-row must not consume the published choices
        require(!f.player.getActionManager().hasSkillWorking(),"Malformed response started production");
        f.choose(0);f.choose(1); // Arrow shafts, Make5; only three logs exist.
        f.run(30);
        require(f.amount(1511)==0&&f.amount(52)==45&&f.amount(946)==1,"Make5 must stop after three logs and retain knife");
        require(f.xp(Skills.FLETCHING)>fletching&&f.hasStat(Skills.FLETCHING),"Fletching UPDATE_STAT/XP missing");
        require(!f.player.getActionManager().hasSkillWorking(),"Exhausted production action stayed active");
        double stopped=f.xp(Skills.FLETCHING);f.choose(1);f.run(6);
        require(f.amount(52)==45&&f.xp(Skills.FLETCHING)==stopped,"Retired quantity response repeated production");
        System.out.println("PASS: encrypted69 use -> native1188 product/quantity -> original action loop -> bounded material use, retained tool, UPDATE_STAT; malformed/stale dialogue refused");

        f.give(1511,1);f.use(1511,946);f.choose(0);f.choose(2);f.input.walking();f.run(8);
        require(f.amount(1511)==1&&f.amount(52)==45&&f.xp(Skills.FLETCHING)==stopped,"Walking before first action tick awarded items/XP");
        f.use(946,1511);f.input.walking();f.choose(0);f.choose(0);f.run(8);
        require(f.amount(1511)==1&&f.amount(52)==45,"Closed product menu accepted delayed dialogue");
        f.level(Skills.FLETCHING,10);
        f.button(1511,1);f.choose(3);f.choose(0);f.choose(0);f.run(10);
        require(f.amount(9440)==1&&f.amount(1511)==0,"Craft option/More choices did not make wooden stock");
        System.out.println("PASS: reversed item use, walking cancels action/menu, native Craft option, paged fourth recipe and Make1");

        f.give(314,15);f.use(52,314);f.choose(0);f.choose(0);f.run(10);
        require(f.amount(52)==30&&f.amount(314)==0&&f.amount(53)==15,"Headless-arrow batch quantities wrong");
        f.give(39,15);f.use(39,53);f.choose(0);f.choose(0);f.run(10);
        require(f.amount(53)==0&&f.amount(39)==0&&f.amount(882)==15,"Bronze-arrow batch quantities wrong");
        System.out.println("PASS: two stack inputs atomically produce original15-item arrow batches");

        f.give(1755,1);f.give(1623,1);double crafting=f.xp(Skills.CRAFTING);
        f.use(1755,1623);f.choose(0);f.choose(0);f.run(8);
        require(f.amount(1623)==1&&f.amount(1607)==0&&f.xp(Skills.CRAFTING)==crafting,"Level1 crafted a level20 sapphire");
        f.level(Skills.CRAFTING,20);crafting=f.xp(Skills.CRAFTING);
        f.use(1623,1755);f.choose(0);f.choose(0);f.run(10);
        require(f.amount(1623)==0&&f.amount(1607)==1&&f.amount(1755)==1&&f.xp(Skills.CRAFTING)>crafting&&f.hasStat(Skills.CRAFTING),"Gem conversion/tool/XP failed");
        System.out.println("PASS: cache-backed gem recipe preserves original level gate, chisel and Crafting XP");

        f.level(Skills.HERBLORE,3);f.give(199,1);double herb=f.xp(Skills.HERBLORE);
        f.button(199,1);f.run(8);
        require(f.amount(199)==0&&f.amount(249)==1&&f.xp(Skills.HERBLORE)>herb,"Clean option did not use original herb action/XP");
        f.give(227,1);f.use(249,227);f.choose(0);f.choose(0);f.run(8);
        require(f.amount(249)==0&&f.amount(227)==0&&f.amount(91)==1,"Unfinished guam potion conversion failed");
        f.give(221,1);herb=f.xp(Skills.HERBLORE);f.use(221,91);f.choose(0);f.choose(0);f.run(8);
        require(f.amount(91)==0&&f.amount(221)==0&&f.amount(121)==1&&f.xp(Skills.HERBLORE)>herb&&f.hasStat(Skills.HERBLORE),"Attack potion conversion/XP failed");
        f.give(233,1);f.give(1973,1);herb=f.xp(Skills.HERBLORE);f.use(233,1973);f.choose(0);f.choose(0);f.run(8);
        require(f.amount(1973)==0&&f.amount(1975)==1&&f.amount(233)==1&&f.xp(Skills.HERBLORE)==herb,"Chocolate grind quantities/tool/zeroXP failed");
        System.out.println("PASS: Clean -> unfinished potion -> attack potion, original XP and zero-XP ingredient grinding");

        Native950Save save=Native950PlayerBinder.capture(f.player,f.input.saveSnapshot(),System.currentTimeMillis());
        for(int skill:new int[]{Skills.FLETCHING,Skills.CRAFTING,Skills.HERBLORE})
            require(save.skills().xp(skill)==f.xp(skill),"Skill XP absent from character snapshot: "+skill);
        require(f.count(ServerPacket.UPDATE_INV_PARTIAL)>0||f.count(ServerPacket.UPDATE_INV_FULL)>0,"No native inventory synchronization");
        System.out.println("PASS: resulting skills reach existing character save snapshot and native inventory synchronization");
    }
    private static final class Fixture implements AutoCloseable {
        final int[] incoming={9,5,0,2},outgoing={59,55,50,52};
        final Native950Isaac clientCipher=new Native950Isaac(incoming),cipher=new Native950Isaac(outgoing);
        final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(incoming),new Native950Isaac(outgoing),Thread.currentThread());
        final EmbeddedChannel channel=new EmbeddedChannel(transport);
        final List<ServerPacket> output=new ArrayList<>();
        final Player player;final Native950Interactions input;
        Fixture(){
            player=Player.createNative950("produce-test",new WorldTile(3217,3258,0),channel);
            player.setActive(true);player.setRunning(true);Native950World.installVarpSink(player);
            Native950ItemCatalog catalog=new Native950ItemCatalog(Collections.<Native950ItemCatalog.Entry>emptyList()).withLegacyDrops();
            Native950Content.BankUi bank=new Native950Content.BankUi(517,201,15,317,39,new int[11],new int[11],Collections.emptyList(),Collections.emptyList());
            input=new Native950Interactions(player,channel,new Native950Content(catalog,bank),null,null);
            for(int slot=0;slot<28;slot++)player.getInventory().items.set(slot,null);
            input.bootstrap();drain();output.clear();
        }
        void give(int id,int amount){require(Native950Skilling.giveItem(player,id,amount),"Fixture cannot give actual950 item "+id);}
        void level(int skill,int level){player.getSkills().setXpWithoutRefresh(skill,Skills.getXPForLevel(skill,level));player.getSkills().setLevelWithoutRefresh(skill,level);}
        double xp(int skill){return player.getSkills().getXp(skill);}
        int amount(int id){return player.getInventory().items.getNumberOf(id);}
        int slot(int id){int slot=player.getInventory().items.lookupSlot(id);require(slot>=0,"Missing inventory endpoint "+id);return slot;}
        void run(int count){for(int i=0;i<count;i++){ticks++;input.beginTick();player.processEntity();player.processEntityUpdate();input.afterMovement();channel.write(Native950Packets.tickEnd());drain();player.resetMasks();}}
        void choose(int row){response(1188,8+5*row);}
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
                    if(kind==ServerPacket.UPDATE_STAT&&length>0)statSkills.add((-bytes.getUnsignedByte(bytes.readerIndex()))&255);
                    bytes.skipBytes(length);output.add(kind);frames++;
                }
            }finally{ReferenceCountUtil.release(message);}
            channel.checkException();require(channel.isActive()&&transport.terminalFailure()==null,"Production transport failed: "+transport.terminalFailure());
        }
        public void close(){input.close();channel.finishAndReleaseAll();}
    }
    private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
}
