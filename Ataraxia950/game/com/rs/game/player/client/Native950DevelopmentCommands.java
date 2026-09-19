package com.rs.game.player.client;

import com.rs.game.ForceMovement;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Locale;

/** Opt-in tools for this local port, without changing account rights or legacy commands. */
public final class Native950DevelopmentCommands {
    public static final String PROPERTY = "ataraxia950.devTools";
    private static final int[] EFFECTS = {94, 184, 436, 1576};
    private Native950DevelopmentCommands() { }

    public static boolean isCommand(String text) {
        if (text == null || !(text.startsWith("::") || text.startsWith(";;"))) return false;
        String command = text.substring(2).trim().toLowerCase(Locale.ROOT).split("\\s+",2)[0];
        return Native950AdminCommands.recognizes(command) || command.equals("nxt") || command.equals("item") || command.equals("npc")
                || command.equals("obj") || command.equals("tele") || command.equals("area")
                || command.equals("areascan") || command.equals("areastop") || command.equals("layoutfixture")
                || command.equals("open") || command.equals("unhide") || command.equals("events") || command.equals("guideclose") || command.equals("cs") || command.equals("varbit") || command.equals("varc");
    }
    /** Bug Test controls are observational and must never interrupt an active manual test. */
    public static boolean preservesGameplay(String text) {
        if (text == null || !(text.startsWith("::") || text.startsWith(";;"))) return false;
        String command = text.substring(2).trim().toLowerCase(Locale.ROOT).split("\\s+", 2)[0];
        return command.equals("bug") || command.equals("bugtest") || command.equals("combatqa");
    }
    public static boolean allowed(boolean enabled, ClientProfile profile, SocketAddress remote) {
        if (!enabled || profile != ClientProfile.NATIVE_950 || !(remote instanceof InetSocketAddress)) return false;
        InetSocketAddress address = (InetSocketAddress) remote;
        return address.getAddress() != null && address.getAddress().isLoopbackAddress();
    }

    interface SpawnActions {
        String npc(Player player, int id);
        String object(Player player, int id, int type, int rotation);
    }
    private static final SpawnActions WORLD_SPAWNS = new SpawnActions() {
        public String npc(Player player, int id) { return Native950DiagnosticSpawns.spawnNpc(player,id); }
        public String object(Player player, int id, int type, int rotation) {
            return type < 0 ? Native950DiagnosticSpawns.spawnObject(player,id)
                    : Native950DiagnosticSpawns.spawnObject(player,id,type,rotation);
        }
    };

