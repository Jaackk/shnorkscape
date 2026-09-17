package com.rs.game.player.actions.herblore;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.activites.quest.root_of_evil.RootOfEvil;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.HerbloreRandomEvent;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.Pots;
import com.rs.game.player.content.packs.portable.PortableType;
import com.rs.game.player.content.skillingcontracts.impl.HerbloreContractList;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.Utils;
import lombok.val;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Herblore extends Action {

    public static final int[] URCHINS = {35721, 35722, 35723};
    public static final short VIAL = 227;
    public static final int JUJU_VIAL = 19994;
    public static final int EMPTY_JUJU_VIAL = 19996;
    public static final short EMPTY_VIAL = 229;
    public static final short CUP_OF_HOT_WATER = 4460;
    public static final short COCONUT_MILK = 5935;
    public static final short PESTLE_AND_MORTAR = 233;
    public static final short SWAMP_TAR = 1939;
    public static final short MUD_RUNE = 4698;
    public static final short RANGE_POT3 = 169;

    private Item node;
    private final boolean portable;
    private int runesGround;
    private Item otherItem;
    private Ingredients ingredients;
    private RawIngredient rawIngredient;
    private int ticks;
    private int slot;

    public Herblore(Item node, Item otherNode, int amount, boolean portable) {
        this.otherItem = otherNode;
        this.ticks = amount;
        this.portable = portable;
        if (node.getId() == PESTLE_AND_MORTAR || otherNode.getId() == PESTLE_AND_MORTAR) {
            this.rawIngredient = RawIngredient.forId(node.getId());
            if (rawIngredient == null) {
                rawIngredient = RawIngredient.forId(otherNode.getId());
                this.node = node;
                this.otherItem = otherNode;
            } else {
                rawIngredient = RawIngredient.forId(node.getId());
                this.node = otherNode;
                this.otherItem = node;
            }
        } else {
            if (ticks > 28)
                ticks = 28;
            this.ingredients = Ingredients.forId(node.getId());
            if (ingredients == null) {
                ingredients = Ingredients.forId(otherNode.getId());
                this.node = node;
                this.otherItem = otherNode;
            } else {
                ingredients = Ingredients.forId(node.getId());
                this.node = otherNode;
                this.otherItem = node;
            }
        }
    }

    public static int isHerbloreSkill(Item first, Item other) {
        Item swap = first;
        Ingredients ingredient = Ingredients.forId(first.getId());
        if (ingredient == null) {
            ingredient = Ingredients.forId(other.getId());
            first = other;
            other = swap;
        }
        if (ingredient != null) {
            int slot = ingredient.getSlot(other.getId());
            if (slot == -1) {
                ingredient = Ingredients.forId(other.getId());
                if (ingredient == null)
                    return -1;
                first = other;
                other = swap;
                slot = ingredient.getSlot(other.getId());
                return slot > -1 ? ingredient.getRewards()[slot] : -1;
            }
            return slot > -1 ? ingredient.getRewards()[slot] : -1;
        }
        swap = first;
        RawIngredient raw = RawIngredient.forId(first.getId());
        if (raw == null) {
            raw = RawIngredient.forId(other.getId());
            first = other;
            other = swap;
        }
        if (raw != null) {
            return other.getId() == PESTLE_AND_MORTAR ? raw.getCrushedItem().getId() : -1;
        }
        return -1;
    }

    public static void performPortableAction(Player player, WorldObject object) {
        ArrayList<Integer> possibleIngredients = new ArrayList<Integer>();
        for (Ingredients i : Ingredients.values()) {
            if (player.getInventory().containsItem(i.getItemId(), 1))
                possibleIngredients.add(i.getItemId());
        }

        if (possibleIngredients.isEmpty()) {
            player.sendMessage("You do not have any ingredients to use.");
            return;
        }

        Item ingredient = getPreferredItemToUse(player, possibleIngredients);
        if (ingredient == null) {
            player.sendMessage("You do not have any ingredients to use.");
            return;
        }

        Ingredients preferredItem = Ingredients.forId(ingredient.getId());
        if (preferredItem == null)
            return;

        ArrayList<Integer> possibleOthers = new ArrayList<Integer>();
        for (int other : Ingredients.forId(ingredient.getId()).getOtherItems()) {
            if (player.getInventory().containsItem(other, 1))
                possibleOthers.add(other);
        }

        if (possibleOthers.isEmpty()) {
            player.sendMessage("You do not have any ingredients to use.");
            return;
        }

        Item other = getPreferredItemToUse(player, possibleOthers);
        if (other == null) {
            player.sendMessage("You do not have any ingredients to use.");
            return;
        }

        final int herblore = Herblore.isHerbloreSkill(ingredient, other);
        if (herblore > -1) {
            boolean portable = PortableType.isPortableObject(object.getId());
            if (!HerbloreRs3Dialogue.sendHerbloreInterface(player, ingredient, other, portable))
                player.getDialogueManager().startDialogue("HerbloreD", herblore, ingredient, other, portable);
        }
    }

    private static Item getPreferredItemToUse(Player player, ArrayList<Integer> ints) {
        Item temp = null;
        for (int i : ints) {
            if (temp == null || player.getInventory().getNumberOf(i) > temp.getAmount())
                temp = new Item(i, player.getInventory().getNumberOf(i));
        }
        return temp;
    }

    public int get_amount(int id) {
        switch (id) {
            case 35721:
                return 10;
            case 35722:
                return 7;
            case 35723:
                return 5;
            default:
                return 1;
        }
    }

    @Override
    public boolean process(Player player) {
        if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsInventoryInter()) {
            player.sendMessage("Please finish what you're doing before doing this action.");
            return false;
        }
        if (player.clickedObject != null) {
            if (!World.containsObjectWithId(player.clickedObject, player.clickedObject.getId()))
                return false;
        }
        if (ingredients == Ingredients.TORSTOL && node.getId() != 227) {
            if (!player.getInventory().containsOneItem(15309) || !player.getInventory().containsOneItem(15313) || !player.getInventory().containsOneItem(15317) || !player.getInventory().containsOneItem(15321) || !player.getInventory().containsOneItem(15325))
                return false;
        }

        if (otherItem.getId() == 12539 && node.getId() == RANGE_POT3) {
            if (!player.getInventory().containsOneItem(RANGE_POT3) || !player.getInventory().containsItem(12539, 5)) {
                player.sendMessage(Colors.RED + "You need at least 5 grenwall spikes per potion to make this!");
                return false;
            }
            return true;
        }
        if (otherItem.getId() == MUD_RUNE && node.getId() == PESTLE_AND_MORTAR) {
            return player.getInventory().containsOneItem(MUD_RUNE) && player.getInventory().containsOneItem(PESTLE_AND_MORTAR);
        }
        if (!player.getInventory().containsOneItem(node.getId(), 1) || !player.getInventory().containsOneItem(otherItem.getId(), 1))
            return false;
        if (Utils.random(500) == 0 && player.hasRandomEvent()) {
            if (!player.followedByRandomEventNPC()) {
                NPC npc = new HerbloreRandomEvent(player, player);
                if (npc.withinDistance(player, 14)) {
                    player.setCurrentRandomEventNPC(npc);
                    player.sendMessage("<col=ff0000>Eli appears from nowhere.", true);
                }
            }
        }
        return true;
    }

    public int getAmount(int item) {
        int amt = 1;
        switch (item) {
            case 35721:
                amt = 10;
                break;
            case 35722:
                amt = Utils.random(6, 9);
                break;
            case 35723:
                amt = 5;
                break;
            case 12539:
                amt = 5;
                break;
            default:
                amt = 1;
                break;
        }
        return amt;
    }

    private static final Set<Ingredients> JUJU_POTIONS = EnumSet.of(
            Ingredients.ERZILLE,
            Ingredients.ARGWAY,
            Ingredients.UGUNE,
            Ingredients.SHENGO,
            Ingredients.SAMADEN,
            Ingredients.CORRUPT_VINE,
            Ingredients.SHADOW_VINE,
            Ingredients.MARBLE_VINE,
            Ingredients.PLANT_TEETH,
            Ingredients.AQUATIC_VINE,
            Ingredients.OILY_VINE,
            Ingredients.DRACONIC_VINE,
            Ingredients.SARADOMIN_VINE,
            Ingredients.GUTHIX_VINE,
            Ingredients.ZAMORAK_VINE);

    @Override
    public int processWithDelay(Player player) {
        if (!canMakeTruthSerum(player)) {
            return -1;
        }
        if (ingredients == Ingredients.EVIL_HERB && ThreadLocalRandom.current().nextInt(16) == 0) {
            player.getInventory().removeItems(new Item(otherItem.getId(), getAmount(otherItem.getId())), rawIngredient == null ? new Item(node.getId(), 1) : null);
            player.sendMessage("The evil potion bursts into flames right before your eyes.");
            int damage = (int) (player.getSkills().getLevel(Skills.HITPOINTS) * 0.10);
            if (damage < 1) {
                damage = 1;
            }
            player.addPotionsMade();
            player.applyHit(new Hit(damage, Hit.HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
            player.lock();
            player.setNextAnimation(new Animation(2836));
            WorldTasksManager.schedule(new WorldTask() {
                boolean looped = false;

                @Override
                public void run() {
                    if (!looped) {
                        looped = true;
                        byte[] delta = Utils.getDirection(player.getDirection());
                        World.sendGraphics(player, new Graphics(453), player.transform(delta[0], delta[1], 0));
                    } else {
                        player.setNextAnimation(new Animation(-1));
                        stop();
                        player.unlock();
                    }
                }
            }, 1, 1);
            return -1;
        }
        boolean cleansed = false;
        if (node.getId() == PESTLE_AND_MORTAR || otherItem.getId() == PESTLE_AND_MORTAR)
            player.setNextAnimation(new Animation(364));
        else {
            if (player.getAnimations().hasEnhancedPotion && player.getAnimations().enhancedPotion) {
                player.setNextGraphics(new Graphics(3216));
                player.setNextGraphics(new Graphics(3217));
                player.setNextGraphics(new Graphics(3218));
                player.setNextAnimation(new Animation(17097));
            } else
                player.setNextAnimation(new Animation(363));
        }
        ticks--;
        if (otherItem.getId() == SWAMP_TAR || node.getId() == SWAMP_TAR)
            player.sendMessage("You add the " + otherItem.getDefinitions().getName().toLowerCase().replace("clean ", "") + " on the swamp tar.", true);

        else if (otherItem.getId() == PESTLE_AND_MORTAR || node.getId() == PESTLE_AND_MORTAR)
            player.sendMessage("You crush the " + otherItem.getDefinitions().getName().toLowerCase() + " with your pestle and mortar.", true);
        else if (ingredients == Ingredients.TORSTOL && node.getId() != VIAL) {
            player.addPotionsMade();

            if (portable && Utils.random(4) == 2) {
                player.getBank().addItem(new Item(ingredients.getRewards()[slot], 1), true);
                player.sendMessage(Colors.GOLD + "<shad=000000>You mix such a potent potion that you fill an extra vial, thanks to this incredible mixing station! It has been sent to your bank.", true);
            }
            player.sendMessage("You combine the torstol with the potions and get an overload; " + "potions made: " + Colors.RED + Utils.getFormattedNumber(player.getPotionsMade()) + "</col>.", true);
            player.getInventory().removeItems(new Item(15309), new Item(15313), new Item(15317), new Item(15321), new Item(15325), new Item(Ingredients.TORSTOL.getItemId()));

        } else {
            if (node.getId() != VIAL)
                player.addPotionsMade();

            if (portable && Utils.random(4) == 2) {
                player.getBank().addItem(new Item(ingredients.getRewards()[slot], 1), true);
                player.sendMessage(Colors.GOLD + "<shad=000000>You mix such a potent potion that you fill an extra vial, thanks to this incredible mixing station! It has been sent to your bank.", true);
            }
            player.sendMessage("You mix the " + otherItem.getDefinitions().getName().toLowerCase() + " into your " + (node.getId() != VIAL ? "potion; potions made: " + Colors.RED + Utils.getFormattedNumber(player.getPotionsMade()) + "</col>." : "vial of water."), true);
        }
        if (otherItem.getId() == PESTLE_AND_MORTAR || node.getId() == PESTLE_AND_MORTAR) {
            int amt = getAmount(otherItem.getId());
            if (!player.getInventory().containsItem(otherItem.getId(), amt)) {
                player.sendMessage(Colors.RED + "You don't have enough " + otherItem.getDefinitions().getName() + " left to grind!", true);
                return -1;
            } else
                player.getInventory().removeItems(new Item(otherItem.getId(), getAmount(otherItem.getId())));

        } else {
            if (ingredients != Ingredients.TORSTOL || ingredients == Ingredients.TORSTOL && node.getId() == VIAL) {
                if (player.hasCleansingActivated() && Utils.random(10) == 0) {
                    if (!player.getInventory().hasFreeSlots()) {
                        player.sendMessage("Due to lack of free space, the newly made potion has been sent to bank.", true);
                        cleansed = true;
                    }
                    player.sendMessage(Colors.GREEN + "<shad=000000>Your Scroll of Cleansing saves you an ingredient!", true);
                } else
                    player.getInventory().removeItems(new Item(otherItem.getId(), getAmount(otherItem.getId())), rawIngredient == null ? new Item(node.getId(), 1) : null);
            }
        }

        val item = rawIngredient != null ? rawIngredient.getCrushedItem() : new Item(ingredients.getRewards()[slot], 1);
        HerbloreContractList.listenComplete(player, item.getId());
        double xp = rawIngredient != null ? 0 : ingredients.getExperience()[slot];
        if (JUJU_POTIONS.contains(ingredients)) {
            xp *= 1.25;
        }
        if (!cleansed)
            player.getInventory().addItem(item);
        else
            player.getBank().addItem(item, true);
        player.getSkills().addXp(Skills.HERBLORE, xp * increasedExperience(player));
        ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);

        if (rawIngredient != null && rawIngredient.getRawId() == MUD_RUNE) {
            runesGround++;
            if (runesGround >= 250) {
                runesGround = 0;
                return -1;
            }
        }

        if (ticks > 0)
            return 1;
        return -1;
    }

    @Override
    public boolean start(Player player) {
        if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsInventoryInter()) {
            player.sendMessage("Please finish what you're doing before doing this action.");
            return false;
        }
        if (player == null || node == null)
            return false;
        if ((ingredients == null && rawIngredient == null) || otherItem == null)
            return false;
        if (otherItem.getId() == PESTLE_AND_MORTAR || node.getId() == PESTLE_AND_MORTAR) {
            if (!player.getInventory().containsOneItem(PESTLE_AND_MORTAR)) {
                player.sendMessage("You need a pestle and mortar in order to do this.");
                return false;
            }
        }
        if (ingredients != null) {
            this.slot = ingredients.getSlot(otherItem.getId());
            if (slot == -1) {
                this.slot = ingredients.getSlot(node.getId());
                if (slot == -1) {
                    Item node = new Item(this.node.getId(), this.node.getAmount());
                    this.node = otherItem;
                    otherItem = node;
                    ingredients = Ingredients.forId(otherItem.getId());
                    this.slot = ingredients.getSlot(otherItem.getId());
                    if (slot == -1)
                        this.slot = ingredients.getSlot(this.node.getId());
                }
            }

            if (player.getSkills().getLevel(Skills.HERBLORE) < ingredients.getLevels()[slot]) {
                player.sendMessage("You need a herblore level of " + ingredients.getLevels()[slot] + " to combine these ingredients.");
                return false;
            }
            return canMakeTruthSerum(player);
        }
        return true;
    }

    @Override
    public void stop(final Player player) {
        setActionDelay(player, 3);
        player.clickedObject = null;
    }

    private boolean canMakeTruthSerum(Player player) {
        if (ingredients == Ingredients.EVIL_DUST &&
                slot == 2) {
            if (player.quests.getCurrentStage(RootOfEvil.class) != 5) {
                return false;
            }
            if (player.hasItem(6952) || player.hasItem(5747)) {
                player.sendMessage("I don't need to make this anymore.");
                return false;
            }
        }
        return true;
    }

    private double increasedExperience(Player player) {
        double xpBoost = 1.0;
        if (player.getEquipment().getHatId() == 25190)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 34923)
            xpBoost *= 1.03;
        if (player.getEquipment().getChestId() == 25191)
            xpBoost *= 1.01;
        if (player.getEquipment().getLegsId() == 25192)
            xpBoost *= 1.01;
        if (player.getEquipment().getBootsId() == 25193)
            xpBoost *= 1.01;
        if (player.getEquipment().getGlovesId() == 25194)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 25190 && player.getEquipment().getChestId() == 25191 && player.getEquipment().getLegsId() == 25192 && player.getEquipment().getBootsId() == 25193 && player.getEquipment().getGlovesId() == 25194)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 34923 && player.getEquipment().getChestId() == 25191 && player.getEquipment().getLegsId() == 25192 && player.getEquipment().getBootsId() == 25193 && player.getEquipment().getGlovesId() == 25194)
            xpBoost *= 1.01;
        if (portable)
            xpBoost *= 1.1;
        if (player.jujuPotions.isActive(Pots.Effects.GUTHIXS_GIFT_JUJU) && Herblore.JUJU_INGREDIENTS.contains(ingredients))
            xpBoost *= 2;
        return xpBoost;
    }


    public static final Set<Ingredients> JUJU_INGREDIENTS = EnumSet.of(Ingredients.ERZILLE,

            Ingredients.ARGWAY,

            Ingredients.UGUNE,

            Ingredients.SHENGO,

            Ingredients.SAMADEN,

            Ingredients.CORRUPT_VINE,
            Ingredients.SHADOW_VINE,
            Ingredients.MARBLE_VINE,
            Ingredients.PLANT_TEETH,
            Ingredients.AQUATIC_VINE,
            Ingredients.OILY_VINE,
            Ingredients.DRACONIC_VINE,
            Ingredients.SARADOMIN_VINE,
            Ingredients.GUTHIX_VINE,
            Ingredients.ZAMORAK_VINE);

    public enum Ingredients {
//Ingredients(int itemId, int[] otherItems, int[] rewards, byte[] levels, double[] experience)

        GUAM(249, new int[]{Herblore.VIAL, Herblore.SWAMP_TAR}, new int[]{91, 10142}, new byte[]{0, 19}, new double[]{0, 30}),

        MARRENTILL(251, new int[]{Herblore.VIAL, Herblore.SWAMP_TAR}, new int[]{93, 10143}, new byte[]{0, 31}, new double[]{0, 42.5}),

        TARROMIN(253, new int[]{Herblore.VIAL, Herblore.SWAMP_TAR}, new int[]{95, 10144}, new byte[]{0, 39}, new double[]{0, 55}),

        HARRALANDER(255, new int[]{Herblore.VIAL, Herblore.SWAMP_TAR}, new int[]{97, 10145}, new byte[]{0, 44}, new double[]{0, 72.5}),

        RANARR(257, new int[]{Herblore.VIAL}, new int[]{99}, new byte[]{0}, new double[]{0}),

        FELLSTALK(21624, new int[]{Herblore.VIAL}, new int[]{21628}, new byte[]{0}, new double[]{0}),

        TOADFLAX(2998, new int[]{Herblore.VIAL, Herblore.COCONUT_MILK}, new int[]{3002, 5942}, new byte[]{0, 0}, new double[]{0, 0}),

        SPIRIT_WEED(12172, new int[]{Herblore.VIAL}, new int[]{12181}, new byte[]{0}, new double[]{0}),

        WERGALI(14854, new int[]{Herblore.VIAL}, new int[]{14856}, new byte[]{0}, new double[]{0}),

        IRIT(259, new int[]{Herblore.VIAL, Herblore.COCONUT_MILK}, new int[]{101, 5951}, new byte[]{0, 0}, new double[]{0, 0}),

        AVANTOE(261, new int[]{Herblore.VIAL, 2436, 145, 147, 149}, new int[]{103, 15308, 15309, 15310, 15311}, new byte[]{0, 88, 88, 88, 88}, new double[]{0, 220, 220, 220, 220}),

        KWUARM(263, new int[]{Herblore.VIAL}, new int[]{105}, new byte[]{0}, new double[]{0}),

        STARFLOWER(9017, new int[]{Herblore.VIAL}, new int[]{9019}, new byte[]{0}, new double[]{0}),

        SNAPDRAGON(3000, new int[]{Herblore.VIAL}, new int[]{3004}, new byte[]{0}, new double[]{0}),

        CADANTINE(265, new int[]{Herblore.VIAL}, new int[]{107}, new byte[]{0}, new double[]{0}),

        LANTADYME(2481, new int[]{Herblore.VIAL, 2442, 163, 165, 167}, new int[]{2483, 15316, 15317, 15318, 15319}, new byte[]{0, 90, 90, 90, 90}, new double[]{0, 240, 240, 240, 240}),

        DWARF_WEED(267, new int[]{Herblore.VIAL, 2440, 157, 159, 161}, new int[]{109, 15312, 15313, 15314, 15315}, new byte[]{0, 89, 89, 89, 89}, new double[]{0, 230, 230, 230, 230}),

        CACTUS_SPINE(6016, new int[]{Herblore.COCONUT_MILK}, new int[]{5936}, new byte[]{0}, new double[]{0}),

        TORSTOL(269, new int[]{Herblore.VIAL, 15309, 15313, 15317, 15321, 15325}, new int[]{111, 15332, 15332, 15332, 15332, 15332}, new byte[]{0, 96, 96, 96, 96, 96}, new double[]{0, 950, 950, 950, 950, 950}),

        EVIL_BARK(3239, new int[]{Herblore.VIAL}, new int[]{11501}, new byte[]{77}, new double[]{179.5}),

        EVIL_HERB(24783, new int[]{Herblore.VIAL}, new int[]{11505}, new byte[]{92}, new double[]{450.5}),

        EVIL_DUST(3325, new int[]{11501, 11505, 99}, new int[]{11433, 11509, 6952}, new byte[]{83, 98, RootOfEvil.HERBLORE_REQ}, new double[]{350.8, 850.5, 1500.5}),


        ERZILLE(19989, new int[]{JUJU_VIAL}, new int[]{19998}, new byte[]{54}, new double[]{50.0}),

        ARGWAY(19990, new int[]{JUJU_VIAL}, new int[]{20000}, new byte[]{59}, new double[]{75.0}),

        UGUNE(19991, new int[]{JUJU_VIAL}, new int[]{19999}, new byte[]{64}, new double[]{100.0}),

        SHENGO(19992, new int[]{JUJU_VIAL}, new int[]{20001}, new byte[]{67}, new double[]{125.0}),

        SAMADEN(19993, new int[]{JUJU_VIAL}, new int[]{20002}, new byte[]{71}, new double[]{150.0}),

        CORRUPT_VINE(19979, new int[]{19998}, new int[]{20024}, new byte[]{54}, new double[]{123.0}),
        SHADOW_VINE(19977, new int[]{20000}, new int[]{20028}, new byte[]{59}, new double[]{135.0}),
        MARBLE_VINE(19980, new int[]{19999}, new int[]{20012}, new byte[]{64}, new double[]{146.0}),
        PLANT_TEETH(19975, new int[]{20001}, new int[]{20008}, new byte[]{67}, new double[]{152.0}),
        AQUATIC_VINE(19976, new int[]{20001}, new int[]{20020}, new byte[]{70}, new double[]{158.0}),
        OILY_VINE(19972, new int[]{20002}, new int[]{20016}, new byte[]{71}, new double[]{160.0}),
        DRACONIC_VINE(19973, new int[]{20002}, new int[]{20004}, new byte[]{74}, new double[]{168.0}),
        SARADOMIN_VINE(19981, new int[]{20002}, new int[]{20032}, new byte[]{75}, new double[]{170.0}),
        GUTHIX_VINE(19982, new int[]{20002}, new int[]{20036}, new byte[]{75}, new double[]{170.0}),
        ZAMORAK_VINE(19983, new int[]{20002}, new int[]{20040}, new byte[]{75}, new double[]{170.0}),

        HARMONY_MOSS(32947, new int[]{20016, 20012, 20004, 20024, 20028, 20032, 20036, 20040, 20020},
                new int[]{32757, 32765, 32773, 32781, 32789, 32797, 32805, 32813, 35739},
                new byte[]{75, 77, 80, 82, 85, 87, 90, 91, 92},
                new double[]{120.0, 109.5, 126.5, 92.3, 101.3, 127.5, 127.5, 127.5, 127.5}),

        REDBERRIES(1951, new int[]{91}, new int[]{169}, new byte[]{3}, new double[]{30.0}),

        BLACK_BEAD(1474, new int[]{95}, new int[]{27514}, new byte[]{5}, new double[]{35.0}),

        WHITE_BEAD(1476, new int[]{95}, new int[]{27514}, new byte[]{5}, new double[]{35.0}),

        RED_BEAD(1470, new int[]{95}, new int[]{27514}, new byte[]{5}, new double[]{35.0}),

        YELLOW_BEAD(1472, new int[]{95}, new int[]{27514}, new byte[]{5}, new double[]{35.0}),

        BEAR_FUR(948, new int[]{93}, new int[]{133}, new byte[]{9}, new double[]{45.0}),

        HARRALANDER_UNF(97, new int[]{251, 9736}, new int[]{4419, 9741}, new byte[]{18, 36}, new double[]{59.5, 84}),

        FROG_SPAWN(5004, new int[]{14856}, new int[]{14840}, new byte[]{42}, new double[]{95.0}),

        CRUSHED_DRAGONSTONE(37914, new int[]{37973}, new int[]{37963}, new byte[]{57}, new double[]{123.5}),

        CAVE_NIGHTSHADE(2398, new int[]{Herblore.COCONUT_MILK}, new int[]{5939}, new byte[]{0}, new double[]{0}),

        EYE_OF_NEWT(221, new int[]{91, 101}, new int[]{121, 145}, new byte[]{0, 45}, new double[]{25, 100}),
//Ingredients(int itemId, int[] otherItems, int[] rewards, byte[] levels, double[] experience)

        UNICORN_HORN_DUST(235, new int[]{93, 101}, new int[]{175, 181}, new byte[]{5, 48}, new double[]{37.5, 106.3}),

        LIMPWURT_ROOT(225, new int[]{95, 105}, new int[]{115, 157}, new byte[]{12, 55}, new double[]{50, 125}),

        RED_SPIDER_EGGS(223, new int[]{97, 3004, 5936}, new int[]{127, 3026, 5937}, new byte[]{22, 63, 73}, new double[]{62.5, 142.5, 165}),

        BLAMISH_SNAIL_SLIME(1581, new int[]{97}, new int[]{1582}, new byte[]{25}, new double[]{80}),

        CHOCOLATE_DUST(1975, new int[]{97}, new int[]{3010}, new byte[]{26}, new double[]{67.5}),

        WHITE_BERRIES(239, new int[]{99, 107}, new int[]{133, 163}, new byte[]{30, 66}, new double[]{75, 150}),

        RUBIUM(12630, new int[]{91}, new int[]{12633}, new byte[]{31}, new double[]{55}),

        TOAD_LEGS(2152, new int[]{3002}, new int[]{3034}, new byte[]{34}, new double[]{80}),

        // GOAT_HORN_DUST(9736, new int[]{97}, new int[]{9741}, new byte[]{36}, new
        // double[]{84}),

        PHARMAKOS_BERRIES(11807, new int[]{3002}, new int[]{11810}, new byte[]{37}, new double[]{85}),

        SNAPE_GRASS(231, new int[]{99, 103}, new int[]{139, 151}, new byte[]{38, 50}, new double[]{87.5, 112.5}),

        COCKATRICE_EGG(12109, new int[]{12181}, new int[]{12142}, new byte[]{40}, new double[]{92}),

        FROGSPAWN(10961, new int[]{14856}, new int[]{14840}, new byte[]{40}, new double[]{92}),

        CHOPPED_ONION(1871, new int[]{101}, new int[]{18661}, new byte[]{46}, new double[]{0}),

        MORT_MYRE_FUNGUS(2970, new int[]{103}, new int[]{3018}, new byte[]{52}, new double[]{117.5}),

        SHRUNK_OGLEROOT(11205, new int[]{95}, new int[]{11204}, new byte[]{52}, new double[]{6}),

        KEBBIT_TEETH_DUST(10111, new int[]{103}, new int[]{10000}, new byte[]{53}, new double[]{120}),

        CRUSHED_GORAK_CLAW(9018, new int[]{9019}, new int[]{9022}, new byte[]{57}, new double[]{130}),

        WIMPY_FEATHER(11525, new int[]{14856}, new int[]{14848}, new byte[]{58}, new double[]{132}),

        DRAGON_SCALE_DUST(241, new int[]{105, 2483}, new int[]{187, 2454}, new byte[]{60, 69}, new double[]{137.5, 157.5}),

        YEW_ROOTS(6049, new int[]{5942, 3002}, new int[]{5945, 5945}, new byte[]{68, 68}, new double[]{155, 155}),

        WINE_OF_ZAMORAK(245, new int[]{109, 189}, new int[]{169, 28201}, new byte[]{72, 93}, new double[]{162.5, 180.0}),

        WINE_OF_SARADOMIN(28256, new int[]{6687}, new int[]{28193}, new byte[]{93}, new double[]{180.0}), WINE_OF_GUTHIX(28253, new int[]{4419}, new int[]{28209}, new byte[]{93}, new double[]{59.5}),

        POTATO_CACTUS(3138, new int[]{2483}, new int[]{3042}, new byte[]{76}, new double[]{172.5}),

        JANGERBERRIES(247, new int[]{111}, new int[]{189}, new byte[]{78}, new double[]{175}),

        MAGIC_ROOTS(6051, new int[]{5951, 101}, new int[]{5954, 5954}, new byte[]{79, 79}, new double[]{177.5, 177.5}),

        CRUSHED_BIRD_NEST(6693, new int[]{3002}, new int[]{6687}, new byte[]{81}, new double[]{180}),

        POISON_IVY_BERRIES(6018, new int[]{5939}, new int[]{5940}, new byte[]{82}, new double[]{190}),

        PAPAYA_FRUIT(5972, new int[]{3018}, new int[]{15301}, new byte[]{84}, new double[]{200}),

        PHOENIX_FEATHER(4621, new int[]{2452, 2454, 2456, 2458}, new int[]{15304, 15305, 15306, 15307}, new byte[]{85, 85, 85, 85}, new double[]{210, 210, 210, 210}),

        GROUND_MUD_RUNES(9594, new int[]{3040, 3042, 3044, 3046}, new int[]{15320, 15321, 15322, 15323}, new byte[]{91, 91, 91, 91}, new double[]{250, 250, 250, 250}),

        GRENWALL_SPIKES(12539, new int[]{2444, 169, 171, 173}, new int[]{15324, 15325, 15326, 15327}, new byte[]{92, 92, 92, 92}, new double[]{260, 260, 260, 260}),

        MORCHELLA_MUSHROOM(21622, new int[]{21628}, new int[]{21632}, new byte[]{94}, new double[]{190.0}),

        BONEMEAL(6810, new int[]{2434, 139, 141, 143}, new int[]{15328, 15329, 15330, 15331}, new byte[]{94, 94, 94, 94}, new double[]{270, 270, 270, 270}),

        BLOODWEED(37953, new int[]{Herblore.VIAL}, new int[]{37973}, new byte[]{0}, new double[]{0.0}),

        SEARING_ASHES(34159, new int[]{37973}, new int[]{37969}, new byte[]{82}, new double[]{185.0});

        private static final Map<Integer, Ingredients> ingredients = new HashMap<Integer, Ingredients>();

        static {
            for (Ingredients ingredient : Ingredients.values()) {
                ingredients.put(ingredient.itemId, ingredient);
            }
        }

        private final int itemId;
        private final int[] otherItems;
        private final int[] rewards;
        private final byte[] levels;
        private final double[] experience;

        Ingredients(int itemId, int[] otherItems, int[] rewards, byte[] levels, double[] experience) {
            this.itemId = itemId;
            this.otherItems = otherItems;
            this.rewards = rewards;
            this.levels = levels;
            this.experience = experience;
        }

        public static Ingredients forId(int itemId) {
            return ingredients.get(itemId);
        }

        public double[] getExperience() {
            return experience;
        }

        public int getItemId() {
            return itemId;
        }

        public byte[] getLevels() {
            return levels;
        }

        public int[] getOtherItems() {
            return otherItems;
        }

        public int[] getRewards() {
            return rewards;
        }

        public int getSlot(int itemId) {
            for (int i = 0; i < otherItems.length; i++) {
                if (itemId == otherItems[i])
                    return i;
            }
            return -1;
        }

    }

    public enum RawIngredient {

        UNICORN_HORN(237, new Item(235, 1)),

        CHOCOLATE_BAR(1973, new Item(1975, 1)),

        KEBBIT_TEETH(10109, new Item(10111, 1)),

        GORAK_CLAW(9016, new Item(9018, 1)),

        BIRDS_NEST(5075, new Item(6693, 1)),

        DESERT_GOAT_HORN(9735, new Item(9736, 1)),

        BLUE_DRAGON_SCALES(243, new Item(241, 1)),

        SPRING_SQ_IRK(10844, new Item(10848, 1)),

        SUMMER_SQ_IRK(10845, new Item(10849, 1)),

        AUTUMN_SQ_IRK(10846, new Item(10850, 1)),

        WINTER_SQ_IRK(10847, new Item(10851, 1)),

        CHARCOAL(973, new Item(704, 1)),

        RUNE_SHARDS(6466, new Item(6467, 1)),

        ASHES(592, new Item(8865, 1)),

        POISON_KARAMBWAN(3146, new Item(3152, 1)),

        SUQAH_TOOTH(9079, new Item(9082, 1)),

        FISHING_BAIT(313, new Item(12129, 1)),

        DIAMOND_ROOT(14703, new Item(14704, 1)),

        BLACK_MUSHROOM(4620, new Item(4622, 1)),

        MUD_RUNES(4698, new Item(9594, 1)),

        DUST_OF_ARMADYL(21776, new Item(21774, 1)),

        WYVERN_BONES(6812, new Item(6810, 1)),

        URCHIN_SMALL(35721, new Item(32622, 10)),

        URCHIN_MED(35722, new Item(32622, 10)),

        URCHIN_LARGE(35723, new Item(32622, 10));

        private static final Map<Integer, RawIngredient> rawIngredients = new HashMap<Integer, RawIngredient>();

        static {
            for (RawIngredient rawIngredient : RawIngredient.values())
                rawIngredients.put(rawIngredient.rawId, rawIngredient);
        }

        private final int rawId;
        private final Item crushedItem;

        RawIngredient(int rawId, Item crushedItem) {
            this.rawId = rawId;
            this.crushedItem = crushedItem;
        }

        public static RawIngredient forId(int itemId) {
            return rawIngredients.get(itemId);
        }

        public Item getCrushedItem() {
            return crushedItem;
        }

        public int getRawId() {
            return rawId;
        }
    }
}
