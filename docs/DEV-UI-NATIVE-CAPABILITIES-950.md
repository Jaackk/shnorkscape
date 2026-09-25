# Revision 950 native Developer Console capabilities

Research only, 25 September 2026. Source baseline `f2bf37230a3658c37ff4ebb1ffad4d2c3f949563`. No console, gameplay, cache, executable, save, installer or staged candidate changes. No deployment or server restart.

## Decision

**A substantially better native console is feasible. Keep the working backend and provisionally keep 1448 as its window manager. Recompose its content rather than inherit the Customisations screen.** Use native scrolling, a search field, a restrained category list, visual rows and one details/action pane. Prove one full NPC preview in isolation before committing the design to an embedded model pane.

The strongest new evidence is **Beasts 753:40**: exact-950 CS3869 sets an NPC definition, its animation and its model view independently. Photo booth 549:19 and marketplace routines corroborate the same contract. Empty server `sendNPCOnIComponent` / `sendIComponentModel` methods are implementation gaps, **not evidence that Vulkan cannot display models**.

The mockup is a design direction, not an existing native template. Its overall organisation is achievable with native primitives; universal object thumbnails, a complete effects simulator, arbitrary scenery previews and a moving translucent placement ghost are not established. Do not promise those to make the next mockup look complete.

## Evidence and confidence

The accompanying [derived evidence](../protocol-analysis/dev-ui-native-capabilities-950.json) records 28 targeted interfaces, 52 component headers, 42 decoded scripts, SHA-256 identities, call relationships, state operands and native handler addresses. Raw cache/client files remain local and uncommitted.

Classification used below:

- **PN — PROVEN NATIVE:** exact-950 native program/component/client evidence establishes the primitive. This does **not** mean our proposed composition has passed Vulkan testing.
- **PS — PROVEN WITH SMALL SCRIPT EXTENSION:** a bounded composition of established primitives is sufficient for the stated narrow behaviour; no such extension was installed in this pass.
- **PU — PLAUSIBLE BUT UNPROVEN:** a remaining contract or presentation question prevents a firm claim.
- **NW — NOT SUPPORTED / WRONG TOOL:** the proposed route is inappropriate or no supported contract was found in that route. This is not a claim that the entire client lacks the capability.

All new preview composition remains **LIVE TEST PENDING**. Existing reported physical successes—Heal, NPC search, chosen-tile placement—remain evidence for the backend/input path, not for arbitrary previews. The latest console appearance/UX is user-rejected and must not be described as accepted.

Provenance discipline:

- Numeric component/script evidence below comes from this paired 950 cache and its original Windows client, SHA-256 `fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`.
- Names such as `mtxmgt`, `boothpreview`, `marketplace_preview` and `bslay_boss_info` were discovery leads from original 949 cache-2670 Gamevals. Their **950 numeric behaviour was examined separately**. These names have not all been promoted to verified 950 runtime symbols.
- Existing audited symbols for 1448/1477/517/623 retain their existing provenance in `Ataraxia950/resources/native950/gameval-lookup-950.tsv`. No runtime lookup entries were added.
- Hex opcodes below are the repository's **normalized** script identities, not values to write directly into 950 bytecode. Use the audited mapping in `protocol-analysis/ui-scripts-950-evidence.json`.
- The legacy Java `IComponentDefinitions` decoder misreads modern format-11 hooks, including impossible hook arrays and incomplete model records. Its hook output was rejected. This report uses common headers, exact serialized argument records and decoded scripts instead.
- CS3503 contains unmapped wire opcode `0x0582`. The research decoder retains it as UNKNOWN with a one-byte operand; complete instruction-count/end-boundary agreement was checked. Its semantics were not guessed or added to the production map.

## Capability matrix

“Embed” describes the next implementation route, not live acceptance. Complexity is relative to the existing backend.