    /** Called on the owning native world thread; guide state never crosses sessions. */
    public static void handle(Player player, Channel channel, String text) {
        handle(player, channel, text, WORLD_SPAWNS, null);
    }
    public static void handle(Player player, Channel channel, String text, Native950SkillGuide skillGuide) {
        handle(player, channel, text, WORLD_SPAWNS, skillGuide);
    }
    /** Tests replace only the world mutation boundary. */
    static void handle(Player player, Channel channel, String text, SpawnActions spawns) {
        handle(player, channel, text, spawns, null);
    }
    private static void handle(Player player, Channel channel, String text, SpawnActions spawns,
                               Native950SkillGuide skillGuide) {
        if (!isCommand(text)) throw new IllegalArgumentException("Not a development command");
        if (!allowed(Boolean.getBoolean(PROPERTY), player.getClientProfile(), channel.remoteAddress())) {
            reply(channel, "Local development commands are disabled for this connection."); return;
        }
        String commandText=text.substring(2).trim();
        String[] parts = commandText.toLowerCase(Locale.ROOT).split("\\s+");
        Native950BugTest.command(player, parts[0], parts.length > 1 && !parts[0].equals("bug") ? commandText.substring(parts[0].length()).trim() : "");
        if (Native950AdminCommands.recognizes(parts[0])) {
            // Markers are diagnostic prose, not a command argument. Preserve the user's casing
            // in the JSONL timeline while the command name remains case-insensitive.
            if(parts[0].equals("bug")&&commandText.length()>3)
                parts=new String[]{"bug",commandText.substring(3).trim()};
            Native950AdminCommands.handle(player, channel, parts); return;
        }
        if (!player.isActive() || player.hasFinished() || player.isDead() || player.isLocked()) {
            reply(channel, "Wait until your character can act."); return;
        }
        if (parts[0].equals("layoutfixture")) { Native950LayoutFixture.handle(player, channel, parts); return; }
        if (parts[0].equals("item")) { item(player, channel, parts); return; }
        if (parts[0].equals("npc") || parts[0].equals("obj")) { spawn(player,channel,parts,spawns); return; }
        if (parts[0].equals("tele")) { teleport(player, channel, parts); return; }
        if (parts[0].equals("area")) { area(player, channel, parts); return; }
        if (parts[0].equals("areascan")) { areaScan(player, channel, parts); return; }
        if (parts[0].equals("areastop")) { areaStop(player, channel); return; }
        if (parts[0].equals("open")) { openSub(player, channel, parts); return; }
        if (parts[0].equals("unhide")) { unhide(player, channel, parts); return; }
        if (parts[0].equals("events")) { events(player, channel, parts); return; }
        if (parts[0].equals("guideclose")) { guideClose(channel, skillGuide); return; }
        if (parts[0].equals("cs")) { clientScript(player, channel, parts); return; }
        if (parts[0].equals("varbit") || parts[0].equals("varc")) { variable(player, channel, parts); return; }
        String command = parts.length == 1 ? "help" : parts[1];
        if (command.equals("level")) { skillLevel(player,channel,parts); return; }
        if (parts.length > 2) { reply(channel, "Use ;;nxt for the available commands."); return; }
        switch (command) {
            case "barbarian": move(player, channel, new WorldTile(2552,3561,0)); break;
            case "wilderness": move(player, channel, new WorldTile(2998,3915,0)); break;
            case "slayer": move(player, channel, new WorldTile(3218,3258,0)); break;
            case "agility": move(player, channel, new WorldTile(2474,3437,0)); break;
            case "banker": move(player, channel, new WorldTile(3217, 3258, 0)); break;
            case "cook": move(player, channel, cookDestination()); break;
            case "combat":
                Native950MeleeCombat combat=player.getNative950Combat();
                WorldTile training=combat==null?null:combat.trainingTile();
                if(training==null){reply(channel,"No available melee creature is loaded nearby.");break;}
                player.resetWalkSteps();player.setNextForceMovement(null);player.setNextWorldTile(training);
                reply(channel,"Moving beside a melee creature. Right-click it and choose Attack. Walking away stops your attacks; the creature may pursue you.");
                break;
            case "skilling":
                int missing = (player.getInventory().containsItem(1351,1) ? 0 : 1)
                        + (player.getInventory().containsItem(590,1) ? 0 : 1)
                        + (player.getInventory().containsItem(1265,1) ? 0 : 1);
                if (player.getInventory().getFreeSlots() < missing) {
                    reply(channel,"Free " + missing + " backpack slots for a bronze hatchet, pickaxe and tinderbox."); break;
                }
                if (!player.getInventory().containsItem(1351,1) && !Native950Skilling.giveItem(player,1351,1)
                        || !player.getInventory().containsItem(590,1) && !Native950Skilling.giveItem(player,590,1)
                        || !player.getInventory().containsItem(1265,1) && !Native950Skilling.giveItem(player,1265,1)) {
                    reply(channel,"The skilling tools are unavailable in this cache."); break;
                }
                move(player,channel,new WorldTile(3217,3258,0));
                reply(channel,"Skilling tools supplied (bronze hatchet, pickaxe, tinderbox). Chop nearby trees or mine nearby rocks. Tools work from your backpack.");
                break;
            case "effects":
                for (int id : EFFECTS) if (!Native950PlayerEffects.isVerifiedGraphic(id)) {
                    reply(channel, "The selected cache does not verify effect " + id + "."); return;
                }
                for (int slot=0; slot<4; slot++) player.setNextGraphics(null);
                for (int id : EFFECTS) player.setNextGraphics(new Graphics(id));
                reply(channel, "Queued four player effects: 94, 184, 436, 1576."); break;
            case "clear":
                Native950PlayerEffects.queueClear(player);
                reply(channel, "Cleared all four graphics slots."); break;
            case "bar":
                player.addHitBars(); // show current HP; does not damage or heal the character
                reply(channel, "Queued your current health bar."); break;
            case "force": force(player, channel); break;
            case "status":
                reply(channel, "Tile " + player.getX() + ", " + player.getY() + ", " + player.getPlane()
                        + "; player masks " + Native950EntityMasks.playerBlocks()
                        + "; NPC masks " + Native950EntityMasks.npcBlocks()
                        + "; refused masks " + Native950EntityMasks.refusals() + "."
                        + (player.getNative950Combat()==null?"":" Combat: "+player.getNative950Combat().status())); break;
            default:
                reply(channel, "Local tests: ;;nxt banker | cook | combat | skilling | agility | barbarian | wilderness | slayer.");
                reply(channel, ";;nxt effects | clear | bar | force | status. ;;nxt level <skill ID> <level>.");
                reply(channel, ";;item <id> [quantity]; ;;npc <id>; ;;obj <id> [type] [rotation]; ;;devhelp for admin tools.");
        }
        System.out.println("[Ataraxia950] Local development command: " + command + " at "
                + player.getX() + "," + player.getY() + "," + player.getPlane());
    }

