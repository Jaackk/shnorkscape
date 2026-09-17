package com.rs.game.player.content;

import java.util.HashMap;
import java.util.Map;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.activites.creations.StealingCreation;
import com.rs.game.item.Item;
import com.rs.game.player.BuffDebuffTimersManager.Timer;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.content.dungeoneering.RingOfKinship;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Utils;

/**
 * @author Cjay0091
 */
public class Foods {

    public static boolean isConsumable(Item item) {
        Food food = Food.forId(item.getId());
        return food != null;
    }

    public static boolean eat(final Player player, final Player p2, Item item) {
        Food food = Food.forId(item.getId());
        if (food == null)
            return false;
        if (p2.getFoodDelay() > Utils.currentTimeMillis() && food.getId() != 3144) {
            player.sendMessage(p2.getDisplayName() + " is busy.");
            return true;
        }
        if (p2.getKaramDelay() > Utils.currentTimeMillis() && food.getId() == 3144)
            return true;
        if (!p2.getControlerManager().canEat(food))
            return true;
        String name = ItemDefinitions.getItemDefinitions(food.getId()).getName().toLowerCase();
        player.sendMessage("You hand the " + name + " to " + p2.getDisplayName() + ".");
        p2.getPackets().sendGameMessage("You eat the " + name + " " + player.getDisplayName() + " hands to you.", true);
        p2.setNextAnimationForce(new Animation(p2.isUnderCombat() ? 18002 : 18001));
        long foodDelay = name.contains("half") ? 800 : 1800;
        p2.getActionManager().setActionDelay((int) foodDelay / 1000);
        p2.addFoodDelay(100);
        p2.addKaramDelay(1800);
        p2.getActionManager().setActionDelay(p2.getActionManager().getActionDelay() + 3);
        player.getInventory().deleteItem(item.getId(), 1);
        int hp = p2.getHitpoints();
        int heal = food.getHeal(player);
        p2.applyHit(new Hit(null, heal + (int) ((float) heal * ((float) player.getRingOfKinship().getBoost(RingOfKinship.MEDIC) / 100)), HitLook.HEALED_DAMAGE));
        if (p2.getHitpoints() > hp)
            p2.getPackets().sendGameMessage("It heals some health.", true);
        if (Alcohol.canDrink(item)) {
            Alcohol.intake(player);
        }
        p2.getInventory().refresh();
        return true;
    }

    public static boolean eat(final Player player, Item item, int slot) {
        Food food = Food.forId(item.getId());
        if (food == null)
            return false;
        if (player.getFoodDelay() > Utils.currentTimeMillis() && food.getId() != 3144)
            return true;
        if (player.getKaramDelay() > Utils.currentTimeMillis() && food.getId() == 3144)
            return true;
        if (!player.getControlerManager().canEat(food))
            return true;
        if (!food.canEat(player))
            return true;
        String name = ItemDefinitions.getItemDefinitions(food.getId()).getName().toLowerCase();
        player.getPackets().sendGameMessage("You eat the " + name + ".", true);
        if (!player.isROTSLocked())
            player.setNextAnimationForce(new Animation(player.isShadow() ? 28499 : player.isUnderCombat() ? 18002 : 18001));
        long foodDelay = name.contains("half") ? 800 : 1800;
        player.getActionManager().setActionDelay((int) foodDelay / 1000);
        if (food.getId() != 3144) {// karambwans
            player.addFoodDelay(foodDelay);
        } else {
            player.addFoodDelay(100);
            player.addKaramDelay(1800);
        }
        player.getActionManager().setActionDelay(player.getActionManager().getActionDelay() + 3);
        if (food != Food.PURPLE_SWEETS) {
            player.getInventory().getItems().set(slot, food.getNewId() == 0 ? null : new Item(food.getNewId(), 1));
        } else {
            player.getInventory().deleteItem(item.getId(), 1);
        }
        player.getInventory().refresh(slot);
        int hp = player.getHitpoints();
        int heal = food.getHeal(player);
        Perk junkFood = player.getInventionManager().hasPerk(Perks.JUNK_FOOD);
        if (junkFood != null)
            heal -= heal * 0.03 * junkFood.getRank();
        if (food.ordinal() < Food.CRAYFISH.ordinal())
            player.applyHit(new Hit(null, heal, HitLook.HEALED_DAMAGE));
        else
            player.heal(heal * 10, food.getExtraHP() * 10);
        if (player.getHitpoints() > hp)
            player.getPackets().sendGameMessage("It heals some health.", true);
        if (Alcohol.canDrink(item)) {
            Alcohol.intake(player);
        }
        player.getInventory().refresh();
        if (food.effect != null) {
            food.effect.effect(player);
        }
        return true;
    }

