package com.rs.game.player.client;

import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.network.protocol.modern950.Native950Packets;
import io.netty.channel.Channel;

/** Session-only tools, behind the native local-development gate and an explicit account grant. */
public final class Native950AdminCommands {
    public static final String ACCOUNTS = "ataraxia950.devAccounts";
    private Native950AdminCommands() { }

    static boolean recognizes(String command) {
        if (Native950ContentCommands.recognizes(command)) return true;
        switch (command) {
            case "god": case "infprayer": case "infadren": case "adrenaline":
            case "almighty": case "infrunes": case "infrun": case "infammo": case "commands":
            case "heal": case "max": case "coords": case "disengage": case "devhelp": case "devstatus":
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
        if (!Native950DevelopmentCommands.allowed(Boolean.getBoolean(Native950DevelopmentCommands.PROPERTY),
                p.getClientProfile(), channel.remoteAddress()) || !authorized(p)) {
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
        if (args.length > (command.equals("adrenaline") ? 2 : 1)) {
            reply(channel, "Usage: ;;" + command + (command.equals("adrenaline") ? " [0-100]" : "")); return;
        }
        if (!p.isActive() || p.hasFinished() || p.isDead() || p.isLocked()) {
            reply(channel, "Wait until your character can act."); return;
        }
        switch (command) {
            case "almighty":
                boolean enabled=!(p.isDevelopmentGodMode()&&p.getPrayer().isInfinitePrayer()
                        &&p.getCombatDefinitions().isInfiniteAdrenaline()&&p.isInfiniteRunEnergy()
                        &&p.isInfiniteCombatRunes()&&p.isInfiniteAmmunition());
                p.setDevelopmentGodMode(enabled);p.getPrayer().setInfinitePrayer(enabled);
                p.getCombatDefinitions().setInfiniteAdrenaline(enabled);p.setInfiniteRunEnergy(enabled);
                p.setInfiniteCombatRunes(enabled);p.setInfiniteAmmunition(enabled);
                if(enabled){heal(p);p.getCombatDefinitions().setSpecialAttackPercentage(100);}
                reply(channel,"<col=ffd166>ALMIGHTY "+state(enabled)+"</col> - god, prayer, adrenaline, run energy, combat runes and ammo.");
                if(enabled)reply(channel,"Equip the correct ammo/weapon first. Modes reset on logout. ;;almighty again disables all six.");
                break;
            case "infrunes":
                p.setInfiniteCombatRunes(!p.isInfiniteCombatRunes());
                reply(channel,"Infinite combat runes "+state(p.isInfiniteCombatRunes())+".");break;
            case "infrun":
                p.setInfiniteRunEnergy(!p.isInfiniteRunEnergy());if(p.isInfiniteRunEnergy())p.setRunEnergy(100);
                reply(channel,"Infinite run energy "+state(p.isInfiniteRunEnergy())+".");break;
            case "infammo":
                p.setInfiniteAmmunition(!p.isInfiniteAmmunition());
                reply(channel,"Infinite ammunition "+state(p.isInfiniteAmmunition())+". Equip compatible ammo or a thrown weapon first.");break;
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
            case "heal": heal(p); reply(channel, "Health, prayer, run energy and drained stats restored."); break;
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
                reply(channel, ";;god, ;;infprayer, ;;infadren toggle independently; reset on logout.");
                reply(channel, ";;adrenaline [0-100], ;;heal, ;;max, ;;coords, ;;disengage, ;;devstatus.");
                reply(channel, "Existing: ;;tele <x> <y> [plane], ;;item <id> [quantity], ;;npc <id>, ;;obj <id>, ;;nxt.");
                reply(channel, ";;gearhelp for gear sets, item/NPC search, spawning and cleanup.");
                reply(channel, ";;almighty toggles all combat resource modes; ;;commands shows the full described list.");
        }
    }

    private static void commandList(Channel channel,String[] args) {
        int page=1;
        try { if(args.length>2)throw new NumberFormatException();if(args.length==2)page=Integer.parseInt(args[1]); }
        catch(NumberFormatException invalid){reply(channel,"Use ;;commands [1-4].");return;}
        if(page<1||page>4){reply(channel,"Use ;;commands [1-4].");return;}
        String[][] pages={
            {"COMBAT & RESOURCES", "almighty|Toggle all six combat resource modes", "god|Toggle damage immunity",
             "infprayer|Toggle unlimited prayer", "infadren|Toggle unlimited adrenaline", "infrunes|Toggle free native combat casts",
             "infrun|Toggle unlimited run energy", "infammo|Toggle ammo consumption (equip ammo first)",
             "adrenaline [0-100]|Set energy; defaults to 100", "heal|Restore health, prayer, run and drained stats"},
            {"GEAR & ITEMS", "meleegear|Torva, Chaotic rapier and accessories", "magegear|Virtus, Chaotic staff and accessories",
             "rangegear|Pernix, Chaotic crossbow and bolts", "weapons|Noxious weapons and Drygore rapiers",
             "item <id> [amount]|Give an item to your backpack", "search <name> [page]|Find item IDs; 10 results per page",
             "gearhelp|Gear and search help", "max|Permanently max skills to their individual caps"},
            {"NPCS & TRAVEL", "findnpc <name> [page]|Search NPC definition IDs", "npc <id> [1-50]|Spawn test NPCs at your tile",
             "npcs|List nearby test NPCs and their live indexes", "removenpc <index>|Remove one of your test NPCs",
             "clearnpcs [radius]|Remove your test NPCs", "tele <x> <y> [plane]|Teleport to coordinates",
             "coords|Show your tile and region", "disengage|Stop native combat and movement"},
            {"DEVELOPMENT", "devstatus|Show combat resource toggle states", "nxt status|Show native world/combat diagnostics",
             "nxt level <skill ID> <level>|Set one saved skill", "obj <id> [type] [rotation]|Place a diagnostic object",
             "nxt|List existing native diagnostic tools", "devhelp|Quick administrator help", "commands [1-4]|Browse this command directory"}
        };
        String[] rows=pages[page-1];reply(channel,"<col=ffd166>===== "+rows[0]+" - "+page+"/4 =====</col>");
        for(int i=1;i<rows.length;i++) {
            String[] row=rows[i].split("\\|",2);
            reply(channel,"<col=80d8ff>;;"+row[0]+"</col> - "+row[1]);
        }
        reply(channel,"<col=ffd166>"+(page<4?"Next: ;;commands "+(page+1):"Back to combat: ;;commands 1")+"</col>");
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
    private static void reply(Channel channel, String message) { channel.write(Native950Packets.gameMessage(0, message)); }
}
