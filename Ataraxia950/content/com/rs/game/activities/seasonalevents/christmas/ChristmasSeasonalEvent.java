package com.rs.game.activities.seasonalevents.christmas;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.rs.Settings;
import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activities.seasonalevents.SeasonalEvent;
import com.rs.game.activities.seasonalevents.SeasonalEventManager;
import com.rs.game.activities.seasonalevents.SeasonalEventPeriod;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.DataInterface;
import com.rs.game.player.Equipment;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.TemporaryAttributes;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.JsonSerializable;
import com.rs.utils.Utils;
import com.rs.utils.data.parsers.npcs.pojos.NPCDirection;
import lombok.val;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96
 */
public final class ChristmasSeasonalEvent extends SeasonalEvent {

    public static final int SANTA_NPC = 18829;
    public static final int LARGE_PRESENT_ITEM = 15420;
    static final int TREE_PRESENT_ITEM = 6199;
    public static final int SMALL_PRESENT_ITEM = 6542;
    private static final int SMALL_PRESENT_CHANCE;
    private static final int TREE_PRESENT_CHANCE;
    private static final int HINT_FREQUENCY = 12_000;
    static final int HINTS_TO_GIVE = 10;
    private static final int PRESENT_HINT_ICON_RADIUS = 25;
    public static final int CMAS_AMULET_CHARGES = 2000;
    public static final int PRESENT_SACK_CAP = 25;

    static {
        SMALL_PRESENT_CHANCE = Settings.TEST_SERVER_MODE ? 3 : 100;
        TREE_PRESENT_CHANCE = Settings.TEST_SERVER_MODE ? 2 : 50;
    }

    static final ImmutableList<String> SNOWMAN_EXAMINE = ImmutableList.of(
            "Look what Santa made!",
            "I'm fighting the urge to break you right now.",
            "Ahh... I love Christmas.",
            "Looks dapper.",
            "Did it just move?"
    );

    static final ImmutableList<String> FOUND_BIG_PRESENT = ImmutableList.of(
            "Hey, it's one of Santa's presents!",
            "This must've been for someone on his 'Nice' list...",
            "Wow, it feels full!",
            "I wonder what's inside this one...",
            "Wow, I found one!",
            "I wonder what I should REALLY do with this one.."
    );

    static final Path READ_INTRODUCTION_PATH = getPath("christmas", "read_introduction.json");
    static final Path TREE_PRESENT_PATH = getPath("christmas", "tree_presents.json");
    static final Path RETURNED_PRESENTS_PATH = getPath("christmas", "returned_presents.json");
    static final Path STOLEN_PRESENTS_PATH = getPath("christmas", "stolen_presents.json");

    static final Set<String> readIntroduction = Sets.newConcurrentHashSet();
    static final Set<String> treePresents = Sets.newConcurrentHashSet();
    static final Multiset<String> returnedPresents = ConcurrentHashMultiset.create();
    static final Multiset<String> stolenPresents = ConcurrentHashMultiset.create();


    private static SendHintsTask hintTask = new SendHintsTask();

    public static SendHintsTask getHintTask() {
        return hintTask;
    }

    public static void addToPresentSack(Player player, Item item) {
        if (item.getAttributes() == null) {
            item.setAttributes(new ConcurrentHashMap<>());
        }
        Object sackPresentsObj = item.getAttributes().get(TemporaryAttributes.Key.PRESENT_SACK);
        int sackPresents = sackPresentsObj == null ? 0 : (int) sackPresentsObj;
        if (sackPresents >= PRESENT_SACK_CAP) {
            player.sendMessage("Your present sack is full.");
            return;
        }
        int totalAmount = player.getInventory().getAmountOf(SMALL_PRESENT_ITEM);
        int remainingSackSpace = PRESENT_SACK_CAP - sackPresents;
        if (totalAmount > remainingSackSpace) {
            totalAmount = remainingSackSpace;
        }
        player.getInventory().deleteItem(SMALL_PRESENT_ITEM, totalAmount);
        item.getAttributes().put(TemporaryAttributes.Key.PRESENT_SACK, totalAmount + sackPresents);
    }

    public static void checkPresentSack(Player player, Item item) {
        if (item.getAttributes() == null) {
            item.setAttributes(new ConcurrentHashMap<>());
        }
        Object value = item.getAttributes().get(TemporaryAttributes.Key.PRESENT_SACK);
        if (value != null) {
            Integer presents = (Integer) value;
            if (presents >= PRESENT_SACK_CAP) {
                player.sendMessage("Your present sack is full. You should return them all to Santa.");
            } else {
                player.sendMessage("Your sack has " + presents + " presents inside.");
            }
        } else {
            player.sendMessage("Your present sack is empty.");
        }
    }

