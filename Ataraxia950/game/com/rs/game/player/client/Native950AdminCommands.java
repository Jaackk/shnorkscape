package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/** Session-only tools, behind the native local-development gate and an explicit account grant. */
public final class Native950AdminCommands {
    public static final String ACCOUNTS = "ataraxia950.devAccounts";

    /*
     * Permanent local-development rule: add every player/admin/development command to this
     * directory in the same change as its handler. Keep aliases beside their canonical command.
     */
    private static final CommandGroup[] COMMAND_DIRECTORY = {
            group("COMBAT & RESOURCES", "ffd166", "fff1b8",
                    entry(";;almighty, ;;god", "all six infinite combat resources; damage immunity"),
                    entry(";;infprayer, ;;infadren, ;;infrunes", "infinite prayer, adrenaline, or combat runes"),
                    entry(";;infrun, ;;infammo, ;;adrenaline [0-100]", "infinite run or ammo; set adrenaline"),
                    entry(";;heal / ;;refill, ;;max, ;;comp", "restore resources; max skills; or grant local completion state"),
                    entry(";;spell [strike|bolt|blast|wave|surge]", "view or choose an Air auto-spell")),
            group("ACTION BARS", "7dd3fc", "bae6fd",
                    entry(";;bar [1-4]", "view or select a saved action bar"),
                    entry(";;copybar <player> [1-4]", "copy their chosen or active bar to your current bar"),
                    entry(";;revo / ;;revolution", "toggle server-side Revolution for the saved bar"),
                    entry(";;testbar, ;;clearbar", "add the test abilities; empty the selected bar")),
            group("BUG TEST", "c4b5fd", "e9d5ff",
                    entry(";;bugtest", "start or stop local UI/combat telemetry"),
                    entry(";;bug <description>", "record a marked state snapshot and queue a game-window screenshot"),
                    entry(";;combatqa [stop|status|reset|cleanup]", "record and manage an automatic combat flight-recorder session")),
            group("GEAR & ITEMS", "86efac", "d9f99d",
                    entry(";;meleegear, ;;magegear, ;;rangegear / ;;ragegear, ;;necrogear", "add a high-tier combat kit"),
                    entry(";;weapons, ;;gear melee|mage|range|necro|weapons, ;;gearhelp", "choose a kit or show its contents"),
                    entry(";;item <id> [amount]", "add an item by cache ID"),
                    entry(";;bank", "open your bank"),
                    entry(";;copy <player>", "replace your inventory and worn gear with an online player's"),
                    entry(";;items", "open the paged Developer Item Browser; search, recent, and quantity controls"),
                    entry(";;search / ;;find / ;;si / ;;itemid / ;;finditem <name> [page]", "find item IDs"),
                    entry(";;findnpc / ;;snpc <name> [page]", "find NPC IDs")),
            group("NPCS & TRAVEL", "fda4af", "fecdd3",
                    entry(";;npc <id> [1-50], ;;npc <name>, ;;npcs", "spawn one-life test NPCs or find spawnable IDs"),
                    entry(";;npcrepeat <id> [1-50], ;;npc repeat <id> [1-50]", "spawn test NPCs that respawn after death"),
                    entry(";;removenpc / ;;delnpc <index>, ;;clearnpcs [0-128]", "remove one or nearby test NPCs"),
                    entry(";;dummy [1-5]", "spawn up to five owned training dummies"),
                    entry(";;wars / ;;warsretreat, ;;death / ;;deathsoffice, ;;vorago", "travel to combat destinations"),
                    entry(";;tele <x> <y> [plane], ;;coords", "teleport by coordinates; report your current tile"),
                    entry(";;savecoords [x y plane] <name>", "save a named location for all players"),
                    entry(";;locs / ;;locations [page]", "list shared saved locations and coordinates"),
                    entry(";;teleto / ;;tpto <player>", "teleport to an online player"),
                    entry(";;disengage, ;;obj <id> [type] [rotation]", "stop combat/movement; spawn a diagnostic object")),
            group("DEVELOPMENT", "f9a8d4", "fbcfe8",
                    entry(";;devstatus, ;;commands / ;;devhelp", "show resource modes; show this directory"),
                    entry(";;uilayout status", "show native workspace diagnostic state"),
                    entry(";;layoutfixture stage|apply", "pinned layout application gate; layoutgate2 only"),
                    entry(";;nxt status|level|banker|cook|combat|skilling|agility|barbarian|wilderness|slayer", "native world and combat tools"),
                    entry(";;nxt effects|clear|bar|force", "player-effect and hit-bar diagnostics"),
                    entry(";;area, ;;areascan, ;;areastop", "scene-area debugging"),
                    entry(";;open, ;;unhide, ;;events, ;;guideclose", "native interface diagnostics"),
                    entry(";;cs, ;;varbit, ;;varc", "client-script and variable diagnostics"))
    };

