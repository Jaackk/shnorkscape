package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Session-owned developer item catalogue. Interface 1265 is an icon surface, never a real Shop. */
final class Native950ItemBrowser {
    private static final int ROOT=1477, MAIN_HOST=735, MAIN_WRAPPER=732, SHOP=1265, SHOP_ITEMS=20;
    private static final int SEARCH_TEXT=44, SEARCH_CONTROL=41, RECENT_TEXT=35, RECENT_CONTROL=32;
    private static final int VIEW_LABEL=52, SELECTION_LABEL=58;
    private static final int TRANSACTION_LABEL=66, OWNED_LABEL=128, PRICE_LABEL=133, PRICE_VALUE=137, COIN_ICON=136;
    private static final int INPUT_FRAME_HOST=749, INPUT_FRAME=1418, INPUT_HOST=2, INPUT=1469;
    private static final int RESULTS_CONTAINER=139, RECENT_LIMIT=12, MAX_RENDERED_RESULTS=40;
    private static final int SHOP_OPTION_MASK=2097406;
    private static final Map<Player,Native950ItemBrowser> OWNERS=new IdentityHashMap<Player,Native950ItemBrowser>();
    private enum Phase { CLOSED, RESULTS, SEARCH, CUSTOM }
    private enum View { TESTING_KIT, SEARCH, RECENT }

    private final Player player;
    private final Channel channel;
    private final Runnable verifier;
    private final Native950QuantityInput countInput;
    private final Deque<Native950ContentCommands.ItemSearchEntry> recent=new ArrayDeque<Native950ContentCommands.ItemSearchEntry>();
    private Phase phase=Phase.CLOSED;
    private View view=View.TESTING_KIT;
    private boolean shopOpen;
    private String query="";
    private List<Native950ContentCommands.ItemSearchEntry> results=Collections.emptyList();
    /** Exact immutable sequence most recently published to container 139. */
    private List<Native950ContentCommands.ItemSearchEntry> rendered=Collections.emptyList();
    private Native950ContentCommands.ItemSearchEntry selected;

    Native950ItemBrowser(Player player,Channel channel,Native950Dialogues dialogues){
        this(player,channel,dialogues,Native950ItemBrowser::verifyCacheBindings,new Native950QuantityInput());
    }

    Native950ItemBrowser(Player player,Channel channel,Native950Dialogues dialogues,Runnable verifier){
        this(player,channel,dialogues,verifier,new Native950QuantityInput());
    }

    Native950ItemBrowser(Player player,Channel channel,Native950Dialogues dialogues,Runnable verifier,
                         Native950QuantityInput countInput){
        this.player=player;this.channel=channel;this.verifier=verifier;this.countInput=countInput;
        synchronized(OWNERS){OWNERS.put(player,this);}
    }

    static void open(Player player){
        Native950ItemBrowser browser;
        synchronized(OWNERS){browser=OWNERS.get(player);}
        if(browser==null){player.sendMessage("The native item browser is not attached to this session yet.");return;}
        browser.openBrowser();
    }

    boolean isOpen(){return phase!=Phase.CLOSED;}

    static void verifyCacheBindings(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Item Browser requires the paired revision-950 cache");
        Native950QuantityInput.verify();
        if(Cache.STORE.getIndexes()[3].getFile(SHOP,0)==null||Cache.STORE.getIndexes()[12].getFile(110,0)==null
                ||Cache.STORE.getIndexes()[12].getFile(8420,0)==null)
            throw new IllegalStateException("The paired cache does not contain the native Item Browser surfaces");
    }

    private void openBrowser(){
        verifier.run();closeInput();
        view=View.TESTING_KIT;query="";selected=null;
        results=Native950ContentCommands.testingKitItemBrowserEntries();phase=Phase.RESULTS;
        render();
        player.sendMessage("Developer Item Browser opened. Left-click gives 1; Search and Recent stay inside this window.");
    }

