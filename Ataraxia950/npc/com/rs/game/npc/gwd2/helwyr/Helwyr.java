package com.rs.game.npc.gwd2.helwyr;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.HelwyrInstance;
import com.rs.game.item.Item;
import com.rs.game.item.floor.LootBeamManager;
import com.rs.game.npc.NPC;
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

/**
 * @author Tom
 * @date April 8, 2017
 */

public class Helwyr extends NPC {

    private static final long serialVersionUID = 7050003141268362974L;
    private int phase;
    private final HelwyrInstance instance;

    public Helwyr(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final boolean spawned, final HelwyrInstance instance) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        setRun(true);
        setIntelligentRouteFinder(true);
        setForceMultiArea(true);
        setForceTargetDistance(50);
        setNoDistanceCheck(true);
        setCantInteract(true);
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                setCantInteract(false);
            }
        }, 6);
        this.instance = instance;
        instance.getPlayers().forEach(p -> p.getTemporaryAttributtes().remove("bleed"));// Clearing out previously cached bleed effect.
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
    public int getCapDamage() {
        return 1250;
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        if (Settings.USE_DAMAGE_CAP) {
            if (getCapDamage() != -1 && hit.getDamage() > getCapDamage()) {
                hit.setDamage(getCapDamage());
            }
        }
        if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE) {
            HeartOfGielinor.refreshHealth(instance, getHitpoints() - hit.getDamage(), getMaxHitpoints());
            return;
        }
        handlePrayers(hit);
        HeartOfGielinor.refreshHealth(instance, getHitpoints() - hit.getDamage(), getMaxHitpoints());
    }

    @Override
    public void processNPC() {
        super.processNPC();
        if (!isUnderCombat() && getInstance().getPlayers().size() == 0 && !hasFinished()) {
            finish();
            getInstance().getWolves().forEach(w -> w.sendDeath(null));
            return;
        }
        for (final Player p : instance.getPlayers()) {
            if (p == null) {
                continue;
            }
            if (p.getTemporaryAttributtes().get("bleed") != null) {
                if (p.getTemporaryAttributtes().remove("skiptick") != null) {
                    continue;// This damage is applied every two ticks.
                }
                final int bleed = (int) p.getTemporaryAttributtes().get("bleed");
                p.applyHit(new Hit(this, bleed, HitLook.REGULAR_DAMAGE));
                if (Utils.currentTimeMillis() - (p.getTemporaryAttributtes().get("bleedTime") == null ? 0 : (long) p.getTemporaryAttributtes().get("bleedTime")) > 15000) {
                    if (bleed <= 20) {
                        p.getTemporaryAttributtes().remove("bleed");
                        continue;
                    }
                    p.getTemporaryAttributtes().put("bleed", bleed - 20);
                    p.getTemporaryAttributtes().put("bleedTime", Utils.currentTimeMillis());
                }
                p.getTemporaryAttributtes().put("skiptick", true);
            }
        }
        if (instance.getTiles().size() == 0) {
            return;
        }
        instance.getPlayers().forEach(p -> {
            boolean inGas = false;
            for (int i = 0; i < instance.getTiles().size(); i++) {
                if (instance.getTiles().get(i) != null && p.withinDistance(instance.getTiles().get(i), 2)) {
                    p.applyHit(new Hit(this, Utils.random(10, 25), HitLook.REGULAR_DAMAGE));
                    inGas = true;
                    final long stunDelay = p.getTemporaryAttributtes().get("stunDelay") == null ? 0 : (long) p.getTemporaryAttributtes().get("stunDelay");
                    if (stunDelay == 0) {
                        if (!p.isFrozen()) {
                            p.getTemporaryAttributtes().put("stunDelay", Utils.currentTimeMillis());
                        }
                    } else {
                        if (stunDelay + 5000 < Utils.currentTimeMillis()) {
                            p.addFreezeDelay(3000);
                            p.resetWalkSteps();
                            p.sendMessage("You feel a little dizzy after standing in the gas for too long.");
                            p.getTemporaryAttributtes().remove("stunDelay");
                        }
                    }
                }
            }
            if (!inGas) {
                p.getTemporaryAttributtes().remove("stunDelay");
            }
        });
    }

    @Override
    public void sendDeath(final Entity source) {
        instance.getWolves().forEach(n -> {
            n.sendDeath(source);
            n.setNextAnimation(new Animation(23579));
        });
        setNextAnimation(new Animation(28204));
        instance.getPlayers().forEach(player -> {
            for (int x = 0; x < instance.getTiles().size(); x++) {
                World.sendGraphics(player, new Graphics(-1), instance.getTiles().get(x));
                World.spawnObject(new WorldObject(101899, 11, 1, instance.getTiles().get(x)));
            }
            if(player.isGroupIronman()) {
                player.gimTracker.incrementBpGained(5);
            }
            player.getAchievements().updateProgress(1, AchievementList.KILL_500_GWD2_BOSSES);
            player.getActivityTimersManager().finishBossTimer(Helwyr.this);
        });
        instance.getTiles().clear();
        for (WorldObject object : World.getRegion(getRegionId()).getAllObjects()) {
            if (object == null || object.getId() == 101899)
                continue;
            if (object.getId() == 101900) {
                instance.getPlayers().forEach(player -> {
                    World.sendGraphics(player, new Graphics(-1), object);
                });
                World.removeObject(object);
                object.setId(101899);
                World.spawnObject(object);
            }
        }
        super.sendDeath(source);
    }

    @Override
    public void spawn() {
        super.spawn();
        setNextAnimation(new Animation(28200));
        setNextGraphics(new Graphics(6120));
        setNextGraphics(new Graphics(6085));
    }

    @Override
    public void drop() {
        try {
            final NPCDrop[] drops = NPCDropsDataParser.getDrops(id);
            final Player killer = getMostDamageReceivedSourcePlayer();
            if (killer == null) {
                return;
            }
            increaseKillStatistics(killer, getInstance().isHardMode() ? "helwyr(cm)" : "helwyr");
            int chance = instance.isHardMode() ? 1000 : 2000;
            if (killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER)) {
                chance *= 0.75;
            }
            if (Utils.random(chance) == 0) {
                HeartOfGielinor.sendPetDrop(killer, new Item(37182), BOSS_DATA.HELWYR);
                killer.getAchievements().updateProgress(1, AchievementList.OBTAIN_BOSS_PET);
            }
            if (killer.getKillStatistics(getInstance().isHardMode() ? 114 : 113) == 1) {
                final int amount = getInstance().isHardMode() ? 100 : 50;
                for (int i = 0; i < 4; i++) {
                    if (i == HeartOfGielinor.SEREN) {
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
            for (NPCDrop drop : drops) {
                if (drop.getRate() == 100) {
                    sendDrop(killer, drop, false);
                } else {
                    double rate = killer.getHeart().getDropRate(getId(), drop);
                    final double random = Utils.getRandomDouble(ContractHandler.isContractNpc(killer, this) ? 95 : 100);
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
    protected void sendDrop(final Player player, NPCDrop drop, boolean lootbeam) {
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
        if (player.getHeart().getInsigniaSettings()[0] && drop.getItemId() == 37103 && player.getHeart().getActiveInsignia() != HeartOfGielinor.SEREN && player.getHeart().getActiveInsignia() != -1) {
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
        if (!(drop.getItemId() == 995
                && CoinAccumulator.handleCoinAccumulator(player, this, drop.getMinAmount() + Utils.getRandom(drop.getExtraAmount())))) {
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
            if (!LootShare.shareLoot(player, this, loot)) {
                boolean finalLootbeam = lootbeam;
                player.getDropCollectionHandler().handleBossKills(loot, DropCollectionConstants.BOSS_DATA.HELWYR.getNpcId());
                player.catchDrop(loot, () -> World.updateGroundItem(loot, tile, player, 60, 0, finalLootbeam));
            }
        }
        if (lootbeam) {
            LootBeamManager.sendLootBeamMessage(player, player.getLootBeamManager().getCurrentLootBeamType());
        }
        sendDropMessage(player, dropName);
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

    public HelwyrInstance getInstance() {
        return instance;
    }

}
