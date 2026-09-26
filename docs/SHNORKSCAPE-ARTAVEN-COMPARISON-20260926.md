# Shnorkscape vs Artaven 950 source — technical comparison for AI agents

Date: 2026-09-26. Read-only analysis. Neither tree was modified.

Audience: a Codex/Claude agent that will plan or perform selective integration between the two
revision-950 server trees. Every claim below is labelled with how it was established:

- **VERIFIED-CODE** — read in the actual current source (file/line given).
- **VERIFIED-DIFF** — established by an EOL-insensitive diff of the two trees.
- **DOC** — taken from Shnorkscape's own handoff/docs; not re-tested here.
- **LIVE** — the Shnorkscape owner physically tested it in the Vulkan client (from handoff status words).
- **UNVERIFIED** — plausible lead only; do not act on it without checking.

## 1. Locations

| Tree | Path | Git history |
|---|---|---|
| Shnorkscape (authoritative, writable) | `C:\Games\950OpenSource` | Yes: 252 commits, 2026-09-17 → 2026-09-25, HEAD `734982f` |
| Artaven snapshot (READ-ONLY) | `C:\Games\UpdatedAuthorFiles\950OpenSource-2026-09-25\950OpenSource` | No usable history; also shipped as `...\950OpenSource-2026-09-25.zip` |

Note: some earlier prompts quote `C:\Games\950OpenSource-2026-09-25\950OpenSource`; that path does not exist.

Both are the same lineage: an Ataraxia (Matrix-family) Java engine in `Ataraxia950/` plus an OpenNXT
Kotlin login/lobby/proxy layer in `OpenNXT/`, talking to the original revision-950 Windows client
(OpenRS2 cache 2691, "950.1"). Most "native 950" gameplay lives in
`Ataraxia950/game/com/rs/game/player/client/Native950*.java`.

## 2. Shnorkscape working-tree state at time of comparison

Uncommitted (do NOT present as finished, do NOT include in comparisons as shipped behaviour):

- Developer Console NPC/player preview work: `Native950DeveloperConsole`, `Native950DeveloperPreview`,
  new `Native950DeveloperPreviewOverrides`, `tools/developer_console_v2_scripts.py`, tests,
  staged-candidate manifest/pins, and the gate allowlists in `Start-950Server.ps1` /
  `Test-Bundle.ps1` / `Prepare-ClientCache.ps1`.
- LIVE: NPC preview clipping/containment, live drag rotation with the model staying visible,
  immediate local zoom.
- LIVE-FAILED: "Save" (persisted per-NPC preview zoom) does not restore after reselection.
  A number-format fix (client `tostring` op `0x086b` emits grouped digits like `3,200`) passed a
  real-cache lifecycle test but still failed physically. Root cause is still open; parked by the owner.

## 3. Method (reproducible)

A Python script walked both trees for `.java .kt .kts .properties .json .tsv .txt` under:
`Ataraxia950/{game,content,npc,network,core,tests,resources}` and `OpenNXT/src`.
Files are compared with `\r\n` normalised to `\n`.

| Category | Source files (.java/.kt) | All tracked kinds |
|---|---|---|
| Identical | 3,180 | 3,225 |
| Different at same path | 290 | 296 |
| Shnorkscape-only | 115 | 135 |
| Artaven-only | 760 | 1,222 |

An earlier Codex scan reported 3,057 / 234 / 69 / 608. The shape is the same; that scan covered a
narrower scope (it excluded tests/resources). Test files: Artaven-only 152, Shnorkscape-only 41.

File counts are a weak proxy for gameplay. Artaven's additions include many `*Acceptance`,
`*Probe`, `*Assets` and `*Pins` evidence programs alongside real features.

## 4. Where the differences are

### 4.1 Artaven-only (VERIFIED-DIFF)

589 of 760 are in `Ataraxia950/game/com/rs/game/player/client/`. Grouped by topic (`Native950` prefix dropped):