    private void searchPrompt(){
        closeShop();selected=null;phase=Phase.SEARCH;
        telemetry("search-opened","view",viewName(),"displayed",rendered.size());
        channel.write(Native950Packets.openSub(ROOT,INPUT_FRAME_HOST,INPUT_FRAME,true));
        channel.write(Native950Packets.openSub(INPUT_FRAME,INPUT_HOST,INPUT,true));
        player.getInterfaceManager().registerNativeOpen(INPUT_FRAME,ROOT,INPUT_FRAME_HOST);
        player.getInterfaceManager().registerNativeOpen(INPUT,INPUT_FRAME,INPUT_HOST);
        channel.write(Native950Packets.hideInterface(ROOT,747,false));
        channel.write(Native950Packets.runClientScript(110,"Search item name or exact ID:"));
    }

    boolean handle(Native950Actions.StringDialogueAction action){
        if(phase!=Phase.SEARCH)return false;
        closeTextInput();phase=Phase.RESULTS;
        String value=action.text()==null?"":action.text().trim();
        if(value.length()<1||value.length()>80){
            player.sendMessage("Enter an item name or exact numeric ID (1-80 characters).");render();return true;
        }
        List<Native950ContentCommands.ItemSearchEntry> matches=Native950ContentCommands.itemMatches(value,MAX_RENDERED_RESULTS+1);
        if(matches.isEmpty()){
            player.sendMessage("No revision-950 items matched '"+value+"'.");render();return true;
        }
        boolean truncated=matches.size()>MAX_RENDERED_RESULTS;
        if(truncated)matches=Collections.unmodifiableList(new ArrayList<Native950ContentCommands.ItemSearchEntry>(
                matches.subList(0,MAX_RENDERED_RESULTS)));
        view=View.SEARCH;query=value;results=matches;selected=null;
        telemetry("search-results","query",value,"results",results.size(),"truncated",truncated);
        player.sendMessage(truncated
                ?"Item Browser is showing the "+MAX_RENDERED_RESULTS+" best matches for '"+value+"'. Refine the search for more specific results."
                :"Item Browser found "+results.size()+" result(s) for '"+value+"'. Left-click an icon to give 1.");
        render();return true;
    }

    /**
     * Rebuild the same native surface used by V1. Remounting is intentional: changing container
     * 139 under an existing shop left rendered icons and clickable stock out of step in Recent.
     */
    private void render(){
        if(results.isEmpty()){
            view=View.TESTING_KIT;query="";results=Native950ContentCommands.testingKitItemBrowserEntries();
        }
        closeShop();
        rendered=Collections.unmodifiableList(new ArrayList<Native950ContentCommands.ItemSearchEntry>(results));
        int[] ids=new int[rendered.size()],amounts=new int[rendered.size()];
        for(int i=0;i<rendered.size();i++){ids[i]=rendered.get(i).id;amounts[i]=1;}
        channel.write(Native950Packets.varp(304,RESULTS_CONTAINER));
        channel.write(Native950Packets.varp(305,-1));
        // V1 used normal shop currency identity. -1 switched the cache-authored menu into Sell mode.
        channel.write(Native950Packets.varp(306,995));
        channel.write(Native950Packets.varcString(2360,"DEVELOPER ITEM BROWSER"));
        channel.write(Native950Packets.inventoryFull(RESULTS_CONTAINER,false,ids,amounts));
        channel.write(Native950Packets.openSub(ROOT,MAIN_HOST,SHOP,false));
        player.getInterfaceManager().registerNativeOpen(SHOP,ROOT,MAIN_HOST);
        channel.write(Native950Packets.hideInterface(ROOT,MAIN_WRAPPER,false));shopOpen=true;
        channel.write(Native950Packets.interfaceEvents(SHOP,SHOP_ITEMS,0,Math.max(0,ids.length-1),SHOP_OPTION_MASK));
        for(int control:new int[]{SEARCH_CONTROL,RECENT_CONTROL})
            channel.write(Native950Packets.interfaceEvents(SHOP,control,-1,-1,2));
        channel.write(Native950Packets.runClientScript(8420,82903048,82903256,82903049,82903257,
                "DEVELOPER ITEM BROWSER",21218,1007));
        channel.write(Native950Packets.runClientScript(1364));
        decorate();
        telemetry("rendered","view",viewName(),"query",query,"count",rendered.size(),"ids",ids(rendered));
    }

