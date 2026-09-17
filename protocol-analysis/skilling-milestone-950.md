# Woodcutting and firemaking milestone — 950

All implementation is contained in `C:/Users/developer/Desktop/950RevTest`.

## Try it

1. Launch with the existing `Start-950Test.ps1 -Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools` options and enter World 1.
2. Enter `;;nxt skilling`. Free two backpack slots if requested. This supplies a bronze hatchet and tinderbox if missing and moves beside the Lumbridge banker. It does not change your levels or erase items.
3. Chop an ordinary nearby tree. A real cache tree verified by the automated route check is at **3228, 3267**, northeast of the banker. Your hatchet works from your backpack; better supported hatchets follow the original level/power rules.
4. Logs and Woodcutting XP arrive through the original inventory and Skills system. Walking cancels chopping. A depleted tree becomes a stump and regrows on its original timer.
5. Right-click a log and choose **Light**, or use the tinderbox on the log. Log-on-tinderbox works too. Light on an open tile: the character attempts ignition and steps aside once it catches.
6. Choose **Use** on the fire to keep adding usable logs from the backpack. Walking cancels the action. The fire expires on the original timer and leaves collectable ashes.
7. Re-login later to check that your inventory and skill progress persist.

## Reused engine behavior

The existing910 `Woodcutting`, `Firemaking`, `Bonfire`, `ActionManager`, `AxeDef`, `TreeDefinitions`, `Log`, `Skills`, `ItemsContainer`, `World`, `Region` and `WorldTasksManager` remain authoritative. Native adapters validate actual 950 assets and translate wire interactions. Player.processEntity already processes ActionManager exactly once; no second skill tick loop was added.

Supported wood resources: normal, evergreen, dead, oak, willow, maple, teak, mahogany, yew, magic and elder trees. Ten authored hatchets from bronze through crystal are admitted from exact950 item/sequence pins. Chopping uses the cache-linked **Lumberjack** role because the old ordinary910 animation numbers have different meanings in950. Standard-animation styling can be refined separately without changing harvest behavior.

Firemaking supports14 ordinary log definitions, including the common harvested logs. Both directLight and item-use enter original Firemaking; cache fire menu option5 Use enters original Bonfire. Normal40baseXP still follows the existing noncombat rate (140 at1x). Failed or cancelled attempts leave the exact owned log available to recover, and stale/foreign piles cannot become free fires orXP.

## World and wire

Native object creation/change is opcode11 (variable-byte), removal26. Existing per-placement object transforms are preserved through stump/regrowth. Session views project effective Region objects, including late joins and scene rebuilds. Candidate75 was proven to be object animation and was not used for spawning. Native item-on-item opcode69 is independent from inventorydrag12; both endpoint slots, IDs and interfaces are checked against the authoritative backpack.

Skilling grants use staged original ItemsContainer insertion and commit atomically. Full inventories, quantityoverflow, invalidinputs and controller vetoes leave resources intact. Actual skill changes are observed and sent via the existing inventory/UPDATE_STAT paths, so the confirmed floatingXP and skillcircles receive normal skillupdates. Saves use the existing Native950PlayerBinder.

## Validation

Full suite: **924 passed, 2 skipped, 926 discovered**. The actual-cache acceptance exercised377 engine ticks and 841 encrypted frames, including realcollisionapproach, tool/capacity/cancellation checks, tree depletion/regrowth, Light/Bonfire, originalfireexpiry, ashesTake, both item-use directions and exactskillXP savecapture. Existing world and reward regression checks and startupcachepreflight passed. See `validation-skilling-2026-09-10.json` for logs and finaldeploymentstatus.

Installed engine SHA-256: `10f9f998126535720efd597db87d0f00da8adb10f8968321b5d6ab4e418d3d0c`. Server and client are running; Kotlin compilation and all17 launcher checks passed.

Live rendering still needs the user's client check. Optional910 pets, invention, contracts, clues, quest/minigame/customtrees, portablefires, proteanlogs and bonfireHPbonuses remain separate ports. The repurposed Evilbark3239 is excluded. Native toolbelt defaults are still unverified; use the actual backpack tools.

Evidence: `woodcutting-port-950.md`, `woodcutting-assets-950.json`, `firemaking-assets-950.json`, `world-objects-950.md`, `world-objects-950-evidence.json`, `item-on-item-950.md`, `item-on-item-950-evidence.json`.

Additional acceptance: bronze hatchet Wield and Remove traversed the original equipment handlers, updated the actual 950 appearance and returned the hatchet to the backpack.

Live log observation: the user entered the updated game, wielded the bronze hatchet, approached trees and used the log Light option. Visual success still awaits the user report. Default log Craft belongs to the unported Fletching UI; use right-click Light or tinderbox use for this milestone.
