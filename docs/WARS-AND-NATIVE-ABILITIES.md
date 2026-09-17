# War's Retreat and native ability testing

## Try it

1. Log in and use `;;wars` to enter War's Retreat.
2. Use `;;max` if you want to raise your saved skills, and equip matching gear.
3. Use `;;almighty` for the existing session-only unlimited resource modes.
4. Use a dummy dispenser or `;;dummy`. One personal dummy is allowed at a time;
   remove it with `;;removenpc <index>` or `;;clearnpcs` before replacing it.
5. Use `;;testbar` to replace slots 1-3 with Backhand, Binding Shot and Impact.
6. Attack the dummy, then activate the ability matching your equipped weapon.

The supported basics require level 31 Attack, Ranged or Magic respectively.
They use a three-tick global cooldown, a 25-tick individual cooldown and a
five-tick stun. They grant nine adrenaline. Damage uses this server's existing
native max-hit model (20-100% of that model's maximum), NOT retail EOC damage
parity. Supply handling uses the existing native rune/ammunition rules and the
almighty toggles. The dummy cannot retaliate, restores its health, and grants
no XP or loot. Normal supported NPCs retain their existing XP/death paths.

## Scope

The main 14-slot bar and its Powers-window mirror support melee, ranged and
magic ability binding/rearrangement. Bindings persist using 14 named settings
inside the existing save format; old profiles default to an empty bar. The main
bar is initialized unlocked on bar 1. `;;clearbar` clears its bindings.

The smaller HUD ability books and the Powers window have separate widget IDs;
both are handled. Dropping from an unopened book is rejected. Ability-on-NPC
input requires a published, visible NPC and goes through the native combat gates.

Backhand, Binding Shot and Impact execute the limited damage prototype. Surge
now has a targetless, collision-checked movement implementation, without verified
animation/effects. Other abilities may be bound but return an explicit
not-implemented message. Secondary bars, bar
switching, item shortcuts, Revolution, thresholds/ultimates, necromancy,
channels, bleeds, buffs and a complete retail combat model remain unimplemented.
Cooldown enforcement is server-side; retail cooldown overlays are not added.

War's Retreat includes verified safe entry/exit tiles, the War's Insignia entry,
bank chests, prayer altar, adrenaline crystals and dummy dispensers. Exit and
Exit to Draynor both return to Draynor. Death's office, boss portals, portal
configuration, the grimoire and Vorago's encounter are not implemented here.

## Cache Evidence

The read-only `Native950AbilityProbe` derives data from the installed 950 cache.
Region 13214 contains altar114748, crystals114749, bank114750 and dispensers79034.
Script11797 reads type varbit1747 (varp739 bits17-23) and ability varbit1748
(bits4-16), rather than the older four-bit shortcut type. Script6995 maps the
melee/ranged/magic shortcut types1/5/6 to enums10147/6738/6740. Scripts1580 and
7974 resolve slot widgets65/66 +13n in1430 and19/20 +13n in1436. Scripts8423/8426
resolve the source books. These scripts, definitions, interfaces and animations
are SHA-256 pinned in `ability-hub-assets.properties` and verified on admission.

No client executable, rendering settings, game cache or existing saved-character
file was manually edited. This is a limited native ability implementation, not
a claim that all combat content is fixed. In-game drag/drop and rendering still
need a live-client check; automated tests inspect state and packets only.

See `ABILITY-FOUNDATIONS-20260917.md` for the subsequent drag-mask correction,
animation correction, logout candidate fix, seven-definition research and
explicitly incomplete combat/Revolution work.

## Verification (2026-09-17)

- Native950 regression suite: 1,279 tests, zero failures/errors, two skipped.
- Isolated real-cache acceptance passed asset hashes, ability definitions,
  modern shortcut bit slices, Powers binding persistence, closed-book rejection,
  dummy health/no retaliation/no rewards, cooldown expiry and landing collision.
- Pre-edit source backup was verified at GitHub commit `8be3a65`.
- Fresh deployment snapshot: `backups/wars-abilities-deploy-20260917-015925`.
  Jaxa's saved-character hash was unchanged from the pre-task snapshot.
