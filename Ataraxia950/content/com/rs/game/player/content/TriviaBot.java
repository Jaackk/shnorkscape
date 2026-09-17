package com.rs.game.player.content;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Multiset;
import com.google.gson.reflect.TypeToken;
import com.rs.cores.CoresManager;
import com.rs.game.World;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.controllers.Dungeoneering;
import com.rs.utils.Utils;
import lombok.val;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the Trivia Bot.
 *
 * @author Noel
 */
public class TriviaBot {

    private static final int AMOUNT = 250000;

    private static final Path WINNERS_PATH = Paths.get("./data/trivia_winners.json");
    private static final Multiset<String> winners = ConcurrentHashMultiset.create();
    /**
     * The Question ID.
     */
    public static int questionId = -1;
    /**
     * The Trivia round count.
     */
    public static int round;
    /**
     * If the question has been answered already.
     */
    public static boolean victory;
    /**
     * Total Trivia answers;
     */
    public static int answers;

    private static final TriviaQuestion[] TRIVIA_QUESTIONS = new TriviaQuestion[]{
            //new TriviaQuestion("What is the herb you need to make an Overload Potion?", 2, "Torstol"),
            //new TriviaQuestion("What skills would I need level 80 in to start Invention?", 2, "Crafting", "Divination", "Smithing"),
            //new TriviaQuestion("Where do I go to start Invention?", 2, "Falador", "Invention guild"),
            //new TriviaQuestion("What bosses can I receive the best in slot gear from?", 2, "The Magister", "Telos", "Elite Dungeons"),
            //new TriviaQuestion("True or False; Ataraxia has no custom items", 2, "False"),
            //new TriviaQuestion("Where should I sell my low-tiered PvM loot?", 2, "Shopkeeper"),
            //new TriviaQuestion("What is the maximum total level on Ataraxia?", 2, "2736"),
            //new TriviaQuestion("Where should I sell my high-tier PvM loot?", 2, "Hazelmere", "Market"),
            //new TriviaQuestion("True or False; Creating the T92 Khopesh weapons requires 3 blessings", 2, "True"),
            //new TriviaQuestion("Name one of the Heart of Gielinor bosses.", 3, "Nymora", "Avaryss", "Vindicta", "Gorvek", "Helwyr", "Gregorovic", "Telos"),
            //new TriviaQuestion("Where should I start my Ataraxia journey?", 2, "Guides", "Slayer", "Barrows", "Thieving"),
            //new TriviaQuestion("What attacks does Telos predominantly use?", 2, "Melee"),
            //new TriviaQuestion("What Donator rank do I need to cut the Dream tree at home?", 2, "Diamond"),
            //new TriviaQuestion("How many hours do I have to wait between Voting for Ataraxia?", 2, "12"),
            //new TriviaQuestion("What is the easiest passive money maker?", 2, "Voting"),
            //new TriviaQuestion("Solve the riddle; Shrouded in rock, you seek magic, will your end be tragic?", 2, "Vorago"),
            //new TriviaQuestion("There's no staff online, where do I get help?", 2, "Friends chat", "Discord", "Kirito", "Jaedmo"),
            //new TriviaQuestion("What is the name of the T90 weapons the Kalphite King can drop?", 2, "Drygore"),
            //new TriviaQuestion("How much kill count does it cost to open an instance in GWD2?", 2, "40"),
            //new TriviaQuestion("How many rewards can Nex: Angel of Death potentially give to players?", 2, "7"),
            //new TriviaQuestion("Which of the original GWD bosses can attack with all combat styles", 3, "Kree'arra"),
            //new TriviaQuestion("True or False: Tormented Demons hit through prayer?", 2, "False"),
            //new TriviaQuestion("How many scales or energies can you use to make T90 power armour", 1, "84"),
            //new TriviaQuestion("Name a Developer of Ataraxia", 1, "Jaedmo", "Armarxk1ng", "lare96", "Xenthium"),
            //new TriviaQuestion("Who is your favourite administrator?", 2, "Jaedmo", "Kirito", "Sintricate"),
            //new TriviaQuestion("Who should I talk to if I have account or donation issues?", 2, "Jaedmo", "Kirito", "Sintricate"),
            //new TriviaQuestion("What combined level do you need in Attack/Strength to enter Warriors' Guild?", 2, "130"),
            //new TriviaQuestion("Who is the oldest NPC in Runescape?", 2, "Hans"),
            //new TriviaQuestion("How many fire runes does it take to make a Polypore Staff?", 2, "15000", "15k"),
            //new TriviaQuestion("What is Death's first name?", 3, "Harold"),
            //new TriviaQuestion("Where can I edit my Donator settings", 2, ";;settings"),
            //new TriviaQuestion("How should I begin playing?", 2, "Slayer", "Thieving", "Barrows"),
            //new TriviaQuestion("Where do I get my first Slayer task?", 2, "Kuradal"),
            //new TriviaQuestion("How many Vote Points is a Rare Item Token?", 2, "25"),
            //new TriviaQuestion("How many trivia points is a Crystal Bow?", 2, "30"),
            //new TriviaQuestion("What is the maximum total level you can achieve after typing ;;virtual", 2, "3270"),
            //new TriviaQuestion("Under which City is the Giant Mole located?", 2, "Falador"),
            //new TriviaQuestion("What does the Noxious Weapon's special attack spawn?", 2, "Mirrorback Spiders", "Mirrorback spider"),
            //new TriviaQuestion("What is the upgraded version of Ragefire boots?", 2, "Hailfire boots"),
            //new TriviaQuestion("What is the upgraded version of Steadfast boots?", 2, "Emberkeen Boots"),
            //new TriviaQuestion("What is the upgraded version of Glaiven boots?", 2, "Flarefrost boots"),
            //new TriviaQuestion("How many Ganodermic Flakes is it for the full set?", 2, "7000", "7k"),
            //new TriviaQuestion("What seed do you need to make a Attuned Crystal bow?", 2, "Crystal Weapon", "Crystal Weapon Seed"),
            //new TriviaQuestion("Unscramble this NPC's name. Gravel.", 3, "Elvarg"),
            //new TriviaQuestion("Unscramble this City's name. Egrounda", 3, "Ardougne"),
            //new TriviaQuestion("Varrock is the Capital of what Kingdom?", 3, "Misthalin"),
            //new TriviaQuestion("What did the Wise Old Man steal from the Draynor Bank?", 3, "Blue Partyhat"),
            //new TriviaQuestion("Testing global message", 2, "Castle Wars"),
    };
    private static final TriviaBotHighscoresBuilder highscoresBuilder = new TriviaBotHighscoresBuilder();

