# Toolbelt and native production UI — revision 950

Status: user confirmed the equipment, toolbelt and native production milestone basically works. The additional Smithing/Smelting drag fix is installed: Forge now uses the native large-window container. Automated production and modal-isolation checks passed, and the user confirmed that dragging now works without breaking the screen.

## What changed

- Equipping and removing ordinary equipment now preserve an ordinary walking route and combat target. Existing level, inventory, controller, death and teleport restrictions still apply.
- Worn Equipment has a native **Tool belt** entry. The belt supplies basic tools automatically, accepts supported upgrades, and supplies tools to gathering and production without requiring a backpack copy.
- Mapped Crafting, Fletching, Cooking, Herblore and Summoning recipes use the client's native Make-X product grid, quantity controls and Make button. Recipes without a complete native mapping retain their existing selection dialogue.
- Furnaces and anvils use the client's native metal/product selection screen, quantity control and **Begin Project** button. Anvils offer all currently implemented smithing recipes, including entries whose requirements are not met. Upgrade selections use the current cache's item relationships.
- Production still checks the actual recipe, level, materials, capacity, station reach and action state before consuming anything. A visible cache entry does not grant an unimplemented recipe.

**Materials must remain in the backpack. The metal bank is a separate, unimplemented feature; this change does not provide metal-bank storage or withdrawal.** Basic default tools cannot be removed. Special toolbelt settings, such as Bonecrusher/Herbicide/Seedicide filters, remain outside this pass.

## Test preparation

Sign in to the updated client and enter World 1. Keep several backpack slots free. The diagnostic `;;item ID quantity` command adds test items. Optional `;;nxt level SKILL LEVEL` commands below change your saved level and XP; skip them if you already meet the requirements.

Run each test while stationary unless its instructions say otherwise. Use ordinary, uncharged test items.

## In-game checks

1. **Equip while walking.** Use `;;item 1205 1` for a bronze dagger. Click a distant clear tile, then choose **Wield** on the dagger while walking. It should equip without cancelling the route. Try **Remove** from Worn Equipment while walking too. The item should return once, with movement continuing normally.

2. **Open the toolbelt.** Open Worn Equipment and click its **Tool belt** footer icon. Check that the native tool categories appear and that selecting categories/tools updates their preview. Close and reopen it. The footer's exact live-client click remains part of this visual check.

3. **Add and remove a pickaxe.** If needed, use `;;nxt level 14 10` for Mining 10, then `;;item 1267 1` for an iron pickaxe. While stationary, right-click it and choose **Add to tool belt**. It should leave the backpack and appear in the belt. Right-click the stored iron pickaxe and choose **Remove**: it should return once, with the basic bronze pickaxe available again. Add it again, leave no pickaxe in your backpack, and mine a nearby supported rock to check tool lookup. Basic knife, hammer, chisel and tinderbox should likewise require no backpack copies.

4. **Fletch logs.** Use `;;item 1511 2`. Right-click Logs and choose **Craft**. Select **Arrow shafts** in the native production screen, set the quantity to **2**, and click **Make**. Expect two logs consumed and 30 shafts produced. No backpack knife should be needed. Reopen the menu and close it before making anything; closing should consume nothing.

5. **Craft a gem.** If needed, use `;;nxt level 12 27` for Crafting 27. Add `;;item 1621 1` (uncut emerald), choose **Craft**, select the cut emerald and make one. Expect one emerald (1605), Crafting XP and no backpack chisel requirement.

6. **Smelt at a furnace.** Carry `;;item 436 2` (copper ore) and `;;item 438 2` (tin ore). Choose **Smelt** on a furnace, select bronze bars, set quantity **2**, then **Begin Project**. Expect two bronze bars and both ore stacks consumed. If a station is inconvenient, `;;obj 11010 10 0` places a diagnostic furnace under your character; move beside it before clicking.

7. **Smith at an anvil.** Add `;;item 2349 4` (bronze bars), choose **Smith** on an anvil, select bronze and **Bronze dagger**, set quantity **2**, then **Begin Project**. The current recipe uses two bars per dagger: expect four bars consumed and two daggers produced. No backpack hammer should be needed. A diagnostic anvil is `;;obj 11497 10 0`; move beside it after spawning.

8. **Requirements and interruption.** At the anvil, inspect another material/product and its upgrade buttons. Try starting a recipe without its ingredients: it should explain the refusal and consume nothing. Open a station menu and walk away before starting; the old selection must not produce items remotely. During a longer batch, walk away and check that further completions stop.

Please report the selected product, quantity, station and visible result if any check differs. Native layout, category selection, toolbelt removal menus and repeated open/close behavior still need client-side confirmation; automated packet checks cannot establish their visual quality.