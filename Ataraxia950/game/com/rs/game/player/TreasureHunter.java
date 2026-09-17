package com.rs.game.player;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.rs.cache.loaders.rs3.RS3ClientScriptMap;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.controllers.DungeonController;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.items.ItemExaminesDataParser;
import com.rs.utils.data.parsers.items.TreasureHunterRewardParser;
import com.rs.utils.data.parsers.items.pojos.TreasureHunterReward;
import com.rs.utils.mysql.QueryExecutor;
import com.rs.utils.mysql.impl.News;

public class TreasureHunter implements Serializable {

    private static final long serialVersionUID = 1346024524947318682L;

    private transient Player player;
    private PossibleReward[] rewards;
    private int heartsOfIce;
    private boolean hasTempHeartsOfIce;
    private boolean[] frozenCategories;
    private transient boolean[] TempFrozenCategories;
    private boolean[] prefrences;// white,yellow,orange,red,purple,xp,prismatic,99,99xp,99prismatic
    private PossibleReward currentReward;
    private boolean[] options;
    private transient boolean skippedFrozen;
    private transient int chestsCount;
    private boolean openTen;
    private int dailyKeys, earnedKeys, boughtKeys;

    public TreasureHunter() {
        frozenCategories = new boolean[34];
        prefrences = new boolean[10];
        options = new boolean[7];
    }

    public void init() {
        if (rewards == null)
            generateRandomRewards();
        refreshPossibleRewards();
        if (frozenCategories == null)
            frozenCategories = new boolean[34];
        if (prefrences == null)
            prefrences = new boolean[10];
        if (options == null)
            options = new boolean[7];
        player.getVarBitManager().sendVarBit(37505, hasTempHeartsOfIce ? 1 : 0);// adds 5 hearts of ice every 5 chests opened

    }

    public void openTreasureHunter() {
        chestsCount = openTen ? Math.min(10, getTotalKeysAmount()) : 1;
        player.getInterfaceManager().sendCentralInterfaceLargeInterface(1253);
//        player.getPackets().sendIComponentSprite(1253, 476, 31911);
        refreshPossibleRewards();
        refreshHeartsOfIce();
        refreshFrozenCategories();
        removeDialogue();
        refreshOptions();
        refreshPrefrences();
        refreshCurrentReward();
        refreshKeys();
        refreshOpenten();
    }