    private void decorate(){
        text(SEARCH_TEXT,"SEARCH ITEMS");text(RECENT_TEXT,"RECENT");
        text(VIEW_LABEL,viewName()+" - "+rendered.size()+" ITEM"+(rendered.size()==1?"":"S"));
        text(TRANSACTION_LABEL,"Give:");text(OWNED_LABEL,"");text(PRICE_LABEL,"Item ID:");
        text(PRICE_VALUE,selected==null?"-":String.valueOf(selected.id));
        text(SELECTION_LABEL,selected==null?"Left-click an item to give 1":selected.label());
        channel.write(Native950Packets.hideInterface(SHOP,COIN_ICON,true));
    }

    boolean handle(Native950Actions.InterfaceAction action){
        if(action.interfaceId()!=SHOP)return false;
        if(phase==Phase.CLOSED)return true;
        if(phase!=Phase.RESULTS)return true;
        int component=action.componentId();
        if(component==SEARCH_CONTROL||component==SEARCH_TEXT){searchPrompt();return true;}
        if(component==RECENT_CONTROL||component==RECENT_TEXT){showRecent();return true;}
        if(component!=SHOP_ITEMS)return true;
        Native950ContentCommands.ItemSearchEntry entry=resolve(action);
        if(entry==null)return true;
        selected=entry;decorate();
        telemetry("item-action","view",viewName(),"query",query,"displaySlot",action.slot(),
                "clientItem",action.itemId(),"resolvedItem",entry.id,"option",action.option());
        switch(action.option()){
            case 1: give(1);break;
            case 2: give(1);break;
            case 3: give(5);break;
            case 4: give(10);break;
            case 5: give(100);break;
            case 6: closeShop();openCustomQuantity();break;
            case 7: removeFromRecent(entry);break;
            default: telemetry("item-action-rejected","reason","unsupported-option","option",action.option());break;
        }
        return true;
    }

    /** Resolve only against the immutable sequence actually sent to the client. */
    private Native950ContentCommands.ItemSearchEntry resolve(Native950Actions.InterfaceAction action){
        Native950ContentCommands.ItemSearchEntry bySlot=action.slot()>=0&&action.slot()<rendered.size()
                ?rendered.get(action.slot()):null;
        Native950ContentCommands.ItemSearchEntry byItem=action.itemId()<0?null:uniqueRenderedItem(action.itemId());
        if(bySlot!=null&&(byItem==null||byItem.id==bySlot.id))return bySlot;
        if(byItem!=null){
            telemetry("mapping-recovered","view",viewName(),"displaySlot",action.slot(),"slotItem",bySlot==null?-1:bySlot.id,
                    "clientItem",action.itemId(),"resolvedItem",byItem.id);
            return byItem;
        }
        telemetry("item-action-rejected","reason","not-in-rendered-snapshot","view",viewName(),"displaySlot",action.slot(),
                "clientItem",action.itemId(),"displayed",rendered.size());
        player.sendMessage("That Item Browser view changed; it has been refreshed without granting anything.");
        selected=null;render();return null;
    }

    private Native950ContentCommands.ItemSearchEntry uniqueRenderedItem(int id){
        Native950ContentCommands.ItemSearchEntry found=null;
        for(Native950ContentCommands.ItemSearchEntry entry:rendered)if(entry.id==id){if(found!=null)return null;found=entry;}
        return found;
    }

