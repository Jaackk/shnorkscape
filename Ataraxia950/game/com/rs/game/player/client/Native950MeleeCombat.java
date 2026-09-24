package com.rs.game.player.client;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Equipment;
import com.rs.game.player.BuffDebuffTimersManager.Timer;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.combat.rs2.ClassicBonuses;
import com.rs.game.player.combat.rs2.RS2BonusDatabase;
import com.rs.game.player.combat.rs2.Rs2CombatFormula;
import com.rs.game.player.content.Combat;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.utils.Utils;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** One world-thread melee loop. No legacy NPC combat, abilities, instance or death callbacks. */
public final class Native950MeleeCombat {
    private static final int LEASH = 16, APPROACH_TIMEOUT = 25, PLAYER_RESPAWN_TICKS = 6;
    // Chain targets the primary plus up to two eligible NPCs within six tiles.
    // This is intentionally independent of the player's primary-target owner.
    private static final int CHAIN_SECONDARY_TARGET_LIMIT = 2, CHAIN_SECONDARY_RANGE = 6;
    private final Thread owner;
    private final Access access;
    private final Rolls rolls;
    private final Loadouts loadouts;
    private final Rewards rewards;
    private final Map<NPC,Fighter> fighters = new IdentityHashMap<>();
    private final Map<Player,Fighter> targets = new IdentityHashMap<>();
    private final Map<Player,Long> nextAttack = new IdentityHashMap<>();
    private final Map<Player,Integer> queuedAbilities = new IdentityHashMap<>();
    private final Map<Player,Long> globalCooldown = new IdentityHashMap<>();
    private final Map<Player,Long> channelUntil = new IdentityHashMap<>();
    private final Map<Player,Map<Integer,Long>> abilityCooldowns = new IdentityHashMap<>();
    private final Map<Player,Map<Integer,DamageOverTime>> damageOverTime = new IdentityHashMap<>();
    private final Native950CombatBuffs buffs = new Native950CombatBuffs();
    private final Native950NecromancyResources necromancy = new Native950NecromancyResources();
    private final Native950Conjures conjures;
    private final Native950CombatAreas areas;
    private final Map<Player,Long> ceaseUntil = new IdentityHashMap<>();
    private final Map<Player,DashImpact> dashImpacts = new IdentityHashMap<>();
    private final Map<Player,java.util.List<PendingHit>> pendingHits = new IdentityHashMap<>();
    private final java.util.List<SkullFlight> skullFlights=new ArrayList<>();
    private final Map<Player,Long> deadPlayers = new IdentityHashMap<>();
    private final Map<Integer,String> unavailableDefinitions = new java.util.TreeMap<>();
    private long tick, swings, hits, kills, respawns;

