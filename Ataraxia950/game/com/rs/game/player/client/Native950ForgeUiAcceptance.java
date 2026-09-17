package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import com.rs.game.player.client.Native950ProductionThirdPassAcceptance.Fixture.CapturedPacket;
import java.util.Collections;
import com.rs.game.player.Skills;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Paired cache and real encrypted interface37 clicks; no listener, profile write or authentication. */
public final class Native950ForgeUiAcceptance {
    public static void main(String[] args)throws Exception {
        require(args.length==1,"Usage: Native950ForgeUiAcceptance <950-flat-cache>");
        require(NativeCacheVerification.isEnforced(),"Cache verification disabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World.getInstance().execute(()->{
            Native950ForgeUi.verifyCacheBindings();Native950Smithing.verifyCacheBindings();
            try(Native950ProductionThirdPassAcceptance.Fixture f=new Native950ProductionThirdPassAcceptance.Fixture()){check(f);}
            return null;
        }).get(120,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"Original scheduler failed");
        System.out.println("PASS: native950 forge tree coverage, encrypted material/product/upgrade/quantity/Start/Close controls, toolbelt hammer, large-modal isolation, stale packets and station reach.");
    }
    private static Native950ForgeUi view(Native950ProductionThirdPassAcceptance.Fixture f){
        try{java.lang.reflect.Field field=Native950Interactions.class.getDeclaredField("forgeUi");field.setAccessible(true);return (Native950ForgeUi)field.get(f.input);}
        catch(ReflectiveOperationException ex){throw new AssertionError(ex);}
    }
    private static Native950Production.Recipe recipe(List<Native950Production.Recipe> rows,int output){
        for(Native950Production.Recipe r:rows)if(r.produced().length>0&&r.produced()[0].getId()==output)return r;
        throw new AssertionError("Missing production recipe "+output);
    }
    private static void materials(Native950ProductionThirdPassAcceptance.Fixture f,Native950Production.Recipe recipe,int quantity){
        for(Item item:recipe.consumed())f.give(item.getId(),item.getAmount()*quantity);
    }
    private static void select(Native950ProductionThirdPassAcceptance.Fixture f,int product,boolean smelting){
        int[] path=Native950ForgeUi.pathForProduct(product,smelting);require(path!=null,"Missing forge product "+product);
        int[] material=Native950ForgeUi.materialPath(path[0],smelting);require(material!=null,"Missing material selector");
        f.nativeButton(37,material[0],material[1],material[2]);
        f.nativeButton(37,path[1],path[2],path[3]);
        if(path[4]!=0){int button=path[4]==1?161:path[4]==2?159:path[4]==3?157:path[4]==4?155:path[4]==5?153:151;f.nativeButton(37,button,-1,-1);}
        require(view(f).selectedProduct()==product,"Forge selected different product");
    }
    private static int hash(int component){return (1477<<16)|component;}
    private static int be(byte[] b,int at){return ((b[at]&255)<<24)|((b[at+1]&255)<<16)|((b[at+2]&255)<<8)|(b[at+3]&255);}
    private static int v1(byte[] b){return ((b[2]&255)<<24)|((b[3]&255)<<16)|((b[0]&255)<<8)|(b[1]&255);}
    private static int v2(byte[] b){return ((b[1]&255)<<24)|((b[0]&255)<<16)|((b[3]&255)<<8)|(b[2]&255);}
    private static int script(byte[] b){require(b.length>=4,"Truncated client script payload");return be(b,b.length-4);}
    private static boolean attached(CapturedPacket packet,int host,int face){
        byte[] b=packet.body;return packet.kind==ServerPacket.IF_OPENSUB&&b.length==23
                &&be(b,0)==hash(host)&&(((b[16]-128)&255)|((b[17]&255)<<8))==face;
    }
    private static void assertForgeMount(Native950ProductionThirdPassAcceptance.Fixture f,int from){
        int opens=0;for(int i=from;i<f.captured.size();i++)if(attached(f.captured.get(i),726,37))opens++;
        require(opens==1,"Forge must attach exactly once to the native large host1477:726");
        require(f.player.getInterfaceManager().getInterfaceParentId(37)==hash(726),"Forge bookkeeping must match its large wire mount");
        assertForgeIsolation(f,from,false);
    }
    private static void assertForgeIsolation(Native950ProductionThirdPassAcceptance.Fixture f,int from,boolean needClose){
        boolean closed=false;
        for(int i=from;i<f.captured.size();i++){
            CapturedPacket p=f.captured.get(i);byte[] b=p.body;int target=-1;
            if(p.kind==ServerPacket.IF_OPENSUB){require(b.length==23,"Bad open frame");target=be(b,0);}
            else if(p.kind==ServerPacket.IF_CLOSESUB){require(b.length==4,"Bad close frame");target=v2(b);if(target==hash(726))closed=true;}
            else if(p.kind==ServerPacket.IF_SETHIDE){require(b.length==5,"Bad visibility frame");target=v1(b);}
            else if(p.kind==ServerPacket.RUNCLIENTSCRIPT){
                require(script(b)!=8389,"Forge must not reset/recenter the unrelated small modal on close");
                if(script(b)==11145){require(b.length==30&&b[0]=='i'&&b[4]=='i'&&b[5]==0,"Unexpected resize signature");target=be(b,6);}
            }
            require(target!=hash(732)&&target!=hash(735),"Forge touched shared small modal component "+(target&65535));
        }
        require(!needClose||closed,"Forge never closed the native large host1477:726");
    }
    private static void checkSmallProductionAfterForge(Native950ProductionThirdPassAcceptance.Fixture f,WorldObject anvil,WorldTile tile){
        f.clear();f.player.setLocation(tile);World.updateEntityRegion(f.player);f.player.resetMasks();
        f.give(946,1);f.give(1511,1);f.level(Skills.FLETCHING,99);f.run(2);
        f.object(anvil,1);f.run(3);require(view(f).isOpen(),"Forge not open for modal transition check");
        f.nativeButton(37,42,-1,-1);f.drain();int start=f.captured.size();
        f.use(946,1511);f.drain();Native950ProductionMenu menu=f.menu();
        require(menu.isNativeOpen()&&f.player.getInterfaceManager().getInterfaceParentId(1370)==hash(735),"Make-X did not keep its small modal mount after forge");
        boolean mounted=false,wrapper=false,host=false;
        for(int i=start;i<f.captured.size();i++){
            CapturedPacket p=f.captured.get(i);byte[] b=p.body;if(attached(p,735,1370))mounted=true;
            if(p.kind==ServerPacket.RUNCLIENTSCRIPT&&script(b)==11145&&b.length==30){
                int target=be(b,6),width=be(b,22),height=be(b,18);
                if(target==hash(732))wrapper=width==512&&height==352;
                if(target==hash(735))host=width==512&&height==334;
            }
        }
        require(mounted&&wrapper&&host,"Make-X inherited forge dimensions or wrong wire mount");
        start=f.captured.size();f.nativeButton(37,42,-1,-1);WorldTasksManager.processTasks();f.drain();
        require(menu.isNativeOpen()&&f.player.getInterfaceManager().getInterfaceParentId(1370)==hash(735),"Stale forge Close or redraw retired the newer Make-X owner");
        assertForgeIsolation(f,start,false);
        Native950ProductionUiCatalog.Category category=Native950ProductionUiCatalog.byId(menu.currentCategoryId());int ordinal=-1;
        for(int i=0;i<category.displayIds.length;i++)if(category.displayIds[i]==50){ordinal=i;break;}
        require(ordinal>=0,"Missing normal shortbow product");f.nativeButton(1371,22,1+4*ordinal,50);f.nativeButton(1371,20,0,-1);f.response(1370,30);f.run(15);
        require(f.amount(50)==1&&f.amount(1511)==0,"Make-X stopped producing after forge's stale close");
    }
    private static void check(Native950ProductionThirdPassAcceptance.Fixture f){
        List<Native950Production.Recipe> smithing=Native950Smithing.recipes();require(smithing.size()==702,"Smithing recipe count");
        for(Native950Production.Recipe r:smithing){int product=r.produced()[0].getId();int[] path=Native950ForgeUi.pathForProduct(product,false);require(path!=null,"No native forge path: "+r.label);require(Native950ForgeUi.upgraded(path[3],path[4])==product,"Forge upgrade path changed product: "+r.label);}
        for(Native950Production.Recipe r:Native950Smelting.recipes())require(Native950ForgeUi.pathForProduct(r.produced()[0].getId(),true)!=null,"Missing furnace bar "+r.label);
        WorldObject anvil=f.station(11497);WorldTile anvilTile=new WorldTile(f.player);f.clear();f.level(Skills.SMITHING,99);
        require(Native950Smithing.choices(f.player,anvil).size()==702,"Missing-material recipes hidden from native forge");
        int forgePackets=f.captured.size();f.object(anvil,1);f.run(3);require(view(f).isOpen(),"Anvil click did not open interface37");assertForgeMount(f,forgePackets);
        require(f.varps.get(8334)==anvil.getId(),"Actual anvil ID was not sent to client station requirements");
        int redraws=Collections.frequency(f.scripts,2586);WorldTasksManager.processTasks();f.drain();
        require(Collections.frequency(f.scripts,2586)==redraws+1,"First-open deferred grid/details redraw missing");
        double xp=f.xp(Skills.SMITHING);f.nativeButton(37,163,-1,-1);f.run(5);require(view(f).isOpen()&&f.xp(Skills.SMITHING)==xp,"Empty forge start mutated or closed unexpectedly");
        f.nativeButton(37,42,-1,-1);require(!view(f).isOpen(),"Native forge Close failed");f.drain();require(f.varps.get(8334)==-1,"Close left a live station context");require(f.player.getInterfaceManager().getInterfaceParentId(37)==-1,"Closed forge retained its mount registration");assertForgeIsolation(f,forgePackets,true);
        f.object(anvil,1);f.run(3);f.nativeButton(37,42,-1,-1);f.drain();redraws=Collections.frequency(f.scripts,2586);
        WorldTasksManager.processTasks();f.drain();require(Collections.frequency(f.scripts,2586)==redraws,"Closed forge received a delayed redraw");
        Native950Production.Recipe dagger=recipe(smithing,1205);
        f.clear();materials(f,dagger,2);f.run(2);f.object(anvil,1);f.run(3);select(f,1205,false);
        require(!f.player.getInventory().containsItem(2347,1)&&Native950Toolbelt.has(f.player,2347),"Test must use toolbelt hammer");
        f.nativeButton(37,35,1,-1);require(view(f).selectedQuantity()==2,"Forge quantity slider failed");
        f.nativeButton(37,163,-1,-1);f.run(90);require(f.amount(1205)==2&&f.amount(2349)==0&&f.xp(Skills.SMITHING)>xp,"Encrypted forge production failed");
        f.nativeButton(37,163,-1,-1);f.run(5);require(f.amount(1205)==2,"Retired forge Start ran again");
        Native950Production.Recipe upgrade=recipe(smithing,45842);
        f.clear();materials(f,upgrade,1);f.run(2);f.object(anvil,1);f.run(3);select(f,45842,false);
        xp=f.xp(Skills.SMITHING);f.nativeButton(37,163,-1,-1);f.run(150);require(f.amount(45842)==1&&f.amount(1203)==0&&f.xp(Skills.SMITHING)>xp,"Native iron dagger upgrade failed");
        f.clear();materials(f,dagger,1);f.run(2);f.object(anvil,1);f.run(3);select(f,1205,false);
        int[] path=Native950ForgeUi.pathForProduct(1205,false);f.nativeButton(37,path[1],path[2],1601);require(view(f).selectedProduct()==1205,"Mismatched product claim changed selection");
        f.nativeButton(37,path[1],0,-1);require(view(f).selectedProduct()==1205,"Invalid actor selected product");
        f.player.setLocation(new WorldTile(f.player.getX()+6,f.player.getY()+6,f.player.getPlane()));World.updateEntityRegion(f.player);f.player.resetMasks();
        redraws=Collections.frequency(f.scripts,2586);WorldTasksManager.processTasks();f.drain();require(Collections.frequency(f.scripts,2586)==redraws,"Walking away allowed deferred redraw");
        f.nativeButton(37,163,-1,-1);f.run(10);require(!view(f).isOpen()&&f.amount(1205)==0&&f.amount(2349)==dagger.consumed()[0].getAmount(),"Moved player forged from stale interface");
        f.clear();WorldObject furnace=f.station(11010);WorldTile furnaceTile=new WorldTile(f.player);f.level(Skills.SMITHING,99);Native950Production.Recipe bronze=recipe(Native950Smelting.recipes(),2349);materials(f,bronze,2);f.run(2);f.object(furnace,1);f.run(3);require(view(f).isOpen(),"Furnace did not open native37");require(f.varps.get(8334)==furnace.getId(),"Furnace kept the old anvil station ID");
        f.nativeButton(37,42,-1,-1);f.player.setLocation(anvilTile);World.updateEntityRegion(f.player);f.player.resetMasks();f.object(anvil,1);f.run(3);f.drain();redraws=Collections.frequency(f.scripts,2586);
        WorldTasksManager.processTasks();f.drain();require(Collections.frequency(f.scripts,2586)==redraws+1&&f.varps.get(8331)==1489&&f.varps.get(8334)==anvil.getId(),"Furnace-to-anvil redraw used a retired opening");
        f.nativeButton(37,42,-1,-1);f.player.setLocation(furnaceTile);World.updateEntityRegion(f.player);f.player.resetMasks();f.object(furnace,1);f.run(3);select(f,2349,true);
        f.nativeButton(37,35,1,-1);f.nativeButton(37,163,-1,-1);f.run(20);require(f.amount(2349)==2&&f.amount(436)==0&&f.amount(438)==0,"Native furnace did not smelt selected quantity");
        f.clear();materials(f,bronze,1);f.run(2);f.object(furnace,1);f.run(3);select(f,2349,true);World.removeObject(furnace);
        f.drain();redraws=Collections.frequency(f.scripts,2586);WorldTasksManager.processTasks();f.drain();require(Collections.frequency(f.scripts,2586)==redraws,"Removed furnace received deferred redraw");
        f.nativeButton(37,163,-1,-1);f.run(10);require(f.amount(2349)==0&&f.amount(436)==1&&f.amount(438)==1,"Removed furnace produced material");
        assertForgeIsolation(f,forgePackets,true);checkSmallProductionAfterForge(f,anvil,anvilTile);
        System.out.println("PASS: all702 smithing and12 smelting recipes have real cache UI paths; "+f.output.size()+" encrypted response frames decoded");
    }
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}