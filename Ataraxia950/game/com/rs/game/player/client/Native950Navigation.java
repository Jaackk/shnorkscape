package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;

/** Owns the paired950 management shell; page definitions come from enum7699, never910 positions. */
public final class Native950Navigation {
    public interface PageListener {
        void opened(int menu,int page);
        void closed(int menu,int page);
        boolean handle(Native950Actions.InterfaceAction action);
    }
    public static final class Page {
        public final int menu,number,structure;
        public final String title;
        public final boolean hidden;
        private final int[] faces;
        private Page(int menu,int number) {
            String p="page."+menu+"."+number+".";
            this.menu=menu;this.number=number;this.structure=Integer.parseInt(DATA.getProperty(p+"struct"));
            this.title=DATA.getProperty(p+"title");this.hidden="1".equals(DATA.getProperty(p+"hidden"));
            this.faces=ints(DATA.getProperty(p+"faces"));
        }
        public int[] interfaces(){return faces.clone();}
    }
    private static final int ROOT=1477,HOST=715,WRAPPER=708,SHELL=1448;
    private static final int[] PAGE_BITS={18995,29607,18997,18998,18999,19000,19003,19002,30609};
    private static final int[] RIBBON_MENUS={0,1,2,3,4,7,8};
    private static final int[] QUICK_MENUS={0,1,3,2,4,7};
    private static final Properties DATA=load();
    private static final Map<Integer,List<Page>> PAGES=pages();
    private static Object verifiedStore;
    private final Player player;
    private final Channel channel;
    private final Native950SkillGuide guide;
    private final int[] remembered={1,2,1,2,3,1,1,1,1};
    private final LinkedHashMap<Integer,Integer> mounted=new LinkedHashMap<>();
    private final LinkedHashMap<Integer,Integer> displaced=new LinkedHashMap<>();
    private Page current;
    private PageListener listener;
    Native950Navigation(Player player,Channel channel,Native950SkillGuide guide){this.player=player;this.channel=channel;this.guide=guide;}
    public void setPageListener(PageListener listener){if(isOpen())throw new IllegalStateException("Attach navigation adapter before opening");this.listener=listener;}
    public boolean isOpen(){return current!=null;}
    public int menu(){return current==null?-1:current.menu;}
    public int page(){return current==null?0:current.number;}
    public boolean isPage(int menu,int page){return current!=null&&current.menu==menu&&current.number==page;}
    public static List<Page> pages(int menu){List<Page> p=PAGES.get(menu);return p==null?Collections.emptyList():p;}
    public static Page page(int menu,int number){for(Page p:pages(menu))if(p.number==number)return p;return null;}
    public static int menuForRibbonActor(int actor){return actor>=0&&actor<RIBBON_MENUS.length?RIBBON_MENUS[actor]:-1;}
    public static int menuForQuickActor(int actor){return actor>=0&&actor<QUICK_MENUS.length?QUICK_MENUS[actor]:-1;}
    public static boolean isOpenRequest(Native950Actions.InterfaceAction a){
        return a.option()==1&&a.itemId()==-1&&((a.interfaceId()==1431&&a.componentId()==0&&menuForRibbonActor(a.slot())>=0)
                ||(a.interfaceId()==1433&&a.componentId()==6&&menuForQuickActor(a.slot())>=0));
    }
    void bootstrap(){channel.write(Native950Packets.interfaceEvents(1433,6,0,5,2));}
    boolean handle(Native950Actions.InterfaceAction a){
        if(isOpenRequest(a)){
            guide.verifyBeforeOpen();int menu=a.interfaceId()==1431?menuForRibbonActor(a.slot()):menuForQuickActor(a.slot());
            if(menu==7){
                // The retail catalogue depends on an external account/store service.
                channel.write(Native950Packets.runClientScript(8179));
                if(isOpen())render();else{
                    channel.write(Native950Packets.varcLarge(2911,-1));
                    channel.write(Native950Packets.runClientScript(8290,1));
                    channel.write(Native950Packets.hideInterface(ROOT,WRAPPER,true));
                }
                channel.write(Native950Packets.gameMessage(0,"The Marketplace is not available on this local server."));return true;
            }
            open(menu,remembered[menu]);return true;
        }
        if(!isOpen())return false;
        // Page adapters own their own operation/slot/item contracts (Quest list includes ops2/3).
        if(a.interfaceId()==ROOT&&a.option()==1&&a.itemId()==-1){
            if(a.componentId()==717&&a.slot()==1||a.componentId()==8&&a.slot()==-1){close();return true;}
            if(a.componentId()==714&&a.slot()>=3&&a.slot()<=23&&(a.slot()+1)%4==0){
                int number=(a.slot()+1)/4;Page next=page(current.menu,number);
                if(next==null||next.hidden){render();return true;}
                open(current.menu,number);return true;
            }
        }
        return listener!=null&&listener.handle(a);
    }
    void openSkills(int skill){
        if(isPage(0,2)){guide.selectSkill(skill,true);return;}
        guide.selectSkill(skill,false);open(0,2);
    }
    private void open(int menu,int number){
        Page next=page(menu,number);
        if(next==null||next.hidden){for(Page p:pages(menu))if(!p.hidden){next=p;break;}}
        if(next==null||next.hidden)return;
        if(player.isDead()||player.isLocked()||player.closeInterfaceLocked||player.isNative950ForceMovementActive()||player.getNextWorldTile()!=null)return;
        if(current==next){render();return;}
        boolean initial=!isOpen();
        if(!initial)removePage();
        current=next;remembered[menu]=next.number;
        selectPage();
        if(initial){
            channel.write(Native950Packets.runClientScript(8179));
            channel.write(Native950Packets.runClientScript(8180,1,1));
            channel.write(Native950Packets.interfaceEvents(ROOT,8,-1,-1,252));
            channel.write(Native950Packets.openSub(ROOT,HOST,SHELL,true));
            player.getInterfaceManager().registerNativeOpen(SHELL,ROOT,HOST);
            for(int bit:new int[]{19029,19031,19032,19033,47565,60056,19004})channel.write(Native950Packets.varbitSmall(bit,0));
        }
        for(int slot=0;slot<5;slot++)if(next.faces[slot]>=0)mount(next.faces[slot],SHELL,3+slot*2);
        render();
        if(isPage(0,2))guide.attachContent();
        if(isPage(0,3)){
            channel.write(Native950Packets.interfaceText(1463,21,player.getDisplayName()));
            //8682 binds Loadout1474:8 to the same container93 and native options as HUD1473:5.
            channel.write(Native950Packets.interfaceEvents(1474,8,0,27,Native950InventoryMenu.EVENT_MASK));
        }
        if(listener!=null)listener.opened(menu,next.number);
    }
    private void selectPage(){
        channel.write(Native950Packets.varbitSmall(18994,current.menu));
        channel.write(Native950Packets.varbitSmall(PAGE_BITS[current.menu],current.number));
        channel.write(Native950Packets.varcLarge(2911,current.menu));
    }
    private void render(){
        selectPage();
        channel.write(Native950Packets.hideInterface(ROOT,714,false));
        channel.write(Native950Packets.runClientScript(8288,current.menu));
        channel.write(Native950Packets.runClientScript(8193));
        for(int slot=0;slot<5;slot++)if(current.faces[slot]>=0){
            channel.write(Native950Packets.runClientScript(8283,current.structure,slot));
            channel.write(Native950Packets.hideInterface(SHELL,3+slot*2,false));
        }
        channel.write(Native950Packets.hideInterface(SHELL,1,true));
        channel.write(Native950Packets.hideInterface(ROOT,WRAPPER,false));
        for(Page p:pages(current.menu))if(!p.hidden)channel.write(Native950Packets.interfaceEvents(ROOT,714,p.number*4-1,p.number*4-1,2));
        channel.write(Native950Packets.interfaceEvents(ROOT,717,1,1,2));
        if(mounted.containsKey(1462)&&player.getInterfaceManager().getInterfaceParentId(1462)==mounted.get(1462))
            refreshEquipment();
    }
    private void refreshEquipment(){
        //8468 onLoad installs updates but does not draw;8471 selects the native layout from final dimensions.
        channel.write(Native950Packets.runClientScript(8471,(1462<<16)|3,94));
    }
    private void mount(int face,int parent,int component){
        int previous=player.getInterfaceManager().getInterfaceParentId(face),target=parent<<16|component;
        if(previous!=-1&&previous!=target){
            displaced.put(face,previous);
            channel.write(Native950Packets.closeSub(previous>>>16,previous&65535));
            player.getInterfaceManager().unregisterNativeOpen(face);
        }
        channel.write(Native950Packets.openSub(parent,component,face,true));
        player.getInterfaceManager().registerNativeOpen(face,parent,component);mounted.put(face,target);
    }
    private void removePage(){
        if(current==null)return;
        if(listener!=null)listener.closed(current.menu,current.number);
        if(isPage(0,3))channel.write(Native950Packets.interfaceEvents(1474,8,0,27,0));
        if(isPage(0,2))guide.detachContent();
        for(Map.Entry<Integer,Integer> e:mounted.entrySet()){
            // A newer owner may already have replaced this interface; never retire it.
            if(player.getInterfaceManager().getInterfaceParentId(e.getKey())!=e.getValue())continue;
            channel.write(Native950Packets.closeSub(e.getValue()>>>16,e.getValue()&65535));
            player.getInterfaceManager().unregisterNativeOpen(e.getKey());
        }
        mounted.clear();
        for(Map.Entry<Integer,Integer> e:displaced.entrySet())if(player.getInterfaceManager().getInterfaceParentId(e.getKey())==-1){
            channel.write(Native950Packets.openSub(e.getValue()>>>16,e.getValue()&65535,e.getKey(),true));
            player.getInterfaceManager().registerNativeOpen(e.getKey(),e.getValue()>>>16,e.getValue()&65535);
            if(e.getKey()==1462)refreshEquipment();
        }
        displaced.clear();
    }
    public void close(){
        if(!isOpen())return;
        removePage();current=null;
        if(player.getInterfaceManager().getInterfaceParentId(SHELL)!=(ROOT<<16|HOST))return;
        channel.write(Native950Packets.runClientScript(8179));
        channel.write(Native950Packets.runClientScript(8180,1,1));
        channel.write(Native950Packets.varcLarge(2911,-1));
        channel.write(Native950Packets.runClientScript(8290,1));
        channel.write(Native950Packets.closeSub(ROOT,HOST));
        player.getInterfaceManager().unregisterNativeOpen(SHELL);
        channel.write(Native950Packets.hideInterface(ROOT,WRAPPER,true));
        channel.write(Native950Packets.interfaceEvents(ROOT,8,-1,-1,254));
    }
    private static Properties load(){
        Properties p=new Properties();
        try(InputStream in=Native950Navigation.class.getResourceAsStream("/native950/navigation-950.properties")){
            if(in==null)throw new IllegalStateException("Missing950 navigation catalog");p.load(in);
        }catch(java.io.IOException ex){throw new IllegalStateException(ex);}
        if(!"1".equals(p.getProperty("catalog.version")))throw new IllegalStateException("Unknown navigation catalog");return p;
    }
    private static int[] ints(String text){String[] p=text.split(",");int[] result=new int[p.length];for(int i=0;i<p.length;i++)result[i]=Integer.parseInt(p[i]);return result;}
    private static Map<Integer,List<Page>> pages(){
        Map<Integer,List<Page>> result=new LinkedHashMap<>();
        for(int menu:ints(DATA.getProperty("menus"))){List<Page> pages=new ArrayList<>();for(int number:ints(DATA.getProperty("menu."+menu+".pages")))pages.add(new Page(menu,number));result.put(menu,Collections.unmodifiableList(pages));}
        return Collections.unmodifiableMap(result);
    }
    public static synchronized void verify(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Management navigation requires the paired950 cache");
        if(verifiedStore==Cache.STORE)return;
        for(String key:DATA.stringPropertyNames()){
            if(key.startsWith("pin.")){
                String[] ids=key.substring(4).split("/");byte[] bytes=Cache.STORE.getIndexes()[Integer.parseInt(ids[0])].getFile(Integer.parseInt(ids[1]),Integer.parseInt(ids[2]));
                MessageDigest digest=digest();if(bytes==null)throw new IllegalStateException("Missing navigation "+key);digest.update(bytes);requireHash(digest,DATA.getProperty(key),key);
            }else if(key.startsWith("face.")){
                int face=Integer.parseInt(key.substring(5));String[] value=DATA.getProperty(key).split(",");int count=Integer.parseInt(value[0]);
                if(Cache.STORE.getIndexes()[3].getValidFilesCount(face)!=count)throw new IllegalStateException("Changed navigation interface "+face);
                MessageDigest digest=digest();for(int child=0;child<count;child++){byte[] bytes=Cache.STORE.getIndexes()[3].getFile(face,child);if(bytes==null)throw new IllegalStateException("Missing navigation child "+face+":"+child);digest.update(bytes);}requireHash(digest,value[1],key);
            }
        }
        verifiedStore=Cache.STORE;
    }
    private static MessageDigest digest(){try{return MessageDigest.getInstance("SHA-256");}catch(java.security.NoSuchAlgorithmException ex){throw new AssertionError(ex);}}
    private static void requireHash(MessageDigest digest,String expected,String key){StringBuilder hash=new StringBuilder();for(byte b:digest.digest())hash.append(String.format("%02x",b&255));if(!expected.equals(hash.toString()))throw new IllegalStateException("Changed navigation cache "+key);}
}
