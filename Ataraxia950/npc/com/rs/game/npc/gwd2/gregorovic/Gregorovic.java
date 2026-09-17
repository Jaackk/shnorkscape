package com.rs.game.npc.gwd2.gregorovic;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.GregorovicInstance;
import com.rs.game.item.Item;
import com.rs.game.item.floor.LootBeamManager;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.actions.herblore.herbicide.Herbicide;
import com.rs.game.player.content.HeartOfGielinor;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.dropcollection.DropCollectionConstants;
import com.rs.game.player.content.dropcollection.DropCollectionConstants.BOSS_DATA;
import com.rs.game.player.content.items.Bonecrusher;
import com.rs.game.player.content.items.CharmingImp;
import com.rs.game.player.content.items.CoinAccumulator;
import com.rs.game.player.content.lootshare.LootShare;
import com.rs.game.player.content.petperks.PetPerk;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class Gregorovic extends NPC {

    private static final long serialVersionUID = -3292021187841555323L;
    private int phase, boostedDamage;
    protected int lastSwitch;
    protected GregorovicInstance instance;

    private final ArrayList<WorldTile> tiles = new ArrayList<WorldTile>();
    protected List<Shadow> shadows = new ArrayList<Shadow>();
    private final List<Spirit> spirits = new ArrayList<Spirit>();
    private int maniaBuff;
    protected int shadowStage;

    private final WorldTile[] from;
    private final WorldTile[] to;

    @Override
    public boolean isIntelligentRouteFinder() {
        return true;
    }

    @Override
    public boolean canWalkNPC(final int toX, final int toY) {
        return true;
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.5;
    }

    @Override
    public double getRangePrayerMultiplier() {
        return 0.5;
    }

    @Override
    public double getMagePrayerMultiplier() {
        return 1;
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        if (Settings.USE_DAMAGE_CAP) {
            if (getCapDamage() != -1 && hit.getDamage() > getCapDamage()) {
                hit.setDamage(getCapDamage());
            }
        }
        final int hp = getHitpoints() - hit.getDamage();
        if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE) {
            HeartOfGielinor.refreshHealth(instance, hp, getMaxHitpoints());
            return;
        }
        handlePrayers(hit);
        HeartOfGielinor.refreshHealth(instance, hp, getMaxHitpoints());
    }

    @Override
    public int getCapDamage() {
        return 1250;
    }

    public WorldTile[] getFrom() {
        return from;
    }

    public WorldTile[] getTo() {
        return to;
    }

    public void boostDamage() {
        boostedDamage++;
    }

    public double getDamageBoost() {
        return 1 + (boostedDamage / 5f);
    }

    @Override
    public void drop() {
        try {
            final NPCDrop[] drops = NPCDropsDataParser.getDrops(id);
            final Player killer = getMostDamageReceivedSourcePlayer();
            if (killer == null) {
                return;
            }
            if (drops == null) {
                return;
            }
            increaseKillStatistics(killer, getInstance().isHardMode() ? "gregorovic(cm)" : "gregorovic");
            int chance = instance.isHardMode() ? 1000 : 2000;
            if (killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER)) {
                chance *= 0.75;
            }
            if (Utils.random(chance) == 0) {
                HeartOfGielinor.sendPetDrop(killer, new Item(37183), BOSS_DATA.GREG);
                killer.getAchievements().updateProgress(1, AchievementList.OBTAIN_BOSS_PET);
            }
            if (killer.getKillStatistics(getInstance().isHardMode() ? 124 : 123) == 1) {
                final int amount = getInstance().isHardMode() ? 100 : 50;
                for (int i = 0; i < 4; i++) {
                    if (i == HeartOfGielinor.SLISKE) {
                        continue;
                    }
                    killer.getHeart().setReputation(i, killer.getHeart().getReputation(i) + amount);
                    killer.sendMessage(HeartOfGielinor.getColour(i) + "You have gained " + amount + " reputation with the " + HeartOfGielinor.getGod(i) + " forces.");
                }
                killer.sendMessage("You have gained reputation for killing a boss for the first time.");
            }
            handleRingOfDeath(killer);
            final NPCDrop[] possibleDrops = new NPCDrop[drops.length];
            int possibleDropsCount = 0;
            for (final NPCDrop drop : drops) {
                if (drop.getRate() == 100) {
                    sendDrop(killer, drop, false);
                } else {
                    double rate = killer.getHeart().getDropRate(getId(), drop);
                    final double random = Utils.getRandomDouble(100);
                    if (rate < 30) {
                        rate *= Settings.getDropQuantityRate(killer);
                    }
                    if (random <= rate && random != 100 && random != 0) {
                        possibleDrops[possibleDropsCount++] = drop;
                    }
                }
            }
            if (possibleDropsCount > 0) {
                sendDrop(killer, possibleDrops[Utils.getRandom(possibleDropsCount - 1)], false);
            }
        } catch (final Exception e) {
            Logger.getGlobal().catching(e);
        } catch (final Error e) {
            Logger.getGlobal().catching(e);
        }
    }

    @Override
    protected void sendDrop(final Player player, final NPCDrop drop, boolean lootbeam) {
        final WorldTile tile = new WorldTile(getCoordFaceX(getSize()), getCoordFaceY(getSize()), getPlane());
        final String dropName = ItemDefinitions.getItemDefinitions(drop.getItemId()).getName().toLowerCase();
        CharmingImp.handleCharmDrops(player, this);
        final Item item = new Item(drop.getItemId());
        if (player.getInventory().containsItem(19675, 1)) {
            if (Herbicide.handleDrop(player, item)) {
                return;
            }
        }
        int amount = drop.getMinAmount() + Utils.getRandom(drop.getExtraAmount());
        if (player.getHeart().getInsigniaSettings()[0] && drop.getItemId() == 37101 && player.getHeart().getActiveInsignia() != HeartOfGielinor.SLISKE && player.getHeart().getActiveInsignia() != -1) {
            player.getHeart().setReputation(player.getHeart().getActiveInsignia(), player.getHeart().getReputation(player.getHeart().getActiveInsignia()) + (amount * 5));
            player.sendMessage("You receive " + (amount * 5) + " reputation for the " + HeartOfGielinor.getGod(player.getHeart().getActiveInsignia()) + " faction.");
            return;
        }
        if (player.getInventory().containsItem(18337, 1)) {
            if (Bonecrusher.handleDrop(player, item)) {
                return;
            }
        }
        if (!lootbeam && player.getLootBeamManager().isViableFloorItem(item.getId())) {
            lootbeam = true;
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
            if (player.getEquipment().getRingId() == 773 || player.getEquipment().getRingId() == 41069 || player.getEquipment().getRingId() == 48483) {
                if (Utils.randomDouble() <= Settings.PERFECT_RING_DROP_DUPLICATION_RATE) {
                    amount *= 2;
                    player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop has been doubled thanks to your ring!");
                }
            }
            val loot = new Item(id, amount);
            player.getDropCollectionHandler().handleBossKills(new Item(id, amount), DropCollectionConstants.BOSS_DATA.GREG.getNpcId());
            if (!LootShare.shareLoot(player, this, loot)) {
                boolean finalLootbeam = lootbeam;
                player.catchDrop(loot, () -> World.updateGroundItem(loot, tile, player, 60, 0, finalLootbeam));
            }
        }
        if (lootbeam) {
            LootBeamManager.sendLootBeamMessage(player, player.getLootBeamManager().getCurrentLootBeamType());
        }
        sendDropMessage(player, dropName);
    }

    public Gregorovic(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final boolean spawned, final GregorovicInstance instance) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        this.instance = instance;
        setRun(true);
        setIntelligentRouteFinder(true);
        setForceTargetDistance(50);
        setForceMultiArea(true);
        setRangedBonuses(1000);
        from = new WorldTile[]{getInstance().getWorldTile(32, 37), getInstance().getWorldTile(44, 55), getInstance().getWorldTile(55, 37)};
        to = new WorldTile[]{getInstance().getWorldTile(35, 40), getInstance().getWorldTile(43, 51), getInstance().getWorldTile(52, 40)};
    }

    @Override
    public int getMaxDistance() {
        return 3;
    }

    @Override
    public void processNPC() {
        super.processNPC();
        if (!isUnderCombat() && getInstance().getPlayers().size() == 0 && !hasFinished()) {
            finish();
            if (getShadows() != null) {
                for (final Shadow s : getShadows()) {
                    if (s != null && !s.hasFinished() && !s.isDead()) {
                        s.sendDeath(null);
                    }
                }
            }
            if (getSpirits() != null) {
                for (final Spirit s : getSpirits()) {
                    if (s != null && !s.hasFinished() && !s.isDead()) {
                        s.sendDeath(null);
                    }
                }
            }
            return;
        }
        if (getHitpoints() <= 14000 && shadowStage == 0 && !isDead()) {
            shadowStage++;
            for (int i = 0; i < (getInstance().isHardMode() ? 3 : 2); i++) {
                shadows.add(new Shadow(22444, new WorldTile(instance.getWorldTile(Utils.random(33, 54), Utils.random(33, 54))), -1, true, true));
            }
        } else if (getHitpoints() <= 6000 && shadowStage == 1 && !isDead()) {
            shadowStage++;
            for (int i = 0; i < (getInstance().isHardMode() ? 4 : 3); i++) {
                shadows.add(new Shadow(22444, new WorldTile(instance.getWorldTile(Utils.random(33, 54), Utils.random(33, 54))), -1, true, true));
            }
        }
        if (lastSwitch > 12) {
            for (final Shadow s : shadows) {
                if (s == null || s.isDead() || s.hasFinished() || isCantInteract()) {
                    continue;
                }
                if (Utils.random(2) == 1) {
                    final WorldTile shadow = new WorldTile(s);
                    final WorldTile greg = new WorldTile(this);
                    s.setNextWorldTile(greg);
                    setNextWorldTile(shadow);
                    setNextGraphics(new Graphics(6137));
                    s.setNextGraphics(new Graphics(6137));
                    lastSwitch = 0;
                    break;
                }
            }

        }
        lastSwitch++;
    }

    @Override
    public void sendDeath(final Entity source) {
        final NPCCombatDefinition defs = getCombatDefinitions();
        resetWalkSteps();
        getCombat().removeTarget();
        if (source instanceof Player) {
            source.deathResetCombat();
            ContractHandler.updateContract(((Player) source), this);
        }
        setNextAnimation(null);
        for (final Shadow s : getShadows()) {
            if (s != null && !s.hasFinished() && !s.isDead()) {
                s.sendDeath(source);
            }
        }
        for (final Spirit s : getSpirits()) {
            if (s != null && !s.hasFinished() && !s.isDead()) {
                s.sendDeath(source);
            }
        }
        instance.getPlayers().forEach(player -> {
            if(player.isGroupIronman()) {
                player.gimTracker.incrementBpGained(3);
            }
            player.getAchievements().updateProgress(1, AchievementList.KILL_500_GWD2_BOSSES);
            player.getActivityTimersManager().finishBossTimer(Gregorovic.this);
        });
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                } else if (loop >= defs.getDeathDelay()) {
                    drop();
                    reset();
                    setLocation(getRespawnTile());
                    finish();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
    }

    /**
     * Skips the regular attacks to the next special attack.
     */
    public void skipBasicAttacks() {
        if (getPhase() < 4) {
            setPhase(3);
        } else if (getPhase() < 8) {
            setPhase(7);
        } else if (getPhase() < 12) {
            setPhase(11);
        }
    }

    @Override
    public void spawn() {
        super.spawn();
        setNextAnimation(new Animation(28223));
    }

    public int getPhase() {
        return phase;
    }

    public void nextPhase() {
        phase++;
    }

    public void setPhase(final int phase) {
        this.phase = phase;
    }

    public GregorovicInstance getInstance() {
        return instance;
    }

    public List<Shadow> getShadows() {
        return shadows;
    }

    public ArrayList<WorldTile> getTiles() {
        return tiles;
    }

    public int getManiaBuff() {
        return maniaBuff;
    }

    public void setManiaBuff(final int buff) {
        maniaBuff = buff;
    }

    public List<Spirit> getSpirits() {
        return spirits;
    }

}