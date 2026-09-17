package com.rs.game.player.content.newlottery;

import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.utils.Colors;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class Lottery {

    /**
     * Turning on debug timer makes lottery games start every {@code WAIT_TIME} seconds rather than minutes.
     */
    private static final boolean DEBUG_TIMER = Settings.DEBUG || Settings.TEST_SERVER_MODE;

    /**
     * Disables the lottery coordinator NPC and does not load databases.
     */
    private static final boolean DISABLED = false;

    /**
     * The wait time (in minutes) before starting the lottery after {@code MINIMUM_BETS} bets are placed.
     */
    private static final int WAIT_TIME = 45;

    /**
     * The minimum amount of bets required to start a lottery.
     */
    public static final int MINIMUM_BETS = 3;


    private static final Lottery singleton = new Lottery();

    public static Lottery getSingleton() {
        return singleton;
    }

    public final int coordinatorId = 2998;
    public final WorldTile coordinatorTile = new WorldTile(5021, 735, 1);
    public final Path statsFile = Paths.get("data/lottery/statistics.txt");
    public final Path betsFile = Paths.get("data/lottery/bets.txt");
    public final Path winningsFile = Paths.get("data/lottery/winnings.txt");

    private final AtomicInteger lotteryTicks = new AtomicInteger(WAIT_TIME);
    public final LotteryStatistics statistics = new LotteryStatistics(this);
    public final LotteryWinningsManager winnings = new LotteryWinningsManager(this);
    public final LotteryBetManager bets = new LotteryBetManager(this);
    public volatile NPC npc;
    public volatile boolean starting;
    public volatile Future<?> lotteryTask;



    public void load() {
        if (!DISABLED) {
            npc = new LotteryCoordinatorNPC();
            statistics.load(statsFile);
            bets.load(betsFile);
            winnings.load(winningsFile);
            startLotteryTask();
        }
    }

    public void startLotteryTask() {
        int betters = bets.getBetCount();
        if (lotteryTask == null && betters >= MINIMUM_BETS) {
            npc.setNextForceTalk(new ForceTalk("Picking lottery winner in " + WAIT_TIME + " minutes!"));
            sendMessage("Lottery starting in " + WAIT_TIME + " minutes! [Betters: " + betters + ", Pot: " + bets.formatTotalWinnings() + "]");
            lotteryTicks.set(WAIT_TIME);
            lotteryTask = CoresManager.getServiceProvider().scheduleRepeatingTask2(this::tick, 1, 1,
                    DEBUG_TIMER ? TimeUnit.SECONDS : TimeUnit.MINUTES);
        }
    }

    public void resetLotteryTask() {
        lotteryTask.cancel(true);
        lotteryTask = null;
        lotteryTicks.set(WAIT_TIME);
    }

    public void save() {
        bets.save(betsFile);
        winnings.save(winningsFile);
    }

    public void tick() {
        if (!starting) {
            int ticks = lotteryTicks.decrementAndGet();
            switch (ticks) {
                case 30:
                case 15:
                    int betters = bets.getBetCount();
                    npc.setNextForceTalk(new ForceTalk("Picking lottery winner in " + ticks + " minutes!"));
                    sendMessage("Lottery starting in " + ticks + " minutes! [Betters: " + betters + ", Pot: " + bets.formatTotalWinnings() + "]");
                    break;
                case 0:
                    start(bets.getTotalWinnings());
                    break;
            }
        }
    }

    public boolean start(int totalWinnings) {
        int limit = Integer.MAX_VALUE - LotteryBetType.INVESTOR.amount;
        boolean exceedsCap = totalWinnings > limit;
        if ((exceedsCap || lotteryTicks.get() == 0) && !starting) {
            starting = true;
            if (exceedsCap) {
                sendMessage("The jackpot has exceeded capacity! [Betters: " + bets.getBetCount() + ", Pot: " + bets.formatTotalWinnings() + "]");
            }
            countMinute();
            return true;
        }
        return false;
    }

    private void countMinute() {
        npc.setNextForceTalk(new ForceTalk("Picking lottery winner in 1 minute!"));
        sendMessage("Picking a winner in " + Colors.RED + "1 minute</col>! [Betters: " + bets.getBetCount() + ", Pot: " + bets.formatTotalWinnings() + "]");
        CoresManager.getServiceProvider().executeWithDelay(this::countThirtySeconds, 50);
    }

    private void countThirtySeconds() {
        npc.setNextForceTalk(new ForceTalk("Picking lottery winner in 30 seconds!"));
        sendMessage("Picking a winner in " + Colors.RED + "30 seconds</col>!");
        CoresManager.getServiceProvider().executeWithDelay(this::countFifteenSeconds, 25);
    }

    private void countFifteenSeconds() {
        npc.setNextForceTalk(new ForceTalk("Picking lottery winner in 15 seconds!"));
        sendMessage("Picking a winner in " + Colors.RED + "15 seconds</col>!");
        CoresManager.getServiceProvider().executeWithDelay(this::pickWinner, 25);
    }

    private void pickWinner() {
       CoresManager.getServiceProvider().executeNow(() -> {
            try {
                Files.deleteIfExists(betsFile);
            } catch (IOException e) {
                Logger.getGlobal().catching(e);
            }
        });

        LotteryBet freeBet = null;
        LocalDate currentDate = LocalDate.now();
        LotteryBet winner = bets.getRandomBet();
        String displayName = winner.displayName;

        int totalWinnings = bets.getTotalWinnings();
        int calculateWinnings = (int) (winner.type.share * totalWinnings);
        int taxedWinnings = totalWinnings - calculateWinnings;
        String formatWinnings = bets.formatWinnings(calculateWinnings);

        npc.setNextForceTalk(new ForceTalk("Congratulations to " + displayName + " for winning the lottery!"));
        sendMessage("Congratulations to " + Colors.RED + displayName + "</col>, who's been awarded a " + winner.type.formattedName + " jackpot of " + formatWinnings + "!");


        if (World.isWeekend()) {
            List<LotteryBetType> selectable = new ArrayList<>();
            for (LotteryBetType type : LotteryBetType.ALL) {
                if (taxedWinnings > type.amount) {
                    selectable.add(type);
                }
            }
            if (selectable.isEmpty() && ThreadLocalRandom.current().nextBoolean()) {
                selectable.add(LotteryBetType.NEWBIE);
                selectable.add(LotteryBetType.REGULAR);
                selectable.add(LotteryBetType.GAMBLER);
                selectable.add(LotteryBetType.INVESTOR);
            }
            if(selectable.size() > 0) {
                LotteryBet secondWinner = bets.getRandomBet();
                if (!winner.username.equals(secondWinner.username)) {
                    LotteryBetType newBet = Utils.randomFrom(selectable);
                    sendMessage("The coordinator is feeling generous this weekend! He has decided to award " + Colors.RED + secondWinner.displayName + "</col> a free " + Colors.RED + newBet.formattedName + "</col> bet in the next lottery!");
                    freeBet = new LotteryBet(secondWinner.username, secondWinner.displayName, newBet, secondWinner.mac);
                    Player player = World.getPlayer(secondWinner.username);
                    if (player != null) {
                        player.sendMessage("Congratulations, you've won a free " + Colors.RED + newBet.formattedName + "</col> bet in the next lottery!");
                    }
                }
            }
        }
        String username = winner.username;
        Player player = World.getPlayer(username);
        boolean gaveReward = player != null && winnings.give(player, calculateWinnings);
        if (!gaveReward) {
            winnings.add(winningsFile, new LotteryWinnings(username, calculateWinnings, currentDate));
        }

        statistics.lastWinner = displayName;
        statistics.lastJackpot = calculateWinnings;
        statistics.totalGpWon += calculateWinnings;
        statistics.totalGpTaxed += totalWinnings - calculateWinnings;
        statistics.totalLottos++;
        statistics.totalBets += bets.getBetCount();
        statistics.save(statsFile);

        starting = false;
        resetLotteryTask();
        bets.clear();
        if (freeBet != null) {
            bets.add(betsFile, freeBet);
        }

        CoresManager.getServiceProvider().executeWithDelay(() -> {
            npc.setNextForceTalk(new ForceTalk("The statistics have been refreshed and are now available."));
            CoresManager.getServiceProvider().executeWithDelay(() -> {
                npc.setNextForceTalk(new ForceTalk("Good luck next time, everyone!"));
                npc.setNextAnimation(new Animation(858));
            }, 5);
        }, 5);
    }

    private void sendMessage(Object object) {
        World.sendWorldMessage(Colors.YELLOW + "[Lottery]:</col> " + object.toString(), false);
    }

    public String getRemainingTimeString() {
        int remainingBets = MINIMUM_BETS - bets.getBetCount();
        if (starting) {
            return Colors.GREEN + "Now!";
        } else if (remainingBets > 1) {
            return Colors.RED + remainingBets + " bets";
        } else if (remainingBets == 1) {
            return Colors.RED + "1 bet";
        } else {
            int ticks = lotteryTicks.get();
            return Colors.GREEN + ticks + " minutes";
        }
    }
}
