package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Actions;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.util.*;
import java.util.function.Consumer;

/** One session owns the existing native management shell and its operation actors. */
final class Native950DeveloperConsole {
    static final int SHELL=1448,ACTORS=5,INIT=21124,BUTTON=21125,TEXT=21126;
    private static final int PAGE_SIZE=6, BROWSER_WINDOW=100;
    private final Map<Integer,Native950DeveloperCatalogue.Entry> rowEntities=new HashMap<>();
    private final Map<Integer,Runnable> collectionRows=new HashMap<>();
    private String domain="Home",section="";
    private String[] selectedTeleport;
    private String lastCollection="";
    private void viewport(int count){
        String key=domain+"/"+section+"/"+browser+"/"+query+"/"+page+"/"+variantFamily;
        write(Native950Packets.runClientScript(21151,count,key.equals(lastCollection)?0:1));lastCollection=key;
    }
    private Native950EquipmentCatalogue.Entry itemSelection;
    private final LinkedHashMap<Integer,Native950EquipmentCatalogue.Entry> recentItems=new LinkedHashMap<>();
    private final Set<Integer> favouriteItems=new LinkedHashSet<>();
    private int previewChild,previewTitle,previewInfo,idleActor,attackActor;private Native950DeveloperPreview preview;
    private final int[] zoomActors=new int[3];
    private static final Map<Player,Native950DeveloperConsole> OWNERS=new IdentityHashMap<>();
    private final Player player;private final Channel channel;private final Runnable prepare,verify;
    private final Consumer<String> execute;private final Map<Integer,Runnable> buttons=new HashMap<>();
    private Set<String> favourites=new LinkedHashSet<>();private boolean open,confirm,awaitingNative;
    private String category="Favourites",query="",status="";
    private int page,textChild;private Native950DeveloperActions.Action selected;
    private List<String> values=Collections.emptyList();private Consumer<String> input;
    private String lastState="";
    private final Map<Integer,Native950DeveloperActions.Action> rowActions=new HashMap<>();
    private List<String> report=Collections.emptyList();private int reportPage;private long selectedAt,renderStarted;
    private boolean advanced,savePlacement;private String clearConfirmation="";private long clearConfirmationEpoch;
    private String processingNotification="",doubleClickNonce="";private long doubleClickEpoch;
    private Native950DeveloperActions.Action doubleClickAction;
    private long epoch;
    private Native950GamevalLookup.Entry symbol;
    private boolean bossOnly,npcAdvanced;
    private String variantFamily="";
    private String browser="";private Native950DeveloperCatalogue.Entry entity;
    private int amount=1,rotation,typeIndex;private boolean repeat;
    private int placementSerial;
    private Native950DeveloperPlacement.Request placement;
    private Native950DeveloperWorldEdits.Edit editSelection,moving;
    private final Native950DeveloperWorldEdits.History history=new Native950DeveloperWorldEdits.History();
    Native950DeveloperConsole(Player p,Channel c,Runnable prepare,Consumer<String> execute){this(p,c,prepare,execute,Native950DeveloperConsole::verify);}
    Native950DeveloperConsole(Player p,Channel c,Runnable prepare,Consumer<String> execute,Runnable verify){
        player=p;channel=c;this.prepare=prepare;this.execute=execute;this.verify=verify;
        synchronized(OWNERS){OWNERS.put(p,this);}
    }
    static void openFor(Player p){Native950DeveloperConsole console; synchronized(OWNERS){console=OWNERS.get(p);}if(console!=null)console.open();}
    static void placementsChanged(Player p){
        Native950DeveloperConsole c; synchronized(OWNERS){c=OWNERS.get(p);}
        if(c!=null){if(c.placement!=null)c.close();c.history.clear();c.editSelection=null;c.moving=null;if(c.open)c.render();}
    }
    static void gamevalsFor(Player p,String query){
        Native950DeveloperConsole c; synchronized(OWNERS){c=OWNERS.get(p);}
        if(c!=null&&c.mayAct()){if(c.placement!=null)c.close();if(!c.open)c.open();if(c.open){c.browseGamevals();c.query=query;c.render();}}
    }
    static void bossesFor(Player p,String query){
        Native950DeveloperConsole c;synchronized(OWNERS){c=OWNERS.get(p);}
        if(c!=null&&c.mayAct()){if(c.placement!=null)c.close();if(!c.open)c.open();if(c.open){c.browse("NPC");c.bossOnly=true;c.query=query;c.render();}}
    }
    boolean isOpen(){return open;}
    void open(){
        if(!Native950DeveloperActions.permitted(player,channel)){message("Developer permission is required.");return;}
        if(player.isDead()||player.isLocked()||player.closeInterfaceLocked||player.isNative950ForceMovementActive()||player.getNextWorldTile()!=null)return;
        verify.run();prepare.run();
        try{favourites=Native950DeveloperPreferences.load(player.getUsername());}catch(java.io.IOException e){message("Could not load developer favourites; using this session only.");}
        open=true;confirm=false;input=null;moving=null;awaitingNative=true;buttons.clear();epoch++;
        write(Native950Packets.runClientScript(8179));write(Native950Packets.runClientScript(8180,1,1));
        write(Native950Packets.interfaceEvents(1477,8,-1,-1,252));
        write(Native950Packets.openSub(1477,715,SHELL,true));player.getInterfaceManager().registerNativeOpen(SHELL,1477,715);
        // Native varc/onLoad callbacks run after server scripts. Rendering here
        // loses the actors to CS8193. CS8286 now acknowledges its real refresh;
        // only the subsequent server round trip publishes our page.
        write(Native950Packets.runClientScript(INIT+6,"SHNORKSCAPE Developer Console"));
        write(Native950Packets.varbitSmall(18994,1));write(Native950Packets.varbitSmall(29607,2));write(Native950Packets.varcLarge(2911,1));
        for(int bit:new int[]{19029,19031,19032,19033,47565,60056,19004})write(Native950Packets.varbitSmall(bit,0));
        write(Native950Packets.runClientScript(8288,1));write(Native950Packets.runClientScript(8193));
        write(Native950Packets.hideInterface(SHELL,1,true));write(Native950Packets.hideInterface(1477,708,true));
        write(Native950Packets.interfaceEvents(1477,717,1,1,2));
        Native950BugTest.event(player,"developer-console","awaiting-native-ready","epoch",epoch);
    }
    void tick(){
        if(!open)return;
        if(!Native950DeveloperActions.permitted(player,channel)||player.isDead()||player.hasFinished()||player.isLocked()){close();return;}
        if(player.getInterfaceManager().getInterfaceParentId(SHELL)!=(1477<<16|715)){open=false;buttons.clear();input=null;placement=null;moving=null;return;}
        if(placement!=null){if(System.currentTimeMillis()>placement.expires){close();status="Placement timed out.";}return;}
        if(awaitingNative)return;
        String current=stateKey();if(input==null&&!lastState.equals(current))render();
    }
    private String stateKey(){StringBuilder b=new StringBuilder();for(Native950DeveloperActions.Action a:Native950DeveloperActions.all())b.append(Native950DeveloperActions.state(a,player));return b.toString();}
    boolean handle(Native950Actions.InterfaceAction a){
        if(!open)return false;
        if(!mayAct()){close();return true;}
        if(a.interfaceId()==1477&&a.option()==1&&a.itemId()==-1&&((a.componentId()==717&&a.slot()==1)||(a.componentId()==8&&a.slot()==-1))){if(input!=null)closeInput();else close();return true;}
        if(a.interfaceId()!=SHELL)return false;
        if(input!=null||a.componentId()!=ACTORS||a.option()!=1||a.itemId()!=-1)return true;
        // The native onOp hook supplies the render epoch. IF_BUTTON alone cannot
        // distinguish an old row from the new action currently occupying its slot.
        return true;
    }
    boolean handle(Native950Actions.StringDialogueAction a){
        String notification=a.text()==null?"":a.text();
        if(notification.equals("__devready")){
            if(open&&awaitingNative&&placement==null&&mayAct()&&player.getInterfaceManager().getInterfaceParentId(SHELL)==(1477<<16|715)){
                awaitingNative=false;Native950BugTest.event(player,"developer-console","native-ready","epoch",epoch);render();
            }return true;
        }
        if(notification.startsWith("__devcancel:")){
            if(placement!=null&&notification.equals("__devcancel:"+placement.slot)){close();status="Placement cancelled.";}return true;
        }
        if(notification.startsWith("__devsearch:")){
            if(!open||awaitingNative||input!=null||placement!=null||!mayAct())return true;
            String[] fields=notification.split(":",3);
            try{if(fields.length==3&&Long.parseLong(fields[1])==epoch&&fields[2].length()<=80){query=fields[2].trim();page=0;entity=null;itemSelection=null;selectedTeleport=null;variantFamily="";render();}}
            catch(NumberFormatException ignored){}return true;
        }
        if(notification.startsWith("__devop:")){
            if(!open||awaitingNative||input!=null||placement!=null||!mayAct())return true;
            String[] fields=notification.split(":",-1);
            try{
                if(fields.length!=3&&fields.length!=4)return true;
                if(Long.parseLong(fields[1])!=epoch){
                    // A physical double-click may enqueue both callbacks before the first redraw.
                    // Only that exact inspected, zero-argument action gets a one-shot continuation.
                    if(fields.length==3&&notification.equals(doubleClickNonce)&&epoch==doubleClickEpoch&&selected==doubleClickAction
                        &&System.currentTimeMillis()-selectedAt<1200){doubleClickNonce="";runSelected();}
                    return true;
                }
                Runnable collectionRow=collectionRows.get(Integer.parseInt(fields[2]));
                if(collectionRow!=null){if(fields.length==3)collectionRow.run();return true;}
                if(fields.length==4&&rowEntities.containsKey(Integer.parseInt(fields[2]))){
                    Native950DeveloperCatalogue.Entry row=rowEntities.get(Integer.parseInt(fields[2]));
                    int op=Integer.parseInt(fields[3]);if(op<2||op>4)return true;
                    selectEntity(row);
                    if(op==2)spawnNear();else if(op==3)beginPlacement();else showReport(Native950CombatInspector.npc(row.id));
                    return true;
                }
                if(fields.length==4){
                    Native950DeveloperActions.Action row=rowActions.get(Integer.parseInt(fields[2]));if(row==null)return true;
                    int op=Integer.parseInt(fields[3]);
                    if(op==2){selected=row;values=row.defaults();confirm=false;report=Collections.emptyList();if(row.parameters.isEmpty())runSelected();else select(row);}
                    else if(op==3)favourite(row);else if(op==4)select(row);return true;
                }
                Native950DeveloperCatalogue.Entry row=rowEntities.get(Integer.parseInt(fields[2]));
                if(row!=null){selectEntity(row);return true;}
                Runnable operation=buttons.get(Integer.parseInt(fields[2]));
                if(operation!=null){Native950BugTest.event(player,"developer-console","operation","epoch",epoch,"actor",fields[2]);processingNotification=notification;try{operation.run();}finally{processingNotification="";}}
            }catch(IllegalArgumentException|IllegalStateException invalid){status=invalid.getMessage();report=Collections.singletonList(status);reportPage=0;if(open)render();}
            return true;
        }
        if(!open||input==null)return false;if(!mayAct()){close();return true;}Consumer<String> callback=input;closeInput();
        if(!mayAct()){close();return true;}
        String text=a.text()==null?"":a.text().trim();
        if(text.length()>80){status="Please use at most 80 characters.";render();return true;}
        try{callback.accept(text);}catch(IllegalArgumentException e){status=e.getMessage();}render();return true;
    }
    boolean cancelInput(){if(input==null)return false;closeInput();return true;}
    private void prompt(String title,Consumer<String> callback){
        if(input!=null)return;input=callback;
        write(Native950Packets.openSub(1477,749,1418,true));write(Native950Packets.openSub(1418,2,1469,true));
        player.getInterfaceManager().registerNativeOpen(1418,1477,749);player.getInterfaceManager().registerNativeOpen(1469,1418,2);
        write(Native950Packets.hideInterface(1477,747,false));write(Native950Packets.runClientScript(110,title));
    }
    private void closeInput(){
        if(input==null)return;input=null;
        write(Native950Packets.closeSub(1418,2));write(Native950Packets.closeSub(1477,749));
        player.getInterfaceManager().unregisterNativeOpen(1469);player.getInterfaceManager().unregisterNativeOpen(1418);
        write(Native950Packets.hideInterface(1477,747,true));write(Native950Packets.runClientScript(1364));
    }
    void close(){
        moving=null;if(!open)return;closeInput();open=false;confirm=false;awaitingNative=false;buttons.clear();
        if(placement!=null){placement=null;write(Native950Packets.runClientScript(INIT+5));}
        if(player.getInterfaceManager().getInterfaceParentId(SHELL)!=(1477<<16|715))return;
        write(Native950Packets.runClientScript(21140));
        write(Native950Packets.runClientScript(INIT+6,""));
        write(Native950Packets.interfaceEvents(SHELL,ACTORS,0,255,0));
        write(Native950Packets.runClientScript(8179));write(Native950Packets.runClientScript(8180,1,1));
        write(Native950Packets.varcLarge(2911,-1));write(Native950Packets.runClientScript(8290,1));
        write(Native950Packets.closeSub(1477,715));player.getInterfaceManager().unregisterNativeOpen(SHELL);
        write(Native950Packets.hideInterface(1477,708,true));write(Native950Packets.interfaceEvents(1477,8,-1,-1,254));
    }
    void dispose(){close();Native950DeveloperWorldEdits.cleanup(player);history.clear();synchronized(OWNERS){if(OWNERS.get(player)==this)OWNERS.remove(player);}}
    private void select(Native950DeveloperActions.Action a){
        boolean again=selected==a&&System.currentTimeMillis()-selectedAt<1200;selectedAt=System.currentTimeMillis();
        selected=a;values=a.defaults();confirm=false;report=Collections.emptyList();
        if(semantic(a))return;
        if(a.parameters.isEmpty()&&!a.confirmation&&(again||!Native950DeveloperActions.state(a,player).isEmpty())){runSelected();return;}
        status=a.parameters.isEmpty()?"Double-click or right-click Execute Now to run this action.":"Configure the controls, then select Execute.";render();
        if(a.configured&&a.parameters.isEmpty()&&!a.confirmation){doubleClickNonce=processingNotification;doubleClickEpoch=epoch;doubleClickAction=a;}
    }
    private boolean semantic(Native950DeveloperActions.Action a){
        if(Arrays.asList("clearnpcs","removenpc","clearobjects").contains(a.id)){navigate("Spawns");return true;}
        if(Arrays.asList("npc","findnpc","npcinfo").contains(a.id)){browse("NPC");return true;}
        if(Arrays.asList("obj","object","findobject","findobj").contains(a.id)){browse("Object");return true;}
        if(Arrays.asList("gameval","gamevals").contains(a.id)){browseGamevals();return true;}
        if(Arrays.asList("bosses","bossinfo","bossfight").contains(a.id)){browse("NPC");bossOnly=true;render();return true;}
        return false;
    }
    private static String label(Native950DeveloperActions.Action a){
        switch(a.id){case "heal":return "Heal";case "almighty":return "Developer combat mode";case "god":return "Damage immunity";
        case "items":return "Equipment Library";case "bugtest":return "Bug Test diagnostics";case "wars":return "War's Retreat";
        case "npc":case "findnpc":return "Browse NPCs";case "obj":return "Browse objects";case "bosses":return "Boss encounters";
        default:return Character.toUpperCase(a.id.charAt(0))+a.id.substring(1);}
    }
    private void favourite(Native950DeveloperActions.Action a){
        Set<String> changed=new LinkedHashSet<>(favourites);if(!changed.remove(a.id))changed.add(a.id);
        try{Native950DeveloperPreferences.save(player.getUsername(),changed);favourites=changed;status="Favourites saved.";}
        catch(java.io.IOException e){status="Could not save favourites; existing preferences preserved.";}render();
    }
    private void runSelected(){
        if(selected==null)return;if(!selected.configured){status="This raw diagnostic is available as a typed command only.";showReport(Arrays.asList(status,selected.usage));return;}if(semantic(selected))return;
        if(selected.confirmation&&!confirm){confirm=true;status="Review this change, then select Confirm action.";render();return;}
        String command=selected.command(values),name=label(selected);confirm=false;
        try{List<String> lines=Native950DeveloperOutput.run(player,channel,()->execute.accept(command));
            report=lines;reportPage=0;status=lines.isEmpty()?name+" completed.":lines.get(0);
        }catch(IllegalArgumentException|IllegalStateException e){status=e.getMessage();report=Collections.singletonList(status);reportPage=0;}
        if(open)render();
    }
    private void showReport(List<String> lines){report=lines;reportPage=0;render();}
    private void renderReport(){
        text(500,43,229,35,17514,"Details / Results");
        // Bound each page by native text height, including long diagnostic lines.
        List<String> chunks=new ArrayList<>();for(String line:report){String value=safe(line.replaceAll("<[^>]*>",""));while(value.length()>32){int cut=value.lastIndexOf(' ',32);if(cut<12)cut=32;chunks.add(value.substring(0,cut));value=value.substring(cut).trim();}chunks.add(value);}
        int pages=Math.max(1,(chunks.size()+13)/14);reportPage=Math.max(0,Math.min(reportPage,pages-1));
        text(500,85,227,260,2100,String.join("<br>",chunks.subList(reportPage*14,Math.min(chunks.size(),reportPage*14+14))));
        button(495,354,116,29,"Previous detail",false,()->{if(reportPage>0){reportPage--;render();}});
        button(616,354,118,29,"Next detail",false,()->{if(reportPage+1<pages){reportPage++;render();}});
        button(495,387,239,28,"Back ("+(reportPage+1)+" / "+pages+")",false,()->{report=Collections.emptyList();render();});
    }
    private void render(){
        if(!open||awaitingNative)return;renderStarted=System.nanoTime();lastState=stateKey();buttons.clear();rowActions.clear();rowEntities.clear();collectionRows.clear();textChild=0;epoch++;
        write(Native950Packets.runClientScript(INIT));
        if(domain.equals("Home"))write(Native950Packets.runClientScript(21145));
        String[] tabs={"Home","NPCs","Objects","Items","Bosses","Combat","Teleports","Tools"};
        for(int i=0;i<tabs.length;i++){
            final String c=tabs[i];
            button(i*93,0,91,30,c,domain.equals(c),()->navigate(c));
        }
        if(domain.equals("Home")){write(Native950Packets.hideInterface(SHELL,4,true));renderHome();footer();return;}
        write(Native950Packets.runClientScript(21137,query,"__devsearch:"+epoch+":","Search "+(category.equals("Player")?"player actions":domain)+"..."));
        write(Native950Packets.interfaceEvents(SHELL,4,0,0,2));
        int searchActor=buttons.size();button(334,38,74,30,"Search",false,()->{});
        write(Native950Packets.runClientScript(21152,searchActor,"__devsearch:"+epoch+":",query));
        button(413,38,68,30,"Clear",false,()->{query="";page=0;entity=null;itemSelection=null;selectedTeleport=null;variantFamily="";render();});
        renderContext();
        if(domain.equals("Items")){renderItems();footer();return;}
        if(domain.equals("Teleports")){renderTeleports();footer();return;}
        if(category.equals("Player")){renderPlayer();footer();return;}
        if(domain.equals("Combat")&&(section.isEmpty()||section.equals("Quick Controls")||section.equals("Loadouts"))){renderCombat();footer();return;}
        if(browser.equals("Gameval")){renderGamevals();footer();return;}
        if(browser.equals("Edits")){renderEdits();footer();return;}
        if(!browser.isEmpty()){
            renderBrowser();footer();return;
        }
        List<Native950DeveloperActions.Action> matches=Native950DeveloperActions.search(category,query,favourites);
        int pages=Math.max(1,(matches.size()+PAGE_SIZE-1)/PAGE_SIZE);page=Math.max(0,Math.min(page,pages-1));
        for(int i=0;i<PAGE_SIZE;i++){
            int index=page*PAGE_SIZE+i;if(index>=matches.size())break;
            Native950DeveloperActions.Action a=matches.get(index);
            String title=(favourites.contains(a.id)?"<col=ffd166>* </col>":"")+label(a)+"  "+Native950DeveloperActions.state(a,player);
            int rowY=77+i*50;
            rowActions.put(buttons.size(),a);button(171,rowY,310,47,"",a==selected,()->select(a));
            text(181,rowY+3,290,19,2100,"<col=ffd479>"+title+"</col>");
            text(181,rowY+23,290,19,2100,"<col=b0a89a>"+safe(shortDescription(a.description))+"</col>");
        }
        if(matches.isEmpty())text(180,92,292,80,2100,"No matching actions.<br>Search across all categories or add a favourite.");
        button(171,387,68,28,"Previous",false,()->{if(page>0){page--;render();}});
        text(245,389,161,24,2100,(page+1)+" / "+pages+"   |   "+matches.size()+" actions");
        button(412,387,69,28,"Next",false,()->{if(page+1<pages){page++;render();}});
        if(!report.isEmpty())renderReport();else if(selected==null){
            text(500,43,229,35,17514,"SHNORKSCAPE");
            text(500,88,226,120,2100,"Select an action to inspect its details and parameters.<br><br>Use Search across every command, or choose a category.<br><br>Favourites are saved for your account.");
            text(500,324,226,54,2100,"Items opens your Equipment Library.<br>Spawns manages your placed NPCs and objects.");
        }else{
            text(500,42,229,45,2100,titleLines(label(selected))+" "+Native950DeveloperActions.state(selected,player));
            text(500,82,227,93,2100,safe(selected.description));
            text(500,174,227,42,2100,"<col=ffd479>Usage</col><br>"+safe(selected.usage));
            for(int i=0;i<selected.parameters.size();i++){
                final int n=i;Native950DeveloperActions.Parameter p=selected.parameters.get(i);
                button(495,219+i*33,239,30,p.name+": "+safe(values.get(i)),false,()->{
                    confirm=false;
                    if(p.choices.length>0){int at=Arrays.asList(p.choices).indexOf(values.get(n));values.set(n,p.choices[(at+1)%p.choices.length]);render();}
                    else prompt(p.name+ (p.number?" ("+p.min+"-"+p.max+")":"")+":",v->values.set(n,p.validate(v)));
                });
            }
            button(495,358,239,27,favourites.contains(selected.id)?"Remove favourite":"Add favourite",false,()->favourite(selected));
            button(495,387,239,28,confirm?"Confirm action":selected.configured?"Execute / Activate":"Raw diagnostic (typed only)",!selected.configured,this::runSelected);
        }
        if(selected==null&&report.isEmpty()){
            button(495,219,239,30,"Browse NPCs",false,()->browse("NPC"));
            button(495,252,239,30,"Browse objects",false,()->browse("Object"));
            button(495,285,239,30,"Cache / Gamevals",false,this::browseGamevals);
        }
        footer();
    }
    private void footer(){
        write(Native950Packets.hideInterface(1477,708,false));
        text(0,421,734,25,2100,"<col=86efac>Developer</col>  |  "+safe(player.getDisplayName())+(status.isEmpty()?"":"  |  "+safe(status)));
        write(Native950Packets.interfaceEvents(SHELL,ACTORS,0,Math.max(0,buttons.size()-1),30));
        Native950BugTest.event(player,"developer-console","render","micros",(System.nanoTime()-renderStarted)/1000,"actors",buttons.size(),"text",textChild);
        channel.flush();
    }
    private void navigate(String c){
        c=c.equals("World")?"Objects":c;
        domain=c;section="";query="";page=0;variantFamily="";bossOnly=c.equals("Bosses");npcAdvanced=false;
        report=Collections.emptyList();clearConfirmation="";category=c;confirm=false;entity=null;selected=null;
        browser=c.equals("NPCs")||c.equals("Bosses")?"NPC":c.equals("Objects")?"Object":c.equals("Spawns")?"Edits":"";
        if(Arrays.asList("Commands","Favourites","Settings","Spawns","Player","Quests").contains(c))domain="Tools";
        itemSelection=null;selectedTeleport=null;preview=null;lastCollection="";
        status="";render();
    }
    private boolean toggleActive(String command){Native950DeveloperActions.Action a=Native950DeveloperActions.find(command.substring(2));return a!=null&&Native950DeveloperActions.state(a,player).contains(">ON<");}
    private String toggleLabel(String name,String command){Native950DeveloperActions.Action a=Native950DeveloperActions.find(command.substring(2));String state=a==null?"":Native950DeveloperActions.state(a,player);return name+(state.isEmpty()?"":" ["+state+"]");}
    private void quick(String command){
        List<String> output=Native950DeveloperOutput.run(player,channel,()->execute.accept(command));
        status=output.isEmpty()?"Action completed.":output.get(0);if(open)render();
    }
    private void homeIcon(String name,int x,int y){
        if(Cache.STORE==null)return;
        for(Native950EquipmentCatalogue.Entry e:Native950DeveloperItems.search(name))if(e.name.equalsIgnoreCase(name)){
            write(Native950Packets.runClientScript(21146,textChild++,e.id,x,y,28,28));return;
        }
    }
    private void renderHome(){
        text(8,44,720,28,17514,"QUICK ACTIONS");
        String[][] actions={{"Heal",";;heal"},{"Almighty (DM)",";;almighty"},{"Open bank",";;bank"},{"War's Retreat",";;wars"},{"Reset cooldowns",";;resetcooldowns"},{"Equipment Library",";;items"}};
        for(int n=0;n<actions.length;n++){final String[] a=actions[n];button(8+(n%3)*244,80+(n/3)*43,236,37,toggleLabel(a[0],a[1]),toggleActive(a[1]),()->quick(a[1]));}
        String[] icons={"Shark","Overload flask (6)","Coins","Law rune","","Torva platebody"};
        for(int n=0;n<icons.length;n++)if(!icons[n].isEmpty())homeIcon(icons[n],15+(n%3)*244,84+(n/3)*43);
        text(8,172,720,28,17514,"EXPLORE / TEST");
        String[] domains={"NPCs","Objects","Items","Bosses","Combat","Teleports","Tools","Spawns","Player"};
        for(int n=0;n<domains.length;n++){final String d=domains[n];button(8+(n%3)*244,209+(n/3)*38,236,33,d,false,()->navigate(d));}
        String[] domainIcons={"Bones","Spade","Torva full helm","Dragon bones","Rune sword","Law rune","Hammer","","Rune platebody"};
        for(int n=0;n<domainIcons.length;n++)if(!domainIcons[n].isEmpty())homeIcon(domainIcons[n],15+(n%3)*244,211+(n/3)*38);
        text(8,331,350,25,17514,"RECENT ITEMS");text(377,331,350,25,17514,"FAVOURITE ACTIONS");
        List<Native950EquipmentCatalogue.Entry> recent=new ArrayList<>(recentItems.values());Collections.reverse(recent);
        for(int n=0;n<Math.min(2,recent.size());n++){final Native950EquipmentCatalogue.Entry e=recent.get(n);button(8,362+n*27,350,25,e.name,false,()->{navigate("Items");itemSelection=e;render();});}
        int n=0;for(String id:favourites){Native950DeveloperActions.Action a=Native950DeveloperActions.find(id);if(a!=null&&a.parameters.isEmpty()){button(377,362+n*27,350,25,label(a),false,()->quick(a.command(a.defaults())));if(++n==2)break;}}
    }
    private void renderContext(){
        String[] entries;
        switch(domain){
            case "NPCs": entries=new String[]{"All NPCs","Bosses","All variants","My spawns"};break;
            case "Objects": entries=new String[]{"All Objects","Banking","Portals","My spawns"};break;
            case "Items": entries=new String[]{"All Items","Best Gear","Weapons","Armour","Necromancy","Consumables","Favourites","Recent"};break;
            case "Bosses": entries=new String[]{"All Bosses","Supported","Partial","Blocked","My spawns"};break;
            case "Combat": entries=new String[]{"Quick Controls","Loadouts","Abilities","NPC Inspector","Boss Inspector","Combat QA"};break;
            case "Teleports": entries=new String[]{"Destinations","Custom"};break;
            default: entries=new String[]{"Commands","Favourites","Gamevals / Cache","Player","Bug Test","World Editor","Settings"};break;
        }
        if(category.equals("Player"))entries=new String[]{"Overview","Loadouts","Commands"};
        if(browser.equals("Edits"))entries=new String[]{"All placements","My NPCs","My Objects","Temporary","Saved","Back to tools"};
        for(int n=0;n<entries.length;n++){final String s=entries[n];button(0,77+n*32,160,29,s,section.equals(s)||(section.isEmpty()&&n==0),()->{
            section=s;page=0;entity=null;itemSelection=null;selectedTeleport=null;report=Collections.emptyList();status="";
            if(s.equals("Back to tools")){navigate("Tools");return;}
            if(s.equals("My spawns")||s.equals("World Editor")){navigate("Spawns");return;}
            if(s.equals("Gamevals / Cache")){browseGamevals();return;}
            if(s.equals("Bug Test")){quick(";;bugtest");return;}
            if(domain.equals("Combat")){
                String id=s.equals("Abilities")?"abilityinfo":s.equals("NPC Inspector")?"npcinfo":s.equals("Boss Inspector")?"bossinfo":s.equals("Combat QA")?"combatqa":null;
                if(id!=null){select(Native950DeveloperActions.find(id));return;}
            }
            if(s.equals("Player")||s.equals("Settings")||s.equals("Commands")||s.equals("Favourites")&&!domain.equals("Items")){category=s;browser="";selected=null;}
            if(domain.equals("NPCs")){bossOnly=s.equals("Bosses");entity=null;variantFamily="";}
            if(domain.equals("Objects")){query=s.equals("Banking")?"bank":s.equals("Portals")?"portal":"";entity=null;}
            render();
        });}
    }
    private void renderItems(){
        if(!report.isEmpty()){renderReport();return;}
        if(section.equals("Best Gear")){
            text(181,88,300,32,17514,"BEST GEAR");
            String[] styles={"Melee","Ranged","Magic","Necromancy"};
            for(int n=0;n<4;n++){final int index=n;button(181,130+n*43,290,36,styles[n],false,()->{status=Native950DeveloperLoadouts.apply(player,Native950DeveloperLoadouts.builtins().get(index));render();});}
            write(Native950Packets.runClientScript(21155,textChild++,495,80,239,275,500));
            text(181,318,290,65,2100,"Equips a complete loadout.<br>Existing gear and supplies move safely to your bank.");
            button(495,387,239,28,"Open Equipment Library",false,()->quick(";;items"));return;
        }
        List<Native950EquipmentCatalogue.Entry> results=new ArrayList<>(section.equals("Recent")?recentItems.values():Native950DeveloperItems.search(query));
        if(section.equals("Favourites")){
            results.clear();for(int id:favouriteItems)results.addAll(Native950DeveloperItems.search(String.valueOf(id)));
        }
        if((section.equals("Recent")||section.equals("Favourites"))&&!query.isEmpty())results.removeIf(e->!e.name.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))&&!String.valueOf(e.id).equals(query));
        if(section.equals("Weapons"))results.removeIf(e->e.slot!=3&&e.slot!=5);
        if(section.equals("Armour"))results.removeIf(e->e.slot<0||e.slot==3||e.slot==5);
        if(section.equals("Necromancy"))results.removeIf(e->e.category!=4&&!e.name.toLowerCase(Locale.ROOT).contains("necrom"));
        if(section.equals("Consumables"))results.removeIf(e->e.slot>=0);
        int pages=Math.max(1,(results.size()+99)/100);page=Math.min(page,pages-1);int start=page*100,count=Math.min(100,results.size()-start);
        if(itemSelection==null||!results.contains(itemSelection))itemSelection=count==0?null:results.get(start);
        viewport(count);
        for(int n=0;n<count;n++){
            final Native950EquipmentCatalogue.Entry e=results.get(start+n);int actor=2000+n;
            collectionRows.put(actor,()->{itemSelection=e;recentItems.remove(e.id);recentItems.put(e.id,e);while(recentItems.size()>20)recentItems.remove(recentItems.keySet().iterator().next());render();});
            write(Native950Packets.runClientScript(21144,n,e.id,"<col=ffd479>"+safe(rowName(e.name,32))+"</col><br>"+(e.tier>0?"Tier "+e.tier:"Inventory item"),"__devop:"+epoch+":"+actor));
            if(itemSelection!=null&&itemSelection.id==e.id)write(Native950Packets.runClientScript(21154,n,3));
        }
        write(Native950Packets.runClientScript(21133));write(Native950Packets.interfaceEvents(SHELL,9,0,Math.max(0,count*3-1),2));
        button(171,387,76,28,"Earlier",false,()->{if(page>0){page--;render();}});
        text(251,389,145,24,2100,(page+1)+" / "+pages+" | "+results.size()+" items");
        button(402,387,79,28,"More",false,()->{if(page+1<pages){page++;render();}});
        if(itemSelection!=null){
            final Native950EquipmentCatalogue.Entry e=itemSelection;
            text(500,43,229,60,2100,titleLines(e.name));write(Native950Packets.runClientScript(21146,textChild++,e.id,578,103,64,64));
            text(500,173,229,58,2100,Native950DeveloperItems.details(e.id).replaceFirst("ID [0-9]+<br>",""));
            button(495,235,116,29,"Give 1",false,()->{status=Native950DeveloperItems.give(player,e.id,1);render();});
            button(616,235,118,29,"Give X",false,()->prompt("Quantity (1-1000000):",v->status=Native950DeveloperItems.give(player,e.id,Integer.parseInt(v))));
            button(495,269,116,29,"Equip",false,()->{status=Native950DeveloperItems.equip(player,e.id);render();});
            button(616,269,118,29,"Add to bank",false,()->prompt("Bank quantity (1-1000000):",v->status=Native950DeveloperItems.bank(player,e.id,Integer.parseInt(v))));
            button(495,303,239,29,favouriteItems.contains(e.id)?"Remove favourite":"Add favourite",false,()->{if(!favouriteItems.remove(e.id))favouriteItems.add(e.id);render();});
            button(495,337,239,29,"More info",false,()->showReport(Arrays.asList(e.name,Native950DeveloperItems.details(e.id).replace("<br>"," | "),"Shared Equipment Library safe index; infinite catalogue stock.","Favourites and Recent on this page are retained for this session.")));
        }else text(500,53,229,80,17514,"Search and select an item");
        button(495,387,239,28,"Open Equipment Library",false,()->quick(";;items"));
    }
    private void loadout(int index){status=Native950DeveloperLoadouts.apply(player,Native950DeveloperLoadouts.builtins().get(index));render();}
    private void renderPlayer(){
        int model=textChild++;write(Native950Packets.runClientScript(21155,model,180,80,300,280,380));
        write(Native950Packets.runClientScript(21156,model,180,80,300,280));
        String[] zoom={"Zoom -","Reset","Zoom +"};int[] change={75,0,-75};
        for(int n=0;n<3;n++){int actor=buttons.size();button(180+n*100,367,96,28,zoom[n],false,()->{});write(Native950Packets.runClientScript(21148,actor,model,change[n],380));}
        text(495,48,239,30,17514,safe(player.getDisplayName()));
        text(500,80,229,20,2100,"Player testing");
        text(500,225,229,22,2100,"<col=ffd479>LOADOUTS</col>");
        String[][] controls={{"Heal",";;heal"},{"Max stats",";;max"},{"Almighty / DM",";;almighty"},{"Reset cooldowns",";;resetcooldowns"}};
        for(int n=0;n<controls.length;n++){final String[] c=controls[n];button(495,100+n*34,239,29,toggleLabel(c[0],c[1]),toggleActive(c[1]),()->quick(c[1]));}
        String[] styles={"Melee","Ranged","Magic","Necromancy"};for(int n=0;n<4;n++){final int i=n;button(495+(n%2)*121,250+(n/2)*35,118,30,styles[n],false,()->loadout(i));}
        button(495,387,239,28,"Open Equipment Library",false,()->quick(";;items"));
    }
    private void renderCombat(){
        text(181,86,300,30,17514,"COMBAT CONTROLS");
        String[][] controls={{"Damage immunity",";;god"},{"Almighty / DM",";;almighty"},{"Infinite adrenaline",";;infadren"},{"Infinite prayer",";;infprayer"},{"Reset cooldowns",";;resetcooldowns"}};
        for(int n=0;n<controls.length;n++){final String[] c=controls[n];button(181,132+n*43,290,36,toggleLabel(c[0],c[1]),toggleActive(c[1]),()->quick(c[1]));}
        text(500,86,229,30,17514,"LOADOUTS");String[] styles={"Melee","Ranged","Magic","Necromancy"};
        for(int n=0;n<4;n++){final int i=n;button(495,132+n*43,239,36,styles[n],false,()->loadout(i));}
        button(495,344,239,36,"Player preview",false,()->navigate("Player"));
    }
    private void renderTeleports(){
        List<String[]> destinations=new ArrayList<>();
        if(!section.equals("Custom")){
            destinations.add(new String[]{"War's Retreat","Combat hub",";;wars"});
            destinations.add(new String[]{"Death's office","Death",";;death"});
            for(Native950DeveloperCatalogue.Entry e:Native950BossCatalogue.search("")){
                Native950BossCatalogue.Boss b=Native950BossCatalogue.find(e.id);
                if(b!=null&&b.route>=0)destinations.add(new String[]{b.name,b.location,";;bossgo "+b.id});
            }
        }
        try{for(Native950SavedLocations.Place p:Native950SavedLocations.list())destinations.add(new String[]{p.name,p.owner,";;tele "+p.x+" "+p.y+" "+p.plane});}
        catch(java.io.IOException e){status="Could not read saved destinations.";}
        destinations.removeIf(d->!d[0].toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)));
        int pages=Math.max(1,(destinations.size()+99)/100);page=Math.min(page,pages-1);int start=page*100,count=Math.min(100,destinations.size()-start);viewport(count);
        for(int n=0;n<count;n++){
            final String[] d=destinations.get(start+n);int actor=2000+n;
            collectionRows.put(actor,()->{status="Selected "+d[0];selectedTeleport=d;render();});
            write(Native950Packets.runClientScript(21150,n,"<col=ffd479>"+safe(d[0])+"</col><br>"+safe(d[1]),"__devop:"+epoch+":"+actor));
            if(selectedTeleport!=null&&selectedTeleport[2].equals(d[2]))write(Native950Packets.runClientScript(21154,n,2));
        }
        write(Native950Packets.runClientScript(21133));write(Native950Packets.interfaceEvents(SHELL,9,0,Math.max(0,count*2-1),2));
        if(page>0)button(171,387,90,28,"Previous",false,()->{page--;render();});
        if(page+1<pages)button(391,387,90,28,"Next",false,()->{page++;render();});
        if(selectedTeleport!=null){text(500,52,229,60,2100,titleLines(selectedTeleport[0]));button(495,250,239,35,"Teleport",false,()->quick(selectedTeleport[2]));}
        button(495,344,239,35,"Open World Map",false,()->{
            close();if(!player.getInterfaceManager().openNative950DeveloperWorldMap(this::open)){status="World map is unavailable for this session.";open();}
        });
        text(500,391,229,30,2100,"Close the map to return here.");
    }
    static String rowName(String value,int limit){return value.length()<=limit?value:value.substring(0,limit-3)+"...";}
    /** Inspector headings use a compact native font and top alignment, at most two lines. */
    static String titleLines(String value){
        StringBuilder out=new StringBuilder();int column=0,lines=1;
        for(String token:safe(value).split(" ")){
            String word=rowName(token,27);
            if(column>0&&column+word.length()+1>27){if(++lines>2){out.append("...");break;}out.append("<br>");column=0;}
            if(column>0){out.append(' ');column++;}out.append(word);column+=word.length();
        }return out.toString();
    }
    static Native950DeveloperCatalogue.Entry selectionOrFirst(List<Native950DeveloperCatalogue.Entry> results,int start,int count,Native950DeveloperCatalogue.Entry selected){
        if(selected!=null)for(int n=start;n<start+count;n++)if(results.get(n).id==selected.id)return results.get(n);
        return count==0?null:results.get(start);
    }
    private static String shortDescription(String value){return value.length()>43?value.substring(0,40)+"...":value;}
    private void browse(String kind){variantFamily="";report=Collections.emptyList();advanced=false;npcAdvanced=false;bossOnly=false;browser=kind;domain=kind.equals("NPC")?"NPCs":"Objects";category=domain;query="";page=0;entity=null;selected=null;confirm=false;status="Search by name, exact ID or audited symbol. Select a result to place it.";render();}
    private void renderBrowser(){
        if(browser.equals("NPC")&&report.isEmpty()&&!npcAdvanced){renderNpcBrowser();return;}
        List<Native950DeveloperCatalogue.Entry> results=bossOnly?Native950BossCatalogue.search(query):Native950DeveloperCatalogue.search(browser,query);
        int window=browser.equals("Object")?BROWSER_WINDOW:PAGE_SIZE;
        int pages=Math.max(1,(results.size()+window-1)/window);page=Math.max(0,Math.min(page,pages-1));
        int count=Math.min(window,results.size()-page*window);if(browser.equals("Object"))viewport(count);
        for(int n=0;n<window;n++){
            int at=page*window+n;if(at>=results.size())break;Native950DeveloperCatalogue.Entry e=results.get(at);
            String label=e.name.length()>31?e.name.substring(0,28)+"...":e.name;
            Runnable select=()->{entity=e;confirm=false;report=Collections.emptyList();advanced=false;savePlacement=false;amount=1;rotation=0;typeIndex=0;for(int i=0;i<e.types.length;i++)if(e.types[i]==10)typeIndex=i;render();};
            if(browser.equals("Object")){
                int actor=2000+n;collectionRows.put(actor,select);
                write(Native950Packets.runClientScript(21150,n,"<col=ffd479>"+safe(label)+"</col><br>"+e.width+" x "+e.height+" | World object","__devop:"+epoch+":"+actor));if(entity!=null&&entity.id==e.id)write(Native950Packets.runClientScript(21154,n,2));continue;
            }
            button(171,77+n*50,310,47,"",entity==e,select);
            text(181,80+n*50,290,19,2100,"<col=ffd479>"+safe(label)+"</col>");
            text(181,100+n*50,290,19,2100,"ID "+e.id+" | "+e.width+" x "+e.height+(e.kind.equals("NPC")?" | Level "+Math.max(0,e.level):" | Placeable variant"));
        }
        if(browser.equals("Object")){write(Native950Packets.runClientScript(21133));write(Native950Packets.interfaceEvents(SHELL,9,0,Math.max(0,count*2-1),2));}
        button(171,387,68,28,"Previous",false,()->{if(page>0){page--;render();}});
        text(245,389,161,24,2100,(page+1)+" / "+pages+" | "+results.size()+" results");
        button(412,387,69,28,"Next",false,()->{if(page+1<pages){page++;render();}});
        if(!report.isEmpty()){renderReport();return;}
        if(entity==null){text(500,48,229,35,17514,browser+" browser");text(500,93,227,200,2100,"Actual revision-950 names and footprints.<br><br>Search by name or exact numeric ID.<br><br>Only concrete definitions with world models are listed.<br><br>Placement is validated again by the existing diagnostic handler.");}
        else{
            text(500,43,229,60,2100,titleLines(entity.name));text(500,103,227,95,2100,(entity.kind.equals("Object")&&!advanced?"Footprint: "+entity.width+" x "+entity.height+"<br>Rotation: "+(rotation*90)+" degrees<br>950 world-model definition":entity.details())+(bossOnly?"<br>"+Native950BossCatalogue.find(entity.id).status:""));
            if(browser.equals("NPC")){
                button(495,219,239,30,"Amount: "+amount,false,()->prompt("NPC amount (1-50):",v->{try{int n=Integer.parseInt(v);if(n<1||n>50)throw new NumberFormatException();amount=n;}catch(NumberFormatException e){throw new IllegalArgumentException("Amount must be 1-50.");}}));
                button(495,252,239,30,"Respawn: "+(repeat?"ON":"OFF"),false,()->{repeat=!repeat;render();});
            }else{
                button(495,219,116,30,"Rotate left",false,()->{rotation=(rotation+3)%4;render();});
                button(616,219,118,30,"Rotate right",false,()->{rotation=(rotation+1)%4;render();});
                button(495,252,239,30,"Save to World: "+(savePlacement?"ON":"OFF"),false,()->{savePlacement=!savePlacement;render();});
            }
            if(browser.equals("NPC")){
                button(495,285,239,29,"Inspect combat / provenance",false,()->showReport(bossOnly?Native950BossCatalogue.details(entity.id):Native950CombatInspector.npc(entity.id)));
                button(495,317,116,29,"Spawn near me",false,this::spawnNear);
                Native950BossCatalogue.Boss boss=Native950BossCatalogue.find(entity.id);
                if(boss!=null)button(616,317,118,29,"Encounter travel",false,()->showReport(Native950DeveloperOutput.run(player,channel,()->execute.accept(";;bossgo "+entity.id))));
            }else{
                button(495,285,239,29,advanced?"Hide technical details":"Advanced / Technical details",false,()->{advanced=!advanced;render();});
                if(advanced)button(495,317,239,29,"Valid model type: "+entity.types[typeIndex],false,()->{typeIndex=(typeIndex+1)%entity.types.length;render();});
            }
            button(495,387,239,28,"Place in world",false,this::beginPlacement);
        }
        if(entity==null&&browser.equals("NPC"))button(495,317,239,29,bossOnly?"All NPCs":"Boss encounters",false,()->{bossOnly=!bossOnly;page=0;render();});
        if(entity!=null&&browser.equals("NPC")&&Native950BossCatalogue.find(entity.id)!=null)
            button(495,358,239,27,confirm?"Confirm test encounter":"Create test encounter",false,()->{
                if(!confirm){confirm=true;status="Creates your temporary encounter. Click Confirm test encounter.";render();return;}
                confirm=false;showReport(Native950DeveloperOutput.run(player,channel,()->execute.accept(";;bossfight "+entity.id)));
            });
        else button(495,358,239,27,"Back to actions",false,()->{browser="";query="";page=0;render();});
    }
    /** Bounded rows scroll locally; selecting one never tears down the viewport. */
    private void renderNpcBrowser(){
        List<Native950DeveloperCatalogue.Entry> results=!variantFamily.isEmpty()?new ArrayList<>(Native950DeveloperCatalogue.search("NPC",variantFamily)):
            bossOnly?Native950BossCatalogue.search(query):section.equals("All variants")?Native950DeveloperCatalogue.search("NPC",query):Native950DeveloperCatalogue.groupedNpcs(query);
        if(!variantFamily.isEmpty())results.removeIf(e->!e.name.equalsIgnoreCase(variantFamily));
        if(domain.equals("Bosses")&&!section.isEmpty()&&!section.equals("All Bosses")){
            results=new ArrayList<>(results);results.removeIf(e->{Native950BossCatalogue.Boss b=Native950BossCatalogue.find(e.id);return b==null||!b.status.toLowerCase(Locale.ROOT).contains(section.toLowerCase(Locale.ROOT));});
        }
        int pages=Math.max(1,(results.size()+BROWSER_WINDOW-1)/BROWSER_WINDOW);page=Math.max(0,Math.min(page,pages-1));
        int start=page*BROWSER_WINDOW,count=Math.min(BROWSER_WINDOW,results.size()-start);
        entity=selectionOrFirst(results,start,count,entity);
        viewport(count);
        for(int n=0;n<count;n++){
            Native950DeveloperCatalogue.Entry e=results.get(start+n);int actor=1000+n;
            // Different container/nonce namespace: never leave holes in host5's
            // contiguous CC_CREATE indices for the inspector buttons that follow.
            rowEntities.put(actor,e);
            write(Native950Packets.runClientScript(21132,n,count,"<col=ffd479>"+safe(rowName(e.name,34))+"</col><br>Level "+Math.max(0,e.level)+" | "+e.width+" x "+e.height,
                    "__devop:"+epoch+":"+actor));
            if(entity!=null&&entity.id==e.id)write(Native950Packets.runClientScript(21154,n,2));
        }
        write(Native950Packets.runClientScript(21133));
        write(Native950Packets.interfaceEvents(SHELL,9,0,Math.max(0,count*2-1),30));
        button(171,387,76,28,"Earlier",false,()->{if(page>0){page--;entity=null;render();}});
        text(251,389,145,24,2100,count==0?"No matches":(start+1)+"-"+(start+count)+" of "+results.size());
        button(402,387,79,28,"More",false,()->{if(page+1<pages){page++;entity=null;render();}});
        previewTitle=textChild;text(500,43,229,45,2100,"No matching NPCs");
        String[] zoomLabels={"Zoom -","Reset","Zoom +"};
        for(int n=0;n<3;n++){zoomActors[n]=buttons.size();button(495+n*80,247,77,25,zoomLabels[n],false,()->{});}
        previewInfo=textChild;text(500,277,227,16,2100,"");
        idleActor=buttons.size();button(495,294,116,27,"Idle",false,()->previewSequence(false));
        attackActor=buttons.size();button(616,294,118,27,"Attack",false,()->previewSequence(true));
        button(495,325,116,27,"Spawn near me",false,()->{if(entity!=null)spawnNear();});
        button(616,325,118,27,"Place in world",false,()->{if(entity!=null)beginPlacement();});
        button(495,356,116,27,"Amount: "+amount,false,()->prompt("NPC amount (1-50):",v->{int n=Integer.parseInt(v);if(n<1||n>50)throw new IllegalArgumentException("Amount must be 1-50.");amount=n;}));
        button(616,356,118,27,"More controls",false,()->{npcAdvanced=true;page=0;render();});
        button(495,387,239,28,!variantFamily.isEmpty()?"Back to grouped results":"Show selected NPC variants",false,()->{
            if(!variantFamily.isEmpty())variantFamily="";else if(entity!=null)variantFamily=entity.name;page=0;render();
        });
        previewChild=textChild++;
        // Reserve a contiguous child even for empty searches; later updates reuse it.
        write(Native950Packets.runClientScript(21135,previewChild,-1,-1,1000,0,93));
        write(Native950Packets.runClientScript(21136,previewChild));
        write(Native950Packets.runClientScript(21143,idleActor,previewChild,-1));
        write(Native950Packets.runClientScript(21143,attackActor,previewChild,-1));
        if(entity!=null)updatePreview();
    }
    private void selectEntity(Native950DeveloperCatalogue.Entry e){
        entity=e;confirm=false;report=Collections.emptyList();amount=1;repeat=false;updatePreview();channel.flush();
    }
    private void updatePreview(){
        try{preview=Native950DeveloperPreview.resolve(entity.id);}catch(RuntimeException invalid){preview=Native950DeveloperPreview.unavailable();}

        write(Native950Packets.runClientScript(21142,previewTitle,titleLines(entity.name)));
        write(Native950Packets.runClientScript(21142,previewInfo,(preview.npc<0?"Preview unavailable":"Level "+Math.max(0,entity.level)+" | "+entity.width+" x "+entity.height)));
        write(Native950Packets.runClientScript(21135,previewChild,preview.npc,preview.idle,preview.zoom,preview.height,preview.nativeFraming?111:176));
        write(Native950Packets.runClientScript(21143,idleActor,previewChild,preview.idle));
        write(Native950Packets.runClientScript(21143,attackActor,previewChild,preview.attack));
        int[] zoomChanges={150,0,-150};for(int n=0;n<3;n++)write(Native950Packets.runClientScript(21148,zoomActors[n],previewChild,zoomChanges[n],preview.zoom));
        Native950BugTest.event(player,"developer-console","preview","npc",entity.id,"idle",preview.idle,"attack",preview.attack,"zoom",preview.zoom,"nativeFraming",preview.nativeFraming);
    }
    private void previewSequence(boolean attack){
        if(preview==null||entity==null)return;int seq=attack?preview.attack:preview.idle;
        if(seq<0){write(Native950Packets.runClientScript(21142,previewInfo,"This animation is not verified for this NPC."));}
        else write(Native950Packets.runClientScript(21141,previewChild,seq));
        channel.flush();
    }
    private void browseGamevals(){domain="Tools";report=Collections.emptyList();browser="Gameval";category="Tools";query="";page=0;symbol=null;selected=null;confirm=false;status="Read-only symbols. 949 names, individually audited against 950. No world actions.";render();}
    private void renderGamevals(){
        List<Native950GamevalLookup.Entry> results=Native950GamevalLookup.search(query);
        int pages=Math.max(1,(results.size()+PAGE_SIZE-1)/PAGE_SIZE);page=Math.max(0,Math.min(page,pages-1));
        for(int n=0;n<PAGE_SIZE;n++){
            int at=page*PAGE_SIZE+n;if(at>=results.size())break;Native950GamevalLookup.Entry e=results.get(at);int y=77+n*50;
            button(171,y,310,47,"",symbol==e,()->{symbol=e;render();});
            text(181,y+3,290,19,2100,"<col=ffd479>"+e.type+" "+e.id+"</col>");
            text(181,y+23,290,19,2100,shortDescription(e.name));
        }
        if(results.isEmpty())text(181,90,290,100,2100,"No audited symbol matches.<br>This is a verified subset, not the complete cache. Use the NPC/object browsers for display-name searches.");
        button(171,387,68,28,"Previous",false,()->{if(page>0){page--;render();}});
        text(245,389,161,24,2100,(page+1)+" / "+pages+" | "+results.size()+" symbols");
        button(412,387,69,28,"Next",false,()->{if(page+1<pages){page++;render();}});
        text(500,43,229,35,17514,"Cache / Gamevals");
        if(symbol==null)text(500,88,227,250,2100,"Search symbolic name, numeric ID, or interface:component.<br><br>Optional type words: component, interface, varp, varbit.<br><br>Original names: OpenRS2 2670 (949).<br>Audited target: 2691 (950.1).<br><br>Missing/changed identities are excluded. No execution or placement from this browser.");
        else{
            // Break long underscore-separated names for native text wrapping, preserving the full name in chat.
            text(500,87,227,116,2100,symbol.name.replace("_","_ ").replace(":",": "));
            text(500,207,227,145,2100,symbol.type+" "+symbol.id+"<br>Packed / numeric ID: "+symbol.packed()+"<br>"+symbol.verify()+"<br>"+symbol.evidence+"<br>"+(symbol.evidence.startsWith("Project")?"Project-authored 950 alias":"Named source 2670 (949); checked target 2691 (950.1)"));
            button(495,354,239,29,"Raw diagnostic: print to chat",false,()->{message(symbol.type+" "+symbol.id+" = "+symbol.name);message(symbol.verify()+"; "+symbol.evidence);});
        }
        button(495,387,239,28,"Back to tools",false,()->navigate("Tools"));
    }
    private void beginPlacement(){
        if(entity==null||!Native950DeveloperActions.permitted(player,channel))return;
        if(placementSerial>=4095)throw new IllegalArgumentException("Placement session limit reached. Relog before placing more.");
        Native950DeveloperWorldEdits.adopt(player);
        if(Native950DeveloperWorldEdits.owned(player.getUsername()).size()+(entity.kind.equals("NPC")?amount:1)>200)throw new IllegalArgumentException("Remove placements before adding more (200 per account).");
        String refusal=Native950DiagnosticSpawns.refusal(player);if(refusal!=null)throw new IllegalArgumentException(refusal);
        if(entity.kind.equals("Object")){
            com.rs.cache.loaders.ObjectDefinitions definition=com.rs.cache.loaders.ObjectDefinitions.getObjectDefinitions(entity.id);
            if(!definition.loaded||definition.transforms!=null||entity.types.length==0)throw new IllegalArgumentException("This object variant cannot be placed. Choose another named variant.");
            try{Native950DiagnosticSpawns.selectShape(definition.shapes,definition.models,entity.types[typeIndex]);}
            catch(IllegalArgumentException invalid){throw new IllegalArgumentException("This object variant cannot be placed. Choose one of the available variants.");}
        }
        placement=new Native950DeveloperPlacement.Request(entity,++placementSerial,entity.kind.equals("NPC")?amount:1,
                entity.types.length==0?-1:entity.types[typeIndex],rotation,repeat,player,System.currentTimeMillis());
        buttons.clear();
        write(Native950Packets.runClientScript(INIT+6,""));
        // Release the management keyboard lifecycle, but retain its hidden target
        // actor until the one-shot ground selection is committed or cancelled.
        write(Native950Packets.runClientScript(8179));write(Native950Packets.runClientScript(8180,1,1));
        write(Native950Packets.varcLarge(2911,-1));write(Native950Packets.runClientScript(8290,1));
        write(Native950Packets.hideInterface(1477,708,true));write(Native950Packets.interfaceEvents(1477,8,-1,-1,254));
        write(Native950Packets.interfaceEvents(SHELL,11,placement.slot,placement.slot,64<<11));
        write(Native950Packets.runClientScript(INIT+4,placement.slot,"PLACING: "+entity.name,"__devcancel:"+placement.slot));
        status="PLACING: "+entity.name+" ("+entity.width+"x"+entity.height+") - click a clear world tile; Escape/right-click cancels.";
        message(status);
        Native950BugTest.event(player,"developer-placement","armed","kind",entity.kind,"id",entity.id,"slot",placement.slot,"size",entity.width,"amount",amount);
    }
    boolean handle(Native950Actions.InterfaceOnTileAction a){
        if(a.sourceHash()!=Native950DeveloperPlacement.SOURCE)return false;
        Native950DeveloperPlacement.Request request=placement;
        if(request==null||!open||!Native950DeveloperActions.permitted(player,channel))return true;
        if(!player.clientHasLoadedMapRegion()||!request.accepts(a.sourceHash(),a.sourceSlot(),a.sourceItemId(),a.x(),a.y(),player,System.currentTimeMillis())){
            Native950BugTest.event(player,"developer-placement","rejected-input","id",request.entry.id,"expectedSlot",request.slot,"slot",a.sourceSlot(),"x",a.x(),"y",a.y(),"mapLoaded",player.clientHasLoadedMapRegion());
            // An old source slot must not cancel or consume the current claim.
            if(a.sourceSlot()==request.slot)message("Placement could not use that tile. Choose a loaded tile within 32 tiles, or Escape to cancel.");
            return true;
        }
        Native950DeveloperWorldEdits.Edit previous=moving;placement=null;close();
        try{
            String refusal=Native950DiagnosticSpawns.refusal(player);if(refusal!=null)throw new IllegalArgumentException(refusal);
            Native950DeveloperPlacement.Mutations world=Native950DeveloperPlacement.world(player);
            List<Object> placed=Native950DeveloperPlacement.commit(request,a.x(),a.y(),world);
            Native950BugTest.event(player,"developer-placement","created","kind",request.entry.kind,"id",request.entry.id,"x",a.x(),"y",a.y(),"count",placed.size());
            if(previous!=null){
                try{Native950DeveloperWorldEdits.delete(player,previous);}catch(java.io.IOException|IllegalArgumentException failure){for(Object actor:placed)world.remove(actor);throw new IllegalArgumentException("Move cancelled: "+failure.getMessage());}
            }
            List<Native950DeveloperWorldEdits.Edit> edits=Native950DeveloperWorldEdits.record(player,request,placed);
            if(previous==null)history.created(edits);else history.clear();
            status="Placed "+request.amount+" "+request.entry.name+". Owned by you.";
            if(savePlacement)for(Native950DeveloperWorldEdits.Edit edit:edits)try{Native950DeveloperWorldEdits.save(player,edit);status="Placed and saved to world.";}catch(java.io.IOException failure){status="Placed temporarily; save failed: "+failure.getMessage();}
        }catch(IllegalArgumentException|IllegalStateException e){
            status=e.getMessage();
            Native950BugTest.event(player,"developer-placement","failed","kind",request.entry.kind,"id",request.entry.id,"x",a.x(),"y",a.y(),"reason",status);
        }
        open();return true;
    }
    private void renderEdits(){
        Native950DeveloperWorldEdits.adopt(player);
        List<Native950DeveloperWorldEdits.Edit> edits=Native950DeveloperWorldEdits.owned(player.getUsername());
        if(section.equals("My NPCs"))edits.removeIf(e->!e.kind.equals("NPC"));
        if(section.equals("My Objects"))edits.removeIf(e->!e.kind.equals("Object"));
        if(section.equals("Temporary"))edits.removeIf(e->e.saved);
        if(section.equals("Saved"))edits.removeIf(e->!e.saved);
        if(!query.isEmpty())edits.removeIf(e->!(editName(e)+" "+e.kind+" "+e.id+" "+e.x+" "+e.y).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)));
        int pages=Math.max(1,(edits.size()+BROWSER_WINDOW-1)/BROWSER_WINDOW);page=Math.max(0,Math.min(page,pages-1));
        int count=Math.min(BROWSER_WINDOW,edits.size()-page*BROWSER_WINDOW);viewport(count);
        for(int n=0;n<count;n++){
            Native950DeveloperWorldEdits.Edit e=edits.get(page*BROWSER_WINDOW+n);int actor=2000+n;
            collectionRows.put(actor,()->{editSelection=e;confirm=false;render();});
            write(Native950Packets.runClientScript(21150,n,"<col=ffd479>"+safe(editName(e))+"</col><br>"+e.kind+(!Native950DeveloperWorldEdits.encounter(e).isEmpty()?" | Encounter":e.saved?" | Saved":e.repeat?" | Repeat":" | Temporary"),"__devop:"+epoch+":"+actor));
        }
        write(Native950Packets.runClientScript(21133));write(Native950Packets.interfaceEvents(SHELL,9,0,Math.max(0,count*2-1),2));
        button(171,387,68,28,"Previous",false,()->{if(page>0){page--;render();}});
        text(245,389,161,24,2100,(page+1)+" / "+pages+" | "+edits.size()+" owned");
        button(412,387,69,28,"Next",false,()->{if(page+1<pages){page++;render();}});
        if(!report.isEmpty()){renderReport();return;}
        if(editSelection!=null&&edits.contains(editSelection)){
            Native950DeveloperWorldEdits.Edit e=editSelection;
            text(500,43,229,48,17514,safe(editName(e)));
            text(500,98,227,101,2100,"Location: "+e.x+", "+e.y+", "+e.plane+"<br>"+(e.saved?"SAVED TO WORLD":"TEMPORARY")+"<br>"+(Native950DeveloperWorldEdits.alive(e)?"Present":"No longer present")+"<br>"+safe(Native950DeveloperWorldEdits.encounter(e))+"<br>Rotation: "+(e.rotation*90)+" degrees");
            button(495,201,116,29,"Move",e.saved,()->editPlacement(e,true));
            button(616,201,118,29,"Duplicate",false,()->editPlacement(e,false));
            button(495,233,116,29,"Rotate",e.saved||!e.kind.equals("Object"),()->{editSelection=Native950DeveloperWorldEdits.rotate(player,e);history.clear();render();});
            button(616,233,118,29,confirm?"Confirm delete":"Delete",false,()->{
                if(e.saved&&!confirm){confirm=true;status="Select Confirm delete to remove this owned placement.";render();return;}
                try{Native950DeveloperWorldEdits.delete(player,e);editSelection=null;confirm=false;history.clear();status="Placement removed.";}catch(java.io.IOException failure){status=failure.getMessage();}render();
            });
            button(495,266,239,29,"Save to World",e.saved,()->{
                try{Native950DeveloperWorldEdits.save(player,e);history.clear();status="Saved. This placement will restore after server startup.";}catch(java.io.IOException failure){status=failure.getMessage();}render();
            });
            button(495,300,116,29,"Inspect definition",false,()->{entity=Native950DeveloperCatalogue.search(e.kind,""+e.id).stream().findFirst().orElse(null);browser=e.kind;bossOnly=false;query="";page=0;render();});
            button(616,300,118,29,"Teleport to",false,()->{status=Native950DeveloperWorldEdits.teleport(player,e);render();});
        }else text(500,48,229,220,2100,"YOUR WORLD PLACEMENTS<br><br>Place NPCs or objects from their browser. Select one here to inspect, move, duplicate, rotate, delete or explicitly Save to World.<br><br>Temporary edits are removed on logout.");
        button(495,354,116,29,"Undo place",!history.canUndo(),()->{history.undo(player);editSelection=null;render();});
        button(616,354,118,29,"Redo place",!history.canRedo(),()->{history.redo(player);render();});
        button(495,387,77,28,"Clear NPCs",false,()->clearOwned("NPC",false));
        button(576,387,77,28,"Objects",false,()->clearOwned("Object",false));
        button(657,387,77,28,"Temporary",false,()->clearOwned("",true));
    }
    private static String editName(Native950DeveloperWorldEdits.Edit e){List<Native950DeveloperCatalogue.Entry> rows=Native950DeveloperCatalogue.search(e.kind,""+e.id);return rows.isEmpty()?e.kind+" "+e.id:rows.get(0).name;}
    private void clearOwned(String kind,boolean temporary){
        String key=kind+":"+temporary;
        if(!key.equals(clearConfirmation)||clearConfirmationEpoch!=epoch){clearConfirmation=key;status="Clear your "+(temporary?"temporary placements":kind+" placements")+"? Click the same control again.";render();clearConfirmationEpoch=epoch;return;}
        clearConfirmation="";try{int n=Native950DeveloperWorldEdits.clearOwned(player,kind,temporary);status="Removed "+n+" of your placements.";editSelection=null;history.clear();}
        catch(java.io.IOException e){status=e.getMessage();}render();
    }
    private void spawnNear(){
        if(entity==null||!entity.kind.equals("NPC"))return;
        String refusal=Native950DiagnosticSpawns.refusal(player);if(refusal!=null)throw new IllegalArgumentException(refusal);
        Native950DeveloperWorldEdits.adopt(player);if(Native950DeveloperWorldEdits.owned(player.getUsername()).size()+amount>200)throw new IllegalArgumentException("Remove placements first (200 per account).");
        Native950DeveloperPlacement.Request request=new Native950DeveloperPlacement.Request(entity,1,amount,-1,0,repeat,player,System.currentTimeMillis());
        Native950DeveloperPlacement.Mutations world=Native950DeveloperPlacement.world(player);
        for(int radius=2;radius<=12;radius++)for(int[] offset:new int[][]{{radius,0},{0,radius},{-radius,0},{0,-radius}}){
            int x=player.getX()+offset[0],y=player.getY()+offset[1];boolean free=true;
            for(com.rs.game.WorldTile tile:Native950DeveloperPlacement.formation(x,y,player.getPlane(),entity.width,amount))if(!world.available(tile,entity.width)){free=false;break;}
            if(!free)continue;
            List<Object> placed=Native950DeveloperPlacement.commit(request,x,y,world);history.created(Native950DeveloperWorldEdits.record(player,request,placed));status="Spawned "+amount+" "+entity.name+". Manage them in Spawns.";render();return;
        }
        throw new IllegalArgumentException("No clear nearby formation. Use Place and choose an open area.");
    }
    private void editPlacement(Native950DeveloperWorldEdits.Edit edit,boolean move){
        if(move&&edit.saved)throw new IllegalArgumentException("Duplicate saved placements before moving them.");
        List<Native950DeveloperCatalogue.Entry> matches=Native950DeveloperCatalogue.search(edit.kind,""+edit.id);
        if(matches.isEmpty())throw new IllegalArgumentException("That cache definition is no longer available.");
        entity=matches.get(0);savePlacement=false;amount=1;repeat=edit.repeat;rotation=edit.rotation;typeIndex=0;
        for(int n=0;n<entity.types.length;n++)if(entity.types[n]==edit.type)typeIndex=n;
        moving=move?edit:null;try{beginPlacement();}catch(RuntimeException failure){moving=null;throw failure;}
    }
    private boolean mayAct(){return Native950DeveloperActions.permitted(player,channel)&&player.isActive()&&!player.isDead()&&!player.hasFinished()&&!player.isLocked()&&!player.closeInterfaceLocked;}
    private void button(int x,int y,int w,int h,String label,boolean selected,Runnable action){
        int actor=buttons.size();buttons.put(actor,action);write(Native950Packets.runClientScript(BUTTON,actor,x,y,w,h,selected?1:0,rowActions.containsKey(actor)?1:0,(selected?"<col=ffd479>"+label+"</col>":label),"__devop:"+epoch+":"+actor));
    }
    private void text(int x,int y,int w,int h,int style,String text){write(Native950Packets.runClientScript(TEXT,textChild++,x,y,w,h,style,text));}
    private void write(Native950Packets.Packet p){channel.write(p);}
    private void message(String text){write(Native950Packets.gameMessage(0,text));}
    static String safe(String text){return text==null?"":text.replace("<","(").replace(">",")");}
    private static void verify(){
        Native950Navigation.verify();
        Properties pins=new Properties();
        try(java.io.InputStream in=Native950DeveloperConsole.class.getResourceAsStream("/native950/developer-console-950.properties")){
            if(in==null)throw new IllegalStateException("Missing Developer Console contracts");pins.load(in);
            for(String id:pins.stringPropertyNames()){
                byte[] raw=Cache.STORE.getIndexes()[12].getFile(Integer.parseInt(id),0);
                if(raw==null)throw new IllegalStateException("Install the staged Developer Console cache before opening ;;dev.");
                StringBuilder sha=new StringBuilder();for(byte b:java.security.MessageDigest.getInstance("SHA-256").digest(raw))sha.append(String.format("%02x",b&255));
                if(!pins.getProperty(id).equals(sha.toString()))throw new IllegalStateException("Changed Developer Console script "+id);
            }
        }catch(java.io.IOException|java.security.NoSuchAlgorithmException e){throw new IllegalStateException("Cannot verify Developer Console",e);}
    }
}
