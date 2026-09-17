package com.rs.game.npc.others;

import com.rs.Settings;
import com.rs.game.Animation;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activities.instances.NihilInstance;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.TertiaryDrop;
import com.rs.game.player.Player;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.content.SlayerTask;
import com.rs.game.player.content.lootshare.LootShare;
import com.rs.game.route.client.PathFinder;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;

public class Nihils extends NPC {

    private static final long serialVersionUID = -5163541424774693272L;

    private final NihilInstance instance;
    private boolean running, insideWall;
    private int cooldown;
    private WorldTile hole, toTile, smokeTile;
    private final NPC nihil = this;
    private int[] random;
    private boolean beenAttacked, healing;
    private int smoke;

    public void setSmokeTile(WorldTile tile) {
        this.smokeTile = tile;
    }

    public void startHealing() {
        healing = true;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                healing = false;
            }
        }, 15);
    }

    public Nihils(int id, WorldTile tile, int mapAreaNameHash, boolean canBeAttackFromOutOfArea, NihilInstance instance) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, false);
        setRun(true);
        this.instance = instance;
    }

    @Override
    public void drop() {
        try {
            NPCDrop[] drops = NPCDropsDataParser.getDrops(id);
            if (drops == null)
                return;
            Player killer = getMostDamageReceivedSourcePlayer();
            if (killer == null)
                return;

            handleRingOfDeath(killer);
            Item[] tertiaryDrops = TertiaryDrop.getTertiaryDrop(this);
            if (tertiaryDrops != null) {
                Item loot = tertiaryDrops[Utils.random(tertiaryDrops.length)];
                if (!LootShare.shareLoot(killer, this, loot)) {
                    if (loot.getId() == 31334 && (killer.getPerkManager().hasPerkActive(DonationPerk.TREASURE_GOBLIN) || killer.getInventory().containsItem(27996, 1))) {
                        killer.getBank().addItem(loot, true);
                        killer.sendMessage("<col=4286f4><shad=000000>Charm Collector: x" + loot.getAmount() + " " + "of " + loot.getName() + " " + (loot.getAmount() == 1 ? "has" : "have") + " " + "been sent to your bank.", true);
                    } else {

                        killer.catchDrop(loot, () -> World.updateGroundItem(loot, new WorldTile(this), killer, 60, 0, false));
                    }
                }
            }
            NPCDrop[] possibleDrops = new NPCDrop[drops.length];
            int possibleDropsCount = 0;
            for (NPCDrop drop : drops) {
                if (drop.getRate() == 100)
                    sendDrop(killer, drop, false);
                else {
                    double rate = drop.getRate();
                    double random = Utils.getRandomDouble(100);
                    if (rate < 30)
                        rate *= Settings.getDropQuantityRate(killer);
                    if (random <= rate && random != 100 && random != 0)
                        possibleDrops[possibleDropsCount++] = drop;
                }
            }
            if (possibleDropsCount > 0)
                sendDrop(killer, possibleDrops[Utils.getRandom(possibleDropsCount - 1)], false);
            SlayerTask.onKill(killer, this);
        } catch (Exception e) {
            Logger.getGlobal().catching(e);
        }
    }

    @Override
    public void handleIngoingHit(final Hit hit) {
        if (healing)
            hit.setLook(HitLook.HEALED_DAMAGE);
        super.handleIngoingHit(hit);
        if (!beenAttacked)
            beenAttacked = true;
    }

    private void sendForceRun() {
        NPC npc = this;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                random = NihilInstance.getRandomLocation(npc.getId());
                PathFinder.simpleWalkTo(nihil, instance.getWorldTile(random[0], random[1]));
                hole = instance.getWorldTile(random[0], random[1]);
                running = true;
            }
        }, 1);

    }

    @Override
    public void processEntity() {
        super.processEntity();
        if (smokeTile != null) {
            if (getId() == 19149) {
                if (instance.getOwner().withinDistance(smokeTile, 2)) {
                    for (int i = 0; i < 7; i++) {
                        if (i != 3 && i != 5)
                            instance.getOwner().getSkills().drainLevel(i, 5);
                    }
                } else
                    smokeTile = null;
            } else if (getId() == 19148) {
                if (smoke == 0)
                    smoke = 3;
                if (instance.getOwner().getHash() == smokeTile.getHash())
                    instance.getOwner().applyHit(new Hit(this, 50, HitLook.REGULAR_DAMAGE));
                smoke--;
                if (smoke == 0)
                    smokeTile = null;
            }
        }
        if (beenAttacked)
            return;
        if (this.withinDistance(instance.getOwner(), 1) && this.getNextWalkDirection() != -1)
            instance.getOwner().applyHit(new Hit(this, Utils.random(150), HitLook.REGULAR_DAMAGE));
        if (cooldown > 0)
            cooldown--;
        if (Utils.random(10) == 0 && !running && !insideWall)
            sendForceRun();
        if (running && cooldown == 0) {
            WorldTile to = null;
            for (int i = 0; i < NihilInstance.NIHIL_PATHS.length; i++) {
                if (this.withinDistance(instance.getWorldTile(NihilInstance.NIHIL_PATHS[i][0], NihilInstance.NIHIL_PATHS[i][1]), 2)) {
                    to = instance.getWorldTile(NihilInstance.NIHIL_PATHS[i][0], NihilInstance.NIHIL_PATHS[i][1]);
                    break;
                }
            }
            if (this.getNextWalkDirection() == -1 && to != null) {
                insideWall = true;
                this.setNextFaceWorldTile(new WorldTile(random[2], random[3], this.getPlane()));
                nihil.setNextAnimation(new Animation(23020));
                running = false;
            }
        }
        if (insideWall) {
            for (int i = 0; i < 5; i++) {
                random = NihilInstance.NIHIL_PATHS[Utils.random(NihilInstance.NIHIL_PATHS.length)];
                if (hole != instance.getWorldTile(random[0], random[1])) {
                    toTile = instance.getWorldTile(random[0], random[1]);
                    break;
                }
            }
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    if (toTile != null)
                        nihil.setNextWorldTile(toTile);
                    else
                        nihil.setNextWorldTile(instance.getWorldTile(NihilInstance.NIHIL_PATHS[0][0], NihilInstance.NIHIL_PATHS[0][1]));
                    nihil.setNextFaceWorldTile(new WorldTile(random[4], random[5], nihil.getPlane()));
                    sendForceRun();
                    insideWall = false;
                    cooldown = 7;
                }
            });
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
        int respawnDelay = getCombatDefinitions().getRespawnDelay();
        if (Settings.DEBUG) {
            Logger.getGlobal().info("Respawn task initiated: [" + getName() + "]; time: [" + respawnDelay + "].");
        }
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                try {
                    if (instance != null && !instance.isOwnerInstance())
                        return;
                    spawn();
                } catch (final Throwable e) {
                    Logger.getGlobal().catching(e);
                }
            }
        }, respawnDelay);
    }

}
