package com.rs.game.player.content.contracts;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.activites.gim.GIM;
import com.rs.game.activities.rots.RiseOfTheSixController;
import com.rs.game.npc.NPC;
import com.rs.game.npc.eds.SeiryuTheAzureSerpent;
import com.rs.game.npc.gwd2.vindicta.Vindicta;
import com.rs.game.npc.qbd.QueenBlackDragon;
import com.rs.game.npc.telos.Telos;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.barrows.Barrows;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.player.controllers.BorkController;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import lombok.Getter;
import lombok.val;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class ContractHandler implements Serializable {

    private static final long serialVersionUID = 1;

    public static void assignPlayerNewContract(Player player) {
        assignPlayerNewContract(player, -1);
    }

    public static void assignPlayerNewContract(Player player, int choosenOrdinal) {
        int randomOrdinal = ThreadLocalRandom.current().nextInt(ContractData.values().length);
        while ((randomOrdinal == ContractData.BORK.ordinal() && !BorkController.canKillBork(player))
                || (player.getSkills().getLevel(Skills.SLAYER) < 115 && randomOrdinal == ContractData.THE_MAGISTER.ordinal()) || (player.getSkills().getLevel(Skills.SLAYER) < 95 && (randomOrdinal == ContractData.LEGIO_PRIMUS.ordinal() || randomOrdinal == ContractData.LEGIO_QUARTUS.ordinal() || randomOrdinal == ContractData.LEGIO_QUINTUS.ordinal() || randomOrdinal == ContractData.LEGIO_SECUNDUS.ordinal() || randomOrdinal == ContractData.LEGIO_SEXTUS.ordinal() || randomOrdinal == ContractData.LEGIO_TERTIUS.ordinal()))) {
            randomOrdinal = ThreadLocalRandom.current().nextInt(ContractData.values().length);
        }
        if (choosenOrdinal != -1)
            randomOrdinal = choosenOrdinal;
        ContractData contract = ContractData.values()[randomOrdinal];
        int minLength = contract.getMinimumContractLength(), maxLength = contract.getMaximumContractLength();
        int minReward = contract.getMinimumReaperPointsReward(), maxReward = contract.getMaximumReaperPointsReward();
        if (player.reaperPerkActivated(ReaperPerks.EXTENDED_MASSACRE)) {
            minLength = (int) (minLength + (Math.ceil(minLength * 0.25)));
            maxLength = (int) (maxLength + (Math.ceil(maxLength * 0.25)));
        }
        int killAmount = contract.ordinal() == ContractData.BORK.ordinal() ? 1 : minLength == maxLength ? minLength : ThreadLocalRandom.current().nextInt(minLength, maxLength + 1);
        player.setContract(new Contract(contract.getNpcId(), 995, ThreadLocalRandom.current().nextInt(minReward, maxReward + 1), ((Settings.DEBUG || Settings.TEST_SERVER_MODE) && randomOrdinal != ContractData.BORK.ordinal() ? 2 : killAmount)));
        player.getContract().setCompleted(false);
    }

    public static void updateContract(Player player, NPC npc) {
        if (npc instanceof QueenBlackDragon) {
            npc.setId(ContractData.QUEEN_BLACK_DRAGON.npcId);
        }
        if (npc instanceof Telos) {
            npc.setId(ContractData.TELOS.npcId);
        }
        if (npc instanceof SeiryuTheAzureSerpent) {
            npc.setId(ContractData.SEIRYU.npcId);
        }
        if (npc instanceof Vindicta) {
            npc.setId(ContractData.VINDICTA.npcId);
        }
        if (player == null || npc == null || player.getContract() == null || !isContractNpc(player, npc) || !isNpcContract(player)) {
            return;
        }
        player.getContract().decreaseAmount();
        player.updateSlayerCounterInformation();
        player.incrementTotalReaperContractKills();
        player.getSkills().addXp(Skills.SLAYER, (double) npc.getCombatLevel() / 2);
        announcePossibleTotalKillsTitleUnlock(player);
        int contractKillsRemaining = player.getContract().getKillAmount();
        if (contractKillsRemaining > 0) {
            player.sendMessage(Colors.GOLD + "<shad=292421>You have " + contractKillsRemaining + " " + npc.getName() + (contractKillsRemaining > 1 ? " kills" : " kill") + " remaining.", true);
            return;
        }
        endContract(player);
    }

    /**
     * This is used for updating contracts which aren't npc specific, such as Rise
     * of the Six and Barrows, etc.
     */
    public static void updateNonNpcContract(Player player, int id) {
        if (player == null || id == -1 || player.getContract() == null || isNpcContract(player)) {
            return;
        }
        int contractId = player.getContract().getNpcId();
        if (contractId != id)
            return;
        player.getContract().decreaseAmount();
        player.updateSlayerCounterInformation();
        player.incrementTotalReaperContractKills();
        player.getSkills().addXp(Skills.SLAYER, (double) getXpForNonNpcContract(player) / 2);
        announcePossibleTotalKillsTitleUnlock(player);
        int contractKillsRemaining = player.getContract().getKillAmount();
        if (contractKillsRemaining > 0) {
            player.sendMessage(Colors.GOLD + "<shad=292421>You have " + contractKillsRemaining + " " + Utils.formatPlayerNameForDisplay(ContractData.CONTRACT_FOR_ID.get(player.getContract().getNpcId()).name()) + (contractKillsRemaining > 1 ? " runs" : " run") + " remaining.", true);
            return;
        }
        endContract(player);
    }

    private static void endContract(Player player) {
        int rewardAmount = player.getContract().getRewardAmount();
        int bonusAmount = 0;
        if (player.reaperPerkActivated(ReaperPerks.EXTENDED_MASSACRE)) {
            bonusAmount = (int) Math.ceil(rewardAmount * 0.25);
        }
        rewardAmount += bonusAmount;
        if (bonusAmount > 0) {
            player.sendMessage(Colors.GREEN + "You've completed your Reaper task! You've received " + rewardAmount + " Reaper points (+" + bonusAmount + " perk bonus) for your efforts.");
        } else {
            player.sendMessage(Colors.GREEN + "You've completed your Reaper task! You've received " + rewardAmount + " Reaper points for your efforts.");
        }
        player.incrementTotalReaperContractsCompleted();
        player.addReaperPoints(rewardAmount);
        player.getContract().setCompleted(true);
        player.setContract(null);
        player.getAchievements().updateProgress(1, AchievementList.COMPLETE_10_REAPER_TASKS, AchievementList.COMPLETE_50_REAPER_TASKS);
        announcePossibleContractTitleUnlock(player);
        /*
         *Processing Perks
         **/
        if (!player.isChooseTask() && player.reaperPerkActivated(ReaperPerks.REAPERS_CHOICE) && Math.random() <= ReaperPerks.REAPERS_CHOICE.getActivationChance()) {
            player.setChooseTask(true);
            player.getPackets().sendGameMessage(Colors.CYAN + "Due to the effect of your Reaper's Choice perk, you will be able to choose your next assignment.");
        }
        if (!player.isSkipTask() && player.reaperPerkActivated(ReaperPerks.TAKE_TWO) && Math.random() <= ReaperPerks.TAKE_TWO.getActivationChance()) {
            player.setSkipTask(true);
            player.getPackets().sendGameMessage(Colors.CYAN + "Due to the effect of your Take Two perk, you will be able to skip your next assignment for free.");
        }
    }

    public static String getFormattedContractName(Player player) {
        return (player.getContract() == null || !ContractData.CONTRACT_FOR_ID.containsKey(player.getContract().getNpcId())) ? Colors.wrap(Colors.RED, "Bugged, please report this.") : Utils.formatPlayerNameForDisplay(ContractData.CONTRACT_FOR_ID.get(player.getContract().getNpcId()).name());
    }

    public static boolean isNpcContract(Player player) {
        return player.getContract().getNpcId() != ContractData.BARROWS_RISE_OF_THE_SIX.getNpcId() && player.getContract().getNpcId() != ContractData.BARROWS.getNpcId();
    }

    /**
     * Configured so that it returns the combat level of the minigame npc(s).
     */
    private static int getXpForNonNpcContract(Player player) {
        int contractId = player.getContract().getNpcId();
        if (contractId == ContractData.BARROWS_RISE_OF_THE_SIX.getNpcId() && player.getControlerManager().getControler() instanceof RiseOfTheSixController) {
            return 650;
        } else if (contractId == ContractData.BARROWS.getNpcId() && player.getControlerManager().getControler() instanceof Barrows) {
            return 115;
        }
        return 0;
    }

    private static final ImmutableSet<Integer> araxxorIds = ImmutableSet.of(19457, 19462, 19463, 19465, 19466, 19467, 20998, 20999, 21000, 19464, 21001);
    private static final ImmutableSet<Integer> gregorovicIds = ImmutableSet.of(22313, 22314, 22433, 22442, 22443, 23844);
    private static final ImmutableSet<Integer> helwyrIds = ImmutableSet.of(22309, 22310, 22438, 22440);
    private static final ImmutableSet<Integer> vindictaIds = ImmutableSet.of(22320, 22321, 22458, 22459, 22460, 22461, 22462, 23873, 22322, 22460, 22462);
    private static final ImmutableSet<Integer> twinFuriesIds = ImmutableSet.of(22316, 22453, 22455);
    private static final ImmutableSet<Integer> jadIds = ImmutableSet.of(2745, 15208);
    private static final ImmutableSet<Integer> kalphiteKingIds = ImmutableSet.of(16697, 16698, 16699);
    private static final ImmutableSet<Integer> voragoIds = ImmutableSet.of(17182, 17183, 17184);
    private static final ImmutableSet<Integer> kalphiteQueenIds = ImmutableSet.of(1158, 1159, 1160, 3835, 3836, 4234);

    public static boolean isContractNpc(Player player, NPC npc) {
        if (player.getContract() == null)
            return false;
        int contractNpcId = player.getContract().getNpcId(), npcId = npc.getId();
        if (contractNpcId == ContractData.ARAXXOR.getNpcId()) {
            return araxxorIds.contains(npcId);
        }
        if (contractNpcId == ContractData.GREGOROVIC.getNpcId()) {
            return gregorovicIds.contains(npcId);
        }
        if (contractNpcId == ContractData.HELWYR.getNpcId()) {
            return helwyrIds.contains(npcId);
        }
        if (contractNpcId == ContractData.VINDICTA.getNpcId()) {
            return vindictaIds.contains(npcId);
        }
        if (contractNpcId == ContractData.TWIN_FURIES.getNpcId()) {
            return twinFuriesIds.contains(npcId);
        }
        if (contractNpcId == ContractData.TZTOK_JAD.getNpcId()) {
            return jadIds.contains(npcId);
        }
        if (contractNpcId == ContractData.KALPHITE_KING.getNpcId()) {
            return kalphiteKingIds.contains(npcId);
        }
        if (contractNpcId == ContractData.TORMENTED_DEMON.getNpcId()) {
            return npcId >= 8349 && npcId <= 8366;
        }
        if (contractNpcId == ContractData.VORAGO.getNpcId()) {
            return voragoIds.contains(npcId);
        }
        if (contractNpcId == ContractData.KALPHITE_QUEEN.getNpcId()) {
            return kalphiteQueenIds.contains(npcId);
        }
        return contractNpcId == npcId;
    }

    private static void announcePossibleTotalKillsTitleUnlock(Player player) {
        if (player.getTotalKills() == 5000) {
            World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " has achieved the FINAL BOSS title!", false);
            HcimNewsManager.getInstance().addNews(player,"<#player> achieved the FINAL BOSS title!");
        } else if (player.getTotalKills() == 15000) {
            World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " has achieved the INSANE FINAL BOSS title!", false);
            HcimNewsManager.getInstance().addNews(player,"<#player> achieved the INSANE FINAL BOSS title!");
        }
    }

    private static void announcePossibleContractTitleUnlock(Player player) {
        if (player.getTotalContract() == 500) {
            World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " has achieved the Reaper title!", false);
        } else if (player.getTotalContract() == 1250) {
            World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " has achieved the INSANE Reaper title!", false);
            HcimNewsManager.getInstance().addNews(player,"<#player> achieved the INSANE Reaper title!");
        } else if (player.getTotalContract() == 5000) {
            World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " has achieved the FINAL BOSS title!", false);
            HcimNewsManager.getInstance().addNews(player,"<#player> achieved the FINAL BOSS title!");
        } else if (player.getTotalContract() == 15000) {
            World.sendWorldMessage(Colors.ORANGE + "<shad=000000><img=6>News: " + player.getDisplayName() + " has achieved the INSANE FINAL BOSS title!", false);
            HcimNewsManager.getInstance().addNews(player,"<#player> achieved the INSANE FINAL BOSS title!");
        }
    }

    public enum ContractData {
        ARAXXOR(19464, new int[]{2, 4}, new int[]{10, 30}),
        BARROWS(100_000, new int[]{4, 15}, new int[]{5, 15}),
        BARROWS_RISE_OF_THE_SIX(100_001, new int[]{3, 4}, new int[]{5, 15}),
        BORK(7134, new int[]{1, 1}, new int[]{5, 12}),
        CHAOS_ELEMENTAL(3200, new int[]{10, 40}, new int[]{10, 20}),
        COMMANDER_ZILYANA(6247, new int[]{7, 35}, new int[]{5, 12}),
        CORPOREAL_BEAST(8133, new int[]{4, 15}, new int[]{8, 15}),
        DAGANNOTH_PRIME(2882, new int[]{5, 20}, new int[]{6, 20}),
        DAGANNOTH_REX(2883, new int[]{5, 20}, new int[]{6, 20}),
        DAGANNOTH_SUPREME(2881, new int[]{5, 20}, new int[]{6, 20}),
        GENERAL_GRAARDOR(6260, new int[]{7, 35}, new int[]{5, 12}),
        GLACOR(14301, new int[]{10, 25}, new int[]{8, 20}),
        GREGOROVIC(22442, new int[]{5, 20}, new int[]{10, 30}),
        HAR_AKEN(15211, new int[]{1, 2}, new int[]{30, 50}),
        HELWYR(22438, new int[]{5, 20}, new int[]{10, 30}),
        KALPHITE_KING(16697, new int[]{2, 4}, new int[]{10, 25}),
        KALPHITE_QUEEN(1160, new int[]{4, 10}, new int[]{10, 20}),
        KING_BLACK_DRAGON(50, new int[]{10, 30}, new int[]{5, 12}),
        KREE_ARRA(6222, new int[]{7, 35}, new int[]{5, 12}),
        KRIL_TSUTSAROTH(6203, new int[]{7, 35}, new int[]{5, 12}),
        LEGIO_PRIMUS(17149, new int[]{5, 15}, new int[]{8, 16}),
        LEGIO_QUARTUS(17152, new int[]{5, 15}, new int[]{8, 16}),
        LEGIO_QUINTUS(17153, new int[]{5, 15}, new int[]{8, 16}),
        LEGIO_SECUNDUS(17150, new int[]{5, 15}, new int[]{8, 16}),
        LEGIO_SEXTUS(17154, new int[]{5, 15}, new int[]{8, 16}),
        LEGIO_TERTIUS(17151, new int[]{5, 15}, new int[]{8, 16}),
        NEX(13450, new int[]{2, 4}, new int[]{10, 25}),
        NEX_ANGEL_OF_DEATH(24004, new int[]{3, 6}, new int[]{20, 35}),
        QUEEN_BLACK_DRAGON(15509, new int[]{5, 10}, new int[]{10, 20}),
        TELOS(22891, new int[]{5, 10}, new int[]{15, 30}),
        TORMENTED_DEMON(8351, new int[]{5, 30}, new int[]{5, 12}),
        TWIN_FURIES(22453, new int[]{5, 20}, new int[]{10, 30}),
        TZTOK_JAD(2745, new int[]{1, 2}, new int[]{20, 30}),
        VINDICTA(22460, new int[]{5, 20}, new int[]{10, 30}),
        VORAGO(17184, new int[]{3, 4}, new int[]{15, 30}),
        THE_MAGISTER(24765, new int[]{4, 10}, new int[]{8, 16}),
        SEIRYU(25593, new int[]{2, 2}, new int[]{15, 30});

        public static final ImmutableMap<Integer, ContractData> CONTRACT_FOR_ID;

        static {
            val builder = ImmutableMap.<Integer, ContractData>builder();
            Arrays.stream(values()).forEach(contract -> builder.put(contract.npcId, contract));
            CONTRACT_FOR_ID = builder.build();
        }

        @Getter
        private final int npcId;

        @Getter
        private final int[] contractLengthMinMax;

        @Getter
        private final int[] reaperPointsMinMax;
        private String lazySearchName;

        ContractData(int npcId, int[] contractLengthMinMax, int[] reaperPointsMinMax) {
            this.npcId = npcId;
            this.contractLengthMinMax = contractLengthMinMax;
            this.reaperPointsMinMax = reaperPointsMinMax;

        }

        public String getFormattedName() {
            if (lazySearchName == null) {
                lazySearchName = name().toLowerCase().replaceAll("_", " ");
            }
            return lazySearchName;
        }

        public int getMinimumContractLength() {
            return contractLengthMinMax[0];
        }

        public int getMaximumContractLength() {
            return contractLengthMinMax[1];
        }

        public int getMinimumReaperPointsReward() {
            return reaperPointsMinMax[0];
        }

        public int getMaximumReaperPointsReward() {
            return reaperPointsMinMax[1];
        }

    }

}