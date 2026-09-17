package com.rs.game.player.bots.lumbridge;

import com.rs.game.item.Item;
import com.rs.game.item.floor.FloorItem;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.smithing.defs.SmeltingBar;
import com.rs.game.player.bots.BotPersonality;
import com.rs.game.player.bots.BotScript;

import java.util.concurrent.ThreadLocalRandom;

public class LumbridgeIronmanBotScript extends BotScript {

    private final BotPersonality personality;
    private final LumbridgeIronmanProfile.ScriptState restoredState;
    private LumbridgeIronmanMemory memory;
    private LumbridgeIronmanActivities.Activity currentActivity;
    private int thinkingCooldown;
    private int activityStarts;
    private boolean wasDying;

    public LumbridgeIronmanBotScript() {
        this(BotPersonality.random(), null);
    }

    public LumbridgeIronmanBotScript(BotPersonality personality) {
        this(personality, null);
    }

    public LumbridgeIronmanBotScript(BotPersonality.Archetype archetype,
            LumbridgeIronmanProfile.ScriptState restoredState) {
        this(archetype == null ? BotPersonality.random() : BotPersonality.forArchetype(archetype), restoredState);
    }

    public LumbridgeIronmanBotScript(BotPersonality personality,
            LumbridgeIronmanProfile.ScriptState restoredState) {
        this.personality = personality == null ? BotPersonality.random() : personality;
        this.restoredState = restoredState;
    }

    @Override
    protected void onStart() {
        memory = new LumbridgeIronmanMemory(personality);
        if (restoredState != null) {
            memory.restore(restoredState);
            activityStarts = Math.max(0, restoredState.getActivityStarts());
            thinkingCooldown = Math.max(0, restoredState.getThinkingCooldown());
        }
        bot().setIronMan(true);
        LumbridgeIronmanData.ensureStarterKit(bot());
        equipBestKnownGear();
        if (restoredState == null && personality.rollChat()) {
            api().forceTalk("fresh start");
            memory.markChatted();
        }
        if (restoredState == null) {
            thinkingCooldown = ThreadLocalRandom.current().nextInt(2, 8);
        }
        delay(memory.randomReactionDelay());
    }

    @Override
    protected boolean canActWhileBusy() {
        return true;
    }

    @Override
    protected void onTick() {
        memory.pulse();
        api().tryEatFood(personality.getEatHpPercent());

        if (bot().hasFinished()) {
            return;
        }

        boolean dying = bot().isDead() || bot().getHitpoints() <= 0;
        if (dying) {
            wasDying = true;
            delay(3);
            return;
        }
        if (wasDying && !bot().isLocked()) {
            wasDying = false;
            handleRespawn();
            return;
        }
        if (wasDying) {
            delay(2);
            return;
        }

        maybeEquipUsefulItems();

        if (thinkingCooldown > 0) {
            thinkingCooldown--;
        }

        if (shouldInterruptForLoot()) {
            replaceActivity(LumbridgeIronmanActivities.loot(), "nearby loot");
        }

        if (currentActivity == null
                || currentActivity.isComplete()
                || currentActivity.isBlocked()
                || shouldRethinkCurrentGoal()) {
            chooseNextActivity();
        }

        if (currentActivity != null) {
            currentActivity.tick(bot(), api(), memory);
            if (currentActivity.isBlocked()) {
                memory.rememberFailedStart();
                delay(memory.randomShortDelay());
            } else if (currentActivity.isComplete()) {
                memory.rememberFinished(currentActivity.kind());
                delay(memory.randomReactionDelay());
            }
        }
    }

    @Override
    public String getDebugInfo() {
        String activity = currentActivity == null ? "none" : currentActivity.kind() + ":" + currentActivity.debug();
        return "lumby-ironman arch=" + personality.getArchetype()
                + " goal=" + memory.currentKind()
                + " reason=" + memory.currentReason()
                + " ticks=" + memory.goalTicksRemaining()
                + " act=" + activity
                + " food=" + LumbridgeIronmanData.countCookedFood(bot())
                + " gear=" + LumbridgeIronmanData.currentMeleeTier(bot());
    }

    public LumbridgeIronmanProfile.ScriptState snapshotState() {
        LumbridgeIronmanProfile.ScriptState state = memory == null
                ? new LumbridgeIronmanProfile.ScriptState()
                : memory.snapshot();
        state.setArchetype(personality.getArchetype());
        state.setActivityStarts(activityStarts);
        state.setThinkingCooldown(thinkingCooldown);
        if (currentActivity != null) {
            state.setCurrentActivityKind(currentActivity.kind());
            state.setCurrentActivityDebug(currentActivity.debug());
        }
        return state;
    }

