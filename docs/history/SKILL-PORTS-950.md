# Current skill coverage

The latest [all-29-skills guide](SKILLS-FIRST-PASS-950.md) includes Farming, Construction, solo Dungeoneering, Invention, Archaeology and basic Ranged, Magic and Necromancy attacks. The earlier batches and their detailed playtests are preserved below.

---

# Latest skill batch: Smithing, Slayer, Hunter and Agility

Implemented in the isolated950 project using the existing action scheduler, inventory transactions, XP display and character saves. The original947/910 reference projects remain unchanged. Turael assignment has been confirmed in the live client. The other new skill appearance/gameplay checks remain ready for your playtest.

| Skill | Added | Scope still to port |
|---|---|---|
| Smithing |702 current-cache anvil recipes across bronze, iron, steel, mithril, adamant, rune, orikalkum, necronium, bane and elder rune; weapons, armour, tools, ammunition, ore boxes and upgrades; heat/progress bars |Metal bank, full native anvil window, reheating and saved unfinished projects. Cancelling currently preserves materials and discards progress.|
| Slayer |Eight original masters; assignments filtered to available native combat creatures; level requirements, kill credit, XP, saved remaining count, completion/streak/points |Special encounters, unlock-dependent families, reward shop, task blocking/skipping and wider combat population.|
| Hunter |All12 original impling species, net/jar/level checks, catch XP, weighted loot, respawn and exact clicked-jar consumption |Traps, barehanded methods, Puro-Puro-specific rules. Existing spawn rows cover only part of the species; diagnostic spawns work for all12.|
| Agility |Basic Barbarian and Wilderness courses, entrance/exit routes, ordered lap bonuses, safe movement and cancellation |Advanced courses, other courses/shortcuts, incidental achievement/contract/perk rewards.|

## Try these first

1. **Smithing:** obtain hammer `;;item 2347 1` and ten bronze bars `;;item 2349 10`. Use a real anvil, or create one with `;;obj 11497` and step clear of it. Click **Smith** (or Use the bars on the anvil), choose **Bronze dagger**, then **Make 5**. The current950 recipe uses two bars per dagger. Check the heat/progress bars, five daggers, retained hammer and XP.
2. **Slayer:** `;;nxt slayer` moves beside Turael, near the development banker. Use **Get task** or **Talk to**, then assignment. Fight the assigned creature using the existing basic melee setup, and ask Turael for progress. Remaining kills and XP should survive signing out and back in. Repeated assignment requests preserve an active task.
3. **Hunter:** Baby implings require Hunter17. Equip butterfly net10010, carry empty impling jar11260, and Catch NPC1028. For diagnostics: `;;item 10010 1`, `;;item 11260 1`, `;;npc 1028`; step off the NPC tile. Loot the filled jar with its **Loot** option. Catching grants Hunter XP; looting does not grant that XP again. Leave backpack space for loot and the empty jar. The original chance of the jar breaking is retained; its minor damage is nonlethal in this baseline.
4. **Agility:** `;;nxt barbarian` moves outside the level30 course. `;;nxt wilderness` moves outside the level52 course. Follow each course in order for the lap bonus. `;;nxt agility` still goes to the existing beginner Gnome course.

For optional diagnostics only, `;;nxt level 13 99` sets Smithing, `;;nxt level 18 99` sets Slayer, `;;nxt level 21 17` sets Hunter, and `;;nxt level 16 52` sets Agility. These explicitly change saved level/XP; the skill batch never grants levels or items automatically.

Recipe and technical evidence: [Smithing](protocol-analysis/smithing-port-950.md), [Slayer](protocol-analysis/slayer-950.md), [Hunter](protocol-analysis/impling-port-950.md), [Agility](protocol-analysis/agility-expansion-950.md). The earlier skill batch guide follows below.

---

# September 12 skill batch: playtest guide

Implemented in this 950 project using the original 910 action, inventory, XP and save systems, with current-cache identity/menu/recipe/animation checks. The tested build is installed and running; new visual results remain to be confirmed in the client.

