# Basic melee presentation in revision 950

2026-09-10. Isolated950 project only. The server loop supplies ordinary noncritical melee
hits with delay0 after committing the capped HP loss. This adapter does not run legacy
combat or death hooks, and does not enable abilities, special attacks or arbitrary hit types.

## Damage units and recipient selection

Player.refreshHitPoints sends engineHP*10 to950varp13537. Script16856 defines maximum life
points as Constitution*100, while the engine stores Constitution*10. See hitpoints-950.md.
Native formatter0x14033E1B0 multiplies wire damage by definition numerator and divides by
its denominator (0x14033E1FF/0x14033E218). Therefore the ordinary 1:1 definition requires
wireDamage=engineDamage*10. The previous untyped damage/10 fallback was inconsistent.

Native950Hits now snapshots each queued hit's integer damage and participant indexes before
player frames. Actor MaskSource has a compatible recipient-index overload; appendBlock calls
it with the actual viewer index. Source and victim see definition0; other viewers see14.
These are the standard selector0 targets of950wrappers133 and150, independently confirmed
in hitbars-hitmarks-950-evidence.json. Direct definitions deliberately select one standard
style; they do not follow an unverified client preference or replay the old HitLook132,
which has become a different message in950. Custom hitstyle preferences are outside scope.

Zero damage uses direct definitions458 for the source/victim and464 for other viewers.
Both contain literal text `0` and the paired bright/muted blue sprite sets23354..23359.
These are the numeric-zero targets of wrappers482/492, not healing or an unrelated effect.
Miss wrappers141/158 used in the first live build resolve to **Dodged** in both of their
available modes, so changing their selector cannot produce the requested zero. See
[the numeric-zero cache and sprite proof](numeric-zero-hitmarks-950.md).

The selected direct definitions have50 native cycles of display lifetime and no morph
selector. Runtime pins cover full logical files in config index2/group46 and the six small
index8 blue sprite files. The helper verifies the running Cache.STORE, caches success by
store identity and refuses nonempty queues without that verification. Positive damage
remains definitions0/14 with engineHP*10; zero styling does not alter HP or client settings.

Only MELEE_DAMAGE or explicit zero MISSED is admitted. Critical, special, soaked, nonmelee,
negative, delayed and over-width entries are counted and refused. Positive legacy Hit.delay
semantics remain unported; delay0 avoids an invented clock conversion. Multiplication uses
long and never clamps a false displayed number: maximum accepted engine damage3276 becomes
32760 native LP;3277 is refused. The list is bounded to255. The engine queue is not cleared
or rewritten by the adapter. NPC frames use the same units and per-viewer relation.

Literal examples for50 engineHP loss,500 nativeLP (smart0x81f4), no delay or bar:

- Player involved: `40 81 00 81 f4 00 00`; observer: `40 81 0e 81 f4 00 00`.
- NPC involved: `00 00 20 ff 00 81 f4 00 00`; observer substitutes type0e.
- Player zero involved: `40 81 81 ca 00 00 00`; observer: `40 81 81 d0 00 00 00`.
- NPC zero involved: `00 00 20 ff 81 ca 00 00 00`; observer substitutes `81 d0` for `81 ca`.

Player count is +128; NPC count is negated. Typed damage is smart, not the old untyped byte.
Native player reader0x14012D140..0x14012D844 and NPC reader0x140123836 feed the shared hit
sink0x14037C0D0. Existing standard EntityHitBar mapping remains0..255 and uses separate
verified definitions0/3/4; damage units never alter percentage ratios.

## Animation and weapon catalog

Native950CombatAnimations pins12 selected950SeqTypes, the3 weapon definitions, and their
3 combat-map definitions. The pure metadata API accepts only empty mainhand, bronze sword1277,
bronze longsword1291 and bronze scimitar1321; unsupported weapons throw rather than guessing.
Live combat must check verifyCache's boolean result, including permissive research mode.

Actual950weapon params select combat maps14922/14923/14924 (itemparam3000, fallback686).
Mapparam2914 changed from910attack18226 to95037378 for sword, and from18241 to37385 for
longsword/scimitar. Mapparam2917 remains block18292. The catalog uses the actual950sequence
IDs, including its new sequence representation, rather than carrying old IDs forward.
Mapparam2853 gives stab5 for sword, slash6 for longsword/scimitar. The explicit unarmed
RS2 presentation uses punch422, block425, crush7. Legacy player death is836.

Cadence is an explicit RS2 gameplay policy in600ms server ticks: unarmed4, sword4,
longsword5, scimitar4. These values are not described as byte-identical cache metadata;
the modern item attack-speed values differ. No offhand or weapon ability mapping is implied.

Reproduce the cache survey with:

`python -B tools/verify_950_melee_animations.py protocol-analysis/melee-animation-950-evidence.json`

The tool opens the packed910cache strictly read-only and fully consumes each selected
sequence record. Every legacy-supported selection retains the same opcode1 frame IDs and
durations in950. Full chicken5387/5388/5389, block425 and death836definitions are identical.
Punch422changes priority/interruption fields, goblin death6182adds sounds, block18292adds
priority; these are retained from950and their complete definitions are pinned. New weapon
sequences37378/37385do not exist in910and are selected from actual950combat maps.

The NXT flat cache lacks legacy asset indexes0and7, so equal sequence references are not
proof of unchanged rendered frame/model dependencies. NPC model and menu admission is a
separate catalog check. Live animation checks are still required; this evidence establishes
correct950metadata selection, not an assertion of pixel-identical motion.

Chicken death5389is153 native20ms cycles (3.06seconds): keep its corpse visible at least6
world ticks. Goblin death6182is90 cycles (1.8seconds): at least3 ticks. Player836totals5119
cycles because it contains a5000-cycle final-pose hold; player recovery must not blindly
wait that raw sum. Catalog durationCycles exposes the raw verified totals without silently
inventing a replacement duration.

## Verification scope

Native950MeleeRenderingTest has independent literal player/NPC hit fixtures, source/victim/
observer selection, per-hit source differences, snapshot immutability, refusal boundaries,
list limits, the actual PlayerInfo recipient seam and selected catalog metadata. Existing
cache-free source tests now require refusal rather than expecting the incorrect untyped
fallback. Real-cache combat acceptance and live checks belong to the integrated milestone.
No build, restart or UI action was performed by this subtask.
