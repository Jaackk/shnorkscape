package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.game.player.QuestManager;
import com.rs.network.protocol.modern950.Native950Actions.InterfaceAction;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.util.*;

/** Adventure/Quests adapter. Navigation owns1783/1500 mounts; this class owns only quest UI state. */
public final class Native950Quests {
    private static final int LIST=1783,DETAIL=1500;
    private final Player player;
    private final Channel channel;
    private boolean open,summaryOpen,initialized,sorting,journal;
    private int selected=-1,active=-1,historyIndex=-1;
    private final List<Integer> history=new ArrayList<>();
    public Native950Quests(Player player,Channel channel){this.player=Objects.requireNonNull(player);this.channel=Objects.requireNonNull(channel);}
    public static void verifyCacheBindings(){Native950QuestCatalog.verify();}
    public boolean isOpen(){return open;}
    public int selectedQuest(){return selected;}
    public int activeQuest(){return active;}
    public boolean showingJournal(){return journal;}
    public void opened(int menu,int page){
        if(menu==0&&page==1){summaryOpen=true;renderHeroSummary();return;}
        if(menu!=3||page!=2)return;verifyCacheBindings();open=true;sorting=false;
        if(!initialized){
            //315 order;316 shows locked;317 reversed;318/43789/43788 hide completed/full/mini.
            bit(315,0);bit(316,1);bit(317,0);bit(318,0);bit(43789,0);bit(43788,0);initialized=true;
        }
        syncKnownProgress();
        channel.write(Native950Packets.interfaceEvents(LIST,18,0,450,14));
        for(int component:new int[]{10,11,12,13,32})channel.write(Native950Packets.interfaceEvents(LIST,component,-1,-1,2));
        for(int component:new int[]{10,13,16,19,403,415,418,421,424,369,381})channel.write(Native950Packets.interfaceEvents(DETAIL,component,-1,-1,2));
        // Accept Quest is an NPC-driven workflow, never a blanket unlock from browsing a list.
        channel.write(Native950Packets.interfaceEvents(DETAIL,409,-1,-1,0));
        channel.write(Native950Packets.runClientScript(2169,hash(LIST,6)));
        if(selected<0){
            hide(1,true);hide(329,true);hide(326,false);
            text(327,"Quests");text(328,"Select a quest to view its requirements, story and rewards.<br>Some quests are not available yet.");
        }else select(selected,false,journal);
    }
    private void renderHeroSummary(){
        verifyCacheBindings();syncKnownProgress();int[] counts=summaryCounts();int total=counts[0]+counts[1]+counts[2];
        //8277's eligibility is18798 -> quest param7838. Retail/default quest vars are not progress.
        //Use the same eligible catalogue but only the server's actual same-named quest state.
        channel.write(Native950Packets.interfaceText(1446,28,counts[2]+" / "+total));
        channel.write(Native950Packets.interfaceText(1446,33,counts[1]+" / "+total));
        channel.write(Native950Packets.interfaceText(1446,38,counts[0]+" / "+total));
        channel.write(Native950Packets.runClientScript(11145,counts[2]*16384/total,0,2,1,hash(1446,49)));
        channel.write(Native950Packets.runClientScript(11145,(counts[2]+counts[1])*16384/total,0,2,1,hash(1446,45)));
        channel.write(Native950Packets.interfaceText(1446,22,moneyPouchSummary()));
        channel.write(Native950Packets.interfaceEvents(1446,65,-1,-1,2));
        channel.write(Native950Packets.interfaceEvents(1446,86,-1,-1,2));
    }
    int[] summaryCounts(){
        int[] counts=new int[3];for(Native950QuestCatalog.Quest q:Native950QuestCatalog.all())if(q.number(7838,0)==0)counts[state(q)]++;return counts;
    }
    String moneyPouchSummary(){return String.format(Locale.US,"%,d",player.getMoneyPouchValue());}
    public void closed(int menu,int page){
        if(menu==0&&page==1){summaryOpen=false;return;}
        if(menu!=3||page!=2||!open)return;open=false;
        if(sorting){channel.write(Native950Packets.runClientScript(10444));channel.write(Native950Packets.interfaceEvents(1477,896,0,9,0));}
        sorting=false;
    }
    public boolean handle(InterfaceAction a){
        if(summaryOpen&&a.interfaceId()==1446&&(a.componentId()==65||a.componentId()==86)){
            if(a.option()==1&&a.itemId()==-1&&a.slot()==-1)player.sendMessage(a.componentId()==65?"Changing your name is not available yet.":"Editing your examine profile is not available yet.");
            return true;
        }
        if(!open)return false;
        if(a.interfaceId()==1477&&a.componentId()==896){
            if(!sorting)return false;
            sorting=false;channel.write(Native950Packets.runClientScript(10444));channel.write(Native950Packets.interfaceEvents(1477,896,0,9,0));
            if(a.option()==1&&a.itemId()==-1&&a.slot()>=0&&a.slot()<10){
                int old=player.getVarsManager().getBitValue(315);bit(317,old==a.slot()?1-player.getVarsManager().getBitValue(317):0);bit(315,a.slot());refreshList();
                channel.write(Native950Packets.runClientScript(12685,hash(LIST,31),hash(LIST,32)));
            }return true;
        }
        if(a.interfaceId()==LIST){
            if(a.itemId()!=-1)return true;
            if(a.componentId()==18){
                Native950QuestCatalog.Quest q=Native950QuestCatalog.get(a.slot());if(q==null||a.option()<1||a.option()>3)return true;
                if(a.option()==3){select(q.key,true,false);player.sendMessage("Quest map hints are not available yet. The overview shows the start point.");}
                else select(q.key,true,a.option()==(state(q)==0?2:1));return true;
            }
            if(a.option()!=1||a.slot()!=-1)return true;
            switch(a.componentId()){
            case 10:toggle(316);break;case 11:toggle(318);break;case 12:toggle(43789);break;case 13:toggle(43788);break;
            case 32:sorting=true;channel.write(Native950Packets.interfaceEvents(1477,896,0,9,2));break;
            default:break;
            }return true;
        }
        if(a.interfaceId()!=DETAIL)return false;
        if(a.itemId()!=-1||a.slot()!=-1||a.option()!=1||selected<0)return true;
        switch(a.componentId()){
        case 10:case 415:travelHistory(1);break;case 13:case 418:travelHistory(-1);break;
        case 16:select(selected,false,false);break;case 424:select(selected,false,true);break;
        case 19:case 421:bookmark();break;
        case 369:case 381:break; //Native4249 binds4250, which reveals the cached text locally.
        case 403:player.sendMessage("Quest map hints are not available yet. The overview shows where this quest begins.");break;
        default:break;
        }return true;
    }
    private void toggle(int id){bit(id,1-player.getVarsManager().getBitValue(id));refreshList();}
    private void refreshList(){channel.write(Native950Packets.runClientScript(2169,hash(LIST,6)));channel.write(Native950Packets.runClientScript(17183));}
    private void select(int key,boolean remember,boolean asJournal){
        Native950QuestCatalog.Quest q=Native950QuestCatalog.get(key);if(q==null)return;
        selected=key;journal=asJournal;
        if(remember&&(historyIndex<0||history.get(historyIndex)!=key)){
            while(history.size()>historyIndex+1)history.remove(history.size()-1);history.add(key);if(history.size()>64)history.remove(0);historyIndex=history.size()-1;
        }
        var(3936,key);channel.write(Native950Packets.varc(699,q.id));
        hide(326,true);hide(1,!journal);hide(329,journal);hide(399,true);hide(410,false);
        if(journal)journal(q);else overview();
        channel.write(Native950Packets.runClientScript(6785));
    }
    private void overview(){
        Native950QuestCatalog.Quest q=Native950QuestCatalog.get(selected);
        channel.write(Native950Packets.varcString(2554,requirements(q)));
        channel.write(Native950Packets.runClientScript(4011));
        // The native status remains truthful without implying the cached story is playable locally.
        text(425,statusText(q)+" - not available yet");
        hide(399,true);hide(410,false);
    }
    private void journal(Native950QuestCatalog.Quest q){
        text(21,q.name);
        List<String> lines=new ArrayList<>();
        lines.add("<col=ffcc66>"+statusText(q)+"</col>");
        lines.add("This quest is not available yet.");
        lines.add("You can browse its requirements and rewards in Quest Overview.");
        lines.add("");
        String start=q.text(7814);if(!start.isEmpty()){lines.add("<col=ffcc66>Start point</col>");for(String line:wrap(start,68))lines.add(line);}
        lines.add("");
        for(int i=0;i<300;i++)text(25+i,i<lines.size()?lines.get(i):"");
        channel.write(Native950Packets.runClientScript(11145,475,Math.max(340,lines.size()*20+20),0,0,hash(DETAIL,24)));
        channel.write(Native950Packets.runClientScript(7791,hash(DETAIL,325),hash(DETAIL,24)));
    }
    private String requirements(Native950QuestCatalog.Quest q){
        List<String> lines=new ArrayList<>();
        for(Integer id:q.prerequisites){Native950QuestCatalog.Quest required=Native950QuestCatalog.byDefinition(id);if(required!=null)lines.add(coloured(required.name,state(required)==2));}
        for(int[] req:q.skills){int skill=req[0],level=req[1];String name=skill>=0&&skill<com.rs.game.player.Skills.SKILL_NAME.length?com.rs.game.player.Skills.SKILL_NAME[skill]:"Skill "+skill;lines.add(coloured(level+" "+name,skill>=0&&skill<com.rs.game.player.Skills.SKILL_NAME.length&&player.getSkills().getLevelForXp(skill)>=level));}
        if(q.pointRequirement>0)lines.add(coloured(q.pointRequirement+" Quest Points",player.getVarsManager().getValue(1297)>=q.pointRequirement));
        return lines.isEmpty()?"None.":String.join("<br>",lines);
    }
    private static String coloured(String text,boolean met){return "<col="+(met?"00a000":"ff4444")+">"+text+"</col>";}
    private void bookmark(){
        if(selected<0)return;active=active==selected?-1:selected;bit(3260,active<0?0:active);channel.write(Native950Packets.runClientScript(6785));refreshList();
        player.sendMessage(active<0?"Quest bookmark cleared.":"Bookmarked "+Native950QuestCatalog.get(active).name+" for this session. This does not start the quest.");
    }
    private void travelHistory(int direction){int next=historyIndex+direction;if(next<0||next>=history.size())return;historyIndex=next;select(history.get(next),false,journal);}
    private void syncKnownProgress(){
        if(player.isCompT()){Native950CombatProgression.grant(player);return;}
        int points=0;for(Native950QuestCatalog.Quest q:Native950QuestCatalog.all()){
            if(q.key!=126&&q.key!=136&&q.key!=162)continue;int status=state(q);
            if(status==2)points+=q.points;
            // Only a same-named canonical legacy quest supplies coarse started/finished state.
            // Internal910 stage numbers are never treated as950 varbit values.
            for(int[] var:q.varps)var(var[0],status==0?0:var[status]);
            for(int[] var:q.varbits)bit(var[0],status==0?0:var[status]);
        }
        var(1297,points);var(423,Native950QuestCatalog.maximumPoints());
    }
    int state(Native950QuestCatalog.Quest q){
        if(player.isCompT())return 2;
        QuestManager.Quests legacy;
        switch(q.key){case 126:legacy=QuestManager.Quests.KINGS_RANSOM;break;case 136:legacy=QuestManager.Quests.PERIL_OF_ICE_MONTAINS;break;case 162:legacy=QuestManager.Quests.NOMADS_REQUIEM;break;default:return 0;}
        if(player.getQuestManager()==null)return 0;
        if(player.getQuestManager().completedQuest(legacy))return 2;
        return player.getQuestManager().getQuestStage(legacy)>=0?1:0;
    }
    private String statusText(Native950QuestCatalog.Quest q){int s=state(q);return s==2?"Completed":s==1?"Started":"Not started";}
    private static List<String> wrap(String text,int width){List<String> out=new ArrayList<>();StringBuilder line=new StringBuilder();for(String word:text.replace("<br>"," ").split(" ")){if(line.length()+word.length()>width){out.add(line.toString());line.setLength(0);}if(line.length()>0)line.append(' ');line.append(word);}if(line.length()>0)out.add(line.toString());return out;}
    private void bit(int id,int value){player.getVarsManager().setVarBit(id,value);channel.write(Native950Packets.varbitLarge(id,value));}
    private void var(int id,int value){player.getVarsManager().setVar(id,value);channel.write(Native950Packets.varp(id,value));}
    private void hide(int component,boolean hide){channel.write(Native950Packets.hideInterface(DETAIL,component,hide));}
    private void text(int component,String value){channel.write(Native950Packets.interfaceText(DETAIL,component,value));}
    private static int hash(int face,int component){return (face<<16)|component;}
}
