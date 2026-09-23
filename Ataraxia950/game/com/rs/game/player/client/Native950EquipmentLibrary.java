package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.util.*;

/** Native517 with independent catalogue/preset ownership; explicit loadout application banks displaced property atomically. */
final class Native950EquipmentLibrary {
    private static final Map<Player,Native950EquipmentLibrary> OWNERS=new IdentityHashMap<>();
    private final Player player;
    private final Channel channel;
    private final Runnable beforeOpen,verifier;
    private final java.util.function.Supplier<Native950EquipmentCatalogue> catalogueSource;
    private final Native950QuantityInput quantity;
    private Native950EquipmentCatalogue catalogue;
    private Native950DeveloperSearch.Session search;
    private String searchText="";
    private final Native950DeveloperLoadouts loadouts=new Native950DeveloperLoadouts();
    private boolean presetView;
    private int selectedPreset=1;
    private boolean open,prompt;
    private int mode=2,savedX=1,pendingSlot=-1,selectedTab=2;
    private long epoch;
    private final Set<Integer> grantedThisTick=new HashSet<>();

    Native950EquipmentLibrary(Player p,Channel c,Runnable beforeOpen){
        this(p,c,beforeOpen,Native950EquipmentLibraryAssets::verify,Native950EquipmentCatalogue::current,new Native950QuantityInput());
    }
    Native950EquipmentLibrary(Player p,Channel c,Runnable beforeOpen,Runnable verifier,
                              java.util.function.Supplier<Native950EquipmentCatalogue> source,Native950QuantityInput input){
        player=p;channel=c;this.beforeOpen=beforeOpen;this.verifier=verifier;catalogueSource=source;quantity=input;
        synchronized(OWNERS){OWNERS.put(p,this);}
    }
    static void open(Player p){
        Native950EquipmentLibrary owner;synchronized(OWNERS){owner=OWNERS.get(p);}
        if(owner!=null)owner.open();else p.sendMessage("The equipment library is not attached to this session.");
    }
    boolean isOpen(){return open;}
    void tick(){grantedThisTick.clear();}
    private boolean allowed(){return player.isActive()&&!player.hasFinished()&&!player.isDead()&&!player.isLocked()
            &&Native950AdminCommands.authorized(player)&&Native950DevelopmentCommands.allowed(player,channel);}
    private void open(){
        if(!allowed())return;
        verifier.run();Native950EquipmentCatalogue next=catalogueSource.get();
        beforeOpen.run();catalogue=next;search=new Native950DeveloperSearch.Session(next);searchText="";presetView=false;selectedPreset=1;epoch++;open=true;selectedTab=2;mode=2;savedX=1;
        tabState(true);publish();preferences();
        channel.write(Native950Packets.openSub(1477,693,517,false));
        player.getInterfaceManager().registerNativeOpen(517,1477,693);
        channel.write(Native950Packets.hideInterface(1477,693,false));
        for(Native950Packets.Packet packet:Native950BankUi.openControls())channel.write(packet);
        channel.write(Native950Packets.interfaceEvents(517,201,0,catalogue.entries.size()-1,Native950BankUi.ITEM_EVENTS));
        channel.write(Native950Packets.interfaceEvents(517,169,2,9,2));
        // All real-bank mutations and optional destinations remain inaccessible.
        for(int component:new int[]{15,35,39,42,45,48,51,60,64,69,74,77,80,123,127,131,134,140,143,146,149,294,295,296,297,298,299,302,306})
            channel.write(Native950Packets.interfaceEvents(517,component,component==15||component==35?0:-1,component==15?27:component==35?18:-1,0));
        for(int component:new int[]{38,41,44,47,50,58,62,122,126,130,133,139,142,145,148})
            channel.write(Native950Packets.hideInterface(517,component,true));
        channel.write(Native950Packets.runClientScript(13898));
        channel.write(Native950Packets.runClientScript(9511,0));
        channel.write(Native950Packets.interfaceText(517,73,"Developer loadout:"));
        channel.write(Native950Packets.interfaceEvents(517,68,-1,-1,0x3fe));
        channel.write(Native950Packets.interfaceEvents(517,267,1,12,6));
        for(int component:new int[]{84,86,303})channel.write(Native950Packets.interfaceEvents(517,component,-1,-1,6));
        title();
    }
    private void title(){
        // Exact517:0 onLoad8420 arguments; only the native title string differs.
        channel.write(Native950Packets.runClientScript(8420,(517<<16)|1,(517<<16)|311,-1,(517<<16)|317,
                "DEVELOPER EQUIPMENT LIBRARY",28241,1017));
    }
    private void tabState(boolean library){
        for(int i=0;i<14;i++){
            channel.write(Native950Packets.varbitSmall(45143+i,library&&i<catalogue.counts.length?catalogue.counts[i]:0));
            channel.write(Native950Packets.varbitSmall(45161+i,0));
            // Authored bank icon selector enum15585; visually audited with Native950LibraryIcons.
            int[] icons={5,1,4,13,33,3,20,29};
            channel.write(Native950Packets.varbitSmall(45193+i,library&&i<icons.length?icons[i]:0));
            // Native enum8657 supports style names, Hybrid, Food and Questing.
            int[] labels={154,1,5,9,13,159,160,115};
            channel.write(Native950Packets.varbitSmall(45207+i,library&&i<labels.length?labels[i]:0));
        }
        channel.write(Native950Packets.varbitSmall(45141,library?selectedTab:1));
        channel.write(Native950Packets.varbitSmall(45158,1));
    }
    private void preferences(){for(Native950Packets.Packet p:Native950BankUi.sync(mode,savedX,false))channel.write(p);}
    private void publish(){
        int size=search.searching?search.slots.size():catalogue.entries.size();
        int[] ids=new int[size],amounts=new int[size];Arrays.fill(ids,-1);
        for(int i=0;i<ids.length;i++){
            Native950EquipmentCatalogue.Entry entry=search.claim(i,catalogue.entries.size());
            if(entry!=null){ids[i]=entry.id;amounts[i]=entry.quantity;}
        }
        channel.write(Native950Packets.varp(8970,-1));channel.write(Native950Packets.varp(8971,ids.length));
        channel.write(Native950Packets.interfaceEvents(517,201,0,ids.length-1,Native950BankUi.ITEM_EVENTS));
        channel.write(Native950Packets.inventoryFull(95,false,ids,amounts));
        Native950Containers.Snapshot inventory=Native950Skilling.containers(player).inventorySnapshot();
        channel.write(Native950Packets.inventoryFull(93,false,inventory.ids,inventory.amounts));
        if(search.searching){
            String filter=searchText;
            if(Native950DeveloperSearch.exactId(searchText)>=0){
                filter=search.visible.isEmpty()?"__no_developer_match__":search.slots.get(search.visible.iterator().next()).name;
                // Native saved-filter text is separate from the editable input text; keep the user's ID visible.
                channel.write(Native950Packets.varcString(6711,filter));
            }
            channel.write(Native950Packets.runClientScript(9325,filter,1));
        }
    }
    boolean handle(Native950Actions.StringDialogueAction action){
        String text=action.text();
        if(text==null||!text.startsWith("__devlib:"))return false;
        if(!open||!allowed()||action.isNameDialogue())return true;
        String command=text.substring(9);
        if(command.equals("cancel")){endSearch();return true;}
        if(command.equals("begin")){
            cancelInput();epoch++;searchText="";search.query(Collections.emptyList());publish();return true;
        }
        if(!command.startsWith("q:")||!search.searching)return true;
        String query=command.substring(2);if(query.length()>80||query.equals(searchText))return true;
        cancelInput();epoch++;searchText=query;
        search.query(Native950DeveloperSearch.find(Native950DeveloperSearch.current(),query));
        publish();
        if(search.limited)player.sendMessage("Showing the first 100 matches. Refine the native search to find a specific item.");
        if(search.full)player.sendMessage("This search session has filled its safe source slots. Close and reopen ;;items to search more items.");
        return true;
    }
    private void endSearch(){
        if(!search.searching)return;
        cancelInput();epoch++;search.searching=false;searchText="";publish();
        channel.write(Native950Packets.varbitSmall(45141,selectedTab));
        channel.write(Native950Packets.runClientScript(9324,1));title();
    }
    private static int previewContainer(int preset){return preset<=10?704+preset*2:896+(preset-11)*2;}
    private void showPreset(int preset){
        if(preset<1||preset>12)return;
        cancelInput();endSearch();presetView=true;selectedPreset=preset;
        channel.write(Native950Packets.varbitSmall(45191,1));
        channel.write(Native950Packets.varbitSmall(39433,1));
        channel.write(Native950Packets.varbitSmall(45226,1));
        channel.write(Native950Packets.varbitSmall(22179,preset));
        Native950DeveloperLoadouts.Loadout loadout=loadouts.get(preset-1);
        int container=previewContainer(preset);
        preview(container,loadout==null?new com.rs.game.item.Item[28]:loadout.inventory);
        preview(container+1,loadout==null?new com.rs.game.item.Item[19]:loadout.equipment);
        channel.write(Native950Packets.runClientScript(13943));
        for(int component:new int[]{74,77,80,294,295,296,297,298,299,302,306})channel.write(Native950Packets.hideInterface(517,component,true));
        title();
    }
    private void preview(int container,com.rs.game.item.Item[] items){
        int[] ids=new int[items.length],amounts=new int[items.length];Arrays.fill(ids,-1);
        for(int i=0;i<items.length;i++)if(items[i]!=null){ids[i]=items[i].getId();amounts[i]=items[i].getAmount();}
        channel.write(Native950Packets.inventoryFull(container,false,ids,amounts));
    }
    private void leavePresets(){
        presetView=false;
        channel.write(Native950Packets.varbitSmall(39433,0));channel.write(Native950Packets.varbitSmall(45226,0));
        channel.write(Native950Packets.varbitSmall(45191,0));channel.write(Native950Packets.runClientScript(13943));
    }
    boolean handle(Native950Actions.InterfaceAction action){
        if(!open||action.interfaceId()!=517)return false;
        if(!allowed()){close();return true;}
        int component=action.componentId();
        if(component==317){close();return true;}
        if(component==153&&action.option()==1){
            cancelInput();endSearch();presetView=true;
            channel.write(Native950Packets.varbitSmall(45191,1));channel.write(Native950Packets.runClientScript(13746,1));
            return true;
        }
        if(component==152||component==86){leavePresets();publish();title();return true;}
        if(component==Native950BankUi.SORT_TAB){player.sendMessage("Library tabs are already ordered by tier and equipment set.");return true;}
        if(component==68&&action.option()>=1&&action.option()<=9){showPreset(action.option());return true;}
        if(component==267&&presetView&&(action.option()==1||action.option()==2)){
            if(action.slot()<1||action.slot()>12)return true;
            showPreset(action.slot());
            if(action.option()==2&&!grantedThisTick.contains(-1)){
                grantedThisTick.add(-1);player.sendMessage(Native950DeveloperLoadouts.apply(player,loadouts.get(selectedPreset-1)));publish();showPreset(selectedPreset);
            }
            return true;
        }
        if(component==84&&presetView&&(action.option()==1||action.option()==2)){
            if(grantedThisTick.contains(-1))return true;grantedThisTick.add(-1);
            player.sendMessage(Native950DeveloperLoadouts.apply(player,loadouts.get(selectedPreset-1)));publish();showPreset(selectedPreset);return true;
        }
        if(component==303&&presetView&&(action.option()==1||action.option()==2)){
            if(selectedPreset<=9){player.sendMessage("Built-in developer loadouts cannot be overwritten. Select Developer Custom 1-3.");return true;}
            try{loadouts.capture(selectedPreset-1,player);player.sendMessage("Developer custom loadout saved for this login session. Real bank presets are unchanged.");showPreset(selectedPreset);}
            catch(IllegalArgumentException|IllegalStateException invalid){player.sendMessage(invalid.getMessage());}
            return true;
        }
        if(Native950BankUi.isLocalSearchControl(component)){
            if(presetView)leavePresets();
            if(component!=237){endSearch();title();}
            return true; // Client owns text and original-slot filtering.
        }
        if(component==165||component==169){
            cancelInput();endSearch();
            if(action.option()==1&&(component==165||action.slot()>=2&&action.slot()<=9)) {
                selectedTab=component==165?1:action.slot();channel.write(Native950Packets.varbitSmall(45141,selectedTab));
                title();
            }
            return true;
        }
        int chosen=Native950BankUi.quantityMode(component);
        if(chosen!=0&&action.option()==1){
            if(component==114){beginQuantity(-1);return true;}
            cancelInput();mode=chosen;preferences();return true;
        }
        if(component!=201){
            for(Native950Packets.Packet packet:Native950BankUi.resetUnsupportedModes())channel.write(packet);
            preferences();publish();return true;
        }
        int slot=action.slot();
        Native950EquipmentCatalogue.Entry entry=search.claim(slot,catalogue.entries.size());
        if(entry==null||grantedThisTick.contains(slot))return true;
        int option=action.option();
        if(option==10){if(action.itemId()==entry.id)player.sendMessage(entry.name+" (ID "+entry.id+")");return true;}
        if(option==6){if(action.itemId()==entry.id)beginQuantity(slot);return true;}
        int requested=amount(option,entry.quantity);
        if(requested<=0)return true;
        int movable=room(entry,requested);
        int actor=movable>0&&(movable>=entry.quantity||entry.stackMode==2)?48447:entry.id;
        if(action.itemId()!=actor){publish();return true;}
        grant(slot,movable);
        return true;
    }
    private int amount(int option,int stock){
        if(option==1)option=mode;
        switch(option){case 2:return 1;case 3:return 5;case 4:return 10;case 5:return savedX;case 7:return stock;default:return 0;}
    }
    private int room(Native950EquipmentCatalogue.Entry entry,int requested){
        Native950Containers.Snapshot inv=Native950Skilling.containers(player).inventorySnapshot();
        long capacity=0;
        for(int i=0;i<inv.ids.length;i++){
            if(entry.stackMode==1&&inv.ids[i]==entry.id)return (int)Math.min(requested,(long)Integer.MAX_VALUE-inv.amounts[i]);
            if(inv.ids[i]<0)capacity+=entry.stackMode==1?Integer.MAX_VALUE:1;
        }
        return (int)Math.min(requested,capacity);
    }
    private void grant(int slot,int amount){
        Native950EquipmentCatalogue.Entry entry=search.claim(slot,catalogue.entries.size());
        if(entry!=null&&amount>0&&Native950Skilling.giveItem(player,entry.id,amount))grantedThisTick.add(slot);
        else player.sendMessage("There is no room for that item, or your current activity refused it.");
        publish();
    }
    private void beginQuantity(int slot){
        if(prompt)return;
        Native950Containers.Snapshot inv=Native950Skilling.containers(player).inventorySnapshot();
        if(!quantity.beginDefault(epoch,inv))return;
        pendingSlot=slot;prompt=true;
        for(Native950Packets.Packet p:quantity.promptPackets(slot<0?"Set the library quantity:":"How many would you like to withdraw?"))channel.write(p);
        player.getInterfaceManager().registerNativeOpen(1418,1477,749);
        player.getInterfaceManager().registerNativeOpen(1469,1418,2);
    }
    boolean handle(Native950Actions.CountDialogueAction action){
        if(!open||!prompt)return false;
        int slot=pendingSlot;
        Native950QuantityInput.Accepted accepted=quantity.consume(action.count(),epoch,Native950Skilling.containers(player).inventorySnapshot(),(s,a)->a);
        cancelInput();
        if(accepted==null||!allowed())return true;
        savedX=accepted.amount;
        if(slot<0){mode=5;preferences();}
        else {Native950EquipmentCatalogue.Entry entry=search.claim(slot,catalogue.entries.size());if(entry!=null){preferences();grant(slot,room(entry,savedX));}}
        return true;
    }
    boolean cancelInput(){
        if(!prompt)return false;
        quantity.cancel();prompt=false;pendingSlot=-1;
        for(Native950Packets.Packet p:quantity.cancelPackets())channel.write(p);
        player.getInterfaceManager().unregisterNativeOpen(1469);player.getInterfaceManager().unregisterNativeOpen(1418);
        return true;
    }
    void close(){
        if(!open)return;
        cancelInput();open=false;epoch++;grantedThisTick.clear();
        channel.write(Native950Packets.interfaceText(517,73,"Load Preset:"));
        Native950BankUi.closeInput(player,channel); // Includes search cleanup13909 and releases bank context24.
        channel.write(Native950Packets.closeSub(1477,693));channel.write(Native950Packets.hideInterface(1477,693,true));
        player.getInterfaceManager().unregisterNativeOpen(517);
        tabState(false);
        for(int id:new int[]{45191,39433,45226,22179})channel.write(Native950Packets.varbitSmall(id,0));
        for(int preset=1;preset<=12;preset++){
            int container=previewContainer(preset);preview(container,new com.rs.game.item.Item[0]);preview(container+1,new com.rs.game.item.Item[0]);
        }
        // Remove catalogue data from the client too. Ordinary banking republishes real95.
        channel.write(Native950Packets.varp(8970,-1));channel.write(Native950Packets.varp(8971,0));
        channel.write(Native950Packets.inventoryFull(95,false,new int[0],new int[0]));
        catalogue=null;search=null;
    }
    void dispose(){close();synchronized(OWNERS){if(OWNERS.get(player)==this)OWNERS.remove(player);}}
}
