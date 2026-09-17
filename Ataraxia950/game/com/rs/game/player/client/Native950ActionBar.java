package com.rs.game.player.client;

import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.util.Map;
import java.util.function.Consumer;

/** Main native bar. Uses 950 script11797's layout, not the legacy four-bit type. */
public final class Native950ActionBar {
    public static final int SLOTS=14;
    // 950 native drag-parent resolver 0x1401ab903: bit23 bypasses parent-depth clipping.
    static final int ABILITY_EVENTS=2|(2<<11)|(1<<18)|(1<<23);
    static final int BOOK_LAST_SLOT=264;
    static final int FULL_MANUAL_MODE_VARBIT=41598;
    static final int REVOLUTION_MODE_VARBIT=41599;
    private final int[] slots=new int[SLOTS];
    private boolean revolutionEnabled;
    public void writeSettings(Map<String,Integer> settings){for(int i=0;i<SLOTS;i++)settings.put("actionBar."+i,slots[i]);settings.put("actionBar.revolution",revolutionEnabled?1:0);}
    public void restore(Map<String,Integer> settings){for(int i=0;i<SLOTS;i++){int v=settings.getOrDefault("actionBar."+i,0);slots[i]=valid(v)?v:0;}revolutionEnabled=settings.getOrDefault("actionBar.revolution",0)==1;}
    static int pack(int type,int id){if(enumFor(type)<0||id<1||id>8191)throw new IllegalArgumentException("Invalid ability");return (type<<17)|(id<<4);}
    static boolean valid(int packed){return packed==0||((packed&~0xffffff)==0&&(packed&15)==0&&enumFor(packed>>>17)>=0&&((packed>>>4)&8191)>0);}
    static int enumFor(int type){return type==1?10147:type==5?6738:type==6?6740:-1;}
    static int bookType(int face,int component){if(face==1450&&component==3)return 1;if(component!=1)return -1;return face==1460?1:face==1452||face==1456?5:face==1461||face==1459?6:-1;}
    static int barSlot(int face,int component){if(face!=1430&&face!=1436)return -1;int relative=component-(face==1430?65:19);return relative>=0&&relative/13<SLOTS&&(relative%13==0||relative%13==1)?relative/13:-1;}
    static int struct(int packed){if(!valid(packed)||packed==0)return -1;Object id=RS3ClientScriptMap.getMap(enumFor(packed>>>17)).getValue((packed>>>4)&8191);return id instanceof Integer?(Integer)id:-1;}
    static String name(int packed){int id=struct(packed);return id<0?null:RS3GeneralRequirementMap.getMap(id).getStringValue(2794);}
    public void bootstrap(Channel c){
        c.write(Native950Packets.varbitSmall(1893,1));
        c.write(Native950Packets.varbitSmall(1892,0));
        refreshRevolution(c);
        enableBooks(c);
        refresh(c);
    }
    void enableBooks(Channel c){
        for(int face:new int[]{1460,1452,1461,1450,1456,1459})c.write(Native950Packets.interfaceEvents(face,face==1450?3:1,0,BOOK_LAST_SLOT,ABILITY_EVENTS));
        for(int face:new int[]{1430,1436})for(int i=0;i<SLOTS;i++)for(int component:new int[]{(face==1430?65:19)+i*13,(face==1430?66:20)+i*13})
            c.write(Native950Packets.interfaceEvents(face,component,-1,1,ABILITY_EVENTS|(1<<21)));
        c.write(Native950Packets.interfaceEvents(1430,256,-1,-1,2));
    }
    private void refresh(Channel c){
        for(int i=0;i<SLOTS;i++){
            c.write(Native950Packets.varp(i<12?823+i:4429+i-12,-1));
            c.write(Native950Packets.varp(i<12?739+i:4415+i-12,slots[i]));
        }
        c.write(Native950Packets.runClientScript(6992));
        c.write(Native950Packets.runClientScript(7964,1436,0,0,1,-1));
    }
    public void testBar(Channel c){slots[0]=pack(1,3);slots[1]=pack(5,2);slots[2]=pack(6,3);bootstrap(c);reply(c,"Test slots 1-3: Backhand (melee), Binding Shot (ranged), Impact (magic). Equip the matching weapon and attack a target first.");}
    public boolean drag(Player p,Channel c,Native950Actions.DragAction a){
        int to=barSlot(a.targetInterfaceId(),a.targetComponentId()),from=barSlot(a.sourceInterfaceId(),a.sourceComponentId());
        if(to<0&&from<0)return false;
        if(p.isLocked()||p.isDead()||!p.getInterfaceManager().containsInterface(a.targetInterfaceId())
                ||!p.getInterfaceManager().containsInterface(a.sourceInterfaceId()))return true;
        if(to<0){reply(c,"Only main-bar rearrangement is supported. Use ;;clearbar to empty it.");return true;}
        if(from>=0){int old=slots[to];slots[to]=slots[from];slots[from]=old;refresh(c);return true;}
        int type=bookType(a.sourceInterfaceId(),a.sourceComponentId());
        if(type<0||!p.getInterfaceManager().containsInterface(a.sourceInterfaceId())||a.sourceSlot()<1||a.sourceSlot()>BOOK_LAST_SLOT){reply(c,"Drag an ability from the melee, ranged or magic ability book.");return true;}
        int packed=pack(type,a.sourceSlot());String name=name(packed);
        if(name==null||name.isEmpty()){reply(c,"That ability is not in the current cache.");return true;}
        slots[to]=packed;refresh(c);reply(c,name+" bound to slot "+(to+1)+".");return true;
    }
    public boolean button(Player p,Channel c,Native950Actions.InterfaceAction a){
        int slot=barSlot(a.interfaceId(),a.componentId());
        int type=bookType(a.interfaceId(),a.componentId());
        if(slot<0&&type<0)return false;
        if(a.option()!=1||!p.getInterfaceManager().containsInterface(a.interfaceId())||p.isLocked()||p.isDead()){
            System.out.println("[Ataraxia950] Ability action ignored iface="+a.interfaceId()+":"+a.componentId()
                    +" option="+a.option()+" slot="+a.slot()+" mounted="+p.getInterfaceManager().containsInterface(a.interfaceId())
                    +" locked="+p.isLocked()+" dead="+p.isDead());
            return true;
        }
        int value=slot>=0?slots[slot]:a.slot()>0&&a.slot()<=BOOK_LAST_SLOT?pack(type,a.slot()):0;
        if(value==0){
            if(slot>=0)reply(c,"That action bar slot does not contain an ability.");
            System.out.println("[Ataraxia950] Ability action had no binding iface="+a.interfaceId()+":"+a.componentId()+" slot="+a.slot());
            return true;
        }
        Native950MeleeCombat combat=p.getNative950Combat();
        String result=combat==null?"Combat is not ready.":combat.ability(p,struct(value));
        System.out.println("[Ataraxia950] Ability action iface="+a.interfaceId()+":"+a.componentId()+" option="+a.option()
                +" slot="+a.slot()+" structure="+struct(value)+" result="+(result==null?"queued":result));
        if(result!=null)reply(c,result);
        return true;
    }
    int selectedStructure(Player p,int face,int component,int slot){
        if(!p.getInterfaceManager().containsInterface(face))return -1;
        int index=barSlot(face,component),type=bookType(face,component);
        return index>=0?struct(slots[index]):type>0&&slot>0&&slot<=BOOK_LAST_SLOT?struct(pack(type,slot)):-1;
    }
    int revolutionCandidate(int enabledSlots,java.util.function.IntPredicate canExecute){
        int[] structures=new int[SLOTS];for(int i=0;i<SLOTS;i++)structures[i]=struct(slots[i]);
        return Native950Revolution.select(structures,enabledSlots,canExecute);
    }
    boolean isRevolutionEnabled(){return revolutionEnabled;}
    void setRevolutionEnabled(Channel c,boolean enabled){setRevolutionEnabled(enabled,packet->c.write(packet));}
    void setRevolutionEnabled(boolean enabled,Consumer<Native950Packets.Packet> send){revolutionEnabled=enabled;sendCombatMode(send);}
    void refreshRevolution(Channel c){sendCombatMode(packet->c.write(packet));}
    private void sendCombatMode(Consumer<Native950Packets.Packet> send){
        send.accept(Native950Packets.varbitSmall(FULL_MANUAL_MODE_VARBIT,revolutionEnabled?0:1));
        send.accept(Native950Packets.varbitSmall(REVOLUTION_MODE_VARBIT,revolutionEnabled?1:0));
    }
    void cooldown(Channel c,int structure,int currentCycle,int duration){c.write(Native950Packets.runClientScript(6570,structure,currentCycle,currentCycle+duration,1,1));}
    public void clear(Channel c){java.util.Arrays.fill(slots,0);refresh(c);reply(c,"Main action bar cleared.");}
    private static void reply(Channel c,String text){c.write(Native950Packets.gameMessage(0,text));}
}
