package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cores.CoresManager;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.network.modern.Native950GameTransport;
import com.rs.network.modern.Native950Isaac;
import com.rs.network.protocol.modern950.Native950NpcMasks;
import com.rs.network.protocol.modern950.Native950PlayerMasks;
import com.rs.network.protocol.modern950.Native950Packets;
import com.rs.network.protocol.modern950.Native950Protocol.ServerPacket;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;
import com.rs.utils.data.parsers.npcs.NPCStatsDataParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.ReferenceCountUtil;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Real-cache melee preflight; ephemeral registries/transport, no login, save or listening socket. */
public final class Native950MeleeAcceptance {
    private static int parsedFrames, ticks;
    private Native950MeleeAcceptance() { }

    public static void main(String[] args) throws Exception {
        require(args.length==1 || (args.length==2 && ("--bosses".equals(args[1]) || "--boss-stress".equals(args[1]))),
                "Usage: Native950MeleeAcceptance <950-flat-cache-directory> [--bosses|--boss-stress]");
        require(NativeCacheVerification.isEnforced(),"Cache verification must remain enabled");
        System.setProperty(Native950World.SPAWNS_PROPERTY,"false");
        System.setProperty(Native950World.LEGACY_SPAWNS_PROPERTY,"false");
        Cache.initFlatReadOnly(Paths.get(args[0]));
        Native950World world=Native950World.getInstance();
        world.execute(()->{
            require(World.getPlayers().isEmpty()&&World.getNPCs().isEmpty(),"Probe requires a fresh isolated JVM");
            NPCCombatDefinitionsDataParser.init();NPCStatsDataParser.init();
            com.rs.utils.data.parsers.npcs.NPCDropsDataParser.init();
            require(Native950CombatAnimations.verifyCache()&&Native950Hits.verifyCache()&&Native950Hitbars.verifyCache(),"Actual950 combat bindings failed");
            for(int id:new int[]{41,1017,12353,12354,12355,12357}) {
                Native950NpcCombatProfile profile=Native950NpcCombatCatalog.fromRunningCache(id);
                require(profile!=null&&profile.size==1,"Missing native combat profile "+id);
            }
            require(Native950CombatAnimations.attackAnimation(1277)==37378
                    &&Native950CombatAnimations.attackAnimation(1291)==37385
                    &&Native950CombatAnimations.attackAnimation(1321)==37385
                    &&Native950CombatAnimations.blockAnimation(1277)==18292,
                    "Actual950 bronze weapon bindings differ from reviewed combat maps");
            System.out.println("PASS: six actual-cache NPC profiles, hitmarks/bars, and bronze weapon animation bindings");
            fightAndRespawn(41,new WorldTile(3230,3298,0),1321,false);
            fightAndRespawn(12353,new WorldTile(3258,3235,0),1277,true);
            // Data-driven witnesses outside the original chicken/goblin list.
            fightAndRespawn(7873,clearHome(1),1277,false);
            fightAndRespawn(81,clearHome(2),1277,false);
            fightAndRespawn(86,clearHome(2),1277,false);
            rangedAutoWitness();
            if(args.length==2) {
                // Existing profiles at an isolated clear footprint, not a claim of boss-area access or mechanics.
                for(int boss:new int[]{2883,5666,2881,2882,6203,7133,18932}) {
                    Native950NpcCombatProfile profile=Native950NpcCombatCatalog.fromRunningCache(boss);
                    require(profile!=null,"Boss profile is not admitted: "+boss);
                    boolean stress="--boss-stress".equals(args[1]);
                    fightAndRespawn(boss,clearHome(profile.size),!stress&&boss==2883?58486:45445,false,true,!stress);
                }
            }
            playerRecovery();
            approachTarget();
            pursueAfterWalking();
            wallIsolation();
            require(World.getPlayers().isEmpty()&&World.getNPCs().isEmpty(),"Ephemeral registry cleanup failed");
            return null;
        }).get(90,TimeUnit.SECONDS);
        require(CoresManager.getNative950Scheduler().failed()==0,"World scheduler recorded a failure");
        System.out.println("PASS: "+ticks+" deterministic owner-thread melee ticks; "+parsedFrames+" parsed encrypted950 frames; no disconnected transport");
        System.out.println("LIMIT: service ticks are accelerated on the actual world owner; this checks cache, collision, state and wire composition, not wall-clock pacing, rendering or authentication; reward/pickup transport has a separate acceptance probe.");
    }

    private static void fightAndRespawn(int id,WorldTile home,int weapon,boolean bankerKit) {
        fightAndRespawn(id,home,weapon,bankerKit,false,false);
    }

