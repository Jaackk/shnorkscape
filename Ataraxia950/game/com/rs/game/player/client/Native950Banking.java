package com.rs.game.player.client;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Bank;
import com.rs.game.player.Player;
import java.util.Arrays;

/** Current-cache ordinary bank transfers over the original Player's staged containers. */
public final class Native950Banking {
    private Native950Banking() { }

    public static boolean deposit(Player player,Bank bank,int slot,int requested,boolean refresh,int component) {
        Native950Containers containers=available(player,bank);
        if(containers==null)return refuse(player,"You cannot use that bank right now.");
        if(component!=14)return refuse(player,"That bank deposit source has not been enabled yet.");
        if(slot<0||slot>=Native950Containers.INVENTORY_SIZE||requested<1)return false;
        Item source=player.getInventory().items.get(slot);
        if(!ordinary(source,containers))return refuse(player,"That item's stored state cannot be banked yet.");
        int id=source.getId(),sourceAmount=source.getAmount();
        ItemDefinitions definition=Native950CacheItems.definition(id);
        if(definition==null)return refuse(player,"That item is missing from the current cache.");
        // Cache opcode flags are not a native bankability contract.  In particular,
        // development/cache variants often carry a legacy flag although the actual
        // bank container can store them.  Preserve the item's authenticated identity
        // and let capacity/controller checks below decide the transaction.
        int bankedId=id;
        // Ordinary note deposits retain normal RuneScape behavior: resolve the authored
        // current-cache link, never id-1/id+1 or an old910 item table.
        for(int depth=0;definition.noted;depth++) {
            if(depth>=16)return refuse(player,"That bank-note reference is invalid.");
            bankedId=definition.certId;
            definition=Native950CacheItems.definition(bankedId);
            if(definition==null||containers.itemType(bankedId)==null)return refuse(player,"That bank-note reference is unavailable.");
        }
        Native950Containers.Snapshot inventory=containers.inventorySnapshot(),stored=containers.bankSnapshot();
        long available=0;for(int i=0;i<inventory.ids.length;i++)if(inventory.ids[i]==id)available+=inventory.amounts[i];
        int amount=(int)Math.min((long)requested,available);
        for(int i=0;i<stored.ids.length;i++)if(stored.ids[i]==bankedId)
            amount=(int)Math.min((long)amount,(long)Integer.MAX_VALUE-stored.amounts[i]);
        if(amount<1)return refuse(player,"There is no room for more of that item in the bank.");
        if(!player.getControlerManager().canDeleteInventoryItem(id,amount))
            return refuse(player,"You cannot bank that item here.");
        if(available(player,bank)!=containers||player.getInventory().items.get(slot)!=source
                ||source.getId()!=id||source.getAmount()!=sourceAmount||!ordinary(source,containers)
                ||!same(inventory,containers.inventorySnapshot())||!same(stored,containers.bankSnapshot()))
            return refuse(player,"The items changed before they could be banked.");
        Native950Containers.Result result=containers.deposit(slot,id,amount,bankedId);
        if(result.moved==0)return refuse(player,result.reason);
        player.getInventory().refresh();
        if(refresh)bank.refreshItems();
        return true;
    }

    public static boolean withdraw(Player player,Bank bank,int slot,int requested) {
        Native950Containers containers=available(player,bank);
        if(containers==null)return refuse(player,"You cannot use that bank right now.");
        if(slot<0||slot>=bank.bankTabs[0].length||requested<1)return false;
        Item source=bank.bankTabs[0][slot];
        if(!ordinary(source,containers))return refuse(player,"That item's stored state cannot be withdrawn yet.");
        int id=source.getId(),sourceAmount=source.getAmount();
        int outputId=withdrawnItemId(player,id);
        int amount=containers.withdrawableAmount(slot,id,requested,outputId);
        if(amount<1)return refuse(player,"Your backpack has no room for that item.");
        Native950Containers.Snapshot inventory=containers.inventorySnapshot(),stored=containers.bankSnapshot();
        if(!player.getControlerManager().canAddInventoryItem(outputId,amount))
            return refuse(player,"You cannot withdraw that item here.");
        if(available(player,bank)!=containers||slot>=bank.bankTabs[0].length||bank.bankTabs[0][slot]!=source
                ||source.getId()!=id||source.getAmount()!=sourceAmount||!ordinary(source,containers)
                ||outputId!=withdrawnItemId(player,id)
                ||!same(inventory,containers.inventorySnapshot())||!same(stored,containers.bankSnapshot()))
            return refuse(player,"The items changed before they could be withdrawn.");
        Native950Containers.Result result=containers.withdraw(slot,id,amount,outputId);
        if(result.moved==0)return refuse(player,result.reason);
        if(bank.getWithdrawNotes()&&outputId==id&&!Native950CacheItems.definition(id).noted)
            player.sendMessage("You cannot withdraw this item as a note; it was withdrawn normally.");
        // The supported950 bank targets the actual backpack, including ordinary coins.
        player.getInventory().refresh();bank.refreshItems();
        return true;
    }

