# Combat live refinement ? 23 September 2026

This candidate addresses the 18:41?18:50 Bug Test. It is staged offline: the running server, client, cache and production characters were not replaced or restarted. Apply it using `Apply Staged Update.cmd` after closing clients and stopping the server yourself.

## Live baseline preserved

Dive chosen-tile movement, travelling/bouncing Death Skulls, the `;;queuehold` native ring, conjure target switching and bank/items context-24 keybind recovery are LIVE PASS. Revolution is functioning live, with polish still partial. Ordinary queue presentation, Finger consumption, world souls and the new buff panels still require the successor live check. No packet test is labelled Vulkan acceptance.

The session contained four Dive arrivals immediately followed by army dismissal, 277 conjure attacks with unresolved actor animation, working soul resource changes and 13 failed screenshots. The screenshot helper selected the old client process name; it now also recognises the current workspace diagnostic Vulkan executable.

## Changes and exact evidence

**Forced movement and ownership.** `Entity` now retains the relocation reason independently of network mask generations. The transport's `teleported` flag remains true until the next movement tick; clearing masks must not turn a forced arrival into a lifecycle teleport. Real `setNextWorldTile` transitions still invalidate ownership. The real-cache world test covers Dive, Surge, Escape, the post-arrival tick, ordinary walking, four-companion recovery and genuine teleport cleanup. This does not alter the working Dive targeting or interpolation protocol.

**Formation.** The four native companions choose distinct clear footprints in compact rings around the owner. A fully blocked search fails without substituting the owner's tile. Recovery uses the same selection and reserves already scheduled destination tiles, including companions whose old positions are outside the owner's region of interest. Target-following and owner isolation remain separate.

**Conduit availability.** Exact950 CS2660 and CS7473 read varp11218 for the Conduit requirement. It was never published. Equipment now shares the server predicate: slot5, unnoted strict-cache definition, params8898=1 and8899=1. Param9107 is not an equipped-conduit flag. T70 lantern55526 and T99 Entropic lantern61355 pass; empty slot, a main-hand Entropic guard and an ordinary shield fail. Equipment/container updates publish11218. The second player's state remains independent.

**Residual souls in the world.** Souls now have persistent player spot state in slot4, separate from the ordinary cast slots0?3. Count changes, including zero, reach each viewer once; unchanged state is not restarted every tick. Late joining viewers, re-entry, reused player indices and a concurrent cast graphic are covered. A real-cache resource?PLAYER_INFO test exercises1?5?0 for two viewers using Soulbound lantern55482 (param8928=48397, the exact five-soul passive).

Graphics7866?7870 use models130426/428/430/432/434 and sequence35465. Exact named cosmetic struct49980 (Pumpkin Residual Souls) uses the matching three-count model134807 and sequence35465. The original five meshes have933/1860/2787/3706/4633 vertices, consistent with the successive count family. These native model effects are used directly; no invented orbit positions or temporary generic actors are introduced. Semantic count-family mapping is an evidence-backed inference, still requiring visual acceptance. Their raw graphic definitions set opcode10; the native spot constructor at0x1403bb533 selects its mode0 from that flag. No speculative packet flag or perpetual resend was added. Final looping, placement and appearance are Vulkan-pending.

**Native buff/debuff UI.** Exact enum7716 slots1009/1038 attach interfaces284/291. The bootstrap now mounts them and no longer explicitly hides their wrappers. Native workspace visibility/positioning is preserved. CS11294 selects284:18 for buffs and291:1 for debuffs. Native struct metadata supplies icons, tooltip and classification; there is no custom overlay.

The shared publisher uses CS10624 to add/remove, CS4252 for supported duration endpoints, and CS9379 for Anticipation/Devotion relative to the native activation clock. Devotion extensions use total elapsed+remaining time. Living Death maps to buff48339 and publishes11059=ability48324 for the native icon/tooltip. Berserk uses52793, Revenge3636, Limitless37214, Searing Winds52801 and Shadow Imbued52802. Conjures use48335/36/37 and32349. Resource buffs48333/34 follow count activation/removal; soul duration follows the server's out-of-combat expiry.

Legacy defensive structs that set duration through CS6570 retain that native path. Variable-duration Barricade's client countdown may still differ from its shield-scaled authoritative duration; server expiry removes it correctly. Revenge/Skeleton/Phantom native stack labels are not yet fully published. These are explicit remaining limits, not a claim of complete effect UI.

**Ordinary queue and Finger.** A normal Touch?Finger queue, without `;;queuehold`, publishes4164 plus bar identity5861=1003, retains Necrosis4 during the GCD, then consumes it on execution, deals damage, clears the marker and emits native CS6570 activation/cooldown feedback. This confirms the server/transport path; the ordinary short-lived ring still needs live visual acceptance. Revolution scheduling was preserved.

## Presentation still incomplete

`protocol-analysis/combat-presentation-gaps-950-20260923.json` reports all85 admitted definitions and20 missing animation bindings. All currently bound graphics pass the identity gate. An absent optional parameter is labelled an unresolved requirement/identity rather than automatically declared a missing projectile. No ability is promoted to COMPLETE.

Conjure idle/follow uses the existing pinned companion BAS assets: Skeleton30265/BAS4703, Zombie30266/BAS4707, Ghost30267/BAS3284, Phantom31142/BAS4763. Actual actor attack, actor spawn/despawn and command effect bindings remain unproven. Player conjure cast35502/7899 is not misrepresented as an actor attack. Exact cache sequence parameters, cosmetic model enums, Undercut949.1 and public source searches did not establish those bindings. No Rasial hostile NPCs, nearby sequence-number guesses or generic undead animations were added. These stages remain PARTIAL/MISSING and are the next presentation priority.

Dive animation/FX, several other ability stages, full incantations, Zombie poison and Phantom command scaling remain incomplete. This pass does not label deeper Necromancy complete or distract from the live failures with NPC expansion.

## Verification and application

See `protocol-analysis/combat-refinement-validation-20260923.json` for final test totals, hashes and staging checks. The canonical matrix contains the latest live results. The single live checklist is `docs/COMBAT-REFINEMENT-LIVE-CHECKLIST-20260923.md`.
