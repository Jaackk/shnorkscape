# Combat and support skills: second pass

Reviewed 12 September 2026 against the paired `950RevTest/cache`, the existing native server, the original Ataraxia Slayer/combat structures, and the RuneScape Wiki pages linked below. This pass improves useful progression within the current basic combat engine. It does not implement the full live RuneScape combat or Dungeoneering systems.

## Player-facing changes

* Magic automatically chooses the strongest unlocked standard air spell: Strike at 1, Bolt at 17, Blast at 41, Wave at 62, and Surge at 81. They consume respectively 1, 2, 3, 4, and 5 air runes per basic cast; an air-supplying staff supplies that cost. The earlier RS2 mind-rune requirement and extra cast XP are removed. Damage still awards the configured combat XP. [Standard spellbook](https://runescape.wiki/w/Standard_spellbook), [Air rune](https://runescape.wiki/w/Air_rune).
* Bows and crossbows now use the lower weapon/ammunition damage tier. Changing ammunition changes damage without changing weapon accuracy. A weapon with a distinct cache damage tier uses that tier rather than its equip requirement. This follows the weapon/ammunition relationship, adapted to the server's conservative classic damage formula. [Ranged weapons](https://runescape.wiki/w/Weapon/Ranged_weapons).
* A Slayer master's ordinary conversation now offers task management and rewards. A confirmed cancellation costs 30 points and keeps the task streak. A confirmed 400-point purchase gives 10,000 base Slayer XP at level 35 or higher. Cancelling or opening a confirmation does not itself spend points. The existing account XP rates apply. [Slayer rewards](https://runescape.wiki/w/Slayer_Rewards).
* Slayer points start with the fifth eligible completion; the tenth-task multiplier is five and the fiftieth-task multiplier is fifteen. Turael continues to give neither points nor a streak increment. [Slayer training](https://runescape.wiki/w/Slayer_training).
* The ring, entrance, and tutor dialogue gain a fifth option: a level-20 frozen challenge. It has three waves of three guardians, with later waves appearing after the previous corpses retire. The normal three-guardian run remains available at level 1. The challenge's entry-time reward is 450 base XP at level 20, rising by 10 per Dungeoneering level up to 1,450 at level 120; tokens are one tenth of base XP. This is a custom endurance challenge, not RuneScape's floor/prestige score calculation. The ten-percent token relationship is drawn from [Dungeoneering](https://runescape.wiki/w/Dungeoneering).

## Per-skill review and boundaries

| Skill | Reviewed behavior and result | Remaining scope |
| --- | --- | --- |
| Attack | Existing current-cache weapon requirements, shared accuracy, and selected melee XP remain in use. | Special attacks, EOC accuracy/balance and ability rotations. |
| Strength | Existing melee damage and selected Strength XP remain in use; only committed damage receives XP. | EOC ability damage and weapon special effects. |
| Defence | Armour defence remains applied when using ranged, magic, or necromancy. Existing melee/ranged/magic XP choices remain authoritative. | Retail armour affinity, shield abilities, and a Necromancy/Defence XP choice. |
| Constitution | Existing damage-linked Constitution XP and skill level-up health behavior remain in use. | Exact current retail monster XP tables and EOC damage balance. |
| Ranged | Validated bow/arrow and crossbow/bolt families, one equipped ammo debit per fired attack, equipped requirements, independent ammo damage and weapon accuracy. | Thrown weapons, powered weapons, ammo recovery, special/enchanted bolt effects and rendered projectiles. |
| Magic | Five automatic air spell tiers, correct rune costs, no base cast XP, and damage limited by player level, weapon tier, and selected spell cap. | Manual spell selection, other elements/books, spellbook UI, ancient effects and rendered projectiles. |
| Necromancy | Current-cache Death Guard classification/animation and damage-linked Necromancy plus Constitution XP were verified. The existing basic attack remains rune-free. | Rituals, conjures, incantations, soul/necrosis mechanics, EOC abilities and special attacks. |
| Slayer | Existing weighted task pools are filtered to supported, populated enemies. Level gates, credited kills, counters and save restoration remain; paid cancel and XP rewards are added. | Blocks, preferred/extended tasks, special finishing tools, Turael replacement resets and complete quest-gated master progression. |
| Dungeoneering | Normal and three-wave challenge routes use real guardians and combat death credit; exactly-once exit rewards and owner protection remain. | Generated layouts, parties, bosses, puzzles, binds, prestige, floor unlocks and token shop. |
| Summoning compatibility | Combat changes do not alter pouch/scroll production or award Summoning combat XP. Production is owned by the separate Summoning module. | Familiar summon/despawn, follow, specials and combat participation require a native lifecycle and are deferred. |

The current RuneScape basic attack system includes adrenaline and EOC timing/damage rules; this server deliberately retains its existing cache-driven/classic tick engine for this pass. Therefore the new spell and ammo limits are progression improvements, not an assertion of retail damage parity. [Basic attacks](https://runescape.wiki/w/Basic_attacks).

The original master IDs, task weights, counts, base point rates, and entry requirements remain the authored Ataraxia table. In particular its early Mazchna/Vannaka rates and requirements differ from the current Wiki table. Smoking Kills point-halving/full-rate quest progression is not available in the native saved skill state, so the original full base-rate policy remains. Only the warm-up and milestone multipliers change here. [Slayer masters](https://runescape.wiki/w/Slayer_Masters), [Slayer training](https://runescape.wiki/w/Slayer_training).

Retail Dungeoneering XP depends on floor, prestige, size, complexity and other modifiers. Those data and controls are not simulated by the challenge's simple level-based reward. The existing static frozen rooms are retained because their actual scene areas, collision and exit objects are verified. [Free-to-play Dungeoneering training](https://runescape.wiki/w/Free-to-play_Dungeoneering_training).

## Cache evidence and safety

Item parameter 23 supplies the paired cache's damage tier. Samples include shortbow 841 (tier 5, equip level 1), magic shortbow 861 (tier 75, equip level 80), rune crossbow 9185 (tier 50), and bronze through rune arrows (tiers 1/10/20/30/40/50). Items without that parameter retain a bounded equip-requirement fallback. Weapon struct 686 supplies attack/block sequences and family metadata; animation sequences and equipped items continue to require current-cache validation. Parameter 2940 is only a projectile/ammunition-family presence check, never the ammunition damage value.

The four frozen chambers retain the verified native area 115 mapping for all sixteen chunks. Guardian 88, ring 15707, tutor 9712, entrance 48496, and exit 51156 pass the existing paired-cache identity checks. Wave sizes stay at three; nine unique objective slots identify a challenge. Replayed death callbacks cannot advance an objective twice. No XP or tokens are paid for partial waves, abort, death or logout. Leaving or logging out retires all owned guardians and releases the room.

No central interaction, player, save, session or world classes are changed. Existing entry, item, NPC, tick, death and logout hooks cover the additional dialogue option and wave logic. Slayer uses its existing six saved settings; Dungeoneering uses its existing tokens/completed/interrupted section. The challenge difficulty and reward are snapshotted for the active run; interrupted runs intentionally return outside without rewards on reload.

## Verification

* Java 8 compilation of the changed production classes and acceptance helpers against the current built server and runtime libraries passed.
* Nineteen focused JUnit tests passed, covering previous Slayer behavior, point warm-up/milestones, atomic cancellation/spending, rune level boundaries, damage caps, nine unique kills and capped challenge rewards.
* `Native950CombatStylesAcceptance` passed 24 actual-cache checks, including rune insufficiency/consumption, no mind-rune debit or extra cast XP, high-tier weapon requirements, ammo damage versus accuracy, armour defence and Necromancy XP classification.
* `Native950DungeoneeringRoutingAcceptance` passed the normal run and level-gated challenge through encrypted ring/entrance/exit input and production combat deaths: 220 ticks and 441 decoded encrypted frames. It checks wave replacement, exactly-once rewards, stale exit packets, repeat runs, abort cleanup and area 115 scene mappings.
* `Native950SlayerAcceptance` passed actual-cache master identity, ordinary dialogue, real combat kill/respawn/completion, confirmed cancellation and XP purchase, low-level refusal, replay protection and temporary save/reload: 73 combat ticks, 100 encrypted native frames and three Slayer stat updates.

These isolated probes use disposable players and deterministic combat rolls. They do not authenticate a client or verify rendered pixels and wall-clock combat pacing. The root task owns full build, full regression checks and deployment. Existing files were backed up under `950RevTest/implementation-backup/skills-secondpass/combat` before installation.