    private static void rangedAutoWitness(){
        require("Shortbow".equals(Native950CacheItems.definition(841).name)
                &&"Bronze arrow".equals(Native950CacheItems.definition(882).name),"Ranged fixture identity changed");
        try(Fixture f=new Fixture(81,clearHome(2),null,841)){
            f.player.getEquipment().getItems().set(Equipment.SLOT_ARROWS,new Item(882,1));
            Native950CombatStyles.Profile profile=Native950CombatStyles.profile(841);
            Native950RangedPresentation visual=Native950RangedPresentation.resolve(f.player,profile);
            require(visual!=null&&visual.hitDelay()==2,"Paired950 arrow projectile/timing not verified");
            int hp=f.npc.getHitpoints();require(f.combat.attack(f.player,f.npc)==null,"Ranged target refused");
            f.tick();require(f.npc.getHitpoints()==hp,"Ranged hit arrived at launch");
            require(f.player.getEquipment().getItem(Equipment.SLOT_ARROWS)==null,"Final arrow was not consumed");
            f.resetMasks();f.tick();require(f.npc.getHitpoints()==hp,"Ranged hit arrived before projectile end");
            f.resetMasks();f.tick();require(f.npc.getHitpoints()<hp,"Final arrow failed to land at projectile end");
            require(f.npc.getNextHits().get(0).getLook()==Hit.HitLook.RANGE_DAMAGE,"Arrow used wrong hit style");
            System.out.println("PASS: exact950 Shortbow/Bronze arrow projectile="+visual.graphic+", final ammunition consumed, delayed range hit at end cycle50");
        }
    }