| Feature | Native source / exact 950 identity | Proven? | Embed in console? | Secondary UI? | Complexity | Principal risk | Recommendation |
|---|---|---|---|---|---|---|---|
| Full NPC model | Beasts **753:40**, CS3869; booth **549:19**, CS10882; marketplace CS11613 | PN | Model primitive yes; composition pending | Yes | Medium | Model framing, transforms, invalid definitions | First preview proof; one selected model |
| NPC animation | CS3869/11061/11613; normalized `0x086a`, active-component `0x067f` | PN | Same model component | Yes | Medium | Wrong skeleton/sequence; same-ID replay | Start with verified idle + one attack |
| Local player model | Booth **549:20**, CS1608 `0x0769`; dynamic Customisations preview | PN | Yes, composition pending | Yes | Medium | Appearance/override state leakage | Use preview-owned state |
| Arbitrary other player appearance | Existing local-player path is not an arbitrary appearance-upload API | PU | Unresolved | Unresolved | High | Model-source/appearance ownership | Do not promise arbitrary remote profiles |
| Equipment / outfit preview | Customisations **1311:343**, CS3503/18274; marketplace **1495:15**, CS17960/18120 | PN for native supported previews; PU for arbitrary whole loadouts | Possible | Recommended first | Medium–high | Inventory/override dependencies | Isolate preview from real equipment |
| Individual item | Native item component operation `0x03c0`; bank **517**; shop **1265**; CS11054 | PN | Yes | Optional | Low–medium | Item icon versus worn/inventory model distinction | Use icons immediately; prove larger 3D view separately |
| Generic raw model | Customisations CS6446: dynamic type 6 + `0x0098` + `0x0112` + `0x067f` | PN | Yes | Good asset-lab primitive | Medium | Raw model is not a complete object definition | Label raw-model inspection accurately |
| Complete scenery object | No verified loc-definition-to-preview-component contract in audited paths | PU | Unresolved | Prefer isolated proof | High | Multi-model shapes, transforms, contouring | Keep metadata and placement; do not fake object fidelity |
| Animation lab | Separate model/sequence setters above | PN for sequence selection; PU for full Play/Stop/Loop/Replay controller | Possible | Recommended | Medium | Loop policy, replay reset, missing FX | Build only after a verified actor/sequence pair |
| Graphics / spotanims | Marketplace **1495:69–78** model components are not proof of a general spotanim API | PU | Unresolved | Asset lab, only after proof | High | Particles, attachments, lifetime | Do not call a raw mesh a working effect |
| Projectile simulation | Interface model setter has no demonstrated trajectory/target/time contract | NW for model-pane-only route | No proven route | Controlled world test would be separate | High | Misrepresenting timing/impact | Keep real projectile diagnostics separate |
| Native scrolling | Beasts **753:28/31**, CS11074 → CS7791; generic CS31 | PN | Yes; clipping/hit testing must move together | No need | Medium | Split visual/text/hit hosts; too many children | Replace pagination with bounded native scrolling |
| Native context menus | CS10324, `CC_SETOP 0x083c`, operation sentinel `-2147483644` | PN | Yes, existing console uses primitive | No | Low | Duplicate authored labels, stale row identity | One meaningful set of operations per row |
| Item / sprite icons | 517 item grid; type-5 components; Beasts **753:39**, CS3869 param3038 | PN | Yes | No | Low | Sprite IDs are not NPC IDs | Verified item/sprite/category icons |
| NPC thumbnails / heads | Booth **549:16** head path, CS10882; full-model primitive above | PN for supported heads/models; PU for cheap universal thumbnails | Yes, with limits | No need | Medium | Missing head models, many animated rows | Static known icon fallback + one detailed model |
| Object thumbnail catalogue | No universal object-ID-to-icon mapping established | PU | Unresolved | No benefit yet | High | Invented assets; expensive mesh loading | Category icon unless verified item/sprite representation exists |
| Placement cursor / tile selection | Existing native target-source arm, wire `0x072c`; client opcode85 | PN | Existing world workflow | World view | Low | Stale source/slot/session | Preserve proven placement ownership |
| Precommit footprint / anchor | Cursor exists; scalable world footprint overlay contract not proven | PU | World, not UI preview | World view | Medium | No hover coordinates supplied to server | At minimum show dimensions; prove marker independently |
| Translucent world ghost | No verified non-committing ghost renderer found | PU; real-NPC mousemove spawning NW | No | World view | High | Visibility/collision/ownership side effects | Defer; never repeatedly spawn/delete actors on hover |
| Travel browser | List shell **908**, frame hook CS8420; lodestones **1092**; map **1421/1422** | PN for component patterns; PS for simple curated list/actions | Yes | Map optional | Medium | Hardcoded map destinations and input ownership | Reuse console list/details for travel, map as secondary |
| Simple / Advanced toggle | Native show/hide + text + buttons already used by console | PS | Yes | No | Low | Technical clutter, lost selection | Default Simple; preserve state when toggling |
| Home / favourites / recent | Existing action/selection registry + native row primitives | PS | Yes | No | Low–medium | Full redraw after every click | Small local presentation updates, server-owned actions |

