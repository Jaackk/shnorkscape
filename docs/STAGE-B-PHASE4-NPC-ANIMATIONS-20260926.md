# Stage B Phase 4 - NPC preview animation metadata

Status: **USER TEST PASSED - APPROVED** (2026-09-26). Implementation base: `1187f447f97b1db3ad0804ffa7a6508eb148dfa9`.
The owner physically tested the new NPC preview animations and reported that they look fine for now.
This is user-reported visual acceptance, not proof that inferred mappings are canonical NPC behaviour.
No individual NPC-by-NPC result was supplied. No Phase 5/V3 work.

## Current working tree and inherited implementation

The takeover began with exactly these twelve Phase 4 files, with no unrelated source or scratch files:

Modified:
- `Ataraxia950/game/com/rs/game/player/client/Native950CombatInspector.java`
- `Ataraxia950/game/com/rs/game/player/client/Native950DeveloperPreview.java`
- `Ataraxia950/game/com/rs/game/player/client/Native950NpcCombatAnimations.java`
- `Ataraxia950/game/com/rs/tools/modern/Native950CachePreflight.java`

Added:
- `Ataraxia950/game/com/rs/game/player/client/Native950NpcAttackAnimations.java`
- `Ataraxia950/game/com/rs/game/player/client/Native950NpcDrawnWeapons.java`
- `Ataraxia950/resources/native950/npcanim-950.properties`
- `Ataraxia950/resources/native950/npcanim-attacks-950.tsv`
- `Ataraxia950/resources/native950/npcanim2-950.properties`
- `Ataraxia950/resources/native950/npcanim2-drawn-950.tsv`
- `Ataraxia950/tests/com/rs/game/player/client/Native950NpcAnimationGapCoverageTest.java`
- `Ataraxia950/tests/com/rs/game/player/client/Native950NpcCombatAnimationsRenderGateTest.java`

Claude had already adapted the two resolvers and four resources, added the render-family gate,
connected a fallback ONLY to Developer Preview, wired both pin verifiers into startup preflight,
and added five cache-dependent plus four cacheless tests. The Combat Inspector addition is specifically
a Slayer cross-check, not a new animation resolver or combat policy.

The takeover preserves that implementation. Added `tools/Native950NpcAnimationPhase4Acceptance.java`
for exhaustive fresh-JVM coverage and preservation checks, this report, a handoff entry and staging
manifest metadata. Corrected comments which overstated model-only evidence and referenced an upstream-only
combat method; removed incidental line-ending churn. No gameplay implementation was expanded.

During implementation the user prohibited committing/pushing. Approval was subsequently granted on
2026-09-26 after the physical test passed; the checkpoint now records that approval.
The inherited files, candidate and test results were saved locally in
`backups/phase4-takeover-20260926-190132/`. No reset, checkout, clean or upstream write occurred.

## Artaven data sources and provenance

Read-only reference: `C:/Games/UpdatedAuthorFiles/950OpenSource-2026-09-25/950OpenSource`.
The two animation classes come from its matching `Ataraxia950/game/com/rs/game/player/client/` paths;
the render gate is selectively adapted from `Native950NpcCombatAnimations`.
All four adopted resources are byte-for-byte identical to the reference. No combat correction table,
combat catalogue replacement, projectile code or Slayer bulk table was imported.

Resource SHA-256:

| Resource | SHA-256 |
|---|---|
| npcanim-950.properties | 21639a7b13cf3325b0365fe576ccec2303e6e163000420a47ea4bd4c372f021f |
| npcanim-attacks-950.tsv | 5106f91b8af661f1566f774ab6c8da754d15d1dcf401e8cdb9672df746cd2056 |
| npcanim2-950.properties | e38476a59aba1a0ac3db5e5b42527ff4cfdf7e75564f1802694cc70b1b56133b |
| npcanim2-drawn-950.tsv | 93c12657d1aa15b50150a11d6cb6bad12911ec5723d08d6e0d06cb44279111e4 |