    private Native950AdminCommands() { }

    static boolean recognizes(String command) {
        if (Native950ContentCommands.recognizes(command)) return true;
        switch (command) {
            case "god": case "infprayer": case "infadren": case "adrenaline": case "bank": case "copy": case "copybar": case "teleto": case "tpto":
            case "savecoords": case "locs": case "locations":
            case "almighty": case "infrunes": case "infrun": case "infammo": case "commands": case "spell": case "bugtest": case "bug": case "combatqa": case "items": case "uilayout": case "comp":
            case "wars": case "warsretreat": case "death": case "deathsoffice": case "vorago": case "dummy": case "testbar": case "clearbar": case "bar":
            case "revo": case "revolution":
            case "heal": case "refill": case "max": case "coords": case "disengage": case "devhelp": case "devstatus":
                return true;
            default: return false;
        }
    }

    static boolean authorized(Player player) {
        if (player.hasAdminRights() || player.isOwner()) return true;
        for (String name : System.getProperty(ACCOUNTS, "").split(","))
            if (!name.trim().isEmpty() && name.trim().equalsIgnoreCase(player.getUsername())) return true;
        return false;
    }

    static void handle(Player p, Channel channel, String[] args) {
        if (!Native950DevelopmentCommands.allowed(p, channel) || !authorized(p)) {
            reply(channel, "Administrator or explicitly granted local developer account required."); return;
        }
        String command = args[0];
        if (Native950ContentCommands.recognizes(command)) {
            if (!p.isActive() || p.hasFinished() || p.isDead() || p.isLocked()) {
                reply(channel, "Wait until your character can act."); return;
            }
            Native950ContentCommands.handle(p, channel, args); return;
        }
        if(command.equals("commands")) { commandList(channel,args);return; }
        if(command.equals("bugtest")) {
            System.out.println("[Ataraxia950] Bug Test command routed: player="+p.getUsername()+" command=bugtest");
            if(args.length!=1){reply(channel,"Use ;;bugtest.");return;}
            boolean enabled=Native950BugTest.toggle(p);
            reply(channel,enabled?"Bug Test Mode enabled. Use ;;bug <description> when something goes wrong.":"Bug Test Mode disabled. Session log saved.");return;
        }
        if(command.equals("bug")) {
            System.out.println("[Ataraxia950] Bug Test command routed: player="+p.getUsername()+" command=bug");
            StringBuilder description=new StringBuilder();for(int i=1;i<args.length;i++){if(i>1)description.append(' ');description.append(args[i]);}
            String summary=description.toString().trim();
            Native950BugTest.marker(p,summary);
            reply(channel,summary.isEmpty()?"Bug marker recorded. Screenshot capture queued.":"Bug marker recorded: "+summary);return;
        }
        if(command.equals("combatqa")) {
            if(args.length>2){reply(channel,"Use ;;combatqa [stop|status|reset|cleanup].");return;}
            String action=args.length==1?"start":args[1];
            if(action.equals("start"))reply(channel,Native950CombatQa.start(p));
            else if(action.equals("stop"))reply(channel,Native950CombatQa.stop(p,"command"));
            else if(action.equals("status"))reply(channel,Native950CombatQa.status(p));
            else if(action.equals("reset"))reply(channel,Native950CombatQa.reset(p));
            else if(action.equals("cleanup"))reply(channel,Native950CombatQa.cleanup());
            else reply(channel,"Use ;;combatqa [stop|status|reset|cleanup].");
            return;
        }
        if(command.equals("items")) {
            if(args.length!=1){reply(channel,"Use ;;items.");return;}
            if (!p.isActive() || p.hasFinished() || p.isDead() || p.isLocked()) { reply(channel,"Wait until your character can act."); return; }
            Native950ItemBrowser.open(p);return;
        }
        int maxArgs=command.equals("copybar")||command.equals("savecoords")?Integer.MAX_VALUE
                :command.equals("adrenaline")||command.equals("bar")||command.equals("spell")||command.equals("dummy")||command.equals("uilayout")
                ||command.equals("locs")||command.equals("locations")||command.equals("copy")||command.equals("teleto")||command.equals("tpto")?2:1;
        if (args.length > maxArgs) {
            String usage=command.equals("adrenaline") ? " [0-100]" : command.equals("bar") ? " [1-4]"
                    : command.equals("spell") ? " [strike|bolt|blast|wave|surge]" : command.equals("dummy") ? " [1-5]" : command.equals("uilayout") ? " status" : "";
            reply(channel, "Usage: ;;" + command + usage); return;
        }
        if (!p.isActive() || p.hasFinished() || p.isDead() || p.isLocked()) {
            reply(channel, "Wait until your character can act."); return;
        }
        switch (command) {
            case "savecoords":
                saveCoords(p,channel,args);break;
            case "locs": case "locations":
                listLocations(channel,args);break;
            case "copybar":
                copyBar(p,channel,args);break;
            case "bank":
                if(args.length!=1){reply(channel,"Use ;;bank.");break;}
                p.getBank().openBank();
                Native950Session.allowRemoteBank(p);
                reply(channel,"Bank opened.");
                break;
            case "teleto": case "tpto":
                Player teleportTarget=onlinePlayer(args,channel,command);if(teleportTarget==null)break;
                teleportToPlayer(p,teleportTarget);reply(channel,"Teleporting to "+teleportTarget.getDisplayName()+".");break;
            case "copy":
                Player copyTarget=onlinePlayer(args,channel,command);if(copyTarget==null)break;
                if(copyTarget==p){reply(channel,"You already have your own inventory and equipment.");break;}
                copyLoadout(p,copyTarget);reply(channel,"Copied inventory and worn equipment from "+copyTarget.getDisplayName()+".");break;
            case "uilayout":
                if(args.length!=2||!args[1].equals("status")){reply(channel,"Use ;;uilayout status.");break;}
                reply(channel,Native950Workspace.status(p));break;
            case "comp":
                Native950Completionist.grant(p);
                reply(channel,"Completionist development state granted: 200m XP, all local skill caps, tracked quests, achievements and cape flags. Saved with this local profile.");
                break;
            case "wars": case "warsretreat":reply(channel,Native950WarsRetreat.teleport(p,true));break;
            case "death": case "deathsoffice":reply(channel,Native950WarsRetreat.deathsOffice(p));break;
            case "vorago":reply(channel,Native950WarsRetreat.voragoEntrance(p));break;
            case "dummy":
                int requestedDummies=1;
                try{if(args.length==2)requestedDummies=Integer.parseInt(args[1]);}catch(NumberFormatException invalid){reply(channel,"Use ;;dummy [1-5].");break;}
                reply(channel,Native950DiagnosticSpawns.spawnTrainingDummies(p,requestedDummies));break;
            case "testbar":p.getNative950ActionBar().testBar(p,channel);break;
            case "clearbar":p.getNative950ActionBar().clear(p,channel);break;
            case "bar":
                if(args.length==1){reply(channel,"Active saved action bar: "+(p.getNative950ActionBar().activeBar()+1)+". Use ;;bar <1-4>.");break;}
                int requestedBar;
                try{requestedBar=Integer.parseInt(args[1]);}catch(NumberFormatException invalid){reply(channel,"Use ;;bar <1-4>.");break;}
                if(requestedBar<1||requestedBar>Native950ActionBar.BARS){reply(channel,"Use ;;bar <1-4>.");break;}
                p.getNative950ActionBar().setActiveBar(p,channel,requestedBar-1);
                reply(channel,"Action bar "+requestedBar+" selected and saved.");break;
            case "revo": case "revolution":
                boolean revolution=!p.getNative950ActionBar().isRevolutionEnabled();
                p.getNative950ActionBar().setRevolutionEnabled(channel,revolution);
                reply(channel,"Server-side Revolution "+state(revolution)+" for this saved action bar.");
                reply(channel,"The Combat Settings checkbox still awaits its native client acknowledgement fix.");break;
            case "spell":
                if(args.length==1){reply(channel,"Selected native auto-spell: "+Native950AutoSpells.select(p).name+". Use ;;spell strike|bolt|blast|wave|surge.");break;}
                reply(channel,Native950AutoSpells.choose(p,channel,args[1]));break;
            case "almighty":
                boolean enabled=!(p.isDevelopmentGodMode()&&p.getPrayer().isInfinitePrayer()
                        &&p.getCombatDefinitions().isInfiniteAdrenaline()&&p.isInfiniteRunEnergy()
                        &&p.isInfiniteCombatRunes()&&p.isInfiniteAmmunition());
                p.setDevelopmentGodMode(enabled);p.getPrayer().setInfinitePrayer(enabled);
                p.getCombatDefinitions().setInfiniteAdrenaline(enabled);p.setInfiniteRunEnergy(enabled);
                p.setInfiniteCombatRunes(enabled);p.setInfiniteAmmunition(enabled);
                if(enabled){heal(p);p.getCombatDefinitions().setSpecialAttackPercentage(100);}
                reply(channel,"<col=ffd166>ALMIGHTY "+state(enabled)+"</col> - god, prayer, adrenaline, run energy, combat runes and ammo.");
                if(enabled)reply(channel,"Compatible arrows/bolts are supplied automatically for your ranged weapon. Modes reset on logout. ;;almighty again disables all six.");
                break;
            case "infrunes":
                p.setInfiniteCombatRunes(!p.isInfiniteCombatRunes());
                reply(channel,"Infinite combat runes "+state(p.isInfiniteCombatRunes())+".");break;
            case "infrun":
                p.setInfiniteRunEnergy(!p.isInfiniteRunEnergy());if(p.isInfiniteRunEnergy())p.setRunEnergy(100);
                reply(channel,"Infinite run energy "+state(p.isInfiniteRunEnergy())+".");break;
            case "infammo":
                p.setInfiniteAmmunition(!p.isInfiniteAmmunition());
                reply(channel,"Infinite ammunition "+state(p.isInfiniteAmmunition())+". Compatible arrows/bolts follow your equipped ranged weapon automatically.");break;
            case "god":
                p.setDevelopmentGodMode(!p.isDevelopmentGodMode());
                if (p.isDevelopmentGodMode()) { p.setHitpoints(p.getMaxHitpoints()); p.refreshHitPoints(); }
                reply(channel, "God mode " + state(p.isDevelopmentGodMode()) + "."); break;
            case "infprayer":
                p.getPrayer().setInfinitePrayer(!p.getPrayer().isInfinitePrayer());
                if (p.getPrayer().isInfinitePrayer()) p.getPrayer().restorePrayer(Skills.getLevelCap(Skills.PRAYER) * 10);
                reply(channel, "Infinite prayer " + state(p.getPrayer().isInfinitePrayer()) + "."); break;
            case "infadren":
                p.getCombatDefinitions().setInfiniteAdrenaline(!p.getCombatDefinitions().isInfiniteAdrenaline());
                if (p.getCombatDefinitions().isInfiniteAdrenaline()) p.getCombatDefinitions().setSpecialAttackPercentage(100);
                reply(channel, "Infinite adrenaline/energy " + state(p.getCombatDefinitions().isInfiniteAdrenaline()) + "."); break;
            case "adrenaline":
                int amount = 100;
                try { if (args.length == 2) amount = Integer.parseInt(args[1]); }
                catch (NumberFormatException invalid) { reply(channel, "Use ;;adrenaline [0-100]."); return; }
                if (amount < 0 || amount > 100) { reply(channel, "Use ;;adrenaline [0-100]."); return; }
                if (p.getCombatDefinitions().isInfiniteAdrenaline() && amount < p.getCombatDefinitions().getSpecialAttackPercentage()) {
                    reply(channel, "Disable ;;infadren before lowering energy."); return;
                }
                p.getCombatDefinitions().setSpecialAttackPercentage(amount);
                reply(channel, "Adrenaline/energy set to " + amount + "%. Abilities are not implemented by this command."); break;
            case "max":
                for (int skill = 0; skill < Skills.SKILL_COUNT; skill++) {
                    int cap = Skills.getLevelCap(skill);
                    p.getSkills().setXpWithoutRefresh(skill, Math.max(p.getSkills().getXp(skill), Skills.getXPForLevel(skill, cap)));
                    p.getSkills().set(skill, cap);
                }
                heal(p); reply(channel, "All skills set to their cache-defined caps. XP and levels are saved."); break;
            case "heal": case "refill": heal(p); reply(channel, "Health, prayer, run energy and drained stats restored."); break;
            case "coords":
                reply(channel, "Tile " + p.getX() + "," + p.getY() + "," + p.getPlane() + "; region " + p.getRegionId() + "."); break;
            case "disengage":
                if (p.getNative950Combat() != null) p.getNative950Combat().stop(p);
                p.getActionManager().forceStop(); p.resetWalkSteps(); p.setRouteEvent(null);
                reply(channel, "Native combat disengaged."); break;
            case "devstatus":
                reply(channel, "God: " + state(p.isDevelopmentGodMode()) + "; infinite prayer: " + state(p.getPrayer().isInfinitePrayer())
                        + "; infinite adrenaline: " + state(p.getCombatDefinitions().isInfiniteAdrenaline()) + ".");
                reply(channel,"Run: "+state(p.isInfiniteRunEnergy())+"; combat runes: "+state(p.isInfiniteCombatRunes())
                        +"; ammunition: "+state(p.isInfiniteAmmunition())+".");break;
            default:
                reply(channel, ";;commands shows every supported local command and a short description.");
        }
    }