    public enum Effect {
        JUJU_GUMBO {
            @Override
            public void effect(Player player) {
                if (player.getSkills().getLevelForXp(Skills.HITPOINTS) < 80) {
                    player.sendMessage("Your heart is not strong enough to receive the full healing of the juju gumbo.");
                    return;
                }
                if (player.jujuGumbo == 0) {
                    player.jujuGumbo = 10;
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            if (player.jujuGumbo < 0 || player.hasFinished()) {
                                stop();
                                player.jujuGumbo = 0;
                                return;
                            }
                            player.heal(20);
                            player.jujuGumbo--;
                        }
                    }, 2, 2);
                }
            }
        },
        BARON_SHARK {
            @Override
            public void effect(Player player) {
                if (player.getSkills().getLevelForXp(Skills.HITPOINTS) < 80) {
                    player.sendMessage("Your heart is not strong enough to receive the full healing of the baron shark.");
                    return;
                }
                if (player.baronShark == 0) {
                    player.baronShark = 10;
                    WorldTasksManager.schedule(new WorldTask() {
                        @Override
                        public void run() {
                            if (player.baronShark < 0 || player.hasFinished()) {
                                stop();
                                player.baronShark = 0;
                                return;
                            }
                            player.heal(20);
                            player.baronShark--;
                        }
                    }, 2, 2);
                }
            }
        },
        SUMMER_PIE {
            @Override
            public void effect(Player player) {

                int runEnergy = (int) (player.getRunEnergy() * 1.1);
                if (runEnergy > 100)
                    runEnergy = 100;
                player.setRunEnergy(runEnergy);
                int level = player.getSkills().getLevel(Skills.AGILITY);
                int realLevel = player.getSkills().getLevelForXp(Skills.AGILITY);
                player.getSkills().set(Skills.AGILITY, level >= realLevel ? realLevel + 5 : level + 5);
            }

        },
        MINT_CAKE {
            @Override
            public void effect(Player player) {

                player.setRunEnergy(100);
            }
        },

        GARDEN_PIE {
            @Override
            public void effect(Player player) {

                int level = player.getSkills().getLevel(Skills.FARMING);
                int realLevel = player.getSkills().getLevelForXp(Skills.FARMING);
                player.getSkills().set(Skills.FARMING, level >= realLevel ? realLevel + 3 : level + 3);
            }

        },

        FISH_PIE {
            @Override
            public void effect(Player player) {

                int level = player.getSkills().getLevel(Skills.FISHING);
                int realLevel = player.getSkills().getLevelForXp(Skills.FISHING);
                player.getSkills().set(Skills.FISHING, level >= realLevel ? realLevel + 3 : level + 3);
            }
        },

        ADMIRAL_PIE {
            @Override
            public void effect(Player player) {

                int level = player.getSkills().getLevel(Skills.FISHING);
                int realLevel = player.getSkills().getLevelForXp(Skills.FISHING);
                player.getSkills().set(Skills.FISHING, level >= realLevel ? realLevel + 5 : level + 5);
            }
        },

        WILD_PIE {
            @Override
            public void effect(Player player) {

                int level = player.getSkills().getLevel(Skills.SLAYER);
                int realLevel = player.getSkills().getLevelForXp(Skills.SLAYER);
                player.getSkills().set(Skills.SLAYER, level >= realLevel ? realLevel + 4 : level + 4);
                int level2 = player.getSkills().getLevel(Skills.RANGE);
                int realLevel2 = player.getSkills().getLevelForXp(Skills.RANGE);
                player.getSkills().set(Skills.RANGE, level2 >= realLevel2 ? realLevel2 + 4 : level2 + 4);
            }
        },

        SPICY_STEW_EFFECT {
            @Override
            public void effect(Player player) {

                if (Utils.random(100) > 5) {
                    int level = player.getSkills().getLevel(Skills.COOKING);
                    int realLevel = player.getSkills().getLevelForXp(Skills.COOKING);
                    player.getSkills().set(Skills.COOKING, level >= realLevel ? realLevel + 6 : level + 6);
                } else {
                    int level = player.getSkills().getLevel(Skills.COOKING);
                    player.getSkills().set(Skills.COOKING, level <= 6 ? 0 : level - 6);
                }
            }

        },

        CABAGE_MESSAGE {
            @Override
            public void effect(Player player) {

                player.getPackets().sendGameMessage("You don't really like it much.", true);
            }
        },

        ONION_MESSAGE {
            @Override
            public void effect(Player player) {

                player.getPackets().sendGameMessage("It hurts to see a grown " + player.getAppearence().isMale() != null
                        ? "male" : "female" + "cry.", true);
            }
        },

        POISION_KARMAMWANNJI_EFFECT {
            @Override
            public void effect(Player player) {

                player.applyHit(new Hit(player, 50, HitLook.POISON_DAMAGE));
            }
        },

        CUP_OF_TEA {
            @Override
            public void effect(Player player) {


                player.setNextForceTalk(new ForceTalk("Aaah, nothing like a nice cuppa tea!"));

                if (player.getAggressiveDelay() > 0) {
                    player.setAggressiveDelay(2);
                    player.getBuffDebuffTimersManager().addTimer(Timer.AGGRESSION_POTION_ACTIVE, 2 * 600);
                }
            }
        };

        public void effect(Player player) {
        }
    }

    public enum Food {
        GISSEL_MUSHROOM(17819, 30),
        EDICAP_MUSHROOM(17821, 90),
        BAKED_CAVE_POTATO(18093, 20),
        GISSEL_POTATO(18095, 60),
        EDICAP_POTATO(18097, 120),
        HEIM_CRAB(18159, 20),
        HEIM_CRAB_POTATO(18099, 50),
        HEIM_CRAB_GISSEL_POTATO(18119, 80),
        HEIM_CRAB_EDICAP_POTATO(18139, 140),
        RED_EYE(18161, 50),
        RED_EYE_POTATO(18101, 80),
        RED_EYE_GISSEL_POTATO(18121, 110),
        RED_EYE_EDICAP_POTATO(18141, 170),
        DUSK_EEL(18163, 70),
        DUSK_EEL_POTATO(18103, 100),
        DUSK_EEL_GISSEL_POTATO(18123, 130),
        DUSK_EEL_EDICAP_POTATO(18143, 190),
        GIANT_FLATFISH(18165, 100),
        FLATFISH_POTATO(18105, 130),
        FLATFISH_GISSEL_POTATO(18125, 160),
        FLATFISH_EDICAP_POTATO(18145, 220),
        SHORT_FINNED_EEL(18167, 120),
        SHORTFIN_POTATO(18107, 150),
        SHORTFIN_GISSEL_POTATO(18127, 180),
        SHORTFIN_EDICAP_POTATO(18147, 240),
        WEB_SNIPPER(18169, 150),
        SNIPPER_POTATO(18109, 180),
        SNIPPER_GISSEL_POTATO(18129, 210),
        SNIPPER_EDICAP_POTATO(18149, 270),
        BOULDABASS(18171, 170),
        BOULDABASS_POTATO(18111, 200),
        BOULDABASS_GISSEL_POTATO(18131, 230),
        BOULDABASS_EDICAP_POTATO(18151, 290),
        SALVE_EEL(18173, 200),
        SALVE_EEL_POTATO(18113, 230),
        SALVE_EEL_GISSEL_POTATO(18133, 260),
        SALVE_EEL_EDICAP_POTATO(18153, 320),
        BLUE_CRAB(18175, 220),
        BLUE_CRAB_POTATO(18115, 250),
        BLUE_CRAB_GISSEL_POTATO(18135, 280),
        BLUE_CRAB_EDICAP_POTATO(18155, 340),
        CAVE_MORAY(18177, 250),
        MORAY_POOTATO(18117, 280),
        MORAY_GISSEL_POTATO(18137, 310),
        MORAY_EDICAP_POTATO(18157, 370),
        GS_COOKIES_1(19467, -1) {
            @Override
            public int getHeal(Player player) {
                return (int) (player.getMaxHitpoints() * 0.25);
            }
        },
        GS_COOKIES_2(47385, -1) {
            @Override
            public int getHeal(Player player) {
                return (int) (player.getMaxHitpoints() * 0.25);
            }
        },
        GS_COOKIES_3(47386, -1) {
            @Override
            public int getHeal(Player player) {
                return (int) (player.getMaxHitpoints() * 0.25);
            }
        },

        /**
         * Fish
         */
        CRAYFISH(13433, 2),

        STEALING_CREATION_FOOD_1(14162, 5) {
            @Override
            public boolean canEat(Player player) {
                return canEatStealingCreationFood(player);
            }
        },

        STEALING_CREATION_FOOD_2(14164, 10) {
            @Override
            public boolean canEat(Player player) {
                return canEatStealingCreationFood(player);
            }
        },

        STEALING_CREATION_FOOD_3(14166, 15) {
            @Override
            public boolean canEat(Player player) {
                return canEatStealingCreationFood(player);
            }
        },

        STEALING_CREATION_FOOD_4(14168, 20) {
            @Override
            public boolean canEat(Player player) {
                return canEatStealingCreationFood(player);
            }
        },

        STEALING_CREATION_FOOD_5(14170, 25) {
            @Override
            public boolean canEat(Player player) {
                return canEatStealingCreationFood(player);
            }
        },

        ANCHOVIE(319, 1),

        SHRIMP(315, 3),

        KARAMBWANJI(3151, 3),

        SARDINE(325, 3),

        POISON_KARAMBWANJI(3146, 0, Effect.POISION_KARMAMWANNJI_EFFECT),

        KARAMBWANI(3144, 18),

        SLIMY_EEL(3381, 7 + Utils.random(2)),

        RAINBOW_FISH(10136, 11),

        CAVE_EEL(5003, 8 + Utils.random(2)),

        LAVA_EEL(2149, 7 + Utils.random(2)),

        HERRING(347, 5),

        MACKEREL(355, 6),

        TROUT(333, 7),

        COD(339, 7),

        PIKE(351, 8),

        SALMON(329, 9),

        TUNA(361, 10),

        LOBSTER(379, 12),

        BASS(365, 13),

        SWORDFISH(373, 14),

        MONKFISH(7946, 16),

        SHARK(385, 20),

        FOG_BANDAGES(12853, 15),

        TURTLE(397, 21),

        MANTA(391, 22),

        CAVEFISH(15266, 20),

        FURY_SHARK(20429, 28),

        GREAT_WHITE_SHARK(34729, 25),

        ROCKTAIL(15272, 23, 0, null, 10),

        /**
         * Meats
         */
        CHICKEN(2140, 3),

        MEAT(2142, 3), // TODO

        RABIT(3228, 5),

        ROAST_RABIT(7223, 7),

        ROASTED_BIRD_MEAT(9980, 6),

        CRAB_MEAT(7521, 10), // TODO

        ROASTED_BEAST_MEAT(9988, 8),

        CHOMPY(2878, 10),

        JUBBLY(7568, 15),

        OOMILE(2343, 14),

        /**
         * Pies
         */
        REDBERRY_PIE_FULL(2325, 5, 2333),

        REDBERRY_PIE_HALF(2333, 5, 2313),

        MEAT_PIE_FULL(2327, 6, 2331),

        MEAT_PIE_HALF(2331, 6, 2313),

        APPLE_PIE_FULL(2323, 7, 2335),

        APPLE_PIE_HALF(2335, 7, 2313),

        GARDEN_PIE_FULL(7178, 6, 7180, Effect.GARDEN_PIE),

        GARDEN_PIE_HALF(7180, 6, 2313, Effect.GARDEN_PIE),

        FISH_PIE_FULL(7188, 6, 7190, Effect.FISH_PIE),

        FISH_PIE_HALF(7188, 6, 2313, Effect.FISH_PIE),

        ADMIRAL_PIE_FULL(7198, 8, 7200, Effect.ADMIRAL_PIE),

        ADMIRAL_PIE_HALF(7200, 8, 2313, Effect.ADMIRAL_PIE),

        WILD_PIE_FULL(7208, 11, 7210, Effect.WILD_PIE),

        WILD_PIE_HALF(7210, 11, 2313, Effect.WILD_PIE),

        SUMMER_PIE_FULL(7218, 11, 7220, Effect.SUMMER_PIE),

        SUMMER_PIE_HALF(7220, 11, 2313, Effect.SUMMER_PIE),

        MINT_CAKE(9475, 5, Effect.MINT_CAKE),

        /**
         * Stews
         */

        STEW(2003, 11, 1923),

        SPICY_STEW(7513, 11, 1923, Effect.SPICY_STEW_EFFECT),

        CURRY(2011, 19, 1923),

        /**
         * Pizzas
         */
        PLAIN_PIZZA_FULL(2289, 7, 2291),

        PLAIN_PIZZA_HALF(2291, 7),

        MEAT_PIZZA_FULL(2293, 8, 2295),

        MEAT_PIZZA_HALF(2295, 8),

        ANCHOVIE_PIZZA_FULL(2297, 9, 2299),

        ANCHOVIE_PIZZA_HALF(2299, 9),

        PINEAPPLE_PIZZA_FULL(2301, 11, 2303),

        PINEAPPLE_PIZZA_HALF(2303, 11),

        /**
         * Potato Toppings
         */
        SPICEY_SAUCE(7072, 2, 1923),

        CHILLI_CON_CARNIE(7062, 14, 1923),

        SCRAMBLED_EGG(7078, 5, 1923),

        EGG_AND_TOMATO(7064, 8, 1923),

        FRIED_ONIONS(7084, 9, 1923),

        MUSHROOM_AND_ONIONS(7066, 11, 1923),

        FRIED_MUSHROOMS(7082, 5, 1923),

        TUNA_AND_CORN(7068, 13, 1923),

        /**
         * Baked Potato
         */
        BAKED_POTATO(6701, 4),

        POTATO_WITH_BUTTER(6703, 14),

        CHILLI_POTATO(7054, 14),

        POTATO_WITH_CHEESE(6705, 16),

        EGG_POTATO(7056, 16),

        MUSHROOM_AND_ONION_POTATO(7058, 20),

        TUNA_POTATO(7060, 24),

        /**
         * Gnome Food
         */
        TOAD_CRUNCHIES(2217, 8),

        SPICY_CRUNCHIES(2213, 7),

        WORM_CRUNCHIES(2205, 8),

        CHOCOCHIP_CRUNCHIES(9544, 7),

        FRUIT_BATTA(2277, 11),

        TOAD_BATTA(2255, 11),

        WORM_BATTA(2253, 11),

        VEGETABLE_BATTA(2281, 11),

        CHEESE_AND_TOMATO_BATTA(9535, 11),

        WORM_HOLE(2191, 12),

        VEG_BALL(2195, 12),

        PRE_MADE_VEG_BALL(2235, 12),

        TANGLED_TOAD_LEGS(2187, 15),

        CHOCOLATE_BOMB(2185, 15),

        /**
         * Misc
         */
        CAKE(1891, 4, 1893),

        KEBAB(1971, 11),

        EDIBLE_SEAWEED(403, 4),

        CELEBRATION_CAKE(20179, 5),

        TWO_THIRDS_CAKE(1893, 4, 1895),

        SLICE_OF_CAKE(1895, 4),

        CHOCOLATE_CAKE(1897, 4, 1899),

        TWO_THIRDS_CHOCOLATE_CAKE(1899, 4, 1901),

        CHOCOLATE_SLICE(1901, 4),

        BANANA(1963, 2),

        BANANA_SLICE(3162, 2),

        FISHCAKE(7530, 11),

        BREAD(2309, 5),

        CHOCOLATE_BAR(1973, 3),

        CABBAGE(1965, 1, Effect.CABAGE_MESSAGE),

        ONION(1957, 1, Effect.ONION_MESSAGE),

        EVIL_TURNIP(12134, 1),

        POT_OF_CREAM(2130, 1),

        CHEESE_WHEEL(18789, 2),

        THIN_SNAIL_MEAT(3369, 5 + Utils.random(2)),

        LEAN_SNAIL_MEAT(3371, 8),

        FAT_SNAIL_MEAT(3373, 8 + Utils.random(2)),

        TOMATO(1982, 2),

        CHEESE(1985, 2),

        PURPLE_SWEETS(10476, 2),

        EASTER_EGG(1961, 20),

        /**
         * Easter eggs.
         */
        EASTER_EGG_1(7928, 14), EASTER_EGG_2(7929, 14), EASTER_EGG_3(7930, 14), EASTER_EGG_4(7931, 14), EASTER_EGG_5(7932, 14), EASTER_EGG_6(7933, 14),

        BANDAGES(14640, 20),// bandages for halloween


        ROCKTAIL_SOUP(26313, 26),

        CUP_OF_TEA(712, 20, 1980, Effect.CUP_OF_TEA),
        JUJU_GUMBO(19949, 24, Effect.JUJU_GUMBO) {
            @Override
            public boolean canEat(Player player) {
                if (player.jujuGumbo > 0) {
                    player.sendMessage("You can still feel the effects from the last juju gumbo...");
                    return false;
                }
                if (player.baronShark > 0) {
                    player.sendMessage("You still feel full from the last baron shark...");
                    return false;
                }
                return true;
            }
        },
        BARON_SHARK(19948, 20, Effect.BARON_SHARK) {
            @Override
            public boolean canEat(Player player) {
                if (player.jujuGumbo > 0) {
                    player.sendMessage("You can still feel the effects from the last juju gumbo...");
                    return false;
                }
                if (player.baronShark > 0) {
                    player.sendMessage("You still feel full from the last baron shark...");
                    return false;
                }
                return true;
            }
        },
        ;


        /**
         * A map of object ids to foods.
         */
        private static final Map<Integer, Food> foods = new HashMap<Integer, Food>();

        /**
         * Populates the tree map.
         */
        static {
            for (final Food food : Food.values()) {
                foods.put(food.id, food);
            }
        }

        /**
         * The food id
         */
        private final int id;
        /**
         * The healing health
         */
        private final int heal;
        /**
         * The new food id if needed
         */
        private int newId;
        private int extraHP;
        /**
         * Our effect
         */
        private Effect effect;

        /**
         * Represents a food being eaten
         *
         * @param id The food id
         * @param heal The healing health received
         */
        Food(int id, int heal) {
            this.id = id;
            this.heal = heal;
        }

        Food(int id, int heal, Effect effect) {
            this(id, heal, 0, effect);
        }

        /**
         * Represents a part of a food item being eaten (example: cake)
         *
         * @param id The food id
         * @param heal The heal amount
         * @param newId The new food id
         */
        Food(int id, int heal, int newId) {
            this(id, heal, newId, null);
        }

        Food(int id, int heal, int newId, Effect effect) {
            this(id, heal, newId, effect, 0);
        }

        Food(int id, int heal, int newId, Effect effect, int extraHP) {
            this.id = id;
            this.heal = heal;
            this.newId = newId;
            this.effect = effect;
            this.extraHP = extraHP;
        }

        /**
         * Gets a food by an object id.
         *
         * @param itemId The object id.
         * @return The food, or <code>null</code> if the object is not a food.
         */
        public static Food forId(int itemId) {
            return foods.get(itemId);
        }

        public int getExtraHP() {
            return extraHP;
        }

        /**
         * Gets the exp amount.
         *
         * @return The exp amount.
         */
        public int getHeal(Player player) {
            return heal;
        }

        /**
         * Gets the id.
         *
         * @return The id.
         */
        public int getId() {
            return id;
        }

        /**
         * Gets the new food id
         *
         * @return The new food id.
         */
        public int getNewId() {
            return newId;
        }

        public boolean canEat(Player player) {
            return true;
        }

        private static boolean canEatStealingCreationFood(Player player) {
            if (StealingCreation.isInGame(player)) {
                return true;
            }
            player.getPackets().sendGameMessage("You can only eat that during Stealing Creation.");
            return false;
        }
    }
}
