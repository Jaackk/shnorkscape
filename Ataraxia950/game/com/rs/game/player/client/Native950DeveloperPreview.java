package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cache.loaders.RenderAnimDefinitions;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.game.player.client.ui.Native950CacheReader;

/** Read-only model configuration. Never creates a world NPC or runs combat. */
final class Native950DeveloperPreview {
    final int npc,idle,attack,zoom,height;
    final boolean nativeFraming;
    private Native950DeveloperPreview(int npc,int idle,int attack,int zoom,int height,boolean nativeFraming){
        this.npc=npc;this.idle=idle;this.attack=attack;this.zoom=zoom;this.height=height;this.nativeFraming=nativeFraming;
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
        // CS3869's exact NPC identity / height / zoom parameters. No guessed boss IDs.
        for(int i=0;i<Native950Beasts.size();i++){
            int boss=reader.enumInt(9031,i);
            if(boss>=0&&reader.structInt(boss,1347)==id){
                int zoom=reader.structInt(boss,3040),height=reader.structInt(boss,3041);
                if(zoom>0)return new Native950DeveloperPreview(id,idle,attack,zoom,height,true);
            }
        }
        // Explicit provisional framing for ordinary definitions; physical small/
        // large acceptance is required. Animation identity is never inferred from size.
        return new Native950DeveloperPreview(id,idle,attack,Math.max(250,1100/Math.max(1,d.size)),0,false);
    }
}