    private static void commandList(Channel channel,String[] args) {
        if(args.length!=1){reply(channel,"Use ;;commands.");return;}
        reply(channel,"<col=ffd166>===== SHNORKSCAPE LOCAL COMMANDS =====</col>");
        for (CommandGroup group : COMMAND_DIRECTORY) {
            reply(channel,"<col="+group.headerColor+">"+group.title+"</col>");
            for (CommandEntry entry : group.entries)
                reply(channel,"<col="+group.commandColor+">"+entry.names+"</col><col=e8e8e8> - "+entry.description+".</col>");
        }
    }

    private static Player onlinePlayer(String[] args,Channel channel,String command) {
        if(args.length<2){reply(channel,"Use ;;"+command+" <player>.");return null;}
        Player target=World.getPlayerByDisplayName(join(args,1));
        if(target==null||target.hasFinished()||!target.isActive()){reply(channel,"That player is not online.");return null;}
        return target;
    }

    private static String join(String[] args,int from) {
        StringBuilder value=new StringBuilder();for(int i=from;i<args.length;i++){if(i>from)value.append(' ');value.append(args[i]);}return value.toString();
    }

    private static void copyBar(Player player,Channel channel,String[] args) {
        if(args.length<2){reply(channel,"Use ;;copybar <player> [1-4].");return;}
        int requested=-1,end=args.length;
        if(args.length>2 && args[args.length-1].matches("[0-9]+")){
            try{requested=Integer.parseInt(args[args.length-1]);}catch(NumberFormatException invalid){requested=0;}
            end--;
            if(requested<1||requested>Native950ActionBar.BARS){reply(channel,"Use ;;copybar <player> [1-4].");return;}
        }
        String name=join(Arrays.copyOf(args,end),1);
        Player source=World.getPlayerByDisplayName(name);
        if(source==null||source.hasFinished()||!source.isActive()){reply(channel,"That player is not online.");return;}
        int sourceBar=requested<0?source.getNative950ActionBar().activeBar():requested-1;
        player.getNative950ActionBar().copyCurrentBarFrom(player,channel,source.getNative950ActionBar(),sourceBar);
        reply(channel,"Copied "+source.getDisplayName()+"'s bar "+(sourceBar+1)+" into your current bar "+(player.getNative950ActionBar().activeBar()+1)+".");
    }

