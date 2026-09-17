# Ataraxia 947 migration

This isolated development copy preserves Ataraxia's gameplay source and introduces a native WIN64 **947-3** compatibility boundary. The original `C:\Users\developer\Desktop\Ataraxia-PS` is unchanged.

**September 8, 2026, settings tab extension:** Gameplay, Controls, Ribbon and
Accessibility are now routed to their existing native pages. Local editing and
server-dependent settings have different support requirements; see
[M7d settings tabs](notes/M7d-settings-tabs.md) for scope and acceptance status.

**September 8, 2026, regional music update:** the installed native session selects
947 music for 307 existing regions, changes tracks after movement, and handles
completion/unmute notifications without the old random replay timer. The HTTP
storage-trailer correction restored audible playback; the user confirmed music
and appropriate tracks in the zones tested. All 646 tests and the corrected
installed music/cache checks pass. See
[M7c acceptance](notes/M7c-region-music-acceptance.md) for runtime and listening
status, and the [current backlog](../MIGRATION-BACKLOG.md) for later milestones.
The equipment-era baseline and validation history below describe earlier work.

**Current stage: an opt-in 947 character with authoritative movement, backpack rearrangement, bank-chest and banker interactions, saved progress, and three bronze equipment items. Core native equipment acceptance passed, including collection, visible models, Remove, banking and re-equipping. The final layout package, automatic panel startup after a full server restart, fresh native equipment restoration and repeated resizing all passed.** Equipment unit tests and integration checks passed under Java 8 and packaged Java 25. OpenNXT serves assets and accepts the local login, then hands its game connection and live ISAAC streams to Ataraxia. `AstraNXT\Start-Ataraxia947.cmd` selects this path; `Start-Client.cmd` selects the earlier OpenNXT scene. The prior banker milestone passed automated NPC checks and native rendering, Bank/Talk to/Examine, item transfers and leaving and returning to view. The preceding chest bank and item-menu flow passed native-client acceptance. Backpack rearrangement passes protocol and real-container probes; its native mouse gesture remains unverified. Earlier native restoration after a full server restart passed; equipment save/reload passed fresh-process checks under Java 8 and packaged Java 25.

## Implemented

