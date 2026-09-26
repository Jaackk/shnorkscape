# Handoff — Developer Console NPC preview (clipping/drag/zoom/Save) and Artaven comparison — 2026-09-26

Read this first if you last saw the project at or before commit `734982f` (2026-09-25).
The owner approved committing and pushing this state on 2026-09-26, with unfinished work recorded in
§0 below. "Uncommitted" wording elsewhere in this document describes the state during the session.

## 0. OPEN WORK — not finished, needs finishing

1. **Save preview size (LIVE-FAILED, parked by owner).** Zoom → Save → switch NPC → return does not
   restore the saved zoom in the live client, although the real-cache lifecycle probe passes. Start by
   logging every `__devzoom` string the server receives (in `Native950DeveloperConsole.handle`) and
   every Save value, then do one physical run. See §5 for suspects. Afterwards, clean stale entries in
   `OpenNXT/server-home/developer-preview-overrides.txt` (ask the owner first).
2. **Player preview** uses the same clipped/drag layout but was not separately physically confirmed.
   Quick owner check needed.
3. **Remove-an-override action** does not exist (Reset intentionally keeps the saved value). Add a
   separate developer action only if the owner wants it.
4. **Automatic framing** is still heuristic for NPCs without Beasts metadata. The resize factor and
   identical-geometry sharing help, but exceptions still need Save (blocked by item 1).
5. **Unused script 21148 local-zoom path for player preview** passes an empty report prefix. The player
   preview has no Save by design.
6. **Stage B Phase 1 (Region + NPC viewport identity fixes) is DONE — see §7.** Phase 2 (interface
   decoder) is DONE — see `docs/STAGE-B-PHASE2-COMPONENT-DECODER-20260926.md`. Later phases have **not** started. Begin only on the owner's explicit
   "CONTINUE TO ARTAVEN AUDIT". See `docs/SHNORKSCAPE-ARTAVEN-COMPARISON-20260926.md` §9 for the
   remaining open questions (per-quest completeness, Grand Exchange audit, opcode 124 semantics,
   `Native950AdmissionBuffer`, and separating real features from evidence programs in the 760
   Artaven-only files).
7. **Older open items** remain in the owner-local development handoff: Necromancy partial,
   P0 intermittent keybind death, object previews, remaining console categories/icons, etc.

Status words used: **LIVE** = owner physically tested in the Vulkan client; **LIVE-FAILED**;
**AUTOMATED** = tests pass; **EVIDENCE** = derived from decoded native scripts/cache.

---

## 1. Current physical state (owner-tested)

| Behaviour | Status |
|---|---|
| NPC preview clipped/contained inside the right-hand preview box | LIVE |
| Mouse-drag rotation; model stays visible throughout; final rotation kept | LIVE |
| Zoom − / Reset / Zoom + immediate (client-local) | LIVE |
| Player preview drag rotation (shares the same layout) | expected working; not separately reported |
| **Save** (per-NPC saved preview zoom restored after reselecting) | **LIVE-FAILED** — parked by owner, "come back later" |

Protect the first three. The owner explicitly asked not to rework the clipping or drag architecture.

---

## 2. Deployment lesson (critical — cost several wasted physical tests)

`Update and Play.cmd` does **not** build anything. It runs `Apply-PlayabilityUpdate.ps1`, which
installs a pre-built, hash-pinned candidate from `dist/developer-console-v3-20260925/` according to
`protocol-analysis/playability-candidate-20260923.json`, then restarts the server and client.
Editing source changes nothing live until you run the full pipeline:

1. `python tools/build_developer_console_950.py` — only when clientscript source
   (`tools/developer_console_v2_scripts.py`) changed. It writes `dist/.../cache-v4/12/*.dat` and the pins
   `Ataraxia950/resources/native950/developer-console-950.properties` +
   `protocol-analysis/developer-console-scripts-950.json`. Each run bumps the per-file cache version
   suffix even when the content is unchanged.
2. `.\Build-Ataraxia950.ps1 -Tasks test,jar`, then `.\Build-Ataraxia950.ps1 -Tasks jar -Deploy`.
   The jar **bundles the pins**. If the jar is built before step 1, `;;dev` fails with
   "IllegalStateException: Changed Developer Console script N"
   (from `Native950DeveloperConsole.verify()`).
3. `.\Build-950Lobby.ps1` (Kotlin overrides compile against the deployed jar).
4. Set the manifest's `OpenNXT/runtime/lib/ataraxia-950-1.0-UNTRACKED.jar` entry `beforeSha256` to the
   hash of the jar you just deployed. `-Deploy` writes outside the installer, and
   `stage_developer_v3.py` otherwise refuses with "Unreviewed runtime change". This is legitimate only
   for a jar you built in this task.