    public void handleButtons(int interfaceId, int componentId, int slotId, int slotId2, int packetId) {
        if (interfaceId == 1607 && componentId == 34) {
            if (player.getControlerManager().getControler() instanceof DungeonController) {
                player.getInterfaceManager().closeTreasureHunterOverlay();
                player.sendMessage("You cannot do this whilst inside Daemonheim.");
                return;
            }
            player.getInterfaceManager().closeMenu();
            openTreasureHunter();
        } else if (interfaceId == 1252) {
            if (componentId == 12) {
                if (player.getControlerManager().getControler() instanceof DungeonController) {
                    player.getInterfaceManager().closeTreasureHunterOverlay();
                    player.sendMessage("You cannot do this whilst inside Daemonheim.");
                    return;
                }
                openTreasureHunter();
            } else if (componentId == 15) {
                player.getInterfaceManager().closeTreasureHunterOverlay();
            }
        } else if (interfaceId == 1253) {
            if (componentId == 850)// claim later
                player.getInterfaceManager().removeCentralInterfaceLargeInterface();
            else if (componentId == 273) // claim later
                player.getInterfaceManager().removeCentralInterfaceLargeInterface();
            else if (componentId >= 395 && componentId <= 433) {
                toggleFrozenCategory(componentId - 394 >= 34 ? 34 : componentId - 394);
            } else if (componentId == 81) {
                openCategoriesMenu();
            } else if ((componentId >= 489 && componentId <= 617) || (componentId >= 623 && componentId <= 643)) {
                toggleTempFrozenCategory((componentId >= 623 && componentId <= 643) ? 34 : (((componentId - 489) / 4) + 1));
            } else if (componentId == 746) {
                Arrays.fill(TempFrozenCategories, false);
                Arrays.fill(prefrences, false);
                refreshPrefrences();
                refreshCategoriesMenu();
                refreshTempFrozenCategories();
            } else if ((componentId >= 677 && componentId <= 694) || componentId == 698 || componentId == 704) {
                togglePrefrences(componentId == 704 ? 6 : componentId == 698 ? 5 : (componentId - 677) / 2);
            } else if (componentId == 711 || componentId == 716 || componentId == 722) {
                togglePrefrences(componentId == 722 ? 9 : componentId == 716 ? 8 : 7);
            } else if (componentId == 730 || componentId == 738) {
                if (componentId == 730) {
                    frozenCategories = Arrays.copyOf(TempFrozenCategories, TempFrozenCategories.length);
                    refreshFrozenCategories();
                }
                player.getPackets().sendHideIComponent(1253, 481, true);
                refreshHeartsOfIce();
            } else if (componentId >= 799 && componentId <= 823) {
                toggleOption((componentId - 799) / 4);
            } else if (componentId == 837) { // close options
                player.getPackets().sendHideIComponent(1253, 793, true);
            } else if (componentId == 46 || componentId == 56 || componentId == 51 || componentId == 41 || componentId == 36) {
                if (getTotalKeysAmount() <= 0) {
                    player.getPackets().sendExecuteScript(5879);
                    return;
                }
                refreshMaxChests();
                chestsCount = openTen ? Math.min(10, getTotalKeysAmount()) : 1;
                openChest(componentId == 46 ? 1 : componentId == 56 ? 2 : componentId == 51 ? 3 : componentId == 41 ? 4 : 5);
            } else if (componentId == 58) {// skipanim
                player.getPackets().sendExecuteScript(6975);
            } else if (componentId == 90 || componentId == 95) {
                toggleOpenten(componentId == 95);
            } else if (componentId == 262 || componentId == 300) {
                claimReward(componentId == 300);
            }
        }
    }

    private void refreshMaxChests() {
        player.getPackets().sendGlobalConfig(2045, Math.min(getTotalKeysAmount(), openTen ? 10 : 0));
    }

    private void claimReward(boolean toBank) {
        if (currentReward != null) {
            if (toBank)
                player.getBank().addItem(new Item(currentReward), true);
            else {
                if (!currentReward.getDefinitions().isStackable() && currentReward.getAmount() > 1 && currentReward.getDefinitions().getCertId() != -1)
                    currentReward.setId(currentReward.getDefinitions().getCertId());
                player.getInventory().addItem(new Item(currentReward));
            }
        } else {
            player.getPackets().sendGameMessage("There was an error claiming your treasure hunter reward, please report to staff.");
        }
        currentReward = null;
        generateRandomRewards();
        if (openTen && chestsCount > 0) {
            useKey();
            selectRandomReward();
            refreshCurrentReward();
        } else {
            chestsCount = this.openTen ? (Math.min(10, getTotalKeysAmount())) : 1;
            refreshCurrentReward();
        }
        refreshOpenten();
    }

    public int getTotalKeysAmount() {
        return dailyKeys + earnedKeys + boughtKeys;
    }

    public void useKey() {
        if (dailyKeys > 0)
            dailyKeys--;
        else if (earnedKeys > 0)
            earnedKeys--;
        else if (boughtKeys > 0)
            boughtKeys--;
        refreshKeys();
    }

    private void refreshKeys() {
        player.getVarBitManager().sendVarBit(4324, dailyKeys);
        player.getPackets().sendGlobalConfig(1800, boughtKeys);
        player.getVarBitManager().sendVarBit(4325, earnedKeys);
        player.getPackets().sendConfigByFile(10862, dailyKeys);
        player.getPackets().sendConfigByFile(11026, earnedKeys);
        player.getPackets().sendIComponentText(1253, 18, ""+getTotalKeysAmount());
    }

