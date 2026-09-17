package com.rs.game.player;

import java.io.Serializable;

/**
 * Handles everything related to Animation Overrides.
 *
 * @author Noel.
 */
public class AnimationOverrides implements Serializable {

	/**
	 * The generated serial UID.
	 */
	private static final long serialVersionUID = -773529284927927947L;
	/**
	 * Representing all available overrides. IF SOMETHING HERE IS ADDED, ADD IT TO {@link AnimationOverrides#transferOverrides(Player, Player)}
	 */
	public boolean battleCry, enhancedPotion, lumberjackWc, deepFishing, zenResting, karateFletch, ironSmith, chiMining,
			samuraiCook, roundHouseWc, blastMining, strongResting, arcaneSmelt, arcaneResting, strongWc, strongMining,
			arcaneFishing, strongBurial, arcaneCook, powerDivination, powerConversion, agileDivination, agileConversion,
			sinisterSlumber, armWarrior, eneResting, crystalResting, headMining, sandWalk, sadWalk, angryWalk,
			proudWalk, happyWalk, barbarianWalk, revenantWalk, blinkWalk, catWalk, singerMining, explosiveWc, singerWc, bombFiremaking, partyhatFiremaking,
			ancestralDivination1, ancestralDivination2, ancestralConversion, zombieWalk, congaWalk, lovedUpWalk, slowmoWalk, partyFeverWalk, assassinWalk,
			twistedJesterWalk, hotSandWalk, plagueDoctorWalk;
	/**
	 * Representing all available unlocked overrides. IF SOMETHING HERE IS ADDED, ADD IT TO {@link AnimationOverrides#transferOverrides(Player, Player)}
	 */
	public boolean hasBattleCry, hasEnhancedPotion, hasLumberjackWc, hasDeepFishing, hasZenResting, hasKarateFletch,
			hasIronSmith, hasChiMining, hasSamuraiCook, hasRoundHouseWc, hasBlastMining, hasStrongResting,
			hasArcaneSmelt, hasArcaneResting, hasStrongWc, hasStrongMining, hasArcaneFishing, hasStrongBurial,
			hasArcaneCook, hasPowerDivination, hasPowerConversion, hasAgileDivination, hasAgileConversion,
			hasSinisterSlumber, hasArmWarrior, hasEneResting, hasCrystalResting, hasHeadMining, hasSandWalk, hasSadWalk,
			hasAngryWalk, hasProudWalk, hasHappyWalk, hasBarbarianWalk, hasRevenantWalk, hasBlinkWalk, hasCatWalk, hasSingerMining,
			hasExplosiveWc, hasSingerWc, hasBombFiremaking, hasPartyhatFiremaking, hasAncestralDivination1, hasAncestralDivination2, hasAncestralConversion,
			hasZombieWalk, hasCongaWalk, hasLovedUpWalk, hasSlowmoWalk, hasPartyFeverWalk, hasAssassinWalk,
			hasTwistedJesterWalk, hasHotSandWalk, hasPlagueDoctorWalk;
	/**
	 * The player instance.
	 */
	@SuppressWarnings("unused")
	private transient Player player;

	/**
	 * The player instance saving to.
	 *
	 * @param player
	 *            The player.
	 */
	protected void setPlayer(Player player) {
		this.player = player;
	}

