package com.rs.game.npc.combat;

import lombok.Getter;
import lombok.Setter;

public class NPCCombatDefinition {

	@Getter @Setter
	private int hitpoints;
	@Getter
	private final int attackAnim;
	@Getter
	private final int defenceAnim;
	@Getter
	private final int deathAnim;
	@Getter @Setter
	private int attackDelay;
	@Getter
	private final int deathDelay;
	@Getter @Setter
	private int respawnDelay;
	@Getter
	private final int maxHit;
	public final String attackStyle;
	@Getter
	private final int attackGfx;
	@Getter
	private final int attackProjectile;
	private final String aggressivenessType;

	public NPCCombatDefinition(int hitpoints, int attackAnim, int defenceAnim, int deathAnim, int attackDelay,
							   int deathDelay, int respawnDelay, int maxHit, String attackStyle, int attackGfx, int attackProjectile,
							   String aggressivenessType) {
		this.hitpoints = hitpoints;
		this.attackAnim = attackAnim;
		this.defenceAnim = defenceAnim;
		this.deathAnim = deathAnim;
		this.attackDelay = attackDelay;
		this.deathDelay = deathDelay;
		this.respawnDelay = respawnDelay;
		this.maxHit = maxHit;
		this.attackStyle = attackStyle;
		this.attackGfx = attackGfx;
		this.attackProjectile = attackProjectile;
		this.aggressivenessType = aggressivenessType;
	}

	public int getAttackStyle() {
		if (attackStyle.equalsIgnoreCase("range")) {
			return NPCCombatDefinitionConstants.RANGE;
		} else if (attackStyle.equalsIgnoreCase("mage")) {
			return NPCCombatDefinitionConstants.MAGE;
		} else if (attackStyle.equalsIgnoreCase("special")) {
			return NPCCombatDefinitionConstants.SPECIAL;
		} else if (attackStyle.equalsIgnoreCase("special2")) {
			return NPCCombatDefinitionConstants.SPECIAL2;
		}
		return NPCCombatDefinitionConstants.MELEE;
	}

	public int getAggressivenessType() {
		if (aggressivenessType.equalsIgnoreCase("agressive") || aggressivenessType.equalsIgnoreCase("aggressive")) {
			return NPCCombatDefinitionConstants.AGRESSIVE;
		}
		return NPCCombatDefinitionConstants.PASSIVE;
	}

	public int getAttackEmote() {
		return attackAnim;
	}

	public int getDefenceEmote() {
		return defenceAnim;
	}

	public int getDeathEmote() {
		return deathAnim;
	}
}
