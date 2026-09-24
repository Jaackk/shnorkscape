# Darkan / Gameval architecture investigation

24 September 2026. Research only: source inspection, read-only cache comparisons and documentation. No gameplay/runtime/client/save changes, external dependency installation or `;;dev` modification. User reports the console is working rather well; further console work is deferred.

## A. Executive conclusion

**Adopt a verified symbolic-identity layer incrementally; keep SHNORKSCAPE.** The developers' central point is sound: modern RS3 tooling resolves meaningful names down to static interface components. SHNORKSCAPE is paying avoidable costs by repeatedly researching numeric identities. Names do not supply interaction contracts, gameplay rules, networking, timing or ownership.

Three different references must not be conflated:

| Reference | Established |
|---|---|
| Public `darkanrs/server`, dev | Modernized **727** server; runtime catalogs and generated Java constants, including components. Not a949/950 EoC server. |
| Public `project-undercut/engine`; local engine-dev | Modern NXT reference, local setup documents949.1; exact screenshot `requireId`/`requireComponentHash` API, beta Gameval decoder/exporter and component alignment tooling. |
| Screenshot's ResilientNXT-949.5 | Private/different content tree. API resemblance corroborated; full implementation, catalog and live feature quality unavailable. |

Our exact live cache2691 (950.1) **does not contain index67 Gamevals**. A complete original950 name table cannot be extracted directly from it. A selected949 beta2670 name can nevertheless become a verified950 symbol through independent target-cache evidence. Six sampled interfaces match exactly, including file identities; frame1477 does not. Do not import the foreign table wholesale.

## B. Claims verified

- Symbolic items/NPCs/objects/sequences appear in actual public content, not only constants.
- Both public families support static component names and packed component references.
- Undercut has the exact screenshot API: runtime JSON lookup, cached reverse maps, required-name failures.
- Original beta data contains representative skybox/component/resource names; independently decoded here.
- Names improve discovery; shared handlers separately reduce implementation duplication.