    /** Explicit local diagnostic only: no level is changed when a character merely logs in. */
    private static void skillLevel(Player player,Channel channel,String[] parts) {
        if(parts.length!=4){reply(channel,"Usage: ;;nxt level <skill ID> <level>. This changes your saved skill level and XP.");return;}
        final int skill,level;
        try{skill=Integer.parseInt(parts[2]);level=Integer.parseInt(parts[3]);}
        catch(NumberFormatException invalid){reply(channel,"Use whole numbers for skill ID and level.");return;}
        // Explicit developer requests can exercise every cache skill requirement, including combat gear.
        if(skill<0||skill>=com.rs.game.player.Skills.SKILL_COUNT||level<1
                ||level>com.rs.game.player.Skills.getLevelCap(skill)){
            reply(channel,"Use a skill ID from 0 to 28 and a level within that skill's normal level cap.");return;
        }
        if(player.getNextWorldTile()!=null||player.isNative950ForceMovementActive()||player.getNextForceMovement()!=null){reply(channel,"Wait for movement to finish first.");return;}
        player.getActionManager().forceStop();player.resetWalkSteps();player.setRouteEvent(null);
        player.getSkills().setXpWithoutRefresh(skill,com.rs.game.player.Skills.getXPForLevel(skill,level));
        player.getSkills().set(skill,level);
        reply(channel,com.rs.game.player.Skills.SKILL_NAME[skill]+" is now level "+level+". This is a saved development change.");
    }

    private static void spawn(Player player, Channel channel, String[] parts, SpawnActions spawns) {
        boolean npc=parts[0].equals("npc");
        if(npc && !Native950AdminCommands.authorized(player)) {
            reply(channel,"Administrator or explicitly granted local developer account required.");return;
        }
        if (parts.length<2 || parts.length>(npc?3:4)) {
            reply(channel,npc?"Usage: ;;npc <id> [1-50]":"Usage: ;;obj <id> [type] [rotation]. Rotation defaults to 0.");return;
        }
        final int id,type,rotation;
        try {
            id=Integer.parseInt(parts[1]);
            type=parts.length>2?Integer.parseInt(parts[2]):-1;
            rotation=parts.length>3?Integer.parseInt(parts[3]):0;
        } catch(NumberFormatException invalid) {
            reply(channel,"Use whole numbers for the ID, object type and rotation.");return;
        }
        if(npc && (id<0||id>0x7fffff||(parts.length>2&&(type<1||type>50)))) {
            reply(channel,"NPC ID must be 0-8388607 and amount 1-50.");return;
        }
        if (!npc && (id<0 || (parts.length>2 && (type<0 || type>22)) || rotation<0 || rotation>3)) {
            reply(channel,"ID must be non-negative; object type must be 0-22 and rotation 0-3.");return;
        }
        if (player.isNative950ForceMovementActive() || player.getNextForceMovement()!=null
                || player.getNextWorldTile()!=null || player.hasTeleported()) {
            reply(channel,"Wait until your movement finishes before spawning.");return;
        }
        // Spawn exactly where the player is now, not where a retained route would next move them.
        Native950Firemaking.cancelPending(player);
        player.getActionManager().forceStop();player.setRouteEvent(null);player.resetWalkSteps();
        if(npc) {
            int amount=parts.length>2?type:1,created=0;String result="";
            for(int i=0;i<amount;i++) {
                result=spawns.npc(player,id);
                if(!result.startsWith("Spawned "))break;
                created++;
            }
            reply(channel,result);
            if(amount>1)reply(channel,"Created "+created+" of "+amount+" requested test NPCs. Use ;;clearnpcs to remove them.");
        } else reply(channel,spawns.object(player,id,type,rotation));
    }

