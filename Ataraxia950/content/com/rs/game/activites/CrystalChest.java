package com.rs.game.activites;

import com.rs.game.Animation;
import com.rs.game.WorldObject;
import com.rs.game.item.Item;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;


/**
 * A class used to handle the Crystal Chest.
 *
 * @author Noel
 */
public class CrystalChest {

    /**
     * An Array holding all the keyparts.
     */
    public static Item[] KEYPARTS = {new Item(985), new Item(987)};

    /**
     * Item holding the Crystal Key.
     */
    private static final Item KEY = new Item(989);

    /**
     * Animation, the Chest Animation.
     */
    private static final Animation CHEST_EMOTE = new Animation(536);

    /**
     * Int[] the Sound ID.
     */
    private static final int[] soundId = {52, 0, 1};

    /**
     * Item array holding all the rewards.
     */
    private static Item[] rewards;

    /**
     * Item rewards.
     */
    private static final Item[] COMMON = {new Item(386, Utils.random(100, 250)), new Item(1128, 4), //Shark, Rune platebodies
            new Item(560, Utils.random(100, 1000)), new Item(565, Utils.random(100, 1000)), //Death Rune, Blood Rune
            new Item(566, Utils.random(100, 1000)), new Item(557, Utils.random(500, 3000)), // Soul Rune, earth rune
            new Item(555, Utils.random(500, 3000)), new Item(554, Utils.random(500, 3000)), new Item(995, Utils.random(100000, 1000000)), //water rune, fire rune, coins
            new Item(556, Utils.random(500, 3000)), new Item(8789, Utils.random(2, 5)), //air rune, magic stone
            new Item(448, Utils.random(25, 150)), new Item(450, Utils.random(15, 125)), //mith ore, addy ore
            new Item(452, Utils.random(8, 75)), new Item(1518, Utils.random(300, 700)), //rune ore, maple log
            new Item(1514, Utils.random(15, 100)), new Item(1516, Utils.random(50, 300)), //magic log, yew log
            new Item(4087), new Item(4585), new Item(4587)}; //dragon plateskirt/legs, dscim

    private static final Item[] UNCOMMON = {new Item(4588, 5), new Item(2455, Utils.random(5, 30)), //dragon scimitar, antifire
            new Item(1128, 10), new Item(5699, 5), new Item(537, Utils.random(20, 80)), new Item(3052, Utils.random(10, 100)),   //rune plate, dds, dragon bones, grimy snap
            new Item(995, Utils.random(100000, 2500000)), new Item(1094, 5), new Item(6524, 1), // coins, rune plateleg, obby shield
            new Item(4092, 5), new Item(26779, Utils.random(5, 20)), // mystic top, phoenix feather
            new Item(4094, 5), new Item(6288, Utils.random(30, 100)), new Item(4090, 5), //mystic bottoms, snakehide, mystic hat
            new Item(2368), new Item(2366), new Item(1746, Utils.random(50, 100)), new Item(2506, Utils.random(40, 100)), //shield right/left, green leather, blue leather
            new Item(2508, Utils.random(30, 100)), new Item(2510, Utils.random(20, 90)), new Item(24375, Utils.random(10, 80)), //red leather, black leather, royal leather
            new Item(8783, Utils.random(50, 200)), new Item(21627, Utils.random(10, 100)), new Item(21623, Utils.random(10, 100)),}; //mahog plank, grimy fellstalk, morchella

    private static final Item[] RARE = {new Item(30825, 1), new Item(30828), new Item(12159, Utils.random(5, 50)), //abby wand/orb, green charm
            new Item(18831, Utils.random(20, 50)), new Item(1188, 20), new Item(1150, 20), new Item(12160, Utils.random(5, 50)), //frost dbones, dshield, dhelm, crim charm
            new Item(1514, Utils.random(100, 350)), new Item(12158, Utils.random(5, 50)), new Item(12163, Utils.random(5, 50)), //magic logs, gold/blue charm
            new Item(995, Utils.random(100000, 5000000)), new Item(4151), new Item(20072), // coins, whip, ddef
            new Item(26636, Utils.random(5, 20)), new Item(35886), new Item(20269, Utils.random(50, 150)), new Item(34160, Utils.random(25, 100)), //overload, supply cache, searing/infernal
            new Item(2639, 1), new Item(2641, 1), new Item(2643, 1), new Item(11280, 1), new Item(19346, 1), new Item(19348, 1), new Item(19350, 1), //cavaliers, dfh orn
            new Item(19352, 1), new Item(19354, 1), new Item(19356, 1), new Item(19358, 1), new Item(19360, 1), //ornament kits
            new Item(25312, 1), new Item(25314, 1), new Item(24352, 1), //ornament kits
            new Item(11894, 1), new Item(11898, 1), new Item(11926, 1), new Item(11928, 1), new Item(11930, 1)};

