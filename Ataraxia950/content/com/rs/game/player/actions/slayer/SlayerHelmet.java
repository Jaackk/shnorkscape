package com.rs.game.player.actions.slayer;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.item.Item;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Utils;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;

import java.util.ArrayList;

/**
 * @author Kris | 2. okt 2018 : 19:17:18
 * @see <a href="https://www.rune-server.ee/members/kris/">Rune-Server profile</a>
 * @see <a href="https://rune-status.net/members/kris.354/">Rune-Status profile</a>
 */
@AllArgsConstructor
public enum SlayerHelmet {

	SLAYER_HELMET(13263, -1, null, 1.1F, -1) {
		@Override
		public float getBoost(final int skill) {
			if (skill == Skills.RANGE || skill == Skills.MAGIC) {
				return 1F;
			}
			return 1.125F;
		}
	},

	NA_FULL_SLAYER_HELMET(15492, 0, null, 1.125F, -1),
	RED_FULL_SLAYER_HELMET(22528, 0, Dye.RED, 1.125F, -1),
	BLUE_FULL_SLAYER_HELMET(22534, 0, Dye.BLUE, 1.125F, -1),
	GREEN_FULL_SLAYER_HELMET(22540, 0, Dye.GREEN, 1.125F, -1),
	YELLOW_FULL_SLAYER_HELMET(22546, 0, Dye.YELLOW, 1.125F, -1),

	NA_REINFORCED_SLAYER_HELMET(30656, 1, null, 1.13F, 400),
	RED_REINFORCED_SLAYER_HELMET(30659, 1, Dye.RED, 1.13F, 400),
	BLUE_REINFORCED_SLAYER_HELMET(30662, 1, Dye.BLUE, 1.13F, 400),
	GREEN_REINFORCED_SLAYER_HELMET(30665, 1, Dye.GREEN, 1.13F, 400),
	YELLOW_REINFORCED_SLAYER_HELMET(30668, 1, Dye.YELLOW, 1.13F, 400),

	NA_STRONG_SLAYER_HELMET(30686, 2, null, 1.135F, 600),
	RED_STRONG_SLAYER_HELMET(30689, 2, Dye.RED, 1.135F, 600),
	BLUE_STRONG_SLAYER_HELMET(30692, 2, Dye.BLUE, 1.135F, 600),
	GREEN_STRONG_SLAYER_HELMET(30695, 2, Dye.GREEN, 1.135F, 600),
	YELLOW_STRONG_SLAYER_HELMET(30698, 2, Dye.YELLOW, 1.135F, 600),

	NA_MIGHTY_SLAYER_HELMET(30716, 3, null, 1.14F, 800),
	RED_MIGHTY_SLAYER_HELMET(30719, 3, Dye.RED, 1.14F, 800),
	BLUE_MIGHTY_SLAYER_HELMET(30722, 3, Dye.BLUE, 1.14F, 800),
	GREEN_MIGHTY_SLAYER_HELMET(30725, 3, Dye.GREEN, 1.14F, 800),
	YELLOW_MIGHTY_SLAYER_HELMET(30728, 3, Dye.YELLOW, 1.14F, 800),

	NA_CORRUPTED_SLAYER_HELMET(40377, 4, null, 1.145F, 1000),
	RED_CORRUPTED_SLAYER_HELMET(40380, 4, Dye.RED, 1.145F, 1000),
	BLUE_CORRUPTED_SLAYER_HELMET(40383, 4, Dye.BLUE, 1.145F, 1000),
	GREEN_CORRUPTED_SLAYER_HELMET(40386, 4, Dye.GREEN, 1.145F, 1000),
	YELLOW_CORRUPTED_SLAYER_HELMET(40389, 4, Dye.YELLOW, 1.145F, 1000);

	@Getter
	private final int id;
	@Getter
	private final int tier;
	@Getter
	private final Dye dye;
	private final float boost;
	@Getter private final int cost;