    private static void item(Player player, Channel channel, String[] parts) {
        if (parts.length < 2 || parts.length > 3) {
            reply(channel, "Usage: ;;item <id> [quantity]. Quantity defaults to 1."); return;
        }
        final int id, quantity;
        try {
            id = Integer.parseInt(parts[1]);
            quantity = parts.length == 3 ? Integer.parseInt(parts[2]) : 1;
        } catch (NumberFormatException invalid) {
            reply(channel, "Use whole numbers for the item ID and quantity."); return;
        }
        if (id < 0 || id > Native950ItemCatalog.MAX_ITEM_ID || quantity < 1) {
            reply(channel, "Item ID must be 0-16777214 and quantity must be positive."); return;
        }
        Native950ItemCatalog.Entry item = Native950Skilling.itemType(player,id);
        if (item == null) { reply(channel, "That item is unavailable in this 950 inventory."); return; }
        if (!Native950Skilling.hasSpace(player,id,quantity)) {
            reply(channel, "Not enough backpack space or stack capacity for that quantity."); return;
        }
        if (!Native950Skilling.giveItem(player,id,quantity)) {
            reply(channel, "You cannot receive that item right now."); return;
        }
        reply(channel, "Added " + quantity + " x " + item.name + " (" + id + ") to your backpack.");
        System.out.println("[Ataraxia950] Local item command: player=" + player.getIndex()
                + " id=" + id + " quantity=" + quantity);
    }

    /**
     * Keep the staged Cook home at3209,3215. Paired950 tile3209,3216 is
     * Trapdoor36687 (mask0x40000);3208,3215 is clear with a clear cardinal edge.
     * Return a fresh tile so callers cannot mutate the landmark between uses.
     */
    static WorldTile cookDestination() { return new WorldTile(3208,3215,0); }

    private static final String AREA_DEBUG="native950.areaDebug";
    private static final class AreaDebug {
        int override=-1,next=-1,last=-1,end=-1,ticks=2,wait;
    }
    private static AreaDebug areaDebug(Player player,boolean create) {
        AreaDebug state=(AreaDebug)player.getTemporaryAttributtes().get(AREA_DEBUG);
        if(state==null && create) { state=new AreaDebug();player.getTemporaryAttributtes().put(AREA_DEBUG,state); }
        return state;
    }
    /** Diagnostic overrides belong only to this character session, never another logged-in player. */
    public static int resolveAreaType(Player player,int configured) {
        AreaDebug state=areaDebug(player,false);
        return state==null || state.override<0 ? configured : state.override;
    }
    private static void area(Player player,Channel channel,String[] parts) {
        if(parts.length!=2){reply(channel,"Use ;;area <n> or ;;area off.");return;}
        if(parts[1].equals("off"))player.getTemporaryAttributtes().remove(AREA_DEBUG);
        else {
            int value;
            try{value=Integer.parseInt(parts[1]);}catch(NumberFormatException invalid){reply(channel,"Use ;;area <n> or ;;area off.");return;}
            if(value<0 || value>65535){reply(channel,"areaType must fit an unsigned short.");return;}
            AreaDebug state=areaDebug(player,true);state.override=value;state.next=-1;
        }
        player.loadMapRegions();
        int selected=resolveAreaType(player,Native950MapAreas.areaTypeFor(player.getX(),player.getY(),Native950MapAreas.defaultAreaType()));
        reply(channel,"Your scene area is now "+selected+"; ;;area off restores automatic cache selection.");
    }

