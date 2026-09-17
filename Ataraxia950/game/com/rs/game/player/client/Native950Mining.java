package com.rs.game.player.client;

import com.rs.cache.Cache;
import com.rs.cache.filestore.store.Store;
import com.rs.cache.loaders.ItemDefinitions;
import com.rs.cache.loaders.AnimationDefinitions;
import com.rs.game.hitbar.impl.MiningHitBar;
import com.rs.game.route.RouteFinder;
import com.rs.game.route.strategy.ObjectStrategy;
import com.rs.cache.loaders.ObjectDefinitions;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.mining.Mining;
import com.rs.game.player.actions.mining.defs.RockDefinitions;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/** Cache admission for the original Mining action on revision 950; no second action scheduler. */
public final class Native950Mining {
    private static final Properties PINS = pins();
    private static Store verifiedStore;
    private static final Map<String, Boolean> verified = new HashMap<>();

    public static final class PickaxeDef {
        public final int itemId;
        public final int level;
        public final int tier;
        public final int animationId;

        public PickaxeDef(int itemId, int level, int tier, int animationId) {
            this.itemId = itemId;
            this.level = level;
            this.tier = tier;
            this.animationId = animationId;
        }
    }

    public static final PickaxeDef[] PICKAXES = {
        new PickaxeDef(44834, 90, 90, 32618), // Pickaxe of earth and song
        new PickaxeDef(45642, 90, 90, 32610), // Elder rune pickaxe
        new PickaxeDef(45154, 80, 80, 32606), // Bane pickaxe
        new PickaxeDef(32646, 70, 70, 32614), // Crystal pickaxe
        new PickaxeDef(46372, 70, 70, 32602), // Necronium pickaxe
        new PickaxeDef(15259, 60, 60, 32588), // Dragon pickaxe
        new PickaxeDef(13661, 60, 60, 10222), // Inferno adze
        new PickaxeDef(45548, 50, 50, 32566),  // Rune pickaxe
        new PickaxeDef(45521, 40, 40, 32562),  // Adamant pickaxe
        new PickaxeDef(45494, 30, 30, 32558),  // Mithril pickaxe
        new PickaxeDef(45467, 20, 20, 32552),  // Steel pickaxe
        new PickaxeDef(1267, 10, 10, 32548),  // Iron pickaxe
        new PickaxeDef(1265, 1, 1, 32540)     // Bronze pickaxe
    };

    private static final int[] ORES_AND_GEMS = {
        434, 1761, 436, 438, 440, 442, 453, 6971, 6973, 6975, 6977, 444, 447, 449, 44820,
        6979, 6981, 6983, 451, 44822, 44824, 44826, 44828, 21778, 44830, 44832,
        1625, 1627, 1629, 1623, 1621, 1619, 1617, 1631, 6571
    };

    private Native950Mining() { }

    public static boolean isRock(WorldObject rock) { return definition(rock) != null; }

    public static boolean start(Player player, WorldObject rock) {
        if (player == null || !player.isNative950()) return false;
        RockDefinitions type = definition(rock);
        return type != null && player.getActionManager().setAction(new Mining(rock, type));
    }

    public static RockDefinitions definition(WorldObject rock) {
        if (rock == null || Cache.STORE == null || !Cache.isFlatReadOnly() || !Native950Scenery.isHarvestableShape(rock)) return null;
        try {
            ObjectDefinitions d = rock.getDefinitions();
            if (!d.loaded || d.transforms != null || d.sizeX < 1 || d.sizeY < 1
                    || d.sizeX > 16 || d.sizeY > 16) return null;
            return definition(d.name, d.options);
        } catch (RuntimeException unavailable) { return null; }
    }

