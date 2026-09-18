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
            case "almighty": case "infrunes": case "infrun": case "infammo": case "commands": case "spell": case "bugtest": case "bug":
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
        if(command.equals("bugtest")) {
            System.out.println("[Ataraxia950] Bug Test command routed: player="+p.getUsername()+" command=bugtest");
            if(args.length!=1){reply(channel,"Use ;;bugtest.");return;}
            boolean enabled=Native950BugTest.toggle(p);
            reply(channel,enabled?"Bug Test Mode enabled. Use ;;bug <description> when something goes wrong.":"Bug Test Mode disabled. Session log saved.");return;
        }
        if(command.equals("bug")) {
            System.out.println("[Ataraxia950] Bug Test command routed: player="+p.getUsername()+" command=bug");
            StringBuilder description=new StringBuilder();for(int i=1;i<args.length;i++){if(i>1)description.append(' ');description.append(args[i]);}
            Native950BugTest.marker(p,description.toString());reply(channel,"Bug marker recorded. Screenshot capture queued.");return;
        }
        if (args.length > (command.equals("adrenaline") || command.equals("bar") || command.equals("spell") ? 2 : 1)) {
            String usage=command.equals("adrenaline") ? " [0-100]" : command.equals("bar") ? " [1-3]"
                    : command.equals("spell") ? " [strike|bolt|blast|wave|surge]" : "";
            reply(channel, "Usage: ;;" + command + usage); return;
        }
        if (!p.isActive() || p.hasFinished() || p.isDead() || p.isLocked()) {
            reply(channel, "Wait until your character can act."); return;
        }
        switch (command) {
            case "wars": case "warsretreat":reply(channel,Native950WarsRetreat.teleport(p,true));break;
            case "death": case "deathsoffice":reply(channel,Native950WarsRetreat.deathsOffice(p));break;
            case "vorago":reply(channel,Native950WarsRetreat.voragoEntrance(p));break;
            case "dummy":reply(channel,Native950DiagnosticSpawns.spawnTrainingDummy(p));break;
            case "testbar":p.getNative950ActionBar().testBar(p,channel);break;
            case "clearbar":p.getNative950ActionBar().clear(p,channel);break;
            case "bar":
                if(args.length==1){reply(channel,"Active saved action bar: "+(p.getNative950ActionBar().activeBar()+1)+". Use ;;bar <1-3>.");break;}
                int requestedBar;
                try{requestedBar=Integer.parseInt(args[1]);}catch(NumberFormatException invalid){reply(channel,"Use ;;bar <1-3>.");break;}
                if(requestedBar<1||requestedBar>Native950ActionBar.BARS){reply(channel,"Use ;;bar <1-3>.");break;}
                p.getNative950ActionBar().setActiveBar(p,channel,requestedBar-1);
                reply(channel,"Action bar "+requestedBar+" selected and saved.");break;
            case "revo": case "revolution":
                boolean revolution=!p.getNative950ActionBar().isRevolutionEnabled();
                p.getNative950ActionBar().setRevolutionEnabled(channel,revolution);
                reply(channel,"Server-side Revolution "+state(revolution)+" for this saved action bar.");
                reply(channel,"The Combat Settings checkbox still awaits its native client acknowledgement fix.");break;
            case "spell":
                if(args.length==1){reply(channel,"Selected native auto-spell: "+Native950AutoSpells.select(p).name+". Use ;;spell strike|bolt|blast|wave|surge.");break;}
                reply(channel,Native950AutoSpells.choose(p,args[1]));break;
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
        String[] rows={
            "<col=ffd166>===== SHNORKSCAPE LOCAL COMMANDS =====</col>",
            "<col=ffd166>COMBAT</col> ;;almighty - all six infinite combat resources; ;;god - damage immunity.",
            ";;infprayer - no drain; ;;infadren - endless adrenaline; ;;infrunes - free spell runes.",
            ";;infrun - endless run energy; ;;infammo - no ammunition use; ;;adrenaline [0-100] - set energy.",
            ";;heal or ;;refill - restore health, prayer, run and drained levels; ;;max - max all skills.",
            ";;spell [strike|bolt|blast|wave|surge] - view or select an Air auto-spell.",
            "<col=ffd166>ACTION BARS</col> ;;bar [1-3] - view/select a saved action bar; ;;revo - toggle server Revolution.",
            ";;testbar - place Backhand, Binding Shot and Impact in slots 1-3; ;;clearbar - empty the selected bar.",
            "<col=ffd166>BUG TEST</col> ;;bugtest - toggle local UI/combat telemetry; ;;bug <description> - mark an issue and capture the game window.",
            "<col=ffd166>GEAR & ITEMS</col> ;;meleegear, ;;magegear, ;;rangegear - add full combat kits; ;;weapons - weapon kit.",
            ";;gear melee|mage|range|weapons - choose a kit; ;;item <id> [amount] - give an item.",
            ";;search <name> [page] - find item IDs; ;;findnpc <name> [page] - find NPC IDs; ;;gearhelp - kit details.",
            "<col=ffd166>NPCS & TRAVEL</col> ;;npc <id> [1-50] - spawn your test NPCs; ;;npcs - list them.",
            ";;removenpc <index> - remove one; ;;clearnpcs [0-128] - remove nearby test NPCs; ;;dummy - training dummy.",
            ";;wars - War's Retreat; ;;death - Death's Office; ;;vorago - Vorago borehole entrance.",
            ";;tele <x> <y> [plane] - coordinate teleport; ;;coords - current tile and region.",
            ";;disengage - stop native combat and movement; ;;obj <id> [type] [rotation] - diagnostic object.",
            "<col=ffd166>DEVELOPMENT</col> ;;devstatus - resource modes; ;;nxt status - native world/combat diagnostics.",
            ";;nxt level <skill ID> <level> - set a saved level; ;;nxt banker|cook|combat|skilling|agility|barbarian|wilderness|slayer.",
            ";;nxt effects|clear|bar|force - display diagnostics; ;;area, ;;areascan, ;;areastop - scene debugging.",
            ";;open, ;;unhide, ;;events, ;;guideclose, ;;cs, ;;varbit, ;;varc - native UI diagnostics; ;;devhelp - brief help."
        };
        for(String row:rows)reply(channel,row);
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