## 1. Customisations: what the preview actually is

### Window versus content

**1448 is the management window**, not the wardrobe model viewer. It has 32 static components and no static type-6 model. Content is mounted/built into hosts including 3/5/7. Title construction is CS8289 through frame struct **21301**, param **3506**, dynamic title child **14**. Child 3 is not the title. Native framing/layout also uses params3548/3557 and font60. Existing CS8286 readiness integration must be preserved rather than bypassed by extra delays.

**1311 contains the Customisations content.** It has 698 static components, also no static type-6 model: **the preview is created dynamically**. Absence of a static model is therefore not absence of native preview support.

Key content identities:

| Component | Role established by header/scripts; symbolic label is a 949 lead |
|---|---|
| 1311:343 | Preview parent, hash85918039; static width300, relative-height mode |
| 1311:344–360 | Background layers/sprites; these do not establish arbitrary lighting controls |
| 1311:362 | Preview drag layer; serialized CS4213 hook, later drag bindings |
| 1311:371 | Variant button layer used by CS18117 |
| 1311:657 | Animation-listener host used by CS6445 |
| 1311:136/137–143 | Search background/input/caret/display/cancel/input-ownership layers |
| 1311:340/341 | Header/list scrollbar leads; native scrolling confirmed independently in Beasts |

### Model, animation and refresh chain

CS6455 resets/checks selected-category state, calls CS6473/15002/7487/7488, then **CS6443 → CS3503** for the preview and additional category helpers. CS3503 finds or deletes existing dynamic preview children, creates a **type-6 child under 1311:343**, sets its dimensions/position, chooses player/pet-related model branches and sets camera/sequence state.

CS18274 reads preview metadata from a selected struct. It uses params such as2535–2541,3752–3756,4267,4295–4298,5166,8546,8677/8678,9201; these are numeric relationships, not newly invented Gameval names. It calls CS6443 for the actor route and **CS6446** for additional raw-model layers. CS6446 takes14 integer arguments and explicitly creates a type-6 child, sets raw model, six view values, sequence and conditional recolour/retexture operations. This is real native compositing, but not evidence that arbitrary scenery definitions can be faithfully assembled by copying one model ID.

State read/written in the traced preview chain includes:

| Namespace / ID | Observed use |
|---|---|
| varc779 | Render-animation/BAS-related selection; CS1608 and CS3503 derive a sequence through `0x089c` |
| varc1963 | Selected Customisations category/mode; branch value5 selects the pet-oriented route |
| varc1968 | Special preview branch; changes view setup and animation |
| varc2692 | Selected NPC-related identity in pet branch |
| varc2693 | Sequence applied when present |
| varc2699 | Submode affecting positioning/category behaviour |
| varc3756 /6714 | Further selected preview/struct state |
| varbit673 | Category state read by CS6455 |
| varc2250 | Native search text reset/used by CS15001 |

These are **screen-owned state**, not recommended new global console variables. Copying the entire wardrobe lifecycle would entangle progression, cosmetics and shared native variables.

### Camera, rotation, animation and limits

CS3503/6446 use normalized `0x0112` to set six model view values. Marketplace uses the explicit-component counterpart **`0x02aa`**. CS11613 reads struct params5044/2541 and8666 for framing; CS17960/18120 also support positional DB table **254**, field selectors1040384 through1040464. Native routines therefore already handle per-asset framing and exceptions. Do not apply one universal zoom to a butterfly, human and Vorago.

CS11619 attaches CS8479/8480 to a preview drag layer and cursor189; CS11620 removes the hooks/cursor. CS8479 then installs active drag updates. Beasts CS1165 reads existing model view values and updates rotation. Rotation can therefore remain client-side rather than round-trip through the world tick.

Sequence setters are separate from the model source. **CS6449 and CS6451 are empty return-only programs in this cache.** Their suggestive position in the older Customisations animation chain is not a functioning animation-lab implementation. Prefer the actual model/sequence primitives and verified marketplace/Beasts routes.

Background sprites are established. Dedicated per-preview light rig, free camera, camera collision and a complete projection/zoom range are **not** established here. The six-value model view is not the world camera API. No arbitrary appearance upload or arbitrary projectile/spotanim pane was proved.

Open/close must retain the existing window readiness and input cleanup, release drag/search hooks and retire the preview before returning to gameplay. Do not reuse all wardrobe callbacks just to borrow its model pane.

### Requested preview subjects A–M

