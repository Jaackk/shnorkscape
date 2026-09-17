package com.rs.game.player.client;

import com.google.gson.Gson;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.WorldObject;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/** Native950 interface37. DB table7 owns the metal/product groups; existing actions own production. */
public final class Native950ForgeUi {
    // Native slot1047 is the large central window. Slot1007 (735/732) resets
    // its host to512x334 on drag completion, clipping this800x484 interface.
    private static final int ROOT=1477,HOST=726,WRAPPER=724,FACE=37;
    private static final int[] MATERIAL_GRIDS={52,62,72,82,92},PRODUCT_GRIDS={103,114,125,136,147};
    private static final int[] UPGRADE_BUTTONS={149,161,159,157,155,153,151},UPGRADES={0,1,2,3,4,5,50};
    private final Player player;private final Channel channel;
    private final Map<Integer,Native950ProductionMenu.Choice> choices=new LinkedHashMap<>();
    private boolean open,smelting;private int materialRow,base,upgrade,quantity=1,x,y,plane;
    private WorldObject station;private long generation;
    private static Object verified;
    private static final class Data {
        Map<Integer,List<com.google.gson.JsonObject>> rows;
        Map<Integer,Map<Integer,Integer>> enums;Map<String,String> pins;
    }
    private static final class Holder {
        static final Data DATA=load();
        private static Data load(){
            java.io.InputStream stream=Native950ForgeUi.class.getResourceAsStream("/native950/forge-ui-950.json");
            if(stream==null)throw new IllegalStateException("Missing950 forge UI metadata");
            try(InputStreamReader reader=new InputStreamReader(stream,StandardCharsets.UTF_8)){return new Gson().fromJson(reader,Data.class);}
            catch(java.io.IOException e){throw new IllegalStateException(e);}
        }
    }
    public Native950ForgeUi(Player player,Channel channel){this.player=player;this.channel=channel;}
    public boolean isOpen(){return open;}
    public static synchronized void verifyCacheBindings(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Forge UI requires the paired950 cache");
        if(verified==Cache.STORE)return;
        for(Map.Entry<String,String> pin:Holder.DATA.pins.entrySet())try{
            String[] ids=pin.getKey().split("\\.");byte[] bytes=Cache.STORE.getIndexes()[Integer.parseInt(ids[0])].getFile(Integer.parseInt(ids[1]),Integer.parseInt(ids[2]));
            if(bytes==null)throw new IllegalStateException("Missing forge asset "+pin.getKey());
            StringBuilder sha=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(bytes))sha.append(String.format("%02x",b&255));
            if(!pin.getValue().equals(sha.toString()))throw new IllegalStateException("Forge asset changed "+pin.getKey());
        }catch(Exception e){throw new IllegalStateException("Unverified950 forge UI",e);}
        verified=Cache.STORE;
    }
    private static int enumValue(int id,int key){Map<Integer,Integer> values=Holder.DATA.enums.get(id);return values==null?-1:values.getOrDefault(key,-1);}
    private static int category(int row,int index){List<com.google.gson.JsonObject> groups=Holder.DATA.rows.get(row);return groups==null||index<0||index>=groups.size()?-1:groups.get(index).get("enum").getAsInt();}
    private static int baseOf(int id){ItemDefinitions d=Native950CacheItems.definition(id);int base=d==null?id:d.getCSOpcode(7806,id);return base>0?base:id;}
    public static int upgraded(int id,int level){
        ItemDefinitions d=Native950CacheItems.definition(id);if(d==null)return -1;
        id=baseOf(id);if(level==0)return id;
        d=Native950CacheItems.definition(id);if(d==null)return -1;
        int maximum=d.getCSOpcode(7805,0),steps=level==50?maximum+1:level;
        if(steps<1||steps>6||level!=50&&level>maximum)return -1;
        for(int i=0;i<steps;i++){d=Native950CacheItems.definition(id);id=d==null?-1:d.getCSOpcode(7807,-1);if(id<1)return -1;}
        d=Native950CacheItems.definition(id);
        return d!=null&&(level==50?d.getCSOpcode(7803,0)==1:d.getCSOpcode(7804,0)==level)?id:-1;
    }
    /** {material row, product grid, child index, base item, upgrade}; read-only acceptance surface. */
    public static int[] pathForProduct(int product,boolean smelt){
        int root=smelt?1482:1489,target=baseOf(product);
        for(int group=0;group<5;group++){
            Map<Integer,Integer> materials=Holder.DATA.enums.get(category(root,group));if(materials==null)continue;
            for(int mat:materials.values()){
                int row=enumValue(smelt?2530:2531,mat);
                for(int g=0;g<5;g++){
                    Map<Integer,Integer> products=Holder.DATA.enums.get(category(row,g));if(products==null)continue;
                    for(Map.Entry<Integer,Integer> entry:products.entrySet())if(baseOf(entry.getValue())==target){
                        ItemDefinitions d=Native950CacheItems.definition(product);int up=d==null?0:d.getCSOpcode(7803,0)==1?50:d.getCSOpcode(7804,0);
                        return new int[]{row,PRODUCT_GRIDS[g],2*entry.getKey()+1,target,up};
                    }
                }
            }
        }return null;
    }
    public static int[] materialPath(int row,boolean smelt){
        for(int g=0;g<5;g++){Map<Integer,Integer> materials=Holder.DATA.enums.get(category(smelt?1482:1489,g));if(materials==null)continue;
            for(Map.Entry<Integer,Integer> e:materials.entrySet())if(enumValue(smelt?2530:2531,e.getValue())==row)return new int[]{MATERIAL_GRIDS[g],2*e.getKey()+1,e.getValue()};
        }return null;
    }
    public int currentMaterialRow(){return materialRow;}
    public int selectedProduct(){return upgraded(base,upgrade);}
    public int selectedQuantity(){return quantity;}
    public int maximumQuantity(){return maximum();}
    public boolean open(List<Native950ProductionMenu.Choice> offered,boolean smelt,WorldObject source){
        if(source==null||!(smelt?Native950Smelting.inReach(player,source):Native950Smithing.inReach(player,source)))return false;
        if(offered==null||offered.isEmpty()){player.sendMessage("No production recipes are available at this station.");return false;}
        verifyCacheBindings();close();smelting=smelt;station=new WorldObject(source);
        for(Native950ProductionMenu.Choice c:offered){Native950Production.Recipe r=c.recipe();if(r!=null&&r.produced().length>0)choices.putIfAbsent(r.produced()[0].getId(),c);}
        int[] chosen=null;
        for(Map.Entry<Integer,Native950ProductionMenu.Choice> e:choices.entrySet()){
            int[] path=pathForProduct(e.getKey(),smelt);if(path==null)continue;
            if(chosen==null)chosen=path;if(e.getValue().recipe().refusal(player)==null){chosen=path;break;}
        }
        if(chosen==null){choices.clear();player.sendMessage("Those recipes do not belong to this station's native production screen.");return false;}
        materialRow=chosen[0];base=chosen[3];upgrade=chosen[4];quantity=1;
        x=player.getX();y=player.getY();plane=player.getPlane();open=true;
        writeState();
        channel.write(Native950Packets.openSub(ROOT,HOST,FACE,true));
        player.getInterfaceManager().registerNativeOpen(FACE,ROOT,HOST);
        channel.write(Native950Packets.hideInterface(ROOT,WRAPPER,false));
        channel.write(Native950Packets.runClientScript(2600,smelt?1:0));
        channel.write(Native950Packets.runClientScript(2586,smelt?1482:1489,materialRow));
        for(int grid:MATERIAL_GRIDS)channel.write(Native950Packets.interfaceEvents(FACE,grid,0,255,2));
        for(int grid:PRODUCT_GRIDS)channel.write(Native950Packets.interfaceEvents(FACE,grid,0,511,2));
        for(int control:UPGRADE_BUTTONS)channel.write(Native950Packets.interfaceEvents(FACE,control,-1,-1,2));
        for(int control:new int[]{42,163})channel.write(Native950Packets.interfaceEvents(FACE,control,-1,-1,2));
        channel.write(Native950Packets.interfaceEvents(FACE,35,0,9999,2));
        // Metal-bank storage is a separate feature. The existing actions consume backpack ingredients.
        channel.write(Native950Packets.hideInterface(FACE,21,true));channel.write(Native950Packets.hideInterface(FACE,22,true));
        refresh();channel.write(Native950Packets.runClientScript(1364));
        // Server varps reach the client before its next UI cycle. On the first open,
        // CS2591 can still see root8331=-1; switching furnace/anvil can see the old root.
        // Redraw both grids and details once after that cycle, only for this live opening.
        final long opening=++generation;
        WorldTasksManager.schedule(new WorldTask(){@Override public void run(){
            if(opening!=generation||!open||!channel.isActive()||!valid()
                    ||!player.getInterfaceManager().containsInterface(FACE))return;
            channel.write(Native950Packets.runClientScript(2586,smelting?1482:1489,materialRow));
            refresh();
        }});
        player.sendMessage("Production uses materials in your backpack.");return true;
    }
    private boolean valid(){return player.isActive()&&!player.hasFinished()&&!player.isDead()&&!player.isLocked()&&!player.closeInterfaceLocked&&!player.isNative950ForceMovementActive()&&player.getNextWorldTile()==null&&player.getX()==x&&player.getY()==y&&player.getPlane()==plane&&station!=null&&(smelting?Native950Smelting.inReach(player,station):Native950Smithing.inReach(player,station));}
    private Native950ProductionMenu.Choice selected(){return choices.get(upgraded(base,upgrade));}
    private int maximum(){Native950ProductionMenu.Choice c=selected();return c==null?0:Math.min(10000,c.recipe().maximumByMaterials(player));}
    private void writeState(){
        // CS7163/7164 check object param7803 on varp8334 for normal/burial/other station requirements.
        channel.write(Native950Packets.varp(8334,station.getId()));
        channel.write(Native950Packets.varp(8331,smelting?1482:1489));channel.write(Native950Packets.varp(8332,materialRow));channel.write(Native950Packets.varp(8333,base));
        channel.write(Native950Packets.varbitLarge(43239,upgrade));channel.write(Native950Packets.varp(8336,quantity));
    }
    private void refresh(){
        quantity=Math.max(1,Math.min(quantity,Math.max(1,maximum())));writeState();
        channel.write(Native950Packets.runClientScript(2589,materialRow,base,upgrade));
        channel.write(Native950Packets.runClientScript(10085,(FACE<<16)|34,(FACE<<16)|35,40049,1,Math.max(1,maximum()),quantity));
    }
    public boolean handle(Native950Actions.InterfaceAction a){
        if(a.interfaceId()!=FACE)return false;
        if(!open)return true;
        if(!valid()){close();return true;}
        if(a.option()!=1)return true;
        int component=a.componentId();
        if(component==42&&a.slot()==-1){close();return true;}
        if(component==35&&a.itemId()==-1&&a.slot()>=0&&a.slot()<maximum()){quantity=a.slot()+1;refresh();return true;}
        for(int i=0;i<UPGRADE_BUTTONS.length;i++)if(component==UPGRADE_BUTTONS[i]&&a.slot()==-1&&a.itemId()==-1){
            if(!smelting&&upgraded(base,UPGRADES[i])>0){upgrade=UPGRADES[i];quantity=1;refresh();}return true;
        }
        for(int i=0;i<MATERIAL_GRIDS.length;i++)if(component==MATERIAL_GRIDS[i]){
            int item=gridItem(smelting?1482:1489,i,a.slot());if(item<1||a.itemId()!=-1&&a.itemId()!=item)return true;
            int row=enumValue(smelting?2530:2531,item);if(!Holder.DATA.rows.containsKey(row))return true;
            int first=enumValue(category(row,0),0);if(first<1)return true;
            materialRow=row;base=baseOf(first);upgrade=0;quantity=1;refresh();return true;
        }
        for(int i=0;i<PRODUCT_GRIDS.length;i++)if(component==PRODUCT_GRIDS[i]){
            int item=gridItem(materialRow,i,a.slot());if(item<1)return true;int drawn=upgraded(item,upgrade);
            if(a.itemId()!=-1&&a.itemId()!=item&&a.itemId()!=drawn)return true;
            base=baseOf(item);if(upgraded(base,upgrade)<1)upgrade=0;quantity=1;refresh();return true;
        }
        if(component==163&&a.slot()==-1&&a.itemId()==-1){
            Native950ProductionMenu.Choice choice=selected();
            if(choice==null){player.sendMessage("That recipe is not available at this station yet.");return true;}
            String refusal=choice.recipe().refusal(player);if(refusal!=null){player.sendMessage(refusal);refresh();return true;}
            int amount=Math.min(quantity,maximum());if(amount<1)return true;close();choice.start(amount);return true;
        }
        return true;
    }
    private static int gridItem(int row,int category,int slot){return slot<1||(slot&1)==0?-1:enumValue(category(row,category),(slot-1)/2);}
    public void close(){
        generation++;choices.clear();station=null;if(!open)return;open=false;
        channel.write(Native950Packets.varp(8334,-1));
        channel.write(Native950Packets.runClientScript(8841,85,0));channel.write(Native950Packets.closeSub(ROOT,HOST));
        player.getInterfaceManager().unregisterNativeOpen(FACE);channel.write(Native950Packets.hideInterface(ROOT,WRAPPER,true));
        channel.write(Native950Packets.runClientScript(1364));
    }
}