5. `python tools/stage_developer_v3.py` (re-pins the manifest and appends the new cache reference hash to
   the gate allowlists in `Start-950Server.ps1`, `Test-Bundle.ps1`, `Prepare-ClientCache.ps1`).
6. Prove the candidate:
   - staged `dist` scripts equal `programs()` output;
   - jar deployed = staged = built;
   - `Native950DeveloperConsole.verify()` passes against live cache + candidate overlay;
   - `.\Apply-PlayabilityUpdate.ps1 -CheckOnly` passes.
7. The **owner** runs `Update and Play.cmd`. Agents must not start/restart the game. Plain `Play.cmd` will
   break `;;dev` whenever the deployed jar's pins are newer than the installed cache.

Safety note: an overlay verification cache was once built with Windows junctions to `cache\*` and then
deleted with a recursive delete in PowerShell 5.1. Nothing was lost (all 198,656 original cache files were
re-checked against `flat-file.tar.gz`). Now remove junctions individually with
`[IO.Directory]::Delete(path,$false)` before any recursive delete. `cmd` is not on PATH in the
PowerShell tool.

The server's working directory is `OpenNXT/`, so relative sidecar files live under `OpenNXT/server-home/`
(e.g. `OpenNXT/server-home/developer-preview-overrides.txt`, `.../developer-preferences/`).

---

## 3. Preview regression history (so nobody repeats it)

- `51eb4c3` moved the NPC model into sized/clipped host `1448:8` (cropping), with the drag layer still on
  host6. Deployed as `e7db9bf`: drag observed "only as cursor", no rotation.
- `0b17e86` moved the drag hooks onto host8 itself. Rotation worked, but the model was **invisible during
  drag**, because the hook owner is drawn as the dragged component and it contained the model.
- 2026-09-26 (uncommitted): reverted to the unclipped host7 model (drag worked, overflow returned), then
  rebuilt clipping the native way (below). Several early "fixes" in this session were never deployed
  because of the pipeline lesson above; only the candidates after that lesson were real tests.

## 4. The working native contract (EVIDENCE + LIVE)

Decoded with `tools/library_trace_read.py` (normalised opcodes via
`protocol-analysis/ui-scripts-950-evidence.json`) and the component headers in
`temp/ui-capability-verified-export/`.

- **Interface 1448 static tree.**
  - Root `0` contains columns `3,5,7,9,11`.
  - Fill-mode children: `4⊂3`, `6⊂5` (buttons/actors column), `8⊂7` (text column), `10⊂9`, `12⊂11`.
  - Each has three children: `17–19⊂4`, `20–22⊂6`, `23–25⊂8`, `26–28⊂10`, `29–31⊂12`.
- **Frame templates.** Hosts 4/6/8/10/12 run onLoad `CS8409(2001..2005)` → `CS8411`.
  - Enum 7716 → struct 21137..21141.
  - Struct params: 3503 = frame root, 3504, 3506 = title, 3513 = **modal cover**.
  - For host8: root `8`, 3504 = `23`, 3506 = `25`, 3513 = `24`.
  - The template gives the cover a translucent type-3 child 0 and `0x08be(1, cover)`.
  - `0x08be` is the click-blocking flag. World map open `CS343` sets `0x08be(0, …)` and close
    `CS1898` sets `0x08be(1, …)` on window frames.
- **Native drag rotation (CS11619 / CS11620 / CS8479 / CS8480 / CS9644 / CS9319 / CS8481 / CS9620).**
  - `0x8a2(component)` is a **getter** returning a component. `0x353` takes **three** args:
    CS11619 does `0x353(0x8a2(dragLayer), -1, dragLayer)`; CS11620 clears with `0x353(-1,-1,layer)`.
  - Hooks: `0x815→8479` (drag), `0x732→8480` (drag complete); cursor 189 via `0x413`.
  - CS9644 addresses the model as `(component, child)`. child ≥ 0 uses `cc_find` (dynamic child);
    child −1 uses `0x5a2` (static component — Beasts `753:40`).
  - Yaw = (drag X `0x057d` − start) × 5 → `0x112` view.
  - The earlier "use −1 like CS11619" idea was **disproven**: our model is dynamic, so the real index is correct.
- **Retail reference.** Customisations `1311:343` (sized clip parent) contains the dynamic model and the
  drag layer `1311:362` (inset fill child). Beasts CS1165 is a timer auto-rotate, not drag.
