package com.rs.game.player.actions;

import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.ForceTalk;
import com.rs.game.Graphics;
import com.rs.game.WorldTile;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.player.Equipment;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.hunter.TrapAction;
import com.rs.game.player.content.FishingSpotsHandler;
import com.rs.game.player.content.Pots;
import com.rs.game.player.content.petperks.PetPerkHandler;
import com.rs.game.player.content.skillingcontracts.impl.FishingContractList;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Fishing extends Action {
    private com.rs.game.player.client.Native950Fishing.Journey nativeFishing;

    /*
     * The Fishing object is a subclass of "Action" when we start fishing, we use,
     * player.getActionManager().start(new Fishing(spot, npc))
     *
     * that will run
     *
     * public Fishing() {} - this is called a constructor, called when we call new
     * Object()
     *
     * public Object(int id) { // do code }
     *
     * new Object(1); // this will run the stuff in do code
     *
     * eventually, through the start() method, we get to the addFish() method
     *
     * once addFish() is called, it basically starts executing the code inside of
     * that function block
     *
     * Item fish = new Item(spot.getFish()[fishId].getId(),
     *
     * this variable only exists inside of the addFish() function, you cannot access
     * it otherwise this is done so they can create an Item object that they will
     * return as the product every time addFish() is run
     *
     * so technically, when addFish() is run, the Item object "fish" is different
     * every execution
     *
     */

    /**
     * These are variables in the Class scope (they're defined in the body of the
     * Class) most aren't set to a value, because when the constructor is run, they
     * will be set
     *
     * @Getter public FishingSpots spot; private NPC npc
     *         <p>
     *         public Fishing(FishingSpots spot, NPC npc) { this.spot = spot;
     *         this.npc = npc; tile = new WorldTile(npc); }
     *         <p>
     *         that means, for example, we cannot call Fishing.spot from outside of
     *         the class, because its null we would need to call getSpot() or spot
     *         on an instance of the Fishing object, so for example
     *         <p>
     *         Fishing fishAction = new Fishing(FishingSpots.get(npc.getId(), npc);
     *         <p>
     *         fishAction.getSpot() because fishAction is an instance of the Fishing
     *         object
     */
    private final FishingSpots spot;
    private final NPC npc;
    private final WorldTile tile;
    private int fishId;
    private boolean usingChompa;

    public Fishing(FishingSpots spot, NPC npc) {
        this.spot = spot;
        this.npc = npc;
        tile = new WorldTile(npc);
    }

    private boolean addFish(Player player) {
        int skillChompa = TrapAction.getSkillChompa(player);
        int rolledFish = rollFish(player);
        if (rolledFish == -1) {
            if (skillChompa != -1) {
                player.getEquipment().removeAmmo(skillChompa, -1);
                player.getSkills().addXp(Skills.FISHING, spot.getFish()[fishId].getXp() * fishingSuit(player) * 0.05);
            }
            return false;
        }
        fishId = rolledFish;
        boolean multipleCatch = rollAgilityMultipleCatch(player, spot.getFish()[fishId]);
        boolean furySharkExtraCatch = hasFurySharkOutfit(player) && ThreadLocalRandom.current().nextInt(100) < 10;

        int fishAmount = 1 + (multipleCatch ? 1 : 0) + (furySharkExtraCatch ? 1 : 0);
        Item fish = new Item(spot.getFish()[fishId].getId(), fishAmount);

        // Agility double-catches give no extra XP; the Fury shark outfit's extra catch does.
        double totalXp = spot.getFish()[fishId].getXp();
        double playerXp = totalXp * (furySharkExtraCatch ? 2 : 1);
        FishingContractList.listen(player, spot.getFish()[fishId], fish.getAmount());

        if (skillChompa != -1) {
            npc.setNextGraphics(new Graphics(3037));
            player.getEquipment().removeAmmo(skillChompa, -1);
            totalXp *= 1.1;
            playerXp *= 1.1;
        }

        player.sendMessage(getMessage(player, fish), true);

        if (spot.getBait() != -1 && !player.getPerkManager().hasPerkActive(DonationPerk.MASTER_FISHERMAN)) {
            if (hasFurySharkOutfit(player) && Utils.random(100) < 55)
                player.sendMessage(Colors.ORANGE + "<shad=000000>Your Fury shark outfit has saved you some bait.", true);
            else
                player.getInventory().deleteItem(spot.getBait(), 1);
        }

        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        boolean hasAugmentedTool = weapon != null && weapon.getInventionData() != null
                && (weapon.getName().toLowerCase().contains("fishing rod-o-matic")
                || weapon.getName().toLowerCase().contains("crystal fishing rod")
                || weapon.getName().toLowerCase().contains("tavia's fishing rod"));
        if (!hasAugmentedTool)
            weapon = null;

        player.getInventionManager().processSkillXp(Skills.FISHING, totalXp * fishingSuit(player)
                * (player.getPerkManager().hasPerkActive(DonationPerk.MASTER_FISHERMAN) ? 2 : 1), weapon);
        player.getSkills().addXp(Skills.FISHING, playerXp * fishingSuit(player)
                * (player.getPerkManager().hasPerkActive(DonationPerk.MASTER_FISHERMAN) ? 2 : 1));

        ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);

        if (shouldConsumeFish(player))
            player.setNextGraphics(new Graphics(5420));
        else {
            if (player.getInventionManager().procGathering(weapon,
                    new Item(fish.getId(), fish.getAmount()), Skills.FISHING,
                    totalXp * fishingSuit(player)
                            * (player.getPerkManager().hasPerkActive(DonationPerk.MASTER_FISHERMAN) ? 2 : 1))) {
                int addItem = fish.getId();
                if (addItem == 383 && isJujuFishingActive(player)) {
                    addJujuSharkCatch(player, fish.getAmount());
                } else {
                    player.getInventory().addItem(addItem, fish.getAmount());
                }
            }
        }

        PetPerkHandler.handleExtraArms(player, fish);

        if ((Utils.getRandom(50) == 0 && FishingSpotsHandler.moveSpot(npc)))
            player.setNextAnimation(new Animation(-1));
        return true;
    }


    private boolean checkAll(Player player) {
        if (player.getInterfaceManager().containsScreenInter() || player.getInterfaceManager().containsInventoryInter()) {
            player.sendMessage("Please finish what you're doing before doing this action.");
            return false;
        }

        if (player.getSkills().getLevel(Skills.FISHING) < spot.getFish()[fishId].getLevel()) {
            player.getDialogueManager().startDialogue("SimpleMessage", "You need a fishing level of " + spot.getFish()[fishId].getLevel() + " to fish here.");
            return false;
        }
        if (!shouldConsumeFish(player) && !player.getInventory().hasFreeSlots()) {
            player.setNextAnimation(new Animation(-1));
            player.sendMessage("Inventory full. To make more room, sell, drop or bank something.");
            return false;
        }
        if (tile.getX() != npc.getX() || tile.getY() != npc.getY())
            return false;

        int skillChompa = TrapAction.getSkillChompa(player);
        if (skillChompa != -1) {
            int requiredLevel = skillChompa == 40995 ? 71 : 31 + (10 * (skillChompa - 31595));
            if (player.getSkills().getLevel(Skills.FISHING) < requiredLevel) {
                player.getDialogueManager().startDialogue("SimpleMessage", "You need a fishing level of " + requiredLevel + " to use this skillchompa.");
                return false;
            }
            return true;
        }
        if (spot.getBait() != -1 && !player.getInventory().containsOneItem(spot.getBait())
                && !player.getPerkManager().hasPerkActive(DonationPerk.MASTER_FISHERMAN)) {
            player.sendMessage("You don't have " + new Item(spot.getBait()).getDefinitions().getName().toLowerCase() + " to fish here.");
            return false;
        }
        if (!hasRequiredTool(player)) {
            if (!hasSharkOutfit(player) && !hasFurySharkOutfit(player)) {
                player.sendMessage("You don't have the required tool to use this Fishing spot.");
                return false;
            }
        }
        player.getInterfaceManager().closeScreenInterface();
        return true;
    }

    private int getFishingDelay(Player player) {
        return isSlowFishingSpot() ? 5 : 4;
    }

    private String getMessage(Player player, Item fish) {
        player.addFishCaught(fish.getAmount());
        if (spot.getFish()[fishId] == Fish.ANCHOVIES || spot.getFish()[fishId] == Fish.SHRIMP)
            return "You manage to catch some " + fish.getDefinitions().getName().toLowerCase() + "; fish caught: " + Colors.RED + Utils.getFormattedNumber(player.getFishCaught()) + "</col>.";
        else if (fish.getAmount() > 1)
            return "You manage to catch " + fish.getAmount() + " " + fish.getDefinitions().getName().toLowerCase() + "; fish caught: " + Colors.RED + Utils.getFormattedNumber(player.getFishCaught()) + "</col>.";
        else
            return "You manage to catch a " + fish.getDefinitions().getName().toLowerCase() + "; fish caught: " + Colors.RED + Utils.getFormattedNumber(player.getFishCaught()) + "</col>.";
    }

    private boolean isSlowFishingSpot() {
        switch (spot) {
            case CAVEFISH_SHOAL:
            case ROCKTAIL_SHOAL:
            case NET2:
            case HARPOON:
            case HARPOON2:
            case GREAT_WHITE:
                return true;
            default:
                return false;
        }
    }

    private int rollFish(Player player) {
        for (int index = spot.getFish().length - 1; index >= 0; index--) {
            Fish fish = spot.getFish()[index];
            if (player.getSkills().getLevel(Skills.FISHING) < fish.getLevel())
                continue;
            if (ThreadLocalRandom.current().nextInt(256) < getFishRoll(player, fish))
                return index;
        }
        return -1;
    }

    private int getFishRoll(Player player, Fish fish) {
        int level = Math.max(1, player.getSkills().getLevel(Skills.FISHING));
        int familiarBonus = player.getFamiliar() != null ? Math.max(0, getSpecialFamiliarBonus(player.getFamiliar().getId())) : 0;
        level += familiarBonus;
        double lowChance = fish.getLowChance();
        double highChance = fish.getHighChance();
        highChance = Math.floor(highChance * player.getAuraManager().getFishingAccurayMultiplier());
        if (hasSharkOutfit(player))
            highChance = Math.floor(highChance * 1.05);
        if (hasFurySharkOutfit(player))
            highChance = Math.floor(highChance * 1.07);
        if (player.jujuPotions.isActive(Pots.Effects.PERFECT_FISHING_JUJU))
            highChance = Math.floor(highChance * 1.05);
        highChance = Math.floor(highChance * getFishingToolChanceMultiplier(player));
        highChance = Math.floor(highChance * getSkillchompaChanceMultiplier(player));
        int roll = (int) Math.floor(lowChance + ((Math.min(level, 99) - 1) * (highChance - lowChance) / 98.0));
        return Math.max(1, Math.min(255, roll));
    }

    private int getFirstAvailableFish(Player player) {
        for (int index = 0; index < spot.getFish().length; index++) {
            if (player.getSkills().getLevel(Skills.FISHING) >= spot.getFish()[index].getLevel())
                return index;
        }
        return 0;
    }

    private boolean rollAgilityMultipleCatch(Player player, Fish fish) {
        if (fish != Fish.TUNA && fish != Fish.SWORDFISH && fish != Fish.SHARK)
            return false;
        int chance = getAgilityMultipleCatchChance(player, fish);
        return chance > 0 && ThreadLocalRandom.current().nextInt(100) < chance;
    }

    private int getAgilityMultipleCatchChance(Player player, Fish fish) {
        int agility = player.getSkills().getLevel(Skills.AGILITY);
        int minimum = fish.getLevel();
        int maximum = fish == Fish.TUNA ? 70 : fish == Fish.SWORDFISH ? 80 : 99;
        if (agility < minimum)
            return 0;
        return Math.max(1, Math.min(10, ((Math.min(agility, maximum) - minimum + 1) * 10) / (maximum - minimum + 1)));
    }

    private boolean usesFishingRodTool() {
        return spot.getTool() == 307 || spot.getTool() == 309;
    }

    private Item getEquippedFishingRod(Player player) {
        if (!usesFishingRodTool())
            return null;
        Item weapon = player.getEquipment().getItem(Equipment.SLOT_WEAPON);
        if (weapon == null)
            return null;
        String name = weapon.getName().toLowerCase();
        if (name.contains("fishing rod-o-matic") || name.contains("crystal fishing rod") || name.contains("tavia's fishing rod"))
            return weapon;
        return null;
    }

    private boolean hasRequiredTool(Player player) {
        return player.getInventory().containsOneItem(spot.getTool())
                || player.getToolBelt().contains(spot.getTool())
                || getEquippedFishingRod(player) != null;
    }

    private boolean shouldConsumeFish(Player player) {
        if (!player.consumeFish || !(hasSharkOutfit(player) || hasFurySharkOutfit(player)))
            return false;
        Fish fish = spot.getFish()[fishId];
        return fish != Fish.SMALL_URCHIN && fish != Fish.MEDIUM_URCHIN && fish != Fish.LARGE_URCHIN;
    }

    private boolean isJujuFishingActive(Player player) {
        return player.jujuPotions.isActive(Pots.Effects.FISHING_JUJU)
                || player.jujuPotions.isActive(Pots.Effects.PERFECT_FISHING_JUJU);
    }

    private void addJujuSharkCatch(Player player, int amount) {
        int baronSharks = 0;
        for (int i = 0; i < amount; i++) {
            if (ThreadLocalRandom.current().nextInt(3) == 0)
                baronSharks++;
        }
        if (baronSharks > 0) {
            player.sendMessage("Through the power of juju, you catch a baron shark!");
            player.getInventory().addItem(19947, baronSharks);
        }
        if (baronSharks < amount)
            player.getInventory().addItem(383, amount - baronSharks);
    }

    private double getFishingToolChanceMultiplier(Player player) {
        Item weapon = getEquippedFishingRod(player);
        if (weapon == null)
            return 1.0;
        String name = weapon.getName().toLowerCase();
        if (name.contains("tavia's fishing rod"))
            return 1.10;
        if (name.contains("crystal fishing rod"))
            return 1.05;
        return 1.0;
    }

    private double getSkillchompaChanceMultiplier(Player player) {
        switch (TrapAction.getSkillChompa(player)) {
            case 31595:
                return 1.01;
            case 31596:
                return 1.02;
            case 31597:
                return 1.03;
            case 31598:
                return 1.04;
            case 40995:
                return 1.05;
            default:
                return 1.0;
        }
    }

    private int getSpecialFamiliarBonus(int id) {
        switch (id) {
            case 6796:
                return 1;
            case 6991:
                return 3;
            case 6850:
                return 4;
        }
        return -1;
    }

    private static final String getRandomMessage(Player player) {
        int random = Utils.random(24);
        switch (random) {
        case 0:
            return player.getDisplayName() + " is your character's name.";
        case 1:
            return player.getDisplayName() + " casts his net widely.";
        case 2:
            return player.getDisplayName() + " makes me laugh.";
        case 3:
            return player.getDisplayName() + ", have you been working out?";
        case 4:
            return player.getDisplayName() + "... I'm hooked on you.";
        case 5:
            return player.getDisplayName() + "... You are my inspiration.";
        case 6:
            return player.getDisplayName() + "... You're an angler extraordinaire!";
        case 7:
            return player.getDisplayName() + ", has a nice smile.";
        case 8:
            return player.getDisplayName() + ", is that a new perfume?";
        case 9:
            return "Wow, " + player.getDisplayName() + " you have some mad skills.";
        case 10:
            return "Do you come here often, " + player.getDisplayName() + "?";
        case 11:
            return "I love what you're wearing, " + player.getDisplayName() + ".";
        case 12:
            return "Looking good, " + player.getDisplayName() + "!";
        case 13:
            return "Nice cast, " + player.getDisplayName() + ".";
        case 14:
            return "Nobody fishes like you you " + player.getDisplayName() + ".";
        case 15:
            return "Ooh, good catch, " + player.getDisplayName() + "!";
        case 16:
            return "What beautiful eyes " + player.getDisplayName() + " has.";
        case 17:
            return "You most certainly are a good fishes, " + player.getDisplayName() + ".";
        case 18:
            return "What would I do without you, " + player.getDisplayName() + "?";
        case 19:
            return "You cast a mean hook, " + player.getDisplayName() + ".";
        case 20:
            return "You rock my world, " + player.getDisplayName() + ".";
        case 21:
            return "You've done this before, haven't you, " + player.getDisplayName() + "?";
        case 22:
            return "You've got the best fish, " + player.getDisplayName() + ".";
        default:
            return "You're gonna need a bigger inventory, " + player.getDisplayName() + ".";
        }
    }

    @Override
    public boolean process(Player player) {
        if(player.isNative950())return nativeFishing!=null&&nativeFishing.process(player);
        if (player.getEquipment().getHatId() == 24431) {
            if (Utils.random(75) == 1) {
                if (Utils.random(25) == 1) {
                    npc.setNextForceTalk(new ForceTalk("Have you have a haircut, " + player.getDisplayName() + "?"));
                    CoresManager.getServiceProvider().executeWithDelay(new Runnable() {
                        @Override
                        public void run() {
                            npc.setNextForceTalk(new ForceTalk("It suits you."));
                        }
                    }, 2, TimeUnit.SECONDS);
                } else
                    npc.setNextForceTalk(new ForceTalk(getRandomMessage(player)));
            }
        }
        return checkAll(player);
    }

    @Override
    public int processWithDelay(Player player) {
        if(player.isNative950())return nativeFishing==null?-1:nativeFishing.catchFish(player);
        boolean caught = addFish(player);
        if (usingChompa && TrapAction.getSkillChompa(player) == -1) {
            player.setNextAnimation(new Animation(-1));
            player.sendMessage("You have run out of skillchompas.");
            return -1;
        }
        startFishingAnimation(player);
        player.setNextFaceEntity(npc);
        if (!caught)
            return getFishingDelay(player);
        boolean isPriffFishing = spot.getFish()[fishId].getId() == 35721 || spot.getFish()[fishId].getId() == 35722 || spot.getFish()[fishId].getId() == 35723;
        if (Utils.random(100) == 0 && (isPriffFishing || hasFurySharkOutfit(player))) {
            player.sendMessage("You feel too exhausted from Fishing and rest for a little..");
            player.setNextAnimation(new Animation(-1));
            return -1;
        }
        return getFishingDelay(player);
    }

    @Override
    public boolean start(Player player) {
        if(player.isNative950()){nativeFishing=new com.rs.game.player.client.Native950Fishing.Journey(spot,npc);
            boolean started=nativeFishing.start(player);if(started)setActionDelay(player,nativeFishing.delay());return started;}
        fishId = getFirstAvailableFish(player);
        if (!checkAll(player))
            return false;
        int skillChompa = TrapAction.getSkillChompa(player);
        usingChompa = skillChompa != -1;
        startFishingAnimation(player);
        player.sendMessage(skillChompa != -1 ? "You throw a skillchompa at the water." : "You attempt to capture a fish...", true);
        if (player.fishingDelay > 0) {
            setActionDelay(player, player.fishingDelay);
        } else {
            player.fishingDelay = getFishingDelay(player);
            setActionDelay(player, player.fishingDelay);
        }
        return true;
    }

    @Override
    public void stop(final Player player) {
        if(player.isNative950()){if(nativeFishing!=null)nativeFishing.stop(player);setActionDelay(player,3);return;}
        player.setNextFaceEntity(null);
        setActionDelay(player, 3);
        player.fishingDelay = -1;
    }

    /**
     * Starts the fishing action animation.
     *
     * @param player The player starting.
     */
    private void startFishingAnimation(Player player) {
        int skillChompa = TrapAction.getSkillChompa(player);
        if (skillChompa != -1) {
            player.setNextAnimationNoPriority(new Animation(23793));// 3037
            return;
        }
        Item weapon = getEquippedFishingRod(player);
        if (weapon != null) {
            String name = weapon.getName().toLowerCase();
            if (name.contains("fishing rod-o-matic")) {
                player.setNextAnimation(new Animation(31055));//incorrect
            }
            else if(name.contains("crystal fishing rod")) {
                if ((hasSharkOutfit(player) || hasFurySharkOutfit(player)) && player.consumeFish)
                    player.setNextAnimationNoPriority(new Animation(26017));
                else {
                    if (player.getAnimations().hasDeepFishing && player.getAnimations().deepFishing)
                        player.setNextAnimation(new Animation(28292));
                    else if (player.getAnimations().hasArcaneFishing && player.getAnimations().arcaneFishing) {
                        player.setNextAnimation(new Animation(20298));
                        npc.setNextGraphics(new Graphics(4007));
                    } else
                        player.setNextAnimation(spot.getAnimation().getIds()[0] == 622 ? new Animation(28292) : spot.getAnimation());
                }
            } else if(name.contains("tavia's fishing rod")) {
                if ((hasSharkOutfit(player) || hasFurySharkOutfit(player)) && player.consumeFish)
                    player.setNextAnimationNoPriority(new Animation(26017));
                else {
                    if (player.getAnimations().hasDeepFishing && player.getAnimations().deepFishing)
                        player.setNextAnimation(new Animation(31417));
                    else if (player.getAnimations().hasArcaneFishing && player.getAnimations().arcaneFishing) {
                        player.setNextAnimation(new Animation(20298));
                        npc.setNextGraphics(new Graphics(4007));
                    } else
                        player.setNextAnimation(spot.getAnimation().getIds()[0] == 622 ? new Animation(31417) : spot.getAnimation());
                }
            }
            return;
        }
        if ((hasSharkOutfit(player) || hasFurySharkOutfit(player)) && player.consumeFish)
            player.setNextAnimationNoPriority(new Animation(26017));
        else {
            if (player.getAnimations().hasDeepFishing && player.getAnimations().deepFishing)
                player.setNextAnimation(new Animation(17084));
            else if (player.getAnimations().hasArcaneFishing && player.getAnimations().arcaneFishing) {
                player.setNextAnimation(new Animation(20298));
                npc.setNextGraphics(new Graphics(4007));
            } else
                player.setNextAnimation(spot.getAnimation());
        }
    }

    /**
     * XP modifier by wearing items.
     *
     * @param player The player.
     * @return the XP modifier.
     */
    private double fishingSuit(Player player) {
        double xpBoost = 1.0;
        if (hasSharkOutfit(player))
            xpBoost *= 1.05;
        if (hasFurySharkOutfit(player))
            xpBoost *= 1.07;
        if (player.getEquipment().getHatId() == 24427)
            xpBoost *= 1.01;
        if (player.getEquipment().getChestId() == 24428)
            xpBoost *= 1.01;
        if (player.getEquipment().getLegsId() == 24429)
            xpBoost *= 1.01;
        if (player.getEquipment().getBootsId() == 24430)
            xpBoost *= 1.01;
        if (player.getEquipment().getHatId() == 24427 && player.getEquipment().getChestId() == 24428 && player.getEquipment().getLegsId() == 24429 && player.getEquipment().getBootsId() == 24430)
            xpBoost *= 1.01;
        return xpBoost;
    }

    /**
     * Shark Outfits. http://runescape.wikia.com/wiki/Shark_outfit
     */
    private boolean hasSharkOutfit(Player player) {
        if (player.getEquipment().getHatId() == 34200 && player.getEquipment().getChestId() == 34201 && player.getEquipment().getLegsId() == 34202 && player.getEquipment().getGlovesId() == 34203 && player.getEquipment().getBootsId() == 34204)
            return true;
        if (player.getEquipment().getHatId() == 34205 && player.getEquipment().getChestId() == 34206 && player.getEquipment().getLegsId() == 34207 && player.getEquipment().getGlovesId() == 34208 && player.getEquipment().getBootsId() == 34209)
            return true;
        return player.getEquipment().getHatId() == 34210 && player.getEquipment().getChestId() == 34211 && player.getEquipment().getLegsId() == 34212 && player.getEquipment().getGlovesId() == 34213 && player.getEquipment().getBootsId() == 34214;
    }

    private boolean hasFurySharkOutfit(Player player) {
        return player.getEquipment().getHatId() == 34215 && player.getEquipment().getChestId() == 34216 && player.getEquipment().getLegsId() == 34217 && player.getEquipment().getGlovesId() == 34218 && player.getEquipment().getBootsId() == 34219;
    }

    public enum Fish {
        // fish(id, level req, exp, low chance, high chance)
        ANCHOVIES(321, 15, 40.0, 24, 128),

        BASS(363, 46, 100.0, 3, 40),

        COD(341, 23, 45.0, 4, 55),

        CAVE_FISH(15264, 85, 300.0, 1, 36),

        HERRING(345, 10, 30.0, 24, 128),

        LOBSTER(377, 40, 90.0, 6, 95),

        MACKEREL(353, 16, 20.0, 5, 65),

        MANTA(389, 81, 200.0, 1, 25),

        MONKFISH(7944, 62, 120.0, 48, 90),

        PIKE(349, 25, 60.0, 16, 96),

        SALMON(331, 30, 70.0, 16, 96),

        SARDINES(327, 5, 20.0, 32, 192),

        SEA_TURTLE(395, 79, 240.0, 1, 25),

        SEAWEED(401, 16, 1.0, 10, 10),

        OYSTER(407, 16, 10.0, 3, 7),

        SHARK(383, 76, 110.0, 3, 40),

        SHRIMP(317, 1, 10.0, 48, 256),

        SWORDFISH(371, 50, 100.0, 4, 48),

        TROUT(335, 20, 50.0, 32, 192),

        TUNA(359, 35, 80.0, 8, 64),

        CAVEFISH(15264, 85, 300.0, 1, 36),

        ROCKTAIL(15270, 90, 380.0, 1, 32),

        KARAMBWAN(3142, 65, 105.0, 100, 250),

        SMALL_URCHIN(35721, 93, 310.0, 10, 55),

        MEDIUM_URCHIN(35722, 95, 330.0, 10, 55),

        LARGE_URCHIN(35723, 97, 350.0, 10, 55),

        GREAT_WHITE_SHARK(34727, 80, 130.0, 3, 40);

        private final int id, level, lowChance, highChance;
        private final double xp;

        Fish(int id, int level, double xp, int lowChance, int highChance) {
            this.id = id;
            this.level = level;
            this.xp = xp;
            this.lowChance = lowChance;
            this.highChance = highChance;
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

        public int getLowChance() {
            return lowChance;
        }

        public int getHighChance() {
            return highChance;
        }
    }

    public enum FishingSpots {

        CAVEFISH_SHOAL(8841, 1, 307, 313, new Animation(622), Fish.CAVE_FISH),

        ROCKTAIL_SHOAL(8842, 1, 307, 15263, new Animation(622), Fish.ROCKTAIL),

        NET(327, 1, 303, -1, new Animation(621), Fish.SHRIMP, Fish.ANCHOVIES),

        BAIT(327, 2, 307, 313, new Animation(622), Fish.SARDINES, Fish.HERRING),

        LURE(328, 1, 309, 314, new Animation(622), Fish.TROUT, Fish.SALMON),

        LURE2(329, 1, 309, 314, new Animation(622), Fish.TROUT, Fish.SALMON),

        BAIT2(328, 2, 307, 313, new Animation(622), Fish.PIKE),

        BAIT3(329, 2, 307, 313, new Animation(622), Fish.PIKE, Fish.CAVE_FISH),

        CAGE(6267, 1, 301, -1, new Animation(619), Fish.LOBSTER),

        CAGE2(312, 1, 301, -1, new Animation(619), Fish.LOBSTER),

        HARPOON(312, 2, 311, -1, new Animation(618), Fish.TUNA, Fish.SWORDFISH),

        BIG_NET(313, 1, 305, -1, new Animation(620), Fish.MACKEREL, Fish.COD, Fish.BASS, Fish.SEAWEED, Fish.OYSTER),

        HARPOON2(313, 2, 311, -1, new Animation(618), Fish.SHARK),

        SPECIAL(1405, 1, 303, -1, new Animation(621), Fish.SEA_TURTLE),

        SPECIAL2(1405, 2, 311, -1, new Animation(618), Fish.MANTA),

        NET2(952, 1, 303, -1, new Animation(621), Fish.MONKFISH),

        LURE_PRIFF_I(21778, 1, 307, 313, new Animation(622), Fish.SMALL_URCHIN),

        LURE_PRIFF_II(21779, 1, 307, 313, new Animation(622), Fish.MEDIUM_URCHIN),

        LURE_PRIFF_III(21780, 1, 307, 313, new Animation(622), Fish.LARGE_URCHIN),

        KARAM_JUNGLE(6996, 1, 3157, -1, new Animation(1193), Fish.KARAMBWAN),

        GREAT_WHITE(1178, 1, 311, -1, new Animation(618), Fish.GREAT_WHITE_SHARK);

        static final Map<Integer, FishingSpots> spot = new HashMap<Integer, FishingSpots>();

        static {
            for (FishingSpots spots : FishingSpots.values())
                spot.put(spots.id | spots.option << 24, spots);
        }

        private final Fish[] fish;
        private final int id, option, tool, bait;
        private final Animation animation;

        FishingSpots(int id, int option, int tool, int bait, Animation animation, Fish... fish) {
            this.id = id;
            this.tool = tool;
            this.bait = bait;
            this.animation = animation;
            this.fish = fish;
            this.option = option;
        }

        public static FishingSpots forId(int id) {
            return spot.get(id);
        }

        public Animation getAnimation() {
            return animation;
        }

        public int getBait() {
            return bait;
        }

        public Fish[] getFish() {
            return fish;
        }

        public int getId() {
            return id;
        }

        public int getOption() {
            return option;
        }

        public int getTool() {
            return tool;
        }
    }
}
