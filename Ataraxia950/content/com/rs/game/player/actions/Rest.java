package com.rs.game.player.actions;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.player.Player;
import com.rs.utils.Utils;

/**
 * Handles the Resting action.
 *
 * @author Noel
 */
public class Rest extends Action {

	private static final int[][] REST_DEFS = { { 5713, 1549, 5748 }, { 11786, 1550, 11788 }, { 5713, 1551, 2921 } };

	private int index;

	@Override
	public boolean process(Player player) {
		if (player.getPoison().isPoisoned()) {
			player.sendMessage("You can't rest while you're poisoned.");
			return false;
		}
		if (player.getAttackedByDelay() + 10000 > Utils.currentTimeMillis()) {
			player.sendMessage("You can't rest until 10 seconds after the end of combat.");
			return false;
		}
		return true;
	}

	@Override
	public int processWithDelay(Player player) {
		if (player.getRunEnergy() > 98)
			player.setRunEnergy(100);
		else if (player.getRunEnergy() <= 98)
			player.setRunEnergy(player.getRunEnergy() + 2);
		return 0;
	}

	@Override
	public boolean start(Player player) {
		if (!process(player))
			return false;
		player.setResting(true);
		if (player.getAnimations().hasZenResting && player.getAnimations().zenResting)
			player.setNextAnimation(new Animation(17301));
		else if (player.getAnimations().hasStrongResting && player.getAnimations().strongResting)
			player.setNextAnimation(new Animation(20304));
		else if (player.getAnimations().hasArcaneResting && player.getAnimations().arcaneResting) {
			player.setNextAnimation(new Animation(20286));
			player.setNextGraphics(new Graphics(4003));
		} else if (player.getAnimations().hasSinisterSlumber && player.getAnimations().sinisterSlumber) {
			player.setNextAnimation(new Animation(23654));
			player.setNextGraphics(new Graphics(4838));
		} else if (player.getAnimations().hasArmWarrior && player.getAnimations().armWarrior) {
			player.setNextAnimation(new Animation(23669));
			player.setNextGraphics(new Graphics(4844));
		} else if (player.getAnimations().hasEneResting && player.getAnimations().eneResting) {
			player.setNextAnimation(new Animation(24684));
			player.setNextGraphics(new Graphics(5173));
		} else if (player.getAnimations().hasCrystalResting && player.getAnimations().crystalResting) {
			player.setNextAnimation(new Animation(24647));
			player.setNextGraphics(new Graphics(5164));
		} else {
			index = Utils.random(REST_DEFS.length);
			player.setNextAnimation(new Animation(REST_DEFS[index][0]));
			player.getAppearence().setRenderEmote(REST_DEFS[index][1]);
		}
		return true;
	}

	@Override
	public void stop(Player player) {
		player.setResting(false);
		if (player.getAnimations().hasZenResting && player.getAnimations().zenResting)
			player.setNextAnimation(new Animation(17303));
		else if (player.getAnimations().hasStrongResting && player.getAnimations().strongResting)
			player.setNextAnimation(new Animation(20306));
		else if (player.getAnimations().hasArcaneResting && player.getAnimations().arcaneResting) {
			player.setNextAnimation(new Animation(20288));
			player.setNextGraphics(new Graphics(4005));
		} else if (player.getAnimations().hasSinisterSlumber && player.getAnimations().sinisterSlumber) {
			player.setNextAnimation(new Animation(23656));
			player.setNextGraphics(new Graphics(4840));
		} else if (player.getAnimations().hasArmWarrior && player.getAnimations().armWarrior) {
			player.setNextAnimation(new Animation(23671));
			player.setNextGraphics(new Graphics(4846));
		} else if (player.getAnimations().hasEneResting && player.getAnimations().eneResting) {
			player.setNextAnimation(new Animation(24685));
			player.setNextGraphics(new Graphics(5175));
		} else if (player.getAnimations().hasCrystalResting && player.getAnimations().crystalResting) {
			player.setNextAnimation(new Animation(24651));
			player.setNextGraphics(new Graphics(5165));
		} else {
			player.getAppearence().setRenderEmote(-1);
			player.setNextAnimation(new Animation(REST_DEFS[index][2]));
		}
		player.getEmotesManager().setNextEmoteEnd();
	}
}