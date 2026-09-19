package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/** Session-owned developer item search. The shop grid is a read-only icon surface, not a Shop. */
final class Native950ItemBrowser {
    private static final int ROOT=1477, MAIN_HOST=735, MAIN_WRAPPER=732, SHOP=1265, SHOP_ITEMS=20;
    private static final int INPUT_FRAME_HOST=749, INPUT_FRAME=1418, INPUT_HOST=2, INPUT=1469;
    private static final int RESULTS_CONTAINER=139, MAX_RESULTS=60;
    private static final Map<Player,Native950ItemBrowser> OWNERS=new IdentityHashMap<Player,Native950ItemBrowser>();
    private enum Phase { CLOSED, SEARCH, RESULTS, QUANTITY, CUSTOM }

    private final Player player;
    private final Channel channel;
    private final Native950Dialogues dialogues;
    private final Native950QuantityInput countInput=new Native950QuantityInput();
    private Phase phase=Phase.CLOSED;
    private List<Native950ContentCommands.ItemSearchEntry> results=Collections.emptyList();
    private Native950ContentCommands.ItemSearchEntry selected;

    Native950ItemBrowser(Player player,Channel channel,Native950Dialogues dialogues){
        this.player=player;this.channel=channel;this.dialogues=dialogues;
        synchronized(OWNERS){OWNERS.put(player,this);}
    }

    static void open(Player player){
        Native950ItemBrowser browser;
        synchronized(OWNERS){browser=OWNERS.get(player);}
        if(browser==null){player.sendMessage("The native item browser is not attached to this session yet.");return;}
        browser.searchPrompt();
    }

    boolean isOpen(){return phase!=Phase.CLOSED;}

    static void verifyCacheBindings(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Item Browser requires the paired revision-950 cache");
        Native950QuantityInput.verify();
        if(Cache.STORE.getIndexes()[3].getFile(SHOP,0)==null||Cache.STORE.getIndexes()[12].getFile(110,0)==null
                ||Cache.STORE.getIndexes()[12].getFile(8420,0)==null)
            throw new IllegalStateException("The paired cache does not contain the native Item Browser surfaces");
    }

    private void searchPrompt(){
        verifyCacheBindings();closeVisible();phase=Phase.SEARCH;selected=null;results=Collections.emptyList();
        channel.write(Native950Packets.openSub(ROOT,INPUT_FRAME_HOST,INPUT_FRAME,true));
        channel.write(Native950Packets.openSub(INPUT_FRAME,INPUT_HOST,INPUT,true));
        player.getInterfaceManager().registerNativeOpen(INPUT_FRAME,ROOT,INPUT_FRAME_HOST);
        player.getInterfaceManager().registerNativeOpen(INPUT,INPUT_FRAME,INPUT_HOST);
        channel.write(Native950Packets.hideInterface(ROOT,747,false));
        channel.write(Native950Packets.runClientScript(110,"Search item name or exact ID:"));
    }

    boolean handle(Native950Actions.StringDialogueAction action){
        if(phase!=Phase.SEARCH)return false;
        closeTextInput();phase=Phase.CLOSED;
        String query=action.text()==null?"":action.text().trim();
        if(query.length()<1||query.length()>80){player.sendMessage("Enter an item name or exact numeric ID (1-80 characters).");searchPrompt();return true;}
        results=Native950ContentCommands.itemMatches(query,MAX_RESULTS);
        if(results.isEmpty()){player.sendMessage("No revision-950 items matched '"+query+"'.");searchPrompt();return true;}
        player.sendMessage("Item Browser: "+results.size()+" result(s) for '"+query+"'. Click an icon to choose it.");
        for(int i=0;i<Math.min(10,results.size());i++)player.sendMessage((i+1)+". "+results.get(i).label());
        if(results.size()>10)player.sendMessage("The native grid shows all results; refine the search to narrow similar variants.");
        showResults(query);return true;
    }

