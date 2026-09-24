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
    private static final int PAGE_SIZE=6;
    private static final Map<Player,Native950DeveloperConsole> OWNERS=new IdentityHashMap<>();
    private final Player player;private final Channel channel;private final Runnable prepare,verify;
    private final Consumer<String> execute;private final Map<Integer,Runnable> buttons=new HashMap<>();
    private Set<String> favourites=new LinkedHashSet<>();private boolean open,confirm,awaitingNative;
    private String category="Favourites",query="",status="Select an action to inspect and configure it.";
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
    private boolean bossOnly;
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
        write(Native950Packets.hideInterface(SHELL,1,true));write(Native950Packets.hideInterface(1477,708,false));
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
                if(fields.length==4){
                    Native950DeveloperActions.Action row=rowActions.get(Integer.parseInt(fields[2]));if(row==null)return true;
                    int op=Integer.parseInt(fields[3]);
                    if(op==2){selected=row;values=row.defaults();confirm=false;report=Collections.emptyList();if(row.parameters.isEmpty())runSelected();else select(row);}
                    else if(op==3)favourite(row);else if(op==4)select(row);return true;
                }
                Runnable operation=buttons.get(Integer.parseInt(fields[2]));
                if(operation!=null){Native950BugTest.event(player,"developer-console","operation","epoch",epoch,"actor",fields[2]);processingNotification=notification;try{operation.run();}finally{processingNotification="";}}
            }catch(IllegalArgumentException|IllegalStateException invalid){status=invalid.getMessage();report=Collections.singletonList(status);reportPage=0;if(open)render();}
            return true;
        }
        if(!open||input==null)return false;Consumer<String> callback=input;closeInput();
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
        if(!open||awaitingNative)return;renderStarted=System.nanoTime();lastState=stateKey();buttons.clear();rowActions.clear();textChild=0;epoch++;
        write(Native950Packets.runClientScript(INIT));
        String[] tabs={"Commands","Spawns","Items","NPCs","Player","World","Combat","Quests","Tools","Settings"};
        for(int i=0;i<tabs.length;i++){
            final String c=tabs[i];
            button(i*74,0,72,30,c,category.equals(c),()->{
                if(c.equals("NPCs")){browse("NPC");return;}
                if(c.equals("World")){browse("Object");return;}
                if(c.equals("Items")){execute.accept(";;items");return;}
                navigate(c);
            });
        }
        button(0,38,409,30,query.isEmpty()?"Search "+(browser.isEmpty()?"commands...":browser+"s..."):"Search: "+safe(query),false,()->prompt(browser.isEmpty()?"Search developer actions / descriptions / aliases:":"Search "+browser+" name or exact ID:",q->{query=q;page=0;confirm=false;}));
        button(413,38,68,30,"Clear",false,()->{query="";page=0;render();});
        int y=77;
        for(String c:Native950DeveloperActions.CATEGORIES){button(0,y,160,27,c.equals("Commands")?"All commands":c,category.equals(c)&&query.isEmpty(),()->navigate(c));y+=28;}
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
            text(500,42,229,35,17514,label(selected)+" "+Native950DeveloperActions.state(selected,player));
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
        text(0,421,734,25,2100,"<col=86efac>Developer</col>  |  "+safe(player.getDisplayName())+"  |  "+safe(status));
        write(Native950Packets.interfaceEvents(SHELL,ACTORS,0,Math.max(0,buttons.size()-1),30));
        Native950BugTest.event(player,"developer-console","render","micros",(System.nanoTime()-renderStarted)/1000,"actors",buttons.size(),"text",textChild);
        channel.flush();
    }
    private void navigate(String c){
        if(c.equals("NPCs")){browse("NPC");return;}if(c.equals("World")){browse("Object");return;}
        report=Collections.emptyList();clearConfirmation="";category=c;query="";page=0;confirm=false;browser=c.equals("Spawns")?"Edits":"";entity=null;selected=null;render();}
    private static String shortDescription(String value){return value.length()>43?value.substring(0,40)+"...":value;}
    private void browse(String kind){report=Collections.emptyList();advanced=false;bossOnly=false;browser=kind;category=kind.equals("NPC")?"NPCs":"World";query="";page=0;entity=null;selected=null;confirm=false;status="Search by name, exact ID or audited symbol. Select a result to place it.";render();}
    private void renderBrowser(){
        List<Native950DeveloperCatalogue.Entry> results=bossOnly?Native950BossCatalogue.search(query):Native950DeveloperCatalogue.search(browser,query);
        int pages=Math.max(1,(results.size()+PAGE_SIZE-1)/PAGE_SIZE);page=Math.max(0,Math.min(page,pages-1));
        for(int n=0;n<PAGE_SIZE;n++){
            int at=page*PAGE_SIZE+n;if(at>=results.size())break;Native950DeveloperCatalogue.Entry e=results.get(at);
            String label=e.name.length()>31?e.name.substring(0,28)+"...":e.name;
            button(171,77+n*50,310,47,"",entity==e,()->{entity=e;confirm=false;report=Collections.emptyList();advanced=false;savePlacement=false;amount=1;rotation=0;typeIndex=0;for(int i=0;i<e.types.length;i++)if(e.types[i]==10)typeIndex=i;render();});
            text(181,80+n*50,290,19,2100,"<col=ffd479>"+safe(label)+"</col>");
            text(181,100+n*50,290,19,2100,"ID "+e.id+" | "+e.width+" x "+e.height+(e.kind.equals("NPC")?" | Level "+Math.max(0,e.level):" | Placeable variant"));
        }
        button(171,387,68,28,"Previous",false,()->{if(page>0){page--;render();}});
        text(245,389,161,24,2100,(page+1)+" / "+pages+" | "+results.size()+" results");
        button(412,387,69,28,"Next",false,()->{if(page+1<pages){page++;render();}});
        if(!report.isEmpty()){renderReport();return;}
        if(entity==null){text(500,48,229,35,17514,browser+" browser");text(500,93,227,200,2100,"Actual revision-950 names and footprints.<br><br>Search by name or exact numeric ID.<br><br>Only concrete definitions with world models are listed.<br><br>Placement is validated again by the existing diagnostic handler.");}
        else{
            text(500,43,229,54,17514,safe(entity.name));text(500,103,227,95,2100,(entity.kind.equals("Object")&&!advanced?"Footprint: "+entity.width+" x "+entity.height+"<br>Rotation: "+(rotation*90)+" degrees<br>950 world-model definition":entity.details())+(bossOnly?"<br>"+Native950BossCatalogue.find(entity.id).status:""));
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
    private void browseGamevals(){report=Collections.emptyList();browser="Gameval";category="Tools";query="";page=0;symbol=null;selected=null;confirm=false;status="Read-only symbols. 949 names, individually audited against 950. No world actions.";render();}
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
        write(Native950Packets.runClientScript(INIT+4,placement.slot,"Place "+entity.name,"__devcancel:"+placement.slot));
        status="Choose a clear nearby tile; Escape cancels.";
    }
    boolean handle(Native950Actions.InterfaceOnTileAction a){
        if(a.sourceHash()!=Native950DeveloperPlacement.SOURCE)return false;
        Native950DeveloperPlacement.Request request=placement;
        if(request==null||!open||!Native950DeveloperActions.permitted(player,channel))return true;
        if(!player.clientHasLoadedMapRegion()||!request.accepts(a.sourceHash(),a.sourceSlot(),a.sourceItemId(),a.x(),a.y(),player,System.currentTimeMillis()))return true;
        Native950DeveloperWorldEdits.Edit previous=moving;placement=null;close();
        try{
            String refusal=Native950DiagnosticSpawns.refusal(player);if(refusal!=null)throw new IllegalArgumentException(refusal);
            Native950DeveloperPlacement.Mutations world=Native950DeveloperPlacement.world(player);
            List<Object> placed=Native950DeveloperPlacement.commit(request,a.x(),a.y(),world);
            if(previous!=null){
                try{Native950DeveloperWorldEdits.delete(player,previous);}catch(java.io.IOException|IllegalArgumentException failure){for(Object actor:placed)world.remove(actor);throw new IllegalArgumentException("Move cancelled: "+failure.getMessage());}
            }
            List<Native950DeveloperWorldEdits.Edit> edits=Native950DeveloperWorldEdits.record(player,request,placed);
            if(previous==null)history.created(edits);else history.clear();
            status="Placed "+request.amount+" "+request.entry.name+". Owned by you.";
            if(savePlacement)for(Native950DeveloperWorldEdits.Edit edit:edits)try{Native950DeveloperWorldEdits.save(player,edit);status="Placed and saved to world.";}catch(java.io.IOException failure){status="Placed temporarily; save failed: "+failure.getMessage();}
        }catch(IllegalArgumentException|IllegalStateException e){status=e.getMessage();}
        open();return true;
    }
    private void renderEdits(){
        Native950DeveloperWorldEdits.adopt(player);
        List<Native950DeveloperWorldEdits.Edit> edits=Native950DeveloperWorldEdits.owned(player.getUsername());
        if(!query.isEmpty())edits.removeIf(e->!(editName(e)+" "+e.kind+" "+e.id+" "+e.x+" "+e.y).toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)));
        int pages=Math.max(1,(edits.size()+PAGE_SIZE-1)/PAGE_SIZE);page=Math.max(0,Math.min(page,pages-1));
        for(int n=0;n<PAGE_SIZE&&page*PAGE_SIZE+n<edits.size();n++){
            Native950DeveloperWorldEdits.Edit e=edits.get(page*PAGE_SIZE+n);
            button(171,77+n*50,310,47,safe(shortDescription(editName(e)))+(!Native950DeveloperWorldEdits.encounter(e).isEmpty()?" [Encounter]":e.saved?" [Saved]":e.repeat?" [Repeat]":" [Temporary]"),editSelection==e,()->{editSelection=e;confirm=false;render();});
        }
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
        int actor=buttons.size();buttons.put(actor,action);write(Native950Packets.runClientScript(BUTTON,actor,x,y,w,h,selected?1:0,rowActions.containsKey(actor)?1:0,label,"__devop:"+epoch+":"+actor));
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