    Native950MeleeCombat(Thread owner) { this(owner, new LiveRolls()); }
    Native950MeleeCombat(Thread owner, Rolls rolls) { this(owner, new LiveAccess(), rolls, Native950MeleeCombat::loadout, new Rewards() {
        public void hit(Player player,NPC npc,int damage){Native950CombatExperience.awardMelee(player,npc,damage);}
        public void hit(Player player,NPC npc,int damage,Loadout gear){Native950CombatExperience.award(player,npc,damage,gear.profile==null?Native950CombatStyles.MELEE:gear.profile.style);}
        public void death(NPC npc,Player player){Native950Loot.createDeathDrops(npc,player);Native950Slayer.onDeath(player,npc);Native950Dungeoneering.onNpcDeath(player,npc);}
    }); }
    Native950MeleeCombat(Thread owner, Access access, Rolls rolls, Loadouts loadouts) {
        this(owner,access,rolls,loadouts,new Rewards(){public void hit(Player p,NPC n,int d){}public void death(NPC n,Player p){}});
    }
    Native950MeleeCombat(Thread owner, Access access, Rolls rolls, Loadouts loadouts, Rewards rewards) {
        this.owner=owner;this.access=access;this.rolls=rolls;this.loadouts=loadouts;this.rewards=rewards;
        areas=new Native950CombatAreas(access::worldGraphic);
        conjures=new Native950Conjures(new Native950Conjures.Host(){
            public NPC spawn(Player p,Native950Conjures.Kind k){return access.spawnConjure(p,k.npc);}
            public void remove(NPC n){access.removeConjure(n);}
            public void follow(NPC n,WorldTile tile){access.followConjureTarget(n,tile);}
            public void followOwner(NPC n,Player p){access.followConjure(n,p);}
            public boolean valid(Player p){return access.player(p)&&Native950Conjures.ownerLifecycleValid(p);}
            public boolean conduit(Player p){return com.rs.cache.Cache.STORE!=null&&matchingOffhand(p,3);}
            public NPC target(Player p){Entity target=combatTarget(p);return target instanceof NPC?(NPC)target:null;}
            public boolean reach(NPC actor,NPC target,int range){return distanceToFootprint(actor,target,target.getSize())>0&&distanceToFootprint(actor,target,target.getSize())<=range&&access.bounceReach(actor,target);}
            public int abilityDamage(Player p){return conjureAbilityDamage(p);}
            public int strike(Player p,NPC target,int min,int max){return conjureStrike(p,target,min,max);}
            public void phantomCommand(Player p,NPC target,int min,int max){
                // CS13240: primary plus at most four nearby enemies, all attributed to this owner.
                conjureStrike(p,target,min,max);int remaining=4;
                java.util.List<Fighter> ordered=new ArrayList<>(fighters.values());ordered.sort((a,b)->Integer.compare(a.npc.getIndex(),b.npc.getIndex()));
                for(Fighter f:ordered)if(f.npc!=target&&secondaryEligible(p,f)
                        &&distanceBetweenFootprints(target,target.getSize(),f.npc,f.profile.size)<=1&&access.bounceReach(target,f.npc)){
                    conjureStrike(p,f.npc,min,max);if(--remaining==0)break;
                }
            }
            public void area(Player p,WorldTile origin,int radius,int min,int max){for(Fighter f:new ArrayList<>(fighters.values()))if(distanceToFootprint(origin,f.npc,f.profile.size)<=radius&&secondaryEligible(p,f)&&access.bounceReach(origin,f.npc))conjureStrike(p,f.npc,min,max);}
        });
    }
    public void attach(Player player) {
        owned(); player.setNative950Combat(this);
        // A disconnect during the visible death stage may checkpoint HP0. Resume the safe
        // return on this world; never call the legacy death/instance machinery on login.
        if (player.isDead()) deadPlayers.put(player,tick+1);
    }
    public void detach(Player player) {
        conjures.clear(player);necromancy.clear(player);ceaseUntil.remove(player);dashImpacts.remove(player);skullFlights.removeIf(f->f.owner==player);
        owned();stop(player);deadPlayers.remove(player);nextAttack.remove(player);player.setNative950Combat(null);
        globalCooldown.remove(player);abilityCooldowns.remove(player);damageOverTime.remove(player);buffs.remove(player,this::buffRemoved);pendingHits.remove(player);Native950AutoSpells.clear(player);
        player.setDevelopmentGodMode(false);
        player.setInfiniteRunEnergy(false);
        player.setInfiniteCombatRunes(false);
        player.setInfiniteAmmunition(false);
        player.getPrayer().setInfinitePrayer(false);
        player.getCombatDefinitions().setInfiniteAdrenaline(false);
    }
    public void register(NPC npc) {
        owned();
        Native950NpcCombatCatalog.Resolution resolution=Native950NpcCombatCatalog.inspectRunningCache(npc.getId());
        Native950NpcCombatProfile profile=resolution.profile;
        if(profile==null) {
            if(!"no Attack option".equals(resolution.reason) && !unavailableDefinitions.containsKey(npc.getId())) {
                unavailableDefinitions.put(npc.getId(),resolution.reason);
                System.out.println("[Ataraxia950] NPC combat unavailable id="+npc.getId()+": "+resolution.reason);
            }
            return;
        }
        if(profile!=null) {
            if(!Native950CombatAnimations.verifyCache() || !Native950Hits.verifyCache())
                throw new IllegalStateException("Native melee requires verified 950 display definitions");
            register(npc,profile);
        }
    }
    void register(NPC npc, Native950NpcCombatProfile profile) {
        owned();
        if(fighters.containsKey(npc)) return;
        // A few legacy rows land on scenery in950. Keep their presentation, but never
        // allow a combat encounter whose eventual respawn would be inside blocked scenery.
        if(!npc.isNative950DiagnosticDefinition()&&!access.clear(npc,profile.size)) return;
        npc.setNative950CombatProfile(profile);
        access.activate(npc);
        Fighter fighter=new Fighter(npc,profile);
        fighter.respawnAllowed=!npc.isNative950DiagnosticDefinition()&&profile.respawnTicks>0;
        fighters.put(npc,fighter);
    }
    void setDiagnosticRepeat(NPC npc,boolean repeat) {
        owned();Fighter fighter=fighters.get(npc);
        if(fighter!=null)fighter.respawnAllowed=repeat;
    }
    public boolean supports(NPC npc) { owned();return fighters.containsKey(npc); }
    void registerTraining(NPC npc) {
        owned();
        if(npc.getId()!=16027||!npc.isNative950DiagnosticDefinition())throw new IllegalArgumentException("Expected training dummy");
        unregister(npc);
        register(npc,new Native950NpcCombatProfile(16027,1,1,100000,1,1,0,100,1,1,-1,-1,-1,0,0));
        Fighter fighter=fighters.get(npc);
        if(fighter==null)throw new IllegalStateException("Training tile is blocked");
        fighter.training=true;
    }
    void unregister(NPC npc) {
        owned();Fighter fighter=fighters.get(npc);
        if(fighter!=null)for(Player player:new ArrayList<>(targets.keySet()))
            if(targets.get(player)==fighter)stop(player);
        fighters.remove(npc);
    }
    public String attack(Player player, NPC npc) {
        Native950BugTest.event(player,"combat","attack-request","npc",npc==null?-1:npc.getId(),"index",npc==null?-1:npc.getIndex());
        owned();
        Fighter fighter=fighters.get(npc);
        if(fighter==null) {
            String reason=npc==null?null:unavailableDefinitions.get(npc.getId());
            if("non-melee attack style is not ported".equals(reason))
                return "That creature's fighting style is not supported yet.";
            return "Combat is not available for that creature yet.";
        }
        if(!available(player,npc) || npc.isDead()) return "You cannot attack that creature right now.";
        String dungeonRefusal=Native950Dungeoneering.attackRefusal(player,npc);
        if(dungeonRefusal!=null)return dungeonRefusal;
        String slayerRefusal=Native950Slayer.attackRefusal(player,npc);
        if(slayerRefusal!=null)return slayerRefusal;
        if(distanceToFootprint(player,npc,fighter.profile.size)>LEASH || distanceToFootprint(player,fighter.home,fighter.profile.size)>LEASH)
            return "Move closer to that creature.";
        if(fighter.returning) return "That creature is returning home.";
        ceaseUntil.remove(player);
        try { Loadout initial=loadouts.get(player);if(initial.profile!=null){String cost=initial.profile.costRefusal(player);if(cost!=null)return cost;} } catch(IllegalArgumentException unsupported) { return unsupported.getMessage(); }
        if(targets.get(player)==fighter) {
            if(fighter.target==player){fighter.attacking=true;fighter.outOfSupplies=false;fighter.approachTicks=0;}
            player.resetWalkSteps();player.setRouteEvent(null);
            player.setTarget(npc);player.setAttackingDelay(Utils.currentTimeMillis()+6000);
            return null; // Clicking again resumes the action without resetting either swing timer.
        }
        // Switching the active target does not pacify NPCs already retaliating.
        Fighter previous=targets.remove(player);
        if(previous!=null&&previous.target==player)previous.attacking=false;
        clearQueuedAbility(player);channelUntil.remove(player);pendingHits.remove(player);damageOverTime.remove(player);
        player.getActionManager().forceStop();
        boolean shared=fighter.target!=null&&fighter.target!=player;
        targets.put(player,fighter);
        // NPCs retain one retaliation/follow target, but player damage ownership is
        // intentionally multi everywhere.  A second player never steals the NPC's
        // existing target or interrupts that player's attack cadence.
        if(!shared)fighter.target=player;
        if(!shared){fighter.attacking=true;fighter.outOfSupplies=false;fighter.approachTicks=0;}
        npc.resetWalkSteps();npc.setNative950CombatEngaged(true);
        player.setRouteEvent(null);player.resetWalkSteps();
        player.setNextFaceEntity(npc);if(!shared)npc.setNextFaceEntity(player);
        player.setTarget(npc);player.setAttackingDelay(Utils.currentTimeMillis()+6000);
        player.setAttackedBy(npc);if(!shared)npc.setAttackedBy(player);
        System.out.println("[Ataraxia950] Melee target player="+player.getIndex()+" npc="+npc.getIndex()+" id="+npc.getId());
        return null;
    }
    /** Like stopping PlayerCombat in910: cancel the player's action, not NPCCombat.target. */
    public void cancelAttack(Player player) {
        owned();clearQueuedAbility(player);pendingHits.remove(player);damageOverTime.remove(player);channelUntil.remove(player);Fighter fighter=targets.get(player);
        if(fighter==null)return;
        if(fighter.target==player){fighter.attacking=false;fighter.approachTicks=0;}
        player.resetWalkSteps();player.setNextFaceEntity(null);
        if(fighter.target!=player||!fighter.retaliating)stop(player);
    }
    /** Logout, death, teleport or a leash break retires both combat owners. */
    public void stop(Player player) {
        owned();stopCurrent(player);
        for(Fighter fighter:new ArrayList<>(fighters.values()))if(fighter.target==player)releaseRetaliation(fighter,player);
    }
    private void stopCurrent(Player player){
        owned();
        clearQueuedAbility(player);
        channelUntil.remove(player);
        pendingHits.remove(player);damageOverTime.remove(player);
        Fighter fighter=targets.remove(player);
        if(fighter==null)return;
        player.resetWalkSteps();player.setNextFaceEntity(null);player.setAttackedBy(null);
        if(player.getTarget()==fighter.npc)player.setTarget(null);
        damageOverTime.remove(player);pendingHits.remove(player);
        releaseRetaliation(fighter,player);
    }
    private void releaseRetaliation(Fighter fighter,Player player){
        if(fighter.target==player){
            Player replacement=null;
            for(Map.Entry<Player,Fighter> entry:targets.entrySet())if(entry.getValue()==fighter&&entry.getKey()!=player
                    &&(replacement==null||entry.getKey().getIndex()<replacement.getIndex()))replacement=entry.getKey();
            if(replacement!=null){
                fighter.target=replacement;fighter.attacking=true;fighter.outOfSupplies=false;fighter.approachTicks=0;
                fighter.npc.setNextFaceEntity(replacement);replacement.setNextFaceEntity(fighter.npc);
                return;
            }
            fighter.target=null;fighter.attacking=false;fighter.retaliating=false;fighter.outOfSupplies=false;fighter.approachTicks=0;fighter.followFailures=0;
            fighter.strikes.clear();
            fighter.npc.resetWalkSteps();fighter.npc.setNextFaceEntity(null);fighter.npc.setAttackedBy(null);
            fighter.returning=!fighter.npc.isDead() && distance(fighter.npc,fighter.home)>0;
            fighter.npc.setNative950CombatEngaged(fighter.returning);
        }
    }
    public void clear() {
        owned();for(Player player:new ArrayList<>(targets.keySet()))stop(player);
        fighters.clear();unavailableDefinitions.clear();nextAttack.clear();deadPlayers.clear();
        for(Player player:new ArrayList<>(queuedAbilities.keySet()))clearQueuedAbility(player);
        globalCooldown.clear();channelUntil.clear();abilityCooldowns.clear();damageOverTime.clear();
        buffs.clear(this::buffRemoved);
        conjures.clear();necromancy.clear();ceaseUntil.clear();dashImpacts.clear();skullFlights.clear();
        pendingHits.clear();
    }
    private static final int[] MOVEMENT_COOLDOWNS={14726,14665,47129,1488};
    private static boolean developerMovement(Player p,int structure){
        if(!p.isDevelopmentAlmighty())return false;
        for(int id:MOVEMENT_COOLDOWNS)if(id==structure)return true;
        return false;
    }
    /** Clear linked Dive state and both native timer endpoints on either mode transition. */
    public void developerModeChanged(Player player){
        owned();
        Map<Integer,Long> cooldowns=abilityCooldowns.get(player);
        for(int structure:MOVEMENT_COOLDOWNS){
            if(cooldowns!=null)cooldowns.remove(structure);
            if(player.getRealChannel()!=null)player.getNative950ActionBar().cooldown(
                    player.getRealChannel(),structure,(int)Utils.currentWorldCycle(),0);
        }
    }
    void publishMovementCooldown(Player player,int structure){
        int duration=developerMovement(player,structure)?0:34;
        Map<Integer,Long> cooldowns=abilityCooldowns.computeIfAbsent(player,p->new java.util.HashMap<>());
        if(duration==0)cooldowns.remove(structure);else cooldowns.put(structure,tick+duration);
        player.getNative950ActionBar().cooldown(player.getRealChannel(),structure,(int)Utils.currentWorldCycle(),duration);
    }
    void refreshBarCooldowns(Player player,int[] slots) {
        owned();
        player.getNative950ActionBar().queueVisual(player,queuedAbilities.getOrDefault(player,-1));
        Map<Integer,Long> cooldowns=abilityCooldowns.get(player);
        if(cooldowns==null||cooldowns.isEmpty())return;
        int cycle=(int)Utils.currentWorldCycle();
        java.util.Set<Integer> sent=new java.util.HashSet<>();
        for(int packed:slots){
            int structure=resolveAbility(player,Native950ActionBar.struct(packed));
            if(structure<0||!sent.add(structure))continue;
            long remaining=abilityCooldownEnd(player,structure)-tick;
            if(remaining>0)player.getNative950ActionBar().refreshCooldown(player.getRealChannel(),structure,cycle,
                    (int)Math.min(Integer.MAX_VALUE,remaining));
        }
    }
    /** A deliberately small native basic-ability slice; legacy ability callbacks never run. */
    public String ability(Player player,int structure) {
        owned();
        structure=resolveAbility(player,structure);
        Native950BugTest.event(player,"combat","ability-request","structure",structure);
        if(structure==44225){
            for(int slot=0;slot<28;slot++){
                Item item=player.getInventory().getItem(slot);
                if(item==null||!Native950Food.supports(item.getId()))continue;
                Native950Food.Result result=Native950Food.eat(player,Native950Skilling.containers(player),slot,item.getId());
                if(!result.accepted)return result.reason;
                player.getNative950ActionBar().cooldown(player.getRealChannel(),structure,(int)Utils.currentWorldCycle(),3);
                Native950BugTest.event(player,"combat","ability-executed","structure",structure,"name","Eat Food");
                return null;
            }
            return "You have no supported food in your backpack.";
        }
        if(structure==47129||structure==1488)return "Choose a destination using the native Dive targeting cursor.";
        if(structure==45340){
            if(!access.player(player)||!player.isActive()||player.hasFinished()||player.isDead())return "You cannot use an ability right now.";
            cancelAttack(player);ceaseUntil.put(player,tick+10);
            Native950BugTest.event(player,"combat","ability-executed","structure",structure,"name","Cease","endTick",tick+10);
            return null;
        }
        if(structure==14726||structure==14665){
            Map<Integer,Long> cooldowns=abilityCooldowns.get(player);
            if(tick<abilityCooldownEnd(player,structure)){
                Native950BugTest.event(player,"combat","ability-rejected","structure",structure,"reason","Surge is cooling down.",
                        "source","manual","cooldownEndTick",cooldowns.getOrDefault(structure,0L));
                return "Surge is cooling down.";
            }
            String refusal=Native950Surge.use(player,structure==14665);
            if(refusal==null){
                publishMovementCooldown(player,structure);
            } else {
                Native950BugTest.event(player,"combat","ability-rejected","structure",structure,"reason",refusal,"source","manual");
            }
            return refusal;
        }
        String refusal=abilityQueueRefusal(player,structure);
        if(refusal!=null){
            Native950BugTest.event(player,"combat","ability-rejected","structure",structure,"reason",refusal,"source","manual",
                    "globalCooldownEndTick",globalCooldown.getOrDefault(player,0L));
            return refusal;
        }
        long remaining=abilityCooldownEnd(player,structure)-tick;
        // A manual request owns one persistent queue slot until ready, replaced or cancelled.
        boolean waiting=tick<globalCooldown.getOrDefault(player,0L)||remaining>0
                ||tick<channelUntil.getOrDefault(player,0L);
        queueAbility(player,structure,waiting);
        Native950AbilityCatalog.Definition definition=Native950AbilityCatalog.get(structure);
        if(waiting)return definition.name+" queued.";
        Fighter fighter=targets.get(player);
        performAbility(player,isAttacking(player,fighter)?fighter:null);
        return null;
    }
    private int resolveAbility(Player p,int structure){return necromancy.effective(p,conjures.effective(p,structure));}
    static int abilityStyle(int structure){
        Native950AbilityCatalog.Definition definition=Native950AbilityCatalog.get(structure);
        return definition==null?-1:definition.style();
    }
    public String tileAbility(Player player,int structure,WorldTile target){
        owned();
        Native950AbilityCatalog.Definition definition=Native950AbilityCatalog.get(structure);
        if(definition==null||(structure!=47129&&structure!=1488))return "That ability does not target a ground tile.";
        if(!access.player(player))return "You cannot use an ability right now.";
        if(player.getSkills().getLevel(definition.skill)<definition.level)return definition.name+" requires level "+definition.level+".";
        Loadout gear=null;
        if(structure==1488){
            try{gear=loadouts.get(player);}catch(IllegalArgumentException invalid){return invalid.getMessage();}
            if(gear.profile!=null&&gear.profile.style!=0||!matchingOffhand(player,0))return "Bladed Dive requires matching melee weapons.";
        }
        if(tick<abilityCooldownEnd(player,47129)||tick<abilityCooldownEnd(player,1488))return "Dive is cooling down.";
        String refusal=Native950Surge.dive(player,target);
        if(refusal!=null)return refusal;
        cancelAttack(player);
        if(structure==1488)dashImpacts.put(player,new DashImpact(new WorldTile(player.getNextForceMovement().getToSecondTile()),gear,tick+4));
        for(int linked:new int[]{47129,1488}){
            publishMovementCooldown(player,linked);
        }
        Native950BugTest.event(player,"combat","tile-ability-executed","structure",structure,"x",target.getX(),"y",target.getY());
        return null;
    }
    int revolutionCandidate(Player player,int slots){
        owned();
        if(tick<ceaseUntil.getOrDefault(player,0L))return -1;
        int candidate=player.getNative950ActionBar().revolutionCandidate(slots,id->abilityRefusal(player,resolveAbility(player,id))==null);
        return resolveAbility(player,candidate);
    }
    /** Native combat has no legacy PlayerCombat action, so expose its live target explicitly. */
    public Entity combatTarget(Player player){
        owned();Fighter fighter=targets.get(player);
        return isAttacking(player,fighter)&&!fighter.npc.isDead()?fighter.npc:null;
    }
    /** The NPC's attacking flag belongs only to its retaliation owner. */
    private boolean isAttacking(Player player,Fighter fighter){
        return fighter!=null&&targets.get(player)==fighter&&(fighter.target!=player||fighter.attacking);
    }
    String abilityRefusal(Player player,int structure) {
        return abilityRefusal(player,structure,true);
    }
    /** Temporal gates may delay a request; all gameplay requirements still apply. */
    private String abilityQueueRefusal(Player player,int structure) {
        return abilityRefusal(player,structure,false);
    }
    private String abilityRefusal(Player player,int structure,boolean requireGlobalCooldown) {
        return abilityRefusal(player,structure,requireGlobalCooldown,true);
    }
    private String abilityRefusal(Player player,int structure,boolean requireGlobalCooldown,boolean resources) {
        Native950AbilityCatalog.Definition definition=Native950AbilityCatalog.get(structure);
        if(definition==null)return "That ability is not available in Combat Alpha yet.";
        int style=definition.style();
        if(!access.player(player)||!player.isActive()||player.hasFinished()||player.isDead()||player.isLocked()
                ||(player.isStunned()&&structure!=14711)||player.isNative950ForceMovementActive()||player.getNextWorldTile()!=null)
            return "You cannot use an ability right now.";
        Fighter fighter=targets.get(player);
        if(definition.targetRequired()&&(!isAttacking(player,fighter)||!available(player,fighter.npc)||fighter.npc.isDead()))
            return "Attack a supported NPC or training dummy first.";
        if(requireGlobalCooldown&&tick<globalCooldown.getOrDefault(player,0L))return "Abilities are on global cooldown.";
        if(requireGlobalCooldown&&tick<channelUntil.getOrDefault(player,0L))return "An ability is still channelling.";
        if(requireGlobalCooldown&&tick<abilityCooldownEnd(player,structure))return "That ability is cooling down.";
        Loadout gear;
        try{gear=loadouts.get(player);}catch(IllegalArgumentException e){return e.getMessage();}
        if(style>=0&&(gear.profile==null?0:gear.profile.style)!=style)return "Equip a weapon matching that ability's combat style.";
        if(definition.shieldRequired()&&!hasNativeShield(player))return definition.name+" requires a shield.";
        String conjureRefusal=conjures.refusal(player,structure);
        if(conjureRefusal!=null)return conjureRefusal;
        String resourceRefusal=necromancy.refusal(player,structure);
        if(resources&&resourceRefusal!=null)return resourceRefusal;
        int energy=structure==48297?necromancy.fingerCost(player):structure==48314?skullsCost(player):definition.adrenalineRequired();
        if(definition.tier==3&&buffs.active(player,Native950CombatBuffs.Type.LIMITLESS,tick))energy=definition.adrenalineCost();
        if(resources&&energy>0&&!player.getCombatDefinitions().isInfiniteAdrenaline()
                &&player.getCombatDefinitions().getSpecialAttackPercentage()<energy)
            return definition.name+" requires "+energy+"% adrenaline.";
        if(definition.offhandRequired&&!matchingOffhand(player,style))return definition.name+" requires a matching off-hand weapon.";
        if(definition.twoHandedRequired&&!player.getEquipment().hasTwoHandedWeapon())return definition.name+" requires a two-handed weapon.";
        int skill=definition.skill;
        if(player.getSkills().getLevel(skill)<definition.level)
            return "You need level "+definition.level+" in the matching combat skill.";
        if(player.getFoodDelay()>Utils.currentTimeMillis())return "Wait until you have finished eating.";
        if(!definition.targetRequired())return null;
        if(((structure>=48311&&structure<=48313)||structure==48309)&&distanceToFootprint(player,fighter.npc,fighter.profile.size)>2)
            return definition.name+" requires a target within two tiles.";
        if(!playerReach(player,fighter.npc,gear))return "Move within attack range first.";
        String refusal=Native950Slayer.attackRefusal(player,fighter.npc);
        if(refusal==null)refusal=Native950Dungeoneering.attackRefusal(player,fighter.npc);
        return refusal!=null?refusal:!resources||gear.profile==null?null:gear.profile.costRefusal(player);
    }
    private long abilityCooldownEnd(Player player,int structure){
        Map<Integer,Long> cooldowns=abilityCooldowns.get(player);
        return developerMovement(player,structure)||cooldowns==null?0:cooldowns.getOrDefault(structure,0L);
    }
    /** Opt-in diagnostic: the real manual queue waits twelve ticks, then executes normally. */
    String holdQueueForVisualCheck(Player player,int slot){
        owned();
        if(!Native950BugTest.enabled(player))return "Enable ;;bugtest first.";
        if(slot<1||slot>14)return "Use ;;queuehold <slot 1-14>.";
        Native950ActionBar bar=player.getNative950ActionBar();
        int structure=Native950ActionBar.struct(bar.slot(bar.activeBar(),slot-1));
        String refusal=abilityQueueRefusal(player,structure);
        if(refusal!=null)return refusal;
        globalCooldown.put(player,Math.max(globalCooldown.getOrDefault(player,0L),tick+12));
        queueAbility(player,structure,true);
        Native950BugTest.event(player,"combat","queue-diagnostic-hold","structure",structure,"until",tick+12);
        return "Queued slot "+slot+" for a 7.2-second visual check; normal validation and cancellation remain active.";
    }
    private void queueAbility(Player player,int structure,boolean waitingForGlobalCooldown) {
        Integer replaced=queuedAbilities.put(player,structure);
        player.getNative950ActionBar().queueVisual(player,waitingForGlobalCooldown?structure:-1);
        Native950AbilityCatalog.Definition definition=Native950AbilityCatalog.get(structure);
        Native950BugTest.event(player,"combat",replaced==null?"ability-queued":"ability-queue-replaced",
                "structure",structure,"name",definition==null?"unknown":definition.name,
                "replaced",replaced==null?"none":replaced,"waitingForGlobalCooldown",waitingForGlobalCooldown,
                "globalCooldownEndTick",globalCooldown.getOrDefault(player,0L),
                "channelEndTick",channelUntil.getOrDefault(player,0L),
                "waitingForAnimation",player.getLastAnimationEnd()>Utils.currentTimeMillis(),
                "animationEndMillis",player.getLastAnimationEnd());
    }
    private void clearQueuedAbility(Player player){
        queuedAbilities.remove(player);
        player.getNative950ActionBar().queueVisual(player,-1);
    }
    private boolean performAbility(Player player,Fighter fighter) {
        Integer structure=queuedAbilities.get(player);
        if(structure==null)return false;
        return performAbility(player,fighter,structure,true);
    }
    private boolean performAbility(Player player,Fighter fighter,int structure,boolean manualQueue) {
        String refusal=abilityRefusal(player,structure,false,false);
        if(refusal==null&&(tick<globalCooldown.getOrDefault(player,0L)||tick<abilityCooldownEnd(player,structure)
                ||tick<channelUntil.getOrDefault(player,0L)))return false;
        if(refusal==null)refusal=abilityQueueRefusal(player,structure);
        if(refusal!=null){
            if(manualQueue){
                clearQueuedAbility(player);
                Native950BugTest.event(player,"combat","ability-queue-cancelled","structure",structure,"reason",refusal);
                player.sendMessage(refusal);
            }
            return false;
        }
        if(manualQueue)clearQueuedAbility(player);
        Loadout gear=loadouts.get(player);int style=abilityStyle(structure);
        if(style<0)style=gear.profile==null?0:gear.profile.style;
        Native950AbilityCatalog.Definition definition=Native950AbilityCatalog.get(structure);
        int adrenalineCost=structure==48297?necromancy.fingerCost(player):structure==48314?skullsCost(player):definition.adrenalineCost();
        int hitCount=structure==48301?necromancy.souls(player):definition.hits;
        boolean livingDeath=buffs.active(player,Native950CombatBuffs.Type.LIVING_DEATH,tick);
        // Native ability input can arrive while the actor still faces a prior tile.
        // Auto attacks already set this mask; abilities must do the same before their
        // animation/projectile is published so ranged and magic do not cast sideways.
        if(definition.targetRequired())player.setNextFaceEntity(fighter.npc);
        if(definition.targetRequired()&&gear.profile!=null&&!gear.profile.consume(player)){
            Native950BugTest.event(player,"combat","ability-queue-cancelled","structure",structure,
                    "reason","You cannot supply that ability's ammunition or runes.");
            player.sendMessage("You cannot supply that ability's ammunition or runes.");return false;
        }
        ceaseUntil.remove(player);
        necromancy.cast(player,structure,tick,livingDeath);
        if(Native950Conjures.handles(structure))conjures.cast(player,structure,tick);
        globalCooldown.put(player,tick+3);
        int cooldown=structure==48314&&livingDeath?17:definition.cooldown;
        abilityCooldowns.computeIfAbsent(player,p->new java.util.HashMap<>()).put(structure,tick+cooldown);
        int cycle=(int)Utils.currentWorldCycle();
        player.getNative950ActionBar().cooldown(player.getRealChannel(),structure,cycle,cooldown);
        Native950BugTest.event(player,"combat","cooldown","structure",structure,"cycle",cycle,"duration",cooldown);
        player.getNative950ActionBar().cooldown(player.getRealChannel(),14881,cycle,3);
        nextAttack.put(player,tick+3);
        int skill=style==0?Skills.STRENGTH:style==1?Skills.RANGE:style==Native950CombatStyles.NECROMANCY?Skills.NECROMANCY:Skills.MAGIC;
        int level=Rs2CombatFormula.effectiveLevel(player.getSkills().getLevel(skill),0,0,1);
        int maximum=Rs2CombatFormula.meleeOrRangedMaxHit(level,gear.strengthBonus,1);
        if(gear.profile!=null)maximum=gear.profile.maxHit(player,maximum);
        // Alpha coefficients are explicit approximations, not values decoded from
        // the ability cache. The cache verifies identity, requirements and cadence.
        int percent=definition.minPercent+rolls.damage(Math.max(0,definition.maxPercent-definition.minPercent));
        if(structure==48297&&livingDeath)percent=percent*150/100;
        boolean roar=definition.book==1&&definition.targetRequired()&&buffs.consume(player,Native950CombatBuffs.Type.CHAOS_ROAR,tick);
        if(roar)percent*=2;
        if(definition.effect==Native950AbilityCatalog.Effect.EXECUTE
                && fighter.npc.getHitpoints()*2<=fighter.profile.hp)percent+=20;
        Native950AbilityCatalog.AnimationResolution resolution=Native950AbilityCatalog.animationResolution(player,structure);
        int animation=resolution.id;
        int projectile=Native950AbilityCatalog.projectileGraphic(structure,animation);
        int animationTicks=Native950AbilityCatalog.animationTicks(animation);
        if(definition.channelled())channelUntil.put(player,tick+definition.channelTicks());
        java.util.List<Long> followUps=new java.util.ArrayList<Long>();
        if(definition.effect==Native950AbilityCatalog.Effect.BUFF){
            Native950CombatBuffs.Type type=Native950CombatBuffs.Type.forStructure(structure);
            if(type==null)throw new IllegalStateException("Missing ability effect "+structure);
            int duration=type==Native950CombatBuffs.Type.BARRICADE?8+shieldLevel(player)/10:type.duration;
            buffs.apply(player,type,tick,duration,null);
            if(type==Native950CombatBuffs.Type.SUNSHINE)areas.start(player,3856);
            if(type==Native950CombatBuffs.Type.DEATHS_SWIFTNESS)areas.start(player,8996);
            if(type==Native950CombatBuffs.Type.LIVING_DEATH)player.getAppearence().setNative950LivingDeath(true);
            if(type==Native950CombatBuffs.Type.LIVING_DEATH)for(int reset:new int[]{48296,48314}){
                abilityCooldowns.get(player).remove(reset);
                player.getNative950ActionBar().cooldown(player.getRealChannel(),reset,cycle,0);
            }
            if(type==Native950CombatBuffs.Type.RESONANCE||type==Native950CombatBuffs.Type.DIVERT){
                Native950CombatBuffs.Type other=type==Native950CombatBuffs.Type.RESONANCE?Native950CombatBuffs.Type.DIVERT:Native950CombatBuffs.Type.RESONANCE;
                buffs.consume(player,other,tick);
                abilityCooldowns.get(player).put(other.structure,tick+definition.cooldown);
                player.getNative950ActionBar().cooldown(player.getRealChannel(),other.structure,cycle,definition.cooldown);
            }
            if(type==Native950CombatBuffs.Type.FREEDOM){player.setStunDelay(0);player.setFreezeDelay(-1);}
            if(type==Native950CombatBuffs.Type.REJUVENATE){
                player.heal(Math.max(1,player.getMaxHitpoints()/40));
                for(int stat=0;stat<Skills.SKILL_NAME.length;stat++)if(player.getSkills().getLevel(stat)<player.getSkills().getLevelForXp(stat))
                    player.getSkills().set(stat,player.getSkills().getLevelForXp(stat));
            }

            Native950BugTest.event(player,"combat","effect-started","effect",definition.name,"durationTicks",duration,"endTick",tick+duration);
        }
        if(structure==52799){
            buffs.apply(player,Native950CombatBuffs.Type.SEARING_WINDS,tick);
            buffs.setBonusDamage(player,Native950CombatBuffs.Type.SEARING_WINDS,nativeDamage(Math.max(1,maximum*20/100)));
        }
        if(structure==28177)buffs.extend(player,Native950CombatBuffs.Type.SHADOW_IMBUED,tick,6);
        int total=0;
        if(structure==48314){
            int skullDamage=nativeDamage(Math.max(1,maximum*percent/100));
            startDeathSkulls(player,fighter.npc,gear,skullDamage,skullsRange(player),igneousNecromancyCape(player)?6:4,projectile);
        }
        SiphonState siphon=structure==48309?new SiphonState():null;
        for(int hit=0;hit<(structure==48314||definition.effect==Native950AbilityCatalog.Effect.BUFF||definition.effect==Native950AbilityCatalog.Effect.PROVOKE?0:hitCount);hit++){
            if(siphon!=null)percent=hit==4?117+rolls.damage(26):22+rolls.damage(6); // Exact950 CS18670.
            int rolled=Math.max(1,maximum*percent/100);
            int requested=Rs2CombatFormula.scaleNative950Damage(rolled,rolls.nativeDamageRemainder(rolled));
            // CS18662 describes one non-channelled volley, one hit per soul: a shared flight/impact, not serial casts.
            int hitDelay=structure==48301?0:definition.hitDelay(hit);
            if(hitDelay>0||projectile>=0){
                long dueTick=tick+hitDelay;
                PendingHit pending=new PendingHit(fighter,requested,dueTick,gear,Native950AbilityCatalog.targetGraphic(structure,animation),
                        definition.channelled()?structure:-1,player,structure);
                pending.projectileGraphic=projectile;
                pending.baseAbilityDamage=nativeDamage(maximum);
                pending.siphon=siphon;pending.siphonFinisher=siphon!=null&&hit==4;
                if(hitDelay==0)releaseAbilityProjectile(player,pending);
                pendingHits.computeIfAbsent(player,p->new ArrayList<>()).add(pending);
                followUps.add(dueTick);
                continue;
            }
            int actual=damage(player,fighter.npc,structure==48313?scytheFinisher(requested,fighter):requested,gear.profile==null?Hit.HitLook.MELEE_DAMAGE:gear.profile.look(),false,
                    definition.effect!=Native950AbilityCatalog.Effect.BLEED);
            total+=actual;
            abilityImpact(player,fighter,structure,requested,actual,gear,nativeDamage(maximum));
            if(siphon!=null)siphonPulse(player,fighter,requested,actual,gear,siphon);
            if(player.isDead()||fighter.npc.isDead())break;
        }
        if(player.isDead()){
            // Tendril recoil can end the cast at its first impact. Do not enqueue
            // more channel hits or replace the death animation with the cast animation.
            if(fighter!=null){
                if(total>0&&!fighter.training)rewards.hit(player,fighter.npc,total,gear);
                if(fighter.training)fighter.npc.setHitpoints(fighter.profile.hp);
                else if(fighter.npc.isDead())npcDied(fighter,player);
            }
            swings++;return true;
        }
        if(structure==14712&&fighter!=null){
            boolean priorOwnerAttacking=fighter.target==null||fighter.attacking;
            // A paused former retaliation owner must not become an active secondary attacker.
            if(!priorOwnerAttacking&&fighter.target!=player)stop(fighter.target);
            fighter.target=player;fighter.attacking=true;fighter.outOfSupplies=false;
            fighter.npc.setNextFaceEntity(player);fighter.npc.setAttackedBy(player);
        }
        // Param2802 is an icon sprite. Actual sequences come from param2915's weapon-family enum.
        int effect=Native950AbilityCatalog.casterGraphic(structure,animation);
        if(animation>=0){
            player.setNextAnimation(new Animation(animation));
        }
        if(effect>=0)player.setNextGraphics(new com.rs.game.Graphics(effect));
        int targetGraphic=Native950AbilityCatalog.targetGraphic(structure,animation);
        if(targetGraphic>0&&fighter!=null&&projectile<0&&hitCount>0&&definition.hitDelay(0)==0)
            fighter.npc.setNextGraphics(new com.rs.game.Graphics(targetGraphic));
        String presentationEvidence=Native950AbilityCatalog.presentationEvidence(structure,animation,effect,targetGraphic);
        Native950BugTest.event(player,"combat","ability-executed","name",definition.name,"structure",structure,
                "target",definition.targetRequired()?fighter.npc.getId()+":"+fighter.npc.getIndex():"self","animation",animation<0?"none":animation,
                "animationResolution",resolution.source,"animationLockEndTick",tick+animationTicks,
                "animationLockEndMillis",player.getLastAnimationEnd(),"graphic",effect<0?"none":effect,
                "targetGraphic",targetGraphic>0?targetGraphic:"none","presentationEvidence",presentationEvidence,
                "cooldownDuration",definition.cooldown,
                "gcdEndTick",tick+3,"channelEndTick",channelUntil.getOrDefault(player,0L),
                "hitTimingSource","effect-cadence","firstHitTick",definition.hits==0?"none":tick+definition.hitDelay(0),
                "followUpHitTicks",followUps.toString());
        if(adrenalineCost>0)player.getCombatDefinitions().decreaseSpecialAttack(adrenalineCost);
        else if(definition.adrenalineGain()>0)player.getCombatDefinitions().setSpecialAttackPercentage(
                Math.min(100,player.getCombatDefinitions().getSpecialAttackPercentage()+buffs.adrenalineGain(player,
                        definition.adrenalineGain()+(structure==48296&&livingDeath?6:0),tick)));
        if(definition.targetRequired()){
        fighter.retaliating=!fighter.training;
        if(definition.effect==Native950AbilityCatalog.Effect.BLEED&&!fighter.npc.isDead()){
            int rawDotDamage=Math.max(1,maximum*(roar?30:15)/100);
            int dotDamage=Rs2CombatFormula.scaleNative950Damage(rawDotDamage,rolls.nativeDamageRemainder(rawDotDamage));
            dotDamage=prayerAdjustedDamage(player,fighter.npc,dotDamage,gear.profile==null?Hit.HitLook.MELEE_DAMAGE:gear.profile.look());
            damageOverTime.computeIfAbsent(player,p->new java.util.HashMap<>()).put(structure,
                    new DamageOverTime(fighter,dotDamage,3,tick+2,gear));
        }
        fighter.npc.resetWalkSteps();
        if(total>0&&!fighter.training)rewards.hit(player,fighter.npc,total,gear);
        if(fighter.training)fighter.npc.setHitpoints(fighter.profile.hp);
        else if(fighter.npc.isDead())npcDied(fighter,player);
        }
        swings++;
        return true;
    }
    /** Combat membership includes attackers and owned flights, not just the selected action target. */
    boolean hasCombatEngagement(Player p){
        if(!access.player(p)||p.hasFinished()||p.isDead()||p.hasLifecycleTeleport())return false;
        // Existing shared combat stance includes incoming attacks and the post-hit combat window.
        // Stopping one's own attack is not the same as leaving combat.
        if(p.isUnderCombat())return true;
        Fighter selected=targets.get(p);
        if(selected!=null&&engagementNpc(p,selected.npc)&&!selected.npc.isDead()&&!selected.returning
                &&isAttacking(p,selected))return true;
        for(Fighter f:fighters.values())if(f.target==p&&f.retaliating&&!f.returning&&!f.npc.isDead()
                &&engagementNpc(p,f.npc)&&distance(p,f.npc)<=LEASH)return true;
        java.util.List<PendingHit> flights=pendingHits.get(p);
        if(flights!=null)for(PendingHit hit:flights)if(engagementNpc(p,hit.fighter.npc)&&!hit.fighter.npc.isDead())return true;
        for(SkullFlight flight:skullFlights)if(flight.owner==p&&flight.remaining>0)return true;
        Map<Integer,DamageOverTime> dots=damageOverTime.get(p);
        if(dots!=null)for(DamageOverTime dot:dots.values())if(engagementNpc(p,dot.fighter.npc)&&!dot.fighter.npc.isDead())return true;
        return false;
    }
    private boolean engagementNpc(Player p,NPC npc){
        return access.npc(npc)&&!npc.hasFinished()&&npc.getPlane()==p.getPlane();
    }
    /** Runs after input and before ordinary entity movement, on the same world tick. */
    public void beforeMovement() {
        owned();tick++;
        necromancy.pulse(tick,this::hasCombatEngagement);
        buffs.checkEquipment(Native950MeleeCombat::hasNativeShield,this::buffRemoved);
        buffs.pulse(tick);
        buffs.expire(tick,this::buffRemoved);
        Iterator<Map.Entry<Player,Long>> dead=deadPlayers.entrySet().iterator();
        while(dead.hasNext()) {
            Map.Entry<Player,Long> entry=dead.next();Player player=entry.getKey();
            try {
            if(!access.player(player)){dead.remove();continue;}
            if(tick<entry.getValue())continue;
            WorldTile tile=respawnTile();
            if(!access.clear(tile))continue; // Wait for collision; other encounters keep running.
            player.setHitpoints(player.getMaxHitpoints());player.refreshHitPoints();
            player.setNextAnimation(new Animation(-1));player.unlock();player.setNextWorldTile(tile);
            player.getPackets().sendGameMessage("You recover in Lumbridge. Your items have been kept.");
            dead.remove();
            } catch(RuntimeException failure) {
                System.err.println("[Ataraxia950] Melee player recovery failed player="+player.getIndex()+": "+failure);
                failure.printStackTrace();dead.remove();
                if(player.getRealChannel()!=null)player.getRealChannel().close();
            }
        }
        java.util.List<NPC> retiredDiagnostics=new ArrayList<>();
        Iterator<Fighter> iterator=fighters.values().iterator();
        while(iterator.hasNext()) {
            Fighter fighter=iterator.next();NPC npc=fighter.npc;
            try {
            if(!access.npc(npc)){if(fighter.target!=null)stop(fighter.target);iterator.remove();continue;}
            if(fighter.respawnAt>0) {
                if(tick>=fighter.hideAt)npc.setNative950DeathVisible(false);
                if(!fighter.respawnAllowed&&tick>=fighter.hideAt){iterator.remove();retiredDiagnostics.add(npc);continue;}
                if(tick>=fighter.respawnAt && access.clear(fighter.home,fighter.profile.size)) {
                    npc.setHitpoints(fighter.profile.hp);npc.resetReceivedHits();npc.resetReceivedDamage();npc.setNextAnimation(new Animation(-1));
                    npc.setNative950DeathVisible(false);npc.setNative950CombatEngaged(false);
                    npc.setNextWorldTile(new WorldTile(fighter.home));fighter.returning=false;fighter.respawnAt=0;fighter.hideAt=0;respawns++;
                    System.out.println("[Ataraxia950] Melee respawn npc="+npc.getIndex()+" id="+npc.getId());
                }
                continue;
            }
            if(fighter.returning) {
                if(distance(npc,fighter.home)==0) {fighter.returning=false;npc.setNative950CombatEngaged(false);}
                else if(access.follow(npc,fighter.home))fighter.followFailures=0;
                else if(++fighter.followFailures>APPROACH_TIMEOUT && access.clear(fighter.home,fighter.profile.size)) {
                    npc.setNextWorldTile(new WorldTile(fighter.home));fighter.returning=false;
                    npc.setNative950CombatEngaged(false);fighter.followFailures=0;
                }
                continue;
            }
            if(fighter.target==null&&Native950BossRules.aggressive(npc.getId())&&!npc.isDead()){
                Player nearest=null;int nearestDistance=8;
                for(Player candidate:access.players()){
                    if(candidate==null||!available(candidate,npc)||candidate.getNextWorldTile()!=null)continue;
                    int d=distanceToFootprint(candidate,npc,fighter.profile.size);
                    if(d>0&&d<nearestDistance&&access.npcRangedReach(npc,candidate,7)){
                        nearest=candidate;nearestDistance=d;
                    }
                }
                if(nearest!=null)engageRetaliation(fighter,nearest);
            }
            Player player=fighter.target;
            // The world restore task owns regeneration; do not heal a second time here.
            if(player==null) continue;
            if(!available(player,npc) || npc.isDead() || distanceToFootprint(player,fighter.home,fighter.profile.size)>LEASH
                    || distance(npc,fighter.home)>LEASH || distanceToFootprint(player,npc,fighter.profile.size)>LEASH
                    || player.getNextWorldTile()!=null) {if(targets.get(player)==fighter)stopCurrent(player);else releaseRetaliation(fighter,player);continue;}
            Loadout gear;
            try{gear=loadouts.get(player);}catch(IllegalArgumentException unsupported){player.sendMessage(unsupported.getMessage());stop(player);continue;}
            boolean npcInReach=npcReach(fighter,player),inReach=playerReach(player,npc,gear);
            if(npcInReach)npc.resetWalkSteps();
            if(inReach) {
                fighter.approachTicks=0;
                if(npcInReach){npc.resetWalkSteps();fighter.followFailures=0;}
                else if(fighter.retaliating&&tick>=fighter.stunnedUntil){if(access.follow(npc,player))fighter.followFailures=0;else if(++fighter.followFailures>APPROACH_TIMEOUT){if(targets.get(player)==fighter)stopCurrent(player);else releaseRetaliation(fighter,player);continue;}}
                if(fighter.attacking)player.resetWalkSteps();
            } else {
                // Same shared Entity.calcFollow path used by the ordinary910 NPCCombat.checkAll.
                if(fighter.retaliating&&!npcInReach&&tick>=fighter.stunnedUntil) {
                    if(access.follow(npc,player))fighter.followFailures=0;
                    else if(++fighter.followFailures>APPROACH_TIMEOUT){if(targets.get(player)==fighter)stopCurrent(player);else releaseRetaliation(fighter,player);continue;}
                }
                if(fighter.attacking && (++fighter.approachTicks>APPROACH_TIMEOUT || !access.approach(player,npc))) {
                    player.getPackets().sendGameMessage("You cannot reach that creature.");cancelAttack(player);
                }
            }
            } catch(RuntimeException failure) {failEncounter(fighter,failure);}
        }
        for(NPC npc:retiredDiagnostics)access.retire(npc);
    }
    /** Damage is committed only after every actor has moved, before any viewer frame. */
    public void afterMovement() {
        owned();
        processDashImpacts();
        processSkulls();
        conjures.pulse(tick);
        // Self buffs need the same queue/executor even when no encounter owns the player.
        if(!queuedAbilities.isEmpty())for(Player player:new ArrayList<>(queuedAbilities.keySet())){
            try{
            Native950AbilityCatalog.Definition queued=Native950AbilityCatalog.get(queuedAbilities.get(player));
            Fighter target=targets.get(player);
            if(queued!=null&&!queued.targetRequired()&&(target==null||!target.attacking))
                performAbility(player,null);
            }catch(RuntimeException failure){
                stop(player);
                System.err.println("[Ataraxia950] Native self ability failed player="+player.getIndex()+": "+failure);
                failure.printStackTrace();
                if(player.getRealChannel()!=null)player.getRealChannel().close();
            }
        }
        for(Fighter fighter:new ArrayList<>(fighters.values())) {
            try {
            Player player=fighter.target;NPC npc=fighter.npc;
            if(player==null || npc.isDead())continue;
            if(!available(player,npc) || player.hasTeleported() || player.getNextWorldTile()!=null
                    || distanceToFootprint(player,fighter.home,fighter.profile.size)>LEASH
                    || distance(npc,fighter.home)>LEASH){if(targets.get(player)==fighter)stopCurrent(player);else releaseRetaliation(fighter,player);continue;}
            if(targets.get(player)==fighter)processDamageOverTime(player,fighter);
            if(npc.isDead())continue;
            if(targets.get(player)==fighter)processPendingHits(player,fighter);
            if(npc.isDead())continue;
            processNpcStrikes(fighter,player);
            if(player.isDead()||fighter.target!=player)continue;
            Long channelEnd=channelUntil.get(player);
            if(channelEnd!=null&&tick>=channelEnd){
                channelUntil.remove(player);
                Native950BugTest.event(player,"combat","channel-completed","endTick",channelEnd,"completedTick",tick);
            }
            Loadout gear;
            try {gear=loadouts.get(player);}catch(IllegalArgumentException unsupported){player.getPackets().sendGameMessage(unsupported.getMessage());stop(player);continue;}
            String slayerRefusal=Native950Slayer.attackRefusal(player,npc);
            if(slayerRefusal!=null){player.sendMessage(slayerRefusal);stop(player);continue;}
            // Ability GCD/channel ownership is separate from the currently displayed
            // animation. Manual presses may replace its pose once they are ready.
            boolean abilityTurn=processAbilityTurn(player,fighter);
            if(player.getLastAnimationEnd()<=Utils.currentTimeMillis()&&tick>=channelUntil.getOrDefault(player,0L)){
            if(npc.isDead())continue;
            Long next=nextAttack.get(player);
            if(!abilityTurn && fighter.attacking && playerReach(player,npc,gear) && (next==null||tick>=next) && player.getFoodDelay()<=Utils.currentTimeMillis()) {
                autoAttack(player,fighter,gear);
                if(npc.isDead())continue;
            }
            }
            if(fighter.retaliating && tick>=fighter.stunnedUntil && npcReach(fighter,player) && tick>=fighter.nextAttack && !player.isDead()) {
                fighter.nextAttack=tick+fighter.profile.attackSpeed;
                npc.setNextFaceEntity(player);
                boolean shockwave=npc.getId()==6260&&rolls.damage(2)==0;
                int attackStyle=shockwave?1:fighter.profile.attackStyle;
                int attackAnimation=shockwave?17391:fighter.profile.attackAnim;
                // Normal-mode displayed maxima: 3000 melee / 1676 ranged. Engine
                // units are one tenth; fractional display remainder stays shared.
                int maximum=npc.getId()==6260?(shockwave?167:300):fighter.profile.maxHit;
                int attackBonus=shockwave?157:fighter.profile.meleeAttackBonus;
                if(attackAnimation>=0)npc.setNextAnimation(new Animation(attackAnimation));
                int defence=Rs2CombatFormula.effectiveLevel(player.getSkills().getLevel(Skills.DEFENCE)
                        +player.getPrayer().getStatBonuses(Skills.DEFENCE),0,0,1);
                int damage=rolls.accurate(Rs2CombatFormula.roll(Rs2CombatFormula.npcEffectiveLevel(fighter.profile.attackLevel),attackBonus),
                        Rs2CombatFormula.roll(defence,gear.defenceBonus))
                        ? Math.min(maximum,nativeDamage(rolls.damage(maximum/10))) : 0;
                if(attackStyle==0)damage(npc,player,damage);
                else {
                    int castGraphic=shockwave?3352:fighter.profile.attackGraphic;
                    if(castGraphic>=0)npc.setNextGraphics(new com.rs.game.Graphics(castGraphic));
                    int delay=shockwave?2:npcFlight(fighter,player);
                    if(fighter.strikes.size()>=32)throw new IllegalStateException("NPC strike queue exceeded bound");
                    fighter.strikes.add(new NpcStrike(tick+delay,damage,player,attackStyle));
                    if(npc.getId()==2881||shockwave){
                        int areaRange=shockwave?12:7;
                        // Area attacks hit every eligible player in range, with a
                        // separate accuracy/damage roll and captured ownership.
                        for(Player other:access.players()){
                            if(other==null||other==player||!available(other,npc)||other.getNextWorldTile()!=null
                                    ||distanceToFootprint(other,npc,fighter.profile.size)>areaRange
                                    ||!access.npcRangedReach(npc,other,areaRange)||fighter.strikes.size()>=32)continue;
                            Loadout otherGear;
                            try{otherGear=loadouts.get(other);}catch(IllegalArgumentException unsupported){continue;}
                            int otherDefence=Rs2CombatFormula.effectiveLevel(other.getSkills().getLevel(Skills.DEFENCE)
                                    +other.getPrayer().getStatBonuses(Skills.DEFENCE),0,0,1);
                            int otherDamage=rolls.accurate(Rs2CombatFormula.roll(Rs2CombatFormula.npcEffectiveLevel(fighter.profile.attackLevel),attackBonus),
                                    Rs2CombatFormula.roll(otherDefence,otherGear.defenceBonus))?Math.min(maximum,nativeDamage(rolls.damage(maximum/10))):0;
                            fighter.strikes.add(new NpcStrike(tick+(shockwave?2:npcFlight(fighter,other)),otherDamage,other,attackStyle));
                        }
                    }
                }
                swings++;
                if(player.isDead())playerDied(player);
                else {
                    if(attackStyle==0 && damage>0 && player.getNextAnimation()==null&&tick>=channelUntil.getOrDefault(player,0L)
                            &&player.getLastAnimationEnd()<=Utils.currentTimeMillis())player.setNextAnimation(new Animation(gear.blockAnimation));
                    //910 CombatScript auto-retaliation gate; never interrupt an explicit walk/skill/route.
                    if(!fighter.attacking && (targets.get(player)==null||targets.get(player)==fighter) && !fighter.outOfSupplies && tick>=ceaseUntil.getOrDefault(player,0L)&&player.getCombatDefinitions().isAutoRetaliate()
                            && !player.getActionManager().hasSkillWorking() && !player.hasWalkSteps()
                            && player.getRouteEvent()==null){fighter.attacking=true;targets.put(player,fighter);}
                }
            }
            } catch(RuntimeException failure) {failEncounter(fighter,failure);}
        }
        // The primary fighter above owns NPC retaliation and movement.  Every other
        // player that selected the same NPC gets a separate outbound combat turn.
        for(Map.Entry<Player,Fighter> entry:new ArrayList<>(targets.entrySet())) {
            Player player=entry.getKey();Fighter fighter=entry.getValue();
            if(fighter.target==player)continue;
            try { processSharedAttacker(player,fighter); }
            catch(RuntimeException failure) { stop(player);System.err.println("[Ataraxia950] Shared NPC attack failed player="+player.getIndex()+": "+failure); }
        }
    }