    /**
     * {@code ;;open <parentInterface> <component> <interfaceId>} - mount any interface into any
     * component, so an unattached panel can be located by experiment.
     *
     * <p>The port attaches 9 of the root's 101 panel slots, so most of the interface tree has
     * never been opened and nothing in the cache says where a given panel belongs. The compass is
     * interface 1919 - its component 2 carries contentType 1339, the only one in all 104,285
     * components, next to 1337 for the 3D scene and 1338 for the minimap viewport - but no root
     * slot names it and no enum maps it, so its mount point has to be found by trying.
     */
    private static void openSub(Player player, Channel channel, String[] parts) {
        if (parts.length != 4) { reply(channel, "Use ;;open <parentInterface> <component> <interfaceId>."); return; }
        int parent, component, child;
        try {
            parent = Integer.parseInt(parts[1]); component = Integer.parseInt(parts[2]); child = Integer.parseInt(parts[3]);
        } catch (NumberFormatException notANumber) { reply(channel, "Use ;;open <parentInterface> <component> <interfaceId>."); return; }
        if (parent < 0 || parent > 65535 || component < 0 || component > 65535 || child < 0 || child > 65535) {
            reply(channel, "Interface and component ids must fit an unsigned short."); return;
        }
        channel.write(Native950Packets.openSub(parent, component, child, false));
        reply(channel, "Opened " + child + " into " + parent + ":" + component + ".");
        System.out.println("[Ataraxia950] Local open command: " + child + " -> " + parent + ":" + component);
    }

    /** {@code ;;unhide <interface> <component> [0|1]} - many minimap controls ship hidden. */
    private static void unhide(Player player, Channel channel, String[] parts) {
        if (parts.length < 3 || parts.length > 4) { reply(channel, "Use ;;unhide <interface> <component> [0|1]."); return; }
        int iface, component, hidden;
        try {
            iface = Integer.parseInt(parts[1]); component = Integer.parseInt(parts[2]);
            hidden = parts.length == 4 ? Integer.parseInt(parts[3]) : 0;
        } catch (NumberFormatException notANumber) { reply(channel, "Use ;;unhide <interface> <component> [0|1]."); return; }
        if (iface < 0 || iface > 65535 || component < 0 || component > 65535) {
            reply(channel, "Interface and component ids must fit an unsigned short."); return;
        }
        channel.write(Native950Packets.hideInterface(iface, component, hidden != 0));
        reply(channel, (hidden != 0 ? "Hid " : "Showed ") + iface + ":" + component + ".");
        System.out.println("[Ataraxia950] Local unhide command: " + iface + ":" + component + " hidden=" + (hidden != 0));
    }

    /**
     * {@code ;;events <interface> <component> <fromSlot> <toSlot> <mask>} - enable interface
     * events on a component, so a panel the port never wired can be made to send its clicks.
     *
     * <p>Mask bit {@code n+1} enables component option {@code n}; with the bit clear the client
     * shows no option and sends nothing at all. Slot -1 addresses the component itself; a range
     * addresses the dynamic children a script created inside it, which is what panels like the
     * skills grid are built from.
     */
    private static void events(Player player, Channel channel, String[] parts) {
        if (parts.length != 6) { reply(channel, "Use ;;events <interface> <component> <fromSlot> <toSlot> <mask>."); return; }
        int iface, component, from, to, mask;
        try {
            iface = Integer.parseInt(parts[1]); component = Integer.parseInt(parts[2]);
            from = Integer.parseInt(parts[3]); to = Integer.parseInt(parts[4]); mask = Integer.parseInt(parts[5]);
        } catch (NumberFormatException notANumber) { reply(channel, "Use ;;events <interface> <component> <fromSlot> <toSlot> <mask>."); return; }
        if (iface < 0 || iface > 65535 || component < 0 || component > 65535
                || from < -1 || to > 65534 || from > to) {
            reply(channel, "Ids must fit an unsigned short and the slot range must be ordered within -1..65534."); return;
        }
        channel.write(Native950Packets.interfaceEvents(iface, component, from, to, mask));
        reply(channel, "Events on " + iface + ":" + component + " slots " + from + ".." + to + " = " + mask + ".");
        System.out.println("[Ataraxia950] Local events command: " + iface + ":" + component
                + " slots " + from + ".." + to + " mask " + mask);
    }

