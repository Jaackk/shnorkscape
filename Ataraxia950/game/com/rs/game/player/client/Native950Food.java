package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.content.Foods.Food;
import com.rs.utils.Utils;
import java.security.MessageDigest;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Ordinary eating through the910 food table, with950 inventory/animation/health boundaries. */
public final class Native950Food {
    // Explicit effect-free rows: special boosts, minigame foods, percentage cookies and
    // familiar/perk effects need their own ports, even when their950 menu also says Eat.
    private static final EnumSet<Food> ORDINARY = EnumSet.of(
            Food.CRAYFISH, Food.ANCHOVIE, Food.SHRIMP, Food.KARAMBWANJI, Food.SARDINE, Food.KARAMBWANI,
            Food.SLIMY_EEL, Food.RAINBOW_FISH, Food.CAVE_EEL, Food.LAVA_EEL, Food.HERRING, Food.MACKEREL,
            Food.TROUT, Food.COD, Food.PIKE, Food.SALMON, Food.TUNA, Food.LOBSTER, Food.BASS,
            Food.SWORDFISH, Food.MONKFISH, Food.SHARK, Food.TURTLE, Food.MANTA, Food.CAVEFISH,
            Food.GREAT_WHITE_SHARK, Food.ROCKTAIL,
            Food.CHICKEN, Food.MEAT, Food.RABIT, Food.ROAST_RABIT, Food.ROASTED_BIRD_MEAT, Food.CRAB_MEAT,
            Food.ROASTED_BEAST_MEAT, Food.CHOMPY, Food.JUBBLY, Food.OOMILE,
            Food.REDBERRY_PIE_FULL, Food.REDBERRY_PIE_HALF, Food.MEAT_PIE_FULL, Food.MEAT_PIE_HALF,
            Food.APPLE_PIE_FULL, Food.APPLE_PIE_HALF, Food.STEW, Food.CURRY,
            Food.PLAIN_PIZZA_FULL, Food.PLAIN_PIZZA_HALF, Food.MEAT_PIZZA_FULL, Food.MEAT_PIZZA_HALF,
            Food.ANCHOVIE_PIZZA_FULL, Food.ANCHOVIE_PIZZA_HALF, Food.PINEAPPLE_PIZZA_FULL, Food.PINEAPPLE_PIZZA_HALF,
            Food.SPICEY_SAUCE, Food.CHILLI_CON_CARNIE, Food.SCRAMBLED_EGG, Food.EGG_AND_TOMATO,
            Food.FRIED_ONIONS, Food.MUSHROOM_AND_ONIONS, Food.FRIED_MUSHROOMS, Food.TUNA_AND_CORN,
            Food.BAKED_POTATO, Food.POTATO_WITH_BUTTER, Food.CHILLI_POTATO, Food.POTATO_WITH_CHEESE,
            Food.EGG_POTATO, Food.MUSHROOM_AND_ONION_POTATO, Food.TUNA_POTATO,
            Food.TOAD_CRUNCHIES, Food.SPICY_CRUNCHIES, Food.WORM_CRUNCHIES, Food.CHOCOCHIP_CRUNCHIES,
            Food.FRUIT_BATTA, Food.TOAD_BATTA, Food.WORM_BATTA, Food.VEGETABLE_BATTA, Food.CHEESE_AND_TOMATO_BATTA,
            Food.WORM_HOLE, Food.VEG_BALL, Food.PRE_MADE_VEG_BALL, Food.TANGLED_TOAD_LEGS, Food.CHOCOLATE_BOMB,
            Food.CAKE, Food.TWO_THIRDS_CAKE, Food.SLICE_OF_CAKE, Food.CHOCOLATE_CAKE,
            Food.TWO_THIRDS_CHOCOLATE_CAKE, Food.CHOCOLATE_SLICE, Food.EDIBLE_SEAWEED,
            Food.BANANA, Food.BANANA_SLICE, Food.FISHCAKE, Food.BREAD, Food.CHOCOLATE_BAR,
            Food.THIN_SNAIL_MEAT, Food.LEAN_SNAIL_MEAT, Food.FAT_SNAIL_MEAT, Food.TOMATO, Food.CHEESE);
    private static final Map<Integer,Native950ItemCatalog.Entry> TYPES = new HashMap<>();
    private static Object store;
    private static boolean sequences;
    private Native950Food() { }

    public static boolean supports(int id) { return id==42251?sailfishDefinition()!=null:definition(id) != null; }

