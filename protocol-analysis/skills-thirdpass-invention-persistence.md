# Third skill pass: Invention and persistence

## Invention research

Added all nine ordinary junk-chance reduction upgrades through the existing inventor workbench menu. Both Discover and Manufacture workbench pieces can show the research entry; these are separate950 object IDs with option1, not two options on the same object. The current research row pins are in Native950InventionResearch.java; cache probe output is logs/invention-research-cache-probe.log.

The paired950 DB rows262 through270 place the required Invention level in column9, unlike the910 manager's column8. Columns0/12 give blueprint identity/prerequisite links. Required levels are34,49,64,69,78,83,91,95,105. Each row is SHA256-verified before use.

The Wiki's research multiplier sequence is1,.99,.97,.95,.93,.91,.88,.86,.83,.80. This is applied inside the shared ordinary disassembly roll, including noted items; guaranteed special materials remain independent of the junk roll. Analyse reports base and effective chance. Merely raising a skill level does not grant research.

Research is an interruptible workbench action with a captured origin/controller and expected next tier. It rechecks level, physical workbench identity/reach, action ownership and prerequisites, and awards each tier once. Existing base80 Crafting/Smithing/Divination access requirements remain. The current-level research requirement permits boosts, as discovery does. The optimisation puzzle is not ported: this simplified study action grants only the original basic/Poor discovery XP (20% of the original perfect-XP table), without an optimisation bonus.

Ordinary disassembly and manufacture now stop if their originating action is replaced, their controller changes, or their character moves/teleports away; workbench activity rejects combat/locked/dead states. Existing atomic container exchange, material caps, XP multipliers and quantity routing remain the owners.

References: [Junk](https://runescape.wiki/w/Junk), [Invention discovery](https://runescape.wiki/w/Invention#Discovery), [Invention](https://runescape.wiki/w/Invention). Cache IDs and fields come from the local cache; mechanics come from these Wiki sections and the original910 InventionManager.

## Saved progress

The user explicitly approved persistent skill progress after automatic approval review requested approval. Existing schema4's bounded SKILL_PROGRESS section now writes nested version3, appending11 bytes: one research-tier byte,12 boon flags in a short, and two integers for familiar pouch/lifetime. Nested versions1/2 remain readable and default only these new fields. Other profile sections and outer schema remain unchanged. Values are bounded and copied defensively. The existing checksummed, atomic file writer is retained.

An old engine cannot read a new nested-version3 section. Rollback requires the matching pre-deployment runtime and character backup, not only replacing the JAR. Validation of old/new roundtrips and an untouched real-profile migration copy is required before deployment.

## Validation so far

- Cache-verified normal encrypted Discover flow for all nine research tiers, level refusal, cancellation, retired responses and exactly-once XP/unlocks passed.
- Existing encrypted Invention/Archaeology routing remained successful:375 ticks,633 frames, including noted disassembly, stale selected instances, locked-action preservation and manufacture.
- Unit coverage added for systemic junk reduction, guaranteed material preservation, no free research and invalid restore refusal.
- Combined build, save migration and post-deployment checks are recorded in the final validation manifest when complete.

The full discovery puzzle, item augmentation/XP, gizmo perks and the native materials catalogue remain future work.

## Final integrated result

All changes built and installed. Full1260-test pass plus2skips; ten cache acceptances,19 bridge checks and cache preflight passed. Expanded normal-input acceptance:407ticks/700frames, including Weave and Summon. Final real-profile migration preserved every existing section and left original bytes unchanged. See skills-thirdpass-validation.json for deployment hash and backup.