This classification concerns the native routes established above, not a claim that every subject works through an unchanged Customisations screen.

| Subject | Classification | Limit |
|---|---|---|
| A. Local player appearance | PN | Local-player model source; preview isolation still required |
| B. Arbitrary player appearance | PU | No arbitrary appearance transport/builder proved |
| C. Equipped items | PN for the native local appearance | Copying any proposed equipment set into an independent appearance remains PU |
| D. Individual wearable | PN for native supported customisation previews | Arbitrary wearable substitution without touching the account remains PU |
| E. Weapons | PN within supported appearance/item routes | Worn model and inventory model are different assets |
| F. Arbitrary NPC models | PN for valid definitions via the full-body setter | Not an unchanged wardrobe option; new composition pending |
| G. Arbitrary NPC animations | PN for sequence selection | Compatibility and correct semantic binding are per actor |
| H. Arbitrary player animations | PN for sequence selection on a player model | Sequence playback alone does not include combat effects |
| I. Object models | PN for raw model; PU for complete loc | Assembly, shape and transform fidelity unresolved |
| J. Graphics / spotanims | PU | No general spotanim-to-component contract established |
| K. Projectiles | NW through the ordinary model pane alone | Trajectory/target/impact simulation is a different mechanism |
| L. Pets/familiars | PN for supported pet/NPC preview routes | Not every familiar definition/variant has been tested |
| M. Model + animation | PN | Independent setters are established; multi-effect scene composition is not |

## 2. NPC previews: strongest contract and alternatives

**Beasts CS3869 instructions469–489**:

1. Read selected boss struct from **varc4485**.
2. Read **param1347** and apply it to **753:40** with normalized `0x02be`.
3. Read varc4484, derive the default sequence with `0x089c`, and apply it with **`0x086a`**.
4. Read params3041/3040 for framing and set the six view values with `0x02aa`.

**CS11061** also accepts an NPC identity and animation as inputs and applies them separately to753:40. **CS1165** provides rotation logic. This is substantially closer to the requested boss browser than a dialogue chathead.

**Photo booth CS10882** independently reads enum9590, shows549:19 and applies its result through the same NPC setter. CS1608 applies local-player appearance to549:20. The head equivalents are549:16/17; a head model is not a full NPC model and some NPCs have no usable head asset.

**Marketplace CS20604** selects between1495:15/16/17 and routes toCS17960,17958,18695 or616. CS17958 calls **CS11613(shopStruct,npcId,renderAnimationId,componentHash)**. CS11613 applies NPC and derived sequence separately, then framing; the caller's pet/commerce schema is incidental to the underlying setters. It is not a good reason to import purchase or entitlement state into `;;dev`.

Native client corroboration:

| Primitive | Normalized opcode | Native wrapper / operation body | Observed contract |
|---|---|---|---|
| Full NPC | `0x02be` | `0x1401e34a0` / `0x1401d2620` | Explicit component + definition ID; model-source kind6; no native boss-list whitelist in this setter |
| Sequence | `0x086a` | `0x1401e37a0` / `0x1401d2a70` | Explicit component + sequence; model field0x224; invalidate when changed |
| Active component sequence | `0x067f` | `0x1401e3800` / same body | Dynamic-child counterpart |
| Raw model | `0x0098` | `0x1401e2d80` / `0x1401d1df0` | Raw model-source kind1 |
| Local player | `0x0769` | `0x1401e36e0` / `0x1401d2970` | Local player model-source kind5 |

**Answer:** valid arbitrary NPC definitions have a native full-body route. Attack/block/special/death labels still need verified sequence bindings. Selecting a sequence does not execute AI, launch projectiles or attach every combat effect. Invalid/morph-only definitions, model bounds and rig compatibility must be checked.

For a first proof, use one known valid small NPC and one large boss, with verified idle/attack pairs. Confirm swap, framing, rotation, replay and close/reopen in Vulkan. Only then embed. Prefer one live model for the selected result, not dozens of animated result thumbnails. Model-loading/swap performance is unmeasured.

## 3. Items, equipment, scenery and the asset lab

Item icons are already native and live-proven in the separate517 Equipment Library. CS11054 also uses the native item operation for the boss-instance entry item. Keep **item ID**, **raw model ID**, **NPC ID** and **loc/object ID** distinct; “object” in an item opcode name is not proof of scenery support.

Customisations and marketplace prove supported outfit/override previews on a player-shaped model. They do not yet prove a safe arbitrary inventory-to-preview appearance builder. The future Give/Equip action must remain an explicit shared-handler operation, separate from preview. Never alter real equipment or real cosmetic settings merely to paint a preview.