    private static int getPresentsInSack(Item item, boolean remove) {
        if (item.getAttributes() == null) {
            item.setAttributes(new ConcurrentHashMap<>());
            return 0;
        }

        Object sackValue;
        if (remove) {
            sackValue = item.getAttributes().remove(TemporaryAttributes.Key.PRESENT_SACK);
        } else {
            sackValue = item.getAttributes().get(TemporaryAttributes.Key.PRESENT_SACK);
        }
        if (sackValue == null) {
            return 0;
        }
        return (int) sackValue;
    }

    public static void checkCmasAmulet(Player player, Item item) {
        if (item.getAttributes() == null) {
            item.setAttributes(new ConcurrentHashMap<>());
        }
        Object value = item.getAttributes().get(TemporaryAttributes.Key.CMAS_AMULET);
        if (value != null) {
            Integer charges = (Integer) value;
            int percentage = (int) (((double) charges / ChristmasSeasonalEvent.CMAS_AMULET_CHARGES) * 100);
            if (percentage == 0) {
                percentage = 1;
            }
            player.sendMessage("Your Christmas amulet has " + percentage + "% of its charges left.");
        } else {
            player.sendMessage("Your Christmas amulet has 100% of its charges left.");
        }
    }

    public static boolean useCmasAmulet(Player player) {
        Item item = player.getEquipment().getItem(Equipment.SLOT_AMULET);
        if (player.getEquipment().getAmuletId() == 26492) {
            if (item.getAttributes() == null) {
                item.setAttributes(new ConcurrentHashMap<>());
            }
            Object value = item.getAttributes().putIfAbsent(TemporaryAttributes.Key.CMAS_AMULET, CMAS_AMULET_CHARGES);
            int newValue;
            if (value == null) {
                newValue = CMAS_AMULET_CHARGES - 1;
            } else {
                Integer oldValue = (Integer) value;
                newValue = oldValue - 1;
            }
            if (newValue <= 0) {
                player.getEquipment().set(Equipment.SLOT_AMULET, null);
                player.sendMessage("Your Christmas amulet has turned to dust.");
                return true;
            }
            item.getAttributes().put(TemporaryAttributes.Key.CMAS_AMULET, newValue);
            return true;
        }
        return false;
    }

    public static void findLargePresent(Player player, WorldObject object) {
        if (object.getId() == 95001 && hasStartedEvent(player)) {
            player.lock();
            Dialogue.sendPlayerDialogueNoContinue(player, Dialogue.NORMAL, Utils.randomFrom(FOUND_BIG_PRESENT));
            WorldTasksManager.schedule(new WorldTask() {
                private int loops;

                @Override
                public void run() {
                    if (loops == 1) {
                        player.setNextAnimation(new Animation(827));
                    } else if (loops == 2) {
                        Dialogue.closeNoContinueDialogue(player);
                    } else if (loops >= 3) {
                        stop();
                        if (hintTask.deleteHint(object)) {
                            stolenPresents.add(player.getUsername());
                            player.hintIconsManager.removeUnsavedHintIcon();
                            player.presentHintArrow = null;
                            CoresManager.getServiceProvider().executeNow(() -> JsonSerializable.save(STOLEN_PRESENTS_PATH, stolenPresents));
                            player.getInventory().addItemDrop(LARGE_PRESENT_ITEM, 1);
                            Dialogue.sendSinglePlayerDialogue(player, Dialogue.GOOFY_LAUGH, "I think I'll keep this one for myself...");
                            World.removeObject(object);
                        } else {
                            Dialogue.sendSinglePlayerDialogue(player, Dialogue.HAPPY, "Grr... Someone picked it up before I could!");
                        }
                    }
                    loops++;
                }

                @Override
                public void onStop() {
                    player.unlock();
                }
            }, 1, 1);
        }
    }

    public static boolean skillGivesPresent(int skill) {
        return skill == Skills.WOODCUTTING || skill == Skills.FISHING || skill == Skills.MINING || skill == Skills.AGILITY || skill == Skills.THIEVING || skill == Skills.FARMING || skill == Skills.HUNTER;
    }

    public static void awardSmallPresent(Player player, WorldTile tile, boolean drop) {
        if (ThreadLocalRandom.current().nextInt(SMALL_PRESENT_CHANCE) == 0 && SeasonalEventManager.isActive(ChristmasSeasonalEvent.class) && hasStartedEvent(player)) {
            val item = new Item(SMALL_PRESENT_ITEM, 1);
            if (player.getEquipment().getCapeId() == 47592) {
                val cape = player.getEquipment().getItem(Equipment.SLOT_CAPE);
                int amount = getPresentsInSack(item, false);
                if (amount < PRESENT_SACK_CAP) {
                    cape.getAttributes().put(TemporaryAttributes.Key.PRESENT_SACK, amount + 1);
                    player.sendMessage(Colors.RED + "You add one of Santa's lost presents to your sack.");
                    return;
                }
            }
            if (drop) {
                World.addGroundItem(item, tile, player, true, 120);
            } else {
                player.getInventory().addItemDrop(item, tile);
            }
            player.sendMessage(Colors.RED + "You find one of Santa's lost presents!");
        }
    }