- **Our working layout** (`tools/developer_console_v2_scripts.py`):
  - 21135 (NPC) / 21155 (player): `reserve()` a hidden host7 slot to keep text CC_CREATE indices
    contiguous; `viewport()` sizes host8 at (491,70,247,177) and hides 23/25; model =
    `CC_CREATE(host8, 6, 0)`.
  - 21136 / 21156 `drag_layer()`, which (a) and (b) together made drag work (LIVE):
    - `1448:24`: `cc_deleteall`, fill host8, unhide;
    - **(a)** `0x08be(0, 1448:24)` to clear the template's modal-cover click blocking;
    - **(b)** `0x353(host7, -1, 1448:24)`, a drag area larger than the layer (like host6 inside host5);
    - hooks `(8479/8480, 24, host8, 0)`, cursor 189;
    - hide host6 (the retired drag layer).
  - 21141 (sequence) and 21147 (zoom) target `(host8, 0)`.
- **Automatic framing** (`Native950DeveloperPreview.resolve`):
  1. exact Beasts struct (`enum 9031`, struct params 1347/3040/3041);
  2. Beasts framing shared by an identical-geometry definition (same model list + scale);
  3. `fallbackDistance(size)` × max(scaleX, scaleY)/128, using NPC definition opcodes 97/98
     (new public getters `NPCDefinitions.getScaleX/getScaleY`).

  A saved override replaces zoom only.

  Examples: Dagannoth Prime #2882 1350→1898 (scale 175/180), Chicken #288 650→325, KBD #2642 now shares
  #50's 1900/300. Man stays 650; Vorago #14416/#21304 resolve 3200 via shared Beasts framing.

## 5. Zoom and Save — current design and the open bug

- **Zoom (LIVE, keep).**
  - `bindZoom()` runs 21148 on the three zoom buttons (this replaces their server notify hook with the
    local 21147).
  - 21147 applies ±150 (or Reset = preferred default) locally, keeping the drag angles.
  - It then reads `0x526`, converts with native `tostring 0x086b` (grouped digits, e.g. `3,200`) and sends
    `__devzoom:<epoch>:<npcId>:<zoom>` via `0x77b`.
  - The server records it in `currentZoom` after checking the epoch, NPC id and bounds
    (`reportedZoom()` strips grouping).
- **Save** persists `currentZoom` via `Native950DeveloperPreviewOverrides` (sparse `npc:<id>=<zoom>`,
  versioned, atomic, refuses to overwrite an unreadable file), re-resolves the preview and re-binds Reset.
  It deliberately does no model redraw.
- **Open bug (LIVE-FAILED).** A real-cache lifecycle probe
  (`tools/Native950DeveloperPreviewSaveLifecycleAcceptance.java`) sends grouped reports through the
  real console, clicks the real Save, switches NPC, reselects, and **passes**: CS21135 carries the
  saved zoom and Reset is bound to it. Physically it still fails.
  - The live override file after earlier builds held only automatic defaults, except one sub-1000 value.
  - Next suspects:
    - whether the live client actually runs 21147's report branch (0x86b/0x267/0x77b; watch whether any
      `__devzoom` string reaches `Native950Interactions`);
    - whether the owner's reselect goes through the grouped representative row vs a variant id;
    - whether any render between zoom and Save rebuilds the model (epoch bump).
  - Add temporary server logging of received `__devzoom` strings before changing code.
  - Stale entries exist in `OpenNXT/server-home/developer-preview-overrides.txt` (e.g. Dagannoth Prime
    1350 from the first buggy build). Ask the owner before removing them.

## 6. Files changed in this checkpoint

- `tools/developer_console_v2_scripts.py` (21135/21136/21141/21147/21148/21155/21156, `drag_layer`)
- `tools/test_developer_console_v2.py` (VM models `0x8a2` getter, 3-arg `0x353`, `0x8be`, grouped
  `0x86b`; drag-layer and zoom-report tests)
- `tools/build_developer_console_950.py` (pins native drag handlers 9644/9319 too)
- `Ataraxia950/.../Native950DeveloperConsole.java` (bindZoom, `__devzoom` handler, reportedZoom, Save,
  4-button zoom row)
- `Ataraxia950/.../Native950DeveloperPreview.java` (framing tiers)
- `Ataraxia950/.../Native950DeveloperPreviewOverrides.java` (new)
- `Ataraxia950/.../cache/loaders/NPCDefinitions.java` (scale getters)
- `Ataraxia950/tests/modern947/Native950DeveloperConsoleTest.java` (grouped-digit parser test)
- `tools/Native950DeveloperPreviewOverrideAcceptance.java`,
  `tools/Native950DeveloperPreviewSaveLifecycleAcceptance.java` (new real-cache probes)