For scenery, the generic model primitive is useful but incomplete: loc definitions can select multiple meshes per shape, carry recolours/retextures, scaling, offsets, animations, terrain-contouring settings and variable forms. A raw mesh may be recognizable yet wrong. Construction portal-nexus1499 has no static model component; this alone does not rule out dynamically created previews, but no generic loc renderer was demonstrated in its audited layout. **Full object preview remains PU**, with a precise next question: which native operation accepts a loc definition plus shape/rotation and preserves its model assembly?

Animation-lab scope that the evidence supports:

- Actor/model + validated sequence selection; native framing and rotation.
- Separate metadata search by verified symbol/name/ID, with unverified aliases labelled.
- A later Play/Stop/Replay controller needs a minimal proof. The sequence setter skips reinitialisation when the ID is unchanged; do not assume assigning the same ID restarts the animation. A clear/reassign transition and native completion behaviour must be tested.
- Loop and one-shot semantics depend on sequence metadata and renderer playback. Do not implement a server-tick “replay” loop as a substitute for understanding those semantics.

Graphics/effects are a different boundary. Marketplace contains ten additional type-6 components1495:69–78, with949 fireworks labels, but neither their existence nor a raw model proves arbitrary spotanim, particle-emitter or projectile preview. CS616's item/DB route is **not** a general graphics renderer. A projectile needs endpoints, time, trajectory and impact ownership that the ordinary model widget does not supply. Preserve real combat/world effect diagnostics; a future isolated world asset lab may use them after separate authorisation. No invented renderer is recommended.

## 4. Native inventory and shell comparison

Targeted inventory used950 component headers plus deeper traces where indicated. Names are discovery labels under the provenance rules above. A row labelled “inventory” is not a fully reverse-engineered reusable subsystem.

| Native foundation | Exact identities / evidence | Useful pattern | Assessment |
|---|---|---|---|
| Management window | 1448, CS8288/8289/8286; existing ready bridge | Frame, title, tabs/content hosts, input lifecycle | Keep provisionally; familiar proven mounting path |
| Customisations | 1311; CS3503/6446/18274; input140 and preview343 | Dynamic actor model, search, variants | Borrow primitives; avoid wholesale progression/cosmetic state |
| Marketplace | 1495:15/16/17; CS20604/11613/18120 | Compact reusable preview and drag logic | Strong preview reference; commerce state must be excluded |
| Photo booth | 549:16–20; CS10882/1608 | Minimal head/body/player/NPC comparison | Good isolated proof; narrow120×219 NPC component needs reframing |
| Beasts / boss instance | 753:40 and1591:2; CS3869/11054 | Model + metadata + native scroll | Best NPC details pattern; native boss schema is curated |
| Bank / shop | 517 /1265 | Dense item grids, actions, search | Preserve `;;items`; not a general NPC browser shell |
| GE history / item sets | 1638 /1719, header inventory | List/table and item-set inspection leads | Not selected as shell; transaction-specific lifecycle not traced |
| Achievements / collection | 1850 /656, header inventory | Categories, details, completion rows | Layout references; existing progression state is unwanted |
| Skill guide / Powers | 1218 /1460; existing pinned ability resources | Item/sprite rows, tabs, tooltips | Use known icons; do not repurpose combat availability state |
| Pet bank / appearance | 1384 /900;900:7/14 type6 | Selection and player model | Secondary examples, not universal actor API |
| Worn / recolour | 1462 /1315 | Equipment slots, colouring controls | Useful pattern, not permission to change worn items |
| Grouping / boss setup | 1519 /1524 /1591 | Categories, selection, encounter controls | Borrow list/action pattern; avoid party/session mutation |
| Construction | 1499 | Portal list/configuration | Travel lead, not proven ghost/object renderer |
| Skybox / machinima | 623 /475 | Sliders, choices, advanced environment controls | Specialised secondary tools; world camera is not preview camera |
| Travel list / lodestones | 908 /1092 | Destination rows / existing map selection | Generic list for arbitrary destinations; lodestone map for native network |
| World map | 1421 +1422, CS343/1898; current Native950WorldMap | Spatial travel/inspection secondary screen | Preserve dedicated viewport/input lifecycle |

### Why not immediately switch shells?

1448 already supports dynamic children and native drawing/operations; its plain current layout is not a hard native limitation. Its important cost is multiple visual/text/hit hosts and native startup ownership. The proven handshake and keyboard recovery are worth retaining.

