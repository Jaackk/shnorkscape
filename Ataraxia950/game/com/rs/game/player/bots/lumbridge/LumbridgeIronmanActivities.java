package com.rs.game.player.bots.lumbridge;

import com.rs.game.Region;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.npc.NPC;
import com.rs.game.player.Bank;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Cooking;
import com.rs.game.player.actions.Fishing;
import com.rs.game.player.actions.Fishing.FishingSpots;
import com.rs.game.player.actions.mining.Mining;
import com.rs.game.player.actions.smithing.Smelting;
import com.rs.game.player.actions.smithing.Smithing;
import com.rs.game.player.actions.smithing.defs.SmeltingBar;
import com.rs.game.player.actions.woodcutting.Woodcutting;
import com.rs.game.player.bots.BotApi;
import com.rs.game.player.bots.BotPlayer;
import com.rs.utils.Utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

final class LumbridgeIronmanActivities {

    private LumbridgeIronmanActivities() {
    }

    interface Activity {
        LumbridgeIronmanData.ActivityKind kind();
        void start(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory);
        void tick(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory);
        boolean isComplete();
        boolean isBlocked();
        String debug();
    }

    static Activity bank(String reason) {
        return new BankActivity(reason);
    }

    static Activity cook() {
        return new CookActivity();
    }

    static Activity fish() {
        return new FishActivity();
    }

    static Activity mine(LumbridgeIronmanData.MineTarget target) {
        return new MineActivity(target);
    }

    static Activity smelt(SmeltingBar bar) {
        return new SmeltActivity(bar);
    }

    static Activity smith(LumbridgeIronmanData.GearTier tier, LumbridgeIronmanData.GearPiece piece) {
        return new SmithActivity(tier, piece);
    }

    static Activity woodcut() {
        return new WoodcutActivity();
    }

    static Activity combat(LumbridgeIronmanData.CombatTarget target) {
        return new CombatActivity(target);
    }

    static Activity loot() {
        return new LootActivity();
    }

    static Activity wander() {
        return new WanderActivity();
    }

    private abstract static class BaseActivity implements Activity {
        private boolean complete;
        private boolean blocked;
        private String debug = "starting";
        private int localTicks;

        @Override
        public void start(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory) {
            localTicks = 0;
            onStart(bot, api, memory);
        }

        @Override
        public void tick(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory) {
            localTicks++;
            if (complete || blocked) {
                return;
            }
            if (bot.isDead() || bot.hasFinished()) {
                blocked("dead or finished");
                return;
            }
            onTick(bot, api, memory);
        }

        @Override
        public boolean isComplete() {
            return complete;
        }

        @Override
        public boolean isBlocked() {
            return blocked;
        }

        @Override
        public String debug() {
            return debug;
        }

        protected void onStart(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory) {
        }

        protected abstract void onTick(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory);

        protected int localTicks() {
            return localTicks;
        }

        protected void complete(String reason) {
            complete = true;
            debug = reason;
        }

        protected void blocked(String reason) {
            blocked = true;
            debug = reason;
        }

        protected void debug(String text) {
            debug = text;
        }

        protected boolean busy(BotPlayer bot) {
            return bot.isLocked()
                    || bot.hasWalkSteps()
                    || bot.getRouteEvent() != null
                    || bot.getActionManager().getAction() != null;
        }

        protected boolean routeNear(BotPlayer bot, BotApi api, WorldTile tile, int radius) {
            if (api.withinDistance(tile, radius)) {
                return true;
            }
            if (api.routeNear(tile, radius)) {
                debug("routing to " + tile.getX() + "," + tile.getY());
                return false;
            }
            if (api.walkNear(tile, Math.max(1, radius))) {
                debug("walking toward " + tile.getX() + "," + tile.getY());
                return false;
            }
            blocked("no route to " + tile.getX() + "," + tile.getY());
            return false;
        }