    /** Sailfish is newer than the inherited Food enum; require its exact950 food contract. */
    private static ItemDefinitions sailfishDefinition(){
        if(Cache.STORE==null||!Cache.isFlatReadOnly())return null;
        ItemDefinitions d=Native950CacheItems.definition(42251);
        return d!=null&&"Sailfish".equals(d.name)&&d.certTemplateId==-1&&!d.isStackable()
                &&"Eat".equals(d.inventoryOptions[0])&&d.getCSOpcode(963)==2400&&d.getCSOpcode(6924)==10
                &&sequence(18001,"1805741cdc5efa47e4ffeb1f21360ac39442a3a8643e254bfb725bca8d76527c")
                &&sequence(18002,"51079b1d37711e79cffc606f737c94f87e1c049e4cb26c85dbcd43257beb8ef1")?d:null;
    }

    private static Result eatSailfish(Player player,Native950Containers containers,int slot){
        ItemDefinitions d=sailfishDefinition();
        if(d==null||player==null||!player.isNative950()||!player.isActive()||player.hasFinished()||player.isDead()
                ||player.isLocked()||player.closeInterfaceLocked||player.isNative950ForceMovementActive()
                ||player.getNextForceMovement()!=null||player.getNextWorldTile()!=null||player.hasTeleported()
                ||player.getEmotesManager().getNextEmoteEnd()>=Utils.currentTimeMillis())return Result.refused("You cannot eat right now.");
        if(containers==null||!containers.ownsInventory(player)||slot<0||slot>=28||!sameFood(player.getInventory().getItem(slot),42251))
            return Result.refused("That food is not available in your backpack.");
        if(player.getFoodDelay()>Utils.currentTimeMillis())return Result.refused("You need to wait a moment before eating again.");
        // Older controllers accept only the inherited Food enum. Do not bypass their food restrictions.
        if(player.getControlerManager().getControler()!=null)return Result.refused("Sailfish is not available in this controlled activity yet.");
        Item held=player.getInventory().getItem(slot);
        if(!player.getControlerManager().handleItemOption1(held,42251,slot)||!player.getControlerManager().canDeleteInventoryItem(42251,1)
                ||player.getInventory().getItem(slot)!=held)return Result.refused("You cannot eat that here.");
        player.getInventory().items.set(slot,null);
        // Cache tooltip life points are ten times the inherited engine HP unit.
        player.heal(d.getCSOpcode(963)/10,player.getMaxHitpoints()*d.getCSOpcode(6924)/100);
        player.addFoodDelay(1800);player.getActionManager().setActionDelay(4);
        player.setNextAnimationForce(new Animation(player.isUnderCombat()?18002:18001));
        player.getPackets().sendGameMessage("You eat the sailfish.",true);
        return new Result(true,null);
    }

    private static synchronized Native950ItemCatalog.Entry definition(int id) {
        Food food = Food.forId(id);
        if (food == null || !ORDINARY.contains(food) || Cache.STORE == null || !Cache.isFlatReadOnly()) return null;
        if (store != Cache.STORE) {
            TYPES.clear(); store = Cache.STORE;
            // These exact sequence records are byte-identical in the local910 and950 caches.
            sequences = sequence(18001, "1805741cdc5efa47e4ffeb1f21360ac39442a3a8643e254bfb725bca8d76527c")
                    && sequence(18002, "51079b1d37711e79cffc606f737c94f87e1c049e4cb26c85dbcd43257beb8ef1");
        }
        if (!sequences) return null;
        if (TYPES.containsKey(id)) return TYPES.get(id);
        Native950ItemCatalog.Entry result = null;
        try {
            if (Native950IdValidity.get().isSafe(Native950IdValidity.Kind.ITEM, id)) {
                byte[] raw = Cache.STORE.getIndexes()[19].getFile(id >>> 8, id & 255);
                ItemDefinitions d = raw == null ? null : ItemDefinitions.decodeStrict947(id, raw, null);
                if (d != null && d.certTemplateId == -1 && d.lendTemplateId == -1
                        && d.bindTemplateId == -1 && d.shardTemplateId == -1 && !d.isStackable()
                        && "Eat".equals(d.inventoryOptions[0])) {
                    int leftover = food.getNewId();
                    Native950ItemCatalog.Entry output = leftover == 0 ? null : Native950CacheItems.entry(leftover);
                    if (leftover == 0 || (Native950IdValidity.get().isSafe(Native950IdValidity.Kind.ITEM, leftover)
                            && output != null && !output.stackable))
                        result = new Native950ItemCatalog.Entry(id, d.name, false, d.inventoryOptions);
                }
            }
        } catch (RuntimeException invalid) { result = null; }
        TYPES.put(id, result); return result;
    }