    public static void awardSmallPresent(Player player, WorldTile tile) {
        awardSmallPresent(player, tile, false);
    }

    public static void awardSmallPresent(Player player) {
        awardSmallPresent(player, player);
    }

    public static boolean hasStartedEvent(Player player) {
        return readIntroduction.contains(player.getUsername());
    }

    public static boolean hasGottenTreePresent(Player player) {
        return treePresents.contains(player.getUsername());
    }

    public static void giveSantaPresents(Player player) {
        if (!hasStartedEvent(player)) {
            Dialogue.sendSinglePlayerDialogue(player, Dialogue.NORMAL, "I should talk to him before asking for favors.");
            return;
        }

        Dialogue.sendNPCDialogueNoContinue(player, SANTA_NPC, Dialogue.NORMAL, "Let's see what you've got here...");
        player.lock();

        WorldTasksManager.schedule(new WorldTask() {
            @Override
            public void run() {
                Dialogue.closeNoContinueDialogue(player);
                player.unlock();
                int presentsAmount = player.getInventory().getNumberOf(SMALL_PRESENT_ITEM);
                Item sackItem = player.getInventory().getItemById(47592);
                if (sackItem == null && player.getEquipment().getCapeId() == 47592) {
                    sackItem = player.getEquipment().getItem(Equipment.SLOT_CAPE);
                }
                if (sackItem != null) {
                    presentsAmount += getPresentsInSack(sackItem, true);
                }
                if (presentsAmount > 0) {
                    player.getInventory().deleteAllId(SMALL_PRESENT_ITEM);
                    int previousAmount = returnedPresents.add(player.getUsername(), presentsAmount);
                    JsonSerializable.save(RETURNED_PRESENTS_PATH, returnedPresents);
                    int previousBase = previousAmount / 10;
                    int currentBase = (previousAmount + presentsAmount) / 10;
                    if (currentBase > previousBase) {
                        int rewardAmount = currentBase - previousBase;
                        player.getDialogueManager().startDialogue(new SantaExtraRewardDialogue(rewardAmount));
                    } else {
                        Dialogue.sendSingleNPCDialogue(player, SANTA_NPC, Dialogue.NORMAL, "Ah, splendid! Thank you for being honest.");
                    }
                } else {
                    Dialogue.sendSingleNPCDialogue(player, SANTA_NPC, Dialogue.ANGRY, "You haven't brought me anything!");
                }
            }
        }, 3);
    }

    public static void viewHints(Player player) {
        if (!hasStartedEvent(player)) {
            Dialogue.sendSinglePlayerDialogue(player, Dialogue.NORMAL, "I should talk to Santa before looking at his list.");
            return;
        }
        val hints = hintTask.getHints();
        if (hints.isEmpty()) {
            Dialogue.sendSinglePlayerDialogue(player, Dialogue.NORMAL, "Oh... looks like the list is empty.");
            return;
        }
        DataInterface inter = new DataInterface("Present locations");
        int hintCount = 1;
        for (val next : hints) {
            inter.add(Colors.DARK_RED + "Hint #" + hintCount++);
            inter.setWordWrap(true);
            inter.add(next.getHint());
            inter.setWordWrap(false);
            inter.blankLine();
            inter.blankLine();
        }
        inter.show(player);
    }

    public static void viewNiceHighscores(Player player) {
        PresentsHighscoresBuilder.showNice(player);
    }

    public static void viewNaughtyHighscores(Player player) {
        PresentsHighscoresBuilder.showNaughty(player);
    }

    public static void inspectSnowman(Player player) {
        player.getDialogueManager().startDialogue(new InspectSnowmanDialogue());
    }

