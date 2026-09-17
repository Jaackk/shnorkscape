# 950 project audit and development tools

Scope: source inspection of the running native path, its legacy boundaries, local launch/build configuration, and representative tests. This is a subsystem audit, not an exhaustive review of every legacy class or a claim of complete in-game coverage. No boss content, object routing, client rendering, cache loading, protocol, or world-loop implementation was changed.

## Architecture

- `Ataraxia950` is a Java 8 gameplay engine. Gradle compiles `core`, `game`, `content`, `network`, `npc`, and `api` into one engine JAR. Large amounts of older Ataraxia content remain in that tree.
- `OpenNXT/src` is the Kotlin/Java frontend: login, lobby, protocol framing and cache service. `Ataraxia950Handoff.kt` admits a connection into the native Java world. `patches/classes` contains the compiled selected 950 overrides and precedes the runtime JARs on the classpath.
- `client/rs2client-vulkan.exe` and `client/rs2client.exe` are supplied native client binaries. Their renderer/engine implementation is not editable source in this bundle. OpenNXT is server/frontend source, not the client's rendering engine.
- `Native950World` owns gameplay mutation on a 600 ms world thread. Input, combat, movement and viewer frames are phased. `Native950Session` binds a real `Player`, hydrates legacy managers, dispatches native actions, publishes frames and checkpoints character state. The legacy world launcher must not run alongside this path.
- `Native950PacketDispatcher` is a compatibility boundary. Verified operations emit native packets; other legacy operations are rejected, counted or ignored. A legacy Java method existing does not mean the native client supports it.
- The paired flat cache supplies definitions, maps, models and scripts. Native catalogs verify selected assets and reject incompatible identities. `Start-950Server.ps1` runs cache preflight. Client startup preparation and JS5/cache transfer are already separate from gameplay.

## Current state

| Area | What the inspected native source supports | Limits / remaining work |
| --- | --- | --- |
| Player lifecycle | Real Player/managers; phased movement, local multiplayer admission, reconnect and character hydration | Legacy manager presence is not equivalent to complete content integration |
| Commands | Native local-development dispatcher plus legacy rights-based command paths | Existing local diagnostic commands use opt-in + native profile + loopback, without an account grant; new admin tools additionally require an explicit grant/admin rights |
| Combat | Native encounter ownership, approach/leash, attack cadence, HP, hit display, XP, drops, Slayer callbacks and respawn | Uses basic/classic-style formulas; NPC catalog explicitly refuses non-melee NPC styles; legacy boss scripts are not invoked by this loop |
| Player styles | `Native950CombatStyles` selects supported melee, ranged, magic and necromancy loadouts using current metadata; supplies checked by profiles | Not a complete RS3 ability/rotation system; support remains equipment/profile dependent |
| Abilities/adrenaline | Action-bar UI and energy display; `CombatDefinitions.specialAttackPercentage`, varp 679, resource decrease/restore APIs | No native ability execution/cooldown engine found in routed native actions. New energy commands do not add abilities |
| Prayer | Real Prayer point state, scheduled drain task, refill, bones/ashes and verified altar actions | Prayer/curses activation UI and native combat protection effects require further integration. Native hit calculation bypasses the legacy prayer damage pipeline |
| HP/death | Native damage and delayed Lumbridge recovery; inventory retained on native death | This is a development recovery policy, not retail death/reclaim mechanics. God mode now covers native hits and direct HP decreases |
| NPCs | Region-driven population, cache identity validation, movement, selected conversations/skilling and combat profiles | Visible spawn does not imply supported trade, dialogue, boss AI or combat |
| Objects/doors | Decoded clicks, range/map checks, pathfinding and selected native skill interactions; bank bridge | General doors, boss entrances and many travel objects are unported and rejected before legacy dispatch |
| Teleports | Native lodestones and movement/collision-aware local teleport commands | Every other legacy destination/instance must be checked separately; quick lodestone charges are explicitly unavailable |
| Inventory/equipment | Real containers, stack/slot checks, native equipment metadata, wear requirements, movement and persistence | Complex item attributes, charges and special effects need individual verification; a wearable item is not proof of its special mechanics |
| Banking | Native UI routing into actual bank storage; deposit/withdraw, quantities, notes/search-related paths and drag handling | Unsupported bank controls are explicitly reset/rejected; full retail presets/tabs are not established by this audit |
| Interfaces | Native bindings, settings, map, skills, production/dialogue/input, navigation, Hero/Loadout and information browsers | Many visible pages are shells or browsers. The existing exit/options UI can close immediately |
| Shops | Shop data parser and restock scheduling are bootstrapped | No general native shop trade path found in NPC/interface routing; loaded shop data alone does not establish usable shops |
| Regions/maps | Cache-backed collision/region loading and scene rebuilds, NPC region population | Map art is separate from gameplay; loaded boss maps do not imply encounters |
| Instancing | Native Dungeoneering has a small fixed room pool with ownership, objectives/guardians and recovery state | Not a general port of the legacy boss instance framework |
| Bosses | Boss information/reward browser; old encounter classes remain in source | Browser stats include placeholders; encounter participation/kill records and native mechanics are not generally implemented |
| Saving | Schema-4 native profiles, validated bounded sections, SHA-256 checksum, forced temporary write + atomic replacement; invalid saves fail closed | Captures selected state, not arbitrary legacy Player fields. Rights and new toggles are not persisted in this format. Changed checkpoints write synchronously on the world thread |
| Login | Local username-based creation/reconnect and duplicate-session admission checks | `AuthoritativeLoginProcessor` returns success without validating passwords. This is a local development login, not an Internet-ready account system |