        protected WorldObject findObject(BotApi api, int radius, final String... names) {
            return api.findNearestObject(radius, object -> {
                if (object == null || object.getDefinitions() == null || object.getDefinitions().name == null) {
                    return false;
                }
                String name = object.getDefinitions().name.toLowerCase();
                for (String wanted : names) {
                    if (wanted != null && name.contains(wanted.toLowerCase())) {
                        return true;
                    }
                }
                return false;
            });
        }

        protected boolean routeToObject(BotPlayer bot, BotApi api, WorldObject object) {
            if (object == null) {
                return false;
            }
            if (bot.withinDistance(object, 1)) {
                return true;
            }
            if (api.routeNear(new WorldTile(object), 1)) {
                debug("routing to " + object.getDefinitions().name);
                return false;
            }
            blocked("could not route to " + object.getDefinitions().name);
            return false;
        }

        protected boolean hasAnyRawFood(Player player) {
            return LumbridgeIronmanData.countRawFood(player) > 0;
        }
    }

    private static final class BankActivity extends BaseActivity {
        private final String reason;

        private BankActivity(String reason) {
            this.reason = reason;
        }

        @Override
        public LumbridgeIronmanData.ActivityKind kind() {
            return LumbridgeIronmanData.ActivityKind.BANK;
        }

        @Override
        protected void onTick(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory) {
            if (!routeNear(bot, api, LumbridgeIronmanData.BANK_SEARCH_TILE, 14)) {
                return;
            }
            depositJunk(bot);
            restockTools(bot);
            withdrawFood(bot, 10);
            memory.markBanked();
            complete("banked: " + reason);
        }

        private void depositJunk(BotPlayer bot) {
            for (int slot = 0; slot < bot.getInventory().getItemsContainerSize(); slot++) {
                Item item = bot.getInventory().getItem(slot);
                if (item == null) {
                    continue;
                }
                if (shouldKeepInInventory(bot, item)) {
                    continue;
                }
                bot.getBank().depositItem(slot, item.getAmount(), false);
            }
            bot.getInventory().refresh();
        }

        private boolean shouldKeepInInventory(BotPlayer bot, Item item) {
            int id = item.getId();
            if (LumbridgeIronmanData.isKeepItem(id)) {
                if (LumbridgeIronmanData.isCookedFood(id)) {
                    return LumbridgeIronmanData.countCookedFood(bot) <= 14;
                }
                return true;
            }
            if (LumbridgeIronmanData.isOre(id) || LumbridgeIronmanData.isBar(id)) {
                return true;
            }
            if (LumbridgeIronmanData.isRawFood(id)) {
                return LumbridgeIronmanData.countRawFood(bot) <= 10;
            }
            return false;
        }

        private void restockTools(BotPlayer bot) {
            for (int id : LumbridgeIronmanData.STARTER_TOOLS) {
                if (!bot.getInventory().containsItem(id, 1) && !LumbridgeIronmanData.hasItemAnywhere(bot, id)) {
                    withdraw(bot, id, 1);
                }
            }
            if (!bot.getInventory().containsItem(LumbridgeIronmanData.FISHING_BAIT, 1)
                    && bot.getBank().containsItemCurrentBank(LumbridgeIronmanData.FISHING_BAIT, 1)) {
                withdraw(bot, LumbridgeIronmanData.FISHING_BAIT, 100);
            }
        }

        private void withdrawFood(BotPlayer bot, int targetAmount) {
            int held = LumbridgeIronmanData.countCookedFood(bot);
            if (held >= targetAmount) {
                return;
            }
            int need = targetAmount - held;
            for (int id : LumbridgeIronmanData.COOKED_FOODS) {
                if (need <= 0 || bot.getInventory().getFreeSlots() <= 0) {
                    return;
                }
                int before = bot.getInventory().getAmountOf(id);
                withdraw(bot, id, need);
                need -= Math.max(0, bot.getInventory().getAmountOf(id) - before);
            }
        }
    }

