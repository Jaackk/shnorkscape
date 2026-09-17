package com.rs.game.player.content.skillingcontracts;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import com.rs.Settings;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.activites.gim.GIM;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.VisWaxManager;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.content.skillingcontracts.impl.AgilityContractList;
import com.rs.game.player.content.skillingcontracts.impl.ConstructionContractList;
import com.rs.game.player.content.skillingcontracts.impl.CookingContractList;
import com.rs.game.player.content.skillingcontracts.impl.CraftingContractList;
import com.rs.game.player.content.skillingcontracts.impl.DivinationContractList;
import com.rs.game.player.content.skillingcontracts.impl.FarmingContractList;
import com.rs.game.player.content.skillingcontracts.impl.FiremakingContractList;
import com.rs.game.player.content.skillingcontracts.impl.FishingContractList;
import com.rs.game.player.content.skillingcontracts.impl.FletchingContractList;
import com.rs.game.player.content.skillingcontracts.impl.HerbloreContractList;
import com.rs.game.player.content.skillingcontracts.impl.HunterContractList;
import com.rs.game.player.content.skillingcontracts.impl.MiningContractList;
import com.rs.game.player.content.skillingcontracts.impl.RunecraftingContractList;
import com.rs.game.player.content.skillingcontracts.impl.SmithingContractList;
import com.rs.game.player.content.skillingcontracts.impl.ThievingContractList;
import com.rs.game.player.content.skillingcontracts.impl.WoodcuttingContractList;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.SerializableFilesManager;
import com.rs.utils.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import lombok.var;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class SkillingContractManager implements Serializable {

    private enum ItemChance {
        ALWAYS(1),
        VERY_COMMON(2),
        COMMON(3),
        UNCOMMON(5),
        RARE(7);

        private final int chanceValue;

        ItemChance(int chanceValue) {
            this.chanceValue = chanceValue;
        }

        public boolean rollTrue() {
            if (this == ALWAYS) {
                return true;
            }
            return ThreadLocalRandom.current().nextInt(chanceValue) == 0;
        }
    }

    @RequiredArgsConstructor
    private static class ContractReward {
        @Getter
        private final int id;
        @Getter
        private final int minAmount;
        @Getter
        private final int maxAmount;
        @Getter
        private final ItemChance chance;

        public boolean canDrop(Player player) {
            return true;
        }

        public Item toItem() {
            ItemDefinitions def = ItemDefinitions.getItemDefinitions(id);
            int newId = id;
            if (!def.isStackable() && !def.isNoted() && def.getCertId() != -1) {
                newId = def.getCertId();
            }
            int amount = ThreadLocalRandom.current().nextInt(minAmount, maxAmount) + 1;
            return new Item(newId, amount);
        }
    }


    public static transient final Map<Integer, ContractList> ALL;
    public static transient final int BASE_SKIP_COST = 4;
    public static transient final int BLOCK_COST = 25;
    public static transient final int SKILLS_FOR_ADVANCED = 10;
    public static transient final int BONUS_XP_MULTIPLIER = 200;
    public static transient int currentTotalForMultiplier;
    public static transient final int TOTAL_FOR_MULTIPLIER;
    public static transient final int TICKETS_FOR_BACKPACK = 250;
    public static transient final int TICKETS_FOR_SHOP = 150;
    private static final long serialVersionUID = 7498990325084392149L;
    public static double multiplier = 1.0;
    public static int totalCompleted;

    static {
        TOTAL_FOR_MULTIPLIER = 50;
        currentTotalForMultiplier = TOTAL_FOR_MULTIPLIER;
    }

    public static void printAll() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (ContractReward reward : rewards) {
            sb.append("Name: ").append(reward.toItem().getName()).append(" | ")
                    .append("Min/max amount: ").append(reward.minAmount).append(" to ").append(reward.maxAmount).append(" | ")
                    .append("Rarity: ").append(reward.chance.name()).append('\n');
        }
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("contract_rewards.txt"))) {
            writer.write(sb.toString());
        }
    }

    public static final ImmutableList<ContractReward> rewards = ImmutableList.of(
            new ContractReward(24778, 1, 3, ItemChance.RARE), // Evil tree seed.
            new ContractReward(34727, 25, 50, ItemChance.VERY_COMMON) { // Raw great white shark.
                @Override
                public boolean canDrop(Player player) {
                    return player.isDiamondDonor();
                }
            },
            new ContractReward(21621, 25, 50, ItemChance.COMMON), // Fellstalk seed.
            new ContractReward(7937, 750, 1250, ItemChance.ALWAYS), // Pure essence.
            new ContractReward(384, 40, 80, ItemChance.VERY_COMMON), // Raw sharks.
            new ContractReward(15271, 20, 40, ItemChance.VERY_COMMON), // Raw rocktail.
            new ContractReward(5973, 40, 60, ItemChance.COMMON), // Papaya fruit.
            new ContractReward(452, 30, 60, ItemChance.COMMON), // Rune ore.
            new ContractReward(450, 60, 120, ItemChance.ALWAYS), // Adamant ore.
            new ContractReward(448, 120, 240, ItemChance.ALWAYS), // Mithril ore.
            new ContractReward(8781, 150, 250, ItemChance.COMMON), // Teak planks.
            new ContractReward(8783, 200, 350, ItemChance.VERY_COMMON), // Mahogany planks.
            new ContractReward(8789, 1, 5, ItemChance.RARE), // Magic stones.
            new ContractReward(28550, 1, 2, ItemChance.UNCOMMON), // Trisk key.
            new ContractReward(6572, 1, 2, ItemChance.RARE), // Uncut onyx.
            new ContractReward(1632, 25, 50, ItemChance.COMMON), // Uncut dragonstone.
            new ContractReward(1618, 50, 75, ItemChance.COMMON), // Uncut diamond.
            new ContractReward(1620, 75, 100, ItemChance.VERY_COMMON), // Uncut ruby.
            new ContractReward(1622, 100, 125, ItemChance.ALWAYS), // Uncut emerald.
            new ContractReward(1624, 125, 150, ItemChance.ALWAYS), // Uncut sapphire.
            new ContractReward(29557, 50, 75, ItemChance.VERY_COMMON), // Elder logs.
            new ContractReward(1514, 75, 100, ItemChance.ALWAYS), // Magic logs.
            new ContractReward(1516, 100, 125, ItemChance.ALWAYS), // Yew logs.
            new ContractReward(35011, 20, 40, ItemChance.UNCOMMON), // Rune dragon bones.
            new ContractReward(35009, 40, 60, ItemChance.COMMON), // Adamant dragon bones.
            new ContractReward(537, 50, 100, ItemChance.VERY_COMMON), // Dragon bones.
            new ContractReward(102, 75, 125, ItemChance.COMMON), // Unf irit potion.
            new ContractReward(14857, 75, 125, ItemChance.COMMON), // Unf wergali potion.
            new ContractReward(104, 50, 75, ItemChance.COMMON), // Unf avantoe potion.
            new ContractReward(106, 50, 75, ItemChance.COMMON), // Unf kwuarm potion.
            new ContractReward(3005, 50, 75, ItemChance.COMMON), // Unf snapdragon potion.
            new ContractReward(108, 50, 75, ItemChance.COMMON), // Unf cadantine potion.
            new ContractReward(2484, 50, 75, ItemChance.COMMON), // Unf lantadyme potion.
            new ContractReward(110, 50, 75, ItemChance.COMMON), // Unf dwarf weed potion.
            new ContractReward(112, 50, 75, ItemChance.COMMON), // Unf torstol potion.
            new ContractReward(102, 50, 75, ItemChance.COMMON), // Unf fellstalk potion.
            new ContractReward(6694, 25, 50, ItemChance.ALWAYS), // Crushed nests.
            new ContractReward(2510, 25, 50, ItemChance.COMMON), // Black d'leather.
            new ContractReward(2508, 50, 75, ItemChance.COMMON), // Red d'leather.
            new ContractReward(2506, 75, 100, ItemChance.COMMON), // Blue d'leather.
            new ContractReward(1746, 100, 125, ItemChance.COMMON), // Green d'leather.
            new ContractReward(2364, 20, 30, ItemChance.COMMON), // Rune bar.
            new ContractReward(28257, 10, 15, ItemChance.COMMON), // Saradomin wine.
            new ContractReward(24375, 20, 40, ItemChance.COMMON), // Royal dragon leather
            new ContractReward(29324, 80, 120, ItemChance.COMMON), // Incandescent energy.
            new ContractReward(31598, 10, 20, ItemChance.UNCOMMON), // Crimson skillchompa.
            new ContractReward(31597, 10, 20, ItemChance.UNCOMMON), // Azure skillchompa.
            new ContractReward(31596, 10, 20, ItemChance.UNCOMMON), // Viridian skillchompa.
            new ContractReward(31595, 10, 20, ItemChance.UNCOMMON), // Cobalt skillchompa.
            new ContractReward(31598, 10, 20, ItemChance.COMMON), // Crimson skillchompa.
            new ContractReward(31597, 10, 20, ItemChance.COMMON), // Azure skillchompa.
            new ContractReward(31596, 10, 20, ItemChance.COMMON), // Viridian skillchompa.
            new ContractReward(31595, 10, 20, ItemChance.COMMON), // Cobalt skillchompa.
            new ContractReward(12160, 30, 50, ItemChance.COMMON), // Crimson Charms.
            new ContractReward(12163, 10, 25, ItemChance.COMMON), // Blue Charms.
            new ContractReward(1778, 100, 200, ItemChance.COMMON) // Bowstring.
    );

    public int getSkipCost(SkillingContract contract) {
        int skipCost = (int) Math.floor((contract.levelRequired / 10.0) * BASE_SKIP_COST);
        if (skipCost < 10)
            skipCost = 10;
        if (hasCoOpContract())
            skipCost *= 2;
        return skipCost;
    }

    static {
        try {
            List<ContractList> contractLists = Arrays.asList(
                    new AgilityContractList(),
                    new ConstructionContractList(),
                    new CookingContractList(),
                    new CraftingContractList(),
                    new FishingContractList(),
                    new FarmingContractList(),
                    new FiremakingContractList(),
                    new FletchingContractList(),
                    new HerbloreContractList(),
                    new MiningContractList(),
                    new RunecraftingContractList(),
                    new SmithingContractList(),
                    new WoodcuttingContractList(),
                    new ThievingContractList(),
                    new DivinationContractList(),
                    new HunterContractList()
            );
            Map<Integer, ContractList> contractListMap = new HashMap<>(contractLists.size());
            for (ContractList list : contractLists) {
                contractListMap.put(list.skillId, list);
            }
            ALL = ImmutableMap.copyOf(contractListMap);
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public int getTotalTickets() {
        int inventoryAmount = player.getInventory().getAmountOf(39922);
        int bankAmount = player.getBank().getNumberOf(39922);
        return inventoryAmount + bankAmount;
    }

    public boolean removeTickets(int amount) {
        int inventoryAmount = player.getInventory().getAmountOf(39922);
        if (inventoryAmount >= amount) {
            player.getInventory().deleteItem(new Item(39922, amount));
            return true;
        }
        int bankAmount = player.getBank().getNumberOf(39922);
        if (bankAmount >= amount) {
            player.getBank().removeItem(new Item(39922, amount));
            return true;
        }
        if (inventoryAmount + bankAmount >= amount) {
            player.getInventory().deleteItem(new Item(39922, inventoryAmount));
            player.getBank().removeItem(new Item(39922, amount - inventoryAmount));
            return true;
        }
        return false;
    }

    private static final class SpawnMaster {
        private final int x;
        private final int y;
        private final int z;
        private final int rotateX;
        private final int rotateY;
        private final boolean newMaster;

        private SpawnMaster(int x, int y, int z, int rotateX, int rotateY, boolean newMaster) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.rotateX = rotateX;
            this.rotateY = rotateY;
            this.newMaster = newMaster;
        }
    }

    private static final ImmutableList<SpawnMaster> MASTER_POSITIONS = ImmutableList.of(
            new SpawnMaster(5020, 738, 1, 1, 0, false),
            new SpawnMaster(5020, 737, 1, 1, 0, true),
            new SpawnMaster(4381, 5926, 0, 0, 1, false),
            new SpawnMaster(4382, 5926, 0, -1, 1, true),
            new SpawnMaster(3888, 6812, 0, 1, 0, false),
            new SpawnMaster(3889, 6809, 0, 0, 1, true),
            new SpawnMaster(3555, 6052, 1, 0, -1, false),
            new SpawnMaster(3554, 6052, 1, 0, -1, true)
    );

    public static void loadNpc() {
        for (SpawnMaster spawn : MASTER_POSITIONS) {
            NPC npc = new NPC(spawn.newMaster ? 219 : 943, new WorldTile(spawn.x, spawn.y, spawn.z), -1, false) {
                @Override
                public void processNPC() {
                    if (ThreadLocalRandom.current().nextInt(16) == 0) {
                        setNextAnimation(new Animation(859));
                    }
                }
            };
            npc.setDirection(Utils.getAngle(spawn.rotateX, spawn.rotateY));
            npc.setRandomWalk(0);
        }
    }

    public static SkillingContract lookup(int skillId, int contractId) {
        ContractList list = ALL.get(skillId);
        if (list == null)
            return null;
        return list.lookup(contractId);
    }

    public transient Player player;
    public int totalContracts;
    public int totalCoOpContracts;

    /**
     * Do not assign any value to this. Here solely for backwards compatibility.
     */
    private int points;
    public AssignedSkillingContract current;
    public BlockLevel currentBlock = BlockLevel.T30;
    public AssignedCoOpSkillingContract partner;

    /**
     * Never call directly.
     */
    private Multiset<TempContractEffect> effects = HashMultiset.create();
    private SetMultimap<Integer, Integer> blocks = HashMultimap.create();


    public SkillingContractManager(Player player) {
        this.player = player;
    }

    public void removeVirtualPoints() {
        if (points > 0 && player.giveItem(new Item(39922, points))) {
            points = -1;
        }
    }

    public void coOpMessage(Object msg) {
        player.sendMessage("<img=7>" + Colors.DCYAN + "Co-op Skilling: " + msg);
    }

    public void coOpMessage(Object msg, Object msg1, Object... otherMsg) {
        StringBuilder sb = new StringBuilder("<img=7>" + Colors.DCYAN + "Co-op Skilling: " + msg + Colors.DCYAN + msg1);
        for (Object o : otherMsg) {
            sb.append(Colors.DCYAN).append(o);
        }
        player.sendMessage(sb.toString());
    }

    public boolean canTalkToAdvancedMaster(boolean sendMessage) {
        if (player.getContracts().totalContracts < 500) {
            if (sendMessage)
                player.sendMessage("You need to complete 500 contracts before interacting with the advanced skilling master.");
            return false;
        }
        int count = SKILLS_FOR_ADVANCED;
        for (int skill = 0; skill < player.getSkills().level.length; skill++) {
            int level = player.getSkills().getLevelForXp(skill);
            if (level >= 80 && !Skills.isCombatSkill(skill)) {
                if(--count <= 0) {
                    return true;
                }
            }
        }
        if (sendMessage)
            player.sendMessage("You need to have at least "+SKILLS_FOR_ADVANCED+" non-combat skills with a level of 80 to interact with the advanced skilling master.");
        return false;
    }

    private void contractComplete() {
        if (current == null)
            throw new IllegalStateException("Player does not have an assigned contract.");
        val contract = current.getContract();
        if (contract == null)
            throw new IllegalStateException("Assigned contract is invalid.");
        boolean reducedRewards = player.lastContract != null && player.lastContract.matches(current);
        double xp = contract.bonusExp;
        int maxTickets = getTicketsAmount(contract.levelRequired);
        int ticketsAmount = maxTickets > 0 ? ThreadLocalRandom.current().nextInt(maxTickets / 2, maxTickets) + 1 : 5;
        int maxViswax = getViswaxAmount(contract.levelRequired);
        int viswax = maxViswax > 0 ? ThreadLocalRandom.current().nextInt(maxViswax / 2, maxViswax) + 1 : 1;
        int rolls = 0;
        if (contract.levelRequired > 70 && ThreadLocalRandom.current().nextBoolean()) {
            rolls++;
        }
        if (contract.advanced && ThreadLocalRandom.current().nextBoolean()) {
            rolls++;
        }
        double amountMultiplier = 1.0;
        if (getEffects().contains(TempContractEffect.FIFTEEN_PERCENT_MORE_XP)) {
            xp *= 1.15;
        }

        boolean throttleReward = true;
        boolean streakedReward = false;
        totalContracts++;
        if (totalContracts % 1000 == 0) {
            player.sendMessage(Colors.RED + "Skilling Contracts: You have completed 1000 contracts in a row!");
            World.sendWorldYellMessage(Colors.RED + "Skilling Contracts: " + player.getDisplayName() + " has completed 1000 skilling contracts in a row!", player);
            player.sendMessage("You receive a " + Colors.RED + "Skilling backpack (x1)</col>.");
            HcimNewsManager.getInstance().addNews(player,"<#player> completed 1000 contracts in a row!");
                    player.addItem(new Item(37694));
            viswax *= 8;
            ticketsAmount *= 16;
            xp *= 6;
            amountMultiplier = 3.5;
            rolls += 7;
            rolls = getRolls(rolls);
            throttleReward = false;
        } else if (totalContracts % 100 == 0) {
            player.sendMessage(Colors.RED + "Skilling Contracts: You have completed 100 skilling contracts in a row!");
            if (ThreadLocalRandom.current().nextBoolean()) {
                player.sendMessage("You receive a " + Colors.RED + "Skilling backpack (x1)</col>.");
                player.addItem(new Item(37694));
            }
            viswax *= 4;
            ticketsAmount *= 8;
            xp *= 4;
            amountMultiplier = 3.0;
            rolls += 6;
            rolls = getRolls(rolls);
            throttleReward = false;
        } else if (totalContracts % 50 == 0) {
            player.sendMessage(Colors.RED + "Skilling Contracts: You have completed 50 contracts in a row!");
            viswax *= 3;
            ticketsAmount *= 6;
            xp *= 2;
            amountMultiplier = 2.5;
            rolls += 3;
            rolls = getRolls(rolls);
            throttleReward = false;
        } else if (totalContracts % 25 == 0) {
            player.sendMessage(Colors.RED + "Skilling Contracts: You have completed 25 contracts in a row!");
            viswax *= 2;
            ticketsAmount *= 4;
            xp *= 1.50;
            amountMultiplier = 2.0;
            rolls += 4;
            rolls = getRolls(rolls);
            throttleReward = false;
        } else if (totalContracts % 5 == 0) {
            player.sendMessage(Colors.RED + "Skilling Contracts: You have completed 5 contracts in a row!");
            viswax++;
            ticketsAmount *= 2;
            xp *= 1.25;
            amountMultiplier = 1.5;
            rolls += 2;
            throttleReward = false;
            streakedReward = true;
        }
        rolls = getRolls(rolls);
        if (hasCoOpContract()) {
            if (ticketsAmount < 30) {
                ticketsAmount = 30;
            }
            if (viswax < 2) {
                viswax = 2;
            }
            if (rolls < 1) {
                rolls = 1;
            }

            throttleReward = false;
            coOpMessage("You have completed your assigned contract! Total co-op contracts completed ~ ", Colors.RED + ++totalCoOpContracts, ".");

            if (partner.advanced) {
                if (partner.isShort) {
                    xp *= 2;
                    rolls++;
                    viswax++;
                    ticketsAmount *= 2.5;
                } else {
                    xp *= 4;
                    rolls += 3;
                    viswax += 3;
                    ticketsAmount *= 4;
                }
            } else {
                if (partner.isShort) {
                    xp *= 1.5;
                    viswax++;
                    ticketsAmount *= 1.5;
                } else {
                    xp *= 3.5;
                    rolls++;
                    viswax += 2;
                    ticketsAmount *= 3;
                }
            }
        }
        if (ThreadLocalRandom.current().nextInt(20) == 0) {
            rolls++;
        }
        if (player.oldTomeActivated) {
            rolls++;
            amountMultiplier += 0.25;
            viswax++;
            ticketsAmount *= 1.50;
            player.oldTomeActivated = false;
        }
        if (player.getPerkManager().hasPerkActive(DonationPerk.SKILLING_ADDICT)) {
            xp *= 1.25;
            ticketsAmount *= 1.25;
            viswax *= 1.25;
        }
        xp *= multiplier;
        ticketsAmount *= multiplier;
        viswax *= multiplier;
        if (ticketsAmount > 50 && throttleReward) {
            ticketsAmount = 50;
        }
        if (viswax > 10 && throttleReward) {
            viswax = 10;
        }
        if (ticketsAmount > 500) {
            ticketsAmount = 500;
        }
        if (viswax > 50) {
            viswax = 50;
        }
        if (streakedReward) {
            if (xp > 50_000) {
                xp = 50_000;
            }
        } else {
            if (xp > 25_000) {
                xp = 25_000;
            }
        }
        if (reducedRewards && !streakedReward) {
            xp /= 2;
            ticketsAmount /= 2;
            viswax /= 2;
            rolls /= 2;
            player.sendMessage(Colors.RED + "Your rewards have been reduced for doing the same contract twice in a row.");
        }
        if (player.skippedLastContract) {
            xp *= 0.90;
            ticketsAmount *= 0.90;
            viswax *= 0.90;
            rolls *= 0.90;
            player.skippedLastContract = false;
        }
        if (ticketsAmount > 0) {
            player.addItem(new Item(39922, ticketsAmount));
        }
        if (viswax > 0) {
            player.addItem(new Item(VisWaxManager.VISWAX_ITEM_ID, viswax));
        }

        if (rolls > 0) {
            if (rolls > 5 && !player.getPerkManager().hasPerkActive(DonationPerk.SKILLING_ADDICT)) {
                rolls = 7;
            } else if (rolls > 7 && player.getPerkManager().hasPerkActive(DonationPerk.SKILLING_ADDICT)) {
                rolls = 9;
            }
            for (int i = 0; i < rolls; i++) {
                val reward = Utils.randomFrom(rewards);
                if (reward.canDrop(player) && reward.getChance().chanceValue > 3 && streakedReward || reward.chance.rollTrue()) {
                    val item = reward.toItem();
                    int amount = (int) (item.getAmount() * amountMultiplier);
                    player.addItem(item.getId(), amount);
                    String message = "You receive " + Colors.RED + item.getName() + "(x" + amount + ")</col>";
                    if (hasCoOpContract()) {
                        coOpMessage(message, ".");
                    } else {
                        player.sendMessage(message + ".");
                    }
                }
            }
        }
        for (TempContractEffect e : TempContractEffect.ALL) {
            int left = getEffects().remove(e, 1);
            if (left == 1) {
                player.sendMessage("Your contract effect [" + Colors.RED + e.description + "</col>] has worn off!");
            } else if (left == 2) {
                player.sendMessage("Your contract effect [" + Colors.RED + e.description + "</col>] is about to wear off!");
            }
        }
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                player.getWalkSteps().clear();
                player.getActionManager().forceStop();
                player.getDialogueManager().startDialogue("CompletedTaskD");
            }
        }, 1);
        SkillingContractTracker.getSingleton().track(player);
        totalCompleted++;
        if (totalCompleted % currentTotalForMultiplier == 0) {
            player.sendMessage(Colors.RED + "Congratulations! You have completed the " + totalCompleted + "th skilling contract on Ataraxia today! You have received " + Colors.RED + "25M coins</col>.");
            player.addItem(new Item(995, 25_000_000));
            if (multiplier < 2.25) {
                multiplier += .25;
                if(multiplier == 2.25) {
                    World.sendWorldMessage(Colors.RED + "Skilling Contracts:</col> A total of " + Colors.RED + totalCompleted + "</col> skilling contracts have been completed by Ataraxians today. Contract XP and rewards multiplier increased to it's maximum amount, " + ((int) (multiplier * 100)) + "%!", false);
                } else {
                    World.sendWorldMessage(Colors.RED + "Skilling Contracts:</col> A total of " + Colors.RED + totalCompleted + "</col> skilling contracts have been completed by Ataraxians today. Contract XP and rewards multiplier increased to " + ((int) (multiplier * 100)) + "%!", false);
                }
                if (multiplier == 2.0) {
                    currentTotalForMultiplier *= 2;
                }
            }
        }
        if (xp > 0) {
            player.getSkills().addXp(current.skillId, xp);
        }
        player.getAchievements().updateProgress(1, AchievementList.COMPLETE_5_DAILY_TASKS, AchievementList.COMPLETE_20_DAILY_TASKS, AchievementList.COMPLETE_50_DAILY_TASKS, AchievementList.COMPLETE_100_DAILY_TASKS);
        player.sendMessage("Skilling contracts completed: " + Colors.RED + +totalContracts + "</col>; Tickets gained: " + Colors.RED + ticketsAmount + "</col>; Viswax gained: " + Colors.RED + viswax + "</col>");
        player.lastContract = new LastSkillingContract(current.skillId, current.contractId);
    }


    public boolean isPartnerOnline() {
        return partner != null && World.getPlayer(partner.partnerName) != null;
    }

    public boolean hasCoOpContract() {
        return partner != null && current != null;
    }

    public boolean hasPartner() {
        return partner != null;
    }

    public boolean hasContract() {
        return current != null;
    }

    private int getRolls(int rolls) {
        if (multiplier >= 2.5) {
            rolls += 4;
        } else if (multiplier >= 2.0) {
            rolls += 3;
        } else if (multiplier >= 1.5) {
            rolls += 2;
        }
        return rolls;
    }

    private int getChimesAmount(int taskLevel) {
        // Don't give chimes for anything below level 30.
        taskLevel -= 29;
        if (taskLevel < 0) {
            taskLevel = 0;
        }
        return (int) (taskLevel * 1.8);
    }

    private int getViswaxAmount(int taskLevel) {
        if (taskLevel > 70) {
            return 4;
        } else if (taskLevel > 50) {
            return 3;
        } else if (taskLevel > 30) {
            return 2;
        }
        return 1;
    }

    private int getTicketsAmount(int taskLevel) {
        if (taskLevel > 70) {
            return 24;
        } else if (taskLevel > 50) {
            return 18;
        } else if (taskLevel > 30) {
            return 12;
        }
        return 6;
    }

    public void checkedAssignContract(boolean highLevel, int npc) {
        player.getDialogueManager().startDialogue("AssignContractD", highLevel, npc);
    }

    public void assignCoOpContract() {
        CoOpRequest request = player.coOpRequest;
        Player other = request.requesting;

        // Close any potential dialogues.
        player.getDialogueManager().finishDialogue();
        other.getDialogueManager().finishDialogue();
        player.coOpRequest = null;
        other.coOpRequest = null;

        // Try to generate a contract with strict filtering.
        boolean countBlocks = true;
        int totalRate = 0;
        List<ContractList> contractLists = new ArrayList<>();
        for (ContractList list : ALL.values()) {
            if (list.canGenerateCoOp(player, other, request.advanced, countBlocks)) {
                totalRate += list.getCoOpRate(player, other);
                contractLists.add(list);
            }
        }

        // Okay, we found nothing. Try again with less strict filtering.
        if (contractLists.size() == 0) {
            countBlocks = false;
            totalRate = 0;
            for (ContractList list : ALL.values()) {
                if (list.canGenerateCoOp(player, other, request.advanced, countBlocks)) {
                    totalRate += list.getRate(player);
                    contractLists.add(list);
                }
            }

            // And there's nothing again. A contract cannot be assigned.
            if (contractLists.size() == 0) {
                String msg = "There are no contracts that match both of your skilling levels. Try again with different settings.";
                coOpMessage(msg);
                other.getContracts().coOpMessage(msg);
                return;
            }
        }

        // Potential contracts were found. Shuffle them and randomly pick one.
        Collections.shuffle(contractLists);
        int roll = ThreadLocalRandom.current().nextInt(totalRate) + 1;
        int mod = 0;
        SkillingContract contract;
        for (ContractList list : contractLists) {
            mod += list.getCoOpRate(player, other);
            if (roll <= mod) {
                contract = list.generateCoOp(player, other, request.advanced, countBlocks);
                if (contract == null) {
                    throw new IllegalStateException("Invalid contract generated (roll: " + roll + ", mod: " + mod + ", contract lists: " + contractLists.size() + ").");
                }
                int actions = contract.generateActionsCoOp(player, other, request, list.skillId);
                current = new AssignedSkillingContract(list.skillId, contract, actions);
                other.getContracts().current = new AssignedSkillingContract(list.skillId, contract, actions);
                partner = new AssignedCoOpSkillingContract(other.getUsername(), request.advanced, request.isShort);
                other.getContracts().partner = new AssignedCoOpSkillingContract(player.getUsername(), request.advanced, request.isShort);
                break;
            }
        }

        // One was picked successfully, notify participants.
        String msg = "Your contract is ~ ";
        coOpMessage(msg + getContractDescription());
        other.getContracts().coOpMessage(msg + other.getContracts().getContractDescription());
    }

    public boolean assignContract(boolean highLevel) {
        List<ContractList> contractLists = new ArrayList<>(ALL.values());
        int totalRate = 0;
        Iterator<ContractList> it = contractLists.iterator();
        while (it.hasNext()) {
            ContractList list = it.next();
            if (list.canGenerate(player, highLevel)) {
                totalRate += list.getRate(player);
            } else {
                it.remove();
            }
        }
        if (contractLists.size() == 0) {
            return false;
        }
        Collections.shuffle(contractLists);
        int roll = ThreadLocalRandom.current().nextInt(totalRate) + 1;
        int mod = 0;
        for (ContractList list : contractLists) {
            mod += list.getRate(player);
            if (roll <= mod) {
                SkillingContract contract = list.generate(player, highLevel);
                current = new AssignedSkillingContract(player, list.skillId, contract);
                return true;
            }
        }
        throw new IllegalStateException("Invalid roll: " + roll);
    }

    public void recordAction(int skillId, int contractId, int amount) {
        if (current != null &&
                current.skillId == skillId &&
                current.contractId == contractId) {
            if (hasCoOpContract()) {
                Player other = World.getPlayer(partner.partnerName);
                if (other == null) {
                    coOpMessage("Your co-op skilling partner is not online. This action has not been counted towards your contract completion.");
                    return;
                }
                SkillingContractManager otherContracts = other.getContracts();
                AssignedSkillingContract otherCurrent = otherContracts.current;
                if (otherCurrent == null || !otherCurrent.same(current)) {
                    resetCoOpContract(false);
                    coOpMessage("Your co-op skilling partner no longer has the same contract as you. Your contract has been reset.");
                    return;
                }
                if (current.currentActions != otherCurrent.currentActions) {
                    int actions = Math.min(current.currentActions, otherCurrent.currentActions);
                    current.currentActions = actions;
                    otherCurrent.currentActions = actions;
                }
                int oldPercentage = (int) (((double) current.currentActions / current.actionsNeeded) * 100);
                current.currentActions += amount;
                otherCurrent.currentActions += amount;
                if (Settings.TEST_SERVER_MODE || current.currentActions >= current.actionsNeeded) {
                    if (Settings.TEST_SERVER_MODE) {
                        coOpMessage("Skilling contract skipped, beta server.");
                        otherContracts.coOpMessage("Skilling contract skipped, beta server.");
                    }
                    contractComplete();
                    otherContracts.contractComplete();
                    resetCoOpContract(false);
                } else {
                    int newPercentage = (int) (((double) current.currentActions / current.actionsNeeded) * 100);
                    if (oldPercentage < 25 && newPercentage >= 25 ||
                            oldPercentage < 50 && newPercentage >= 50 ||
                            oldPercentage < 75 && newPercentage >= 75 ||
                            oldPercentage < 90 && newPercentage >= 90) {
                        String message = "Contract progress ~ " + newPercentage + "%";
                        coOpMessage(message);
                        otherContracts.coOpMessage(message);
                    }
                }
            } else {
                int oldPercentage = (int) (((double) current.currentActions / current.actionsNeeded) * 100);
                current.currentActions += amount;
                if (Settings.TEST_SERVER_MODE || current.currentActions >= current.actionsNeeded) {
                    if (Settings.TEST_SERVER_MODE) {
                        player.sendMessage(Colors.GREEN + "Skilling contract skipped, beta server.");
                    }
                    contractComplete();
                    resetContract();
                } else {
                    int newPercentage = (int) (((double) current.currentActions / current.actionsNeeded) * 100);
                    if (oldPercentage < 25 && newPercentage >= 25 ||
                            oldPercentage < 50 && newPercentage >= 50 ||
                            oldPercentage < 75 && newPercentage >= 75 ||
                            oldPercentage < 90 && newPercentage >= 90) {
                        player.sendMessage(Colors.RED + "Skilling contract progress:</col> " + newPercentage + "%");
                    }
                }
            }
        }
    }

    public void recordAction(int skillId, int contractId) {
        recordAction(skillId, contractId, 1);
    }

    public Multiset<TempContractEffect> getEffects() {
        if (effects == null)
            effects = HashMultiset.create();
        return effects;
    }

    public SetMultimap<Integer, Integer> getBlocks() {
        if (blocks == null)
            blocks = HashMultimap.create();
        return blocks;
    }

    public void resetContract() {
        if (hasCoOpContract()) {
            resetCoOpContract(true);
        } else {
            current = null;
        }
    }

    public void resetCoOpContract(boolean notify) {
        if (hasCoOpContract()) {
            String username = player.getUsername();
            String otherUsername = partner.partnerName;
            Player other = World.getPlayer(otherUsername);
            current = null;
            partner = null;
            if (notify)
                coOpMessage("Your contract has been reset.");
            if (other == null) {
                CoresManager.getServiceProvider().executeNow(() -> {
                    Player plr = SerializableFilesManager.loadPlayer(otherUsername);
                    if (plr == null || !plr.getContracts().partner.partnerName.equals(username)) {
                        return;
                    }
                    // change to cancelled co-op contract = true, then just cancel on login and notify them
                    plr.getContracts().current = null;
                    plr.getContracts().partner = null;
                    SerializableFilesManager.savePlayer(plr);
                });
            } else if (other.getContracts().hasCoOpContract() &&
                    other.getContracts().partner.partnerName.equals(username)) {
                if (notify)
                    other.getContracts().coOpMessage("Your contract has been reset.");
                other.getContracts().current = null;
                other.getContracts().partner = null;
            }
        }
    }

    public AssignedSkillingContract fixInvalidContract() {
        var assigned = player.getContracts().current;
        if (player.getContracts().current == null) {
            throw new IllegalStateException("No contract to fix.");
        }
        var contract = assigned.getContract();
        if (contract == null) {
            int oldActions = assigned.currentActions;
            assignContract(true);
            assigned = player.getContracts().current;

            int newActions = assigned.currentActions;
            if (newActions > oldActions) {
                newActions = oldActions;
            }
            player.sendMessage(Colors.DEF_SEARCH_CYAN + "A new contract has been assigned to you because your old one was invalid. Actions remaining: " + newActions + ".");
        }
        return assigned;
    }

    public String getContractDescription() {
        AssignedSkillingContract assigned = player.getContracts().current;
        if (assigned == null)
            throw new IllegalStateException("Player does not have assigned contract.");
        assigned = fixInvalidContract();
        if (assigned.getContract() == null) {
            return "Invalid contract ID.";
        }
        return assigned.getContract().description.
                replace("<amount>", Integer.toString(assigned.getActionsLeft()));
    }

    public BlockLevel getCurrentBlock() {
        if (currentBlock == null)
            currentBlock = BlockLevel.T30;
        return currentBlock;
    }
}