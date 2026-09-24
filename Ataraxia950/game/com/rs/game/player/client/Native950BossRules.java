package com.rs.game.player.client;

import com.rs.game.Hit;

/** Encounter policy, separate from the accepted shared tick/queue implementation.
 * Dagannoth mechanics: RS Wiki Kings strategy; identities: pinned 950 definitions.
 * This is not a claim of complete retail encounters (see combat expansion audit). */
final class Native950BossRules {
    private Native950BossRules(){}
    static boolean king(int id){return id>=2881&&id<=2883;}
    static void verifyIdentity(int id){
        if(!king(id))return;
        String name=id==2881?"supreme":id==2882?"prime":"rex";
        if(Native950Symbols.require("npc","shnorkscape_dagannoth_"+name+"_"+id)!=id)
            throw new IllegalStateException("Dagannoth identity changed");
    }
    static boolean stunImmune(int id){return id==2881||id==2882;}
    static boolean acceptsDamage(int id,Hit.HitLook look){
        if(!king(id))return true;
        // Non-style poison remains legitimate; typed autos, abilities, bleeds,
        // bounces and conjures all pass through this same final damage gate.
        if(look!=Hit.HitLook.MELEE_DAMAGE&&look!=Hit.HitLook.RANGE_DAMAGE
                &&look!=Hit.HitLook.MAGIC_DAMAGE&&look!=Hit.HitLook.NECROMANCY_DAMAGE)return true;
        return id==2881?look==Hit.HitLook.MELEE_DAMAGE:
                id==2882?look==Hit.HitLook.RANGE_DAMAGE:look==Hit.HitLook.MAGIC_DAMAGE;
    }
}
