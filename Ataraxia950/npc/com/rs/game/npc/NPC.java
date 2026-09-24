package com.rs.game.npc;

import com.google.common.collect.ImmutableSet;
import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.NPCDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.ForceTalk;
import com.rs.game.HeadIcon;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.dungeon_architect.DungeonArchitectController;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.quest.deathsbounty.HusbandMichNPC;
import com.rs.game.activities.instances.GodwarsInstance;
import com.rs.game.activities.rots.npcs.AhrimNPC;
import com.rs.game.activities.rots.npcs.DharokNPC;
import com.rs.game.activities.rots.npcs.GuthanNPC;
import com.rs.game.activities.rots.npcs.KarilNPC;
import com.rs.game.activities.rots.npcs.ToragNPC;
import com.rs.game.activities.rots.npcs.VeracNPC;
import com.rs.game.activities.seasonalevents.christmas.ChristmasSeasonalEvent;
import com.rs.game.item.Item;
import com.rs.game.item.floor.LootBeamManager;
import com.rs.game.map.bossInstance.BossInstance;
import com.rs.game.npc.combat.NPCCombat;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.combat.NPCCombatDefinitionConstants;
import com.rs.game.npc.corp.CorporealBeast;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.npc.giantmole.GiantMoleInstance;
import com.rs.game.npc.godwars.armadyl.KreeArra;
import com.rs.game.npc.godwars.bandos.GeneralGraardor;
import com.rs.game.npc.godwars.saradomin.CommanderZilyana;
import com.rs.game.npc.godwars.zammorak.KrilTsutsaroth;
import com.rs.game.npc.godwars.zaros.Nex;
import com.rs.game.npc.gwd2.helwyr.Helwyr;
import com.rs.game.npc.gwd2.twinfuries.Avaryss;
import com.rs.game.npc.gwd2.twinfuries.Nymora;
import com.rs.game.npc.gwd2.vindicta.Vindicta;
import com.rs.game.npc.others.SecondaryBar;
import com.rs.game.npc.others.TormentedDemon;
import com.rs.game.npc.telos.ColoredAnimaGolem;
import com.rs.game.npc.telos.Telos;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.client.Native950NpcCombatProfile;
import com.rs.game.player.actions.herblore.herbicide.Herbicide;
import com.rs.game.player.actions.hunter.FlyingEntityHunter.FlyingEntities;
import com.rs.game.player.actions.slayer.sophanemdungeon.SophanemSlayerNPC;
import com.rs.game.player.combat.PlayerCombat;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.Magic;
import com.rs.game.player.content.SlayerTask;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.crystaltriskellion.CrystalTriskelion;
import com.rs.game.player.content.eds.EliteDungeonsConstants;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.content.items.Bonecrusher;
import com.rs.game.player.content.items.CharmingImp;
import com.rs.game.player.content.items.CoinAccumulator;
import com.rs.game.player.content.items.Defenders;
import com.rs.game.player.content.items.Ectoplasmator;
import com.rs.game.player.content.lootshare.LootShare;
import com.rs.game.player.content.petperks.PetPerk;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.player.controllers.*;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.FixedTileStrategy;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.MapAreas;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.NPCCombatDefinitionsDataParser;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.NPCDropTableRolls;
import com.rs.utils.data.parsers.npcs.NPCStatsDataParser;
import com.rs.utils.data.parsers.npcs.NPCWeaknessesDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCDirection;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import com.rs.utils.data.parsers.npcs.pojos.NPCStats;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * A class holding all NPC data.
 *
 * @author Noel
 */
public class NPC extends Entity implements Serializable {

    /**
     * The generated serial UID for Serializable.
     */
    private static final long serialVersionUID = -4794678936277614443L;

    /**
     * Integers representing NPC movement masks.
     */
    public static int NORMAL_WALK = 0x2, WATER_WALK = 0x4, FLY_WALK = 0x8;
    public WorldTile forceWalk;
    /**
     * NPC Configurations.
     */
    protected int id;
    /** Zero for legacy NPCs; native entities use their verified modern cache size. */
    private final int native947Size;
    /** P6: opted into Entity movement; combat, aggression and respawn stay refused. */
    private transient boolean native947Movable;
    /** P6: tiles either side of the respawn tile a movable native NPC may wander, 0 = stationary. */
    private transient int native947Wander;
    private transient Native950NpcCombatProfile native950CombatProfile;
    /** Explicit actual-cache identity; never authorizes another ID after a transform. */
    private transient int native950DiagnosticId = -1;
    private transient com.rs.cache.filestore.store.Store native950DiagnosticStore;
    private transient boolean native950CombatEngaged, native950DeathVisible;
    /** Read-only menu metadata; never exposes the native NPC to legacy definition consumers. */
    private transient NPCDefinitions native947MenuDefinition;
    public transient boolean spawnFrozen;
    private final int weakness;
    private final WorldTile respawnTile;
    private final int mapAreaNameHash;
    private boolean canBeAttackFromOutOfArea;
    private boolean randomwalk;
    private int[] bonuses; // NPC bonuses go up to 9
    private boolean spawned;
    private transient NPCCombat combat;
    private long lastAttackedByTarget;
    private boolean cantInteract;
    private int capDamage;
    private int lureDelay;
    private int walkType;
    private final int maxDistance;
    private boolean cantFollowUnderCombat;
    private boolean forceAgressive;
    private int forceTargetDistance;
    private boolean forceFollowClose;
    private boolean forceMultiAttacked;
    private boolean noDistanceCheck;
    private transient NPCStats stats;

    @Getter
    @Setter
    private int spawnDirection = NPCDirection.NORTHWEST.getValue();

    // npc masks
    private transient boolean changedRenderAnimation;
    private transient boolean changedModels;
    private transient int nextRenderAnimation;
    private transient SecondaryBar nextSecondaryBar;
    private transient Transformation nextTransformation;
    private transient boolean changedName;
    private transient boolean changedCombatLevel;
    private transient boolean refreshHeadIcon;
    // name changing masks
    private String name;
    private int combatLevel;
    private transient long locked;
    private boolean intelligentRouteFinder;
    private transient boolean cantSetTargetAutoRelatio;
    private transient BossInstance bossInstance; // if its a instance npc
    private final String[] gnomeTrainerForceTalk = {"That's it, straight up!", "Come on scaredy cat get across that rope!", "My granny can move faster than you!", "Move it, move it, move it!"};
    private final String[] trialAnnouncerForceTalk = {"Welcome to the world of " + Settings.SERVER_NAME + "!", "Fear Botany Bay, citizens!", "Fear the wild...", "Hmm... Who will it be today?", "Beware! You may be next.", "Who will be the next victim?", "The wild is a ruthless place!", "Muhahaha.. who's next?", "The wild has NO remorse!", "I can smell the blood from here... Ooh.", "Another death; another blood stain!"};
    private boolean combust;

    // random event stuff here
    protected transient Player randomEventTarget;
    protected transient long createTime;
    protected transient boolean stop;

    /*
     * public int getAttackStyle() { if (bonuses[10] > 0) return
     * NPCCombatDefinitionConstants.MAGE; if (bonuses[9] > 0) return
     * NPCCombatDefinitionConstants.RANGE; return
     * NPCCombatDefinitionConstants.MELEE; }
     */
    public int getAttackStyle() {
        if (bonuses[2] > 0) {
            return NPCCombatDefinitionConstants.MAGE;
        }
        if (bonuses[1] > 0) {
            return NPCCombatDefinitionConstants.RANGE;
        }
        return NPCCombatDefinitionConstants.MELEE;
    }

    public boolean isCombusting() {
        return combust;
    }

    public void setCombusting(final boolean value) {
        combust = value;
    }

    public void playSoundEffect(final int id) {
        getPossibleTargets().forEach(target -> {
            if (target instanceof Player) {
                ((Player) target).getPackets().sendSound(id, 0, 2);
            }
        });
    }

    public WorldTile getForceWalk() {
        return forceWalk;
    }

    /*
     * public int getMaxHit(int style) { int maxHit = bonuses[8] / 8; if (style ==
     * 1) maxHit = bonuses[9] / 8; else if (style == 2) maxHit = bonuses[10] / 8;
     * maxHit += getCombatLevel() * 0.5; return (int) (maxHit * 0.33);
     */
    public int getMaxHit(final int style) {
        int maxHit = bonuses[0];
        if (style == 1) {
            maxHit = bonuses[1];
        } else if (style == 2) {
            maxHit = bonuses[2];
        }
        maxHit += getCombatLevel() * 0.5;

        return (int) (maxHit * 0.33);
    }