| Area | Implementation | Limits |
|---|---|---|
| Reproducible baseline | Java 8 / Gradle 4.9 build, corrected source settings, dependency resolution, JUnit tests, packaged application, exported runtime classpath | Full source compiles; the legacy world has not been started or functionally smoke-tested. |
| Revision identity | Immutable `ClientProfile`, transient per-connection `ClientSession`, explicit login entry points | 910 remains the existing gameplay path. Attempting to enter it with 947 throws before changing player or lobby state. |
| Native wire protocol | All 130 extracted incoming sizes, 18 verified outgoing packet types, ground/minimap walking, object/NPC/interface actions, item dragging, inventory updates, interface events, varbits and messages | A verified size is not a verified action. Unsupported packet meanings remain observable without being dispatched to 910 handlers. Player updates currently cover one local character; NPC updates cover one stationary banker. |
| Game transport | Authenticated OpenNXT handoff preserves live ISAAC streams and buffers pending TCP bytes with bounds and a timeout; `Native947GameTransport` accepts only verified output | Unsupported input is counted and never dispatched to old 910 handlers. |
| Character lifecycle | `Native947World` creates or restores a real Ataraxia `Player`, owns a 600 ms world thread, routes movement and ordered interactions through Ataraxia, and cleans up the character and banker on disconnect | One active local character; no password/account-security implementation, combat, or legacy world integrations. |
| Banker NPC | Isolated real Ataraxia NPC 494, cache-pinned definition and base animation set, visibility addition/retention/removal, Bank/Talk to/Examine input, collision-aware approach and existing bank storage; Collect adds the one-time bronze kit | Stationary banker at `(3217,3257,0)` only. Bank/Talk to/Examine, visibility and one-time Collect passed native acceptance. No combat, moving NPCs, other NPC content or legacy spawn import. |
| Local progress | Dedicated modern profile store; atomic save at creation and each changed world tick; restores position, exact backpack slots and compact bank groups; schema 2 adds 19 equipment slots and the claimed-kit flag while reading schema 1 | Local development profiles only. Invalid or incompatible saves refuse login without replacement; no legacy account import. Earlier full-server-restart verification passed; native schema-2 file conservation and Java 8/packaged Java 25 equipment restoration passed. Fresh native equipment restoration after a full server restart passed. |
| Backpack and bank | Real Ataraxia Inventory/Bank storage; backpack slot swaps; approach and use a real Lumbridge bank chest or the banker; deposit/withdraw 1, 5, 10 or All, deposit all backpack items, close and reopen | Three starter item types plus three bronze equipment types; one bank tab. Notes, placeholders, presets, arbitrary quantities and general item actions remain unsupported. |
| Bronze equipment | One-time kit from banker Collect; real Ataraxia Equipment storage; backpack Wield/Wear, worn-panel Remove, capacity/item conservation, native appearance and combined movement/appearance updates | Only sword 1277, square shield 1173 and med helm 1139. Builds, unit tests, Java 8/packaged Java 25 integration and core native interactions passed. Final bootstrap packaging, automatic panel startup, native restart/restore and repeated resizing passed. Other equipment, two-handed rules, statistics, requirements and combat are not implemented. |
| Modern cache access | `FlatCacheRepository` and read-only `Store`/`Index` compatibility adapters validate reference layouts, group CRCs and file boundaries; modern map/object data feeds collision; item/UI/script and banker definition/animation fingerprints pin the first interaction bindings | Only verified modern layouts are accepted. Unreadable collision regions block movement. The broader item/NPC catalog and old custom spawns still require migration. |
| Pinned evidence | `target-947.json` identifies the original/patched client, startup template and packet maps by SHA-256; `target-cache.json` pins all 45 reference tables | The startup template is provisional and derived from a 949 configuration; it is not an archived original 947 config. Cache pinning records the candidate snapshot, not completed gameplay compatibility. |

The copy includes source, resources, local dependency libraries and build files. Original player saves and production state were not copied. World startup data will be staged deliberately when the revised lifecycle is ready.

## Verify the milestone

**Equipment milestone: 150 unit tests passed** (105 Ataraxia and 45 OpenNXT), with no failures, errors or skipped tests. Both projects built and packaged successfully; the final resize-safe layout package reran all 45 frontend tests successfully. Unit tests cover capacity and full-backpack behavior, all-or-nothing collection, replacement conservation, invalid/stale state, schema-1 compatibility, schema-2 storage and appearance packet fixtures. The final packaged bootstrap passed automatic panel startup and native resize acceptance after a full server restart.

`Native947EquipmentSmoke` passed under **Java 8 and packaged Java 25**. It exercises encrypted Collect, all three wear operations, stale-request rejection, combined movement/appearance, Remove, gear deposit/withdraw/re-equip, actual container output and durable item conservation. Same-process and fresh-child-JVM reconnects restored the equipped profile with the correct first appearance, no duplicate kit and clean session removal. The existing `Native947WorldSmoke`, `Native947InteractionsSmoke` and `Native947PersistenceSmoke` regression probes also passed under Java 8.

**Core native equipment acceptance passed.** The banker granted its kit once and rejected a repeated Collect. Backpack Wield/Wear equipped all three items and rendered their models. Worn-panel Remove returned each item to the backpack; all three were deposited, withdrawn and equipped again. An independent native profile read confirmed schema 2 at `(3218,3257,0)`, with two carried coins, 998 coins/five logs/five shrimps banked, exactly one worn sword/shield/helm and `equipmentKitClaimed=true`. The original supplies match the schema-1 baseline. Current profile SHA256 is `E220595DA52C1F8586F320C8B8064060B5E6B4BCAC9CF3AF00EB61D36DB6B219`; the baseline hash is `A5EEF997D0D7ED6B635C539B020E13B1D00FF45A63A974D35D13FF1558BCB5F9`.

