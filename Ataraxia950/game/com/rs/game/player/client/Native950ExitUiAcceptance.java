package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.controllers.Controller;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.utils.Utils;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/** Native menu/action/guard checks plus a real session's final checkpoint in a fresh temp profile. */
public final class Native950ExitUiAcceptance {
    private static int checks;
    private static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
    private static Native950Actions.InterfaceAction click(int iface,int comp,int slot,int item,int option){
        int hash=iface<<16|comp;
        return (Native950Actions.InterfaceAction)Native950Actions.decode(option==1?18:122,
                new byte[]{(byte)(item>>16),(byte)(item>>8),(byte)item,(byte)(hash>>16),
                (byte)(hash>>24),(byte)hash,(byte)(hash>>8),(byte)(slot>>8),(byte)slot});
    }
    private static Native950Actions.InterfaceAction click(int comp){return click(1433,comp,-1,-1,1);}
    private static Native950Actions.InterfaceAction entry(){return click(1477,99,1,-1,1);}
    private static boolean packet(EmbeddedChannel ch,Native950Packets.Packet expected){
        ch.flush();Object o;boolean found=false;
        while((o=ch.readOutbound())!=null){
            if(o instanceof Native950Packets.Packet){Native950Packets.Packet p=(Native950Packets.Packet)o;
                found|=p.type()==expected.type()&&Arrays.equals(p.payload(),expected.payload());}
            ReferenceCountUtil.release(o);
        }
        return found;
    }
    private static int packetIndex(List<Native950Packets.Packet> packets,Native950Packets.Packet expected){
        for(int i=0;i<packets.size();i++){
            Native950Packets.Packet p=packets.get(i);
            if(p.type()==expected.type()&&Arrays.equals(p.payload(),expected.payload()))return i;
        }
        return -1;
    }
    private static void checkOpeningRecipe(EmbeddedChannel ch){
        ch.flush();Object o;List<Native950Packets.Packet> packets=new ArrayList<>();
        while((o=ch.readOutbound())!=null){
            if(o instanceof Native950Packets.Packet)packets.add((Native950Packets.Packet)o);
            ReferenceCountUtil.release(o);
        }
        Native950Packets.Packet[] ordered={Native950Packets.openSub(1477,806,1433,true),
            Native950Packets.runClientScript(8177),Native950Packets.hideInterface(1477,805,false),
            Native950Packets.hideInterface(1477,806,false),Native950Packets.hideInterface(1433,0,false),
            Native950Packets.runClientScript(13831,1)};
        int previous=-1;
        for(int i=0;i<ordered.length;i++){
            int index=packetIndex(packets,ordered[i]);
            check(index>previous,"Missing or reordered Exit root/layout step "+i);previous=index;
        }
        check(packetIndex(packets,Native950Packets.hideInterface(1477,808,true))<0
            &&packetIndex(packets,Native950Packets.hideInterface(1477,809,true))<0,
            "Exit hid unowned sibling input layers");
    }
    private static void checkLayoutRecipe(EmbeddedChannel ch,boolean opening){
        ch.flush();Object o;List<Native950Packets.Packet> packets=new ArrayList<>();
        while((o=ch.readOutbound())!=null){
            if(o instanceof Native950Packets.Packet)packets.add((Native950Packets.Packet)o);
            ReferenceCountUtil.release(o);
        }
        Native950Packets.Packet mount=opening?Native950Packets.openSub(1477,692,1475,true)
            :Native950Packets.closeSub(1477,692);
        Native950Packets.Packet mode=Native950Packets.varcSmall(3477,opening?1:0);
        int mountIndex=packetIndex(packets,mount),modeIndex=packetIndex(packets,mode);
        check(mountIndex>=0&&modeIndex>=0,"Native layout editor omitted its mount or edit-mode state");
        if(opening)check(mountIndex<modeIndex,"Native layout editor entered edit mode before mounting its UI");
        else check(modeIndex<mountIndex,"Native layout editor closed its UI before leaving edit mode");
    }
    public static void main(String[] args)throws Exception{
        if(args.length!=1)throw new IllegalArgumentException("Usage: Native950ExitUiAcceptance <paired950-cache>");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World world=Native950World.getInstance();
        world.execute(()->{unit();return null;}).get(90,TimeUnit.SECONDS);
        persistence(world);
        System.out.println("PASS exit menu: "+checks+" cache, native action, lifecycle, logout restriction and durable session checks.");
    }
    private static void unit(){
        Native950ExitUi.verify();
        EmbeddedChannel ch=new EmbeddedChannel();
        Player p=Player.createNative950("exit-ui-probe",new WorldTile(3217,3258,0),ch);
        p.setActive(true);p.setIndex(1);
        int[] calls={0};boolean[] lobby={false};
        Native950ExitUi ui=new Native950ExitUi(p,ch,Native950ExitUi::verify,toLobby->{calls[0]++;lobby[0]=toLobby;});
        try{
            check(!Native950ExitUi.isOpenRequest(click(1465,99,1,-1,1)),"Minimap content incorrectly owns exit actor");
            check(!Native950ExitUi.isOpenRequest(click(1477,99,0,-1,1)),"Invalid exit actor accepted");
            check(!Native950ExitUi.isOpenRequest(click(1477,99,1,995,1)),"Item-bearing exit click accepted");
            check(!Native950ExitUi.isOpenRequest(click(1477,99,1,-1,2)),"Other operation accepted");
            p.getInterfaceManager().registerNativeOpen(1433,1477,751);
            ui.bootstrap();check(packet(ch,Native950Packets.closeSub(1477,751)),"Old options mount was not removed at bootstrap");
            check(!p.getInterfaceManager().containsInterface(1433),"Old options ownership survived bootstrap");
            p.getInterfaceManager().registerNativeOpen(1433,1477,807);ui.bootstrap();
            check(p.getInterfaceManager().getInterfaceParentId(1433)==(1477<<16|807),"Bootstrap removed another options owner");
            p.getInterfaceManager().unregisterNativeOpen(1433);
            check(ui.handle(entry())&&ui.isOpen(),"Exit entry did not open options");
            checkOpeningRecipe(ch);
            check(p.getInterfaceManager().containsInterface(1433),"Opened options were not owned");
            check(ui.consumeOpeningCloseAcknowledgement(),"Opening acknowledgement was not fenced");
            check(ui.consumeOpeningCloseAcknowledgement(),"Repeated opening acknowledgement closed the exit overlay");
            check(ui.isOpen(),"Repeated opening acknowledgement did not retain the exit overlay");
            ui.handle(click(22));
            check(!ui.isOpen()&&ui.isLayoutEditing(),"Edit Layout did not replace Options with the native editor");
            checkLayoutRecipe(ch,true);
            check(p.getInterfaceManager().getInterfaceParentId(1475)==(1477<<16|692),"Native layout editor ownership was not registered");
            check(ui.consumeLayoutEditorClose()&&!ui.isLayoutEditing(),"Native layout editor close was not reconciled");
            check(packet(ch,Native950Packets.varcSmall(3477,0)),"Native layout editor close did not leave edit mode");
            check(!p.getInterfaceManager().containsInterface(1475),"Native layout editor ownership survived client close");
            check(!ui.consumeLayoutEditorClose(),"Stale native layout editor close was consumed twice");
            ui.handle(entry());
            ui.handle(click(22));
            ui.close();
            check(!ui.isLayoutEditing(),"Server close did not retire the native layout editor");
            checkLayoutRecipe(ch,false);
            ui.handle(entry());
            ui.handle(click(86));check(calls[0]==0,"Unarmed confirmation logged out");
            ui.handle(click(72));check(packet(ch,Native950Packets.hideInterface(1433,62,false)),"Logout does not show native confirmation");
            ui.handle(click(89));ui.handle(click(86));check(calls[0]==0&&p.isActive(),"Cancel did not retire confirmation");
            ui.handle(click(1433,69,2,-1,1));ui.handle(click(1433,69,-1,995,1));
            check(calls[0]==0,"Forged slot or item logout accepted");
            ui.handle(click(72));p.lock(3);ui.handle(click(86));check(calls[0]==0&&ui.isOpen(),"Lock bypassed logout");
            p.unlock();p.setAttackedByDelay(Utils.currentTimeMillis());
            ui.handle(click(86));check(calls[0]==0,"Recent incoming combat bypassed logout");
            p.setAttackedByDelay(0);p.setAttackingDelay(Utils.currentTimeMillis());
            ui.handle(click(86));check(calls[0]==0,"Recent outgoing combat bypassed logout");
            p.setAttackingDelay(0);p.setNextWorldTile(new WorldTile(3218,3258,0));
            ui.handle(click(86));check(calls[0]==0,"Pending teleport bypassed logout");
            p.setNextWorldTile(null);
            p.addWalkSteps(3218,3258,4,false);
            p.getControlerManager().startControler(new Controller(){public void start(){}public boolean canLogout(){return false;}});
            ui.handle(click(86));check(calls[0]==0&&p.hasWalkSteps()&&p.isActive(),"Controller veto consumed route or logged out");
            p.getControlerManager().forceStop();
            ui.handle(click(69));check(calls[0]==0&&p.isActive(),"Unimplemented lobby entry silently logged out");
            check(!ui.handle(click(15)),"Exit helper swallowed another menu owner's entry");
            for(int local:new int[]{43,66}){ui.handle(click(local));check(!ui.isOpen()&&p.isActive()&&calls[0]==0,"Local menu explanation failed or disconnected: "+local);ui.handle(entry());}
            ui.handle(click(1477,8,-1,-1,1));
            check(!ui.isOpen()&&!p.getInterfaceManager().containsInterface(1433),"Escape did not close owned options");
            check(packet(ch,Native950Packets.closeSub(1477,806)),"Close used wrong mount");
            check(!ui.handle(click(86))&&calls[0]==0,"Stale closed-menu click logged out");
            ui.close();check(!packet(ch,Native950Packets.runClientScript(8179)),"Stale close disturbed another modal");
            ui.handle(entry());packet(ch,Native950Packets.runClientScript(8177));
            p.getInterfaceManager().registerNativeOpen(1433,1477,807);ui.close();
            check(!ui.isOpen()&&p.getInterfaceManager().getInterfaceParentId(1433)==(1477<<16|807)
                &&!packet(ch,Native950Packets.runClientScript(8179)),"Replaced options ownership was closed by a stale owner");
            p.getInterfaceManager().unregisterNativeOpen(1433);
            ui.handle(entry());ui.handle(click(72));ui.handle(click(86));
            check(calls[0]==1&&!lobby[0]&&!p.isActive()&&ui.isSigningOut()&&!ui.isOpen(),"Confirmed logout not exactly once and inactive");
            check(!p.hasWalkSteps(),"Accepted logout retained walking");
            ui.handle(entry());ui.handle(click(86));ui.handle(click(86));check(calls[0]==1,"Duplicate logout request repeated transport");
        }finally{p.getControlerManager().forceStop();ui.close();ch.finishAndReleaseAll();}
        EmbeddedChannel failed=new EmbeddedChannel();
        Player q=Player.createNative950("exit-pin-probe",new WorldTile(3217,3258,0),failed);q.setActive(true);
        Native950ExitUi invalid=new Native950ExitUi(q,failed,()->{throw new IllegalStateException("pin mismatch");},toLobby->{throw new AssertionError("transport reached");});
        try{
            boolean refused=false;try{invalid.handle(entry());}catch(IllegalStateException expected){refused=true;}
            check(refused&&!invalid.isOpen()&&q.isActive()&&failed.readOutbound()==null,"Failed cache verification mutated exit state or wrote packets");
        }finally{failed.finishAndReleaseAll();}
    }
    private static Native950Content content(){
        return new Native950Content(new Native950ItemCatalog(Collections.emptyList()).withLegacyDrops(),
            new Native950Content.BankUi(517,201,15,317,39,new int[11],new int[11],Collections.emptyList(),Collections.emptyList()));
    }
    private static void pump(EmbeddedChannel ch){
        ch.runPendingTasks();Object o;while((o=ch.readOutbound())!=null)ReferenceCountUtil.release(o);ch.checkException();
    }
    private static <T>T await(EmbeddedChannel ch,CompletableFuture<T> future)throws Exception{
        long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(90);
        while(!future.isDone()&&System.nanoTime()<until){pump(ch);Thread.sleep(10);}
        return future.get(2,TimeUnit.SECONDS);
    }
    private static void persistence(Native950World world)throws Exception{
        Path directory=Files.createTempDirectory("native950-exit-profile-");
        Native950SaveStore store=new Native950SaveStore(directory.resolve("profiles"));
        EmbeddedChannel ch=new EmbeddedChannel();
        try{
            Native950Session session=await(ch,world.attach(ch,"Exit Save",()->0,()->0,new byte[]{0},
                new Native950World.SceneConfig(3217,3258,0,1,7,0,0,0),Collections.emptyList(),content(),store));
            check(ch.isActive()&&!session.isClosed(),"Disposable session not admitted");
            await(ch,world.execute(()->{
                Player p=session.player();
                p.getInventory().items.set(0,new Item(995,777));
                p.getInventory().items.set(9,new Item(199,1));
                Native950ExitUi ui=new Native950ExitUi(p,ch);
                ui.handle(entry());ui.handle(click(72));ui.handle(click(86));
                check(!p.isActive()&&ui.isSigningOut(),"Real native logout did not fence subsequent input");
                return null;
            }));
            long until=System.nanoTime()+TimeUnit.SECONDS.toNanos(15);
            while(world.reservedSlots()!=0&&System.nanoTime()<until){pump(ch);Thread.sleep(10);}
            check(!ch.isActive()&&session.isClosed()&&world.reservedSlots()==0&&World.getPlayers().isEmpty(),"Logout leaked real session/player reservation");
            Native950Save saved=new Native950SaveStore(directory.resolve("profiles")).load("Exit Save");
            check(saved!=null&&saved.inventoryIds()[0]==995&&saved.inventoryAmounts()[0]==777
                &&saved.inventoryIds()[9]==199&&saved.inventoryAmounts()[9]==1,"Final session checkpoint lost last-tick backpack change");
            check(saved.x()==3217&&saved.y()==3258&&saved.plane()==0,"Final logout checkpoint lost position");
            System.out.println("PASS isolated final-checkpoint profile: "+directory);
        }finally{ch.close();pump(ch);ch.finishAndReleaseAll();}
    }
}
