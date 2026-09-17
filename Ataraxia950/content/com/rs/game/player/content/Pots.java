package com.rs.game.player.content;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableBiMap;
import com.rs.Settings;
import com.rs.cache.Cache;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cores.CoresManager;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.Hit;
import com.rs.game.Hit.HitLook;
import com.rs.game.activites.clanwars.FfaZone;
import com.rs.game.activites.dnd.eviltree.EvilTree;
import com.rs.game.activites.dungeon_architect.DungeonArchitectController;
import com.rs.game.item.Item;
import com.rs.game.npc.familiar.Familiar;
import com.rs.game.player.BuffDebuffTimersManager.Timer;
import com.rs.game.player.FarmingManager;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.herblore.Herblore;
import com.rs.game.player.actions.invention.InventionConstants.Perks;
import com.rs.game.player.actions.invention.InventionData.Perk;
import com.rs.game.player.content.petperks.PetPerk;
import com.rs.game.player.content.petperks.PetPerkUtils;
import com.rs.game.player.controllers.CrucibleController;
import com.rs.game.player.controllers.DungeonController;
import com.rs.game.player.controllers.Dungeoneering;
import com.rs.game.player.controllers.Wilderness;
import com.rs.game.player.dialogue.Dialogue;
import com.rs.game.tasks.WorldTask;
import com.rs.game.tasks.WorldTasksManager;
import com.rs.utils.Colors;
import com.rs.utils.InputIntegerEvent;
import com.rs.utils.Logger;
import com.rs.utils.Utils;

public final class Pots {

    public static final int VIAL = 229;
    public static final int POTION_FLASK = 23191;

    public static Pot getPot(int id) {
        for (Pot pot : Pot.values())
            for (int potionId : pot.id) {
                if (id == potionId)
                    return pot;
            }
        return null;
    }

    public static int getDoses(Pot pot, Item item) {
        for (int i = pot.id.length - 1; i >= 0; i--) {
            if (pot.id[i] == item.getId())
                return pot.id.length - i;
        }
        return 0;
    }

    public static boolean sameType(Pot pot, Pot pot2) {
        return pot != null && pot2 != null && pot.getEffect() == pot2.getEffect();
    }

    public static void main(String[] args) throws Exception {
        Cache.init();
        for (Pot pot : Pot.values()) {
            if (pot.isFlask() && pot.toPotion() == null)
                Logger.getGlobal().info("has flask null ->" + pot.name());
        }
    }

    public static boolean emptyPot(Player player, Item item, int slot) {
        Pot pot = getPot(item.getId());
        if (item.getId() != 17492) {
            if (pot == null || pot.isFlask())
                return false;
        }
        if (player.getControlerManager().getControler() instanceof DungeonController)
            item.setId(17490);
        else
            item.setId(VIAL);
        player.getInventory().refresh(slot);
        player.sendMessage("You empty the vial.", true);
        return true;
    }

    public static boolean pot(Player player, Item item, int slot) {
        Pot pot = getPot(item.getId());
        if (pot == null)
            return false;
        if (player.getPotDelay() > Utils.currentTimeMillis())
            return true;
        if (!player.getControlerManager().canPot(pot))
            return true;
        if (!pot.effect.canDrink(player))
            return true;
        player.addPotDelay(1075);
        pot.effect.extra(player);
        int dosesLeft = getDoses(pot, item) - 1;
        if (dosesLeft == 0)
            player.getInventory().deleteItem(slot, item);
        else {
            player.getInventory().getItems().set(slot, new Item(dosesLeft > 0 ? pot.getIdForDoses(dosesLeft) : pot.isPotion() ? VIAL : getReplacedId(pot), 1));
            player.getInventory().refresh(slot);
        }
        for (int skillId : pot.effect.affectedSkills)
            player.getSkills().set(skillId, pot.effect.getAffectedSkill(player, skillId, player.getSkills().getLevel(skillId), player.getSkills().getLevelForXp(skillId)));
        if (!player.isCantDoDefenceEmote())
            player.setNextAnimationForce(new Animation(player.isShadow() ? 28499 : player.isUnderCombat() ? 18003 : 18000));
        player.getPackets().sendSound(4580, 0, 1);
        if (pot.isFlask() || pot.isPotion()) {
            player.sendMessage(pot.effect.drinkMessage != null ? pot.effect.drinkMessage : "You drink some of your " + item.getDefinitions().getName().toLowerCase().replace(" (1)", "").replace(" (2)", "").replace(" (3)", "").replace(" (4)", "").replace(" (5)", "").replace(" (6)", "") + ".", true);
            player.sendMessage(dosesLeft == 0 ? "You finish the last of your potion and smash the " + (pot.isFlask() ? "flask" : "vial") + " on the ground." : "You have " + dosesLeft + " dose" + (dosesLeft > 1 ? "s" : "") + " remaining.", true);
        }
        return true;
    }

    @SuppressWarnings("incomplete-switch")
    private static int getReplacedId(Pot pot) {
        switch (pot) {
            case JUG:
                return 1935;
            case BEER:
                return 1919;
        }
        return 0;
    }

    public static void resetBuffs(Player player) {
        player.setOverloadDelay(0);
        player.setSupremeOverloadDelay(0);
        player.setPrayerRenewalDelay(0);
        player.setPolDelay(0);
        player.setLastBonfire(0);
        player.setHitpoints(player.getSkills().getLevel(Skills.HITPOINTS * 10));
        player.getEquipment().refreshConfigs(false);
        player.refreshHitPoints();
        player.setAggressiveDelay(0);
        player.setWeakWeaponPoison(Utils.currentTimeMillis());
        player.setMediumWeaponPoison(Utils.currentTimeMillis());
        player.setStrongWeaponPoison(Utils.currentTimeMillis());
        player.getBuffDebuffTimersManager().removeTimer(Timer.OVERLOADED);
        player.getBuffDebuffTimersManager().removeTimer(Timer.SUPREME_OVERLOAD_POTION_ACTIVE);
        player.getBuffDebuffTimersManager().removeTimer(Timer.AGGRESSION_POTION_ACTIVE);
        player.getBuffDebuffTimersManager().removeTimer(Timer.PRAYER_RENEW_ACTIVE);
        player.getBuffDebuffTimersManager().removeTimer(Timer.BONFIRE_BOOST_ACTIVE);
    }

    public static void resetOverLoadEffect(Player player) {
        if (!player.isDead()) {
            int virtualLevel = player.getSkills().getLevel(Skills.ATTACK);
            int realLevel = player.getSkills().getLevelForXp(Skills.ATTACK);
            if (virtualLevel > realLevel)
                player.getSkills().set(Skills.ATTACK, realLevel);
            virtualLevel = player.getSkills().getLevel(Skills.STRENGTH);
            realLevel = player.getSkills().getLevelForXp(Skills.STRENGTH);
            if (virtualLevel > realLevel)
                player.getSkills().set(Skills.STRENGTH, realLevel);
            virtualLevel = player.getSkills().getLevel(Skills.DEFENCE);
            realLevel = player.getSkills().getLevelForXp(Skills.DEFENCE);
            if (virtualLevel > realLevel)
                player.getSkills().set(Skills.DEFENCE, realLevel);
            virtualLevel = player.getSkills().getLevel(Skills.MAGIC);
            realLevel = player.getSkills().getLevelForXp(Skills.MAGIC);
            if (virtualLevel > realLevel)
                player.getSkills().set(Skills.MAGIC, realLevel);
            virtualLevel = player.getSkills().getLevel(Skills.RANGE);
            realLevel = player.getSkills().getLevelForXp(Skills.RANGE);
            if (virtualLevel > realLevel)
                player.getSkills().set(Skills.RANGE, realLevel);
            player.heal(500);
        }
        player.setOverloadDelay(0);
        player.getBuffDebuffTimersManager().removeTimer(Timer.OVERLOADED);
        player.getPackets().sendGameMessage("<col=930000>The effects of overload have worn off and you feel normal again.");
    }

    public static void resetSupremeOverLoadEffect(Player player) {
        if (!player.isDead()) {
            int virtualLevel = player.getSkills().getLevel(Skills.ATTACK);
            int realLevel = player.getSkills().getLevelForXp(Skills.ATTACK);
            if (virtualLevel > realLevel)
                player.getSkills().set(Skills.ATTACK, realLevel);
            virtualLevel = player.getSkills().getLevel(Skills.STRENGTH);
            realLevel = player.getSkills().getLevelForXp(Skills.STRENGTH);
            if (virtualLevel > realLevel)
                player.getSkills().set(Skills.STRENGTH, realLevel);
            virtualLevel = player.getSkills().getLevel(Skills.DEFENCE);
            realLevel = player.getSkills().getLevelForXp(Skills.DEFENCE);
            if (virtualLevel > realLevel)
                player.getSkills().set(Skills.DEFENCE, realLevel);
            virtualLevel = player.getSkills().getLevel(Skills.MAGIC);
            realLevel = player.getSkills().getLevelForXp(Skills.MAGIC);
            if (virtualLevel > realLevel)
                player.getSkills().set(Skills.MAGIC, realLevel);
            virtualLevel = player.getSkills().getLevel(Skills.RANGE);
            realLevel = player.getSkills().getLevelForXp(Skills.RANGE);
            if (virtualLevel > realLevel)
                player.getSkills().set(Skills.RANGE, realLevel);
            player.heal(500);
        }
        player.setSupremeOverloadDelay(0);
        player.getBuffDebuffTimersManager().removeTimer(Timer.SUPREME_OVERLOAD_POTION_ACTIVE);
        player.getPackets().sendGameMessage("<col=930000>The effects of supreme overload have worn off and you feel normal again.");
    }

    public static void applySupremeOverLoadEffect(Player player) {
        if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
            int virtualLevel = player.getSkills().getLevel(Skills.ATTACK);
            int realLevel = player.getSkills().getLevelForXp(Skills.ATTACK);
            int level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.ATTACK, (int) (level + 7 + (realLevel * 0.15)));