Primary source locations: `game/com/rs/game/player/client/Native950World.java`, `Native950Session.java`, `Native950PlayerBinder.java`, `Native950SaveStore.java`, `Native950Interactions.java`, `Native950ActionRouter.java`, `Native950MeleeCombat.java`, `Native950CombatStyles.java`, `Native950NpcCombatCatalog.java`, `Native950Containers.java`, `Native950EquipmentTypes.java`, `Native950Lodestones.java`, `Native950Dungeoneering.java`, `Native950Bootstrap.java`; `game/com/rs/game/player/Prayer.java`, `CombatDefinitions.java`; `OpenNXT/src/main/kotlin/com/opennxt/login/LoginProcessor.kt`.

## Entrance diagnosis

`Native950Interactions.object()` resolves the clicked object, then permits only a native `skillOption` or option 2 on a valid bank. Otherwise it sends "That object action is not available yet". `Native950ActionRouter.whitelistedObject()` separately admits the bank chest for its legacy bridge.

The recorded Vorago click was object 17819 at 2972,3433. The legacy `ObjectHandler` has a branch opening `VoragoInstanceD`, but the native allow-list prevents reaching it. That dialogue creates/joins legacy boss instances, while the native combat loop deliberately does not run those encounter callbacks. Simply whitelisting this entrance would not establish a working Vorago encounter.

Object 127139 at 3104,3313 was also rejected in the prior logs, matching the reported War's entrance attempt. No corresponding War's Retreat gameplay handler was found in the inspected native/legacy routing. Its exact cache identity was not independently decoded in this task. No entrance behavior was changed.

## Commands

New commands are available to Jaxa through the launcher property `ataraxia950.devAccounts=jaxa`. They also accept existing administrator rights (`hasAdminRights`) or owner status, but still require the existing enabled native local-development connection gate. No automatic owner promotion or saved rights change was added. The new dispatcher rejects remote connections even for admins. Additional developer usernames can be explicitly configured as a comma-separated property value.