See [Undercut resolver](https://gitlab.com/project-undercut/engine/-/blob/f5d2b28ae31cfdaf27062b203567c9879993b858/core/src/main/kotlin/world/gregs/voidps/gameval/Gameval.kt), [Darkan inventory consumer](https://gitlab.com/darkanrs/server/-/blob/853588e38304eca993ee5928ac45845d5e4e7df0/world/src/main/java/com/rs/game/model/entity/player/Inventory.kt), and section Q's evidence audit.

## C. Partially true / misunderstood

“We don't use numbers anymore” describes callers, not the wire or resolver. Dynamic slots, operations, coordinates and packet layouts remain numeric. A generated `component_623_62` is a symbol without much added meaning.

Pinned727 catalogs contain **3,900 named components/65,398 total**,209 named interfaces/1,324,16,186 named sequences/17,224,18,664 named items/25,489 and298 named containers/674. These are identity coverage, not gameplay completeness. Production Inventory uses meaningful names; Adventurer's Log uses placeholders; Doors and POH still contain extensive numeric IDs.

Darkan's catalog-to-constant generation is reproducible. Its original727 name-recovery pipeline is not: its documentation says the one-time recovery tooling is gone. Editable catalogs are the retained artifact. Undercut's beta exporter is available source. [Pinned provenance contract](https://gitlab.com/darkanrs/cache-unpacked/-/blob/f05ba1e700c7e6b810c451b2f5cbc792d1057372/gamevals/CONTRACT.md)

## D. Unsupported claims

The screenshots do not prove full/live-correct POH, Dungeoneering or Necromancy; that every private-project name is original Jagex data; that all950 identities are recoverable; or that changing bases would be cheaper. No measured token/development saving percentage is available. No private source was copied.

## E. Screenshot analysis

IDs below are **source2670 IDs**, not blanket-approved950 mappings. A=original/cache-derived name; B=generated symbol; C=project wrapper/alias; D=manual mapping; E=other. A Java variable holding a genuine name is still authored code.

| Visible identity | Finding / classification |
|---|---|
| `machinima_interface_skybox` | Enum15005 in viewer export, A candidate; enum source archive not independently decoded. `SKYBOX_ENUM` identifier is C. |
| `machinima_remember_selection_skybox` | A, player varbit28041, independently decoded67/61; target definition matches. |
| `machinima_custom_sun_colour_r_perm`, fog and volumetric counterparts | A, varbits42886/42889/42892; original names decoded; selected950 definitions match. Verify other suffix variants individually. |
| `skybox_tab`, `filter_tab` | A suffixes of `machinima_skybox_filter:...`,623:27/26. |
| `load_save_button`, `save_button` | A suffixes,623:16/17. |
| `sun_colour`, `fog_colour`, `volumetric_colour` | A suffixes,623:1/3/5. |
| `scroll_bar`, `button_click_layer` | A suffixes,623:12/37. `GENERATED_LIST_PARENT` is C. |
| `content_panel` | Inspected screenshot/data use `content_panel_blue`,623:11; abbreviated prompt text is not the exact symbol. |
| `com_21`, etc. | In original table: A but weakly descriptive. Original names are not always informative. |
| `requireId`, `requireComponentHash`, `onIfButton`, bridge classes | C: authored resolver/event architecture, not Jagex gameplay implementation. |
| `SKYBOX_NAMES`, filter labels, counts and child bounds | C/E: authored UI choices and numeric limits. |
| `rand_floor_select`, `rand_complexity_select` | A,947/938; raw names verified and complete target groups match. |
| `rand_party_side_menu` | **Not found** in examined2670 catalog; it has `rand_party_side_new` at91. Private alias/different export/version remains unknown. |
| `toplevel_v2:minigame_window_content`, `modal_window_content` | A source names1477:291/736; **not approved at those950 slots** because frame differs. |
| `rand_dragonkin_ruin_large_dungeon_entrance_steps_lvl1` | Loc48496 in public export; source archive not independently decoded. Loc is an object definition, not the3454,3722 coordinates in code. |
| `construction_house_tutorial_furniture_chair_crude` | Appears as obj61880,loc138056,dbrow18480. Typed lookup matters. These specific target950 payloads were not audited. |
| `TUTORIAL_COMPLETE=100`, stage constants, furniture selection wrappers | C/D: authored progression/selection rules. Gamevals do not prove the threshold or implement POH. |

For selected components above, raw67/0 confirms the names. All623 and1448 definition files match950. No generated declarations are visible in these screenshots: do not label every string as a generated constant.

## F. Concrete public examples and support by category

Classifications apply to inspected public source, not all derivatives.

| Type | Support / production evidence |
|---|---|
| Items | **STRONG GAMEVAL SUPPORT**, mixed adoption. Inventory `Obj.coins`; Undercut FoodPlugin symbolic cake/pizza remainder chains. |
| NPCs | **STRONG mechanism**, incomplete semantic coverage. BossInstanceRegistry uses `Npc.king_dragon`, `Npc.kalphite_queen`; GiantMole uses `Npc.npc_3340`. |
| Objects/locs | **PARTIAL / RAW IDS STILL COMMON**. Staircases uses `Loc.fai_varrock_stairs_taller`; Doors and HouseObjects retain large numeric tables. |
| Sequences | **STRONG**, some placeholders. DungeoneeringSpellbook named alchemy sequences alongside `Seq.seq_9633`. |
| Spotanims | **PARTIAL**. Darkan uses `Spotanim.highlvlalchemy` and placeholders. Inspected modern67 type list has no dedicated spotanim table; typed seq/model joins can derive candidates. |
| Graphics | **STRONG identity mechanism** for `graphic`=2D assets, not world spotanim/GFX. |
| Sounds | **PARTIAL**. Typed Sound exists; sampled spell uses placeholder `Sound.sound_98`. Modern beta sound/MIDI namespaces exist. |
| Varbits / varps | **STRONG**, domain-sensitive. Player/NPC/clan/object/client domains differ; GameSettings uses named Varbits. |
| Interfaces / static components | **PARTIAL semantic coverage, strong mechanism**. Named inventory and generated placeholders both in content; Undercut ParentWindows uses full names. |
| Dynamic children / operations | **WRAPPER-BASED**. Host may be named; generated row/slot and op remain runtime values. |
| CS2 | **PARTIAL**. Separate clientscript catalog; Inventory uses `Cs2.cs2_5560/5561`. Undercut has hash/unscrambling tools. No clientscript table in examined RS3 index67 type list; names for950 scripts6570/8286 not established here. |
| Enums / structs / params | **STRONG identity mechanism; PARTIAL consumer audit**. Supported type tables; enum15005 and ability-related struct names aid discovery. Parameter contracts still need decoding. |
| Quests | **PARTIAL**.168 named/204 entries in727 catalog; stages/dialogue/vars still authored. |
| Abilities | **WRAPPER-BASED / PARTIAL**. Ability structs/sequences/resource vars have names; no universal single ability table in inspected decoder. |
| Locations/coordinates | **RAW VALUES STILL COMMON**. Loc means object type; tiles, regions and instance transforms are separate. |
| Inventories/containers | **STRONG**. `Inv.worn` ->94 -> Equipment item-container packets. |
| Skills | **WRAPPER-BASED / NOT FOUND as separate Gameval table**. Skill constants/enums/parameters are separate concepts. |
| Other types | Model,BAS,cursor,hitmark,headbar,category,DB row/table,achievement,material,font,UI animation supported in modern metadata; availability revision-specific. |

### End-to-end traces

1. **Darkan inventory:** catalogs interface679=`inventory`, component679:0=`inventory:inv` -> generator `Component.inventory.inv` -> packed44,498,944 -> Inventory.unlockInventoryOptions extracts low16 and sends IFEvents for slots0..27 with authored use/operation flags.
2. **Equipment:** inv94=`worn` -> generated `Inv.worn` -> Equipment sends/updates container94. Name does not implement dirty-container tracking.
3. **Coins:** obj995=`coins` -> generated `Obj.coins` -> Inventory addition/removal and Add-to-pouch handler. Overflow/pouch logic remains authored.
4. **Undercut parent:** original name `toplevel_v2_parent:suboverlay_layer_5` -> component JSON1448:11 -> cached reverse map ->94,896,139 -> ParentWindows.layerOf(5) -> IfSetHide/closeQueued/openPanel. All32 target files match.
5. **Skybox:** original67/0 string at623:27 -> normalized full name -> resolver40,828,955 -> interface addressing. All112 target files match. Screenshot's actual handler remains unverified.
6. **Resources:** original67/61 names10986=`combatv2_buff_necromancy_necrosis_stacks`,11035=`combatv2_buff_necromancy_residual_soul_stacks` -> player-var lookup -> existing SHNORKSCAPE publications. Both generic var definitions match, but semantic confidence additionally comes from existing950 scripts/live tests, not identical generic bytes alone.

Consumers: [Equipment](https://gitlab.com/darkanrs/server/-/blob/853588e38304eca993ee5928ac45845d5e4e7df0/world/src/main/java/com/rs/game/model/entity/player/Equipment.kt), [Staircases](https://gitlab.com/darkanrs/server/-/blob/853588e38304eca993ee5928ac45845d5e4e7df0/world/src/main/java/com/rs/game/content/world/areas/global/Staircases.kt).

## G. Origin, generation and reproducibility

**Modern route:** Jagex beta cache -> index67 group per type -> dense(version1)/sparse(version2) string-offset table -> null-terminated names -> normalization/domain split -> JSON -> runtime lookup. Undercut lowercases component strings and replaces the first double underscore with colon. Combined player variables contain prefixed varbit names, separated/rebased by the exporter.

Independent decoding here verified1,881 interface names and10,627 player-var names against the viewer. All50,690 raw player-varbit entries match; the viewer has3,210 additional entries whose origins were not established. Component data has104,326 entries, with **55 differing viewer/raw keys**, all in interface 1433 (Escape Menu): 51 changed values and two keys present only in each source. Example raw1433:40=`report_issue_layer`; viewer=`report_issue_build`. Cause unresolved: record separate provenance, not a silent correction. Selected screenshot components agree.

**Darkan727 route:** newer names plus documented cross-revision recovery -> editable727 catalogs -> buildSrc generator -> typed Java constants -> content. Recovery channels include structure/script/hash evidence. Catalog-to-code generation is available; historical recovery is not. Authored additions coexist with recovered names. Generator validates collisions, escaping, component/interface agreement and coverage. Output is deterministic under `core/build/generated/sources/gameval/java`; task `:core:generateGamevalConstants`. External project was inspected, not built/run. [Generator](https://gitlab.com/darkanrs/server/-/blob/853588e38304eca993ee5928ac45845d5e4e7df0/buildSrc/src/main/kotlin/org/darkan/build/gameval/GamevalCodegen.kt)

**API details:** Undercut reads per-type external JSON, with bundled fallback, loads lazily and caches reverse maps. `requireId` throws for missing names. `requireComponentHash` returns32-bit packed value; `requireComponentId` returns low16. In current Darkan727, **requireComponentId returns the packed hash instead**. Same-looking APIs are not interchangeable. Darkan's folder-only runtime loader hides placeholders and retries missing catalogs; build-time constants are a separate path. Neither runtime lookup alone proves950 bindings. [727 resolver](https://gitlab.com/darkanrs/server/-/blob/853588e38304eca993ee5928ac45845d5e4e7df0/core/src/main/kotlin/org/darkan/core/gameval/GamevalCatalog.kt)

## H. Component deep dive

Static component names are genuinely available data. Packing is `(interfaceId << 16) | (componentId & 65535)`, an address rather than a cryptographic hash.

A static name does not name every child created by CC_CREATE. Our1448:11 placement host is static; its session slot is dynamic. onOp hooks, IFEvents masks, current mount and source-slot/epoch validation remain necessary. A component named `skybox_tab_click` does not reveal the full callback/input contract.

Undercut ComponentAlignment matches sequences of component shapes (length plus first9 bytes), accepting remaps above90% matched-shape agreement when better than identity. Failed alignments are listed. The inspected exporter retains original keys if no remap exists, including unresolved interfaces, and its drift helper **logs** CRC mismatch. Useful research assistance, but not sufficient fail-closed proof for SHNORKSCAPE. Do not ship unresolved beta slots or treat a high shape score as exact identity.

The old `;;dev` marker defect illustrates the limit: a border/container name might have made misuse clearer, but would not itself solve deferred IF_SETTEXT mutation, native refresh ordering or acknowledgement. Type checks and live evidence remain essential.

## I. Generic-content architecture examples

- **Stairs:** named Loc families share orientation/plane transforms; fallback uses display names/options, with location exceptions. Symbols save discovery; transforms save repeated implementation.
- **Doors:** common pairing/movement serves many objects, using options/shapes plus long numeric exception lists. Display-name registration is not Gameval lookup.
- **Mining:** RockType, shared Mining action, level/pickaxe/depletion rules cover families. Display-name variants and special numeric IDs remain. Metadata-driven families are a separate benefit.
- **Undercut food:** default handler and symbolic portion chains avoid individual cake/pizza implementations; healing/delays/resources remain shared code.
- **UI:** named parents plus GameInterface/IFEvents/lifecycle helpers reduce boilerplate. EventRegistry/NameResolver support resolution and unresolved-name reporting.
- **POH:** HouseRooms/HouseObjects/HouseBuilds/RoomReference separate templates, furniture, requirements and saved layout. Much is authored numeric data; reusable architecture is not evidence Gamevals completed the feature.

Avoid automatic gameplay equivalence based only on matching name prefixes. Use reviewed typed metadata and explicit exceptions.

## J. Exact revision950 compatibility

Target is [OpenRS2 cache2691,950.1](https://archive.openrs2.org/caches/runescape/2691), with existing audited local deltas. Names sampled from beta2670,949,25August2026. Viewer index lists2691 as skipped because67 is absent; local `cache/255/67.dat` and `cache/67` are absent. No full original950 name source found.

Production FlatCacheRepository compared file identities and decoded payloads read-only:

| Interface | Beta files |950 files | Same-ID equal files | Result |
|---|---:|---:|---:|---|
|91 party side |55|55|55|Exact group/file-ID match |
|517 bank |341|341|341|Exact match |
|623 skybox/filter |112|112|112|Exact match |
|938 complexity |71|71|71|Exact match |
|947 floor select |762|762|762|Exact match |
|1448 parent |32|32|32|Exact match |
|1477 main frame |926|924|65|**Unsafe to import directly** |

The six matching groups provide strong target evidence for their original source names.1477 needs reviewed alignment; sampled291/707/713/714/715/736 differ. Near-revision proximity is not a fallback.

Varp2/60:13,202 beta versus13,270 target files;13,196 same-ID matches. Varbit2/69:61,465 versus61,889;60,168 matches. Counts are not blanket semantic approval. Screenshot varbits28041/42886/42889/42892 match;10986/11035 additionally have existing950 script/live corroboration. Living Death35475 already has prior group-equality evidence in `protocol-analysis/polish2-gameval-cross-revision-950.json`.

Keep exact matches, changed assets and missing identities separate. Changed mappings need950 scripts, typed joins or reviewed structural proof. Do not rewrite target bytes to fit foreign names.

## K. SHNORKSCAPE comparison and content opportunities

| Previous investigation | Expected Gameval contribution / remaining hard part |
|---|---|
| Action-bar/Powers/bank components | Discovery/readability improves; masks, dynamic slots, script signatures and keyboard-context24 release still require contracts. |
| Cooldown6570, refresh8286, buff bars | Names could narrow search if recovered. Exact950 names not established here; timing/var publication remains hard. |
| Dive | Named sequence35755 helps presentation discovery; opcode85 targeting, movement/clipping/synchronization remain authored. |
| Living Death/conjures | Material candidate-discovery benefit; hostile/cosmetic/player-owned distinctions, overwrites and cadence require joins/live checks. |
| Necromancy resources |10986/11035 become searchable and readable; generation/consumption/combat lifetime/swap-retention rules remain implementation. |
| Developer Console | Named static hosts aid composition/debugging; lifecycle/input sequencing is not solved by names. Backend remains reusable. |
| Equipment Library | Already cache-driven and tier/style-aware with safe isolated stock. Names complement existing architecture. |
| Persistence/multiplayer | Little direct benefit; retain saves, bank isolation, owner/world-thread rules. |
| Revision migration | Verified mapping layer localizes review; unverified constants can disguise wrong-revision bindings. |

**Content system opportunities:**

1. Transportation/stair/door and small skilling-node families: good bounded opportunities for shared handlers, with exact950 option/collision validation.
2. Quest/dialogue templates and shops: reusable registration, requirements and inventory safety. Audit save conventions and progression vars first.
3. POH: public `construction/playerOwnedHouse` includes rooms/builds/furniture, instances, controllers, servants and player manager. Valuable read-only architecture reference. A future950 design must validate DB rows, furniture transforms, chunks/collision, visiting permissions and persistence; do not port727 maps blindly.
4. Dungeoneering: dungeon/party/room/controller/spellbook/resource-shop code is substantial reference material. Completeness and950 compatibility were not live tested.
5. Minigames/bosses: reuse lifecycle/instance ideas selectively; encounter behaviour, rewards and multiplayer require separate audits.

Public Darkan supplies credible content-breadth reference material, not evidence that replacing our native950 combat/runtime is cheaper. Ataraxia ancestry does not prevent names, registries or templates. Preserve SHNORKSCAPE's existing live-passed systems and exact-cache checks.

## L. AI/token-efficiency implications

Likely **high benefit** for repeated ID discovery, search, onboarding, logs and test readability; **moderate benefit** for finding cross-references; **limited benefit** for packet decoding, UI lifecycle races, timing and ownership.

Example: search `residual_soul` in a verified index rather than rediscover11035. Search `living_death` across seq/model/struct candidates, then inspect typed links. Log full component name plus numeric pair and revision instead of ambiguous “component11”. Wrong-domain/stale bindings should fail at generation/startup.

No defensible saving percentage yet. Measure a small pilot of lookup tasks and rejected stale mappings. Names should shorten handoffs by linking evidence, not replace evidence with attractive labels.

## M. Recommended architecture — design only

Build on existing read-only FlatCacheRepository, with four separate layers:

1. **Original names:** immutable source cache/revision/type/ID/name, payload SHA and decoder version; preserve raw and normalized names.
2. **Verified950 bindings:** target identity, target payload/reference hashes, mapping method and evidence. Reject ambiguity; generic identical var definitions alone do not prove semantics.
3. **Authored aliases:** explicit `shnork.*`, linked to verified bindings with rationale. Never present aliases as original Jagex data.
4. **Generated API/search index:** deterministic sorted output, generated headers, no manual editing, pinned schema/generator/input manifests. Retain numeric debug/wire values.

Validate duplicates, type/domain collisions, escaped-identifier collisions, component bounds/ownership, missing required names, unknown formats, cache drift, viewer/raw disagreement and locally patched assets. Unresolved symbols remain research results, never automatic runtime bindings.

Use distinct ComponentHash/ComponentId and variable-domain types or unambiguous methods. Initially generate only reviewed/new-code-needed Java8 constants plus a broad research index. Startup verifies target hashes; cache updates produce a reviewable diff, not silent remapping.

## N. Incremental plan

**Phase1 — recommended first:** offline lookup/provenance index, starting with verified bank517, parent1448, skybox623 and existing resource/presentation evidence. Search originals/aliases separately; show type, source/target IDs, evidence and hashes. Frame1477 remains quarantined pending review. No gameplay migration.

**Phase2:** new code defaults to verified symbols; missing names trigger explicit research or documented aliases, never foreign-ID fallback.

**Phase3:** migrate touched high-value UI/resources while preserving hash contracts/tests and behaviour.

**Phase4:** optional legacy cleanup only when useful; no cosmetic combat rewrite.

Pilot acceptance: exact representative IDs, deliberate missing/collision/wrong-cache failures, deterministic output, numeric diagnostics, no runtime behaviour change. This investigation does not implement that runtime layer.

## O. Developer Console integration — design only

Yes: a future **Cache / Gamevals** section should become a core research capability using the existing action registry and safe catalogue/placement backend.

Search partial symbol, numeric ID **plus type**, display name or alias. Show provenance, source/target revision, verified/derived/alias/unresolved status, component pair/hash and decoded relationships. `sunshine`, `bank`, `living_death` should show typed candidates, distinguishing base/cosmetic/related effects.

“References” must mean actual decoded links, not inferred text matches. Separate2D graphic from spotanim; static hosts from runtime children; loc definitions from map coordinates. Safe NPC/object selections may enter existing Place/undo/ownership flows; unresolved entries stay read-only. Native clipboard/model-preview capability needs separate investigation. A standalone lookup or read-only `;;gameval` can precede the native page. Neither is implemented here.

## P. Risks / unknowns

- No complete original67 data in950 cache2691. Frame1477 drift is demonstrated, not hypothetical.
- Viewer1433 discrepancy and extra viewer varbits remain unresolved; do not merge exports blindly.
- Names are not behaviour contracts or proof of asset suitability. Some genuine names are generic; some generated727 constants are placeholders; authored names coexist with recovered names.
- Undercut local archive has no `.git`; only selected files were matched to public commit, not the whole archive.
- Private screenshot project's aliases, complete source and live content remain unavailable.
- Public Darkan has GPLv3 notices. No runtime source was ported; applicable terms must be considered before future code reuse. Read-only availability is not unrestricted redistribution.
- No reference server/client was run. No claim its full gameplay is live-tested.

## Q. Sources / commits / tools / reproducibility

| Reference | Examined identity / use |
|---|---|
| [Darkan server](https://gitlab.com/darkanrs/server/-/tree/853588e38304eca993ee5928ac45845d5e4e7df0) | dev853588e38304eca993ee5928ac45845d5e4e7df0,23Sept2026;727 modules. Sparse read-only checkout in temp/gameval-research. |
| [Pinned cache catalogs](https://gitlab.com/darkanrs/cache-unpacked/-/tree/f05ba1e700c7e6b810c451b2f5cbc792d1057372/gamevals) | Submodulef05ba1e700c7e6b810c451b2f5cbc792d1057372; six catalogs inspected; provenance/generation traced. |
| [Project Undercut](https://gitlab.com/project-undercut/engine/-/tree/f5d2b28ae31cfdaf27062b203567c9879993b858) | Observed devf5d2b28ae31cfdaf27062b203567c9879993b858. Local README949.1. Resolver SHA bdcd7b89... and exporter9a4e38a4... exactly match public files. Local decoder/alignment/registry/content inspected without executing. |
| Local older Darkan | Documents/RSPS/Darkan Server/world-server, origindarkanrs/world-server,6b0712f5f49a15c5c06b3723b4b6c177375a69a3,8Feb2026. Not confused with current symbols. |
| [Gameval viewer](https://gameval.z-kris.com/) | Broad source/export discovery; index SHA3481b8af...;2670 datasets. Not sufficient950 authority. Its own source repository was not established. |
| [OpenRS2 beta2670](https://archive.openrs2.org/caches/runescape/2670) / [live2691](https://archive.openrs2.org/caches/runescape/2691) | Raw67/0,24,61 plus selected2/3 groups and references; original bytes used for comparison. |
| Current SHNORKSCAPE | Native950DeveloperConsole,Native950ActionBar,Native950Settings, resource/presentation pins, catalogue and polish2 cross-revision report. |

### Useful tooling candidates

| Tool | Purpose / suitability |
|---|---|
| Undercut GamevalIndexDecoder + gamevalExport | Best reviewed format reference; direct beta67 decoding, domain split, optional component alignment, SQLite read-only cache. Would require adaptation to our flat loader; unsafe unresolved alignment must be replaced by fail-closed checks. No external build required for our small reader. |
| Darkan GamevalCodegen / validation | Deterministic typed constants, collisions and catalog validation. Input is727 catalogs, not950. Architecture reusable; implementation not installed. |
| Viewer | Quick multi-type symbol search and downloads, revision selection; raw discrepancy found. Use as candidate discovery with pinned hashes. |
| OpenRS2 | Exact historical cache bytes, group downloads and reference tables. Essential reproducible identity comparison; it does not supply gameplay. |
| [RuneLite Gameval namespace](https://github.com/runelite/runelite/tree/master/runelite-api/src/main/java/net/runelite/api/gameval), [OpenRune](https://github.com/OpenRune/OpenRune-Server) | OSRS architectural leads, not RS3 mappings. Not validated against950; no reason to import their runtime dependencies. |
| [rs3cache](https://github.com/mejrs/rs3cache), [rsmv](https://github.com/skillbert/rsmv) | Source-available RS3 cache/model inspection candidates. Full950 format support not run/certified in this pass. Existing SHNORKSCAPE loader is simpler for this bounded job. |

Rune-Server searches pointed to Gameval viewer/Undercut and older animation tools; forum assertions were not used as numeric authority. Prefer source/bytes. No opaque binaries installed.

### Research artifacts and reproduction

- `protocol-analysis/gameval-investigation-950.json`: selected names, original-data hashes, viewer discrepancies, coverage, source URLs and production-reader comparisons. **Not a deployable symbol registry.**
- `tools/research_gamevals_950.py`: independent bounded67 decoder and audit; writes only requested research output.
- `tools/GamevalIdentityResearch.java`: production FlatCacheRepository, read-only two-cache comparisons.
- Inputs retained in ignored `temp/gameval-research`; bulk reference code/cache assets are not committed. Inventory includes download URLs/SHA256. Reference-table URLs: `https://archive.openrs2.org/caches/runescape/2670/archives/255/groups/{2|3}.dat`. Put tables under `beta-cache/255/` and selected2/3 groups under `beta-cache/{index}/{group}.dat`.

Run Python with `--inputs temp/gameval-research --output <new-report.json>`. Compile Java helper against `Ataraxia950/build/libs/ataraxia-950-1.0-UNTRACKED.jar;OpenNXT/runtime/lib/*`, emitting to a new temp folder; run with arguments `cache temp/gameval-research/beta-cache`. No external builds/dependencies. Java output captured by Windows PowerShell is UTF-16 `file-comparison-final.tsv`; Python consumes that explicitly.

Raw format decode and production-reader comparisons completed; discrepancies retained, not disguised as passing matches. Repeat output is deterministic. No gameplay regression needed for unreferenced research utilities/documentation, and no runtime successor is staged.

### Direct answers

1. **Substantially correct, with qualifications:** identities help authoring; generic implementations are an additional independent benefit.
2. **Yes, down to static components**, with variable semantic coverage and numeric/dynamic contracts underneath.
3. **Most visible skybox strings are original names or suffixes; constants/APIs/bridges are authored.** `rand_party_side_menu` remains unverified; section E distinguishes cases.
4. **A verified950 subset is feasible now; complete direct extraction from2691 is not.** Quarantine changed mappings.
5. **Material benefit for discovery/search/comprehension; partial benefit for native reverse engineering.** No percentage claimed.
6. **Offline verified lookup/provenance index first**, then typed generated API for new code.
7. **Yes, a future core research section in `;;dev`**, preserving its working UI and ownership-safe editor backend.
