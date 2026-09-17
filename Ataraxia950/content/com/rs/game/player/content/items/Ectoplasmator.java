package com.rs.game.player.content.items;

import com.google.common.collect.ImmutableMap;
import com.rs.Settings;
import com.rs.game.World;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.content.items.AshScattering.AshesData;
import com.rs.utils.Colors;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author lare96 <http://github.com/lare96>
 */
public final class Ectoplasmator {

    /**
     * Makes the drop rate 1/2 for ectoplasmator and 1/2 for essence.
     */
    public static final boolean TEST_ECTOPLASMATOR = false;

    /**
     * The maximum amount of charges the attuned ectoplasmator can hold.
     */
    public static final int MAX_CHARGES = 10_000;

    /**
     * The base drop rate for the regular ectoplasmator (1/x).
     */
    public static final int DEFAULT_ECTO_RATE = 500;

    /**
     * The base drop rate for ghostly essence (1/12).
     */
    public static final int GHOSTLY_ESSENCE_DROP_RATE = 16;

    private enum EctoplasmatorType {
        NORMAL, ATTUNED
    }

    private static final ImmutableMap<String, Integer> DROP_RATES;

    static {
        Map<String, Integer> rates = new HashMap<>();
        rates.put("Ghost", DEFAULT_ECTO_RATE);
        rates.put("Death wing", DEFAULT_ECTO_RATE);
        rates.put("Ghast", DEFAULT_ECTO_RATE);
        rates.put("Loar shade", DEFAULT_ECTO_RATE);
        rates.put("Phrin shade", DEFAULT_ECTO_RATE);
        rates.put("Riyl shade", DEFAULT_ECTO_RATE);
        rates.put("Asyn shade", DEFAULT_ECTO_RATE);
        rates.put("Fiyr shade", DEFAULT_ECTO_RATE);
        rates.put("Aberrant spectre", DEFAULT_ECTO_RATE);
        rates.put("Tortured soul", DEFAULT_ECTO_RATE);
        rates.put("Tormented wraith", DEFAULT_ECTO_RATE);
        rates.put("Spiritual ranger", 350);
        rates.put("Spiritual mage", 350);
        rates.put("Spiritual warrior", 350);
        rates.put("Dharok the Wretched", 300);
        rates.put("Guthan the Infested", 300);
        rates.put("Karil the Tainted", 300);
        rates.put("Torag the Corrupted", 300);
        rates.put("Verac the Defiled", 300);
        rates.put("Revenant imp", DEFAULT_ECTO_RATE);
        rates.put("Revenant goblin", DEFAULT_ECTO_RATE);
        rates.put("Revenant icefiend", DEFAULT_ECTO_RATE);
        rates.put("Recenant hobgoblin", DEFAULT_ECTO_RATE);
        rates.put("Revenant pyrefiend", DEFAULT_ECTO_RATE);
        rates.put("Revenant vampyre", DEFAULT_ECTO_RATE);
        rates.put("Revenant hellhound", 350);
        rates.put("Ghostly warrior", DEFAULT_ECTO_RATE);
        rates.put("Revenant cyclops", 350);
        rates.put("Revenant werewolf", 325);
        rates.put("Revenant demon", 300);
        rates.put("Revenant ork", 300);
        rates.put("Revenant dark beast", 250);
        rates.put("Tormented demon", 150);
        rates.put("Chaos elemental", 150);
        rates.put("K'ril tsutsaroth", 85);
        rates.put("Revenant knight", 200);
        rates.put("Revenant dragon", 150);
        rates.put("Abyssal demon", DEFAULT_ECTO_RATE);
        rates.put("Greater demon", DEFAULT_ECTO_RATE);
        rates.put("Kalgerion demon", 350);
        rates.put("Banshee", DEFAULT_ECTO_RATE);
        Map<String, Integer> formattedRates = new HashMap<>(rates.size());
        for (Map.Entry<String, Integer> entry : rates.entrySet()) {
            String key = entry.getKey().toLowerCase();
            int value = entry.getValue();
            formattedRates.put(key, value);
        }
        DROP_RATES = ImmutableMap.copyOf(formattedRates);
    }