1495 is a preview-oriented layout, not a ready universal console.753 is a boss catalogue, with hardcoded metadata and frame relationships.549 is narrow and photo-specific.517 would entangle the working library/bank experience. None of these is yet proved to offer a lower-risk complete replacement.

Prefer **1448 + purpose-built native content composition + optional specialised viewer**. Preserve the existing ready callback but give every view one coherent clipping/hit-test hierarchy. A large responsive pane is an implementation/resize test, not something established by static header dimensions:1311:343 has relative height,753:40 and1495:15 have relative width/height. The mockup's exact pixel layout is not a native guarantee at every game resolution.

## 5. Scrolling, menus, visual rows and immediate feedback

### Scrolling

CS11074 constructs content, measures height, sets scroll size (`0x00a0`), resets/sets scroll position (`0x01a2`) and calls **CS7791(scrollHost,contentHost)** when needed. Its established hosts are753:31 and753:28. CS31 is a native scrollbar builder with native style data (including fallback struct28551) and event helpers. This establishes native lists/scrollbars; it does not prove automatic virtualization of78,000 definitions.

The current console builds visual/text/hit children across1448:3/5/7. Scrolling just one host would leave labels or hit targets behind. Future composition must scroll and clip all three consistently or replace that split with a tested row hierarchy. Include wheel-over-row, scrollbar drag, partial-row clipping and right-click-at-scroll-offset in the physical proof.

Keep search server-indexed and return a bounded result window. Let the client scroll its loaded rows immediately. Add a bounded refill/page mechanism behind scrolling only after measuring it. Do not construct one child per cache definition or promise a native virtualized collection without evidence. Preserve selected definition ID and scroll offset across details/Back.

### Menus

CS10324 establishes native operation labels via `CC_SETOP`, with operation indices1–10. Event arguments include operation **-2147483644**, source component **-2147483645**, and dynamic child **-2147483643**. Native hook installation, operation/event enablement and server dispatch must agree. A visible label alone does not guarantee a packet.

The duplicate Open Details is authored duplication, not an unavoidable native menu entry. Future row creation should assign each operation once, clear unused operations on reused rows and choose one default left-click action. Example: left-click selects/details; right-click Spawn Near Me / Place / Spawn Multiple / Inspect Combat / Favourite. Selection may be immediate, but execution must validate the server's current row identity, nonce, permissions and ownership. Do not overload numeric slots with stale search results.

### Icons

Use existing native item rendering for items and verified sprites for categories, skills and abilities. Beasts reads sprite param3038 into753:39 independently of its full model. Boss icons therefore need the corresponding metadata; an arbitrary NPC ID is not a sprite ID.

A miniature type-6 NPC widget is technically a model, not a cached thumbnail service. Heads require a head asset. Neither offers guaranteed cheap universal rows. Start with curated native boss/category icons and a single large model. Objects without a verified sprite/item representation should show an honest category icon rather than a made-up likeness.

### Responsiveness

Prior measurements in the console UX report correlate input/nearby server output at median267.5ms/max572ms on a600ms world cadence. They are a proxy, **not Vulkan paint measurements**. Cached search is already cheap (prior1,000 cached queries about2.3ms total); reducing server round trips and whole-view reconstruction matters more than another search-index rewrite.

Client-side candidates: hover, pressed/selected visuals, loaded-row scrolling, model rotation, tab presentation for already loaded data, caret/text editing and Simple/Advanced visibility. Server-owned work: global search/catalogue admission, Give/Equip, spawn/place/delete, save/undo/redo, permissions, travel and ownership. Never move authority client-side to make a button feel fast.

Native Customisations search has caret/display/cancel layers and CS15000→15001 plusCS9833/13120-related input handling. This is a genuine integrated-input pattern; it is not proof that all NPC/object definitions are available as a client-side text index. Use a scoped query handshake and bounded results, then preserve local view state. Partial updates should change the affected row/detail/status, not rebuild the whole window after each click.

## 6. Placement preview and floating scenery

The current world cursor path is established: a selected interface source is armed with exact wire opcode0x072c (native handler0x1401f76f0), then chosen-tile client opcode85 reaches server validation with source/slot/coordinates. Preserve that ownership and cancellation path.

This does **not** establish a hover-time footprint renderer or a translucent scene ghost. The normal chosen-tile packet provides a committed click, not a stream of cursor coordinates. The audited Construction/skybox/machinima layouts do not establish a reusable arbitrary ghost API. Never implement preview by spawning and deleting real actors on every mouse move.