- **Quests.** 12 `Q*` modules: DeathPlateau, DemonSlayer, DragonSlayer, ImpCatcher, JunglePotion,
  LetThemEatPie, PiratesTreasure, PlagueCity, PriestInPeril, ShieldOfArrav, TheKnightsSword,
  WolfWhistle. Nine more: CooksAssistant, DruidicRitual, ErnestTheChicken, GertrudesCat,
  GoblinDiplomacy, RestlessGhost, SheepShearer, VampyreSlayer, WitchsPotion. The framework includes
  QuestRegistry, QuestModule, QuestModuleTable, QuestHooks, QuestStates, QuestRewards,
  QuestRequirements, QuestInstances and QuestLegacyModules. Per-quest completeness is UNVERIFIED;
  planning-table rows are not playable quests.
- **Economy/social.** GrandExchange* (Book, Catalog, Journal, Transfer, Ui, Favourites, Wire packets),
  Shops*, Trade*, MoneyPouch*, BankPresets*, Clans*, FriendsChat*, PublicChatUi, SocialSession/State/Ui,
  plus packet classes `Native950{Clan,FriendsChat,Grouping,PublicChat,Social,Telemetry}Packets`.
- **Death.** Death*, DeathsOffice, Gravestones*, KeptOnDeath, DeathLedger.
- **Housing/instances.** Housing* (Catalog, Furniture, Scene, State, Ui, Input, Travel, Teleport,
  Entrances), Homesteads*, Instances, `Native950InstanceRegion`, `Native950DynamicScene`,
  `Native950Housing{Actions,Packets}`.
- **Combat.** Artaven's own stack, parallel to ours: Abilities, AbilityBook, AbilityLoadout, AbilityUnlocks,
  AdrenalineMeter, BasicAttack, OffensiveAbilities, Necromancy*, Rituals, EclipsedSoul, Magic*,
  Ranged*, Melee*, CombatPrayer, PrayerBook/Codex/Forms/Slots/Icons, QuickPrayers, PotionEffects/Timers,
  AutoRetaliate, FamiliarCombat/Specials, DefensiveEffects, BuffBar, ProjectilesAcceptance.
- **NPC metadata.** NpcAttackAnimations, NpcDrawnWeapons, NpcAnimationPreview, NpcReach, NpcRemap,
  CombatDataRows/CombatRowCorrections/NativeCombatRows, DropTables, SlayerAssets/Progress,
  PlacedIdentities, PlacedCreatureIndex.
- **World.** Cities/CityPopulations, city files (Lumbridge, Varrock, Falador, Ardougne, Edgeville,
  Taverley, Yanille, Rellekka, AlKharid, Havenhythe), Transports, CharterShips, Canoes, MagicCarpets,
  FairyRings, SpiritTrees, KaramjaFerry, Crossings, WorldNav, ObjectNavigation, ObjectTransitions,
  ObjectDestinations, ObjectEditor, WorldSpawns, SpawnSuppressions, Bankers, Signposts, Wilderness.
- **Skills.** "Fourth pass" work across Agility, Construction, Cooking, Crafting, Divination, Farming,
  Firemaking, Fletching, Herblore, Hunter, Invention, Magic, Melee, Mining, Prayer, Runecrafting,
  Smithing, Summoning, Thieving and Woodcutting; also Archaeology storage and Daemonheim resources.
- **UI/misc.** AchievementsUi, CalendarUi, CustomizationsUi, MusicUi/Player, NotesUi, PowersUi, SkyboxUi,
  SystemsUi, HudPanels, RuneMetrics, HeroSummary, Tutorial, LevelUp, XpLamps, Emotes, Clothing,
  Makeover, Hairdresser.
- **Protocol/network.** `Native950ComponentValueActions` (client opcode 124), `Native950AdmissionBuffer`,
  `Native950GrandExchangeWire`.
- **Engine.** `Native950ObjectClipping`, `Native950InstanceRegion`.

### 4.2 Shnorkscape-only (VERIFIED-DIFF)

69 files in `player/client` plus resources, LAN security and tests:

- **Developer tooling.** DeveloperConsole, DeveloperActions, DeveloperCatalogue, DeveloperSearch,
  DeveloperItems, DeveloperLoadouts, DeveloperOutput, DeveloperPlacement, DeveloperPreferences,
  DeveloperPreview(+Overrides, uncommitted), DeveloperWorldEdits, EquipmentLibrary(+Assets, Catalogue),
  ItemBrowser, LibraryBridge, GamevalLookup, Symbols, AdminCommands, ContentCommands, DevelopmentAmmo,
  Completionist, BugTest, CombatQa, CombatInspector.
- **Combat.** AbilityAssets, AbilityCatalog, AbilityCoverage, Revolution, Surge, CombatBuffs,
  CombatEffectUi, CombatPreferences, CombatProgression, CombatAreas, CombatTravel, ConjureFormation,
  NecromancyEquipment, NecromancyResources, SoulVisual, LivingDeathAppearance, PlayerHits, PrayerContract,
  PresentationBindings, RangedPresentation.
- **Bosses.** BossCatalogue, BossRules (+ `boss-*.tsv/json/properties` resources).
- **Client workspace/layout persistence.** Workspace, WorkspaceStore, WorkspaceCapture, WorkspacePipe,
  WorkspaceIntegerDescriptor(+Generator), WorkspaceGateSeed, DisposableWorkspaceRestore, LayoutEditor,
  LayoutFixture, WindowCapture.
- **Other.** BankSort, PhysicalBanks, SavedLocations, WarsRetreat, WorldTraversal, ChristmasCracker.
- **OpenNXT.** `security/NativeLanAccess` plus LAN handoff/provisioning/override-linkage tests.
- **Outside the compared source.** `tools/` (cache decoders, script assemblers, acceptance probes),
  `protocol-analysis/` (exact-950 evidence), `docs/`, and the staged-update pipeline
  (`Update and Play.cmd`, `Apply-PlayabilityUpdate.ps1`, `tools/stage_developer_v3.py`, cache launch gates).

### 4.3 Differing at the same path (VERIFIED-DIFF, 290 source files)

Concentrated in `player/client` (136), `content/.../actions` (16), `content/.../content` (12),
OpenNXT net/model (12) and the modern950 protocol (5).

Shared core files that differ, so merge carefully:
Region, World, Entity, Hit, Player, Prayer, Skills, Equipment, Inventory, Bank, CombatDefinitions,
BuffDebuffTimersManager, NPC, IComponentDefinitions, NPCDefinitions, AnimationDefinitions,
Native950{Actions,Packets,Protocol,PlayerInfo,PlayerMasks}, Native950GameTransport, ActionBar,
ActionRouter, Projectiles, Hits, NpcViewport, NpcView, PacketDispatcher, Interactions, Save, SaveStore,
Session, Settings, Quests, Slayer, and most skill classes.

Treat every one of these as a three-way situation with no common base available. Hand-merge
specific hunks; never copy whole files.

## 5. Specific findings verified in code

### 5.1 Region object removal — BUG PRESENT IN SHNORKSCAPE (VERIFIED-CODE)

- `WorldObject` does not override `equals`, so it inherits `WorldTile.equals`
  (`WorldTile.java:255`), which compares coordinates only.
- Shnorkscape `Region.java` calls `List.remove(object)` on `spawnedObjects` / `removedOriginalObjects`
  at lines 1018, 1024, 1059, 1065, 1095, 1104, 1144, 1153, 1196, 1203, 1243 and 1252. When wall, floor
  and furniture share a tile, this can remove the wrong object record.
- Artaven adds `removeObjectIdentity(ledger, target)` (`removeIf(c -> c == target)`) and passes the
  exact selected ledger instance at each site.
- Artaven also resolves modern definitions via `Native950ObjectClipping.resolve()` when
  `Cache.isFlatReadOnly()`. That is a separate, larger change; review it independently.
- Recommendation: adapt the identity-removal helper only, and add a regression test with two objects
  on one tile.

### 5.2 NPC viewport identity — BUG PRESENT IN SHNORKSCAPE (VERIFIED-CODE)