- Staging artefacts: `developer-console-950.properties`, `developer-console-scripts-950.json`,
  `playability-candidate-20260923.json`, gate allowlists in
  `Start-950Server.ps1` / `Test-Bundle.ps1` / `Prepare-ClientCache.ps1`

Line endings: the repo has mixed per-file EOLs and no autocrlf. Keep each unchanged line's original ending.
New files use LF, like their siblings.

Automated state at handoff: all 23 script tests pass, full `gradlew test` passes, and both real-cache
acceptance probes pass. The staged candidate verifies (`verify()` OK, installer `-CheckOnly` OK) and is
installed live.

## 7. Artaven upstream comparison and Stage B

Full technical report: `docs/SHNORKSCAPE-ARTAVEN-COMPARISON-20260926.md`.
Artaven's snapshot is at `C:\Games\UpdatedAuthorFiles\950OpenSource-2026-09-25\950OpenSource` (READ-ONLY).
Key verified findings:

- **Decoder.** Artaven's `IComponentDefinitions` supports modern widget types 10–16 and format 9/11 hooks. Adopted (Stage B Phase 2).
- **Opcode 124.** Native component value/selection. Wire contract independently verified against
  the client binary; Shnorkscape now decodes it (decode-only, no gameplay consumer added). Confirmed
  NOT the missing mechanism for Action Bar Equipment Binding — interface 365 components 19/20 (that
  feature's real transport) don't set the gating bit. The actual Equipment Binding bug
  (`Native950CombatPreferences.equipmentChanged()`/`boundBar()`, weapon changes not selecting the
  bar) remains open. Live semantics of the 10 real components that do set the bit are unproven
  without a packet capture. See docs/STAGE-B-PHASE3-COMPONENT-VALUE-ACTIONS-20260926.md.
- **Grand Exchange.** Real order book with optional `grandExchange.autoFill` system liquidity.
- **Souls.** Artaven clamps souls to capacity; ours intentionally does not. Keep ours.

### Stage B Phase 1 — ADOPTED (owner-approved, committed)

- **Region object-identity removal (adopted).** `WorldObject` inherits `WorldTile`'s coordinate-only
  `equals`, so `List.remove(object)` on `spawnedObjects`/`removedOriginalObjects` could erase a
  different object layer (wall/floor/furniture) sharing a tile. Added
  `Region.removeObjectIdentity(ledger, target)` (`removeIf(c -> c == target)`, Artaven's exact helper
  and comment) and converted all 12 call sites across `spawnObject` (×2) and `removeObject` (×4
  overloads) to use it with the already-resolved `spawned`/`removed` instance.
  **Deliberately deferred:** Artaven's companion change routing object definitions through
  `Native950ObjectClipping.resolve()` when `Cache.isFlatReadOnly()`. That's a separate, larger,
  unreviewed change; not adopted in this pass.
- **NPC viewport identity-set (adopted).** `Native950NpcViewport.nearbyNpcs()` used
  `!nearby.contains(npc)` (coordinate-based `List.contains`), which collapsed two distinct NPCs sharing
  a tile (companions, familiars, a target standing on another actor) into one candidate. Replaced with
  an identity set (`Collections.newSetFromMap(new IdentityHashMap<NPC, Boolean>())`).
  **Deliberately preserved:** our `!npc.isNative950Conjure()` guard in `describe()` was kept exactly as
  is. Artaven's version of this file removes that guard and adds `RestlessGhost`/`QuestNpcs` visibility
  filters that reference classes not present in our tree — neither was ported.
- **Regression tests (new, both proven to fail against the pre-fix code, then pass after restoring the
  fix — verified by literally reverting the two source fixes in place, rebuilding, confirming all three
  tests failed, then restoring and confirming the full suite passed again):**
  - `Ataraxia950/tests/modern947/Native950RegionObjectIdentityTest.java` — two tests, each placing a
    wall and a floor-decoration object (different `OBJECT_SLOTS` layers) on one tile in an insertion
    order chosen so an equals-based removal would target the wrong one first; asserts respawning/
    removing one layer never erases the other's ledger entry.
  - `Ataraxia950/tests/modern947/Native950NpcViewportIdentityTest.java` — registers two real `NPC`s
    with the same definition on the same tile into the same world region and asserts `nearbyNpcs`
    returns both by reference identity.
- Full `gradlew test` passes with these changes in place.

Stage B Phase 2 (interface decoder) is done: `docs/STAGE-B-PHASE2-COMPONENT-DECODER-20260926.md`.
Later phases begin only on the owner's explicit **CONTINUE TO ARTAVEN AUDIT**.