    private void handleRespawn() {
        bot().setNextWorldTile(LumbridgeIronmanData.LUMBRIDGE_SPAWN);
        bot().heal(bot().getMaxHitpoints());
        api().cancelAll();
        currentActivity = null;
        thinkingCooldown = ThreadLocalRandom.current().nextInt(2, 8);
        LumbridgeIronmanData.ensureStarterKit(bot());
        equipBestKnownGear();
        if (personality.rollChat()) {
            api().forceTalk("rip");
            memory.markChatted();
        }
        delay(memory.randomReactionDelay());
    }

    private boolean shouldRethinkCurrentGoal() {
        if (currentActivity == null) {
            return true;
        }
        if (thinkingCooldown > 0) {
            return false;
        }
        if (memory.shouldRethinkGoal()) {
            thinkingCooldown = ThreadLocalRandom.current().nextInt(20, 70);
            return true;
        }
        return false;
    }

    private boolean shouldInterruptForLoot() {
        if (bot().getInventory().getFreeSlots() <= 0 || memory.lootedRecently(8)) {
            return false;
        }
        if (currentActivity != null && currentActivity.kind() == LumbridgeIronmanData.ActivityKind.LOOT) {
            return false;
        }
        FloorItem item = LumbridgeIronmanActivities.findUsefulFloorItem(bot(), 5);
        return item != null && ThreadLocalRandom.current().nextDouble() < 0.55 + personality.getGreed() * 0.35;
    }

    private void chooseNextActivity() {
        LumbridgeIronmanActivities.Activity next = planNextActivity();
        replaceActivity(next, next == null ? "planner returned nothing" : next.debug());
    }

    private void replaceActivity(LumbridgeIronmanActivities.Activity next, String reason) {
        if (next == null) {
            next = LumbridgeIronmanActivities.wander();
        }
        currentActivity = next;
        currentActivity.start(bot(), api(), memory);
        memory.startGoal(next.kind(), reason);
        activityStarts++;
    }

    private LumbridgeIronmanActivities.Activity planNextActivity() {
        if (bot().getInventory().getFreeSlots() == 0) {
            LumbridgeIronmanActivities.Activity production = chooseInventoryProduction();
            if (production != null) {
                return production;
            }
            return LumbridgeIronmanActivities.bank("inventory full");
        }

        FloorItem loot = LumbridgeIronmanActivities.findUsefulFloorItem(bot(), 8);
        if (loot != null && !memory.recentlyFinished(LumbridgeIronmanData.ActivityKind.LOOT)) {
            return LumbridgeIronmanActivities.loot();
        }

        LumbridgeIronmanActivities.Activity survival = chooseSurvivalActivity();
        if (survival != null) {
            return survival;
        }

        LumbridgeIronmanActivities.Activity upgrade = chooseGearUpgradeActivity();
        if (upgrade != null && ThreadLocalRandom.current().nextDouble() < 0.80 + personality.getEfficiency() * 0.15) {
            return upgrade;
        }

        return chooseWeightedFreeWillActivity(upgrade);
    }

    private LumbridgeIronmanActivities.Activity chooseInventoryProduction() {
        LumbridgeIronmanActivities.Activity upgrade = chooseGearUpgradeActivity();
        if (upgrade != null) {
            LumbridgeIronmanData.ActivityKind kind = upgrade.kind();
            if (kind == LumbridgeIronmanData.ActivityKind.SMELT || kind == LumbridgeIronmanData.ActivityKind.SMITH) {
                return upgrade;
            }
        }
        if (LumbridgeIronmanData.countRawFood(bot()) > 0) {
            return LumbridgeIronmanActivities.cook();
        }
        return null;
    }

    private LumbridgeIronmanActivities.Activity chooseSurvivalActivity() {
        int cookedFood = LumbridgeIronmanData.countCookedFood(bot());
        int rawFood = LumbridgeIronmanData.countRawFood(bot());
        int combat = bot().getSkills().getCombatLevel();
        int foodFloor = combat >= 20 ? 8 : combat >= 10 ? 5 : 2;
        if (cookedFood >= foodFloor) {
            return null;
        }
        if (rawFood > 0) {
            return LumbridgeIronmanActivities.cook();
        }
        if (canFishLocally()) {
            return LumbridgeIronmanActivities.fish();
        }
        return LumbridgeIronmanActivities.combat(LumbridgeIronmanData.CombatTarget.CHICKEN);
    }