    private static final class CookActivity extends BaseActivity {
        @Override
        public LumbridgeIronmanData.ActivityKind kind() {
            return LumbridgeIronmanData.ActivityKind.COOK;
        }

        @Override
        protected void onTick(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory) {
            if (!hasAnyRawFood(bot)) {
                complete("no raw food");
                return;
            }
            if (busy(bot)) {
                debug("cooking busy");
                return;
            }
            WorldObject range = findObject(api, 24, "range", "fire", "cooking range");
            if (range == null) {
                if (!routeNear(bot, api, LumbridgeIronmanData.LUMBRIDGE_COURTYARD, 12)) {
                    return;
                }
                range = findObject(api, 30, "range", "fire", "cooking range");
            }
            if (range == null) {
                blocked("no range or fire found");
                return;
            }
            if (!routeToObject(bot, api, range)) {
                return;
            }
            int raw = LumbridgeIronmanData.firstRawFood(bot);
            if (raw < 0) {
                complete("raw food cooked");
                return;
            }
            bot.clickedObject = range;
            bot.getActionManager().setAction(new Cooking(range, new Item(raw), false, bot.getInventory().getAmountOf(raw)));
            debug("cooking " + raw);
        }
    }

    private static final class FishActivity extends BaseActivity {
        @Override
        public LumbridgeIronmanData.ActivityKind kind() {
            return LumbridgeIronmanData.ActivityKind.FISH;
        }

        @Override
        protected void onTick(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory) {
            if (!bot.getInventory().hasFreeSlots()) {
                complete("inventory full");
                return;
            }
            if (busy(bot)) {
                debug("fishing busy");
                return;
            }
            if (!routeNear(bot, api, LumbridgeIronmanData.LUMBRIDGE_WATER, 20)) {
                return;
            }
            NPC spotNpc = api.findNearestNpc(24, npc -> resolveUsableSpot(bot, npc) != null);
            if (spotNpc == null) {
                blocked("no usable fishing spot");
                return;
            }
            if (!bot.withinDistance(spotNpc, 1)) {
                api.routeNear(new WorldTile(spotNpc), 1);
                debug("routing to fishing spot");
                return;
            }
            FishingSpots spot = resolveUsableSpot(bot, spotNpc);
            if (spot == null) {
                blocked("spot mapping disappeared");
                return;
            }
            bot.getActionManager().setAction(new Fishing(spot, spotNpc));
            debug("fishing " + spot.name());
        }

        private static FishingSpots resolveUsableSpot(BotPlayer bot, NPC npc) {
            if (npc == null) {
                return null;
            }
            int fishingLevel = bot.getSkills().getLevel(Skills.FISHING);
            for (int option = 1; option <= 2; option++) {
                FishingSpots spot = FishingSpots.forId(npc.getId() | (option << 24));
                if (spot == null) {
                    continue;
                }
                if (!bot.getInventory().containsItem(spot.getTool(), 1)) {
                    continue;
                }
                if (spot.getBait() != -1 && !bot.getInventory().containsItem(spot.getBait(), 1)) {
                    continue;
                }
                if (spot.getFish().length == 0 || fishingLevel < spot.getFish()[0].getLevel()) {
                    continue;
                }
                return spot;
            }
            return null;
        }
    }

    private static final class MineActivity extends BaseActivity {
        private final LumbridgeIronmanData.MineTarget target;

        private MineActivity(LumbridgeIronmanData.MineTarget target) {
            this.target = target == null ? LumbridgeIronmanData.MineTarget.COPPER : target;
        }

        @Override
        public LumbridgeIronmanData.ActivityKind kind() {
            return LumbridgeIronmanData.ActivityKind.MINE;
        }