	public static void transferOverrides(Player from, Player to) {
		if (from.getAnimations().battleCry) {
			to.getAnimations().battleCry = from.getAnimations().battleCry;
		}
		if (from.getAnimations().enhancedPotion) {
			to.getAnimations().enhancedPotion = from.getAnimations().enhancedPotion;
		}
		if (from.getAnimations().lumberjackWc) {
			to.getAnimations().lumberjackWc = from.getAnimations().lumberjackWc;
		}
		if (from.getAnimations().deepFishing) {
			to.getAnimations().deepFishing = from.getAnimations().deepFishing;
		}
		if (from.getAnimations().zenResting) {
			to.getAnimations().zenResting = from.getAnimations().zenResting;
		}
		if (from.getAnimations().karateFletch) {
			to.getAnimations().karateFletch = from.getAnimations().karateFletch;
		}
		if (from.getAnimations().ironSmith) {
			to.getAnimations().ironSmith = from.getAnimations().ironSmith;
		}
		if (from.getAnimations().chiMining) {
			to.getAnimations().chiMining = from.getAnimations().chiMining;
		}
		if (from.getAnimations().samuraiCook) {
			to.getAnimations().samuraiCook = from.getAnimations().samuraiCook;
		}
		if (from.getAnimations().roundHouseWc) {
			to.getAnimations().roundHouseWc = from.getAnimations().roundHouseWc;
		}
		if (from.getAnimations().blastMining) {
			to.getAnimations().blastMining = from.getAnimations().blastMining;
		}
		if (from.getAnimations().strongResting) {
			to.getAnimations().strongResting = from.getAnimations().strongResting;
		}
		if (from.getAnimations().arcaneSmelt) {
			to.getAnimations().arcaneSmelt = from.getAnimations().arcaneSmelt;
		}
		if (from.getAnimations().arcaneResting) {
			to.getAnimations().arcaneResting = from.getAnimations().arcaneResting;
		}
		if (from.getAnimations().strongWc) {
			to.getAnimations().strongWc = from.getAnimations().strongWc;
		}
		if (from.getAnimations().strongMining) {
			to.getAnimations().strongMining = from.getAnimations().strongMining;
		}
		if (from.getAnimations().arcaneFishing) {
			to.getAnimations().arcaneFishing = from.getAnimations().arcaneFishing;
		}
		if (from.getAnimations().strongBurial) {
			to.getAnimations().strongBurial = from.getAnimations().strongBurial;
		}
		if (from.getAnimations().arcaneCook) {
			to.getAnimations().arcaneCook = from.getAnimations().arcaneCook;
		}
		if (from.getAnimations().powerDivination) {
			to.getAnimations().powerDivination = from.getAnimations().powerDivination;
		}
		if (from.getAnimations().powerConversion) {
			to.getAnimations().powerConversion = from.getAnimations().powerConversion;
		}
		if (from.getAnimations().agileDivination) {
			to.getAnimations().agileDivination = from.getAnimations().agileDivination;
		}
		if (from.getAnimations().agileConversion) {
			to.getAnimations().agileConversion = from.getAnimations().agileConversion;
		}
		if (from.getAnimations().sinisterSlumber) {
			to.getAnimations().sinisterSlumber = from.getAnimations().sinisterSlumber;
		}
		if (from.getAnimations().armWarrior) {
			to.getAnimations().armWarrior = from.getAnimations().armWarrior;
		}
		if (from.getAnimations().eneResting) {
			to.getAnimations().eneResting = from.getAnimations().eneResting;
		}
		if (from.getAnimations().crystalResting) {
			to.getAnimations().crystalResting = from.getAnimations().crystalResting;
		}
		if (from.getAnimations().headMining) {
			to.getAnimations().headMining = from.getAnimations().headMining;
		}
		if (from.getAnimations().sandWalk) {
			to.getAnimations().sandWalk = from.getAnimations().sandWalk;
		}
		if (from.getAnimations().sadWalk) {
			to.getAnimations().sadWalk = from.getAnimations().sadWalk;
		}
		if (from.getAnimations().angryWalk) {
			to.getAnimations().angryWalk = from.getAnimations().angryWalk;
		}
		if (from.getAnimations().proudWalk) {
			to.getAnimations().proudWalk = from.getAnimations().proudWalk;
		}
		if (from.getAnimations().happyWalk) {
			to.getAnimations().happyWalk = from.getAnimations().happyWalk;
		}
		if (from.getAnimations().barbarianWalk) {
			to.getAnimations().barbarianWalk = from.getAnimations().barbarianWalk;
		}
		if (from.getAnimations().revenantWalk) {
			to.getAnimations().revenantWalk = from.getAnimations().revenantWalk;
		}
		if (from.getAnimations().blinkWalk) {
			to.getAnimations().blinkWalk = from.getAnimations().blinkWalk;
		}
		if (from.getAnimations().catWalk) {
			to.getAnimations().catWalk = from.getAnimations().catWalk;
		}
		if (from.getAnimations().hasBattleCry) {
			to.getAnimations().hasBattleCry = from.getAnimations().hasBattleCry;
		}
		if (from.getAnimations().hasEnhancedPotion) {
			to.getAnimations().hasEnhancedPotion = from.getAnimations().hasEnhancedPotion;
		}
		if (from.getAnimations().hasLumberjackWc) {
			to.getAnimations().hasLumberjackWc = from.getAnimations().hasLumberjackWc;
		}
		if (from.getAnimations().hasDeepFishing) {
			to.getAnimations().hasDeepFishing = from.getAnimations().hasDeepFishing;
		}
		if (from.getAnimations().hasZenResting) {
			to.getAnimations().hasZenResting = from.getAnimations().hasZenResting;
		}
		if (from.getAnimations().hasKarateFletch) {
			to.getAnimations().hasKarateFletch = from.getAnimations().hasKarateFletch;
		}
		if (from.getAnimations().hasIronSmith) {
			to.getAnimations().hasIronSmith = from.getAnimations().hasIronSmith;
		}
		if (from.getAnimations().hasChiMining) {
			to.getAnimations().hasChiMining = from.getAnimations().hasChiMining;
		}
		if (from.getAnimations().hasSamuraiCook) {
			to.getAnimations().hasSamuraiCook = from.getAnimations().hasSamuraiCook;
		}
		if (from.getAnimations().hasRoundHouseWc) {
			to.getAnimations().hasRoundHouseWc = from.getAnimations().hasRoundHouseWc;
		}
		if (from.getAnimations().hasBlastMining) {
			to.getAnimations().hasBlastMining = from.getAnimations().hasBlastMining;
		}
		if (from.getAnimations().hasStrongResting) {
			to.getAnimations().hasStrongResting = from.getAnimations().hasStrongResting;
		}
		if (from.getAnimations().hasArcaneSmelt) {
			to.getAnimations().hasArcaneSmelt = from.getAnimations().hasArcaneSmelt;
		}
		if (from.getAnimations().hasArcaneResting) {
			to.getAnimations().hasArcaneResting = from.getAnimations().hasArcaneResting;
		}
		if (from.getAnimations().hasStrongWc) {
			to.getAnimations().hasStrongWc = from.getAnimations().hasStrongWc;
		}
		if (from.getAnimations().hasStrongMining) {
			to.getAnimations().hasStrongMining = from.getAnimations().hasStrongMining;
		}
		if (from.getAnimations().hasArcaneFishing) {
			to.getAnimations().hasArcaneFishing = from.getAnimations().hasArcaneFishing;
		}
		if (from.getAnimations().hasStrongBurial) {
			to.getAnimations().hasStrongBurial = from.getAnimations().hasStrongBurial;
		}
		if (from.getAnimations().hasArcaneCook) {
			to.getAnimations().hasArcaneCook = from.getAnimations().hasArcaneCook;
		}
		if (from.getAnimations().hasPowerDivination) {
			to.getAnimations().hasPowerDivination = from.getAnimations().hasPowerDivination;
		}
		if (from.getAnimations().hasPowerConversion) {
			to.getAnimations().hasPowerConversion = from.getAnimations().hasPowerConversion;
		}
		if (from.getAnimations().hasAgileDivination) {
			to.getAnimations().hasAgileDivination = from.getAnimations().hasAgileDivination;
		}
		if (from.getAnimations().hasAgileConversion) {
			to.getAnimations().hasAgileConversion = from.getAnimations().hasAgileConversion;
		}
		if (from.getAnimations().hasSinisterSlumber) {
			to.getAnimations().hasSinisterSlumber = from.getAnimations().hasSinisterSlumber;
		}
		if (from.getAnimations().hasArmWarrior) {
			to.getAnimations().hasArmWarrior = from.getAnimations().hasArmWarrior;
		}
		if (from.getAnimations().hasEneResting) {
			to.getAnimations().hasEneResting = from.getAnimations().hasEneResting;
		}
		if (from.getAnimations().hasCrystalResting) {
			to.getAnimations().hasCrystalResting = from.getAnimations().hasCrystalResting;
		}
		if (from.getAnimations().hasHeadMining) {
			to.getAnimations().hasHeadMining = from.getAnimations().hasHeadMining;
		}
		if (from.getAnimations().hasSandWalk) {
			to.getAnimations().hasSandWalk = from.getAnimations().hasSandWalk;
		}
		if (from.getAnimations().hasSadWalk) {
			to.getAnimations().hasSadWalk = from.getAnimations().hasSadWalk;
		}
		if (from.getAnimations().hasAngryWalk) {
			to.getAnimations().hasAngryWalk = from.getAnimations().hasAngryWalk;
		}
		if (from.getAnimations().hasProudWalk) {
			to.getAnimations().hasProudWalk = from.getAnimations().hasProudWalk;
		}
		if (from.getAnimations().hasHappyWalk) {
			to.getAnimations().hasHappyWalk = from.getAnimations().hasHappyWalk;
		}
		if (from.getAnimations().hasBarbarianWalk) {
			to.getAnimations().hasBarbarianWalk = from.getAnimations().hasBarbarianWalk;
		}
		if (from.getAnimations().hasRevenantWalk) {
			to.getAnimations().hasRevenantWalk = from.getAnimations().hasRevenantWalk;
		}
		if (from.getAnimations().hasCatWalk) {
			to.getAnimations().hasCatWalk = from.getAnimations().hasCatWalk;
		}
		if (from.getAnimations().hasBlinkWalk) {
			to.getAnimations().hasBlinkWalk = from.getAnimations().hasBlinkWalk;
		}
		if (from.getAnimations().hasSingerMining) {
			to.getAnimations().hasSingerMining = from.getAnimations().hasSingerMining;
		}
		if (from.getAnimations().hasExplosiveWc) {
			to.getAnimations().hasExplosiveWc = from.getAnimations().hasExplosiveWc;
		}
		if (from.getAnimations().hasSingerWc) {
			to.getAnimations().hasSingerWc = from.getAnimations().hasSingerWc;
		}
		if (from.getAnimations().hasBombFiremaking) {
			to.getAnimations().hasBombFiremaking = from.getAnimations().hasBombFiremaking;
		}
		if (from.getAnimations().hasPartyhatFiremaking) {
			to.getAnimations().hasPartyhatFiremaking = from.getAnimations().hasPartyhatFiremaking;
		}
		if (from.getAnimations().hasAncestralDivination1) {
			to.getAnimations().hasAncestralDivination1 = from.getAnimations().hasAncestralDivination1;
		}
		if (from.getAnimations().hasAncestralDivination2) {
			to.getAnimations().hasAncestralDivination2 = from.getAnimations().hasAncestralDivination2;
		}
		if (from.getAnimations().hasAncestralConversion) {
			to.getAnimations().hasAncestralConversion = from.getAnimations().hasAncestralConversion;
		}
		if (from.getAnimations().hasZombieWalk) {
			to.getAnimations().hasZombieWalk = from.getAnimations().hasZombieWalk;
		}
		if (from.getAnimations().hasCongaWalk) {
			to.getAnimations().hasCongaWalk = from.getAnimations().hasCongaWalk;
		}
		if (from.getAnimations().hasLovedUpWalk) {
			to.getAnimations().hasLovedUpWalk = from.getAnimations().hasLovedUpWalk;
		}
		if (from.getAnimations().hasSlowmoWalk) {
			to.getAnimations().hasSlowmoWalk = from.getAnimations().hasSlowmoWalk;
		}
		if (from.getAnimations().hasPartyFeverWalk) {
			to.getAnimations().hasPartyFeverWalk = from.getAnimations().hasPartyFeverWalk;
		}
		if (from.getAnimations().hasAssassinWalk) {
			to.getAnimations().hasAssassinWalk = from.getAnimations().hasAssassinWalk;
		}
		if (from.getAnimations().hasTwistedJesterWalk) {
			to.getAnimations().hasTwistedJesterWalk = from.getAnimations().hasTwistedJesterWalk;
		}
		if (from.getAnimations().hasHotSandWalk) {
			to.getAnimations().hasHotSandWalk = from.getAnimations().hasHotSandWalk;
		}
		if (from.getAnimations().hasPlagueDoctorWalk) {
			to.getAnimations().hasPlagueDoctorWalk = from.getAnimations().hasPlagueDoctorWalk;
		}
	}
}