    public enum EctoplasmatorMonster {
        GHOST("Ghost", 4.5, 15),
        DEATH_WING("Death wing", 4.5, 15),
        GHAST("Ghast", 4.5, 15),
        BANSHEE("Banshee", 4.5, 15),
        LOAR_SHADE("Loar shade", 4.5, 15),
        PHRIN_SHADE("Phrin shade", 4.5, 15),
        RIYL_SHADE("Riyl shade", 4.5, 15),
        ASYN_SHADE("Asyn shade", 4.5, 15),
        FIYR_SHADE("Fiyr shade", 4.5, 15),
        ABERRANT_SPECTRE("Aberrant spectre", 15, 72),
        TORTURED_SOUL("Tortured soul", 15, 72),
        TORMENTED_WRAITH("Tormented wraith", 15, 72),
        SPIRITUAL_RANGER("Spiritual ranger", 15, 72),
        SPIRITUAL_MAGE("Spiritual mage", 15, 72),
        SPIRITUAL_WARRIOR("Spiritual warrior", 15, 72),
        DHAROK_THE_WRETCHED("Dharok the Wretched", 15, 72),
        GUTHAN_THE_INFESTED("Guthan the Infested", 15, 72),
        KARIL_THE_TAINTED("Karil the Tainted", 15, 72),
        TORAG_THE_CORRUPTED("Torag the Corrupted", 15, 72),
        VERAC_THE_DEFILED("Verac the Defiled", 15, 72),
        REVENANT_IMP("Revenant imp", 15, 72),
        REVENANT_GOBLIN("Revenant goblin", 15, 72),
        REVENANT_ICEFIEND("Revenant icefiend", 15, 72),
        REVENANT_HOBGOBLIN("Recenant hobgoblin", 15, 72),
        REVENANT_PYREFIEND("Revenant pyrefiend", 15, 72),
        REVENANT_VAMPYRE("Revenant vampyre", 15, 72),
        REVENANT_HELLHOUND("Revenant hellhound", 15, 72),
        GHOSTLY_WARRIOR("Ghostly warrior", 15, 72),
        REVENANT_CYCLOPS("Revenant cyclops", 15, 72),
        REVENANT_WEREWOLF("Revenant werewolf", 15, 72),
        REVENANT_DEMON("Revenant demon", 15, 72),
        REVENANT_ORK("Revenant ork", 15, 72),
        REVENANT_DARK_BEAST("Revenant dark beast", 15, 72),
        REVENANT_KNIGHT("Revenant knight", 15, 72),
        REVENANT_DRAGON("Revenant dragon", 15, 72);

        public final String name;
        public final double exp; // 0 = NO EXP
        public final double attunedExp; // 0 = NO EXP

        public static EctoplasmatorMonster get(String name) {
            for (EctoplasmatorMonster monster : values()) {
                if (monster.name.equalsIgnoreCase(name))
                    return monster;
            }
            return null;
        }

        public double getExp(EctoplasmatorType type) {
            switch (type) {
                case NORMAL:
                    return exp;
                case ATTUNED:
                    return attunedExp;
                default:
                    throw new IllegalArgumentException();
            }
        }

        EctoplasmatorMonster(String name, double exp, double attunedExp) {
            this.name = name.toLowerCase();
            this.exp = exp;
            this.attunedExp = attunedExp;
        }
    }

    public enum EssenceMonster {
        QUEEN_BLACK_DRAGON("Queen black dragon", 25, 150),
        CHAOS_ELEMENTAL("Chaos elemental", 10, 90),
        TORMENTED_DEMON("Tormented demon", 10, 50),
        KRIL_TSUTSAROTH("K'ril tsutsaroth", 50, 200);