    private void showRecent(){
        if(recent.isEmpty()){
            player.sendMessage("No recent Item Browser grants yet. Left-click something in Testing Kit or Search first.");
            view=View.TESTING_KIT;query="";results=Native950ContentCommands.testingKitItemBrowserEntries();
        }else{
            view=View.RECENT;query="";
            results=Collections.unmodifiableList(new ArrayList<Native950ContentCommands.ItemSearchEntry>(recent));
        }
        selected=null;phase=Phase.RESULTS;render();
    }

    private void openCustomQuantity(){
        if(selected==null){phase=Phase.RESULTS;render();return;}
        Native950Containers containers=Native950Skilling.containers(player);
        if(containers==null){player.sendMessage("The native backpack is unavailable.");phase=Phase.RESULTS;render();return;}
        Native950Containers.Snapshot snapshot=containers.inventorySnapshot();
        if(!countInput.beginDefault(0L,snapshot)){player.sendMessage("Another quantity request is already active.");phase=Phase.RESULTS;render();return;}
        phase=Phase.CUSTOM;
        telemetry("quantity-opened","item",selected.id,"view",viewName());
        for(Native950Packets.Packet packet:countInput.promptPackets("How many "+selected.name+" would you like?"))channel.write(packet);
        player.getInterfaceManager().registerNativeOpen(INPUT_FRAME,ROOT,INPUT_FRAME_HOST);
        player.getInterfaceManager().registerNativeOpen(INPUT,INPUT_FRAME,INPUT_HOST);
    }

    boolean handle(Native950Actions.CountDialogueAction action){
        if(phase!=Phase.CUSTOM)return false;
        Native950Containers containers=Native950Skilling.containers(player);
        Native950Containers.Snapshot current=containers==null?null:containers.inventorySnapshot();
        Native950QuantityInput.Accepted accepted=countInput.consume(action.count(),0L,current,(slot,amount)->amount);
        closeCountInput();phase=Phase.RESULTS;
        if(accepted==null||accepted.amount<1){
            telemetry("grant-rejected","reason","invalid-quantity-or-backpack-changed","requested",action.count());
            player.sendMessage("That quantity was invalid or your backpack changed.");render();return true;
        }
        give(accepted.amount);return true;
    }

    boolean handle(Native950Actions.DialogueClickAction action){return false;}

    boolean cancelInput(){
        if(phase==Phase.SEARCH){closeTextInput();phase=Phase.RESULTS;render();player.sendMessage("Item Browser search cancelled.");return true;}
        if(phase==Phase.CUSTOM){countInput.cancel();closeCountInput();phase=Phase.RESULTS;render();player.sendMessage("Item Browser quantity cancelled.");return true;}
        return false;
    }

    private void give(int amount){
        Native950ContentCommands.ItemSearchEntry entry=selected;
        if(entry==null||amount<1||Native950Skilling.itemType(player,entry.id)==null){
            telemetry("grant-rejected","reason","invalid-item","item",entry==null?-1:entry.id,"amount",amount);
            player.sendMessage("That item is not valid in the paired revision-950 cache.");phase=Phase.RESULTS;render();return;
        }
        boolean granted=Native950Skilling.giveItem(player,entry.id,amount);
        if(!granted)
            player.sendMessage("Could not add "+amount+" x "+entry.name+": the backpack is full, the stack would overflow, or your activity refused it.");
        else{
            player.sendMessage("Added "+amount+" x "+entry.name+" (ID "+entry.id+") to your backpack.");remember(entry);
        }
        telemetry(granted?"grant-succeeded":"grant-rejected","view",viewName(),"displaySlot",indexOf(rendered,entry.id),
                "resolvedItem",entry.id,"amount",amount,"reason",granted?"added":"backpack-or-activity-refused");
        phase=Phase.RESULTS;
        if(view==View.RECENT)results=Collections.unmodifiableList(new ArrayList<Native950ContentCommands.ItemSearchEntry>(recent));
        render();
    }

    private void remember(Native950ContentCommands.ItemSearchEntry entry){
        removeRecentId(entry.id);recent.addFirst(entry);while(recent.size()>RECENT_LIMIT)recent.removeLast();
    }