            virtualLevel = player.getSkills().getLevel(Skills.STRENGTH);
            realLevel = player.getSkills().getLevelForXp(Skills.STRENGTH);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.STRENGTH, (int) (level + 7 + (realLevel * 0.15)));

            virtualLevel = player.getSkills().getLevel(Skills.DEFENCE);
            realLevel = player.getSkills().getLevelForXp(Skills.DEFENCE);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.DEFENCE, (int) (level + 7 + (realLevel * 0.15)));

            virtualLevel = player.getSkills().getLevel(Skills.MAGIC);
            realLevel = player.getSkills().getLevelForXp(Skills.MAGIC);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.MAGIC, (int) (level + 5 + (realLevel * 0.1)));

            virtualLevel = player.getSkills().getLevel(Skills.RANGE);
            realLevel = player.getSkills().getLevelForXp(Skills.RANGE);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.RANGE, (int) (level + 6 + (realLevel * 0.1)));
        } else {
            int virtualLevel = player.getSkills().getLevel(Skills.ATTACK);
            int realLevel = player.getSkills().getLevelForXp(Skills.ATTACK);
            int level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.ATTACK, (int) (level + 7 + (realLevel * 0.22)));

            virtualLevel = player.getSkills().getLevel(Skills.STRENGTH);
            realLevel = player.getSkills().getLevelForXp(Skills.STRENGTH);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.STRENGTH, (int) (level + 7 + (realLevel * 0.22)));

            virtualLevel = player.getSkills().getLevel(Skills.DEFENCE);
            realLevel = player.getSkills().getLevelForXp(Skills.DEFENCE);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.DEFENCE, (int) (level + 7 + (realLevel * 0.22)));

            virtualLevel = player.getSkills().getLevel(Skills.MAGIC);
            realLevel = player.getSkills().getLevelForXp(Skills.MAGIC);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.MAGIC, (int) (level + 5 + (realLevel * 0.2)));

            virtualLevel = player.getSkills().getLevel(Skills.RANGE);
            realLevel = player.getSkills().getLevelForXp(Skills.RANGE);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.RANGE, (int) (level + 6 + (Math.floor(realLevel / 5.2))));
        }
    }

    public static void applyOverLoadEffect(Player player) {
        if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
            int virtualLevel = player.getSkills().getLevel(Skills.ATTACK);
            int realLevel = player.getSkills().getLevelForXp(Skills.ATTACK);
            int level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.ATTACK, (int) (level + 5 + (realLevel * 0.15)));

            virtualLevel = player.getSkills().getLevel(Skills.STRENGTH);
            realLevel = player.getSkills().getLevelForXp(Skills.STRENGTH);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.STRENGTH, (int) (level + 5 + (realLevel * 0.15)));

            virtualLevel = player.getSkills().getLevel(Skills.DEFENCE);
            realLevel = player.getSkills().getLevelForXp(Skills.DEFENCE);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.DEFENCE, (int) (level + 5 + (realLevel * 0.15)));

            virtualLevel = player.getSkills().getLevel(Skills.MAGIC);
            realLevel = player.getSkills().getLevelForXp(Skills.MAGIC);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.MAGIC, (int) (level + 5 + (realLevel * 0.1)));

            virtualLevel = player.getSkills().getLevel(Skills.RANGE);
            realLevel = player.getSkills().getLevelForXp(Skills.RANGE);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.RANGE, (int) (level + 5 + (realLevel * 0.1)));
        } else {
            int virtualLevel = player.getSkills().getLevel(Skills.ATTACK);
            int realLevel = player.getSkills().getLevelForXp(Skills.ATTACK);
            int level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.ATTACK, (int) (level + 5 + (realLevel * 0.22)));

            virtualLevel = player.getSkills().getLevel(Skills.STRENGTH);
            realLevel = player.getSkills().getLevelForXp(Skills.STRENGTH);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.STRENGTH, (int) (level + 5 + (realLevel * 0.22)));

            virtualLevel = player.getSkills().getLevel(Skills.DEFENCE);
            realLevel = player.getSkills().getLevelForXp(Skills.DEFENCE);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.DEFENCE, (int) (level + 5 + (realLevel * 0.22)));

            virtualLevel = player.getSkills().getLevel(Skills.MAGIC);
            realLevel = player.getSkills().getLevelForXp(Skills.MAGIC);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.MAGIC, (int) (level + 5 + (realLevel * 0.15)));

            virtualLevel = player.getSkills().getLevel(Skills.RANGE);
            realLevel = player.getSkills().getLevelForXp(Skills.RANGE);
            level = virtualLevel > realLevel ? realLevel : virtualLevel;
            player.getSkills().set(Skills.RANGE, (int) (level + 4 + (Math.floor(realLevel / 5.2))));
        }
    }

    public enum Pot {

        ATTACK_POTION(new int[]{2428, 121, 123, 125}, Effects.ATTACK_POTION),

        STRENGTH_POTION(new int[]{113, 115, 117, 119}, Effects.STRENGTH_POTION),

        DEFENCE_POTION(new int[]{2432, 133, 135, 137}, Effects.DEFENCE_POTION),

        RANGING_POTION(new int[]{27504, 27506, 27508, 27510}, Effects.RANGE_POTION),

        MAGIC_POTION(new int[]{27512, 27514, 27516, 27518}, Effects.MAGIC_POTION),

        MAGIC_FLASK(new int[]{27532, 27534, 27536, 27538, 27540, 27542}, Effects.MAGIC_POTION),

        SUPER_MAGIC_POTION(new int[]{3040, 3042, 3044, 3046}, Effects.SUPER_MAGIC_POTION),

        SUPER_MAGIC_FLASK(new int[]{23423, 23425, 23427, 23429, 23431, 23433}, Effects.SUPER_MAGIC_POTION),

        ANTIPOISON_POTION(new int[]{2446, 175, 177, 179}, Effects.ANTIPOISON),

        SUPER_ANTIPOISON_POTION(new int[]{2448, 181, 183, 185}, Effects.SUPER_ANTIPOISON),

        ANTIPOISON_PLUS(new int[]{5943, 5945, 5947, 5949}, Effects.ANTIPOISON_PLUS),

        ANTIPOISON_PLUS_FLASK(new int[]{23579, 23581, 23583, 23585, 23587, 23589}, Effects.ANTIPOISON_PLUS),

        ANTIPOISON_PLUS_PLUS(new int[]{5952, 5954, 5956, 5958}, Effects.ANTIPOISON_PLUS_PLUS),

        ANTIPOISON_PLUS_PLUS_FLASK(new int[]{23591, 23593, 23595, 23597, 23599, 23601}, Effects.ANTIPOISON_PLUS_PLUS),

        PRAYER_POTION(new int[]{2434, 139, 141, 143}, Effects.PRAYER_POTION),

        SUPER_ATT_POTION(new int[]{2436, 145, 147, 149}, Effects.SUPER_ATT_POTION),

        SUPER_STR_POTION(new int[]{2440, 157, 159, 161}, Effects.SUPER_STR_POTION),

        SUPER_DEF_POTION(new int[]{2442, 163, 165, 167}, Effects.SUPER_DEF_POTION),

        ENERGY_POTION(new int[]{3008, 3010, 3012, 3014}, Effects.ENERGY_POTION),

        SUPER_ENERGY(new int[]{3016, 3018, 3020, 3022}, Effects.SUPER_ENERGY),

        EXTREME_ATT_POTION(new int[]{15308, 15309, 15310, 15311}, Effects.EXTREME_ATT_POTION),

        EXTREME_STR_POTION(new int[]{15312, 15313, 15314, 15315}, Effects.EXTREME_STR_POTION),

        EXTREME_DEF_POTION(new int[]{15316, 15317, 15318, 15319}, Effects.EXTREME_DEF_POTION),

        EXTREME_MAGE_POTION(new int[]{15320, 15321, 15322, 15323}, Effects.EXTREME_MAG_POTION),

        EXTREME_RANGE_POTION(new int[]{15324, 15325, 15326, 15327}, Effects.EXTREME_RAN_POTION),

        SUPER_RESTORE_POTION(new int[]{3024, 3026, 3028, 3030}, Effects.SUPER_RESTORE),

        SARADOMIN_BREW(new int[]{6685, 6687, 6689, 6691}, Effects.SARADOMIN_BREW),

        SUPER_SARADOMIN_BREW(new int[]{28191, 28193, 28195, 28197}, Effects.SUPER_SARADOMIN_BREW),

        SUPER_SARADOMIN_BREW_FLASK(new int[]{28227, 28229, 28231, 28233, 28235, 28237}, Effects.SUPER_SARADOMIN_BREW),

        ZAMORAK_BREW(new int[]{2450, 189, 191, 193}, Effects.ZAMORAK_BREW),

        SUPER_ZAMORAK_BREW(new int[]{28199, 28201, 28203, 28205}, Effects.SUPER_ZAMORAK_BREW),

        SUPER_ZAMORAK_BREW_FLASK(new int[]{28215, 28217, 28219, 28221, 28223, 28225}, Effects.SUPER_ZAMORAK_BREW),

        RECOVER_SPECIAL(new int[]{15300, 15301, 15302, 15303}, Effects.RECOVER_SPECIAL),

        SUPER_PRAYER(new int[]{15328, 15329, 15330, 15331}, Effects.SUPER_PRAYER),

        OVERLOAD(new int[]{15332, 15333, 15334, 15335}, Effects.OVERLOAD),

        ANTIFIRE_POTION(new int[]{2452, 2454, 2456, 2458}, Effects.ANTI_FIRE),

        SUPER_ANTIFIRE(new int[]{15304, 15305, 15306, 15307}, Effects.SUPER_ANTI_FIRE),

        SUMMONING_POTION(new int[]{12140, 12142, 12144, 12146}, Effects.SUMMONING_POT),

        SUMMONING_FLASK(new int[]{23621, 23623, 23625, 23627, 23629, 23631}, Effects.SUMMONING_POT),

        SANFEW_SERUM(new int[]{10925, 10927, 10929, 10931}, Effects.SANFEW_SERUM),

        PRAYER_RENEWAL(new int[]{21630, 21632, 21634, 21636}, Effects.PRAYER_RENEWAL),

        PRAYER_RENEWAL_FLASK(new int[]{23609, 23611, 23613, 23615, 23617, 23619}, Effects.PRAYER_RENEWAL),

        ATTACK_FLASK(new int[]{23195, 23197, 23199, 23201, 23203, 23205}, Effects.ATTACK_POTION),

        STRENGTH_FLASK(new int[]{23207, 23209, 23211, 23213, 23215, 23217}, Effects.STRENGTH_POTION),

        RESTORE_POTION(new int[]{2430, 127, 129, 131}, Effects.RESTORE_POTION),

        RESTORE_FLASK(new int[]{23219, 23221, 23223, 23225, 23227, 23229}, Effects.RESTORE_POTION),

        DEFENCE_FLASK(new int[]{23231, 23233, 23235, 23237, 23239, 23241}, Effects.DEFENCE_POTION),

        PRAYER_FLASK(new int[]{23243, 23245, 23247, 23249, 23251, 23253}, Effects.PRAYER_POTION),

        SUPER_ATT_FLASK(new int[]{23255, 23257, 23259, 23261, 23263, 23265}, Effects.SUPER_ATT_POTION),

        FISHING_POTION(new int[]{2438, 151, 153, 155}, Effects.FISHING_POTION),

        FISHING_FLASK(new int[]{23267, 23269, 23271, 23273, 23275, 23277}, Effects.FISHING_POTION),

        SUPER_STR_FLASK(new int[]{23279, 23281, 23283, 23285, 23287, 23289}, Effects.SUPER_STR_POTION),

        SUPER_DEF_FLASK(new int[]{23291, 23293, 23295, 23297, 23299, 23301}, Effects.SUPER_DEF_POTION),

        RANGING_FLASK(new int[]{27520, 27522, 27524, 27526, 27528, 27530}, Effects.RANGE_POTION),

        SUPER_RANGING_FLASK(new int[]{23303, 23305, 23307, 23309, 23311, 23313}, Effects.SUPER_RANGE_POTION),

        SUPER_RANGING_POTION(new int[]{2444, 169, 171, 173}, Effects.SUPER_RANGE_POTION),

        COMBAT_POTION(new int[]{9739, 9741, 9743, 9745,}, Effects.COMBAT_POTION),

        COMBAT_FLASK(new int[]{23447, 23449, 23451, 23453, 23455, 23457}, Effects.COMBAT_POTION),

        ANTIPOISON_FLASK(new int[]{23315, 23317, 23319, 23321, 23323, 23325}, Effects.ANTIPOISON),

        SUPER_ANTIPOISON_FLASK(new int[]{23327, 23329, 23331, 23333, 23335, 23337}, Effects.SUPER_ANTIPOISON),

        SUPER_ANTIFIRE_FLASK(new int[]{23489, 23490, 23491, 23492, 23493, 23494}, Effects.SUPER_ANTI_FIRE),

        SARADOMIN_BREW_FLASK(new int[]{23351, 23353, 23355, 23357, 23359, 23361}, Effects.SARADOMIN_BREW),

        ZAMORAK_BREW_FLASK(new int[]{23339, 23341, 23343, 23345, 23347, 23349}, Effects.ZAMORAK_BREW),

        ANTIFIRE_FLASK(new int[]{23363, 23365, 23367, 23369, 23371, 23373}, Effects.ANTI_FIRE),

        ENERGY_FLASK(new int[]{23375, 23377, 23379, 23381, 23383, 23385}, Effects.ENERGY_POTION),

        SUPER_ENERGY_FLASK(new int[]{23387, 23389, 23391, 23393, 23395, 23397}, Effects.SUPER_ENERGY),

        AGILITY_POTION(new int[]{3032, 3034, 3036, 3038}, Effects.AGILITY_POTION),

        AGILITY_FLASK(new int[]{23411, 23413, 23415, 23417, 23419, 23421}, Effects.AGILITY_POTION),

        SUPER_RESTORE_FLASK(new int[]{23399, 23401, 23403, 23405, 23407, 23409}, Effects.SUPER_RESTORE),

        RECOVER_SPECIAL_FLASK(new int[]{23483, 23484, 23485, 23486, 23487, 23488}, Effects.RECOVER_SPECIAL),

        EXTREME_ATT_FLASK(new int[]{23495, 23496, 23497, 23498, 23499, 23500}, Effects.EXTREME_ATT_POTION),

        EXTREME_STR_FLASK(new int[]{23501, 23502, 23503, 23504, 23505, 23506}, Effects.EXTREME_STR_POTION),

        EXTREME_DEF_FLASK(new int[]{23507, 23508, 23509, 23510, 23511, 23512}, Effects.EXTREME_DEF_POTION),

        EXTREME_MAGE_FLASK(new int[]{23513, 23514, 23515, 23516, 23517, 23518}, Effects.EXTREME_MAG_POTION),

        EXTREME_RANGE_FLASK(new int[]{23519, 23520, 23521, 23522, 23523, 23524}, Effects.EXTREME_RAN_POTION),

        SUPER_PRAYER_FLASK(new int[]{23525, 23526, 23527, 23528, 23529, 23530}, Effects.SUPER_PRAYER),

        OVERLOAD_FLASK(new int[]{23531, 23532, 23533, 23534, 23535, 23536}, Effects.OVERLOAD),

        GRAND_STRENGTH(new int[]{32958, 32956, 32954, 32952, 32950, 32948}, Effects.GRAND_STRENGTH),

        GRAND_RANGING(new int[]{32970, 32968, 32966, 32964, 32962, 32960}, Effects.GRAND_RANGING),

        GRAND_MAGIC(new int[]{32982, 32980, 32978, 32976, 32974, 32972}, Effects.GRAND_MAGE),

        GRAND_ATTACK(new int[]{32994, 32992, 32990, 32988, 32986, 32984}, Effects.GRAND_ATTACK),

        GRAND_DEFENCE(new int[]{33006, 33004, 33002, 33000, 32998, 32996}, Effects.GRAND_DEFENCE),

        SUPER_MELEE(new int[]{33018, 33016, 33014, 33012, 33010, 33008}, Effects.SUPER_MELEE),

        SUPER_WARMASTER(new int[]{33030, 33028, 33026, 33024, 33022, 33020}, Effects.SUPER_WARMASTER),

        SUPER_PRAYER_RENEWAL(new int[]{33186, 33184, 33182, 33180, 33178, 33176}, Effects.SUPER_PRAYER_RENEWAL),

        EXTREME_BRAWLER(new int[]{33066, 33064, 33062, 33060, 33058, 33056}, Effects.EXTREME_BRAWLER),

        EXTREME_BATTLEMAGE(new int[]{33078, 33076, 33074, 33072, 33070, 33068}, Effects.EXTREME_BATTLEMAGE),

        EXTREME_SHARPSHOOTER(new int[]{33090, 33088, 33086, 33084, 33082, 33080}, Effects.EXTREME_SHARPSHOOTER),

        EXTREME_WARMASTER(new int[]{33102, 33100, 33098, 33096, 33094, 33092}, Effects.EXTREME_WARMASTER),

        WYRMFIRE(new int[]{33054, 33052, 33050, 33048, 33046, 33044}, Effects.WYRMFIRE),

        SUPREME_ATTACK(new int[]{33126, 33124, 33122, 33120, 33118, 33116}, Effects.SUPREME_ATTACK),

        SUPREME_DEFENCE(new int[]{33138, 33136, 33134, 33132, 33130, 33128}, Effects.SUPREME_DEFENCE),

        SUPREME_MAGIC(new int[]{33150, 33148, 33146, 33144, 33142, 33140}, Effects.SUPREME_MAGIC),

        SUPREME_RANGING(new int[]{33162, 33160, 33158, 33156, 33154, 33152}, Effects.SUPREME_RANGING),

        SUPREME_STRENGTH(new int[]{33114, 33112, 33110, 33108, 33106, 33104}, Effects.SUPREME_STRENGTH),

        BRIGHTFIRE(new int[]{33174, 33172, 33170, 33168, 33166, 33164}, Effects.BRIGHTFIRE),

        REPLENISHMENT(new int[]{33042, 33040, 33038, 33036, 33034, 33032}, Effects.REPLENISHMENT),

        HOLY_OVERLOAD(new int[]{33246, 33244, 33242, 33240, 33238, 33236}, Effects.HOLY_OVERLOAD),

        SEARING_OVERLOAD(new int[]{33258, 33256, 33254, 33252, 33250, 33248}, Effects.SEARING_OVERLOAD),

        OVERLOAD_SALVE(new int[]{33198, 33196, 33194, 33192, 33190, 33188}, Effects.OVERLOAD_SALVE),

        SUPREME_OVERLOAD(new int[]{33210, 33208, 33206, 33204, 33202, 33200}, Effects.SUPREME_OVERLOAD),

        SUPREME_OVERLOAD_SALVE(new int[]{33222, 33220, 33218, 33216, 33214, 33212}, Effects.SUPREME_OVERLOAD_SALVE),

        AGGRESSION_POTION(new int[]{37971, 37969, 37967, 37965}, Effects.AGGRESSION_POTION),

        AGGRESSION_FLASK(new int[]{37939, 37937, 37935, 37933, 37931, 37929}, Effects.AGGRESSION_POTION),

        BEER(new int[]{1917, 1919}, Effects.BEER),

        JUG(new int[]{1993, 1935}, Effects.WINE),

        DUNGEONEERING_WEAK_MAGIC_POTION(new int[]{17556, 17490}, Effects.DUNGEONEERING_WEAK_MAGIC_POTION),

        DUNGEONEERING_WEAK_RANGED_POTION(new int[]{17558, 17490}, Effects.DUNGEONEERING_WEAK_RANGED_POTION),

        DUNGEONEERING_WEAK_MELEE_POTION(new int[]{17560, 17490}, Effects.DUNGEONEERING_WEAK_MELEE_POTION),

        DUNGEONEERING_WEAK_DEFENCE_POTION(new int[]{17562, 17490}, Effects.DUNGEONEERING_WEAK_DEFENCE_POTION),

        DUNGEONEERING_WEAK_STAT_RESTORE_POTION(new int[]{17564, 17490}, Effects.DUNGEONEERING_STAT_RESTORE_POTION),

        DUNGEONEERING_WEAK_CURE_POTION(new int[]{17568, 17490}, Effects.DUNGEONEERING_WEAK_CURE),

        DUNGEONEERING_WEAK_REJUVENATION_POTION(new int[]{17570, 17490}, Effects.DUNGEONEERING_WEAK_REJUVENATION_POTION),

        DUNGEONEERING_WEAK_POISON(new int[]{17572, 17490}, Effects.WEAK_WEAPON_POISON),

        DUNGEONEERING_WEAK_GATHERERS_POTION(new int[]{17574, 17490}, Effects.WEAK_GATHERERS_POTION),

        DUNGEONEERING_WEAK_ARTISANS_POTION(new int[]{17576, 17490}, Effects.WEAK_ARTISANS_POTION),

        DUNGEONEERING_WEAK_NATURALISTS_POTION(new int[]{17578, 17490}, Effects.WEAK_NATURALISTS_POTION),

        DUNGEONEERING_WEAK_SURVIVALISTS_POTION(new int[]{17580, 17490}, Effects.WEAK_SURVIVALISTS_POTION),

        DUNGEONEERING_MAGIC_POTION(new int[]{17582, 17490}, Effects.DUNGEONEERING_MAGIC_POTION),

        DUNGEONEERING_RANGED_POTION(new int[]{17584, 17490}, Effects.DUNGEONEERING_RANGED_POTION),

        DUNGEONEERING_MELEE_POTION(new int[]{17586, 17490}, Effects.DUNGEONEERING_MELEE_POTION),

        DUNGEONEERING_DEFENCE_POTION(new int[]{17588, 17490}, Effects.DUNGEONEERING_DEFENCE_POTION),

        DUNGEONEERING_STAT_RESTORE_POTION(new int[]{17590, 17490}, Effects.DUNGEONEERING_STAT_RESTORE_POTION),

        DUNGEONEERING_CURE_POTION(new int[]{17592, 17490}, Effects.DUNGEONEERING_CURE),

        DUNGEONEERING_REJUVENATION_POTION(new int[]{17594, 17490}, Effects.DUNGEONEERING_REJUVENATION_POTION),

        DUNGEONEERING_POISON(new int[]{17596, 17572, 17490}, Effects.WEAPON_POISON),

        DUNGEONEERING_GATHERERS_POTION(new int[]{17598, 17490}, Effects.GATHERERS_POTION),

        DUNGEONEERING_ARTISANS_POTION(new int[]{17600, 17490}, Effects.ARTISANS_POTION),

        DUNGEONEERING_NATURALISTS_POTION(new int[]{17602, 17490}, Effects.NATURALISTS_POTION),

        DUNGEONEERING_SURVIVALISTS_POTION(new int[]{17604, 17490}, Effects.SURVIVALISTS_POTION),

        DUNGEONEERING_STRONG_MAGIC_POTION(new int[]{17606, 17490}, Effects.STRONG_DUNGEONEERING_MAGIC_POTION),

        DUNGEONEERING_STRONG_RANGED_POTION(new int[]{17608, 17490}, Effects.STRONG_DUNGEONEERING_RANGED_POTION),

        DUNGEONEERING_STRONG_MELEE_POTION(new int[]{17610, 17490}, Effects.STRONG_DUNGEONEERING_MELEE_POTION),

        DUNGEONEERING_STRONG_DEFENCE_POTION(new int[]{17612, 17490}, Effects.STRONG_DUNGEONEERING_DEFENCE_POTION),

        DUNGEONEERING_STRONG_STAT_RESTORE_POTION(new int[]{17614, 17490}, Effects.STRONG_DUNGEONEERING_STAT_RESTORE_POTION),

        DUNGEONEERING_STRONG_CURE_POTION(new int[]{17616, 17490}, Effects.STRONG_DUNGEONEERING_CURE),

        DUNGEONEERING_STRONG_REJUVENATION_POTION(new int[]{17618, 17490}, Effects.STRONG_DUNGEONEERING_REJUVENATION_POTION),

        DUNGEONEERING_STRONG_POISON(new int[]{17620, 17596, 17572, 17490}, Effects.STRONG_WEAPON_POISON),

        DUNGEONEERING_STRONG_GATHERERS_POTION(new int[]{17622, 17490}, Effects.STRONG_GATHERERS_POTION),

        DUNGEONEERING_STRONG_ARTISANS_POTION(new int[]{17624, 17490}, Effects.STRONG_ARTISANS_POTION),

        DUNGEONEERING_STRONG_NATURALISTS_POTION(new int[]{17626, 17490}, Effects.STRONG_NATURALISTS_POTION),
        PERFECT_WOODCUTTING_JUJU(new int[]{32759, 32757, 32755, 32753, Herblore.EMPTY_JUJU_VIAL}, Effects.PERFECT_WOODCUTTING_JUJU),
        PERFECT_FARMING_JUJU(new int[]{32767, 32765, 32763, 32761, Herblore.EMPTY_JUJU_VIAL}, Effects.PERFECT_FARMING_JUJU),
        PERFECT_MINING_JUJU(new int[]{32775, 32773, 32771, 32769, Herblore.EMPTY_JUJU_VIAL}, Effects.PERFECT_MINING_JUJU),
        PERFECT_SMITHING_JUJU(new int[]{32783, 32781, 32779, 32777, Herblore.EMPTY_JUJU_VIAL}, Effects.PERFECT_SMITHING_JUJU),
        PERFECT_AGILITY_JUJU(new int[]{32791, 32789, 32787, 32785, Herblore.EMPTY_JUJU_VIAL}, Effects.PERFECT_AGILITY_JUJU),
        PERFECT_PRAYER_JUJU(new int[]{32799, 32797, 32795, 32793, Herblore.EMPTY_JUJU_VIAL}, Effects.PERFECT_PRAYER_JUJU),
        PERFECT_HERBLORE_JUJU(new int[]{32807, 32805, 32803, 32801, Herblore.EMPTY_JUJU_VIAL}, Effects.PERFECT_HERBLORE_JUJU),
        PERFECT_DUNGEONEERING_JUJU(new int[]{32815, 32813, 32811, 32809, Herblore.EMPTY_JUJU_VIAL}, Effects.PERFECT_DUNGEONEERING_JUJU),
        PERFECT_FISHING_JUJU(new int[]{35741, 35739, 35737, 35735, Herblore.EMPTY_JUJU_VIAL}, Effects.PERFECT_FISHING_JUJU),
        HUNTER_JUJU(new int[]{20023, 20024, 20025, 20026, Herblore.EMPTY_JUJU_VIAL}, Effects.HUNTER_JUJU),
        SCENTLESS_JUJU(new int[]{20027, 20028, 20029, 20030, Herblore.EMPTY_JUJU_VIAL}, Effects.SCENTLESS_JUJU),
        FARMING_JUJU(new int[]{20011, 20012, 20013, 20014, Herblore.EMPTY_JUJU_VIAL}, Effects.FARMING_JUJU),
        COOKING_JUJU(new int[]{20007, 20008, 20009, 20010, Herblore.EMPTY_JUJU_VIAL}, Effects.COOKING_JUJU),
        FISHING_JUJU(new int[]{20019, 20020, 20021, 20022, Herblore.EMPTY_JUJU_VIAL}, Effects.FISHING_JUJU),
        WOODCUTTING_JUJU(new int[]{20015, 20016, 20017, 20018, Herblore.EMPTY_JUJU_VIAL}, Effects.WOODCUTTING_JUJU),
        MINING_JUJU(new int[]{20003, 20004, 20005, 20006, Herblore.EMPTY_JUJU_VIAL}, Effects.MINING_JUJU),
        SARADOMINS_BLESSING_JUJU(new int[]{20031, 20032, 20033, 20034, Herblore.EMPTY_JUJU_VIAL}, Effects.SARADOMINS_BLESSING_JUJU),
        GUTHIXS_GIFT_JUJU(new int[]{20035, 20036, 20037, 20038, Herblore.EMPTY_JUJU_VIAL}, Effects.GUTHIXS_GIFT_JUJU),
        ZAMORAKS_FAVOUR_JUJU(new int[]{20039, 20040, 20041, 20042, Herblore.EMPTY_JUJU_VIAL}, Effects.ZAMORAKS_FAVOUR_JUJU),
        DUNGEONEERING_STRONG_SURVIVALISTS_POTION(new int[]{17628, 17490}, Effects.STRONG_SURVIVALISTS_POTION), NATURES_ESSENCE(new int[]{11433}, Effects.NATURES_ESSENCE), BLOOD_SERUM(new int[]{11509}, Effects.BLOOD_SERUM), SANFEW_SERUM_FLASK(new int[]{23567, 23569, 23571, 23573, 23575, 23577}, Effects.SANFEW_SERUM);

        public static final ImmutableBiMap<Pot, Pot> VIAL_TO_FLASK = ImmutableBiMap.<Pot, Pot>builder().put(ATTACK_POTION, ATTACK_FLASK).put(STRENGTH_POTION, STRENGTH_FLASK).put(DEFENCE_POTION, DEFENCE_FLASK).put(RANGING_POTION, RANGING_FLASK).put(MAGIC_POTION, MAGIC_FLASK).put(ANTIPOISON_POTION, ANTIPOISON_FLASK).put(ANTIPOISON_PLUS, ANTIPOISON_PLUS_FLASK).put(ANTIPOISON_PLUS_PLUS, ANTIPOISON_PLUS_PLUS_FLASK).put(PRAYER_POTION, PRAYER_FLASK).put(SUPER_ATT_POTION, SUPER_ATT_FLASK).put(SUPER_STR_POTION, SUPER_STR_FLASK).put(SUPER_DEF_POTION, SUPER_DEF_FLASK).put(SUPER_RANGING_POTION, SUPER_RANGING_FLASK).put(ENERGY_POTION, ENERGY_FLASK).put(SUPER_ENERGY, SUPER_ENERGY_FLASK).put(EXTREME_ATT_POTION, EXTREME_ATT_FLASK).put(EXTREME_STR_POTION, EXTREME_STR_FLASK).put(EXTREME_DEF_POTION, EXTREME_DEF_FLASK).put(EXTREME_MAGE_POTION, EXTREME_MAGE_FLASK).put(EXTREME_RANGE_POTION, EXTREME_RANGE_FLASK).put(SUPER_RESTORE_POTION, SUPER_RESTORE_FLASK).put(SARADOMIN_BREW, SARADOMIN_BREW_FLASK).put(SUPER_SARADOMIN_BREW, SUPER_SARADOMIN_BREW_FLASK).put(ZAMORAK_BREW, ZAMORAK_BREW_FLASK).put(SUPER_ZAMORAK_BREW, SUPER_ZAMORAK_BREW_FLASK).put(RECOVER_SPECIAL, RECOVER_SPECIAL_FLASK).put(SUPER_PRAYER, SUPER_PRAYER_FLASK).put(OVERLOAD, OVERLOAD_FLASK).put(ANTIFIRE_POTION, ANTIFIRE_FLASK).put(SUPER_ANTIFIRE, SUPER_ANTIFIRE_FLASK).put(SUMMONING_POTION, SUMMONING_FLASK).put(SANFEW_SERUM, SANFEW_SERUM_FLASK).put(PRAYER_RENEWAL, PRAYER_RENEWAL_FLASK).put(AGGRESSION_POTION, AGGRESSION_FLASK).put(COMBAT_POTION, COMBAT_FLASK).put(SUPER_ANTIPOISON_POTION, SUPER_ANTIPOISON_FLASK).build();
        public static final ImmutableBiMap<Pot, Pot> FLASK_TO_VIAL = VIAL_TO_FLASK.inverse();

        public int[] id;
        public int[] notedId;
        private final Effects effect;

        private static final HashMap<Pot, Pot> potion_flask = new HashMap<Pot, Pot>();

        static {
            for (Pot pot : Pot.values()) {
                if (pot.getMaxDoses() < 4)
                    continue;
                potion_flask.put(pot, pot.isFlask() ? pot.toPotion() : pot.toFlask());
            }
        }

        Pot(int[] id, Effects effect) {
            this.id = id;
            this.effect = effect;
            if (id != null) {
                notedId = Arrays.stream(id).map(it -> ItemDefinitions.getItemDefinitions(it).certId).toArray();
            }
        }

        public boolean matches(int itemUsed) {
            for (int identifier : id) {
                if (identifier == itemUsed)
                    return true;
            }
            return false;
        }

        public int[] getPotions() {
            return id;
        }

        public boolean isFlask() {
            return getMaxDoses() == 6;
        }

        public Effects getEffect() {
            return effect;
        }

        public boolean isPotion() {
            return getMaxDoses() == 4;
        }

        public int getMaxDoses() {
            return id.length;
        }

        public int getIdForDoses(int doses) {
            return id[getMaxDoses() - doses];
        }

        public int getNotedVialDose(int it) {
            if (isFlask())
                throw new RuntimeException();
            if (it == notedId[0]) {
                return 4;
            } else if (it == notedId[1]) {
                return 3;
            } else if (it == notedId[2]) {
                return 2;
            } else if (it == notedId[3]) {
                return 1;
            }
            throw new RuntimeException();
        }

        public int getVialDose(int it) {
            if (isFlask())
                throw new RuntimeException();
            if (it == id[0]) {
                return 4;
            } else if (it == id[1]) {
                return 3;
            } else if (it == id[2]) {
                return 2;
            } else if (it == id[3]) {
                return 1;
            }
            throw new RuntimeException();
        }

        public int getFlaskDose(int it) {
            if (isPotion())
                throw new RuntimeException();
            if (it == id[0]) {
                return 6;
            } else if (it == id[1]) {
                return 5;
            } else if (it == id[2]) {
                return 4;
            } else if (it == id[3]) {
                return 3;
            } else if (it == id[4]) {
                return 2;
            } else if (it == id[5]) {
                return 1;
            }
            throw new RuntimeException();
        }

        public Pot toFlask() {
            if (isFlask())
                return this;
            if (potion_flask.containsKey(this))
                return potion_flask.get(this);
            for (int i = 0; i < Pot.values().length; i++) {
                Pot pot = Pot.values()[i];
                if (pot == null || !pot.isFlask())
                    continue;
                String checkName = Pot.values()[i].name().toLowerCase().replace("_flask", "");
                String thisName = Pot.values()[ordinal()].name().toLowerCase().replace("_potion", "");
                if (checkName.equals(thisName))
                    return pot;
            }
            return null;
        }

        public Pot toPotion() {
            if (isPotion())
                return this;
            if (potion_flask.containsKey(this))
                return potion_flask.get(this);
            for (int i = 0; i < Pot.values().length; i++) {
                Pot pot = Pot.values()[i];
                if (pot == null || !pot.isPotion())
                    continue;
                String checkName = Pot.values()[i].name().toLowerCase().replace("_potion", "");
                String thisName = Pot.values()[ordinal()].name().toLowerCase().replace("_flask", "");
                if (checkName.equals(thisName))
                    return pot;
            }
            return null;
        }

        public int[] getNotedId() {
            int[] notedIds = new int[id.length];
            for (int i = 0; i < id.length; i++) {
                int notedId = ItemDefinitions.getItemDefinitions(id[i]).getCertId();
                notedIds[i] = notedId == -1 ? id[i] : notedId;
            }
            return notedIds;
        }

        public int[] getId() {
            return id;
        }

    }


    public enum Effects {
        HUNTER_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 0, 10);
            }
        },
        SCENTLESS_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 0, 6);
            }
        },
        FARMING_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 0, 6);
            }
        },
        COOKING_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 0, 10);
            }
        },
        FISHING_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 0, 6);
            }
        },
        WOODCUTTING_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 0, 6);
            }
        },
        MINING_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 0, 6);
            }
        },
        SARADOMINS_BLESSING_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 0, 6);
            }
        },
        GUTHIXS_GIFT_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 0, 6);
            }
        },
        ZAMORAKS_FAVOUR_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 0, 6);
            }
        },
        PERFECT_WOODCUTTING_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 1, 0);
            }
        },
        PERFECT_FARMING_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 1, 0);
            }
        },
        PERFECT_MINING_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 1, 0);
            }
        },
        PERFECT_SMITHING_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 1, 0);
            }
        },
        PERFECT_AGILITY_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 1, 0);
            }
        },
        PERFECT_PRAYER_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 1, 0);
            }
        },
        PERFECT_HERBLORE_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 1, 0);
            }
        },
        PERFECT_DUNGEONEERING_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 4, 0);
            }
        },
        PERFECT_FISHING_JUJU() {
            @Override
            public void extra(Player player) {
                player.jujuPotions.addActive(this, 1, 0);
            }
        },
        NATURES_ESSENCE() {
            @Override
            public boolean canDrink(Player player) {
                if (Settings.TEST_SERVER_MODE) {
                    player.sendMessage(Colors.PINK + "Developer: ET potion stacking restriction bypassed (beta server).");
                    return true;
                }
                if (player.bloodSerumDelay > 0) {
                    player.sendMessage(Colors.RED + "The power of Evil blood serum is too overwhelming...");
                    return false;
                }
                if (player.naturesEssenceDelay > 0) {
                    long secondsLeft = TimeUnit.SECONDS.convert(player.naturesEssenceDelay * 600, TimeUnit.MILLISECONDS);
                    player.sendMessage(Colors.DARK_GREEN + "You are already being affected by this potion (" + secondsLeft + "s left).");
                    return false;
                }
                return true;
            }

            @Override
            public void extra(Player player) {
                player.lock();
                player.setNextGraphics(new Graphics(3871));
                WorldTasksManager.schedule(new WorldTask() {
                    @Override
                    public void run() {
                        for (FarmingManager.FarmingSpot spot : player.getFarmingManager().getSpots()) {
                            if (spot == null || spot.productInfo == null || spot.isDead() || spot.isCleared())
                                continue;
                            if (!spot.reachedMaxStage()) {
                                spot.increaseStage();
                            }
                            if (spot.isDiseased()) {
                                spot.setDiseased(false);
                            }
                        }
                        player.unlock();
                        player.sendMessage(Colors.DARK_GREEN + "You feel a sense of tranquility and oneness with nature.");
                        player.naturesEssenceDelay = 200;
                        if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE)) {
                            player.sendFilteredMessage("Nature's essence extended from 2 to 4 minutes, thanks to the " + Colors.RED + "Herbivore</col> perk.");
                            player.naturesEssenceDelay *= 2;
                        }
                    }
                }, 3);
            }
        },
        BLOOD_SERUM() {
            @Override
            public boolean canDrink(Player player) {
                if (Settings.TEST_SERVER_MODE) {
                    player.sendFilteredMessage(Colors.PINK + "Developer: ET potion stacking restriction bypassed (beta server).");
                } else {
                    if (player.naturesEssenceDelay > 0) {
                        player.sendMessage(Colors.DARK_GREEN + "The power of the Nature's essence is too overwhelming...");
                        return false;
                    }
                }
                if (player.bloodSerumDelay > 0) {
                    long secondsLeft = TimeUnit.SECONDS.convert(player.bloodSerumDelay * 600, TimeUnit.MILLISECONDS);
                    player.sendMessage(Colors.RED + "You are already being affected by this potion (" + secondsLeft + "s left).");
                    return false;
                }
                if (!EvilTree.isNearInstanceOrGlobal(player)) {
                    player.sendMessage(Colors.RED + "You can only drink this potion while fighting Evil Trees.");
                    return false;
                }
                return true;
            }

            @Override
            public void extra(Player player) {
                player.sendMessage(Colors.RED + "You start to feel very strange.");
                player.bloodSerumDelay = 200;
                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE)) {
                    player.sendFilteredMessage("Evil blood serum extended from 2 to 4 minutes, thanks to the " + Colors.RED + "Herbivore</col> perk.");
                    player.bloodSerumDelay *= 2;
                }
            }
        },
        ATTACK_POTION(Skills.ATTACK) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 3 + (realLevel * 0.1));
            }
        },
        FISHING_POTION(Skills.FISHING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (level + 3);
            }
        },
        ZAMORAK_BREW(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                if (skillId == Skills.ATTACK || skillId == Skills.STRENGTH) {
                    int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                    return (int) (level + 1 + (realLevel * 0.08));
                } else {
                    int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                    return (int) (level - 1 - (realLevel * 0.08));
                }
            }

            @Override
            public void extra(Player player) {
                int hitpointsModification = (int) (player.getMaxHitpoints() * 0.12);
                if (player.getHitpoints() <= hitpointsModification) {
                    player.heal(-player.getHitpoints());
                    player.sendDeath(null);
                } else
                    player.heal(-hitpointsModification);
            }

        },

        SUPER_ZAMORAK_BREW(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.RANGE, Skills.MAGIC) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                if (skillId == Skills.ATTACK || skillId == Skills.STRENGTH || skillId == Skills.RANGE || skillId == Skills.MAGIC) {
                    int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                    return (int) (level + 2 + (realLevel * 0.12));
                } else {
                    int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                    return (int) (level - 2 - (realLevel * 0.12));
                }
            }

            @Override
            public void extra(Player player) {
                int hitpointsModification = (int) (player.getMaxHitpoints() * 0.10);
                if (player.getHitpoints() <= hitpointsModification) {
                    player.heal(-player.getHitpoints());
                    player.sendDeath(null);
                } else {
                    player.removeHitpoints(new Hit(hitpointsModification, HitLook.UNBLOCKABLE_REGULAR_DAMAGE));
                    // target.applyHit(new Hit(target, player.getHitpoints(),
                    // HitLook.REGULAR_DAMAGE));
                }
            }

        },

        SANFEW_SERUM(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MAGIC, Skills.RANGE, Skills.AGILITY, Skills.COOKING, Skills.CRAFTING, Skills.FARMING, Skills.FIREMAKING, Skills.FISHING, Skills.FLETCHING, Skills.HERBLORE, Skills.MINING, Skills.RUNECRAFTING, Skills.SLAYER, Skills.SMITHING, Skills.THIEVING, Skills.WOODCUTTING, Skills.SUMMONING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int boost = (int) (realLevel * 0.33);
                if (virtualLevel > realLevel)
                    return virtualLevel;
                if (virtualLevel + boost > realLevel)
                    return realLevel;
                return virtualLevel + boost;
            }

            @Override
            public void extra(Player player) {
                player.getPrayer().restorePrayer((int) ((int) (player.getSkills().getLevelForXp(Skills.PRAYER) * 0.33 * 10) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
                player.addPoisonImmune(180000);
                // TODO DISEASE HEALING
            }

        },
        SUMMONING_POT(Skills.SUMMONING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int restore = (int) (Math.floor(player.getSkills().getLevelForXp(Skills.SUMMONING) * 0.25) + 7);
                if (virtualLevel + restore > realLevel)
                    return realLevel;
                return virtualLevel + restore;
            }

            @Override
            public void extra(Player player) {
                Familiar familiar = player.getFamiliar();
                if (familiar != null)
                    familiar.restoreSpecialAttack(15);
            }
        },
        ANTIPOISON() {
            @Override
            public void extra(Player player) {
                player.addPoisonImmune((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 86_000);
                player.sendMessage("You are now immune to all kinds of poison.");
                player.getBuffDebuffTimersManager().addTimer(Timer.ANTIPOISON_ACTIVE, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 86_000);
                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Antipoison extended from 1 to 3 minutes, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);
            }
        },
        SUPER_ANTIPOISON() {
            @Override
            public void extra(Player player) {
                player.addPoisonImmune((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 346_000);
                player.sendMessage("You are now immune to all kinds of poison.");
                player.getBuffDebuffTimersManager().addTimer(Timer.ANTIPOISON_ACTIVE, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 346_000);
                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Super anti-poison extended from 6 to 12 minutes, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);
            }
        },

        ANTIPOISON_PLUS() {
            @Override
            public void extra(Player player) {
                player.addPoisonImmune((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 518_000);
                player.sendMessage("You are now immune to all kinds of poison.");
                player.getBuffDebuffTimersManager().addTimer(Timer.ANTIPOISON_ACTIVE, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 518_000);
                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Antipoison+ extended from 8 to 16 minutes, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);
            }
        },

        ANTIPOISON_PLUS_PLUS() {
            @Override
            public void extra(Player player) {
                player.addPoisonImmune((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 720_000);
                player.sendMessage("You are now immune to all kinds of poison.");
                player.getBuffDebuffTimersManager().addTimer(Timer.ANTIPOISON_ACTIVE, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 720_000);
                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Antipoison++ extended from 12 to 24 minutes, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);
            }
        },
        ENERGY_POTION() {
            @Override
            public void extra(Player player) {
                int restoredEnergy = player.getRunEnergy() + 20;
                player.setRunEnergy(restoredEnergy > 100 ? 100 : restoredEnergy);
            }
        },
        SUPER_ENERGY() {
            @Override
            public void extra(Player player) {
                int restoredEnergy = player.getRunEnergy() + 40;
                player.setRunEnergy(restoredEnergy > 100 ? 100 : restoredEnergy);
            }
        },

        AGILITY_POTION(Skills.AGILITY) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return level + 3;
            }
        },

        ANTI_FIRE() {
            @Override
            public void extra(final Player player) {
                player.addFireImmune((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 360000);
                final long current = player.getFireImmune();
                player.sendMessage("You are now immune to all kinds of dragonfire.");
                player.getBuffDebuffTimersManager().addTimer(Timer.ANTIFIRE_ACTIVE, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 360000);
                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Anti-fire extended from 5 mins. to 10 mins. thanks to Herbivore perk.", true);

                WorldTasksManager.schedule(new WorldTask() {
                    boolean stop = false;

                    @Override
                    public void run() {
                        if (current != player.getFireImmune()) {
                            stop();
                            return;
                        }
                        if (!stop) {
                            player.sendMessage(Colors.RED + "Your antifire potion is about to run out...");
                            stop = true;
                        } else {
                            stop();
                            player.sendMessage(Colors.RED + "Your antifire potion has ran out.</col>");
                        }
                    }
                }, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 500, 100);
            }
        },
        SUPER_ANTI_FIRE() {
            @Override
            public void extra(final Player player) {
                player.addSuperAntiFire((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 360000);
                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Super Anti-fire extended from 5 mins. to 10 mins. thanks to Herbivore perk.", true);
                player.getBuffDebuffTimersManager().addTimer(Timer.SUPER_ANTIFIRE_ACTIVE, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 360000);
                final long current = player.getSuperAntiFire();
                WorldTasksManager.schedule(new WorldTask() {
                    boolean stop = false;

                    @Override
                    public void run() {
                        if (current != player.getSuperAntiFire()) {
                            stop();
                            return;
                        }
                        if (!stop) {
                            player.sendMessage(Colors.RED + "Your super antifire potion is about to run out...");
                            stop = true;
                        } else {
                            stop();
                            player.sendMessage(Colors.RED + "Your super antifire potion has run out.");
                        }
                    }
                }, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 500, 100);
            }
        },
        STRENGTH_POTION(Skills.STRENGTH) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 3 + (realLevel * 0.1));
            }
        },
        DEFENCE_POTION(Skills.DEFENCE) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 3 + (realLevel * 0.1));
            }
        },
        RANGE_POTION(Skills.RANGE) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 3 + (realLevel * 0.1));
            }
        },

        COMBAT_POTION(Skills.ATTACK, Skills.STRENGTH) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 4 + (realLevel * 0.1));
            }
        },

        SUPER_RANGE_POTION(Skills.RANGE) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 4 + (realLevel * 0.1));
            }
        },
        MAGIC_POTION(Skills.MAGIC) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return level + 8;
            }
        },
        SUPER_MAGIC_POTION(Skills.MAGIC) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return level + 12;
            }
        },
        PRAYER_POTION() {
            @Override
            public void extra(Player player) {
                player.getPrayer().restorePrayer((int) ((int) (Math.floor(player.getSkills().getLevelForXp(Skills.PRAYER) * 2.5) + 70) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
            }

            @Override
            public boolean canDrink(Player player) {
                Perk profane = player.getInventionManager().hasPerk(Perks.PROFANE);
                if (profane != null) {
                    player.getPackets().sendGameMessage("Your Profane Perk preventes you from drinking prayer potions.");
                    return false;
                }
                return true;
            }

        },
        SUPER_STR_POTION(Skills.STRENGTH) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 5 + (realLevel * 0.15));
            }
        },
        SUPER_DEF_POTION(Skills.DEFENCE) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 5 + (realLevel * 0.15));
            }
        },
        SUPER_ATT_POTION(Skills.ATTACK) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 5 + (realLevel * 0.15));
            }
        },
        EXTREME_STR_POTION(Skills.STRENGTH) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 5 + (realLevel * 0.22));
            }
        },
        EXTREME_DEF_POTION(Skills.DEFENCE) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 5 + (realLevel * 0.22));
            }
        },
        EXTREME_ATT_POTION(Skills.ATTACK) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 5 + (realLevel * 0.22));
            }
        },
        EXTREME_RAN_POTION(Skills.RANGE) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 4 + (Math.floor(realLevel / 5.2)));
            }
        },
        EXTREME_MAG_POTION(Skills.MAGIC) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return level + 19;
            }
        },
        RECOVER_SPECIAL() {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                Long time = (Long) player.getTemporaryAttributtes().get("Recover_Special_Pot");
                if (time != null) {
                    long difference = Utils.currentTimeMillis() - time;
                    long remaining = 60_000 - difference;
                    if (remaining > 0) {
                        long seconds = TimeUnit.SECONDS.convert(remaining, TimeUnit.MILLISECONDS);
                        player.sendMessage("The effects of this potion still linger (" + seconds + "s left).");
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void extra(Player player) {
                player.getTemporaryAttributtes().put("Recover_Special_Pot", Utils.currentTimeMillis());
                player.getCombatDefinitions().restoreSpecialAttack(25);
            }
        },
        SARADOMIN_BREW("You drink some of the foul liquid.", Skills.ATTACK, Skills.DEFENCE, Skills.STRENGTH, Skills.MAGIC, Skills.RANGE) {
            @Override
            public boolean canDrink(Player player) {
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                if (skillId == Skills.DEFENCE) {
                    int boost = (int) (realLevel * 0.25);
                    int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                    return level + boost;
                } else {
                    int decreaseValue = (int) (virtualLevel * 0.90);
                    if (player.getCurrentPet() != null) {
                        if (player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED)) {
                            int tier = PetPerkUtils.getPerkTier(PetPerk.OVERLOADED, player.getCurrentPet());
                            switch (tier) {
                                case 1:
                                    if (decreaseValue < realLevel - (realLevel / 4))
                                        decreaseValue = realLevel - (realLevel / 4);
                                    break;
                                case 2:
                                    if (decreaseValue < realLevel - (realLevel / 5))
                                        decreaseValue = realLevel - (realLevel / 5);
                                    break;
                                case 3:
                                    if (decreaseValue < realLevel - (realLevel / 10))
                                        decreaseValue = realLevel - (realLevel / 10);
                                    break;
                            }
                        }
                    }
                    return decreaseValue;
                }
            }

            @Override
            public void extra(Player player) {
                int hitpointsModification = (int) (player.getMaxHitpoints() * 0.15);
                player.heal(hitpointsModification + 20, hitpointsModification);
            }
        },

        SUPER_SARADOMIN_BREW("You begin to feel heavily refreshed.", Skills.ATTACK, Skills.DEFENCE, Skills.STRENGTH, Skills.MAGIC, Skills.RANGE) {
            @Override
            public boolean canDrink(Player player) {
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                if (skillId == Skills.DEFENCE) {
                    int boost = (int) (realLevel * 0.30);
                    int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                    return level + boost;
                } else {
                    return (int) (virtualLevel * 0.90);
                }
            }

            @Override
            public void extra(Player player) {
                int hitpointsModification = (int) (player.getMaxHitpoints() * 0.205);
                player.heal(hitpointsModification + 30, hitpointsModification);
            }
        },

        OVERLOAD() {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                if (player.getSupremeOverloadDelay() > 0 || player.getOverloadDelay() > 0) {
                    player.sendMessage("You may only use this potion every six minutes.");
                    return false;
                }
                if ((player.getHitpoints() <= 500 && (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED))) || player.getOverloadDelay() > 480) {
                    player.getPackets().sendGameMessage("You need more than 500 life points to survive the power of overload.");
                    return false;
                }
                return true;
            }

            @Override
            public void extra(final Player player) {
                if (PetPerkUtils.getPerkTier(PetPerk.OVERLOADED, player.getCurrentPet()) == 3) {
                    player.sm("Thanks to your Tier 3 Overloaded perk your overload converted itself to an Supreme overload salve.");
                    SUPREME_OVERLOAD_SALVE.extra(player);
                    return;
                }
                int multiplier = (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1);
                if (player.getCurrentPet() != null) {
                    if (player.getCurrentPet().getPerks().contains(PetPerk.OVERLOAD_EXTENDER)) {
                        multiplier *= 3;
                        if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                            player.sendMessage("Overload extended from 6 minutes to 30, thanks to the " + Colors.RED + "Herbivore</col> perk and your pet perk.", true);
                        else
                            player.sm("Overload extended from 6 minutes to 15, thanks to the \" + Colors.RED + \"Overloaded</col> petperk.");

                    }

                }
                player.setOverloadDelay(multiplier * 601);
                player.getBuffDebuffTimersManager().addTimer(Timer.OVERLOADED, (multiplier * 601) * 600);
                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Overload extended from 6 minutes to 12, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);
                if (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED)) {
                    WorldTasksManager.schedule(new WorldTask() {
                        int count = 4;

                        @Override
                        public void run() {
                            if (count == 0)
                                stop();
                            player.setNextAnimation(new Animation(3170));
                            player.setNextGraphics(new Graphics(560));
                            player.applyHit(new Hit(player, 100, HitLook.REGULAR_DAMAGE, 0));
                            count--;
                        }
                    }, 0, 2);
                }
            }
        },
        SUPER_PRAYER() {
            @Override
            public void extra(Player player) {
                player.getPrayer().restorePrayer((int) ((int) (70 + (player.getSkills().getLevelForXp(Skills.PRAYER) * 3.43)) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
            }
        },
        PRAYER_RENEWAL() {
            @Override
            public void extra(Player player) {
                player.setPrayerRenewalDelay((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 601);
                player.getBuffDebuffTimersManager().addTimer(Timer.PRAYER_RENEW_ACTIVE, ((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 601) * 600);
                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Prayer Renewal extended from 6 mins. to 12 mins. thanks to Herbivore perk.", true);
            }

            @Override
            public boolean canDrink(Player player) {
                Perk profane = player.getInventionManager().hasPerk(Perks.PROFANE);
                if (profane != null) {
                    player.getPackets().sendGameMessage("Your Profane Perk preventes you from drinking prayer potions.");
                    return false;
                }
                return true;
            }
        },
        SUPER_RESTORE(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MAGIC, Skills.RANGE, Skills.AGILITY, Skills.COOKING, Skills.CRAFTING, Skills.FARMING, Skills.FIREMAKING, Skills.FISHING, Skills.FLETCHING, Skills.HERBLORE, Skills.MINING, Skills.RUNECRAFTING, Skills.SLAYER, Skills.SMITHING, Skills.THIEVING, Skills.WOODCUTTING, Skills.SUMMONING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int boost = (int) (realLevel * 0.33);
                if (virtualLevel > realLevel)
                    return virtualLevel;
                if (virtualLevel + boost > realLevel)
                    return realLevel;
                return virtualLevel + boost;
            }

            @Override
            public void extra(Player player) {
                player.getPrayer().restorePrayer((int) ((int) (player.getSkills().getLevelForXp(Skills.PRAYER) * 0.33 * 10) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
                player.removeHPReduction(7);
            }

        },
        RESTORE_POTION(Skills.ATTACK, Skills.STRENGTH, Skills.MAGIC, Skills.RANGE, Skills.PRAYER) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int boost = (int) (realLevel * 0.33);
                if (virtualLevel > realLevel)
                    return virtualLevel;
                if (virtualLevel + boost > realLevel)
                    return realLevel;
                return virtualLevel + boost;
            }
        },
        BEER(Skills.ATTACK, Skills.STRENGTH) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                if (skillId == Skills.STRENGTH) {
                    int boost = (int) (realLevel * 0.07);
                    if (virtualLevel > realLevel)
                        return virtualLevel;
                    if (virtualLevel + boost > realLevel)
                        return realLevel;
                    return virtualLevel + boost;
                } else {
                    return (int) (virtualLevel * 0.90);
                }
            }

            @Override
            public void extra(Player player) {
                player.sendMessage("You drink the beer. You feel slightly reinvigorated...", true);
                player.sendMessage("...and slightly dizzy too.", true);
            }
        },

        STRENGTH_BOOST(Skills.STRENGTH) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                return (int) (virtualLevel * 1.1);
            }

            @Override
            public void extra(Player player) {
                player.sendMessage("You drink the chocolatey milk..", true);
                player.sendMessage(".. and feel a lot more positive and stronger.", true);
            }
        },

        WINE(Skills.ATTACK) {
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                return (int) (virtualLevel * 0.90);
            }

            @Override
            public void extra(Player player) {
                player.sendMessage("You drink the wine. You feel slightly reinvigorated...", true);
                player.sendMessage("...and slightly dizzy too.", true);
                player.heal(70);
            }
        },

        GRAND_STRENGTH(Skills.STRENGTH) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 5 + (realLevel * 0.20));
            }

        },

        GRAND_RANGING(Skills.RANGE) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 5 + (realLevel * 0.20));
            }

        },

        GRAND_MAGE(Skills.MAGIC) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 5 + (realLevel * 0.20));
            }

        },

        GRAND_ATTACK(Skills.ATTACK) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 5 + (realLevel * 0.20));
            }

        },

        GRAND_DEFENCE(Skills.DEFENCE) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 5 + (realLevel * 0.20));
            }

        },

        SUPER_MELEE(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 5 + (realLevel * 0.18));
            }

        },

        SUPER_WARMASTER(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MAGIC, Skills.DEFENCE) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level * 1.12) + 2;
            }

        },

        SUPER_PRAYER_RENEWAL() {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                Perk profane = player.getInventionManager().hasPerk(Perks.PROFANE);
                if (profane != null) {
                    player.getPackets().sendGameMessage("Your Profane Perk preventes you from drinking prayer potions.");
                    return false;
                }
                return true;
            }

            @Override
            public void extra(Player player) {
                player.getPrayer().restorePrayer((int) ((int) (player.getSkills().getLevelForXp(Skills.PRAYER) * 0.30 * 10) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));

                player.setPrayerRenewalDelay((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 601);

                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Prayer Renewal extended from 6 minutes to 12, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);
            }

        },

        WYRMFIRE() {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public void extra(final Player player) {
                player.addSuperAntiFire((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 360000);

                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Wyrmfire potion extended from 6 minutes to 12, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);

                final long current = player.getSuperAntiFire();
                WorldTasksManager.schedule(new WorldTask() {
                    boolean stop = false;

                    @Override
                    public void run() {
                        if (current != player.getSuperAntiFire()) {
                            stop();
                            return;
                        }
                        if (!stop) {
                            player.sendMessage(Colors.RED + "Your wyrmfire potion is about to run out...");
                            stop = true;
                        } else {
                            stop();
                            player.sendMessage(Colors.RED + "Your wyrmfire potion has run out.");
                        }
                    }
                }, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 600, 100);
            }

        },

        EXTREME_BRAWLER(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 5 + (realLevel * 0.22));
            }
        },

        EXTREME_BATTLEMAGE(Skills.MAGIC, Skills.DEFENCE) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                if (skillId == Skills.MAGIC)
                    return level + 9;
                return (int) (level + 5 + (realLevel * 0.22));
            }
        },

        EXTREME_SHARPSHOOTER(Skills.RANGE, Skills.DEFENCE) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 5 + (realLevel * 0.22));
            }
        },

        EXTREME_WARMASTER(Skills.ATTACK, Skills.STRENGTH, Skills.RANGE, Skills.MAGIC, Skills.DEFENCE) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                if (skillId == Skills.MAGIC)
                    return level + 9;
                return (int) (level + 5 + (realLevel * 0.22));
            }
        },

        SUPREME_STRENGTH(Skills.STRENGTH) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 7 + (realLevel * 0.22));
            }
        },

        SUPREME_ATTACK(Skills.ATTACK) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 7 + (realLevel * 0.22));
            }
        },

        SUPREME_DEFENCE(Skills.DEFENCE) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 7 + (realLevel * 0.22));
            }
        },

        SUPREME_RANGING(Skills.RANGE) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 7 + (realLevel * 0.22));
            }
        },

        SUPREME_MAGIC(Skills.MAGIC) {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return level + 23;
            }
        },

        BRIGHTFIRE() {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                return true;
            }

            @Override
            public void extra(final Player player) {
                player.getPrayer().restorePrayer((int) ((int) (player.getSkills().getLevelForXp(Skills.PRAYER) * 0.30 * 10) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));

                player.setPrayerRenewalDelay((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 601);

                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Brightfire Prayer Renewal extended from 6 minutes to 12, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);

                player.addSuperAntiFire((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 360000);

                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Brightfire potion extended from 6 minutes to 12, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);

                final long current = player.getSuperAntiFire();
                WorldTasksManager.schedule(new WorldTask() {
                    boolean stop = false;

                    @Override
                    public void run() {
                        if (current != player.getSuperAntiFire()) {
                            stop();
                            return;
                        }
                        if (!stop) {
                            player.sendMessage(Colors.RED + "Your brightfire potion is about to run out...");
                            stop = true;
                        } else {
                            stop();
                            player.sendMessage(Colors.RED + "Your brightfire potion has run out.");
                        }
                    }
                }, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 600, 100);
            }
        },

        REPLENISHMENT(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MAGIC, Skills.RANGE, Skills.AGILITY, Skills.COOKING, Skills.CRAFTING, Skills.FARMING, Skills.FIREMAKING, Skills.FISHING, Skills.FLETCHING, Skills.HERBLORE, Skills.MINING, Skills.RUNECRAFTING, Skills.SLAYER, Skills.SMITHING, Skills.THIEVING, Skills.WOODCUTTING, Skills.SUMMONING) {
            @Override
            public boolean canDrink(Player player) {
                boolean restoreOnly = player.getTemporaryAttributtes().get("replenishment") != null;
                if (restoreOnly)
                    return true;
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                Long time = (Long) player.getTemporaryAttributtes().get("Recover_Special_Pot");
                if (time != null && Utils.currentTimeMillis() - time < 60_000) {
                    player.sendMessage("You may only use this potion every 60 seconds.");
                    return false;
                }
                return true;
            }

            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int boost = (int) (realLevel * 0.25) + 8;
                if (virtualLevel > realLevel)
                    return virtualLevel;
                if (virtualLevel + boost > realLevel)
                    return realLevel;
                return virtualLevel + boost;
            }

            @Override
            public void extra(Player player) {
                boolean restoreOnly = player.getTemporaryAttributtes().remove("replenishment") != null;
                if (!restoreOnly) {
                    player.getTemporaryAttributtes().put("Recover_Special_Pot", Utils.currentTimeMillis());
                    player.getCombatDefinitions().restoreSpecialAttack(25);
                }
                player.getPrayer().restorePrayer((int) (((int) ((player.getSkills().getLevelForXp(Skills.PRAYER) * 0.25) + 8) * 10) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
            }
        },

        HOLY_OVERLOAD() {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                if (player.getSupremeOverloadDelay() > 0 || player.getOverloadDelay() > 0) {
                    player.sendMessage("You may only use this potion every six minutes.");
                    return false;
                }
                if ((player.getHitpoints() <= 500 && (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED))) || player.getOverloadDelay() > 480) {
                    player.getPackets().sendGameMessage("You need more than 500 life points to survive the power of overload.");
                    return false;
                }
                return true;
            }

            @Override
            public void extra(final Player player) {
                if (PetPerkUtils.getPerkTier(PetPerk.OVERLOADED, player.getCurrentPet()) == 3) {
                    player.sm("Thanks to your Tier 3 Overloaded perk your overload converted itself to an Supreme overload salve.");
                    SUPREME_OVERLOAD_SALVE.extra(player);
                    return;
                }
                int multiplier = (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1);
                if (player.getCurrentPet() == null) {
                    if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                        player.sendMessage("Holy overload prayer renewal extended from 6 minutes to 12, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);
                    if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                        player.sendMessage("Holy overload extended from 6 minutes to 12, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);
                } else {
                    if (player.getCurrentPet().getPerks().contains(PetPerk.OVERLOAD_EXTENDER) && PetPerkUtils.getPerkTier(PetPerk.OVERLOAD_EXTENDER, player.getCurrentPet()) > 2) {
                        multiplier *= 3;
                        if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                            player.sendMessage("Overload extended from 6 minutes to 30, thanks to the " + Colors.RED + "Herbivore</col> perk and your pet perk.", true);
                        else
                            player.sm("Overload extended from 6 minutes to 15, thanks to the \" + Colors.RED + \"Overloaded</col> petperk.");
                    }
                }
                player.getPrayer().restorePrayer((int) ((int) (player.getSkills().getLevelForXp(Skills.PRAYER) * 0.30 * 10) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
                player.setPrayerRenewalDelay(multiplier * 601);
                player.setOverloadDelay(multiplier * 601);
                player.getBuffDebuffTimersManager().addTimer(Timer.PRAYER_RENEW_ACTIVE, (multiplier * 601) * 600);
                player.getBuffDebuffTimersManager().addTimer(Timer.OVERLOADED, (multiplier * 601) * 600);
                if (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED)) {
                    WorldTasksManager.schedule(new WorldTask() {
                        int count = 4;

                        @Override
                        public void run() {
                            if (count == 0)
                                stop();
                            player.setNextAnimation(new Animation(3170));
                            player.setNextGraphics(new Graphics(560));
                            player.applyHit(new Hit(player, 100, HitLook.REGULAR_DAMAGE, 0));
                            count--;
                        }
                    }, 0, 2);
                }
            }
        },

        SEARING_OVERLOAD() {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                if (player.getSupremeOverloadDelay() > 0 || player.getOverloadDelay() > 0) {
                    player.sendMessage("You may only use this potion every six minutes.");
                    return false;
                }
                if ((player.getHitpoints() <= 500 && (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED))) || player.getOverloadDelay() > 480) {
                    player.getPackets().sendGameMessage("You need more than 500 life points to survive the power of overload.");
                    return false;
                }
                return true;
            }

            @Override
            public void extra(final Player player) {
                if (PetPerkUtils.getPerkTier(PetPerk.OVERLOADED, player.getCurrentPet()) == 3) {
                    player.sm("Thanks to your Tier 3 Overloaded perk your overload converted itself to an Supreme overload salve.");
                    SUPREME_OVERLOAD_SALVE.extra(player);
                    return;
                }
                player.setOverloadDelay((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 601);

                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Searing overload extended from 6 mins. to 10 mins. thanks to Herbivore perk.", true);

                if (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED)) {
                    WorldTasksManager.schedule(new WorldTask() {
                        int count = 4;

                        @Override
                        public void run() {
                            if (count == 0)
                                stop();
                            player.setNextAnimation(new Animation(3170));
                            player.setNextGraphics(new Graphics(560));
                            player.applyHit(new Hit(player, 100, HitLook.REGULAR_DAMAGE, 0));
                            count--;
                        }
                    }, 0, 2);
                }
                player.addSuperAntiFire((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 360000);
                player.getBuffDebuffTimersManager().addTimer(Timer.SUPER_ANTIFIRE_ACTIVE, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 360000);
                player.getBuffDebuffTimersManager().addTimer(Timer.OVERLOADED, ((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 601) * 600);
                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Searing overload potion extended from 6 minutes to 12, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);

                final long current = player.getSuperAntiFire();
                WorldTasksManager.schedule(new WorldTask() {
                    boolean stop = false;

                    @Override
                    public void run() {
                        if (current != player.getSuperAntiFire()) {
                            stop();
                            return;
                        }
                        if (!stop) {
                            player.sendMessage(Colors.RED + "Your searing overload antifire protection is about to run out...");
                            stop = true;
                        } else {
                            stop();
                            player.sendMessage(Colors.RED + "Your searing overload antifire protection has run out.");
                        }
                    }
                }, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 600, 100);
            }

        },

        OVERLOAD_SALVE() {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                if (player.getSupremeOverloadDelay() > 0 || player.getOverloadDelay() > 0) {
                    player.sendMessage("You may only use this potion every six minutes.");
                    return false;
                }
                if ((player.getHitpoints() <= 500 && (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED))) || player.getOverloadDelay() > 480) {
                    player.getPackets().sendGameMessage("You need more than 500 life points to survive the power of overload.");
                    return false;
                }
                return true;
            }

            @Override
            public void extra(final Player player) {
                if (PetPerkUtils.getPerkTier(PetPerk.OVERLOADED, player.getCurrentPet()) == 3) {
                    player.sm("Thanks to your Tier 3 Overloaded perk your overload converted itself to an Supreme overload salve.");
                    SUPREME_OVERLOAD_SALVE.extra(player);
                    return;
                }
                player.addPoisonImmune((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 360_000);
                player.sendMessage("You are now immune to all kinds of poison.");
                player.getPrayer().restorePrayer((int) ((int) (player.getSkills().getLevelForXp(Skills.PRAYER) * 0.30 * 10) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
                player.setPrayerRenewalDelay((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 601);
                player.setOverloadDelay((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 601);
                player.addSuperAntiFire((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 360_000);
                player.getBuffDebuffTimersManager().addTimer(Timer.PRAYER_RENEW_ACTIVE, ((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 601) * 600);
                player.getBuffDebuffTimersManager().addTimer(Timer.OVERLOADED, ((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 601) * 600);
                player.getBuffDebuffTimersManager().addTimer(Timer.SUPER_ANTIFIRE_ACTIVE, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 360_000);
                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendFilteredMessage("Overload salve extended from 6 minutes to 12, thanks to the " + Colors.RED + "Herbivore</col> perk.");
                if (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED)) {
                    WorldTasksManager.schedule(new WorldTask() {
                        int count = 4;

                        @Override
                        public void run() {
                            if (count == 0)
                                stop();
                            player.setNextAnimation(new Animation(3170));
                            player.setNextGraphics(new Graphics(560));
                            player.applyHit(new Hit(player, 100, HitLook.REGULAR_DAMAGE, 0));
                            count--;
                        }
                    }, 0, 2);
                }

                final long current = player.getSuperAntiFire();
                WorldTasksManager.schedule(new WorldTask() {
                    boolean stop = false;

                    @Override
                    public void run() {
                        if (current != player.getSuperAntiFire()) {
                            stop();
                            return;
                        }
                        if (!stop) {
                            player.sendMessage(Colors.RED + "Your overload salve antifire protection is about to run out...");
                            stop = true;
                        } else {
                            stop();
                            player.sendMessage(Colors.RED + "Your overload salve antifire protection has run out.");
                        }
                    }
                }, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 600, 100);
            }

        },

        SUPREME_OVERLOAD() {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                if (player.getSupremeOverloadDelay() > 0 || player.getSupremeOverloadDelay() > 0 || player.getOverloadDelay() > 0) {
                    player.sendMessage("You may only use this potion every six minutes.");
                    return false;
                }
                if ((player.getHitpoints() <= 500 && (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED))) || player.getOverloadDelay() > 480) {
                    player.getPackets().sendGameMessage("You need more than 500 life points to survive the power of overload.");
                    return false;
                }
                return true;
            }

            @Override
            public void extra(final Player player) {
                if (PetPerkUtils.getPerkTier(PetPerk.OVERLOADED, player.getCurrentPet()) == 3) {
                    player.sm("Thanks to your Tier 3 Overloaded perk your overload converted itself to an Supreme overload salve.");
                    SUPREME_OVERLOAD_SALVE.extra(player);
                    return;
                }
                player.setSupremeOverloadDelay((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 601);

                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Supreme overload extended from 6 minutes to 12, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);
                player.getBuffDebuffTimersManager().addTimer(Timer.SUPREME_OVERLOAD_POTION_ACTIVE, ((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 601) * 600);
                if (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED)) {
                    WorldTasksManager.schedule(new WorldTask() {
                        int count = 4;

                        @Override
                        public void run() {
                            if (count == 0)
                                stop();
                            player.setNextAnimation(new Animation(3170));
                            player.setNextGraphics(new Graphics(560));
                            player.applyHit(new Hit(player, 100, HitLook.REGULAR_DAMAGE, 0));
                            count--;
                        }
                    }, 0, 2);
                }

            }

        },

        SUPREME_OVERLOAD_SALVE() {
            @Override
            public boolean canDrink(Player player) {
                if (player.getControlerManager().getControler() instanceof Wilderness || player.getControlerManager().getControler() instanceof CrucibleController || FfaZone.isOverloadChanged(player)) {
                    player.sendMessage("You cannot drink this potion here.");
                    return false;
                }
                if (player.getSupremeOverloadDelay() > 0 || player.getOverloadDelay() > 0) {
                    player.sendMessage("You may only use this potion every six minutes.");
                    return false;
                }
                if ((player.getHitpoints() <= 500 && (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED))) || player.getOverloadDelay() > 480) {
                    player.getPackets().sendGameMessage("You need more than 500 life points to survive the power of overload.");
                    return false;
                }
                return true;
            }

            @Override
            public void extra(final Player player) {
                int multiplier = (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1);
                if (player.getCurrentPet() == null) {
                    if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                        player.sendMessage("Supreme overload salve prayer renewal extended from 6 minutes to 12, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);
                    if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                        player.sendMessage("Supreme overload salve from 6 minutes to 12, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);
                } else {
                    if (player.getCurrentPet().getPerks().contains(PetPerk.OVERLOAD_EXTENDER) && PetPerkUtils.getPerkTier(PetPerk.OVERLOAD_EXTENDER, player.getCurrentPet()) > 2) {
                        multiplier *= 3;
                        if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                            player.sendMessage("Overload extended from 6 minutes to 30, thanks to the " + Colors.RED + "Herbivore</col> perk and your pet perk.", true);
                        else
                            player.sm("Supreme overload salve extended from 6 minutes to 15, thanks to the \" + Colors.RED + \"Overloaded</col> petperk.");
                    }
                }
                player.addPoisonImmune(multiplier * 360_000);
                player.sendMessage("You are now immune to all kinds of poison.");
                player.getPrayer().restorePrayer((int) ((int) (player.getSkills().getLevelForXp(Skills.PRAYER) * 0.30 * 10) * player.getAuraManager().getPrayerPotsRestoreMultiplier()));
                player.setSupremeOverloadDelay(multiplier * 601);
                player.getBuffDebuffTimersManager().addTimer(Timer.SUPREME_OVERLOAD_POTION_ACTIVE, (multiplier * 601) * 600);
                player.getBuffDebuffTimersManager().addTimer(Timer.ANTIPOISON_ACTIVE, multiplier * 360_000);
                if (player.getCurrentPet() == null || !player.getCurrentPet().getPerks().contains(PetPerk.OVERLOADED)) {
                    WorldTasksManager.schedule(new WorldTask() {
                        int count = 4;

                        @Override
                        public void run() {
                            if (count == 0)
                                stop();
                            player.setNextAnimation(new Animation(3170));
                            player.setNextGraphics(new Graphics(560));
                            player.applyHit(new Hit(player, 100, HitLook.REGULAR_DAMAGE, 0));
                            count--;
                        }
                    }, 0, 2);
                }
                player.setPrayerRenewalDelay(multiplier * 600);
                player.addSuperAntiFire(multiplier * 360_000);

                final long current = player.getSuperAntiFire();
                WorldTasksManager.schedule(new WorldTask() {
                    boolean stop = false;

                    @Override
                    public void run() {
                        if (current != player.getSuperAntiFire()) {
                            stop();
                            return;
                        }
                        if (!stop) {
                            player.sendMessage(Colors.RED + "Your supreme overload salve antifire protection is about to run out...");
                            stop = true;
                        } else {
                            stop();
                            player.sendMessage(Colors.RED + "Your supreme overload salve antifire protection has run out.");
                        }
                    }
                }, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 600, 100);
            }

        },
        AGGRESSION_POTION() {
            @Override
            public boolean canDrink(Player player) {
                if (player.inPzInstance) {
                    player.sendMessage(Colors.RED + "Aggression potions are less effective in PZ instances.");
                }
                if (player.getAggressiveDelay() > 0) {
                    player.sendMessage("You may only use this potion every six minutes.");
                    return false;
                }

                return true;
            }

            @Override
            public void extra(Player player) {
                player.setAggressiveDelay((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 601);
                player.getBuffDebuffTimersManager().addTimer(Timer.AGGRESSION_POTION_ACTIVE, ((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 601) * 600);
                if (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE))
                    player.sendMessage("Aggression potion extended from 6 minutes to 12, thanks to the " + Colors.RED + "Herbivore</col> perk.", true);
            }
        },
        DUNGEONEERING_WEAK_MAGIC_POTION(Skills.MAGIC) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 2 + (realLevel * 0.07));
            }
        },
        DUNGEONEERING_WEAK_RANGED_POTION(Skills.RANGE) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 2 + (realLevel * 0.07));
            }
        },
        DUNGEONEERING_WEAK_MELEE_POTION(Skills.ATTACK, Skills.STRENGTH) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 2 + (realLevel * 0.07));
            }
        },
        DUNGEONEERING_WEAK_DEFENCE_POTION(Skills.DEFENCE) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 2 + (realLevel * 0.07));
            }
        },
        DUNGEONEERING_WEAK_STAT_RESTORE_POTION(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MAGIC, Skills.RANGE, Skills.AGILITY, Skills.COOKING, Skills.CRAFTING, Skills.FARMING, Skills.FIREMAKING, Skills.FISHING, Skills.FLETCHING, Skills.HERBLORE, Skills.MINING, Skills.RUNECRAFTING, Skills.SLAYER, Skills.SMITHING, Skills.THIEVING, Skills.WOODCUTTING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int boost = (int) ((realLevel * 0.12) + 5.2);
                if (virtualLevel > realLevel)
                    return virtualLevel;
                if (virtualLevel + boost > realLevel)
                    return realLevel;
                return virtualLevel + boost;
            }
        },

        DUNGEONEERING_WEAK_CURE() {
            @Override
            public void extra(final Player player) {

                player.addFireImmune((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 300000);
                player.addPoisonImmune((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 300000);
                player.sendMessage("You are now immune to all kinds of poison & dragonfire.");

                final long current = player.getFireImmune();
                WorldTasksManager.schedule(new WorldTask() {
                    boolean stop = false;

                    @Override
                    public void run() {
                        if (current != player.getFireImmune()) {
                            stop();
                            return;
                        }
                        if (!stop) {
                            player.sendMessage(Colors.RED + "Your cure potion is about to run out...");
                            stop = true;
                        } else {
                            stop();
                            player.sendMessage(Colors.RED + "Your cure potion has run out.");
                        }
                    }
                }, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 500, 100);
            }
        },
        DUNGEONEERING_WEAK_REJUVENATION_POTION(Skills.SUMMONING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int restore = (int) (Math.floor(player.getSkills().getLevelForXp(Skills.SUMMONING) * 0.08) + 4);
                if (virtualLevel + restore > realLevel)
                    return realLevel;
                return virtualLevel + restore;
            }

            @Override
            public void extra(Player player) {
                player.getPrayer().restorePrayer((int) ((int) (player.getSkills().getLevelForXp(Skills.PRAYER) * 0.08 * 10) * player.getAuraManager().getPrayerPotsRestoreMultiplier()) + 40);
            }
        },

        WEAK_WEAPON_POISON() {
            @Override
            public void extra(final Player player) {

                player.addWeakWeaponPoison(60000);
                player.sendMessage("Your attacks are now poisonous.");

                final long current = player.getWeakWeaponPoison();
                WorldTasksManager.schedule(new WorldTask() {
                    boolean stop = false;

                    @Override
                    public void run() {
                        if (current != player.getWeakWeaponPoison()) {
                            stop();
                            return;
                        }
                        if (!stop) {
                            player.sendMessage(Colors.RED + "Your weapon poison is about to run out...");
                            stop = true;
                        } else {
                            stop();
                            player.sendMessage(Colors.RED + "Your weapon poison has run out.");
                        }
                    }
                }, 500, 100);
            }
        },

        WEAK_GATHERERS_POTION(Skills.WOODCUTTING, Skills.MINING, Skills.FISHING, Skills.DIVINATION) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 3 + (realLevel * 0.02));
            }
        },

        WEAK_ARTISANS_POTION(Skills.CRAFTING, Skills.FLETCHING, Skills.SMITHING, Skills.CONSTRUCTION, Skills.FIREMAKING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 3 + (realLevel * 0.02));
            }
        },

        WEAK_NATURALISTS_POTION(Skills.COOKING, Skills.FARMING, Skills.HERBLORE, Skills.RUNECRAFTING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 3 + (realLevel * 0.02));
            }
        },

        WEAK_SURVIVALISTS_POTION(Skills.AGILITY, Skills.HUNTER, Skills.THIEVING, Skills.SLAYER) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 3 + (realLevel * 0.02));
            }
        },

        DUNGEONEERING_MAGIC_POTION(Skills.MAGIC) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 2.5 + (realLevel * 0.11));
            }
        },
        DUNGEONEERING_RANGED_POTION(Skills.RANGE) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 2.5 + (realLevel * 0.11));
            }
        },
        DUNGEONEERING_MELEE_POTION(Skills.ATTACK, Skills.STRENGTH) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 2.5 + (realLevel * 0.11));
            }
        },
        DUNGEONEERING_DEFENCE_POTION(Skills.DEFENCE) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 2.5 + (realLevel * 0.11));
            }
        },
        DUNGEONEERING_STAT_RESTORE_POTION(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MAGIC, Skills.RANGE, Skills.AGILITY, Skills.COOKING, Skills.CRAFTING, Skills.FARMING, Skills.FIREMAKING, Skills.FISHING, Skills.FLETCHING, Skills.HERBLORE, Skills.MINING, Skills.RUNECRAFTING, Skills.SLAYER, Skills.SMITHING, Skills.THIEVING, Skills.WOODCUTTING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int boost = (int) ((realLevel * 0.17) + 7);
                if (virtualLevel > realLevel)
                    return virtualLevel;
                if (virtualLevel + boost > realLevel)
                    return realLevel;
                return virtualLevel + boost;
            }
        },

        DUNGEONEERING_CURE() {
            @Override
            public void extra(final Player player) {

                player.addFireImmune((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 600000);
                player.addPoisonImmune((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 600000);
                player.sendMessage("You are now immune to all kinds of poison & dragonfire.");

                final long current = player.getFireImmune();
                WorldTasksManager.schedule(new WorldTask() {
                    boolean stop = false;

                    @Override
                    public void run() {
                        if (current != player.getFireImmune()) {
                            stop();
                            return;
                        }
                        if (!stop) {
                            player.sendMessage(Colors.RED + "Your cure potion is about to run out...");
                            stop = true;
                        } else {
                            stop();
                            player.sendMessage(Colors.RED + "Your cure potion has run out.");
                        }
                    }
                }, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 500, 100);
            }
        },
        DUNGEONEERING_REJUVENATION_POTION(Skills.SUMMONING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int restore = (int) (Math.floor(player.getSkills().getLevelForXp(Skills.SUMMONING) * 0.15) + 7);
                if (virtualLevel + restore > realLevel)
                    return realLevel;
                return virtualLevel + restore;
            }

            @Override
            public void extra(Player player) {
                player.getPrayer().restorePrayer((int) ((int) (player.getSkills().getLevelForXp(Skills.PRAYER) * 0.15 * 10) * player.getAuraManager().getPrayerPotsRestoreMultiplier()) + 70);
            }
        },

        WEAPON_POISON() {
            @Override
            public void extra(final Player player) {

                player.addMediumWeaponPoison(240000);
                player.sendMessage("Your attacks are now poisonous.");

                final long current = player.getMediumWeaponPoison();
                WorldTasksManager.schedule(new WorldTask() {
                    boolean stop = false;

                    @Override
                    public void run() {
                        if (current != player.getMediumWeaponPoison()) {
                            stop();
                            return;
                        }
                        if (!stop) {
                            player.sendMessage(Colors.RED + "Your weapon poison is about to run out...");
                            stop = true;
                        } else {
                            stop();
                            player.sendMessage(Colors.RED + "Your weapon poison has run out.");
                        }
                    }
                }, 500, 100);
            }
        },

        GATHERERS_POTION(Skills.WOODCUTTING, Skills.MINING, Skills.FISHING, Skills.DIVINATION) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 4 + (realLevel * 0.04));
            }
        },

        ARTISANS_POTION(Skills.CRAFTING, Skills.FLETCHING, Skills.SMITHING, Skills.CONSTRUCTION, Skills.FIREMAKING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 4 + (realLevel * 0.04));
            }
        },

        NATURALISTS_POTION(Skills.COOKING, Skills.FARMING, Skills.HERBLORE, Skills.RUNECRAFTING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 4 + (realLevel * 0.04));
            }
        },

        SURVIVALISTS_POTION(Skills.AGILITY, Skills.HUNTER, Skills.THIEVING, Skills.SLAYER) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 4 + (realLevel * 0.04));
            }
        },

        STRONG_DUNGEONEERING_MAGIC_POTION(Skills.MAGIC) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 3 + (realLevel * 0.15));
            }
        },
        STRONG_DUNGEONEERING_RANGED_POTION(Skills.RANGE) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 3 + (realLevel * 0.15));
            }
        },
        STRONG_DUNGEONEERING_MELEE_POTION(Skills.ATTACK, Skills.STRENGTH) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 3 + (realLevel * 0.15));
            }
        },
        STRONG_DUNGEONEERING_DEFENCE_POTION(Skills.DEFENCE) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 3 + (realLevel * 0.15));
            }
        },
        STRONG_DUNGEONEERING_STAT_RESTORE_POTION(Skills.ATTACK, Skills.STRENGTH, Skills.DEFENCE, Skills.MAGIC, Skills.RANGE, Skills.AGILITY, Skills.COOKING, Skills.CRAFTING, Skills.FARMING, Skills.FIREMAKING, Skills.FISHING, Skills.FLETCHING, Skills.HERBLORE, Skills.MINING, Skills.RUNECRAFTING, Skills.SLAYER, Skills.SMITHING, Skills.THIEVING, Skills.WOODCUTTING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int boost = (int) ((realLevel * 0.24) + 10);
                if (virtualLevel > realLevel)
                    return virtualLevel;
                if (virtualLevel + boost > realLevel)
                    return realLevel;
                return virtualLevel + boost;
            }
        },

        STRONG_DUNGEONEERING_CURE() {
            @Override
            public void extra(final Player player) {

                player.addFireImmune((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 1200000);
                player.addPoisonImmune((player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 1200000);
                player.sendMessage("You are now immune to all kinds of poison & dragonfire.");

                final long current = player.getFireImmune();
                WorldTasksManager.schedule(new WorldTask() {
                    boolean stop = false;

                    @Override
                    public void run() {
                        if (current != player.getFireImmune()) {
                            stop();
                            return;
                        }
                        if (!stop) {
                            player.sendMessage(Colors.RED + "Your cure potion is about to run out...");
                            stop = true;
                        } else {
                            stop();
                            player.sendMessage(Colors.RED + "Your cure potion has run out.");
                        }
                    }
                }, (player.getPerkManager().hasPerkActive(DonationPerk.HERBIVORE) ? 2 : 1) * 500, 100);
            }
        },
        STRONG_DUNGEONEERING_REJUVENATION_POTION(Skills.SUMMONING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int restore = (int) (Math.floor(player.getSkills().getLevelForXp(Skills.SUMMONING) * 0.22) + 10);
                if (virtualLevel + restore > realLevel)
                    return realLevel;
                return virtualLevel + restore;
            }

            @Override
            public void extra(Player player) {
                player.getPrayer().restorePrayer((int) ((int) (player.getSkills().getLevelForXp(Skills.PRAYER) * 0.22 * 10) * player.getAuraManager().getPrayerPotsRestoreMultiplier()) + 100);
            }
        },

        STRONG_WEAPON_POISON() {
            @Override
            public void extra(final Player player) {

                player.addStrongWeaponPoison(600000);
                player.sendMessage("Your attacks are now poisonous.");

                final long current = player.getStrongWeaponPoison();
                WorldTasksManager.schedule(new WorldTask() {
                    boolean stop = false;

                    @Override
                    public void run() {
                        if (current != player.getStrongWeaponPoison()) {
                            stop();
                            return;
                        }
                        if (!stop) {
                            player.sendMessage(Colors.RED + "Your weapon poison is about to run out...");
                            stop = true;
                        } else {
                            stop();
                            player.sendMessage(Colors.RED + "Your weapon poison has run out.");
                        }
                    }
                }, 500, 100);
            }
        },

        STRONG_GATHERERS_POTION(Skills.WOODCUTTING, Skills.MINING, Skills.FISHING, Skills.DIVINATION) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 6 + (realLevel * 0.06));
            }
        },

        STRONG_ARTISANS_POTION(Skills.CRAFTING, Skills.FLETCHING, Skills.SMITHING, Skills.CONSTRUCTION, Skills.FIREMAKING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 6 + (realLevel * 0.06));
            }
        },

        STRONG_NATURALISTS_POTION(Skills.COOKING, Skills.FARMING, Skills.HERBLORE, Skills.RUNECRAFTING) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 6 + (realLevel * 0.06));
            }
        },

        STRONG_SURVIVALISTS_POTION(Skills.AGILITY, Skills.HUNTER, Skills.THIEVING, Skills.SLAYER) {
            @Override
            public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
                int level = virtualLevel > realLevel ? realLevel : virtualLevel;
                return (int) (level + 6 + (realLevel * 0.06));
            }
        };

        private final int[] affectedSkills;
        private final String drinkMessage;

        Effects(int... affectedSkills) {
            this(null, affectedSkills);
        }

        Effects(String drinkMessage, int... affectedSkills) {
            this.drinkMessage = drinkMessage;
            this.affectedSkills = affectedSkills;
        }

        public int getAffectedSkill(Player player, int skillId, int virtualLevel, int realLevel) {
            return virtualLevel;
        }

        public boolean canDrink(Player player) {
            // usualy unused
            return true;
        }

        public void extra(Player player) {
            // usualy unused
        }
    }

    public static void sendActivePotionTimers(Player player) {
        Map<String, Long> active = new HashMap<String, Long>();
        if (player.getPoisonImmune() > Utils.currentTimeMillis())
            active.put("poison immunity", player.getPoisonImmune());
        if (player.getFireImmune() > Utils.currentTimeMillis())
            active.put("antifire immunity", player.getFireImmune());
        if (player.getSuperAntiFire() > Utils.currentTimeMillis())
            active.put("antifire immunity", player.getSuperAntiFire());
        if (player.getOverloadDelay() > 0)
            active.put("overload", (player.getOverloadDelay() * 600) + Utils.currentTimeMillis());
        if (player.getPrayerRenewalDelay() > 0)
            active.put("prayer renewal", (player.getPrayerRenewalDelay() * 600) + Utils.currentTimeMillis());
        if (player.getSupremeOverloadDelay() > 0)
            active.put("supreme overload", (player.getSupremeOverloadDelay() * 600) + Utils.currentTimeMillis());
        if (player.getWeakWeaponPoison() > Utils.currentTimeMillis())
            active.put("weak weapon poison", player.getWeakWeaponPoison());
        if (player.getMediumWeaponPoison() > Utils.currentTimeMillis())
            active.put("medium weapon poison", player.getWeakWeaponPoison());
        if (player.getStrongWeaponPoison() > Utils.currentTimeMillis())
            active.put("strong weapon poison", player.getStrongWeaponPoison());
        if (player.getLastBonfire() > 0)
            active.put("bonfire health boost", (player.getLastBonfire() * 600) + Utils.currentTimeMillis());
        if (player.getAggressiveDelay() > 0)
            active.put("aggression", (player.getAggressiveDelay() * 600) + Utils.currentTimeMillis());
        if (player.bloodSerumDelay > 0)
            active.put("evil blood serum", (player.bloodSerumDelay * 600) + Utils.currentTimeMillis());
        if (player.naturesEssenceDelay > 0)
            active.put("nature's essence", (player.naturesEssenceDelay * 600) + Utils.currentTimeMillis());

        if (active.isEmpty()) {
            player.sendMessage("You currently have no active potion effects.");
            return;
        }

        for (Entry<String, Long> effect : active.entrySet()) {
            if (effect == null)
                continue;

            player.sendMessage("Your " + effect.getKey() + (effect.getKey().equals("bonfire health boost") ? "" : " potion effect") + " will last another " + Utils.formatActiveTimer(effect.getValue()) + ".");
        }
    }

    public static boolean shareEffect(final Player player, final Player p2, Item item, boolean boost) {
        final Pot pot = getPot(item.getId());
        if (pot == null)
            return false;
        if (!p2.getControlerManager().canPot(pot))
            return false;
        if (!pot.effect.canDrink(p2))
            return false;
        if (pot.effect.equals(Effects.OVERLOAD) || pot.effect.equals(Effects.PRAYER_RENEWAL))
            return false;
        final String name = item.getDefinitions().getName();
        if (name == null)
            return false;
        if (boost) {// the potion boosts stats
            if (pot.effect.equals(Effects.SANFEW_SERUM) || pot.effect == Effects.ANTIPOISON_PLUS_PLUS || pot.effect.equals(Effects.SUPER_ANTIPOISON) || pot.effect.equals(Effects.ANTIPOISON) || pot.effect.equals(Effects.ANTI_FIRE) || pot.effect.equals(Effects.SUPER_ANTI_FIRE) || pot.effect.equals(Effects.SUPER_ENERGY) || pot.effect.equals(Effects.ENERGY_POTION) || pot.effect.equals(Effects.SUPER_RESTORE) || pot.effect.equals(Effects.RESTORE_POTION) || pot.effect.equals(Effects.RECOVER_SPECIAL) || pot.effect.equals(Effects.PRAYER_POTION) || pot.effect.equals(Effects.SUPER_PRAYER)
                    // boost only pots
                    || pot.effect.equals(Effects.EXTREME_ATT_POTION) || pot.effect.equals(Effects.EXTREME_DEF_POTION) || pot.effect.equals(Effects.EXTREME_MAG_POTION) || pot.effect.equals(Effects.EXTREME_RAN_POTION) || pot.effect.equals(Effects.EXTREME_STR_POTION) || pot.effect.equals(Effects.ZAMORAK_BREW) || pot.effect.equals(Effects.SARADOMIN_BREW) || pot.effect.equals(Effects.SUPER_SARADOMIN_BREW)) {
                return false;
            }
        } else if (!boost) {// the potion restores stats
            if (!(pot.effect.equals(Effects.SANFEW_SERUM) || pot.effect == Effects.ANTIPOISON_PLUS_PLUS || pot.effect.equals(Effects.SUPER_ANTIPOISON) || pot.effect.equals(Effects.ANTIPOISON) || pot.effect.equals(Effects.ANTI_FIRE) || pot.effect.equals(Effects.SUPER_ANTI_FIRE) || pot.effect.equals(Effects.SUPER_ENERGY) || pot.effect.equals(Effects.ENERGY_POTION) || pot.effect.equals(Effects.SUPER_RESTORE) || pot.effect.equals(Effects.RESTORE_POTION) || pot.effect.equals(Effects.RECOVER_SPECIAL) || pot.effect.equals(Effects.PRAYER_POTION) || pot.effect.equals(Effects.SUPER_PRAYER))) {
                return false;
            }
        }
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    p2.setNextGraphics(new Graphics(734, 0, 80));
                } else if (loop == 1) {
                    for (int skillId : pot.effect.affectedSkills)
                        p2.getSkills().set(skillId, pot.effect.getAffectedSkill(p2, skillId, p2.getSkills().getLevel(skillId), p2.getSkills().getLevelForXp(skillId)));
                    pot.effect.extra(p2);// uses the effect on the other players
                    p2.getPackets().sendGameMessage(player.getDisplayName() + " has just shared the effects of their " + name.toLowerCase().replace(" (1)", "").replace(" (2)", "").replace(" (3)", "").replace(" (4)", "").replace(" (5)", "").replace(" (6)", "") + " with you.");
                    this.stop();// stops the loop
                }
                loop++;

            }
        }, 0, 1);
        return true;
    }

    public static boolean shareEffect(final Player player, Item item, int slot, final int others, final boolean boost) {
        final Pot pot = getPot(item.getId());
        if (pot == null) {
            player.getPackets().sendGameMessage("This item has no effect to share.");
            return false;
        }
        if (player.getPotDelay() > Utils.currentTimeMillis())
            return false;
        if (!player.getControlerManager().canPot(pot))
            return false;
        if (!pot.effect.canDrink(player))
            return false;
        if (pot.effect.equals(Effects.OVERLOAD) || pot.effect.equals(Effects.PRAYER_RENEWAL)) {
            player.getPackets().sendGameMessage("You cannot share the effects of this potion.");
            return false;
        }
        final String name = item.getDefinitions().getName();
        if (name == null)
            return false;
        if (boost) {// the potion boosts stats
            if (pot.effect.equals(Effects.SANFEW_SERUM) || pot.effect == Effects.ANTIPOISON_PLUS_PLUS || pot.effect == Effects.ANTIPOISON_PLUS || pot.effect.equals(Effects.SUPER_ANTIPOISON) || pot.effect.equals(Effects.ANTIPOISON) || pot.effect.equals(Effects.ANTI_FIRE) || pot.effect.equals(Effects.SUPER_ANTI_FIRE) || pot.effect.equals(Effects.SUPER_ENERGY) || pot.effect.equals(Effects.ENERGY_POTION) || pot.effect.equals(Effects.SUPER_RESTORE) || pot.effect.equals(Effects.RESTORE_POTION) || pot.effect.equals(Effects.RECOVER_SPECIAL) || pot.effect.equals(Effects.PRAYER_POTION) || pot.effect.equals(Effects.SUPER_PRAYER)
                    // boost only pots
                    || pot.effect.equals(Effects.EXTREME_ATT_POTION) || pot.effect.equals(Effects.EXTREME_DEF_POTION) || pot.effect.equals(Effects.EXTREME_MAG_POTION) || pot.effect.equals(Effects.EXTREME_RAN_POTION) || pot.effect.equals(Effects.EXTREME_STR_POTION) || pot.effect.equals(Effects.ZAMORAK_BREW) || pot.effect.equals(Effects.SARADOMIN_BREW)) {
                player.getPackets().sendGameMessage("You cannot share this potion's effects with others using this spell.");
                return true;
            }
        } else if (!boost) {// the potion restores stats
            if (!(pot.effect.equals(Effects.SANFEW_SERUM) || pot.effect == Effects.ANTIPOISON_PLUS_PLUS || pot.effect.equals(Effects.SUPER_ANTIPOISON) || pot.effect.equals(Effects.ANTIPOISON) || pot.effect.equals(Effects.ANTI_FIRE) || pot.effect.equals(Effects.SUPER_ANTI_FIRE) || pot.effect.equals(Effects.SUPER_ENERGY) || pot.effect.equals(Effects.ENERGY_POTION) || pot.effect.equals(Effects.SUPER_RESTORE) || pot.effect.equals(Effects.RESTORE_POTION) || pot.effect.equals(Effects.RECOVER_SPECIAL) || pot.effect.equals(Effects.PRAYER_POTION) || pot.effect.equals(Effects.SUPER_PRAYER))) {
                player.getPackets().sendGameMessage("You cannot share this potion's effects with others using this spell.");
                return true;
            }
        }
        player.addPotDelay(1075);
        int doses2 = getDoses(pot, item) - 1;
        if (others > 0)
            doses2 -= others;
        int toPot = pot.id.length - doses2;
        if (name.contains("flask") && doses2 <= 0 && toPot >= pot.id.length) {
            player.getInventory().deleteItem(slot, new Item(item.getId(), 1));
        } else
            player.getInventory().getItems().set(slot, new Item(doses2 > 0 && toPot < pot.id.length ? pot.id[toPot] : 229, 1));
        player.getInventory().refresh(slot);
        WorldTasksManager.schedule(new WorldTask() {
            int loop;

            @Override
            public void run() {
                if (loop == 0) {
                    player.lock();
                    player.setLunarDelay(3000);// 3 seconds
                    if (boost)
                        player.getSkills().addXp(Skills.MAGIC, 88);
                    else if (!boost)
                        player.getSkills().addXp(Skills.MAGIC, 84);
                    player.setNextAnimation(new Animation(4413));
                } else if (loop == 1) {
                    for (int skillId : pot.effect.affectedSkills)
                        player.getSkills().set(skillId, pot.effect.getAffectedSkill(player, skillId, player.getSkills().getLevel(skillId), player.getSkills().getLevelForXp(skillId)));
                    player.setNextGraphics(new Graphics(734, 0, 80));
                    pot.effect.extra(player);
                    if (others > 0)
                        player.getPackets().sendGameMessage("You share the effects of your " + name.toLowerCase().replace(" (1)", "").replace(" (2)", "").replace(" (3)", "").replace(" (4)", "").replace(" (5)", "").replace(" (6)", "") + " with " + others + " other" + (others > 1 ? "s" : "") + ".", true);
                    player.unlock();
                    this.stop();
                }
                loop++;

            }
        }, 0, 1);
        return true;
    }

    public static void decantPotsInInv(Player player, int npcId, int toDoses) {
        player.lock();
        CoresManager.getServiceProvider().executeNow(new Runnable() {

            @Override
            public void run() {
                ArrayList<Item> potionsToAdd = new ArrayList<>();
                for (Pot pot : Pot.values()) {
                    if (pot == null || !pot.isPotion())
                        continue;
                    int potDoses = 0;
                    for (int i = pot.getId().length - 1; i >= 0; i--) {
                        int id = pot.getId()[i];
                        if (player.getInventory().containsItem(id, 1)) {
                            potDoses += player.getInventory().getAmountOf(id) * (pot.getId().length - i);
                        }
                        if (player.getInventory().containsItem(ItemDefinitions.getItemDefinitions(id).getCertId(), 1)) {
                            potDoses += player.getInventory().getAmountOf(ItemDefinitions.getItemDefinitions(id).getCertId()) * (pot.getId().length - i);
                        }
                    }
                    boolean hasThisPot = false;
                    for (int potItemId : pot.getId()) {
                        if (player.getInventory().containsItem(potItemId, 1))
                            hasThisPot = true;
                    }
                    for (int potItemId : pot.getNotedId()) {
                        if (player.getInventory().containsItem(potItemId, 1))
                            hasThisPot = true;
                    }
                    if (!hasThisPot)
                        continue;
                    int dosesRemoved = 0;
                    for (int i = 4; i > 0; i--) {
                        while (dosesRemoved <= potDoses && (player.getInventory().containsItem(pot.getIdForDoses(i), 1) || player.getInventory().containsItem(ItemDefinitions.getItemDefinitions(pot.getIdForDoses(i)).getCertId(), 1))) {
                            if (player.getInventory().containsItem(pot.getIdForDoses(i), 1)) {
                                player.getInventory().deleteItem(new Item(pot.getIdForDoses(i), 1));
                                dosesRemoved += i;
                            }
                            if (player.getInventory().containsItem(ItemDefinitions.getItemDefinitions(pot.getIdForDoses(i)).getCertId(), 1)) {
                                player.getInventory().deleteItem(new Item(ItemDefinitions.getItemDefinitions(pot.getIdForDoses(i)).getCertId(), 1));
                                dosesRemoved += i;
                            }
                        }
                    }
                    for (int i = toDoses; i > 0; i--) {
                        int amount = (dosesRemoved / i);
                        if (amount == 0 || dosesRemoved <= 0)
                            continue;
                        int itemId = ItemDefinitions.getItemDefinitions(pot.getIdForDoses(i)).getCertId();
                        if (itemId == -1)
                            itemId = pot.getIdForDoses(i);
                        Item item = new Item(itemId, amount);
                        potionsToAdd.add(item);
                        dosesRemoved = (dosesRemoved % i);
                    }
                }
                for (Item potion : potionsToAdd) {
                    if (potion == null)
                        continue;
                    player.getInventory().addItemDrop(potion.getId(), potion.getAmount());
                }
                Dialogue.closeNoContinueDialogue(player);
                player.getDialogueManager().startDialogue("SimpleNPCMessage", npcId, "..There you go chum!");
                player.unlock();
            }

        });

    }

    public static boolean mixPot(Player player, Item fromItem, Item toItem, int fromSlot, int toSlot) {
        if (fromItem.getId() == VIAL || toItem.getId() == VIAL) {
            Pot pot = getPot(fromItem.getId() == VIAL ? toItem.getId() : fromItem.getId());
            if (pot == null)
                return false;
            int doses = getDoses(pot, fromItem.getId() == VIAL ? toItem : fromItem);
            if (pot.isFlask()) {
                Pot potionPot = pot.toPotion();
                int doses2 = 0;
                doses2 += doses;
                doses = doses2 > potionPot.getMaxDoses() ? doses2 - potionPot.getMaxDoses() : 0;
                doses2 -= doses;
                player.getInventory().getItems().set(fromItem.getId() == VIAL ? toSlot : fromSlot, doses <= 0 ? null : new Item(pot.getIdForDoses(doses), 1));
                player.getInventory().getItems().set(fromItem.getId() == VIAL ? fromSlot : toSlot, new Item(potionPot.getIdForDoses(doses2), 1));
                player.getInventory().refresh(fromSlot);
                player.getInventory().refresh(toSlot);
                player.getPackets().sendGameMessage("You pour from one container into the other" + (doses <= 0 ? " and the glass shatters to pieces." : "."));
                return true;
            }
            if (doses == 1) {
                player.getInventory().switchItem(fromSlot, toSlot);
                player.getPackets().sendGameMessage("You pour from one container into the other.", true);
                return true;
            }
            int vialDoses = doses / 2;// 1
            doses -= vialDoses;// 2
            player.getInventory().getItems().set(fromItem.getId() == VIAL ? toSlot : fromSlot, new Item(pot.getIdForDoses(doses), 1));
            player.getInventory().getItems().set(fromItem.getId() == VIAL ? fromSlot : toSlot, new Item(pot.getIdForDoses(vialDoses), 1));
            player.getInventory().refresh(fromSlot);
            player.getInventory().refresh(toSlot);
            player.getPackets().sendGameMessage("You split the potion between the two vials.", true);
            return true;
        }
        if (fromItem.getId() == POTION_FLASK || toItem.getId() == POTION_FLASK) {
            Pot pot = getPot(fromItem.getId() == POTION_FLASK ? toItem.getId() : fromItem.getId());
            if (pot == null)
                return false;
            if (pot.isFlask()) {
                player.getPackets().sendGameMessage("Nothing interesting happens.");
                return false;
            }
            int dosesCount = 0;
            for (int i = 0; i < player.getInventory().getItems().getSize(); i++) {
                Item item = player.getInventory().getItem(i);
                if (item == null)
                    continue;
                Pot checkPot = getPot(item.getId());
                if (checkPot == null || checkPot.isFlask() || !sameType(pot, checkPot))
                    continue;
                dosesCount += getDoses(checkPot, item);
            }
            if (dosesCount < 6) {
                player.getPackets().sendGameMessage("You don't have enough doses to fill a potion flask.");
                return false;
            }
            while (dosesCount % 6 != 0)
                dosesCount--;
            if (player.getInventory().getAmountOf(POTION_FLASK) < dosesCount / 6)
                dosesCount = player.getInventory().getAmountOf(POTION_FLASK) * 6;
            if (dosesCount >= 12) {
                player.getDialogueManager().startDialogue(new Dialogue() {
                    int dosesCount = 0;

                    @Override
                    public void start() {
                        dosesCount = (int) parameters[0];
                        sendOptionsDialogue(SEND_DEFAULT_OPTIONS_TITLE, "Fill 1", "Fill 5", "Fill X", "Fill All");
                    }

                    @Override
                    public void run(int interfaceId, int componentId) {
                        if (componentId == OPTION_3) {
                            end();
                            player.sendInputInteger("Enter amount:", new InputIntegerEvent() {
                                @Override
                                public void run(Player player) {
                                    int amount = getInteger();
                                    if (amount * 6 > dosesCount)
                                        amount = dosesCount / 6;
                                    if (amount <= 0)
                                        return;
                                    fill(amount);
                                }
                            });
                            return;
                        }
                        end();
                        int amount = componentId == OPTION_1 ? 1 : componentId == OPTION_2 ? 5 : dosesCount / 6;
                        if (amount * 6 > dosesCount)
                            amount = dosesCount / 6;
                        if (amount == 0)
                            return;
                        fill(amount);

                    }

                    public void fill(int amount) {
                        int count = amount;
                        if (count == 1) {
                            fillPotionFlask(player, pot, fromItem.getId() == POTION_FLASK ? fromSlot : toSlot, true);
                            return;
                        }
                        while (count > 0) {
                            forLoop:
                            for (int i = 0; i < player.getInventory().getItems().getSize(); i++) {
                                Item item = player.getInventory().getItem(i);
                                if (item == null || item.getId() != POTION_FLASK)
                                    continue;
                                fillPotionFlask(player, pot, i, false);
                                break forLoop;
                            }
                            count--;
                        }
                        player.getPackets().sendGameMessage("You successfully filled " + amount + " " + ItemDefinitions.getItemDefinitions(pot.toFlask().getIdForDoses(6)).getName() + ".");
                    }

                    @Override
                    public void finish() {
                    }
                }, dosesCount);
                return false;
            }
            fillPotionFlask(player, pot, fromItem.getId() == POTION_FLASK ? fromSlot : toSlot, true);
            return true;
        }
        Pot pot = getPot(fromItem.getId());
        Pot pot2 = getPot(toItem.getId());
        if (pot == null || pot2 == null || !sameType(pot, pot2))
            return false;
        int doses1 = getDoses(pot, fromItem);
        int doses2 = getDoses(pot2, toItem);
        if (doses2 == pot2.getMaxDoses()) {
            player.getPackets().sendGameMessage("Nothing interesting happens.");
            return false;
        }
        doses2 += doses1;
        doses1 = doses2 > pot2.getMaxDoses() ? doses2 - pot2.getMaxDoses() : 0;
        doses2 -= doses1;
        if (doses1 == 0 && pot.isFlask())
            player.getInventory().deleteItem(fromSlot, fromItem);
        else {
            player.getInventory().getItems().set(fromSlot, new Item(doses1 > 0 ? pot.getIdForDoses(doses1) : VIAL, 1));
            player.getInventory().refresh(fromSlot);
        }
        player.getInventory().getItems().set(toSlot, new Item(pot2.getIdForDoses(doses2), 1));
        player.getInventory().refresh(toSlot);
        player.getPackets().sendGameMessage("You pour from one container into the other" + (pot.isFlask() && doses1 == 0 ? " and the glass shatters to pieces." : "."));
        return true;
    }

    public static void fillPotionFlask(Player player, Pot pot, int slotId, boolean sendMessage) {
        int removedDoses = 0;
        for (int i = 0; i < player.getInventory().getItems().getSize(); i++) {
            Item item = player.getInventory().getItem(i);
            if (item == null)
                continue;
            Pot checkPot = getPot(item.getId());
            if (checkPot == null || checkPot.isFlask() || !sameType(pot, checkPot))
                continue;
            int doses = getDoses(checkPot, item);
            int toRemove = removedDoses + doses > 6 ? 6 - removedDoses : doses;
            doses -= toRemove;
            player.getInventory().getItems().set(i, new Item(doses == 0 ? VIAL : checkPot.getIdForDoses(doses), 1));
            player.getInventory().refresh(i);
            removedDoses += toRemove;
        }
        player.getInventory().getItems().set(slotId, new Item(pot.toFlask().getIdForDoses(removedDoses), 1));
        player.getInventory().refresh(slotId);
        if (sendMessage)
            player.getPackets().sendGameMessage("You pour from one container into the other.", true);
    }

}