        public final String name;
        public final int min;
        public final int max;

        public static EssenceMonster get(String name) {
            for (EssenceMonster drop : values()) {
                if (drop.name.equalsIgnoreCase(name))
                    return drop;
            }
            return null;
        }

        EssenceMonster(String name, int min, int max) {
            this.name = name;
            this.min = min;
            this.max = max;
        }

        public int getAmount() {
            return ThreadLocalRandom.current().nextInt(min, max + 1);
        }
    }

    public static final int ECTOPLASMATOR_ID = 25354;
    public static final int ATTUNED_ECTOPLASMATOR_ID = 32339;
    public static final int DEGRADED_ATTUNED_ECTOPLASMATOR_ID = 32340;
    public static final int GHOSTLY_ESSENSE_ID = 32341;

    public static boolean useEssence(Player player, int ectoplasmator, Item essence) {
        if (essence.getId() != GHOSTLY_ESSENSE_ID) {
            return false;
        }
        switch (ectoplasmator) {
            case ECTOPLASMATOR_ID:
                if (essence.getAmount() < 100) {
                    player.sendMessage("You need 100 ghostly essence to upgrade the ectoplasmator.");
                } else {
                    player.getInventory().removeItems(new Item(GHOSTLY_ESSENSE_ID, 100), new Item(ECTOPLASMATOR_ID));
                    player.getInventory().addItem(new Item(ATTUNED_ECTOPLASMATOR_ID));
                    player.ectoCharges = 1000;
                    player.sendMessage("You attune the ectoplasmator. It has " + Colors.RED + "1000</col> charges.");
                }
                return true;
            case DEGRADED_ATTUNED_ECTOPLASMATOR_ID:
            case ATTUNED_ECTOPLASMATOR_ID:
                int useAmount = essence.getAmount();
                int chargesAmount = useAmount * 10;
                int totalAmount = player.ectoCharges + chargesAmount;
                if (totalAmount > MAX_CHARGES) {
                    double overflow = totalAmount - MAX_CHARGES;
                    double essenceAmount = Math.ceil(overflow / 10.0);
                    useAmount -= essenceAmount;
                }

                if (useAmount <= 0) {
                    player.sendMessage("The ectoplasmator cannot hold anymore charges.");
                    return false;
                }

                int newCharges = useAmount * 10;
                if (ectoplasmator == DEGRADED_ATTUNED_ECTOPLASMATOR_ID) {
                    player.getInventory().removeItems(new Item(GHOSTLY_ESSENSE_ID, useAmount), new Item(DEGRADED_ATTUNED_ECTOPLASMATOR_ID));
                    player.getInventory().addItem(new Item(ATTUNED_ECTOPLASMATOR_ID));
                    player.ectoCharges = newCharges;
                } else {
                    player.getInventory().removeItems(new Item(GHOSTLY_ESSENSE_ID, useAmount));
                    player.ectoCharges += newCharges;
                }
                player.sendMessage("You add " + Colors.RED + newCharges + "</col> charges to the ectoplasmator.");
                return true;
            default:
                return false;
        }
    }

    public static void onDrop(Player player, NPC npc, WorldTile tile) {
        String name = npc.getName().toLowerCase();
        dropEctoplasmator(player, name);
        dropEssence(player, name, tile);
        ectoplasmator(player).ifPresent(ectoType -> giveXp(player, name, ectoType));
    }

    public static boolean scatterAshes(Player player, NPC npc, int dropId) {
        AshesData ashes;
        if (hasAttunedEctoplasmator(player) && (ashes = AshesData.forId(dropId)) != null) {
            player.skills.addXp(Skills.PRAYER, ashes.getExp());
            decrementCharge(player);
            player.addBonesOffered();
            return true;
        }
        return false;
    }

