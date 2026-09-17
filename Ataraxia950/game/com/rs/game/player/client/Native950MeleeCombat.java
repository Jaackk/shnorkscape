package com.rs.game.player.client;

import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Equipment;
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
    private final Map<Player,Map<Integer,Long>> abilityCooldowns = new IdentityHashMap<>();
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
    }
    public void attach(Player player) {
        owned(); player.setNative950Combat(this);
        // A disconnect during the visible death stage may checkpoint HP0. Resume the safe
        // return on this world; never call the legacy death/instance machinery on login.
        if (player.isDead()) deadPlayers.put(player,tick+1);
    }
    public void detach(Player player) {
        owned();stop(player);deadPlayers.remove(player);nextAttack.remove(player);player.setNative950Combat(null);
        globalCooldown.remove(player);abilityCooldowns.remove(player);
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
        if(!access.clear(npc,profile.size)) return;
        npc.setNative950CombatProfile(profile);
        access.activate(npc);
        fighters.put(npc,new Fighter(npc,profile));
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
        if(fighter!=null&&fighter.target!=null)stop(fighter.target);
        fighters.remove(npc);
    }
    public String attack(Player player, NPC npc) {
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
        if(targets.containsKey(player) && targets.get(player)!=fighter && targets.get(player).retaliating) return "You are already fighting another creature.";
        if(fighter.target!=null && fighter.target!=player) return "That creature is already fighting someone else.";
        try { Loadout initial=loadouts.get(player);if(initial.profile!=null){String cost=initial.profile.costRefusal(player);if(cost!=null)return cost;} } catch(IllegalArgumentException unsupported) { return unsupported.getMessage(); }
        if(targets.get(player)==fighter) {
            fighter.attacking=true;fighter.outOfSupplies=false;fighter.approachTicks=0;player.resetWalkSteps();player.setRouteEvent(null);
            player.setTarget(npc);player.setAttackingDelay(Utils.currentTimeMillis()+6000);
            return null; // Clicking again resumes the action without resetting either swing timer.
        }
        stop(player);
        player.getActionManager().forceStop();
        targets.put(player,fighter);fighter.target=player;fighter.attacking=true;fighter.outOfSupplies=false;fighter.approachTicks=0;
        npc.resetWalkSteps();npc.setNative950CombatEngaged(true);
        player.setRouteEvent(null);player.resetWalkSteps();
        player.setNextFaceEntity(npc);npc.setNextFaceEntity(player);
        player.setTarget(npc);player.setAttackingDelay(Utils.currentTimeMillis()+6000);
        player.setAttackedBy(npc);npc.setAttackedBy(player);
        System.out.println("[Ataraxia950] Melee target player="+player.getIndex()+" npc="+npc.getIndex()+" id="+npc.getId());
        return null;
    }
    /** Like stopping PlayerCombat in910: cancel the player's action, not NPCCombat.target. */
    public void cancelAttack(Player player) {
        owned();queuedAbilities.remove(player);Fighter fighter=targets.get(player);
        if(fighter==null)return;
        fighter.attacking=false;fighter.approachTicks=0;
        player.resetWalkSteps();player.setNextFaceEntity(null);
        if(!fighter.retaliating)stop(player);
    }
    /** Logout, death, teleport or a leash break retires both combat owners. */
    public void stop(Player player) {
        queuedAbilities.remove(player);
        owned();Fighter fighter=targets.remove(player);
        if(fighter==null)return;
        player.resetWalkSteps();player.setNextFaceEntity(null);player.setAttackedBy(null);
        if(player.getTarget()==fighter.npc)player.setTarget(null);
        fighter.target=null;fighter.attacking=false;fighter.retaliating=false;fighter.outOfSupplies=false;fighter.approachTicks=0;fighter.followFailures=0;
        fighter.npc.resetWalkSteps();fighter.npc.setNextFaceEntity(null);fighter.npc.setAttackedBy(null);
        fighter.returning=!fighter.npc.isDead() && distance(fighter.npc,fighter.home)>0;
        fighter.npc.setNative950CombatEngaged(fighter.returning);
    }
    public void clear() {
        owned();for(Player player:new ArrayList<>(targets.keySet()))stop(player);
        fighters.clear();unavailableDefinitions.clear();nextAttack.clear();deadPlayers.clear();
        queuedAbilities.clear();globalCooldown.clear();abilityCooldowns.clear();
    }
    /** A deliberately small native basic-ability slice; legacy ability callbacks never run. */
    public String ability(Player player,int structure) {
        owned();
        if(structure==14726){
            Map<Integer,Long> cooldowns=abilityCooldowns.get(player);
            if(cooldowns!=null&&tick<cooldowns.getOrDefault(structure,0L))return "Surge is cooling down.";
            String refusal=Native950Surge.use(player);
            if(refusal==null){
                abilityCooldowns.computeIfAbsent(player,p->new java.util.HashMap<>()).put(structure,tick+34);
                player.getNative950ActionBar().cooldown(player.getRealChannel(),structure,(int)Utils.currentWorldCycle(),34);
            }
            return refusal;
        }
        String refusal=abilityRefusal(player,structure);
        if(refusal!=null)return refusal;
        queuedAbilities.put(player,structure);
        return null;
    }
    static int abilityStyle(int structure){return structure==14682?0:structure==14664?1:structure==14727?2:-1;}
    int revolutionCandidate(Player player,int slots){
        owned();
        return player.getNative950ActionBar().revolutionCandidate(slots,id->abilityRefusal(player,id)==null);
    }
    /** Native combat has no legacy PlayerCombat action, so expose its live target explicitly. */
    public Entity combatTarget(Player player){
        owned();Fighter fighter=targets.get(player);
        return fighter!=null&&fighter.attacking&&fighter.target==player&&!fighter.npc.isDead()?fighter.npc:null;
    }
    private String abilityRefusal(Player player,int structure) {
        int style=abilityStyle(structure);
        if(style<0)return "That ability's full950 effect is not implemented yet. Supported: Backhand, Binding Shot, Impact and targetless Surge.";
        Fighter fighter=targets.get(player);
        if(fighter==null||!fighter.attacking||!available(player,fighter.npc)||fighter.npc.isDead()||player.getNextWorldTile()!=null||player.isStunned())
            return "Attack a supported NPC or training dummy first.";
        if(tick<globalCooldown.getOrDefault(player,0L))return "Abilities are on global cooldown.";
        Map<Integer,Long> cooldowns=abilityCooldowns.get(player);
        if(cooldowns!=null&&tick<cooldowns.getOrDefault(structure,0L))return "That ability is cooling down.";
        Loadout gear;
        try{gear=loadouts.get(player);}catch(IllegalArgumentException e){return e.getMessage();}
        if((gear.profile==null?0:gear.profile.style)!=style)return "Equip a weapon matching that ability's combat style.";
        int skill=style==0?Skills.ATTACK:style==1?Skills.RANGE:Skills.MAGIC;
        if(player.getSkills().getLevel(skill)<31)return "You need level 31 in the matching combat skill.";
        if(!playerReach(player,fighter.npc,gear))return "Move within attack range first.";
        if(player.getFoodDelay()>Utils.currentTimeMillis())return "Wait until you have finished eating.";
        String refusal=Native950Slayer.attackRefusal(player,fighter.npc);
        if(refusal==null)refusal=Native950Dungeoneering.attackRefusal(player,fighter.npc);
        return refusal!=null?refusal:gear.profile==null?null:gear.profile.costRefusal(player);
    }
    private boolean performAbility(Player player,Fighter fighter) {
        Integer structure=queuedAbilities.remove(player);
        if(structure==null)return false;
        String refusal=abilityRefusal(player,structure);
        if(refusal!=null){player.sendMessage(refusal);return false;}
        Loadout gear=loadouts.get(player);int style=abilityStyle(structure);
        if(gear.profile!=null&&!gear.profile.consume(player)){player.sendMessage("You cannot supply that ability's ammunition or runes.");return false;}
        globalCooldown.put(player,tick+3);
        abilityCooldowns.computeIfAbsent(player,p->new java.util.HashMap<>()).put(structure,tick+25);
        int cycle=(int)Utils.currentWorldCycle();
        player.getNative950ActionBar().cooldown(player.getRealChannel(),structure,cycle,25);
        player.getNative950ActionBar().cooldown(player.getRealChannel(),14881,cycle,3);
        nextAttack.put(player,tick+3);
        int skill=style==0?Skills.STRENGTH:style==1?Skills.RANGE:Skills.MAGIC;
        int level=Rs2CombatFormula.effectiveLevel(player.getSkills().getLevel(skill),0,0,1);
        int maximum=Rs2CombatFormula.meleeOrRangedMaxHit(level,gear.strengthBonus,1);
        if(gear.profile!=null)maximum=gear.profile.maxHit(player,maximum);
        // First-pass native damage uses the existing server's max-hit scale, not retail EOC parity.
        int rolled=Math.max(1,maximum/5+rolls.damage(Math.max(0,maximum-maximum/5)));
        int actual=damage(player,fighter.npc,Rs2CombatFormula.scaleDamageForAtaraxia(rolled),gear.profile==null?Hit.HitLook.MELEE_DAMAGE:gear.profile.look());
        // Param2802 is an icon sprite. Actual sequences come from param2915's weapon-family enum.
        int animation=com.rs.cache.Cache.STORE==null?-1:Native950AbilityCatalog.animation(player,structure);
        if(animation>=0){
            player.setNextAnimation(new Animation(animation));
            int effect=Native950AbilityCatalog.sequenceParam(animation,2920);
            if(effect>=0)player.setNextGraphics(new com.rs.game.Graphics(effect));
        }
        player.getCombatDefinitions().setSpecialAttackPercentage(Math.min(100,player.getCombatDefinitions().getSpecialAttackPercentage()+9));
        fighter.retaliating=!fighter.training;fighter.stunnedUntil=tick+5;
        fighter.npc.resetWalkSteps();
        if(actual>0&&!fighter.training)rewards.hit(player,fighter.npc,actual,gear);
        if(fighter.training)fighter.npc.setHitpoints(fighter.profile.hp);
        else if(fighter.npc.isDead())npcDied(fighter,player);
        swings++;
        return true;
    }
    /** Runs after input and before ordinary entity movement, on the same world tick. */
    public void beforeMovement() {
        owned();tick++;
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
        Iterator<Fighter> iterator=fighters.values().iterator();
        while(iterator.hasNext()) {
            Fighter fighter=iterator.next();NPC npc=fighter.npc;
            try {
            if(!access.npc(npc)){if(fighter.target!=null)stop(fighter.target);iterator.remove();continue;}
            if(fighter.respawnAt>0) {
                if(tick>=fighter.hideAt)npc.setNative950DeathVisible(false);
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
            Player player=fighter.target;
            // The world restore task owns regeneration; do not heal a second time here.
            if(player==null) continue;
            if(!available(player,npc) || npc.isDead() || distanceToFootprint(player,fighter.home,fighter.profile.size)>LEASH
                    || distance(npc,fighter.home)>LEASH || distanceToFootprint(player,npc,fighter.profile.size)>LEASH
                    || player.getNextWorldTile()!=null) {stop(player);continue;}
            Loadout gear;
            try{gear=loadouts.get(player);}catch(IllegalArgumentException unsupported){player.sendMessage(unsupported.getMessage());stop(player);continue;}
            boolean npcInReach=access.reach(player,npc),inReach=playerReach(player,npc,gear);
            if(inReach) {
                fighter.approachTicks=0;
                if(npcInReach){npc.resetWalkSteps();fighter.followFailures=0;}
                else if(fighter.retaliating&&tick>=fighter.stunnedUntil){if(access.follow(npc,player))fighter.followFailures=0;else if(++fighter.followFailures>APPROACH_TIMEOUT){stop(player);continue;}}
                if(fighter.attacking)player.resetWalkSteps();
            } else {
                // Same shared Entity.calcFollow path used by the ordinary910 NPCCombat.checkAll.
                if(fighter.retaliating&&tick>=fighter.stunnedUntil) {
                    if(access.follow(npc,player))fighter.followFailures=0;
                    else if(++fighter.followFailures>APPROACH_TIMEOUT){stop(player);continue;}
                }
                if(fighter.attacking && (++fighter.approachTicks>APPROACH_TIMEOUT || !access.approach(player,npc))) {
                    player.getPackets().sendGameMessage("You cannot reach that creature.");cancelAttack(player);
                }
            }
            } catch(RuntimeException failure) {failEncounter(fighter,failure);}
        }
    }
    /** Damage is committed only after every actor has moved, before any viewer frame. */
    public void afterMovement() {
        owned();
        for(Fighter fighter:new ArrayList<>(fighters.values())) {
            try {
            Player player=fighter.target;NPC npc=fighter.npc;
            if(player==null || npc.isDead())continue;
            if(!available(player,npc) || player.hasTeleported() || player.getNextWorldTile()!=null
                    || distanceToFootprint(player,fighter.home,fighter.profile.size)>LEASH
                    || distance(npc,fighter.home)>LEASH){stop(player);continue;}
            Loadout gear;
            try {gear=loadouts.get(player);}catch(IllegalArgumentException unsupported){player.getPackets().sendGameMessage(unsupported.getMessage());stop(player);continue;}
            String slayerRefusal=Native950Slayer.attackRefusal(player,npc);
            if(slayerRefusal!=null){player.sendMessage(slayerRefusal);stop(player);continue;}
            if(performAbility(player,fighter)&&npc.isDead())continue;
            Long next=nextAttack.get(player);
            if(fighter.attacking && playerReach(player,npc,gear) && (next==null||tick>=next) && player.getFoodDelay()<=Utils.currentTimeMillis()) {
                if(gear.profile!=null&&!gear.profile.consume(player)){player.sendMessage("You cannot supply the ammunition or runes for that attack.");fighter.outOfSupplies=true;cancelAttack(player);}
                else {
                nextAttack.put(player,tick+gear.speed);
                player.setNextFaceEntity(npc);player.setNextAnimation(new Animation(gear.attackAnimation));
                int skill=gear.profile==null?Skills.ATTACK:gear.profile.skill;
                int attack=Rs2CombatFormula.effectiveLevel(player.getSkills().getLevel(skill),0,3,1);
                int strength=Rs2CombatFormula.effectiveLevel(player.getSkills().getLevel(gear.profile==null||gear.profile.style==0?Skills.STRENGTH:skill),0,0,1);
                int maximum=Rs2CombatFormula.meleeOrRangedMaxHit(strength,gear.strengthBonus,1);
                if(gear.profile!=null)maximum=gear.profile.maxHit(player,maximum);
                int damage=rolls.accurate(Rs2CombatFormula.roll(attack,gear.attackBonus),
                        Rs2CombatFormula.roll(Rs2CombatFormula.npcEffectiveLevel(fighter.profile.defenceLevel),fighter.profile.meleeDefenceBonus))
                        ? Rs2CombatFormula.scaleDamageForAtaraxia(rolls.damage(maximum)) : 0;
                fighter.retaliating=!fighter.training;
                int actual=damage(player,npc,damage,gear.profile==null?Hit.HitLook.MELEE_DAMAGE:gear.profile.look());swings++;
                if(actual>0&&!fighter.training)rewards.hit(player,npc,actual,gear);
                if(fighter.training)npc.setHitpoints(fighter.profile.hp);
                if(gear.profile!=null&&gear.profile.ammoFamily==3&&player.getEquipment().getItem(Equipment.SLOT_WEAPON)==null){
                    fighter.outOfSupplies=true;cancelAttack(player);player.sendMessage("You have run out of thrown weapons.");
                }
                if(npc.isDead()) {npcDied(fighter,player);continue;}
                if(damage>0 && fighter.profile.blockAnim>=0)npc.setNextAnimation(new Animation(fighter.profile.blockAnim));
                }
            }
            if(fighter.retaliating && tick>=fighter.stunnedUntil && access.reach(npc,player) && tick>=fighter.nextAttack && !player.isDead()) {
                fighter.nextAttack=tick+fighter.profile.attackSpeed;
                npc.setNextFaceEntity(player);
                if(fighter.profile.attackAnim>=0)npc.setNextAnimation(new Animation(fighter.profile.attackAnim));
                int defence=Rs2CombatFormula.effectiveLevel(player.getSkills().getLevel(Skills.DEFENCE),0,0,1);
                int damage=rolls.accurate(Rs2CombatFormula.roll(Rs2CombatFormula.npcEffectiveLevel(fighter.profile.attackLevel),fighter.profile.meleeAttackBonus),
                        Rs2CombatFormula.roll(defence,gear.defenceBonus))
                        ? Rs2CombatFormula.scaleDamageForAtaraxia(rolls.damage(fighter.profile.maxHit/10)) : 0;
                damage(npc,player,damage);swings++;
                if(player.isDead())playerDied(player);
                else {
                    if(damage>0 && player.getNextAnimation()==null)player.setNextAnimation(new Animation(gear.blockAnimation));
                    //910 CombatScript auto-retaliation gate; never interrupt an explicit walk/skill/route.
                    if(!fighter.attacking && !fighter.outOfSupplies && player.getCombatDefinitions().isAutoRetaliate()
                            && !player.getActionManager().hasSkillWorking() && !player.hasWalkSteps()
                            && player.getRouteEvent()==null)fighter.attacking=true;
                }
            }
            } catch(RuntimeException failure) {failEncounter(fighter,failure);}
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
    private boolean playerReach(Player p,NPC n,Loadout gear){
        if(gear.profile==null||gear.profile.range<=1)return access.reach(p,n);
        return access.rangedReach(p,n,gear.profile.range);
    }
    private int damage(Entity source, Entity target, int requested) {return damage(source,target,requested,Hit.HitLook.MELEE_DAMAGE);}
    private int damage(Entity source, Entity target, int requested,Hit.HitLook look) {
        int damage=target instanceof Player && ((Player)target).isInvulnerable()
                ? 0 : Math.max(0,Math.min(target.getHitpoints(),requested));
        target.setHitpoints(target.getHitpoints()-damage);
        target.getNextHits().add(new Hit(source,damage,look,0));target.addHitBars();
        target.setAttackedBy(source);target.setAttackedByDelay(Utils.currentTimeMillis()+6000);source.setAttackingDelay(Utils.currentTimeMillis()+6000);
        if(target instanceof Player)((Player)target).refreshHitPoints();
        if(damage>0){hits++;target.addReceivedDamage(source,damage);}
        System.out.println("[Ataraxia950] Melee hit "+source.getClientIndex()+" -> "+target.getClientIndex()+" damage="+damage+" hp="+target.getHitpoints());
        return damage;
    }
    private void npcDied(Fighter fighter,Player player) {
        NPC npc=fighter.npc;stop(player);npc.resetWalkSteps();npc.setNative950CombatEngaged(true);
        npc.setNative950DeathVisible(true);npc.setNextAnimation(new Animation(fighter.profile.deathAnim));
        fighter.hideAt=tick+Math.max(fighter.profile.deathTicks,fighter.profile.deathAnimationTicks);
        fighter.respawnAt=fighter.hideAt+fighter.profile.respawnTicks;kills++;
        Player credited=npc.getMostDamageReceivedSourcePlayer();
        if(credited!=null)rewards.death(npc,credited);
        player.getPackets().sendGameMessage("You defeat the "+fighter.profile.name+".");
        System.out.println("[Ataraxia950] Melee death npc="+npc.getIndex()+" id="+npc.getId()+" respawnIn="+(fighter.respawnAt-tick)+" ticks");
    }
    private void playerDied(Player player) {
        Native950Dungeoneering.onPlayerDeath(player);
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
    interface Rolls {boolean accurate(long attack,long defence);int damage(int maximum);}
    interface Access {void activate(NPC npc);boolean player(Player player);boolean npc(NPC npc);boolean clear(WorldTile tile);default boolean clear(WorldTile tile,int size){return clear(tile);}boolean reach(Entity from,Entity to);default boolean rangedReach(Player from,NPC to,int range){return reach(from,to);}boolean approach(Player player,NPC npc);default boolean follow(NPC npc,WorldTile target){return false;}}
    private static final class LiveRolls implements Rolls {
        public boolean accurate(long a,long d){return ThreadLocalRandom.current().nextDouble()<Rs2CombatFormula.hitChance(a,d);}
        public int damage(int maximum){return maximum<=0?0:ThreadLocalRandom.current().nextInt(maximum+1);}
    }
    private static final class LiveAccess implements Access {
        public void activate(NPC npc){npc.enableNative950Movement();}
        public boolean player(Player player){return World.getPlayers().get(player.getIndex())==player&&player.getRealChannel()!=null&&player.getRealChannel().isActive();}
        public boolean npc(NPC npc){return npc!=null&&World.getNPCs().get(npc.getIndex())==npc;}
        public boolean clear(WorldTile tile){return Native950MeleeReach.clearFootprint(tile,1);}
        public boolean clear(WorldTile tile,int size){return Native950MeleeReach.clearFootprint(tile,size);}
        public boolean reach(Entity a,Entity b){return Native950MeleeReach.canReach(a,b);}
        public boolean rangedReach(Player p,NPC n,int range){return p.getPlane()==n.getPlane()
                &&distanceToFootprint(p,n,n.getSize())>0&&distanceToFootprint(p,n,n.getSize())<=range
                &&p.clipedProjectile(n,false);}
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
        Player target;boolean attacking,retaliating,returning,outOfSupplies,training;int approachTicks,followFailures;long nextAttack,hideAt,respawnAt,stunnedUntil;
        Fighter(NPC npc,Native950NpcCombatProfile profile){this.npc=npc;this.profile=profile;home=new WorldTile(npc);}
    }
}
