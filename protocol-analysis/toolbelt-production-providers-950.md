# Native950 production provider and catalog adaptation

This scope supports the native Make-X menu and dedicated forge UI. Existing actions remain the authority for reach, skill level, materials, inventory capacity and scheduled completion.

## Provider changes

- `Native950Production.Recipe.refusal` accepts required tools in the backpack or native toolbelt. Ingredient protection, exchange capacity, action readiness and XP logic are unchanged. Requirement descriptions now name both locations.
- `Native950Smithing.choices` presents all 702 admitted current-cache recipes when the player is beside a valid anvil. Missing bars or base equipment no longer hide recipes; Start still runs the original validated action.
- Crafting, Smithing, Smelting, Fletching and Cooking providers already expose the immutable Recipe's actual outputs, skill, level, XP, inputs, tools and material-limited batch count. No redundant metadata model was introduced.

## Paired-cache native catalog

`Native950ProductionUiCatalog` and `production-ui-catalog-950.properties` contain 525 dense, zero-based item enums reachable from current enum7056 and the verified root menus. Original item ordinals are preserved. 557 enum records (categories, verified root/name pairs, master7056 and skill map681) are SHA-256 pinned before dispatch. Parameter2989 resolves UI-only display aliases; parameter2640 and enum681 translate native skill IDs.

Verified current roots include 6939/6940 (wood Fletching), 6941/6942 (string/join), 6943/6944 (feathering), 6945/6946 (ammunition tips), 6981/6982 (gems and bolt tips), 8403/8404 (leather), 8405/8406 (robes), 7004/7005 (pottery wheel), 7006/7007 (pottery firing), and 10644/10645 (spinning). Single categories cover glassblowing6977, silver6984, gold6985, smelting7083/2412 and original metal smithing7085..7090.

Do not reuse the original910 generic shell15093/15092: in950 these enumerate metal-bank ores/bars. Old6987 and6817 also have different meanings. Interface301 is the Assist System, not the forge. Root's dedicated interface37 implementation handles modern smithing upgrades and forge categories outside the ordinary7056 graph. Unsupported native mappings must retain the existing production dialogue fallback rather than omit recipes.

Read-only audit tools: `tools/ProductionMenuProducts950.java`, `tools/VerifyProductionUiCatalog950.java`; output `protocol-analysis/production-menu-products-950.tsv` records1127 provider output rows including secondary returned containers.

## Equipment footer entry evidence

Current script8472 maps equipment footer host1462:33, but8471 calls16559 with actual button container1462:35 and footer group3. Enum5134[3] points to5137. Its ordinal2 is DBrow1200 (cache2/41), whose name is Tool belt and whose option1 opens it. Scripts16555/16557 build the footer actor; the clickable subchild is1. The on-wire actor serialization must be confirmed by the integrated UI acceptance/live check.

## Verification

- Isolated Java8 compilation of the new catalog passed.
- Paired950 cache verification passed all557 enum hashes and525 category records.
- Confirmed real mappings: shortbow(unstrung)50 -> category6947/root6939; diamond1601 ->6983/6981; gold ring1635 ->6985; wool ball1759 ->7046/10644; bronze bar2349 ->7083; silver bar2355 ->2412; bronze platebody1117 ->7085; Orikalkum bar44838 ->7083; spirit wolf pouch12047 ->6934.
- Focused production test now verifies a stored basic knife completes a log recipe with no backpack knife and stays in the belt. Optional missing-tool/protected-item checks use a non-basic tool4162.
- ProductionThirdPassAcceptance and InventionRoutingAcceptance fixtures now emit real encrypted1371 product-grid and quantity-slider clicks plus1370 Make when the native menu is open; their nonproduction/no-output dialogue flows remain explicit dialogue packets.
- Shared compilation passed. Native950ForgeUiAcceptance passed against freshly compiled main classes: all702 Smithing and12 Smelting recipe UI paths,740 decoded encrypted frames, quantity/upgrade controls, a toolbelt-only hammer, stale packets, close, movement and removed-station checks. ProductionThirdPassAcceptance passed613 ticks/1271 frames with the corrected native channel helper overlay. InventionRoutingAcceptance passed407 ticks/769 frames. The root task runs the final full build and regression suite after remaining UI changes.

Backups of preexisting edited source are under `implementation-backup/toolbelt-production-ui`, preserving relative paths. New catalog source/resource and audit tools had no original file.
## Default-tool regression coverage

The original cancellation/lost-tool test now uses optional rock hammer4162 for the loss case, retaining cancellation and tool-removal coverage. Smithing tests explicitly verify starting without a backpack hammer, completing after a duplicate backpack hammer is removed, and refusing completion when an area controller vetoes the output during progress. These distinguish permanent basic-tool availability from the independent permission and scheduling checks.