The panel's initial zero-sized wrapper was repaired in the active session. Saving the corrected geometry through cache scripts `8707(3)` and `8708(3,8)` made the panel survive a **maximized → small → maximized** native window cycle. The production bootstrap includes those calls. **Final packaging and native restart acceptance passed.** After a full server restart (PID 31236), manual sign-in at 15:09:05 restored `x` at `(3218,3257,0)` with all three equipment models and the worn panel visible automatically, without diagnostic layout injection. A small → maximized → small → maximized window cycle passed. Native Remove on helmet slot 0 returned it to backpack slot 1 and restored the hair; Wear returned the helmet and its model. Banker Collect still rejected a duplicate kit with chat. The final profile retained SHA256 `E220595DA52C1F8586F320C8B8064060B5E6B4BCAC9CF3AF00EB61D36DB6B219` and all original quantities. Client and server were left in the world with all three items equipped and the bank closed. Evidence is in `../logs/equipment947-native-proof.txt`, `equipment947-live-success.out.log` / `.err.log` and `equipment947-live-profile-final.json`; the first-check logs preserve the original kit/bank flow. The following **127-test and native banker evidence belongs to the preceding NPC milestone**.

NPC milestone verification, September 6, 2026: both production builds passed with **85 Ataraxia and 42 OpenNXT tests, 127 total**, and no failures, errors or skipped tests. Existing `Native947WorldSmoke`, `Native947InteractionsSmoke` and `Native947PersistenceSmoke` regression probes passed under Java 8. Historical movement, chest and persistence results below remain unchanged.

`Native947NpcSmoke` passed under Java 8 and the combined packaged Java 25 runtime. It verifies a fresh JVM loading a remote restored character independently of the banker region, walking from the normal starting area, visible NPC re-addition during a scene rebuild, NPC Bank/Talk to/Examine, forged-index and out-of-view rejection, cancellation, visibility removal/re-addition, saved-profile reconnect and NPC/player cleanup. Both probe processes exited successfully; production code did not change after the tested build.

Native banker acceptance passed after the user signed in. Banker 494 rendered as the expected human model with Bank, Talk to, Collect, Load Last Preset and Examine menus. Examine displayed **"Banker."**, and Talk to displayed the welcome/help response. Left-click Bank opened the saved **998 coins, five logs and five shrimps** with **two coins** in the backpack. Depositing one coin produced 999 banked/one carried; withdrawing one returned to 998 banked/two carried.

Walking north automatically closed the bank. At `(3215,3276,0)` the player was beyond the 15-tile view; on returning, the server re-added the banker at player tile `(3215,3272,0)`. The native banker was visible again at `(3214,3259,0)`. Clicking Bank routed around the counter to `(3217,3258,0)` and reopened the saved 998 coins/five logs/five shrimps with two backpack coins. An independent save-file read confirmed the final tile and every quantity. The bank was closed and the client/server left running. Live logs contain no runtime errors or interaction rejections. Results are preserved in `../logs/npc947-native-proof.txt` and `../logs/npc947-live-success.out.log` / `.err.log`.

NPC input/cache evidence is in `../OpenNXT/data/prot/947/generated/native947-3/verified/NPC_INPUT.md` and `NPC_ACTIONS-and-banker-cache.txt`; the reproducible inspection script is `../OpenNXT/tools/inspect_native947_banker.py`.

From PowerShell in this directory:

```powershell
.\Test-Migration.ps1 -Offline
```

The command checks the pinned client artifacts, compiles and packages Ataraxia, runs the migration tests, and validates the installed flat cache against its pinned reference tables. It also runs isolated map, world and interaction probes against the real cache, without opening network listeners or a native game window. Omit `-Offline` on a machine that still needs to download dependencies. Use `-CachePath` if the same cache snapshot is stored elsewhere.

Previous movement milestone, validated September 6, 2026: **26 Ataraxia JUnit tests passed** and the complete source compiled and packaged. The actual Java region probe loaded **9 Lumbridge regions containing 27,262 objects** and found a valid route. The world probe rejected a step into a real cache obstacle, accepted encrypted walking on a real Player, walked 47 steps around obstacles, rebuilt the scene at `(3221,3256)`, rejected out-of-range movement, and disconnected/reconnected cleanly. The same probe passed under Java 25 using the combined OpenNXT distribution.