    private static void guideClose(Channel channel, Native950SkillGuide skillGuide) {
        if (skillGuide == null) { reply(channel, "No skill guide is attached to this session."); return; }
        skillGuide.close();
        reply(channel, "Skill guide closed.");
    }

    /**
     * {@code ;;cs <scriptId> [int ...]} - run a client script with integer arguments.
     *
     * <p>The port has no IF_SETPOSITION, so a panel cannot be inset from the server directly; CS2
     * can do it, and RUNCLIENTSCRIPT is a verified writer. This exists to find the script the
     * real client uses to lay a window out inside the Management Windows shell, the same way
     * script 5682 was found for the skill guide's content pane.
     */
    private static void clientScript(Player player, Channel channel, String[] parts) {
        if (parts.length < 2) { reply(channel, "Use ;;cs <scriptId> [int ...]."); return; }
        int id;
        Object[] args = new Object[parts.length - 2];
        try {
            id = Integer.parseInt(parts[1]);
            for (int i = 2; i < parts.length; i++) args[i - 2] = Integer.valueOf(Integer.parseInt(parts[i]));
        } catch (NumberFormatException notANumber) { reply(channel, "Use ;;cs <scriptId> [int ...]."); return; }
        if (id < 0 || id > 65535) { reply(channel, "Script ids fit an unsigned short."); return; }
        channel.write(Native950Packets.runClientScript(id, args));
        StringBuilder text = new StringBuilder().append("Ran client script ").append(id).append('(');
        for (int i = 0; i < args.length; i++) text.append(i == 0 ? "" : ",").append(args[i]);
        reply(channel, text.append(").").toString());
        System.out.println("[Ataraxia950] Local cs command: " + text);
    }

    /**
     * {@code ;;varbit <id> <value>} / {@code ;;varc <id> <value>} - set a client variable.
     *
     * <p>The Management Windows shell (interface 1448 at 1477:715) decides which window it is
     * showing from varbit 18994 and varc 2911; Native950Settings sets both to 9 for Settings.
     * Enum 12737 maps that window id to its interface, and key 23 is 1218, the skill guide.
     */
    private static void variable(Player player, Channel channel, String[] parts) {
        if (parts.length != 3) { reply(channel, "Use ;;varbit <id> <value> or ;;varc <id> <value>."); return; }
        int id, value;
        try { id = Integer.parseInt(parts[1]); value = Integer.parseInt(parts[2]); }
        catch (NumberFormatException notANumber) { reply(channel, "Use ;;varbit <id> <value> or ;;varc <id> <value>."); return; }
        if (id < 0 || id > 65535) { reply(channel, "Ids fit an unsigned short."); return; }
        if (parts[0].equals("varbit")) channel.write(Native950Packets.varbitLarge(id, value));
        else channel.write(Native950Packets.varcLarge(id, value));
        reply(channel, parts[0] + " " + id + " = " + value + ".");
        System.out.println("[Ataraxia950] Local " + parts[0] + " command: " + id + " = " + value);
    }

    /** Optional per-player investigation; normal area selection is derived from the cache. */
    static void advanceAreaSweep(Player player) {
        AreaDebug state=areaDebug(player,false);
        if(state==null || state.next<0 || --state.wait>0)return;
        state.wait=state.ticks;
        if(state.next>state.end){state.next=-1;return;}
        state.last=state.next++;state.override=state.last;player.loadMapRegions();
        System.out.println("[Ataraxia950] area sweep player="+player.getIndex()+" area="+state.last
                +" at "+player.getX()+","+player.getY()+","+player.getPlane());
    }
    private static void areaScan(Player player,Channel channel,String[] parts) {
        if(parts.length<3 || parts.length>4){reply(channel,"Use ;;areascan <from> <to> [ticks].");return;}
        int from,to,ticks;
        try{from=Integer.parseInt(parts[1]);to=Integer.parseInt(parts[2]);ticks=parts.length==4?Integer.parseInt(parts[3]):2;}
        catch(NumberFormatException invalid){reply(channel,"Use ;;areascan <from> <to> [ticks].");return;}
        if(from<0 || to>65535 || from>to || ticks<1 || ticks>20){reply(channel,"Need 0 <= from <= to <= 65535 and 1..20 ticks.");return;}
        AreaDebug state=areaDebug(player,true);state.ticks=ticks;state.wait=1;state.end=to;state.next=from;state.last=-1;
        reply(channel,"Sweeping your scene area "+from+".."+to+"; ;;areastop stops and ;;area off restores automatic selection.");
    }
    private static void areaStop(Player player,Channel channel) {
        AreaDebug state=areaDebug(player,false);
        if(state==null){reply(channel,"No sweep has run in this session.");return;}
        state.next=-1;
        reply(channel,state.last<0?"Sweep stopped.":"Stopped at area "+state.last+"; ;;area off restores automatic selection.");
    }