    public static void lookUnderTree(Player player) {
        if (hasGottenTreePresent(player)) {
            Dialogue.sendSinglePlayerDialogue(player, Dialogue.NORMAL, "He already left something here for me before, I doubt there's anything else.");
            return;
        }
        player.lock();
        Dialogue.sendPlayerDialogueNoContinue(player, Dialogue.NORMAL, "Hmm, I wonder if Santa left anything here for me yet...");
        WorldTasksManager.schedule(new WorldTask() {
            private int loops;

            @Override
            public void run() {
                if (loops == 1) {
                    player.setNextAnimation(new Animation(827));
                } else if (loops == 2) {
                    Dialogue.closeNoContinueDialogue(player);
                } else if (loops == 3) {
                    boolean playedEnough = Settings.TEST_SERVER_MODE || Utils.getMinutesPlayed(player) > 720;
                    int freeSlots = player.getInventory().getFreeSlots();
                    if (ThreadLocalRandom.current().nextInt(TREE_PRESENT_CHANCE) == 0 && playedEnough && !hasGottenTreePresent(player) && freeSlots > 0) {
                        player.getDialogueManager().startDialogue(new LookUnderTreeDialogue(freeSlots));
                    } else {
                        Dialogue.sendSinglePlayerDialogue(player, Dialogue.HAPPY, "Doesn't seem like it...");
                    }
                } else if (loops >= 4) {
                    stop();
                }
                loops++;
            }

            @Override
            public void onStop() {
                player.unlock();
            }
        }, 1, 1);
    }

    public static void checkNearbyPresents(Player player) {
        for (val next : hintTask.getHints()) {
            val hintTile = next.getHintTile();
            WorldTile presentHintArrow = player.presentHintArrow;
            if (player.withinDistance(hintTile, PRESENT_HINT_ICON_RADIUS) && (
                    presentHintArrow == null ||
                            hintTile.getX() != presentHintArrow.getX() ||
                            hintTile.getY() != presentHintArrow.getY() ||
                            hintTile.getPlane() != presentHintArrow.getPlane())) {
                player.hintIconsManager.addHintIcon(hintTile.getX(), hintTile.getY(), hintTile.getPlane(), 85, 5, 0, -1, false);
                player.presentHintArrow = hintTile;
                return;
            }
        }
        if (player.presentHintArrow != null && !player.withinDistance(player.presentHintArrow, PRESENT_HINT_ICON_RADIUS)) {
            player.hintIconsManager.removeUnsavedHintIcon();
            player.presentHintArrow = null;
        }
    }

    public static void inspectSmallPresent(Player player) {
        if (!SeasonalEventManager.isActive(ChristmasSeasonalEvent.class)) {
            player.sendMessage("This can only be used during the month of December.");
            return;
        }
        if (hasStartedEvent(player)) {
            Dialogue.sendSinglePlayerDialogue(player, Dialogue.NORMAL,
                    "Okay, I'll return this one to Santa.");
        } else {
            Dialogue.sendSinglePlayerDialogue(player, Dialogue.NORMAL,
                    "I shouldn't open it... it might be someone's Christmas present!",
                    "I think I saw Santa at ;;home, maybe it's his.");
        }
    }

    @Override
    protected void start() {
        objects.add(new WorldObject(95024, 10, 2, 5023, 708, 1));
        objects.add(new WorldObject(66006, 10, 2, 5022, 709, 1));
        objects.add(new WorldObject(88923, 10, 2, 5028, 710, 1));

        val santa = new NPC(SANTA_NPC, new WorldTile(5026, 709, 1), -1, false);
        santa.setLocked(true);
        santa.setSpawnDirection(NPCDirection.NORTHEAST.getValue());
        npcs.add(santa);

        if (hintTask.isCancelled()) {
            hintTask = new SendHintsTask();
        }
        WorldTasksManager.schedule(hintTask, Settings.TEST_SERVER_MODE ? 25 : 100, HINT_FREQUENCY);
    }

    @Override
    public void asyncStart() throws Exception {
        JsonSerializable.load(READ_INTRODUCTION_PATH, HashSet.class, readIntroduction::addAll);
        JsonSerializable.load(TREE_PRESENT_PATH, HashSet.class, treePresents::addAll);
        JsonSerializable.load(RETURNED_PRESENTS_PATH, ArrayList.class, returnedPresents::addAll);
        JsonSerializable.load(STOLEN_PRESENTS_PATH, ArrayList.class, stolenPresents::addAll);
    }

    @Override
    protected void end() {
        hintTask.stop();
    }

    @Override
    public void asyncEnd() throws Exception {
        readIntroduction.clear();
        treePresents.clear();
        returnedPresents.clear();
        stolenPresents.clear();
        Files.deleteIfExists(READ_INTRODUCTION_PATH);
        Files.deleteIfExists(TREE_PRESENT_PATH);
        Files.deleteIfExists(RETURNED_PRESENTS_PATH);
        Files.deleteIfExists(STOLEN_PRESENTS_PATH);
    }

    @Override
    protected SeasonalEventPeriod computePeriod() {
        return new SeasonalEventPeriod(Month.DECEMBER, 1, Month.DECEMBER, 31);
    }
}