	public static final SlayerHelmet[] VALUES = values();
	public static final SlayerHelmet[] UNCOLOURED_VALUES;
	public static final Int2ObjectOpenHashMap<SlayerHelmet> MAP = new Int2ObjectOpenHashMap<SlayerHelmet>(VALUES.length);
	
	public static final float getSlayerBoost(final Player player, final int skill) {
		val helmId = player.getEquipment().getHatId();
		val helm = MAP.get(helmId);
		if (helm == null) {
			return 1F;
		}
		return helm.getBoost(skill);
	}

	static {
		val list = new ArrayList<SlayerHelmet>(5);
		for (int i = VALUES.length - 1; i >= 0; i--) {
			val value = VALUES[i];
			MAP.put(value.id, value);
			if (value.tier == -1 || value.dye != null) {
				continue;
			}
			list.add(value);
		}
		UNCOLOURED_VALUES = list.toArray(new SlayerHelmet[list.size()]);
	}

	public float getBoost(final int skill) {
		return boost;
	}

	public SlayerHelmet getNextHelmet() {
		if (tier == 4 || tier == -1) {
			return null;
		}
		val nextTier = tier + 1;
		for (int i = VALUES.length - 1; i >= 0; i--) {
			val helm = VALUES[i];
			if (helm.tier == nextTier && helm.dye == dye) {
				return helm;
			}
		}
		return null;
	}
	
	public static final boolean uncolour(final Player player, final Item itemUsed, final Item usedWith) {
		val cloth = itemUsed.getId() == 3188 ? itemUsed : usedWith.getId() == 3188 ? usedWith : null;
		if (cloth == null) {
			return false;
		}
		val helmet = cloth == itemUsed ? usedWith : itemUsed;
		val constant = MAP.get(helmet.getId());
		if (constant == null) {
			return false;
		}
		if (constant.getDye() == null) {
			return false;
		}
		val uncolouredHelm = Utils.findMatching(VALUES, h -> h.tier == constant.tier && h.dye == null);
		if (uncolouredHelm == null) {
			return false;
		}
		val inventory = player.getInventory();
		inventory.deleteItem(helmet);
		inventory.addItem(new Item(uncolouredHelm.id));
		player.sendMessage("You wipe the Slayer helmet clean.");
		return true;
	}
	
	public static final boolean recolour(final Player player, final Item itemUsed, final Item usedWith) {
		val dye = getDye(itemUsed, usedWith);
		if (dye == null) {
			return false;
		}
		val helm = getHelm(itemUsed, usedWith);
		if (helm == null) {
			return false;
		}
		val recolouredHelmet = Utils.findMatching(VALUES, h -> h.tier == helm.tier && h.dye == dye);
		if (recolouredHelmet == null) {
			return false;
		}
		val inventory = player.getInventory();
		inventory.deleteItem(itemUsed);
		inventory.deleteItem(usedWith);
		inventory.addItem(new Item(recolouredHelmet.id));
		player.sendMessage("You dye your " + ItemDefinitions.getItemDefinitions(recolouredHelmet.id).getName() + " " + dye.toString().toLowerCase() + ".");
		return true;
	}

	private static final Dye getDye(final Item itemUsed, final Item usedWith) {
		Dye dye = Utils.findMatching(Dye.VALUES, d -> d.getId() == itemUsed.getId());
		if (dye == null) {
			dye = Utils.findMatching(Dye.VALUES, d -> d.getId() == usedWith.getId());
		}
		return dye;
	}

	private static final SlayerHelmet getHelm(final Item itemUsed, final Item usedWith) {
		SlayerHelmet helm = Utils.findMatching(UNCOLOURED_VALUES, h -> h.id == itemUsed.getId());
		if (helm == null) {
			helm = Utils.findMatching(UNCOLOURED_VALUES, h -> h.id == usedWith.getId());
		}
		return helm;
	}

}
