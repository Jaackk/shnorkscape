# SHNORKSCAPE Engineering Handover

## Start here

**2026-09-20 disposable application gate:** PID35192 V4 source fixture validated
against the pinned912-ID schema and six bootstrap controls. See
[Disposable application gate](NATIVE-950-DISPOSABLE-APPLICATION-GATE.md).
The new `layoutfixture stage|apply` command is hard-restricted to layoutgate2,
uses native protocol setters plus8741(6), and never runs on login. Source fixture
stays local. Application/readback and visual acceptance are still pending; this
is NOT production durability. Preserve editor20258e4 and Jaxa.

**Latest workspace checkpoint (2026-09-19):** read
[Native acquisition proof and durability design](NATIVE-950-WORKSPACE-ACQUISITION-AND-DURABILITY.md).
V3 acquisition is live-proven; Custom1 and active8 match on all712 readable fields
after Save and after Load. Strict B-to-C equality is not claimed: five paired
fields changed. Jack clarified C is post-Load; later editor testing happened after
C. Do not attribute those changes to a repeated save without new evidence. Native
editor `20258e4` remains protected. No durability/replay has been deployed. The
older baseline and opcode-persistence discussion below are historical, not the
current implementation instruction. Do not redo reader or opcode investigations.

SHNORKSCAPE is a local, revision-950 RuneScape 3 private-server development project. It combines the original 950 native Vulkan/OpenGL client with OpenNXT's login/cache/JS5 service and an Ataraxia Java gameplay world. The target is a smooth, usable EOC combat sandbox that retains native revision-950 UI behaviour instead of replacing the client with imitation interfaces or speculative server-side geometry.

## Project lineage: 950 client on a converted 910 server

The Rune-Server release from which this project descends explicitly describes itself as a **"950 revision client and cache running on a converted 910 server."** The gameplay source is originally Ataraxia 910. SHNORKSCAPE must therefore not be treated as a clean native-950 server implementation: many surviving defects can be 910-era server behaviour that was only partially adapted to the 950 client.

For every UI, protocol, settings, or combat-presentation defect, explicitly ask: **Is SHNORKSCAPE still carrying a 910-era implementation that conflicts with the revision-950 client?** Prefer this three-way comparison:

1. Original Ataraxia 910 behaviour and ownership.
2. The current SHNORKSCAPE conversion and its compatibility changes.
3. The actual revision-950 client/cache contract.

Use this model especially for workspace/login/logout, ServerpermVarcs, action bars, Prayer, Magic, Revolution, settings, interfaces, combat presentation, varps/varbits, and native session lifecycle. A working 910 mechanism is valuable evidence of what SHNORKSCAPE inherited, but it is not authority for revision-950 IDs or protocol details.

Jack's priorities are, in order: client smoothness, authentic native UI behaviour, stable sessions and saves, polished EOC combat, and evidence that matches what he can see in the Vulkan client. Passing unit tests alone are never proof of visible client success.

**Current safe source and deployed baseline:** `f4179f84bfff748c783ed07ad5c3e38ea5c718d1` (`f4179f8`). It is the rollback of the rejected workspace implementation `baf38f3`. Treat `baf38f3` as a failed experiment, not current behaviour. The full healthy 215-entry ServerpermVarc bootstrap, action bar, bindings, tested keybinds, yellow manual-press feedback, combat work, Bug Test/Combat QA, and Item Browser are present in the safe baseline.

The central remaining blockers are native workspace persistence, incomplete native EOC presentation/UI paths, and content that must not be guessed from older revisions. Do not restart the client/server or edit Jaxa while doing read-only investigation.

## Repository and runtime map

The workspace root is `C:\Games\950OpenSource`.

