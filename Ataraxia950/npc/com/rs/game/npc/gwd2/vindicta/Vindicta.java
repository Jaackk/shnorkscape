package com.rs.game.npc.gwd2.vindicta;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cores.CoresManager;
import com.rs.cores.FixedLengthRunnable;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.VindictaInstance;
import com.rs.game.item.Item;
import com.rs.game.item.floor.LootBeamManager;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.impl.gwd2.VindictaCombat;
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
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Tom
 * @date April 9, 2017
 */

public class Vindicta extends NPC {

    private static final long serialVersionUID = 3958941320672345882L;

    private int phase;
    private int gorvekPhase;
    public VindictaInstance instance;
    public List<WorldTile[]> tileSets = new ArrayList<WorldTile[]>();
    private final List<Player> hitPlayers = new ArrayList<Player>();
    private final List<WorldTile> safeTiles = new ArrayList<WorldTile>();
    private boolean hurricane;

    public static final int[][] CORNERS = new int[][]{{34, 34}, {34, 9}, {17, 9}, {17, 34}};

    @Override
    public boolean isIntelligentRouteFinder() {
        return true;
    }

    @Override
    public boolean canWalkNPC(final int toX, final int toY) {
        return true;
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

    public boolean performedHurricane() {
        return hurricane;
    }

    public void setGorvekPhase(final int phase) {
        gorvekPhase = phase;
    }

    public int getGorvekPhase() {
        return gorvekPhase;
    }

    public void setHasPerformedHurricane() {
        hurricane = true;
    }

    public void addSafeTile(final WorldTile t) {
        safeTiles.add(t);
    }

    public void removeSafeTile(final WorldTile t) {
        safeTiles.remove(t);
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

    public Vindicta(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final boolean spawned, final VindictaInstance instance) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        setIntelligentRouteFinder(true);
        setForceTargetDistance(50);
        setForceAgressive(true);
        setRun(true);
        setNoDistanceCheck(true);
        this.instance = instance;
    }

    @Override
    public void sendDeath(final Entity source) {
        super.sendDeath(source);
        instance.getPlayers().forEach(player -> {
            if (player.isGroupIronman()) {
                player.gimTracker.incrementBpGained(5);
            }
            player.getAchievements().updateProgress(1, AchievementList.KILL_500_GWD2_BOSSES);
            player.getActivityTimersManager().finishBossTimer(Vindicta.this);
        });
        for (int s = 0; s < tileSets.size(); s++) {
            if (tileSets.get(s) == null) {
                continue;
            }
            for (int i = 0; i < tileSets.get(s).length; i++) {
                if (tileSets.get(s)[i] != null) {
                    for (final Player p : instance.getPlayers()) {
                        p.getPackets().sendGraphics(new Graphics(-1), tileSets.get(s)[i]);
                    }
                }
            }
        }
        tileSets.clear();
    }

    @Override
    public int getCapDamage() {
        return 1250;
    }

    public void addFires(final WorldTile[] tiles) {
        final List<WorldTile> t = new ArrayList<WorldTile>();
        try {
            for (int i = 0; i < tiles.length; i++) {
                final WorldTile dest = tiles[i];
                if (dest == null) {
                    continue;
                }
                loop:
                for (int x = 0; x < tileSets.size(); x++) {
                    for (int a = 0; a < tileSets.get(x).length; a++) {
                        if (tileSets.get(x)[a] != null && tileSets.get(x)[a].getHash() == dest.getHash()) {
                            getInstance().getPlayers().forEach(p -> p.getPackets().sendGraphics(new Graphics(-1), dest));
                            break loop;
                        }
                    }
                }
                for (int x = safeTiles.size() - 1; x > 0; x--) {
                    if (safeTiles.get(x).getTileHash() == dest.getTileHash()) {
                        safeTiles.remove(x);
                    }
                }
                t.add(dest);
            }
            tileSets.add(tiles);
        } catch (final Exception e) {
        }
        CoresManager.getServiceProvider().scheduleFixedLengthTask(new FixedLengthRunnable() {
            private int ticks;

            @Override
            public boolean repeat() {
                try {
                    if (t.isEmpty()) {
                        return false;
                    }
                    if (ticks % 15 == 0 && !t.isEmpty()) {
                        getInstance().getPlayers().forEach(p  -> {if(!t.isEmpty())p.getPackets().sendGraphics(new Graphics(6112), t.remove(0));});
                    }
                    ticks++;
                } catch (final Exception e) {
                    Logger.getGlobal().catching(e);
                    return false;
                }
                return true;
            }

        }, 0, 1, TimeUnit.MILLISECONDS);
    }

    public void removeFires(final WorldTile[] tiles) {
        tileSets.remove(tiles);
    }

    @Override
    public void spawn() {
        super.spawn();
        setNextNPCTransformation(22459);
    }

    private void rideOnDragon() {
        final NPC gorvek = getInstance().getGorvek();
        getCombat().setCombatDelay(5);
        gorvek.setNextWorldTile(new WorldTile(getX(), getY(), 1));
        gorvek.setNextAnimation(new Animation(28276));
        setNextAnimation(new Animation(28263));
        setNextNPCTransformation(getInstance().isHardMode() ? 22462 : 22460);
        setRun(true);
        setPhase(0);
        gorvek.setNextWorldTile(new WorldTile(getInstance().getWorldTile(63, 62)));
    }

    private final void checkForceAttack() {
        if (getId() != 22463 && hurricane && getInstance().getPlayers().size() == 1 && getCombat().getTarget() != null && getCombat().getTarget().getDistance(this) > getSize() && getCombat().getTarget().getX() <= getInstance().getWorldTile(39, 38).getX()) {
            if (getTemporaryAttributtes().get("rangedDelay") != null) {
                final long delay = (long) getTemporaryAttributtes().get("rangedDelay");
                if (delay > Utils.currentTimeMillis()) {
                    return;
                }
            }
            if (getId() == 22459) {
                if (getPhase() != 2 && getPhase() != 6) {
                    if (getPhase() < 2) {
                        setPhase(2);
                    } else {
                        setPhase(6);
                    }
                }
            } else {
                if (getPhase() != 1 && getPhase() != 3) {
                    if (getPhase() < 1) {
                        setPhase(1);
                    } else {
                        setPhase(3);
                    }
                }
            }
            resetWalkSteps();
            setFreezeDelay(VindictaCombat.rangedAttack(this, getCombat().getTarget()) - 2);
            getTemporaryAttributtes().put("rangedDelay", Utils.currentTimeMillis() + 3000);
        }
    }

    @Override
    public void processNPC() {
        super.processNPC();
        if (!isUnderCombat() && getInstance().getPlayers().size() == 0 && !hasFinished()) {
            finish();
            return;
        }
        hitPlayers.clear();
        checkForceAttack();
        instance.getPlayers().forEach(p -> {
            for (int i = 0; i < tileSets.size(); i++) {
                if (tileSets.get(i) == null) {
                    continue;
                }
                loop:
                for (int x = 0; x < tileSets.get(i).length; x++) {
                    if (tileSets.get(i)[x] != null && p.withinDistance(tileSets.get(i)[x], 1)) {
                        for (final WorldTile t : safeTiles) {
                            if (p.getTileHash() == t.getTileHash()) {
                                continue loop;
                            }
                        }
                        if (!hitPlayers.contains(p)) {
                            hitPlayers.add(p);
                        }
                    }
                }
            }
        });
        Vindicta thisNPC = this;
        hitPlayers.forEach(p -> { if(thisNPC != null && !thisNPC.isDead() && !thisNPC.hasFinished()) p.applyHit(new Hit(null, getInstance().isHardMode() ? Utils.random(100, 200) : Utils.random(50, 75), HitLook.MAGIC_DAMAGE)); });
        if (getId() == 22459 && getHitpoints() <= 10000 || getId() == 22461 && getHitpoints() <= 15000) {
            rideOnDragon();
        }
    }

    @Override
    public void drop() {
        try {
            final NPCDrop[] drops = NPCDropsDataParser.getDrops(id);
            if (drops == null) {
                return;
            }
            final Player killer = getMostDamageReceivedSourcePlayer();
            if (killer == null) {
                return;
            }
            increaseKillStatistics(killer, getInstance().isHardMode() ? "vindicta(cm)" : "vindicta");
            int chance = instance.isHardMode() ? 1000 : 2000;
            if (killer.getPerkManager().hasPerkActive(DonationPerk.PETSCHANTER)) {
                chance *= 0.75;
            }
            if (Utils.random(chance) == 0) {
                final Item item = Utils.random(2) == 0 ? new Item(37180) : new Item(37181);
                HeartOfGielinor.sendPetDrop(killer, killer.hasItem(item) ? new Item(item.getId() == 37180 ? 37181 : 37180) : item, BOSS_DATA.VINDY);
                killer.getAchievements().updateProgress(1, AchievementList.OBTAIN_BOSS_PET);
            }
            if (killer.getKillStatistics(getInstance().isHardMode() ? 120 : 115) == 1) {
                final int amount = getInstance().isHardMode() ? 100 : 50;
                for (int i = 0; i < 4; i++) {
                    if (i == HeartOfGielinor.ZAROS) {
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
        if (player.getHeart().getInsigniaSettings()[0] && drop.getItemId() == 37100 && player.getHeart().getActiveInsignia() != HeartOfGielinor.ZAROS && player.getHeart().getActiveInsignia() != -1) {
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
            if (player.getEquipment().getRingId() == 773 || player.getEquipment().getRingId() == 48483 || player.getEquipment().getRingId() == 41069) {
                if (Utils.randomDouble() <= Settings.PERFECT_RING_DROP_DUPLICATION_RATE) {
                    amount *= 2;
                    player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop has been doubled thanks to your ring!");
                }
            }
            val loot = new Item(id, amount);
            player.getDropCollectionHandler().handleBossKills(new Item(id, amount), DropCollectionConstants.BOSS_DATA.VINDY.getNpcId());
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

    public int getPhase() {
        return phase;
    }

    public void nextPhase() {
        phase++;
    }

    public void setPhase(final int phase) {
        this.phase = phase;
    }

    public VindictaInstance getInstance() {
        return instance;
    }
}