- **CACHE-DERIVED / EXACT:** pinned NPC, item, struct and sequence bytes; NPC param2816, item
  param686, struct params2914/2917, and the literal worn-model matches. Fresh preflight re-derives
  these relationships. Exact bytes do not by themselves prove how an NPC should attack.
- **EVIDENCE-BACKED:** 16 declared-family rows with upstream-recorded Wiki corroboration;
  drawn table class A (5 rows) has weapon corroboration, class B (6 rows) has agreeing declared
  family and drawn-model routes. Wiki quotations are inherited provenance, not independently
  re-fetched in this pass.
- **INFERRED:** drawn class C (28 rows) maps a visible weapon to its family animation without a
  declared NPC attack contract. Elemental wizards have competing spell-structure candidates:
  the chosen staff animation is not established as their unique/canonical casting animation.
- **MANUALLY CURATED:** inclusion/evidence annotations in the supplied tables, and the existing
  legacy Slayer task-name list. These are distinct from the pinned cache facts.
- **UNKNOWN:** canonical anatomical/skeleton correctness; owner testing found the new previews visually acceptable
  for now, which does not upgrade inferred mappings to proven behaviour. The render
  gate rejects legacy-frame combat on an Animaya standing animation; it is not a complete skeleton
  compatibility proof. A missing render/stand does not provide positive compatibility evidence.

Upstream comments describe the upstream combat path and historical round-4 refusals. In this project
neither table is consumed by live combat. Round 5 adds dark wizard variants8871-8874; round-4 comments
about refusing dark wizards must not be read as the final combined coverage.

## Before / exact coverage gained

Before Phase 4, Preview took `attackAnim` from the existing admitted combat profile or used -1.
Now it preserves that choice when nonnegative and only fills gaps using the new table plus render gate.

Fresh-JVM acceptance initializes the SAME authored NPC combat/stats loaders used by the server:
2,644 combat definitions and 6,706 stat definitions. Omitting these loaders falsely counts already
authored animations as new; the probe explicitly asserts Graardor's baseline to prevent that mistake.

Across 55 imported definitions: **50 gain a preview Attack; five retain their authored Attack**.
All 16 declared-family rows gain coverage. Of 39 drawn rows, 34 gain coverage (A4/B4/C26).
Retained: Seren archer22470 (18237), Seren warrior22471 (18235), Black Knights22488 (18240) and
22489 (18245), Ancient warrior22498 (18222). None is overridden by the imported alternative.

New sequences include Guard5919/5920/9234 ->37378; Fire/Water/Earth/Air wizard2709-2712 ->18321;
Falador Guard9/3228 ->37385; Armoured phantom30023 ->37373. Complete rows and before/after values
are recorded in `protocol-analysis/stage-b-phase4-animation-coverage-20260926.txt`.

Uncovered controls remain unchanged: Man1 attack422, Graardor6260 attack17389,
Abyssal demon21502 attack24431, Seren mage22472 attack20743, Dark wizard172 and Necromancer22478
still -1. The drawn verifier accepts 34 block bindings and withholds five; blocks are NOT wired to
preview controls or live combat in this pass. No new Idle sequences, categories or boss mechanics.

## Combat / Slayer metadata and deliberate deferrals

Combat Inspector reports raw cache param50 beside an independent legacy `SlayerTaskData` name match.
An absent category or disagreement is not an error and does not change task eligibility. No conversion
of Attack to Aggressive. No `Native950SlayerAssets`, 2,931-row monster table or 181-category table was
imported; richer catalogue filters and labels remain a later decision. No live-combat fallback,
combat-row corrections, new boss encounters, damage, reach, timing or projectile changes.

Protected: Revolution, queues, keybinds, conjures, boss policies, catalogue/search, framing,
drag, clipping, zoom and the parked Save-preview bug. Phase 1-3 was not reworked.

## Startup preflight / tests

