package com.rs.game.activites.dnd.eviltree;

import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.activites.dnd.eviltree.action.NurtureEvilSaplingAction;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.activites.quest.root_of_evil.FoundRootD;
import com.rs.game.activites.quest.root_of_evil.RootOfEvil;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.content.items.HerbloreBox;
import com.rs.game.player.content.pet.Pets;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates rewards for the tree depending on the type and state of the {@link EvilTree}.
 *
 * @author lare96 <http://github.com/lare96>
 */
public final class EvilTreeRewards {
    private static final boolean PET_ACTIVE = true;

    private static final class RewardItem {
        private final int id;
        private final int min;
        private final int max;
        private final int chance;

        private RewardItem(int id, int min, int max, int chance) {
            if (min <= 0) {
                min = 1;
            }
            if (max < min) {
                max = min;
            }
            this.id = id;
            this.min = min;
            this.max = max;
            this.chance = chance;
        }

        private RewardItem(int id, int amount, int chance) {
            this(id, amount, amount, chance);
        }

        private RewardItem setNoted() {
            ItemDefinitions def = ItemDefinitions.getItemDefinitions(id);
            if (def.isStackable() || def.certId <= 0) {
                return this;
            }
            return new RewardItem(def.certId, min, max, chance);
        }

        public Item toItem() {
            if (chance <= 1 || ThreadLocalRandom.current().nextInt(chance) == 0) {
                int amount;
                if (min == max) {
                    amount = min;
                } else {
                    amount = ThreadLocalRandom.current().nextInt(min, max + 1);
                }
                return new Item(id, amount);
            }
            return null;
        }
    }

    private final EvilTree tree;
    private final Set<String> claimedRewards = new HashSet<>();
    private boolean showRewards = true;

    public EvilTreeRewards(EvilTree tree) {
        this.tree = tree;
    }