    private boolean canFishLocally() {
        // The Lumbridge river only has bait/lure spots (NPC 329, pike level 25+).
        // Until a NET spot or higher-tier tools are nearby, fishing isn't viable below 25.
        return bot().getSkills().getLevel(Skills.FISHING) >= 25
                && bot().getInventory().containsItem(LumbridgeIronmanData.FISHING_ROD, 1)
                && bot().getInventory().containsItem(LumbridgeIronmanData.FISHING_BAIT, 1);
    }

    private LumbridgeIronmanActivities.Activity chooseGearUpgradeActivity() {
        LumbridgeIronmanData.GearTier desiredTier = LumbridgeIronmanData.nextUsefulGearTier(bot());
        if (desiredTier == null || desiredTier == LumbridgeIronmanData.GearTier.NONE) {
            desiredTier = LumbridgeIronmanData.GearTier.BRONZE;
        }

        LumbridgeIronmanData.GearPiece missing = desiredTier.firstMissingCorePiece(bot());
        if (missing != null && desiredTier.hasBarsFor(bot(), missing)) {
            ensureBarInInventory(desiredTier, missing.bars);
            return LumbridgeIronmanActivities.smith(desiredTier, missing);
        }

        SmeltingBar smeltable = LumbridgeIronmanData.bestSmeltableBar(bot(), desiredTier);
        if (smeltable != null && hasSmeltRequirementsInInventory(smeltable)) {
            return LumbridgeIronmanActivities.smelt(smeltable);
        }

        if (desiredTier.isMissingCoreGear(bot())) {
            return LumbridgeIronmanActivities.mine(LumbridgeIronmanData.bestMineTarget(bot(), desiredTier));
        }

        LumbridgeIronmanData.GearTier currentTier = LumbridgeIronmanData.currentMeleeTier(bot());
        if (currentTier.ordinal() < desiredTier.ordinal()) {
            return LumbridgeIronmanActivities.mine(LumbridgeIronmanData.bestMineTarget(bot(), desiredTier));
        }

        return null;
    }

    private LumbridgeIronmanActivities.Activity chooseWeightedFreeWillActivity(
            LumbridgeIronmanActivities.Activity efficientUpgrade) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double roll = random.nextDouble();
        double aggression = personality.getAggression();
        double efficiency = personality.getEfficiency();
        double greed = personality.getGreed();

        double combatWeight = 0.18 + aggression * 0.35;
        double miningWeight = 0.18 + efficiency * 0.20;
        double foodWeight = LumbridgeIronmanData.countCookedFood(bot()) < 12 ? 0.18 : 0.08;
        double woodWeight = 0.10 + personality.getIdleTendency() * 0.08;
        double bankWeight = bot().getInventory().getFreeSlots() < 8 ? 0.14 : 0.04;
        double upgradeWeight = efficientUpgrade == null ? 0.0 : 0.12 + greed * 0.16;

        double total = combatWeight + miningWeight + foodWeight + woodWeight + bankWeight + upgradeWeight + 0.10;
        double cursor = roll * total;

        cursor -= upgradeWeight;
        if (cursor <= 0 && efficientUpgrade != null) {
            return efficientUpgrade;
        }

        cursor -= combatWeight;
        if (cursor <= 0) {
            return LumbridgeIronmanActivities.combat(LumbridgeIronmanData.CombatTarget.bestFor(bot()));
        }

        cursor -= miningWeight;
        if (cursor <= 0) {
            LumbridgeIronmanData.GearTier desired = LumbridgeIronmanData.nextUsefulGearTier(bot());
            return LumbridgeIronmanActivities.mine(LumbridgeIronmanData.bestMineTarget(bot(), desired));
        }

        cursor -= foodWeight;
        if (cursor <= 0) {
            if (LumbridgeIronmanData.countRawFood(bot()) > 0) {
                return LumbridgeIronmanActivities.cook();
            }
            if (canFishLocally()) {
                return LumbridgeIronmanActivities.fish();
            }
            return LumbridgeIronmanActivities.combat(LumbridgeIronmanData.CombatTarget.CHICKEN);
        }

        cursor -= woodWeight;
        if (cursor <= 0) {
            return LumbridgeIronmanActivities.woodcut();
        }

        cursor -= bankWeight;
        if (cursor <= 0) {
            return LumbridgeIronmanActivities.bank("routine cleanup");
        }