    public int getDailyKeys() {
        return dailyKeys;
    }

    public void setDailyKeys(int dailyKeys) {
        this.dailyKeys = dailyKeys;
        refreshKeys();
    }

    public int getEarnedKeys() {
        return earnedKeys;
    }

    public void setEarnedKeys(int earnedKeys) {
        this.earnedKeys = earnedKeys;
        refreshKeys();
    }

    public int getBoughtKeys() {
        return boughtKeys;
    }

    public void setBoughtKeys(int boughtKeys) {
        this.boughtKeys = boughtKeys;
        refreshKeys();
    }

    private void toggleOpenten(boolean openTen) {
        this.openTen = openTen;
        chestsCount = openTen ? Math.min(10, getTotalKeysAmount()) : 1;
        refreshOpenten();
        refreshMaxChests();
    }

    private void refreshOpenten() {
        player.getVarBitManager().sendVarBit(40933, openTen ? 2 : 0);
        player.getPackets().sendIComponentText(1253, 18, ""+this.getTotalKeysAmount());
        player.getPackets().sendExecuteScript(1641 + (openTen ? 1 : 0));
    }

    private void toggleOption(int index) {
        options[index] = !options[index];
        refreshOptions();
    }

    private void refreshOptions() {
        player.getVarBitManager().sendVarBit(22145, options[0] ? 1 : 0);
        player.getVarBitManager().sendVarBit(22146, options[1] ? 1 : 0);
        player.getVarBitManager().sendVarBit(22561, options[2] ? 1 : 0);
        player.getVarBitManager().sendVarBit(29383, options[3] ? 1 : 0);
        player.getVarBitManager().sendVarBit(29384, options[4] ? 1 : 0);
        player.getVarBitManager().sendVarBit(38952, options[5] ? 1 : 0);
        player.getVarBitManager().sendVarBit(38953, options[6] ? 1 : 0);
    }

    private void openCategoriesMenu() {
        TempFrozenCategories = Arrays.copyOf(frozenCategories, frozenCategories.length);
        player.getPackets().sendHideIComponent(1253, 481, false);
        removeDialogue();
        refreshTempFrozenCategories();
        refreshCategoriesMenu();
        refreshPrefrences();
    }

    public void openChest(int chestIndex) {
        if (currentReward == null)
            selectRandomReward();
        if (chestIndex != -1)
            useKey();
        player.getPackets().sendExecuteScript(1646);
        boolean stackable = currentReward.getDefinitions().isStackable();
        boolean cantClaimToInv = (stackable && !player.getInventory().containsItem(currentReward.getId(), 1) && player.getInventory().getFreeSlots() == 0) || (!stackable && player.getInventory().getFreeSlots() == 0);
        if (!currentReward.getDefinitions().isStackable() && currentReward.getAmount() > 1 && currentReward.getDefinitions().getCertId() == -1 && !cantClaimToInv)
            cantClaimToInv = player.getInventory().getFreeSlots() < currentReward.getAmount();
        if (!currentReward.getDefinitions().isStackable() && currentReward.getAmount() > 1 && currentReward.getDefinitions().getCertId() != -1 && cantClaimToInv)
            cantClaimToInv = !player.getInventory().containsItem(currentReward.getDefinitions().getCertId(), 1) && player.getInventory().getFreeSlots() == 0;
        player.getPackets().sendExecuteScript(9122, chestIndex == -1 ? 1 : chestIndex, currentReward.getId(), currentReward.getAmount(), 1, ItemExaminesDataParser.getExamine(currentReward), currentReward.getData().getRarity() + 1, getCategoryId(currentReward.getData().getCategory()) + 1, 2, 0, 4, cantClaimToInv ? 2 : 1, 3, 1, chestsCount - 1, chestIndex == -1 ? 1 : 0, 0/* reroll */, 0, 0, skippedFrozen ? 1 : 0);
        skippedFrozen = false;
        chestsCount--;
        refreshOpenten();
    }

