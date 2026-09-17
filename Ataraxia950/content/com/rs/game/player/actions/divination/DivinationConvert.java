package com.rs.game.player.actions.divination;

import com.rs.game.Animation;
import com.rs.game.Graphics;
import com.rs.game.player.Player;
import com.rs.game.player.Skills;
import com.rs.game.player.actions.Action;
import com.rs.utils.Utils;

/**
 * Used to handle collected memory depositing.
 *
 * @author Noel
 */
public class DivinationConvert extends Action {

	private MemoryInfo info;
	private boolean enriched;
	private ConvertMode mode;
    private com.rs.game.player.client.Native950Divination.ConvertJourney native950Journey;
    public DivinationConvert(com.rs.game.WorldObject rift, ConvertMode mode) {
        this.mode = mode;
        native950Journey = new com.rs.game.player.client.Native950Divination.ConvertJourney(rift, mode);
    }
    public static double ordinaryEnergyRate(MemoryInfo info, int level, boolean enriched, boolean boon) {
        double[] rates = ENERGY_CONVERSION_RATES[info.ordinal()];
        int offset = Math.max(0, level - info.getLevel());
        return rates[Math.min(offset, rates.length - 1)] * (enriched ? 1.5 : 1) * (boon ? BOON_MULT : 1);
    }
    public static double ordinaryXp(MemoryInfo info, boolean enriched, ConvertMode mode, boolean energyPaid, boolean boon) {
        double base = info.getXp() * (enriched ? ENRICHED_XP_MULT : 1);
        double xp = mode == ConvertMode.CONVERT_TO_ENERGY ? 1 : base * (mode == ConvertMode.CONVERT_TO_MORE_XP && energyPaid ? MORE_XP_MULT : 1);
        return xp * (boon ? BOON_MULT : 1);
    }


	private static final double ENRICHED_XP_MULT = 2.0;
	private static final double MORE_XP_MULT = 1.25;
	private static final double BOON_MULT = 1.1;
	private static final double[][] ENERGY_CONVERSION_RATES = {
			{ 1.5, 1.53, 1.57, 1.6, 1.63, 1.67, 1.7, 1.73, 1.8 },
			{ 1.5, 1.53, 1.57, 1.6, 1.63, 1.67, 1.7, 1.73, 1.77, 1.8 },
			{ 1.55, 1.63, 1.72, 1.8, 1.88, 1.97, 2.05, 2.13, 2.22, 2.3 },
			{ 1.9, 1.96, 2.01, 2.07, 2.12, 2.18, 2.23, 2.29, 2.34, 2.4 },
			{ 2.0, 2.09, 2.19, 2.28, 2.38, 2.47, 2.57, 2.66, 2.76, 2.85 },
			{ 2.45, 2.5, 2.55, 2.6, 2.65, 2.7, 2.75, 2.8, 2.85, 2.9 },
			{ 2.45, 2.57, 2.68, 2.8, 2.92, 3.03, 3.15, 3.27, 3.38, 3.5 },
			{ 2.8, 2.89, 2.99, 3.08, 3.18, 3.27, 3.37, 3.46, 3.56, 3.65 },
			{ 2.85, 3.06, 3.28, 3.49, 3.7 },
			{ 2.9, 3.15, 3.4, 3.65, 3.9 },
			{ 3.2, 3.45, 3.7, 3.95, 4.2 },
			{ 3.5, 3.78, 4.05, 4.33, 5.2 } };

	public DivinationConvert(Player player, Object[] args) {
		setMode((ConvertMode) args[0]);
		checkAll(player);
	}



	private int getMemoryItemId() {
		return enriched ? info.getEnrichedMemoryId() : info.getMemoryId();
	}


	private double getBaseConvertXp() {
		double base = info.getXp();
		return enriched ? base * ENRICHED_XP_MULT : base;
	}


	private int getEnergyCostForMoreXp() {
		return enriched ? 10 : 5;
	}


	private int getEnergyReward(Player player) {
        double rate = ordinaryEnergyRate(info, player.getSkills().getLevel(Skills.DIVINATION), enriched, player.getBoon(info.ordinal()));
		int amount = (int) rate;
		if (Utils.randomDouble() < rate - amount)
			amount++;
		return Math.max(1, amount);
	}


	private double getXpForMode(Player player, ConvertMode mode, boolean hasEnergyForMoreXp) {
        return ordinaryXp(info, enriched, mode, hasEnergyForMoreXp, player.getBoon(info.ordinal()));
	}