Feasibility ladder:

1. Native targeting cursor and visible footprint dimensions in the details pane: established primitives.
2. Selected anchor marker / full footprint after tile selection but before explicit confirmation: plausible, requires exact marker and cleanup proof. Do not invent a graphic ID.
3. Moving translucent model following the pointer: unproven; postpone until a native scene-preview contract is found.

### What the object path currently does

`Native950DiagnosticSpawns.placeObject` validates the definition, rejects variable forms, selects a supported shape (prefers10), rotates width/height, checks footprint/scene occupancy, creates `new WorldObject(id,type,rotation,tile)` and calls `World.spawnObject`. It does **not** calculate a cosmetic Y offset or copy a source map-placement transform.

`Native950Packets.objectAdd` emits native **LOC_ADD_CHANGE opcode11** with definition, shape, rotation and local tile offset. Its normal six-byte form has no absolute elevation. An optional flagged transform appends quaternion/translation/scale data; the existing writer supports it. `WorldObject.native950MapTransform` preserves original map-placement data, but a fresh developer object constructed from an ordinary tile has none.

Consequently a valid shape does not guarantee natural placement for every asset. Some definitions are pieces of a larger construction, wall/floor decorations, or depend on original placement transforms. Definitions also carry mesh offsets, scaling and contouring behaviour. A raw model's origin need not be its visible base. Bridge/plane state is a separate concern: the native scene uses its terrain/plane rules, while server collision correctly treats bridge flags as a different effective collision plane. Do not “fix” elevation by copying collision-plane adjustment into rendering blindly.

**Root cause for the reported floating objects remains UNRESOLVED without a specific placed definition/tile/shape/rotation witness.** The missing source-placement-transform path is a concrete code difference, not a proven universal diagnosis. No terrain-height renderer was fully reversed in this bounded pass.

Next focused diagnostic should compare a failing object with a naturally placed950 instance of the same concrete definition: original shape, rotation, plane/bridge flags, optional map transform, model/definition offsets and slope. Then compare ordinary flat-ground placement. Preserve transforms when duplicating an existing placement; choose a suitable concrete shape for fresh objects; only introduce a correction after identifying the actual discrepancy. A blanket zero-offset or arbitrary lowering rule is wrong.

## 7. Proposed Console v2 architecture

### Main console

Retain1448 readiness/window management, replace the content composition deliberately. Use a restrained native frame, top-level domains, integrated search, scrollable results and a persistent right-hand details pane. Do not show every possible tool/action at once.

Simple mode displays name, visual where supported, level/category/basic status and two or three obvious actions. Advanced reveals IDs, verified symbol, source/provenance, sequence/effect and packet diagnostics on demand. One native toggle can hide/show the technical subtree without clearing selection/search.

Home should prioritise Recent, Favourites and quick Heal / DM / Best Gear / Clear My NPCs / Clear My Objects / War's Retreat. NPC/Object pages should feel like find → inspect → act. Dangerous or broad deletion stays explicit and owner-scoped. Keep separate `;;items` as the successful equipment library.

Selection and local visual feedback should be immediate for loaded data. The server action registry remains the execution layer; friendly controls collect meaningful choices, not raw command strings. A compact status/result area is useful; a large always-visible technical console log should be Advanced-only or collapsible.

### Model viewer and asset lab

First prove a compact model component using the **Beasts NPC/sequence/view contract** without mounting all Beasts content. If embedding conflicts with the main scrolling/focus lifecycle, open a deliberate secondary native viewer with Back. Preserve query, filters, selection, loaded result window, scroll position and favourites. On close, retire model/drag/input hooks before restoring the console or gameplay.

Use the same viewer foundation for player/item proofs where supported. Animation/graphics labs are separate advanced tools; do not expose unproven controls as if functional. Never substitute a screenshot or an unrelated model for an unsupported preview.

### Teleports

For arbitrary destinations, **the console's generic list/details pattern is the best primary foundation**. Native908 is a small reusable-frame/list lead: its root contains a serialized CS8420 setup with “Teleport List”, frame struct21218 and central context1007. It does not prove built-in global search, favourites or destination images.

Reuse1092 only for the actual lodestone network. Existing exact research pins its29 destinations, enum5726 and CS14999/13702; its576×360 panel requires correct host sizing. It is unsuitable as a general map of arbitrary developer destinations.

