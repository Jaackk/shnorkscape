package com.rs.game.npc.others;

import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.LootBeamManager;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.lootshare.LootShare;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;

public class Revenant extends NPC {

    private static final long serialVersionUID = -4806711968146659375L;

    public Revenant(final int id, final WorldTile tile, final int mapAreaNameHash, final boolean canBeAttackFromOutOfArea, final boolean spawned) {
        super(id, tile, mapAreaNameHash, canBeAttackFromOutOfArea, spawned);
        setForceTargetDistance(4);
    }

    private int heal;

    @Override
    public void processNPC() {
        super.processNPC();
        if (isDead() || hasFinished()) {
            return;
        }
        if (heal < getMaxHitpoints() && getHitpoints() < getMaxHitpoints() / 2 && Utils.random(4) == 0) {
            final int amount = Utils.random(100);
            heal(amount);
            heal += amount;
        }
    }

    public int getSpawnAnimation() {
        switch (getId()) {
            case 13465:
                return 7410;
            case 13466:
            case 13467:
            case 13468:
            case 13469:
                return 7447;
            case 13470:
            case 13471:
                return 7485;
            case 13472:
                return -1;
            case 13473:
                return 7426;
            case 13474:
                return 7403;
            case 13475:
                return 7457;
            case 13476:
                return 7464;
            case 13477:
                return 7478;
            case 13478:
                return 7416;
            case 13479:
                return 7471;
            case 13480:
                return 7440;
            case 13481:
            default:
                return -1;
        }
    }

    public static final int getGloves(final int skill) {
        switch (skill) {
            case Skills.ATTACK:
            case Skills.STRENGTH:
            case Skills.DEFENCE:
                return 13845;
            case Skills.RANGE:
                return 13846;
            case Skills.MAGIC:
                return 13847;
            case Skills.PRAYER:
                return 13848;
            case Skills.AGILITY:
                return 13849;
            case Skills.WOODCUTTING:
                return 13850;
            case Skills.FIREMAKING:
                return 13851;
            case Skills.MINING:
                return 13852;
            case Skills.HUNTER:
                return 13853;
            case Skills.THIEVING:
                return 13854;
            case Skills.SMITHING:
                return 13855;
            case Skills.FISHING:
                return 13856;
            case Skills.COOKING:
                return 13857;
            default:
                return -1;
        }
    }

    public static final int getBrawlersCharges(final int itemId) {
        switch (itemId) {
            case 13845:
            case 13846:
            case 31847:
                return 300000;
            case 13848:
                return 1274;
            case 13849:
            case 13856:
                return 400;
            case 13850:
            case 13852:
                return 832;
            case 13851:
                return 1202;
            case 13853:
                return 285;
            case 13854:
                return 1000;
            case 13855:
                return 434;
            case 13857:
                return 1109;
            default:
                return 0;
        }
    }

    @Override
    protected void sendDrop(final Player player, NPCDrop drop, boolean lootbeam) {
        final WorldTile tile = new WorldTile(getCoordFaceX(getSize()), getCoordFaceY(getSize()), getPlane());
        final String dropName = ItemDefinitions.getItemDefinitions(drop.getItemId()).getName().toLowerCase();
        if (!lootbeam && player.getLootBeamManager().isViableFloorItem(drop.getItemId())) {
            lootbeam = true;
        }
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
        int amount = drop.getMinAmount() + Utils.getRandom(drop.getExtraAmount());
        if (player.getEquipment().getRingId() == 773 || player.getEquipment().getRingId() == 41069 || player.getEquipment().getRingId() == 48483) {
            if (Utils.randomDouble() <= Settings.PERFECT_RING_DROP_DUPLICATION_RATE) {
                amount *= 2;
                player.sendMessage(Colors.DCYAN + "<shad=000000>Your drop has been doubled thanks to your ring!");
            }
        }
        val loot = new Item(id, amount);
        if (!LootShare.shareLoot(player, this, loot)) {
            boolean finalLootbeam = lootbeam;
            player.catchDrop(loot, () -> World.updateGroundItem(loot, tile, player, 60, 0, finalLootbeam));
        }
        if (lootbeam) {
            LootBeamManager.sendLootBeamMessage(player, player.getLootBeamManager().getCurrentLootBeamType());
        }
        sendDropMessage(player, dropName);
    }

    @Override
    protected void sendDropMessage(final Player player, final String dropName) {
        if (dropName.contains("statius") || dropName.contains("vesta") || dropName.contains("morrigan") || dropName.contains("zuriel")
                || dropName.contains("brawler") || dropName.contains("ancient statue")) {
            for (final Player p : World.getPlayers()) {
                if (p == null) {
                    continue;
                }
                if (p.withinDistance(player, 100)) {
                    p.sendMessage("<img=6><col=ff0000>Local announcement: " + player.getDisplayName() + " received a " + dropName + " as a drop.");
                }
            }
        }
    }

    @Override
    public void drop() {
        try {
            NPCDrop[] drops = NPCDropsDataParser.getDrops(id);
            if (drops == null || getMaxHitpoints() == 1)
                return;
            Player killer = getMostDamageReceivedSourcePlayer();
            if (killer == null) {
                return;
            }
            killer.increaseKillStatistics("revenant", true);
            if (killer.getKillStatistics(119) % 10 == 0) {
                killer.sendMessage("You've killed a total of " + Colors.RED + killer.getKillStatistics(119) + "</col> x " + Colors.RED + "revenants</col>.", true);
            }
            handleRingOfDeath(killer);
            NPCDrop[] possibleDrops = new NPCDrop[drops.length];
            int possibleDropsCount = 0;
            for (NPCDrop drop : drops) {
                if (killer.getTreasureTrails().isScroll(drop.getItemId())) {
                    if (killer.getTreasureTrails().hasClueScrollItem()) {
                        continue;
                    }
                }
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
        } catch (final Exception e) {
            Logger.getGlobal().catching(e);
        } catch (final Error e) {
            Logger.getGlobal().catching(e);
        }
    }

    @Override
    public void spawn() {
        super.spawn();
        heal = 0;
        setNextAnimation(new Animation(getSpawnAnimation()));
    }

}