        @Override
        protected void onTick(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory) {
            if (!bot.getInventory().hasFreeSlots()) {
                complete("inventory full");
                return;
            }
            if (busy(bot)) {
                debug("mining busy");
                return;
            }
            if (!routeNear(bot, api, target.searchTile, 18)) {
                return;
            }
            WorldObject rock = findObject(api, 24, target.objectName);
            if (rock == null) {
                blocked("no " + target.objectName);
                return;
            }
            if (!routeToObject(bot, api, rock)) {
                return;
            }
            bot.clickedObject = rock;
            bot.getActionManager().setAction(new Mining(rock, target.rock));
            debug("mining " + target.objectName);
        }
    }

    private static final class SmeltActivity extends BaseActivity {
        private final SmeltingBar bar;

        private SmeltActivity(SmeltingBar bar) {
            this.bar = bar;
        }

        @Override
        public LumbridgeIronmanData.ActivityKind kind() {
            return LumbridgeIronmanData.ActivityKind.SMELT;
        }

        @Override
        protected void onTick(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory) {
            if (bar == null) {
                blocked("no bar selected");
                return;
            }
            if (busy(bot)) {
                debug("smelting busy");
                return;
            }
            if (!hasInventoryRequirements(bot, bar)) {
                complete("missing smelting ore");
                return;
            }
            WorldObject furnace = findObject(api, 30, "furnace", "forge");
            if (furnace == null) {
                if (!routeNear(bot, api, LumbridgeIronmanData.SMITHING_SEARCH_TILE, 18)) {
                    return;
                }
                furnace = findObject(api, 30, "furnace", "forge");
            }
            if (furnace == null) {
                blocked("no furnace");
                return;
            }
            if (!routeToObject(bot, api, furnace)) {
                return;
            }
            bot.clickedObject = furnace;
            bot.getActionManager().setAction(new Smelting(bar.getButtonId(), furnace, countSmelts(bot, bar), false));
            debug("smelting " + bar);
        }

        private boolean hasInventoryRequirements(Player player, SmeltingBar bar) {
            for (Item item : bar.getItemsRequired()) {
                if (player.getInventory().getAmountOf(item.getId()) < item.getAmount()) {
                    return false;
                }
            }
            return true;
        }

        private int countSmelts(Player player, SmeltingBar bar) {
            int amount = 60;
            for (Item item : bar.getItemsRequired()) {
                amount = Math.min(amount, player.getInventory().getAmountOf(item.getId()) / item.getAmount());
            }
            return Math.max(1, amount);
        }
    }

    private static final class SmithActivity extends BaseActivity {
        private final LumbridgeIronmanData.GearTier tier;
        private final LumbridgeIronmanData.GearPiece piece;

        private SmithActivity(LumbridgeIronmanData.GearTier tier, LumbridgeIronmanData.GearPiece piece) {
            this.tier = tier;
            this.piece = piece;
        }

        @Override
        public LumbridgeIronmanData.ActivityKind kind() {
            return LumbridgeIronmanData.ActivityKind.SMITH;
        }

        @Override
        protected void onTick(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory) {
            if (tier == null || tier == LumbridgeIronmanData.GearTier.NONE || piece == null || tier.forgingBar == null) {
                blocked("no smithing target");
                return;
            }
            if (LumbridgeIronmanData.hasItemAnywhere(bot, tier.itemFor(piece))) {
                complete("already has " + tier + " " + piece);
                return;
            }
            if (busy(bot)) {
                debug("smithing busy");
                return;
            }
            if (bot.getInventory().getAmountOf(tier.forgingBar.getBarId()) < piece.bars) {
                complete("missing bars");
                return;
            }
            WorldObject anvil = findObject(api, 30, "anvil", "forge");
            if (anvil == null) {
                if (!routeNear(bot, api, LumbridgeIronmanData.SMITHING_SEARCH_TILE, 18)) {
                    return;
                }
                anvil = findObject(api, 30, "anvil", "forge");
            }
            if (anvil == null) {
                blocked("no anvil");
                return;
            }
            if (!routeToObject(bot, api, anvil)) {
                return;
            }
            bot.clickedObject = anvil;
            bot.getTemporaryAttributtes().put("SmithingBar", tier.forgingBar);
            bot.getTemporaryAttributtes().put("SmithingObject", Boolean.FALSE);
            int barsHeld = bot.getInventory().getAmountOf(tier.forgingBar.getBarId());
            int quantity = Math.max(1, barsHeld / Math.max(1, piece.bars));
            bot.getActionManager().setAction(new Smithing(quantity, piece.smithingIndex, false));
            debug("smithing " + tier + " " + piece + " x" + quantity);
        }
    }