| Skill | Added in this pass | Remaining scope |
|---|---|---|
| Crafting | 103 recipes: leather/cloth, spinning, gold/silver jewellery, glass, pottery |Special recipes/systems beyond the listed families |
| Summoning | 82 pouches and 79 scroll conversions at full obelisks |Familiar spawning, combat, special moves, follower UI and storage |
| Runecrafting | 13 ordinary altars; six basic ruin entrances/exits; essence, multipliers and XP |Abyss, essence pouches, combination runes, Soul altar and other advanced systems |
| Divination | 12 tiers of memories/energy; energy, XP and extra-XP conversion modes |Wisp-to-spring transformation/depletion, transmutation, special gear/perks |
| Thieving | 69 pickpocket NPC IDs and 35 stall IDs; rewards, failures, stun and depleted stalls |Guard aggression and special NPC systems; stun damage is currently nonlethal |
| Hunter |Four ordinary butterfly species; nets, jars, release and respawn |Traps, birds, implings and other methods |
| Agility |Complete seven-obstacle beginner Gnome course, ordered lap bonus |Other courses, advanced course, shortcuts and optional reward hooks |

Wisps pause while players harvest and resume wandering after the last player stops. Natural gathering NPCs reuse the original spawn data and existing region loading: 162 valid current-cache positions across 22 regions, 12 ordinary wisps and 4 butterflies. Four blocked old positions are skipped. The rest of the world retains the existing general NPC spawn scope. Temporary diagnostic spawns still work.

## Quick checks

Use an open backpack. ;;item adds only what fits. Diagnostic objects spawn at your current tile; move clear of their footprint before clicking. No new items or skill levels are granted automatically on login.

1. **Crafting:** `;;item 1733 1` (needle), `;;item 1734 10` (thread), `;;item 1741 10` (leather). Craft the leather or use the needle on it, select boots, then Make 5. Check materials, items and XP. For glass, use pipe1785 with molten glass1775. For pottery, use soft clay1761 at wheel2642 and fire the result at oven2643.
2. **Summoning:** obtain pouch12155, gold charm12158, wolf bones2859 and 7 spirit shards12183. Use a full obelisk (diagnostic `;;obj 67036`) and choose Spirit wolf pouch. Reopen with the pouch to make 10 Howl scrolls. Both recipes work at level 1; summoning the familiar itself is still outside this batch.
3. **Runecrafting:** carry Air talisman1438 and rune essence1436 (leave a slot for the talisman). `;;tele 3128 3403 0` places you beside the actual Air ruins. Enter, use the altar/Craft runes, then leave through the portal. Diagnostic Air altar: `;;obj 2478`.
4. **Divination:** Natural Pale wisps are around (3125, 3215, 0). For a local diagnostic, `;;npc 18150` creates one; move beside it and Harvest. Use an Energy rift (diagnostic `;;obj 87306`) to convert memories; Configure offers the three supported modes.
5. **Thieving:** `;;npc 1`, step off its tile and Pickpocket. Check coins/XP on success and temporary stun/damage on failure. `;;obj 635` creates a Tea stall: steal, wait for restoration, steal again.
6. **Hunter:** ordinary butterfly catching starts at level 15. Equip net10010, carry empty jars10012, and Catch a Ruby harvest (NPC5085), naturally found around (2328, 3526, 0). Release the filled jar10020. Check that the butterfly reappears. Optional local test command `;;nxt level 21 15` explicitly changes your saved Hunter level and XP; omit it to preserve natural progression.
7. **Agility:** `;;nxt agility` moves north of the Gnome log. Follow log, net, tree up, rope, tree down, net, pipe. Check balancing/crawling animations, plane changes, XP, restored running and lap completion.

For level-gated diagnostics, `;;nxt level <skillID> <1-99>` is restricted to the existing opt-in local 950 development connection and the ported gathering/production skills. It deliberately changes that character's saved level/XP. IDs for this batch: Crafting12, Agility16, Thieving17, Runecrafting20, Hunter21, Summoning23, Divination25.

Full item/recipe/station lists are in protocol-analysis/crafting-summoning-recipes-950.tsv, crafting-summoning-items-950.tsv and crafting-summoning-objects-950.tsv. Technical scope/evidence: crafting-summoning-port-950.md, runecrafting-divination-port-950.md, thieving-hunter-port-950.md and agility-port-950.md in protocol-analysis. Prior woodcutting, firemaking, mining, smelting, fishing, cooking, fletching, gem crafting, herblore and prayer remain available.
