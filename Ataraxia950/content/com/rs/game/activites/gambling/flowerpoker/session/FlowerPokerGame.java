package com.rs.game.activites.gambling.flowerpoker.session;

import com.rs.cores.CoresManager;
import com.rs.external.api.mailing.MailingAPI;
import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.gambling.flowerpoker.Flower;
import com.rs.game.activites.gambling.flowerpoker.FlowerPokerSession;
import com.rs.game.activites.gambling.flowerpoker.FlowerPokerWinValidator;
import com.rs.game.activites.gambling.flowerpoker.FlowerPokerWinValidator.WinResponse;
import com.rs.game.activites.gambling.flowerpoker.FlowerPokerWinValidator.WinResponseType;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.Magic;
import com.rs.utils.Colors;
import com.rs.utils.FileLogger;
import com.rs.utils.LoggingSystem;
import com.rs.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class FlowerPokerGame implements FlowerPokerSession {
	
    private static final Set<Integer> USED_LOCATIONS = new HashSet<>();
    
    private static final WorldTile[] LOCATION_DATA = {
            new WorldTile(2384, 3104, 0),
            new WorldTile(2378, 3103, 0),
            new WorldTile(2376, 3096, 0),
            new WorldTile(2394, 3080, 0),
            new WorldTile(2406, 3084, 0),
            new WorldTile(2421, 3107, 0),
            new WorldTile(2427, 3109, 0),
            new WorldTile(2415, 3083, 0),
            new WorldTile(2425, 3079, 0),
            new WorldTile(2428, 3079, 0),
    };
    public static final int MAX_CAPACITY_GAMES = LOCATION_DATA.length;
    private static final int NEXT_PERSON_SPEED = 3;
    private static final int TOTAL_FLOWERS = 5;
    private static final FileLogger fileLogger = new FileLogger("fp_logs.txt");
    private final int locationIndex;
    private final Player player1;
    private final Player player2;
    private final List<Item> playerOnesItems;
    private final List<Item> playerTwosItems;
    private final Flower[] playerOnesFlowers;
    private final Flower[] playerTwosFlowers;
    private final FlowerPokerWinValidator flowerPokerWinValidator;
    private final WorldObject[] flowerObjects;
    private int flowerObjectIndex;
    private boolean gameAborted;

    //bugs:
    // can spam click when teleing and walk away,
    // can log out while teleing

    public FlowerPokerGame(Player player1, Player player2, List<Item> playerOnesItems, List<Item> playerTwosItems) {
        this.player1 = player1;
        this.player2 = player2;
        this.playerOnesItems = playerOnesItems;
        this.playerTwosItems = playerTwosItems;
        playerOnesFlowers = new Flower[TOTAL_FLOWERS];
        playerTwosFlowers = new Flower[TOTAL_FLOWERS];
        flowerObjects = new WorldObject[TOTAL_FLOWERS * 2];
        locationIndex = findOptimalLocationIndex();
        flowerPokerWinValidator = new FlowerPokerWinValidator(playerOnesFlowers, playerTwosFlowers);
    }

    @Override
    public void start() {
        forEachPlayer(player -> {
            player.getTemporaryAttributtes().put(FlowerPokerSession.PLAYING_FLOWER_POKER_KEY, true);
            player.getTemporaryAttributtes().put("temp-frozen", true);
            player.lock();
        });
        movePlayerToGame();
        CoresManager.getServiceProvider().executeWithDelay(() -> performGame(0), 7);
    }

    private void movePlayerToGame() {
        WorldTile worldTile = LOCATION_DATA[locationIndex];
        Magic.sendNormalTeleportSpell(player1, 0, 0, worldTile, false, false, false, true);
        Magic.sendNormalTeleportSpell(player2, 0, 0, worldTile.transform(-1, 0, 0), false, false, false, true);
        forEachPlayer(Player::lock);
    }

    private void performGame(int executions) {
        if (executions == TOTAL_FLOWERS) {
            winGame();
            return;
        }
        if (gameAborted) {
            return;
        }
        Flower flower1 = plantFlower(player1);
        if (flower1 != null) {
            playerOnesFlowers[executions] = flower1;
            if (isIllegitimateFlower(flower1)) {
                forEachPlayer(player -> player.sendMessage(Colors.YELLOW + "A " + flower1 + "! Restart the game!"));
                CoresManager.getServiceProvider().executeWithDelay(this::restartGame, NEXT_PERSON_SPEED);
            } else {
                CoresManager.getServiceProvider().executeWithDelay(() -> {
                    Flower flower2 = plantFlower(player2);
                    if (flower2 != null) {
                        playerTwosFlowers[executions] = flower2;
                        if (isIllegitimateFlower(flower2)) {
                            forEachPlayer(player -> player.sendMessage(Colors.YELLOW + "A " + flower2 + "! Restart the game!"));
                            CoresManager.getServiceProvider().executeWithDelay(this::restartGame, NEXT_PERSON_SPEED);
                        } else {
                            CoresManager.getServiceProvider().executeWithDelay(() -> performGame(executions + 1), NEXT_PERSON_SPEED);
                        }
                    }
                }, NEXT_PERSON_SPEED);
            }
        }
    }

    private void winGame() {
        WinResponse winResponse = flowerPokerWinValidator.determineWinner();
        Player winner;
        ForceTalk winningForceTalk = new ForceTalk(winResponse.getWinnerWinType() + "!");
        if (winResponse.getWinResponseType() == WinResponseType.PLAYER_ONE) {
            winner = player1;
        } else if (winResponse.getWinResponseType() == WinResponseType.PLAYER_TWO) {
            winner = player2;
        } else {
            forEachPlayer(player -> {
                player.setNextForceTalk(winningForceTalk);
                player.sendMessage(Colors.YELLOW + "It's a tie!");
            });
            CoresManager.getServiceProvider().executeWithDelay(this::restartGame, 5);
            return;
        }
        Player loser = winner == player1 ? player2 : player1;
        int itemsWon = playerOnesItems.size() + playerTwosItems.size();
        Function<Player, String> namegen = plr -> {
            String display = plr.getDisplayName();
            String user = plr.getUsername();
            if (display == null || display.equalsIgnoreCase(user)) {
                return display + " (" + user + ")";
            } else {
                return user;
            }
        };
        fileLogger.logMessage(namegen.apply(winner) + " has beaten " + namegen.apply(loser) + " [item count: " + itemsWon + "].");

        winner.setNextAnimation(new Animation(866)); // dancing emote
        loser.setNextAnimation(new Animation(860)); // crying emote
        winner.setNextForceTalk(winningForceTalk);
        giveWinnerItems(winner, loser);
        winner.sendMessage(Colors.GREEN + "You won! You got a \"" + winResponse.getWinnerWinType() + "\" while your opponent got a \"" + winResponse.getLoserWinType() + "\".");
        loser.sendMessage(Colors.RED + "You lost. You got a \"" + winResponse.getLoserWinType() + "\" while your opponent got a \"" + winResponse.getWinnerWinType() + "\".");
        CoresManager.getServiceProvider().executeWithDelay(() -> {
            forEachPlayer(player -> {
                player.unlock();
                player.getControlerManager().startControler("GamblingAreaController");
                player.getTemporaryAttributtes().remove(FlowerPokerSession.PLAYING_FLOWER_POKER_KEY);
            });
            postGameActions();
        }, 8);
    }

    private void giveWinnerItems(Player winner, Player loser) {
    	List<Item> items = new ArrayList<Item>();
        for (Item playerTwosItem : playerTwosItems) {
            winner.addItem(playerTwosItem);
            items.add(playerTwosItem);
        }
        
        for (Item playerOnesItem : playerOnesItems) {
            winner.addItem(playerOnesItem);
            items.add(playerOnesItem);
        }
        
        LoggingSystem.logGambling(winner, loser, items);
    }

    private void restartGame() {
        Player randomPlayer = Utils.randomFrom(player1, player2);
        Player otherPlayer = randomPlayer == player1 ? player2 : player1;
        randomPlayer.setNextForceTalk(new ForceTalk("Ah shit, here we go again."));
        CoresManager.getServiceProvider().executeWithDelay(() -> otherPlayer.setNextForceTalk(new ForceTalk("Ukh.")), 2);
        postGameActions();
        FlowerPokerSession newGame = new FlowerPokerGame(player1, player2, playerOnesItems, playerTwosItems);
        newGame.start();
    }

    private boolean isIllegitimateFlower(Flower flower) {
        return flower == Flower.BLACK || flower == Flower.WHITE;
    }

    private Flower plantFlower(Player player) {
        if (gameAborted) {
            return null;
        }
        if (player1.getControlerManager().getControler() != null || player2.getControlerManager().getControler() != null) {
            forEachPlayer(each -> {
                each.setNextForceTalk(new ForceTalk("THE GAME HAS BEEN ABORTED!"));
                each.sendMessage("The game has been aborted due to an unforeseen problem. Please report this to a developer.");
                sendEmergencyIssue();
            });
            abort();
            return null;
        }
        double random = Math.random();
        Flower flower = Flower.getFlowerForChance(random);
        WorldObject flowerObject = new WorldObject(flower.getObjectId(), 10, 0, player.getX(), player.getY(), player.getPlane());
        flowerObjects[flowerObjectIndex++] = flowerObject;
        World.spawnObject(flowerObject, true);
        player.addWalkSteps(player.getX(), player.getY() - 1, 1);
        CoresManager.getServiceProvider().executeWithDelay(() -> player.faceObject(flowerObject), 1);
        player.lock();
        return flower;
    }

    private void sendEmergencyIssue() {
        MailingAPI.sendCustomEmail("Critical error with Flower Poker!", "CONTROLLER ISSUE IN FLOWER POKER! Player1 (" + player1 + ") was at " + player1.getControlerManager().getControler() + " while Player2 (" + player2 + ") was at " + player2.getControlerManager().getControler() + ".");
    }

    private void postGameActions() {
        for (WorldObject flowerObject : flowerObjects) {
            if (flowerObject != null) {
                World.removeObject(flowerObject);
            }
        }
        USED_LOCATIONS.remove(locationIndex);
        forEachPlayer(player -> {
            player.setFlowerPokerSession(null);
            player.getTemporaryAttributtes().remove("temp-frozen");
        });
    }

    @Override
    public void abort() {
        postGameActions();
        gameAborted = true;
        for (Item playerOnesItem : playerOnesItems) {
            player1.addItem(playerOnesItem);
        }
        for (Item playerTwosItem : playerTwosItems) {
            player2.addItem(playerTwosItem);
        }
        forEachPlayer(player -> {
            player.getTemporaryAttributtes().remove(FlowerPokerSession.PLAYING_FLOWER_POKER_KEY);
            player.getControlerManager().startControler("GamblingAreaController");
        });
    }

    private int findOptimalLocationIndex() {
        int randomIndex = Utils.random(LOCATION_DATA.length);
        if (USED_LOCATIONS.contains(randomIndex)) {
            return findOptimalLocationIndex();
        }
        USED_LOCATIONS.add(randomIndex);
        return randomIndex;
    }

    private void forEachPlayer(Consumer<Player> playerConsumer) {
        playerConsumer.accept(player1);
        playerConsumer.accept(player2);
    }

    public static int getActiveGames() {
        return USED_LOCATIONS.size();
    }
}