    @Override
    public boolean canMove(final int dir) {
        return true;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    /**
     * Creates an unregistered native NPC without reading 910 definitions,
     * combat data, spawn settings or starting legacy world tasks.
     */
    public static NPC createNative950(final int definitionId, final WorldTile tile,
                                      final int verifiedSize) {
        Objects.requireNonNull(tile, "tile");
        if (definitionId < 0 || verifiedSize < 1 || verifiedSize > 255)
            throw new IllegalArgumentException("Invalid native 947 NPC definition or size");
        if (tile.getX() < 0 || tile.getX() > 16383 || tile.getY() < 0 || tile.getY() > 16383
                || tile.getPlane() < 0 || tile.getPlane() > 3)
            throw new IllegalArgumentException("Invalid native 947 NPC tile");
        return new NPC(definitionId, tile, verifiedSize);
    }

    private transient int native950ConjureId=-1;
    private transient com.rs.cache.filestore.store.Store native950ConjureStore;
    public static NPC createNative950Conjure(int id,WorldTile tile){
        String name=com.rs.game.player.client.Native950Conjures.verifiedName(id);
        NPCDefinitions d=NPCDefinitions.decodeStrict947(id,Cache.STORE.getIndexes()[18].getFile(id>>>7,id&127),null);
        NPC npc=createNative950(id,tile,d.size);
        npc.native947MenuDefinition=d;npc.name=name;npc.native950ConjureId=id;npc.native950ConjureStore=Cache.STORE;
        return npc;
    }
    public boolean isNative950Conjure(){return native950ConjureStore!=null&&native950ConjureStore==Cache.STORE&&native950ConjureId==id;}

    /** Creates a diagnostic entity from the exact installed950 file, without old-ID admission. */
    public static NPC createNative950Diagnostic(final int definitionId, final WorldTile tile) {
        if (definitionId < 0 || definitionId > 65534)
            throw new IllegalArgumentException("NPC ID must be 0-65534.");
        if (!Cache.isFlatReadOnly())
            throw new IllegalStateException("Diagnostic NPCs require the paired 950 cache.");
        com.rs.cache.filestore.store.Index[] indexes = Cache.STORE.getIndexes();
        byte[] data = indexes.length <= 18 || indexes[18] == null ? null
                : indexes[18].getFile(definitionId >>> 7, definitionId & 127);
        if (data == null) throw new IllegalArgumentException("That NPC ID is missing from the 950 cache.");
        NPCDefinitions definition = NPCDefinitions.decodeStrict947(definitionId, data, null);
        if (definition.transformTo != null)
            throw new IllegalArgumentException("That NPC has variable forms; use a concrete form's NPC ID.");
        if (definition.models == null || definition.models.length == 0)
            throw new IllegalArgumentException("That NPC has no world model to display.");
        if (definition.name == null || definition.name.trim().isEmpty())
            throw new IllegalArgumentException("That NPC has no usable cache name.");
        if (definition.size < 1 || definition.size > 255)
            throw new IllegalArgumentException("That NPC has an invalid cache size.");
        if (tile == null || tile.getX() + definition.size > 16384 || tile.getY() + definition.size > 16384)
            throw new IllegalArgumentException("That NPC's footprint crosses the world boundary.");
        NPC npc = createNative950(definitionId, tile, definition.size);
        npc.native947MenuDefinition = definition;
        npc.native950DiagnosticId = definitionId;
        npc.native950DiagnosticStore = Cache.STORE;
        return npc;
    }

    /** Cache/ID-scoped viewport admission; ordinary world spawns still use the port identity table. */
    public boolean isNative950DiagnosticDefinition() {
        return isNative950() && native950DiagnosticId == id && native950DiagnosticStore != null
                && native950DiagnosticStore == Cache.STORE && Cache.isFlatReadOnly();
    }

    private NPC(final int definitionId, final WorldTile tile, final int verifiedSize) {
        super(tile);
        id = definitionId;
        native947Size = verifiedSize;
        respawnTile = new WorldTile(tile);
        mapAreaNameHash = -1;
        maxDistance = -1;
        weakness = -1;
        combatLevel = 0;
        capDamage = -1;
        spawned = true;
        initEntity();
        setLastRegionId(-1);
        setRun(false);
        setHitpoints(1);
        // An empty movement pass initializes both directions and lastWorldTile.
        processMovement();
    }

    public final boolean isNative950() {
        return native947Size != 0;
    }

    private void requireLegacyNpc(final String operation) {
        if (isNative950())
            throw new UnsupportedOperationException("Native 947 NPC cannot use legacy " + operation);
    }

    private void requireNative950Npc(final String operation) {
        if (!isNative950())
            throw new UnsupportedOperationException("Legacy NPC cannot use native 947 " + operation);
    }

    /**
     * P6: opts a native 947 NPC into {@code Entity.processMovement} - the walk queue,
     * the collision recheck and the region update - and nothing else. The legacy
     * entry points stay refused: {@link #processEntity()} and {@link #processNPC()}
     * need {@code combat}, {@code MapAreas} and the 910 definition tables, none of
     * which a native NPC has, and {@link #getDefinitions()}, {@link #getCombatDefinitions()},
     * {@link #setNPC(int)} and the spawn/respawn path remain unsupported.
     *
     * <p>{@code Entity.needMapUpdate()} dereferences the last loaded map region tile,
     * which the native constructor deliberately leaves unset for a stationary NPC, so
     * the region set is loaded here, once, before the NPC can take its first step.
     */
    public void enableNative950Movement() {
        requireNative950Npc("movement");
        if (hasFinished())
            throw new IllegalStateException("A finished native 947 NPC cannot be made movable");
        if (native947Movable)
            return;
        loadMapRegions();
        native947Movable = true;
    }

    public Native950NpcCombatProfile getNative950CombatProfile() { return native950CombatProfile; }

    /** Explicit admission leaves ordinary native presentation NPCs at their inert HP1 default. */
    public void setNative950CombatProfile(final Native950NpcCombatProfile profile) {
        requireNative950Npc("combat profile");
        Objects.requireNonNull(profile,"profile");
        if(profile.npcId!=id || profile.size!=native947Size)
            throw new IllegalArgumentException("Combat profile does not match native NPC identity/size");
        if(hasFinished()) throw new IllegalStateException("Finished native NPC cannot enter combat");
        if(native950CombatProfile!=null) {
            if(native950CombatProfile==profile) return;
            throw new IllegalStateException("Native NPC combat profile is already installed");
        }
        native950CombatProfile=profile;
        combatLevel=profile.combatLevel;
        setHitpoints(profile.hp);
    }

    public boolean isNative950CombatEngaged() { return native950CombatEngaged; }
    public void setNative950CombatEngaged(final boolean engaged) {
        requireNative950Npc("combat engagement");
        if(engaged && native950CombatProfile==null)
            throw new IllegalStateException("Native NPC has no admitted combat profile");
        native950CombatEngaged=engaged;
    }

    public boolean isNative950DeathVisible() { return native950DeathVisible; }
    /** The combat owner keeps an HP0 actor visible only for its bounded death presentation. */
    public void setNative950DeathVisible(final boolean visible) {
        requireNative950Npc("death presentation");
        if(visible && ((native950CombatProfile==null && !isNative950Conjure()) || !isDead() || hasFinished()))
            throw new IllegalStateException("Death presentation requires an admitted HP0 native NPC");
        native950DeathVisible=visible;
    }

    public boolean isNative950Movable() {
        return native947Movable;
    }

    /**
     * P6: makes a movable native 947 NPC wander up to {@code radius} tiles from its
     * spawn tile. The step generator is the random-walk block of {@link #processNPC()}
     * with combat, aggression, freeze, force-walk and map-area handling removed,
     * because none of those exist for a native NPC.
     */
    public void setNative950Wander(final int radius) {
        requireNative950Npc("movement");
        if (radius < 0 || radius > 32)
            throw new IllegalArgumentException("Native 947 wander radius must fit 0..32 tiles");
        enableNative950Movement();
        native947Wander = radius;
    }

    public int getNative950Wander() {
        return native947Wander;
    }

    /**
     * P6 world-phase move for a native 947 NPC. Called once per world tick, for every
     * registered native NPC, before any viewer's frame is built. A stationary NPC
     * (the default) does nothing at all, so the verified static-banker frames are
     * unchanged.
     */
    public void processNative950Movement() {
        requireNative950Npc("movement");
        if (!native947Movable || hasFinished() || isDead())
            return;
        if (!native950CombatEngaged && native947Wander > 0 && !hasWalkSteps()) {
            boolean can = false;
            for (int i = 0; i < 2; i++) {
                if (Math.random() * 1000.0 < 100.0) {
                    can = true;
                    break;
                }
            }
            if (can) {
                final int span = native947Wander * 2;
                final int moveX = (int) Math.round(Math.random() * span - native947Wander);
                final int moveY = (int) Math.round(Math.random() * span - native947Wander);
                resetWalkSteps();
                addWalkSteps(respawnTile.getX() + moveX, respawnTile.getY() + moveY, 5, true);
            }
        }
        processMovement();
    }

    /**
     * Initializes the NPC.
     *
     * @param id The NPC ID.
     * @param tile The WorldTIle.
     * @param mapAreaNameHash The Region area name.
     * @param canBeAttackFromOutOfArea if Can be attacked out of Region.
     */
    public NPC(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea) {
        this(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, false);
    }

    /*
     * creates and adds npc
     */
    public NPC(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final boolean spawned, final boolean toSpawn) {
        super(tile);
        // Direct encounter subclasses bypass World.spawnNPC. Stop them before reading
        // legacy definitions or creating combat/region state in the native world.
        if (toSpawn && CoresManager.isNative950())
            throw new IllegalStateException("Legacy NPC construction is unavailable in the native 950 world");
        this.id = id;
        native947Size = 0;
        respawnTile = new WorldTile(tile);
        this.mapAreaNameHash = mapAreaNameHash;
        maxDistance = -1;
        this.canBeAttackFromOutOfArea = canBeAttackFromOutOfArea;
        this.spawned = spawned;
        combatLevel = -1;
        weakness = NPCWeaknessesDataParser.getWeakness(getId());
        setHitpoints(getMaxHitpoints());
        setDirection(getRespawnDirection());
        setRandomWalk(getDefinitions().movementCapabilities);
        setBonuses();
        combat = new NPCCombat(this);
        capDamage = -1;
        lureDelay = 12000;
        if (toSpawn) {
            initEntity();
            World.addNPC(this);
            World.updateEntityRegion(this);
            loadMapRegions();
        }
        checkMultiArea();
        if (id >= 14688 && id <= 14701)
            setRandomWalk(id == 14694 ? NORMAL_WALK | FLY_WALK : id == 14698 ? 0 : NORMAL_WALK);
        if (id == 14698)
            setCantFollowUnderCombat(id == 14698);
        setNPCStats();
    }

    public NPC(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final boolean spawned) {
        this(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned, true);
    }


    public void setCombat(final NPCCombat combat) {
        this.combat = combat;
    }

    public final int getWeakness() {
        return weakness;
    }

    public boolean canBeAttackedByAutoRetaliate() {
        return Utils.currentTimeMillis() - getLastAttackedByTarget() > lureDelay;
    }

    public boolean canBeAttackFromOutOfArea() {
        return canBeAttackFromOutOfArea;
    }

    private static final ImmutableSet<Integer> AGGRESSIVE_NPCS = ImmutableSet.of(1610);

    /**
     * Checks if the NPC should force aggression towards the entities around it.
     *
     * @return if should be agressive.
     */
    public boolean checkAgressivity() {
        final ArrayList<Entity> possibleTarget = getPossibleTargets();

        if (possibleTarget.isEmpty()) {
            return false;
        }

        final Entity target = possibleTarget.get(Utils.random(possibleTarget.size()));

        if (!(target instanceof Player)) {
            return false;
        }

        final Player player = ((Player) target);

        // check if the targeted player has aggressive potion effect
        boolean pzDisable = player.inPzInstance && player.isUnderCombat();
        if (player.getAggressiveDelay() > 0 && getMaxHitpoints() > 1 && !cantInteract && !forbiddenAggressiveNpc() && !pzDisable) {
            if (!PlayerCombat.canAttackNpc(player, this)) {
                return false;
            }
            setTarget(target);
            target.setAttackedBy(target);
            target.setFindTargetDelay(Utils.currentTimeMillis() + 600);
            return true;
        }

        // check if npc is aggressive
        if (AGGRESSIVE_NPCS.contains(id) || forceAgressive || getCombatDefinitions().getAggressivenessType() == NPCCombatDefinitionConstants.AGRESSIVE) {
            final int aggressionLevel = (getCombatLevel() * 2) + 1;

            // check if the targeted player has the required level to not be attacked
            // and check to make sure the targeted player isn't at their tolerance
            // if (!isToleranceAffected()) {
            setTarget(target);
            target.setAttackedBy(target);
            target.setFindTargetDelay(Utils.currentTimeMillis() + 5000);
            return true;
            // }
        }

        return false;
    }

    private boolean forbiddenAggressiveNpc() {
        return id == 24003;
    }

    protected boolean isToleranceAffected() {
        return false;
    }

    public void checkGodwarsKillcount() {
        final Player killer = getMostDamageReceivedSourcePlayer();
        if (killer == null) {
            return;
        }
        if (killer.getControlerManager().getControler() instanceof GodWars) {
            ((GodWars) killer.getControlerManager().getControler()).handleKC(this);
        }
    }

    public boolean containsItem(final int id) {
        final Item item = new Item(id);
        return containsItem(item);
    }

    public int getBonus(final int index) {
        if (index >= bonuses.length)
            return 0;
        return bonuses[index];
    }

    public int drainLevel(final int levelId, final int amount) {
        int drainLeft = amount - bonuses[levelId];
        if (drainLeft < 0) {
            drainLeft = 0;
        }
        bonuses[levelId] -= amount;
        if (bonuses[levelId] < 0) {
            bonuses[levelId] = 0;
        }
        return drainLeft;
    }

    public boolean containsItem(final Item item) {
        final Player killer = getMostDamageReceivedSourcePlayer();
        return killer.getInventory().getItems().contains(new Item(item.getId(), 1)) || killer.getEquipment().getItems().contains(new Item(item.getId(), 1));
    }

    public void deserialize() {
        if (combat == null) {
            combat = new NPCCombat(this);
        }
        spawn();
    }

    /**
     * Handles increasing NPC kill statistics.
     *
     * @param killer The killer.
     * @param name The NPC name.
     */
    protected void increaseKillStatistics(final Player killer, final String name) {
        killer.getAchievements().updateProgress(1, AchievementList.KILL_5000_MONSTERS, AchievementList.KILL_10000_MONSTERS);
        if (killer.increaseKillStatistics(name, false) != -1) {
            val includeS = name.endsWith("s") ? "" : "s";
            killer.sendMessage("You've killed a total of " + Colors.RED + killer.increaseKillStatistics(name, true) + "" + "</col> x " + Colors.RED + (name.equalsIgnoreCase("large mound") ? "WildyWyrm" : name) + includeS + "</col>.", true);
        }
    }

    protected void handlePetDrop(final Player killer, final String name) {
        Item pet = null;
        String image = null;
        if (name.equalsIgnoreCase("general graardor")) {
            final int random = killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) && GodwarsInstance.getDrops(this) != null ? 1000 : GodwarsInstance.getDrops(this) != null ? 2500 : killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 3750 : 5000;
            if (Utils.random(random) == 1) {
                pet = new Item(33806);
                image = "bandos.png";
            }

        }
        if (name.equalsIgnoreCase("k'ril tsutsaroth")) {
            final int random = killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) && GodwarsInstance.getDrops(this) != null ? 1000 : GodwarsInstance.getDrops(this) != null ? 2500 : killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 3750 : 5000;
            if (Utils.random(random) == 1) {
                pet = new Item(33805);
                image = "zammy.png";
            }
        }
        if (name.equalsIgnoreCase("commander zilyana")) {
            final int random = killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) && GodwarsInstance.getDrops(this) != null ? 1000 : GodwarsInstance.getDrops(this) != null ? 2500 : killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 3750 : 5000;
            if (Utils.random(random) == 1) {
                pet = new Item(33807);
                image = "sara.png";
            }
        }
        if (name.equalsIgnoreCase("kree'arra")) {
            final int random = killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) && GodwarsInstance.getDrops(this) != null ? 1000 : GodwarsInstance.getDrops(this) != null ? 2500 : killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 3750 : 5000;
            if (Utils.random(random) == 1) {
                pet = new Item(33804);
                image = "arma.png";
            }
        }
        if (name.equalsIgnoreCase("dagannoth prime")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1875 : 2500) == 1) {
                pet = new Item(33826);
                image = "dag.png";
            }
        }
        if (name.equalsIgnoreCase("dagannoth supreme")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1875 : 2500) == 1) {
                pet = new Item(33828);
                image = "dag.png";
            }
        }
        if (name.equalsIgnoreCase("dagannoth rex")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1875 : 2500) == 1) {
                pet = new Item(33827);
                image = "dag.png";
            }
        }
        if (name.equalsIgnoreCase("chaos elemental")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1500 : 2000) == 1) {
                pet = new Item(33811);
                image = "ellie.png";
            }
        }
        if (name.equalsIgnoreCase("kalphite queen")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1500 : 2000) == 1) {
                if (Utils.random(1) == 0) {
                    pet = new Item(33816);
                    image = "kq1.png";
                } else {
                    pet = new Item(33817);
                    image = "kq2.png";
                }
            }
        }
        if (name.equalsIgnoreCase("glacor")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1250 : 1500) == 1) {
                pet = new Item(41379);
            }
        }
        if (name.equalsIgnoreCase("glacor")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1259 : 1500) == 1) {
                pet = new Item(41380);
            }
        }
        if (name.equalsIgnoreCase("glacor")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1250 : 1500) == 1) {
                pet = new Item(41381);
            }
        }
        if (name.equalsIgnoreCase("edimmu")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 2000 : 2500) == 1) {
                pet = new Item(32730);
            }
        }

        if (name.equalsIgnoreCase("frost dragon")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1500 : 2000) == 1) {
                pet = new Item(31459);
                image = "frosty.png";
            }
        }

        if (name.equalsIgnoreCase("araxxi")) {
            final int petChance = Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 3000 : 4000);
            {
                image = "araxx.png";
                switch (petChance) {
                    case 1:
                        pet = new Item(31742);
                        break;
                    case 2012:
                        pet = new Item(31743);
                        break;
                    case 63:
                        pet = new Item(31744);
                        break;
                    case 4874:
                        pet = new Item(31745);
                        break;
                    case 0:
                        pet = new Item(31746);
                        break;
                    case 3728:
                        pet = new Item(31747);
                        break;
                    case 3729:
                        pet = new Item(31809);
                        break;
                    case 3730:
                        pet = new Item(31810);
                        break;
                    default:
                        break;
                }
            }
        }
        if (name.equalsIgnoreCase("corporeal beast")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1875 : 2500) == 1) {
                pet = new Item(33812);
                image = "corp.png";
            }
        }
        if (name.equalsIgnoreCase("king black dragon")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1875 : 2500) == 1) {
                pet = new Item(33818);
                image = "kbd.png";
            }
        }
        if (name.equalsIgnoreCase("nex")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 750 : 1000) == 1) {
                pet = new Item(33808);
                image = "nex.png";
            }
        }
        if (name.equalsIgnoreCase("queen black dragon")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 750 : 1000) == 1) {
                pet = new Item(33825);
                image = "qbd.png";
            }
        }
        if (name.equalsIgnoreCase("kalphite king")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1875 : 2500) == 1) {
                pet = new Item(33815);
                image = "kalking.png";
            }
        }
        if (name.equalsIgnoreCase("vorago")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 375 : 500) == 1) {
                pet = new Item(28630);
                image = "vitalis.png";
            }
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 185 : 250) == 1) {
                pet = new Item(33717);
                image = "bombii.png";
            }
        }
        if (name.equalsIgnoreCase("legio primus")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1875 : 2500) == 1) {
                pet = new Item(33819);
                image = "legio.png";
            }
        }
        if (name.equalsIgnoreCase("legio secundus")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1875 : 2500) == 1) {
                pet = new Item(33820);
                image = "legio.png";
            }
        }
        if (name.equalsIgnoreCase("legio tertius")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1875 : 2500) == 1) {
                pet = new Item(33821);
                image = "legio.png";
            }
        }
        if (name.equalsIgnoreCase("legio quartus")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1875 : 2500) == 1) {
                pet = new Item(33822);
                image = "legio.png";
            }
        }
        if (name.equalsIgnoreCase("legio quintus")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1875 : 2500) == 1) {
                pet = new Item(33823);
                image = "legio.png";
            }
        }
        if (name.equalsIgnoreCase("legio sextus")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1875 : 2500) == 1) {
                pet = new Item(33824);
                image = "legio.png";
            }
        }

        if (name.equalsIgnoreCase("giant mole")) {
            if (Utils.random(killer.getCurrentInstance() != null && killer.getCurrentInstance() instanceof GiantMoleInstance && killer.getCurrentInstance().isHardMode() ? killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1475 : 1600 : killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1875 : 2500) == 1) {
                pet = new Item(33813);
                image = "mole.png";
            }
        }

        if (name.equalsIgnoreCase("automaton guardian") || name.equalsIgnoreCase("automaton tracer") || name.equalsIgnoreCase("automaton generator")) {
            if (Utils.random(killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER) ? 1875 : 2500) == 1) {
                pet = new Item(27490);
                image = "cresbot.png";
            }
        }
        if (pet != null && !killer.hasItem(pet)) {
            World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + killer.getDisplayName() + " received a " + pet.getName() + " pet drop.", false);
            HcimNewsManager.getInstance().addNews(killer,"<#player> got a " + pet.getName() + " pet drop", 2);
                    killer.addItem(pet);
            killer.getAchievements().updateProgress(1, AchievementList.OBTAIN_BOSS_PET);
            /* so pets gets added to the dropcollection aswell */
            killer.getDropCollectionHandler().handleBossKills(pet, id);
            QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/" + image + "\" width=17> " + killer.getDisplayName() + " received a " + pet.getName() + " pet drop."));
        }
    }

    /**
     * Sends the Drop.
     */
    public void drop() {
        try {
            final NPCDrop[] drops = GodwarsInstance.getDrops(this) != null ? GodwarsInstance.getDrops(this) : NPCDropsDataParser.getDrops(id);
            final WorldTile tile = new WorldTile(getCoordFaceX(getSize()), getCoordFaceY(getSize()), getPlane());
            final Player killer = getMostDamageReceivedSourcePlayer();
            if (killer == null) {
                return;
            }
            killer.getInventionManager().processScavengingPerk();
            Ectoplasmator.onDrop(killer, this, tile);
            if (combatLevel > 75) {
                ChristmasSeasonalEvent.awardSmallPresent(killer, tile);
            } else if (ThreadLocalRandom.current().nextInt(25) == 0) {
                ChristmasSeasonalEvent.awardSmallPresent(killer, tile);
            }
            if (drops == null || getMaxHitpoints() == 1) {
                return;
            }
            if ((bossInstance != null && (bossInstance.isFinished() || bossInstance.getSettings().isPractiseMode()))) {
                return;
            }

            if (killer.getControlerManager().getControler() instanceof DTController) {
                return;
            }
            boolean dropCatcher = killer.getPerkManager().hasPerkActive(DonationPerk.DROP_CATCHER);
            if (getId() == 6203 || getId() == 6247 || getId() == 6222 || getId() == 6260) {
                if (Utils.random(75) == 0) {
                    val loot = TertiaryDrop.getTertiaryDrop(getId(), GodwarsInstance.getDrops(this) != null);

                    if (!LootShare.shareLoot(killer, this, loot)) {
                        killer.catchDrop(loot, () -> World.addGroundItem(loot, tile, killer, true, 180));
                    }
                }
            }

            if (killer.getPerkManager().hasPerkActive(DonationPerk.KEY_EXPERT) && Utils.random(250) == 5) {
                killer.sm(Colors.SALMON + "Your Key Expert perk has blessed you with a Crystal Key. It has been added to your bank.");
                killer.getBank().addItem(new Item(989, 1), true);
            }
            final Item[] tertiaryDrops = TertiaryDrop.getTertiaryDrop(this);
            if (tertiaryDrops != null) {
                final Item loot = tertiaryDrops[Utils.random(tertiaryDrops.length)];
                if (!LootShare.shareLoot(killer, this, loot)) {
                    killer.getDropCollectionHandler().handleBossKills(loot, id);
                    killer.catchDrop(loot, () -> World.addGroundItem(loot, tile, killer, true, 180));
                }
                sendDropMessage(killer, loot.getName());
            }
            if (Utils.random(300) == 1) {
                val loot = new Item(Utils.random(2) == 1 ? 987 : 985, 1);
                if (!LootShare.shareLoot(killer, this, loot)) {
                    killer.catchDrop(loot, () -> World.addGroundItem(loot, tile, killer, true, 180));
                }
            }
            CrystalTriskelion.sendTriskelionDrop(killer, getId(), tile);
            final String name = getDefinitions().name.toLowerCase();
            increaseKillStatistics(killer, name);
            handlePetDrop(killer, name);
            if (killer.getControlerManager().getControler() instanceof Dungeoneering || killer.getControlerManager().getControler() instanceof DungeonArchitectController) {
                Dungeoneering.handleDrop(killer, this);
                return;
            }
            if (killer.getControlerManager().getControler() instanceof Shadowreef) {

                World.sendWorldMessage(Colors.CYAN + "<shad=000000><img=6>News: " + killer.getDisplayName() + " Your Shadow Reef Loot Has Been Sent To Bank!", false);
                Shadowreef.handleDrop(killer, this);
                sendDrop(killer, new NPCDrop(995, 100, 2000), true);
            }
// sends to inventory or drops on the ground above


            // big christmas present / seasonal drop code

            /*
             * if (name.contains("kalphite king") || name.contains("araxx") ||
             * name.contains("gorvek") || name.contains("vorago") ||*
             * name.contains("vindicta") || name.contains("avary") ||
             * name.contains("nymora") || name.contains("gregoro") ||*
             * name.contains("helwyr") || name.contains("vorago") ||
             * name.contains("queen black dragon") || name.contains("chaos elemental") ||*
             * name.contains("corporeal beast") || name.contains("nex")) { if
             * (Utils.random(250) == 0) { sendDrop(killer, new NPCDrop(33611, 1, 1), true);
             * killer.getXmas().announceDrop(" has received a big Christmas present from " +
             * getDefinitions().name + "!"); } }
             *
             * if (name.contains("graardor") || name.contains("zilyana") ||*
             * name.contains("kree") || name.contains("tsutsaroth") ||
             * name.contains("king black dragon") || name.contains("kalphite queen") ||*
             * name.contains("dagannoth ")) { if (Utils.random(500) == 0) { sendDrop(killer,
             * new NPCDrop(33611, 1, 1), true);*
             * killer.getXmas().announceDrop(" has received a big Christmas present from " +
             * getDefinitions().name + "!"); } }
             */
            if (name.contains("camel warrior")) {
                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }
                if (Utils.random(300) == 1) {
                    sendDrop(killer, new NPCDrop(36019, 1, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a camel staff from a Camel Warrior!", false);
                    QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + killer.getDisplayName() + " has received a camel staff from a Camel Warrior!"));
                    HcimNewsManager.getInstance().addNews(killer, "<#player> received a camel staff from a Camel Warrior!", 3);
                }
            }

            if (this instanceof SophanemSlayerNPC && ThreadLocalRandom.current().nextInt(4) == 0) {
                sendDrop(killer, new NPCDrop(5973, 10, 20), true);
            }
            if (name.contains("ripper demon")) {
                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }
                if (Utils.random(500) == 1) {
                    sendDrop(killer, new NPCDrop(36004, 1, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a ripper claw from a Ripper Demon!", false);
                    QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + killer.getDisplayName() + " has received a ripper claw from a Ripper Demon!"));
                    HcimNewsManager.getInstance().addNews(killer, "<#player> received a ripper claw from a Ripper Demon!", 3);
                }
            }


            if (name.contains("automaton guardian")) {
                if (Utils.random(250) == 1) {
                    sendDrop(killer, new NPCDrop(27481, 1, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a pair of static gloves from Automaton Guardian!", false);
                    QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + killer.getDisplayName() + " received a pair of static gloves from Automaton Guardian!"));
                    HcimNewsManager.getInstance().addNews(killer, "<#player> received a pair of static gloves from Automaton Guardian!", 3);
                }
            }

            if (name.contains("automaton generator")) {
                if (Utils.random(250) == 1) {
                    sendDrop(killer, new NPCDrop(27484, 1, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a pair of tracking gloves from Automaton Generator!", false);
                    QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + killer.getDisplayName() + " received a pair of tracking gloves from Automaton Generator!"));
                    HcimNewsManager.getInstance().addNews(killer, "<#player> received a pair of tracking gloves from Automaton Generator!", 3);
                }
            }

            if (name.contains("automaton tracer")) {
                if (Utils.random(250) == 1) {
                    sendDrop(killer, new NPCDrop(27487, 1, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a pair of pneumatic gloves from Automaton Tracer!", false);
                    QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + killer.getDisplayName() + " received a pair of pneumatic gloves from Automaton Tracer!"));
                    HcimNewsManager.getInstance().addNews(killer, "<#player> received a pair of pneumatic gloves from Automaton Tracer!", 3);
                }
            }
            if (name.contains("moss golem")) {
                if (Utils.random(550) == 0) {
                    sendDrop(killer, new NPCDrop(41106, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Cinderbane Gloves!", false);
                }
                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }
                SolakController.handleDrop(killer, this);

                sendDrop(killer, new NPCDrop(995, 100, 8000), false);
            }
            if (name.contains("bulbous crawler")) {
                if (Utils.random(800) == 0) {
                    sendDrop(killer, new NPCDrop(41106, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Cinderbane Gloves!", false);
                }
                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }
                SolakController.handleDrop(killer, this);

                sendDrop(killer, new NPCDrop(995, 100, 4000), false);
            }
            if (name.contains("cave crawler")) { //slayer only monsters level 50 and below only

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }


                sendDrop(killer, new NPCDrop(995, 100, 2000), false);
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20267, 100, 18), false);
                }
                if (Utils.random(30) == 0) {
                    sendDrop(killer, new NPCDrop(445, 100, 11), false);
                }
                if (Utils.random(40) == 0) {
                    sendDrop(killer, new NPCDrop(560, 100, 13), false);
                }
            }
            if (name.contains("moss giant")) { //slayer only monsters level 50 and below only

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }


                sendDrop(killer, new NPCDrop(995, 100, 2000), false);
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20267, 100, 18), false);
                }
                if (Utils.random(30) == 0) {
                    sendDrop(killer, new NPCDrop(445, 100, 11), false);
                }
                if (Utils.random(40) == 0) {
                    sendDrop(killer, new NPCDrop(560, 100, 13), false);
                }
            }
            if (name.contains("skeleton")) { //slayer only monsters level 50 and below only

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }


                sendDrop(killer, new NPCDrop(995, 100, 2000), false);
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20267, 100, 18), false);
                }
                if (Utils.random(30) == 0) {
                    sendDrop(killer, new NPCDrop(445, 100, 11), false);
                }
                if (Utils.random(40) == 0) {
                    sendDrop(killer, new NPCDrop(560, 100, 13), false);
                }
            }
            if (name.contains("waterfiends")) { //slayer only monsters level 50 and below only

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }


                sendDrop(killer, new NPCDrop(995, 100, 2000), false);
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20267, 100, 18), false);
                }
                if (Utils.random(30) == 0) {
                    sendDrop(killer, new NPCDrop(445, 100, 11), false);
                }
                if (Utils.random(40) == 0) {
                    sendDrop(killer, new NPCDrop(560, 100, 13), false);
                }
            }
            if (name.contains("black demon")) { //slayer only monsters level 50 and below only

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }

                sendDrop(killer, new NPCDrop(995, 100, 2500), false);
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20269, 100, 22), false);
                }
                if (Utils.random(30) == 0) {
                    sendDrop(killer, new NPCDrop(445, 100, 16), false);
                }
                if (Utils.random(40) == 0) {
                    sendDrop(killer, new NPCDrop(560, 100, 19), false);
                }
            }
            if (name.contains("gargoyle")) { //slayer only monsters level 50 and below only

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }
                if (Utils.random(500) == 0) {
                    sendDrop(killer, new NPCDrop(1632, 100, 52, 250), true);
                    World.sendWorldMessage(Colors.DIAMOND + "<shad=000000><img=6>News: " + killer.getDisplayName() + " Has Received A Rare Drop | Uncut DragonStones.", false);
                }
                if (Utils.random(35) == 0) {
                    sendDrop(killer, new NPCDrop(47310, 100, 1), false);
                }
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(47306, 100, 1), false);
                }
                if (Utils.random(70) == 0) {
                    sendDrop(killer, new NPCDrop(47241, 100, 3), false);
                }
                if (Utils.random(80) == 0) {
                    sendDrop(killer, new NPCDrop(20269, 100, 17), false);
                }

            }
            if (name.contains("nechryael")) { //slayer only monsters level 80 and below only
                sendDrop(killer, new NPCDrop(995, 100, 1800), false);

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }
                if (Utils.random(500) == 0) {
                    sendDrop(killer, new NPCDrop(1632, 100, 52, 250), true);
                    World.sendWorldMessage(Colors.DIAMOND + "<shad=000000><img=6>News: " + killer.getDisplayName() + " Has Received A Rare Drop | Uncut DragonStones.", false);
                }
                if (Utils.random(100) == 0) {
                    sendDrop(killer, new NPCDrop(47310, 100, 1), false);
                }
                if (Utils.random(70) == 0) {
                    sendDrop(killer, new NPCDrop(47306, 100, 1), false);
                }
                if (Utils.random(120) == 0) {
                    sendDrop(killer, new NPCDrop(47241, 100, 3), false);
                }
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20269, 100, 17), false);
                }

                if (Utils.random(23) == 0) {
                    sendDrop(killer, new NPCDrop(2358, 100, 11), false);
                }
                if (Utils.random(20) == 0) {
                    sendDrop(killer, new NPCDrop(811, 100, 23), false);
                }
            }
            if (name.contains("hellhound")) { //slayer only monsters level 50 and below only

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }


                sendDrop(killer, new NPCDrop(995, 100, 2000), false);
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20267, 100, 18), false);
                }
                if (Utils.random(80) == 0) {
                    sendDrop(killer, new NPCDrop(445, 100, 11), false);
                }
                if (Utils.random(120) == 0) {
                    sendDrop(killer, new NPCDrop(560, 100, 13), false);
                }
            }
            if (name.contains("greater demon")) { //slayer only monsters level 50 and below only

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }


                sendDrop(killer, new NPCDrop(995, 100, 2000), false);
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20267, 100, 18), false);
                }
                if (Utils.random(80) == 0) {
                    sendDrop(killer, new NPCDrop(445, 100, 11), false);
                }
                if (Utils.random(120) == 0) {
                    sendDrop(killer, new NPCDrop(560, 100, 13), false);
                }
            }
            if (name.contains("turoth")) { //slayer only monsters level 50 and below only

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }


                sendDrop(killer, new NPCDrop(995, 100, 2000), false);
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20267, 100, 18), false);
                }
                if (Utils.random(80) == 0) {
                    sendDrop(killer, new NPCDrop(445, 100, 11), false);
                }
                if (Utils.random(120) == 0) {
                    sendDrop(killer, new NPCDrop(560, 100, 13), false);
                }
            }
            if (name.contains("fungal rodent")) { //slayer only monsters level 50 and below only

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }

                sendDrop(killer, new NPCDrop(995, 100, 4000), false);
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20267, 100, 18), false);
                }
                if (Utils.random(80) == 0) {
                    sendDrop(killer, new NPCDrop(445, 100, 11), false);
                }
                if (Utils.random(120) == 0) {
                    sendDrop(killer, new NPCDrop(560, 100, 13), false);
                }
            }
            if (name.contains("fire giant")) { //slayer only monsters level 50 and below only

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }

                sendDrop(killer, new NPCDrop(995, 100, 1500), false);
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20267, 100, 18), false);
                }
                if (Utils.random(80) == 0) {
                    sendDrop(killer, new NPCDrop(445, 100, 11), false);
                }
                if (Utils.random(120) == 0) {
                    sendDrop(killer, new NPCDrop(560, 100, 13), false);
                }
            }
            if (name.contains("mature grotworm")) { //slayer only monsters level 50 and below only

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }

                sendDrop(killer, new NPCDrop(995, 100, 4000), false);
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20267, 100, 18), false);
                }
                if (Utils.random(80) == 0) {
                    sendDrop(killer, new NPCDrop(445, 100, 11), false);
                }
                if (Utils.random(120) == 0) {
                    sendDrop(killer, new NPCDrop(560, 100, 13), false);
                }
            }
            if (name.contains("lesser demon")) { //slayer only monsters level 50 and below only
                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }
                sendDrop(killer, new NPCDrop(995, 100, 3000), false);
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20267, 100, 18), false);
                }
                if (Utils.random(80) == 0) {
                    sendDrop(killer, new NPCDrop(445, 100, 11), false);
                }
                if (Utils.random(120) == 0) {
                    sendDrop(killer, new NPCDrop(560, 100, 13), false);
                }
            }


            if (name.contains("cockatrice")) { //slayer only monsters level 50 and below only

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }

                sendDrop(killer, new NPCDrop(995, 100, 2000), false);
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20267, 100, 18), false);
                }
                if (Utils.random(80) == 0) {
                    sendDrop(killer, new NPCDrop(445, 100, 11), false);
                }
                if (Utils.random(120) == 0) {
                    sendDrop(killer, new NPCDrop(560, 100, 13), false);
                }
            }
            if (name.contains("basilisk")) { //slayer only monsters level 50 and below only

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }

                sendDrop(killer, new NPCDrop(995, 100, 2000), false);
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20267, 100, 18), false);
                }
                if (Utils.random(80) == 0) {
                    sendDrop(killer, new NPCDrop(445, 100, 11), false);
                }
                if (Utils.random(120) == 0) {
                    sendDrop(killer, new NPCDrop(560, 100, 13), false);
                }
            }
            if (name.contains("kurask")) { //slayer only monsters level 50 and below only

                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }

                sendDrop(killer, new NPCDrop(995, 100, 2000), false);
                if (Utils.random(50) == 0) {
                    sendDrop(killer, new NPCDrop(20267, 100, 18), false);
                }
                if (Utils.random(80) == 0) {
                    sendDrop(killer, new NPCDrop(445, 100, 11), false);
                }
                if (Utils.random(120) == 0) {
                    sendDrop(killer, new NPCDrop(560, 100, 13), false);
                }
            }

            if (name.contains("vinecrawler")) {
                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(41106, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Cinderbane Gloves!", false);
                }
                if (Utils.random(550) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }
                SolakController.handleDrop(killer, this);

                sendDrop(killer, new NPCDrop(995, 100, 2000), false);
            }
            if (name.contains("solak")) {
                if (Utils.random(550) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }
                if (Utils.random(502) == 0) {
                    sendDrop(killer, new NPCDrop(42770, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Blightbound Crossbow from a Solak World Boss!", false);
                }
                if (Utils.random(505) == 0) {
                    sendDrop(killer, new NPCDrop(42774, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Off-Hand Blightbound Crossbow from a Solak World Boss!", false);
                }
                if (Utils.random(485) == 0) {
                    sendDrop(killer, new NPCDrop(41106, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received  Cinderbane Gloves from a Solak World Boss!", false);
                }
                SolakController.handleDrop(killer, this);
                sendDrop(killer, new NPCDrop(995, 100, 1000000), false);
                World.sendWorldMessage(Colors.CYAN + "<shad=000000><img=6>News: " + killer.getDisplayName() + " Your Solak Loot Has Been Sent To Bank!", false);
            }

            if (name.contains("kalphite king")) {
                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }
                if (Defenders.getCurrentTier(killer, 1) && Utils.random(64) == 0) {
                    sendDrop(killer, new NPCDrop(36163, 1, 1), true);
                    World.sendWorldMessage(Colors.CYAN + "<shad=000000><img=6>News: " + killer.getDisplayName() + " received a perfect chitin from Kalphite King!", false);
                    HcimNewsManager.getInstance().addNews(killer, "<#player> received a perfect chitin from Kalphite King!", 3);
                            QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + killer.getDisplayName() + " received a perfect chitin from Kalphite King."));
                }
            }
            if (name.contains("nex")) {
                if (Utils.random(1000) == 0) {
                    sendDrop(killer, new NPCDrop(48792, 100, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a Ataraxia Dollar (Vote) Which is Super Rare!", false);
                }
                if (Defenders.getCurrentTier(killer, 0) && Utils.random(59) == 0) {
                    sendDrop(killer, new NPCDrop(36159, 1, 1), true);
                    World.sendWorldMessage(Colors.CYAN + "<shad=000000><img=6>News: " + killer.getDisplayName() + " received an ancient emblem from Nex!", false);
                    HcimNewsManager.getInstance().addNews(killer, "<#player> received an ancient emblem from Nex!", 3);
                            QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + killer.getDisplayName() + " received an ancient emblem from Nex!"));
                }
            }
            if (getCombatLevel() >= 90 && Utils.random(750) == 0) {
                val loot = new Item(18778);
                if (!LootShare.shareLoot(killer, this, loot)) {
                    killer.catchDrop(loot, () -> World.updateGroundItem(loot, tile, killer, 60, 0, true));
                }
            }
            final int level = Combat.getSlayerLevelForNPC(getId());
            if (level >= 78 && Utils.random(250) == 0) {
                val loot = new Item(29863);
                killer.catchDrop(loot, () -> World.updateGroundItem(loot, tile, killer, 60, 0, true));
            }

            if (isCyclops(getDefinitions().name) && Utils.random(500) > 450) {
                if (killer.getControlerManager().getControler() instanceof WarriorsGuild) {
                    val loot = new Item(whatDefender());
                    if (loot.getId() == Defenders.DRAGON_DEFENDER) {
                        killer.getAchievements().updateProgress(1, AchievementList.OBTAIN_DRAGON_DEFENDER);
                    }
                    if (!LootShare.shareLoot(killer, this, loot)) {
                        killer.catchDrop(loot, () -> World.updateGroundItem(loot, new WorldTile(this), killer, 60, 0, true));
                    }
                }
            }

            /** Clue scroll drops */
            if (isClueScrollNPC(getDefinitions().name) && Utils.random(200) <= 1) {
                if (!killer.getTreasureTrails().hasClueScrollItem()) {
                    killer.getTreasureTrails().resetCurrentClue();
                    final int itemId = getDefinitions().name.equalsIgnoreCase("Hellhound") ? 2723 : getDefinitions().name.equalsIgnoreCase("greater demon") ? 2723 : getCombatLevel() < 50 ? 2678 // combat < 50 -
                            // easy
                            : getCombatLevel() < 90 ? 2803 // 50 < combat < 90 -
                            // medium
                            : getCombatLevel() < 150 ? 2723 // 90 <
                            // combat <
                            // 150 -
                            // hard
                            : 19044; // 150 < combat - elite
                    val loot = new Item(itemId);
                    if (!LootShare.shareLoot(killer, this, loot)) {
                        killer.catchDrop(loot, () -> World.updateGroundItem(loot, new WorldTile(this), killer, 60, 0, true));
                    }
                }
            }
            if (killer.isHCIronMan() && Utils.random(50) <= 1) {
                if (name.contains("crab") || name.contains("crawling hand") || name.contains("banshee") || name.contains("slug")) {
                    val loot = new Item(10498);
                    if (!LootShare.shareLoot(killer, this, loot)) {

                        killer.catchDrop(loot, () -> World.updateGroundItem(loot, new WorldTile(this), killer, 60, 0, false));
                    }
                }
            }
            handleRingOfDeath(killer);

            NPCDropTableRolls.roll(drops, Settings.getDropQuantityRate(killer),
                    ContractHandler.isContractNpc(killer, this) ? 95 : 100,
                    drop -> !(killer.getTreasureTrails() != null
                            && killer.getTreasureTrails().isScroll(drop.getItemId())
                            && killer.getTreasureTrails().hasClueScrollItem()),
                    drop -> {
                        if (drop.getRate() != 100 || !Ectoplasmator.scatterAshes(killer, this, drop.getItemId()))
                            sendDrop(killer, drop, false);
                    });

// ===================
// CHARM DROP SECTION (weighted)
// ===================

// Charm item IDs
            final int GOLD_CHARM = 12158;
            final int GREEN_CHARM = 12159;
            final int CRIMSON_CHARM = 12160;
            final int BLUE_CHARM = 12163;

            int charmLevel = getCombatLevel(); // Use a unique variable name
            int charmChance = Math.min(charmLevel, 100); // 1% to 100%

            if (ThreadLocalRandom.current().nextInt(100) < charmChance) {
                int rand = ThreadLocalRandom.current().nextInt(100); // 0-99
                int charmId;
                if (rand < 40) {
                    charmId = GOLD_CHARM;      // 40%
                } else if (rand < 70) {
                    charmId = GREEN_CHARM;     // next 30%
                } else if (rand < 90) {
                    charmId = CRIMSON_CHARM;   // next 20%
                } else {
                    charmId = BLUE_CHARM;      // final 10%
                }
                killer.getInventory().addItem(charmId, 1);
                killer.sendMessage("You received a charm drop!");
            }

/*
            final int SILVER = 13713;
            final int GOLD = 13714;
            final int PLATINUM = 13715;
            final int DIAMOND = 13716;
            final int MASTER = 13717;

            int NPClevel = getCombatLevel(); // NPC's combat level
            int dropId = -1;
            double dropChance = 0;

// Define drop logic based on combat level
            if (NPClevel >= 1 && NPClevel <= 30) {
                dropId = SILVER;
                dropChance = Math.min(NPClevel / 30.0 * 10, 10); // 1%-10%
            } else if (NPClevel >= 31 && NPClevel <= 60) {
                dropId = GOLD;
                dropChance = Math.min((NPClevel - 30) / 30.0 * 10, 10);
            } else if (NPClevel >= 61 && NPClevel <= 90) {
                dropId = PLATINUM;
                dropChance = Math.min((NPClevel - 60) / 30.0 * 10, 10);
            } else if (NPClevel >= 91 && NPClevel <= 120) {
                dropId = DIAMOND;
                dropChance = Math.min((NPClevel - 90) / 30.0 * 10, 10);
            } else if (NPClevel >= 121) {
                dropId = MASTER;
                dropChance = Math.min(1 + (NPClevel - 121) / 29.0 * 9, 10); // 1%-10% from 121 to 150+
            }

// Attempt drop if applicable
            if (dropId != -1 && ThreadLocalRandom.current().nextDouble(100) < dropChance) {
                killer.getInventory().addItem(dropId, 1);
            }   */



            //money drop
/*
            final int CUSTOM_DROP_ID = 995;

            int monsterLevel = getCombatLevel(); // Assumes this method gives monster level
            int dropAmount = monsterLevel * 10;  // 1 item per 10 combat levels

            if (dropAmount > 0) {
                killer.getInventory().addItem(CUSTOM_DROP_ID, dropAmount);
            }   */




            SlayerTask.onKill(killer, this);
        } catch (final Exception e) {
            Logger.getGlobal().catching(e);
        } catch (final Error e) {
            Logger.getGlobal().catching(e);
        }
    }

    public void handleRingOfDeath(final Player killer) {
        if (killer.getEquipment().getRingId() == 31869 || killer.getEquipment().getRingId() == 31871 || killer.getEquipment().getRingId() == 41069 || killer.getEquipment().getRingId() == 48483) {
            if (Utils.random(2) == 1) {
                final int toRestore = getMaxHitpoints() / 150;
                final int special = killer.getCombatDefinitions().getSpecialAttackPercentage();
                int total = special + toRestore;
                if ((toRestore > 0 || toRestore <= 5) && (total < 100)) {
                    if (total > 100) {
                        total = 100;
                    } else if (total < 0) {
                        total = 0;
                    }
                    killer.getCombatDefinitions().setSpecialAttackPercentage(total);
                } else {
                    killer.getCombatDefinitions().setSpecialAttackPercentage(100);
                }
                killer.sendFilteredMessage("Your ring of death emits a sinister glow.");
            }
        }
    }

    @Override
    public void finish() {
        if (isNative950()) {
            World.removeNative950Npc(this);
            return;
        }
        if (hasFinished()) {
            return;
        }
        setFinished(true);
        World.updateEntityRegion(this);
        World.removeNPC(this);
    }

    public void forceWalkRespawnTile() {
        setForceWalk(respawnTile);
    }

    public int[] getBonuses() {
        return bonuses;
    }

    public int getCapDamage() {
        return capDamage;
    }

    public void setCapDamage(final int capDamage) {
        this.capDamage = capDamage;
    }

    public NPCCombat getCombat() {
        return combat;
    }

    public NPCCombatDefinition getCombatDefinitions() {
        requireLegacyNpc("combat definitions");
        return NPCCombatDefinitionsDataParser.getNPCCombatDefinitions(id);
    }
    
    @Override
    public int getCombatLevel() {
        return combatLevel >= 0 ? combatLevel : getDefinitions().combatLevel;
    }

    public void setCombatLevel(final int level) {
        combatLevel = getDefinitions().combatLevel == level ? -1 : level;
        changedCombatLevel = true;
    }

    public int getCustomCombatLevel() {
        return combatLevel;
    }

    public int highestDefBonus() {
        int index = 0;
        for (int i = 0; i < getBonuses().length; i++) {
            if (i < 5 || i > 7) {
                continue;
            }
            if (getBonuses()[i] > index) {
                index = getBonuses()[i];
            }
        }
        return index;
    }

    public Item getDefender() {
        int id = 8844;
        if (containsItem(8850) || containsItem(20072)) {
            id = 20072;
        } else if (containsItem(8849) || containsItem(8850)) {
            id = 8850;
        } else if (containsItem(8848)) {
            id = 8849;
        } else if (containsItem(8847)) {
            id = 8848;
        } else if (containsItem(8846)) {
            id = 8847;
        } else if (containsItem(8845)) {
            id = 8846;
        } else if (containsItem(8844)) {
            id = 8845;
        } else {
            id = 8844;
        }
        return new Item(id);
    }

    public NPCDefinitions getDefinitions() {
        requireLegacyNpc("NPC definitions");
        return NPCDefinitions.getNPCDefinitions(id);
    }

    public int getForceTargetDistance() {
        return forceTargetDistance;
    }

    public void setForceTargetDistance(final int forceTargetDistance) {
        this.forceTargetDistance = forceTargetDistance;
    }

    public int getId() {
        return id;
    }

    public int getLureDelay() {
        return lureDelay;
    }

    public void setLureDelay(final int lureDelay) {
        this.lureDelay = lureDelay;
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 0;
    }

    public int getMapAreaNameHash() {
        return mapAreaNameHash;
    }

    public int getMaxHit() {
        return getCombatDefinitions().getMaxHit();
    }

    @Override
    public int getMaxHitpoints() {
        if (isNative950()) return native950CombatProfile==null ? 1 : native950CombatProfile.hp;
        return getCombatDefinitions().getHitpoints();
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0;
    }

    public WorldTile getMiddleWorldTile() {
        final int size = getSize();
        return size == 1 ? this : new WorldTile(getCoordFaceX(size), getCoordFaceY(size), getPlane());
    }
    @Override
    public String getName() {
        return name != null ? name : (isNative950() ? native947MenuDefinition() : getDefinitions()).name;
    }

    /** Menu classification for shared handlers, without admitting combat/transform definition access. */
    public boolean hasMenuOption(final String option) {
        return (isNative950() ? native947MenuDefinition() : getDefinitions()).hasOption(option);
    }

    /** Actual native one-based menu slot; bounds checks never touch the cache. */
    public String getNative950MenuOption(final int oneBased) {
        if(oneBased<1 || oneBased>5) return null;
        return native947MenuDefinition().menuOptions[oneBased-1];
    }

    private NPCDefinitions native947MenuDefinition() {
        requireNative950Npc("menu metadata");
        if (!Cache.isFlatReadOnly())
            throw new IllegalStateException("Native 947 NPC menu metadata requires the paired read-only cache");
        if (native947MenuDefinition != null) return native947MenuDefinition;
        com.rs.cache.filestore.store.Index[] indexes = Cache.STORE.getIndexes();
        byte[] data = indexes.length <= 18 || indexes[18] == null ? null
                : indexes[18].getFile(id >>> 7, id & 127);
        if (data == null)
            throw new IllegalStateException("Missing native 947 NPC menu definition " + id);
        // Decode this exact cache file strictly rather than borrowing the shared
        // legacy/global definition array, which may contain an earlier cache's entry.
        NPCDefinitions definition = NPCDefinitions.decodeStrict947(id, data, null);
        if (definition.size != native947Size || definition.name == null || definition.name.isEmpty())
            throw new IllegalStateException("Native 947 NPC menu definition does not match entity " + id);
        native947MenuDefinition = definition;
        return definition;
    }

    @Getter
    private transient String lcName;

    public String getLowercaseName() {
        if(lcName == null) {
            lcName = getName().toLowerCase();
        }
        return lcName;
    }

    public void setName(final String string) {
        name = getDefinitions().name.equals(string) ? null : string;
        changedName = true;
    }

    private WorldTile ROTSforceWalk;

    public boolean isROTSForceWalking() {
        return ROTSforceWalk != null;
    }

    public void setROTSForceWalk(final WorldTile tile) {
        resetWalkSteps();
        ROTSforceWalk = tile;
    }

    public boolean hasROTSForceWalk() {
        return ROTSforceWalk != null;
    }

    public void setChangedRenderAnimation(final boolean changedRenderAnimation) {
        this.changedRenderAnimation = changedRenderAnimation;
    }

    public boolean hasChangedRenderAnimation() {
        return changedRenderAnimation;
    }

    public void setChangedModels(final boolean changedModels) {
        this.changedModels = changedModels;
    }

    public boolean hasChangedModels() {
        return changedModels;
    }

    private transient int[] newModels;

    public void setChangedModels(final int... models) {
        newModels = new int[models.length];
        for (int i = 0; i < models.length; i++) {
            newModels[i] = models[i];
        }
        changedModels = true;
    }

    public int[] getChangedModels() {
        return newModels;
    }

    public void setNextRenderAnimation(final int nextRenderAnimation) {
        this.nextRenderAnimation = nextRenderAnimation;
        setChangedRenderAnimation(true);
    }

    public int getNextRenderAnimation() {
        return nextRenderAnimation;
    }

    public void setNextSecondaryBar(final SecondaryBar nextSecondaryBar) {
        this.nextSecondaryBar = nextSecondaryBar;
    }

    public SecondaryBar getNextSecondaryBar() {
        return nextSecondaryBar;
    }

    public Transformation getNextTransformation() {
        return nextTransformation;
    }

    public ArrayList<Entity> getPossibleTargets(final boolean checkNPCs, final boolean checkPlayers) {
        final int size = getSize();
        final int agroRatio = 1;
        final ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        for (final int regionId : getMapRegionsIds()) {
            if (checkPlayers) {
                final List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
                if (playerIndexes != null) {
                    for (final int playerIndex : playerIndexes) {
                        final Player player = World.getPlayers().get(playerIndex);
                        int aggroDistance = forceTargetDistance > 0 ? forceTargetDistance : agroRatio;

                        // make the aggro range larger for players who are using aggro pots
                        // as long as the forceTargetDistance is the default
                        if (player != null && player.getAggressiveDelay() > 0) {
                            if (!PlayerCombat.canAttackNpc(player, this)) {
                                continue;
                            }
                            aggroDistance = Math.max(aggroDistance, 8);
                            forceMultiAttacked = true;
                            if (!player.getAggressiveOnYou().contains(this)) {
                                player.getAggressiveOnYou().add(this);
                            }
                        }

                        if (player == null || !PlayerCombat.canAttackNpc(player, this) || player.isDead() || player.hasFinished() || !player.isRunning() || player.getPlane() != getPlane() || player.getAppearence().isHidden() || !Utils.isOnRange(getX(), getY(), size, player.getX(), player.getY(), player.getSize(), aggroDistance) || (!forceMultiAttacked && (!isAtMultiArea() || !player.isAtMultiArea()) && (player.getAttackedBy() != this && (player.getAttackedByDelay() > Utils.currentTimeMillis() || player.getFindTargetDelay() > Utils.currentTimeMillis()))) || !clipedProjectile(player, false) && player.getTileHash() != getMiddleWorldTile().getTileHash() || (!forceAgressive && !Wilderness.isAtWild(this) && player.getSkills().getCombatLevelWithSummoning() >= getCombatLevel() * 2 && player.getAggressiveDelay() == 0)) {
                            continue;
                        }
                        possibleTarget.add(player);
                    }
                }
            }
            if (checkNPCs || getId() >= 20611 && getId() <= 20616) {
                final List<Integer> npcsIndexes = World.getRegion(regionId).getNPCsIndexes();
                if (npcsIndexes != null) {
                    for (final int npcIndex : npcsIndexes) {
                        final NPC npc = World.getNPCs().get(npcIndex);
                        if (npc == null || npc == this || npc.isDead() || npc.hasFinished() || !Utils.isOnRange(getX(), getY(), size, npc.getX(), npc.getY(), npc.getSize(), forceTargetDistance > 0 ? forceTargetDistance : agroRatio) || !npc.getDefinitions().hasAttackOption() || ((!isAtMultiArea() || !npc.isAtMultiArea()) && npc.getAttackedBy() != this && npc.getAttackedByDelay() > Utils.currentTimeMillis()) || !clipedProjectile(npc, false)) {
                            continue;
                        }
                        possibleTarget.add(npc);
                    }
                }
            }
        }
        return possibleTarget;
    }

    public ArrayList<Entity> getPossibleTargets() {
        return getPossibleTargets(false, true);
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0;
    }

    public int getRespawnDirection() {
        final NPCDefinitions definitions = getDefinitions();
        if (definitions.contrast << 32 != 0 && definitions.respawnDirection > 0 && definitions.respawnDirection <= 8) {
            return (4 + definitions.respawnDirection) << 11;
        }
        return 0;
    }

    public WorldTile getRespawnTile() {
        return respawnTile;
    }

    @Override
    public int getSize() {
        if (isNative950()) return native947Size;
        return getDefinitions().size;
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
//		if (capDamage != -1 && hit.getDamage() > capDamage) {
//			hit.setDamage(capDamage);
//		}
        if (name != null && name.toLowerCase().contains("dragon")) {
            final Entity source = hit.getSource();
            if (source != null && source instanceof Player && hit.getLook() == HitLook.RANGE_DAMAGE) {
                final Player p2 = (Player) source;
                if (p2.getEquipment().getAmmoId() == 21660 || p2.getEquipment().getAmmoId() == 21640) { // dragonbane arrow and bolts effect
                    hit.setDamage((int) (hit.getDamage() * 1.25));
                }
            }
        }
        if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE && !hit.isInstantKill()) {
            return;
        }
        if (!hit.isInstantKill() && hit.getSource() instanceof Player) {
            final Entity source = hit.getSource();
            if (source != null) {
                final Player p2 = (Player) source;
                p2.getControlerManager().processIncomingHit(hit, this);
                if (p2 != null && !p2.isDead() && !p2.hasFinished() && getHitpoints() >= (getMaxHitpoints() - (getMaxHitpoints() * 0.15))) {
                    p2.getActivityTimersManager().startBossTimer(this);
                }
            }
        }
        handlePrayers(hit);
    }

    public void handlePrayers(final Hit hit) {
        final Entity source = hit.getSource();
        if (source == null) {
            return;
        }
        if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE && !hit.isInstantKill()) {
            return;
        }
        if (source instanceof Player) {
            final Player p2 = (Player) source;
            p2.getPrayer().handleOutgoingHit(hit, this);
        }
    }

    public void lock(final long time) {
        locked = Utils.currentTimeMillis() + time;
    }

    public boolean hasChangedCombatLevel() {
        return changedCombatLevel;
    }

    public boolean hasDefender() {
        return containsItem(8844) || containsItem(8845) || containsItem(8846) || containsItem(8847) || containsItem(8848) || containsItem(8849) || containsItem(8850) || containsItem(20072);
    }

    public boolean hasForceWalk() {
        return forceWalk != null;
    }

    public boolean hasRandomWalk() {
        return randomwalk;
    }

    private boolean cannotMove;

    public boolean isCannotMove() {
        return cannotMove;
    }

    public void setCannotMove(final boolean canMove) {
        cannotMove = canMove;
    }

    public boolean isCantFollowUnderCombat() {
        return cantFollowUnderCombat;
    }

    public void setCantFollowUnderCombat(final boolean canFollowUnderCombat) {
        cantFollowUnderCombat = canFollowUnderCombat;
    }

    public boolean isCantInteract() {
        return cantInteract;
    }

    public void setCantInteract(final boolean cantInteract) {
        this.cantInteract = cantInteract;
        // Native melee observes this flag before approach/damage and releases its own
        // encounter; the native NPC deliberately has no legacy NPCCombat object.
        if (cantInteract && !isNative950()) {
            combat.reset();
        }
    }

    private final ImmutableSet<String> clueNpcs = ImmutableSet.of("'Rum'-pumped crab", "Adamant Dragon", "Aberrant spectre", "Abyssal demon", "Abyssal leech", "Air elemental", "Airut", "Ancient mage", "Ancient ranger", "Ankou", "Armoured zombie", "Arrg", "Automaton tracer", "Automaton guardian", "Automaton generator", "Aviansie", "Aquanite", "Bandit", "Banshee", "Barbarian", "Barbarian woman", "Basilisk", "Black Guard", "Black Guard Berserker", "Black Guard crossbowdwarf", "Black Heather", "Black Knight", "Black Knight Titan", "Black demon", "Black dragon", "Bladed muspah", "Blood nihil", "Blood reaver", "Bloodveld", "Blue dragon", "Bork", "Brine rat", "Bronze dragon", "Brutal green dragon", "Camel warrior", "Catablepon", "Cave bug", "Cave crawler", "Cave horror", "Cave slime", "Chaos Elemental", "Chaos druid", "Chaos druid warrior", "Chaos dwarf", "Chaos dwarf hand cannoneer", "Chaos dwogre", "Celestial dragon", "Cockatrice", "Cockroach drone", "Cockroach soldier", "Cockroach worker", "Columbarium", "Columbarium key", "Commander Zilyana", "Corporeal Beast", "Crawling Hand", "Crystal shapeshifter", "Cyclops", "Cyclossus", "Dagannoth", "Dagannoth Prime", "Dagannoth Rex", "Dagannoth Supreme", "Dagannoth guardian", "Dagannoth spawn", "Dark beast", "Desert Lizard", "Desert strykewyrm", "Dragonstone dragon", "Dried zombie", "Dust devil", "Dwarf", "Earth elemental", "Earth warrior", "Elf warrior", "Elite Black Knight", "Elite Dark Ranger", "Elite Khazard guard", "Exiled Kalphite Queen", "Exiled kalphite guardian", "Exiled kalphite marauder", "Ferocious barbarian spirit", "Force muspah", "Fire elemental", "Fire giant", "Flesh Crawler", "Forgotten Archer", "Forgotten Mage", "Forgotten Warrior", "Frog", "Frost dragon", "Ganodermic beast", "Gargoyle", "General Graardor", "General malpractitioner", "Ghast", "Ghostly warrior", "Giant Mole", "Giant ant soldier", "Giant ant worker", "Giant rock crab", "Giant skeleton", "Giant wasp", "Glacor", "Glod", "Gnoeals", "Goblin statue", "Gorak", "Greater demon", "Greater reborn mage", "Greater reborn ranger", "Greater reborn warrior", "Green dragon", "Grotworm", "Haakon the Champion", "Harold", "Harpie Bug Swarm", "Hellhound", "Hill giant", "Hobgoblin", "Hydrix dragon", "Ice giant", "Ice nihil", "Ice strykewyrm", "Ice troll", "Ice troll female", "Ice troll male", "Ice troll runt", "Ice warrior", "Icefiend", "Iron dragon", "Jelly", "Jogre", "Jungle horror", "Jungle strykewyrm", "K'ril Tsutsaroth", "Kal'gerion demon", "Kalphite Guardian", "Kalphite King", "Kalphite Queen", "Kalphite Soldier", "Kalphite Worker", "Killerwatt", "King Black Dragon", "Kraka", "Kree'arra", "Kurask", "Lanzig", "Lesser demon", "Lesser reborn mage", "Lesser reborn ranger", "Lesser reborn warrior", "Lizard", "Locust lancer", "Locust ranger", "Locust rider", "Mature grotworm", "Mighty banshee", "Minotaur", "Mithril dragon", "Molanisk", "Moss giant", "Mountain troll", "Mummy", "Mutated bloodveld", "Mutated jadinko male", "Mutated zygomite", "Nechryael", "Nex", "Ogre", "Ogre statue", "Onyx dragon", "Ork statue", "Otherworldly being", "Ourg statue", "Paladin", "Pee Hat", "Pirate", "Pyrefiend", "Queen Black Dragon", "Red dragon", "Ripper demon", "Rock lobster", "Rockslug", "Rune dragon", "Salarin the Twisted", "Scabaras lancer", "Scarab mage", "Sea Snake Hatchling", "Shadow nihil", "Shadow warrior", "Skeletal Wyvern", "Skeletal miner", "Skeleton", "Skeleton fremennik", "Skeleton thug", "Skeleton warlord", "Small Lizard", "Smoke nihil", "Soldier", "Sorebones", "Speedy Keith", "Spiritual mage", "Spiritual warrior", "Steel dragon", "Stick", "Suqah", "Terror dog", "Thrower Troll", "Throwing muspah", "Thug", "Tortured soul", "Trade floor guard", "Tribesman", "Troll general", "Troll spectator", "Tstanon Karlak", "Turoth", "Tyras guard", "TzHaar-Hur", "TzHaar-Ket", "TzHaar-Mej", "TzHaar-Xil", "Undead troll", "Vampyre", "Vyre corpse", "Vyrelady", "Vyrelord", "Vyrewatch", "Wallasalki", "Warped terrorbird", "Warped tortoise", "Warrior", "Water elemental", "Waterfiend", "Werewolf", "White Knight", "WildyWyrm", "Yeti", "Yuri", "Zakl'n Gritch", "Zombie", "Zombie hand", "Zombie swab");

    public boolean isClueScrollNPC(final String npcName) {
        return clueNpcs.contains(npcName);
    }

    public boolean isCyclops(final String npcName) {
        switch (npcName) {
            case "Cyclops":
                return true;
        }
        return false;
    }

    public boolean isFamiliar() {
        return this instanceof Familiar;
    }

    public boolean isForceAgressive() {
        return forceAgressive;
    }

    public void setForceAgressive(final boolean forceAgressive) {
        this.forceAgressive = forceAgressive;
    }

    public boolean isForceFollowClose() {
        return forceFollowClose;
    }

    public void setForceFollowClose(final boolean forceFollowClose) {
        this.forceFollowClose = forceFollowClose;
    }

    public boolean isForceMultiAttacked() {
        return forceMultiAttacked;
    }

    public void setForceMultiAttacked(final boolean forceMultiAttacked) {
        this.forceMultiAttacked = forceMultiAttacked;
    }

    public boolean isForceWalking() {
        return forceWalk != null;
    }

    /**
     * Gets the locked.
     *
     * @return The locked.
     */
    public boolean isLocked() {
        return locked > Utils.currentTimeMillis();
    }

    /**
     * Sets the locked.
     *
     * @param locked The locked to set.
     */
    public void setLocked(final boolean locked) {
        this.locked = locked ? Integer.MAX_VALUE : 0;
    }

    public boolean isNoDistanceCheck() {
        return noDistanceCheck;
    }

    public void setNoDistanceCheck(final boolean noDistanceCheck) {
        this.noDistanceCheck = noDistanceCheck;
    }

    public boolean isSpawned() {
        return spawned;
    }

    public void setSpawned(final boolean spawned) {
        this.spawned = spawned;
    }

    @Override
    public boolean isUnderCombat() {
        if (isNative950()) return native950CombatEngaged;
        return combat.underCombat();
    }

    @Override
    public boolean needMasksUpdate() {
        return super.needMasksUpdate() || refreshHeadIcon || nextTransformation != null || changedRenderAnimation || changedModels || changedCombatLevel || changedName;
    }

    public void setNextNPCTransformation(final int id) {
        setNPC(id);
        nextTransformation = new Transformation(id);
        if (getCustomCombatLevel() != -1) {
            changedCombatLevel = true;
        }
        if (getCustomName() != null) {
            changedName = true;
        }
    }

    public String getCustomName() {
        return name;
    }

    @Override
    public void processEntity() {
        requireLegacyNpc("entity processing");
        try {
            super.processEntity();
            processNPC();
        } catch (final Exception e) {
            Logger.getGlobal().catching(e);
        }
    }

    /**
     * We init custom NPC settings.
     */
    public void loadNPCSettings() {

        if (id == 6892) {
            setName("Pet Manager");

            setRandomWalk(0);
        }
        if (id == 26435) {
            setName("T-Rex");

           setCombatLevel(7500);
            setRandomWalk(0);
        }
       // if (id == 24854) {
          //  switch (Utils.random(5)) {
             //   case 0:
               //     setNextForceTalk(new ForceTalk("Welcome to Grand exchange!"));
                //    break;
               // case 1:
                //    setNextForceTalk(new ForceTalk("You can sell most items here !"));
                 //   break;
             //   case 2:
                  //  setNextForceTalk(new ForceTalk("you can buy most items here!"));
                  //  break;
          //  }
           // setName("GE Clerk");
           // setRandomWalk(0);
       // }
        if (id == 22532) {
            switch (Utils.random(100)) {
                case 0:
                    setNextForceTalk(new ForceTalk("you can transfer blackcoins into gp here!"));
                    break;
                case 1:
                    setNextForceTalk(new ForceTalk("you can trasnfer gp into blackcoins here !"));
                    break;
                case 2:
                    setNextForceTalk(new ForceTalk("each blackcoin is 25k gp"));
                    break;
            }
            setName("BlackCoins Master");
            setRandomWalk(0);
        }
        if (id == 22542) {
            switch (Utils.random(100)) {
                case 0:
                    setNextForceTalk(new ForceTalk("you buy end game gear using blackcoins here!"));
                    break;

            }
            setName("End-Game Shop");
            setRandomWalk(0);
        }
        if (id == 17495) {
            switch (Utils.random(35)) {
                case 0:
                    setNextForceTalk(new ForceTalk("you can get 1 thieving skilling outfit every 1000 steal."));
                    break;
                case 1:
                    setNextForceTalk(new ForceTalk("you need to master, stealing befor getting all thieving outfit."));
                    break;

            }
            setName("Master Thieves' fighter");
            setRandomWalk(0);
        }
       if (id == 24856) {
            switch (Utils.random(100)) {
                case 0:
                   setNextForceTalk(new ForceTalk("Welcome to the bank!"));
                   break;
                case 1:
                   setNextForceTalk(new ForceTalk("Access Bank 1 With Me"));
                    break;
        case 2:
                   setNextForceTalk(new ForceTalk("We will keep your items safe."));
                   break;
           }
           setName("Banker");
           setRandomWalk(0);
       }

        if (id == 24855) {
            switch (Utils.random(100)) {
                case 0:
                    setNextForceTalk(new ForceTalk("Welcome to the bank!"));
                    break;
                case 1:
                    setNextForceTalk(new ForceTalk("Access Bank 2 With Me"));
                    break;
                case 2:
                    setNextForceTalk(new ForceTalk("We will keep your items safe."));
                    break;
            }
            setName("Banker");
            setRandomWalk(0);
        }

        if (id == 6521) {
            switch (Utils.random(55)) {
                case 0:
                  setNextForceTalk(new ForceTalk("Welcome to the Grand Exchange Area!"));
                   break;
                case 1:
                    setNextForceTalk(new ForceTalk("Welcome to the Market Place!"));
                   break;
            }
            setName("GE Tutor");
            setRandomWalk(0);
       }
        if (id == 25508) {
            switch (Utils.random(55)) {
                case 0:
                    setNextForceTalk(new ForceTalk("ARE YOU READY TO FIGHT SOLAK WORLD BOSS!"));
                    break;
                case 1:
                    setNextForceTalk(new ForceTalk("I AM THE MASTER OF KILLING SOLAK !"));
                    break;
                case 2:
                    setNextForceTalk(new ForceTalk("YOU WILL NOT SURVIVE SOLAK!"));
                    break;
            }
            setName("Master of Solak");
            setRandomWalk(0);
        }
       
        if (id >= 22470 && id <= 22502) {
            setRandomWalk(7);
        }
        for (final int npcId : Settings.NON_WALKING_NPCS) {
            if (npcId == id) {
                setRandomWalk(0);
                break;
            }
        }
        for (final int npcId : Settings.FORCE_WALKING_NPCS) {
            if (npcId == id) {
                setRandomWalk(NORMAL_WALK);
                break;
            }
        }
        if (id == 6139) {
            setName(Settings.SERVER_NAME + "'s welcomer");
            setRandomWalk(0);
        }
          
    }

    public void processNPC() {
        requireLegacyNpc("NPC processing");
        if (isDead() || isLocked()) {
            return;
        }
        loadNPCSettings();
        if (!combat.process()) {
            if (isROTSForceWalking()) {
                if (getX() != ROTSforceWalk.getX() || getY() != ROTSforceWalk.getY()) {
                    if (!hasWalkSteps()) {
                        final int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getId() == 6203 ? 1 : getSize(), new FixedTileStrategy(ROTSforceWalk.getX(), ROTSforceWalk.getY()), true);
                        final int[] bufferX = RouteFinder.getLastPathBufferX();
                        final int[] bufferY = RouteFinder.getLastPathBufferY();
                        for (int i = steps - 1; i >= 0; i--) {
                            if (!addWalkSteps(bufferX[i], bufferY[i], 50, true)) {
                                break;
                            }
                        }
                    }
                    if (!hasWalkSteps()) {
                        ROTSforceWalk = null;
                    }
                } else {
                    ROTSforceWalk = null;
                }
            }
            if (!(this instanceof Telos) && !(this instanceof ColoredAnimaGolem))
                if (!isForceWalking()) {
                    if (!cantInteract) {
                        if (!checkAgressivity()) {
                            if (getFreezeDelay() < Utils.currentTimeMillis()) {
                                if (!hasWalkSteps() && (getWalkType() & NORMAL_WALK) != 0) {
                                    boolean can = false;
                                    for (int i = 0; i < 2; i++) {
                                        if (Math.random() * 1000.0 < 100.0) {
                                            can = true;
                                            break;
                                        }
                                    }
                                    if (can) {
                                        final int moveX = (int) Math.round(Math.random() * 10.0 - 5.0);
                                        final int moveY = (int) Math.round(Math.random() * 10.0 - 5.0);
                                        resetWalkSteps();
                                        if (getMapAreaNameHash() != -1) {
                                            if (!MapAreas.isAtArea(getMapAreaNameHash(), this)) {
                                                forceWalkRespawnTile();
                                                return;
                                            }
                                            addWalkSteps(getX() + moveX, getY() + moveY, 5, (getWalkType() & FLY_WALK) == 0);
                                        } else {

                                            addWalkSteps(respawnTile.getX() + moveX, respawnTile.getY() + moveY, 5, (getWalkType() & FLY_WALK) == 0);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
        }
        if (!(this instanceof Telos) && !(this instanceof ColoredAnimaGolem))
            if (isForceWalking()) {
                if (getFreezeDelay() < Utils.currentTimeMillis()) {
                    if (getX() != forceWalk.getX() || getY() != forceWalk.getY()) {
                        if (!hasWalkSteps()) {
                            final int steps = RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, getX(), getY(), getPlane(), getSize(), new FixedTileStrategy(forceWalk.getX(), forceWalk.getY()), true);
                            final int[] bufferX = RouteFinder.getLastPathBufferX();
                            final int[] bufferY = RouteFinder.getLastPathBufferY();
                            for (int i = steps - 1; i >= 0; i--) {
                                if (!addWalkSteps(bufferX[i], bufferY[i], 25, true)) {
                                    break;
                                }
                            }
                        }
                        if (!hasWalkSteps()) {
                            setNextWorldTile(new WorldTile(forceWalk));
                            forceWalk = null;
                        }
                    } else {
                        forceWalk = null;
                    }
                }
            }
        if (id == 162 && Utils.random(20) == 0) {
            setNextForceTalk(new ForceTalk(gnomeTrainerForceTalk[Utils.random(gnomeTrainerForceTalk.length)]));
        }
        if (id == 15786 && Utils.random(20) == 0) {
            setNextForceTalk(new ForceTalk(trialAnnouncerForceTalk[Utils.random(trialAnnouncerForceTalk.length)]));
        }
    }

    public void removeTarget() {
        if (combat.getTarget() == null) {
            return;
        }
        combat.removeTarget();
    }

    @Override
    public void reset() {
        /**
         * Checking Slayer task here because all other methods are often overran by
         * certain NPCs, so it's unsafe to put them in those.
         */
        @SuppressWarnings("unused") final Player killer = getMostDamageReceivedSourcePlayer();
        /*
         * if (killer != null) killer.getSlayer().checkTask(this);
         */
        super.reset();
        setDirection(getRespawnDirection());
        combat.reset();
        setBonuses();
        stats = NPCStatsDataParser.getStats(id);
        forceWalk = null;
    }

    @Override
    public void resetMasks() {
        super.resetMasks();
        nextTransformation = null;
        changedCombatLevel = false;
        changedName = false;
        refreshHeadIcon = false;
        nextSecondaryBar = null;
        changedRenderAnimation = false;
    }

    @Override
    public void sendDeath(final Entity source) {
        final NPCCombatDefinition defs = getCombatDefinitions();
        resetWalkSteps();
        combat.removeTarget();
        /*
         * Death combat reset deathcombat combatreset deathreset
         */
        if (source instanceof Player) {
            source.deathResetCombat();
        }
        setNextAnimation(null);
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                } else if (loop >= defs.getDeathDelay()) {
                    if (source instanceof Player) {
                        Player plr = (Player) source;
                        if (plr.isGroupIronman()) {
                            switch (id) {
                                case 22818:
                                    plr.gimTracker.incrementBpGained(2);
                                    break;

                            }
                        }
                        plr.getControlerManager().processNPCDeath(NPC.this);
                        ContractHandler.updateContract(plr, NPC.this);
                    }
                    // for(int i =0; i < 150; i ++) {
                    drop();
                    // }
                    reset();
                    setLocation(respawnTile);
                    finish();
                    if (!isSpawned()) {
                        setRespawnTask();
                    }
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    /**
     * temporary fix for overriding npcs not doing animations TODO find a way to
     * send the right animations
     *
     * @param animation
     */
    @Override
    public void setNextAnimation(final Animation animation) {
        if (hasChangedModels() && this instanceof Familiar)
            return;
        super.setNextAnimation(animation);
    }

    public int getAttackSpeed() {
        final Map<Integer, Object> data = getDefinitions().clientScriptData;
        if (data != null) {
            final Integer speed = (Integer) data.get(14);
            if (speed != null) {
                return speed;
            }
        }
        return 4;
    }

    protected void sendDrop(final Player player, final NPCDrop drop, boolean lootbeam) {
        final WorldTile tile = getId() == 22001 ? new WorldTile(getCoordFaceX(getSize()), getCoordFaceY(getSize()), 0) : new WorldTile(getCoordFaceX(getSize()), getCoordFaceY(getSize()), getPlane());
        final String dropName = ItemDefinitions.getItemDefinitions(drop.getItemId()).getName().toLowerCase();
        boolean dropOk = true;

        // if (Herbicide.handleHerbicide(player, dropName, drop))
        // dropOk = false;
        //CharmingImp.handleCharmDrops(player, this);
        if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE)) {
            if (Utils.getRandom(70) <= 1) {
                player.getBank().addItem(new Item(3062), true);
                player.sendMessage("A Herb Box has been sent to your bank thanks to your Herbivore Perk!.", true);
            }
        }
        if (dropName.contains("Clue")) {
            dropOk = false;
        }

        if (getName().equalsIgnoreCase("Green dragon")) {
            if (Utils.getRandom((player.getPerkManager().hasPerkActive(DonationPerk.DRAGON_TRAINER) ? 1000 : 1500)) == 0) {
                if (!player.hasItem(new Item(12473))) {
                    World.updateGroundItem(new Item(12473, 1), tile, player, 60, 0, true);
                    player.sendMessage("This green dragon was carrying an egg which hatched when dropped.", true);
                }
            }
        }

        if (getName().equalsIgnoreCase("Blue dragon")) {
            if (Utils.getRandom((player.getPerkManager().hasPerkActive(DonationPerk.DRAGON_TRAINER) ? 1000 : 1500)) == 0) {
                if (!player.hasItem(new Item(12471))) {
                    World.updateGroundItem(new Item(12471, 1), tile, player, 60, 0, true);
                    player.sendMessage("This blue dragon was carrying an egg which hatched when dropped.", true);
                }
            }
        }
        if (getName().equalsIgnoreCase("Red dragon")) {
            if (Utils.getRandom((player.getPerkManager().hasPerkActive(DonationPerk.DRAGON_TRAINER) ? 1000 : 1500)) == 0) {
                if (!player.hasItem(new Item(12469))) {
                    World.updateGroundItem(new Item(12469, 1), tile, player, 60, 0, true);
                    player.sendMessage("This red dragon was carrying an egg which hatched when dropped.", true);
                }
            }
        }

        if (getName().equalsIgnoreCase("Black dragon") || getName().equalsIgnoreCase("King black dragon") && !getName().contains("Queen")) {
            if (Utils.getRandom((player.getPerkManager().hasPerkActive(DonationPerk.DRAGON_TRAINER) ? 1000 : 1500)) == 0) {
                if (!player.hasItem(new Item(12475))) {
                    World.updateGroundItem(new Item(12475, 1), tile, player, 60, 0, true);
                    player.sendMessage("This black dragon was carrying an egg which hatched when dropped.", true);
                }
            }
        }
        if (dropOk) {
            final Item item = new Item(drop.getItemId());

            if (player.getInventory().containsItem(19675, 1)) {
                if (Herbicide.handleDrop(player, item)) {
                    return;
                }
            }

            if (player.getInventory().containsItem(18337, 1)) {
                if (Bonecrusher.handleDrop(player, item)) {
                    return;
                }
            }
            if (!lootbeam && player.getLootBeamManager().isViableFloorItem(item.getId())) {
                lootbeam = true;
            }
            if (!(drop.getItemId() == 995 && CoinAccumulator.handleCoinAccumulator(player, this, drop.getMinAmount() + Utils.getRandom(drop.getExtraAmount())))) {
                int id = drop.getItemId();

                if (player.isDiamondDonor() && player.isNotingDrops()) {
                    val definitions = ItemDefinitions.getItemDefinitions(id);
                    if (definitions != null && !definitions.isNoted() && definitions.getCertId() != -1) {
                        id = definitions.getCertId();
                    }
                } else if (player.getMoneySpent() >= 100 && player.isNotingDrops()) {
                    if (ArrayUtils.contains(NOTED_GOLD_DONATOR_ITEMS, id)) {
                        val definitions = ItemDefinitions.getItemDefinitions(id);
                        if (definitions != null && !definitions.isNoted() && definitions.getCertId() != -1) {
                            id = definitions.getCertId();
                        }
                    }
                }
                int amount = NPCDropTableRolls.amount(drop);
                if (Settings.DOUBLE_DROPS) {
                    amount *= 2;
                }
                if (this.getId() == 26166 || this.getId() == 26162 || this.getId() == 26165 || this.getId() == 26164 || this.getId() == 26163 || this.getId() == 26170 || this.getId() == 26162 || this.getId() == 26158 || this.getId() == 26149 || this.getId() == 26157 || this.getId() == 26154 || this.getId() == 26144 || this.getId() == 26175) {

                    player.getBank().addItem(new Item(drop.getItemId(), amount), true);
                    return;


                }
                if (this.getId() == 25529) { //solak
                    player.getBank().addItem(new Item(3062), true);
                    player.getBank().addItem(new Item(drop.getItemId(), amount), true);
                    player.setNextWorldTile(new WorldTile(1375, 5643, 0));


                    return;

                }
                if (player.getEquipment().getRingId() == 773 || player.getEquipment().getRingId() == 41069) {
                    if (Utils.randomDouble() <= Settings.PERFECT_RING_DROP_DUPLICATION_RATE) {
                        amount *= 2;
                        player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop has been doubled thanks to your ring!");
                    }
                }
                if (player.getCurrentPet() != null) {
                    if (player.getCurrentPet().getPerks().contains(PetPerk.DOUBLE_TROUBLE)) {
                        if (Utils.random(100) <= PetPerkUtils.getConModifierForPerk(player, PetPerk.DOUBLE_TROUBLE) * 10) {
                            player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop suddenly vanishes because of your pet perk!");
                            return;
                        } else if (Utils.random(100) <= PetPerkUtils.getProModifierForPerk(player, PetPerk.DOUBLE_TROUBLE) * 10) {
                            amount *= 2;
                            player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop has been doubled thanks to your pet perk!");
                        }
                    }
                }
                // }
                /*
                 * if (player.getPetPerkManager().hasActivePetPerk() &&
                 * player.getPetPerkManager().ifActivePetHasPerk(PetPerks.DOUBLE_TROUBLE) &&
                 * Utils.random(100) <=
                 * player.getPetPerkManager().getConModifierForPerk(PetPerks.DOUBLE_TROUBLE) *
                 * 100) { player.sendMessage(Colors.DCYAN +
                 * "<shad=000000>Your drop suddenly vanishes because of your pet perk!");
                 * return; } else { if (player.getPetPerkManager().hasActivePetPerk() &&
                 * player.getPetPerkManager().ifActivePetHasPerk(PetPerks.DOUBLE_TROUBLE)) { if
                 * (Utils.random(100) <=
                 * player.getPetPerkManager().getProModifierForPerk(PetPerks.DOUBLE_TROUBLE) *
                 * 100) { amount *= 2; player.sendMessage(Colors.DCYAN +
                 * "<shad=000000>Your drop has been doubled thanks to your pet perk!"); } } }
                 */
                val loot = new Item(id, amount);
                if (!LootShare.shareLoot(player, this, loot)) {
                    boolean finalLootbeam = lootbeam;
                    player.catchDrop(loot, () -> World.updateGroundItem(loot, tile, player, 60, 0, finalLootbeam));
                }
                player.getDropCollectionHandler().handleBossKills(loot, getId());
            }
            if (lootbeam) {
                LootBeamManager.sendLootBeamMessage(player, player.getLootBeamManager().getCurrentLootBeamType());
            }

            // sendUniqueItem(player, drop.getItemId());
            sendDropMessage(player, dropName);
        }
    }

    protected static final int[] NOTED_GOLD_DONATOR_ITEMS = new int[]{526, 530, 532, 534, 536, 2859, 3125, 4812, 4834, 6729, 6812, 14793, 18830, 30209, 22312, 35008, 35010, 592, 20264, 20266, 20268, 20268, 32945, 34159};

    protected void sendDropMessage(final Player player, final String dropName) {
        if (dropName.contains("pernix") || dropName.contains("ishhara") || dropName.contains("torva") || dropName.contains("virtus") || dropName.contains("bandos") || dropName.contains("hilt") || (dropName.contains("armadyl") && !dropName.contains("rune") && !dropName.contains("shard")) || dropName.contains("spirit shield") || (dropName.contains("saradomin ") && !dropName.contains("brew")) || dropName.contains("dragon claw") || dropName.contains("dragon full") || dropName.contains("dragon pick") || dropName.contains("zaryte") || dropName.contains("steadf") || dropName.contains("glaiven") || dropName.contains("ragef") || dropName.contains("hiss") || dropName.contains("murmur") || dropName.contains("whisper") || dropName.contains("dragon claw") || dropName.contains("ascension grip") || dropName.contains("drygore") || dropName.contains("subjugation") || dropName.contains("draconic") || (dropName.contains("zamorak") && !dropName.contains("wine") && !dropName.contains("brew") && !dropName.contains("warpriest")) || dropName.contains("dragon kite") || dropName.contains("dragon pick") || dropName.contains("scythe") || dropName.contains("seismic") || dropName.contains("elixi") || dropName.contains("sigil") || dropName.contains("zamorakian spear") || dropName.contains("wyrm s") || dropName.contains("wyrm h") || dropName.contains("celestial") || dropName.contains("razorback") || dropName.contains("gemstone") || dropName.contains("avaryss") || dropName.contains("nymora") || dropName.contains("crest of") || dropName.contains("dragon rider lan") || dropName.contains("shadow glai") || dropName.contains("of the cywir elders") || dropName.contains("gloves of passage") || dropName.contains("the minister") || dropName.contains("signet") || dropName.contains("blood neck") || dropName.contains("dragon 2h c") || dropName.contains("visage") || dropName.contains("berserker rin") || dropName.contains("seers'") || dropName.contains("dragon hat") || dropName.contains("archers'") || dropName.contains("grips") || dropName.contains("razorback") || dropName.contains("handwrap") || dropName.contains("statius'") || dropName.contains("zuriel") || dropName.contains("vesta") || dropName.contains("spider leg") || dropName.contains("dragon limb") || dropName.contains("morrigan") || dropName.contains("loyalty") || dropName.contains("robin hood") || dropName.contains("ranger bo") || dropName.contains("glacyte") || dropName.contains("antlers") || dropName.contains("ean ess") || dropName.contains("ian ess") || dropName.contains("serenic ess")) {
            String message = Utils.getAorAn(dropName) + dropName;
            if (dropName.contains("gloves") || dropName.contains("boots") || dropName.contains("grips") || dropName.contains("razorback") || dropName.contains("handwraps")) {
                message = "a pair of " + dropName;
            }

            if (message.contains("null")) {
                return;
            }

            World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " received " + message + " from " + (getName().equalsIgnoreCase("large mound") ? "WildyWyrm" : getName()) + ".", false);

            QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + player.getDisplayName() + " received " + message + " from " + getName() + "."));
        }
    }


    @Override
    public void setAttackedBy(final Entity target) {
        super.setAttackedBy(target);
        // Native melee owns its own target/cadence; preserve Entity bookkeeping without
        // consulting the deliberately absent legacy NPCCombat instance.
        if (isNative950()) return;
        if (target == combat.getTarget() && !(combat.getTarget() instanceof Familiar)) {
            setLastAttackedByTarget(Utils.currentTimeMillis());
        }
    }

    public void resetBonuses() {
        setBonuses();
    }

    public void setBonuses() {
        bonuses = getDefinitions().getCacheBonuses();
    }

    public void setBonuses(final int[] bonuses) {
        this.bonuses = bonuses;
    }

    public void setBonus(final int index, final int level) {
        bonuses[index] = level;
    }

    public void setAttackBonuses(final int level) {
        bonuses[3] = level;
    }

    public void setMagicBonuses(final int level) {
        bonuses[5] = level;
    }

    public void setRangedBonuses(final int level) {
        bonuses[4] = level;
    }

    public void setDefenceBonuses(final int level) {
        bonuses[6] = level;
    }

    public void setCanBeAttackFromOutOfArea(final boolean b) {
        canBeAttackFromOutOfArea = b;
    }

    public void setForceWalk(final WorldTile tile) {
        resetWalkSteps();
        forceWalk = tile;
    }

    public boolean hasChangedName() {
        return changedName;
    }

    public void setNPC(final int id) {
        requireLegacyNpc("NPC transformation");
        this.id = id;
        setBonuses();
        setNPCStats();
    }

    public NPC setRandomWalk2(final int forceRandomWalk) {
        setWalkType(forceRandomWalk);
        return this;
    }

    public void setRandomWalk(final int forceRandomWalk) {
        setWalkType(forceRandomWalk);
    }

    public void setRespawnTask() {
        if (bossInstance != null && bossInstance.isFinished()) {
            return;
        }
        if(this instanceof HusbandMichNPC)
            return;
        if (!hasFinished()) {
            reset();
            setLocation(respawnTile);
            finish();
        }
        int respawnDelay = getCombatDefinitions().getRespawnDelay();
        if (getId() == 6898) {
            respawnDelay = 1;
        }
        if (getName().toLowerCase().contains("impling")) {
            respawnDelay *= 3;
        }
        if (bossInstance != null) {
            respawnDelay /= bossInstance.getSettings().getSpawnSpeed();
        }
        if (Settings.DEBUG) {
            Logger.getGlobal().info("Respawn task initiated: [" + getName() + "]; time: [" + respawnDelay + "].");
        }
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    spawn();
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, respawnDelay);
    }

    @Override
    public void setTarget(final Entity entity) {
        if (isForceWalking()) {
            return;
        }
        if ((this instanceof GeneralGraardor) || (this instanceof KrilTsutsaroth) || (this instanceof CommanderZilyana) || (this instanceof KreeArra) || (this instanceof com.rs.game.npc.kalphiteking.KalphiteKing) || (this instanceof com.rs.game.npc.vorago.Vorago) || (this instanceof CorporealBeast) || (this instanceof Helwyr) || (this instanceof Nex) || (this instanceof Vindicta) || this instanceof AhrimNPC || this instanceof KarilNPC || this instanceof VeracNPC || this instanceof GuthanNPC || this instanceof DharokNPC || this instanceof ToragNPC || (this instanceof Avaryss) || (this instanceof Nymora) || (this instanceof TormentedDemon)) {
            if (entity instanceof Familiar) {
                if (((Familiar) entity).getOwner() != null) {
                    combat.setTarget(((Familiar) entity).getOwner());
                    setLastAttackedByTarget(Utils.currentTimeMillis());
                    return;
                }
            }
        }
        combat.setTarget(entity);
        setLastAttackedByTarget(Utils.currentTimeMillis());
    }

    public void spawn() {
        setFinished(false);
        World.addNPC(this);
        setLastRegionId(0);
        World.updateEntityRegion(this);
        loadMapRegions();
        checkMultiArea();
    }

    public boolean isCantSetTargetAutoRelatio() {
        return cantSetTargetAutoRelatio;
    }

    public void setCantSetTargetAutoRelatio(final boolean cantSetTargetAutoRelatio) {
        this.cantSetTargetAutoRelatio = cantSetTargetAutoRelatio;
    }

    @Override
    public String toString() {
        if (isNative950())
            return "Native 947 NPC - " + id + " - " + getX() + " " + getY() + " " + getPlane();
        return getDefinitions().name + " - " + id + " - " + getX() + " " + getY() + " " + getPlane();
    }

    public void transformIntoNPC(final int id) {
        setNPC(id);
        nextTransformation = new Transformation(id);
    }

    public int whatDefender() {
        int id = 8844;
        if (containsItem(8850) || containsItem(20072)) {
            id = 20072;
        } else if (containsItem(8849) || containsItem(8850)) {
            id = 8850;
        } else if (containsItem(8848)) {
            id = 8849;
        } else if (containsItem(8847)) {
            id = 8848;
        } else if (containsItem(8846)) {
            id = 8847;
        } else if (containsItem(8845)) {
            id = 8846;
        } else if (containsItem(8844)) {
            id = 8845;
        } else {
            id = 8844;
        }
        return id;
    }

    public boolean withinDistance(final Player tile, final int distance) {
        return super.withinDistance(tile, distance);
    }

    public boolean isIntelligentRouteFinder() {
        return intelligentRouteFinder;
    }

    public void setIntelligentRouteFinder(final boolean intelligentRouteFinder) {
        this.intelligentRouteFinder = intelligentRouteFinder;
    }

    public BossInstance getBossInstance() {
        return bossInstance;
    }

    public void setBossInstance(final BossInstance instance) {
        bossInstance = instance;
    }

    /**
     * Head Icons.
     */
    public HeadIcon[] getIcons() {
        return new HeadIcon[0];
    }

    public void requestIconRefresh() {
        refreshHeadIcon = true;
    }

    public boolean isRefreshHeadIcon() {
        return refreshHeadIcon;
    }

    /**
     * Generates a new target if enough time has passed since the generation of*
     * previous target
     *
     * @return new target.
     */
    public boolean switchTarget() {
        return switchTarget(false);
    }

    public boolean switchTarget(boolean force) {
        if (force || (Utils.random(3) == 0 && (lastAttackedByTarget + 15000) >= Utils.currentTimeMillis())) {
            if (getPossibleTargets().size() != 0) {
                Random random = new Random();
                ArrayList<Entity> targets = getPossibleTargets();
                Entity target = targets.get(random.nextInt(targets.size()));
                setTarget(target);
                return true;
            }
        }
        return false;
    }

    /**
     * Exclusively used for the Impetuous Impulses minigame.
     */
    public void setRespawnTaskImpling() {
        if (!hasFinished()) {
            reset();
            setLocation(respawnTile);
            finish();
            if (Settings.DEBUG) {
                Logger.getGlobal().info("Finishing NPC: [" + this + "].");
            }
        }
        if (!(getX() >= 2316 && getX() <= 2354 && getY() >= 4264 && getY() <= 4285 && getPlane() == 0)) {
            id = FlyingEntities.randomImpling().getNpcId();
            setLocation(new WorldTile(Utils.random(2558 + 3, 2626 - 3), Utils.random(4285 + 3, 4354 - 3), 0));
        }
        final long respawnDelay = getCombatDefinitions().getRespawnDelay() * 600;
        if (Settings.DEBUG) {
            Logger.getGlobal().info("Respawn task initiated: [" + this + "]; time: [" + respawnDelay + "].");
        }
        CoresManager.getServiceProvider().executeWithDelay(() -> {
            try {
                spawn();
            } catch (final Throwable e) {
                Logger.getGlobal().catching(e);
            }
        }, respawnDelay, TimeUnit.MILLISECONDS);
    }

    public boolean canBeAttackedByAutoRelatie() {
        return Utils.currentTimeMillis() - getLastAttackedByTarget() > lureDelay;
    }
    // TODO rewrite this with the new collectionlog system
    /*
     * public static void sendUniqueItem(Player player, int item) { for
     * (CollectionItems.Collection unique : CollectionItems.Collection.values()) {
     * if (item == unique.getId()) { player.addUniqueItem(unique.getId()); } } }
     */

    public void setId(final int id) {
        this.id = id;
    }

    public int getWalkType() {
        return walkType;
    }

    public void setWalkType(final int walkType) {
        this.walkType = walkType;
    }

    public long getLastAttackedByTarget() {
        return lastAttackedByTarget;
    }

    public void setLastAttackedByTarget(final long lastAttackedByTarget) {
        this.lastAttackedByTarget = lastAttackedByTarget;
    }

    /**
     * Gets the randomEventTarget.
     *
     * @return the randomEventTarget
     */
    public Player getRandomEventTarget() {
        return randomEventTarget;
    }

    /**
     * Sets the randomEventTarget.
     *
     * @param randomEventTarget the randomEventTarget to set
     */
    public void setRandomEventTarget(final Player randomEventTarget) {
        this.randomEventTarget = randomEventTarget;
    }

    /**
     * Gets the createTime.
     *
     * @return the createTime
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * Sets the createTime.
     *
     * @param createTime the createTime to set
     */
    public void setCreateTime(final long createTime) {
        this.createTime = createTime;
    }

    /**
     * Gets the stop.
     *
     * @return the stop
     */
    public boolean isStop() {
        return stop;
    }

    /**
     * Sets the stop.
     *
     * @param stop the stop to set
     */
    public void setStop(final boolean stop) {
        this.stop = stop;
    }

    public boolean canBeAttacked(Player attacker) {
        return true;
    }

    public void setNPCStats() {
        stats = NPCStatsDataParser.getStats(id);
    }

    public NPCStats getStats() {
        if (stats == null)
            stats = NPCStatsDataParser.getStats(id);
        return stats;
    }

}