| Command | Behavior |
| --- | --- |
| `;;god` | Toggle independent damage immunity; enabling restores HP. Preserves unrelated existing invulnerability state |
| `;;infprayer` | Toggle prayer consumption suppression; enabling refills points. Covers scheduled, explicit and direct-set decreases |
| `;;infadren` | Toggle consumption suppression for the existing adrenaline/special-energy state; enabling fills to 100 |
| `;;adrenaline [0-100]` | Set energy; omitted value fills to 100. Rejects lowering while infinite energy is enabled |
| `;;heal` | Restore HP, prayer, run energy and drained skill levels |
| `;;max` | Set all 29 skills to this cache's individual caps and matching minimum XP; retains already higher XP; saved change |
| `;;coords` | Show tile, plane and region ID |
| `;;disengage` | Stop the native encounter and current action/path |
| `;;devstatus` | Show the three independent toggles |
| `;;devhelp` | Show concise command help |

Toggles are transient, default off on a new Player, and explicitly cleared on native combat detach/logout. There is no per-tick refill loop. Commands refuse dead, inactive, finished or locked characters. They do not provide a death/recovery bypass. `::` is accepted as well as `;;`.

Existing native tools remain: `;;item <id> [quantity]`, `;;npc <id>`, `;;obj <id> [type] [rotation]`, `;;tele <x> <y> [plane]`, `;;nxt level <skill ID> <level>`, `;;nxt status`, and the other `;;nxt` test destinations/effects/tools. Advanced existing diagnostics include `area`, `areascan`, `areastop`, `open`, `unhide`, `events`, `guideclose`, `cs`, `varbit`, and `varc`. These are diagnostics, not promises of gameplay support. Legacy command registration and `Commands.processCommand` also exist, with their own rights/controller checks; those legacy commands were not broadly certified for native use.

Reference inspection: local Darkan `MiscAdminCommands.kt` provides independent infinite-resource and max commands; the local Matrix Commands sources provide god/debug-object examples; the local Elveron876 administrator source uses the special-energy API for adrenaline. Only behaviors were referenced. No systems or implementation code were ported.

## Changes

- `Native950AdminCommands.java`: account-restricted command parsing, confirmations and existing-API operations.
- `Native950DevelopmentCommands.java`: routes the new commands; splits oversized `;;nxt` help into legal-sized messages.
- `Player.java`: separate transient development god flag; shared invulnerability query for legacy hits; native direct-HP protection.
- `Native950MeleeCombat.java`: immunity-aware damage before HP/hit/received-damage accounting; clears development modes on detach.
- `Prayer.java`: transient infinite flag and consumption guards, including scheduled drain.
- `CombatDefinitions.java`: transient infinite flag and consumption guards; retains special-attack cleanup and uses the existing energy setter.
- `Native950AdminCommandsTest.java`, `Native950MeleeCombatTest.java`: permissions, parsing, limits, toggles, real resource consumption, lethal native hits, direct HP damage, stat caps and logout cleanup.
- `Start-950Server.ps1`: explicit local developer grant for the existing Jaxa character.

## Risks and next steps

1. Establish version control and a reproducible backup/build baseline. This folder is not a Git checkout. Preserve the cache/client startup path and measure changes against it.
2. Add a small verified native door/travel registry, beginning with ordinary doors. Each entry needs its cache identity, option, destination/collision, interruption and return path tested. Keep boss entrances unavailable until the encounter pipeline is ready.
3. Decide the intended combat model. The present loop is basic combat, not full EOC. Implement ability execution, cooldowns, resource semantics, prayer effects and death policy as separate milestones before bosses.
4. Build one native shop end-to-end with safe stock/currency transactions, then expand using data.
5. Audit richer equipment attributes and save coverage before adding degradation, augmentation or complex rewards. Native saves currently serialize item IDs/amounts, not every legacy Item attribute.
6. Before inviting remote users, implement real authentication and a persisted server-controlled role model, then audit both native and legacy command entry points. The current username grant is appropriate only behind the unchanged local-only gate; passwords are not checked. Existing local diagnostics are broader than the new account-restricted tools.
7. Profile synchronous checkpoint writes and verbose combat logging under multiple players before making performance changes. Their costs exist in the current source; this task adds no polling/refill loops.

## Verification

Build and test outcomes are recorded separately in `ADMIN-TOOLS-VALIDATION-2026-09-17.md`. Automated tests exercise real Java state with simulated channels/entities. They are not manual in-game testing.