The earlier cache probe decoded all **45 reference tables** describing **544,044 groups**, with representative map/item/NPC/object/interface reads. These are targeted validations, not a full scan of world gameplay compatibility.

The executable's file version **947-3** and its login wire build **947.1** are distinct. The live login header carries `00 00 03 b3 00 00 00 01`; the handoff checks that wire build. Native packet evidence remains named `native947-3` after the executable it was extracted from.

That movement milestone's frontend build passed **35 tests**, for **61 tests total** across both projects. In the native client, the disposable `x` character completed lobby and game login, rendered Lumbridge, walked through the courtyard and continued north to `(3219,3273)` using ground and minimap clicks. This crossed the scene-update threshold and the starting map region's northern boundary; the client continued rendering and moving normally. The live log confirmed Ataraxia ownership and authoritative movement, with no handoff or world errors. The successful run is preserved in `../logs/ataraxia947-live-success.out.log` and `.err.log`.

Interaction milestone verification, September 6, 2026: **56 Ataraxia tests and 38 OpenNXT tests passed, 94 total**, with no failures, errors or skipped tests. Both projects compiled and packaged successfully. The checks cover literal native packet bytes, bounded action queues, actual container conservation, stale/forged requests, stack and non-stack behavior, full inventory handling, backpack swaps and bank proximity/cancellation.

Persistence automated verification, September 6, 2026: **73 Ataraxia tests and 40 OpenNXT tests passed, 113 total**, with no failures, errors or skipped tests. Both projects compiled and packaged successfully. `Native947WorldSmoke` and the existing `Native947InteractionsSmoke` regression probes passed under Java 8.

The new `Native947PersistenceSmoke` passed under Java 8 and the combined packaged Java 25 runtime, including a fresh child JVM. It verified exact backpack slots, saved tile and bank restoration, no additional starter grant, separate profiles, rejection of corrupt-profile admission and disconnect on an active checkpoint failure. The failure check preserved the existing save and withheld the failed mutation's completed tick. The final packaged Java 25 rerun also passed. A separate saved-position probe rejected both missing-map and blocked-floor locations. Logs are listed below.

Native persistence acceptance passed after a full server stop and restart. Before stopping, local profile `x` stood at `(3215,3256,0)` with one coin in the backpack and **999 coins, five logs and five shrimps** in an open bank. A fresh game login restored the same tile and backpack, started with the bank closed, and displayed **"Your local progress has been loaded."** The saved file was byte-identical before and after the restart, as recorded in `../logs/persistence947-native-restart-proof.txt`. Opening the actual chest showed the saved 999/5/5 bank contents. A native one-coin withdrawal then succeeded, leaving two coins in the backpack and 998 coins/five logs/five shrimps in the bank.

An independent final save-file read confirmed `(3215,3256,0)`, backpack slot 0 holding two coins, and bank groups of 998 coins, five logs and five shrimps. The runtime identified the attachment as a restored profile and reported no errors, exceptions or save failures. The bank was closed and the client/server left running for play.

The native bank results below describe the preceding interaction milestone.

`Native947InteractionsSmoke` also passed against the real cache under **Java 8** and the combined packaged **Java 25** runtime. Its encrypted input routes a real Ataraxia Player to the cache bank chest, exercises deposits, withdrawals, bank closure and backpack rearrangement, and rejects invalid IDs, incorrect prediction forms, duplicate drags and repeated claims against compacted slots. Both runs finished with exactly **1,000 coins, five logs and five shrimps** conserved and the temporary character removed on disconnect. These results establish automated and packaged-runtime verification.