    private static void giveXp(Player player, String name, EctoplasmatorType type) {
        EctoplasmatorMonster exp = EctoplasmatorMonster.get(name);
        if (exp != null) {
            player.skills.addXp(Skills.PRAYER, exp.getExp(type));
            decrementCharge(player);
            player.addBonesOffered();
        }
    }

    private static void dropEctoplasmator(Player player, String name) {
        Integer rate = DROP_RATES.get(name);
        if (rate != null &&
                (Settings.TEST_SERVER_MODE || ThreadLocalRandom.current().nextInt(rate) == 0)
                && player.getInventory().hasFreeSlots()
                && !player.hasItem(new Item(ECTOPLASMATOR_ID))
                && !player.hasItem(new Item(ATTUNED_ECTOPLASMATOR_ID))
                && !player.hasItem(new Item(DEGRADED_ATTUNED_ECTOPLASMATOR_ID))) {
            player.sendMessage("As you slay your enemy, a light in your inventory reveals an " + Colors.RED + "ectoplasmator</col>!");
            player.getInventory().addItem(new Item(ECTOPLASMATOR_ID));
        }
    }

    private static void dropEssence(Player player, String name, WorldTile tile) {
        EssenceMonster essenceMonster = EssenceMonster.get(name);
        if (essenceMonster != null) {
            if (ThreadLocalRandom.current().nextInt(GHOSTLY_ESSENCE_DROP_RATE) == 0 || (TEST_ECTOPLASMATOR && ThreadLocalRandom.current().nextBoolean())) {
                val loot = new Item(GHOSTLY_ESSENSE_ID, essenceMonster.getAmount());
                player.catchDrop(loot, () -> World.addGroundItem(loot, tile, player, true, 180));
            }
        }
    }

    private static void decrementCharge(Player player) {
        ectoplasmator(player).filter(type -> type == EctoplasmatorType.ATTUNED).
                ifPresent(type -> {
                    if (--player.ectoCharges <= 0) {
                        player.ectoCharges = 0;
                        player.sendMessage(Colors.RED + "Your ectoplasmator has degraded!");

                        Item item = new Item(DEGRADED_ATTUNED_ECTOPLASMATOR_ID);
                        int index = player.getInventory().getItems().getThisItemSlot(ATTUNED_ECTOPLASMATOR_ID);
                        if (index != -1) {
                            player.getInventory().set(index, item);
                            return;
                        }
                        index = player.getEquipment().getItems().getThisItemSlot(ATTUNED_ECTOPLASMATOR_ID);
                        if (index != -1) {
                            player.getEquipment().set(index, item);
                            return;
                        }
                        if (player.getFamiliar() != null && player.getFamiliar().getBob() != null) {
                            index = player.getFamiliar().getBob().getBeastItems().getThisItemSlot(ATTUNED_ECTOPLASMATOR_ID);
                            if (index != -1) {
                                player.getFamiliar().getBob().getBeastItems().set(index, item);
                                return;
                            }
                        }
                        throw new IllegalStateException("didn't have attuned ectoplasmator");
                    }

                    if (player.ectoCharges % 100 == 0) {
                        player.sendMessage("You have " + Colors.RED + player.ectoCharges + "</col> ectoplasmator charges left.");
                    }
                });
    }

    private static Optional<EctoplasmatorType> ectoplasmator(Player player) {
        if (player.containsOneItem(ECTOPLASMATOR_ID)) {
            return Optional.of(EctoplasmatorType.NORMAL);
        } else if (player.containsOneItem(DEGRADED_ATTUNED_ECTOPLASMATOR_ID)) {
            return Optional.of(EctoplasmatorType.NORMAL);
        } else if (player.containsOneItem(ATTUNED_ECTOPLASMATOR_ID)) {
            return Optional.of(EctoplasmatorType.ATTUNED);
        }
        return Optional.empty();
    }

    private static boolean hasAttunedEctoplasmator(Player player) {
        return ectoplasmator(player).filter(type -> type == EctoplasmatorType.ATTUNED).isPresent();
    }
}