        return LumbridgeIronmanActivities.wander();
    }

    private boolean hasSmeltRequirementsInInventory(SmeltingBar bar) {
        if (bar == null) {
            return false;
        }
        for (Item item : bar.getItemsRequired()) {
            if (bot().getInventory().getAmountOf(item.getId()) < item.getAmount()) {
                return false;
            }
        }
        return true;
    }

    private void ensureBarInInventory(LumbridgeIronmanData.GearTier tier, int amount) {
        if (tier == null || tier.forgingBar == null) {
            return;
        }
        int barId = tier.forgingBar.getBarId();
        int held = bot().getInventory().getAmountOf(barId);
        if (held >= amount) {
            return;
        }
        LumbridgeIronmanActivities.withdraw(bot(), barId, amount - held);
    }

    private void maybeEquipUsefulItems() {
        equipBestKnownGear();
        equipBestTool(LumbridgeIronmanData.GearPiece.PICKAXE);
        equipBestTool(LumbridgeIronmanData.GearPiece.HATCHET);
    }

    private void equipBestKnownGear() {
        LumbridgeIronmanData.GearPiece[] gear = {
                LumbridgeIronmanData.GearPiece.WEAPON,
                LumbridgeIronmanData.GearPiece.HELM,
                LumbridgeIronmanData.GearPiece.BODY,
                LumbridgeIronmanData.GearPiece.LEGS,
                LumbridgeIronmanData.GearPiece.SHIELD
        };
        for (LumbridgeIronmanData.GearPiece piece : gear) {
            int current = currentEquippedId(piece);
            LumbridgeIronmanData.GearTier currentTier = tierForItem(current);
            LumbridgeIronmanData.GearTier best = currentTier;
            for (LumbridgeIronmanData.GearTier tier : LumbridgeIronmanData.GearTier.values()) {
                if (tier == LumbridgeIronmanData.GearTier.NONE) {
                    continue;
                }
                int itemId = tier.itemFor(piece);
                if (bot().getInventory().containsItem(itemId, 1) && tier.ordinal() > best.ordinal()) {
                    best = tier;
                }
            }
            if (best != currentTier && best != LumbridgeIronmanData.GearTier.NONE) {
                api().equipItemId(best.itemFor(piece));
            }
        }
    }

    private void equipBestTool(LumbridgeIronmanData.GearPiece piece) {
        // Tools (pickaxe/hatchet) share the weapon slot. If the bot already has a real
        // weapon or any tool equipped, leave it alone — otherwise pickaxe and hatchet
        // would clobber each other every tick.
        int currentWeapon = bot().getEquipment().getWeaponId();
        if (currentWeapon > 0 && isCombatWeapon(currentWeapon)) {
            return;
        }
        if (currentWeapon > 0 && isAnyTool(currentWeapon)) {
            return;
        }
        LumbridgeIronmanData.GearTier best = LumbridgeIronmanData.GearTier.NONE;
        for (LumbridgeIronmanData.GearTier tier : LumbridgeIronmanData.GearTier.values()) {
            if (tier == LumbridgeIronmanData.GearTier.NONE) {
                continue;
            }
            int itemId = tier.itemFor(piece);
            if (bot().getInventory().containsItem(itemId, 1) && tier.ordinal() > best.ordinal()) {
                best = tier;
            }
        }
        if (best != LumbridgeIronmanData.GearTier.NONE) {
            api().equipItemId(best.itemFor(piece));
        }
    }

    private static boolean isCombatWeapon(int itemId) {
        for (LumbridgeIronmanData.GearTier tier : LumbridgeIronmanData.GearTier.values()) {
            if (tier == LumbridgeIronmanData.GearTier.NONE) {
                continue;
            }
            if (tier.weapon == itemId) {
                return true;
            }
        }
        return false;
    }

    private static boolean isAnyTool(int itemId) {
        for (LumbridgeIronmanData.GearTier tier : LumbridgeIronmanData.GearTier.values()) {
            if (tier == LumbridgeIronmanData.GearTier.NONE) {
                continue;
            }
            if (tier.pickaxe == itemId || tier.hatchet == itemId) {
                return true;
            }
        }
        return false;
    }

    private int currentEquippedId(LumbridgeIronmanData.GearPiece piece) {
        switch (piece) {
            case WEAPON:
            case PICKAXE:
            case HATCHET:
                return bot().getEquipment().getWeaponId();
            case HELM:
                return bot().getEquipment().getHatId();
            case BODY:
                return bot().getEquipment().getChestId();
            case LEGS:
                return bot().getEquipment().getLegsId();
            case SHIELD:
                return bot().getEquipment().getShieldId();
            default:
                return -1;
        }
    }

    private LumbridgeIronmanData.GearTier tierForItem(int itemId) {
        for (LumbridgeIronmanData.GearTier tier : LumbridgeIronmanData.GearTier.values()) {
            if (tier.contains(itemId)) {
                return tier;
            }
        }
        return LumbridgeIronmanData.GearTier.NONE;
    }
}
