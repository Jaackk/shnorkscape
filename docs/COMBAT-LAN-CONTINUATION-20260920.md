# Combat and LAN continuation

Protected rollback: `1df667c`. Workspace is closed and unchanged.

## Rex gate: test assumptions and a shared defect

The previous `--bosses` mode used guaranteed-accuracy/maximum-damage rolls for
both sides, a melee weapon, no armour and no supplies. It remains available as
`--boss-stress`; its failure has not been waived or turned into a success.

The inherited `DagannothKing.handleIngoingHit` reduces attacks of the wrong style
(Rex expects Magic). Native combat bypasses this legacy callback, so full king
style restrictions are still not ported. The revised supplied gate uses Magic
against Rex and melee against Barrelchest, seeded normal accuracy/damage rolls,
level99 skills, cache-verified Testing Kit armour, finite runes/sharks and normal
Protect from Melee. It advances fixture-only animation/food/prayer deadlines by
600ms per accelerated tick; no global clock, account, boss stat or damage override.

The armour experiment revealed a shared defect: Native950CombatStyles fell back
to ClassicItemBonusResolver.armourFromTier, whose magic armour has zero crush
defence. That silently discarded native armour. Param2870 is the typed EoC armour
rating: Ataraxia ItemDefinitions.getArmor divides by10, then the existing
Rs2AtaraxiaCacheBonuses adapter divides the rating by10. Undercut's EquipmentBonuses
also sums combatv2_player_armour, mapped to2870 by its tests. The native fallback
now uses that value without calling legacy item-ID overrides. Missing values keep
the previous fallback; malformed/negative present values fail closed. Explicit
classic bonus rows remain unchanged. This is not a complete EoC balance rewrite.

Automated supplied encounter result:

- Rex2883: HP3500 -> death -> owned loot -> corpse removal -> fullHP same-index
  respawn; 215 air runes and11 sharks consumed, player finalHP725.
- Barrelchest5666: HP2500 -> same complete loop; five sharks consumed, finalHP675.
- Combined with ordinary encounters: 1,146 owner-thread ticks, 5,522 parsed
  encrypted950 frames, no transport disconnect.

These are isolated clear-footprint encounters, not proof of boss-area access,
special mechanics or Vulkan presentation. Real client verification remains due.

## LAN reference direction

Darkan WorldLoginDecoder delegates to LobbyCommunicator.authWorldLogin.
Vernox Login.doLogin checks the account's password before admission (its old
SHA1/master-password options are not a design to copy). SHNORKSCAPE needs the
account-bound verification, not unconditional SUCCESS or a shared LAN password.
No listener/firewall/router change has been enabled by this checkpoint.
