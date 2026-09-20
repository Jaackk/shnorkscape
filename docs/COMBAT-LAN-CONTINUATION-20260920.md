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

## Shared NPC ranged/magic checkpoint

The catalog now admits 1,867 authored profiles: 1,500 melee, 143 ranged and
224 magic. The earlier 633-style refusal included special attacks and other
invalid rows; it was not 633 safe ordinary ranged/magic implementations.
232 SPECIAL/SPECIAL2 rows still require bespoke mechanics, and two rows have
unverified effect identities. Attack menu, identity and metadata checks remain.

Ataraxia Default/NPCCombat supplies the ordinary non-melee lifecycle: seven-tile
projectile-clipped reach, style-specific levels, optional caster graphic and
projectile, delayed styled hit. Cache accuracy is param29/melee, 4/ranged and
3/magic; it is not a shared melee rating. Effects require paired-cache identity
verification. Native opcode154 publishes NPC-to-player projectiles; delayed
damage follows the same flight duration. Explicit absent effects stay absent.
Stop/death clears outstanding strikes, and block animation occurs on impact.

Focused tests cover catalog style/effect/accuracy selection, range/LOS, delayed
hit marks, projectile endpoints/timing and stop cancellation. Full test/jar and
the supplied Rex/Barrelchest acceptance run pass. Vulkan presentation is pending.

## LAN reference direction (continued)

## Native ability membership

The expanded offline inventory uses the exact950 script6995 dispatcher and
8247 conditional transformer, pinned respectively to
`b371951bd526283bf7d8560faa153a032239bd3db069a47fecbf71bd45e9e274` and
`0fb25f13c1de3cb06d670cc25c20c751cfffc7e03e07a94b8c49631f9dc63b56`.
The repository's existing independently derived opcode map decodes their full
instruction boundaries/counts. Native book enums are10147/6738/6740/6736/6737/
16973 (melee/ranged/magic/defence/constitution/necromancy).

Of171 ability-shaped structures,120 are native book entries,20 additional
structures occur in the conditional native transformer, and31 are not reached
by these reviewed paths. The latter are NOT automatically declared obsolete;
other surfaces could still reference them. The canonical visible set is
player-dependent (unlocks, weapon modes, recast state), not171 independent buttons.
Current execution coverage remains31 partial,140 missing. The JSON now retains
typed parameters and visibility classifications for systematic implementation.

Undercut AbilityBooks/AbilityTransform supplies the architectural comparison;
950's8247 also substitutes modern melee and upgraded abilities beyond that
reference. Surge/Escape/Dive have no direct2914/2915 animation binding in their
950 structures. Existing910 ActionBar provides18358/18527 and3537/3526 as
historical candidates, not authorization to publish unverified950 effects.
Movement completion and live presentation therefore remain open; no generic
animation was inserted to inflate coverage.

Darkan WorldLoginDecoder delegates to LobbyCommunicator.authWorldLogin.
Vernox Login.doLogin checks the account's password before admission (its old
SHA1/master-password options are not a design to copy). SHNORKSCAPE needs the
account-bound verification, not unconditional SUCCESS or a shared LAN password.
No listener/firewall/router change has been enabled by this checkpoint.

## Optional LAN candidate

The opt-in LAN candidate now verifies individually provisioned guest passwords
with salted PBKDF2-SHA256, bounded attempts and reserved-account rejection.
Existing host-only reconnect recall was unsafe for two clients on one address:
remote handoff now requires authenticated prior session keys, matching peer,
expiry and single consumption. It never falls back to address-only identity.
Local admission and normal Play.cmd remain unchanged. Remote OpenNXT developer
permissions are denied; native engine rights remain separately guarded.

The complete OpenNXT build, candidate override build, credential tests and
handoff acceptance tests pass. Overrides are tested ahead of the candidate JAR,
matching production classpath precedence. PowerShell scripts parse cleanly.
The guest ZIP contains only the existing hash-pinned non-diagnostic client,
launch scripts and instructions, not accounts, credentials or server content.

LAN is OFF by default. No credentials, listener, firewall rule or router change
was created during build/package validation. Real second-machine lobby/world
handoff still requires live verification; token-phase incompatibility must fail
closed, never reintroduce IP-only recall. See LAN-GUEST-CANDIDATE.md. Combat
visual confirmation and movement-ability completion remain separate gates.

## Native ranged auto presentation

Ordinary ranged autos now resolve projectile2940 from the exact950 weapon or
equipped ammunition before consuming the final item. Ataraxia PlayerCombat's
arrow/bolt/thrown launch timings feed the native world projectile publisher;
damage is scheduled against the absolute end cycle, not applied on launch.
The exact-cache Shortbow841/Bronze arrow882 witness verifies projectile10,
one-arrow depletion, no premature damage, and a range hit at end cycle50.

Native item references use nonempty definitions in the pinned950 cache, not the
910/950 identical-effect list: projectile10 legitimately differs from910.
The existing legacy graphic safety boundary is not relaxed. Missing native
projectile evidence remains absent rather than substituted with a generic arrow.
Delayed player-hit block animations now occur at impact. The last thrown item
does not cancel its own launched hit; explicit stop/logout still cancels it.

Ability animation resolution also accepts typed flat2914 bindings, with weapon
family enum/default precedence as in Undercut. Absent/mistyped parameters do not
silently become sequence0. No missing ability is marked implemented by this.
Rex/Barrelchest gates still pass:1149 ticks/5538 parsed frames including the new
arrow witness. Actual Vulkan projectile visibility remains a live test.

## Local deployment, 20 September 2026

After Jack closed the client, the old server confirmed player removal. Backup:
`backups/pre-edit-20260920-121933-255` (includes both runtime JARs, overrides,
profiles, workspace state and previous server logs). No client was launched by
the agent. All seven saved profile/workspace files remain byte-identical after
deployment. The original workspace flags are preserved and LAN remains OFF.

The first readiness check caught a Kotlin override ABI mismatch: compiling the
OpenNXT override under module `opennxt950` renamed internal methods referenced by
the full JAR's `OpenNXT` module. No player logged in during that failed check.
Build-950Lobby now uses the same `OpenNXT` module name. The new
NativeOverrideLinkageAcceptance runs with overrides first and verifies both the
internal offline method and its synthetic default bridge. This is a build fix,
not a login protocol change.

Final runtime PID6716, loopback-only80/8950/43650. Normal Play.cmd jav_config is
HTTP200. Live JS5-over-HTTP index255/group12 returns322443 bytes on both HTTP
ports. Verify950Kt passes;5145 JS5 payload checks pass; remote handoff isolation
passes on the deployed classpath. The combat candidate's1437 tests had zero
failures/errors and two skips. Vulkan combat presentation still requires Jack.

Deployed engine SHA256:
`b1c85a08f48bc3b38b1cb50edefda6a93213789176f62702476f8d05c5f00d85`

Deployed OpenNXT SHA256:
`e557c6e2d6b1433a891d72863a9427f779794ce4874b2086c05c91438ed8f4f4`

Live checklist: normal Play.cmd login; attack an ordinary combat NPC using
melee, a bow with compatible ammo, and a staff with air runes; observe sustained
targeting, launch/cast, projectile arrival and hits. Then test Rex using Magic
and supplies, including death/drop/respawn. Automated boss witnesses are not
proof of rendered behaviour, area access or complete bespoke boss mechanics.
