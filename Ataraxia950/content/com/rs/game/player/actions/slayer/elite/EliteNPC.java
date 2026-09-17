package com.rs.game.player.actions.slayer.elite;

import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.TertiaryDrop;
import com.rs.game.player.Player;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.actions.slayer.EliteMonster;
import com.rs.game.player.content.Combat;
import com.rs.game.player.content.SlayerTask;
import com.rs.game.player.content.contracts.ContractHandler;
import com.rs.game.player.content.crystaltriskellion.CrystalTriskelion;
import com.rs.game.player.content.items.Defenders;
import com.rs.game.player.content.lootshare.LootShare;
import com.rs.game.player.controllers.DTController;
import com.rs.game.player.controllers.WarriorsGuild;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.NPCDropsDataParser;
import com.rs.utils.data.parsers.npcs.pojos.NPCDrop;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;
import lombok.Getter;
import lombok.val;

/**
 * @author Kris | 3. okt 2018 : 11:22:26
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
public abstract class EliteNPC extends NPC {

    private static final long serialVersionUID = -5956568217280083011L;

    protected static final String[] PREFIXES = new String[]{"Cruel", "Dangerous", "Infamous", "Monstrous", "Notorious", "Powerful",
            "Renowned"};

    protected static final String[] SUFFIXES = new String[]{"of Death", "of Doom", "of Fear", "of Pain", "of Peril", "of Suffering"};

    private static final Class<?>[] INVOCATION_ARGS = new Class<?>[]{
            Player.class, int.class, WorldTile.class
    };


    public static final EliteNPC spawn(final Player player, final WorldTile tile, final NPC npc) {
        val wrapper = EliteMonster.MAP.get(npc.getDefinitions().getName());
        if (wrapper == null) {
            return null;
        }
        try {
            player.sendMessage("An elite monster has spawned!");
            EliteNPC npc1 = wrapper.getClazz().getDeclaredConstructor(INVOCATION_ARGS).newInstance(player, wrapper.getId(), tile);
            if (npc1.getId() == EliteMonster.WATERFIEND.getId()) {
                npc1.setForceMultiAttacked(true);
            }
            return npc1;
        } catch (final Exception e) {
            Logger.getGlobal().catching(e);
        }
        return null;
    }

    @Override
    public void drop() {
        try {
            final NPCDrop[] drops = NPCDropsDataParser.getDrops(id);
            if (drops == null || getMaxHitpoints() == 1) {
                return;
            }
            final Player killer = getMostDamageReceivedSourcePlayer();
            if (killer == null) {
                return;
            }
            if (killer.getControlerManager().getControler() instanceof DTController) {
                return;
            }
            final WorldTile tile = new WorldTile(getCoordFaceX(getSize()), getCoordFaceY(getSize()), getPlane());

            if (killer.getPerkManager().hasPerkActive(DonationPerk.KEY_EXPERT) && Utils.random(250) == 5) {
                killer.sm(Colors.SALMON + "Your Key Expert perk has blessed you with a Crystal Key. It has been added to your bank.");
                killer.getBank().addItem(new Item(989, 1), true);
            }

            boolean dropCatcher = killer.getPerkManager().hasPerkActive(DonationPerk.DROP_CATCHER);
            final Item[] tertiaryDrops = TertiaryDrop.getTertiaryDrop(this);
            if (tertiaryDrops != null) {
                final Item loot = tertiaryDrops[Utils.random(tertiaryDrops.length)];
                if (!LootShare.shareLoot(killer, this, loot)) {
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
            if (name.contains("camel warrior")) {
                if (Utils.random(300) == 1) {
                    sendDrop(killer, new NPCDrop(36019, 1, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a camel staff from a Camel Warrior!", false);
                    QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + killer.getDisplayName() + " has received a camel staff from a Camel Warrior!"));
                }
            }

            if (name.contains("ripper demon") || name.contains("slasher demon")) {
                if (Utils.random(500) == 1) {
                    sendDrop(killer, new NPCDrop(36004, 1, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a ripper claw from a Ripper Demon!", false);
                    QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + killer.getDisplayName() + " has received a ripper claw from a Ripper Demon!"));
                }
            }

            if (name.contains("automaton guardian")) {
                if (Utils.random(250) == 1) {
                    sendDrop(killer, new NPCDrop(27481, 1, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a pair of static gloves from Automaton Guardian!", false);
                    QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + killer.getDisplayName() + " received a pair of static gloves from Automaton Guardian!"));
                }
            }

            if (name.contains("automaton generator")) {
                if (Utils.random(250) == 1) {
                    sendDrop(killer, new NPCDrop(27484, 1, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a pair of tracking gloves from Automaton Generator!", false);
                    QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + killer.getDisplayName() + " received a pair of tracking gloves from Automaton Generator!"));
                }
            }

            if (name.contains("automaton tracer")) {
                if (Utils.random(250) == 1) {
                    sendDrop(killer, new NPCDrop(27487, 1, 1), true);
                    World.sendWorldMessage(Colors.GOLD + "<shad=000000><img=6>News: " + killer.getDisplayName() + " has received a pair of pneumatic gloves from Automaton Tracer!", false);
                    QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + killer.getDisplayName() + " received a pair of pneumatic gloves from Automaton Tracer!"));
                }
            }

            if (name.contains("kalphite king")) {
                if (Defenders.getCurrentTier(killer, 1) && Utils.random(64) == 0) {
                    sendDrop(killer, new NPCDrop(36163, 1, 1), true);
                    World.sendWorldMessage(Colors.CYAN + "<shad=000000><img=6>News: " + killer.getDisplayName() + " received a perfect chitin from Kalphite King!", false);
                    QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + killer.getDisplayName() + " received a perfect chitin from Kalphite King."));
                }
            }
            if (name.contains("nex")) {
                if (Defenders.getCurrentTier(killer, 0) && Utils.random(59) == 0) {
                    sendDrop(killer, new NPCDrop(36159, 1, 1), true);
                    World.sendWorldMessage(Colors.CYAN + "<shad=000000><img=6>News: " + killer.getDisplayName() + " received an ancient emblem from Nex!", false);
                    QueryExecutor.submit(new News(killer, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + killer.getDisplayName() + " received an ancient emblem from Nex!"));
                }
            }
            if (getCombatLevel() >= 90 && Utils.random(750) == 0) {
                World.updateGroundItem(new Item(18778), tile, killer, 60, 0, true);
            }
            final int level = Combat.getSlayerLevelForNPC(getId());
            if (level >= 78 && Utils.random(250) == 0) {
                World.updateGroundItem(new Item(29863), tile, killer, 60, 0, true);
            }

            if (isCyclops(getDefinitions().name) && Utils.random(500) > 450) {
                if (killer.getControlerManager().getControler() instanceof WarriorsGuild) {
                    val loot = new Item(whatDefender());
                    if (!LootShare.shareLoot(killer, this, loot)) {
                        killer.catchDrop(loot, () -> World.updateGroundItem(loot, new WorldTile(this), killer, 60, 0, true));
                    }
                }
            }

            /** Clue scroll drops */
            if (isClueScrollNPC(getDefinitions().name) && Utils.random(200) <= 1) {
                if (!killer.getTreasureTrails().hasClueScrollItem()) {
                    killer.getTreasureTrails().resetCurrentClue();
                    final int itemId = getCombatLevel() < 50 ? 2678 // combat < 50 -
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
            for (int i = 0; i < 3; i++) {
                final NPCDrop[] possibleDrops = new NPCDrop[drops.length];
                int possibleDropsCount = 0;
                for (final NPCDrop drop : drops) {
                    if (killer.getTreasureTrails().isScroll(drop.getItemId())) {
                        if (killer.getTreasureTrails().hasClueScrollItem()) {
                            continue;
                        }
                    }
                    if (drop.getRate() == 100) {
                        if (i == 0) {
                            sendDrop(killer, drop, false);
                        }
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
            SlayerTask.onKill(killer, this);
            ContractHandler.updateContract(killer, this);
        } catch (final Exception e) {
            Logger.getGlobal().catching(e);
        } catch (final Error e) {
            Logger.getGlobal().catching(e);
        }
    }

    protected EliteNPC(final Player owner, final int id, final WorldTile tile) {
        super(id, tile, -1, true, true, true);
        setName(getVaryingName());
        this.owner = owner;
        if (owner.getAttackedBy() != null) {
            owner.getAttackedBy().resetCombat();
        }
        owner.resetCombat();
        getCombat().setTarget(owner);
        setForceMultiArea(owner.isForceMultiArea());
    }

    @Getter
    protected Player owner;
    protected int inactivityTicks;

    /**
     * Generates a random varying name for the NPC.
     *
     * @return a random name.
     */
    protected String getVaryingName() {
        return PREFIXES[Utils.random(PREFIXES.length)] + " " + getName() + " " + SUFFIXES[Utils.random(SUFFIXES.length)];
    }

    @Override
    public void processNPC() {
        super.processNPC();
        if (owner == null) {
            return;
        }
        if (owner.getDistance(this) > 15) {
            owner = null;
            return;
        }
        if (getAttackingDelay() < Utils.currentTimeMillis() && getAttackedByDelay() < Utils.currentTimeMillis()) {
            if (++inactivityTicks >= 300) {
                owner = null;
            }
        } else {
            inactivityTicks = 0;
        }
    }
}