    private static void fightAndRespawn(int id,WorldTile home,int weapon,boolean bankerKit,boolean boss,boolean supplied) {
        try(Fixture f=new Fixture(id,home,null,weapon,bankerKit,supplied)) {
            int originalIndex=f.npc.getIndex(),originalHp=f.npc.getHitpoints();
            // Test-only strength avoids an intentionally guaranteed-accuracy cow winning
            // before the lifecycle assertion. No authenticated account or server stats change.
            if(originalHp>100) f.player.getSkills().setLevelWithoutRefresh(Skills.STRENGTH,20);
            if(boss) {
                for(int skill:new int[]{Skills.ATTACK,Skills.STRENGTH,Skills.RANGE,Skills.DEFENCE,Skills.HITPOINTS,Skills.MAGIC,Skills.PRAYER}) {
                    f.player.getSkills().setXpWithoutRefresh(skill,Skills.getXPForLevel(skill,99));
                    f.player.getSkills().setLevelWithoutRefresh(skill,99);
                }
                f.player.setHitpoints(f.player.getMaxHitpoints());f.player.refreshHitPoints();f.resetMasks();
                if(supplied) {
                    int style=id==2883?2:id==2882?1:0;
                    Native950DeveloperLoadouts.Loadout loadout=Native950DeveloperLoadouts.builtins().get(style);
                    f.player.getEquipment().getItems().clear();
                    for(int slot=0;slot<loadout.equipment.length;slot++)if(loadout.equipment[slot]!=null)
                        f.player.getEquipment().getItems().set(slot,new Item(loadout.equipment[slot]));
                    f.player.getInventory().items.set(0,new Item(556,2000));
                    for(int slot=1;slot<28;slot++)f.player.getInventory().items.set(slot,new Item(42251,1));
                    f.player.getPrayer().restorePrayer(990);
                    int protect=f.npc.getNative950CombatProfile().attackStyle==0?13:f.npc.getNative950CombatProfile().attackStyle==1?12:11;
                    f.player.getPrayer().switchPrayer(protect,false);
                    require(f.player.getPrayer().usingPrayer(0,protect),"Native style protection did not activate");
                }
            }
            String refusal=f.combat.attack(f.player,f.npc);
            require(refusal==null,"Actual melee target was rejected: "+refusal+"; "+f.npcState());
            int deathTick=-1,hiddenTick=-1;boolean respawned=false,retaliated=false;
            for(int step=1;step<=(boss?600:105);step++) {
                if(supplied && !f.npc.isDead() && f.player.getHitpoints()<f.player.getMaxHitpoints()*3/5
                        && f.player.getFoodDelay()<=com.rs.utils.Utils.currentTimeMillis()) {
                    for(int slot=1;slot<28;slot++)if(f.player.getInventory().getItem(slot)!=null) {
                        Native950Food.Result eaten=Native950Food.eat(f.player,f.containers,slot,42251);
                        require(eaten.accepted,"Boss food transaction refused: "+eaten.reason);break;
                    }
                }
                if(supplied&&!f.npc.isDead()){
                    int basic=id==2883?14730:id==2882?14663:14679;
                    if(f.combat.abilityRefusal(f.player,basic)==null)
                        require(f.combat.ability(f.player,basic)==null,"Boss manual basic activation failed");
                }
                boolean killedBeforeTick=f.npc.isDead();
                Tick state=f.tick();
                require(!f.player.isDead(),"Encounter killed player: npc="+id+", tick="+step+", npcHp="+f.npc.getHitpoints());
                if(state.playerHit)retaliated=true;
                if(deathTick<0 && f.npc.isDead()) {
                    deathTick=step-(killedBeforeTick?1:0);
                    java.util.List<com.rs.game.item.floor.FloorItem> drops=World.getRegion(f.npc.getRegionId()).getGroundItemsSafe();
                    require(drops.stream().anyMatch(item -> item.isNative950() && !f.initialFloor.contains(item)
                            && f.player.getUsername().equals(item.getOwner()) && item.isInvisible()),
                            "Ordinary910 death table produced no owned950 floor loot for "+id);
                    require(state.npcHit&&f.npc.isNative950DeathVisible()&&f.visible(),"Lethal hit/death presentation disappeared");
                    require(f.npc.getNextAnimation()!=null&&f.npc.getNextAnimation().getIds()[0]==f.npc.getNative950CombatProfile().deathAnim,"Wrong death animation");
                }
                if(deathTick>=0 && f.npc.isDead()&&!f.npc.isNative950DeathVisible()&&hiddenTick<0) {
                    hiddenTick=step;
                    int minimum=f.npc.getNative950CombatProfile().deathAnimationTicks;
                    require(hiddenTick-deathTick>=minimum,"Corpse removed before its resolved display lifetime");
                    require(Arrays.equals(hex("01 e0"),f.last(ServerPacket.NPC_INFO).body),"Hidden corpse must emit native retained removal");
                    require(!f.visible(),"Corpse still published after hide deadline");
                }
                if(hiddenTick>=0 && !f.npc.isDead()) {
                    String atDeadline=" at tick="+step+", hiddenTick="+hiddenTick+", expectedDelay="
                            +f.npc.getNative950CombatProfile().respawnTicks+", expectedHp="+originalHp
                            +", expectedIndex="+originalIndex+", "+f.npcState();
                    require(step-hiddenTick==f.npc.getNative950CombatProfile().respawnTicks,"Respawn cadence drifted"+atDeadline);
                    require(f.npc.getHitpoints()==originalHp&&f.npc.getIndex()==originalIndex
                            &&World.getNPCs().get(originalIndex)==f.npc,"Respawn HP/registered identity mismatch"+atDeadline);
                    require(same(f.npc,home)&&Native950MeleeReach.clearFootprint(f.npc,f.npc.getSize()),"Respawn home mismatch; expected="
                            +home.getX()+","+home.getY()+","+home.getPlane()+atDeadline);
                    // Native NPC_INFO excludes a relocated actor for its teleport frame.
                    // The authoritative respawn still occurs on time; normal movement on
                    // the following tick clears teleport state before the same-index add.
                    require(f.npc.hasTeleported()&&!f.visible(),"Respawn relocation must defer native addition"+atDeadline);
                    f.resetMasks();f.tick();
                    String afterAddition=" at tick="+(step+1)+", expectedHp="+originalHp
                            +", expectedIndex="+originalIndex+", "+f.npcState();
                    require(!f.npc.hasTeleported()&&f.visible()&&f.npc.getHitpoints()==originalHp
                            &&f.npc.getIndex()==originalIndex&&World.getNPCs().get(originalIndex)==f.npc,
                            "Respawn must publish the same living actor on the next frame"+afterAddition);
                    require(same(f.npc,home),"Respawn addition changed the authoritative home"+afterAddition);
                    respawned=true;break;
                }
                f.resetMasks();
            }
            require(deathTick>0&&hiddenTick>deathTick&&respawned&&retaliated,"Incomplete melee/retaliation/death/respawn loop for "+id);
            if(supplied) {
                require(!f.player.isInvulnerable()&&!f.player.isInfiniteCombatRunes(),"Boss gate used infinite survival/resources");
                // A sustained basic-ability rotation may suppress every auto cast; do not
                // demand rune consumption from abilities whose normal contract is rune-free.
                if(id==2883)require(Native950CombatStyles.loadout(f.player).profile.style==2,"Rex gate lost its magic loadout");
                System.out.println("BOSS resources: id="+id+", weapon="+f.player.getEquipment().getWeaponId()+", foodRemaining="+f.player.getInventory().getAmountOf(42251)+", airRunes="+f.player.getInventory().getAmountOf(556)+", finalHp="+f.player.getHitpoints());
            }
            System.out.println("PASS: NPC"+id+" weapon"+weapon+(bankerKit?" shield1173 helm1139":"")+" HP"+originalHp+" -> lethal mask -> visible corpse -> removal -> full-HP respawn -> next-frame same-index addition");
        }
    }

