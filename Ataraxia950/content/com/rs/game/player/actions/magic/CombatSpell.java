package com.rs.game.player.actions.magic;

public enum CombatSpell {

//	WIND_RUSH(98, 14221, null, new Graphics(2700, 0, 96), 1.2, 10, 220, 217, Combat.TYPE_AIR, 2699),
//	WIND_STRIKE(25, 14221, null, new Graphics(2700, 0, 96), 5.5, 16, 220, 221, Combat.TYPE_AIR, 2699),
//	CONFUSE(26, 26, 716, new Graphics(102, 0, 80), new Graphics(104), 13, 0, -1, 152, Combat.TYPE_AIR, new NewProjectile(null, null, 103, 40, 20, 65), new SpellEffect() {
//		@Override
//		public void spellEffect(Player player, Entity target, int damage) {
//			if (damage == 0)
//				return;
//	         target.getTemporaryModifiersManager().applyModifier(Key.HIT_CHANCE_MODIFIER, 60000, -5);
//		}
//	}),
//	WATER_STRIKE(28, 27, 14221, new Graphics(2701), new Graphics(2708, 0, 96), 7.5, 22, 211, 212, Combat.TYPE_WATER, 2703),
//	EARTH_STRIKE(30, 28, 14221, new Graphics(2713), new Graphics(2723, 0, 96), 9.5, 28, 132, 133, Combat.TYPE_EARTH, 2718),
//	WEAKEN(31, 29, 716, new Graphics(105, 0, 80), new Graphics(107), 16, 0, -1, 152, Combat.TYPE_AIR, new NewProjectile(null, null, 106, 40, 20, 65), new SpellEffect() {
//		@Override
//		public void spellEffect(Player player, Entity target, int damage) {
//			if (damage == 0)
//				return;
//			target.getTemporaryModifiersManager().applyModifier(Key.DAMAGE_DEALT_MODIFIER, 60000, -0.05);
//		}
//	}),
//	FIRE_STRIKE(32, 30, 14221, new Graphics(2728), new Graphics(2737, 0, 96), 11.5, 34, 160, 161, Combat.TYPE_FIRE, 2729),
//	WIND_BOLT(34, 32, 14220, null, new Graphics(2700, 0, 96), 13.5, 40, 218, 219, Combat.TYPE_AIR, 2699),
//	CURSE(35, 33, 716, new Graphics(108, 0, 80), new Graphics(110), 19, 0, -1, 152, Combat.TYPE_AIR, new NewProjectile(null, null, 109, 40, 20, 65), new SpellEffect() {
//		@Override
//		public void spellEffect(Player player, Entity target, int damage) {
//			if (damage == 0)
//				return;
//            target.getTemporaryModifiersManager().applyModifier(Key.DAMAGE_RECIEVED_MODIFIER, 60000, 0.05);
//		}
//	}),
//	BIND(36, 34, 710, new Graphics(177, 0, 100), new Graphics(181), 60.5, 0, 101, 99, Combat.TYPE_AIR, 178, new SpellEffect() {
//		@Override
//		public void spellEffect(Player player, Entity target, int damage) {
//			if (damage > 0)
//				target.addFreezeDelay(5000, true);
//		}
//	}),
//	WATER_BOLT(39, 36, 14220, new Graphics(2707, 0, 100), new Graphics(2709, 0, 96), 16.5, 45, 209, 210, Combat.TYPE_WATER, 2704),
//	EARTH_BOLT(42, 37, 14222, new Graphics(2714), new Graphics(2724, 0, 96), 19.5, 52, 130, 131, Combat.TYPE_EARTH, 2719),
//	FIRE_BOLT(45, 41, 14223, new Graphics(2728), new Graphics(2738, 0, 96), 22.5, 58, 157, 158, Combat.TYPE_FIRE, 2731),
//	CRUMBLE_UNDEAD(47, 724, new Graphics(145, 0, 100), new Graphics(147, 0, 96), 24, 59, 122, 124, Combat.TYPE_AIR, 146),
//	WIND_BLAST(49, 42, 14221, new Graphics(2699), new Graphics(2700, 0, 96), 25.5, 61, 216, 217, Combat.TYPE_AIR, 2699),
//	WATER_BLAST(52, 43, 14220, new Graphics(2701), new Graphics(2710, 0, 96), 31.5, 64, 207, 208, Combat.TYPE_WATER, 2705),
//	IBANS_BLAST(54, 708, null, new Graphics(89, 0, 96), 45, 65, -1, -1, Combat.TYPE_FIRE, new NewProjectile(null, null, 88, 40, 32, 35, 0, 35, 0)),
//	SNARE(55, 44, 710, new Graphics(177, 0, 100), new Graphics(180, 0, 96), 91.1, 0, -1, 152, Combat.TYPE_AIR, 178, new SpellEffect() {
//		@Override
//		public void spellEffect(Player player, Entity target, int damage) {
//			if (damage > 0)
//				target.addFreezeDelay(10000, true);
//		}
//	}),
//	MAGIC_DART(56, 1575, null, new Graphics(329, 0, 96), 60, 65, -1, -1, Combat.TYPE_AIR, new NewProjectile(null, null, 328, 40, 32, 35, 0, 35, 0)),
//	EARTH_BLAST(58, 45, 14222, new Graphics(2715), new Graphics(2725, 0, 96), 31.5, 69, 128, 129, Combat.TYPE_EARTH, 2720),
//	FIRE_BLAST(63, 47, 14223, new Graphics(2728), new Graphics(2739, 0, 96), 34.5, 74, 155, 156, Combat.TYPE_FIRE, 2733),
//	SARADOMIN_STRIKE(66, 811, null, new Graphics(76, 0, 96), 34.5, 76, 1656, -1, Combat.TYPE_AIR, null, new SpellEffect() {
//		@Override
//		public void spellEffect(Player player, Entity target, int damage) {
//			if (damage > 0 && target instanceof Player)
//				((Player) target).getPrayer().drainPrayer(10);
//		}
//	}),
//	CLAWS_OF_GUTHIX(67, 811, null, new Graphics(77, 0, 96), 34.5, 76, 1653, -1, Combat.TYPE_EARTH, null, new SpellEffect() {
//		@Override
//		public void spellEffect(Player player, Entity target, int damage) {
//			if (damage == 0)
//				return;
//			if (Math.random() > 0.2)
//			    return;
//			if (target instanceof NPC) {
//				final NPC npc = (NPC) target;
//				npc.getStats().setDefenceLevel((int) ((double)npc.getStats().getDefenceLevel() * 0.95));
//			} else if (target instanceof Player) {
//				final Player p2 = (Player) target;
//				if (p2.getSkills().getLevel(Skills.DEFENCE) > Math.ceil(p2.getSkills().getLevelForXp(Skills.DEFENCE) * 0.95)) {
//					final int maxDrain = p2.getSkills().getLevel(Skills.DEFENCE) - (int) Math.ceil(p2.getSkills().getLevelForXp(Skills.DEFENCE) * 0.95);
//					final int drain = (int) (p2.getSkills().getLevel(Skills.DEFENCE) - (p2.getSkills().getLevel(Skills.DEFENCE) * 0.95));
//					final int realDrain = drain > maxDrain ? maxDrain : drain;
//					if (realDrain > 0)
//						p2.getSkills().drainLevel(Skills.DEFENCE, realDrain);
//				}
//			}
//		}
//	}),
//	FLAMES_OF_ZAMORAK(68, 811, null, new Graphics(78), 34.5, 76, 1655, -1, Combat.TYPE_FIRE, null, new SpellEffect() {
//		@Override
//		public void spellEffect(Player player, Entity target, int damage) {
//			if (damage == 0)
//				return;
//			if (target instanceof NPC) {
//				final NPC npc = (NPC) target;
//                npc.getStats().setMagicLevel(npc.getStats().getMagicLevel() - 5 <= 1 ? 1 : npc.getStats().getMagicLevel() - 5);
//			} else if (target instanceof Player) {
//				final Player p2 = (Player) target;
//				if (p2.getSkills().getLevel(Skills.MAGIC) > Math.ceil(p2.getSkills().getLevelForXp(Skills.MAGIC) * 0.95)) {
//					final int maxDrain = p2.getSkills().getLevel(Skills.MAGIC) - (int) Math.ceil(p2.getSkills().getLevelForXp(Skills.MAGIC) * 0.95);
//					final int drain = (int) (p2.getSkills().getLevel(Skills.MAGIC) - (p2.getSkills().getLevel(Skills.MAGIC) * 0.95));
//					final int realDrain = drain > maxDrain ? maxDrain : drain;
//					if (realDrain > 0)
//						p2.getSkills().drainLevel(Skills.MAGIC, realDrain);
//				}
//			}
//		}
//	}),
//	AIR_WAVE(70, 48, 14221, new Graphics(2699), new Graphics(2700, 0, 96), 36, 80, 222, 223, Combat.TYPE_AIR, 2699),
//	WATER_WAVE(73, 49, 14220, new Graphics(2702), new Graphics(2710, 0, 96), 37.5, 80, 213, 214, Combat.TYPE_WATER, 2706),
//	VULNERABILITY(75, 50, 716, new Graphics(167, 0, 60), new Graphics(169), 68, 0, -1, 152, Combat.TYPE_AIR, new NewProjectile(null, null, 168, 40, 20, 65), new SpellEffect() {
//		@Override
//		public void spellEffect(Player player, Entity target, int damage) {
//			if (damage == 0)
//				return;
//            target.getTemporaryModifiersManager().applyModifier(Key.DAMAGE_RECIEVED_MODIFIER, 60000, 0.10);
//		}
//	}),
//	EARTH_WAVE(77, 54, 14222, new Graphics(2716), new Graphics(2726, 0, 96), 42.5, 80, 134, 135, Combat.TYPE_EARTH, 2721),
//	ENFEEBLE(78, 56, 716, new Graphics(170), new Graphics(172, 0, 96), 81, 0, -1, 152, Combat.TYPE_AIR, new NewProjectile(null, null, 171, 40, 20, 65), new SpellEffect() {
//		@Override
//		public void spellEffect(Player player, Entity target, int damage) {
//			if (damage == 0)
//				return;
//            target.getTemporaryModifiersManager().applyModifier(Key.DAMAGE_DEALT_MODIFIER, 60000, -0.1);
//		}
//	}),
//	FIRE_WAVE(80, 58, 14223, new Graphics(2728), new Graphics(2740, 0, 96), 42.5, 80, 162, 163, Combat.TYPE_FIRE, 2735),
//	STORM_OF_ARMADYL(99, 10546, new Graphics(457), new Graphics(1019), 70, 85, -1, -1, Combat.TYPE_AIR, null),
//	ENTANGLE(81, 59,  710, new Graphics(177, 0, 100), new Graphics(179, 0, 96), 91.1, 0, -1, 152, Combat.TYPE_AIR, 178, new SpellEffect() {
//		@Override
//		public void spellEffect(Player player, Entity target, int damage) {
//			if (damage > 0)
//				target.addFreezeDelay(15000, true);
//		}
//	}),
//	STUN(82, 60, 710, new Graphics(998, 0, 45), new Graphics(80, 0, 96), 95, 0, -1, 152, Combat.TYPE_AIR, new NewProjectile(null, null, 174, 18, 18, 65, 0, 50, 0), new SpellEffect() {
//		@Override
//		public void spellEffect(Player player, Entity target, int damage) {
//			if (damage == 0)
//				return;
//            target.getTemporaryModifiersManager().applyModifier(Key.HIT_CHANCE_MODIFIER, 60000, -10);
//		}
//	}),
//	WIND_SURGE(84, 61, 10546, new Graphics(457), new Graphics(2700, 0, 96), 80, 92, 222, 223, Combat.TYPE_AIR, 462),
//	TELEBLOCK(86, 10503, new Graphics(1841), new Graphics(1843), 80, 0, 202, 203, Combat.TYPE_AIR, 1842, new SpellEffect() {
//		@Override
//		public void spellEffect(Player player, Entity target, int damage) {
//			if (damage > 0 && target instanceof Player) {
//				final Player p2 = (Player) target;
//				final boolean halved = p2.getPrayer().isMageProtecting();
//				p2.sendMessage(Colors.LPURPLE + "A teleblock has been cast on you! It will wear off in about " + (halved ? "2 minutes and 30 seconds." : "5 minutes."));
//				p2.setTeleBlockDelay(halved ? 150000 : 300000);
//			}
//		}
//	}),
//	WATER_SURGE(87, 62, 10542, new Graphics(2701), new Graphics(2712, 0, 96), 80, 92, 213, 214, Combat.TYPE_WATER, 2707),
//	EARTH_SURGE(89, 63, 14209, new Graphics(2717), new Graphics(2727, 0, 96), 80, 92, 134, 135, Combat.TYPE_EARTH, 2722),
//	FIRE_SURGE(91, 67, 2791, new Graphics(2728), new Graphics(2741, 0, 96), 80, 95, 162, 163, Combat.TYPE_FIRE, 2735),
//	SMOKE_RUSH(28, 1978, null, new Graphics(385), 30, 60, 176, 177, Combat.TYPE_AIR, 386),
//	SHADOW_RUSH(32, 1978, null, new Graphics(379), 31, 63, 175, 177, Combat.TYPE_EARTH, 378),
//	BLOOD_RUSH(24, 1978, null, new Graphics(373), 33, 67, -1, 174, Combat.TYPE_FIRE, null),
//	ICE_RUSH(20, 1978, null, new Graphics(361), 34, 69, -1, 173, Combat.TYPE_WATER, 360),
//	MIASMIC_RUSH(36, 10513, new Graphics(1845), new Graphics(1847), 35, 70, -1, -1, Combat.TYPE_FIRE, null),
//	SMOKE_BURST(30, 1979, null, new Graphics(389), 36, 73, 179, 180, Combat.TYPE_AIR, null),
//	SHADOW_BURST(34, 1979, null, new Graphics(382), 37, 75, 178, -1, Combat.TYPE_EARTH, null),
//	BLOOD_BURST(26, 1979, null, new Graphics(376), 39, 79, 103, -1, Combat.TYPE_FIRE, null),
//	ICE_BURST(22, 1979, null, new Graphics(363), 46, 80, 171, 169, Combat.TYPE_WATER, null),
//	MIASMIC_BURST(38, 10516, new Graphics(1848), new Graphics(1849), 42, 80, -1, -1, Combat.TYPE_FIRE, null),
//	SMOKE_BLITZ(29, 1978, null, new Graphics(387), 42, 80, 183, 184, Combat.TYPE_AIR, 386),
//	SHADOW_BLITZ(33, 1978, null, new Graphics(381), 43, 80, 175, 180, Combat.TYPE_EARTH, 380),
//	BLOOD_BLITZ(25, 1978, null, new Graphics(375), 45, 92, 108, 105, Combat.TYPE_FIRE, 374),
//	ICE_BLITZ(21, 1978, new Graphics(366), new Graphics(366), 46, 92, 171, 169, Combat.TYPE_WATER, null),
//	MIASMIC_BLITZ(37, 10524, new Graphics(1850), new Graphics(1851), 48, 92, -1, -1, Combat.TYPE_FIRE, null),
//	SMOKE_BARRAGE(31, 1979, null, new Graphics(391), 48, 92, 183, 184, Combat.TYPE_AIR, null),
//	SHADOW_BARRAGE(35, 1979, null, new Graphics(383), 49, 92, 175, 178, Combat.TYPE_EARTH, null),
//	BLOOD_BARRAGE(27, 1979, null, new Graphics(377), 51, 92, 108, 107, Combat.TYPE_FIRE, null),
//	ICE_BARRAGE(23, 1979, null, new Graphics(369), 52, 94, 171, 168, Combat.TYPE_WATER, null),
//	MIASMIC_BARRAGE(39, 10518, new Graphics(1853), new Graphics(1854), 54, 97, -1, -1, Combat.TYPE_FIRE, null);
//	
//	private final int id, daemonheimId, sound, scaleLevel, type, hitSound;
//	private final NewProjectile projectile;
//	private final double experience;
//	private final Graphics graphics, hitGraphics;
//	private final Animation animation;
//	private final SpellEffect effect;
//	
//	private CombatSpell(final int id, final int anim, final Graphics graphics, final Graphics hitGraphics, final double experience, 
//			final int scaleLevel, final int sound, final int hitSound, final int type, final int projectileId, final SpellEffect effect) {
//		this(id, anim, graphics, hitGraphics, experience, scaleLevel, sound, hitSound, type, new NewProjectile(null, null, projectileId, 18, 18, 50, 0, 50, 0), effect);
//	}
//	
//	private CombatSpell(final int id, final int anim, final Graphics graphics, final Graphics hitGraphics, final double experience, 
//			final int scaleLevel, final int sound, final int hitSound, final int type, final int projectileId) {
//		this(id, anim, graphics, hitGraphics, experience, scaleLevel, sound, hitSound, type, new NewProjectile(null, null, projectileId, 18, 18, 50, 0, 50, 0), null);
//	}
//	
//	private CombatSpell(final int id, final int anim, final Graphics graphics, final Graphics hitGraphics, final double experience, 
//			final int scaleLevel, final int sound, final int hitSound, final int type, final NewProjectile projectile) {
//		this(id, anim, graphics, hitGraphics, experience, scaleLevel, sound, hitSound, type, projectile, null);
//	}
//
//	private CombatSpell(final int id, final int daemonheimId, final int anim, final Graphics graphics, final Graphics hitGraphics, final double experience, 
//			final int scaleLevel, final int sound, final int hitSound, final int type, final int projectileId, final SpellEffect effect) {
//		this(id, daemonheimId, anim, graphics, hitGraphics, experience, scaleLevel, sound, hitSound, type, new NewProjectile(null, null, projectileId, 18, 18, 50, 0, 50, 0), effect);
//	}
//	
//	private CombatSpell(final int id, final int daemonheimId, final int anim, final Graphics graphics, final Graphics hitGraphics, final double experience, 
//			final int scaleLevel, final int sound, final int hitSound, final int type, final int projectileId) {
//		this(id, daemonheimId, anim, graphics, hitGraphics, experience, scaleLevel, sound, hitSound, type, new NewProjectile(null, null, projectileId, 18, 18, 50, 0, 50, 0), null);
//	}
//	
//	private CombatSpell(final int id, final int anim, final Graphics graphics, final Graphics hitGraphics, final double experience, 
//			final int scaleLevel, final int sound, final int hitSound, final int type, final NewProjectile projectile, final SpellEffect effect) {
//		this(id, -1, anim, graphics, hitGraphics, experience, scaleLevel, sound, hitSound, type, projectile, effect);
//	}
//	
//	private CombatSpell(final int id, final int daemonheimId, final int anim, final Graphics graphics, final Graphics hitGraphics, final double experience, 
//			final int scaleLevel, final int sound, final int hitSound, final int type, final NewProjectile projectile, final SpellEffect effect) {
//		this.id = id;
//		this.daemonheimId = daemonheimId;
//		this.animation = new Animation(anim);
//		this.graphics = graphics;
//		this.hitGraphics = hitGraphics;
//		this.experience = experience;
//		this.scaleLevel = scaleLevel;
//		this.sound = sound;
//		this.hitSound = hitSound;
//		this.type = type;
//		this.projectile = projectile;
//		this.effect = effect;
//	}
//	
//	public final Animation getAnimation() {
//		return animation;
//	}
//	
//	public final Graphics getGraphics() {
//		return graphics;
//	}
//	
//	public final Graphics getHitGraphics() {
//		return hitGraphics;
//	}
//	
//	public final double getBaseExperience() {
//		return experience;
//	}
//	
//	public final int getScaleLevel() {
//		return scaleLevel;
//	}
//	
//	public final int getSound() {
//		return sound;
//	}
//	
//	public final int getHitSound() {
//		return hitSound;
//	}
//	
//	public final int getType() {
//		return type;
//	}
//	
//	public final NewProjectile getProjectile() {
//		return projectile;
//	}
//	
//	public final SpellEffect getEffect() {
//		return effect;
//	}
//	
//	public final int getId() {
//		return id;
//	}
//	
//	public final int getDaemonheimId() {
//		return daemonheimId;
//	}
}