package com.rs.game.player.actions.divination.impl;

import com.rs.game.Animation;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.activites.quest.deathsbounty.DeathsBounty;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.actions.divination.DivineObject;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles Divine location Hunting.
 *
 * @author Noel
 */
public class DivineHunting extends Action {

    private final DivineHuntingSpots spot;
    private final WorldObject spott;
    private final WorldTile tile;
    private int itemId;

    public DivineHunting(DivineHuntingSpots spot, int x, int y, int plane, WorldObject spott) {
        this.spot = spot;
        this.spott = spott;
        tile = new WorldTile(x, y, plane);
    }

    @Override
    public boolean start(Player player) {
        if (!checkAll(player))
            return false;
        itemId = getRandomItem(player);
        return true;
    }

    @Override
    public boolean process(Player player) {
        player.setNextAnimation(spot.getAnimation());
        player.faceObject(spott);
        if (!DivineObject.canHarvest(player)) {
            player.setNextAnimation(new Animation(-1));
            return false;
        }
        return checkAll(player);
    }

    private int getRandomItem(Player player) {
        int random = Utils.random(spot.getRewards().length);
        int difference = player.getSkills().getLevel(Skills.HUNTER) - spot.getRewards()[random].getLevel();
        if (difference < 0)
            return random = 0;
        if (random < -1)
            return random = 0;
        return random;
    }

    @Override
    public int processWithDelay(Player player) {
        addItem(player);
        if (player.getDailyManager().getTask() != null) {
            if (spot.getRewards()[itemId].getId() == player.getTaskItemId()) {
                player.getDailyManager().processTask();
            }
        }
        return Utils.random(1, 3);
    }

    private void addItem(Player player) {
        itemId = getRandomItem(player);
        Item hItem = new Item(spot.getRewards()[itemId].getId(), 1);
        int roll = Utils.random(100);
        Player owner = spott.getOwner();
        String name = hItem.getDefinitions().getName().toLowerCase();
        player.getInventory().addItem(hItem);
        player.addCreaturesCaught();
        player.sendMessage(getMessage(player, hItem), true);
        double totalXp = spot.getRewards()[itemId].getXp();
        player.getSkills().addXp(Skills.HUNTER, totalXp);
        DivineObject.handleHarvest(player);
        if (roll >= 75 && owner != null && player != owner && DivineObject.checkPercentage(owner) < 100) {
            if (Utils.random(100) >= 75)
                owner.gathered++;
            if (!hItem.getDefinitions().isStackable())
                hItem.setId(hItem.getDefinitions().getCertId());
            owner.getInventory().addItem(hItem);
            owner.sendMessage(player.getDisplayName() + " caught a " + name + " for you.", true);
        }
        DeathsBounty.dropUrn(player);
    }

    private String getMessage(Player player, Item hunt) {
        return "You manage to catch a " + hunt.getDefinitions().getName().toLowerCase() + "; " + "creatures caught: "
                + Colors.RED + Utils.getFormattedNumber(player.getCreaturesCaught()) + "</col>.";
    }

    private boolean checkAll(Player player) {
        Player owner = spott.getOwner();
        if (owner == null)
            owner = player;
        if (!DivineObject.canUseDivineObj(player, owner)) {
            return false;
        }
        if (player.getSkills().getLevel(Skills.HUNTER) < spot.getRewards()[itemId].getLevel()) {
            player.setNextAnimation(new Animation(-1));
            player.sendMessage("You need a Hunter level of at least " + spot.getRewards()[itemId].getLevel()
                    + " to use this Divine location.");
            return false;
        }
        if (!player.getInventory().hasFreeSlots()) {
            player.setNextAnimation(new Animation(-1));
            player.sendMessage("Inventory full. To make room, sell, drop or bank something.", true);
            return false;
        }
        if (!World.containsObjectWithId(tile, spot.getId())) {
            player.setNextAnimation(new Animation(-1));
            return false;
        }
        return true;
    }

    @Override
    public void stop(final Player player) {
        setActionDelay(player, 3);
    }

    public enum Rewards {

        polar_kebbit_fur(10117, 1, 60),

        raw_beast_meat(9986, 1, 60),

        common_kebbit_fur(10121, 3, 74),

        feldip_weasel_fur(10119, 7, 94),

        desert_devil_fur(10123, 13, 132),

        long_kebbit_spike(10107, 49, 300),

        red_feathers(10088, 1, 68),

        raw_bird_meat(9978, 1, 68),

        yellow_feathers(10090, 5, 94),

        orange_feathers(10091, 9, 122),

        blue_feathers(10089, 11, 130),

        stripy_feathers(10087, 19, 180),

        wimpy_feathers(11525, 39, 251),

        kebbit_claws(10113, 23, 148),

        barb_tail_harpoon(10129, 33, 168),

        kebbit_spike(10105, 37, 202),

        diseased_kebbit_fur(12567, 44, 200),

        kebbit_teeth(12567, 51, 300),

        Chinchompas(10033, 53, 350),

        red_chinchompas(10034, 63, 400),

        raw_pawya_meat(12535, 66, 500),

        grenwall_spikes(12539, 77, 800),;

        private final int id, level;
        private final double xp;

        Rewards(int id, int level, double xp) {
            this.id = id;
            this.level = level;
            this.xp = xp;
        }

        public int getId() {
            return id;
        }

        public int getLevel() {
            return level;
        }

        public double getXp() {
            return xp;
        }
    }

    public enum DivineHuntingSpots {

        DIVINE_KEBBIT_BURROW(87270, 1, new Animation(21236), Rewards.polar_kebbit_fur, Rewards.raw_beast_meat,
                Rewards.common_kebbit_fur, Rewards.feldip_weasel_fur, Rewards.desert_devil_fur,
                Rewards.long_kebbit_spike),

        DIVINE_BIRD_SNARE(87271, 1, new Animation(21236), Rewards.red_feathers, Rewards.raw_bird_meat,
                Rewards.yellow_feathers, Rewards.orange_feathers, Rewards.blue_feathers, Rewards.stripy_feathers,
                Rewards.wimpy_feathers),

        DIVINE_DEADFALL_TRAP(87272, 1, new Animation(21236), Rewards.kebbit_claws, Rewards.barb_tail_harpoon,
                Rewards.kebbit_spike, Rewards.diseased_kebbit_fur, Rewards.kebbit_teeth),

        DIVINE_BOX_TRAP(87273, 1, new Animation(21236), Rewards.Chinchompas, Rewards.red_chinchompas,
                Rewards.raw_pawya_meat, Rewards.grenwall_spikes);

        static final Map<Integer, DivineHuntingSpots> spot = new HashMap<Integer, DivineHuntingSpots>();

        static {
            for (DivineHuntingSpots spots : DivineHuntingSpots.values())
                spot.put(spots.id | spots.option << 24, spots);
        }

        private final Rewards[] rewards;
        private final int id, option;
        private final Animation animation;

        DivineHuntingSpots(int id, int option, Animation animation, Rewards... rewards) {
            this.id = id;
            this.animation = animation;
            this.rewards = rewards;
            this.option = option;
        }

        public static DivineHuntingSpots forId(int id) {
            return spot.get(id);
        }

        public Rewards[] getRewards() {
            return rewards;
        }

        public int getId() {
            return id;
        }

        public int getOption() {
            return option;
        }

        public Animation getAnimation() {
            return animation;
        }
    }
}