    private static void saveCoords(Player player,Channel channel,String[] args) {
        if(args.length<2){reply(channel,"Use ;;savecoords [x y plane] <name>.");return;}
        int x=player.getX(),y=player.getY(),plane=player.getPlane(),start=1;
        if(args[1].matches("-?[0-9]+")){
            if(args.length<5){reply(channel,"Use ;;savecoords <x> <y> <plane> <name>.");return;}
            try{x=Integer.parseInt(args[1]);y=Integer.parseInt(args[2]);plane=Integer.parseInt(args[3]);}
            catch(NumberFormatException invalid){reply(channel,"Coordinates must be numbers.");return;}
            start=4;
        }
        String name=join(args,start);
        try{reply(channel,"Saved "+Native950SavedLocations.save(player.getUsername(),name,x,y,plane).line()+".");}
        catch(IllegalArgumentException invalid){reply(channel,"Use a 2-40 character name and coordinates within the game world.");}
        catch(IOException failure){reply(channel,"Location was not saved: "+failure.getMessage());}
    }

    private static void listLocations(Channel channel,String[] args) {
        int page=1;
        try{if(args.length==2)page=Integer.parseInt(args[1]);}
        catch(NumberFormatException invalid){reply(channel,"Use ;;locs [page].");return;}
        if(page<1){reply(channel,"Use ;;locs [page].");return;}
        try{
            List<Native950SavedLocations.Place> places=Native950SavedLocations.list();
            int pages=Math.max(1,(places.size()+9)/10);
            if(page>pages){reply(channel,"Only "+pages+" saved location page(s).");return;}
            reply(channel,"Saved locations: "+places.size()+"; page "+page+"/"+pages+". Use ;;tele <x> <y> <plane>.");
            for(int i=(page-1)*10;i<Math.min(page*10,places.size());i++)reply(channel,places.get(i).line());
        }catch(IOException failure){reply(channel,"Cannot list saved locations: "+failure.getMessage());}
    }