    public static void showWinners(Player player) {
        highscoresBuilder.open(player);
    }


    public static void loadWinners() throws IOException {
        if(Files.exists(WINNERS_PATH)) {
            try (BufferedReader reader = Files.newBufferedReader(WINNERS_PATH)) {
                Type mapType = new TypeToken<Map<String, Integer>>() {
                }.getType();
                Map<String, Integer> loaded = Utils.GSON.fromJson(reader, mapType);
                for (val next : loaded.entrySet()) {
                    winners.add(next.getKey(), next.getValue());
                }
            }
        }
    }

    public static void saveWinners() {
        CoresManager.getServiceProvider().executeNow(() -> {
            Map<String, Integer> saveMap = new HashMap<>();
            for (val next : winners.entrySet()) {
                saveMap.put(next.getElement(), next.getCount());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(WINNERS_PATH)) {
                Utils.GSON.toJson(saveMap, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Inits the Trivia Bot's question.
     */
    public static void run() {
        questionId = Utils.random(TRIVIA_QUESTIONS.length);
        answers = 0;
        victory = false;
        for (Player participant : World.getPlayers()) {
            if (participant == null)
                continue;
            participant.hasAnswered = false;
            //participant.sendMessage("<col=56A5EC><shad=000000><img=6>[Trivia]</col></shad> " + TRIVIA_QUESTIONS[questionId].getQuestion(), false);
        }
    }


    /**
     * Sends the Trivia round winner.
     *
     * @param winner The winner.
     * @param player The player.
     */
    public static void sendRoundWinner(Player player) {
        int online = World.getPlayers().size();
        int maxPlayers = (online <= 20 ? 3 : (online <= 40 ? 5 : (online <= 80 ? 7 : (online <= 160 ? 10 : 25))));
        for (Player participant : World.getPlayers()) {
            if (participant == null)
                continue;
            if (answers <= maxPlayers) {
                answers++;
                if (answers == maxPlayers)
                    victory = true;
                final int reward = AMOUNT / answers;
                if (player.getControlerManager().getControler() instanceof Dungeoneering) {
                    player.getBank().addItem(new Item(995, (int) (reward * (TRIVIA_QUESTIONS[questionId].getDifficulty() * 0.75))), true);
                } else
                    player.addMoney((int) (reward * (TRIVIA_QUESTIONS[questionId].getDifficulty() * 0.75)));
                player.setTriviaPoints(player.getTriviaPoints() + 1);
                player.setTotalTrivia(player.getTriviaPoints() + 1);
                player.hasAnswered = true;
                player.sendMessage("<col=56A5EC><shad=000000><img=6>[Trivia]</col></shad> " + "You've answered correctly and won " + Utils.getFormattedNumber((int) (reward * (TRIVIA_QUESTIONS[questionId].getDifficulty() * 0.75))) + " coins!</col>", false);
                player.getAchievements().updateProgress(1, AchievementList.REACH_20_TRIVIA_POINTS);
                winners.add(player.getUsername());
                saveWinners();
                return;
            }
        }
    }

    /**
     * Verifies the Trivia questions answer.
     *
     * @param player The player.
     * @param answer The answer.
     */
    public static void verifyAnswer(final Player player, String answer) {
        if (victory) {
            player.sendMessage("<col=56A5EC><shad=000000><img=6>[Trivia]</col></shad> That round has already been won, wait for the next round.");
            return;
        } else if (player.hasAnswered) {
            player.sendMessage("<col=56A5EC><shad=000000><img=6>[Trivia]</col></shad> You have already answered this question.");
            return;
        }
        boolean correct = false;
        for (String values : TRIVIA_QUESTIONS[questionId].getAnswers()) {
            if (values.equalsIgnoreCase(answer)) {
                correct = true;
                break;
            }
        }
        if (correct) {
            round++;
            sendRoundWinner(player);
        } else
            player.sendMessage("<col=56A5EC><shad=000000><img=6>[Trivia]</col></shad> That answer wasn't correct, please try again.");
    }


    public static Multiset<String> getWinners() {
        return winners;
    }
}

