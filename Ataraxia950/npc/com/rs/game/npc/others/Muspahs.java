package com.rs.game.npc.others;

import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Entity;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.MuspahInstance;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.TertiaryDrop;
import com.rs.game.npc.combat.NPCCombatDefinition;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.content.SlayerTask;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.lootshare.LootShare;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

import java.util.concurrent.TimeUnit;

public class Muspahs extends NPC {

    private static final long serialVersionUID = -5163541424774693272L;

    private final MuspahInstance instance;

    public Muspahs(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final MuspahInstance instance) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, true);
        this.instance = instance;
        setForceMultiArea(true);
        setForceTargetDistance(30);
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        if (hit.isSpecialHit()) {
            hit.setDamage(hit.getDamage() * 2);
        }
        super.handleIngoingHit(hit);
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
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    setNextAnimation(new Animation(defs.getDeathEmote()));
                } else if (loop >= defs.getDeathDelay()) {
                    drop();
                    reset();
                    setLocation(instance.getRandomTile());
                    finish();
                    setRespawnTask();
                    stop();
                }
                loop++;
            }
        }, 0, 1);
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
            SlayerTask.onKill(killer, this);
            handleRingOfDeath(killer);
            final Item[] tertiaryDrops = TertiaryDrop.getTertiaryDrop(this);
            if (tertiaryDrops != null) {
                final Item loot = tertiaryDrops[Utils.random(tertiaryDrops.length)];

                if (!LootShare.shareLoot(killer, this, loot)) {
                    if (loot.getId() == 31334 && (killer.getPerkManager().hasPerkActive(DonationPerk.TREASURE_GOBLIN) || killer.getInventory().containsItem(27996, 1))) {
                        killer.getBank().addItem(loot, true);
                        killer.sendMessage("<col=4286f4><shad=000000>Charm Collector: x" + loot.getAmount() + " " + "of " + loot.getName() + " " + (loot.getAmount() == 1 ? "has" : "have") + " " + "been sent to your bank.", true);
                    } else {
                        killer.catchDrop(loot, () -> World.updateGroundItem(loot, new WorldTile(getCoordFaceX(getSize()), getCoordFaceY(getSize()), getPlane()), killer, 60, 0, false));
                    }
                }
                final NPCDrop[] possibleDrops = new NPCDrop[drops.length];
                int possibleDropsCount = 0;
                for (final NPCDrop drop : drops) {
                    if (drop.getRate() == 100) {
                        sendDrop(killer, drop, false);
                    } else {
                        double rate = drop.getRate();
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
            }
        } catch (final Exception e) {
            Logger.getGlobal().catching(e);
        } catch (final Error e) {
            Logger.getGlobal().catching(e);
        }
    }

    @Override
    public void setRespawnTask() {
        if (!hasFinished()) {
            reset();
            setLocation(getRespawnTile());
            finish();
        }
        if (instance != null && !instance.isOwnerInstance())
            return;
        final long respawnDelay = getCombatDefinitions().getRespawnDelay() * 600;
        if (Settings.DEBUG) {
            Logger.getGlobal().info("Respawn task initiated: [" + getName() + "]; time: [" + respawnDelay + "].");
        }
        CoresManager.getServiceProvider().executeWithDelay(() -> {
            try {
                if (instance != null && !instance.isOwnerInstance())
                    return;
                spawn();
                setNextAnimation(new Animation(22986));
            } catch (final Throwable e) {
                Logger.getGlobal().catching(e);
            }
        }, respawnDelay, TimeUnit.MILLISECONDS);
    }

}