    private static void teleportToPlayer(Player player,Player target) {
        if(player.getNative950Combat()!=null)player.getNative950Combat().stop(player);
        player.getActionManager().forceStop();player.resetWalkSteps();player.setRouteEvent(null);player.setNextForceMovement(null);
        player.setNextWorldTile(new WorldTile(target));
    }

    private static void copyLoadout(Player player,Player source) {
        Item[] sourceInventory=source.getInventory().getItems().getItems();
        Item[] sourceEquipment=source.getEquipment().getItems().getItems();
        Item[] inventory=player.getInventory().getItems().getItems();
        Item[] equipment=player.getEquipment().getItems().getItems();
        for(int slot=0;slot<inventory.length;slot++)player.getInventory().getItems().set(slot,slot<sourceInventory.length&&sourceInventory[slot]!=null?new Item(sourceInventory[slot]):null);
        for(int slot=0;slot<equipment.length;slot++)player.getEquipment().getItems().set(slot,slot<sourceEquipment.length&&sourceEquipment[slot]!=null?new Item(sourceEquipment[slot]):null);
        player.getInventory().refresh();player.getEquipment().refresh();player.getAppearence().generateAppearenceData();
    }

    private static CommandGroup group(String title, String headerColor, String commandColor, CommandEntry... entries) {
        return new CommandGroup(title, headerColor, commandColor, entries);
    }