    /** Pure current-cache output identity for both UI prediction and the actual withdrawal. */
    public static int withdrawnItemId(Player player,int sourceId) {
        if(player==null||player.getBank()==null||!player.getBank().getWithdrawNotes())return sourceId;
        ItemDefinitions source=Native950CacheItems.definition(sourceId);
        if(source==null||source.noted||source.certId<0)return sourceId;
        ItemDefinitions note=Native950CacheItems.definition(source.certId);
        if(note==null||!note.noted||note.certTemplateId<0||note.certId!=sourceId||note.stackable!=1
                ||Native950CacheItems.entry(note.getId())==null)return sourceId;
        return note.getId();
    }

    public static int withdrawableAmount(Player player,Native950Containers containers,int slot,int sourceId,int requested) {
        if(containers==null||!containers.ownsInventory(player))return 0;
        return containers.withdrawableAmount(slot,sourceId,requested,withdrawnItemId(player,sourceId));
    }

    /** Deposit each eligible worn slot in one commit; skipped equipment remains worn without a partial stack. */
    public static boolean depositEquipment(Player player,Bank bank,boolean refresh) {
        Native950Containers containers=available(player,bank);
        if(containers==null)return refuse(player,"You cannot use that bank right now.");
        Native950Containers.EquipmentBankChange change;
        try{change=containers.prepareEquipmentDeposit();}
        catch(IllegalArgumentException|IllegalStateException invalid){return refuse(player,"Your stored item state cannot be banked yet.");}
        int worn=0,skipped=0;String firstReason=null;
        for(int slot=0;slot<change.originalEquipment.length;slot++) {
            Item item=change.originalEquipment[slot];if(item==null)continue;worn++;
            ItemDefinitions type=Native950CacheItems.definition(item.getId());
            String reason=null;
            if(!ordinary(item,containers)||type==null)reason="stored item state is not supported";
            else if(!Native950EquipmentActions.cacheAllowsRemoval(type))reason=type.getName()+" cannot be removed";
            else if(!player.getControlerManager().canRemoveEquip(slot,item.getId()))reason="equipment removal was refused here";
            if(reason==null) {
                Native950Containers.Result included=change.include(slot);
                if(included.moved==0)reason=included.reason;
            }
            if(reason!=null){skipped++;if(firstReason==null)firstReason=reason;}
        }
        if(worn==0)return refuse(player,"You are not wearing any equipment.");
        if(change.movedSlots==0)return refuse(player,"No equipment was deposited: "+firstReason+".");
        if(available(player,bank)!=containers)return refuse(player,"You cannot use that bank right now.");
        Native950Containers.Result committed;
        try{committed=change.commit();}
        catch(IllegalArgumentException|IllegalStateException changed){return refuse(player,"The equipment changed before it could be banked.");}
        if(committed.moved==0)return refuse(player,committed.reason);
        player.getAppearence().generateAppearenceData();
        if(refresh)bank.refreshItems();
        if(skipped>0)player.sendMessage("Some equipment stayed worn: "+firstReason+".");
        return true;
    }
    static boolean bankable(ItemDefinitions definition) { return definition!=null; }

    private static Native950Containers available(Player player,Bank bank) {
        if(player==null||!player.isNative950()||!player.isActive()||player.hasFinished()||player.isDead()
                ||player.isLocked()||player.isNative950ForceMovementActive()||player.getNextWorldTile()!=null
                ||Boolean.TRUE.equals(player.getTemporaryAttributtes().get("teleporting"))
                ||bank==null||player.getBank()!=bank)return null;
        Native950Containers containers=Native950Skilling.containers(player);
        return containers!=null&&containers.ownsInventory(player)?containers:null;
    }
    private static boolean ordinary(Item item,Native950Containers containers) {
        return item!=null&&item.getAmount()>0&&item.getCharges()==0&&item.getAttributes()==null
                &&item.getInventionData()==null&&containers.itemType(item.getId())!=null;
    }
    private static boolean same(Native950Containers.Snapshot a,Native950Containers.Snapshot b) {
        return Arrays.equals(a.ids,b.ids)&&Arrays.equals(a.amounts,b.amounts);
    }
    public static boolean refuse(Player player,String reason) {
        if(player!=null)player.sendMessage(reason==null?"That bank transfer could not be completed.":reason);
        return false;
    }
}
