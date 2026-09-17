package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.game.player.QuestManager;
import com.rs.game.player.Skills;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Paired950 catalogue and encrypted normal IF-button routing; no account, listener or client process. */
public final class Native950QuestsAcceptance {
    public static void main(String[] args)throws Exception{
        require(args.length==1,"Usage: Native950QuestsAcceptance <950-flat-cache>");require(NativeCacheVerification.isEnforced(),"Cache verification is required");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World.getInstance().execute(()->{try(Native950ProductionUiAcceptance.Fixture f=new Native950ProductionUiAcceptance.Fixture()){check(f);}return null;}).get(120,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"Scheduler failure");System.out.println("PASS:362 paired950 quests; native encrypted catalogue, overview/journal, history, filters, sort, bookmark, legacy state and stale-input guards; no progression or account write.");
    }
    @SuppressWarnings("unchecked") private static void check(Native950ProductionUiAcceptance.Fixture f)throws Exception{
        Native950QuestCatalog.verify();require(Native950QuestCatalog.all().size()==362,"Wrong catalogue size");require(Native950QuestCatalog.maximumPoints()==472,"Wrong cache point total");
        require(Native950QuestCatalog.get(1).id==257&&Native950QuestCatalog.get(1).name.equals("Cook's Assistant"),"Public key confused with definition id");
        Native950Quests ui=new Native950Quests(f.player,f.channel);ui.opened(2,2);require(!ui.isOpen(),"Wrong tab opened quests");
        final java.util.List<Native950Packets.Packet> summaryPackets=new java.util.ArrayList<>();
        f.channel.pipeline().addLast("summary-capture",new io.netty.channel.ChannelOutboundHandlerAdapter(){
            @Override public void write(io.netty.channel.ChannelHandlerContext c,Object msg,io.netty.channel.ChannelPromise promise){if(msg instanceof Native950Packets.Packet)summaryPackets.add((Native950Packets.Packet)msg);c.write(msg,promise);}
        });
        f.player.setMoneyPouchValue(1234567);ui.opened(0,1);f.drain();require(!ui.isOpen(),"Hero summary captured quest-list input");
        Native950Packets.Packet expectedScript=Native950Packets.runClientScript(11145,0,0,2,1,(1446<<16)|49);
        Native950Packets.Packet expectedMoney=Native950Packets.interfaceText(1446,22,"1,234,567");
        require(summaryPackets.stream().anyMatch(p->p.type()==expectedScript.type()&&java.util.Arrays.equals(p.payload(),expectedScript.payload())),"Summary did not set an empty completed-progress bar");
        require(summaryPackets.stream().anyMatch(p->p.type()==expectedMoney.type()&&java.util.Arrays.equals(p.payload(),expectedMoney.payload())),"Summary omitted the actual money-pouch balance");
        require(java.util.Arrays.equals(ui.summaryCounts(),new int[]{332,0,0}),"Visible quest counts include hidden rows or fake completion");
        for(int component:new int[]{28,33,38}){
            Native950Packets.Packet expected=Native950Packets.interfaceText(1446,component,(component==38?332:0)+" / 332");
            require(summaryPackets.stream().anyMatch(p->p.type()==expected.type()&&java.util.Arrays.equals(p.payload(),expected.payload())),"Actual summary count missing from component "+component);
        }
        summaryPackets.clear();click(f,ui,1446,65,-1,-1,1);click(f,ui,1446,86,-1,-1,1);require(summaryPackets.stream().filter(p->p.type()==com.rs.network.protocol.modern950.Native950Protocol.ServerPacket.MESSAGE_GAME).count()==2,"Summary account/profile buttons have no feedback");
        ui.closed(0,1);summaryPackets.clear();click(f,ui,1446,65,-1,-1,1);require(summaryPackets.isEmpty(),"Closed summary accepted stale account action");
        f.channel.pipeline().remove("summary-capture");
        f.channel.write(Native950Packets.openSub(1448,3,1783,true));f.channel.write(Native950Packets.openSub(1448,5,1500,true));ui.opened(3,2);
        f.drain();require(f.output.contains(com.rs.network.protocol.modern950.Native950Protocol.ServerPacket.VARP_LARGE)&&f.output.contains(com.rs.network.protocol.modern950.Native950Protocol.ServerPacket.VARBIT_LARGE),"Native quest state was rejected by a legacy varp binding facade");
        require(ui.isOpen()&&ui.selectedQuest()==-1,"Quest landing state");
        require(f.player.getVarsManager().getBitValue(316)==1&&f.player.getVarsManager().getBitValue(318)==0&&f.player.getVarsManager().getBitValue(43789)==0&&f.player.getVarsManager().getBitValue(43788)==0,"Native filter polarity hides the initial quest list");
        require(f.player.getVarsManager().getValue(1297)==0,"Browsing gave quest points");
        for(Native950QuestCatalog.Quest q:Native950QuestCatalog.all()){
            click(f,ui,1783,18,q.key,-1,1);require(ui.selectedQuest()==q.key,"Sparse native key rejected:"+q.key);require(f.player.getVarsManager().getValue(3936)==q.key,"Wrong client-selected quest id");
        }
        click(f,ui,1783,18,1,-1,1);require(!ui.showingJournal(),"Unstarted left-click did not show native overview");
        int selected=ui.selectedQuest();click(f,ui,1783,18,2,1511,1);require(ui.selectedQuest()==selected,"Forged item-bearing quest click selected another quest");
        click(f,ui,1783,18,65534,-1,1);require(ui.selectedQuest()==selected,"Invalid native row was accepted");
        click(f,ui,1783,18,1,-1,2);require(ui.showingJournal(),"Quest journal option did not work");click(f,ui,1500,16,-1,-1,1);require(!ui.showingJournal(),"Overview return did not work");
        click(f,ui,1500,421,-1,-1,1);require(ui.activeQuest()==1&&f.player.getVarsManager().getBitValue(3260)==1,"Bookmark did not use public quest id");
        require(ui.state(Native950QuestCatalog.get(1))==0,"Bookmark started Cook's Assistant");
        click(f,ui,1500,421,-1,-1,1);require(ui.activeQuest()==-1,"Bookmark toggle failed");
        click(f,ui,1783,18,2,-1,1);click(f,ui,1500,418,-1,-1,1);require(ui.selectedQuest()==1,"Quest back history");click(f,ui,1500,415,-1,-1,1);require(ui.selectedQuest()==2,"Quest forward history");
        int before=f.player.getVarsManager().getBitValue(318);click(f,ui,1783,11,-1,-1,1);require(f.player.getVarsManager().getBitValue(318)==1-before,"Completed filter did not toggle");
        click(f,ui,1477,896,5,-1,1);require(f.player.getVarsManager().getBitValue(315)==0,"Unarmed dropdown changed quest sort");click(f,ui,1783,32,-1,-1,1);click(f,ui,1477,896,5,-1,1);require(f.player.getVarsManager().getBitValue(315)==5,"Quest sort did not change");
        click(f,ui,1783,32,-1,-1,1);click(f,ui,1477,896,5,-1,1);require(f.player.getVarsManager().getBitValue(317)==1,"Repeated sort did not reverse");
        // Legacy level-based auto-completion must never run merely because the page was opened.
        for(int s:new int[]{Skills.CONSTRUCTION,Skills.FARMING,Skills.HUNTER,Skills.THIEVING,Skills.MAGIC,Skills.DEFENCE})f.level(s,99);
        ui.closed(3,2);ui.opened(3,2);require(!f.player.getQuestManager().completedQuest(QuestManager.Quests.KINGS_RANSOM),"Quest UI invoked legacy level-based completion");
        require(f.player.getVarsManager().getBitValue(11298)==0,"Quest UI fabricated King's Ransom completion");
        java.lang.reflect.Field field=QuestManager.class.getDeclaredField("completedQuests");field.setAccessible(true);List<QuestManager.Quests> completed=(List<QuestManager.Quests>)field.get(f.player.getQuestManager());completed.add(QuestManager.Quests.KINGS_RANSOM);
        ui.closed(3,2);ui.opened(3,2);require(f.player.getVarsManager().getBitValue(11298)==90,"Actual legacy completion did not use current950 threshold");require(f.player.getVarsManager().getValue(1297)==1,"Actual legacy points were not displayed");
        require(java.util.Arrays.equals(ui.summaryCounts(),new int[]{331,0,1}),"Actual completed quest omitted from Summary");
        click(f,ui,1783,18,126,-1,1);require(ui.showingJournal(),"Completed quest left-click ignored cached option order");completed.clear();ui.closed(3,2);ui.opened(3,2);require(f.player.getVarsManager().getBitValue(11298)==0,"Cleared source progress retained stale flag");
        f.player.getQuestManager().setQuestStage(QuestManager.Quests.NOMADS_REQUIEM,3);ui.closed(3,2);ui.opened(3,2);require(f.player.getVarsManager().getBitValue(10095)==1,"910 internal stage was copied into950 varbit");
        require(java.util.Arrays.equals(ui.summaryCounts(),new int[]{331,1,0}),"Actual started quest omitted from Summary");
        click(f,ui,1783,32,-1,-1,1);ui.closed(3,2);selected=ui.selectedQuest();click(f,ui,1783,18,1,-1,1);click(f,ui,1477,896,0,-1,1);require(ui.selectedQuest()==selected,"Closed quest page accepted stale action");f.drain();
    }
    private static void click(Native950ProductionUiAcceptance.Fixture f,Native950Quests ui,int face,int component,int slot,int item,int option){
        int[] codes={18,122,89};int hash=(face<<16)|component;ByteBuf b=Unpooled.buffer(10);b.writeByte((codes[option-1]+f.clientCipher.getAsInt())&255);b.writeMedium(item);b.writeByte(hash>>>16);b.writeByte(hash>>>24);b.writeByte(hash);b.writeByte(hash>>>8);b.writeShort(slot);
        f.channel.writeInbound(b);f.channel.runPendingTasks();require(f.transport.drainActions(a->{require(a instanceof Native950Actions.InterfaceAction,"Wrong decoded quest packet");ui.handle((Native950Actions.InterfaceAction)a);})==1,"Encrypted quest input was not decoded");f.drain();
    }
    private static void require(boolean ok,String why){if(!ok)throw new AssertionError(why);}
}