- `Native950NpcViewport.java:251` has `if (npc != null && !nearby.contains(npc)) nearby.add(npc);`.
  `NPC` also inherits coordinate equality, so two distinct NPCs on one tile collapse into one.
- Artaven replaces this with an identity set:
  `Collections.newSetFromMap(new IdentityHashMap<>())` and `seen.add(npc)`.
- **Conflict:** Artaven's same file removes Shnorkscape's `!npc.isNative950Conjure()` guard
  (Shnorkscape line 163) and adds quest-visibility filters (`Native950RestlessGhost.visible`,
  `Native950QuestNpcs.visible`) that do not exist in Shnorkscape.
- Recommendation: port only the identity-set hunk. Keep the conjure guard. Add a test with two NPCs
  on one tile, one of them a conjure.

### 5.3 Interface decoder — ARTAVEN BETTER (VERIFIED-DIFF, about 200 changed lines)

Artaven's `IComponentDefinitions` adds:
- `decodeIncomplete` plus a static `incompleteDecodes` counter and failure reasons;
- `modernFormatExtension` bytes;
- `additionalHooks` (3 in format 6, 4 in formats 9/11);
- `aspect*Type == 4` handling for format ≥ 5;
- type-10 support for formats 6/9/11;
- native widget types 11/12/13/15/16 (formats 9/11).

Shnorkscape's `docs/DEV-UI-NATIVE-CAPABILITIES-950.md` records that the legacy decoder misreads
modern format-11 hooks. Artaven's version targets exactly that gap.

Shnorkscape tooling depends on this decoder: `tools/research_native_ui_950.py` has its own header
parser, and `Ataraxia950/game/com/rs/tools/InterfaceFullDumper.java` is also a differing file.
Recommendation: adopt it after running both the Artaven decode test
(`IComponentNativeWidgetDecodeTest`) and Shnorkscape's interface-dependent tests.

### 5.4 Component value actions (VERIFIED-CODE exists; semantics UNVERIFIED here)

`Ataraxia950/network/com/rs/network/protocol/modern950/Native950ComponentValueActions.java` defines
`OPCODE=124`. Codex previously noted an 11-byte selection message and IF_SETEVENTS bit 24.

Possibly relevant to Shnorkscape's unresolved Action Bar equipment binding, and a candidate transport
for reporting client-side control values. Establish exact semantics before use.

### 5.5 Grand Exchange (VERIFIED-CODE, behaviour summary only)

`Native950GrandExchange.java`, `Native950GrandExchangeBook.java` and related files (about 1,300 lines):

- A persistent order book. `Book.submit/update/process` first calls `match()` (player vs player).
  If `Native950ServerSettings.grandExchangeAutoFill()` (key `grandExchange.autoFill`) is on, it then
  calls `synthetic()` (system liquidity).
- `tick()` re-processes at most every 60 s. `onLogin` also processes.
- State is copied, mutated, then committed through `h.store.saveExchange(...)` (copy-on-write
  persistence). A failure sets `h.failed` and stops matching until recovery.

So "auto fills" means optional system-backed fills layered on genuine player matching.
Duplication/persistence safety, coin/item validation and the packaged acceptance failures are
UNVERIFIED. Audit them before adoption.

### 5.6 Residual souls — INTENTIONAL CONFLICT (VERIFIED-CODE)

- Artaven `Native950NecromancyAbilityExpansion.java:222`: `s.souls = Math.min(s.souls, soulCapacity(p))`.
  This reduces stored souls when equipment capacity drops.
- Shnorkscape `Native950NecromancyResources.java:18/37`:
  `s.souls = Math.max(s.souls, Math.min(soulCap(player), s.souls + 1))`. It caps gains only, so
  earned souls survive gear changes. This is intentional owner policy.
- Keep Shnorkscape's behaviour.

### 5.7 Projectiles — DIVERGED RESPONSIBILITIES (VERIFIED-DIFF)

Shnorkscape `Native950Projectiles.java` is 78 lines: world-thread batching and publication to all scene
viewers. Artaven's is 426 lines with broader publication logic. Do not replace ours wholesale; compare
behaviour per call site.