Final native-client bank acceptance also passed. The disposable `x` character walked to chest 79036, selected **Use**, and routed into reach at `(3215,3256,0)`. Deposit-all moved all 1,000 coins, five logs and five shrimps into the bank. **Withdraw-5 Logs** from the middle slot succeeded even though native compaction sent item 315 in that button packet; **Withdraw-10 Shrimps** moved the five available; **Withdraw-All Coins** restored 1,000 coins. Depositing one coin, closing and reopening the bank retained that coin for the same session, and a left-click withdrawal restored the full coin stack. The final closed-bank view contained all starter items. Deposit-5 Logs also passed in an earlier live check.

**Examine Coins**, bank-open messages and transfer messages were visible with chat varbit 18797 set to 1. The final live logs contain no runtime errors or interaction rejections: `../logs/ataraxia947-bank-live-success.out.log` and `.err.log`.

Backpack drag remains a specific acceptance gap: native packet semantics and real-container swaps pass the encrypted smoke, but the native mouse gesture was not verified. The available Sky automation cannot control how long the mouse button stays held, which prevents a reliable test of the client's drag threshold. This is not recorded as a live drag success.

Outputs:

- `build/equipment947-build.log`: successful engine build and unit tests. `../OpenNXT/build/equipment947-package.log` and `../OpenNXT/build/equipment947-layout-package.log` and the final `../OpenNXT/build/equipment947-final-package.log`: successful frontend tests and packaging. The installed engine JAR has SHA256 `3FB0A769815E421A9E64D26C51B298D9BB25B544F5CDD1F7C8158377A261BFEC`.
- `build/equipment947-smoke.log` and `build/equipment947-packaged-smoke.log`: successful Java 8 and packaged Java 25 equipment integration, including fresh-process restoration.
- `build/equipment947-Native947WorldSmoke.log`, `build/equipment947-Native947InteractionsSmoke.log` and `build/equipment947-Native947PersistenceSmoke.log`: successful Java 8 movement, existing interactions and persistence regression probes.
- `../logs/equipment947-native-proof.txt`, `../logs/equipment947-live-first-check.out.log` / `.err.log` and `../logs/equipment947-live-profile.json`: passed native collection, models, Remove, banking, re-equipping and conserved schema-2 profile. The proof also records the final package and native restart/resize acceptance. Final evidence is in `../logs/equipment947-live-success.out.log` / `.err.log` and `../logs/equipment947-live-profile-final.json`.
- `build/appearance947-evidence.log`: completed read-only native/cache appearance inspection, not a gameplay acceptance test. Reproduce with `../OpenNXT/tools/inspect_native947_appearance.py`; equipment definitions/UI evidence comes from `inspect_native947_equipment.py`.
- `build/npc947-build.log`: successful NPC milestone Ataraxia production build and 85 tests.
- `../OpenNXT/build/npc947-package-tests.log`: successful frontend production build and 42 tests, for 127 combined unit tests.
- `build/npc947-probe-build.log` and `../OpenNXT/build/npc947-package.log`: final test-probe build and combined distribution packaging; production code was unchanged.
- `build/npc947-world-regression.log`, `build/npc947-interactions-regression.log` and `build/npc947-persistence-regression.log`: successful movement, existing bank/backpack and persistence probes under Java 8.
- `build/npc947-smoke.log` and `build/npc947-packaged-smoke.log`: successful NPC real-cache integration probes under Java 8 and combined packaged Java 25.
- `../logs/npc947-native-proof.txt` and `../logs/npc947-live-success.out.log` / `.err.log`: successful native rendering, interactions, banking, visibility return and final saved-state evidence.
- `build/reports/tests/test/index.html`: test results.
- `build/cache947-report.json`: reference-table fingerprints and representative map, item, NPC, object and interface files.
- `build/install/ataraxia-947/lib/`: packaged application and libraries.
- `build/runtime-classpath.txt`: complete classpath for isolated migration tools.
- `build/interaction947-build-final.log`: final Ataraxia build, tests and packaging.
- `../OpenNXT/build/interaction947-package-final.log`: final OpenNXT build, tests and packaging.
- `build/interaction947-smoke-final.log` and `build/interaction947-packaged-smoke-final.log`: successful real-cache interaction probes under Java 8 and packaged Java 25.
- `build/persistence947-build.log` and `../OpenNXT/build/persistence947-package.log`: persistence builds, tests and packaging.
- `build/persistence947-smoke.log` and `build/persistence947-packaged-smoke.log`: persistence probes under Java 8 and packaged Java 25, including fresh-process restoration.
- `build/persistence947-Native947WorldSmoke.log` and `build/persistence947-Native947InteractionsSmoke.log`: movement and interaction regressions after persistence integration.
- `build/saved-position-probe/probe-final.log`: rejection of missing-map and blocked-floor saved positions.
- `../logs/persistence947-native-restart-proof.txt`: matching save-file hashes around the native full-server-restart check.
- `../logs/persistence947-live-before.out.log` / `.err.log` and `../logs/persistence947-live-after.out.log` / `.err.log`: native creation, banking and restoration before and after the server restart.

