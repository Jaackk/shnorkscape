package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.RenderAnimDefinitions;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.game.player.client.ui.Native950CacheReader;
import java.util.*;

/** Read-only model configuration. Never creates a world NPC or runs combat. */
final class Native950DeveloperPreview {
    /** Same bounds the client's CS21147 applies to the model view's zoom value. */
    static final int MIN_ZOOM=50,MAX_ZOOM=6000;
    final int npc,idle,attack,zoom,height;
    final boolean nativeFraming;
    private Native950DeveloperPreview(int npc,int idle,int attack,int zoom,int height,boolean nativeFraming){
        this.npc=npc;this.idle=idle;this.attack=attack;this.zoom=zoom;this.height=height;this.nativeFraming=nativeFraming;
    }
    static Native950DeveloperPreview unavailable(){return new Native950DeveloperPreview(-1,-1,-1,1000,0,false);}
    static int clampZoom(int zoom){return Math.max(MIN_ZOOM,Math.min(MAX_ZOOM,zoom));}
    // Vulkan-calibrated fallback for common human / small / medium actors.
    // Man at 650 and Supreme at 1350 occupy ~160-170px. Preserve large-actor
    // distance (including the user-accepted Vorago framing). Native Beasts
    // metadata still wins. Footprint is not a universal mesh-bounds estimator.
    static int fallbackDistance(int size){return size<=1?650:size<=3?450*size:1000*Math.min(6,size);}
    /** Apparent size scales with the definition's own resize factor (128 = unscaled). */
    static int scaledDistance(int distance,int scaleX,int scaleY){
        int scale=Math.max(scaleX,scaleY);
        return scale<=0||scale==128?distance:clampZoom((int)((long)distance*scale/128));
    }

    private static Map<String,int[]> nativeByGeometry;
    private static String geometry(NPCDefinitions d){return Arrays.toString(d.models)+"|"+d.getScaleX()+"|"+d.getScaleY();}
    /** Beasts framing keyed by identical model list and scale; ambiguous shapes are dropped. */
    private static synchronized Map<String,int[]> nativeByGeometry(Native950CacheReader reader){
        if(nativeByGeometry!=null)return nativeByGeometry;
        Map<String,int[]> result=new HashMap<>();Set<String> ambiguous=new HashSet<>();
        for(int i=0;i<Native950Beasts.size();i++){
            int boss=reader.enumInt(9031,i);if(boss<0)continue;
            int npc=reader.structInt(boss,1347),zoom=reader.structInt(boss,3040),height=reader.structInt(boss,3041);
            if(npc<0||zoom<=0)continue;
            NPCDefinitions d;try{d=NPCDefinitions.getNPCDefinitions(npc);}catch(RuntimeException invalid){continue;}
            if(d.decodeFailure!=null||d.models==null||d.models.length==0)continue;
            String key=geometry(d);int[] framing={zoom,height};int[] previous=result.putIfAbsent(key,framing);
            if(previous!=null&&!Arrays.equals(previous,framing))ambiguous.add(key);
        }
        result.keySet().removeAll(ambiguous);
        return nativeByGeometry=result;
    }

    static Native950DeveloperPreview resolve(int id){
        if(Cache.STORE==null)return new Native950DeveloperPreview(-1,-1,-1,1000,0,false);
        NPCDefinitions d=NPCDefinitions.getNPCDefinitions(id);
        if(d.decodeFailure!=null||d.transformTo!=null||d.models==null||d.models.length==0)
            return new Native950DeveloperPreview(-1,-1,-1,1000,0,false);
        int idle=-1;
        if(d.renderEmote>=0){
            RenderAnimDefinitions bas=RenderAnimDefinitions.getRenderAnimDefinitions(d.renderEmote);
            if(bas.decodeFailure==null&&bas.standAnimation>=0){
                byte[] sequence=Cache.STORE.getIndexes()[20].getFile(bas.standAnimation>>>7,bas.standAnimation&127);
                if(sequence!=null)try{AnimationDefinitions.decodeStrict947(bas.standAnimation,sequence,null);idle=bas.standAnimation;}
                catch(RuntimeException invalid){/* Keep an unavailable preview stationary. */}
            }
        }
        Native950NpcCombatProfile combat=Native950NpcCombatCatalog.inspectRunningCache(id).profile;
        int attack=combat==null?-1:combat.attackAnim;
        Native950CacheReader reader=new Native950CacheReader.Flat();
        // A developer-saved default always wins over every automatic tier below;
        // it exists specifically to correct them. Height/anchor stay automatic.
        Integer override=Native950DeveloperPreviewOverrides.get("npc:"+id);
        // 1. CS3869's exact NPC identity / height / zoom parameters. No guessed boss IDs.
        for(int i=0;i<Native950Beasts.size();i++){
            int boss=reader.enumInt(9031,i);
            if(boss>=0&&reader.structInt(boss,1347)==id){
                int zoom=reader.structInt(boss,3040),height=reader.structInt(boss,3041);
                if(zoom>0)return new Native950DeveloperPreview(id,idle,attack,override!=null?override:zoom,height,true);
            }
        }
        // 1b. The same native framing for a variant with identical models and scale.
        int[] shared=nativeByGeometry(reader).get(geometry(d));
        if(shared!=null)return new Native950DeveloperPreview(id,idle,attack,override!=null?override:shared[0],shared[1],true);
        // 2. Provisional footprint estimate, corrected by the definition's own resize.
        // Animation identity is never inferred from size.
        int automatic=scaledDistance(fallbackDistance(d.size),d.getScaleX(),d.getScaleY());
        return new Native950DeveloperPreview(id,idle,attack,override!=null?override:automatic,0,false);
    }
}
