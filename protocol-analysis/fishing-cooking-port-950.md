# Fishing and Cooking port to revision 950

The original 910 `Fishing` and `Cooking` Action classes remain the action entry points. Their native branches use the paired 950 cache and the existing ActionManager, Skills, inventory, collision and player update systems. There is no additional scheduler.

## Fishing

Sixteen ordinary original FishingSpots methods are supported on ten unchanged NPC IDs:

| NPC ID | Supported methods |
| --- | --- |
| 327 | Net, Bait |
| 328, 329 | Lure, Bait |
| 312 | Cage, Harpoon |
| 313 | Large net, Harpoon |
| 6267 | Cage |
| 952 | Monkfish net |
| 1405 | Special net, Harpoon |
| 8841 | Cavefish bait |
| 8842 | Rocktail bait |

The second ordinary NPC action is native menu slot 3. This is checked against actual cache menus; the old FishingSpots table calls it option 2.

Fish selection, required levels, base catch curves, experience and four/five tick delays come from the original FishingSpots/Fish tables. Backpack tools and bait are required. Bait is consumed only with a successful atomic catch; baitless catches use normal inventory insertion. Movement, invalid/removed/moved spots, no tools/bait and a full backpack stop the action. Actual collision must show an adjacent reachable NPC before starting.

The five original NPC IDs with changed identity (21778, 21779, 21780, 6996 and 1178) remain unported. Skillchompas, toolbelt defaults, equipped rod overrides, outfits, familiar/perk/invention bonuses, random events and custom spot movement are not enabled by this ordinary port.

### Availability in the current world

The existing `Start-950Server.ps1 -LumbridgeNpcs` setting limits automatic NPC loading to region 12850. Its existing `Ataraxia950/data/npcs/spawns.json` roster has four NPC 329 Lure/Bait spots at (3238,3255), (3239,3243), (3238,3252), and (3239,3241), plus three NPC 6267 Cage spots at (3259,3203), (3259,3204), and (3259,3205). These are existing roster coordinates, not new placements or proof of a live client test.

Beginner Net/Bait NPC 327 is already in the old Draynor roster at (3086,3227), (3086,3228), (3085,3231), and (3085,3230), region 12338. That region is outside the current Lumbridge-only switch. The code supports its safe identity, but no change here broadens the world's spawn scope.

With development commands enabled, use `;;npc 327`, then walk one tile beside the spawned spot. It spawns at the player's exact tile. The integrated Fishing route runs before diagnostic NPC restrictions, and reaches the spot through normal collision routing. Useful item commands:

- `;;item 303`: small fishing net.
- `;;item 307` and `;;item 313 100`: fishing rod and bait.
- `;;item 309` and `;;item 314 100`: fly fishing rod and feathers.
- `;;item 301`: lobster pot.
- `;;item 311`: harpoon.

## Cooking

The original Cookables table supplies meat, fish, potato and pie results, level requirements, burn thresholds and experience. The original fire/range burn curve and level-dependent one-to-three tick cadence are retained, including this server's explicit 0.4 Cooking XP multiplier before the common Skills multiplier. Failed food grants its actual burnt result and no XP. Success/burn exchanges raw food atomically; a full backpack of unstackable raw food can therefore cook one-for-one without losing items. Quantity, walking and vanished fire/range cancellation remain authoritative.

Native object Cook, Cook-at and Cook-on options open available foods; the shared verified 950 dialogue then offers quantity selection. Player-created fires use their actual Use menu, with a Cook food / Add logs choice when raw food is present. Special spit-roast/cooking-station recipes, ports rocktail soup, poisoned karambwan and the old incorrectly mapped baron shark are excluded; cave eel's unnamed burnt 950 item is also excluded. Item-on-world-object packets are a separate unfinished port.

## Assets and validation

All 145 referenced items, 15 NPC definitions and seven animation definitions are SHA-256 pinned to this 950 cache in `fishing-cooking-assets-950.properties`. Runtime admission still checks only supported original recipes/NPC identities. All seven sequence definitions (618, 619, 620, 621, 622, 1193 and 897) were compared directly to the original 910 packed cache and are byte-identical. Client rendering remains a manual check.

`Native950FishingCookingTest` exercises original chance/cadence/burn bounds and exact menu operations. `Native950FishingCookingAcceptance` runs in a fresh JVM and verifies actual-cache admission, original ActionManager types, catches/bait/XP, denied starts, movement cancellation, full-backpack cooking, recipe quantities, removed-fire cancellation and save snapshot XP. It never writes a character save or opens a listening socket. Main integration owns the combined build and acceptance run.

### Completed checks, 12 September 2026

The combined build reported 1,011 tests passed and two skipped. The fresh-cache action probe admitted exactly 16 fishing methods and 45 cooking recipes. Catches, one-bait consumption, level/tool/full-backpack gates, movement cancellation, full-backpack cooking, quantity, removed-fire cancellation, and skill save capture passed. Save capture uses the existing Native950PlayerBinder, as the real session does; the inventory-only snapshot deliberately does not contain current skill values.

The separate `temp/Native950FishingCookingRoutedAcceptance.java` probe ran against the fresh production JAR with full encrypted input/output. It rejected an NPC not yet published to the viewer, then accepted Net option 1, approached through real cache collision, and started the original Fishing action. Native Bait option 3 also caught a fish, consumed one bait and awarded XP. The fire's actual Use option 5 opened Cook food, then the food list, then quantity; choosing shrimp and Make 1 started original Cooking and produced exactly one cooked shrimp with XP. The final run parsed 110 encrypted frames over 30 engine ticks. Catches are random, so that frame count is not a fixed assertion.

Logs: `logs/fishing-cooking-acceptance.log` and `logs/fishing-cooking-routed-acceptance.log`. No account save or listening socket was created by these probes. Visual rendering, sounds, and user feedback in the desktop client still need a live client check.