    private static final class WoodcutActivity extends BaseActivity {
        @Override
        public LumbridgeIronmanData.ActivityKind kind() {
            return LumbridgeIronmanData.ActivityKind.WOODCUT;
        }

        @Override
        protected void onTick(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory) {
            if (!bot.getInventory().hasFreeSlots()) {
                complete("inventory full");
                return;
            }
            if (busy(bot)) {
                debug("woodcutting busy");
                return;
            }
            LumbridgeIronmanData.TreeTarget target = LumbridgeIronmanData.TreeTarget.bestFor(bot);
            if (!routeNear(bot, api, target.searchTile, 18)) {
                return;
            }
            WorldObject tree = findObject(api, 24, target.objectName);
            if (tree == null) {
                blocked("no " + target.objectName);
                return;
            }
            if (!routeToObject(bot, api, tree)) {
                return;
            }
            bot.clickedObject = tree;
            bot.getActionManager().setAction(new Woodcutting(tree, target.tree, null));
            debug("woodcutting " + target.objectName);
        }
    }

    private static final class CombatActivity extends BaseActivity {
        private final LumbridgeIronmanData.CombatTarget target;

        private CombatActivity(LumbridgeIronmanData.CombatTarget target) {
            this.target = target == null ? LumbridgeIronmanData.CombatTarget.CHICKEN : target;
        }

        @Override
        public LumbridgeIronmanData.ActivityKind kind() {
            return LumbridgeIronmanData.ActivityKind.COMBAT;
        }

        @Override
        protected void onTick(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory) {
            api.tryEatFood(memory.personality().getEatHpPercent());
            if (!hasFoodForTarget(bot) && target != LumbridgeIronmanData.CombatTarget.CHICKEN) {
                complete("needs food before " + target.npcName);
                return;
            }
            if (busy(bot)) {
                debug("combat busy");
                return;
            }
            if (!routeNear(bot, api, target.searchTile, 18)) {
                return;
            }
            NPC npc = api.findNearestNpc(20, candidate -> {
                if (candidate == null || candidate.isDead() || candidate.hasFinished() || candidate.isUnderCombat()) {
                    return false;
                }
                if (candidate.getDefinitions() == null || candidate.getDefinitions().name == null) {
                    return false;
                }
                return candidate.getDefinitions().name.toLowerCase().contains(target.npcName);
            });
            if (npc == null) {
                blocked("no " + target.npcName);
                return;
            }
            if (memory.personality().usesSpec()) {
                api.tryUseSpecial(55);
            }
            if (api.attack(npc)) {
                if (memory.mayChat(180)) {
                    api.forceTalk(getCombatLine(target));
                    memory.markChatted();
                }
                debug("fighting " + target.npcName);
            } else {
                blocked("attack rejected");
            }
        }

        private boolean hasFoodForTarget(Player player) {
            return LumbridgeIronmanData.countCookedFood(player) >= target.foodWanted;
        }

        private String getCombatLine(LumbridgeIronmanData.CombatTarget target) {
            if (target == LumbridgeIronmanData.CombatTarget.CHICKEN) {
                return "need feathers";
            }
            if (target == LumbridgeIronmanData.CombatTarget.COW) {
                return "hides and food";
            }
            return "training a bit";
        }
    }