    private static void playerRecovery() {
        try(Fixture f=new Fixture(12353,new WorldTile(3258,3235,0),null,1291)) {
            f.player.setHitpoints(1);f.player.refreshHitPoints();f.resetMasks();
            String refusal=f.combat.attack(f.player,f.npc);
            require(refusal==null,"Recovery encounter was rejected: "+refusal+"; "+f.npcState());
            f.tick();
            require(f.player.isDead()&&f.player.isLocked(),"Lethal retaliation did not enter safe player death");
            require(f.player.getEquipment().getWeaponId()==1291,"Death removed the ephemeral weapon");
            int max=f.player.getMaxHitpoints();boolean recovered=false;
            for(int i=0;i<8;i++) {
                f.resetMasks();f.tick();
                if(!f.player.isDead()) {recovered=true;break;}
            }
            require(recovered&&!f.player.isLocked()&&f.player.getHitpoints()==max,"Player recovery did not restore health/control");
            require(same(f.player,Native950MeleeCombat.respawnTile())&&World.canMoveNPC(f.player,1),"Player recovered outside verified clear Lumbridge tile");
            require(f.player.getEquipment().getWeaponId()==1291,"Safe recovery lost equipment");
            require(f.hasVarp13537(max*10),"Full native life-points varp missing after recovery");
            System.out.println("PASS: actual melee retaliation -> player HP0/lock -> clear Lumbridge return, full HP varp and retained bronze longsword");
        }
    }

    private static void approachTarget() {
        WorldTile home=new WorldTile(3258,3235,0),start=findApproachStart(home);
        try(Fixture f=new Fixture(12353,home,start,1277)) {
            String refusal=f.combat.attack(f.player,f.npc);
            require(refusal==null,"Approach encounter was rejected: "+refusal+"; "+f.npcState());
            boolean progressed=false,approachedWithoutHit=false,hit=false;int usedTicks=0;
            for(int step=1;step<=12;step++) {
                Tick state=f.tick();usedTicks=step;
                int dx=f.npc.getX()-f.player.getX(),dy=f.npc.getY()-f.player.getY();
                boolean reachable=f.player.getPlane()==f.npc.getPlane()&&Math.abs(dx)+Math.abs(dy)==1
                        &&World.checkWalkStep(f.player.getPlane(),f.player.getX(),f.player.getY(),dx,dy,1);
                boolean swung=state.npcHit||state.playerHit
                        ||(f.player.getNextAnimation()!=null&&f.player.getNextAnimation().getIds()[0]==Native950CombatAnimations.attackAnimation(1277))
                        ||(f.npc.getNextAnimation()!=null&&f.npc.getNextAnimation().getIds()[0]==f.npc.getNative950CombatProfile().attackAnim);
                String actual=" at tick="+step+", start="+start.getX()+","+start.getY()+", player="
                        +f.player.getX()+","+f.player.getY()+", reachable="+reachable+", "+f.npcState();
                require(!swung||reachable,"Approach produced a melee swing before clear cardinal adjacency"+actual);
                require(same(f.npc,home),"Engaged goblin moved from the approach target"+actual);
                if(!same(f.player,start))progressed=true;
                if(!reachable&&!swung&&progressed)approachedWithoutHit=true;
                if(state.npcHit) {hit=true;break;}
                f.resetMasks();
            }
            require(progressed&&approachedWithoutHit&&hit,"Actual route failed to approach then attack; progressed="
                    +progressed+", noHitApproach="+approachedWithoutHit+", hit="+hit+", ticks="+usedTicks
                    +", start="+start.getX()+","+start.getY()+", player="+f.player.getX()+","+f.player.getY()+", "+f.npcState());
            System.out.println("PASS: actual950 route from "+start.getX()+","+start.getY()+" approached goblin "+home.getX()+","+home.getY()
                    +" in "+usedTicks+" ticks; movement preceded the first collision-valid adjacent swing");
        }
    }