| Path | Responsibility |
| --- | --- |
| `Ataraxia950/` | Java 8 gameplay engine. Its `game/com/rs/game/player/client/` package contains the native-950 adapter, session/world owner, combat, UI, saves, diagnostics, and Item Browser. |
| `OpenNXT/` | Kotlin/OpenNXT source. It provides login, HTTP, JS5, cache and the handoff into the Java native world. |
| `patches/classes/` | Generated Kotlin override classes. It is first on the runtime classpath and is ignored by Git. Rebuild it after engine-JAR changes. |
| `OpenNXT/runtime/lib/ataraxia-950-1.0-UNTRACKED.jar` | The deployed Java engine JAR, ignored by Git. A running JVM loads this artifact, not the source tree. |
| `cache/` | Flat OpenRS2 cache 2691 / 950.1. Revision-specific authority for definitions, scripts, assets and interface data. |
| `client/` | Local native executables, including `rs2client-vulkan.exe`. Do not casually patch or replace them. |
| `players/modern950/players/` | Checksummed modern local character profiles. Jaxa lives here; this directory is ignored. |
| `client-state/` | Isolated client storage. Its presence proves only that local storage exists; it does not prove workspace persistence ownership. |
| `logs/bugtest/`, `logs/combatqa/` | Ignored, session-scoped diagnostics and screenshots. |
| `backups/` | Ignored pre-edit/deploy snapshots made by the standard backup script. |
| `docs/` | Repository-grounded reports, protocol notes and this handover. |

At handover time, `origin/main` equals `f4179f8` and the worktree was clean. The rollback JAR SHA-256 was `38D40ADE10CFA8CBCDA722127351850484F70ECB65F96CD9CDDABBA81115B465`. Runtime PIDs and hashes are transient; do not treat this document as proof that a later JVM still loads that artifact.

### Build, launch and stop

- `Backup-BeforeEdit.ps1 -Task '<description>'`: verifies the expected GitHub remote, creates `backups/pre-edit-<timestamp>/`, copies private saves/runtime artifacts, commits staged source if needed, pushes, and checks the remote SHA. Run it before risky edits and again before deployment snapshots.
- `Build-Ataraxia950.ps1 -Tasks test`: Java 8 Gradle test suite. The script intentionally uses the bundled JDK 8 and offline Gradle cache.
- `Build-Ataraxia950.ps1 -Tasks jar -Deploy`: builds the engine and copies the exact JAR to `OpenNXT/runtime/lib/`. It does **not** rebuild Kotlin overrides.
- `Build-950Lobby.ps1`: compiles the selected Kotlin overrides with the bundled Java 25 toolchain against the deployed engine JAR, into `patches/classes`.
- `Start-950Server.ps1`: validates the paired cache, sets local native environment variables, starts the OpenNXT/Ataraxia server and writes `logs/server.pid.json`.
- `Start-950Test.ps1 -Vulkan` / `Play.cmd`: starts a healthy server if needed, refuses to reuse a JVM older than the deployed engine JAR, waits for `jav_config.ws`, then starts Vulkan.
- `Stop-950Test.ps1` / `Stop.cmd`: identity-checks `logs/*.pid.json` before stopping only this workspace's client/server.

Never assume source HEAD equals runtime. After changing Java source: test, build/deploy JAR, rebuild Kotlin overrides, stop the tracked JVM, start again, then check the running PID record, `jav_config.ws?binaryType=2`, and TCP port `43650`. `Start-950Test.ps1` deliberately rejects an older JVM when the JAR timestamp is newer.

## Safety, Git and deployment contract

For a coherent feature slice:

1. `git status`, confirm `origin` is `https://github.com/Jaackk/shnorkscape.git`, and push current work.
2. Run `Backup-BeforeEdit.ps1`; preserve Jaxa and runtime files outside Git.
3. Make one narrow change. Do not fold unrelated cleanup into a risky UI/combat change.
4. Run focused tests, then the appropriate broader Java/protocol/cache tests. Rebuild Kotlin overrides when Java API or login code changes.
5. Commit and push a descriptive checkpoint.
6. Before deploy, snapshot Jaxa again and record a semantic/byte hash as appropriate.
7. Build/deploy exact source, rebuild overrides, cleanly stop, start, verify HTTP/JS5 and save integrity.
8. State clearly whether validation is structural, cache/protocol verified, or visually live-verified by Jack.

Do not use `git reset --hard`, overwrite player files, or reverse unrelated user changes. Do not commit runtime JARs, profiles, bug-test logs, screenshots, cache, or client state. Do not weaken Windows security controls to run the client.