    public static Result eat(Player player, Native950Containers containers, int slot, int id) {
        if(id==42251)return eatSailfish(player,containers,slot);
        return eat(player, containers, slot, id, definition(id));
    }

    /** Tests replace only the verified cache lookup, keeping the real player/container transaction. */
    static Result eat(Player player, Native950Containers containers, int slot, int id, Native950ItemCatalog.Entry type) {
        if (player == null || !player.isNative950() || !player.isActive() || player.hasFinished() || player.isDead()
                || player.isLocked() || player.closeInterfaceLocked || player.isNative950ForceMovementActive()
                || player.getNextForceMovement() != null || player.getNextWorldTile() != null || player.hasTeleported()
                || player.getEmotesManager().getNextEmoteEnd() >= Utils.currentTimeMillis())
            return Result.refused("You cannot eat right now.");
        Food food = Food.forId(id);
        if (food == null || !ORDINARY.contains(food) || type == null || type.id != id || type.stackable
                || !"Eat".equals(type.option(1))) return Result.refused("Eating that item is not available yet.");
        if (containers == null || !containers.ownsInventory(player) || slot < 0 || slot >= 28)
            return Result.refused("That food is not available in your backpack.");
        Item held = player.getInventory().getItem(slot);
        if (!sameFood(held, id)) return Result.refused("The item in that slot has changed.");
        long now = Utils.currentTimeMillis();
        if ((id == 3144 ? player.getKaramDelay() : player.getFoodDelay()) > now)
            return Result.refused("You need to wait a moment before eating again.");
        if (!player.getControlerManager().handleItemOption1(held, id, slot)
                || !player.getControlerManager().canEat(food)
                || !player.getControlerManager().canDeleteInventoryItem(id, 1))
            return Result.refused("You cannot eat that here.");
        Item[] outputs = food.getNewId() == 0 ? new Item[0] : new Item[]{new Item(food.getNewId(), 1)};
        if (outputs.length != 0 && !player.getControlerManager().canAddInventoryItem(food.getNewId(), 1))
            return Result.refused("You cannot keep the remaining food or container here.");
        if (player.getInventory().getItem(slot) != held || !sameFood(held, id))
            return Result.refused("The item in that slot has changed.");
        // Preview the ordinary inventory shape, output metadata and capacity before any consumption.
        // Then replace the clicked slot itself, preserving pie/cake portions even with a full bag.
        if (!containers.canExchangeItems(new Item[]{new Item(id, 1)}, outputs))
            return Result.refused("There is no room for the remaining food or container.");
        int heal = food.getHeal(player) * 10;
        int extra = food.getExtraHP() * 10;
        if (heal < 0 || extra < 0) return Result.refused("Eating that item is not available yet.");
        player.getInventory().items.set(slot, outputs.length == 0 ? null : outputs[0]);
        int before = player.getHitpoints();
        player.heal(heal, extra);
        String name = type.name.toLowerCase(Locale.ROOT);
        long delay = name.contains("half") ? 800 : 1800;
        player.addFoodDelay(id == 3144 ? 100 : delay);
        if (id == 3144) player.addKaramDelay(1800);
        player.getActionManager().setActionDelay((int) delay / 1000 + 3);
        player.setNextAnimationForce(new Animation(player.isUnderCombat() ? 18002 : 18001));
        player.getPackets().sendGameMessage("You eat the " + name + ".", true);
        if (player.getHitpoints() > before) player.getPackets().sendGameMessage("It heals some health.", true);
        return new Result(true, null);
    }

    private static boolean sameFood(Item item, int id) {
        return item != null && item.getId() == id && item.getAmount() == 1
                && item.getCharges() == 0 && item.getAttributes() == null && item.getInventionData() == null;
    }

    private static boolean sequence(int id, String expected) {
        try {
            byte[] raw = Cache.STORE.getIndexes()[20].getFile(id >>> 7, id & 127);
            if (raw == null) return false;
            StringBuilder actual = new StringBuilder();
            for (byte b : MessageDigest.getInstance("SHA-256").digest(raw)) actual.append(String.format("%02x", b & 255));
            return expected.equals(actual.toString());
        } catch (Exception invalid) { return false; }
    }

    public static final class Result {
        public final boolean accepted;
        public final String reason;
        private Result(boolean accepted, String reason) { this.accepted = accepted; this.reason = reason; }
        private static Result refused(String reason) { return new Result(false, reason); }
    }
}