    private static void pursueAfterWalking() {
        WorldTile home=new WorldTile(3258,3235,0);
        try(Fixture f=new Fixture(12353,home,null,1277)) {
            f.player.getCombatDefinitions().setAutoRetaliate(false);
            require(f.combat.attack(f.player,f.npc)==null,"Pursuit initial attack rejected");
            f.tick();f.resetMasks();
            int npcHp=f.npc.getHitpoints(),playerHp=f.player.getHitpoints();
            f.combat.cancelAttack(f.player);
            WorldTile goal=findApproachStart(home);
            require(f.player.calcFollow(goal,32,true,true)&&f.player.hasWalkSteps(),"No explicit escape route");
            boolean chased=false,hitAgain=false;
            for(int step=0;step<25;step++) {
                f.tick();
                require(f.npc.getHitpoints()==npcHp,"Walking/disabled auto-retaliation restarted player attacks");
                require(!f.player.isDead(),"Pursuit probe unexpectedly died");
                if(!same(f.npc,home))chased=true;
                if(f.player.getHitpoints()<playerHp)hitAgain=true;
                boolean reached=same(f.player,goal)&&Native950MeleeReach.canReach(f.player,f.npc);
                f.resetMasks();if(reached&&chased&&hitAgain)break;
            }
            require(same(f.player,goal)&&chased&&hitAgain&&f.npc.isNative950CombatEngaged(),
                    "Explicit walk lost NPC pursuit/retaliation: "+f.npcState());
            f.combat.stop(f.player);
            for(int step=0;step<35 && (!same(f.npc,home)||f.npc.isNative950CombatEngaged());step++) {f.tick();f.resetMasks();}
            require(same(f.npc,home)&&!f.npc.isNative950CombatEngaged(),"Released NPC did not return to its exact home");
            System.out.println("PASS: explicit player walk preserved NPC intelligent pursuit and retaliation; disabled auto-retaliate respected; exact-home return");
        }
    }

    private static WorldTile findApproachStart(WorldTile home) {
        World.getRegion(home.getRegionId(),true);
        // Search only the 11x11 square around the known clear home. Every visited
        // edge is traversable in both directions, so the selected start has a
        // proven route back without using the production approach implementation.
        List<WorldTile> queue=new ArrayList<WorldTile>();queue.add(home);
        java.util.Set<Integer> seen=new java.util.HashSet<Integer>();seen.add((home.getX()<<14)|home.getY());
        for(int cursor=0;cursor<queue.size();cursor++) {
            WorldTile tile=queue.get(cursor);
            if(Math.max(Math.abs(tile.getX()-home.getX()),Math.abs(tile.getY()-home.getY()))==5)return tile;
            for(int[] d:new int[][]{{1,0},{0,1},{-1,0},{0,-1}}) {
                int x=tile.getX()+d[0],y=tile.getY()+d[1];
                if(Math.abs(x-home.getX())>5||Math.abs(y-home.getY())>5||seen.contains((x<<14)|y))continue;
                if(!World.isFloorFree(home.getPlane(),x,y,1)
                        ||!World.checkWalkStep(home.getPlane(),tile.getX(),tile.getY(),d[0],d[1],1)
                        ||!World.checkWalkStep(home.getPlane(),x,y,-d[0],-d[1],1))continue;
                seen.add((x<<14)|y);queue.add(new WorldTile(x,y,home.getPlane()));
            }
        }
        throw new AssertionError("No collision-proven approach start five tiles from "+home.getX()+","+home.getY()+"; visited="+queue.size());
    }

    private static void wallIsolation() {
        WorldTile[] wall=findWall();
        try(Fixture f=new Fixture(12353,wall[1],wall[0],1277)) {
            String refusal=f.combat.attack(f.player,f.npc);
            require(refusal==null,"Wall fixture must enter approach state: "+refusal+"; "+f.npcState());
            int hp=f.npc.getHitpoints(),playerHp=f.player.getHitpoints();
            // Before movement has routed around this wall, the post-move damage gate must
            // reject the actual blocked cardinal edge, even though both floor tiles are clear.
            f.combat.afterMovement();f.publish();
            require(f.npc.getHitpoints()==hp&&f.player.getHitpoints()==playerHp
                    &&f.npc.getNextHits().isEmpty()&&f.player.getNextHits().isEmpty(),"Melee damage crossed actual950 wall collision");
            System.out.println("PASS: actual950 blocked edge "+wall[0].getX()+","+wall[0].getY()+" -> "+wall[1].getX()+","+wall[1].getY()+" prevents melee damage");
        }
    }

    private static WorldTile[] findWall() {
        World.getRegion(12850,true);
        for(int x=3201;x<3262;x++)for(int y=3201;y<3262;y++) {
            if(!World.isFloorFree(0,x,y,1))continue;
            for(int[] d:new int[][]{{1,0},{0,1}})if(World.isFloorFree(0,x+d[0],y+d[1],1)
                    &&!World.checkWalkStep(0,x,y,d[0],d[1],1))
                return new WorldTile[]{new WorldTile(x,y,0),new WorldTile(x+d[0],y+d[1],0)};
        }
        throw new AssertionError("No actual blocked cardinal wall edge found in Lumbridge");
    }

