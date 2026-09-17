package com.rs.game.npc.gwd2;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.LootBeamManager;
import com.rs.game.npc.NPC;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.Player;
import com.rs.game.player.actions.herblore.herbicide.Herbicide;
import com.rs.game.player.content.HeartOfGielinor;
import com.rs.game.player.content.items.Bonecrusher;
import com.rs.game.player.content.items.CharmingImp;
import com.rs.game.player.content.items.CoinAccumulator;
import com.rs.game.player.content.lootshare.LootShare;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class SerenFaction extends FactionNPC {

    private static final long serialVersionUID = 6594122811840384188L;

    public SerenFaction(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final boolean spawned) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        setForceTargetDistance(8);
        setCombat(new FactionCombatHandler(this));
    }

    @Override
    public boolean canWalkNPC(final int toX, final int toY) {
        return true;
    }

    @Override
    public ArrayList<Entity> getPossibleTargets() {
        final int size = getSize();
        final ArrayList<Entity> possibleTarget = new ArrayList<Entity>();
        for (final int regionId : getMapRegionsIds()) {
            final List<Integer> playerIndexes = World.getRegion(regionId).getPlayerIndexes();
            if (playerIndexes != null) {
                for (final int playerIndex : playerIndexes) {
                    final Player player = World.getPlayers().get(playerIndex);
                    if (player == null || player.isUnderCombat() || player.getFindTargetDelay() > Utils.currentTimeMillis() || player.isDead() || player.hasFinished() || !player.isRunning() || player.getAppearence().isHidden() || player.getHeart().getActiveInsignia() == HeartOfGielinor.SEREN || !Utils.isOnRange(getX(), getY(), size, player.getX(), player.getY(), player.getSize(), getForceTargetDistance() > 0 ? getForceTargetDistance() : 1) || !clipedProjectile(player, false)) {
                        continue;
                    }
                    possibleTarget.add(player);
                }
            }
            final List<Integer> npcsIndexes = World.getRegion(regionId).getNPCsIndexes();
            if (npcsIndexes != null) {
                for (final int npcIndex : npcsIndexes) {
                    final NPC npc = World.getNPCs().get(npcIndex);
                    if (npc == null || npc == this || npc instanceof Familiar || npc.getId() == 16980 || npc.isDead() || npc.hasFinished() || npc instanceof SerenFaction || !Utils.isOnRange(getX(), getY(), size, npc.getX(), npc.getY(), npc.getSize(), getForceTargetDistance() > 0 ? getForceTargetDistance() : 1) || !npc.getDefinitions().hasAttackOption() || !clipedProjectile(npc, false)) {
                        continue;
                    }
                    possibleTarget.add(npc);
                }
            }
        }
        return possibleTarget;
    }

    @Override
    public boolean checkAgressivity() {
        if (isUnderCombat() || getCombat().getTarget() != null || getTarget() != null) {
            return false;
        }
        final ArrayList<Entity> targets = getPossibleTargets();
        if (!targets.isEmpty()) {
            Entity target = null;
            for (int i = 0; i < targets.size(); i++) {
                target = targets.get(i);
                if (target.getTarget() == null) {
                    target.setTarget(this);
                    setAttackedBy(target);
                    setFindTargetDelay(Utils.currentTimeMillis() + 5000);
                    break;
                }
            }
            if (target == null) {
                target = targets.get(Utils.random(targets.size()));
            }
            setTarget(target);
            target.setAttackedBy(this);
            target.setFindTargetDelay(Utils.currentTimeMillis() + 5000);
            return true;
        }
        return false;
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
        if (player.getHeart().getInsigniaSettings()[1] && drop.getItemId() == 37008 && (player.getInventory().hasFreeSlots() || !player.getInventory().hasFreeSlots() && player.getInventory().containsItem(37008, 1))) {
            player.getInventory().addItem(37008, amount);
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
            if (player.getEquipment().getRingId() == 773 || player.getEquipment().getRingId() == 41069) {
                if (Utils.randomDouble() <= Settings.PERFECT_RING_DROP_DUPLICATION_RATE) {
                    amount *= 2;
                    player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop has been doubled thanks to your ring!");
                }
            }
            val loot = new Item(id, amount);
            if (!LootShare.shareLoot(player, this, loot)) {
                boolean finalLootbeam = lootbeam;
                player.catchDrop(loot, () -> World.updateGroundItem(loot, tile, player, 60, 0,finalLootbeam));
            }
        }
        if (lootbeam) {
            LootBeamManager.sendLootBeamMessage(player, player.getLootBeamManager().getCurrentLootBeamType());
        }
        sendDropMessage(player, dropName);
    }

    @Override
    public void sendDeath(final Entity source) {
        final NPCCombatDefinition defs = getCombatDefinitions();
        resetWalkSteps();
        getCombat().removeTarget();
        setNextAnimation(null);
        final Player killer = getMostDamageReceivedSourcePlayer();
        if (killer != null) {
            if (killer.getHeart().getBountyTargetFaction() == HeartOfGielinor.SEREN) {
                if (killer.getHeart().getCurrentBounty() != 0) {
                    killer.getHeart().decrementBounty();
                }
            }
            killer.getHeart().sendMultiplier();
        }
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                } else if (loop >= defs.getDeathDelay()) {
                    if (source instanceof Player) {
                        final Player player = (Player) source;
                        player.getHeart().incrementKillcount(HeartOfGielinor.SEREN);
                    }
                    drop();
                    reset();
                    setLocation(getRespawnTile());
                    finish();
                    if (!isSpawned()) {
                        setRespawnTask();
                    }
                    stop();
                }
                loop++;
            }
        }, 0, 0);
    }

    @Override
    public void setRespawnTask() {
        if (!hasFinished()) {
            reset();
            setLocation(getRespawnTile());
            finish();
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
        }, getCombatDefinitions().getRespawnDelay());
    }
}
