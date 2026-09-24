package com.rs.game.player.client;

import com.rs.game.Hit;

/** Encounter policy, separate from the accepted shared tick/queue implementation.
 * Dagannoth mechanics: RS Wiki Kings strategy; identities: pinned 950 definitions.
 * This is not a claim of complete retail encounters (see combat expansion audit). */
final class Native950BossRules {
    private Native950BossRules(){}
    static boolean aggressive(int id){return king(id)||id==6260||id==6261||id==6263||id==6265;}
    static boolean king(int id){return id>=2881&&id<=2883;}
    static void verifyIdentity(int id){
        if(id==6260){
            Native950Symbols.require("npc","shnorkscape_general_graardor_6260");
            Native950Symbols.require("sequence","godwars_general_graardor_2012_attack_ranged");
            Native950Symbols.require("effect","shnorkscape_godwars_general_graardor_2012_attack_ranged_spotanim");return;
        }
        if(id==6261||id==6263||id==6265){
            String minion=id==6261?"sergeant_strongstack":id==6263?"sergeant_steelwill":"sergeant_grimspike";
            Native950Symbols.require("npc","shnorkscape_"+minion+"_"+id);return;
        }
        if(!king(id))return;
        String name=id==2881?"supreme":id==2882?"prime":"rex";
        if(Native950Symbols.require("npc","shnorkscape_dagannoth_"+name+"_"+id)!=id)
            throw new IllegalStateException("Dagannoth identity changed");
    }
    static Native950NpcCombatProfile presentation(Native950NpcCombatProfile p){
        if(p.npcId==6260)return p.withMaximumHit(300);
        if(!king(p.npcId))return p;
        String prefix="dagannoth_kings_2016_update_";
        String style=p.npcId==2881?"range":p.npcId==2882?"magic":"melee";
        int block=Native950Symbols.require("sequence",prefix+style+"_defend");
        int death=p.npcId==2883?p.deathAnim:Native950Symbols.require("sequence",prefix+style+"_death");
        int projectile=p.npcId==2883?-1:Native950Symbols.require("effect","shnorkscape_"+prefix+style+"_attack_proj");
        int graphic=p.npcId==2883?-1:Native950Symbols.require("effect","shnorkscape_"+prefix+style+"_attack_spotanim");
        if(p.npcId==2882)Native950Symbols.require("effect","shnorkscape_"+prefix+"magic_attack_impact");
        return p.withPresentation(block,death,projectile,graphic,Native950NpcCombatAnimations.durationCycles(death));
    }
    static int impactGraphic(int id){return id==2882?6357:-1;}
    static boolean stunImmune(int id){return id==2881||id==2882||id==6260;}
    static boolean acceptsDamage(int id,Hit.HitLook look){
        if(id==6260&&look==Hit.HitLook.POISON_DAMAGE)return false;
        if(!king(id))return true;
        // Non-style poison remains legitimate; typed autos, abilities, bleeds,
        // bounces and conjures all pass through this same final damage gate.
        if(look!=Hit.HitLook.MELEE_DAMAGE&&look!=Hit.HitLook.RANGE_DAMAGE
                &&look!=Hit.HitLook.MAGIC_DAMAGE&&look!=Hit.HitLook.NECROMANCY_DAMAGE)return true;
        return id==2881?look==Hit.HitLook.MELEE_DAMAGE:
                id==2882?look==Hit.HitLook.RANGE_DAMAGE:look==Hit.HitLook.MAGIC_DAMAGE;
    }
}
