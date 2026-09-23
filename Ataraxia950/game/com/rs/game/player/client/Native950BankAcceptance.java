package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.ObjectStrategy;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Actual950 bank prediction -> encrypted input -> original ObjectHandler/Bank/Inventory proof. */
public final class Native950BankAcceptance {
    private static int frames,ticks,actions;
    private static final int EMPTY=48447;
    private Native950BankAcceptance() { }
    public static void main(String[] args)throws Exception {
        require(args.length==1,"Usage: Native950BankAcceptance <950-flat-cache-directory>");
        require(NativeCacheVerification.isEnforced(),"Cache verification must remain enabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        // Live appearance initializes this selected-cache table before a character can bank.
        com.rs.cache.loaders.BodyDefinitions.init();
        Native950BankUi.verify();
        Native950World.getInstance().execute(() -> {
            require(World.getPlayers().isEmpty(),"Acceptance requires an isolated ephemeral world");
            try(Fixture f=new Fixture()) {
                exhaustion(f);capturedCompactionClaims(f);quantities(f);capacity(f);withdrawX(f);bankControls(f);remoteBank(f);
                Native950Interactions.State state=f.input.snapshot();
                require(state.handlerFailures==0&&state.unhandledActions==0&&state.unmatchedPairs==0,
                        "Bank action failed or bypassed routing: "+state.routerReport);
            }
            try(Fixture f=new Fixture(36786)) {
                f.seed(0,new Item(199,2));f.button(0,199,2);
                require(f.amount(199)==1,"Ordinary Bank booth could not withdraw");
                f.control(39);require(f.amount(199)==0&&f.bankAmount(199)==2,"Ordinary Bank booth could not deposit");
                f.player.setLocation(f.player.getX()+20,f.player.getY()+20,0);World.updateEntityRegion(f.player);
                f.nextTick();f.button(0,199,2);
                require(f.amount(199)==0,"Physical bank retained remote authority after leaving reach");
                System.out.println("PASS: cache-name Bank booth route, deposit/withdraw and reach-scoped session");
            }
            require(World.getPlayers().isEmpty(),"Ephemeral bank player was not cleaned up");
            return null;
        }).get(90,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"Owner scheduler failed");
        System.out.println("PASS: "+actions+" encrypted950 bank/object/amount actions, "+ticks+" original movement/entity ticks, "+frames+" parsed encrypted frames; no login, listener or account save");
        System.out.println("LIMIT: proves paired-cache metadata, actor prediction, real bank handlers and encrypted container updates; visual menu rendering still needs the live client.");
    }

    private static void exhaustion(Fixture f) {
        f.seed(0,new Item(199,1),new Item(1511,10),new Item(201,1));
        // The current client has already cleared this actor by the time it sends the click.
        f.button(0,199,1);
        require(f.amount(199)==0&&f.bankAmount(199)==1,"A stale pre-prediction actor withdrew an exhausted slot");
        f.nextTick();f.button(0,EMPTY,1);
        require(f.amount(199)==1,"Default Withdraw1 could not exhaust the first herb");
        f.bank(new int[]{1511,201},new int[]{10,1});f.assertContainerFrames();
        System.out.println("PASS: first-slot grimy guam199 Withdraw1 accepts cleared actor48447 and retains following rows");

        f.seed(0,new Item(199,1),new Item(215,1),new Item(1511,10),new Item(201,1));
        f.button(1,EMPTY,2);
        require(f.amount(215)==1,"Reported single grimy kwuarm215 failed in a middle bank slot");
        f.bank(new int[]{199,1511,201},new int[]{1,10,1});f.assertContainerFrames();
        System.out.println("PASS: reported grimy kwuarm215 middle-slot exhaustion preserves both neighboring bank rows");

        f.seed(0,new Item(199,1),new Item(1511,10),new Item(201,1));
        f.button(2,EMPTY,2);
        require(f.amount(201)==1,"Final-slot grimy marrentill201 withdrawal failed");
        f.bank(new int[]{199,1511},new int[]{1,10});f.assertContainerFrames();

        f.seed(0,new Item(199,1),new Item(215,1),new Item(201,1));
        f.button(0,EMPTY,2);
        long committed=f.input.snapshot().transactions;
        // Both the original and replacement row would exhaust to the identical actor48447.
        f.button(0,EMPTY,2);
        require(f.amount(199)==1&&f.amount(215)==0&&f.input.snapshot().transactions==committed,
                "Duplicate same-tick actor48447 withdrew the next single-item row");
        f.bank(new int[]{215,201},new int[]{1,1});
        System.out.println("PASS: final-row exhaustion and same-tick duplicate protection when the replacement is another single herb");
    }

    private static Item[] capturedBankRows() {
        // Reported adjacent rows retain their exact indices; the unrelated prefix is synthetic.
        return new Item[]{new Item(995,1000),new Item(1511,80),new Item(199,1),new Item(201,1),
                new Item(436,10),new Item(438,10),new Item(211,1),new Item(215,1),new Item(24000,1),
                new Item(1040,1),new Item(554,50),new Item(20000,1),new Item(62789,1)};
    }

    private static void capturedCompactionClaims(Fixture f) {
        for(int[] row:new int[][]{{6,211,215},{7,215,24000},{11,20000,62789}}) {
            f.seed(0,capturedBankRows());
            require(!f.player.getBank().getWithdrawNotes(),"Captured regression requires normal-item mode");
            f.control(114);f.count(2); // Actual failing session used default X2 with one item remaining.
            require(f.player.getBank().getDefaultInteractionAmount()==2,"Captured defaultX2 was not selected");
            long committed=f.input.snapshot().transactions;
            f.button(row[0],row[2],1);f.assertContainerFrames();
            require(f.amount(row[1])==0&&f.amount(row[2])==0&&f.bankAmount(row[1])==1
                    &&f.bankAmount(row[2])==1&&f.input.snapshot().transactions==committed,
                    "Captured next-row actor was accepted as the selected bank item");
            f.nextTick();f.button(row[0],EMPTY,1);
            require(f.amount(row[1])==1&&f.amount(row[2])==0&&f.bankAmount(row[1])==0
                    &&f.bankAmount(row[2])==1,"Correct modal-bank exhaustion failed or withdrew the next row");
            f.assertContainerFrames();
        }
        f.control(93);
        System.out.println("PASS: captured slot6 211->215, slot7 215->24000 and slot11 20000->62789 claims cannot select neighboring rows; modal-bank X2 exhaustion transfers the intended single item");
    }

    private static void quantities(Fixture f) {
        f.seed(0,new Item(199,1),new Item(1511,10),new Item(201,1));
        f.button(1,1511,2);
        require(f.amount(1511)==1&&f.bankAmount(1511)==9,"Partial Withdraw1 must retain actor1511");
        f.assertContainerFrames();
        f.nextTick();f.button(1,1511,3);
        require(f.amount(1511)==6&&f.bankAmount(1511)==4,"Partial Withdraw5 moved wrong quantity");
        f.nextTick();f.button(1,EMPTY,7);
        require(f.amount(1511)==10&&f.bankAmount(1511)==0,"WithdrawAll failed to clear the middle stack");
        f.bank(new int[]{199,201},new int[]{1,1});f.assertContainerFrames();
        f.seed(0,new Item(199,1),new Item(1511,3),new Item(201,1));
        f.button(1,EMPTY,3);
        require(f.amount(1511)==3&&f.bankAmount(1511)==0,"Withdraw5 must cap at the smaller existing stack");
        f.bank(new int[]{199,201},new int[]{1,1});
        System.out.println("PASS: partial1, partial5, all, and stock-capped5 use real actor while partial and48447 only on exhaustion");
    }

    private static void capacity(Fixture f) {
        f.seed(28,new Item(199,1),new Item(1511,10),new Item(201,1));
        long committed=f.input.snapshot().transactions;
        f.button(0,199,2);
        require(f.amount(199)==0&&f.bankAmount(199)==1&&f.input.snapshot().transactions==committed,
                "Full inventory consumed a single bank herb");
        f.bank(new int[]{199,1511,201},new int[]{1,10,1});f.assertContainerFrames();
        f.nextTick();f.button(0,EMPTY,2);
        require(f.amount(199)==0&&f.bankAmount(199)==1,"Forged cleared actor bypassed full inventory");
        f.seed(25,new Item(199,1),new Item(1511,10),new Item(201,1));
        f.button(1,1511,7);
        require(f.amount(1511)==3&&f.bankAmount(1511)==7,"Capacity-limited WithdrawAll did not stop at three free slots");
        f.bank(new int[]{199,1511,201},new int[]{1,7,1});f.assertContainerFrames();
        System.out.println("PASS: full inventory leaves exact bank rows untouched; capacity-limited All keeps real actor and moves only three logs");
    }

    private static void withdrawX(Fixture f) {
        f.seed(0,new Item(199,1),new Item(1511,10),new Item(201,1));
        f.button(1,1511,6);
        require(f.player.getInterfaceManager().containsInterface(1469),"Withdraw-X did not open the paired950 quantity input");
        require(f.bankAmount(1511)==10&&f.amount(1511)==0,"Opening X prompt consumed bank items");
        f.count(3);
        require(f.amount(1511)==3&&f.bankAmount(1511)==7,"Encrypted64bit Withdraw-X amount3 did not use original Bank.withdrawItem");
        require(!f.player.getInterfaceManager().containsInterface(1469),"Withdraw-X prompt ownership survived successful response");
        f.assertContainerFrames();
        f.count(3);
        require(f.amount(1511)==3&&f.bankAmount(1511)==7,"Duplicate quantity reply withdrew again");
        f.nextTick();f.button(1,1511,6);f.count(999);
        require(f.amount(1511)==10&&f.bankAmount(1511)==0,"Withdraw-X did not cap at remaining stock");
        f.bank(new int[]{199,201},new int[]{1,1});

        f.seed(0,new Item(199,1),new Item(1511,10),new Item(201,1));
        f.button(1,1511,6);f.count(-1);
        require(f.bankAmount(1511)==10&&f.amount(1511)==0,"Negative signed64bit quantity mutated a bank row");
        require(!f.player.getInterfaceManager().containsInterface(1469),"Invalid amount did not retire the prompt");
        System.out.println("PASS: exact-ID Withdraw-X prompt, encrypted signed64bit response, partial/stock cap, retired duplicates and negative rejection");
    }

    private static void bankControls(Fixture f) {
        f.seed(0,new Item(1511,80),new Item(199,1),new Item(995,1000));
        f.control(96);
        require(f.player.getBank().getDefaultInteractionAmount()==5,"Quantity5 did not change server mode");
        f.button(0,1511,1);
        require(f.amount(1511)==5&&f.bankAmount(1511)==75,"Selected5 did not drive default withdrawal");
        f.nextTick();f.control(127);f.control(103);f.button(0,EMPTY,1);
        require(f.amount(1512)==75&&f.bankAmount(1511)==0,"Notes All did not use cache note ID/stack capacity");
        f.assertContainerFrames();
        require(f.player.getBank().getWithdrawNotes(),"Note toggle lost authority");
        System.out.println("PASS: restored5/All selectors, notes, output-ID prediction and container feedback");

        // Deposit-X aggregates ordinary unstackable slots and unnnotes through the same cache path.
        f.nextTick();f.button(15,0,1511,6);f.count(3);
        require(f.amount(1511)==2&&f.bankAmount(1511)==3,"Deposit-X did not aggregate3 separate log slots");
        require(f.player.getBank().getLastX()==3,"Deposit-X did not retain the entered quantity");
        f.nextTick();f.button(15,5,-1,7);
        require(f.amount(1512)==0&&f.bankAmount(1511)==78,"DepositAll notes did not return base logs");
        f.control(39);
        require(f.amount(1511)==0&&f.bankAmount(1511)==80,"Deposit inventory button was intercepted or incomplete");

        f.control(114);f.count(37);
        require(f.player.getBank().getNativeDefaultInteractionAmount()==11&&f.player.getBank().getLastX()==37,
                "Custom quantity did not preserve modeX separately from37");
        int logSlot=f.bankSlot(1511);f.nextTick();f.button(logSlot,1511,1);
        require(f.amount(1512)==37&&f.bankAmount(1511)==43,"Default custom37 failed noted withdrawal");
        f.nextTick();f.button(logSlot,1511,5);
        require(f.amount(1512)==74&&f.bankAmount(1511)==6,"Saved-X menu operation5 failed");
        f.nextTick();f.button(logSlot,1511,6);f.control(127);f.count(2);
        require(f.amount(1512)==74&&f.bankAmount(1511)==6,"Changing note mode reinterpreted a pending quantity");
        f.control(237);
        require(!f.hasContainer(93)&&!f.hasContainer(95),"Search click refreshed containers and disturbed native keyboard focus");
        f.nextTick();f.button(logSlot,1511,2);
        require(f.amount(1511)==1&&f.bankAmount(1511)==5,"Search route changed authoritative bank slot identity");
        System.out.println("PASS: Deposit-X, unnoting, deposit inventory, custom37, saved-X, stale mode cancellation and local search");

        f.control(127);
        Native950Interactions.State beforeClose=f.input.snapshot();
        // The current cache sends CLOSE_MODAL before the Close button's IF_BUTTON1.
        f.closeModal();f.assertBankClosedOnce();
        require(!f.input.snapshot().bankOpen&&!f.input.router().bankInterfaceOpen(),
                "Encrypted CLOSE_MODAL did not retire the bank");
        Native950Interactions.State closed=f.input.snapshot();
        for(int duplicate=0;duplicate<2;duplicate++) {
            f.control(317);
            Native950Interactions.State afterClose=f.input.snapshot();
            require(!afterClose.bankOpen&&!f.input.router().bankInterfaceOpen(),"Trailing Close reopened the bank");
            require(afterClose.rejectedActions==closed.rejectedActions&&afterClose.routerRejections==closed.routerRejections,
                    "Trailing or duplicate Close was rejected after CLOSE_MODAL");
            require(afterClose.transactions==beforeClose.transactions&&sameItems(beforeClose.inventory,afterClose.inventory)
                    &&sameItems(beforeClose.bank,afterClose.bank)&&sameItems(beforeClose.equipment,afterClose.equipment),
                    "Closing the bank changed an item container or committed a transaction");
            require(!f.hasFrame(ServerPacket.MESSAGE_GAME),"Trailing or duplicate Close emitted an error message");
        }
        System.out.println("PASS: encrypted CLOSE_MODAL5 followed by Close317 and duplicate317 is silent, idempotent and conserves every container");
        require(f.player.getBank().getWithdrawNotes()&&f.player.getBank().getLastX()==37
                &&f.player.getBank().getNativeDefaultInteractionAmount()==11,"Bank close reset saved preferences");
        f.seed(0,new Item(1511,10));
        require(f.player.getBank().getWithdrawNotes()&&f.player.getBank().getLastX()==37
                &&f.player.getBank().getNativeDefaultInteractionAmount()==11,"Bank reopen reset saved preferences");
        f.player.getEquipment().getItems().set(3,new Item(1277,1));
        f.nextTick();f.nextTick();f.control(42);
        require(f.player.getEquipment().getItems().get(3)==null&&f.bankAmount(1277)==1,
                "Deposit equipment did not move the actual equipped sword");
        f.control(96);f.control(114);f.escape();
        require(!f.player.getInterfaceManager().containsInterface(1469)
                &&f.player.getBank().getDefaultInteractionAmount()==5
                &&f.hasPacket(Native950Packets.varbitSmall(45189,3)),"Escape did not restore the authoritative quantity5");
        f.count(99);require(f.player.getBank().getLastX()==37,"Escape left a quantity request active");
        f.button(68,65535,-1,2);
        require(f.hasPacket(Native950Packets.varbitSmall(45191,0))
                &&f.hasPacket(Native950Packets.varbitSmall(45139,0)),"Unsupported secondary preset action left a predicted unsupported mode active");
        f.control(106);f.control(114);f.control(317);f.assertBankClosedOnce();f.count(100);
        require(f.player.getBank().getLastX()==37,"Closed default-quantity prompt accepted a stale reply");
        System.out.println("PASS: Close/reopen preserves preferences, equipment deposit uses shared gear/bank, close retires custom input");
    }

    private static void remoteBank(Fixture f) {
        f.player.getBank().openBank();
        f.input.allowRemoteBank();
        f.nextTick();
        require(f.input.snapshot().bankOpen,"Remote bank command did not open the native bank");
        int before=f.bankAmount(199);
        f.player.getInventory().items.set(0,new Item(199,1));
        f.nextTick();f.control(39);
        require(f.amount(199)==0&&f.bankAmount(199)==before+1,
                "Remote bank rejected a valid deposit because no chest/banker was in reach");
        f.control(317);f.assertBankClosedOnce();
        System.out.println("PASS: command-opened remote bank accepts deposits and clears its remote scope on close");
    }

    private static boolean sameItems(Native950Containers.Snapshot first,Native950Containers.Snapshot second) {
        return Arrays.equals(first.ids,second.ids)&&Arrays.equals(first.amounts,second.amounts);
    }

    private static final class Fixture implements AutoCloseable {
        final int[] incoming={9,5,0,2},outgoing={59,55,50,52};
        final Native950Isaac clientCipher=new Native950Isaac(incoming),cipher=new Native950Isaac(outgoing);
        final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(incoming),new Native950Isaac(outgoing),Thread.currentThread());
        final EmbeddedChannel channel=new EmbeddedChannel(transport);
        final List<Frame> output=new ArrayList<Frame>();
        final Player player;
        final Native950Interactions input;
        final WorldObject chest;
        Fixture() {this(79036);}
        Fixture(int bankId) {
            player=Player.createNative950("bankuitest",new WorldTile(3217,3258,0),channel);
            player.setActive(true);player.setRunning(true);Native950World.installVarpSink(player);
            World.addNative950Player(player,1);World.updateEntityRegion(player);
            player.loadMapRegions();player.setClientHasLoadedMapRegion();
            Native950ItemCatalog catalog=new Native950ItemCatalog(Collections.<Native950ItemCatalog.Entry>emptyList()).withLegacyDrops();
            for(int id:new int[]{199,201,215,1511,526})require(catalog.get(id)!=null,"Actual950 item metadata missing: "+id);
            input=new Native950Interactions(player,channel,new Native950Content(catalog,bankUi()),null,null);
            input.bootstrap();chest=placeChest(bankId);drain();output.clear();
        }
        private WorldObject placeChest(int bankId) {
            for(int y=3255;y<3290;y++)for(int x=3205;x<3250;x++) {
                WorldObject candidate=new WorldObject(bankId,10,0,x,y,0);
                require(Native950PhysicalBanks.accepts(candidate,2),"Actual950 physical bank option2 is not supported");
                boolean clear=true;
                for(int dx=-1;dx<=candidate.getDefinitions().sizeX;dx++)
                    for(int dy=-1;dy<=candidate.getDefinitions().sizeY;dy++)
                        if(!World.isFloorFree(0,x+dx,y+dy,1))clear=false;
                if(!clear)continue;
                World.spawnObject(candidate);
                for(int dx=-1;dx<=candidate.getDefinitions().sizeX;dx++)
                    for(int dy=-1;dy<=candidate.getDefinitions().sizeY;dy++) {
                        if(!World.isFloorFree(0,x+dx,y+dy,1))continue;
                        player.setLocation(x+dx,y+dy,0);World.updateEntityRegion(player);player.resetMasks();
                        if(RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,player.getX(),player.getY(),0,1,
                                new ObjectStrategy(candidate),false)==0)return candidate;
                    }
                World.removeObject(candidate);
            }
            throw new AssertionError("Could not place reachable950 bank chest on actual collision map");
        }
        void seed(int filledSlots,Item... rows) {
            input.walking();player.resetWalkSteps();player.setRouteEvent(null);player.resetMasks();
            for(int slot=0;slot<28;slot++)player.getInventory().items.set(slot,slot<filledSlots?new Item(526,1):null);
            player.getBank().bankTabs=new Item[][]{rows};
            player.getBank().setCurrentTab(0);
            nextTick();output.clear();
            ByteBuf bytes=Unpooled.buffer(10);
            bytes.writeByte((48+clientCipher.getAsInt())&255);
            bytes.writeByte(chest.getY()+128);bytes.writeByte(chest.getY()>>>8);bytes.writeInt(chest.getId());
            bytes.writeByte(chest.getX()>>>8);bytes.writeByte(chest.getX()+128);bytes.writeByte(0);
            send(bytes);
            for(int i=0;i<20&&!input.snapshot().bankOpen;i++)nextTick();
            require(input.snapshot().handlerFailures==0,"Chest handler failed before bank admission: "+input.snapshot().routerReport);
            require(input.snapshot().bankOpen&&input.router().bankInterfaceOpen(),
                    "Actual chest Use/route/Bank.openBank did not open: "+input.snapshot().routerReport);
            assertBankMountedOnce();assertBankPublicationOrder();
            nextTick();output.clear();
        }
        void nextTick() {
            ticks++;input.beginTick();player.processEntity();player.processEntityUpdate();input.afterMovement();
            channel.write(Native950Packets.tickEnd());drain();player.resetMasks();
        }
        void control(int component) { button(component,65535,-1,1); }
        int bankSlot(int id) {
            Item[] rows=player.getBank().bankTabs[0];
            for(int i=0;i<rows.length;i++)if(rows[i].getId()==id)return i;
            throw new AssertionError("Missing bank item "+id);
        }
        void button(int slot,int actor,int option) { button(201,slot,actor,option); }
        void button(int component,int slot,int actor,int option) {
            int[] opcodes={18,122,89,100,81,126,49,66,31,59};int hash=(517<<16)|component;
            ByteBuf bytes=Unpooled.buffer(10);
            bytes.writeByte((opcodes[option-1]+clientCipher.getAsInt())&255);bytes.writeMedium(actor);
            bytes.writeByte(hash>>>16);bytes.writeByte(hash>>>24);bytes.writeByte(hash);bytes.writeByte(hash>>>8);
            bytes.writeShort(slot);output.clear();send(bytes);
        }
        boolean hasPacket(Native950Packets.Packet packet) {
            for(Frame frame:output)if(frame.kind==packet.type()&&Arrays.equals(frame.body,packet.payload()))return true;
            return false;
        }
        boolean hasFrame(ServerPacket packet) {
            for(Frame frame:output)if(frame.kind==packet)return true;
            return false;
        }
        void closeModal() {
            ByteBuf bytes=Unpooled.buffer(1);bytes.writeByte((5+clientCipher.getAsInt())&255);
            output.clear();send(bytes);
        }
        void escape() {
            ByteBuf bytes=Unpooled.buffer(1);bytes.writeByte((11+clientCipher.getAsInt())&255);
            output.clear();send(bytes);
        }
        void count(long count) {
            ByteBuf bytes=Unpooled.buffer(9);bytes.writeByte((120+clientCipher.getAsInt())&255);bytes.writeLong(count);
            output.clear();send(bytes);
        }
        void send(ByteBuf bytes) {
            channel.writeInbound(bytes);channel.runPendingTasks();
            require(transport.drainActions(action -> {actions++;input.handle(action);})==1,"Encrypted bank input did not reach exactly one original interaction");
            drain();
        }
        int amount(int id){return player.getInventory().items.getNumberOf(id);}
        int bankAmount(int id){int total=0;for(Item item:player.getBank().bankTabs[0])if(item.getId()==id)total+=item.getAmount();return total;}
        void bank(int[] ids,int[] amounts) {
            Item[] rows=player.getBank().bankTabs[0];require(rows.length==ids.length,"Bank row count changed unexpectedly");
            for(int slot=0;slot<rows.length;slot++)require(rows[slot].getId()==ids[slot]&&rows[slot].getAmount()==amounts[slot],
                    "Bank row "+slot+" changed: expected "+ids[slot]+"x"+amounts[slot]+" got "+rows[slot].getId()+"x"+rows[slot].getAmount());
        }
        void assertBankMountedOnce() {
            int mounts=0,modalMounts=0,childMounts=0;
            for(Frame frame:output)if(frame.kind==ServerPacket.IF_OPENSUB) {
                require(frame.body.length==23,"Malformed IF_OPENSUB in bank opening recipe");
                int interfaceId=((frame.body[16]-128)&255)|((frame.body[17]&255)<<8);
                if(interfaceId!=517)continue;
                mounts++;
                int parent=((frame.body[0]&255)<<24)|((frame.body[1]&255)<<16)
                        |((frame.body[2]&255)<<8)|(frame.body[3]&255);
                if(parent==((1477<<16)|693))modalMounts++;
                if(parent==((1477<<16)|695))childMounts++;
            }
            require(mounts==1&&modalMounts==1&&childMounts==0,
                    "Expected one modal bank mount at693 and none at695: total="+mounts+" modal="+modalMounts+" child="+childMounts);
            com.rs.game.player.content.InterfaceManager manager=player.getInterfaceManager();
            require(manager.getInterfaceParentId(517)==((1477<<16)|693)&&manager.containsBankInterface()
                    &&manager.containsInterfaceIn(com.rs.game.player.content.InterfaceManager.MainInterfaceComponents.BANK)
                    &&manager.getInterfaceIdIn(com.rs.game.player.content.InterfaceManager.MainInterfaceComponents.BANK)==517
                    &&!manager.containsInterfaceAtParent(1477,695),"Bank bookkeeping does not match its sole native host");
        }
        void assertBankClosedOnce() {
            int modalCloses=0,childCloses=0;
            for(Frame frame:output)if(frame.kind==ServerPacket.IF_CLOSESUB) {
                require(frame.body.length==4,"Malformed IF_CLOSESUB in bank closing recipe");
                int parent=((frame.body[1]&255)<<24)|((frame.body[0]&255)<<16)
                        |((frame.body[3]&255)<<8)|(frame.body[2]&255);
                if(parent==((1477<<16)|693))modalCloses++;
                if(parent==((1477<<16)|695))childCloses++;
            }
            require(modalCloses==1&&childCloses==0,"Bank close was duplicated or targeted the wrong child");
            com.rs.game.player.content.InterfaceManager manager=player.getInterfaceManager();
            require(manager.getInterfaceParentId(517)==-1&&!manager.containsBankInterface()
                    &&!manager.containsInterfaceIn(com.rs.game.player.content.InterfaceManager.MainInterfaceComponents.BANK)
                    &&manager.getInterfaceIdIn(com.rs.game.player.content.InterfaceManager.MainInterfaceComponents.BANK)==-1,
                    "Closed bank retained native bookkeeping");
        }
        private static boolean matches(Frame frame,Native950Packets.Packet packet) {
            return frame.kind==packet.type()&&Arrays.equals(frame.body,packet.payload());
        }
        void assertBankPublicationOrder() {
            Native950Containers.Snapshot state=input.snapshot().bank;
            int occupied=0,publications=0;
            for(int id:state.ids)if(id>=0)occupied++;
            Native950Packets.Packet clear=Native950Packets.varp(8970,-1);
            Native950Packets.Packet span=Native950Packets.varp(8971,occupied);
            Native950Packets.Packet items=Native950Packets.inventoryFull(95,false,state.ids,state.amounts);
            for(int index=0;index<output.size();index++) {
                Frame frame=output.get(index);
                if(frame.kind!=ServerPacket.UPDATE_INV_FULL||frame.body.length<5
                        ||(((frame.body[0]&255)<<8)|(frame.body[1]&255))!=95)continue;
                // Legacy partial-span refreshes may precede the adapter's full600-slot publication.
                if((((frame.body[3]&255)<<8)|(frame.body[4]&255))!=state.ids.length)continue;
                publications++;
                require(matches(frame,items),"Authoritative compact bank publication differs from shared container");
                require(index>=2&&matches(output.get(index-2),clear)&&matches(output.get(index-1),span),
                        "Compact bank publication must follow8970=-1 then8971=occupied before container95");
            }
            require(publications>0,"Missing authoritative compact bank publication");
        }
        void assertContainerFrames() {
            require(hasContainer(93)&&hasContainer(95),"Transfer/rejection failed to refresh both authoritative950 containers");
            assertBankPublicationOrder();
        }
        boolean hasContainer(int id) {
            for(Frame frame:output)if(frame.kind==ServerPacket.UPDATE_INV_FULL&&frame.body.length>=5
                    &&(((frame.body[0]&255)<<8)|(frame.body[1]&255))==id)return true;
            return false;
        }
        void drain() {
            channel.flush();channel.runPendingTasks();Object message;
            while((message=channel.readOutbound())!=null)try {
                require(message instanceof ByteBuf,"950 bank transport emitted nonbytes");ByteBuf bytes=(ByteBuf)message;
                while(bytes.isReadable()) {
                    int opcode=(bytes.readUnsignedByte()-cipher.getAsInt())&255;
                    if(opcode>=128)opcode=((opcode-128)<<8)|((bytes.readUnsignedByte()-cipher.getAsInt())&255);
                    ServerPacket kind=null;for(ServerPacket row:ServerPacket.values())if(row.opcode()==opcode){kind=row;break;}
                    require(kind!=null,"Unknown950 bank opcode or cipher mismatch "+opcode);
                    int length=kind.size();if(length==-1)length=bytes.readUnsignedByte();else if(length==-2)length=bytes.readUnsignedShort();
                    require(length>=0&&length<=bytes.readableBytes(),"950 bank packet length mismatch "+kind);
                    byte[] body=new byte[length];bytes.readBytes(body);output.add(new Frame(kind,body));frames++;
                }
            } finally {ReferenceCountUtil.release(message);}
            channel.checkException();require(channel.isActive()&&transport.terminalFailure()==null,"950 bank transport disconnected");
        }
        @Override public void close() {
            input.close();World.removeObject(chest);World.removeNative950Player(player);channel.finishAndReleaseAll();
        }
    }

    /** Same native operation amounts and paired-cache opening recipe as Native950CacheContent.bankUi. */
    private static Native950Content.BankUi bankUi() {
        int[] deposits=new int[11],withdrawals=new int[11];
        for(int[] amounts:new int[][]{deposits,withdrawals}) {
            amounts[1]=1;amounts[2]=1;amounts[3]=5;amounts[4]=10;amounts[7]=Integer.MAX_VALUE;
        }
        List<Native950Packets.Packet> open=new ArrayList<>(Arrays.asList(
                Native950Packets.varbitSmall(45141,1),Native950Packets.varbitSmall(45158,1),
                Native950Packets.openSub(1477,693,517,false),Native950Packets.hideInterface(1477,693,false)));
        open.addAll(Native950BankUi.openControls());
        return new Native950Content.BankUi(517,201,15,317,39,deposits,withdrawals,open,
                Arrays.asList(Native950Packets.closeSub(1477,693),Native950Packets.hideInterface(1477,693,true)),6);
    }
    private static final class Frame {
        final ServerPacket kind;final byte[] body;
        Frame(ServerPacket kind,byte[] body){this.kind=kind;this.body=body;}
    }
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
}