    /** Outbound-only turn for a second player on an NPC.  The NPC's single retaliation owner stays untouched. */
    private void processSharedAttacker(Player player,Fighter fighter) {
        NPC npc=fighter.npc;
        if(npc.isDead()||!available(player,npc)||player.hasTeleported()||player.getNextWorldTile()!=null
                ||distanceToFootprint(player,npc,fighter.profile.size)>LEASH){stop(player);return;}
        Loadout gear=loadouts.get(player);
        processPendingHits(player,fighter);
        if(npc.isDead())return;
        processDamageOverTime(player,fighter);
        if(npc.isDead())return;
        Long channelEnd=channelUntil.get(player);
        if(channelEnd!=null&&tick>=channelEnd)channelUntil.remove(player);
        if(!playerReach(player,npc,gear)){access.approach(player,npc);return;}
        boolean abilityTurn=processAbilityTurn(player,fighter);
        if(player.getLastAnimationEnd()>Utils.currentTimeMillis()||tick<channelUntil.getOrDefault(player,0L))return;
        if(npc.isDead())return;
        Long next=nextAttack.get(player);
        if(abilityTurn||next!=null&&tick<next||player.getFoodDelay()>Utils.currentTimeMillis())return;
        autoAttack(player,fighter,gear);
    }
    /** Manual queue wins. Revolution uses the same ready executor on this tick, before autos. */
    private boolean processAbilityTurn(Player player,Fighter fighter){
        if(targets.get(player)!=fighter)return false;
        if(queuedAbilities.containsKey(player)&&performAbility(player,fighter))return true;
        if(!isAttacking(player,fighter)||!player.getNative950ActionBar().isRevolutionEnabled())return false;
        int candidate=revolutionCandidate(player,player.getNative950ActionBar().revolutionSlots());
        if(candidate<0)return false;
        Native950BugTest.event(player,"combat","revolution-selected","structure",candidate,"source","revolution",
                "activeBar",player.getNative950ActionBar().activeBar()+1);
        return performAbility(player,fighter,candidate,false);
    }
    private void processDashImpacts(){
        for(Player player:new ArrayList<>(dashImpacts.keySet())){
            DashImpact impact=dashImpacts.get(player);
            if(!access.player(player)||player.isDead()||player.hasFinished()||tick>impact.expires){dashImpacts.remove(player);continue;}
            if(player.isNative950ForceMovementActive()||player.getNextWorldTile()!=null)continue;
            dashImpacts.remove(player);
            if(!player.matches(impact.tile))continue;
            int level=Rs2CombatFormula.effectiveLevel(player.getSkills().getLevel(Skills.STRENGTH),0,0,1);
            int maximum=Rs2CombatFormula.meleeOrRangedMaxHit(level,impact.gear.strengthBonus,1);
            if(impact.gear.profile!=null)maximum=impact.gear.profile.maxHit(player,maximum);
            int requested=nativeDamage(Math.max(1,maximum*(75+rolls.damage(20))/100));
            java.util.List<Fighter> nearby=new ArrayList<>(fighters.values());
            nearby.sort((a,b)->Integer.compare(a.npc.getIndex(),b.npc.getIndex()));
            int affected=0;
            for(Fighter target:nearby){
                if(affected>=9)break;
                if(target.npc.isDead()||!available(player,target.npc)||distanceToFootprint(player,target.npc,target.profile.size)>1
                        ||!access.reach(player,target.npc)||Native950Slayer.attackRefusal(player,target.npc)!=null
                        ||Native950Dungeoneering.attackRefusal(player,target.npc)!=null)continue;
                int actual=damage(player,target.npc,requested,Hit.HitLook.MELEE_DAMAGE);
                if(actual>0&&!target.training)rewards.hit(player,target.npc,actual,impact.gear);
                if(target.training)target.npc.setHitpoints(target.profile.hp);
                else if(target.npc.isDead())secondaryNpcDied(target,player);
                affected++;
            }
            if(affected>0)player.getCombatDefinitions().setSpecialAttackPercentage(Math.min(100,player.getCombatDefinitions().getSpecialAttackPercentage()+9));
            Native950BugTest.event(player,"combat","bladed-dive-landed","affected",affected);
        }
    }
    private void autoAttack(Player player,Fighter fighter,Loadout gear){
        NPC npc=fighter.npc;
                // Resolve before consuming the final arrow/bolt/thrown item.
                Native950RangedPresentation ranged=Native950RangedPresentation.resolve(player,gear.profile);
                if(gear.profile!=null&&!gear.profile.consume(player)){player.sendMessage("You cannot supply the ammunition or runes for that attack.");if(fighter.target==player)fighter.outOfSupplies=true;cancelAttack(player);}
                else {
                nextAttack.put(player,tick+gear.speed);
                Native950AutoSpells.Presentation spell=gear.profile!=null&&gear.profile.style==Native950CombatStyles.MAGIC
                        ?Native950AutoSpells.presentation(player):null;
                int animation=spell!=null?spell.animation:-1;
                player.setNextFaceEntity(npc);player.setNextAnimation(new Animation(animation>=0?animation:gear.attackAnimation));
                boolean necroAuto=gear.profile!=null&&gear.profile.style==Native950CombatStyles.NECROMANCY;
                int necroCaster=necroAuto?Native950AbilityCatalog.sequenceParam(gear.attackAnimation,2920):-1;
                int necroImpact=necroAuto?Native950AbilityCatalog.sequenceParam(gear.attackAnimation,2933):-1;
                if(necroCaster>=0)player.setNextGraphics(new com.rs.game.Graphics(necroCaster));
                int skill=gear.profile==null?Skills.ATTACK:gear.profile.skill;
                int attack=Rs2CombatFormula.effectiveLevel(player.getSkills().getLevel(skill)+player.getPrayer().getStatBonuses(skill),0,3,1);
                int strength=Rs2CombatFormula.effectiveLevel(player.getSkills().getLevel(gear.profile==null||gear.profile.style==0?Skills.STRENGTH:skill),0,0,1);
                int maximum=Rs2CombatFormula.meleeOrRangedMaxHit(strength,gear.strengthBonus,1);
                if(gear.profile!=null)maximum=gear.profile.maxHit(player,maximum);
                int damage=rolls.accurate(Rs2CombatFormula.roll(attack,gear.attackBonus),
                        Rs2CombatFormula.roll(Rs2CombatFormula.npcEffectiveLevel(fighter.profile.defenceLevel),fighter.profile.meleeDefenceBonus))
                        ? nativeDamage(rolls.damage(maximum)) : 0;
                fighter.retaliating=!fighter.training;
                if(gear.profile!=null&&gear.profile.style==Native950CombatStyles.NECROMANCY)
                    necromancy.basicAttack(player,tick,buffs.active(player,Native950CombatBuffs.Type.LIVING_DEATH,tick));
                if(spell!=null&&spell.available()){
                    // Use the same 950 projectile owner as existing world content and
                    // commit the hit when its cache-defined impact reaches the target.
                    int flightCycles=World.sendProjectileNew(player,npc,spell.projectile,41,16,55,5,0,90).getEndTime();
                    long dueTick=tick+Math.max(1,Utils.projectileTimeToCycles(flightCycles));
                    pendingHits.computeIfAbsent(player,p->new ArrayList<>()).add(new PendingHit(fighter,damage,dueTick,gear,spell.impact));
                    Native950BugTest.event(player,"magic","spell-auto-cast","spell",Native950AutoSpells.select(player).name,
                            "animation",spell.animation,"projectile",spell.projectile,"impact",spell.impact,"hitTick",dueTick);
                } else if(ranged!=null){
                    access.projectile(ranged.projectile(player,npc));
                    long dueTick=tick+ranged.hitDelay();
                    pendingHits.computeIfAbsent(player,p->new ArrayList<>()).add(new PendingHit(fighter,damage,dueTick,gear,-1));
                    Native950BugTest.event(player,"combat","ranged-auto-fired","projectile",ranged.graphic,
                            "startCycle",ranged.start,"endCycle",ranged.end,"hitTick",dueTick);
                } else {
                    if(necroImpact>=0)npc.setNextGraphics(new com.rs.game.Graphics(necroImpact));
                    int actual=damage(player,npc,damage,gear.profile==null?Hit.HitLook.MELEE_DAMAGE:gear.profile.look());
                    if(actual>0&&!fighter.training)rewards.hit(player,npc,actual,gear);
                    if(fighter.training)npc.setHitpoints(fighter.profile.hp);
                    if(actual>0&&fighter.profile.blockAnim>=0)npc.setNextAnimation(new Animation(fighter.profile.blockAnim));
                }
                swings++;
                if(gear.profile!=null&&gear.profile.ammoFamily==3&&player.getEquipment().getItem(Equipment.SLOT_WEAPON)==null){
                    // The last launched item still lands; explicit cancel/logout clears pending hits.
                    if(fighter.target==player){fighter.outOfSupplies=true;fighter.attacking=false;}
                    player.sendMessage("You have run out of thrown weapons.");
                }
                if(npc.isDead())npcDied(fighter,player);
                }
    }

