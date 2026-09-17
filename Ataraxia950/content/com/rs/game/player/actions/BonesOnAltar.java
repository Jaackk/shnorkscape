package com.rs.game.player.actions;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.activites.halloween.ClueScrollDistributor;
import com.rs.game.item.Item;
import com.rs.game.npc.NPC;
import com.rs.game.npc.others.randomevent.impl.PrayerRandomEvent;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.utils.Colors;
import com.rs.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles Bones on Altar action.
 *
 * @author Noel
 */
public final class BonesOnAltar extends Action {

	public static final String MESSAGE = "The gods are very pleased with your offerings.";
	public static final double MULTIPLIER = 2.5;
	private Bones bone;
	private final Item item;
	private final WorldObject object;
	private static final Animation USING = new Animation(896);

	public BonesOnAltar(final WorldObject object, final Item item) {
		this.item = item;
		this.object = object;
	}

	public final static Bones isGood(Item item) {
		return Bones.forId(item.getId());
	}

	private final double firstAge(Player player) {
		double xpBoost = 1.0;
		if (player.getEquipment().getHatId() == 27587)
			xpBoost *= 1.01;
		if (player.getEquipment().getHatId() == 34925)
			xpBoost *= 1.03;
		if (player.getEquipment().getCapeId() == 27588)
			xpBoost *= 1.01;
		if (player.getEquipment().getAmuletId() == 27589)
			xpBoost *= 1.01;
		if (player.getEquipment().getRingId() == 27590)
			xpBoost *= 1.01;
		if (player.getEquipment().getGlovesId() == 27591)
			xpBoost *= 1.01;
		if (player.getEquipment().getHatId() == 27587 && player.getEquipment().getCapeId() == 27588 && player.getEquipment().getAmuletId() == 27589 && player.getEquipment().getRingId() == 27590 && player.getEquipment().getGlovesId() == 27591)
			xpBoost *= 1.01;
		if (player.getEquipment().getHatId() == 34925 && player.getEquipment().getCapeId() == 27588 && player.getEquipment().getAmuletId() == 27589 && player.getEquipment().getRingId() == 27590 && player.getEquipment().getGlovesId() == 27591)
			xpBoost *= 1.03;
		return xpBoost;
	}

	@Override
	public boolean process(Player player) {
		if (!World.containsObjectWithId(object, object.getId()))
			return false;
		if (!player.getInventory().containsItem(item.getId(), 1))
			return false;
        return player.getInventory().containsItem(bone.getBone().getId(), 1);
    }

	@Override
	public int processWithDelay(Player player) {
		player.closeInterfaces();
		double xpBoost = firstAge(player);
		if (player.getAnimations().hasStrongBurial && player.getAnimations().strongBurial) {
			player.setNextAnimation(new Animation(20294));
			player.setNextGraphics(new Graphics(4001));
		} else {
			player.setNextAnimation(USING);
			player.getPackets().sendGraphics(new Graphics(624), object);
		}
		player.getInventory().deleteItem(item.getId(), 1);
		player.getSkills().addXp(Skills.PRAYER, bone.getXP() * player.getAuraManager().getPrayerMultiplier() * MULTIPLIER * xpBoost);
		player.addBonesOffered();
		if (Utils.random(150) == 0 && player.hasRandomEvent()) {
			if (!player.followedByRandomEventNPC()) {
				NPC npc = new PrayerRandomEvent(player, player);
				if (npc.withinDistance(player, 14)) {
					player.setCurrentRandomEventNPC(npc);
					player.sendMessage("<col=ff0000>An odd looking monk emerges from the altar.");
				}
			}
		}
		player.sendMessage("The Gods are very pleased with your offerings; " + "bones offered: " + Colors.RED + Utils.getFormattedNumber(player.getBonesOffered()) + "</col>.", true);
		ClueScrollDistributor.givePlayerClueScrollIfProbable(player, ClueScrollDistributor.SKILLING_PERCENT);
		player.getInventory().refresh();
		player.faceObject(object);
		return 3;
	}

	@Override
	public boolean start(Player player) {
		if ((this.bone = Bones.forId(item.getId())) == null) {
			return false;
		}
		player.faceObject(object);
		return true;
	}

	@Override
	public void stop(final Player player) {
		this.setActionDelay(player, 3);
	}

	public enum Bones {

		BONES(new Item(526, 1), 5),
		BAT_BONES(new Item(530, 1), 6),
		WOLF_BONES(new Item(2859, 1), 8),
		BIG_BONES(new Item(532, 1), 15),
		JOGRE_BONES(new Item(3125, 1), 15),
		BURNT_JOGRE_BONES(new Item(3127, 1), 16),
		BABYDRAGON_BONES(new Item(534, 1), 30),
		WYVERN_BONES(new Item(6812, 1), 50),
		DRAGON_BONES(new Item(536, 1), 72),
		OURG_BONES(new Item(4834, 1), 140),
		ADAMANT_DRAGON_BONES(new Item(35008), 144),
		FROST_DRAGON_BONES(new Item(18830, 1), 180),
		RUNE_DRAGON_BONES(new Item(35010, 1), 190),
		DAGANNOTH_BONES(new Item(6729, 1), 125),
		IMPIOUS(new Item(20264, 1), 25),
		ACCURSED(new Item(20266, 1), 50),
		INFERNAL(new Item(20268, 1), 130),
		INFERNALA(new Item(22312, 1), 130),
		TORTURED(new Item(32945, 1), 160),
		SEARING(new Item(34159, 1), 190),
		AIRUT_BONES(new Item(30209, 1), 132),
		REINFORCED_DRAGON_BONES(new Item(35010, 1), 190),
	    EVIL_DUST(new Item(3325, 1), 220);

		private static final Map<Integer, Bones> bones = new HashMap<Integer, Bones>();

		static {
			for (Bones bone : Bones.values()) {
				bones.put(bone.getBone().getId(), bone);
			}
		}

		private final Item item;
		private final int xp;

		Bones(final Item item, final int xp) {
			this.item = item;
			this.xp = xp;
		}

		public static final Bones forId(final int itemId) {
			return bones.get(itemId);
		}

		public final Item getBone() {
			return item;
		}

		public final int getXP() {
			return xp;
		}
	}
}