For just the build, run `Build.ps1`. Its explicit Java 8 selection avoids the system's newer default JDK. Build dependencies stay in the sibling `.gradle-ataraxia` directory. An existing Java 8 JDK is required.

## Run the integrated client

From the parent `AstraNXT` directory:

```powershell
.\Stop-Server.ps1
.\Build-Ataraxia947.ps1 -Offline
.\Start-Client.ps1 -Backend ataraxia947
```

Alternatively, double-click `Start-Ataraxia947.cmd` after building. Close any existing RuneScape window before changing backends. Sign in with local profile `x` / `x`, select the lobby's World tab once, wait for World 1, then choose Play Now.

The launch scripts record the selected backend and modern save path, and reject accidental backend changes or reuse of an older temporary Ataraxia process. Use `Stop-Server.ps1` first when upgrading or switching. The server remains restricted to loopback. The integrated runtime uses OpenNXT's Java 25 and newer Netty libraries, with Ataraxia compiled separately on Java 8.

Successful world entry records `Ataraxia 947 now owns game session` in `../logs/server.err.log`. Movement records in `../logs/server.out.log` identify the Ataraxia player and its authoritative coordinates. Disconnect removes the live character and banker and frees the session slot. Reconnecting with the same local name restores the last committed position, backpack, bank, equipment and kit-claim flag; the banker is recreated as world content and is not part of the profile format. Equipment restoration passed fresh-process checks under Java 8 and packaged Java 25; the final package also passed fresh native equipment restoration after a full server restart.

## Local profiles and saving

The integrated launcher sets `OPENNXT_PLAYER_SAVE_PATH` to the absolute workspace directory `Ataraxia947/data/modern947/players`. The handoff requires this setting and the dedicated `modern947/players` suffix; the store creates its directory. Direct server invocations must provide the same explicit setting. The earlier OpenNXT scene and standalone world/interaction probes remain transient and do not use these profiles.

A new profile is saved before gameplay begins. Each changed 600 ms world tick then atomically replaces its save before flushing that tick's client output. Schema **2** contains the world position, all 28 backpack slots including holes and order, compact bank item groups, **19 equipment slots** and the **equipmentKitClaimed** flag. Schema-1 local profiles remain readable: they retain position, backpack and bank and start with empty equipment and an unclaimed kit. The next changed save writes schema 2. This does not migrate legacy Java-serialized accounts. The native `x` profile migrated with original supplies intact and all three equipped items saved. Reconnect and server restart load the last committed state; equipment restoration passed fresh-process checks under Java 8 and packaged Java 25, and after a full server restart with a fresh native login. Starter supplies are granted only when creating a missing profile; an existing empty backpack and bank remain empty.

Local names are case-insensitive. Underscores and repeated spaces normalize to spaces; the canonical name must contain 1–12 ASCII letters, digits, spaces or hyphens and at least one letter or digit. This selects a local profile, not a secured account. Passwords are not stored or protected by this feature, and the server remains loopback-only with one active character.

Corrupt or incompatible profiles, unsupported item state and unavailable saved map tiles refuse world entry without resetting or overwriting the save. If a save fails during play, the session closes before that tick's output is flushed; reconnect resumes the last successfully committed tick. The format is separate from legacy Java-serialized accounts. Original `Ataraxia-PS` saves are never read or migrated.