    private void failEncounter(Fighter fighter, RuntimeException failure) {
        Player affected=fighter.target;
        System.err.println("[Ataraxia950] Melee encounter failed npc="+fighter.npc.getIndex()+": "+failure);
        failure.printStackTrace();
        if(affected!=null){stop(affected);if(affected.getRealChannel()!=null)affected.getRealChannel().close();}
        // If a callback failed between lethal HP and the death mask, still give the
        // world a finite recovery path for this NPC without invoking legacy death code.
        if(fighter.npc.isDead() && fighter.respawnAt==0){
            fighter.npc.setNative950DeathVisible(false);fighter.hideAt=tick;
            fighter.respawnAt=tick+fighter.profile.respawnTicks;
        }
    }
    private void processDamageOverTime(Player player,Fighter fighter){
        Map<Integer,DamageOverTime> effects=damageOverTime.get(player);
        if(effects==null)return;
        Iterator<DamageOverTime> iterator=effects.values().iterator();
        while(iterator.hasNext()){
            DamageOverTime dot=iterator.next();
            if(dot.fighter!=fighter){iterator.remove();continue;}
            if(dot.nextTick>tick)continue;
            int actual=damage(player,fighter.npc,dot.damage,dot.gear.profile==null?Hit.HitLook.MELEE_DAMAGE:dot.gear.profile.look(),true,false);
            if(actual>0&&!fighter.training)rewards.hit(player,fighter.npc,actual,dot.gear);
            if(fighter.training)fighter.npc.setHitpoints(fighter.profile.hp);
            else if(fighter.npc.isDead()){damageOverTime.remove(player);npcDied(fighter,player);return;}
            if(--dot.remaining==0)iterator.remove();else dot.nextTick=tick+2;
        }
        if(effects.isEmpty())damageOverTime.remove(player);
    }
    /**
     * Chain never changes the player's primary combat owner. It selects at most
     * two admitted native NPCs within six tiles of the primary footprint and applies
     * the same hit/reward pipeline used by ordinary ability damage.
     */
    private void chainSecondaryHits(Player player,Fighter primary,int requested,Loadout gear){
        secondaryAreaHits(player,primary,requested,gear,CHAIN_SECONDARY_RANGE,CHAIN_SECONDARY_TARGET_LIMIT);
    }
    private void secondaryAreaHits(Player player,Fighter primary,int requested,Loadout gear,int radius,int limit){
        int chained=0;
        for(Fighter candidate:new ArrayList<>(fighters.values())){
            if(chained>=limit)break;
            int range=distanceBetweenFootprints(primary.npc,primary.profile.size,candidate.npc,candidate.profile.size);
            if(candidate==primary||!secondaryEligible(player,candidate)||range>radius)
                continue;
            int actual=damage(player,candidate.npc,requested,gear.profile==null?Hit.HitLook.MELEE_DAMAGE:gear.profile.look());
            if(actual>0&&!candidate.training)rewards.hit(player,candidate.npc,actual,gear);
            if(candidate.training)candidate.npc.setHitpoints(candidate.profile.hp);
            Native950BugTest.event(player,"combat","chain-secondary-hit","npc",candidate.npc.getId()+":"+candidate.npc.getIndex(),
                    "damage",actual,"range",range);
            chained++;
            if(candidate.npc.isDead())secondaryNpcDied(candidate,player);
        }
        Native950BugTest.event(player,"combat","chain-secondary-summary","count",chained,"limit",limit,"range",radius);
    }
    /** Primary plus at most four registered NPCs in the paired ability's directional area. */
    private void dragonBreathSecondaryHits(Player player,Fighter primary,int requested,Loadout gear){
        int affected=0;
        for(Fighter candidate:new ArrayList<>(fighters.values())){
            if(affected>=4)break;
            if(candidate==primary||!secondaryEligible(player,candidate)||!dragonBreathArea(player,primary,candidate))
                continue;
            int actual=damage(player,candidate.npc,requested,gear.profile==null?Hit.HitLook.MELEE_DAMAGE:gear.profile.look());
            if(actual>0&&!candidate.training)rewards.hit(player,candidate.npc,actual,gear);
            if(candidate.training)candidate.npc.setHitpoints(candidate.profile.hp);
            Native950BugTest.event(player,"combat","dragon-breath-secondary-hit",
                    "npc",candidate.npc.getId()+":"+candidate.npc.getIndex(),"damage",actual);
            affected++;
            if(candidate.npc.isDead())secondaryNpcDied(candidate,player);
        }
        Native950BugTest.event(player,"combat","dragon-breath-secondary-summary","count",affected,"limit",4);
    }