    public static RockDefinitions definition(String name, String[] options) {
        boolean mine = false;
        if (options != null) for (String option : options) mine |= "Mine".equalsIgnoreCase(option);
        if (!mine || name == null) return null;
        switch (name.toLowerCase(Locale.ROOT)) {
            case "clay rock": return RockDefinitions.CLAY;
            case "soft clay rock": return RockDefinitions.SOFT_CLAY;
            case "copper rock": return RockDefinitions.Copper_Ore;
            case "tin rock": return RockDefinitions.Tin_Ore;
            case "iron rock": return RockDefinitions.Iron_Ore;
            case "silver rock": return RockDefinitions.Silver_Ore;
            case "coal rock": return RockDefinitions.Coal_Ore;
            case "sandstone rock": return RockDefinitions.Sandstone_Ore;
            case "gold rock": return RockDefinitions.Gold_Ore;
            case "mithril rock": return RockDefinitions.Mithril_Ore;
            case "adamantite rock": return RockDefinitions.Adamant_Ore;
            case "luminite rock": return RockDefinitions.LUMINITE_ORE;
            case "granite rock": return RockDefinitions.Granite_Ore;
            case "runite rock": return RockDefinitions.Runite_Ore;
            case "orichalcite rock": return RockDefinitions.ORICHALCITE_ORE;
            case "drakolith rock": return RockDefinitions.DRAKOLITH_ORE;
            case "phasmatite rock": return RockDefinitions.PHASMATITE_ORE;
            case "necrite rock": return RockDefinitions.NECRITE_ORE;
            case "banite rock": return RockDefinitions.BANE_ORE;
            case "light animica rock": return RockDefinitions.LIGHT_ORE;
            case "dark animica rock": return RockDefinitions.DARK_ORE;
            case "gem rock":
            case "common gem rock":
            case "uncommon gem rock":
            case "precious gem rock":
            case "rare gem rock":
                return RockDefinitions.GEM_ROCK;
            default: return null;
        }
    }

    public static boolean current(WorldObject rock) {
        if (rock == null || rock.getType() < 0 || rock.getType() > 22) return false;
        WorldObject actual = World.getRegion(rock.getRegionId()).getObjectWithType(
                rock.getPlane(), rock.getXInRegion(), rock.getYInRegion(), rock.getType());
        return actual != null && actual.getId() == rock.getId() && actual.getRotation() == rock.getRotation();
    }

    public static boolean inReach(Player player, WorldObject rock) {
        ObjectDefinitions d = rock.getDefinitions();
        int width = (rock.getRotation() & 1) == 0 ? d.sizeX : d.sizeY;
        int height = (rock.getRotation() & 1) == 0 ? d.sizeY : d.sizeX;
        int x = player.getX(), y = player.getY();
        if (player.getPlane() != rock.getPlane() || player.hasWalkSteps()) return false;
        boolean adjacent = x >= rock.getX() - 1 && x <= rock.getX() + width && y >= rock.getY() - 1 && y <= rock.getY() + height
                && !(x >= rock.getX() && x < rock.getX() + width && y >= rock.getY() && y < rock.getY() + height);
        return adjacent && RouteFinder.findRoute(RouteFinder.WALK_ROUTEFINDER, x, y, player.getPlane(),
                player.getSize(), new ObjectStrategy(rock), false) == 0 && !RouteFinder.lastIsAlternative();
    }

    public static PickaxeDef bestPickaxe(Player player) {
        int level = player.getSkills().getLevelForXp(Skills.MINING);
        for (PickaxeDef pickaxe : PICKAXES) {
            if (level >= pickaxe.level && (player.getInventory().containsItem(pickaxe.itemId, 1)
                    || player.getEquipment().getWeaponId() == pickaxe.itemId || Native950Toolbelt.has(player,pickaxe.itemId))
                    && itemEntry(pickaxe.itemId) != null && animation(pickaxe) >= 0) {
                return pickaxe;
            }
        }
        return null;
    }

    public static int animation(PickaxeDef pickaxe) {
        if (pickaxe == null || !verify("item", pickaxe.itemId, 19, 8)
                || !verify("sequence", pickaxe.animationId, 20, 7)) return -1;
        AnimationDefinitions sequence = AnimationDefinitions.getAnimationDefinitions(pickaxe.animationId);
        // The paired sequence itself names the held tool. This caught obsolete 910 pickaxe IDs.
        return sequence.leftHandItem == pickaxe.itemId || sequence.rightHandItem == pickaxe.itemId
                ? pickaxe.animationId : -1;
    }

    public static int animationDelay(int animationId) {
        // Keep the original engine delay unit (600 ms), using the paired sequence length.
        return Math.max(1, (AnimationDefinitions.getAnimationDefinitions(animationId).getEmoteTime() + 599) / 600);
    }

