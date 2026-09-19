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
    private static final int SEARCH_TEXT=44, SEARCH_CONTROL=45, RECENT_TEXT=35, RECENT_CONTROL=36;
    private static final int PREVIOUS=46, NEXT=47, PAGE_LABEL=49, VIEW_LABEL=52, SELECTION_LABEL=58;
    private static final int TRANSACTION_LABEL=66, OWNED_LABEL=128, PRICE_LABEL=133, PRICE_VALUE=137, COIN_ICON=136;
    private static final int INPUT_FRAME_HOST=749, INPUT_FRAME=1418, INPUT_HOST=2, INPUT=1469;
    private static final int RESULTS_CONTAINER=139, PAGE_SIZE=40, RECENT_LIMIT=12;
    private static final int SHOP_OPTION_MASK=2097406;
    private static final Map<Player,Native950ItemBrowser> OWNERS=new IdentityHashMap<Player,Native950ItemBrowser>();
    private enum Phase { CLOSED, RESULTS, SEARCH, QUANTITY, CUSTOM }

    private final Player player;
    private final Channel channel;
    private final Native950Dialogues dialogues;
    private final Runnable verifier;
    private final Native950QuantityInput countInput=new Native950QuantityInput();
    private final Deque<Native950ContentCommands.ItemSearchEntry> recent=new ArrayDeque<Native950ContentCommands.ItemSearchEntry>();
    private Phase phase=Phase.CLOSED;
    private boolean shopOpen;
    private int page;
    private String view="Featured";
    private List<Native950ContentCommands.ItemSearchEntry> results=Collections.emptyList();
    private Native950ContentCommands.ItemSearchEntry selected;

    Native950ItemBrowser(Player player,Channel channel,Native950Dialogues dialogues){
        this(player,channel,dialogues,Native950ItemBrowser::verifyCacheBindings);
    }

    Native950ItemBrowser(Player player,Channel channel,Native950Dialogues dialogues,Runnable verifier){
        this.player=player;this.channel=channel;this.dialogues=dialogues;
        this.verifier=verifier;
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
        verifier.run();
        if(phase==Phase.CLOSED){
            results=Native950ContentCommands.featuredItemBrowserEntries();
            view="Featured";page=0;selected=null;phase=Phase.RESULTS;
        }else if(phase==Phase.SEARCH)closeTextInput();
        else if(phase==Phase.QUANTITY)dialogues.close();
        else if(phase==Phase.CUSTOM){countInput.cancel();closeCountInput();}
        phase=Phase.RESULTS;
        showPage();
        player.sendMessage("Developer Item Browser opened. Search, Recent and page controls are inside the window.");
    }

    private void searchPrompt(){
        if(!shopOpen)showPage();
        phase=Phase.SEARCH;
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
        String query=action.text()==null?"":action.text().trim();
        if(query.length()<1||query.length()>80){player.sendMessage("Enter an item name or exact numeric ID (1-80 characters).");showPage();return true;}
        List<Native950ContentCommands.ItemSearchEntry> matches=Native950ContentCommands.itemMatches(query,Integer.MAX_VALUE);
        if(matches.isEmpty()){player.sendMessage("No revision-950 items matched '"+query+"'.");showPage();return true;}
        results=matches;view="Search: "+query;page=0;selected=null;
        player.sendMessage("Item Browser found "+results.size()+" result(s) for '"+query+"'.");
        showPage();return true;
    }

    private void showPage(){
        if(results.isEmpty())results=Native950ContentCommands.featuredItemBrowserEntries();
        page=Math.max(0,Math.min(page,pageCount(results.size())-1));
        List<Native950ContentCommands.ItemSearchEntry> visible=page(results,page,PAGE_SIZE);
        int[] ids=new int[visible.size()],amounts=new int[visible.size()];
        for(int i=0;i<visible.size();i++){ids[i]=visible.get(i).id;amounts[i]=1;}
        channel.write(Native950Packets.varp(304,RESULTS_CONTAINER));
        channel.write(Native950Packets.varp(305,-1));channel.write(Native950Packets.varp(306,-1));
        channel.write(Native950Packets.varcString(2360,"DEVELOPER ITEM BROWSER"));
        channel.write(Native950Packets.inventoryFull(RESULTS_CONTAINER,false,ids,amounts));
        if(!shopOpen){
            channel.write(Native950Packets.openSub(ROOT,MAIN_HOST,SHOP,false));
            player.getInterfaceManager().registerNativeOpen(SHOP,ROOT,MAIN_HOST);
            channel.write(Native950Packets.hideInterface(ROOT,MAIN_WRAPPER,false));
            shopOpen=true;
        }
        channel.write(Native950Packets.interfaceEvents(SHOP,SHOP_ITEMS,0,Math.max(0,ids.length-1),SHOP_OPTION_MASK));
        for(int control:new int[]{SEARCH_CONTROL,SEARCH_TEXT,RECENT_CONTROL,RECENT_TEXT,PREVIOUS,NEXT})
            channel.write(Native950Packets.interfaceEvents(SHOP,control,-1,-1,2));
        channel.write(Native950Packets.runClientScript(8420,82903048,82903256,82903049,82903257,
                "DEVELOPER ITEM BROWSER",21218,1007));
        decorate(visible.size());
        channel.write(Native950Packets.runClientScript(1364));
    }

    private void decorate(int visible){
        text(SEARCH_TEXT,"Search");text(RECENT_TEXT,"Recent");
        text(PAGE_LABEL,"Page "+(page+1)+" / "+pageCount(results.size()));
        text(VIEW_LABEL,view+" - "+results.size()+" result"+(results.size()==1?"":"s"));
        text(TRANSACTION_LABEL,"Give:");text(OWNED_LABEL,"");text(PRICE_LABEL,"Item ID:");
        text(PRICE_VALUE,selected==null?"-":String.valueOf(selected.id));
        text(SELECTION_LABEL,selected==null?"Select an item ("+visible+" on this page)":selected.label());
        channel.write(Native950Packets.hideInterface(SHOP,COIN_ICON,true));
    }

    boolean handle(Native950Actions.InterfaceAction action){
        if(action.interfaceId()!=SHOP)return false;
        if(phase==Phase.CLOSED)return true;
        if(phase!=Phase.RESULTS)return true;
        int component=action.componentId();
        if(component==SEARCH_CONTROL||component==SEARCH_TEXT){searchPrompt();return true;}
        if(component==RECENT_CONTROL||component==RECENT_TEXT){showRecent();return true;}
        if(component==PREVIOUS){if(page>0){page--;selected=null;showPage();}return true;}
        if(component==NEXT){if(page+1<pageCount(results.size())){page++;selected=null;showPage();}return true;}
        if(component!=SHOP_ITEMS)return true;
        List<Native950ContentCommands.ItemSearchEntry> visible=page(results,page,PAGE_SIZE);
        if(action.slot()<0||action.slot()>=visible.size())return true;
        Native950ContentCommands.ItemSearchEntry entry=visible.get(action.slot());
        if(action.itemId()!=-1&&action.itemId()!=entry.id){player.sendMessage("That Item Browser result changed; refresh the page.");showPage();return true;}
        selected=entry;decorate(visible.size());
        switch(action.option()){
            case 1: phase=Phase.QUANTITY;dialogues.options(entry.label(),"Give 1","Give 5","Give 10","Give 100","Give X...");break;
            case 2: give(1);break;
            case 3: give(5);break;
            case 4: give(10);break;
            case 5: give(100);break;
            case 6: openCustomQuantity();break;
            default: break;
        }
        return true;
    }

    private void showRecent(){
        results=recent.isEmpty()?Native950ContentCommands.featuredItemBrowserEntries()
                :Collections.unmodifiableList(new ArrayList<Native950ContentCommands.ItemSearchEntry>(recent));
        view=recent.isEmpty()?"Featured (no recent items yet)":"Recent";page=0;selected=null;phase=Phase.RESULTS;showPage();
    }

    boolean handle(Native950Actions.DialogueClickAction action){
        if(phase!=Phase.QUANTITY)return false;
        if(!dialogues.consumeResponse(action.interfaceId(),action.componentId(),action.slot()))return true;
        int row=(action.componentId()-8)/5;
        if(row>=0&&row<4){int[] amounts={1,5,10,100};dialogues.close();phase=Phase.RESULTS;give(amounts[row]);return true;}
        if(row==4){dialogues.close();openCustomQuantity();return true;}
        dialogues.close();phase=Phase.RESULTS;return true;
    }

    private void openCustomQuantity(){
        if(selected==null){phase=Phase.RESULTS;return;}
        Native950Containers containers=Native950Skilling.containers(player);
        if(containers==null){player.sendMessage("The native backpack is unavailable.");phase=Phase.RESULTS;return;}
        Native950Containers.Snapshot snapshot=containers.inventorySnapshot();
        if(!countInput.beginDefault(0L,snapshot)){player.sendMessage("Another quantity request is already active.");phase=Phase.RESULTS;return;}
        phase=Phase.CUSTOM;
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
        if(accepted==null||accepted.amount<1){player.sendMessage("That quantity was invalid or your backpack changed.");return true;}
        give(accepted.amount);return true;
    }

    boolean cancelInput(){
        if(phase==Phase.SEARCH){closeTextInput();phase=Phase.RESULTS;player.sendMessage("Item Browser search cancelled.");return true;}
        if(phase==Phase.CUSTOM){countInput.cancel();closeCountInput();phase=Phase.RESULTS;player.sendMessage("Item Browser quantity cancelled.");return true;}
        return false;
    }

    private void give(int amount){
        Native950ContentCommands.ItemSearchEntry entry=selected;
        if(entry==null||amount<1||Native950Skilling.itemType(player,entry.id)==null){player.sendMessage("That item is not valid in the paired revision-950 cache.");return;}
        if(!Native950Skilling.giveItem(player,entry.id,amount))
            player.sendMessage("Could not add "+amount+" x "+entry.name+": the backpack is full, the stack would overflow, or your activity refused it.");
        else {player.sendMessage("Added "+amount+" x "+entry.name+" (ID "+entry.id+") to your backpack.");remember(entry);}
        phase=Phase.RESULTS;showPage();
    }

    private void remember(Native950ContentCommands.ItemSearchEntry entry){
        recent.remove(entry);recent.addFirst(entry);while(recent.size()>RECENT_LIMIT)recent.removeLast();
    }

    void close(){
        if(phase==Phase.SEARCH)closeTextInput();
        else if(phase==Phase.QUANTITY)dialogues.close();
        else if(phase==Phase.CUSTOM){countInput.cancel();closeCountInput();}
        closeShop();phase=Phase.CLOSED;selected=null;results=Collections.emptyList();page=0;
    }
    void dispose(){close();synchronized(OWNERS){if(OWNERS.get(player)==this)OWNERS.remove(player);}}

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

    static int pageCount(int size){return Math.max(1,(Math.max(0,size)+PAGE_SIZE-1)/PAGE_SIZE);}
    static <T> List<T> page(List<T> values,int page,int pageSize){
        if(values==null||values.isEmpty()||page<0||pageSize<1)return Collections.emptyList();
        int from=page*pageSize;if(from>=values.size())return Collections.emptyList();
        return Collections.unmodifiableList(new ArrayList<T>(values.subList(from,Math.min(values.size(),from+pageSize))));
    }
}