Interrupted verification resumed first after source/test review, using freshly compiled classes and
resources ahead of runtime dependencies, Java25 in a fresh JVM and the installed read-only cache.
`Native950CachePreflight` exited0 and explicitly included both new NPC animation tables.
Inherited XML: 1,626 tests, zero failures/errors, two skips. The previous build log's three failures
was stale; it was not the latest XML result.

Independent full rebuild/rerun: **1,626 tests, zero failures/errors, two existing skips**.
Both Phase 4 classes passed all **nine** focused tests (none skipped) within that run.
The exhaustive fresh-JVM probe passed all55 imported definitions and six unaffected controls.
The staged jar passed the real startup preflight and `Native950DeveloperConsole.verify()` against
the installed cache. These prove bindings, dispatch and preservation, not physical visual quality.
Machine-readable evidence: `protocol-analysis/stage-b-phase4-20260926.json`.

## Deployment and user check

This is Java/resources-only. No clientscript or cache edit is required, so rebuilding scripts would
unnecessarily bump versions. No Kotlin API used by overrides changes; their existing binaries can be
preserved. Build the jar, stage it through the existing manifest, verify the STAGED jar against actual
cache and Console pins, and use installer CheckOnly. Do not overwrite the running runtime jar to stage.
The owner runs `Update and Play.cmd` after READY FOR USER TEST. No agent restart or client automation.

Final candidate: `dist/developer-console-v3-20260925/`, using the existing installer allocation.
Built and staged jar SHA-256:
`CB5A1B7E3B2FC3A16FB2DE731A8159636658E5C8F3D7C30B797C69F95192941E`.
Only the jar changed among the51 staged artifacts; the other50 are byte-identical to the previous
candidate. `Apply-PlayabilityUpdate.ps1 -CheckOnly` passed all51 pins and launch-cache gates.
The manifest explicitly records the uncommitted source state and hashes of all12 Phase4 source/test/
resource files; base1187f44 is not represented as containing this uncommitted implementation.
No direct `-Deploy`, Kotlin rebuild or runtime jar replacement was necessary for this additive,
Java/resources-only change. This preserves the running installation until the owner applies it.

Additional tracked reporting files: this document, `SHNORKSCAPE-NEXT-AGENT-HANDOFF.txt`,
`protocol-analysis/stage-b-phase4-20260926.json`,
`protocol-analysis/stage-b-phase4-animation-coverage-20260926.txt`, and the existing
`protocol-analysis/playability-candidate-20260923.json`. The standalone acceptance tool is intentional
and reproducible, not a scratch probe. Compiled probe outputs and local backups remain ignored.

In `;;dev -> NPCs`, search by exact ID (use All variants if needed):

1. **Guard5919:** new sword Attack (37378); normal humanoid/weapon-bearing positive case.
2. **Armoured phantom30023:** new halberd-family Attack (37373); Slayer-creature positive case.
3. **Fire wizard2709:** new inferred staff Attack (18321); judge whether it looks appropriate, not
   merely whether it moves.
4. **General Graardor6260:** existing Attack17389 and Idle17387 must remain unchanged.

For each, alternate Idle/Attack and check limbs/weapon alignment. Drag while animated, change zoom,
switch selection and return: framing, continuous visibility, clipping and rotation should remain as
before. Do not use Save-preview success as an acceptance criterion; that issue is deliberately parked.
No need to spawn or fight these NPCs. The owner subsequently reported USER TEST PASSED for the new preview animations. The checklist above
records the requested test scope, not an assertion that each individual case was explicitly reported.

## Approved checkpoint

2026-09-26: owner approved Stage B Phase4 and authorised commit/push to main. The tested source
hashes are unchanged; the nine focused/full-suite results above remain applicable. Provenance
ratings A/B/C and the model-only inference limitations are unchanged. Artaven remained read-only.
The next task is public GitHub cleanup ONLY when requested; removing files from GitHub must retain
all local files. No cleanup, Phase5, further integration or V3 work was started.