    private static CommandEntry entry(String names, String description) { return new CommandEntry(names, description); }

    private static final class CommandGroup {
        private final String title;
        private final String headerColor;
        private final String commandColor;
        private final CommandEntry[] entries;

        private CommandGroup(String title, String headerColor, String commandColor, CommandEntry[] entries) {
            this.title = title;
            this.headerColor = headerColor;
            this.commandColor = commandColor;
            this.entries = entries;
        }
    }

    private static final class CommandEntry {
        private final String names;
        private final String description;

        private CommandEntry(String names, String description) {
            this.names = names;
            this.description = description;
        }
    }

    private static void heal(Player p) {
        for (int skill = 0; skill < Skills.SKILL_COUNT; skill++)
            if (p.getSkills().getLevel(skill) < p.getSkills().getLevelForXp(skill))
                p.getSkills().set(skill, p.getSkills().getLevelForXp(skill));
        p.setHitpoints(p.getMaxHitpoints()); p.refreshHitPoints();
        p.getPrayer().restorePrayer(Skills.getLevelCap(Skills.PRAYER) * 10);
        p.setRunEnergy(100);
    }
    private static String state(boolean enabled) { return enabled ? "enabled" : "disabled"; }
    private static void reply(Channel channel, String message) { channel.write(Native950Packets.gameMessage(0, message.length()>180?message.substring(0,177)+"...":message)); }
}