    public void refreshCurrentReward() {
        if (currentReward == null) {
            player.getPackets().sendExecuteScript(9122, 0, -1, 0, 1, "", 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            player.getPackets().sendExecuteScript(6973);
            player.getPackets().sendExecuteScript(4766);
        } else {
            player.getPackets().sendExecuteScript(6975);
            openChest(-1);
        }
        player.getPackets().sendExecuteScript(5879);
    }

    public static final int COMMON = 0, FAIRLY_COMMON = 1, UNCOMMON = 2, RARE = 3, VERY_RARE = 4;
    public static final double[] CHANCES = { 30.00, 15, 7.5, 0.5 };// FAIRLY_COMMON,UNCOMMON,RARE,VERY_RARE

    public void selectRandomReward() {
        if (rewards == null)
            return;
        int[] frozenCategories = getCurrentFrozenCategories();
        List<PossibleReward> possibleRewards = new ArrayList<PossibleReward>();
        while (currentReward == null) {
            double r = Math.random() * 100;
            if (r <= CHANCES[3] && hasRewardByRarity(VERY_RARE)) {
                for (int i = 0; i < rewards.length; i++) {
                    if (rewards[i].getData().getRarity() == VERY_RARE)
                        possibleRewards.add(rewards[i]);
                }
            } else if (r <= CHANCES[2] && hasRewardByRarity(RARE)) {
                for (int i = 0; i < rewards.length; i++) {
                    if (rewards[i].getData().getRarity() == RARE)
                        possibleRewards.add(rewards[i]);
                }
            } else if (r <= CHANCES[1] && hasRewardByRarity(UNCOMMON)) {
                for (int i = 0; i < rewards.length; i++) {
                    if (rewards[i].getData().getRarity() == UNCOMMON)
                        possibleRewards.add(rewards[i]);
                }
            } else if (r <= CHANCES[0] && hasRewardByRarity(FAIRLY_COMMON)) {
                for (int i = 0; i < rewards.length; i++) {
                    if (rewards[i].getData().getRarity() == FAIRLY_COMMON)
                        possibleRewards.add(rewards[i]);
                }
            } else {
                for (int i = 0; i < rewards.length; i++) {
                    if (rewards[i].getData().getRarity() == COMMON)
                        possibleRewards.add(rewards[i]);
                }
            }
            if (possibleRewards.isEmpty())
                continue;
            PossibleReward reward = possibleRewards.get(Utils.random(possibleRewards.size()));
            int categoryId = getCategoryId(reward.getData().getCategory());
            boolean frozen = false;
            for (int i : frozenCategories) {
                if (categoryId == i) {
                    frozen = true;
                    break;
                }
            }
            if (frozen) {
                skippedFrozen = true;
                continue;
            }
            currentReward = reward;
        }
        if (currentReward.getData().getRarity() >= VERY_RARE)
            announceWin();
        setHeartsOfIce(heartsOfIce - (Math.min(getFrozenCategoriesCount(), frozenCategories.length)));
    }

    /**
     * Announces a rare reward.
     */
    private void announceWin() {
        Item item = currentReward;
        String message = "News: " + player.getDisplayName() + " has just won " + "x" + Utils.getFormattedNumber(item.getAmount()) + " of " + item.getName() + " from treasure hunter!";
        World.sendWorldMessage(Colors.ORANGE + "<img=7>" + message + "!", false);

        QueryExecutor.submit(new News(player, "<b><img src=\"../bin/images/news/sof.png\" width=17> " + message + "."));
    }

    public boolean hasRewardByRarity(int rarity) {
        for (PossibleReward reward : rewards)
            if (reward.getData().getRarity() == rarity)
                return true;
        return false;
    }

    private int[] getCurrentFrozenCategories() {
        int maxFrozenCategories = getMaxFrozenCategories();
        int TotalHeartsOfIce = heartsOfIce;
        if (hasTempHeartsOfIce)
            TotalHeartsOfIce += 5;
        int[] frozen = new int[Math.min(TotalHeartsOfIce, maxFrozenCategories)];
        Arrays.fill(frozen, -1);
        for (int i = 0; i < frozen.length; i++) {
            for (int j = 0; j < frozenCategories.length; j++) {
                if (!frozenCategories[j] || frozen[i] == j)
                    continue;
                frozen[i] = j;
                break;
            }
        }
        return frozen;
    }

    private void togglePrefrences(int index) {
        boolean freeze = !prefrences[index];
        prefrences[index] = !prefrences[index];
        boolean hasAnyGem = false;
        for (int i = 0; i < 5; i++) {
            if (prefrences[i]) {
                hasAnyGem = true;
                break;
            }
        }
        if (!hasAnyGem) {
            prefrences[5] = false;
            prefrences[6] = false;
        }
        if (!prefrences[7]) {
            prefrences[8] = false;
            prefrences[9] = false;
        }
        refreshPrefrences();
        if (rewards == null)
            return;
        for (int i = 0; i < rewards.length; i++) {
            TreasureHunterReward data = rewards[i].getData();
            int rarity = data.getRarity();
            String rewardName = rewards[i].getDefinitions().getName().toLowerCase();
            boolean lampOrStar = (rewardName.contains("xp lamp") || rewardName.contains("fallen star")) && !rewardName.contains("prismatic");
            boolean prismatic = rewardName.contains("prismatic");
            boolean isLevel99Skill = false;// getSkillId(data.getCategory()) != -1 &&
                                           // player.getSkills().getLevelForXp(getSkillId(data.getCategory())) >= 99;
            if (index >= 7 && isLevel99Skill) {
                if (prefrences[7] && index == 8 && lampOrStar)
                    TempFrozenCategories[getCategoryId(data.getCategory())] = !prefrences[8];
                else if (prefrences[7] && index == 9 && prismatic)
                    TempFrozenCategories[getCategoryId(data.getCategory())] = !prefrences[9];
                else
                    TempFrozenCategories[getCategoryId(data.getCategory())] = prefrences[7] ? ((!lampOrStar || !prefrences[8]) && (!prismatic || !prefrences[9])) : freeze;
            } else {
                if (isLevel99Skill)
                    continue;
                if (prefrences[rarity] && index == 5 && lampOrStar)
                    TempFrozenCategories[getCategoryId(data.getCategory())] = !prefrences[5];
                else if (prefrences[rarity] && index == 6 && prismatic)
                    TempFrozenCategories[getCategoryId(data.getCategory())] = !prefrences[6];
                else if (rarity == index)
                    TempFrozenCategories[getCategoryId(data.getCategory())] = (!lampOrStar || !prefrences[5]) && (!prismatic || !prefrences[6]) && freeze;
            }
        }
        refreshCategoriesMenu();
        refreshTempFrozenCategories();
    }

    public int getSkillId(String category) {
        for (int i = 0; i < Skills.SKILL_NAME.length; i++) {
            if (Skills.SKILL_NAME[i].equalsIgnoreCase(category))
                return i;
        }
        return -1;
    }

    public int getCategoryId(String category) {
        for (Entry<Long, Object> e : RS3ClientScriptMap.getMap(8519).getValues().entrySet()) {
            if (((String) e.getValue()).equalsIgnoreCase(category)) {
                int key = e.getKey().intValue() - 1;
                return key >= 33 ? 33 : key;
            }
        }
        return -1;
    }

    private void refreshPrefrences() {
        for (int i = 0; i < 5; i++)
            player.getVarBitManager().sendVarBit(28971 + i, prefrences[i] ? 1 : 0);
        player.getVarBitManager().sendVarBit(28977, prefrences[5] ? 1 : 0);
        player.getVarBitManager().sendVarBit(28976, prefrences[6] ? 1 : 0);
        player.getVarBitManager().sendVarBit(28978, prefrences[7] ? 1 : 0);
        player.getVarBitManager().sendVarBit(28980, prefrences[8] ? 1 : 0);
        player.getVarBitManager().sendVarBit(28979, prefrences[9] ? 1 : 0);
    }

    private void toggleTempFrozenCategory(int category) {
        TempFrozenCategories[category - 1] = !TempFrozenCategories[category - 1];
        refreshCategoriesMenu();
        refreshTempFrozenCategories();
    }

    private void refreshTempFrozenCategories() {
        for (int i = 0; i < TempFrozenCategories.length; i++) {
            player.getVarBitManager().sendVarBit(i == 33 ? 26528 : i == 32 ? 34501 : i <= 25 ? 21580 + i : 28987 + (i - 26), TempFrozenCategories[i] ? 1 : 0);
        }
    }

    private void refreshCategoriesMenu() {
        int applyType = canApplyFrozenCategories();
        player.getPackets().sendHideIComponent(1253, 731, applyType == 0 && !Arrays.equals(TempFrozenCategories, frozenCategories));
        player.getPackets().sendIComponentText(1253, 654, (applyType == 0 ? "<col=00ff00>" : "<col=ff0000>") + getTempFrozenCategoriesCount());
        player.getPackets().sendIComponentText(1253, 655, applyType == 0 ? "" : applyType == 1 ? "You cannot freeze more than " + getMaxFrozenCategories() + " categories at once." : "You do not have enough Hearts of Ice to freeze " + getTempFrozenCategoriesCount() + " categories.");
        player.getPackets().sendIComponentText(1253, 653, "" + heartsOfIce);
        refreshHeartsOfIce();
        player.getPackets().sendExecuteScript(3917);
    }

    public int canApplyFrozenCategories() {
        int maxFrozenCategories = getMaxFrozenCategories();
        int TotalHeartsOfIce = heartsOfIce;
        int frozenCategoriesCount = getTempFrozenCategoriesCount();
        if (hasTempHeartsOfIce)
            TotalHeartsOfIce += 5;
        return frozenCategoriesCount <= Math.min(TotalHeartsOfIce, maxFrozenCategories) ? 0 : TotalHeartsOfIce >= maxFrozenCategories ? 1 : 2;
    }

    public int getTempFrozenCategoriesCount() {
        int count = 0;
        for (boolean frozen : TempFrozenCategories)
            if (frozen)
                count++;
        return count;
    }

    private void removeDialogue() {
        player.getPackets().sendHideIComponent(1253, 839, true);
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void generateRandomRewards() {
        rewards = new PossibleReward[39];
        for (int i = 0; i < rewards.length; i++)
            rewards[i] = getRandomRewardForCategory(RS3ClientScriptMap.getMap(8519).getStringValue(i + 1));
    }

    public void refreshPossibleRewards() {
        if (rewards == null)
            generateRandomRewards();
        for (int i = 0; i < rewards.length; i++) {
            int itemIdVar = i == 32 ? 6745 : i == 38 ? 4335 : i <= 31 ? (4065 + (i * 2)) : (4065 + ((i - 1) * 2));
            int rarityVarbit = i == 32 ? 34502 : i == 38 ? 22140 : i <= 31 ? (21474 + (i * 2)) : (21474 + ((i - 1) * 2));
            int amountVarbit = i == 32 ? 34503 : i == 38 ? 22141 : i <= 31 ? (21475 + (i * 2)) : (21475 + ((i - 1) * 2));
            int descStringVarc = i == 32 ? 5915 : i == 38 ? 4138 : i <= 31 ? (3947 + i) : (3947 + (i - 1));
            int isMemberVarc = i == 32 ? 5914 : i == 38 ? 4140 : i <= 31 ? (4038 + i) : (4038 + (i - 1));
            player.getVarBitManager().sendVar(itemIdVar, rewards[i].getId());
            player.getVarBitManager().sendVarBit(rarityVarbit, rewards[i].getData().getRarity() + 1);
            player.getVarBitManager().sendVarBit(amountVarbit, rewards[i].getAmount());
            player.getPackets().sendGlobalString(descStringVarc, ItemExaminesDataParser.getExamine(rewards[i]));
            player.getPackets().sendGlobalConfig(isMemberVarc, rewards[i].getDefinitions().isMembersOnly() ? 1 : 0);
        }
    }

    public int getHeartsOfIce() {
        return heartsOfIce;
    }

    public void setHeartsOfIce(int heartsOfIce) {
        this.heartsOfIce = heartsOfIce;
        refreshHeartsOfIce();
    }

    private void refreshHeartsOfIce() {
        player.getPackets().sendGlobalConfig(4082, heartsOfIce);
        player.getVarBitManager().sendVarBit(37505, hasTempHeartsOfIce ? 0 : 1);// adds 5 hearts of ice every 5 chests opened
    }

    private void toggleFrozenCategory(int category) {
        int categoryIndex = category - 1;
        if (frozenCategories[categoryIndex]) {
            frozenCategories[categoryIndex] = !frozenCategories[categoryIndex];
        } else {
            int maxFrozenCategories = getMaxFrozenCategories();
            int TotalHeartsOfIce = heartsOfIce;
            int frozenCategoriesCount = getFrozenCategoriesCount();
            if (hasTempHeartsOfIce)
                TotalHeartsOfIce += 5;
            if (frozenCategoriesCount >= Math.min(TotalHeartsOfIce, maxFrozenCategories)) {
                refreshFrozenCategories();
                return;
            }
            frozenCategories[categoryIndex] = !frozenCategories[categoryIndex];
        }
        refreshFrozenCategories();
    }

    public boolean canFreeze() {
        int maxFrozenCategories = getMaxFrozenCategories();
        int TotalHeartsOfIce = heartsOfIce;
        int frozenCategoriesCount = getFrozenCategoriesCount();
        if (hasTempHeartsOfIce)
            TotalHeartsOfIce += 5;
        return frozenCategoriesCount < Math.min(TotalHeartsOfIce, maxFrozenCategories);
    }

    public int getFrozenCategoriesCount() {
        int count = 0;
        for (boolean frozen : frozenCategories)
            if (frozen)
                count++;
        return count;
    }

    private void refreshFrozenCategories() {
        for (int i = 0; i < frozenCategories.length; i++) {
            player.getVarBitManager().sendVarBit(i == 33 ? 26527 : i == 32 ? 34500 : i <= 25 ? 21554 + i : 28981 + (i - 26), frozenCategories[i] ? 1 : 0);// actual frozen categories
        }
        player.getVarBitManager().sendVarBit(20806, canFreeze() ? 0 : 1);
        player.getPackets().sendGlobalConfig(4142, getMaxFrozenCategories());
        player.getPackets().sendExecuteScript(9695);
    }

    public int getMaxFrozenCategories() {
        return 10;
    }

    private PossibleReward getRandomRewardForCategory(String category) {
        List<TreasureHunterReward> rewards = TreasureHunterRewardParser.getTreasureHunterRewards().stream().filter(r -> r.getCategory().equalsIgnoreCase(category)).collect(Collectors.toList());
        if (rewards.isEmpty())
            rewards = TreasureHunterRewardParser.getTreasureHunterRewards().stream().filter(r -> r.getCategory().equalsIgnoreCase(RS3ClientScriptMap.getMap(8519).getStringValue(Utils.random(34)))).collect(Collectors.toList());
        TreasureHunterReward data = rewards.get(Utils.random(rewards.size()));
        return new PossibleReward(data.getItemId(), Utils.random(data.getMinAmount(), data.getMaxAmount() + 1), data);
    }

    public static class PossibleReward extends Item {
        private static final long serialVersionUID = -6087426254455288151L;
        private final TreasureHunterReward data;

        public PossibleReward(int id, TreasureHunterReward data) {
            super(id, 1);
            this.data = data;
        }

        public PossibleReward(int id, int amount, TreasureHunterReward data) {
            super(id, amount, 0);
            this.data = data;
        }

        public PossibleReward(Item item, TreasureHunterReward data) {
            super(item);
            this.data = data;
        }

        public TreasureHunterReward getData() {
            super.switchOldAttributes();
            return data;
        }

    }

    private long lastDailySpinsGiveaway;

    public void giveDailyKeys() {
        if (player.isKingOfTheSkillGameMode()) {
            return;
        }
        if (Utils.getMinutesPlayed(player) < 15)
            return;
        if ((Utils.currentTimeMillis() - lastDailySpinsGiveaway) < (24 * 60 * 60 * 1000)) // 24
            // hours
            return;
        lastDailySpinsGiveaway = Utils.currentTimeMillis();
        int previous = dailyKeys;
        dailyKeys++;

        if (player.isExtremeDonator())
            dailyKeys++;

        if (player.isSupremeDonator())
            dailyKeys++;

        if (player.isLegendaryDonator())
            dailyKeys++;

        if (player.isUltimateDonator())
            dailyKeys++;

        if (player.isMasterDonator())
            dailyKeys++;
        if (dailyKeys > 15) // max limit of daily spins bitconfig is 8
            dailyKeys = 15;

        if (dailyKeys > previous)
            player.sendMessage("<col=FF0000>You have been awarded " + (dailyKeys - previous) + " " + "daily Treasure Hunter key" + (dailyKeys == 1 ? "." : "s."));
        if (dailyKeys != previous)
            refreshKeys();
    }

    public void giveBoughtKeys(int amount) {
        if (player.isIronMan() || player.isHCIronMan() || player.isNoviceIronMan() || player.isExpertIronMan() || player.isIntermediateIronMan())
            return;
        int previous = boughtKeys;
        boughtKeys += amount;
        setHeartsOfIce(getHeartsOfIce() + (boughtKeys * 4));
        if (boughtKeys != previous)
            refreshKeys();
    }

    public void giveEarnedSpins(int amount) {
        int previous = earnedKeys;
        earnedKeys += amount;
        if (earnedKeys != previous)
            refreshKeys();
    }

    public void resetKeys() {
        dailyKeys = 0;
        earnedKeys = 0;
        boughtKeys = 0;
        refreshKeys();
    }

    private boolean giveHeartsOfIce;

    private void transferSpins() {
        if (!giveHeartsOfIce) {
            setHeartsOfIce(getHeartsOfIce() + (boughtKeys * 4));
            giveHeartsOfIce = true;
        }
        if (player.getSquealOfFortune().getTotalSpins() <= 0)
            return;
        int amount = 0;
        if (player.getSquealOfFortune().getDailySpins() > 0) {
            this.dailyKeys += player.getSquealOfFortune().getDailySpins();
            amount += player.getSquealOfFortune().getDailySpins();
            player.getSquealOfFortune().setDailySpins(0);
        }
        if (player.getSquealOfFortune().getEarnedSpins() > 0) {
            this.earnedKeys += player.getSquealOfFortune().getEarnedSpins();
            amount += player.getSquealOfFortune().getEarnedSpins();
            player.getSquealOfFortune().setEarnedSpins(0);
        }
        if (player.getSquealOfFortune().getBoughtSpins() > 0) {
            this.boughtKeys += player.getSquealOfFortune().getBoughtSpins();
            amount += player.getSquealOfFortune().getBoughtSpins();
            player.getSquealOfFortune().setBoughtSpins(0);
        }
        setHeartsOfIce(amount * 3);
        player.getPackets().sendGameMessage("<col=00ff00>All of your squeal of fortune spins have been converted to treasure hunter keys!");
        refreshKeys();
    }

}