    private void showResults(String query){
        closeVisible();phase=Phase.RESULTS;
        int[] ids=new int[results.size()],amounts=new int[results.size()];
        for(int i=0;i<results.size();i++){ids[i]=results.get(i).id;amounts[i]=1;}
        channel.write(Native950Packets.varp(304,RESULTS_CONTAINER));
        channel.write(Native950Packets.varp(305,-1));channel.write(Native950Packets.varp(306,995));
        channel.write(Native950Packets.varcString(2360,"Item Browser - "+query));
        channel.write(Native950Packets.inventoryFull(RESULTS_CONTAINER,false,ids,amounts));
        channel.write(Native950Packets.openSub(ROOT,MAIN_HOST,SHOP,false));
        player.getInterfaceManager().registerNativeOpen(SHOP,ROOT,MAIN_HOST);
        channel.write(Native950Packets.hideInterface(ROOT,MAIN_WRAPPER,false));
        channel.write(Native950Packets.interfaceEvents(SHOP,SHOP_ITEMS,0,Math.max(0,ids.length-1),2));
        channel.write(Native950Packets.runClientScript(8420,82903048,82903256,82903049,82903257,
                "Item Browser",21218,1007));
        channel.write(Native950Packets.runClientScript(1364));
    }

    boolean handle(Native950Actions.InterfaceAction action){
        if(action.interfaceId()!=SHOP)return false;
        if(phase!=Phase.RESULTS)return true;
        if(action.componentId()!=SHOP_ITEMS||action.option()!=1||action.slot()<0||action.slot()>=results.size())return true;
        Native950ContentCommands.ItemSearchEntry entry=results.get(action.slot());
        if(action.itemId()!=-1&&action.itemId()!=entry.id){player.sendMessage("That Item Browser result changed; search again.");searchPrompt();return true;}
        selected=entry;closeShop();phase=Phase.QUANTITY;
        dialogues.options(entry.label(),"Give 1","Give 5","Give 10","Give 100","Custom quantity...");
        return true;
    }

    boolean handle(Native950Actions.DialogueClickAction action){
        if(phase!=Phase.QUANTITY)return false;
        if(!dialogues.consumeResponse(action.interfaceId(),action.componentId(),action.slot()))return true;
        int row=(action.componentId()-8)/5;
        if(row>=0&&row<4){int[] amounts={1,5,10,100};dialogues.close();give(amounts[row]);return true;}
        if(row==4){dialogues.close();openCustomQuantity();return true;}
        close();return true;
    }

    private void openCustomQuantity(){
        Native950Containers containers=Native950Skilling.containers(player);
        if(containers==null){player.sendMessage("The native backpack is unavailable.");close();return;}
        Native950Containers.Snapshot snapshot=containers.inventorySnapshot();
        if(!countInput.beginDefault(0L,snapshot)){player.sendMessage("Another quantity request is already active.");close();return;}
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
        closeCountInput();phase=Phase.CLOSED;
        if(accepted==null||accepted.amount<1){player.sendMessage("That quantity was invalid or your backpack changed. Choose the item again.");showResults("previous results");return true;}
        give(accepted.amount);return true;
    }

    boolean cancelInput(){
        if(phase!=Phase.SEARCH&&phase!=Phase.CUSTOM)return false;
        close();player.sendMessage("Item Browser input cancelled.");return true;
    }

    private void give(int amount){
        Native950ContentCommands.ItemSearchEntry entry=selected;selected=null;
        if(entry==null||amount<1||Native950Skilling.itemType(player,entry.id)==null){player.sendMessage("That item is not valid in the paired revision-950 cache.");close();return;}
        if(!Native950Skilling.giveItem(player,entry.id,amount))
            player.sendMessage("Could not add "+amount+" x "+entry.name+": the backpack is full, the stack would overflow, or your activity refused it.");
        else player.sendMessage("Added "+amount+" x "+entry.name+" (ID "+entry.id+") to your backpack.");
        if(results.isEmpty())close();else showResults("previous results");
    }

    void close(){closeVisible();phase=Phase.CLOSED;selected=null;results=Collections.emptyList();}
    void dispose(){close();synchronized(OWNERS){if(OWNERS.get(player)==this)OWNERS.remove(player);}}

    private void closeVisible(){
        if(phase==Phase.SEARCH)closeTextInput();
        else if(phase==Phase.RESULTS)closeShop();
        else if(phase==Phase.QUANTITY)dialogues.close();
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
        channel.write(Native950Packets.closeSub(ROOT,MAIN_HOST));player.getInterfaceManager().unregisterNativeOpen(SHOP);
        channel.write(Native950Packets.hideInterface(ROOT,MAIN_WRAPPER,true));channel.write(Native950Packets.runClientScript(1364));
    }
}