    /** RS Wiki Mining stamina milestones; visible Agility boosts extend capacity. */
    public static int staminaCapacity(int mining, int agility) {
        if (mining < 15) return 0;
        int base = mining >= 88 ? 110 : mining >= 71 ? 100 : mining >= 67 ? 90 : mining >= 57 ? 80
                : mining >= 46 ? 70 : mining >= 33 ? 60 : mining >= 26 ? 50 : mining >= 19 ? 40 : 30;
        return base + Math.max(1, Math.min(255, agility));
    }
    public static int staminaPercent(int stamina, int capacity) {
        return capacity <= 0 ? 100 : (int)Math.min(100L, Math.max(0L, stamina * 100L / capacity));
    }
    public static double staminaMultiplier(int mining, int stamina, int capacity) {
        if (mining < 15) return 1.0;
        if (capacity <= 0 || stamina <= 0) return 0.2;
        return stamina >= capacity ? 1.0 : 0.9;
    }

    public static void progress(Player player, int stamina, int oreProgress, int durability) {
        player.getNextHitBars().removeIf(bar -> bar instanceof MiningHitBar);
        player.getNextHitBars().add(player.getSkills().getLevel(Skills.MINING) < 15
                ? MiningHitBar.removeStamina() : MiningHitBar.stamina(stamina));
        player.getNextHitBars().add(MiningHitBar.progress((int)Math.min(100L, Math.max(0L, oreProgress * 100L / Math.max(1, durability)))));
    }

    public static void clearProgress(Player player) {
        player.getNextHitBars().removeIf(bar -> bar instanceof MiningHitBar);
        player.getNextHitBars().add(MiningHitBar.removeStamina());
        player.getNextHitBars().add(MiningHitBar.removeProgress());
    }

    public static boolean validEmptyRock(RockDefinitions type) {
        int id = type.getEmptyId();
        if (id < 0) return true;
        if (!verify("object", id, 16, 8)) return false;
        ObjectDefinitions d = ObjectDefinitions.getObjectDefinitions(id);
        return "Rock".equalsIgnoreCase(d.name) && d.transforms == null;
    }

    public static Native950ItemCatalog.Entry itemEntry(int id) {
        if (!verify("item", id, 19, 8)) return null;
        ItemDefinitions item = strictItem(id);
        if (item == null || item.certTemplateId != -1 || item.lendTemplateId != -1 || item.bindTemplateId != -1
                || item.shardTemplateId != -1 || item.isStackable()) return null;
        boolean isPick = false;
        for (PickaxeDef candidate : PICKAXES) isPick |= (candidate.itemId == id);
        String[] options = new String[5];
        if (isPick) {
            if (item.getEquipSlot() != 3 || !item.containsInventoryOption(1, "Wield")
                    || item.getMaleWornModelId1() < 0 || item.getFemaleWornModelId1() < 0) return null;
            options[1] = "Wield";
            return new Native950ItemCatalog.Entry(id, item.getName(), false, options, 3, 2);
        }
        boolean isOreOrGem = false;
        for (int candidate : ORES_AND_GEMS) isOreOrGem |= (candidate == id);
        if (!isOreOrGem) return null;
        options[4] = "Drop";
        return new Native950ItemCatalog.Entry(id, item.getName(), false, options);
    }

    private static ItemDefinitions strictItem(int id) {
        try {
            return ItemDefinitions.decodeStrict947(id, Cache.STORE.getIndexes()[19].getFile(id >>> 8, id & 255), null);
        } catch (RuntimeException invalid) {
            return null;
        }
    }

    private static synchronized boolean verify(String kind, int id, int index, int shift) {
        if (Cache.STORE == null || !Cache.isFlatReadOnly()) return false;
        if (verifiedStore != Cache.STORE) { verifiedStore = Cache.STORE; verified.clear(); }
        String key = kind + "." + id;
        if (!verified.containsKey(key)) {
            String expected = PINS.getProperty(key);
            boolean valid = false;
            if (expected != null) {
                try {
                    byte[] bytes = Cache.STORE.getIndexes()[index].getFile(id >>> shift, id & ((1 << shift) - 1));
                    if (bytes != null) {
                        StringBuilder hex = new StringBuilder();
                        for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes)) hex.append(String.format("%02x", b & 255));
                        valid = expected.equals(hex.toString());
                    }
                } catch (Exception invalid) {
                    valid = false;
                }
            }
            verified.put(key, valid);
        }
        return verified.get(key);
    }

    private static Properties pins() {
        Properties result = new Properties();
        try (InputStream in = Native950Mining.class.getResourceAsStream("/native950/mining-assets-950.properties")) {
            if (in == null) throw new IllegalStateException("Missing 950 mining bindings");
            result.load(in);
            return result;
        } catch (java.io.IOException invalid) {
            throw new ExceptionInInitializerError(invalid);
        }
    }
}
