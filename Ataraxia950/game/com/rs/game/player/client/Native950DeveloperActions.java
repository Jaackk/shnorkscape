package com.rs.game.player.client;

import com.rs.game.player.Player;
import io.netty.channel.Channel;
import java.util.*;
import java.util.regex.*;

/** UI metadata over the command directory. Execution always returns to the existing dispatcher. */
final class Native950DeveloperActions {
    static final String[] CATEGORIES={"Favourites","Commands","Combat","Player","Items","NPCs","Spawns","World","Quests","Tools","Settings"};
    static final class Parameter {
        final String name,initial; final int min,max; final String[] choices; final boolean number;
        Parameter(String name,String initial,int min,int max){this.name=name;this.initial=initial;this.min=min;this.max=max;number=true;choices=new String[0];}
        Parameter(String name,String initial,String... choices){this.name=name;this.initial=initial;this.choices=choices;min=0;max=0;number=false;}
        String validate(String value){
            if(value==null)throw new IllegalArgumentException(name+" is required.");value=value.trim();
            if(number){try{int n=Integer.parseInt(value);if(n>=min&&n<=max)return Integer.toString(n);}catch(NumberFormatException ignored){}throw new IllegalArgumentException(name+" must be "+min+"-"+max+".");}
            if(choices.length>0){for(String c:choices)if(c.equalsIgnoreCase(value))return c;throw new IllegalArgumentException("Choose a valid "+name+".");}
            if(value.length()<1||value.length()>80||!value.matches("[A-Za-z0-9 _'.,:-]+"))throw new IllegalArgumentException("Enter a valid "+name+" (1-80 characters).");
            return value;
        }
    }
    static final class Action {
        final String id,aliases,usage,description,category;final List<Parameter> parameters;final boolean configured,confirmation;
        Action(String id,String aliases,String usage,String description,String category,List<Parameter> parameters,boolean configured){
            this.id=id;this.aliases=aliases;this.usage=usage;this.description=description;this.category=category;
            this.parameters=Collections.unmodifiableList(parameters);this.configured=configured;
            confirmation=Arrays.asList("copy","clearbar","max","comp","clearnpcs","removenpc","clearobjects","bossfight","bossclear","resetcooldowns").contains(id);
        }
        String command(List<String> values){
            if(!configured||values.size()!=parameters.size())throw new IllegalArgumentException("This diagnostic requires its documented chat command.");
            StringBuilder result=new StringBuilder(";;").append(id);
            for(int i=0;i<parameters.size();i++)result.append(' ').append(parameters.get(i).validate(values.get(i)));
            return result.toString();
        }
        List<String> defaults(){List<String> v=new ArrayList<>();for(Parameter p:parameters)v.add(p.initial);return v;}
    }
    private static Parameter n(String name,int value,int min,int max){return new Parameter(name,""+value,min,max);}
    private static Parameter t(String name,String value,String... choices){return new Parameter(name,value,choices);}
    private static final List<Action> ALL=build();
    static List<Action> all(){return ALL;}
    static Action find(String id){for(Action a:ALL)if(a.id.equals(id))return a;return null;}
    static List<Action> search(String category,String query,Set<String> favourites){
        String q=query==null?"":query.trim().toLowerCase(Locale.ROOT);List<Action> result=new ArrayList<>();
        for(Action a:ALL){
            if(q.isEmpty()&&!category.equals("Commands")&&!category.equals(a.category)&&!(category.equals("Favourites")&&favourites.contains(a.id)))continue;
            if(!q.isEmpty()&&!(a.id+" "+a.aliases+" "+a.description+" "+a.category).toLowerCase(Locale.ROOT).contains(q))continue;
            result.add(a);
        }
        if(!q.isEmpty())result.sort(Comparator.comparing((Action a)->!Pattern.compile(";;"+Pattern.quote(q)+"(?![a-z0-9])").matcher(a.aliases).find()));
        return result;
    }
    static boolean permitted(Player p,Channel c){return Native950DevelopmentCommands.allowed(p,c)&&Native950AdminCommands.authorized(p);}
    static void execute(Player p,Channel c,Native950SkillGuide guide,Action a,List<String> values){
        if(!permitted(p,c))throw new IllegalArgumentException("Developer permission is required.");
        if(!p.isActive()||p.hasFinished()||p.isDead()||p.isLocked())throw new IllegalArgumentException("Wait until your character can act.");
        if(a==null||find(a.id)!=a)throw new IllegalArgumentException("Unknown developer action.");
        Native950DevelopmentCommands.handle(p,c,a.command(values),guide);
    }
    static String state(Action a,Player p){
        Boolean enabled=null;
        switch(a.id){
            case "combatqa":enabled=Native950CombatQa.enabled(p);break;
            case "bugtest":enabled=Native950BugTest.enabled(p);break;
            case "god":enabled=p.isDevelopmentGodMode();break;
            case "almighty":enabled=p.isDevelopmentAlmighty();break;
            case "infprayer":enabled=p.getPrayer().isInfinitePrayer();break;
            case "infadren":enabled=p.getCombatDefinitions().isInfiniteAdrenaline();break;
            case "infrunes":enabled=p.isInfiniteCombatRunes();break;
            case "infammo":enabled=p.isInfiniteAmmunition();break;
            case "infrun":enabled=p.isInfiniteRunEnergy();break;
            case "revo":enabled=p.getNative950ActionBar().isRevolutionEnabled();break;
        }
        return enabled==null?"":enabled?"<col=86efac>ON</col>":"<col=b0a89a>OFF</col>";
    }
    private static List<Action> build(){
        LinkedHashMap<String,Action> actions=new LinkedHashMap<>();
        Pattern names=Pattern.compile(";;([a-z0-9]+)");
        for(String[] row:Native950AdminCommands.developerDirectory())for(String segment:row[1].split(",\\s*(?=;;)")){
            Matcher m=names.matcher(segment);if(!m.find())continue;String id=m.group(1);
            if(actions.containsKey(id)||id.equals("dev"))continue;
            List<Parameter> params=new ArrayList<>();boolean configured=true;
            switch(id){
                case "bosses":params.add(t("Boss search","dagannoth"));break;
                case "bossinfo":case "bossgo":case "bossfight":params.add(n("Boss NPC ID",6260,0,200000));break;
                case "abilityinfo":params.add(n("Action bar slot",1,1,14));break;
                case "npcinfo":params.add(t("NPC ID, name or symbol","6260"));break;
                case "adrenaline":params.add(n("Adrenaline %",100,0,100));break;
                case "bar":params.add(n("Saved bar",1,1,4));break;
                case "dummy":params.add(n("Amount",1,1,5));break;
                case "item":params.add(n("Item ID",995,0,200000));params.add(n("Amount",1,1,Integer.MAX_VALUE));break;
                case "npc":case "npcrepeat":params.add(n("NPC ID",16027,0,200000));params.add(n("Amount",1,1,50));break;
                case "obj":params.add(n("Object ID",0,0,200000));params.add(n("Type",10,0,22));params.add(n("Rotation",0,0,3));break;
                case "tele":params.add(n("X",3217,0,16383));params.add(n("Y",3258,0,16383));params.add(n("Plane",0,0,3));break;
                case "copy":case "teleto":params.add(t("Player",""));break;
                case "copybar":params.add(t("Player",""));params.add(n("Saved bar",1,1,4));break;
                case "bug":params.add(t("Description","Developer Console check"));break;
                case "combatqa":params.add(t("Action","status","start","status","stop","reset","cleanup"));break;
                case "spell":params.add(t("Spell","surge","strike","bolt","blast","wave","surge"));break;
                case "gear":params.add(t("Style","melee","melee","mage","range","necro","weapons"));break;
                case "clearobjects":case "clearnpcs":params.add(n("Radius",16,0,128));break;
                case "removenpc":params.add(n("NPC index",1,1,32767));break;
                case "locs":case "commands":params.add(n("Page",1,1,100));break;
                case "gameval":case "search":case "findnpc":params.add(t("Name or ID",""));break;
                case "savecoords":params.add(t("Location name",""));break;
                case "uilayout":params.add(t("Action","status","status"));break;
                case "nxt":params.add(t("Tool","status","status","banker","cook","combat","skilling","agility","barbarian","wilderness","slayer","effects","clear","bar","force"));break;
                case "open":case "unhide":case "events":case "cs":case "varbit":case "varc":case "layoutfixture":configured=false;break;
                default: if(segment.contains("[")||segment.contains("<"))configured=false;
            }
            String cat=row[0].equals("COMBAT & RESOURCES")||row[0].equals("ACTION BARS")?"Combat":row[0].equals("GEAR & ITEMS")?"Items":row[0].equals("NPCS & TRAVEL")?"World":"Tools";
            if(Arrays.asList("npc","npcrepeat","npcs","removenpc","clearnpcs","dummy","findnpc","npcinfo").contains(id))cat="NPCs";
            if(Arrays.asList("max","copy","coords").contains(id))cat="Player";
            if(Arrays.asList("melee","mage","range","necro","disengage","combatqa").contains(id))cat="Combat";
            if(id.startsWith("boss"))cat="Combat";
            if(id.equals("comp"))cat="Quests";if(id.equals("uilayout"))cat="Settings";
            actions.put(id,new Action(id,segment,segment,description(id,row[2]),cat,params,configured));
        }
        return Collections.unmodifiableList(new ArrayList<>(actions.values()));
    }
    private static String description(String id,String fallback){
        switch(id){
            case "clearobjects":return "Remove your placed objects within the selected radius and plane, including saved placements. Map objects and other players are protected.";
            case "gameval":return "Browse cache-derived symbolic names with verified 950 bindings and source provenance. Read-only research tool.";
            case "heal":return "Restore health, prayer and run energy, and restore drained stats.";
            case "god":return "Toggle damage immunity. Other combat requirements remain active.";
            case "almighty":return "Toggle full developer combat mode: immunity, infinite resources and cooldown-free Surge, Escape and Dive.";
            case "items":return "Open the Equipment Library: curated gear, global item search and developer loadouts.";
            case "bank":return "Open your real bank to store and withdraw your items.";
            case "dummy":return "Spawn nearby combat dummies for ability and damage testing.";
            case "wars":return "Teleport to War's Retreat, the combat testing hub.";
            case "adrenaline":return "Set your current adrenaline to the selected percentage.";
            case "npc":return "Spawn temporary NPCs by cache ID and amount. Use the NPC browser for chosen-tile placement.";
            case "npcrepeat":return "Spawn test NPCs that respawn after death.";
            case "findnpc":return "Find NPC definitions by partial name or exact cache ID.";
            case "npcs":return "List nearby test NPCs and their indices.";
            case "tele":return "Teleport to the selected world coordinates and plane.";
            case "bugtest":return "Toggle Bug Test diagnostics for live input, interfaces and combat.";
            case "comp":return "Apply permanent completion and combat unlocks for development testing.";
            case "max":return "Raise skills to their development maximum levels.";
            default:return fallback;
        }
    }
}
