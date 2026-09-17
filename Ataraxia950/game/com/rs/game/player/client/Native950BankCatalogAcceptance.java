package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.BodyDefinitions;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.modern.FlatCacheRepository;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.ControlerManager;
import com.rs.game.player.Player;
import com.rs.game.player.TemporaryAttributes;
import com.rs.game.player.controllers.Controller;
import com.rs.game.player.actions.invention.InventionData;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/** Full paired-cache item banking sweep through real Bank entrypoints; no network listener or account files. */
public final class Native950BankCatalogAcceptance {
    private static int definitions,unnamed,resolved,blocked,banked,notes,lent,bound,shards,oldRange;
    private Native950BankCatalogAcceptance() { }
    public static void main(String[] args)throws Exception {
        require(args.length==1,"Usage: Native950BankCatalogAcceptance <950-flat-cache>");
        require(NativeCacheVerification.isEnforced(),"Cache verification must remain enabled");
        Cache.initFlatReadOnly(Paths.get(args[0]));BodyDefinitions.init();
        FlatCacheRepository files=new FlatCacheRepository(Paths.get(args[0]));
        List<Native950ItemCatalog.Entry> entries=new ArrayList<Native950ItemCatalog.Entry>();
        int maximum=-1;
        for(FlatCacheRepository.Group group:files.getIndexes().get(19).getGroups().values()) {
            for(int file:group.getFileIds()) {
                int id=(group.id<<8)|file;maximum=Math.max(maximum,id);definitions++;
                ItemDefinitions type=Native950CacheItems.definition(id);
                require(type!=null,"Malformed or unresolved current-cache definition "+id);
                Native950ItemCatalog.Entry entry=Native950CacheItems.entry(id);
                if(entry==null){unnamed++;continue;}
                require(entry.stackable==(type.stackable==1),"Legacy stackability override "+id);
                entries.add(entry);resolved++;
            }
        }
        Native950ItemCatalog catalog=new Native950ItemCatalog(entries);
        require(catalog.get(0)!=null&&!catalog.get(0).stackable,"Cache ID0 Dwarf remains must be ordinary nonstackable");
        EmbeddedChannel channel=new EmbeddedChannel(); try {
            Player player=Player.createNative950("bank-sweep",new WorldTile(3217,3258,0),channel);
            player.setActive(true);
            Native950Containers containers=new Native950Containers(player,catalog);
            Native950Skilling.attach(player,containers);
            for(Native950ItemCatalog.Entry entry:entries) {
                reset(player);ItemDefinitions type=Native950CacheItems.definition(entry.id);
                int bankedId=entry.id;ItemDefinitions target=type;
                boolean mayBank=Native950Banking.bankable(type);
                while(target.noted) {
                    bankedId=target.certId;target=Native950CacheItems.definition(bankedId);
                    require(target!=null,"Unresolved bank note base "+entry.id);
                }
                mayBank&=Native950Banking.bankable(target)&&catalog.get(bankedId)!=null;
                if(entry.stackable)player.getInventory().items.set(17,new Item(entry.id,3));
                else for(int slot:new int[]{2,9,17})player.getInventory().items.set(slot,new Item(entry.id,1));
                boolean deposited=player.getBank().depositItem(17,Integer.MAX_VALUE,false,14);
                require(deposited==mayBank,"Current-cache bank policy mismatch for "+entry.id+" "+entry.name);
                if(!mayBank) {
                    require(count(player,entry.id)==3&&player.getBank().bankTabs[0].length==0,"Refused deposit mutated "+entry.id);blocked++;drain(channel);continue;
                }
                require(count(player,entry.id)==0,"Deposit did not consume exact source quantity "+entry.id);
                require(player.getBank().bankTabs[0].length==1&&player.getBank().bankTabs[0][0].getId()==bankedId
                        &&player.getBank().bankTabs[0][0].getAmount()==3,"Wrong bank identity or quantity "+entry.id);
                Native950Save saved=containers.saveSnapshot("bank-sweep",3217,3258,0);
                require(saved.bankIds()[0]==bankedId&&saved.bankAmounts()[0]==3,"Save lost bank identity "+entry.id);
                require(player.getBank().withdrawItem(0,Integer.MAX_VALUE),"Current-cache withdrawal failed "+entry.id);
                require(player.getBank().bankTabs[0].length==0&&count(player,bankedId)==3,"Withdrawal lost or duplicated "+entry.id);
                require(!player.getBank().withdrawItem(0,Integer.MAX_VALUE),"Exhausted bank row withdrew twice "+entry.id);
                banked++;if(type.noted)notes++;if(type.lendTemplateId!=-1)lent++;if(type.bindTemplateId!=-1)bound++;
                if(type.shardTemplateId!=-1)shards++;if(entry.id>=20502&&entry.id<=20652)oldRange++;
                drain(channel);
            }
            boundaries(player,containers,channel);
            noteWithdrawals(player,containers,entries,channel);
            equipmentDeposits(player,channel);
            Native950Skilling.detach(player);
        } finally {channel.finishAndReleaseAll();}
        require(banked>50000&&notes>1000,"Sweep unexpectedly excluded a broad item category");
        System.out.println("PASS: all "+definitions+" cache definitions (ID0.."+maximum+") decoded; "+resolved+" named identities; "+unnamed+" unnamed templates/internal records");
        System.out.println("PASS: "+banked+" real Bank deposit/save-snapshot/withdraw round trips; "+blocked+" authored non-bankable refusals; notes="+notes+", lent="+lent+", bound="+bound+", shards="+shards+", former910 hardcoded-range="+oldRange);
        System.out.println("PASS: controller veto/mutation, special state preservation and backpack capacity; no account files or live player touched");
    }
    private static void noteWithdrawals(final Player player,Native950Containers containers,List<Native950ItemCatalog.Entry> entries,EmbeddedChannel channel)throws Exception {
        if(!player.getBank().getWithdrawNotes())player.getBank().switchWithdrawNotes();
        int checked=0;
        for(Native950ItemCatalog.Entry entry:entries) {
            int output=Native950Banking.withdrawnItemId(player,entry.id);if(output==entry.id)continue;
            reset(player);player.getBank().bankTabs=new Item[][]{{new Item(entry.id,50)}};
            for(int slot=0;slot<28;slot++)if(slot!=17)player.getInventory().items.set(slot,new Item(1511,1));
            require(Native950Banking.withdrawableAmount(player,containers,0,entry.id,50)==50,"Note preview used base capacity "+entry.id);
            require(player.getBank().withdrawItem(0,50),"Noted withdrawal refused "+entry.id);
            require(player.getInventory().items.get(17).getId()==output&&player.getInventory().items.get(17).getAmount()==50
                    &&player.getBank().bankTabs[0].length==0,"Note withdrawal identity/quantity mismatch "+entry.id);
            if(Native950Banking.bankable(Native950CacheItems.definition(entry.id))) {
                require(player.getBank().depositItem(17,50,false),"Current note failed reverse deposit "+entry.id);
                require(player.getBank().bankTabs[0].length==1&&player.getBank().bankTabs[0][0].getId()==entry.id
                        &&player.getBank().bankTabs[0][0].getAmount()==50,"Note reverse deposit changed base "+entry.id);
            }
            checked++;drain(channel);
        }
        require(checked>8000,"Unexpected missing note links");
        reset(player);player.getBank().bankTabs=new Item[][]{{new Item(995,20)}};
        require(Native950Banking.withdrawnItemId(player,995)==995&&player.getBank().withdrawItem(0,20)&&count(player,995)==20,"Unnotable coins did not fall back normally");
        reset(player);player.getBank().bankTabs=new Item[][]{{new Item(1511,20)}};
        final int note=Native950Banking.withdrawnItemId(player,1511);
        install(player,new Controller(){public void start(){}@Override public boolean canAddInventoryItem(int id,int amount){require(id==note,"Controller saw bank base instead of output note");return false;}});
        require(!player.getBank().withdrawItem(0,20)&&player.getBank().bankTabs[0][0].getAmount()==20,"Note ignored output controller veto");
        install(player,new Controller(){public void start(){}@Override public boolean canAddInventoryItem(int id,int amount){player.getBank().switchWithdrawNotes();return true;}});
        require(!player.getBank().withdrawItem(0,20)&&player.getBank().bankTabs[0][0].getAmount()==20,"Controller changed note mode during commit");
        install(player,null);reset(player);if(player.getBank().getWithdrawNotes())player.getBank().switchWithdrawNotes();drain(channel);
        System.out.println("PASS: "+checked+" current-cache note withdrawal links with one free backpack slot, reverse deposits, unnotable fallback and controller/output-mode guards");
    }
    private static void equipmentDeposits(final Player player,EmbeddedChannel channel)throws Exception {
        reset(player);Native950ItemCatalog gear=new Native950ItemCatalog(java.util.Collections.<Native950ItemCatalog.Entry>emptyList()).withLegacyDrops();
        Native950Containers containers=new Native950Containers(player,gear);Native950Skilling.attach(player,containers);
        Item shield=new Item(1173,1);
        player.getEquipment().getItems().set(3,new Item(1277,1));player.getEquipment().getItems().set(5,shield);player.getEquipment().getItems().set(13,new Item(882,500));
        install(player,new Controller(){public void start(){}@Override public boolean canRemoveEquip(int slot,int id){return slot!=5;}});
        require(Native950Banking.depositEquipment(player,player.getBank(),false),"Eligible equipment subset failed");
        require(player.getEquipment().getItems().get(3)==null&&player.getEquipment().getItems().get(13)==null
                &&player.getEquipment().getItems().get(5)==shield,"Bulk equipment removed controller-denied item");
        require(bankCount(player,1277)==1&&bankCount(player,882)==500,"Equipment quantity lost in bank");
        require(player.getBank().getNumberOf(1277)==1&&player.getBank().getNumberOf(882)==500,"Native Bank quantity API omitted active-bank equipment");
        install(player,null);require(Native950Banking.depositEquipment(player,player.getBank(),false),"Remaining shield deposit failed");
        require(bankCount(player,1173)==1&&player.getBank().getNumberOf(1173)==1,"Shield missing from active bank or quantity API after deposit");
        reset(player);final Item weapon=new Item(1277,1);player.getEquipment().getItems().set(3,weapon);
        install(player,new Controller(){public void start(){}@Override public boolean canRemoveEquip(int slot,int id){player.getEquipment().getItems().set(slot,new Item(id,1));return true;}});
        require(!Native950Banking.depositEquipment(player,player.getBank(),false)&&player.getBank().bankTabs[0].length==0,"Bulk gear deposit consumed controller replacement");
        install(player,null);player.getEquipment().getItems().clear();reset(player);drain(channel);
        System.out.println("PASS: actual-cache equipment subset deposit preserves controller-denied slots, ammo quantity and callback replacement state");
    }
    private static void boundaries(final Player player,Native950Containers containers,EmbeddedChannel channel)throws Exception {
        reset(player);final Item coins=new Item(995,20);player.getInventory().items.set(17,coins);
        install(player,new Controller(){public void start(){}@Override public boolean canDeleteInventoryItem(int id,int amount){return false;}});
        require(!player.getBank().depositItem(17,20,false)&&player.getInventory().items.get(17)==coins,"Deposit ignored controller veto");
        install(player,new Controller(){public void start(){}@Override public boolean canDeleteInventoryItem(int id,int amount){coins.setAmount(19);return true;}});
        require(!player.getBank().depositItem(17,20,false)&&coins.getAmount()==19&&player.getBank().bankTabs[0].length==0,"Controller mutation consumed stale amount");
        install(player,null);
        for(int kind=0;kind<3;kind++) {
            reset(player);Item item=new Item(995,10);
            if(kind==0)item.setCharges(3);
            if(kind==1){ConcurrentHashMap<TemporaryAttributes.Key,Object> data=new ConcurrentHashMap<TemporaryAttributes.Key,Object>();data.put(TemporaryAttributes.Key.ITEM_INVENTION_DATA,"fixture");item.setAttributes(data);}
            if(kind==2)item.setInventionData(new InventionData(4321.5));
            player.getInventory().items.set(17,item);
            require(!player.getBank().depositItem(17,10,false)&&player.getInventory().items.get(17)==item,"Bank erased special item state");
            player.getInventory().items.clear();player.getBank().bankTabs=new Item[][]{{item}};
            require(!player.getBank().withdrawItem(0,10)&&player.getBank().bankTabs[0][0]==item,"Withdrawal erased special item state");
        }
        reset(player);player.getBank().bankTabs=new Item[][]{{new Item(1511,5)}};
        for(int slot=0;slot<28;slot++)player.getInventory().items.set(slot,new Item(526,1));
        require(!player.getBank().withdrawItem(0,5)&&player.getBank().bankTabs[0][0].getAmount()==5,"Full inventory lost items");
        player.getInventory().items.set(7,null);player.getInventory().items.set(23,null);
        require(player.getBank().withdrawItem(0,5)&&count(player,1511)==2&&player.getBank().bankTabs[0][0].getAmount()==3,"Partial capacity withdrawal did not preserve remainder");
        reset(player);player.getBank().bankTabs=new Item[][]{{new Item(995,20)}};
        install(player,new Controller(){public void start(){}@Override public boolean canAddInventoryItem(int id,int amount){return false;}});
        require(!player.getBank().withdrawItem(0,20)&&player.getBank().bankTabs[0][0].getAmount()==20,"Withdrawal ignored controller veto");
        install(player,null);reset(player);drain(channel);
    }
    private static int bankCount(Player player,int id){int total=0;for(Item item:player.getBank().bankTabs[0])if(item.getId()==id)total+=item.getAmount();return total;}
    private static void reset(Player player){player.getInventory().items.clear();player.getBank().bankTabs=new Item[][]{new Item[0]};player.getBank().lastContainerCopy=null;}
    private static int count(Player player,int id){int n=0;for(Item item:player.getInventory().items.getItems())if(item!=null&&item.getId()==id)n+=item.getAmount();return n;}
    private static void drain(EmbeddedChannel channel){channel.flush();Object value;while((value=channel.readOutbound())!=null)ReferenceCountUtil.release(value);channel.checkException();}
    private static void install(Player player,Controller controller)throws Exception {
        if(controller!=null)controller.setPlayer(player);
        Field field=ControlerManager.class.getDeclaredField("controler");field.setAccessible(true);field.set(player.getControlerManager(),controller);
        field=ControlerManager.class.getDeclaredField("inited");field.setAccessible(true);field.setBoolean(player.getControlerManager(),true);
    }
    private static void require(boolean ok,String reason){if(!ok)throw new AssertionError(reason);}
}