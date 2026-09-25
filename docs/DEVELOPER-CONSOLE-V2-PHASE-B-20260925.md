# Developer Console V2 — Phase B checkpoint

Phase A is **CONFIRMED LIVE** by the user: native NPC models, rotation, scrolling and integrated search are useful. This checkpoint preserves those primitives and the registry, owned editor, Gamevals, library and combat lifecycle. It is **not final visual acceptance** of V2.

## Implemented for the next physical check

- Home is the default. Eight major tabs: Home, NPCs, Objects, Items, Bosses, Combat, Teleports, Tools. Sidebars contain contextual sections instead of repeating the top navigation. Commands live under Tools. Home has quick actions, shortcuts, recent items and favourite actions.
- Items uses the Equipment Library's existing curated catalogue and complete safe search index. Name, numeric ID and verified item symbols produce actual item rows with native item icons. Curated matches rank first; obscure safe matches remain available. Inspector: Give 1/X, Equip, Add to bank, favourites, metadata, More info and Open Equipment Library. The separate bank-style library is unchanged.
- Best Gear uses the existing four full loadout transactions; displaced inventory/equipment goes to the bank. Individual Equip uses the existing wear validation and returns displaced gear to inventory. If refused after granting, the new item remains in the backpack and the result explains this. Bank grants reject capacity/overflow and preserve other property.
- Default NPC results group equal cache names, using an offline 28,067-definition ranking: verified combat profile, curated boss identity and real bank/talk/trade/attack operations. Ranking does not assert a complete boss encounter. All Variants, selected-family variants and exact IDs remain available.
- NPC Zoom minus/plus/reset preserves native rotation. Missing validated attack bindings remain unavailable. Player testing has a native local-player preview and shared Heal/Max/DM/cooldown/loadout controls.
- Native scrolling extends to Objects, Items, Teleports and owned Spawns. Inspector redraws preserve scroll; changing the domain/query/page resets it. Spawns retains ownership, teleport, deletion, persistence and undo/redo.
- Teleports uses existing audited travel handlers and saved locations. Native World Map opens from the console; its close button returns to the same console state. It does **not** implement map click-to-teleport.
- The search field has a visible native border. Existing text context11 acquisition/release is unchanged.
- Placement sets the native selected-action caption to `PLACING: <name>`, announces cancellation instructions, preserves the result on returning to the NPC page, and records arm/reject/create/failure boundaries in Bug Test.

## Live failure evidence

Jaxa timeline `session-20260925-030251-412-jaxa`: at 03:04:15 Place was invoked for NPC17182; at 03:04:17 opcode85 arrived with source1448:11, slot7 and tile3291,10147. The console reopened. The previous version did not log the commit outcome and overwrote the result with generic help. This establishes that targeting reached the server, **not** that a spawn succeeded or which validation rejected it. Collision/ownership rules were not relaxed.

## Validation

- Full core regression: 1,589 tests; zero failures/errors; two existing skips.
- Final focused console/editor/map and script checks are recorded in `protocol-analysis/developer-console-v2-phase-b-950.json`.
- Read-only paired-cache probe covers global Torva/ID search, item hashes, canonical NPC choices/variant access and every console script pin.
- Native helper21144 active item opcode550 shares the setter at1401d1020 with explicit3c0; zoom getter order comes from CS1165; local-player setter79b comes from CS3503. VM checks verify stack, contiguous dynamic children, menus, camera preservation and keyboard cleanup. These are not Vulkan rendering proof.
- Installer CheckOnly, disposable fixtures and isolated cache preflight must pass before advertising the staged candidate.

## Still partial / unresolved

- Physical appearance/clickability of the new item icons, local-player preview, zoom controls and page layouts needs one narrow Vulkan check before further composition.
- Automatic NPC framing is still the existing provisional/native Beasts metadata. A native model-bounds contract has not been established; the new zoom controls make oversize previews adjustable. No claim of automatic Kalphite King framing acceptance.
- Startup flash remains unresolved. Hiding the management ancestor could suppress the native readiness source. No speculative sleep or visibility change was added to the accepted handshake.
- World-map chosen-destination teleport requires an independently verified map input/coordinate contract. The existing map opening/closing works independently of that research.
- Item favourites/recent are session-local; command favourites retain their existing persisted store. Cross-domain persistent recents/favourites and expanded verified type filters are follow-up work.
- Separate per-style Weapons Only buttons, native Home icons, broader curated teleport categories, Animation Lab composition and full object-model preview remain pending. The console does not advertise unsupported ghost previews or fabricated model/animation IDs.
- The persistent world-space placement banner/cursor polish is partial: the verified target caption and chat instruction are present; no new unproven overlay was installed.

## One short live check

After applying the staged update manually with both clients/server closed:

1. Fresh `;;dev`: confirm Home/eight tabs/contextual sidebar. Check Items → `torva`: icons, row selection, Give 1, Give X, Equip/Add to bank and Open Equipment Library. Verify your existing items remain intact.
2. NPCs → Vorago/Kalphite King: grouped result, All Variants, rotation and Zoom minus/plus/reset. Try Place on a clear large area with Bug Test enabled; report the displayed result, marking `;;bug dev-v2-b-placement` if it fails.
3. Player preview/loadout, Spawns scrolling/Teleport To, Teleports → World Map → close button. Finish with one ability keybind after closing the console/library.

No server/client restart or deployment is authorized by staging.