## Jaxa and player-save protection

Modern native profiles are owned by `Native950Save`, `Native950SaveStore`, and `Native950PlayerBinder` under `Ataraxia950/game/com/rs/game/player/client/`. Profiles are account-keyed, bounded, checksummed and atomically written. They hold selected game state such as position, backpack, bank, equipment, skills, vitals, appearance, settings/action bars and skill progress; they are not a general serialization of all legacy `Player` fields.

Jaxa's profile is private under `players/modern950/players/`; do not publish its filename or content. Backup scripts snapshot the directory. Hash comparison is useful before deployment, but some legitimate lifecycle fields such as `lastLogin` can change after a real login. When a hash changes, compare decoded sections/semantics before claiming corruption. Never casually normalize, regenerate, migrate, or resave Jaxa just to make a test convenient.

Strict state validation exists because a prior Overload path created legacy Torva/degradation metadata that the current native item state could not represent; it caused a reconnect/load event after the first dose. The repair was to keep modern self-damage out of legacy degradation/equipment callbacks, not to weaken validation.

## Sources of authority

Use this research order before expensive native reverse engineering:

1. Exact current revision-950 client/cache behaviour.
2. Original Ataraxia 910 implementation, to identify inherited or partially converted server behaviour.
3. Project Undercut 949.1 as a near-revision native-client/server reference.
4. Elveron876, Vernox, and other working local RSPS implementations as behavioural or architectural references.
5. Public Rune-Server and RSPS source references.
6. Targeted binary reverse engineering only for the remaining concrete gap.

Use reference projects as architectural and behavioural evidence only; verify every revision-specific interface, component, script, variable and packet against revision 950 before implementation. For bugs inherited from the conversion, document the three-way diff: **Ataraxia 910 -> current SHNORKSCAPE -> revision-950 expectation.**

For 950-specific claims, use the actual 950 cache, the original native 950 client, OpenNXT descriptors, verified packet traces, cache scripts/configs/varbits/animations, and current protocol tests. The pinned original WIN64 client hash cited in the workspace handoff is `fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`.

`Documents/RSPS/NocturneServer/Elveron876` is valuable as a behavioural/architecture reference: EOC target ownership, ability lifecycle, queues, cooldown groups, effects and combat feel. It is **not** authority for 950 packet/component/interface IDs, scripts, varps/varbits, animations, workspace protocol, or shortcut encodings. Earlier failures came from copying older assumptions: old shortcut packing, old interface components, sprites mistaken for animations, and guessed scripts/redraw calls. Use Elveron to ask “what should this mechanic do?” and 950 evidence to answer “how does this client contract express it?”

## Current known-good and protected behaviour

### Live verified by Jack

- Renderer/client performance is excellent; performance regressions are bugs.
- Main action bar is visible again, existing bindings are intact, and tested action-bar keybinds work.
- The native yellow manual ability-press square/highlight works and is specifically valued. It means manual input was registered. Do not remove or redesign it.
- Native action-bar dragging, server-side binding, bar switching, and cooldown circles have had substantial live coverage, though some visual shortcut representation remains historically fragile.
- Combat against `;;dummy` is stable after the prior disconnect regression repair; `;;almighty` stays active during combat.
- Item Browser search, Recent, visible-item identity and left-click Give 1 work in the clean fixed-grid version.

### Important distinctions

Do not conflate manual input acknowledgement (yellow square), a queued ability, and a cooldown overlay. The manual queue is server-side and bounded/revalidated; desired native queued-ring feedback is not proven. Cooldown circles use a verified native path. Action-bar UI surfaces include main interface `1430` and its Powers mirror `1436`; these are distinct. Shortcut packing was verified as `type << 17 | id << 4`, not older four-bit packing.

### Structurally/cache/protocol verified but still requiring visual confirmation

- Some ability animations, graphics, projectiles, buff display and action-bar visual state have correct server-side/cache-backed paths but remain incomplete or unconfirmed in the real Vulkan client.
- Native Prayer panel input/presentation, Magic/autocast visual selection, native Revolution checkbox completion, status icons, combat-ready stance, and workspace persistence are not safe to call finished.