    private static final class LootActivity extends BaseActivity {
        @Override
        public LumbridgeIronmanData.ActivityKind kind() {
            return LumbridgeIronmanData.ActivityKind.LOOT;
        }

        @Override
        protected void onTick(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory) {
            if (!bot.getInventory().hasFreeSlots()) {
                complete("inventory full");
                return;
            }
            FloorItem item = findUsefulFloorItem(bot, 10);
            if (item == null) {
                complete("no useful loot");
                return;
            }
            if (!bot.withinDistance(item.getTile(), 0)) {
                if (busy(bot)) {
                    debug("walking to loot " + item.getId());
                    return;
                }
                api.routeTo(item.getTile());
                debug("routing to loot " + item.getId());
                return;
            }
            if (World.removeGroundItem(bot, item)) {
                memory.markLooted();
                debug("picked up " + item.getId());
                complete("looted");
            } else {
                blocked("pickup failed");
            }
        }
    }

    private static final class WanderActivity extends BaseActivity {
        private WorldTile target;

        @Override
        public LumbridgeIronmanData.ActivityKind kind() {
            return LumbridgeIronmanData.ActivityKind.WANDER;
        }

        @Override
        protected void onStart(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory) {
            WorldTile[] choices = {
                    LumbridgeIronmanData.LUMBRIDGE_COURTYARD,
                    LumbridgeIronmanData.CHICKEN_COOP,
                    LumbridgeIronmanData.COW_FIELD,
                    LumbridgeIronmanData.LUMBRIDGE_TREE_PATCH,
                    LumbridgeIronmanData.LUMBRIDGE_SWAMP_MINE
            };
            target = choices[ThreadLocalRandom.current().nextInt(choices.length)];
        }

        @Override
        protected void onTick(BotPlayer bot, BotApi api, LumbridgeIronmanMemory memory) {
            if (target == null) {
                complete("nowhere to wander");
                return;
            }
            if (api.withinDistance(target, 5)) {
                if (ThreadLocalRandom.current().nextInt(4) == 0) {
                    api.walkNear(target, 4);
                    debug("wandering locally");
                }
                complete("wandered");
                return;
            }
            if (!api.routeNear(target, 5)) {
                api.walkNear(bot.getSpawnTile(), 8);
            }
            debug("wandering");
        }
    }

    static FloorItem findUsefulFloorItem(BotPlayer bot, int radius) {
        FloorItem nearest = null;
        int nearestDistance = Integer.MAX_VALUE;
        for (int regionId : bot.getMapRegionsIds()) {
            Region region = World.getRegion(regionId, true);
            List<FloorItem> items = region.getGroundItems();
            if (items == null) {
                continue;
            }
            for (FloorItem item : items) {
                if (item == null || item.getTile() == null || item.getTile().getPlane() != bot.getPlane()) {
                    continue;
                }
                if (item.hasOwner() && !bot.getUsername().equals(item.getOwner())) {
                    continue;
                }
                if (!LumbridgeIronmanData.isUsefulLoot(item.getId())) {
                    continue;
                }
                int distance = Utils.getDistance(bot.getX(), bot.getY(), item.getTile().getX(), item.getTile().getY());
                if (distance <= radius && distance < nearestDistance) {
                    nearest = item;
                    nearestDistance = distance;
                }
            }
        }
        return nearest;
    }

    static boolean withdraw(Player player, int itemId, int amount) {
        if (amount <= 0 || player.getInventory().getFreeSlots() <= 0) {
            return false;
        }
        Bank bank = player.getBank();
        Item banked = bank.getItem(itemId);
        if (banked == null || banked.getAmount() <= 0) {
            return false;
        }
        int moved = Math.min(amount, banked.getAmount());
        int[] slot = bank.getItemSlot(banked);
        if (slot == null) {
            return false;
        }
        bank.removeItem(slot, moved, false, Bank.DESTROY_ITEM);
        player.getInventory().addItem(itemId, moved);
        return true;
    }
}
