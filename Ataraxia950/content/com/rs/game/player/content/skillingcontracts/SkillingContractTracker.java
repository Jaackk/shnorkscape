package com.rs.game.player.content.skillingcontracts;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.activites.gim.GIM;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.hcim_news.HcimNewsManager;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import lombok.val;
import lombok.var;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.rs.game.player.content.titles.PlayerTitle.SKILLING_CHAMPION;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class SkillingContractTracker {

    public static final boolean DISABLED = false;

    static final class Tracker {
        final String displayName;
        final String username;

        Tracker(Player player) {
            this(player.getDisplayName(), player.getUsername());
        }

        Tracker(String displayName, String username) {
            this.displayName = displayName;
            this.username = username;
        }

        @Override
        public int hashCode() {
            return username.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof Tracker) {
                Tracker tracker = (Tracker) obj;
                return username.equals(tracker.username);
            }
            return false;
        }
    }

    public final Path dbPath = Paths.get("data/contracts/database.txt");
    public final Path rewardsPath = Paths.get("data/contracts/rewards.txt");
    public final int playersNeeded = Settings.TEST_SERVER_MODE ? 1 : 3;
    public final int trackHours = 24;
    public final ImmutableList<Item> rewards = ImmutableList.of(
            new Item(29557, 2000), // Elder logs.
            new Item(2510, 1500), // Black d'leather.
            new Item(995, 20_000_000), // 20M
            new Item(35886, 6), // Supply cache.
            new Item(41451, 3),  // Supply boxes v
            new Item(41452, 3),
            new Item(41453, 3),
            new Item(41454, 3),
            new Item(41455, 3),
            new Item(28550, 5), // Trisk key.
            new Item(6572, 5), // Uncut onyx.
            new Item(1632, 1500), // Uncut dragonstone.
            new Item(34160, 750), // Searing ashes.
            new Item(35011, 500), // Rune dragon bones.
            new Item(3005, 750), // Unf snapdragon potion.
            new Item(112, 750), // Unf torstol potion.
            new Item(102, 750), // Unf fellstalk potion.
            new Item(37974, 35), // Unf bloodweed potion.
            new Item(31598, 1500), // Dragon skillchompas.
            new Item(31350, 650), // Protean shit v
            new Item(33740, 650),
            new Item(34528, 650),
            new Item(30037, 650),
            new Item(32337, 650),
            new Item(13593, 10), // Old tome.
            new Item(6199, 10), // Mbox.
            new Item(452, 750), // Rune ore.
            new Item(8789, 75), // Magic stones.
            new Item(37694, 2) // Skilling backpack.
    );


    public final Multiset<Tracker> completedTasks = ConcurrentHashMultiset.create();
    private final Set<String> rewardSet = Sets.newConcurrentHashSet();
    private volatile String lastWinner;
    private final AtomicInteger ticks = new AtomicInteger(0);
    private final AtomicBoolean accepting = new AtomicBoolean(true);
    public int highestScore;

    public void init() throws IOException {
        if (DISABLED)
            return;

        // Load rewards, if needed.
        if (Files.exists(rewardsPath)) {
            try (Scanner scanner = new Scanner(rewardsPath)) {
                while (scanner.hasNextLine()) {
                    rewardSet.add(scanner.nextLine());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Load main file, if needed.
        if (Files.exists(dbPath)) {
            try (Scanner scanner = new Scanner(dbPath)) {
                String readString = scanner.nextLine(); // Read last winner.
                if (readString.equals("<null>")) {
                    lastWinner = null;
                } else {
                    lastWinner = readString;
                }
                ticks.set(Integer.parseInt(scanner.nextLine())); // Read ticks.
                scanner.nextLine(); // Read separator.
                while (scanner.hasNextLine()) { // Read completed task counts.
                    String[] args = scanner.nextLine().split(":");
                    String username = args[0];
                    String displayName = args[1];
                    int occurrences = Integer.parseInt(args[2]);
                    completedTasks.add(new Tracker(displayName, username), occurrences);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            setHighestScore();
        }
        start();
    }

    public void save() {
        saveDb();
        saveRewards();
    }

    public void saveOneDb(Tracker tracker) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dbPath.toFile(), true))) {
            writer.newLine();
            writer.write(tracker.username +
                    ":" +
                    tracker.displayName +
                    ":" +
                    1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void saveDb() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dbPath.toFile()))) {
            writer.write(lastWinner == null ? "<null>" : lastWinner);
            writer.newLine();
            writer.write(Integer.toString(ticks.get()));
            writer.newLine();
            writer.write("----------");
            Set<Multiset.Entry<Tracker>> entries = completedTasks.entrySet();
            for (Multiset.Entry<Tracker> entry : entries) {
                val tracker = entry.getElement();
                writer.newLine();
                writer.write(tracker.username);
                writer.write(":");
                writer.write(tracker.displayName);
                writer.write(":");
                writer.write(Integer.toString(entry.getCount()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void saveRewards() {
        try (FileWriter writer = new FileWriter(rewardsPath.toFile())) {
            boolean firstIteration = true;
            for (String username : rewardSet) {
                if (!firstIteration) {
                    writer.write('\n');
                }
                writer.write(username);
                firstIteration = false;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void login(Player player) {
        if (rewardSet.contains(player.username)) {
            if (player.getBank().hasBankSpace()) {
                val item = giveItem(player);
                player.sendMessage("You have pending skilling champion rewards. Your reward: " + Colors.RED + item.getName() + "(x" + item.getAmount() + ")</col> was sent to your bank. You have also received 500 Skilling tickets.");
                if (isChampion(player)) {
                    if (player.getAppearence().getTitle() == -1 && player.getCustomTitle() == null) {
                        player.sendMessage("Your " + Colors.RED + "Skilling Champion</col> title has been displayed.");
                        player.getAppearence().setTitle(SKILLING_CHAMPION.getTitleId());
                    } else {
                        player.sendMessage("The " + Colors.RED + "Skilling Champion</col> title is now unlocked.");
                    }
                }
                rewardSet.remove(player.getUsername());
                CoresManager.getServiceProvider().executeNow(this::saveRewards);
            } else {
                player.sendMessage(Colors.RED + "You have pending skilling champion rewards. Please make space in your bank and you will receive them on your next login.");
            }
        }
    }

    public void start() {
        if (DISABLED)
            return;
        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                ticks.incrementAndGet();
                tick();
            }
        }, Settings.TEST_SERVER_MODE ? 10 : 6000, Settings.TEST_SERVER_MODE ? 10 : 6000); // Run every hour.
    }

    public void track(Player player) {
        if (accepting.get() && !DISABLED) {
            val newTracker = new Tracker(player);
            completedTasks.add(newTracker);
            SkillingChampionHighscores.getInstance().rebuildList = true;
            setHighestScore();
            CoresManager.getServiceProvider().executeNow(() -> saveOneDb(newTracker));
        }
    }

    public String getLastWinner() {
        return lastWinner;
    }

    public boolean isChampion(Player player) {
        return player.getDisplayName().equals(SkillingContractTracker.getSingleton().getLastWinner());
    }

    public String displayNextIn() {
        int remaining = (trackHours - ticks.get());
        if (DISABLED) {
            return Colors.RED + "Disabled</col>";
        } else if (remaining == 1) {
            return Colors.GREEN + "less than an hour!</col>";
        } else if (remaining > 1) {
            return Colors.GREEN + remaining + " hours</col>";
        } else {
            return Colors.RED + "N/A</col>";
        }
    }

    public String displayLastWinner() {
        return lastWinner == null ? Colors.RED + "None</col>" : Colors.GREEN + lastWinner + "</col>";
    }

    public String displayTopScore() {
        if (hasEnoughParticipants()) {
            return Colors.GREEN + highestScore + "</col>";
        }
        return Colors.RED + "N/A</col>";
    }

    public String getYourScore(Player player) {
        int score = completedTasks.count(new Tracker(player));
        if (hasEnoughParticipants() && score > 0) {
            if (score >= highestScore) {
                return Colors.GREEN + score + "</col>";
            } else if (score >= (highestScore / 2)) {
                return Colors.ORANGE + score + "</col>";
            }
        }
        return Colors.RED + score + "</col>";
    }

    public void tick() {
        if (ticks.get() >= trackHours) {
            accepting.set(false);
            if (hasEnoughParticipants()) {
                String previousWinner = lastWinner;
                Multiset.Entry<Tracker> winner = getHighestOccurrence();
                lastWinner = winner.getElement().displayName;
                sendMessage("The champion for the current " + Colors.RED + "24h</col> period is " + Colors.RED + lastWinner + "</col>, with " + Colors.RED + winner.getCount() + "</col> skilling contracts completed!");
                Player player = World.getPlayer(winner.getElement().username);
                if (player == null) {
                    rewardSet.add(winner.getElement().username);
                    CoresManager.getServiceProvider().executeNow(this::saveRewards);
                } else {
                    if (!player.getBank().hasBankSpace()) {
                        rewardSet.add(winner.getElement().username);
                        CoresManager.getServiceProvider().executeNow(this::saveRewards);
                        player.sendMessage(Colors.RED + "You are the new skilling champion! Please make space in your bank and you will receive your reward on your next login.");
                    } else {
                        val item = giveItem(player);
                        player.sendMessage(Colors.RED + "You are the new skilling champion! Your reward: " + item.getName() + "(x" + item.getAmount() + ") was sent to your bank! You have also received 500 Skilling tickets.");
                        HcimNewsManager.getInstance().addNews(player, "<#player> was the skilling champion!");
                    }

                    if (!Objects.equals(lastWinner, previousWinner)) {
                        player.sendMessage("You have temporarily unlocked the " + Colors.RED + "Skilling Champion</col> title.");
                        player.getAppearence().setTitle(SKILLING_CHAMPION.getTitleId());
                        World.computePlayerByDisplayName(previousWinner).
                                filter(plr -> plr.getAppearence().getTitle() == 300).
                                ifPresent(plr -> {
                                    plr.getAppearence().setTitle(-1);
                                    plr.sendMessage("Your " + Colors.RED + "Skilling Champion</col> title has been removed.");
                                });
                    }
                }
            } else {
                sendMessage("No champion has been selected for this period.");
            }
            SkillingContractManager.multiplier = 1.0;
            SkillingContractManager.currentTotalForMultiplier = SkillingContractManager.TOTAL_FOR_MULTIPLIER;
            SkillingContractManager.totalCompleted = 0;
            completedTasks.clear();
            SkillingChampionHighscores.getInstance().rebuildList = true;
            ticks.set(0);
            CoresManager.getServiceProvider().executeNow(() -> {
                saveDb();
                accepting.set(true);
            });
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    sendMessage("Starting new " + Colors.RED + "24h</col> skilling champion period. Complete as many skilling contracts as you can!");
                }
            }, Settings.TEST_SERVER_MODE ? 1 : 50);
        } else if (hasEnoughParticipants()) {
            // Otherwise update on progress.
            Multiset.Entry<Tracker> highest = getHighestOccurrence();
            CoresManager.getServiceProvider().executeNow(this::saveDb);
            sendMessage(Colors.RED + highest.getElement().displayName + "</col> is in the lead with " + Colors.RED + highest.getCount() + "</col> skilling contracts completed! Hours remaining: " + Colors.RED + (trackHours - ticks.get()) + "</col>.");
        } else {
            CoresManager.getServiceProvider().executeNow(this::saveDb);
        }
    }

    public int participantsCount() {
        return completedTasks.elementSet().size();
    }

    public boolean hasEnoughParticipants() {
        return participantsCount() >= playersNeeded;
    }

    private Item giveItem(Player player) {
        var item = Utils.randomFrom(rewards);
        player.getBank().addItem(new Item(39922, 500), true);
        player.getBank().addItem(item, true);
        return item;
    }

    private void sendMessage(String msg) {
        World.sendWorldMessage("<img=7><shad=000000>" + Colors.RED + "Skilling Champion:</col> " + msg, false);
    }

    private Multiset.Entry<Tracker> getHighestOccurrence() {
        Multiset.Entry<Tracker> highest = null;
        int count = 0;
        for (Multiset.Entry<Tracker> entry : completedTasks.entrySet()) {
            if (entry.getCount() > count) {
                count = entry.getCount();
                highest = entry;
            }
        }
        if (highest == null)
            throw new IllegalStateException("Impossible, could not generate winner for skilling champion.");
        return highest;
    }

    private void setHighestScore() {
        highestScore = 0;
        for (Multiset.Entry<Tracker> entry : completedTasks.entrySet()) {
            if (entry.getCount() > highestScore) {
                highestScore = entry.getCount();
            }
        }
    }

    private static final SkillingContractTracker instance = new SkillingContractTracker();

    public static SkillingContractTracker getSingleton() {
        return instance;
    }
}