	public boolean checkAll(Player player) {
		boolean enrichedFirst = mode == ConvertMode.CONVERT_TO_MORE_XP;
		if (enrichedFirst) {
			for (MemoryInfo i : MemoryInfo.values()) {
				if (i != MemoryInfo.PALE && player.getInventory().containsItem(i.getEnrichedMemoryId(), 1)) {
					info = i;
					enriched = true;
					return true;
				}
			}
		}
		for (MemoryInfo i : MemoryInfo.values()) {
			if (player.getInventory().containsItem(i.getMemoryId(), 1)) {
				info = i;
				enriched = false;
				return true;
			}
			if (!enrichedFirst && i != MemoryInfo.PALE && player.getInventory().containsItem(i.getEnrichedMemoryId(), 1)) {
				info = i;
				enriched = true;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean start(Player player) {
        if (native950Journey != null) { setActionDelay(player, 1); return native950Journey.start(player); }
		return checkAll(player);
	}

	@Override
	public boolean process(Player player) {
        if (native950Journey != null) return native950Journey.process(player);
		return checkAll(player);
	}

	@Override
	public int processWithDelay(Player player) {
        if (native950Journey != null) return native950Journey.convert(player);
		switch (mode) {
		case CONVERT_TO_ENERGY:
			if (player.getAnimations().hasPowerConversion && player.getAnimations().powerConversion) {
				player.setNextAnimation(new Animation(22858));
				player.setNextGraphics(new Graphics(4623));
			} else if (player.getAnimations().hasAgileConversion && player.getAnimations().agileConversion) {
				player.setNextAnimation(new Animation(22867));
				player.setNextGraphics(new Graphics(4622));
			} else if (player.getAnimations().hasAncestralConversion && player.getAnimations().ancestralConversion) {
				player.setNextAnimation(new Animation(29493));
				player.setNextGraphics(new Graphics(6389));
			} else {
				player.setNextAnimation(new Animation(21232));
				player.setNextGraphics(new Graphics(4239));
			}
			player.getSkills().addXp(Skills.DIVINATION, getXpForMode(player, ConvertMode.CONVERT_TO_ENERGY, false));
			player.getInventory().deleteItem(getMemoryItemId(), 1);
			player.getInventory().addItem(info.getEnergyId(), getEnergyReward(player));
			break;
		case CONVERT_TO_XP:
			if (player.getAnimations().hasPowerConversion && player.getAnimations().powerConversion) {
				player.setNextAnimation(new Animation(22858));
				player.setNextGraphics(new Graphics(4625));
			} else if (player.getAnimations().hasAgileConversion && player.getAnimations().agileConversion) {
				player.setNextAnimation(new Animation(22867));
				player.setNextGraphics(new Graphics(4627));
			} else if (player.getAnimations().hasAncestralConversion && player.getAnimations().ancestralConversion) {
				player.setNextAnimation(new Animation(29493));
				player.setNextGraphics(new Graphics(6389));
			} else {
				player.setNextAnimation(new Animation(21234));
				player.setNextGraphics(new Graphics(4240));
			}
			player.getSkills().addXp(Skills.DIVINATION, getXpForMode(player, ConvertMode.CONVERT_TO_XP, false));
			player.getInventory().deleteItem(getMemoryItemId(), 1);
			break;
			case CONVERT_TO_MORE_XP: {
				int energyCost = getEnergyCostForMoreXp();
				boolean hasEnergy = player.getInventory().containsItem(info.getEnergyId(), energyCost);


				if (player.getAnimations().hasPowerConversion && player.getAnimations().powerConversion) {
					player.setNextAnimation(new Animation(22858));
					player.setNextGraphics(new Graphics(4625));
				} else if (player.getAnimations().hasAgileConversion && player.getAnimations().agileConversion) {
					player.setNextAnimation(new Animation(22867));
					player.setNextGraphics(new Graphics(4622));
				} else if (player.getAnimations().hasAncestralConversion && player.getAnimations().ancestralConversion) {
					player.setNextAnimation(new Animation(29493));
					player.setNextGraphics(new Graphics(6389));
				} else {
					player.setNextAnimation(new Animation(21234));
					player.setNextGraphics(new Graphics(4240));
				}


				player.getSkills().addXp(Skills.DIVINATION, getXpForMode(player, ConvertMode.CONVERT_TO_MORE_XP, hasEnergy));


				player.getInventory().deleteItem(getMemoryItemId(), 1);


				if (hasEnergy) {
					player.getInventory().deleteItem(info.getEnergyId(), energyCost);
				}
				break;
			}

		}
		return 1;
	}

	@Override
	public void stop(Player player) {
        if (native950Journey != null) native950Journey.stop(player);
		setActionDelay(player, 1);
	}

	public boolean isEnriched() {
		return enriched;
	}

	public void setEnriched(boolean enriched) {
		this.enriched = enriched;
	}

	public MemoryInfo getInfo() {
		return info;
	}

	public void setInfo(MemoryInfo info) {
		this.info = info;
	}

	public ConvertMode getMode() {
		return mode;
	}

	public void setMode(ConvertMode mode) {
		this.mode = mode;
	}

	public enum ConvertMode {
		CONVERT_TO_ENERGY, CONVERT_TO_XP, CONVERT_TO_MORE_XP
	}
}
