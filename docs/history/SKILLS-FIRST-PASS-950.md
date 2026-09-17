# All 29 skills: first-pass training guide

Current implementation snapshot: **12 September 2026**, for `950RevTest`.

Every skill now has a basic training route in the source. This means a repeatable action with requirements, resources and XP; it does **not** mean every training method, interface or associated feature is complete. This guide supersedes older lists of entirely unported skills in the earlier milestone notes. Most content reuses the 910 action, recipe, combat and XP systems with 950 cache bindings. Archaeology and Necromancy had no implementation in the original 910 source and use new baselines.

## Training routes

Numbers in parentheses are item IDs. IDs in the first column are skill IDs, useful for diagnostics.

| ID | Skill | First-pass route |
|---|---|---|
|0|Attack|Attack a supported NPC barehanded or with a melee weapon. The existing combat XP policy shares melee XP between its selected skills.|
|1|Defence|Train through the existing melee combat XP split; ranged/magic damage also follows the original selected-skill XP policy.|
|2|Strength|Use ordinary melee attacks; it receives its share of the melee combat XP split.|
|3|Constitution|Deal damage through supported melee, ranged, magic or Necromancy attacks; Constitution XP is awarded alongside combat XP.|
|4|Ranged **new**|Equip a shortbow(841) and bronze arrows(882), then Attack. Supported bows/crossbows consume matching equipped ammunition.|
|5|Prayer|Bury ordinary bones(526), or Scatter supported ashes from the backpack.|
|6|Magic **new**|Wield staff of air(1381), carry mind runes(558), then Attack to automatically cast Air Strike. Other supported magic weapons also require air runes(556).|
|7|Cooking|Use raw fish, such as raw shrimps(317), on a supported fire or range; select the recipe and quantity.|
|8|Woodcutting|Carry a bronze hatchet(1351) and chop a normal tree; stronger supported trees/tools require their levels.|
|9|Fletching|Use knife(946) on logs(1511), select a product and quantity. Supported stringing, arrow and bolt recipes also work.|
|10|Fishing|Carry the appropriate tool and bait; use Net/Bait/Lure/Cage/Harpoon on a supported fishing spot. Small fishing net(303) provides an early route.|
|11|Firemaking|Use tinderbox(590) on logs(1511), or add supported logs to an existing fire/bonfire.|
|12|Crafting|Use needle(1733), thread(1734) and leather(1741); choose an available leather item. Gem cutting, spinning, jewellery, glass and pottery are also available.|
|13|Smithing|Smelt ores at a furnace, or use an anvil with hammer(2347) and bars. Current950 bronze daggers consume two bronze bars(2349) each.|
|14|Mining|Carry bronze pickaxe(1265), click copper/tin rocks, and walk into reach. Mining displays stamina and ore-progress bars.|
|15|Herblore|Clean grimy herbs, then use clean herbs, water-filled vials and secondary ingredients for supported potion recipes.|
|16|Agility|Complete the beginner Gnome course in order. `;;nxt agility` reaches its start; Barbarian(level 30) and Wilderness(level 52) courses are also available.|
|17|Thieving|Pickpocket ordinary men/women, or steal from supported stalls. Success grants items/XP; failure can stun the player.|
|18|Slayer|Use `;;nxt slayer`, ask Turael for a task, then kill assigned creatures. Assignment progress, completion and points are saved.|
|19|Farming **new**|Rake an allotment, use three potato seeds(5318) with seed dibber(5325), wait 40 minutes, and harvest with spade(952). Rake is5341. Growth continues offline.|
|20|Runecrafting|Carry air talisman(1438) and rune essence(1436); enter Air ruins near3128,3403,0, craft at the altar and leave through its portal.|
|21|Hunter|At level 15, equip butterfly net(10010), carry butterfly jars(10012), and catch Ruby harvest butterflies. Baby implings start at17 and use empty impling jars(11260).|
|22|Construction **new**|Use Construct on a Furniture workbench with hammer(2347), saw(8794), planks(960) and steel nails(1539); make an available flatpack.|
|23|Summoning|At a full obelisk, combine pouch(12155), gold charm(12158), wolf bones(2859) and seven spirit shards(12183) into a Spirit wolf pouch. Convert it into Howl scrolls.|
|24|Dungeoneering **new**|Use a ring of kinship(15707) to travel to Daemonheim, then speak to the tutor or enter a dungeon. Bring equipment/food, defeat all three guardians and climb the exit to claim XP/tokens.|
|25|Divination|Harvest Pale wisps near3125,3215,0; convert their memories at an Energy rift. Configure selects energy, XP or extra-XP conversion.|
|26|Invention **new**|At the public Falador workbench, disassemble ordinary backpack items into stored components; manufacture available products. Requires base level 80 Crafting, Smithing and Divination.|
|27|Archaeology **new**|Excavate the campus starter remains with a usable mattock; Store materials at the nearby workbench, then Restore the damaged artefact. See exact locations below.|
|28|Necromancy **new**|Wield Death guard(55502), then Attack a supported NPC. Basic auto-attacks train Necromancy and Constitution.|

