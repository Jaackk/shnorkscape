package com.rs.game.player.client;
import com.rs.cache.Cache;
import com.rs.game.*;
import com.rs.game.player.*;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.protocol.modern950.*;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.buffer.ByteBuf;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

/** Isolated chosen-tile input -> actual movement -> native player frames, never client rendering. */
public final class Native950DiveAcceptance {
    static Fixture fixture;
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));Native950World world=Native950World.getInstance();
        try{
            world.execute(()->{fixture=new Fixture();fixture.start();return null;}).get(30,TimeUnit.SECONDS);
            boolean arrived=false;
            for(int i=0;i<30&&!arrived;i++){Thread.sleep(100);arrived=world.execute(()->fixture.p.getNextWorldTile()!=null).get(5,TimeUnit.SECONDS);}
            check(arrived,"Real scheduler never queued Dive arrival");
            world.execute(()->{fixture.finish();return null;}).get(10,TimeUnit.SECONDS);
            System.out.println("PASS: source-safe opcode85 -> actual950 collision -> scheduled force movement -> authoritative chosen tile -> native PLAYER_INFO with no duplicate terminal movement; cooldown and stale-source refusal. LIVE Vulkan pending.");
        }finally{if(fixture!=null)world.execute(()->{fixture.close();return null;}).get(10,TimeUnit.SECONDS);}
    }
    static final class Fixture {
        final EmbeddedChannel channel=new EmbeddedChannel(new Native950GameTransport(()->0,()->0,Thread.currentThread()));
        final Player p=Player.createNative950("dive-offline",new WorldTile(3217,3258,0),channel);
        final Native950EntityFrames frames=new Native950EntityFrames();
        final Native950MeleeCombat combat=new Native950MeleeCombat(Thread.currentThread());
        Native950Interactions input;WorldTile destination;long generation;List<Player> roster;
        Fixture()throws Exception{
            p.setIndex(1);p.setActive(true);p.setRunning(true);p.getSkills().setLevelWithoutRefresh(Skills.AGILITY,99);
            World.addNative950Player(p,1);World.updateEntityRegion(p);p.loadMapRegions();p.setClientHasLoadedMapRegion();
            Native950World.installVarpSink(p);combat.attach(p);
            Native950Content.BankUi bank=new Native950Content.BankUi(517,201,15,317,39,new int[11],new int[11],Collections.emptyList(),Collections.emptyList(),6);
            input=new Native950Interactions(p,channel,new Native950Content(new Native950ItemCatalog(Collections.emptyList()).withLegacyDrops(),bank));
            java.lang.reflect.Field field=Native950ActionBar.class.getDeclaredField("bars");field.setAccessible(true);
            ((int[][])field.get(p.getNative950ActionBar()))[0][0]=Native950ActionBar.pack(1,7);
            p.getInterfaceManager().registerNativeOpen(1430,1436,0);
            p.getNative950ActionBar().setActiveBar(p,channel,0);p.getAppearence().generateAppearenceData();roster=Collections.singletonList(p);
            for(Native950Packets.Packet packet:frames.admit(p,new Native950World.SceneConfig(p.getX(),p.getY(),0,1,7,0,0,0),roster))channel.write(packet);
            drain();p.resetMasks();
            for(int dx=-3;dx<=3&&destination==null;dx++)for(int dy=-3;dy<=3;dy++){
                if(Math.max(Math.abs(dx),Math.abs(dy))<2)continue;
                WorldTile target=new WorldTile(p.getX()+dx,p.getY()+dy,0);
                WorldTile resolved=Native950Surge.targetedDestination(p,target,(t,x,y)->World.isRegionLoaded(t.getRegionId())&&World.canMoveNPC(new WorldTile(t.getX()+x,t.getY()+y,0),1)&&World.checkWalkStep(0,t.getX(),t.getY(),x,y,1));
                if(resolved.matches(target)){destination=target;break;}
            }
            check(destination!=null,"No clear actual950 destination");
        }
        Native950Actions.Action tile(int slot){int x=destination.getX(),y=destination.getY();return Native950Actions.decode(85,new byte[]{(byte)(slot>>8),(byte)slot,-1,-1,-1,5,(byte)150,0,(byte)(64+13*slot),(byte)(x>>8),(byte)(x+128),(byte)y,(byte)(y>>8)});}
        void start(){
            input.handle(tile(1));check(p.getNextNative950ForceMovement()==null,"Wrong source slot accepted");drain();
            input.handle(tile(0));check(p.getNextNative950ForceMovement()!=null,"Correct opcode85 did not start Dive");
            check(destination.matches(p.getNextNative950ForceMovement().finalTile()),"Dive used facing rather than chosen tile");generation=p.getNative950ForceMaskGeneration();
            publish();List<byte[]> outputs=drain();check(outputs.stream().anyMatch(b->(b[0]&255)==Native950Protocol.ServerPacket.PLAYER_INFO.opcode()),"Initial PLAYER_INFO absent");
            p.resetMasks();
        }
        void publish(){frames.beginFrames(roster);frames.encode(new Native950Frames.Frame(p,channel,roster,Collections.emptyList(),null,false,7,p.getX(),p.getY(),p.getPlane()));}
        void finish(){
            check(p.getNative950ForceArrivalGeneration()==generation,"Wrong terminal generation");p.processMovement();
            check(destination.matches(p)&&p.getNextWorldTile()==null,"Chosen destination not committed");publish();
            byte[] terminal=drain().stream().filter(b->(b[0]&255)==Native950Protocol.ServerPacket.PLAYER_INFO.opcode()).findFirst().orElse(null);
            check(terminal!=null&&Arrays.equals(Arrays.copyOfRange(terminal,3,terminal.length),new byte[]{0,127,(byte)244}),"Terminal frame doubled client movement");
            p.resetMasks();input.handle(tile(0));check(p.getNextNative950ForceMovement()==null,"Cooldown admitted a second Dive");
        }
        List<byte[]> drain(){channel.flushOutbound();List<byte[]> result=new ArrayList<>();Object value;while((value=channel.readOutbound())!=null){check(value instanceof ByteBuf,"Unframed output");ByteBuf b=(ByteBuf)value;try{byte[] data=new byte[b.readableBytes()];b.readBytes(data);result.add(data);}finally{b.release();}}return result;}
        void close(){if(input!=null)input.close();combat.detach(p);World.removeNative950Player(p);channel.finishAndReleaseAll();}
    }
}