    private static final class Fixture implements AutoCloseable {
        final Native950GameTransport transport=new Native950GameTransport(new Native950Isaac(new int[]{9,5,0,1}),new Native950Isaac(new int[]{59,55,50,51}),Thread.currentThread());
        final Native950Isaac cipher=new Native950Isaac(new int[]{59,55,50,51});
        final EmbeddedChannel channel=new EmbeddedChannel(transport);
        final Native950EntityFrames frames=new Native950EntityFrames();
        final Native950NpcViewport view=new Native950NpcViewport();
        final List<Frame> output=new ArrayList<Frame>();
        final Native950MeleeCombat combat;
        final Native950Containers containers;
        final NPC npc;final Player player;
        final java.util.Set<com.rs.game.item.floor.FloorItem> initialFloor=java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        int preX,preY,prePlane;byte[] lastAppearanceHash;
        Fixture(int id,WorldTile home,WorldTile explicitPlayerTile,int weapon) {
            this(id,home,explicitPlayerTile,weapon,false);
        }
        Fixture(int id,WorldTile home,WorldTile explicitPlayerTile,int weapon,boolean bankerKit) {
            this(id,home,explicitPlayerTile,weapon,bankerKit,false);
        }
        Fixture(int id,WorldTile home,WorldTile explicitPlayerTile,int weapon,boolean bankerKit,boolean seeded) {
            final java.util.Random random=new java.util.Random(950L+id);
            combat=new Native950MeleeCombat(Thread.currentThread(),new Native950MeleeCombat.Rolls(){
                public boolean accurate(long attack,long defence){return !seeded || random.nextDouble()<com.rs.game.player.combat.rs2.Rs2CombatFormula.hitChance(attack,defence);}
                public int damage(int maximum){return !seeded?maximum:maximum<=0?0:random.nextInt(maximum+1);}
            });
            World.getRegion(home.getRegionId(),true);require(World.isFloorFree(home.getPlane(),home.getX(),home.getY(),1),"NPC fixture home is blocked: "+id);
            Native950NpcCombatProfile profile=Native950NpcCombatCatalog.fromRunningCache(id);
            require(profile!=null,"Missing generic profile for fixture "+id);
            initialFloor.addAll(World.getRegion(home.getRegionId()).getGroundItemsSafe());
            npc=NPC.createNative950(id,home,profile.size);World.addNative950Npc(npc);World.updateEntityRegion(npc);
            combat.register(npc);require(combat.supports(npc),"Actual profile was not admitted: "+id);
            WorldTile start=explicitPlayerTile==null?adjacent(home,npc.getSize()):explicitPlayerTile;
            require(World.isFloorFree(start.getPlane(),start.getX(),start.getY(),1),"Player fixture floor is blocked");
            player=Player.createNative950("meleeprobe",start,channel);player.setActive(true);player.setRunning(true);
            containers=new Native950Containers(player,new Native950ItemCatalog(Collections.emptyList()).withLegacyDrops());
            Native950Skilling.attach(player,containers);
            Native950World.installVarpSink(player); // same health-varp path as production attachment
            World.addNative950Player(player,1);World.updateEntityRegion(player);player.loadMapRegions();
            player.getEquipment().getItems().set(Equipment.SLOT_WEAPON,new Item(weapon,1));
            if(bankerKit) {
                require(weapon==1277,"Banker kit must include its actual bronze sword");
                player.getEquipment().getItems().set(Equipment.SLOT_SHIELD,new Item(1173,1));
                player.getEquipment().getItems().set(Equipment.SLOT_HAT,new Item(1139,1));
            }
            player.getAppearence().generateAppearenceData();
            require(player.getAppearence().getAppeareanceData()!=null,"Actual950 appearance generation failed");
            combat.attach(player);
            Native950World.SceneConfig scene=new Native950World.SceneConfig(start.getX(),start.getY(),start.getPlane(),1,7,0,0,0);
            for(Native950Packets.Packet packet:frames.admit(player,scene,Collections.singletonList(player)))channel.write(packet);
            view.synchronize(player,channel,7,false,Collections.singletonList(npc));drain();
            require(visible(),"NPC did not enter native viewport");resetMasks();output.clear();
            preX=player.getX();preY=player.getY();prePlane=player.getPlane();
            lastAppearanceHash=player.getAppearence().getMD5AppeareanceDataHash().clone();
        }
        Tick tick() {
            // This isolated probe compresses600ms ticks into milliseconds. Advance the
            // fixture's presentation deadline too; never change the global/game clock.
            try {
                java.lang.reflect.Field deadline=Entity.class.getDeclaredField("lastAnimationEnd");
                deadline.setAccessible(true);
                deadline.setLong(player,Math.max(0,deadline.getLong(player)-600));
            } catch(ReflectiveOperationException failure){throw new AssertionError(failure);}
            ticks++;preX=player.getX();preY=player.getY();prePlane=player.getPlane();
            // Advance this fixture's food deadline with the same accelerated 600ms tick.
            player.addFoodDelay(player.getFoodDelay()-com.rs.utils.Utils.currentTimeMillis()-600);
            try {
                java.lang.reflect.Field field=com.rs.game.player.Prayer.class.getDeclaredField("nextDrain");
                field.setAccessible(true);long[] deadlines=(long[])field.get(player.getPrayer());
                if(deadlines!=null)for(int i=0;i<deadlines.length;i++)if(deadlines[i]!=0)deadlines[i]-=600;
            }catch(ReflectiveOperationException failure){throw new AssertionError(failure);}
            player.getPrayer().processPrayerDrain();
            combat.beforeMovement();player.processMovement();npc.processNative950Movement();combat.afterMovement();
            Tick state=new Tick(!npc.getNextHits().isEmpty(),!player.getNextHits().isEmpty());
            if(state.npcHit) {
                require(!npc.getNextHitBars().isEmpty(),"NPC damage omitted its HP bar");
                Native950Hits.Snapshot hit=Native950Hits.fromRunningCache(npc);
                require(hit.refusals()==0&&hit.size()==npc.getNextHits().size(),"Live NPC hit snapshot was refused");
                if(npc.getNextHits().size()==1&&npc.getNextHits().get(0).getDamage()==10
                        &&!npc.getNextHits().get(0).isCriticalHit()) {
                    Native950NpcMasks.Update involved=new Native950NpcMasks.Update().hits(hit.npcHits(player.getIndex()).toArray(new Native950NpcMasks.Hit[0]),new Native950NpcMasks.Hitbar[0]);
                    Native950NpcMasks.Update observer=new Native950NpcMasks.Update().hits(hit.npcHits(2).toArray(new Native950NpcMasks.Hit[0]),new Native950NpcMasks.Hitbar[0]);
                    Hit.HitLook look=npc.getNextHits().get(0).getLook();
                    String ownType=look==Hit.HitLook.RANGE_DAMAGE?"80 88":look==Hit.HitLook.MAGIC_DAMAGE?"80 8b":"80 85";
                    String observerType=look==Hit.HitLook.RANGE_DAMAGE?"80 99":look==Hit.HitLook.MAGIC_DAMAGE?"80 9c":"80 96";
                    require(Arrays.equals(hex("00 00 20 ff "+ownType+" 64 00 00"),Native950NpcMasks.maskBlock(involved)),"Involved ordinary100-life-point hit differs from independent950 bytes");
                    require(Arrays.equals(hex("00 00 20 ff "+observerType+" 64 00 00"),Native950NpcMasks.maskBlock(observer)),"Observer hit styling differs from independent950 bytes");
                }
            }
            if(state.playerHit)require(!player.getNextHitBars().isEmpty()&&Native950Hits.fromRunningCache(player).refusals()==0,"Player retaliation omitted native hit/bar");
            publish();
            if(state.playerHit)require(hasVarp13537(player.getHitpoints()*10),"Native player health varp did not match damage");
            return state;
        }
        void publish() {
            output.clear();
            Native950NpcMasks.Update block=Native950EntityMasks.npcMasks(npc,false,player);
            byte[] expected=block==null?null:Native950NpcMasks.maskBlock(block);
            Native950PlayerMasks.Builder playerBlock=player.getNextHits().isEmpty()?null:Native950EntityMasks.playerMasks(player);
            byte[] appearanceHash=player.getAppearence().getMD5AppeareanceDataHash();
            boolean appearanceChanged=!Arrays.equals(lastAppearanceHash,appearanceHash);
            // Combat XP now changes combat level: PLAYER_INFO legitimately combines the new
            // appearance with the same tick's hit/health payload. Assert the entire combined mask.
            if(playerBlock!=null && appearanceChanged)playerBlock.appearance(player.getAppearence().getAppeareanceData());
            byte[] playerExpected=playerBlock==null?null:Native950PlayerMasks.encode(playerBlock.build());
            List<Player> players=Collections.singletonList(player);frames.beginFrames(players);
            frames.encode(new Native950Frames.Frame(player,channel,players,Collections.singletonList(npc),null,false,7,preX,preY,prePlane));
            view.synchronize(player,channel,7,false,Collections.singletonList(npc));channel.write(Native950Packets.tickEnd());drain();
            lastAppearanceHash=appearanceHash==null?null:appearanceHash.clone();
            Frame p=last(ServerPacket.PLAYER_INFO),n=last(ServerPacket.NPC_INFO);
            require(p!=null&&n!=null&&output.indexOf(p)<output.indexOf(n),"Actual950 player/NPC frame ordering failed");
            if(expected!=null&&visible())require(endsWith(n.body,expected),"NPC mask was lost between bridge and encrypted frame");
            if(playerExpected!=null&&!player.hasTeleported())require(endsWith(p.body,playerExpected),"Player hit/HP mask was lost between bridge and encrypted frame");
        }
        boolean visible(){for(int index:view.snapshot().indices)if(index==npc.getIndex())return true;return false;}
        String npcState(){return "npc="+npc.getId()+", index="+npc.getIndex()+", hp="+npc.getHitpoints()
                +", tile="+npc.getX()+","+npc.getY()+","+npc.getPlane()+", teleported="+npc.hasTeleported()
                +", deathVisible="+npc.isNative950DeathVisible()+", viewport="+Arrays.toString(view.snapshot().indices);}
        boolean hasVarp13537(int value){
            // Independent950 decode fixture: 13537=0x34e1, lowbyte+128=0x61.
            // Small carries signed value then id high/low128; large carries idLE128/intV1.
            byte[] small={(byte)value,0x34,0x61};
            byte[] large={0x61,0x34,(byte)(value>>>8),(byte)value,(byte)(value>>>24),(byte)(value>>>16)};
            for(Frame frame:output)if((frame.kind==ServerPacket.VARP_LARGE&&Arrays.equals(frame.body,large))
                    ||(value>=-128&&value<=127&&frame.kind==ServerPacket.VARP_SMALL&&Arrays.equals(frame.body,small)))return true;
            return false;
        }
        Frame last(ServerPacket kind){for(int i=output.size()-1;i>=0;i--)if(output.get(i).kind==kind)return output.get(i);return null;}
        void resetMasks(){player.resetMasks();npc.resetMasks();}
        void drain(){
            channel.flush();channel.runPendingTasks();Object message;
            while((message=channel.readOutbound())!=null)try{
                require(message instanceof ByteBuf,"950 transport did not produce bytes");ByteBuf bytes=(ByteBuf)message;
                while(bytes.isReadable()){
                    int opcode=(bytes.readUnsignedByte()-cipher.getAsInt())&255;
                    if(opcode>=128)opcode=((opcode-128)<<8)|((bytes.readUnsignedByte()-cipher.getAsInt())&255);
                    ServerPacket kind=null;for(ServerPacket row:ServerPacket.values())if(row.opcode()==opcode){kind=row;break;}
                    require(kind!=null,"Unknown950 opcode/cipher mismatch "+opcode);
                    int length=kind.size();if(length==-1)length=bytes.readUnsignedByte();else if(length==-2)length=bytes.readUnsignedShort();
                    require(length>=0&&length<=bytes.readableBytes(),"Invalid950 frame length for "+kind);
                    byte[] body=new byte[length];bytes.readBytes(body);output.add(new Frame(kind,body));parsedFrames++;
                }
            }finally{ReferenceCountUtil.release(message);}
            channel.checkException();require(channel.isActive()&&transport.terminalFailure()==null,"950 transport disconnected or failed");
        }
        public void close(){
            Native950Skilling.detach(player);
            combat.detach(player);combat.clear();frames.release(player);view.close();
            World.removeNative950Player(player);World.removeNative950Npc(npc);channel.finishAndReleaseAll();
        }
    }
    private static WorldTile clearHome(int size) {
        World.getRegion(12850,true);
        for(int x=3252;x<=3260;x++)for(int y=3234;y<=3242;y++) {
            WorldTile tile=new WorldTile(x,y,0);
            if(Native950MeleeReach.clearFootprint(tile,size) && !Native950MeleeReach.contactTiles(tile,size).isEmpty()) return tile;
        }
        throw new AssertionError("No clear actual950 test footprint for size "+size);
    }
    private static WorldTile adjacent(WorldTile home,int size){
        List<WorldTile> tiles=Native950MeleeReach.contactTiles(home,size);
        if(!tiles.isEmpty())return tiles.get(0);
        throw new AssertionError("Actual NPC home has no clear cardinal attack tile");
    }
    private static boolean same(WorldTile a,WorldTile b){return a.getX()==b.getX()&&a.getY()==b.getY()&&a.getPlane()==b.getPlane();}
    private static boolean endsWith(byte[] bytes,byte[] suffix){if(bytes.length<suffix.length)return false;for(int i=0;i<suffix.length;i++)if(bytes[bytes.length-suffix.length+i]!=suffix[i])return false;return true;}
    private static byte[] hex(String value){String s=value.replace(" ","");byte[] b=new byte[s.length()/2];for(int i=0;i<b.length;i++)b[i]=(byte)Integer.parseInt(s.substring(i*2,i*2+2),16);return b;}
    private static void require(boolean condition,String message){if(!condition)throw new AssertionError(message);}
    private static final class Tick{final boolean npcHit,playerHit;Tick(boolean npcHit,boolean playerHit){this.npcHit=npcHit;this.playerHit=playerHit;}}
    private static final class Frame{final ServerPacket kind;final byte[] body;Frame(ServerPacket kind,byte[] body){this.kind=kind;this.body=body;}}
}