## Combat Alpha architecture and audit

Primary code is in `Native950MeleeCombat`, `Native950AbilityCatalog`, `Native950CombatAnimations`, `Native950CombatBuffs`, `Native950CombatStyles`, `Native950Revolution`, `Native950Prayer`, `Native950Magic`, `Native950Hits`, `Native950Interactions`, and supporting cache/item/profile classes. Start with `docs/ABILITY-FOUNDATIONS-20260917.md`, `docs/PROJECT-AUDIT-2026-09-17.md`, commit `5244050`, and the current tests under `Ataraxia950/tests/modern947/`.

The intended model is one shared manual/Revolution execution pipeline: request -> validate target/range/style/weapon/resources/cooldown/GCD -> queue one legal request when appropriate -> execute -> reserve timing/animation ownership -> schedule hits/effects -> revalidate delayed work -> clean up on death, target loss, teleport or combat cancellation. Auto-attacks must not overwrite active ability/channel animation but must resume without arbitrary freezes. The native world is the 600 ms authoritative tick owner; do not add background combat loops or every-tick UI polling.

Recent audited work includes explicit channel cadence rather than trusting zero-duration sequence metadata; bounded area follow-ups for Chain, Dragon Breath, Tsunami, Hurricane and Meteor Strike; native bleeds; server-owned Berserk and Death's Swiftness timed effects; Overload/degradation boundary repairs; explicit damage-unit policy removing forced `x10` quantisation; ranged Almighty ammunition handling; and server-side Revolution eligibility/order. These are source/test verified; their visual presentation is not automatically retail-complete.

The ability audit covers 31 supported abilities and records style/tier, requirements, adrenaline, cooldown/GCD, hit cadence, effects, animation/graphic/projectile evidence and confidence. Do not add abilities for count alone. Improve supported definitions using the matrix and paired cache evidence. Known presentation risks include idle stance between attacks, no verified Berserk animation/status display, Death's Swiftness presentation, status icons, uncertain graphics, and the queued-ring indicator.

### Revolution

The server-side engine uses the active bar in order and shared manual validation/cooldown/GCD/resources; it excludes utility abilities such as Surge. `;;revo` is a development control. Earlier attempts at the native Combat Settings transaction using guessed preferences/scripts were not live-successful. Do not rebuild the server engine merely because the native checkbox is still unresolved.

### Prayer

Underlying state uses real Prayer identity, activation/mutual exclusion, points/drain, altar restore, protection/offensive bonuses and cleanup. `;;infprayer` and `;;almighty` interact with resource consumption. The native visual panel/input path was not fully established; do not claim it is solved because a handler or old component ID fires.

### Magic/autocast

Selected spell is a shared persisted combat state, with developer `;;spell` only as a tool. Air Strike/Bolt/Blast/Wave/Surge, rune checks/consumption, development infinite runes, target/range and hit timing have server-side support. Autocast selection/presentation and complete native spellbook routing need live/cache evidence. Some magic appearance is embedded in animations; missing separate graphics is not automatically a defect.

### Native/legacy boundary

The most dangerous recurring defect class is current native-950 combat accidentally entering old Matrix/legacy paths. Audit all crossings involving hits, target state, equipment degradation, jewellery/death callbacks, potions, graphics/projectiles and Prayer. A legacy method existing is not permission to call it. `Native950PacketDispatcher` is a compatibility boundary: verified operations emit native packets; unverified legacy calls should be rejected/countable rather than fabricated.

## Bug Test and Combat QA

`;;bugtest` toggles a session-scoped lightweight flight recorder. `;;bug <description>` writes a marker, relevant state snapshot and best-effort game-window screenshot. Telemetry is JSONL in `logs/bugtest/session-<timestamp>-<player>/`, includes correlation IDs and meaningful interface/action-bar/ability/combat/settings/item transitions, and is designed to be fail-open: diagnostics and screenshot failure must not affect gameplay. It avoids every-tick logging and uses bounded asynchronous work. Do not log chat/authentication content or commit these logs.

