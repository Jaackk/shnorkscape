# Native950 production Make-X UI (2026-09-12)

The generic production menu now presents the actual paired950 cache's Make-X interfaces1370/1371 for mapped recipes. The normal ActionManager callbacks, requirements, tools, inventory exchanges, XP, cancellation and batch units remain authoritative. Modern smithing/smelting use the separate native forge37 implemented by the integration agent.

## Sources and cache evidence

The old910 `content/com/rs/game/player/content/RS3SkillsDialogue.java` supplies the architectural pattern: cache menu/category/product enums, parameters2640/2645/2655..2674, callbacks, and quantity state. Its root IDs and button offsets were not assumed compatible. All following details were traced in the supplied950 cache, using `tools/cache950.py` and the existing normalized clientscript decoder (`protocol-analysis/ui-scripts-950-evidence.json`).

- Enum7716 key1007 -> struct21304: central wrapper1477:732, host1477:735. The wrapper is512x352 and the content host512x334. Frame1370 is mounted here, content1371 at1370:0.
- Frame1370:0 loads8420/8421. The Make target is1370:30 with native optionMask1 and pauseText; it submits dialogue opcode101 with slot-1. Close is1370:32 with ordinary optionMask2/IF_BUTTON. The old26 target is not used.
- Content1371:22 uses7118/7119/7120. Each category enum row always allocates four actors, including hidden products; clickable actor is `1 + 4 * ordinal`. Parameter2989 aliases a display product to its inventory output.
- Selection7121 writes product1170 and updates details/quantity. The server validates the selected category ordinal, optional item identity, offered callback, current skill and cache material identities.
- Quantity7147 calls10085 with1371:19/20 and vars8846/8847. Programs10424/10451 create invisible Select actors;10450 submits op1 on1371:20 at `quantity - 1`. The server supports1..60 batches and rechecks the current material bound when making. Batch outputs retain the recipe multiplier; selecting1 batch of arrows produces15 arrows.
- Category7117 invokes10428/10431/3376. Static1371:28 opens the dropdown. Program10435 creates text actors at1477:896; artwork1477:897 forwards op1 through13126. Only an armed production dropdown may consume that shared root response. The selected actor retains the root enum ordinal. Program10444 closes the dropdown and clears its client ownership.
- On-load7144 registers keyboard context40 through8841. Closing directly unregisters40, closes only the owned mounts, clears1170/1169/1168/7881/8846/8847, hides the central wrapper, and restores central layout. It does not execute a generic modal-close script that could acknowledge a newer panel.
- Variables:1168 root menu,1169 product category,1170 display product,7881 category-name enum,8846 maximum,8847 quantity; varc-string2390 category title.

`resources/native950/production-make-x-950.properties` pins115 component/script/enum/struct files by SHA-256, including the shared dropdown and central ancestor components. `Native950ProductionMenu.verifyCacheBindings()` exposes the check for startup preflight. The separate provider-owned `Native950ProductionUiCatalog` pins current dense category enums and proven root/name pairs. Notable corrected mappings include current leather8403/8404 and magic-robes8405/8406; old15093/15092 and sprite map6817 have been repurposed and are not reused.

## Behavior and boundaries

- Direct native opening when a verified category or root covers every offered ordinary recipe. Category switching stays inside those offered callbacks.
- Mixed contexts or products without a verified mapping retain the initial semantic choice list. Selecting an individually mapped recipe then opens native Make-X. Unmapped recipes and non-production actions retain the previous dialogue quantity/action flow, so no content is dropped silently.
- Matching includes actual output, skill, and cache ingredient IDs. Example: oak shafts use display34672, consume oak1521 and produce30 arrow shafts52; they are not presented as the normal-log recipe.
- Cache categories can show additional unavailable products. These are not automatically enabled merely because the client knows their definitions; server selection reports that the product is unavailable from this interaction. This UI pass does not invent additional recipe content.
- Modern Smithing recipes are excluded from generic1370 mapping because their correct950 screen is forge37. The root integration owns this separate UI.
- Recipe tools now recognize the separately implemented tool belt; this renderer does not duplicate tool ownership or alter recipe transactions.

## Validation

`Native950ProductionUiAcceptance` passed on2026-09-12 at13:00 using the real paired cache and encrypted950 transport:102 world ticks and597 decoded outbound frames. It additionally asserts the decoded outbound Make event-mask is1, sends the actual opcode101 pause response, and refuses forged ordinary IF_BUTTON Make or stale1188 replies. It checks ordinary logs/knife and gem-cutting routing, native product and quantity controls, oak alias/material/yield,1-batch15-arrow output,60-batch bound, native category dropdown arm/select, unarmed/forged/stale rejection, close/walk cancellation, material changes before Make, and non-production callback fallback. It creates no account save, listener, client process or authentication session.

The migrated `Native950ProductionThirdPassAcceptance` also passed against the corrected channel renderer:613 ticks/1271 frames, covering real Make-X for tar, bolt tips, staves, dough, bread and cake plus all previous third-pass production checks. The Invention/Archaeology routing acceptance retained its dialogue fallbacks and passed407 ticks/769 frames (reported by the fixture agent).

Isolated execution (JDK8, before the integration agent's final full build):

```
java -Dataraxia.native.verifyCache=true -Dataraxia950.data=C:\Users\developer\Desktop\950RevTest\Ataraxia950\data -cp <isolated-classes>;Ataraxia950\build\classes\java\main;Ataraxia950\resources;OpenNXT\runtime\lib\* com.rs.game.player.client.Native950ProductionUiAcceptance C:\Users\developer\Desktop\950RevTest\cache
```

After the full build, omit the isolated-classes prefix. Final combined build/deployment and on-screen visual checks are handled by the integration agent. These headless checks prove packet framing, ownership and gameplay behavior; they do not claim that the native client was visually inspected.

## Quick manual checks

1. Use a knife946 on normal logs1511 (or the log's ordinary Fletch action with the knife in the tool belt). The native product grid should offer shafts and bows, show material/level information, and manufacture the selected batch count.
2. Repeat with oak1521 and choose shafts. One batch should consume one oak log and produce30 shafts52.
3. Use chisel1755 on uncut sapphire1623. Native gem cutting should show sapphire1607. Close with the X or Escape, then reopen and make one.
4. While Make-X is open, walk or open another owned modal. The production action and its dropdown/key ownership must be cancelled; a later click must not manufacture the old selection.

Backup of the original menu: `implementation-backup/toolbelt-production-ui/production-menu-20260912/Ataraxia950/game/com/rs/game/player/client/Native950ProductionMenu.java`.

Live validation correction: the screen rendered correctly, but the first live click exposed a wrong event override: setting mask2 on1370:30 removed its cached pause bit0 and made mouse/Space inert. Native component bytes show mask1 and pauseText Select;7147/1458/13982 preserve that pause target. The renderer now sends mask1, and the menu handles only the matching current native dialogue response for Make. The earlier synthesized IF_BUTTON acceptance was insufficient; it has been replaced with real opcode101 coverage and an outbound-mask assertion. Final live recheck is coordinated by the integration agent.

