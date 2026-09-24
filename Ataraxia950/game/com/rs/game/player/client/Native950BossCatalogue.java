package com.rs.game.player.client;

import com.google.gson.Gson;
import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Curated support/evidence index. Runtime admission remains Native950NpcCombatCatalog. */
final class Native950BossCatalogue {
    static final class Data { List<Boss> bosses; }
    static final class Boss {
        int id,footprint,route;String name,symbol,status,presentationStatus,location,reset,multiplayer;
        int[] forms,animations,projectiles,graphics,minions;
        String[] styles,phases,mechanics,immunities,gaps,evidence;
    }
    private static final List<Boss> ALL=load();
    private static final Map<String,List<NPC>> TESTS=new HashMap<>();
    private static List<Boss> load(){
        try(InputStream in=Native950BossCatalogue.class.getResourceAsStream("/native950/boss-support-950.json")){
            if(in==null)throw new IllegalStateException("Missing boss support matrix");
            Data data=new Gson().fromJson(new InputStreamReader(in,StandardCharsets.UTF_8),Data.class);
            Set<Integer> ids=new HashSet<>();
            for(Boss b:data.bosses)if(b.id<0||!ids.add(b.id)||b.name==null||b.gaps==null||b.route< -1||b.route>=Native950CombatTravel.DESTINATIONS.size())throw new IllegalStateException("Invalid boss record");
            return Collections.unmodifiableList(data.bosses);
        }catch(IOException e){throw new IllegalStateException(e);}
    }
    static Boss find(int id){for(Boss b:ALL)if(b.id==id)return b;return null;}
    static List<Native950DeveloperCatalogue.Entry> search(String query){
        String q=query==null?"":query.trim().toLowerCase(Locale.ROOT);List<Native950DeveloperCatalogue.Entry> out=new ArrayList<>();
        for(Boss b:ALL)if(q.isEmpty()||Integer.toString(b.id).equals(q)||(b.name+" "+b.symbol+" "+b.status+" "+String.join(" ",b.styles)).toLowerCase(Locale.ROOT).contains(q))
            out.addAll(Native950DeveloperCatalogue.search("NPC",Integer.toString(b.id)));
        return out;
    }
    static List<String> details(int id){
        Boss b=find(id);if(b==null)throw new IllegalArgumentException("No curated boss record for "+id);
        List<String> out=new ArrayList<>(Native950CombatInspector.npc(id));
        out.add("Boss status: "+b.status+"; presentation: "+b.presentationStatus);
        out.add("Forms="+Arrays.toString(b.forms)+"; styles="+Arrays.toString(b.styles)+"; location="+b.location);
        out.add("Mechanics: "+String.join("; ",b.mechanics));out.add("Immunities: "+String.join("; ",b.immunities));
        out.add("Minions="+Arrays.toString(b.minions)+"; reset="+b.reset);out.add("Multiplayer: "+b.multiplayer);
        out.add("Gaps: "+String.join("; ",b.gaps));
        out.add(b.route<0?"ACCESS BLOCKED: no audited developer destination.":"Audited developer route: ;;bossgo "+id+" (sandbox travel; not retail entry requirements).");
        related(out,"sequence",b.animations);related(out,"effect",b.projectiles);related(out,"effect",b.graphics);
        if(b.route>=0)out.add("Entrance relation: "+Native950CombatTravel.DESTINATIONS.get(b.route).symbol+"; source: audited map route.");
        return out;
    }
    private static void related(List<String> out,String type,int[] ids){
        for(int id:ids){boolean found=false;
            for(Native950GamevalLookup.Entry e:Native950GamevalLookup.search(Integer.toString(id)))if(e.type.equals(type)){
                out.add("Related "+type+" "+id+": "+e.name+"; "+e.verify());out.add("Provenance: "+e.evidence);found=true;
            }
            if(!found)out.add("Related "+type+" "+id+": numeric production binding; no audited symbolic name.");
        }
    }
    static void travel(Player p,int id){
        Boss b=find(id);if(b==null||b.route<0)throw new IllegalArgumentException("No audited boss destination for this ID.");
        Native950WorldTraversal.arriveDeveloper(p,Native950CombatTravel.DESTINATIONS.get(b.route).object());
    }
    static boolean testActor(int id){return Native950BossRules.aggressive(id);}
    static String spawn(Player p,int id){
        Boss b=find(id);if(b==null||(!Native950BossRules.king(id)&&id!=6260))return "Temporary encounters are currently audited for Kings and normal Graardor only.";
        String refusal=Native950DiagnosticSpawns.refusal(p);if(refusal!=null)return refusal;
        List<NPC> previous=TESTS.get(p.getUsername());
        if(previous!=null&&previous.stream().anyMatch(World::containsNPC))return "Clear your previous temporary encounter with ;;bossclear first.";
        TESTS.remove(p.getUsername());
        List<NPC> created=new ArrayList<>();
        try{
            int[] actors=new int[1+b.minions.length];actors[0]=id;System.arraycopy(b.minions,0,actors,1,b.minions.length);
            for(int i=0;i<actors.length;i++){
                Native950NpcCombatCatalog.Resolution resolution=Native950NpcCombatCatalog.inspectRunningCache(actors[i]);
                if(resolution.profile==null)throw new IllegalArgumentException("Actor "+actors[i]+": "+resolution.reason);
                // Spaced 4x4 cells prevent overlap even for the three-tile boss.
                WorldTile tile=new WorldTile(p.getX()+3+(i%2)*4,p.getY()+3+(i/2)*4,p.getPlane());
                created.add(Native950DiagnosticSpawns.placeNpc(p,actors[i],tile,false));
            }
            TESTS.put(p.getUsername(),created);
            return "Created your temporary "+b.name+" test encounter ("+created.size()+" actors). One life; ;;bossclear removes only your owned boss actors.";
        }catch(RuntimeException failed){
            for(NPC npc:created)if(World.containsNPC(npc))Native950World.getInstance().removeDiagnosticNpc(npc);
            return "Encounter not created; rolled back all new actors: "+failed.getMessage();
        }
    }
    static int cleanup(Player p){
        List<NPC> actors=TESTS.remove(p.getUsername());int count=0;
        if(actors!=null)for(NPC npc:actors)if(World.containsNPC(npc)&&Native950DiagnosticSpawns.ownedBy(p,npc)){
            Native950World.getInstance().removeDiagnosticNpc(npc);count++;
        }
        return count;
    }
    static String clear(Player p){
        String refusal=Native950DiagnosticSpawns.refusal(p);if(refusal!=null)return refusal;
        int count=cleanup(p);
        return "Removed "+count+" of your temporary boss/bodyguard actors. Map spawns and other owners are unchanged.";
    }
}
