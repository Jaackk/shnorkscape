package com.rs.game.player.content.jujupotions.harmonypillar;

import com.google.common.collect.ImmutableList;
import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.player.FarmingManager;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.Pots;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import lombok.Setter;
import lombok.val;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class HarmonyPillarManager implements Serializable {

    static final double GROWTH_XP_NEEDED = 75_000;
    static final int MAX_HARMONY_MOSS = 9;
    static final double HARVEST_XP = 1133.3;
    static final double PLANT_XP = 165.0;
    static final int PLANT_LVL = 75;
    private static final ImmutableList<WorldTile> ALL_PILLARS = ImmutableList.of(
            new WorldTile(2231, 3400, 1),
            new WorldTile(2230, 3395, 1),
            new WorldTile(2218, 3400, 1),
            new WorldTile(2219, 3393, 1),
            new WorldTile(2234, 3400, 1)
    );

    private static final long serialVersionUID = -2234247447721155566L;
    private final Map<WorldTile, HarmonyPillar> pillars = new HashMap<>(ALL_PILLARS.size());
    @Setter
    private transient Player player;

    public void addXp(int id, double xp) {
        for (HarmonyPillar next : pillars.values()) {
            if (next.getAttunedTo() == id) {
                next.addXp(xp);
            }
        }
    }

    public boolean plant(WorldObject pillarObj) {
        val pillar = pillars.get(pillarObj);
        if (pillar == null) {
            return false;
        }
        if (player.getSkills().getLevel(Skills.FARMING) < PLANT_LVL) {
            player.sendMessage("You need a Farming level of " + PLANT_LVL + " to plant this seed.");
            return false;
        }
        if (!player.getInventory().containsOneItem(5343)) {
            player.sendMessage("You need a seed dibber to do this.");
            return false;
        }
        if (!player.getInventory().containsOneItem(5325)) {
            player.sendMessage("You need a gardening trowel to do this.");
            return false;
        }
        player.sendMessage("You plant the harmony moss seed under the harmony pillar.");
        player.setNextAnimation(FarmingManager.SEED_DIPPING_ANIMATION);
        player.getSkills().addXp(Skills.FARMING, PLANT_XP);
        player.getInventory().deleteItem(new Item(32665, 1));
        pillar.setState(HarmonyPillarState.GROWING_1);
        return true;
    }

    public boolean handleOption1(WorldObject pillarObj) {
        val pillar = pillars.get(pillarObj);
        if (pillar == null) {
            return false;
        }

        switch (pillar.getState()) {
            case GROWN:
                return pick(pillar);
            case WEEDED:
                return rake(pillar);
        }
        return false;
    }

    public boolean handleOption2(WorldObject pillarObj) {
        val pillar = pillars.get(pillarObj);
        if (pillar == null) {
            return false;
        }
        switch (pillar.getState()) {
            case GROWN:
            case WEEDED:
            case CLEAR:
            case GROWING_1:
            case GROWING_2:
            case GROWING_3:
            case GROWING_4:
                return inspect(pillar);
        }
        return false;
    }

    public boolean handleOption3(WorldObject pillarObj) {
        val pillar = pillars.get(pillarObj);
        if (pillar == null) {
            return false;
        }

        switch (pillar.getState()) {
            case GROWN:
                return clear(pillar);
        }
        return false;
    }

    public boolean handleOption4(WorldObject pillarObj) {
        val pillar = pillars.get(pillarObj);
        if (pillar == null) {
            return false;
        }

        switch (pillar.getState()) {
            case GROWN:
            case CLEAR:
            case WEEDED:
            case GROWING_1:
            case GROWING_2:
            case GROWING_3:
            case GROWING_4:
                return guide(pillar);
        }
        return false;
    }

    private boolean pick(HarmonyPillar pillar) {
        if (pillar.getState() == HarmonyPillarState.GROWN) {
            player.getActionManager().setAction(new Action() {

                @Override
                public boolean start(Player player) {
                    return true;
                }

                @Override
                public boolean process(Player player) {
                    if (pillar.mossAmount > 0) {
                        return true;
                    } else {
                        player.sendMessage("You pick all of the moss from the harmony pillar; produce harvested: " + Colors.RED + Utils.getFormattedNumber(player.produceGathered) + "</col>.", true);
                        player.setNextAnimation(new Animation(-1));
                        pillar.setState(HarmonyPillarState.WEEDED);
                        return false;
                    }
                }

                @Override
                public int processWithDelay(Player player) {
                    if (!player.getInventory().hasFreeSlots()) {
                        player.sendMessage("You don't have enough space in your inventory.", true);
                        return -1;
                    }
                    player.sendMessage("You pick some harmony moss.");
                    player.setNextAnimation(FarmingManager.BUSH_PICKING_ANIMATION);
                    player.addProduceGathered();
                    player.getSkills().addXp(Skills.FARMING, HARVEST_XP);
                    boolean jujuActive = player.jujuPotions.isActive(Pots.Effects.PERFECT_FARMING_JUJU) &&
                            ThreadLocalRandom.current().nextInt(4) == 0;
                    int amount = 1;
                    if (jujuActive) {
                        amount++;
                        player.sendMessage("Through the power of juju, you receive another harmony moss!");
                    }
                    player.getInventory().addItem(32947, amount);
                    pillar.mossAmount--;
                    return 2;
                }

                @Override
                public void stop(Player player) {
                    setActionDelay(player, 1);
                }
            });
            return true;
        }
        return false;
    }

    private boolean rake(HarmonyPillar pillar) {
        if (pillar.getState() == HarmonyPillarState.WEEDED) {
            player.getActionManager().setAction(new Action() {
                private int loops = 3;

                @Override
                public boolean start(Player player) {
                    if (!player.getInventory().containsOneItem(5341)) {
                        player.sendMessage("You need a rake to do this.");
                        return false;
                    }
                    if (player.getSkills().getLevel(Skills.FARMING) < PLANT_LVL) {
                        player.sendMessage("You need a Farming level of " + PLANT_LVL + " to do this.");
                        return false;
                    }
                    return true;
                }

                @Override
                public boolean process(Player player) {
                    return loops > 0;
                }

                @Override
                public int processWithDelay(Player player) {
                    player.setNextAnimation(FarmingManager.RAKING_ANIMATION);
                    if (--loops == 0) {
                        pillar.setState(HarmonyPillarState.CLEAR);
                        return -1;
                    }
                    return 1;
                }

                @Override
                public void stop(Player player) {
                    setActionDelay(player, 1);
                }
            });
            return true;
        }
        return false;
    }

    private String getSkillName(HarmonyPillar pillar) {
        String skillName = pillar.getAttunedTo() >= 0 ? Skills.SKILL_NAME[pillar.getAttunedTo()] : null;
        if (skillName == null) {
            // Pillar is not attuned to anything?
            pillar.setState(HarmonyPillarState.WEEDED);
            Dialogue.sendSingleDialogue(player, "Your pillar has been reset because it was bugged.");
            return null;
        }
        return skillName;
    }

    private void sendGrowingProgress(HarmonyPillar pillar, int stage) {
        String skillName = getSkillName(pillar);
        if (skillName == null)
            return;
        double currentXp = pillar.getCurrentXp();
        int result = (int) ((currentXp / GROWTH_XP_NEEDED) * 100.0);
        Dialogue.sendSingleDialogue(player, "Growing stage " + Colors.DARK_RED + stage + "/4</col>; Attuned to " + Colors.DARK_RED + skillName + "</col>. Progress until next stage ~ " + Colors.DARK_RED + result + "</col>%.");
    }

    public boolean inspect(HarmonyPillar pillar) {
        switch (pillar.getState()) {
            case WEEDED:
                Dialogue.sendSingleDialogue(player, "This pillar needs to be raked before a harmony moss seed can be planted.");
                break;
            case CLEAR:
                String skillName = getSkillName(pillar);
                if (skillName == null)
                    break;
                long hours = LocalDateTime.now().until(pillar.getReattuneDate(), ChronoUnit.HOURS);
                String str = hours > 0 ? "You can plant a harmony moss seed, or wait " + Colors.DARK_RED + hours + "</col> more hour(s) for the pillar to attune to a different skill." :
                        "You can plant a harmony moss seed, or wait a little longer for the pillar to attune to a different skill.";
                Dialogue.sendSingleDialogue(player, "This pillar is attuned to the " + Colors.DARK_RED + skillName + "</col> skill.", str);
                break;
            case GROWING_1:
                sendGrowingProgress(pillar, 1);
                break;
            case GROWING_2:
                sendGrowingProgress(pillar, 2);
                break;
            case GROWING_3:
                sendGrowingProgress(pillar, 3);
                break;
            case GROWING_4:
                sendGrowingProgress(pillar, 4);
                break;
            case GROWN:
                Dialogue.sendSingleDialogue(player, "This pillar is ready for harvest!");
                break;
        }
        return true;
    }

    public boolean guide(HarmonyPillar pillar) {
        switch (pillar.getState()) {
            case WEEDED:
            case CLEAR:
            case GROWING_1:
            case GROWING_2:
            case GROWING_3:
            case GROWING_4:
            case GROWN:
                Dialogue.sendSingleDialogue(player, "Google 'RS Wiki Farming' if you need help with training Farming.");
                break;
        }
        return true;
    }

    public boolean clear(HarmonyPillar pillar) {
        player.getActionManager().setAction(new Action() {
            private int loops = 2;

            @Override
            public boolean start(Player player) {
                if (!player.getInventory().containsOneItem(952) && !player.getToolBelt().contains(952)) {
                    player.sendMessage("You need a spade to do this.");
                    return false;
                }
                player.sendMessage("You start scraping off the moss from the pillar.");
                return true;
            }

            @Override
            public boolean process(Player player) {
                if (loops <= 0) {
                    player.sendMessage("You remove all the moss from the pillar.");
                    player.setNextAnimation(new Animation(-1));
                    pillar.setState(HarmonyPillarState.CLEAR);
                    return false;
                }
                return true;
            }

            @Override
            public int processWithDelay(Player player) {
                player.setNextAnimation(FarmingManager.SPADE_ANIMATION);
                if (Utils.random(3) == 0)
                    loops--;
                return 2;
            }

            @Override
            public void stop(Player player) {
                setActionDelay(player, 3);
            }
        });
        return true;
    }

    public void init() {
        for (WorldTile tile : ALL_PILLARS) {
            HarmonyPillar found = pillars.get(tile);
            if (found == null) { // Register all pillar locations.
                HarmonyPillar newPillar = new HarmonyPillar(tile);
                pillars.put(tile, newPillar);
                found = newPillar;
            } else if (found.shouldWeedsGrow()) { // Reweed the pillar if it's been CLEAR for > 24h.
                val finalFound = found;
                CoresManager.getServiceProvider().addGameTask(() -> finalFound.setState(HarmonyPillarState.WEEDED));
            }

            // Spawn pillar object.
            found.setPlayer(player);
            CoresManager.getServiceProvider().addGameTask(found::spawn);
        }
    }

    Map<WorldTile, HarmonyPillar> getPillars() {
        return pillars;
    }
}
