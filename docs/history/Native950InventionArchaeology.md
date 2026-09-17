# First-pass Invention and Archaeology for revision 950

This pass makes both skills trainable through world objects, the normal production menu, the existing ActionManager, inventory transactions and Skills XP. It is a training baseline; it does not claim complete retail skill parity.

## Invention

- Public Inventor's workbench: **2967,3407,0**, near the Falador lodestone. The paired cache also places Discover/Manufacture workbenches at **6168/6169,1046,0** in the guild interior. The public workbench keeps training accessible while the guild entrance/tutorial travel is still unported.
- Workbench menu: view the component pouch, disassemble ordinary backpack items, manufacture products, choose a quantity. Components are personal and persisted; new characters receive no free components.
- Invention requires base level 80 in Crafting, Smithing and Divination, matching the original910 Disassemble entry policy. Manufactured products additionally require the current950 Invention level. Invention uses the existing elite XP curve.
- The original `data/items/itemsDisassembleData.json` provides required item quantities, material chances, junk chance and XP. The new resource contains **10,844 ordinary rows whose item IDs and names match the old910 and current950 item dumps**, and pins each current item record. Noted/lent/bound/shard templates and modified/charged/augmented item instances are rejected.
- Component indices were rebound by current cache material names. **910 Junk index75 is Historic components in950; current Junk is82.** Current material enum10742 and each chosen material data record are hash checked. Unknown or changed item/material identities fail closed.
- Eight current-cache manufacturing recipes: charge pack36389, weapon/armour/tool gizmo shells36719/36721/36723, augmentor36725, equipment siphon36730, divine charge36390 and empty divine charge41073. The product's950 parameters supply its level, XP, materials, physical inputs and alternate divine energies. Components are removed only after the inventory product transaction succeeds; full backpacks do not destroy components.
- Blueprint discovery/minigame, augmented-item XP, perks, gizmo installation, charge-pack effects, siphoning, machines and most manufactured-device effects remain for later passes. This implementation makes manufacturing and disassembly trainable; manufacturing a device does not imply its gameplay effect is implemented.

## Archaeology

No Archaeology implementation was found in the original910 source, so this is a new baseline using verified950 assets and recipe parameters, not a literal910 port.

- Archaeology campus natural workbench115421: **3355,3393,0**, with the current cache's Restore and Store options.
- Permanent starter plots beside it: **Centurion remains116393 at3363,3393,0** (level1) and **Venator remains117101 at3368,3393,0** (level5). These replace dependence on the retail tutorial's conditional/server-owned placement. Placement checks the complete footprint, an adjacent walkable border, and existing original/dynamic objects; repeated region loads do not duplicate plots.
- Carry or wield an ordinary mattock at a level permitted by its current950 item data; bronze mattock is49539. The action walks into reach before digging, stops on movement/removal/tool loss, awards materials and eventually a damaged artefact, and reports full backpack explicitly. The initial excavation cadence/reward pacing is deliberately simple server policy; it is not the retail excavation probability system. Animation830 is the existing spade/digging fallback pending native mattock animation refinement.
- Store backpack materials using the workbench's **Store** option. Materials go to a personal persisted store. Restore consumes a damaged artefact from the backpack and the stored materials atomically. This is necessary because a Venator recipe needs28 unstackable materials plus its damaged artefact, which cannot all fit in28 backpack slots.
- Three current-cache restoration recipes: Centurion's dress sword49741->49742 at level1, Venator dagger49921->49922 and Venator light crossbow49923->49924 at level5. Levels, ingredient quantities and restoration XP come from the950 product record; Venator restoration is305.1 base XP. Excavation progress survives walking to store materials during the same login; an unfinished excavation cycle resets after logout. XP, material storage and inventory contents persist normally.
- Saved material slots are append-only:0=imperial iron49444,1=purpleheart wood49445,2=Third Age iron49460,3=Zarosian insignia49514. The64-slot save shape leaves room for more sites without renumbering current state.
- Soil screening, complete digsites, collections/chronotes, relics, mysteries, qualifications and Time Sprite mechanics are not part of this pass.

## Verification

- Twelve JUnit tests: cloned save snapshots, clean initial state, shape/negative/overflow rejection, correct950 Junk index, guaranteed/weighted910 component quantities and storage/backpack recipe separation.
- `Native950InventionArchaeologyAcceptance`: **434 engine ticks,512 decoded950 frames,67 stat updates**; all8 manufactured recipes, all3 restorations, actual ordinary disassembly, full-backpack storage/resume yielding an artefact, level1 excavation, tool/level/cancellation/distance safeguards. No listener, player save file or authentication was used.
- `Native950InventionPlacementAcceptance`: current-cache clear placement and repeated-region idempotence for all3 public objects.
- `Native950InventionRoutingAcceptance`: **217 engine ticks,337 decoded950 frames, passed against the combined central routes**; encrypted950 object clicks through `Native950Interactions`, walking before processing, disassembly/quantity/retired-menu checks, manufacture, excavation, Store option2 and restoration quantity menu. All tested menus reached their normal handlers.
- Live client rendering and actual mouse interaction still need manual verification. Definition options and decoded packets do not substitute for visual checking.

## Integration hooks

- `Native950Invention.accepts(object,option)`, `choices(player,object)`, `isStation(object)`, `itemEntries()`, `verifyCacheBindings()`.
- `Native950Archaeology.accepts(object,option)`, `isExcavation(object)`, `start(player,object)`, `isStation(object)`, `choices(player,object)`, `storeMaterials(player,object)`, `itemEntries()`, `verifyCacheBindings()`.
- After the world thread loads a region, call both classes' `populateRegion(regionId)`; they only act on11829 and13365 respectively.
- Save `Native950Invention.materials(player)` as128 integers and `Native950Archaeology.materials(player)` as64 integers; restore with the corresponding `restoreMaterials` method. Methods return/accept defensive copies and validate exact shape and nonnegative amounts. Root schema4 integration owns the actual serialization.