    public void giveRewards(Player player) {
        if (tree.getTreeObject() == null) {
            return;
        }
        if (claimedRewards.contains(player.getUsername())) {
            player.sendMessage("You have already looted this Evil Tree.");
            return;
        }
        int helpPercentage = getHelpPercentage(player);
        if (helpPercentage == 0) {
            player.getDialogueManager().startDialogue(sendCannotClaim());
            return;
        }
        claimedRewards.add(player.getUsername());
        int rolls = 1;
        if (player.getPet() != null && player.getPet().getType() == Pets.ENTLING && !EvilTree.canNurtureEntling(player)) {
            player.sendMessage(Colors.RED + "Entling is in a good mood! He channels the tree for extra rewards.");
            player.getPet().setNextGraphics(new Graphics(1638));
            rolls++;
        }
        player.setNextAnimation(new Animation(24887));
        player.sendMessage("You reach into the trunk...");
        player.sendMessage("Your Evil Tree reward potential is: " + Colors.RED + (helpPercentage > 100 ? "100" : helpPercentage) + "</col>%.");
        final int finalRolls = rolls;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                if (tree.getTreeObject() != null) {
                    tree.getTreeObject().getDamageSet().remove(player.getUsername(), Integer.MAX_VALUE);
                }
                showRewards = true;
                List<Item> rewards = generateRewards(EvilTreeRewards.this, player, tree.getType(), tree.isInstanced(), helpPercentage, finalRolls);
                if (showRewards) {
                    player.getPackets().sendItems(141, Iterables.toArray(rewards, Item.class));
                    player.getInterfaceManager().sendInterface(364);
                }
                for (Item item : rewards) {
                    player.addItem(item);
                }
            }
        }, 2);
    }

    public static List<Item> generateRewards(EvilTreeRewards thisRewards, Player player, EvilTreeType type, boolean isInstanced, double helpPercentage, int baseRolls) {
        double multiplier = type.getRewardsBonus();
        List<RewardItem> possibleRewards = new ArrayList<>();
        if (helpPercentage >= 100) {
            player.evilTreeKc++;
            if (!isInstanced) {
                int amount = getRewardAmount(ThreadLocalRandom.current().nextBoolean() ? 2 : 1, multiplier, helpPercentage);
                possibleRewards.add(new RewardItem(NurtureEvilSaplingAction.EVIL_SEED_ID, amount / 2, amount, 2)); // Evil seed.
            } else if (ThreadLocalRandom.current().nextInt(4) == 0) {
                possibleRewards.add(new RewardItem(NurtureEvilSaplingAction.EVIL_SEED_ID, 1, 2, 4)); // Evil seed.
            }

            int kindlingAmount = getRewardAmount(isInstanced ? 10 : 20, multiplier, helpPercentage);
            possibleRewards.add(new RewardItem(14666, kindlingAmount / 2, kindlingAmount, 2));

            if (PET_ACTIVE) {
                int baseChance = 712;
                if (player.evilTreeKc >= 1000) {
                    baseChance /= 2;
                }
                if (!isInstanced) {
                    baseChance /= 2;
                }
                possibleRewards.add(new RewardItem(Pets.ENTLING.getBabyItemId(), 1, baseChance));
            }

            int rootChance = 0;
            if (Settings.TEST_SERVER_MODE) {
                rootChance = 1;
                if (isInstanced || player.quests.getCurrentStage(RootOfEvil.class) > 1 || player.hasItem(9919)) {
                    rootChance = 0;
                }
            }
            if (rootChance > 0) {
                possibleRewards.add(new RewardItem(9919, 1, rootChance));
            }

            int dustAmt = getRewardAmount(5, multiplier, helpPercentage);
            possibleRewards.add(new RewardItem(3326, dustAmt / 2, dustAmt, 2));

            int herbsAmt = getRewardAmount(7, multiplier, helpPercentage);
            possibleRewards.add(new RewardItem(24783, herbsAmt / 2, herbsAmt, isInstanced ? 2 : 1)); // Evil herbs.

            int barkAmt = getRewardAmount(10, multiplier, helpPercentage);
            possibleRewards.add(new RewardItem(3239, barkAmt / 2, barkAmt, isInstanced ? 2 : 1).setNoted()); // Evil bark.

            if (ThreadLocalRandom.current().nextInt(5) == 0 && !player.getTreasureTrails().hasClueScrollItem()) {
                ClueScrollDistributor.givePlayerClueScroll(player, ThreadLocalRandom.current().nextBoolean() ? 2 : 3);
                player.sendMessage("The gods have blessed you with a clue scroll in your inventory.");
            }


            String message = "The Evil Tree Hunter thanks you for your commitment to killing " + Colors.RED + player.evilTreeKc + "</col> ";
            String suffix = player.evilTreeKc == 1 ? "tree." : "trees.";
            player.sendMessage(message + suffix);
            baseRolls++;
        } else {
            player.sendMessage("The Evil Tree Hunter looks disappointed with your effort.");
        }
        if (helpPercentage >= 60) {
            possibleRewards.add(new RewardItem(36273, 1, 10)); // Quickshafter.
            possibleRewards.add(new RewardItem(7409, 1, 20)); // Magic secateurs.
            possibleRewards.add(new RewardItem(28907, 1, 5)); // Prismatic lamp.
            possibleRewards.add(new RewardItem(28550, 1, 5).setNoted()); // Trisk key.

            int papayaAmount = getRewardAmount(20, multiplier, helpPercentage);
            possibleRewards.add(new RewardItem(5972, papayaAmount / 2, papayaAmount, 2).setNoted()); // Papaya fruit.

            int crushedNestAmount = getRewardAmount(40, multiplier, helpPercentage);
            possibleRewards.add(new RewardItem(6693, crushedNestAmount / 2, crushedNestAmount, 2).setNoted()); // Crushed nests.

            int teakPlankAmount = getRewardAmount(50, multiplier, helpPercentage);
            possibleRewards.add(new RewardItem(8780, teakPlankAmount / 2, teakPlankAmount, 2).setNoted()); // Teak planks.

            int mahoganyPlankAmount = getRewardAmount(75, multiplier, helpPercentage);
            possibleRewards.add(new RewardItem(8782, mahoganyPlankAmount / 2, mahoganyPlankAmount, 2).setNoted()); // Mahog. planks.

            int fellstalkSeedAmount = getRewardAmount(20, multiplier, helpPercentage);
            possibleRewards.add(new RewardItem(21621, fellstalkSeedAmount / 2, fellstalkSeedAmount, 3).setNoted()); // Fellstalk seeds.

            int ckeyAmount = getRewardAmount(2, multiplier, helpPercentage);
            possibleRewards.add(new RewardItem(989, 1, ckeyAmount, isInstanced ? 4 : 3).setNoted()); // Crystal key.

            possibleRewards.add(new RewardItem(28904, 1, isInstanced ? 5 : 3)); // Prismatic lamp.
            baseRolls++;
        }
        if (helpPercentage >= 45) {
            possibleRewards.add(new RewardItem(28906, 1, isInstanced ? 5 : 3)); // Prismatic lamp.

            int plankAmt = getRewardAmount(125, multiplier, helpPercentage);
            possibleRewards.add(new RewardItem(30037, plankAmt / 2, plankAmt, isInstanced ? 5 : 3)); // Protean plank.

            int herbChance = helpPercentage >= 100 ? 2 : 8;
            if (ThreadLocalRandom.current().nextInt(herbChance) == 0) {
                int herbAmt = getRewardAmount(5, multiplier, helpPercentage);
                possibleRewards.add(new RewardItem(37953, herbAmt / 2, herbAmt, isInstanced ? 2 : 1).setNoted()); // Herbs.
            } else {
                int herbAmt = getRewardAmount(60, multiplier, helpPercentage);
                possibleRewards.add(new RewardItem(Utils.randomFrom(HerbloreBox.RARES).getId(), herbAmt / 2, herbAmt, 2).setNoted()); // Herbs.
            }

            possibleRewards.add(new RewardItem(28905, 1, 5)); // Prismatic lamp.

            int logsAmt = getRewardAmount(200, multiplier, helpPercentage);
            possibleRewards.add(new RewardItem(34528, logsAmt / 3, logsAmt, isInstanced ? 5 : 3)); // Protean logs.

            baseRolls++;
        }

        if (!isInstanced) {
            possibleRewards.add(new RewardItem(40986, 1, 2, 2)); // Evil tree teleport.
        }

        int ticketsAmount = getRewardAmount(30, multiplier, helpPercentage);
        possibleRewards.add(new RewardItem(39922, ticketsAmount / 2, ticketsAmount, 1)); // Skilling tickets.

        int minAmt = getRewardAmount(type.minLogAmt, multiplier, helpPercentage);
        int maxAmt = getRewardAmount(type.maxLogAmt, multiplier, helpPercentage);
        possibleRewards.add(new RewardItem(type.logId, minAmt, maxAmt, 1).setNoted()); // Tree logs.

        Collections.shuffle(possibleRewards);
        List<Item> rewards = new ArrayList<>();
        for (Iterator<RewardItem> it = possibleRewards.iterator(); it.hasNext(); ) {
            RewardItem rwItem = it.next();
            if (rewards.size() >= 9 && rwItem.id != 9919) {
                return rewards;
            }
            if (rwItem.chance <= 1) {
                it.remove();
                Item item = rwItem.toItem();
                if (item != null) {
                    if (item.getId() == 9919) {
                        if (thisRewards != null)
                            thisRewards.showRewards = false;
                        player.lock();
                        player.getActionManager().forceStop();
                        WorldTasksManager.schedule(new WorldTask() {
                            @Override
                            public void run() {
                                player.unlock();
                                player.getWalkSteps().clear();
                                player.getDialogueManager().startDialogue(new FoundRootD());
                                player.quests.advanceStage(RootOfEvil.class);
                            }
                        }, 2);
                    }
                    rewards.add(item);
                }
            }
        }

        for (RewardItem rwItem : possibleRewards) {
            if (rewards.size() >= 9) {
                return rewards;
            }
            if (baseRolls <= 0) {
                continue;
            }
            Item item = rwItem.toItem();
            if (item != null) {
                if (item.getId() == Pets.ENTLING.getBabyItemId()) {
                    World.sendWorldMessage(Colors.RED + "<shad=000000><img=6>News: " + player.getDisplayName() + " has received the Entling pet from an Evil Tree!", false);
                    QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/drop.png\" height=17> " + player.getDisplayName() + " has received an Entling pet from Evil Trees!"));
                    HcimNewsManager.getInstance().addNews(player, "<#player> received the Entling pet from an Evil Tree!");
                }
                rewards.add(item);
            }
            baseRolls--;
        }
        return rewards;
    }

    /**
     * The max reward amount is 3 times the base amount.
     */
    private static int getRewardAmount(double baseAmount, double multiplier, double helpPercentage) {
        double helpD = helpPercentage / 100.0;
        double totalD = helpD + multiplier;
        if (totalD > 3.0) {
            totalD = 3.0;
        }
        return (int) (baseAmount * totalD);
    }


    public int getHelpPercentage(Player player, int totalFighters) {
        Multiset<String> damageSet = tree.getTreeObject().getDamageSet();
        double playerDamage = damageSet.count(player.getUsername());
        if (playerDamage == 0) {
            return 0;
        }
        if (totalFighters == 0) {
            totalFighters = 1;
        }
        double maxExpectedDamage = Math.floor(tree.getType().hitpoints / totalFighters);
        double res = (playerDamage / maxExpectedDamage) * 100.0;
        if (totalFighters == 1 && res >= 95) {
            return 200;
        } else if (totalFighters == 1 && res >= 75) {
            return 150;
        } else if (totalFighters == 1 && res >= 50) {
            return 100;
        } else if (totalFighters == 1 && res >= 25) {
            return 50;
        }
        return (int) res;
    }

    public int getHelpPercentage(Player player) {
        return getHelpPercentage(player, tree.getTreeObject().getTotalFighters());
    }

    private Dialogue sendCannotClaim() {
        return new Dialogue() {
            @Override
            public void start() {
                sendNPCDialogue(13790, ANGRY,
                        "If you want to claim rewards, maybe you should help kill it next time!");
            }

            @Override
            public void run(int interfaceId, int componentId) {
                end();
            }

            @Override
            public void finish() {

            }
        };
    }

    public Set<String> getClaimedRewards() {
        return claimedRewards;
    }
}