`;;combatqa [stop|status|reset|cleanup]` manages Combat QA under `logs/combatqa/`. It records combat-oriented telemetry, automatic storyboards/screenshots, anomaly detection, stale-capture rejection and manual Bug Test correlation. It uses bounded queues and cleanup to protect runtime performance. The completed high-value QA session is `logs/combatqa/session-20260919-141048-308-jaxa/`; its report/review is committed around `ba5a771` and later fixes such as AoE commits `fcc32ea`, `11d58ce`, `641039a`, `6a80a2f` supersede some findings. Do not confuse later `workspace-opaque` Bug Test sessions with that combat QA evidence.

Every newly added player/admin/development command must be added to `;;commands` in the same change with a concise description and the existing readable category styling. Current high-value commands include `;;commands`, `;;dummy [1-5]`, `;;almighty`, individual infinite-resource toggles, `;;revo`, `;;bar`, `;;spell`, `;;bugtest`, `;;bug`, `;;combatqa`, `;;items`, `;;uilayout`, and `;;comp`; inspect `Native950AdminCommands.java` for exact supported syntax rather than extending this list from memory.

## Item Browser: frozen

`;;items` is a developer-only browser in `Native950ItemBrowser`. It uses a native grid with cache-backed search/ranking, a single authoritative rendered snapshot for slot identity, Recent history, safe grants, Testing Kit, scrolling, and left-click Give 1. The current clean result cap is 40: broad unlimited result expansion visually overlapped native models and was reverted. The 40-slot Combat Alpha Testing Kit is verified by `Native950ItemBrowserAcceptance` against paired-cache definitions and Combat Alpha equipment recognition.

**Do not alter Item Browser UI or interaction architecture unless Jack explicitly asks.** Prior regressions came from parallel Java list versus native container identity, treating script `150` as a simple option script when it initializes larger native state, dynamic/fixed-grid experiments that distorted models, fake pagination assumptions, and quantity-menu experiments exposing cache-authored shop options. Native shop wording/extra options are less important than preserving the working grid, identity and safe Give 1 path.

## Workspace persistence: the critical unresolved work

### Current status: evidence-blocked

The opcode-14/136 acknowledgement hypothesis was tested in production at commit `5c2f40d`: the server acknowledged the exact initial 1291-byte, 215-record upload with empty opcode `136`, then Jack moved Backpack under Bug Test. The move was visually confirmed, but no subsequent opcode-14 delta arrived. The acknowledgement did not unlock workspace uploads on this lifecycle. The temporary proof implementation was removed; do not retry it or infer persistence from it. Native workspace persistence remains unresolved pending genuinely new evidence.

Jack can move, resize, dock, tab, select and close native panels during a session. After logout/relogin or client restart, the arrangement resets and default windows stack/overlap. The desired result is native persistence of positions, resize state, docking, tab groups, active tabs and visibility, not hard-coded coordinates.

Read `docs/NATIVE-950-WORKSPACE-PERSISTENCE-HANDOFF.md` before changing anything. It is the authoritative reverse-engineering handoff.

### Established evidence

- Isolated `client-state/Jagex/RuneScape/...` works and `Settings.jcache` exists, but controlled workspace changes did not establish that file as workspace storage.
- A healthy native login requires the full 215-entry ServerpermVarc bootstrap. Commit `66f8024` omitted 92 entries overlapping old `ILayoutDefaults`; it caused a black/non-rendering world, missing ribbon/workspace and an empty `MANAGEMENT WINDOWS` shell. It was reverted. Never blanket-filter those 92 values.
- Controlled payload comparisons proved client `33/125` are delta-like input/pointer traffic and `54/65` are ordinary pointer transport (also emitted by an empty-world click). They are not workspace persistence. Do not resume random opcode fishing.
- Cache scripts `8707`, `8708` and central serializer `8709` operate on native panel state and permanent client variable/varbit domain. Geometry/state fields are clamped to 12-bit ranges; parent varc `3296` contains varbits `19037..19040`. This points to native permanent-variable ownership, not a custom coordinate packet.
- Original 950 client opcode `14` is a variable-length permanent-variable upload. The observed 1291-byte shape is `u8 completion + 215 * (BE u16 id + BE i32 value)`. It batches roughly 1500-byte chunks, throttles sends, retains an in-flight batch and awaits the empty server acknowledgement opcode `136` before releasing it.
- Opcode `136` is proven by matching client pending-vector/completion-field usage, not copied from an old revision.

