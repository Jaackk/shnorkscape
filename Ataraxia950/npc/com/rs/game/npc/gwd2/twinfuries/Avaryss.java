package com.rs.game.npc.gwd2.twinfuries;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.TwinFuriesInstance;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.actions.herblore.herbicide.Herbicide;
import com.rs.game.player.content.HeartOfGielinor;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.dropcollection.DropCollectionConstants;
import com.rs.game.player.content.dropcollection.DropCollectionConstants.BOSS_DATA;
import com.rs.game.player.content.items.Bonecrusher;
import com.rs.game.player.content.items.CharmingImp;
import com.rs.game.player.content.items.CoinAccumulator;
import com.rs.game.player.content.lootshare.LootShare;
import com.rs.game.player.content.petperks.PetPerk;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Tom
 * @date April 14, 2017
 */

public class Avaryss extends NPC {

    private static final long serialVersionUID = -3119789621001568251L;
    private final TwinFuriesInstance instance;
    private boolean finished;
    private long fireballDelay;

    public Avaryss(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea,
                   final boolean spawned, final TwinFuriesInstance instance) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        setForceTargetDistance(50);
        setRun(true);
        setIntelligentRouteFinder(true);
        setForceAgressive(true);
        setForceMultiArea(true);
        this.instance = instance;
        instance.setPhase(0);
        setFreezeDelay(1);
        fireballDelay = Utils.currentTimeMillis() + 5000;
    }

    @Override
    public boolean canWalkNPC(final int toX, final int toY) {
        return true;
    }

    @Override
    public void spawn() {
        super.spawn();
        setNextAnimation(new Animation(28248));
    }

    @Override
    public double getMeleePrayerMultiplier() {
        return 0.5;
    }

    @Override
    public void processNPC() {
        super.processNPC();
        if (!isUnderCombat() && getInstance().getPlayers().size() == 0 && !hasFinished()) {
            finish();
            getInstance().getNymora().finish();
            return;
        }
        if (getInstance().isHardMode() && fireballDelay < Utils.currentTimeMillis()) {
            new RedFireball(this, null).effect();
            fireballDelay = Utils.currentTimeMillis() + 25000;
        }
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
        if (instance.isChannelling()) {
            hit.setDamage(hit.getDamage() * 2);
        }
        if (hit.getLook() != HitLook.MELEE_DAMAGE && hit.getLook() != HitLook.RANGE_DAMAGE && hit.getLook() != HitLook.MAGIC_DAMAGE) {
            HeartOfGielinor.refreshHealth(instance, getHitpoints() - hit.getDamage(), getMaxHitpoints());
            return;
        }
        handlePrayers(hit);
        final int health = getHitpoints() - hit.getDamage();
        getInstance().getNymora().setHitpoints(health);
        HeartOfGielinor.refreshHealth(instance, health, getMaxHitpoints());
    }

    @Override
    public void sendDeath(final Entity source) {
        if (finished) {
            return;
        }
        finished = true;
        setHitpoints(0);
        super.sendDeath(source);
        instance.getPlayers().forEach(player -> {
                if (player.isGroupIronman()) {
                    player.gimTracker.incrementBpGained(3);
                }
            player.getAchievements().updateProgress(1, AchievementList.KILL_500_GWD2_BOSSES);
            player.getActivityTimersManager().finishBossTimer(Avaryss.this);
        });
        getInstance().getNymora().sendDeath(source);
    }

    @Override
    public void drop() {
        try {
            final NPCDrop[] drops = NPCDropsDataParser.getDrops(instance.isHardMode() ? 22455 : id);
            final Player killer = getMostDamageReceivedSourcePlayer();
            if (killer == null) {
                return;
            }
            increaseKillStatistics(killer, getInstance().isHardMode() ? "twin furies'(cm)" : "twin furies");
            int chance = instance.isHardMode() ? 1000 : 2000;
            if (killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER)) {
                chance *= 0.75;
            }
            if (Utils.random(chance) == 0) {
                final Item item = Utils.random(2) == 0 ? new Item(37184) : new Item(37185);
                HeartOfGielinor.sendPetDrop(killer, killer.hasItem(item) ? new Item(item.getId() == 37184 ? 37185 : 37184) : item, BOSS_DATA.TWINS);
                killer.getAchievements().updateProgress(1, AchievementList.OBTAIN_BOSS_PET);
            }
            if (killer.getKillStatistics(getInstance().isHardMode() ? 122 : 121) == 1) {
                final int amount = getInstance().isHardMode() ? 100 : 50;
                for (int i = 0; i < 4; i++) {
                    if (i == HeartOfGielinor.ZAMORAK) {
                        continue;
                    }
                    killer.getHeart().setReputation(i, killer.getHeart().getReputation(i) + amount);
                    killer.sendMessage(HeartOfGielinor.getColour(i) + "You have gained " + amount + " reputation with the "
                            + HeartOfGielinor.getGod(i) + " forces.");
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
        if (player.getHeart().getInsigniaSettings()[0] && drop.getItemId() == 37102
                && player.getHeart().getActiveInsignia() != HeartOfGielinor.ZAMORAK && player.getHeart().getActiveInsignia() != -1) {
            player.getHeart().setReputation(player.getHeart().getActiveInsignia(),
                    player.getHeart().getReputation(player.getHeart().getActiveInsignia()) + (amount * 5));
            player.sendMessage("You receive " + (amount * 5) + " reputation for the "
                    + HeartOfGielinor.getGod(player.getHeart().getActiveInsignia()) + " faction.");
            return;
        }
        if (player.getInventory().containsItem(18337, 1)) {
            if (Bonecrusher.handleDrop(player, item)) {
                return;
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
//		if (!lootbeam && GrandExchange.getPrice(item.getId()) >= player.setLootBeam && player.hasLootBeam()) {
//			lootbeam = true;
//		}
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
                player.getDropCollectionHandler().handleBossKills(loot, DropCollectionConstants.BOSS_DATA.TWINS.getNpcId());
                player.catchDrop(loot, () -> World.updateGroundItem(loot, tile, player, 60, 0, finalLootbeam));
            }
        }

        sendDropMessage(player, dropName);
    }

    public TwinFuriesInstance getInstance() {
        return instance;
    }

}
