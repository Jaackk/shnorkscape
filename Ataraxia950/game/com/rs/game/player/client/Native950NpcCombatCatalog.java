package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.combat.rs2.Rs2AtaraxiaCacheBonuses;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;
import com.rs.utils.data.parsers.npcs.NPCStatsDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCStats;
import java.util.function.IntPredicate;
import java.util.function.IntUnaryOperator;

/** NPC-independent basic melee profiles: current cache metadata plus concrete server stats. */
public final class Native950NpcCombatCatalog {
    private Native950NpcCombatCatalog() { }

    public static boolean supports(int npcId) {
        return Cache.STORE!=null && inspectRunningCache(npcId).profile!=null;
    }
    public static Native950NpcCombatProfile fromRunningCache(int npcId) {
        return inspectRunningCache(npcId).profile;
    }
    /** Missing content is a per-creature refusal, never a reason to remove a visual spawn. */
    public static Resolution inspectRunningCache(int npcId) {
        if(Cache.STORE==null || !Cache.isFlatReadOnly())
            throw new IllegalStateException("Native950 NPC combat requires the paired read-only cache");
        if(npcId<0 || npcId>0x7fffff) return refused("invalid NPC id");
        if(!Native950IdValidity.get().isSafe(Native950IdValidity.Kind.NPC,npcId))
            return refused("unverified legacy NPC identity");
        byte[] raw=Cache.STORE.getIndexes()[18].getFile(npcId>>>7,npcId&127);
        return resolve(npcId,raw,true,NPCCombatDefinitionsDataParser.getDefinitions().get(npcId),
                NPCStatsDataParser.getDefinitions().get(npcId),Native950NpcCombatAnimations::durationCycles);
    }

    /** Pure resolution: no NPC-ID branches, mutable loader defaults or invented health/levels. */
    static Resolution resolve(int npcId,byte[] raw,boolean identitySafe,NPCCombatDefinition combat,
            NPCStats stats,IntUnaryOperator sequenceCycles) {
        if(!identitySafe) return refused("unverified legacy NPC identity");
        if(raw==null) return refused("missing NPC definition");
        final NPCDefinitions definition;
        try {definition=NPCDefinitions.decodeStrict947(npcId,raw,null);}
        catch(RuntimeException invalid){return refused("undecodable NPC definition");}
        if(!hasAttack(definition)) return refused("no Attack option");
        if(definition.transformTo!=null) return refused("transformed NPC requires a resolved combat form");
        if(definition.size<1 || definition.size>Native950MeleeReach.MAX_SIZE || definition.combatLevel<0
                || definition.name==null || definition.name.trim().isEmpty() || "null".equalsIgnoreCase(definition.name))
            return refused("invalid NPC combat metadata");
        if(combat==null) return refused("missing authored combat row");
        if(stats==null) return refused("missing authored stat row");
        if(!"MELEE".equalsIgnoreCase(combat.attackStyle)) return refused("non-melee attack style is not ported");
        if(!positive(combat.getHitpoints(),100000000) || !positive(stats.getAttackLevel(),10000)
                || !positive(stats.getDefenceLevel(),10000) || combat.getMaxHit()<0 || combat.getMaxHit()>10000000
                || !positive(combat.getDeathDelay(),1000) || !positive(combat.getRespawnDelay(),100000))
            return refused("invalid authored combat stats");
        Integer speed=parameter(definition,14),accuracy=parameter(definition,29),armour=parameter(definition,2865);
        // Present but malformed values must not silently select the fallback.
        if(invalidParameter(definition,14,speed) || invalidParameter(definition,29,accuracy)
                || invalidParameter(definition,2865,armour)) return refused("invalid cache combat parameter");
        int attackSpeed=speed==null?combat.getAttackDelay():speed;
        if(!positive(attackSpeed,100) || (accuracy!=null && (accuracy<0 || accuracy>10000000))
                || (armour!=null && (armour<0 || armour>10000000))) return refused("invalid cache combat parameter");
        if(combat.getAttackAnim() < -1 || combat.getDefenceAnim() < -1 || combat.getDeathAnim() < -1)
            return refused("invalid authored animation id");
        int attackCycles=cycles(sequenceCycles,combat.getAttackAnim());
        // An explicit absent animation does not make otherwise complete combat stats unusable.
        // A positive changed binding is different: do not emit an unrelated sequence.
        if(combat.getAttackAnim()>=0 && attackCycles<1) return refused("unverified attack animation binding");
        int blockCycles=cycles(sequenceCycles,combat.getDefenceAnim());
        int deathCycles=cycles(sequenceCycles,combat.getDeathAnim());
        int attack=combat.getAttackAnim(),block=blockCycles>0?combat.getDefenceAnim():-1,
                death=deathCycles>0?combat.getDeathAnim():-1;
        // Long final-pose holds (e.g. human836) are not a hundred-second corpse lifetime.
        // Ordinary animations get their actual duration, bounded to ten display ticks.
        int deathAnimationTicks=deathCycles>0?(int)Math.min(10,((long)deathCycles+29)/30):0;
        Native950NpcCombatProfile profile=new Native950NpcCombatProfile(npcId,definition.size,definition.combatLevel,
                combat.getHitpoints(),stats.getAttackLevel(),stats.getDefenceLevel(),combat.getMaxHit(),
                attackSpeed,combat.getDeathDelay(),combat.getRespawnDelay(),attack,block,death,
                accuracy==null?0:(int)Math.round(accuracy/Rs2AtaraxiaCacheBonuses.NPC_ACCURACY_RATING_DIVISOR),
                armour==null?0:(int)Math.round(armour/Rs2AtaraxiaCacheBonuses.NPC_ARMOUR_RATING_DIVISOR),
                definition.name,speed!=null,accuracy!=null,armour!=null,deathAnimationTicks);
        return new Resolution(profile,null);
    }
    /** Compatibility seam for existing entity-state tests. Pure conversion grants no runtime admission. */
    static Native950NpcCombatProfile fromDefinitions(int id,byte[] raw,NPCCombatDefinition combat,
            NPCStats stats,IntPredicate animationVerified) {
        Resolution result=resolve(id,raw,true,combat,stats,
                sequence->animationVerified!=null && animationVerified.test(sequence)?30:-1);
        if(result.profile==null) throw new IllegalStateException(result.reason+": "+id);
        return result.profile;
    }
    private static int cycles(IntUnaryOperator resolver,int id) {return id<0 || resolver==null?-1:resolver.applyAsInt(id);}
    private static boolean hasAttack(NPCDefinitions definition) {
        if(definition.menuOptions!=null) for(String option:definition.menuOptions)
            if("Attack".equalsIgnoreCase(option)) return true;
        return false;
    }
    private static Integer parameter(NPCDefinitions definition,int key) {
        Object value=definition.clientScriptData==null?null:definition.clientScriptData.get(key);
        return value instanceof Integer?(Integer)value:null;
    }
    private static boolean invalidParameter(NPCDefinitions definition,int key,Integer value) {
        return definition.clientScriptData!=null && definition.clientScriptData.containsKey(key) && value==null;
    }
    private static boolean positive(int value,int max) {return value>0 && value<=max;}
    private static Resolution refused(String reason) {return new Resolution(null,reason);}
    public static final class Resolution {
        public final Native950NpcCombatProfile profile;
        public final String reason;
        private Resolution(Native950NpcCombatProfile profile,String reason){this.profile=profile;this.reason=reason;}
    }
}