## Try the backpack and bank

Every newly created local profile starts with **1,000 coins, five logs and five shrimps** in its backpack. Restored profiles keep their saved items. With the bank closed, drag an item to another backpack slot to rearrange it. Examine displays the verified item name. The three bronze items support Wield/Wear as described below; general item-menu actions remain unimplemented.

Walk north from the Lumbridge starting courtyard to the ground-level bank chest at **`(3215,3257,0)`**, object **79036**. Select **Use**. The server routes the character into reach, checks the actual cache object and opens the bank only after arrival. A new walking request cancels the approach or closes an open bank.

The **Banker 494 at `(3217,3257,0)`** offers another route into the same saved bank. Choose **Bank** to approach, **Talk to** for a short chat response, or **Examine** for the name. These prior native interactions and item transfers passed. The player must walk around the counter before banking; the server verifies an adjacent collision-safe interaction tile. Walking cancels the approach. Forged indices, NPCs outside the last published view, wrong-plane or out-of-range targets cannot open the bank. The banker is added within 15 tiles, retained while visible, removed outside that range and synchronized when the scene rebuilds. **Collect** routes into reach to offer the one-time bronze equipment kit; native collection and repeated-claim rejection passed. **Load Last Preset** remains unsupported.

Inside the bank, left-click an item to deposit or withdraw **one**. Right-click for the supported **1, 5, 10 and All** choices. The deposit-all button transfers the whole backpack. Close the bank with its close button; using the chest again shows the retained contents. Stackable coins and separate logs/shrimps follow their verified item definitions. Capacity limits and item counts are checked on the world thread before any transfer.

The integrated backend saves item, bank and equipment changes with the world tick. Notes, placeholders, bank tabs/presets, arbitrary quantity entry, other NPC services and general item actions remain unavailable. Equipment is limited to the three verified bronze items. Original player saves are never loaded or modified.

The native client runs item prediction scripts before sending button and drag packets. In the configured All Items bank view, a partial withdrawal reports the original item ID; an exhausting withdrawal compacts the actors and reports the next bank item's ID, or `48447` when the exhausted slot was last. The adapter strictly checks this predicted ID using the amount that can actually move, including backpack capacity, rather than the requested quantity alone. Exhausted bank-inventory actors report `-1`, and backpack drags report the two IDs after the actor swap. These forms are checked against authoritative storage, quantities and supported components. Every further bank item transfer against a slot whose identity changed earlier in the same world tick is rejected. The packets contain no original item ID or operation sequence: these checks establish consistency with the current predicted state, but cannot prove which earlier screen state motivated a delayed claim. The current saves remain local development profiles; this limit must be revisited before multiplayer or broader item/economy integrations.

Bank refreshes also reset the occupied-slot player variable, select All Items, and configure predicted coin withdrawals into the backpack. Chat varbit 18797 is enabled so examine and transfer messages appear. The source cache and native executable are unchanged.

## Try the bronze equipment implementation

Builds, unit tests, Java 8/packaged Java 25 integration and core native equipment checks passed. Final layout packaging, automatic panel startup, resizing and fresh native reconnect after a full server restart passed. Keep **three free backpack slots**, then choose **Collect** on the banker. Native NPC option **4** grants one **bronze sword 1277**, **bronze square shield 1173** and **bronze med helm 1139** together. Insufficient room leaves the entire kit unclaimed. The claim is saved once per profile, including profiles created before schema 2; repeated Collect cannot create another kit.

With the bank closed, right-click the sword or shield in backpack **1473:5** and choose **Wield**, or choose **Wear** on the helmet. Each uses verified inventory option **2**. Worn equipment uses container **94** and interface **1462:31**; **Remove (option 1)** returns an item to the backpack. The bootstrap uses verified cache scripts **11145** and **13268** to size and position the wrapper 232 pixels left of the backpack. Scripts **8707(3)** and **8708(3,8)** save the repaired panel to the current layout and selected custom preset so resizing preserves it; both active-session and final packaged startup/resize checks passed. Current item IDs, slots, capacity and interface state are checked before mutation. Real `Player.getEquipment()` storage holds the items, and replacing/removing items preserves displaced equipment. A full backpack blocks removal when no item can be returned. All three items passed native bank deposit/withdraw checks.

