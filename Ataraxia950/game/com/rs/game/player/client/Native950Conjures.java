package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.game.*;
import com.rs.game.npc.NPC;
import com.rs.game.player.*;
import java.util.*;

/** World-thread companion ownership. These actors never enter hostile NPC AI. */
public final class Native950Conjures {
    static final int ECTOPLASM=55336;
    enum Kind {
        SKELETON(48302,48303,30265,10994,10992,53572,2,20,5,22,28,"Skeleton Warrior","4ec497cc72bfafed4753644d040e84fcc73975074bc252f59f27f533a64daa70"),
        ZOMBIE(48304,48305,30266,11006,11004,53574,40,60,6,18,22,"Putrid Zombie","d95325ef224fbb94e5ab83c75d9d772552d935fc54e542c6929611685ac9d521"),
        GHOST(48306,48307,30267,11018,11016,53576,40,60,7,18,22,"Vengeful Ghost","ce21d09dabc6ffd422c1123e63241ba4349612abdd3eaa3eb17363be49df4226"),
        PHANTOM(31820,32342,31142,11820,11818,55974,70,80,0,0,0,"Phantom Guardian","153ade460e4367742cb46fe9844603a9d08d06e1ac5814e7b1c5c54c686bbeb8");
        final int conjure,command,npc,activeVar,timerVar,unlock,level,commandLevel,speed,min,max;
        final String name,hash;
        Kind(int c,int cmd,int npc,int active,int timer,int unlock,int level,int commandLevel,int speed,int min,int max,String name,String hash){
            this.conjure=c;command=cmd;this.npc=npc;activeVar=active;timerVar=timer;this.unlock=unlock;
            this.level=level;this.commandLevel=commandLevel;this.speed=speed;this.min=min;this.max=max;this.name=name;this.hash=hash;
        }
        static Kind of(int structure){for(Kind k:values())if(k.conjure==structure||k.command==structure)return k;return null;}
    }
    interface Host {
        NPC spawn(Player owner,Kind kind);
        void remove(NPC actor);
        void follow(NPC actor,WorldTile destination);
        boolean valid(Player owner);
        boolean conduit(Player owner);
        NPC target(Player owner);
        boolean reach(NPC actor,NPC target,int range);
        int strike(Player owner,NPC target,int minPercent,int maxPercent);
        void area(Player owner,WorldTile origin,int radius,int min,int max);
        int abilityDamage(Player owner);
    }
    private static final class Spirit {
        final Kind kind;final NPC actor;final long expires;long nextAttack,commandUntil,explodeAt;int rage,valour;
        Spirit(Kind kind,NPC actor,long now,int duration){this.kind=kind;this.actor=actor;expires=now+duration;nextAttack=now+7;}
    }
    private final Map<Player,EnumMap<Kind,Spirit>> owners=new IdentityHashMap<>();
    private final Host host;
    Native950Conjures(Host host){this.host=host;}
    static int limit(int level){return level>=106?4:level>=84?3:level>=52?2:1;}
    static boolean handles(int structure){return structure==33965||Kind.of(structure)!=null;}
    static int base(int structure){Kind k=Kind.of(structure);return k==null?structure:k.conjure;}
    int effective(Player owner,int structure){Kind k=Kind.of(structure);return k!=null&&structure==k.conjure&&get(owner,k)!=null
            &&owner.getSkills().getLevel(Skills.NECROMANCY)>=k.commandLevel?k.command:structure;}
    private Spirit get(Player owner,Kind k){Map<Kind,Spirit> m=owners.get(owner);return m==null?null:m.get(k);}
    int count(Player owner){Map<Kind,Spirit> m=owners.get(owner);return m==null?0:m.size();}
    private List<Kind> additions(Player owner,int structure){
        List<Kind> result=new ArrayList<>();int capacity=limit(owner.getSkills().getLevel(Skills.NECROMANCY))-count(owner);
        for(Kind k:Kind.values())if((structure==33965||structure==k.conjure)&&get(owner,k)==null
                &&owner.getSkills().getLevel(Skills.NECROMANCY)>=k.level&&result.size()<capacity)result.add(k);
        return result;
    }
    String refusal(Player owner,int structure){
        if(!handles(structure))return null;
        if(!host.conduit(owner))return "Equip an underworld conduit to maintain your conjures.";
        Kind k=Kind.of(structure);
        if(k!=null&&structure==k.command&&k!=Kind.ZOMBIE&&host.target(owner)==null)return "Attack a supported target before commanding this conjure.";
        if(k!=null&&structure==k.command)return get(owner,k)==null?"That conjure is no longer present.":get(owner,k).explodeAt>0?"That conjure is already exploding.":null;
        List<Kind> wanted=additions(owner,structure);
        if(wanted.isEmpty())return "You already have the conjures your Necromancy level permits.";
        int cost=wanted.size()*(structure==33965?2:1);
        return !owner.isInfiniteCombatRunes()&&!owner.getInventory().containsItem(ECTOPLASM,cost)?"You need "+cost+" ectoplasm.":null;
    }
    void cast(Player owner,int structure,long tick){
        Kind k=Kind.of(structure);
        if(k!=null&&structure==k.command){
            Spirit spirit=get(owner,k);
            if(spirit==null)throw new IllegalStateException("Conjure disappeared after validation");
            if(k==Kind.ZOMBIE)spirit.explodeAt=tick+4;
            else if(k==Kind.PHANTOM){NPC target=host.target(owner);if(target!=null)host.strike(owner,target,45,55);spirit.valour=0;}
            else {spirit.commandUntil=k==Kind.SKELETON?tick+10:spirit.expires;spirit.nextAttack=Math.min(spirit.nextAttack,tick+2);}
            return;
        }
        List<Kind> wanted=additions(owner,structure);List<Spirit> created=new ArrayList<>();
        try{
            for(Kind kind:wanted){
                int pact=Cache.STORE==null?0:Math.max(0,Math.min(3,owner.getVarsManager().getBitValue(53587)));
                created.add(new Spirit(kind,host.spawn(owner,kind),tick,70+pact*10));
            }
        }catch(RuntimeException failure){for(Spirit s:created)host.remove(s.actor);throw failure;}
        if(!owner.isInfiniteCombatRunes())owner.getInventory().deleteItem(ECTOPLASM,created.size()*(structure==33965?2:1));
        EnumMap<Kind,Spirit> spirits=owners.computeIfAbsent(owner,p->new EnumMap<>(Kind.class));
        for(Spirit spirit:created){spirits.put(spirit.kind,spirit);publish(owner,spirit,true,tick);}
    }
    void pulse(long tick){
        for(Player owner:new ArrayList<>(owners.keySet())){
            if(!host.valid(owner)||!host.conduit(owner)){clear(owner);continue;}
            for(Spirit spirit:new ArrayList<>(owners.get(owner).values())){
                NPC actor=spirit.actor;Kind k=spirit.kind;
                if(actor.hasFinished()||actor.isDead()||actor.getPlane()!=owner.getPlane()||distance(actor,owner)>24
                        ||(tick>=spirit.expires&&spirit.explodeAt==0)){dismiss(owner,spirit,tick);continue;}
                if(spirit.explodeAt>0){if(tick>=spirit.explodeAt){host.area(owner,actor,2,360,440);dismiss(owner,spirit,tick);}continue;}
                NPC target=host.target(owner);
                if(target==null||target.hasFinished()||target.isDead()){
                    if(distance(actor,owner)>2)host.follow(actor,owner);else actor.resetWalkSteps();
                    continue;
                }
                int range=k==Kind.GHOST?6:1;
                if(k==Kind.PHANTOM){if(distance(actor,owner)>2)host.follow(actor,owner);continue;}
                if(!host.reach(actor,target,range)){host.follow(actor,target);continue;}
                actor.resetWalkSteps();actor.setNextFaceEntity(target);
                if(tick<spirit.nextAttack)continue;
                boolean commanded=tick<spirit.commandUntil;
                spirit.nextAttack=tick+(commanded&&k==Kind.SKELETON?2:k.speed);
                int scale=k==Kind.SKELETON?100+3*spirit.rage:100;
                int dealt=host.strike(owner,target,k.min*scale/100,k.max*scale/100);
                if(commanded&&k==Kind.SKELETON&&!target.isDead())host.strike(owner,target,k.min*scale/100,k.max*scale/100);
                if(k==Kind.SKELETON)spirit.rage=Math.min(25,spirit.rage+1);
                if(k==Kind.GHOST){owner.heal(dealt*140/100);if(commanded)haunts.computeIfAbsent(owner,p->new IdentityHashMap<>()).put(target,tick+8);}
                Native950BugTest.event(owner,"combat","conjure-attack","kind",k,"actor",actor.getIndex(),"target",target.getIndex(),"damage",dealt,"attackAnimation","unresolved");
            }
        }
        for(Map<NPC,Long> map:haunts.values())map.entrySet().removeIf(e->e.getKey().hasFinished()||e.getKey().isDead()||tick>=e.getValue());
        haunts.entrySet().removeIf(e->e.getValue().isEmpty());
    }
    private final Map<Player,Map<NPC,Long>> haunts=new IdentityHashMap<>();
    int hauntedBonus(Player owner,NPC target,int damage,long tick){Map<NPC,Long> map=haunts.get(owner);return map!=null&&tick<map.getOrDefault(target,0L)?Math.min(damage/10,host.abilityDamage(owner)/5):0;}
    int absorb(Player owner,int requested){Spirit s=get(owner,Kind.PHANTOM);if(s==null||requested<=0)return requested;s.valour=Math.min(25,s.valour+1);return requested-Math.min(requested/20,host.abilityDamage(owner)/10);}
    void clear(Player owner){Map<Kind,Spirit> m=owners.get(owner);if(m!=null)for(Spirit s:new ArrayList<>(m.values()))dismiss(owner,s,0);haunts.remove(owner);}
    void clear(){for(Player p:new ArrayList<>(owners.keySet()))clear(p);haunts.clear();}
    private void dismiss(Player owner,Spirit spirit,long tick){
        Map<Kind,Spirit> m=owners.get(owner);if(m!=null){m.remove(spirit.kind);if(m.isEmpty())owners.remove(owner);}
        host.remove(spirit.actor);publish(owner,spirit,false,tick);
    }
    private void publish(Player owner,Spirit spirit,boolean active,long tick){
        owner.getVarsManager().sendVar(spirit.kind.activeVar,active?1:0);
        if(owner.getRealChannel()!=null){
            if(owner.getSkills().getLevel(Skills.NECROMANCY)>=spirit.kind.commandLevel)
                owner.getRealChannel().write(com.rs.network.protocol.modern950.Native950Packets.varbitSmall(spirit.kind.unlock,1));
            owner.getNative950ActionBar().refreshTransforms(owner);
        }
        Native950BugTest.event(owner,"combat",active?"conjure-created":"conjure-dismissed","kind",spirit.kind,"actor",spirit.actor.getIndex(),"expires",spirit.expires);
    }
    private static int distance(WorldTile a,WorldTile b){return Math.max(Math.abs(a.getX()-b.getX()),Math.abs(a.getY()-b.getY()));}
    /** Only the four pinned companion assets may bypass the older NPC identity table. */
    public static String verifiedName(int npcId){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())throw new IllegalStateException("Conjures require the paired950 cache");
        for(Kind k:Kind.values())if(k.npc==npcId){
            byte[] raw=Cache.STORE.getIndexes()[18].getFile(npcId>>>7,npcId&127);
            try{StringBuilder hash=new StringBuilder();for(byte b:java.security.MessageDigest.getInstance("SHA-256").digest(raw))hash.append(String.format("%02x",b&255));
                if(!k.hash.equals(hash.toString()))throw new IllegalStateException("Changed conjure asset "+npcId);
            }catch(java.security.NoSuchAlgorithmException impossible){throw new AssertionError(impossible);}
            return k.name;
        }
        throw new IllegalArgumentException("Not an admitted conjure actor");
    }
}
