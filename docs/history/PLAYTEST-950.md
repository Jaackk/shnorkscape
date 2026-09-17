# September 12 skill and interface update

## September 12: broad skill batch installed

See [SKILL-PORTS-950.md](SKILL-PORTS-950.md) for the current coverage and practical in-game checks: Crafting, Summoning production, Runecrafting, Divination, Thieving, butterfly Hunter and beginner Gnome Agility. Full-suite and actual-cache checks passed; new live visual checks remain pending. The newest engine/runtime details and backup are in [HANDOFF-950.md](HANDOFF-950.md).

The remaining port includes full Smithing forging/metal bank, Farming, Construction, Slayer, Dungeoneering, Invention, Archaeology, Necromancy, familiar behavior, Hunter traps, other Agility courses and specialized systems within the already-started skills. This is broad ordinary-content coverage, not complete skill parity.

Everything in this update is contained in `950RevTest`. The 947 and original 910 folders remain reference sources.

## Start

Run `Start-950Test.ps1 -Ataraxia -Collision -Vulkan -LumbridgeNpcs -DevTools`, sign in as usual, and enter World 1. Use `;;nxt banker` to return to the Lumbridge development area. `;;item <id> <quantity>` supplies ordinary test items without changing your levels. Skill level requirements still apply.

## First checks

**UI polish confirmed, September 12:** the user confirmed skill switching without a Hero refresh, immediately unlocked lodestone icons, and the full lodestone panel now work well. Guide content and destination map-area loading were already accepted. The checks below remain useful for future regression testing.

**Mining confirmed, September 12:** the user confirmed the installed arrival timing fix works well; stamina/progress bars were confirmed earlier. The mining steps below remain useful regression checks.

- **Mining:** carry a bronze pickaxe (`;;item 1265`) and click a copper or tin rock from several tiles away. Walk to the rock before the mining animation begins. Yellow stamina and blue ore-progress bars should appear while mining and clear when you walk away. Copper/tin rocks should yield ore and Mining XP. Old steel/mithril/adamant/rune pickaxe IDs were replaced in this cache: use **45467 / 45494 / 45521 / 45548** respectively.
- **Skill guide:** click several skills, including Necromancy; try the guide categories and sorting, resize, close, open Settings, and reopen the guide. The guide now uses the native Hero window's Skills page.
- **Lodestones:** visit City of Um, Wendlewick, Anachronia, Karamja or Ashdale, then return to Lumbridge. Also log out in Um, log back in, and return to Lumbridge. The server now reads the cache's map-area labels instead of retaining the previous destination's area.

## New basic skill workflows

| Skill | Quick starting example |
| --- | --- |
| Fishing | `;;item 303` supplies a small net. `;;npc 327` creates a temporary Net/Bait spot at your tile: walk onto a clear adjacent tile, then choose Net. For Bait, supply rod307 and bait313. Natural Lure/Bait spots already exist east of Lumbridge; they require higher Fishing levels. |
| Cooking | Supply raw shrimps with `;;item 317 5`. Use a range's Cook option, or light a log with tinderbox590 and then Use the fire. Choose Cook food, the food, and an amount. Burn chance and the original server's Cooking XP rate are retained. |
| Fletching | Supply knife946 and logs1511. Click Craft on logs, choose shafts, then an amount. Use feathers314 on shafts52 to make headless shafts, then bronze arrowheads39 to make arrows. String/Tip/Feather menus and item-on-item combinations also work. |
| Crafting | Supply chisel1755 and uncut opals1625. Click Craft or use the chisel on the gem. Higher gems retain their level requirements. This batch ports gem cutting. |
| Herblore | Clean grimy guam199. Use clean guam249 with vial of water227, then mix unfinished guam91 with eye of newt221. Mixing, unfinished potions and ordinary ingredient grinding reuse the original recipe tables. Pestle and mortar233 is retained as a tool. |
| Prayer | Bury bones526 from your backpack; supported demonic ashes expose Scatter. The item is consumed once and XP reaches the existing skill popup/save system. |
| Smithing | Bring copper ore436 and tin ore438 to the Lumbridge furnace at3226,3256. Choose Smelt, Bronze bar, then an amount. Twelve ordinary bars are available with current950 ingredients; this batch ports smelting, not anvil forging. |

Production choices use the existing native chatbox dialogue. More choices cycles through additional products. Choose Make1/5/10/all; walking, opening another activity, missing materials, losing a required tool, or a removed furnace/fire stops the action. A full backpack can still process items when the consumed ingredients free enough space.

## Scope

The original910 action scheduler, inventory, Skills/XP and save system are reused. Fishing and Cooking retain the original Action classes; production uses the old recipe enums through a shared atomic transaction/action adapter. All relevant950 item/animation assets are checked against the selected cache. Levels and base XP are retained unless a current-cache ingredient record requires a correction, as with smelting.

These are basic working skill paths. Full skill parity still needs toolbelt support, specialist/quest/dungeoneering recipes, production interfaces, contracts/perks/invention, anvil forging, broader Crafting, and the remaining skills. Generic gem-rock levels/rewards still use the original shared mining table. NPC spawning remains scoped to Lumbridge with the normal launch flags; diagnostic spawns are temporary. Item-on-object production is not connected yet; use Cook/Smelt/fire Use menus. Creating a potion or weapon does not itself add every consumption/equipment/combat behavior for that item.

Automated checks cover cache identities, collision routes, native input/output, quantities, XP and save capture. Actual client visuals and audio still need your in-game checks.
