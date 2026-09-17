package com.rs.game.player.actions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.CookingRandomEvent;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.Pots;
import com.rs.game.player.content.achievementsystem.AchievementList;
import com.rs.game.player.content.packs.portable.PortableStation;
import com.rs.game.player.content.petperks.PetPerkHandler;
import com.rs.game.player.content.skillingcontracts.impl.CookingContractList;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.player.dialogue.impl.CookingD;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import com.rs.cache.loaders.ItemDefinitions;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Cooking extends Action {
    private com.rs.game.player.client.Native950Cooking.Journey nativeCooking;

    private final Item item;
    private final WorldObject object;
    private final boolean portable;
    private int quantity;

    private Cookables cook;
    private final Animation FIRE_COOKING = new Animation(897);
    private final Animation RANGE_COOKING = new Animation(897);

    public Cooking(WorldObject object, Item item, boolean portable) {
        this(object, item, portable, Integer.MAX_VALUE);
    }

    public Cooking(WorldObject object, Item item, boolean portable, int quantity) {
        this.item = item;
        this.object = object;
        this.portable = portable;
        this.quantity = quantity;
    }

    public static boolean useCook(Player player, WorldObject object) {
        Set<Cookables> rawSet = EnumSet.noneOf(Cookables.class);
        Cookables lastRaw = null;
        for (Item item : player.getInventory().getItemArray()) {
            if (item == null) {
                continue;
            }
            Cookables raw = Cookables.RAW.get(item.getId());
            if (raw != null) {
                rawSet.add(raw);
                lastRaw = raw;
            }
        }

        if (rawSet.isEmpty()) {
            player.sendMessage("You do not have any raw food to add to this fire.");
            return false;
        }

        if (CookingRs3Dialogue.sendCookingInterface(player, object, rawSet.size() == 1 ? lastRaw : null))
            return true;

        if (rawSet.size() == 1 && lastRaw != null) {
            player.getActionManager().setAction(new Cooking(object, lastRaw.getRawItem(), PortableStation.isPortableObject(object)));
            return true;
        } else {
            player.getDialogueManager().startDialogue("CookingD", Iterables.toArray(rawSet, Cookables.class), object);
            return true;
        }
    }

    private static boolean hasSpaceFor(Player player, int resultId, int amount) {
        ItemDefinitions defs = ItemDefinitions.getItemDefinitions(resultId);
        if (defs.isStackable()) {
            // if a stack exists, no new slot needed; otherwise need 1 free slot
            return player.getInventory().containsItem(resultId, 1)
                    || player.getInventory().getFreeSlots() >= 1;
        } else {
            // unstackables always need at least one free slot
            return player.getInventory().getFreeSlots() >= 1;
        }
    }


    public double increasedExperience(Player player, double totalXp) {
        if (Wilderness.isAtWild(player) && player.getEquipment().getGlovesId() == 13857)
            totalXp *= 1.1;
        if (player.getEquipment().getGlovesId() == 775)
            totalXp *= 1.01;
        if (player.getPerkManager().hasPerkActive(DonationPerk.CONVINCING_COOK))
            totalXp *= 1.15;
        if (portable)
            totalXp *= 1.21;
        return totalXp;
    }

    public static Cookables isCookingSkill(Item item) {
        return Cookables.forId(item.getId());
    }

    /**
     * XP modifier by wearing items.
     *
     * @param player The player.
     * @return the XP modifier.
     */
    public static double chefsSuit(Player player) {
        double xpBoost = 1.0;
        if (player.getEquipment().getHatId() == 25180)
            xpBoost *= 1.01;
        if (player.getEquipment().getChestId() == 25181)
            xpBoost *= 1.01;
        if (player.getEquipment().getLegsId() == 25182)
            xpBoost *= 1.01;
        if (player.getEquipment().getBootsId() == 25183)
            xpBoost *= 1.01;
        if (player.getEquipment().getGlovesId() == 25184)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 34924)
            xpBoost *= 1.03;
        if (player.getEquipment().getHatId() == 34924 && player.getEquipment().getChestId() == 25181 && player.getEquipment().getLegsId() == 25182 && player.getEquipment().getBootsId() == 25183 && player.getEquipment().getGlovesId() == 25184)
            xpBoost *= 1.03;
        if (player.getEquipment().getHatId() == 25180 && player.getEquipment().getChestId() == 25181 && player.getEquipment().getLegsId() == 25182 && player.getEquipment().getBootsId() == 25183 && player.getEquipment().getGlovesId() == 25184)
            xpBoost *= 1.01;
        return xpBoost;
    }

    private boolean isBurned(Cookables cook, Player player) {
        int level = player.getSkills().getLevel(Skills.COOKING);
        if (player.getPerkManager().hasPerkActive(DonationPerk.CONVINCING_COOK))
            return false;
        boolean hasCookingGauntlets = player.getEquipment().getGlovesId() == 775;
        double burn_chance = (50.0);
        if (this.object != null && object.getDefinitions().getName().contains("fire"))
            burn_chance += 5;
        int burnLevel = cook.getBurningLvl();
        if (hasCookingGauntlets)
            burnLevel -= 5;
        if (level >= burnLevel)
            return false;
        double lev_needed = cook.getLvl();
        double burn_stop = burnLevel;
        double multi_a = (burn_stop - lev_needed);
        double burn_dec = (burn_chance / multi_a);
        double multi_b = (level - lev_needed);
        burn_chance -= (multi_b * burn_dec);
        return burn_chance > (Math.random() * 100.00);
    }

    public static void performPortableAction(Player player, WorldObject object) {
        performPortableAction(player, object, null);
    }

    public static void performPortableAction(Player player, WorldObject object, Cookables preferredCookable) {
        ArrayList<Cookables> possiblities = new ArrayList<Cookables>();
        for (Cookables cookable : Cookables.values())
            if (player.getInventory().containsItem(cookable.getRawItem().getId(), 1))
                possiblities.add(cookable);
        Cookables[] cookables = possiblities.toArray(new Cookables[possiblities.size()]);
        if (cookables.length == 0)
            player.sendMessage("You do not have anything to cook on the range.");
        else if (CookingRs3Dialogue.sendCookingInterface(player, object,
                preferredCookable != null && possiblities.contains(preferredCookable)
                        ? preferredCookable : cookables.length == 1 ? cookables[0] : null))
            return;
        else if (cookables.length == 1)
            player.getActionManager().setAction(new Cooking(object, cookables[0].getRawItem(), PortableStation.isPortableObject(object)));
        else
            player.getDialogueManager().startDialogue(CookingD.class.getSimpleName(), cookables, object);
    }



    private int getCookingTickDelay(Player player) {
// Use current (boosted) level:
        int level = player.getSkills().getLevel(Skills.COOKING);


// If you want *real* level (ignores temporary boosts), use this instead:
// int level = player.getSkills().getLevelForXp(Skills.COOKING);


        int req = cook.getLvl(); // required cooking level for this Cookable


        int above = Math.max(0, level - req);


// Every 4 levels above requirement reduces delay by 1 tick:
// 0..3 above => 3 ticks
// 4..7 above => 2 ticks
// 8+ above => 1 tick
        int delay = 3 - (above / 4);


// Clamp to 1..3 ticks (optional but matches your example)
        if (delay < 1) delay = 1;
        if (delay > 3) delay = 3;


        return delay;
    }


    @Override
    public boolean process(Player player) {
        if(player.isNative950())return nativeCooking!=null&&nativeCooking.process(player);
        if (quantity <= 0)
            return false;
        if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsInventoryInter()) {
            player.sendMessage("Please finish what you're doing before doing this action.");
            return false;
        }
        if (!World.containsObjectWithId(object, object.getId()))
            return false;
        if (player.clickedObject != null) {
            if (!World.containsObjectWithId(player.clickedObject, player.clickedObject.getId()))
                return false;
        }
        if (!player.getInventory().containsItem(item.getId(), 1)) {
            return false;
        }
        if (!player.getInventory().containsItem(cook.getRawItem().getId(), 1)) {
            return false;
        }
        if (cook.getRawItem().getId() == 15272) {
            if (player.getPorts().spice == 0) {
                player.sendMessage("You do not have enough spices to cook a rocktail soup.");
                return false;
            }
        }
        if (player.getSkills().getLevel(Skills.COOKING) < cook.getLvl()) {
            player.sendMessage("You need a Cooking level of " + cook.getLvl() + " to cook this.");
            return false;
        }
        if (Utils.random(350) == 0 && player.hasRandomEvent()) {
            if (!player.followedByRandomEventNPC()) {
                NPC npc = new CookingRandomEvent(player, player);
                if (npc.withinDistance(player, 14)) {
                    player.setCurrentRandomEventNPC(npc);
                    player.sendMessage("<col=ff0000>A Mysterious Cook appears from the " + object.getDefinitions().name + ".");
                }
            }
        }
        return true;
    }

    @Override
    public int processWithDelay(Player player) {
        if(player.isNative950())return nativeCooking==null?-1:nativeCooking.cook(player);
        // Work out the outcome for THIS tick first (so we can space-check correctly)
        final boolean burned = isBurned(cook, player);

        int resultId   = burned ? cook.getBurntId().getId()      : cook.getProduct().getId();
        int resultAmt  = burned ? cook.getBurntId().getAmount()  : cook.getProduct().getAmount();

        // Juju -> gumbo transformation (decide it now so the space check is accurate)
        boolean gumbo = false;
        if (!burned
                && player.jujuPotions.isActive(Pots.Effects.COOKING_JUJU)
                && cook == Cookables.ROCKTAIL_SOUP
                && ThreadLocalRandom.current().nextInt(20) < 3) {
            resultId = 19949; // gumbo
            gumbo = true;
            // resultAmt stays the same
        }

        // Inventory space check (important when raws are stackable)
        if (!hasSpaceFor(player, resultId, resultAmt)) {
            player.sendMessage("You need more free inventory space to continue cooking.");
            return -1; // stop the action
        }

        // Play the animation only after we know we can actually add the item
        if (player.getAnimations().hasSamuraiCook && player.getAnimations().samuraiCook) {
            player.setNextAnimation(new Animation(17314));
            player.setNextGraphics(new Graphics(3306));
        } else if (player.getAnimations().hasArcaneCook && player.getAnimations().arcaneCook) {
            player.setNextAnimation(new Animation(20298));
            player.setNextGraphics(new Graphics(3999));
        } else {
            player.setNextAnimation(object.getDefinitions().name.contains("fire") ? FIRE_COOKING : RANGE_COOKING);
        }
        player.faceObject(object);

        if (burned) {
            // burn path
            player.getInventory().deleteItem(item.getId(), 1);
            player.getInventory().addItem(cook.getBurntId().getId(), cook.getBurntId().getAmount());
            player.sendMessage("Oops! You accidentally burnt the " + cook.getProduct().getDefinitions().getName().toLowerCase() + ".", true);
        } else {
            // success path
            boolean effPerkProc = PetPerkHandler.handleEfficiencyExpert(player, new Item(item.getId(), 1));
            if (!effPerkProc) {
                // raws are stackable in your setup, so this does NOT free a slot
                player.getInventory().deleteItem(item.getId(), 1);
            }

            int addItem = resultId; // either normal cooked item or gumbo
            if (effPerkProc)
                player.addItem(addItem, resultAmt);
            else
                player.getInventory().addItem(addItem, resultAmt);

            if (portable && Utils.random(9) == 4) {
                player.getBank().addItem(new Item(addItem, resultAmt), true);
                player.sendMessage(Colors.GOLD + "<shad=000000>You make two portions of food thanks to this fabulous range! The extra portion has been sent to your bank.", true);
            }

            CookingContractList.listen(player, cook);

            double xp = cook.getXp();
            if (gumbo) xp *= 3; // gumbo XP bonus
            double totalXp = increasedExperience(player, xp) * 0.4; // your nerf after boosts
            player.getSkills().addXp(Skills.COOKING, totalXp);

            player.addFoodCooked();
            if (player.getDailyManager().getTask() != null) {
                if (cook.getProduct().getId() == player.getTaskItemId()) {
                    player.getDailyManager().processTask();
                }
            }

            player.sendMessage("You successfully cook the " + cook.getProductName() + "; " +
                    "items cooked: " + Colors.RED + Utils.getFormattedNumber(player.getFoodCooked()) + "</col>.", true);

            if (cook.getProduct().getId() == 7218)
                player.getAchievements().updateProgress(1, AchievementList.COOK_10_SUMMER_PIES);

            ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);

            if (cook == Cookables.ROCKTAIL_SOUP)
                player.getPorts().spice -= 1;
        }

        quantity--;
        if (quantity <= 0)
            return -1;

        if (player.getInventory().containsItem(cook.getRawItem().getId(), 1))
            player.sendMessage("You attempt to cook the " + cook.getProductName() + "...", true);

        return getCookingTickDelay(player);
    }


    @Override
    public boolean start(Player player) {
        if(player.isNative950()){nativeCooking=new com.rs.game.player.client.Native950Cooking.Journey(object,item.getId(),quantity);return nativeCooking.start(player);}
        if ((this.cook = Cookables.forId(item.getId())) == null)
            return false;
        if (cook.isFireOnly() && !object.getDefinitions().name.equals("Fire")) {
            player.sendMessage("You may only cook this on a fire.");
            return false;
        } else if (cook.isSpitRoast() && object.getId() != 11363) {
            player.sendMessage("You may only cook this on an iron spit.");
            return false;
        } else if (cook.isCookingStation() && object.getId() != 81736) {
            player.sendMessage("You may only cook this on a cooking station.");
            return false;
        } else if (item.getId() == 15272 && player.getPorts().spice == 0) {
            player.sendMessage("You do not have enough spices to cook a rocktail soup.");
            return false;
        } else if (player.getSkills().getLevel(Skills.COOKING) < cook.getLvl()) {
            player.sendMessage("You need a Cooking level of " + cook.getLvl() + " to cook this.");
            return false;
        }
        player.closeInterfaces();
        player.faceObject(object);
        player.sendMessage("You attempt to cook the " + cook.getProduct().getDefinitions().getName().toLowerCase() + "...", true);
        return true;
    }

    @Override
    public void stop(final Player player) {
        if(player.isNative950()){if(nativeCooking!=null)nativeCooking.stop(player);setActionDelay(player,3);return;}
        this.setActionDelay(player, player.getPerkManager().hasPerkActive(DonationPerk.CONVINCING_COOK) ? 2 : 3);
        player.clickedObject = null;
    }

    public enum Cookables {

        /**
         * Cooked Meat
         */
        RAW_MEAT(new Item(2132), 1, 34, 30, new Item(2146, 1), new Item(2142, 1), false, false, false),

        RAW_RAT_MEAT(new Item(2134, 1), 1, 34, 30, new Item(2146, 1), new Item(2142, 1), false, false, false),

        RAW_BEAR_MEAT(new Item(2136, 1), 1, 34, 30, new Item(2146, 1), new Item(2142, 1), false, false, false),

        /**
         * Cooked Chicken
         */
        RAW_CHICKEN(new Item(2138), 1, 34, 30, new Item(2144, 1), new Item(2140, 1), false, false, false),

        /**
         * Ugthanki Meat
         */
        RAW_UGTHANKI_MEAT(new Item(1859), 1, 120, 30, new Item(2146, 1), new Item(1861, 1), false, false, false),

        /**
         * Raw Rabbit
         */
        RAW_RABBIT(new Item(3226), 1, 34, 30, new Item(7222, 1), new Item(3228, 1), false, true, false),

        /**
         * Bird Meat
         */
        RAW_BIRD_MEAT(new Item(9978), 11, 99, 62, new Item(9982, 1), new Item(9980, 1), true, false, false),

        /**
         * Crab meat
         */
        RAW_CRAB_MEAT(new Item(7518), 21, 62, 100, new Item(7520, 1), new Item(7521, 1), false, false, false),

        /**
         * Beast meat
         */
        RAW_BEAST_MEAT(new Item(9986), 21, 99, 82, new Item(9990, 1), new Item(9988, 1), true, false, false),

        /**
         * Chompy
         */
        RAW_CHOMPY(new Item(2876), 30, 99, 140, new Item(2880, 1), new Item(2878, 1), false, true, false),

        /**
         * Jubbly
         */
        RAW_JUBBLY(new Item(7566), 41, 49, 160, new Item(7570, 1), new Item(7568, 1), true, false, false),

        /**
         * CrayFish
         */
        RAW_CRAYFISH(new Item(13435), 1, 34, 30, new Item(13437, 1), new Item(13433, 1), false, false, false),

        /**
         * Shrimp
         */
        RAW_SHRIMP(new Item(317), 1, 34, 30, new Item(7954, 1), new Item(315, 1), false, false, false),

        /**
         * Karambwanji
         */
        RAW_KARAMBWANJI(new Item(3150), 1, 28, 10, new Item(3148, 1), new Item(3151, 1), false, false, false),

        RAW_KARAMBWAN(new Item(3142), 1, 120, 190, new Item(3148, 1), new Item(3144, 1), false, false, false),
        /**
         * Sardine
         */
        RAW_SARDINE(new Item(327), 1, 38, 40, new Item(323, 1), new Item(325, 1), false, false, false),

        /**
         * Anchovies
         */
        RAW_ANCHOVIES(new Item(321), 1, 34, 30, new Item(323, 1), new Item(319, 1), false, false, false),

        /**
         * Karambwan
         */
        POISON_KARAMBWAN(new Item(3146), 1, 20, 80, new Item(3148, 1), new Item(3144, 1), false, false, false),

        /**
         * Herring
         */
        RAW_HERRING(new Item(345), 5, 41, 50, new Item(357, 1), new Item(347, 1), false, false, false),

        /**
         * Mackerel
         */
        RAW_MACKEREL(new Item(353), 10, 45, 60, new Item(357, 1), new Item(355, 1), false, false, false),

        /**
         * Trout
         */
        RAW_TROUT(new Item(335), 15, 50, 70, new Item(343, 1), new Item(333, 1), false, false, false),

        /**
         * Cod
         */
        RAW_COD(new Item(341), 18, 52, 75, new Item(343, 1), new Item(339, 1), false, false, false),

        /**
         * Pike
         */
        RAW_PIKE(new Item(349), 20, 53, 80, new Item(343, 1), new Item(351, 1), false, false, false),

        /**
         * Salmon
         */
        RAW_SALMON(new Item(331), 25, 58, 90, new Item(343, 1), new Item(329, 1), false, false, false),

        /**
         * Slimy Eel
         */
        RAW_SLIMY_EEL(new Item(3379), 28, 58, 95, new Item(3383, 1), new Item(3381, 1), false, false, false),

        /**
         * Tuna
         */
        RAW_TUNA(new Item(359), 30, 65, 100, new Item(367, 1), new Item(361, 1), false, false, false),

        /**
         * Rainbow Fish
         */
        RAW_RAINBOW_FISH(new Item(10138), 35, 60, 110, new Item(10140, 1), new Item(10136, 1), false, false, false),

        /**
         * Cave eel
         */
        RAW_CAVE_EEL(new Item(5001), 38, 40, 115, new Item(5006, 1), new Item(5003, 1), false, false, false),

        /**
         * Lobster
         */
        RAW_LOBSTER(new Item(377), 40, 64, 120, new Item(381, 1), new Item(379, 1), false, false, false),

        /**
         * Bass
         */
        RAW_BASS(new Item(363, 1), 43, 79, 130, new Item(367, 1), new Item(365, 1), false, false, false),

        /**
         * SwordFish
         */
        RAW_SWORDFISH(new Item(371, 1), 45, 81, 140, new Item(375, 1), new Item(373, 1), false, false, false),

        /**
         * Lava Eel
         */
        RAW_LAVA_EEL(new Item(2148, 1), 53, 53, 30, new Item(-1, 1), new Item(2149, 1), false, false, false),

        /**
         * MonkFish
         */
        RAW_MONKFISH(new Item(7944, 1), 62, 90, 150, new Item(7948, 1), new Item(7946, 1), false, false, false),

        /**
         * Shark
         */
        RAW_SHARK(new Item(383, 1), 80, 94, 210, new Item(387, 1), new Item(385, 1), false, false, false),

        BARON_SHARK(new Item(19947, 1), 80, 94, 210, new Item(19948, 1), new Item(385, 1), false, false, false),

        /**
         * Shark
         */
        GREAT_WHITE_RAW_SHARK(new Item(34727), 84, 94, 500, new Item(34731, 1), new Item(34729, 1), false, false, false),

        /**
         * Sea turtle
         */
        RAW_SEA_TURTLE(new Item(395, 1), 82, 120, 212, new Item(399, 1), new Item(397, 1), false, false, false),

        /**
         * Cavefish
         */
        RAW_CAVEFISH(new Item(15264, 1), 88, 94, 214, new Item(15268, 1), new Item(15266, 1), false, false, false),

        /**
         * Manta Ray
         */
        RAW_MANTA_RAY(new Item(389, 1), 91, 120, 216, new Item(393, 1), new Item(391, 1), false, false, false),

        /**
         * RockTail
         */
        RAW_ROCKTAIL(new Item(15270, 1), 92, 94, 225, new Item(15274, 1), new Item(15272, 1), false, false, false),

        /**
         * RedBerry Pie
         */
        RAW_REDBERRY_PIE(new Item(2321, 1), 10, 100, 78, new Item(2329, 1), new Item(2325, 1), false, false, false),

        /**
         * Meat pie
         */
        RAW_MEAT_PIE(new Item(2319, 1), 20, 100, 110, new Item(2329, 1), new Item(2327, 1), false, false, false),
        /**
         * Mud pie
         */
        RAW_MUD_PIE(new Item(7168, 1), 29, 100, 128, new Item(2329, 1), new Item(7170, 1), false, false, false),

        /**
         * Apple pie
         */
        RAW_APPLE_PIE(new Item(2317, 1), 30, 100, 130, new Item(2329, 1), new Item(2323, 1), false, false, false),

        /**
         * Garden Pie
         */
        RAW_GARDEN_PIE(new Item(7176, 1), 34, 100, 138, new Item(2329, 1), new Item(7178, 1), false, false, false),

        /**
         * Fish Pie
         */
        RAW_FISH_PIE(new Item(7186, 1), 47, 100, 164, new Item(2329, 1), new Item(7188, 1), false, false, false),

        /**
         * Admiral Pie
         */
        RAW_ADMIRAL_PIE(new Item(7196, 1), 70, 100, 210, new Item(2329, 1), new Item(7198, 1), false, false, false),

        /**
         * Wild Pie
         */
        RAW_WILD_PIE(new Item(7206, 1), 85, 100, 240, new Item(2329, 1), new Item(7208, 1), false, false, false),

        /**
         * Summer pie
         */
        RAW_SUMMER_PIE(new Item(7216, 1), 95, 100, 260, new Item(2329, 1), new Item(7218, 1), false, false, false),

        /**
         * Fish Cake
         */
        RAW_FISHCAKE(new Item(7529, 1), 31, 100, 100, new Item(7531, 1), new Item(7530, 1), false, false, false),

        /**
         * Potato
         */
        RAW_POTATO(new Item(1942, 1), 7, 120, 15, new Item(6699, 1), new Item(6701, 1), false, false, false),

        ROCKTAIL_SOUP(new Item(15272, 1), 93, 1, 500, new Item(-1, 1), new Item(26313, 1), false, false, true);

        public static final ImmutableMap<Integer, Cookables> RAW;
        public static final ImmutableList<Cookables> VALUES = ImmutableList.copyOf(values());

        static {
            Map<Integer, Cookables> raw = new HashMap<>();
            for (Cookables ingredient : Cookables.values()) {
                raw.put(ingredient.getRawItem().getId(), ingredient);
            }
            RAW = ImmutableMap.copyOf(raw);
        }

        private final Item raw;
        private final int lvl;
        private final int burningLvl;
        private final int xp;
        private final Item burnt;
        private final Item total;
        private final boolean spitRoast;
        private final boolean fireOnly;
        private final boolean cookingStation;

        Cookables(Item raw, int lvl, int burningLvl, int exp, Item burnt, Item total, boolean spitRoast, boolean fireOnly, boolean cookingStationH) {
            this.raw = raw;
            this.lvl = lvl;
            this.burningLvl = burningLvl;
            this.xp = exp;
            this.burnt = burnt;
            this.total = total;
            this.spitRoast = spitRoast;
            this.fireOnly = fireOnly;
            this.cookingStation = cookingStationH;
        }

        public static Cookables forId(int itemId) {
            return RAW.get(itemId);
        }

        public int getBurningLvl() {
            return burningLvl;
        }

        public Item getBurntId() {
            return burnt;
        }

        public int getLvl() {
            return lvl;
        }

        public Item getProduct() {
            return total;
        }

        public Item getRawItem() {
            return raw;
        }

        public Item getTotal() {
            return total;
        }

        public int getXp() {
            return xp;
        }

        public boolean isFireOnly() {
            return fireOnly;
        }

        public boolean isSpitRoast() {
            return spitRoast;
        }

        public boolean isCookingStation() {
            return cookingStation;
        }

        private String lcName;

        public String getProductName() {
            if (lcName == null) {
                lcName = getProduct().getDefinitions().getName().toLowerCase();
            }
            return lcName;
        }
    }
}