    static boolean dragonBreathArea(Player player,Fighter primary,Fighter candidate){
        if(player.getPlane()!=candidate.npc.getPlane())return false;
        if(distanceBetweenFootprints(primary.npc,primary.profile.size,candidate.npc,candidate.profile.size)<=1)return true;
        int primaryDistance=distanceToFootprint(player,primary.npc,primary.profile.size);
        int candidateDistance=distanceToFootprint(player,candidate.npc,candidate.profile.size);
        int primaryDx=Integer.signum(primary.npc.getX()-player.getX());
        int primaryDy=Integer.signum(primary.npc.getY()-player.getY());
        int candidateDx=Integer.signum(candidate.npc.getX()-player.getX());
        int candidateDy=Integer.signum(candidate.npc.getY()-player.getY());
        return candidateDistance<=primaryDistance&&primaryDx==candidateDx&&primaryDy==candidateDy;
    }
    /** Tsunami selects a bounded 90-degree forward area independently of primary range. */
    private void tsunamiSecondaryHits(Player player,Fighter primary,int requested,Loadout gear){
        int affected=0;
        for(Fighter candidate:new ArrayList<>(fighters.values())){
            if(affected>=8)break;
            if(candidate==primary||!secondaryEligible(player,candidate)||!tsunamiArea(player,primary,candidate))
                continue;
            int actual=damage(player,candidate.npc,requested,gear.profile==null?Hit.HitLook.MELEE_DAMAGE:gear.profile.look());
            if(actual>0&&!candidate.training)rewards.hit(player,candidate.npc,actual,gear);
            if(candidate.training)candidate.npc.setHitpoints(candidate.profile.hp);
            Native950BugTest.event(player,"combat","tsunami-secondary-hit",
                    "npc",candidate.npc.getId()+":"+candidate.npc.getIndex(),"damage",actual);
            affected++;
            if(candidate.npc.isDead())secondaryNpcDied(candidate,player);
        }
        Native950BugTest.event(player,"combat","tsunami-secondary-summary","count",affected,"limit",8,"range",4);
    }