## New Farming and Construction locations

- **Farming:** Falador farm near **3053,3305,0** has supported allotments, flowers and herbs. Bring the tools and seeds listed above. Potato growth takes the original 40 minutes; it continues while logged out.
- **Construction:** the natural Rimmington Furniture workbench is at **2941,3229,0**. Stand nearby and choose Construct. With hammer, saw, four planks and four steel nails, you can make two Crude wooden chair flatpacks (current item61929).

[Detailed crop, compost and furniture checks](Ataraxia950/Native950FarmingConstruction-README.md).

## New Invention and Archaeology locations

All coordinates below use plane 0. The public objects were checked against current-cache collision and existing objects; repeated region loads do not duplicate them.

- **Invention:** public Inventor's workbench at **2967,3407**, near the Falador lodestone. Choose a backpack item to Disassemble, choose a quantity, then use stored components to Manufacture. Eight initial recipes use current 950 levels/materials/XP. Components persist and are not granted free on login.
- **Archaeology:** **Centurion remains at3363,3393** require level 1; **Venator remains at3368,3393** require level 5. Carry or wield bronze mattock(49539), or another supported ordinary mattock whose level requirement you meet.
- **Archaeology workbench:** the natural **Restore/Store workbench at3355,3393**. Store backpack materials first, then Restore. Material storage and finished items persist. Unfinished excavation progress also survives logout and server restarts.

## Limits of the eight new baselines

| Skill | Present scope and next refinement |
|---|---|
|Ranged|Bow/crossbow auto-attacks, ammunition and range/line-of-sight checks. Thrown/powered weapons, projectile visuals, special attacks and abilities remain later work.|
|Magic|Automatic Air Strike with real rune costs and cast/damage XP. Other spells, spell selection, utility magic, projectile visuals and abilities remain later work.|
|Necromancy|Weapon auto-attacks and the correct skill XP. Rituals, conjures, abilities and complete equipment progression remain later work.|
|Farming|36 crops across 19 ordinary allotment/herb/flower/hops patch definitions; raking, compost, planting, offline growth and harvesting. Crops stay healthy in this baseline; disease, watering/protection, trees and animal farming remain later work.|
|Construction|45 flatpack recipes at supported workbenches. Player-owned houses, room building, furniture placement and house services remain later work.|
|Dungeoneering|Repeatable solo runs in four reserved frozen chambers, three guardians, one completion reward per run and saved tokens/counts. Bring your existing equipment. Procedural floors, parties, complexity, puzzles, binding, prestige and token shops remain later work.|
|Invention|10,844 identity-matched ordinary disassembly rows and eight manufacture recipes. Blueprint puzzles, augmented-item XP/perks, machines and most manufactured-device effects remain later work; making a device does not imply its effect is implemented.|
|Archaeology|Two starter plots, three restorations, personal material storage and cache-derived restoration requirements/XP. Excavation uses simple pacing and a generic digging animation. Full digsites, soil screening, relics, collections/chronotes and mysteries remain later work.|

Earlier skills also retain limits: Summoning currently trains through pouches/scrolls without familiars; Hunter lacks traps; Smithing lacks the metal bank and full native anvil workflow; Slayer rewards/blocking and additional Agility courses remain incomplete. See [earlier skill milestones](SKILL-PORTS-950.md) for those boundaries and [Invention/Archaeology details](Native950InventionArchaeology.md) for their implementation evidence.

## Testing and supplies

Normal training uses the interactions above. Some tool/equipment acquisition, shops and quests remain unported, so this is not yet a fully self-sufficient fresh-account progression game. For direct tests, `;;item <id> <quantity>` supplies only what fits; `;;npc <id>` and `;;obj <id>` create temporary diagnostic targets at your tile. Step clear before interacting. A Furniture workbench can be tested with `;;obj 139147`, an allotment with `;;obj 8550`, and a full obelisk with `;;obj 67036`.

`;;nxt level <skillID> <level>` explicitly changes saved level/XP; ordinary login does not grant levels or supplies. Leave backpack space and expect actions to stop when requirements or materials run out. Engineering checks cover cache bindings, ordinary routing, resource consumption, XP and saves; newly added client visuals still need playtesting. Report the skill, clicked option, item/object ID and any message when a route fails.