The immutable `Native947Appearance` template emits modern item IDs plus **`0x800`**, keeps the selected cache's 19 logical wear positions, hides hair slot 8 under the med helm, and uses sword animation set **2584** instead of unarmed **2699**. Initial login encodes restored equipment before sending the first player appearance. Each changed tick sends one appearance update, combining it with movement when necessary. Removing the helm restores hair; removing the sword restores the unarmed stance. The implementation does not call the old appearance serializer or enable combat.

Exact binary/cache evidence is recorded in `../OpenNXT/data/prot/947/generated/native947-3/verified/APPEARANCE_EQUIPMENT.md` and `EQUIPMENT_CONTENT.md`, with reproducible inspection tools in `../OpenNXT/tools`. Completed native acceptance results are tracked above.

## Remaining work

1. Extend visibility to a second player and moving NPCs before enabling existing NPC spawns; the single stationary banker now passes native acceptance.
2. Extend the verified equipment catalog, rules, requirements and statistics. The three-item milestone passed native interactions, the final package, full server restart/restoration and repeated resizing. Other item families and general item actions need separate handlers; existing 910 dispatchers remain unavailable because they emit incompatible messages.
3. Complete the broader bank milestone with supported bank extensions and the native backpack drag gesture. The chest/banker/transfer/save and scoped equipment flows do not provide full legacy banking parity.
4. Migrate validated gameplay services onto the modern world loop. Full legacy `Player.start`/`LoginManager` startup is deliberately not used until its 910 output and external integrations are separated.

## Key source locations

- `game/com/rs/game/player/client/`: client profile/session boundary.
- `network/com/rs/network/protocol/modern947/`: revision-specific wire formats and immutable walking, object, NPC, interface and drag actions.
- `network/com/rs/network/modern/`: Netty integration and enabled ISAAC implementation.
- `game/com/rs/cache/modern/`: validated read-only flat-cache reader.
- `game/com/rs/tools/modern/CacheMigrationProbe.java`: cache validation without world/account startup.
- `game/com/rs/game/player/client/Native947Containers.java` and `Native947Interactions.java`: authoritative backpack/bank/equipment operations, kit collection and target validation.
- `game/com/rs/game/player/client/Native947Appearance.java`: immutable modern body template and the three verified equipment appearances.
- `game/com/rs/game/player/client/Native947NpcView.java`: session-owned banker registration, published NPC visibility and live target validation.
- `npc/com/rs/game/npc/NPC.java`: isolated native NPC factory that avoids legacy combat and world startup.
- `game/com/rs/game/player/client/Native947Save.java` and `Native947SaveStore.java`: bounded modern profile state and atomic persistence.
- `game/com/rs/game/player/client/Native947InteractionsSmoke.java`: encrypted real-cache interaction probe without a native window.
- `game/com/rs/game/player/client/Native947EquipmentSmoke.java`: equipment integration probe, passed under Java 8 and packaged Java 25 including fresh-process restoration.
- `../OpenNXT/src/main/kotlin/com/opennxt/net/login/Native947CacheContent.kt`: pinned modern item, interface, script, quantity and banker bindings.
- `tests/modern947/` and `tests/cache/`: fixtures and integration checks.

Verified native evidence remains in `../OpenNXT/data/prot/947/generated/native947-3/verified`. New findings are recorded in `INTERACTIONS.md`, `INVENTORY_INTERFACES.md`, `inventory-cache-bindings.md`, and the separate hashed `inventory-interfaces/manifest.json`. The protocol tests compare the imported size table against that extraction and use literal encrypted packet fixtures. The original pinned target manifests were not replaced by these additions. No remote accounts or external services are needed for these checks.