    private void removeFromRecent(Native950ContentCommands.ItemSearchEntry entry){
        if(view!=View.RECENT){player.sendMessage("Remove from Recent is available in the Recent view.");render();return;}
        boolean removed=removeRecentId(entry.id);selected=null;
        telemetry("recent-removed","item",entry.id,"removed",removed,"remaining",recent.size());
        player.sendMessage(removed?entry.name+" removed from Item Browser history.":"That item was not in Recent.");
        if(recent.isEmpty()){view=View.TESTING_KIT;results=Native950ContentCommands.testingKitItemBrowserEntries();}
        else results=Collections.unmodifiableList(new ArrayList<Native950ContentCommands.ItemSearchEntry>(recent));
        render();
    }

    private boolean removeRecentId(int id){
        for(java.util.Iterator<Native950ContentCommands.ItemSearchEntry> it=recent.iterator();it.hasNext();)
            if(it.next().id==id){it.remove();return true;}
        return false;
    }

    void close(){closeInput();closeShop();phase=Phase.CLOSED;selected=null;results=Collections.emptyList();rendered=Collections.emptyList();}
    void dispose(){close();synchronized(OWNERS){if(OWNERS.get(player)==this)OWNERS.remove(player);}}

    private void closeInput(){
        if(phase==Phase.SEARCH)closeTextInput();
        else if(phase==Phase.CUSTOM){countInput.cancel();closeCountInput();}
    }
    private void closeTextInput(){
        channel.write(Native950Packets.closeSub(INPUT_FRAME,INPUT_HOST));channel.write(Native950Packets.closeSub(ROOT,INPUT_FRAME_HOST));
        player.getInterfaceManager().unregisterNativeOpen(INPUT);player.getInterfaceManager().unregisterNativeOpen(INPUT_FRAME);
        channel.write(Native950Packets.hideInterface(ROOT,747,true));channel.write(Native950Packets.runClientScript(1364));
    }
    private void closeCountInput(){
        for(Native950Packets.Packet packet:countInput.cancelPackets())channel.write(packet);
        player.getInterfaceManager().unregisterNativeOpen(INPUT);player.getInterfaceManager().unregisterNativeOpen(INPUT_FRAME);
    }
    private void closeShop(){
        if(!shopOpen)return;
        channel.write(Native950Packets.closeSub(ROOT,MAIN_HOST));player.getInterfaceManager().unregisterNativeOpen(SHOP);
        channel.write(Native950Packets.hideInterface(ROOT,MAIN_WRAPPER,true));channel.write(Native950Packets.runClientScript(1364));shopOpen=false;
    }
    private void text(int component,String value){channel.write(Native950Packets.interfaceText(SHOP,component,value));}
    private String viewName(){return view==View.TESTING_KIT?"TESTING KIT":view==View.RECENT?"RECENT":"SEARCH: "+query;}
    private void telemetry(String event,Object... fields){Native950BugTest.event(player,"item-browser",event,fields);}
    private static int indexOf(List<Native950ContentCommands.ItemSearchEntry> values,int id){
        for(int i=0;i<values.size();i++)if(values.get(i).id==id)return i;return -1;
    }
    private static String ids(List<Native950ContentCommands.ItemSearchEntry> values){
        StringBuilder out=new StringBuilder();for(Native950ContentCommands.ItemSearchEntry entry:values){if(out.length()>0)out.append(',');out.append(entry.id);}return out.toString();
    }

    String viewForTests(){return viewName();}
    int selectedIdForTests(){return selected==null?-1:selected.id;}
    int visibleResultIdForTests(int slot){return slot<0||slot>=rendered.size()?-1:rendered.get(slot).id;}
    int visibleSlotForTests(int id){return indexOf(rendered,id);}
    int visibleResultCountForTests(){return rendered.size();}
    int recentCountForTests(){return recent.size();}
    static int nativeCapacityForTests(){return MAX_RENDERED_RESULTS;}
}