The expected lifecycle is: healthy static defaults -> client changes native workspace -> validated account-bound permanent-variable upload -> durable server persistence of final batch -> server `136` acknowledgement -> later login overlays saved values onto all healthy defaults. IDs must be cache-defined permanent integer client variables; never use static 215 IDs as the only allowlist. Persisted data must be bounded, checksummed/atomic and account-isolated. Never remove bootstrap defaults.

### Failed production implementation: do not reapply

`baf38f3279c2df134f6a6291ca0d846afc904a08` attempted that lifecycle: it added opcode 14 decoding, server opcode 136, a per-account `Native950ServerpermStore`, session accumulation, login overlay and structural tests. Java tests and Kotlin compilation passed. **Live result: client froze/stuck in lobby and could not enter the game.**

Rollback `f4179f84bfff748c783ed07ad5c3e38ea5c718d1` reverted only that production implementation and records: “structurally tested but live client froze in lobby; production implementation reverted pending fresh-agent investigation.” The handoff document and protocol discoveries remain valid. A future agent must diff `baf38f3` against the handoff and the login progression/lobby contract before attempting a new implementation; do not cherry-pick it or assume the structural tests covered the live handshake phase. No new workspace experiment should be deployed until a specific model accounts for that lobby stall.

Normal Logout has also historically appeared to reconnect Jaxa into the world rather than reliably returning to lobby. It may be relevant to native save lifecycle, but it is not proven as the workspace root cause. Keep it documented and instrumented rather than asserting causation.

## Other failed/dangerous approaches

- Never use old component IDs, scripts, varps/varbits or protocol rows just because Elveron/Matrix did. They can be well-formed and still mean something else on 950.
- Do not use icon sprite IDs as animations. Ability tooltip sprite parameter `2802` and actual weapon-family animation resolution parameter `2915` are distinct.
- Do not use animation frame duration as the sole combat cadence: modern sequences can report zero duration, which collapsed channel hits.
- Do not reintroduce the legacy target-panel bridge; a prior stance experiment destabilized/disconnected sessions.
- Do not use guessed redraw/status scripts or whitelist unverified scripts merely to remove a visual symptom.
- Do not make native grids larger through script `150` or send unlimited Item Browser results; it distorts the native grid.
- Do not route unknown native object/boss actions directly into legacy controllers. Vorago/War's etc. need an evidence-backed native integration, not a whitelist.

## Where to resume

1. Preserve `f4179f8` and obtain a clean live baseline from Jack before edits.
2. For workspace persistence, review `baf38f3` only as a failed diff against the authoritative workspace handoff and trace the exact lobby-stage interaction it disturbed. Do not redo packet fishing or 92-varc filtering.
3. For combat, use the existing Combat QA session and ability matrix to select one evidence-backed presentation or mechanic issue. Keep action bar/keybind/yellow press feedback protected.
4. Make one checkpoint at a time, with explicit manual validation requests where the native client is the only authority.

## Useful Git landmarks

- `f4179f8`: current safe rollback baseline.
- `baf38f3`: failed workspace implementation; do not reapply.
- `ab7f5dc`: authoritative workspace persistence handoff documentation.
- `4b411f1`: restored required native action-bar visibility.
- `66f8024`: unsafe 92-varc omission experiment; reverted by later recovery.
- `5244050`: offline ability matrix/combat architecture audit.
- `a3b1935`, `ba5a771`: Combat QA and its review.
- `3ca0b8e`, `63f58f3`, `5d7804a`, `4d3c642`: Item Browser identity/recovery/revert history.
- `543f5ea`, `adf6a02`, `feb44f3`, `1608c40`, `869bd64`, `07a9382`, `674a525`, `5daa7a2`: important Combat Alpha correctness checkpoints.

This project succeeds when the source, wire protocol and actual client all agree. Preserve the working experience first; make the next change only when it has a real 950 evidence trail.