Offer world map1421/1422 as an intentional secondary spatial view. Current code mounts both as siblings, preserves the map input layer, usesCS343 wheel/gesture setup andCS1898 close cleanup. Mounting it as an arbitrary content child risks losing its native input. Boss/Grouping data can provide verified destinations and icons, not permission to mutate parties. Travel execution stays server-validated.

### Smallest next implementation proof—not performed here

1. One bounded native scrolling list with correct wheel/drag/clipping and row actions.
2. One selected NPC full model with correct idle and attack, rotation, framing and close/reopen.
3. One integrated search field with focus recovery and local selection feedback.
4. Only then apply the new approved mockup across domains, retaining the backend and stateful Back navigation.

This avoids another large speculative redesign. No new stage is justified by research alone.

## 8. Direct answers to the fourteen questions

1. **Arbitrary NPC model? Yes, for valid supported definitions.** Beasts/booth/marketplace establish a definition-ID full-body setter. New console rendering remains untested.
2. **Control animation? Yes, set a sequence separately.** Correct per-NPC attack/special/death bindings and replay/loop handling still need verification.
3. **World objects? Raw models yes; faithful arbitrary loc previews unproven.** Shape/model assembly and placement transforms are the missing contract.
4. **Equipment/items? Native icons and supported player/outfit/item previews exist.** Arbitrary whole-loadout preview isolated from real equipment still needs a proof.
5. **Arbitrary combat animations/graphics? Sequences on compatible actors are supported; a complete effect/projectile simulation is not proved by that.**
6. **Embed or secondary? Embedding the primitive is technically supported; start with an isolated proof.** Use a secondary native viewer if focus/layout integration is unsafe.
7. **Proper scrolling instead of Previous/Next? Yes.** Native scroll primitives are clear; bounded data and coherent clipping/hit targets are required.
8. **Useful icons/thumbnails? Yes for items, known sprites, supported NPC heads/models.** No universal cheap NPC/object thumbnail service was established.
9. **Footprint/ghost before placement? Native selection cursor yes; footprint/anchor overlay and translucent ghost remain unproven.**
10. **Why floating objects? Not conclusively diagnosed.** Fresh placement lacks original optional transforms; model origin/shape/terrain/bridge state also matter. Compare a failing placement with its native map instance.
11. **1448 still best? Best-supported provisional main host, not a demonstrated universal winner.** There is no evidence-based reason yet to discard its working lifecycle.
12. **Which interfaces to reuse?** Beasts753 model/details/scroll contracts; marketplace1495 preview/drag helpers; photo booth549 for proof; bank517 icons as reference; specialised map/lodestone screens for their actual functions.
13. **Best teleport foundation?** Shared native console list/details plus curated travel data;908 for frame/list study,1092 for actual lodestones,1421/1422 for optional map navigation.
14. **What sluggishness can be removed?** Round trips for presentation-only selection/rotation/scrolling, full-window redraws and rebuilding unchanged rows. Actual authorised world mutations still require server processing; no precise final latency is claimed.

## Reproduction and handoff

The following performs no server boot, cache edit or deployment. Use a fresh output directory because the exporter refuses overwrite:

```powershell
runtime/java25/bin/javac.exe -cp 'OpenNXT/runtime/lib/*' -d temp/ui-capabilities-20260925 tools/ui-capability-research/ExportUiCapabilities.java
runtime/java25/bin/java.exe --enable-native-access=ALL-UNNAMED -cp 'temp/ui-capabilities-20260925;OpenNXT/runtime/lib/*' ExportUiCapabilities cache temp/ui-capability-new-export
python tools/research_native_ui_950.py --components temp/ui-capability-new-export --output temp/ui-capability-recheck.json
```

The script checks independent booth/Beasts/marketplace NPC setter witnesses, native sequence relationships, the empty animation helpers, scroll-size/scroll-position/scrollbar relationships and the operation sentinel. It records hashes and numeric identities, without committing raw assets. Native handler bodies were inspected with `tools/dis950.py`; addresses are pinned to the executable hash above. No full gameplay suite was run because runtime source was unchanged.

Read alongside [the previous console UX report](DEVELOPER-CONSOLE-UX-20260925.md), [Gameval investigation](DARKAN-GAMEVAL-INVESTIGATION.md), `tools/verify_950_lodestones.py`, `Native950WorldMap.java`, `Native950DiagnosticSpawns.java`, and `Native950Packets.java`. **Research is complete; implementation awaits the user's next mockup/prompt.**
