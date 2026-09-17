package com.rs.game.player.content.artisansworkshop;

import com.rs.cache.loaders.ItemDefinitions;
import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.World;
import com.rs.game.WorldObject;
import com.rs.game.WorldTile;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.PerkManager.DonationPerk;
import com.rs.game.player.actions.Action;
import com.rs.game.player.content.artisansworkshop.ArtisansWorkShopConstants.Ingot;
import com.rs.game.player.content.artisansworkshop.ArtisansWorkShopConstants.Track;

public class ArtisansWorkShopAction extends Action {
	private Ingot ingot;
	private Track track;
	private final int productIndex;
	private final WorldObject object;
	private int ticks;
	private int hits;

	public ArtisansWorkShopAction(WorldObject object, Ingot ingot, int productIndex, int ticks) {
		this.object = object;
		this.ingot = ingot;
		this.productIndex = productIndex;
		this.ticks = ticks;
	}

	public ArtisansWorkShopAction(WorldObject object, Track track, int ticks) {
		this.object = object;
		this.track = track;
		this.productIndex = -1;
		this.ticks = ticks;
	}

	@Override
	public boolean start(Player player) {
		if (!checkAll(player))
			return false;
		setActionDelay(player, 1);
		hits = ingot != null ? 4 : 2;
		return true;
	}

	private boolean checkAll(Player player) {
		if (!player.getInventory().containsItem(2347, 1)) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need a hammer in order to work.");
			return false;
		}
		if (ingot != null) {
			if (player.getInventory().getItems().getNumberOf(ingot.getItemId()) < 1) {
				player.getPackets().sendGameMessage("You do not have sufficient bars!");
				return false;
			}
		} else {
			for (int itemId : track.getRequiredItems()) {
				if (!player.getInventory().containsItem(itemId, 1)) {
					player.getPackets().sendGameMessage("You don't have the required items to make this.");
					return false;
				}
			}
		}
		int requiredLevel = ingot != null ? ingot.getRequiredLevel() : track.getRequiredLevel();
		if (player.getSkills().getLevel(Skills.SMITHING) < requiredLevel) {
			player.getDialogueManager().startDialogue("SimpleMessage", "You need a Smithing level of " + requiredLevel + " to create this.");
			return false;
		}
		return true;
	}

	@Override
	public boolean process(Player player) {
		return checkAll(player);
	}

	@Override
	public int processWithDelay(Player player) {
		player.setNextAnimation(new Animation(898));
		World.sendGraphics(player, new Graphics(2123), new WorldTile(object));
		hits--;
		if (hits == 0) {
			ticks--;
			double xp = 0;
			if (ingot != null) {
				player.getInventory().deleteItem(ingot.getItemId(), 1);
				player.getInventory().addItem(ingot.getProducts()[productIndex], 1);

				xp = ingot.getXp();
				player.getPackets().sendGameMessage("You smith the bar into a " + ItemDefinitions.getItemDefinitions(ingot.getProducts()[productIndex]).getName().toLowerCase() + ".", true);
				if (productIndex == ArtisansWorkShop.currentInstructions) {
					xp *= 1.1;
					player.getPackets().sendGameMessage("You receive +10% xp bonus for following suak instructions.", true);
				}
			} else {
				for (int itemId : track.getRequiredItems())
					player.getInventory().deleteItem(itemId, 1);
				player.getInventory().addItem(track.getItemId(), 1);
				xp = track.getXp();
				player.getPackets().sendGameMessage("You smith the bar into a " + ItemDefinitions.getItemDefinitions(track.getItemId()).getName().toLowerCase() + ".", true);
			}
			player.getArtisansWorkShop().increaseArtisansXPGained((int) xp);
			player.getSkills().addXp(Skills.SMITHING, xp);
			hits = ingot != null ? 3 : 1;
		}
		if (ticks > 0) {
			return player.getPerkManager().hasPerkActive(DonationPerk.ALCHEMIC_SMITHING) ? 3 : 4;
		}
		return -1;
	}

	@Override
	public void stop(Player player) {
		this.setActionDelay(player, 3);
	}

}