    static boolean tsunamiArea(Player player,Fighter primary,Fighter candidate){
        if(player.getPlane()!=candidate.npc.getPlane()||distanceToFootprint(player,candidate.npc,candidate.profile.size)>4)return false;
        long primaryX=primary.npc.getX()-player.getX(),primaryY=primary.npc.getY()-player.getY();
        long candidateX=candidate.npc.getX()-player.getX(),candidateY=candidate.npc.getY()-player.getY();
        long dot=primaryX*candidateX+primaryY*candidateY;
        long cross=primaryX*candidateY-primaryY*candidateX;
        return dot>0&&Math.abs(cross)<=dot;
    }
    /** Both Hurricane hits affect the primary plus up to nine adjacent registered NPCs. */
    private void hurricaneSecondaryHits(Player player,Fighter primary,int requested,Loadout gear){
        int affected=0;
        for(Fighter candidate:new ArrayList<>(fighters.values())){
            if(affected>=9)break;
            if(candidate==primary||!secondaryEligible(player,candidate)
                    ||!hurricaneArea(player,candidate.npc,candidate.profile.size))
                continue;
            int actual=damage(player,candidate.npc,requested,gear.profile==null?Hit.HitLook.MELEE_DAMAGE:gear.profile.look());
            if(actual>0&&!candidate.training)rewards.hit(player,candidate.npc,actual,gear);
            if(candidate.training)candidate.npc.setHitpoints(candidate.profile.hp);
            Native950BugTest.event(player,"combat","hurricane-secondary-hit",
                    "npc",candidate.npc.getId()+":"+candidate.npc.getIndex(),"damage",actual);
            affected++;
            if(candidate.npc.isDead())secondaryNpcDied(candidate,player);
        }
        Native950BugTest.event(player,"combat","hurricane-secondary-summary","count",affected,"limit",9,"range",1);
    }
    static boolean hurricaneArea(WorldTile player,WorldTile candidate,int candidateSize){
        return player.getPlane()==candidate.getPlane()&&distanceToFootprint(player,candidate,candidateSize)<=1;
    }
    /** Meteor Strike splashes the primary's adjacent registered NPCs without taking ownership. */
    private void meteorSecondaryHits(Player player,Fighter primary,int requested,Loadout gear){
        int affected=0;
        for(Fighter candidate:new ArrayList<>(fighters.values())){
            if(affected>=8)break;
            if(candidate==primary||!secondaryEligible(player,candidate)
                    ||distanceBetweenFootprints(primary.npc,primary.profile.size,candidate.npc,candidate.profile.size)>1)
                continue;
            int actual=damage(player,candidate.npc,requested,gear.profile==null?Hit.HitLook.MELEE_DAMAGE:gear.profile.look());
            if(actual>0&&!candidate.training)rewards.hit(player,candidate.npc,actual,gear);
            if(candidate.training)candidate.npc.setHitpoints(candidate.profile.hp);
            Native950BugTest.event(player,"combat","meteor-secondary-hit",
                    "npc",candidate.npc.getId()+":"+candidate.npc.getIndex(),"damage",actual);
            affected++;
            if(candidate.npc.isDead())secondaryNpcDied(candidate,player);
        }
        Native950BugTest.event(player,"combat","meteor-secondary-summary","count",affected,"limit",8,"range",1);
    }
    private boolean secondaryEligible(Player player,Fighter candidate){
        if(candidate.returning||candidate.respawnAt>0||candidate.npc.isDead()||!access.npc(candidate.npc)
                ||player.getPlane()!=candidate.npc.getPlane())return false;
        int distance=Math.max(1,distanceToFootprint(player,candidate.npc,candidate.profile.size));
        return access.rangedReach(player,candidate.npc,distance)
                &&Native950Slayer.attackRefusal(player,candidate.npc)==null
                &&Native950Dungeoneering.attackRefusal(player,candidate.npc)==null;
    }
    private boolean npcReach(Fighter fighter,Player player) {
        return fighter.profile.attackStyle==0?access.reach(fighter.npc,player):access.npcRangedReach(fighter.npc,player,7);
    }
    private int npcFlight(Fighter fighter,Player target){
        int graphic=fighter.profile.attackProjectile;if(graphic<0)return 2;
        NPC npc=fighter.npc;
        int end=35+Utils.getDistance(npc.getX(),npc.getY(),target.getX(),target.getY())*30/4;
        access.projectile(new com.rs.game.Projectile(npc,target,false,false,0,graphic,41,16,35,end,npc.getSize()*64,16));
        return Math.max(1,Utils.projectileTimeToCycles(end));
    }
    private void processNpcStrikes(Fighter fighter,Player player) {
        for(int i=0;i<fighter.strikes.size();) {
            NpcStrike strike=fighter.strikes.get(i);
            if(strike.due>tick){i++;continue;}
            fighter.strikes.remove(i);
            player=strike.target;
            if(!available(player,fighter.npc)||player.getNextWorldTile()!=null||distance(player,fighter.npc)>LEASH)continue;
            Hit.HitLook look=strike.style==1?Hit.HitLook.RANGE_DAMAGE:Hit.HitLook.MAGIC_DAMAGE;
            int actual=damage(fighter.npc,player,strike.damage,look);
            int impact=Native950BossRules.impactGraphic(fighter.npc.getId());
            if(actual>0&&impact>=0)player.setNextGraphics(new com.rs.game.Graphics(impact));
            if(player.isDead()){playerDied(player);return;}
            if(actual>0 && player.getNextAnimation()==null && tick>=channelUntil.getOrDefault(player,0L)
                    && player.getLastAnimationEnd()<=Utils.currentTimeMillis())
                player.setNextAnimation(new Animation(loadouts.get(player).blockAnimation));
        }
    }
    // Exact950 CS9681(7)->CS9688 compares cape passive21 in these four item params.
    static boolean igneousNecromancyCape(Player p){
        int cape=p.getEquipment().getCapeId();
        if(cape<0||com.rs.cache.Cache.STORE==null)return false;
        for(int param:new int[]{2881,8591,8592,8902})if(Native950CacheItems.definition(cape).getCSOpcode(param)==21)return true;
        return false;
    }
    static int skullsCost(Player p){return igneousNecromancyCape(p)?60:100;}
    static int skullsRange(Player p){
        int weapon=p.getEquipment().getWeaponId();
        if(weapon<0||com.rs.cache.Cache.STORE==null)return 6;
        com.rs.cache.loaders.ItemDefinitions item=Native950CacheItems.definition(weapon);
        return item.getCSOpcode(8898)==1?Math.max(6,item.getCSOpcode(13)):6;
    }
    private int conjureAbilityDamage(Player p){
        Loadout gear=loadouts.get(p);int level=Rs2CombatFormula.effectiveLevel(p.getSkills().getLevel(Skills.NECROMANCY),0,0,1);
        int maximum=Rs2CombatFormula.meleeOrRangedMaxHit(level,gear.strengthBonus,1);
        return nativeDamage(gear.profile==null?maximum:gear.profile.maxHit(p,maximum));
    }
    private int conjureStrike(Player owner,NPC npc,int min,int max){
        Fighter target=fighters.get(npc);if(target==null||!secondaryEligible(owner,target)||!available(owner,npc))return 0;
        int requested=(int)Math.min(Integer.MAX_VALUE,(long)conjureAbilityDamage(owner)*(min+rolls.damage(Math.max(0,max-min)))/100);
        int actual=damage(owner,npc,requested,Hit.HitLook.CONJURE_DAMAGE,true,false);
        if(actual>0&&!target.training)rewards.hit(owner,npc,actual,loadouts.get(owner));
        if(target.training)npc.setHitpoints(target.profile.hp);else if(npc.isDead())npcDied(target,owner);else engageRetaliation(target,owner);
        return actual;
    }
    static int skullFlightTicks(int distance){return distance>=6?3:2;}
    void startDeathSkulls(Player owner,NPC target,Loadout gear,int damage,int range,int bounces,int graphic){
        owned();
        if(graphic<0)throw new IllegalStateException("Death Skulls requires its verified projectile");
        Hit.HitLook look=gear.profile==null?Hit.HitLook.MAGIC_DAMAGE:gear.profile.look();
        int snapshot=buffs.outgoing(owner,look,prayerAdjustedDamage(owner,target,damage,look),tick);
        boolean critical=snapshot>0&&rolls.critical();
        if(critical)snapshot=(int)Math.min(Integer.MAX_VALUE,(long)snapshot*5/4);
        SkullFlight flight=new SkullFlight(owner,gear,snapshot,range,bounces,graphic,critical);
        launchSkull(flight,owner,target);skullFlights.add(flight);
    }
    private void launchSkull(SkullFlight flight,WorldTile from,Entity to){
        int delay=skullFlightTicks(distance(from,to));
        flight.target=to;flight.destination=new WorldTile(to);flight.due=tick+delay;
        com.rs.game.Projectile p=new com.rs.game.Projectile(from,to,false,false,0,flight.graphic,41,41,0,delay*30,5,0);
        p.setNewProjectile(true);access.projectile(p);
        Native950BugTest.event(flight.owner,"combat","skulls-flight","from",from.getClass().getSimpleName()+":"+from.getX()+","+from.getY(),
                "to",to.getClass().getSimpleName()+":"+to.getIndex(),"graphic",flight.graphic,"impactTick",flight.due,"remaining",flight.remaining);
    }
    private void processSkulls(){
        for(SkullFlight flight:new ArrayList<>(skullFlights)){
            Player owner=flight.owner;
            if(!access.player(owner)||!owner.isActive()||owner.hasFinished()||owner.isDead()||owner.hasTeleported()
                    ||owner.getNextWorldTile()!=null||owner.getPlane()!=flight.destination.getPlane()){
                skullFlights.remove(flight);continue;
            }
            if(tick<flight.due)continue;
            Entity previous=flight.target;
            WorldTile origin=previous.hasFinished()?flight.destination:new WorldTile(previous);
            if(previous instanceof NPC){
                Fighter target=fighters.get(previous);
                if(target!=null&&secondaryEligible(owner,target)&&available(owner,target.npc)){
                    int actual=damage(owner,target.npc,flight.damage,flight.gear.profile==null?Hit.HitLook.MAGIC_DAMAGE:flight.gear.profile.look(),true,false,flight.critical);
                    if(actual>0&&!target.training)rewards.hit(owner,target.npc,actual,flight.gear);
                    if(target.training)target.npc.setHitpoints(target.profile.hp);
                    else if(target.npc.isDead())npcDied(target,owner);
                    else engageRetaliation(target,owner);
                    Native950BugTest.event(owner,"combat","skulls-impact","npc",target.npc.getIndex(),"damage",actual);
                }
            }
            if(flight.remaining--<=0){skullFlights.remove(flight);continue;}
            Fighter next=null;
            for(Fighter candidate:fighters.values()){
                if(candidate.npc==previous||!secondaryEligible(owner,candidate)||!available(owner,candidate.npc)
                        ||distanceToFootprint(origin,candidate.npc,candidate.profile.size)>flight.range
                        ||!access.bounceReach(origin,candidate.npc))continue;
                if(next==null||candidate.profile.hp>next.profile.hp||candidate.profile.hp==next.profile.hp&&candidate.npc.getIndex()<next.npc.getIndex())next=candidate;
            }
            Entity destination=next==null?previous!=owner&&distance(origin,owner)<=flight.range&&access.bounceReach(origin,owner)?owner:null:next.npc;
            if(destination==null){skullFlights.remove(flight);continue;}
            launchSkull(flight,previous.hasFinished()?origin:previous,destination);
        }
    }
    private void engageRetaliation(Fighter fighter,Player owner){
        if(fighter.training)return;
        fighter.retaliating=true;
        if(fighter.target==null){fighter.target=owner;fighter.attacking=targets.get(owner)==fighter;fighter.npc.setNative950CombatEngaged(true);}
    }
    private static final class SkullFlight {
        final Player owner;final Loadout gear;final int damage,range,graphic;final boolean critical;
        int remaining;long due;Entity target;WorldTile destination;
        SkullFlight(Player owner,Loadout gear,int damage,int range,int remaining,int graphic,boolean critical){
            this.owner=owner;this.gear=gear;this.damage=damage;this.range=range;this.remaining=remaining;this.graphic=graphic;this.critical=critical;
        }
    }
    private void processPendingHits(Player player,Fighter fighter){
        java.util.List<PendingHit> scheduled=pendingHits.get(player);
        if(scheduled==null)return;
        if(!available(player,fighter.npc)||player.hasTeleported()){
            pendingHits.remove(player);
            return;
        }
        Iterator<PendingHit> iterator=scheduled.iterator();
        while(iterator.hasNext()){
            PendingHit hit=iterator.next();
            if(!hit.released&&hit.channelStructure>=0&&(!validChannel(player,fighter,hit.channelStructure)
                    ||player.getEquipment().getWeaponId()!=hit.weapon||player.getEquipment().getShieldId()!=hit.offhand)){
                iterator.remove();channelUntil.remove(player);
                Native950BugTest.event(player,"combat","channel-hit-cancelled","structure",hit.channelStructure,
                        "dueTick",hit.dueTick,"reason","target-range-style-or-interruption");
                continue;
            }
            if(hit.dueTick>tick)continue;
            if(hit.projectileGraphic>=0&&!hit.released){releaseAbilityProjectile(player,hit);continue;}
            iterator.remove();
            if(hit.fighter!=fighter||(!isAttacking(player,fighter)&&hit.abilityStructure>=0)||fighter.npc.isDead())continue;
            if(hit.impactGraphic>=0)fighter.npc.setNextGraphics(new com.rs.game.Graphics(hit.impactGraphic));
            int requested=hit.siphonFinisher?(int)Math.min(Integer.MAX_VALUE,(long)hit.damage+hit.siphon.healValue):hit.damage;
            int actual=damage(player,fighter.npc,requested,hit.gear.profile==null?Hit.HitLook.MELEE_DAMAGE:hit.gear.profile.look());
            if(actual>0&&!fighter.npc.isDead()&&fighter.profile.blockAnim>=0)
                fighter.npc.setNextAnimation(new Animation(fighter.profile.blockAnim));
            if(actual>0&&!fighter.training)rewards.hit(player,fighter.npc,actual,hit.gear);
            abilityImpact(player,fighter,hit.abilityStructure,hit.damage,actual,hit.gear,hit.baseAbilityDamage);
            if(hit.siphon!=null&&!hit.siphonFinisher)siphonPulse(player,fighter,hit.damage,actual,hit.gear,hit.siphon);
            if(fighter.training)fighter.npc.setHitpoints(fighter.profile.hp);
            else if(fighter.npc.isDead()){
                pendingHits.remove(player);npcDied(fighter,player);return;
            }
        }
        if(scheduled.isEmpty())pendingHits.remove(player);
    }
    /** All impact effects share the immediate and projectile/delayed-hit boundary. */
    private void abilityImpact(Player player,Fighter fighter,int structure,int requested,int actual,Loadout gear,int baseAbilityDamage){
        Native950AbilityCatalog.Definition definition=Native950AbilityCatalog.get(structure);
        if(definition==null)return;
        if(definition.effect==Native950AbilityCatalog.Effect.STUN&&!fighter.npc.isDead()&&!Native950BossRules.stunImmune(fighter.npc.getId()))fighter.stunnedUntil=tick+5;
        if(structure==14728)chainSecondaryHits(player,fighter,requested,gear);
        if(structure==14730)dragonBreathSecondaryHits(player,fighter,requested,gear);
        if(structure==14735)tsunamiSecondaryHits(player,fighter,requested,gear);
        if(structure==14685)hurricaneSecondaryHits(player,fighter,requested,gear);
        if(structure==14688)meteorSecondaryHits(player,fighter,requested,gear);
        if(structure==14671)secondaryAreaHits(player,fighter,requested,gear,1,8);
        if(structure==48299)secondaryAreaHits(player,fighter,requested*2/3,gear,1,9);
        if(structure>=48311&&structure<=48313){
            if(actual>0&&structure!=48313&&rolls.damage(99)<25)necromancy.gainSoul(player,tick);
            scytheSecondaryHits(player,fighter,structure,requested,gear);
        }
        if(structure==14717&&!fighter.npc.isDead())buffs.apply(player,Native950CombatBuffs.Type.DEBILITATE,tick,13+shieldLevel(player)/9,fighter.npc);
        if(structure==24188&&actual>0)player.heal(fighter.npc.isDead()?actual:actual/4);
        if(structure==28177||structure==28180){
            // Exact950 tooltip CS18689/18625: recoil uses ability damage, not damage received.
            int recoilPercent=structure==28177?100+rolls.damage(35):35+rolls.damage(5);
            int recoil=(int)Math.min(Integer.MAX_VALUE,(long)baseAbilityDamage*recoilPercent/100);
            damage(player,player,recoil,Hit.HitLook.REGULAR_DAMAGE,true,false);
            if(player.isDead())playerDied(player);
        }
    }
    private static int scytheFinisher(int damage,Fighter target){return (int)Math.min(Integer.MAX_VALUE,(long)damage*(200-Math.min(100,(long)target.npc.getHitpoints()*100/Math.max(1,target.profile.hp)))/100);}
    private void scytheSecondaryHits(Player p,Fighter primary,int structure,int requested,Loadout gear){
        int limit=structure==48311?9:25,count=0,fx=Integer.signum(primary.npc.getX()-p.getX()),fy=Integer.signum(primary.npc.getY()-p.getY());
        java.util.List<Fighter> ordered=new ArrayList<>(fighters.values());ordered.sort((a,b)->Integer.compare(a.npc.getIndex(),b.npc.getIndex()));
        for(Fighter target:ordered){
            if(count>=limit)break;
            if(target==primary||!secondaryEligible(p,target)||distanceToFootprint(p,target.npc,target.profile.size)>2||!access.bounceReach(p,target.npc))continue;
            int dx=target.npc.getX()-p.getX(),dy=target.npc.getY()-p.getY();
            if(structure==48311&&(dx*fx+dy*fy<=0||Math.abs(dx*fy-dy*fx)>dx*fx+dy*fy))continue;
            int actual=damage(p,target.npc,structure==48313?scytheFinisher(requested,target):requested,Hit.HitLook.NECROMANCY_DAMAGE);
            if(actual>0&&!target.training)rewards.hit(p,target.npc,actual,gear);
            if(actual>0&&structure!=48313&&rolls.damage(99)<25)necromancy.gainSoul(p,tick);
            if(target.training)target.npc.setHitpoints(target.profile.hp);else if(target.npc.isDead())secondaryNpcDied(target,p);else engageRetaliation(target,p);
            count++;
        }
    }
    /** CS18670: four area pulses, 70% healing, then a single-target finishing hit. */
    private void siphonPulse(Player player,Fighter primary,int requested,int primaryDamage,Loadout gear,SiphonState state){
        long damageDealt=primaryDamage;
        java.util.List<Fighter> ordered=new ArrayList<>(fighters.values());
        ordered.sort((a,b)->Integer.compare(a.npc.getIndex(),b.npc.getIndex()));
        int affected=1;
        for(Fighter candidate:ordered){
            if(affected>=25)break;
            if(candidate==primary||!secondaryEligible(player,candidate)
                    ||distanceToFootprint(player,candidate.npc,candidate.profile.size)>2)continue;
            int actual=damage(player,candidate.npc,requested,gear.profile.look());damageDealt+=actual;affected++;
            if(actual>0&&!candidate.training)rewards.hit(player,candidate.npc,actual,gear);
            if(candidate.training)candidate.npc.setHitpoints(candidate.profile.hp);
            else if(candidate.npc.isDead())secondaryNpcDied(candidate,player);
        }
        int heal=(int)Math.min(Integer.MAX_VALUE,damageDealt*70/100);
        state.healValue=(int)Math.min(Integer.MAX_VALUE,(long)state.healValue+heal);player.heal(heal);
        Native950BugTest.event(player,"combat","siphon-pulse","targets",affected,"healValue",heal,"accumulatedHeal",state.healValue);
    }
    private void releaseAbilityProjectile(Player player,PendingHit hit){
        if(hit.projectileGraphic<0)return;
        // Shared native projectile encoding and distance timing used by NPC ranged/magic attacks.
        // Graphic identity comes exclusively from this ability's exact950 struct/sequence.
        int end=35+Utils.getDistance(player.getX(),player.getY(),hit.fighter.npc.getX(),hit.fighter.npc.getY())*30/4;
        com.rs.game.Projectile projectile=new com.rs.game.Projectile(player,hit.fighter.npc,false,false,0,
                hit.projectileGraphic,41,16,35,end,5,0);
        projectile.setNewProjectile(true);access.projectile(projectile);
        hit.released=true;hit.dueTick=tick+Math.max(1,Utils.projectileTimeToCycles(end));
        Native950BugTest.event(player,"combat","ability-projectile","structure",hit.abilityStructure,
                "graphic",hit.projectileGraphic,"impactTick",hit.dueTick);
    }
    private boolean validChannel(Player player,Fighter fighter,int structure){
        if(!isAttacking(player,fighter)||player.isStunned()||player.isLocked()||player.getNextWorldTile()!=null)return false;
        Native950AbilityCatalog.Definition definition=Native950AbilityCatalog.get(structure);
        Loadout current;
        try{current=loadouts.get(player);}catch(IllegalArgumentException unsupported){return false;}
        return definition!=null&&(current.profile==null?0:current.profile.style)==definition.style()
                &&(structure!=48309||distanceToFootprint(player,fighter.npc,fighter.profile.size)<=2)
                &&(!definition.offhandRequired||matchingOffhand(player,definition.style()))
                &&(!definition.twoHandedRequired||player.getEquipment().hasTwoHandedWeapon())
                &&playerReach(player,fighter.npc,current);
    }
    private boolean playerReach(Player p,NPC n,Loadout gear){
        if(gear.profile==null||gear.profile.range<=1)return access.reach(p,n);
        return access.rangedReach(p,n,gear.profile.range);
    }
    private int damage(Entity source, Entity target, int requested) {return damage(source,target,requested,Hit.HitLook.MELEE_DAMAGE);}
    private int nativeDamage(int rawDamage){return Rs2CombatFormula.scaleNative950Damage(rawDamage,rolls.nativeDamageRemainder(rawDamage));}
    private int damage(Entity source, Entity target, int requested,Hit.HitLook look) {
        return damage(source,target,requested,look,false,true);
    }
    private int damage(Entity source,Entity target,int requested,Hit.HitLook look,boolean adjusted,boolean buffable){
        return damage(source,target,requested,look,adjusted,buffable,null);
    }
    private int damage(Entity source,Entity target,int requested,Hit.HitLook look,boolean adjusted,boolean buffable,Boolean criticalSnapshot){
        if(source!=target){
            if(source instanceof Player)necromancy.combatActivity((Player)source,tick);
            if(target instanceof Player)necromancy.combatActivity((Player)target,tick);
        }
        if(!adjusted)requested=prayerAdjustedDamage(source,target,requested,look);
        if(buffable&&source instanceof Player)requested=buffs.outgoing((Player)source,look,requested,tick);
        if(source instanceof Player&&target instanceof NPC&&buffable)requested+=conjures.hauntedBonus((Player)source,(NPC)target,requested,tick);
        if(target instanceof Player){
            Player defender=(Player)target;
            if(look==Hit.HitLook.MELEE_DAMAGE||look==Hit.HitLook.RANGE_DAMAGE||look==Hit.HitLook.MAGIC_DAMAGE||look==Hit.HitLook.NECROMANCY_DAMAGE)
                requested=conjures.absorb(defender,requested);
            buffs.receivedAttack(defender,tick);
            if(buffs.active(defender,Native950CombatBuffs.Type.DEVOTION,tick)&&protectedStyle(defender,look))requested=0;
            if(buffs.active(defender,Native950CombatBuffs.Type.BARRICADE,tick)||defender.isInvulnerable())requested=0;
            if(requested>0&&buffs.active(defender,Native950CombatBuffs.Type.PREPARATION,tick)){
                Map<Integer,Long> cooldowns=abilityCooldowns.get(defender);
                if(cooldowns!=null)for(int shared:new int[]{14713,45045})if(cooldowns.containsKey(shared)){
                    long end=Math.max(tick,cooldowns.get(shared)-5);cooldowns.put(shared,end);
                    defender.getNative950ActionBar().cooldown(defender.getRealChannel(),shared,(int)Utils.currentWorldCycle(),(int)(end-tick));
                }
            }
            if(requested>0&&buffs.consume(defender,Native950CombatBuffs.Type.RESONANCE,tick)){
                defender.heal((int)Math.min(Integer.MAX_VALUE,(long)requested*(50+shieldLevel(defender)/2)/100));requested=0;
            }
            if(requested>0&&buffs.consume(defender,Native950CombatBuffs.Type.DIVERT,tick)){
                defender.getCombatDefinitions().setSpecialAttackPercentage(Math.min(100,
                        defender.getCombatDefinitions().getSpecialAttackPercentage()+divertAdrenaline(requested,shieldLevel(defender))));
                requested=0;
            }
            int reflected=buffs.active(defender,Native950CombatBuffs.Type.REFLECT,tick)?requested/2:0;
            requested=buffs.incoming(defender,source,requested,tick);
            if(reflected>0&&source instanceof NPC&&!source.isDead()){
                int dealt=damage(defender,source,reflected,look,true,false);
                Fighter reflectedTarget=fighters.get(source);
                if(reflectedTarget!=null){
                    if(dealt>0&&!reflectedTarget.training)rewards.hit(defender,(NPC)source,dealt,loadouts.get(defender));
                    if(reflectedTarget.training)source.setHitpoints(reflectedTarget.profile.hp);
                    else if(source.isDead())secondaryNpcDied(reflectedTarget,defender);
                }
            }
        }
        boolean critical=criticalSnapshot!=null?criticalSnapshot:requested>0&&source instanceof Player&&target instanceof NPC&&buffable&&rolls.critical();
        if(critical&&criticalSnapshot==null)requested=(int)Math.min(Integer.MAX_VALUE,((long)requested*5)/4);
        if(target instanceof NPC&&!Native950BossRules.acceptsDamage(((NPC)target).getId(),look))requested=0;
        int damage=target instanceof Player && ((Player)target).isInvulnerable()
                ? 0 : Math.max(0,Math.min(target.getHitpoints(),requested));
        boolean revive=target instanceof Player&&damage>=target.getHitpoints()
                &&buffs.consume((Player)target,Native950CombatBuffs.Type.IMMORTALITY,tick);
        target.setHitpoints(target.getHitpoints()-damage);
        if(revive){target.setHitpoints(Math.max(1,((Player)target).getMaxHitpoints()*40/100));((Player)target).sendMessage("Immortality returns you to life.");}
        Hit display=new Hit(source,damage,look,0);
        if(critical&&damage>0)display.setCriticalMark();
        target.getNextHits().add(display);target.addHitBars();
        target.setAttackedBy(source);target.setAttackedByDelay(Utils.currentTimeMillis()+6000);source.setAttackingDelay(Utils.currentTimeMillis()+6000);
        if(target instanceof Player)((Player)target).refreshHitPoints();
        if(damage>0){hits++;target.addReceivedDamage(source,damage);}
        if(damage>0&&buffable&&look==Hit.HitLook.RANGE_DAMAGE&&source instanceof Player&&target instanceof NPC
                &&buffs.active((Player)source,Native950CombatBuffs.Type.SHADOW_IMBUED,tick)){
            Player attacker=(Player)source;
            attacker.getCombatDefinitions().setSpecialAttackPercentage(Math.min(100,
                    attacker.getCombatDefinitions().getSpecialAttackPercentage()+5));
        }
        return damage;
    }
    /** Shares Prayer's authoritative buffs without entering legacy hit/degradation callbacks. */
    static int prayerAdjustedDamage(Entity source,Entity target,int damage,Hit.HitLook look){
        int style=look==Hit.HitLook.MELEE_DAMAGE?0:look==Hit.HitLook.RANGE_DAMAGE?1:look==Hit.HitLook.MAGIC_DAMAGE?2:-1;
        if(style<0)return damage;
        if(source instanceof Player)damage=(int)Math.min(Integer.MAX_VALUE,
                Math.floor(damage*(1.0+((Player)source).getPrayer().getDamageMultiplier(style))));
        if(target instanceof Player){
            com.rs.game.player.Prayer prayer=((Player)target).getPrayer();
            int protection=style==0?13:style==1?12:11;
            if(prayer.usingPrayer(0,protection)||prayer.usingPrayer(1,protection))damage/=2;
        }
        return damage;
    }
    private void npcDied(Fighter fighter,Player player) {
        for(Player attacker:new ArrayList<>(targets.keySet()))if(targets.get(attacker)==fighter)stopCurrent(attacker);
        retireNpc(fighter,player);
    }
    /** A Chain secondary can die without interrupting the primary target. */
    private void secondaryNpcDied(Fighter fighter,Player player) {
        // An area hit may kill an NPC another player is fighting. Retire every
        // associated target/queue exactly as for a direct kill; preserve unrelated fights.
        npcDied(fighter,player);
    }
    private void retireNpc(Fighter fighter,Player player) {
        buffs.onKill(player,tick);
        fighter.strikes.clear();
        NPC npc=fighter.npc;npc.resetWalkSteps();npc.setNative950CombatEngaged(true);
        npc.setNative950DeathVisible(true);npc.setNextAnimation(new Animation(fighter.profile.deathAnim));
        fighter.hideAt=tick+Math.max(fighter.profile.deathTicks,fighter.profile.deathAnimationTicks);
        // A zero authored interval means one-life. Explicit developer repeat uses a 60-tick interval.
        fighter.respawnAt=fighter.hideAt+(fighter.respawnAllowed&&fighter.profile.respawnTicks==0?60:fighter.profile.respawnTicks);kills++;
        Player credited=npc.getMostDamageReceivedSourcePlayer();
        if(credited!=null)rewards.death(npc,credited);
        player.getPackets().sendGameMessage("You defeat the "+fighter.profile.name+".");
        System.out.println("[Ataraxia950] Melee death npc="+npc.getIndex()+" id="+npc.getId()+" respawnIn="+(fighter.respawnAt-tick)+" ticks");
    }
    public void playerDied(Player player) {
        owned();
        if(!player.isDead()||deadPlayers.containsKey(player))return;
        conjures.clear(player);necromancy.clear(player);ceaseUntil.remove(player);dashImpacts.remove(player);skullFlights.removeIf(f->f.owner==player);
        Native950Dungeoneering.onPlayerDeath(player);
        buffs.remove(player,this::buffRemoved);
        Native950Potions.removeOverloadOnDeath(player);
        if(player.getPrayer().hasPrayersOn())player.getPrayer().closeAllPrayers();
        stop(player);player.resetWalkSteps();player.setRouteEvent(null);player.setNextForceMovement(null);
        player.setNextAnimation(new Animation(Native950CombatAnimations.deathAnimation()));player.lock(PLAYER_RESPAWN_TICKS);
        deadPlayers.put(player,tick+PLAYER_RESPAWN_TICKS);
        player.getPackets().sendGameMessage("You have been defeated. You will recover in Lumbridge with your items.");
    }
    public WorldTile trainingTile() {
        owned();
        java.util.List<Fighter> choices=new ArrayList<>(fighters.values());
        choices.sort((a,b)->Integer.compare(a.npc.getIndex(),b.npc.getIndex()));
        for(Fighter fighter:choices) {
            if(fighter.npc.isDead() || fighter.target!=null)continue;
            for(WorldTile tile:Native950MeleeReach.contactTiles(fighter.npc,fighter.profile.size))
                if(access.clear(tile))return tile;
        }
        return null;
    }
    public String status() {owned();return "fighters="+fighters.size()+", unavailableTypes="+unavailableDefinitions.size()+", engaged="+targets.size()+", swings="+swings+", damagingHits="+hits+", kills="+kills+", respawns="+respawns;}
    int pendingHitCount(Player player) {owned();java.util.List<PendingHit> hits=pendingHits.get(player);return hits==null?0:hits.size();}
    long channelEndTick(Player player){owned();return channelUntil.getOrDefault(player,0L);}
    int dotCount(Player player){owned();Map<Integer,DamageOverTime> effects=damageOverTime.get(player);return effects==null?0:effects.size();}
    static boolean matchingOffhand(Player player,int style){
        if(style==Native950CombatStyles.NECROMANCY)return Native950NecromancyEquipment.conduit(player);
        Item item=player.getEquipment().getItem(Equipment.SLOT_SHIELD);
        return item!=null&&player.getEquipment().hasOffHand()
                &&Native950CombatStyles.classify(Native950CacheItems.definition(item.getId()))==style;
    }
    boolean isBerserkActive(Player player) {owned();return buffs.active(player,Native950CombatBuffs.Type.BERSERK,tick);}
    static int shieldLevel(Player player){
        if(!hasNativeShield(player))return 0;
        Native950EquipmentTypes.Type type=Native950EquipmentTypes.resolve(player.getEquipment().getShieldId());
        return type==null?0:Math.max(0,Math.min(99,type.requirements.getOrDefault(Skills.DEFENCE,0)));
    }
    static boolean hasNativeShield(Player player){return player.getEquipment().hasShield()&&!player.getEquipment().hasOffHand();}
    static int divertAdrenaline(int damage,int shieldLevel){
        double weighted=Math.min(3000,damage)+Math.min(3000,Math.max(0,damage-3000))/2.0
                +Math.min(3000,Math.max(0,damage-6000))/4.0+Math.max(0L,(long)damage-9000)/8.0;
        return Math.min(50,(int)(weighted*0.8/(200-Math.max(0,Math.min(99,shieldLevel)))));
    }
    private static boolean protectedStyle(Player player,Hit.HitLook look){
        int id=look==Hit.HitLook.MELEE_DAMAGE?13:look==Hit.HitLook.RANGE_DAMAGE?12:look==Hit.HitLook.MAGIC_DAMAGE?11:-1;
        return id>=0&&(player.getPrayer().usingPrayer(0,id)||player.getPrayer().usingPrayer(1,id));
    }
    boolean isDeathsSwiftnessActive(Player player) {owned();return buffs.active(player,Native950CombatBuffs.Type.DEATHS_SWIFTNESS,tick);}
    private void buffRemoved(Player player,Native950CombatBuffs.Type type){
        if(type==Native950CombatBuffs.Type.LIVING_DEATH)player.getAppearence().setNative950LivingDeath(false);
        if(type==Native950CombatBuffs.Type.SUNSHINE)areas.remove(player,3856);
        if(type==Native950CombatBuffs.Type.DEATHS_SWIFTNESS)areas.remove(player,8996);
        Native950BugTest.event(player,"combat","effect-removed","effect",type,"tick",tick);
    }
    static WorldTile respawnTile(){return new WorldTile(3217,3258,0);}
    private boolean available(Player player,NPC npc) {
        return player!=null&&player.getClientProfile()==ClientProfile.NATIVE_950&&access.player(player)&&access.npc(npc)
                &&player.isActive()&&!player.hasFinished()&&!player.isDead()&&!player.isLocked()&&!player.isNative950ForceMovementActive()
                &&!npc.hasFinished()&&!npc.isCantInteract()&&player.getPlane()==npc.getPlane()
                &&player.getControlerManager().canHit(npc);
    }
    private void owned(){if(Thread.currentThread()!=owner)throw new IllegalStateException("Native melee state belongs to its world thread");}
    private static int distance(WorldTile a,WorldTile b){return Math.max(Math.abs(a.getX()-b.getX()),Math.abs(a.getY()-b.getY()));}
    static int distanceToFootprint(WorldTile point,WorldTile origin,int size) {
        int dx=Math.max(0,Math.max(origin.getX()-point.getX(),point.getX()-(origin.getX()+size-1)));
        int dy=Math.max(0,Math.max(origin.getY()-point.getY(),point.getY()-(origin.getY()+size-1)));
        return Math.max(dx,dy);
    }
    private static int distanceBetweenFootprints(WorldTile first,int firstSize,WorldTile second,int secondSize){
        int dx=Math.max(0,Math.max(first.getX()-(second.getX()+secondSize-1),second.getX()-(first.getX()+firstSize-1)));
        int dy=Math.max(0,Math.max(first.getY()-(second.getY()+secondSize-1),second.getY()-(first.getY()+firstSize-1)));
        return Math.max(dx,dy);
    }
    static Loadout loadout(Player player) {
        if(!Native950CombatAnimations.verifyCache() || !Native950Hits.verifyCache())
            throw new IllegalStateException("Native melee requires the verified 950 animation and hit definitions");
        int weapon=player.getEquipment().getWeaponId();
        if(!Native950CombatAnimations.supportsWeapon(weapon))return Native950CombatStyles.loadout(player);
        ClassicBonuses sum=ClassicBonuses.ZERO;
        Item[] equipped=player.getEquipment().getItems().getItems();
        for(int slot=0;slot<equipped.length;slot++) {
            Item item=equipped[slot];if(item==null)continue;
            // Generic current-cache equipment admission, complete reviewed neutral metadata,
            // and the actual occupied slot are required before omitting classic bonuses.
            if(Native950CosmeticEquipment.isNeutralCosmeticEquipment(item.getId(),slot))continue;
            boolean verifiedKit=Native950MeleeEquipment.isVerifiedKitItem(item.getId());
            if(!verifiedKit && !Native950IdValidity.get().isSafe(Native950IdValidity.Kind.ITEM,item.getId()))
                throw new IllegalArgumentException("An equipped item is not ready for combat yet.");
            ClassicBonuses bonus=RS2BonusDatabase.lookup2009scape(Native950MeleeEquipment.classicName(item.getId(),item.getName()));
            if(bonus==null)throw new IllegalArgumentException("Remove equipment without classic combat bonuses before fighting: "+item.getName()+".");
            if(verifiedKit && !Native950MeleeEquipment.hasExpectedClassicBonuses(item.getId(),bonus))
                throw new IllegalArgumentException("The starter equipment's classic bonus profile has changed.");
            sum=sum.plus(bonus);
        }
        Item offhand=player.getEquipment().getItem(Equipment.SLOT_SHIELD);
        if(offhand!=null && offhand.getDefinitions().isMeleeTypeWeapon())throw new IllegalArgumentException("Off-hand weapons are not supported by this melee style yet.");
        int style=Native950CombatAnimations.attackStyle(weapon);
        return new Loadout(sum.attackBonusForStyle(style),sum.strBonus,sum.defenceBonusForStyle(Combat.CRUSH_STYLE),
                Native950CombatAnimations.attackSpeed(weapon),Native950CombatAnimations.attackAnimation(weapon),Native950CombatAnimations.blockAnimation(weapon));
    }
    static final class Loadout {
        final int attackBonus,strengthBonus,defenceBonus,speed,attackAnimation,blockAnimation;
        final Native950CombatStyles.Profile profile;
        Loadout(int attackBonus,int strengthBonus,int defenceBonus,int speed,int attackAnimation,int blockAnimation){
            this(attackBonus,strengthBonus,defenceBonus,speed,attackAnimation,blockAnimation,null);
        }
        Loadout(int attackBonus,int strengthBonus,int defenceBonus,int speed,int attackAnimation,int blockAnimation,Native950CombatStyles.Profile profile){
            this.profile=profile;
            if(speed<1)throw new IllegalArgumentException("Melee speed must be positive");
            this.attackBonus=attackBonus;this.strengthBonus=strengthBonus;this.defenceBonus=defenceBonus;this.speed=speed;this.attackAnimation=attackAnimation;this.blockAnimation=blockAnimation;
        }
    }
    interface Rewards {void hit(Player player,NPC npc,int damage);default void hit(Player player,NPC npc,int damage,Loadout gear){hit(player,npc,damage);}void death(NPC npc,Player owner);}
    interface Loadouts {Loadout get(Player player);}
    interface Rolls {boolean accurate(long attack,long defence);int damage(int maximum);default int nativeDamageRemainder(int rawDamage){return 0;}default boolean critical(){return false;}}
    interface Access {default Iterable<Player> players(){return java.util.Collections.emptyList();}default void followConjureTarget(NPC actor,WorldTile target){follow(actor,target);}default void worldGraphic(WorldTile tile,int graphic){}default void followConjure(NPC actor,Player owner){follow(actor,owner);}default NPC spawnConjure(Player owner,int id){throw new IllegalStateException("Conjure actor access unavailable");}default void removeConjure(NPC actor){}default boolean bounceReach(WorldTile from,Entity to){return true;}default void retire(NPC npc){}void activate(NPC npc);boolean player(Player player);boolean npc(NPC npc);boolean clear(WorldTile tile);default boolean clear(WorldTile tile,int size){return clear(tile);}boolean reach(Entity from,Entity to);default boolean rangedReach(Player from,NPC to,int range){return reach(from,to);}default boolean npcRangedReach(NPC from,Player to,int range){return reach(from,to);}default void projectile(com.rs.game.Projectile projectile){}boolean approach(Player player,NPC npc);default boolean follow(NPC npc,WorldTile target){return false;}}
    private static final class LiveRolls implements Rolls {
        public boolean accurate(long a,long d){return ThreadLocalRandom.current().nextDouble()<Rs2CombatFormula.hitChance(a,d);}
        public int damage(int maximum){return maximum<=0?0:ThreadLocalRandom.current().nextInt(maximum+1);}
        public int nativeDamageRemainder(int rawDamage){return rawDamage<=0?0:ThreadLocalRandom.current().nextInt(Rs2CombatFormula.ATARAXIA_DAMAGE_SCALE);}
        public boolean critical(){return ThreadLocalRandom.current().nextInt(10)==0;}
    }
    private static final class LiveAccess implements Access {
        public Iterable<Player> players(){return World.getPlayers();}
        public void retire(NPC npc){Native950World.getInstance().discardDeadCombatNpc(npc);}
        public void activate(NPC npc){npc.enableNative950Movement();}
        public boolean player(Player player){return World.getPlayers().get(player.getIndex())==player&&player.getRealChannel()!=null&&player.getRealChannel().isActive();}
        public boolean npc(NPC npc){return npc!=null&&World.getNPCs().get(npc.getIndex())==npc;}
        public boolean clear(WorldTile tile){return Native950MeleeReach.clearFootprint(tile,1);}
        public boolean clear(WorldTile tile,int size){return Native950MeleeReach.clearFootprint(tile,size);}
        public boolean reach(Entity a,Entity b){return Native950MeleeReach.canReach(a,b);}
        public NPC spawnConjure(Player owner,int id){
            int size=com.rs.cache.loaders.NPCDefinitions.decodeStrict947(id,com.rs.cache.Cache.STORE.getIndexes()[18].getFile(id>>>7,id&127),null).size;
            WorldTile tile=conjurePosition(owner,size,null);
            if(tile==null)throw new IllegalStateException("No clear conjure position near owner");
            NPC npc=NPC.createNative950Conjure(id,tile);Native950World.getInstance().addConjure(npc);return npc;
        }
        public void followConjure(NPC actor,Player owner){
            WorldTile tile=conjurePosition(owner,actor.getSize(),actor);
            if(tile==null){actor.resetWalkSteps();return;}
            int distance=Math.max(Math.abs(actor.getX()-owner.getX()),Math.abs(actor.getY()-owner.getY()));
            if(actor.getPlane()!=owner.getPlane()||distance>12){actor.resetWalkSteps();actor.setNextWorldTile(tile);return;}
            follow(actor,tile);
        }
        public void followConjureTarget(NPC actor,WorldTile target){
            if(!(target instanceof Entity)){follow(actor,target);return;}
            WorldTile tile=conjurePosition((Entity)target,actor.getSize(),actor);
            if(tile==null){actor.resetWalkSteps();return;}
            follow(actor,tile);
        }
        private WorldTile conjurePosition(Entity owner,int size,NPC exclude){
            java.util.List<WorldTile> occupied=new ArrayList<>();java.util.List<Integer> sizes=new ArrayList<>();
            for(NPC npc:World.getNPCs())if(npc!=null&&npc!=exclude&&!npc.hasFinished()){
                WorldTile position=npc.getNextWorldTile()==null?npc:npc.getNextWorldTile();
                if(npc.getNextWorldTile()==null&&!npc.getWalkSteps().isEmpty()){
                    Object[] steps=npc.getWalkSteps().toArray();Object[] last=(Object[])steps[steps.length-1];
                    position=new WorldTile((Integer)last[1],(Integer)last[2],npc.getPlane());
                }
                if(position.getPlane()==owner.getPlane()&&position.withinDistance(owner,16)){
                    occupied.add(position);sizes.add(npc.getSize());
                }
            }
            for(Player p:World.getPlayers())if(p!=null&&p!=owner&&!p.hasFinished()
                    &&p.getPlane()==owner.getPlane()&&p.withinDistance(owner,16)){
                occupied.add(p);sizes.add(p.getSize());
            }
            return Native950ConjureFormation.place(owner,owner.getSize(),size,occupied,sizes,Native950MeleeReach::clearFootprint);
        }
        public void removeConjure(NPC npc){Native950World.getInstance().removeConjure(npc);}
        public boolean bounceReach(WorldTile from,Entity to){return from.getPlane()==to.getPlane()&&to.clipedProjectile(from,false);}
        public boolean rangedReach(Player p,NPC n,int range){return p.getPlane()==n.getPlane()
                &&distanceToFootprint(p,n,n.getSize())>0&&distanceToFootprint(p,n,n.getSize())<=range
                &&p.clipedProjectile(n,false);}
        public boolean npcRangedReach(NPC n,Player p,int range){return p.getPlane()==n.getPlane()
                &&distanceToFootprint(p,n,n.getSize())>0&&distanceToFootprint(p,n,n.getSize())<=range&&n.clipedProjectile(p,false);}
        public void worldGraphic(WorldTile tile,int graphic){
            for(Player viewer:World.getPlayers())if(viewer!=null&&viewer.isNative950()&&viewer.isActive()&&!viewer.hasFinished()
                    &&viewer.getPlane()==tile.getPlane()&&viewer.withinDistance(tile)&&viewer.getRealChannel()!=null)
                viewer.getRealChannel().write(com.rs.network.protocol.modern950.Native950Packets.worldSpotanim(tile.getX(),tile.getY(),tile.getPlane(),graphic,0));
        }
        public void projectile(com.rs.game.Projectile projectile){Native950World.getInstance().queueProjectile(projectile);}
        public boolean follow(NPC npc,WorldTile target){
            npc.resetWalkSteps();
            if(npc.getFreezeDelay()>=Utils.currentTimeMillis() || npc.isCantFollowUnderCombat())return true;
            //910's intelligent path uses EntityStrategy for a footprint and FixedTileStrategy for home.
            // The basic variant stops adjacent to home and may stop diagonally outside950 melee reach.
            npc.calcFollow(target,npc.getRun()?2:1,true,true);
            return npc.hasWalkSteps() || (target instanceof Entity?reach(npc,(Entity)target):npc.matches(target));
        }
        public boolean approach(Player player,NPC npc){
            // A wall may make the closest side unreachable even though another side is open.
            // Try each collision-valid side in distance order; RouteFinder buffers are consumed
            // immediately, before another route request can replace them.
            java.util.List<WorldTile> sides=Native950MeleeReach.contactTiles(npc,npc.getSize());
            sides.sort((a,b)->Integer.compare(distance(player,a),distance(player,b)));
            player.resetWalkSteps();
            for(WorldTile side:sides){
                int count=RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER,player.getX(),player.getY(),player.getPlane(),1,new FixedTileStrategy(side.getX(),side.getY()),false);
                if(count<0||RouteFinder.lastIsAlternative())continue;
                int[] xs=RouteFinder.getLastPathBufferX(),ys=RouteFinder.getLastPathBufferY();int remaining=32;
                for(int step=count-1;step>=0&&remaining>0;step--){if(!player.addWalkSteps(xs[step],ys[step],remaining,true))break;remaining=32-player.getWalkSteps().size();}
                if(player.hasWalkSteps()||reach(player,npc))return true;
            }
            return false;
        }
    }
    private static final class Fighter {
        final NPC npc;final Native950NpcCombatProfile profile;final WorldTile home;
        final java.util.List<NpcStrike> strikes=new ArrayList<>();
        Player target;boolean attacking,retaliating,returning,outOfSupplies,training,respawnAllowed;int approachTicks,followFailures;long nextAttack,hideAt,respawnAt,stunnedUntil;
        Fighter(NPC npc,Native950NpcCombatProfile profile){this.npc=npc;this.profile=profile;home=new WorldTile(npc);}
    }
    private static final class NpcStrike {
        final long due;final int damage,style;final Player target;
        NpcStrike(long due,int damage,Player target,int style){this.due=due;this.damage=damage;this.target=target;this.style=style;}
    }
    private static final class DashImpact {
        final WorldTile tile;final Loadout gear;final long expires;
        DashImpact(WorldTile tile,Loadout gear,long expires){this.tile=tile;this.gear=gear;this.expires=expires;}
    }
    private static final class DamageOverTime {
        final Fighter fighter;final int damage;int remaining;long nextTick;final Loadout gear;
        DamageOverTime(Fighter fighter,int damage,int remaining,long nextTick,Loadout gear){this.fighter=fighter;this.damage=damage;this.remaining=remaining;this.nextTick=nextTick;this.gear=gear;}
    }
    private static final class SiphonState { int healValue; }
    private static final class PendingHit {
        final Fighter fighter;final int damage;long dueTick;final Loadout gear;final int impactGraphic,channelStructure,abilityStructure,weapon,offhand;
        int projectileGraphic=-1,baseAbilityDamage;boolean released;
        SiphonState siphon;boolean siphonFinisher;
        PendingHit(Fighter fighter,int damage,long dueTick,Loadout gear){this(fighter,damage,dueTick,gear,-1);}
        PendingHit(Fighter fighter,int damage,long dueTick,Loadout gear,int impactGraphic){this(fighter,damage,dueTick,gear,impactGraphic,-1,null,-1);}
        PendingHit(Fighter fighter,int damage,long dueTick,Loadout gear,int impactGraphic,int channelStructure,Player caster){
            this(fighter,damage,dueTick,gear,impactGraphic,channelStructure,caster,-1);
        }
        PendingHit(Fighter fighter,int damage,long dueTick,Loadout gear,int impactGraphic,int channelStructure,Player caster,int abilityStructure){
            this.fighter=fighter;this.damage=damage;this.dueTick=dueTick;this.gear=gear;this.impactGraphic=impactGraphic;this.channelStructure=channelStructure;
            this.abilityStructure=abilityStructure;
            weapon=caster==null?-1:caster.getEquipment().getWeaponId();offhand=caster==null?-1:caster.getEquipment().getShieldId();
        }
    }
}
