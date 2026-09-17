# Inventory menus and selected targets, revision950

The inventory problem is not a failure to decode the ordinary inventory-option item opcodes. Native item menus are authored by the client cache, and the server independently controls which operations can send packets and which selected targets are allowed. The previous adapter mixed those layers.

## Missing Use

Backpack1473:5 originally received mask2360382 (`0x24043e`). That retained drag parent depth1, drag-target bit21 and operation bits1..5/10, but zeroed source target bits11..17 and destination bit22. Native950 selection setter `0x14019f4e8` reads `(events >>> 11) & 127`; the caption helper `0x14019f728` rejects a zero source target mask. Item-to-item menu construction at `0x1401699b8` additionally requires destination bit22 and source target32. NPC/object targets require source bits2/4 at `0x14016d2cd` and `0x14016deef`.

`Native950InventoryMenu.EVENT_MASK` is now6633470 (`0x6537fe`): all ten cache-authored operation slots, source targetsNPC2/object4/component-item32, drag parent depth1, drag-target21 and Use-target22. The mask does not invent captions: an item's native scripts still choose their text and whether Use exists. Ground-item/player/world-tile selected families are not enabled.

Both the live Kotlin `Ataraxia950Handoff.interfaceBootstrap -> Native950InterfaceBootstrap` path and the engine binding table's `bootstrap.slots` recipe use this mask. Bank open/close attach/detach517 and leave backpack1473 mounted; ribbon inventory toggles change native wrapper visibility/layout, preserving its event namespace. No changes were made to the947 reference frontend.

## Why visible Drop did nothing

Actual950 cache chain8677 ->8678 ->8680 ->12090 ->2833 ->2410 builds ordinary backpack menus. Script2410 maps cache option1/2/3/4/5 onto interface operation1/2/3/7/8. Examine is10. Therefore Drop, normally cache option5, is UI operation8, client opcode66. The prior adapter both omitted operation8 from its server event mask and treated native operations as consecutive item-definition indices. Native onOp hooks can provide visible captions even when server event bits disable the resulting packet.

`ordinaryCacheOption` now exposes that verified mapping. `usesOrdinaryOperations` rejects the71 hard-coded IDs, eight categories and params6799/4840 that switch2833 to specialized menu scripts. It also rejects unresolved/malformed definitions and unsupported transformed template types; certificates require an ordinary valid base. This guard prevents an operation8 from a different menu being interpreted as a destructive Drop. Ordinary coins995, logs1511, bones526, noted bones527, tinderbox590, needle1733, leather1741, raw/cooked shrimps317/315 and wolf pouch12047 were checked in the actual cache and pass. Clue scroll2677 has ordinary Destroy at cache option5; it must not become Drop.

Category matching is verified at the native field level: normalized CS2 opcode `0x46b` maps to950 `0x60a`, registered at `0x1400610bb` with callback `0x1400a6160`. Its getter reads unsigned16 field `+0x228` at `0x1400a6177`; item definition opcode94 stores that exact field at `0x14036f5a0`. The guard therefore uses the same category that the native specialized-menu switch sees.

Menu/caption availability does not mean every item effect has been ported. Specialized menus and unsupported ordinary effects must return an explanatory message; no numeric option is forwarded blindly into unrelated910 handlers.

## Selected item target packets

Selection state identity is established by native setter `0x14019f4c7` (component hash), `0x14019f4df` (dynamic slot), `0x14019f50b` (item getter). It is shared with the already derived item-to-item69.

| Packet | Exact native950 body |
|---|---|
|Object target90,18bytes; writer `0x1400e516b..0x1400e5372`|byte0 modifierNeg;1..2 x BEu16+128;3..4 sourceSlot BEu16;5..6 y LEu16+128;7..9 sourceItem LEu24;10..13 objectID LEi32;14..17 sourceHash bytes8,0,24,16|
|NPC target19,12bytes; writer `0x1400e56fb..0x1400e5800`|0..1 NPCindex BEu16;2..4 sourceItem LEu24;5 modifierNeg;6..7 sourceSlot LEu16;8..11 sourceHash BEi32|

`ItemOnObjectAction` and `ItemOnNpcAction` retain immutable client claims only. The world checks backpack origin, exact slot/item identity, supported relationship, target visibility/current-world ownership, collision approach and final proximity. Coordinates are bounded14bit, modifier0/1, objectID nonnegative. Absent source slot/item sentinels remain -1 for world rejection. Ground-target112 stays underived/disabled.

## Reproducible evidence and checks

`tools/verify_950_inventory_menus.py` inspects the unmodified950 clientSHA256 `fc7492548627a4068e88a7a45ba4f0be93004adf246bb4b174b24ae588768b36`, nine exact cache scripts, their normalized instruction indices, specialized switch tables, selection-mask instructions and the object/NPC target writers. The generated `inventory-menus-950-evidence.json` records byte slices, hashes and instruction listings. The tool passed against the actual cache. Relevant scripts are runtime pinned in both the established engine UI binding table and the Kotlin handoff validation.

JUnit checks cover source and target event gates, preserved drag behavior, literal native950 IF_SETEVENTS bytes, actual loaded binding recipe, nonconsecutive operation mapping, specialized guard branches, asymmetric object/NPC selected-target fixtures, wide item IDs, source sentinels and malformed field/framing bounds. The bridge verifier additionally inspects the actual Kotlin bootstrap packet. These checks establish wire/cache behavior; final visible menu/Use/Drop/drag checks belong to the in-game playtest.