### 5.8 Transport/admission (UNVERIFIED)

Artaven adds `Native950AdmissionBuffer`, which buffers outgoing packets until native transport is
ready, and its `Native950GameTransport` differs from ours. This could affect login stability.
Treat it as high-risk: it needs strong evidence and focused tests before any change.

## 6. Shnorkscape functionality and validation status

Status is taken from Shnorkscape's `SHNORKSCAPE-NEXT-AGENT-HANDOFF.txt` status words (IMPLEMENTED /
AUTOMATED-TESTED / LIVE-CONFIRMED / LIVE-FAILED / NOT TESTED) and from git history. It was not re-run
here.

| System | Where | Status |
|---|---|---|
| Native combat + action bars + ability queue/keybinds | ActionBar, Abilities*, Revolution, CombatPreferences | Multiple LIVE-CONFIRMED items; a P0 intermittent keybind death was recorded as unresolved (DOC) |
| Revolution (cache-defined settings) | Revolution | IMPLEMENTED; see commits 2026-09-17/23 |
| Multi-hit/channel abilities, bleeds, ultimates, buffs/timers | commits 2026-09-17/18 | AUTOMATED-TESTED; partly LIVE |
| Necromancy | NecromancyResources, ConjureFormation, SoulVisual, LivingDeathAppearance | PARTIAL: first six entries; conjures/souls foundations; several mechanics missing; book icons wrong (DOC) |
| Projectiles to all viewers, world-thread batched | Projectiles | IMPLEMENTED (commits 2026-09-20) |
| Prayer via exact 950 cache contract | PrayerContract | IMPLEMENTED + tests |
| Workspace/layout persistence | Workspace* | LIVE-proven on account Jaxa (save + restart; commit "Prove Jaxa automatic workspace durability") |
| LAN guest auth | OpenNXT NativeLanAccess | IMPLEMENTED + acceptance tests; used live |
| Equipment Library + developer loadouts | EquipmentLibrary, DeveloperLoadouts | LIVE-CONFIRMED ("looks excellent", tier-aware ammo) |
| Developer Console (native, in interface 1448) | DeveloperConsole* | Phase A USER LIVE ACCEPTED; later V3 polish partly live; preview Save LIVE-FAILED (uncommitted) |
| Bug Test telemetry + Combat QA recorder | BugTest, CombatQa | IMPLEMENTED + tests |
| Boss catalogue/rules, scoped encounter recovery | BossCatalogue, BossRules | IMPLEMENTED; Graardor normal combat added |
| Staged update pipeline | Update and Play.cmd, Apply-PlayabilityUpdate.ps1, stage scripts | In daily use; hash-pinned with rollback backups |

## 7. Exact-950 contracts discovered in Shnorkscape (useful to any agent)

These came from decoding native clientscripts (`tools/library_trace_read.py` using the normalised
opcode map in `protocol-analysis/ui-scripts-950-evidence.json`). Opcodes below are normalised
identities, not raw wire values.

- **Model drag rotation.** CS11619 is `0x353(0x8a2(dragLayer), -1, dragLayer)`, then drag hooks
  `0x815→CS8479` and `0x732→CS8480`, then cursor 189 via `0x413`. `0x8a2` is a component getter
  (it returns a component); `0x353` takes three arguments. CS11620 clears with
  `0x353(-1, -1, layer)` plus empty hooks.
  - CS8479/CS9644 address the rotated model as `(component, child)`: child ≥ 0 uses a
    `cc_find` dynamic child; child −1 uses `0x5a2` for a static component. Yaw derives from
    `0x057d` drag X, times 5.
  - The hook owner is drawn as the dragged component, so it must never contain the model.
    Owner = the model holder produced an invisible model during drag (LIVE).
