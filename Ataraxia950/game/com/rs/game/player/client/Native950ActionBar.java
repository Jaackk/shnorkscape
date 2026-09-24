package com.rs.game.player.client;

import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.cache.loaders.rs3.RS3GeneralRequirementMap;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import java.util.Map;
import java.util.function.Consumer;

/** Main native bar. Uses 950 script11797's layout, not the legacy four-bit type. */
public final class Native950ActionBar {
    public static final int SLOTS=14;
    public static final int BARS=4;
    // 950 native drag-parent resolver 0x1401ab903: bit23 bypasses parent-depth clipping.
    static final int ABILITY_EVENTS=2|(2<<11)|(1<<18)|(1<<23);
    // Exact paired-950 masks and surfaces from OpenNXT's native world bootstrap.  These
    // are the ability-book actors the client actually installs, not the older 910 faces.
    static final int MELEE_BOOK_EVENTS=8592390;
    static final int RANGED_BOOK_EVENTS=8616966;
    static final int MAGIC_BOOK_EVENTS=8617038;
    static final int BAR_SELECTOR_EVENTS=2046;
    // Script 11797's native 950 shortcut children, captured from the local bootstrap.
    // Each pair is a separately masked child in one of the fourteen slot groups.
    private static final int[][] NATIVE_SLOT_EVENT_COMPONENTS={{64,69},{77,82},{90,95},{103,108},{116,121},{129,134},{142,147},
            {155,160},{168,173},{181,186},{194,199},{207,212},{220,225},{233,238}};
    private static final int[] NATIVE_SLOT_EVENT_MASKS={11239422,2098176,2098176,2098176,2098176,2098176,2098176,
            2098176,2098176,2098176,2098176,2098176,11239422,2098176};
    static final int ROOT_INTERFACE=1477, TRASH_COMPONENT=18;
    static final int BOOK_LAST_SLOT=264;
    static final int FULL_MANUAL_MODE_VARBIT=41598;
    static final int REVOLUTION_MODE_VARBIT=41599;
    // The stock action-bar owner selects this display mode during every refresh. Without it
    // the native slot builder can retain an uninitialised shortcut presentation.
    static final int DISPLAY_MODE_VARBIT=27893;
    private final int[][] bars=new int[BARS][SLOTS];
    private int activeBar;
    private boolean locked;
    final Native950CombatPreferences preferences=new Native950CombatPreferences();
    private boolean revolutionEnabled;
    // CS2526 + DB row1306: native checkboxes store disable flags, with param7524 inversion.
    private int revolutionDisabledTiers;
    private int revolutionSlots=9;
    private static final int[] REVOLUTION_TIER_BITS={38666,52329,38708,38709};
    private int[] slots(){return bars[activeBar];}
    public void writeSettings(Map<String,Integer> settings){
        // Schema one stored only bar one as fourteen raw values. Remove those keys so a
        // migrated save stays below Native950Save's bounded settings section.
        for(int i=0;i<SLOTS;i++)settings.remove("actionBar."+i);
        for(int bar=0;bar<BARS;bar++)for(int pair=0;pair<SLOTS/2;pair++)
            settings.put("actionBar."+bar+"."+pair,savePair(bars[bar][pair*2],bars[bar][pair*2+1]));
        settings.put("actionBar.active",preferences.save(settings,activeBar));
        // Preserve the bounded save-key count and old 0/1 values. Zero range means legacy nine.
        settings.put("actionBar.revolution",(revolutionEnabled?1:0)|(revolutionDisabledTiers<<1)
                |((revolutionSlots==9?0:revolutionSlots)<<5)|(preferences.flags()<<9));
    }
    public void restore(Map<String,Integer> settings){
        boolean compact=settings.containsKey("actionBar.0.0");
        for(int bar=0;bar<BARS;bar++)for(int pair=0;pair<SLOTS/2;pair++){
            int value=compact?settings.getOrDefault("actionBar."+bar+"."+pair,0):(bar==0?legacyPair(settings,pair):0);
            bars[bar][pair*2]=loadValue(value&65535);
            bars[bar][pair*2+1]=loadValue(value>>>16);
        }
        int savedActive=settings.getOrDefault("actionBar.active",0);
        activeBar=settings.containsKey("actionBar.binding.0")?savedActive&3:Math.max(0,Math.min(BARS-1,savedActive));
        int revo=settings.getOrDefault("actionBar.revolution",0);
        preferences.restore(settings,settings.containsKey("actionBar.binding.0")?savedActive:activeBar,revo>>>9);
        revo&=511;
        revolutionEnabled=(revo&1)!=0;revolutionDisabledTiers=(revo>>>1)&15;
        int range=(revo>>>5)&15;revolutionSlots=range>=1&&range<=14?range:9;
    }
    private static int legacyPair(Map<String,Integer> settings,int pair){return savePair(settings.getOrDefault("actionBar."+(pair*2),0),settings.getOrDefault("actionBar."+(pair*2+1),0));}
    private static int savePair(int first,int second){return saveValue(first)|(saveValue(second)<<16);}
    private static int saveValue(int packed){return !valid(packed)?0:((packed>>>17)<<13)|((packed>>>4)&8191);}
    private static int loadValue(int value){int type=value>>>13,id=value&8191;return type==0&&id==0?0:(enumFor(type)>=0&&id>0?pack(type,id):0);}
    static int pack(int type,int id){if(enumFor(type)<0||id<1||id>8191)throw new IllegalArgumentException("Invalid ability");return (type<<17)|(id<<4);}
    static boolean valid(int packed){return packed==0||((packed&~0xffffff)==0&&(packed&15)==0&&enumFor(packed>>>17)>=0&&((packed>>>4)&8191)>0);}
    /**
     * The cache's live action-bar builder (CS2 11797) reads the shortcut type from
     * varbits 1747 (bits 17..23) and the ability id from varbits 1748 (bits 4..16).
     * The native varp must therefore retain the same packed layout as the authoritative
     * server value.  Repacking the type into bits 0..3 makes every ability type zero,
     * which the client renders as an empty shortcut.
     */
    static int clientShortcut(int packed){
        // The compact save/catalogue category 7 predates the native mapping and must
        // remain stable for existing bars. Exact950 CS2 6995 switch1 routes type17
        // to enum16973; native type7 instead selects enum6739. Translate only at
        // the wire boundary, retaining the full 13-bit key and the saved schema.
        return (packed>>>17)==7 ? (17<<17)|(packed&0x1ffff) : packed;
    }
    static int enumFor(int type){return type==1?10147:type==3?6736:type==4?6737:type==5?6738:type==6?6740:type==7?16973:-1;}
    static int bookType(int face,int component){
        // Recorded in the live 950 Bug Test session while dragging Flurry and Dismember.
        // Preserve this native melee-page source until its cache owner is fully decoded.
        if(face==1450&&component==3)return 1;
        if(component!=1)return -1;
        if(face==1460||face==1881||face==1888)return 1;
        if(face==1452||face==1456)return 5;
        // Exact950 scripts564 -> 8426 -> 8437: book12=defence, book13=constitution.
        if(face==1449)return 3;
        if(face==1882)return 4;
        if(face==1207||face==1211||face==1214||face==1219||face==1220||face==1221)return 7;
        return face==1459||face==1461||face==1884||face==1885||face==1886||face==1887?6:-1;
    }
    static int bookType(Player p,int face,int component){
        // Combined defensive books use native category 0/1; these are not ranged books.
        if(component==1&&(face==1880||face==1883)){
            int category=p.getVarsManager().getBitValue(face==1880?36453:36454);
            return category==0?3:category==1?4:-1;
        }
        return bookType(face,component);
    }
    // The native 1430 shortcut children form fourteen consecutive thirteen-component
    // groups. Bug Test captured 1430:66; the local bootstrap additionally proves the
    // first/last children of every group through 1430:238.
    static int barSlot(int face,int component){
        if(face==1430)return component>=64&&component<=238?(component-64)/13:-1;
        if(face==1436){int relative=component-19;return relative>=0&&relative/13<SLOTS&&(relative%13==0||relative%13==1)?relative/13:-1;}
        return -1;
    }
    static int struct(int packed){if(!valid(packed)||packed==0)return -1;Object id=RS3ClientScriptMap.getMap(enumFor(packed>>>17)).getValue((packed>>>4)&8191);return id instanceof Integer?(Integer)id:-1;}
    static String name(int packed){int id=struct(packed);return id<0?null:RS3GeneralRequirementMap.getMap(id).getStringValue(2794);}
    public void bootstrap(Player p,Channel c){
        enableBooks(c);
        preferences.sync(p,c);
        // Native8426 hide-unavailable filters: expose entries, without granting unlocks
        // or weakening the server's equipment/level/execution validation.
        c.write(Native950Packets.varbitSmall(44637,0));c.write(Native950Packets.varbitSmall(27344,0));
        Native950Prayer.enableInterface(c);
        Native950AutoSpells.syncSelection(p,c);
        sync(p,c,"bootstrap",barSnapshot());
    }
    public void bootstrap(Channel c){bootstrap(null,c);}
    void enableBooks(Channel c){
        c.write(Native950Packets.interfaceEvents(1450,3,0,BOOK_LAST_SLOT,ABILITY_EVENTS));
        for(int face:new int[]{1460,1881,1888})c.write(Native950Packets.interfaceEvents(face,1,0,BOOK_LAST_SLOT,MELEE_BOOK_EVENTS));
        for(int face:new int[]{1452,1456})c.write(Native950Packets.interfaceEvents(face,1,0,BOOK_LAST_SLOT,RANGED_BOOK_EVENTS));
        for(int face:new int[]{1449,1882,1880,1883})c.write(Native950Packets.interfaceEvents(face,1,0,BOOK_LAST_SLOT,ABILITY_EVENTS));
        for(int face:new int[]{1880,1883})c.write(Native950Packets.interfaceEvents(face,7,7,8,2));
        for(int face:new int[]{1459,1461,1884,1885,1886,1887})c.write(Native950Packets.interfaceEvents(face,1,0,BOOK_LAST_SLOT,MAGIC_BOOK_EVENTS));
        // Exact950 version6 hooks: Powers1207/1211/1214 and HUD1219/1220/1221
        // initialize native categories4/14/15, all using Necromancy enum16973.
        for(int face:new int[]{1207,1211,1214,1219,1220,1221})c.write(Native950Packets.interfaceEvents(face,1,0,BOOK_LAST_SLOT,ABILITY_EVENTS));
        for(int face:new int[]{1430,1436})for(int i=0;i<SLOTS;i++)for(int component:new int[]{(face==1430?65:19)+i*13,(face==1430?66:20)+i*13})
            c.write(Native950Packets.interfaceEvents(face,component,-1,1,ABILITY_EVENTS|(1<<21)));
        for(int slot=0;slot<SLOTS;slot++)for(int component:NATIVE_SLOT_EVENT_COMPONENTS[slot])
            c.write(Native950Packets.interfaceEvents(1430,component,-1,-1,NATIVE_SLOT_EVENT_MASKS[slot]));
        c.write(Native950Packets.interfaceEvents(1430,16,-1,-1,BAR_SELECTOR_EVENTS));
        c.write(Native950Packets.interfaceEvents(1430,254,-1,-1,BAR_SELECTOR_EVENTS));
        c.write(Native950Packets.interfaceEvents(1430,261,-1,-1,BAR_SELECTOR_EVENTS));
        c.write(Native950Packets.interfaceEvents(1430,256,-1,-1,2));
        c.write(Native950Packets.interfaceEvents(1430,270,-1,-1,2));
        c.write(Native950Packets.interfaceEvents(ROOT_INTERFACE,TRASH_COMPONENT,-1,-1,1<<21));
    }
    /**
     * Writes the complete client-visible state for the selected saved bar.  Every mutation
     * reaches this method so a server-side binding cannot diverge from the bar the client
     * renders.  It is deliberately event-driven: no world-tick caller refreshes the UI.
     */
    private void sync(Channel c){sync(null,c,"sync",barSnapshot());}
    private void sync(Player p,Channel c,String reason,String before){
        // A single summary ties every emitted action-bar config/script burst to its mutation.
        // The detailed wire list stays bounded to these event-driven calls, never world ticks.
        // Player-specific tracing is emitted by the caller paths where a Player is available.
        if(p!=null)Native950BugTest.event(p,"action-bar","visual-sync","reason",reason,"activeBar",activeBar+1,
                "before",before,"after",barSnapshot(),"varbits",binding("varbit",1893)+","+binding("varbit",1892)+","+binding("varbit",DISPLAY_MODE_VARBIT)+","+binding("varbit",FULL_MANUAL_MODE_VARBIT)+","+binding("varbit",REVOLUTION_MODE_VARBIT),
                "clientShortcuts",clientSnapshot(),"slotConfigs",slotConfigs(),"scripts",binding("script",6992)+","+binding("script",7964));
        visualWrite(p,c,Native950Packets.varbitSmall(1893,activeBar+1),"varbit",1893,activeBar+1);
        visualWrite(p,c,Native950Packets.varbitSmall(1892,locked?1:0),"varbit",1892,locked?1:0);
        visualWrite(p,c,Native950Packets.varbitSmall(DISPLAY_MODE_VARBIT,2),"varbit",DISPLAY_MODE_VARBIT,2);
        visualWrite(p,c,Native950Packets.varbitSmall(FULL_MANUAL_MODE_VARBIT,revolutionEnabled?0:1),"varbit",FULL_MANUAL_MODE_VARBIT,revolutionEnabled?0:1);
        visualWrite(p,c,Native950Packets.varbitSmall(REVOLUTION_MODE_VARBIT,revolutionEnabled?1:0),"varbit",REVOLUTION_MODE_VARBIT,revolutionEnabled?1:0);
        visualWrite(p,c,Native950Packets.varbitSmall(38639,revolutionSlots),"varbit",38639,revolutionSlots);
        for(int tier=0;tier<4;tier++)visualWrite(p,c,Native950Packets.varbitSmall(REVOLUTION_TIER_BITS[tier],(revolutionDisabledTiers>>>tier)&1),
                "varbit",REVOLUTION_TIER_BITS[tier],(revolutionDisabledTiers>>>tier)&1);
        for(int i=0;i<SLOTS;i++){
            int typeConfig=typeConfig(activeBar,i),shortcutConfig=shortcutConfig(activeBar,i);
            visualWrite(p,c,Native950Packets.varp(typeConfig,-1),"varp",typeConfig,-1);
            visualWrite(p,c,Native950Packets.varp(shortcutConfig,clientShortcut(slots()[i])),"varp",shortcutConfig,clientShortcut(slots()[i]));
            boolean tile=slots()[i]==pack(1,7)||slots()[i]==pack(1,29);
            for(int component:NATIVE_SLOT_EVENT_COMPONENTS[i])
                c.write(Native950Packets.interfaceEvents(1430,component,-1,-1,tile?11239422:NATIVE_SLOT_EVENT_MASKS[i]));
            // Actual icon/target-parent children must admit ground targets too.
            for(int component:new int[]{65+i*13,66+i*13})
                c.write(Native950Packets.interfaceEvents(1430,component,-1,1,slotEvents(tile)));
        }
        visualWrite(p,c,Native950Packets.runClientScript(6992),"script",6992,"");
        visualWrite(p,c,Native950Packets.runClientScript(7964,1436,0,0,1,-1),"script",7964,"1436,0,0,1,-1");
        // Slot rebuilds can clear the native cooldown sweep. Restamp only the
        // selected bar's still-cooling abilities when a binding/bar changes.
        if(p!=null&&p.getNative950Combat()!=null)p.getNative950Combat().refreshBarCooldowns(p,slots());
    }
    /** Refresh only transforms, then restore overlays cleared by the native redraw. */
    void refreshTransforms(Player player){
        Channel channel=player.getRealChannel();
        if(channel==null)return;
        visualWrite(player,channel,Native950Packets.runClientScript(6992),"script",6992,"");
        if(player.getNative950Combat()!=null)player.getNative950Combat().refreshBarCooldowns(player,slots());
    }
    void copyCurrentBarFrom(Player recipient,Channel channel,Native950ActionBar source,int sourceBar){
        if(source==null||sourceBar<0||sourceBar>=BARS)throw new IllegalArgumentException("Invalid source bar");
        String before=barSnapshot();
        System.arraycopy(source.bars[sourceBar],0,slots(),0,SLOTS);
        sync(recipient,channel,"copybar",before);
    }
    public void testBar(Player p,Channel c){String before=barSnapshot();slots()[0]=pack(1,3);slots()[1]=pack(5,2);slots()[2]=pack(6,3);sync(p,c,"testbar",before);reply(c,"Test slots 1-3: Backhand (melee), Binding Shot (ranged), Impact (magic). Equip the matching weapon and attack a target first.");}
    public void testBar(Channel c){testBar(null,c);}
    public boolean drag(Player p,Channel c,Native950Actions.DragAction a){
        int to=barSlot(a.targetInterfaceId(),a.targetComponentId()),from=barSlot(a.sourceInterfaceId(),a.sourceComponentId());
        if(to<0&&from<0)return false;
        if(locked||p.isLocked()||p.isDead()||!(from>=0?actionBarMounted(p.getInterfaceManager().containsInterface(a.sourceInterfaceId()),
                p.getInterfaceManager().containsInterface(ROOT_INTERFACE)):p.getInterfaceManager().containsInterface(a.sourceInterfaceId())))return true;
        if(from>=0&&(isTrashTarget(a)||a.targetComponentHash()==-1)){String before=barSnapshot();slots()[from]=0;sync(p,c,"clear",before);Native950BugTest.event(p,"action-bar","cleared","bar",activeBar+1,"slot",from+1);reply(c,"Action bar slot "+(from+1)+" cleared.");return true;}
        if(to<0){reply(c,"Drop an action-bar slot on the native trash target to remove it.");return true;}
        if(!actionBarMounted(p.getInterfaceManager().containsInterface(a.targetInterfaceId()),p.getInterfaceManager().containsInterface(ROOT_INTERFACE)))return true;
        if(from>=0){
            if(isNoOpRearrangement(from,to)){
                Native950BugTest.event(p,"action-bar","rearrange-ignored","bar",activeBar+1,"from",from+1,"to",to+1,"reason","same-slot");
                return true;
            }
            String before=barSnapshot();int old=slots()[to];slots()[to]=slots()[from];slots()[from]=old;sync(p,c,"rearrange",before);Native950BugTest.event(p,"action-bar","rearranged","bar",activeBar+1,"from",from+1,"to",to+1);return true;
        }
        int type=bookType(p,a.sourceInterfaceId(),a.sourceComponentId());
        if(type<0||!p.getInterfaceManager().containsInterface(a.sourceInterfaceId())||a.sourceSlot()<1||a.sourceSlot()>BOOK_LAST_SLOT){reply(c,"Drag an ability from a supported combat ability book.");return true;}
        int packed=pack(type,a.sourceSlot());String name=name(packed);
        if(name==null||name.isEmpty()){reply(c,"That ability is not in the current cache.");return true;}
        String before=barSnapshot();slots()[to]=packed;sync(p,c,"bind",before);Native950BugTest.event(p,"action-bar","bound","bar",activeBar+1,"slot",to+1,"packed",packed,"structure",struct(packed),"name",name,"sync","varps+6992+7964");reply(c,name+" bound to slot "+(to+1)+".");return true;
    }
    // Target flags occupy bits11..17; bit6 of that field is a ground tile.
    static int slotEvents(boolean ground){return (ABILITY_EVENTS|(1<<21))|(ground?(1<<17):0);}
    static int selectorIndex(int component,int option){
        // CS16001 adds labels1..5 to operations6..10 of1430:261.
        return component==261?option>=6&&option<=10?option-6:-1:option>=1&&option<=BARS?option-1:-1;
    }
    static boolean isTrashTarget(Native950Actions.DragAction action){return action.targetInterfaceId()==ROOT_INTERFACE&&action.targetComponentId()==TRASH_COMPONENT;}
    static boolean isNoOpRearrangement(int from,int to){return from==to;}
    public boolean button(Player p,Channel c,Native950Actions.InterfaceAction a){
        // CS7973 names the native lock control1430:270 and reads bit1892.
        if(a.interfaceId()==1430&&a.componentId()==270&&a.option()==1&&a.slot()==-1&&a.itemId()==-1){
            if(actionBarMounted(p.getInterfaceManager().containsInterface(1430),p.getInterfaceManager().containsInterface(ROOT_INTERFACE))){
                locked=!locked;c.write(Native950Packets.varbitSmall(1892,locked?1:0));
            }
            return true;
        }
        if((a.interfaceId()==1880||a.interfaceId()==1883)&&a.componentId()==7&&a.slot()>=7&&a.slot()<=8){
            if(a.option()==1&&p.getInterfaceManager().containsInterface(a.interfaceId())&&!p.isLocked()&&!p.isDead()){
                int bit=a.interfaceId()==1880?36453:36454;
                p.getVarsManager().setVarBit(bit,a.slot()-7);c.write(Native950Packets.varbitSmall(bit,a.slot()-7));
            }
            return true;
        }
        // Actual 950 input from the native preset menu uses 1430:261. Keep 254 for
        // layouts that still route through the sibling selector component.
        if(a.interfaceId()==1430&&(a.componentId()==254||a.componentId()==261)){
            int selected=selectorIndex(a.componentId(),a.option());
            if(a.slot()==-1&&selected>=0&&selected<BARS&&!p.isLocked()&&!p.isDead()){
                setActiveBar(p,c,selected);reply(c,"Action bar "+(activeBar+1)+" selected.");
                Native950BugTest.event(p,"action-bar","preset-selection","requested",a.option(),"result","selected","activeBar",activeBar+1);
            }else if(a.slot()==-1&&selected>=BARS&&!p.isLocked()&&!p.isDead()){
                reply(c,"Only action bars 1-"+BARS+" are saved by this local build. Use ;;bar <1-"+BARS+"> to select one.");
                Native950BugTest.event(p,"action-bar","preset-selection","requested",a.option(),"result","unsupported","savedBars",BARS);
            }else{
                Native950BugTest.event(p,"action-bar","preset-selection","requested",a.option(),"result","ignored","slot",a.slot(),"locked",p.isLocked(),"dead",p.isDead());
            }
            return true;
        }
        int slot=barSlot(a.interfaceId(),a.componentId());
        int type=bookType(p,a.interfaceId(),a.componentId());
        if(slot<0&&type<0)return false;
        int value=slot>=0?slots()[slot]:a.slot()>0&&a.slot()<=BOOK_LAST_SLOT?pack(type,a.slot()):0;
        int key=(value>>>4)&8191;
        // Spells and abilities share enum6740. Live1461:1 autocast is IF_BUTTON2,
        // while a spell shortcut on the bar is IF_BUTTON1; both own the same selection.
        boolean spellAction=(value>>>17)==6&&Native950AutoSpells.forKey(key)!=null
                &&(a.option()==1||(slot<0&&a.interfaceId()==1461&&a.option()==2));
        if(spellAction){
            if(!p.getInterfaceManager().containsInterface(a.interfaceId())||p.isLocked()||p.isDead())return true;
            String result=Native950AutoSpells.choose(p,c,key);
            Native950BugTest.event(p,"magic","spell-click","interface",a.interfaceId(),"component",a.componentId(),
                    "option",a.option(),"key",key,"barSlot",slot,"result",result);
            reply(c,result);return true;
        }
        // Keyboard shortcuts arrive as actions on the action-bar child (1430), while
        // the workspace owns its root wrapper (1477).  Testing only the child makes
        // a correctly visible native bar reject its own keybinds after a workspace
        // restore.  Keep the source-component validation above; this only recognises
        // the wrapper relationship the 950 client actually uses.
        boolean mounted=slot>=0 ? actionBarMounted(p.getInterfaceManager().containsInterface(a.interfaceId()),
                p.getInterfaceManager().containsInterface(ROOT_INTERFACE))
                : p.getInterfaceManager().containsInterface(a.interfaceId());
        if(a.option()!=1||!mounted||p.isLocked()||p.isDead()){
            Native950BugTest.event(p,"action-bar","activation-rejected","interface",a.interfaceId(),
                    "component",a.componentId(),"slot",slot,"option",a.option(),"activeBar",activeBar+1,
                    "childMounted",p.getInterfaceManager().containsInterface(a.interfaceId()),
                    "workspaceMounted",p.getInterfaceManager().containsInterface(ROOT_INTERFACE),
                    "locked",p.isLocked(),"dead",p.isDead());
            System.out.println("[Ataraxia950] Ability action ignored iface="+a.interfaceId()+":"+a.componentId()
                    +" option="+a.option()+" slot="+a.slot()+" mounted="+p.getInterfaceManager().containsInterface(a.interfaceId())
                    +" locked="+p.isLocked()+" dead="+p.isDead());
            return true;
        }
        if(value==0){
            Native950BugTest.event(p,"action-bar","activation-empty","interface",a.interfaceId(),
                    "component",a.componentId(),"slot",slot,"activeBar",activeBar+1);
            if(slot>=0)reply(c,"That action bar slot does not contain an ability.");
            System.out.println("[Ataraxia950] Ability action had no binding iface="+a.interfaceId()+":"+a.componentId()+" slot="+a.slot());
            return true;
        }
        Native950MeleeCombat combat=p.getNative950Combat();
        String result=combat==null?"Combat is not ready.":combat.ability(p,struct(value));
        Native950BugTest.event(p,"combat","ability-click","bar",activeBar+1,"slot",slot<0?-1:slot+1,"structure",struct(value),"result",result==null?"queued":result);
        System.out.println("[Ataraxia950] Ability action iface="+a.interfaceId()+":"+a.componentId()+" option="+a.option()
                +" slot="+a.slot()+" structure="+struct(value)+" result="+(result==null?"queued":result));
        if(result!=null&&!result.endsWith(" queued."))reply(c,result);
        return true;
    }
    /** A 950 shortcut can be delivered through the visible bar child or its workspace owner. */
    static boolean actionBarMounted(boolean childMounted,boolean workspaceMounted){return childMounted||workspaceMounted;}
    int selectedStructure(Player p,int face,int component,int slot){
        int index=barSlot(face,component),type=bookType(p,face,component);
        boolean mounted=index>=0?actionBarMounted(p.getInterfaceManager().containsInterface(face),p.getInterfaceManager().containsInterface(ROOT_INTERFACE))
                :p.getInterfaceManager().containsInterface(face);
        if(!mounted)return -1;
        return index>=0?struct(slots()[index]):type>0&&slot>0&&slot<=BOOK_LAST_SLOT?struct(pack(type,slot)):-1;
    }
    int revolutionCandidate(int enabledSlots,java.util.function.IntPredicate canExecute){
        int[] structures=new int[SLOTS];for(int i=0;i<SLOTS;i++)structures[i]=struct(slots()[i]);
        return Native950Revolution.select(structures,enabledSlots,id->revolutionTierAllowed(id)&&canExecute.test(id));
    }
    boolean revolutionTierAllowed(int structure){
        Native950AbilityCatalog.Definition d=Native950AbilityCatalog.get(structure);
        return d!=null&&d.tier>=1&&d.tier<=4&&(revolutionDisabledTiers&(1<<(d.tier-1)))==0;
    }
    int revolutionSlots(){return revolutionSlots;}
    void setRevolutionSlots(int slots,Channel c){
        if(slots<1||slots>SLOTS)throw new IllegalArgumentException("Revolution range");
        revolutionSlots=slots;refreshRevolution(c);
    }
    void toggleRevolutionTier(int tier,Channel c){
        if(tier<1||tier>4)throw new IllegalArgumentException("Revolution tier");
        revolutionDisabledTiers^=1<<(tier-1);refreshRevolution(c);
    }
    boolean isRevolutionEnabled(){return revolutionEnabled;}
    void setRevolutionEnabled(Channel c,boolean enabled){setRevolutionEnabled(enabled,packet->c.write(packet));}
    void setRevolutionEnabled(boolean enabled,Consumer<Native950Packets.Packet> send){revolutionEnabled=enabled;sendCombatMode(send);}
    void refreshRevolution(Channel c){sendCombatMode(packet->c.write(packet));}
    private void sendCombatMode(Consumer<Native950Packets.Packet> send){
        send.accept(Native950Packets.varbitSmall(FULL_MANUAL_MODE_VARBIT,revolutionEnabled?0:1));
        send.accept(Native950Packets.varbitSmall(REVOLUTION_MODE_VARBIT,revolutionEnabled?1:0));
        send.accept(Native950Packets.varbitSmall(38639,revolutionSlots));
        for(int i=0;i<4;i++)send.accept(Native950Packets.varbitSmall(REVOLUTION_TIER_BITS[i],(revolutionDisabledTiers>>>i)&1));
    }
    void cooldown(Channel c,int structure,int currentCycle,int duration){publishCooldown(c,structure,currentCycle,duration,true);}
    // CS6570 argument3 stamps the client's start cycle. A redraw must preserve that epoch.
    void refreshCooldown(Channel c,int structure,int currentCycle,int duration){
        // CS6570 also restarts the field-effect clock for these ultimates. Their retained
        // cooldown varcs are read by6506 and are not cleared by6992/slot redraws.
        if(structure==19254||structure==19251)return;
        publishCooldown(c,structure,currentCycle,duration,false);
    }
    private void publishCooldown(Channel c,int structure,int currentCycle,int duration,boolean start){
        c.write(Native950Packets.runClientScript(6570,structure,currentCycle,currentCycle+duration,start?1:0,1));
    }
    /** Listener1430:{70,83,...239} calls CS5899(slot,1003,overlay71+13*i). */
    void queueVisual(Player player,int structure){
        Native950AbilityCatalog.Definition definition=Native950AbilityCatalog.get(structure);
        int packed=definition==null?0:pack(definition.book,definition.key),slot=0;
        if(packed!=0)for(int i=0;i<SLOTS;i++)if(slots()[i]==packed){slot=i+1;break;}
        player.getVarsManager().sendVar(4164,0);
        player.getVarsManager().sendVar(5861,slot==0?0:1003);
        if(slot!=0)player.getVarsManager().sendVar(4164,slot);
        Native950BugTest.event(player,"combat","queue-visual-published","structure",structure,"preset",activeBar+1,
                "slot",slot,"rootSlot",slot==0?0:1003,"overlay",slot==0?-1:(1430<<16)|(71+13*(slot-1)));
    }
    void setActiveBar(Player p,Channel c,int index){if(index<0||index>=BARS)return;String before=barSnapshot();activeBar=index;sync(p,c,"select-bar",before);}
    void setActiveBar(Channel c,int index){setActiveBar(null,c,index);}
    int activeBar(){return activeBar;}
    int slot(int bar,int index){return bar>=0&&bar<BARS&&index>=0&&index<SLOTS?bars[bar][index]:0;}
    public void clear(Player p,Channel c){String before=barSnapshot();java.util.Arrays.fill(slots(),0);sync(p,c,"clear-command",before);reply(c,"Action bar "+(activeBar+1)+" cleared.");}
    public void clear(Channel c){clear(null,c);}
    private String barSnapshot(){return java.util.Arrays.toString(slots());}
    private String clientSnapshot(){int[] values=new int[SLOTS];for(int i=0;i<SLOTS;i++)values[i]=clientShortcut(slots()[i]);return java.util.Arrays.toString(values);}
    private String slotConfigs(){StringBuilder out=new StringBuilder();for(int i=0;i<SLOTS;i++){if(i>0)out.append(',');out.append(binding("varp",typeConfig(activeBar,i))).append('|').append(binding("varp",shortcutConfig(activeBar,i)));}return out.toString();}
    /** Exact per-preset config layout used by cache script 6995 and the retained ActionBar owner. */
    static int typeConfig(int bar,int slot){
        if(bar<0||slot<0||slot>=SLOTS)throw new IllegalArgumentException("Invalid action-bar config index");
        return bar>=5?5335+14*(bar-5)+slot:slot>=12?4429+2*bar+slot-12:823+12*bar+slot;
    }
    static int shortcutConfig(int bar,int slot){
        if(bar<0||slot<0||slot>=SLOTS)throw new IllegalArgumentException("Invalid action-bar config index");
        return bar>=5?5265+14*(bar-5)+slot:slot>=12?4415+2*bar+slot-12:739+12*bar+slot;
    }
    /**
     * Native950Packets writes these known wire IDs directly.  Native950IdMap is a separate
     * compatibility allow-list, so an absent entry is diagnostic context, not a client-write
     * rejection.  Keeping that distinction in Bug Test avoids chasing a false transport fault.
     */
    private static String binding(String kind,int id){
        int resolved=kind.equals("varp")?Native950IdMap.varp(id):kind.equals("varbit")?Native950IdMap.varbit(id):Native950IdMap.script(id);
        return kind+"="+id+":direct-native-packet; mapping="+(resolved<0?"unmapped":"declared:"+resolved);
    }
    private static void visualWrite(final Player p,Channel c,Native950Packets.Packet packet,final String kind,final int id,final Object value){
        if(p!=null)Native950BugTest.event(p,"action-bar","visual-write-attempt","kind",kind,"id",id,"value",value,"binding",binding(kind,id));
        ChannelFuture future=c.write(packet);
        if(p!=null)future.addListener(new ChannelFutureListener(){public void operationComplete(ChannelFuture completed){
            Throwable failure=completed.cause();String message=failure==null?"":String.valueOf(failure.getMessage());if(message.length()>512)message=message.substring(0,512);
            Native950BugTest.event(p,"action-bar",completed.isSuccess()?"visual-write-succeeded":"visual-write-failed","kind",kind,"id",id,
                    "failure",failure==null?"":failure.getClass().getSimpleName(),"message",message);
        }});
    }
    private static void reply(Channel c,String text){c.write(Native950Packets.gameMessage(0,text));}
}