    private static final Item[] VERY_RARE = {new Item(995, Utils.random(500000, 5000000)), new Item(32380), new Item(32383), new Item(32386), new Item(32389), //ghost hunter
            new Item(31392), new Item(31393), new Item(31394), new Item(31395), new Item(6739), new Item(15259), new Item(28537), new Item(28539), // various items
            new Item(28541), new Item(28543), new Item(28545), new Item(33502), new Item(33512), new Item(24431), new Item(34936), new Item(28145), //various items
            new Item(36204), new Item(36207), new Item(28547), new Item(28548), new Item(28549), new Item(37200)};


    /**
     * Handles the reward.
     *
     * @param player The player.
     */
    public static void addCrystalReward(Player player) {
        int commonRewards = Utils.random(50), uncommonRewards = Utils.random(100), rareRewards = Utils.random(200);
        if (uncommonRewards > 65 && rareRewards > 100)
            rewards = new Item[]{RARE[Utils.random(RARE.length)]};
        else if (commonRewards > 15 && uncommonRewards > 50)
            rewards = new Item[]{UNCOMMON[Utils.random(UNCOMMON.length)]};
        else if (commonRewards < 40 && uncommonRewards > 40)
            rewards = new Item[]{COMMON[Utils.random(COMMON.length)]};
        else
            rewards = new Item[]{COMMON[Utils.random(COMMON.length)]};
        if (Utils.random(300) > 275)
            rewards = new Item[]{VERY_RARE[Utils.random(VERY_RARE.length)]};
        for (Item item : rewards) {
            if (!item.getDefinitions().isNoted() && item.getAmount() > 1)
                if (item.getDefinitions().getCertId() != -1)
                    item.setId(item.getDefinitions().getCertId());
            if (item.getId() == 995) {
                player.addMoney(item.getAmount());
                return;
            }
            player.addItem(item);
        }
    }

    /**
     * Makes the Crystal Key
     *
     * @param player The Player.
     */

    public static void makeKey(Player player) {
        if (player.containsOneItem(985) && player.containsOneItem(987)) {
            player.getInventory().removeItems(KEYPARTS);
            player.getInventory().addItem(KEY);
            player.sendMessage("You bound the keyparts together and make a " + KEY.getName().toLowerCase() + ".");
        } else
            player.sendMessage("You'll need both key halves in order to assemble the key.");
    }

    /**
     * Opens the chest
     *
     * @param object The Chest.
     * @param player The Player.
     */
    public static void openChest(WorldObject object, final Player player) {
        int index = player.getInventory().getItemSlot(989);
        if (index >= 0 && !player.isLocked()) {
            player.getInventory().set(index, null);
            player.faceObject(object);
            player.lock(2);
            player.setNextAnimation(CHEST_EMOTE);
            player.sendMessage("You unlock the chest with your key..", true);
            player.getPackets().sendSound(soundId[0], soundId[1], soundId[2]);
            WorldTasksManager.schedule(new WorldTask() {
                @Override
                public void run() {
                    addCrystalReward(player);
                    player.incrementChestsOpened();
                    player.sendMessage("You find some treasure in the chest; chests opened: " + Colors.RED
                            + Utils.getFormattedNumber(player.getChestsOpened()) + "</col>.", true);
                    if (player.getPerkManager().hasPerkActive(DonationPerk.KEY_EXPERT))
                        addCrystalReward(player);
                    player.unlock();
                    this.stop();
                }
            }, 1);
        } else if (!player.getInventory().containsItem(989, 1))
            player.getDialogueManager().startDialogue("SimpleMessage", "You need a crystal key to open this chest.");
    }

    /**
     * Opens the interface of all obtainable rewards from the chest.
     *
     * @param player The player to send the interface to.
     */
    public static void openRewardsInterface(Player player) {
        player.getInterfaceManager().sendInterface(275);
        for (int i = 0; i < 310; i++)
            player.getPackets().sendIComponentText(275, i, "");
        player.getPackets().sendIComponentText(275, 1, "Crystal Chest rewards</u>");
        player.getPackets().sendIComponentText(275, 10, "Coins: x 50'000 - 200'000");
        int number = 0;
        for (Item reward : COMMON) {
            if (reward.getId() == 995)
                continue;
            String name = reward.getName() + (reward.getDefinitions().isNoted() ? " - noted" : "");
            player.getPackets().sendIComponentText(275, 11 + number, "" + name);
            number++;
        }
        for (Item reward : UNCOMMON) {
            if (reward.getId() == 995)
                continue;
            String name = reward.getName() + (reward.getDefinitions().isNoted() ? " - noted" : "");
            player.getPackets().sendIComponentText(275, number + 11, "" + name);
            number++;
        }
        for (Item reward : RARE) {
            if (reward.getId() == 995)
                continue;
            String name = reward.getName() + (reward.getDefinitions().isNoted() ? " - noted" : "");
            player.getPackets().sendIComponentText(275, number + 11, "" + name);
            number++;
        }
        for (Item reward : VERY_RARE) {
            if (reward.getId() == 995)
                continue;
            String name = reward.getName() + (reward.getDefinitions().isNoted() ? " - noted" : "");
            player.getPackets().sendIComponentText(275, number + 11, "" + name);
            number++;
        }
    }
}