    /**
     * {@code ;;tele <x> <y> [plane]} - land anywhere, so a scene that renders wrong can be
     * bisected without a lodestone. The rebuild the arrival triggers is the ordinary one, so what
     * the client does with it is exactly what it does for a lodestone landing on the same tile.
     */
    private static void teleport(Player player, Channel channel, String[] parts) {
        if (parts.length < 3 || parts.length > 4) { reply(channel, "Use ;;tele <x> <y> [plane]."); return; }
        int x, y, plane;
        try {
            x = Integer.parseInt(parts[1]); y = Integer.parseInt(parts[2]);
            plane = parts.length == 4 ? Integer.parseInt(parts[3]) : player.getPlane();
        } catch (NumberFormatException notANumber) { reply(channel, "Use ;;tele <x> <y> [plane]."); return; }
        if (x < 0 || x > 16383 || y < 0 || y > 16383 || plane < 0 || plane > 3) {
            reply(channel, "Coordinates must be inside the world."); return;
        }
        WorldTile tile = new WorldTile(x, y, plane);
        World.getRegion(tile.getRegionId(), true);
        if (!World.isRegionLoaded(tile.getRegionId())) { reply(channel, "That map square is not in this cache."); return; }
        player.resetWalkSteps(); player.setRouteEvent(null); player.setNextForceMovement(null);
        player.setNextWorldTile(tile);
        String clear = World.isFloorFree(plane, x, y) ? "" : " (the tile is solid in the cache)";
        reply(channel, "Moving to " + x + "," + y + "," + plane + clear + ".");
        System.out.println("[Ataraxia950] Local teleport command: player=" + player.getIndex()
                + " -> " + x + "," + y + "," + plane + " region " + tile.getRegionId()
                + " map square " + (x >> 6) + "," + (y >> 6) + clear);
    }

    private static void move(Player player, Channel channel, WorldTile tile) {
        if (!World.canMoveNPC(tile, player.getSize())) {
            reply(channel, "The destination is blocked in this cache."); return;
        }
        player.resetWalkSteps(); player.setRouteEvent(null); player.setNextForceMovement(null);
        player.setNextWorldTile(tile);
        reply(channel, "Moving beside the " + (tile.getY() > 3240 ? "Lumbridge banker" : "Cook") + ".");
    }
    private static void force(Player player, Channel channel) {
        // Two open cardinal tiles only, with real collision checks on both edges.
        int[][] choices = {{1,0,ForceMovement.EAST},{0,1,ForceMovement.NORTH},
                {-1,0,ForceMovement.WEST},{0,-1,ForceMovement.SOUTH}};
        for (int[] step : choices) {
            int x=player.getX(), y=player.getY(), plane=player.getPlane(), size=player.getSize();
            if (!World.checkWalkStep(plane,x,y,step[0],step[1],size)
                    || !World.checkWalkStep(plane,x+step[0],y+step[1],step[0],step[1],size)) continue;
            WorldTile target=new WorldTile(x+2*step[0],y+2*step[1],plane);
            player.resetWalkSteps(); player.setRouteEvent(null);
            player.setNextForceMovement(new ForceMovement(target, 2, step[2]));
            player.lock(3); // keep manual clicks from competing with this two-tick demonstration
            reply(channel, "Moving two clear tiles over 1.2 seconds."); return;
        }
        reply(channel, "Move to an open area before testing forced movement.");
    }
    private static void reply(Channel channel, String message) {
        channel.write(Native950Packets.gameMessage(0, message));
    }
}