- **Clipped preview layout that works (LIVE).** Model = dynamic child 0 of sized layer `1448:8`.
  Drag layer = its empty static child `1448:24`. Two resets are required:
  - `0x08be(0, 1448:24)`, because frame template 2003 marks `1448:24` as a modal cover with
    click-blocking `0x08be(1)`. CS343/CS1898 toggle the same op on window frames when the world map
    opens and closes.
  - The drag area passed to `0x353` must be larger than the layer. `host7` was used.
  - This mirrors retail Customisations `1311:343` (clip parent) and `1311:362` (drag layer).
- **Frame templates.** Interface 1448 hosts 4/6/8/10/12 each run CS8409(2001–2005) → CS8411.
  Enum 7716 maps each template to a struct (21137–21141) whose params name roles:
  3503 = frame root, 3504, 3506 = title, 3513 = modal cover.
- **1448 static tree.** Root 0 contains columns 3, 5, 7, 9, 11. Each column's fill-mode child is
  4/6/8/10/12, and those have three children each (17–31).
- **Button operation hooks.** Buttons get `CC_SETONOP` (`0x6a`) → a notify script sending
  `__devop:<epoch>:<actor>` via `0x77b`. A later `0x6a` on the same component replaces that server
  notification entirely.
- **tostring.** `0x086b` (126 native uses between an int load and a string join). Observed
  physically: its output uses thousands grouping (`3,200`).
- **Model view.** Six-value view getters `0x763, 0x25d, 0x836, 0x804, 0x73b, 0x526`; setter `0x112`
  on the active component. `0x526` is zoom, clamped 50–6000 by the console script.
- **NPC framing.** Beasts `753:40` via CS3869 (struct params 1347 = NPC, 3040 = zoom, 3041 = height).
  CS1165 is a timer auto-rotate (+2 yaw per tick), not a drag contract. NPC definition opcodes
  97/98 are resize XZ/Y (128 = unscaled).

## 8. Integration guidance for an agent

1. Never bulk-merge. There is no common git base; 290 shared files diverged on both sides.
2. Suggested order, smallest and most provable first:
   1. Region identity removal (5.1)
   2. NPC viewport identity set (5.2)
   3. IComponentDefinitions (5.3)
   4. Component value actions (5.4)
   5. NPC animation/metadata tables
   6. housing placement contracts
   7. Grand Exchange (audit first)
   8. quests, one module at a time
   9. magic/prayer diffs
   10. world content
   11. transport/admission (highest risk)
   12. bosses last — Artaven reports his boss update was unfinished.
3. Protect Shnorkscape's Developer Console, Equipment Library, developer loadouts, Gameval tooling,
   Bug Test, Combat QA, Revolution/queue/keybinds, developer combat modes, boss tooling and encounter
   ownership, projectile batching/publication, residual-souls policy, LAN/workspace/update safeguards
   and verified cache/client modifications.
4. Shnorkscape deployment rule: a source edit does not reach the live client until the full pipeline
   has run:
   1. `python tools/build_developer_console_950.py` (if clientscripts changed)
   2. `Build-Ataraxia950.ps1 -Tasks jar -Deploy`
   3. `Build-950Lobby.ps1`
   4. re-pin the manifest jar `beforeSha256`
   5. `python tools/stage_developer_v3.py`
   6. verify: `Native950DeveloperConsole.verify()` against live + candidate cache, and
      `Apply-PlayabilityUpdate.ps1 -CheckOnly`
   7. the user runs `Update and Play.cmd`

   `verify()` hashes pinned scripts bundled in the jar, so a jar built before re-assembling the scripts
   breaks `;;dev` with an IllegalStateException.
5. Shnorkscape rules (see `AGENTS.md`): run `Backup-BeforeEdit.ps1` before editing, and do not
   commit/push without owner approval.

## 9. Open questions to verify next

- Per-quest completeness in Artaven (dialogue, progression, persistence, rewards).
- The Grand Exchange packaged acceptance failures: genuine bugs vs stale fixtures.
  Coin/item duplication paths.
- Semantics of opcode 124 / IF_SETEVENTS bit 24.
- Whether `Native950AdmissionBuffer` solves a race that Shnorkscape still has.
- The 760 Artaven-only files: separate evidence programs from live features.
